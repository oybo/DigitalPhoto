/** @file Errors.h
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

#ifndef _ERRORS_H
#define _ERRORS_H

//#define _GNU_SOURCE
#define _LARGEFILE64_SOURCE


#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <pthread.h>
#include <string.h>
#include <errno.h>


#ifndef bool
typedef int bool;
#endif

#ifndef false
#define false 0
#endif

#ifndef true
#define true 1
#endif

#ifndef MAX
#define MAX(v0,v1) (((v0)>(v1)) ? (v0) : (v1))
#endif

#ifndef MIN
#define MIN(v0,v1) (((v0)>(v1)) ? (v1) : (v0))
#endif

#define MAX_URL_LEN    (1024)

typedef int32_t     status_t;

typedef struct InterruptCB
{
    int (*interrupt)(void *);
    void *opaque;
} InterruptCB;


/*
 * Error codes.
 * All error codes are negative values.
 */
enum
{
    OK                = 0,    // Everything's swell.
    NO_ERROR          = 0,    // No errors.

    UNKNOWN_ERROR       = 0x80000000,

    NO_MEMORY           = -ENOMEM,
    INVALID_OPERATION   = -ENOSYS,
    BAD_VALUE           = -EINVAL,
    BAD_TYPE            = 0x80000001,
    NAME_NOT_FOUND      = -ENOENT,
    PERMISSION_DENIED   = -EPERM,
    NO_INIT             = -ENODEV,
    ALREADY_EXISTS      = -EEXIST,
    DEAD_OBJECT         = -EPIPE,
    FAILED_TRANSACTION  = 0x80000002,
    JPARKS_BROKE_IT     = -EPIPE,
    BAD_INDEX           = -EOVERFLOW,
    NOT_ENOUGH_DATA     = -ENODATA,
    WOULD_BLOCK         = -EWOULDBLOCK,
    TIMED_OUT           = -ETIMEDOUT,
    UNKNOWN_TRANSACTION = -EBADMSG,
    FDS_NOT_ALLOWED     = 0x80000007
};


enum
{
    MEDIA_ERROR_BASE        = -1000,

    ERROR_ALREADY_CONNECTED = MEDIA_ERROR_BASE,
    ERROR_NOT_CONNECTED     = MEDIA_ERROR_BASE - 1,
    ERROR_UNKNOWN_HOST      = MEDIA_ERROR_BASE - 2,
    ERROR_CANNOT_CONNECT    = MEDIA_ERROR_BASE - 3,
    ERROR_IO                = MEDIA_ERROR_BASE - 4,
    ERROR_CONNECTION_LOST   = MEDIA_ERROR_BASE - 5,
    ERROR_MALFORMED         = MEDIA_ERROR_BASE - 7,
    ERROR_OUT_OF_RANGE      = MEDIA_ERROR_BASE - 8,
    ERROR_BUFFER_TOO_SMALL  = MEDIA_ERROR_BASE - 9,
    ERROR_UNSUPPORTED       = MEDIA_ERROR_BASE - 10,
    ERROR_END_OF_STREAM     = MEDIA_ERROR_BASE - 11,

    // Not technically an error.
    INFO_FORMAT_CHANGED    = MEDIA_ERROR_BASE - 12,
    INFO_DISCONTINUITY     = MEDIA_ERROR_BASE - 13,
    INFO_OUTPUT_BUFFERS_CHANGED = MEDIA_ERROR_BASE - 14,
    ERROR_NOT_INIT         = MEDIA_ERROR_BASE - 15,
    ERROR_USER_CLOSE       = MEDIA_ERROR_BASE - 16,
    ERROR_CRITICAL         = MEDIA_ERROR_BASE - 17,
    ERROR_NO_SUPPORT_RANGE = MEDIA_ERROR_BASE - 18,
    ERROR_FILE_NO_EXIST   = MEDIA_ERROR_BASE - 19,
    ERROR_NO_SUPPORT_PROTOCOL = MEDIA_ERROR_BASE - 20,
    ERROR_SERVER_REFUSE        = MEDIA_ERROR_BASE - 21,
    ERROR_ACCESS_FORBIDDEN    = MEDIA_ERROR_BASE - 22,
    ERROR_SERVER_INTERNAL_ERR  = MEDIA_ERROR_BASE - 23,
    ERROR_ACCESS_DISK_FAIL = MEDIA_ERROR_BASE - 24
};

#endif // _ERRORS_H

