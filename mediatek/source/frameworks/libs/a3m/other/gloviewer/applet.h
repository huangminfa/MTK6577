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
/**
 * Applet base class
 *
 */

#pragma once
#ifndef A3M_APPLET_H
#define A3M_APPLET_H

/******************************************************************************
 * Include Files
 ******************************************************************************/
#include <a3m/base_types.h>    /* A3M_CHAR8    */
#include <a3m/noncopyable.h>   /* NonCopyable  */
#include <a3m/keycode.h>       /* Keycode enum */

class Applet : a3m::NonCopyable
{
public:
  ~Applet() {}

  virtual A3M_BOOL init() { return A3M_TRUE; }
  virtual void draw() {}
  virtual void update() {}
  virtual void touchMove( A3M_INT32 x, A3M_INT32 y ) {}
  virtual void touchUp( A3M_INT32 x, A3M_INT32 y ) {}
  virtual void touchDown( A3M_INT32 x, A3M_INT32 y ) {}
  virtual void keyDown( Keycode key ) {}
  virtual void keyUp( Keycode key ) {}
  virtual void mouseWheel( A3M_INT32 delta ) {}
};

#endif // A3M_APPLET_H
