package com.mediatek.nfc.tag.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.utils.Utils;

public class SettingsActivity extends Activity {
    private static final String TAG = Utils.TAG + "/Settings";

    private Button mSystemSettingsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.logd(TAG, "-->onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        mSystemSettingsBtn = (Button) findViewById(R.id.system_settings_btn);
        mSystemSettingsBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showSystemSettings();
            }
        });

    }

    private void showSystemSettings() {
        Utils.logd(TAG, "-->showSystemSettings()");

        Intent intent = new Intent("mediatek.settings.NFC_SETTINGS");
        startActivity(intent);
    }
}
