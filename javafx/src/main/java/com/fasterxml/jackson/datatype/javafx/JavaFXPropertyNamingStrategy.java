package com.fasterxml.jackson.datatype.javafx;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;

/**
 * Adds additional functionality on top of the {@link PropertyNamingStrategy} for
 * {@link javafx.beans.property.Property} accessor methods and fields.
 */
public class JavaFXPropertyNamingStrategy extends PropertyNamingStrategy {

    /*
    /**********************************************************
    /* Offer the same strategies
    /**********************************************************
     */

    // kb: I don't like these, but because the Strategy is so tightly coupled to the normal
    // strategy, this was required to give the same translation functionality to finding properties
    // perhaps this mechanism could be more dynamic in the future.

    public final static JavaFXPropertyNamingStrategy LOWER_CASE =
            new JavaFXDelegateStrategy(new PropertyNamingStrategy.LowerCaseStrategy());

    public final static JavaFXPropertyNamingStrategy UPPER_CAMEL_CASE =
            new JavaFXDelegateStrategy(new PropertyNamingStrategy.UpperCamelCaseStrategy());

    public final static JavaFXPropertyNamingStrategy KEBAB_CASE =
            new JavaFXDelegateStrategy(new PropertyNamingStrategy.KebabCaseStrategy());

    public final static JavaFXPropertyNamingStrategy SNAKE_CASE =
            new JavaFXDelegateStrategy(new PropertyNamingStrategy.SnakeCaseStrategy());


    /**
     * Method called to find external name (name used in JSON) for given logical
     * JavaFX {@link javafx.beans.property.Property} value property, as defined
     * by a property accessor method; typically called when building a serializer.
     * (but not always -- when using "getter-as-setter", may be called during
     * deserialization)
     *
     * @param config Configuration in used: either <code>SerializationConfig</code>
     *   or <code>DeserializationConfig</code>, depending on whether method is called
     *   during serialization or deserialization
     * @param member Member used to access property
     * @param defaultName Default name that would be used for property in absence of custom strategy
     *
     * @return Logical name to use for property that the field represents
     */
    public String nameForJavaFXPropertyMethod(MapperConfig<?> config,
                                              AnnotatedMember member,
                                              String defaultName) {
        return defaultName;
    }

    /**
     * Method called to find external name (name used in JSON) for given logical
     * JavaFX {@link javafx.beans.property.Property} value property, as defined
     * by given field.
     *
     * @param config Configuration in used: either <code>SerializationConfig</code>
     *   or <code>DeserializationConfig</code>, depending on whether method is called
     *   during serialization or deserialization
     * @param member Member used to access property
     * @param defaultName Default name that would be used for property in absence of custom strategy
     *
     * @return Logical name to use for property that the field represents
     */
    public String nameForJavaFXProperty(MapperConfig<?> config,
                                        AnnotatedField member,
                                        String defaultName) {
        return defaultName;
    }

    public abstract static class JavaFXPropertyNamingStrategyBase extends JavaFXPropertyNamingStrategy {

        @Override
        public String nameForJavaFXPropertyMethod(MapperConfig<?> config, AnnotatedMember member, String defaultName) {
            return translate(defaultName);
        }

        @Override
        public String nameForJavaFXProperty(MapperConfig<?> config, AnnotatedField member, String defaultName) {
            return translate(defaultName);
        }

        protected abstract String translate(String input);
    }

    /**
     * Used for quick-use of "parent" {@link PropertyNamingStrategyBase} values to save rewriting this content.
     */
    private static class JavaFXDelegateStrategy extends JavaFXPropertyNamingStrategyBase {
        protected final PropertyNamingStrategyBase _delegate;

        protected JavaFXDelegateStrategy(PropertyNamingStrategyBase delegate) {
            this._delegate = delegate;
        }

        @Override
        protected String translate(String input) {
            return _delegate.translate(input);
        }
    }

}
