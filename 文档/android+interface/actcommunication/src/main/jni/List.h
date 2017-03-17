/** @file List.h
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
#include "HeapPool.h"

#ifndef _LIST_H_
#define _LIST_H_

#ifdef __cplusplus
extern "C" {
#endif

typedef struct list_node_s
{
    void *value;
    struct list_node_s *prev;
    struct list_node_s *next;
} list_node_t;

/* 用户负责释放pushBack和insert进来的value指向的内存 */
typedef struct list_s
{
    list_node_t *head;
    list_node_t *tail;
    heap_pool_t *heap;
    int size;

    list_node_t *(*begin)(struct list_s *pList);
    list_node_t *(*end)(struct list_s *pList);
    void (*erase)(struct list_s *pList, list_node_t *pNode);
    list_node_t *(*pushBack)(struct list_s *pList, void *value);
    list_node_t *(*insert)(struct list_s *pList, list_node_t *pNode, void *value);
    bool (*isEmpty)(struct list_s *pList);
    list_node_t *(*itemAt)(struct list_s *pList, int index);
} list_t;

list_t *create_list(int item_count);
void delete_list(list_t *pList);

#ifdef __cplusplus
}
#endif

#endif/*_LIST_H_*/
