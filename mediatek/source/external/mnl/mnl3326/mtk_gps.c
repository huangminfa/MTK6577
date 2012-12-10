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

#define _MTK_GPS_C_
#include "mnl_linux.h"

#include "libnvram.h"
#include "CFG_GPS_File.h"
#include "CFG_GPS_Default.h"
#include "CFG_file_lid.h"
#include "Custom_NvRam_LID.h"

/******************************************************************************
* Macro & Definition
******************************************************************************/
//#define LIB_MQUEUE   //define LIB_MQUEUE use linux kernel mq
                     //not define LIB_MQUEUE will use mtk internal mq

/******************************************************************************
* Static variables
******************************************************************************/
// NV Storage pointer
static FILE *nvram_fp = NULL;
#define NV_FILE    "/data/misc/mtkgps.dat"
static char filebuf[BUFSIZ];
// mutex handler array
static pthread_mutex_t g_hMutex[MTK_MUTEX_END];
//static pthread_mutex_t g_hMutex1 = PTHREAD_MUTEX_INITIALIZER;

// BEE event
static pthread_cond_t g_bee_cond = PTHREAD_COND_INITIALIZER;
static pthread_mutex_t g_bee_mtx = PTHREAD_MUTEX_INITIALIZER;

#if defined(LIB_MQUEU)
// message queue file descriptor
static mqd_t mnl_mq_fd = -1;
struct mq_attr mnl_mq_attr;
static mqd_t mnl_agps_mq_fd = -1;
struct mq_attr mnl_agps_mq_attr;
#else
#define MNL_MSG_RING_SIZE 128
static pthread_mutex_t g_hMutexMsg = PTHREAD_MUTEX_INITIALIZER;
static pthread_cond_t g_gps_msg_cond = PTHREAD_COND_INITIALIZER;
static pthread_mutex_t g_gps_msg_mtx = PTHREAD_MUTEX_INITIALIZER;
static msg_ring_buf mnl_msg_ring_body;
static msg_ring_buf * mnl_msg_ring;
static mtk_gps_msg * mnl_msg_ring_buffer[MNL_MSG_RING_SIZE]; //pointer array
static mtk_int32 mnl_msg_cnt = 0;

#define MNL_AGPS_MSG_RING_SIZE 128
static pthread_mutex_t g_hMutexAgpsMsg = PTHREAD_MUTEX_INITIALIZER;
static pthread_cond_t g_agps_msg_cond = PTHREAD_COND_INITIALIZER;
static pthread_mutex_t g_agps_msg_mtx = PTHREAD_MUTEX_INITIALIZER;
static msg_ring_buf mnl_agps_msg_ring_body;
static msg_ring_buf * mnl_agps_msg_ring;
static mtk_gps_msg * mnl_agps_msg_ring_buffer[MNL_AGPS_MSG_RING_SIZE];
static mtk_int32 mnl_agps_msg_cnt = 0;
#endif

/******************************************************************************
* Extern Variables
******************************************************************************/
extern FILE *dbglog_fp;
extern mtk_bool enable_dbg_log;
extern MNL_CONFIG_T mnl_config;
extern ap_nvram_gps_config_struct stGPSReadback;
extern int gps_nvram_valid;
extern int deltat_read_clear(long *diff_sec);   // newT - oldT
void write_full (int hHandle, const char* buffer, mtk_uint32 size);


/*=============================================================================
* 
*   Utility functions for NMEA
*
=============================================================================*/
/*****************************************************************************
 * FUNCTION
 *  calc_nmea_checksum
 * DESCRIPTION
 *
 * PARAMETERS
 *  
 * RETURNS
 *
 *****************************************************************************/
unsigned char
calc_nmea_checksum (const char* sentence)
{
	unsigned char checksum = 0;

	while (*sentence)
	{
		checksum ^= (unsigned char)*sentence++;
	}

	return  checksum;
}
/*****************************************************************************
 * FUNCTION
 *  calc_dsp_checksum
 * DESCRIPTION
 *
 * PARAMETERS
 *  
 * RETURNS
 *
 *****************************************************************************/
unsigned char
calc_dsp_checksum (const char* sentence)
{
	unsigned char checksum = 0;

	while (*sentence)
	{
		checksum += (unsigned char)*sentence++;
	}

	return  checksum;
}
/*****************************************************************************
 * FUNCTION
 *  translate_nmea_deg_min_sec
 * DESCRIPTION
 *
 * PARAMETERS
 *  
 * RETURNS
 *
 *****************************************************************************/
double
translate_nmea_deg_min_sec (double deg)
{
	int  intpart = (int)deg;
	double fracpart = deg - (double)intpart;

	if (fracpart < .0)
	{
		fracpart = -fracpart;
	}
	if (intpart < 0)
	{
		intpart = -intpart;
	}
	fracpart *= 60.0;

	return  ((double)(intpart) * 100.0) + fracpart;
}
#if 0
/*****************************************************************************
 * FUNCTION
 *  Check_Mtk_Nmea_Pkt_Token
 * DESCRIPTION
 *  The input MTK NMEA Packet Format: $PMTKxxx,ddd,ddd,...*HH
 *  argument[0]: command ID
 *  argument[1]: argument 1
 *  argument[2]: argument 2  
 * PARAMETERS
 *  
 * RETURNS
 *
 *****************************************************************************/
mtk_bool Check_Mtk_Nmea_Pkt_Token(const char* command, mtk_r8 *argument)
{
	mtk_uint32 i, j;                 // loop counters
	mtk_uint32 string_length;        // length of input
	mtk_uint32 string_length_temp;   // temporary string length value
	char BufTemp[PMTK_MAX_FIELD_LENGTH+1];
	mtk_bool ret;
	const char *pCH;
	mtk_uint32 Idx;
	mtk_uint16 u2CmdType;


	ret = MTK_GPS_TRUE;

	// The number of elements of argument array is PMTK_MAX_FIELD_NUM + 1, 
	// and the element size is 8.
	memset(argument, 0, sizeof(mtk_r8) * (PMTK_MAX_FIELD_NUM + 1));


	// MTK NMEA packet starts with "$PMTK"

	if (strncmp(command, "$PMTK", 5) != 0)
	{
		return MTK_GPS_FALSE;
	}

	command += 5;


	// Check argument[0] packet type

	for (Idx = 0; Idx < 3; Idx++)
	{
		if ( *(command+Idx) >= '0' &&
			*(command+Idx) <= '9' )
		{
			BufTemp[Idx] = *(command+Idx);
		}
		else
		{
			return MTK_GPS_FALSE;
		}
	}
	BufTemp[Idx] = '\0';
	argument[0] = atof(BufTemp);
	u2CmdType = (mtk_uint16)argument[0];


	// If has arguments: PMTKxxx,xxx,xxx*HH
	// If has no argument: PMTKxxx*HH

	if ( (*(command+Idx) != ',') &&
		(*(command+Idx) != '*') )
	{
		return MTK_GPS_FALSE;
	}


	// If has arguments, process tha arguments

	if (*(command+Idx) == ',')
	{
		// Compute argument length = 
		// "length of entire input" - "length of '$PMTKxxx,'"  - "length of HH"

		command += (Idx+1);
		pCH = command;
		string_length_temp = 0;
		while ((*pCH != '*') && (*pCH != '\r') && (*pCH != '\n'))
		{
			pCH++;
			string_length_temp++;
		}
		string_length_temp++; // include the symbol '*' or '\r' or '\n'


		// Find comma separated arguments past the '$PMTKxxx'

		for ( j = 1 ; j <= PMTK_MAX_FIELD_NUM ; j++ )
		{
			string_length = 0;

			// For those commands that has arguments in HEX
			// Translate those HEX arguments in double floating point

			if ((u2CmdType == PMTK_DT_EPH) ||
				(u2CmdType == PMTK_DT_ALM) ||
				(u2CmdType == PMTK_SET_OUTPUT_DEBUG) ||
				(u2CmdType == PMTK_TEST_ALL) ||
				(u2CmdType == PMTK_CMD_WR_LLSW_REG) ||
				(u2CmdType == PMTK_Q_RD_LLSW_REG))
			{ // parameters should be hex
				for (i = 0; i < string_length_temp; i++)
				{
					if ( ( *(command+i) >= '0'  &&  *(command+i) <= '9' )  ||
						( *(command+i) >= 'A'  &&  *(command+i) <= 'F' ) ||
						( *(command+i) >= 'a'  &&  *(command+i) <= 'f' ) )
					{
						string_length++;
					}
					else if (( *(command+i) == ',' ) || ( *(command+i) == '*' ))
					{
						// following copy from AscToHex()
						mtk_int32 i;                           // Loop counter variable.
						mtk_uint8 digit;                       // single digit of the input ASCII string
						mtk_uint32 temp;                        // temporary work variable

						if ((string_length == 0) || (string_length > 8))
						{
							// Input packet is invalid
							return (MTK_GPS_FALSE);
						}

						temp =   0x00000000;            // Initialise to zero.

						i = 0;
						while ( i <= 8 )
						{
							digit = command[i];
							if ( digit >= '0'  &&  digit <= '9' )
							{
								temp = ( temp << 4 )  +  ( digit - '0' );
							}
							else if ( digit >= 'A'  &&  digit <= 'F' )
							{
								temp = ( temp << 4 )  +  ( digit - 'A' + 10 );
							}
							else if ( digit >= 'a'  &&  digit <= 'f' )
							{
								temp = ( temp << 4 )  +  ( digit - 'a' + 10 );
							}
							else if ( ( digit == ',' ) || ( digit == '*' ) )
							{
								argument[j] = (mtk_r8)temp;   // set output value
								if ( digit == '*' )
								{
									return (MTK_GPS_TRUE);
								}
								else
								{
									string_length++;
								}
								break;
							}
							else                         // non-hex char detected,
							{                            // output value left unchanged
								return (MTK_GPS_FALSE);
							}
							i++;
						};

						break;
					}
					else if ( *(command+i) == 0x0A || *(command+i) == 0x0D)
					{
						return (MTK_GPS_TRUE);
					}
					else
					{
						// Input packet is invalid
						return (MTK_GPS_FALSE);
					}
				}
			}
			else
			{
				for (i = 0; i < string_length_temp; i++)
				{
					if (i >= PMTK_MAX_FIELD_LENGTH)
					{
						ret = MTK_GPS_FALSE;
						j = PMTK_MAX_FIELD_NUM;               // terminate argument loop
					}
					else if ( ( *(command+i) >= '0'  &&  *(command+i) <= '9' )  ||
						( *(command+i) == '-' ) ||
						( *(command+i) == '.' ) )
					{
						string_length++;
						BufTemp[i] = *(command+i);
					}
					else if ( *(command+i) == ',' )
					{
						string_length++;
						// End of the current argument
						BufTemp[i] = 0;
						argument[j] = atof(BufTemp);
						break;
					}
					else if ( *(command+i) == '*' )
					{
						// End of the input
						BufTemp[i] = 0;
						argument[j] = atof(BufTemp);
						j = PMTK_MAX_FIELD_NUM;               // terminate argument loop
						break;
					}
					else if ( *(command+i) == 0x0A || *(command+i) == 0x0D)
					{
						j = PMTK_MAX_FIELD_NUM;               // terminate argument loop
					}
					else
					{
						// Input packet is invalid
						ret = MTK_GPS_FALSE;
						j = PMTK_MAX_FIELD_NUM;               // terminate argument loop
					}
				}
			}

			command = command + string_length;
			string_length_temp = string_length_temp - string_length;
		}
	}

	return ret;
}
/**
* Utc_to_Gps converts UTC time to GPS time
*
* @param iYr, iMo, iDay, iHr, iMin, dfSec: UTC time
* @param piWn: GPS week
* @param pdfTow: GPS time
*
* @return None
*/
static void
utc_to_gps (int iYr, int iMo, int iDay, int iHr, int iMin, double dfSec,
			short* piWn, double* pdfTow)
{
	int iYearsElapsed;     // Years since 1980.
	int iDaysElapsed;      // Days elapsed since Jan 5/Jan 6, 1980.
	int iLeapDays;         // Leap days since Jan 5/Jan 6, 1980.
	int i;
	// Number of days into the year at the start of each month (ignoring leap
	// years).
	const unsigned short doy[12] = {0,31,59,90,120,151,181,212,243,273,304,334};

	iYearsElapsed = iYr - 1980;


	i = 0;
	iLeapDays = 0;
	while (i <= iYearsElapsed)
	{
		if ((i % 100) == 20)
		{
			if ((i % 400) == 20)
			{
				iLeapDays++;
			}
		}
		else if ((i % 4) == 0)
		{
			iLeapDays++;
		}
		i++;
	}

	/*  iLeapDays = iYearsElapsed / 4 + 1; */
	if ((iYearsElapsed % 100) == 20)
	{
		if (((iYearsElapsed % 400) == 20) && (iMo <= 2))
		{
			iLeapDays--;
		}
	}
	else if (((iYearsElapsed % 4) == 0) && (iMo <= 2))
	{
		iLeapDays--;
	}
	iDaysElapsed = iYearsElapsed * 365 + doy[iMo - 1] + iDay + iLeapDays - 6;

	// Convert time to GPS weeks and seconds
	*piWn = iDaysElapsed / 7;
	*pdfTow = (double)(iDaysElapsed % 7) * 86400
		+ iHr * 3600 + iMin * 60 + dfSec;
}
/*****************************************************************************
 * FUNCTION
 *  Check_Mtk_Nmea_Pkt_Token
 * DESCRIPTION
 *  The input MTK NMEA Packet Format: $PMTKxxx,ddd,ddd,...*HH
 *  argument[0]: command ID
 *  argument[1]: argument 1
 *  argument[2]: argument 2  
 * PARAMETERS
 *  
 * RETURNS
 *
 *****************************************************************************/
