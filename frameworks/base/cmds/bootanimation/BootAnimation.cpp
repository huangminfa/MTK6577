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

#define LOG_TAG "BootAnimation"

#include <stdint.h>
#include <sys/types.h>
#include <math.h>
#include <fcntl.h>
#include <utils/misc.h>
#include <signal.h>

#include <cutils/properties.h>

#include <binder/IPCThreadState.h>
#include <utils/threads.h>
#include <utils/Atomic.h>
#include <utils/Errors.h>
#include <utils/Log.h>
#include <utils/AssetManager.h>

#include <ui/PixelFormat.h>
#include <ui/Rect.h>
#include <ui/Region.h>
#include <ui/DisplayInfo.h>
#include <ui/FramebufferNativeWindow.h>

#include <surfaceflinger/ISurfaceComposer.h>
#include <surfaceflinger/ISurfaceComposerClient.h>

#include <core/SkBitmap.h>
#include <core/SkStream.h>
#include <images/SkImageDecoder.h>

#include <GLES/gl.h>
#include <GLES/glext.h>
#include <EGL/eglext.h>

#include <media/mediaplayer.h>
#include <media/MediaPlayerInterface.h>
#include <system/audio.h>

#include "BootAnimation.h"

#define USER_BOOTANIMATION_FILE "/data/local/bootanimation.zip"
#define SYSTEM_BOOTANIMATION_FILE "/system/media/bootanimation.zip"
#define SYSTEM_ENCRYPTED_BOOTANIMATION_FILE "/system/media/bootanimation-encrypted.zip"

namespace android {

// ---------------------------------------------------------------------------

BootAnimation::BootAnimation() : Thread(false)
{
    mSession = new SurfaceComposerClient();
}

BootAnimation::BootAnimation(bool bSetBootOrShutDown, bool bSetPlayMP3,bool bSetRotated) : Thread(false)
{
	mSession = new SurfaceComposerClient();
	//force portrait for both boot up and shut down  ALPS00120158
//	mSession->setOrientation(0, ISurfaceComposer::eOrientationDefault,0);

	bBootOrShutDown = bSetBootOrShutDown;
	bShutRotate = bSetRotated;
	bPlayMP3 = bSetPlayMP3;
}

BootAnimation::~BootAnimation() {
}

void BootAnimation::onFirstRef() {
    status_t err = mSession->linkToComposerDeath(this);
    if(err != 0){
        XLOGE("linkToComposerDeath failed (%s) ", strerror(-err));
    }
    if (err == NO_ERROR) {
        run("BootAnimation", PRIORITY_DISPLAY);
    }
}

sp<SurfaceComposerClient> BootAnimation::session() const {
    return mSession;
}


void BootAnimation::binderDied(const wp<IBinder>& who)
{
    // woah, surfaceflinger died!
    XLOGD("SurfaceFlinger died, exiting...");

    // calling requestExit() is not enough here because the Surface code
    // might be blocked on a condition variable that will never be updated.
    kill( getpid(), SIGKILL );
    requestExit();
}

status_t BootAnimation::initTexture(Texture* texture, AssetManager& assets,
        const char* name) {
    Asset* asset = assets.open(name, Asset::ACCESS_BUFFER);
    if (!asset)
        return NO_INIT;
    SkBitmap bitmap;
    SkImageDecoder::DecodeMemory(asset->getBuffer(false), asset->getLength(),
            &bitmap, SkBitmap::kNo_Config, SkImageDecoder::kDecodePixels_Mode);
    asset->close();
    delete asset;

    // ensure we can call getPixels(). No need to call unlock, since the
    // bitmap will go out of scope when we return from this method.
    bitmap.lockPixels();

    const int w = bitmap.width();
    const int h = bitmap.height();
    const void* p = bitmap.getPixels();

    GLint crop[4] = { 0, h, w, -h };
    texture->w = w;
    texture->h = h;

    int tw = 1 << (31 - __builtin_clz(w));
    int th = 1 << (31 - __builtin_clz(h));
    if (tw < w) tw <<= 1;
    if (th < h) th <<= 1;


    glGenTextures(1, &texture->name);
    glBindTexture(GL_TEXTURE_2D, texture->name);

    switch (bitmap.getConfig()) {
        case SkBitmap::kA8_Config:
            glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, w, h, 0, GL_ALPHA,
                    GL_UNSIGNED_BYTE, p);
            break;
        case SkBitmap::kARGB_4444_Config:
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w, h, 0, GL_RGBA,
                    GL_UNSIGNED_SHORT_4_4_4_4, p);
            break;
        case SkBitmap::kARGB_8888_Config:
#ifdef USES_ARGB_ORDER
            if (tw != w || th != h) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_BGRA, tw, th, 0, GL_BGRA,
                        GL_UNSIGNED_BYTE, 0);
                glTexSubImage2D(GL_TEXTURE_2D, 0,
                        0, 0, w, h, GL_BGRA, GL_UNSIGNED_BYTE, p);
            } else {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_BGRA, w, h, 0, GL_BGRA,
                    GL_UNSIGNED_BYTE, p);
            }
