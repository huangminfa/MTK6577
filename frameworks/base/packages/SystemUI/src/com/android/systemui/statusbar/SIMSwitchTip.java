package com.android.systemui.statusbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.systemui.R;

/**
 * [SystemUI] Support "SIM Switch Tip".
 * This class is used to control the show/hide of SIM switch tip window.
 */
public class SIMSwitchTip {
    private String SIM_SWITCH_TIP_SETTING = "settings";
    private String VOICE_CALL_SERVICE;
    private String MESSAGES_SERVICE;
    private String DATA_CONNECTION_SERVICE;
    private String REMIND_NEXT_TIME;

    private Context mContext;
    private WindowManager mWindowManager;
    private LayoutInflater mLayoutInflater;
    private View mSwitchTipView;
    private TextView mTipContentView;
    private CheckBox mRemindCheckBox;

    private boolean mShowing = false;
    private int mStatusBarHeight;

    public SIMSwitchTip(Context context) {
        this.mContext = context;
        init();
    }

    private void init() {
        Resources res = mContext.getResources();
        VOICE_CALL_SERVICE = res.getString(R.string.voice_call_service);
        MESSAGES_SERVICE = res.getString(R.string.messages_service);
        DATA_CONNECTION_SERVICE = res.getString(R.string.data_connection_service);
        REMIND_NEXT_TIME = res.getString(R.string.remind_next_time);

        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mStatusBarHeight = res.getDimensionPixelSize(com.android.internal.R.dimen.status_bar_height);

        mLayoutInflater = LayoutInflater.from(mContext);
        mSwitchTipView = mLayoutInflater.inflate(R.layout.zzz_sim_switch_tip, null);
        mTipContentView = (TextView) mSwitchTipView.findViewById(R.id.sim_switch_tip_content);
        mRemindCheckBox = (CheckBox) mSwitchTipView.findViewById(R.id.sim_switch_tip_check);
        mRemindCheckBox.setText(REMIND_NEXT_TIME);
        ImageView closeView = (ImageView) mSwitchTipView.findViewById(R.id.sim_switch_tip_close);
        closeView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
    }

    public boolean isShowing() {
        return mShowing;
    }

    public void show(String businessType) {
        if (!mShowing) {
            // read state.
            SharedPreferences settings = mContext.getSharedPreferences(SIM_SWITCH_TIP_SETTING, 0);
            String str = settings.getString("remind_me_next_time", "1");
            boolean remind = str.equals("1");
            mRemindCheckBox.setChecked(remind);
            if (remind) {
                // set tip content.
                setTipContent(businessType);
                // set parameters containing position and size.
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.x = 0;
                lp.y = mStatusBarHeight;
                lp.width = LayoutParams.WRAP_CONTENT;
                lp.height = LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.LEFT | Gravity.TOP;
                lp.format = PixelFormat.TRANSLUCENT;
                lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                           WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                           WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
                lp.packageName = mContext.getPackageName();
                mWindowManager.addView(mSwitchTipView, lp);
                // set showing flag
                mShowing = true;
            }
        }
    }

    public void hide() {
        if (mShowing) {
            try {
                // save state.
                SharedPreferences settings = mContext.getSharedPreferences(SIM_SWITCH_TIP_SETTING, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("remind_me_next_time", mRemindCheckBox.isChecked() ? "1" : "0");
                editor.commit();
                // remove view from window.
                mWindowManager.removeViewImmediate(mSwitchTipView);
            } finally {
                // set showing flag whether hiding view is successful.
                mShowing = false;
            }
        }
    }

    private void setTipContent(String businessType) {
        String serviceName = "";
        if (businessType.equals(android.provider.Settings.System.VOICE_CALL_SIM_SETTING)) {
            serviceName = VOICE_CALL_SERVICE;
        } else if (businessType.equals(android.provider.Settings.System.SMS_SIM_SETTING)) {
            serviceName = MESSAGES_SERVICE;
        } else if (businessType.equals(android.provider.Settings.System.GPRS_CONNECTION_SIM_SETTING)) {
            serviceName = DATA_CONNECTION_SERVICE;
        }
        mTipContentView.setText(mContext.getResources().getString(R.string.sim_switch_tip, serviceName));
        mRemindCheckBox.setText(REMIND_NEXT_TIME);
    }
    
    public void updateResources() {
    	Resources res = mContext.getResources();
		VOICE_CALL_SERVICE = res.getString(R.string.voice_call_service);
        MESSAGES_SERVICE = res.getString(R.string.messages_service);
        DATA_CONNECTION_SERVICE = res.getString(R.string.data_connection_service);
        REMIND_NEXT_TIME = res.getString(R.string.remind_next_time);
        ImageView closeView = (ImageView) mSwitchTipView.findViewById(R.id.sim_switch_tip_close);
        closeView.setImageDrawable(res.getDrawable(R.drawable.zzz_sim_switch_tip_close));
    }
}
