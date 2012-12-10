package com.android.stk;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
    
public class NotificationAlertActivity extends Activity {
    
    private String mNotificationMessage = "";
    private String mTitle = "";
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        Window window = getWindow();
        setContentView(R.layout.stk_msg_dialog);

        TextView mMessageView = (TextView)findViewById(R.id.dialog_message);
        Button okButton = (Button) findViewById(R.id.button_ok);
        Button cancelButton = (Button) findViewById(R.id.button_cancel);
        
//        Bundle extras = getIntent().getExtras();
//        if (extras != null) {
//            mNotificationMessage = extras.getString(StkAppService.NOTIFICATION_KEY);
//            mTitle = extras.getString(StkAppService.NOTIFICATION_TITLE);
//        }
        mNotificationMessage = StkApp.mIdleMessage;
        mTitle = StkApp.mPLMN;
        
        window.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
                com.android.internal.R.drawable.stat_notify_sim_toolkit);
        setTitle(mTitle);

        okButton.setOnClickListener(mButtonClicked);
        cancelButton.setOnClickListener(mButtonClicked);

        mMessageView.setText(mNotificationMessage);
    }

    private View.OnClickListener mButtonClicked = new View.OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };
}
