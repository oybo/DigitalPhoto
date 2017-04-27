/** @file actcom.c
 *  @par Copyright:
 *  - Copyright 2010 Mavrix Inc as unpublished work
 *  All Rights Reserved
 *  - The information contained herein is the confidential property
 *  of Mavrix.  The use, copying, transfer or disclosure of such information
 *  is prohibited except by express written agreement with Mavrix Inc.
 *  @author   hexibin
 *  @version  1.0
 *  @date     2016/6/7
 *  @par function description:
 */


#include "Looper.h"
#include <dlfcn.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <sys/ioctl.h>
#include <net/if.h>
#include <arpa/inet.h>
#include <netinet/tcp.h>
#include "ActCommunication.h"

#ifdef MEMWATCH
#include "memwatch.h"
#endif


//server socket port
#define TCP_SERVER_PORT               (3046)
//֧�����ӵĿͻ�������
#define TCP_SERVER_ACCEPT_CNT           (40)

#define AC_MAX_CMD_SIZE               (5*1024)
#define AC_MAX_CMD_INIT_SIZE          (256)
#define AC_MAX_CMD_STEP_SIZE          (256)
#define AC_MAX_DATA_SIZE               (50*1024)


#define AC_KEY_SIZE             "dataSize:"
#define AC_MAX_IP_LEN              (16)
#define AC_SOCKET_CNT              (2)

#define LOG_TAG    "ActCommunication"

typedef struct
{
    char *pCmdBuffer;
    uint32_t mCmdDataLen;

    uint32_t mRefCount;
}ac_msg_t;

typedef struct tcp_sock_s
{
    int32_t mSockFd;
    int8_t bSockError;
    int8_t mConnecting;   // 0: no connect, 1: connecting, 2: connected

    //recv....
    char *pCmdBuffer;
    uint32_t mCmdBufferSize;
    uint32_t mCmdDataLen;

    char *pDataBuffer;
    uint32_t mDataBufferSize;
    //���ڽ��ܵ����ݵ��ܴ�С
    uint32_t mTotalSize;
    //�Ѿ����ܵ�������
    uint32_t mRecvSize;

    //send....
    list_t *pMsgList;
    uint32_t mSendLen;
}tcp_sock_t;


typedef struct ac_server_s
{
    int32_t mPipeFd[2];   /* 0: read, 1: write */
    tcp_sock_t pSockList[AC_SOCKET_CNT];
    char ip[AC_MAX_IP_LEN];

    looper_t *mLooper;
    heap_pool_t *mMsgHeap;
    int32_t mMsgGeneration;
    int8_t mRunning;
    int8_t mStopPending;
    int8_t mConnected;

    AcEventListener *cb;
    pthread_mutex_t mLock;
    pthread_cond_t mCondition;
} ac_server_t;

typedef struct
{
    int32_t type;
    int32_t generation;
    void *param1;
    void *param2;
    char ip[AC_MAX_IP_LEN];
} as_message_t;

enum
{
    kWhatMonitorNetwork,
    kWhatStop,
    kWhatSendMsg,
    kWhatRegisterCb,
    kWhatUnregisterCb,
    kWhatConnect,
    kWhatDisconnect
};


static as_message_t *create_as_message(ac_server_t *s, int32_t type, int32_t generation, void *param1, void *param2)
{
    as_message_t *msg;

    msg = s->mMsgHeap->malloc_item(s->mMsgHeap);

    if(NULL == msg)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "malloc fail, msg type=%d!\n", type);
        return NULL;
    }

    msg->type = type;
    msg->generation = generation;
    msg->param1 = param1;
    msg->param2 = param2;

    return msg;
}

static void postMonitorQueue(ac_server_t *s, int32_t type, int64_t delayUs)
{
    as_message_t *msg;

    s->mMsgGeneration++;
    msg = create_as_message(s, type, s->mMsgGeneration, NULL, NULL);

    if(NULL != msg)
    {
        s->mLooper->post(s->mLooper, msg, delayUs);
    }
}

static int32_t AsMakeSocketNonBlocking(int32_t s)
{
    int32_t res;
    int32_t flags;

    flags = fcntl(s, F_GETFL, 0);
    if (flags < 0)
    {
        flags = 0;
    }

    res = fcntl(s, F_SETFL, flags | O_NONBLOCK);
    if (res < 0)
    {
        return -1;
    }

    return 0;
}