#else
            if (tw != w || th != h) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, tw, th, 0, GL_RGBA,
                        GL_UNSIGNED_BYTE, 0);
                glTexSubImage2D(GL_TEXTURE_2D, 0,
                        0, 0, w, h, GL_RGBA, GL_UNSIGNED_BYTE, p);
            } else {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w, h, 0, GL_RGBA,
                    GL_UNSIGNED_BYTE, p);
            }
#endif
            break;
        case SkBitmap::kRGB_565_Config:
            if (tw != w || th != h) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, tw, th, 0, GL_RGB,
                        GL_UNSIGNED_SHORT_5_6_5, 0);
                glTexSubImage2D(GL_TEXTURE_2D, 0,
                        0, 0, w, h, GL_RGB, GL_UNSIGNED_SHORT_5_6_5, p);
            } else {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, w, h, 0, GL_RGB,
                    GL_UNSIGNED_SHORT_5_6_5, p);
            }
            break;
        default:
            break;
    }

    glTexParameteriv(GL_TEXTURE_2D, GL_TEXTURE_CROP_RECT_OES, crop);
    glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
    return NO_ERROR;
}

status_t BootAnimation::initTexture(void* buffer, size_t len)
{
    //StopWatch watch("blah");

    SkBitmap bitmap;
    SkMemoryStream  stream(buffer, len);
    SkImageDecoder* codec = SkImageDecoder::Factory(&stream);
    codec->setDitherImage(false);
    if (codec) {
        codec->decode(&stream, &bitmap,
                SkBitmap::kRGB_565_Config,
            SkImageDecoder::kDecodePixels_Mode);
        delete codec;
    }

    // ensure we can call getPixels(). No need to call unlock, since the
    // bitmap will go out of scope when we return from this method.
    bitmap.lockPixels();

    const int w = bitmap.width();
    const int h = bitmap.height();
    const void* p = bitmap.getPixels();

    GLint crop[4] = { 0, h, w, -h };
    int tw = 1 << (31 - __builtin_clz(w));
    int th = 1 << (31 - __builtin_clz(h));
    if (tw < w) tw <<= 1;
    if (th < h) th <<= 1;

    switch (bitmap.getConfig()) {
        case SkBitmap::kARGB_8888_Config:
#ifdef USES_ARGB_ORDER
            if (tw != w || th != h) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_BGRA, tw, th, 0, GL_BGRA,
                        GL_UNSIGNED_BYTE, 0);
                glTexSubImage2D(GL_TEXTURE_2D, 0,
                        0, 0, w, h, GL_BGRA, GL_UNSIGNED_BYTE, p);
            } else {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_BGRA, tw, th, 0, GL_BGRA,
                        GL_UNSIGNED_BYTE, p);
            }
#else
            if (tw != w || th != h) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, tw, th, 0, GL_RGBA,
                        GL_UNSIGNED_BYTE, 0);
                glTexSubImage2D(GL_TEXTURE_2D, 0,
                        0, 0, w, h, GL_RGBA, GL_UNSIGNED_BYTE, p);
            } else {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, tw, th, 0, GL_RGBA,
                        GL_UNSIGNED_BYTE, p);
            }
#endif
            break;

        case SkBitmap::kRGB_565_Config:
            if (tw != w || th != h) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, tw, th, 0, GL_RGB,
                        GL_UNSIGNED_SHORT_5_6_5, 0);
                glTexSubImage2D(GL_TEXTURE_2D, 0,
                        0, 0, w, h, GL_RGB, GL_UNSIGNED_SHORT_5_6_5, p);
            } else {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, tw, th, 0, GL_RGB,
                        GL_UNSIGNED_SHORT_5_6_5, p);
            }
            break;
        default:
            break;
    }

    glTexParameteriv(GL_TEXTURE_2D, GL_TEXTURE_CROP_RECT_OES, crop);

    return NO_ERROR;
}

