import React, { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

function ChatRoom() {
  const [message, setMessage] = useState('');
  const [chatList, setChatList] = useState([]);
  const chatRoomId = 1;
  const senderId = 1; // 테스트용. 실제 로그인 유저 ID 대체 필요

  useEffect(() => {
    const socket = new SockJS('http://localhost:8080/ws/chat');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
      stompClient.subscribe(`/topic/chatroom.${chatRoomId}`, (msg) => {
        const body = JSON.parse(msg.body);
        setChatList(prev => [...prev, body.content]);
      });
    });

    // Clean-up
    return () => {
      if (stompClient.connected) stompClient.disconnect();
    };
  }, []);

  const sendMessage = () => {
    const socket = new SockJS('http://localhost:8080/ws/chat');
    const stompClient = Stomp.over(socket);
    stompClient.connect({}, () => {
      stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
        chatRoomId,
        senderId,
        content: message
      }));
      setMessage('');
    });
  };

  return (
    <div>
      <h2>채팅방 #{chatRoomId}</h2>
      <div style={{ border: '1px solid #ccc', padding: '1rem', height: '300px', overflowY: 'scroll' }}>
        {chatList.map((msg, i) => <div key={i}>{msg}</div>)}
      </div>
      <input value={message} onChange={e => setMessage(e.target.value)} placeholder="메시지 입력" />
      <button onClick={sendMessage}>보내기</button>
    </div>
  );
}

export default ChatRoom;
