package com.backend.Fiteam.Domain.Character.Service;

import com.backend.Fiteam.Domain.Character.Entity.CharacterQuestion;
import com.backend.Fiteam.Domain.Character.Repository.CharacterQuestionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Service
@RequiredArgsConstructor
@RestControllerAdvice
public class CharacterQuestionService {

    private final CharacterQuestionRepository characterQuestionRepository;

    /*
    public Optional<CharacterQuestion> getCharacterQuestionById(int id) {
        return characterQuestionRepository.findById(id);
    }
    */

    public List<CharacterQuestion> getAllCharacterQuestions() {
        return characterQuestionRepository.findAll();
    }
}
