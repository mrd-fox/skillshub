package com.simplon_project.skillhub.skillhub.course.adapter.common.mapper;

import java.util.IdentityHashMap;
import java.util.Map;

public class CycleAvoidingMappingContext {
    private final Map<Object, Object> knownInstances = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T getMappedInstance(Object source, Class<T> targetType) {
        return (T) knownInstances.get(source);
    }

    public void storeMappedInstance(Object source, Object target) {
        knownInstances.put(source, target);
    }
}
