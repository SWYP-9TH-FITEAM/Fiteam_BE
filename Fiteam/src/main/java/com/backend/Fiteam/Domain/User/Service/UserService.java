package com.backend.Fiteam.Domain.User.Service;

import com.backend.Fiteam.AppCache.CharacterCardCache;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.TeamStatus;
import com.backend.Fiteam.Domain.Character.Entity.CharacterCard;
import com.backend.Fiteam.Domain.Character.Repository.CharacterCardRepository;
import com.backend.Fiteam.Domain.Character.Service.CharacterCardService;
import com.backend.Fiteam.Domain.Group.Entity.GroupMember;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Team.Dto.TeamContactResponseDto;
import com.backend.Fiteam.Domain.Team.Entity.Team;
import com.backend.Fiteam.Domain.Group.Entity.TeamType;
import com.backend.Fiteam.Domain.Team.Repository.TeamRepository;
import com.backend.Fiteam.Domain.Team.Repository.TeamTypeRepository;
import com.backend.Fiteam.Domain.User.Dto.TestResultResponseDto;
import com.backend.Fiteam.Domain.User.Dto.UserCardHistoryDto;
import com.backend.Fiteam.Domain.User.Dto.UserCardResponseDto;
import com.backend.Fiteam.Domain.User.Dto.UserGroupStatusDto;
import com.backend.Fiteam.Domain.User.Dto.UserProfileDto;
import com.backend.Fiteam.Domain.User.Dto.UserSettingsRequestDto;
import com.backend.Fiteam.Domain.User.Dto.UserSettingsResponseDto;
import com.backend.Fiteam.Domain.User.Entity.User;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@RestControllerAdvice
public class UserService {

    private final UserRepository userRepository;
    private final CharacterCardRepository characterCardRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ProjectGroupRepository projectGroupRepository;
    private final TeamRepository teamRepository;
    private final TeamTypeRepository teamTypeRepository;
    private final CharacterCardService characterCardService;

    private final RedisTemplate<String, String> redisTemplate;
    // 캐릭터 카드 캐시 사용
    private final CharacterCardCache characterCardCache;

    // 캐싱해둔 캐릭터 카드 get 함수


