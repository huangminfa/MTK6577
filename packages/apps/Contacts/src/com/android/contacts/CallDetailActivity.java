/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.contacts;

import com.android.contacts.BackScrollManager.ScrollableHeader;
import com.android.contacts.calllog.CallDetailHistoryAdapter;
import com.android.contacts.calllog.CallLogQuery;
import com.android.contacts.calllog.CallTypeHelper;
import com.android.contacts.calllog.ContactInfo;
import com.android.contacts.calllog.ContactInfoHelper;
import com.android.contacts.calllog.PhoneNumberHelper;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.util.AsyncTaskExecutor;
import com.android.contacts.util.AsyncTaskExecutors;
import com.android.contacts.util.PhoneCapabilityTester;
import com.mediatek.contacts.HyphonManager;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.calllog.CallLogSimInfoHelper;
import com.mediatek.contacts.extention.CallLogExtention;
import com.mediatek.contacts.extention.CallLogExtentionManager;

import android.app.StatusBarManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.content.ActivityNotFoundException;

/**
 * import com.android.contacts.voicemail.VoicemailPlaybackFragment;
 * import com.android.contacts.voicemail.VoicemailStatusHelper;
 * import com.android.contacts.voicemail.VoicemailStatusHelper.StatusMessage;
 * import com.android.contacts.voicemail.VoicemailStatusHelperImpl;
 * 
 */
import com.android.internal.telephony.ITelephony;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.provider.CallLog.Calls;
import android.provider.Contacts.Intents.Insert;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.Telephony.SIMInfo;
//import android.provider.VoicemailContract.Voicemails;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.util.OperatorUtils;
import com.mediatek.contacts.util.ContactsGroupUtils.USIMAas;
import com.android.contacts.util.Constants;

/**
 * Displays the details of a specific call log entry.
 * <p>
 * This activity can be either started with the URI of a single call log entry, or with the
 * {@link #EXTRA_CALL_LOG_IDS} extra to specify a group of call log entries.
 */
public class CallDetailActivity extends Activity implements ProximitySensorAware {
    private static final String TAG = "CallDetail";

    /** The time to wait before enabling the blank the screen due to the proximity sensor. */
    private static final long PROXIMITY_BLANK_DELAY_MILLIS = 100;
    /** The time to wait before disabling the blank the screen due to the proximity sensor. */
    private static final long PROXIMITY_UNBLANK_DELAY_MILLIS = 500;

    /** The enumeration of {@link AsyncTask} objects used in this class. */
    public enum Tasks {
        MARK_VOICEMAIL_READ,
        DELETE_VOICEMAIL_AND_FINISH,
        REMOVE_FROM_CALL_LOG_AND_FINISH,
        UPDATE_PHONE_CALL_DETAILS,
    }

    /** A long array extra containing ids of call log entries to display. */
    public static final String EXTRA_CALL_LOG_IDS = "EXTRA_CALL_LOG_IDS";
    /** If we are started with a voicemail, we'll find the uri to play with this extra. */
    public static final String EXTRA_VOICEMAIL_URI = "EXTRA_VOICEMAIL_URI";
    /** If we should immediately start playback of the voicemail, this extra will be set to true. */
    public static final String EXTRA_VOICEMAIL_START_PLAYBACK = "EXTRA_VOICEMAIL_START_PLAYBACK";

    private CallTypeHelper mCallTypeHelper;
    private PhoneNumberHelper mPhoneNumberHelper;
    private PhoneCallDetailsHelper mPhoneCallDetailsHelper;
    private TextView mHeaderTextView;
    private View mHeaderOverlayView;
    private ImageView mMainActionView;
    private ImageButton mMainActionPushLayerView;
    private ImageView mContactBackgroundView;
    private AsyncTaskExecutor mAsyncTaskExecutor;
    private ContactInfoHelper mContactInfoHelper;

    private String mNumber = null;
    private String mDefaultCountryIso;

    /* package */ LayoutInflater mInflater;
    /* package */ Resources mResources;
    /* package */ Context mContext;///M:AAS
    /** Helper to load contact photos. */
    private ContactPhotoManager mContactPhotoManager;
    /** Helper to make async queries to content resolver. */
    private CallDetailActivityQueryHandler mAsyncQueryHandler;
    // The following lines are deleted by Mediatek Inc to close Google default
    // Voicemail function.

    /** Helper to get voicemail status messages. */
    // private VoicemailStatusHelper mVoicemailStatusHelper;
    // Views related to voicemail status message.
    // private View mStatusMessageView;
    // private TextView mStatusMessageText;
    // private TextView mStatusMessageAction;
    
    // The previous lines are deleted by Mediatek Inc to close Google default
    // Voicemail function.
    /** Whether we should show "edit number before call" in the options menu. */
    private boolean mHasEditNumberBeforeCallOption;
    /** Whether we should show "trash" in the options menu. */
    //private boolean mHasTrashOption;
    /** Whether we should show "remove from call log" in the options menu. */
    //private boolean mHasRemoveFromCallLogOption;

    private ProximitySensorManager mProximitySensorManager;
    private final ProximitySensorListener mProximitySensorListener = new ProximitySensorListener();

    /** Listener to changes in the proximity sensor state. */
    private class ProximitySensorListener implements ProximitySensorManager.Listener {
        /** Used to show a blank view and hide the action bar. */
        private final Runnable mBlankRunnable = new Runnable() {
            @Override
            public void run() {
                View blankView = findViewById(R.id.blank);
                blankView.setVisibility(View.VISIBLE);
                getActionBar().hide();
            }
        };
        /** Used to remove the blank view and show the action bar. */
        private final Runnable mUnblankRunnable = new Runnable() {
            @Override
            public void run() {
                View blankView = findViewById(R.id.blank);
                blankView.setVisibility(View.GONE);
                getActionBar().show();
            }
        };

        @Override
        public synchronized void onNear() {
            clearPendingRequests();
            postDelayed(mBlankRunnable, PROXIMITY_BLANK_DELAY_MILLIS);
        }

        @Override
        public synchronized void onFar() {
            clearPendingRequests();
            postDelayed(mUnblankRunnable, PROXIMITY_UNBLANK_DELAY_MILLIS);
        }

