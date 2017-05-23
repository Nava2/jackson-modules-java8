package com.fasterxml.jackson.datatype.javafx.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.type.ReferenceType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ObservableValue;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Provides a {@link JsonSerializer} for {@link ReadOnlyProperty} values.
 */
class ObjectPropertySerializer<T>
        extends PropertySerializerBase<T, ObjectProperty<T>>
        implements ContextualSerializer {

    protected ObjectPropertySerializer(ReferenceType type,
                                       TypeSerializer typeDeser,
                                       JsonSerializer<?> valueDeser) {
        this(type, typeDeser, valueDeser, null);
    }

    protected ObjectPropertySerializer(ReferenceType type,
                                       TypeSerializer typeDeser,
                                       JsonSerializer<?> valueDeser,
                                       BeanProperty property) {
        super(type, typeDeser, valueDeser, property);
    }

    protected JsonSerializer<?> withResolved(TypeSerializer typeSer,
                                             JsonSerializer<?> ser,
                                             SerializerProvider prov,
                                             BeanProperty property) {
        return new ObjectPropertySerializer<>(_refType, typeSer, ser, property);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        JsonSerializer<?> valueSer = _valueSer;
        TypeSerializer typeSer = _typeSer;

        if (valueSer == null) {
            valueSer = prov.findTypedValueSerializer(_refType.getContentType(), true, property);
        }

        if (typeSer != null) {
            typeSer = typeSer.forProperty(property);
        }

        // no need to create a new one
        if (valueSer == _valueSer && typeSer == _typeSer) {
            return this;
        }

        return withResolved(typeSer, valueSer, prov, property);
    }


    @Override
    protected void serializeValue(final ObjectProperty<T> value, final JsonGenerator gen, final SerializerProvider provider)
            throws IOException {

        if (value.isBound()) {
            gen.writeObjectField("bound", true);

            gen.writeFieldName(PROPERTY_VALUE);

            final Field badReflection;
            final ObservableValue<? extends T> boundTo;
            try {
                badReflection = ObjectPropertyBase.class.getDeclaredField("observable");
                badReflection.setAccessible(true);

                boundTo = (ObservableValue<? extends T>)badReflection.get(value);
            } catch (NoSuchFieldException | IllegalAccessException nsfe) {
                throw new IllegalStateException(nsfe);
            }

            JavaType obvValType = provider.getTypeFactory().constructReferenceType(ObservableValue.class, _refType.getReferencedType());
            JsonSerializer<Object> obvValSer = provider.findValueSerializer(obvValType);

            obvValSer.serialize(boundTo, gen, provider);
        } else {
            if (value.getValue() != null) {

                gen.writeFieldName(PROPERTY_VALUE);

                JsonSerializer<Object> ser = _valueSer;
                if (ser == null) {
                    ser = provider.findTypedValueSerializer(_refType.getContentType(), true, _property);
                }
                if (_typeSer != null) {
                    ser.serializeWithType(value.getValue(), gen, provider, _typeSer);
                } else {
                    ser.serialize(value.getValue(), gen, provider);
                }
            } else {
                gen.writeNullField(PROPERTY_VALUE);
            }
        }
    }
}
