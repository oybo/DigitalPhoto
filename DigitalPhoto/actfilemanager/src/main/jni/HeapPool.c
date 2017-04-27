/** @file HeapPool.c
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


#include "HeapPool.h"

#ifdef MEMWATCH
#include "memwatch.h"
#endif


#define PAGE_MAGIC    0x1f1f1f1f

enum
{
    PAGE_FLAG_FREE = 0,
    PAGE_FLAG_USED,
    PAGE_FLAG_MALLOC
};

typedef struct
{
    int flag;
#if MEMORY_CHECK
    int magic;
#endif
} item_t;

static void *malloc_item(heap_pool_t *pool)
{
    int i = 0;
    item_t *item;

    if(NULL == pool)
    {
        return NULL;
    }

    while(i < pool->count)
    {
        i++;
        pool->next_index++;

        if(pool->next_index >= pool->count)
        {
            pool->next_index = 0;
        }

        item = (item_t *)(pool->buffer + pool->size * pool->next_index);
#if MEMORY_CHECK

        if(item->magic != PAGE_MAGIC)
        {
            print_err("memory was modify, index=%d!", pool->next_index);
        }

#endif

        if(item->flag == PAGE_FLAG_FREE)
        {
            pool->freeCount--;
            item->flag = PAGE_FLAG_USED;
#if MEMORY_CHECK
            pool->need_malloc = 0;
#endif
            return (void *)item + sizeof(item_t);
        }
    }

    //print_err("malloc new item, size=%d!", pool->size);
    item = malloc(pool->size);
#if MEMORY_CHECK
    pool->need_malloc = 1;
#endif

    if(NULL == item)
    {
        return NULL;
    }

    item->flag = PAGE_FLAG_MALLOC;
#if MEMORY_CHECK
    item->magic = PAGE_MAGIC;
#endif
    return (void *)item + sizeof(item_t);
}

static void free_item(heap_pool_t *pool, void *data)
{
    item_t *item;
#if MEMORY_CHECK
    int a;
    int b;
#endif

    item = (item_t *)(data - sizeof(item_t));

    if(item->flag == PAGE_FLAG_MALLOC)
    {
#if MEMORY_CHECK

        if(item->magic != PAGE_MAGIC)
        {
            print_err("malloc item was modify!");
        }

#endif
        free(item);
        return;
    }

#if MEMORY_CHECK

    do
    {
        if((int)item < (int)pool->buffer)
        {
            goto ERROUT;
        }

        a = ((int)item - (int)pool->buffer) / pool->size;
        b = ((int)item - (int)pool->buffer) % pool->size;

        if((a >= pool->count) || (b != 0))
        {
            goto ERROUT;
        }

        if((item->flag != PAGE_FLAG_USED) || (item->magic != PAGE_MAGIC))
        {
            goto ERROUT;
        }

        break;
    ERROUT:
        print_err("free item memory was modify!");
        //return ;
    }
    while(0);

#endif
    item->flag = PAGE_FLAG_FREE;
    pool->freeCount++;
}

static int heap_need_malloc(heap_pool_t *pool)
{
#if MEMORY_CHECK
    return pool->need_malloc;
#else
    return 0;
#endif
}

static int heap_free_size(heap_pool_t *pool)
{
    return pool->freeCount * pool->size;
}

heap_pool_t *create_heap_pool(int size, int count)
{
    int i = 0;
    heap_pool_t *heap_pool;
    item_t *item;

    heap_pool = malloc(sizeof(heap_pool_t));

    if(NULL == heap_pool)
    {
        //print_err("malloc error!");
        return NULL;
    }

    heap_pool->malloc_item = malloc_item;
    heap_pool->free_item = free_item;
    heap_pool->heap_need_malloc = heap_need_malloc;
    heap_pool->free_size = heap_free_size;

    heap_pool->count = count;
    heap_pool->freeCount = count;
    heap_pool->size = (((size + 3) >> 2) << 2) + sizeof(item_t);
    heap_pool->next_index = -1;
#if MEMORY_CHECK
    heap_pool->need_malloc = 0;
#endif

    heap_pool->buffer = malloc(heap_pool->size * heap_pool->count);

    if(NULL == heap_pool->buffer)
    {
        free(heap_pool);
        //print_err("page_pool_init malloc error!");
        return NULL;
    }

    item = (item_t *)heap_pool->buffer;

    while(i < count)
    {
#if MEMORY_CHECK
        item->magic = PAGE_MAGIC;
#endif
        item->flag = PAGE_FLAG_FREE;
        i++;
        item = (item_t *)((char *)item + heap_pool->size);
    }

    return heap_pool;
}

void delete_heap_pool(heap_pool_t *pool)
{
    if(NULL == pool)
    {
        return;
    }

    free(pool->buffer);
    free(pool);
}

