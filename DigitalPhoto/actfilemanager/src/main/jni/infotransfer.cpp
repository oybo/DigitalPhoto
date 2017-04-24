//
// Created by yuchen on 2016/5/25.
//
#include <pthread.h>

#include "infotransfer.h"
//#include "curl/curl.h"
#include "common.h"

static char username[] = "anonymous";
static char passwd[] = "ftp";

static char list_current_dir[] = "LIST .";

extern void postDeleteResponse(const char *url, int result);
extern void postDeleteDirectoryResponse(const char *dir_path,int result);
extern void postCreateDirectoryResponse(const char *parent_path,int result);
extern void postBrowseResponse(FileInfo **filelist, int fileCount, int result,const char *current_path);
extern void postDisconnectResponse(int result);

static InfoTransferManager *itmanager;

//static int transfer_add_request(InfoTransferManager* itm, int* opts, void** params, int optnum);
static int transfer_add_request(InfoTransferManager *itm, int *opts, void **params, int optnum,
                                int optype);

static int transfer_clear_requests(InfoTransferManager *itm);

static int transfer_write_func(void *ptr, size_t size, size_t nmemb, void *stream);

static void *transfer_runner(void *arg);

int infoTransferInit(const char *ip) {
    int ret;
    CURLcode res;
    pthread_attr_t attr;

    if (ip == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "JNI version error!");
        return -1;
    }

    res = curl_global_init(CURL_GLOBAL_DEFAULT);
    if (res != 0) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "something wrong when curl global init");
        return -1;
    }


    itmanager = (InfoTransferManager *) malloc(sizeof(InfoTransferManager));
    if (itmanager == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "error malloc ftmanager");
        return -1;
    }


    memset(itmanager, 0, sizeof(InfoTransferManager));
    itmanager->mInited = 0;
    itmanager->mCurlHandle = NULL;
    memset((void *) itmanager->mIp, 0, 16);
    strcpy(itmanager->mIp, ip);
    itmanager->mPendingReqNum = 0;
    itmanager->mAbort = 0;
	itmanager->headerlist = NULL;
	itmanager->mListData = (FileInfo*)malloc(sizeof(FileInfo)*MAX_FILES_IN_DIR);
	if(itmanager->mListData == NULL)
	{
	    __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "something wrong when malloc listdata");
		return -1;
	}
	//__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "listdata %p",itmanager->mListData);

    ret = pthread_attr_init(&attr);
    if (ret != 0) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "error malloc ftmanager");
        free(itmanager);
        itmanager = NULL;
        return -1;
    }

    ret = pthread_create(&itmanager->mTaskRunner, &attr, transfer_runner, itmanager);
    if (ret != 0) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "error create transfer thread");
        free(itmanager);
        itmanager = NULL;
        return -1;
    }
    pthread_mutex_init(&itmanager->mOnNewRequestMutex, NULL);
    pthread_cond_init(&itmanager->mOnNewRequestCond, NULL);
    return 0;
}

int infoTransferCleanup() {
    int ret;
    void *ret_p = &ret;

    if (itmanager != NULL) {
        //stop current task and clear all pending requests;
        transfer_clear_requests(itmanager);

        //fixme: if no pending requests need a signal to wake up sleeping runner;

        //fixme: need a lock here to protect mAbort?
        itmanager->mAbort = 1;
        //pthread_join(itmanager->mTaskRunner,&((void *)ret));
        pthread_join(itmanager->mTaskRunner, &ret_p);
        pthread_mutex_destroy(&itmanager->mOnNewRequestMutex);
        pthread_cond_destroy(&itmanager->mOnNewRequestCond);
        if(itmanager->mNetBuffer != NULL)
            free(itmanager->mNetBuffer);
		free(itmanager->mListData);
        free(itmanager);
        itmanager = NULL;
    }

    curl_global_cleanup();

    return 0;
}

