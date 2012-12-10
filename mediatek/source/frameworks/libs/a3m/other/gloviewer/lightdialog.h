/**************************************************************************
 *
 * Copyright (c) 2012 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 ***************************************************************************/
/**
 * Light Dialog
 *
 */

#pragma once
#ifndef A3M_LIGHTDIALOG_H
#define A3M_LIGHTDIALOG_H
#include "viewerdialog.h"  /* Used as base class */

namespace a3m
{
  class Light;
}

/*
 * This is part of the SceneDialog. Seperated so that it can be shown only
 * when the user selects a Light node.
 */
class LightDialog : public ViewerDialog
{
public:
  LightDialog( HWND parentWindowHandle );

  // Called when the user selects a light node.
  void init( a3m::Light *light );

  // Update control text to reflect any changes in light data.
  void update();

private:
  a3m::Light *m_light; // Current light
};

#endif // A3M_LIGHTDIALOG_H
