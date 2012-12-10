/**************************************************************************
 *
 * Copyright (c) 2010 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 ***************************************************************************/
/** \file
 * PC mouse definitions
 *
 */

#pragma once
#ifndef PSS_MOUSE_H
#define PSS_MOUSE_H

/******************************************************************************
 * Include Files
 ******************************************************************************/
/* None */


#ifdef __cplusplus
extern "C" {
#endif

/******************************************************************************
 * Type Definitions
 ******************************************************************************/
/** \defgroup  a3mPssMouseButtonType PSS Mouse button codes
 *  \ingroup   a3mPss
 *  @{
 */

/** Mouse button enum */
typedef enum MouseButtonTag
{
  MOUSE_BUTTON_LEFT   = 1 << 0,         /**< Left button   */
  MOUSE_BUTTON_RIGHT  = 1 << 1,         /**< Right button  */
  MOUSE_BUTTON_MIDDLE = 1 << 2          /**< Middle button */
} MouseButton;

/** @} */

#ifdef __cplusplus
}
#endif

#endif
