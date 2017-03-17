/** @file List.c
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
#ifdef MEMWATCH
#include "memwatch.h"
#endif


static list_node_t *list_begin(list_t *pList)
{
    return pList->head;
}

static list_node_t *list_end(list_t *pList)
{
    return pList->tail;
}

static list_node_t *list_item_at(list_t *pList, int index)
{
    list_node_t *pNode = pList->head;

    while((index > 0) && (NULL != pNode))
    {
        pNode = pNode->next;
        index--;
    }

    return pNode;
}

static void list_erase(list_t *pList, list_node_t *pNode)
{
    if(pNode->prev != NULL)
    {
        pNode->prev->next = pNode->next;
    }
    else
    {
        pList->head = pNode->next;
    }

    if(pNode->next != NULL)
    {
        pNode->next->prev = pNode->prev;
    }
    else
    {
        pList->tail = pNode->prev;
    }

    pList->heap->free_item(pList->heap, pNode);
    pList->size--;
}

static list_node_t *list_push_back(list_t *pList, void *value)
{
    list_node_t *pNode;

    pNode = pList->heap->malloc_item(pList->heap);

    if(NULL == pNode)
    {
        return NULL;
    }

    pNode->value = value;
    pList->size++;

    if(NULL == pList->head)
    {
        pNode->prev = NULL;
        pNode->next = NULL;
        pList->head = pNode;
        pList->tail = pNode;
    }
    else
    {
        pNode->prev = pList->tail;
        pNode->next = NULL;
        pList->tail->next = pNode;
        pList->tail = pNode;
    }

    return pNode;
}

static list_node_t *list_insert(list_t *pList, list_node_t *pNode, void *value)
{
    list_node_t *pNodeNew;

    if(NULL == pNode)
    {
        return list_push_back(pList, value);
    }

    pNodeNew = pList->heap->malloc_item(pList->heap);

    if(NULL == pNodeNew)
    {
        return NULL;
    }

    pList->size++;
    pNodeNew->value = value;
    pNodeNew->prev = pNode->prev;
    pNodeNew->next = pNode;
    pNode->prev = pNodeNew;

    if(pNodeNew->prev != NULL)
    {
        pNodeNew->prev->next = pNodeNew;
    }
    else
    {
        pList->head = pNodeNew;
    }

    return pNodeNew;
}

static bool list_is_empty(list_t *pList)
{
    return (pList->head == NULL);
}


list_t *create_list(int item_count)
{
    list_t *pList;

    pList = malloc(sizeof(list_t));

    if(NULL == pList)
    {
        return NULL;
    }

    pList->head = NULL;
    pList->tail = NULL;
    pList->size = 0;
    pList->begin = list_begin;
    pList->end = list_end;
    pList->erase = list_erase;
    pList->pushBack = list_push_back;
    pList->insert = list_insert;
    pList->isEmpty = list_is_empty;
    pList->itemAt = list_item_at;
    pList->heap = create_heap_pool(sizeof(list_node_t), item_count);

    if(NULL == pList->heap)
    {
        free(pList);
        return NULL;
    }

    return pList;
}

void delete_list(list_t *pList)
{
    if(NULL == pList)
    {
        return;
    }

    if(NULL != pList->head)
    {
       // print_err("list no empty!");
    }

    delete_heap_pool(pList->heap);
    free(pList);
}

