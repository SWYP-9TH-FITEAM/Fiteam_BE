USE fiteam;

INSERT INTO CharacterCard (
    img_url, code, name, keyword, summary, team_strength, caution,
    best_match_code1, best_match_reason1,
    best_match_code2, best_match_reason2,
    worst_match_code1, worst_match_reason1,
    worst_match_code2, worst_match_reason2) VALUES
    ('https://kr.object.ncloudstorage.com/fiteam-character/1_1.png', 'EPVC', '팀 분위기가 반짝이는 \n아이디어 조율 요정', '아이디어, 조율력, 팀 분위기 업', '외향적이고 창의적이며 계획을 중시하고 분위기를 잘 맞춤', '아이디어 공유, 팀원 간 중재, 분위기메이커', '우유부단, 결정 장애, 방향 설정 고민', 'IDAL', '실행력이 뛰어난 유형과 조합 시 조율 중심 기획력이 빛남', 'IPVL', '조용한 추진자와 기획 리더와의 조화', 'EDVL', '강한 리더십 성향과 충돌 가능', 'EDAL', '결과 중심 실행형과 협업 시 주도권 다툼 우려'),
    ('https://kr.object.ncloudstorage.com/fiteam-character/2_1.png', 'EPVL', '아이디어로 팀을 이끄는 \n크리에이티브 리더', '기획 리더, 창의성, 결단력', '외향적이고 계획적이며 아이디어로 리드함', '아이디어 주도, 기획 리딩, 결단력 있음', '타인 배려 부족, 독주 경향, 경청 부족', 'IPVC', '조용하지만 창의적인 유형과 시너지', 'IDVC', '실용적 실행자와 기획 균형', 'EDAL', '실행 중심 리더와 주도권 충돌', 'IDAL', '보수적인 유형과 아이디어 갈등'),
    ('https://kr.object.ncloudstorage.com/fiteam-character/3_1.png', 'EPAC', '전략적 사고로 중심을 잡는 \n조율 마스터', '데이터 감각, 전략 사고, 조율 능력', '외향적이고 분석적이며 계획적, 조율형', '데이터 기반 협업, 갈등 관리', '융통성 부족, 유연성 아쉬움, 새로움 부족', 'IDVC', '분석형 실무자와 안정된 기획 가능', 'IPAC', '논리적 계획과 감정 중재가 어우러짐', 'EDVL', '추진 위주 스타일과 방식 충돌', 'IDAL', '실행 중심의 직진형과 협업 시 마찰'),
    ('https://kr.object.ncloudstorage.com/fiteam-character/4_1.png', 'EPAL', '계획력 만렙! 추진력 있는 \n기획 리더', '기획력, 추진력, 조직력', '분석적 사고로 외향적 리딩 + 계획형 추진력', '전략 기획, 리더십 발휘', '결과 중심, 소통 부족, 과정 생략', 'IPAC', '논리적 기획자들과 전략적 호흡', 'IPAL', '차분한 실행자와 균형 있는 협업', 'EDVL', '추진 중심 리더와 주도권 충돌', 'IDVC', '조율형과의 소통 단절 가능'),
    ('https://kr.object.ncloudstorage.com/fiteam-character/5_1.png', 'EDVC', '아이디어가 떠오르면 바로 실천하는 \n실행력 천재', '실행력, 창의 아이디어, 행동 중심', '외향적이면서 아이디어 풍부, 바로 행동', '추진력, 분위기 메이커', '계획 미흡, 충동성, 지속력 부족', 'IPAL', '신중한 계획형과 조합 시 실행력 보완', 'IPVL', '차분한 리더와 안정적 협업 가능', 'EPAL', '기획형 리더와 추진 방식 충돌', 'IDAL', '내향적 실행형과 커뮤니케이션 충돌 가능'),
    ('https://kr.object.ncloudstorage.com/fiteam-character/6_1.png', 'EDVL', '속도감 있게 밀어붙이는 \n추진 리더', '속도 갑, 리더형, 실전 감각', '외향적이고 실행 중심 + 창의적 리더 기질', '빠른 결정과 팀 리딩', '장기 전략 부족, 속도 과함, 전략 부재', 'IPVL', '조용한 전략가와 안정적 조율', 'IPAL', '계획 중심 실행과 상호 보완', 'EPVC', '외향적 조율자와 리더십 충돌', 'IPVC', '내향적 아이디어형과 속도 차이'),
    ('https://kr.object.ncloudstorage.com/fiteam-character/7_1.png', 'EDAC', '정확하고 꼼꼼한 \n실무형 멀티플레이어', '실행력, 분석형, 협업 감각', '외향적, 실행형, 분석적이며 조율력도 있음', '실무 강자, 다툼 조율 잘함', '창의성 부족, 감정 무딤, 즉흥성 약함', 'IDAC', '정확하고 체계적인 실행 파트너', 'IPAL', '기획형과 실무 분담 가능', 'EPVL', '기획형 리더와 주도권 경쟁', 'EPVC', '아이디어 조율자와 창의성 방식 차이'),
    ('https://kr.object.ncloudstorage.com/fiteam-character/8_1.png', 'EDAL', '빠르게 판단하고 이끄는 \n실전형 해결사', '문제 해결, 신속한 판단, 목표 주도', '데이터 기반 빠른 실행, 리더십 있음', '문제 해결력, 목표 주도', '소통 부족, 무뚝뚝, 협업 어려움', 'IPAC', '논리적인 팀원과 궁합 좋음', 'IDAC', '차분하고 꼼꼼한 팀원과 상호 보완', 'EPVC', '조율형과 팀워크 방식 다름', 'IPVC', '창의적 유형과 접근 방식 충돌'),
    ('https://kr.object.ncloudstorage.com/fiteam-character/9_1.png', 'IPVC', '조용히 반짝이는 \n아이디어 수집가', '섬세한 아이디어, 조용한 사고, 조율력', '내향적이고 창의적이며 계획을 선호하고 조율 감각이 뛰어남', '디테일한 아이디어 제안', '표현 부족, 존재감 낮음, 설득력 약함', 'EPVL', '외향적 창의 리더와 함께 창의적 흐름을 구체화할 수 있음', 'EDVC', '즉흥 실행자와의 균형으로 실행력을 높일 수 있음', 'EDAL', '강한 주도성과 리더십이 충돌할 수 있음', 'EPAL', '실행 중심 기획형과 속도 및 방식 차이로 갈등 우려'),
    ('https://kr.object.ncloudstorage.com/fiteam-character/10_1.png','IPVL', '묵묵히 역할을 수행하는 \n믿음직한 플래너', '계획적, 책임감, 조용한 리더', '조용히 이끄는 전략가 스타일로 체계적이고 책임감이 강함', '전체 조율, 장기적 플랜 수립 능력', '거리감, 과묵함, 직접 피드백 부족', 'EDVL', '추진형과 조합 시 실행력과 전략의 균형을 맞출 수 있음', 'IPAL', '논리적 전략가와 장기 프로젝트에서 궁합이 좋음', 'EPVC', '외향적이고 즉흥적인 팀원과 리듬이 맞지 않을 수 있음', 'EDVC', '의사결정 속도 차이로 갈등 가능'),
    ('https://kr.object.ncloudstorage.com/fiteam-character/11_1.png','IPAC', '숫자와 사람 사이를 잇는 \n전략형 조율러', '분석적, 조율력, 해결력', '분석적이고 계획적이며 조율 능력이 뛰어난 전략형 중재자', '문제 해결, 갈등 완화 능력 탁월', '실행력 부족, 과분석, 속도 느림', 'EPAC', '논리적 계획 수립자와 함께 신중한 결정을 내릴 수 있음', 'IPAL', '분석적 리더와 꼼꼼한 프로젝트 관리에 적합', 'EDVL', '즉흥 실행형과 의사결정 속도 차이로 마찰 가능', 'EDAL', '직진형 스타일과 갈등 시 융통성 부족'),
    ('https://kr.object.ncloudstorage.com/fiteam-character/12_1.png','IPAL', '전략에 진심인 조용한 \n컨트롤 타워', '전략적, 판단력, 실행력', '조용하지만 전략적 사고와 정확한 판단력이 돋보이는 기획 실행자', '계획적이고 책임감 있는 실행력', '융통성 부족, 반응 느림, 유연하지 않음', 'EPAL', '기획형 리더와 전략 수립에서의 호흡이 좋음', 'EDVC', '즉흥 실행자와 협업 시 실행력을 보완받을 수 있음', 'IDVC', '창의형과의 감각적 접근에서 마찰 가능', 'EDVL', '빠른 결단이 필요한 상황에서 리더십 충돌 우려'),
    ('https://kr.object.ncloudstorage.com/fiteam-character/13_1.png','IDVC', '내공 깊은 실용주의 \n아이디어 메이커', '실용성, 독립성, 창의력', '내향적이지만 창의성과 실행력을 동시에 갖춘 실용형 인물', '자기주도 실행력, 독립적 문제 해결 능력', '소통 부족, 협업 소극적, 고립 경향', 'EPAC', '논리적 기획자와 협업 시 실행에 힘을 실어줄 수 있음', 'IPAL', '전략적 유형과 함께 결과 중심 작업에 강함', 'EPVL', '외향적이고 주도적인 리더와의 의견 대립 가능성', 'IPAC', '분석 중심의 속도 느린 팀과 비효율 초래 가능'),
    ('https://kr.object.ncloudstorage.com/fiteam-character/14_1.png','IDVL', '차분하게 팀을 이끄는 조용한 \n추진 엔진', '몰입도, 조용한 추진력, 성과 지향', '실행에 강하고 조용한 리더 기질로 몰입도가 높음', '조용한 추진력, 계획된 성과 실현 능력', '피드백 부족, 혼자 일함, 소통 아쉬움', 'EPVC', '아이디어형과 조합 시 실행으로 연결되는 시너지 가능', 'IPVL', '전략가와 함께 플랜 실행에서 균형 유지', 'EDVL', '외향적 리더와의 강한 방향성 충돌 가능', 'IPAL', '분석 중시형과 협업 시 속도 차이 발생'),
    ('https://kr.object.ncloudstorage.com/fiteam-character/15_1.png','IDAC', '데이터로 업무를 해결하는 \n꼼꼼한 실행러', '데이터 분석, 디테일, 실행력', '실행력과 분석력을 겸비한 내향적 실무 전문가', '꼼꼼한 진행과 철저한 업무 완성도', '표현력 부족, 창의력 낮음, 감정 표현 어려움', 'EDAC', '실행 관리자와 협업 시 업무 효율 극대화', 'IPAC', '분석 조율자와 논리적 호흡이 뛰어남', 'EPVL', '외향적 리더와 감정 커뮤니케이션에서 갈등 가능', 'EPVC', '아이디어 중심 성향과 협업 시 답답함 초래'),
    ('https://kr.object.ncloudstorage.com/fiteam-character/16_1.png','IDAL', '데이터로 설득하고 문제를 해결하는 \n신뢰형 리더', '책임감, 신뢰감, 문제 해결력', '실행력과 리더십, 분석력을 고루 갖춘 신뢰감 있는 내향형 리더', '책임감 있고 계획적인 업무 처리', '고립, 침묵형, 감정 공유 어려움', 'EPVC', '조율형 기획자와 함께하면 팀 안정감 상승', 'EPAC', '논리적 팀원과 문제 해결력 극대화 가능', 'EDVL', '즉흥적 추진형과 의사결정 충돌 가능', 'IDVC', '감성적·창의형과의 정서적 거리감 발생');



