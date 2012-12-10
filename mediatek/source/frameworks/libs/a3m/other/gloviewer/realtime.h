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
#pragma once
#ifndef A3M_REALTIME_H
#define A3M_REALTIME_H
#include <a3m/base_types.h>   /* A3M_FLOAT and A3M_INT32 declarations */

/** Real time timer
 * Use the member function elapsedSeconds() to return the time since the object
 * was constructed (or since reset() was called).
 */
class RealTime
{
public:
  RealTime();
  ~RealTime();

  A3M_FLOAT elapsedSeconds() const;

  void reset();
private:
  A3M_INT32 m_ms;
};

#endif /* A3M_REALTIME_H */
