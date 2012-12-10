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

// gpstask.c : Defines the entry point for the console application.
//

#define _MNL_PROCESS_C_
#include "mnl_linux.h"
#include "MTK_BEE.h"
#include <sys/epoll.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <sys/stat.h>
#include "CFG_GPS_File.h"

/******************************************************************************
* Configuration
******************************************************************************/
#define PMTK_SERVER_BACKLOG   5
#define AGENT_LOG_ENABLE      1
/******************************************************************************
* Structure & enumeration
******************************************************************************/
typedef enum 
{
    MNL_THREAD_UNKNOWN        = -1,
    MNL_THREAD_DSP_INPUT      = 0,
    MNL_THREAD_MNL            = 1,
    MNL_THREAD_PMTK_INPUT     = 2,
    MNL_THREAD_AGENT          = 3,
    MNL_THREAD_AGPSDISPATCH   = 4,
#if defined(SUPPORT_HOTSTILL)    
    MNL_THREAD_BEE            = 5,
#endif    
    MNL_THREAD_NUM,
    MNL_THREAD_LAST           = 0x7FFFFFFF
} MNL_THREAD_ID_T;
/*---------------------------------------------------------------------------*/
typedef struct _MNL_THREAD_T
{
    int                 snd_fd;     
    MNL_THREAD_ID_T     thread_id;
    pthread_t           thread_handle;
    int (*thread_exit)(struct _MNL_THREAD_T *arg);
    int (*thread_active)(struct _MNL_THREAD_T *arg);   
} MNL_THREAD_T;
/*---------------------------------------------------------------------------*/
typedef struct /*internal data structure for single process*/
{
    int dae_rcv_fd; /*receive message from daemon*/
    int dae_snd_fd; /*send message to daemon*/
    int sig_rcv_fd; /*receive message from queue containing internal signal*/
    int sig_snd_fd; /*send message to queue containing internal signal*/
    int (*notify_alive)(int snd_fd);
} MNL_SINPROC_T;
/******************************************************************************
* Global variable
******************************************************************************/
/******************************************************************************
* static variable
******************************************************************************/
// Thread of dsp input data, mnl task
#if defined(SUPPORT_HOTSTILL)
static volatile int g_ThreadExitBEE          = 0;
#endif
static volatile int g_ThreadExitDSPIn        = 0;
static volatile int g_ThreadExitAgent        = 0;
static volatile int g_ThreadExitAgpsDispatch = 0;
static void mtk_sys_agps_set_profile(const mtk_agps_user_profile *pUser_profile);

/*****************************************************************************/
FILE *dbglog_fp = NULL;
mtk_bool enable_dbg_log = MTK_GPS_FALSE;
#ifdef DSP_IMG//mike
#define DSP_BIN_MAX_SIZE  (65536)
static unsigned char *dsp_img = NULL;
#endif

/*****************************************************************************/
MNL_CONFIG_T mnl_config = 
{
    .init_speed = 38400, 
    .link_speed = 921600,
    .debug_log = 0, 
    .debug_mnl  = MNL_NMEA_DEBUG_NONE, 
    .pmtk_conn  = PMTK_CONNECTION_SOCKET, 
    .socket_port = 7000,
    .dev_dbg = DBG_DEV, 
    .dev_dsp = DSP_DEV, 
    .dev_gps = GPS_DEV, 
    .bee_path = BEE_PATH, 
    .delay_reset_dsp = 500,
    .nmea2file = 0,
    .dbg2file = 0,
    .nmea2socket = 1,
    .dbg2socket = 0,
    .timeout_init = 0, 
    .timeout_monitor = 0, 
    .timeout_wakeup = 0, 
    .timeout_sleep = 0, 
    .timeout_pwroff = 0, 
    .timeout_ttff = 0,
    .EPO_enabled = 0,
    .BEE_enabled = 1,
    .SUPL_enabled = 0,
    .SUPLSI_enabled = 0,
    .EPO_priority = 64,
    .BEE_priority = 32,
    .SUPL_priority = 96  
};

ap_nvram_gps_config_struct stGPSReadback;
int gps_nvram_valid = 0;

/*****************************************************************************/
#define GPS_RETRY_NUM   5
enum {
    GPS_PWRCTL_UNSUPPORTED  = 0xFF,
    GPS_PWRCTL_OFF          = 0x00,
    GPS_PWRCTL_ON           = 0x01,
    GPS_PWRCTL_RST          = 0x02,
    GPS_PWRCTL_OFF_FORCE    = 0x03,
    GPS_PWRCTL_RST_FORCE    = 0x04,
    GPS_PWRCTL_MAX          = 0x05,
};
#define MNL_ATTR_PWRCTL  "/sys/class/gpsdrv/gps/pwrctl"
#define MNL_ATTR_SUSPEND "/sys/class/gpsdrv/gps/suspend"
#define MNL_ATTR_STATE   "/sys/class/gpsdrv/gps/state"
#define MNL_ATTR_PWRSAVE "/sys/class/gpsdrv/gps/pwrsave"
#define MNL_ATTR_STATUS  "/sys/class/gpsdrv/gps/status"

static int mnl_write_attr(const char *name, unsigned char attr) 
{
    int err, fd = open(name, O_RDWR);
    char buf[] = {attr + '0'};
    
    if (fd == -1) {
        MNL_ERR("open %s err = %s\n", name, strerror(errno));
        return -errno;
    }
    do { err = write(fd, buf, sizeof(buf) ); }
    while (err < 0 && errno == EINTR);
    
    if (err != sizeof(buf)) { 
        MNL_ERR("write fails = %s\n", strerror(errno));
        err = -errno;
    } else {
        err = 0;    /*no error*/
    }
    if (close(fd) == -1) {
        MNL_ERR("close fails = %s\n", strerror(errno));
        err = (err) ? (err) : (-errno);
    }
    MNL_MSG("write '%d' to %s okay\n", attr, name);    
    return err;
}

static int gps_hw_check(void)
{
    int rty_cnt = 0;
	ssize_t bytewrite = 0, byteread = 0;
	uint8_t query_cmd[]  = {0x04, 0x24, 0x0b, 0x00, 0x08, 0xff, 0x19, 0x00, 0xe5, 0x0d, 0x0a};
    uint8_t query_response[]  = {0x04, 0x24, 0x0b, 0x00, 0x1d, 0xff, 0x01, 0xaa, 0x42, 0x0d, 0x0a};
    uint8_t buf[12] = {0};

    while(rty_cnt < GPS_RETRY_NUM)
    {
        mtk_sys_if_set_spd (38400, 0);

		MNL_MSG("query_cmd:%02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x\n",
			       query_cmd[0],query_cmd[1], query_cmd[2], query_cmd[3], query_cmd[4] ,query_cmd[5],query_cmd[6], query_cmd[7],
			       query_cmd[8],query_cmd[9], query_cmd[10]);
		
        bytewrite = write(dsp_fd, query_cmd, sizeof(query_cmd));
        if (bytewrite == sizeof(query_cmd))
        {
            usleep(500000);
			byteread = read(dsp_fd, buf, sizeof(buf));
			MNL_MSG("query response:%02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x\n",
			         buf[0],buf[1], buf[2], buf[3], buf[4], buf[5], buf[6], buf[7],
			         buf[8],buf[9], buf[10]);
			if((byteread == sizeof(query_response)) && (memcmp(buf, query_response, sizeof(query_response)) == 0))
			{ 
			    MNL_MSG("gps chip power on get ack\n");	
			    break;
            }
        }
		
        //force reset chip
        mnl_write_attr(MNL_ATTR_PWRCTL, GPS_PWRCTL_RST_FORCE);
		rty_cnt++;
	}
	if(rty_cnt == GPS_RETRY_NUM)
	{
	    return -1;
    }
    else
	{
	    MNL_MSG("gps chip power on success:%d\n", rty_cnt);	
		return 0;
    }
}

/*****************************************************************************/
int exit_thread_normal(MNL_THREAD_T *arg);
int exit_thread_extra(MNL_THREAD_T *arg);
int thread_active_notify(MNL_THREAD_T *arg);
/*---------------------------------------------------------------------------*/
static MNL_THREAD_T mnl_thread[MNL_THREAD_NUM] = {
    {C_INVALID_FD, MNL_THREAD_DSP_INPUT,      C_INVALID_TID, exit_thread_normal, thread_active_notify},
    {C_INVALID_FD, MNL_THREAD_MNL,            C_INVALID_TID, exit_thread_extra,  thread_active_notify},
    {C_INVALID_FD, MNL_THREAD_PMTK_INPUT,     C_INVALID_TID, exit_thread_normal, thread_active_notify},
	{C_INVALID_FD, MNL_THREAD_AGENT,		  C_INVALID_TID, exit_thread_normal, thread_active_notify},
	{C_INVALID_FD, MNL_THREAD_AGPSDISPATCH,	  C_INVALID_TID, exit_thread_normal, thread_active_notify},
#if defined(SUPPORT_HOTSTILL)    
    {C_INVALID_FD, MNL_THREAD_BEE,            C_INVALID_TID, exit_thread_normal, thread_active_notify},
#endif 
};

