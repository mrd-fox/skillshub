package com.simplon_project.skillhub.skillhub.storage.adapter.out.messaging.events;

import com.simplon_project.skillhub.skillhub.storage.domaine.exception.NodeIdValidationException;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

import static com.simplon_project.skillhub.skillhub.storage.domaine.common.Utils.generateUUID;


public class EventId {
    private String id;

    private EventId(String id) {
        if (StringUtils.isBlank(id)) {
            throw new NodeIdValidationException();
        }
        this.id = id;
    }

    public static EventId of(String id) {
        return new EventId(id);
    }

    public static EventId random() {
        return new EventId(generateUUID());
    }

    public String asString() {
        return id;
    }

    public UUID asUUID() {
        return UUID.fromString(id);
    }

}
