// src/App.js
import React, { useState, useEffect } from 'react';
import LoginPage from './LoginPage.jsx';
import ChatApp   from './ChatApi/ChatApp.jsx';

export default function App() {
  const [token, setToken]   = useState(localStorage.getItem('token') || '');
  const [userId, setUserId] = useState(null);

  // 토큰이 바뀔 때마다 localStorage에 저장 & userId 디코딩
  useEffect(() => {
    if (token) {
      localStorage.setItem('token', token);
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        setUserId(parseInt(payload.sub, 10)); // 백엔드 createToken(admin.getId(), ...)에서 sub에 id 저장
      } catch {
        setUserId(null);
      }
    } else {
      localStorage.removeItem('token');
      setUserId(null);
    }
  }, [token]);

  return token && userId ? (
    <ChatApp token={token} userId={userId} onLogout={() => setToken('')} />
  ) : (
    <LoginPage onLogin={setToken} />
  );
}
