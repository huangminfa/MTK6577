/*
 * Copyright (c) 2007-2012 Inside Secure, All Rights Reserved.
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

#include <errno.h>
#include <pthread.h>
#include <semaphore.h>
#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <sys/queue.h>

#include "com_android_nfc.h"

#define ERROR_BUFFER_TOO_SMALL       -12
#define ERROR_INSUFFICIENT_RESOURCES -9


/* FOR READ TAG */
#define MAX_OPENNFC_TECH_LENGTH  					0x0FU
#define MAX_LIB_TECH_LENGTH  						0x0FU
#define MAX_JAVA_TECH_LENGTH      					0x0FU

#define MAX_UID_LENGTH            					0x0FU
#define MAX_ATR_LENGTH            					0x30U
#define ATQA_LENGTH           					    0x02U
#define ATQB_LENGTH           					    0x0BU
#define APP_DATA_B_LENGTH      					    0x04U
#define PROT_INFO_B_LENGTH      					0x03U
#define FEL_PM_LEN									0x08U
#define FEL_SYS_CODE_LEN       					    0x02U

#define NDEF_MODE_READ_ONLY            				  1
#define NDEF_MODE_READ_WRITE            			  2
#define NDEF_MODE_UNKNOWN                			  3

#define STATUS_SUCCESS                    		    0
#define STATUS_DESELECTED                           1

#define	MAXIMUM_READ_NDEF_BUFFER_SIZE			1024*512

#define MAX(a,b) ((a) > (b) ? (a) : (b))
#define MIN(a,b) ((a) < (b) ? (a) : (b))

/* name of the file with firmware binary */
#define FW_FILENAME "/vendor/firmware//microread_fw.bin"

static jmethodID cached_NfcManager_notifyNdefMessageListeners;
static jmethodID cached_NfcManager_notifyTransactionListeners;
jmethodID cached_NfcManager_notifyLlcpLinkActivation;
jmethodID cached_NfcManager_notifyLlcpLinkDeactivated;
static jmethodID cached_NfcManager_notifyTargetDeselected;

static jmethodID cached_NfcManager_notifySeFieldActivated;
static jmethodID cached_NfcManager_notifySeFieldDeactivated;

static jmethodID cached_NfcManager_notifySeApduReceived;
static jmethodID cached_NfcManager_notifySeMifareAccess;
static jmethodID cached_NfcManager_notifySeEmvCardRemoval;

jobject genericCached_P2pDevice;
jobject genericManager;

