/*
 * Copyright (c) 2011 Inside Secure, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*******************************************************************************
   Contains the kovio implementation
*******************************************************************************/
#define P_MODULE  P_MODULE_DEC( KOVIO )

#include "wme_context.h"

#define P_KOVIO_MAX_LENGTH      0x20

#if (P_BUILD_CONFIG == P_CONFIG_DRIVER) || (P_BUILD_CONFIG == P_CONFIG_MONOLITHIC)

/* Declare a Kovio driver exchange data structure */
typedef struct __tPKovioDriverConnection
{
   /* Connection object registry */
   tHandleObjectHeader        sObjectHeader;

} tPKovioDriverConnection;

/* Destroy connection callback */
static uint32_t static_PKovioDriverDestroyConnection(
            tContext* pContext,
            void* pObject );

/* Connection registry Kovio type */
tHandleType g_sKovioDriverConnection = { static_PKovioDriverDestroyConnection,
                                          null, null, null, null, null, null, null, null };

#define P_HANDLE_TYPE_KOVIO_DRIVER_CONNECTION (&g_sKovioDriverConnection)

static uint32_t static_PKovioDriverDestroyConnection(
            tContext* pContext,
            void* pObject )
{
   tPKovioDriverConnection* pKovioDriverConnection = (tPKovioDriverConnection*)pObject;

   PDebugTrace("static_PKovioDriverDestroyConnection");

   CMemoryFree( pKovioDriverConnection );

   return P_HANDLE_DESTROY_DONE;
}

/** @see tPReaderDriverCreateConnection() */
static W_ERROR static_PKovioDriverCreateConnection(
            tContext* pContext,
            uint8_t nServiceIdentifier,
            W_HANDLE* phDriverConnection )
{
   tPKovioDriverConnection* pKovioDriverConnection;
   W_HANDLE hDriverConnection;
   W_ERROR nError;

   /* Check the parameters */
   if ( phDriverConnection == null )
   {
      PDebugError("static_PKovioDriverCreateConnection: W_ERROR_BAD_PARAMETER");
      return W_ERROR_BAD_PARAMETER;
   }

   /* Create the Kovio buffer */
   pKovioDriverConnection = (tPKovioDriverConnection*)CMemoryAlloc( sizeof(tPKovioDriverConnection) );
   if ( pKovioDriverConnection == null )
   {
      PDebugError("static_PKovioDriverCreateConnection: pKovioDriverConnection == null");
      return W_ERROR_OUT_OF_RESOURCE;
   }
   CMemoryFill(pKovioDriverConnection, 0, sizeof(tPKovioDriverConnection));

   /* Create the Kovio operation handle */
   if ( ( nError = PHandleRegister(
                     pContext,
                     pKovioDriverConnection,
                     P_HANDLE_TYPE_KOVIO_DRIVER_CONNECTION,
                     &hDriverConnection ) ) != W_SUCCESS )
   {
      PDebugError("static_PKovioDriverCreateConnection: could not create Kovio connection handle");
      CMemoryFree(pKovioDriverConnection);
      return nError;
   }

   *phDriverConnection = hDriverConnection;

   return W_SUCCESS;
}

/** @see tPReaderDriverParseDetectionMessage() */
static W_ERROR static_PKovioDriverParseDetectionMessage(
               tContext* pContext,
               const uint8_t* pBuffer,
               uint32_t nLength,
               tPReaderDriverCardInfo* pCardInfo )
{
   W_ERROR nError = W_SUCCESS;

   PDebugTrace("static_PKovioDriverParseDetectionMessage()");
   if(pCardInfo->nProtocolBF != W_NFCC_PROTOCOL_READER_KOVIO)
   {
      PDebugError("static_P14P4DriverParseDetectionMessage: protocol error");
      nError = W_ERROR_NFC_HAL_COMMUNICATION;
      goto return_function;
   }

   CMemoryCopy(pCardInfo->aUID,
               pBuffer,
               nLength);

   pCardInfo->nUIDLength = (uint8_t) nLength;
   pCardInfo->nProtocolBF |= W_NFCC_PROTOCOL_READER_KOVIO;

return_function:

   if(nError == W_SUCCESS)
   {
      PDebugTrace("static_PKovioDriverParseDetectionMessage: UID = ");
      PDebugTraceBuffer(pCardInfo->aUID, pCardInfo->nUIDLength);

   }
   else
   {
      PDebugTrace("static_PKovioDriverParseDetectionMessage: error %s", PUtilTraceError(nError));
   }

   return nError;
}

