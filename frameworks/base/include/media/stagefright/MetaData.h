/*
 * Copyright (C) 2009 The Android Open Source Project
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

#ifndef META_DATA_H_

#define META_DATA_H_

#include <sys/types.h>

#include <stdint.h>

#include <utils/RefBase.h>
#include <utils/KeyedVector.h>

namespace android {

// The following keys map to int32_t data unless indicated otherwise.
enum {
    kKeyMIMEType          = 'mime',  // cstring
    kKeyWidth             = 'widt',  // int32_t, image pixel
    kKeyHeight            = 'heig',  // int32_t, image pixel
    kKeyDisplayWidth      = 'dWid',  // int32_t, display/presentation
    kKeyDisplayHeight     = 'dHgt',  // int32_t, display/presentation

    // a rectangle, if absent assumed to be (0, 0, width - 1, height - 1)
    kKeyCropRect          = 'crop',

    kKeyRotation          = 'rotA',  // int32_t (angle in degrees)
    kKeyIFramesInterval   = 'ifiv',  // int32_t
    kKeyStride            = 'strd',  // int32_t
    kKeySliceHeight       = 'slht',  // int32_t
    kKeyChannelCount      = '#chn',  // int32_t
    kKeySampleRate        = 'srte',  // int32_t (audio sampling rate Hz)
    kKeyFrameRate         = 'frmR',  // int32_t (video frame rate fps)
    kKeyBitRate           = 'brte',  // int32_t (bps)
    kKeyESDS              = 'esds',  // raw data
    kKeyAVCC              = 'avcc',  // raw data
    kKeyD263              = 'd263',  // raw data
    kKeyVorbisInfo        = 'vinf',  // raw data
    kKeyVorbisBooks       = 'vboo',  // raw data
    kKeyWantsNALFragments = 'NALf',
    kKeyIsSyncFrame       = 'sync',  // int32_t (bool)
    kKeyIsCodecConfig     = 'conf',  // int32_t (bool)
    kKeyTime              = 'time',  // int64_t (usecs)
    kKeyDecodingTime      = 'decT',  // int64_t (decoding timestamp in usecs)
    kKeyNTPTime           = 'ntpT',  // uint64_t (ntp-timestamp)
    kKeyTargetTime        = 'tarT',  // int64_t (usecs)
    kKeyDriftTime         = 'dftT',  // int64_t (usecs)
    kKeyAnchorTime        = 'ancT',  // int64_t (usecs)
    kKeyDuration          = 'dura',  // int64_t (usecs)
    kKeyColorFormat       = 'colf',
    kKeyPlatformPrivate   = 'priv',  // pointer
    kKeyDecoderComponent  = 'decC',  // cstring
    kKeyBufferID          = 'bfID',
    kKeyMaxInputSize      = 'inpS',
    kKeyThumbnailTime     = 'thbT',  // int64_t (usecs)
    kKeyTrackID           = 'trID',
    kKeyIsDRM             = 'idrm',  // int32_t (bool)

    kKeyAlbum             = 'albu',  // cstring
    kKeyArtist            = 'arti',  // cstring
    kKeyAlbumArtist       = 'aart',  // cstring
    kKeyComposer          = 'comp',  // cstring
    kKeyGenre             = 'genr',  // cstring
    kKeyTitle             = 'titl',  // cstring
    kKeyYear              = 'year',  // cstring
    kKeyAlbumArt          = 'albA',  // compressed image data
    kKeyAlbumArtMIME      = 'alAM',  // cstring
    kKeyAuthor            = 'auth',  // cstring
    kKeyCDTrackNumber     = 'cdtr',  // cstring
    kKeyDiscNumber        = 'dnum',  // cstring
    kKeyDate              = 'date',  // cstring
    kKeyWriter            = 'writ',  // cstring
    kKeyCompilation       = 'cpil',  // cstring
    kKeyLocation          = 'loc ',  // cstring
    kKeyTimeScale         = 'tmsl',  // int32_t

    // video profile and level
    kKeyVideoProfile      = 'vprf',  // int32_t
    kKeyVideoLevel        = 'vlev',  // int32_t

    // Set this key to enable authoring files in 64-bit offset
    kKey64BitFileOffset   = 'fobt',  // int32_t (bool)
    kKey2ByteNalLength    = '2NAL',  // int32_t (bool)

    // Identify the file output format for authoring
    // Please see <media/mediarecorder.h> for the supported
    // file output formats.
    kKeyFileType          = 'ftyp',  // int32_t

    // Track authoring progress status
    // kKeyTrackTimeStatus is used to track progress in elapsed time
    kKeyTrackTimeStatus   = 'tktm',  // int64_t

    kKeyNotRealTime       = 'ntrt',  // bool (int32_t)

    // Ogg files can be tagged to be automatically looping...
    kKeyAutoLoop          = 'autL',  // bool (int32_t)

    kKeyValidSamples      = 'valD',  // int32_t

    kKeyIsUnreadable      = 'unre',  // bool (int32_t)
#ifndef ANDROID_DEFAULT_CODE
    kKeyVideoPreCheck	  = 'vpck',	 //int32_t(bool)
    kKeyAudioPadEnable	  = 'apEn',	 //int32_t(bool),hai.li
    kKeyMaxQueueBuffer    = 'mque',  //int32_t, Demon Deng for OMXCodec
    kKeyAacObjType         = 'aaco',    // Morris Yang for MPEG4 audio object type
    kKeySDP               = 'ksdp',  //int32_t, Demon Deng for SDP
    kKeyRvActualWidth     =  'rvaw', // int32_t, Morris Yang for RV
    kKeyRvActualHeight    =  'rvah', // int32_t, Morris Yang for RV
    kKeyServerTimeout     = 'srvt',  //int32_t, Demon Deng for RTSP Server timeout
    kKeyIs3gpBrand		  = '3gpB',  //int32_t(bool), hai.li
    kKeyIsQTBrand		  = 'qtBd',  //int32_t(bool), hai.li
    kKeyFirstSampleOffset = 'FSOf',  //int64_t, hai.li
    kKeyMPEG4VOS			  = 'MP4C',  //raw data, hai.li for other container support mpeg4 codec
    kKeyRTSPSeekMode      = 'rskm',  //int32_t, Demon Deng for RTSP Seek Mode
    kKeyInputBufferNum    = 'inbf',  //int32_t, Demon Deng for OMXCodec
    kKeyOutputBufferNum   = 'onbf',  //int32_t,for VE
    kKeyHasUnsupportVideo = 'UnSV',  //int32_t(bool), hai.li, file has unsupport video track.
    kKeyRTPTarget         = 'rtpt',  //int32_t, Demon Deng for ARTPWriter
    kKeyCodecInfoIsInFirstFrame = 'CIFF', //int32(bool), hai.li,codec info is in the first frame 
    kKeyCamMemInfo        = 'CMIf',  // int32_t, Morris Yang for OMXVEnc With Camera 
    kKeyCamMemVa		  = 'CMVa',	 //int32_t, camera yuv buffer virtual address
    kKeyCamMemSize		  = 'CMSz',  //int32_t, camera yuv buffer size
    kKeyCamMemCount		  = 'CMCt',  //int32_t, camera yuv buffer count
    kKeyOutBufSize        = 'inbuf',//int32_t,for OMX Output Buffer Size
    kKeyFrameNum          = 'frnu',//int32_t,for mp3 output buffer frame limit.
    kKeySamplesperframe      = 'sapf', // int32_t samples per frame
    kKeyHLSVideoFirestRender   = 'v1Rn', //int64, timestamp, http live
#endif
    // An indication that a video buffer has been rendered.
    kKeyRendered          = 'rend',  // bool (int32_t)

    // The language code for this media
    kKeyMediaLanguage     = 'lang',  // cstring

    // To store the timed text format data
    kKeyTextFormatData    = 'text',  // raw data

    kKeyRequiresSecureBuffers = 'secu',  // bool (int32_t)

#ifndef ANDROID_DEFAULT_CODE

#ifdef MTK_AUDIO_APE_SUPPORT
    kkeyComptype            = 'ctyp',   // int16_t compress type  
    kkeyApechl              = 'chls',   // int16_t compress type
    kkeyApebit              = 'bits',   // int16_t compress type
    kKeyTotalFrame         = 'alls',  // int32_t all frame in file
    kKeyFinalSample         = 'fins', // int32_t last frame's sample
    kKeyBufferSize            = 'bufs',  //int32_t buffer size for ape
    kKeyNemFrame            = 'nfrm',  //int32_t seek frame's numbers for ape
    kKeySeekByte            = 'sekB',  //int32_t new seek first byte  for ape
    kKeyApeFlag            = 'apef',  //int32_t new seek first byte  for ape
#endif
   kKeyVorbisFlag 		   = 'vorbisf',  //vorbis flag
//#ifdef MTK_CMMB_SUPPORT
    //MTK_OP01_PROTECT_START
    kKeyIsCmmb            = 'cmmb', //bool (int32_t) is cmmb or not
    //MTK_OP01_PROTECT_END
//#endif
#ifdef MTK_RMVB_PLAYBACK_SUPPORT
    kKeyRAC              = 'racs',  // ra codec specific data
    kKeyRVC              = 'rvcs',  // rv codec specific data
#endif
#ifdef MTK_ASF_PLAYBACK_SUPPORT
    kKeyWMAC              = 'wmac',  // wma codec specific data
    kKeyWMVC              = 'wmvc',  // wmv codec specific data
#endif

	kKeyCodecConfigInfo    = 'cinf',  // raw data
	kkeyOmxTimeSource      = 'omts', 
    kKeySupportTryRead     = 'tryR', //in32_t try read is supported
	kKeyIsAACADIF		   = 'adif',  // int32_t (bool)
	kKeyAacProfile         = 'prof', // int32_t aac profile
	kKeyDataSourceObserver = 'dsob',	  //pointer, pointer of awesomeplayer weiguo
	kKeyHasSEIBuffer	  = 'SEIB', //bool (int32_t)
    kKeyFlacMetaInfo          = 'FMIF',  //Flac metadata info
#ifdef MTK_S3D_SUPPORT
    kKeyVideoStereoMode   = 'VStM', //int32_t video 3d mode
#endif
    kKeyAspectRatioWidth    = 'aspw',
    kKeyAspectRatioHeight   = 'asph',
#endif // #ifndef ANDROID_DEFAULT_CODE
};

enum {
    kTypeESDS        = 'esds',
    kTypeAVCC        = 'avcc',
    kTypeD263        = 'd263',
};

#ifndef ANDROID_DEFAULT_CODE
#ifdef MTK_S3D_SUPPORT
enum video_stereo_mode{
	VIDEO_STEREO_DEFAULT = -1,
	VIDEO_STEREO_2D = 0,
	VIDEO_STEREO_FRAME_SEQUENCE = 1,
	VIDEO_STEREO_SIDE_BY_SIDE = 2,
	VIDEO_STEREO_TOP_BOTTOM = 3,
	VIDEO_STEREO_LIST_END
};
#endif
#endif

class MetaData : public RefBase {
public:
    MetaData();
    MetaData(const MetaData &from);

    enum Type {
        TYPE_NONE     = 'none',
        TYPE_C_STRING = 'cstr',
        TYPE_INT32    = 'in32',
        TYPE_INT64    = 'in64',
        TYPE_FLOAT    = 'floa',
        TYPE_POINTER  = 'ptr ',
        TYPE_RECT     = 'rect',
    };

    void clear();
    bool remove(uint32_t key);

    bool setCString(uint32_t key, const char *value);
    bool setInt32(uint32_t key, int32_t value);
    bool setInt64(uint32_t key, int64_t value);
    bool setFloat(uint32_t key, float value);
    bool setPointer(uint32_t key, void *value);

    bool setRect(
            uint32_t key,
            int32_t left, int32_t top,
            int32_t right, int32_t bottom);

    bool findCString(uint32_t key, const char **value);
    bool findInt32(uint32_t key, int32_t *value);
    bool findInt64(uint32_t key, int64_t *value);
    bool findFloat(uint32_t key, float *value);
    bool findPointer(uint32_t key, void **value);

    bool findRect(
            uint32_t key,
            int32_t *left, int32_t *top,
            int32_t *right, int32_t *bottom);

    bool setData(uint32_t key, uint32_t type, const void *data, size_t size);

    bool findData(uint32_t key, uint32_t *type,
                  const void **data, size_t *size) const;

protected:
    virtual ~MetaData();

private:
    struct typed_data {
        typed_data();
        ~typed_data();

        typed_data(const MetaData::typed_data &);
        typed_data &operator=(const MetaData::typed_data &);

        void clear();
        void setData(uint32_t type, const void *data, size_t size);
        void getData(uint32_t *type, const void **data, size_t *size) const;

    private:
        uint32_t mType;
        size_t mSize;

        union {
            void *ext_data;
            float reservoir;
        } u;

        bool usesReservoir() const {
            return mSize <= sizeof(u.reservoir);
        }

        void allocateStorage(size_t size);
        void freeStorage();

        void *storage() {
            return usesReservoir() ? &u.reservoir : u.ext_data;
        }

        const void *storage() const {
            return usesReservoir() ? &u.reservoir : u.ext_data;
        }
    };

    struct Rect {
        int32_t mLeft, mTop, mRight, mBottom;
    };

    KeyedVector<uint32_t, typed_data> mItems;

    // MetaData &operator=(const MetaData &);
};

}  // namespace android

#endif  // META_DATA_H_
