package com.mediatek.thememanager;

import java.util.ArrayList;
import java.util.HashMap; 

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.IActivityManager;
import android.app.ProgressDialog;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.util.Log;

import com.mediatek.xlog.Xlog;

import com.android.settings.DialogCreatable;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class ThemeManager extends SettingsPreferenceFragment implements
		OnItemClickListener,DialogCreatable {

    private static final String TAG = "ThemeManager";

	private ArrayList<ThemeData> mThemeDatas = new ArrayList<ThemeData>();

	/**
	 * The position of current used theme.
	 */
	private int mCurrentPosition = 0; 

	/**
	 * The number of theme in the system.
	 */
	private int mThemeCount = 0;

	/**
	 * The PreferenceFragment's hide method provide ListView
	 */
	private ListView listview; 

	/**
	 * The listview data ,in this code is from mThemeDatas
	 */
	ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
	LayoutInflater mInflater;
    private Object mLock = new Object();
    ProgressDialog mProgressDialog;
    private StatusBarManager mStatusBarManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Xlog.d(TAG, "onCreate");
		mInflater = (LayoutInflater) this.getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		mStatusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
		loadThemesFromDatabase();
	}

	@Override
	public void onStart() {    
		super.onStart();
		listview = getListView();       
		listview.setAdapter(new MyAdapter(this.getActivity(), mThemeDatas));
		listview.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		mStatusBarManager.disable(StatusBarManager.DISABLE_EXPAND);
        if (mCurrentPosition != position) {
            new SetThemeTask(this.getActivity()).execute(position);
		mCurrentPosition = position;
        } else {
        	mStatusBarManager.disable(StatusBarManager.DISABLE_NONE);
					this.getActivity().finish();
				}
	}

	/**
	 * Get all theme informations from database.
	 */
	private void loadThemesFromDatabase() {
		int position = 0;
		int columnIndex = 0;

		if (null != mThemeDatas) {
			mThemeDatas.clear();
		}

		Configuration config = getResources().getConfiguration();
		Cursor cursor = this.getActivity().getContentResolver().query(
				Themes.CONTENT_URI, null, null, null, null);
		if (cursor == null) {
			Xlog.w(TAG, "There is no theme in database.");
			return;
		}

		for (cursor.moveToFirst(); (!cursor.isAfterLast()); cursor.moveToNext()) {
			columnIndex = cursor.getColumnIndex(Themes.PACKAGE_NAME);
			String packageName = cursor.getString(columnIndex);

			columnIndex = cursor.getColumnIndex(Themes.THEME_PATH);
			String themePath = cursor.getString(columnIndex);

			columnIndex = cursor.getColumnIndex(Themes.THEME_NAME_ID);
			int themeNameId = cursor.getInt(columnIndex);

			if (config != null && config.skin.equals(themePath)) {
				mCurrentPosition = position;
			}
			String themeName;
			if (packageName.equals("android")) {
				themeName = getResources().getString(themeNameId);
			} else {
                themeName = getThemeName(this.getActivity(),
						packageName, themeNameId);
			}

			if (themeName != null) {
				ThemeData themeData = new ThemeData();
				themeData.packageName = packageName;
				themeData.themePath = themePath;
				themeData.themeName = themeName;
				mThemeDatas.add(position++, themeData);

				Xlog.d(TAG, "position = " + position + " ThemePath = "
						+ themePath + " ThemeNameId = " + themeNameId);
			} else {
				this.getActivity().getContentResolver().delete(
						Themes.CONTENT_URI, Themes.PACKAGE_NAME + " = ?",
						new String[] { packageName });
				Xlog.d(TAG, "delete record whose package name is = "
						+ packageName);
            }
        }
		mThemeCount = position;
		if (cursor != null) {
			cursor.close();
		}
	}
    private void showSetThemeDialog() {
        Xlog.d(TAG, "showSetThemeDialog()");
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this.getActivity(), null, getString(R.string.loading), true, false);
        } else {
            Xlog.i(TAG, "showSetThemeDialog mProgressDialog != null.");
        }
    }
    private void finishSetThemeDialog() {
        Xlog.d(TAG, "finishSetThemeDialog()");
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
    
    @Override
    public void onDestroy() {
        Xlog.d(TAG,"onDestroy");
        finishSetThemeDialog();
        super.onDestroy();
    }
    
    static String getThemeName(Context context, String packageName, int resId) {
        PackageManager mPm = context.getPackageManager();
        String themeName;
        try {
            //log theme name id and text
            Resources r = context.getPackageManager().getResourcesForApplication(packageName);
            themeName = String.valueOf(r.getText(resId));
            Xlog.d(TAG, "get theme name " + themeName + " from id " + Integer.toHexString(resId));
            
        } catch (NameNotFoundException e) {
            Xlog.d("TAG", "PackageManager cann't find resources for " + packageName);
            themeName = null;
        } catch (NotFoundException e) {
            Xlog.d("TAG", "Cann't find string in theme package, use default name.");
            int len = packageName.length();
            CharSequence pn = (CharSequence)packageName;
            int i = len - 1;
            while (pn.charAt(i) != '.') {
                i --;
            }
            String last = String.valueOf(pn.subSequence(i + 1, len));
            themeName = context.getResources().getString(R.string.custom_theme_name, last);
        }
        
        return themeName;
    }
    
    private class SetThemeTask extends AsyncTask<Integer, Void, Void> {
        public SetThemeTask(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Integer... types) {
            int position = types[0];
            IActivityManager am = ActivityManagerNative.getDefault();
            try {
                // The mThemeDatas list will be cleared when onDestroy is called,
                // so null pointer exception would happens when calling mThemeDatas.get(position),
                // synchronized set theme and destroy to fix this issue.
                synchronized (mLock) {
                    if (mThemeDatas == null) {
                        Xlog.e(TAG, "doInBackground error occured, mThemeDatas becomes null.");
                    }
                    Configuration config = am.getConfiguration();
                    config.skin = mThemeDatas.get(position).themePath.toString();
                    // Update system Properties, change system theme.
                    Xlog.d(TAG, "doInBackground() am.updateConfiguration() config.skin = " + config.skin);
                    am.updateConfiguration(config);
                }
            } catch (RemoteException e) {
                Xlog.e(TAG, "Update configuration for theme changed failed.");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            showSetThemeDialog();
        }

        @Override
        protected void onPostExecute(Void unused) {
        	mStatusBarManager.disable(StatusBarManager.DISABLE_NONE);
//            ((Activity)mContext).finish();
        }

        private Context mContext;
    }

	class MyAdapter extends BaseAdapter {
		Context context = null;
		ArrayList<ThemeData> mThemeDatas = null;

		public MyAdapter(Context context, ArrayList<ThemeData> mThemeDatas) {
			this.context = context;
			this.mThemeDatas = mThemeDatas;
		}

		public int getCount() {
			return mThemeDatas.size();
		}

		public Object getItem(int position) {
			return mThemeDatas.get(position);
		}

		public long getItemId(int position) {
			return position;
		}
 
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.thememanager, null);
				holder = new ViewHolder();
				holder.text = (TextView) convertView
						.findViewById(R.id.theme_name);
				holder.icon = (ImageView) convertView
						.findViewById(R.id.theme_preview);
				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			ThemeData themeData = mThemeDatas.get(position);
			holder.text.setText(themeData.themeName);
			if (("android").equals(themeData.packageName)) {
				// default
				Bitmap mDefaultPreviewImage = BitmapFactory.decodeResource(
						getResources(), R.drawable.ic_default_theme);
				holder.icon.setImageBitmap(mDefaultPreviewImage);

			} else { 
				// other
				Bitmap previewImage = getResources().getThemePreview(
						themeData.themePath);
				holder.icon.setImageBitmap(previewImage);
			}
			return convertView;
		}
	}

	static class ViewHolder {
		TextView text;
		ImageView icon;
	}
}
