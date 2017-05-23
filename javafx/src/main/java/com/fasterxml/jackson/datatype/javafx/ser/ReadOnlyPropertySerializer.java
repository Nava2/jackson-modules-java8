package com.fasterxml.jackson.datatype.javafx.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.ReferenceType;
import javafx.beans.property.ReadOnlyProperty;

import java.io.IOException;

/**
 * Provides a {@link JsonSerializer} for {@link ReadOnlyProperty} values.
 */
public class ReadOnlyPropertySerializer<T>
        extends StdSerializer<ReadOnlyProperty<T>>
        implements ContextualSerializer {

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_BEAN = "bean";
    public static final String PROPERTY_VALUE = "value";

    protected final ReferenceType _refType;

    protected final BeanProperty _property;

    protected final TypeSerializer _typeSer;
    protected final JsonSerializer<Object> _valueSer;

    public ReadOnlyPropertySerializer(ReferenceType type,
                                      TypeSerializer typeDeser,
                                      JsonSerializer<?> valueDeser) {
        this(type, typeDeser, valueDeser, null);
    }

    protected ReadOnlyPropertySerializer(ReferenceType type,
                                         TypeSerializer typeDeser,
                                         JsonSerializer<?> valueDeser,
                                         BeanProperty property) {
        super(type);

        this._refType = type;
        this._typeSer = typeDeser;
        this._valueSer = (JsonSerializer<Object>)valueDeser;
        this._property = property;
    }

    protected JsonSerializer<?> withResolved(TypeSerializer typeSer,
                                             JsonSerializer<?> ser,
                                             SerializerProvider prov,
                                             BeanProperty property) {
        return new ReadOnlyPropertySerializer<>(_refType, typeSer, ser, property);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        JsonSerializer<?> valueSer = _valueSer;
        TypeSerializer typeSer = _typeSer;

        if (valueSer == null) {
            valueSer = prov.findValueSerializer(_refType.getContentType(), property);
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
    public void serialize(ReadOnlyProperty<T> value, JsonGenerator gen, SerializerProvider provider) throws IOException, JsonProcessingException {


        gen.writeStartObject();

        if (gen.canWriteTypeId()) {
            gen.writeTypeId(value);
        }

        if (gen.canWriteObjectId()) {
            gen.writeObjectId(_typeSer.getTypeIdResolver().idFromValue(value));
        }

        if (value.getName() != null) {
            gen.writeStringField(PROPERTY_NAME, value.getName());
        } else {
            gen.writeNullField(PROPERTY_NAME);
        }

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

        gen.writeEndObject();
    }
}
