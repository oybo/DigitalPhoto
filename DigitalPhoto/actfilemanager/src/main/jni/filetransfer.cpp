//
// Created by yuchen on 2016/5/25.
//
#include <pthread.h>
#include <sys/stat.h>
#include <unistd.h>

#include "curl/curl.h"
#include "filetransfer.h"
#include "common.h"

extern void postOperationProgress(int opcode, int processed,int total);
extern void postUploadResponse(const char *remote_path, const char *file_path,int result);
extern void postDownloadResponse(const char* url, const char* path, int result);

static FileTransferManager* ftmanager = NULL;

static int transfer_add_request(FileTransferManager* ftm, int* opts, void** params, int optnum, int optype);
static int transfer_clear_requests(FileTransferManager* ftm);
static void* transfer_runner(void *arg);
static int transfer_read_func(void *ptr, size_t size, size_t nmemb, void *stream);
static int transfer_write_func(void *ptr, size_t size, size_t nmemb, void *stream);
static int transfer_clear_requests(FileTransferManager* ftm);

int fileTransferInit(const char* ip)
{
    int ret = 0;
    CURLcode res;

    if(ip == NULL)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "JNI version error!");
        return -1;
    }

    res = curl_global_init(CURL_GLOBAL_ALL);
    if(res != CURLE_OK)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "something wrong when curl global init");
        return -1;
    }

    ftmanager = (FileTransferManager *)malloc(sizeof(FileTransferManager));
    if(ftmanager == NULL)
    {
        curl_global_cleanup();
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "error malloc ftmanager");
        return -1;
    }
    
    memset(ftmanager, 0, sizeof(FileTransferManager));
    ftmanager->mInited = 0;
    ftmanager->mCurlHandle = NULL;
    strcpy(ftmanager->mIp, ip);
    ftmanager->mPendingReqNum = 0;
    ftmanager->mAbort = 0;
	ftmanager->mCurrentFile = NULL;

    ret = pthread_create(&ftmanager->mTaskRunner, NULL, transfer_runner, ftmanager);
    if(ret != 0)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "error create transfer thread");
        curl_global_cleanup();
        free(ftmanager);
        ftmanager = NULL;
        return -1;
    }
    
    pthread_mutex_init(&ftmanager->mOnNewRequestMutex, NULL);
    pthread_cond_init(&ftmanager->mOnNewRequestCond, NULL);

    return 0;
}

int fileTransferCleanup()
{
    if(ftmanager == NULL)
		return 0;
	
    //stop current task and clear all pending requests;
    transfer_clear_requests(ftmanager);

    pthread_mutex_lock(&ftmanager->mOnNewRequestMutex);
    ftmanager->mAbort = 1;
    pthread_cond_broadcast(&ftmanager->mOnNewRequestCond);
    pthread_mutex_unlock(&ftmanager->mOnNewRequestMutex);
    
    pthread_join(ftmanager->mTaskRunner, NULL);
    pthread_mutex_destroy(&ftmanager->mOnNewRequestMutex);
    pthread_cond_destroy(&ftmanager->mOnNewRequestCond);

	if(ftmanager->mCurrentFile != NULL)
        fclose(ftmanager->mCurrentFile);
	if(ftmanager->mCurlHandle != NULL)
        curl_easy_cleanup(ftmanager->mCurlHandle);
	
    free(ftmanager);
    ftmanager = NULL;
    curl_global_cleanup();

    return 0;
}

int fileTransferUpload(const char* file_path,const char* remote_path)
{
    int ret = 0;
    int opts[MAX_OPT_NUM];
    void* params[MAX_OPT_NUM];
    int optnum = 0;
    char url[1024];

    if(file_path == NULL || remote_path == NULL)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,"null file pointer");
        return -1;
    }

	strcpy(url, "ftp://");
	strcat(url, ftmanager->mIp);
	strcat(url, remote_path);

    opts[optnum]=CURLOPT_READFUNCTION;
    params[optnum]=(void *)transfer_read_func;
    optnum++;

#if 0
    opts[optnum]=CURLOPT_UPLOAD;
    params[optnum]=(void*)1L;
    optnum++;
