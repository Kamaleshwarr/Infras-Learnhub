package com.company.learninghub.initiative.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.BusinessConflictException;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.initiative.domain.InitiativeStatus;
import com.company.learninghub.initiative.domain.LearningInitiative;
import com.company.learninghub.initiative.dto.CreateInitiativeRequest;
import com.company.learninghub.initiative.dto.InitiativeResponse;
import com.company.learninghub.initiative.dto.ReactivateInitiativeRequest;
import com.company.learninghub.initiative.dto.UpdateInitiativeRequest;
import com.company.learninghub.initiative.mapper.LearningInitiativeMapper;
import com.company.learninghub.initiative.repository.LearningInitiativeRepository;
import com.company.learninghub.submission.repository.CertificateSubmissionRepository;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Locale;
import java.util.UUID;

@Service
public class LearningInitiativeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LearningInitiativeService.class);

    static final String INITIATIVE_DELETE_BLOCKED_MESSAGE =
            "This initiative cannot be deleted because certificate submissions already exist.";

    private static final int TITLE_MAX_LENGTH = 100;
    private static final int DESCRIPTION_MAX_LENGTH = 2000;
    private static final int REWARD_MAX_LENGTH = 500;

    private final LearningInitiativeRepository initiativeRepository;
    private final CertificateSubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final LearningInitiativeMapper initiativeMapper;
    private final Clock clock;

    @Autowired
    public LearningInitiativeService(
            LearningInitiativeRepository initiativeRepository,
            CertificateSubmissionRepository submissionRepository,
            UserRepository userRepository,
            LearningInitiativeMapper initiativeMapper
    ) {
        this(initiativeRepository, submissionRepository, userRepository, initiativeMapper, Clock.systemUTC());
    }

    LearningInitiativeService(
            LearningInitiativeRepository initiativeRepository,
            CertificateSubmissionRepository submissionRepository,
            UserRepository userRepository,
            LearningInitiativeMapper initiativeMapper,
            Clock clock
    ) {
        this.initiativeRepository = initiativeRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
        this.initiativeMapper = initiativeMapper;
        this.clock = clock;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public InitiativeResponse create(CreateInitiativeRequest request, AuthenticatedUser authenticatedUser) {
        User createdBy = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));

        validateMetadata(
                request.title(),
                request.description(),
                request.rewardDescription(),
                request.startDateUtc(),
                request.expiryDateUtc(),
                true,
                null
        );

        LearningInitiative initiative = new LearningInitiative(
                request.title(),
                request.description(),
                request.rewardDescription(),
                request.startDateUtc(),
                request.expiryDateUtc(),
                InitiativeStatus.DRAFT,
                createdBy
        );

        return initiativeMapper.toResponse(initiativeRepository.save(initiative));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public InitiativeResponse update(UUID initiativeId, UpdateInitiativeRequest request) {
        LearningInitiative initiative = findInitiativeOrThrow(initiativeId);
        boolean startDateChanged = isStartDateChanged(initiative.getStartDateUtc(), request.startDateUtc());
        validateMetadata(
                request.title(),
                request.description(),
                request.rewardDescription(),
                request.startDateUtc(),
                request.expiryDateUtc(),
                startDateChanged,
                initiative.getStartDateUtc()
        );
        initiative.updateDetails(
                request.title(),
                request.description(),
                request.rewardDescription(),
                request.startDateUtc(),
                request.expiryDateUtc(),
                initiative.getStatus()
        );
        return initiativeMapper.toResponse(initiative);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public InitiativeResponse publish(UUID initiativeId) {
        LearningInitiative initiative = findInitiativeOrThrow(initiativeId);
        assertTransition(initiative.getStatus(), InitiativeStatus.ACTIVE);
        validateMetadata(
                initiative.getTitle(),
                initiative.getDescription(),
                initiative.getRewardDescription(),
                initiative.getStartDateUtc(),
                initiative.getExpiryDateUtc(),
                true,
                null
        );
        initiative.updateDetails(
                initiative.getTitle(),
                initiative.getDescription(),
                initiative.getRewardDescription(),
                initiative.getStartDateUtc(),
                initiative.getExpiryDateUtc(),
                InitiativeStatus.ACTIVE
        );
        return initiativeMapper.toResponse(initiative);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public InitiativeResponse returnToDraft(UUID initiativeId) {
        LearningInitiative initiative = findInitiativeOrThrow(initiativeId);
        assertTransition(initiative.getStatus(), InitiativeStatus.DRAFT);
        initiative.updateDetails(
                initiative.getTitle(),
                initiative.getDescription(),
                initiative.getRewardDescription(),
                initiative.getStartDateUtc(),
                initiative.getExpiryDateUtc(),
                InitiativeStatus.DRAFT
        );
        return initiativeMapper.toResponse(initiative);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public InitiativeResponse markExpired(UUID initiativeId) {
        LearningInitiative initiative = findInitiativeOrThrow(initiativeId);
        assertTransition(initiative.getStatus(), InitiativeStatus.EXPIRED);
        NormalizedInitiativeDates dates = normalizeExpiredDates(
                initiative.getStartDateUtc(),
                initiative.getExpiryDateUtc()
        );
        initiative.updateDetails(
                initiative.getTitle(),
                initiative.getDescription(),
                initiative.getRewardDescription(),
                dates.startDateUtc(),
                dates.expiryDateUtc(),
                InitiativeStatus.EXPIRED
        );
        return initiativeMapper.toResponse(initiative);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public InitiativeResponse reactivate(UUID initiativeId, ReactivateInitiativeRequest request) {
        LearningInitiative initiative = findInitiativeOrThrow(initiativeId);
        assertTransition(initiative.getStatus(), InitiativeStatus.ACTIVE);
        validateMetadata(
                initiative.getTitle(),
                initiative.getDescription(),
                initiative.getRewardDescription(),
                initiative.getStartDateUtc(),
                request.expiryDateUtc(),
                false,
                initiative.getStartDateUtc()
        );
        validateExpiryOnOrAfterToday(request.expiryDateUtc());
        if (request.expiryDateUtc().isBefore(initiative.getStartDateUtc())) {
            throw new IllegalArgumentException("expiryDateUtc must be on or after startDateUtc");
        }
        initiative.updateDetails(
                initiative.getTitle(),
                initiative.getDescription(),
                initiative.getRewardDescription(),
                initiative.getStartDateUtc(),
                request.expiryDateUtc(),
                InitiativeStatus.ACTIVE
        );
        return initiativeMapper.toResponse(initiative);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(UUID initiativeId, AuthenticatedUser authenticatedUser) {
        LearningInitiative initiative = findInitiativeOrThrow(initiativeId);
        assertDeletable(initiativeId);
        initiativeRepository.delete(initiative);
        LOGGER.info(
                "Deleted learning initiative id={} title=\"{}\" status={} deletedByUserId={} deletedByEmail={}",
                initiative.getId(),
                initiative.getTitle(),
                initiative.getStatus(),
                authenticatedUser.getId(),
                authenticatedUser.getEmail()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public long countSubmissions(UUID initiativeId) {
        findInitiativeOrThrow(initiativeId);
        return submissionRepository.countByInitiativeId(initiativeId);
    }

    private void assertDeletable(UUID initiativeId) {
        if (submissionRepository.countByInitiativeId(initiativeId) > 0) {
            throw new BusinessConflictException(INITIATIVE_DELETE_BLOCKED_MESSAGE);
        }
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

    private LearningInitiative findInitiativeOrThrow(UUID initiativeId) {
        return initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning initiative was not found"));
    }

    private void assertTransition(InitiativeStatus currentStatus, InitiativeStatus targetStatus) {
        boolean allowed = switch (currentStatus) {
            case DRAFT -> targetStatus == InitiativeStatus.ACTIVE;
            case ACTIVE -> targetStatus == InitiativeStatus.DRAFT || targetStatus == InitiativeStatus.EXPIRED;
            case EXPIRED -> targetStatus == InitiativeStatus.ACTIVE;
        };
        if (!allowed) {
            throw new IllegalArgumentException(
                    "Cannot transition initiative from " + currentStatus + " to " + targetStatus
            );
        }
    }

    private void validateMetadata(
            String title,
            String description,
            String rewardDescription,
            Instant startDateUtc,
            Instant expiryDateUtc,
            boolean validateStartAgainstToday,
            Instant storedStartDateUtc
    ) {
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("title is required");
        }
        if (title.trim().length() > TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException("title must be 100 characters or fewer");
        }
        if (!StringUtils.hasText(description)) {
            throw new IllegalArgumentException("description is required");
        }
        if (description.trim().length() > DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException("description must be 2000 characters or fewer");
        }
        if (rewardDescription != null && rewardDescription.length() > REWARD_MAX_LENGTH) {
            throw new IllegalArgumentException("rewardDescription must be 500 characters or fewer");
        }
        if (startDateUtc == null) {
            throw new IllegalArgumentException("startDateUtc is required");
        }
        if (expiryDateUtc == null) {
            throw new IllegalArgumentException("expiryDateUtc is required");
        }
        if (expiryDateUtc.isBefore(startDateUtc)) {
            throw new IllegalArgumentException("expiryDateUtc must be on or after startDateUtc");
        }
        if (validateStartAgainstToday) {
            validateStartDateOnOrAfterToday(startDateUtc);
        } else if (storedStartDateUtc != null && isStartDateChanged(storedStartDateUtc, startDateUtc)) {
            validateStartDateOnOrAfterToday(startDateUtc);
        }
    }

    private NormalizedInitiativeDates normalizeExpiredDates(Instant startDateUtc, Instant expiryDateUtc) {
        Instant normalizedStart = startDateUtc;
        Instant normalizedExpiry = startOfTodayUtc();
        if (normalizedStart.isAfter(normalizedExpiry)) {
            normalizedStart = normalizedExpiry;
        }
        if (normalizedExpiry.isBefore(normalizedStart)) {
            throw new IllegalArgumentException("expiryDateUtc must be on or after startDateUtc");
        }
        return new NormalizedInitiativeDates(normalizedStart, normalizedExpiry);
    }

    private boolean isStartDateChanged(Instant storedStartDateUtc, Instant requestedStartDateUtc) {
        ZoneOffset utc = ZoneOffset.UTC;
        LocalDate storedDate = LocalDate.ofInstant(storedStartDateUtc, utc);
        LocalDate requestedDate = LocalDate.ofInstant(requestedStartDateUtc, utc);
        return !storedDate.equals(requestedDate);
    }

    private void validateStartDateOnOrAfterToday(Instant startDateUtc) {
        ZoneOffset utc = ZoneOffset.UTC;
        LocalDate today = LocalDate.ofInstant(clock.instant(), utc);
        LocalDate startDate = LocalDate.ofInstant(startDateUtc, utc);
        if (startDate.isBefore(today)) {
            throw new IllegalArgumentException("startDateUtc cannot be earlier than today (UTC)");
        }
    }

    private void validateExpiryOnOrAfterToday(Instant expiryDateUtc) {
        ZoneOffset utc = ZoneOffset.UTC;
        LocalDate today = LocalDate.ofInstant(clock.instant(), utc);
        LocalDate expiryDate = LocalDate.ofInstant(expiryDateUtc, utc);
        if (expiryDate.isBefore(today)) {
            throw new IllegalArgumentException("expiryDateUtc cannot be earlier than today (UTC)");
        }
    }

    private Instant startOfTodayUtc() {
        return LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
    }

    private record NormalizedInitiativeDates(Instant startDateUtc, Instant expiryDateUtc) {
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
