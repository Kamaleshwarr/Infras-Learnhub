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
import com.company.learninghub.learn.dto.TechnologyCreateRequest;
import com.company.learninghub.learn.dto.TechnologyResponse;
import com.company.learninghub.learn.dto.TechnologyUpdateRequest;
import com.company.learninghub.learn.mapper.LearnTechnologyMapper;
import com.company.learninghub.learn.repository.LearnTechnologyProjectLinkRepository;
import com.company.learninghub.learn.repository.LearnTechnologyRepository;
import com.company.learninghub.projectknowledge.domain.Project;
import com.company.learninghub.projectknowledge.repository.ProjectRepository;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
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

    private static final int NAME_MAX_LENGTH = 100;
    private static final int SHORT_NAME_MAX_LENGTH = 30;
    private static final int DESCRIPTION_MAX_LENGTH = 2000;

    private final LearnTechnologyRepository technologyRepository;
    private final LearnTechnologyProjectLinkRepository projectLinkRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final LearnTechnologyMapper technologyMapper;

    @Autowired
    public LearnTechnologyService(
            LearnTechnologyRepository technologyRepository,
            LearnTechnologyProjectLinkRepository projectLinkRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            LearnTechnologyMapper technologyMapper
    ) {
        this.technologyRepository = technologyRepository;
        this.projectLinkRepository = projectLinkRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.technologyMapper = technologyMapper;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TechnologyResponse create(TechnologyCreateRequest request, AuthenticatedUser authenticatedUser) {
        User createdBy = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));

        validateMetadata(
                request.name(),
                request.shortName(),
                request.description(),
                request.category(),
                request.difficulty()
        );
        assertUniqueName(request.name(), null);

        LearnTechnology technology = new LearnTechnology(
                normalizeRequired(request.name(), "name is required"),
                normalizeRequired(request.shortName(), "shortName is required"),
                normalizeOptional(request.description()),
                request.category(),
                request.difficulty(),
                TechnologyStatus.DRAFT,
                false,
                createdBy
        );

        return technologyMapper.toResponse(technologyRepository.save(technology), List.of());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TechnologyResponse update(UUID technologyId, TechnologyUpdateRequest request) {
        LearnTechnology technology = findTechnologyOrThrow(technologyId);
        validateMetadata(
                request.name(),
                request.shortName(),
                request.description(),
                request.category(),
                request.difficulty()
        );
        assertUniqueName(request.name(), technologyId);

        boolean featured = request.featured() != null ? request.featured() : technology.isFeatured();
        technology.updateDetails(
                normalizeRequired(request.name(), "name is required"),
                normalizeRequired(request.shortName(), "shortName is required"),
                normalizeOptional(request.description()),
                request.category(),
                request.difficulty(),
                featured
        );

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
                normalizePageable(pageable)
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
        return listTechnologies(status, search, category, difficulty, normalizePageable(pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public List<RelatedTechnologySummary> listPublishedTechnologiesForProject(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project was not found");
        }
        return projectLinkRepository.findPublishedTechnologiesByProjectId(projectId, TechnologyStatus.PUBLISHED)
                .stream()
                .map(link -> technologyMapper.toRelatedTechnologySummary(link.getTechnology()))
                .toList();
    }

    private Page<TechnologyResponse> listTechnologies(
            TechnologyStatus status,
            String search,
            TechnologyCategory category,
            TechnologyDifficulty difficulty,
            Pageable pageable
    ) {
        String normalizedSearch = normalizeSearch(search);
        Specification<LearnTechnology> specification = Specification
                .where(hasStatus(status))
                .and(matchesSearch(normalizedSearch))
                .and(hasCategory(category))
                .and(hasDifficulty(difficulty));

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
        boolean allowed = switch (currentStatus) {
            case DRAFT -> targetStatus == TechnologyStatus.PUBLISHED;
            case PUBLISHED -> targetStatus == TechnologyStatus.ARCHIVED;
            case ARCHIVED -> false;
        };
        if (!allowed) {
            throw new IllegalArgumentException(
                    "Cannot transition technology from " + currentStatus + " to " + targetStatus
            );
        }
    }

    private void validateMetadata(
            String name,
            String shortName,
            String description,
            TechnologyCategory category,
            TechnologyDifficulty difficulty
    ) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("name is required");
        }
        if (name.trim().length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("name must be 100 characters or fewer");
        }
        if (!StringUtils.hasText(shortName)) {
            throw new IllegalArgumentException("shortName is required");
        }
        if (shortName.trim().length() > SHORT_NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("shortName must be 30 characters or fewer");
        }
        if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException("description must be 2000 characters or fewer");
        }
        if (category == null) {
            throw new IllegalArgumentException("category is required");
        }
        if (difficulty == null) {
            throw new IllegalArgumentException("difficulty is required");
        }
    }

    private void assertUniqueName(String name, UUID excludeId) {
        String normalizedName = normalizeRequired(name, "name is required");
        boolean duplicate = excludeId == null
                ? technologyRepository.existsByNameIgnoreCase(normalizedName)
                : technologyRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, excludeId);
        if (duplicate) {
            throw new BusinessConflictException("A technology with this name already exists");
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

    private String normalizeRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
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
            String pattern = "%" + search.toLowerCase(Locale.ROOT) + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("shortName")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
            );
        };
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
            case "id", "name", "shortName", "category", "difficulty", "status", "featured", "createdAt", "updatedAt" ->
                    order.getProperty();
            case "createdAtUtc" -> "createdAt";
            case "updatedAtUtc" -> "updatedAt";
            default -> throw new IllegalArgumentException("Unsupported sort property: " + order.getProperty());
        };

        Sort.Order translated = new Sort.Order(order.getDirection(), property, order.getNullHandling());
        return order.isIgnoreCase() ? translated.ignoreCase() : translated;
    }
}
