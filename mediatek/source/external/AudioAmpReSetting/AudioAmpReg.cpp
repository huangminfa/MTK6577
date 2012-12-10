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

#include "AudioAMPControlInterface.h"
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include<stdio.h>
#include "AudioIoctl.h"

char const * const kAudioDeviceName = "/dev/eac";

using namespace android;

int main(int arv, char ** arg)
{
   int fd = ::open(kAudioDeviceName, O_RDWR);
   if(fd <=0 )
   {
       printf("Failed to open audiohardware!\n");
       return -1;
   }
   AudioAMPControlInterface *auioAmpDevice = AudioDeviceManger::createInstance(fd);
   if(!auioAmpDevice)
   {
       printf("Failed to create AudioAMP interface!\n");
       return -1;
   }
   bool contine=true;
   while(contine)
   {
       int option=2;
       printf("input your option:: 0(read), 1(write), 2(exit)\n");
       scanf("%d",&option);
       switch(option)
       {
            case 0:
            {
                int regName =0;
                int regValue = 0;
                printf("input register name(0---7)\n");
                scanf("%d",&regName); 
                auioAmpDevice->getParameters(AUD_AMP_GET_REGISTER, regName,&regValue);
                printf("get register(%d) value(0x%x)\n",regName,regValue);
                break;
            }    
            case 1:
            {
                int regName =0;
                int regValue = 0;
                printf("input register name(0---9)\n");
                scanf("%d",&regName); 
                printf("input register value (hex and noprefix)\n");
                scanf("%x",&regValue); 
                //printf("regValue =0x%x\n",regValue);
                auioAmpDevice->setParameters(AUD_AMP_SET_REGISTER, regName,regValue);
                int newRegValue=0;
                auioAmpDevice->getParameters(AUD_AMP_GET_REGISTER, regName,&newRegValue);
                if(regValue == newRegValue)
                {     
                    printf("set register(%d) value(0x%x)\n",regName,newRegValue);
                }
                else
                {
                    printf("fail to set register(%d)",regName);
                }
                break;
            }
            default:
            {
                contine=false;
                break;
            }
       }
   }

 AudioDeviceManger::releaseInstance(auioAmpDevice);
 close(fd);
 return 0;
}


