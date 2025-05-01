// src/App.js
import React, { useState } from "react";
import LoginPage from "./LoginPage";
import ChatTest from "./ChatApi/ChatTest";

function App() {
    const [authToken, setAuthToken] = useState(localStorage.getItem("authToken"));
    const [view, setView] = useState("login");

    const handleLoginSuccess = (token) => {
        setAuthToken(token);
        localStorage.setItem("authToken", token);
    };

    const handleLogout = () => {
        setAuthToken(null);
        localStorage.removeItem("authToken");
    };

    return (
        <div>
            <h1>채팅 기능 테스트</h1>
            <div style={{ marginBottom: "20px" }}>
                <button onClick={() => setView("login")}>로그인</button>
                {authToken && <button onClick={() => setView("chatTest")}>채팅 테스트</button>}
                {authToken && <button onClick={handleLogout}>로그아웃</button>}
            </div>

            <div>
                {view === "login" && <LoginPage setAuthToken={handleLoginSuccess} />}
                {view === "chatTest" && <ChatTest authToken={authToken} />}
            </div>
        </div>
    );
}

export default App;