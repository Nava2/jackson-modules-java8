package com.fasterxml.jackson.datatype.javafx.des;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by kevin on 17/05/2017.
 */
class ObservableListDeserializer extends ObservableCollectionDeserializer<ObservableList<Object>> {

    private static final long serialVersionUID = 1L;

    public ObservableListDeserializer(CollectionType type,
                                      TypeDeserializer typeDeser,
                                      JsonDeserializer<?> valueDeser) {
        super(type, typeDeser, valueDeser);
    }

    @Override
    protected JsonDeserializer<?> createFromContext(CollectionType type, TypeDeserializer typeDeser, JsonDeserializer<?> deser) {
        return new ObservableListDeserializer(type, typeDeser, deser);
    }

    @Override
    protected Collection<Object> getCollection(DeserializationContext ctxt) {
        return new ArrayList<>();
    }

    @Override
    protected ObservableList<Object> toObservable(Collection<Object> objects, DeserializationContext ctxt) {
        return FXCollections.observableList((ArrayList<Object>)objects);
    }
}
