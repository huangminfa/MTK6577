/*****************************************************************************
 *
 * Copyright (c) 2012 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 *****************************************************************************/

#pragma once
#ifndef COM_MEDIATEK_A3M_JNI_RENDERFLAGS_H
#define COM_MEDIATEK_A3M_JNI_RENDERFLAGS_H

#include <a3m/flags.h>

extern a3m::FlagMask const VISIBLE; // Visible by default

// Layer bits are reset by default
extern a3m::FlagMask const RENDER_LAYER_BIT0;
extern a3m::FlagMask const RENDER_LAYER_BIT1;

#endif // COM_MEDIATEK_A3M_JNI_RENDERFLAGS_H

