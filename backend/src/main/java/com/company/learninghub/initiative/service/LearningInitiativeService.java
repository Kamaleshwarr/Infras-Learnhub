package com.company.learninghub.initiative.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.initiative.domain.InitiativeStatus;
import com.company.learninghub.initiative.domain.LearningInitiative;
import com.company.learninghub.initiative.dto.CreateInitiativeRequest;
import com.company.learninghub.initiative.dto.InitiativeResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

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
        Page<LearningInitiative> initiatives = isAdmin(authenticatedUser)
                ? initiativeRepository.findForAdmin(status, normalizedSearch, repositoryPageable)
                : initiativeRepository.findActiveForEmployee(normalizedSearch, Instant.now(clock), repositoryPageable);

        return initiatives.map(initiativeMapper::toResponse);
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

