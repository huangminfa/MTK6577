#include "card_emulation_ext.h"
#include "open_nfc_extension.h"

/**
 * Create message for answer to "get secure element policy"
 * @param[in]	uiccPolicy		Policy get for UICC
 * @param[in]	sePolicy		Policy get for embed SE
 * @param[in]	applicationID	Application ID
 * @param[in]	requestID		Request ID
 * @return	Created message
 */
static tMessageOpenNFC * createGetSecureElementPolicyAnswerMessage(uint32_t uiccPolicy, uint32_t sePolicy, tAppId applicationID, int requestID)
{
	tMessageOpenNFC * messageAnswer = createMessageOpenNFC(OPEN_NFC_CARD_EMULATION_POLICY, applicationID, requestID, OPEN_NFC_EXTERNAL_API_SUCCES, OPEN_NFC_GET_CARD_EMULATION_POLICY, 8);

	int offset = 0;
	serializeUint32(messageAnswer->pData, 8, &offset, uiccPolicy);
	serializeUint32(messageAnswer->pData, 8, &offset, sePolicy);

	return messageAnswer;
}

/**
 * Get secure element policies
 */
static void getSecureElementPolicy(tAppId applicationID, int requestID)
{
	uint32_t uiccPolicy = 0;
	uint32_t sePolicy = 0;

	//
	uint32_t nbSE = 0;
	W_ERROR error = WNFCControllerGetIntegerProperty(W_NFCC_PROP_SE_NUMBER, &nbSE);

	if(error!=W_SUCCESS)
	{
		LogWarning("Can't retrieve the number of Secure Element error=%x", error);

		tMessageOpenNFC * messageAnswer = createMessageOpenNFC(OPEN_NFC_CARD_EMULATION_POLICY, applicationID, requestID, OPEN_NFC_EXTERNAL_API_CARD_EMULATION_NOT_AVAILABLE, OPEN_NFC_GET_CARD_EMULATION_POLICY, 0);

		if (sendOpenNfcExtNotification(messageAnswer) == W_FALSE)
		{
			LogError("External API didn't manage to set the answer of getSecureElementPolicy");
		}

		return;
	}

	if(nbSE==0)
	{
		LogInformation("No secure element found");

		tMessageOpenNFC * messageAnswer = createMessageOpenNFC(OPEN_NFC_CARD_EMULATION_POLICY, applicationID, requestID, OPEN_NFC_EXTERNAL_API_CARD_EMULATION_NOT_AVAILABLE, OPEN_NFC_GET_CARD_EMULATION_POLICY, 0);

		if (sendOpenNfcExtNotification(messageAnswer) == W_FALSE)
		{
			LogError("External API didn't manage to set the answer of getSecureElementPolicy");
		}

		return;
	}

	tWSEInfoEx seInformation;
	uint32_t index;

	for(index=0; index<nbSE; index++)
	{
		error = WSEGetInfoEx(index, & seInformation);

		if(error==W_SUCCESS)
		{
			if((seInformation.nCapabilities & W_SE_FLAG_UICC) != 0)
			{
				uiccPolicy |= seInformation.nVolatilePolicy;
			}
			else if(seInformation.nCapabilities != 0)
			{
				sePolicy |= seInformation.nVolatilePolicy;
			}
		}
	}

	//

	tMessageOpenNFC * messageAnswer = createGetSecureElementPolicyAnswerMessage(uiccPolicy, sePolicy, applicationID, requestID);

	if (sendOpenNfcExtNotification(messageAnswer) == W_FALSE)
	{
		LogError("External API didn't manage to set the answer of getSecureElementPolicy");
	}
}

/**
 * Change secure element policies
 * @param[in]	uiccPolicy	New UICC policy
 * @param[in]	sePolicy	New embed SE policy
 */
