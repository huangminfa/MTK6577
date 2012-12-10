/*
 * Copyright 2008, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "wifi"

#include "jni.h"
#include <ScopedUtfChars.h>
#include <utils/misc.h>
#include <android_runtime/AndroidRuntime.h>
#include <utils/Log.h>
#include <utils/String16.h>
#include <utils/String8.h>
#include <cutils/xlog.h>
#include <pthread.h>
#include <unicode/ucnv.h>
#include <unicode/ucsdet.h>

#include <net/if.h>
#include <sys/socket.h>
#include <linux/wireless.h>

#include "wifi.h"
#include "wifi_hotspot.h"

#define WIFI_PKG_NAME "android/net/wifi/WifiNative"
#define BUF_SIZE 256
#define SSID_LEN 500
#define LINE_LEN 1024
#define CONVERT_LINE_LEN 2048
#define REPLY_LEN 4096
#define CHARSET_CN ("gbk")

#define IOCTL_SET_INTS                   (SIOCIWFIRSTPRIV + 12)
#define PRIV_CMD_SET_TX_POWER            25 

//TODO: This file can be refactored to push a lot of the functionality to java
//with just a few JNI calls - doBoolean/doInt/doString

namespace android {

static jmethodID cached_WifiNative_notifySupplicantHung;
static jboolean sScanModeActive = false;
static jboolean sIsHotspot = false;

struct accessPointObjectItem {
    String8 *ssid;
    String8 *bssid;
    bool    isCh;
    struct  accessPointObjectItem *pNext;
};

struct accessPointObjectItem *g_pItemList = NULL;
struct accessPointObjectItem *g_pLastNode = NULL;
pthread_mutex_t *g_pItemListMutex = NULL;
String8 *g_pCurrentSSID = NULL;
bool g_isChSSID = false;

static void addAPObjectItem(const char *ssid, const char *bssid, bool isCh)
{
	if (NULL == ssid || NULL == bssid) {
		XLOGE("ssid or bssid is NULL");
		return;
	}
	
	struct accessPointObjectItem *pTmpItemNode = NULL;
    struct accessPointObjectItem *pItemNode = NULL;
	bool foundItem = false;
	
	pthread_mutex_lock(g_pItemListMutex);
	pTmpItemNode = g_pItemList;
	
	while (pTmpItemNode) {
		if (pTmpItemNode->bssid && (*(pTmpItemNode->bssid) == bssid)) {
			foundItem = true;
			break;
		}
		pTmpItemNode = pTmpItemNode->pNext;
	}
	if (foundItem) {
		*(pTmpItemNode->ssid) = ssid;
		pTmpItemNode->isCh = isCh;
		//XLOGD("found AP %s", pTmpItemNode->ssid->string());			
	} else {
		pItemNode = new struct accessPointObjectItem();
		if (NULL == pItemNode) {
		    XLOGE("Failed to allocate memory for new item!");
			goto EXIT;
		}
		memset(pItemNode, 0, sizeof(accessPointObjectItem));	
		pItemNode->bssid = new String8();
		if (NULL == pItemNode->bssid) {
			XLOGE("Failed to allocate memory for new bssid!");
			goto EXIT;
		}
		*(pItemNode->bssid) = bssid;
		pItemNode->ssid = new String8();
		if (NULL == pItemNode->ssid) {
			XLOGE("Failed to allocate memory for new ssid!");
			goto EXIT;
		}
		*(pItemNode->ssid) = ssid;
		pItemNode->isCh = isCh;
		pItemNode->pNext = NULL;
        //XLOGD("not found AP, new one %s", ssid);
		
		if (NULL == g_pItemList) {
		    g_pItemList = pItemNode;
		    g_pLastNode = g_pItemList;
		} else {
		    g_pLastNode->pNext = pItemNode;
		    g_pLastNode = pItemNode;
		}
	}

EXIT:
	pthread_mutex_unlock(g_pItemListMutex);
}

static bool isUTF8String(const char* str, long length)
{
    int nBytes = 0;
    unsigned char chr;
    bool bAllAscii = true; 
    for (int i = 0; i < length; i++) {
        chr = *(str+i);
        if ((chr & 0x80) != 0) {
            bAllAscii = false;
        }
        if (0 == nBytes) {
            if (chr >= 0x80) {
                if (chr >= 0xFC && chr <= 0xFD) {
                    nBytes = 6;
                } else if (chr >= 0xF8) {
                    nBytes = 5;
                } else if (chr >= 0xF0) {
                    nBytes = 4;
                } else if (chr >= 0xE0) {
                    nBytes = 3;
                } else if (chr >= 0xC0) {
                    nBytes = 2;
                } else {
                    return false;
                }
                nBytes--;
            }
        } else {
            if ((chr & 0xC0) != 0x80) {
                return false;
            }
            nBytes--;
        }
    }

    if (nBytes > 0 || bAllAscii) {
        return false;
    }
    return true;
}

static void parseScanResults(String16& str, const char *reply)
{
	unsigned int lineCount = 0;
	unsigned int lineBeg = 0, lineEnd = 0;
	size_t  maxLineLength = 0;
	size_t  replyLen = strlen(reply);
    char    *ssid = NULL;
    char    *flag = NULL;
    char 	*pos = NULL;
    char	bssid[BUF_SIZE] = {0};
    char	freq[BUF_SIZE] = {0};
    char	signal[BUF_SIZE] = {0};
	String8 line;
	
	UChar dest[CONVERT_LINE_LEN] = {0};
	UErrorCode err = U_ZERO_ERROR;
	UCharsetDetector* pucd = ucsdet_open(&err);
	if (U_FAILURE(err)) {
	    XLOGE("ucsdet_open error");
		return;
	}
	UConverter* pConverter = ucnv_open(CHARSET_CN, &err);
	if (U_FAILURE(err)) {
	    XLOGE("ucnv_open error");
		ucsdet_close(pucd);
		return;
	}

    for (lineBeg = 0, lineEnd = 0; lineEnd <= replyLen; ++lineEnd) {
		if (lineEnd == replyLen || '\n' == reply[lineEnd]) {
		    line.setTo(reply + lineBeg, lineEnd - lineBeg + 1);
		    if (line.size() >= maxLineLength) {
		        maxLineLength = line.size();
		        //XLOGD("max line=%s, maxLineLength=%d", line.string(), maxLineLength);
		    }
		    lineBeg = lineEnd + 1;
		}
	}
	ssid = new char[maxLineLength];
	if (NULL == ssid) {
	    XLOGE("Failed to allocate memory for ssid!");
	    goto EXIT;
	}
	flag = new char[maxLineLength];
	if (NULL == flag) {
	    XLOGE("Failed to allocate memory for flag!");
	    goto EXIT;
	}
	//Parse every line of the reply to construct accessPointObjectItem list
	for (lineBeg = 0, lineEnd = 0; lineEnd <= replyLen; ++lineEnd) {
		if (lineEnd == replyLen || '\n' == reply[lineEnd]) {
			++lineCount;
			line.setTo(reply + lineBeg, lineEnd - lineBeg + 1);
			bool isUTF8 = isUTF8String(line.string(), line.size());
			//XLOGD("%s, line=%s, isUTF8=%d", __FUNCTION__, line.string(), isUTF8);
			memset(ssid, 0, maxLineLength);
			memset(flag, 0, maxLineLength);
			if (1 != lineCount) {
			    sscanf(line.string(), "%s %s %s %s %[^\n]", bssid, freq, signal, flag, ssid);
				if ('[' != flag[0])	{
					sscanf(line.string(), "%s %s %s %[^\n]", bssid, freq, signal, ssid);
				}
				//XLOGD("After sscanf, bssid:%s, freq:%s, signal:%s, ssid:%s", bssid, freq, signal, ssid);
				bool isCh = false;
				for (pos = ssid; '\0' != *pos; pos++) {
					if (0x80 == (*pos & 0x80)) {
						isCh = true;
						break;
					}
				}
				addAPObjectItem(ssid, bssid, isCh);
			}
			
			if (!isUTF8) {
				ucnv_toUChars(pConverter, dest, CONVERT_LINE_LEN, line.string(), line.size(), &err);
				if (U_FAILURE(err)) {
					XLOGE("ucnv_toUChars error");
					goto EXIT;
				}
				str += String16(dest);
				memset(dest, 0, CONVERT_LINE_LEN);
			} else {
				str += String16(line.string());
			}
			lineBeg = lineEnd + 1;
		}
	}

EXIT:
    if (ssid != NULL) {
        delete[] ssid;
	    ssid = NULL;
	}
	if (flag != NULL) {
        delete[] flag;
	    flag = NULL;
	}
	ucsdet_close(pucd);
	ucnv_close(pConverter);	
}

static void constructReply(String16& str, const char *cmd, const char *reply)
{
	//XLOGD("%s, cmd = %s, reply = %s", __FUNCTION__, cmd, reply);
	size_t 	replyLen = strlen(reply);
	unsigned int lineBeg = 0, lineEnd = 0;
	String8 line;
	UChar dest[CONVERT_LINE_LEN] = {0};
	UErrorCode err = U_ZERO_ERROR;
	UCharsetDetector* pucd = ucsdet_open(&err);
	if (U_FAILURE(err)) {
	    XLOGE("ucsdet_open error");
		return;
	}
	UConverter* pConverter = ucnv_open(CHARSET_CN, &err);
	if (U_FAILURE(err)) {
	    XLOGE("ucnv_open error");
		ucsdet_close(pucd);
		return;
	}

    for (lineBeg = 0, lineEnd = 0; lineEnd <= replyLen; ++lineEnd) {
	    if (lineEnd == replyLen || '\n' == reply[lineEnd]) {
	        line.setTo(reply + lineBeg, lineEnd - lineBeg + 1);
			bool isUTF8 = isUTF8String(line.string(), line.size());
		    //XLOGD("%s, line=%s, isUTF8=%d", __FUNCTION__, line.string(), isUTF8);
		    if (!isUTF8) {
				ucnv_toUChars(pConverter, dest, CONVERT_LINE_LEN, line.string(), line.size(), &err);
				if (U_FAILURE(err)) {
				    XLOGE("ucnv_toUChars error");
					goto EXIT;
				}
				str += String16(dest);
				memset(dest, 0, CONVERT_LINE_LEN);
			} else {
				str += String16(line.string());
			}
			lineBeg = lineEnd + 1;
	    }  	    
	}

EXIT:
	ucsdet_close(pucd);
	ucnv_close(pConverter);	
}

static int doCommand(JNIEnv *env, const char *cmd, char *replybuf, int replybuflen)
{
    size_t reply_len = replybuflen - 1;
    int result = ::wifi_command(cmd, replybuf, &reply_len);
    if (-2 == result) {
        XLOGE("supplicant's response timed out");
        jclass wifi = env->FindClass(WIFI_PKG_NAME);
        LOG_FATAL_IF(wifi == NULL, "Unable to find class " WIFI_PKG_NAME);
        env->CallStaticVoidMethod(wifi, cached_WifiNative_notifySupplicantHung);
    }

    if (result != 0) {
        return -1;
    } else {
        // Strip off trailing newline
        if (reply_len > 0 && replybuf[reply_len-1] == '\n')
            replybuf[reply_len-1] = '\0';
        else
            replybuf[reply_len] = '\0';
        return 0;
    }
}

static jint doIntCommand(JNIEnv* env, const char* fmt, ...)
{
    char buf[BUF_SIZE] = {0};
    va_list args;
    va_start(args, fmt);
    int byteCount = vsnprintf(buf, sizeof(buf), fmt, args);
    va_end(args);
    if (byteCount < 0 || byteCount >= BUF_SIZE) {
        return -1;
    }
    char reply[BUF_SIZE] = {0};
    if (doCommand(env, buf, reply, sizeof(reply)) != 0) {
        return -1;
    }
    return static_cast<jint>(atoi(reply));
}

static jboolean doBooleanCommand(JNIEnv* env, const char* expect, const char* fmt, ...)
{
    char buf[BUF_SIZE] = {0};
    va_list args;
    va_start(args, fmt);
    int byteCount = vsnprintf(buf, sizeof(buf), fmt, args);
    va_end(args);
    if (byteCount < 0 || byteCount >= BUF_SIZE) {
        return JNI_FALSE;
    }
    char reply[BUF_SIZE] = {0};
    if (doCommand(env, buf, reply, sizeof(reply)) != 0) {
        return JNI_FALSE;
    }
    return (strcmp(reply, expect) == 0);
}

static void printLongReply(const char *reply) {
	unsigned int lineBeg = 0, lineEnd = 0;
	size_t replyLen = strlen(reply);
	String8 line;
	for (lineBeg = 0, lineEnd = 0; lineEnd <= replyLen; ++lineEnd) {
		if (lineEnd == replyLen || '\n' == reply[lineEnd]) {
			line.setTo(reply + lineBeg, lineEnd - lineBeg + 1);
			XLOGI("%s", line.string());
			lineBeg = lineEnd + 1;
		}
	}		
}

// Send a command to the supplicant, and return the reply as a String
static jstring doStringCommand(JNIEnv* env, const char* fmt, ...) {
    char buf[BUF_SIZE] = {0};
    va_list args;
    va_start(args, fmt);
    int byteCount = vsnprintf(buf, sizeof(buf), fmt, args);
    va_end(args);
    if (byteCount < 0 || byteCount >= BUF_SIZE) {
        return NULL;
    }
    char reply[REPLY_LEN] = {0};
    if (doCommand(env, buf, reply, sizeof(reply)) != 0) {
        return NULL;
    }
    // TODO: why not just NewStringUTF?
    //XLOGD("%s, buf %s", __FUNCTION__, buf);		 
	String16 str;
	if (0 == strcmp(buf, "SCAN_RESULTS") ||
	    (strstr(buf, "GET_NETWORK") && strstr(buf, "ssid")) ||
		(0 == strcmp(buf, "STATUS")) ||
		(0 == strcmp(buf, "LIST_NETWORKS"))) {	
		if (0 == strcmp(buf, "SCAN_RESULTS")) {
		    printLongReply(reply);
			parseScanResults(str, reply);
		} else {
		    //printLongReply(reply);
			constructReply(str, buf, reply);
		}
	} else {
	    //printLongReply(reply);
        str += String16((char *)reply);
	}
    return env->NewString((const jchar *)str.string(), str.size());
}

static jboolean android_net_wifi_isDriverLoaded(JNIEnv* env, jobject)
{
    return (jboolean)(::is_wifi_driver_loaded() == 1);
}

static jboolean android_net_wifi_loadDriver(JNIEnv* env, jobject)
{
    g_pItemListMutex = new pthread_mutex_t;
    if (NULL == g_pItemListMutex) {
        XLOGE("Failed to allocate memory for g_pItemListMutex!");
		return JNI_FALSE;
    }
    pthread_mutex_init(g_pItemListMutex, NULL); //27803
	g_pCurrentSSID = new String8();
	if (NULL == g_pCurrentSSID) {
	    XLOGE("Failed to allocate memory for g_pCurrentSSID!");
		return JNI_FALSE;
	}
    return (jboolean)(::wifi_load_driver() == 0);
}

static jboolean android_net_wifi_unloadDriver(JNIEnv* env, jobject)
{
    if (g_pCurrentSSID != NULL) { //27803
		delete g_pCurrentSSID;
		g_pCurrentSSID = NULL;
	}
	if (g_pItemListMutex != NULL) {
	    pthread_mutex_lock(g_pItemListMutex);
	    struct accessPointObjectItem *pCurrentNode = g_pItemList;
        struct accessPointObjectItem *pNextNode = NULL;
        while (pCurrentNode) {
            pNextNode = pCurrentNode->pNext;
            if (NULL != pCurrentNode->ssid) {
                delete pCurrentNode->ssid;
                pCurrentNode->ssid = NULL;
            }
            if (NULL != pCurrentNode->bssid) {
                delete pCurrentNode->bssid;
                pCurrentNode->bssid = NULL;
            }
            delete pCurrentNode;
            pCurrentNode = pNextNode;
        }
        g_pItemList = NULL;
        g_pLastNode = NULL;
        pthread_mutex_unlock(g_pItemListMutex);
        pthread_mutex_destroy(g_pItemListMutex);
        delete g_pItemListMutex;
        g_pItemListMutex = NULL;
    }
    return (jboolean)(::wifi_unload_driver() == 0);
}

static jboolean android_net_wifi_startSupplicant(JNIEnv* env, jobject)
{
    return (jboolean)(::wifi_start_supplicant() == 0);
}

static jboolean android_net_wifi_startP2pSupplicant(JNIEnv* env, jobject)
{
    return (jboolean)(::wifi_start_p2p_supplicant() == 0);
}

static jboolean android_net_wifi_stopSupplicant(JNIEnv* env, jobject)
{
    return doBooleanCommand(env, "OK", "TERMINATE");
}

static jboolean android_net_wifi_killSupplicant(JNIEnv* env, jobject)
{
    return (jboolean)(::wifi_stop_supplicant() == 0);
}

static jboolean android_net_wifi_connectToSupplicant(JNIEnv* env, jobject, jboolean isHotspot)
{
    sIsHotspot = isHotspot;
    if (isHotspot) {
#ifdef MTK_WLAN_SUPPORT	
        return (jboolean)(::wifi_hotspot_connect_to_supplicant() == 0);
#else
        return JNI_FALSE;
#endif                    
    } else {
        return (jboolean)(::wifi_connect_to_supplicant() == 0);
    }
}

static void android_net_wifi_closeSupplicantConnection(JNIEnv* env, jobject)
{
    if (sIsHotspot) {
#ifdef MTK_WLAN_SUPPORT        
        ::wifi_hotspot_close_supplicant_connection();
#endif            
    } else {
        ::wifi_close_supplicant_connection();
    }
}

static jstring android_net_wifi_waitForEvent(JNIEnv* env, jobject)
{
    char buf[BUF_SIZE] = {0};
    int nread = -1;
    if (sIsHotspot) {
#ifdef MTK_WLAN_SUPPORT        
        nread = ::wifi_hotspot_wait_for_event(buf, sizeof buf);
#endif                                
    } else {
        nread = ::wifi_wait_for_event(buf, sizeof buf);
    }
    if (nread > 0) {
        return env->NewStringUTF(buf);
    } else {
        return NULL;
    }
}

static jstring android_net_wifi_listNetworksCommand(JNIEnv* env, jobject)
{
    return doStringCommand(env, "LIST_NETWORKS");
}

static jint android_net_wifi_addNetworkCommand(JNIEnv* env, jobject)
{
    return doIntCommand(env, "ADD_NETWORK");
}

static jboolean android_net_wifi_wpsPbcCommand(JNIEnv* env, jobject, jstring javaBssid)
{
    ScopedUtfChars bssid(env, javaBssid);
    if (bssid.c_str() == NULL) {
        return JNI_FALSE;
    }
    return doBooleanCommand(env, "OK", "WPS_PBC %s", bssid.c_str());
}

static jboolean android_net_wifi_wpsPinFromAccessPointCommand(JNIEnv* env, jobject,
        jstring javaBssid, jstring javaApPin)
{
    ScopedUtfChars bssid(env, javaBssid);
    if (bssid.c_str() == NULL) {
        return JNI_FALSE;
    }
    ScopedUtfChars apPin(env, javaApPin);
    if (apPin.c_str() == NULL) {
        return JNI_FALSE;
    }
    return doBooleanCommand(env, "OK", "WPS_REG %s %s", bssid.c_str(), apPin.c_str());
}

static jstring android_net_wifi_wpsPinFromDeviceCommand(JNIEnv* env, jobject, jstring javaBssid)
{
    ScopedUtfChars bssid(env, javaBssid);
    if (bssid.c_str() == NULL) {
        return NULL;
    }
    return doStringCommand(env, "WPS_PIN %s", bssid.c_str());
}

static jboolean android_net_wifi_setCountryCodeCommand(JNIEnv* env, jobject, jstring javaCountry)
{
    ScopedUtfChars country(env, javaCountry);
    if (country.c_str() == NULL) {
        return JNI_FALSE;
    }
    return doBooleanCommand(env, "OK", "DRIVER COUNTRY %s", country.c_str());
}

static jboolean android_net_wifi_setNetworkVariableCommand(JNIEnv* env,
                                                           jobject,
                                                           jint netId,
                                                           jstring javaName,
                                                           jstring javaValue)
{
    ScopedUtfChars name(env, javaName);
    if (name.c_str() == NULL) {
        return JNI_FALSE;
    }
    ScopedUtfChars value(env, javaValue);
    if (value.c_str() == NULL) {
        return JNI_FALSE;
    }
    XLOGD("setNetworkVariableCommand, name:%s, value:%s, netId:%d", name.c_str(), value.c_str(), netId);
    struct accessPointObjectItem *pTmpItemNode = NULL;
    if (0 == strcmp(name.c_str(), "bssid")) {
		pthread_mutex_lock(g_pItemListMutex);
    	pTmpItemNode = g_pItemList;   
    	if (NULL == pTmpItemNode) {
    		XLOGD("g_pItemList is NULL");
    	}
    	if (NULL == g_pCurrentSSID) {
	        XLOGE("g_pCurrentSSID is NULL");
	        pthread_mutex_unlock(g_pItemListMutex);
	        return JNI_FALSE;
	    }
		while (pTmpItemNode) {
			if (pTmpItemNode->bssid && (0 == strcmp(pTmpItemNode->bssid->string(), value.c_str())) && pTmpItemNode->ssid) { 
				*g_pCurrentSSID = *(pTmpItemNode->ssid);
				g_isChSSID = pTmpItemNode->isCh;
				XLOGD("Found bssid:%s, g_pCurrentSSID:%s, g_isChSSID:%d", pTmpItemNode->bssid->string(), g_pCurrentSSID->string(), g_isChSSID);
				break;
			}			
			pTmpItemNode = pTmpItemNode->pNext;
		}
		
		pthread_mutex_unlock(g_pItemListMutex);
        return JNI_TRUE;		
	}
	
    if (0 == strcmp(name.c_str(), "ssid") && g_isChSSID) {
		g_isChSSID = false;
		char finalSSID[SSID_LEN] = {0};
        char tmp[SSID_LEN] = {0};
		char *ptr = (char*)value.c_str() + 1;
		while ((*ptr) == ' ') {
			ptr++;
		}
		strncpy(tmp, value.c_str() + 1, ptr - value.c_str() - 1);
		sprintf(finalSSID, "\"%s%s\"", tmp, g_pCurrentSSID->string());
		//XLOGD("finalSSID:%s", finalSSID);
		return doBooleanCommand(env, "OK", "SET_NETWORK %d %s %s", netId, name.c_str(), finalSSID);
	}
	
    return doBooleanCommand(env, "OK", "SET_NETWORK %d %s %s", netId, name.c_str(), value.c_str());
}

static jstring android_net_wifi_getNetworkVariableCommand(JNIEnv* env,
                                                          jobject,
                                                          jint netId,
                                                          jstring javaName)
{
    ScopedUtfChars name(env, javaName);
    if (name.c_str() == NULL) {
        return NULL;
    }
    return doStringCommand(env, "GET_NETWORK %d %s", netId, name.c_str());
}

static jboolean android_net_wifi_removeNetworkCommand(JNIEnv* env, jobject, jint netId)
{
    return doBooleanCommand(env, "OK", "REMOVE_NETWORK %d", netId);
}

static jboolean android_net_wifi_enableNetworkCommand(JNIEnv* env,
                                                  jobject,
                                                  jint netId,
                                                  jboolean disableOthers)
{
    return doBooleanCommand(env, "OK", "%s_NETWORK %d", disableOthers ? "SELECT" : "ENABLE", netId);
}

static jboolean android_net_wifi_disableNetworkCommand(JNIEnv* env, jobject, jint netId)
{
    return doBooleanCommand(env, "OK", "DISABLE_NETWORK %d", netId);
}

static jstring android_net_wifi_statusCommand(JNIEnv* env, jobject)
{
    return doStringCommand(env, "STATUS");
}

static jboolean android_net_wifi_pingCommand(JNIEnv* env, jobject)
{
    return doBooleanCommand(env, "PONG", "PING");
}

static jstring android_net_wifi_scanResultsCommand(JNIEnv* env, jobject)
{
    return doStringCommand(env, "SCAN_RESULTS");
}

static jboolean android_net_wifi_disconnectCommand(JNIEnv* env, jobject)
{
    return doBooleanCommand(env, "OK", "DISCONNECT");
}

static jboolean android_net_wifi_reconnectCommand(JNIEnv* env, jobject)
{
    return doBooleanCommand(env, "OK", "RECONNECT");
}
static jboolean android_net_wifi_reassociateCommand(JNIEnv* env, jobject)
{
    return doBooleanCommand(env, "OK", "REASSOCIATE");
}

static jboolean doSetScanMode(JNIEnv* env, jboolean setActive)
{
    return doBooleanCommand(env, "OK", (setActive ? "DRIVER SCAN-ACTIVE" : "DRIVER SCAN-PASSIVE"));
}

static jboolean android_net_wifi_scanCommand(JNIEnv* env, jobject, jboolean forceActive)
{
    jboolean result;

    // Ignore any error from setting the scan mode.
    // The scan will still work.
    if (forceActive && !sScanModeActive)
        doSetScanMode(env, true);
    result = doBooleanCommand(env, "OK", "SCAN");
    if (forceActive && !sScanModeActive)
        doSetScanMode(env, sScanModeActive);
    return result;
}

static jboolean android_net_wifi_setScanModeCommand(JNIEnv* env, jobject, jboolean setActive)
{
    sScanModeActive = setActive;
    return doSetScanMode(env, setActive);
}

static jboolean android_net_wifi_startDriverCommand(JNIEnv* env, jobject)
{
    return doBooleanCommand(env, "OK", "DRIVER START");
}

static jboolean android_net_wifi_stopDriverCommand(JNIEnv* env, jobject)
{
    return doBooleanCommand(env, "OK", "DRIVER STOP");
}

/*
    Multicast filtering rules work as follows:

    The driver can filter multicast (v4 and/or v6) and broadcast packets when in
    a power optimized mode (typically when screen goes off).

    In order to prevent the driver from filtering the multicast/broadcast packets, we have to
    add a DRIVER RXFILTER-ADD rule followed by DRIVER RXFILTER-START to make the rule effective

    DRIVER RXFILTER-ADD Num
        where Num = 0 - Unicast, 1 - Broadcast, 2 - Mutil4 or 3 - Multi6

    and DRIVER RXFILTER-START

    In order to stop the usage of these rules, we do

    DRIVER RXFILTER-STOP
    DRIVER RXFILTER-REMOVE Num
        where Num is as described for RXFILTER-ADD

    The  SETSUSPENDOPT driver command overrides the filtering rules
*/

