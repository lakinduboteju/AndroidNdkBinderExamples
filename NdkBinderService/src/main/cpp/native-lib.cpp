#include <jni.h>
#include "MyService.h"

#include <android/binder_ibinder_jni.h>

using aidl::com::example::MyService;
using namespace std;

extern "C" JNIEXPORT jobject JNICALL
Java_com_example_ndkbinderservice_MyService_createServiceBinder(
        JNIEnv* env,
        jobject /* this */)
{
    static MyService myService;
    return env->NewGlobalRef(AIBinder_toJavaBinder(env, myService.asBinder().get()));
}
