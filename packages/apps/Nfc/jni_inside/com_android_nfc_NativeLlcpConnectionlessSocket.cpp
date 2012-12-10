/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
* These methods are not used for OpenNFC architecture
*/
#include <semaphore.h>
#include <errno.h>

#include "com_android_nfc.h"


namespace android {

static jboolean com_android_nfc_NativeLlcpConnectionlessSocket_doSendTo(JNIEnv *e, jobject o, jint nsap, jbyteArray data)
{
	return JNI_FALSE;
}

static jobject com_android_nfc_NativeLlcpConnectionlessSocket_doReceiveFrom(JNIEnv *e, jobject o, jint linkMiu)
{
	return NULL;
}

static jboolean com_android_nfc_NativeLlcpConnectionlessSocket_doClose(JNIEnv *e, jobject o)
{
	return JNI_FALSE;
}


/*
 * JNI registration.
 */
static JNINativeMethod gMethods[] =
{
   {"doSendTo", "(I[B)Z", (void *)com_android_nfc_NativeLlcpConnectionlessSocket_doSendTo},
      
   {"doReceiveFrom", "(I)Landroid/nfc/LlcpPacket;", (void *)com_android_nfc_NativeLlcpConnectionlessSocket_doReceiveFrom},
      
   {"doClose", "()Z", (void *)com_android_nfc_NativeLlcpConnectionlessSocket_doClose},
};


int register_com_android_nfc_NativeLlcpConnectionlessSocket(JNIEnv *e)
{
   return jniRegisterNativeMethods(e,
      "com/android/nfc/nxp/NativeLlcpConnectionlessSocket",
      gMethods, NELEM(gMethods));
}

} // android namespace
