package com.simplon_project.skillhub.skillhub.user.domain.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class User extends Base {

    private Id externalId;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String city;
    private String country;
    private String phoneNumber;
    private String postalCode;
}
