package com.android.phone;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.android.internal.telephony.Phone;
import com.android.phone.PhoneFeatureConstants.FeatureOption;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Telephony.SIMInfo;
import com.mediatek.xlog.Xlog;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;


public class VTAdvancedSetting extends PreferenceActivity implements Preference.OnPreferenceChangeListener{

    //In current stage, we consider that only the card slot 0 support wcdma
    static public final int VT_CARD_SLOT = 0;
    private static final String BUTTON_VT_REPLACE_KEY     = "button_vt_replace_expand_key";
    private static final String SELECT_DEFAULT_PICTURE    = "0";
    private static final String SELECT_MY_PICTURE         = "2";
    
    public static final String SELECT_DEFAULT_PICTURE2    = "0";
    public static final String SELECT_MY_PICTURE2         = "1";
    
    public static final String NAME_PIC_TO_REPLACE_LOCAL_VIDEO_USERSELECT = "pic_to_replace_local_video_userselect";
    public static final String NAME_PIC_TO_REPLACE_LOCAL_VIDEO_DEFAULT = "pic_to_replace_local_video_default";
    public static final String NAME_PIC_TO_REPLACE_PEER_VIDEO_USERSELECT = "pic_to_replace_peer_video_userselect";
    public static final String NAME_PIC_TO_REPLACE_PEER_VIDEO_DEFAULT = "pic_to_replace_peer_video_default";   

    /** The launch code when picking a photo and the raw data is returned */
    public static final int REQUESTCODE_PICTRUE_PICKED_WITH_DATA = 3021;
    private ListPreference mButtonVTReplace;
    private ListPreference mButtonVTPeerReplace;
    private int mWhichToSave = 0;
    
    // debug data
    private static final String LOG_TAG = "Settings/VTAdvancedSetting";
    private static final boolean DBG = true; // (PhoneApp.DBG_LEVEL >= 2);
    
    //Operator customization: SS used data
    private int mSimId = VT_CARD_SLOT; 
    private static final String BUTTON_VT_CF_KEY = "button_cf_expand_key";
    private static final String BUTTON_VT_CB_KEY = "button_cb_expand_key";
    private static final String BUTTON_VT_MORE_KEY = "button_more_expand_key";
    
    private static final String BUTTON_VT_PEER_REPLACE_KEY = "button_vt_replace_peer_expand_key";
    private static final String BUTTON_VT_ENABLE_PEER_REPLACE_KEY = "button_vt_enable_peer_replace_key";
    private static final String BUTTON_VT_ENABLE_BACK_CAMERA_KEY     = "button_vt_enable_back_camera_key";
    private static final String BUTTON_VT_PEER_BIGGER_KEY     = "button_vt_peer_bigger_key";
    private static final String BUTTON_VT_MO_LOCAL_VIDEO_DISPLAY_KEY     = "button_vt_mo_local_video_display_key";
    private static final String BUTTON_VT_MT_LOCAL_VIDEO_DISPLAY_KEY     = "button_vt_mt_local_video_display_key";
    private static final String BUTTON_VT_AUTO_DROPBACK_KEY = "button_vt_auto_dropback_key";
    private static final String CHECKBOX_RING_ONLY_ONCE = "ring_only_once";
    private Preference mButtonCf = null;
    private Preference mButtonCb = null;
    private Preference mButtonMore = null;
    private CheckBoxPreference mButtonVTEnablePeerReplace;
    private CheckBoxPreference mButtonVTMoVideo;
    private CheckBoxPreference mCheckBoxRingOnlyOnce;
    private CheckBoxPreference mButtonVTEnablebackCamer;
    private CheckBoxPreference mButtonVTPeerBigger;
    private CheckBoxPreference mButtonVTAutoDropBack;
    private ListPreference mButtonVTMtVideo;
    
    private PreCheckForRunning preCfr = null;
    boolean isOnlyOneSim = false;
    
    private static void log(String msg) {
        Xlog.d(LOG_TAG, msg);
    }
    
     
    protected void onCreate(Bundle icicle) {
        
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.vt_advanced_setting);
        