INSERT INTO CharacterQuestion (id, dimension, question, type_a, type_b) VALUES
    (1,  'E-I', '함께 일하는 사람들과 자주 소통하고 협업하는 과정에서 더 활력이 생긴다.',                                                                 'E', 'I'),
    (2,  'P-D', '어떤 일이든 시작하기 전에 전체적인 계획을 세우고, 그 계획에 맞춰 움직일 때 더 편안하고 효율적이라고 느낀다.',                         'P', 'D'),
    (3,  'V-A', '새로운 프로젝트를 시작할 때, 기존 데이터를 분석하기보다 아이디어를 먼저 떠올리는 편이다.',                                     'V', 'A'),
    (4,  'C-L', '팀의 의견을 조율하고 모두가 동의할 수 있는 결정을 내리는 것이 중요하다고 생각한다.',                                         'C', 'L'),
    (5,  'E-I', '혼자 조용한 환경이 아닌, 다른 사람들과 이야기하며 생각을 나눌 때 아이디어가 더 많이 떠올린다.',                              'E', 'I'),
    (6,  'P-D', '장기적인 목표를 설정하고, 이를 달성하기 위한 중간 계획과 단계별 실행을 짜는 것을 즐기는 편이다.',                         'P', 'D'),
    (7,  'V-A', '창의적이고 실험적인 방식으로 문제를 해결해보는 것을 즐긴다.',                                                             'V', 'A'),
    (8,  'C-L', '다양한 사람의 의견을 듣고 균형 잡힌 방향을 찾는 데 익숙하다.',                                                           'C', 'L'),
    (9,  'E-I', '회의나 모임이 자주 있는 날이 오히려 없는 날보다 더 에너지가 넘치고 활기차다.',                                            'E', 'I'),
    (10, 'P-D', '예기치 못한 변화나 변수보다, 미리 준비하고 예측할 수 있는 상황에서 더 안정감과 통제력을 느낀다.',                          'P', 'D'),
    (11, 'V-A', '직관적으로 방향을 잡고, 아이디어를 기반으로 판단하는 편이다.',                                                          'V', 'A'),
    (12, 'C-L', '상황이 모호할수록 타협과 조정을 통해 해답을 찾으려 한다.',                                                              'C', 'L'),
    (13, 'E-I', '새로운 사람들과 빠르게 친해지고 대화를 자연스럽게 시작할 수 있다.',                                                       'E', 'I'),
    (14, 'P-D', '여행을 떠날 때는 주요 일정과 시간표를 사전에 계획하고, 어떤 장소를 언제 갈지 미리 정리해두는 것을 선호한다.',                'P', 'D'),
    (15, 'V-A', '색다른 시도나 방식으로 일하는 걸 선호하며, 반복적인 방식엔 쉽게 지루함을 느낀다.',                                        'V', 'A'),
    (16, 'C-L', '회의에서는 내 의견보다 팀원들의 의견을 먼저 듣고 흐름을 정리하려고 한다.',                                                'C', 'L'),
    (17, 'E-I', '발표나 회의에서 자주 발언하며 내 의견을 적극적으로 전달하는 편이다.',                                                  'E', 'I'),
    (18, 'P-D', '주말이나 휴일에도 해야 할 일이나 일정을 미리 정리하고 계획을 세워두는 편이 훨씬 편하다.',                                   'P', 'D'),
    (19, 'V-A', '생각이 자유롭게 흐를 수 있는 환경에서 더 집중이 잘 된다.',                                                            'V', 'A'),
    (20, 'C-L', '의사결정 시 다수의 공감대를 만드는 과정이 결과보다 더 중요하다고 느낀다.',                                            'C', 'L'),
    (21, 'E-I', '낯선 사람들과 네트워킹하는 자리를 즐기고 그 안에서 자신을 잘 표현할 수 있다.',                                            'E', 'I'),
    (22, 'P-D', '일정을 미리 정리해두면 마음이 더 놓이고, 오히려 스케줄이 타이트할수록 안심이 된다.',                                       'P', 'D'),
    (23, 'V-A', '신기하거나 낯선 것을 보면 직접 시도해보고 싶은 호기심이 생긴다.',                                                       'V', 'A'),
    (24, 'C-L', '팀 내 감정이나 분위기를 살피며 대화를 이끄는 것이 자연스럽다.',                                                        'C', 'L'),
    (25, 'E-I', '사람들과 잡담하거나 소소한 대화를 나누는 시간이 오히려 에너지를 충전시킨다.',                                            'E', 'I'),
    (26, 'P-D', '다른 사람과의 약속이나 일정은 가능한 한 일찍 조율해서 사전에 정해두는 것을 선호한다.',                                    'P', 'D'),
    (27, 'V-A', '미래를 상상하며 어떤 일을 기획하는 과정이 재미있고 자연스럽다.',                                                       'V', 'A'),
    (28, 'C-L', '의견 충돌이 있을 때 정면 돌파보다는 조용히 유연하게 해결하려 한다.',                                                    'C', 'L'),
    (29, 'E-I', '팀원들과 아이디어를 자주 교환하면서 함께 일하는 방식이 더 효과적이라고 느낀다.',                                        'E', 'I'),
    (30, 'P-D', '쇼핑을 할 때 필요한 물건들을 미리 리스트로 정리해두고, 계획한 항목만 구매하는 편이다.',                                    'P', 'D'),
    (31, 'V-A', '실험적이고 독창적인 아이디어를 공유할 때 더 자신감이 생긴다.',                                                      'V', 'A'),
    (32, 'C-L', '회의 중에는 중립적 입장을 유지하며 흐름을 부드럽게 연결하려 한다.',                                                    'C', 'L'),
    (33, 'E-I', '회의나 브레인스토밍에서 말하면서 내 생각이 더 명확해지는 경우가 많다.',                                                  'E', 'I'),
    (34, 'P-D', '업무를 시작하기 전에 필요한 자료나 준비물을 꼼꼼히 챙기고, 사전 점검을 통해 불확실성을 줄이려 한다.',                          'P', 'D'),
    (35, 'V-A', '다양한 가능성을 탐색하며 자유롭게 사고를 확장하는 과정에서 아이디어가 잘 떠오른다.',                                        'V', 'A'),
    (36, 'C-L', '리더 역할보다는 사람들 사이의 간극을 메우는 역할이 더 잘 맞는다.',                                                      'C', 'L'),
    (37, 'E-I', '복잡한 문제를 해결할 때 다른 사람들의 피드백을 자주 받아 방향을 정한다.',                                                 'E', 'I'),
    (38, 'P-D', '문서를 만들거나 계획서를 작성하면서 일의 흐름을 정리하고, 그 계획을 기준으로 업무를 진행하는 편이다.',                   'P', 'D'),
    (39, 'V-A', '분석적 해석보다는 무에서 유를 만들어내는 것에 흥미를 느끼며, 새로운 것에 의미를 부여하는 편이다.',                       'V', 'A'),
    (40, 'C-L', '팀의 분위기를 유지하면서 갈등을 최소화하는 것이 내 역할이라고 생각한다.',                                              'C', 'L'),
    (41, 'E-I', '여러 사람이 함께 있는 공간에서도 쉽게 집중하고 자연스럽게 일할 수 있다.',                                               'E', 'I'),
    (42, 'P-D', '프로젝트를 진행할 때는 중간 점검이나 진척 상황을 확인하는 과정을 중요하게 생각한다.',                                     'P', 'D'),
    (43, 'V-A', '아이디어가 떠오르면 메모를 하거나 끄적이는 습관이 있다.',                                                              'V', 'A'),
    (44, 'C-L', '피드백을 줄 때는 상대의 기분을 고려하며 말투와 표현을 조절하려 한다.',                                                  'C', 'L'),
    (45, 'E-I', '공유 오피스나 북적이는 카페 같은 열린 환경에서 일할 때도 즐거움을 느낀다.',                                                'E', 'I'),
    (46, 'P-D', '중요한 업무나 과제는 마감일보다 미리 끝내는 것이 더 마음이 편하고 만족스럽다.',                                        'P', 'D'),
    (47, 'V-A', '비유나 상상력을 자주 활용해서 개념이나 아이디어를 설명하는 편이다.',                                                  'V', 'A'),
    (48, 'C-L', '협업에서는 주도하기보다는 상대와 보조를 맞추며 진행하는 것이 편하다.',                                                 'C', 'L'),
    (49, 'E-I', 'SNS나 커뮤니티 활동을 통해 다른 사람들과 자주 소통하며 생각을 나눈다.',                                                 'E', 'I'),
    (50, 'P-D', '예상 가능한 리스크나 돌발 상황을 미리 고려해 여러 가지 대응 방안을 세워두는 편이다.',                                   'P', 'D'),
    (51, 'V-A', '데이터 시각화보다는 감각적이고 시각적인 방식으로 표현하는 것을 좋아한다.',                                              'V', 'A'),
    (52, 'C-L', '중요한 결정에서도 모두의 의견을 듣고 최대한 조율한 후 방향을 잡고 싶다.',                                                 'C', 'L'),
    (53, 'E-I', '하루에 여러 모임이나 일정이 있어도 지치기보다는 오히려 활력이 생긴다.',                                                  'E', 'I'),
    (54, 'P-D', '내가 세운 계획이 틀어지거나 일정이 변경될 경우 스트레스를 받는 일이 종종 있다.',                                        'P', 'D'),
    (55, 'V-A', '정확성보다는 독창성이 중요한 가치라고 생각하며, 업무나 작업에서도 이를 중시한다.',                                      'V', 'A'),
    (56, 'C-L', '회의나 프로젝트 중 리드보다는 주변의 흐름을 살피며 균형을 잡는 역할에 집중한다.',                                        'C', 'L'),
    (57, 'E-I', '여러 사람이 있는 자리에서도 내 생각을 거리낌 없이 표현할 수 있다.',                                                    'E', 'I'),
    (58, 'P-D', '나는 변동보다는 구조화된 규칙과 일정, 체계에 따라 움직일 때 더 안정감을 느낀다.',                                      'P', 'D'),
    (59, 'V-A', '질서 있는 분석보다는 창조적인 혼돈 속에서 더 큰 가능성을 발견할 수 있다고 생각한다.',                                  'V', 'A'),
    (60, 'C-L', '대화나 회의 중엔 나서기보다는 상황을 파악하고 적절히 연결하는 것을 중요하게 여긴다.',                                'C', 'L');


