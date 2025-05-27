package com.backend.Fiteam.Domain.Chat.Service;

import com.backend.Fiteam.Domain.Chat.Dto.ChatRoomListResponseDto;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseEmitterService {
    // userId → SseEmitter 매핑
    private final Map<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();

    /*
    public SseEmitter subscribe(Integer userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(userId, emitter);
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(    () -> emitters.remove(userId));

        System.out.println("SSE 구독 성공: userId=" + userId);
        return emitter;
    }
    */
    public SseEmitter subscribe(Integer userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(userId, emitter);

        // **최초 빈 이벤트(heartbeat) 전송**
        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")                  // FE 쪽에서 잡아도 되고, 잡지 않아도 됩니다
                    .data("connected"));
        } catch (IOException ex) {
            // 초기 전송이 실패하면 맵에서 제거
            emitters.remove(userId);
        }

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(    () -> emitters.remove(userId));
        System.out.println("SSE 구독 성공: userId=" + userId);
        return emitter;
    }



    // 기존 pushUpdate는 채팅방 전체 리스트용이었다면,
    // 아래는 단일 채팅방 델타(update one room) 전용입니다.
    public void pushRoomUpdate(Integer userId, ChatRoomListResponseDto delta) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("chat-room-updated")       // FE에서 listen 할 이벤트명
                        .data(delta)
                );
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
    }
}

