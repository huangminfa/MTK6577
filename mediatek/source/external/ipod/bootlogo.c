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

#include <cutils/log.h>
#include <cutils/atomic.h>
#include "mtkfb.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "zlib.h"
#include "cust_display.h"
#include <mtd_utilc.h>
#include <sys/reboot.h>
#include "ipodmain.h"

void cust_show_battery_capacity(unsigned int capacity);
void cust_show_battery_capacity_new(unsigned int capacity);

typedef enum {
   DISP_VERTICAL_PROG_BAR = 0,
   DISP_HORIZONTAL_PROG_BAR,
} DISP_PROG_BAR_DIRECT;

#define RGB565_TO_ARGB8888(x)   \
    ((((x) &   0x1F) << 3) |    \
     (((x) &  0x7E0) << 5) |    \
     (((x) & 0xF800) << 8) |    \
     (0xFF << 24)) // opaque

#define LOGO_BUFFER_SIZE	0x300000
static const unsigned int VOLTAGE_SCALE = 4;

typedef struct {
    unsigned int left, top, right, bottom;
} RECT;

static RECT bar_rect = {BAR_LEFT, BAR_TOP, BAR_RIGHT, BAR_BOTTOM};

static unsigned int bar_occupied_color = RGB565_TO_ARGB8888(BAR_OCCUPIED_COLOR);
static unsigned int bar_empty_color    = RGB565_TO_ARGB8888(BAR_EMPTY_COLOR);
static unsigned int bar_bg_color       = RGB565_TO_ARGB8888(BAR_BG_COLOR);

static unsigned int *fb_addr = NULL;
static int fb_fd = 0;

static unsigned int *logo_addr = NULL;
static unsigned int logo_fd = 0;

static unsigned int fb_size = 0;
static struct fb_var_screeninfo vinfo;
static struct fb_fix_screeninfo finfo;
static unsigned int x_virtual = 0;

// for new

// for new method
/*
#ifndef ANIMATION_NEW

#define CAPACITY_LEFT                (172) // battery capacity center
#define CAPACITY_TOP                 (330)
#define CAPACITY_RIGHT               (307)
#define CAPACITY_BOTTOM              (546)

#define NUMBER_LEFT                  (178) // number
#define NUMBER_TOP                   (190)
#define NUMBER_RIGHT                 (216)
#define NUMBER_BOTTOM                (244)

#define PERCENT_LEFT                 (254) // percent number_left + 2*number_width
#define PERCENT_TOP                  (190)
#define PERCENT_RIGHT                (302)
#define PERCENT_BOTTOM               (244)

#define TOP_ANIMATION_LEFT           (172) // top animation
#define TOP_ANIMATION_TOP            (100)
#define TOP_ANIMATION_RIGHT          (307)
#define TOP_ANIMATION_BOTTOM         (124)

#endif   // for new method
*/
#define UINT32 unsigned int
#define UINT16 unsigned short
#define UINT8 unsigned char

#define INT32 int

UINT32 animation_index = 0;
int charging_index = -1;
static char animation_addr[82*16*2] = {0x0};


static int number_pic_width = NUMBER_RIGHT - NUMBER_LEFT;       //width
static int number_pic_height = NUMBER_BOTTOM - NUMBER_TOP;       //height
int number_pic_size = (NUMBER_RIGHT - NUMBER_LEFT)*(NUMBER_BOTTOM - NUMBER_TOP)*2;   //size
char number_pic_addr[(NUMBER_RIGHT - NUMBER_LEFT)*(NUMBER_BOTTOM - NUMBER_TOP)*2] = {0x0}; //addr
RECT number_location_rect = {NUMBER_LEFT,NUMBER_TOP,NUMBER_RIGHT,NUMBER_BOTTOM}; 
static int number_pic_start_0 = 4;
static int number_pic_percent = 14;


static int line_width = CAPACITY_RIGHT - CAPACITY_LEFT;
static int line_height = 1;
int line_pic_size = (TOP_ANIMATION_RIGHT - TOP_ANIMATION_LEFT)*2;
char line_pic_addr[(TOP_ANIMATION_RIGHT - TOP_ANIMATION_LEFT)*2] = {0x0};
RECT battery_rect = {CAPACITY_LEFT,CAPACITY_TOP,CAPACITY_RIGHT,CAPACITY_BOTTOM};


