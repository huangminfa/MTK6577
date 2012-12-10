package com.mediatek.contacts;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.provider.Telephony.Intents;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ListAdapter;

import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.Phone;
import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.SpecialCharSequenceMgr;
import com.android.internal.telephony.ITelephony;

import com.mediatek.contacts.util.ContactsSettingsUtils;
import com.mediatek.contacts.util.TelephonyUtils;
import com.mediatek.contacts.widget.SimPickerDialog;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.featureoption.FeatureOption;

public class SpecialCharSequenceMgrProxy {

    private final static String TAG = "SpecialCharSequenceMgrProxy";

    private static final String MMI_IMEI_DISPLAY = "*#06#";

    private static final String ADN_PHONE_NUMBER_COLUMN_NAME = "number";
    private static final String ADN_NAME_COLUMN_NAME = "name";
    private static final String ADN_INDEX_COLUMN_NAME = "index";

    private static final int ADN_QUERY_TOKEN_SIM1 = 0;
    private static final int ADN_QUERY_TOKEN_SIM2 = 1;

    private static final String SIM_CONTACT_URI = "content://icc/adn";
    private static final String USIM_CONTACT_URI = "content://icc/pbr";
	
    private static SimContactQueryCookie sCookie = null;

    private static boolean sStopProgress = false;


    private SpecialCharSequenceMgrProxy() {
    }

    public static boolean handleChars(Context context, String input, EditText textField) {
        if(ContactsApplication.sGemini)
            return handleChars(context, input, false, textField);
        else
            return SpecialCharSequenceMgr.handleChars(context, input, false, textField);
    }

    static boolean handleChars(Context context, String input) {
        if(ContactsApplication.sGemini)
            return handleChars(context, input, false, null);
        else
            return SpecialCharSequenceMgr.handleChars(context, input, false, null);
    }

    static boolean handleChars(Context context, String input, boolean useSystemWindow,
            EditText textField) {
        Log.d(TAG, "handleChars() dialString:" + input);
        if(ContactsApplication.sGemini) {
            String dialString = PhoneNumberUtils.stripSeparators(input);
            if (handleIMEIDisplay(context, dialString, useSystemWindow)
                    || handlePinEntry(context, dialString)
                    || handleAdnEntry(context, dialString, textField)
                    || handleSecretCode(context, dialString)) {
                return true;
            }
            return false;
        } else
            return SpecialCharSequenceMgr.handleChars(context, input, useSystemWindow, textField);
    }

    static boolean handleIMEIDisplay(Context context, String input, boolean useSystemWindow) {
        if(ContactsApplication.sGemini) {
            if (input.equals(MMI_IMEI_DISPLAY)) {
                int phoneType = ((TelephonyManager)context.getSystemService(
                        Context.TELEPHONY_SERVICE)).getCurrentPhoneType();
                showIMEIPanel(context, useSystemWindow);
                return true;
                /*int phoneType = ((TelephonyManager)context.getSystemService(
                        Context.TELEPHONY_SERVICE)).getCurrentPhoneType();

                if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {
                    showIMEIPanel(context, useSystemWindow);
                    return true;
                } else if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                    showMEIDPanel(context, useSystemWindow);
                    return true;
                }*/
            }
            return false;
        } else {
            return SpecialCharSequenceMgr.handleIMEIDisplay(context, input, useSystemWindow);
        }
    }