static void _as_set_socket_keepalive(int32_t s)
{
    int keepAlive = 1;//�趨KeepAlive
    int keepIdle = 8;//��ʼ�״�KeepAlive̽��ǰ��TCP�ձ�ʱ��
    int keepInterval = 4;//����KeepAlive̽����ʱ����
    int keepCount = 5;//�ж��Ͽ�ǰ��KeepAlive̽�����

    //����keepalive��⣬���ͻ��˱��������������粻�ɴ�ȣ�30��Ͽ�����
    if(setsockopt(s,SOL_SOCKET,SO_KEEPALIVE,(void*)&keepAlive,sizeof(keepAlive)) == -1)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "setsockopt SO_KEEPALIVE error!\n");
    }
    if(setsockopt(s,SOL_TCP,TCP_KEEPIDLE,(void *)&keepIdle,sizeof(keepIdle)) == -1)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "setsockopt TCP_KEEPIDLE error!\n");
    }
    if(setsockopt(s,SOL_TCP,TCP_KEEPINTVL,(void *)&keepInterval,sizeof(keepInterval)) == -1)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "setsockopt TCP_KEEPINTVL error!\n");
    }
    if(setsockopt(s,SOL_TCP,TCP_KEEPCNT,(void *)&keepCount,sizeof(keepCount)) == -1)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "setsockopt TCP_KEEPCNT error!\n");
    }
}

static char* as_get_value(char *buffer, char *key, char *value, int32_t value_len)
{
    char *p;
    char *pValue;
    char *end;
    int32_t len;

    p = strstr(buffer, key);
    if(NULL == p)
        return NULL;

    pValue = p + strlen(key);
    end = strstr(pValue, "\r\n");
    len = end - pValue;

    if(value != NULL)
    {
        if((len + 1) > value_len)
        {
            __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "found key, but value too long, value=%.*s, buffer size=%d\n",
                len, pValue, value_len);
            return NULL;
        }
        memcpy(value, pValue, len);
        value[len] = 0;
    }
    return pValue;
}

static void _free_msg(ac_msg_t *pCmdMsg)
{
    pCmdMsg->mRefCount--;
    if(pCmdMsg->mRefCount == 0)
        free(pCmdMsg);
}

static int32_t _create_tcp_socket(ac_server_t *as, tcp_sock_t *pSock, char *ip)
{
    int32_t clientSocket;
    int32_t ret;
    struct sockaddr_in servaddr;
    struct hostent *ent;

    clientSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (clientSocket == -1)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "create socket fail");
        pSock->bSockError = 1;
        return -1;
    }

    AsMakeSocketNonBlocking(clientSocket);
    _as_set_socket_keepalive(clientSocket);

    bzero(&servaddr, sizeof(servaddr));
    ent = gethostbyname(ip);
    if(NULL == ent)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "gethostbyname fail, ip=%s", ip);
        close(clientSocket);
        pSock->bSockError = 1;
        return -1;
    }
    
    servaddr.sin_family = AF_INET;
    servaddr.sin_port = htons(TCP_SERVER_PORT);
    servaddr.sin_addr.s_addr = *(uint32_t *)(ent->h_addr);

    ret = connect(clientSocket, (struct sockaddr *)&servaddr, sizeof(struct sockaddr));
    if((ret < 0) && (errno != EINPROGRESS))
    {
        close(clientSocket);
        pSock->bSockError = 1;
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "socket connect fail, ret=%d, errno=%d", ret, errno);
        return -1;
    }

    pSock->mSockFd = clientSocket;
    pSock->bSockError = 0;
    pSock->mConnecting = 1;
    pSock->pCmdBuffer = malloc(AC_MAX_CMD_INIT_SIZE);
    pSock->mCmdBufferSize = AC_MAX_CMD_INIT_SIZE;
    pSock->pMsgList = create_list(20);
    
    return 0;
}

