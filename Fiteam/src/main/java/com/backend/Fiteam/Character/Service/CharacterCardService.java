package com.backend.Fiteam.Character.Service;

import com.backend.Fiteam.Character.Entity.CharacterCard;
import com.backend.Fiteam.Character.Repository.CharacterCardRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CharacterCardService {
    private final CharacterCardRepository characterCardRepository;

    public Optional<CharacterCard> getCharacterCardById(int id) {
        return characterCardRepository.findById(id);
    }

    public List<CharacterCard> getAllCharacterCards() {
        return characterCardRepository.findAll();
    }

}