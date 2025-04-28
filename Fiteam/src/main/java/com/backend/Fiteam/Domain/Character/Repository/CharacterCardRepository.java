package com.backend.Fiteam.Domain.Character.Repository;

import com.backend.Fiteam.Domain.Character.Entity.CharacterCard;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterCardRepository extends JpaRepository<CharacterCard, Integer> {
    Optional<CharacterCard> findById(int id);
}
