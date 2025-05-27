package com.backend.Fiteam.HyperClova;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChatStreamEvent {
    private String event;  // e.g. "completion"
    private String id;     // 이벤트 ID
    private String data;   // 조각별 응답 JSON
}
