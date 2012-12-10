//For Operator Custom
//MTK_OP01_PROTECT_START
package com.android.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.drm.DrmManagerClient;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.mediatek.xlog.Xlog;

public class DrmSettings extends SettingsPreferenceFragment  {
    private static final String TAG = "DrmSettings";
    private static final String DRM_RESET = "drm_settings";
    private static Dialog mConfirmDialog;
    private static Preference mPreferenceReset;
    private static final int DIALOG_RESET = 1000;
    private static DrmManagerClient mClient;
    private Context mContext;
    private View mContentView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.drm_settings);

        mPreferenceReset = findPreference(DRM_RESET);
        mContext = getActivity();
        mClient = new DrmManagerClient(mContext);
    }

    @Override
    public Dialog onCreateDialog(int id) {
        Builder builder = new AlertDialog.Builder(mContext);
        AlertDialog dialog;
        switch (id) {
        case DIALOG_RESET:
            builder.setMessage(getResources().getString(
                    R.string.drm_reset_dialog_msg));
            builder.setTitle(getResources().getString(
                    R.string.drm_settings_title));
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {
                            if(mClient != null) {
                            	int result = mClient.removeAllRights();
                                if (result == mClient.ERROR_NONE) {
                                    Toast.makeText(mContext, R.string.drm_reset_toast_msg, Toast.LENGTH_SHORT).show();
                                    mPreferenceReset.setEnabled(false);
                                } else {
                                    Xlog.i(TAG, "removeAllRights fail!");
                                }
                                mClient = null;
                            }
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, null);
            dialog = builder.create();
            return dialog;
        default:
            return null;
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mPreferenceReset) {
            showDialog(DIALOG_RESET);
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mClient = null;
    }

}
//MTK_OP01_PROTECT_END
