package com.fasterxml.jackson.datatype.jsr310;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.Test;

import java.io.IOException;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class TestOffsetTimeDeserialization extends ModuleTestBase
{
    private final ObjectReader READER = newMapper().readerFor(OffsetTime.class);

    @Test
    public void testDeserializationAsString01() throws Exception
    {
        expectSuccess(OffsetTime.of(12, 0, 0, 0, ZoneOffset.UTC), "'12:00Z'");
    }

    @Test
    public void testBadDeserializationAsString01() throws Throwable
    {
        expectFailure("'notanoffsettime'");
    }

    @Test
    public void testDeserializationAsArrayDisabled() throws Throwable
    {
    	try {
    		read("['12:00Z']");
    	    fail("expected JsonMappingException");
        } catch (JsonMappingException e) {
           // OK
        } catch (IOException e) {
            throw e;
        }

    }
    
    @Test
    public void testDeserializationAsEmptyArrayDisabled() throws Throwable
    {
    	// works even without the feature enabled
    	assertNull(read("[]"));
    }
    
    @Test
    public void testDeserializationAsArrayEnabled() throws Throwable
    {
    	String json="['12:00Z']";
    	OffsetTime value= newMapper()
    			.configure(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, true)
    			.readerFor(OffsetTime.class).readValue(aposToQuotes(json));
    	notNull(value);
        expect(OffsetTime.of(12, 0, 0, 0, ZoneOffset.UTC), value);
    }
    
    @Test
    public void testDeserializationAsEmptyArrayEnabled() throws Throwable
    {
    	String json="[]";
    	OffsetTime value= newMapper()
    			.configure(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, true)
    			.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
    			.readerFor(OffsetTime.class).readValue(aposToQuotes(json));
    	assertNull(value);
    }

    
    private void expectFailure(String json) throws Throwable {
        try {
            read(json);
            fail("expected DateTimeParseException");
        } catch (JsonProcessingException e) {
            if (e.getCause() == null) {
                throw e;
            }
            if (!(e.getCause() instanceof DateTimeParseException)) {
                throw e.getCause();
            }
        } catch (IOException e) {
            throw e;
        }
    }

    private void expectSuccess(Object exp, String json) throws IOException {
        final OffsetTime value = read(json);
        notNull(value);
        expect(exp, value);
    }

    private OffsetTime read(final String json) throws IOException {
        return READER.readValue(aposToQuotes(json));
    }

    private static void notNull(Object value) {
        assertNotNull("The value should not be null.", value);
    }

    private static void expect(Object exp, Object value) {
        assertEquals("The value is not correct.", exp,  value);
    }
}
