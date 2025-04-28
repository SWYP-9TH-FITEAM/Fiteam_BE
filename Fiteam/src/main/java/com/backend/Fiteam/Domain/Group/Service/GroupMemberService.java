package com.backend.Fiteam.Domain.Group.Service;


import com.backend.Fiteam.Domain.Group.Entity.GroupMember;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.User.Dto.UserGroupProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Service
@RequiredArgsConstructor
@RestControllerAdvice
public class GroupMemberService {
    private final GroupMemberRepository groupMemberRepository;


    @Transactional
    public void updateGroupMemberProfile(Integer groupMemberId, Integer userId, UserGroupProfileDto requestDto) {
        GroupMember groupMember = groupMemberRepository.findById(groupMemberId)
                .orElseThrow(() -> new IllegalArgumentException("그룹 멤버를 찾을 수 없습니다."));

        if (!groupMember.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 그룹 멤버 정보만 수정할 수 있습니다.");
        }

        // null 체크 후 값이 있을 때만 수정
        if (requestDto.getPosition() != null) {
            groupMember.setPosition(requestDto.getPosition());
        }
        if (requestDto.getWorkHistory() != null) {
            groupMember.setWorkHistory(requestDto.getWorkHistory());
        }
        if (requestDto.getProjectGoal() != null) {
            groupMember.setProjectGoal(requestDto.getProjectGoal());
        }
        if (requestDto.getUrl() != null) {
            groupMember.setUrl(requestDto.getUrl());
        }
        if (requestDto.getIntroduction() != null) {
            groupMember.setIntroduction(requestDto.getIntroduction());
        }
    }
}
