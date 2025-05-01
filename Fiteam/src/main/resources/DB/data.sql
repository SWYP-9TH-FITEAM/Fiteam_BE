-- CharacterCard 샘플
INSERT INTO CharacterCard (code, name, summary, team_strength, caution, best_match_code, best_match_reason, worst_match_code, worst_match_reason) VALUES
                                                                                                                                                      ('ENFP', '열정가', '열정적이고 창의적인 성향', '긍정 에너지 제공', '계획에 약함', 'ISTJ', '실행력 있는 파트너', 'INFP', '소극적 충돌 가능'),
                                                                                                                                                      ('ISTJ', '현실주의자', '논리적이고 체계적', '실행력 강함', '융통성 부족', 'ENFP', '창의적 아이디어 보완', 'ESFP', '즉흥성 충돌');

-- CharacterQuestion 예시 2개
INSERT INTO CharacterQuestion (id, dimension, question, type_a, type_b) VALUES
                                                                            (1, 'EI', '팀 활동을 할 때 나는 다른 사람과 많이 이야기한다.', 'E', 'I'),
                                                                            (2, 'PD', '나는 계획을 세워 움직이는 편이다.', 'P', 'D');

-- TeamType
INSERT INTO TeamType (name, description, start_datetime, end_datetime, min_members, max_members, position_based, config_json) VALUES
    ('기본 팀 매칭', '4~6명 자유매칭', '2025-05-01 09:00:00', '2025-05-03 18:00:00', 4, 6, false, '{"rule":"자유매칭"}');

-- Manager
INSERT INTO Manager (email, password, manager_name, organization) VALUES
    ('manager1@email.com', 'managerpw', '홍매니저', '스위프9기');

-- ProjectGroup
INSERT INTO ProjectGroup (manager_id, name, description, max_user_count, team_make_type, contact_policy) VALUES
    (1, 'SWYP 9기', '협업 중심 팀빌딩', 50, 1, '카카오톡 오픈채팅');

-- User
INSERT INTO User (email, password, user_name, profile_img_url, phone_number, kakao_id, job, major, introduction, url,
                  card_id1, card_id2, details, num_EI, num_PD, num_VA, num_CL) VALUES
    ('testuser@email.com', '$2a$10$ouWlHsUdmT.Q/MC4ADICRe7k49PZINyA4Zf9WlRZWBAsy19/Rf89y', '테스트유저', 'https://example.com/image.jpg', '01012345678', 'kakaoid1',
     '백엔드', '컴퓨터공학과', '안녕하세요 저는 백엔드 개발자입니다.', 'https://github.com/testuser', 1, 2,
     '이 사용자는 열정적이고 창의적인 성향입니다.', 7, 6, 5, 8),
    ('testuser2@email.com', '$2a$10$ouWlHsUdmT.Q/MC4ADICRe7k49PZINyA4Zf9WlRZWBAsy19/Rf89y', '테스트유저', 'https://example.com/image.jpg', '01012345678', 'kakaoid1',
    '백엔드', '컴퓨터공학과', '안녕하세요 저는 백엔드 개발자입니다.', 'https://github.com/testuser', 1, 2,
    '이 사용자는 열정적이고 창의적인 성향입니다.', 7, 6, 5, 8);

-- GroupMember
INSERT INTO GroupMember (group_id, user_id, is_accepted, invited_at, team_status, ban, position, work_history, project_goal, url, introduction)
VALUES (1, 1, true, '2025-04-30 14:31:15', '미참가', false, '백엔드', 2, '팀으로 협업하는 경험을 쌓고 싶어요.', 'https://portfolio.com/user1', '꼼꼼한 백엔드');

-- Team
INSERT INTO Team (group_id, team_id, master_user_id, name, max_members, description, status, team_type_id) VALUES
    (1, 1, 1, '테스트 팀', 6, '기획자와 개발자가 모인 팀', '모집중', 1);

-- TeamMember
INSERT INTO TeamMember (group_id, team_id, user_id, position) VALUES
    (1, 1, 1, '백엔드');

-- Notification
INSERT INTO Notification (sender_id, sender_type, user_id, type, table_id, content) VALUES
    (1, 'user', 1, '팀초대', 1, 'OO님이 팀에 초대하셨습니다.');
