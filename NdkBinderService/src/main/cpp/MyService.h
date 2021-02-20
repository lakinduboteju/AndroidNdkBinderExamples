#pragma once

#include <aidl/com/example/BnMyService.h>

using aidl::com::example::ComplexType;
using ndk::ScopedAStatus;

namespace aidl {
namespace com {
namespace example {

class MyService : public BnMyService
{
public:
    ScopedAStatus basicTypes(int32_t in_anInt, int64_t in_aLong, bool in_aBoolean,
            float in_aFloat, double in_aDouble, const std::string& in_aString) override;

    ScopedAStatus complexType(const ComplexType& in_aComplexObject, std::string* _aidl_return) override;

    ScopedAStatus returnComplexType(int32_t in_anInt, int64_t in_aLong, bool in_aBoolean,
            float in_aFloat, double in_aDouble, const std::string& in_aString,
            ComplexType* _aidl_return) override;
};

} // namespace example
} // namespace com
} // namespace aidl
