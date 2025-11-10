package com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;


@Entity
@Table(name = "user_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEntity extends AbstractBaseEntity {

    @EmbeddedId
    private EntityId id;

    @Column(name = "external_id", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID externalId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String address; // Simplified field for MVP

    @Column(name = "postal_code")
    private String postalCode;

    @Column
    private String city;

    @Column
    private String country;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Override
    public EntityId getId() {
        return id;
    }
}
