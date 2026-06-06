package com.company.learninghub.initiative.mapper;

import com.company.learninghub.initiative.domain.LearningInitiative;
import com.company.learninghub.initiative.dto.InitiativeCreatedByResponse;
import com.company.learninghub.initiative.dto.InitiativeResponse;
import com.company.learninghub.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class LearningInitiativeMapper {

    public InitiativeResponse toResponse(LearningInitiative initiative) {
        return new InitiativeResponse(
                initiative.getId(),
                initiative.getTitle(),
                initiative.getDescription(),
                initiative.getRewardDescription(),
                initiative.getStartDateUtc(),
                initiative.getExpiryDateUtc(),
                initiative.getStatus(),
                toCreatedByResponse(initiative.getCreatedBy()),
                initiative.getCreatedAt(),
                initiative.getUpdatedAt()
        );
    }

    private InitiativeCreatedByResponse toCreatedByResponse(User user) {
        return new InitiativeCreatedByResponse(
                user.getId(),
                user.getEmployeeId(),
                user.getFullName(),
                user.getEmail()
        );
    }
}

