package com.backend.Fiteam.Domain.Character.Service;

import com.backend.Fiteam.Domain.Character.Dto.CharacterCardDto;
import com.backend.Fiteam.Domain.Character.Dto.CharacterMatchDto;
import com.backend.Fiteam.Domain.Character.Dto.CompatibilityUserInfoDto;
import com.backend.Fiteam.Domain.Character.Entity.CharacterCard;
import com.backend.Fiteam.Domain.Character.Repository.CharacterCardRepository;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CharacterCardService {
    private final CharacterCardRepository characterCardRepository;

    public Optional<CharacterCardDto> getCharacterCardById(int id) {
        Optional<CharacterCard> cardOpt = characterCardRepository.findById(id);
        if (cardOpt.isEmpty()) return Optional.empty();

        CharacterCard card = cardOpt.get();

        CharacterMatchDto best1 = getMatchDtoByCode(card.getBestMatchCode1(), card.getBestMatchReason1());
        CharacterMatchDto best2 = getMatchDtoByCode(card.getBestMatchCode2(), card.getBestMatchReason2());
        CharacterMatchDto worst1 = getMatchDtoByCode(card.getWorstMatchCode1(), card.getWorstMatchReason1());
        CharacterMatchDto worst2 = getMatchDtoByCode(card.getWorstMatchCode2(), card.getWorstMatchReason2());

        CharacterCardDto dto = CharacterCardDto.builder()
                .id(card.getId())
                .name(card.getName())
                .imgUrl(card.getImgUrl())
                .code(card.getCode())
                .summary(card.getSummary())
                .teamStrength(card.getTeamStrength())
                .caution(card.getCaution())
                .bestMatch1(best1)
                .bestMatch2(best2)
                .worstMatch1(worst1)
                .worstMatch2(worst2)
                .build();

        return Optional.of(dto);
    }

    private CharacterMatchDto getMatchDtoByCode(String code, String reason) {
        return characterCardRepository.findByCode(code)
                .map(m -> CharacterMatchDto.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .imgUrl(m.getImgUrl())
                        .code(m.getCode())
                        .reason(reason)
                        .build())
                .orElse(null);
    }


    public List<CharacterCard> getAllCharacterCards() {
        return characterCardRepository.findAll();
    }


    public CompatibilityResult calculateCompatibilityScore(CompatibilityUserInfoDto userA, CompatibilityUserInfoDto userB) {
        if (userA.getCardId() == null || userB.getCardId() == null) {
            throw new IllegalArgumentException("성향 카드 정보가 누락되었습니다.");
        }

        // 1. 카드 정보 조회
        CharacterCard myCard = characterCardRepository.findById(userA.getCardId())
                .orElseThrow(() -> new NoSuchElementException("나의 캐릭터 카드를 찾을 수 없습니다."));
        CharacterCard otherCard = characterCardRepository.findById(userB.getCardId())
                .orElseThrow(() -> new NoSuchElementException("상대방 캐릭터 카드를 찾을 수 없습니다."));

        String myCode = myCard.getCode();
        String otherCode = otherCard.getCode();

        // 2. 카드 코드 기반 매칭 점수
        int baseScore = 60;
        String baseDescription = "보통의 궁합입니다. 서로의 차이를 이해하며 협업해 보세요.";

        if (otherCode.equals(myCard.getBestMatchCode1()) || otherCode.equals(myCard.getBestMatchCode2())) {
            baseScore = 90;
            baseDescription = "아주 잘 맞는 최고의 궁합입니다!";
        } else if (otherCode.equals(myCard.getWorstMatchCode1()) || otherCode.equals(myCard.getWorstMatchCode2())) {
            baseScore = 30;
            baseDescription = "충돌 가능성이 높은 조합입니다. 서로의 차이를 존중하세요.";
        }

        // 3. 성향 점수 거리 기반 유사도 점수
        double distance = Math.sqrt(
                Math.pow(userA.getNumEI() - userB.getNumEI(), 2) +
                        Math.pow(userA.getNumPD() - userB.getNumPD(), 2) +
                        Math.pow(userA.getNumVA() - userB.getNumVA(), 2) +
                        Math.pow(userA.getNumCL() - userB.getNumCL(), 2)
        );
        int similarityScore = (int)(100 - Math.min(distance * 10, 100));  // 거리 0 → 100점, 거리 10 이상 → 최소 0점

        // 4. 종합 점수
        int finalScore = (baseScore + similarityScore) / 2;

        // 5. 설명 조합
        String finalDescription = baseDescription;
        if (finalScore >= 80) {
            finalDescription += " 성향까지 매우 유사한 최고의 파트너입니다.";
        } else if (finalScore >= 60) {
            finalDescription += " 성향 면에서도 어느 정도 조화를 이룰 수 있습니다.";
        } else {
            finalDescription += " 성향 차이가 크므로 협업 시 더 많은 이해가 필요합니다.";
        }

        return new CompatibilityResult(finalScore, finalDescription);
    }

    @Getter
    @AllArgsConstructor
    public static class CompatibilityResult {
        private final int score;
        private final String description;
    }

}