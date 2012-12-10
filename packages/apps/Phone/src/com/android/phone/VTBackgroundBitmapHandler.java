package com.android.phone;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class VTBackgroundBitmapHandler {
    private Bitmap mBitmap = null;

    public void recycle() {
        if (null != mBitmap) {
            mBitmap.recycle();
        }
        mBitmap = null;
    }
    
    public void forceUpdateBitmapBySetting() {
        recycle();
        if (VTSettingUtils.getInstance().mPicToReplacePeer
                                .equals(VTAdvancedSetting.SELECT_DEFAULT_PICTURE2)) {
            mBitmap = BitmapFactory.decodeFile(VTAdvancedSetting.getPicPathDefault2());
        } else if (VTSettingUtils.getInstance().mPicToReplacePeer
                                .equals(VTAdvancedSetting.SELECT_MY_PICTURE2)) {
            mBitmap = BitmapFactory.decodeFile(VTAdvancedSetting.getPicPathUserselect2());
        }
    }
    
    public void updateBitmapBySetting() {
        if (null != mBitmap) {
            return;
        }
        forceUpdateBitmapBySetting();
    }
    
    public Bitmap getBitmap() {
        return mBitmap;
    }
}
