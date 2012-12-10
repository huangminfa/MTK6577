/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

/*[
 *		Name:					mmi_if.h
 *
 *		Project:				OMC
 *
 *		Created On:				July 2004
 *
 *		Derived From:			//depot/main/base/omc/mmi/mmi_if.h#4
 *
 *		Version:				$Id: //depot/main/base/omc/mmi_if.h#10 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2006
 *
]*/

/*! \file
 *  \brief
 *		Interface with MMI code.
 */

#ifndef _OMC_MMI_MMI_IF_H_
#define _OMC_MMI_MMI_IF_H_

#ifdef __cplusplus
extern "C" {
#endif

/*===========================================================================*/
/*                    Defines                                                */
/*===========================================================================*/

/*! User commands */
typedef enum OMC_MMI_UserCmd_e
{
	/*!
	 * No command available from user.
	 */
	OMC_MMI_CMD_NONE = 0,

	/*!
	 * Command used to abort an action before it starts.
	 * Used with the screen \ref OMC_MMI_CONTINUE_ABORT_SCREEN.
	 */
	OMC_MMI_CMD_ABORT,

	/*!
	 * Command to indicate that the user wishes to continue with an action.
	 * It may also be used to indicate that the user has completed inputting
	 * data.
	 */
	OMC_MMI_CMD_CONTINUE,

	/*!
	 * Command used to indicate the user has cancelled, rather than respond
	 * with one of the expected responses.
	 */
	OMC_MMI_CMD_CANCEL,

	/*!
	 * Number of \a OMC_MMI_UserCmd defined.
	 */
	OMC_MMI_CMD_ID_COUNT

} OMC_MMI_UserCmd;


/*! Screen information */
typedef enum OMC_MMI_ScreenId_e
{
	/*!
	 * Undefined screen.
	 */
	OMC_MMI_UNDEFINED_SCREEN = 0,

	/*!
	 * Initial screen for the client.
	 * An information message to indicate the client has started
	 * should be displayed. No response from the user is required but
	 * \ref OMC_MMI_getCommand() must return \ref OMC_MMI_CMD_CONTINUE
	 * if it is called.
	 */
	OMC_MMI_INITIAL_SCREEN,

	/*!
	 * Authorising screen for the client.
	 * An information message to indicate the client has started
	 * authorising communications with the server. No response from
	 * the user is required but \ref OMC_MMI_getCommand() must return
	 * \ref OMC_MMI_CMD_CONTINUE if it is called.
	 */
	OMC_MMI_AUTH_SCREEN,

	/*!
	 * Authentication failure screen for the client.
	 * An information message to indicate the client has failed to
	 * authenticate with the server. The details of the failure are
	 * supplied by calling \ref OMC_MMI_setErrorDetails(). User
	 * acknowledgement required.
	 */
	OMC_MMI_AUTH_FAIL_SCREEN,

	/*!
	 * Session screen for the client.
	 * An information message to indicate the client has started
	 * a session with the server. No response from the user is required but
	 * \ref OMC_MMI_getCommand() must return \ref OMC_MMI_CMD_CONTINUE
	 * if it is called.
	 */
	OMC_MMI_IN_SESSION_SCREEN,

	/*!
	 * The message supplied by the server is to be displayed
	 * on the screen. No response from the user is required but
	 * \ref OMC_MMI_getCommand() must return \ref OMC_MMI_CMD_CONTINUE
	 * if it is called.
	 */
	OMC_MMI_SERVER_INFO_SCREEN,

	/*!
	 * Server supplied message to be displayed.
	 * User confirmation required, and one of the commands
	 * \ref OMC_MMI_CMD_ABORT or \ref OMC_MMI_CMD_CONTINUE should be
	 * returned when the client calls the function \ref OMC_MMI_getCommand().
	 * NB. This screen is only used by OMA DM.
	 */
	OMC_MMI_CONTINUE_ABORT_SCREEN,

	/*!
	 * Server supplied message to be displayed, requesting
	 * information from the user. User's entered details to
	 * be returned to client when it calls the function
	 * \ref OMC_MMI_getUserDetails().
	 * NB. This screen is only used by OMA DM.
	 */
	OMC_MMI_ENTER_DETAILS_SCREEN,

	/*!
	 * User offered a set of choices provided by the server,
	 * Only one option is allowed and should be returned to
	 * the client when it calls the function \ref OMC_MMI_getSelection().
	 * NB. This screen is only used by OMA DM.
	 */
	OMC_MMI_SINGLE_CHOICE_SCREEN,

	/*!
	 * User offered a set of choices provided by the server,
	 * User allowed to select more than one and these choices
	 * should be returned to the client when it calls the function
	 * \ref OMC_MMI_getSelection().
	 * NB. This screen is only used by OMA DM.
	 */
	OMC_MMI_MULTIPLE_CHOICE_SCREEN,

	/*!
	 * Display text to indicate that OMC has finished
	 * and is about to exit after a failure. The details of the error
	 * are supplied by calling \ref OMC_MMI_setErrorDetails(). User
	 * acknowledgement required.
	 */
	OMC_MMI_EXIT_FAIL_SCREEN,

	/*!
	 * Display text to indicate that OMC has finished
	 * and is about to exit after a successful exchange. User
	 * acknowledgement required.
	 */
	OMC_MMI_EXIT_OK_SCREEN,

	/*!
	 * Display details of what caused a sync session to fail. The details
	 * of the error are supplied by calling \ref OMC_MMI_setErrorDetails().
	 * User acknowledgement required.
	 * NB. This screen is only used by OMA DS.
	 */
	OMC_MMI_SYNC_FAIL_SCREEN,

	/*! Number of \a OMC_MMI_ScreenId defined. */
	OMC_MMI_SCREEN_ID_COUNT

} OMC_MMI_ScreenId;


/*! Echo type information for input screens */
typedef enum
{
	/*!
	 * Undefined echo type.
	 */
	OMC_MMI_ECHO_UNDEFINED		= 0,

	/*!
	 * Anything entered by the user should appear on screen as normal.
	 */
	OMC_MMI_ECHO_TEXTUAL		= 1,

	/*!
	 * Anything entered by the user should appear on screen in an obsured
	 * form. This is only used to obscure passwords which are generally
	 * echoed as asterisks.
	 */
	OMC_MMI_ECHO_PASSWORD		= 2

} OMC_MMI_EchoType;


/*! Input type information for input screens */
typedef enum
{
	/*!
	 * Undefined input type.
	 */
	OMC_MMI_INPUT_UNDEFINED		= 0,

	/*!
	 * The user should be able to input all alphanumeric characters.
	 */
	OMC_MMI_INPUT_ALPHANUMERIC	= 1,

	/*!
	 * The user should only be able to input numeric characters (including
	 * a sign and/or a decimal point).
	 */
	OMC_MMI_INPUT_NUMERIC		= 2,

	/*!
	 * The input is required to be in the form of a date.
	 */
	OMC_MMI_INPUT_DATE			= 3,

	/*!
	 * The input is required to be in the form of a time.
	 */
	OMC_MMI_INPUT_TIME			= 4,

	/*!
	 * The input is required to be in the form of a phone number. Allowed
	 * characters are numeric characters, '+', 'p', 'w' and 's'.
	 */
	OMC_MMI_INPUT_PHONE			= 5,

	/*!
	 * The input is required to be in the form of an IP address. Allowed
	 * characters are numeric characters and '.'.
	 */
	OMC_MMI_INPUT_IPADDRESS		= 6

} OMC_MMI_InputType;


/*! Structure containing display options for DM UI screens */
typedef struct OMC_MMI_screenOptions_s
{
	/*!
	 * Specifies a minimum display time for a screen in seconds. 0 is used to
	 * indicate 'not set'.
	 */
	IU32				minDT;

	/*!
	 * Specifies a maximum display time for a screen in seconds. 0 is used to
	 * indicate 'not set'.
	 */
	IU32				maxDT;

	/*!
	 * Specifies the maximum number of characters a user is allowed to enter
	 * where user input is requested.  0 is used to indicate 'not set'.
	 */
	IU32				maxLen;

	/*!
	 * Specifies the echo type to be usd for user input. The value of
	 * OMC_MMI_ECHO_UNDEFINED is used if the screen does not allow the user
	 * to enter data.
	 */
	OMC_MMI_EchoType	echoType;

	/*!
	 * Specifies the input type to be usd for user input. The value of
	 * OMC_MMI_INPUT_UNDEFINED is used if the screen does not allow the user
	 * to enter data.
	 */
	OMC_MMI_InputType	inputType;

	/*!
	 * If the screen requires a user to enter data, this will contain the
	 * default response. This will be NULL if there is no default response.
	 */
	UTF8Str				defaultResponse;

	/*!
	 * If a confirmation screen is to be shown this will be set to indicate
	 * a default response. OMC_MMI_CMD_NONE will be used if there is no
	 * default response.
	 */
	OMC_MMI_UserCmd		defaultCmd;

} OMC_MMI_ScreenOptions, *OMC_MMI_ScreenOptionsPtr;



typedef struct OMC_MMI_ChoiceList_s
{
	UTF8Str		choice;
	IBOOL		selected;
} OMC_MMI_ChoiceList, *OMC_MMI_ChoiceListPtr;



/* Restrict displayable text to 255 characters */
#define OMC_MMI_IMPL_LEN 256

typedef struct OMC_MMI_Data_s
{
	IU32		maxProgress;
	IU32		currProgress;
	IU32		len;			/* Number of options in a choices UI */
	IU32		selected;		/* Bitmap indicating the choices selected */
	UTF8CStr	*ptr;			/* Array of strings comprising the choices
								 * available in a choices UI. */

	/* A piece of information to display (or a title for a choices UI). */
	IU8			displayText[OMC_MMI_IMPL_LEN];

	/* Error details */
	OMC_Error	errorCode;
	IU32		resultCode;
	UTF8CStr	datastoreName;
	UTF8CStr	commandName;
	UTF8CStr	explanation;

	/* Data to be returned later. */
	OMC_MMI_UserCmd usrcmd;		/* command from user */
	UTF8Str			usrstr;		/* string received from user */
	IU32			usrlen;		/* length of string */
	IU32			usrsel;		/* selected choice mask from user */
} OMC_MMI_Data, *OMC_MMI_DataPtr;



/*===========================================================================*/
/*                           Function Prototypes                             */
/*                                                                           */
/*                               Porting API                                 */
/*===========================================================================*/
/*!
================================================================================
 * Perform MMI initialization. This will be called before any other MMI
 * function is called. This MUST NOT cause anything to be displayed.
 *
 * \param	pUser	Pointer to user data for porting.
 *
 * \return	OMC_ERR_OK if initialization was successful.  Can return
 * 			OMC_ERR_YIELD or OMC_ERR_WAIT if it needs to do so, or any other
 * 			error for failure.
================================================================================
 */
extern OMC_Yield OMC_MMI_init(OMC_UserDataPtr pUser);


/*!
================================================================================
 * Perform MMI termination. No MMI functions will be called after this has been
 * called, except for perhaps \ref OMC_MMI_init() to reinitialize the MMI
 * system
 *
 * \param	pUser	Pointer to user data for porting.
 *
 * \return	OMC_ERR_OK if termination was successful.  Can return OMC_ERR_YIELD
 * 			or OMC_ERR_WAIT if it needs to do so, or any other error for
 * 			failure.
================================================================================
 */
extern OMC_Yield OMC_MMI_term(OMC_UserDataPtr pUser);


/*!
================================================================================
 * Set the new MMI screen. This should in turn lead to the relevant screen
 * being displayed as soon as possible after the function is called.
 *
 * For screens that require no response from the user (such as
 * \ref OMC_MMI_SERVER_INFO_SCREEN) it must be arranged that
 * \ref OMC_MMI_getCommand() will return \ref OMC_MMI_CMD_CONTINUE
 * if it is called.
 *
 * \param	pUser		Pointer to user data for porting.
 * \param	screenId	The new screen identifier.
 * \param	optPtr		The display options for this screen. For non-DM
 *						originated screens (alerts) this parameter will be
 *						NULL.
 *
 * \return	OMC_ERR_OK if successful.  Can return OMC_ERR_YIELD or OMC_ERR_WAIT
 *			if it needs to do so, or any other error for failure.
================================================================================
 */
extern OMC_Yield OMC_MMI_setScreen(OMC_UserDataPtr pUser,
								   OMC_MMI_ScreenId screenId,
								   OMC_MMI_ScreenOptionsPtr optPtr);


/*!
================================================================================
 * Set the text to be displayed in screens \ref OMC_MMI_CONTINUE_ABORT_SCREEN
 * and \ref OMC_MMI_ENTER_DETAILS_SCREEN.
 * This will be called before \ref OMC_MMI_setScreen() is called.
 * The text must be copied before returning from this function.
 *
 * \param	pUser		Pointer to user data for porting.
 * \param	text		Text to be displayed.
 *
 * \return An error if one occurred (or OMC_ERR_OK if successful)
================================================================================
 */
extern OMC_Error OMC_MMI_setDisplayText(OMC_UserDataPtr pUser,
										UTF8CStr text);


/*!
================================================================================
 * Set the option list to be displayed in screens
 * \ref OMC_MMI_SINGLE_CHOICE_SCREEN and \ref OMC_MMI_MULTIPLE_CHOICE_SCREEN.
 * This will be called before \ref OMC_MMI_setScreen() is called.
 *
 * \param	pUser		Pointer to user data for porting.
 * \param	listPtr		Pointer to the first choice in the list. The options
 *						strings must be copied before returning from this
 *						function.
 * \param	selection	A 32bit value, where bit 0 refers to the first choice
 *						and bit 1 refers to the second choice etc. If a bit is
 *						set, then the choice should default to selected in the
 *						user interface.
 * \param	count		Number of choices in the list.
 *
 * \return An error if one occurred (or OMC_ERR_OK if successful)
================================================================================
 */
extern OMC_Error OMC_MMI_setOptionList(OMC_UserDataPtr pUser,
									   UTF8CStr *listPtr,
									   IU32 selection,
									   IU32 count);


/*!
================================================================================
 * Set the details of the error to be displayed in screens
 * \ref OMC_MMI_AUTH_FAIL_SCREEN, \ref OMC_MMI_EXIT_FAIL_SCREEN and
 * \ref OMC_MMI_SYNC_FAIL_SCREEN.
 * This will be called before \ref OMC_MMI_setScreen() is called.
 * Any strings supplied must be copied before returning from this function.
 *
 * Note that this function cannot fail. It is up to the implementation
 * to do whatever is needed to allow \ref OMC_MMI_setScreen() to be called
 * if there is a problem storing the error details.
 *
 * \param	pUser			Pointer to user data for porting.
 * \param	errorCode		OMC error code.
 * \param	datastoreName	The name of the datastore that failed (if
 *							OMC_MMI_SYNC_FAIL_SCREEN) or NULL.
 * \param	commandName		The name of the command that failed (eg "Sync") or
 *							NULL.
 * \param	resultCode		The result code returned by the server or 0.
 * \param	explanation		The explanatory text returned by the server or NULL.
================================================================================
 */
extern void OMC_MMI_setErrorDetails(OMC_UserDataPtr pUser,
										 OMC_Error errorCode,
										 UTF8CStr datastoreName,
										 UTF8CStr commandName,
										 IU32 resultCode,
										 UTF8CStr explanation);


/*!
================================================================================
 * Set the current value for a progress gauge. If the value is greater than or
 * equal to the maximum setting, 100% progress should be displayed. This will be
 * called before and after \ref OMC_MMI_setScreen() is called. The screen should
 * be updated to reflect the new value as soon as possible.
 *
 * \param	pUser			Pointer to user data for porting.
 * \param	currentProgress	The current download progress value.
================================================================================
 */
extern void OMC_MMI_setCurrentProgress(OMC_UserDataPtr pUser,
									   IU32 currentProgress);


/*!
================================================================================
 * Set the maximum value for a progress gauge.
 * This will be called before \ref OMC_MMI_setScreen() is called.
 *
 * \param	pUser			Pointer to user data for porting.
 * \param	maxProgress		The download progress maximum value.
================================================================================
 */
extern void OMC_MMI_setMaxProgress(OMC_UserDataPtr pUser,
								   IU32 maxProgress);


/*===========================================================================
 * Functions called by OMC to fetch data from the MMI implementation.
 *===========================================================================*/

/*!
================================================================================
 * This will be called to test whether a user command is available.
 * When a command is available the "user details" string or the 'selection'
 * options value will be available as appropriate for the selected screen.
 * It is possible to return OMC_ERR_OK with the command OMC_MMI_CMD_NONE if the
 * user has not responded (rather than a yield or a wait).
 *
 * \param	pUser	Pointer to user data for porting.
 * \param	pCmd	Pointer to place to put command received from user.
 *
 * \return		OMC_ERR_OK if command available.  May return OMC_ERR_YIELD or
 * 				OMC_ERR_WAIT if more time is needed to get the information, or
 * 				any other error for failure.
================================================================================
 */
extern OMC_Yield OMC_MMI_getCommand(OMC_UserDataPtr pUser,
									OMC_MMI_UserCmd *pCmd);


/*!
================================================================================
 * This will be called to check whether a user-entered string is available and
 * if so to copy it into the OMC's data area.  If called with the string
 * parameter set to NULL or with the length parameter set to zero no copying
 * will be done, the length of the data area needed to hold the string
 * (including the trailing nul character) must be returned, or zero if there
 * is no user data available.
 *
 * It will be valid once OMC_MMI_getCommand has returned OMC_ERR_OK, up until
 * OMC_MMI_setScreen is next called.
 *
 * \param	pUser		Pointer to user data for porting.
 *
 * \param	string		This is a string to contain the information entered
 *                      by the user in an input screen.
 *						It should be formatted by the following rules:
 *						1) If the input type is OMC_MMI_INPUT_DATE the string
 *							should be formatted as DDMMYYYY
 *						2) If the input type is OMC_MMI_INPUT_TIME the string
 *							should be formatted as HHMMSS
 *						3) If the input type is OMC_MMI_INPUT_IPADDR the string
 *							should be formatted as www.xxx.yyy.zzz
 *						4) If the input type is OMC_MMI_INPUT_PHONE the string
 *							should contain only digits, '+', 'p', 'w', 's'. If
 *							'+' is present, it must be the first character.
 *						5) If the input type is OMC_MMI_INPUT_NUMERIC the
 *							string should only contain digits, '-', '+' and
 *							'.'.
 *						6) For OMC_MMI_INPUT_ALPHANUMERIC the string may
 *							contain all alphanumeric characters.
 *
 *  \param	length		Size of the string area passed in.  If this is zero then
 *  					no copying will be done and the function will return the
 *  					size needed to hold the string (including its
 *  					terminating nul).
 *
 * \return Length of user string including terminating nul character.
================================================================================
 */
extern IU32 OMC_MMI_getUserDetails(OMC_UserDataPtr pUser,
								   UTF8Str         string,
								   IU32            length);


/*!
================================================================================
 * Call to get the user's selection from a list.
 *
 * It will be valid once OMC_MMI_getCommand has returned OMC_ERR_OK, up until
 * OMC_MMI_setScreen is next called.
 *
 * \param	pUser	Pointer to user data for porting.
 *
 * \return	A 32 bit value, where each bit represents one of the
 *			choices the user was presented with. Bit 0 relates to
 *			choice 1, bit 2 relates to choice 2 etc.
 *			If the user has selected a choice, the appropriate bit
 *			(or bits for multiple choices) will be set to 1.
================================================================================
 */
extern IU32 OMC_MMI_getSelection(OMC_UserDataPtr pUser);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* _OMC_MMI_MMI_IF_H_ */