static jboolean android_net_wifi_startMultiV4Filtering(JNIEnv* env, jobject)
{
    return doBooleanCommand(env, "OK", "DRIVER RXFILTER-STOP")
            && doBooleanCommand(env, "OK", "DRIVER RXFILTER-REMOVE 2")
            && doBooleanCommand(env, "OK", "DRIVER RXFILTER-START");
}

static jboolean android_net_wifi_stopMultiV4Filtering(JNIEnv* env, jobject)
{
    return doBooleanCommand(env, "OK", "DRIVER RXFILTER-ADD 2")
            && doBooleanCommand(env, "OK", "DRIVER RXFILTER-START");
}

static jboolean android_net_wifi_startMultiV6Filtering(JNIEnv* env, jobject)
{
    return doBooleanCommand(env, "OK", "DRIVER RXFILTER-STOP")
            && doBooleanCommand(env, "OK", "DRIVER RXFILTER-REMOVE 3")
            && doBooleanCommand(env, "OK", "DRIVER RXFILTER-START");
}

static jboolean android_net_wifi_stopMultiV6Filtering(JNIEnv* env, jobject)
{
    return doBooleanCommand(env, "OK", "DRIVER RXFILTER-ADD 3")
        && doBooleanCommand(env, "OK", "DRIVER RXFILTER-START");
}


