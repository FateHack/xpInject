//
// Created by Fate on 2020-04-06.
//

#include "include/MemScanner.h"
#include "include/Utils.h"
#include <dlfcn.h>
#include <fcntl.h>
#include <sys/types.h>
#include <unistd.h>
#include "include/Log.h"

int handle = -1;//文件句柄
int MemorySearchRange;//搜索内存范围

//初始化
void Init() {
    char *memPath = "/proc/self/mem"; //mem路径
    handle = open(memPath, O_RDWR); //读写权限
    if (handle == -1)
        LOGE("open failed!!");
    else
        lseek(handle, 0, SEEK_SET);
}

//设置搜索范围
void SetSearchRange(int type) {
    MemorySearchRange = type;
}

void ReadMaps() {
    const char *mapsPath = "/proc/self/maps";
    FILE *stream=fopen(mapsPath,"r");

}