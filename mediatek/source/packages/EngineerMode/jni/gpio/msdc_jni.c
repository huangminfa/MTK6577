/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2008
*
*  BY OPENING THIS FILE, BUYER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
*  THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
*  RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO BUYER ON
*  AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
*  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
*  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
*  NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
*  SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
*  SUPPLIED WITH THE MEDIATEK SOFTWARE, AND BUYER AGREES TO LOOK ONLY TO SUCH
*  THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. MEDIATEK SHALL ALSO
*  NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE RELEASES MADE TO BUYER'S
*  SPECIFICATION OR TO CONFORM TO A PARTICULAR STANDARD OR OPEN FORUM.
*
*  BUYER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND CUMULATIVE
*  LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
*  AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
*  OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY BUYER TO
*  MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
*
*  THE TRANSACTION CONTEMPLATED HEREUNDER SHALL BE CONSTRUED IN ACCORDANCE
*  WITH THE LAWS OF THE STATE OF CALIFORNIA, USA, EXCLUDING ITS CONFLICT OF
*  LAWS PRINCIPLES.  ANY DISPUTES, CONTROVERSIES OR CLAIMS ARISING THEREOF AND
*  RELATED THERETO SHALL BE SETTLED BY ARBITRATION IN SAN FRANCISCO, CA, UNDER
*  THE RULES OF THE INTERNATIONAL CHAMBER OF COMMERCE (ICC).
*
*****************************************************************************/


/*******************************************************************************
 *
 * Filename:
 * ---------
 *   msdc_jni.c
 *
 * Project:
 * --------
 *   Android
 *
 * Description:
 * ------------
 *    c file of msdc jni function
 *
 * Author:
 * -------
 *   MTK80905

 *******************************************************************************/
 
 #define LOG_TAG "MSDC_IOCTL_JNI"
 #include <utils/Log.h>
 
#include <jni.h>
#include "../../../../kernel/include/sd_misc.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <fcntl.h>



struct oldmsdc_ioctl{
	int  						opcode;
	int  						host_num;
	int  						iswrite;
	int  						trans_type;
	unsigned int    total_size;
	unsigned int    address;
	unsigned int*   buffer;
	int  						cmd_driving;
	int  						dat_driving;
	int  						clock_freq;
	int  						result;
};


#define MSDC_ODC_4MA     (0x0)
#define MSDC_ODC_8MA     (0x4)
#define MSDC_ODC_12MA    (0x2)
#define MSDC_ODC_16MA    (0x6)
#define MSDC_ODC_COUNT		4

#define MSDC_HOST_SCLK              (25000000)

#define MSDC_DRIVING_SETTING              (0)
#define MSDC_CLOCK_FREQUENCY              (1)
#define MSDC_SINGLE_READ_WRITE   			    (2)
#define MSDC_MULTIPLE_READ_WRITE   				(3)


#define NEW_MSDC_HOST_SCLK              (25000000)

#define NEW_MSDC_DRIVING_SETTING              (0)
#define NEW_MSDC_CLOCK_FREQUENCY              (1)
#define NEW_MSDC_SINGLE_READ_WRITE 	    (2)
#define NEW_MSDC_MULTIPLE_READ_WRITE   				(3)

int driving[MSDC_ODC_COUNT] = {
	MSDC_ODC_4MA, 
	MSDC_ODC_8MA, 
	MSDC_ODC_12MA, 
	MSDC_ODC_16MA
};
	JNIEXPORT jint JNICALL Java_com_mediatek_engineermode_io_EMgpio_NewGetCurrent