/*****************************************************************************/
int send_active_notify(int snd_fd);
/*---------------------------------------------------------------------------*/
static MNL_SINPROC_T mnl_sinproc = 
{
    .dae_rcv_fd = C_INVALID_FD,
    .dae_snd_fd = C_INVALID_FD,
    .sig_rcv_fd = C_INVALID_FD,
    .sig_snd_fd = C_INVALID_FD,
    .notify_alive = send_active_notify,
};
/******************************************************************************
*   MNL Implementation
******************************************************************************/
void
write_full (int hHandle, const char* buffer, mtk_uint32 size)
{
    if (nmea_debug_level & MNL_NEMA_DEBUG_SENTENCE)
    	MNL_MSG("%s\n", buffer);
    
	if (hHandle != -1)
	{
        write(hHandle, buffer, size);
	}
}
/*****************************************************************************/
static void
dump_nmea_data (void)
{
	mtk_gps_position pvt;
	int  slen, i, cnt, olen, idx;
	char strbuf[1024], outbuf[1024];

	if ((gps_fd == -1) ||
		(mtk_gps_get_position(&pvt) != MTK_GPS_SUCCESS))
	{
        MNL_ERR("mtk_gps_get_position error: (%d)\n", gps_fd);
		return;
	}

	/* U.S. output permission */
	if ((pvt.gspeed > 515.0f) || (pvt.LLH[2] > 18270.0))
	{
        MNL_ERR("U.S. output permission: (%.2f, %.2f, %d)\n", pvt.gspeed, pvt.LLH[2], pvt.fix_type);
        if (pvt.fix_type != MTK_GPS_FIX_TYPE_INVALID)
    		return;
	}

	/* GPGGA */
	sprintf(strbuf,
	        "GPGGA,%02d%02d%02d.%03d,%09.4f,%c,%010.4f,%c,%d,%d,",
            pvt.utc_time.hour, pvt.utc_time.min,
			pvt.utc_time.sec, pvt.utc_time.msec,
			translate_nmea_deg_min_sec(pvt.LLH[0]), ((pvt.LLH[0] >= .0) ? 'N' : 'S'),
            translate_nmea_deg_min_sec(pvt.LLH[1]), ((pvt.LLH[1] >= .0) ? 'E' : 'W'),
			pvt.fix_quality, pvt.sv_used_num);
	slen = strlen(strbuf);
	if (pvt.fix_type != MTK_GPS_FIX_TYPE_INVALID)
	{
		sprintf(&strbuf[slen], "%.2f", pvt.HDOP);
		slen += strlen(&strbuf[slen]);
	}
    sprintf(&strbuf[slen], ",%.1f,M,%.1f,M",
		    pvt.LLH[3], pvt.LLH[2] - pvt.LLH[3]);
	slen += strlen(&strbuf[slen]);
	if (pvt.fix_quality == MTK_GPS_FIX_Q_DGPS)
	{
		sprintf(&strbuf[slen], ",%.1f,%d",
			    pvt.DGPS_age, pvt.DGPS_station_ID);
	}
	else
	{
		sprintf(&strbuf[slen], ",,");
	}
    idx = olen = sprintf(outbuf, "$%s*%02X\r\n", strbuf, calc_nmea_checksum(strbuf));
    //DBGOUT(outbuf, olen);
	//write_full(gps_fd, outbuf, strlen(outbuf));

	/* GPGSA */
	sprintf(strbuf,
			"GPGSA,A,%d", pvt.fix_type);
    slen = strlen(strbuf);
    for (i = 0; i < 12; i++)
	{
		if (pvt.sv_used_prn_list[i])
		{
			sprintf(&strbuf[slen], ",%02d", pvt.sv_used_prn_list[i]);
		}
		else
		{
			sprintf(&strbuf[slen], ",");
		}
        slen += strlen(&strbuf[slen]);
	}
	sprintf(&strbuf[slen], ",%.2f,%.2f,%.2f",
			pvt.PDOP, pvt.HDOP, pvt.VDOP);
    olen = sprintf(outbuf + idx, "$%s*%02X\r\n", strbuf, calc_nmea_checksum(strbuf));
    //DBGOUT(outbuf + idx, olen);
    idx += olen;
	//write_full(gps_fd, outbuf, strlen(outbuf));

	/* GPGSV */
    cnt = (pvt.sv_in_view_num + 3) / 4;
	if (cnt > 0)
	{
		for (i = 0; i < cnt; i++)
		{
			int svcnt;

			sprintf(strbuf, "GPGSV,%d,%d,%d", cnt, i+1, pvt.sv_in_view_num);
			slen = strlen(strbuf);
			for (svcnt = (i * 4); (svcnt < ((i+1) * 4)) && (svcnt < pvt.sv_in_view_num); svcnt++)
			{
				sprintf(&strbuf[slen], ",%02d,%02d,%03d,",
				    pvt.sv_in_view_prn_list[svcnt],
					pvt.sv_in_view_elev[svcnt],
					pvt.sv_in_view_azim[svcnt]);
				slen += strlen(&strbuf[slen]);
				if (pvt.sv_in_view_snr[svcnt] > 0)
				{
					sprintf(&strbuf[slen], "%d", pvt.sv_in_view_snr[svcnt]);
					slen += strlen(&strbuf[slen]);
				}
			}
			olen = sprintf(outbuf + idx, "$%s*%02X\r\n", strbuf, calc_nmea_checksum(strbuf));
			//DBGOUT(outbuf + idx, olen);
			idx += olen;
			//write_full(gps_fd, outbuf, strlen(outbuf));
		}
	}

	/* GPRMC */
	sprintf(strbuf,
	        "GPRMC,%02d%02d%02d.%03d,%c,%09.4f,%c,%010.4f,%c,%.3f,%.2f,%02d%02d%02d,,%c",
            pvt.utc_time.hour, pvt.utc_time.min,
			pvt.utc_time.sec, pvt.utc_time.msec,
            (pvt.fix_type >= MTK_GPS_FIX_TYPE_2D ? 'A' : 'V'),
			translate_nmea_deg_min_sec(pvt.LLH[0]), ((pvt.LLH[0] >= .0) ? 'N' : 'S'),
            translate_nmea_deg_min_sec(pvt.LLH[1]), ((pvt.LLH[1] >= .0) ? 'E' : 'W'),
            pvt.gspeed * 1.942795467, pvt.heading,
            pvt.utc_time.mday, pvt.utc_time.month + 1,
			pvt.utc_time.year % 100,
			(pvt.fix_type >= MTK_GPS_FIX_TYPE_2D ?
			(pvt.fix_quality == MTK_GPS_FIX_Q_DGPS ? 'D' : 'A') :
	        (pvt.fix_quality == MTK_GPS_FIX_Q_EST ? 'E' : 'N')));
	olen = sprintf(outbuf + idx, "$%s*%02X\r\n", strbuf, calc_nmea_checksum(strbuf));
	//DBGOUT(outbuf + idx, olen);
	idx += olen;
	//write_full(gps_fd, outbuf, strlen(outbuf));

	/* GPVTG */
	sprintf(strbuf,
	        "GPVTG,%.2f,T,,M,%.3f,N,%.3f,K,%c",
			pvt.heading,
			pvt.gspeed * 1.942795467,
			pvt.gspeed * 3.6,
            (pvt.fix_type >= MTK_GPS_FIX_TYPE_2D ?
			(pvt.fix_quality == MTK_GPS_FIX_Q_DGPS ? 'D' : 'A') :
	        (pvt.fix_quality == MTK_GPS_FIX_Q_EST ? 'E' : 'N')));
	olen = sprintf(outbuf + idx, "$%s*%02X\r\n", strbuf, calc_nmea_checksum(strbuf));
	//DBGOUT(outbuf + idx, olen);
	idx += olen;
	
	//Lichunhui Add accuracy 
	/* GPACCURACY */
	sprintf(strbuf,
	        "GPACCURACY,%.1f",
			pvt.Pos_3D_Accuracy);  //3D accuracy
	olen = sprintf(outbuf + idx, "$%s*%02X\r\n", strbuf, calc_nmea_checksum(strbuf));
	//DBGOUT(outbuf + idx, olen);
	idx += olen;
	mtk_sys_nmea_output(outbuf, idx);
}
/*****************************************************************************/
// callback function from MNL main thread
static mtk_int32
linux_gps_callback (mtk_gps_notification_type msg)
{
	switch (msg)
	{
	case MTK_GPS_MSG_FIX_READY:
        dump_nmea_data();
        mtk_gps_debug_wrapper(1);
        mtk_gps_debug_wrapper(2);
        if (mnl_sinproc.notify_alive)
        {
            mnl_sinproc.notify_alive(mnl_sinproc.dae_snd_fd);
        }
		break;
    case MTK_GPS_MSG_FIX_PROHIBITED:
    {
        MNL_MSG("MTK_GPS_MSG_FIX_PROHIBITED");        
        if (mnl_sinproc.notify_alive)
        {
            mnl_sinproc.notify_alive(mnl_sinproc.dae_snd_fd);
        }
        break;    
    }
		
	default:
		break;
	}

	return  MTK_GPS_SUCCESS;
}
/*****************************************************************************/
void * thread_pmtk_input_func(void * arg)
{
    int exit = 0;
    ssize_t bytes = 0, j = 0, i;
    char buf[PMTK_UART_IN_BUFFER_SIZE];
    char cmd[PMTK_CMD_BUFFER_SIZE];
    
	MNL_MSG("thread pmtk create: %.8X\n", (unsigned int)pthread_self());

    while(!exit)
    {
		bytes = read(dbg_fd, buf, PMTK_UART_IN_BUFFER_SIZE);

        if (bytes > 0) {
#if 0
            for (i = 0; i < bytes, j < PMTK_CMD_BUFFER_SIZE; i++) {
                cmd[j] = buf[i];
                if (j && ((cmd[j - 1] == '\r') && (cmd[j] == '\n'))) {
                    mtk_sys_nmea_input(cmd, j + 1);
                    j = 0;
                } else {
                    j++;
                }                
            }
            if (j == PMTK_CMD_BUFFER_SIZE) {
                MNL_MSG("pmtk cmd buffer full\n");
                j = 0;
            }
#else
            MNL_MSG(" %s \n", buf);
            if((!strncmp(buf, "$PMTK101", strlen("$PMTK101")))||
               (!strncmp(buf, "$PMTK102", strlen("$PMTK102")))||
               (!strncmp(buf, "$PMTK103", strlen("$PMTK103")))||
               (!strncmp(buf, "$PMTK104", strlen("$PMTK104")))) {
                mtk_gps_msg *pMsg = NULL;         
                pMsg = mtk_sys_msg_alloc(sizeof(mtk_gps_msg)); /*not need free*/
                pMsg->srcMod = MTK_MOD_END_LIST;
                pMsg->dstMod = MTK_MOD_END_LIST;
                pMsg->type = MTK_AGPS_MSG_END_LIST;
                pMsg->length = 0;

                mtk_sys_agps_msg_reset();                
                
                MNL_MSG("GPS Restart, force IDLE mode(Command directly)\n");
                if(MTK_GPS_ERROR == mtk_sys_agps_msg_send(pMsg))
                {
        	          mtk_sys_msg_free(pMsg);  
                }    
            }
            mtk_gps_nmea_input(buf, bytes);
#endif
        } else {
            if (errno == EINTR) {
                MNL_MSG("thread pmtk input exit \n");
                break;
            } else if (bytes == 0) { /*client close*/    
                MNL_MSG("client %d close\n", dbg_fd);
                break;
            } else {
                MNL_MSG("pmtk sleep 200ms. bytes=%d\n", (int)bytes);
                usleep(200000);  // sleep 200 ms
            }
        }
    }
	return NULL;
}
/*****************************************************************************/
void * thread_mnl_func(void * arg)
{
	MNL_MSG("thread mnl create : %.8X\n", (unsigned int)pthread_self());
	if (MTK_GPS_SUCCESS == mtk_gps_run(linux_gps_callback))
 	{
		MNL_MSG("thread mnl exit\n");
	}
	else
	{
#if 0
		while(1)
		{
			sleep(2);
			MNL_MSG("thread mnl mtk_gps_run is not working\n");
		}
#endif
	}
	MNL_MSG("thread mnl return\n");
    pthread_exit(NULL);
	return NULL;
}
/*****************************************************************************/
void signal_handler(int status)
{
}
/*****************************************************************************/
#if defined(SUPPORT_HOTSTILL)
void * thread_bee_func(void * arg)
{
    int ret;

	MNL_MSG("thread bee create\n");

    ret = mtk_sys_bee_event_create();
    if(ret == MTK_GPS_ERROR)
    {
        MNL_ERR("BEE event create fail\n");
    }
    else
    {
        MNL_MSG("BEE event create ok\n");
    }

    ret = mtk_gps_bee_init((mtk_uint8*)mnl_config.bee_path);
    if(ret == MTK_GPS_ERROR)
    {
        MNL_ERR("BEE init fail\n");
    }
    else
    {
        MNL_MSG("BEE init ok\n");
    }

    while(!g_ThreadExitBEE)
    {
        mtk_sys_bee_event_wait();    //block here until get BEE event
        //for test
        MNL_MSG("mtk_gps_bee_gen\n");
        mtk_gps_bee_gen();
    }
	
    mtk_sys_bee_event_delete();

	return  MTK_GPS_SUCCESS;
}
#endif
/*****************************************************************************/
static const char hexTABLE[] =
{
   0x30, 0x31, 0x32, 0x33,  // Characters '0' - '3'
   0x34, 0x35, 0x36, 0x37,  // Characters '4' - '7'
   0x38, 0x39, 0x41, 0x42,  // Characters '8' - 'B'
   0x43, 0x44, 0x45, 0x46   // Characters 'C' - 'F'
};
static char uart_buf_temp_debug[DSP_UART_IN_READ_SIZE*2+2];
/*****************************************************************************/
void * thread_dsp_input_func(void * arg)
{
	mtk_uint32 ret;
    MNL_THREAD_T *ptr = (MNL_THREAD_T*)arg;
    
    if (!arg) {
        MNL_MSG("FATAL error!!\n");
        pthread_exit(NULL);
        return NULL;
    }
    
	MNL_MSG("thread dsp create : %.8X\n", (unsigned int)pthread_self());
    
	// allow the process to receive SIGIO
  	//fcntl(dsp_fd, F_SETOWN, getpid());

	while (!g_ThreadExitDSPIn)
	{
		ssize_t res;
		char buffer[DSP_UART_IN_READ_SIZE];

		res = read(dsp_fd, buffer, DSP_UART_IN_READ_SIZE);
        if(g_ThreadExitDSPIn)
        {
            MNL_MSG("g_ThreadExitDSPIn:%d\n", g_ThreadExitDSPIn);
            ret = 0xf7;
            break;
        }
        
		if (res > 0)
		{
            mtk_uint32 length = (mtk_uint32)res;
			mtk_uint32 offset = 0;
			if (nmea_debug_level & MNL_NMEA_DEBUG_RX_FULL)
			{
			    int i;
			    time_t tm;
                struct tm *p;
                struct timeb tp;
                char msg[512];
                int len = 0;

                time(&tm);
                ftime(&tp);
                p = localtime(&tm);                    
			    
                MNL_MSG("[MNL_DSP] RX %d bytes: (time=%d/%d/%d %.2d:%.2d:%.2d.%d)\n", 
                    length, 1900 + p->tm_year, 1 + p->tm_mon, p->tm_mday,
                    p->tm_hour, p->tm_min, p->tm_sec, tp.millitm);
    
                for (i = 0; i < (int)length; i++) {
                    len += sprintf(msg + len, "%.2x ", (unsigned char)buffer[i]);
                    if ((i + 1) % 16 == 0) {
                        MNL_MSG("%s\n", msg);
                        len = 0;
                    }
                }
                if (len)
                    MNL_MSG("%s\n", msg);
			}
            else if (nmea_debug_level & MNL_NMEA_DEBUG_RX_PARTIAL)
            {
			    time_t tm;
                struct tm *p;
                struct timeb tp;
                char msg[512] = {0};
                char dat[4] = {0,0,0,0};
                int head = 2, last = 23;
                int min = ((int)length > head) ? head : (int)length;
                int i, beg, len = 0, end = 0;
                                
                time(&tm);
                ftime(&tp);
                p = localtime(&tm);


                for (i = 0; i < min; i++) 
                    len += sprintf(msg+len, "%.2x ", (unsigned char)buffer[i]);

                if ((head+last) < (int)length) {
                    len += sprintf(msg+len, ".. ");
                    i++;
                    beg = length-last;
                    end = last;
                } else {
                    beg = i;
                    end = length;
                }

                for (; i < (int)end; i++) 
                    len += sprintf(msg + len, "%.2x ", (unsigned char)buffer[beg++]);
                                
                MNL_MSG("[MNL] RX %4d bytes: (%.4d/%.2d/%.2d %.2d:%.2d:%.2d.%3d) %s\n",
                (int)length, 1900 + p->tm_year, 1 + p->tm_mon, p->tm_mday,
                p->tm_hour, p->tm_min, p->tm_sec, tp.millitm, msg);
                //MNL_MSG("[MNL_DSP] %s\n", msg);
            }
			while (offset < length)
			{
				mtk_uint32 used;
				mtk_int32  res;
				if (nmea_debug_level & MNL_NMEA_DEBUG_RX_FULL)
				{
    				int debug_len;
    				memset(uart_buf_temp_debug, '\0', sizeof(uart_buf_temp_debug));
    				for (debug_len = 0 ; debug_len < (int)length; debug_len++)
    				{
    				    uart_buf_temp_debug[debug_len*2] = hexTABLE[((buffer[debug_len] >> 4) & 0x0000000F)];
    				    uart_buf_temp_debug[debug_len*2+1] = hexTABLE[(buffer[debug_len] & 0x0000000F)];
    				}
                	MNL_MSG("Packet:%s \n", uart_buf_temp_debug);
			    }
				if ((res = mtk_gps_data_input(&buffer[offset], length - offset, &used)) != MTK_GPS_SUCCESS) {
					MNL_MSG("mtk_gps_data_input return %d\n", res);
					//exit = 2;  // Set 2 as error condition, this is not fatal error, it could be ignored.
					break;
				}
				if (used != (length - offset))
					MNL_MSG("mtk_gps_data_input buffer full (offset=%d, length=%d, used = %d)!!\n", offset, length, used);

				offset += used;
				if (0 == used) {
				    MNL_MSG("mtk_gps_data_input ignore input (used=0)\n");
				    break;
					//sleep(0);  // yield cpu usage a while
				}
			}
		} 
        else {
            if (errno == EINTR) {
                MNL_ERR("thread dsp input exit\n");
                break;
            } else {
                MNL_MSG("UART polling (length=%d)\n", (int)res);
                usleep(50000);  // sleep 50 ms
            }
		}
	}
    
	MNL_MSG("thread dsp end with ret=%x\n",ret);
	pthread_exit((void *)ret);
	return NULL;
}


