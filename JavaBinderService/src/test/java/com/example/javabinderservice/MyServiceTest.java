package com.example.javabinderservice;

import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import com.example.ComplexType;
import com.example.IMyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ServiceController;

import static org.junit.Assert.*;

/**
 * Unit tests for JavaBinderService MyService
 * Tests cover service lifecycle, binder methods, and AIDL interface implementation
 */
@RunWith(RobolectricTestRunner.class)
public class MyServiceTest {

    private ServiceController<MyService> controller;
    private MyService service;
    private IBinder binder;
    private IMyService myService;

    @Before
    public void setUp() {
        controller = Robolectric.buildService(MyService.class);
        service = controller.create().get();
    }

    @Test
    public void testServiceCreation() {
        assertNotNull(service);
    }

    @Test
    public void testOnBind() {
        Intent intent = new Intent();
        binder = service.onBind(intent);

        assertNotNull(binder);
        assertTrue(binder instanceof IMyService.Stub);
    }

    @Test
    public void testBinderIsIMyServiceStub() {
        Intent intent = new Intent();
        binder = service.onBind(intent);

        myService = IMyService.Stub.asInterface(binder);
        assertNotNull(myService);
    }

    @Test
    public void testBasicTypes() throws RemoteException {
        Intent intent = new Intent();
        binder = service.onBind(intent);
        myService = IMyService.Stub.asInterface(binder);

        // Should not throw exception
        myService.basicTypes(
                2025,
                1234567890L,
                true,
                3.14f,
                3.141592653589793,
                "Test String"
        );
    }

    @Test
    public void testComplexType() throws RemoteException {
        Intent intent = new Intent();
        binder = service.onBind(intent);
        myService = IMyService.Stub.asInterface(binder);

        ComplexType complexType = new ComplexType(
                100,
                200L,
                true,
                1.5f,
                2.5,
                "Complex Test"
        );

        String result = myService.complexType(complexType);

        assertNotNull(result);
        assertTrue(result.contains("ComplexType{"));
        assertTrue(result.contains("int=100"));
        assertTrue(result.contains("long=200"));
    }

    @Test
    public void testReturnComplexType() throws RemoteException {
        Intent intent = new Intent();
        binder = service.onBind(intent);
        myService = IMyService.Stub.asInterface(binder);

        ComplexType result = myService.returnComplexType(
                42,
                9999L,
                false,
                2.71f,
                1.414,
                "Returned Complex"
        );

        assertNotNull(result);
        assertEquals(42, result.mInt);
        assertEquals(9999L, result.mLong);
        assertEquals(false, result.mBoolean);
        assertEquals(2.71f, result.mFloat, 0.001f);
        assertEquals(1.414, result.mDouble, 0.001);
        assertEquals("Returned Complex", result.mString);
    }

    @Test
    public void testMultipleBindCalls() {
        Intent intent = new Intent();
        IBinder binder1 = service.onBind(intent);
        IBinder binder2 = service.onBind(intent);

        assertNotNull(binder1);
        assertNotNull(binder2);
        assertEquals(binder1, binder2); // Should return same binder instance
    }

    @Test
    public void testServiceLifecycle() {
        controller.create().startCommand(0, 0);
        assertNotNull(service);

        controller.bind().get();
        Intent intent = new Intent();
        IBinder binder = service.onBind(intent);
        assertNotNull(binder);

        controller.unbind().destroy();
    }

    @Test
    public void testComplexTypeWithNullString() throws RemoteException {
        Intent intent = new Intent();
        binder = service.onBind(intent);
        myService = IMyService.Stub.asInterface(binder);

        ComplexType complexType = new ComplexType(
                1,
                2L,
                false,
                3.0f,
                4.0,
                null
        );

        String result = myService.complexType(complexType);
        assertNotNull(result);
        assertTrue(result.contains("string='null'"));
    }

    @Test
    public void testReturnComplexTypeWithEmptyString() throws RemoteException {
        Intent intent = new Intent();
        binder = service.onBind(intent);
        myService = IMyService.Stub.asInterface(binder);

        ComplexType result = myService.returnComplexType(
                0,
                0L,
                true,
                0.0f,
                0.0,
                ""
        );

        assertNotNull(result);
        assertEquals("", result.mString);
    }

    @Test
    public void testBasicTypesWithBoundaryValues() throws RemoteException {
        Intent intent = new Intent();
        binder = service.onBind(intent);
        myService = IMyService.Stub.asInterface(binder);

        // Should not throw exception with boundary values
        myService.basicTypes(
                Integer.MAX_VALUE,
                Long.MAX_VALUE,
                true,
                Float.MAX_VALUE,
                Double.MAX_VALUE,
                "Max Values"
        );

        myService.basicTypes(
                Integer.MIN_VALUE,
                Long.MIN_VALUE,
                false,
                Float.MIN_VALUE,
                Double.MIN_VALUE,
                "Min Values"
        );
    }
}
