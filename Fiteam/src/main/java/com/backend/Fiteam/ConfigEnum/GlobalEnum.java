package com.backend.Fiteam.ConfigEnum;


import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonCreator;

public class GlobalEnum {

    public enum SenderType implements EnumType {
        USER(2, "User"),
        MANAGER(1, "Manager"),
        ADMIN(0, "Admin");

        private final int code;
        private final String label;
        SenderType(int code, String label) {this.code = code;this.label = label;}
        @Override public int getCode() {return code;}
        @Override public String getLabel() {return label;}
        @JsonCreator
        public static SenderType fromLabel(String label) {
            return Arrays.stream(values())
                    .filter(e -> e.label.equalsIgnoreCase(label) || e.name().equalsIgnoreCase(label))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown SenderType label: " + label));
        }
    }
    public enum NotificationEventType implements EnumType {
        GROUP_INVITE(0, "Group invite"),
        GROUP_NOTICE(1, "GroupNotice"),
        TEAM_CONTACTS(2, "TeamContacts"),
        RANDOM_TEAM_BUILDING_RESULT(3, "RandomTeamBuilding"),
        TEAM_BUILDING_START(4, "TeamBuildingStart"),
        TEAM_BUILDING_END(5, "TeamBuildingEnd"),
        TEAM_LEADER_CHANGE(6, "Team_Leader_Change");

        private final int code;
        private final String label;
        NotificationEventType(int code, String label) {this.code = code;this.label = label;}
        @Override public int getCode() {return code;}
        @Override public String getLabel() {return label;}
        @JsonCreator
        public static NotificationEventType fromLabel(String label) {
            return Arrays.stream(values())
                    .filter(e -> e.label.equalsIgnoreCase(label) || e.name().equalsIgnoreCase(label))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown NotificationEventType label: " + label));
        }
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

