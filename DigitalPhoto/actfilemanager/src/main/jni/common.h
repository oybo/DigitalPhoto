//
// Created by yuchen on 2016/5/25.
//

#ifndef COMMON_H
#define COMMON_H

#include <errno.h>

#include "android/log.h"
#include "curl/curl.h"

#define LOG_TAG    "actfilemanager"

#define MAX_OPT_NUM     16
#define MAX_REQUESTS    16

#define OP_TYPE_UPLOAD      1
#define OP_TYPE_DOWNLOAD    2
#define OP_TYPE_DELETE      3
#define OP_TYPE_BROWSE      4
#define OP_TYPE_DELETE_DIR  5
#define OP_TYPE_CREATE_DIR  6
#define OP_TYPE_QUIT        7
#define OP_TYPE_PWD         8
#define OP_TYPE_CWD         9

#define MAX_PATH_LENGTH     1024
#define MAX_URL_LENGTH      1024

#define MAX_FILES_IN_DIR    4096

/*typedef enum
{
    FILE_TYPE_MUSIC = 1,
    FILE_TYPE_VIDEO,
    FILE_TYPE_PHOTO
}FileType_e;*/

typedef enum
{
    FILE_TYPE_FILE = 1,
    FILE_TYPE_DIRECTORY,
    FILE_TYPE_LINK
}FileType_e;

//命令请求
typedef struct {
    int mOptNum;    //option num
    int mOptCmd[MAX_OPT_NUM]; //all options needed in the request
    void* mOptParam[MAX_OPT_NUM]; //all params to the options
    int mType; //type of the request, download, upload, browse, etc
}RequestOpt;

//file info from LIST cmd， parse from LIST cmd response
/*typedef struct {
    char* mName;
    char* mTime;
    long mSize;
    int mType;
    char* mThumb; //url to retrive thumbnail of the file
}FileInfo;*/
#define MAX_NAME_LEN 64
typedef struct {
    char mName[MAX_NAME_LEN];
    //long mSize;
    int mType;
}FileInfo;

#endif //COMMON_H