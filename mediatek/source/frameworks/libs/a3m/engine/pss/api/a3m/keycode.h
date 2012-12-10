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
 * PC keyboard key codes
 *
 */

#pragma once
#ifndef PSS_KEYCODE_H
#define PSS_KEYCODE_H

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

/** \defgroup  a3mPssKeyType PSS Key codes KEY_A, KEY_TAB, etc.
 *  \ingroup   a3mPss
 *  @{
 */

/** Windows key code enum */
typedef enum KeycodeTag
{
  KEY_LBUTTON,     /**< Left mouse button */
  KEY_RBUTTON,     /**< Right mouse button */
  KEY_CANCEL,      /**< CTRL+BREAK processing */
  KEY_MBUTTON,     /**< Middle mouse button */
  KEY_BACK,        /**< BACKSPACE key */
  KEY_TAB,         /**< TAB key */
  KEY_CLEAR,       /**< CLEAR key */
  KEY_RETURN,      /**< ENTER key */
  KEY_SHIFT,       /**< Shift key */
  KEY_CONTROL,     /**< Ctrl key*/
  KEY_MENU,        /**< Alt key */
  KEY_PAUSE,       /**< Pause key */
  KEY_CAPITAL,     /**< CAPS LOCK key */
  KEY_ESCAPE,      /**< ESC key*/
  KEY_SPACE,       /**< SPACEBAR */
  KEY_PRIOR,       /**< PAGE UP key */
  KEY_NEXT,        /**< PAGE DOWN key */
  KEY_END,         /**< END key */
  KEY_HOME,        /**< HOME key */
  KEY_LEFT,        /**< LEFT ARROW key */
  KEY_UP,          /**< UP ARROW key */
  KEY_RIGHT,       /**< RIGHT ARROW key */
  KEY_DOWN,        /**< DOWN ARROW key */
  KEY_SELECT,      /**< SELECT key */
  KEY_PRINT,       /**< PRINT key */
  KEY_EXEC,        /**< EXECUTE key */
  KEY_SNAPSHOT,    /**< PRINT SCREEN key */
  KEY_INSERT,      /**< INS key */
  KEY_DELETE,      /**< DEL key */
  KEY_HELP,        /**< HELP key */
  KEY_0,           /**< 0 key */
  KEY_1,           /**< 1 key */
  KEY_2,           /**< 2 key */
  KEY_3,           /**< 3 key */
  KEY_4,           /**< 4 key */
  KEY_5,           /**< 5 key */
  KEY_6,           /**< 6 key */
  KEY_7,           /**< 7 key */
  KEY_8,           /**< 8 key */
  KEY_9,           /**< 9 key */
  KEY_A,           /**< A key */
  KEY_B,           /**< B key */
  KEY_C,           /**< C key */
  KEY_D,           /**< D key */
  KEY_E,           /**< E key */
  KEY_F,           /**< F key */
  KEY_G,           /**< G key */
  KEY_H,           /**< H key */
  KEY_I,           /**< I key */
  KEY_J,           /**< J key*/
  KEY_K,           /**< K key */
  KEY_L,           /**< L key */
  KEY_M,           /**< M key */
  KEY_N,           /**< N key */
  KEY_O,           /**< O key */
  KEY_P,           /**< P key */
  KEY_Q,           /**< Q key */
  KEY_R,           /**< R key */
  KEY_S,           /**< S key */
  KEY_T,           /**< T key */
  KEY_U,           /**< U key */
  KEY_V,           /**< V key */
  KEY_W,           /**< W key */
  KEY_X,           /**< X key */
  KEY_Y,           /**< Y key */
  KEY_Z,           /**< Z key */
  KEY_NUMPAD0,     /**< Numeric keypad 0 key*/
  KEY_NUMPAD1,     /**< Numeric keypad 1 key */
  KEY_NUMPAD2,     /**< Numeric keypad 2 key */
  KEY_NUMPAD3,     /**< Numeric keypad 3 key */
  KEY_NUMPAD4,     /**< Numeric keypad 4 key */
  KEY_NUMPAD5,     /**< Numeric keypad 5 key */
  KEY_NUMPAD6,     /**< Numeric keypad 6 key */
  KEY_NUMPAD7,     /**< Numeric keypad 7 key */
  KEY_NUMPAD8,     /**< Numeric keypad 8 key */
  KEY_NUMPAD9,     /**< Numeric keypad 9 key */
  KEY_MULTIPLY,    /**< Multiply key */
  KEY_ADD,         /**< Add key */
  KEY_SEPARATOR,   /**< Separator key */
  KEY_SUBTRACT,    /**< Subtract key */
  KEY_DECIMAL,     /**< Decimal key */
  KEY_DIVIDE,      /**< Divide key */
  KEY_F1,          /**< F1 key */
  KEY_F2,          /**< F2 key */
  KEY_F3,          /**< F3 key */
  KEY_F4,          /**< F4 key */
  KEY_F5,          /**< F5 key */
  KEY_F6,          /**< F6 key */
  KEY_F7,          /**< F7 key */
  KEY_F8,          /**< F8 key */
  KEY_F9,          /**< F9 key */
  KEY_F10,         /**< F10 key */
  KEY_F11,         /**< F11 key */
  KEY_F12,         /**< F12 key */
  KEY_F13,         /**< F13 key */
  KEY_F14,         /**< F14 key */
  KEY_F15,         /**< F15 key */
  KEY_F16,         /**< F16 key */
  KEY_F17,         /**< F17 key */
  KEY_F18,         /**< F18 key */
  KEY_F19,         /**< F19 key */
  KEY_F20,         /**< F20 key */
  KEY_F21,         /**< F21 key */
  KEY_F22,         /**< F22 key */
  KEY_F23,         /**< F23 key */
  KEY_F24,         /**< F24 key */
  KEY_NUMLOCK,     /**< NUM LOCK key */
  KEY_SCROLL,      /**< SCROLL LOCK key */
  KEY_LSHIFT,      /**< Left SHIFT key */
  KEY_RSHIFT,      /**< Right SHIFT key */
  KEY_LCONTROL,    /**< Left CTRL key */
  KEY_RCONTROL,    /**< Right CTRL key */
  KEY_LMENU,       /**< Left MENU key */
  KEY_RMENU,       /**< Right MENU key */
  KEY_PLAY,        /**< PLAY key (Nokia/Ericsson definitions) */
  KEY_ZOOM,        /**< ZOOM key (Nokia/Ericsson definitions) */
  KEY_UNKNOWN,      /**< Any unsupported key */
  NUM_KEYS         /**< Number of keys */
} Keycode;

/** @} */

#ifdef __cplusplus
}
#endif

#endif