static jint android_net_wifi_getRssiHelper(JNIEnv* env, const char *cmd)
{
    char reply[BUF_SIZE] = {0};
    int rssi = -200;

    if (doCommand(env, cmd, reply, sizeof(reply)) != 0) {
        return (jint)-1;
    }

    // reply comes back in the form "<SSID> rssi XX" where XX is the
    // number we're interested in.  if we're associating, it returns "OK".
    // beware - <SSID> can contain spaces.
    if (strcmp(reply, "OK") != 0) {
        // beware of trailing spaces
        char* end = reply + strlen(reply);
        while (end > reply && end[-1] == ' ') {
            end--;
        }
        *end = 0;

        char* lastSpace = strrchr(reply, ' ');
        // lastSpace should be preceded by "rssi" and followed by the value
        if (lastSpace && !strncasecmp(lastSpace - 4, "rssi", 4)) {
            sscanf(lastSpace + 1, "%d", &rssi);
        }
    }
    return (jint)rssi;
}

static jstring android_net_wifi_getMacAddressCommand(JNIEnv* env, jobject)
{
    char reply[BUF_SIZE] = {0};
    char buf[BUF_SIZE] = {0};

    if (doCommand(env, "DRIVER MACADDR", reply, sizeof(reply)) != 0) {
        return NULL;
    }
    // reply comes back in the form "Macaddr = XX.XX.XX.XX.XX.XX" where XX
    // is the part of the string we're interested in.
    if (sscanf(reply, "%*s = %255s", buf) == 1) {
        return env->NewStringUTF(buf);
    }
    return NULL;
}

