package com.fasterxml.jackson.datatype.javafx.des;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.UnresolvedForwardReference;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.ReferenceType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;

/**
 * Created by kevin on 2017-05-23.
 */
public class ObjectPropertyDeserializer
        extends StdDeserializer<ObjectProperty<?>>
        implements ContextualDeserializer {
    
    protected final ReferenceType _refType;
    
    protected final BeanProperty _property;
    
    protected final TypeDeserializer _typeDeser;
    protected final JsonDeserializer<Object> _valueDeser;
    
    protected final ObjectIdReader _objectIdReader;
    
    protected ObjectPropertyDeserializer(final ReferenceType refType,
                                         final TypeDeserializer typeSer,
                                         final JsonDeserializer<?> valueSer,
                                         final ObjectIdReader objectIdReader,
                                         final BeanProperty property) {
        super(refType);
        this._refType = refType;
        this._property = property;
        this._typeDeser = typeSer;
        this._valueDeser = (JsonDeserializer<Object>)valueSer;
        this._objectIdReader = objectIdReader;
    }
    
    public ObjectPropertyDeserializer(final ReferenceType refType,
                                      final TypeDeserializer typeSer,
                                      final JsonDeserializer<?> valueSer) {
        this(refType, typeSer, valueSer, null, null);
    }
    
    protected ObjectPropertyDeserializer withResolved(final BeanProperty property,
                                                         final TypeDeserializer typeSer,
                                                         final JsonDeserializer<?> valueSer,
                                                         final ObjectIdReader objectIdReader) {
        return new ObjectPropertyDeserializer(_refType, typeSer, valueSer, objectIdReader, property);
    }
    
    @Override
    public ObjectProperty<?> deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        
        if (p.isExpectedStartObjectToken()) {
            ctxt.reportWrongTokenException(_refType, JsonToken.START_OBJECT, "Expected object start");
            return null; // not accessed
        }
    
        p.nextToken();
        if (_objectIdReader != null && _objectIdReader.maySerializeAsObject()) {
            if (p.hasTokenId(JsonTokenId.ID_FIELD_NAME)
                    && _objectIdReader.isValidReferencePropertyName(p.getCurrentName(), p)) {
                return deserializeFromObjectId(p, ctxt);
            }
        }
        
        // this means it wasn't an ID, so we get to handle this by hand
        
        switch (p.nextToken()) {
        case FIELD_NAME: {
            String fieldName = p.nextFieldName();
            p.nextToken();
            
            
        }
        }
        
        return new SimpleObjectProperty<>(null);
    }
    
    /**
     * Method called in cases where it looks like we got an Object Id
     * to parse and use as a reference.
     */
    protected ObjectProperty<?> deserializeFromObjectId(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        Object id = _objectIdReader.readObjectReference(p, ctxt);
        ReadableObjectId roid = ctxt.findObjectId(id, _objectIdReader.generator, _objectIdReader.resolver);
        // do we have it resolved?
        Object pojo = roid.resolve();
        
        if (pojo == null) { // not yet; should wait...
            throw new UnresolvedForwardReference(p,
                    String.format("Could not resolve Object Id [%s] (for %s).", id, _refType),
                    p.getCurrentLocation(), roid);
        }
        
        // cast it as it was resolved
        return (ObjectProperty<?>) pojo;
    }
    
    @Override
    public JsonDeserializer<?> createContextual(final DeserializationContext ctxt, final BeanProperty property) throws JsonMappingException {
        final AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        final AnnotatedMember accessor = (property == null || intr == null)
                ? null : property.getMember();
        
        JsonDeserializer<?> valueDeser = _valueDeser;
        TypeDeserializer typeDeser = _typeDeser;
    
        
        if (valueDeser == null) {
            valueDeser = ctxt.findContextualValueDeserializer(_refType.getReferencedType(), property);
        } else { // otherwise directly assigned, probably not contextual yet:
            valueDeser = ctxt.handleSecondaryContextualization(valueDeser, property, _refType.getReferencedType());
        }
        
        if (typeDeser != null) {
            typeDeser = typeDeser.forProperty(property);
        }
    
        ObjectIdReader oir = _objectIdReader;
        
        if (accessor != null && intr != null) {
        
            ObjectIdInfo objectIdInfo = intr.findObjectIdInfo(accessor);
            
            // Parameters used for construction of ObjectIdReader
            JavaType idType = null;
            SettableBeanProperty idProp = null;
            ObjectIdGenerator<?> gen;
            ObjectIdResolver idResolver;
            JsonDeserializer<Object> idDeser;
            
            if (objectIdInfo == null) {
                // no ObjectId override, but maybe ObjectIdRef?
                if (oir != null) {
                    objectIdInfo = intr.findObjectReferenceInfo(accessor, null);
                }
            } else {
                // Ugh: mostly copied from BeanSerializerBase
            
                // 2.1: allow modifications by "id ref" annotations as well:
                objectIdInfo = intr.findObjectReferenceInfo(accessor, objectIdInfo);
                Class<?> implClass = objectIdInfo.getGeneratorType();
                JavaType type = ctxt.constructType(implClass);
                idType = ctxt.getTypeFactory().findTypeParameters(type, ObjectIdGenerator.class)[0];
                idProp = null;
                
                if (ObjectIdGenerators.PropertyGenerator.class.isAssignableFrom(implClass)) {
                    ctxt.reportBadDefinition(ctxt.getContextualType(), "Property deserialization is not supported");
//                    PropertyName propName = objectIdInfo.getPropertyName();
//                    idProp = ctxt.find
                }
            }
    
            if (objectIdInfo == null) {
                // If at this point there is no identification setup, then we need to generate it via the information stored
                // on the type's annotations. Usually handled via mixins :)
                // TODO could this be done without traversing the entire hierarchy? We only care about the class-level annotations
                BeanDescription beanDesc = ctxt.getConfig().introspectClassAnnotations(accessor.getType());
                objectIdInfo = intr.findObjectIdInfo(beanDesc.getClassInfo());
            }
    
            // There's no ID information, so we can't do shit
            if (objectIdInfo == null) {
                ctxt.reportBadDefinition(accessor.getType(), "No ID information for type");
            }
    
            gen = ctxt.objectIdGeneratorInstance(accessor, objectIdInfo);
            idResolver = ctxt.objectIdResolverInstance(accessor, objectIdInfo);
            idDeser = ctxt.findContextualValueDeserializer(idType, property);
    
            oir = ObjectIdReader.construct(idType, objectIdInfo.getPropertyName(), gen, idDeser,
                    idProp, idResolver);
        }
    
        
        // no need to create a new one
        if ((valueDeser == _valueDeser) && (typeDeser == _typeDeser)
                && (oir == _objectIdReader) && (property == _property)) {
            return this;
        }
        
        return withResolved(property, typeDeser, valueDeser, oir);
    }
}
