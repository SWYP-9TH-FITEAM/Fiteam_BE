// src/ChatApi/ChatRoom.jsx
import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { fetchMessages } from './api.js';

const WS_URL = 'http://localhost:8080/ws/chat';

export default function ChatRoom({ token, userId, roomId }) {
  const [messages, setMessages] = useState([]);
  const [draft, setDraft]       = useState('');
  const stompRef                = useRef(null);
  const endRef                  = useRef();

  // ① 메시지 이력 로드
  useEffect(() => {
    if (!roomId) return;
    fetchMessages(token, roomId)
      .then(res => {
        const list = Array.isArray(res) ? res : res.content;
        setMessages(list.reverse());
        endRef.current?.scrollIntoView({ block: 'end' });
      })
      .catch(console.error);
  }, [token, roomId]);

  // ② STOMP 연결 & 구독
  useEffect(() => {
    if (!roomId) return;
    const client = Stomp.over(new SockJS(WS_URL));
    client.connect(
      { Authorization: `Bearer ${token}` },
      () => {
        client.subscribe(`/topic/chatroom.${roomId}`, ({ body }) => {
          setMessages(prev => [...prev, JSON.parse(body)]);
          endRef.current?.scrollIntoView({ block: 'end' });
        });
      },
      err => console.error('WebSocket 오류:', err)
    );
    stompRef.current = client;
    return () => stompRef.current.disconnect();
  }, [token, roomId]);

  // ③ 메시지 전송 (필수 필드 모두 포함)
  const sendMessage = () => {
    const content = draft.trim();
    if (!content || !stompRef.current) return;

    const payload = {
      chatRoomId:  roomId,
      senderId:    userId,
      senderType:  'USER',
      messageType: 'TEXT',
      content,
    };

    stompRef.current.send(
      '/app/chat.sendMessage',
      { Authorization: `Bearer ${token}` },
      JSON.stringify(payload)
    );
    setDraft('');
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <div style={{ flex: 1, overflowY: 'auto', padding: 16, background: '#fafafa' }}>
        {messages.map((m, i) => (
          <div
            key={i}
            style={{
              textAlign: m.senderId === userId ? 'right' : 'left',
              margin: '4px 0'
            }}
          >
            <span
              style={{
                display: 'inline-block',
                padding: 8,
                borderRadius: 8,
                background: m.senderId === userId ? '#d1ffd1' : '#fff'
              }}
            >
              {m.content}
            </span>
          </div>
        ))}
        <div ref={endRef} />
      </div>
      <div style={{ display: 'flex', borderTop: '1px solid #ddd', padding: 12 }}>
        <input
          value={draft}
          onChange={e => setDraft(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && sendMessage()}
          placeholder="메시지를 입력하세요"
          style={{ flex: 1, padding: 8 }}
        />
        <button onClick={sendMessage} style={{ marginLeft: 8, padding: '8px 16px' }}>
          전송
        </button>
      </div>
    </div>
  );
}