#endif

    //fixme:generate url using ip and type
    opts[optnum]=CURLOPT_URL;
    params[optnum]=(void*)url;
    optnum++;

    //fixme:do we need extra ftp cmd

    opts[optnum]=CURLOPT_READDATA;
    params[optnum] = (void *)file_path;
    optnum++;

	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,"update %s to %s",file_path,url);

    ret = transfer_add_request(ftmanager, opts, params, optnum, OP_TYPE_UPLOAD);
    return ret;
}

int fileTransferDownload(const char* url, const char* path)
{
    int ret = 0;
    int opts[MAX_OPT_NUM];
    void* params[MAX_OPT_NUM];
    int optnum = 0;
	char fileurl[1024];

	//form a url to retrieve things
    memset(fileurl, 0, 1024);
	strcpy(fileurl, "ftp://");
	strcat(fileurl, ftmanager->mIp);
	strcat(fileurl, url);

    opts[optnum]=CURLOPT_URL;
    params[optnum]=(void*)fileurl;
    optnum++;

	opts[optnum]=CURLOPT_WRITEFUNCTION;
    params[optnum]=(void *)transfer_write_func;
    optnum++;

    //fixme:do we need extra ftp cmd

    opts[optnum]=CURLOPT_WRITEDATA;
    params[optnum] = (void *)path;
    optnum++;

	/*for(int i = 0; i < optnum; i++)
	{
	    if(opts[i] == CURLOPT_WRITEFUNCTION)
			continue;
	    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,"%d %s",opts[i],(char*)params[i]);
	}*/
	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "%s to %s", fileurl,path);

    ret = transfer_add_request(ftmanager, opts, params, optnum, OP_TYPE_DOWNLOAD);
    return ret;
}

//for downloaded data to write to file
static int transfer_write_func(void *ptr, size_t size, size_t nmemb, void *stream)
{
    int ret;
    FILE* fp = (FILE*)stream;

    if((fp == NULL) || (ftmanager->mAbort == 1))
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,"null file pointer or app quit");
        return -1;
    }

    ret = fwrite(ptr, size, nmemb, fp);
	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,"transfer_write_func ret %d size %d nmemb %d",ret,size,nmemb);
    //fixme: how to confirm write is over
    return ret;
}

//for uploaded data to read from file
static int transfer_read_func(void *ptr, size_t size, size_t nmemb, void *stream)
{
    FILE* fp = (FILE*)stream;

    if((fp == NULL) || (ftmanager->mAbort == 1))
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,"null file pointer or app quit");
        return CURL_READFUNC_ABORT;
    }

    size_t ret = fread(ptr, size, nmemb, fp);
    if(ret == 0)
    {
        //end of file, all transferred
        if(ferror(fp))
        {
            __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "<transfer_read_func>read file error");
            return CURL_READFUNC_ABORT;
        }
    }
	//__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "read file %d",ret);

    return ret;
}

static int transfer_progress_func(void *ptr, curl_off_t dltotal, curl_off_t dlnow,
                                  curl_off_t ultotal, curl_off_t ulnow)
{
    FileTransferManager* ftm = *((FileTransferManager**)ptr);
    
    //__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "%s dltotal %lld dlnow %lld ultotal %lld ulnow %lld %ld",(const char *)ftm->mCurrentUrl,dltotal,dlnow,ultotal,ulnow,ftm->mCurrentTotalSize);
    if(ftm->mCurrentOpType == OP_TYPE_DOWNLOAD)
    {
        postOperationProgress(OP_TYPE_DOWNLOAD, dlnow,dltotal);
    }
    else if(ftm->mCurrentOpType == OP_TYPE_UPLOAD)
    {
        postOperationProgress(OP_TYPE_UPLOAD, ulnow,ultotal);
    }
	return 0;
}

