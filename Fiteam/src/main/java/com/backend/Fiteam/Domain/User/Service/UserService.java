package com.backend.Fiteam.Domain.User.Service;

import com.backend.Fiteam.Domain.Character.Entity.CharacterCard;
import com.backend.Fiteam.Domain.Character.Repository.CharacterCardRepository;
import com.backend.Fiteam.Domain.Group.Entity.GroupMember;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.User.Dto.SaveTestAnswerRequestDto;
import com.backend.Fiteam.Domain.User.Dto.TestResultResponseDto;
import com.backend.Fiteam.Domain.User.Dto.UserCardResponseDto;
import com.backend.Fiteam.Domain.User.Dto.UserGroupProfileDto;
import com.backend.Fiteam.Domain.User.Dto.UserLikeCancelRequestDto;
import com.backend.Fiteam.Domain.User.Dto.UserLikeRequestDto;
import com.backend.Fiteam.Domain.User.Dto.UserProfileDto;
import com.backend.Fiteam.Domain.User.Entity.User;
import com.backend.Fiteam.Domain.User.Entity.UserLike;
import com.backend.Fiteam.Domain.User.Repository.UserLikeRepository;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
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
    private final UserLikeRepository userLikeRepository;


    @Transactional
    public void saveCharacterTestResult(Integer userId, SaveTestAnswerRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        int E = 0, I = 0, P = 0, D = 0, V = 0, A = 0, C = 0, L = 0;

        for (Map<String, Integer> answer : requestDto.getAnswers()) {
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

        user.setNumEI(numEI);
        user.setNumPD(numPD);
        user.setNumVA(numVA);
        user.setNumCL(numCL);
        user.setDetails("description");

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



    public UserProfileDto getUserProfile(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        return new UserProfileDto(user.getUserName(), user.getProfileImgUrl());
    }

    public UserCardResponseDto getUserProfileCard(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 유저를 찾을 수 없습니다. id: " + userId));

        if (user.getCardId1() == null) {
            throw new NoSuchElementException("해당 유저는 CharactorCard 가 없습니다.");
        }

        CharacterCard card = characterCardRepository.findById(user.getCardId1())
                .orElseThrow(() -> new NoSuchElementException("해당 카드 정보를 찾을 수 없습니다. id: " + user.getCardId1()));

        return UserCardResponseDto.builder()
                .code(card.getCode())
                .name(card.getName())
                .summary(card.getSummary())
                .teamStrength(card.getTeamStrength())
                .caution(card.getCaution())
                .bestMatchCode(card.getBestMatchCode())
                .bestMatchReason(card.getBestMatchReason())
                .worstMatchCode(card.getWorstMatchCode())
                .worstMatchReason(card.getWorstMatchReason())
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

        groupMember.setIsAccepted(true);
    }


    public void sendLike(Integer senderId, UserLikeRequestDto dto) {
        // 1. 그룹 멤버 여부 확인
        boolean isValidGroup = groupMemberRepository.existsByGroupIdAndUserIdAndIsAcceptedTrue(dto.getGroupId(), senderId) &&
                groupMemberRepository.existsByGroupIdAndUserIdAndIsAcceptedTrue(dto.getGroupId(), dto.getReceiverId());

        if (!isValidGroup) {
            throw new IllegalArgumentException("같은 그룹에 속해있는 사용자에게만 좋아요를 남길 수 있습니다.");
        }

        // 2. 중복 체크
        boolean alreadyLiked = userLikeRepository.existsBySenderIdAndReceiverId(
                senderId, dto.getReceiverId());

        if (alreadyLiked) {
            throw new IllegalArgumentException("이미 해당 항목에 대해 좋아요를 남겼습니다.");
        }

        // 3. 저장
        UserLike like = UserLike.builder()
                .senderId(senderId)
                .receiverId(dto.getReceiverId())
                .groupId(dto.getGroupId())
                .memo(dto.getMemo())
                .number(dto.getNumber())
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();

        userLikeRepository.save(like);
    }


    public void cancelAllLikesToUser(Integer senderId, Integer receiverId) {
        UserLike like = userLikeRepository.findBySenderIdAndReceiverId(senderId, receiverId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저에게 남긴 좋아요가 없습니다."));

        userLikeRepository.delete(like);
    }



}
