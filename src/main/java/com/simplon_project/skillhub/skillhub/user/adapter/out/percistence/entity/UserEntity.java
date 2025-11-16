package com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "user_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEntity extends AbstractBaseEntity {

    @Getter
    @EmbeddedId
    private EntityId id;

    @Column(name = "external_id", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID externalId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column
    private String address;

    @Column(name = "postal_code")
    private String postalCode;

    @Column
    private String city;

    @Column
    private String country;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(nullable = false)
    private boolean active;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<RoleEntity> roles = new HashSet<>();

}
