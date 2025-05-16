package com.simplon_project.skillhub.skillhub.course.adapter.common.mapper;

import org.mapstruct.Context;

import java.util.Collection;
import java.util.List;

public interface GenericDtoMapper<D, T> {
    T toDto(D domain, @Context CycleAvoidingMappingContext cycleAvoidingMappingContext);

    D toDomain(T dto, @Context CycleAvoidingMappingContext cycleAvoidingMappingContext);

    List<T> toDto(Collection<D> domain, @Context CycleAvoidingMappingContext cycleAvoidingMappingContext);

    List<D> toDomain(Collection<T> dto, @Context CycleAvoidingMappingContext cycleAvoidingMappingContext);
}
