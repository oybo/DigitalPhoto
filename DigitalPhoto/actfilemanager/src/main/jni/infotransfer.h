//
// Created by yuchen on 2016/5/25.
//

#ifndef INFOTRANSFER_H
#define INFOTRANSFER_H

#include <pthread.h>
#include "curl/curl.h"
#include "common.h"
typedef struct {
    int mInited;
    pthread_t mTaskRunner;
    pthread_cond_t mOnNewRequestCond;
    pthread_mutex_t mOnNewRequestMutex;
    int mAbort;

    CURL* mCurlHandle;  //info handle
    char mIp[16];
    int mPendingReqNum;
    RequestOpt* mReqList[MAX_REQUESTS];

    int mCurrentOpType;
    char mCurrentUrl[MAX_URL_LENGTH];
    char mCurrentPath[MAX_PATH_LENGTH];

    //used to store response from browse request,every single element malloced after browse cmd,
    //fixme: but when to release
    FileInfo *mListData;//[MAX_FILES_IN_DIR];
    int mFileCount;

    char *mNetBuffer;
    int mBufferSize;
    int mDataSize;
}InfoTransferManager;

int infoTransferInit(const char* ip);
int infioTransferCleanup();
int infoTransferDelete(const char* url);
int infoTransferDeleteDirectory(const char *dir_path);
int infoTransferCreateDirectory(const char *remote_path);
int infoTransferListInfo(const char *dirpath);
int infoTransferRename(const char *from, const char *to);
int infoTransferQuitFromRemote();
int infoTransferCleanup();
#endif //INFOTRANSFER_H
