# Android NDK (C++) Binder Examples

[![Android](https://img.shields.io/badge/Android-36-green.svg)](https://developer.android.com)
[![NDK](https://img.shields.io/badge/NDK-26+-blue.svg)](https://developer.android.com/ndk)
[![Gradle](https://img.shields.io/badge/Gradle-8.13-brightgreen.svg)](https://gradle.org)
[![AGP](https://img.shields.io/badge/AGP-8.13.1-blue.svg)](https://developer.android.com/studio/releases/gradle-plugin)

A comprehensive Android Studio project demonstrating inter-process communication (IPC) using Android Binder with C++ NDK APIs. This project showcases two fundamental scenarios for building high-performance services that bridge Java and native C++ code.

## 📖 What You'll Learn

This tutorial demonstrates:

1. ✅ **How to implement an Android Service in C++** using NDK Binder APIs
2. ✅ **How to call an Android Service from C++** JNI layer
3. ✅ **AIDL interface definitions** for cross-language communication
4. ✅ **Complex data type marshalling** between Java and C++
5. ✅ **Service binding patterns** for IPC communication
6. ✅ **Modern Android development** with latest build tools and testing practices

## 🏗️ Project Structure

```
AndroidNdkBinderExamples/
├── Common/                    # Shared AIDL definitions and data types
│   ├── src/main/aidl/        # AIDL interface definitions
│   │   ├── IMyService.aidl   # Service interface
│   │   └── ComplexType.aidl  # Custom parcelable type
│   ├── src/main/cpp/         # C++ implementations
│   │   └── includes/         # C++ headers (ComplexType.h)
│   ├── src/main/java/        # Java data classes
│   └── src/test/java/        # Unit tests
│
├── NdkBinderService/         # C++ Binder service implementation
│   ├── src/main/cpp/         # Native service implementation
│   └── src/main/java/        # Java wrapper for C++ service
│
├── JavaBinderService/        # Java Binder service implementation
│   ├── src/main/java/        # Pure Java service
│   ├── src/test/java/        # Unit tests
│   └── src/androidTest/      # Instrumentation tests
│
├── NdkBinderClient/          # C++ client for Java service
│   ├── src/main/cpp/         # Native client implementation
│   └── src/main/java/        # Activity that delegates to C++
│
└── JavaBinderClient/         # Java client for C++ service
    └── src/main/java/        # Pure Java client Activity
```

## 🎯 Two Main Examples

### Example 1: C++ Service + Java Client

**Demonstrates:** Implementing a high-performance service in C++ and consuming it from Java

```
┌─────────────────┐         ┌──────────────────┐
│ JavaBinderClient│  Binder │ NdkBinderService │
│   (Java App)    │ ◄─────► │   (C++ Service)  │
└─────────────────┘   IPC   └──────────────────┘
```

### Example 2: Java Service + C++ Client

**Demonstrates:** Calling a Java service from native C++ code via JNI

```
┌──────────────────┐        ┌──────────────────┐
│ NdkBinderClient  │ Binder │ JavaBinderService│
│  (C++ via JNI)   │◄─────► │   (Java Service) │
└──────────────────┘  IPC   └──────────────────┘
```

## ⚙️ Prerequisites

Before you begin, ensure you have:

- **Android Studio**: Hedgehog (2023.1.1) or later
- **Android SDK**: API Level 36 (Android 15)
- **Android NDK**: Version 26.1.10909125 or later
- **JDK**: Version 17
- **CMake**: Version 3.22.1+ (install via SDK Manager)
- **Gradle**: 8.13+ (included via wrapper)

### Initial Setup

1. **Create `local.properties`** in the project root with your SDK and JDK paths:

```properties
sdk.dir=/path/to/your/Android/Sdk
org.gradle.java.home=/path/to/your/jdk-17
```

2. **Sync Project** in Android Studio (File → Sync Project with Gradle Files)

## 🚀 Building the Project

### Quick Build Commands

```bash
# Clean build all modules
./gradlew clean assembleDebug -x validateSigningDebug -x lint

# Build specific module
./gradlew NdkBinderService:assembleDebug -x validateSigningDebug

# Incremental build (without clean)
./gradlew assembleDebug -x validateSigningDebug
```

**Note:** We skip `validateSigningDebug` due to a known JVM compatibility issue with AGP 8.13.1 and Java 17. This doesn't affect APK generation.

### Build Output

After successful build, you'll find APKs in:
```
<module>/build/outputs/apk/debug/<module>-debug.apk
```

## 📱 Example 1: C++ Service with Java Client

This example shows how to implement a high-performance Android Service in C++ and consume it from a Java client application.

### Step 1: Build and Install the C++ Service

```bash
# Build the NDK Binder Service
./gradlew NdkBinderService:assembleDebug -x validateSigningDebug

# Install on device/emulator
adb install -f NdkBinderService/build/outputs/apk/debug/NdkBinderService-debug.apk
```

### Step 2: Build and Install the Java Client

```bash
# Build the Java client
./gradlew JavaBinderClient:assembleDebug -x validateSigningDebug

# Install on device/emulator
adb install -f JavaBinderClient/build/outputs/apk/debug/JavaBinderClient-debug.apk
```

### Step 3: Run the Example

1. Launch the **JavaBinderClient** app from your device's app launcher
2. The app will automatically bind to the C++ service
3. Watch the logcat output to see the IPC communication:

```bash
adb logcat -s ndkbinderexamples:D
```

### How It Works

**Service Side (C++):**

The service is implemented in pure C++ using NDK Binder APIs:

```cpp
// NdkBinderService/src/main/cpp/MyService.cpp
class MyService : public BnMyService
{
public:
    ScopedAStatus basicTypes(int32_t in_anInt, int64_t in_aLong,
                             bool in_aBoolean, float in_aFloat,
                             double in_aDouble,
                             const std::string& in_aString) override
    {
        // Service implementation in C++
        return ScopedAStatus::ok();
    }

    ScopedAStatus complexType(const ComplexType& in_aComplexObject,
                              std::string* _aidl_return) override
    {
        // Process complex object in C++
        *_aidl_return = formatComplexType(in_aComplexObject);
        return ScopedAStatus::ok();
    }
};
```

**Java Wrapper:**

A thin Java wrapper loads the native library and exposes the C++ service:

```java
// NdkBinderService/src/main/java/.../MyService.java
public class MyService extends Service {
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return createServiceBinder(); // Native method
    }

    public native IBinder createServiceBinder();
}
```

**Client Side (Java):**

The Java client binds to the service using standard Android APIs:

```java
// JavaBinderClient/src/main/java/.../MainActivity.java
Intent intent = new Intent();
intent.setClassName("com.example.ndkbinderservice",
                   "com.example.ndkbinderservice.MyService");
bindService(intent, this, BIND_AUTO_CREATE);
```

## 📱 Example 2: Java Service with C++ Client

This example demonstrates calling a Java service from native C++ code through the JNI layer.

### Step 1: Build and Install the Java Service

```bash
# Build the Java service
./gradlew JavaBinderService:assembleDebug -x validateSigningDebug

# Install on device/emulator
adb install -f JavaBinderService/build/outputs/apk/debug/JavaBinderService-debug.apk
```

### Step 2: Build and Install the C++ Client

```bash
# Build the NDK client
./gradlew NdkBinderClient:assembleDebug -x validateSigningDebug

# Install on device/emulator
adb install -f NdkBinderClient/build/outputs/apk/debug/NdkBinderClient-debug.apk
```

### Step 3: Run the Example

1. Launch the **NdkBinderClient** app from your device's app launcher
2. The app binds to the Java service and communicates via C++
3. Monitor the communication in logcat:

```bash
adb logcat -s ndkbinderexamples:D
```

### How It Works

**Service Side (Java):**

A standard Java service implementing the AIDL interface:

```java
// JavaBinderService/src/main/java/.../MyService.java
private class MyServiceBinder extends IMyService.Stub {
    @Override
    public void basicTypes(int anInt, long aLong, boolean aBoolean,
                          float aFloat, double aDouble, String aString) {
        // Java implementation
    }

    @Override
    public String complexType(ComplexType aComplexObject) {
        // Process complex object in Java
        return aComplexObject.toString();
    }
}
```

**Client Side (C++):**

The IBinder received from Java is passed to C++ where all communication happens:

```cpp
// NdkBinderClient/src/main/cpp/native-lib.cpp
std::shared_ptr<IMyService> g_spMyService;

extern "C" JNIEXPORT void JNICALL
Java_com_example_ndkbinderclient_MainActivity_onServiceConnected(
        JNIEnv* env, jobject, jobject binder)
{
    AIBinder* pBinder = AIBinder_fromJavaBinder(env, binder);
    const ::ndk::SpAIBinder spBinder(pBinder);
    g_spMyService = IMyService::fromBinder(spBinder);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_ndkbinderclient_MainActivity_talkToService(
        JNIEnv* env, jobject)
{
    // Call service methods from C++
    ScopedAStatus result = g_spMyService->basicTypes(
        2021, 65535000, true, 3.14f, 3.141592653589793, "Hello from C++!");

    // ... process results
}
```

## 🔧 AIDL Interface Definitions

### Service Interface

The AIDL interface defines the contract between service and client:

```java
// Common/src/main/aidl/com/example/IMyService.aidl
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

### Custom Parcelable Type

For NDK usage, we define a parcelable with a C++ header:

```java
// Common/src/main/aidl/com/example/ComplexType.aidl
package com.example;

parcelable ComplexType ndk_header "ComplexType.h";
```

**Note:** We use `ndk_header` (not `cpp_header`) for NDK 26+ compatibility.

### C++ Implementation

The C++ header implements the parcelable protocol:

```cpp
// Common/src/main/cpp/includes/ComplexType.h
#include <string>
#include <android/binder_parcel.h>
#include <android/binder_parcel_utils.h>

class ComplexType
{
public:
    int i_Int;
    long l_Long;
    bool b_Boolean;
    float f_Float;
    double d_Double;
    std::string s_String;

    binder_status_t readFromParcel(const AParcel* pParcel) {
        // Read all fields from parcel
        AParcel_readInt32(pParcel, &i_Int);
        // ... read other fields
        return STATUS_OK;
    }

    binder_status_t writeToParcel(AParcel* pParcel) const {
        // Write all fields to parcel
        AParcel_writeInt32(pParcel, i_Int);
        // ... write other fields
        return STATUS_OK;
    }
};
```

## 🔨 Build System Details

### AIDL Code Generation

The project uses a custom Gradle task to generate NDK-compatible C++ code from AIDL files:

```gradle
// Common/build.gradle
tasks.register('compileAidlNdk') {
    doLast {
        // Auto-detect latest build tools
        def buildToolsVersion = findLatestBuildTools()
        def aidl = "${android.sdkDirectory}/build-tools/${buildToolsVersion}/aidl"

        // Generate C++ code
        exec {
            executable(aidl)
            args('--lang=ndk',
                 '-o', cppOutDir,
                 '-h', headerOutDir,
                 '-I', searchPath,
                 aidlFile)
        }

        // Auto-fix for NDK 26+ API changes
        fixGeneratedCodeForNDK26Plus()
    }
}
```

### NDK 26+ Compatibility Fixes

The build system automatically fixes API incompatibilities in AIDL-generated code:

1. **API Method Rename**: `asBinderReference()` → `asBinder()`
2. **defineClass Signature**: Simplified from 4 arguments to 2 arguments

These fixes are applied automatically during the build process, ensuring compatibility with NDK 26 and later.

### CMake Configuration

Native code is built using CMake. The configuration is minimal:

```cmake
# NdkBinderService/src/main/cpp/CMakeLists.txt
cmake_minimum_required(VERSION 3.22.1)

add_library(native-lib SHARED
        native-lib.cpp
        MyService.cpp)

target_link_libraries(native-lib
        android
        log)
```

**Note:** We no longer hardcode the CMake version; it uses the SDK-provided version automatically.

## 🧪 Testing

The project includes comprehensive test coverage for all components.

### Unit Tests

Run all unit tests:

```bash
./gradlew test
```

Run tests for a specific module:

```bash
./gradlew JavaBinderService:testDebugUnitTest
```

**Test Coverage:**

- **Common Module**: 15 unit tests
  - `ComplexTypeTest`: Parcelable implementation, boundary values, null handling
  - `ConstantsTest`: Constant validation

- **JavaBinderService**: 13 unit tests
  - Service lifecycle, binder methods, AIDL implementation, edge cases

**Framework:** JUnit 4.13.2 + Robolectric 4.16 (for Android framework mocking)

### Instrumentation Tests

Instrumentation tests run on an actual Android device/emulator.

**Important:** Due to a known AGP/Java 17 compatibility issue, instrumentation tests must be run manually:

```bash
# 1. Build and install the app APK
./gradlew JavaBinderService:assembleDebug -x validateSigningDebug
adb install -r JavaBinderService/build/outputs/apk/debug/JavaBinderService-debug.apk

# 2. Build and install the test APK
./gradlew JavaBinderService:assembleDebugAndroidTest -x validateSigningDebug
adb install -r JavaBinderService/build/outputs/apk/androidTest/debug/JavaBinderService-debug-androidTest.apk

# 3. Run the tests
adb shell am instrument -w com.example.javabinderservice.test/androidx.test.runner.AndroidJUnitRunner
```

**Test Coverage:**

- **JavaBinderService**: 10 instrumentation tests
  - Real Android runtime service binding
  - IPC communication testing
  - Service connection lifecycle
  - Integration scenarios

### Test Coverage Reports

Generate coverage reports with JaCoCo:

```bash
./gradlew jacocoTestReport
```

View reports at: `build/reports/jacoco/jacocoTestReport/html/index.html`

## 🔍 Understanding the Key Concepts

### 1. NDK Binder APIs

Android's Binder IPC mechanism is traditionally used from Java, but NDK Binder APIs (available since Android 10 / API 29) allow C++ code to participate directly in Binder communication.

**Key Classes:**
- `AIBinder`: Native IBinder representation
- `ScopedAStatus`: Status codes for binder calls
- `BnMyService`: Native service stub (generated from AIDL)
- `BpMyService`: Native service proxy (generated from AIDL)

### 2. AIDL (Android Interface Definition Language)

AIDL defines the programming interface for IPC. The `aidl` compiler generates:
- **For Java**: Stub and Proxy classes
- **For NDK**: C++ headers and implementation stubs

### 3. Parcelable Data

For complex data types to cross the IPC boundary, they must be parcelable:
- **Java**: Implement `Parcelable` interface
- **C++**: Implement `readFromParcel()` and `writeToParcel()` methods

### 4. Service Binding

Both examples use the standard Android service binding pattern:
1. Client calls `bindService()` with an `Intent`
2. System calls service's `onBind()` returning an `IBinder`
3. Client's `onServiceConnected()` receives the `IBinder`
4. Client can now make IPC calls through the binder

## ⚠️ Known Limitations & Workarounds

### 1. validateSigningDebug Task

**Issue:** JvmWideVariable initialization error with AGP 8.13.1 and Java 17

**Workaround:** Skip the task during build:
```bash
./gradlew assembleDebug -x validateSigningDebug
```

**Impact:** None on APK generation; only affects debug signing validation

### 2. Lint Tasks (Optional)

**Issue:** Some lint tasks may have compatibility issues

**Workaround:** Skip lint for faster builds:
```bash
./gradlew assembleDebug -x validateSigningDebug -x lint
```

**Impact:** APK builds work perfectly; only static analysis is affected

### 3. Connected Android Test Task

**Issue:** `connectedAndroidTest` fails due to JvmWideVariable issue

**Workaround:** Use manual APK installation method (shown in Testing section)

**Impact:** Tests work perfectly when run manually

## 🐛 Troubleshooting

### Build fails with "Unsupported class file major version 61"

**Cause:** Java 17 incompatible with Gradle version

**Solution:** Ensure Gradle 8.13+ is being used (check `gradle/wrapper/gradle-wrapper.properties`)

### Build fails with "CMake not found"

**Cause:** CMake not installed via SDK Manager

**Solution:**
1. Open Android Studio → SDK Manager
2. Go to SDK Tools tab
3. Check "CMake" and click Apply

### Build fails with "asBinderReference not declared"

**Cause:** AIDL generated code not fixed for NDK 26+

**Solution:** Ensure `Common/build.gradle` includes the auto-fix code in the `compileAidlNdk` task

### Build fails with "android:exported needs to be specified"

**Cause:** Android 12+ requires explicit `android:exported` for components with intent filters

**Solution:** This is already fixed in the manifests. If you see this error, check that `AndroidManifest.xml` files have `android:exported="true"` on all `<activity>` tags with intent filters.

### Tests fail with "Android SDK 36 requires Java 21"

**Cause:** Robolectric 4.16 requires Java 21 for SDK 36, but we use Java 17

**Solution:** Already configured! Check that `Common/src/test/resources/robolectric.properties` exists with `sdk=34`

## 🎓 Learning Resources

### Official Documentation

- [Android NDK Binder](https://developer.android.com/ndk/reference/group/ndk-binder)
- [AIDL Overview](https://developer.android.com/guide/components/aidl)
- [Android Services](https://developer.android.com/guide/components/services)
- [JNI Tips](https://developer.android.com/training/articles/perf-jni)

### Related Topics

- Binder IPC internals
- Native service development
- Cross-language data marshalling
- Android service lifecycle
- Multi-process architecture

## 📊 Technical Specifications

| Component | Version | Purpose |
|-----------|---------|---------|
| Gradle | 8.13 | Build system |
| Android Gradle Plugin | 8.13.1 | Android build support |
| Android SDK | 36 (API Level 36) | Target platform |
| NDK | 26.1.10909125+ | Native development |
| CMake | 3.22.1+ | Native build system |
| Java | 17 | JVM language |
| C++ Standard | C++17 | Native language |
| Min SDK | 29 (Android 10) | Minimum supported version |

### Dependency Versions

```gradle
// AndroidX
androidx.appcompat:appcompat:1.7.1
com.google.android.material:material:1.13.0
androidx.constraintlayout:constraintlayout:2.2.1

// Testing
junit:junit:4.13.2
org.mockito:mockito-core:5.20.0
org.robolectric:robolectric:4.16
androidx.test.ext:junit:1.3.0
androidx.test.espresso:espresso-core:3.7.0
androidx.test:runner:1.7.0

// Coverage
org.jacoco:jacoco:0.8.14
```

## 🤝 Contributing

Contributions are welcome! When contributing:

1. ✅ Write unit tests for all new Java code
2. ✅ Add instrumentation tests for service/IPC changes
3. ✅ Maintain or improve test coverage
4. ✅ Follow existing code style and conventions
5. ✅ Update documentation for new features
6. ✅ Ensure all tests pass before submitting

## 📝 License

This project is provided as-is for educational purposes. Feel free to use it as a reference for your own Android NDK Binder implementations.

## 🙏 Acknowledgments

This project serves as a practical reference for Android developers working with:
- NDK Binder APIs
- Inter-process communication (IPC)
- JNI integration
- AIDL interfaces
- Native Android services

**Last Updated:** November 2025
**Maintained For:** Android 15 (API 36) and NDK 26+
**Build Status:** ✅ Fully Working with Comprehensive Test Coverage

---

**Happy Coding!** 🚀 If you find this project helpful, please give it a ⭐ on GitHub!
