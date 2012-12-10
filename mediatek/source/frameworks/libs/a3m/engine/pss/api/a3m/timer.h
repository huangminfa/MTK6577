/*****************************************************************************
 *
 * Copyright (c) 2010 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 *****************************************************************************/
/** \file
 * PSS Timer API declaration
 *
 */

#pragma once
#ifndef PSS_TIMER_H
#define PSS_TIMER_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/base_types.h> /* A3M_UINT32 etc. */

/** \defgroup  a3mPssTimerApi PSS Timer API
 *  \ingroup   a3mPss
 *
 *  The PSS Timer API provides a high resolution timer which measures CPU time.
 *  It only provides a time stamp representing the elapsed time since the timer
 *  was initialised.
 *
 *  @{
 */

/*****************************************************************************
 * Global Functions
 *****************************************************************************/
/**
 * PSS Timer initialisation routine.
 * Initialises the PSS hi-res timer.  This must be called before the rest of
 * the timer API can be used and should ideally be done at application startup.
 * If this is called multiple times only the first invocation has an effect.
 * When successful, TRUE is returned, else if a hi-res timer is not available
 * on this platform, FALSE is returned.
 *
 * \return TRUE if successful, else FALSE
 */
A3M_BOOL pssTimerInit(void);

/**
 * PSS Timer get elapsed cycle count.
 * This returns the number of clock cycles since #pssTimerInit was called.
 * To convert this into seconds, divide the returned cycle count with the
 * timer's clock frequency (obtained with #pssTimerGetFrequency).
 *
 * \return Clock cycle count since timer initialisation
 */
A3M_INT64 pssTimerGetTicks(void);

/**
 * PSS Timer get elapsed time in milliseconds.
 * This returns the time in milliseconds since #pssTimerInit was called.
 *
 * \return Elapsed time in milliseconds since timer initialisation
 */
A3M_UINT32 pssTimerGetTimeMs(void);

/**
 * PSS Timer get clock frequency.
 * This returns the frequency of the clock used to advance the timer
 * tick count.
 *
 * \return Timer clock frequency as ticks-per-second (Hz)
 */
A3M_UINT32 pssTimerGetFrequency(void);

/** @} */

#endif
