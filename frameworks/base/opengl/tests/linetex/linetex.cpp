/*
**
** Copyright 2006, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License"); 
** you may not use this file except in compliance with the License. 
** You may obtain a copy of the License at 
**
**     http://www.apache.org/licenses/LICENSE-2.0 
**
** Unless required by applicable law or agreed to in writing, software 
** distributed under the License is distributed on an "AS IS" BASIS, 
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
** See the License for the specific language governing permissions and 
** limitations under the License.
*/

#define LOG_TAG "fillrate"

#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>

#include <EGL/egl.h>
#include <GLES/gl.h>
#include <GLES/glext.h>

#include <utils/StopWatch.h>
#include <ui/FramebufferNativeWindow.h>
#include <ui/EGLUtils.h>

#include <surfaceflinger/Surface.h>
#include <surfaceflinger/SurfaceComposerClient.h>

using namespace android;

int main(int argc, char** argv)
{
    EGLint configAttribs[] = {
         EGL_DEPTH_SIZE, 0,
         EGL_NONE
     };
     
     EGLint majorVersion;
     EGLint minorVersion;
     EGLContext context;
     EGLConfig config;
     EGLSurface surface;
     EGLint w, h;
     EGLDisplay dpy;

     // init android native window surface
     sp<SurfaceComposerClient> client;
     sp<SurfaceControl>        surfaceCtl;
     sp<Surface>               native_surface;
     client = new SurfaceComposerClient();
     surfaceCtl = client->createSurface(String8("linetex"), 0, 480, 800, PIXEL_FORMAT_RGB_565, 0);
     client->openGlobalTransaction();
     surfaceCtl->setLayer(100000);
     client->closeGlobalTransaction();
     native_surface = surfaceCtl->getSurface();
     native_window_set_buffers_format(native_surface.get(), PIXEL_FORMAT_RGB_565);
     //EGLNativeWindowType window = android_createDisplaySurface();
     
     dpy = eglGetDisplay(EGL_DEFAULT_DISPLAY);
     eglInitialize(dpy, &majorVersion, &minorVersion);
          
     //status_t err = EGLUtils::selectConfigForNativeWindow(
     //        dpy, configAttribs, window, &config);
     status_t err = EGLUtils::selectConfigForNativeWindow(
             dpy, configAttribs, native_surface.get(), &config);
     if (err) {
         fprintf(stderr, "couldn't find an EGLConfig matching the screen format\n");
         return 0;
     }

     //surface = eglCreateWindowSurface(dpy, config, window, NULL);
     surface = eglCreateWindowSurface(dpy, config, native_surface.get(), NULL);
     context = eglCreateContext(dpy, config, NULL, NULL);
     eglMakeCurrent(dpy, surface, surface, context);   
     eglQuerySurface(dpy, surface, EGL_WIDTH, &w);
     eglQuerySurface(dpy, surface, EGL_HEIGHT, &h);
     
     printf("w=%d, h=%d\n", w, h);
     
     glBindTexture(GL_TEXTURE_2D, 0);
     glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
     glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
     glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
     glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
     glTexEnvx(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
     glDisable(GL_DITHER);
     glDisable(GL_BLEND);
     glEnable(GL_TEXTURE_2D);
     glColor4f(1,1,1,1);


     // default pack-alignment is 4
     const uint16_t t16[64] = { 0xFFFF, 0, 0xF800, 0, 0x07E0, 0, 0x001F, 0 };

     const GLfloat vertices[4][2] = {
             { w/2,  0 },
             { w/2,  h }
     };

     const GLfloat texCoords[4][2] = {
             { 0,  0 },
             { 1,  1 }
     };

     glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 1, 4, 0, GL_RGB, GL_UNSIGNED_SHORT_5_6_5, t16);

     glViewport(0, 0, w, h);
     glMatrixMode(GL_PROJECTION);
     glLoadIdentity();
     glOrthof(0, w, 0, h, 0, 1);

     glEnableClientState(GL_VERTEX_ARRAY);
     glEnableClientState(GL_TEXTURE_COORD_ARRAY);
     glVertexPointer(2, GL_FLOAT, 0, vertices);
     glTexCoordPointer(2, GL_FLOAT, 0, texCoords);

     glClearColor(0,0,0,0);
     glClear(GL_COLOR_BUFFER_BIT);
     glDrawArrays(GL_LINES, 0, 2);
     eglSwapBuffers(dpy, surface);

     usleep(5*1000000);

     eglTerminate(dpy);
     
     return 0;
}