static void setSecureElementPolicy(uint32_t uiccPolicy, uint32_t sePolicy, tAppId applicationID, int requestID)
{
	uint32_t nbSE = 0;
	W_ERROR error = WNFCControllerGetIntegerProperty(W_NFCC_PROP_SE_NUMBER, &nbSE);

	if(error!=W_SUCCESS)
	{
		LogWarning("Can't retrieve the number of Secure Element error=%x", error);

		tMessageOpenNFC * messageAnswer = createMessageOpenNFC(OPEN_NFC_CARD_EMULATION_POLICY, applicationID, requestID, OPEN_NFC_EXTERNAL_API_CARD_EMULATION_NOT_AVAILABLE, OPEN_NFC_SET_CARD_EMULATION_POLICY, 0);

		if (sendOpenNfcExtNotification(messageAnswer) == W_FALSE)
		{
			LogError("External API didn't manage to set the answer of getSecureElementPolicy");
		}

		return;
	}

	if(nbSE==0)
	{
		LogInformation("No secure element found");

		tMessageOpenNFC * messageAnswer = createMessageOpenNFC(OPEN_NFC_CARD_EMULATION_POLICY, applicationID, requestID, OPEN_NFC_EXTERNAL_API_CARD_EMULATION_NOT_AVAILABLE, OPEN_NFC_SET_CARD_EMULATION_POLICY, 0);

		if (sendOpenNfcExtNotification(messageAnswer) == W_FALSE)
		{
			LogError("External API didn't manage to set the answer of getSecureElementPolicy");
		}

		return;
	}

	tWSEInfoEx seInformation;
	uint32_t index;

	for(index=0; index<nbSE; index++)
	{
		error = WSEGetInfoEx(index, & seInformation);

		if(error==W_SUCCESS)
		{
			WSESetPolicySync(index, W_NFCC_STORAGE_BOTH , 0);
		}
	}


	for(index=0; index<nbSE; index++)
	{
		error = WSEGetInfoEx(index, & seInformation);

		if(error==W_SUCCESS)
		{
			if((seInformation.nCapabilities & W_SE_FLAG_UICC) != 0)
			{
				error = WSESetPolicySync(index, W_NFCC_STORAGE_BOTH , uiccPolicy);

				if(error != W_SUCCESS)
				{
					LogWarning("We meet error=%x when try to set policy for UICC", error);
				}
			}
			else if(seInformation.nCapabilities != 0)
			{
				error = WSESetPolicySync(index, W_NFCC_STORAGE_BOTH , sePolicy);

				if(error != W_SUCCESS)
				{
					LogWarning("We meet error=%x when try to set policy for embed SE", error);
				}
			}
		}
	}

	tMessageOpenNFC * messageAnswer = createMessageOpenNFC(OPEN_NFC_CARD_EMULATION_POLICY, applicationID, requestID, OPEN_NFC_EXTERNAL_API_SUCCES, OPEN_NFC_SET_CARD_EMULATION_POLICY, 0);

	if (sendOpenNfcExtNotification(messageAnswer) == W_FALSE)
	{
		LogError("External API didn't manage to set the answer of getSecureElementPolicy");
	}
}

/**
 * Called each time API server received a message for card emulation policy
 * @param[in]	messageOpenNFC	Message received
 */
static void cardEmulationPolicyCallback(tMessageOpenNFC * messageOpenNFC)
{
	uint32_t integer1, integer2;
	int offset = 0;

	tMessageOpenNFCHeader messageHeader = messageOpenNFC->header;

	tAppId applicationID = messageHeader.appId;
	int requestID = messageHeader.reqId;

	switch (messageHeader.commandId)
	{
		case OPEN_NFC_GET_CARD_EMULATION_POLICY:
			getSecureElementPolicy(applicationID, requestID);
			break;
		case OPEN_NFC_SET_CARD_EMULATION_POLICY:
			integer1 = parseUint32(messageOpenNFC->pData, messageHeader.length, &offset); //UICC policy
			integer2 = parseUint32(messageOpenNFC->pData, messageHeader.length, &offset); //SE policy

			setSecureElementPolicy(integer1, integer2, applicationID, requestID);
			break;
	}
}

/**
 * Initialize the server part of Open NFC Extension for Card Emulation
 */
void initializeCardEmulationExt()
{
	if (registerOpenNfcExtCallback(OPEN_NFC_CARD_EMULATION_POLICY, cardEmulationPolicyCallback) == W_FALSE)
	{
		LogError("Open NFC Extensions API can't register callback for card emulation policy");
	}
}
