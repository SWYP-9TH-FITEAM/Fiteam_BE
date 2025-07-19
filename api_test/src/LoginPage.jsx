// src/LoginPage.jsx
import React, { useState } from 'react';
import { login } from './ChatApi/api.js';

export default function LoginPage({ onLogin }) {
  const [email,    setEmail]    = useState('');
  const [password, setPassword] = useState('');
  const [error,    setError]    = useState('');

  const handleSubmit = async e => {
    e.preventDefault();
    setError('');

    console.log('→ 로그인 시도:', { email, password });
    try {
      const { token, type } = await login(email, password);
      console.log('← 로그인 성공 응답:', { token, type });
      onLogin(token);
    } catch (err) {
      console.error('← 로그인 에러:', err);
      setError(err.message || '로그인 실패');
    }
  };

  return (
    <form
      onSubmit={handleSubmit}
      style={{
        maxWidth: 360,
        margin: '100px auto',
        padding: 20,
        border: '1px solid #ccc',
        borderRadius: 6,
      }}
    >
      <h2 style={{ textAlign: 'center' }}>로그인</h2>

      <input
        type="email"
        placeholder="Email"
        value={email}
        onChange={e => setEmail(e.target.value)}
        required
        style={{ width:'100%', padding:8, margin:'8px 0' }}
      />

      <input
        type="password"
        placeholder="Password"
        value={password}
        onChange={e => setPassword(e.target.value)}
        required
        style={{ width:'100%', padding:8, margin:'8px 0' }}
      />

      <button
        type="submit"
        style={{
          width:'100%',
          padding:10,
          background:'#007bff',
          color:'#fff',
          border:'none',
          borderRadius:4,
          cursor:'pointer'
        }}
      >
        로그인
      </button>

      {error && (
        <p style={{ color:'red', marginTop:10, textAlign:'center' }}>
          {error}
        </p>
      )}
    </form>
  );
}
