// src/ChatApi/ChatRoomList.jsx
import React from 'react';

export default function ChatRoomList({ rooms, onSelect, selected }) {
  return (
    <ul style={{ listStyle:'none', padding:0 }}>
      {rooms.map(r => (
        <li key={r.chatRoomId}
            onClick={()=>onSelect(r.chatRoomId)}
            style={{
              padding:12,
              background: r.chatRoomId===selected ? '#eef' : '#fff',
              cursor:'pointer', borderBottom:'1px solid #ddd'
            }}
        >
          <strong>{r.otherUserName}</strong>
          <p style={{ margin:4, fontSize:12, color:'#555' }}>
            {r.lastMessageContent || '—'}
          </p>
          {r.unreadMessageCount>0 && (
            <span style={{ fontSize:10, color:'red' }}>
              {r.unreadMessageCount}
            </span>
          )}
        </li>
      ))}
    </ul>
  );
}
