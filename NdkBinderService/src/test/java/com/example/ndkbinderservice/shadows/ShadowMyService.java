package com.example.ndkbinderservice.shadows;

import android.os.IBinder;
import com.example.ndkbinderservice.MyService;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowService;

/**
 * Shadow for MyService that mocks native library loading
 * This allows Robolectric tests to run without requiring JNI support
 */
@Implements(MyService.class)
public class ShadowMyService extends ShadowService {

    /**
     * Mock implementation of createServiceBinder that returns null
     * In Robolectric tests, we're testing the Java wrapper layer, not native functionality
     */
    @Implementation
    public IBinder createServiceBinder() {
        // Return null to simulate native library not being available in test environment
        // Actual native functionality is tested via instrumentation tests on device/emulator
        return null;
    }

    /**
     * Prevent native library loading by shadowing the static block
     * This is implicitly handled by Robolectric when the shadow is applied
     */
}