/*****************************************************************************/
static void * thread_agent_func(void * arg)
{
	mtk_gps_msg *agps_msg;
	mtk_int32 ret;

    MNL_TRC();
	mtk_agps_agent_init();	
	
    while(!g_ThreadExitAgent)
    {
	   	// - recv msg 
        agps_msg = NULL;   	
        ret = mtk_sys_agps_msg_recv(&agps_msg);        
        if (ret == MTK_GPS_SUCCESS && (!g_ThreadExitAgent))
        {
            mtk_agps_agent_proc(agps_msg);
        }
        else
        {
            //read msg fail or return...
            MNL_ERR("thread agent get msg failed:%d\n", g_ThreadExitAgent);
        }

        // - free msg
        if(agps_msg) 
        {    
            mtk_sys_msg_free(agps_msg);
        }
	}
	
	MNL_MSG("thread agent return\n");
    pthread_exit(NULL);
	return MTK_GPS_SUCCESS;
}


/*****************************************************************************/
static void * thread_agpsdispatch_func(void * arg)
{
    int agpsdispatch_sock = -1, left = 0;
    struct sockaddr_un local;
    struct sockaddr_un remote;
    socklen_t remotelen;
    mtk_uint8 BufIn[MTK_AGPS_PMTK_MAX_SIZE];
    mtk_uint16 type, payload_len;
    mtk_gps_msg *pMsg;
    MNL_TRC();

    if ((agpsdispatch_sock = socket(AF_LOCAL, SOCK_DGRAM, 0)) == -1)
    {
        MNL_ERR("thread_agpsdispatch_func: socket open failed\n");
        return NULL;       
    }

    unlink(MTK_PROFILE2MNL);
    memset(&local, 0, sizeof(local));
    local.sun_family = AF_LOCAL;
    strcpy(local.sun_path, MTK_PROFILE2MNL);

    if (bind(agpsdispatch_sock, (struct sockaddr *)&local, sizeof(local)) < 0 )
    {           
        MNL_ERR("thread_agpsdispatch_func: socket bind failed\n");
        close(agpsdispatch_sock);
        agpsdispatch_sock = -1;       
        return NULL;
    }
        

    while(!g_ThreadExitAgpsDispatch)
    {
        remotelen = sizeof(remote);
        left = recvfrom(agpsdispatch_sock, BufIn, sizeof(BufIn), 0, (struct sockaddr *)&remotelen, &remotelen);
        if (left < 0)
        {
            if (errno != EINTR && errno != EAGAIN)
                MNL_ERR("thread_agpsdispatch_func: recvfrom error\r\n");
            break;
        }

        type = (BufIn[3]<<8 |BufIn[2]);
        payload_len = (mtk_uint16)(BufIn[5]<<8 |BufIn[4]);
        if(MTK_AGPS_SUPL_NI_REQ  == type || MTK_AGPS_SUPL_PMTK_DATA == type || MTK_AGPS_SUPL_END == type)
        {             
            pMsg = mtk_sys_msg_alloc((payload_len + sizeof(mtk_gps_msg)));
            if(pMsg)
            {
                if(MTK_AGPS_SUPL_NI_REQ  == type)
                {
                    pMsg->type = MTK_AGPS_MSG_REQ_NI;
                }
                else if(MTK_AGPS_SUPL_PMTK_DATA == type)
                {
                    pMsg->type = MTK_AGPS_MSG_SUPL_PMTK;
                }                    
                else if(MTK_AGPS_SUPL_END == type)
                {
                    pMsg->type = MTK_AGPS_MSG_SUPL_TERMINATE;
                }
                
                pMsg->srcMod = MTK_MOD_DISPATCHER;
                pMsg->dstMod = MTK_MOD_AGENT;
                pMsg->length = payload_len;
                if (payload_len != 0)
                {
                    memcpy(pMsg->data, (char*)&BufIn[6], payload_len);

                #if AGENT_LOG_ENABLE
                    MNL_MSG("@#$^ [AGNT] RecvMsg (%d %d %d %d %s)\r\n", 
                                        pMsg->srcMod, pMsg->dstMod, pMsg->type, pMsg->length, pMsg->data);
                #endif                         
                }
                else {
                #if AGENT_LOG_ENABLE
                    MNL_MSG("@#$^ [AGNT] RecvMsg (%d %d %d %d) no data\r\n", 
                                        pMsg->srcMod, pMsg->dstMod, pMsg->type, pMsg->length);   
                #endif
                }
                
                if(MTK_GPS_ERROR == mtk_sys_agps_msg_send(pMsg)) //Send to Agent
                {
        	          mtk_sys_msg_free(pMsg);  
                }                              
            }
                
        }                        
        else {                
            MNL_MSG("thread_agpsdispatch_func: msg type invalid\r\n");                
        }
    }
	MNL_MSG("thread agps dispatch return\n");
    close(agpsdispatch_sock);
    agpsdispatch_sock = -1;  
    pthread_exit(NULL);
	return NULL;
}


