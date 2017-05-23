package com.fasterxml.jackson.datatype.javafx;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.datatype.javafx.util.JavaFXBeanUtil;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ObservableValue;

import java.lang.annotation.Annotation;

/**
 * Created by kevin on 18/05/2017.
 */
class JavaFXAnnotationIntrospector extends JacksonAnnotationIntrospector {

    private JsonProperty getJsonProperty(Annotated annotated) {
        final String name;
        if (annotated instanceof AnnotatedField) {
            name = annotated.getName();
        } else if (annotated instanceof AnnotatedMethod) {
            name = annotated.getName().substring(0, annotated.getName().length() - "Property".length());
        } else {
            // can't handle this
            return null;
        }

        return new JsonProperty() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return JsonProperty.class;
            }

            @Override
            public String value() {
                return name;
            }

            @Override
            public boolean required() {
                return true;
            }

            @Override
            public int index() {
                return INDEX_UNKNOWN;
            }

            @Override
            public String defaultValue() {
                return "";
            }

            @Override
            public Access access() {
                return Access.READ_ONLY;
            }
        };
    }

    @Override @SuppressWarnings("unchecked")
    protected <A extends Annotation> A _findAnnotation(Annotated annotated, Class<A> annoClass) {
        JavaType annotatedType = annotated.getType();
        
        if (annotatedType != null) {
            if (annoClass == JsonProperty.class) {
                if (annotatedType.isTypeOrSubTypeOf(ReadOnlyProperty.class)) {
                    if (annotated.hasAnnotation(annoClass)) {
                        return super._findAnnotation(annotated, annoClass);
                    } else {
                        return (A) getJsonProperty(annotated);
                    }
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

    protected String getPropertyReferenceTag(AnnotatedMember member) {
        String owningTypeName = member.getDeclaringClass().getName();

        return owningTypeName + "%" + member.getFullName();
    }

    @Override
    public ReferenceProperty findReferenceType(AnnotatedMember member) {

        if (member instanceof AnnotatedField && member.getType().isTypeOrSubTypeOf(ObservableValue.class)) {
            return ReferenceProperty.managed(getPropertyReferenceTag(member));
        }

        return super.findReferenceType(member);
    }
}