    /*
    saveCharacterTestResult 함수
    1. 시간복잡도 : O(n) - 작은 상수레벨 반복 수준
    2. Read 1 + Write 1 = 총 2번 DB 접속
    3. 병렬처리 여부 : ❌
    4. 개선점
        -1) 중복 제출 방지 -> Redis 캐시로 해결
    */
    @Transactional
    public void saveCharacterTestResult(Integer userId, List<Map<String, Integer>> answers) {
        // 1. 중복 제출 방지 (Redis 확인)
        String redisKey = "submitted:" + userId;
        if (redisTemplate.hasKey(redisKey)) {
            throw new IllegalStateException("이미 테스트를 제출한 사용자입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        int E = 0, I = 0, P = 0, D = 0, V = 0, A = 0, C = 0, L = 0;

        for (Map<String, Integer> answer : answers) {
            for (String key : answer.keySet()) {
                int value = answer.get(key);
                switch (key) {
                    case "E" -> E += value;
                    case "I" -> I += value;
                    case "P" -> P += value;
                    case "D" -> D += value;
                    case "V" -> V += value;
                    case "A" -> A += value;
                    case "C" -> C += value;
                    case "L" -> L += value;
                }
            }
        }

        int numEI = E;
        int numPD = P;
        int numVA = V;
        int numCL = C;

        // 높은 점수 쪽 선택해서 Code 만들기
        StringBuilder codeBuilder = new StringBuilder();
        codeBuilder.append(E >= I ? "E" : "I");
        codeBuilder.append(P >= D ? "P" : "D");
        codeBuilder.append(V >= A ? "V" : "A");
        codeBuilder.append(C >= L ? "C" : "L");
        String code = codeBuilder.toString();

        // CharacterCard 테이블에서 code 찾기
        CharacterCard characterCard = characterCardRepository.findByCode(code)
                .orElseThrow(() -> new NoSuchElementException("CharacterCard not found with code: " + code));

        // HyperCLOVA로 분석 결과 생성
        //String description = hyperCLOVAService.generateDescription(numEI, numPD, numVA, numCL);
        String description = characterCardService.buildCharacterDescription(E, P, V, C);

        // 기존 cardId1 → cardId2로 백업
        user.setCardId2(user.getCardId1());

        // 새로운 cardId1 저장
        user.setCardId1(characterCard.getId());

        // 성향점수와 AI 분석결과 저장
        user.setProfileImgUrl(characterCard.getImgUrl());
        user.setNumEI(numEI);
        user.setNumPD(numPD);
        user.setNumVA(numVA);
        user.setNumCL(numCL);
        user.setDetails(description);

        userRepository.save(user);

        redisTemplate.opsForValue().set(redisKey, "1", Duration.ofMinutes(5));
    }

    // 이정도면 정말 빠름
    @Transactional(readOnly = true)
    public TestResultResponseDto getTestResult(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        if (user.getCardId1() == null) {
            throw new IllegalArgumentException("테스트 결과가 존재하지 않습니다.");
        }

        CharacterCard characterCard = characterCardCache.getCard(user.getCardId1());

        return TestResultResponseDto.builder()
                .code(characterCard.getCode())
                .name(characterCard.getName())
                .numEI(user.getNumEI())
                .numPD(user.getNumPD())
                .numVA(user.getNumVA())
                .numCL(user.getNumCL())
                .build();
    }

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        return UserProfileDto.builder()
                .userName(user.getUserName())
                .profileImgUrl(user.getProfileImgUrl())
                .job(user.getJob())
                .build();

    }

    @Transactional(readOnly = true)
    public List<UserCardHistoryDto> getUserCardHistory(Integer userId) {
        // 1) 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 유저를 찾을 수 없습니다. id: " + userId));

        if (user.getCardId1() == null) {
            throw new NoSuchElementException("해당 유저는 테스트 결과가 없습니다.");
        }

        List<UserCardHistoryDto> history = new ArrayList<>();

        // 2) 최신 카드 (cardId1) -> 캐싱으로 대체함
        // CharacterCard card1 = characterCardRepository.findById(user.getCardId1()).orElseThrow(() -> new NoSuchElementException("해당 카드 정보를 찾을 수 없습니다. id: " + user.getCardId1()));
        CharacterCard card1 = characterCardCache.getCard(user.getCardId1());

        history.add(buildDto(card1, user.getDetails()));

        // 3) 이전 카드 (cardId2) — null 체크
        if (user.getCardId2() != null) {
            CharacterCard card2 = characterCardCache.getCard(user.getCardId2());
            history.add(buildDto(card2, user.getDetails()));
        }

        return history;
    }

    private UserCardHistoryDto buildDto(CharacterCard card, String details) {
        return UserCardHistoryDto.builder()
                .code(card.getCode())
                .imgUrl(card.getImgUrl())
                .name(card.getName())
                .keyword(card.getKeyword())
                .summary(card.getSummary())
                .teamStrength(card.getTeamStrength())
                .caution(card.getCaution())
                .bestMatchCode1(card.getBestMatchCode1())
                .bestMatchReason1(card.getBestMatchReason1())
                .bestMatchCode2(card.getBestMatchCode2())
                .bestMatchReason2(card.getBestMatchReason2())
                .worstMatchCode1(card.getWorstMatchCode1())
                .worstMatchReason1(card.getWorstMatchReason1())
                .worstMatchCode2(card.getWorstMatchCode2())
                .worstMatchReason2(card.getWorstMatchReason2())
                .details(details)
                .build();
    }

    @Transactional(readOnly = true)
    public UserCardResponseDto getUserProfileCard(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 유저를 찾을 수 없습니다. id: " + userId));

        if (user.getCardId1() == null) {
            throw new NoSuchElementException("해당 유저는 테스트 결과 가 없습니다.");
        }

        CharacterCard card = characterCardCache.getCard(user.getCardId1());

        return UserCardResponseDto.builder()
                .code(card.getCode())
                .imgUrl(card.getImgUrl())
                .name(card.getName())
                .keyword(card.getKeyword())
                .summary(card.getSummary())
                .teamStrength(card.getTeamStrength())
                .caution(card.getCaution())
                .bestMatchCode1(card.getBestMatchCode1())
                .bestMatchReason1(card.getBestMatchReason1())
                .bestMatchCode2(card.getBestMatchCode2())
                .bestMatchReason2(card.getBestMatchReason2())
                .worstMatchCode1(card.getWorstMatchCode1())
                .worstMatchReason1(card.getWorstMatchReason1())
                .worstMatchCode2(card.getWorstMatchCode2())
                .worstMatchReason2(card.getWorstMatchReason2())
                .details(user.getDetails())
                .ei(user.getNumEI())
                .pd(user.getNumPD())
                .va(user.getNumVA())
                .cl(user.getNumCL())
                .build();
    }

    /*
    acceptGroupInvitation 함수
    1. 시간복잡도 : O(1) - 모든 조회는 PK 또는 Unique 인덱스 기반
    2. DB Read : 총 4회
        - GroupMember (userId + groupId 복합키)
        - ProjectGroup (groupId)
        - TeamType (teamTypeId)
        - User (userId)
    3. DB Write : 총 2회
        - Team 저장
        - GroupMember 업데이트
    4. 병렬처리 여부 : ❌
    5. 개선점
        -1) 중복 수락 방지 확인
        -3) 팀 상태 및 생성일 처리 일관성 확인
    */
    @Transactional
    public void acceptGroupInvitation(Integer groupId, Integer userId) {
        GroupMember groupMember = groupMemberRepository.findByUserIdAndGroupId(userId,groupId)
                .orElseThrow(() -> new IllegalArgumentException("초대 내역이 존재하지 않습니다."));

        if (Boolean.TRUE.equals(groupMember.getIsAccepted())) {
            throw new IllegalArgumentException("이미 수락한 초대입니다.");
        }

        // 초대 수락
        groupMember.setIsAccepted(true);

        // 매니져가 초대를 보내면서 teamtype 설정 해두는게 좋을듯
        // 그룹 정보 조회
        ProjectGroup projectGroup = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹이 존재하지 않습니다."));

        // 팀타입 정보 조회
        Integer teamTypeId = projectGroup.getTeamMakeType();
        TeamType teamType = teamTypeRepository.findById(teamTypeId)
                .orElseThrow(() -> new IllegalArgumentException("팀 구성 방식이 존재하지 않습니다."));

        // 유저 이름 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        // 1인 팀 생성
        Team newTeam = Team.builder()
                .groupId(groupId)
                .masterUserId(userId)
                .name("temp_" + user.getUserName())
                .maxMembers(teamType.getMaxMembers())
                .description(null)
                .teamStatus(TeamStatus.WAITING)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();

        Team savedTeam = teamRepository.save(newTeam);
        savedTeam.setTeamId(savedTeam.getId());

        // GroupMember 테이블에도 팀 정보 반영
        groupMember.setTeamId(savedTeam.getId());
        groupMember.setTeamStatus(TeamStatus.WAITING);
        groupMemberRepository.save(groupMember);
    }

    /*
    getUserGroupsByStatus 함수
    1. 시간복잡도 : O(n) - 사용자의 그룹 멤버십 수에 비례
    2. DB Read : n + 1 회
        - groupMemberRepository.findAllByUserIdAndIsAccepted[True/False] → 1회
        - 각 GroupMember마다 projectGroupRepository.findById() → n회
    3. 병렬처리 여부 : ❌
    4. 개선점
        -1) N+1 쿼리 발생 → ProjectGroup을 미리 JOIN 또는 IN 쿼리로 묶는 방식으로 개선 가능
    */
    @Transactional(readOnly = true)
    public List<UserGroupStatusDto> getUserGroupsByStatus_V1(Integer userId, boolean isAccepted) {
        List<GroupMember> memberships = isAccepted
                ? groupMemberRepository.findAllByUserIdAndIsAcceptedTrue(userId)
                : groupMemberRepository.findAllByUserIdAndIsAcceptedFalse(userId);

        return memberships.stream()
                .map(gm -> {
                    ProjectGroup pg = projectGroupRepository.findById(gm.getGroupId())
                            .orElseThrow(() -> new IllegalArgumentException("그룹 정보를 찾을 수 없습니다."));
                    return UserGroupStatusDto.builder()
                            .groupId(pg.getId())
                            .groupName(pg.getName())
                            .invitedAt(gm.getInvitedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserGroupStatusDto> getUserGroupsByStatus(Integer userId, boolean isAccepted) {
        List<GroupMember> memberships = isAccepted
                ? groupMemberRepository.findAllByUserIdAndIsAcceptedTrue(userId)
                : groupMemberRepository.findAllByUserIdAndIsAcceptedFalse(userId);

        if (memberships.isEmpty()) return List.of(); // 참여중인거 없으면 바로 리턴

        List<Integer> groupIds = memberships.stream().map(GroupMember::getGroupId).distinct().toList();

        // ProjectGroup 일괄 조회 및 Map 구성
        Map<Integer, ProjectGroup> groupMap = projectGroupRepository.findAllById(groupIds).stream()
                .collect(Collectors.toMap(ProjectGroup::getId, Function.identity()));

        // 4. DTO 매핑
        return memberships.stream()
                .map(gm -> { ProjectGroup pg = groupMap.get(gm.getGroupId());
                    if (pg == null) {
                        throw new IllegalStateException("그룹 정보를 찾을 수 없습니다. id: " + gm.getGroupId());
                    }
                    return UserGroupStatusDto.builder()
                            .groupId(pg.getId())
                            .groupName(pg.getName())
                            .invitedAt(gm.getInvitedAt())
                            .build();
                }).toList();
    }

    // 프로필 수정이 너무 과하게 호출되지 않도록 5초Lock 추가
    @Transactional
    public void updateUserSettings(Integer userId, UserSettingsRequestDto dto) {
        String redisKey = "update:user:" + userId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            throw new IllegalStateException("설정은 잠시 후 다시 변경할 수 있습니다.");
        }

        // 1) 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        // 2) null 체크 후 값이 있을 때만 업데이트
        if (dto.getPhoneNumber() != null) {
            user.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getKakaoId() != null) {
            user.setKakaoId(dto.getKakaoId());
        }
        if (dto.getJob() != null) {
            user.setJob(dto.getJob());
        }
        if (dto.getMajor() != null) {
            user.setMajor(dto.getMajor());
        }
        if (dto.getIntroduction() != null) {
            user.setIntroduction(dto.getIntroduction());
        }
        if (dto.getUrl() != null) {
            user.setUrl(dto.getUrl());
        }

        // 3) 업데이트 시간 기록
        user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        // 4) 저장
        userRepository.save(user);
        redisTemplate.opsForValue().set(redisKey, "1", Duration.ofSeconds(5));
    }

    @Transactional(readOnly = true)
    public UserSettingsResponseDto getUserSettings(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        return UserSettingsResponseDto.builder()
                .userName(user.getUserName())
                .profileImgUrl(user.getProfileImgUrl())
                .phoneNumber(user.getPhoneNumber())
                .kakaoId(user.getKakaoId())
                .job(user.getJob())
                .major(user.getMajor())
                .introduction(user.getIntroduction())
                .url(user.getUrl())
                .cardId1(user.getCardId1())
                .cardId2(user.getCardId2())
                .details(user.getDetails())
                .numEI(user.getNumEI())
                .numPD(user.getNumPD())
                .numVA(user.getNumVA())
                .numCL(user.getNumCL())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public TeamContactResponseDto getContactForUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저 정보를 찾을 수 없습니다."));
        return new TeamContactResponseDto(
                user.getId(),
                user.getUserName(),
                user.getPhoneNumber()
        );
    }
}
