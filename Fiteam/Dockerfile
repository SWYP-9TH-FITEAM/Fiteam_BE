# 1. OpenJDK 23 이미지 사용 (eclipse-temurin 권장)
FROM eclipse-temurin:23-jdk-alpine

COPY Fiteam-0.0.1-SNAPSHOT.jar app.jar

# 3. Spring Boot 앱 포트
EXPOSE 8080

# 4. 앱 실행
ENTRYPOINT ["java", "-jar", "app.jar"]


