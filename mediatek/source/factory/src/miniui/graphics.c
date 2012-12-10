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

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <stdlib.h>
#include <unistd.h>

#include <fcntl.h>
#include <stdio.h>

#include <sys/ioctl.h>
#include <sys/mman.h>
#include <sys/types.h>

#include <linux/fb.h>
#include <linux/kd.h>

#include <pixelflinger/pixelflinger.h>

#include "common.h"
#include "graphics.h"

#include <math.h>

const int GGL_SUBPIXEL_BITS = 4;

#define  TRI_FRACTION_BITS  (GGL_SUBPIXEL_BITS)
#define  TRI_ONE            (1 << TRI_FRACTION_BITS)
#define  TRI_HALF           (1 << (TRI_FRACTION_BITS-1))
#define  TRI_FROM_INT(x)    ((x) << TRI_FRACTION_BITS)

#if defined(FEATURE_FTM_FONT_10x18)
#include "font_10x18.h"
#elif defined(FEATURE_FTM_FONT_8x14)
#include "font_8x14.h"
#elif defined(FEATURE_FTM_FONT_6x10)
#include "font_6x10.h"
#else
#error "font size is not definied"
#endif

typedef struct {
    GGLSurface texture;
    unsigned cwidth;
    unsigned cheight;
    unsigned ascent;
} GRFont;

static GRFont *gr_font = 0;
static GGLContext *gr_context = 0;
static GGLSurface gr_font_texture;
static GGLSurface gr_framebuffer[2];
static GGLSurface gr_mem_surface;
static unsigned gr_active_fb = 0;

static int gr_fb_fd = -1;
static int gr_vt_fd = -1;

static struct fb_var_screeninfo vi;

