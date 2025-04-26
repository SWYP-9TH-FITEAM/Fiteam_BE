package com.backend.Fiteam.Character.Repository;

import com.backend.Fiteam.Character.Entity.CharacterQuestion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterQuestionRepository extends JpaRepository<CharacterQuestion, Integer> {
    Optional<CharacterQuestion> findById(int id);
}
