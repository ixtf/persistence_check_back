package com.github.ixtf.persistence.api;

import com.github.ixtf.persistence.reflection.ClassRepresentation;
import com.github.ixtf.persistence.reflection.FieldRepresentation;
import lombok.SneakyThrows;

import javax.persistence.AttributeConverter;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

/**
 * @author jzb 2019-02-18
 */
public abstract class AbstractEntityConverter implements EntityConverter {
    @SneakyThrows
    @Override
    public <T> T toEntity(ClassRepresentation<T> classRepresentation, Object dbData) {
        final T entity = classRepresentation.getConstructor().newInstance();
        classRepresentation.getFields().parallelStream().forEach(it -> {
            final Object colValue = getColValue(it, dbData);
            fillField(entity, it, colValue);
        });
        return entity;
    }

    protected abstract Object getColValue(FieldRepresentation fieldRepresentation, Object dbValue);

    @SneakyThrows
    private void fillField(Object entity, FieldRepresentation fieldRepresentation, Object colValue) {
        final Field nativeField = fieldRepresentation.getNativeField();
        final Class<?> nativeType = nativeField.getType();
        if (colValue == null) {
            setFieldValue(entity, nativeField, nativeType, colValue);
            return;
        }

        final AttributeConverter attributeConverter = fieldRepresentation.getConverter()
                .map(it -> {
                    try {
                        return CONVERTER_CACHE.get(it);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElse(null);
        if (attributeConverter != null) {
            final Object value = attributeConverter.convertToEntityAttribute(colValue);
            setFieldValue(entity, nativeField, nativeType, value);
            return;
        }

        switch (fieldRepresentation.getType()) {
            case MAP: {
                // todo 后续支持
                throw new UnsupportedOperationException();
            }
            case SUBENTITY: {
                final Object o = getSubEntityFieldValue(entity, fieldRepresentation, colValue);
                nativeField.set(entity, o);
                return;
            }
            case EMBEDDABLE: {
                final Object o = getEmbeddableFieldValue(entity, fieldRepresentation, colValue);
                nativeField.set(entity, o);
                return;
            }
            case COLLECTION: {
                final ParameterizedType parameterizedType = (ParameterizedType) nativeField.getGenericType();
                final Type rawType = parameterizedType.getRawType();
                final Class actualClass = (Class) parameterizedType.getActualTypeArguments()[0];
                final Iterable iterable = (Iterable) colValue;
                final Object o;
                if (Set.class.equals(rawType)) {
                    o = getSetFieldValue(entity, fieldRepresentation, actualClass, iterable);
                } else {
                    o = getListFieldValue(entity, fieldRepresentation, actualClass, iterable);
                }
                nativeField.set(entity, o);
                return;
            }
            default: {
                final ValueReaderDecorator valueReader = ValueReaderDecorator.getInstance();
                final Object value = valueReader.read(nativeType, colValue);
                setFieldValue(entity, nativeField, nativeType, value);
                return;
            }
        }
    }

    protected abstract Object getSubEntityFieldValue(Object entity, FieldRepresentation fieldRepresentation, Object colValue);

    protected abstract Object getEmbeddableFieldValue(Object entity, FieldRepresentation fieldRepresentation, Object colValue);

    protected abstract Set getSetFieldValue(Object entity, FieldRepresentation fieldRepresentation, Class actualClass, Iterable colValue);

    protected abstract List getListFieldValue(Object entity, FieldRepresentation fieldRepresentation, Class actualClass, Iterable colValue);

    protected void setFieldValue(Object entity, Field nativeField, Class<?> nativeType, Object value) throws IllegalAccessException {
        if (value == null) {
            if (!nativeType.isPrimitive()) {
                nativeField.set(entity, value);
            }
            return;
        }

        if (byte.class.equals(nativeType)) {
            nativeField.set(entity, ((Number) value).byteValue());
        } else if (short.class.equals(nativeType)) {
            nativeField.set(entity, ((Number) value).shortValue());
        } else if (int.class.equals(nativeType)) {
            nativeField.set(entity, ((Number) value).intValue());
        } else if (long.class.equals(nativeType)) {
            nativeField.set(entity, ((Number) value).longValue());
        } else if (float.class.equals(nativeType)) {
            nativeField.set(entity, ((Number) value).floatValue());
        } else if (double.class.equals(nativeType)) {
            nativeField.set(entity, ((Number) value).doubleValue());
        } else if (boolean.class.equals(nativeType)) {
            nativeField.set(entity, ((Boolean) value).booleanValue());
        } else {
            nativeField.set(entity, value);
        }
    }
}