static int get_framebuffer(GGLSurface *fb)
{
    int fd;
    struct fb_fix_screeninfo fi;
    void *bits;

    fd = open("/dev/graphics/fb0", O_RDWR);
    if (fd < 0) {
        perror("cannot open fb0");
        return -1;
    }

    if (ioctl(fd, FBIOGET_FSCREENINFO, &fi) < 0) {
        perror("failed to get fb0 info");
        close(fd);
        return -1;
    }

    if (ioctl(fd, FBIOGET_VSCREENINFO, &vi) < 0) {
        perror("failed to get fb0 info");
        close(fd);
        return -1;
    }

    bits = mmap(0, fi.smem_len, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
    if (bits == MAP_FAILED) {
        perror("failed to mmap framebuffer");
        close(fd);
        return -1;
    }

    fb->version = sizeof(*fb);
	if(0 == strncmp(MTK_LCM_PHYSICAL_ROTATION, "270", 3)
	||0 == strncmp(MTK_LCM_PHYSICAL_ROTATION, "90", 2)){
	    fb->width = vi.yres;
    	fb->height = vi.xres;
    	fb->stride = vi.yres;
	}
	else{
		fb->width = vi.xres;
    	fb->height = vi.yres;
    	fb->stride = vi.xres;
	}
    fb->data = bits;
    fb->format = GGL_PIXEL_FORMAT_RGB_565;

    fb++;

    fb->version = sizeof(*fb);
	if(0 == strncmp(MTK_LCM_PHYSICAL_ROTATION, "270", 3)
	||0 == strncmp(MTK_LCM_PHYSICAL_ROTATION, "90", 2)){
	    fb->width = vi.yres;
    	fb->height = vi.xres;
    	fb->stride = vi.yres;
	}
	else{
		fb->width = vi.xres;
    	fb->height = vi.yres;
    	fb->stride = vi.xres;
	}

    fb->data = (void*) (((unsigned) bits) + vi.yres * vi.xres_virtual * 2);
    fb->format = GGL_PIXEL_FORMAT_RGB_565;

    return fd;
}

static void get_memory_surface(GGLSurface* ms) {
  ms->version = sizeof(*ms);
  if(0 == strncmp(MTK_LCM_PHYSICAL_ROTATION, "270", 3)
	||0 == strncmp(MTK_LCM_PHYSICAL_ROTATION, "90", 2)){
	ms->width = vi.yres;
  	ms->height = vi.xres;
  	ms->stride = vi.yres;
  }
  else{
	ms->width = vi.xres;
  	ms->height = vi.yres;
  	ms->stride = vi.xres;
  }
  ms->data = malloc(vi.xres * vi.yres * 2);
  ms->format = GGL_PIXEL_FORMAT_RGB_565;
}

static void set_active_framebuffer(unsigned n)
{
    if (n > 1) return;
    vi.yres_virtual = vi.yres * 2;
    vi.yoffset = n * vi.yres;
    vi.bits_per_pixel = 16;
    if (ioctl(gr_fb_fd, FBIOPUT_VSCREENINFO, &vi) < 0) {
        perror("active fb swap failed");
    }
}

void gr_flip(void)
{
    GGLContext *gl = gr_context;
    int j,k;
    unsigned fb_lineLength = vi.xres_virtual * 2;
	unsigned mem_surface_lineLength = vi.xres * 2;
    void *d = NULL;
    void *s = gr_mem_surface.data;
	unsigned int width = vi.xres_virtual;
	unsigned int height = vi.yres;
    /* swap front and back buffers */
    gr_active_fb = (gr_active_fb + 1) & 1;
    d = gr_framebuffer[gr_active_fb].data;
    /* copy data from the in-memory surface to the buffer we're about
     * to make active. */

	if(0 == strncmp(MTK_LCM_PHYSICAL_ROTATION, "270", 3))
	{
		unsigned int l;
		unsigned short *s_temp;
		unsigned short *d_temp;

		s_temp = (unsigned short*)s;
		for (j=0; j<width; j++){
	  		for (k=0, l=height-1; k<height; k++, l--)
	    	{
				d_temp = d + ((width * l + j) << 1);
				*d_temp = *s_temp++;
	    	}
		}
	}
	else if(0 == strncmp(MTK_LCM_PHYSICAL_ROTATION, "90", 2))
	{
		unsigned int l;
		unsigned short *s_temp;
		unsigned short *d_temp;

		s_temp = (unsigned short*)s;
		for (j=width - 1; j>=0; j--){
	  		for (k=0, l=0; k<height; k++, l++)
	    	{
				d_temp = d + ((width * l + j) << 1);
				*d_temp = *s_temp++;
	    	}
		}
	}
	else{
    	for (j = 0; j < vi.yres; ++ j)
    	{
        	memcpy(d, s, mem_surface_lineLength);
			d += fb_lineLength;
			s += mem_surface_lineLength;
    	}
	}
/*
    memcpy(gr_framebuffer[gr_active_fb].data, gr_mem_surface.data,
           vi.xres * vi.yres * 2);
*/
    /* inform the display driver */
    set_active_framebuffer(gr_active_fb);
}

void gr_color(unsigned char r, unsigned char g, unsigned char b, unsigned char a)
{
    GGLContext *gl = gr_context;
    GGLint color[4];
    color[0] = ((r << 8) | r) + 1;
    color[1] = ((g << 8) | g) + 1;
    color[2] = ((b << 8) | b) + 1;
    color[3] = ((a << 8) | a) + 1;
    gl->color4xv(gl, color);
}

int gr_measure(const char *s)
{
    return gr_font->cwidth * strlen(s);
}

int gr_text(int x, int y, const char *s)
{
    GGLContext *gl = gr_context;
    GRFont *font = gr_font;
    unsigned off;

    y -= font->ascent;

    gl->bindTexture(gl, &font->texture);
    gl->texEnvi(gl, GGL_TEXTURE_ENV, GGL_TEXTURE_ENV_MODE, GGL_REPLACE);
    gl->texGeni(gl, GGL_S, GGL_TEXTURE_GEN_MODE, GGL_ONE_TO_ONE);
    gl->texGeni(gl, GGL_T, GGL_TEXTURE_GEN_MODE, GGL_ONE_TO_ONE);
    gl->enable(gl, GGL_TEXTURE_2D);

    while((off = *s++)) {
        off -= 32;
        if (off < 96) {
            gl->texCoord2i(gl, (off * font->cwidth) - x, 0 - y);
            gl->recti(gl, x, y, x + font->cwidth, y + font->cheight);
        }
        x += font->cwidth;
    }

    return x;
}

void gr_fill(int x, int y, int w, int h)
{
    GGLContext *gl = gr_context;
    gl->disable(gl, GGL_TEXTURE_2D);
    gl->recti(gl, x, y, w, h);
}

void gr_point(int x, int y, int radius)
{
    GGLContext *gl = gr_context;
    gl->disable(gl, GGL_TEXTURE_2D);
    
    GGLcoord point[2];
    point[0]= TRI_FROM_INT(x);
    point[1]= TRI_FROM_INT(y);
    GGLcoord gl_radius = TRI_FROM_INT(radius);
    const GGLcoord* v = point;

    gl->pointx(gl, v, gl_radius);
}

void gr_line(int sx, int sy, int dx, int dy, int width)
{
    GGLContext *gl = gr_context;
    gl->disable(gl, GGL_TEXTURE_2D);

    GGLcoord s[2];
    GGLcoord d[2];
    GGLcoord gl_width = TRI_FROM_INT(width);
    const GGLcoord* v0 = s;
    const GGLcoord* v1 = d;   
  
    s[0] = TRI_FROM_INT(sx);
    s[1] = TRI_FROM_INT(sy);
    d[0] = TRI_FROM_INT(dx);
    d[1] = TRI_FROM_INT(dy);
   
    gl->linex(gl, v0, v1, gl_width);
}

void gr_circle(int x, int y, int radius)
{
    GGLContext *gl = gr_context;
    gl->disable(gl, GGL_TEXTURE_2D);
#if 0    
    GGLcoord point[2];
    point[0]= TRI_FROM_INT(x);
    point[1]= TRI_FROM_INT(y);    

    const GGLcoord* v = &point;
#endif    

    GGLcoord s[2];
    GGLcoord d[2];
    const GGLcoord* v0 = s;
    const GGLcoord* v1 = d;    

    int i = 0, j = 0;
    int r = radius;
    int x1 = 0, y1 = 0, x2 = 0, y2 = 0, x3 = 0, y3 = 0, x4 = 0, y4 = 0;
    
    for(i=x-r; i<=x+r; i++) 
    {
        x1=x2; y1=y2; x3=x4; y3=y4;
        x2 = i;
        y2 = (int)((float)y+r*sin(acos(((float)i-x)/r)));
        x4 = i;
        y4 = (int)((float)y-r*sin(acos(((float)i-x)/r)));      
        if(j==0) 
        { 
            j=1; x1=x2; y1=y2; x3=x4; y3=y4; 
        }
#if 0        
        point[0]= TRI_FROM_INT(x1);
        point[1]= TRI_FROM_INT(y1);
        gl->pointx(gl, v, TRI_ONE);
        point[0]= TRI_FROM_INT(x2);
        point[1]= TRI_FROM_INT(y2);
        gl->pointx(gl, v, TRI_ONE);
        point[0]= TRI_FROM_INT(x3);
        point[1]= TRI_FROM_INT(y3);
        gl->pointx(gl, v, TRI_ONE);
        point[0]= TRI_FROM_INT(x4);
        point[1]= TRI_FROM_INT(y4);
        gl->pointx(gl, v, TRI_ONE);        
#endif
        s[0] = TRI_FROM_INT(x1);
        s[1] = TRI_FROM_INT(y1);
        d[0] = TRI_FROM_INT(x2);
        d[1] = TRI_FROM_INT(y2);        
        gl->linex(gl, v0, v1, TRI_ONE);
        s[0] = TRI_FROM_INT(x3);
        s[1] = TRI_FROM_INT(y3);
        d[0] = TRI_FROM_INT(x4);
        d[1] = TRI_FROM_INT(y4);        
        gl->linex(gl, v0, v1, TRI_ONE);            
    }
}

void gr_blit(gr_surface source, int sx, int sy, int w, int h, int dx, int dy) {
    if (gr_context == NULL) {
        return;
    }
    GGLContext *gl = gr_context;

    gl->bindTexture(gl, (GGLSurface*) source);
    gl->texEnvi(gl, GGL_TEXTURE_ENV, GGL_TEXTURE_ENV_MODE, GGL_REPLACE);
    gl->texGeni(gl, GGL_S, GGL_TEXTURE_GEN_MODE, GGL_ONE_TO_ONE);
    gl->texGeni(gl, GGL_T, GGL_TEXTURE_GEN_MODE, GGL_ONE_TO_ONE);
    gl->enable(gl, GGL_TEXTURE_2D);
    gl->texCoord2i(gl, sx - dx, sy - dy);
    gl->recti(gl, dx, dy, dx + w, dy + h);
}

unsigned int gr_get_width(gr_surface surface) {
    if (surface == NULL) {
        return 0;
    }
    return ((GGLSurface*) surface)->width;
}

unsigned int gr_get_height(gr_surface surface) {
    if (surface == NULL) {
        return 0;
    }
    return ((GGLSurface*) surface)->height;
}

static void gr_init_font(void)
{
    GGLSurface *ftex;
    unsigned char *bits, *rle;
    unsigned char *in, data;

    gr_font = calloc(sizeof(*gr_font), 1);
    ftex = &gr_font->texture;

    bits = malloc(font.width * font.height);

    ftex->version = sizeof(*ftex);
    ftex->width = font.width;
    ftex->height = font.height;
    ftex->stride = font.width;
    ftex->data = (void*) bits;
    ftex->format = GGL_PIXEL_FORMAT_A_8;

    in = font.rundata;
    while((data = *in++)) {
        memset(bits, (data & 0x80) ? 255 : 0, data & 0x7f);
        bits += (data & 0x7f);
    }

    gr_font->cwidth = font.cwidth;
    gr_font->cheight = font.cheight;
    gr_font->ascent = font.cheight - 2;
}

int gr_init(void)
{
    gglInit(&gr_context);
    GGLContext *gl = gr_context;

    gr_init_font();
    gr_vt_fd = open("/dev/tty0", O_RDWR | O_SYNC);
    if (gr_vt_fd < 0) {
        // This is non-fatal; post-Cupcake kernels don't have tty0.
        perror("can't open /dev/tty0");
    } else if (ioctl(gr_vt_fd, KDSETMODE, (void*) KD_GRAPHICS)) {
        // However, if we do open tty0, we expect the ioctl to work.
        perror("failed KDSETMODE to KD_GRAPHICS on tty0");
        gr_exit();
        return -1;
    }

    gr_fb_fd = get_framebuffer(gr_framebuffer);
    if (gr_fb_fd < 0) {
        gr_exit();
        return -1;
    }

    get_memory_surface(&gr_mem_surface);

    fprintf(stderr, "framebuffer: fd %d (%d x %d)\n",
            gr_fb_fd, gr_framebuffer[0].width, gr_framebuffer[0].height);

        /* start with 0 as front (displayed) and 1 as back (drawing) */
    gr_active_fb = 0;
    set_active_framebuffer(0);
    gl->colorBuffer(gl, &gr_mem_surface);


    gl->activeTexture(gl, 0);
    gl->enable(gl, GGL_BLEND);
    gl->blendFunc(gl, GGL_SRC_ALPHA, GGL_ONE_MINUS_SRC_ALPHA);

    return 0;
}

void gr_exit(void)
{
    close(gr_fb_fd);
    gr_fb_fd = -1;

    free(gr_mem_surface.data);

    ioctl(gr_vt_fd, KDSETMODE, (void*) KD_TEXT);
    close(gr_vt_fd);
    gr_vt_fd = -1;
}

int gr_fb_width(void)
{
    return gr_framebuffer[0].width;
}

int gr_fb_height(void)
{
    return gr_framebuffer[0].height;
}

gr_pixel *gr_fb_data(void)
{
    return (unsigned short *) gr_mem_surface.data;
}
