package com.simplon_project.skillhub.skillhub.storage.domaine.model;

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
public class MediaId {
    private String id;

    private MediaId(String id) {
        if (StringUtils.isBlank(id)) {
            throw new NodeIdValidationException();
        }
        this.id = id;
    }

    public static MediaId of(String id) {
        return new MediaId(id);
    }

    public static MediaId random() {
        return new MediaId(generateUUID());
    }

    public String asString() {
        return id;
    }

    public UUID asUUID() {
        return UUID.fromString(id);
    }

}
