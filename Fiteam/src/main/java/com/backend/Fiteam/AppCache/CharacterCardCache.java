package com.backend.Fiteam.AppCache;

import com.backend.Fiteam.Domain.Character.Entity.CharacterCard;
import com.backend.Fiteam.Domain.Character.Repository.CharacterCardRepository;
import jakarta.annotation.PostConstruct;
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

    private final CharacterCardRepository characterCardRepository;
    private Map<Integer, CharacterCard> cardCache;

    @PostConstruct
    public void loadAllCards() {
        this.cardCache = characterCardRepository.findAll().stream()
                .collect(Collectors.toMap(CharacterCard::getId, Function.identity()));
    }

    public CharacterCard getCard(Integer id) {
        CharacterCard card = cardCache.get(id);
        if (card == null) {
            throw new NoSuchElementException("해당 카드 정보를 찾을 수 없습니다. id: " + id);
        }
        return card;
    }

    public List<CharacterCard> getCardList(List<Integer> ids) {
        return ids.stream()
                .map(cardCache::get)
                .filter(Objects::nonNull)
                .toList();
    }
}

