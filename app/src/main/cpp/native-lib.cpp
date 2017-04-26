#include <jni.h>
#include <time.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdlib.h>
#include <stdio.h>

#include "gif_lib.h"
#define  LOG_TAG    "dongnao"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  argb(a,r,g,b) ( ((a) & 0xff) << 24 ) | ( ((b) & 0xff) << 16 ) | ( ((g) & 0xff) << 8 ) | ((r) & 0xff)
#define  delay(ext) (10*((ext)->Bytes[2] << 8 | (ext)->Bytes[1]))
#define  dispose(ext) (((ext)->Bytes[0] & 0x1c) >> 2)
#define  transparency(ext) ((ext)->Bytes[0] & 1)
#define  trans_index(ext) ((ext)->Bytes[3])
#define  ZERO_DELAY 80
typedef struct GifAnimInfo {
    int total_duration;
    int current_frame;
    int frame_duration;
    int total_frame;

} GifAnimInfo;
char* jstringTostring(JNIEnv* env, jstring jstr)
{
    char* rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("utf-8");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr= (jbyteArray)env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0)
    {
        rtn = (char*)malloc(alen + 1);

        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}
 int getFrame(GifFileType* gif_handle, int level) {
    unsigned int i,j,k,ms;
    ExtensionBlock * ext = 0;
    SavedImage * frame;
    if (((GifAnimInfo *)gif_handle->UserData)->total_duration == 0) {
        return 0;
    }
    ms = (level * 10) % ((GifAnimInfo *)gif_handle->UserData)->total_duration;
    for (i=0,k=0; i<gif_handle->ImageCount; i++) {
        frame = &(gif_handle->SavedImages[i]);
        for (j=0; j<frame->ExtensionBlockCount; j++) {
            if (frame->ExtensionBlocks[j].Function == GRAPHICS_EXT_FUNC_CODE) {
                ext = &(frame->ExtensionBlocks[j]);
                break;
            }
        }

        if (ext == 0) {
            return 0;
        }
        k += delay(ext) == 0 ? ZERO_DELAY : delay(ext);
        if (ms <= k) {
            return i;
        }
    }
    return -1;
}
extern "C" {

JNIEXPORT jint JNICALL
Java_com_dongnao_gifplayer_GifHandler_getWidth(JNIEnv *env, jclass type, jlong gif) {

    GifFileType* gif_handle = (GifFileType *)gif;
    LOGE("gif  宽度  %d ",gif_handle->SWidth);
    LOGE("gif  地址  %d ", gif);


    return gif_handle->SWidth;
}

JNIEXPORT jint JNICALL
Java_com_dongnao_gifplayer_GifHandler_getDuration(JNIEnv *env, jclass type, jlong gif) {
    GifFileType* gif_handle = (GifFileType *)gif;
    GifAnimInfo* gifAnimInfo= (GifAnimInfo *) gif_handle->UserData;
    return gifAnimInfo->frame_duration;
}

JNIEXPORT jint JNICALL
Java_com_dongnao_gifplayer_GifHandler_getHeight(JNIEnv *env, jclass type, jlong gif) {
    GifFileType* gif_handle = (GifFileType *)gif;
    LOGE("gif  高度  %d ",gif_handle->SHeight);
    return gif_handle->SHeight;
}




JNIEXPORT jlong JNICALL
Java_com_dongnao_gifplayer_GifHandler_loadGif(JNIEnv *env, jclass type, jstring path_) {
    const char *path = env->GetStringUTFChars(path_, 0);
    SavedImage * frame = 0;
    GifImageDesc * frameInfo = 0;
    ExtensionBlock * ext = 0;
    int frame_delay,error,i,j;

    char* filePath=jstringTostring(env,path_);
    //打开gif文件
    GifFileType* gif=DGifOpenFileName(filePath,&error);
    //初始化gif结构体
    DGifSlurp(gif);
    env->ReleaseStringUTFChars(path_,filePath);
    GifAnimInfo * gif_info = (GifAnimInfo *) malloc(sizeof(GifAnimInfo));
    //设置用户数据
    gif->UserData=(void *)gif_info;
    gif_info->current_frame=0;
    gif_info->frame_duration=0;
    gif_info->total_duration=0;
    LOGE("gif image count  %d", gif->ImageCount);
    for (i=0; i<gif->ImageCount; i++) {
        frame = &(gif->SavedImages[i]);
        frameInfo = &(frame->ImageDesc);
        for (j=0; j<frame->ExtensionBlockCount; j++) {
            if (frame->ExtensionBlocks[j].Function == GRAPHICS_EXT_FUNC_CODE) {
                ext = &(frame->ExtensionBlocks[j]);
                break;
            }
        }

        if (ext) {
            frame_delay = delay(ext);
            if (frame_delay == 0)
                frame_delay = ZERO_DELAY;

            gif_info->total_duration += frame_delay;
        }
    }
    gif_info->frame_duration=gif_info->total_duration/gif->ImageCount;
    gif_info->total_frame=gif->ImageCount;
    LOGE("gif image total_duration   %d",  gif_info->total_duration);
    LOGE("gif image frame_duration   %d",  gif_info->frame_duration);
    LOGE("gif image frame_duration   %d",  gif_info->total_frame);
    return (long long) gif;
}


JNIEXPORT jint JNICALL
Java_com_dongnao_gifplayer_GifHandler_updateFrame(JNIEnv *env, jclass type, jlong gif,
                                                  jobject bitmap) {
    GifFileType* gif_handle = (GifFileType *)gif;
    GifAnimInfo* gifAnimInfo= (GifAnimInfo *) gif_handle->UserData;
    AndroidBitmapInfo info;
    void * pixels;
    int ret;
    int framePage=gifAnimInfo->current_frame;

    gifAnimInfo->current_frame=gifAnimInfo->current_frame+1;
    if (framePage >= gifAnimInfo->total_frame) {
        gifAnimInfo->current_frame=0;
        framePage=0;
    }
    AndroidBitmap_getInfo(env, bitmap, &info);
    LOGE("framesPage   %d     ",framePage);
    AndroidBitmap_lockPixels(env,bitmap,&pixels);

    drawFrame(gif_handle, &info, (int *) pixels, framePage, false);
    AndroidBitmap_unlockPixels(env,bitmap);
    return gifAnimInfo->frame_duration;
}
}