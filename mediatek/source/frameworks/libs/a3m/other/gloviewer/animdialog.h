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
 * Animation Dialog
 *
 */

#pragma once
#ifndef A3M_ANIMDIALOG_H
#define A3M_ANIMDIALOG_H

#include "viewerdialog.h"  /* Used as base class */

/*
 * Modeless dialog allowing users to control animation
 */
class AnimationDialog : public ViewerDialog
{
public:
  AnimationDialog( HWND parentWindowHandle );

  /*
   * Client classes should derive from this to recieve notifications
   */
  class Listener
  {
  public:
    virtual void onScrub( A3M_FLOAT ) {}
  };

  // Set the object which should recieve notifications.
  void setListener( Listener *m_listener ) {m_listener = m_listener;}

  void onHScroll( A3M_UINT32 pos );

private:
  Listener *m_listener;
};

#endif // A3M_ANIMDIALOG_H
