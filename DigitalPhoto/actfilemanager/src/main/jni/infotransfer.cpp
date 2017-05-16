//
// Created by yuchen on 2016/5/25.
//
#include <pthread.h>
#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

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
extern void postRenameResponse(int result);

static InfoTransferManager *itmanager = NULL;

static const char *month_table[12] = {
	"Jan",
	"Feb",
	"Mar",
	"Apr",
	"May",
	"Jun",
	"Jul",
	"Aug",
	"Sep",
	"Oct",
	"Nov",
	"Dec"
};

//static int transfer_add_request(InfoTransferManager* itm, int* opts, void** params, int optnum);
static int transfer_add_request(InfoTransferManager *itm, int *opts, void **params, int optnum,
                                int optype);

static int transfer_clear_requests(InfoTransferManager *itm);

static int transfer_write_func(void *ptr, size_t size, size_t nmemb, void *stream);

static void *transfer_runner(void *arg);

int infoTransferInit(const char *ip)
{
    int ret;
    CURLcode res;

    if (ip == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "JNI version error!");
        return -1;
    }

    res = curl_global_init(CURL_GLOBAL_ALL);
    if (res != CURLE_OK) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "something wrong when curl global init");
        return -1;
    }

    itmanager = (InfoTransferManager *) malloc(sizeof(InfoTransferManager));
    if (itmanager == NULL) {
		curl_global_cleanup();
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "error malloc ftmanager");
        return -1;
    }

    memset(itmanager, 0, sizeof(InfoTransferManager));
    itmanager->mInited = 0;
    itmanager->mCurlHandle = NULL;
    strcpy(itmanager->mIp, ip);
    itmanager->mPendingReqNum = 0;
    itmanager->mAbort = 0;
	itmanager->mListData = (FileInfo*)malloc(sizeof(FileInfo)*MAX_FILES_IN_DIR);
	if(itmanager->mListData == NULL)
	{
		curl_global_cleanup();
		free(itmanager);
		itmanager = NULL;
	    __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "something wrong when malloc listdata");
		return -1;
	}
	//__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "listdata %p",itmanager->mListData);

    ret = pthread_create(&itmanager->mTaskRunner, NULL, transfer_runner, itmanager);
    if (ret != 0) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "error create transfer thread");
		curl_global_cleanup();
		free(itmanager->mListData);
        free(itmanager);
        itmanager = NULL;
        return -1;
    }
    pthread_mutex_init(&itmanager->mOnNewRequestMutex, NULL);
    pthread_cond_init(&itmanager->mOnNewRequestCond, NULL);
    return 0;
}

int infoTransferCleanup()
{
    if (itmanager == NULL)
		return 0;
	
    //stop current task and clear all pending requests;
    transfer_clear_requests(itmanager);

    pthread_mutex_lock(&itmanager->mOnNewRequestMutex);
    itmanager->mAbort = 1;
    pthread_cond_broadcast(&itmanager->mOnNewRequestCond);
    pthread_mutex_unlock(&itmanager->mOnNewRequestMutex);
	
    pthread_join(itmanager->mTaskRunner, NULL);
    pthread_mutex_destroy(&itmanager->mOnNewRequestMutex);
    pthread_cond_destroy(&itmanager->mOnNewRequestCond);
	if(itmanager->mCurlHandle != NULL)
        curl_easy_cleanup(itmanager->mCurlHandle);
    if(itmanager->mNetBuffer != NULL)
        free(itmanager->mNetBuffer);
	free(itmanager->mListData);
    free(itmanager);
    itmanager = NULL;
	curl_global_cleanup();

    return 0;
}

int infoTransferDelete(const char *url)
{
    int ret = 0;
    int opts[MAX_OPT_NUM];
    void *params[MAX_OPT_NUM];
    int optnum = 0;
    char dirurl[1024];
    char cmd[1024];

    if (url == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "null file pointer");
        return -1;
    }

	strcpy(dirurl, "ftp://");
	strcat(dirurl, itmanager->mIp);
	strcat(dirurl, "/");
    opts[optnum] = CURLOPT_URL;
	params[optnum] = (void *) dirurl;
	optnum++;

	opts[optnum] = CURLOPT_CUSTOMREQUEST;
	strcpy(cmd, "DELE ");
	strcat(cmd, url);
	params[optnum] = (void *) cmd;
	optnum++;

	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "delete under %s(cmd %s)",dirurl,cmd);
    ret = transfer_add_request(itmanager, opts, params, optnum, OP_TYPE_DELETE);
    return ret;
}

