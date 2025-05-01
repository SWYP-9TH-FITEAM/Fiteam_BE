import React, { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const ChatTest = ({ authToken }) => {
  const [receiverId, setReceiverId] = useState('');
  const [chatRoomId, setChatRoomId] = useState(null);
  const [message, setMessage] = useState('');
  const [chatList, setChatList] = useState([]);
  const [client, setClient] = useState(null);
  const [userId, setUserId] = useState(null);

  // 로그인한 사용자 ID 가져오기
  useEffect(() => {
    if (authToken) {
      fetch("http://localhost:8080/v1/user/mypagedata", {
        headers: { Authorization: `Bearer ${authToken}` }
      })
        .then(res => res.json())
        .then(data => {
          setUserId(data.userId || data.id);
        });
    }
  }, [authToken]);

  // 채팅방 입장 시 기존 메시지 불러오기 + WebSocket 연결
  useEffect(() => {
    if (!chatRoomId) return;

    // 1. 기존 메시지 로드
    fetch(`http://localhost:8080/v1/chat/room/${chatRoomId}/messages`, {
      headers: {
        Authorization: `Bearer ${authToken}`
      }
    })
      .then(res => res.json())
      .then(data => {
        const history = data.map(msg => `${msg.senderId}: ${msg.content}`);
        setChatList(history);
      });

    // 2. WebSocket 연결 및 구독
    const socket = new SockJS('http://localhost:8080/ws/chat');
    const stompClient = Stomp.over(socket);
    stompClient.connect({}, () => {
      stompClient.subscribe(`/topic/chatroom.${chatRoomId}`, (msg) => {
        const body = JSON.parse(msg.body);
        setChatList(prev => [...prev, `${body.senderId}: ${body.content}`]);
      });
    });
    setClient(stompClient);

    return () => {
      if (stompClient && stompClient.connected) stompClient.disconnect();
    };
  }, [chatRoomId]);

  // 채팅방 생성
  const createRoom = async () => {
    const res = await fetch('http://localhost:8080/v1/chat/room', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${authToken}`
      },
      body: JSON.stringify({ receiverId: parseInt(receiverId) })
    });
    const data = await res.json();
    setChatRoomId(data.chatRoomId);
  };

  // 메시지 전송
  const sendMessage = () => {
    if (!client || !client.connected) return;
    client.send('/app/chat.sendMessage', {}, JSON.stringify({
      chatRoomId,
      senderId: userId,
      content: message
    }));
    setMessage('');
  };

  return (
    <div>
      <h3>채팅 테스트 (userId: {userId})</h3>

      {!chatRoomId && (
        <>
          <input
            placeholder="상대방 userId 입력"
            value={receiverId}
            onChange={(e) => setReceiverId(e.target.value)}
          />
          <button onClick={createRoom}>채팅방 생성</button>
        </>
      )}

      {chatRoomId && (
        <>
          <div style={{ height: '200px', overflowY: 'scroll', border: '1px solid gray', marginTop: '1rem' }}>
            {chatList.map((m, i) => <div key={i}>{m}</div>)}
          </div>
          <input
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            placeholder="메시지를 입력하세요"
          />
          <button onClick={sendMessage}>보내기</button>
        </>
      )}
    </div>
  );
};

export default ChatTest;
