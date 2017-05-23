package com.fasterxml.jackson.datatype.javafx;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeModifier;
import javafx.beans.value.ObservableValue;

import java.lang.reflect.Type;

/**
 * Use a modifier to property discern reference types
 */
public class JavaFXTypeModifier extends TypeModifier
{


    @Override
    public JavaType modifyType(JavaType type, Type jdkType, TypeBindings bindings, TypeFactory typeFactory)
    {
        if (type.isJavaLangObject() || type.isReferenceType() || type.isContainerType()) {
            return type;
        }

        // Look for Property values
        if (type.isTypeOrSubTypeOf(ObservableValue.class)) {
            return ReferenceType.upgradeFrom(type, bindings.getBoundType(0));
        }

        return type;
    }
}