/*****************************************************************************/
mtk_int32
mtk_sys_agps_disaptcher_callback (mtk_uint16 type, mtk_uint16 length, char *data)
{ 
    mtk_int32 ret = MTK_GPS_SUCCESS;
    
    if (type == MTK_AGPS_CB_SUPL_PMTK || type == MTK_AGPS_CB_SUPL_SI_REQ)
    {
        if (mnl_config.SUPL_enabled)
        {
            int sock2supl = -1;
            struct sockaddr_un local;
            mtk_agps_msg *pMsg = NULL;
            mtk_uint16 total_length = length + sizeof(mtk_agps_msg);
            
            pMsg = (mtk_agps_msg *)malloc(total_length);
            if(pMsg)
            {
           
                if(type == MTK_AGPS_CB_SUPL_PMTK)
                {
                    pMsg->type = MTK_AGPS_SUPL_PMTK_DATA;
                }
                else if(type == MTK_AGPS_CB_SUPL_SI_REQ)
                {
                    pMsg->type = MTK_AGPS_SUPL_ASSIST_REQ;
                }
                pMsg->srcMod = MTK_MOD_GPS;
                pMsg->dstMod = MTK_MOD_SUPL; 
                pMsg->length = length;
                if (pMsg->length != 0)
                {
                    memcpy(pMsg->data, data, length);
                #if AGENT_LOG_ENABLE
                    MNL_MSG("@#$^ [AGNT] SendMsg (%d %d %d %d %s)\r\n", 
                                        pMsg->srcMod, pMsg->dstMod, pMsg->type, pMsg->length, pMsg->data);                              
                #endif
                }
                
                else {     
                #if AGENT_LOG_ENABLE
                    MNL_MSG("@#$^ [AGNT] SendMsg (%d %d %d %d) no data\r\n", 
                                        pMsg->srcMod, pMsg->dstMod, pMsg->type, pMsg->length);
                #endif
                }

                if((sock2supl = socket(AF_LOCAL, SOCK_DGRAM, 0)) == -1)
                {
                    MNL_ERR("@#$^ [AGNT] SendMsg:open sock2supl fails\r\n");
                    free(pMsg); 
                    pMsg = NULL; 
                    return MTK_GPS_ERROR;
                }

                memset(&local, 0, sizeof(local));
                local.sun_family = AF_LOCAL;
                strcpy(local.sun_path, MTK_MNL2SUPL);

                if (sendto(sock2supl, (void *)pMsg, total_length, 0, (struct sockaddr*)&local, sizeof(local)) < 0)
                {
                    MNL_ERR("send message fail:%s\r\n", strerror(errno));
                    ret = MTK_GPS_ERROR;
                }
                close(sock2supl);
                if(pMsg)
                {
                    free(pMsg); 
                    pMsg = NULL;
                }
            }
        }
        else {
            MNL_MSG("mtk_sys_agps_disaptcher_callback: SUPL disable\r\n");    
            ret = MTK_GPS_ERROR;
        } 
    }    
    return ret;
}

/*****************************************************************************/
static void 
mtk_sys_agps_set_profile(const mtk_agps_user_profile *pUser_profile)
{
    mtk_gps_msg *pMsg;
    mtk_uint16 payload_len = sizeof(mtk_agps_user_profile);
    
    pMsg = mtk_sys_msg_alloc(payload_len+ sizeof(mtk_gps_msg));    
    if(pMsg)
    {
        pMsg->srcMod = MTK_MOD_DISPATCHER;
        pMsg->dstMod = MTK_MOD_AGENT;
        pMsg->type = MTK_AGPS_MSG_PROFILE; 
        pMsg->length = payload_len;
        memcpy(pMsg->data, (char*)pUser_profile,payload_len);
        if(MTK_GPS_ERROR == mtk_sys_agps_msg_send(pMsg))
        {
        	   mtk_sys_msg_free(pMsg);  
        }    
                                                  
    }
}

