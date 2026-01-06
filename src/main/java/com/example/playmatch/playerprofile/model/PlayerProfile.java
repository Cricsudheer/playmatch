package com.example.playmatch.playerprofile.model;

import com.example.playmatch.auth.model.User;
import com.example.playmatch.api.model.Gender;
import com.example.playmatch.api.model.PrimaryRole;
import com.example.playmatch.api.model.JerseySize;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "player_profile")
public class PlayerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false, length = 80)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "mobile", nullable = false, length = 15)
    private String mobile;

    @Column(name = "city", nullable = false, length = 80)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_role", nullable = false)
    private PrimaryRole primaryRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "jersey_size")
    private JerseySize jerseySize;

    @Column(name = "upi_id", length = 64)
    private String upiId;

    @Column(name = "code_of_conduct_accepted", nullable = false)
    @Builder.Default
    private Boolean codeOfConductAccepted = false;

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
