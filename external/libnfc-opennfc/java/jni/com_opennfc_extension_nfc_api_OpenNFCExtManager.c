#include <JNIHelp.h>
#include <jni.h>
#include <android/log.h>
#include "open_nfc_extension.h"

static JavaVM* globalJvm = NULL;
static jclass classManager = NULL;


static JNIEnv* getJniEnv() {
	JNIEnv* ret = NULL;
	JavaVMAttachArgs thread_args;

	thread_args.name = "open_nfc_ext_thread";
	thread_args.version = JNI_VERSION_1_6;
	thread_args.group = NULL;

	(*globalJvm)->AttachCurrentThread(globalJvm, &ret, &thread_args);
	return ret;
}

static void freeJniEnv() {
	(*globalJvm)->DetachCurrentThread(globalJvm);
}

static void com_opennfc_extension_nfc_api_OpenNFCExtManager_nativeSendData(JNIEnv * e, jobject o, jint extClientId, jbyte cmd, jint reqId, jbyteArray data)
{
	JNIEnv env = *e;
	bool_t status;
	tMessageOpenNFC msg;

	LogInformation("nativeSendData(): ENTER");
	msg.header.clientId = extClientId;
	msg.header.commandId = cmd;
	msg.header.reqId = reqId;
	msg.pData = (uint8_t*) env->GetByteArrayElements(e, data, NULL);
	msg.header.length  = (msg.pData == NULL) ? 0 : env->GetArrayLength(e, data);

	LogInformation("nativeSendData: length=%d, pData=%p", msg.header.length, msg.pData);
	status = sendOpenNfcExtMessage(&msg);

	env->ReleaseIntArrayElements(e, data, (jint*) msg.pData, 0);

/*
	pthread_t openNfcExtRcvNotificationsThreadId;
	pthread_create(&openNfcExtRcvNotificationsThreadId, NULL, testThread, (void*) o);
*/

//	   f = e->GetFieldID(c, "mConnectedTechIndex", "I");

}

/*	callback function to be called when the notification for Open NFC Extension Manager's reply is received */
void openNfcExtNotifCallback (tMessageOpenNFC* pMessage)
{
	LogInformation("openNfcExtNotifCallback: ENTER");

	JNIEnv* e = getJniEnv();
	jmethodID method;
	JNIEnv env = *e;
	jbyteArray outData = env->NewByteArray(e, pMessage->header.length);
	if (pMessage->header.length > 0) {
		env->SetByteArrayRegion(e, outData, 0, pMessage->header.length,
		  (jbyte *) pMessage->pData);
	}

	method = env->GetStaticMethodID(e, classManager, "replyCallback", "(II[B)V");
	if (method == 0) {
	   LogError("openNfcExtNotifCallback(): can't call replyCallback()");
	   return;
	}

	env->CallStaticVoidMethod(e, classManager, method, pMessage->header.reqId, pMessage->header.errStatus,
			outData);
	env->DeleteLocalRef(e, outData);
	freeJniEnv();

}

static void com_opennfc_extension_nfc_api_OpenNFCExtManager_initialize(JNIEnv * e, jobject o)
{
	JNIEnv env = *e;
	jclass c;
	jfieldID f;

//	registerOpenNfcExtNotificationCallback(openNfcExtNotifCallback);

	if (! openOpenNfcExtConnection()) {
		LogError("initalize(): openOpenNfcExtConnection() failed");
	}
}

static JNINativeMethod gMethods[] =
{
		{"nativeSendData",	"(IBI[B)V",	(void*) com_opennfc_extension_nfc_api_OpenNFCExtManager_nativeSendData},
		{"initialize",	"()V",	(void*) com_opennfc_extension_nfc_api_OpenNFCExtManager_initialize},

};

int initalize_com_opennfc_extension_nfc_api_OpenNFCExtManager(JNIEnv * environmentJNI)
{
	jclass classTemporary = (*environmentJNI)->FindClass(environmentJNI, "com/opennfc/extension/nfc/api/OpenNFCExtManager");
	classManager = (*environmentJNI)->NewGlobalRef(environmentJNI, classTemporary);
	(*environmentJNI)->DeleteLocalRef(environmentJNI, classTemporary);

	return jniRegisterNativeMethods(environmentJNI, "com/opennfc/extension/nfc/api/OpenNFCExtManager",	gMethods, NELEM(gMethods));
}



jint JNI_OnLoad(JavaVM *jvm, void *reserved)
{
	JNIEnv*	environmentJNI;

	if((*jvm)->GetEnv(jvm, (void **)(& environmentJNI), JNI_VERSION_1_6))
	{
		return JNI_ERR;
	}

	if(initalize_com_opennfc_extension_nfc_api_OpenNFCExtManager(environmentJNI))
	{
		return JNI_ERR;
	}
	globalJvm = jvm;
	return JNI_VERSION_1_6;
}
