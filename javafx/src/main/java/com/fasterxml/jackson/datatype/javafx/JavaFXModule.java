package com.fasterxml.jackson.datatype.javafx;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.javafx.des.JavaFXDeserializers;
import com.fasterxml.jackson.datatype.javafx.ser.JavaFXSerializers;
import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 * Created by kevin on 17/05/2017.
 */
public class JavaFXModule extends SimpleModule {

    public JavaFXModule() {
        super("JavaFXProperties");
    }
    

    @Override
    public void setupModule(SetupContext context) {

        context.setMixInAnnotations(ReadOnlyProperty.class, ReadOnlyPropertyMixin.class);

        context.addSerializers(new JavaFXSerializers());
        context.addDeserializers(new JavaFXDeserializers());
        context.addTypeModifier(new JavaFXTypeModifier());
    }

    abstract class ReadOnlyPropertyMixin<E> {

        @JsonIgnore
        abstract public Object getBean();
        
        @JsonProperty
        abstract public String getName();

        @JsonProperty
        abstract public E getValue();
    }

    abstract class ReadOnlyListPropertyMixin<E> extends ReadOnlyPropertyMixin<ObservableList<E>> {

    }

    abstract class ListPropertyMixin<E> extends ReadOnlyListPropertyMixin<E> {

    }

    abstract class ReadOnlyMapPropertyMixin<K, V> extends ReadOnlyPropertyMixin<ObservableMap<K, V>> {

    }
}
