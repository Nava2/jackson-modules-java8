package com.fasterxml.jackson.datatype.javafx.des;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.MapType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kevin on 17/05/2017.
 *
 * <em>Strongly based off of {@link com.fasterxml.jackson.datatype.guava.deser.GuavaMapDeserializer}</em>
 */
class ObservableMapDeserializer extends StdDeserializer<ObservableMap<Object, Object>>
        implements ContextualDeserializer {


    private static final long serialVersionUID = 1L;

    protected final MapType _collType;

    protected final KeyDeserializer _keyDeser;
    protected final TypeDeserializer _typeDeser;
    protected final JsonDeserializer<?> _valueDeser;

    public ObservableMapDeserializer(MapType type,
                                     KeyDeserializer keyDeser,
                                     TypeDeserializer typeDeser,
                                     JsonDeserializer<?> valueDeser) {
        super(type);

        this._collType = type;
        this._keyDeser = keyDeser;
        this._typeDeser = typeDeser;
        this._valueDeser = valueDeser;

    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        JsonDeserializer<?> valueDeser = _valueDeser;
        KeyDeserializer keyDeser = _keyDeser;
        TypeDeserializer typeDeser = _typeDeser;

        if (valueDeser == null) {
            valueDeser = ctxt.findContextualValueDeserializer(_collType.getContentType(), property);
        }

        if (keyDeser == null) {
            keyDeser = ctxt.findKeyDeserializer(_collType.getKeyType(), property);
        }

        if (typeDeser != null) {
            typeDeser = typeDeser.forProperty(property);
        }

        // no need to create a new one
        if (valueDeser == _valueDeser
                && typeDeser == _typeDeser
                && keyDeser == _keyDeser) {
            return this;
        }

        return new ObservableMapDeserializer(_collType, keyDeser, typeDeser, valueDeser);
    }

    @Override
    public ObservableMap<Object, Object> deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException, JsonProcessingException {

        final KeyDeserializer keyDes = _keyDeser;
        final JsonDeserializer<?> valueDes = _valueDeser;
        final TypeDeserializer typeDeser = _typeDeser;

        Map<Object, Object> out = new HashMap<>();

        // move past OBJECT_START
        p.nextToken();
        for (; p.getCurrentToken() == JsonToken.FIELD_NAME; p.nextToken()) {
            // Must point to field name now
            String fieldName = p.getCurrentName();
            Object key = (keyDes == null) ? fieldName : keyDes.deserializeKey(fieldName, ctxt);

            // move to value
            p.nextToken();

            final Object value;
            if (typeDeser == null) {
                value = valueDes.deserialize(p, ctxt);
            } else {
                value = valueDes.deserializeWithType(p, ctxt, typeDeser);
            }
            out.put(key, value);
        }

        return FXCollections.observableMap(out);
    }
}
