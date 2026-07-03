package com.company.learninghub.learn.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.BusinessConflictException;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.learn.domain.LearnTechnology;
import com.company.learninghub.learn.domain.LearnTechnologyProjectLink;
import com.company.learninghub.learn.domain.TechnologyCategory;
import com.company.learninghub.learn.domain.TechnologyDifficulty;
import com.company.learninghub.learn.domain.TechnologyStatus;
import com.company.learninghub.learn.dto.RelatedProjectSummary;
import com.company.learninghub.learn.dto.RelatedTechnologySummary;
import com.company.learninghub.learn.dto.TechnologyCurationRequest;
import com.company.learninghub.learn.dto.TechnologyResponse;
import com.company.learninghub.learn.mapper.LearnTechnologyMapper;
import com.company.learninghub.learn.repository.LearnTechnologyProjectLinkRepository;
import com.company.learninghub.learn.repository.LearnTechnologyRepository;
import com.company.learninghub.projectknowledge.domain.Project;
import com.company.learninghub.projectknowledge.repository.ProjectRepository;
import com.company.learninghub.user.domain.RoleName;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class LearnTechnologyService {

    static final String DUPLICATE_PROJECT_LINK_MESSAGE =
            "This organizational project is already linked to the technology.";

    private static final int ORG_NOTES_MAX_LENGTH = 2000;

    private final LearnTechnologyRepository technologyRepository;
    private final LearnTechnologyProjectLinkRepository projectLinkRepository;
    private final ProjectRepository projectRepository;
    private final LearnTechnologyMapper technologyMapper;

    @Autowired
    public LearnTechnologyService(
            LearnTechnologyRepository technologyRepository,
            LearnTechnologyProjectLinkRepository projectLinkRepository,
            ProjectRepository projectRepository,
            LearnTechnologyMapper technologyMapper
    ) {
        this.technologyRepository = technologyRepository;
        this.projectLinkRepository = projectLinkRepository;
        this.projectRepository = projectRepository;
        this.technologyMapper = technologyMapper;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TechnologyResponse updateCuration(UUID technologyId, TechnologyCurationRequest request) {
        LearnTechnology technology = findTechnologyOrThrow(technologyId);

        if (request.featured() != null) {
            technology.setFeaturedOverride(request.featured());
        }
        if (request.status() != null) {
            assertTransition(technology.getStatus(), request.status());
            technology.setStatus(request.status());
        }
        if (request.orgNotes() != null) {
            validateOrgNotes(request.orgNotes());
            technology.setOrgNotes(normalizeOptional(request.orgNotes()));
        }

        return technologyMapper.toResponse(technology, loadRelatedProjects(technologyId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TechnologyResponse publish(UUID technologyId) {
        LearnTechnology technology = findTechnologyOrThrow(technologyId);
        assertTransition(technology.getStatus(), TechnologyStatus.PUBLISHED);
        technology.setStatus(TechnologyStatus.PUBLISHED);
        return technologyMapper.toResponse(technology, loadRelatedProjects(technologyId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TechnologyResponse hide(UUID technologyId) {
        LearnTechnology technology = findTechnologyOrThrow(technologyId);
        assertTransition(technology.getStatus(), TechnologyStatus.HIDDEN);
        technology.setStatus(TechnologyStatus.HIDDEN);
        return technologyMapper.toResponse(technology, loadRelatedProjects(technologyId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TechnologyResponse archive(UUID technologyId) {
        LearnTechnology technology = findTechnologyOrThrow(technologyId);
        assertTransition(technology.getStatus(), TechnologyStatus.ARCHIVED);
        technology.setStatus(TechnologyStatus.ARCHIVED);
        return technologyMapper.toResponse(technology, loadRelatedProjects(technologyId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TechnologyResponse addProjectLink(UUID technologyId, UUID projectId) {
        LearnTechnology technology = findTechnologyOrThrow(technologyId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project was not found"));

        if (projectLinkRepository.existsByTechnologyIdAndProjectId(technologyId, projectId)) {
            throw new BusinessConflictException(DUPLICATE_PROJECT_LINK_MESSAGE);
        }

        projectLinkRepository.save(new LearnTechnologyProjectLink(technology, project));
        return technologyMapper.toResponse(technology, loadRelatedProjects(technologyId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TechnologyResponse removeProjectLink(UUID technologyId, UUID projectId) {
        LearnTechnology technology = findTechnologyOrThrow(technologyId);
        if (!projectLinkRepository.existsByTechnologyIdAndProjectId(technologyId, projectId)) {
            throw new ResourceNotFoundException("Technology project link was not found");
        }
        projectLinkRepository.deleteByTechnologyIdAndProjectId(technologyId, projectId);
        return technologyMapper.toResponse(technology, loadRelatedProjects(technologyId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public TechnologyResponse getById(UUID technologyId, AuthenticatedUser authenticatedUser) {
        LearnTechnology technology = findTechnologyOrThrow(technologyId);
        if (isAdmin(authenticatedUser) || technology.isVisibleToEmployees()) {
            return technologyMapper.toResponse(technology, loadRelatedProjects(technologyId));
        }
        throw new ResourceNotFoundException("Technology was not found");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public Page<TechnologyResponse> listEmployeeTechnologies(
            String search,
            TechnologyCategory category,
            TechnologyDifficulty difficulty,
            Pageable pageable
    ) {
        return listTechnologies(
                TechnologyStatus.PUBLISHED,
                search,
                category,
                difficulty,
                normalizePageable(pageable),
                true
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<TechnologyResponse> listAdminTechnologies(
            TechnologyStatus status,
            String search,
            TechnologyCategory category,
            TechnologyDifficulty difficulty,
            Pageable pageable
    ) {
        return listTechnologies(status, search, category, difficulty, normalizePageable(pageable), false);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public List<RelatedTechnologySummary> listPublishedTechnologiesForProject(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project was not found");
        }
        return projectLinkRepository.findPublishedTechnologiesByProjectId(projectId, TechnologyStatus.PUBLISHED)
                .stream()
                .map(LearnTechnologyProjectLink::getTechnology)
                .filter(LearnTechnology::isCatalogPresent)
                .map(technologyMapper::toRelatedTechnologySummary)
                .toList();
    }

    private Page<TechnologyResponse> listTechnologies(
            TechnologyStatus status,
            String search,
            TechnologyCategory category,
            TechnologyDifficulty difficulty,
            Pageable pageable,
            boolean employeeView
    ) {
        String normalizedSearch = normalizeSearch(search);
        Specification<LearnTechnology> specification = Specification
                .where(hasStatus(status))
                .and(matchesSearch(normalizedSearch))
                .and(hasCategory(category))
                .and(hasDifficulty(difficulty));

        if (employeeView) {
            specification = specification.and(hasCatalogPresent(true));
        }

        return technologyRepository.findAll(specification, pageable)
                .map(technology -> technologyMapper.toResponse(technology, List.of()));
    }

    private List<RelatedProjectSummary> loadRelatedProjects(UUID technologyId) {
        return technologyMapper.toRelatedProjectSummaries(
                projectLinkRepository.findByTechnologyIdOrderByProject_NameAsc(technologyId)
        );
    }

    private LearnTechnology findTechnologyOrThrow(UUID technologyId) {
        return technologyRepository.findById(technologyId)
                .orElseThrow(() -> new ResourceNotFoundException("Technology was not found"));
    }

    private void assertTransition(TechnologyStatus currentStatus, TechnologyStatus targetStatus) {
        if (currentStatus == targetStatus) {
            return;
        }

        boolean allowed = switch (currentStatus) {
            case HIDDEN -> targetStatus == TechnologyStatus.PUBLISHED;
            case PUBLISHED -> targetStatus == TechnologyStatus.ARCHIVED
                    || targetStatus == TechnologyStatus.HIDDEN;
            case ARCHIVED -> false;
        };
        if (!allowed) {
            throw new IllegalArgumentException(
                    "Cannot transition technology from " + currentStatus + " to " + targetStatus
            );
        }
    }

    private void validateOrgNotes(String orgNotes) {
        if (orgNotes != null && orgNotes.length() > ORG_NOTES_MAX_LENGTH) {
            throw new IllegalArgumentException("orgNotes must be 2000 characters or fewer");
        }
    }

    private boolean isAdmin(AuthenticatedUser authenticatedUser) {
        return authenticatedUser.getRoleNames().contains(RoleName.ADMIN);
    }

    private String normalizeSearch(String search) {
        if (!StringUtils.hasText(search)) {
            return null;
        }
        return search.trim();
    }

    private String normalizeOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private Specification<LearnTechnology> hasCatalogPresent(boolean catalogPresent) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("catalogPresent"), catalogPresent);
    }

    private Specification<LearnTechnology> hasStatus(TechnologyStatus status) {
        return (root, query, criteriaBuilder) -> status == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("status"), status);
    }

    private Specification<LearnTechnology> hasCategory(TechnologyCategory category) {
        return (root, query, criteriaBuilder) -> category == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("category"), category);
    }

    private Specification<LearnTechnology> hasDifficulty(TechnologyDifficulty difficulty) {
        return (root, query, criteriaBuilder) -> difficulty == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("difficulty"), difficulty);
    }

    private Specification<LearnTechnology> matchesSearch(String search) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(search)) {
                return criteriaBuilder.conjunction();
            }
            String term = search.toLowerCase(Locale.ROOT);
            return criteriaBuilder.or(
                    textFieldContainsWholeTerm(criteriaBuilder, root.get("name"), term),
                    textFieldContainsWholeTerm(criteriaBuilder, root.get("shortName"), term),
                    textFieldContainsWholeTerm(criteriaBuilder, root.get("description"), term),
                    slugFieldContainsTerm(criteriaBuilder, root.get("slug"), term),
                    tagsContainExactTerm(criteriaBuilder, root.get("tags"), term)
            );
        };
    }

    private Predicate textFieldContainsWholeTerm(
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            Expression<String> field,
            String term
    ) {
        Expression<String> lowered = criteriaBuilder.lower(field);
        return criteriaBuilder.or(
                criteriaBuilder.equal(lowered, term),
                criteriaBuilder.like(lowered, term + " %"),
                criteriaBuilder.like(lowered, "% " + term + " %"),
                criteriaBuilder.like(lowered, "% " + term),
                criteriaBuilder.like(lowered, term + ",%"),
                criteriaBuilder.like(lowered, "% " + term + ",%"),
                criteriaBuilder.like(lowered, "%(" + term + ")%"),
                criteriaBuilder.like(lowered, "%(" + term + " %")
        );
    }

    private Predicate slugFieldContainsTerm(
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            Expression<String> slugField,
            String term
    ) {
        return criteriaBuilder.or(
                criteriaBuilder.equal(slugField, term),
                criteriaBuilder.like(slugField, term + "-%"),
                criteriaBuilder.like(slugField, "%-" + term),
                criteriaBuilder.like(slugField, "%-" + term + "-%")
        );
    }

    private Predicate tagsContainExactTerm(
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            Expression<?> tagsField,
            String term
    ) {
        String sanitizedTerm = term.replace("\"", "");
        Expression<String> tagsText = criteriaBuilder.function(
                "text",
                String.class,
                tagsField
        );
        return criteriaBuilder.like(criteriaBuilder.lower(tagsText), "%\"" + sanitizedTerm + "\"%");
    }

    private Pageable normalizePageable(Pageable pageable) {
        if (pageable.isUnpaged()) {
            return pageable;
        }

        Sort sort = Sort.by(pageable.getSort().stream()
                .map(this::toRepositorySortOrder)
                .toList());
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private Sort.Order toRepositorySortOrder(Sort.Order order) {
        String property = switch (order.getProperty()) {
            case "id", "slug", "name", "shortName", "category", "difficulty", "status", "createdAt", "updatedAt" ->
                    order.getProperty();
            case "featured" -> "catalogFeatured";
            case "createdAtUtc" -> "createdAt";
            case "updatedAtUtc" -> "updatedAt";
            default -> throw new IllegalArgumentException("Unsupported sort property: " + order.getProperty());
        };

        Sort.Order translated = new Sort.Order(order.getDirection(), property, order.getNullHandling());
        if (order.isIgnoreCase() && isTextSortProperty(property)) {
            return translated.ignoreCase();
        }
        return translated;
    }

    private boolean isTextSortProperty(String property) {
        return "slug".equals(property)
                || "name".equals(property)
                || "shortName".equals(property);
    }
}
