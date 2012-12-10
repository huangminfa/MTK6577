package com.android.settings.schpwronoff;

import com.android.settings.R;

import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.database.Cursor;
import android.os.Parcel;
import android.os.PowerManager;
import android.widget.Toast;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.IWindowManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.android.internal.app.ShutdownThread;

import com.mediatek.xlog.Xlog;

/**
 * Glue class: connects AlarmAlert IntentReceiver to AlarmAlert
 * activity.  Passes through Alarm ID.
 */
public class SchPwrOffReceiver extends BroadcastReceiver {
	private static final String TAG="SchPwrOffReceiver";
    /** If the alarm is older than STALE_WINDOW seconds, ignore.  It
        is probably the result of a time or timezone change */
    private final static int STALE_WINDOW = 60 * 30;
    private final static String  SHUTDOWN_IPO = "android.intent.action.ACTION_SHUTDOWN_IPO";
    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
    	Xlog.d(TAG,"SchPwrOffReceiver's intent action "+String.valueOf(intent.getAction()));
    	 if (SHUTDOWN_IPO.equals(intent.getAction())){
    		if(ShutdownActivity.mCountDownTimer != null){
    			Xlog.d(TAG,"SchPwrOffReceiver , ShutdownActivity.mCountDownTimer != null");
    			ShutdownActivity.mCountDownTimer.cancel();
    			ShutdownActivity.mCountDownTimer = null;
    		}else{
    			Xlog.d(TAG,"SchPwrOffReceiver , ShutdownActivity.mCountDownTimer == null");	
    		}
         	SchPwrWakeLock.releaseCpuLock();
         	return;
         }
    	 
        mContext = context;
        Alarm alarm = null;
        // Grab the alarm from the intent. Since the remote AlarmManagerService
        // fills in the Intent to add some extra data, it must unparcel the
        // Alarm object. It throws a ClassNotFoundException when unparcelling.
        // To avoid this, do the marshalling ourselves.
        final byte[] data = intent.getByteArrayExtra(Alarms.ALARM_RAW_DATA);
        if (data != null) {
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            alarm = Alarm.CREATOR.createFromParcel(in);
        }

        if (alarm == null) {
            Xlog.d(TAG,"SchPwrOffReceiver failed to parse the alarm from the intent");
            return;
        }

        // Intentionally verbose: always log the alarm time to provide useful
        // information in bug reports.
        long now = System.currentTimeMillis();
        SimpleDateFormat format =
                new SimpleDateFormat("HH:mm:ss.SSS aaa");
        Xlog.d(TAG,"SchPwrOffReceiver.onReceive() id " + alarm.id + " setFor "
                + format.format(new Date(alarm.time)));

        if (now > alarm.time + STALE_WINDOW * 1000) {
            Xlog.d(TAG,"SchPwrOffReceiver ignoring stale alarm");
            return;
        }

        // Maintain a cpu wake lock until the AlarmAlert and AlarmKlaxon can
        // pick it up.
        //AlarmAlertWakeLock.acquireCpuWakeLock(context);

        /* Close dialogs and window shade */
        Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(closeDialogs);

        
        // Decide which activity to start based on the state of the keyguard.

        if (alarm.id == 1) // power on
        {
            Xlog.d(TAG,"SchPwrOffReceiver.onReceive() id " + alarm.id + " get power on time out ");
        }
        else if (alarm.id == 2) // power off
        {
            if (SchPwrOnOffService.isInCall != true)
            {
                Intent i = new Intent(context,ShutdownActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                PendingIntent pendingIntent = PendingIntent.getActivity(context,getResultCode(),i,PendingIntent.FLAG_ONE_SHOT);
                AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                am.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+900,pendingIntent);
            }
            else
            {
                Xlog.d(TAG,"SchPwrOffReceiver.onReceive() id " + alarm.id + " in call ");
            }

            // Disable this alarm if it does not repeat.
            if (!alarm.daysOfWeek.isRepeatSet()) {
                Xlog.d(TAG,"SchPwrOffReceiver.onReceive(): isRepeatSet() ");
                Alarms.enableAlarm(context, alarm.id, false);
            } else {
                // Enable the next alert if there is one. The above call to
                // enableAlarm will call setNextAlert so avoid calling it twice.
                Xlog.d(TAG,"SchPwrOffReceiver.onReceive(): not isRepeatSet()");
                Alarms.setNextAlertPowerOff(context);
            }
        }
    }

    private void fireShutDown() {
        Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
        intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
}
