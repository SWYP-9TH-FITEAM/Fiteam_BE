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

    // 성향 검사결과 점수에 따른 discription 문구 작성 함수
    private static final int MAX_DIM = 75;
    private static final int THRESHOLD = 38;

    public String buildCharacterDescription(int numEI, int numPD, int numVA, int numCL) {
        return describeEI(numEI) + " "
                + describePD(numPD) + " "
                + describeVA(numVA) + " "
                + describeCL(numCL);
    }

    private String describeEI(int score) {
        if (score >= THRESHOLD) {
            if (score >= 63)      return "매우 활발하고 주도적인 성격으로, 새로운 사람과 상황을 즉시 주도하려는 성향이 강합니다.";
            else if (score >= 50) return "외향적이고 에너지가 넘쳐 팀 분위기를 이끄는 데 주저함이 없습니다.";
            else                  return "사교적이고 활발한 성향이지만, 때로는 다른 사람의 의견을 수용하며 균형을 맞춥니다.";
        } else {
            int iScore = MAX_DIM - score;
            if (iScore >= 63)      return "매우 조용하고 독립적이며, 혼자서 작업하는 것을 선호하는 성향이 강합니다.";
            else if (iScore >= 50) return "내향적이고 사려 깊은 성격으로, 깊이 있는 사고와 집중력을 발휘합니다.";
            else                   return "차분하고 내성적인 성향으로, 심사숙고한 결정을 선호하지만 적절히 소통하려 합니다.";
        }
    }

    private String describePD(int score) {
        if (score >= THRESHOLD) {
            if (score >= 63)      return "철저한 계획형 성향으로, 세부 일정까지 완벽하게 준비하면서 안정적인 추진을 선호합니다.";
            else if (score >= 50) return "체계적인 계획과 준비가 돋보여, 마감 기한을 잘 지키고 업무를 조직적으로 관리합니다.";
            else                  return "일정 수준의 계획력을 발휘해 업무를 체계적으로 준비하지만, 상황 변화에 유연하게 대응하기도 합니다.";
        } else {
            int dScore = MAX_DIM - score;
            if (dScore >= 63)      return "매우 추진력이 넘치고 돌발 상황에도 신속하게 대응하는 강력한 실행형 리더입니다.";
            else if (dScore >= 50) return "강한 추진력으로 빠른 의사결정과 실행을 즐기며, 결과 중심적으로 행동합니다.";
            else                   return "적절한 추진력이 있어 일을 빠르게 시작하지만, 계획 보완이 필요할 때가 있습니다.";
        }
    }

    private String describeVA(int score) {
        if (score >= THRESHOLD) {
            if (score >= 63)      return "탁월한 혁신가로서 독창적인 아이디어를 끊임없이 발굴하며, 변화를 주도합니다.";
            else if (score >= 50) return "높은 창의적 사고로 혁신적인 해결책을 제시하며, 가치를 창출하는 데 집중합니다.";
            else                  return "적당한 창의력을 발휘해 새로운 아이디어를 제안하지만, 때로는 실용성과 균형을 고려합니다.";
        } else {
            int aScore = MAX_DIM - score;
            if (aScore >= 63)      return "철저한 분석가로서, 모든 정보를 세밀하게 검토하고 신중한 결정을 내리는 데 강합니다.";
            else if (aScore >= 50) return "논리적 분석과 데이터 기반 의사결정이 강점이며, 디테일하게 업무를 검토합니다.";
            else                   return "기본적인 분석력을 활용해 문제를 해결하지만, 때로는 실행 중심으로 전진하기도 합니다.";
        }
    }

    private String describeCL(int score) {
        if (score >= THRESHOLD) {
            if (score >= 63)      return "탁월한 협업가로서, 갈등을 중재하고 모두가 만족하는 결론을 이끌어냅니다.";
            else if (score >= 50) return "우수한 조율 능력으로 팀워크를 강화하며, 다양한 의견을 수용해 협력합니다.";
            else                  return "협업을 중시하여 팀원 간 소통에 기여하지만, 때로는 의견을 조율하는 데 시간이 걸립니다.";
        } else {
            int lScore = MAX_DIM - score;
            if (lScore >= 63)      return "강한 독립성과 책임감을 바탕으로, 주어진 업무를 스스로 계획하고 완수하는 데 뛰어납니다.";
            else if (lScore >= 50) return "높은 책임감과 독립적인 문제 해결 능력을 보유하여, 신뢰할 수 있는 역할을 수행합니다.";
            else                   return "적절한 독립성을 유지하며 책임감 있게 업무를 처리하지만, 협업을 보완해야 할 때도 있습니다.";
        }
    }
}