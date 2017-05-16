#include <jni.h>
#include <unistd.h>

#include "actFileManagerJNI.h"
//#include "common.h"

static jobject cpObject = NULL;
static JavaVM *gJavaVM = NULL;
static jclass clazz = NULL;
static jclass fileInfoClazz = NULL;
static jmethodID postOperationProgressID = NULL;
static jmethodID postUploadResponseID = NULL;
static jmethodID postDeleteResponseID = NULL;
static jmethodID postDeleteDirectoryResponseID = NULL;
static jmethodID postCreateDirectoryResponseID = NULL;
static jmethodID postBrowseResponseID = NULL;
static jmethodID postDownloadResponseID = NULL;
static jmethodID postDisconnectResponseID = NULL;
static jmethodID postRenameResponseID = NULL;


static int fileTransferInited = 0;
static int infoTransferInited = 0;

static const char *const classPathName = "com/actions/actfilemanager/ActFileManager";
static const char *const classFileInfoPath = "com/actions/actfilemanager/ActFileInfo";

extern "C" {
JNIEXPORT void JNICALL Java_com_actions_actfilemanager_nativeSetup(JNIEnv *env, jobject instance,
                                                                   jobject player);
JNIEXPORT void JNICALL Java_com_actions_actfilemanager_nativeClose(JNIEnv *env, jobject instance);
JNIEXPORT jint JNICALL Java_com_actions_actfilemanager_connect(JNIEnv *env, jobject instance,
                                                               jstring ip);
JNIEXPORT jint JNICALL Java_com_actions_actfilemanager_disconnect(JNIEnv *env, jobject instance);
JNIEXPORT jint JNICALL Java_com_actions_actfilemanager_createDirectory(JNIEnv *env, jobject instance,
                                                                   jstring remotepath);
JNIEXPORT jint JNICALL Java_com_actions_actfilemanager_uploadFile(JNIEnv *env, jobject instance,jstring filepath,
                                                                   jstring path);
JNIEXPORT jint JNICALL Java_com_actions_actfilemanager_deleteDirectory(JNIEnv *env, jobject instance,
                                                                   jstring dirpath);
JNIEXPORT jint JNICALL Java_com_actions_actfilemanager_deleteFile(JNIEnv *env, jobject instance,
                                                                  jstring url);
JNIEXPORT jint JNICALL Java_com_actions_actfilemanager_browseFiles(JNIEnv *env, jobject instance,
                                                                   jstring dirPath);
JNIEXPORT jint JNICALL Java_com_actions_actfilemanager_downloadFile(JNIEnv *env, jobject instance,
                                                                    jstring url, jstring path);
JNIEXPORT jint JNICALL
Java_com_actions_actfilemanager_rename(JNIEnv *env, jobject instance, jstring from,
                                             jstring to);
}

static JNINativeMethod gMethods[] = {
        {"nativeSetup",  "(Ljava/lang/Object;)V",                   (void *) Java_com_actions_actfilemanager_nativeSetup},
        {"nativeClose",  "()V",                                     (void *) Java_com_actions_actfilemanager_nativeClose},
        {"connect",      "(Ljava/lang/String;)I",                   (void *) Java_com_actions_actfilemanager_connect},
        {"disconnect",   "()I",                                     (void *) Java_com_actions_actfilemanager_disconnect},
        {"createDirectory",  "(Ljava/lang/String;)I",               (void *) Java_com_actions_actfilemanager_createDirectory},
        {"uploadFile",   "(Ljava/lang/String;Ljava/lang/String;)I", (void *) Java_com_actions_actfilemanager_uploadFile},
        {"deleteDirectory",  "(Ljava/lang/String;)I",               (void *) Java_com_actions_actfilemanager_deleteDirectory},
        {"deleteFile",   "(Ljava/lang/String;)I",                   (void *) Java_com_actions_actfilemanager_deleteFile},
        {"browseFiles",  "(Ljava/lang/String;)I",                   (void *) Java_com_actions_actfilemanager_browseFiles},
        {"downloadFile", "(Ljava/lang/String;Ljava/lang/String;)I", (void *) Java_com_actions_actfilemanager_downloadFile},
		{"rename", "(Ljava/lang/String;Ljava/lang/String;)I", (void *) Java_com_actions_actfilemanager_rename},
};

