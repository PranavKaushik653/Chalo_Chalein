package com.chalochalein.backend.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private static final int MAX_FAILURES = 5;

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JdbcClient jdbc;
    public record RegisterRequest(
            @NotBlank @Email @Size(max=254) String email,
            @NotBlank @Size(min = 10, max = 72) String password,
            @NotBlank @Size(max = 100) String fullName,
            @Pattern(regexp = "^\\+?[0-9]{10,15}$") String phone
    ) {}

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return users.findByEmail(email.trim())
                .map(AppUserDetails::from)
                .orElseThrow(() -> new UsernameNotFoundException("unknown user"));
    }

    @Transactional
    public UUID register(RegisterRequest r) {
        String email = r.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(email)) throw conflict();
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(r.password()));
        u.setFullName(r.fullName().trim());
        u.setPhone(r.phone());
        u.getRoles().add(Role.TRAVELLER);            // everyone starts as a traveller, nothing else
        try {
            users.saveAndFlush(u);
        } catch (DataIntegrityViolationException e) {
            throw conflict();
        }
        return u.getId();
    }

    private ResponseStatusException conflict() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Unable to register with these details");
    }

    @EventListener
    public void onBadCredentials(AuthenticationFailureBadCredentialsEvent e) {
        jdbc.sql("""
                UPDATE users
                SET failed_logins = failed_logins + 1,
                    locked_until = CASE WHEN failed_logins + 1 >= :max
                                        THEN now() + interval '15 minutes' ELSE locked_until END,
                    updated_at = now()
                WHERE lower(email) = lower(:email)""")
                .param("max", MAX_FAILURES).param("email", String.valueOf(e.getAuthentication().getName())).update();
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent e) {
        if (e.getAuthentication().getPrincipal() instanceof AppUserDetails u) {
            jdbc.sql("UPDATE users SET failed_logins = 0, locked_until = NULL WHERE id = :id")
                    .param("id", u.getId()).update();
        }
    }

}
