package com.fasterxml.jackson.datatype.jsr310.deser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.datatype.jsr310.ModuleTestBase;

import org.junit.Test;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class ZonedDateTimeDeserTest extends ModuleTestBase
{
    private final ObjectReader READER = newMapper().readerFor(ZonedDateTime.class);
    private final TypeReference<Map<String, ZonedDateTime>> MAP_TYPE_REF = new TypeReference<Map<String, ZonedDateTime>>() { };

    @Test
    public void testDeserializationAsString01() throws Exception
    {
        assertEquals("The value is not correct.",
                ZonedDateTime.of(2000, 1, 1, 12, 0, 0, 0, ZoneId.of("UTC")),
                READER.readValue(quote("2000-01-01T12:00Z")));
    }

    @Test
    public void testBadDeserializationAsString01() throws Throwable
    {
        expectFailure(quote("notazone"));
    }
    
    @Test
    public void testDeserializationAsArrayDisabled() throws Throwable
    {
        try {
            READER.readValue("[\"2000-01-01T12:00Z\"]");
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
    	try {
    	    READER.readValue("[]");
    	    fail("expected JsonMappingException");
        } catch (JsonMappingException e) {
           // OK
        } catch (IOException e) {
            throw e;
        }
    	try {
    		String json="[]";
        	newMapper()
            	.readerFor(ZonedDateTime.class)
            	.with(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
            	.readValue(aposToQuotes(json));
    	    fail("expected JsonMappingException");
        } catch (JsonMappingException e) {
           // OK
        } catch (IOException e) {
            throw e;
        }
    }

    @Test
    public void testDeserializationAsArrayEnabled() throws Throwable
    {
        ZonedDateTime value = newMapper()
    			.readerFor(ZonedDateTime.class)
               .with(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
    			.readValue("[\"2000-01-01T12:00Z\"]");
        assertEquals("The value is not correct.",
                ZonedDateTime.of(2000, 1, 1, 12, 0, 0, 0, ZoneId.of("UTC")),
                value);
    }
    
    @Test
    public void testDeserializationAsEmptyArrayEnabled() throws Throwable
    {
        String json="[]";
        ZonedDateTime value = newMapper()
                .readerFor(ZonedDateTime.class)
                .with(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS,
                        DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
                .readValue(aposToQuotes(json));
        assertNull(value);
    }

    /*
    /**********************************************************
    /* Tests for empty string handling
    /**********************************************************
     */

    @Test
    public void testLenientDeserializeFromEmptyString() throws Exception {

        String key = "zoneDateTime";
        ObjectMapper mapper = newMapper();
        ObjectReader objectReader = mapper.readerFor(MAP_TYPE_REF);

        String valueFromNullStr = mapper.writeValueAsString(asMap(key, null));
        Map<String, ZonedDateTime> actualMapFromNullStr = objectReader.readValue(valueFromNullStr);
        ZonedDateTime actualDateFromNullStr = actualMapFromNullStr.get(key);
        assertNull(actualDateFromNullStr);

        String valueFromEmptyStr = mapper.writeValueAsString(asMap(key, ""));
        Map<String, ZonedDateTime> actualMapFromEmptyStr = objectReader.readValue(valueFromEmptyStr);
        ZonedDateTime actualDateFromEmptyStr = actualMapFromEmptyStr.get(key);
        assertEquals("empty string failed to deserialize to null with lenient setting", null, actualDateFromEmptyStr);
    }

    @Test ( expected =  MismatchedInputException.class)
    public void testStrictDeserializeFromEmptyString() throws Exception {

        final String key = "zonedDateTime";
        final ObjectMapper mapper = mapperBuilder()
                .withConfigOverride(ZonedDateTime.class,
                        o -> o.setFormat(JsonFormat.Value.forLeniency(false)))
            .build();
        final ObjectReader objectReader = mapper.readerFor(MAP_TYPE_REF);

        String valueFromNullStr = mapper.writeValueAsString(asMap(key, null));
        Map<String, ZonedDateTime> actualMapFromNullStr = objectReader.readValue(valueFromNullStr);
        assertNull(actualMapFromNullStr.get(key));

        String valueFromEmptyStr = mapper.writeValueAsString(asMap(key, ""));
        objectReader.readValue(valueFromEmptyStr);
    }

    private void expectFailure(String json) throws Throwable {
        try {
            READER.readValue(aposToQuotes(json));
            fail("expected DateTimeParseException");
        } catch (JsonProcessingException e) {
            if (e.getCause() == null) {
                throw e;
            }
            if (!(e.getCause() instanceof DateTimeParseException)) {
                throw e.getCause();
            }
        }
    }
}