(JNIEnv * env, jobject obj, jint hostNum,jint opcode)
{
 
	if (hostNum >3)
	{
		LOGD("----error: hostNum or currentIdx too large.\n");
		return -1;
	}

	int sd_fd, ret, idx;
	struct msdc_ioctl command;

LOGD("1 before open misc-sd\n");
	sd_fd = open("/dev/misc-sd", O_RDONLY);     
	if(sd_fd < 0)
	{
		LOGD("----error: 1 can't open misc-sd----, error code:%d\n", sd_fd);
		return -1;
	}
LOGD("2 after open misc-sd:%d\n", sd_fd);
    if(opcode == 0){
	command.opcode = MSDC_DRIVING_SETTING;
    }else{
     command.opcode = MSDC_HOPPING_SETTING;
    }
	command.host_num =hostNum; //0~3
	command.clk_pu_driving = 0xF;
	command.clk_pd_driving = 0xF;
	command.cmd_pu_driving = 0xF;
	command.cmd_pd_driving = 0xF;
	command.dat_pu_driving = 0xF;
	command.dat_pd_driving = 0xF;
	command.hopping_bit = 0xF;
	command.hopping_time = 0xF;
	command.iswrite =0; //0: read, 1:write
	command.clock_freq =0;
	command.result = -1;

	ret = ioctl(sd_fd, -1, (void *)&command);
LOGD("3 after ioctl\n");
	if(ret < 0 || -1 == command.result)
	{
		LOGD("----error: can't call misc-sd----, return:%d, fd:%d\n",ret, sd_fd);
		return -1;
	}

	close(sd_fd);

	int l_clkIdx_pu = 0xF;
	int l_clkIdx_pd = 0xF;
	int l_cmdIdx_pu = 0xF;
	int l_cmdIdx_pd = 0xF;
	int l_datIdx_pu = 0xF;
	int l_datIdx_pd = 0xF;
    int l_hopBitIdx = 0xF;
    int l_hopTimeIdx = 0xF;
	l_clkIdx_pu = command.clk_pu_driving;
	l_clkIdx_pd = command.clk_pd_driving;
	l_cmdIdx_pu = command.cmd_pu_driving;
	l_cmdIdx_pd = command.cmd_pd_driving;
	l_datIdx_pu = command.dat_pu_driving;
	l_datIdx_pd = command.dat_pd_driving;
	l_hopBitIdx = command.hopping_bit;
	l_hopTimeIdx = command.hopping_time;
  if(opcode == 0){
	if (l_clkIdx_pu != 0xF && l_clkIdx_pd != 0xF &&
			l_cmdIdx_pu != 0xF && l_cmdIdx_pd != 0xF &&
					l_datIdx_pu != 0xF && l_datIdx_pd != 0xF )
	{
		LOGD("success: clk_pu=%d,  clk_pd = %d, cmd_pu=%d,  cmd_pd = %d, dat_pu=%d,  dat_pd = %d\r\n", 
				l_clkIdx_pu, l_clkIdx_pd, l_cmdIdx_pu, l_cmdIdx_pd, l_datIdx_pu, l_datIdx_pd);
				return ((l_datIdx_pd << 20) | (l_datIdx_pu << 16) | (l_cmdIdx_pd << 12) | (l_cmdIdx_pu << 8) | (l_clkIdx_pd << 4) | l_clkIdx_pu);
	}
	else
	{
		LOGD("MSDC_JNI: error: clk_pu=%d,  clk_pd = %d, cmd_pu=%d,  cmd_pd = %d, dat_pu=%d,  dat_pd = %d\r\n", 
				l_clkIdx_pu, l_clkIdx_pd, l_cmdIdx_pu, l_cmdIdx_pd, l_datIdx_pu, l_datIdx_pd);
		return -1;
	}
	}else {
			if (l_hopBitIdx !=0xF && l_hopTimeIdx != 0xF)
			{
				LOGD("success: clk_pu=%d,  clk_pd = %d, cmd_pu=%d,  cmd_pd = %d, dat_pu=%d,  dat_pd = %d\r\n", 
						l_clkIdx_pu, l_clkIdx_pd, l_cmdIdx_pu, l_cmdIdx_pd, l_datIdx_pu, l_datIdx_pd);
				return (l_hopTimeIdx << 28)|(l_hopBitIdx << 24);
			}
			else
			{
				LOGD("MSDC_JNI: error: clk_pu=%d,  clk_pd = %d, cmd_pu=%d,  cmd_pd = %d, dat_pu=%d,  dat_pd = %d\r\n", 
						l_clkIdx_pu, l_clkIdx_pd, l_cmdIdx_pu, l_cmdIdx_pd, l_datIdx_pu, l_datIdx_pd);
				return -1;
			}
	}
}

	JNIEXPORT jboolean JNICALL Java_com_mediatek_engineermode_io_EMgpio_NewSetCurrent
