# Android NDK (C++) Binder Examples

## How to run NDK Binder service

1. Build and install [NdkBinderService](NdkBinderService/) APK. It contains an Android Service, whose binder implementation is done in C++ JNI layer using NDK Binder APIs.

2. Build and install [JavaBinderClient](JavaBinderClient/) APK. It contains an Android Activity, who binds the Service from `NdkBinderService` and talks to Service using Java Binder APIs.

3. Run `JavaBinderClient`'s main Activity.

## How to run NDK Binder client

1. Build and install [JavaBinderService](JavaBinderService/) APK. It contains an Android Service implemented in Java.

2. Build and install [NdkBinderClient](NdkBinderClient/) APK. It contains an Android Activity, who binds the Service from `JavaBinderService` and passes the IBinder object to C++ JNI layer to talk to the Service using NDK Binder APIs.

3. Run `NdkBinderClient`'s main Activity.

## NDK Binder service implementation details

[NdkBinderService](NdkBinderService/) : Android app (APK) module, containing a Java Service ([MyService.java](NdkBinderService/src/main/java/com/example/ndkbinderservice/MyService.java)) that implements an AIDL ([IMyService.aidl](Common/src/main/aidl/com/example/IMyService.aidl)). Implementation (Binder native) is done in C++ JNI layer ([MyService.cpp](NdkBinderService/src/main/cpp/MyService.cpp)).

AIDL

[Common/src/main/aidl/com/example/IMyService.aidl](Common/src/main/aidl/com/example/IMyService.aidl)

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

[Common/src/main/aidl/com/example/ComplexType.aidl](Common/src/main/aidl/com/example/ComplexType.aidl)

```java
package com.example;

parcelable ComplexType cpp_header "ComplexType.h";
```

There is a Gradle task (`compileAidlNdk`) to auto-generate NDK C++ binder source files.

[Common/build.gradle](Common/build.gradle)

```gradle
plugins {
    id 'com.android.library'
}

android {
    compileSdkVersion 30
    buildToolsVersion '29.0.3'
    
    defaultConfig {
        minSdkVersion 29
        
        externalNativeBuild {
            cmake {
                cppFlags "-std=c++17"
            }
        }
    }
    
    externalNativeBuild {
        cmake {
            path "src/main/cpp/CMakeLists.txt"
            version "3.10.2"
        }
    }
    
    ...
}

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

Service's JNI library and NDK Binder implementations.

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

## NDK Binder client implementation details

[NdkBinderClient](NdkBinderClient/) : Android app (APK) module, containing a Java Activity ([MainActivity.java](NdkBinderClient/src/main/java/com/example/ndkbinderclient/MainActivity.java)) that binds an Android Service. IBinder object received onServiceConnection is passed to a C++ JNI layer ([native-lib.cpp](NdkBinderClient/src/main/cpp/native-lib.cpp)), and communication with the service happens in JNI layer.

[NdkBinderClient/src/main/java/com/example/ndkbinderclient/MainActivity.java](NdkBinderClient/src/main/java/com/example/ndkbinderclient/MainActivity.java)

```java
public class MainActivity extends AppCompatActivity implements ServiceConnection
{
    static
    {
        System.loadLibrary("native-lib");
    }

    private volatile boolean mIsServiceConnected = false;
    private final ConditionVariable mServiceConnectionWaitLock = new ConditionVariable();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Intent intent = new Intent();
        intent.setClassName("com.example.javabinderservice",
                "com.example.javabinderservice.MyService");

        bindService(intent, this, BIND_AUTO_CREATE);

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                // Not connected to service yet?
                while(!mIsServiceConnected)
                {
                    mServiceConnectionWaitLock.block(); // waits for service connection
                }

                talkToService();
            }
        }).start();
    }

    @Override
    protected void onPause()
    {
        unbindService(this);

        mIsServiceConnected = false;

        onServiceDisconnected();

        super.onPause();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder)
    {
        onServiceConnected(iBinder);

        mIsServiceConnected = true;

        mServiceConnectionWaitLock.open(); // breaks service connection waits
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName)
    {
        mIsServiceConnected = false;

        onServiceDisconnected();
    }

    public native void onServiceConnected(IBinder binder);
    public native void onServiceDisconnected();
    public native String talkToService();
}
```

JNI library implementation.

[NdkBinderClient/src/main/cpp/native-lib.cpp](NdkBinderClient/src/main/cpp/native-lib.cpp)

```c++
#include <jni.h>
#include <aidl/com/example/IMyService.h>
#include <android/binder_ibinder_jni.h>

std::shared_ptr<IMyService> g_spMyService;

extern "C" JNIEXPORT void JNICALL
Java_com_example_ndkbinderclient_MainActivity_onServiceConnected(
        JNIEnv* env,
        jobject /* this */,
        jobject binder)
{
    AIBinder* pBinder = AIBinder_fromJavaBinder(env, binder);

    const ::ndk::SpAIBinder spBinder(pBinder);
    g_spMyService = IMyService::fromBinder(spBinder);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_ndkbinderclient_MainActivity_onServiceDisconnected(
        JNIEnv* env,
        jobject /* this */)
{
    g_spMyService = nullptr;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_ndkbinderclient_MainActivity_talkToService(
        JNIEnv* env,
        jobject /* this */)
{
    ScopedAStatus basicTypesResult = g_spMyService->basicTypes(2021, 65535000,
            true, 3.14f, 3.141592653589793238, "Hello, World!");

    if(basicTypesResult.isOk())
    {
        ...
    }
    else
    {
        ...
    }

    ComplexType ct(2021, 65535000, true, 3.14f,3.141592653589793238,
            "Hello, World!");

    std::string sReturnedString;

    ScopedAStatus complexTypeResult = g_spMyService->complexType(ct, &sReturnedString);

    if(complexTypeResult.isOk())
    {
        ...
    }
    else
    {
        ...
    }

    ComplexType returnedComplexObject;

    ScopedAStatus returnComplexTypeResult = g_spMyService->returnComplexType(2021,
            65535000, true, 3.14f, 3.141592653589793238,
            "Hello, World!", &returnedComplexObject);

    if(returnComplexTypeResult.isOk())
    {
        ...
    }
    else
    {
        ...
    }

    std::string sRet;
    returnedComplexObject.toString(&sRet);

    return env->NewStringUTF(sRet.c_str());
}
```
