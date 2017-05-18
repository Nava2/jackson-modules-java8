package com.fasterxml.jackson.datatype.javafx.des;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by kevin on 17/05/2017.
 */
class ObservableSetDeserializer extends ObservableCollectionDeserializer<ObservableSet<Object>> {

    private static final long serialVersionUID = 1L;

    public ObservableSetDeserializer(CollectionType type,
                                     TypeDeserializer typeDeser,
                                     JsonDeserializer<?> valueDeser) {
        super(type, typeDeser, valueDeser);
    }

    @Override
    protected JsonDeserializer<?> createFromContext(CollectionType type, TypeDeserializer typeDeser, JsonDeserializer<?> deser) {
        return new ObservableSetDeserializer(type, typeDeser, deser);
    }

    @Override @SuppressWarnings("unchecked")
    protected Collection<Object> getCollection(DeserializationContext ctxt) {
        if (_collType.getContentType().isTypeOrSubTypeOf(Enum.class)) {
            return EnumSet.noneOf((Class)_collType.getContentType().getRawClass());
        }

        return new LinkedHashSet<>();
    }

    @Override
    protected ObservableSet<Object> toObservable(Collection<Object> objects, DeserializationContext ctxt) {
        return FXCollections.observableSet((Set<Object>)objects);
    }
}