/*  define the minimum version
 *     initialization
 *  register native methods.
 * */
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    jint result = -1;

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "JNI version error!");
        return result;
    }

    if (env == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "JNI get env fail!");
        return result;
    }

    //gJavaVM
    env->GetJavaVM(&gJavaVM);
    if (env->ExceptionOccurred()) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "JNI-----GetJavaVM fail.");
        return result;
    }

    //get class ID.
    jclass tmp = env->FindClass(classPathName);
    if (tmp == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "JNI Cannot find %s!", classPathName);
        return result;
    }
    clazz = (jclass) env->NewGlobalRef(tmp);

    tmp = env->FindClass(classFileInfoPath);
    if (tmp == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "JNI Cannot find %s!", classFileInfoPath);
        return result;
    }
    fileInfoClazz = (jclass) env->NewGlobalRef(tmp);

    //get MethodID
    postOperationProgressID = env->GetMethodID(clazz, "postOperationProgress",
                                               "(Ljava/lang/Object;III)V");
    postUploadResponseID = env->GetMethodID(clazz, "postUploadResponse",
                                            "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;I)V");
    postDeleteResponseID = env->GetMethodID(clazz, "postDeleteResponse",
                                            "(Ljava/lang/Object;Ljava/lang/String;I)V");
	postDeleteDirectoryResponseID = env->GetMethodID(clazz, "postDeleteDirectoryResponse",
                                            "(Ljava/lang/Object;Ljava/lang/String;I)V");
	postCreateDirectoryResponseID = env->GetMethodID(clazz, "postCreateDirectoryResponse",
                                            "(Ljava/lang/Object;Ljava/lang/String;I)V");
    postBrowseResponseID = env->GetMethodID(clazz, "postBrowseResponse",
                                            "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;I)V");
    postDownloadResponseID = env->GetMethodID(clazz, "postDownloadResponse",
                                              "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;I)V");
	postDisconnectResponseID = env->GetMethodID(clazz, "postDisconnectResponse",
		                                        "(Ljava/lang/Object;I)V");
	postRenameResponseID = env->GetMethodID(clazz, "postRenameResponse",
		                                        "(Ljava/lang/Object;I)V");

    //if ExceptionOccurred
    if (env->ExceptionOccurred()) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "JNI-----GetStaticMethodID fail.");
        return result;
    }

    //register methods.
    if (env->RegisterNatives(clazz, gMethods, sizeof(gMethods) / sizeof(gMethods[0])) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "JNI-----RegisterNatives fail.");
        return result;
    }

    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "JNI_OnLoad-----sucess!");

    return JNI_VERSION_1_4;
}

/* UnregisterNatives  */
void JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "JNI UnregisterNatives: fail");
        return;
    }
    else {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "JNI_OnUnload Unregister!");
        env->UnregisterNatives(clazz);
        return;
    }

}

void postOperationProgress(int opcode, int processed,int total) {
    if (cpObject == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "notify param object==NULL! notify fail!");
        return;
    }

    JNIEnv *env = NULL;
    gJavaVM->AttachCurrentThread(&env, NULL);
    if (env == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "get env failed");
        return;
    }

    env->CallVoidMethod(cpObject, postOperationProgressID, cpObject, opcode, processed,total);
    if (env->ExceptionOccurred()) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "notify --CallVoidMethod fail!");
    }

    gJavaVM->DetachCurrentThread();
    env = NULL;
}

void postUploadResponse(const char *remote_path, const char *file_path,int result) {
    if (cpObject == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "notify param object==NULL! notify fail!");
        return;
    }

    JNIEnv *env = NULL;
    gJavaVM->AttachCurrentThread(&env, NULL);
    if (env == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "get env failed");
        return;
    }

    jstring jremote_path = env->NewStringUTF(remote_path);
	jstring jfile_path = env->NewStringUTF(file_path);
    env->CallVoidMethod(cpObject, postUploadResponseID, cpObject, jremote_path,jfile_path, result);
    if (env->ExceptionOccurred()) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "notify --CallVoidMethod fail!");
    }

    env->DeleteLocalRef(jremote_path);
	env->DeleteLocalRef(jfile_path);
    gJavaVM->DetachCurrentThread();
    env = NULL;
}

void postDeleteResponse(const char *url, int result) {
    if (cpObject == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "notify param object==NULL! notify fail!");
        return;
    }

    JNIEnv *env = NULL;
    gJavaVM->AttachCurrentThread(&env, NULL);
    if (env == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "get env failed");
        return;
    }

    jstring jurl = env->NewStringUTF(url);
    env->CallVoidMethod(cpObject, postDeleteResponseID, cpObject, jurl, result);
    if (env->ExceptionOccurred()) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "notify --CallVoidMethod fail!");
    }

    env->DeleteLocalRef(jurl);
    gJavaVM->DetachCurrentThread();
    env = NULL;
}

