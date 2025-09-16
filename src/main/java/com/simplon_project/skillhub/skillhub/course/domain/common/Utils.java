package com.simplon_project.skillhub.skillhub.course.domain.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)

public class Utils {
    public static String generateUUID() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