mtk_int32 Mtk_Nmea_Process_Buff (mtk_r8 *argument)
{
	mtk_int32 i, j;
	mtk_int16 i2Cmd;
	union
	{
		mtk_param_restart restart;
		mtk_param_dgps_cfg param_dgps_config;
		mtk_param_nav_cfg nav;
	}  cfg;

	i2Cmd = (mtk_int16)argument[0];
	switch (i2Cmd)
	{
	case 0:  // This command is needed for PowerGPS to identify MTK GPS solution, otherwise NMEA sentence won't be shown
		mtk_sys_nmea_output("$PMTK001,0,3*30\x0d\x0a", strlen("$PMTK001,0,3*30\x0d\x0a")); 
		break;

	case 101:
		cfg.restart.restart_type = MTK_GPS_START_HOT;
		mtk_gps_set_param (MTK_PARAM_CMD_RESTART, &cfg.restart);  // Send parameter to invoke the restart    
        mtk_sys_ttff_handler(cfg.restart.restart_type);
		break;

	case 102:
		cfg.restart.restart_type = MTK_GPS_START_WARM;
		mtk_gps_set_param (MTK_PARAM_CMD_RESTART, &cfg.restart);
        mtk_sys_ttff_handler(cfg.restart.restart_type);
		break;

	case 103:
		cfg.restart.restart_type = MTK_GPS_START_COLD;
		mtk_gps_set_param (MTK_PARAM_CMD_RESTART, &cfg.restart);
        mtk_sys_ttff_handler(cfg.restart.restart_type);
		break;

	case 104:
		cfg.restart.restart_type = MTK_GPS_START_FULL;
		mtk_gps_set_param (MTK_PARAM_CMD_RESTART, &cfg.restart);
        mtk_sys_ttff_handler(cfg.restart.restart_type);
		break;

	case 220:  // Fix interval
		mtk_gps_get_param(MTK_PARAM_NAV_CONFIG, &cfg.nav);
		cfg.nav.fix_interval = (mtk_uint32)argument[1];
		mtk_gps_set_param(MTK_PARAM_NAV_CONFIG, &cfg.nav);
		break;

	case 250:
		mtk_sys_nmea_output("$PMTK001,250,3*37\x0d\x0a", strlen("$PMTK001,250,3*37\x0d\x0a"));  // Data port is not supported in MNL, just return OK for upper layer
		break;

	case 299:
		if (((mtk_uint32)argument[2] == 0x2454) && 
			((mtk_uint32)argument[3] == 0xABCD))
		{
			mtk_sys_nmea_output("$PMTK001,299,3*32\x0d\x0a", strlen("$PMTK001,299,3*32\x0d\x0a"));
		}
		else
		{
			mtk_sys_nmea_output("$PMTK001,299,2*33\x0d\x0a", strlen("$PMTK001,299,2*33\x0d\x0a"));
		}
		break;

	case 301:
		{
			mtk_gps_get_param(MTK_PARAM_DGPS_CONFIG, &cfg.param_dgps_config);

			if (((mtk_int32)argument[1] >= MTK_DGPS_MODE_NONE) && 
				((mtk_int32)argument[1] <= MTK_DGPS_MODE_AUTO))
			{
				cfg.param_dgps_config.dgps_mode = (mtk_gps_dgps_mode)(mtk_int32)argument[1];
				mtk_gps_set_param(MTK_PARAM_DGPS_CONFIG, &cfg.param_dgps_config);
				mtk_sys_nmea_output("$PMTK001,301,3*32\x0d\x0a", strlen("$PMTK001,301,3*32\x0d\x0a"));
			}
			else
			{
				mtk_sys_nmea_output("$PMTK001,301,2*33\x0d\x0a", strlen("$PMTK001,301,2*33\x0d\x0a"));
			}
		}
		break;

	case 313:
		mtk_gps_get_param(MTK_PARAM_DGPS_CONFIG, &cfg.param_dgps_config);

		if ((mtk_uint32)argument[1] == 1)
		{
			cfg.param_dgps_config.dgps_mode = MTK_DGPS_MODE_SBAS;
			mtk_gps_set_param(MTK_PARAM_DGPS_CONFIG, &cfg.param_dgps_config);
			mtk_sys_nmea_output("$PMTK001,313,3*31\x0d\x0a", strlen("$PMTK001,313,3*31\x0d\x0a"));
		}
		else if ((mtk_uint32)argument[1] == 0)
		{
			cfg.param_dgps_config.dgps_mode = MTK_DGPS_MODE_NONE;
			mtk_gps_set_param(MTK_PARAM_DGPS_CONFIG, &cfg.param_dgps_config);
			mtk_sys_nmea_output("$PMTK001,313,3*31\x0d\x0a", strlen("$PMTK001,313,3*31\x0d\x0a"));
		}
		else
		{
			mtk_sys_nmea_output("$PMTK001,313,2*30\x0d\x0a", strlen("$PMTK001,313,2*30\x0d\x0a"));
		}
		break;

	case 318:
		mtk_gps_get_param(MTK_PARAM_DGPS_CONFIG, &cfg.param_dgps_config);

		if (((mtk_uint32)argument[1] != 0) && 
			(((mtk_uint32)argument[1] < 120) || ((mtk_uint32)argument[1] >= 139)))
		{
			mtk_sys_nmea_output("$PMTK001,318,2*3B\x0d\x0a", strlen("$PMTK001,318,2*3B\x0d\x0a"));  // Not support range
		}
		else
		{
			cfg.param_dgps_config.sbas_prn = (mtk_uint32)argument[1];
			mtk_gps_set_param(MTK_PARAM_DGPS_CONFIG, &cfg.param_dgps_config);
			mtk_sys_nmea_output("$PMTK001,318,3*3A\x0d\x0a", strlen("$PMTK001,318,3*3A\x0d\x0a"));
		}
		break;

	case PMTK_API_SET_RTC_TIME:
		{
			short WeekNo;
			double TOW;

			utc_to_gps((int)argument[1], (int)argument[2], (int)argument[3],
				(int)argument[4], (int)argument[5], (int)argument[6],
				&WeekNo, &TOW);
			mtk_gps_set_time(WeekNo, TOW, 1.0e4);
		}
		break;

	case 395:
		// MNL doesn't support
		break;

#if defined(SUPPORT_DSP_RW)
	case PMTK_CMD_WR_LLSW_REG:  // Write DSP register, should not open to customer
		{
			extern mtk_int32 mtk_gps_set_reg(mtk_uint16 argument1, mtk_uint16 argument2);

			mtk_gps_set_reg((mtk_uint16)argument[1], (mtk_uint16)argument[2]);
			mtk_sys_nmea_output("$PMTK001,171,3*37\x0d\x0a", strlen("$PMTK001,171,3*37\x0d\x0a"));
		}
		break;

	case PMTK_Q_RD_LLSW_REG:  // Read DSP register, should not open to customer
		{
			extern mtk_int32 mtk_gps_get_reg(mtk_uint16 u2ReadAddr,char *pSendBuff);
			char pResult[64];

			mtk_gps_get_reg((mtk_uint16)argument[1], pResult);
			mtk_sys_nmea_output(pResult, strlen(pResult));
		}
		break;
#endif

	case 605:
		// To do, Get MNL version
		{
			char pVersion[64];

			memset(pVersion, 0, sizeof(pVersion));
			mtk_gps_get_param(MTK_PARAM_LIB_VERSION, pVersion);
			mtk_sys_nmea_output(pVersion, strlen(pVersion));
		}
		break;

	case PMTK_DT_EPH:
		{
			mtk_uint32 offset;
			char word[4];
			char eph_data[72];

			offset = 0;
			for (i = 0; i < 24; i++)
			{
				*((mtk_uint32*)word) = (mtk_uint32)argument[2+i];
				for (j = 0; j < 3; j++)
				{
					eph_data[offset++] = word[j];
				}
			}
			mtk_gps_set_ephemeris((mtk_uint8)argument[1], eph_data);
		}
		break;

	case PMTK_DT_ALM:
		{
			mtk_uint32 offset;
			char word[4];
			char alm_data[24];

			offset = 0;
			for (i = 0; i < 8; i++)
			{
				*((mtk_uint32*)word) = (mtk_uint32)argument[3+i];
				for (j = 0; j < 3; j++)
				{
					alm_data[offset++] = word[j];
				}
			}
			mtk_gps_set_almanac((mtk_uint8)argument[1], (mtk_uint16)argument[2], alm_data);
		}
		break;

	case PMTK_DT_TIME:
		mtk_gps_set_time((mtk_uint16)argument[1], (mtk_r8)argument[2], (mtk_r4)argument[3]);
		break;

	case PMTK_DT_LOC:
		mtk_gps_set_position(&argument[1]);
		break;

#if defined(SUPPORT_AGPS)
	case PMTK_DT_CLK:
		{
			MTK_AGPS_CMD_DATA_T rAdt;

			rAdt.u2Cmd = PMTK_DT_CLK;
			memset(&rAdt.uData.rAClk, 0, sizeof(MTK_ASSIST_CLK_T));
			rAdt.uData.rAClk.dfClkDrift = (mtk_r8) argument[1];    // GPS Clock Frequency Error [nsec/sec]
			rAdt.uData.rAClk.i4ClkRMSAcc = (mtk_int32) argument[2];   // Frequency Measurement RSM Accuracy [nsec/sec]
			rAdt.uData.rAClk.i4ClkAge = (mtk_int32) argument[3];      // Age (sec) of clock drift value since last estimated
			mtk_gps_set_agps_data(&rAdt);
		}
		break;

	case PMTK_DT_KLB:
		{
			MTK_AGPS_CMD_DATA_T rAdt;

			rAdt.u2Cmd = PMTK_DT_KLB;
			memset(&rAdt.uData.rAKlb, 0, sizeof(MTK_ASSIST_KLB_T));
			rAdt.uData.rAKlb.i1a0 = (mtk_int8) argument[1];       // Alpha0
			rAdt.uData.rAKlb.i1a1 = (mtk_int8) argument[2];       // Alpha1
			rAdt.uData.rAKlb.i1a2 = (mtk_int8) argument[3];       // Alpha2
			rAdt.uData.rAKlb.i1a3 = (mtk_int8) argument[4];       // Alpha3
			rAdt.uData.rAKlb.i1b0 = (mtk_int8) argument[5];       // Beta0
			rAdt.uData.rAKlb.i1b1 = (mtk_int8) argument[6];       // Beta1
			rAdt.uData.rAKlb.i1b2 = (mtk_int8) argument[7];       // Beta2
			rAdt.uData.rAKlb.i1b3 = (mtk_int8) argument[8];       // Beta3
			mtk_gps_set_agps_data(&rAdt);
		}
		break;

	case PMTK_DT_UCP:
		{
			MTK_AGPS_CMD_DATA_T rAdt;

			rAdt.u2Cmd = PMTK_DT_UCP;
			memset(&rAdt.uData.rAUcp, 0, sizeof(MTK_ASSIST_UCP_T));
			rAdt.uData.rAUcp.i4A1 = (mtk_int32) argument[1];      // UTC A1
			rAdt.uData.rAUcp.i4A0 = (mtk_int32) argument[2];      // UTC A0
			rAdt.uData.rAUcp.u1Tot = (mtk_uint8) argument[3];     // UTC Tot reference time of week
			rAdt.uData.rAUcp.u1WNt = (mtk_uint8) argument[4];     // UTC WNt reference week number
			rAdt.uData.rAUcp.i1dtLS = (mtk_int8) argument[5];    // UTC dtLS
			rAdt.uData.rAUcp.u1WNLSF = (mtk_uint8) argument[6];   // UTC WNLSF
			rAdt.uData.rAUcp.u1DN = (mtk_uint8) argument[7];      // UTC DN
			rAdt.uData.rAUcp.i1dtLSF = (mtk_int8) argument[8];   // UTC dtLSF
			mtk_gps_set_agps_data(&rAdt);
		}
		break;

	case PMTK_DT_BSV:
		{
			mtk_int32 i;
			MTK_AGPS_CMD_DATA_T rAdt;

			rAdt.u2Cmd = PMTK_DT_BSV;
			memset(&rAdt.uData.rABsv, 0, sizeof(MTK_ASSIST_BSV_T));
			rAdt.uData.rABsv.i1NumBad = (mtk_int8)argument[1];
			for (i = 0; i < rAdt.uData.rABsv.i1NumBad; i++)
			{
				rAdt.uData.rABsv.au1SvId[i] = (mtk_uint8)argument[2 + i];
			}
			mtk_gps_set_agps_data(&rAdt);
		}
		break;

	case PMTK_DT_ACQ:
		{
			MTK_AGPS_CMD_DATA_T rAdt;

			rAdt.u2Cmd = PMTK_DT_ACQ;
			memset(&rAdt.uData.rAAcq, 0, sizeof(MTK_ASSIST_ACQ_T));
			rAdt.uData.rAAcq.u1SV = (mtk_uint8)argument[1];
			rAdt.uData.rAAcq.i4GPSTOW = (mtk_int32)argument[2];
			rAdt.uData.rAAcq.i2Dopp = (mtk_int16)argument[3];
			rAdt.uData.rAAcq.i1DoppRate = (mtk_int8)argument[4];
			rAdt.uData.rAAcq.u1DoppSR = (mtk_uint8)argument[5];
			rAdt.uData.rAAcq.u2Code_Ph = (mtk_uint16)argument[6];
			rAdt.uData.rAAcq.i1Code_Ph_Int = (mtk_int8)argument[7];
			rAdt.uData.rAAcq.i1GPS_Bit_Num = (mtk_int8)argument[8];
			rAdt.uData.rAAcq.u1CodeSR = (mtk_uint8)argument[9];
			rAdt.uData.rAAcq.u1Azim = (mtk_uint8)argument[10];
			rAdt.uData.rAAcq.u1Elev = (mtk_uint8)argument[11];
			mtk_gps_set_agps_data(&rAdt);
		}
		break;

	case PMTK_DT_RTCM:
		{
			mtk_int32 i;
			MTK_AGPS_CMD_DATA_T rAdt;

			rAdt.u2Cmd = PMTK_DT_RTCM;
			memset(&rAdt.uData.rARtcm, 0, sizeof(MTK_ASSIST_DGP_T));
			rAdt.uData.rARtcm.u4Tow = (mtk_uint32)argument[1];
			rAdt.uData.rARtcm.u1Status = (mtk_uint8)argument[2];
			rAdt.uData.rARtcm.u1NumSv = (mtk_uint8)argument[3];
			for (i = 0; i < rAdt.uData.rARtcm.u1NumSv; i++)
			{
				rAdt.uData.rARtcm.arSVC[i].u1SatID = (mtk_uint8)argument[4 + (5*i)];
				rAdt.uData.rARtcm.arSVC[i].u1IODE = (mtk_uint8)argument[5 + (5*i)];
				rAdt.uData.rARtcm.arSVC[i].u1UDRE = (mtk_uint8)argument[6 + (5*i)];
				rAdt.uData.rARtcm.arSVC[i].i2PRC = (mtk_int16)argument[7 + (5*i)];
				rAdt.uData.rARtcm.arSVC[i].i1RRC = (mtk_int8)argument[8 + (5*i)];
			}
			mtk_gps_set_agps_data(&rAdt);
		}
		break;
#endif

#if defined(SUPPORT_MP_TEST)//Hiki, support MP Test
	case 810:
		{
			if(mtk_gps_enter_mp_test((mtk_uint16)argument[1], (mtk_uint8)argument[2]) == MTK_GPS_SUCCESS)
				mtk_sys_nmea_output("$PMTK001,810,3*39\x0d\x0a",19);
			else
				mtk_sys_nmea_output("$PMTK001,810,2*38\x0d\x0a",19);
		}
		break;

#endif


	default:
		return MTK_GPS_ERROR; // Command is not supported
	}

#if 0
	if((100 < i2Cmd) && (i2Cmd < 106))  // Restart Commands Hot to Factory Restarts
	{
		mtk_create_new_file();
	}
#endif

	return MTK_GPS_SUCCESS;
}
#endif
/*=============================================================================
* 
*   Porting Layer functions
*
=============================================================================*/
/*****************************************************************************
 * FUNCTION
 *  mtk_sys_init
 * DESCRIPTION
 *
 * PARAMETERS
 *  
 * RETURNS
 *
 *****************************************************************************/
