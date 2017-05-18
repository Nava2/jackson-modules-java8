package com.fasterxml.jackson.datatype.javafx.des;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;

/**
 * Created by kevin on 17/05/2017.
 */
public class JavaFXDeserializers extends Deserializers.Base {

    @Override
    public JsonDeserializer<?> findReferenceDeserializer(ReferenceType refType,
                                                         DeserializationConfig config,
                                                         BeanDescription beanDesc,
                                                         TypeDeserializer contentTypeDeserializer,
                                                         JsonDeserializer<?> contentDeserializer) throws JsonMappingException {
        if (refType.isTypeOrSubTypeOf(ObservableValue.class)) {
            if (refType.isTypeOrSubTypeOf(ReadOnlyProperty.class)) {

            }
        }

        return null;
    }

    @Override
    public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
        return super.findBeanDeserializer(type, config, beanDesc);
    }

    @Override
    public JsonDeserializer<?> findCollectionDeserializer(CollectionType type,
                                                          DeserializationConfig config,
                                                          BeanDescription beanDesc,
                                                          TypeDeserializer elementTypeDeserializer,
                                                          JsonDeserializer<?> elementDeserializer)
            throws JsonMappingException {

        if (type.isTypeOrSubTypeOf(Observable.class)) {
            if (type.isTypeOrSubTypeOf(ObservableList.class)) {
                return new ObservableListDeserializer(type, elementTypeDeserializer, elementDeserializer);
            }

            if (type.isTypeOrSubTypeOf(ObservableSet.class)) {
                return new ObservableSetDeserializer(type, elementTypeDeserializer, elementDeserializer);
            }
        }

        return null;
    }

    @Override
    public JsonDeserializer<?> findMapDeserializer(MapType type,
                                                   DeserializationConfig config,
                                                   BeanDescription beanDesc,
                                                   KeyDeserializer keyDeserializer,
                                                   TypeDeserializer elementTypeDeserializer,
                                                   JsonDeserializer<?> elementDeserializer)
            throws JsonMappingException {

        if (type.isTypeOrSubTypeOf(Observable.class)) {
            if (type.isTypeOrSubTypeOf(ObservableMap.class)) {
                return new ObservableMapDeserializer(type, keyDeserializer, elementTypeDeserializer, elementDeserializer);
            }
        }

        return null;
    }
}
