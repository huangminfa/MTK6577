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
 *		Name:					omc_timeslice.h
 *
 *		Project:				OMC
 *
 *		Created On:				August 2005
 *
 *		Derived From:			//depot/main/base/ssp/da/timeslice.h#10
 *
 *		Version:				$Id: //depot/main/base/omc/omc_timeslice.h#10 $
 *
 *		Coding Standards:		3.0
 *
 *		Purpose:				Time slicing support.
 *
 *			This file contains the definition of the Insignia time slicing
 *			macros. These are used to break up a long task into smaller
 *			chunks as required by some mobile operating systems.
 *
 *		(c) Copyright Insignia Solutions plc, 2003 - 2005
 *
]*/

/*! \file
 *		Defines the macros used for time slicing support.
 *
 * \brief	Time slicing
 */

#ifndef _OMC_OMC_TIMESLICE_H_
#define _OMC_OMC_TIMESLICE_H_

#include <omc/omc_debug.h>	/* For OMC_assert() */

/*
 * Time slicing - An introduction
 * ==============================
 *
 * The time slicing support turns every slicing function into a state
 * machine but allows the code to be written as if was simply procedural.
 * When a function yields it returns an error code to its caller, which
 * saves its state and returns the error code to its caller, and so on.
 * When it is time to run again each function jumps to where it was
 * last executing and re-calls the function it was calling, which jumps
 * to where it was last executing and re-calls the function it was
 * calling, and so on.
 *
 * The mechanism used to run these state machines is to make the body of
 * each function into a giant switch statement with the points where the
 * function can yield being cases in this switch. (The macros hide almost
 * all this implementation.) However, this means that slicing functions
 * cannot yield from within a switch statement in the code (although it is
 * still possible to use switch statements as long as there is no yield
 * in any of the cases). This means that some switch statements will have
 * to be turned into a series of if statments in some cases. Also, the
 * function state is recorded in the session data structure hence slicing
 * functions cannot be recursive.
 *
 * A slicing function has return type SLICE. This is effectively the
 * same as returning OMC_Yield (except that the checking code below
 * can fiddle with this to help catch coding errors). There are special
 * yield and wait error codes that slicing functions return to indicate
 * that they have not completed execution and need to be called again.
 * These error codes are handled by the slicing macros. It is the caller's
 * responsibility to ensure that the same arguments are passed to the
 * function on each call. This may require the arguments to be state
 * variables.
 *
 * Time slicing support also allows for functions that can return yield
 * or wait but that are not slicing functions. These are termed NONSLICE
 * functions. It may be the case that these functions need to use time
 * slicing themselves. In this case the function should use YIELD
 * variants of the SLICE macros described below.
 *
 * The state variables are stored in structures in the session data.
 * Access to these is via the SV() macro which requires the defines
 * SLICE_SESSION and SLICE_VARS to be set to the pointer to the session
 * data and the name of the structure in the session data which is used
 * to store the state variables. (The name of the structure is usually
 * the name of the function to help ensure the name is unique.) Every
 * slicing function has one state variable which is the current state of
 * the state machine. Use the macro DEF_SLICE to define this in the state
 * variables. Other variables may be needed if values need to be
 * preserved across a slicing call or yield. There are also some variables
 * which need to be defined in the session data. Use the macro
 * DEF_SLICE_SESSION (without a trailing semicolon) to define these.
 *
 * The first instruction in a slicing function should be the SLICE_START()
 * macro. It is possible to have code before this but this will be executed
 * every time the function is called, even on calls after the function
 * has yielded.
 *
 * The last instruction in a slicing function must be the SLICE_END()
 * macro. This takes an error code to return as an argument. (This must
 * never be a yield or wait error code.)
 *
 * It is possible to return from a function before the end and the
 * SLICE_EXIT() macro does this. This takes an error code to return
 * just like the SLICE_END() macro. A simple return statement must never
 * be used. Beware that the SLICE_EXIT() macro does not require a
 * semicolon after it and if used in an if expression without being in
 * a block then this "unwanted" semicolon can separate an if from its
 * else and cause a compilation error.
 *
 * To call a slicing function from a slicing function the SLICE_CALL()
 * macro is used. The first argument of this macro is a number that must
 * be non-zero and unique in this function. The second argument is the
 * variable in which to store the error code returned by the function.
 * This is used by the macro when detecting yield and wait requests which
 * are always handled inside the macro. When the function returns a
 * non-slicing error code it is available for the caller to inspect. The
 * third argument is the function call to make. One argument of a slicing
 * function, and it is conventionally the first, is a pointer to the
 * session data.
 *
 * To call a function that can return yield or wait error codes but
 * that is not itself a slicing function (such as API functions), the
 * NONSLICE_CALL() macro is used. This takes the same arguments as the
 * SLICE_CALL() macro.
 *
 * There are also macros SLICE_YIELD() and SLICE_WAIT() that perform
 * an explicit yield or wait.  If a slicing function runs a long loop
 * then it is recommended that one of these is used at some point in
 * the loop. The difference between them is that yield is used if the
 * function wants to run again at the earliest opportunity while wait
 * is used if the program is waiting for some external event (such as
 * a keypress) to occur before it can do any further work.
 *
 * So an example slicing function might look like:
 *
 * LOCAL SLICE getName(OMC_SessionDataPtr sdp, UTF8Str* name)
 * {
 * #define SLICE_SESSION sdp
 * #define SLICE_VARS    getName
 *
 *     OMC_Yield error;
 *
 *     SLICE_START();
 *
 *     SLICE_CALL(10, error, fetchNameLength(sdp, &SV(length)));
 *
 *     if ( SV(length) > MAX_NAME_LENGTH )
 *     {
 *         SLICE_EXIT(OMC_ERR_NAME_TOO_LONG);
 *     }
 *
 *     *name = OMC_malloc(SV(length + 1);
 *
 *     if ( *name == NULL )
 *     {
 *         SLICE_EXIT(OMC_ERR_MEMORY);
 *     }
 *
 *     SLICE_CALL(20, error, fetchName(sdp, *name, SV(length)));
 *
 *     SLICE_END(error);
 * #undef SLICE_VARS
 * #undef SLICE_SESSION
 * }
 */


