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
 * Scene Dialog
 *
 */
#pragma once
#ifndef A3M_SCENEDIALOG_H
#define A3M_SCENEDIALOG_H

#include "viewerdialog.h"  /* Used as base class */
#include "lightdialog.h"   /* Container for light-specific controls */
#include "cameradialog.h"  /* Container for camera-specific controls */
#include "soliddialog.h"   /* Container for solid-specific controls */

#include <a3m/scenenodevisitor.h> /* Visit node to determine type   */

/*
 * Modeless dialog to allow user to examine the scene graph and click on
 * individual nodes to examine data specific to each node type.
 */
class SceneDialog : public ViewerDialog, a3m::SceneNodeVisitor
{
public:
  SceneDialog( HWND parentWindowHandle );

  // Called by client when the scene has been loaded.
  void init( a3m::SceneNode *node );

  // Overriden methods from ViewerDialog.
  virtual void onCommand( A3M_INT32 control, A3M_INT32 event, A3M_INT32 lparam );
  virtual void onNotify( A3M_INT32 control, A3M_INT32 lparam );

  // Overriden methods from SceneNodeVisitor.
  virtual void visit( a3m::SceneNode *node );
  virtual void visit( a3m::Solid *solid );
  virtual void visit( a3m::Light *light );
  virtual void visit( a3m::Camera *camera );

  // Update control text to reflect any changes in data.
  void update();

  // Hide all child dialogs when a new node has been selected
  void hideChildDialogs();
private:
  a3m::SceneNode *m_selected;  // Currently selected node
  LightDialog m_lightDlg;      // Light-specific controls
  CameraDialog m_cameraDlg;    // Camera-specific controls
  SolidDialog m_solidDlg;      // Solid-specific controls
};

#endif // A3M_SCENEDIALOG_H
