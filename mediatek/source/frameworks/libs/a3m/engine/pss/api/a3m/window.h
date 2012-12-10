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
 * Declaration of Window API functions.
 */

#pragma once
#ifndef PSS_WINDOW_H
#define PSS_WINDOW_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/mouse.h>      /* for MouseButton enum */
#include <a3m/keycode.h>    /* for Keycode enum */
#include <a3m/base_types.h> /* for A3M_INT16 etc. */


/** \defgroup a3mPssWinApi PSS Windowing API types and functions
 *  \ingroup  a3mPss
 *
 *  Pss Window API provides win32 services for creating scene window, get the
 *  widow parameters, close or refresh the window.
 *
 * @{
 */

/*****************************************************************************
 * Type Definitions
 *****************************************************************************/
/** Window event type enum */
typedef enum EventTypeTag
{
  EVT_TYPE_KEYUP,             /**< Keyboard key released */
  EVT_TYPE_KEYDOWN,           /**< Keyboard key pressed down */
  EVT_TYPE_MOUSEUP,           /**< Mouse button released */
  EVT_TYPE_MOUSEDOWN,         /**< Mouse button pressed down */
  EVT_TYPE_MOUSEMOVE,         /**< Mouse moved */
  EVT_TYPE_MOUSEWHEEL,        /**< Mouse wheel moved */
  EVT_TYPE_MOUSELEAVE,        /**< Mouse leaves the client area */
  EVT_TYPE_RESIZE,            /**< Window resized */
  EVT_TYPE_POSITION,          /**< Window moved */
  EVT_TYPE_CLOSE,             /**< Window closed */
  EVT_TYPE_UNKNOWN            /**< Window non-defined event */
} EventType;

/** Window key event structure */
typedef struct KeyEventTag
{
  Keycode keycode;            /**< Key, KEY_D for example */
} KeyEvent;

/** Window mouse event structure */
typedef struct MouseEventTag
{
  A3M_INT32        delta;     /**< Mouse wheel movement: +delta = wheel moved
                                   up, -delta = wheel moved down */
  A3M_UINT32       button;    /**< Current state of the L, M, R buttons */
  A3M_INT32        x;         /**< Mouse X position in screen coordinates */
  A3M_INT32        y;         /**< Mouse Y position in screen coordinates */
} MouseEvent;

/** Window re-position event structure */
typedef struct PositionEventTag
{
  A3M_INT32        x;         /**< Client window X position in screen
                                   coordinates */
  A3M_INT32        y;         /**< Client window Y position in screen
                                   coordinates */
} PositionEvent;

/** Window re-size event structure */
typedef struct ResizeEventTag
{
  A3M_INT32        width;     /**< New width of window */
  A3M_INT32        height;    /**< New height of window */
} ResizeEvent;

/** Window event structure */
typedef struct WindowEventTag
{
  EventType type;             /**< Event type, and thus an indication of
                                   what the union contains */
  union                       /**< Union of all the different window events
                                   which can occur at run-time */
  {
    KeyEvent       key;
    MouseEvent     mouse;
    ResizeEvent    resize;
    PositionEvent  position;
  };
} WindowEvent;

/** Window event handler */
typedef void (*OnEvent)(A3M_UINT32 wndId /**< Window ID */,
                        WindowEvent* evt /**< Event */ );

/*****************************************************************************
 * Global Functions
 *****************************************************************************/
/**
 * Create a new window with the specified title, width, height and
 * bits-per-pixel.  If successful, a non-zero window ID is returned.  If the
 * window couldn't be created, 0 is returned.
 *
 * \return Window ID on success, else 0
 *
 * \todo How does it behave with oversize dimensions? etc.
 */
A3M_UINT32 pssWindowCreate(const A3M_CHAR8* title
                           /**< Title text for window */,
                           A3M_INT32 width
                           /**< requid width of window */,
                           A3M_INT32 height
                           /**< height of window */,
                           A3M_INT32 bpp
                           /**< bits per pixel of window */,
                           OnEvent eventCallback
                           /**< window event callback */,
                           A3M_BOOL setClientArea = A3M_FALSE
                           /**< If TRUE -> window client area is resized to
                                match the specified width & height*/);

/**
 * This function refreshes the specified window by
 * flipping the back buffer and polling for events.
 */
void pssWindowRefresh(A3M_UINT32 wndId /**< window ID */);

/**
 * Close the specified window and
 * free all allocated resources.
 */
void pssWindowClose(A3M_UINT32 wndId /**< window ID */);

/**
 * Get window width
 * \return the width of the specified window.
 */
A3M_INT32 pssGetWindowWidth(A3M_UINT32 wndId /**< window ID */);

/**
 * Get window height
 * \return the height of the specified window.
 */
A3M_INT32 pssGetWindowHeight(A3M_UINT32 wndId /**< window ID */);

/**
 * Get window rectangle
 * \return x,y,x',y' rectangle position of the specified window.
 */
void pssGetWindowRectangle(A3M_UINT32 wndId
                           /**< window ID */,
                           A3M_INT32* left
                           /**< Pointer to hold rectangle left value */,
                           A3M_INT32* right
                           /**< rectangle right */,
                           A3M_INT32* bottom
                           /**< rectangle bottom */,
                           A3M_INT32* top
                           /**< rectangle top */);

/** @} */

#endif
