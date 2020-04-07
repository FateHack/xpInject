#include <jni.h>
#include <string>
#include <stdlib.h>
#include <cxxabi.h>
#include <dlfcn.h>
#include <hookzz.h>
#include "include/Utils.h"
#include "include/xhook.h"
#include "include/hookzz.h"
#include "include/Log.h"
#include <sys/ptrace.h>
#include <unistd.h>

#if !defined(PT_DENY_ATTACH)
#define PT_DENY_ATTACH 31
#endif
#if !defined(SYS_ptrace)
#define SYS_ptrace 26
#endif
#if !defined(SYS_syscall)
#define SYS_syscall 0
#endif
#if !defined(SYS_sysctl)
#define SYS_sysctl 202
#endif

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
//    if (!getLibraryMap(getSoName().c_str()).isValid()) {
//        char nativeLibPath[1024] = {0};
//        sprintf(nativeLibPath, "/data/data/%s/lib/%s", getProcName().c_str(), getSoName().c_str());
//        void *handle = dlopen(nativeLibPath, RTLD_NOW);
//        if (handle) {
//            void *sym = dlsym(handle, "JNI_OnLoad");
//            if (sym == nullptr) {
//                LOGD("sym is null");
//            } else {
//                typedef int (*JNI_OnLoadFn)(JavaVM *, void *);
//                JNI_OnLoadFn jni_on_load = reinterpret_cast<JNI_OnLoadFn>(sym);
//                int version = (*jni_on_load)(vm, nullptr);
//            }
//        } else {
//            LOGD("handle is null");
//        }
//    }
    return result;
}

int (*__kill)(pid_t __pid, int __signal);

int $__kill(pid_t pid, int sig) {
    LOGD("pid=%d,sig=%d", pid, sig);
    if (sig == 9) {
        pthread_exit(NULL);
    }
    return __kill(pid, sig);
}


void (*ori_exit)(int status);

void fake_exit(int status) {
    LOGD("status=%d", status);
    ori_exit(status);
}

void kill_pre_call(RegState *rs, ThreadStack *ts, CallStack *cs, const HookEntryInfo *info) {
    // uint32_t base = (uint32_t) getLibraryMap("libcrackme.so").startAddr;
    // LOGD("off--%p", (void *) (rs->lr - base));
}


__attribute__((constructor)) void entry() {

    LOGD("inject success!!");
    ZzHookReplace((void *) kill, (void *) $__kill, (void **) &__kill);
    ZzHookReplace((void *) exit, (void *) fake_exit, (void **) &ori_exit);
    ZzHookPrePost((void *) kill, kill_pre_call, NULL);

}
