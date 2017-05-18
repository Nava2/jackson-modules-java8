package com.fasterxml.jackson.datatype.javafx.ser;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.ReferenceType;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ObservableValue;

/**
 * Created by kevin on 17/05/2017.
 */
public class JavaFXSerializers extends Serializers.Base {

    @Override
    public JsonSerializer<?> findReferenceSerializer(SerializationConfig config,
                                                     ReferenceType type,
                                                     BeanDescription beanDesc,
                                                     TypeSerializer contentTypeSerializer,
                                                     JsonSerializer<Object> contentValueSerializer) {
        if (type.isTypeOrSubTypeOf(ObservableValue.class)) {
            if (type.isTypeOrSubTypeOf(ReadOnlyProperty.class)) {
                return new ReadOnlyPropertySerializer<>(type, contentTypeSerializer, contentValueSerializer);
            }
        }

        return null;
    }

    @Override
    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
        return super.findSerializer(config, type, beanDesc);
    }
}
