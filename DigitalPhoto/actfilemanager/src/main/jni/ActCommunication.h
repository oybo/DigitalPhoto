#include <jni.h>
#include "android/log.h"
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
#include <dlfcn.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <netinet/tcp.h>

#ifndef _ACT_COMMUNICATION_H
#define _ACT_COMMUNICATION_H

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL
        Java_com_actions_actcommunication_ActCommunication_nativeSetup(JNIEnv *env, jobject instance, jobject player);
JNIEXPORT jint JNICALL
        Java_com_actions_actcommunication_ActCommunication_sendMsg(JNIEnv *env, jobject instance, jobjectArray msg);
JNIEXPORT jint JNICALL
        Java_com_actions_actcommunication_ActCommunication_connect(JNIEnv *env, jobject instance, jstring ip_);
JNIEXPORT jint JNICALL
        Java_com_actions_actcommunication_ActCommunication_disconnect(JNIEnv *env, jobject instance);
JNIEXPORT jint JNICALL
        Java_com_actions_actcommunication_ActCommunication_readSystemCfgFile(JNIEnv *env, jobject instance);
JNIEXPORT jint JNICALL
        Java_com_actions_actcommunication_ActCommunication_sendData(JNIEnv *env, jobject instance, jobjectArray msg, jbyteArray data);

////////////////////////////////////////////////////////////////////////////////////

#define AC_MAX_CMD_ITEM               (100)


typedef struct
{
   void (*onClientConnect)();
   void (*onClientDisconnect)();
   void (*onRecvMsg)(char *msg[], int count, void *data, int data_len);
}AcEventListener;

int actcom_sendMsg(int ac_handle, const char *msg[], int count, void *data, int data_len);
int actcom_register_event_listener(int ac_handle, AcEventListener *cb);
int actcom_unregister_event_listener(int ac_handle);
int actcom_connect(int ac_handle, const char *ip);
int actcom_disconnect(int ac_handle);
int actcom_open(void);
void actcom_close(int ac_handle);


#ifdef __cplusplus
}
#endif

#endif  //ENDIF _ACT_COMMUNICATION_H
