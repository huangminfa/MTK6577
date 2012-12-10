
package com.mediatek.nfc.tag.record;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity to show installed application in phone
 */
public class AppList extends ListActivity implements OnItemClickListener {
    private static final String TAG = Utils.TAG + "/AppList";

    public static final String EXTRA_APP_INFO = "app_info";

    List<AppEntry> mEntries = new ArrayList<AppEntry>();

    AppListAdapter mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.app_list);
        boolean includeSysApp = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                Utils.KEY_ADVANCED_APP_TAG, Utils.DEFAULT_VALUE_ADVANCED_APP_TAG);
        if (includeSysApp) {
            initLauncherAppList();
        } else {
            init3rdPartyAppList();
        }
        mAdapter = new AppListAdapter(this, mEntries);
        getListView().setAdapter(mAdapter);

        getListView().setOnItemClickListener(this);
    }

    /**
     * Query all 3rd party application installed in current device
     */
    private void init3rdPartyAppList() {
        Utils.logd(TAG, "-->init3rdPartyAppList()");
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> mApplications = new ArrayList<ApplicationInfo>();
        mApplications = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES
                | PackageManager.GET_DISABLED_COMPONENTS);

        int size = mApplications.size();
        Utils.logi(TAG, "Installed application size = " + size);

        for (int i = 0; i < size; i++) {
            ApplicationInfo info = mApplications.get(i);
            // Search only third part application
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                Utils.logd(TAG, "Get a third party application package name=" + info.packageName
                        + ",  source dir=" + info.sourceDir);
                AppEntry entry = new AppEntry(this, info);
                mEntries.add(entry);
            }
        }
    }

    /**
     * Query all applications that can be shown in Launcher
     */
    private void initLauncherAppList() {
        Utils.logd(TAG, "-->initLauncherAppList()");
        Intent queryIntent = new Intent(Intent.ACTION_MAIN);
        queryIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PackageManager pm = getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(queryIntent, 0);
        Utils.logi(TAG, "Number of activity appears in Launcher = " + resolveInfos.size());

        for (ResolveInfo resolveInfo : resolveInfos) {
            ApplicationInfo appInfo = resolveInfo.activityInfo.applicationInfo;
            AppEntry entry = new AppEntry(this, appInfo);
            if (!mEntries.contains(entry)) { // To avoid cases of more than one
                // launcher activity in the same
                // package
                mEntries.add(entry);
            } else { // TODO for duplicated one, double check.
                Utils.logw(TAG, "Found duplicated activity name=" + resolveInfo.activityInfo.name);
                Intent tempIntent = new Intent(Intent.ACTION_MAIN);
                tempIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                tempIntent.setPackage(resolveInfo.activityInfo.applicationInfo.packageName);
                ResolveInfo tempInfo = pm.resolveActivity(tempIntent, 0);
                Utils.logw(TAG, "Double resolve activity name=" + tempInfo.activityInfo.name);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Utils.logd(TAG, "-->onItemClick(), position=" + position + ", id=" + id);
        AppEntry entry = mEntries.get(position);
        Intent data = new Intent();
        data.putExtra(EXTRA_APP_INFO, entry.mInfo);
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    public static class AppEntry {
        final File mApkFile;

        String mLabel;

        boolean mMounted;

        ApplicationInfo mInfo;

        Drawable mIcon;

        AppEntry(Context context, ApplicationInfo info) {
            mApkFile = new File(info.sourceDir);
            this.mInfo = info;
            ensureLabel(context);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof AppEntry)) {
                return false;
            }
            AppEntry other = (AppEntry) obj;
            if (TextUtils.isEmpty(this.mInfo.packageName)
                    || TextUtils.isEmpty(other.mInfo.packageName)) {
                return false;
            }
            return this.mInfo.packageName.equals(other.mInfo.packageName);
        }
        
        @Override
        public int hashCode() {
            return super.hashCode();
        }

        void ensureLabel(Context context) {
            if (this.mLabel == null || !this.mMounted) {
                if (!this.mApkFile.exists()) {
                    this.mMounted = false;
                    this.mLabel = mInfo.packageName;
                } else {
                    this.mMounted = true;
                    CharSequence label = mInfo.loadLabel(context.getPackageManager());
                    this.mLabel = label != null ? label.toString() : mInfo.packageName;
                }
            }
        }

        boolean ensureIcon(Context context) {
            if (this.mIcon == null) {
                if (this.mApkFile.exists()) {
                    this.mIcon = this.mInfo.loadIcon(context.getPackageManager());
                    return true;
                } else {
                    this.mMounted = false;
                    this.mIcon = context.getResources().getDrawable(
                            R.drawable.sym_app_on_sd_unavailable_icon);
                }
            } else if (!this.mMounted) {
                // If the app wasn't mounted but is now mounted, reload
                // its icon.
                if (this.mApkFile.exists()) {
                    this.mMounted = true;
                    this.mIcon = this.mInfo.loadIcon(context.getPackageManager());
                    return true;
                }
            }
            return false;
        }
    }
}

/**
 * Adapter to show installed application list
 */
class AppListAdapter extends BaseAdapter {
    private Context mContext;

    private List<AppList.AppEntry> mEntries;

    private LayoutInflater mLayoutInflater;

    public AppListAdapter(Context context, List<AppList.AppEntry> entries) {
        mEntries = entries;
        mContext = context;
        if (entries == null) {
            mEntries = new ArrayList<AppList.AppEntry>();
        }
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mEntries.size();
    }

    @Override
    public Object getItem(int location) {
        return mEntries.get(location);
    }

    @Override
    public long getItemId(int location) {
        return location + 10;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView title;
        ImageView imageView;

        View view;
        if (convertView == null) {
            view = mLayoutInflater.inflate(R.layout.icon_list_item, null);
        } else {
            view = convertView;
        }
        title = (TextView) view.findViewById(R.id.title);
        imageView = (ImageView) view.findViewById(R.id.icon);

        AppList.AppEntry entry = mEntries.get(position);

        title.setText(entry.mLabel);
        entry.ensureIcon(mContext);
        imageView.setImageDrawable(entry.mIcon);
        return view;
    }
}
