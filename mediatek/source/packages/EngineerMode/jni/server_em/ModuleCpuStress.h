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

#ifndef __AFM_MODULE_CPUSTRESS__
#define __AFM_MODULE_CPUSTRESS__

#define ERROR "ERROR"
#define CPUTEST_RESULT_SIZE 512

#define INDEX_TEST_L2C 0
#define INDEX_TEST_NEON 1
#define INDEX_TEST_NEON_DUAL 2
//#define INDEX_TEST_NEON_1 3
#define INDEX_TEST_CA9 4
#define INDEX_TEST_CA9_DUAL 5
//#define INDEX_TEST_CA9_1 6
#define INDEX_TEST_BACKUP 20
#define INDEX_TEST_BACKUP_TEST 21
#define INDEX_TEST_BACKUP_SINGLE 22
#define INDEX_TEST_BACKUP_DUAL 23
#define INDEX_TEST_RESTORE 24
#define INDEX_TEST_RESTORE_TEST 25
#define INDEX_TEST_RESTORE_SINGLE 26
#define INDEX_TEST_RESTORE_DUAL 27


#define FILE_L2C "/sys/bus/platform/drivers/ca9_l2c_test/slt_ca9_l2c"
#define FILE_NEON "/sys/bus/platform/drivers/cpu0_ca9_neon/cpu0_slt_ca9_neon"
#define FILE_NEON_DUAL_0 "/sys/bus/platform/drivers/cpu0_ca9_neon/cpu0_slt_ca9_neon"
#define FILE_NEON_DUAL_1 "/sys/bus/platform/drivers/cpu1_ca9_neon/cpu1_slt_ca9_neon"
#define FILE_CA9 "/sys/bus/platform/drivers/cpu0_ca9_max_power/cpu0_slt_ca9_max_power"
#define FILE_CA9_DUAL_0 "/sys/bus/platform/drivers/cpu0_ca9_max_power/cpu0_slt_ca9_max_power"
#define FILE_CA9_DUAL_1 "/sys/bus/platform/drivers/cpu1_ca9_max_power/cpu1_slt_ca9_max_power"

#define FILE_CPU0_SCAL "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"
#define FILE_CPU1_SCAL "/sys/devices/system/cpu/cpu1/cpufreq/scaling_governor"
#define FILE_CPU0_ONLINE "/sys/devices/system/cpu/cpu0/online"
#define FILE_CPU1_ONLINE "/sys/devices/system/cpu/cpu1/online"
#define FILE_HOTPLUG "/proc/mtk_hotplug/enable"

#define INDEX_SWCODEC_TEST_SINGLE 0
#define INDEX_SWCODEC_TEST_FORCE_SINGLE 1
#define INDEX_SWCODEC_TEST_FORCE_DUAL 2

#define COMMAND_SWCODEC_TEST_SINGLE "/data/mfv_ut_75 EM "
#define COMMAND_SWCODEC_TEST_FORCE_SINGLE "/data/mfv_ut_77_cpu0 EM "
#define COMMAND_SWCODEC_TEST_DUAL_0 "/data/mfv_ut_77_cpu0 EM "
#define COMMAND_SWCODEC_TEST_DUAL_1 "/data/mfv_ut_77_cpu1 EM "

#define INDEX_THERMAL_DISABLE 0
#define INDEX_THERMAL_ENABLE 1
#define THERMAL_DISABLE_COMMAND "/system/bin/thermal_manager /etc/.tp/.ht120.mtc"
#define THERMAL_ENABLE_COMMAND "/system/bin/thermal_manager /etc/.tp/thermal.conf"

struct thread_params_t {
	char file[CPUTEST_RESULT_SIZE >> 1];
	char result[CPUTEST_RESULT_SIZE >> 1];
};

struct thread_status_t {
	pthread_t pid;
	int create_result;
	struct thread_params_t param;
};

static char backup_first[CPUTEST_RESULT_SIZE>>1] = {0};
static char backup_second[CPUTEST_RESULT_SIZE>>1] = {0};
static pthread_mutex_t lock;

class RPCClient;

class ModuleCpuStress {
public:
	ModuleCpuStress();
	virtual ~ModuleCpuStress();
	static int ApMcu(RPCClient* msgSender);
	static int SwCodec(RPCClient* msgSender);
	static int BackupRestore(RPCClient* msgSender);
	static int ThermalUpdate(RPCClient* msgSender);
};

#endif	