static int percent_pic_width = PERCENT_RIGHT - PERCENT_LEFT;
static int percent_pic_height = PERCENT_BOTTOM - PERCENT_TOP;
int percent_pic_size = (PERCENT_RIGHT - PERCENT_LEFT)*(PERCENT_BOTTOM - PERCENT_TOP)*2;
char percent_pic_addr[(PERCENT_RIGHT - PERCENT_LEFT)*(PERCENT_BOTTOM - PERCENT_TOP)*2] = {0x0};
RECT percent_location_rect = {PERCENT_LEFT,PERCENT_TOP,PERCENT_RIGHT,PERCENT_BOTTOM};

static int top_animation_width = TOP_ANIMATION_RIGHT - TOP_ANIMATION_LEFT;
static int top_animation_height = TOP_ANIMATION_BOTTOM - TOP_ANIMATION_TOP;
int top_animation_size = (TOP_ANIMATION_RIGHT - TOP_ANIMATION_LEFT)*(TOP_ANIMATION_BOTTOM - TOP_ANIMATION_TOP)*2;
char top_animation_addr[(TOP_ANIMATION_RIGHT - TOP_ANIMATION_LEFT)*(TOP_ANIMATION_BOTTOM - TOP_ANIMATION_TOP)*2] = {0x0};
RECT top_animation_rect = {TOP_ANIMATION_LEFT,TOP_ANIMATION_TOP,TOP_ANIMATION_RIGHT,TOP_ANIMATION_BOTTOM};

int charging_low_index = 0;
int charging_animation_index = 0;
//for new



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
	if (inCharging)
		reboot(RB_POWER_OFF);
	else //reboot
	{
		LOGE("reboot after 3sec"); // to prevent interlace operation with MD reset
		sleep(3);
		reboot(LINUX_REBOOT_CMD_RESTART);
	}
	
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

    printf("show_logo, in_addr=0x%08x, fb_addr=0x%08x, logolen=%d\n", 
                inaddr, fb_addr, logolen);

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


void mt65xx_disp_show_low_battery(void)
{
    show_logo(2);
    mt65xx_disp_update();

    return;
}

void mt65xx_disp_show_battery_full(void)
{
#ifdef ANIMATION_NEW
    cust_show_battery_capacity_new(100);
#else
    cust_show_battery_capacity(100);
#endif
    return;
}

void mt65xx_disp_show_charger_ov_logo(void)
{

    show_logo(3);
    mt65xx_disp_update();

    return;
}

void mt65xx_disp_show_battery_capacity(unsigned int capacity)
{
#ifdef ANIMATION_NEW
    cust_show_battery_capacity_new(capacity);
#else
    cust_show_battery_capacity(capacity);
#endif

    return;
}

void mt65xx_disp_fill_rect(unsigned int left, unsigned int top,
                           unsigned int right, unsigned int bottom,
                           unsigned int color)
{
    void * fb_addr = mt65xx_get_fb_addr();
    const unsigned int WIDTH = x_virtual;
    const unsigned int COLOR = color;

    unsigned int*pLine = (unsigned int*)fb_addr + top * WIDTH + left;
    int x, y;
#if 1
	if(0 == strncmp(MTK_LCM_PHYSICAL_ROTATION, "270", 3))
	{
		unsigned int l;
        unsigned int   *d = fb_addr;

		for (x=top; x<bottom; x++){
	  		for (y=left, l=vinfo.yres - left; y<right; y++, l--)
	    	{
				d = fb_addr + ((x_virtual * l + x) << 2);
				*d = COLOR;
	    	}
		}
	}
	else if(0 == strncmp(MTK_LCM_PHYSICAL_ROTATION, "90", 2))
	{
		unsigned int l;
        unsigned int   *d = fb_addr;
		for (x=vinfo.xres - top + 1; x > vinfo.xres - bottom; x--){
			for (y=left, l=left; y<right; y++, l++)
		   	{
				d = fb_addr + ((x_virtual * l + x) << 2);
				*d = COLOR;
		   	}
		}
	}
	else
#endif
    {
		for (y = top; y < (int)bottom; ++ y) {
        	unsigned int*pPixel = pLine;
        	for (x = left; x < (int)right; ++ x) {
            	*pPixel++ = COLOR;
        	}
        	pLine += WIDTH;
		}
    }
}