void postDeleteDirectoryResponse(const char *dir_path,int result)
{
    if (cpObject == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "notify param object==NULL! notify fail!");
        return;
    }

	JNIEnv *env = NULL;
    gJavaVM->AttachCurrentThread(&env, NULL);
    if (env == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "get env failed");
        return;
    }

    jstring jurl = env->NewStringUTF(dir_path);
    env->CallVoidMethod(cpObject, postDeleteDirectoryResponseID, cpObject, jurl, result);
    if (env->ExceptionOccurred()) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "notify --CallVoidMethod fail!");
    }

    env->DeleteLocalRef(jurl);
    gJavaVM->DetachCurrentThread();
    env = NULL;
	
}

void postCreateDirectoryResponse(const char *parent_path,int result)
{
    if (cpObject == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "notify param object==NULL! notify fail!");
        return;
    }

	JNIEnv *env = NULL;
    gJavaVM->AttachCurrentThread(&env, NULL);
    if (env == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "get env failed");
        return;
    }

    jstring jurl = env->NewStringUTF(parent_path);
    env->CallVoidMethod(cpObject, postCreateDirectoryResponseID, cpObject, jurl, result);
    if (env->ExceptionOccurred()) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "notify --CallVoidMethod fail!");
    }

    env->DeleteLocalRef(jurl);
    gJavaVM->DetachCurrentThread();
    env = NULL;
	
}


void postDisconnectResponse(int result)
{
    if (cpObject == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "notify param object==NULL! notify fail!");
        return;
    }

	JNIEnv *env = NULL;
    gJavaVM->AttachCurrentThread(&env, NULL);
    if (env == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "get env failed");
        return;
    }

    env->CallVoidMethod(cpObject, postDisconnectResponseID, cpObject, result);
    if (env->ExceptionOccurred()) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "notify --CallVoidMethod fail!");
    }

    gJavaVM->DetachCurrentThread();
    env = NULL;
	
}


void postBrowseResponse(FileInfo **filelist, int fileCount, int result, const char *current_path) {
    int i;
	FileInfo *mFileList; 

    if (cpObject == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "notify param object==NULL! notify fail!");
        return;
    }

    JNIEnv *env = NULL;
    gJavaVM->AttachCurrentThread(&env, NULL);
    if (env == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "get env failed");
        return;
    }

	jstring jcurrentpath = env->NewStringUTF(current_path);
	if(jcurrentpath == NULL)
		goto browse_finish;

    if (result != 0) {
        goto bad_apple;
    }

	mFileList = *filelist;

    if (env && clazz && fileInfoClazz && postBrowseResponseID) {
        jclass listClazz = env->FindClass("java/util/ArrayList");
        if (listClazz == NULL) {
            __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "faild to find array list");
            goto bad_apple;
        }

        jmethodID listConstructor = env->GetMethodID(listClazz, "<init>", "()V");
        if (listConstructor == NULL) {
            __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "faild to find TrackInfo init");
            goto bad_apple;
        }
        jobject listObj = env->NewObject(listClazz, listConstructor);
        if (listObj == NULL) {
            __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "faild to find list constructor");
            goto bad_apple;
        }
        jmethodID addMethod = env->GetMethodID(listClazz, "add",
                                               "(Ljava/lang/Object;)Z");
        if (addMethod == NULL) {
            __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "faild to find list add method");
            goto bad_apple;
        }

        jmethodID infoConstructor = env->GetMethodID(fileInfoClazz, "<init>", "()V");
        if (infoConstructor == NULL) {
            __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "faild to find TrackInfo constructor");
            goto bad_apple;
        }

        jfieldID mName = env->GetFieldID(fileInfoClazz, "mFileName",
                                         "Ljava/lang/String;");
        jfieldID mType = env->GetFieldID(fileInfoClazz, "mFileType",
                                         "I");
		jfieldID mTime = env->GetFieldID(fileInfoClazz, "mModifyTime",
                                         "Ljava/lang/String;");
        jfieldID mSize = env->GetFieldID(fileInfoClazz, "mFileSize",
                                         "I");

        for (i = 0; i < fileCount; i++) {
            FileInfo *file = mFileList + i;
			//__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "file name %s file type %d",file->mName,file->mType);
            if ((&file) == NULL) {
                continue;
            }

            jobject infoObj = env->NewObject(fileInfoClazz, infoConstructor);
            if (infoObj == NULL) {
                __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,
                                    "Could not create a CaseInfo object");
                goto bad_apple;
            }

            jstring strname = env->NewStringUTF(file->mName);
            if (!strname)
                strname = env->NewStringUTF(" ");
			env->SetObjectField(infoObj, mName, strname);
            env->DeleteLocalRef(strname);
			
			jstring strtime = env->NewStringUTF(file->mDate);
            if (!strtime)
                strtime = env->NewStringUTF(" ");
			env->SetObjectField(infoObj, mTime, strtime);
            env->DeleteLocalRef(strtime);
			
            env->SetIntField(infoObj, mType, file->mType);
			env->SetIntField(infoObj, mSize, (int)file->mSize);

            env->CallBooleanMethod(listObj, addMethod, infoObj);
            env->DeleteLocalRef(infoObj);
        }
        env->DeleteLocalRef(listClazz);

        //return listObj;
        env->CallVoidMethod(cpObject, postBrowseResponseID, cpObject, listObj, jcurrentpath, result);
        if (env->ExceptionOccurred()) {
            __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "notify --CallStaticVoidMethod fail!");
            gJavaVM->DetachCurrentThread();
            env = NULL;
        }
		goto browse_finish;
    }

    bad_apple:
    env->CallVoidMethod(cpObject, postBrowseResponseID, cpObject, NULL,jcurrentpath, -1);
    if (env->ExceptionOccurred()) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "notify --CallVoidMethod fail!");
		env->DeleteLocalRef(jcurrentpath);
        gJavaVM->DetachCurrentThread();
        env = NULL;
    }

	browse_finish:
    env->DeleteLocalRef(jcurrentpath);
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "postBrowseResponse finish");
    gJavaVM->DetachCurrentThread();
    env = NULL;
}

