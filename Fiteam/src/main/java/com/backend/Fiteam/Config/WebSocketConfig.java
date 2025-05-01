package com.backend.Fiteam.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // WebSocket 연결 Endpoint 등록
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat") // 클라이언트가 연결할 WebSocket URL
                .setAllowedOriginPatterns("*") // 개발중이기에 "*" 로 설정
                .withSockJS(); // SockJS fallback 지원 (브라우저 호환성 ↑)
    }

    // 메시지 브로커 구성
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue"); // 메시지를 구독할 prefix
        registry.setApplicationDestinationPrefixes("/app"); // 클라이언트에서 보낼 prefix
    }
}