static void _remove_client(ac_server_t *as, tcp_sock_t *pSock)
{
    pSock->bSockError = 0;
    pSock->mConnecting = 0;
    if(pSock->mSockFd >= 0)
    {
        close(pSock->mSockFd);
        pSock->mSockFd = -1;
        if((as->cb != NULL) && (as->mConnected == 1))
            as->cb->onClientDisconnect();
    }

    pSock->mCmdBufferSize = 0;
    pSock->mCmdDataLen = 0;
    if(pSock->pCmdBuffer != NULL)
    {
        free(pSock->pCmdBuffer);
        pSock->pCmdBuffer = NULL;
    }

    pSock->mDataBufferSize = 0;
    pSock->mTotalSize = 0;
    pSock->mRecvSize = 0;
    if(pSock->pDataBuffer != NULL)
    {
        free(pSock->pDataBuffer);
        pSock->pDataBuffer = NULL;
    }

    pSock->mSendLen = 0;
    if(pSock->pMsgList != NULL)
    {
        list_node_t *it;

        it = pSock->pMsgList->begin(pSock->pMsgList);
        while(it != NULL)
        {
            _free_msg((ac_msg_t *)it->value);
            pSock->pMsgList->erase(pSock->pMsgList, it);
            it = pSock->pMsgList->begin(pSock->pMsgList);
        }
        delete_list(pSock->pMsgList);
        pSock->pMsgList = NULL;
    }

    as->mConnected = 0;
}

static void _remove_all_client(ac_server_t *as)
{
    int32_t i;

    for(i=0; i<AC_SOCKET_CNT; i++)
    {
        _remove_client(as, &as->pSockList[i]);
    }
}

static int32_t _on_recv_cmd(ac_server_t *as, tcp_sock_t *pSock)
{
    int32_t count = 0;
    char *msg[AC_MAX_CMD_ITEM];
    char *p;
    char *pTail;
    
    if(as->cb == NULL)
        goto OUT;

    p = pSock->pCmdBuffer;
    while(p != NULL)
    {
        pTail = strstr(p, "\r\n");
        if(pTail == NULL)
            break;

        *pTail = 0;
        msg[count] = p;
        p = strchr(p, ':');
        if(p == NULL)
        {
            __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "msg format err: %s\n", pSock->pCmdBuffer);
            goto OUT;
        }

        *p = 0;
        p++;
        msg[count+1] = p;
        count += 2;
        p = pTail + 2;

        if(count == AC_MAX_CMD_ITEM)
            break;
    }

    if((count == 0) && (pSock->mTotalSize == 0))
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "msg format err: %s\n", pSock->pCmdBuffer);
        goto OUT;
    }
    
    if(pSock->mTotalSize > 0)
    {
        as->cb->onRecvMsg(
            msg, count,
            pSock->pDataBuffer, pSock->mTotalSize);
    }
    else
    {
        as->cb->onRecvMsg(msg, count, NULL, 0);
    }

OUT:
    pSock->mTotalSize = 0;
    return 0;
}

//�ɶ�״̬��read����0��ʾclient�ر�socket
static int32_t _read_from_client(ac_server_t *as, tcp_sock_t *pSock)
{
    int32_t ret;
    char *p;
    char *pValue;

    if(pSock->mTotalSize > 0)
    {
AGAIN2:
        ret = recv(pSock->mSockFd,
            pSock->pDataBuffer + pSock->mRecvSize,
            pSock->mTotalSize - pSock->mRecvSize, 0);
        if(ret <= 0)
        {
            if((ret < 0) && (errno == EINTR))
                goto AGAIN2;

            pSock->bSockError = 1;
            __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "socket read error, remove client, ret=%d, errno=%d\n", ret, errno);
            return -1;
        }

        pSock->mRecvSize += ret;
        if(pSock->mRecvSize == pSock->mTotalSize)
        {
            _on_recv_cmd(as, pSock);
            pSock->mCmdDataLen = 0;
        }
        return 0;
    }

    if(pSock->mCmdBufferSize == (pSock->mCmdDataLen + 1))
    {
        pSock->mCmdBufferSize += AC_MAX_CMD_STEP_SIZE;
        if(pSock->mCmdBufferSize > AC_MAX_CMD_SIZE)
        {
            pSock->bSockError = 1;
            __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "cmd to long: %s\n", pSock->pCmdBuffer);
            return -1;
        }
        pSock->pCmdBuffer = realloc(pSock->pCmdBuffer, pSock->mCmdBufferSize);
    }
    
AGAIN:
    ret = recv(pSock->mSockFd,
        pSock->pCmdBuffer + pSock->mCmdDataLen,
        pSock->mCmdBufferSize - pSock->mCmdDataLen - 1, 0);
    if(ret <= 0)
    {
        if((ret < 0) && (errno == EINTR))
            goto AGAIN;

        pSock->bSockError = 1;
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "socket read error, remove client, ret=%d, errno=%d\n", ret, errno);
        return -1;
    }

    pSock->mCmdDataLen += ret;
    pSock->pCmdBuffer[pSock->mCmdDataLen] = 0;

