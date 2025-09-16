package com.simplon_project.skillhub.skillhub.course.domain.model;

import com.simplon_project.skillhub.skillhub.course.domain.exception.NodeIdValidationException;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

import static com.simplon_project.skillhub.skillhub.course.domain.common.Utils.generateUUID;

@EqualsAndHashCode
@NoArgsConstructor
@ToString
public class Id {

    private String id;

    private Id(String id) {
        if (StringUtils.isBlank(id)) {
            throw new NodeIdValidationException();
        }
        this.id = id;
    }

    public static Id of(String id) {
        return new Id(id);
    }

    public static Id random() {
        return new Id(generateUUID());
    }

    public String asString() {
        return id;
    }

    public UUID asUUID() {
        return UUID.fromString(id);
    }
}
