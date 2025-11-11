package com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity;

import com.simplon_project.skillhub.skillhub.user.domain.enums.RolesEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class RoleEntity {

    @EmbeddedId
    private EntityId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private RolesEnum name;
}