        /** Removed any delayed requests that may be pending. */
        public synchronized void clearPendingRequests() {
            View blankView = findViewById(R.id.blank);
            blankView.removeCallbacks(mBlankRunnable);
            blankView.removeCallbacks(mUnblankRunnable);
        }

        /** Post a {@link Runnable} with a delay on the main thread. */
        private synchronized void postDelayed(Runnable runnable, long delayMillis) {
            // Post these instead of executing immediately so that:
            // - They are guaranteed to be executed on the main thread.
            // - If the sensor values changes rapidly for some time, the UI will not be
            //   updated immediately.
            View blankView = findViewById(R.id.blank);
            blankView.postDelayed(runnable, delayMillis);
        }
    }

    static final String[] CALL_LOG_PROJECTION = new String[] {
        CallLog.Calls.DATE,
        CallLog.Calls.DURATION,
        CallLog.Calls.NUMBER,
        CallLog.Calls.TYPE,
        CallLog.Calls.COUNTRY_ISO,
        CallLog.Calls.GEOCODED_LOCATION,
        //The following lines are provided and maintained by Mediatek Inc.
        CallLog.Calls.SIM_ID,
        CallLog.Calls.VTCALL,
        //The previous lines are provided and maintained by Mediatek Inc.
    };

    static final int DATE_COLUMN_INDEX = 0;
    static final int DURATION_COLUMN_INDEX = 1;
    static final int NUMBER_COLUMN_INDEX = 2;
    static final int CALL_TYPE_COLUMN_INDEX = 3;
    static final int COUNTRY_ISO_COLUMN_INDEX = 4;
    static final int GEOCODED_LOCATION_COLUMN_INDEX = 5;

