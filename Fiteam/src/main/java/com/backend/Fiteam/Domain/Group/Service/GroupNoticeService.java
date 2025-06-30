package com.backend.Fiteam.Domain.Group.Service;

import com.backend.Fiteam.ConfigEnum.GlobalEnum.NotificationEventType;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.SenderType;
import com.backend.Fiteam.Domain.Group.Dto.GroupNoticeDetailDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupNoticeRequestDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupNoticeSummaryDto;
import com.backend.Fiteam.Domain.Group.Entity.GroupMember;
import com.backend.Fiteam.Domain.Group.Entity.GroupNotice;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.Group.Repository.GroupNoticeRepository;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Notification.Service.NotificationService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.NoSuchElementException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupNoticeService {

    private final GroupNoticeRepository noticeRepository;
    private final ProjectGroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final NotificationService notificationService;
    private final ProjectGroupRepository projectGroupRepository;

    @Transactional
    public GroupNotice createNotice(Integer managerId, GroupNoticeRequestDto dto) {
        // 1) 그룹 존재 및 권한 체크
        ProjectGroup group = groupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다: " + dto.getGroupId()));
        if (!group.getManagerId().equals(managerId)) {
            throw new IllegalArgumentException("해당 그룹을 관리할 권한이 없습니다.");
        }

        // 2) 엔티티 생성 및 저장
        GroupNotice notice = GroupNotice.builder()
                .managerId(managerId)
                .groupId(dto.getGroupId())
                .title(dto.getTitle())
                .context(dto.getContext())
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
        GroupNotice saved = noticeRepository.save(notice);

        // 3) 모든 그룹 멤버에게 알림 생성 & 푸시
        List<GroupMember> members = groupMemberRepository
                .findAllByGroupIdAndIsAcceptedTrue(dto.getGroupId());
        for (GroupMember member : members) {
            if (member.getUserId().equals(managerId)) continue;

            String message = String.format(
                    "[%s] 새로운 공지: %s\n%s",
                    group.getName(),
                    dto.getTitle(),
                    dto.getContext()       // Notice에 저장된 전체 내용
            );

            notificationService.createAndPushNotification(
                    member.getUserId(),
                    managerId,
                    SenderType.MANAGER,
                    NotificationEventType.GROUP_NOTICE,
                    message
            );
        }

        return saved;
    }

    @Transactional
    public GroupNotice updateNotice(Integer managerId, Integer noticeId, GroupNoticeRequestDto dto) {
        var notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoSuchElementException("수정할 공지를 찾을 수 없습니다: " + noticeId));
        if (!notice.getManagerId().equals(managerId)) {
            throw new IllegalArgumentException("본인이 작성한 공지만 수정할 수 있습니다.");
        }

        notice.setTitle(dto.getTitle());
        notice.setContext(dto.getContext());
        notice.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        return noticeRepository.save(notice);
    }

    @Transactional
    public void deleteNotice(Integer managerId, Integer noticeId) {
        var notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoSuchElementException("삭제할 공지를 찾을 수 없습니다: " + noticeId));
        if (!notice.getManagerId().equals(managerId)) {
            throw new IllegalArgumentException("본인이 작성한 공지만 삭제할 수 있습니다.");
        }
        noticeRepository.delete(notice);
    }


    @Transactional(readOnly = true)
    public List<GroupNoticeSummaryDto> getNoticesByManager(Integer managerId) {
        // 1) 매니저가 작성한 모든 공지 가져오기
        List<GroupNotice> notices = noticeRepository
                .findAllByManagerIdOrderByCreatedAtDesc(managerId);

        // 2) 필요한 groupId 집합 추출
        Set<Integer> groupIds = notices.stream()
                .map(GroupNotice::getGroupId)
                .collect(Collectors.toSet());

        // 3) 한 번에 그룹 엔티티 로드 → ID → 이름 맵으로 변환
        Map<Integer, String> nameMap = groupRepository.findAllById(groupIds)
                .stream()
                .collect(Collectors.toMap(ProjectGroup::getId, ProjectGroup::getName));

        // 4) DTO로 변환할 때 groupName 주입
        return notices.stream()
                .map(n -> GroupNoticeSummaryDto.builder()
                        .id(n.getId())
                        .groupId(n.getGroupId())
                        .groupName(nameMap.get(n.getGroupId()))   // ← 여기서 이름 설정
                        .title(n.getTitle())
                        .createdAt(n.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GroupNoticeDetailDto getNoticeById(Integer noticeId) {
        // 공지 존재 및 작성자 검증
        GroupNotice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoSuchElementException("공지 없음: " + noticeId));

        // 그룹 이름 조회
        String groupName = groupRepository.findById(notice.getGroupId())
                .orElseThrow(() -> new NoSuchElementException("그룹 없음: " + notice.getGroupId()))
                .getName();

        // DTO 반환
        return GroupNoticeDetailDto.builder()
                .id(notice.getId())
                .managerId(notice.getManagerId())
                .groupId(notice.getGroupId())
                .groupName(groupName)
                .title(notice.getTitle())
                .context(notice.getContext())
                .createdAt(notice.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<GroupNoticeSummaryDto> getNoticesForMember(Integer userId, Integer groupId) {
        // 1) 권한 체크: userId가 groupId에 속한(수락된) 멤버인지
        boolean inGroup = groupMemberRepository
                .existsByGroupIdAndUserIdAndIsAcceptedTrue(groupId, userId);
        if (!inGroup) {
            throw new IllegalArgumentException("해당 그룹의 멤버가 아닙니다.");
        }

        // 2) 공지 조회
        List<GroupNotice> notices = noticeRepository
                .findAllByGroupIdOrderByCreatedAtDesc(groupId);

        // 3) 그룹 이름
        ProjectGroup group = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("그룹이 없습니다: " + groupId));

        // 4) DTO 매핑
        return notices.stream()
                .map(n -> GroupNoticeSummaryDto.builder()
                        .id(n.getId())
                        .groupId(groupId)
                        .groupName(group.getName())
                        .title(n.getTitle())
                        .createdAt(n.getCreatedAt())
                        .build()
                )
                .collect(Collectors.toList());
    }
}