namespace android {

/* FOR READ TAG */
typedef struct infoPerTag
{
    uint8_t         AppData[MAX_ATR_LENGTH];
    uint8_t         MaxDataRate;
    uint8_t         Afi;
	/** tech Type **/
	uint8_t 		openNfcTechType[MAX_OPENNFC_TECH_LENGTH];
	uint8_t			nb_openNfcTechType;
	uint8_t 		libNfcTechType[MAX_LIB_TECH_LENGTH];
	uint32_t 		libNfcTechHandle[MAX_LIB_TECH_LENGTH];
	uint8_t			nb_libNfcTechType;
	uint8_t 		javaNfcTechType[MAX_JAVA_TECH_LENGTH];
	uint8_t			nb_javaNfcTechType;
	/** UID **/
    uint8_t         Uid[MAX_UID_LENGTH];
    uint8_t         UidLength;
    /** 14443A **/
    uint8_t         AppDataLength;
    uint8_t         Sak;
    uint8_t         AtqA[ATQA_LENGTH];

    uint8_t         Fwi_Sfgt;
    /** 14443B **/
    uint8_t         ProtInfo[PROT_INFO_B_LENGTH];
    uint8_t         HiLayerResp[MAX_ATR_LENGTH];
    uint8_t         HiLayerRespLength;
    /** Jewel **/
    uint8_t         HeaderRom0;
    uint8_t         HeaderRom1;
    /** Felica **/
    uint8_t     	PMm[FEL_PM_LEN];
    uint8_t     	SystemCode[FEL_SYS_CODE_LEN];
    /** 15693 **/
    uint8_t         Dsfid;
    uint8_t         Flags;

} infoPerTag;

typedef struct ndefMessage
{
    uint8_t             buffer[MAXIMUM_READ_NDEF_BUFFER_SIZE];
    uint32_t            length;
} ndefMessage;

typedef struct cardDetectionParameters
{
	W_ERROR wCardDetectionError;
	W_ERROR removalError;
	W_HANDLE hConnection;
	nfc_jni_native_data *nat;

} cardDetectionParameters;

/* for transceive priority issue */
techListForTransceive tTransceive;
/**Indicates that the client Open NFC is already started*/
static bool_t clientOpenNFCAlreadyStarted = W_FALSE;
/**Indicates if we are already register for error reading management*/
static bool_t alreadyRegisterForErrorManagement = W_FALSE;
/**Open NFC version*/
static const char16_t VERSION_OPEN_NFC_TARGET[] ={ '4', '.', '4', 0 };

/**Thread ID for pumping events*/
static pthread_t threadWBasicExecuteEventLoop;
/**Handle for be able cancel the fact to be alert when an unknown card appear*/
static W_HANDLE handleUnknownCardRegistry;
/**Handle for be able cancel the fact to be alert when a collision happen*/
static W_HANDLE handleCollisionRegistry;
/**Handle for be able cancel the card detection*/
static W_HANDLE handleCardEventRegistry;
/**Semaphore for waiting end of operation*/
static sem_t semEndOperation;

extern uint32_t createServerSocketLinkedHandle(char16_t * serviceName, uint32_t serviceNameLength, uint8_t sap);
extern uint32_t createSocketLinkedHandle(uint8_t sap);
extern void initP2PSockets();

/**
 * Pump the events in separate thread
 */
void * threadedWBasicExecuteEventLoop(void * parameter)
{
	LogInformation("Launch threadedWBasicExecuteEventLoop");

	JavaVM * javaVM = getJavaVM();
	JNIEnv * jniEnvironment=NULL;


	javaVM->GetEnv( (void **)&jniEnvironment, JNI_VERSION_1_6);

	bool attached = false;
	if (jniEnvironment == NULL)
	{
		attached = true;
		JavaVMAttachArgs thread_args;
		thread_args.name = "threadedWBasicExecuteEventLoop";
		thread_args.version = JNI_VERSION_1_6;
		thread_args.group = NULL;
		javaVM->AttachCurrentThread(&jniEnvironment, &thread_args);
	}

	WBasicExecuteEventLoop();

	if (attached == true)
	{
		javaVM->DetachCurrentThread();
	}

	return NULL;
}

/**
 * Initialize the client, if need
 * @return Status of initialization
 */
static W_ERROR intializeClient()
{
	  LogInformation("intializeClient");

	if(clientOpenNFCAlreadyStarted==W_TRUE)
	{
		return W_SUCCESS;
	}

	W_ERROR error = WBasicInit(VERSION_OPEN_NFC_TARGET);

	if(error!=W_SUCCESS)
	{
		LogError("WBasicInit failed wit error : %x",error);

		return error;
	}

	if (pthread_create(&threadWBasicExecuteEventLoop, NULL, threadedWBasicExecuteEventLoop, NULL) != 0)
	{
		LogError("Can't launch thread for WBasicExecuteEventLoop");
		return W_ERROR_PROGRAMMING;
	}

#ifdef EXTENDED_OPEN_NFC
	startOpenNfcExtServer();
#endif

	clientOpenNFCAlreadyStarted = W_TRUE;

	return W_SUCCESS;
}

/**
 * Called by Open NFC stack when an unknown card is present
 * @param	pHandlerParameter	Blind parameter
 * @param	nEventCode			Code event
 */
static void UnknownCardDetectionHandler(void *pHandlerParameter, uint32_t nEventCode)
{
	  LogInformation("There is no listener for this tag.\n");
}

/**
 * Called by Open NFC stack when a collision happen
 * @param	pHandlerParameter	Blind parameter
 * @param	nEventCode			Code event
 */
static void ListenToCollisionHandler(void *pHandlerParameter, uint32_t nEventCode)
{
	  LogInformation("Several cards are detected, please present only one.\n");
}

/**
 * Initialize Open NFC stack client and register it for be alert in case of error
 */
static W_ERROR registerErrorEvents()
{
	  LogInformation("registerErrorEvents");

	if(alreadyRegisterForErrorManagement==W_TRUE)
	{
		return W_SUCCESS;
	}

	W_ERROR error = intializeClient();

	if(error!=W_SUCCESS)
	{
		LogError("Initialization failed error : %x",error);

		return error;
	}

	if (WReaderErrorEventRegister(UnknownCardDetectionHandler, null, W_READER_ERROR_UNKNOWN, W_FALSE,
			&handleUnknownCardRegistry)!= W_SUCCESS)
	{
		LogError("Error: Cannot register the unknown card handler function.\n");
		/* Just Warn: this may happen when starting several instances of the program */
	}

	  LogInformation("WReaderErrorEventRegister OK.\n");
	if (WReaderErrorEventRegister(ListenToCollisionHandler, null, W_READER_ERROR_COLLISION, W_FALSE,
			&handleCollisionRegistry) != W_SUCCESS)
	{
		LogError( "Error: Cannot register the card collision handler function.\n");
		/* Just Warn: this may happen when starting several instances of the program */
	}

	alreadyRegisterForErrorManagement = W_TRUE;

	return W_SUCCESS;
}

/**
 * Change the stand by mode state
 * @param	enable	New stand by mode status
 */
void setStandByMode(bool_t enable)
{
	W_ERROR error = WNFCControllerSetRFLockSync(W_NFCC_STORAGE_VOLATILE, enable, enable);
	error = WNFCControllerSwitchStandbyMode(enable);
	  LogInformation("setStandByMode : enable=%d => WNFCControllerSwitchStandbyMode : %x", enable, error);
}



bool isFirstCheck;
bool presentTag;

bool isNdefCompliant;
bool isJewelExist;
bool isMifareDesfire;

bool isCheckNdefMessageOK;
int tagAccessStatus;
int maxNdefMessageLength;
uint32_t freeSpaceSize;
sem_t semEndCardDetection;
W_HANDLE removalHandle;

static infoPerTag * INFO_PER_TAG;
struct cardDetectionParameters *CDP;
ndefMessage * dataNdefMessages;

void  initInfoPerTagStructure(){
	INFO_PER_TAG = (infoPerTag *) malloc(sizeof(infoPerTag));
	INFO_PER_TAG->nb_libNfcTechType = 0;
	CDP = (cardDetectionParameters *) malloc(sizeof(cardDetectionParameters));
	dataNdefMessages = (ndefMessage *) malloc(sizeof(ndefMessage));
	return;
}

void closeInfoPerTagStructure()
{
	LogInformation("closeInfoPerTagStructure");
	free(INFO_PER_TAG);
	free(CDP);
	free(dataNdefMessages);
}

void CloseRemovalHandle(){
    WBasicCloseHandle(removalHandle);
}

void CloseConnectionHandle(){
    WBasicCloseHandle(CDP->hConnection);
/*
	LogInformation("CloseConnectionHandle(): WReaderHandlerWorkPerformed(): hConnection=0x%X", CDP->hConnection);
	WReaderHandlerWorkPerformed(CDP->hConnection, W_FALSE, W_TRUE);
*/
}

void setInitialTag(){
	presentTag = false;
}

bool get_isCheckNdefMessageOK(){
	return  isCheckNdefMessageOK;
}

bool get_isMifareDesfire(){
	return isMifareDesfire;
}

void set_isCheckNdefMessageOK(bool CheckNdefMessageOK){
	isCheckNdefMessageOK = CheckNdefMessageOK;
	isNdefCompliant = true;
}

int get_tagAccessStatus(){
	return tagAccessStatus;
}

int get_maxNdefMessageLength(){
	return maxNdefMessageLength;
}

bool get_presentTag(){
	return  presentTag;
}

techListForTransceive get_tTransceive(){
	return tTransceive;
}

uint8_t * getNdefMessagebuffer(){
	return dataNdefMessages->buffer;
}

uint32_t getNdefMessageLength(){
	return dataNdefMessages->length;
}

bool isTechPresent (uint8_t tech){

	bool result = false;
	int i;
	if(INFO_PER_TAG->nb_openNfcTechType > 0){
		for (i=0; i<INFO_PER_TAG->nb_openNfcTechType; i++){
			if (INFO_PER_TAG->libNfcTechType[i] == tech){
				result = true;
				break;
			}
		}
	}
	return result;
}

/*
 * Convert between OpenNFc and PN544 type
 * return phNfc_eRFDevType_t
 */
phNfc_eRFDevType_t convert_DevType(int devType, int type)
{
	phNfc_eRFDevType_t phNfc_eRFDevType = phNfc_eUnknown_DevType;
	  LogInformation("convert_DevType - devType: %d\n", devType);
	switch (devType)
	{
	case W_PROP_ISO_14443_3_A:
		phNfc_eRFDevType = phNfc_eISO14443_3A_PICC;
		break;
	case W_PROP_ISO_14443_4_A:
		phNfc_eRFDevType = phNfc_eISO14443_4A_PICC;
		break;
	case W_PROP_ISO_14443_3_B:
		phNfc_eRFDevType = phNfc_eISO14443_B_PICC;
		break;
	case W_PROP_ISO_14443_4_B:
		phNfc_eRFDevType = phNfc_eISO14443_4B_PICC;
		break;
	case W_PROP_ISO_15693_3:
		phNfc_eRFDevType = phNfc_eISO15693_PICC;
		break;
	case W_PROP_ISO_15693_2:
		phNfc_eRFDevType = phNfc_eISO15693_PICC;
		break;
	case W_PROP_ISO_7816_4:
		 if (type == 0){
			 phNfc_eRFDevType = phNfc_eISO14443_3A_PICC;
		 }else if (type == 1){
			 phNfc_eRFDevType = phNfc_eISO14443_B_PICC;
		 } else{
		 }
		break;
	case W_PROP_BPRIME:
		phNfc_eRFDevType = phNfc_eUnknown_DevType;
		break;
	case W_PROP_NFC_TAG_TYPE_1:
		phNfc_eRFDevType = phNfc_eJewel_PICC;
		break;
	case W_PROP_NFC_TAG_TYPE_2:
		phNfc_eRFDevType = phNfc_eISO14443_3A_PICC;
		break;
	case W_PROP_NFC_TAG_TYPE_3:
		phNfc_eRFDevType = phNfc_eFelica_PICC;
		break;
	case W_PROP_NFC_TAG_TYPE_4_A:
		phNfc_eRFDevType = phNfc_eISO14443_4A_PICC;
		break;
	case W_PROP_NFC_TAG_TYPE_4_B:
		phNfc_eRFDevType = phNfc_eISO14443_4B_PICC;
		break;
	case W_PROP_NFC_TAG_TYPE_5:
		phNfc_eRFDevType = phNfc_eISO14443_B_PICC;
		break;
	case W_PROP_NFC_TAG_TYPE_6:
		phNfc_eRFDevType = phNfc_eISO15693_PICC;
		break;
	case W_PROP_TYPE1_CHIP:
		phNfc_eRFDevType = phNfc_eJewel_PICC;
		break;
	case W_PROP_JEWEL:
		phNfc_eRFDevType = phNfc_eJewel_PICC;
		break;
	case W_PROP_TOPAZ:
		phNfc_eRFDevType = phNfc_eJewel_PICC;
		break;
	case W_PROP_TOPAZ_512:
		phNfc_eRFDevType = phNfc_eJewel_PICC;
		break;
	case W_PROP_FELICA:
		phNfc_eRFDevType = phNfc_eFelica_PICC;
		break;
	case W_PROP_PICOPASS_2K:
		phNfc_eRFDevType = phNfc_eISO14443_B_PICC;
		break;
	case W_PROP_PICOPASS_32K:
		phNfc_eRFDevType = phNfc_eISO14443_B_PICC;
		break;
	case W_PROP_ICLASS_2K:
		phNfc_eRFDevType = phNfc_eISO15693_PICC;
		break;
	case W_PROP_ICLASS_16K:
		phNfc_eRFDevType = phNfc_eISO15693_PICC;
		break;
	case W_PROP_MIFARE_UL:
		phNfc_eRFDevType = phNfc_eMifare_PICC;
		break;
	case W_PROP_MIFARE_UL_C:
		phNfc_eRFDevType = phNfc_eMifare_PICC;
		break;
	case W_PROP_MIFARE_MINI:
		phNfc_eRFDevType = phNfc_eMifare_PICC;
		break;
	case W_PROP_MIFARE_1K:
		phNfc_eRFDevType = phNfc_eMifare_PICC;
		break;
	case W_PROP_MIFARE_4K:
		phNfc_eRFDevType = phNfc_eMifare_PICC;
		break;
	case W_PROP_MIFARE_DESFIRE_D40:
		phNfc_eRFDevType = phNfc_eISO14443_4A_PICC;
		break;
	case W_PROP_MIFARE_DESFIRE_EV1_2K:
		phNfc_eRFDevType = phNfc_eISO14443_4A_PICC;
		break;
	case W_PROP_MIFARE_DESFIRE_EV1_4K:
		phNfc_eRFDevType = phNfc_eISO14443_4A_PICC;
		break;
	case W_PROP_MIFARE_DESFIRE_EV1_8K:
		phNfc_eRFDevType = phNfc_eISO14443_4A_PICC;
		break;
	case W_PROP_MIFARE_PLUS_X_2K:
		phNfc_eRFDevType = phNfc_eISO14443_4A_PICC;
		break;
	case W_PROP_MIFARE_PLUS_X_4K:
		phNfc_eRFDevType = phNfc_eISO14443_4A_PICC;
		break;
	case W_PROP_MIFARE_PLUS_S_2K:
		phNfc_eRFDevType = phNfc_eISO14443_4A_PICC;
		break;
	case W_PROP_MIFARE_PLUS_S_4K:
		phNfc_eRFDevType = phNfc_eISO14443_4A_PICC;
		break;
	case W_PROP_TI_TAGIT:
		phNfc_eRFDevType = phNfc_eISO15693_PICC;
		break;
	case W_PROP_ST_LRI_512:
		phNfc_eRFDevType = phNfc_eISO15693_PICC;
		break;
	case W_PROP_ST_LRI_2K:
		phNfc_eRFDevType = phNfc_eISO15693_PICC;
		break;
	case W_PROP_NXP_ICODE:
		phNfc_eRFDevType = phNfc_eISO15693_PICC;
		break;
	case W_PROP_MY_D_MOVE:
		phNfc_eRFDevType = phNfc_eISO14443_3A_PICC;
		break;
	case W_PROP_MY_D_NFC:
		phNfc_eRFDevType = phNfc_eISO14443_3A_PICC;
		break;
	case W_PROP_KOVIO:
		phNfc_eRFDevType = phNfc_eISO14443_3A_PICC;
		break;
	case W_PROP_SECURE_ELEMENT:
		phNfc_eRFDevType = phNfc_eUnknown_DevType;
		break;
	case W_PROP_VIRTUAL_TAG:
		phNfc_eRFDevType = phNfc_eUnknown_DevType;
		break;
	case W_PROP_P2P_TARGET:
		phNfc_eRFDevType = phNfc_eNfcIP1_Target;
		break;
	case W_PROP_ISO_7816_4_A:
		phNfc_eRFDevType = phNfc_eISO14443_3A_PICC;
		break;
	case W_PROP_ISO_7816_4_B:
		phNfc_eRFDevType = phNfc_eISO14443_B_PICC;
		break;
	case W_PROP_NFC_TAG_TYPE_2_GENERIC:
		phNfc_eRFDevType = phNfc_eISO14443_A_PICC;
		break;

	default:
		phNfc_eRFDevType = phNfc_eUnknown_DevType;
			break;
	}

	return phNfc_eRFDevType;
}


/*
 * Callback when tag removed
 *
 */
void tagRemovalListener(void *pHandlerParameter, uint32_t nEventCode)
{
	  LogInformation("removalListener: A tag has been removed.");
	setInitialTag();
}


/*
 * Get Tag Basic Info as access status and free space size
 *
 */
W_ERROR getTagBasicInfo(W_HANDLE hConnection)
{
	tNDEFTagInfo info;
	W_ERROR nError = WNDEFGetTagInfo(hConnection, &info);

	// Initial value
	tagAccessStatus = NDEF_MODE_READ_WRITE;
	freeSpaceSize = 0;
	if (nError == W_SUCCESS)
	{
		if (info.bIsLocked)
		{
			tagAccessStatus = NDEF_MODE_READ_ONLY;
		}
		else
		{
			tagAccessStatus = NDEF_MODE_READ_WRITE;
		}
		freeSpaceSize = info.nFreeSpaceSize;
		  LogInformation("CALL_BACK_REGISTERY-> freeSpaceSize = %d.   tagAccessStatus= %d", freeSpaceSize, tagAccessStatus);
	}
	return nError;
}


int addTechIfNeeded(int *techList, int* handleList, int* typeList, int listSize,
        int maxListSize, int techToAdd, int handleToAdd, int typeToAdd) {
    bool found = false;
    for (int i = 0; i < listSize; i++) {
        if (techList[i] == techToAdd) {
            found = true;
            break;
        }
    }
    if (!found && listSize < maxListSize) {
        techList[listSize] = techToAdd;
        handleList[listSize] = handleToAdd;
        typeList[listSize] = typeToAdd;
        return listSize + 1;
    }
    else {
        return listSize;
    }
}

/*
 *  Utility to get a technology tree and a corresponding handle list from a detected tag.
 */
void nfc_jni_get_technology_tree(JNIEnv* e, uint8_t count, jintArray* techList, jintArray* handleList,
        jintArray* libnfcTypeList)
{
   int technologies[MAX_LIB_TECH_LENGTH];
   int handles[MAX_LIB_TECH_LENGTH];
   int libnfctypes[MAX_LIB_TECH_LENGTH];
     LogInformation("nfc_jni_get_technology_tree: count =%d", count);
   int index = 0;

   for (int target = count - 1; target >= 0; target--) {
       int type = INFO_PER_TAG->libNfcTechType[target];
       int handle = INFO_PER_TAG->libNfcTechHandle[target];
       switch (type)
       {
          case phNfc_eISO14443_A_PICC:
          case phNfc_eISO14443_4A_PICC:
            {
              index = addTechIfNeeded(technologies, handles, libnfctypes, index,
                      MAX_LIB_TECH_LENGTH, TARGET_TYPE_ISO14443_4, handle, type);
              break;
            }
          case phNfc_eISO14443_4B_PICC:
            {
              index = addTechIfNeeded(technologies, handles, libnfctypes, index,
                      MAX_LIB_TECH_LENGTH, TARGET_TYPE_ISO14443_4, handle, type);
              index = addTechIfNeeded(technologies, handles, libnfctypes, index,
                      MAX_LIB_TECH_LENGTH, TARGET_TYPE_ISO14443_3B, handle, type);
            }break;
          case phNfc_eISO14443_3A_PICC:
            {
              index = addTechIfNeeded(technologies, handles, libnfctypes,
                      index, MAX_LIB_TECH_LENGTH, TARGET_TYPE_ISO14443_3A, handle, type);
            }break;
          case phNfc_eISO14443_B_PICC:
            {
              index = addTechIfNeeded(technologies, handles, libnfctypes,
                      index, MAX_LIB_TECH_LENGTH, TARGET_TYPE_ISO14443_4, handle, type);
              index = addTechIfNeeded(technologies, handles, libnfctypes,
                      index, MAX_LIB_TECH_LENGTH, TARGET_TYPE_ISO14443_3B, handle, type);
            }break;
          case phNfc_eISO15693_PICC:
            {
              index = addTechIfNeeded(technologies, handles, libnfctypes,
                      index, MAX_LIB_TECH_LENGTH, TARGET_TYPE_ISO15693, handle, type);
            }break;
          case phNfc_eMifare_PICC:
            {
              int sak = INFO_PER_TAG->Sak;
              switch(sak)
              {
                case 0x00:
                  index = addTechIfNeeded(technologies, handles, libnfctypes,
                          index, MAX_LIB_TECH_LENGTH, TARGET_TYPE_MIFARE_UL, handle, type);
                  break;
                default:
                  index = addTechIfNeeded(technologies, handles, libnfctypes,
                          index, MAX_LIB_TECH_LENGTH, TARGET_TYPE_MIFARE_CLASSIC, handle, type);
                  break;
              }
            }break;
          case phNfc_eFelica_PICC:
            {
              index = addTechIfNeeded(technologies, handles, libnfctypes,
                      index, MAX_LIB_TECH_LENGTH, TARGET_TYPE_FELICA, handle, type);
            }break;
          case phNfc_eJewel_PICC:
            {
              index = addTechIfNeeded(technologies, handles, libnfctypes,
                      index, MAX_LIB_TECH_LENGTH, TARGET_TYPE_ISO14443_3A, handle, type);
            }break;
          default:
            {
              index = addTechIfNeeded(technologies, handles, libnfctypes,
                      index, MAX_LIB_TECH_LENGTH, TARGET_TYPE_UNKNOWN, handle, type);
            }
        }
   }
     LogInformation("nfc_jni_get_technology_tree: index =%d", index);
   // Build the Java arrays
   if (techList != NULL) {
       *techList = e->NewIntArray(index);
       e->SetIntArrayRegion(*techList, 0, index, technologies);
   }

   if (handleList != NULL) {
       *handleList = e->NewIntArray(index);
       e->SetIntArrayRegion(*handleList, 0, index, handles);
   }

   if (libnfcTypeList != NULL) {
       *libnfcTypeList = e->NewIntArray(index);
       e->SetIntArrayRegion(*libnfcTypeList, 0, index, libnfctypes);
   }
}

/*
 *  Utility to recover poll bytes from target infos
 */
static void set_target_pollBytes(JNIEnv *e, jobject tag, jintArray techList)
{
    jclass tag_cls = e->GetObjectClass(tag);
    jfieldID f = e->GetFieldID(tag_cls, "mTechPollBytes", "[[B");

    jobjectArray existingPollBytes = (jobjectArray) e->GetObjectField(tag, f);

    if (existingPollBytes != NULL) {
        return;
    }
      LogInformation("set_target_pollBytes");

    jint *techId = e->GetIntArrayElements(techList, 0);
    int techListLength = e->GetArrayLength(techList);

    jbyteArray pollBytes = e->NewByteArray(0);
    jobjectArray techPollBytes = e->NewObjectArray(techListLength,
            e->GetObjectClass(pollBytes), 0);
    e->DeleteLocalRef(pollBytes);
      LogInformation("set_target_pollBytes: object set");
    for (int tech = 0; tech < techListLength; tech++) {
        switch(techId[tech])
        {
            /* ISO14443-3A: ATQA/SENS_RES */
            case TARGET_TYPE_ISO14443_3A:
            	//if (psRemoteDevInfo->RemDevType == phNfc_eJewel_PICC) {
                if (isTechPresent (phNfc_eJewel_PICC)) {
                    // Jewel ATQA is not read and stored by the PN544, but it is fixed
                    // at {0x00, 0x0C} in the spec. So eJewel can safely be
                    // translated to {0x00, 0x0C}.
                    const static jbyte JewelAtqA[2] = {0x00, 0x0C};
                    pollBytes = e->NewByteArray(2);
                    e->SetByteArrayRegion(pollBytes, 0, 2, (jbyte*) JewelAtqA);
                }
                else {
                    pollBytes = e->NewByteArray(ATQA_LENGTH);
                    e->SetByteArrayRegion(pollBytes, 0, ATQA_LENGTH, (jbyte *)INFO_PER_TAG->AtqA);
                }
                break;
            /* ISO14443-3B: Application data (4 bytes) and Protocol Info (3 bytes) from ATQB/SENSB_RES */
            case TARGET_TYPE_ISO14443_3B:
                pollBytes = e->NewByteArray(7);
                e->SetByteArrayRegion(pollBytes, 0, 4, (jbyte *)INFO_PER_TAG->AppData);
                e->SetByteArrayRegion(pollBytes, 4, 3, (jbyte *)INFO_PER_TAG->ProtInfo);
                break;
            /* JIS_X_6319_4: PAD0 (2 byte), PAD1 (2 byte), MRTI(2 byte), PAD2 (1 byte), RC (2 byte) */
            case TARGET_TYPE_FELICA:
                pollBytes = e->NewByteArray(10);
                e->SetByteArrayRegion(pollBytes, 0, 8, (jbyte *)INFO_PER_TAG->PMm);
                e->SetByteArrayRegion(pollBytes, 8, 2, (jbyte *)INFO_PER_TAG->SystemCode);
                break;
            /* ISO15693: response flags (1 byte), DSFID (1 byte) */
            case TARGET_TYPE_ISO15693:
                pollBytes = e->NewByteArray(2);
                e->SetByteArrayRegion(pollBytes, 0, 1,  (jbyte *)&INFO_PER_TAG->Flags);
                e->SetByteArrayRegion(pollBytes, 1, 1,  (jbyte *)&INFO_PER_TAG->Dsfid);
                break;
            default:
                pollBytes = e->NewByteArray(0);
                break;
        }
        e->SetObjectArrayElement(techPollBytes, tech, pollBytes);
        e->DeleteLocalRef(pollBytes);
    }
    e->SetObjectField(tag, f, techPollBytes);
    e->DeleteLocalRef(techPollBytes);
    e->DeleteLocalRef(tag_cls);
    e->DeleteLocalRef(existingPollBytes);
      LogInformation("set_target_pollBytes done");
}

/*
 *  Utility to recover activation bytes from target infos
 */
static void set_target_activationBytes(JNIEnv *e, jobject tag, jintArray techList)
{
    jclass tag_cls = e->GetObjectClass(tag);

    jfieldID f = e->GetFieldID(tag_cls, "mTechActBytes", "[[B");
    jobjectArray existingActBytes = (jobjectArray) e->GetObjectField(tag, f);
      LogInformation("set_target_activationBytes");
    if (existingActBytes != NULL) {
        return;
    }

    int techListLength = e->GetArrayLength(techList);
    jint *techId = e->GetIntArrayElements(techList, 0);
      LogInformation("set_target_activationBytes: techListLength = %d", techListLength);
    jbyteArray actBytes = e->NewByteArray(0);
    jobjectArray techActBytes = e->NewObjectArray(techListLength,
            e->GetObjectClass(actBytes), 0);
    e->DeleteLocalRef(actBytes);
      LogInformation("set_target_activationBytes techActBytes OK");
    for (int tech = 0; tech < techListLength; tech++) {
        switch(techId[tech]) {

            /* ISO14443-3A: SAK/SEL_RES */
            case TARGET_TYPE_ISO14443_3A:
                actBytes = e->NewByteArray(1);
                e->SetByteArrayRegion(actBytes, 0, 1, (jbyte *)&INFO_PER_TAG->Sak);
                break;
            /* ISO14443-3A & ISO14443-4: SAK/SEL_RES, historical bytes from ATS */
            /* ISO14443-3B & ISO14443-4: HiLayerResp */
            case TARGET_TYPE_ISO14443_4:
                // Determine whether -A or -B
                if (isTechPresent (phNfc_eISO14443_B_PICC) || isTechPresent (phNfc_eISO14443_4B_PICC)) {
                    actBytes = e->NewByteArray(INFO_PER_TAG->HiLayerRespLength);
                    e->SetByteArrayRegion(actBytes, 0, INFO_PER_TAG->HiLayerRespLength, (jbyte *)INFO_PER_TAG->HiLayerResp);
                }
                else if (isTechPresent (phNfc_eISO14443_A_PICC) || isTechPresent (phNfc_eISO14443_4A_PICC)) {
                    actBytes = e->NewByteArray(INFO_PER_TAG->AppDataLength);
                    e->SetByteArrayRegion(actBytes, 0, INFO_PER_TAG->AppDataLength, (jbyte *)INFO_PER_TAG->AppData);
                }
                break;
            /* ISO15693: response flags (1 byte), DSFID (1 byte) */
            case TARGET_TYPE_ISO15693:
                actBytes = e->NewByteArray(2);
                e->SetByteArrayRegion(actBytes, 0, 1,  (jbyte *)&INFO_PER_TAG->Flags);
                e->SetByteArrayRegion(actBytes, 1, 1,  (jbyte *)&INFO_PER_TAG->Dsfid);
                break;
            default:
                actBytes = e->NewByteArray(0);
                break;
        }
        e->SetObjectArrayElement(techActBytes, tech, actBytes);
        e->DeleteLocalRef(actBytes);
    }
    e->SetObjectField(tag, f, techActBytes);

//    e->DeleteLocalRef(actBytes);
    e->DeleteLocalRef(techActBytes);

    e->DeleteLocalRef(tag_cls);
    e->DeleteLocalRef(existingActBytes);

      LogInformation("set_target_activationBytes done");
}


/*
 * send tag UID to java
 *
 */
static void  transferToJavaData(JNIEnv *e, nfc_jni_native_data *nat, bool isSetUid, int status, W_HANDLE hConnection){

	jbyteArray tagUid;
	jobject tag;
	jfieldID f;
	jclass tag_cls = NULL;
	jmethodID ctor;
	  LogInformation("Enter transferToJavaData");
	if (status == W_ERROR_ITEM_NOT_FOUND){
		e->CallVoidMethod(nat->manager, cached_NfcManager_notifyTargetDeselected);
		return;
	}

	tag_cls = e->GetObjectClass(nat->cached_NfcTag);
	/* New tag instance */
	ctor = e->GetMethodID(tag_cls, "<init>", "()V");

	tag = e->NewObject(tag_cls, ctor);
	if(isSetUid){
	// Set tag UID
	f = e->GetFieldID(tag_cls, "mUid", "[B");
	tagUid = e->NewByteArray(INFO_PER_TAG->UidLength);
	if(INFO_PER_TAG->UidLength > 0)
	{
		e->SetByteArrayRegion(tagUid, 0, INFO_PER_TAG->UidLength, (jbyte *) INFO_PER_TAG->Uid);
	}
	e->SetObjectField(tag, f, tagUid);
	e->DeleteLocalRef(tagUid);
	}
	  LogInformation("transferToJavaData, prepare tech lists.");
	/* Generate technology list */
	jintArray techList;
	jintArray handleList;
	jintArray typeList;

	if(INFO_PER_TAG->nb_libNfcTechType > 0)
		nfc_jni_get_technology_tree(e, INFO_PER_TAG->nb_libNfcTechType, &techList, &handleList, &typeList);
	else{
		 techList = e->NewIntArray(0);
		 handleList = e->NewIntArray(0);
		 typeList = e->NewIntArray(0);
	}
	/* Push the technology list into the java object */
	f = e->GetFieldID(tag_cls, "mTechList", "[I");
	e->SetObjectField(tag, f, techList);

	f = e->GetFieldID(tag_cls, "mTechHandles", "[I");
	e->SetObjectField(tag, f, handleList);

	f = e->GetFieldID(tag_cls, "mTechLibNfcTypes", "[I");
	e->SetObjectField(tag, f, typeList);

	f = e->GetFieldID(tag_cls, "mConnectedTechIndex", "I");
	e->SetIntField(tag, f, (jint) 0);

	f = e->GetFieldID(tag_cls, "mConnectedHandle", "I");
	e->SetIntField(tag, f, (jint) hConnection);

	  LogInformation("transferToJavaData, tech lists filled");

	// Success, set poll & act bytes
	set_target_pollBytes(e, tag, techList);
	set_target_activationBytes(e, tag, techList);

	e->CallVoidMethod(nat->manager, cached_NfcManager_notifyNdefMessageListeners, tag);

	e->DeleteLocalRef(techList);
	e->DeleteLocalRef(handleList);
	e->DeleteLocalRef(typeList);
	e->DeleteLocalRef(tag);
	e->DeleteLocalRef(tag_cls);

	  LogInformation("transferToJavaData done");
}

/*
 * Check tag information as well as Ndef Message
 *
 */
void readNdefMessageCompleteListener(void* pCallbackParameter, W_HANDLE hMessage, W_ERROR nError)
{
	// Get Tag Information first
	  LogInformation("Enter readNdefMessageCompleteListener: this method allows get tag info as well as Ndef Message");
	W_ERROR cardDetectionError = CDP->wCardDetectionError;
	W_ERROR errRemoval = CDP->removalError;
	W_HANDLE hConnection = CDP->hConnection;
	JNIEnv *e = NULL;
	uint32_t nPropertyNumber;
	uint8_t* pConnectionList;
	uint32_t i;
	int typeAB = -1; // type A: 0.   type B: 1
	bool isSetUid = false;
	struct nfc_jni_native_data *nat;
	nat = CDP->nat;
	getJavaVM()->GetEnv((void **) &e, JNI_VERSION_1_6);

	  LogInformation("readNdefMessageCompleteListener : hConnection = %d", CDP->hConnection);

	if (cardDetectionError != W_SUCCESS)
	{
		  LogInformation("readNdefMessageCompleteListener : cardDetectionError happened %0xX", cardDetectionError);
		setInitialTag();
		WBasicCloseHandle(hConnection);
/*
		LogInformation("readNdefMessageCompleteListener: WReaderHandlerWorkPerformed(): hConnection=0x%X", hConnection);
		WReaderHandlerWorkPerformed(hConnection, W_FALSE, W_TRUE);
*/

		transferToJavaData(e, nat, isSetUid, W_ERROR_ITEM_NOT_FOUND, hConnection);
		return;
	}

	/* Check Tag information*/
	if (WBasicGetConnectionPropertyNumber(hConnection, &nPropertyNumber) == W_SUCCESS)
		{
			  LogInformation("readNdefMessageCompleteListener: nPropertyNumber: %d\n ", nPropertyNumber);
			pConnectionList = (uint8_t *) malloc(nPropertyNumber * sizeof(uint8_t));
			if (pConnectionList)
			{
				if (WBasicGetConnectionProperties(hConnection, pConnectionList, nPropertyNumber) == W_SUCCESS)
				{
					INFO_PER_TAG->nb_openNfcTechType = (uint8_t) nPropertyNumber;
					// Initialize nb_xNfcTechType to 0
					INFO_PER_TAG->nb_libNfcTechType = 0;
					INFO_PER_TAG->nb_javaNfcTechType = 0;
					tW14Part3ConnectionInfo s14Part3ConnectionInfo;
					tW14Part4ConnectionInfo s14Part4ConnectionInfo;
					tW15ConnectionInfo s15ConnectionInfo;
					tWFeliCaConnectionInfo sFeliCaConnectionInfo;
					tWType1ChipConnectionInfo sType1ChipConnectionInfo;
					tWMifareConnectionInfo sMifareConnectionInfo;
					tWMyDConnectionInfo sMyDConnectionInfo;
					phNfc_eRFDevType_t nxpType;
					// just find typeA or typeB
					for (i = 0; i < nPropertyNumber; i++)
					{
						if ((pConnectionList[i] == W_PROP_ISO_14443_3_A) || (pConnectionList[i] == W_PROP_ISO_14443_4_A))
						{
							typeAB = 0;
							break;
						}
						if ((pConnectionList[i] == W_PROP_ISO_14443_3_B) || (pConnectionList[i] == W_PROP_ISO_14443_4_B))
						{
							typeAB = 1;
							break;
						}
					}

					// big Loop starts
					for (i = 0; i < nPropertyNumber && INFO_PER_TAG->nb_libNfcTechType < MAX_LIB_TECH_LENGTH; i++)
					{
					   INFO_PER_TAG->openNfcTechType[i] = pConnectionList[i];
					   nxpType = convert_DevType(pConnectionList[i], typeAB);
					   if (nxpType == phNfc_eUnknown_DevType)
					   	   continue;
					     LogInformation("readNdefMessageCompleteListener nxpType: %d",nxpType);
					   switch (pConnectionList[i])
					   {
					     case W_PROP_ISO_14443_3_A:
							  if (tTransceive > T_W_PROP_ISO_14443_3_A)
								{
									tTransceive = T_W_PROP_ISO_14443_3_A;
								}
					    	  if (W14Part3GetConnectionInfo(hConnection, &s14Part3ConnectionInfo) == W_SUCCESS)
					    	    {
									if (s14Part3ConnectionInfo.sW14TypeA.nSAK == 0x40)
									{
										  LogInformation("readNdefMessageCompleteListener : p2p device is detected: hConnection=0x%X",
												hConnection);
										//setInitialTag();
//										WBasicCloseHandle(hConnection);

										LogInformation("readNdefMessageCompleteListener(): WReaderHandlerWorkPerformed(): hConnection=0x%X", hConnection);
										WReaderHandlerWorkPerformed(hConnection, W_FALSE, W_TRUE);


//										transferToJavaData(e, nat, isSetUid, W_ERROR_ITEM_NOT_FOUND, hConnection);
										return;
									}
					    		  INFO_PER_TAG->libNfcTechType[INFO_PER_TAG->nb_libNfcTechType] = nxpType;
					    		  INFO_PER_TAG->libNfcTechHandle[INFO_PER_TAG->nb_libNfcTechType] = hConnection;
					    		  INFO_PER_TAG->nb_libNfcTechType++;
					    		  memcpy(INFO_PER_TAG->Uid, s14Part3ConnectionInfo.sW14TypeA.aUID, 10);
					    		  INFO_PER_TAG->UidLength = s14Part3ConnectionInfo.sW14TypeA.nUIDLength;
					    		  isSetUid = true;
					    		  INFO_PER_TAG->Sak = s14Part3ConnectionInfo.sW14TypeA.nSAK;
					    		  INFO_PER_TAG->AtqA[0] =(uint8_t) (s14Part3ConnectionInfo.sW14TypeA.nATQA & 0x00FF);
					    		  INFO_PER_TAG->AtqA[1] =(uint8_t) ((s14Part3ConnectionInfo.sW14TypeA.nATQA & 0xFF00) >> 8);

					    	    }
					    	  break;
					     case W_PROP_ISO_7816_4_B:
							  if (tTransceive > T_W_PROP_ISO_7816_4_B)
								{
									tTransceive = T_W_PROP_ISO_7816_4_B;
								}
					     case W_PROP_ISO_14443_4_B:
							  if (tTransceive > T_W_PROP_ISO_14443_4_B)
								{
									tTransceive = T_W_PROP_ISO_14443_4_B;
								}
					     case W_PROP_ISO_14443_3_B:
							  if (tTransceive > T_W_PROP_ISO_14443_3_B)
								{
									tTransceive = T_W_PROP_ISO_14443_3_B;
								}
					    	 if (W14Part3GetConnectionInfo(hConnection, &s14Part3ConnectionInfo) == W_SUCCESS)
							{
					    		    INFO_PER_TAG->libNfcTechType[INFO_PER_TAG->nb_libNfcTechType] = nxpType;
					    		    INFO_PER_TAG->libNfcTechHandle[INFO_PER_TAG->nb_libNfcTechType] = hConnection;
					    		    INFO_PER_TAG->nb_libNfcTechType++;
									memcpy(INFO_PER_TAG->Uid, s14Part3ConnectionInfo.sW14TypeB.aATQB + 1, 4);
									INFO_PER_TAG->UidLength = 4;
									isSetUid = true;
									memcpy(INFO_PER_TAG->AppData, s14Part3ConnectionInfo.sW14TypeB.aATQB + 5, 4);
									memcpy(INFO_PER_TAG->ProtInfo, s14Part3ConnectionInfo.sW14TypeB.aATQB + 9, 3);
									memcpy(INFO_PER_TAG->HiLayerResp, s14Part3ConnectionInfo.sW14TypeB.aHigherLayerResponse,
											MIN(48,s14Part3ConnectionInfo.sW14TypeB.nHigherLayerResponseLength));
									INFO_PER_TAG->HiLayerRespLength = MIN(48,s14Part3ConnectionInfo.sW14TypeB.nHigherLayerResponseLength);
									INFO_PER_TAG->Afi = s14Part3ConnectionInfo.sW14TypeB.nAFI;
							}
					    	 break;

					     case W_PROP_ISO_7816_4_A:
							  if (tTransceive > T_W_PROP_ISO_7816_4_A)
								{
									tTransceive = T_W_PROP_ISO_7816_4_A;
								}
					     case W_PROP_ISO_14443_4_A:
							  if (tTransceive > T_W_PROP_ISO_14443_4_A)
								{
									tTransceive = T_W_PROP_ISO_14443_4_A;
								}
					    	 if (W14Part4GetConnectionInfo(hConnection, &s14Part4ConnectionInfo) == W_SUCCESS)
					    	 {
					    		INFO_PER_TAG->libNfcTechType[INFO_PER_TAG->nb_libNfcTechType] = nxpType;
					    		INFO_PER_TAG->libNfcTechHandle[INFO_PER_TAG->nb_libNfcTechType] = hConnection;
					    		INFO_PER_TAG->nb_libNfcTechType++;
					    		uint8_t * Uid14443_4_A = (uint8_t *) malloc(10);
					    		uint32_t id14443_4_BLength;
					    		WReaderGetIdentifier(hConnection, Uid14443_4_A, (uint32_t) 10, &id14443_4_BLength);
					    		memcpy(INFO_PER_TAG->Uid, Uid14443_4_A, id14443_4_BLength);
					    		INFO_PER_TAG->UidLength = id14443_4_BLength;
					    		isSetUid = true;
								memcpy(INFO_PER_TAG->AppData, s14Part4ConnectionInfo.sW14TypeA.aApplicationData,
										MIN(48,s14Part4ConnectionInfo.sW14TypeA.nApplicationDataLength));
								INFO_PER_TAG->AppDataLength = MIN(48,s14Part4ConnectionInfo.sW14TypeA.nApplicationDataLength);
								INFO_PER_TAG->MaxDataRate = s14Part4ConnectionInfo.sW14TypeA.nDataRateMaxDiv;
								INFO_PER_TAG->Fwi_Sfgt = s14Part4ConnectionInfo.sW14TypeA.nFWI_SFGI;
					    	 }

							 if (W14Part3GetConnectionInfo(hConnection, &s14Part3ConnectionInfo) == W_SUCCESS)
							 { // Check phNfcTypes.h
								      LogInformation( "W14Part3GetConnectionInfo: for W_PROP_ISO_7816_4_A");
									INFO_PER_TAG->Sak = s14Part3ConnectionInfo.sW14TypeA.nSAK;
									INFO_PER_TAG->AtqA[0] = (uint8_t) (s14Part3ConnectionInfo.sW14TypeA.nATQA & 0x00FF);
									INFO_PER_TAG->AtqA[1] = (uint8_t) ((s14Part3ConnectionInfo.sW14TypeA.nATQA & 0xFF00) >> 8);
							 }
							 break;

							case W_PROP_TI_TAGIT:
							case W_PROP_ST_LRI_512:
							case W_PROP_ST_LRI_2K:
							case W_PROP_NXP_ICODE:
							case W_PROP_ISO_15693_2:
							case W_PROP_ISO_15693_3:
								if (tTransceive > T_W_PROP_ISO_15693_3)
								{
									tTransceive = T_W_PROP_ISO_15693_3;
								}
								isNdefCompliant = true;
								if (W15GetConnectionInfo(hConnection, &s15ConnectionInfo) == W_SUCCESS)
								{
									INFO_PER_TAG->libNfcTechType[INFO_PER_TAG->nb_libNfcTechType] = nxpType;
					    		    INFO_PER_TAG->libNfcTechHandle[INFO_PER_TAG->nb_libNfcTechType] = hConnection;
					    		    INFO_PER_TAG->nb_libNfcTechType++;
									// invert the uid
									uint8_t temp;
									int middle = 4;
									int i, j;
									for (i = 0, j = 7; i < 4; i++, j--)
									{
										temp = s15ConnectionInfo.UID[i];
										s15ConnectionInfo.UID[i] = s15ConnectionInfo.UID[j];
										s15ConnectionInfo.UID[j] = temp;
									}
									memcpy(INFO_PER_TAG->Uid, s15ConnectionInfo.UID, 8);
									INFO_PER_TAG->UidLength = 8;
									isSetUid = true;
									INFO_PER_TAG->Dsfid = s15ConnectionInfo.nDSFID;
									INFO_PER_TAG->Flags = 0x00;
									INFO_PER_TAG->Afi = s15ConnectionInfo.nAFI;
								}
								break;

							case W_PROP_ISO_7816_4:
								  INFO_PER_TAG->libNfcTechType[INFO_PER_TAG->nb_libNfcTechType] = nxpType;
					    		  INFO_PER_TAG->libNfcTechHandle[INFO_PER_TAG->nb_libNfcTechType] = hConnection;
					    		  INFO_PER_TAG->nb_libNfcTechType++;
								if (typeAB == 0)
								{
									if (tTransceive > T_W_PROP_ISO_7816_4_A)
									{
										tTransceive = T_W_PROP_ISO_7816_4_A;
									}
									if (W14Part3GetConnectionInfo(hConnection, &s14Part3ConnectionInfo) == W_SUCCESS)
									{
										memcpy(INFO_PER_TAG->Uid, s14Part3ConnectionInfo.sW14TypeA.aUID, 10);
										INFO_PER_TAG->UidLength = s14Part3ConnectionInfo.sW14TypeA.nUIDLength;
										isSetUid = true;
										INFO_PER_TAG->AtqA[0] = (uint8_t) (s14Part3ConnectionInfo.sW14TypeA.nATQA & 0x00FF);
										INFO_PER_TAG->AtqA[1] = (uint8_t) ((s14Part3ConnectionInfo.sW14TypeA.nATQA & 0xFF00) >> 8);
										INFO_PER_TAG->Sak = s14Part3ConnectionInfo.sW14TypeA.nSAK;
									}
									if (W14Part4GetConnectionInfo(hConnection, &s14Part4ConnectionInfo) == W_SUCCESS)
									{
										memcpy(INFO_PER_TAG->AppData, s14Part4ConnectionInfo.sW14TypeA.aApplicationData,
												MIN(48,s14Part4ConnectionInfo.sW14TypeA.nApplicationDataLength));
										INFO_PER_TAG->AppDataLength = MIN(48,s14Part4ConnectionInfo.sW14TypeA.nApplicationDataLength);
										INFO_PER_TAG->MaxDataRate = s14Part4ConnectionInfo.sW14TypeA.nDataRateMaxDiv;
										INFO_PER_TAG->Fwi_Sfgt = s14Part4ConnectionInfo.sW14TypeA.nFWI_SFGI;
									}
								}
								else if (typeAB == 1)
								{
									if (tTransceive > T_W_PROP_ISO_7816_4_B)
									{
										tTransceive = T_W_PROP_ISO_7816_4_B;
									}

							    	 if (W14Part3GetConnectionInfo(hConnection, &s14Part3ConnectionInfo) == W_SUCCESS)
									{
											memcpy(INFO_PER_TAG->Uid, s14Part3ConnectionInfo.sW14TypeB.aATQB + 1, 4);
											INFO_PER_TAG->UidLength = 4;
											isSetUid = true;
											memcpy(INFO_PER_TAG->AppData, s14Part3ConnectionInfo.sW14TypeB.aATQB + 5, 4);
											memcpy(INFO_PER_TAG->ProtInfo, s14Part3ConnectionInfo.sW14TypeB.aATQB + 9, 3);
											memcpy(INFO_PER_TAG->HiLayerResp, s14Part3ConnectionInfo.sW14TypeB.aHigherLayerResponse,
													MIN(48,s14Part3ConnectionInfo.sW14TypeB.nHigherLayerResponseLength));
											INFO_PER_TAG->HiLayerRespLength = MIN(48,s14Part3ConnectionInfo.sW14TypeB.nHigherLayerResponseLength);
											INFO_PER_TAG->Afi = s14Part3ConnectionInfo.sW14TypeB.nAFI;
									}

								}
								break;

							case W_PROP_BPRIME:
								if (tTransceive > T_W_PROP_BPRIME)
								{
									tTransceive = T_W_PROP_BPRIME;
								}
								break;

							case W_PROP_NFC_TAG_TYPE_2:
							case W_PROP_NFC_TAG_TYPE_2_GENERIC:
								isNdefCompliant = true;
								break;

							case W_PROP_NFC_TAG_TYPE_3:
								if (tTransceive > T_W_PROP_NFC_TAG_TYPE_3)
								{
									tTransceive = T_W_PROP_NFC_TAG_TYPE_3;
								}
								isNdefCompliant = true;
							case W_PROP_FELICA:
								if (tTransceive > T_W_PROP_FELICA)
								{
									tTransceive = T_W_PROP_FELICA;
								}
								if (WFeliCaGetConnectionInfo(hConnection, &sFeliCaConnectionInfo) == W_SUCCESS)
								{
									INFO_PER_TAG->libNfcTechType[INFO_PER_TAG->nb_libNfcTechType] = nxpType;
					    		    INFO_PER_TAG->libNfcTechHandle[INFO_PER_TAG->nb_libNfcTechType] = hConnection;
					    		    INFO_PER_TAG->nb_libNfcTechType++;
									memcpy(INFO_PER_TAG->Uid, sFeliCaConnectionInfo.aManufacturerID, 8);
									INFO_PER_TAG->UidLength = 8;
									isSetUid = true;
									memcpy(INFO_PER_TAG->PMm,sFeliCaConnectionInfo.aManufacturerParameter, 8);
									// (opennfc:) uint16_t nSystemCode; =>   (NXP:) uint8_t     SystemCode[PHHAL_FEL_SYS_CODE_LEN];
									INFO_PER_TAG->SystemCode[0] = (uint8_t) (sFeliCaConnectionInfo.nSystemCode & 0x00FF);
									INFO_PER_TAG->SystemCode[1] = (uint8_t) ((sFeliCaConnectionInfo.nSystemCode & 0xFF00) >> 8);
								}
								break;

							case W_PROP_NFC_TAG_TYPE_4_A:
								if (tTransceive > T_W_PROP_NFC_TAG_TYPE_4_A)
								{
									tTransceive = T_W_PROP_NFC_TAG_TYPE_4_A;
								}
								isNdefCompliant = true;
								break;
							case W_PROP_NFC_TAG_TYPE_4_B:
								if (tTransceive > T_W_PROP_NFC_TAG_TYPE_4_B)
								{
									tTransceive = T_W_PROP_NFC_TAG_TYPE_4_B;
								}
								isNdefCompliant = true;
								break;
							case W_PROP_NFC_TAG_TYPE_5:
								isNdefCompliant = true;
								break;
							case W_PROP_PICOPASS_2K:
								isNdefCompliant = true;
								break;
							case W_PROP_PICOPASS_32K:
								isNdefCompliant = true;
								break;
							case W_PROP_NFC_TAG_TYPE_6:
								isNdefCompliant = true;
								break;

							case W_PROP_JEWEL:
								isJewelExist = true;
								isNdefCompliant = false;
								goto TYPE1_CHIP;
							case W_PROP_TOPAZ:
								if (tTransceive > T_W_PROP_TOPAZ)
								{
									tTransceive = T_W_PROP_TOPAZ;
								}
								isNdefCompliant = true;
								goto TYPE1_CHIP;
							case W_PROP_TOPAZ_512:
								if (tTransceive > T_W_PROP_TOPAZ)
								{
									tTransceive = T_W_PROP_TOPAZ;
								}
								isNdefCompliant = true;
								goto TYPE1_CHIP;
							case W_PROP_NFC_TAG_TYPE_1:
								if (tTransceive > T_W_PROP_NFC_TAG_TYPE_1)
								{
									tTransceive = T_W_PROP_NFC_TAG_TYPE_1;
								}
								// No break here
							case W_PROP_TYPE1_CHIP:
								isNdefCompliant = true;
								TYPE1_CHIP:
								if (WType1ChipGetConnectionInfo(hConnection, &sType1ChipConnectionInfo) == W_SUCCESS)
								{
									INFO_PER_TAG->libNfcTechType[INFO_PER_TAG->nb_libNfcTechType] = nxpType;
					    		    INFO_PER_TAG->libNfcTechHandle[INFO_PER_TAG->nb_libNfcTechType] = hConnection;
					    		    INFO_PER_TAG->nb_libNfcTechType++;
									memcpy(INFO_PER_TAG->Uid, sType1ChipConnectionInfo.UID, 7);
									INFO_PER_TAG->UidLength = 7;
									isSetUid = true;
								}
									INFO_PER_TAG->HeaderRom0 = sType1ChipConnectionInfo.aHeaderRom[0];
									INFO_PER_TAG->HeaderRom1 = sType1ChipConnectionInfo.aHeaderRom[1];
									// W_PROP_TYPE1_CHIP: Set AtqA
									INFO_PER_TAG->AtqA[0] = (uint8_t) 0x00;
									INFO_PER_TAG->AtqA[1] = (uint8_t) 0x0C;
								break;

							case W_PROP_MIFARE_UL:
							case W_PROP_MIFARE_UL_C:
								isNdefCompliant = true;
								if (tTransceive > T_W_PROP_MIFARE)
								{
									tTransceive = T_W_PROP_MIFARE;
								}
								if (WMifareGetConnectionInfo(hConnection, &sMifareConnectionInfo) == W_SUCCESS)
								{
									INFO_PER_TAG->libNfcTechType[INFO_PER_TAG->nb_libNfcTechType] = nxpType;
					    		    INFO_PER_TAG->libNfcTechHandle[INFO_PER_TAG->nb_libNfcTechType] = hConnection;
					    		    INFO_PER_TAG->nb_libNfcTechType++;
									memcpy(INFO_PER_TAG->Uid, sMifareConnectionInfo.UID, 7);
									INFO_PER_TAG->UidLength = 7;
									isSetUid = true;
								}
									INFO_PER_TAG->AtqA[0] = 0x00;
									INFO_PER_TAG->AtqA[1] = 0x00;
									INFO_PER_TAG->Sak = 0x00;
								break;

							case W_PROP_MIFARE_MINI:
								if (tTransceive > T_W_PROP_MIFARE)
								{
									tTransceive = T_W_PROP_MIFARE;
								}
								if (WMifareGetConnectionInfo(hConnection, &sMifareConnectionInfo) == W_SUCCESS)
								{
									INFO_PER_TAG->libNfcTechType[INFO_PER_TAG->nb_libNfcTechType] = nxpType;
					    		    INFO_PER_TAG->libNfcTechHandle[INFO_PER_TAG->nb_libNfcTechType] = hConnection;
					    		    INFO_PER_TAG->nb_libNfcTechType++;
									memcpy(INFO_PER_TAG->Uid, sMifareConnectionInfo.UID, 7);
									INFO_PER_TAG->UidLength = 7;
									isSetUid = true;
								}
								INFO_PER_TAG->AtqA[0] = 0x00;
								INFO_PER_TAG->AtqA[1] = 0x00;
								INFO_PER_TAG->Sak = 0x09;
								break;

							case W_PROP_MIFARE_1K:
								if (tTransceive > T_NOT_SUPPORT_TYPE)
								{
									tTransceive = T_NOT_SUPPORT_TYPE;
								}
								if (WMifareGetConnectionInfo(hConnection, &sMifareConnectionInfo) == W_SUCCESS)
								{
									INFO_PER_TAG->libNfcTechType[INFO_PER_TAG->nb_libNfcTechType] = nxpType;
					    		    INFO_PER_TAG->libNfcTechHandle[INFO_PER_TAG->nb_libNfcTechType] = hConnection;
					    		    INFO_PER_TAG->nb_libNfcTechType++;
									memcpy(INFO_PER_TAG->Uid, sMifareConnectionInfo.UID, 7);
									INFO_PER_TAG->UidLength = 7;
									isSetUid = true;
								}
								INFO_PER_TAG->AtqA[0] = 0x00;
								INFO_PER_TAG->AtqA[1] = 0x00;
								INFO_PER_TAG->Sak = 0x08;
								break;

							case W_PROP_MIFARE_4K:
								if (tTransceive > T_NOT_SUPPORT_TYPE)
								{
									tTransceive = T_NOT_SUPPORT_TYPE;
								}
								if (WMifareGetConnectionInfo(hConnection, &sMifareConnectionInfo) == W_SUCCESS)
								{
									INFO_PER_TAG->libNfcTechType[INFO_PER_TAG->nb_libNfcTechType] = nxpType;
					    		    INFO_PER_TAG->libNfcTechHandle[INFO_PER_TAG->nb_libNfcTechType] = hConnection;
					    		    INFO_PER_TAG->nb_libNfcTechType++;
									memcpy(INFO_PER_TAG->Uid, sMifareConnectionInfo.UID, 7);
									INFO_PER_TAG->UidLength = 7;
									isSetUid = true;
								}
								INFO_PER_TAG->AtqA[0] = 0x00;
								INFO_PER_TAG->AtqA[1] = 0x00;
								INFO_PER_TAG->Sak = 0x38;
								break;

							case W_PROP_MIFARE_PLUS_X_2K:
							case W_PROP_MIFARE_PLUS_S_2K:
								if (tTransceive > T_NOT_SUPPORT_TYPE)
								{
									tTransceive = T_NOT_SUPPORT_TYPE;
								}
								if (WMifareGetConnectionInfo(hConnection, &sMifareConnectionInfo) == W_SUCCESS)
								{
									INFO_PER_TAG->libNfcTechType[INFO_PER_TAG->nb_libNfcTechType] = nxpType;
					    		    INFO_PER_TAG->libNfcTechHandle[INFO_PER_TAG->nb_libNfcTechType] = hConnection;
					    		    INFO_PER_TAG->nb_libNfcTechType++;
									memcpy(INFO_PER_TAG->Uid, sMifareConnectionInfo.UID, 7);
									INFO_PER_TAG->UidLength = 7;
									isSetUid = true;
								}
								if (pConnectionList[i] == W_PROP_MIFARE_PLUS_S_2K)
								{
								INFO_PER_TAG->AtqA[0] = 0x00;
								INFO_PER_TAG->AtqA[1] = 0x00;
								INFO_PER_TAG->Sak = 0x10;
								}
								break;

							case W_PROP_MIFARE_PLUS_X_4K:
							case W_PROP_MIFARE_PLUS_S_4K:
								if (tTransceive > T_NOT_SUPPORT_TYPE)
								{
									tTransceive = T_NOT_SUPPORT_TYPE;
								}
								if (WMifareGetConnectionInfo(hConnection, &sMifareConnectionInfo) == W_SUCCESS)
								{
									INFO_PER_TAG->libNfcTechType[INFO_PER_TAG->nb_libNfcTechType] = nxpType;
					    		    INFO_PER_TAG->libNfcTechHandle[INFO_PER_TAG->nb_libNfcTechType] = hConnection;
					    		    INFO_PER_TAG->nb_libNfcTechType++;
									memcpy(INFO_PER_TAG->Uid, sMifareConnectionInfo.UID, 7);
									INFO_PER_TAG->UidLength = 7;
									isSetUid = true;
								}
								if (pConnectionList[i] == W_PROP_MIFARE_PLUS_S_4K)
								{
								INFO_PER_TAG->AtqA[0] = 0x00;
								INFO_PER_TAG->AtqA[1] = 0x00;
								INFO_PER_TAG->Sak = 0x11;
								}
								break;

							case W_PROP_MIFARE_DESFIRE_D40:
								isMifareDesfire = true;
								if (tTransceive > T_W_PROP_ISO_14443_4_A)
								{
									tTransceive = T_W_PROP_ISO_14443_4_A;
								}
								break;
							case W_PROP_MIFARE_DESFIRE_EV1_2K:
								isMifareDesfire = true;
								if (tTransceive > T_W_PROP_ISO_14443_4_A)
								{
									tTransceive = T_W_PROP_ISO_14443_4_A;
								}
								break;
							case W_PROP_MIFARE_DESFIRE_EV1_4K:
								isMifareDesfire = true;
								if (tTransceive > T_W_PROP_ISO_14443_4_A)
								{
									tTransceive = T_W_PROP_ISO_14443_4_A;
								}
								break;
							case W_PROP_MIFARE_DESFIRE_EV1_8K:
								isMifareDesfire = true;
								if (tTransceive > T_W_PROP_ISO_14443_4_A)
								{
									tTransceive = T_W_PROP_ISO_14443_4_A;
								}
								break;
							case W_PROP_ICLASS_2K:
								break;
							case W_PROP_ICLASS_16K:
								break;

							case W_PROP_MY_D_MOVE:
							case W_PROP_MY_D_NFC:
								if (tTransceive > T_W_PROP_MY_D_NFC)
								{
									tTransceive = T_W_PROP_MY_D_NFC;
								}
								isNdefCompliant = true;
								break;

							case W_PROP_KOVIO:
								isNdefCompliant = true;
								INFO_PER_TAG->libNfcTechType[INFO_PER_TAG->nb_libNfcTechType] = nxpType;
					    		INFO_PER_TAG->libNfcTechHandle[INFO_PER_TAG->nb_libNfcTechType] = hConnection;
					    		INFO_PER_TAG->nb_libNfcTechType++;
								break;
							default:
								  LogInformation( "readNdefMessageCompleteListener: Unknown type \n\n");
								break;

					   } // fin switch

					} // fin for

					if (isJewelExist)
					   isNdefCompliant = false;
					  LogError( "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
					  LogError( "nPropertyNumber = %d\n", nPropertyNumber);
					  LogError( "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");

				}
			}

		}

	if (errRemoval != W_SUCCESS)
		{
		LogError("A tag is forced to be removed.");
		setInitialTag();
//		INFO_PER_TAG->nb_libNfcTechType = 0;
		WBasicCloseHandle(hConnection);
/*
		LogInformation("readNdefMessageCompleteListener(): WReaderHandlerWorkPerformed(): hConnection=0x%X", hConnection);
		WReaderHandlerWorkPerformed(hConnection, W_FALSE, W_TRUE);
*/

		CloseRemovalHandle();
		transferToJavaData(e, nat, isSetUid, STATUS_SUCCESS, -1);
		return;
		}

	  LogInformation("readNdefMessageCompleteListener: get tag info done");


	/*
	 * read Ndef Message
	 *
	 */

	uint32_t nMessageLength;
	uint32_t nActualMessageLength;
	uint8_t* pMessage;
	W_HANDLE hNextMessage;
	uint32_t offset;
	uint8_t* writePoint;
	offset = 0;

	dataNdefMessages->length = 0;
	if ((nError != W_SUCCESS) && (nError != W_ERROR_ITEM_NOT_FOUND))
	{  LogInformation("readNdefMessageCompleteListener get ndef error");
		isCheckNdefMessageOK = false;
	}
	else
	{  LogInformation("readNdefMessageCompleteListener get ndef OK");
		isCheckNdefMessageOK = true;
	}
	if (nError != W_SUCCESS)
		{
			if (nError == W_ERROR_RF_COMMUNICATION)
			{
				// While reading Ndef message
				// if W_ERROR_RF_COMMUNICATION occurs, OpenNFC does not call the removalListener even if the card is removed later.
				  LogInformation("readNdefMessageCompleteListener: W_ERROR_RF_COMMUNICATION error. Close connection.");
				setInitialTag();
			}
			if (nError == W_ERROR_ITEM_NOT_FOUND)
			{
				  LogInformation("readNdefMessageCompleteListener: W_ERROR_ITEM_NOT_FOUND");
				if (isNdefCompliant == true)
				{
					  LogInformation("readNdefMessageCompleteListener: W_ERROR_ITEM_NOT_FOUND, and isNdefCompliant true");
					maxNdefMessageLength = freeSpaceSize;
				}else{
					   LogInformation("readNdefMessageCompleteListener: W_ERROR_ITEM_NOT_FOUND, and isNdefCompliant false");
					isCheckNdefMessageOK = false;
				}
			}
			transferToJavaData(e, nat, isSetUid, STATUS_SUCCESS, hConnection);
			return;
		}
	writePoint = dataNdefMessages->buffer;
	while (1)
	{
	    nMessageLength = WNDEFGetMessageLength(hMessage);
		pMessage = (uint8_t *)malloc(nMessageLength);
		  LogInformation("readNdefMessageCompleteListener : nMessageLength=%d", nMessageLength);
		if (pMessage == null)
		{
	    	dataNdefMessages->length = 0;
	    	maxNdefMessageLength = freeSpaceSize;
	    	  LogInformation("readNdefMessageCompleteListener: pMessage is null, but return OK");
	    	transferToJavaData(e, nat, isSetUid, STATUS_SUCCESS, hConnection);
	    	return;
		}
	    if (WNDEFGetMessageContent(hMessage, pMessage, nMessageLength, &nActualMessageLength) != W_SUCCESS)
		{
	    	free(pMessage);
	    	dataNdefMessages->length = offset;
	    	maxNdefMessageLength = freeSpaceSize + offset;
	    	  LogInformation("readNdefMessageCompleteListener: Not formatted, but return OK");
	    	transferToJavaData(e, nat, isSetUid, STATUS_SUCCESS, hConnection);
	    	return;
		}
	    offset += nMessageLength;
		if (offset >= MAXIMUM_READ_NDEF_BUFFER_SIZE)
		{
			maxNdefMessageLength = MAXIMUM_READ_NDEF_BUFFER_SIZE;
			  LogInformation("readNdefMessageCompleteListener : over MAXIMUM_READ_NDEF_BUFFER_SIZE.");
			free(pMessage);
			transferToJavaData(e, nat, isSetUid, STATUS_SUCCESS, hConnection);
			return;
		}
		memcpy(writePoint, pMessage, nMessageLength);
		writePoint += nMessageLength;
		free(pMessage);
		hNextMessage = WNDEFGetNextMessage(hMessage);
		WBasicCloseHandle(hMessage);
		hMessage = hNextMessage;
		if (W_NULL_HANDLE == hNextMessage)
		{
			  LogInformation("readNdefMessageCompleteListener : Tchao");
			break;
		}
	} // Fin while

	maxNdefMessageLength = freeSpaceSize + offset;
	dataNdefMessages->length = offset;
	isNdefCompliant = true;
	  LogInformation("readNdefMessageCompleteListener OK, maxNdefMessageLength = %d", maxNdefMessageLength);

	// transfer to java data
	transferToJavaData(e, nat, isSetUid, STATUS_SUCCESS, hConnection);
}

/*
 * Read tag technology and its related information
 *
 */
static void static_CardDetectionHandler(void* pHandlerParameter, W_HANDLE hConnection, W_ERROR nError)
{
	W_ERROR errRemoval;
	memset(INFO_PER_TAG, 0, sizeof(infoPerTag));
	LogInformation("static_CardDetectionHandler hConnection = 0x%X", hConnection);
	errRemoval = WReaderListenToCardRemovalDetection(hConnection, tagRemovalListener, NULL, &removalHandle);
	LogInformation("static_CardDetectionHandler errRemoval = 0x%X", errRemoval);

	tagAccessStatus = NDEF_MODE_READ_ONLY;
	freeSpaceSize = 0;
	getTagBasicInfo(hConnection);

	/* initialization parameters when a new tag is discovered */
	presentTag = true;
	isNdefCompliant = false;
	isJewelExist = false;
	isMifareDesfire = false;
	tTransceive = T_W_INIT;
	maxNdefMessageLength = 0;

	CDP->wCardDetectionError = nError;
	CDP->removalError = errRemoval;
	CDP->hConnection = hConnection;
	CDP->nat = (struct nfc_jni_native_data *)pHandlerParameter;

	/* Check Ndef Message*/
	isCheckNdefMessageOK = false;

	W_HANDLE checkNdefInformationHandleOperation;
	WNDEFReadMessage(hConnection, (tWNDEFReadMessageCompleted*) (&readNdefMessageCompleteListener), NULL,
					W_NDEF_TNF_ANY_TYPE, NULL, &checkNdefInformationHandleOperation);
}
/*  FIN: READING TAG*/


/**
 * Disable the card detection (Here we just go in stand by mode)
 * @param	e	JNI environment
 * @param	o	Reference object
 */
static void com_android_nfc_NfcManager_disableDiscovery(JNIEnv *e, jobject o)
{
	  LogInformation("com_android_nfc_NfcManager_disableDiscovery");
	setStandByMode(W_TRUE);
}

/**
 * Enable the card detection
 * @param	e	JNI environment
 * @param	o	Reference object
 */
static void com_android_nfc_NfcManager_enableDiscovery(JNIEnv *e, jobject o)
{
	W_ERROR nError;
	struct nfc_jni_native_data *nat;
	nat = nfc_jni_get_nat(e, o);

	  LogInformation("com_android_nfc_NfcManager_enableDiscovery");

	setStandByMode(W_FALSE);

	// Read card
	nError = WReaderListenToCardDetection(
			static_CardDetectionHandler,
			(void *) nat,
			W_PRIORITY_MINIMUM, NULL,
			0,
			&handleCardEventRegistry);

	  LogInformation("registration Reader Listen OK");

	if (nError != W_SUCCESS)
	{
		LogError( "com_android_nfc_NfcManager_enableDiscovery WReaderListenToCardDetection failed : %x", nError);
	}
}

/**
 * Do not use for openNFC
 */
static void com_android_nfc_NfcManager_doResetTimeouts( JNIEnv *e, jobject o) {
}

/**
 * Do not use for openNFC
 */
static bool com_android_nfc_NfcManager_doSetTimeout( JNIEnv *e, jobject o,
        jint tech, jint timeout) {
	return true;
}

/**
 * Do not use for openNFC
 */
static jint com_android_nfc_NfcManager_doGetTimeout( JNIEnv *e, jobject o,
        jint tech) {
	return 0;

}

static jboolean com_android_nfc_NfcManager_init_native_struc(JNIEnv *e, jobject o)
{
   struct nfc_jni_native_data *nat = NULL;
   jclass cls;
   jobject obj;
   jfieldID f;

     LogInformation("******  Init Native Structure ******");
   /* Initialize native structure */
   nat = (nfc_jni_native_data*)malloc(sizeof(struct nfc_jni_native_data));
   if(nat == NULL)
   {
      LOGD("malloc of nfc_jni_native_data failed");
      return JNI_FALSE;
   }
   memset(nat, 0, sizeof(*nat));
   e->GetJavaVM(&(nat->vm));
   nat->env_version = e->GetVersion();
   nat->manager = e->NewGlobalRef(o);

   cls = e->GetObjectClass(o);
   f = e->GetFieldID(cls, "mNative", "I");
   e->SetIntField(o, f, (jint)nat);

   /* Initialize native cached references */
   cached_NfcManager_notifyNdefMessageListeners = e->GetMethodID(cls,
      "notifyNdefMessageListeners","(Lcom/android/nfc/nxp/NativeNfcTag;)V");

   cached_NfcManager_notifyTransactionListeners = e->GetMethodID(cls,
      "notifyTransactionListeners", "([B)V");

   cached_NfcManager_notifyLlcpLinkActivation = e->GetMethodID(cls,
      "notifyLlcpLinkActivation","(Lcom/android/nfc/nxp/NativeP2pDevice;)V");

   cached_NfcManager_notifyLlcpLinkDeactivated = e->GetMethodID(cls,
      "notifyLlcpLinkDeactivated","(Lcom/android/nfc/nxp/NativeP2pDevice;)V");

   cached_NfcManager_notifyTargetDeselected = e->GetMethodID(cls,
      "notifyTargetDeselected","()V");

   cached_NfcManager_notifySeFieldActivated = e->GetMethodID(cls,
      "notifySeFieldActivated", "()V");

   cached_NfcManager_notifySeFieldDeactivated = e->GetMethodID(cls,
      "notifySeFieldDeactivated", "()V");

   cached_NfcManager_notifySeApduReceived= e->GetMethodID(cls,
      "notifySeApduReceived", "([B)V");

   cached_NfcManager_notifySeMifareAccess = e->GetMethodID(cls,
      "notifySeMifareAccess", "([B)V");

   cached_NfcManager_notifySeEmvCardRemoval =  e->GetMethodID(cls,
      "notifySeEmvCardRemoval", "()V");

   if(nfc_jni_cache_object(e,"com/android/nfc/nxp/NativeNfcTag",&(nat->cached_NfcTag)) == -1)
   {
      LOGD("Native Structure initialization failed");
      return JNI_FALSE;
   }

   if(nfc_jni_cache_object(e,"com/android/nfc/nxp/NativeP2pDevice",&(nat->cached_P2pDevice)) == -1)
   {
      LOGD("Native Structure initialization failed");
      return JNI_FALSE;
   }

   genericCached_P2pDevice = nat->cached_P2pDevice;
   genericManager = nat->manager;

   TRACE("****** Init Native Structure OK ******");
   return JNI_TRUE;
}

/**
 * Initialize the stack for be ready to do NFC work
 * @param	jniEvironment	JNI environment
 * @param	referenceObject	Object reference
 */
static jboolean com_android_nfc_NfcManager_initialize(JNIEnv *jniEvironment, jobject referenceObject)
{
	  LogInformation("com_android_nfc_NfcManager_initialize");
	W_ERROR error = JNI_FALSE;

	// Initialization of the structure for tag reading
	initInfoPerTagStructure();

	initP2PSockets();

	error = registerErrorEvents();

	if (error != W_SUCCESS)
	{
		return JNI_FALSE;
	}

	tWP2PConfiguration  configuration;
	error = WP2PGetConfiguration(&configuration);
	LogInformation("com_android_nfc_NfcManager_initialize: WP2PGetConfiguration() - default values: nLocalLTO=%d, "
			"nLocalMIU=%d, bAllowInitiatorMode=%d, bAllowActiveMode=%d,bAllowTypeATargetProtocol=%d",
			configuration.nLocalLTO, configuration.nLocalMIU, configuration.bAllowInitiatorMode,
			configuration.bAllowActiveMode, configuration.bAllowTypeATargetProtocol);

	configuration.nLocalLTO = (uint16_t)500;
	configuration.nLocalMIU = (uint16_t)256;
	configuration.bAllowInitiatorMode = W_TRUE;
	configuration.bAllowActiveMode = W_TRUE;
	configuration.bAllowTypeATargetProtocol = W_FALSE;
	error = WP2PSetConfiguration(&configuration);
	LogInformation("com_android_nfc_NfcManager_initialize: setting new P2PConfiguration: nLocalLTO=%d, "
			"nLocalMIU=%d, bAllowInitiatorMode=%d, bAllowActiveMode=%d,bAllowTypeATargetProtocol=%d",
			configuration.nLocalLTO, configuration.nLocalMIU, configuration.bAllowInitiatorMode,
			configuration.bAllowActiveMode, configuration.bAllowTypeATargetProtocol);
	if(error != W_SUCCESS)
	{
		LogError("P2P initialization failed (error = 0x%X)", error);
	}

	return JNI_TRUE;
}

/**
 * Deinitialize the stack (Here we just put in stand by mode)
 * @param	e	JNI environment
 * @param	o	Reference object
 */
static jboolean com_android_nfc_NfcManager_deinitialize(JNIEnv *e, jobject o)
{
	LogInformation("com_android_nfc_NfcManager_deinitialize");
	setStandByMode(W_TRUE);
	closeInfoPerTagStructure();
	return JNI_TRUE;
}

static jintArray com_android_nfc_NfcManager_doGetSecureElementList(JNIEnv *e, jobject o) {
    jintArray list= NULL;
    return list;
}

/**
 * OpenNFc always on active mode
 */
static void com_android_nfc_NfcManager_doSelectSecureElement(JNIEnv *e, jobject o) {

}

static void com_android_nfc_NfcManager_doDeselectSecureElement(JNIEnv *e, jobject o) {

}

/* Llcp methods */

static jboolean com_android_nfc_NfcManager_doCheckLlcp(JNIEnv *e, jobject o)
{
	return JNI_TRUE;
}

static jboolean com_android_nfc_NfcManager_doActivateLlcp(JNIEnv *e, jobject o)
{
	return JNI_TRUE;
}



static jobject com_android_nfc_NfcManager_doCreateLlcpConnectionlessSocket(JNIEnv *e, jobject o, jint nSap)
{
	return NULL;
}

static jobject com_android_nfc_NfcManager_doCreateLlcpServiceSocket(JNIEnv *e, jobject o, jint nSap, jstring sn, jint miu, jint rw, jint linearBufferLength)
{
	struct nfc_jni_native_data *nat;
	jobject serviceSocket = NULL;
	jclass clsNativeLlcpServiceSocket;
	jfieldID f;
	//phNfc_sData_t serviceName;
	char16_t* serviceNameBuffer;

	uint32_t serviceNameLength = 0;

	/* Retrieve native structure address */
	nat = nfc_jni_get_nat(e, o);

	/* Service socket */
	if (sn == NULL)
	{
		serviceNameBuffer = NULL;
		//serviceNameLength = 0;
	}
	else
	{
		jboolean isCopy = JNI_TRUE;


		serviceNameBuffer = (char16_t*) e->GetStringChars(sn, &isCopy);
		serviceNameLength = (uint32_t) e->GetStringLength(sn);

		LogInformation("com_android_nfc_NfcManager_doCreateLlcpServiceSocket isCopy=%s length=%d",
				((isCopy==JNI_TRUE) ? "TRUE" : "FALSE"), serviceNameLength);

	}

	//Create Open NFC socket
	uint32_t handleServerSocket = createServerSocketLinkedHandle(serviceNameBuffer, serviceNameLength, (uint8_t) (nSap & 0xFF));

	if (sn != NULL)
	{
		e->ReleaseStringChars(sn, (jchar*) serviceNameBuffer);
	}
	//Create the service socket Java object

   /* Create new NativeLlcpServiceSocket object */
   if(nfc_jni_cache_object(e, "com/android/nfc/nxp/NativeLlcpServiceSocket", &(serviceSocket)) == -1)
   {
      LogError("Llcp Socket object creation error");
      return NULL;
   }
   /* Get NativeLlcpServiceSocket class object */
   clsNativeLlcpServiceSocket = e->GetObjectClass(serviceSocket);
   if(e->ExceptionCheck())
   {
	   LogError("Llcp Socket get object class error");
      return NULL;
   }
/*
   tWP2PConfiguration configuration;
   W_ERROR status = WP2PGetConfiguration(&configuration);
   uint16_t nLocalMIU = configuration.nLocalMIU;
   if (status != W_SUCCESS)
   {
	  LogWarning("P2P get configuration error (0x%X)", status);
	  nLocalMIU = 256;
   }
*/


   /* Set socket handle */
   f = e->GetFieldID(clsNativeLlcpServiceSocket, "mHandle", "I");
   e->SetIntField(serviceSocket, f, (jint) handleServerSocket);
   LogInformation("Service socket: Handle = 0x%X",handleServerSocket);

   /* Set socket linear buffer length */
   f = e->GetFieldID(clsNativeLlcpServiceSocket, "mLocalLinearBufferLength", "I");
   e->SetIntField(serviceSocket, f, linearBufferLength);
   LogInformation("Service socket: Linear buffer length = %d",linearBufferLength);

   /* Set socket MIU */
   f = e->GetFieldID(clsNativeLlcpServiceSocket, "mLocalMiu", "I");
   e->SetIntField(serviceSocket, f, miu);
   LogInformation("Service socket: MIU = %d", miu);

   /* Set socket RW */
   f = e->GetFieldID(clsNativeLlcpServiceSocket, "mLocalRw", "I");
   e->SetIntField(serviceSocket, f, rw);
   LogInformation("Service socket: RW = %d", rw);

   return serviceSocket;
}

static jobject com_android_nfc_NfcManager_doCreateLlcpSocket(JNIEnv *e, jobject o, jint nSap, jint miu, jint rw, jint linearBufferLength)
{
	uint32_t handle = createSocketLinkedHandle((uint8_t)nSap);

	struct nfc_jni_native_data *nat;
	jobject clientSocket = NULL;
	jclass clsNativeLlcpSocket;
	jfieldID f;

	/* Retrieve native structure address */
	nat = nfc_jni_get_nat(e, o);

	if(nfc_jni_cache_object(e,"com/android/nfc/nxp/NativeLlcpSocket",&(clientSocket)) == -1)
	{
		LogError("Llcp socket object creation error");
		return NULL;
	}

	clsNativeLlcpSocket = e->GetObjectClass(clientSocket);
	if(e->ExceptionCheck())
	{
		LogError("Get class object error");
		return NULL;
	}


	/* Set socket SAP */
	f = e->GetFieldID(clsNativeLlcpSocket, "mSap", "I");
	e->SetIntField(clientSocket, f,(jint)nSap);
	TRACE("socket SAP = %d\n",nSap);

	/* Set socket handle */
	f = e->GetFieldID(clsNativeLlcpSocket, "mHandle", "I");
	e->SetIntField(clientSocket, f,(jint)handle);
	TRACE("socket Handle = 0x%X\n", handle);

	/* Set socket MIU */
	f = e->GetFieldID(clsNativeLlcpSocket, "mLocalMiu", "I");
	e->SetIntField(clientSocket, f, miu);
	TRACE("socket MIU = %d", miu);

	/* Set socket RW */
	f = e->GetFieldID(clsNativeLlcpSocket, "mLocalRw", "I");
	e->SetIntField(clientSocket, f, rw);
	TRACE("socket RW = %d", rw);

	return clientSocket;
}

static jint com_android_nfc_NfcManager_doGetLastError(JNIEnv *e, jobject o)
{
	return 0;
}

static void com_android_nfc_NfcManager_doAbort(JNIEnv *e, jobject o)
{
}

/* a structure to provide parameters to be used in firmware update callback function */
typedef struct
{
	/* semaphore for the end of operation */
	sem_t sem;
	/* buffer to new firmware binary data */
	uint8_t* pUpdateBuffer;
	/* status of the operation */
	W_ERROR status;
} firmwareUpdateCallbackParams_t;

/* sends notification about f/w update completion */
static void firmwareUpdateCompletion(firmwareUpdateCallbackParams_t* pCallbackParameter, W_ERROR status)
{
	  LogInformation("firmwareUpdateCompletion: status=0x%04X", status);
	pCallbackParameter->status = status;
	SEMAPHORE_POST(&pCallbackParameter->sem);
}

static void firmwareUpdateCallback(void *pCallbackParameter, W_ERROR nResult)
{
	firmwareUpdateCallbackParams_t* pFWUpdateCallbackParams = (firmwareUpdateCallbackParams_t*) pCallbackParameter;
	if (nResult != W_SUCCESS)
	{
		LogWarning("NFC firmware update failed (0x%X)", nResult);
	}
	else
	{
		  LogInformation("NFC firmware is updated");
	}
	free(pFWUpdateCallbackParams->pUpdateBuffer);
	firmwareUpdateCompletion(pFWUpdateCallbackParams, nResult);
}

static void resetCallback(void *pCallbackParameter, W_ERROR nResult)
{
	FILE* pFWFile = NULL;
	uint32_t nUpdateBufferLength, readLength;
	firmwareUpdateCallbackParams_t* pFWUpdateCallbackParams = (firmwareUpdateCallbackParams_t*) pCallbackParameter;

	  LogInformation("NFC chip reset callback: err=0x%X", nResult);
	if (nResult == W_SUCCESS)
	{
		pFWFile = fopen(FW_FILENAME, "rb");
		if (pFWFile == NULL)
		{
			LogError("Can't update NFC firmware: can't open f/w file");
			goto err_label;
		}

		fseek(pFWFile, 0, SEEK_END);
		nUpdateBufferLength = ftell(pFWFile);
		fseek(pFWFile, 0, SEEK_SET);

		pFWUpdateCallbackParams->pUpdateBuffer = (uint8_t*) malloc(nUpdateBufferLength);
		if (pFWUpdateCallbackParams->pUpdateBuffer == NULL)
		{
			LogError("Can't update NFC firmware: can't allocate buffer");
			fclose(pFWFile);
			goto err_label;
		}
		readLength = fread(pFWUpdateCallbackParams->pUpdateBuffer, 1, nUpdateBufferLength, pFWFile);
		fclose(pFWFile);
		if (readLength < nUpdateBufferLength)
		{
			LogError("Can't update NFC firmware: can't read f/w file");
			free(pFWUpdateCallbackParams->pUpdateBuffer);
			goto err_label;
		}

		/* start f/w update */
		WNFCControllerFirmwareUpdate(firmwareUpdateCallback, pFWUpdateCallbackParams, pFWUpdateCallbackParams->pUpdateBuffer,
				nUpdateBufferLength, W_NFCC_MODE_ACTIVE);

		return;

	}
	else
	{
		LogWarning("Can't update NFC f/w: NFC chip can't be switched in maintenance mode (0x%X)", nResult);
	}
err_label:
	firmwareUpdateCompletion(pFWUpdateCallbackParams, nResult);
}


static jboolean com_android_nfc_NfcManager_doDownload(JNIEnv *e, jobject o)
{
	W_ERROR error = intializeClient();
	if (error != W_SUCCESS) {
		return JNI_FALSE;
	}
	firmwareUpdateCallbackParams_t cbParams;
	if (SEMAPHORE_CREATE(&cbParams.sem) == -1) {
		return JNI_FALSE;
	}

	WNFCControllerReset(resetCallback, &cbParams, W_NFCC_MODE_MAINTENANCE);

	/* wait for the end of operation */
	SEMAPHORE_WAIT(&cbParams.sem);

	SEMAPHORE_DESTROY(&cbParams.sem);
	return (cbParams.status == W_SUCCESS);
}

static jstring com_android_nfc_NfcManager_doDump(JNIEnv *e, jobject o)
{
	return NULL;
}

// ----- MTK begins ----- //
static jboolean com_android_nfc_NfcManager_isUiccSecureElementPresence(JNIEnv *e, jobject o) {
	return false;
}

static void com_android_nfc_NfcManager_doSelectUiccSecureElement(JNIEnv *e, jobject o, jint select) {
	//dummy
}

static void com_android_nfc_NfcManager_doSelectUiccSecureElementInPBTF(JNIEnv *e, jobject o, jint select) {
	//dummy
}

static void com_android_nfc_NfcManager_doSelectP2pBitrate106(JNIEnv *e, jobject o, jint select) {
	//dummy
}
// ----- MTK ends ----- //

/*
 * JNI registration.
 */
static JNINativeMethod gMethods[] =
{
   {"doDownload", "()Z",
        (void *)com_android_nfc_NfcManager_doDownload},

   {"initializeNativeStructure", "()Z",
      (void *)com_android_nfc_NfcManager_init_native_struc},

   {"initialize", "()Z",
      (void *)com_android_nfc_NfcManager_initialize},

   {"deinitialize", "()Z",
      (void *)com_android_nfc_NfcManager_deinitialize},

   {"enableDiscovery", "()V",
      (void *)com_android_nfc_NfcManager_enableDiscovery},

   {"doGetSecureElementList", "()[I",
      (void *)com_android_nfc_NfcManager_doGetSecureElementList},

   {"doSelectSecureElement", "()V",
      (void *)com_android_nfc_NfcManager_doSelectSecureElement},

   {"doDeselectSecureElement", "()V",
      (void *)com_android_nfc_NfcManager_doDeselectSecureElement},

   {"doCheckLlcp", "()Z",
      (void *)com_android_nfc_NfcManager_doCheckLlcp},

   {"doActivateLlcp", "()Z",
      (void *)com_android_nfc_NfcManager_doActivateLlcp},

   {"doCreateLlcpConnectionlessSocket", "(I)Lcom/android/nfc/nxp/NativeLlcpConnectionlessSocket;",
      (void *)com_android_nfc_NfcManager_doCreateLlcpConnectionlessSocket},

   {"doCreateLlcpServiceSocket", "(ILjava/lang/String;III)Lcom/android/nfc/nxp/NativeLlcpServiceSocket;",
      (void *)com_android_nfc_NfcManager_doCreateLlcpServiceSocket},

   {"doCreateLlcpSocket", "(IIII)Lcom/android/nfc/nxp/NativeLlcpSocket;",
      (void *)com_android_nfc_NfcManager_doCreateLlcpSocket},

   {"doGetLastError", "()I",
      (void *)com_android_nfc_NfcManager_doGetLastError},

   {"disableDiscovery", "()V",
      (void *)com_android_nfc_NfcManager_disableDiscovery},

   {"doSetTimeout", "(II)Z",
      (void *)com_android_nfc_NfcManager_doSetTimeout},

   {"doGetTimeout", "(I)I",
      (void *)com_android_nfc_NfcManager_doGetTimeout},

   {"doResetTimeouts", "()V",
      (void *)com_android_nfc_NfcManager_doResetTimeouts},

   {"doAbort", "()V",
      (void *)com_android_nfc_NfcManager_doAbort},

   {"doDump", "()Ljava/lang/String;",
      (void *)com_android_nfc_NfcManager_doDump},
	  
	// ----- MTK begins ----- //
   {"doIsUiccSecureElementPresence", "()Z",
      (void *)com_android_nfc_NfcManager_isUiccSecureElementPresence},
   
   {"doSelectUiccSecureElement", "(I)V",
      (void *)com_android_nfc_NfcManager_doSelectUiccSecureElement},
      
   {"doSelectUiccSecureElementInPBTF", "(I)V",
      (void *)com_android_nfc_NfcManager_doSelectUiccSecureElementInPBTF},      

   {"doSelectP2pBitrate106", "(I)V",
      (void *)com_android_nfc_NfcManager_doSelectP2pBitrate106},
	// ----- MTK ends ----- //

};


int register_com_android_nfc_NativeNfcManager(JNIEnv *e)
{
    nfc_jni_native_monitor_t *nfc_jni_native_monitor;

   nfc_jni_native_monitor = nfc_jni_init_monitor();
   if(nfc_jni_native_monitor == NULL)
   {
      LOGE("NFC Manager cannot recover native monitor %x\n", errno);
      return -1;
   }

   return jniRegisterNativeMethods(e,
      "com/android/nfc/nxp/NativeNfcManager",
      gMethods, NELEM(gMethods));
}

} /* namespace android */
