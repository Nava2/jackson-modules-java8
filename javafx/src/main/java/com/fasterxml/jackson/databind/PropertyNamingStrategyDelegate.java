package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;

/**
 * Created by kevin on 19/05/2017.
 */
public class PropertyNamingStrategyDelegate extends PropertyNamingStrategy.PropertyNamingStrategyBase {

    protected final PropertyNamingStrategyBase _delegate;

    public PropertyNamingStrategyDelegate(PropertyNamingStrategyBase delegate) {
        if (delegate == null) {
            throw new NullPointerException("delegate == null");
        }

        this._delegate = delegate;
    }

    @Override
    public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
        return _delegate.nameForField(config, field, defaultName);
    }

    @Override
    public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return _delegate.nameForGetterMethod(config, method, defaultName);
    }

    @Override
    public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return _delegate.nameForSetterMethod(config, method, defaultName);
    }

    @Override
    public String nameForConstructorParameter(MapperConfig<?> config, AnnotatedParameter ctorParam, String defaultName) {
        return _delegate.nameForConstructorParameter(config, ctorParam, defaultName);
    }

    @Override
    public String translate(String propertyName) {
        return _delegate.translate(propertyName);
    }
}
