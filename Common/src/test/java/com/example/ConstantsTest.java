package com.example;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Constants interface
 */
public class ConstantsTest {

    @Test
    public void testLogTagExists() {
        assertNotNull(Constants.LOG_TAG);
    }

    @Test
    public void testLogTagValue() {
        assertEquals("ndkbinderexamples", Constants.LOG_TAG);
    }

    @Test
    public void testLogTagNotEmpty() {
        assertFalse(Constants.LOG_TAG.isEmpty());
    }
}