/* The protocol information structure */
tPRegistryDriverReaderProtocolConstant g_sPKovioReaderProtocolConstant = {
   W_NFCC_PROTOCOL_READER_KOVIO,
   NAL_SERVICE_READER_KOVIO,
   static_PKovioDriverCreateConnection,
   null,
   static_PKovioDriverParseDetectionMessage,
   null };

#endif /* P_CONFIG_DRIVER || P_CONFIG_MONOLITHIC */

#if (P_BUILD_CONFIG == P_CONFIG_USER) || (P_BUILD_CONFIG == P_CONFIG_MONOLITHIC)


static const uint8_t static_aInvertionByte[] = {
   0x00, 0x80, 0x40, 0xC0, 0x20, 0xA0, 0x60, 0xE0, 0x10, 0x90, 0x50, 0xD0, 0x30, 0xB0, 0x70, 0xF0,
   0x08, 0x88, 0x48, 0xC8, 0x28, 0xA8, 0x68, 0xE8, 0x18, 0x98, 0x58, 0xD8, 0x38, 0xB8, 0x78, 0xF8,
   0x04, 0x84, 0x44, 0xC4, 0x24, 0xA4, 0x64, 0xE4, 0x14, 0x94, 0x54, 0xD4, 0x34, 0xB4, 0x74, 0xF4,
   0x0C, 0x8C, 0x4C, 0xCC, 0x2C, 0xAC, 0x6C, 0xEC, 0x1C, 0x9C, 0x5C, 0xDC, 0x3C, 0xBC, 0x7C, 0xFC,
   0x02, 0x82, 0x42, 0xC2, 0x22, 0xA2, 0x62, 0xE2, 0x12, 0x92, 0x52, 0xD2, 0x32, 0xB2, 0x72, 0xF2,
   0x0A, 0x8A, 0x4A, 0xCA, 0x2A, 0xAA, 0x6A, 0xEA, 0x1A, 0x9A, 0x5A, 0xDA, 0x3A, 0xBA, 0x7A, 0xFA,
   0x06, 0x86, 0x46, 0xC6, 0x26, 0xA6, 0x66, 0xE6, 0x16, 0x96, 0x56, 0xD6, 0x36, 0xB6, 0x76, 0xF6,
   0x0E, 0x8E, 0x4E, 0xCE, 0x2E, 0xAE, 0x6E, 0xEE, 0x1E, 0x9E, 0x5E, 0xDE, 0x3E, 0xBE, 0x7E, 0xFE,
   0x01, 0x81, 0x41, 0xC1, 0x21, 0xA1, 0x61, 0xE1, 0x11, 0x91, 0x51, 0xD1, 0x31, 0xB1, 0x71, 0xF1,
   0x09, 0x89, 0x49, 0xC9, 0x29, 0xA9, 0x69, 0xE9, 0x19, 0x99, 0x59, 0xD9, 0x39, 0xB9, 0x79, 0xF9,
   0x05, 0x85, 0x45, 0xC5, 0x25, 0xA5, 0x65, 0xE5, 0x15, 0x95, 0x55, 0xD5, 0x35, 0xB5, 0x75, 0xF5,
   0x0D, 0x8D, 0x4D, 0xCD, 0x2D, 0xAD, 0x6D, 0xED, 0x1D, 0x9D, 0x5D, 0xDD, 0x3D, 0xBD, 0x7D, 0xFD,
   0x03, 0x83, 0x43, 0xC3, 0x23, 0xA3, 0x63, 0xE3, 0x13, 0x93, 0x53, 0xD3, 0x33, 0xB3, 0x73, 0xF3,
   0x0B, 0x8B, 0x4B, 0xCB, 0x2B, 0xAB, 0x6B, 0xEB, 0x1B, 0x9B, 0x5B, 0xDB, 0x3B, 0xBB, 0x7B, 0xFB,
   0x07, 0x87, 0x47, 0xC7, 0x27, 0xA7, 0x67, 0xE7, 0x17, 0x97, 0x57, 0xD7, 0x37, 0xB7, 0x77, 0xF7,
   0x0F, 0x8F, 0x4F, 0xCF, 0x2F, 0xAF, 0x6F, 0xEF, 0x1F, 0x9F, 0x5F, 0xDF, 0x3F, 0xBF, 0x7F, 0xFF};

