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
 * Viewer Dialog
 *
 */

#pragma once
#ifndef A3M_VIEWERDIALOG_H
#define A3M_VIEWERDIALOG_H
#include <a3m/base_types.h>  /* A3M_INT32 etc.             */
#define NOMINMAX             /* Don't want windows.h defining min & max */
#include <windows.h>         /* HWND etc.                  */
#include <commctrl.h>        /* Common control identifiers */
#include <a3m/vector2.h>     /* Vector types               */
#include <a3m/colour.h>      /* Colour4f type              */
/*
 * A base class for viewer dialogs
 */
class ViewerDialog
{
public:
  ViewerDialog( HWND parentWindowHandle, A3M_UINT32 resource );

  // Overridden by subclasses to react to control notifications etc.
  virtual void onCommand( A3M_INT32 control, A3M_INT32 event, A3M_INT32 lparam ) {}
  virtual void onNotify( A3M_INT32 control, A3M_INT32 lparam ) {}
  virtual void onHScroll( A3M_UINT32 pos ) {}
  virtual void onDrawCtl( A3M_INT32 control, HDC dc, RECT const &rect );

  // Returns true if the dialog is visible
  A3M_BOOL visible() const;
  // Show or hide dialog.
  void visible( A3M_BOOL makeVisible );

  // Return the window handle for this dialog.
  HWND hwnd() { return m_wnd; }

  // Set the visibility of a control on the dialog
  void setCtlVisible( A3M_UINT32 control, A3M_BOOL visible );

  // Convenience functions for setting the text of edit controls
  void setEditText( A3M_UINT32 control, A3M_CHAR8 const *text );
  void setEditText( A3M_UINT32 control, A3M_FLOAT v );
  void setEditText( A3M_UINT32 control, A3M_INT32 value );
  void setEditText( A3M_UINT32 control1, A3M_UINT32 control2, A3M_UINT32 control3,
                    a3m::Vector3f const &v );
  void setEditText( A3M_UINT32 control1, A3M_UINT32 control2,
                    A3M_UINT32 control3, A3M_UINT32 control4,
                    a3m::Vector4f const &v );
  void setEditText( A3M_UINT32 control1, A3M_UINT32 control2, A3M_UINT32 control3,
                    a3m::Colour4f const &c );
  void setEditText( A3M_UINT32 control1, A3M_UINT32 control2,
                    A3M_UINT32 control3, A3M_UINT32 control4,
                    a3m::Colour4f const &c );
  void setEditColour( A3M_UINT32 control, a3m::Colour4f const &c );
  void setEditColour( A3M_UINT32 control, a3m::Vector4f const &c );

private:
  HWND m_wnd; // The window handle of this dialog
};

#endif // A3M_SCENEDIALOG_H
