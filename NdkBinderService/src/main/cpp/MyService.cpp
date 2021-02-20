#include "MyService.h"
#include <LogDefs.h>

using aidl::com::example::MyService;

ScopedAStatus MyService::basicTypes(int32_t in_anInt, int64_t in_aLong, bool in_aBoolean,
        float in_aFloat, double in_aDouble, const std::string &in_aString)
{
    LOGD("[MyService] [cpp] basicTypes: int=%d, long=%ld, bool=%d, float=%f, double=%f, string=%s",
            in_anInt, in_aLong, in_aBoolean, in_aFloat, in_aDouble, in_aString.c_str());

    return ScopedAStatus::ok();
}

ScopedAStatus MyService::complexType(const ComplexType &in_aComplexObject, std::string* _aidl_return)
{
    LOGD("[MyService] [cpp] complexType: int=%d, long=%ld, bool=%d, float=%f, double=%lf, string=%s",
         in_aComplexObject.i_Int,
         in_aComplexObject.l_Long,
         in_aComplexObject.b_Boolean,
         in_aComplexObject.f_Float,
         in_aComplexObject.d_Double,
         in_aComplexObject.s_String.c_str());

    char strBuf[1024];

    snprintf(strBuf, 1024, "int=%d, long=%ld, bool=%d, float=%f, double=%lf, string=%s",
            in_aComplexObject.i_Int,
            in_aComplexObject.l_Long,
            in_aComplexObject.b_Boolean,
            in_aComplexObject.f_Float,
            in_aComplexObject.d_Double,
            in_aComplexObject.s_String.c_str());

    *_aidl_return = std::string(strBuf);

    return ScopedAStatus::ok();
}

ScopedAStatus MyService::returnComplexType(int32_t in_anInt, int64_t in_aLong, bool in_aBoolean,
        float in_aFloat, double in_aDouble, const std::string &in_aString,
        ComplexType *_aidl_return)
{
    LOGD("[MyService] [cpp] returnComplexType: int=%d, long=%ld, bool=%d, float=%f, double=%f, string=%s",
         in_anInt, in_aLong, in_aBoolean, in_aFloat, in_aDouble, in_aString.c_str());

    _aidl_return->i_Int = in_anInt;
    _aidl_return->l_Long = in_aLong;
    _aidl_return->b_Boolean = in_aBoolean;
    _aidl_return->f_Float = in_aFloat;
    _aidl_return->d_Double = in_aDouble;
    _aidl_return->s_String = in_aString;

    return ScopedAStatus::ok();
}
