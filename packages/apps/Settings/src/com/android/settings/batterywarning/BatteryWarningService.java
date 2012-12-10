package com.android.settings.batterywarning;

import java.util.Timer;

import com.android.settings.R;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
//import android.os.IPowerManager;
import android.os.PowerManager;
//import android.os.RemoteException;
//import android.os.ServiceManager;
//import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.mediatek.xlog.Xlog;

/**
 * @author mtk80968
 *
 */
public class BatteryWarningService extends Service {
    
	private static final String XLOGTAG = "Settings/BW";
	private static final String TAG = "WarningMessage:";
//    private static final String TAG = "BatteryWariningService";
    
    private static final String FILE_BATTERY_NOTIFY_CODE = 
        "/sys/devices/platform/mt-battery/BatteryNotify";
    private static Timer mTimer;
    private BatteryWarningMessageReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private static final Uri WARNING_SOUND_URI = Uri.parse("file:///system/media/audio/ui/VideoRecord.ogg");
    public static AlertDialog mChargerOverVoltageDialog; 
    public static AlertDialog mBatteryOverTemperatureDialog; 
    public static AlertDialog mOverCurrentProtectionDialog; 
    public static AlertDialog mBatteryrOverVoltageDialog; 
    public static AlertDialog mSatetyTimerTimeoutDialog; 
    
    private static boolean mShowChargerOverVoltageDialog = true;
    private static boolean mShowBatteryOverTemperatureDialog = true;
    private static boolean mShowOverCurrentProtectionDialog = true;
    private static boolean mShowBatteryrOverVoltageDialog = true;
    private static boolean mShowSatetyTimerTimeoutDialog = true;
    private static Ringtone mRingtone;
	
//    private PowerManager mPowerManager;
//    private static boolean mIsScreenOn = true;
	enum WarningType {
			CHARGER_OVER_VOL,
			BATTERY_OVER_TEMP,
			OVER_CUR_PROTENCTION,
			BATTERY_OVER_VOL,
			SAFETY_TIMEOUT
		}
	// Note: Must match the sequence of the WarningType

