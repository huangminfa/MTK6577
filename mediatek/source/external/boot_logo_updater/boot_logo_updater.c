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
#include <stdlib.h>
#include <ctype.h>

#include <linux/fb.h>
#include <sys/ioctl.h>
#include <sys/mman.h>

#include <cutils/properties.h>
#include <utils/Log.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/mount.h>
#include <mtd/mtd-abi.h>
#include<unistd.h>


char const * const fb_dev_node_paths[] = {
        "/dev/graphics/fb%u",
        "/dev/fb%u",
        0
};
extern void bootlogo_show_boot();
extern void bootlogo_fb_init();
extern void bootlogo_fb_deinit();
const char LOGO_PATH[] = "/system/media/images/boot_logo";
const char LOGO_PATH1[] = "/system/media/images/boot_logo1";
const char LOGO_C_DEV[] = "/dev/logo";


#define RGB565_TO_ARGB8888(x)   \
    ((((x) &   0x1F) << 3) |    \
     (((x) &  0x7E0) << 5) |    \
     (((x) & 0xF800) << 8) |    \
     (0xFF << 24)) // opaque

#ifdef LOG_TAG
#undef LOG_TAG
#define LOG_TAG "BootLogoUpdater"
#endif


#define BOOT_MODE_PATH "/sys/class/BOOT/BOOT/boot/boot_mode"
#define LCD_BACKLIGHT_PATH "/sys/class/leds/lcd-backlight/brightness"
#define BOOT_REASON_SYS_PROPERTY "sys.boot.reason"
#define BOOT_PACKAGE_SYS_PROPERTY "persist.sys.bootpackage"

#define FLAG_OFFSET 515
static void  setFlag(int flag)
{
	int fd;
	int iWriteSize;
	int result;
	int ofset=0;
	char *tempBuf=NULL;
	struct mtd_info_user info;
	struct erase_info_user erase_info;
	fd=open(LOGO_C_DEV,O_RDWR);
	if(fd<0)
	{
		return;
	}

	result=ioctl(fd,MEMGETINFO,&info);
	if(result<0)
	{
		goto end;
	}

	erase_info.start= (FLAG_OFFSET/info.erasesize)*info.erasesize;
	erase_info.length=info.erasesize;
	iWriteSize=erase_info.length;
	ofset=FLAG_OFFSET-erase_info.start;
	fprintf(stderr, "writesize=%d,erasesize=%d,start=%d\n",info.writesize,info.erasesize,erase_info.start);
	tempBuf=(int*)malloc(iWriteSize);
	if(tempBuf==NULL)
	{
		fprintf(stderr,"0001: malloc faile\n");
		goto end;
	}
	result=lseek(fd,erase_info.start,SEEK_SET);
	if(result!=(erase_info.start))
	{
		fprintf(stderr,"0002: seek faile\n");
		goto end;
	}
	result=read(fd,tempBuf,iWriteSize);
	if(result!=iWriteSize)
	{
		fprintf(stderr,"0003: read faile\n");
		goto end;
	}
	if(flag)
	{
		tempBuf[ofset]=1;
	}
	else
	{
		tempBuf[ofset]=0;
	}
	result=ioctl(fd, MEMERASE, &erase_info);
	if(result<0)
	{
		fprintf(stderr,"eraser error\n");
		goto end;
	}
	printf("eraser ok\n");

	result=lseek(fd,erase_info.start,SEEK_SET);
	if(result!=(erase_info.start))
	{
		fprintf(stderr,"0004: seek faile\n");
		goto end;
	}
	result=write(fd,tempBuf,iWriteSize);
	if(result!=iWriteSize)
	{
		fprintf(stderr,"0005: write faile\n");
		goto end;
	}
end:
	if(tempBuf!=NULL)
	{
		free(tempBuf);
	}
	close(fd);
}

int changeLogo()
{
	int fd = 0;
	char flag = 0;
	int result;
	fd = open(LOGO_C_DEV, O_RDONLY);
	result=lseek(fd,FLAG_OFFSET,SEEK_SET);
	if(result!=FLAG_OFFSET)
	{
		fprintf(stderr,"1001: seek faile\n");
		goto end;
	}
	result = read(fd,&flag,1);
	if(result!=1)
	{
		fprintf(stderr,"1002: read faile\n");
		goto end;
	}
	close(fd);
	if(flag)
	{
		return 1;
	}
	else
	{
		return 0;
	}

end:
	close(fd);
	return 0;
}

/*
 * return value:
 * 0: normal
 * 1: Alarm boot
 * 2: Schedule power-on
 */
