//
// Created by Fate on 2020-04-06.
//

#ifndef MEMSCANNER_H
#define MEMSCANNER_H

#endif


#include <stdio.h>

struct MAPS {
    long int addr;       //起始地址
    long int taddr;      //结束地址
    struct MAPS *next;   //指向下一节点的指针
};

struct FREEZE {
    long int addr;       //地址
    char *value;         //值
    int type;            //类型
    struct FREEZE *next; //指向下一节点的指针
};

struct RESULTS {
    long int addr;       //地址
    void *value;         //值
    int type;            //类型
    struct RESULTS *next;//指针
};

struct RESULTSINFO {
    long int addr;       //地址
    void *value;         //值
    int type;            //类型
};

struct ADDRESSINFO {
    long int addr;
    void *value;
};

#define LEN sizeof(struct MAPS)
#define RES sizeof(struct RESULTS)
#define FRE sizeof(struct FREEZE)

typedef struct MAPS *PMAPS;                   //存储maps的链表
typedef struct FREEZE *PFREEZE;               //存储冻结的数据的链表
typedef struct RESULTS *PRES;                 //存放结果
typedef struct RESULTSINFO RESULTSINFO;       //结果信息
typedef RESULTSINFO SETINFO;                  //设置信息
typedef ADDRESSINFO ADDRESSINFO;              //地址信息

typedef int TYPE;
typedef int RANGE;
typedef int COUNT;
typedef int COLOR;
typedef int64_t OFFSET;
typedef int64_t ADDRESS;
typedef char PACKAGENAME;

enum type {
    DWORD,
    FLOAT,
    DOUBLE,
    WORD,
    BYTE,
    QWORD
};

enum Range {
    ALL,        //所有内存
    B_BAD,      //B内存
    C_ALLOC,    //Ca内存
    C_BSS,      //Cb内存
    C_DATA,     //Cd内存
    C_HEAP,     //Ch内存
    JAVA_HEAP,  //Jh内存
    A_ANONMYOUS,//A内存
    CODE_SYSTEM,//Xs内存
    STACK,      //S内存
    ASHMEM      //As内存
};

#ifdef __cplusplus
extern "C" {
void Init();//初始化
void SetSearchRange(int type);//设置搜索范围
void BaseAddressSearch(char *value, int type, long int BaseAddr);//基址搜索
void MemorySearch(char *value, TYPE type);//内存搜索
void MemoryOffset(char *value, int64_t offset, int type);//搜索偏移
void MemoryWrite(char *value, long int offset, int type);//内存写入
int64_t GetResultCount();//获取结果数量
void GetResultsInfo(int count, RESULTSINFO *r);//获取结果数据
void GetAddressInfo(int64_t addr, ADDRESSINFO *a);//获取地址信息
void SetValue(SETINFO s);//写内存
void PrintMaps(PMAPS pMap);//打印pMaps里面的内容
void PrintResults();//打印结果
void ClearResults();//清除链表,释放空间
int AddFreezeItem_All(char *Value, int type, long int offset);//冻结所有结果
int AddFreezeItem(long int addr, char *value, int type, long int offset);//增加冻结数据
int RemoveFreezeItem(long int addr);//清除固定冻结数据
int RemoveFreezeItem_All();//清空所有冻结数据
int StartFreeze();//开始冻结
int StopFreeze();//停止冻结
int SetFreezeDelay(long int De);//设置冻结延迟
int PrintFreezeItems();//打印冻结表

#ifdef __cplusplus
}
#endif

#endif