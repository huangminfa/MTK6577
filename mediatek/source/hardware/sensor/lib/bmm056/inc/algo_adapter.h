/*
 * =====================================================================================
 *
 *       Filename:  algo_adapter.h
 *
 *    Description:  
 *
 *        Version:  1.0
 *        Created:  05/08/2011 10:19:47 PM
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  Zhengguang.Guo@bosch-sensortec.com
 *        Company:  
 *
 * =====================================================================================
 */



/*************************************************************************************************/
/*  Disclaimer
*
* Common:
* Bosch Sensortec products are developed for the consumer goods industry. They may only be used
* within the parameters of the respective valid product data sheet.  Bosch Sensortec products are
* provided with the express understanding that there is no warranty of fitness for a particular purpose.
* They are not fit for use in life-sustaining, safety or security sensitive systems or any system or device
* that may lead to bodily harm or property damage if the system or device malfunctions. In addition,
* Bosch Sensortec products are not fit for use in products which interact with motor vehicle systems.
* The resale and/or use of products are at the purchasers own risk and his own responsibility. The
* examination of fitness for the intended use is the sole responsibility of the Purchaser.
*
* The purchaser shall indemnify Bosch Sensortec from all third party claims, including any claims for
* incidental, or consequential damages, arising from any product use not covered by the parameters of
* the respective valid product data sheet or not approved by Bosch Sensortec and reimburse Bosch
* Sensortec for all costs in connection with such claims.
*
* The purchaser must monitor the market for the purchased products, particularly with regard to
* product safety and inform Bosch Sensortec without delay of all security relevant incidents.
*
* Engineering Samples are marked with an asterisk (*) or (e). Samples may vary from the valid
* technical specifications of the product series. They are therefore not intended or fit for resale to third
* parties or for use in end products. Their sole purpose is internal client testing. The testing of an
* engineering sample may in no way replace the testing of a product series. Bosch Sensortec
* assumes no liability for the use of engineering samples. By accepting the engineering samples, the
* Purchaser agrees to indemnify Bosch Sensortec from all claims arising from the use of engineering
* samples.
*
* Special:
* This software module (hereinafter called "Software") and any information on application-sheets
* (hereinafter called "Information") is provided free of charge for the sole purpose to support your
* application work. The Software and Information is subject to the following terms and conditions:
*
* The Software is specifically designed for the exclusive use for Bosch Sensortec products by
* personnel who have special experience and training. Do not use this Software if you do not have the
* proper experience or training.
*
* This Software package is provided `` as is `` and without any expressed or implied warranties,
* including without limitation, the implied warranties of merchantability and fitness for a particular
* purpose.
*
* Bosch Sensortec and their representatives and agents deny any liability for the functional impairment
* of this Software in terms of fitness, performance and safety. Bosch Sensortec and their
* representatives and agents shall not be liable for any direct or indirect damages or injury, except as
* otherwise stipulated in mandatory applicable law.
*
* The Information provided is believed to be accurate and reliable. Bosch Sensortec assumes no
* responsibility for the consequences of use of such Information nor for any infringement of patents or
* other rights of third parties which may result from its use. No license is granted by implication or
* otherwise under any patent or patent rights of Bosch. Specifications mentioned in the Information are
* subject to change without notice.
*
* It is not allowed to deliver the source code of the Software to any third party without permission of
* Bosch Sensortec.
*/


#ifndef __ALGO_ADAPTER_H
#define __ALGO_ADAPTER_H
#include "configure.h"
#include "options.h"
#include "algo_if.h"

#define CFG_TOLERANCE_TIME_PRECISION 2

#define ALGO_OPMODE_OFF 0

enum ALGO_FUSION_MODE {
	ALGO_FUSION_MODE_COMPASS = 0,
	ALGO_FUSION_MODE_M4G,
	ALGO_FUSION_MODE_IMU,
	ALGO_FUSION_MODE_9DOF,
};


#define ALGO_MODULE_ID_ACC (uint32_t)'_acc'
#define ALGO_MODULE_ID_MAG (uint32_t)'_mag'
#define ALGO_MODULE_ID_GYR (uint32_t)'_gyr'
#define ALGO_MODULE_ID_CMP (uint32_t)'_cmp'	/* compass */
#define ALGO_MODULE_ID_M4G (uint32_t)'_m4g'
#define ALGO_MODULE_ID_IMU (uint32_t)'_imu'
#define ALGO_MODULE_ID_9DOF (uint32_t)'_9df'
#define ALGO_MODULE_ID_VLA (uint32_t)'_vla'


struct algo_module {
	const char *name;
	uint32_t id;

	int op_mode:3;
	int ref:5;

	void (*enable)(struct algo_module *, int);
};


struct algo_product {
	const uint32_t type;
	const int32_t id;
	uint32_t enable:1;
	uint32_t dr_a:8;
	uint32_t dr_m:8;
	uint32_t dr_g:8;
};

extern BS_U8 g_dr_a;
extern BS_U8 g_dr_m;
extern BS_U8 g_dr_g;

extern FILE *g_fp_profile_calib_a;
extern FILE *g_fp_profile_calib_m;
extern FILE *g_fp_profile_calib_g;

extern struct calib_profile g_profile_calib_a;
extern struct calib_profile g_profile_calib_m;
extern struct calib_profile g_profile_calib_g;

void algo_bst_get_version(int *, char *, char* *);

int algo_init_bst();

int algo_enable_product(struct algo_product *ap, int enable);

#if SPT_SENSOR_A
BS_U8 algo_proc_data_a(BS_S32 ts);
int algo_get_proc_data_a(sensor_data_t *pdata);
unsigned char algo_get_data_accuracy_a();
#endif

#if SPT_SENSOR_M
BS_U8 algo_proc_data_m(BS_S32 ts);
int algo_get_proc_data_m(sensor_data_t *pdata);
unsigned char algo_get_data_accuracy_m();
#endif

#if SPT_SENSOR_G
BS_U8 algo_proc_data_g(BS_S32 ts);
int algo_get_proc_data_g(sensor_data_t *pdata);
unsigned char algo_get_data_accuracy_g();
#endif

#if SPT_SENSOR_O
BS_U8 algo_proc_data_o(BS_S32 ts);
int algo_get_proc_data_o(sensor_data_t *pdata);
unsigned char algo_get_data_accuracy_o();
#endif


#if SPT_SENSOR_RO
BS_U8 algo_proc_data_ro(BS_S32 ts);
int algo_get_proc_data_ro(sensor_data_t *pdata);
unsigned char algo_get_data_accuracy_ro();
#endif


#if SPT_SENSOR_VG
BS_U8 algo_proc_data_vg(BS_S32 ts);
int algo_get_proc_data_vg(sensor_data_t *pdata);
#endif

#if SPT_SENSOR_VLA
BS_U8 algo_proc_data_vla(BS_S32 ts);
int algo_get_proc_data_vla(sensor_data_t *pdata);
#endif

#if SPT_SENSOR_VRV
BS_U8 algo_proc_data_vrv(BS_S32 ts);
int algo_get_proc_data_vrv(sensor_data_t *pdata);
#endif

void algo_load_calib_profile(char magic);
void algo_save_calib_profile(char magic);


void algo_on_interval_changed(struct algo_product *ap, int *interval);
BS_S32 algo_proc_data(BS_S32 ts);

void algo_adapter_init();
void algo_mod_init();
int algo_get_hint_interval();
void algo_get_curr_hw_dep(hw_dep_set_t *dep);

#endif
