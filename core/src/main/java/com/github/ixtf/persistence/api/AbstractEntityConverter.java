package com.github.ixtf.persistence.api;

import com.github.ixtf.persistence.reflection.ClassRepresentation;
import com.github.ixtf.persistence.reflection.ClassRepresentations;
import com.github.ixtf.persistence.reflection.FieldRepresentation;
import com.github.ixtf.persistence.reflection.GenericFieldRepresentation;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.PropertyUtils;

import javax.persistence.AttributeConverter;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.StreamSupport;

/**
 * @author jzb 2019-02-18
 */
public abstract class AbstractEntityConverter implements EntityConverter {
    @SneakyThrows
    @Override
    public <T> T toEntity(ClassRepresentation<T> classRepresentation, Object dbData) {
        final T entity = classRepresentation.getConstructor().newInstance();
        classRepresentation.getFields().stream().forEach(it -> {
            final Object colValue = getColValue(it, dbData);
            fillEntityAttribute(entity, it, colValue);
        });
        return entity;
    }

    @SneakyThrows
    private void fillEntityAttribute(Object entity, FieldRepresentation fieldRepresentation, Object colValue) {
        final String fieldName = fieldRepresentation.getFieldName();
        final Class<?> nativeType = fieldRepresentation.getFieldType();
        if (colValue == null) {
            setEntityAttributeValue(entity, fieldName, nativeType, colValue);
            return;
        }

        final AttributeConverter attributeConverter = EntityConverter.attributeConverter(fieldRepresentation);
        if (attributeConverter != null) {
            final Object value = attributeConverter.convertToEntityAttribute(colValue);
            setEntityAttributeValue(entity, fieldName, nativeType, value);
            return;
        }

        switch (fieldRepresentation.getType()) {
            case MAP: {
                // todo 后续支持
                throw new UnsupportedOperationException();
            }
            case SUBENTITY: {
                final Object o = convertToEntityAttribute_SubEntity(entity, fieldRepresentation, colValue);
                PropertyUtils.setProperty(entity, fieldName, o);
                return;
            }
            case EMBEDDABLE: {
                final Object o = convertToEntityAttribute_Embeddable(entity, fieldRepresentation, colValue);
                PropertyUtils.setProperty(entity, fieldName, o);
                return;
            }
            case COLLECTION: {
                final GenericFieldRepresentation genericFieldRepresentation = (GenericFieldRepresentation) fieldRepresentation;
                final Class elementType = genericFieldRepresentation.getElementType();
                final Collector collector = genericFieldRepresentation.getCollector();
                final Iterable iterable = (Iterable) colValue;
                final Object o;
                if (genericFieldRepresentation.isEntityField()) {
                    o = convertToEntityAttribute_Collection_Entity(entity, genericFieldRepresentation, iterable);
                } else {
                    final Function function = genericFieldRepresentation.isEmbeddableField()
                            ? it -> toEntity(elementType, it)
                            : it -> ValueReaderDecorator.getInstance().read(elementType, it);
                    o = StreamSupport.stream(iterable.spliterator(), false).map(function).collect(collector);
                }
                PropertyUtils.setProperty(entity, fieldName, o);
                return;
            }
            default: {
                final ValueReaderDecorator valueReader = ValueReaderDecorator.getInstance();
                final Object value = valueReader.read(nativeType, colValue);
                setEntityAttributeValue(entity, fieldName, nativeType, value);
                return;
            }
        }
    }

    protected void setEntityAttributeValue(Object entity, String fieldName, Class<?> nativeType, Object value) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (value == null) {
            if (!nativeType.isPrimitive()) {
                PropertyUtils.setProperty(entity, fieldName, value);
            }
            return;
        }