    private static int[] sWarningTitle = new int[] {
        R.string.title_charger_over_voltage,
        R.string.title_battery_over_temperature,
        R.string.title_over_current_protection,
        R.string.title_battery_over_voltage,
        R.string.title_safety_timer_timeout
    };
	    private static int[] sWarningMsg = new int[] {
        R.string.msg_charger_over_voltage,
        R.string.msg_battery_over_temperature,
        R.string.msg_over_current_protection,
        R.string.msg_battery_over_voltage,
        R.string.msg_safety_timer_timeout
    };
    @Override
    public void onCreate() {
        super.onCreate();
        Xlog.d(XLOGTAG, TAG+"Battery Warning Service: onCreate()");
        
        mTimer = new Timer();
        mTimer.schedule(new ReadCodeTask(BatteryWarningService.this, FILE_BATTERY_NOTIFY_CODE), 10000, 10000);
        
        Xlog.d(XLOGTAG, TAG+"Task schedule started: delay--" + 10000 + " period--" + 10000);
        
//        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        mReceiver = new BatteryWarningMessageReceiver();
        mIntentFilter = new IntentFilter(BatteryNotifyCodes.ACTION_CHARGER_OVER_VOLTAGE);
        mIntentFilter.addAction(BatteryNotifyCodes.ACTION_BATTER_OVER_TEMPERATURE);
        mIntentFilter.addAction(BatteryNotifyCodes.ACTION_OVER_CURRENT_PROTECTION);
        mIntentFilter.addAction(BatteryNotifyCodes.ACTION_BATTER_OVER_VOLTAGE);
        mIntentFilter.addAction(BatteryNotifyCodes.ACTION_SAFETY_TIMER_TIMEOUT);
        mIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        
        registerReceiver(mReceiver, mIntentFilter);
    }


    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mTimer.cancel();
        mTimer = null;
        unregisterReceiver(mReceiver);
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public static class BatteryWarningMessageReceiver extends BroadcastReceiver{
//        PowerManager mPowerManager;
//        boolean mIsScreenOn = true;
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            String action = intent.getAction();
            if(action.equals(BatteryNotifyCodes.ACTION_CHARGER_OVER_VOLTAGE)){
                Xlog.d(XLOGTAG, TAG+"receiver: over charger voltage, please disconnect charger");
                showWarningDialog(context,mShowChargerOverVoltageDialog,mChargerOverVoltageDialog,WarningType.CHARGER_OVER_VOL);
            }
            else if(action.equals(BatteryNotifyCodes.ACTION_BATTER_OVER_TEMPERATURE)){
                Xlog.d(XLOGTAG, TAG+"receiver: over battery temperature, please remove battery");
                showWarningDialog(context,mShowBatteryOverTemperatureDialog,mBatteryOverTemperatureDialog,WarningType.BATTERY_OVER_TEMP);                
            }
            else if(action.equals(BatteryNotifyCodes.ACTION_OVER_CURRENT_PROTECTION)){
                Xlog.d(XLOGTAG, TAG+"receiver: over current-protection, please disconnect charger"+ mShowOverCurrentProtectionDialog);
                showWarningDialog(context,mShowOverCurrentProtectionDialog,mOverCurrentProtectionDialog,WarningType.OVER_CUR_PROTENCTION);                
            }
            else if(action.equals(BatteryNotifyCodes.ACTION_BATTER_OVER_VOLTAGE)){
                Xlog.d(XLOGTAG, TAG+"receiver: over battery voltage, please remove battery");
                showWarningDialog(context,mShowBatteryrOverVoltageDialog,mBatteryrOverVoltageDialog,WarningType.BATTERY_OVER_VOL);
            }
            else if(action.equals(BatteryNotifyCodes.ACTION_SAFETY_TIMER_TIMEOUT)){
                Xlog.d(XLOGTAG, TAG+"receiver: over 12 hours, battery does not charge full, please disconnect charger");
                showWarningDialog(context,mShowSatetyTimerTimeoutDialog,mSatetyTimerTimeoutDialog,WarningType.SAFETY_TIMEOUT);  
            }
            else if(action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                if(mChargerOverVoltageDialog != null && mChargerOverVoltageDialog.isShowing()){
                    mChargerOverVoltageDialog.dismiss();
                }
                if(mSatetyTimerTimeoutDialog != null && mSatetyTimerTimeoutDialog.isShowing()){
                    mSatetyTimerTimeoutDialog.dismiss();
                }
            }
            
        }//end of onReceive()
        
    }
    
    /**
     * 
     * @param context The Context that had been passed to {@link #warningMessageDialog(Context, int, int, int)}
     * @param titleResId Set the title using the given resource id.
     * @param messageResId Set the message using the given resource id.
     * @param imageResId Set the image using the given resource id.
     * @return Creates a {@link AlertDialog} with the arguments supplied to this builder. 
     */
    static AlertDialog warningMessageDialog(Context context, int titleResId, int messageResId, int imageResId){
        
        View view = View.inflate(context, R.layout.battery_warning, null);
        TextView mMessageView = (TextView)view.findViewById(R.id.subtitle);
        mMessageView.setText(messageResId);
        ImageView mImageView = (ImageView)view.findViewById(R.id.image);
        mImageView.setImageResource(imageResId);
        
        AlertDialog.Builder mBatteryWarning = new AlertDialog.Builder(context);
        mBatteryWarning.setCancelable(false);
        mBatteryWarning.setTitle(titleResId);
        mBatteryWarning.setView(view);
        mBatteryWarning.setIconAttribute(android.R.attr.alertDialogIcon);
        mBatteryWarning.setPositiveButton(R.string.btn_ok_msg, mOnPositiveButtonClickListener);
        mBatteryWarning.setNegativeButton(R.string.btn_cancel_msg, mOnNegativeButtonClickListener);

        return mBatteryWarning.create();
    }
    
    /**
     * 
     * @param context The Context that had been passed to {@link #warningMessageDialog(Context, Uri)}
     * @param defaultUri
     */
    
    static void playAlertSound(Context context, Uri defaultUri){
        
        if(defaultUri != null) {
            mRingtone = RingtoneManager.getRingtone(context, defaultUri);
            if (mRingtone != null) {
                mRingtone.setStreamType(AudioManager.STREAM_SYSTEM);
                mRingtone.play();
            }
        }
    }

	static void showWarningDialog(Context context,boolean showDialog,AlertDialog warningDialog,WarningType dialogType) {
		 Xlog.d(XLOGTAG, TAG+"showDialog:"+showDialog);
		 WarningType mWarningType = dialogType;
		 if(showDialog){
		    if(warningDialog != null && warningDialog.isShowing()){
		       warningDialog.dismiss();
		    }
		     warningDialog = 
			  	warningMessageDialog(context, 
			  	sWarningTitle[mWarningType.ordinal()], 
			  	sWarningMsg[mWarningType.ordinal()],
			  	R.drawable.battery_low_battery);
                     warningDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                     warningDialog.show();
                     playAlertSound(context, WARNING_SOUND_URI);
                     Xlog.d(XLOGTAG, TAG+"create & show dialog:"+dialogType);        
               }
		switch(mWarningType){
			case CHARGER_OVER_VOL:
				mChargerOverVoltageDialog = warningDialog;
				break;
			case BATTERY_OVER_TEMP:
				mBatteryOverTemperatureDialog = warningDialog;
				break;
			case OVER_CUR_PROTENCTION:
				mOverCurrentProtectionDialog = warningDialog;
				break;
			case BATTERY_OVER_VOL:
				mBatteryrOverVoltageDialog = warningDialog;
				break;
			case SAFETY_TIMEOUT:
				mSatetyTimerTimeoutDialog = warningDialog;
				break;
		}
	}

    public static OnClickListener mOnNegativeButtonClickListener = new OnClickListener(){
        public void onClick(DialogInterface a0, int a1) {
			Xlog.d(XLOGTAG, TAG+"Dismiss dialog"+a0);
        	if( mRingtone != null){
        		mRingtone.stop();
        	}
            if(a0.equals(mChargerOverVoltageDialog)){
                mShowChargerOverVoltageDialog = false;
            } else if(a0.equals(mBatteryOverTemperatureDialog)){
                mShowBatteryOverTemperatureDialog = false;
            } else if(a0.equals(mOverCurrentProtectionDialog)){
                mShowOverCurrentProtectionDialog = false;
            } else if(a0.equals(mBatteryrOverVoltageDialog)){
                mShowBatteryrOverVoltageDialog = false;
            } else if(a0.equals(mSatetyTimerTimeoutDialog)){
                mShowSatetyTimerTimeoutDialog = false;
            }
        }
    };
    
    public static OnClickListener mOnPositiveButtonClickListener = new OnClickListener(){
        public void onClick(DialogInterface a0, int a1) {
			Xlog.d(XLOGTAG, TAG+"positive Button later nofitication dialog"+a0);
        	if( mRingtone != null){
        		mRingtone.stop();
        	}
        }
    };  

}
