package com.elacs.testStudents.service;

import java.util.List;

public interface TelegramClientService {

    enum AuthState {
        /** TELEGRAM_API_ID not configured — integration disabled */
        DISABLED,
        /** TDLib is initializing */
        NOT_STARTED,
        /** Waiting for SMS/Telegram auth code */
        WAITING_CODE,
        /** Waiting for 2FA cloud password */
        WAITING_2FA,
        /** Authorized and ready */
        AUTHORIZED,
        /** Client closed */
        CLOSED,
        /** Unrecoverable error */
        ERROR
    }

    record ChatInfo(long id, String title) {}

    AuthState getAuthState();

    /** Submit the code from SMS / Telegram notification */
    void submitAuthCode(String code);

    /** Submit the cloud 2FA password (if 2FA is enabled on the account) */
    void submit2faPassword(String password);

    /**
     * Mark all messages in a chat or forum topic as read.
     * @param topicId 0 for the whole chat; otherwise the message_thread_id of the forum topic
     */
    void markTopicAsRead(long chatId, long topicId);

    /** Read td.binlog from the temp directory and persist it to the DB */
    void flushSessionToDb();

    /** Return the first 50 chats from the account (requires AUTHORIZED state) */
    List<ChatInfo> getChats();
}
