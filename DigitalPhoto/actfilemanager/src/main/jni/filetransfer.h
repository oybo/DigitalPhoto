//
// Created by yuchen on 2016/5/25.
//

#ifndef FILETRANSFER_H
#define FILETRANSFER_H


#include <pthread.h>
#include "curl/curl.h"
#include "common.h"
typedef struct {
    int mInited;
    pthread_t mTaskRunner;
    pthread_cond_t mOnNewRequestCond;
    pthread_mutex_t mOnNewRequestMutex;
    pthread_mutex_t mReqListMutex;
    int mAbort;

    CURL* mCurlHandle;
    char mIp[16];
    int mPendingReqNum;
    RequestOpt* mReqList[MAX_REQUESTS];

    FILE* mCurrentFile;
    off_t mCurrentTotalSize;
    off_t mCurrentProcessed;
    int mCurrentOpType;
    char mCurrentUrl[MAX_URL_LENGTH];
    char mCurrentPath[MAX_PATH_LENGTH];

    //fixme: need a lock here to protect mReqList

}FileTransferManager;

int fileTransferInit(const char* ip);
int fileTranserCleanup();
int fileTransferUpload(const char* file_path,const char* remote_path);
int fileTransferDownload(const char* url, const char* path);
int fileTransferCleanup();
#endif //FILETRANSFER_H
