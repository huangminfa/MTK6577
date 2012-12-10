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

/*****************************************************************************
 *
 * Filename:
 * ---------
 *   OMXCodec_ut.cpp
 *
 * Project:
 * --------
 *   MT65xx
 *
 * Description:
 * ------------
 *   OMXcodec Video unit test code
 *
 * Author:
 * -------
 *   Bruce Hsu (mtk04278)
 *
 ****************************************************************************/

//reference from OMXHarness.cpp and MtkVideoTranscoder.cpp
#define LOG_TAG "OMXCodecUT"
#include <utils/Log.h>

#include "OMXCodec_ut.h"

#include <sys/time.h>

#include <binder/ProcessState.h>
//#include <binder/MemoryDealer.h>
#include <binder/IServiceManager.h>
#include <media/IMediaPlayerService.h>
//#include <media/stagefright/DataSource.h>
#include <media/stagefright/FileSource.h>
#include <media/stagefright/MediaBuffer.h>
#include <media/stagefright/MediaDebug.h>
#include <media/stagefright/MediaDefs.h>
#include <media/stagefright/MediaErrors.h>
#include <media/stagefright/MediaExtractor.h>
#include <media/stagefright/MetaData.h>

#include <sys/types.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#define DEFAULT_TIMEOUT         500000

#define EXPECT(condition, info) \
    if (!(condition)) {         \
        LOGE(info); printf("\n  * " info "\n"); return UNKNOWN_ERROR; \
    }

