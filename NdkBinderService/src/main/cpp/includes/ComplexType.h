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
};

} // namespace example
} // namespace com
} // namespace aidl
