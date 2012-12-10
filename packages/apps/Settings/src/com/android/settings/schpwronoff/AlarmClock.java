package com.android.settings.schpwronoff;

import com.android.settings.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.ImageView;
import com.mediatek.xlog.Xlog;

import java.util.Calendar;
import java.text.DateFormatSymbols;

import com.android.settings.SettingsPreferenceFragment;

/**
 * AlarmClock application.
 */
public class AlarmClock extends SettingsPreferenceFragment implements
		OnItemClickListener {
	private static final String TAG = "AlarmClock";
	final static String PREFERENCES = "AlarmClock";
	final static String PREF_CLOCK_FACE = "face";
	final static String PREF_SHOW_CLOCK = "show_clock";

	/** Cap alarm count at this number */
	final static int MAX_ALARM_COUNT = 12;

	/**
	 * This must be false for production. If true, turns on logging, test code,
	 * etc.
	 */
	final static boolean DEBUG = true;

	private SharedPreferences mPrefs;
	private LayoutInflater mFactory;
	private View mClock = null;
	private ListView mAlarmsList;
	private Cursor mCursor;
	private String mAm, mPm;

	/*
	 * FIXME: it would be nice for this to live in an xml config file.
	 */

	private class AlarmTimeAdapter extends CursorAdapter {
		public AlarmTimeAdapter(Context context, Cursor cursor) {
			super(context, cursor);
		}

		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View ret = mFactory.inflate(R.layout.schpwr_alarm_time, parent,
					false);
			((TextView) ret.findViewById(R.id.am)).setText(mAm);
			((TextView) ret.findViewById(R.id.pm)).setText(mPm);

			DigitalClock digitalClock = (DigitalClock) ret
					.findViewById(R.id.digitalClock);
			if (digitalClock != null) {
				digitalClock.setLive(false);
			}
			Xlog.d(TAG, "newView " + cursor.getPosition());
			return ret;
		}

		public void bindView(View view, Context context, Cursor cursor) {
			final Alarm alarm = new Alarm(cursor);
			final Context cont = context;
			CheckBox onButton = (CheckBox) view.findViewById(R.id.alarmButton);
			if (onButton != null) {
				onButton.setChecked(alarm.enabled);
				onButton.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						boolean isChecked = ((CheckBox) v).isChecked();
						Alarms.enableAlarm(cont, alarm.id, isChecked);
						if (isChecked) {
							SetAlarm.popAlarmSetToast(cont, alarm.hour,
									alarm.minutes, alarm.daysOfWeek, alarm.id);
						}
					}
				});
			}

			ImageView onOffView = (ImageView) view
					.findViewById(R.id.power_on_off);
			if (onOffView != null) {
				onOffView.setImageDrawable(getResources().getDrawable(
						(alarm.id == 1) ? R.drawable.ic_settings_schpwron
								: R.drawable.ic_settings_schpwroff));
			}

			DigitalClock digitalClock = (DigitalClock) view
					.findViewById(R.id.digitalClock);

			// set the alarm text
			final Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, alarm.hour);
			c.set(Calendar.MINUTE, alarm.minutes);
			if (digitalClock != null) {
				digitalClock.updateTime(c);
			}

			// Set the repeat text or leave it blank if it does not repeat.
			TextView daysOfWeekView = (TextView) digitalClock
					.findViewById(R.id.daysOfWeek);
			final String daysOfWeekStr = alarm.daysOfWeek.toString(context,
					false);
			if (daysOfWeekView != null) {
				if (daysOfWeekStr != null && daysOfWeekStr.length() != 0) {
					daysOfWeekView.setText(daysOfWeekStr);
					daysOfWeekView.setVisibility(View.VISIBLE);
				} else {
					daysOfWeekView.setVisibility(View.GONE);
				}
			}
		}
	};

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		final int id = (int) info.id;
		switch (item.getItemId()) {
		case R.id.enable_alarm:
			final Cursor c = (Cursor) mAlarmsList.getAdapter().getItem(
					info.position);
			final Alarm alarm = new Alarm(c);
			Alarms.enableAlarm(getActivity(), alarm.id, !alarm.enabled);
			if (!alarm.enabled) {
				SetAlarm.popAlarmSetToast(getActivity(), alarm.hour,
						alarm.minutes, alarm.daysOfWeek, alarm.id);
			}
			return true;

		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		String[] ampm = new DateFormatSymbols().getAmPmStrings();
		mAm = ampm[0];
		mPm = ampm[1];
		mFactory = LayoutInflater.from(getActivity());
		mPrefs = getActivity().getSharedPreferences(PREFERENCES, 0);
		mCursor = Alarms.getAlarmsCursor(getActivity().getContentResolver());
		// updateLayout();
		Intent i = new Intent();
		i.setClass(getActivity(), SchPwrOnOffService.class);
		getActivity().startService(i);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		View viewFocus = getActivity().getCurrentFocus();
		int viewId = -1;
		int position = -1;
		if (viewFocus != null) {
			viewId = viewFocus.getId();
			if (viewFocus instanceof ListView) {
				position = ((ListView) viewFocus).getSelectedItemPosition();
			}
		}

		super.onConfigurationChanged(newConfig);
		// updateLayout();

		if (viewId >= 0 && position >= 0) {
			ListView mListView = (ListView) getActivity().findViewById(viewId);
			mListView.requestFocus();
			mListView.setSelection(position);
		}
	}

	private void updateLayout() {
		LayoutInflater inflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.schpwr_alarm_clock, null);
		mAlarmsList = (ListView) v.findViewById(android.R.id.list);
		if (mAlarmsList != null) {
			mAlarmsList
					.setAdapter(new AlarmTimeAdapter(getActivity(), mCursor));
			mAlarmsList.setVerticalScrollBarEnabled(true);
			mAlarmsList.setOnItemClickListener(this);
			mAlarmsList.setOnCreateContextMenuListener(getActivity());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater
				.inflate(R.layout.schpwr_alarm_clock, container, false);
		mAlarmsList = (ListView) v.findViewById(android.R.id.list);
		if (mAlarmsList != null) {
			mAlarmsList
					.setAdapter(new AlarmTimeAdapter(getActivity(), mCursor));
			mAlarmsList.setVerticalScrollBarEnabled(true);
			mAlarmsList.setOnItemClickListener(this);
			mAlarmsList.setOnCreateContextMenuListener(getActivity());
		}
		registerForContextMenu(mAlarmsList);
		return v;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mCursor.close();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		// Inflate the menu from xml.
		getActivity().getMenuInflater().inflate(R.menu.schpwr_context_menu,
				menu);

		// Use the current item to create a custom view for the header.
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		final Cursor c = (Cursor) mAlarmsList.getAdapter().getItem(
				(int) info.position);
		final Alarm alarm = new Alarm(c);

		// Construct the Calendar to compute the time.
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, alarm.hour);
		cal.set(Calendar.MINUTE, alarm.minutes);
		final String time = Alarms.formatTime(getActivity(), cal);

		// Inflate the custom view and set each TextView's text.
		final View v = mFactory.inflate(R.layout.schpwr_context_menu_header,
				null);
		TextView textView = (TextView) v.findViewById(R.id.header_time);
		if (textView != null) {
			textView.setText(time);
		}

		// Set the custom view on the menu.
		menu.setHeaderView(v);
		// Change the text to "disable" if the alarm is already enabled.
		if (alarm.enabled) {
			menu.findItem(R.id.enable_alarm).setTitle(R.string.disable_schpwr);
		} else {
			menu.findItem(R.id.enable_alarm).setTitle(R.string.enable_schpwr);
		}
	}

	public void onItemClick(AdapterView parent, View v, int pos, long id) {
		final Bundle bundle = new Bundle();
		bundle.putInt(Alarms.ALARM_ID, (int) id);
		startFragment(this, SetAlarm.class.getName(), 0, bundle);
	}
}