int infoTransferDelete(const char *url) {
    int ret = 0;
    int opts[MAX_OPT_NUM];
    void *params[MAX_OPT_NUM];
    int optnum = 0;
    char dirurl[1024];
    //char filename[1024];
    char cmd[1024];
    struct curl_slist *headerlist = itmanager->headerlist;

    if (url == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "null file pointer");
        return -1;
    }
	
#if 0
    opts[optnum] = CURLOPT_URL;
    char *dirend = strrchr(url, '/');
    int off = dirend - url;
    memset(dirurl, 0, 1024);
	strcpy(dirurl, "ftp://");
    strcat(dirurl, itmanager->mIp);
	memset(filename, 0, 1024);
    memcpy(filename, url, off+1);
	strcat(dirurl, filename);
	
    memset(filename, 0, 1024);
    strcpy(filename, dirend + 1);
    params[optnum] = (void *) dirurl;
    optnum++;

    opts[optnum] = CURLOPT_POSTQUOTE;//CURLOPT_QUOTE;
    memset(cmd, 0, 1024);
    strcpy(cmd, "DELE ");
    strcat(cmd, filename);
    headerlist = curl_slist_append(headerlist, cmd);
    params[optnum] = (void *) headerlist;
    optnum++;
#endif

    opts[optnum] = CURLOPT_URL;
    memset(dirurl, 0, 1024);
	strcpy(dirurl, "ftp://");
	strcat(dirurl, itmanager->mIp);
	strcat(dirurl, "/");
	params[optnum] = (void *) dirurl;
	optnum++;

	opts[optnum] = CURLOPT_CUSTOMREQUEST;
	memset(cmd, 0, 1024);
	strcpy(cmd, "DELE ");
	strcat(cmd, url);
	params[optnum] = (void *) cmd;
	optnum++;

	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "delete under %s(cmd %s)",dirurl,cmd);

    ret = transfer_add_request(itmanager, opts, params, optnum, OP_TYPE_DELETE);
    return ret;
}

int infoTransferDeleteDirectory(const char *dir_path) {
	// remote_path like /helloworld/
    int ret = 0;
    int opts[MAX_OPT_NUM];
    void *params[MAX_OPT_NUM];
    int optnum = 0;
    char dirurl[1024];
    char dirname[1024];
    char cmd[1024];
    struct curl_slist *headerlist = itmanager->headerlist;

    if (dir_path == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "null file pointer");
        return -1;
    }

#if 0
    opts[optnum] = CURLOPT_URL;
    char *dirend = strrchr(dir_path, '/');
	*dirend = 0x0;
	dirend = strrchr(dir_path, '/');
    int off = dirend - dir_path;
    memset(dirurl, 0, 1024);
	strcpy(dirurl, "ftp://");
    strcat(dirurl, itmanager->mIp);
	memset(dirname, 0, 1024);
    memcpy(dirname, dir_path, off+1);
	strcat(dirurl, dirname);
	memset(dirname, 0, 1024);
    strcpy(dirname, dirend + 1);
	params[optnum] = (void *) dirurl;
    optnum++;

    opts[optnum] = CURLOPT_POSTQUOTE;
    memset(cmd, 0, 1024);
    strcpy(cmd, "RMD ");
    strcat(cmd, dirname);
    headerlist = curl_slist_append(headerlist, cmd);
    params[optnum] = (void *) headerlist;
    optnum++;
#endif

    opts[optnum] = CURLOPT_URL;
    memset(dirurl, 0, 1024);
	strcpy(dirurl, "ftp://");
	strcat(dirurl, itmanager->mIp);
	strcat(dirurl, "/");
	params[optnum] = (void *) dirurl;
	optnum++;

	opts[optnum] = CURLOPT_CUSTOMREQUEST;
	memset(cmd, 0, 1024);
	strcpy(cmd, "RMD ");
	strcat(cmd, dir_path);
	params[optnum] = (void *) cmd;
	optnum++;

	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "delete DIR under %s(cmd %s)",dirurl,cmd);

    ret = transfer_add_request(itmanager, opts, params, optnum, OP_TYPE_DELETE_DIR);
    return ret;

}

