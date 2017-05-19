package com.fasterxml.jackson.datatype.javafx.util;

import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.util.BeanUtil;

/**
 * Add more utility functionality on top of the shipped {@link BeanUtil} to add
 * {@link javafx.beans.property.Property} accessors.
 *
 */
public class JavaFXBeanUtil extends BeanUtil {

    protected static final String JAVAFX_PROPERTY_STD_SUFFIX = "Property";

    /**
     * @since 2.5
     */
    public static String okNameForJavaFXAccessor(AnnotatedMethod am) {
        final String name = am.getName();
        if (name.endsWith(JAVAFX_PROPERTY_STD_SUFFIX)) {
            return stdMangleJavaFXProperty(name);
        }
        return null;
    }

    /**
     * Extract the property name from the base
     */
    protected static String stdMangleJavaFXProperty(String baseName) {
        return baseName.substring(0, baseName.length() - JAVAFX_PROPERTY_STD_SUFFIX.length());
    }
}