/*
 * Macro to access a function's current state variable.
 */
#define	SLICE_STATE_NAME			sTaTe
#define	SLICE_STATE()				SV(SLICE_STATE_NAME)


/*
 * Define CHECK_SLICE_USAGE to enable code which attempts to police
 * some of the requirements for TIME_SLICING.
 */
#if defined(TIME_SLICING) && defined(CHECK_SLICE_USAGE)

/*
 * The time slicing support requires certain conventions to be followed
 * when writing code, or else things may go horribly wrong. In order to try
 * and catch code which doesn't obey these conventions, this extra code can
 * be enabled. Some of the things to try and catch are:
 *
 * - Recursively calling a function.
 * If a function calls back into itself (even indirectly) then it will
 * immediately jump to where it was last executing rather than starting
 * again from the beginning.
 *
 * - Not calling a SLICING function with a SLICE_CALL() macro.
 * The SLICE_CALL() macro does several house-keeping chores such as storing
 * where the calling function has got to when the called function returns
 * a yield request.
 *
 * - Returning from a SLICE function using a normal 'return' statement.
 * The SLICE_EXIT() or SLICE_END() macros do some house-keeping chores such
 * as resetting execution to the start of the function as well as exiting
 * the function.
 *
 * How it works
 * ============
 *
 * The first time a SLICE_CALL() is made, it sets the check value, while
 * subsequent calls do not. The SLICE_START() macro checks this on entry
 * and ensures that either the check value is set and this is the first
 * time it has been called - the slice state for the function is zero -
 * or the slice value is not set and the  slice state is non-zero.
 * Together these checks allow us to ensure the functions are only called
 * via SLICE_CALL() and are not called in a recursive manner. Note that
 * these are runtime tests and will not warn when incorrect code is
 * compiled.
 *
 * The second part of the checking makes sure no-one uses a simple return
 * to exit a SLICE function. This is simply done by changing the type of
 * the return value for SLICE functions. Incompatible type compiler
 * warnings will be generated if a plain return is used to exit a slicing
 * function.
 */