int infoTransferCreateDirectory(const char *remote_path) {
	// remote_path like /helloworld/
    int ret = 0;
    int opts[MAX_OPT_NUM];
    void *params[MAX_OPT_NUM];
    int optnum = 0;
    char dirurl[1024];
    char dirname[1024];
    char cmd[1024];
    struct curl_slist *headerlist = itmanager->headerlist;

    if (remote_path == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "null file pointer");
        return -1;
    }
	
#if 0
    opts[optnum] = CURLOPT_URL;
    char *dirend = strrchr(remote_path, '/');
	*dirend = 0x0;
	dirend = strrchr(remote_path, '/');
    int off = dirend - remote_path;
    memset(dirurl, 0, 1024);
	strcpy(dirurl, "ftp://");
    strcat(dirurl, itmanager->mIp);
	memset(dirname, 0, 1024);
    memcpy(dirname, remote_path, off+1);
	strcat(dirurl, dirname);
	memset(dirname, 0, 1024);
    strcpy(dirname, dirend + 1);
	params[optnum] = (void *) dirurl;
    optnum++;

    opts[optnum] = CURLOPT_POSTQUOTE;
    memset(cmd, 0, 1024);
    strcpy(cmd, "MKD ");
    strcat(cmd, dirname);
    headerlist = curl_slist_append(headerlist, cmd);
    params[optnum] = (void *) headerlist;
    optnum++;
#endif
    opts[optnum] = CURLOPT_URL;
    memset(dirurl, 0, 1024);
	strcpy(dirurl, "ftp://");
	strcat(dirurl, itmanager->mIp);
	strcat(dirurl, "/");
	params[optnum] = (void *) dirurl;
	optnum++;

	opts[optnum] = CURLOPT_CUSTOMREQUEST;
	memset(cmd, 0, 1024);
	strcpy(cmd, "MKD ");
	strcat(cmd, remote_path);
	params[optnum] = (void *) cmd;
	optnum++;

	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "create DIR under %s(cmd %s)",dirurl,cmd);

    ret = transfer_add_request(itmanager, opts, params, optnum, OP_TYPE_CREATE_DIR);
    return ret;

}

int infoTransferQuitFromRemote() {
	// remote_path like /helloworld/
    int ret = 0;
    int opts[MAX_OPT_NUM];
    void *params[MAX_OPT_NUM];
    int optnum = 0;
    char dirurl[1024];
    char cmd[1024];
    //struct curl_slist *headerlist = itmanager->headerlist;

#if 0
    opts[optnum] = CURLOPT_URL;
    memset(dirurl, 0, 1024);
	strcpy(dirurl, "ftp://");
    strcat(dirurl, itmanager->mIp);
	strcat(dirurl, "/");
	params[optnum] = (void *) dirurl;
    optnum++;

    opts[optnum] = CURLOPT_QUOTE;
    memset(cmd, 0, 1024);
    strcpy(cmd, "QUIT");
    headerlist = curl_slist_append(headerlist, cmd);
    params[optnum] = (void *) headerlist;
    optnum++;
#endif

    opts[optnum] = CURLOPT_URL;
    memset(dirurl, 0, 1024);
	strcpy(dirurl, "ftp://");
	strcat(dirurl, itmanager->mIp);
	strcat(dirurl, "/");
	params[optnum] = (void *) dirurl;
	optnum++;

	opts[optnum] = CURLOPT_CUSTOMREQUEST;
	memset(cmd, 0, 1024);
	strcpy(cmd, "QUIT ");
	params[optnum] = (void *) cmd;
	optnum++;

	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "quit %s (cmd %s)",dirurl,cmd);

    ret = transfer_add_request(itmanager, opts, params, optnum, OP_TYPE_QUIT);
    return ret;

}

