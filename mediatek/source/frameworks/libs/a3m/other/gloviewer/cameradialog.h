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
 * Camera Dialog
 *
 */

#pragma once
#ifndef A3M_CAMERADIALOG_H
#define A3M_CAMERADIALOG_H
#include "viewerdialog.h"  /* Used as base class */

namespace a3m
{
  class Camera;
}

/*
 * This is part of the SceneDialog. Seperated so that it can be shown only
 * when the user selects a Camera node.
 */
class CameraDialog : public ViewerDialog
{
public:
  CameraDialog( HWND parentWindowHandle );

  // Called when the user selects a camera node.
  void init( a3m::Camera *camera );

  // Update control text to reflect any changes in camera data.
  void update();

private:
  a3m::Camera *m_camera; // Current camera
};

#endif // A3M_CAMERADIALOG_H