        if (byte.class.equals(nativeType)) {
            PropertyUtils.setProperty(entity, fieldName, ((Number) value).byteValue());
        } else if (short.class.equals(nativeType)) {
            PropertyUtils.setProperty(entity, fieldName, ((Number) value).shortValue());
        } else if (int.class.equals(nativeType)) {
            PropertyUtils.setProperty(entity, fieldName, ((Number) value).intValue());
        } else if (long.class.equals(nativeType)) {
            PropertyUtils.setProperty(entity, fieldName, ((Number) value).longValue());
        } else if (float.class.equals(nativeType)) {
            PropertyUtils.setProperty(entity, fieldName, ((Number) value).floatValue());
        } else if (double.class.equals(nativeType)) {
            PropertyUtils.setProperty(entity, fieldName, ((Number) value).doubleValue());
        } else if (boolean.class.equals(nativeType)) {
            PropertyUtils.setProperty(entity, fieldName, ((Boolean) value).booleanValue());
        } else {
            PropertyUtils.setProperty(entity, fieldName, value);
        }
    }

    protected abstract Object getColValue(FieldRepresentation fieldRepresentation, Object dbValue);

    protected abstract Object convertToEntityAttribute_SubEntity(Object entity, FieldRepresentation fieldRepresentation, Object colValue);

    protected abstract Object convertToEntityAttribute_Embeddable(Object entity, FieldRepresentation fieldRepresentation, Object colValue);

    protected abstract Object convertToEntityAttribute_Collection_Entity(Object entity, GenericFieldRepresentation fieldRepresentation, Iterable colValue);

    @SneakyThrows
    @Override
    public <T> T toDbData(T dbData, Object entity) {
        final ClassRepresentation<?> classRepresentation = ClassRepresentations.create(entity);
        classRepresentation.getFields().forEach(it -> fillDatabaseColumn(entity, it, dbData));
        return dbData;
    }

    @SneakyThrows
    private void fillDatabaseColumn(Object entity, FieldRepresentation fieldRepresentation, Object dbData) {
        final String fieldName = fieldRepresentation.getFieldName();
        final Object fieldValue = PropertyUtils.getProperty(entity, fieldName);
        if (fieldValue == null) {
            setDatabaseColumnValue(entity, fieldRepresentation, dbData, fieldValue);
            return;
        }

        final AttributeConverter attributeConverter = EntityConverter.attributeConverter(fieldRepresentation);
        if (attributeConverter != null) {
            final Object colValue = attributeConverter.convertToDatabaseColumn(fieldValue);
            setDatabaseColumnValue(entity, fieldRepresentation, dbData, colValue);
            return;
        }

        switch (fieldRepresentation.getType()) {
            case MAP: {
                // todo 后续支持
                throw new UnsupportedOperationException();
            }
            case SUBENTITY: {
                final Object o = convertToDatabaseColumn_SubEntity(entity, fieldRepresentation, fieldValue);
                setDatabaseColumnValue(entity, fieldRepresentation, dbData, o);
                return;
            }
            case EMBEDDABLE: {
                final Object o = convertToDatabaseColumn_Embeddable(entity, fieldRepresentation, fieldValue);
                setDatabaseColumnValue(entity, fieldRepresentation, dbData, o);
                return;
            }
            case COLLECTION: {
                final Object o = convertToDatabaseColumn_Collection(entity, (GenericFieldRepresentation) fieldRepresentation, (Iterable) fieldValue);
                setDatabaseColumnValue(entity, fieldRepresentation, dbData, o);
                return;
            }
            default: {
                setDatabaseColumnValue(entity, fieldRepresentation, dbData, fieldValue);
                return;
            }
        }
    }

    protected abstract void setDatabaseColumnValue(Object entity, FieldRepresentation fieldRepresentation, Object dbData, Object colValue);

    protected abstract Object convertToDatabaseColumn_Collection(Object entity, GenericFieldRepresentation fieldRepresentation, Iterable iterable);

    protected abstract Object convertToDatabaseColumn_Embeddable(Object entity, FieldRepresentation fieldRepresentation, Object fieldValue);

    protected abstract Object convertToDatabaseColumn_SubEntity(Object entity, FieldRepresentation fieldRepresentation, Object fieldValue);
}
