package com.company.learninghub.initiative.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.initiative.domain.InitiativeStatus;
import com.company.learninghub.initiative.domain.LearningInitiative;
import com.company.learninghub.initiative.dto.CreateInitiativeRequest;
import com.company.learninghub.initiative.dto.InitiativeResponse;
import com.company.learninghub.initiative.dto.InitiativeVisibilityDiagnosticResponse;
import com.company.learninghub.initiative.dto.InitiativeVisibilityDiagnosticsResponse;
import com.company.learninghub.initiative.dto.UpdateInitiativeRequest;
import com.company.learninghub.initiative.mapper.LearningInitiativeMapper;
import com.company.learninghub.initiative.repository.LearningInitiativeRepository;
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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LearningInitiativeService {

    private final LearningInitiativeRepository initiativeRepository;
    private final UserRepository userRepository;
    private final LearningInitiativeMapper initiativeMapper;
    private final Clock clock;

    @Autowired
    public LearningInitiativeService(
            LearningInitiativeRepository initiativeRepository,
            UserRepository userRepository,
            LearningInitiativeMapper initiativeMapper
    ) {
        this(initiativeRepository, userRepository, initiativeMapper, Clock.systemUTC());
    }

    LearningInitiativeService(
            LearningInitiativeRepository initiativeRepository,
            UserRepository userRepository,
            LearningInitiativeMapper initiativeMapper,
            Clock clock
    ) {
        this.initiativeRepository = initiativeRepository;
        this.userRepository = userRepository;
        this.initiativeMapper = initiativeMapper;
        this.clock = clock;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public InitiativeResponse create(CreateInitiativeRequest request, AuthenticatedUser authenticatedUser) {
        User createdBy = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));

        LearningInitiative initiative = new LearningInitiative(
                request.title(),
                request.description(),
                request.rewardDescription(),
                request.startDateUtc(),
                request.expiryDateUtc(),
                request.status(),
                createdBy
        );

        return initiativeMapper.toResponse(initiativeRepository.save(initiative));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public InitiativeResponse update(UUID initiativeId, UpdateInitiativeRequest request) {
        LearningInitiative initiative = findInitiativeOrThrow(initiativeId);
        initiative.updateDetails(
                request.title(),
                request.description(),
                request.rewardDescription(),
                request.startDateUtc(),
                request.expiryDateUtc(),
                request.status()
        );
        return initiativeMapper.toResponse(initiative);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(UUID initiativeId) {
        LearningInitiative initiative = findInitiativeOrThrow(initiativeId);
        initiativeRepository.delete(initiative);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public InitiativeResponse getById(UUID initiativeId, AuthenticatedUser authenticatedUser) {
        LearningInitiative initiative = findInitiativeOrThrow(initiativeId);
        if (isAdmin(authenticatedUser) || initiative.isVisibleToEmployeesAt(clock.instant())) {
            return initiativeMapper.toResponse(initiative);
        }
        throw new ResourceNotFoundException("Learning initiative was not found");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public Page<InitiativeResponse> list(
            InitiativeStatus status,
            String search,
            Pageable pageable,
            AuthenticatedUser authenticatedUser
    ) {
        String normalizedSearch = normalizeSearch(search);
        Pageable repositoryPageable = normalizePageable(pageable);
        Specification<LearningInitiative> specification = isAdmin(authenticatedUser)
                ? adminSpecification(status, normalizedSearch)
                : employeeSpecification(normalizedSearch, Instant.now(clock));
        Page<LearningInitiative> initiatives = initiativeRepository.findAll(specification, repositoryPageable);

        return initiatives.map(initiativeMapper::toResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public InitiativeVisibilityDiagnosticsResponse employeeVisibilityDiagnostics(AuthenticatedUser authenticatedUser) {
        Instant now = Instant.now(clock);
        ZoneOffset utc = ZoneOffset.UTC;
        LocalDate today = LocalDate.ofInstant(now, utc);

        Page<InitiativeResponse> adminActivePage = list(
                InitiativeStatus.ACTIVE,
                null,
                PageRequest.of(0, 100, Sort.by("expiryDateUtc").ascending()),
                authenticatedUser
        );
        Page<InitiativeResponse> employeePage = list(
                InitiativeStatus.ACTIVE,
                null,
                PageRequest.of(0, 100, Sort.by("expiryDateUtc").ascending()),
                employeePrincipalForDiagnostics(authenticatedUser)
        );

        Set<UUID> adminActiveIds = adminActivePage.getContent().stream()
                .map(InitiativeResponse::id)
                .collect(Collectors.toSet());
        Set<UUID> employeeIds = employeePage.getContent().stream()
                .map(InitiativeResponse::id)
                .collect(Collectors.toSet());

        List<InitiativeVisibilityDiagnosticResponse> diagnostics = initiativeRepository.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt")
        ).stream()
                .map(initiative -> toVisibilityDiagnostic(
                        initiative,
                        now,
                        today,
                        adminActiveIds.contains(initiative.getId()),
                        employeeIds.contains(initiative.getId())
                ))
                .toList();

        return new InitiativeVisibilityDiagnosticsResponse(
                now,
                "GET /api/v1/initiatives?size=100&status=ACTIVE",
                "GET /api/v1/initiatives?size=100&status=ACTIVE",
                diagnostics
        );
    }

    private AuthenticatedUser employeePrincipalForDiagnostics(AuthenticatedUser authenticatedUser) {
        if (!isAdmin(authenticatedUser)) {
            throw new ResourceNotFoundException("Learning initiative was not found");
        }
        User employee = userRepository.findByEmailIgnoreCase("employee@learninghub.local")
                .orElseThrow(() -> new ResourceNotFoundException("Default employee user was not found"));
        return AuthenticatedUser.from(employee);
    }

    private InitiativeVisibilityDiagnosticResponse toVisibilityDiagnostic(
            LearningInitiative initiative,
            Instant now,
            LocalDate today,
            boolean includedInAdminActiveList,
            boolean includedInEmployeeList
    ) {
        ZoneOffset utc = ZoneOffset.UTC;
        return new InitiativeVisibilityDiagnosticResponse(
                initiative.getId(),
                initiative.getTitle(),
                initiative.getStatus(),
                initiative.getStartDateUtc(),
                initiative.getExpiryDateUtc(),
                LocalDate.ofInstant(initiative.getStartDateUtc(), utc),
                LocalDate.ofInstant(initiative.getExpiryDateUtc(), utc),
                today,
                initiative.isVisibleToEmployeesAt(now),
                initiative.employeeExclusionReasonsAt(now),
                includedInAdminActiveList,
                includedInEmployeeList
        );
    }

    private LearningInitiative findInitiativeOrThrow(UUID initiativeId) {
        return initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning initiative was not found"));
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

    private Specification<LearningInitiative> adminSpecification(InitiativeStatus status, String search) {
        return Specification
                .where(hasStatus(status))
                .and(titleContains(search));
    }

    private Specification<LearningInitiative> employeeSpecification(String search, Instant now) {
        ZoneOffset utc = ZoneOffset.UTC;
        LocalDate today = LocalDate.ofInstant(now, utc);
        Instant startOfToday = today.atStartOfDay(utc).toInstant();
        Instant startOfTomorrow = today.plusDays(1).atStartOfDay(utc).toInstant();

        return Specification
                .where(hasStatus(InitiativeStatus.ACTIVE))
                .and(startsOnOrBeforeToday(startOfTomorrow))
                .and(expiresOnOrAfterToday(startOfToday))
                .and(titleContains(search));
    }

    private Specification<LearningInitiative> hasStatus(InitiativeStatus status) {
        return (root, query, criteriaBuilder) -> status == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("status"), status);
    }

    private Specification<LearningInitiative> titleContains(String search) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(search)) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + search.toLowerCase(Locale.ROOT) + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern);
        };
    }

    private Specification<LearningInitiative> startsOnOrBeforeToday(Instant startOfTomorrow) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("startDateUtc"), startOfTomorrow);
    }

    private Specification<LearningInitiative> expiresOnOrAfterToday(Instant startOfToday) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("expiryDateUtc"), startOfToday);
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
            case "id", "title", "status", "startDateUtc", "expiryDateUtc", "createdAt", "updatedAt" ->
                    order.getProperty();
            case "createdAtUtc" -> "createdAt";
            case "updatedAtUtc" -> "updatedAt";
            default -> throw new IllegalArgumentException("Unsupported sort property: " + order.getProperty());
        };

        Sort.Order translated = new Sort.Order(order.getDirection(), property, order.getNullHandling());
        return order.isIgnoreCase() ? translated.ignoreCase() : translated;
    }
}

