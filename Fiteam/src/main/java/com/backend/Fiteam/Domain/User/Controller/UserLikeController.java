package com.backend.Fiteam.Domain.User.Controller;

import com.backend.Fiteam.Domain.User.Dto.UserLikeRequestDto;
import com.backend.Fiteam.Domain.User.Dto.UserLikeResponseDto;
import com.backend.Fiteam.Domain.User.Service.UserLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/like")
@RequiredArgsConstructor
@Tag(name = "4. UserLikeController - 그룹에서 유저 Like 관련")
public class UserLikeController {

    /*
    1.좋아요 표시
    2.좋아요 취소
    3.좋아요한 유저 List get
    4.좋아요 메모 확인하기
    */

    private final UserLikeService userLikeService;

    // 1.그룹 내 다른 유저에게 좋아요 표시
    @Operation(summary = "1.그룹 내 다른 유저에게 좋아요 표시", description = "JWT 인증된 사용자가 같은 그룹의 다른 유저에게 좋아요를 남깁니다.")
    @PostMapping("/add")
    public ResponseEntity<String> likeUser(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserLikeRequestDto dto) {
        Integer senderId = Integer.parseInt(userDetails.getUsername());
        userLikeService.likeUser(senderId, dto);
        return ResponseEntity.ok("좋아요가 등록되었습니다.");
    }

    // 2.좋아요 취소
    @Operation(summary = "2.좋아요 취소. 좋아요한 유저 List get에서 UserLikeID 값을 넣어주세요")
    @DeleteMapping("/unlike/{likeId}")
    public ResponseEntity<String> unlikeUser(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer likeId) {
        Integer senderId = Integer.parseInt(userDetails.getUsername());
        userLikeService.unlikeUser(senderId, likeId);
        return ResponseEntity.ok("좋아요가 취소되었습니다.");
    }

    // 3.좋아요한 유저 List get
    @Operation(summary = "3. 좋아요한 유저 List get.", description = "내가 남긴 좋아요 목록 조회")
    @GetMapping("/list")
    public ResponseEntity<List<UserLikeResponseDto>> getMyLikes(@AuthenticationPrincipal UserDetails userDetails) {
        Integer senderId = Integer.parseInt(userDetails.getUsername());
        List<UserLikeResponseDto> likes = userLikeService.getMyLikes(senderId);
        return ResponseEntity.ok(likes);
    }

    // 4. 좋아요 메모 확인하기
    @Operation(summary = "4. 좋아요 메모 확인하기", description = "JWT 인증된 사용자가 자신이 남긴 상대방에게 작성한 메모를 반환합니다.")
    @GetMapping("/memo/{likeId}")
    public ResponseEntity<String> getMyLikeMemo(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer likeId) {
        Integer senderId = Integer.parseInt(userDetails.getUsername());
        String memo = userLikeService.getLikeMemo(senderId, likeId);
        return ResponseEntity.ok(memo);
    }
}