int infoTransferDeleteDirectory(const char *dir_path)
{
	// remote_path like /helloworld/
    int ret = 0;
    int opts[MAX_OPT_NUM];
    void *params[MAX_OPT_NUM];
    int optnum = 0;
    char dirurl[1024];
    char cmd[1024];

    if (dir_path == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "null file pointer");
        return -1;
    }

    opts[optnum] = CURLOPT_URL;
	strcpy(dirurl, "ftp://");
	strcat(dirurl, itmanager->mIp);
	strcat(dirurl, "/");
	params[optnum] = (void *) dirurl;
	optnum++;

	opts[optnum] = CURLOPT_CUSTOMREQUEST;
	strcpy(cmd, "RMD ");
	strcat(cmd, dir_path);
	params[optnum] = (void *) cmd;
	optnum++;

	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "delete DIR under %s(cmd %s)",dirurl,cmd);
    ret = transfer_add_request(itmanager, opts, params, optnum, OP_TYPE_DELETE_DIR);
    return ret;
}

// remote_path like /helloworld/
int infoTransferCreateDirectory(const char *remote_path)
{
    int ret = 0;
    int opts[MAX_OPT_NUM];
    void *params[MAX_OPT_NUM];
    int optnum = 0;
    char dirurl[1024];
    char cmd[1024];

    if (remote_path == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "null file pointer");
        return -1;
    }
	
    opts[optnum] = CURLOPT_URL;
	strcpy(dirurl, "ftp://");
	strcat(dirurl, itmanager->mIp);
	strcat(dirurl, "/");
	params[optnum] = (void *) dirurl;
	optnum++;

	opts[optnum] = CURLOPT_CUSTOMREQUEST;
	strcpy(cmd, "MKD ");
	strcat(cmd, remote_path);
	params[optnum] = (void *) cmd;
	optnum++;

	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "create DIR under %s(cmd %s)",dirurl,cmd);
    ret = transfer_add_request(itmanager, opts, params, optnum, OP_TYPE_CREATE_DIR);
    return ret;
}

int infoTransferQuitFromRemote(void)
{
    int ret = 0;
    int opts[MAX_OPT_NUM];
    void *params[MAX_OPT_NUM];
    int optnum = 0;
    char dirurl[1024];
    char cmd[1024];

    opts[optnum] = CURLOPT_URL;
	strcpy(dirurl, "ftp://");
	strcat(dirurl, itmanager->mIp);
	strcat(dirurl, "/");
	params[optnum] = (void *) dirurl;
	optnum++;

	opts[optnum] = CURLOPT_CUSTOMREQUEST;
	strcpy(cmd, "QUIT ");
	params[optnum] = (void *) cmd;
	optnum++;

	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "quit %s (cmd %s)",dirurl,cmd);
    ret = transfer_add_request(itmanager, opts, params, optnum, OP_TYPE_QUIT);
    return ret;
}

// dirpath like /helloworld/
int infoTransferListInfo(const char *dirpath)
{
    int ret = 0;
    int opts[MAX_OPT_NUM];
    void *params[MAX_OPT_NUM];
    int optnum = 0;
    char dirurl[1024];
    //char cmd[1024];

	//strcpy(dirurl, "ftp://");
    //strcat(dirurl, itmanager->mIp);
	//strcat(dirurl, "/");
	sprintf(dirurl, "ftp://%s%s", itmanager->mIp, dirpath);
    opts[optnum] = CURLOPT_URL;
	params[optnum] = (void *) dirurl;
    optnum++;

	opts[optnum] = CURLOPT_WRITEFUNCTION;
    params[optnum] = (void *) transfer_write_func;
    optnum++;

    //opts[optnum] = CURLOPT_CUSTOMREQUEST;
    //strcpy(cmd, "LIST ");
	//strcat(cmd, dirpath);
    //params[optnum] = (void *) cmd;
    //optnum++;

	strcpy(itmanager->mCurrentPath, dirpath);

	//__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "LIST %s (cmd %s)",dirurl,cmd);
    ret = transfer_add_request(itmanager, opts, params, optnum, OP_TYPE_BROWSE);
    return ret;
}

int infoTransferRename(const char *from, const char *to)
{
    int ret = 0;
    int opts[MAX_OPT_NUM];
    void *params[MAX_OPT_NUM];
    int optnum = 0;
    char dirurl[1024];
    char cmd1[1024];
	char cmd2[1024];

    opts[optnum] = CURLOPT_URL;
	strcpy(dirurl, "ftp://");
	strcat(dirurl, itmanager->mIp);
	strcat(dirurl, "/");
	params[optnum] = (void *) dirurl;
	optnum++;

	//opts[optnum] = CURLOPT_CUSTOMREQUEST;
	//sprintf(cmd, "RNFR %s\r\nRNTO %s", from, to);
	//params[optnum] = (void *) cmd;
	//optnum++;

	opts[optnum] = CURLOPT_CUSTOMREQUEST;
	sprintf(cmd1, "RNFR %s", from);
	params[optnum] = (void *) cmd1;
	optnum++;

	opts[optnum] = CURLOPT_CUSTOMREQUEST;
	sprintf(cmd2, "RNTO %s", to);
	params[optnum] = (void *) cmd2;
	optnum++;

    ret = transfer_add_request(itmanager, opts, params, optnum, OP_TYPE_RENAME);
    return ret;
}

