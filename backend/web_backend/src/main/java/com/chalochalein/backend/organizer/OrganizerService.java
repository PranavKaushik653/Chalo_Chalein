package com.chalochalein.backend.organizer;

import com.chalochalein.backend.common.AuditService;
import com.chalochalein.backend.user.UserRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizerService {

    private static final Set<String> STATUSES = Set.of(
            "PENDING", "VERIFIED", "REJECTED", "SUSPENDED"
    );
    private final OrganizerProfileRepository profiles;
    private final UserRepository users;
    private final AuditService audit;

    public record ApplyRequest(
            @NotBlank @Size(max = 120) String businessName,
            @Size(max = 50) @Pattern(regexp = "^@?[A-Za-z0-9._]{1,30}$") String instagramHandle,
            @NotBlank @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$") String gstin
    ){}
    public record OrganizerRow(UUID userId, String businessName, String instagramHandle, String gstin,
                               String status, Instant createdAt) {}

    @Transactional
    public void apply(UUID userId, ApplyRequest applyRequest) {
        if(profiles.existsById(userId)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Applicatio already exists");
        OrganizerProfile p = new OrganizerProfile();
        p.setUserId(userId);
        p.setBusinessName(applyRequest.businessName().trim());
        p.setInstagramHandle(applyRequest.instagramHandle());
        p.setGstin(applyRequest.gstin());
        profiles.save(p);
        audit.log(userId, "ORGANIZER_APPLIED", "organizer", userId.toString());
    }

    @Transactional(readOnly = true)
    public List<OrganizerRow> list(String status) {
        String s = status == null ? "PENDING" : status;
        if (!STATUSES.contains(s)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown status");
        return profiles.findByVerificationStatusOrderByCreatedAtAsc(s).stream()
                .map(p -> new OrganizerRow(p.getUserId(), p.getBusinessName(), p.getInstagramHandle(),
                        p.getGstin(), p.getVerificationStatus(), p.getCreatedAt()))
                .toList();
    }


}