INSERT INTO Admin (email, password, admin_name, position)
VALUES
    ('eunjishin@admin.com',  '$2a$10$VuPfJJPhJdkwMYV.aZIKjeFKxrYHBnqmfldMVDLyD9Y9yI/l4foGG', '신은지', 'PM'),
    ('kimdohyeong@admin.com','$2a$10$VuPfJJPhJdkwMYV.aZIKjeFKxrYHBnqmfldMVDLyD9Y9yI/l4foGG', '김도형', 'FE'),
    ('yejinlee@admin.com',    '$2a$10$VuPfJJPhJdkwMYV.aZIKjeFKxrYHBnqmfldMVDLyD9Y9yI/l4foGG', '이예진', 'FE'),
    ('kimdoeun@admin.com',    '$2a$10$VuPfJJPhJdkwMYV.aZIKjeFKxrYHBnqmfldMVDLyD9Y9yI/l4foGG', '김도은', 'DS'),
    ('sungmoonhong@admin.com','$2a$10$VuPfJJPhJdkwMYV.aZIKjeFKxrYHBnqmfldMVDLyD9Y9yI/l4foGG', '홍성문', 'BE');


INSERT INTO Manager (email, password, manager_name, organization)
VALUES
    ('manager1@test.com', '$2a$10$VuPfJJPhJdkwMYV.aZIKjeFKxrYHBnqmfldMVDLyD9Y9yI/l4foGG', '김도형매니저', '스위프9-1기'),
    ('manager2@test.com', '$2a$10$VuPfJJPhJdkwMYV.aZIKjeFKxrYHBnqmfldMVDLyD9Y9yI/l4foGG', '이예진매니저', '스위프9-2기');


