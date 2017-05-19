package com.fasterxml.jackson.datatype.javafx;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.POJOPropertyBuilder;
import com.fasterxml.jackson.datatype.javafx.util.JavaFXBeanUtil;

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
    
    public boolean isJavaFXProperty() {
        return hasJavaFxPropertyAccessor()
                || (hasField() && JavaFXBeanUtil.isTypeJavaFXReadOnlyProperty(_fields.value.getType()));
    }

    public boolean hasJavaFxPropertyAccessor() {
        return _propertyAccessors != null;
    }

    public void addJavaFXPropertyAccessor(AnnotatedMethod a, PropertyName name, boolean explName, boolean visible, boolean ignored) {
        _propertyAccessors = new Linked<>(a, _propertyAccessors, name, explName, visible, ignored);
    }

    public AnnotatedMember getJavaFxProperty() {
        return _propertyAccessors != null ? _propertyAccessors.value : null;
    }

    public AnnotatedMethod getJavaFxPropertyAccessor()
    {
        // Easy with zero or one getters...
        Linked<AnnotatedMethod> curr = _propertyAccessors;
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
    
            // While getters have precedence concerns about multiple ways to specify,
            // this capability isn't a problem for Properties as they are only defined via the
            // suffix "Property"
            throw new IllegalArgumentException("Conflicting accessor definitions for property \"" + getName() + "\": "
                    + curr.value.getFullName() + " vs " + next.value.getFullName());
        }
        
        // One more thing; to avoid having to do it again...
        _propertyAccessors = _propertyAccessors.withoutNext();
        return curr.value;
    }
}
