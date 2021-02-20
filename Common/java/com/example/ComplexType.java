package com.example;

import android.os.Parcel;
import android.os.Parcelable;

public class ComplexType implements Parcelable
{
    int mInt;
    long mLong;
    boolean mBoolean;
    float mFloat;
    double mDouble;
    String mString;

    public ComplexType(int anInt, long aLong, boolean aBoolean, float aFloat,
                       double aDouble, String aString)
    {
        mInt = anInt;
        mLong = aLong;
        mBoolean = aBoolean;
        mFloat = aFloat;
        mDouble = aDouble;
        mString = aString;
    }

    protected ComplexType(Parcel in)
    {
        mInt = in.readInt();
        mLong = in.readLong();
        mBoolean = in.readBoolean();
        mFloat = in.readFloat();
        mDouble = in.readDouble();
        mString = in.readString();
    }

    public static final Creator<ComplexType> CREATOR = new Creator<ComplexType>()
    {
        @Override
        public ComplexType createFromParcel(Parcel in)
        {
            return new ComplexType(in);
        }

        @Override
        public ComplexType[] newArray(int size)
        {
            return new ComplexType[size];
        }
    };

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i)
    {
        parcel.writeInt(mInt);
        parcel.writeLong(mLong);
        parcel.writeBoolean(mBoolean);
        parcel.writeFloat(mFloat);
        parcel.writeDouble(mDouble);
        parcel.writeString(mString);
    }
}
