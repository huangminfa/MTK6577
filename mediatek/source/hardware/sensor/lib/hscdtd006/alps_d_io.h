/* alps_d_io.h
 *
 * I/O controll header for alps sensor
 *
 * Copyright (C) 2011 ALPS ELECTRIC CO., LTD.
 *
 * This software is licensed under the terms of the GNU General Public
 * License version 2, as published by the Free Software Foundation, and
 * may be copied, distributed, and modified under those terms.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */

#ifndef ___ALPS_D_IO_H_INCLUDED
#define ___ALPS_D_IO_H_INCLUDED

#include <linux/ioctl.h>

/* IOCTLs for ALPS_Msensor device zhijie*/
/* IOCTLs for AcdApiDaemon */
#define MSENSOR						   0x83
#define ALPSIO_AD_GET_ACTIVATE     _IOR(MSENSOR, 0x30, int)
#define ALPSIO_AD_GET_DELAY        _IOR(MSENSOR, 0x31, int)
#define ALPSIO_AD_GET_DATA         _IOR(MSENSOR, 0x32, int[11])
#define ALPSIO_AD_SET_DATA         _IOW(MSENSOR, 0x33, int[15])
#define ALPSIO_AD_EXE_SELF_TEST_A  _IOR(MSENSOR, 0x34, int)
#define ALPSIO_AD_EXE_SELF_TEST_B  _IOR(MSENSOR, 0x35, int)

enum {
    ACTIVE_SS_NUL = 0x00 ,
    ACTIVE_SS_ACC = 0x01 ,
    ACTIVE_SS_MAG = 0x02 ,
    ACTIVE_SS_ORI = 0x04 ,
};

struct TAIFD_HW_DATA {
    int activate;
    int delay;
    int acc[4];
    int mag[4];
};

struct TAIFD_SW_DATA {
    int acc[5];
    int mag[5];
    int ori[5];
};


#endif