//parse one file info and fill into FileInfo item
static int parseFileInfo(const char *buf, FileInfo *item)
{
	//if modify time is current year, then modify time format is: 15672 Dec  2 12:00
	//else mofify time format is: Dec 31 2015
    //file example: "-rw-------   1 user group        15672 Dec  2 12:00 Java_server_proxy.rar"
    //dir example: "drwx------   3 user group            0 Nov 26 08:09 python"
    //dir: drw-rw-rw-   1 user     ftp  0 Dec 31 2015 story_music
    //file: -rw-rw-rw-   1 user     ftp  1048588 Sep 9 2001 swfconfig.shm
    char *p = (char*)buf;
    int i;
	int j;
	int y = 0, m = 0, d = 0;
	char *tmp;
	char *time = NULL;
	char *filename = NULL;

	if(buf == NULL)
		return -1;
	
	if(*p == 'd')
	    item->mType = FILE_TYPE_DIRECTORY;
	else if(*p == '-')
	    item->mType = FILE_TYPE_FILE;
	else if(*p == 'l')
	    item->mType = FILE_TYPE_LINK;

    item->mName[0] = 0;
    for(i=0; i<9; i++)
    {
    	switch(i)
		{
		case 4:
			item->mSize = strtoll(p, NULL, 0);
			break;
		case 5:
			for(j=0; j<12; j++)
			{
				if(strncasecmp(p, month_table[j], strlen(month_table[j])) == 0)
					break;
			}
			if(j == 12) j = 0;
			m = j + 1;
			break;
		case 6:
			d = strtol(p, NULL, 0);
			break;
		case 7:
			tmp = strchr(p, ':');
			if(tmp == NULL)
			{
				y = strtol(p, NULL, 0);
			}
			else
			{
				//time_t current_time;
				//struct tm *s_time;
				
				//time(&current_time);
    			//s_time = gmtime(&current_time);
				//y = s_time->tm_year + 1900;
                y = 1900;
				time = p;
			}
			break;
		case 8:
			filename = p;
			break;
		default:
			break;
		}
		
        while(!isspace(*p))
        {
            if(*p == '\0')
                break;
            p++;
        }
        while(isspace(*p))
        {
            if(*p == '\0')
                break;
			*p = 0;
            p++;
        }
    }
    
    strcpy(item->mName, filename);
	if(time == NULL)
		sprintf(item->mDate, "%d/%d/%d", y, m, d);
	else
		sprintf(item->mDate, "%d/%d/%d %s", y, m, d, time);
	//__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "file name %s file_type %d size: %lld, time=%s",
	//	item->mName, item->mType, item->mSize, item->mDate);
	return 0;
}

//for downloaded data to write to file
static int transfer_write_func(void *ptr, size_t size, size_t nmemb, void *stream)
{
    int ret;
    FileInfo *filist = (FileInfo *) stream;
    char *start;
    char *now;
    int offset = 0;
    int infosize = 0;
	char split = '\n';
	int newDataSize = size * nmemb;

	if(itmanager->mAbort == 1)
		return -1;

	//__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "size=%d, nmemb=%d", size, nmemb);
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
    while (offset < itmanager->mDataSize)
	{
        //seperate one line from response
        now = strchr(start, split);
        //__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "offset=%d, now=%p, start=%s",
        //    offset, now, start);
        if (now != NULL)
		{
            //copy one file info out;
            infosize = now - start;
            now[0] = 0;

			//__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "item line: %s", start);
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
			//__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "no found split");
            break;
        }
    }

    itmanager->mDataSize -= offset;
    memmove(itmanager->mNetBuffer, itmanager->mNetBuffer+offset, itmanager->mDataSize);
    return nmemb;
}