static int transfer_add_request(FileTransferManager* ftm, int* opts, void** params, int optnum, int optype)
{
    int i;
    int ret;
    int paramsize = 0;

    //fixme: need a lock here to protect mAbort?
    if(ftm->mAbort == 1)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "file transfer runner aborting");
        return 0;
    }
	//__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "ADD %d optnum %d",optype,optnum);
     
    //add a request here, signal runner to wake up if it's waiting
    pthread_mutex_lock(&ftm->mOnNewRequestMutex);
    //add a request
    if(ftm->mPendingReqNum >= MAX_REQUESTS)
    {
        pthread_mutex_unlock(&ftm->mOnNewRequestMutex);
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "too many pending requests");
        return -1;
    }

    RequestOpt* reqp = (RequestOpt*)malloc(sizeof(RequestOpt));
    if(reqp == NULL)
    {
        pthread_mutex_unlock(&ftm->mOnNewRequestMutex);
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "fail to malloc request");
        return -1;
    }

    for(i=0;i<optnum;i++)
    {
        reqp->mOptCmd[i]=opts[i];
        switch((CURLoption)opts[i])
        {
            case CURLOPT_URL:
            case CURLOPT_READDATA:
            case CURLOPT_WRITEDATA:
                if(params[i] == NULL)
                {
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

    ftm->mReqList[ftm->mPendingReqNum] = reqp;
    ftm->mPendingReqNum++;

    pthread_cond_broadcast(&ftm->mOnNewRequestCond);
    pthread_mutex_unlock(&ftm->mOnNewRequestMutex);

	//__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "transfer_add_request %d",ftm->mPendingReqNum);

    return 0;
}

static int transfer_clear_requests(FileTransferManager* ftm)
{
    int i;
    int reqnum;
    //lock and clear and shake and unlock
    pthread_mutex_lock(&ftm->mOnNewRequestMutex);
    reqnum = ftm->mPendingReqNum;
    for(i=0;i<reqnum ;i++)
    {
        RequestOpt* reqp = ftm->mReqList[i];
        void *option = reqp->mOptParam[i];
        //switch((CURLoption)(reqp->mOptParam[i]))
        switch(*((CURLoption*)(option)))
        {
            case CURLOPT_URL:
            case CURLOPT_READDATA:
            case CURLOPT_WRITEDATA:
                if(reqp->mOptParam[i] != NULL)
                {
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
        ftm->mReqList[i] = NULL;
        ftm->mPendingReqNum--;
    }

    //clear all requests here, signal runner to wake up if it's waiting
    pthread_cond_broadcast(&ftm->mOnNewRequestCond);
    pthread_mutex_unlock(&ftm->mOnNewRequestMutex);
    return 0;
}

static int transfer_retrieve_request(FileTransferManager* ftm, RequestOpt** req)
{
    int i;

    *req = ftm->mReqList[0];
    for(i=1;i<ftm->mPendingReqNum;i++)
    {
        ftm->mReqList[i-1] = ftm->mReqList[i];
		ftm->mReqList[i] = NULL;
    }
    ftm->mPendingReqNum--;

    return 0;
}

static void* transfer_runner(void *arg)
{
    FileTransferManager* ftm = (FileTransferManager*)arg;

    //create handle
    ftm->mCurlHandle = curl_easy_init();
    if(ftm->mCurlHandle == NULL)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "error create transfer handle");
        return NULL;
    }

    //run all requests in loop
    pthread_mutex_lock(&ftm->mOnNewRequestMutex);
    while(ftm->mAbort == 0)
    {
        RequestOpt* req;
        int i;
        CURLcode curlret;

        if(ftm->mPendingReqNum > 0)
        {
            transfer_retrieve_request(ftm, &req);
            pthread_mutex_unlock(&ftm->mOnNewRequestMutex);

            ftm->mCurrentOpType = req->mType;
            for(i=0;i<req->mOptNum;i++)
            {
                switch(req->mOptCmd[i])
                {
                case CURLOPT_URL:
                    strcpy(ftm->mCurrentUrl,(const char*)req->mOptParam[i]);
                    curl_easy_setopt(ftm->mCurlHandle, (CURLoption)req->mOptCmd[i], ftm->mCurrentUrl);
                    break;
                case CURLOPT_READDATA:
                    struct stat buf;
                    int ret;
                    
                    if(ftm->mCurrentFile != NULL)
                    {
                        fclose(ftm->mCurrentFile);
                        ftm->mCurrentFile = NULL;
                    }
                    
                    ftm->mCurrentFile = fopen((const char*)req->mOptParam[i], "rb");
                    if(ftm->mCurrentFile == NULL)
                    {
                        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "cant open file!");
                        goto release_request;
                    }
                    
                    strcpy(ftm->mCurrentPath,(const char*)req->mOptParam[i]);
                    ret = stat((const char*)req->mOptParam[i], &buf);
                    if(ret != 0)
                    {
                        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "cant stat file!");
                        goto release_request;
                    }
                    ftm->mCurrentTotalSize = buf.st_size;
                    ftm->mCurrentProcessed= 0;
					curl_easy_setopt(ftm->mCurlHandle,CURLOPT_UPLOAD,1);
                    curl_easy_setopt(ftm->mCurlHandle, (CURLoption)req->mOptCmd[i], ftm->mCurrentFile);
					curl_easy_setopt(ftm->mCurlHandle, CURLOPT_INFILESIZE_LARGE,(curl_off_t)ftm->mCurrentTotalSize);
                    break;
                case CURLOPT_WRITEDATA:					
					//__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "transfer_runner %d",__LINE__);
                    if(ftm->mCurrentFile != NULL)
                    {
                        fclose(ftm->mCurrentFile);
                        ftm->mCurrentFile = NULL;
                    }
					//__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "transfer_runner %d %s",__LINE__,(const char*)req->mOptParam[i]);
                    ftm->mCurrentFile = fopen((const char*)req->mOptParam[i], "wb");
					__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "download %s mCurrentFile %p",(const char*)req->mOptParam[i],ftm->mCurrentFile);
                    if(ftm->mCurrentFile == NULL)
                    {
                        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "cant open file!");
                        goto release_request;
                    }
					//__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "transfer_runner %d %s",__LINE__,(const char*)req->mOptParam[i]);
                    strcpy(ftm->mCurrentPath,(const char*)req->mOptParam[i]);
					curl_easy_setopt(ftm->mCurlHandle,CURLOPT_UPLOAD,0);
                    curl_easy_setopt(ftm->mCurlHandle, (CURLoption)req->mOptCmd[i], ftm->mCurrentFile);
					//__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "transfer_runner %d",__LINE__);
                    break;
                default:
                    curl_easy_setopt(ftm->mCurlHandle, (CURLoption)req->mOptCmd[i], req->mOptParam[i]);
                    break;
                }

            }

            //progression func
            curl_easy_setopt(ftm->mCurlHandle, CURLOPT_NOPROGRESS, 0L);
            curl_easy_setopt(ftm->mCurlHandle, CURLOPT_XFERINFOFUNCTION, transfer_progress_func);
			curl_easy_setopt(ftm->mCurlHandle, CURLOPT_XFERINFODATA, &ftm);

            //execute here
            curlret = curl_easy_perform(ftm->mCurlHandle);
			__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "curl_easy_perform(file)[%d] RETURN %d",ftm->mCurrentOpType,curlret);
            if(curlret != CURLE_OK)
            {
                //fixme: error, need to notify?
                switch(ftm->mCurrentOpType)
                {
                case OP_TYPE_UPLOAD:
                    postUploadResponse((const char *)ftm->mCurrentUrl,(const char *) ftm->mCurrentPath, -1);
                    break;
                case OP_TYPE_DOWNLOAD:
					unlink(ftm->mCurrentPath);
                    postDownloadResponse((const char *) ftm->mCurrentUrl, (const char *) ftm->mCurrentPath, -1);
                    break;
                }
            }
            else
            {
                switch(ftm->mCurrentOpType)
                {
                case OP_TYPE_UPLOAD:
                    postUploadResponse((const char *)ftm->mCurrentUrl, (const char *)ftm->mCurrentPath, 0);
                    break;
                case OP_TYPE_DOWNLOAD:
                    postDownloadResponse((const char *) ftm->mCurrentUrl, (const char *)ftm->mCurrentPath, 0);
                    break;
                }
            }

//release request;
release_request:
            if(ftm->mCurrentFile != NULL)
            {
                fclose(ftm->mCurrentFile);
                ftm->mCurrentFile = NULL;
            }
			for (i = 0; i < req->mOptNum; i++)
            {
				switch ((CURLoption) req->mOptCmd[i])
				{
			    case CURLOPT_URL:
                case CURLOPT_READDATA:
                case CURLOPT_WRITEDATA:
					if(req->mOptParam[i] != NULL)
					{
					    //dont assume error yet, let do_run thread to check
					    free(req->mOptParam[i]);
                        req->mOptParam[i] = NULL;
                        break;
					}
					break;
			    default:
                    req->mOptParam[i] = NULL;
					break;
				}
			}
            free(req);
            pthread_mutex_lock(&ftm->mOnNewRequestMutex);
        }
        else
        {
			pthread_cond_wait(&ftm->mOnNewRequestCond, &ftm->mOnNewRequestMutex);
        }
    }

    //quit and cleanup
    return NULL;
}
