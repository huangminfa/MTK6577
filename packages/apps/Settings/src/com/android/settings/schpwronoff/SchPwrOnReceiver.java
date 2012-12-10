package com.android.settings.schpwronoff;

import com.android.settings.R;

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
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.android.internal.app.ShutdownThread;

import com.mediatek.xlog.Xlog;

/**
 * Glue class: connects AlarmAlert IntentReceiver to AlarmAlert
 * activity.  Passes through Alarm ID.
 */
public class SchPwrOnReceiver extends BroadcastReceiver {
	private static final String TAG="SchPwrOnReceiver";
    /** If the alarm is older than STALE_WINDOW seconds, ignore.  It
        is probably the result of a time or timezone change */
    private final static int STALE_WINDOW = 60 * 30;

    @Override
    public void onReceive(Context context, Intent intent) {
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
            Xlog.d(TAG,"SchPwrOnReceiver failed to parse the alarm from the intent");
            return;
        }

        // Intentionally verbose: always log the alarm time to provide useful
        // information in bug reports.
        long now = System.currentTimeMillis();
        SimpleDateFormat format =
                new SimpleDateFormat("HH:mm:ss.SSS aaa");
        Xlog.d(TAG,"SchPwrOnReceiver.onReceive() id " + alarm.id + " setFor "
                + format.format(new Date(alarm.time)));

        if (now > alarm.time + STALE_WINDOW * 1000) {
            Xlog.d(TAG,"SchPwrOnReceiver ignoring stale alarm");
            return;
        }

        // Maintain a cpu wake lock until the AlarmAlert and AlarmKlaxon can
        // pick it up.
        //AlarmAlertWakeLock.acquireCpuWakeLock(context);

        /* Close dialogs and window shade */
        //Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        //context.sendBroadcast(closeDialogs);

        Xlog.d(TAG,"SchPwrOnReceiver.onReceive() id " + alarm.id + " time out ");
        // Decide which activity to start based on the state of the keyguard.
        if (alarm.id == 1) // power on
        {
            if (!alarm.daysOfWeek.isRepeatSet()) {
                Xlog.d(TAG,"SchPwrOnReceiver.onReceive(): isRepeatSet()");
                Alarms.enableAlarm(context, alarm.id, false);
            } else {
                // Enable the next alert if there is one. The above call to
                // enableAlarm will call setNextAlert so avoid calling it twice.
                Xlog.d(TAG,"SchPwrOnReceiver.onReceive(): not isRepeatSet()");
                Alarms.setNextAlertPowerOn(context);
            }
        }
        else if (alarm.id == 2) // power off
        {
            Xlog.d(TAG,"SchPwrOnReceiver.onReceive() id " + alarm.id + " get power off time out ");
        }
    }
}
