package com.fasterxml.jackson.datatype.javafx.ser;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.WritableObjectId;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.ReferenceType;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;

import java.io.IOException;

/**
 * Provides a {@link JsonSerializer} for {@link ReadOnlyProperty} values.
 */
abstract class PropertySerializerBase<T, P extends Property<T>>
        extends StdSerializer<P>
        implements ContextualSerializer {

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_BEAN = "bean";
    public static final String PROPERTY_VALUE = "value";

    protected final ReferenceType _refType;

    protected final BeanProperty _property;

    protected final TypeSerializer _typeSer;
    protected final JsonSerializer<Object> _valueSer;
    
    /**
     * For dealing with ID values
     */
    protected final ObjectIdWriter _objectIdWriter;

    protected PropertySerializerBase(ReferenceType type,
                                     TypeSerializer typeDeser,
                                     JsonSerializer<?> valueDeser) {
        this(type, typeDeser, valueDeser, null, null);
    }

    protected PropertySerializerBase(ReferenceType type,
                                     TypeSerializer typeDeser,
                                     JsonSerializer<?> valueDeser,
                                     BeanProperty property,
                                     ObjectIdWriter objectIdWriter) {
        super(type);

        this._refType = type;
        this._typeSer = typeDeser;
        this._valueSer = (JsonSerializer<Object>)valueDeser;
        this._property = property;
        this._objectIdWriter = objectIdWriter;
    }

    protected abstract JsonSerializer<?> withResolved(TypeSerializer typeSer,
                                                      JsonSerializer<?> ser,
                                                      SerializerProvider prov,
                                                      BeanProperty property,
                                                      ObjectIdWriter objectIdWriter);

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
    
        final AnnotationIntrospector intr = prov.getAnnotationIntrospector();
        final AnnotatedMember accessor = (property == null || intr == null)
                ? null : property.getMember();
        
        JsonSerializer<?> valueSer = _valueSer;
        TypeSerializer typeSer = _typeSer;

        if (valueSer == null) {
            valueSer = prov.findValueSerializer(_refType.getContentType(), property);
        }

        if (typeSer != null) {
            typeSer = typeSer.forProperty(property);
        }
        
        ObjectIdWriter oiw = _objectIdWriter;
        if (accessor != null) {
    
            ObjectIdInfo objectIdInfo = intr.findObjectIdInfo(accessor);
            if (objectIdInfo == null) {
                // no ObjectId override, but maybe ObjectIdRef?
                if (oiw != null) {
                    objectIdInfo = intr.findObjectReferenceInfo(accessor, null);
                    if (objectIdInfo != null) {
                        oiw = _objectIdWriter.withAlwaysAsId(objectIdInfo.getAlwaysAsId());
                    }
                }
            } else {
                // Ugh: mostly copied from BeanSerializerBase
                
                // 2.1: allow modifications by "id ref" annotations as well:
                objectIdInfo = intr.findObjectReferenceInfo(accessor, objectIdInfo);
                Class<?> implClass = objectIdInfo.getGeneratorType();
                JavaType type = prov.constructType(implClass);
                JavaType idType = prov.getTypeFactory().findTypeParameters(type, ObjectIdGenerator.class)[0];
                
                /*
                 FIXME Deal with property id handler, it's awkward because it doesn't really make sense in this context.
                  */
                ObjectIdGenerator<?> gen = prov.objectIdGeneratorInstance(accessor, objectIdInfo);
                oiw = ObjectIdWriter.construct(idType, objectIdInfo.getPropertyName(), gen,
                            objectIdInfo.getAlwaysAsId());
                
            }
        }
        
        // If at this point there is no identification setup, then we need to generate it via the information stored
        // on the type's annotations. Usually handled via mixins :)
        if (oiw == null && accessor != null) {
            BeanDescription beanDesc = prov.getConfig().introspectClassAnnotations(accessor.getType());
            ObjectIdInfo info = intr.findObjectIdInfo(beanDesc.getClassInfo());
            if (info != null) {
                JavaType idType = prov.getTypeFactory().findTypeParameters(prov.constructType(info.getGeneratorType()), ObjectIdGenerator.class)[0];
                ObjectIdGenerator<?> gen = prov.objectIdGeneratorInstance(accessor, info);
                oiw = ObjectIdWriter.construct(idType, info.getPropertyName(), gen, info.getAlwaysAsId());
                // be sure to set the serializer for the id
                oiw = oiw.withSerializer(prov.findValueSerializer(idType));
            } else {
                prov.reportMappingProblem(new IllegalStateException(),
                        "No ID information for type: %s", accessor.getType());
            }
        }
        
        // no need to create a new one
        if (valueSer == _valueSer && typeSer == _typeSer && oiw == _objectIdWriter) {
            return this;
        }

        return withResolved(typeSer, valueSer, prov, property, oiw);
    }

    protected abstract void serializeValue(P value, JsonGenerator gen, SerializerProvider provider) throws IOException;

    @Override
    public void serialize(P value, JsonGenerator gen, SerializerProvider provider) throws IOException, JsonProcessingException {
        gen.writeStartObject();
        
        ObjectIdWriter w = _objectIdWriter;
        
        if (w != null) {
            // use native first
            WritableObjectId objectId = provider.findObjectId(value, w.generator);
            // If possible, write as id already
            if (objectId.writeAsId(gen, provider, w)) {
                return;
            }
            
            // If not, need to inject the id:
            Object id = objectId.generateId(value);
            if (w.alwaysAsId) {
                w.serializer.serialize(id, gen, provider);
                return;
            }
            
            objectId.writeAsField(gen, provider, w);
        }

        if (gen.canWriteTypeId()) {
            gen.writeTypeId(value);
        }

        if (value.getName() != null) {
            gen.writeStringField(PROPERTY_NAME, value.getName());
        } else {
            gen.writeNullField(PROPERTY_NAME);
        }

        serializeValue(value, gen, provider);

        gen.writeEndObject();
    }
}