-- 4. User (10명: aa ~ jj)
INSERT INTO User (
    email, password, user_name, profile_img_url, phone_number, kakao_id,job, major, introduction, url, card_id1, card_id2,details, num_EI, num_PD, num_VA, num_CL, created_at)
VALUES
    ('aa@test.com', '$2a$10$VuPfJJPhJdkwMYV.aZIKjeFKxrYHBnqmfldMVDLyD9Y9yI/l4foGG', 'aa', 'https://kr.object.ncloudstorage.com/fiteam-character/7_1.png', '010-0000-0000', 'kakao_aa',
     'Developer', 'CS', '안녕하세요, 저는 aa입니다.', 'https://github.com/aa', 7, 2,
     'ISTP 유형의 적극적인 사람입니다.', 7, 3, 5, 8, NOW()),
    ('bb@test.com', '$2a$10$VuPfJJPhJdkwMYV.aZIKjeFKxrYHBnqmfldMVDLyD9Y9yI/l4foGG', 'bb', 'https://kr.object.ncloudstorage.com/fiteam-character/12_1.png', '010-0000-0001', 'kakao_bb',
     'Developer', 'CS', '안녕하세요, 저는 bb입니다.', 'https://github.com/bb', 12, 2,
     'ESTJ 유형의 분석적인 사람입니다.', 6, 4, 4, 7, NOW()),
    ('cc@test.com', '$2a$10$VuPfJJPhJdkwMYV.aZIKjeFKxrYHBnqmfldMVDLyD9Y9yI/l4foGG', 'cc', 'https://example.com/cc.jpg', '010-0000-0002', 'kakao_cc',
     'Developer', 'CS', '안녕하세요, 저는 cc입니다.', 'https://github.com/cc', 1, 2,
     'INFP 유형의 창의적인 사람입니다.', 5, 5, 6, 6, NOW()),
    ('dd@test.com', '$2a$10$VuPfJJPhJdkwMYV.aZIKjeFKxrYHBnqmfldMVDLyD9Y9yI/l4foGG', 'dd', 'https://kr.object.ncloudstorage.com/fiteam-character/7_1.png', '010-0000-0003', 'kakao_dd',
     'Developer', 'CS', '안녕하세요, 저는 dd입니다.', 'https://github.com/dd', 7, 2,
     'ENTP 유형의 호기심 많은 사람입니다.', 8, 2, 7, 5, NOW()),
    ('ee@test.com', '$2a$10$VuPfJJPhJdkwMYV.aZIKjeFKxrYHBnqmfldMVDLyD9Y9yI/l4foGG', 'ee', 'https://example.com/ee.jpg', '010-0000-0004', 'kakao_ee',
     'Developer', 'CS', '안녕하세요, 저는 ee입니다.', 'https://github.com/ee', 1, 2,
     'ISFJ 유형의 섬세한 사람입니다.', 4, 6, 5, 7, NOW()),
    ('ff@test.com', '$2a$10$VuPfJJPhJdkwMYV.aZIKjeFKxrYHBnqmfldMVDLyD9Y9yI/l4foGG', 'ff', 'https://example.com/ff.jpg', '010-0000-0005', 'kakao_ff',
     'Developer', 'CS', '안녕하세요, 저는 ff입니다.', 'https://github.com/ff', 1, 2,
     'ENFJ 유형의 사회적인 사람입니다.', 7, 4, 6, 6, NOW()),
    ('gg@test.com', '$2a$10$VuPfJJPhJdkwMYV.aZIKjeFKxrYHBnqmfldMVDLyD9Y9yI/l4foGG', 'gg', 'https://kr.object.ncloudstorage.com/fiteam-character/12_1.png', '010-0000-0006', 'kakao_gg',
     'Developer', 'CS', '안녕하세요, 저는 gg입니다.', 'https://github.com/gg', 1, 2,
     'ISTJ 유형의 논리적인 사람입니다.', 6, 5, 4, 8, NOW()),
    ('hh@test.com', '$2a$10$VuPfJJPhJdkwMYV.aZIKjeFKxrYHBnqmfldMVDLyD9Y9yI/l4foGG', 'hh', 'https://example.com/hh.jpg', '010-0000-0007', 'kakao_hh',
     'Developer', 'CS', '안녕하세요, 저는 hh입니다.', 'https://github.com/hh', 1, 2,
     'ESFP 유형의 활발한 사람입니다.', 8, 3, 6, 5, NOW()),
    ('ii@test.com', '$2a$10$VuPfJJPhJdkwMYV.aZIKjeFKxrYHBnqmfldMVDLyD9Y9yI/l4foGG', 'ii', 'https://kr.object.ncloudstorage.com/fiteam-character/7_1.png', '010-0000-0008', 'kakao_ii',
     'Developer', 'CS', '안녕하세요, 저는 ii입니다.', 'https://github.com/ii', 1, 2,
     'INFJ 유형의 통찰력 있는 사람입니다.', 5, 7, 5, 6, NOW()),
    ('jj@test.com', '$2a$10$VuPfJJPhJdkwMYV.aZIKjeFKxrYHBnqmfldMVDLyD9Y9yI/l4foGG', 'jj', 'https://example.com/jj.jpg', '010-0000-0009', 'kakao_jj',
     'Developer', 'CS', '안녕하세요, 저는 jj입니다.', 'https://github.com/jj', 1, 2,
     'ENTJ 유형의 리더십 있는 사람입니다.', 7, 5, 6, 7, NOW());