#define CSU_MAGIC_CALL 		0xCA110B0B
#define CSU_MAGIC_RUN		0x105E0AB1

#define	CSU_CHECK_NAME		cHeCk
#define	CSU_RETURN_NAME		rEtUrN

#define CSU_DEF_SESSION													\
IU32		CSU_CHECK_NAME;												\
OMC_Yield	CSU_RETURN_NAME;

#define CSU_BEGIN_SLICING()												\
if ( 0 == SLICE_SESSION->CSU_CHECK_NAME ) {								\
	SLICE_SESSION->CSU_CHECK_NAME = CSU_MAGIC_CALL;						\
}

#define CSU_END_SLICING(ERROR)											\
if ( !IS_SLICE_REQUESTED(ERROR) ) {										\
	OMC_assert(CSU_MAGIC_RUN == SLICE_SESSION->CSU_CHECK_NAME);			\
	SLICE_SESSION->CSU_CHECK_NAME = 0;									\
}

#define CSU_SET_CALL()													\
SLICE_SESSION->CSU_CHECK_NAME = CSU_MAGIC_CALL

#define CSU_CHECK_CALL()												\
OMC_assert( (0 == SLICE_STATE()) ?										\
	(CSU_MAGIC_CALL == SLICE_SESSION->CSU_CHECK_NAME) :					\
	(CSU_MAGIC_RUN  == SLICE_SESSION->CSU_CHECK_NAME) )

#define CSU_CLEAR_CALL()												\
SLICE_SESSION->CSU_CHECK_NAME = CSU_MAGIC_RUN

#define CSU_STUB_CHECK()												\
OMC_assert( CSU_MAGIC_CALL == SLICE_SESSION->CSU_CHECK_NAME )

#define CSU_SLICE_CALL(ERROR, FUNCTION)									\
ERROR = *FUNCTION

#define CSU_SLICE_RETURN(ERROR)											\
SLICE_SESSION->CSU_RETURN_NAME = ERROR;									\
return &SLICE_SESSION->CSU_RETURN_NAME

#else

#define CSU_DEF_SESSION
#define CSU_BEGIN_SLICING()
#define CSU_END_SLICING(ERROR)
#define CSU_SET_CALL()
#define CSU_CHECK_CALL()
#define CSU_CLEAR_CALL()

#define CSU_STUB_CHECK()												\
UNUSED(SLICE_SESSION)

#define CSU_SLICE_CALL(ERROR, FUNCTION)									\
ERROR = FUNCTION

#define CSU_SLICE_RETURN(ERROR)											\
return ERROR

#endif

#define CSU_NONSLICE_CALL(ERROR, FUNCTION)								\
ERROR = FUNCTION

#define CSU_NONSLICE_RETURN(ERROR)										\
return ERROR

#define CSU_CHECK_RETURN(ERROR)											\
OMC_assert(!IS_SLICE_REQUESTED(ERROR))


/*
 * The macros SLICE_YIELD_SLEEP and SLICE_WAIT_SLEEP are used when not
 * time slicing to sleep for a short time at all points where there
 * would be a yield when time slicing. Default versions are supplied
 * here that do not sleep (and hence will cause busy waiting to occur).
 * This can be overridden to cause a sleep to occur.
 */
#ifndef SLICE_YIELD_SLEEP
#define SLICE_YIELD_SLEEP
#endif
#ifndef SLICE_WAIT_SLEEP
#define SLICE_WAIT_SLEEP
#endif