NEXT_CMD:
    p = strstr(pSock->pCmdBuffer, "\r\n\r\n");
    if(p == NULL)
        return 0;

    p += 3;
    *p = 0;
    p++;
    
    pValue = as_get_value(pSock->pCmdBuffer, AC_KEY_SIZE, NULL, 0);
    if(pValue == NULL)
    {
        _on_recv_cmd(as, pSock);
        
        pSock->mCmdDataLen -= p - pSock->pCmdBuffer;
        memmove(pSock->pCmdBuffer, p, pSock->mCmdDataLen);
    }
    else
    {
        uint32_t dataSize = strtol(pValue, NULL, 0);
        uint32_t cmdSize = p - pSock->pCmdBuffer;
        uint32_t recvDataSize = pSock->mCmdDataLen - cmdSize;
        
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "datasize=%d\n", dataSize);
        if(dataSize > AC_MAX_DATA_SIZE)
        {
            pSock->bSockError = 1;
            __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "data to long: %s\n", pSock->pCmdBuffer);
            return -1;
        }

        if(dataSize > pSock->mDataBufferSize)
        {
            if(pSock->pDataBuffer)
                free(pSock->pDataBuffer);
            pSock->pDataBuffer = malloc(dataSize);
            pSock->mDataBufferSize = dataSize;
        }

        pSock->mTotalSize = dataSize;
        pSock->mRecvSize = recvDataSize;

        if(recvDataSize == 0)
            return 0;
        if(recvDataSize < dataSize)
        {
            memcpy(pSock->pDataBuffer, p, recvDataSize);
            return 0;
        }

        pSock->mRecvSize = dataSize;
        memcpy(pSock->pDataBuffer, p, dataSize);
        _on_recv_cmd(as, pSock);

        p += dataSize;
        pSock->mCmdDataLen -= p - pSock->pCmdBuffer;
        if(recvDataSize > dataSize)
            memmove(pSock->pCmdBuffer, p, pSock->mCmdDataLen);
    }
    
    goto NEXT_CMD;
}

static void _on_socket_connected(ac_server_t *as)
{
    int32_t i;

    for(i=0; i<AC_SOCKET_CNT; i++)
    {
        if(as->pSockList[i].mConnecting != 2)
            return;
    }

    as->mConnected = 1;
    if(as->cb != NULL)
        as->cb->onClientConnect();
}

static int32_t _send_data_to_client(ac_server_t *as, tcp_sock_t *pSock)
{
    int32_t ret;
    ac_msg_t *pMsg;
    list_node_t *it;
    
    if(pSock->bSockError == 1)
        return -1;

    if(pSock->mConnecting == 1)
    {
        pSock->mConnecting = 2;
        _on_socket_connected(as);
        return 0;
    }

    it = pSock->pMsgList->begin(pSock->pMsgList);
    if(it == NULL)
        return -1;
    
    pMsg = (ac_msg_t*)it->value;
    
AGAIN:
    ret = send(pSock->mSockFd,
        pMsg->pCmdBuffer + pSock->mSendLen,
        pMsg->mCmdDataLen - pSock->mSendLen, 0);
    if(ret <= 0)
    {
        if((ret < 0) && (errno == EINTR))
            goto AGAIN;

        pSock->bSockError = 1;
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "socket write error, remove client, ret=%d, errno=%d\n", ret, errno);
        return -1;
    }

    pSock->mSendLen += ret;
    if(pSock->mSendLen == pMsg->mCmdDataLen)
    {
        pSock->mSendLen = 0;
        _free_msg(pMsg);
        
        pSock->pMsgList->erase(pSock->pMsgList, it);
    }

    return 0;
}

