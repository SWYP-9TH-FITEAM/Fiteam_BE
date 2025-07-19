// src/ChatApi/ChatApp.jsx
import React, { useState, useEffect } from 'react';
import { fetchChatRooms, createChatRoom } from './api.js';
import ChatRoomList from './ChatRoomList.jsx';
import ChatRoom     from './ChatRoom.jsx';

const SSE_URL = 'http://localhost:8080/v1/user-chat/rooms/subscribe';

export default function ChatApp({ token, userId, onLogout }) {
  const [rooms, setRooms]             = useState([]);
  const [selectedRoomId, setSelected] = useState(null);
  const [newUserId, setNewUserId]     = useState('');

  // 1) 초기 목록 로드 + SSE 구독
  useEffect(() => {
    if (!token) return;

    // HTTP로 초기 채팅방 목록 불러오기
    fetchChatRooms(token)
      .then(data => setRooms(data))
      .catch(console.error);

    // SSE 구독
    const es = new EventSource(`${SSE_URL}?token=${token}`);
    // INIT 이벤트: 전체 목록 세팅
    es.addEventListener('INIT', e => {
      setRooms(JSON.parse(e.data));
    });
    // 업데이트 이벤트: delta를 반영하고, 최신 방을 맨 위로
    es.addEventListener('chat-room-updated', e => {
      const delta = JSON.parse(e.data);
      setRooms(prev => {
        // 이전 목록에서 이 방을 제외
        const others = prev.filter(r => r.chatRoomId !== delta.chatRoomId);
        // 기존 방 정보가 있으면 그걸 병합, 없으면 delta 그대로
        const existing = prev.find(r => r.chatRoomId === delta.chatRoomId) || {};
        const updated  = { ...existing, ...delta };
        // 최신 방을 맨 앞에 추가
        return [updated, ...others];
      });
    });

    return () => es.close();
  }, [token]);

  // 2) 새 채팅방 생성
  const handleCreate = async () => {
    if (!newUserId.trim()) return;
    try {
      const room = await createChatRoom(token, Number(newUserId));
      setSelected(room.chatRoomId);
      setNewUserId('');
      // 목록에 없으면 맨 앞에 추가
      setRooms(prev =>
        prev.some(r => r.chatRoomId === room.chatRoomId)
          ? prev
          : [room, ...prev]
      );
    } catch (err) {
      console.error('채팅방 생성 실패:', err);
    }
  };

  return (
    <div style={{ display: 'flex', height: '100vh' }}>
      {/* 사이드바 */}
      <aside style={{ width: 280, borderRight: '1px solid #ddd', padding: 16 }}>
        <button onClick={onLogout}>로그아웃</button>
        <div style={{ margin: '16px 0', display: 'flex' }}>
          <input
            type="number"
            placeholder="상대 User ID"
            value={newUserId}
            onChange={e => setNewUserId(e.target.value)}
            style={{ flex: 1, marginRight: 8 }}
          />
          <button onClick={handleCreate}>생성</button>
        </div>
        <ChatRoomList
          rooms={rooms}
          selected={selectedRoomId}
          onSelect={setSelected}
        />
      </aside>

      {/* 메인 */}
      <main style={{ flex: 1 }}>
        {selectedRoomId
          ? <ChatRoom
              token={token}
              userId={userId}
              roomId={selectedRoomId}
            />
          : <p style={{ padding: 20, color: '#666' }}>
              채팅방을 선택하세요.
            </p>
        }
      </main>
    </div>
  );
}
