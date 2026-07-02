package com.company.learninghub.learn.mapper;

import com.company.learninghub.learn.domain.LearnTechnology;
import com.company.learninghub.learn.domain.LearnTechnologyProjectLink;
import com.company.learninghub.learn.dto.RelatedProjectSummary;
import com.company.learninghub.learn.dto.RelatedTechnologySummary;
import com.company.learninghub.learn.dto.TechnologyCreatedByResponse;
import com.company.learninghub.learn.dto.TechnologyResponse;
import com.company.learninghub.user.domain.User;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class LearnTechnologyMapper {

    public TechnologyResponse toResponse(LearnTechnology technology) {
        return toResponse(technology, loadRelatedProjects(technology));
    }

    public TechnologyResponse toResponse(LearnTechnology technology, List<RelatedProjectSummary> relatedProjects) {
        return new TechnologyResponse(
                technology.getId(),
                technology.getSlug(),
                technology.getName(),
                technology.getShortName(),
                technology.getDescription(),
                technology.getCategory(),
                technology.getDifficulty(),
                technology.getStatus(),
                technology.isFeatured(),
                technology.getFeaturedOverride(),
                technology.isCatalogFeatured(),
                technology.getEstimatedDuration(),
                technology.getOfficialWebsite(),
                technology.getOfficialDocumentation(),
                technology.getTags(),
                technology.getOrgNotes(),
                technology.getCatalogVersion(),
                technology.getCatalogSource(),
                technology.isCatalogPresent(),
                relatedProjects,
                toCreatedByResponse(technology.getCreatedBy()),
                technology.getCreatedAt(),
                technology.getUpdatedAt()
        );
    }

    public RelatedTechnologySummary toRelatedTechnologySummary(LearnTechnology technology) {
        return new RelatedTechnologySummary(
                technology.getId(),
                technology.getName(),
                technology.getShortName()
        );
    }

    public RelatedProjectSummary toRelatedProjectSummary(LearnTechnologyProjectLink link) {
        return new RelatedProjectSummary(
                link.getProject().getId(),
                link.getProject().getName()
        );
    }

    public List<RelatedProjectSummary> toRelatedProjectSummaries(List<LearnTechnologyProjectLink> links) {
        return links.stream()
                .sorted(Comparator.comparing(link -> link.getProject().getName(), String.CASE_INSENSITIVE_ORDER))
                .map(this::toRelatedProjectSummary)
                .toList();
    }

    private List<RelatedProjectSummary> loadRelatedProjects(LearnTechnology technology) {
        if (technology.getProjectLinks() == null || technology.getProjectLinks().isEmpty()) {
            return List.of();
        }
        return toRelatedProjectSummaries(technology.getProjectLinks());
    }

    private TechnologyCreatedByResponse toCreatedByResponse(User user) {
        return new TechnologyCreatedByResponse(
                user.getId(),
                user.getEmployeeId(),
                user.getFullName(),
                user.getEmail()
        );
    }
}
