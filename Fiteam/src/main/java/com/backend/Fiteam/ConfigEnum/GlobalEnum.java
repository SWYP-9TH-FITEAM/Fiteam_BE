package com.backend.Fiteam.ConfigEnum;


import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonCreator;

public class GlobalEnum {

    // ✅ 예시 1: 사용자 권한
    public enum UserRole {
        USER,      // 일반 사용자
        MANAGER,   // 관리자
        ADMIN      // 최고 관리자
    }

    // ✅ 예시 2: 공통 상태값
    public enum Status {
        ACTIVE,     // 활성 상태
        INACTIVE,   // 비활성 상태
        DELETED     // 삭제 상태
    }

    // ✅ 예시 3: 알림 타입
    public enum NotificationType {
        EMAIL,      // 이메일 알림
        SMS,        // 문자 알림
        PUSH        // 푸시 알림
    }

    public enum TeamStatus implements EnumType {
        WAITING(0, "대기중"),
        RECRUITING(1, "모집중"),
        CLOSED(2, "모집마감"),
        FIXED(3,"팀확정"),
        TEMP(4, "임시팀");

        private final int code;
        private final String label;

        TeamStatus(int code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }

        public static TeamStatus fromCode(Integer code) {
            return Arrays.stream(values())
                    .filter(v -> v.code == code)
                    .findFirst()
                    .orElse(null);
        }

        @JsonCreator
        public static TeamStatus fromLabel(String label) {
            return Arrays.stream(values())
                    .filter(v -> v.getLabel().equalsIgnoreCase(label) || v.name().equalsIgnoreCase(label))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid label: " + label));
        }
    }


    public enum TeamRequestStatus implements EnumType {
        PENDING(0, "대기중"),
        APPROVED(1, "승인됨"),
        REJECTED(2, "거절됨");

        private final int code;
        private final String label;

        TeamRequestStatus(int code, String label) {
            this.code = code;
            this.label = label;
        }

        public int getCode() {
            return code;
        }

        public String getLabel() {
            return label;
        }

        public static TeamRequestStatus fromCode(Integer code) {
            return Arrays.stream(values())
                    .filter(v -> v.code == code)
                    .findFirst()
                    .orElse(null);
        }

        @JsonCreator
        public static TeamRequestStatus fromLabel(String label) {
            return Arrays.stream(values())
                    .filter(v -> v.getLabel().equals(label))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid label: " + label));
        }
    }
}

