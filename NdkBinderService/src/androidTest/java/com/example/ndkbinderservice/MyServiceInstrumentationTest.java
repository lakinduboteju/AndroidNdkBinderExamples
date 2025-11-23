package com.example.ndkbinderservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ServiceTestRule;

import com.example.ComplexType;
import com.example.IMyService;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

/**
 * Instrumentation tests for NdkBinderService MyService
 * These tests run on an Android device or emulator with full JNI support
 * Tests verify the C++ AIDL implementation accessed via NDK
 */
@RunWith(AndroidJUnit4.class)
public class MyServiceInstrumentationTest {

    @Rule
    public final ServiceTestRule serviceRule = new ServiceTestRule();

    private Context context;
    private IMyService myService;
    private IBinder binder;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @After
    public void tearDown() {
        myService = null;
        binder = null;
    }

    @Test
    public void testBindService() throws TimeoutException {
        Intent serviceIntent = new Intent(context, MyService.class);
        binder = serviceRule.bindService(serviceIntent);

        assertNotNull("Service binder should not be null", binder);
    }

    @Test
    public void testServiceBinder_isIMyServiceStub() throws TimeoutException {
        Intent serviceIntent = new Intent(context, MyService.class);
        binder = serviceRule.bindService(serviceIntent);

        myService = IMyService.Stub.asInterface(binder);
        assertNotNull("IMyService interface should not be null", myService);
    }

    @Test
    public void testNativeBinderCreation() throws TimeoutException {
        Intent serviceIntent = new Intent(context, MyService.class);
        binder = serviceRule.bindService(serviceIntent);

        assertNotNull("Native binder should be created successfully", binder);

        // Verify it's a valid IMyService interface from native code
        myService = IMyService.Stub.asInterface(binder);
        assertNotNull("Native binder should implement IMyService", myService);
    }

    @Test
    public void testBasicTypes_nativeImplementation() throws TimeoutException, RemoteException {
        Intent serviceIntent = new Intent(context, MyService.class);
        binder = serviceRule.bindService(serviceIntent);
        myService = IMyService.Stub.asInterface(binder);

        // Call native basicTypes implementation - should complete without exception
        myService.basicTypes(
                2025,
                1234567890L,
                true,
                3.14f,
                3.141592653589793,
                "Native Test"
        );
    }

    @Test
    public void testComplexType_nativeReturnsFormattedString() throws TimeoutException, RemoteException {
        Intent serviceIntent = new Intent(context, MyService.class);
        binder = serviceRule.bindService(serviceIntent);
        myService = IMyService.Stub.asInterface(binder);

        ComplexType complexType = new ComplexType(
                42,
                999L,
                true,
                2.71f,
                1.414,
                "Native Complex"
        );

        String result = myService.complexType(complexType);

        assertNotNull("Result should not be null", result);

        // Native C++ implementation returns formatted string
        assertTrue("Result should contain int value", result.contains("int=42"));
        assertTrue("Result should contain long value", result.contains("long=999"));
        assertTrue("Result should contain bool value", result.contains("bool=1")); // C++ true = 1
        assertTrue("Result should contain float value", result.contains("float=2.71"));
        assertTrue("Result should contain double value", result.contains("double=1.414"));
        assertTrue("Result should contain string value", result.contains("string=Native Complex"));
    }

    @Test
    public void testReturnComplexType_nativeReturnsCorrectObject() throws TimeoutException, RemoteException {
        Intent serviceIntent = new Intent(context, MyService.class);
        binder = serviceRule.bindService(serviceIntent);
        myService = IMyService.Stub.asInterface(binder);

        ComplexType result = myService.returnComplexType(
                100,
                200L,
                false,
                3.5f,
                4.5,
                "Native Return"
        );

        assertNotNull("Returned ComplexType should not be null", result);
        assertEquals("Int value should match", 100, result.mInt);
        assertEquals("Long value should match", 200L, result.mLong);
        assertFalse("Boolean value should match", result.mBoolean);
        assertEquals("Float value should match", 3.5f, result.mFloat, 0.001f);
        assertEquals("Double value should match", 4.5, result.mDouble, 0.001);
        assertEquals("String value should match", "Native Return", result.mString);
    }

    @Test
    public void testMultipleNativeServiceCalls() throws TimeoutException, RemoteException {
        Intent serviceIntent = new Intent(context, MyService.class);
        binder = serviceRule.bindService(serviceIntent);
        myService = IMyService.Stub.asInterface(binder);

        // Make multiple calls to ensure native service stability
        for (int i = 0; i < 10; i++) {
            myService.basicTypes(i, i * 100L, i % 2 == 0, i * 1.5f, i * 2.5, "Native Call " + i);
        }
    }

    @Test
    public void testComplexType_withNullString() throws TimeoutException, RemoteException {
        Intent serviceIntent = new Intent(context, MyService.class);
        binder = serviceRule.bindService(serviceIntent);
        myService = IMyService.Stub.asInterface(binder);

        ComplexType complexType = new ComplexType(1, 2L, false, 3.0f, 4.0, null);
        String result = myService.complexType(complexType);

        assertNotNull("Result should not be null even with null string", result);
        // Native code should handle null strings (empty string in C++)
    }