static void static_PKovioDecodeData(
            uint8_t * pOutput,
            const uint8_t * pInput,
            uint32_t length)
{
   uint8_t bBegin = 1;
   uint8_t tmp;

   while(length-- > 0)
   {
      tmp = ((*pInput & 0x80) == 0x80);
      *pOutput = ((*pInput << 1) & 0xFE) | bBegin;
      *pOutput = static_aInvertionByte[*pOutput];

      bBegin = tmp;

      pOutput++;
      pInput++;
   }
}

typedef struct __tPKovioUserConnection
{
   /* Connection registry handle */
   tHandleObjectHeader        sObjectHeader;

   uint8_t                    aData[P_KOVIO_MAX_LENGTH];

   uint32_t                   nLength;

} tPKovioUserConnection;


/* Destroy connection callback */
static uint32_t static_PKovioUserDestroyConnection(
            tContext* pContext,
            void* pObject );

/* Get property numbet callback */
static uint32_t static_PKovioUserGetPropertyNumber(
            tContext* pContext,
            void* pObject);

/* Get property numbet callback */
static bool_t static_PKovioUserGetProperties(
         tContext* pContext,
         void* pObject,
         uint8_t* pPropertyArray );

/* Check the property */
static bool_t static_PKovioUseCheckProperty(
         tContext* pContext,
         void* pObject,
         uint8_t nPropertyValue );

/* Get identifier length */
static uint32_t static_PKovioGetIdentifierLength(
         tContext* pContext,
         void* pObject);

/* Get identifier */
static void static_PKovioGetIdentifier(
         tContext* pContext,
         void* pObject,
         uint8_t* pIdentifierBuffer);

/* Connection registry Kovio type */
tHandleType g_sKovioUserConnection = {   static_PKovioUserDestroyConnection,
                                          null,
                                          static_PKovioUserGetPropertyNumber,
                                          static_PKovioUserGetProperties,
                                          static_PKovioUseCheckProperty,
                                          static_PKovioGetIdentifierLength,
                                          static_PKovioGetIdentifier,
                                          null, null };


#define P_HANDLE_TYPE_KOVIO_USER_CONNECTION (&g_sKovioUserConnection)

/**
 * @brief   Destroyes a Kovio connection object.
 *
 * @param[in]  pContext  The context.
 *
 * @param[in]  pObject  The object to destroy.
 **/
static uint32_t static_PKovioUserDestroyConnection(
            tContext* pContext,
            void* pObject )
{
   tPKovioUserConnection* pKovioUserConnection = (tPKovioUserConnection*)pObject;

   PDebugTrace("static_PKovioUserDestroyConnection");

   /* The driver connection is closed by the reader registry parent object */
   /* pKovioUserConnection->hDriverConnection */

   /* Free the Kovio connection structure */
   CMemoryFree( pKovioUserConnection );

   return P_HANDLE_DESTROY_DONE;
}


/**
 * @brief   Gets the Kovio connection property number.
 *
 * @param[in]  pContext  The context.
 *
 * @param[in]  pObject  The object.
 *
 * @param[in]  pPropertyArray  The property array.
 **/
static uint32_t static_PKovioUserGetPropertyNumber(
            tContext* pContext,
            void * pObject)
{
   return 1;
}


static bool_t static_PKovioUserGetProperties(
         tContext* pContext,
         void* pObject,
         uint8_t* pPropertyArray )
{
   PDebugTrace("static_PKovioUserGetProperties");

   pPropertyArray[0] = W_PROP_KOVIO;
   return W_TRUE;
}