static jboolean android_net_wifi_setPowerModeCommand(JNIEnv* env, jobject, jint mode)
{
    return doBooleanCommand(env, "OK", "DRIVER POWERMODE %d", mode);
}

static jint android_net_wifi_getPowerModeCommand(JNIEnv* env, jobject)
{
    char reply[BUF_SIZE] = {0};
    int power;

    if (doCommand(env, "DRIVER GETPOWER", reply, sizeof(reply)) != 0) {
        return (jint)-1;
    }
    // reply comes back in the form "powermode = XX" where XX is the
    // number we're interested in.
    if (sscanf(reply, "%*s = %u", &power) != 1) {
        return (jint)-1;
    }
    return (jint)power;
}

static jboolean android_net_wifi_setBandCommand(JNIEnv* env, jobject, jint band)
{
    return doBooleanCommand(env, "OK", "DRIVER SETBAND %d", band);
}

static jint android_net_wifi_getBandCommand(JNIEnv* env, jobject)
{
    char reply[25] = {0};
    int band;

    if (doCommand(env, "DRIVER GETBAND", reply, sizeof(reply)) != 0) {
        return (jint)-1;
    }
    // reply comes back in the form "Band X" where X is the
    // number we're interested in.
    sscanf(reply, "%*s %u", &band);
    return (jint)band;
}