-- 2) 팀 타입 (직군별 구성) 하나
INSERT INTO TeamType (id, name, description,start_datetime, end_datetime,min_members, max_members,position_based, is_building_done, config_json)
VALUES
(1,'랜덤팀 빌딩 테스트','랜덤으로 팀을 구성하는 방식','2025-05-28 20:30:00','2025-06-01 00:00:00',4, 6,FALSE, FALSE, NULL);

-- 3) 프로젝트 그룹 하나 (매니저1이 만든 샘플 그룹)
INSERT INTO ProjectGroup (id, manager_id, name, description,max_user_count, team_make_type, contact_policy)
VALUES
(1,1,'샘플 그룹','초기 샘플 그룹입니다.',20,1,'EMAIL');


-- 5) 각 유저별로 1인 1팀씩 생성 (팀 ID 1~10)
INSERT INTO Team (id, group_id, team_id, master_user_id, name,max_members, description, team_status)
VALUES
    (1, 1,  1,  1, 'Team 1',  6, '초기 1인 팀', 0),
    (2, 1,  2,  2, 'Team 2',  6, '초기 1인 팀', 0),
    (3, 1,  3,  3, 'Team 3',  6, '초기 1인 팀', 0),
    (4, 1,  4,  4, 'Team 4',  6, '초기 1인 팀', 0),
    (5, 1,  5,  5, 'Team 5',  6, '초기 1인 팀', 0),
    (6, 1,  6,  6, 'Team 6',  6, '초기 1인 팀', 0),
    (7, 1,  7,  7, 'Team 7',  6, '초기 1인 팀', 0),
    (8, 1,  8,  8, 'Team 8',  6, '초기 1인 팀', 0),
    (9, 1,  9,  9, 'Team 9',  6, '초기 1인 팀', 0),
    (10,1, 10, 10, 'Team 10', 6, '초기 1인 팀', 0);
-- 6) 모든 유저 초대 → 수락하여 GroupMember, 각각 1인 팀에 배정
INSERT INTO GroupMember (id, group_id, user_id, is_accepted, invited_at,ban, team_id, team_status)
VALUES
    (1,  1,  1, TRUE, '2025-05-10 00:00:00', FALSE, 1, 0),
    (2,  1,  2, TRUE, '2025-05-10 00:00:00', FALSE, 2, 0),
    (3,  1,  3, TRUE, '2025-05-10 00:00:00', FALSE, 3, 0),
    (4,  1,  4, TRUE, '2025-05-10 00:00:00', FALSE, 4, 0),
    (5,  1,  5, TRUE, '2025-05-10 00:00:00', FALSE, 5, 0),
    (6,  1,  6, TRUE, '2025-05-10 00:00:00', FALSE, 6, 0),
    (7,  1,  7, TRUE, '2025-05-10 00:00:00', FALSE, 7, 0),
    (8,  1,  8, TRUE, '2025-05-10 00:00:00', FALSE, 8, 0),
    (9,  1,  9, TRUE, '2025-05-10 00:00:00', FALSE, 9, 0),
    (10, 1, 10, TRUE, '2025-05-10 00:00:00', FALSE,10, 0);
