package com.fasterxml.jackson.datatype.javafx;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.POJOPropertiesCollector;
import com.fasterxml.jackson.databind.introspect.POJOPropertyBuilder;
import com.fasterxml.jackson.datatype.javafx.util.JavaFXBeanUtil;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import static com.fasterxml.jackson.datatype.javafx.util.JavaFXBeanUtil.isTypeJavaFXReadOnlyProperty;

/**
 * Created by kevin on 19/05/2017.
 */
public class JavaFXPOJOPropertiesCollector extends POJOPropertiesCollector {

    protected JavaFXPOJOPropertiesCollector(MapperConfig<?> config, boolean forSerialization, JavaType type, AnnotatedClass classDef, String mutatorPrefix) {
        super(config, forSerialization, type, classDef, mutatorPrefix);
    }

    @SuppressWarnings("unchecked")
    protected LinkedHashMap<String, JavaFXPOJOPropertyBuilder> _getPropertiesBuilderMap() {
        // We do the cast hackery as we know the cast is valid
        // FIXME Move this to POJOPropertiesCollector and make the collection
        //       be LinkedHashMap<String, ? extends POJOPropertyBuilder>
        return (LinkedHashMap<String, JavaFXPOJOPropertyBuilder>)(LinkedHashMap)_properties;
    }

    /**
     * Use this override to adjust the properties found to make their <em>real</em> types known, not their "getter" type
     * which is unfortunately what Jackson defaults to.
     */
    @Override
    protected void collectAll() {

        if (!_collected) {
            super.collectAll();
            
            
            

            _getPropertiesBuilderMap().values().stream()
                    .forEach(bld -> {
                        
                        
                        
                        System.out.println(bld.getFullName());
                    });
        }
    }
    
    protected boolean _shouldPruneFinalFields() {
        return !_forSerialization && !_config.isEnabled(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS);
    }
    
    @Override
    protected void _addMethods(Map<String, POJOPropertyBuilder> props) {
    
        AnnotationIntrospector ai = _annotationIntrospector;
        final boolean pruneFinalFields = _shouldPruneFinalFields();
        final boolean transientAsIgnoral = _config.isEnabled(MapperFeature.PROPAGATE_TRANSIENT_MARKER);
    
        for (AnnotatedMethod m : _classDef.memberMethods()) {
            /* For methods, handling differs between getters and setters AND ACCESSORS; and
             * we will also only consider entries that either follow the bean
             * naming convention or are explicitly marked: just being visible
             * is not enough (unlike with fields)
             */
            int argCount = m.getParameterCount();
            if (argCount == 0) { // getters or accessors
                // If the return type is a JavaFX property, try to treat it as an Accessor
                if (isTypeJavaFXReadOnlyProperty(m.getType())) {
                    _addJavaFXAccessorMethod(props, m, ai);
                } else {
                    // Otherwise, we treat it as a normal getter
                    _addGetterMethod(props, m, ai);
                }
            } else if (argCount == 1) { // setters
                _addSetterMethod(props, m, ai);
            } else if (argCount == 2) { // any getter?
                if (ai != null) {
                    if (Boolean.TRUE.equals(ai.hasAnySetter(m))) {
                        if (_anySetters == null) {
                            _anySetters = new LinkedList<AnnotatedMethod>();
                        }
                        _anySetters.add(m);
                    }
                }
            }
        }
        
    }

    protected void _addJavaFXAccessorMethod(Map<String, POJOPropertyBuilder> props,
                                            AnnotatedMethod m,
                                            AnnotationIntrospector ai)
    {
        // Very first thing: skip if not returning any value
        if (!m.hasReturnType()) {
            return;
        }

        // any getter?
        // @JsonAnyGetter?
        if (Boolean.TRUE.equals(ai.hasAnyGetter(m))) {
            if (_anyGetters == null) {
                _anyGetters = new LinkedList<>();
            }
            _anyGetters.add(m);
            return;
        }

        // @JsonValue?
        if (Boolean.TRUE.equals(ai.hasAsValue(m))) {
            if (_jsonValueAccessors == null) {
                _jsonValueAccessors = new LinkedList<>();
            }
            _jsonValueAccessors.add(m);
            return;
        }
        
        PropertyName pn = ai.findNameForSerialization(m);
        boolean nameExplicit = (pn != null);

        String implName = ai.findImplicitPropertyName(m); // from naming convention
        boolean visible;
        if (!nameExplicit) { // no explicit name; must consider implicit
            if (implName == null) {
                implName = JavaFXBeanUtil.okNameForJavaFXAccessor(m);
            }

            if (implName == null) { // if not, must skip
                visible = _visibilityChecker.isIsGetterVisible(m);
            } else {
                visible = _visibilityChecker.isGetterVisible(m);
            }
        } else { // explicit indication of inclusion, but may be empty
            // we still need implicit name to link with other pieces
            if (implName == null) {
                implName = JavaFXBeanUtil.okNameForJavaFXAccessor(m);
            }
            // if not regular getter name, use method name as is
            if (implName == null) {
                implName = m.getName();
            }
            if (pn.isEmpty()) {
                pn = PropertyName.construct(implName);
                nameExplicit = false;
            }
            visible = true;
        }
        
        boolean ignore = ai.hasIgnoreMarker(m);
        _property(props, implName).addJavaFXPropertyAccessor(m, pn, nameExplicit, visible, ignore);
    }

    protected JavaFXPOJOPropertyBuilder _constructPropertyBuilder(MapperConfig<?> config, AnnotationIntrospector ai,
                                                            boolean forSerialization, PropertyName internalName) {
        return new JavaFXPOJOPropertyBuilder(config, ai, forSerialization, internalName);
    }

    @Override
    protected JavaFXPOJOPropertyBuilder _property(Map<String, POJOPropertyBuilder> props,
                                            PropertyName name) {
        return (JavaFXPOJOPropertyBuilder)props.computeIfAbsent(name.getSimpleName(),
                simpleName -> _constructPropertyBuilder(_config, _annotationIntrospector, _forSerialization, name));
    }

    @Deprecated @Override
    protected JavaFXPOJOPropertyBuilder _property(Map<String, POJOPropertyBuilder> props,
                                            String implName)
    {
        return _property(props, PropertyName.construct(implName));
    }
}
