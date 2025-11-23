package com.example;

import android.os.Parcel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

/**
 * Unit tests for ComplexType class
 * Tests cover constructor, parcelable implementation, and toString
 */
@RunWith(RobolectricTestRunner.class)
public class ComplexTypeTest {

    private ComplexType complexType;
    private static final int TEST_INT = 2025;
    private static final long TEST_LONG = 1234567890L;
    private static final boolean TEST_BOOLEAN = true;
    private static final float TEST_FLOAT = 3.14f;
    private static final double TEST_DOUBLE = 3.141592653589793;
    private static final String TEST_STRING = "Hello, Android NDK Binder!";

    @Before
    public void setUp() {
        complexType = new ComplexType(
                TEST_INT,
                TEST_LONG,
                TEST_BOOLEAN,
                TEST_FLOAT,
                TEST_DOUBLE,
                TEST_STRING
        );
    }

    @Test
    public void testConstructor() {
        assertEquals(TEST_INT, complexType.mInt);
        assertEquals(TEST_LONG, complexType.mLong);
        assertEquals(TEST_BOOLEAN, complexType.mBoolean);
        assertEquals(TEST_FLOAT, complexType.mFloat, 0.001f);
        assertEquals(TEST_DOUBLE, complexType.mDouble, 0.0001);
        assertEquals(TEST_STRING, complexType.mString);
    }

    @Test
    public void testDescribeContents() {
        assertEquals(0, complexType.describeContents());
    }

    @Test
    public void testParcelable() {
        // Write to parcel
        Parcel parcel = Parcel.obtain();
        complexType.writeToParcel(parcel, 0);

        // Reset parcel for reading
        parcel.setDataPosition(0);

        // Create from parcel
        ComplexType fromParcel = ComplexType.CREATOR.createFromParcel(parcel);

        // Verify all fields
        assertEquals(complexType.mInt, fromParcel.mInt);
        assertEquals(complexType.mLong, fromParcel.mLong);
        assertEquals(complexType.mBoolean, fromParcel.mBoolean);
        assertEquals(complexType.mFloat, fromParcel.mFloat, 0.001f);
        assertEquals(complexType.mDouble, fromParcel.mDouble, 0.0001);
        assertEquals(complexType.mString, fromParcel.mString);

        parcel.recycle();
    }

    @Test
    public void testParcelableWithNullString() {
        ComplexType typeWithNullString = new ComplexType(
                TEST_INT,
                TEST_LONG,
                TEST_BOOLEAN,
                TEST_FLOAT,
                TEST_DOUBLE,
                null
        );

        Parcel parcel = Parcel.obtain();
        typeWithNullString.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        ComplexType fromParcel = ComplexType.CREATOR.createFromParcel(parcel);
        assertNull(fromParcel.mString);

        parcel.recycle();
    }

    @Test
    public void testParcelableWithEmptyString() {
        ComplexType typeWithEmptyString = new ComplexType(
                TEST_INT,
                TEST_LONG,
                TEST_BOOLEAN,
                TEST_FLOAT,
                TEST_DOUBLE,
                ""
        );

        Parcel parcel = Parcel.obtain();
        typeWithEmptyString.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        ComplexType fromParcel = ComplexType.CREATOR.createFromParcel(parcel);
        assertEquals("", fromParcel.mString);

        parcel.recycle();
    }

    @Test
    public void testCreatorNewArray() {
        ComplexType[] array = ComplexType.CREATOR.newArray(5);
        assertNotNull(array);
        assertEquals(5, array.length);
    }

    @Test
    public void testToString() {
        String result = complexType.toString();

        assertNotNull(result);
        assertTrue(result.contains("ComplexType{"));
        assertTrue(result.contains("int=" + TEST_INT));
        assertTrue(result.contains("long=" + TEST_LONG));
        assertTrue(result.contains("boolean=" + TEST_BOOLEAN));
        assertTrue(result.contains("float=" + TEST_FLOAT));
        assertTrue(result.contains("double=" + TEST_DOUBLE));
        assertTrue(result.contains("string='" + TEST_STRING + "'"));
    }

    @Test
    public void testToStringWithNullString() {
        ComplexType typeWithNullString = new ComplexType(
                TEST_INT,
                TEST_LONG,
                TEST_BOOLEAN,
                TEST_FLOAT,
                TEST_DOUBLE,
                null
        );

        String result = typeWithNullString.toString();
        assertNotNull(result);
        assertTrue(result.contains("string='null'"));
    }

    @Test
    public void testBoundaryValues() {
        ComplexType boundaryType = new ComplexType(
                Integer.MAX_VALUE,
                Long.MAX_VALUE,
                false,
                Float.MAX_VALUE,
                Double.MAX_VALUE,
                "Boundary Test"
        );

        assertEquals(Integer.MAX_VALUE, boundaryType.mInt);
        assertEquals(Long.MAX_VALUE, boundaryType.mLong);
        assertFalse(boundaryType.mBoolean);
        assertEquals(Float.MAX_VALUE, boundaryType.mFloat, 0.001f);
        assertEquals(Double.MAX_VALUE, boundaryType.mDouble, 0.0001);
    }

    @Test
    public void testNegativeValues() {
        ComplexType negativeType = new ComplexType(
                Integer.MIN_VALUE,
                Long.MIN_VALUE,
                false,
                Float.MIN_VALUE,
                Double.MIN_VALUE,
                "Negative Test"
        );

        assertEquals(Integer.MIN_VALUE, negativeType.mInt);
        assertEquals(Long.MIN_VALUE, negativeType.mLong);
        assertEquals(Float.MIN_VALUE, negativeType.mFloat, 0.001f);
        assertEquals(Double.MIN_VALUE, negativeType.mDouble, 0.0001);
    }
}
