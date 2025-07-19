// src/ChatApi/api.js
const API_BASE = 'http://localhost:8080/v1';

export async function login(email, password) {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `Login failed: ${res.status}`);
  }
  // { token: "...", type: "user|manager|admin" } 반환
  return res.json();
}

export async function fetchChatRooms(token) {
  const res = await fetch(`${API_BASE}/user-chat/list`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok) throw new Error(`Fetch rooms failed: ${res.status}`);
  return res.json();
}

export async function fetchMessages(token, roomId) {
  const res = await fetch(
    `${API_BASE}/user-chat/${roomId}/messages?page=0&size=50`,
    { headers: { Authorization: `Bearer ${token}` } }
  );
  if (!res.ok) throw new Error(`Fetch messages failed: ${res.status}`);
  return res.json();
}

export async function createChatRoom(token, receiverId) {
  const res = await fetch(`${API_BASE}/user-chat/room`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ receiverId }),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `Create room failed: ${res.status}`);
  }
  return res.json();
}