namespace android {

YUVSource::YUVSource(const char* inputFile, int32_t inputColorFormat, int32_t inputWidth, int32_t inputHeight, int32_t& status)
{
    pfFin = fopen(inputFile, "rb");
    if(pfFin == NULL)
    {
        LOGE("Open file %s fail!");
        status = UNKNOWN_ERROR;
        return;
    }

    mWidth = inputWidth;
    mHeight= inputHeight;
    mColorFormat = inputColorFormat;
    // init meta data
    mMetaData = new MetaData;
    mMetaData->setInt32(kKeyWidth, mWidth);
    mMetaData->setInt32(kKeyHeight, mHeight);
    mMetaData->setInt32(kKeyColorFormat, mColorFormat);
    mMetaData->setCString(kKeyMIMEType, MEDIA_MIMETYPE_VIDEO_RAW);
    //mMetaData->setInt64(kKeyDuration, mSourceDurationUs);

    for (int i = 0 ; i < 2 ; i++)
    {
        MediaBuffer *pBuf = new MediaBuffer(inputWidth*inputHeight*3/2);
        mBufferGroup.add_buffer(pBuf);
        LOGD("buf %d is %X", i, pBuf);
    }

    pthread_mutex_init(&mFileLock, NULL);

    mTimeUs = 0;
    mFrame = 0;
    mStart = false;
    status = OK;
}
YUVSource::~YUVSource()
{
    if(pfFin)
    {
        fclose(pfFin);
    }
    pthread_mutex_destroy(&mFileLock);
}
sp<MetaData> YUVSource::getFormat()
{
    LOGE("YUV getFormat");
    return mMetaData;
}
status_t YUVSource::start(MetaData *params)
{
    LOGE("YUV start");
    mStart = true;
    return OK;
}
status_t YUVSource::stop()
{
    LOGE("YUV stop");
    mStart = false;
    return OK;
}
status_t YUVSource::read(MediaBuffer **buffer, const MediaSource::ReadOptions *options)
{
    LOGE("YUV read");
    if(mStart)
    {
        pthread_mutex_lock(&mFileLock);
        if(options != NULL)
        {
            int64_t time_us;
            LOGE("read get option");
            ReadOptions::SeekMode  seekMode;
            if(options->getSeekTo(&time_us, &seekMode))
            {
                LOGD("time=%lu, frame=%d, mode=%d, Don't support seekMode", time_us, time_us/33333, seekMode);
                int iFrameNum = time_us/33333;
                mTimeUs = iFrameNum*33333;
                LOGD("do file seek pos=%lu, tid=%d", iFrameNum*mWidth*mHeight*3/2, gettid());
                fseek(pfFin, iFrameNum*mWidth*mHeight*3/2, SEEK_SET);
                LOGD("after fseek");
            }
        }

        CHECK_EQ(mBufferGroup.acquire_buffer(buffer), OK);
        LOGD("before YUV read, buf size=%u, tid=%d", (*buffer)->range_length(), gettid());
        int iLen = fread((uint8_t*)(*buffer)->data(), 1, mWidth*mHeight*3/2, pfFin);
        LOGD("after YUV read, len=%d", iLen);
        (*buffer)->set_range(0, iLen);
        (*buffer)->meta_data()->clear();
        (*buffer)->meta_data()->setInt64(kKeyTime, mTimeUs);
        if(iLen == 0)//EOS
        {
            (*buffer)->release();
            *buffer = NULL;
            pthread_mutex_unlock(&mFileLock);
            return ERROR_END_OF_STREAM;
        }
        else
        {
            mTimeUs += 33333;
        }
        pthread_mutex_unlock(&mFileLock);
        return OK;
    }
    else
    {
        LOGE("YUVSource Read but doesn't start");
        return UNKNOWN_ERROR;
    }
}

static int getWHFromName(const char *szName, int *piWidth, int *piHeight)
{
    struct NameToMime {
        const char *szName;
        int         iWidth;
        int         iHeight;
    };
    static const NameToMime kNameToMime[] = {
        {"OMX.MTK.VIDEO.ENCODER.AVC", 480, 320},
        {"OMX.MTK.VIDEO.ENCODER.MPEG4", 320, 240}
    };
    for(size_t i=0;i<sizeof(kNameToMime)/sizeof(kNameToMime[0]);i++)
    {
        if(!strcmp(szName, kNameToMime[i].szName))
        {
            *piWidth = kNameToMime[i].iWidth;
            *piHeight= kNameToMime[i].iHeight;
            return 1;
        }
    }
    return 0;
}
static const char *getMimeFromName(const char *szName)
{
    struct NameToMime {
        const char *szName;
        const char *szMime;
    };
    static const NameToMime kNameToMime[] = {
        {"OMX.MTK.VIDEO.DECODER.AVC", "video/avc"},
        {"OMX.MTK.VIDEO.DECODER.MPEG4", "video/mp4v-es"},
        {"OMX.MTK.VIDEO.DECODER.VPX", "video/x-vnd.on2.vp8"},
    };
    for(size_t i=0;i<sizeof(kNameToMime)/sizeof(kNameToMime[0]);i++)
    {
        if(!strcmp(szName, kNameToMime[i].szName))
        {
            return kNameToMime[i].szMime;
        }
    }
    return "";
}

static const char *getFileFromName(const char *szName)
{
    struct NameToFile{
        const char *szName;
        const char *szFile;
    };
    static const NameToFile kNameToFile[] = {
        {"OMX.MTK.VIDEO.DECODER.AVC", "file:///mnt/sdcard2/OMXCodec/h264.mp4"},
        {"OMX.MTK.VIDEO.DECODER.MPEG4", "file:///mnt/sdcard2/OMXCodec/mp4.mp4"},
        {"OMX.MTK.VIDEO.DECODER.VPX", "file:///mnt/sdcard2/OMXCodec/vp8.webm"},
        {"OMX.MTK.VIDEO.ENCODER.AVC", "/mnt/sdcard2/hvga/lab_fast_hvga_212f.yuv"},
        {"OMX.MTK.VIDEO.ENCODER.MPEG4", "/mnt/sdcard2/qvga/akiyo_mtk_qvga.yuv"}
    };
    for(size_t i=0;i<sizeof(kNameToFile)/sizeof(kNameToFile[0]);i++)
    {
        //LOGD("%s %s", szName, kNameToFile[i].szName);
        if(!strcmp(szName, kNameToFile[i].szName))
        {
            return kNameToFile[i].szFile;
        }
    }
    return "";
}

OMXCodecTest::OMXCodecTest() 
{
    init("OMX.MTK.VIDEO.DECODER.AVC");
}

OMXCodecTest::OMXCodecTest(const char *szCompName)
{
    init(szCompName);
}

OMXCodecTest::~OMXCodecTest()
{
    LOGD("~OMXCodecTest");
}

int OMXCodecTest::initOMX()
{
    LOGE("+initOMX");
    sp<IServiceManager> sm = defaultServiceManager();
    sp<IBinder> binder = sm->getService(String16("media.player"));
    sp<IMediaPlayerService> service = interface_cast<IMediaPlayerService>(binder);
    mOMX = service->getOMX();

    LOGE("-initOMX");
    return mOMX != 0 ? OK : NO_INIT;
}
#if 0
static sp<MediaExtractor> CreateExtractorFromURI(const char *uri) {
    sp<DataSource> source = DataSource::CreateFromURI(uri);
    LOGE("create source by uri:%s", uri);

    if (source == NULL) {
        LOGE("source is NULL");
        return NULL;
    }

    return MediaExtractor::Create(source);
}
static sp<MediaSource> CreateSourceForMime(const char *mime) {
    //const char *url = GetURLForMime(mime);
    const char *url = "file:///sdcard/test.3gp";
    CHECK(url != NULL);

    LOGE("url=%s", url);
    sp<MediaExtractor> extractor = CreateExtractorFromURI(url);

    if (extractor == NULL) {
        LOGE("extractor is NULL");
        return NULL;
    }

    for (size_t i = 0; i < extractor->countTracks(); ++i) {
        sp<MetaData> meta = extractor->getTrackMetaData(i);
        CHECK(meta != NULL);

        const char *trackMime;
        CHECK(meta->findCString(kKeyMIMEType, &trackMime));

        if (!strcasecmp("video/avc", trackMime)) {
            return extractor->getTrack(i);
        }
    }

    return NULL;
}
#endif//0
int OMXCodecTest::initSource(const char *szName)
{
    const char *uri = getFileFromName(szName);
    const char *mime = getMimeFromName(szName);

    if(mIsEncoder)
    {
        if(getWHFromName(szName, &mWidth, &mHeight) == 0)
        {
            return UNKNOWN_ERROR;
        }

        status_t err;
        mSource = new YUVSource(uri, (int32_t)OMX_COLOR_FormatYUV420Planar, (int32_t)mWidth, (int32_t)mHeight, (int32_t&)err);
        if(err != OK)
        {
            mSource.clear();
            mSource = NULL;
            LOGE("mSource is NULL");
            return NO_INIT;
        }
        return OK;
    }
    else
    {
        mSource = getSource(uri, mime);
        mSeekSource = getSource(uri, mime);

        if(mSource == NULL || mSeekSource == NULL)
        {
            LOGE("mSource or mSeekSource is NULL");
            return NO_INIT;
        }
        else
        {
            return OK;
        }
    }
}

int OMXCodecTest::initMetaData(const char *szCompName)
{
    if(mIsEncoder)
    {
        mMetaData = new MetaData;

        mMetaData->setInt32(kKeyWidth, mWidth);
        mMetaData->setInt32(kKeyHeight, mHeight);
        mMetaData->setInt32(kKeyStride, mWidth);        //should be ROUND16(width)
        mMetaData->setInt32(kKeySliceHeight, mHeight);  //should be ROUND16(height)

        mMetaData->setInt32(kKeyFrameRate, mFps);
        mMetaData->setInt32(kKeyBitRate, 512*1024);
        mMetaData->setInt32(kKeyIFramesInterval, 1); //second
        mMetaData->setInt32(kKeyColorFormat, OMX_COLOR_FormatYUV420Planar);    // MT6575
        if(!strcmp(szCompName, "OMX.MTK.VIDEO.ENCODER.MPEG4"))
        {
            mMetaData->setCString(kKeyMIMEType, MEDIA_MIMETYPE_VIDEO_MPEG4);
        }
        else if(!strcmp(szCompName, "OMX.MTK.VIDEO.ENCODER.AVC"))
        {
            mMetaData->setCString(kKeyMIMEType, MEDIA_MIMETYPE_VIDEO_AVC);
        }
        else
        {
            LOGE("codec not supported");
            return NO_INIT;
        }
    }
    else
    {
        mMetaData = mSource->getFormat();
    }
    return OK;
}

int OMXCodecTest::init(const char *szCompName)
{
    LOGE("+init");

    //mCompName = strdup(szCompName);
    checkNameForEncoder(szCompName);

    if(initOMX() != OK)
    {
        printf("initOMX fail\n");
        return -1;
    }

    if(initSource(szCompName) != OK)
    {
        printf("initSource fail\n");
        return -1;
    }

    if(initMetaData(szCompName) != OK)
    {
        printf("initMetaData fail\n");
        return -1;
    }

    mOmxCodec = OMXCodec::Create(
            mOMX, mMetaData, mIsEncoder/* createEncoder */,
            mSource, szCompName);

    if(mOmxCodec == NULL)
    {
        printf("create OMXCodec fail\n");
        return -1;
    }

    LOGE("-init");
    return 1;
}

bool OMXCodecTest::checkNameForEncoder(const char *szCompName)
{
    mIsEncoder = (strstr(szCompName, "ENCODER")) ? true : false;
    return mIsEncoder;
}

sp<MediaSource> OMXCodecTest::getSource(const char *szUri, const char *szMime)
{
    sp<DataSource> source = DataSource::CreateFromURI(szUri);
    if (source == NULL)
    {
        LOGE("source is NULL");
        return NULL;
    }

    sp<MediaExtractor> extractor = MediaExtractor::Create(source);
    if (extractor == NULL)
    {
        LOGE("extractor is NULL");
        return NULL;
    }
    for (size_t i = 0; i < extractor->countTracks(); ++i)
    {
        sp<MetaData> meta = extractor->getTrackMetaData(i);
        CHECK(meta != NULL);

        const char *trackMime;
        CHECK(meta->findCString(kKeyMIMEType, &trackMime));

        if (!strcasecmp(szMime, trackMime))
        {
            return extractor->getTrack(i);
        }
    }
    return NULL;
}

int OMXCodecTest::doTest()
{
    if(mOmxCodec == NULL)
    {
        LOGE("OmxCodec is NULL");
        return -1;
    }

    if(StartAndStopTest()) printf("case 1 success\n");
    else printf("case 1 fail\n");
    if(GetFormatTest()) printf("case 2 success\n");
    else printf("case 2 fail\n");
    
    if(mIsEncoder)
    {
        if(SetForceIFrameTest()) printf("case 3 success\n");
        else printf("case 3 fail\n");
        //GetCameraMetaDateTest();
    }

    if(ReadBeforeStartTest()) printf("case 4 success\n");
    else printf("case 4 fail\n");

    if(!mIsEncoder)
    {
        if(TrickPlayTest()) printf("case 5 success\n");
        else printf("case 5 fail\n");
    }
    return 1;
}

int OMXCodecTest::StartAndStopTest()
{
    LOGD("start");
    mOmxCodec->start();

    MediaBuffer *buffer=NULL;
    status_t err;

    for(int i=0;i<0xffffffff;i++)
    {
        LOGD("frame %d", i);

        if(i == 0)
        {
            MediaSource::ReadOptions    options;
            options.setSeekTo(0);
            err = mOmxCodec->read(&buffer, &options);
        }
        else
        {
            err = mOmxCodec->read(&buffer);
        }
        if(err == OK)
        {
            if(buffer != NULL)
            {
                int iIsSyncFrame;
                if(!buffer->meta_data()->findInt32(kKeyIsSyncFrame, &iIsSyncFrame))
                {
                    iIsSyncFrame = -1;
                }
                //printf("frame %d, size=%d, sync=%d\n", i, buffer->range_length(), iIsSyncFrame);
                buffer->release();
                buffer = NULL;
            }
        }
        else
        {
            if(buffer != NULL)
            {
                LOGD("impossible!!");
                printf("frame %d, size=%d\n", i, buffer->range_length());
                buffer->release();
                buffer = NULL;
            }
            if(err == ERROR_END_OF_STREAM)
            {
                LOGD("EOS");
                break;
            }
            else
            {
                LOGD("err=%X", err);
                mOmxCodec->stop();
                return 0;
            }
        }
    }

    LOGD("stop");
    mOmxCodec->stop();

    return 1;
}
int OMXCodecTest::GetFormatTest()
{
    sp<MetaData> format = mOmxCodec->getFormat();
    if(format == NULL) return 0;
    const char *szMime;
    int width, height;
    if(!format->findCString(kKeyMIMEType, &szMime)) return 0;
    if(!format->findInt32(kKeyWidth, &width)) return 0;
    if(!format->findInt32(kKeyHeight, &height)) return 0;
    LOGD("Get output format mime=%s, w=%d, h=%d", szMime, width, height);

    return 1;
}
int OMXCodecTest::SetForceIFrameTest()
{
    LOGD("start");
    mOmxCodec->start();

    MediaBuffer *buffer=NULL;
    status_t err;
    bool bGetIFrame=false;
    for(int i=0;i<mFps;i++)
    {
        LOGD("frame %d", i);
        reinterpret_cast<OMXCodec*>(mOmxCodec.get())->vEncSetForceIframe(true);
        if(i == 0)
        {
            //int64_t iTime=0;
            MediaSource::ReadOptions    options;
            options.setSeekTo(0);
            err = mOmxCodec->read(&buffer, &options);
        }
        else
        {
            err = mOmxCodec->read(&buffer);
        }
        if(err == OK)
        {
            if(buffer != NULL)
            {
                int iIsSyncFrame;
                if(!buffer->meta_data()->findInt32(kKeyIsSyncFrame, &iIsSyncFrame))
                {
                    iIsSyncFrame = -1;
                }
                if(!bGetIFrame && i > 1 && iIsSyncFrame == 1)
                {
                    LOGD("get I frame");
                    bGetIFrame = true;
                }
                //printf("frame %d, size=%d, sync=%d\n", i, buffer->range_length(), iIsSyncFrame);
                buffer->release();
                buffer = NULL;
            }
        }
    }

    LOGD("stop");
    mOmxCodec->stop();
    return bGetIFrame ? 1 : 0;
}
int OMXCodecTest::GetCameraMetaDateTest()
{
    sp<MetaData> camera = reinterpret_cast<OMXCodec*>(mOmxCodec.get())->getCameraMeta();
    if(camera == NULL)
    {
        LOGD("Camera meta does not exist");
        return 0;
    }
    else
    {
        int32_t prCameraInfo;
        camera->findInt32(kKeyCamMemInfo, &prCameraInfo);
        LOGD("Get camera int %X", prCameraInfo);
    }
    return 1;
}
int OMXCodecTest::ReadBeforeStartTest()
{
    MediaBuffer *buffer;
    status_t err;

    err = mOmxCodec->read(&buffer, NULL);
    LOGD("Read before start err=%X", err);
    return (err == OK) ? 0 : 1;
}
static double uniform_rand() {
    return (double)rand() / RAND_MAX;
}
static bool CloseEnough(int64_t time1Us, int64_t time2Us) {
#if 0
    int64_t diff = time1Us - time2Us;
    if (diff < 0) {
        diff = -diff;
    }

    return diff <= 50000;
#else
    return time1Us == time2Us;
#endif
}
int OMXCodecTest::TrickPlayTest()
{
    CHECK_EQ(mOmxCodec->start(), OK);
    CHECK_EQ(mSeekSource->start(), OK);

    int64_t durationUs;
    CHECK(mSource->getFormat()->findInt64(kKeyDuration, &durationUs));

    LOGI("stream duration is %lld us (%.2f secs)",
         durationUs, durationUs / 1E6);

    static const int32_t kNumIterations = 100;

    // We are always going to seek beyond EOS in the first iteration (i == 0)
    // followed by a linear read for the second iteration (i == 1).
    // After that it's all random.
    for (int32_t i = 0; i < kNumIterations; ++i) {
        int64_t requestedSeekTimeUs;
        int64_t actualSeekTimeUs;
        MediaSource::ReadOptions options;

        double r = uniform_rand();

        if ((i == 1) || (i > 0 && r < 0.5)) {
            // 50% chance of just continuing to decode from last position.

            requestedSeekTimeUs = -1;

            LOGI("requesting linear read");
        } else {
            if (i == 0 || r < 0.05) {
                // 5% chance of seeking beyond end of stream.

                requestedSeekTimeUs = durationUs;

                LOGI("requesting seek beyond EOF");
            } else {
                requestedSeekTimeUs =
                    (int64_t)(uniform_rand() * durationUs);

                LOGI("requesting seek to %lld us (%.2f secs)",
                     requestedSeekTimeUs, requestedSeekTimeUs / 1E6);
            }

            MediaBuffer *buffer = NULL;
            options.setSeekTo(
                    requestedSeekTimeUs, MediaSource::ReadOptions::SEEK_NEXT_SYNC);

            if (mSeekSource->read(&buffer, &options) != OK) {
                CHECK_EQ(buffer, NULL);
                actualSeekTimeUs = -1;
            } else {
                CHECK(buffer != NULL);
                CHECK(buffer->meta_data()->findInt64(kKeyTime, &actualSeekTimeUs));
                CHECK(actualSeekTimeUs >= 0);

                buffer->release();
                buffer = NULL;
                while(actualSeekTimeUs < requestedSeekTimeUs)
                {
                    mSeekSource->read(&buffer, NULL);
                    buffer->meta_data()->findInt64(kKeyTime, &actualSeekTimeUs);
                    buffer->release();
                    buffer = NULL;
                    //printf("get frame time, %lld\n", actualSeekTimeUs);
                }
            }

            LOGI("nearest keyframe is at %lld us (%.2f secs)",
                 actualSeekTimeUs, actualSeekTimeUs / 1E6);
        }
        //printf("seek times %d, request time=%lld\n", i, requestedSeekTimeUs);
        status_t err;
        MediaBuffer *buffer;
        for (;;) {
            err = mOmxCodec->read(&buffer, &options);
            options.clearSeekTo();
            if (err == INFO_FORMAT_CHANGED) {
                CHECK_EQ(buffer, NULL);
                continue;
            }
            if (err == OK) {
                CHECK(buffer != NULL);
                if (buffer->range_length() == 0) {
                    buffer->release();
                    buffer = NULL;
                    continue;
                }
            } else {
                CHECK_EQ(buffer, NULL);
            }

            break;
        }

        if (requestedSeekTimeUs < 0) {
            // Linear read.
            if (err != OK) {
                CHECK_EQ(buffer, NULL);
            } else {
                CHECK(buffer != NULL);
                buffer->release();
                buffer = NULL;
            }
        } else if (actualSeekTimeUs < 0) {
            EXPECT(err != OK,
                   "We attempted to seek beyond EOS and expected "
                   "ERROR_END_OF_STREAM to be returned, but instead "
                   "we got a valid buffer.");
            EXPECT(err == ERROR_END_OF_STREAM,
                   "We attempted to seek beyond EOS and expected "
                   "ERROR_END_OF_STREAM to be returned, but instead "
                   "we found some other error.");
            CHECK_EQ(err, ERROR_END_OF_STREAM);
            CHECK_EQ(buffer, NULL);
        } else {
            EXPECT(err == OK,
                   "Expected a valid buffer to be returned from "
                   "OMXCodec::read.");
            CHECK(buffer != NULL);

            int64_t bufferTimeUs;
            CHECK(buffer->meta_data()->findInt64(kKeyTime, &bufferTimeUs));
            if (!CloseEnough(bufferTimeUs, actualSeekTimeUs)) {
                printf("\n  * Attempted seeking to %lld us (%.2f secs)",
                       requestedSeekTimeUs, requestedSeekTimeUs / 1E6);
                printf("\n  * Nearest keyframe is at %lld us (%.2f secs)",
                       actualSeekTimeUs, actualSeekTimeUs / 1E6);
                printf("\n  * Returned buffer was at %lld us (%.2f secs)\n\n",
                       bufferTimeUs, bufferTimeUs / 1E6);

                buffer->release();
                buffer = NULL;

                CHECK_EQ(mOmxCodec->stop(), OK);

                return 0;
            }

            buffer->release();
            buffer = NULL;
        }
    }

    CHECK_EQ(mSeekSource->stop(), OK);
    CHECK_EQ(mOmxCodec->stop(), OK);
    return 1;
}

}  // namespace android

int main(int argc, const char *argv[])
{
    using namespace android;

    if(argc != 2)
    {
        printf("usage:%s component_name\n", argv[0]);
        return 0;
    }

    unsigned long seed = 0xdeadbeef;
    srand(seed);

    android::ProcessState::self()->startThreadPool();
    DataSource::RegisterDefaultSniffers();

    sp<OMXCodecTest> hTest = new OMXCodecTest(argv[1]);

    LOGE("Test OmxCodec Start");
    hTest->doTest();
    LOGE("Test OmxCodec End");

    return 0;
}

