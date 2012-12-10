package com.mediatek.nfc.tag.write;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.utils.Utils;

import java.io.IOException;

public class WriteTagActivity extends Activity {
    private static final String TAG = Utils.TAG + "/WriteTagActivity";

    // Stage with icon
    private static final int STAGE_SCAN = 1;

    private static final int STAGE_WRITING = 2;

    private static final int STAGE_DONE = 3;

    private static final int STAGE_ERROR = 4;

    // Stage without icon, but error title
    private static final int STAGE_NOT_EMPTY = 5;

    private static final int STAGE_TOO_BIG = 6;

    private static final int MSG_COUNTER = 101;

    // Count down time duration for "Done" button
    private static final int COUNTER_LENGTH = 10;

    private NfcAdapter mAdapter;

    private PendingIntent mPendingIntent;

    private TextView mTitle;

    private ImageView mStageImageView;

    private TextView mStatus;

    private Button mRighPositiveButton;

    private Button mLeftNegativeButton;

    private NdefMessage mMessage;

    private int mMessageSize;

    private NdefRecord mRecord;

    // Current scanned tag instance
    private Tag mCurrentTag;

    private static int sCurrentStage = STAGE_SCAN;
    
    // When NFC is off, notify user to turn it on for the first time
    private static final int DLG_NOTIFY_NFC = 1;

