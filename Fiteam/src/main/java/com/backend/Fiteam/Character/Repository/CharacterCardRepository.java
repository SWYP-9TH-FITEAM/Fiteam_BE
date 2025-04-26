package com.backend.Fiteam.Character.Repository;

import com.backend.Fiteam.Character.Entity.CharacterCard;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterCardRepository extends JpaRepository<CharacterCard, Integer> {
    Optional<CharacterCard> findById(int id);
}