/*
 * Not defining TIME_SLICING removes the code to support time slicing.
 *
 * Defining DEBUG_TIME_SLICING switches the time slicing macros to
 * versions that never return (although they may sleep). This is
 * particularly useful when intending to single step through a
 * slicing function in a debugger.
 */
#ifdef TIME_SLICING /* { */

#define	TS_SET_STATE(CASE)												\
SLICE_STATE() = CASE

#define	TS_CASE(CASE)													\
case CASE:

#ifdef DEBUG_TIME_SLICING /* { */

#ifdef SLICE_SLEEP_INCLUDE
#include SLICE_SLEEP_INCLUDE
#endif

#define	TS_DO_SLICE_YIELD()												\
SLICE_YIELD_SLEEP

#define	TS_DO_YIELD_YIELD()												\
SLICE_YIELD_SLEEP

#define	TS_DO_SLICE_WAIT()												\
SLICE_WAIT_SLEEP

#define	TS_DO_YIELD_WAIT()												\
SLICE_WAIT_SLEEP

#define	TS_SLICE_CHECK_YIELD(CASE, ERROR)								\
OMC_assert( !IS_SLICE_REQUESTED(ERROR) )

#define	TS_YIELD_CHECK_YIELD(CASE, ERROR)								\
OMC_assert( !IS_SLICE_REQUESTED(ERROR) )

#else /* DEBUG_TIME_SLICING }{ */

#define	TS_DO_SLICE_YIELD()												\
CSU_SLICE_RETURN(OMC_ERR_YIELD)

#define	TS_DO_YIELD_YIELD()												\
CSU_NONSLICE_RETURN(OMC_ERR_YIELD)

#define	TS_DO_SLICE_WAIT()												\
CSU_SLICE_RETURN(OMC_ERR_WAIT)

#define	TS_DO_YIELD_WAIT()												\
CSU_NONSLICE_RETURN(OMC_ERR_WAIT)

#define	TS_SLICE_CHECK_YIELD(CASE, ERROR)								\
if ( IS_SLICE_REQUESTED(ERROR) )										\
{																		\
	TS_SET_STATE(CASE);													\
	CSU_SLICE_RETURN(ERROR);											\
}

#define	TS_YIELD_CHECK_YIELD(CASE, ERROR)								\
if ( IS_SLICE_REQUESTED(ERROR) )										\
{																		\
	TS_SET_STATE(CASE);													\
	CSU_NONSLICE_RETURN(ERROR);											\
}

#endif /* DEBUG_TIME_SLICING } */

#else /* TIME_SLICING }{ */

#define	TS_SET_STATE(CASE)
#define	TS_CASE(CASE)
#define	TS_DO_SLICE_YIELD()
#define	TS_DO_YIELD_YIELD()
#define	TS_DO_SLICE_WAIT()
#define	TS_DO_YIELD_WAIT()
#define	TS_SLICE_CHECK_YIELD(CASE, ERROR)
#define	TS_YIELD_CHECK_YIELD(CASE, ERROR)

#endif /* TIME_SLICING } */


#if defined(TIME_SLICING) && defined(CHECK_SLICE_USAGE) && \
	!defined(DEBUG_TIME_SLICING)

#define	TS_DEBUG_SLICE_YIELD(CASE)		SLICE_YIELD(-CASE);
#define	TS_DEBUG_YIELD_YIELD(CASE)		YIELD_YIELD(-CASE);

#else

#define	TS_DEBUG_SLICE_YIELD(CASE)
#define	TS_DEBUG_YIELD_YIELD(CASE)

#endif


/*******************************************************
 ***** The external time slicing macros start here *****
 *******************************************************/

/*
 * Macro used to access persistent local variables.
 *
 * Note that SLICE_VARS is usually defined to include the name of the
 * function. So an example usage would be:
 *
 * LOCAL SLICE getName(OMC_SessionDataPtr sdp, UTF8Str name)
 * {
 * #define SLICE_SESSION sdp
 * #define SLICE_VARS    getName
 *   ...
 *   SV(length) = getLength();
 *   ...
 * #undef SLICE_VARS
 * #undef SLICE_SESSION
 * }
 */
