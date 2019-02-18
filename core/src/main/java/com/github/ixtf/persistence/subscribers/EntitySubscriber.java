package com.github.ixtf.persistence.subscribers;

import com.github.ixtf.persistence.api.EntityConverter;
import com.github.ixtf.persistence.reflection.ClassRepresentation;
import com.github.ixtf.persistence.reflection.ClassRepresentations;
import com.google.common.collect.ImmutableList;
import lombok.SneakyThrows;

import java.util.stream.Stream;

/**
 * @author jzb 2019-02-18
 */
public class EntitySubscriber<T, DB> extends OperationSubscriber<DB> {
    private final ClassRepresentation<T> classRepresentation;
    private final EntityConverter entityConverter;
    private final ImmutableList.Builder<T> builder = ImmutableList.builder();

    public EntitySubscriber(Class<T> clazz, EntityConverter entityConverter) {
        this.classRepresentation = ClassRepresentations.create(clazz);
        this.entityConverter = entityConverter;
    }

    @SneakyThrows
    public Stream<T> entities() {
        await();
        final Throwable error = getError();
        if (error != null) {
            throw error;
        }
        return builder.build().stream();
    }

    @SneakyThrows
    @Override
    public void onNext(DB t) {
        super.onNext(t);
        final T entity = entityConverter.toEntity(classRepresentation, t);
        builder.add(entity);
    }
}
