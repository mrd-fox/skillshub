package com.simplon_project.skillhub.skillhub.course.domain.enums;

public enum VideoStatusEnum {
    UPLOADED, //optional
    PROCESSING,//after upload finished
    IN_REVIEW, //admin review
    READY, //redy to publish
    PUBLISHED, //ready to sales
    REJECTED, //admin rejected
    FAILED, //techical issue
    PENDING, //after init
    EXPIRED
}