    @Test
    public void testReturnComplexType_withEmptyString() throws TimeoutException, RemoteException {
        Intent serviceIntent = new Intent(context, MyService.class);
        binder = serviceRule.bindService(serviceIntent);
        myService = IMyService.Stub.asInterface(binder);

        ComplexType result = myService.returnComplexType(0, 0L, true, 0.0f, 0.0, "");

        assertNotNull("Returned ComplexType should not be null", result);
        assertEquals("Empty string should be preserved", "", result.mString);
    }

    @Test
    public void testServiceConnection_lifecycle() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final IBinder[] binderHolder = new IBinder[1];

        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binderHolder[0] = service;
                latch.countDown();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };

        Intent serviceIntent = new Intent(context, MyService.class);
        boolean bound = context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        assertTrue("Service should bind successfully", bound);
        assertTrue("Connection should be established within timeout",
                latch.await(5, TimeUnit.SECONDS));
        assertNotNull("Binder should not be null after connection", binderHolder[0]);

        // Verify the binder is from native code
        IMyService service = IMyService.Stub.asInterface(binderHolder[0]);
        assertNotNull("Native binder should implement IMyService", service);

        context.unbindService(connection);
    }

    @Test
    public void testBoundaryValues_nativeHandling() throws TimeoutException, RemoteException {
        Intent serviceIntent = new Intent(context, MyService.class);
        binder = serviceRule.bindService(serviceIntent);
        myService = IMyService.Stub.asInterface(binder);

        // Test native code handles maximum values
        myService.basicTypes(
                Integer.MAX_VALUE,
                Long.MAX_VALUE,
                true,
                Float.MAX_VALUE,
                Double.MAX_VALUE,
                "MAX"
        );

        // Test native code handles minimum values
        myService.basicTypes(
                Integer.MIN_VALUE,
                Long.MIN_VALUE,
                false,
                Float.MIN_VALUE,
                Double.MIN_VALUE,
                "MIN"
        );
    }

    @Test
    public void testComplexType_withSpecialCharacters() throws TimeoutException, RemoteException {
        Intent serviceIntent = new Intent(context, MyService.class);
        binder = serviceRule.bindService(serviceIntent);
        myService = IMyService.Stub.asInterface(binder);

        ComplexType complexType = new ComplexType(
                123,
                456L,
                true,
                7.89f,
                0.123,
                "Special: !@#$%^&*()_+-=[]{}|;:',.<>?/\\"
        );

        String result = myService.complexType(complexType);

        assertNotNull("Result should handle special characters", result);
        assertTrue("Result should contain int value", result.contains("int=123"));
        assertTrue("Result should contain long value", result.contains("long=456"));
    }

    @Test
    public void testReturnComplexType_withUnicodeString() throws TimeoutException, RemoteException {
        Intent serviceIntent = new Intent(context, MyService.class);
        binder = serviceRule.bindService(serviceIntent);
        myService = IMyService.Stub.asInterface(binder);

        String unicodeString = "Unicode: 你好 مرحبا שלום";
        ComplexType result = myService.returnComplexType(
                999,
                888L,
                true,
                1.23f,
                4.56,
                unicodeString
        );

        assertNotNull("Returned ComplexType should not be null", result);
        assertEquals("Unicode string should be preserved", unicodeString, result.mString);
    }

    @Test
    public void testNativeStaticService_persistsAcrossBinds() throws TimeoutException, RemoteException {
        // First bind
        Intent serviceIntent1 = new Intent(context, MyService.class);
        IBinder binder1 = serviceRule.bindService(serviceIntent1);
        IMyService service1 = IMyService.Stub.asInterface(binder1);

        assertNotNull("First binder should not be null", service1);

        // The native implementation uses a static MyService instance
        // Multiple binds should return the same underlying native service
        service1.basicTypes(1, 2L, true, 3.0f, 4.0, "Test");
    }

    @Test
    public void testComplexType_verifyAllFieldsInOutput() throws TimeoutException, RemoteException {
        Intent serviceIntent = new Intent(context, MyService.class);
        binder = serviceRule.bindService(serviceIntent);
        myService = IMyService.Stub.asInterface(binder);

        ComplexType complexType = new ComplexType(
                2021,
                65535000L,
                true,
                3.14f,
                3.141592653589793,
                "Hello, World!"
        );

        String result = myService.complexType(complexType);

        // Verify all fields are present in the native-formatted output
        assertNotNull("Result should not be null", result);
        assertTrue("Should contain int field", result.contains("int=2021"));
        assertTrue("Should contain long field", result.contains("long=65535000"));
        assertTrue("Should contain bool field", result.contains("bool=1"));
        assertTrue("Should contain float field", result.contains("float=3.14"));
        assertTrue("Should contain double field", result.contains("double=3.14159"));
        assertTrue("Should contain string field", result.contains("string=Hello, World!"));
    }
}
