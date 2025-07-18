// CharacterCardController.java
package com.backend.Fiteam.Domain.Character.Controller;

import com.backend.Fiteam.Domain.Character.Dto.CharacterCardDto;
import com.backend.Fiteam.Domain.Character.Entity.CharacterCard;
import com.backend.Fiteam.Domain.Character.Service.CharacterCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/card")
@RequiredArgsConstructor
@RestControllerAdvice
@Tag(name = "1-1. 캐릭터카드 16개 관련")
public class CharacterCardController {
    private final CharacterCardService characterCardService;

    @Operation(description = "id(1~16)에 해당하는 캐릭터 Card 가져오기")
    @GetMapping("/{id}")
    public ResponseEntity<CharacterCardDto> getCharacterCardById(@PathVariable int id) {
        Optional<CharacterCardDto> card = characterCardService.getCharacterCardById(id);
        if (card.isEmpty()) {
            return ResponseEntity.status(404).body(null); // 404 Not Found
        }
        return ResponseEntity.ok(card.get());
    }

    @Operation(description = "캐릭터 Card 16개 Json List로 가져오기.")
    @GetMapping("/all")
    public ResponseEntity<List<CharacterCard>> getAllCharacterCards() {
        List<CharacterCard> cards = characterCardService.getAllCharacterCards();
        return ResponseEntity.ok(cards);
    }

}
