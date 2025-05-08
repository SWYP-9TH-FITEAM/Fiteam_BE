package com.backend.Fiteam.Domain.User.Service;

import com.backend.Fiteam.Domain.Character.Entity.CharacterCard;
import com.backend.Fiteam.Domain.Character.Repository.CharacterCardRepository;
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
import com.backend.Fiteam.Domain.User.Dto.UserCardResponseDto;
import com.backend.Fiteam.Domain.User.Dto.UserGroupStatusDto;
import com.backend.Fiteam.Domain.User.Dto.UserProfileDto;
import com.backend.Fiteam.Domain.User.Dto.UserSettingsRequestDto;
import com.backend.Fiteam.Domain.User.Dto.UserSettingsResponseDto;
import com.backend.Fiteam.Domain.User.Entity.User;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public void saveCharacterTestResult(Integer userId, List<Map<String, Integer>> answers) {
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

        // 기존 cardId1 → cardId2로 백업
        user.setCardId2(user.getCardId1());

        // 새로운 cardId1 저장
        user.setCardId1(characterCard.getId());

        // 성향점수와 AI 분석결과 저장
        user.setNumEI(numEI);
        user.setNumPD(numPD);
        user.setNumVA(numVA);
        user.setNumCL(numCL);
        user.setDetails("hyper clova description");

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public TestResultResponseDto getTestResult(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        if (user.getCardId1() == null) {
            throw new IllegalArgumentException("테스트 결과가 존재하지 않습니다.");
        }

        CharacterCard characterCard = characterCardRepository.findById(user.getCardId1())
                .orElseThrow(() -> new NoSuchElementException("CharacterCard not found with id: " + user.getCardId1()));

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

        return new UserProfileDto(user.getUserName(), user.getProfileImgUrl(), user.getJob());
    }

    @Transactional(readOnly = true)
    public UserCardResponseDto getUserProfileCard(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 유저를 찾을 수 없습니다. id: " + userId));

        if (user.getCardId1() == null) {
            throw new NoSuchElementException("해당 유저는 테스트 결과 가 없습니다.");
        }

        CharacterCard card = characterCardRepository.findById(user.getCardId1())
                .orElseThrow(() -> new NoSuchElementException("해당 카드 정보를 찾을 수 없습니다. id: " + user.getCardId1()));

        return UserCardResponseDto.builder()
                .code(card.getCode())
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

    @Transactional
    public void acceptGroupInvitation(Integer groupId, Integer userId) {
        GroupMember groupMember = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
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
                .teamId(groupMember.getId()) // groupMember id → teamId로 사용
                .masterUserId(userId)
                .name("temp_" + user.getUserName())  // 예: temp_김철수
                .maxMembers(teamType.getMaxMembers())
                .description(null)
                .status("대기중")
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();

        teamRepository.save(newTeam);

        // GroupMember 테이블에도 팀 정보 반영
        groupMember.setTeamId(newTeam.getTeamId());
        groupMember.setTeamStatus("대기중");
    }


    public List<UserGroupStatusDto> getUserGroupsByStatus(Integer userId, boolean isAccepted) {
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

    @Transactional
    public void updateUserSettings(Integer userId, UserSettingsRequestDto dto) {
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
                user.getPhoneNumber(),
                user.getKakaoId()
        );
    }
}
