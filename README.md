# Android NDK (C++) Binder Examples

## How to run NDK Binder service

1. Build and install `NdkBinderService` APK. It contains an Android Service, whose implementation is done in C++ JNI layer using NDK Binder APIs.

2. Build and install `JavaBinderClient` APK. It contains an Android Activity, who binds the Service from `NdkBinderService` and talks to Service using Java Binder APIs.

3. Run `JavaBinderClient`'s main Activity.

## How to run NDK Binder client

1. Build and install `JavaBinderService` APK. It contains an Android Service implemented in Java.

2. Build and install `NdkBinderClient` APK. It contains an Android Activity, who binds the Service from `JavaBinderService` and passes the IBinder object to C++ JNI layer to talk to the Service using NDK Binder APIs.

3. Run `NdkBinderClient`'s main Activity.

## NDK Binder service implementation details

[NdkBinderService](NdkBinderService/) : Android app (APK) module, containing a Java Service ([MyService.java](NdkBinderService/src/main/java/com/example/ndkbinderservice/MyService.java)) that implements an AIDL ([IMyService.aidl](Common/src/main/aidl/com/example/IMyService.aidl)). Implementation (Binder native) is done in C++ JNI layer ([MyService.cpp](NdkBinderService/src/main/cpp/MyService.cpp)).

AIDL

[Common/aidl/com/example/IMyService.aidl](Common/aidl/com/example/IMyService.aidl)

```java
package com.example;

import com.example.ComplexType;

interface IMyService
{
    void basicTypes(int anInt, long aLong, boolean aBoolean,
                    float aFloat, double aDouble, String aString);
    
    String complexType(in ComplexType aComplexObject);

    ComplexType returnComplexType(int anInt, long aLong,
                    boolean aBoolean, float aFloat,
                    double aDouble, String aString);
}
```

Gradle task to auto-generate NDK C++ binder source files.

[Common/build.gradle](Common/build.gradle)

```gradle
task compileAidlNdk() {
    doLast {
        def aidlCpp = [android.sdkDirectory,
                       'build-tools',
                       android.buildToolsVersion,
                       'aidl'].join(File.separator)

        def outDir = [projectDir.absolutePath,
                      'src', 'main', 'cpp', 'aidl'].join(File.separator)

        def headerOutDir = [projectDir.absolutePath,
                           'src', 'main', 'cpp', 'includes'].join(File.separator)

        def searchPathForImports = [projectDir.absolutePath, 'src', 'main', 'aidl'].join(File.separator)

        def aidlFile = [projectDir.absolutePath,
                       'src', 'main', 'aidl',
                       'com', 'example', 'IMyService.aidl'].join(File.separator)

        exec {
            executable(aidlCpp)
            args('--lang=ndk',
                 '-o', outDir,
                 '-h', headerOutDir,
                 '-I', searchPathForImports,
                 aidlFile)
        }
    }
}

afterEvaluate {
    preBuild.dependsOn(compileAidlNdk)
}
```

JNI library and NDK Binder implementations.

[NdkBinderService/src/main/cpp/native-lib.cpp](NdkBinderService/src/main/cpp/native-lib.cpp)

```c++
#include <jni.h>
#include "MyService.h"

extern "C" JNIEXPORT jobject JNICALL
Java_com_example_ndkbinderservice_MyService_createServiceBinder(
        JNIEnv* env,
        jobject /* this */)
{
    static MyService myService;
    return env->NewGlobalRef(AIBinder_toJavaBinder(env, myService.asBinder().get()));
}
```

[NdkBinderService/src/main/cpp/MyService.cpp](NdkBinderService/src/main/cpp/MyService.cpp)

```c++
#include <aidl/com/example/BnMyService.h>

class MyService : public BnMyService
{
public:
    ScopedAStatus basicTypes(int32_t in_anInt, int64_t in_aLong,
                             bool in_aBoolean, float in_aFloat,
                             double in_aDouble,
                             const std::string& in_aString) override
    {
        return ScopedAStatus::ok();
    }

    ScopedAStatus complexType(const ComplexType& in_aComplexObject,
                              std::string* _aidl_return) override
    {
        char strBuf[1024];

        snprintf(strBuf, 1024,
                 "int=%d, long=%ld, bool=%d, float=%f, double=%lf, string=%s",
                 in_aComplexObject.i_Int,
                 in_aComplexObject.l_Long,
                 in_aComplexObject.b_Boolean,
                 in_aComplexObject.f_Float,
                 in_aComplexObject.d_Double,
                 in_aComplexObject.s_String.c_str());

        *_aidl_return = std::string(strBuf);

        return ScopedAStatus::ok();
    }

    ...
};
```

[Common/src/main/cpp/includes/ComplexType.h](Common/src/main/cpp/includes/ComplexType.h)

```c++
#include <android/binder_status.h>

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
```

Service implementation.

[NdkBinderService/src/main/java/com/example/ndkbinderservice/MyService.java](NdkBinderService/src/main/java/com/example/ndkbinderservice/MyService.java)

```java
package com.example.ndkbinderservice;

public class MyService extends Service
{
    static
    {
        System.loadLibrary("native-lib");
    }

    private IBinder mBinder;

    @Override
    public void onCreate()
    {
        super.onCreate();

        mBinder = createServiceBinder();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    public native IBinder createServiceBinder();
}
```