void mt65xx_disp_draw_prog_bar(DISP_PROG_BAR_DIRECT direct,
                               unsigned int left, unsigned int top,
                               unsigned int right, unsigned int bottom,
                               unsigned int fgColor, unsigned int bgColor,
                               unsigned int start_div, unsigned int total_div,
                               unsigned int occupied_div)
{
    const unsigned int PADDING = 3;
    unsigned int div_size  = (bottom - top) / total_div;
    unsigned int draw_size = div_size - PADDING;

    unsigned int i;

    if (DISP_HORIZONTAL_PROG_BAR == direct)
	{
		div_size = (right - left) / total_div;
		draw_size = div_size - PADDING;
    	for (i = start_div; i < start_div + occupied_div; ++ i)
    	{
			unsigned int draw_left = left + div_size * i + PADDING;
			unsigned int draw_right = draw_left + draw_size;

        	// fill one division of the progress bar
        	mt65xx_disp_fill_rect(draw_left, top, draw_right, bottom, fgColor);
		}
    }
	else if(DISP_VERTICAL_PROG_BAR == direct)
	{
		div_size  = (bottom - top) / total_div;
    	draw_size = div_size - PADDING;

    	for (i = start_div; i < start_div + occupied_div; ++ i)
    	{
        	unsigned int draw_bottom = bottom - div_size * i - PADDING;
        	unsigned int draw_top    = draw_bottom - draw_size;

        	// fill one division of the progress bar
        	mt65xx_disp_fill_rect(left, draw_top, right, draw_bottom, fgColor);
    	}
	}
	else
	{
		LOGE("direction not implemented");
	}
}

void cust_show_battery_capacity(unsigned int capacity)
{
	DISP_PROG_BAR_DIRECT direct;
#if MTK_QVGA_LANDSCAPE_SUPPORT
	if(0 == strncmp(MTK_LCM_PHYSICAL_ROTATION, "270", 3)
	   || 0 == strncmp(MTK_LCM_PHYSICAL_ROTATION, "90", 2))
//	if((MTK_LCM_PHYSICAL_ROTATION == 90 || MTK_LCM_PHYSICAL_ROTATION == 270))
	{
		direct = DISP_HORIZONTAL_PROG_BAR;
	}
	else
	{
		direct = DISP_VERTICAL_PROG_BAR;
	}
#else
	direct = DISP_VERTICAL_PROG_BAR;
#endif
	direct = DISP_VERTICAL_PROG_BAR;
    unsigned int capacity_grids = 0;

    if (capacity > 100) capacity = 100;

    capacity_grids = (capacity * VOLTAGE_SCALE) / 100;

    show_logo(1);

    // Fill Occupied Color

    mt65xx_disp_draw_prog_bar(direct,
                              bar_rect.left + 1, bar_rect.top + 1,
                              bar_rect.right, bar_rect.bottom,
                              bar_occupied_color, bar_bg_color,
                              0, VOLTAGE_SCALE, capacity_grids);

    // Fill Empty Color

    mt65xx_disp_draw_prog_bar(direct,
                              bar_rect.left + 1, bar_rect.top + 1,
                              bar_rect.right, bar_rect.bottom,
                              bar_empty_color, bar_bg_color,
                              capacity_grids, VOLTAGE_SCALE,
                              VOLTAGE_SCALE - capacity_grids);

    mt65xx_disp_update();
}

