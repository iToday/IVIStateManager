
#ifndef ILOG_H
#define ILOG_H
#include <utils/Log.h>
#include <android/log.h>
#ifdef TAG

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG , TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO  , TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN  , TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR , TAG, __VA_ARGS__)

#else

#define LOGV(...) ((void)0)
#define LOGD(...) ((void)0)
#define LOGI(...) ((void)0)
#define LOGW(...) ((void)0)
#define LOGE(...) ((void)0)

#endif

#ifdef TAG_FILE

#define LOGF(...) ((void)0)

#else
#define LOGF(...) ((void)0)
#endif

#endif
