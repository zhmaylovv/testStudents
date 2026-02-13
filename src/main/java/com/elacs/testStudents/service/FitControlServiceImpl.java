package com.elacs.testStudents.service;

import com.elacs.testStudents.dto.fit.ExternalErrorResponse;
import com.elacs.testStudents.dto.fit.FitFullSessionRq;
import com.elacs.testStudents.dto.fit.FitRefreshTokenRq;
import com.elacs.testStudents.dto.fit.FitStartSessionRq;
import com.elacs.testStudents.dto.fit.FitStartSessionRs;
import com.elacs.testStudents.dto.fit.Phone;
import com.elacs.testStudents.dto.fit.Token;
import com.elacs.testStudents.dto.fit.pojors.FitFullSessionRs;
import com.elacs.testStudents.dto.fit.pojotranings.FitTrainingRs;
import com.elacs.testStudents.dto.fit.pojotranings.GroupTrainingSchedule;
import com.elacs.testStudents.exceptions.RefreshTokenErrorException;
import com.elacs.testStudents.exceptions.SessionErrorException;
import com.elacs.testStudents.exceptions.SubscribeError;
import com.elacs.testStudents.exceptions.VerificationAlreadySentException;
import com.elacs.testStudents.model.FitData;
import com.elacs.testStudents.repository.FitDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FitControlServiceImpl implements FitControlService {
    private static final String URL = System.getenv("FIT_URL");
    private static final String PHONE = System.getenv("FIT_PHONE");
    private static final String SIGNATURE = System.getenv("FIT_SIGN");
    private final RestClient restClient;
    private final FitDataRepository fitDataRepository;
    private final ObjectMapper mapper;
    @Lazy
    @Autowired
    private SchedulerService schedulerService;

    @Override

    public String getTempToken() {
        FitStartSessionRq rq = FitStartSessionRq.builder()
                .phone(Phone.builder()
                        .countryCode("7")
                        .number(PHONE)
                        .build())
                .signature(SIGNATURE)
                .build();
        FitStartSessionRs response = null;
        try {
            response = restClient.post()
                    .uri("https://" + URL + "/authorization/sendVerificationCode")
                    .body(rq)
                    .headers(headers -> {
                        headers.set("Accept", "application/json");
                        headers.set("timezone", "+0700");
                        headers.set("System-Name", "android");
                        headers.set("System-Version", "16");
                        headers.set("Platform", "Google google Pixel 9");
                        headers.set("Locate", "ru-Ru");
                        headers.set("App-Version", "6.5.0");
                        headers.set("Content-Type", "application/json; charset=utf-8");
                        headers.set("Host", URL);
                        headers.set("Connection", "Keep-Alive");
                        headers.set("Accept-Encoding", "gzip");
                        headers.set("User-Agent", "okhttp/5.0.0-alpha.3");
                    })
                    .retrieve().body(FitStartSessionRs.class);
        } catch (HttpClientErrorException.Forbidden e) {
            ExternalErrorResponse external = e.getResponseBodyAs(ExternalErrorResponse.class);
            if (external.error().type().equals("VERIFICATION_CODE_ALREADY_SEND_ANDROID")) {
                throw new VerificationAlreadySentException(external.error().message());
            }
        }
        assert response != null;
        log.info(response.toString());
        return response.result().token();
    }

    @Override
    public void getFullSession(String tempToken, String code) {
        FitFullSessionRq rq = FitFullSessionRq.builder()
                .token(tempToken)
                .verificationCode(code)
                .externalCompanySource(null)
                .type("sms")
                .build();
        FitFullSessionRs rs = restClient.post()
                .uri("https://" + URL + "/authorization/basic/v2")
                .body(rq)
                .headers(headers -> {
                    headers.set("Accept", "application/json");
                    headers.set("timezone", "+0700");
                    headers.set("System-Name", "android");
                    headers.set("System-Version", "16");
                    headers.set("Platform", "Google google Pixel 9");
                    headers.set("Locate", "ru-Ru");
                    headers.set("App-Version", "6.5.0");
                    headers.set("Content-Type", "application/json; charset=utf-8");
                    headers.set("Host", URL);
                    headers.set("Connection", "Keep-Alive");
                    headers.set("Accept-Encoding", "gzip");
                    headers.set("User-Agent", "okhttp/5.0.0-alpha.3");
                })
                .retrieve().body(FitFullSessionRs.class);
        assert rs != null;
        log.info(rs.toString());
        saveSession(rs);
    }

    @Override
    public void refreshToken() {
        FitData data = fitDataRepository.findById(1L).orElseThrow(() -> new SessionErrorException("Сессия не найдена"));

        FitRefreshTokenRq rq = FitRefreshTokenRq.builder()
                .token(data.getToken())
                .refresh(data.getRefreshToken())
                .build();
        FitFullSessionRs rs = null;
        try {
            rs = restClient.post()
                    .uri("https://" + URL + "/authorization/refreshtoken")
                    .body(rq)
                    .headers(headers -> {
                        headers.set("Accept", "application/json");
                        headers.set("timezone", "+0700");
                        headers.set("System-Name", "android");
                        headers.set("System-Version", "16");
                        headers.set("Platform", "Google google Pixel 9");
                        headers.set("Locate", "ru-Ru");
                        headers.set("App-Version", "6.5.0");
                        headers.set("Content-Type", "application/json; charset=utf-8");
                        headers.set("Host", URL);
                        headers.set("Connection", "Keep-Alive");
                        headers.set("Accept-Encoding", "gzip");
                        headers.set("User-Agent", "okhttp/5.0.0-alpha.3");
                    })
                    .retrieve().body(FitFullSessionRs.class);
        } catch (Exception e) {
            throw new RefreshTokenErrorException("Не получилось обновить токен");
        }
        assert rs != null;
        log.info("TOKEN ALMOST REFRESHED" + rs.toString());
        saveSession(rs);
    }

    @Override
    public boolean isSessionAlive() {
        FitData data = fitDataRepository.findById(1L).orElseThrow(() -> new SessionErrorException("Сессия не найдена"));

        if (isTokenTimeExp(data.getExp())) {
            log.info("TOKEN IS EXPIRE");
            return false;
        }
        return Boolean.TRUE.equals(restClient.get()
                .uri("https://" + URL + "/club/0a863945-2cee-4f4b-8e63-a7985208ddb5/start")
                .headers(headers -> {
                    headers.set("Accept", "application/json");
                    headers.set("timezone", "+0700");
                    headers.set("System-Name", "android");
                    headers.set("System-Version", "16");
                    headers.set("Platform", "Google google Pixel 9");
                    headers.set("Locate", "ru-Ru");
                    headers.set("App-Version", "6.5.0");
                    headers.set("Content-Type", "application/json; charset=utf-8");
                    headers.set("Host", URL);
                    headers.set("Connection", "Keep-Alive");
                    headers.set("Accept-Encoding", "gzip");
                    headers.set("User-Agent", "okhttp/5.0.0-alpha.3");
                    headers.set("Token", data.getToken());
                }).exchange((request, response) -> {
                    return response.getStatusCode().is2xxSuccessful();
                }));
    }

    @Override
    public void subscribe(String id, LocalDateTime date) {
        if (!isSessionAlive()) refreshToken();
        FitData data = fitDataRepository.findById(1L).orElseThrow(() -> new SessionErrorException("Сессия не найдена"));
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateStr = date.format(dateTimeFormatter);
        String body = "{\"trainingDate\":\"" + dateStr + "\",\"subscriptionGuid\":\"" + id + "\"}";
        FitFullSessionRs rs = null;
        try {
            rs = restClient.post()
                    .uri("https://" + URL + "/groupTrainingSchedule/clubs/0a863945-2cee-4f4b-8e63-a7985208ddb5/training/" + id + "/signup/v2")
                    .body(body)
                    .headers(headers -> {
                        headers.set("Accept", "application/json");
                        headers.set("timezone", "+0700");
                        headers.set("System-Name", "android");
                        headers.set("System-Version", "16");
                        headers.set("Platform", "Google google Pixel 9");
                        headers.set("Locate", "ru-Ru");
                        headers.set("App-Version", "6.5.0");
                        headers.set("Content-Type", "application/json; charset=utf-8");
                        headers.set("Host", URL);
                        headers.set("Connection", "Keep-Alive");
                        headers.set("Accept-Encoding", "gzip");
                        headers.set("User-Agent", "okhttp/5.0.0-alpha.3");
                        headers.set("Token", data.getToken());
                    })
                    .retrieve().body(FitFullSessionRs.class);
        } catch (Exception e) {
            log.info("SUBSCRIBE ERROR" + e.getMessage());
            throw new SubscribeError("Не получилось записаться");
        }
        log.info("ВЫПОЛНЕНA ЗАДАЧА!!!");
    }

    @Override
    public List<GroupTrainingSchedule> getTrainingList() {
        FitData data = fitDataRepository.findById(1L).orElseThrow(() -> new SessionErrorException("Сессия не найдена"));
        LocalDate date = LocalDate.now().plusDays(1);
        String ss = "{\"day\":" + date.getDayOfMonth() + ",\"month\":" + date.getMonthValue() + ",\"year\":" + date.getYear() + ",\"filters\":[{\"id\":\"subdivision\",\"value\":[\"ADULT\"]}]}";
        FitTrainingRs rs = restClient.post()
                .uri("https://" + URL + "/groupTrainingSchedule/clubs/0a863945-2cee-4f4b-8e63-a7985208ddb5/v3")
                .body(ss)
                .headers(headers -> {
                    headers.set("Accept", "application/json");
                    headers.set("timezone", "+0700");
                    headers.set("System-Name", "android");
                    headers.set("System-Version", "16");
                    headers.set("Platform", "Google google Pixel 9");
                    headers.set("Locate", "ru-Ru");
                    headers.set("App-Version", "6.5.0");
                    headers.set("Content-Type", "application/json; charset=utf-8");
                    headers.set("Token", data.getToken());
                    headers.set("Host", URL);
                    headers.set("Connection", "Keep-Alive");
                    headers.set("Accept-Encoding", "gzip");
                    headers.set("User-Agent", "okhttp/5.0.0-alpha.3");
                })
                .retrieve().body(FitTrainingRs.class);
        assert rs != null;
        return rs.result().groupTrainingScheduleList();
    }

    @Override
    public void setSchedulerTask(String id, LocalDateTime dateTime) {
        schedulerService.scheduleTask(id, dateTime);
    }

    private boolean isTokenTimeExp(String exp) {
        long seconds = Double.valueOf(exp).longValue();
        return Instant.ofEpochSecond(seconds).plusSeconds(5).isBefore(Instant.now());
    }

    private void saveSession(FitFullSessionRs rs) {
        String[] parts = rs.result().access().token().split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        Token tokenObj = mapper.readValue(payload, Token.class);
        FitData data = FitData.builder()
                .token(rs.result().access().token())
                .refreshToken(rs.result().access().refresh())
                .id(1L)
                .cid(tokenObj.cid())
                .ecs(tokenObj.ecs())
                .exp(tokenObj.exp())
                .gid(tokenObj.gid())
                .iat(tokenObj.iat())
                .isr(tokenObj.isr())
                .jti(tokenObj.jti())
                .build();
        fitDataRepository.save(data);
    }
}