int mtk_sys_init()
{
    int index;
	// Initialize Mutex array
	for (index = 0; index < MTK_MUTEX_END; index++)
		pthread_mutex_init(&g_hMutex[index], NULL);
    
    /*create message queue*/
#if defined(LIB_MQUEUE)
	mnl_mq_attr.mq_maxmsg = 40;
	mnl_mq_attr.mq_msgsize = sizeof(mnl_msg_struct);
	mnl_mq_attr.mq_flags   = 0;
	mnl_mq_fd = mq_open (MNL_MQ_NAME, O_CREAT|O_RDWR|O_EXCL, PMODE, &mnl_mq_attr);

	if (mnl_mq_fd == -1) {
		MNL_MSG("Fail to create mnl_msg_queue, errno=%s\n",strerror(errno));
		if (errno == EEXIST) {
			MNL_MSG("mnl_msg_queue already exists, unlink it now ...\n");
			mq_unlink(MNL_MQ_NAME);
		}
		return MTK_GPS_ERROR;
	} 

    mnl_agps_mq_attr.mq_maxmsg = 72;
	mnl_agps_mq_attr.mq_msgsize = sizeof(mnl_msg_struct);
	mnl_agps_mq_attr.mq_flags   = 0;
	mnl_agps_mq_fd = mq_open (MNL_AGPS_MQ_NAME, O_CREAT|O_RDWR|O_EXCL, PMODE, &mnl_agps_mq_attr);

	if (mnl_agps_mq_fd == -1) {
		MNL_MSG("Fail to create mnl_agps_msg_queue, errno=%s\n",strerror(errno));
		if (errno == EEXIST) {
			MNL_MSG("mnl_agps_msg_queue already exists, unlink it now ...\n");
			mq_unlink(MNL_AGPS_MQ_NAME);
		}
		return MTK_GPS_ERROR;
	} 
#else
    pthread_mutex_init(&g_hMutexMsg, NULL);
    mtk_sys_gps_event_create();
    mnl_msg_ring = &mnl_msg_ring_body;
    mnl_msg_ring->start_buffer = &mnl_msg_ring_buffer[0];
    mnl_msg_ring->end_buffer = &mnl_msg_ring_buffer[MNL_MSG_RING_SIZE-1];
    mnl_msg_ring->next_write = mnl_msg_ring->start_buffer;
    mnl_msg_ring->next_read = mnl_msg_ring->start_buffer;

    pthread_mutex_init(&g_hMutexAgpsMsg, NULL);
    mtk_sys_agps_event_create();
    mnl_agps_msg_ring = &mnl_agps_msg_ring_body;
    mnl_agps_msg_ring->start_buffer = &mnl_agps_msg_ring_buffer[0];
    mnl_agps_msg_ring->end_buffer = &mnl_agps_msg_ring_buffer[MNL_AGPS_MSG_RING_SIZE-1];
    mnl_agps_msg_ring->next_write = mnl_agps_msg_ring->start_buffer;
    mnl_agps_msg_ring->next_read = mnl_agps_msg_ring->start_buffer;
#endif     

    // Read NVRam
    {
        //int gps_nvram_fd = 0;
        F_ID gps_nvram_fd;
        int file_lid = AP_CFG_CUSTOM_FILE_GPS_LID;    
        int rec_size;
        int rec_num;
        int i;

        memset(&stGPSReadback, 0, sizeof(stGPSReadback));
        gps_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISREAD);
        if(gps_nvram_fd.iFileDesc != 0)
        {
            read(gps_nvram_fd.iFileDesc, &stGPSReadback , rec_size*rec_num);
            NVM_CloseFileDesc(gps_nvram_fd);

            if(strlen(stGPSReadback.dsp_dev) != 0)
            {
                gps_nvram_valid = 1;
                strncpy(mnl_config.dev_dsp, stGPSReadback.dsp_dev, sizeof(mnl_config.dev_dsp));

        		MNL_MSG("GPS NVRam (%d * %d) : \n", rec_size, rec_num);
        		MNL_MSG("dsp_dev : %s\n", stGPSReadback.dsp_dev);
        		MNL_MSG("gps_if_type : %d\n", stGPSReadback.gps_if_type);
        		MNL_MSG("gps_tcxo_hz : %d\n", stGPSReadback.gps_tcxo_hz);
        		MNL_MSG("gps_tcxo_ppb : %d\n", stGPSReadback.gps_tcxo_ppb);
        		MNL_MSG("gps_tcxo_type : %d\n", stGPSReadback.gps_tcxo_type);
        		MNL_MSG("gps_lna_mode : %d\n", stGPSReadback.gps_lna_mode);
        		MNL_MSG("gps_sbas_mode : %d\n", stGPSReadback.gps_sbas_mode);
            }
            else
            {
        		MNL_MSG("GPS NVRam mnl_config.dev_dsp == NULL \n");
            }
        }
        else
        {
            MNL_MSG("GPS NVRam gps_nvram_fd == NULL \n");
        }
    }

    return MTK_GPS_SUCCESS;
}
/*****************************************************************************
 * FUNCTION
 *  mtk_sys_init
 * DESCRIPTION
 *
 * PARAMETERS
 *  
 * RETURNS
 *
 *****************************************************************************/
