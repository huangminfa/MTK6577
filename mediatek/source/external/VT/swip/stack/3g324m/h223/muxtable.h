/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

/******************************************************************************
        Copyright (c) 2001 RADVISION Inc. and RADVISION Ltd.
*******************************************************************************
NOTICE:
This document contains information that is confidential and proprietary
to RADVISION Inc. and RADVISION Ltd.. No part of this document may be
reproduced in any form whatsoever without written prior approval by
RADVISION Inc. or RADVISION Ltd..

RADVISION Inc. and RADVISION Ltd. reserve the right to revise this
publication and make changes without obligation to notify any person of
such revisions or changes.
******************************************************************************/

#ifndef _MUX_TABLE_H
#define _MUX_TABLE_H


#ifdef __cplusplus
extern "C" {
#endif

/*---------------------------------------------------------------------------*/
/*                           TYPE DEFINITIONS                                */
/*---------------------------------------------------------------------------*/

#define MUX_TABLE_DESC_MAX          16

#define CONTROL_AUDIO ((CHANNEL_TYPE_CONTROL) | (CHANNEL_TYPE_AUDIO))
#define CONTROL_VIDEO ((CHANNEL_TYPE_CONTROL) | (CHANNEL_TYPE_VIDEO))
#define CONTROL_DATA  ((CHANNEL_TYPE_CONTROL) | (CHANNEL_TYPE_DATA))
#define AUDIO_VIDEO   ((CHANNEL_TYPE_AUDIO) | (CHANNEL_TYPE_VIDEO))
#define AUDIO_DATA    ((CHANNEL_TYPE_AUDIO) | (CHANNEL_TYPE_DATA))
#define VIDEO_DATA    ((CHANNEL_TYPE_VIDEO) | (CHANNEL_TYPE_DATA))
#define CONTROL_AUDIO_VIDEO ((CHANNEL_TYPE_CONTROL) |\
                             (CHANNEL_TYPE_AUDIO) |\
                             (CHANNEL_TYPE_VIDEO))
#define CONTROL_AUDIO_DATA  ((CHANNEL_TYPE_CONTROL) |\
                             (CHANNEL_TYPE_AUDIO) |\
                             (CHANNEL_TYPE_DATA))
#define CONTROL_VIDEO_DATA  ((CHANNEL_TYPE_CONTROL) |\
                             (CHANNEL_TYPE_VIDEO) |\
                             (CHANNEL_TYPE_DATA))
#define AUDIO_VIDEO_DATA    ((CHANNEL_TYPE_AUDIO) |\
                             (CHANNEL_TYPE_VIDEO) |\
                             (CHANNEL_TYPE_DATA))
#define CONTROL_AUDIO_VIDEO_DATA ((CHANNEL_TYPE_CONTROL) |\
                                  (CHANNEL_TYPE_AUDIO) |\
                                  (CHANNEL_TYPE_VIDEO) |\
                                  (CHANNEL_TYPE_DATA))


typedef struct{
    TMuxTblAtom atom[RV_H223_MUX_TABLE_ATOM_PER_ENTRY_MAX]; /* Array of atoms.*/
    RvUint32    arraySize; /* Actual size of atom array.*/
}TMuxTblEntry;

typedef struct{
    TMuxTblEntry entry[MUX_TABLE_DESC_MAX];
    RvUint32 nextEntry;   /* Index of entry to be filled.*/
}TMuxTbl;




/* TO OREN:
Few examples of muxtable entries according to H223(page11) strings (atom looks
like this : [type(a = 1/l = 2), rc(0 = UCF), lcn if type = 1/ first atom index if type = 2]:
5)[1,4,1][1,1,2][1,2,3][2,0,1]
8)[1,25,1][1,1,2][1,1,3][2,5,1][2,0,0]

Sample code fragment that parses such entry:
typedef struct{
  TMuxTblAtom *pactive;
  RvUint16 runningCount;
}ListDesc;

RvStatus DemuxData (TMuxTblAtom *atomArray, RvUint32 arraySize)
{
  ListDesc list[RV_H223_NESTING_DEPTH_MAX + 1];
  RvUint32 listBalance = 0;

  RvBool  stop = RV_FALSE;
  TMuxTblAtom *patom = atomArray;

  ...

  memset (list, 0, sizeof(list));

  // Write the actual data.
  while (RV_FALSE == stop)
  {
    if (TYPE_LIST == patom->atomType)
    {
      if (list[listBalance].pactive != patom)
      {
        // This is the first time we've reached this list atom.
        listBalance++;
        list[listBalance - 1].pactive = patom;
        list[listBalance - 1].runningCount = (UCF == patom->repeatCount) ? UCF : (patom->repeatCount - 1);
        patom = &atomArray[patom->firstAtom];
      }
      else
      {
        // We have already been here.
        if (0 == list[listBalance - 1].runningCount)
        {
          // 0 has 2 meanings - UCF or end of list's running count.
          if (UCF == patom->repeatCount)
          {
            // Repeat the list again.
            patom = &atomArray[patom->firstAtom];
          }
          else
          {
            // The list has been  repeated (patom->repeatCount) times. Mark it as
            // not used and move forward.
            list[listBalance - 1].pactive = NULL;
            listBalance--;
            patom++;
          }
        }
        else
        {
          // Update the runningCount and repeat the list again.
          list[listBalance - 1].runningCount--;
          patom = &atomArray[patom->firstAtom];
        }
      }
    }
    else
    {
      // This is data atom - demux it.

      ...

      patom++;
    }
  }
}
END TO OREN*/
#ifdef __cplusplus
}
#endif

#endif /* _MUX_TABLE_H */
