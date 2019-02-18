package com.github.ixtf.persistence.mongo.api;

import com.github.ixtf.persistence.api.AbstractEntityConverter;
import com.github.ixtf.persistence.api.EntityConverter;
import com.github.ixtf.persistence.mongo.Jmongo;
import com.github.ixtf.persistence.reflection.FieldRepresentation;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;
import org.apache.commons.collections4.IterableUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author jzb 2019-02-18
 */
@Slf4j
public class DocumentEntityConverter extends AbstractEntityConverter {

    private DocumentEntityConverter() {
    }

    public static EntityConverter get(Class clazz) {
        return Holder.INSTANCE;
    }

    @Override
    protected Object getColValue(FieldRepresentation fieldRepresentation, Object dbValue) {
        final Document document = (Document) dbValue;
        final String colName = fieldRepresentation.getColName();
        return document.get(colName);
    }

    @Override
    protected Object getSubEntityFieldValue(Object entity, FieldRepresentation fieldRepresentation, Object colValue) {
        final Field nativeField = fieldRepresentation.getNativeField();
        final Class<?> subEntityClass = nativeField.getType();
        if (colValue instanceof String || colValue instanceof ObjectId) {
            final LazyLoader lazyLoader = () -> {
                log.debug("lazyLoaderSubEntity[" + subEntityClass.getSimpleName() + "," + nativeField.getName() + "]");
                return Jmongo.find(subEntityClass, colValue).orElse(null);
            };
            final Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(subEntityClass);
            return enhancer.create(subEntityClass, lazyLoader);
        }
        return toEntity(subEntityClass, colValue);
    }

    @Override
    protected Object getEmbeddableFieldValue(Object entity, FieldRepresentation fieldRepresentation, Object colValue) {
        final Field nativeField = fieldRepresentation.getNativeField();
        return toEntity(nativeField.getType(), colValue);
    }

    @Override
    protected Set getSetFieldValue(Object entity, FieldRepresentation fieldRepresentation, Class actualClass, Iterable iterable) {
        final Object itemValue = IterableUtils.get(iterable, 0);
        if (itemValue instanceof String) {
            final LazyLoader lazyLoader = () -> {
                log.debug("lazyLoaderEntity_Set[" + entity.getClass().getSimpleName() + "." + fieldRepresentation.getNativeField().getName() + "]");
                return Jmongo.list(actualClass, iterable).collect(toSet());
            };
            final Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(Set.class);
            return (Set) enhancer.create(Set.class, lazyLoader);
        }
        return (Set) StreamSupport.stream(iterable.spliterator(), false)
                .map(it -> toEntity(actualClass, it))
                .collect(toSet());
    }

    @Override
    protected List getListFieldValue(Object entity, FieldRepresentation fieldRepresentation, Class actualClass, Iterable iterable) {
        final Object itemValue = IterableUtils.get(iterable, 0);
        if (itemValue instanceof String) {
            final LazyLoader lazyLoader = () -> {
                log.debug("lazyLoaderEntity_List[" + entity.getClass().getSimpleName() + "." + fieldRepresentation.getNativeField().getName() + "]");
                return Jmongo.list(actualClass, iterable).collect(toList());
            };
            final Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(List.class);
            return (List) enhancer.create(List.class, lazyLoader);
        }
        return (List) StreamSupport.stream(iterable.spliterator(), false)
                .map(it -> toEntity(actualClass, it))
                .collect(toSet());
    }

    private static class Holder {
        private static final EntityConverter INSTANCE = new DocumentEntityConverter();
    }
}
