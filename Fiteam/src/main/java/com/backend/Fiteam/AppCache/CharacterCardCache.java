package com.backend.Fiteam.AppCache;

import com.backend.Fiteam.Domain.Character.Entity.CharacterCard;
import com.backend.Fiteam.Domain.Character.Repository.CharacterCardRepository;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CharacterCardCache {

    // @PostConstruct + 불변 Map
    // 가장 단순하게, 애플리케이션 시작 시 한 번만 로딩하고 변경이 전혀 없는 경우라서 불변(immutable) Map으로 관리

    private final CharacterCardRepository characterCardRepository;
    private Map<Integer, CharacterCard> cacheById;
    private Map<String, CharacterCard> cacheByCode;

    @PostConstruct
    public void init() {
        List<CharacterCard> all = characterCardRepository.findAll();
        cacheById = all.stream()
                .collect(Collectors.toUnmodifiableMap(CharacterCard::getId, Function.identity()));
        cacheByCode = all.stream()
                .collect(Collectors.toUnmodifiableMap(CharacterCard::getCode, Function.identity()));
    }

    public CharacterCard getCardById(Integer id) {
        CharacterCard card = cacheById.get(id);
        if (card == null) {
            throw new NoSuchElementException("해당 카드 정보를 찾을 수 없습니다. id: " + id);
        }
        return card;
    }

    public CharacterCard getCardByCode(String code) {
        CharacterCard card = cacheByCode.get(code);
        if (card == null) {
            throw new NoSuchElementException("해당 카드 정보를 찾을 수 없습니다. code: " + code);
        }
        return card;
    }

    public List<CharacterCard> getAllCards() {
        return new ArrayList<>(cacheById.values());
    }
}

