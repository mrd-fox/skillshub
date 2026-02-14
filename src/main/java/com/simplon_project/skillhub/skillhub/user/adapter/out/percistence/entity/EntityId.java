package com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class EntityId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID value;

    protected EntityId() {
    }

    private EntityId(UUID value) {
        this.value = Objects.requireNonNull(value, "id cannot be null");
    }

    public static EntityId of(UUID id) {
        return new EntityId(id);
    }

    public static EntityId fromString(String id) {
        return new EntityId(UUID.fromString(id));
    }

    public static EntityId random() {
        return new EntityId(UUID.randomUUID());
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityId))
            return false;
        EntityId entityId = (EntityId) o;
        return Objects.equals(value, entityId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value == null ? "null" : value.toString();
    }
}
