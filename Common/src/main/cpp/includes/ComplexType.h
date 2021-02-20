#pragma once

#include <android/binder_status.h>

namespace aidl {
namespace com {
namespace example {

class ComplexType
{
public:
    int i_Int;
    long l_Long;
    bool b_Boolean;
    float f_Float;
    double d_Double;
    std::string s_String;

public:
    ComplexType() : i_Int(-1), l_Long(-1), b_Boolean(false), f_Float(-1.0f),
                    d_Double(-1.0), s_String()
    {}

    ComplexType(int iInt, long lLong, bool bBoolean, float fFloat, double dDouble,
            std::string sString)
    {
        i_Int = iInt;
        l_Long = lLong;
        b_Boolean = bBoolean;
        f_Float = fFloat;
        d_Double = dDouble;
        s_String = sString;
    }

public:
    binder_status_t readFromParcel(const AParcel* pParcel)
    {
        int32_t iNotNull;
        AParcel_readInt32(pParcel, &iNotNull);

        AParcel_readInt32(pParcel, &i_Int);

        int64_t aLong;
        AParcel_readInt64(pParcel, &aLong);
        l_Long = aLong;

        AParcel_readBool(pParcel, &b_Boolean);

        AParcel_readFloat(pParcel, &f_Float);

        AParcel_readDouble(pParcel, &d_Double);

        ndk::AParcel_readString(pParcel, &s_String);

        return STATUS_OK;
    }

    binder_status_t writeToParcel(AParcel* pParcel) const
    {
        int32_t iNotNull = 1;
        AParcel_writeInt32(pParcel, iNotNull);

        AParcel_writeInt32(pParcel, i_Int);

        AParcel_writeInt64(pParcel, l_Long);

        AParcel_writeBool(pParcel, b_Boolean);

        AParcel_writeFloat(pParcel, f_Float);

        AParcel_writeDouble(pParcel, d_Double);

        ndk::AParcel_writeString(pParcel, s_String);

        return STATUS_OK;
    }

    void toString(std::string* pOutputString)
    {
        char strBuf[1024];
        snprintf(strBuf, 1024,
                "ComplexType{int=%d, long=%ld, bool=%d, float=%f, double=%lf, string='%s'}",
                 i_Int, l_Long, b_Boolean, f_Float, d_Double, s_String.c_str());

        pOutputString->append(strBuf);
    }
};

} // namespace example
} // namespace com
} // namespace aidl