static jboolean android_net_wifi_setBluetoothCoexistenceModeCommand(JNIEnv* env, jobject, jint mode)
{
    return doBooleanCommand(env, "OK", "DRIVER BTCOEXMODE %d", mode);
}

static jboolean android_net_wifi_setBluetoothCoexistenceScanModeCommand(JNIEnv* env, jobject, jboolean setCoexScanMode)
{
    return doBooleanCommand(env, "OK", "DRIVER BTCOEXSCAN-%s", setCoexScanMode ? "START" : "STOP");
}

static jboolean android_net_wifi_saveConfigCommand(JNIEnv* env, jobject)
{
    // Make sure we never write out a value for AP_SCAN other than 1
    (void)doBooleanCommand(env, "OK", "AP_SCAN 1");
    return doBooleanCommand(env, "OK", "SAVE_CONFIG");
}

static jboolean android_net_wifi_reloadConfigCommand(JNIEnv* env, jobject)
{
    return doBooleanCommand(env, "OK", "RECONFIGURE");
}

static jboolean android_net_wifi_setScanResultHandlingCommand(JNIEnv* env, jobject, jint mode)
{
    return doBooleanCommand(env, "OK", "AP_SCAN %d", mode);
}

static jboolean android_net_wifi_addToBlacklistCommand(JNIEnv* env, jobject, jstring javaBssid)
{
    ScopedUtfChars bssid(env, javaBssid);
    if (bssid.c_str() == NULL) {
        return JNI_FALSE;
    }
    return doBooleanCommand(env, "OK", "BLACKLIST %s", bssid.c_str());
}

