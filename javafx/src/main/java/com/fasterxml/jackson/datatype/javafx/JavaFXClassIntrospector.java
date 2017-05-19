package com.fasterxml.jackson.datatype.javafx;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;
import com.fasterxml.jackson.databind.introspect.POJOPropertiesCollector;

/**
 * Created by kevin on 19/05/2017.
 */
public class JavaFXClassIntrospector extends BasicClassIntrospector {


    @Override
    protected POJOPropertiesCollector collectProperties(MapperConfig<?> config, JavaType type, MixInResolver r, boolean forSerialization, String mutatorPrefix) {
        return super.collectProperties(config, type, r, forSerialization, mutatorPrefix);
    }

    @Override
    protected POJOPropertiesCollector constructPropertyCollector(MapperConfig<?> config, AnnotatedClass ac, JavaType type, boolean forSerialization, String mutatorPrefix) {
        return new JavaFXPOJOPropertiesCollector(config, forSerialization, type, ac, mutatorPrefix);
    }
}
