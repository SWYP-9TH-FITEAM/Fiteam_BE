package com.backend.Fiteam.AWS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/v1/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@ModelAttribute UploadRequestDto request) throws IOException {
        String imageUrl = s3Service.uploadFile(request.getFolder() + "/", request.getId(), request.getFile());
        return ResponseEntity.ok(imageUrl);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteImage(@RequestBody DeleteRequestDto request) {
        s3Service.deleteFile(request.getFileName(), request.getFolder() + "/");
        return ResponseEntity.ok("삭제 완료: " + request.getFileName());
    }

    @Data
    @AllArgsConstructor
    public static class UploadRequestDto {
        private MultipartFile file;
        private String folder;
        private Integer id;
    }

    @Data
    @AllArgsConstructor
    public static class DeleteRequestDto {
        private String fileName;
        private String folder;
    }
}
