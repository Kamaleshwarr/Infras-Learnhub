package com.company.learninghub.projectknowledge.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.projectknowledge.domain.Project;
import com.company.learninghub.projectknowledge.domain.ProjectExternalContact;
import com.company.learninghub.projectknowledge.domain.ProjectFunctionalRole;
import com.company.learninghub.projectknowledge.domain.ProjectMember;
import com.company.learninghub.projectknowledge.domain.ProjectRole;
import com.company.learninghub.projectknowledge.dto.ProjectExternalContactRequest;
import com.company.learninghub.projectknowledge.dto.ProjectExternalContactResponse;
import com.company.learninghub.projectknowledge.dto.ProjectMemberCandidateResponse;
import com.company.learninghub.projectknowledge.dto.ProjectMemberRequest;
import com.company.learninghub.projectknowledge.dto.ProjectMemberResponse;
import com.company.learninghub.projectknowledge.mapper.ProjectKnowledgeMapper;
import com.company.learninghub.projectknowledge.repository.ProjectExternalContactRepository;
import com.company.learninghub.projectknowledge.repository.ProjectMemberRepository;
import com.company.learninghub.projectknowledge.util.ProjectNavigationUrlValidator;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ProjectTeamService {

    private final ProjectMemberRepository memberRepository;
    private final ProjectExternalContactRepository externalContactRepository;
    private final ProjectScopeAuthorization authorization;
    private final ProjectKnowledgeMapper mapper;
    private final UserRepository userRepository;

    public ProjectTeamService(
            ProjectMemberRepository memberRepository,
            ProjectExternalContactRepository externalContactRepository,
            ProjectScopeAuthorization authorization,
            ProjectKnowledgeMapper mapper,
            UserRepository userRepository
    ) {
        this.memberRepository = memberRepository;
        this.externalContactRepository = externalContactRepository;
        this.authorization = authorization;
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> listMembers(UUID projectId, AuthenticatedUser principal) {
        authorization.requireReadableProject(projectId, principal);
        return memberRepository.findByProjectIdOrdered(projectId).stream()
                .map(mapper::toMemberResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public ProjectMemberResponse addOrUpdateMember(UUID projectId, ProjectMemberRequest request, AuthenticatedUser principal) {
        Project project = authorization.requireOwnerProject(projectId, principal);
        User user = findUser(request.userId());
        ProjectMember member = memberRepository.findByProjectIdAndUserId(projectId, user.getId())
                .orElseGet(() -> new ProjectMember(
                        project,
                        user,
                        request.projectRole(),
                        request.functionalRole(),
                        normalizeOptional(request.responsibility()),
                        Boolean.TRUE.equals(request.primaryContact()),
                        normalizeDisplayOrder(request.displayOrder())
                ));
        member.updateAssignment(
                request.projectRole(),
                request.functionalRole(),
                normalizeOptional(request.responsibility()),
                request.primaryContact() == null ? member.isPrimaryContact() : request.primaryContact(),
                request.displayOrder() == null ? member.getDisplayOrder() : normalizeDisplayOrder(request.displayOrder())
        );
        return mapper.toMemberResponse(memberRepository.save(member));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public void removeMember(UUID projectId, UUID userId, AuthenticatedUser principal) {
        authorization.requireOwnerProject(projectId, principal);
        ProjectMember member = memberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project member was not found"));
        if (member.getProjectRole() == ProjectRole.OWNER
                && memberRepository.countByProjectIdAndProjectRole(projectId, ProjectRole.OWNER) <= 1) {
            throw new IllegalArgumentException("Project must retain at least one owner");
        }
        memberRepository.delete(member);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public List<ProjectMemberCandidateResponse> searchMemberCandidates(
            UUID projectId,
            String search,
            AuthenticatedUser principal
    ) {
        authorization.requireOwnerProject(projectId, principal);
        String normalized = normalizeSearch(search);
        if (normalized == null) {
            return List.of();
        }
        String pattern = "%" + normalized.toLowerCase(Locale.ROOT) + "%";
        Specification<User> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("fullName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("employeeId")), pattern)
            ));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return userRepository.findAll(specification, PageRequest.of(0, 10)).stream()
                .map(mapper::toMemberCandidateResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public List<ProjectExternalContactResponse> listExternalContacts(UUID projectId, AuthenticatedUser principal) {
        authorization.requireReadableProject(projectId, principal);
        return externalContactRepository.findByProjectIdAndActiveTrueOrderByDisplayOrderAscNameAsc(projectId).stream()
                .map(mapper::toExternalContactResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public ProjectExternalContactResponse createExternalContact(
            UUID projectId,
            ProjectExternalContactRequest request,
            AuthenticatedUser principal
    ) {
        Project project = authorization.requireOwnerProject(projectId, principal);
        ProjectExternalContact contact = externalContactRepository.save(new ProjectExternalContact(
                project,
                normalizeRequired(request.name(), "Contact name is required"),
                request.contactType(),
                normalizeOptional(request.roleTitle()),
                normalizeOptional(request.organization()),
                normalizeOptional(request.email()),
                normalizeOptional(request.phone()),
                normalizeContactUrl(request.contactUrl()),
                normalizeOptional(request.notes()),
                request.primaryContact() == null || request.primaryContact(),
                normalizeDisplayOrder(request.displayOrder()),
                findUser(principal.getId())
        ));
        return mapper.toExternalContactResponse(contact);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public ProjectExternalContactResponse updateExternalContact(
            UUID projectId,
            UUID contactId,
            ProjectExternalContactRequest request,
            AuthenticatedUser principal
    ) {
        authorization.requireOwnerProject(projectId, principal);
        ProjectExternalContact contact = findExternalContact(projectId, contactId);
        contact.updateDetails(
                normalizeRequired(request.name(), "Contact name is required"),
                request.contactType(),
                normalizeOptional(request.roleTitle()),
                normalizeOptional(request.organization()),
                normalizeOptional(request.email()),
                normalizeOptional(request.phone()),
                normalizeContactUrl(request.contactUrl()),
                normalizeOptional(request.notes()),
                request.primaryContact() == null || request.primaryContact(),
                request.displayOrder() == null ? contact.getDisplayOrder() : normalizeDisplayOrder(request.displayOrder()),
                request.active() == null || request.active()
        );
        return mapper.toExternalContactResponse(contact);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public void deleteExternalContact(UUID projectId, UUID contactId, AuthenticatedUser principal) {
        authorization.requireOwnerProject(projectId, principal);
        ProjectExternalContact contact = findExternalContact(projectId, contactId);
        externalContactRepository.delete(contact);
    }

    public int countPrimaryContacts(UUID projectId) {
        return (int) (memberRepository.countByProjectIdAndPrimaryContactTrue(projectId)
                + externalContactRepository.countByProjectIdAndPrimaryContactTrueAndActiveTrue(projectId));
    }

    private ProjectExternalContact findExternalContact(UUID projectId, UUID contactId) {
        ProjectExternalContact contact = externalContactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("External contact was not found"));
        if (!contact.getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException("External contact was not found");
        }
        return contact;
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User was not found"));
    }

    private String normalizeRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeSearch(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private int normalizeDisplayOrder(Integer displayOrder) {
        return displayOrder == null ? 0 : Math.max(0, displayOrder);
    }

    private String normalizeContactUrl(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return ProjectNavigationUrlValidator.normalizeNavigationUrl(value, "Contact URL");
    }
}