static void _as_monitor_network(ac_server_t *as)
{
    fd_set rs, ws;
    tcp_sock_t *pSock;
    int32_t maxFd = 0;
    struct timeval tv;
    int32_t ret;
    int32_t retval = 0;
    char tmp;
    int32_t i;

    FD_ZERO(&rs);
    FD_ZERO(&ws);

    FD_SET(as->mPipeFd[0], &rs);
    maxFd = (maxFd > as->mPipeFd[0]) ? maxFd : as->mPipeFd[0];

    for(i=0; i<AC_SOCKET_CNT; i++)
    {
        pSock = &as->pSockList[i];
        if(pSock->bSockError == 1)
        {
            _remove_client(as, pSock);
            retval += _create_tcp_socket(as, pSock, as->ip);
        }
        if(pSock->mSockFd < 0)
            continue;

        if(pSock->mConnecting == 2)
        {
            FD_SET(pSock->mSockFd, &rs);
            maxFd = (maxFd > pSock->mSockFd) ? maxFd : pSock->mSockFd;
        }
        
        if((!pSock->pMsgList->isEmpty(pSock->pMsgList))
            || (pSock->mConnecting == 1))
        {
            FD_SET(pSock->mSockFd, &ws);
            maxFd = (maxFd > pSock->mSockFd) ? maxFd : pSock->mSockFd;
        }
    }

    tv.tv_sec = 1;
    tv.tv_usec = 0;

    ret = select(maxFd + 1, &rs, &ws, NULL, &tv);
    if(ret <= 0)
    {
        postMonitorQueue(as, kWhatMonitorNetwork, 0);
        return ;
    }
    
    if(FD_ISSET(as->mPipeFd[0], &rs))
    {
        read(as->mPipeFd[0], &tmp, 1);
    }

    for(i=0; i<AC_SOCKET_CNT; i++)
    {
        pSock = &as->pSockList[i];
        if(pSock->mSockFd < 0)
            continue;
        if(FD_ISSET(pSock->mSockFd, &rs))
        {
            retval += _read_from_client(as, pSock);
        }
        if(FD_ISSET(pSock->mSockFd, &ws)
            && (pSock->bSockError == 0))
        {
            retval += _send_data_to_client(as, pSock);
        }
    }

    if(retval < 0)
        postMonitorQueue(as, kWhatMonitorNetwork, 1000000);
    else
        postMonitorQueue(as, kWhatMonitorNetwork, 0);
}

static int32_t _as_stop(ac_server_t *as)
{
    pthread_mutex_lock(&as->mLock);
    as->mStopPending = 0;
    as->mRunning = 0;
    as->mMsgGeneration++;
    pthread_cond_broadcast(&as->mCondition);

    pthread_mutex_unlock(&as->mLock);

    return 0;
}

static void _as_send_msg(ac_server_t *as, ac_msg_t *pCmdMsg, int32_t data_len)
{
    tcp_sock_t *pSock;

    if(data_len > 0)
        pSock = &as->pSockList[1];
    else
        pSock = &as->pSockList[0];

    if(pSock->mConnecting == 2)
    {
        pSock->pMsgList->pushBack(pSock->pMsgList, pCmdMsg);
        pCmdMsg->mRefCount++;
    }
    if(pCmdMsg->mRefCount == 0)
        free(pCmdMsg);
}

static void _as_disconnect(ac_server_t *as)
{
    _remove_all_client(as);
}

static void _as_connect(ac_server_t *as, char *ip)
{
    int32_t i;
    
    _as_disconnect(as);
    strcpy(as->ip, ip);
    
    for(i=0; i<AC_SOCKET_CNT; i++)
    {
        _create_tcp_socket(as, &as->pSockList[i], ip);
    }
}

static void as_message_received(void *param, void *msg)
{
    ac_server_t *as = (ac_server_t *)param;
    as_message_t *pMsg = (as_message_t*)msg;

    switch(pMsg->type)
    {
    case kWhatMonitorNetwork:
    {
        if(pMsg->generation != as->mMsgGeneration)
        {
            // Stale event
            break;
        }

        _as_monitor_network(as);
        break;
    }

    case kWhatStop:
        _as_stop(as);
        break;

    case kWhatRegisterCb:
        as->cb = (AcEventListener*)pMsg->param1;
        break;
    case kWhatUnregisterCb:
        as->cb = NULL;
        break;
    case kWhatConnect:
        _as_connect(as, pMsg->ip);
        break;
    case kWhatDisconnect:
        _as_disconnect(as);
        break;

    case kWhatSendMsg:
        _as_send_msg(as, (ac_msg_t *)pMsg->param1, (int32_t)pMsg->param2);
        break;
    
    default:
        break;
    }
}

/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