/*****************************************************************************/
int
linux_gps_init (void)
{
    mtk_int32 status;
    static mtk_init_cfg init_cfg;
    mtk_uint8 clk_type = 0xff;  // for new 43EVK board
    FILE *parm_fp;    
    mtk_agps_user_profile userprofile;

    parm_fp = fopen(PARM_FILE, "r");
    if (parm_fp) {
        clk_type = 3;    // 26M integer TCXO
        fclose(parm_fp);
    }
    
    memset(&init_cfg, 0, sizeof(mtk_init_cfg));
    if(gps_nvram_valid == 1)
    {
        init_cfg.if_type = stGPSReadback.gps_if_type; //MTK_IF_SW_EMULATION; //MTK_IF_UART_NO_HW_FLOW_CTRL;
    }
    else
    {
        init_cfg.if_type = MTK_IF_UART_NO_HW_FLOW_CTRL; //MTK_IF_SW_EMULATION; //MTK_IF_UART_NO_HW_FLOW_CTRL;
    }
    init_cfg.pps_mode = MTK_PPS_DISABLE;        // PPS disabled
    init_cfg.pps_duty = 100;                    // pps_duty (100ms high)
    init_cfg.if_init_spd = mnl_config.init_speed;               // 38400bps
	init_cfg.if_link_spd = mnl_config.link_speed;              // 115200bps
	//init_cfg.if_link_spd = 921600;              // 921600bps
    if(gps_nvram_valid == 1)
    {
        init_cfg.hw_TCXO_Hz = stGPSReadback.gps_tcxo_hz;            // 16.368MHz TCXO
	    init_cfg.hw_TCXO_ppb = stGPSReadback.gps_tcxo_ppb;                 // 0.5ppm TCXO
    }
    else
    {
        init_cfg.hw_TCXO_Hz = 16368000;            // 16.368MHz TCXO
	    init_cfg.hw_TCXO_ppb = 500;                 // 0.5ppm TCXO
    }
	init_cfg.fix_interval = 1000;               // 1Hz update rate
    if(gps_nvram_valid == 1)
    {
	    init_cfg.internal_LNA = stGPSReadback.gps_lna_mode;					// O -> Mixer in , 1 -> Internal LNA
    }
    else
    {
	    init_cfg.internal_LNA = 0;					// O -> Mixer in , 1 -> Internal LNA
    }
	init_cfg.Lat = 90.0;                        // initial latitude
	init_cfg.Lon = 0.0;                         // initial longitude
	init_cfg.datum = MTK_DATUM_WGS84;           // datum
    if(gps_nvram_valid == 1)
    {
    	init_cfg.dgps_mode = stGPSReadback.gps_sbas_mode;    // disable SBAS
    }
    else
    {
    	init_cfg.dgps_mode = MTK_DGPS_MODE_NONE;    // disable SBAS
    }
	init_cfg.agps_mode = MTK_AGPS_MODE_AUTO;    // enable AGPS
	init_cfg.opmode = MTK_INITCFG_OPMODE_2D_FIRSTFIX; // 2D first fix
	init_cfg.elev_mask = 5;                     // SV elevation mask
	init_cfg.sbas_prn = 0;                      // automatic selection of SBAS satellites
	init_cfg.sbas_test_mode = MTK_GPS_FALSE;    // SBAS integrity mode	
    if(gps_nvram_valid == 1)
    {
    	init_cfg.u1ClockType = stGPSReadback.gps_tcxo_type; //clk_type;
    }
    else
    {
    	init_cfg.u1ClockType = 0; //clk_type;
    }
	init_cfg.fgFrameAiding = 0;
#ifdef DSP_IMG//mike
    {
        int hDSPBin = -1;
        int nRead;
        
        hDSPBin = open(DSP_FILE, O_RDONLY);
        if (hDSPBin == -1)
        {
	        MNL_MSG("No dsp.bin \n");               
            init_cfg.reservedx = 0;
            init_cfg.reservedy = NULL;
        } else {
            dsp_img = malloc(DSP_BIN_MAX_SIZE);
            if(dsp_img)
            {
                nRead = read(hDSPBin, dsp_img, DSP_BIN_MAX_SIZE);
                if (nRead > 0)
                {
                    init_cfg.reservedx = nRead;
                    init_cfg.reservedy = (void*)dsp_img;
                }
                else
                {
                    init_cfg.reservedx = 0;
                    init_cfg.reservedy = NULL;
                }
                close(hDSPBin);
	            MNL_MSG("Use %s, size = %d \n", DSP_FILE, nRead);
            }
            else {
                init_cfg.reservedx = 0;
                init_cfg.reservedy = NULL;
                MNL_ERR("malloc failed for[%s]\n", DSP_FILE);
            } 
        }
    }
#else
    init_cfg.reservedx = 0;
    init_cfg.reservedy = NULL;
#endif

    g_ThreadExitDSPIn        = 0;
    g_ThreadExitAgent        = 0;
    g_ThreadExitAgpsDispatch = 0;
    
 	if (pthread_create(&mnl_thread[MNL_THREAD_DSP_INPUT].thread_handle, 
                       NULL, thread_dsp_input_func, 
                       (void*)&mnl_thread[MNL_THREAD_DSP_INPUT])) {
	   MNL_MSG("error creating dsp thread \n");
	   return MTK_GPS_ERROR;
	}
    
#if defined(SUPPORT_HOTSTILL)
    MNL_MSG("bee_enable = %d\n", mnl_config.BEE_enabled);

 	if (pthread_create(&mnl_thread[MNL_THREAD_BEE].thread_handle, 
                       NULL, thread_bee_func, 
                       (void*)&mnl_thread[MNL_THREAD_BEE])) 
	{
	   MNL_MSG("error creating bee thread \n");
	   return MTK_GPS_ERROR;
	}
#endif

	MNL_MSG("mtk_gps_init start\n");
	status = mtk_gps_init(MTK_GPS_START_FULL, &init_cfg);
	MNL_MSG("mtk_gps_init end, status=%d\n",status);

    if(dsp_img)
    {
        free(dsp_img);
        dsp_img = NULL;
    }

 	if (pthread_create(&mnl_thread[MNL_THREAD_MNL].thread_handle, 
                       NULL, thread_mnl_func, 
                       (void*)&mnl_thread[MNL_THREAD_MNL])) 
	{
	   MNL_MSG("error creating mnl thread \n");
	   return MTK_GPS_ERROR;
	}
    
    /*for PMTK_CONNECTION_SERIAL, the thread is activated during startup*/
    /*for PMTK_CONNECTION_SOCKET, the thread is created after accept connection*/
    if ((mnl_config.pmtk_conn == PMTK_CONNECTION_SERIAL) && (dbg_fd != -1)) {
     	if (pthread_create(&mnl_thread[MNL_THREAD_PMTK_INPUT].thread_handle, 
                           NULL, thread_pmtk_input_func,
                           (void*)&mnl_thread[MNL_THREAD_PMTK_INPUT])) 
    	{
    	   MNL_MSG("error creating mnl thread \n");
    	   return MTK_GPS_ERROR;
    	}
    }
    
	if (pthread_create(&mnl_thread[MNL_THREAD_AGENT].thread_handle, 
                       NULL, thread_agent_func, 
                       (void*)&mnl_thread[MNL_THREAD_AGENT])) 
	{
	   MNL_MSG("error creating agent thread \n");
	   return MTK_GPS_ERROR;
	}

	if (pthread_create(&mnl_thread[MNL_THREAD_AGPSDISPATCH].thread_handle, 
                       NULL, thread_agpsdispatch_func, 
                       (void*)&mnl_thread[MNL_THREAD_AGPSDISPATCH])) 
	{
	   MNL_MSG("error creating dispatch thread for agps \n");
	   return MTK_GPS_ERROR;
	}

    /* update user profile config*/
    userprofile.EPO_enabled = mnl_config.EPO_enabled;
    userprofile.BEE_enabled = mnl_config.BEE_enabled;    
    userprofile.SUPL_enabled = mnl_config.SUPL_enabled;
    userprofile.SUPLSI_enabled = mnl_config.SUPLSI_enabled;
    userprofile.EPO_priority = mnl_config.EPO_priority;
    userprofile.BEE_priority = mnl_config.BEE_priority;
    userprofile.SUPL_priority = mnl_config.SUPLSI_enabled;
    mtk_sys_agps_set_profile(&userprofile);

	return  status;
}
/*****************************************************************************/
int
linux_gps_uninit (void)
{
    int idx ,err;
    MNL_TRC();

    for (idx = 1; idx < MNL_THREAD_NUM; idx++) {
        if (mnl_thread[idx].thread_handle == C_INVALID_TID)
            continue;  
        
        if (!mnl_thread[idx].thread_exit)
            continue;  

        if ((err = mnl_thread[idx].thread_exit(&mnl_thread[idx]))) {
            MNL_ERR("fails to thread_exit thread %d], err = %d\n", idx, err);
            return MTK_GPS_ERROR;
        }        
    }
    return MTK_GPS_SUCCESS;
}
/******************************************************************************
*   MNL Porting For Android Platform
******************************************************************************/
void linux_gps_load_property()
{
    enable_dbg_log = 0;
    nmea_debug_level = 0;
    if(!mnl_utl_load_property(&mnl_config))
    {
        enable_dbg_log = mnl_config.debug_log; //1 
        nmea_debug_level = mnl_config.debug_mnl;
    }
}