    static boolean handlePinEntry(Context context, String input) {
        if(ContactsApplication.sGemini) {
            final ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if ((input.startsWith("**04") || input.startsWith("**05")) && input.endsWith("#")) {
                try {
                    final String _input = input;
                    DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //convert the click item to slot id
                            final AlertDialog alert = (AlertDialog) dialog;
                            final ListAdapter listAdapter = alert.getListView().getAdapter();
                            final int slot = ((Integer)listAdapter.getItem(which)).intValue();
                            
                            try {
                                Log.d(TAG, "handlePinMmiGemini, slot " + slot);
                                phone.handlePinMmiGemini(_input, slot);
                            } catch(Exception e) {
                                Log.d(TAG, "exception : "+e.getMessage());
                            }
                            
                            dialog.dismiss();
                        }
                    };

                    final long defaultSim = ContactsSettingsUtils.getDefaultSIMForVoiceCall();

                    if(defaultSim == ContactsSettingsUtils.DEFAULT_SIM_NOT_SET ||
                            defaultSim == ContactsSettingsUtils.VOICE_CALL_SIM_SETTING_INTERNET) {
                        return false;
                    }

                    final SIMInfoWrapper simInfoWrapper = SIMInfoWrapper.getDefault();
                    int simCount = simInfoWrapper.getInsertedSimCount();

                    if(defaultSim == ContactsSettingsUtils.DEFAULT_SIM_SETTING_ALWAYS_ASK && simCount == 2) {
                        AlertDialog dialog = SimPickerDialog.create(context, context.getResources().getString(R.string.call_pin_dialog_title), false, onClickListener);
                        dialog.show();
                        return true;
                    } else {
                        // default sim is internet, nothing to do
                        if(defaultSim == ContactsSettingsUtils.VOICE_CALL_SIM_SETTING_INTERNET)
                            return false;

                        // default sim is always ask but sim count < 2
                        if(defaultSim == ContactsSettingsUtils.DEFAULT_SIM_SETTING_ALWAYS_ASK) {
                            if (simInfoWrapper.getInsertedSimInfoList() != null && simInfoWrapper.getInsertedSimInfoList().size() > 0 ){
                                return phone.handlePinMmiGemini(_input, simInfoWrapper.getInsertedSimInfoList().get(0).mSlot);
                            }else{
                                return false;
                            }
                        }

                        final int slot = simInfoWrapper.getSimSlotById((int)defaultSim);
                        return phone.handlePinMmiGemini(_input, slot);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to handlePinMmi due to remote exception");
                    return false;
                }
            }
            return false;
        } else
            return SpecialCharSequenceMgr.handlePinEntry(context, input);
    }

    static void showIMEIPanel(Context context, boolean useSystemWindow) {
        if (ContactsApplication.sGemini) {
            final TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            final CharSequence[] imeiStrs = new CharSequence[2];
            imeiStrs[0] = telephonyManager.getDeviceIdGemini(Phone.GEMINI_SIM_1);
            imeiStrs[1] = telephonyManager.getDeviceIdGemini(Phone.GEMINI_SIM_2);

            if(TextUtils.isEmpty(imeiStrs[0]))
                imeiStrs[0] = context.getResources().getString(R.string.imei_invalid);

            if(TextUtils.isEmpty(imeiStrs[1]))
                imeiStrs[1] = context.getResources().getString(R.string.imei_invalid);

            AlertDialog alert = new AlertDialog.Builder(context).setTitle(R.string.imei)
                    .setItems(imeiStrs, null).setPositiveButton(android.R.string.ok, null)
                    .setCancelable(false).create();
            alert.show();
        } else
            SpecialCharSequenceMgr.showIMEIPanel(context, useSystemWindow);
    }

    static void showMEIDPanel(Context context, boolean useSystemWindow) {
        SpecialCharSequenceMgr.showMEIDPanel(context, useSystemWindow);
    }

    static boolean handleAdnEntry(Context context, String input, EditText textField) {
        log("handleAdnEntry, input = " + input);
        if(ContactsApplication.sGemini) {
            KeyguardManager keyguardManager =
                (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager.inKeyguardRestrictedInputMode()) {
                return false;
            }

            int len = input.length();
            if ((len > 1) && (len < 5) && (input.endsWith("#"))) {
                sStopProgress = false;
                try {
                    // get the ordinal number of the sim contact
                    int index = -1;
                    try {
                        index = Integer.parseInt(input.substring(0, len-1));
                        if(index <= 0)
                            return false;
                    } catch(Exception e) {
                        return false;
                    }
                    

                    // The original code that navigated to a SIM Contacts list view did not
                    // highlight the requested contact correctly, a requirement for PTCRB
                    // certification.  This behaviour is consistent with the UI paradigm
                    // for touch-enabled lists, so it does not make sense to try to work
                    // around it.  Instead we fill in the the requested phone number into
                    // the dialer text field.

                    // create the async query handler
                    QueryHandler handler = new QueryHandler (context.getContentResolver());

                    // create the cookie object
                    // index in SIM
                    SimContactQueryCookie sc = new SimContactQueryCookie(index, handler, 0);
                    sCookie = sc;
                    // setup the cookie fields
                    sc.contactNum = index;
                    sc.setTextField(textField);
                    if (null != textField) {
                        sc.text = textField.getText().toString();
                    } else {
                        sc.text = null;
                    }
                    log("index = " + index);

                    // create the progress dialog
                    if(null == sc.progressDialog) {
                        sc.progressDialog = new ProgressDialog(context);
                    }
                    sc.progressDialog.setTitle(R.string.simContacts_title);
                    sc.progressDialog.setMessage(context.getText(R.string.simContacts_emptyLoading));
                    sc.progressDialog.setIndeterminate(true);
                    sc.progressDialog.setCancelable(true);
                    sc.progressDialog.setOnCancelListener(sc);
                    sc.progressDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
                    sc.context = context;

                    final long defaultSim = Settings.System.getLong(context.getContentResolver(), Settings.System.VOICE_CALL_SIM_SETTING, -3);

                    if(defaultSim == ContactsSettingsUtils.VOICE_CALL_SIM_SETTING_INTERNET ||
                       defaultSim == ContactsSettingsUtils.DEFAULT_SIM_NOT_SET) {
                        return false;
                    }

                    final SIMInfoWrapper simInfoWrapper = SIMInfoWrapper.getDefault();
                    int simCount = simInfoWrapper.getInsertedSimCount();

                    int slot = ADN_QUERY_TOKEN_SIM1;
                    if(defaultSim == ContactsSettingsUtils.DEFAULT_SIM_SETTING_ALWAYS_ASK) {
                        if(!TelephonyUtils.isRadioOnInner(0) && !TelephonyUtils.isRadioOnInner(1)) {
                            log("radio power off, bail out");
                            return false;
                        }

                        final boolean sim1Ready = TelephonyUtils.isSimReadyInner(0);
                        final boolean sim2Ready = TelephonyUtils.isSimReadyInner(1);

                        if(!sim1Ready && !sim2Ready) {
                            log("sim not ready, bail out");
                            return false;
                        }

                        sc.doubleQuery = false;

                        if(!sim1Ready && sim2Ready) {
                            slot = ADN_QUERY_TOKEN_SIM2;
                        } else if(sim1Ready && sim2Ready) {
                            sc.doubleQuery = true;
                        }
                        log("sim1Ready = " + sim1Ready + " sim2Ready = " + sim2Ready + " doubleQuery = " + sc.doubleQuery);
                    } else {
                        slot = simInfoWrapper.getSimSlotById((int)defaultSim);

                        if(!TelephonyUtils.isRadioOn((int)defaultSim) || !TelephonyUtils.isSimReady((int)defaultSim)) {
                            log("radio power off or sim not ready, bail out");
                            return false;
                        }
                    }

                    Uri uri = Uri.parse(buildSIMContactQueryUri(slot));
                    log("slot = " + slot + " uri = " + uri);
                    if (null != sc.progressDialog && !sc.progressDialog.isShowing()) {
                        Log.d(TAG, "handleAdnEntry() sc.progressDialog.show()");	   
                        sc.progressDialog.show();
                    }
                    handler.startQuery(slot, sc, uri, new String[] { ADN_PHONE_NUMBER_COLUMN_NAME,ADN_INDEX_COLUMN_NAME },
                            null, null, null);
                } catch(Exception e) {
                    Log.d(TAG, e.getMessage());
                }
                return true;
            }
            return false;
        } else
            return SpecialCharSequenceMgr.handleAdnEntry(context, input, textField);
    }

    static boolean handleSecretCode(Context context, String input) {
        // Secret codes are in the form *#*#<code>#*#*
        return SpecialCharSequenceMgr.handleSecretCode(context, input);
    }

    static String buildSIMContactQueryUri(int slot) {
        StringBuilder builder = new StringBuilder();
        if(TelephonyUtils.isUSIMInner(slot)) {
            builder.append(USIM_CONTACT_URI);
        } else
            builder.append(SIM_CONTACT_URI);

        // slot 0/1 ---> adn1/2
        builder.append(slot+1);
        return builder.toString();
    }

    private static class SimContactQueryCookie implements DialogInterface.OnCancelListener{
        public ProgressDialog progressDialog;
        public int contactNum;
        public boolean doubleQuery;

        // Used to identify the query request.
        private int mToken;
        private QueryHandler mHandler;

        // The text field we're going to update
        private EditText textField;
        public String text;

        public Context context;
        public String[] simNumber = new String[2];
        public String[] simName = new String[2];
        public boolean[] find = new boolean[2];

        public SimContactQueryCookie(int number, QueryHandler handler, int token) {
            contactNum = number;
            mHandler = handler;
            mToken = token;
        }

        /**
         * Synchronized getter for the EditText.
         */
        public synchronized EditText getTextField() {
            return textField;
        }

        public synchronized QueryHandler getQueryHandler(){
            return mHandler;
        }
        
        /**
         * Synchronized setter for the EditText.
         */
        public synchronized void setTextField(EditText text) {
            textField = text;
        }

        /**
         * Cancel the ADN query by stopping the operation and signaling
         * the cookie that a cancel request is made.
         */
        public synchronized void onCancel(DialogInterface dialog) {
            // close the progress dialog
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            // setting the textfield to null ensures that the UI does NOT get
            // updated.
            textField = null;

            // Cancel the operation if possible.
            mHandler.cancelOperation(mToken);
        }
    }

    private static class QueryHandler extends AsyncQueryHandler {

        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        protected void showToast(Context context, SimContactQueryCookie sc, String name, String number) {
            final EditText text = sc.textField;
            int len = number != null ? number.length() : 0;
            
            if (sc.text.equals(number)) {
                Toast .makeText(context, context.getString(R.string.non_phone_caption) + "\n" + number, Toast.LENGTH_LONG).show();
            } else if ((len > 1) && (len < 5) && (number.endsWith("#"))) {
                Toast.makeText(context, context.getString(R.string.non_phone_caption) + "\n" + number, Toast.LENGTH_LONG).show();
            } else {
                // fill the text in.
                text.setText(number);
                text.setSelection(text.getText().length());

                // display the name as a toast
                name = context.getString(R.string.menu_callNumber, name);
                Toast.makeText(context, name, Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Override basic onQueryComplete to fill in the textfield when
         * we're handed the ADN cursor.
         */
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor c) {
            log("onQueryComplete token = "+token);
            final SimContactQueryCookie sc = (SimContactQueryCookie) cookie;
            log("onQueryComplete sc = "+sc);		
            if(sc == null ||sStopProgress == true){
                log("onQueryComplete sc = "+sc + "sStopProgress " + sStopProgress);
                if (c != null) c.close();
                return;
            }
            final Context context = sc.progressDialog.getContext();
            EditText text = sc.getTextField();

            String name = null;
            String number = null;

            if (c != null && text != null) {
                c.moveToPosition(-1);
                while (c.moveToNext()) {
                    if ((token == ADN_QUERY_TOKEN_SIM1)&& fdnRequest(ADN_QUERY_TOKEN_SIM1)){
                        break;
                    }	
					
                    if ((token == ADN_QUERY_TOKEN_SIM2)&& fdnRequest(ADN_QUERY_TOKEN_SIM2)){
                        break;
                    }	
					
                    if (c.getInt(c.getColumnIndexOrThrow(ADN_INDEX_COLUMN_NAME)) == sc.contactNum) {
                        name = c.getString(c.getColumnIndexOrThrow(ADN_NAME_COLUMN_NAME));
                        number = c.getString(c.getColumnIndexOrThrow(ADN_PHONE_NUMBER_COLUMN_NAME));
                        sc.find[token] = true;
                        break;
                    }
                }
                c.close();
            }

            log("sc.find["+token+"] "+sc.find[token]);

            sc.simName[token] = name;
            sc.simNumber[token] = number;
            log("name = " + name + " number = " + number);

            if(!sc.doubleQuery) {
                if (sc.progressDialog != null && sc.progressDialog.isShowing()) {
                    sc.progressDialog.dismiss();
                    sc.progressDialog = null;
                }

                if(sc.find[token]) {
                    showToast(context, sc, name, number);
                } // findFlag
            } else {
                if(token == ADN_QUERY_TOKEN_SIM2) {
                    if (sc.progressDialog != null && sc.progressDialog.isShowing()) {
                        sc.progressDialog.dismiss();
                        sc.progressDialog = null;
                    }

                    DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                final AlertDialog alert = (AlertDialog) dialog;
                                final ListAdapter listAdapter = alert.getListView().getAdapter();
                                final int slot = ((Integer)listAdapter.getItem(which)).intValue();
                                log("onClick slot = " + slot + "  dialog = " + dialog);
                                try {
                                    if(sc.find[slot]) {
                                        showToast(context, sc, sc.simName[slot], sc.simNumber[slot]);
                                    }
                                } catch (Exception e) {
                                    log("Exception = " + e);
                                }
                                
                                dialog.dismiss();
                                log("onClick dismiss dialog = " + dialog);
                            } catch(Exception e) {
                                Log.d(TAG, "exception : "+e.getMessage());
                                Log.d(TAG, "exception : "+ e);
                            }
                        }
                    };

                    AlertDialog dialog = SimPickerDialog.create(context, context.getString(R.string.call_pin_dialog_title), false, onClickListener);
                    dialog.show();
                    log("onquerycomplete: show the selector dialog = " + dialog);
              } else {
                  QueryHandler handler = sc.getQueryHandler();
                  Uri uri = Uri.parse(buildSIMContactQueryUri(ADN_QUERY_TOKEN_SIM2));
                  handler.startQuery(ADN_QUERY_TOKEN_SIM2, sc, uri, new String[] { ADN_PHONE_NUMBER_COLUMN_NAME,ADN_INDEX_COLUMN_NAME },
                        null, null, null);
              }
           }
        }
    }

	/*
	 * public static boolean fdnRequest(int slot) {
	 * 
	 * Phone phone = PhoneFactory.getDefaultPhone(); if (null == phone) {
	 * Log.e(TAG, "fdnRequest phone is null"); return false; } IccCard iccCard;
	 * if (true == FeatureOption.MTK_GEMINI_SUPPORT) { iccCard = ((GeminiPhone)
	 * phone).getIccCardGemini(slot); } else { iccCard = phone.getIccCard(); }
	 * 
	 * return iccCard.getIccFdnEnabled(); }
	 */
	static boolean fdnRequest(int slot) {

		boolean bRet = false;

		final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
				.getService(Context.TELEPHONY_SERVICE));
		if (null == iTel) {
			Log.e(TAG, "fdnRequest iTel is null");
			return false;
		}

		try {
			if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
				bRet = iTel.isFDNEnabledGemini(slot);
			} else {
				bRet = iTel.isFDNEnabled();
			}
		} catch (RemoteException e) {
			Log.e(TAG, e.toString());
			e.printStackTrace();
		}

		Log.d(TAG, "fdnRequest fdn enable is " + bRet);
		return bRet;
	}

    static void log(String msg) {
        Log.d(TAG, msg);
    }

    public static void dismissDialog(){
        Log.d(TAG, "dismissProgressDialog");
        sStopProgress = true;
        Log.d(TAG, "sStopProgress " + sStopProgress);			
        if (sCookie != null && sCookie.progressDialog != null && sCookie.progressDialog.isShowing()) {
            sCookie.progressDialog.dismiss();			
        }
    }
}
