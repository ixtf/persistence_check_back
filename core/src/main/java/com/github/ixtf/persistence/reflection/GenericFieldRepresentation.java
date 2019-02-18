package com.github.ixtf.persistence.reflection;

import javax.persistence.AttributeConverter;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class GenericFieldRepresentation extends AbstractFieldRepresentation {

    GenericFieldRepresentation(FieldType type, Field field, String name, Class<? extends AttributeConverter> converter) {
        super(type, name, field, converter);
    }

    @Override
    public boolean isId() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GenericFieldRepresentation that = (GenericFieldRepresentation) o;
        return type == that.type &&
                Objects.equals(nativeField, that.nativeField) &&
                Objects.equals(colName, that.colName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, nativeField, colName);
    }

    public boolean isEmbeddable() {
        return isEmbeddableField() || isEntityField();
    }

    private boolean isEntityField() {
        return hasFieldAnnotation(Entity.class);
    }

    private boolean isEmbeddableField() {
        return hasFieldAnnotation(Embeddable.class);
    }

    private boolean hasFieldAnnotation(Class<?> annotation) {
        return Class.class.cast(ParameterizedType.class.cast(getNativeField()
                .getGenericType())
                .getActualTypeArguments()[0])
                .getAnnotation(annotation) != null;
    }

    public Class getElementType() {
        return Class.class.cast(ParameterizedType.class.cast(getNativeField()
                .getGenericType())
                .getActualTypeArguments()[0]);
    }

    public Collection getCollectionInstance() {
        Class<?> type = getNativeField().getType();
        if (Deque.class.equals(type) || Queue.class.equals(type)) {
            return new LinkedList<>();
        } else if (List.class.equals(type) || Iterable.class.equals(type)) {
            return new ArrayList<>();
        } else if (NavigableSet.class.equals(type) || SortedSet.class.equals(type)) {
            return new TreeSet<>();
        } else if (Set.class.equals(type)) {
            return new HashSet<>();
        }
        throw new UnsupportedOperationException("This collection is not supported yet: " + type);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GenericFieldRepresentation{");
        sb.append(", type=").append(type);
        sb.append(", field=").append(nativeField);
        sb.append(", name='").append(colName).append('\'');
        sb.append(", fieldName='").append(fieldName).append('\'');
        sb.append(", converter=").append(converter);
        sb.append('}');
        return sb.toString();
    }
}