static jboolean android_net_wifi_clearBlacklistCommand(JNIEnv* env, jobject)
{
    return doBooleanCommand(env, "OK", "BLACKLIST clear");
}

static jboolean android_net_wifi_setSuspendOptimizationsCommand(JNIEnv* env, jobject, jboolean enabled)
{
    return doBooleanCommand(env, "OK", "DRIVER SETSUSPENDOPT %d", enabled ? 0 : 1);
}

static void android_net_wifi_enableBackgroundScanCommand(JNIEnv* env, jobject, jboolean enable)
{
    //Note: BGSCAN-START and BGSCAN-STOP are documented in core/res/res/values/config.xml
    //and will need an update if the names are changed
    if (enable) {
        doBooleanCommand(env, "OK", "DRIVER BGSCAN-START");
    } else {
        doBooleanCommand(env, "OK", "DRIVER BGSCAN-STOP");
    }
}

static void android_net_wifi_setScanIntervalCommand(JNIEnv* env, jobject, jint scanInterval)
{
    doBooleanCommand(env, "OK", "SCAN_INTERVAL %d", scanInterval);
}

static jboolean android_net_wifi_doBooleanCommand(JNIEnv* env, jobject, jstring javaCommand)
{
    ScopedUtfChars command(env, javaCommand);
    if (command.c_str() == NULL) {
        return JNI_FALSE;
    }
    return doBooleanCommand(env, "OK", "%s", command.c_str());
}

static jint android_net_wifi_doIntCommand(JNIEnv* env, jobject, jstring javaCommand)
{
    ScopedUtfChars command(env, javaCommand);
    if (command.c_str() == NULL) {
        return -1;
    }
    return doIntCommand(env, "%s", command.c_str());
}

static jstring android_net_wifi_doStringCommand(JNIEnv* env, jobject, jstring javaCommand)
{
    ScopedUtfChars command(env, javaCommand);
    if (command.c_str() == NULL) {
        return NULL;
    }
    return doStringCommand(env, "%s", command.c_str());
}

// MTK proprietary added by mtk03034
static jstring android_net_wifi_wpsPinCommand(JNIEnv* env, jobject clazz, jstring javaBssid)
{      
    ScopedUtfChars bssid(env, javaBssid);
    if (bssid.c_str() == NULL) {
        return NULL;
    }
    return doStringCommand(env, "WPS_PIN %s", bssid.c_str());
}

static jboolean android_net_wifi_mtk_wpsPbcCommand(JNIEnv* env, jobject clazz, jstring javaBssid)
{   
    ScopedUtfChars bssid(env, javaBssid);
    if (bssid.c_str() == NULL) {
        return JNI_FALSE;
    }
    return doBooleanCommand(env, "OK", "WPS_PBC %s", bssid.c_str());
}

static jboolean android_net_wifi_wpsAbortCommand(JNIEnv* env, jobject clazz)
{
    /* currently support *ANY* only */
    return doBooleanCommand(env, "OK", "WPS_ABORT");
}

// For CTIA Test
static jboolean android_net_wifi_CTIATestOffCommand(JNIEnv* env, jobject clazz)
{
    /* currently support *ANY* only */
    return doBooleanCommand(env, "OK", "DRIVER smt-test-off");
}

