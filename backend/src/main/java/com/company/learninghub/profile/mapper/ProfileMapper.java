package com.company.learninghub.profile.mapper;

import com.company.learninghub.profile.dto.ProfileResponse;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ProfileMapper {

    private static final String AVATAR_URL = "/api/v1/profile/avatar";

    public ProfileResponse toResponse(User user) {
        boolean hasAvatar = StringUtils.hasText(user.getAvatarStorageKey());
        return new ProfileResponse(
                user.getId(),
                user.getEmployeeId(),
                user.getFullName(),
                user.getEmail(),
                primaryRole(user),
                user.isActive(),
                user.isMustChangePassword(),
                hasAvatar,
                hasAvatar ? AVATAR_URL : null,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private RoleName primaryRole(User user) {
        if (user.hasRoleName(RoleName.ADMIN)) {
            return RoleName.ADMIN;
        }
        if (user.hasRoleName(RoleName.EMPLOYEE)) {
            return RoleName.EMPLOYEE;
        }
        return null;
    }
}