void cust_show_battery_capacity_new(UINT32 capacity)
{
#if MTK_QVGA_LANDSCAPE_SUPPORT
//	DISP_PROG_BAR_DIRECT direct = DISP_HORIZONTAL_PROG_BAR;
#else
//	DISP_PROG_BAR_DIRECT direct = DISP_VERTICAL_PROG_BAR;
#endif
	DISP_PROG_BAR_DIRECT direct = DISP_VERTICAL_PROG_BAR;

        printf("[ChargingAnimation]capacity : %d\n", capacity);

        if (capacity <= 0) 
          {
             //show_logo(2);
             //mt65xx_disp_update();	
             return;
          }
        
        if (capacity < 10 && capacity > 0) 
          {
             charging_low_index ++ ;
             show_logo(25 + charging_low_index);
             if (charging_low_index >= 9) charging_low_index = 0;
             show_animation_number(number_pic_start_0+capacity,1);
             show_animation(14, percent_location_rect, percent_pic_addr);
             mt65xx_disp_update();	
             return;
          }

        if (capacity >= 100) 
          {
             show_logo(37); // battery 100
             mt65xx_disp_update();	
             return;
          }

	UINT32 capacity_grids = 0;     
	//if (capacity > 100) capacity = 100;
	capacity_grids = battery_rect.bottom - (battery_rect.bottom - battery_rect.top) * (capacity - 10) / 90;
	printf("[ChargingAnimation]capacity : %d\n", capacity);
        printf("[ChargingAnimation]capacity_grids : %d\n", capacity_grids);
        show_logo(35);
        //show_animation_number(6,1); // for test

        show_animation_line(36,capacity_grids);
        
        top_animation_rect.bottom = capacity_grids;
        top_animation_rect.top = capacity_grids - top_animation_height;
        if (capacity <= 90) 
          {
            charging_animation_index++;
            printf("[ChargingAnimation]top_animation : left = %d, top = %d, right = %d, bottom = %d\n",  top_animation_rect.left, top_animation_rect.top,  top_animation_rect.right, top_animation_rect.bottom);
            show_animation(15 + charging_animation_index, top_animation_rect, top_animation_addr);
            if (charging_animation_index >= 9) charging_animation_index = 0;
         }

       if ((capacity >= 0)&&(capacity < 10))
	{
		show_animation_number(number_pic_start_0+capacity,1);
	}
	
	if ((capacity >= 10)&&(capacity < 100))
	{
		show_animation_number(number_pic_start_0+(capacity/10),0);
		show_animation_number(number_pic_start_0+(capacity%10),1);	
	}
        show_animation(14, percent_location_rect, percent_pic_addr);

        mt65xx_disp_update();
}

/*
static void fill_battery_flow(UINT32 left, UINT32 top, UINT32 right, UINT32 bottom, char *addr)
{
    void * fb_addr = mt65xx_get_fb_addr();
    const UINT32 WIDTH = x_virtual;
    UINT16 *pLine = (UINT16 *)fb_addr + top * WIDTH + left;	
    INT32 x, y;
    INT32 i = 0;
    UINT16 *pLine2 = (UINT16*)addr;
    UINT16 s = 0;
    for (y = top; y < bottom; ++ y) {
        UINT16 *pPixel = pLine;
        for (x = left; x < right; ++ x) {
            s = pLine2[i++];
	    if(s != 0)
	      *pPixel = RGB565_TO_ARGB8888(s);
	    pPixel++;
        }
        pLine += WIDTH;
    }
}

static void fill_battery_flow_line(UINT32 left, UINT32 top, UINT32 right, UINT32 bottom, char *addr)
{
    void * fb_addr = mt65xx_get_fb_addr();
    const UINT32 WIDTH = x_virtual;

    UINT16 *pLine = (UINT16 *)fb_addr + top * WIDTH + left;
	
    INT32 x, y;
    INT32 i = 0;
    UINT16 *pLine2 = (UINT16*)addr;
    UINT16 s = 0;
    for (y = top; y < bottom; ++ y) {
        UINT16 *pPixel = pLine;
        for (x = left; x < right; ++ x) {
	    s = pLine2[i++];
	    if(s != 0)
	      *pPixel = RGB565_TO_ARGB8888(s);
	    pPixel++;
        }
        i = 0;
        pLine += WIDTH;
    }
}
*/

