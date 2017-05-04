#include "ActCommunication.h"

#define LOG_TAG    "ActCommunication"

static jobject cpObject = NULL;
static JavaVM*   gJavaVM = NULL;
static jclass clazz = NULL;
static jmethodID onRecvMsgMethodID = NULL;
static jmethodID onRecvDataMethodID = NULL;
static jmethodID onStatusChangeMethodID = NULL;
static const char* const classPathName = "com/actions/actcommunication/ActCommunication";
static int32_t asHandle = 0;
static AcEventListener asCb;

static JNINativeMethod gMethods[] = {
    {"nativeSetup",      "(Ljava/lang/Object;)V",        (void *)Java_com_actions_actcommunication_ActCommunication_nativeSetup},
    {"sendMsg",       "([Ljava/lang/Object;)I",        (void *)Java_com_actions_actcommunication_ActCommunication_sendMsg},
    {"sendData",       "([Ljava/lang/Object;[B)I",        (void *)Java_com_actions_actcommunication_ActCommunication_sendData},
    {"connect",          "(Ljava/lang/String;)I",    (void *)Java_com_actions_actcommunication_ActCommunication_connect},
    {"disconnect",       "()I",    (void *)Java_com_actions_actcommunication_ActCommunication_disconnect},
    {"readSystemCfgFile",       "()I",    (void *)Java_com_actions_actcommunication_ActCommunication_readSystemCfgFile},
};

/*  define the minimum version
 *     initialization
 *  register native methods.
 * */
jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "JNI version error!");
        return result;
    }

    if (env == NULL)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "JNI get env fail!");
        return result;
    }

    //gJavaVM
    env->GetJavaVM(&gJavaVM);
    if (env->ExceptionOccurred())
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "JNI-----GetJavaVM fail.");
        return result;
    }

    //get class ID.
    jclass tmp = env->FindClass(classPathName);
    if (tmp == NULL)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "JNI Cannot find %s!", classPathName);
        return result;
    }
    clazz = (jclass)env->NewGlobalRef(tmp);

    //get MethodID
    onRecvMsgMethodID = env->GetStaticMethodID(clazz, "onRecvMsg", "(Ljava/lang/Object;[Ljava/lang/String;)V");
    onRecvDataMethodID = env->GetStaticMethodID(clazz, "onRecvData", "(Ljava/lang/Object;[Ljava/lang/String;[B)V");
    onStatusChangeMethodID  = env->GetStaticMethodID(clazz, "onStatusChange", "(Ljava/lang/Object;Ljava/lang/String;)V");
    
    //if ExceptionOccurred
    if (env->ExceptionOccurred())
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "JNI-----GetStaticMethodID fail.");
        return result;
    }

    //register methods.
    if (env->RegisterNatives(clazz, gMethods, sizeof(gMethods)/sizeof(gMethods[0])) < 0)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "JNI-----RegisterNatives fail.");
        return result;
    }

    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "JNI_OnLoad-----sucess, msg=%p, data=%p", onRecvMsgMethodID, onRecvDataMethodID);

    return JNI_VERSION_1_4;
}

/* UnregisterNatives  */
void JNI_OnUnload(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK)
    {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "JNI UnregisterNatives: fail");
        return;
    }
    else
    {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "JNI_OnUnload Unregister!");
        env-> UnregisterNatives(clazz);
        return;
    }
    //NPT_String::PrintAllocated();
    //NPT_LOG_INFO_1("NPT_String buffer %d", NPT_String::g_AllocatedBytes);
}

static void actcom_onRecvMsg(char *msg[], int count, void *data, int data_len)
{	
    JNIEnv *myEnv = NULL;
    
	if(cpObject== NULL) 
	{
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "notify param object==NULL! notify fail!");
		return;
	}
	
	//Get myEnv
	gJavaVM->AttachCurrentThread(&myEnv, NULL);	
    if(myEnv == NULL)
        return;

    if((clazz == NULL) || (onRecvMsgMethodID == NULL) || (onRecvDataMethodID == NULL))
    {
        gJavaVM->DetachCurrentThread();
        return;
    }

    jobjectArray swArray = 0;
    jclass refClass = myEnv->FindClass("java/lang/String");
    jstring refString = myEnv->NewStringUTF("");
    swArray = myEnv->NewObjectArray(count, refClass, refString);
    myEnv->DeleteLocalRef(refClass);
    myEnv->DeleteLocalRef(refString);
    
    int i = 0;
    while(i < count)
    {
        jstring tmp = myEnv->NewStringUTF(msg[i]);
        myEnv->SetObjectArrayElement(swArray, i, tmp);
        myEnv->DeleteLocalRef(tmp);
		i++;
	}

    if(data_len > 0)
    {
        jbyte *by = (jbyte*)data;
        jbyteArray jarray = myEnv->NewByteArray(data_len);
        myEnv->SetByteArrayRegion(jarray, 0, data_len, by);
        myEnv->CallStaticVoidMethod(clazz, onRecvDataMethodID, cpObject, swArray, jarray);
        myEnv->DeleteLocalRef(jarray);
    }
    else
    {
        myEnv->CallStaticVoidMethod(clazz, onRecvMsgMethodID, cpObject, swArray);
    }
    
    myEnv->DeleteLocalRef(swArray);
	if (myEnv->ExceptionOccurred())
	{
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "notify --CallStaticVoidMethod fail!");
	}

	gJavaVM->DetachCurrentThread();
}

