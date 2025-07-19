package com.backend.Fiteam.AppCache;

import com.backend.Fiteam.Domain.Character.Entity.CharacterQuestion;
import com.backend.Fiteam.Domain.Character.Repository.CharacterQuestionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class CharacterQuestionCache {

    private final CharacterQuestionRepository characterQuestionRepository;
    private List<CharacterQuestion> cache;

    @PostConstruct
    public void init() {
        // 애플리케이션 시작 시 전체 질문 로드 후 불변 리스트로 저장->질문 갯수만큼
        cache = Collections.unmodifiableList(characterQuestionRepository.findAll());
    }

    public List<CharacterQuestion> getAllQuestions() {
        return cache;
    }

}