void postDownloadResponse(const char *url, const char *path, int result) {
    if (cpObject == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "notify param object==NULL! notify fail!");
        return;
    }

    JNIEnv *env = NULL;
    gJavaVM->AttachCurrentThread(&env, NULL);
    if (env == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "get env failed");
        return;
    }
	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "postDownloadResponse url %s path %s result %d",url,path,result);

    jstring jurl = env->NewStringUTF(url);
    jstring jpath = env->NewStringUTF(path);
    env->CallVoidMethod(cpObject, postDownloadResponseID, cpObject, jurl, jpath, result);
    if (env->ExceptionOccurred()) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "notify --CallVoidMethod fail!");
    }

    env->DeleteLocalRef(jurl);
    env->DeleteLocalRef(jpath);
    gJavaVM->DetachCurrentThread();
    env = NULL;
	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "postDownloadResponse return");
}

void postRenameResponse(int result) {
    if (cpObject == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "notify param object==NULL! notify fail!");
        return;
    }

    JNIEnv *env = NULL;
    gJavaVM->AttachCurrentThread(&env, NULL);
    if (env == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "get env failed");
        return;
    }
	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "postRenameResponse result %d", result);

    env->CallVoidMethod(cpObject, postRenameResponseID, cpObject, result);
    if (env->ExceptionOccurred()) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "notify --CallVoidMethod fail!");
    }

    gJavaVM->DetachCurrentThread();
    env = NULL;
	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "postDownloadResponse return");
}


JNIEXPORT void JNICALL
Java_com_actions_actfilemanager_nativeSetup(JNIEnv *env, jobject instance, jobject player) {
    // TODO
    //__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "func=%s", __FUNCTION__);
    cpObject = env->NewGlobalRef(player);
}

JNIEXPORT void JNICALL
Java_com_actions_actfilemanager_nativeClose(JNIEnv *env, jobject instance) {

    // TODO
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "func=%s", __FUNCTION__);

}

JNIEXPORT jint JNICALL
Java_com_actions_actfilemanager_connect(JNIEnv *env, jobject instance, jstring ip) {
    int ret = 0;
    const char *strip = env->GetStringUTFChars(ip, 0);//	转换为本地类型
    if (strip == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "connect ip is NULL!");
        env->ReleaseStringUTFChars(ip, strip);
        return -1;
    }

    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "connect ip is %s!", strip);

    ret = fileTransferInit(strip);
    if (ret < 0) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "something wrong when file transfer init");
        env->ReleaseStringUTFChars(ip, strip);
        return -1;
    }
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "fileTransferInit is successfully!");

    ret = infoTransferInit(strip);
    if (ret < 0) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "something wrong when info transfer init");
        env->ReleaseStringUTFChars(ip, strip);
        return -1;
    }

    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "infoTransferInit is successfully!");

    return ret;
}

JNIEXPORT jint JNICALL
Java_com_actions_actfilemanager_disconnect(JNIEnv *env, jobject instance) {
    int ret = 0;

	ret = infoTransferQuitFromRemote();
	ret = fileTransferCleanup();
	ret = infoTransferCleanup();
    return ret;
}


