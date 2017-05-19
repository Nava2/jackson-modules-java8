package com.fasterxml.jackson.datatype.javafx;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.POJOPropertyBuilder;
import javafx.beans.property.ReadOnlyProperty;

/**
 * Created by kevin on 19/05/2017.
 */
public class JavaFXPOJOPropertyBuilder extends POJOPropertyBuilder {

    protected Linked<AnnotatedMethod> _propertyAccessors;

    public JavaFXPOJOPropertyBuilder(MapperConfig<?> config, AnnotationIntrospector ai, boolean forSerialization, PropertyName internalName) {
        super(config, ai, forSerialization, internalName);
    }

    protected JavaFXPOJOPropertyBuilder(MapperConfig<?> config, AnnotationIntrospector ai, boolean forSerialization, PropertyName internalName, PropertyName name) {
        super(config, ai, forSerialization, internalName, name);
    }

    protected JavaFXPOJOPropertyBuilder(POJOPropertyBuilder src, PropertyName newName) {
        super(src, newName);
    }

    protected static boolean _isTypeReadOnlyProperty(JavaType type) {
        return type.isTypeOrSubTypeOf(ReadOnlyProperty.class);
    }

    public boolean isJavaFXProperty() {
        return hasJavaFxPropertyAccessor()
                || (hasField() && _isTypeReadOnlyProperty(_fields.value.getType()));
    }

    public boolean hasJavaFxPropertyAccessor() {
        return _propertyAccessors != null;
    }

    public void addJavaFXPropertyAccessor(AnnotatedMethod a, PropertyName name, boolean explName, boolean visible, boolean ignored) {
        _propertyAccessors = new Linked<>(a, _propertyAccessors, name, explName, visible, ignored);
    }

    public AnnotatedMember getJavaFxProperty() {
        return _propertyAccessors.value;
    }

    public AnnotatedMethod getJavaFxPropertyAccessor()
    {
        // Easy with zero or one getters...
        Linked<AnnotatedMethod> curr = _getters;
        if (curr == null) {
            return null;
        }
        Linked<AnnotatedMethod> next = curr.next;
        if (next == null) {
            return curr.value;
        }
        // But if multiple, verify that they do not conflict...
        for (; next != null; next = next.next) {
            /* [JACKSON-255] Allow masking, i.e. do not report exception if one
             *   is in super-class from the other
             */
            Class<?> currClass = curr.value.getDeclaringClass();
            Class<?> nextClass = next.value.getDeclaringClass();
            if (currClass != nextClass) {
                if (currClass.isAssignableFrom(nextClass)) { // next is more specific
                    curr = next;
                    continue;
                }
                if (nextClass.isAssignableFrom(currClass)) { // current more specific
                    continue;
                }
            }
            /* 30-May-2014, tatu: Three levels of precedence:
             *
             * 1. Regular getters ("getX")
             * 2. Is-getters ("isX")
             * 3. Implicit, possible getters ("x")
             */
            int priNext = _getterPriority(next.value);
            int priCurr = _getterPriority(curr.value);

            if (priNext != priCurr) {
                if (priNext < priCurr) {
                    curr = next;
                }
                continue;
            }
            throw new IllegalArgumentException("Conflicting getter definitions for property \""+getName()+"\": "
                    +curr.value.getFullName()+" vs "+next.value.getFullName());
        }
        // One more thing; to avoid having to do it again...
        _getters = curr.withoutNext();
        return curr.value;
    }
}