        if (PhoneUtils.isSupportFeature("3G_SWITCH")) {
            this.mSimId = PhoneApp.getInstance().phoneMgr.get3GCapabilitySIM();
        }
        
        mButtonVTReplace = (ListPreference)findPreference(BUTTON_VT_REPLACE_KEY);
        mButtonVTReplace.setOnPreferenceChangeListener(this);
        mButtonVTPeerReplace = (ListPreference)findPreference(BUTTON_VT_PEER_REPLACE_KEY);
        mButtonVTPeerReplace.setOnPreferenceChangeListener(this);
        
        preCfr = new PreCheckForRunning(this);
        //Operator customization : for common project remove these buttons
        mButtonCf = (Preference)findPreference(BUTTON_VT_CF_KEY);
        mButtonCb = (Preference)findPreference(BUTTON_VT_CB_KEY);
        mButtonMore = (Preference)findPreference(BUTTON_VT_MORE_KEY);
        mButtonVTEnablePeerReplace=(CheckBoxPreference)findPreference(BUTTON_VT_ENABLE_PEER_REPLACE_KEY);
        mButtonVTMoVideo=(CheckBoxPreference)findPreference(BUTTON_VT_MO_LOCAL_VIDEO_DISPLAY_KEY);
        mButtonVTMtVideo=(ListPreference)findPreference(BUTTON_VT_MT_LOCAL_VIDEO_DISPLAY_KEY);
        mButtonVTEnablebackCamer=(CheckBoxPreference)findPreference(BUTTON_VT_ENABLE_BACK_CAMERA_KEY);
        mButtonVTPeerBigger=(CheckBoxPreference)findPreference(BUTTON_VT_PEER_BIGGER_KEY);
        mButtonVTAutoDropBack=(CheckBoxPreference)findPreference(BUTTON_VT_AUTO_DROPBACK_KEY);
        mCheckBoxRingOnlyOnce = (CheckBoxPreference)findPreference(CHECKBOX_RING_ONLY_ONCE);
        Xlog.d(LOG_TAG,"FeatureOption.MTK_VT3G324M_SUPPORT="+FeatureOption.MTK_VT3G324M_SUPPORT+"" +
        		"FeatureOption.MTK_PHONE_VT_VOICE_ANSWER="+FeatureOption.MTK_PHONE_VT_VOICE_ANSWER);
        if (!(FeatureOption.MTK_VT3G324M_SUPPORT && FeatureOption.MTK_PHONE_VT_VOICE_ANSWER)){
        	getPreferenceScreen().removePreference(mCheckBoxRingOnlyOnce);
        } 
        if (CallSettings.isMultipleSim()) {
            List<SIMInfo> list = SIMInfo.getInsertedSIMList(this);
            if (list.size() == 1) {
                this.isOnlyOneSim = true;
                this.mSimId = list.get(0).mSlot;
            }
        } else {
            this.isOnlyOneSim = true;
            this.mSimId = 0;
            preCfr.byPass = !isOnlyOneSim;
        }
    }

    public void onResume() {
        super.onResume();
        //There is only one sim inserted(dualsim with one sim or single sim)
        if (isOnlyOneSim) {
            boolean isRadioOn = CallSettings.isRadioOn(this.mSimId);            
            if (!isRadioOn) {
                mButtonCf.setEnabled(false);
                mButtonCb.setEnabled(false);
                mButtonMore.setEnabled(false);
            }
        }
        boolean is3GEnable = false;
		int Capability3G=PhoneApp.getInstance().phoneMgr.get3GCapabilitySIM();
        if(Capability3G==1 || Capability3G==0) {
            is3GEnable = true ;
        }
        updateView(is3GEnable);
    }
    
    private void updateView(boolean isEnable) {
        if((mButtonVTReplace.isEnabled() && !isEnable) ||
                (!mButtonVTReplace.isEnabled() && isEnable)) {
            mButtonVTReplace.setEnabled(isEnable);
            mButtonVTPeerReplace.setEnabled(isEnable);
            mButtonCf.setEnabled(isEnable);
            mButtonCb.setEnabled(isEnable);
            mButtonMore.setEnabled(isEnable);
            mButtonVTEnablePeerReplace.setEnabled(isEnable);
            mButtonVTMoVideo.setEnabled(isEnable);
            mButtonVTMtVideo.setEnabled(isEnable);
            mButtonVTEnablebackCamer.setEnabled(isEnable);
            mButtonVTPeerBigger.setEnabled(isEnable);
            mButtonVTAutoDropBack.setEnabled(isEnable);
        }
       
    }
    public boolean onPreferenceChange(Preference preference, Object objValue)
    {
        if (preference == mButtonVTReplace){
            
            VTCallUtils.checkVTFile();  
            mWhichToSave = 0;
            
            if(objValue.toString().equals(SELECT_DEFAULT_PICTURE)){
                if (DBG) log(" Picture for replacing local video -- selected DEFAULT PICTURE");
                showDialogDefaultPic(VTAdvancedSetting.getPicPathDefault());                
            } 
            else if(objValue.toString().equals(SELECT_MY_PICTURE)){
                if (DBG) log(" Picture for replacing local video -- selected MY PICTURE");               
                showDialogMyPic(VTAdvancedSetting.getPicPathUserselect());                
            }
        } else if (preference == mButtonVTPeerReplace){
        	
        	VTCallUtils.checkVTFile();
        	mWhichToSave = 1;
        	
        	if(objValue.toString().equals(SELECT_DEFAULT_PICTURE2)){
                if (DBG) log(" Picture for replacing peer video -- selected DEFAULT PICTURE");
                showDialogDefaultPic(VTAdvancedSetting.getPicPathDefault2());                
            } 
            else if(objValue.toString().equals(SELECT_MY_PICTURE2)){
                if (DBG) log(" Picture for replacing peer video -- selected MY PICTURE");               
                showDialogMyPic(VTAdvancedSetting.getPicPathUserselect2());                
            }
        	
        }
        
        return true;
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
        if (DBG) log("onActivityResult: requestCode = " + requestCode + ", resultCode = " +resultCode);
        
        if (resultCode != RESULT_OK) return;
        
        switch (requestCode){
        
        case REQUESTCODE_PICTRUE_PICKED_WITH_DATA:
            try {
                final Bitmap bitmap = data.getParcelableExtra("data");
				if (bitmap != null) {
					if(mWhichToSave == 0){
						VTCallUtils.saveMyBitmap(VTAdvancedSetting.getPicPathUserselect(), bitmap);
					}else{
						VTCallUtils.saveMyBitmap(VTAdvancedSetting.getPicPathUserselect2(), bitmap);
					}
					bitmap.recycle();
					if (DBG)
						log(" - Bitmap.isRecycled() : " + bitmap.isRecycled());
				}
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(mWhichToSave == 0){
            	showDialogMyPic(VTAdvancedSetting.getPicPathUserselect());  
            }else{
            	showDialogMyPic(VTAdvancedSetting.getPicPathUserselect2());  
            }
            break;
            
        }
    }
    
    private void showDialogDefaultPic(String filename)
    {
        ImageView mImg = new ImageView(this);
        final Bitmap mBitmap = BitmapFactory.decodeFile(filename);
        mImg.setImageBitmap(mBitmap);
        LinearLayout linear = new LinearLayout(this);
        linear.addView(mImg, new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        linear.setGravity(Gravity.CENTER);
        
        AlertDialog.Builder myBuilder = new AlertDialog.Builder(this);
        myBuilder.setView(linear);
        myBuilder.setTitle(R.string.vt_pic_replace_local_default);
        myBuilder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog,
                    int which) {
                mBitmap.recycle();
                if (DBG) log(" - Bitmap.isRecycled() : " + mBitmap.isRecycled() );
                return;
            }
        });
        
        AlertDialog myAlertDialog = myBuilder.create();
        myAlertDialog.show(); 
    }
    
    private void showDialogMyPic(String filename)
    {
        ImageView mImg2 = new ImageView(this);
        final Bitmap mBitmap2 = BitmapFactory.decodeFile(filename);
        mImg2.setImageBitmap(mBitmap2);
        LinearLayout linear = new LinearLayout(this);
        linear.addView(mImg2, new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        linear.setGravity(Gravity.CENTER);
        
        AlertDialog.Builder myBuilder = new AlertDialog.Builder(this);
        myBuilder.setView(linear);
        myBuilder.setTitle(R.string.vt_pic_replace_local_mypic);
        myBuilder.setPositiveButton(R.string.vt_change_my_pic,
                new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog,
                    int which) {
                // TODO Auto-generated method stub

                try {
                    Intent intent = new Intent(
                            Intent.ACTION_GET_CONTENT,
                            null);

                    intent.setType("image/*");
                    intent.putExtra("crop", "true");
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);
                    intent.putExtra("outputX",
                            getResources().getInteger(
                                    R.integer.qcif_x));
                    intent.putExtra("outputY",
                            getResources().getInteger(
                                    R.integer.qcif_y));
                    intent
                            .putExtra("return-data",
                                    true);
                    intent.putExtra("scaleUpIfNeeded", true);

                    startActivityForResult(intent,
                            REQUESTCODE_PICTRUE_PICKED_WITH_DATA);

                } catch (ActivityNotFoundException e) {
                    if (DBG)
                        log("Pictrue not found , Gallery ActivityNotFoundException !");
                }
                
                mBitmap2.recycle();
                if (DBG) log(" - Bitmap.isRecycled() : " + mBitmap2.isRecycled() );
                
                return;
            }
        });
        myBuilder.setNegativeButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog,
                    int which) {
                mBitmap2.recycle();
                if (DBG) log(" - Bitmap.isRecycled() : " + mBitmap2.isRecycled() );
                return;
            }
        });
        
        AlertDialog myAlertDialog = myBuilder.create();
        myAlertDialog.show(); 
    }
    
    
    static public String getPicPathDefault()
    {
        return "/data/data/com.android.phone/" + NAME_PIC_TO_REPLACE_LOCAL_VIDEO_DEFAULT + ".vt";
    }
    
    static public String getPicPathUserselect()
    {
        return "/data/data/com.android.phone/" + NAME_PIC_TO_REPLACE_LOCAL_VIDEO_USERSELECT + ".vt";
    }
    
    static public String getPicPathDefault2()
    {
    	return "/data/data/com.android.phone/" + NAME_PIC_TO_REPLACE_PEER_VIDEO_DEFAULT + ".vt";
    }
    
    static public String getPicPathUserselect2()
    {
    	return "/data/data/com.android.phone/" + NAME_PIC_TO_REPLACE_PEER_VIDEO_USERSELECT + ".vt";
    }
    
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mButtonCf) {
            Intent intent = new Intent(this, GsmUmtsCallForwardOptions.class);
            intent.putExtra(Phone.GEMINI_SIM_ID_KEY, mSimId);
            intent.putExtra("ISVT", true);
            //this.startActivity(intent);
            preCfr.checkToRun(intent, this.mSimId, 302);
            return true;
        } else if (preference == mButtonCb) {
            Intent intent = new Intent(this, CallBarring.class);
            intent.putExtra(Phone.GEMINI_SIM_ID_KEY, mSimId);
            intent.putExtra("ISVT", true);
            //this.startActivity(intent);
            preCfr.checkToRun(intent, this.mSimId, 302);
            return true;
        } else if (preference == mButtonMore) {
            Intent intent = new Intent(this, GsmUmtsAdditionalCallOptions.class);
            intent.putExtra(Phone.GEMINI_SIM_ID_KEY, mSimId);
            intent.putExtra("ISVT", true);
            //this.startActivity(intent);
            preCfr.checkToRun(intent, this.mSimId, 302);
            
            return true;
        }
        return false;
    }
    
    protected void onDestroy() {
        super.onDestroy();
        if (preCfr != null) {
            preCfr.deRegister();
        }
    }
}
