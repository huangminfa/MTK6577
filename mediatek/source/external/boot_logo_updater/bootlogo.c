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

#include <sys/mman.h>

#include <dlfcn.h>

#include <cutils/ashmem.h>
#include <cutils/log.h>

#include <hardware/hardware.h>
#include <hardware/gralloc.h>

#include <fcntl.h>
#include <errno.h>
#include <sys/ioctl.h>
#include <string.h>
#include <stdlib.h>
#include <sched.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <linux/fb.h>

#include <cutils/pmem.h>
#include <cutils/log.h>
#include <cutils/atomic.h>
//#include "mtkfb.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "zlib.h"
//#include <mtd_utilc.h>
#include <sys/reboot.h>

#ifdef LOG_TAG
#undef LOG_TAG
#define LOG_TAG "BootLogoUpdater"
#endif

#define RGB565_TO_ARGB8888(x)   \
    ((((x) &   0x1F) << 3) |    \
     (((x) &  0x7E0) << 5) |    \
     (((x) & 0xF800) << 8) |    \
     (0xFF << 24)) // opaque

#define LOGO_BUFFER_SIZE	0x300000

static unsigned int *fb_addr = NULL;
static int fb_fd = 0;

static unsigned int *logo_addr = NULL;
static unsigned int logo_fd = 0;

static unsigned int fb_size = 0;
static struct fb_var_screeninfo vinfo;
static struct fb_fix_screeninfo finfo;
static unsigned int x_virtual = 0;
void mt65xx_logo_init(void)
{
	// read and de-compress logo data here
	int fd = 0;
	int len = 0;
	int mtdid = 0;

	fd = open("/dev/logo", O_RDONLY);
	if(fd < 0)
	{
		LOGE("open logo partition device file fail");
		return;
	}

	logo_addr = (unsigned int*)malloc(LOGO_BUFFER_SIZE);
	if(logo_addr == NULL)
	{
		LOGE("allocate logo buffer fail, size=0x%08x", LOGO_BUFFER_SIZE);
		goto error_reboot;
	}

	// (1) skip the image header
	len = read(fd, logo_addr, 512);
	if (len < 0)
	{
		LOGE("read from logo addr for 512B is failed!");
		goto error_reboot;
	}
	len = read(fd, logo_addr, LOGO_BUFFER_SIZE);
	if (len < 0)
	{
		LOGE("read from logo addr for buffer is failed!");
		goto error_reboot;
	}
    close(fd);
	return;

error_reboot:
	close(fd);
	reboot(LINUX_REBOOT_CMD_RESTART);
}

void mt65xx_disp_update(void)
{
	// use ioctl to framebuffer to udpate scree
	vinfo.yoffset = 0;
    vinfo.activate |= (FB_ACTIVATE_FORCE | FB_ACTIVATE_NOW);

    if (ioctl(fb_fd, FBIOPUT_VSCREENINFO, &vinfo) < 0) {
        LOGE("ioctl FBIOPUT_VSCREENINFO flip failed\n");
    }
}

void* mt65xx_get_logo_db_addr(void)
{
	return logo_addr;
}


void* mt65xx_get_fb_addr(void)
{
	return fb_addr;
}

unsigned int mt65xx_get_fb_size(void)
{
	return fb_size;
}

// this should be the ut case entry
int mt65xx_fb_init(void)
{
	unsigned int bytespp;

	fb_fd = open("/dev/graphics/fb0", O_RDWR);
	if(fb_fd < 0)
	{
		LOGE("open dev file fail\n");
		goto error;
	}

    ioctl(fb_fd,FBIOGET_VSCREENINFO,&vinfo);
	ioctl(fb_fd,FBIOGET_FSCREENINFO,&finfo);

    bytespp = vinfo.bits_per_pixel / 8;

//    fb_size = vinfo.xres_virtual * vinfo.yres * vinfo.bits_per_pixel / 8;
	fb_size = finfo.line_length * vinfo.yres;
	x_virtual = finfo.line_length/(vinfo.bits_per_pixel / 8);
    fb_addr =(unsigned int*)mmap (0, fb_size, PROT_READ | PROT_WRITE, MAP_SHARED, fb_fd,0);
	if(fb_addr == NULL)
	{
		LOGE("mmap fail\n");
		goto error;
	}
	else
	{

	}

	return 0;

error:
	munmap(fb_addr, fb_size);
	close(fb_fd);

	return -1;
}

void mt65xx_fb_deinit(void)
{
	munmap(fb_addr, fb_size);
	close(fb_fd);
}

void mt65xx_logo_deinit(void)
{
	close(logo_fd);
	free(logo_addr);
}

/*=======================================================================*/
/* HEADER FILES                                                          */
/*=======================================================================*/

// ---------------------------------------------------------------------------

