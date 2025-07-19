CREATE DATABASE IF NOT EXISTS fiteam
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE fiteam;

-- ========== 성향검사 & 캐릭터 카드 ==========
CREATE TABLE CharacterCard (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               img_url VARCHAR(100) NOT NULL,
                               code VARCHAR(4) NOT NULL UNIQUE,
                               name VARCHAR(30),
                               keyword VARCHAR(50),
                               summary TEXT,
                               team_strength TEXT,
                               caution TEXT,
                               best_match_code1 VARCHAR(4),
                               best_match_reason1 TEXT,
                               best_match_code2 VARCHAR(4),
                               best_match_reason2 TEXT,
                               worst_match_code1 VARCHAR(4),
                               worst_match_reason1 TEXT,
                               worst_match_code2 VARCHAR(4),
                               worst_match_reason2 TEXT
);

CREATE TABLE CharacterQuestion (
                                   id INT PRIMARY KEY,
                                   dimension VARCHAR(4),
                                   question VARCHAR(300),
                                   type_a CHAR(1),
                                   type_b CHAR(1)
);

-- ========== 사용자 테이블 ==========
CREATE TABLE User (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      email VARCHAR(50) NOT NULL UNIQUE,
                      password VARCHAR(100),
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
                      details VARCHAR(500),
                      num_EI INT,
                      num_PD INT,
                      num_VA INT,
                      num_CL INT,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP,
                      FOREIGN KEY (card_id1) REFERENCES CharacterCard(id),
                      FOREIGN KEY (card_id2) REFERENCES CharacterCard(id)
);

CREATE TABLE Admin (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(100),
                       admin_name VARCHAR(30),
                       position VARCHAR(50),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========== 방문 기록 ==========
CREATE TABLE VisitLog (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          user_id BIGINT,
                          visit_date DATE NOT NULL,
                          FOREIGN KEY (user_id) REFERENCES User(id)
);

CREATE TABLE IF NOT EXISTS SystemNotice (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            admin_id INT NOT NULL,
                                            title VARCHAR(100) NOT NULL,
                                            content TEXT NOT NULL,
                                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            FOREIGN KEY (admin_id) REFERENCES Admin(id)
);

CREATE TABLE Manager (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         email VARCHAR(50) NOT NULL UNIQUE,
                         password VARCHAR(100),
                         manager_name VARCHAR(30),
                         organization VARCHAR(50),
                         profile_img_url TEXT,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE TeamType (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(50),
                          description VARCHAR(100),
                          start_datetime DATETIME,
                          end_datetime DATETIME,
                          min_members INT,
                          max_members INT,
                          position_based BOOLEAN DEFAULT FALSE,
                          is_building_done BOOLEAN DEFAULT FALSE,
                          config_json TEXT
);

-- ========== 그룹 및 팀 빌딩 ==========
CREATE TABLE ProjectGroup (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              manager_id BIGINT,
                              name VARCHAR(50),
                              description TEXT,
                              max_user_count INT,
                              team_make_type BIGINT,
                              contact_policy VARCHAR(255),
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (manager_id) REFERENCES Manager(id),
                              FOREIGN KEY (team_make_type) REFERENCES TeamType(id)
);

-- ========== 그룹 공지 ==========
CREATE TABLE GroupNotice (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             manager_id BIGINT NOT NULL,
                             group_id BIGINT NOT NULL,
                             title VARCHAR(100) NOT NULL,
                             context TEXT NOT NULL,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (manager_id) REFERENCES User(id),
                             FOREIGN KEY (group_id) REFERENCES ProjectGroup(id)
);

CREATE TABLE Team (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      group_id BIGINT,
                      team_id BIGINT,
                      master_user_id BIGINT,
                      name VARCHAR(50),
                      max_members INT,
                      description TEXT,
                      team_status INT DEFAULT 0,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      FOREIGN KEY (group_id) REFERENCES ProjectGroup(id),
                      FOREIGN KEY (master_user_id) REFERENCES User(id)
);

CREATE TABLE GroupMember (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             group_id BIGINT,
                             user_id BIGINT,
                             is_accepted BOOLEAN DEFAULT FALSE,
                             invited_at TIMESTAMP,
                             ban BOOLEAN DEFAULT FALSE,
                             team_id BIGINT,
                             team_status INT DEFAULT 0,
                             work_history INT,
                             project_goal VARCHAR(200),
                             project_purpose VARCHAR(50),
                             url VARCHAR(200),
                             introduction TEXT,
                             FOREIGN KEY (group_id) REFERENCES ProjectGroup(id),
                             FOREIGN KEY (team_id) REFERENCES Team(id),
                             FOREIGN KEY (user_id) REFERENCES User(id)
);

CREATE TABLE TeamRequest (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             group_id BIGINT,
                             team_id BIGINT,
                             sender_id BIGINT,
                             receiver_id BIGINT,
                             status INT DEFAULT 0,
                             requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (group_id) REFERENCES ProjectGroup(id),
                             FOREIGN KEY (team_id) REFERENCES Team(id),
                             FOREIGN KEY (sender_id) REFERENCES User(id),
                             FOREIGN KEY (receiver_id) REFERENCES User(id)
);

CREATE TABLE UserLike (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          sender_id BIGINT,
                          receiver_id BIGINT,
                          group_id BIGINT,
                          number INT,
                          memo TEXT,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (sender_id) REFERENCES User(id),
                          FOREIGN KEY (receiver_id) REFERENCES User(id),
                          FOREIGN KEY (group_id) REFERENCES ProjectGroup(id)
);

-- ========== 채팅 ==========
CREATE TABLE ChatRoom (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          user1_id BIGINT,
                          user2_id BIGINT,
                          group_id BIGINT,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (user1_id) REFERENCES User(id),
                          FOREIGN KEY (user2_id) REFERENCES User(id),
                          FOREIGN KEY (group_id) REFERENCES ProjectGroup(id),
                          UNIQUE KEY uq_user_user_group (user1_id, user2_id, group_id)
);

CREATE TABLE ManagerChatRoom (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 manager_id BIGINT NOT NULL,
                                 user_id BIGINT NOT NULL,
                                 group_id BIGINT NOT NULL,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (manager_id) REFERENCES Manager(id),
                                 FOREIGN KEY (user_id) REFERENCES User(id),
                                 FOREIGN KEY (group_id) REFERENCES ProjectGroup(id),
                                 UNIQUE KEY uq_mgr_user_group (manager_id, user_id, group_id)
);

CREATE TABLE ChatMessage (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             chat_room_id BIGINT NOT NULL,
                             sender_type ENUM('USER','MANAGER') NOT NULL DEFAULT 'USER',
                             sender_id BIGINT NOT NULL,
                             message_type VARCHAR(20),
                             content TEXT,
                             is_read BOOLEAN DEFAULT FALSE,
                             sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (chat_room_id) REFERENCES ChatRoom(id)
);

-- ========== 알림 ==========
CREATE TABLE Notification (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              sender_id BIGINT NOT NULL,
                              sender_type INT NOT NULL,
                              user_id BIGINT NOT NULL,
                              type INT NOT NULL,
                              content TEXT,
                              is_read BOOLEAN DEFAULT FALSE,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (user_id) REFERENCES User(id)
);

-- 성능 향상을 위한 인덱싱
ALTER TABLE GroupMember
    ADD INDEX idx_gm_user                (user_id),
    ADD INDEX idx_gm_group               (group_id),
    ADD INDEX idx_gm_user_group          (user_id, group_id),
    ADD INDEX idx_gm_group_order         (group_id, id);

ALTER TABLE Notification
    ADD INDEX idx_ntf_user   (user_id);
