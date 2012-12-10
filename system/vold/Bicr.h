/*
 * Copyright (C) 2008 The Android Open Source Project
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

#ifndef _BICR_H
#define _BICR_H


class Bicr {
private:
    int mState;
    static Bicr *sInstance;

public:
    typedef enum STATUS{
        NOT_EXIST,
        UNSHARED,
        SHARED , 
        UNSHARING, 
        SHARING, 
        STATUS_COUNT
    }BICR_STATUS;

    static const char *CD_ROM_PATH;
    static const char *CD_ROM_LUN_PATH;
    static const char *status_name[STATUS_COUNT];
    
public:
    Bicr(void);
    virtual ~Bicr(){};
    static Bicr *Instance();

    int shareCdRom();
    int unShareCdRom();
    const char* getStatus();
    int getState() { return mState; }
};

#endif