    @Override
    public void onCreate(Bundle savedState) {
        Utils.logd(TAG, "-->onCreate()");
        super.onCreate(savedState);

        setContentView(R.layout.write_tag);
        mTitle = (TextView) findViewById(R.id.write_title);
        mStatus = (TextView) findViewById(R.id.write_status);
        mStageImageView = (ImageView) findViewById(R.id.stage_img);
        mRighPositiveButton = (Button) findViewById(R.id.btn_right_positive);
        mRighPositiveButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickPositiveBtn();
            }
        });
        mLeftNegativeButton = (Button) findViewById(R.id.btn_left_negative);
        mLeftNegativeButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickNegativeBtn();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras == null
                || (!extras.containsKey("ndef_record") && !extras.containsKey("ndef_message"))) {
            Utils.loge(TAG, "Nothing to write.");
            return;
        }

        mMessage = (NdefMessage) extras.getParcelable("ndef_message");
        if (mMessage == null) {
            Utils.logd(TAG, "No message extra, try to get record extra");
            mRecord = extras.getParcelable("ndef_record");
            mMessage = new NdefMessage(new NdefRecord[] {
                mRecord
            });
        }

        if (null != mTitle) {
            mTitle.setText("Waiting for new coming tag...");
        }

        mAdapter = NfcAdapter.getDefaultAdapter(this);

        // Create a generic PendingIntent that will be deliver to this activity.
        // The NFC stack
        // will fill in the intent with the details of the discovered tag before
        // delivering to
        // this activity.
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass())
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        sCurrentStage = STAGE_SCAN;
        updateUI();
    }

    @Override
    public void onResume() {
        Utils.logd(TAG, "-->onResume()");
        super.onResume();
        if (mAdapter != null) {
            mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }
        
        int nfcState = mAdapter.getAdapterState();
        if (nfcState != NfcAdapter.STATE_ON && nfcState != NfcAdapter.STATE_TURNING_ON) {
            showDialog(DLG_NOTIFY_NFC);
        }
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        if (DLG_NOTIFY_NFC == id) {
            dialog = new AlertDialog.Builder(this)
                    .setTitle(android.R.string.dialog_alert_title)
                    .setMessage(R.string.msg_turn_on_nfc_before_write)
                    //setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton(android.R.string.yes, 
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    mAdapter.enable();
                                }
                            })
                    .setNegativeButton(android.R.string.no, 
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    WriteTagActivity.this.finish();
                                }
                            })
                     .create();
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    Utils.logi(TAG, "Waring enabling NFC dialog is dismissed.");
                    WriteTagActivity.this.finish();
                }
            });
        }
        return dialog;
    }

    private void clickPositiveBtn() {
        Utils.logd(TAG, "-->clickPositiveBtn()");
        switch (sCurrentStage) {
            case STAGE_DONE:
                finishWriting();
                break;
            case STAGE_NOT_EMPTY:
                beginToWrite();
                break;
            case STAGE_ERROR:
                /* Directly fail down */
            case STAGE_TOO_BIG:
                sCurrentStage = STAGE_SCAN;
                updateUI();
                break;
            default:
                Utils
                        .loge(TAG, "Right positive button should not exist in stage: "
                                + sCurrentStage);
                break;
        }
    }

    private void clickNegativeBtn() {
        Utils.logd(TAG, "-->clickNegativeBtn()");
        finishWriting();
    }

    @Override
    public void onNewIntent(Intent intent) {
        Utils.logi(TAG, "-->onNewIntent(), Get a new TAG found intent.");

        if (sCurrentStage != STAGE_SCAN) { // Only take effect in scanning stage
            Utils.loge(TAG, "Device is not in scan state, ignore incoming tag.");
            return;
        }
        mCurrentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (mCurrentTag == null) {
            Utils.loge(TAG, "Fail to get tag instance.");
            return;
        }
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean confirmOverwrite = preferences.getBoolean(Utils.KEY_CONFIRM_OVERWRITE,
                Utils.DEFAULT_VALUE_CONFIRM_OVERWRITE);
        Utils.logv(TAG, "confirmOverwrite?" + confirmOverwrite);
        if (rawMsgs != null && rawMsgs.length > 0 && confirmOverwrite) { 
            // Not empty tag
            sCurrentStage = STAGE_NOT_EMPTY;
            updateUI();
            return;
        }

        beginToWrite();
    }

    private void beginToWrite() {
        Utils.logd(TAG, "-->beginToWrite()");

        Ndef ndef = Ndef.get(mCurrentTag);
        if (ndef != null) {
            Utils
                    .logi(TAG, "Scanned tag type=" + ndef.getType() + ", maxSize="
                            + ndef.getMaxSize());
        } else {
            Utils.loge(TAG, " Fail to get NDEF instance.");
            return;
        }
        String[] techStrList = mCurrentTag.getTechList();
        Utils.logi(TAG, "     Tag supported tech num = " + techStrList.length);
        for (int i = 0; i < techStrList.length; i++) {
            Utils.logi(TAG, "   tech[" + i + "]= " + techStrList[i]);
        }

        mMessageSize = mMessage.toByteArray().length;
        Utils.logi(TAG, "To write message size = " + mMessageSize);
        if (mMessageSize > ndef.getMaxSize()) {
            sCurrentStage = STAGE_TOO_BIG;
            if (mStatus != null) {
                mStatus.setText(getString(R.string.write_status_message_too_big, mMessageSize, ndef
                        .getMaxSize()));
            }
            updateUI();
            return;
        }

        sCurrentStage = STAGE_WRITING;
        updateUI();
        if (null != mCurrentTag) {
            new TagWriteTask().execute(mCurrentTag);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }
    }

    /**
     * Update tag write result
     * 
     * @param messageRes
     * @param success success or not
     */
    void setStatus(int messageRes, boolean success) {
        if (null != mStatus) {
            if (!success) {
                String failReason = "";
                if (messageRes > 0) {
                    failReason = getString(messageRes);
                }
                mStatus.setText(getString(R.string.write_status_error, failReason));
                sCurrentStage = STAGE_ERROR;
            } else {
                mStatus.setText(R.string.write_status_done);
                sCurrentStage = STAGE_DONE;
            }
            updateUI();
        }
    }

    /**
     * Update UI to show different stage of writing: Scan, writing and done
     */
    private void updateUI() {
        Utils.logd(TAG, "-->updateUI(), stage=" + sCurrentStage);
        // Set stage icon component's visibility
        if (sCurrentStage <= STAGE_ERROR) {
            mTitle.setVisibility(View.GONE);
            mStageImageView.setVisibility(View.VISIBLE);
        } else {
            mTitle.setVisibility(View.VISIBLE);
            mStageImageView.setVisibility(View.GONE);
        }
        // Set positive and negative button's visibility
        if (sCurrentStage < STAGE_ERROR) {
            mLeftNegativeButton.setVisibility(View.GONE);
        } else {
            mLeftNegativeButton.setVisibility(View.VISIBLE);
        }
        if (sCurrentStage < STAGE_DONE) {
            mRighPositiveButton.setVisibility(View.GONE);
        } else {
            mRighPositiveButton.setVisibility(View.VISIBLE);
        }

        switch (sCurrentStage) {
            case STAGE_SCAN:
                mStageImageView.setImageResource(R.drawable.ic_write_stage_scan);
                mStatus.setText(R.string.write_status_scan);
                break;
            case STAGE_WRITING:
                mStageImageView.setImageResource(R.drawable.ic_write_stage_writing);
                mStatus.setText(R.string.write_status_writing);
                break;
            case STAGE_DONE:
                mStageImageView.setImageResource(R.drawable.ic_write_stage_done);
                mStatus.setText(R.string.write_status_done);
                mRighPositiveButton.setText(android.R.string.ok);
                // Begin count down
                Thread t = new Thread(new CounterThread(COUNTER_LENGTH));
                t.start();
                break;
            case STAGE_ERROR:
                mStageImageView.setImageResource(R.drawable.ic_write_stage_error);
                mRighPositiveButton.setText(android.R.string.yes);
                break;
            case STAGE_NOT_EMPTY:
                mStatus.setText(R.string.write_status_not_empty);
                mTitle.setText(R.string.write_status_title_not_empty);
                mRighPositiveButton.setText(android.R.string.ok);
                break;
            case STAGE_TOO_BIG:
                mTitle.setText(R.string.write_status_title_message_too_big);
                mRighPositiveButton.setText(R.string.btn_text_retry);
                break;

            default:
                Utils.loge(TAG, "Unsupported stage");
                break;
        }
    }

    /**
     * Write NDEF tag is a I/O blocking operation, execute it in background
     * Result "success" stand for success, any string else will stand for fail
     * reason
     */
    class TagWriteTask extends AsyncTask<Tag, Void, Integer> {
        @Override
        protected Integer doInBackground(Tag... params) {
            Utils.logi(TAG, "-->writeTag()");
            Tag tag = params[0];
            int failReasonRes = -1;
            try {
                Ndef ndef = Ndef.get(tag);

                if (ndef != null) {
                    Utils.logd(TAG, "-->writeTag(), enter common NDEF branch");
                    int maxSize = ndef.getMaxSize();
                    boolean canMakeReadOnly = ndef.canMakeReadOnly();
                    boolean isWritable = ndef.isWritable();
                    Utils.logd(TAG, "   max supported NDEF size=" + maxSize + ",  canMakeReadOnly?"
                            + canMakeReadOnly + ", isWritable?" + isWritable);
                    ndef.connect();

                    if (!ndef.isWritable()) {
                        failReasonRes = R.string.write_error_readonly;
                        return failReasonRes;
                    }

                    ndef.writeNdefMessage(mMessage);

                    boolean shouldMakeReadOnly = getSharedPreferences(Utils.CONFIG_FILE_NAME,
                            Context.MODE_PRIVATE).getBoolean(Utils.KEY_LOCK_TAG, false);
                    if (shouldMakeReadOnly) {
                        if (canMakeReadOnly) {
                            // ndef.makeReadOnly();
                            // TODO do not release lock tag function now
                            Utils.loge(TAG, "Need to make this tag read-only, ignore for temp.");
                        } else {
                            failReasonRes = R.string.error_tag_can_not_be_locked;
                        }
                    }

                    Utils.logd(TAG, "Wrote message to pre-formatted tag.");
                    return failReasonRes;
                } else {
                    Utils.logi(TAG, "-->writeTag(), enter NdefFormatable branch");
                    NdefFormatable format = NdefFormatable.get(tag);
                    if (format != null) {
                        try {
                            format.connect();
                            format.format(mMessage);
                            Utils.logd(TAG, "Formatted tag and wrote message.");
                            return failReasonRes;
                        } catch (IOException e) {
                            Utils.loge(TAG, "IOException happened while format tag.");
                        }
                    } else {
                        failReasonRes = R.string.write_error_not_support_ndef;
                        return failReasonRes;
                    }
                }
            } catch (TagLostException e) {
                Utils.loge(TAG, "Tag lost while writing tag. Failed to write tag", e);
            } catch (IOException e) {
                Utils.loge(TAG, "IOException happened while writing tag. Failed to write tag", e);
            } catch (FormatException e) {
                Utils.loge(TAG, "Format happened while writing tag. Failed to write tag", e);
            }
            failReasonRes = R.string.write_error_exception;
            return failReasonRes;
        }

        @Override
        protected void onPostExecute(Integer failReasonRes) {
            if (failReasonRes <= 0) {
                setStatus(-1, true);
            } else {
                setStatus(failReasonRes, false);
            }
        }
    }

    @Override
    protected void onDestroy() {
        Utils.logd(TAG, "-->onDestroy()");
        super.onDestroy();
        sCurrentStage = STAGE_SCAN;
    }

    private void finishWriting() {
        Utils.logd(TAG, "-->finishWriting()");
        finish();
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_COUNTER) {
                int remainTime = msg.arg1;
                Utils.logd(TAG, "Receive done button cound message, remaining time = ["
                        + remainTime + "s]");
                if (remainTime > 0) {
                    mRighPositiveButton.setText(getString(R.string.write_stage_title_done_count,
                            remainTime));
                }
                if (remainTime <= 0) {
                    finishWriting();
                }
            }
        };
    };

    class CounterThread implements Runnable {
        private int mCountLength = 0;

        public CounterThread(int countLength) {
            mCountLength = countLength;
        }

        @Override
        public void run() {
            while (mCountLength >= 0 && sCurrentStage == STAGE_DONE) {
                mHandler.obtainMessage(MSG_COUNTER, mCountLength--, 0).sendToTarget();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