int infoTransferListInfo(const char *dirpath) {
	// dirpath like /helloworld/
    int ret = 0;
    int opts[MAX_OPT_NUM];
    void *params[MAX_OPT_NUM];
    int optnum = 0;
    char dirurl[1024];
    char cmd[1024];

    opts[optnum] = CURLOPT_URL;
    memset(dirurl, 0, 1024);
	strcpy(dirurl, "ftp://");
    strcat(dirurl, itmanager->mIp);
	strcat(dirurl, "/");
	params[optnum] = (void *) dirurl;
    optnum++;

	opts[optnum] = CURLOPT_WRITEFUNCTION;
    params[optnum] = (void *) transfer_write_func;
    optnum++;

    opts[optnum] = CURLOPT_CUSTOMREQUEST;
    memset(cmd, 0, 1024);
    strcpy(cmd, "LIST ");
	strcat(cmd, dirpath);
    params[optnum] = (void *) cmd;
    optnum++;

	memset(itmanager->mCurrentPath,0x0,MAX_PATH_LENGTH);
	strcpy(itmanager->mCurrentPath, dirpath);

	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "LIST %s (cmd %s)",dirurl,cmd);

    ret = transfer_add_request(itmanager, opts, params, optnum, OP_TYPE_BROWSE);
    return ret;
}

int infoTransferListCurrentDirectory() {
	// dirpath like /helloworld/
    int ret = 0;
    int opts[MAX_OPT_NUM];
    void *params[MAX_OPT_NUM];
    int optnum = 0;
    char dirurl[1024];
    char cmd[1024];
    struct curl_slist *headerlist = itmanager->headerlist;


    opts[optnum] = CURLOPT_URL;
    memset(dirurl, 0, 1024);
	strcpy(dirurl, "ftp://");
    strcat(dirurl, itmanager->mIp);
	strcat(dirurl, "/");
	params[optnum] = (void *) dirurl;
    optnum++;

	opts[optnum] = CURLOPT_WRITEFUNCTION;
    params[optnum] = (void *) transfer_write_func;
    optnum++;

    /*
    opts[optnum] = CURLOPT_POSTQUOTE;
    memset(cmd, 0, 1024);
    strcpy(cmd, "LIST .");
    headerlist = curl_slist_append(headerlist, cmd);
    params[optnum] = (void *) headerlist;
    optnum++;*/
    
    opts[optnum] = CURLOPT_CUSTOMREQUEST;
	memset(cmd, 0, 1024);
	strcpy(cmd, "LIST .");
	params[optnum] = (void *) cmd;
	optnum++;

	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "list %s (cmd %s)",dirurl,cmd);

    ret = transfer_add_request(itmanager, opts, params, optnum, OP_TYPE_BROWSE);
    return ret;
}


int infoTransferChangeCurrentDirectory(const char *dirpath) {
	// dirpath like /helloworld/
    int ret = 0;
    int opts[MAX_OPT_NUM];
    void *params[MAX_OPT_NUM];
    int optnum = 0;
    char dirurl[1024];
    char cmd[1024];
    struct curl_slist *headerlist = itmanager->headerlist;

#if 0
    opts[optnum] = CURLOPT_URL;
    memset(dirurl, 0, 1024);
	strcpy(dirurl, "ftp://");
    strcat(dirurl, itmanager->mIp);
	strcat(dirurl, "/");
	params[optnum] = (void *) dirurl;
    optnum++;

	opts[optnum] = CURLOPT_WRITEFUNCTION;
    params[optnum] = (void *) transfer_write_func;
    optnum++;

    opts[optnum] = CURLOPT_POSTQUOTE;
    memset(cmd, 0, 1024);
    strcpy(cmd, "CWD ");
	strcat(cmd, dirpath);
    headerlist = curl_slist_append(headerlist, cmd);
    params[optnum] = (void *) headerlist;
    optnum++;
#endif
    opts[optnum] = CURLOPT_URL;
    memset(dirurl, 0, 1024);
	strcpy(dirurl, "ftp://");
	strcat(dirurl, itmanager->mIp);
	strcat(dirurl, "/");
	params[optnum] = (void *) dirurl;
	optnum++;

	opts[optnum] = CURLOPT_CUSTOMREQUEST;
	memset(cmd, 0, 1024);
	strcpy(cmd, "CWD ");
	strcat(cmd, dirpath);
	params[optnum] = (void *) cmd;
	optnum++;

	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "CWD %s (cmd %s)",dirurl,cmd);

    ret = transfer_add_request(itmanager, opts, params, optnum, OP_TYPE_CWD);
    return ret;
}

