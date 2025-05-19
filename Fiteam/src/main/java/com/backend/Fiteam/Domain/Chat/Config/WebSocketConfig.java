package com.backend.Fiteam.Domain.Chat.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.*;


@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    //private PresenceChannelInterceptor presenceChannelInterceptor;

    // WebSocket 연결 Endpoint 등록
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat") // 클라이언트가 연결할 WebSocket URL
                .setAllowedOriginPatterns("*") // 개발중이기에 "*" 로 설정
                .withSockJS();
    }

    // 메시지 브로커 구성
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue"); // 메시지를 구독할 prefix
        registry.setApplicationDestinationPrefixes("/app"); // 클라이언트에서 보낼 prefix
        registry.setUserDestinationPrefix("/user");          // 서버 → 특정 사용자
    }

    /*
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(presenceChannelInterceptor);
    }
     */
}