int actcom_sendMsg(int ac_handle, const char *msg[], int count, void *data, int data_len)
{
    as_message_t *pMsg;
    ac_server_t *as = (ac_server_t*)ac_handle;
    int32_t i;
    int32_t size;
    int32_t cmdSize;
    char *p;
    char value[20];
    ac_msg_t *pCmdMsg;
    char *pDataBuffer;

    if((ac_handle == 0)
        || ((count % 2) != 0)
        || (count < 0)
        || (count > AC_MAX_CMD_ITEM)
        || ((count == 0) && (data_len == 0)))
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "param err!\n");
        return -1;
    }

    i = 0;
    cmdSize = 2;
    while(i < count)
    {
        cmdSize += strlen(msg[i]) + strlen(msg[i+1]) + 3;
        i += 2;
    }
    if(data_len > 0)
    {
        sprintf(value, "%d", data_len);
        cmdSize += strlen(AC_KEY_SIZE) + strlen(value) + 2;
    }

    if(cmdSize >= AC_MAX_CMD_SIZE)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "cmd size too large: %d\n", cmdSize);
        return -1;
    }
    if(data_len >= AC_MAX_DATA_SIZE)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "data size too large: %d\n", data_len);
        return -1;
    }
    
    size = sizeof(ac_msg_t) + cmdSize + 1 + data_len;
    pCmdMsg = (ac_msg_t*)malloc(size);
    if(NULL == pCmdMsg)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "malloc fail: %d\n", size);
        return -1;
    }

    pCmdMsg->mRefCount = 0;
    pCmdMsg->mCmdDataLen = cmdSize;
    pCmdMsg->pCmdBuffer = (char*)pCmdMsg + sizeof(ac_msg_t);

    i = 0;
    p = pCmdMsg->pCmdBuffer;
    while(i < count)
    {
        cmdSize = strlen(msg[i]) + strlen(msg[i+1]) + 3;
        sprintf(p, "%s:%s\r\n", msg[i], msg[i+1]);
        p += cmdSize;
        i += 2;
    }
    if(data_len > 0)
    {
        cmdSize = strlen(AC_KEY_SIZE) + strlen(value) + 2;
        sprintf(p, "%s%s\r\n", AC_KEY_SIZE, value);
        p += cmdSize;
    }
    sprintf(p, "\r\n");

    if(data_len > 0)
    {
        pDataBuffer = pCmdMsg->pCmdBuffer + cmdSize;
        memcpy(pDataBuffer, data, data_len);
        pCmdMsg->mCmdDataLen += data_len;
    }
    
    pthread_mutex_lock(&as->mLock);

    if((as->mRunning == 0)
        || (as->mStopPending == 1)
        || (as->mConnected == 0))
    {
        free(pCmdMsg);
        pthread_mutex_unlock(&as->mLock);
        return -1;
    }

    pMsg = create_as_message(as, kWhatSendMsg, 0, pCmdMsg, (void*)data_len);
    if(NULL == pMsg)
    {
        pthread_mutex_unlock(&as->mLock);
        free(pCmdMsg);
        return -1;
    }

    as->mLooper->post(as->mLooper, pMsg, 0);
    write(as->mPipeFd[1], as, 1);

    pthread_mutex_unlock(&as->mLock);

    return 0;
}

int actcom_register_event_listener(int ac_handle, AcEventListener *cb)
{
    as_message_t *msg;
    ac_server_t *as = (ac_server_t*)ac_handle;

    pthread_mutex_lock(&as->mLock);

    msg = create_as_message(as, kWhatRegisterCb, 0, cb, NULL);
    if(NULL == msg)
    {
        pthread_mutex_unlock(&as->mLock);
        return -1;
    }

    as->mLooper->post(as->mLooper, msg, 0);
    write(as->mPipeFd[1], as, 1);

    pthread_mutex_unlock(&as->mLock);

    return 0;
}

int actcom_unregister_event_listener(int ac_handle)
{
    as_message_t *msg;
    ac_server_t *as = (ac_server_t*)ac_handle;

    pthread_mutex_lock(&as->mLock);

    msg = create_as_message(as, kWhatUnregisterCb, 0, NULL, NULL);
    if(NULL == msg)
    {
        pthread_mutex_unlock(&as->mLock);
        return -1;
    }

    as->mLooper->post(as->mLooper, msg, 0);
    write(as->mPipeFd[1], as, 1);

    pthread_mutex_unlock(&as->mLock);

    return 0;
}