int infoTransferGetCurrentDirectory() {
	// dirpath like /helloworld/
    int ret = 0;
    int opts[MAX_OPT_NUM];
    void *params[MAX_OPT_NUM];
    int optnum = 0;
    char dirurl[1024];
    char cmd[1024];
    struct curl_slist *headerlist = itmanager->headerlist;

#if 0
    opts[optnum] = CURLOPT_URL;
    memset(dirurl, 0, 1024);
	strcpy(dirurl, "ftp://");
    strcat(dirurl, itmanager->mIp);
	strcat(dirurl, "/");
	params[optnum] = (void *) dirurl;
    optnum++;

	opts[optnum] = CURLOPT_WRITEFUNCTION;
    params[optnum] = (void *) transfer_write_func;
    optnum++;

    opts[optnum] = CURLOPT_POSTQUOTE;
    memset(cmd, 0, 1024);
    strcpy(cmd, "PWD ");
    headerlist = curl_slist_append(headerlist, cmd);
    params[optnum] = (void *) headerlist;
    optnum++;
#endif
    opts[optnum] = CURLOPT_URL;
    memset(dirurl, 0, 1024);
	strcpy(dirurl, "ftp://");
	strcat(dirurl, itmanager->mIp);
	strcat(dirurl, "/");
	params[optnum] = (void *) dirurl;
	optnum++;

	opts[optnum] = CURLOPT_CUSTOMREQUEST;
	memset(cmd, 0, 1024);
	strcpy(cmd, "PWD");
	params[optnum] = (void *) cmd;
	optnum++;

	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "PWD %s (cmd %s)",dirurl,cmd);

    ret = transfer_add_request(itmanager, opts, params, optnum, OP_TYPE_PWD);
    return ret;
}


int infoTransferBrowse(const char *dirpath) {
    int ret;
    int opts[MAX_OPT_NUM];
    void *params[MAX_OPT_NUM];
    int optnum = 0;
    char dirurl[1024];
    char filename[1024];
    char cmd[1024];
    struct curl_slist *headerlist = itmanager->headerlist;

    //form a url to retrieve things
    memset(dirurl, 0, 1024);
    strcpy(dirurl, "ftp://");
    strcat(dirurl, itmanager->mIp);
	strcat(dirurl, dirpath);

    opts[optnum] = CURLOPT_URL;
    params[optnum] = (void *) dirurl;
    optnum++;

	opts[optnum] = CURLOPT_USERNAME;
    params[optnum] = (void *) username;
    optnum++;

	opts[optnum] = CURLOPT_PASSWORD;
    params[optnum] = (void *) passwd;
    optnum++;

    opts[optnum] = CURLOPT_WRITEFUNCTION;
    params[optnum] = (void *) transfer_write_func;
    optnum++;

    //opts[2] = CURLOPT_WRITEDATA;
    //params[2] = itmanager->mListData;
    //optnum++;
    memset(itmanager->mCurrentPath,0x0,MAX_PATH_LENGTH);
	strcpy(itmanager->mCurrentPath, dirpath);

	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "%s for %s", dirurl,itmanager->mCurrentPath);

    ret = transfer_add_request(itmanager, opts, params, optnum, OP_TYPE_BROWSE);

    //__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "transfer_add_browse_request %d",ret);

    return ret;
}