#define	SV(NAME)					SLICE_SESSION->SLICE_VARS.NAME

/*
 * Macros to define a function's current state variable.
 */
#define DEF_YIELD					int SLICE_STATE_NAME
#define DEF_SLICE					DEF_YIELD

/*
 * Macro to define session data slice usage variables.
 */
#define DEF_SLICE_SESSION			CSU_DEF_SESSION

/*
 * Macro to enter the slicing code.
 */
#define SLICE_TOP_LEVEL(ERROR, FUNCTION)								\
do {																	\
	CSU_BEGIN_SLICING();												\
	CSU_SLICE_CALL(ERROR, FUNCTION);									\
	CSU_END_SLICING(ERROR);												\
} while ( 0 )

/*
 * Macro used at the start of all slicing functions.
 *
 * Any code before this will get run every time the function
 * is executed even if it is resuming after a yield.
 *
 * Note: It is not possible to using do {...} while (0) around these
 * instructions as they start but do not end a block!
 */
#ifdef TIME_SLICING

#define	SLICE_START()													\
CSU_CHECK_CALL();														\
switch ( SLICE_STATE() )												\
{																		\
default:																\
TS_SET_STATE(0);														\
CSU_SLICE_RETURN(OMC_ERR_INTERNAL);										\
case 0:																	\
CSU_CLEAR_CALL()

#define	YIELD_START()													\
switch ( SLICE_STATE() )												\
{																		\
default:																\
TS_SET_STATE(0);														\
CSU_NONSLICE_RETURN(OMC_ERR_INTERNAL);									\
case 0:

#else

#define	SLICE_START()
#define	YIELD_START()

#endif

/*
 * Macro used at the end of all slicing functions.
 *
 * There can be no code after this macro.
 *
 * Note: It is not possible to using do {...} while (0) around these
 * instructions as they end but do not start a block!
 */
#ifdef TIME_SLICING

#define	SLICE_END(ERROR)												\
}																		\
TS_SET_STATE(0);														\
CSU_CHECK_RETURN(ERROR);												\
CSU_SLICE_RETURN(ERROR)

#define	YIELD_END(ERROR)												\
}																		\
TS_SET_STATE(0);														\
CSU_CHECK_RETURN(ERROR);												\
CSU_NONSLICE_RETURN(ERROR)

#else

#define	SLICE_END(ERROR)												\
CSU_CHECK_RETURN(ERROR);												\
CSU_SLICE_RETURN(ERROR)

#define	YIELD_END(ERROR)												\
CSU_CHECK_RETURN(ERROR);												\
CSU_NONSLICE_RETURN(ERROR)

#endif


/*
 * Macro to exit a slicing function prematurely.
 *
 * Note: Using do {...} while (0) around these instructions produces
 * unreachable code errors on some compilers.
 *
 * Beware: Not having do {...} while (0) around these instructions
 * means that the semicolon following the macro is an empty statement.
 * This can cause an else to become detached from its if and result
 * in compilation errors. It is best to put the macro inside a block
 * in such cases.
 */
#define	SLICE_EXIT(ERROR)												\
{																		\
	TS_SET_STATE(0);													\
	CSU_CHECK_RETURN(ERROR);											\
	CSU_SLICE_RETURN(ERROR);											\
}

#define	YIELD_EXIT(ERROR)												\
{																		\
	TS_SET_STATE(0);													\
	CSU_CHECK_RETURN(ERROR);											\
	CSU_NONSLICE_RETURN(ERROR);											\
}

/*
 * Macro to yield then continue running a slicing function.
 */
#define SLICE_YIELD(CASE)												\
do {																	\
	TS_SET_STATE(CASE);													\
	TS_DO_SLICE_YIELD();												\
	TS_CASE(CASE);														\
} while ( 0 )

/*
 * Macro to yield then continue running a yielding function.
 */
