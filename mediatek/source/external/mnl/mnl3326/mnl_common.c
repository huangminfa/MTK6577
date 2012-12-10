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

#define _MNL_COMMON_C_
/*******************************************************************************
* Dependency
*******************************************************************************/
#include <stdio.h>   /* Standard input/output definitions */
#include <string.h>  /* String function definitions */
#include <stdlib.h>
#include <sys/time.h>
#include <sys/timeb.h>
#include "mnl_common.h"

/*******************************************************************************
* structure & enumeration
*******************************************************************************/

/****************************************************************************** 
 * Functions
******************************************************************************/
#if defined(READ_PROPERTY_FROM_FILE)
/******************************************************************************
Sample for mnl.prop: (for emulator)
-------------------------------------------------------------------------------
init.speed=38400
link.speed=38400
dev.dsp=/dev/ttyS3
dev.gps=/dev/gps 
bee.path=/bee
pmtk.conn=socket        
pmtk.socket.port=7000
#pmtk.conn=serial
#pmtk.serial.dev=/dev/ttygserial
debug.nema=1 (0:none; 1:normal; 2:full)
debug.mnl=1 
******************************************************************************/
const char *mnl_prop_path[] = {
    "/data/misc/mnl.prop",   /*mainly for target*/    
    "/sbin/mnl.prop",   /*mainly for emulator*/
};
#define PROPBUF_SIZE 512
static char propbuf[512];
#define IS_SPACE(ch) ((ch == ' ') || (ch == '\t') || (ch == '\n'))
/******************************************************************************
* Read property from file and overwritting the existing property
******************************************************************************/
int get_prop(char *pStr, char** ppKey, char** ppVal)
{
    int len = (int)strlen(pStr);
    char *end = pStr + len;
    char *key = NULL, *val = NULL;
    int stage = 0;

    if (!len)
    {
        return -1;    //no data    
	} 
    else if (pStr[0] == '#') 
    {   /*ignore comment*/
		*ppKey = *ppVal = NULL;	
		return 0;
    } 
    else if (pStr[len-1] != '\n') 
    {
        if (len >= PROPBUF_SIZE-1) 
        {
            MNL_ERR("%s: buffer is not enough!!\n", __FUNCTION__);            
			return -1;
        } 
        else 
        {
            pStr[len-1] = '\n';
        }
    }
    key = pStr;
    while((*pStr != '=') && (pStr < end)) pStr++;
    if (pStr >= end)
	{
		MNL_ERR("%s: '=' is not found!!\n", __FUNCTION__);
        return -1;    //format error
	}

    *pStr++ = '\0';        
    while(IS_SPACE(*pStr) && (pStr < end)) pStr++;    //skip space chars
    val = pStr;
    while(!IS_SPACE(*pStr) && (pStr < end)) pStr++;
    *pStr = '\0';
	*ppKey = key;
	*ppVal = val;
	return 0;
    
}
/*****************************************************************************/
int set_prop(MNL_CONFIG_T* prConfig, char* key, char* val)
{
	if (!strcmp(key, "init.speed")) 
    {
        prConfig->init_speed = atoi(val);
	} 
    else if (!strcmp(key, "link.speed")) 
    {
        prConfig->link_speed = atoi(val);
	} 
    else if (!strcmp(key, "dev.dsp")) 
    {   
		if (strlen(val) < sizeof(prConfig->dev_dsp))
            strcpy(prConfig->dev_dsp, val);
	}
    else if (!strcmp(key, "dev.gps")) 
    {
	    if (strlen(val) < sizeof(prConfig->dev_gps))
	        strcpy(prConfig->dev_gps, val);
	} 
    else if (!strcmp(key, "bee.path")) 
	{
	    if (strlen(val) < sizeof(prConfig->bee_path))
            strcpy(prConfig->bee_path, val);
	} 
    else if (!strcmp(key, "pmtk.serial.dev")) 
    {
	    if (strlen(val) < sizeof(prConfig->dev_dbg))
            strcpy(prConfig->dev_dbg, val);
	}
    else if (!strcmp(key, "pmtk.conn")) 
    {
	    if (!strcmp(val, "serial"))
            prConfig->pmtk_conn = PMTK_CONNECTION_SERIAL;
        else if (!strcmp(val, "socket"))
            prConfig->pmtk_conn = PMTK_CONNECTION_SOCKET;
	} 
    else if (!strcmp(key, "pmtk.serial.port")) 
    {
	    prConfig->socket_port = atoi(val);
	}
    else if (!strcmp(key, "debug.mnl"))
    {   
        prConfig->debug_mnl  = strtol(val, NULL, 16);
    }      
    else if (!strcmp(key, "debug.nmea2file"))
    {
        prConfig->nmea2file = (atoi(val) > 0) ? (1) : (0);
        if(prConfig->nmea2file > 0)
        {   /*it will be set to enable_dbg_log*/          
            prConfig->debug_log = 1;
        }
    }    
    else if (!strcmp(key, "debug.dbg2file"))
    {
        prConfig->dbg2file = (atoi(val) > 0) ? (1) : (0);
        if(prConfig->dbg2file > 0)
        {   /*it will be set to enable_dbg_log*/
            prConfig->debug_log =1;
        }
    }
    else if (!strcmp(key, "debug.nmea2socket"))
    {
        prConfig->nmea2socket = (atoi(val) > 0) ? (1) : (0);
    }
    else if (!strcmp(key, "debug.dbg2socket"))
    {
        prConfig->dbg2socket = (atoi(val) > 0) ? (1) : (0);
    }
    else if (!strcmp(key, "timeout.monitor"))
    {
        prConfig->timeout_monitor = atoi(val);
    }
    else if (!strcmp(key, "timeout.init"))
    {
        prConfig->timeout_init = atoi(val);
    }
    else if (!strcmp(key, "timeout.sleep"))
    {
        prConfig->timeout_sleep = atoi(val);
    }
    else if (!strcmp(key, "timeout.pwroff"))
    {
        prConfig->timeout_pwroff = atoi(val);
    }
    else if (!strcmp(key, "timeout.wakeup"))
    {
        prConfig->timeout_wakeup = atoi(val);
    }
    else if (!strcmp(key, "timeout.ttff"))
    {
        prConfig->timeout_ttff = atoi(val);
    }
    else if (!strcmp(key, "delay.reset_dsp"))
    {
        prConfig->delay_reset_dsp = atoi(val);
    }
    else if(!strcmp(key, "EPO_enabled"))
    {
        prConfig->EPO_enabled = atoi(val);
    }
    else if(!strcmp(key, "BEE_enabled"))
    {
        prConfig->BEE_enabled = atoi(val);
    }
    else if(!strcmp(key, "SUPL_enabled"))
    {
        prConfig->SUPL_enabled = atoi(val);
    }
    else if(!strcmp(key, "SUPLSI_enabled"))
    {
        prConfig->SUPLSI_enabled = atoi(val);
    }
    else if(!strcmp(key, "EPO_priority"))
    {
        prConfig->EPO_priority = atoi(val);
    }
    else if(!strcmp(key, "BEE_priority"))
    {
        prConfig->BEE_priority = atoi(val);
    }
    else if(!strcmp(key, "SUPL_priority"))
    {
        prConfig->SUPL_priority = atoi(val);
    }
        
	return 0;
}
/*****************************************************************************/
int read_prop(MNL_CONFIG_T* prConfig, const char* name)
{
    FILE *fp = fopen(name, "rb");
	char *key, *val;
    if (!fp){
        MNL_MSG("%s: open %s fail!\n",__FUNCTION__,name);
        return -1;
    }
    while(fgets(propbuf, sizeof(propbuf), fp))
    {
        if (get_prop(propbuf, &key, &val))
        {       
			MNL_MSG("%s: Get Property fails!!\n", __FUNCTION__);
			return -1;
		}
		if (!key || !val)
			continue;
		if (set_prop(prConfig, key,val))
        {
			MNL_ERR("%s: Set Property fails!!\n", __FUNCTION__);
			return -1;
		}		
    }
    fclose(fp);
    return 0;
}
/*****************************************************************************/
int mnl_utl_load_property(MNL_CONFIG_T* prConfig) 
{
    int idx;
    int cnt = sizeof(mnl_prop_path)/sizeof(mnl_prop_path[0]); 
    int res;
    for (idx = 0; idx < cnt; idx++) 
    {
        if (!read_prop(prConfig, mnl_prop_path[idx]))
            break;
    }

    MNL_MSG("========================================\n");
    if (idx < cnt) /*successfully read property from file*/  {
        MNL_MSG("[setting] reading from %s\n", mnl_prop_path[idx]);
        res = 0; 
    } else {
        MNL_MSG("[setting] load default value\n");
        res = -1;
    }
    MNL_MSG("dev_dsp/dev_gps : %s %s\n", prConfig->dev_dsp, prConfig->dev_gps);
    MNL_MSG("init_speed/link_speed: %d %d\n", prConfig->init_speed, prConfig->link_speed);
    MNL_MSG("pmtk_conn/socket_port/dev_dbg : %d %d %s\n", prConfig->pmtk_conn, prConfig->socket_port, 
                             prConfig->dev_dbg);
    MNL_MSG("debug_log/debug_mnl: %d 0x%04X\n", prConfig->debug_log, prConfig->debug_mnl);
    MNL_MSG("nmea2file/dbg2file: %d/%d\n", prConfig->nmea2file, prConfig->dbg2file);
    MNL_MSG("time-out: %d %d %d %d %d %d\n", prConfig->timeout_init, prConfig->timeout_monitor, prConfig->timeout_wakeup,
                                    prConfig->timeout_ttff, prConfig->timeout_sleep, prConfig->timeout_pwroff);
    MNL_MSG("EPO_Enabled: %d\n", prConfig->EPO_enabled);
    MNL_MSG("BEE_Enabled: %d\n", prConfig->BEE_enabled);
    MNL_MSG("SUPL_Enabled: %d\n", prConfig->SUPL_enabled);
    MNL_MSG("SUPLSI_Enabled: %d\n", prConfig->SUPLSI_enabled);
    MNL_MSG("EPO_priority: %d\n", prConfig->EPO_priority);
    MNL_MSG("BEE_priority: %d\n", prConfig->BEE_priority);
    MNL_MSG("SUPL_priority: %d\n", prConfig->SUPL_priority);
    MNL_MSG("========================================\n");   
    return res;
}
/*****************************************************************************/
#endif 