static int transfer_add_request(InfoTransferManager *itm, int *opts, void **params, int optnum, int optype)
{
    int i;
    int ret;

    if (itm->mAbort == 1) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "file transfer runner aborting");
        return -1;
    }

    //add a request here, signal runner to wake up if it's waiting
    pthread_mutex_lock(&itm->mOnNewRequestMutex);
    //add a request
    if (itm->mPendingReqNum >= MAX_REQUESTS) {
		pthread_mutex_unlock(&itm->mOnNewRequestMutex);
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "too many pending requests");
        return -1;
    }

    RequestOpt *reqp = (RequestOpt *) malloc(sizeof(RequestOpt));
    if (reqp == NULL) {
		pthread_mutex_unlock(&itm->mOnNewRequestMutex);
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "fail to malloc request");
        return -1;
    }

    for (i = 0; i < optnum; i++)
	{
        reqp->mOptCmd[i] = opts[i];
        switch ((CURLoption) opts[i])
		{
        case CURLOPT_URL:
	    case CURLOPT_CUSTOMREQUEST:
            if (params[i] == NULL) {
                //dont assume error yet, let do_run thread to check
                reqp->mOptParam[i] = params[i];
                break;
            }

            reqp->mOptParam[i] = strdup((char*)params[i]);
            break;
        default:
            reqp->mOptParam[i] = params[i];
			break;
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

static int transfer_clear_requests(InfoTransferManager *itm)
{
    int i, j;
    int reqnum;
	
    //lock and clear and shake and unlock
    pthread_mutex_lock(&itm->mOnNewRequestMutex);
    reqnum = itm->mPendingReqNum;
    for (i = 0; i < reqnum; i++)
	{
        RequestOpt *reqp = itm->mReqList[i];
        for (j = 0; j < reqp->mOptNum; j++)
		{
            switch ((CURLoption) reqp->mOptCmd[i])
			{
            case CURLOPT_URL:
			case CURLOPT_CUSTOMREQUEST:
                if (reqp->mOptParam[i] != NULL)
				{
                    //dont assume error yet, let do_run thread to check
                    free(reqp->mOptParam[i]);
                    reqp->mOptParam[i] = NULL;
                    break;
                }
                break;
            default:
                reqp->mOptParam[i] = NULL;
				break;
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

static int transfer_retrieve_request(InfoTransferManager *itm, RequestOpt **req)
{
    int i;

    *req = itm->mReqList[0];
    for (i = 1; i < itm->mPendingReqNum; i++) {
        itm->mReqList[i - 1] = itm->mReqList[i];
		itm->mReqList[i] = NULL;
    }
    itm->mPendingReqNum--;

    return 0;
}

static void *transfer_runner(void *arg)
{
    InfoTransferManager *itm = (InfoTransferManager *) arg;
	struct curl_slist *headerlist=NULL;

    //create handle
    itm->mCurlHandle = curl_easy_init();
    if (itm->mCurlHandle == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "error create transfer handle");
        return NULL;
    }

    //run all requests in loop
    pthread_mutex_lock(&itm->mOnNewRequestMutex);
    while (itm->mAbort == 0)
	{
        RequestOpt *req;
        int i;
        CURLcode curlret;

        if (itm->mPendingReqNum > 0)
		{
            transfer_retrieve_request(itm, &req);
			itm->mCurrentOpType = req->mType;
			itm->mFileCount = 0;
            itm->mDataSize = 0;
			pthread_mutex_unlock(&itm->mOnNewRequestMutex);

            for (i = 0; i < req->mOptNum; i++)
			{
                switch ((CURLoption) req->mOptCmd[i])
				{
                case CURLOPT_URL:
                    strcpy(itm->mCurrentUrl, (const char *) req->mOptParam[i]);
                    curl_easy_setopt(itm->mCurlHandle, (CURLoption) req->mOptCmd[i],
                                     itm->mCurrentUrl);
                    break;
				case CURLOPT_CUSTOMREQUEST:
					headerlist = curl_slist_append(headerlist, (char*)req->mOptParam[i]);
					break;
                default:
                    curl_easy_setopt(itm->mCurlHandle, (CURLoption) req->mOptCmd[i],
                         req->mOptParam[i]);
					break;
                }
            }

			if(headerlist != NULL)
				curl_easy_setopt(itm->mCurlHandle, CURLOPT_POSTQUOTE, headerlist);

            curlret = curl_easy_perform(itm->mCurlHandle);
			__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "curl_easy_perform(info) result(%d) on type %d",curlret,req->mType);

            if (curlret == CURLE_OK)
			{
				//when create/delete the dir successfully but will return CURLE_FTP_COULDNT_RETR_FILE as an zero tranfer finish
                switch (itm->mCurrentOpType)
				{
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
				case OP_TYPE_RENAME:
					postRenameResponse(0);
					break;
				default:
					break;
                }
            }
            else
			{
				//fixme: error, need to notify?
                switch (itm->mCurrentOpType)
				{
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
				case OP_TYPE_RENAME:
					postRenameResponse(-1);
					break;
				default:
					break;
                }
            }
			
            for (i = 0; i < req->mOptNum; i++)
			{
                switch ((CURLoption) req->mOptCmd[i])
				{
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
					break;
                }
            }

			if(headerlist != NULL)
			{
				curl_slist_free_all (headerlist);
				headerlist = NULL;
			}
            free(req);
			curl_easy_reset(itm->mCurlHandle);
			pthread_mutex_lock(&itm->mOnNewRequestMutex);
        }
        else {
			pthread_cond_wait(&itm->mOnNewRequestCond, &itm->mOnNewRequestMutex);
        }
    }
}

