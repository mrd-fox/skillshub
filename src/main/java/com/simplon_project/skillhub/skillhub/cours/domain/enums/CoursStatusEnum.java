package com.simplon_project.skillhub.skillhub.cours.domain.enums;

public enum CoursStatusEnum {
    CREATED, //draft
    WAITING_VALIDATION, //modaration step
    VALIDATED, //able to be pulished for purchase
    REJECTED, //can't be published
    PUBLISHED, //ready for pourchases

}
