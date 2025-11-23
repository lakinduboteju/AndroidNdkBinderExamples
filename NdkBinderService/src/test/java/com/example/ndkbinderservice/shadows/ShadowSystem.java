package com.example.ndkbinderservice.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow for System class to prevent native library loading in Robolectric tests
 * This allows tests to run without requiring JNI support
 */
@Implements(System.class)
public class ShadowSystem {

    /**
     * Mock implementation of loadLibrary that does nothing
     * Prevents UnsatisfiedLinkError when MyService static block tries to load native library
     */
    @Implementation
    public static void loadLibrary(String libname) {
        // Do nothing - native libraries cannot be loaded in Robolectric JVM environment
        // Actual native functionality is tested via instrumentation tests on device/emulator
    }
}