static jboolean android_net_wifi_CTIATestOnCommand(JNIEnv* env, jobject clazz)
{
    /* currently support *ANY* only */
    return doBooleanCommand(env, "OK", "DRIVER smt-test-on");
}

static jboolean android_net_wifi_CTIATestRateCommand(JNIEnv* env, jobject clazz, jint rate)
{   
    return doBooleanCommand(env, "OK", "DRIVER smt-rate %d", rate);
}

static jboolean android_net_wifi_CTIATestPowerCommand(JNIEnv* env, jobject clazz, jint powerMode)
{ 
    return doBooleanCommand(env, "OK", "SW_CMD_SET 0x10008000 0x%d", powerMode);
}

static jboolean android_net_wifi_CTIATestSetCommand(JNIEnv* env, jobject clazz, jint id, jint value)
{    
    return doBooleanCommand(env, "OK", "SW_CMD_SET 0x%x 0x%x", id, value);
}

static jstring android_net_wifi_CTIATestGetCommand(JNIEnv* env, jobject clazz, jint id)
{    
    return doStringCommand(env, "SW_CMD_GET 0x%x", id);
}

//MTK_OP01_PROTECT_START
static jboolean android_net_wifi_saveAPPriorityCommand(JNIEnv* env, jobject clazz)
{
    return doBooleanCommand(env, "OK", "SAVE_CONFIG");
}
//MTK_OP01_PROTECT_END

static jboolean android_net_wifi_setTxPowerEnabledCommand(JNIEnv* env, jobject clazz, jboolean enable)
{   
    jboolean result = JNI_FALSE;
    struct iwreq wrq;
    int skfd;
    int32_t au4TxPwr[2] = {4, 1};
    au4TxPwr[1] = enable ? 1 : 0;
    
    /* initialize socket */
    skfd = socket(PF_INET, SOCK_DGRAM, 0);
    if (skfd < 0) {
        XLOGE("Open socket failed");
        return result;
    }
    
    /* initliaze WEXT request */
    wrq.u.data.pointer = &(au4TxPwr[0]);
    wrq.u.data.length = 2;
    
    wrq.u.data.flags = PRIV_CMD_SET_TX_POWER;
    strncpy(wrq.ifr_name, "wlan0", IFNAMSIZ);
    
    /* do IOCTL */
    if (ioctl(skfd, IOCTL_SET_INTS, &wrq) < 0) {
        XLOGE("setTxPowerEnabledCommand failed");
    } else {
        XLOGD("setTxPowerEnabledCommand succeed");
        result = JNI_TRUE;
    }
    close(skfd);
    return result;
}

static jboolean android_net_wifi_setTxPowerCommand(JNIEnv* env, jobject clazz, jint offset)
{
    jboolean result = JNI_FALSE;
    struct iwreq wrq;
    int skfd;
    int32_t au4TxPwr[4] = {0, 0, 0, 0};
    au4TxPwr[3] = offset;
    
    /* initialize socket */
    skfd = socket(PF_INET, SOCK_DGRAM, 0);
    if (skfd < 0) {
        XLOGE("Open socket failed");
        return result;
    }
    
    /* initliaze WEXT request */
    wrq.u.data.pointer = &(au4TxPwr[0]);
    wrq.u.data.length = 4;
    
    wrq.u.data.flags = PRIV_CMD_SET_TX_POWER;
    strncpy(wrq.ifr_name, "wlan0", IFNAMSIZ);
    
    /* do IOCTL */
    if (ioctl(skfd, IOCTL_SET_INTS, &wrq) < 0) {
        XLOGE("setTxPowerCommand failed");
    } else {
        XLOGD("setTxPowerCommand succeed");
        result = JNI_TRUE;
    }
    close(skfd);
    return result;
}

// ----------------------------------------------------------------------------

/*
 * JNI registration.
 */
