/** @file Looper.h
 *  @par Copyright:
 *  - Copyright 2010 Mavrix Inc as unpublished work
 *  All Rights Reserved
 *  - The information contained herein is the confidential property
 *  of Mavrix.  The use, copying, transfer or disclosure of such information
 *  is prohibited except by express written agreement with Mavrix Inc.
 *  @author   hexibin
 *  @version  1.0
 *  @date     2012/09/10
 *  @par function description:
 */

#include "List.h"
#include "Errors.h"

#ifndef _LOOPER_H_
#define _LOOPER_H_

#ifdef __cplusplus
extern "C" {
#endif

typedef void (*MSG_PROC)(void *param, void *msg);
typedef void (*FREE_FUNC)(void *param, void *data);

typedef struct
{
    int64_t mWhenUs;
    void *mMessage;
} event_t;

/* Looper负责释放post传递进来的msg指针对应的内存，无论消息递送成功或失败 */
typedef struct looper_s
{
    status_t (*start)(struct looper_s *pLooper, int priority, MSG_PROC callBack, void *param);
    status_t (*stop)(struct looper_s *pLooper);
    void (*post)(struct looper_s *pLooper, void *msg, int64_t delayUs);

    pthread_mutex_t mLock;
    pthread_cond_t mCondition;
    pthread_t tid;
    list_t *mEventQueue;
    heap_pool_t *heap;
    MSG_PROC callBack;
    void *param;
    FREE_FUNC free_func;
    void *free_param;
    bool isRunning;
} looper_t;

int64_t get_now_us(void);
int get_ddr_size(size_t *ret);
looper_t *create_looper(int msg_count, FREE_FUNC callBack, void *param);
void delete_looper(looper_t *pLooper);

#ifdef __cplusplus
}
#endif

#endif/*_LOOPER_H_*/


