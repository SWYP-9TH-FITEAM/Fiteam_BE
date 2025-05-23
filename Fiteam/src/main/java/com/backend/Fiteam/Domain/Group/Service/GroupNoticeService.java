package com.backend.Fiteam.Domain.Group.Service;

import com.backend.Fiteam.Domain.Group.Dto.GroupNoticeRequestDto;
import com.backend.Fiteam.Domain.Group.Entity.GroupNotice;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Repository.GroupNoticeRepository;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
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
        return noticeRepository.save(notice);
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
}