/*****************************************************************************/
int exit_thread_normal(MNL_THREAD_T *arg)
{   /* exit thread by pthread_kill -> pthread_join*/
    int err;
    if (!arg)
        return MTK_GPS_ERROR;
    
    if(arg->thread_id == MNL_THREAD_DSP_INPUT)
    {
        int ld = N_TTY;
        g_ThreadExitDSPIn = 1;
	    if (ioctl(dsp_fd, TIOCSETD, &ld) < 0) {
		    MNL_ERR("TIOCSETD failed\n");
	    }
    } 
    else if(arg->thread_id == MNL_THREAD_AGENT) {
        mtk_gps_msg *pDummy_gps_msg = NULL;
        MNL_MSG("agent thread return trigger\n");
        
        g_ThreadExitAgent = 1;        
        pDummy_gps_msg = mtk_sys_msg_alloc(sizeof(mtk_gps_msg));/*not need free*/
        if(MTK_GPS_ERROR == mtk_sys_agps_msg_send(pDummy_gps_msg))
        {
        	   mtk_sys_msg_free(pDummy_gps_msg);       	
        }       
    } 
    else if(arg->thread_id == MNL_THREAD_AGPSDISPATCH) {
        int sock2diapatch = -1;
        struct sockaddr_un local;
        mtk_agps_msg *pDummy_agps_msg = (mtk_agps_msg *)malloc(sizeof(mtk_agps_msg));
        MNL_MSG("agps dispatch thread return trigger\n");
        
        g_ThreadExitAgpsDispatch = 1;      
        if((sock2diapatch = socket(AF_LOCAL, SOCK_DGRAM, 0)) == -1)
        {
            MNL_ERR("exit_thread_normal: open sock2supl fails\r\n");
            free(pDummy_agps_msg); 
            pDummy_agps_msg = NULL; 
            goto EXIT;
        }

        memset(&local, 0, sizeof(local));
        local.sun_family = AF_LOCAL;
        strcpy(local.sun_path, MTK_PROFILE2MNL);

        if (sendto(sock2diapatch, pDummy_agps_msg, sizeof(mtk_agps_msg), 0, (struct sockaddr*)&local, sizeof(local)) < 0)
        {
            MNL_ERR("send msg to dispatch fail:%s\r\n", strerror(errno));
        }
        close(sock2diapatch);
        if(pDummy_agps_msg){
            free(pDummy_agps_msg); 
            pDummy_agps_msg = NULL;
        }
    }
    

 EXIT:   
    if ((err = pthread_kill(arg->thread_handle, SIGUSR1)))
    {
        MNL_ERR("pthread_kill failed idx:%d, err:%d\n", arg->thread_id, err);
        return err;
    }  
    if ((err = pthread_join(arg->thread_handle, NULL)))
    {
        MNL_ERR("pthread_join failed idx:%d, err:%d\n", arg->thread_id, err);
        return err;   
    }
    return 0;    
}
/*****************************************************************************/
int exit_thread_extra(MNL_THREAD_T *arg)
{   /* exit thread by 
      (1) sending TERMINATE command
      (2) pthread_kill -> pthread_join
      NOTE: the method only works for MNL thread
    */
    int err;

    MNL_MSG();
    
    if (!arg)
        return MTK_GPS_ERROR;
    
    if ((err = mtk_gps_set_param(MTK_PARAM_CMD_TERMINATE, NULL, MTK_MOD_NIL, MTK_MOD_NIL)))
        return err;
        
    if ((err = pthread_kill(arg->thread_handle, SIGUSR1))) 
        return err;
        
    if ((err = pthread_join(arg->thread_handle, NULL)))
        return err;
        
    return 0;    
}
/*****************************************************************************/
static int sig_send_cmd(int fd, char* cmd, int len)
{
    if (fd == C_INVALID_FD) 
    {
        return 0;
    } 
    else 
    {    
        int  ret;
        MNL_VER("sig_send_cmd (%d, 0x%x)\n", fd, (int)(*cmd));
        do { 
            ret = write( fd, cmd, len ); 
        }while (ret < 0 && errno == EINTR);
        
        if (ret == len)
            return 0;
        else 
        {
            MNL_ERR("sig_send_cmd fails: %d (%s)\n", errno, strerror(errno));
            return -1;
        }
    }    

}
/*****************************************************************************/
void mtk_sys_ttff_handler(int type) 
{   /*the TTFF handler is called from PMTK handler*/
    char *msg = NULL;
    char buf[] = {MNL_CMD_RCV_TTFF};
    int err;
    
    if (type == MTK_GPS_START_HOT)
        msg = "HOT ";
    else if (type == MTK_GPS_START_WARM)
        msg = "WARM";
    else if (type == MTK_GPS_START_COLD)
        msg = "COLD";
    else if (type == MTK_GPS_START_FULL)
        msg = "FULL";
    else 
        MNL_ERR("invalid TTFF type: %d\n", type);

    MNL_MSG("receive %s TTFF\n", msg);        
    if ((err = sig_send_cmd(mnl_sinproc.dae_snd_fd, buf, sizeof(buf))))
        MNL_MSG("send command 0x%X fails\n", (unsigned int)buf[0]);
        
}
/*****************************************************************************/
int thread_active_notify(MNL_THREAD_T *arg)
{
    MNL_TRC();
    if (!arg) 
    {
        MNL_MSG("fatal error: null pointer!!\n");
        return -1;
    }
    if (arg->snd_fd != C_INVALID_FD) 
    {
        char buf[] = {MNL_CMD_ACTIVE};
        return sig_send_cmd(arg->snd_fd, buf, sizeof(buf));
    }
    return 0;
}
/*****************************************************************************/
static void* thread_debug_func( void*  arg ) 
{
    int server_fd = C_INVALID_SOCKET, conn_fd = C_INVALID_SOCKET, on;
    struct sockaddr_in server_addr;
    struct sockaddr_in client_addr;
    socklen_t size;
    char buf[128];

    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == -1) 
    {
        MNL_ERR("socket error = %d (%s)\n", errno, strerror(errno));
        pthread_exit(NULL);
        return NULL;
    }

    /* Enable address reuse */
    on = 1;
    if (setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on))) 
    {
        close(server_fd);
        MNL_ERR("setsockopt error = %d (%s) \n", errno, strerror(errno));
        pthread_exit(NULL);
        return NULL;
    }

    server_addr.sin_family = AF_INET;   /*host byte order*/
    server_addr.sin_port = htons(mnl_config.socket_port); /*short, network byte order*/
    server_addr.sin_addr.s_addr = INADDR_ANY; /*automatically fill with my IP*/
    memset(server_addr.sin_zero, 0x00, sizeof(server_addr.sin_zero));

    if (bind(server_fd, (struct sockaddr*)&server_addr, sizeof(server_addr)) == -1) 
    {
        close(server_fd);
        MNL_ERR("bind error = %d (%s) \n", errno, strerror(errno));
        pthread_exit(NULL);
        return NULL;
    }

    if (listen(server_fd, PMTK_SERVER_BACKLOG) == -1) 
    {
        close(server_fd);
        MNL_ERR("listen error = %d (%s) \n", errno, strerror(errno));
        pthread_exit(NULL);
        return NULL;
    }

    MNL_MSG("listening debug port: %d\n", mnl_config.socket_port);

    while (1) 
    {
        size = sizeof(client_addr);
        conn_fd = accept(server_fd, (struct sockaddr*)&client_addr, &size);
        if (conn_fd <= 0) 
        {
            MNL_ERR("accept error: %d (%s)\n", errno, strerror(errno));
            continue;
        } 
        else 
        {
            MNL_MSG("accept connection [%d] %s:%d\n", conn_fd, 
                inet_ntoa(client_addr.sin_addr), ntohs(client_addr.sin_port));             
            dbg_fd = conn_fd;
            /*loop until being interrupted or client close*/
            thread_pmtk_input_func(NULL);   
            close(conn_fd);
            dbg_fd = C_INVALID_FD;
            conn_fd = C_INVALID_FD;
        }        
    }    
    pthread_exit(NULL);
    return NULL;
}
#define ENABLE_GPS_BIND_CCCI