int updateBootReason() {
    int fd;
    size_t s;
    char boot_mode;
    char boot_reason; // 0: normal boot, 1: alarm boot
    char propVal[PROPERTY_VALUE_MAX];
    int ret = 0;

    fd = open(BOOT_MODE_PATH, O_RDWR);
    if (fd < 0) {
        LOGE("fail to open: %s\n", BOOT_MODE_PATH);
        boot_reason = '0';
    } else {
        s = read(fd, (void *)&boot_mode, sizeof(boot_mode));
        close(fd);
    
        if(s <= 0) {
            LOGE("can't read the boot_mode");
            boot_reason = '0';
        } else {
            // ALARM_BOOT = 7 
            LOGI("boot_mode = %c", boot_mode);
            if ( boot_mode == '7' ) {
		//add for encrypt mode to avoid invoke the power-off alarm
                 property_get("vold.decrypt", propVal, "");
                if (!strcmp(propVal, "") || !strcmp(propVal, "trigger_restart_framework")) {
		    boot_reason = '1';
        	    ret = 1;
                } else {
		    boot_reason = '0';
		    ret = 2;
                }
            } else {
                // schedule on/off, normal boot
                property_get(BOOT_PACKAGE_SYS_PROPERTY, propVal, "0");
                int package = atoi(propVal);
                LOGI("boot package = %d", package);
                if ( package != 1 ) {
                    // it's not triggered by Desk Clock, change to normal boot
                    ret = 2;
                }
                boot_reason = '0';
            }
        }
    }
    sprintf(propVal, "%c", boot_reason);
    LOGI("update boot reason = %s, ret = %d", propVal, ret);
    property_set(BOOT_REASON_SYS_PROPERTY, propVal);
    return ret;
}

void showBootLogo() {
    bootlogo_fb_init();
    bootlogo_show_boot();
    bootlogo_fb_deinit();
    sleep(1);
}

int write_to_file(const char* path, const char* buf, int size) {

    if (!path) {
        LOGE("path is null!");
        return 0;
    }

    int fd = open(path, O_RDWR);
    if (fd == -1) {
        LOGE("Could not open '%s'\n", path);
        return 0;
    }

    int count = write(fd, buf, size); 
    if (count != size) {
        LOGE("write file (%s) fail, count: %d\n", path, count);
        return 0;
    }
    close(fd);
    return count;
}

void set_int_value(const char * path, const int value)
{
	char buf[32];
	sprintf(buf, "%d", value);

	write_to_file(path, buf, strlen(buf));
}


int main(int argc,char** argv)
{
    int fb = -1;
    int fd = -1;
    size_t fbsize = 0;
    ssize_t rdsize = 0;
    char name[64];
    struct fb_var_screeninfo vinfo;
    struct fb_fix_screeninfo finfo;
    void *fbbuf = NULL;

    size_t rgb565_logo_size = 0;
    unsigned short *rgb565_logo = NULL;

    unsigned int i = 0;

	if(argc>1)
	{
	setgid(0);
	setuid(0);
	chmod(LOGO_C_DEV,0777);
		if(argv[1][0]=='0')
		{
			setFlag(0);
		}
		else if(argv[1][0]=='1')
		{
			setFlag(1);
		}
		else
		{
			if(changeLogo())
			{
				printf("1");
			}
			else
			{
				printf("0");
			}
		}
		return 0;
	}
    LOGI("starting boot_logo_updater ...");
    
    int ret = updateBootReason();
    if (ret == 1) {
        LOGI("skip the boot logo!");
        set_int_value(LCD_BACKLIGHT_PATH, 120);
        return 0;    
    } else if (ret == 2) {
        LOGI("schedule on");     
    }

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

    fbsize = vinfo.xres_virtual * vinfo.yres * (vinfo.bits_per_pixel / 8);
    rgb565_logo_size = vinfo.xres * vinfo.yres * 2;

    // (3) open logo file
    fprintf(stderr, "check logo flag\n");
		if(changeLogo())
		{
    fprintf(stderr, "check logo flag =1\n");
    	fd = open(LOGO_PATH1, O_RDONLY);
		}
		else
		{
    fprintf(stderr, "check logo flag =0\n");
    	fd = open(LOGO_PATH, O_RDONLY);
		}
    if (fd < 0) {
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
        goto done;
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
					d = fbbuf + ((vinfo.xres_virtual * l + j) << 2);
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
					d = fbbuf + ((vinfo.xres_virtual * l + j) << 2);
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
	    		for(k = vinfo.xres; k < vinfo.xres_virtual; ++ k)
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

    printf("[boot_logo_updater] update boot logo successfully!\n");

done:    
    if (rgb565_logo) free(rgb565_logo);
    if (fbbuf) munmap(fbbuf, fbsize);
    if (fd >= 0) close(fd);
    if (fb >= 0) close(fb);

    return 0;
}