static bool_t static_PKovioUseCheckProperty(
         tContext* pContext,
         void* pObject,
         uint8_t nPropertyValue )
{
   PDebugTrace(
      "static_PKovioUseCheckProperty: nPropertyValue= (0x%02X)", nPropertyValue  );

   return ( nPropertyValue == W_PROP_KOVIO )?W_TRUE:W_FALSE;

}

/**
 * @brief   Parses the Type Kovio card response at start up.
 *
 * @param[in]  pContext  The context.
 *
 * @param[in]  pKovioUserConnection  The pKovio user connection structure.
 *
 * @param[in]  pBuffer  The response buffer.
 *
 * @param[in]  nLength  The length of the response buffer.
 **/
static W_ERROR static_PKovioUserParseCardInfo(
            tContext* pContext,
            tPKovioUserConnection* pPKovioUserConnection,
            const uint8_t* pBuffer,
            uint32_t nLength )
{
   if(nLength > P_KOVIO_MAX_LENGTH)
   {
      PDebugWarning("static_PKovioUserParseCardInfo: data length truncated to %d bytes", P_KOVIO_MAX_LENGTH);
      nLength = P_KOVIO_MAX_LENGTH;
   }

   static_PKovioDecodeData(pPKovioUserConnection->aData,
               pBuffer,
               nLength);

   pPKovioUserConnection->nLength = nLength;

   return W_SUCCESS;
}

/* See Header file */
void PKovioUserCreateConnection(
            tContext* pContext,
            W_HANDLE hUserConnection,
            W_HANDLE hDriverConnection,
            tPBasicGenericCallbackFunction* pCallback,
            void* pCallbackParameter,
            uint8_t nProtocol,
            const uint8_t* pBuffer,
            uint32_t nLength )
{
   tPKovioUserConnection* pKovioUserConnection;
   tDFCCallbackContext sCallbackContext;
   W_ERROR nError;

   PDFCFillCallbackContext(
      pContext,
      (tDFCCallback*)pCallback,
      pCallbackParameter,
      &sCallbackContext );

   /* Create the Kovio buffer */
   pKovioUserConnection = (tPKovioUserConnection*)CMemoryAlloc( sizeof(tPKovioUserConnection) );
   if ( pKovioUserConnection == null )
   {
      PDebugError("PKovioUserCreateConnection: pKovioUserConnection == null");
      nError = W_ERROR_OUT_OF_RESOURCE;
      goto return_function;
   }
   CMemoryFill(pKovioUserConnection, 0, sizeof(tPKovioUserConnection));

   /* Parse the information */
   if ( ( nError = static_PKovioUserParseCardInfo(
                     pContext,
                     pKovioUserConnection,
                     pBuffer,
                     nLength ) ) != W_SUCCESS )
   {
      PDebugError( "PKovioUserCreateConnection: error while parsing the card information");
      goto return_function;
   }

   /* Add the Kovio structure */
   if ( ( nError = PHandleAddHeir(
                     pContext,
                     hUserConnection,
                     pKovioUserConnection,
                     P_HANDLE_TYPE_KOVIO_USER_CONNECTION ) ) != W_SUCCESS )
   {
      PDebugError("PKovioUserCreateConnection: could not add the Kovio Connection");
      /* Send the result */
      CMemoryFree(pKovioUserConnection);
      goto return_function;
   }

return_function:

   if(nError != W_SUCCESS)
   {
      PDebugError( "PKovioUserCreateConnection: returning %s",
         PUtilTraceError(nError) );
   }

   PDFCPostContext2(
         &sCallbackContext,
         nError );
}

static uint32_t static_PKovioGetIdentifierLength(
         tContext* pContext,
         void* pObject)
{
   tPKovioUserConnection* pPKovioUserConnection = (tPKovioUserConnection*)pObject;

   return pPKovioUserConnection->nLength;
}

/* Get identifier */
static void static_PKovioGetIdentifier(
         tContext* pContext,
         void* pObject,
         uint8_t* pIdentifierBuffer)
{
   tPKovioUserConnection* pPKovioUserConnection = (tPKovioUserConnection*)pObject;

   CMemoryCopy(pIdentifierBuffer, pPKovioUserConnection->aData, pPKovioUserConnection->nLength);
}

#endif /* P_CONFIG_USER || P_CONFIG_MONOLITHIC */

