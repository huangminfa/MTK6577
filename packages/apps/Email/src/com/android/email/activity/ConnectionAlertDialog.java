package com.android.email.activity;

import com.android.email.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

public class ConnectionAlertDialog extends DialogFragment {
    public static ConnectionAlertDialog newInstance() {
        ConnectionAlertDialog frag = new ConnectionAlertDialog();
        return frag;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.unable_to_connect)
                .setMessage(R.string.need_connection_prompt)
                .setPositiveButton(getString(R.string.connection_settings),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                startActivity(new Intent(
                                        Settings.ACTION_WIFI_SETTINGS));
                            }
                        })
                .setNegativeButton(getString(R.string.cancel_action), null)
                .create();
    }
}
