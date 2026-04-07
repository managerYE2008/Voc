#include<jni.h>
#include<string>
#include <android\log.h>

std::string nativeGetPreviewText(const std::string& uri_path){

}

extern "C"{
    JNIEXPORT jstring JNICALL
    Java_com_example_volcabularycards_ui_activity_ExcelWordAddActivity_nativeGetPreviewText(
        JNIEnv* env,
        jobject thiz,          // ← 这个参数必须有
        jbyteArray fileData,   // byte[] 对应 jbyteArray
        jint maxRows)  {

        // 1. jstring → std::string


        // 3. std::string → jstring
        //return env->NewStringUTF(result.c_str());
    }
};