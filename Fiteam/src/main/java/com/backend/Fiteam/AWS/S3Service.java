package com.backend.Fiteam.AWS;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(String folder, Integer Id, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 비어 있습니다.");
        }

        // 파일 이름 지정
        String fileName = Id + "_img" + ".jpg";

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucketName, folder+fileName, inputStream, metadata));

            return amazonS3.getUrl(bucketName, folder+fileName).toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("S3 업로드 실패: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String fileName, String fileFolder) {
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, fileFolder+fileName));
            System.out.println("S3 삭제 성공: " + fileName);
        } catch (Exception e) {
            System.err.println("S3 삭제 실패: " + e.getMessage());
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다.", e);
        }
    }
}
