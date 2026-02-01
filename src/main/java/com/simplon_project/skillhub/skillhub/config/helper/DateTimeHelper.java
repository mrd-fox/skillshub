package com.simplon_project.skillhub.skillhub.config.helper;

import org.springframework.modulith.NamedInterface;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@NamedInterface
public final class DateTimeHelper {
    private DateTimeHelper() {
    }

    public static LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    /**
     * Convertit un LocalDateTime en Instant (dans le fuseau par défaut).
     */
    public static Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}