//parse one file info and fill into FileInfo item
static int parseFileInfo(const char *buf, FileInfo *item) {
    //file example: "-rw-------   1 user group        15672 Dec  2 12:00 Java_server_proxy.rar"
    //dir example: "drwx------   3 user group            0 Nov 26 08:09 python"
    const char *p = buf;
    int i;
    int j;

	if(buf == NULL)
		return -1;
	
	if(*p == 'd')
	    item->mType = FILE_TYPE_DIRECTORY;
	else if(*p == '-')
	    item->mType = FILE_TYPE_FILE;
	else if(*p == 'l')
	    item->mType = FILE_TYPE_LINK;

    item->mName[0] = 0;
    for(i=0; i<8; i++)
    {
        while((*p != ' ') && (*p != '\t'))
        {
            if(*p == '\0')
                return -1;
            p++;
        }
        while((*p == ' ') || (*p == '\t'))
        {
            if(*p == '\0')
                return -1;
            p++;
        }
    }
    
    strcpy(item->mName, p);
	//__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "file name %s file_type %d",item->mName,item->mType);
	return 0;
}

//for downloaded data to write to file
static int transfer_write_func(void *ptr, size_t size, size_t nmemb, void *stream) {
    int ret;
    FileInfo *filist = (FileInfo *) stream;
    char *start;
    char *now;
    int offset = 0;
    int infosize = 0;
	char split = 0xA; // "/n"
	int newDataSize = size * nmemb;

    //if (filist == NULL) {
    //    __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "null file pointer");
    //    return -1;
    //}
    //__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "size %d nmemb %d, r=%x, n=%x",
    //    size,nmemb, '\r', '\n');
    //__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "%s", start);
	//for(int i=0; i < size * nmemb; i++)
	//{
	//    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "<%d>%c(0x%x)", i,listdata[i],listdata[i]);
	//}

    if((newDataSize + itmanager->mDataSize + 1) > itmanager->mBufferSize)
    {
        itmanager->mBufferSize = newDataSize + itmanager->mDataSize + 1;
        itmanager->mNetBuffer = (char*)realloc(itmanager->mNetBuffer, itmanager->mBufferSize);
    }
    memcpy(itmanager->mNetBuffer+itmanager->mDataSize, ptr, newDataSize);
    itmanager->mDataSize += newDataSize;
    itmanager->mNetBuffer[itmanager->mDataSize] = 0;
    start = itmanager->mNetBuffer;
    
    //fixme: is "\r\n" a line seperator for sure?
    while (offset < itmanager->mDataSize) {
        //seperate one line from response
        now = strchr(start, split);
        //__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "offset=%d, now=%p, start=%s",
        //    offset, now, start);
        if (now != NULL) {
            //copy one file info out;
            infosize = now - start;
            now[0] = 0;

			//__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "offset %d fileinfo %s strlen %d",offset,fileinfo,strlen(fileinfo));
            FileInfo *item = itmanager->mListData + itmanager->mFileCount;
            ret = parseFileInfo((const char *)start, item);
            offset = offset + infosize + 1;
            start = start + infosize + 1;
            
            if (ret != 0) {
                __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "parse file info failed");
                continue;
            }
            itmanager->mFileCount++;
        }
        else
        {
            break;
        }
    }

    itmanager->mDataSize -= offset;
    memmove(itmanager->mNetBuffer, itmanager->mNetBuffer+offset, itmanager->mDataSize);
    return newDataSize;
}


static int transfer_add_request(InfoTransferManager *itm, int *opts, void **params, int optnum,
                                int optype) {
    int i;
    int ret;

    //fixme: need a lock here to protect mAbort?
    if (itm->mAbort == 1) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "file transfer runner aborting");
        return 0;
    }

    //add a request here, signal runner to wake up if it's waiting
    pthread_mutex_lock(&itm->mOnNewRequestMutex);
    //add a request
    if (itm->mPendingReqNum >= MAX_REQUESTS) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "too many pending requests");
        return -1;
    }

    RequestOpt *reqp = (RequestOpt *) malloc(sizeof(RequestOpt));
    if (reqp == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "fail to malloc request");
        return -1;
    }

    for (i = 0; i < optnum; i++) {
        int paramsize = 0;
        reqp->mOptCmd[i] = opts[i];
        switch ((CURLoption) opts[i]) {
            case CURLOPT_URL:
		    case CURLOPT_CUSTOMREQUEST:
                if (params[i] == NULL) {
                    //dont assume error yet, let do_run thread to check
                    reqp->mOptParam[i] = params[i];
                    break;
                }
                paramsize = 1 + strlen((char *) params[i]);
                reqp->mOptParam[i] = (void *) malloc(paramsize);
                memcpy(reqp->mOptParam[i], params[i], paramsize);
                break;
            default:
                reqp->mOptParam[i] = params[i];
        }
    }

    reqp->mType = optype;
    reqp->mOptNum = optnum;

    itm->mReqList[itm->mPendingReqNum] = reqp;
    itm->mPendingReqNum++;

    pthread_cond_broadcast(&itm->mOnNewRequestCond);
    pthread_mutex_unlock(&itm->mOnNewRequestMutex);

    return 0;
}

