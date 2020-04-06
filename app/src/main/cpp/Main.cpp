#include <jni.h>
#include <string>
#include <stdlib.h>
#include <android/log.h>
#include <cxxabi.h>
#include <dlfcn.h>
#include "include/Utils.h"
#include "include/xhook.h"
#include "include/hookzz.h"

#define LOG_TAG "Fuck"
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG_TAG,__VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

JNIEXPORT jint

JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    jint result = JNI_ERR;
    jint version = 0;
    JNIEnv *env = 0;
    do {
        if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) == JNI_OK) {
            version = JNI_VERSION_1_6;
        } else if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) == JNI_OK) {
            version = JNI_VERSION_1_4;
        } else if (vm->GetEnv((void **) &env, JNI_VERSION_1_2) == JNI_OK) {
            version = JNI_VERSION_1_2;
        } else if (vm->GetEnv((void **) &env, JNI_VERSION_1_1) == JNI_OK) {
            version = JNI_VERSION_1_1;
        } else {
            break;
        }
        result = version;

    } while (0);
    LOGD("version=%d", result);
    //native层 loadlibrary
    if (!getLibraryMap(getSoName().c_str()).isValid()) {
        char nativeLibPath[1024] = {0};
        sprintf(nativeLibPath, "/data/data/%s/lib/%s", getProcName().c_str(), getSoName().c_str());
        void *handle = dlopen(nativeLibPath, RTLD_NOW);
        if (handle) {
            void *sym = dlsym(handle, "JNI_OnLoad");
            if (sym == nullptr) {
                LOGD("sym is null");
            } else {
                typedef int (*JNI_OnLoadFn)(JavaVM *, void *);
                JNI_OnLoadFn jni_on_load = reinterpret_cast<JNI_OnLoadFn>(sym);
                int version = (*jni_on_load)(vm, nullptr);
            }
        } else {
            LOGD("handle is null");
        }
    }
    return result;
}

int (*__kill)(pid_t __pid, int __signal);

int $__kill(pid_t pid, int sig) {
    LOGD("pid=%d,sig=%d", pid, sig);
//    if (sig == 9) {
//        pthread_exit(NULL);
//        return 0;
//    }
    return __kill(pid, sig);
}


__attribute__((constructor)) void entry() {

    LOGD("inject success!!");
    xhook_register(".*\\.so$", "kill", (void *) $__kill, (void **) &__kill);
    xhook_refresh(0);
}