static void actcom_onConnect()
{	
    JNIEnv *myEnv = NULL;
    
	if(cpObject== NULL) 
	{
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "notify param object==NULL! notify fail!");
		return;
	}
	
	//Get myEnv
	gJavaVM->AttachCurrentThread(&myEnv, NULL);	
    if(myEnv == NULL)
        return;

    if((clazz == NULL) || (onStatusChangeMethodID == NULL))
    {
        gJavaVM->DetachCurrentThread();
        return;
    }

    jstring type = myEnv->NewStringUTF("connect");
    myEnv->CallStaticVoidMethod(clazz, onStatusChangeMethodID, cpObject, type);
    myEnv->DeleteLocalRef(type);
    
	if (myEnv->ExceptionOccurred())
	{
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "notify --CallStaticVoidMethod fail!");
	}

	gJavaVM->DetachCurrentThread();
}

static void actcom_onDisconnect()
{	
    JNIEnv *myEnv = NULL;
    
	if(cpObject== NULL) 
	{
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "notify param object==NULL! notify fail!");
		return;
	}
	
	//Get myEnv
	gJavaVM->AttachCurrentThread(&myEnv, NULL);	
    if(myEnv == NULL)
        return;

    if((clazz == NULL) || (onStatusChangeMethodID == NULL))
    {
        gJavaVM->DetachCurrentThread();
        return;
    }

    jstring type = myEnv->NewStringUTF("disconnect");
    myEnv->CallStaticVoidMethod(clazz, onStatusChangeMethodID, cpObject, type);
    myEnv->DeleteLocalRef(type);
    
	if (myEnv->ExceptionOccurred())
	{
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "notify --CallStaticVoidMethod fail!");
	}

	gJavaVM->DetachCurrentThread();
}


JNIEXPORT void JNICALL
Java_com_actions_actcommunication_ActCommunication_nativeSetup(JNIEnv *env, jobject instance, jobject player) {

    // TODO
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "func=%s", __FUNCTION__);
    cpObject = env->NewGlobalRef(player);
}

JNIEXPORT jint JNICALL
Java_com_actions_actcommunication_ActCommunication_sendMsg(JNIEnv *env, jobject instance, jobjectArray msg) {

    int queuesize = env->GetArrayLength(msg);
    int i;
    jint ret = -1;
    const char *item[AC_MAX_CMD_ITEM];
    jstring itemObj[AC_MAX_CMD_ITEM];

    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "func=%s, line=%d, len=%d", __FUNCTION__, __LINE__, queuesize);
    if((queuesize > AC_MAX_CMD_ITEM) || (asHandle == 0))
        return -1;
    
    for(i=0; i<queuesize; i++){
        itemObj[i] = (jstring)env->GetObjectArrayElement(msg, i);
        item[i] = env->GetStringUTFChars(itemObj[i], 0);
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "msg=%s", item[i]);
    }
    
    actcom_sendMsg(asHandle, item, queuesize, NULL, 0);
    for(i=0; i<queuesize; i++){
        env->ReleaseStringUTFChars(itemObj[i], item[i]);
        env->DeleteLocalRef(itemObj[i]);
    }

    return ret;
}

JNIEXPORT jint JNICALL
Java_com_actions_actcommunication_ActCommunication_sendData(JNIEnv *env, jobject instance, jobjectArray msg, jbyteArray data) {

    int queuesize = env->GetArrayLength(msg);
    int i;
    jint ret = -1;
    const char *item[AC_MAX_CMD_ITEM];
    jstring itemObj[AC_MAX_CMD_ITEM];

    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "func=%s, line=%d, len=%d", __FUNCTION__, __LINE__, queuesize);
    if((queuesize > AC_MAX_CMD_ITEM) || (asHandle == 0))
        return -1;
    
    for(i=0; i<queuesize; i++){
        itemObj[i] = (jstring)env->GetObjectArrayElement(msg, i);
        item[i] = env->GetStringUTFChars(itemObj[i], 0);
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "msg=%s", item[i]);
    }

    int byteLen = env->GetArrayLength(data);
    char *dataPtr = (char*)env->GetByteArrayElements(data, NULL);
    //__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "send data len=%d, %c%c", byteLen, dataPtr[0], dataPtr[1]);

    actcom_sendMsg(asHandle, item, queuesize, (void*)dataPtr, byteLen);
    
    env->ReleaseByteArrayElements(data, (jbyte*)dataPtr, 0);
    for(i=0; i<queuesize; i++){
        env->ReleaseStringUTFChars(itemObj[i], item[i]);
        env->DeleteLocalRef(itemObj[i]);
    }
    
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_actions_actcommunication_ActCommunication_connect(JNIEnv *env, jobject instance, jstring ip_) {
    const char *ip = env->GetStringUTFChars(ip_, 0);

    // TODO
    __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "connect ip=%s", ip);
    if(asHandle != 0)
    {
        actcom_unregister_event_listener(asHandle);
        actcom_close(asHandle);
        asHandle = 0;
    }

    asCb.onRecvMsg = actcom_onRecvMsg;
    asCb.onClientConnect = actcom_onConnect;
    asCb.onClientDisconnect = actcom_onDisconnect;

    asHandle = actcom_open();
    actcom_register_event_listener(asHandle, &asCb);
    actcom_connect(asHandle, ip);
    
    env->ReleaseStringUTFChars(ip_, ip);

    return 0;
}

JNIEXPORT jint JNICALL
Java_com_actions_actcommunication_ActCommunication_disconnect(JNIEnv *env, jobject instance) {

    if(asHandle != 0)
    {
        actcom_unregister_event_listener(asHandle);
        actcom_close(asHandle);
        asHandle = 0;
    }
    
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_actions_actcommunication_ActCommunication_readSystemCfgFile(JNIEnv *env, jobject instance) {


    return 100;
}
