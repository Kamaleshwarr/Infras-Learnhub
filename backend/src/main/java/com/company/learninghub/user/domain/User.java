package com.company.learninghub.user.domain;

import com.company.learninghub.common.domain.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "employee_id", nullable = false, unique = true, length = 64)
    private String employeeId;

    @Column(name = "email", nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword = false;

    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;

    @Column(name = "identity_provider", length = 100)
    private String identityProvider;

    @Column(name = "external_subject", length = 255)
    private String externalSubject;

    @Column(name = "avatar_storage_provider", length = 20)
    private String avatarStorageProvider;

    @Column(name = "avatar_storage_key", length = 500)
    private String avatarStorageKey;

    @Column(name = "avatar_content_type", length = 100)
    private String avatarContentType;

    @Column(name = "avatar_original_filename", length = 255)
    private String avatarOriginalFilename;

    @Column(name = "avatar_file_size_bytes")
    private Long avatarFileSizeBytes;

    @Column(name = "avatar_updated_at")
    private Instant avatarUpdatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<UserRole> userRoles = new HashSet<>();

    protected User() {
    }

    public User(String employeeId, String email, String fullName, String passwordHash) {
        this.employeeId = employeeId;
        this.email = email;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
    }

    public UUID getId() {
        return id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public Instant getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public void setPasswordChangedAt(Instant passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
    }

    public String getIdentityProvider() {
        return identityProvider;
    }

    public void setIdentityProvider(String identityProvider) {
        this.identityProvider = identityProvider;
    }

    public String getExternalSubject() {
        return externalSubject;
    }

    public void setExternalSubject(String externalSubject) {
        this.externalSubject = externalSubject;
    }

    public String getAvatarStorageProvider() {
        return avatarStorageProvider;
    }

    public void setAvatarStorageProvider(String avatarStorageProvider) {
        this.avatarStorageProvider = avatarStorageProvider;
    }

    public String getAvatarStorageKey() {
        return avatarStorageKey;
    }

    public void setAvatarStorageKey(String avatarStorageKey) {
        this.avatarStorageKey = avatarStorageKey;
    }

    public String getAvatarContentType() {
        return avatarContentType;
    }

    public void setAvatarContentType(String avatarContentType) {
        this.avatarContentType = avatarContentType;
    }

    public String getAvatarOriginalFilename() {
        return avatarOriginalFilename;
    }

    public void setAvatarOriginalFilename(String avatarOriginalFilename) {
        this.avatarOriginalFilename = avatarOriginalFilename;
    }

    public Long getAvatarFileSizeBytes() {
        return avatarFileSizeBytes;
    }

    public void setAvatarFileSizeBytes(Long avatarFileSizeBytes) {
        this.avatarFileSizeBytes = avatarFileSizeBytes;
    }

    public Instant getAvatarUpdatedAt() {
        return avatarUpdatedAt;
    }

    public void setAvatarUpdatedAt(Instant avatarUpdatedAt) {
        this.avatarUpdatedAt = avatarUpdatedAt;
    }

    public Set<UserRole> getUserRoles() {
        return Collections.unmodifiableSet(userRoles);
    }

    public void assignRole(Role role) {
        UserRole userRole = new UserRole(this, role);
        userRoles.add(userRole);
    }

    public void replaceRole(Role role) {
        if (hasRoleName(role.getName()) && userRoles.size() == 1) {
            return;
        }
        userRoles.clear();
        assignRole(role);
    }

    public Set<RoleName> roleNames() {
        Set<RoleName> roles = new HashSet<>();
        for (UserRole userRole : userRoles) {
            roles.add(userRole.getRole().getName());
        }
        return Collections.unmodifiableSet(roles);
    }

    public boolean hasRoleName(RoleName roleName) {
        return roleNames().contains(roleName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User user)) {
            return false;
        }
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

