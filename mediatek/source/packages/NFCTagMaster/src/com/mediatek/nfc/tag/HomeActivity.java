
package com.mediatek.nfc.tag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mediatek.nfc.tag.history.HistoryHost;
import com.mediatek.nfc.tag.settings.SettingsActivity;
import com.mediatek.nfc.tag.utils.Utils;
import com.mediatek.nfc.tag.write.TagTypeList;

public class HomeActivity extends Activity implements OnClickListener {
    private static final String TAG = Utils.TAG + "/HomeActivity";

    private LinearLayout mEWalletEntrance = null;

    private LinearLayout mWritingEntrance = null;

    private LinearLayout mSettingsEntrance = null;

    private LinearLayout mHistoryEntrance = null;

    private NfcAdapter mNfcAdapter;

    // When NFC is off, notify user to turn it on for the first time
    private boolean mNeedNotifyTurnOnNfc = false;

    private static final int DLG_NOTIFY_NFC = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.home_frame);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        int nfcState = mNfcAdapter.getAdapterState();
        if (nfcState != NfcAdapter.STATE_ON && nfcState != NfcAdapter.STATE_TURNING_ON) {
            mNeedNotifyTurnOnNfc = true;
        }

        initUI();
    }

    private void initUI() {
        mEWalletEntrance = (LinearLayout) findViewById(R.id.launcher_ewallet);
        mWritingEntrance = (LinearLayout) findViewById(R.id.launcher_writing);
        mSettingsEntrance = (LinearLayout) findViewById(R.id.launcher_settings);
        mHistoryEntrance = (LinearLayout) findViewById(R.id.launcher_history);
        mEWalletEntrance.setOnClickListener(this);
        mWritingEntrance.setOnClickListener(this);
        mSettingsEntrance.setOnClickListener(this);
        mHistoryEntrance.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mEWalletEntrance) {
            startEWallet();
        } else if (v == mWritingEntrance) {
            startTagWritingPage();
        } else if (v == mSettingsEntrance) {
            startSettingsPage();
        } else if (v == mHistoryEntrance) {
            startHistoryPage();
        }
    }

    private void startEWallet() {
        Utils.logd(TAG, "-->startEWallet()");
        Toast.makeText(this, "EWallet has not been installed.", Toast.LENGTH_LONG).show();
    }

    private void startTagWritingPage() {
        Utils.logd(TAG, "-->startTagWritingPage()");
        Intent intent = new Intent(this, TagTypeList.class);
        startActivity(intent);
    }

    private void startSettingsPage() {
        Utils.logd(TAG, "-->startSettingsPage()");
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void startHistoryPage() {
        Utils.logd(TAG, "-->startHistoryPage()");
        Intent intent = new Intent(this, HistoryHost.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Just notify once
        if (mNeedNotifyTurnOnNfc) {
            mNeedNotifyTurnOnNfc = false;
            showDialog(DLG_NOTIFY_NFC);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        if (DLG_NOTIFY_NFC == id) {
            dialog = new AlertDialog.Builder(this).setTitle(
                    android.R.string.dialog_alert_title)
                    .setMessage(R.string.msg_turn_on_nfc)
//                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton(android.R.string.yes, 
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    mNfcAdapter.enable();
                                }
                            })
                    .setNegativeButton(android.R.string.no,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                     .create();
        }
        return dialog;
    }
}
