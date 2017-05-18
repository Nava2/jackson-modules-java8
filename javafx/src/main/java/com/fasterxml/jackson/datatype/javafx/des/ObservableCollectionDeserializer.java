package com.fasterxml.jackson.datatype.javafx.des;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import javafx.beans.Observable;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by kevin on 17/05/2017.
 */
abstract class ObservableCollectionDeserializer<T extends Observable & Collection<Object>> extends StdDeserializer<T>
        implements ContextualDeserializer {


    private static final long serialVersionUID = 1L;

    protected final CollectionType _collType;

    protected final TypeDeserializer _typeDeser;
    protected final JsonDeserializer<?> _valueDeser;

    public ObservableCollectionDeserializer(CollectionType type,
                                            TypeDeserializer typeDeser,
                                            JsonDeserializer<?> valueDeser) {
        super(type);

        this._collType = type;
        this._typeDeser = typeDeser;
        this._valueDeser = valueDeser;

    }

    protected abstract JsonDeserializer<?> createFromContext(CollectionType type,
                                                             TypeDeserializer typeDeser,
                                                             JsonDeserializer<?> deser);

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        JsonDeserializer<?> deser = _valueDeser;
        TypeDeserializer typeDeser = _typeDeser;

        if (deser == null) {
            deser = ctxt.findContextualValueDeserializer(_collType.getContentType(), property);
        }

        if (typeDeser != null) {
            typeDeser = typeDeser.forProperty(property);
        }

        // no need to create a new one
        if (deser == _valueDeser && typeDeser == _typeDeser) {
            return this;
        }

        return createFromContext(_collType, typeDeser, deser);
    }

    protected abstract Collection<Object> getCollection(DeserializationContext ctxt);
    protected abstract T toObservable(Collection<Object> objects, DeserializationContext ctxt);

    @Override
    public T deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {

        Collection<Object> out = getCollection(ctxt);
        JsonToken t;
        while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
            Object value;

            if (t == JsonToken.VALUE_NULL) {
                value = null;
            } else if (_typeDeser == null) {
                value = _valueDeser.deserialize(jp, ctxt);
            } else {
                value = _valueDeser.deserializeWithType(jp, ctxt, _typeDeser);
            }
            out.add(value);
        }

        return toObservable(out, ctxt);
    }
}
