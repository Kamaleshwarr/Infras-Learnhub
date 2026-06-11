package com.company.learninghub.auth.security;

import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class AuthenticatedUser implements UserDetails {

    private final UUID id;
    private final String employeeId;
    private final String fullName;
    private final String email;
    private final String passwordHash;
    private final boolean active;
    private final boolean mustChangePassword;
    private final Instant passwordChangedAt;
    private final Set<RoleName> roleNames;
    private final Set<GrantedAuthority> authorities;

    private AuthenticatedUser(User user) {
        this.id = user.getId();
        this.employeeId = user.getEmployeeId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.active = user.isActive();
        this.mustChangePassword = user.isMustChangePassword();
        this.passwordChangedAt = user.getPasswordChangedAt();
        this.roleNames = user.roleNames();
        this.authorities = roleNames.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toUnmodifiableSet());
    }

    public static AuthenticatedUser from(User user) {
        return new AuthenticatedUser(user);
    }

    public UUID getId() {
        return id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public Set<RoleName> getRoleNames() {
        return roleNames;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public Instant getPasswordChangedAt() {
        return passwordChangedAt;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return active;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}

