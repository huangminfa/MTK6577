package com.android.gallery3d.app;

import android.net.Uri;

import com.android.gallery3d.util.MtkLog;
import com.android.gallery3d.util.MtkUtils;

//should split operater's info into different operator class in easy porting task.
public class DefaultMovieFeatureSwitcher implements MovieFeatureSwitcher {
    private static final String TAG = "DefaultMovieFeatureSwicher";
    private static final boolean LOG = true;
    
    public DefaultMovieFeatureSwitcher() {
    }
    
    @Override
    public boolean isEnabledStereoVideo() {
        if (MtkUtils.isSupport3d()) {
            return true;
        }
        return false;
    }
    
    @Override
    public boolean isEnabledBookmark() {
        if (MtkUtils.isCmccOperator()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isEnabledInputUri() {
        if (MtkUtils.isCmccOperator()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isEnabledLoop() {
        if (MtkUtils.isCmccOperator()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isEnabledServerDetail() {
        if (MtkUtils.isCmccOperator()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isEnabledStereoAudio() {
        if (MtkUtils.isCmccOperator()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isEnabledStreamingSettings() {
        if (MtkUtils.isCmccOperator()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isEnabledVideoList() {
        boolean enable = false;
        if (MtkUtils.isCmccOperator()) {
            enable = true;
        }
        if (LOG) MtkLog.v(TAG, "isEnabledVideoList() return " + enable);
        return enable;
    }

    @Override
    public boolean isEnabledFullscreenNotification(Uri originalUri, MovieItem movieInfo) {
        boolean enable = false;
        if ((MtkUtils.isCmccOperator() && MtkUtils.isFromMms(originalUri)) ||
                (MtkUtils.isCmccOperator() && !MtkUtils.isLocalFile(movieInfo.getUri(), movieInfo.getMimeType())) ||
                (MtkUtils.isCuOperator() && !MtkUtils.isLocalFile(movieInfo.getUri(), movieInfo.getMimeType()))) {
            enable = true;
        }
        if (LOG) MtkLog.v(TAG, "isEnabledFullscreenNotification() return " + enable);
        return enable;
    }

    @Override
    public boolean isEnabledStop() {
        //set stop function as common feature.
        return true;
    }
    
}
