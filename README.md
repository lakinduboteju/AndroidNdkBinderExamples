# Android NDK (C++) Binder Examples

## NDK Binder Service

AIDL

`Common/aidl/com/example/IMyService.aidl`

```java
package com.example;

import com.example.ComplexType;

interface IMyService {
    void basicTypes(int anInt, long aLong, boolean aBoolean,
                    float aFloat, double aDouble, String aString);
    
    String complexType(in ComplexType aComplexObject);
}
```

Gradle task to auto-generate NDK C++ binder source files.

`NdkBinderService/build.gradle`

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

        def searchPathForImports = [rootDir.absolutePath,
                                    'Common', 'aidl'].join(File.separator)

        def aidlFile = [rootDir.absolutePath, 'Common', 'aidl',
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

`NdkBinderService/src/main/cpp/native-lib.cpp`

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

`NdkBinderService/src/main/cpp/MyService.cpp`

```c++
#include "aidl/com/example/BnMyService.h"

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
};
```

`NdkBinderService/src/main/cpp/includes/ComplexType.h`

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

`NdkBinderService/src/main/java/com/example/ndkbinderservice/MyService.java`

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