int actcom_connect(int ac_handle, const char *ip)
{
    as_message_t *msg;
    ac_server_t *as = (ac_server_t*)ac_handle;

    if(strlen(ip) >= AC_MAX_IP_LEN)
    {
        return -1;
    }
    
    pthread_mutex_lock(&as->mLock);

    msg = create_as_message(as, kWhatConnect, 0, NULL, NULL);
    if(NULL == msg)
    {
        pthread_mutex_unlock(&as->mLock);
        return -1;
    }

    strcpy(msg->ip, ip);
    as->mLooper->post(as->mLooper, msg, 0);
    write(as->mPipeFd[1], as, 1);

    pthread_mutex_unlock(&as->mLock);

    return 0;
}

int actcom_disconnect(int ac_handle)
{
    as_message_t *msg;
    ac_server_t *as = (ac_server_t*)ac_handle;

    pthread_mutex_lock(&as->mLock);

    msg = create_as_message(as, kWhatDisconnect, 0, NULL, NULL);
    if(NULL == msg)
    {
        pthread_mutex_unlock(&as->mLock);
        return -1;
    }

    as->mLooper->post(as->mLooper, msg, 0);
    write(as->mPipeFd[1], as, 1);

    pthread_mutex_unlock(&as->mLock);

    return 0;
}


static int32_t actcom_stop(ac_server_t *as)
{
    as_message_t *msg;

    pthread_mutex_lock(&as->mLock);
    if(as->mRunning == 0)
    {
        pthread_mutex_unlock(&as->mLock);
        return 0;
    }

    msg = create_as_message(as, kWhatStop, 0, NULL, NULL);
    if(NULL == msg)
    {
        pthread_mutex_unlock(&as->mLock);
        return -1;
    }

    as->mStopPending = 1;
    as->mLooper->post(as->mLooper, msg, 0);
    write(as->mPipeFd[1], as, 1);

    while(as->mStopPending)
    {
        pthread_cond_wait(&as->mCondition, &as->mLock);
    }

    pthread_mutex_unlock(&as->mLock);

    return 0;
}

int actcom_open(void)
{
    ac_server_t *as;
    int32_t ret;

    as = malloc(sizeof(ac_server_t));
    if(NULL == as)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "malloc fail!");
        return 0;
    }

    memset(as, 0, sizeof(ac_server_t));
    as->mPipeFd[0] = -1;
    as->mPipeFd[1] = -1;
    as->pSockList[0].mSockFd = -1;
    as->pSockList[1].mSockFd = -1;
    
    pthread_mutex_init(&as->mLock, NULL);
    pthread_cond_init(&as->mCondition, NULL);

    ret = pipe(as->mPipeFd);
    if(ret < 0)
        goto ERROUT;

    AsMakeSocketNonBlocking(as->mPipeFd[0]);
    AsMakeSocketNonBlocking(as->mPipeFd[1]);

    as->mMsgHeap = create_heap_pool(sizeof(as_message_t), 5);
    if(NULL == as->mMsgHeap)
        goto ERROUT;

    as->mLooper = create_looper(5, (FREE_FUNC)as->mMsgHeap->free_item, as->mMsgHeap);
    if(NULL == as->mLooper)
        goto ERROUT;

    as->mLooper->start(as->mLooper, 0, as_message_received, as);
    as->mRunning = 1;
    postMonitorQueue(as, kWhatMonitorNetwork, 0);
    
    return (int32_t)as;

ERROUT:
    actcom_close((int32_t)as);
    return 0;
}

void actcom_close(int ac_handle)
{
    ac_server_t *as = (ac_server_t*)ac_handle;
    
    if(as == NULL)
        return;

    actcom_stop(as);
    if(NULL != as->mLooper)
    {
        as->mLooper->stop(as->mLooper);
        delete_looper(as->mLooper);
    }

    if(NULL != as->mMsgHeap)
    {
        delete_heap_pool(as->mMsgHeap);
    }

    _remove_all_client(as);
    if(as->mPipeFd[1] >= 0)
        close(as->mPipeFd[1]);
    if(as->mPipeFd[0] >= 0)
        close(as->mPipeFd[0]);

    pthread_mutex_destroy(&as->mLock);
    pthread_cond_destroy(&as->mCondition);
    
    free(as);
}


