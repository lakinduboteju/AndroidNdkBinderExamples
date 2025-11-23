package com.example.javabinderservice;

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
 * Instrumentation tests for JavaBinderService MyService
 * These tests run on an Android device or emulator
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
    public void testBasicTypes_doesNotThrowException() throws TimeoutException, RemoteException {
        Intent serviceIntent = new Intent(context, MyService.class);
        binder = serviceRule.bindService(serviceIntent);
        myService = IMyService.Stub.asInterface(binder);

        // Should complete without throwing exception
        myService.basicTypes(
                2025,
                1234567890L,
                true,
                3.14f,
                3.141592653589793,
                "Instrumentation Test"
        );
    }

    @Test
    public void testComplexType_returnsValidString() throws TimeoutException, RemoteException {
        Intent serviceIntent = new Intent(context, MyService.class);
        binder = serviceRule.bindService(serviceIntent);
        myService = IMyService.Stub.asInterface(binder);

        ComplexType complexType = new ComplexType(
                42,
                999L,
                true,
                2.71f,
                1.414,
                "Instrumentation Complex"
        );

        String result = myService.complexType(complexType);

        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain ComplexType", result.contains("ComplexType{"));
        assertTrue("Result should contain int value", result.contains("int=42"));
        assertTrue("Result should contain long value", result.contains("long=999"));
    }

    @Test
    public void testReturnComplexType_returnsCorrectObject() throws TimeoutException, RemoteException {
        Intent serviceIntent = new Intent(context, MyService.class);
        binder = serviceRule.bindService(serviceIntent);
        myService = IMyService.Stub.asInterface(binder);

        ComplexType result = myService.returnComplexType(
                100,
                200L,
                false,
                3.5f,
                4.5,
                "Test Return"
        );

        assertNotNull("Returned ComplexType should not be null", result);
        assertEquals("Int value should match", 100, result.mInt);
        assertEquals("Long value should match", 200L, result.mLong);
        assertEquals("Boolean value should match", false, result.mBoolean);
        assertEquals("Float value should match", 3.5f, result.mFloat, 0.001f);
        assertEquals("Double value should match", 4.5, result.mDouble, 0.001);
        assertEquals("String value should match", "Test Return", result.mString);
    }

    @Test
    public void testMultipleServiceCalls() throws TimeoutException, RemoteException {
        Intent serviceIntent = new Intent(context, MyService.class);
        binder = serviceRule.bindService(serviceIntent);
        myService = IMyService.Stub.asInterface(binder);

        // Make multiple calls to ensure service stability
        for (int i = 0; i < 10; i++) {
            myService.basicTypes(i, i * 100L, i % 2 == 0, i * 1.5f, i * 2.5, "Call " + i);
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

        context.unbindService(connection);
    }

    @Test
    public void testBoundaryValues() throws TimeoutException, RemoteException {
        Intent serviceIntent = new Intent(context, MyService.class);
        binder = serviceRule.bindService(serviceIntent);
        myService = IMyService.Stub.asInterface(binder);

        // Test with maximum values
        myService.basicTypes(
                Integer.MAX_VALUE,
                Long.MAX_VALUE,
                true,
                Float.MAX_VALUE,
                Double.MAX_VALUE,
                "MAX"
        );

        // Test with minimum values
        myService.basicTypes(
                Integer.MIN_VALUE,
                Long.MIN_VALUE,
                false,
                Float.MIN_VALUE,
                Double.MIN_VALUE,
                "MIN"
        );
    }
}
