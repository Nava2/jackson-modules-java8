package com.fasterxml.jackson.datatype.javafx.des;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.javafx.ser.ReadOnlyPropertySerializer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.io.IOException;
import java.util.Optional;
import java.util.OptionalDouble;

import static com.fasterxml.jackson.core.JsonToken.END_OBJECT;

/**
 * Created by kevin on 17/05/2017.
 */
public class ReadOnlyDoubleDeserializer
        extends JsonDeserializer<ReadOnlyDoubleProperty> {

    public ReadOnlyDoubleDeserializer() {

    }

    @Override
    public Class<?> handledType() {
        return ReadOnlyDoubleProperty.class;
    }

    @Override
    public ReadOnlyDoubleProperty deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (!jp.isExpectedStartObjectToken()) {
            throw new InvalidFormatException(jp, "Should have started object", null, handledType());
        }

        TypeFactory tf = ctxt.getTypeFactory();

        JsonDeserializer<Object> valueDeserializer = ctxt.findNonContextualValueDeserializer(tf.constructType(double.class));

        Optional<String> name = Optional.empty();
        Optional<Object> bean = Optional.empty();
        OptionalDouble value = OptionalDouble.empty();

        while (jp.currentToken() != END_OBJECT) {
            switch (jp.nextFieldName()) {
                case ReadOnlyPropertySerializer.PROPERTY_NAME:
                    name = Optional.of(jp.nextTextValue());
                    break;

                case ReadOnlyPropertySerializer.PROPERTY_BEAN: {
                    JsonNode node = jp.readValueAsTree();

                    String typeId = node.get(JsonTypeInfo.Id.CLASS.getDefaultPropertyName()).asText();

                    try {
                        Class<?> clazz = tf.findClass(typeId);

                        ObjectMapper mapper = new ObjectMapper();
                        bean = Optional.of(mapper.treeToValue(node, clazz));
                    } catch (ClassNotFoundException e) {
                        throw new InvalidTypeIdException(jp, "Invalid type", ctxt.getContextualType(), typeId);
                    }
                }
                break;

                case ReadOnlyPropertySerializer.PROPERTY_VALUE: {
                    jp.nextValue();
                    value = OptionalDouble.of((Integer) valueDeserializer.deserialize(jp, ctxt));
                    jp.nextToken();
                }
                break;
            }
        }

        DoubleProperty out = new SimpleDoubleProperty(bean.orElse(null), name.orElse(null));
        value.ifPresent(out::set);

        return out;
    }
}
