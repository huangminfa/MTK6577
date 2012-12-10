/*
**
** Copyright 2007, The Android Open Source Project
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

#include <cutils/properties.h>
#include <string.h>
#include <unistd.h>
//#define LOG_NDEBUG 0

#define LOG_TAG "AudioHardwareInterface"
#include <utils/Log.h>
#include <utils/String8.h>

#include "AudioHardwareStub.h"
#include "AudioHardwareGeneric.h"
#ifdef WITH_A2DP
#include "A2dpAudioInterface.h"
#endif

#ifdef ENABLE_AUDIO_DUMP
#include "AudioDumpInterface.h"
#endif


// change to 1 to log routing calls
#define LOG_ROUTING_CALLS 1

namespace android_audio_legacy {

#if LOG_ROUTING_CALLS
static const char* routingModeStrings[] =
{
    "OUT OF RANGE",
    "INVALID",
    "CURRENT",
    "NORMAL",
    "RINGTONE",
    "IN_CALL",
    "IN_COMMUNICATION"
};

static const char* routeNone = "NONE";

static const char* displayMode(int mode)
{
    if ((mode < AudioSystem::MODE_INVALID) || (mode >= AudioSystem::NUM_MODES))
        return routingModeStrings[0];
    return routingModeStrings[mode+3];
}
#endif

// ----------------------------------------------------------------------------

AudioHardwareInterface* AudioHardwareInterface::create()
{
    return NULL;
}

AudioStreamOut::~AudioStreamOut()
{
}

AudioStreamIn::~AudioStreamIn() {}

AudioHardwareBase::AudioHardwareBase()
{
    mMode = 0;
}

status_t AudioHardwareBase::setMode(int mode)
{
#if LOG_ROUTING_CALLS
    LOGD("setMode(%s)", displayMode(mode));
#endif
    if ((mode < 0) || (mode >= AudioSystem::NUM_MODES))
        return BAD_VALUE;
    if (mMode == mode)
        return ALREADY_EXISTS;
    mMode = mode;
    return NO_ERROR;
}

// add by chipeng to add EM parameter
 status_t AudioHardwareBase::SetEMParameter(void *ptr , int len)
 {
     return NO_ERROR;
 }
 status_t AudioHardwareBase::GetEMParameter(void *ptr , int len)
 {
     return NO_ERROR;
 }
 status_t AudioHardwareBase::SetAudioCommand(int par1, int par2)
 {
     return NO_ERROR;
 }
 status_t AudioHardwareBase::GetAudioCommand(int par1)
 {
     return NO_ERROR;
 }
 status_t AudioHardwareBase::SetAudioData(int par1,size_t len,void *ptr)
 {
     return NO_ERROR;
 }
 status_t AudioHardwareBase::GetAudioData(int par1,size_t len,void *ptr)
 {
     return NO_ERROR;
 }
 // add by Tina to set ACF Preview parameter
 status_t AudioHardwareBase::SetACFPreviewParameter(void *ptr , int len)
 {
     return NO_ERROR;
 }
 status_t AudioHardwareBase::SetHCFPreviewParameter(void *ptr , int len)
 {
     return NO_ERROR;
 }
 /////////////////////////////////////////////////////////////////////////
 //    for PCMxWay Interface API ...   Stan
 /////////////////////////////////////////////////////////////////////////
 int AudioHardwareBase::xWayPlay_Start(int sample_rate)
 {
     return NO_ERROR;
 }
 int AudioHardwareBase::xWayPlay_Stop(void)
 {
     return NO_ERROR;
 }
 int AudioHardwareBase::xWayPlay_Write(void *buffer, int size_bytes)
 {
     return NO_ERROR;
 }
 int AudioHardwareBase::xWayPlay_GetFreeBufferCount(void)
 {
     return NO_ERROR;
 }
int AudioHardwareBase::xWayRec_Start(int sample_rate)
 {
     return NO_ERROR;
 }
 int AudioHardwareBase::xWayRec_Stop(void)
 {
     return NO_ERROR;
 }
 int AudioHardwareBase::xWayRec_Read(void *buffer, int size_bytes)
 {
     return NO_ERROR;
 }

// default implementation
status_t AudioHardwareBase::setParameters(const String8& keyValuePairs)
{
    return NO_ERROR;
}

// default implementation
String8 AudioHardwareBase::getParameters(const String8& keys)
{
    AudioParameter param = AudioParameter(keys);
    return param.toString();
}

// default implementation
size_t AudioHardwareBase::getInputBufferSize(uint32_t sampleRate, int format, int channelCount)
{
    if (sampleRate != 8000) {
        LOGW("getInputBufferSize bad sampling rate: %d", sampleRate);
        return 0;
    }
    if (format != AudioSystem::PCM_16_BIT) {
        LOGW("getInputBufferSize bad format: %d", format);
        return 0;
    }
    if (channelCount != 1) {
        LOGW("getInputBufferSize bad channel count: %d", channelCount);
        return 0;
    }

    return 320;
}

status_t AudioHardwareBase::dumpState(int fd, const Vector<String16>& args)
{
    const size_t SIZE = 256;
    char buffer[SIZE];
    String8 result;
    snprintf(buffer, SIZE, "AudioHardwareBase::dumpState\n");
    result.append(buffer);
    snprintf(buffer, SIZE, "\tmMode: %d\n", mMode);
    result.append(buffer);
    ::write(fd, result.string(), result.size());
    dump(fd, args);  // Dump the state of the concrete child.
    return NO_ERROR;
}

// ----------------------------------------------------------------------------

}; // namespace android
