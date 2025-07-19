// src/ChatTest.jsx
import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const API_BASE = 'http://localhost:8080/v1/user-chat';
const authHeaders = token => ({
  'Content-Type': 'application/json',
  Authorization: `Bearer ${token}`,
});

const ChatTest = ({ token }) => {
  const [rooms, setRooms] = useState([]);
  const [selectedRoomId, setSelectedRoomId] = useState(null);
  const [partnerId, setPartnerId] = useState('');
  const [messages, setMessages] = useState([]);
  const [draft, setDraft] = useState('');
  const stompClient = useRef(null);
  const scrollRef = useRef();

  // 1) 초기 방 목록 로드 & SSE 구독
  useEffect(() => {
    if (!token) return;

    // 기존 방 목록
    fetch(`${API_BASE}/list`, { headers: authHeaders(token) })
      .then(res => res.json())
      .then(setRooms)
      .catch(console.error);

    // SSE 구독 (INIT + 업데이트 델타)
    const es = new EventSource(`${API_BASE}/rooms/subscribe?token=${token}`);
    es.addEventListener('INIT', e => {
      setRooms(JSON.parse(e.data));
    });
    es.addEventListener('chat-room-updated', e => {
      const delta = JSON.parse(e.data);
      setRooms(prev => {
        const idx = prev.findIndex(r => r.chatRoomId === delta.chatRoomId);
        if (idx >= 0) {
          const updated = [...prev];
          updated[idx] = { ...updated[idx], ...delta };
          return updated;
        }
        return [delta, ...prev];
      });
    });
    return () => es.close();
  }, [token]);

  // 2) 채팅방 생성
  const createRoom = () => {
    if (!partnerId.trim()) return;
    fetch(`${API_BASE}/room`, {
      method: 'POST',
      headers: authHeaders(token),
      body: JSON.stringify({ receiverId: Number(partnerId) }),
    })
      .then(res => res.json())
      .then(room => {
        setSelectedRoomId(room.chatRoomId);
      })
      .catch(console.error);
  };

  // 3) 선택된 방의 메시지 이력 로드 & WebSocket 구독
  useEffect(() => {
    if (!selectedRoomId || !token) return;

    // 이력 호출
    fetch(
      `${API_BASE}/${selectedRoomId}/messages?page=0&size=50`,
      { headers: authHeaders(token) }
    )
      .then(res => res.json())
      .then(page => setMessages(Array.isArray(page) ? page : page.content))
      .catch(console.error);

    // STOMP 연결
    const socket = new SockJS('http://localhost:8080/ws/chat');
    const client = Stomp.over(socket);
    client.connect(
      { Authorization: `Bearer ${token}` },
      () => {
        client.subscribe(
          `/topic/chatroom.${selectedRoomId}`,
          ({ body }) => {
            const msg = JSON.parse(body);
            setMessages(prev => [...prev, msg]);
            // 자동 스크롤
            setTimeout(() => {
              scrollRef.current?.scrollTo(0, scrollRef.current.scrollHeight);
            }, 50);
          }
        );
      },
      err => console.error('WebSocket 오류:', err)
    );
    stompClient.current = client;

    return () => {
      client.disconnect();
    };
  }, [selectedRoomId, token]);

  // 4) 메시지 전송
  const sendMessage = () => {
    if (!draft.trim() || !stompClient.current) return;
    stompClient.current.send(
      '/app/chat.sendMessage',
      { Authorization: `Bearer ${token}` },
      JSON.stringify({
        chatRoomId: selectedRoomId,
        content: draft.trim(),
      })
    );
    setDraft('');
  };

  return (
    <div style={{ display: 'flex', height: '100vh' }}>
      {/* 방 목록 & 생성 */}
      <div style={{ width: 250, borderRight: '1px solid #ccc', padding: 10 }}>
        <h4>채팅방 목록</h4>
        <div>
          <input
            type="number"
            placeholder="상대방 ID"
            value={partnerId}
            onChange={e => setPartnerId(e.target.value)}
            style={{ width: '70%', marginRight: 5 }}
          />
          <button onClick={createRoom}>생성</button>
        </div>
        <div style={{ marginTop: 15, maxHeight: '80vh', overflowY: 'auto' }}>
          {rooms.map(r => (
            <div
              key={r.chatRoomId}
              onClick={() => setSelectedRoomId(r.chatRoomId)}
              style={{
                padding: 8,
                cursor: 'pointer',
                background:
                  r.chatRoomId === selectedRoomId ? '#eef' : 'transparent',
              }}
            >
              <strong>{r.otherUserName || r.otherNickName}</strong>
              <div
                style={{
                  whiteSpace: 'nowrap',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  fontSize: 12,
                  color: '#666',
                }}
              >
                {r.lastMessage}
              </div>
              {r.unreadCount > 0 && (
                <span style={{ color: 'red', fontSize: 12 }}>
                  {r.unreadCount}
                </span>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* 채팅창 */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        {selectedRoomId ? (
          <>
            <div
              ref={scrollRef}
              style={{
                flex: 1,
                padding: 10,
                overflowY: 'auto',
                background: '#fafafa',
              }}
            >
              {messages.map((m, idx) => (
                <div key={idx} style={{ margin: '5px 0' }}>
                  <strong>{m.senderNickName || m.senderId}:</strong>{' '}
                  {m.content}
                </div>
              ))}
            </div>
            <div
              style={{
                display: 'flex',
                padding: 10,
                borderTop: '1px solid #ddd',
              }}
            >
              <input
                style={{ flex: 1, marginRight: 10 }}
                placeholder="메시지를 입력하세요"
                value={draft}
                onChange={e => setDraft(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && sendMessage()}
              />
              <button onClick={sendMessage}>전송</button>
            </div>
          </>
        ) : (
          <div
            style={{
              flex: 1,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#999',
            }}
          >
            채팅방을 선택하거나 생성하세요.
          </div>
        )}
      </div>
    </div>
  );
};

export default ChatTest;