(JNIEnv * env, jobject obj, jint hostNum, jint clkPU, jint clkPD, jint cmdPU, jint cmdPD, jint datPU, jint datPD, jint hopBit , jint hopTime,jint opcode)
{

	if(hostNum >3 || clkPU >7 || clkPD >7 || cmdPU >7 || cmdPD >7 || datPU >7 || datPD >7 || hopBit >3 || hopTime > 5)
	{
		LOGD("----error: hostNum or currentIdx too large.\n");
		return -1;
	}

	int sd_fd, ret;
	struct msdc_ioctl command;

	sd_fd = open("/dev/misc-sd", O_RDONLY); 
	if(sd_fd < 0)
	{
		LOGD("----error: can't open misc-sd----, error code:%d\n", sd_fd);
		return JNI_FALSE;
	}
	 if(opcode == 0){
	command.opcode = MSDC_DRIVING_SETTING;
	    }else{
	     command.opcode = MSDC_HOPPING_SETTING;
	    }
	command.host_num =hostNum; //0~3
	command.clk_pu_driving = clkPU;
	command.clk_pd_driving = clkPD;
	command.cmd_pu_driving = cmdPU;
	command.cmd_pd_driving = cmdPD;
	command.dat_pu_driving = datPU;
	command.dat_pd_driving = datPD;
	command.hopping_bit = hopBit;
	command.hopping_time = hopTime;
	command.iswrite =1; //0: read, 1:write
	command.clock_freq =0;
	command.result = -1;
	ret = ioctl(sd_fd, -1, (void *)&command);

	if(ret < 0 || -1 == command.result)
	{
		LOGD("----error: can't call misc-sd----, error:%d, fd:%d\n",ret, sd_fd);
		return JNI_FALSE;
	}

	close(sd_fd);

	return JNI_TRUE;

}


JNIEXPORT jint JNICALL Java_com_mediatek_engineermode_io_EMgpio_GetCurrent
  (JNIEnv * env, jobject obj, jint hostNum)
  {
  	if(hostNum >3 )
  	{
  		LOGD("----error: hostNum or currentIdx too large.\n");
  		return -1;
  	}
  	
  	int sd_fd, ret, idx;
	struct oldmsdc_ioctl command;
		
    sd_fd = open("/dev/mt6573-sd0", O_RDWR, 0);    // "/dev/mt6573-sd0~3" 
    if(sd_fd < 0)
    {
        LOGD("----error: can't open mt6573_sd----, error code:%d\n", sd_fd);
        return -1;
    }
    
    
    command.opcode = MSDC_DRIVING_SETTING;
	command.host_num =hostNum; //0~3
	command.cmd_driving=0;
	command.dat_driving=0;
	command.iswrite =0; //0: read, 1:write
	command.clock_freq =0;
	command.result = -1;
	ret = ioctl(sd_fd, -1, (void *)&command);
	if(ret < 0 || -1 == command.result)
	{
		LOGD("----error: can't call mt6573_sd----, return:%d, fd:%d\n",ret, sd_fd);
		return -1;
	}
	    
    close(sd_fd);
    
    int dataIdx = 0xFFFF;
	int cmdIdx = 0xFFFF;
	
    for(idx=0; idx<MSDC_ODC_COUNT; idx++)
    {
    	if(driving[idx] == command.dat_driving)
    	{ 
    		dataIdx = idx;
    	}
    	if(driving[idx] == command.cmd_driving)
    	{
    		cmdIdx = idx;
    	}
    }
    if(dataIdx != 0xFFFF && cmdIdx != 0xFFFF)
    {
	    LOGD("success: data=%d,  command = %d\r\n", dataIdx, cmdIdx);
    	return (cmdIdx << 16) | dataIdx;
    }
    else
    {
    	LOGD("MSDC_JNI: error: data=0x%x,  command = 0x%x\r\n", dataIdx, cmdIdx);
    	return -1;
    }
  }
 
					
JNIEXPORT jboolean JNICALL Java_com_mediatek_engineermode_io_EMgpio_SetCurrent
  (JNIEnv * env, jobject obj, jint hostNum, jint currentDataIdx, jint currentCmdIdx)
  {
  	if(hostNum >3 || currentDataIdx >3 || currentCmdIdx >3)
  	{
  		LOGD("----error: hostNum or currentIdx too large.\n");
  		return -1;
  	}
  	
  	int sd_fd, ret;
	struct oldmsdc_ioctl command;
		
    sd_fd = open("/dev/mt6573-sd0", O_RDWR, 0);    // "/dev/mt6573-sd0~3" 
    if(sd_fd < 0)
    {
        LOGD("----error: can't open mt6573_sd----, error code:%d\n", sd_fd);
        return JNI_FALSE;
    }
    
    
    command.opcode = MSDC_DRIVING_SETTING;
	command.host_num =hostNum; //0~3
	command.cmd_driving=driving[currentCmdIdx];
	command.dat_driving=driving[currentDataIdx];
	command.iswrite =1; //0: read, 1:write
	command.clock_freq =0;
	command.result = -1;
	ret = ioctl(sd_fd, -1, (void *)&command);
	if(ret < 0 || -1 == command.result)
	{
		LOGD("----error: can't call mt6573_sd()----, error:%d, fd:%d\n",ret, sd_fd);
		return JNI_FALSE;
	}
	    
    close(sd_fd);
    return JNI_TRUE;
  	
  }

