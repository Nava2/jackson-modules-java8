package com.fasterxml.jackson.datatype.javafx;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.datatype.javafx.util.JavaFXBeanUtil;
import javafx.beans.property.ReadOnlyProperty;

import java.lang.annotation.Annotation;

/**
 * Created by kevin on 18/05/2017.
 */
class JavaFXAnnotationIntrospector extends JacksonAnnotationIntrospector {

    @Override @SuppressWarnings("unchecked")
    protected <A extends Annotation> A _findAnnotation(Annotated annotated, Class<A> annoClass) {
        JavaType annotatedType = annotated.getType();
        
        if (annotatedType != null && JavaFXBeanUtil.isTypeJavaFXReadOnlyProperty(annotatedType)) {
            // Get the data used for Identity, otherwise we will attach it with the default value.
            if (annoClass == JsonIdentityInfo.class) {
                JsonIdentityInfo fromChildren = super._findAnnotation(annotated, JsonIdentityInfo.class);

                if (fromChildren != null) {
                    return (A)fromChildren;
                } else {
                    return (A)new JsonIdentityInfo() {

                        @Override
                        public Class<? extends Annotation> annotationType() {
                            return JsonIdentityInfo.class;
                        }

                        @Override
                        public String property() {
                            return "@id";
                        }

                        @Override
                        public Class<? extends ObjectIdGenerator<?>> generator() {
                            // Use UUIDs since Integers may clash later ??
                            return ObjectIdGenerators.UUIDGenerator.class;
                        }

                        @Override
                        public Class<? extends ObjectIdResolver> resolver() {
                            return SimpleObjectIdResolver.class;
                        }

                        @Override
                        public Class<?> scope() {
                            return Object.class;
                        }
                    };
                }
            }

        }

        return super._findAnnotation(annotated, annoClass);
    }

    @Override
    protected boolean _hasAnnotation(Annotated annotated, Class<? extends Annotation> annoClass) {
        if (annoClass == JsonIdentityInfo.class && annotated.getType().isTypeOrSubTypeOf(ReadOnlyProperty.class)) {
            return true;
        }

        return super._hasAnnotation(annotated, annoClass);
    }

    @Override
    public ReferenceProperty findReferenceType(AnnotatedMember member) {
        return super.findReferenceType(member);
    }
}
