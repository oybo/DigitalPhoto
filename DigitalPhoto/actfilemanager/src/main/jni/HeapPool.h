/** @file HeapPool.h
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

#include "Errors.h"

#ifndef _HEAP_POOL_H_
#define _HEAP_POOL_H_

#ifdef __cplusplus
extern "C" {
#endif

#define MEMORY_CHECK     0

typedef struct heap_pool_s
{
    void *(*malloc_item)(struct heap_pool_s *pool);
    void (*free_item)(struct heap_pool_s *pool, void *data);
    int (*heap_need_malloc)(struct heap_pool_s *pool);
    int (*free_size)(struct heap_pool_s *pool);

    char *buffer;
    int count;
    int freeCount;
    int size;
    int next_index;
#if MEMORY_CHECK
    int need_malloc;
#endif
} heap_pool_t;

heap_pool_t *create_heap_pool(int size, int count);
void delete_heap_pool(heap_pool_t *pool);

#ifdef __cplusplus
}
#endif

#endif/*_HEAP_POOL_H_*/

