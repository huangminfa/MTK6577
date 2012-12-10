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
 * Solid Dialog
 *
 */

#pragma once
#ifndef A3M_SOLIDDIALOG_H
#define A3M_SOLIDDIALOG_H
#include "viewerdialog.h"  /* Used as base class */
#include "a3m/appearance.h"  /* AppearanceUniformCollector */

namespace a3m
{
  class Solid;
  class ShaderUniformBase;
}

/*
 * This is part of the SceneDialog. Seperated so that it can be shown only
 * when the user selects a Solid node.
 */
class SolidDialog : public ViewerDialog, public a3m::PropertyCollector
{
public:
  SolidDialog( HWND parentWindowHandle );

  // Called when the user selects a solid node.
  void init( a3m::Solid *solid );

  // Overriden methods from ViewerDialog.
  virtual void onCommand( A3M_INT32 control, A3M_INT32 event, A3M_INT32 lparam );

  // Update control text to reflect any changes in solid data.
  void update();

  // Implemented from PropertyCollector
  A3M_BOOL collect(
      a3m::ShaderUniformBase::Ptr const& uniform,
      A3M_CHAR8 const* propertyName,
      A3M_INT32 index );

private:
  a3m::Solid *m_solid;                                 // Current solid
  a3m::ShaderUniformBase *m_uniform;                   // Current uniform
};

#endif // A3M_SolidDIALOG_H