#define YIELD_YIELD(CASE)												\
do {																	\
	TS_SET_STATE(CASE);													\
	TS_DO_YIELD_YIELD();												\
	TS_CASE(CASE);														\
} while ( 0 )


/*
 * Macro to wait for an external kick then continue running a slicing
 * function.
 */
#define SLICE_WAIT(CASE)												\
do {																	\
	TS_SET_STATE(CASE);													\
	TS_DO_SLICE_WAIT();													\
	TS_CASE(CASE);														\
} while ( 0 )

/*
 * Macro to wait for an external kick then continue running a yielding
 * function.
 */
#define YIELD_WAIT(CASE)												\
do {																	\
	TS_SET_STATE(CASE);													\
	TS_DO_YIELD_WAIT();													\
	TS_CASE(CASE);														\
} while ( 0 )


/*
 * Macro to yield while a condition that will be changed externally
 * is true.
 */
#define SLICE_WAIT_WHILE(CASE, TEST)									\
do {																	\
	TS_DEBUG_SLICE_YIELD(CASE)											\
	TS_CASE(CASE);														\
	while ( TEST )														\
	{																	\
		TS_SET_STATE(CASE);												\
		TS_DO_SLICE_WAIT();												\
	}																	\
} while ( 0 )

/*
 * Macro to call a slicing function from a slicing function.
 */
#define SLICE_CALL(CASE, ERROR, FUNCTION)								\
do {																	\
	TS_DEBUG_SLICE_YIELD(CASE)											\
	CSU_SET_CALL();														\
	TS_CASE(CASE);														\
	CSU_SLICE_CALL(ERROR, FUNCTION);									\
	TS_SLICE_CHECK_YIELD(CASE, ERROR);									\
} while ( 0 )

/*
 * Macro to call a non-slicing function that can yield from a
 * slicing function.
 */
#define NONSLICE_CALL(CASE, ERROR, FUNCTION)							\
do {																	\
	TS_DEBUG_SLICE_YIELD(CASE)											\
	TS_CASE(CASE);														\
	CSU_NONSLICE_CALL(ERROR, FUNCTION);									\
	TS_SLICE_CHECK_YIELD(CASE, ERROR);									\
} while ( 0 )

/*
 * Macro to call a slicing function from a non-slicing function
 * that can yield.
 */
#define YIELD_SLICE_CALL(CASE, ERROR, FUNCTION)							\
do {																	\
	TS_DEBUG_YIELD_YIELD(CASE)											\
	CSU_SET_CALL();														\
	TS_CASE(CASE);														\
	CSU_SLICE_CALL(ERROR, FUNCTION);									\
	TS_YIELD_CHECK_YIELD(CASE, ERROR);									\
} while ( 0 )

/*
 * Macro to call a non-slicing function that can yield from a
 * non-slicing function that can yield.
 */
#define YIELD_NONSLICE_CALL(CASE, ERROR, FUNCTION)						\
do {																	\
	TS_DEBUG_YIELD_YIELD(CASE)											\
	TS_CASE(CASE);														\
	CSU_NONSLICE_CALL(ERROR, FUNCTION);									\
	TS_YIELD_CHECK_YIELD(CASE, ERROR);									\
} while ( 0 )


/*
 * Macro for returning a value from a stub (ie empty) slicing
 * function. (Note that SLICE_SESSION must be defined but that
 * SLICE_VARS is not needed.)
 */
#define	SLICE_STUB(ERROR)												\
CSU_STUB_CHECK();														\
CSU_CLEAR_CALL();														\
CSU_SLICE_RETURN(ERROR)


/*
 * Macro for a trivial slicing function that just calls a
 * non-slicing function. (Note that SLICE_SESSION must be
 * defined but that SLICE_VARS is not needed.)
 */
#define	NONSLICE_FORWARD(FUNCTION)										\
CSU_CLEAR_CALL();														\
CSU_SLICE_RETURN(FUNCTION)


#endif /* !_OMC_OMC_TIMESLICE_H_ */