int mtk_sys_uninit()
{
    int index;
    
    for (index = 0; index < MTK_MUTEX_END; index++)
        pthread_mutex_destroy(&g_hMutex[index]);
       
#if defined(LIB_MQUEUE)
	mq_close(mnl_mq_fd);         /* Close message queue in parent */
	mq_unlink(MNL_MQ_NAME);      /* Unlink message queue */
    mq_close(mnl_agps_mq_fd);    /* Close message queue in parent */
	mq_unlink(MNL_AGPS_MQ_NAME); /* Unlink message queue */
#else
    mtk_sys_gps_event_delete();
    pthread_mutex_destroy(&g_hMutexMsg);
    mtk_sys_agps_event_delete();
    pthread_mutex_destroy(&g_hMutexAgpsMsg);
#endif

    return MTK_GPS_SUCCESS;
}

#if 0
/*****************************************************************************
 * FUNCTION
 *  mtk_sys_nmea_input
 * DESCRIPTION
 *
 * PARAMETERS
 *  
 * RETURNS
 *
 *****************************************************************************/
mtk_int32 mtk_sys_nmea_input (const char* buffer, mtk_uint32 length)
{
    mtk_r8 argument[PMTK_MAX_FIELD_NUM + 1];   // arguments read off the command line
    //MNL_MSG("mtk_sys_nmea_input(%d) %s\n", length, buffer);

    // To do , handle PMTK command here, please input the whole NMEA command
    if (Check_Mtk_Nmea_Pkt_Token((const char*)buffer, (mtk_r8 *)argument) == MTK_GPS_FALSE)
    {
        //MNL_MSG("Check_Mtk_Nmea_Pkt_Token() == FALSE\n");
        return MTK_GPS_ERROR;  // Not a complete and valid sentence
    }
    //MNL_MSG("Check_Mtk_Nmea_Pkt_Token() == TRUE\n");

    Mtk_Nmea_Process_Buff(argument);

    return MTK_GPS_SUCCESS;
}
#endif

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_time_read
 * DESCRIPTION
 *
 * PARAMETERS
 *  utctime     [OUT]
 * RETURNS
 *  success (MTK_GPS_SUCCESS)
 *  failed (MTK_GPS_ERROR)
 *  system time changed since last call (MTK_GPS_ERROR_TIME_CHANGED)
 *****************************************************************************/
