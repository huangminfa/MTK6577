package com.android.phone;

import java.io.IOException;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.content.Intent;
import android.widget.Button;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.util.Log;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.phone.PhoneFeatureConstants.FeatureOption;

import android.view.View;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.Toast;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.gemini.GeminiPhone;
import android.os.SystemProperties;

public class UssdAlertActivity extends AlertActivity implements 
    DialogInterface.OnClickListener {
    public static String LOG_TAG = "UssdAlertActivity";
    public static int USSD_DIALOG_REQUEST = 1;
    public static int USSD_DIALOG_NOTIFICATION = 2;
    public static String USSD_MESSAGE_EXTRA = "ussd_message";
    public static String USSD_TYPE_EXTRA = "ussd_type";
    public static String USSD_SLOT_ID = "slot_id";
    
    private TextView mMsg = null;
    EditText inputText = null;
    
    private String mText = null;
    private int mType = USSD_DIALOG_REQUEST;
    private int slotId = 0;
    Phone phone = null;
    private static final String TAG = "UssdAlertActivity";
    private MediaPlayer mMediaPlayer = null;
    private Thread playTonethread = null;
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the "dialog"
        final AlertController.AlertParams p = mAlertParams;
        
        phone = PhoneApp.getInstance().phone;
        Intent intent = getIntent();
        mText = intent.getStringExtra(USSD_MESSAGE_EXTRA);
        mType = intent.getIntExtra(USSD_TYPE_EXTRA, USSD_DIALOG_REQUEST);
        slotId = intent.getIntExtra(USSD_SLOT_ID, 0);
        //p.mIconId = android.R.drawable.ic_dialog_alert;
        //p.mTitle = getString(R.string.bt_enable_title);
        //p.mTitle = "USSD";
        p.mView = createView();
        if (mType == USSD_DIALOG_REQUEST) {
            p.mPositiveButtonText = getString(R.string.send_button);
            p.mNegativeButtonText = getString(R.string.cancel);
        } else {
            p.mPositiveButtonText = getString(R.string.ok);
        }
        
        p.mPositiveButtonListener = this;
        p.mNegativeButtonListener = this;
        
//        if (mType == USSD_DIALOG_NOTIFICATION) {
//            
//        }
        if(isOrangeSupport()){
            playUSSDTone(PhoneApp.getInstance().getApplicationContext());
        }
        PhoneUtils.mUssdActivity = this;
        setupAlert();
    }

    protected void onResume() {
        super.onResume();
        if (mType == USSD_DIALOG_REQUEST) {
            String text = inputText.getText().toString();
            if (text != null && text.length() > 0) {
                mAlert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
            } else {
                mAlert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            }
            inputText.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s, int start,
                    int count, int after) {
            }
              
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            
            public void afterTextChanged(Editable s) {
                int count = s == null ? 0 : s.length();
                if (count > 0) {
                    mAlert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    mAlert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                }
            }
          });
        }
    }
    
    private View createView() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_ussd_response, null);
        mMsg = (TextView)dialogView.findViewById(R.id.msg);
        inputText = (EditText) dialogView.findViewById(R.id.input_field);

        if (mMsg != null) {
            mMsg.setText(mText);
        }
        
        if (mType == USSD_DIALOG_NOTIFICATION) {
            inputText.setVisibility(View.GONE);
        }
            
        return dialogView;
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (mType == USSD_DIALOG_REQUEST) {
                    sendUssd();
                }
                finish();
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                PhoneUtils.cancelUssdDialog();
                finish();
                break;
        }
    }
    
    private void sendUssd() {
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            Log.w(LOG_TAG, "sendUssd sendUssdResponseGemini button, simId:" + slotId); 
            Log.w(LOG_TAG, "sendUssd USSR string :"+inputText.getText().toString());
            ((GeminiPhone)phone).sendUssdResponseGemini(inputText.getText().toString(), slotId);
        } else {
            Log.w(LOG_TAG, "sendUssd sendUssdResponseGemini button"); 
            Log.w(LOG_TAG, "sendUssd USSR string :"+inputText.getText().toString());
            phone.sendUssdResponse(inputText.getText().toString());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.v(TAG, "onNewIntent");
        //force to finish ourself and then start new one
        finish();
        if(isOrangeSupport()){
            playUSSDTone(PhoneApp.getInstance().getApplicationContext());
        }
        startActivity(intent);
    }
    public void playUSSDTone(final Context context){
        if(null== playTonethread){
            playTonethread = new Thread(new Runnable(){
                public void run() {
                    mMediaPlayer = new MediaPlayer();   
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(context,RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                        mMediaPlayer.prepare();
                        } catch (IllegalArgumentException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                         } catch (SecurityException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                         } catch (IllegalStateException e) {
                             // TODO Auto-generated catch block
                             e.printStackTrace();
                         } catch (IOException e) {
                             // TODO Auto-generated catch block
                             e.printStackTrace();
                         }
                         mMediaPlayer.start();
                         setMediaListener(mMediaPlayer);
                     }
            });
        }
        playTonethread.start();
    }
    public void setMediaListener(MediaPlayer mediaPlayer){
       mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
       public void onCompletion(MediaPlayer mp) {   
       // TODO Auto-generated method stub  
       try {   
           mMediaPlayer.release();
           mMediaPlayer = null;
           } catch (Exception e) {
              Log.i(TAG , "str_OnCompletionListener is errror!!!");
           }
       }
     });
   }
    /* Orange customization begin */
    public static boolean isOrangeSupport() {
        String optr = SystemProperties.get("ro.operator.optr");
        if (optr != null && "OP03".equals(optr)) {
            return true;
        }
        return false;
    }
    /* Orange customization end */
}
