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

#include <stdio.h>
#include <string.h>
#include <fcntl.h>

#include <linux/fb.h>
#include <sys/ioctl.h>
#include <sys/mman.h>
#include <sys/reboot.h>

char const * const fb_dev_node_paths[] = {
        "/dev/graphics/fb%u",
        "/dev/fb%u",
        0
};

const char LOGO_PATH[] = "/system/media/images/boot_logo";

#define RGB565_TO_ARGB8888(x)   \
    ((((x) &   0x1F) << 3) |    \
     (((x) &  0x7E0) << 5) |    \
     (((x) & 0xF800) << 8) |    \
     (0xFF << 24)) // opaque

//int main(void)
void boot_logo_updater()
{
    int fb = -1;
    int fd = -1;
    size_t fbsize = 0;
    ssize_t rdsize = 0;
	unsigned int x_virtual = 0;
    char name[64];
    struct fb_var_screeninfo vinfo;
    struct fb_fix_screeninfo finfo;
    void *fbbuf = NULL;

    size_t rgb565_logo_size = 0;
    unsigned short *rgb565_logo = NULL;

    unsigned int i = 0;

    // (1) open framebuffer driver
    
    while ((fb == -1) && fb_dev_node_paths[i]) {
        snprintf(name, 64, fb_dev_node_paths[i], 0);
        fb = open(name, O_RDWR, 0);
        i++;
    }

    if (fb < 0)
        return -1;

    // (2) get screen info

    if (ioctl(fb, FBIOGET_VSCREENINFO, &vinfo) < 0) {
        fprintf(stderr, "ioctl FBIOGET_VSCREENINFO failed\n");
        goto done;
    }

    if (ioctl(fb, FBIOGET_FSCREENINFO, &finfo) < 0) {
        fprintf(stderr, "ioctl FBIOGET_FSCREENINFO failed\n");
        goto done;
    }

    fbsize = finfo.line_length * vinfo.yres;
	x_virtual = finfo.line_length / (vinfo.bits_per_pixel / 8);
    rgb565_logo_size = vinfo.xres * vinfo.yres * 2;

    // (3) open logo file

    if ((fd = open(LOGO_PATH, O_RDONLY)) < 0) {
        fprintf(stderr, "failed to open logo file: %s\n", LOGO_PATH);
        goto done;
    }

    // (4) map framebuffer

    fbbuf = mmap(0, fbsize, PROT_READ|PROT_WRITE, MAP_SHARED, fb, 0);
    if (fbbuf == (void *)-1) {
        fprintf(stderr, "failed to map framebuffer\n");
        fbbuf = NULL;
        goto done;
    }

    // (5) copy the 2nd logo to frmaebuffer

    rgb565_logo = malloc(rgb565_logo_size);
    if (!rgb565_logo) {
        fprintf(stderr, "allocate %d bytes memory for boot logo failed\n",
                rgb565_logo_size);
        //goto done;
	reboot(LINUX_REBOOT_CMD_RESTART);
    }

    rdsize = read(fd, rgb565_logo, rgb565_logo_size);

    if (rdsize < (ssize_t)rgb565_logo_size) {
        fprintf(stderr, "logo file size: %ld bytes, "
                        "while expected size: %d bytes\n",
                        rdsize, rgb565_logo_size);
        goto done;
    }

    if (16 == vinfo.bits_per_pixel) // RGB565
    {
//        memcpy(fbbuf, rgb565_logo, rgb565_logo_size);
		unsigned short *s = rgb565_logo;
        unsigned short *d = fbbuf;
		unsigned j = 0;
		for (j = 0; j < vinfo.yres; ++ j)
        {
            memcpy((void*)d, (void*)s, vinfo.xres * 2);
	    	d += finfo.line_length;
	    	s += vinfo.xres * 2;
        }
    }
    else if (32 == vinfo.bits_per_pixel) // ARGB8888
    {
        unsigned short src_rgb565 = 0;

        unsigned short *s = rgb565_logo;
        unsigned int   *d = fbbuf;
        int j = 0;
		int k = 0;
/*
        for (j = 0; j < vinfo.xres * vinfo.yres; ++ j)
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
					d = fbbuf + ((x_virtual * l + j) << 2);
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
					d = fbbuf + ((x_virtual * l + j) << 2);
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
        fprintf(stderr, "unknown format bpp: %d\n", vinfo.bits_per_pixel);
        goto done;
    }

    // (6) flip to front buffer immediately

    vinfo.yoffset = 0;
    vinfo.activate |= (FB_ACTIVATE_FORCE | FB_ACTIVATE_NOW);

    if (ioctl(fb, FBIOPUT_VSCREENINFO, &vinfo) < 0) {
        fprintf(stderr, "ioctl FBIOPUT_VSCREENINFO flip failed\n");
        goto done;
    }

done:
    if (rgb565_logo) free(rgb565_logo);
    if (fbbuf) munmap(fbbuf, fbsize);
    if (fd >= 0) close(fd);
    if (fb >= 0) close(fb);

    return;
}
