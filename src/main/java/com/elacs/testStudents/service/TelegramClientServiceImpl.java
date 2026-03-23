package com.elacs.testStudents.service;

import com.elacs.testStudents.model.TelegramSession;
import com.elacs.testStudents.repository.TelegramSessionRepository;
import it.tdlight.Init;
import it.tdlight.client.APIToken;
import it.tdlight.client.AuthenticationSupplier;
import it.tdlight.client.ClientInteraction;
import it.tdlight.client.InputParameter;
import it.tdlight.client.ParameterInfo;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.client.SimpleTelegramClientBuilder;
import it.tdlight.client.SimpleTelegramClientFactory;
import it.tdlight.client.TDLibSettings;
import it.tdlight.jni.TdApi;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TelegramClientServiceImpl implements TelegramClientService {

    private static final String API_ID_STR  = System.getenv("TELEGRAM_API_ID");
    private static final String API_HASH    = System.getenv("TELEGRAM_API_HASH");
    private static final String PHONE       = System.getenv("TELEGRAM_PHONE");
    private static final Path   SESSION_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "tg-session");

    @Value("${telegram.read.interval.minutes:5}")
    private int sessionFlushIntervalMinutes;

    private final TelegramSessionRepository sessionRepository;
    private final TaskScheduler taskScheduler;

    private SimpleTelegramClientFactory factory;
    private SimpleTelegramClient client;

    private volatile AuthState authState = AuthState.NOT_STARTED;
    private volatile Instant floodWaitUntil = Instant.EPOCH;
    private static final Pattern FLOOD_WAIT_PATTERN = Pattern.compile("FLOOD_WAIT_(\\d+)");

    private final AtomicReference<CompletableFuture<String>> pendingCode = new AtomicReference<>();
    private final AtomicReference<CompletableFuture<String>> pending2fa  = new AtomicReference<>();

    public TelegramClientServiceImpl(TelegramSessionRepository sessionRepository,
                                     TaskScheduler taskScheduler) {
        this.sessionRepository = sessionRepository;
        this.taskScheduler = taskScheduler;
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @PostConstruct
    public void init() {
        if (API_ID_STR == null || API_ID_STR.isBlank()) {
            log.warn("TELEGRAM_API_ID env var not set — Telegram integration disabled");
            authState = AuthState.DISABLED;
            return;
        }

        // Schedule periodic session flush (every N minutes)
        taskScheduler.scheduleAtFixedRate(
                this::flushSessionToDb,
                Instant.now().plus(Duration.ofMinutes(sessionFlushIntervalMinutes)),
                Duration.ofMinutes(sessionFlushIntervalMinutes));

        // Initialize TDLib in a background thread so startup is not blocked
        new Thread(this::initClient, "tdlib-init").start();
    }

    @PreDestroy
    public void destroy() {
        flushSessionToDb();
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                log.warn("Error closing TDLib client", e);
            }
        }
        if (factory != null) {
            try {
                factory.close();
            } catch (Exception e) {
                log.warn("Error closing TDLib factory", e);
            }
        }
        authState = AuthState.CLOSED;
    }

    // -------------------------------------------------------------------------
    // TDLib initialization
    // -------------------------------------------------------------------------

    private void initClient() {
        try {
            Init.init();

            restoreSessionFromDb();

            int apiId = Integer.parseInt(API_ID_STR.trim());
            APIToken apiToken = new APIToken(apiId, API_HASH);

            TDLibSettings settings = TDLibSettings.create(apiToken);
            settings.setDatabaseDirectoryPath(SESSION_DIR);
            settings.setDownloadedFilesDirectoryPath(SESSION_DIR.resolve("downloads"));

            factory = new SimpleTelegramClientFactory();
            // Builder methods return void — no chaining, use variable
            SimpleTelegramClientBuilder builder = factory.builder(settings);
            builder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onAuthorizationState);
            builder.setClientInteraction(new WebClientInteraction());
            client = builder.build(AuthenticationSupplier.user(PHONE));

            log.info("TDLib client started, waiting for authorization...");
        } catch (Exception e) {
            log.error("Failed to initialize TDLib client", e);
            authState = AuthState.ERROR;
        }
    }

    private void restoreSessionFromDb() {
        sessionRepository.findById(1L).ifPresent(session -> {
            if (session.getBinlog() != null && session.getBinlog().length > 0) {
                try {
                    Files.createDirectories(SESSION_DIR);
                    Files.write(SESSION_DIR.resolve("td.binlog"), session.getBinlog());
                    log.info("Session restored from DB ({} bytes)", session.getBinlog().length);
                } catch (IOException e) {
                    log.error("Failed to restore session binlog to disk", e);
                }
            }
        });
    }

    private void onAuthorizationState(TdApi.UpdateAuthorizationState update) {
        TdApi.AuthorizationState state = update.authorizationState;
        log.info("TDLib auth state: {}", state.getClass().getSimpleName());

        if (state instanceof TdApi.AuthorizationStateReady) {
            authState = AuthState.AUTHORIZED;
            log.info("Telegram authorized successfully");
            flushSessionToDb();

        } else if (state instanceof TdApi.AuthorizationStateWaitCode) {
            authState = AuthState.WAITING_CODE;

        } else if (state instanceof TdApi.AuthorizationStateWaitPassword) {
            authState = AuthState.WAITING_2FA;

        } else if (state instanceof TdApi.AuthorizationStateClosed) {
            authState = AuthState.CLOSED;
            log.warn("TDLib client closed — clearing stale session and restarting");
            clearSessionFromDb();
            new Thread(this::restartClient, "tdlib-restart").start();
        }
    }

    // -------------------------------------------------------------------------
    // TelegramClientService interface
    // -------------------------------------------------------------------------

    @Override
    public AuthState getAuthState() {
        return authState;
    }

    @Override
    public Instant getFloodWaitUntil() {
        return floodWaitUntil;
    }

    @Override
    public void submitAuthCode(String code) {
        CompletableFuture<String> future = pendingCode.get();
        if (future != null) {
            future.complete(code);
        } else {
            log.warn("No pending auth code request — code submitted too early or already completed");
        }
    }

    @Override
    public void submit2faPassword(String password) {
        CompletableFuture<String> future = pending2fa.get();
        if (future != null) {
            future.complete(password);
        } else {
            log.warn("No pending 2FA request");
        }
    }

    @Override
    public void markTopicAsRead(long chatId, long topicId) {
        if (authState != AuthState.AUTHORIZED || client == null) {
            log.warn("Cannot mark as read: not authorized (state={})", authState);
            return;
        }
        if (Instant.now().isBefore(floodWaitUntil)) {
            log.warn("Skipping markTopicAsRead: FLOOD_WAIT active until {}", floodWaitUntil);
            return;
        }
        if (topicId == 0) {
            markChatAsRead(chatId);
        } else {
            markForumTopicAsRead(chatId, topicId);
        }
    }

    @Override
    public void flushSessionToDb() {
        Path binlogPath = SESSION_DIR.resolve("td.binlog");
        if (!Files.exists(binlogPath)) {
            return;
        }
        try {
            byte[] data = Files.readAllBytes(binlogPath);
            TelegramSession session = sessionRepository.findById(1L)
                    .orElse(TelegramSession.builder().id(1L).build());
            session.setBinlog(data);
            session.setUpdatedAt(LocalDateTime.now());
            sessionRepository.save(session);
            log.debug("Session flushed to DB ({} bytes)", data.length);
        } catch (IOException e) {
            log.error("Failed to flush session to DB", e);
        }
    }

    @Override
    public List<TopicInfo> getForumTopics(long chatId) {
        if (authState != AuthState.AUTHORIZED || client == null) {
            return List.of();
        }
        try {
            TdApi.ForumTopics result = client
                    .send(new TdApi.GetForumTopics(chatId, "", 0, 0, 0, 100))
                    .get(10, TimeUnit.SECONDS);
            return Arrays.stream(result.topics)
                    .map(t -> new TopicInfo(t.info.messageThreadId, t.info.name))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to load forum topics for chat {}: {}", chatId, e.getMessage());
            return List.of();
        }
    }

    @Override
    public void restartAuth() {
        log.info("Manual restart requested");
        clearSessionFromDb();
        new Thread(this::restartClient, "tdlib-restart").start();
    }

    @Override
    public List<ChatInfo> getChats() {
        if (authState != AuthState.AUTHORIZED || client == null) {
            return List.of();
        }
        try {
            TdApi.Chats chats = client.send(new TdApi.GetChats(null, 50))
                    .get(10, TimeUnit.SECONDS);

            List<CompletableFuture<ChatInfo>> futures = new ArrayList<>();
            for (long chatId : chats.chatIds) {
                CompletableFuture<ChatInfo> f = client.send(new TdApi.GetChat(chatId))
                        .thenApply(chat -> new ChatInfo(chat.id, chat.title))
                        .exceptionally(e -> new ChatInfo(chatId, "id:" + chatId));
                futures.add(f);
            }

            return futures.stream()
                    .map(f -> {
                        try {
                            return f.get(5, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to load chats", e);
            return List.of();
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void handleFloodWait(Throwable e) {
        String msg = e.getMessage() != null ? e.getMessage() : "";
        Matcher m = FLOOD_WAIT_PATTERN.matcher(msg);
        if (m.find()) {
            long seconds = Long.parseLong(m.group(1));
            floodWaitUntil = Instant.now().plusSeconds(seconds);
            log.warn("Telegram FLOOD_WAIT: pausing all requests for {} seconds (until {})", seconds, floodWaitUntil);
        }
    }

    private void clearSessionFromDb() {
        try {
            sessionRepository.deleteById(1L);
            Files.deleteIfExists(SESSION_DIR.resolve("td.binlog"));
            log.info("Stale session cleared from DB and disk");
        } catch (Exception e) {
            log.warn("Failed to clear stale session", e);
        }
    }

    private void restartClient() {
        if (client != null) {
            try { client.close(); } catch (Exception ignored) {}
            client = null;
        }
        if (factory != null) {
            try { factory.close(); } catch (Exception ignored) {}
            factory = null;
        }
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        authState = AuthState.NOT_STARTED;
        initClient();
    }

    private void markChatAsRead(long chatId) {
        client.send(new TdApi.OpenChat(chatId))
                .thenCompose(ok -> client.send(new TdApi.GetChatHistory(chatId, 0, 0, 1, false)))
                .thenAccept(messages -> {
                    client.send(new TdApi.CloseChat(chatId));
                    if (messages.messages.length == 0) {
                        log.debug("Chat {} has no messages, skipping", chatId);
                        return;
                    }
                    long lastId = messages.messages[0].id;
                    client.send(new TdApi.ViewMessages(chatId, new long[]{lastId}, null, true))
                            .thenAccept(v -> log.info("Chat {} marked as read", chatId))
                            .exceptionally(e -> {
                                log.warn("Failed to view messages in chat {}: {}", chatId, e.getMessage());
                                return null;
                            });
                })
                .exceptionally(e -> {
                    client.send(new TdApi.CloseChat(chatId));
                    handleFloodWait(e);
                    log.warn("Failed to get history for chat {}: {}", chatId, e.getMessage());
                    return null;
                });
    }

    private void markForumTopicAsRead(long chatId, long topicId) {
        // topicId = messageThreadId (returned by GetForumTopics, NOT the sequential number in URL)
        client.send(new TdApi.OpenChat(chatId))
                .thenCompose(ok -> client.send(new TdApi.GetForumTopic(chatId, topicId)))
                .thenAccept(topic -> {
                    client.send(new TdApi.CloseChat(chatId));
                    if (topic.lastMessage == null) {
                        log.debug("Forum topic {} in chat {} has no messages, skipping", topicId, chatId);
                        return;
                    }
                    if (topic.unreadCount == 0) {
                        log.debug("Forum topic {} in chat {} already read", topicId, chatId);
                        return;
                    }
                    long lastId = topic.lastMessage.id;
                    client.send(new TdApi.ViewMessages(chatId, new long[]{lastId},
                                    new TdApi.MessageSourceForumTopicHistory(), true))
                            .thenAccept(v -> log.info("Topic {} in chat {} marked as read (was {} unread)",
                                    topicId, chatId, topic.unreadCount))
                            .exceptionally(e -> {
                                handleFloodWait(e);
                                log.warn("Failed to view messages in topic {} chat {}: {}", topicId, chatId, e.getMessage());
                                return null;
                            });
                })
                .exceptionally(e -> {
                    client.send(new TdApi.CloseChat(chatId));
                    handleFloodWait(e);
                    log.warn("Failed to get forum topic {} in chat {}: {}", topicId, chatId, e.getMessage());
                    return null;
                });
    }

    // -------------------------------------------------------------------------
    // ClientInteraction — receives user-input requests from TDLib
    // -------------------------------------------------------------------------

    private class WebClientInteraction implements ClientInteraction {
        @Override
        public CompletableFuture<String> onParameterRequest(InputParameter parameter,
                                                             ParameterInfo parameterInfo) {
            log.info("TDLib requesting parameter: {}", parameter);
            return switch (parameter) {
                case ASK_CODE -> {
                    authState = AuthState.WAITING_CODE;
                    CompletableFuture<String> future = new CompletableFuture<>();
                    pendingCode.set(future);
                    yield future;
                }
                case ASK_PASSWORD -> {
                    authState = AuthState.WAITING_2FA;
                    CompletableFuture<String> future = new CompletableFuture<>();
                    pending2fa.set(future);
                    yield future;
                }
                // These require no user input — return empty string to acknowledge
                case NOTIFY_LINK, TERMS_OF_SERVICE ->
                        CompletableFuture.completedFuture("");
                default -> {
                    log.warn("TDLib requested unhandled parameter: {}", parameter);
                    yield CompletableFuture.completedFuture("");
                }
            };
        }
    }
}
