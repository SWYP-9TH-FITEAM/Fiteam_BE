package com.backend.Fiteam.Domain.Character.Controller;

import com.backend.Fiteam.Domain.Character.Entity.CharacterQuestion;
import com.backend.Fiteam.Domain.Character.Service.CharacterQuestionService;
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
    /*
    @GetMapping("/{id}")
    public ResponseEntity<Optional<CharacterQuestion>> getCharacterQuestionById(@PathVariable int id) {
        Optional<CharacterQuestion> question = characterQuestionService.getCharacterQuestionById(id);
        if (question.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(question);
    }
    */

    @Operation(description = "질문 문항 전체 가져오기")
    @GetMapping("/all")
    public ResponseEntity<List<CharacterQuestion>> getAllCharacterQuestions() {
        List<CharacterQuestion> questions = characterQuestionService.getAllCharacterQuestions();
        return ResponseEntity.ok(questions);
    }
}