JNIEXPORT jint JNICALL
Java_com_actions_actfilemanager_createDirectory(JNIEnv *env, jobject instance,
                                            jstring remotepath) {
    int ret = 0;
	const char *dir_path = env->GetStringUTFChars(remotepath, 0);
	if(dir_path == NULL)
    {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "remote_path is NULL!");
		env->ReleaseStringUTFChars(remotepath, dir_path);
    }
	ret = infoTransferCreateDirectory(dir_path);
    env->ReleaseStringUTFChars(remotepath, dir_path);
	
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_actions_actfilemanager_uploadFile(JNIEnv *env, jobject instance,jstring filepath,
                                           jstring path) {
    int ret = 0;

	const char *file_path = env->GetStringUTFChars(filepath, 0);

    if(file_path == NULL)
    {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "file_path is NULL!");
		env->ReleaseStringUTFChars(filepath, file_path);
		return -1;
    }	
	
    const char *remote_path = env->GetStringUTFChars(path, 0);
	
    if (remote_path == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "path is NULL!");
		env->ReleaseStringUTFChars(filepath, file_path);
        env->ReleaseStringUTFChars(path, remote_path);
        return -1;
    }

    ret = fileTransferUpload(file_path,remote_path);

	env->ReleaseStringUTFChars(filepath, file_path);
    env->ReleaseStringUTFChars(path, remote_path);
	
    return ret;
}


JNIEXPORT jint JNICALL
Java_com_actions_actfilemanager_deleteDirectory(JNIEnv *env, jobject instance, jstring dirpath) {
    int ret = 0;

	const char *dir_path = env->GetStringUTFChars(dirpath, 0);
	if(dir_path == NULL)
    {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "remote_path is NULL!");
		env->ReleaseStringUTFChars(dirpath, dir_path);
    }
	ret = infoTransferDeleteDirectory(dir_path);
    env->ReleaseStringUTFChars(dirpath, dir_path);
	
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_actions_actfilemanager_deleteFile(JNIEnv *env, jobject instance, jstring url) {
    int ret = 0;
    const char *strurl = env->GetStringUTFChars(url, 0);//	转换为本地类型
    if (strurl == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "path is NULL!");
        env->ReleaseStringUTFChars(url, strurl);
        return -1;
    }
    ret = infoTransferDelete(strurl);
    env->ReleaseStringUTFChars(url, strurl);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_actions_actfilemanager_browseFiles(JNIEnv *env, jobject instance, jstring dirPath) {
    int ret = 0;

	const char *dirpath = env->GetStringUTFChars(dirPath,0);
	if(dirpath == NULL)
	{
	    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "dirpath is NULL!");
		env->ReleaseStringUTFChars(dirPath, dirpath);
		return -1;
	}	

	ret = infoTransferListInfo(dirpath);
	env->ReleaseStringUTFChars(dirPath, dirpath);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_actions_actfilemanager_downloadFile(JNIEnv *env, jobject instance, jstring url,
                                             jstring path) {
    int ret = 0;

    const char *strpath = env->GetStringUTFChars(path, 0);//	转换为本地类型
    if (strpath == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "path is NULL!");
        env->ReleaseStringUTFChars(path, strpath);
        return -1;
    }

    const char *strurl = env->GetStringUTFChars(url, 0);//	转换为本地类型
    if (strurl == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "path is NULL!");
		env->ReleaseStringUTFChars(path, strpath);
        env->ReleaseStringUTFChars(url, strurl);
        return -1;
    }
	//__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "download file %s to %s",strurl,strpath);
    ret = fileTransferDownload(strurl, strpath);

    env->ReleaseStringUTFChars(path, strpath);
    env->ReleaseStringUTFChars(url, strurl);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_actions_actfilemanager_rename(JNIEnv *env, jobject instance, jstring from,
                                             jstring to) {
    int ret = 0;

    const char *strfrom = env->GetStringUTFChars(from, 0);
    if (strfrom == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "path is NULL!");
        return -1;
    }

    const char *strto = env->GetStringUTFChars(to, 0);
    if (strto == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "path is NULL!");
		env->ReleaseStringUTFChars(from, strfrom);
        return -1;
    }
	
	//__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "rename file %s to %s",strfrom,strto);
    ret = infoTransferRename(strfrom, strto);

    env->ReleaseStringUTFChars(from, strfrom);
    env->ReleaseStringUTFChars(to, strto);
    return ret;
}


