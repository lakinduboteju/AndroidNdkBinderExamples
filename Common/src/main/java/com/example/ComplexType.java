package com.example;

import android.os.Parcel;
import android.os.Parcelable;

public class ComplexType implements Parcelable
{
    public final int mInt;
    public final long mLong;
    public final boolean mBoolean;
    public final float mFloat;
    public final double mDouble;
    public final String mString;

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

    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();

        str.append("ComplexType{")
                .append("int=").append(mInt)
                .append(", long=").append(mLong)
                .append(", boolean=").append(mBoolean)
                .append(", float=").append(mFloat)
                .append(", double=").append(mDouble)
                .append(", string='").append(mString).append('\'')
                .append('}');

        return str.toString();
    }
}