int mt65xx_logo_decompress(void *in, void *out, int inlen, int outlen)
{
    int ret;
    unsigned have;
    z_stream strm;

    memset(&strm, 0, sizeof(z_stream));
    /* allocate inflate state */
    strm.zalloc = Z_NULL;
    strm.zfree = Z_NULL;
    strm.opaque = Z_NULL;
    strm.avail_in = 0;
    strm.next_in = Z_NULL;
    ret = inflateInit(&strm);
    if (ret != Z_OK)
        return ret;

    /* decompress until deflate stream ends or end of file */
    do {
        strm.avail_in = inlen;
        if (strm.avail_in <= 0)
            break;
        strm.next_in = in;

        /* run inflate() on input until output buffer not full */
        do {
            strm.avail_out = outlen;
            strm.next_out = out;
            ret = inflate(&strm, Z_NO_FLUSH);
            switch (ret) {
            case Z_NEED_DICT:
                ret = Z_DATA_ERROR;     /* and fall through */
            case Z_DATA_ERROR:
            case Z_MEM_ERROR:
                (void)inflateEnd(&strm);
                return ret;
            }
            have = outlen - strm.avail_out;
        } while (strm.avail_out == 0);


        /* done when inflate() says it's done */
    } while (ret != Z_STREAM_END);

    if (ret == Z_STREAM_END)
    /* clean up and return */
    (void)inflateEnd(&strm);
    return ret == Z_STREAM_END ? Z_OK : Z_DATA_ERROR;
}

static void show_logo(unsigned int index)
{
	unsigned int logonum;
    unsigned int logolen;
	unsigned int inaddr;
    void  *fb_addr = mt65xx_get_fb_addr();
	void *temp = NULL;
    unsigned int fb_size = mt65xx_get_fb_size();
    void  *db_addr = mt65xx_get_logo_db_addr();

	unsigned int *pinfo = (unsigned int*)db_addr;
    logonum = pinfo[0];

	temp = malloc(fb_size);
	if(temp == NULL)
	{
		LOGE("allocate buffer fail\n");
		//return;
		if (!index) // index==0, draw logo
			reboot(LINUX_REBOOT_CMD_RESTART);
		else //draw charging animation.
			reboot(RB_POWER_OFF);

	}

	if(index < logonum)
		logolen = pinfo[3+index] - pinfo[2+index];
	else
		logolen = pinfo[1] - pinfo[2+index];

	inaddr = (unsigned int)db_addr+pinfo[2+index];

    mt65xx_logo_decompress((void*)inaddr, (void*)temp, logolen, fb_size);

	if (16 == vinfo.bits_per_pixel) // RGB565
    {
        memcpy(fb_addr, temp, fb_size);
    }
    else if (32 == vinfo.bits_per_pixel) // ARGB8888
    {
        unsigned short src_rgb565 = 0;

        unsigned short *s = temp;
        unsigned int   *d = fb_addr;
        int j = 0;
		int k = 0;
/*
        for (j = 0; j < fb_size/4; ++ j)
        {
            src_rgb565 = *s++;
            *d++ = RGB565_TO_ARGB8888(src_rgb565);
        }
*/
#if 1
		if(0 == strncmp(MTK_LCM_PHYSICAL_ROTATION, "270", 3))
		{
			unsigned int l;
			for (j=0; j<vinfo.xres; j++){
		  		for (k=0, l=vinfo.yres-1; k<vinfo.yres; k++, l--)
		    	{
					src_rgb565 = *s++;
					d = fb_addr + ((x_virtual * l + j) << 2);
					*d = RGB565_TO_ARGB8888(src_rgb565);
		    	}
			}
		}
		else if(0 == strncmp(MTK_LCM_PHYSICAL_ROTATION, "90", 2))
		{
			unsigned int l;
			for (j=vinfo.xres - 1; j>=0; j--){
		  		for (k=0, l=0; k<vinfo.yres; k++, l++)
		    	{
					src_rgb565 = *s++;
					d = fb_addr + ((x_virtual * l + j) << 2);
					*d = RGB565_TO_ARGB8888(src_rgb565);
		    	}
			}
		}
		else
#endif		
		{
			for (j = 0; j < vinfo.yres; ++ j){
	    		for(k = 0; k < vinfo.xres; ++ k)
            	{
                	src_rgb565 = *s++;
                	*d++ = RGB565_TO_ARGB8888(src_rgb565);
            	}
	    		for(k = vinfo.xres; k < x_virtual; ++ k)
					*d++ = 0xFFFFFFFF;
			}
		}

    }
    else
    {
        LOGE("unknown format bpp: %d\n", vinfo.bits_per_pixel);
    }


	free(temp);
}

void mt65xx_disp_show_boot_logo(void)
{
    show_logo(0);
    mt65xx_disp_update();

    return;
}

void bootlogo_fb_init()
{
	mt65xx_logo_init();
	mt65xx_fb_init();
}

void bootlogo_fb_deinit()
{
	mt65xx_fb_deinit();
	mt65xx_logo_deinit();
}

void bootlogo_show_boot()
{
	mt65xx_disp_show_boot_logo();
}