static JNINativeMethod gWifiMethods[] = {
    /* name, signature, funcPtr */

    { "loadDriver", "()Z",  (void *)android_net_wifi_loadDriver },
    { "isDriverLoaded", "()Z",  (void *)android_net_wifi_isDriverLoaded},
    { "unloadDriver", "()Z",  (void *)android_net_wifi_unloadDriver },
    { "startSupplicant", "()Z",  (void *)android_net_wifi_startSupplicant },
    { "startP2pSupplicant", "()Z",  (void *)android_net_wifi_startP2pSupplicant },
    { "stopSupplicant", "()Z", (void*) android_net_wifi_stopSupplicant },
    { "killSupplicant", "()Z",  (void *)android_net_wifi_killSupplicant },
    { "connectToSupplicant", "(Z)Z",  (void *)android_net_wifi_connectToSupplicant },
    { "closeSupplicantConnection", "()V",  (void *)android_net_wifi_closeSupplicantConnection },

    { "listNetworksCommand", "()Ljava/lang/String;",
        (void*) android_net_wifi_listNetworksCommand },
    { "addNetworkCommand", "()I", (void*) android_net_wifi_addNetworkCommand },
    { "setNetworkVariableCommand", "(ILjava/lang/String;Ljava/lang/String;)Z",
        (void*) android_net_wifi_setNetworkVariableCommand },
    { "getNetworkVariableCommand", "(ILjava/lang/String;)Ljava/lang/String;",
        (void*) android_net_wifi_getNetworkVariableCommand },
    { "removeNetworkCommand", "(I)Z", (void*) android_net_wifi_removeNetworkCommand },
    { "enableNetworkCommand", "(IZ)Z", (void*) android_net_wifi_enableNetworkCommand },
    { "disableNetworkCommand", "(I)Z", (void*) android_net_wifi_disableNetworkCommand },
    { "waitForEvent", "()Ljava/lang/String;", (void*) android_net_wifi_waitForEvent },
    { "statusCommand", "()Ljava/lang/String;", (void*) android_net_wifi_statusCommand },
    { "scanResultsCommand", "()Ljava/lang/String;", (void*) android_net_wifi_scanResultsCommand },
    { "pingCommand", "()Z",  (void *)android_net_wifi_pingCommand },
    { "disconnectCommand", "()Z",  (void *)android_net_wifi_disconnectCommand },
    { "reconnectCommand", "()Z",  (void *)android_net_wifi_reconnectCommand },
    { "reassociateCommand", "()Z",  (void *)android_net_wifi_reassociateCommand },
    { "scanCommand", "(Z)Z", (void*) android_net_wifi_scanCommand },
    { "setScanModeCommand", "(Z)Z", (void*) android_net_wifi_setScanModeCommand },
    { "startDriverCommand", "()Z", (void*) android_net_wifi_startDriverCommand },
    { "stopDriverCommand", "()Z", (void*) android_net_wifi_stopDriverCommand },
    { "startFilteringMulticastV4Packets", "()Z", (void*) android_net_wifi_startMultiV4Filtering},
    { "stopFilteringMulticastV4Packets", "()Z", (void*) android_net_wifi_stopMultiV4Filtering},
    { "startFilteringMulticastV6Packets", "()Z", (void*) android_net_wifi_startMultiV6Filtering},
    { "stopFilteringMulticastV6Packets", "()Z", (void*) android_net_wifi_stopMultiV6Filtering},
    { "setPowerModeCommand", "(I)Z", (void*) android_net_wifi_setPowerModeCommand },
    { "getPowerModeCommand", "()I", (void*) android_net_wifi_getPowerModeCommand },
    { "setBandCommand", "(I)Z", (void*) android_net_wifi_setBandCommand},
    { "getBandCommand", "()I", (void*) android_net_wifi_getBandCommand},
    { "setBluetoothCoexistenceModeCommand", "(I)Z",
    		(void*) android_net_wifi_setBluetoothCoexistenceModeCommand },
    { "setBluetoothCoexistenceScanModeCommand", "(Z)Z",
    		(void*) android_net_wifi_setBluetoothCoexistenceScanModeCommand },
    { "getMacAddressCommand", "()Ljava/lang/String;", (void*) android_net_wifi_getMacAddressCommand },
    { "saveConfigCommand", "()Z", (void*) android_net_wifi_saveConfigCommand },
    { "reloadConfigCommand", "()Z", (void*) android_net_wifi_reloadConfigCommand },
    { "setScanResultHandlingCommand", "(I)Z", (void*) android_net_wifi_setScanResultHandlingCommand },
    { "addToBlacklistCommand", "(Ljava/lang/String;)Z", (void*) android_net_wifi_addToBlacklistCommand },
    { "clearBlacklistCommand", "()Z", (void*) android_net_wifi_clearBlacklistCommand },
    { "startWpsPbcCommand", "(Ljava/lang/String;)Z", (void*) android_net_wifi_wpsPbcCommand },
    { "startWpsWithPinFromAccessPointCommand", "(Ljava/lang/String;Ljava/lang/String;)Z",
        (void*) android_net_wifi_wpsPinFromAccessPointCommand },
    { "startWpsWithPinFromDeviceCommand", "(Ljava/lang/String;)Ljava/lang/String;",
        (void*) android_net_wifi_wpsPinFromDeviceCommand },
    { "setSuspendOptimizationsCommand", "(Z)Z",
        (void*) android_net_wifi_setSuspendOptimizationsCommand},
    { "setCountryCodeCommand", "(Ljava/lang/String;)Z",
        (void*) android_net_wifi_setCountryCodeCommand},
    { "enableBackgroundScanCommand", "(Z)V", (void*) android_net_wifi_enableBackgroundScanCommand},
    { "setScanIntervalCommand", "(I)V", (void*) android_net_wifi_setScanIntervalCommand},
    { "doBooleanCommand", "(Ljava/lang/String;)Z", (void*) android_net_wifi_doBooleanCommand},
    { "doIntCommand", "(Ljava/lang/String;)I", (void*) android_net_wifi_doIntCommand},
    { "doStringCommand", "(Ljava/lang/String;)Ljava/lang/String;", (void*) android_net_wifi_doStringCommand},
    
    /* MTK proprietary : added by mtk03034 */
    { "doWpsPinCommand", "(Ljava/lang/String;)Ljava/lang/String;", (void*) android_net_wifi_wpsPinCommand },
    { "doWpsPbcCommand", "(Ljava/lang/String;)Z", (void*) android_net_wifi_mtk_wpsPbcCommand },
    { "doWpsAbortCommand", "()Z", (void*) android_net_wifi_wpsAbortCommand },

    /* CTIA test : added by mtk03034 */
    { "doCTIATestOnCommand", "()Z", (void*) android_net_wifi_CTIATestOnCommand },
    { "doCTIATestOffCommand", "()Z", (void*) android_net_wifi_CTIATestOffCommand },
    { "doCTIATestRateCommand", "(I)Z", (void*) android_net_wifi_CTIATestRateCommand },
    { "doCTIATestPowerCommand", "(I)Z", (void*) android_net_wifi_CTIATestPowerCommand },
    { "doCTIATestSetCommand", "(II)Z", (void*) android_net_wifi_CTIATestSetCommand },
    { "doCTIATestGetCommand", "(I)Ljava/lang/String;", (void*) android_net_wifi_CTIATestGetCommand },

    //MTK_OP01_PROTECT_START
    { "saveAPPriorityCommand", "()Z", (void*) android_net_wifi_saveAPPriorityCommand },
    //MTK_OP01_PROTECT_END
    
    { "setTxPowerEnabledCommand", "(Z)Z", (void*) android_net_wifi_setTxPowerEnabledCommand },
    { "setTxPowerCommand", "(I)Z", (void*) android_net_wifi_setTxPowerCommand },
};

int register_android_net_wifi_WifiManager(JNIEnv* env)
{
    jclass wifi = env->FindClass(WIFI_PKG_NAME);
    LOG_FATAL_IF(wifi == NULL, "Unable to find class " WIFI_PKG_NAME);
    cached_WifiNative_notifySupplicantHung = env->GetMethodID(wifi, "notifySupplicantHung","()V");
    
    return AndroidRuntime::registerNativeMethods(env,
            WIFI_PKG_NAME, gWifiMethods, NELEM(gWifiMethods));
}

}; // namespace android
