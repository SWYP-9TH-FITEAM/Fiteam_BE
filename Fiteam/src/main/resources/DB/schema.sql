CREATE DATABASE IF NOT EXISTS Fiteam DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE Fiteam;

-- ========== 사용자 테이블 ==========
CREATE TABLE User (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(50),
    user_name VARCHAR(30),
    profile_img_url TEXT,
    phone_number VARCHAR(20),
    kakao_id VARCHAR(30),
    job VARCHAR(50),
    major VARCHAR(50),
    introduction TEXT,
    url VARCHAR(200),
    card_id1 INT,
    card_id2 INT,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE Admin (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(50),
    admin_name VARCHAR(30),
    position VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Manager (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(50),
    manager_name VARCHAR(30),
    organization VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========== 성향검사 & 캐릭터 카드 ==========
CREATE TABLE CharacterCard (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(4) NOT NULL UNIQUE,
    name VARCHAR(30),
    summary VARCHAR(200),
    team_strength TEXT,
    caution TEXT,
    best_match_code VARCHAR(4),
    best_match_reason VARCHAR(300),
    worst_match_code VARCHAR(4),
    worst_match_reason VARCHAR(300)
);

CREATE TABLE CharacterQuestion (
    id INT PRIMARY KEY,
    dimension VARCHAR(3),
    question VARCHAR(300),
    type_a CHAR(1),
    type_b CHAR(1)
);
-- ========== 그룹 및 팀 빌딩 ==========
CREATE TABLE ProjectGroup (
    id INT AUTO_INCREMENT PRIMARY KEY,
    manager_id INT,
    name VARCHAR(50),
    description VARCHAR(200),
    max_user_count INT,
    team_make_type INT,
    contact_policy VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE GroupMember (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT,
    user_id INT,
    is_accepted BOOLEAN DEFAULT FALSE,
    invited_at TIMESTAMP,
    team_status VARCHAR(20),
    ban BOOLEAN DEFAULT FALSE,
    position VARCHAR(30),
    work_history INT,
    project_goal VARCHAR(200),
    url VARCHAR(200),
    introduction TEXT
);

CREATE TABLE TeamType (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),
    description VARCHAR(100),
    start_datetime DATETIME,
    end_datetime DATETIME,
    min_members INT,
    max_members INT,
    position_based BOOLEAN DEFAULT FALSE,
    config_json TEXT
);

CREATE TABLE Team (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT,
    team_id INT,
    master_user_id INT,
    name VARCHAR(50),
    max_members INT,
    description TEXT,
    status VARCHAR(20),
    team_type_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE TeamMember (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT,
    team_id INT,
    user_id INT,
    position VARCHAR(30),
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE TeamRequest (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT,
    team_id INT,
    sender_id INT,
    receiver_id INT,
    status VARCHAR(30) DEFAULT '대기중',
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE UserLike (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT,
    receiver_id INT,
    group_id INT,
    like_num INT,
    number INT,
    memo VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========== 채팅 ==========
CREATE TABLE ChatRoom (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user1_id INT,
    user2_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ChatMessage (
    id INT AUTO_INCREMENT PRIMARY KEY,
    chat_room_id INT,
    sender_id INT,
    content VARCHAR(300),
    is_read BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========== 알림 ==========
CREATE TABLE Notification (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT,
    sender_type VARCHAR(50),
    user_id INT,
    content VARCHAR(300),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