static void fill_rect_flow(UINT32 left, UINT32 top, UINT32 right, UINT32 bottom, char *addr)
{
    void * fb_addr = mt65xx_get_fb_addr();
    const UINT32 WIDTH = x_virtual;
    const UINT32 HEIGHT = vinfo.yres; 
    UINT32 *pLine = (UINT32 *)fb_addr + top * WIDTH + left;
    UINT16 *pLine2 = (UINT16*)addr;	
    UINT32 x, y;
    UINT32 i = 0;
    UINT16 s = 0;
    printf("[ChargingAnimation]fill_rect_flow: WIDTH = %d, HEIGHT = %d, vinfo.xres = %d, fb_addr=0x%08x, pLine=0x%08x\n", WIDTH, HEIGHT, vinfo.xres,fb_addr,pLine);
    if(0 == strncmp(MTK_LCM_PHYSICAL_ROTATION, "270", 3))
	{
    printf("[ChargingAnimation]fill_rect_flow : MTK_LCM_PHYSICAL_ROTATION = 270\n");
	unsigned int l;
        UINT32 *d = fb_addr;
		for (x=top; x<bottom; x++) {
	  		for (y=left, l= HEIGHT - left; y<right; y++, l--)
	    	        {
				d = fb_addr + ((WIDTH * l + x) << 2);
	                        s = pLine2[i++];
	                        if(s != 0)
	                          *d = RGB565_TO_ARGB8888(s);
	    	        }
		}
	}
    else if(0 == strncmp(MTK_LCM_PHYSICAL_ROTATION, "90", 2))
	{
   printf("[ChargingAnimation]fill_rect_flow : MTK_LCM_PHYSICAL_ROTATION = 90\n");
        unsigned int l;
        UINT32 *d = fb_addr;
		for (x=WIDTH - top + 1; x > WIDTH - bottom; x--) {
			for (y=left, l=left; y<right; y++, l++)
		   	{
				d = fb_addr + ((WIDTH * l + x) << 2);
				s = pLine2[i++];
	                        if(s != 0)
	                          *d = RGB565_TO_ARGB8888(s);
		   	}
		}
	}
   else 
        {
          printf("[ChargingAnimation]fill_rect_flow: addr=0x%08x, pLine=0x%08x, WIDTH =0x%08x\n", addr, pLine,WIDTH);
           for (y = top; y < bottom; ++ y) {
              UINT32 *pPixel = pLine;
              for (x = left; x < right; ++ x) {
                s = pLine2[i++];
	        if(s != 0)
	          *pPixel = RGB565_TO_ARGB8888(s);
	        pPixel++;
              }
              pLine += WIDTH; 
           }
        }
}


static void fill_line_flow(UINT32 left, UINT32 top, UINT32 right, UINT32 bottom, char *addr)
{
    void * fb_addr = mt65xx_get_fb_addr();
    const UINT32 WIDTH = x_virtual;
    const UINT32 HEIGHT = vinfo.yres;
    UINT32 *pLine = (UINT32 *)fb_addr + top * WIDTH + left;
    UINT16 *pLine2 = (UINT16*)addr;		
    UINT32 x, y;
    UINT32 i = 0;
    UINT16 s = 0;
/*
    void * fb_addr = mt65xx_get_fb_addr();
    const unsigned int WIDTH = x_virtual;
    unsigned int *pLine = (unsigned int *)fb_addr + top * WIDTH + left;	
    unsigned int x, y;
    unsigned int i = 0;
    unsigned short *pLine2 = (unsigned short*)addr;
    unsigned short s = 0;
*/
    if(0 == strncmp(MTK_LCM_PHYSICAL_ROTATION, "270", 3))
	{
	unsigned int l;
        UINT32 *d = fb_addr;
		for (x=top; x<bottom; x++) {
	  		for (y=left, l= HEIGHT - left; y<right; y++, l--)
	    	        {
				d = fb_addr + ((WIDTH * l + x) << 2);
	                        s = pLine2[i++];
	                        if(s != 0)
	                          *d = RGB565_TO_ARGB8888(s);
	    	        }
                    i = 0;
		}
	}
    else if(0 == strncmp(MTK_LCM_PHYSICAL_ROTATION, "90", 2))
	{
        unsigned int l;
        UINT32 *d = fb_addr;
		for (x=WIDTH - top + 1; x > WIDTH - bottom; x--) {
			for (y=left, l=left; y<right; y++, l++)
		   	{
				d = fb_addr + ((WIDTH * l + x) << 2);
	                        s = pLine2[i++];
	                        if(s != 0)
	                          *d = RGB565_TO_ARGB8888(s);
		   	}
                    i = 0;
		}
	}
   else 
        {
           for (y = top; y < bottom; ++ y) {
              UINT32 *pPixel = pLine;
              for (x = left; x < right; ++ x) {
                s = pLine2[i++];
	        if(s != 0)
	          *pPixel = RGB565_TO_ARGB8888(s);
	        pPixel++;
              }
              pLine += WIDTH;
              i = 0;
           }

/*
    for (y = top; y < bottom; ++ y) {
        unsigned int *pPixel = pLine;
        for (x = left; x < right; ++ x) {
//            *pPixel++ = COLOR;
			  s = pLine2[i++];
			  if(s != 0)
			  *pPixel = RGB565_TO_ARGB8888(s);
			  pPixel++;
        }
        pLine += WIDTH;
    }
*/
        }
}

