package com.backend.Fiteam.Domain.Character.Controller;

import com.backend.Fiteam.Domain.Character.Dto.SaveTestResultResponseDto;
import com.backend.Fiteam.Domain.Character.Entity.CharacterQuestion;
import com.backend.Fiteam.Domain.Character.Service.CharacterQuestionService;
import com.backend.Fiteam.Domain.User.Dto.SaveTestAnswerRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/question")
@RequiredArgsConstructor
public class CharacterQuestionController {

    private final CharacterQuestionService characterQuestionService;

    @Operation(description = "질문 문항 전체 가져오기")
    @GetMapping("/all")
    public ResponseEntity<List<CharacterQuestion>> getAllCharacterQuestions() {
        List<CharacterQuestion> questions = characterQuestionService.getAllCharacterQuestions();
        return ResponseEntity.ok(questions);
    }

    @Operation(summary = "로그인 없이 성향검사 결과 연산", description = "질문 결과를 연산하여 캐릭터카드 ID와 4가지 성향 점수를 반환합니다.")
    @PostMapping("/unauth/test-result")
    public ResponseEntity<SaveTestResultResponseDto> saveTestResult(@RequestBody SaveTestAnswerRequestDto requestDto) {
        try {
            SaveTestResultResponseDto response = characterQuestionService.saveCharacterTestResult(requestDto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
