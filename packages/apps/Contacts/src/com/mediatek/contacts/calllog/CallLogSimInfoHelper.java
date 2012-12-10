package com.mediatek.contacts.calllog;

import java.util.List;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.android.contacts.ContactsUtils;
import android.provider.Telephony.SIMInfo;
import android.util.Log;

import com.android.contacts.R;

public class CallLogSimInfoHelper {

    static final int SLOT_ID_FIRST = 0;
    static final int SLOT_ID_SECOND = 1;
    
    private static final String TAG = "CallLogSimInfoHelper";

    private Resources mResources;
    private String mSipCallDisplayName = "";
    private Drawable mDrawableSimSipColor;
    private Drawable mDrawableSimLockedColor;
    private Drawable mDrawableSimColor1;
    private Drawable mDrawableSimColor2;
    private int mInsertSimColor1 = -1;
    private int mInsertSimColor2 = -1;

    public CallLogSimInfoHelper(Resources resources) {
        mResources = resources;
    }

    public String getSimDisplayNameById(int simId) {
        if (ContactsUtils.CALL_TYPE_SIP == simId) {
            if ("".equals(mSipCallDisplayName)) {
            	mSipCallDisplayName = mResources.getString(R.string.call_sipcall);
            }
            return mSipCallDisplayName;
        } else if (ContactsUtils.CALL_TYPE_NONE == simId) {
            return "";
        } else {
            return SIMInfoWrapper.getDefault().getSimDisplayNameById(simId);
        }
    }

    public Drawable getSimColorDrawableById(int simId) {
    	log("getSimColorDrawableById() simId == [" + simId +"]" );
    	int mCalllogSimnameHeight = (int) mResources
    	.getDimension(R.dimen.calllog_list_item_simname_height);
        if (ContactsUtils.CALL_TYPE_SIP == simId) {
            // The request is sip color
            if (null == mDrawableSimSipColor) {
//                mDrawableSimSipColor = mResources.getDrawable(com.mediatek.internal.R.drawable.sim_background_sip);
        		Bitmap bitmap = BitmapFactory.decodeResource(mResources,
        				com.mediatek.internal.R.drawable.sim_background_sip);
				bitmap = Bitmap
						.createScaledBitmap(bitmap, mCalllogSimnameHeight,
								mCalllogSimnameHeight, false);
				mDrawableSimSipColor = new BitmapDrawable(mResources, bitmap);
            }
            return mDrawableSimSipColor.getConstantState().newDrawable();
        } else if (ContactsUtils.CALL_TYPE_NONE == simId) {
            return null;
        } else {
            int color = SIMInfoWrapper.getDefault().getInsertedSimColorById(simId);
            log("getSimColorDrawableById() color == [" + color +"]" );
            if (-1 != color) {
                if (SLOT_ID_FIRST == SIMInfoWrapper.getDefault().getSlotIdBySimId(simId)) {
                    if (null == mDrawableSimColor1 || mInsertSimColor1 != color) {
                        int simColorResId = SIMInfoWrapper.getDefault().getSimBackgroundResByColorId(color);
                        mInsertSimColor1 = color;
                        Bitmap bitmap = BitmapFactory.decodeResource(mResources, simColorResId);
                        bitmap = Bitmap.createScaledBitmap(bitmap, mCalllogSimnameHeight, mCalllogSimnameHeight, false);
                        mDrawableSimColor1 = new BitmapDrawable(mResources, bitmap);
                    }
                    return mDrawableSimColor1;
                } else if (SLOT_ID_SECOND == SIMInfoWrapper.getDefault().getSlotIdBySimId(simId)) {
                    if (null == mDrawableSimColor2 || mInsertSimColor2 != color) {
                        int simColorResId = SIMInfoWrapper.getDefault().getSimBackgroundResByColorId(color);
                        mInsertSimColor2 = color;
                        Bitmap bitmap = BitmapFactory.decodeResource(mResources, simColorResId);
                        bitmap = Bitmap.createScaledBitmap(bitmap, mCalllogSimnameHeight, mCalllogSimnameHeight, false);
                        mDrawableSimColor2 = new BitmapDrawable(mResources, bitmap);
                    }
                    return mDrawableSimColor2;
                } else {
                    // should never been here
                    return null;
                }
            } else {
                // The request color is not inserted sim currently
                if (null == mDrawableSimLockedColor) {
//                    mDrawableSimLockedColor = 
//                        mResources.getDrawable(com.mediatek.internal.R.drawable.sim_background_locked);
        			Bitmap bitmap = BitmapFactory.decodeResource(mResources,
        					com.mediatek.internal.R.drawable.sim_background_locked);
					bitmap = Bitmap
							.createScaledBitmap(bitmap, mCalllogSimnameHeight,
									mCalllogSimnameHeight, false);
					mDrawableSimLockedColor = new BitmapDrawable(mResources, bitmap);
                }
                // Not inserted sim has same background but different length, so can not share same drawable
                return mDrawableSimLockedColor.getConstantState().newDrawable();
            }
        }
    }

    public void resetCacheInfo() {
        mDrawableSimColor1 = null;
        mDrawableSimColor2 = null;
        mInsertSimColor1 = -1;
        mInsertSimColor2 = -1;
    }

    public static int getSimIdBySlotID(final int slot) {
        List<SIMInfo> insertedSIMInfoList = SIMInfoWrapper.getDefault().getInsertedSimInfoList();

        if (null == insertedSIMInfoList) {
            return -1;
        }

        for (int i=0; i < insertedSIMInfoList.size(); ++ i) {
            if (slot == insertedSIMInfoList.get(i).mSlot) {
                return (int)insertedSIMInfoList.get(i).mSimId;
            }
        }
        return -1;
    }
    
    private void log(final String log) {
        Log.i(TAG, log);
    }
}
