package com.chalochalein.backend.user;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestHeader;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class AppUserDetails implements UserDetails, CredentialsContainer {

    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String email;
    private final String fullName;
    private String passwordHash;
    private final boolean enabled;
    private final Instant lockedUntil;
    private final ArrayList<String> roleNames = new ArrayList<>();

    private AppUserDetails (User u){
        this.id = u.getId();
        this.email = u.getEmail();
        this.fullName = u.getFullName();
        this.passwordHash = u.getPasswordHash();
        this.enabled="ACTIVE".equals(u.getStatus());
        this.lockedUntil=u.getLockedUntil();
        u.getRoles().forEach(r -> roleNames.add(r.name()));
    }

    public static AppUserDetails from (User u){
        return new AppUserDetails(u);}

        public UUID getId(){return id;}
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public List<String> getRoleNames() { return roleNames; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> list = new ArrayList<>();
        roleNames.forEach(r -> list.add(new SimpleGrantedAuthority("ROLE_" + r)));
        return list;
    }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return lockedUntil == null || lockedUntil.isBefore(Instant.now()); }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }
    @Override public void eraseCredentials() { this.passwordHash = null; }
}