static int transfer_clear_requests(InfoTransferManager *itm) {
    int i, j;
    int reqnum;
    //lock and clear and shake and unlock
    pthread_mutex_lock(&itm->mOnNewRequestMutex);
    reqnum = itm->mPendingReqNum;
    for (i = 0; i < reqnum; i++) {
        RequestOpt *reqp = itm->mReqList[i];
        for (j = 0; j < reqp->mOptNum; j++) {
            //switch((CURLoption)reqp->mOptParam[i])
            switch ((CURLoption) reqp->mOptCmd[i]) {
                case CURLOPT_URL:
                    if (reqp->mOptParam[i] != NULL) {
                        //dont assume error yet, let do_run thread to check
                        free(reqp->mOptParam[i]);
                        reqp->mOptParam[i] = NULL;
                        break;
                    }
                    break;
                default:
                    reqp->mOptParam[i] = NULL;
            }
            free(reqp);
        }
        itm->mReqList[i] = NULL;
        itm->mPendingReqNum--;
    }

    //clear all requests here, signal runner to wake up if it's waiting
    pthread_cond_broadcast(&itm->mOnNewRequestCond);
    pthread_mutex_unlock(&itm->mOnNewRequestMutex);
    return 0;
}

static int transfer_retrieve_request(InfoTransferManager *itm, RequestOpt **req) {
    int i;
    //lock and retrieve and move and unlock
    pthread_mutex_lock(&itm->mOnNewRequestMutex);
    *req = itm->mReqList[0];

    for (i = 1; i < itm->mPendingReqNum; i++) {
        itm->mReqList[i - 1] = itm->mReqList[i];
		itm->mReqList[i] = NULL;
    }
    itm->mPendingReqNum--;

    //clear all requests here, signal runner to wake up if it's waiting
    //pthread_cond_broadcast(&itm->mOnNewRequestCond);
    pthread_mutex_unlock(&itm->mOnNewRequestMutex);

    return 0;
}