mtk_int32 mtk_sys_time_read (mtk_time* utctime)
{
	time_t t;
	struct tm *pGMTime;
    mtk_int32 deltaT;

	if (utctime == NULL)
	{
		return MTK_GPS_ERROR;
	}

	t = time(NULL);
	pGMTime = gmtime(&t);

	utctime->year = (mtk_uint16)pGMTime->tm_year; // tm_year counts from 1900
	utctime->month = (mtk_uint8)pGMTime->tm_mon; // range of tm_month is 0-11
	utctime->mday = (mtk_uint8)pGMTime->tm_mday;
	utctime->hour = (mtk_uint8)pGMTime->tm_hour;
	utctime->min = (mtk_uint8)pGMTime->tm_min;
	utctime->sec = (mtk_uint8)pGMTime->tm_sec;
	utctime->msec = 0; // no millisecond field in struct tm

    //MNL_MSG("mtk_sys_time_read(): %d/%d/%d/%d/%d/%d\n", (utctime->year)+1900, (utctime->month)+1, utctime->mday, utctime->hour, utctime->min, utctime->sec);

    if(deltat_read_clear((long *)&deltaT) == 0) // read sucessfully
    {
        if(deltaT != 0)
        {
            deltaT = 0 - deltaT;    // oldT - newT
            mtk_gps_time_change_notify(deltaT);            
            MNL_MSG("mtk_sys_time_read(): %d/%d/%d/%d/%d/%d\n", (utctime->year)+1900, (utctime->month)+1, utctime->mday, utctime->hour, utctime->min, utctime->sec);
            MNL_MSG("mtk_sys_time_read(): deltaT = %d\n", deltaT);
        }
    }
    
	return  MTK_GPS_SUCCESS;
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_time_write
 * DESCRIPTION
 *
 * PARAMETERS
 *  utctime     [IN]
 * RETURNS
 *  success (MTK_GPS_SUCCESS)
 *  failed (MTK_GPS_ERROR)
 *  system time changed since last call (MTK_GPS_ERROR_TIME_CHANGED)
 *****************************************************************************/
mtk_int32 mtk_sys_time_write (mtk_time utctime)
{
#if 0
	time_t t;
	struct tm *pGMTime;

	if (utctime == NULL)
	{
		return MTK_GPS_ERROR;
	}

	t = time(NULL);
	pGMTime = gmtime(&t);

	utctime->year = (mtk_uint16)pGMTime->tm_year; // tm_year counts from 1900
	utctime->month = (mtk_uint8)pGMTime->tm_mon; // range of tm_month is 0-11
	utctime->mday = (mtk_uint8)pGMTime->tm_mday;
	utctime->hour = (mtk_uint8)pGMTime->tm_hour;
	utctime->min = (mtk_uint8)pGMTime->tm_min;
	utctime->sec = (mtk_uint8)pGMTime->tm_sec;
	utctime->msec = 0; // no millisecond field in struct tm
	return  MTK_GPS_SUCCESS;
#else
	return  MTK_GPS_ERROR;
#endif
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_task_sleep
 * DESCRIPTION
 *
 * PARAMETERS
 *  milliseconds [IN]
 * RETURNS
 *  void
 *****************************************************************************/
void
mtk_sys_task_sleep (mtk_uint32 milliseconds)
{
    unsigned int sleep_in_usec;

    sleep_in_usec = milliseconds*1000;
    usleep(sleep_in_usec);
}


/*****************************************************************************
 * FUNCTION
 *  mtk_sys_storage_open
 * DESCRIPTION
 *
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_storage_open (void)
{
	nvram_fp = fopen(NV_FILE, "r+b");

	if (nvram_fp != NULL)
	{
		setbuf(nvram_fp, filebuf);
		MNL_MSG("\nmtkgps.dat opened successfully!!\n");
		return MTK_GPS_SUCCESS;
	}
	else
	{
		MNL_MSG("%s create new!!", NV_FILE);
		nvram_fp = fopen(NV_FILE, "w+b");
		if (nvram_fp != NULL)
			return MTK_GPS_SUCCESS;
	}
	MNL_MSG("\nmtkgps.dat open fail!!\n");
	return MTK_GPS_ERROR;
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_storage_close
 * DESCRIPTION
 *
 * RETURNS
 *  void
 *****************************************************************************/
void
mtk_sys_storage_close (void)
{
	if (nvram_fp != NULL)
	{
		fflush(nvram_fp);
		fclose(nvram_fp);
        nvram_fp = NULL;
		MNL_MSG("\nmtkgps.dat closed successfully!!\n");
	}
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_storage_delete
 * DESCRIPTION
 *
 * RETURNS
 *  void
 *****************************************************************************/
void
mtk_sys_storage_delete (void)
{
    MNL_MSG("\nmtk_sys_storage_delete()\n");
	if (nvram_fp != NULL)
	{
		mtk_sys_storage_close();
	}
    remove(NV_FILE);

    return;
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_storage_read
 * DESCRIPTION
 *
 *  blocking read until reaching 'length' or EOF
 * PARAMETERS
 *  buffer      [OUT]
 *  offset      [IN]
 *  length      [IN]
 *  p_nRead     [OUT]
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_storage_read (void* buffer, mtk_uint32 offset, mtk_uint32 length,
                      mtk_uint32* p_nRead)
{
	if (nvram_fp != 0)
	{
		fseek((FILE *)nvram_fp, offset, SEEK_SET);
		*p_nRead = (mtk_uint32)fread(buffer, 1, length, (FILE *)nvram_fp);
		if (*p_nRead == length)
		{
            if (nmea_debug_level & MNL_NMEA_DEBUG_STORAGE) 
            {
                int i;
                char msg[512] = {0};
                int len = 0;
                MNL_MSG("\n[MNL] Read %d bytes:", length);
            
                for (i = 0; i < (int)length; i++) {        
                    if (i % 16 == 0) {
                        len = 0;
                        snprintf(msg, sizeof(msg), "\n0x%.8x-0x%.8x: ", i, i + 15);
                    }
                    len += snprintf(msg+len, sizeof(msg)-len, "%.2x ", *((const unsigned char*)buffer + i));
                }            
                MNL_MSG("%s\n", msg);
            }
			return MTK_GPS_SUCCESS;
		}
		else
		{
		    MNL_MSG("\nread mtkgps.dat fail!!\n");
			return MTK_GPS_ERROR;
		}
	}
	else
	{
	    MNL_MSG("\nmtkgps.dat not existed!!\n");
		return MTK_GPS_ERROR;
	}
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_storage_write
 * DESCRIPTION
 *
 * PARAMETERS
 *  buffer      [IN]
 *  offset      [IN]
 *  length      [IN]
 *  p_nWritten  [OUT]
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_storage_write (const void* buffer, mtk_uint32 offset, mtk_uint32 length,
                       mtk_uint32* p_nWritten)
{
	if (nvram_fp != 0)
	{
		fseek((FILE *)nvram_fp, offset, SEEK_SET);
		*p_nWritten = (mtk_uint32)fwrite(buffer, 1, length, (FILE *)nvram_fp);
		if (*p_nWritten == length)
		{
            if (nmea_debug_level & MNL_NMEA_DEBUG_STORAGE)
            {
                int i;
                int len = 0;
                char msg[512];
                MNL_MSG("\n[MNL] Write %d bytes:", length);
            
                for (i = 0; i < (int)length; i++) {        
                    if (i % 16 == 0) {
                        len = 0;
                        snprintf(msg, sizeof(msg), "\n0x%.8x-0x%.8x: ", i, i + 15);
                    }
                    len += snprintf(msg+len, sizeof(msg)-len, "%.2x ", *((const unsigned char*)buffer + i));
                }
                MNL_MSG("%s\n", msg);
            }
			return MTK_GPS_SUCCESS;
		}
		else
		{
		    MNL_MSG("\nwrite mtkgps.dat fail!!\n");
			return MTK_GPS_ERROR;
		}
	}
	else
	{
	    MNL_MSG("\nmtkgps.dat not existed!!\n");
		return MTK_GPS_ERROR;
	}
}


/*****************************************************************************
 * FUNCTION
 *  mtk_sys_msg_recv
 * DESCRIPTION
 *
 * PARAMETERS
 *  msg         [OUT]
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_msg_recv (mtk_gps_msg** msg)
{
#if defined(LIB_MQUEUE)
	ssize_t num_bytes_received = -1;
	mnl_msg_struct msg_buffer;

    if(mnl_mq_fd == -1)
	{
		MNL_MSG("mtk_sys_msg_recv(): mnl_mq_fd == -1\n");
		exit(1);
	}
	
	while (1)
	{
		num_bytes_received = mq_receive(mnl_mq_fd,(char *)&msg_buffer,sizeof(mnl_msg_struct),0);
		if (num_bytes_received == -1)
		{
			usleep(50000);
			MNL_MSG("mnl wait msg\n");
			continue;  // Blocking the caller if no message
		}
		else
		{
			*msg = (mtk_gps_msg *) msg_buffer.msg_ptr;
			return	MTK_GPS_SUCCESS;
		}
	}
	return	MTK_GPS_SUCCESS;
#else
    while(mnl_msg_cnt <= 0)
    {
       //MNL_MSG("dbg_msg,MNL_RECV_COUNT,%d",mnl_msg_cnt);
       mtk_sys_gps_event_wait();
    }

    pthread_mutex_lock(&g_hMutexMsg);       

	(*msg) = *(mnl_msg_ring->next_read);

    if (nmea_debug_level & MNL_NMEA_DEBUG_MESSAGE) 
    {
        MNL_MSG("mtk_sys_msg_recv(%p), [%p %p], [%p %p], [%3d/%3d], [%d, %d]\n", *msg, 
                mnl_msg_ring->start_buffer, mnl_msg_ring->end_buffer,
                mnl_msg_ring->next_read, mnl_msg_ring->next_write, 
                mnl_msg_cnt-1, MNL_MSG_RING_SIZE,
                (*msg)->type, (*msg)->length);
    }     

	mnl_msg_ring->next_read++;

	// Wrap check output circular buffer
	if ( mnl_msg_ring->next_read > mnl_msg_ring->end_buffer )
	{
		mnl_msg_ring->next_read = mnl_msg_ring->start_buffer;
	}

    mnl_msg_cnt--;

    pthread_mutex_unlock(&g_hMutexMsg);

    return  MTK_GPS_SUCCESS;
#endif
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_msg_send
 * DESCRIPTION
 *
 * PARAMETERS
 *  msg         [IN]
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_msg_send (const mtk_gps_msg* msg)
{
#if defined(LIB_MQUEUE)
    mnl_msg_struct mnl_msg;
    if(mnl_mq_fd == -1)
    {
        MNL_ERR("mtk_sys_msg_send(): mnl_mq_fd == -1\n");
        exit(1);
    }

    if (msg)
    {
        mnl_msg.msg_type = 1;  
        mnl_msg.msg_ptr = (mtk_gps_msg*) msg;

        //MNL_MSG("Sending msg fd=%d!\n",mnl_mq_fd);
        if (mq_send(mnl_mq_fd,(const char*) &mnl_msg,sizeof(mnl_msg_struct),1) == -1)
        {
            MNL_MSG("Send msg fail, errno=%s!!\n",strerror(errno));
            return  MTK_GPS_ERROR;
        }
        else
        {
            //MNL_MSG("Send msg ok!!\n");
            return  MTK_GPS_SUCCESS;
        }
    }
    else
    {
        MNL_ERR("msg is invalid!\n");
    }

    return  MTK_GPS_ERROR;
    
#else	
    if (msg == NULL)
    {
        usleep(20000);
        MNL_ERR("mtk_sys_msg_send: no msg get\n");
        return MTK_GPS_ERROR;
    }

    pthread_mutex_lock(&g_hMutexMsg);
    if (nmea_debug_level & MNL_NMEA_DEBUG_MESSAGE) 
    {
        MNL_MSG("mtk_sys_msg_send(%p), [%p %p], [%p %p], [%3d/%3d], [%d, %d]\n", msg, 
                mnl_msg_ring->start_buffer, mnl_msg_ring->end_buffer,
                mnl_msg_ring->next_read, mnl_msg_ring->next_write, 
                mnl_msg_cnt+1, MNL_MSG_RING_SIZE,
                msg->type, msg->length);
    }

    /*buffer full*/
    if(mnl_msg_cnt == MNL_MSG_RING_SIZE)
    {
    	  pthread_mutex_unlock(&g_hMutexMsg);
	      MNL_MSG("mnl_msg_ring buffer will be full, %d\n", mnl_msg_cnt);
	      return MTK_GPS_ERROR;
    }
    /*end*/

	*(mnl_msg_ring->next_write) = (mtk_gps_msg*)msg;

    mnl_msg_ring->next_write++;

	// Wrap check the input circular buffer
	if ( mnl_msg_ring->next_write > mnl_msg_ring->end_buffer )
	{
		mnl_msg_ring->next_write = mnl_msg_ring->start_buffer;
	}

    mnl_msg_cnt++;

    pthread_mutex_unlock(&g_hMutexMsg);
    mtk_sys_gps_event_set();

    return MTK_GPS_SUCCESS;
#endif
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_msg_cnt
 * DESCRIPTION
 *
 * PARAMETERS
 * RETURNS
 *  message count of main GPS thread
 *****************************************************************************/
mtk_int32
mtk_sys_msg_cnt (void)
{
#if defined(LIB_MQUEUE)
	if(mnl_mq_fd == -1)
	{
		MNL_ERR("mtk_sys_msg_cnt(): mnl_mq_fd == -1\n");
        exit(1);
	}

	if (mnl_mq_fd != -1)
	{
        struct mq_attr curr_mq_attr;

		if (mq_getattr(mnl_mq_fd, &curr_mq_attr) == 0)
		{
            cnt = curr_mq_attr.mq_curmsgs;
			MNL_MSG("mq_getattr() ok, cnt = %d!!\n", cnt);
			return  cnt;
		}
		else
		{
			MNL_ERR("mq_getattr() fail, errno=%s!!\n", strerror(errno));
			return  MTK_GPS_ERROR;
		}
	}
	else
	{
		MNL_ERR("mnl_mq_fd is invalid, errno=%s!\n", strerror(errno));
	}

	return  MTK_GPS_ERROR;
#else
    return mnl_msg_cnt;
#endif
	
}

#if defined(LIB_MQUEU)
#else
/*****************************************************************************
 * FUNCTION
 *  mtk_sys_gps_event_create
 * DESCRIPTION
 *  Create an event object for gps msg
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_gps_event_create(void)
{
    // Initialize Mutex and condition for gps msg 
    if (pthread_mutex_init(&g_gps_msg_mtx, NULL))
    {
        return MTK_GPS_ERROR;
    }
    
    if (pthread_cond_init(&g_gps_msg_cond, NULL))
    {
        return MTK_GPS_ERROR;
    }
    
    return MTK_GPS_SUCCESS;
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_gps_event_delete
 * DESCRIPTION
 *  Delete the event object of gps msg
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_gps_event_delete(void)
{
    mtk_int32 ret = MTK_GPS_SUCCESS;
    
    if (pthread_cond_destroy(&g_gps_msg_cond))
    {
        ret = MTK_GPS_ERROR;
    }
    
    if (pthread_mutex_destroy(&g_gps_msg_mtx))
    {
        ret = MTK_GPS_ERROR;
    }
    
    return ret;    
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_gps_event_set
 * DESCRIPTION
 *  Set the event object of gps msg
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_gps_event_set(void)
{
    mtk_int32 ret = MTK_GPS_SUCCESS;
    
    if (pthread_mutex_lock(&g_gps_msg_mtx))
    {
         ret = MTK_GPS_ERROR;
    }    
    //MNL_TRC();    
    if (pthread_cond_signal(&g_gps_msg_cond))
    {
        ret = MTK_GPS_ERROR;
    }
    
    pthread_mutex_unlock(&g_gps_msg_mtx);
    return ret;
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_gps_event_wait
 * DESCRIPTION
 *  Wait for the event object of gps msg
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_gps_event_wait(void)
{
    mtk_int32 ret = MTK_GPS_SUCCESS;
        
    if (pthread_mutex_lock(&g_gps_msg_mtx))
    {
        ret = MTK_GPS_ERROR;
    }
    //MNL_TRC();
    if (pthread_cond_wait(&g_gps_msg_cond, &g_gps_msg_mtx))
    {
        ret = MTK_GPS_ERROR;
    }
        
    pthread_mutex_unlock(&g_gps_msg_mtx);
        
    return ret;
}


/////////////////////////////////////////////////////////////
/*****************************************************************************
 * FUNCTION
 *  mtk_sys_agps_event_create
 * DESCRIPTION
 *  Create an event object for Agent msg
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_agps_event_create(void)
{
    // Initialize Mutex and condition for Agent msg 
    if (pthread_mutex_init(&g_agps_msg_mtx, NULL))
    {
        return MTK_GPS_ERROR;
    }
    
    if (pthread_cond_init(&g_agps_msg_cond, NULL))
    {
        return MTK_GPS_ERROR;
    }
    
    return MTK_GPS_SUCCESS;
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_agps_event_delete
 * DESCRIPTION
 *  Delete the event object of Agent msg
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_agps_event_delete(void)
{
    mtk_int32 ret = MTK_GPS_SUCCESS;
    
    if (pthread_cond_destroy(&g_agps_msg_cond))
    {
        ret = MTK_GPS_ERROR;
    }
    
    if (pthread_mutex_destroy(&g_agps_msg_mtx))
    {
        ret = MTK_GPS_ERROR;
    }
    
    return ret;    
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_agps_event_set
 * DESCRIPTION
 *  Set the event object of Agent msg
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_agps_event_set(void)
{
    mtk_int32 ret = MTK_GPS_SUCCESS;
    
    if (pthread_mutex_lock(&g_agps_msg_mtx))
    {
         ret = MTK_GPS_ERROR;
    }
    //MNL_TRC();
    if (pthread_cond_signal(&g_agps_msg_cond))
    {
        ret = MTK_GPS_ERROR;
    }
    
    pthread_mutex_unlock(&g_agps_msg_mtx);
    return ret;
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_agps_event_wait
 * DESCRIPTION
 *  Wait for the event object of Agent msg
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_agps_event_wait(void)
{
    mtk_int32 ret = MTK_GPS_SUCCESS;    
        
    if (pthread_mutex_lock(&g_agps_msg_mtx))
    {
        ret = MTK_GPS_ERROR;
    }
    //MNL_TRC();
    if (pthread_cond_wait(&g_agps_msg_cond, &g_agps_msg_mtx))
    {
        ret = MTK_GPS_ERROR;
    }
        
    pthread_mutex_unlock(&g_agps_msg_mtx);
        
    return ret;
}
#endif

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_agps_msg_cnt
 * DESCRIPTION
 *
 * PARAMETERS
 * RETURNS
 *  message count of main GPS thread
 *****************************************************************************/
mtk_int32
mtk_sys_agps_msg_cnt (void)
{
#if defined(LIB_MQUEUE)
    if(mnl_agps_mq_fd == -1)
    {
        MNL_ERR("mtk_sys_agps_msg_cnt: mnl_agps_mq_fd == -1\n");
        exit(1);
    }
    
    if (mnl_agps_mq_fd != -1)
    {
        struct mq_attr curr_agps_mq_attr;
    
        if (mq_getattr(mnl_agps_mq_fd, &curr_agps_mq_attr) == 0)
        {
            cnt = curr_agps_mq_attr.mq_curmsgs;
            MNL_MSG("mq_getattr() ok, cnt = %d!!\n", cnt);
            return  cnt;
        }
        else
        {
            MNL_ERR("mq_getattr() fail, errno=%s!!\n", strerror(errno));
            return  -1;
        }
    }
    else
    {
        MNL_ERR("mnl_agps_mq_fd is invalid, errno=%s!\n", strerror(errno));
        return  -1;
    }
    
#else
    return  mnl_agps_msg_cnt;
#endif
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_agps_msg_recv
 * DESCRIPTION
 *
 * PARAMETERS
 *  msg         [OUT]
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_agps_msg_recv (mtk_gps_msg** msg)
{
#if defined(LIB_MQUEUE)
    ssize_t num_bytes_received = -1;
    mnl_msg_struct agps_msg_buffer;

    if(mnl_agps_mq_fd == -1)
    {
        MNL_MSG("mtk_sys_agps_msg_recv: mnl_mq_fd == -1\n");
        exit(1);
    }
        
    while (1)
    {
        num_bytes_received = mq_receive(mnl_agps_mq_fd,(char *)&agps_msg_buffer,sizeof(mnl_msg_struct),0);
        if (num_bytes_received == -1)
        {
            usleep(50000);
            MNL_MSG("mnl wait msg\n");
            continue;  // Blocking the caller if no message
        }
        else
        {
            *msg = (mtk_gps_msg *) agps_msg_buffer.msg_ptr;
            return  MTK_GPS_SUCCESS;
        }
    }
    return  MTK_GPS_SUCCESS;
#else
    while(mnl_agps_msg_cnt <= 0)
    {
       //MNL_MSG("dbg_msg,AGENT_RECV_COUNT,%d",mnl_agps_msg_cnt);
       mtk_sys_agps_event_wait();
    }
      
    pthread_mutex_lock(&g_hMutexAgpsMsg);

    (*msg) = *(mnl_agps_msg_ring->next_read);
    
    if (nmea_debug_level & MNL_NMEA_DEBUG_MESSAGE) 
    {
        MNL_MSG("mtk_sys_agps_msg_recv(%p), [%p %p], [%p %p], [%3d/%3d], [%d, %d]\n", *msg, 
                   mnl_agps_msg_ring->start_buffer, mnl_agps_msg_ring->end_buffer,
                    mnl_agps_msg_ring->next_read, mnl_agps_msg_ring->next_write, 
                    mnl_agps_msg_cnt-1, MNL_AGPS_MSG_RING_SIZE,
                    (*msg)->type, (*msg)->length);
    }     
    
    mnl_agps_msg_ring->next_read++;
    
    // Wrap check output circular buffer
    if ( mnl_agps_msg_ring->next_read > mnl_agps_msg_ring->end_buffer )
    {
        mnl_agps_msg_ring->next_read = mnl_agps_msg_ring->start_buffer;
    }
    
    mnl_agps_msg_cnt--;   
    pthread_mutex_unlock(&g_hMutexAgpsMsg);
    
    return  MTK_GPS_SUCCESS;
#endif
}

mtk_int32
mtk_sys_agps_msg_reset (void)
{
    pthread_mutex_lock(&g_hMutexAgpsMsg);

    //MNL_MSG("mtk_sys_agps_msg_send_IN,[%p %p], [%p %p], [%3d/%3d]\n", 
    //            mnl_agps_msg_ring->start_buffer, mnl_agps_msg_ring->end_buffer,
    //            mnl_agps_msg_ring->next_read, mnl_agps_msg_ring->next_write, 
    //            mnl_agps_msg_cnt+1, MNL_AGPS_MSG_RING_SIZE);


    while(mnl_agps_msg_ring->next_read != mnl_agps_msg_ring->next_write)
    {
        if((*(mnl_agps_msg_ring->next_read)) != NULL)    
        {
            mtk_sys_msg_free( *(mnl_agps_msg_ring->next_read) );
            *(mnl_agps_msg_ring->next_read) = NULL;
        }
        else
        {
            MNL_MSG("mtk_sys_agps_msg_reset,ERROR,NULL pointer");
        }
           
        mnl_agps_msg_ring->next_read++;
      
        // Wrap check the input circular buffer
        if (mnl_agps_msg_ring->next_read > mnl_agps_msg_ring->end_buffer )
        {
            mnl_agps_msg_ring->next_read = mnl_agps_msg_ring->start_buffer;
        }
       
    }

    //MNL_MSG("mtk_sys_agps_msg_send_OUT,[%p %p], [%p %p], [%3d/%3d]\n", 
    //            mnl_agps_msg_ring->start_buffer, mnl_agps_msg_ring->end_buffer,
    //            mnl_agps_msg_ring->next_read, mnl_agps_msg_ring->next_write, 
    //            mnl_agps_msg_cnt+1, MNL_AGPS_MSG_RING_SIZE);
    
    pthread_mutex_unlock(&g_hMutexAgpsMsg);

    return  MTK_GPS_SUCCESS;
}


/*****************************************************************************
 * FUNCTION
 *  mtk_sys_agps_msg_send
 * DESCRIPTION
 *
 * PARAMETERS
 *  msg         [IN]
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_agps_msg_send (const mtk_gps_msg* msg)
{
#if defined(LIB_MQUEUE)
    mnl_msg_struct mnl_agps_msg;
    if(mnl_agps_mq_fd == -1)
    {
        MNL_MSG("mtk_sys_agps_msg_send: mnl_agps_mq_fd == -1\n");
        exit(1);
    }
    
    if (msg)
    {
        mnl_agps_msg.msg_type = 1;  
        mnl_agps_msg.msg_ptr = (mtk_gps_msg*) msg;
    
        MNL_MSG("Sending msg fd=%d!\n",mnl_agps_mq_fd);
        if (mq_send(mnl_agps_mq_fd,(const char*)&mnl_agps_msg, sizeof(mnl_msg_struct),1) == -1)
        {
            MNL_ERR("Send msg fail, errno=%s!!\n",strerror(errno));
            return  MTK_GPS_ERROR;
        
        }
        else
        {
            MNL_MSG("Send msg ok!!\n");
            return  MTK_GPS_SUCCESS;
        }
    }
    else
    {
        MNL_ERR("mtk_sys_agps_msg_send:msg is invalid!\n");
    }
    
    return  MTK_GPS_ERROR;   
#else	
    if (msg == NULL)
    {
        usleep(20000);
        MNL_ERR("mtk_sys_agps_msg_send: msg invalid!\n");
        return MTK_GPS_ERROR;
    }
    
    pthread_mutex_lock(&g_hMutexAgpsMsg);
    if (nmea_debug_level & MNL_NMEA_DEBUG_MESSAGE) 
    {
        MNL_MSG("mtk_sys_agps_msg_send(%p), [%p %p], [%p %p], [%3d/%3d], [%d, %d]\n", msg, 
                    mnl_agps_msg_ring->start_buffer, mnl_agps_msg_ring->end_buffer,
                    mnl_agps_msg_ring->next_read, mnl_agps_msg_ring->next_write, 
                    mnl_agps_msg_cnt+1, MNL_AGPS_MSG_RING_SIZE,
                    msg->type, msg->length);
    }
    
    /*buffer full*/
    if(mnl_agps_msg_cnt == MNL_AGPS_MSG_RING_SIZE)
    {
        pthread_mutex_unlock(&g_hMutexAgpsMsg);
	    MNL_MSG("mtk_agps_msg_ring buffer is full, return Error");
	    return MTK_GPS_ERROR;
    }
    /*end*/

    *(mnl_agps_msg_ring->next_write) = (mtk_gps_msg*)msg;    
     mnl_agps_msg_ring->next_write++;
    
    // Wrap check the input circular buffer
    if (mnl_agps_msg_ring->next_write >= mnl_agps_msg_ring->end_buffer )
    {
        mnl_agps_msg_ring->next_write = mnl_agps_msg_ring->start_buffer;
    }
    
    mnl_agps_msg_cnt++;    
    pthread_mutex_unlock(&g_hMutexAgpsMsg);

    mtk_sys_agps_event_set();
    
    return MTK_GPS_SUCCESS;
#endif
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_msg_alloc
 * DESCRIPTION
 *
 * PARAMETERS
 *  size        [IN]   the length of the whole mtk_gps_msg structure
 * RETURNS
 *  pointer to the created message if succeeded
 *  NULL (0) if failed
 *****************************************************************************/
mtk_gps_msg*
mtk_sys_msg_alloc (mtk_uint16 size)
{
	return  malloc((size_t)size);
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_msg_free
 * DESCRIPTION
 *
 * PARAMETERS
 *  msg         [IN]
 * RETURNS
 *  void
 *****************************************************************************/
void
mtk_sys_msg_free (mtk_gps_msg* msg)
{
	if (msg)
	{
		free(msg);
                		
	}
	else
	{
	    MNL_MSG("mtk_sys_msg_free() is NULL!!\n");
	}
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_mem_alloc
 * DESCRIPTION
 *  Allocate a block of memory
 * PARAMETERS
 *  size        [IN]   the length of the whole memory to be allocated
 * RETURNS
 *  On success, return the pointer to the allocated memory
 *  NULL (0) if failed
 *****************************************************************************/
void*
mtk_sys_mem_alloc (mtk_uint32 size)
{
    return malloc(size);
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_mem_free
 * DESCRIPTION
 *  Release unused memory
 * PARAMETERS
 *  pmem         [IN]
 * RETURNS
 *  void
 *****************************************************************************/
void
mtk_sys_mem_free (void* pmem)
{
    if(pmem)
    {
        free(pmem);
    }
    else
   {
        MNL_MSG("mtk_sys_mem_free() is NULL!!\n");
   }

}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_data_output
 * DESCRIPTION
 *  Transmit data to the GPS chip
 *  (The function body needs to be implemented)
 * PARAMETERS
 *  msg         [IN]
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_data_output (char* buffer, mtk_uint32 length)
{
    if (nmea_debug_level & MNL_NMEA_DEBUG_TX_FULL)
    {
        int i;
        time_t tm;
        struct tm *p;
        struct timeb tp;
        int len = 0;
        char msg[512];
        
        time(&tm);
        ftime(&tp);
        p = localtime(&tm);

        MNL_MSG("\n[MNL_DSP] TX %d bytes: (time=%d/%d/%d %.2d:%.2d:%.2d.%d)\n", 
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

	//MNL_MSG("mtk_sys_data_output length=%d\n",length);
	if (dsp_fd != -1)
	{
		int dwWrite;
		mtk_uint32 offset = 0;
		mtk_uint8 fSuccess = MTK_GPS_TRUE;

		while ((offset < length) && fSuccess)
		{
			dwWrite = write(dsp_fd, &buffer[offset], length - offset);
			if (dwWrite < 0)
			{
				fSuccess = MTK_GPS_FALSE;
			}
			else
			{
				//MNL_MSG("dwWrite=%d\n",dwWrite);
				offset += dwWrite;
				if (0 == dwWrite)
				{
					sleep(0);
				}
			}
		}
        if (fSuccess) 
        {   /*after sending sleep command, force flush and sleep for a while*/
            #define SLEEP_DELAY 20
            static char cmd_sleep[] = {0x04,0x24,0x0b,0x00,0x05,0xff,0x01,0x00,0xf0,0x0d,0x0a};
            static int cmd_len = (int)(sizeof(cmd_sleep)/sizeof(cmd_sleep[0]));
            static int usec = SLEEP_DELAY*1000;
            if ((length == (mtk_uint32)cmd_len) && !memcmp(cmd_sleep, buffer, cmd_len)) {
                MNL_MSG("sleep command is detected, sleep %d ms", SLEEP_DELAY);
                tcflush(dsp_fd, TCIOFLUSH);
                usleep(usec);
            }            
        }
		return  fSuccess ? MTK_GPS_SUCCESS : MTK_GPS_ERROR;
	}
	else
	{
		return  MTK_GPS_ERROR;
	}
}

mtk_int32 mtk_sys_nmea_output (char* buffer, mtk_uint32 length)
{
    //write_full(gps_fd, buffer, length);
    // to socket
    if(mnl_config.nmea2socket > 0)
    {
        DBGOUT(buffer, length);
    }
    // to file
    if(mnl_config.nmea2file > 0)
    {
        if((enable_dbg_log == MTK_GPS_TRUE) && (dbglog_fp != 0))
        {
            fwrite(buffer, 1, length, (FILE *)dbglog_fp);
        }
    }
    // to framework
    write_full(gps_fd, buffer, length);
    return 0;
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_data_output_dbg
 * DESCRIPTION
 *  Transmit debug data out to task
 *  (The function body needs to be implemented)
 * PARAMETERS
 *  msg         [IN]
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_data_output_dbg (char* buffer, mtk_uint32 length)
{
    //write_full(gps_fd, buffer, length);
    // to socket
    if(mnl_config.dbg2socket > 0)
    {
        DBGOUT(buffer, length);
        MNL_ERR("%s\n", buffer);
    }
    // to file
    if(mnl_config.dbg2file > 0)
    {
        if((enable_dbg_log == MTK_GPS_TRUE) && (dbglog_fp != 0))
        {
            fwrite(buffer, 1, length, (FILE *)dbglog_fp);
        }
    }
    return 0;
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_data_output_dbg
 * DESCRIPTION
 *  Transmit debug data out to task
 *  (The function body needs to be implemented)
 * PARAMETERS
 *  buffer         [IN] data pointer
 *  length         [IN] size of data
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_data_output_dbg_uart (char* buffer, mtk_uint32 length)
{
   // to socket      
   if(mnl_config.dbg2socket > 0)
   {
       DBGOUT(buffer, length);
       MNL_ERR("%s\n", buffer);
   }
   return MTK_GPS_SUCCESS;
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_gps_output_dbg
 * DESCRIPTION
 *  Transmit gps debug data out to task
 *  (The function body needs to be implemented)
 * PARAMETERS
 *  msg         [IN]
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_gps_output_dbg (char* buffer, mtk_uint32 length)
{
    //write_full(gps_fd, buffer, length);
    // to socket
    if(mnl_config.dbg2socket > 0)
    {
        DBGOUT(buffer, length);
    }
    // to file
    if(mnl_config.dbg2file > 0)
    {
        if((enable_dbg_log == MTK_GPS_TRUE) && (dbglog_fp != 0))
        {
            fwrite(buffer, 1, length, (FILE *)dbglog_fp);
        }
    }
    //MNL_MSG("mtk_sys_gps_output_dbg\r\n");

    return 0;
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_if_set_spd
 * DESCRIPTION
 *  Set baud rate at host side from GPS lib
 *  (The function body needs to be implemented)
 * PARAMETERS
 *  baudrate         [IN]
 *  hw_fc            [IN]
 * RETURNS
 *  success(0)
 *****************************************************************************/
mtk_int32
mtk_sys_if_set_spd (mtk_uint32 baudrate, mtk_uint8 hw_fc)
{
#if 1
	// UART interface
	struct termios termOptions;
	MNL_MSG("if_set_spd=%d\n",baudrate);

	if (dsp_fd == -1)
	{
		return MTK_GPS_ERROR;
	}
	
	// Get the current options:
	tcgetattr( dsp_fd, &termOptions );

	switch(baudrate)
	{
		case 38400:
			// Set the input/output speed to 38400
			cfsetispeed(&termOptions, B38400);
			cfsetospeed(&termOptions, B38400);
			break;

		case 115200:
			cfsetispeed(&termOptions, B115200);
			cfsetospeed(&termOptions, B115200);
			break;

		case 230400:
			cfsetispeed(&termOptions, B230400);
			cfsetospeed(&termOptions, B230400);
			break;

		case 460800:
			cfsetispeed(&termOptions, B460800);
			cfsetospeed(&termOptions, B460800);
			break;
            
		case 921600:
			cfsetispeed(&termOptions, B921600);
			cfsetospeed(&termOptions, B921600);
			break;
		default:
			break;
	}

	/*
	if (hw_fc == 1)
	{
    		termOptions.c_cflag |= CRTSCTS; //CNEW_RTSCTS;
	}
	else
	{
		termOptions.c_cflag &= ~CRTSCTS; //~CNEW_RTSCTS;
	}
	*/
 
	// Now set the term options (set immediately)
	tcsetattr( dsp_fd, TCSANOW, &termOptions );
    tcflush(dsp_fd, TCIOFLUSH); /* Infinity: Flush in/out buffer */
	return MTK_GPS_SUCCESS;
#else
	// SPI interface
	// To do ....
#endif
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_create_mutex
 * DESCRIPTION
 *  Create a mutex object
 * PARAMETERS
 *  mutex_num        [IN]  mutex index used by MTK Nav Library
  * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_create_mutex(mtk_mutex_enum mutex_idx)
{
	// In linux, the mutex array is initialized to PTHREAD_MUTEX_INITIALIZER
	// No need to handle create action, but may do some error checking instead.

	if (mutex_idx >= MTK_MUTEX_END)
	{
		return MTK_GPS_ERROR;
	}
	
	return MTK_GPS_SUCCESS;
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_take_mutex
 * DESCRIPTION
 *  Request ownership of a mutex and if it's not available now, then block the thread execution
 * PARAMETERS
 *  mutex_num        [IN]  mutex index used by MTK Nav Library
  * RETURNS
 *  void
 *****************************************************************************/
void
mtk_sys_take_mutex(mtk_mutex_enum mutex_idx)
{
	pthread_mutex_lock(&g_hMutex[mutex_idx]);
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_give_mutex
 * DESCRIPTION
 *  Release a mutex ownership
 * PARAMETERS
 *  mutex_num        [IN]  mutex index used by MTK Nav Library
  * RETURNS
 *  void
 *****************************************************************************/
void
mtk_sys_give_mutex(mtk_mutex_enum mutex_idx)
{
	pthread_mutex_unlock(&g_hMutex[mutex_idx]); 
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_destroy_mutex
 * DESCRIPTION
 *  Destroy a mutex object
 * PARAMETERS
 *  mutex_num        [IN]  mutex index used by MTK Nav Library
  * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_destroy_mutex(mtk_mutex_enum mutex_idx)
{
	if (mutex_idx >= MTK_MUTEX_END)
	{
		return MTK_GPS_ERROR;
	}

	return MTK_GPS_SUCCESS;
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_start_result_handler
 * DESCRIPTION
 *  Handler routine for the result of restart command
 *  (The function body needs to be implemented)
 * PARAMETERS
 *  result         [IN]  the result of restart
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32 
mtk_sys_start_result_handler(mtk_gps_start_result result)
{
	if ((mtk_int32) MTK_GPS_ERROR == (mtk_int32)result)
	{
	// To do, handle restart result if needed by the host.
	}
	return MTK_GPS_SUCCESS;
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_spi_poll
 * DESCRIPTION
 *  Polling data input routine for SPI during dsp boot up stage.
 *  If use UART interface, this function can do nothing at all.
 *  (The function body needs to be implemented)
 * PARAMETERS
 *  void
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_spi_poll(void)
{
	// spi interface will need this function to read the SPI input data
	return MTK_GPS_SUCCESS;
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_set_spi_mode
 * DESCRIPTION
 *  Set SPI interrupt/polling and support burst or not.
 *  If use UART interface, this function can do nothing at all.
 *  (The function body needs to be implemented)
 * PARAMETERS
 *  enable_int         [IN]  1 for enter interrupt mode , 0 for entering polling mode
 *  enable_burst       [IN]  1 for enable burst transfer, 0 for disable burst transfer
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_set_spi_mode(mtk_uint8 enable_int, mtk_uint8 enable_burst)
{
	// spi interface will need this function to handle mode and transfer in driver if needed.
	return MTK_GPS_SUCCESS;
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_epo_open
 * DESCRIPTION
 *  Open EPO file
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_epo_open (void)
{
    return MTK_GPS_ERROR; //0
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_epo_close
 * DESCRIPTION
 *  Close EPO file
 * RETURNS
 *  void
 *****************************************************************************/
void
mtk_sys_epo_close (void)
{
    return;
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_epo_read
 * DESCRIPTION
 *  Read EPO file
 *  (blocking read until reaching 'length' or EOF)
 * PARAMETERS
 *  buffer      [OUT]
 *  offset      [IN]
 *  length      [IN]
 *  p_nRead     [OUT]
 * RETURNS
 *  success(MTK_GPS_SUCCESS)
 *****************************************************************************/
mtk_int32
mtk_sys_epo_read (void* buffer, mtk_uint32 offset, mtk_uint32 length,
                      mtk_uint32* p_nRead)
{
    return MTK_GPS_ERROR; //0
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_bee_event_create
 * DESCRIPTION
 *  Create an event object for BEE module
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_bee_event_create(void)
{
    // Initialize Mutex and condition for BEE
    if (pthread_mutex_init(&g_bee_mtx, NULL))
    {
        return MTK_GPS_ERROR;
    }
    
    if (pthread_cond_init(&g_bee_cond, NULL))
    {
        return MTK_GPS_ERROR;
    }
    else
    {
        return MTK_GPS_SUCCESS;
    }

    return MTK_GPS_SUCCESS;
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_bee_event_delete
 * DESCRIPTION
 *  Delete the event object of BEE module
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_bee_event_delete(void)
{
    mtk_int32 return_value = MTK_GPS_SUCCESS;
    
    if (pthread_cond_destroy(&g_bee_cond))
    {
        return_value = MTK_GPS_ERROR;
    }
    
    if (pthread_mutex_destroy(&g_bee_mtx))
    {
        return_value = MTK_GPS_ERROR;
    }
    
    return return_value;    
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_bee_event_set
 * DESCRIPTION
 *  Set the event object of BEE module
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_bee_event_set(void)
{
    mtk_int32 return_value = MTK_GPS_SUCCESS;
    
    if (pthread_mutex_lock(&g_bee_mtx))
    {
         return_value = MTK_GPS_ERROR;
    }
    if (pthread_cond_signal(&g_bee_cond))
    {
        return_value = MTK_GPS_ERROR;
    }
    
    pthread_mutex_unlock(&g_bee_mtx);
    
    return return_value;
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_bee_event_wait
 * DESCRIPTION
 *  Wait for the event object of BEE module, blocking mode
 * RETURNS
 *  success(MTK_GPS_SUCCESS); failure (MTK_GPS_ERROR)
 *****************************************************************************/
mtk_int32
mtk_sys_bee_event_wait(void)
{
    mtk_int32 return_value = MTK_GPS_SUCCESS;
    
    if (pthread_mutex_lock(&g_bee_mtx))
    {
         return_value = MTK_GPS_ERROR;
    }
    if (pthread_cond_wait(&g_bee_cond, &g_bee_mtx))
    {
        return_value = MTK_GPS_ERROR;
    }
    
    pthread_mutex_unlock(&g_bee_mtx);
    
    return return_value;
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_pmtk_cmd_cb
 * DESCRIPTION
 *  Notify porting layer that MNL has received one PMTK command.
 * PARAMETERS
 *  u2CmdNum        [IN]  The received PMTK command number.
 * RETURNS
 *  void
 *****************************************************************************/
void
mtk_sys_pmtk_cmd_cb(mtk_uint16 u2CmdNum)
{
    ;
}

/*****************************************************************************
* FUNCTION
*  mtk_sys_time_tick_get
* DESCRIPTION
*  get the current system tick of target platform (msec)
* PARAMETERS
*  none
* RETURNS
*  system time tick
*****************************************************************************/
mtk_uint32
mtk_sys_time_tick_get (void)
{
#define UPTIME  "/proc/uptime"
    FILE *fp;
    float t = 0;

    fp = fopen(UPTIME, "r");

    fscanf(fp, "%f", &t);

    MNL_MSG("sys_tick: %s: (%f) %d \n", UPTIME, t, (unsigned int)(t * 1000.0));

    fclose(fp);

    return (mtk_uint32)(t * 1000.0);
}

/*****************************************************************************
 * FUNCTION
 *  mtk_sys_time_tick_get_max
 * DESCRIPTION
 *  get the maximum system tick of target platform (msec)
 * PARAMETERS
 *  none
 * RETURNS
 *  system time tick
 *****************************************************************************/
mtk_uint32
mtk_sys_time_tick_get_max (void)
{
    return 0xFFFFFFFF;
}

