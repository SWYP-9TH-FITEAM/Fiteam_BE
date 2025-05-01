import React, { useState } from "react";

const LoginPage = ({ setAuthToken }) => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const handleLogin = async () => {
        try {
            const response = await fetch("http://localhost:8080/v1/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password }),
            });

            if (!response.ok) {
                throw new Error("로그인 실패!");
            }

            const data = await response.json();
            setAuthToken(data.token);
            localStorage.setItem("authToken", data.token);
            alert("로그인 성공");
        } catch (error) {
            alert(error.message);
        }
    };

    return (
        <div>
            <h2>로그인</h2>
            <input type="email" placeholder="이메일 입력" value={email} onChange={e => setEmail(e.target.value)} />
            <input type="password" placeholder="비밀번호 입력" value={password} onChange={e => setPassword(e.target.value)} />
            <button onClick={handleLogin}>로그인</button>
        </div>
    );
};

export default LoginPage;