status_t BootAnimation::readyToRun() {
    mAssets.addDefaultAssets();

    DisplayInfo dinfo;
    XLOGD("readyToRun getDisplayInfo()...");
    status_t status = session()->getDisplayInfo(0, &dinfo);
    if (status)
        return -1;

    // create the native surface
    XLOGD("***** bootanimation createSurface(dinfo.w %d dinfo.h %d) *****",dinfo.w,dinfo.h);
    int width = 0;
    int height = 0;

    // Checking the rotation before entering boot animation
    switch (dinfo.orientation) {
        case ISurfaceComposer::eOrientationDefault:
        case ISurfaceComposer::eOrientation180:
            width = dinfo.w;
            height = dinfo.h;
            break;
        
        default:
            width = dinfo.h;
            height = dinfo.w;
            break;
    }

    /*
    if(!bBootOrShutDown) {
        if(width > height) {
            int nTemp = width;
            width = height;
            height = nTemp;
        }        
    }
    */

    sp<SurfaceControl> control = session()->createSurface(
            0, width, height, PIXEL_FORMAT_RGB_565);

    // <--- add by Ryan
    // draw a black screen first to avoid landscape ghost image
    sp<Surface> _surface = control->getSurface();

    Surface::SurfaceInfo info;
    _surface->lock(&info);
    ssize_t bpr = info.s * bytesPerPixel(info.format);
    memset((uint16_t*)info.bits, 0x00, bpr*info.h);
    _surface->unlockAndPost();

    // disconnect the original api type
    ANativeWindow* window = _surface.get();
    native_window_api_disconnect(window, NATIVE_WINDOW_API_CPU);
    // add by Ryan --->
    
  //  if(!bBootOrShutDown) 
    {
        mSession->setOrientation(0, ISurfaceComposer::eOrientationDefault,0);
    }
	
    SurfaceComposerClient::openGlobalTransaction();
   
    status = control->setLayer(0x40000000);
    if (status) {
        XLOGE("control->setLayer(0x40000000) return status %d",status);
    }
    SurfaceComposerClient::closeGlobalTransaction();
   
    sp<Surface> s = control->getSurface();

    // initialize opengl and egl
    XLOGD("control->getSurface()");
    const EGLint attribs[] = {
            EGL_RED_SIZE,   8,
            EGL_GREEN_SIZE, 8,
            EGL_BLUE_SIZE,  8,
            EGL_DEPTH_SIZE, 0,
            EGL_NONE
    };
    EGLint w, h, dummy;
    EGLint numConfigs;
    EGLConfig config;
    EGLSurface surface;
    EGLContext context;

    EGLDisplay display = eglGetDisplay(EGL_DEFAULT_DISPLAY);

    XLOGD("initialize opengl and egl");
    EGLBoolean eglret = eglInitialize(display, 0, 0);
    if (eglret == EGL_FALSE) {
        XLOGE("eglInitialize(display, 0, 0) return EGL_FALSE");
    }
    eglChooseConfig(display, attribs, &config, 1, &numConfigs);
    surface = eglCreateWindowSurface(display, config, s.get(), NULL);
    context = eglCreateContext(display, config, NULL, NULL);
    eglret = eglQuerySurface(display, surface, EGL_WIDTH, &w);
    if (eglret == EGL_FALSE) {
        XLOGE("eglQuerySurface(display, surface, EGL_WIDTH, &w) return EGL_FALSE");
    }
    eglret = eglQuerySurface(display, surface, EGL_HEIGHT, &h);
    if (eglret == EGL_FALSE) {
        XLOGE("eglQuerySurface(display, surface, EGL_HEIGHT, &h) return EGL_FALSE");
    }

    if (eglMakeCurrent(display, surface, surface, context) == EGL_FALSE){
        XLOGE("eglMakeCurrent(display, surface, surface, context) return EGL_FALSE");
        return NO_INIT;
    }
    mDisplay = display;
    mContext = context;
    mSurface = surface;
    mWidth = w;
    mHeight = h;
    mFlingerSurfaceControl = control;
    mFlingerSurface = s;
    XLOGV("open bootanimation.zip");
    
	if(changeLogo())
	{
		mAndroidAnimation = false;
		if(bBootOrShutDown){
			status_t err = mZip.open("/data/local/bootanimation1.zip");
			if (err != NO_ERROR) {
				err = mZip.open("/system/media/bootanimation1.zip");
				if (err != NO_ERROR) {
					mAndroidAnimation = true;
				}
			}
		} else {
			if(!bShutRotate){
				status_t err = mZip.open("/data/local/shutanimation1.zip");
				if (err != NO_ERROR) {
					err = mZip.open("/system/media/shutanimation1.zip");
					if (err != NO_ERROR) {
						mAndroidAnimation = true;
					}
				}
			} else {
				status_t err = mZip.open("/data/local/shutrotate1.zip");
				if (err != NO_ERROR) {
					err = mZip.open("/system/media/shutrotate1.zip");
					if (err != NO_ERROR) {
						mAndroidAnimation = true;
					}
				}
			}
		}
	}
	else
	{
		mAndroidAnimation = false;
		if(bBootOrShutDown){
			status_t err = mZip.open("/data/local/bootanimation.zip");
			if (err != NO_ERROR) {
				err = mZip.open("/system/media/bootanimation.zip");
				if (err != NO_ERROR) {
					mAndroidAnimation = true;
				}
			}
		} else {
			if(!bShutRotate){
				status_t err = mZip.open("/data/local/shutanimation.zip");
				if (err != NO_ERROR) {
					err = mZip.open("/system/media/shutanimation.zip");
					if (err != NO_ERROR) {
						mAndroidAnimation = true;
					}
				}
			} else {
				status_t err = mZip.open("/data/local/shutrotate.zip");
				if (err != NO_ERROR) {
					err = mZip.open("/system/media/shutrotate.zip");
					if (err != NO_ERROR) {
						mAndroidAnimation = true;
					}
				}
			}
		}
	}
    XLOGV("after check bootanimation.zip");
    /*
    mAndroidAnimation = true;
    // If the device has encryption turned on or is in process 
    // of being encrypted we show the encrypted boot animation.
    char decrypt[PROPERTY_VALUE_MAX];
    property_get("vold.decrypt", decrypt, "");

    bool encryptedAnimation = atoi(decrypt) != 0 || !strcmp("trigger_restart_min_framework", decrypt);

    if ((encryptedAnimation &&
            (access(SYSTEM_ENCRYPTED_BOOTANIMATION_FILE, R_OK) == 0) &&
            (mZip.open(SYSTEM_ENCRYPTED_BOOTANIMATION_FILE) == NO_ERROR)) ||

            ((access(USER_BOOTANIMATION_FILE, R_OK) == 0) &&
            (mZip.open(USER_BOOTANIMATION_FILE) == NO_ERROR)) ||

            ((access(SYSTEM_BOOTANIMATION_FILE, R_OK) == 0) &&
            (mZip.open(SYSTEM_BOOTANIMATION_FILE) == NO_ERROR))) {
        mAndroidAnimation = false;
    } */

    return NO_ERROR;
}

