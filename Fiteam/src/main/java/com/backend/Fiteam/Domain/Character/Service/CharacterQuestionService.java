package com.backend.Fiteam.Domain.Character.Service;

import com.backend.Fiteam.AppCache.CharacterCardCache;
import com.backend.Fiteam.AppCache.CharacterQuestionCache;
import com.backend.Fiteam.Domain.Character.Dto.SaveTestResultResponseDto;
import com.backend.Fiteam.Domain.Character.Entity.CharacterCard;
import com.backend.Fiteam.Domain.Character.Entity.CharacterQuestion;
import com.backend.Fiteam.Domain.Character.Repository.CharacterCardRepository;
import com.backend.Fiteam.Domain.Character.Repository.CharacterQuestionRepository;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Service
@RequiredArgsConstructor
@RestControllerAdvice
public class CharacterQuestionService {

    private final CharacterCardService characterCardService;
    private final CharacterCardCache characterCardCache;

    private final CharacterQuestionCache characterQuestionCache;

    public List<CharacterQuestion> getAllCharacterQuestions() {
        return characterQuestionCache.getAllQuestions();
    }

    public SaveTestResultResponseDto saveCharacterTestResult(List<Map<String, Integer>> answers) {
        int E = 0, I = 0, P = 0, D = 0, V = 0, A = 0, C = 0, L = 0;

        // 1) 답변 점수 합산
        for (Map<String, Integer> answer : answers) {
            for (Map.Entry<String, Integer> e : answer.entrySet()) {
                switch (e.getKey()) {
                    case "E" -> E += e.getValue();
                    case "I" -> I += e.getValue();
                    case "P" -> P += e.getValue();
                    case "D" -> D += e.getValue();
                    case "V" -> V += e.getValue();
                    case "A" -> A += e.getValue();
                    case "C" -> C += e.getValue();
                    case "L" -> L += e.getValue();
                }
            }
        }

        int numEI = E;
        int numPD = P;
        int numVA = V;
        int numCL = C;

        // 2) 코드 생성
        StringBuilder codeBuilder = new StringBuilder();
        codeBuilder.append(E >= I ? "E" : "I");
        codeBuilder.append(P >= D ? "P" : "D");
        codeBuilder.append(V >= A ? "V" : "A");
        codeBuilder.append(C >= L ? "C" : "L");
        String code = codeBuilder.toString();

        // 3) CharacterCard 조회 → 인메모리 캐시 사용
        CharacterCard characterCard = characterCardCache.getCardByCode(code);

        String description = characterCardService.buildCharacterDescription(E, P, V, C);

        // 4) DTO 반환 (DB 저장 없음)
        return SaveTestResultResponseDto.builder()
                .cardId(characterCard.getId())
                .numEI(numEI)
                .numPD(numPD)
                .numVA(numVA)
                .numCL(numCL)
                .details(description)
                .build();
    }
}