static void *transfer_runner(void *arg) {
    InfoTransferManager *itm = (InfoTransferManager *) arg;

    //create handle
    itm->mCurlHandle = curl_easy_init();
    if (itm->mCurlHandle == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "error create transfer handle");
        return NULL;
    }

    //run all requests in loop
    while (itm->mAbort == 0) {
        RequestOpt *req;
        int i;
        CURLcode curlret;

        if (itm->mPendingReqNum > 0) {
            //fixme: lock here
            transfer_retrieve_request(itm, &req);
            //fixme: unlock here

            itm->mCurrentOpType = req->mType;
            if (itm->mCurrentOpType == OP_TYPE_BROWSE) {
                itm->mFileCount = 0;
                itm->mDataSize = 0;
            }

            for (i = 0; i < req->mOptNum; i++) {
                switch ((CURLoption) req->mOptCmd[i]) {
                    case CURLOPT_URL:
                        memset(itm->mCurrentUrl, 0, MAX_URL_LENGTH);
                        strcpy(itm->mCurrentUrl, (const char *) req->mOptParam[i]);
                        curl_easy_setopt(itm->mCurlHandle, (CURLoption) req->mOptCmd[i],
                                         itm->mCurrentUrl);
                        break;
                    default:
                        curl_easy_setopt(itm->mCurlHandle, (CURLoption) req->mOptCmd[i],
                                         req->mOptParam[i]);
                }
            }

            curlret = curl_easy_perform(itm->mCurlHandle);
			__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "curl_easy_perform(info) result(%d) on type %d",curlret,req->mType);

            if ((curlret == CURLE_OK) ||(curlret == CURLE_FTP_COULDNT_RETR_FILE)) {
				//when create/delete the dir successfully but will return CURLE_FTP_COULDNT_RETR_FILE as an zero tranfer finish
                switch (itm->mCurrentOpType) {
                    case OP_TYPE_DELETE:
                        postDeleteResponse((const char *) itm->mCurrentUrl, 0);
                        break;
                    case OP_TYPE_BROWSE:
                        postBrowseResponse(&(itm->mListData), itm->mFileCount, 0, itm->mCurrentPath);
                        break;
				    case OP_TYPE_DELETE_DIR:
						postDeleteDirectoryResponse((const char *) itm->mCurrentUrl, 0);
						break;
					case OP_TYPE_CREATE_DIR:
						postCreateDirectoryResponse((const char *) itm->mCurrentUrl, 0);
						break;
					case OP_TYPE_QUIT:
						postDisconnectResponse(0);
						break;
                }
            }
            else {
				//fixme: error, need to notify?
                switch (itm->mCurrentOpType) {
                    case OP_TYPE_DELETE:
                        postDeleteResponse((const char *) itm->mCurrentUrl, -1);
                        break;
                    case OP_TYPE_BROWSE:
                        postBrowseResponse(NULL, 0, -1, itm->mCurrentPath);
                        break;
				    case OP_TYPE_DELETE_DIR:
						postDeleteDirectoryResponse((const char *) itm->mCurrentUrl, -1);
						break;
				    case OP_TYPE_CREATE_DIR:
						postCreateDirectoryResponse((const char *) itm->mCurrentUrl, -1);
						break;
					case OP_TYPE_QUIT:
						postDisconnectResponse(-1);
						break;
                }
            }
            //release request;
            release_request:
            for (i = 0; i < req->mOptNum; i++) {
                switch ((CURLoption) req->mOptCmd[i]) {
                    case CURLOPT_URL:
				    case CURLOPT_CUSTOMREQUEST:
                        if (req->mOptParam[i] != NULL) {
                            //dont assume error yet, let do_run thread to check
                            free(req->mOptParam[i]);
                            req->mOptParam[i] = NULL;
                            break;
                        }
                        break;
                    case CURLOPT_WRITEDATA:
                        //fixme: release mListData here
                        break;
                    default:
                        req->mOptParam[i] = NULL;
                }
            }
            free(req);
        }
        else {
            //fixme: timed wait for request to come
            //struct timespec timed;

            //struct timeval now;
            //if (gettimeofday(&now, NULL)) {
            //    __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "error error getting system time");
            //    return NULL;
            //}

            //timeout period is 5 sec
            //now.tv_usec += 5000 * 1000;
            //if (now.tv_usec >= 1000000) {
            //    now.tv_sec += now.tv_usec / 1000000;
            //    now.tv_usec = now.tv_usec % 1000000;
            //}

            // setup timeout
            //timed.tv_sec = now.tv_sec + 10;
            //timed.tv_nsec = now.tv_usec * 1000;


            pthread_mutex_lock(&itm->mOnNewRequestMutex);
			pthread_cond_wait(&itm->mOnNewRequestCond, &itm->mOnNewRequestMutex);
            //int wait_res = pthread_cond_wait(&itm->mOnNewRequestCond, &itm->mOnNewRequestMutex);
            //if (wait_res == ETIMEDOUT) {
            //    __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "TIMOUT on waiting requests");
            //    pthread_mutex_unlock(&itm->mOnNewRequestMutex);
            //    return NULL;
            //}

            pthread_mutex_unlock(&itm->mOnNewRequestMutex);
        }


    }
}