bool BootAnimation::threadLoop()
{
    bool r;
    XLOGD("enter threadLoop()");
    const char *pSoundFileName;
    const char *pBackupSoundFileName;

    if(!bBootOrShutDown){
        pSoundFileName= "/data/local/shutaudio.mp3";
	    pBackupSoundFileName="/system/media/shutaudio.mp3";
    } else {
        pSoundFileName= "/data/local/bootaudio.mp3";
	    pBackupSoundFileName="/system/media/bootaudio.mp3";
    }
    bool bexist = false;
    int nMp3Exist = -1;

    bexist = (access(pSoundFileName, F_OK) == 0);
    if(!bexist){
	    bexist = (access(pBackupSoundFileName, F_OK) == 0);
	    if(bexist){
	        nMp3Exist = 1;
	    }
    } 

	if(bexist && bPlayMP3){
		sp<MediaPlayer> mediaplayer=new MediaPlayer();
		status_t mediastatus;
		if(nMp3Exist < 0){
			mediastatus = mediaplayer->setDataSource(pSoundFileName, NULL);
		} else {
			mediastatus = mediaplayer->setDataSource(pBackupSoundFileName, NULL);
		}
		if(mediastatus == NO_ERROR){
		 	mediaplayer->setAudioStreamType(AUDIO_STREAM_BOOT);
			mediastatus = mediaplayer->prepare();
		}

		if(mediastatus!=NO_ERROR && nMp3Exist < 0){
			mediastatus = mediaplayer->setDataSource(pBackupSoundFileName,NULL);
			if(mediastatus == NO_ERROR){
			 	mediaplayer->setAudioStreamType(AUDIO_STREAM_BOOT);
				mediastatus = mediaplayer->prepare();
			}
		}

		if(mediastatus == NO_ERROR){		    
			mediastatus = mediaplayer->start();
		}

		if (mAndroidAnimation) {
			r = android();
		} else {
			XLOGD("threadLoop() movie()");
			r = movie();
		}
		if(mediastatus == NO_ERROR){		    
			mediaplayer->stop();
			mediaplayer->disconnect();       
		}

		XLOGV("threadLoop() exit movie()");
		eglMakeCurrent(mDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
		eglDestroyContext(mDisplay, mContext);
		eglDestroySurface(mDisplay, mSurface);
		mFlingerSurface.clear();
		mFlingerSurfaceControl.clear();
		eglTerminate(mDisplay);
		IPCThreadState::self()->stopProcess();

	} else {
		if (mAndroidAnimation) {
			r = android();
		} else {
			XLOGD("threadLoop() movie()");
			r = movie();
		}
		XLOGV("threadLoop() exit movie()");
		eglMakeCurrent(mDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
		eglDestroyContext(mDisplay, mContext);
		eglDestroySurface(mDisplay, mSurface);
		mFlingerSurface.clear();
		mFlingerSurfaceControl.clear();
		eglTerminate(mDisplay);
		IPCThreadState::self()->stopProcess();
	}

	XLOGD("threadLoop() exit");
    return r;
}

bool BootAnimation::android()
{
    initTexture(&mAndroid[0], mAssets, "images/android-logo-mask.png");
    initTexture(&mAndroid[1], mAssets, "images/android-logo-shine.png");

    // clear screen
    glShadeModel(GL_FLAT);
    glDisable(GL_DITHER);
    glDisable(GL_SCISSOR_TEST);
    glClearColor(0,0,0,1);
    glClear(GL_COLOR_BUFFER_BIT);
    eglSwapBuffers(mDisplay, mSurface);

    glEnable(GL_TEXTURE_2D);
    glTexEnvx(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);

	XLOGD("***** bootanimation init(%d %d %d %d) *****", mWidth, mHeight, mAndroid[0].w, mAndroid[0].h);
    GLint xc = (mWidth  - mAndroid[0].w) / 2;    
    GLint yc = (mHeight - mAndroid[0].h) / 2;

    int x = xc, y = yc;
    int w = mAndroid[0].w, h = mAndroid[0].h;
    if (x < 0) {
        w += x;
        x  = 0;
    }
    if (y < 0) {
        h += y;
        y  = 0;
    }
    if (w > mWidth) {
        w = mWidth;
    }
    if (h > mHeight) {
        h = mHeight;
    }

    const Rect updateRect(x, y, x+w, y+h);

    glScissor(updateRect.left, mHeight - updateRect.bottom, updateRect.width(),
            updateRect.height());

    // Blend state
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glTexEnvx(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);

    const nsecs_t startTime = systemTime();
    do {
        nsecs_t now = systemTime();
        double time = now - startTime;
        float t = 4.0f * float(time / us2ns(16667)) / mAndroid[1].w;
        GLint offset = (1 - (t - floorf(t))) * mAndroid[1].w;
        GLint x = xc - offset;

        glDisable(GL_SCISSOR_TEST);
        glClear(GL_COLOR_BUFFER_BIT);

        glEnable(GL_SCISSOR_TEST);
        glDisable(GL_BLEND);
        glBindTexture(GL_TEXTURE_2D, mAndroid[1].name);
        glDrawTexiOES(x,                 yc, 0, mAndroid[1].w, mAndroid[1].h);
        glDrawTexiOES(x + mAndroid[1].w, yc, 0, mAndroid[1].w, mAndroid[1].h);

        glEnable(GL_BLEND);
        glBindTexture(GL_TEXTURE_2D, mAndroid[0].name);
        glDrawTexiOES(xc, yc, 0, mAndroid[0].w, mAndroid[0].h);

        EGLBoolean res = eglSwapBuffers(mDisplay, mSurface);
        if (res == EGL_FALSE)
            break;

        // 12fps: don't animate too fast to preserve CPU
        const nsecs_t sleepTime = 83333 - ns2us(systemTime() - now);
        if (sleepTime > 0)
            usleep(sleepTime);
    } while (!exitPending());

    glDeleteTextures(1, &mAndroid[0].name);
    glDeleteTextures(1, &mAndroid[1].name);
    return false;
}


bool BootAnimation::movie()
{
    ZipFileRO& zip(mZip);

    size_t numEntries = zip.getNumEntries();
    ZipEntryRO desc = zip.findEntryByName("desc.txt");
    FileMap* descMap = zip.createEntryFileMap(desc);
    if (!descMap) {
        XLOGE("descMap is null");
        return false;
    }

    String8 desString((char const*)descMap->getDataPtr(),
            descMap->getDataLength());
    char const* s = desString.string();
    XLOGD("movie() Parse the description file");
    Animation animation;

    // Parse the description file
    for (;;) {
        const char* endl = strstr(s, "\n");
        if (!endl) break;
        String8 line(s, endl - s);
        const char* l = line.string();
        int fps, width, height, count, pause;
        char path[256];
        if (sscanf(l, "%d %d %d", &width, &height, &fps) == 3) {
            //XLOGD("> w=%d, h=%d, fps=%d", fps, width, height);
            animation.width = width;
            animation.height = height;
            animation.fps = fps;
        }
        if (sscanf(l, "p %d %d %s", &count, &pause, path) == 3) {
            //XLOGD("> count=%d, pause=%d, path=%s", count, pause, path);
            Animation::Part part;
            part.count = count;
            part.pause = pause;
            part.path = path;
            animation.parts.add(part);
        }
        s = ++endl;
    }

    // read all the data structures
    const size_t pcount = animation.parts.size();
    XLOGD("animation.parts.size() pcount = %d, numEntries = %ld", pcount,numEntries);
    for (size_t i=0 ; i<numEntries ; i++) {
        char name[256];
        ZipEntryRO entry = zip.findEntryByIndex(i);
        if (zip.getEntryFileName(entry, name, 256) == 0) {
            const String8 entryName(name);
            const String8 path(entryName.getPathDir());
            const String8 leaf(entryName.getPathLeaf());
            if (leaf.size() > 0) {
                for (size_t j=0 ; j<pcount ; j++) {
                    if (path == animation.parts[j].path) {
                        int method;
                        // supports only stored png files
                        if (zip.getEntryInfo(entry, &method, 0, 0, 0, 0, 0)) {
                            if (method == ZipFileRO::kCompressStored) {
                                FileMap* map = zip.createEntryFileMap(entry);
                                if (map) {
                                    Animation::Frame frame;
                                    frame.name = leaf;
                                    frame.map = map;
                                    Animation::Part& part(animation.parts.editItemAt(j));
                                    part.frames.add(frame);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    XLOGD("exit zip parser");
    // clear screen
    glShadeModel(GL_FLAT);
    glDisable(GL_DITHER);
    glDisable(GL_SCISSOR_TEST);
    glDisable(GL_BLEND);
    glClearColor(0,0,0,1);
    glClear(GL_COLOR_BUFFER_BIT);

    XLOGD("movie() before eglSwapBuffers");
    EGLBoolean retres = eglSwapBuffers(mDisplay, mSurface);
    if (retres == EGL_FALSE) {
        XLOGE("eglSwapBuffers(mDisplay, mSurface) return EGL_FALSE");
    }
    XLOGD("movie() after eglSwapBuffers");
    
    glBindTexture(GL_TEXTURE_2D, 0);
    glEnable(GL_TEXTURE_2D);
    glTexEnvx(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
    glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
    glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    int xc = (mWidth - animation.width) / 2;
    int yc = ((mHeight - animation.height) / 2);
    if (xc < 0)
  	{
  		 xc = 0;
  	}
    if (yc < 0)
  	{
  		 yc = 0;
  	}
    nsecs_t lastFrame = systemTime();
    nsecs_t frameDuration = s2ns(1) / animation.fps;
    frameDuration = ns2us(frameDuration);
    nsecs_t nDuration = frameDuration;
    XLOGD("Boot animation frameDuration = s2ns(1) / animation.fps(%d) = %lld (us) ;", animation.fps, nDuration);

    Region clearReg(Rect(mWidth, mHeight));
    clearReg.subtractSelf(Rect(xc, yc, xc+animation.width, yc+animation.height));
    bool bFirst = true;
    int nFramecount = 0;
    
    if(exitPending()){
        XLOGD("exitPending is true");
    } else {
        XLOGD("exitPending is false");
    }
    for (size_t i=0 ; i<pcount && !exitPending() ; i++) {
        const Animation::Part& part(animation.parts[i]);
        const size_t fcount = part.frames.size();
        glBindTexture(GL_TEXTURE_2D, 0);
        if(bFirst){
           XLOGV("Boot animation first package will start.");
        }

        for (int r=0 ; !part.count || r<part.count ; r++) {
            for (int j=0 ; j<fcount && !exitPending(); j++) {
                const Animation::Frame& frame(part.frames[j]);
				XLOGD("[BootAnimation %s %d]%lld ms,file=%s",__FUNCTION__,__LINE__,ns2ms(systemTime()),frame.name.string());

                if (r > 0) {
                    glBindTexture(GL_TEXTURE_2D, frame.tid);
                } else {
                    if (part.count != 1) {
                        glGenTextures(1, &frame.tid);
                        glBindTexture(GL_TEXTURE_2D, frame.tid);
                        glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                        glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                    }
                    initTexture(
                            frame.map->getDataPtr(),
                            frame.map->getDataLength());
                }

                if (!clearReg.isEmpty()) {
                    Region::const_iterator head(clearReg.begin());
                    Region::const_iterator tail(clearReg.end());
                    glEnable(GL_SCISSOR_TEST);
                    while (head != tail) {
                        const Rect& r(*head++);
                        glScissor(r.left, mHeight - r.bottom,
                                r.width(), r.height());
                        glClear(GL_COLOR_BUFFER_BIT);
                    }
                    glDisable(GL_SCISSOR_TEST);
                }
                glDrawTexiOES(xc, yc, 0, animation.width, animation.height);
                EGLBoolean retres1 = eglSwapBuffers(mDisplay, mSurface);
                if (retres1 == EGL_FALSE) {
                    XLOGE("eglSwapBuffers(mDisplay, mSurface) return EGL_FALSE");
                }
                if(bFirst && j == fcount-1){
                   XLOGV("Boot animation first folder animation play end. total frames = %d",j + 1);
                   bFirst = false;
                }

                nsecs_t now = systemTime();
                nsecs_t nTimeCost = ns2us(now - lastFrame);
                nsecs_t delay = frameDuration - nTimeCost;
                
                if(bFirst) {
                    //XLOGV("Boot animation play frame %d cost time = %ld us; sleep(if above 0) = %ld (us)",j + 1,nTimeCost,delay);
                    XLOGD("[BootAnimation %s %d]j+1=%d , nTimeCost=%lld us , delay=%lld us",__FUNCTION__,__LINE__,j + 1,nTimeCost,delay);
                } 
                //lastFrame = now;//mtk54232
                
                if (delay > 0){
                    usleep(delay);
                }
                lastFrame = systemTime();//mtk54232
            }
            
            if(part.pause > 0)
                usleep(part.pause * ns2us(frameDuration));
            else
              usleep(ns2us(frameDuration));
        }

        // free the textures for this part
        if (part.count != 1) {
            for (size_t j=0 ; j<fcount ; j++) {
                const Animation::Frame& frame(part.frames[j]);
                glDeleteTextures(1, &frame.tid);
            }
        }
    }
    if(exitPending()){
        XLOGD("exitPending is true");
    } else {
        XLOGD("exitPending is false");
    }
    return false;
}

bool BootAnimation::changeLogo()
{
	int fd = 0;
	char flag = 0;
	int result;
	fd = open(LOGO_B_DEV, O_RDONLY);
	result=lseek(fd,FLAG_OFFSET,SEEK_SET);
	if(result!=FLAG_OFFSET)
	{
		goto end;
	}
	result = read(fd,&flag,1);
	if(result!=1)
	{
		goto end;
	}
	close(fd);
	if(flag)
	{
		return true;
	}
	else
	{
		return false;
	}

end:
	close(fd);
	return false;
}
// ---------------------------------------------------------------------------

}
; // namespace android
