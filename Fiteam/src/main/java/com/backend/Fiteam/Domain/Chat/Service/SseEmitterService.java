package com.backend.Fiteam.Domain.Chat.Service;

import com.backend.Fiteam.Domain.Chat.Dto.ChatRoomListResponseDto;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseEmitterService {

    private static final Logger log = LoggerFactory.getLogger(SseEmitterService.class);
    private static final Long TIMEOUT = 1000L * 60 * 30; // 30분

    private final Map<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Integer userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitters.put(userId, emitter);

        // 연결 종료 / 타임아웃 / 에러 시 emitter 제거
        emitter.onCompletion(() -> {
            emitters.remove(userId);
            log.info("SSE 연결 종료: userId={}", userId);
        });

        emitter.onTimeout(() -> {
            emitters.remove(userId);
            log.info("SSE 연결 타임아웃: userId={}", userId);
        });

        emitter.onError(e -> {
            emitters.remove(userId);
            log.warn("SSE 연결 에러: userId={}, error={}", userId, e.getMessage());
        });

        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("connected"));
        } catch (IOException ex) {
            emitters.remove(userId);
            log.error("초기 SSE 전송 실패: userId={}, error={}", userId, ex.getMessage());
        }

        log.info("SSE 구독 성공: userId={}", userId);
        return emitter;
    }

    public void pushRoomUpdate(Integer userId, ChatRoomListResponseDto delta) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("chat-room-updated")
                        .data(delta));
            } catch (IOException e) {
                emitters.remove(userId);
                log.warn("SSE 전송 실패: userId={}, error={}", userId, e.getMessage());
            }
        }
    }

    public boolean isConnected(Integer userId) {
        return emitters.containsKey(userId);
    }
}