void mt65xx_disp_fill_rect_ex(unsigned int left, unsigned int top,
                           unsigned int right, unsigned int bottom,
                           void *addr)
{
    void * fb_addr = mt65xx_get_fb_addr();
    const unsigned int WIDTH = x_virtual;
    unsigned int *pLine = (unsigned int *)fb_addr + top * WIDTH + left;	
    unsigned int x, y;
    unsigned int i = 0;
    unsigned short *pLine2 = (unsigned short*)addr;
    unsigned short s = 0;
    for (y = top; y < bottom; ++ y) {
        unsigned int *pPixel = pLine;
        for (x = left; x < right; ++ x) {
//            *pPixel++ = COLOR;
			  s = pLine2[i++];
			  if(s != 0)
			  *pPixel = RGB565_TO_ARGB8888(s);
			  pPixel++;
        }
        pLine += WIDTH;
    }
}

void show_animation(UINT32 index, RECT rect, char* addr){
	        UINT32 logonum;
		UINT32 logolen;
		UINT32 inaddr;
		UINT32 i;
		
                void  *fb_addr = mt65xx_get_fb_addr();
                UINT32 fb_size = mt65xx_get_fb_size();
                void  *db_addr = mt65xx_get_logo_db_addr();
	
		unsigned int *pinfo = (unsigned int*)db_addr;
		logonum = pinfo[0];
		
		printf("[ChargingAnimation]show_animation : index = %d, logonum = %d\n", index, logonum);
		//ASSERT(index < logonum);

		printf("[ChargingAnimation]show_animation :pinfo[0] = %d, pinfo[1] = %d, pinfo[2]= %d, pinfo[3] = %d\n",pinfo[0] , pinfo[1],pinfo[2],pinfo[3]);
		printf("[ChargingAnimation]show_animation :pinfo[2+index] = %d, pinfo[1] = %d\n",pinfo[2+index] , pinfo[3+index]);
		if(index < logonum)
			logolen = pinfo[3+index] - pinfo[2+index];
		else
			logolen = pinfo[1] - pinfo[2+index];
	
		inaddr = (unsigned int)db_addr+pinfo[2+index];
		printf("[ChargingAnimation]show_animation: in_addr=0x%08x, dest_addr=0x%08x, logolen=%d\n", 
					inaddr, logolen, logolen);
	
		mt65xx_logo_decompress((void*)inaddr, (void*)addr, logolen, (rect.right-rect.left)*(rect.bottom-rect.top)*2);
                printf("[ChargingAnimation]show_animation : rect right = %d\n", rect.right);
                printf("[ChargingAnimation]show_animation : rect top = %d\n", rect.top);
                printf("[ChargingAnimation]show_animation : rect size = %d\n", (rect.right-rect.left)*(rect.bottom-rect.top)*2);

		fill_rect_flow(rect.left,rect.top,rect.right,rect.bottom,addr);
		
	}


// number_position: 0~1st number, 1~2nd number ,2~%
void show_animation_number(UINT32 index,UINT32 number_position){
	UINT32 logonum;
	UINT32 logolen;
	UINT32 inaddr;
	UINT32 i;
		
	void  *fb_addr = mt65xx_get_fb_addr();
        UINT32 fb_size = mt65xx_get_fb_size();
        void  *db_addr = mt65xx_get_logo_db_addr();

	unsigned int *pinfo = (unsigned int*)db_addr;
	logonum = pinfo[0];
	
	printf("[ChargingAnimation]show_animation_number :index= %d, logonum = %d\n", index, logonum);
	//ASSERT(index < logonum);

	if(index < logonum)
		logolen = pinfo[3+index] - pinfo[2+index];
	else
		logolen = pinfo[1] - pinfo[2+index];

	inaddr = (unsigned int)db_addr+pinfo[2+index];
	printf("[ChargingAnimation]show_animation_number, in_addr=0x%08x, dest_addr=0x%08x, logolen=%d\n", 
				inaddr, logolen, logolen);

	//windows draw default 160 180,
	mt65xx_logo_decompress((void*)inaddr, (void*)number_pic_addr, logolen, number_pic_size);

	fill_rect_flow(number_location_rect.left+ number_pic_width*number_position,
						number_location_rect.top,
						number_location_rect.right+number_pic_width*number_position,
						number_location_rect.bottom,number_pic_addr);
            printf("[ChargingAnimation]show_animation_number : left = %d, top = %d, right = %d, bottom = %d\n",  (number_location_rect.left+ number_pic_width*number_position),number_location_rect.top, (number_location_rect.right+number_pic_width*number_position), number_location_rect.bottom);
            printf("[ChargingAnimation]show_animation_number size of number_pic_addr: + %d\n", sizeof(number_pic_addr));

}