#ifdef ENABLE_GPS_BIND_CCCI
#define  CCCI_FS_IOCTL_SET_GPS    0xfd
#define  CCCI_CPE_MAJOR           156 
#endif
/*****************************************************************************/
int linux_gps_dev_init(void)
{
	struct termios termOptions;
#ifdef ENABLE_GPS_BIND_CCCI
    //for Bind with CCCI
	int fd = -1;
	fd = open("/dev/ccci_ss", O_RDWR);
    if(fd == -1)
    {
        mknod("/dev/ccci_ss", S_IFCHR|0777, (dev_t)(CCCI_CPE_MAJOR<<8|0));
        fd = open("/dev/ccci_ss", O_RDWR);
    }
    if(fd != -1)
    {
        int gps_flag = 1;
        ioctl(fd, CCCI_FS_IOCTL_SET_GPS, &gps_flag);
        LOGD("Set Flag for UART bind\n");
        close(fd);
    }
#endif
	/* infinity { */
	// Initialize GPS driver 
	gps_fd = open(mnl_config.dev_gps, O_RDWR | O_NOCTTY | O_NDELAY);
	if (gps_fd == -1) 
    {
		MNL_MSG("open_port: Unable to open - %s \n", mnl_config.dev_gps);
        /*the process should exit if fail to open GPS device*/
        return MTK_GPS_ERROR;   
	}
    else 
    {
		fcntl(gps_fd, F_SETFL, 0);

		// Get the current options:
		tcgetattr(gps_fd, &termOptions);

		// Set 8bit data, No parity, stop 1 bit (8N1):
		termOptions.c_cflag &= ~PARENB;
		termOptions.c_cflag &= ~CSTOPB;
		termOptions.c_cflag &= ~CSIZE;
		termOptions.c_cflag |= CS8 | CLOCAL | CREAD;

		MNL_MSG("c_lflag=%x,c_iflag=%x,c_oflag=%x\n",termOptions.c_lflag,termOptions.c_iflag,
								termOptions.c_oflag);
		//termOptions.c_lflag

		// Raw mode
		termOptions.c_iflag &= ~(INLCR | ICRNL | IXON | IXOFF | IXANY);
		termOptions.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);  /*raw input*/
		termOptions.c_oflag &= ~OPOST;  /*raw output*/

		tcflush(gps_fd,TCIFLUSH);//clear input buffer
		termOptions.c_cc[VTIME] = 100; /* inter-character timer unused */
		termOptions.c_cc[VMIN] = 0; /* blocking read until 0 character arrives */
		/*
		* Set the new options for the port...
		*/
		tcsetattr(gps_fd, TCSANOW, &termOptions);
	}
	/* infinity } */


	// Initialize UART port
	dsp_fd = open(mnl_config.dev_dsp, O_RDWR | O_NOCTTY);
	if (dsp_fd == -1) 
    {
		MNL_MSG("open_port: Unable to open - %s", mnl_config.dev_dsp);
        /*the process should exit if fail to open UART device*/
        return MTK_GPS_ERROR; 
	}
    else 
    {
		fcntl(dsp_fd, F_SETFL, 0);

		// Get the current options:
		tcgetattr(dsp_fd, &termOptions);

		// Set 8bit data, No parity, stop 1 bit (8N1):
		termOptions.c_cflag &= ~PARENB;
		termOptions.c_cflag &= ~CSTOPB;
		termOptions.c_cflag &= ~CSIZE;
		termOptions.c_cflag |= CS8 | CLOCAL | CREAD;

		MNL_MSG("c_lflag=%x,c_iflag=%x,c_oflag=%x\n",termOptions.c_lflag,termOptions.c_iflag,
								termOptions.c_oflag);
		//termOptions.c_lflag

		// Raw mode
		termOptions.c_iflag &= ~(INLCR | ICRNL | IXON | IXOFF | IXANY);
		termOptions.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);  /*raw input*/
		termOptions.c_oflag &= ~OPOST;  /*raw output*/

		tcflush(dsp_fd,TCIFLUSH);//clear input buffer
		termOptions.c_cc[VTIME] = 100; /* inter-character timer unused */
		termOptions.c_cc[VMIN] = 0; /* blocking read until 0 character arrives */
		/*
		* Set the new options for the port...
		*/
		tcsetattr(dsp_fd, TCSANOW, &termOptions);
	}

    // init debug log
    if (enable_dbg_log == MTK_GPS_TRUE) 
    {
	    time_t tm;
        struct tm *p;
        char msg[64] = {0};
        
        time(&tm);
        p = localtime(&tm);
        
        snprintf(msg, sizeof(msg), "%s.%04d%02d%02d%02d%02d%02d", LOG_FILE,
        1900 + p->tm_year, 1 + p->tm_mon, p->tm_mday,
        p->tm_hour, p->tm_min, p->tm_sec);
        
        dbglog_fp = fopen(msg, "w");
        if (dbglog_fp == NULL)
            MNL_MSG("%s create fail!!", LOG_FILE);
    }

    // Initialize Debug port
    if (mnl_config.pmtk_conn == PMTK_CONNECTION_SERIAL) 
    {
        dbg_fd = open(mnl_config.dev_dbg, O_RDWR | O_NOCTTY);
        if (dbg_fd == -1) 
        {
            MNL_MSG("open_port: Unable to open %s - ", mnl_config.dev_dbg);
        }
        else 
        {
    	    struct sigaction sig;
    	    
            /* install the signal handler before making the device asynchronous */
            sig.sa_handler  = signal_handler;
            //sig.sa_mask     = 0;
            sig.sa_flags    = 0;
            sig.sa_restorer = NULL;
            sigaction(SIGIO, &sig, NULL);

            fcntl(dbg_fd, F_SETOWN, getpid());
    		fcntl(dbg_fd, F_SETFL, FASYNC);

    		// Get the current options:
    		tcgetattr(dbg_fd, &termOptions);

    		// Set 8bit data, No parity, stop 1 bit (8N1):
    		termOptions.c_cflag &= ~PARENB;
    		termOptions.c_cflag &= ~CSTOPB;
    		termOptions.c_cflag &= ~CSIZE;
    		termOptions.c_cflag |= CS8 | CLOCAL | CREAD;

    		MNL_MSG("c_lflag=%x,c_iflag=%x,c_oflag=%x\n",termOptions.c_lflag,termOptions.c_iflag,
    								termOptions.c_oflag);
    		//termOptions.c_lflag

    		// Raw mode
    		termOptions.c_iflag &= ~(INLCR | ICRNL | IXON | IXOFF | IXANY);
    		termOptions.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);  /*raw input*/
    		termOptions.c_oflag &= ~OPOST;  /*raw output*/

    		tcflush(dbg_fd,TCIFLUSH);//clear input buffer
    		termOptions.c_cc[VTIME] = 100; /* inter-character timer unused */
    		termOptions.c_cc[VMIN] = 0; /* blocking read until 0 character arrives */

            cfsetispeed(&termOptions, B115200);
            cfsetospeed(&termOptions, B115200);
    		
    		/*
    		* Set the new options for the port...
    		*/
    		tcsetattr(dbg_fd, TCSANOW, &termOptions);
    	}
    }
    else if (mnl_config.pmtk_conn == PMTK_CONNECTION_SOCKET)
    {
        int err = 0;
        pthread_t thread_dbg;
        if ((err = pthread_create(&thread_dbg, NULL, thread_debug_func, NULL))) 
        {
            MNL_MSG("create command handler err = %d\n", errno);
            return err;
        }
    }
    return 0;
}
/*****************************************************************************/
int linux_gps_dev_uninit(void)
{
    if (dbg_fd != C_INVALID_FD)
        close(dbg_fd);
    if (dsp_fd != C_INVALID_FD)
    	close(dsp_fd);
    if (gps_fd != C_INVALID_FD)
    	close(gps_fd);
    // init debug log
    if(enable_dbg_log == MTK_GPS_TRUE)
    {
        if (dbglog_fp != NULL)
    	    fclose(dbglog_fp);
    }
    dbg_fd = C_INVALID_FD;
    dsp_fd = C_INVALID_FD;
    gps_fd = C_INVALID_FD;
    dbglog_fp = NULL;
    return 0;
}
/*****************************************************************************/
static int epoll_register( int  epoll_fd, int  fd )
{
    struct epoll_event  ev;
    int                 ret, flags;

    /* important: make the fd non-blocking */
    flags = fcntl(fd, F_GETFL);
    fcntl(fd, F_SETFL, flags | O_NONBLOCK);

    ev.events  = EPOLLIN;
    ev.data.fd = fd;
    do {
        ret = epoll_ctl( epoll_fd, EPOLL_CTL_ADD, fd, &ev );
    } while (ret < 0 && errno == EINTR);
    return ret;
}
/*****************************************************************************/
static int mnlcmd_handler(int fd) /*sent from mnld*/
{
    int ret;
    char cmd = MNL_CMD_UNKNOWN;
    do {
        ret = read(  fd, &cmd, sizeof(cmd) );
    } while (ret < 0 && errno == EINTR);
    if (ret == 0) 
    {
        MNL_MSG("mnlcmd_handler EOF"); /*it should not happen*/
        return 0;
    }
    else if (ret != sizeof(cmd)) 
    {
        MNL_MSG("mnlcmd_handler fails: %d %d(%s)\n", ret, errno, strerror(errno));
        return -1;
    }
    
    MNL_MSG("mnlcmd_handler(0x%X)\n", cmd);

    //LiChunhui, force Agent to idle mode
    if ((cmd == MNL_CMD_RESTART_HOT)||(cmd == MNL_CMD_RESTART_WARM)||
        (cmd == MNL_CMD_RESTART_COLD)||(cmd == MNL_CMD_RESTART_FULL)) {
        mtk_gps_msg *pMsg = NULL;         
        pMsg = mtk_sys_msg_alloc(sizeof(mtk_gps_msg));/*not need free*/
        pMsg->srcMod = MTK_MOD_END_LIST;
        pMsg->dstMod = MTK_MOD_END_LIST;
        pMsg->type = MTK_AGPS_MSG_END_LIST;
        pMsg->length = 0;

        mtk_sys_agps_msg_reset();
        
        MNL_MSG("GPS Restart, force IDLE mode(mnl trigger)\n");
        if(MTK_GPS_ERROR == mtk_sys_agps_msg_send(pMsg))
        {
        	   mtk_sys_msg_free(pMsg);  
        }    
    }
    
    if (cmd == MNL_CMD_SLEEP) 
    {        
        if ((ret = mtk_gps_set_param(MTK_PARAM_CMD_SLEEP, NULL, MTK_MOD_AGENT, MTK_MOD_MNL)))  
        {
            MNL_ERR("MNL sleep = %d\n", ret);
        } 
        else 
        {
            /*notify mnld that sleep command is successfully executed*/
            char buf[] = {MNL_CMD_SLEPT};        
            ret = sig_send_cmd(mnl_sinproc.dae_snd_fd, buf, sizeof(buf));
        }
        return ret;
    } 
    else if (cmd == MNL_CMD_WAKEUP) 
    {
        if ((ret = mtk_gps_set_param(MTK_PARAM_CMD_WAKEUP, NULL, MTK_MOD_AGENT, MTK_MOD_MNL))) 
            MNL_ERR("MNL wakeup = %d\n", ret);
        return ret;   
    } 
    else if (cmd == MNL_CMD_RESTART_HOT) 
    {
        mtk_param_restart restart = {MTK_GPS_START_HOT};
        if ((ret = mtk_gps_set_param (MTK_PARAM_CMD_RESTART, &restart, MTK_MOD_AGENT, MTK_MOD_MNL)))
            MNL_ERR("MNL hot start = %d\n", ret);
        return ret;
    } 
    else if (cmd == MNL_CMD_RESTART_WARM) 
    {
        mtk_param_restart restart = {MTK_GPS_START_WARM};
        if ((ret = mtk_gps_set_param (MTK_PARAM_CMD_RESTART, &restart, MTK_MOD_AGENT, MTK_MOD_MNL)))
            MNL_ERR("MNL warm start = %d\n", ret);
        return ret;
    }
    else if (cmd == MNL_CMD_RESTART_COLD) 
    {
        mtk_param_restart restart = {MTK_GPS_START_COLD};
        if ((ret = mtk_gps_set_param (MTK_PARAM_CMD_RESTART, &restart, MTK_MOD_AGENT, MTK_MOD_MNL)))
            MNL_ERR("MNL cold start = %d\n", ret);
        return ret;
    }
    else if (cmd == MNL_CMD_RESTART_FULL) 
    {
        mtk_param_restart restart = {MTK_GPS_START_FULL};
        if ((ret = mtk_gps_set_param (MTK_PARAM_CMD_RESTART, &restart, MTK_MOD_AGENT, MTK_MOD_MNL)))
            MNL_ERR("MNL full start = %d\n", ret);
        return ret;
    }
    else 
    {
        MNL_MSG("unknown command: 0x%2X\n", cmd);
        errno = -EINVAL;
        return errno;
    }
}
/*****************************************************************************/
#define ERR_FORCE_QUIT  0x0E01
/*****************************************************************************/
static int mnlctl_handler(int fd) /*sent from mnld*/
{
    int ret;
    char cmd = MNL_CMD_UNKNOWN;
    do {
        ret = read(  fd, &cmd, sizeof(cmd) );
    } while (ret < 0 && errno == EINTR);
    if (ret == 0) 
    {
        MNL_MSG("%s EOF", __FUNCTION__); /*it should not happen*/
        return 0;
    }
    else if (ret != sizeof(cmd)) 
    {
        MNL_MSG("%s fails: %d %d(%s)\n", __FUNCTION__, ret, errno, strerror(errno));
        return -1;
    }
    
    MNL_MSG("%s(0x%X)\n", __FUNCTION__, cmd);


    if (cmd == MNL_CMD_QUIT) 
    {
        return ERR_FORCE_QUIT;
    }
    else 
    {
        MNL_MSG("unknown command: 0x%2X\n", cmd);
        errno = -EINVAL;
        return errno;
    }
}
/*****************************************************************************/
int send_active_notify(int snd_fd) {
    if ((snd_fd != C_INVALID_FD) && !(mnl_config.debug_mnl & MNL_NMEA_DISABLE_NOTIFY)) 
    {
        char buf[] = {MNL_CMD_ACTIVE};
        return sig_send_cmd(snd_fd, buf, sizeof(buf));
    }
    return -1;
}
/*****************************************************************************/
void linux_signal_handler(int signo) 
{
    pthread_t self = pthread_self();
    if (signo == SIGTERM) 
    {
        char buf[] = {MNL_CMD_QUIT};  
        sig_send_cmd(mnl_sinproc.sig_snd_fd, buf, sizeof(buf));
        //return;
    }    
    MNL_MSG("Signal handler of %.8x -> %s\n", (unsigned int)self, sys_siglist[signo]);    
}
/*****************************************************************************/
static int linux_setup_signal_handler(void) 
{
    struct sigaction actions;   
    int err;
    int s[2];
    
    /*the signal handler is MUST, otherwise, the thread will not be killed*/
    memset(&actions, 0, sizeof(actions));
    sigemptyset(&actions.sa_mask);
    actions.sa_flags = 0;
    actions.sa_handler = linux_signal_handler;    
    if ((err = sigaction(SIGTERM, &actions, NULL))) 
    {
        MNL_MSG("register signal hanlder for SIGTERM: %s\n", strerror(errno));
        return -1;
    }

    if (socketpair(AF_UNIX, SOCK_STREAM, 0, s))
        return -1;
    
    fcntl(s[0], F_SETFD, FD_CLOEXEC);
    fcntl(s[0], F_SETFL, O_NONBLOCK);
    fcntl(s[1], F_SETFD, FD_CLOEXEC);
    fcntl(s[1], F_SETFL, O_NONBLOCK);
    
    mnl_sinproc.sig_snd_fd = s[0];
    mnl_sinproc.sig_rcv_fd = s[1];
    return 0;
} 
/*****************************************************************************/
static void* thread_cmd_func( void*  arg )
{
    int epoll_fd   = epoll_create(2);
    int epoll_cnt  = 0;
    int err;

    // register control file descriptors for polling
    if (mnl_sinproc.dae_rcv_fd != C_INVALID_FD) 
    {
        epoll_register( epoll_fd, mnl_sinproc.dae_rcv_fd);
        epoll_cnt++;
    }
    if (mnl_sinproc.sig_rcv_fd != C_INVALID_FD) 
    {
        epoll_register( epoll_fd, mnl_sinproc.sig_rcv_fd);
        epoll_cnt++;
    }
    if (!epoll_cnt) 
    {
        MNL_ERR("thread_cmd_func exit due to zero epoll count\n");
        goto exit;
    }            

    MNL_MSG("thread_cmd_func running: PPID[%d], PID[%d]\n", getppid(), getpid());

    // now loop
    for (;;) 
    {
        struct epoll_event   events[2];
        int                  ne, nevents;

        nevents = epoll_wait( epoll_fd, events, 2, -1 );
        if (nevents < 0) 
        {
            if (errno == EINTR)
            {                
                MNL_MSG("epoll_wait() is interrupted, try again!!\n");
            }
            else 
            {
                MNL_ERR("epoll_wait() return error: %s", strerror(errno));
                goto exit;
            }
        } 
        else 
        {
            //MNL_MSG("epoll_wait() received %d events", nevents);
        }
        
        for (ne = 0; ne < nevents; ne++) 
        {
            if ((events[ne].events & (EPOLLERR|EPOLLHUP)) != 0) 
            {
                MNL_ERR("EPOLLERR or EPOLLHUP after epoll_wait() !?");
                goto exit;
            }
            if ((events[ne].events & EPOLLIN) != 0) 
            {
                int  fd = events[ne].data.fd;
                if (fd == mnl_sinproc.dae_rcv_fd) 
                {
                    err = mnlcmd_handler(fd);
                    if (err)
                    {
                        MNL_ERR("mnlcmd_handler: %d\n", errno);
                    }
                }
                else if (fd == mnl_sinproc.sig_rcv_fd)
                {
                    err = mnlctl_handler(fd);
                    if (err == ERR_FORCE_QUIT) 
                    {
                        MNL_ERR("receives ERR_FORCE_QUIT\n");
                        goto exit;                        
                    }
                    else if (err) 
                    {
                        MNL_ERR("mnlctl_handler: %d\n", errno);
                    }
                }
            }
        } 
    }
exit:
    if (epoll_fd != C_INVALID_FD)
        close(epoll_fd);
    return NULL;
}
/*****************************************************************************/
int single_process() 
{
    int idx, ret,err;
    pthread_t thread_cmd;
    clock_t beg,end;
    MNL_MSG("Execute process: PPID = %.8d, PID = %.8d\n", getppid(), getpid());         

    int                 snd_fd;     
    MNL_THREAD_ID_T     thread_id;
    pthread_t           thread_handle;
    int (*thread_exit)(struct _MNL_THREAD_T *arg);
    int (*thread_active)(struct _MNL_THREAD_T *arg);   

    for (idx = 0; idx < MNL_THREAD_NUM; idx++) 
    {
        mnl_thread[idx].snd_fd = mnl_sinproc.dae_snd_fd;
        //mnl_thread[idx].thread_id = MNL_THREAD_UNKNOWN;
        mnl_thread[idx].thread_handle = C_INVALID_TID;
        mnl_thread[idx].thread_exit = NULL;
        mnl_thread[idx].thread_active = NULL;          
    }
       
    /*initialize system resource (message queue, mutex) used by library*/
    if ((err = mtk_sys_init())) 
    {
        MNL_MSG("mtk_sys_init err = %d\n",err);
        goto exit_proc;
    }
    
    MNL_MSG("main running\n");
    
    /*initialize UART/GPS device*/
    if ((err = linux_gps_dev_init())) 
    {
        MNL_MSG("linux_gps_dev_init err = %d\n", err);
        goto exit_proc;
    }

	//retry mechanism here
	if(gps_hw_check() != 0)
	{
	    MNL_MSG("gps chip power on failed\n");
	    goto exit_proc;
	}	
    
    /*initialize library thread*/
    if ((err = linux_gps_init())) 
    {
        MNL_MSG("linux_gps_init err = %d\n", err);
        goto exit_proc;
    }

    if ((err = linux_setup_signal_handler())) 
    {
        MNL_MSG("linux_gps_init err = %d\n", err);
        goto exit_proc;        
    }
        
    MNL_MSG("MNL running..\n");   

    thread_cmd_func(NULL);
    
exit_proc:        
    /*exiting*/
    MNL_MSG("MNL exiting \n");            
    
    /*finalize library*/
    if ((ret = linux_gps_uninit())) 
    {
        MNL_ERR("linux_gps_uninit err = %d\n", errno);
        err = (err) ? (err) : (ret);
    }
    
            
    if ((ret = mtk_gps_set_param(MTK_PARAM_CMD_RESET_DSP, NULL, MTK_MOD_AGENT, MTK_MOD_MNL))) 
    {
        MNL_ERR("mtk_gps_set_param err = %d\n", errno);
        err = (err) ? (err) : (ret);
    }

    beg = clock();
    usleep(mnl_config.delay_reset_dsp*1000); /*to wait until software reset*/
    end = clock();
    MNL_MSG("Reset delay: %.4f ms\n", (end-beg)*1000.0/CLOCKS_PER_SEC);
    
    if ((ret = linux_gps_dev_uninit())) 
    {
        MNL_ERR("mtk_sys_dev_unint err = %d\n", errno);        
        err = (err) ? (err) : (ret);
    }
    
    if ((ret = mtk_sys_uninit())) 
    {
        MNL_ERR("mtk_sys_uninit err = %d=\n", errno);
        err = (err) ? (err) : (ret);
    }
    if (mnl_sinproc.sig_rcv_fd != C_INVALID_FD)
        close(mnl_sinproc.sig_rcv_fd);
    if (mnl_sinproc.sig_snd_fd != C_INVALID_FD)
        close(mnl_sinproc.sig_snd_fd);
    if (mnl_sinproc.dae_rcv_fd != C_INVALID_FD)
        close(mnl_sinproc.dae_rcv_fd);
    if (mnl_sinproc.dae_snd_fd != C_INVALID_FD) 
        close(mnl_sinproc.dae_snd_fd);

    MNL_MSG("MNL exiting down\n");  
    return err;             
}
/*****************************************************************************/
int main (int argc, char** argv)
{
    pid_t pid = C_INVALID_PID;
    MNL_MSG("mnl_process running: argc(%d)\n", argc);
#if defined(READ_PROPERTY_FROM_FILE)
    linux_gps_load_property();
#endif 
    if ((argc == 3) || (argc == 4)) 
    {
        int fd0 = atoi(argv[1]);
        int fd1 = atoi(argv[2]);
        if ((fd0 > 0) && (fd1 > 0)) 
        {
            mnl_sinproc.dae_snd_fd = fd1;
            mnl_sinproc.dae_rcv_fd = fd0;
        }
        MNL_MSG("the pipe id is %d, %d\n", fd0, fd1);            
        if (argc == 4) 
            mnl_config.link_speed = atoi(argv[3]);
    }
    return single_process();       
}
