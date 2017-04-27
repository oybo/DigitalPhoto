/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


#include <sys/time.h>
#include <time.h>

#include "Looper.h"
#ifdef MEMWATCH
#include "memwatch.h"
#endif

int64_t get_now_us(void)
{
    struct timeval tv;

    gettimeofday(&tv, NULL);
    return (int64_t)tv.tv_sec * 1000000ll + tv.tv_usec;
}

int get_ddr_size(size_t *ret)
{
    int fd = -1;
    size_t numread;
    char buf[128];

    if((fd = open("/sys/dma_mem/ddr_mem_size", O_RDONLY)) == -1)
    {
        return -1;
    }

    numread = read(fd, buf, 128 - 1);

    if(numread < 1)
    {
        close(fd);
        return -1;
    }

    buf[numread] = '\0';
    close(fd);
    *ret = (size_t)atoi(buf);

    return 0;
}

static void *looper_loop(void *param)
{
    looper_t *pLooper = (looper_t *)param;
    event_t *event;
    list_t *pList = pLooper->mEventQueue;
    list_node_t *it;
    int64_t whenUs;
    int64_t nowUs;

    while(pLooper->isRunning)
    {
        pthread_mutex_lock(&pLooper->mLock);

        if(pList->isEmpty(pList))
        {
            pthread_cond_wait(&pLooper->mCondition, &pLooper->mLock);
            pthread_mutex_unlock(&pLooper->mLock);
            continue;
        }

        it = pList->begin(pList);
        event = (event_t *)(it->value);
        whenUs = event->mWhenUs;
        nowUs = get_now_us();

        if(whenUs > nowUs)
        {
            struct timespec ts;
            ts.tv_sec = whenUs / 1000000;
            ts.tv_nsec = (whenUs % 1000000) * 1000;
            pthread_cond_timedwait(&pLooper->mCondition, &pLooper->mLock, &ts);
            pthread_mutex_unlock(&pLooper->mLock);
            continue;
        }

        pList->erase(pList, it);
        pthread_mutex_unlock(&pLooper->mLock);

        pLooper->callBack(pLooper->param, event->mMessage);
        if(pLooper->free_func != NULL)
            pLooper->free_func(pLooper->free_param, event->mMessage);
        pLooper->heap->free_item(pLooper->heap, event);
    }

    return NULL;
}

static status_t looper_start(looper_t *pLooper, int priority, MSG_PROC callBack, void *param)
{
    int ret;

    pthread_mutex_lock(&pLooper->mLock);

    if(pLooper->isRunning)
    {
        pthread_mutex_unlock(&pLooper->mLock);
        return INVALID_OPERATION;
    }

    pLooper->callBack = callBack;
    pLooper->param = param;
    pLooper->isRunning = true;

    ret = pthread_create(&pLooper->tid, NULL, looper_loop, (void *)pLooper);
    
    if(ret < 0)
    {
        pLooper->isRunning = false;
        //print_err("create thread fail!");
    }
    
    pthread_mutex_unlock(&pLooper->mLock);
    
    return OK;
}

static status_t looper_stop(looper_t *pLooper)
{
    event_t *event;
    list_t *pList = pLooper->mEventQueue;
    list_node_t *it;

    if(!pLooper->isRunning)
    {
        return INVALID_OPERATION;
    }

    pLooper->isRunning = false;
    pthread_cond_signal(&pLooper->mCondition);
    pthread_join(pLooper->tid, NULL);

    pthread_mutex_lock(&pLooper->mLock);

    while(!pList->isEmpty(pList))
    {
        it = pList->begin(pList);
        event = (event_t *)(it->value);
        pList->erase(pList, it);
        if(pLooper->free_func != NULL)
            pLooper->free_func(pLooper->free_param, event->mMessage);
        pLooper->heap->free_item(pLooper->heap, event);
    }

    pthread_mutex_unlock(&pLooper->mLock);
    return OK;
}

static void looper_post(looper_t *pLooper, void *msg, int64_t delayUs)
{
    int64_t whenUs;
    event_t *event;
    list_t *pList;
    list_node_t *it;

    if((NULL == pLooper) || (NULL == msg))
    {
        if((pLooper->free_func != NULL) && (msg != NULL))
            pLooper->free_func(pLooper->free_param, msg);
        return;
    }

    pthread_mutex_lock(&pLooper->mLock);

    if(!pLooper->isRunning)
    {
        if(pLooper->free_func != NULL)
            pLooper->free_func(pLooper->free_param, msg);
        pthread_mutex_unlock(&pLooper->mLock);
        return;
    }

    if(delayUs > 0)
    {
        whenUs = get_now_us() + delayUs;
    }
    else
    {
        whenUs = get_now_us();
    }

    pList = pLooper->mEventQueue;
    it = pList->begin(pList);

    while(it != NULL)
    {
        event = (event_t *)it->value;

        if(event->mWhenUs > whenUs)
        {
            break;
        }

        it = it->next;
    }

    event = pLooper->heap->malloc_item(pLooper->heap);

    if(NULL == event)
    {
        if(pLooper->free_func != NULL)
            pLooper->free_func(pLooper->free_param, msg);
        pthread_mutex_unlock(&pLooper->mLock);
        return;
    }

    event->mWhenUs = whenUs;
    event->mMessage = msg;

    if(it == pList->begin(pList))
    {
        pthread_cond_signal(&pLooper->mCondition);
    }

    pList->insert(pList, it, event);
    pthread_mutex_unlock(&pLooper->mLock);
}

looper_t *create_looper(int msg_count, FREE_FUNC callBack, void *param)
{
    looper_t *pLooper;

    pLooper = malloc(sizeof(looper_t));

    if(NULL == pLooper)
    {
        return NULL;
    }

    pthread_mutex_init(&pLooper->mLock, NULL);
    pthread_cond_init(&pLooper->mCondition, NULL);
    
    pLooper->mEventQueue = create_list(msg_count);
    pLooper->heap = create_heap_pool(sizeof(event_t), msg_count);
    pLooper->callBack = NULL;
    pLooper->param = NULL;
    pLooper->free_func = callBack;
    pLooper->free_param = param;
    pLooper->isRunning = false;

    pLooper->start = looper_start;
    pLooper->stop = looper_stop;
    pLooper->post = looper_post;
    return pLooper;
}

void delete_looper(looper_t *pLooper)
{
    looper_stop(pLooper);
    delete_list(pLooper->mEventQueue);
    delete_heap_pool(pLooper->heap);
    pthread_mutex_destroy(&pLooper->mLock);
    pthread_cond_destroy(&pLooper->mCondition);
    free(pLooper);
}