    private final View.OnClickListener mPrimaryActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(((ViewEntry) view.getTag()).primaryIntent.putExtra(Constants.EXTRA_FOLLOW_SIM_MANAGEMENT, true));
        }
    };

    private final View.OnClickListener mSecondaryActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                startActivity(((ViewEntry) view.getTag()).secondaryIntent);
            } catch (ActivityNotFoundException e) {
                Log.d(TAG, "ActivityNotFoundException for secondaryIntent");
            }
        }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        log("CallDetailActivity  onCreat()");
        super.onCreate(icicle);
        /**
        * Change Feature by Mediatek Begin.
        * Original Android's Code:
          setContentView(R.layout.call_detail);
        * Descriptions:
        */
        setContentView(R.layout.call_detail_without_voicemail);
        /**
        * Change Feature by Mediatek End.
        */

        mAsyncTaskExecutor = AsyncTaskExecutors.createThreadPoolExecutor();
        mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mResources = getResources();
        mContext = getApplicationContext();///M:AAS

        mCallTypeHelper = new CallTypeHelper(getResources());
        mPhoneNumberHelper = new PhoneNumberHelper(mResources);
        mPhoneCallDetailsHelper = new PhoneCallDetailsHelper(mResources, mCallTypeHelper,
                                                             mPhoneNumberHelper, null, this);
        mAsyncQueryHandler = new CallDetailActivityQueryHandler(this);
        mHeaderTextView = (TextView) findViewById(R.id.header_text);
        mHeaderOverlayView = findViewById(R.id.photo_text_bar);
        
        // The following lines are deleted by Mediatek Inc to close Google default
        // Voicemail function.
        /**
         mVoicemailStatusHelper = new VoicemailStatusHelperImpl();
         mStatusMessageView = findViewById(R.id.voicemail_status);
         mStatusMessageText = (TextView)
         findViewById(R.id.voicemail_status_message);
         mStatusMessageAction = (TextView)
         findViewById(R.id.voicemail_status_action);
        */
        // The previous lines are deleted by Mediatek Inc to close Google default
        // Voicemail function.
        mMainActionView = (ImageView) findViewById(R.id.main_action);
        mMainActionPushLayerView = (ImageButton) findViewById(R.id.main_action_push_layer);
        mContactBackgroundView = (ImageView) findViewById(R.id.contact_background);
        mDefaultCountryIso = ContactsUtils.getCurrentCountryIso(this);
        mContactPhotoManager = ContactPhotoManager.getInstance(this);
        mProximitySensorManager = new ProximitySensorManager(this, mProximitySensorListener);
        mContactInfoHelper = new ContactInfoHelper(this, ContactsUtils.getCurrentCountryIso(this));
        // The following lines are provided and maintained by Mediatek Inc.
        mSimName = (TextView) findViewById(R.id.sim_name);
        SIMInfoWrapper.getDefault().registerForSimInfoUpdate(mHandler, SIM_INFO_UPDATE_MESSAGE, null);
        mStatusBarMgr = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        // The previous lines are provided and maintained by Mediatek Inc.
        configureActionBar();
        //optionallyHandleVoicemail(); deleted by Mediatek Inc to close Google default Voicemail function.
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED);
            this.registerReceiver(mReceiver, intentFilter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        log("CallDetailActivity  onResume()");
        hasSms = PhoneCapabilityTester.isSmsIntentRegistered(getApplicationContext());

        updateData(getCallLogEntryUris());
      //The following lines are provided and maintained by Mediatek Inc.
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            mShowSimIndicator = true;
            setSimIndicatorVisibility(true);
        }
        //The previous lines are provided and maintained by Mediatek Inc.
    }
    
    // The following lines are deleted by Mediatek Inc to close Google default
    // Voicemail function.
    /**
     * Handle voicemail playback or hide voicemail ui.
     * <p>
     * If the Intent used to start this Activity contains the suitable extras, then start voicemail
     * playback.  If it doesn't, then hide the voicemail ui.
     */
    /**
    private void optionallyHandleVoicemail() {
        View voicemailContainer = findViewById(R.id.voicemail_container);
        if (hasVoicemail()) {
            // Has voicemail: add the voicemail fragment.  Add suitable arguments to set the uri
            // to play and optionally start the playback.
            // Do a query to fetch the voicemail status messages.
            VoicemailPlaybackFragment playbackFragment = new VoicemailPlaybackFragment();
            Bundle fragmentArguments = new Bundle();
            fragmentArguments.putParcelable(EXTRA_VOICEMAIL_URI, getVoicemailUri());
            if (getIntent().getBooleanExtra(EXTRA_VOICEMAIL_START_PLAYBACK, false)) {
                fragmentArguments.putBoolean(EXTRA_VOICEMAIL_START_PLAYBACK, true);
            }
            playbackFragment.setArguments(fragmentArguments);
            voicemailContainer.setVisibility(View.VISIBLE);
            getFragmentManager().beginTransaction()
                    .add(R.id.voicemail_container, playbackFragment).commitAllowingStateLoss();
            mAsyncQueryHandler.startVoicemailStatusQuery(getVoicemailUri());
            markVoicemailAsRead(getVoicemailUri());
        } else {
            // No voicemail uri: hide the status view.
            mStatusMessageView.setVisibility(View.GONE);
            voicemailContainer.setVisibility(View.GONE);
        }
    }

    private boolean hasVoicemail() {
        return getVoicemailUri() != null;
    }

    private Uri getVoicemailUri() {
        return getIntent().getParcelableExtra(EXTRA_VOICEMAIL_URI);
    }

    private void markVoicemailAsRead(final Uri voicemailUri) {
        log("CallDetailActivity  markVoicemailAsRead()");
        mAsyncTaskExecutor.submit(Tasks.MARK_VOICEMAIL_READ, new AsyncTask<Void, Void, Void>() {
            @Override
            public Void doInBackground(Void... params) {
                ContentValues values = new ContentValues();
                values.put(Voicemails.IS_READ, true);
                getContentResolver().update(voicemailUri, values,
                        Voicemails.IS_READ + " = 0", null);
                return null;
            }
        });
    }
    */
    // The previous lines are deleted by Mediatek Inc to close Google default
    // Voicemail function.
    /**
     * Returns the list of URIs to show.
     * <p>
     * There are two ways the URIs can be provided to the activity: as the data on the intent, or as
     * a list of ids in the call log added as an extra on the URI.
     * <p>
     * If both are available, the data on the intent takes precedence.
     */
    private Uri[] getCallLogEntryUris() {
        log("CallDetailActivity getCallLogEntryUris()");
        Uri uri = getIntent().getData();
        if (uri != null) {
            // If there is a data on the intent, it takes precedence over the extra.
         	Uri queryUri = Uri.parse("content://call_log/callsjoindataview");
            //uris[index] = ContentUris.withAppendedId(Calls.CONTENT_URI_WITH_VOICEMAIL, ids[index]);
        	long id = ContentUris.parseId(uri);
        	uri = ContentUris.withAppendedId(queryUri, id);
            return new Uri[]{ uri };
        } 
        long[] ids = getIntent().getLongArrayExtra(EXTRA_CALL_LOG_IDS);
        Uri[] uris = new Uri[ids.length];
        for (int index = 0; index < ids.length; ++index) {
        	Uri queryUri = Uri.parse("content://call_log/callsjoindataview");
            //uris[index] = ContentUris.withAppendedId(Calls.CONTENT_URI_WITH_VOICEMAIL, ids[index]);
        	uris[index] = ContentUris.withAppendedId(queryUri, ids[index]);
        }
        return uris;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CALL: {
                // Make sure phone isn't already busy before starting direct call
                TelephonyManager tm = (TelephonyManager)
                        getSystemService(Context.TELEPHONY_SERVICE);
                if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                            Uri.fromParts("tel", mNumber, null));
                    callIntent.setClassName(Constants.PHONE_PACKAGE, Constants.OUTGOING_CALL_BROADCASTER);
                    startActivity(callIntent);
                    return true;
                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Update user interface with details of given call.
     *
     * @param callUris URIs into {@link CallLog.Calls} of the calls to be displayed
     */
    private void updateData(final Uri... callUris) {
        class UpdateContactDetailsTask extends AsyncTask<Void, Void, PhoneCallDetails[]> {
            @Override
            public PhoneCallDetails[] doInBackground(Void... params) {
                // TODO: All phone calls correspond to the same person, so we can make a single
                // lookup.
                final int numCalls = callUris.length;
                PhoneCallDetails[] details = new PhoneCallDetails[numCalls];
                try {
                    for (int index = 0; index < numCalls; ++index) {
                        details[index] = getPhoneCallDetailsForUri(callUris[index]);
                    }
                    return details;
                } catch (IllegalArgumentException e) {
                    // Something went wrong reading in our primary data.
                    Log.w(TAG, "invalid URI starting call details", e);
                    return null;
                }
            }

            @Override
            public void onPostExecute(PhoneCallDetails[] details) {
                if (details == null) {
                    // Somewhere went wrong: we're going to bail out and show error to users.
                    Toast.makeText(CallDetailActivity.this, R.string.toast_call_detail_error,
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // We know that all calls are from the same number and the same contact, so pick the
                // first.
                PhoneCallDetails firstDetails = details[0];
                mNumber = firstDetails.number.toString();
                final Uri contactUri = firstDetails.contactUri;
                final Uri photoUri = firstDetails.photoUri;

                // Set the details header, based on the first phone call.
                mPhoneCallDetailsHelper.setCallDetailsHeader(mHeaderTextView, firstDetails);

                // Cache the details about the phone number.
                final Uri numberCallUri = mPhoneNumberHelper.getCallUri(mNumber,firstDetails.simId);
                final boolean canPlaceCallsTo = mPhoneNumberHelper.canPlaceCallsTo(mNumber);
//                final boolean isVoicemailNumber = mPhoneNumberHelper.isVoicemailNumber(mNumber);
                final boolean isVoicemailNumber = false;
                final boolean isSipNumber = mPhoneNumberHelper.isSipNumber(mNumber);

                // Let user view contact details if they exist, otherwise add option to create new
                // contact from this number.
                final Intent mainActionIntent;
                final int mainActionIcon;
                final String mainActionDescription;

                final CharSequence nameOrNumber;
                if (!TextUtils.isEmpty(firstDetails.name)) {
                    nameOrNumber = firstDetails.name;
                } else {
                    nameOrNumber = firstDetails.number;
                }

                if (contactUri != null) {
                    mainActionIntent = new Intent(Intent.ACTION_VIEW, contactUri);
                    mainActionIcon = R.drawable.ic_contacts_holo_dark;
                    mainActionDescription =
                            getString(R.string.description_view_contact, nameOrNumber);
                } else if (isVoicemailNumber) {
                    mainActionIntent = null;
                    mainActionIcon = 0;
                    mainActionDescription = null;
                    // } else if (isSipNumber) {
                    // // TODO: This item is currently disabled for SIP
                    // addresses, because
                    // // the Insert.PHONE extra only works correctly for PSTN
                    // numbers.
                    // //
                    // // To fix this for SIP addresses, we need to:
                    // // - define ContactsContract.Intents.Insert.SIP_ADDRESS,
                    // and use it here if
                    // // the current number is a SIP address
                    // // - update the contacts UI code to handle
                    // Insert.SIP_ADDRESS by
                    // // updating the SipAddress field
                    // // and then we can remove the "!isSipNumber" check above.
                    // mainActionIntent = null;
                    // mainActionIcon = 0;
                    // mainActionDescription = null;
                } else if (canPlaceCallsTo) {
                    mainActionIntent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                    mainActionIntent.setType(Contacts.CONTENT_ITEM_TYPE);
                    /**
                    * Change Feature by Mediatek Begin.
                    * Original Android's Code:
                       mainActionIntent.putExtra(Insert.PHONE, mNumber);
                    * Descriptions:
                    */
                    if (isSipNumber) {
                    	mainActionIntent.putExtra(
                    			ContactsContract.Intents.Insert.SIP_ADDRESS,
                    			mNumber);
                    } else {
                    	mainActionIntent.putExtra(Insert.PHONE, mNumber);
                    }

                    /**
                    * Change Feature by Mediatek End.
                    */
                    mainActionIcon = R.drawable.ic_add_contact_holo_dark;
                    mainActionDescription = getString(R.string.description_add_contact);
                } else {
                    // If we cannot call the number, when we probably cannot add it as a contact either.
                    // This is usually the case of private, unknown, or payphone numbers.
                    mainActionIntent = null;
                    mainActionIcon = 0;
                    mainActionDescription = null;
                }

                if (mainActionIntent == null) {
                    mMainActionView.setVisibility(View.INVISIBLE);
                    mMainActionPushLayerView.setVisibility(View.GONE);
                    mHeaderTextView.setVisibility(View.INVISIBLE);
                    mHeaderOverlayView.setVisibility(View.INVISIBLE);
                } else {
                    mMainActionView.setVisibility(View.VISIBLE);
                    mMainActionView.setImageResource(mainActionIcon);
                    mMainActionPushLayerView.setVisibility(View.VISIBLE);
                    mMainActionPushLayerView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(mainActionIntent);
                        }
                    });
                    mMainActionPushLayerView.setContentDescription(mainActionDescription);
                    mHeaderTextView.setVisibility(View.VISIBLE);
                    mHeaderOverlayView.setVisibility(View.VISIBLE);
                }
                // The following lines are provided and maintained by Mediatek
                // Inc.
                if (FeatureOption.MTK_GEMINI_SUPPORT ) {
                	 mSimName.setPadding(4, 2, 4, 2);
                    if (firstDetails.simId == ContactsUtils.CALL_TYPE_SIP) {
                        mSimName
                                .setBackgroundResource(com.mediatek.internal.R.drawable.sim_background_sip);
                        mSimName.setText(R.string.call_sipcall);
                        mSimName.setPadding(4, 2, 4, 2);
                    } else if (ContactsUtils.CALL_TYPE_NONE == firstDetails.simId) {
                        mSimName.setVisibility(View.INVISIBLE);
                    } else {
                        String simName = SIMInfoWrapper.getDefault().getSimDisplayNameById(
                                firstDetails.simId);
                        if (null != simName) {
                            mSimName.setText(simName);
                            mSimName.setPadding(4, 2, 4, 2);
                        } else {
                            mSimName.setVisibility(View.INVISIBLE);
                        }
                        int color = SIMInfoWrapper.getDefault().getInsertedSimColorById(
                                firstDetails.simId);
                        int simColorResId = SIMInfoWrapper.getDefault().getSimBackgroundResByColorId(color);
                        if (-1 != color) {
                            mSimName.setBackgroundResource(simColorResId);
                            mSimName.setPadding(4, 2, 4, 2);
                        } else {
                            mSimName
                                    .setBackgroundResource(com.mediatek.internal.R.drawable.sim_background_locked);
                            mSimName.setPadding(4, 2, 4, 2);
                        }
                    }
                } else {
                    mSimName.setVisibility(View.GONE);
                }
                // The previous lines are provided and maintained by Mediatek
                // Inc.
                // This action allows to call the number that places the call.
                if (canPlaceCallsTo) {
                    boolean isVoicemailUri = PhoneNumberHelper.isVoicemailUri(numberCallUri);
                    int slotId = SIMInfoWrapper.getDefault().getSimSlotById((int)firstDetails.simId);
                    
                    final CharSequence displayNumber =
                            mPhoneNumberHelper.getDisplayNumber(
                                    firstDetails.number, firstDetails.formattedNumber);

                    Intent it = new Intent(Intent.ACTION_CALL_PRIVILEGED, numberCallUri).putExtra(
                            Constants.EXTRA_ORIGINAL_SIM_ID, (long)firstDetails.simId);
                    it.setClassName(Constants.PHONE_PACKAGE, Constants.OUTGOING_CALL_BROADCASTER);
                    if (isVoicemailUri && slotId != -1) {
                        it.putExtra("simId", slotId);
                    }
                    ViewEntry entry = new ViewEntry(getString(R.string.menu_callNumber,
                            displayNumber),
                            it,
                            getString(R.string.description_call, nameOrNumber));
                    
                    // Only show a label if the number is shown and it is not a SIP address.
                    if (!TextUtils.isEmpty(firstDetails.name)
                            && !TextUtils.isEmpty(firstDetails.number)
                            && !PhoneNumberUtils.isUriNumber(firstDetails.number.toString())) {
                        /*
                         * New Feature by Mediatek Begin.
                         * Original Android's code: xxx
                         * M:AAS
                         */
                        AccountTypeManager atm = AccountTypeManager.getInstance(mContext);
                        String accountType = atm.getAccountTypeBySlot(slotId);
                        if(OperatorUtils.isAasEnabled(accountType)) {
                            entry.label = USIMAas.getUSIMAASById(slotId, firstDetails.numberType);
                            Log.i(TAG,"AAS: CallDetailActivity,getUSIMAASById, label = " + entry.label);//TODO:remove it.
                        } else {
                        /*
                         * New Feature by Mediatek End.
                         */
                            if (0 == firstDetails.numberType) {
                                entry.label = mResources.getString(R.string.list_filter_custom);
                            } else {
                                entry.label = Phone.getTypeLabel(mResources,
                                        firstDetails.numberType, firstDetails.numberLabel);
                            }
                        }
                    }

                    // The secondary action allows to send an SMS to the number that placed the
                    // call.
                    if (mPhoneNumberHelper.canSendSmsTo(mNumber) && hasSms) {
                        Intent itSecond = new Intent(Intent.ACTION_SENDTO,
                                Uri.fromParts("sms", mNumber, null));
                        entry.setSecondaryAction(
                                R.drawable.ic_text_holo_dark,
                                itSecond,
                                getString(R.string.description_send_text_message, nameOrNumber));
                    }
                    
                    // The following lines are provided and maintained by
                    // Mediatek Inc.
                    // For Video Call.
                    
                    if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {
                        Intent itThird = new Intent(Intent.ACTION_CALL_PRIVILEGED, numberCallUri)
                                .putExtra(Constants.EXTRA_IS_VIDEO_CALL, true).putExtra(
                                        Constants.EXTRA_ORIGINAL_SIM_ID, (long) firstDetails.simId);
                        itThird.setClassName(Constants.PHONE_PACKAGE,
                                Constants.OUTGOING_CALL_BROADCASTER);
                        if (isVoicemailUri && slotId != -1) {
                            itThird.putExtra("simId", slotId);
                        }
                        entry.setThirdAction(
                                getString(R.string.menu_videocallNumber, displayNumber), itThird,
                                getString(R.string.description_call, nameOrNumber));
                    } else {
                        entry.thirdIntent = null;
                    }

                    Intent itFourth = new Intent(
                            Intent.ACTION_CALL_PRIVILEGED, numberCallUri)
                    .putExtra(Constants.EXTRA_ORIGINAL_SIM_ID,
                            (long) firstDetails.simId).putExtra(
                            Constants.EXTRA_IS_IP_DIAL, true).putExtra(
                            Constants.EXTRA_ORIGINAL_SIM_ID,
                            (long) firstDetails.simId);
                    itFourth.setClassName(Constants.PHONE_PACKAGE, Constants.OUTGOING_CALL_BROADCASTER);
                    if (isVoicemailUri && slotId != -1) {
                        itFourth.putExtra("simId", slotId);
                    }
                    
                    entry.setFourthAction(getString(R.string.menu_ipcallNumber,
                            displayNumber), itFourth, getString(
                                    R.string.description_call, nameOrNumber));

                    entry.geocode = firstDetails.geocode;
                    // The previous lines are provided and maintained by
                    // Mediatek Inc.

                    configureCallButton(entry);
                /*
                     * New Feature by Mediatek Begin.
                     *   Original Android's code:
                     *     
                     *   CR ID: ALPS00308657
                     *   Descriptions: RCS
                     */

                    if (mCallLogExtention == null) {
                        mCallLogExtention = CallLogExtentionManager.getInstance()
                                .getCallLogExtention();
                    }
                    mCallLogExtention.setEXtenstionItem(CallDetailActivity.this, contactUri, firstDetails);
//                    setRCSItem(contactUri);
                    
                    
                    

                    /*
                     * New Feature by Mediatek End.
                     */
                    
                } else {
                    if (mCallLogExtention == null) {
                        mCallLogExtention = CallLogExtentionManager.getInstance()
                                .getCallLogExtention();
                    }
                    mCallLogExtention.disableCallButton(CallDetailActivity.this);
//                    disableCallButton();
                }

                mHasEditNumberBeforeCallOption =
                        canPlaceCallsTo && !isSipNumber;
                //mHasTrashOption = hasVoicemail();
                //mHasRemoveFromCallLogOption = !hasVoicemail();
                invalidateOptionsMenu();

                ListView historyList = (ListView) findViewById(R.id.history);
                /**
                * Change Feature by Mediatek Begin.
                * Original Android's Code:
                historyList.setAdapter(
                        new CallDetailHistoryAdapter(CallDetailActivity.this, mInflater,
                                mCallTypeHelper, details, hasVoicemail(), canPlaceCallsTo,
                                findViewById(R.id.controls)));

                * Descriptions:
                */
                
                //add condiser the number support rcs
                CallDetailHistoryAdapter adapter = mCallLogExtention.getCallDetailHistoryAdapter(CallDetailActivity.this,
                        mInflater, mCallTypeHelper, details, false, canPlaceCallsTo,
                        findViewById(R.id.controls),mNumber);
                        
                if(adapter != null && contactUri != null){
                    historyList.setAdapter(adapter);
                } else {
                    historyList.setAdapter(new CallDetailHistoryAdapter(CallDetailActivity.this,
                          mInflater, mCallTypeHelper, details, false, canPlaceCallsTo,
                          findViewById(R.id.controls)));
                }

                /**
                * Change Feature by Mediatek End.
                */

                BackScrollManager.bind(
                        new ScrollableHeader() {
                            private View mControls = findViewById(R.id.controls);
                            private View mPhoto = findViewById(R.id.contact_background_sizer);
                            private View mHeader = findViewById(R.id.photo_text_bar);
                            private View mSeparator = findViewById(R.id.blue_separator);

                            @Override
                            public void setOffset(int offset) {
                                mControls.setY(-offset);
                            }

                            @Override
                            public int getMaximumScrollableHeaderOffset() {
                                // We can scroll the photo out, but we should keep the header if
                                // present.
                                if (mHeader.getVisibility() == View.VISIBLE) {
                                    return mPhoto.getHeight() - mHeader.getHeight();
                                } else {
                                    // If the header is not present, we should also scroll out the
                                    // separator line.
                                    return mPhoto.getHeight() + mSeparator.getHeight();
                                }
                            }
                        },
                        historyList);
                loadContactPhotos(photoUri);
                findViewById(R.id.call_detail).setVisibility(View.VISIBLE);
            }
        }
        mAsyncTaskExecutor.submit(Tasks.UPDATE_PHONE_CALL_DETAILS, new UpdateContactDetailsTask());
    }

    /** Return the phone call details for a given call log URI. */
    private PhoneCallDetails getPhoneCallDetailsForUri(Uri callUri) {
        ContentResolver resolver = getContentResolver();
//        Cursor callCursor = resolver.query(callUri, CALL_LOG_PROJECTION, null, null, null);
        Cursor callCursor = resolver.query(callUri, CallLogQuery.PROJECTION_CALLS_JOIN_DATAVIEW, null, null, null);
        
        try {
            if (callCursor == null || !callCursor.moveToFirst()) {
                throw new IllegalArgumentException("Cannot find content: " + callUri);
            }
            /**
             * Change Feature by Mediatek Begin.
             * Original Android's Code:
            // Read call log specifics.
            String number = callCursor.getString(NUMBER_COLUMN_INDEX);
            long date = callCursor.getLong(DATE_COLUMN_INDEX);
            long duration = callCursor.getLong(DURATION_COLUMN_INDEX);
            int callType = callCursor.getInt(CALL_TYPE_COLUMN_INDEX);
            String countryIso = callCursor.getString(COUNTRY_ISO_COLUMN_INDEX);
            final String geocode = callCursor.getString(GEOCODED_LOCATION_COLUMN_INDEX);
            // The following lines are provided and maintained by Mediatek Inc.
            int simId = callCursor.getInt(CALL_SIMID_COLUMN_INDEX);
            int vtCall = callCursor.getInt(CALL_VT_COLUMN_INDEX);
            log("number= " + number);
            log("date= " + date);
            log("duration= " + duration);
            log("callType= " + callType);
            log("countryIso= " + countryIso);
            log("geocode= " + geocode);
            log("simId= " + simId);
            log("vtCall= " + vtCall);
            // The previous lines are provided and maintained by Mediatek Inc.
            
            if (TextUtils.isEmpty(countryIso)) {
                countryIso = mDefaultCountryIso;
            }

            // Formatted phone number.
            final CharSequence formattedNumber;
            // Read contact specifics.
            final CharSequence nameText;
            final int numberType;
            final CharSequence numberLabel;
            final Uri photoUri;
            final Uri lookupUri;
            // If this is not a regular number, there is no point in looking it up in the contacts.
            ContactInfo info =
                    mPhoneNumberHelper.canPlaceCallsTo(number)
                    && !mPhoneNumberHelper.isVoiceMailNumberForMtk(number, simId)
                    && !mPhoneNumberHelper.isEmergencyNumber(number)
                            ? mContactInfoHelper.lookupNumber(number, countryIso)
                            : null;
            if (info == null) {
                formattedNumber = mPhoneNumberHelper.getDisplayNumber(number, null);
                nameText = "";
                numberType = 0;
                numberLabel = "";
                photoUri = null;
                lookupUri = null;
            } else {
                formattedNumber = info.formattedNumber;
                nameText = info.name;
                numberType = info.type;
                numberLabel = info.label;
                photoUri = info.photoUri;
                lookupUri = info.lookupUri;
            }
            
            * return new PhoneCallDetails(number, formattedNumber, countryIso, geocode,
                  *       new int[]{ callType }, date, duration,
                  *        nameText, numberType, numberLabel, lookupUri, photoUri);
             * Descriptions:
             */

            ContactInfo contactInfo = ContactInfo.fromCursor(callCursor);
			String photo = callCursor
					.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_PHOTO_URI);

			Uri photoUri = null;
			if (null != photo) {
				photoUri = Uri.parse(photo);
			}else {
				photoUri=null;
			}
			log("number = "+contactInfo.number);
            if (!mPhoneNumberHelper.canPlaceCallsTo(contactInfo.number)
                  || mPhoneNumberHelper.isVoiceMailNumberForMtk(contactInfo.number, contactInfo.simId)
                  || mPhoneNumberHelper.isEmergencyNumber(contactInfo.number)){
            	contactInfo.formattedNumber = mPhoneNumberHelper.getDisplayNumber(contactInfo.number, null).toString();
            	contactInfo.name = "";
            	contactInfo.nNumberTypeId = 0;
            	contactInfo.label = "";
            	photoUri = null;
            	contactInfo.lookupUri = null;
            }
            
            return new PhoneCallDetails(contactInfo.number, contactInfo.formattedNumber, 
                    contactInfo.countryIso, contactInfo.geocode,
                    contactInfo.type, contactInfo.date,
                    contactInfo.duration, contactInfo.name,
                    contactInfo.nNumberTypeId, contactInfo.label,
                    contactInfo.lookupUri, photoUri, contactInfo.simId,
                    contactInfo.vtCall, 0, contactInfo.ipPrefix);
			/**
			 * Change Feature by Mediatek End.
			 */
        } finally {
            if (callCursor != null) {
                callCursor.close();
            }
        }
    }

    /** Load the contact photos and places them in the corresponding views. */
    private void loadContactPhotos(Uri photoUri) {
        mContactPhotoManager.loadPhoto(mContactBackgroundView, photoUri, true, true);
    }

    static final class ViewEntry {
        public final String text;
        public final Intent primaryIntent;
        /** The description for accessibility of the primary action. */
        public final String primaryDescription;

        public CharSequence label = null;
        /** Icon for the secondary action. */
        public int secondaryIcon = 0;
        /** Intent for the secondary action. If not null, an icon must be defined. */
        public Intent secondaryIntent = null;
        /** The description for accessibility of the secondary action. */
        public String secondaryDescription = null;

        // The following lines are provided and maintained by Mediatek Inc.
        
        public CharSequence geocode = null;
        
        public int thirdIcon = 0;
        /**
         * Intent for the third action-vtCall. If not null, an icon must be
         * defined.
         */
        public Intent thirdIntent = null;
        public String thirdDescription = null;
        public String videoText ;
        
    	public Intent fourthIntent = null;
		public String fourthDescription = null;
		public String ipText;
        /** The description for accessibility of the third action. */

		public void setThirdAction(String text, Intent intent,
				String description) {
			videoText = text;
			thirdIntent = intent;
			thirdDescription = description;
		}

		/** The description for accessibility of the fourth action. */

		public void setFourthAction(String text, Intent intent,
				String description) {
			ipText = text;
			fourthIntent = intent;
			fourthDescription = description;
		}

        // The previous lines are provided and maintained by Mediatek Inc.
        
        public ViewEntry(String text, Intent intent, String description) {
            this.text = text;
            primaryIntent = intent;
            primaryDescription = description;
        }

        public void setSecondaryAction(int icon, Intent intent, String description) {
            secondaryIcon = icon;
            secondaryIntent = intent;
            secondaryDescription = description;
        }
    }

    /** Disables the call button area, e.g., for private numbers. */
    private void disableCallButton() {
        findViewById(R.id.call_and_sms).setVisibility(View.GONE);
        findViewById(R.id.separator01).setVisibility(View.GONE);
        findViewById(R.id.separator02).setVisibility(View.GONE);
        findViewById(R.id.video_call).setVisibility(View.GONE);
        findViewById(R.id.ip_call).setVisibility(View.GONE);
    }

    /** Configures the call button area using the given entry. */
    private void configureCallButton(ViewEntry entry) {
        View convertView = findViewById(R.id.call_and_sms);
        convertView.setVisibility(View.VISIBLE);

        ImageView icon = (ImageView) convertView.findViewById(R.id.call_and_sms_icon);
        View divider = convertView.findViewById(R.id.call_and_sms_divider);
        TextView text = (TextView) convertView.findViewById(R.id.call_and_sms_text);

        View mainAction = convertView.findViewById(R.id.call_and_sms_main_action);
        mainAction.setOnClickListener(mPrimaryActionListener);
        mainAction.setTag(entry);
        mainAction.setContentDescription(entry.primaryDescription);

        if (entry.secondaryIntent != null) {
            icon.setOnClickListener(mSecondaryActionListener);
            icon.setImageResource(entry.secondaryIcon);
            icon.setVisibility(View.VISIBLE);
            icon.setTag(entry);
            icon.setContentDescription(entry.secondaryDescription);
            divider.setVisibility(View.VISIBLE);
        } else {
            icon.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }
        
        
        text.setText(entry.text);

        TextView label = (TextView) convertView.findViewById(R.id.call_and_sms_label);
        if (TextUtils.isEmpty(entry.label)) {
            label.setVisibility(View.GONE);
        } else {
            label.setText(entry.label);
            label.setVisibility(View.VISIBLE);
            label.setPadding( 0, 0, this.getResources().getDimensionPixelSize(
                    R.dimen.call_log_inner_margin), 0);
        }
        
        // The following lines are provided and maintained by Mediatek Inc.
        //For video call 
        TextView geocode = (TextView) convertView.findViewById(R.id.call_number_geocode);
        View labelAndgeocodeView = (View) convertView.findViewById(R.id.labe_and_geocode_text);

        if (FeatureOption.MTK_PHONE_NUMBER_GEODESCRIPTION) {

            if (TextUtils.isEmpty(entry.geocode)) {
                geocode.setVisibility(View.GONE);
            } else {
                geocode.setText(entry.geocode);
                geocode.setVisibility(View.VISIBLE);
            }

            if (TextUtils.isEmpty(entry.label) && TextUtils.isEmpty(entry.geocode)) {
                labelAndgeocodeView.setVisibility(View.GONE);
            } else {
                labelAndgeocodeView.setVisibility(View.VISIBLE);
            }
        } else {
            geocode.setVisibility(View.GONE);

            if (TextUtils.isEmpty(entry.label)) {
                labelAndgeocodeView.setVisibility(View.GONE);
            } else {
                labelAndgeocodeView.setVisibility(View.VISIBLE);
            }
        }
        
        View separator01 = findViewById(R.id.separator01);
        separator01.setVisibility(View.VISIBLE);
        View separator02 = findViewById(R.id.separator02);
        separator02.setVisibility(View.VISIBLE);
        
        View convertView1 = findViewById(R.id.video_call);
        View videoAction = convertView1.findViewById(R.id.video_call_action);
        if (entry.thirdIntent != null) {
            videoAction.setOnClickListener(mThirdActionListener);
            videoAction.setTag(entry);
            videoAction.setContentDescription(entry.thirdDescription);
            videoAction.setVisibility(View.VISIBLE);
            TextView videoText = (TextView) convertView1.findViewById(R.id.video_call_text);

            videoText.setText(entry.videoText);

            TextView videoLabel = (TextView) convertView1.findViewById(R.id.video_call_label);
            if (TextUtils.isEmpty(entry.label)) {
                videoLabel.setVisibility(View.GONE);
            } else {
                videoLabel.setText(entry.label);
                videoLabel.setVisibility(View.VISIBLE);
            }
        } else {
            separator01.setVisibility(View.GONE);
            convertView1.setVisibility(View.GONE);
        }
        
        //For IP call
        View convertView2 = findViewById(R.id.ip_call);
        View ipAction = convertView2.findViewById(R.id.ip_call_action);
        ipAction.setOnClickListener(mFourthActionListener);
        ipAction.setTag(entry);
        ipAction.setContentDescription(entry.fourthDescription);
        TextView ipText = (TextView) convertView2.findViewById(R.id.ip_call_text);

        ipText.setText(entry.ipText);

        TextView ipLabel = (TextView) convertView2.findViewById(R.id.ip_call_label);
        if (TextUtils.isEmpty(entry.label)) {
        	ipLabel.setVisibility(View.GONE);
        } else {
        	ipLabel.setText(entry.label);
        	ipLabel.setVisibility(View.VISIBLE);
        }
        
        // The previous lines are provided and maintained by Mediatek Inc.
    }
    
    // The following lines are deleted by Mediatek Inc to close Google default
    // Voicemail function.
 /**
  * 
    protected void updateVoicemailStatusMessage(Cursor statusCursor) {
        if (statusCursor == null) {
            mStatusMessageView.setVisibility(View.GONE);
            return;
        }
        final StatusMessage message = getStatusMessage(statusCursor);
        if (message == null || !message.showInCallDetails()) {
            mStatusMessageView.setVisibility(View.GONE);
            return;
        }

        mStatusMessageView.setVisibility(View.VISIBLE);
        mStatusMessageText.setText(message.callDetailsMessageId);
        if (message.actionMessageId != -1) {
            mStatusMessageAction.setText(message.actionMessageId);
        }
        if (message.actionUri != null) {
            mStatusMessageAction.setClickable(true);
            mStatusMessageAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, message.actionUri));
                }
            });
        } else {
            mStatusMessageAction.setClickable(false);
        }
    }

    private StatusMessage getStatusMessage(Cursor statusCursor) {
        List<StatusMessage> messages = mVoicemailStatusHelper.getStatusMessages(statusCursor);
        if (messages.size() == 0) {
            return null;
        }
        // There can only be a single status message per source package, so num of messages can
        // at most be 1.
        if (messages.size() > 1) {
            Log.w(TAG, String.format("Expected 1, found (%d) num of status messages." +
                    " Will use the first one.", messages.size()));
        }
        return messages.get(0);
    }
 */
    // The previous lines are deleted by Mediatek Inc to close Google default
    // Voicemail function.
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.call_details_options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /**
        * Change Feature by Mediatek Begin.
        * Original Android's Code:
        // This action deletes all elements in the group from the call log.
        // We don't have this action for voicemails, because you can just use the trash button.
        menu.findItem(R.id.menu_remove_from_call_log).setVisible(mHasRemoveFromCallLogOption);
        menu.findItem(R.id.menu_edit_number_before_call).setVisible(mHasEditNumberBeforeCallOption);
        menu.findItem(R.id.menu_trash).setVisible(mHasTrashOption);
        * Descriptions:
        */
        menu.findItem(R.id.menu_remove_from_call_log).setVisible(true);
        menu.findItem(R.id.menu_edit_number_before_call).setVisible(mHasEditNumberBeforeCallOption);
        /**
        * Change Feature by Mediatek End.
        */
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onHomeSelected();
                return true;
            }

            // All the options menu items are handled by onMenu... methods.
            default:
                throw new IllegalArgumentException();
        }
    }

    public void onMenuRemoveFromCallLog(MenuItem menuItem) {
        final StringBuilder callIds = new StringBuilder();
        for (Uri callUri : getCallLogEntryUris()) {
            if (callIds.length() != 0) {
                callIds.append(",");
            }
            callIds.append(ContentUris.parseId(callUri));
        }
        mAsyncTaskExecutor.submit(Tasks.REMOVE_FROM_CALL_LOG_AND_FINISH,
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    public Void doInBackground(Void... params) {
                        getContentResolver().delete(Calls.CONTENT_URI_WITH_VOICEMAIL,
                                Calls._ID + " IN (" + callIds + ")", null);
                        return null;
                    }

                    @Override
                    public void onPostExecute(Void result) {
                        finish();
                    }
                });
    }

    public void onMenuEditNumberBeforeCall(MenuItem menuItem) {
        startActivity(new Intent(Intent.ACTION_DIAL, mPhoneNumberHelper.getCallUri(mNumber)));
    }
    // The following lines are deleted by Mediatek Inc to close Google default
    // Voicemail function.
  /**
   * 
    public void onMenuTrashVoicemail(MenuItem menuItem) {
        final Uri voicemailUri = getVoicemailUri();
        mAsyncTaskExecutor.submit(Tasks.DELETE_VOICEMAIL_AND_FINISH,
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    public Void doInBackground(Void... params) {
                        getContentResolver().delete(voicemailUri, null, null);
                        return null;
                    }
                    @Override
                    public void onPostExecute(Void result) {
                        finish();
                    }
                });
    }
  */
    // The previous lines are deleted by Mediatek Inc to close Google default
    // Voicemail function.

    private void configureActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME);
        }
    }

    /** Invoked when the user presses the home button in the action bar. */
    private void onHomeSelected() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Calls.CONTENT_URI);
        // This will open the call log even if the detail view has been opened directly.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        // Immediately stop the proximity sensor.
        disableProximitySensor(false);
        mProximitySensorListener.clearPendingRequests();
        
      //The following lines are provided and maintained by Mediatek Inc.
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            mShowSimIndicator = false;
            setSimIndicatorVisibility(false);
        }
        //The previous lines are provided and maintained by Mediatek Inc.
        
        super.onPause();
    }

    @Override
    public void enableProximitySensor() {
        mProximitySensorManager.enable();
    }

    @Override
    public void disableProximitySensor(boolean waitForFarState) {
        mProximitySensorManager.disable(waitForFarState);
    }
    
    @Override
    protected void onDestroy() {
        log("onDestroy()");
        super.onDestroy();
        SIMInfoWrapper.getDefault().unregisterForSimInfoUpdate(mHandler);
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            unregisterReceiver(mReceiver);
        }
    }

    // The following lines are provided and maintained by Mediatek Inc.
    private TextView mSimName;
    private boolean hasSms = true;

    static final int CALL_SIMID_COLUMN_INDEX = 6;
    static final int CALL_VT_COLUMN_INDEX = 7;
    
	private final View.OnClickListener mThirdActionListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			startActivity(((ViewEntry) view.getTag()).thirdIntent);
		}
	};
    
    private final View.OnClickListener mFourthActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(((ViewEntry) view.getTag()).fourthIntent);
        }
    };

    private static final int SIM_INFO_UPDATE_MESSAGE = 100;

    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SIM_INFO_UPDATE_MESSAGE:
                    updateData(getCallLogEntryUris());
                    break;
                default:
                    break;
            }
        }
    };

    public StatusBarManager mStatusBarMgr;
    private BroadcastReceiver mReceiver = new CallDetailBroadcastReceiver();
    private boolean mShowSimIndicator = false;

    void setSimIndicatorVisibility(boolean visible) {
        if(visible)
            mStatusBarMgr.showSIMIndicator(getComponentName(), Settings.System.VOICE_CALL_SIM_SETTING);
        else
            mStatusBarMgr.hideSIMIndicator(getComponentName());
    }


    private class CallDetailBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            log("CallDetailBroadcastReceiver, onReceive action = " + action);

            if(Intent.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED.equals(action)) {
                if(FeatureOption.MTK_GEMINI_SUPPORT) {
                    if (mShowSimIndicator) {
                        setSimIndicatorVisibility(true);
                    }
                }
            }
        }
    }

    private void log(final String msg) {
        if (true) {
            Log.d(TAG, msg);
        }
    }
    
    public static final String EXTRA_CALL_LOG_NAME = "EXTRA_CALL_LOG_NAME";
    public static final String EXTRA_CALL_LOG_NUMBER_TYPE = "EXTRA_CALL_LOG_NUMBER_TYPE";
    public static final String EXTRA_CALL_LOG_PHOTO_URI = "EXTRA_CALL_LOG_PHOTO_URI";
    public static final String EXTRA_CALL_LOG_LOOKUP_URI = "EXTRA_CALL_LOG_LOOKUP_URI";
    
    // get CallLogExtention
    private CallLogExtention mCallLogExtention;
    // The previous lines are provided and maintained by Mediatek Inc.
}
