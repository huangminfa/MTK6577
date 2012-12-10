package com.android.settings.schpwronoff;

import com.android.settings.R;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.android.settings.SettingsPreferenceFragment;

import com.mediatek.xlog.Xlog;

/**
 * Manages each alarm
 */
public class SetAlarm extends SettingsPreferenceFragment
        implements TimePickerDialog.OnTimeSetListener {
	private static final String TAG="SetAlarm";
    private Preference mTimePref;
    private RepeatPreference mRepeatPref;
    private MenuItem mTestAlarmItem;

    private int     mId;
    private boolean mEnabled;
    private int     mHour;
    private int     mMinutes;
    private static final int MENU_REVET = Menu.FIRST;
    private static final int MENU_SAVE = Menu.FIRST + 1;
    private String mPrevTitle;
    /**
     * Set an alarm.  Requires an Alarms.ALARM_ID to be passed in as an
     * extra. FIXME: Pass an Alarm object like every other Activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {  
        addPreferencesFromResource(R.xml.schpwr_alarm_prefs);

        PreferenceScreen view = getPreferenceScreen();
        mTimePref = view.findPreference("time");
        mRepeatPref = (RepeatPreference) view.findPreference("setRepeat");

        final Bundle arguments = getArguments();
        if(arguments!=null){
            mId = arguments.getInt(Alarms.ALARM_ID);
        }
        mPrevTitle=getActivity().getTitle().toString();
        if(mId==1){
        	getActivity().setTitle(R.string.schedule_power_on_set);
        }else{
        	getActivity().setTitle(R.string.schedule_power_off_set);
        }
            Xlog.d(TAG,"In SetAlarm, alarm id = " + mId);

        // load alarm details from database 
        Alarm alarm = Alarms.getAlarm(getContentResolver(), mId);
        if(alarm!=null){
            mEnabled = alarm.enabled;
            mHour = alarm.hour;
            mMinutes = alarm.minutes;
            if(mRepeatPref!=null){
                mRepeatPref.setDaysOfWeek(alarm.daysOfWeek);
            }
        }
        updateTime();
        setHasOptionsMenu(true);
        super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public void onDetach() {
        if(mPrevTitle!=null)
            getActivity().setTitle(mPrevTitle);
        super.onDetach();
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mTimePref) {
            new TimePickerDialog(getActivity(), this, mHour, mMinutes,
                    DateFormat.is24HourFormat(getActivity())).show();
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(Menu.NONE, MENU_REVET, 0, R.string.revert)
                .setEnabled(true)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(Menu.NONE, MENU_SAVE, 0, R.string.done)
                .setEnabled(true)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_REVET:
                finishFragment();
                return true;
            case MENU_SAVE:
                saveAlarm();
                finishFragment();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mHour = hourOfDay;
        mMinutes = minute;
        updateTime();
        // If the time has been changed, enable the alarm.
        mEnabled = true;
    }

    private void updateTime() {
        Xlog.d(TAG,"updateTime " + mId);
        mTimePref.setSummary(Alarms.formatTime(getActivity(), mHour, mMinutes,
                mRepeatPref.getDaysOfWeek()));
    }

    private void saveAlarm() {
        final String alert = Alarms.ALARM_ALERT_SILENT;
        mEnabled |= mRepeatPref.isPressedPositive;
        Alarms.setAlarm(getActivity(), mId, mEnabled, mHour, mMinutes,
                mRepeatPref.getDaysOfWeek(), true, "", alert);

        if (mEnabled) {
            popAlarmSetToast(getActivity(), mHour, mMinutes,
                    mRepeatPref.getDaysOfWeek(), mId);
        }
    }

    /**
     * Write alarm out to persistent store and pops toast if alarm
     * enabled
     */
    private static void saveAlarm(
            Context context, int id, boolean enabled, int hour, int minute,
            Alarm.DaysOfWeek daysOfWeek, boolean vibrate, String label,
            String alert, boolean popToast) {
            Xlog.d(TAG,"** saveAlarm " + id + " " + label + " " + enabled
                + " " + hour + " " + minute + " vibe " + vibrate);

        // Fix alert string first
        Alarms.setAlarm(context, id, enabled, hour, minute, daysOfWeek, vibrate,
                label, alert);

        if (enabled && popToast) {
            popAlarmSetToast(context, hour, minute, daysOfWeek, 1);
        }
    }

    /**
     * Display a toast that tells the user how long until the alarm
     * goes off.  This helps prevent "am/pm" mistakes.
     */
    static void popAlarmSetToast(Context context, int hour, int minute,
                                 Alarm.DaysOfWeek daysOfWeek, int mId) {
        String toastText = formatToast(context, hour, minute, daysOfWeek, mId);
        Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
    }

    /**
     * format "Alarm set for 2 days 7 hours and 53 minutes from
     * now"
     */
    static String formatToast(Context context, int hour, int minute,
                              Alarm.DaysOfWeek daysOfWeek, int id) {
        long alarm = Alarms.calculateAlarm(hour, minute,
                                           daysOfWeek).getTimeInMillis();
        long delta = alarm - System.currentTimeMillis();;
        long hours = delta / (1000 * 60 * 60);
        long minutes = delta / (1000 * 60) % 60;
        long days = hours / 24;
        hours = hours % 24;

        String daySeq = (days == 0) ? "" :
                (days == 1) ? context.getString(R.string.day) :
                context.getString(R.string.days, Long.toString(days));

        String minSeq = (minutes == 0) ? "" :
                (minutes == 1) ? context.getString(R.string.minute) :
                context.getString(R.string.minutes, Long.toString(minutes));

        String hourSeq = (hours == 0) ? "" :
                (hours == 1) ? context.getString(R.string.hour) :
                context.getString(R.string.hours, Long.toString(hours));

        boolean dispDays = days > 0;
        boolean dispHour = hours > 0;
        boolean dispMinute = minutes > 0;

        int index = (dispDays ? 1 : 0) |
                    (dispHour ? 2 : 0) |
                    (dispMinute ? 4 : 0);

        String[] formats = context.getResources().getStringArray(R.array.alarm_set);
        if (id == 2)
        {
            index+=8;
        }
        return String.format(formats[index], daySeq, hourSeq, minSeq);
    }

    /**
     * Test code: this is disabled for production build.  Sets
     * this alarm to go off on the next minute
     */
    void setTestAlarm() {

        // start with now
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());

        int nowHour = c.get(java.util.Calendar.HOUR_OF_DAY);
        int nowMinute = c.get(java.util.Calendar.MINUTE);

        int minutes = (nowMinute + 1) % 60;
        int hour = nowHour + (nowMinute == 0 ? 1 : 0);

        saveAlarm(getActivity(), mId, true, hour, minutes, mRepeatPref.getDaysOfWeek(),
                true, "", Alarms.ALARM_ALERT_SILENT, true);
    }

}