void show_animation_line(UINT32 index,UINT32 capacity_grids){
	UINT32 logonum;
	UINT32 logolen;
	UINT32 inaddr;
	UINT32 i;
		
	void  *fb_addr = mt65xx_get_fb_addr();
        UINT32 fb_size = mt65xx_get_fb_size();
        void  *db_addr = mt65xx_get_logo_db_addr();

	unsigned int *pinfo = (unsigned int*)db_addr;
	logonum = pinfo[0];
	
	printf("[ChargingAnimation]show_animation_line :index= %d, logonum = %d\n", index, logonum);
	//ASSERT(index < logonum);

	if(index < logonum)
		logolen = pinfo[3+index] - pinfo[2+index];
	else
		logolen = pinfo[1] - pinfo[2+index];

	inaddr = (unsigned int)db_addr+pinfo[2+index];
	printf("[ChargingAnimation]show_animation_line, in_addr=0x%08x, dest_addr=0x%08x, logolen=%d\n", 
				inaddr, logolen, logolen);

	//windows draw default 160 180,
	mt65xx_logo_decompress((void*)inaddr, (void*)line_pic_addr, logolen, line_pic_size);
        printf("[ChargingAnimation]show_animation_line :line_pic_size= %d, line_pic_addr size = %d\n", line_pic_size, sizeof(line_pic_addr));
        printf("[ChargingAnimation]show_animation_line : left = %d, top = %d, right = %d, bottom = %d\n",  battery_rect.left,capacity_grids,battery_rect.right, battery_rect.bottom);
	fill_line_flow(battery_rect.left, capacity_grids, battery_rect.right, battery_rect.bottom, line_pic_addr);
	
}

int ut(void)
{
	// this is unit test case of boot logo in native space

	// (1) load logo data from nand flash partition
	mt65xx_logo_init();

	// (2) open framebuffer device, prepare related data structure, and fb will be locked
	mt65xx_fb_init();

	// (3) show boot logo
	mt65xx_disp_show_boot_logo();
	sleep(1);

	// (4) show low battery
	mt65xx_disp_show_low_battery();
	sleep(1);

	// (5) show charging animation
	{
		int i = 0;
		int j = 0;
		for(j=0;j<100;j++)
			for(i=0;i<100;i+=25)
			{
				mt65xx_disp_show_battery_capacity(i);
				sleep(1);
			}
	}

	// (6) deinit framebuffer driver and related data structures
	mt65xx_fb_deinit();

	// (7) deinit logo data
	mt65xx_logo_deinit();

	return 0;
}

/*
int main(int argc, char **argv)
{
	return ut();
}
*/

/*
void (void*)bootlogo_fb_init();
void (void*)bootlogo_fb_deinit();
void (void*)bootlogo_show_charging(int capacity);
void (void*)bootlogo_show_boot();
*/

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

void bootlogo_show_charging(int capacity, int cnt)
{
	LOGI("bootlogo_show_charging, %d, %d", capacity, cnt);

	static int bc_offset = 0;
	int bc, base;

	if (get_ov_status()) {
		mt65xx_disp_show_charger_ov_logo();
		return;
	}

	if (cnt == 1) {
		if (capacity < 25)
			bc_offset = 0;
		else if (capacity < 50)
			bc_offset = 25;
		else if (capacity < 75)
			bc_offset = 50;
		else if (capacity < 100)
			bc_offset = 75;
		else
			bc_offset = 100;
	}

	base = (int) (cnt / 5);
	bc = bc_offset + (base*25)%(125-bc_offset);

	LOGI("bootlogo_show_charging, base: %d, bc: %d", base, bc);
	
#ifdef ANIMATION_NEW
        mt65xx_disp_show_battery_capacity(capacity);
#else
        mt65xx_disp_show_battery_capacity(bc);
#endif
	//LOGI("bootlogo_show_charging, done");
}

