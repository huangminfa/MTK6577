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

#include <sys/socket.h>
#include <sys/un.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <sys/stat.h>
#include <unistd.h>
#include <pthread.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <utils/Log.h>
#include<sys/mount.h>




/*/ proc in mtdcore
	return sprintf(buf, "mtd%d: %8.8llx %8.8x \"%s\"\n", i,
		       (unsigned long long)this->size,
		       this->erasesize, this->name);
*/
int get_partition_numb(char const *s1)
//int get_partition_numb(char *s1)
{
	int fd;
	int result;
	int number;
	int iWriteSize = 32* 32;
	char *tempBuf=NULL;
	char *current;
      char *id;
	
	fd=open("/proc/mtd",O_RDWR);
	if(fd<0)
	{
		LOGE("[mtd_util]:mtd open error\r\n");
		return 0;
	}

    	tempBuf=(char *)malloc(iWriteSize);
      
	if(tempBuf==NULL)
	{
		LOGE("[mtd_util]:malloc error\r\n");
		close(fd);
		free(tempBuf);
		return 0;
	}
	memset(tempBuf,0,iWriteSize);
	 
      
	result=read(fd,tempBuf,iWriteSize);
	if(result!=iWriteSize)
	{
		LOGE("[mtd_util]:mtd proc read iWriteSize:%d\r\n",result);
	}
	if(*s1 !='\0')
	{
		current = strstr((char *)tempBuf,s1);
		if(current!=NULL)
		{
			LOGE("[mtd_util]:Find string %s \r\n",current);
			current = current-30;
			id = strstr((char *)current,"mtd");
			LOGE("[mtd_util]:Find string %s \r\n",id);  //id=mtdxx
		}
		else
		{
			LOGE("[mtd_util]:Can not Find string %s \r\n",s1);
			return 0;
		}
		
	}
	else
	{
		LOGE("[mtd_util]:no string input error\r\n");
		return 0;
	}
      id = id+3;  
      number=atoi(id);
	LOGE("[mtd_util]:Find string %d \r\n",number);
		
	free(tempBuf);
	close(fd);
    	return number;
}

#if 0
int
main(int argc, char *argv[])
{
	get_partition_numb("userdata");
	get_partition_numb("lhh");
	get_partition_numb("");

    return(0);
}
#endif
