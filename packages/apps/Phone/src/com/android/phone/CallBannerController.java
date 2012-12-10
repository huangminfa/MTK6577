package com.android.phone;

import java.util.Date;

import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import android.provider.ContactsContract.Contacts;
import android.provider.Telephony.SIMInfo;
import com.android.internal.telephony.CallManager;
import com.android.phone.PhoneFeatureConstants.FeatureOption;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Connection;
import android.pim.ContactsAsyncHelper;
import com.android.internal.telephony.CallerInfo;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.CallerInfoAsyncQuery;

public class CallBannerController implements CallerInfoAsyncQuery.OnQueryCompleteListener{

    private static final String LOG_TAG = "CallBannerController";
    private static final boolean DBG = true;//(PhoneApp.DBG_LEVEL >= 2);

    protected CallBanner mCallBanner;
    protected SIMInfo mSimInfo;
    protected Context mContext;
    protected PhoneApp mApplication;
    // Cached DisplayMetrics density.
    protected float mDensity;
    // Text colors, used for various labels / titles
    private int mTextColorCallTypeSip;
    // Track the state for the photo.
    private int mCallBannerSidePadding;
    private int mCallBannerTopBottomPadding;
    protected ContactsAsyncHelper.ImageTracker mPhotoTracker;
    
    private boolean mNeedClearUserData = false;
    
    private int[] mSimColorMap = {
            R.drawable.incall_status_color0,
            R.drawable.incall_status_color1,
            R.drawable.incall_status_color2,
            R.drawable.incall_status_color3,
            R.drawable.incall_status_color4,
            R.drawable.incall_status_color5,
            R.drawable.incall_status_color6,
            R.drawable.incall_status_color7,
        };

    public CallBannerController(CallBanner callBanner, Context context) {
        mCallBanner = callBanner;
        mContext = context;
        mApplication = PhoneApp.getInstance();
        mDensity = context.getResources().getDisplayMetrics().density;
        mCallBannerSidePadding = context.getResources().getDimensionPixelSize(R.dimen.call_banner_side_padding);
        mCallBannerTopBottomPadding = context.getResources().getDimensionPixelSize(R.dimen.call_banner_top_bottom_padding);
        // Text colors
        mTextColorCallTypeSip = context.getResources().getColor(R.color.incall_callTypeSip);
        // create a new object to track the state for the photo.
        mPhotoTracker = new ContactsAsyncHelper.ImageTracker();
    }
    
    void updateState(Call call) {
        if (DBG) log("updateState(" + call + ")...");

        if (!VTInCallScreenFlags.getInstance().mVTFullScreen) {
            mCallBanner.setVisibility(View.VISIBLE);
        }
        
        // !!!! Can be updated for decreasing sim info updating count during one call
        // get sim information: display name and color to mSimInfo
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            mSimInfo = PhoneUtils.getVTUsedSimInfo();
            if (null != mSimInfo) {
                if (!TextUtils.isEmpty(mSimInfo.mDisplayName)) {
                    mCallBanner.mSimIndicator.setText(mSimInfo.mDisplayName);
                    mCallBanner.mSimIndicator.setVisibility(View.VISIBLE);
                } else {
                    mCallBanner.mSimIndicator.setVisibility(View.INVISIBLE);
                }
            } else {
                // Sim card is not inserted, set call banner invisible
                mCallBanner.setVisibility(View.INVISIBLE);
                return;
            }
        } else {
            mCallBanner.mSimIndicator.setVisibility(View.INVISIBLE);
        }
        
        updateCallBannerBackground(call, mCallBanner.mMainCallBanner);
        String operatorName = PhoneUtils.getVTNetworkOperatorName();
        if (null != operatorName) {
            mCallBanner.mOperatorName.setText(operatorName);
            mCallBanner.mOperatorName.setVisibility(View.VISIBLE);
        }
        
        if (null == call) {
            return;
        }
        updateCallStateWidgets(call);
        Connection conn = null;
        int phoneType = call.getPhone().getPhoneType();
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            conn = call.getLatestConnection();
        } else if ((phoneType == Phone.PHONE_TYPE_GSM)
              || (phoneType == Phone.PHONE_TYPE_SIP)) {
            conn = call.getEarliestConnection();
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }

        if (conn == null) {
            if (DBG) log("updateState: connection is null, using default values.");
            // if the connection is null, we run through the behaviour
            // we had in the past, which breaks down into trivial steps
            // with the current implementation of getCallerInfo and
            // updateDisplayForPerson.
            CallerInfo info = PhoneUtils.getCallerInfo(mContext, null /* conn */);
            updateDisplayForPerson(info, Connection.PRESENTATION_ALLOWED, false, call, conn);
        } else {
            if (DBG) log("  - CONN: " + conn + ", state = " + conn.getState());
            int presentation = conn.getNumberPresentation();

            // make sure that we only make a new query when the current
            // callerinfo differs from what we've been requested to display.
            boolean runQuery = true;
            Object o = conn.getUserData();
            if (mNeedClearUserData) {
                if (DBG) log("language changed to clear userdata");
                conn.clearUserData();
                mNeedClearUserData = false;
                runQuery = true;
            } else if (o instanceof PhoneUtils.CallerInfoToken) {
                runQuery = mPhotoTracker.isDifferentImageRequest(
                        ((PhoneUtils.CallerInfoToken) o).currentInfo);
            } else {
                runQuery = mPhotoTracker.isDifferentImageRequest(conn);
            }

            if (runQuery) {
                if (DBG) log("- updateState: starting CallerInfo query...");
                PhoneUtils.CallerInfoToken info =
                        PhoneUtils.startGetCallerInfo(mContext, conn, this, call);
                updateDisplayForPerson(info.currentInfo, presentation, !info.isFinal,
                                       call, conn);
            } else {
                // No need to fire off a new query.  We do still need
                // to update the display, though (since we might have
                // previously been in the "conference call" state.)
                if (DBG) log("- updateState: using data we already have...");
                if (o instanceof CallerInfo) {
                    CallerInfo ci = (CallerInfo) o;
                    // Update CNAP information if Phone state change occurred
                    ci.cnapName = conn.getCnapName();
                    ci.numberPresentation = conn.getNumberPresentation();
                    ci.namePresentation = conn.getCnapNamePresentation();
                    if (DBG) log("- updateState: CNAP data from Connection: "
                            + "CNAP name=" + ci.cnapName
                            + ", Number/Name Presentation=" + ci.numberPresentation);
                    if (DBG) log("   ==> Got CallerInfo; updating display: ci = " + ci);
                    updateDisplayForPerson(ci, presentation, false, call, conn);
                } else if (o instanceof PhoneUtils.CallerInfoToken){
                    CallerInfo ci = ((PhoneUtils.CallerInfoToken) o).currentInfo;
                    if (DBG) log("- updateState: CNAP data from Connection: "
                            + "CNAP name=" + ci.cnapName
                            + ", Number/Name Presentation=" + ci.numberPresentation);
                    if (DBG) log("   ==> Got CallerInfoToken; updating display: ci = " + ci);
                    updateDisplayForPerson(ci, presentation, true, call, conn);
                } else {
                    Log.w(LOG_TAG, "updateState: runQuery was false, "
                          + "but we didn't have a cached CallerInfo object!  o = " + o);
                    // TODO: any easy way to recover here (given that
                    // the CallCard is probably displaying stale info
                    // right now?)  Maybe force the CallCard into the
                    // "Unknown" state?
                }
            }
        }
    }
    
    void updateElapsedTimeWidget(final long timeElapsed) {
        if (DBG) log("updateElapsedTimeWidget: " + timeElapsed);
        /**
         * change by mediatek .inc
         * description : show 00:00 when timmElapsed
         * is zero to improve UX
         */
        if (timeElapsed < 0) {
            mCallBanner.mCallStateLabel.setText("");
        } else {
            String time = DateUtils.formatElapsedTime(timeElapsed);
            if (DBG) log("updateElapsedTimeWidget: " + timeElapsed);
            mCallBanner.mCallStateLabel.setText(time);
        }
    }

    private void updateCallBannerBackground(Call call, ViewGroup callBanner) {
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            if (null == mSimInfo || null == mSimColorMap || mSimInfo.mColor < 0
                    || mSimInfo.mColor >= mSimColorMap.length) {
                if (DBG) log("mSimInfo is null or mSimInfo.mColor invalid, do not update background");
                return;
            }
            callBanner.setBackgroundResource(mSimColorMap[mSimInfo.mColor]);
        } else {
            callBanner.setBackgroundResource(R.drawable.incall_status_color3);
        }
        callBanner.setPadding(mCallBannerSidePadding, mCallBannerTopBottomPadding, mCallBannerSidePadding, mCallBannerTopBottomPadding);
    }
    
    private void updateCallStateWidgets(Call call) {
        if (DBG) log("updateCallStateWidgets(call " + call + ")...");
        final Call.State state = call.getState();
        final Phone phone = call.getPhone();
        final int phoneType = phone.getPhoneType();

        String callStateLabel = null;  // Label to display as part of the call banner
        //int bluetoothIconId = 0;  // Icon to display alongside the call state label

        // google default did not include mCallTime operation here,
        // Mtk modify the GUI, Call Timer is moved to call state widgets,
        // so operate mCallTime here
        switch (state) {
            case IDLE:
                // "Call state" is meaningless in this state.
                // The "main CallCard" should never be trying to display
                // an idle call!  In updateState(), if the phone is idle,
                // we call updateNoCall(), which means that we shouldn't
                // have passed a call into this method at all.
                Log.w(LOG_TAG, "displayMainCallStatus: IDLE call in the main call card!");

                // (It is possible, though, that we had a valid call which
                // became idle *after* the check in updateState() but
                // before we get here...  So continue the best we can,
                // with whatever (stale) info we can get from the
                // passed-in Call object.)
                break;

            case ACTIVE:
                if (null != call.getLatestConnection()
                        && VTCallUtils.VTTimingMode.VT_TIMING_DEFAULT == VTCallUtils.checkVTTimingMode(call.getLatestConnection().getAddress())) {
                    long duration = CallTime.getCallDuration(call);  // msec
                    callStateLabel = DateUtils.formatElapsedTime(duration / 1000);
                }
                break;

            case HOLDING:
                callStateLabel = mContext.getString(R.string.card_title_on_hold);
                break;

            case DIALING:
            case ALERTING:
                callStateLabel = mContext.getString(R.string.card_title_dialing);
                break;

            case INCOMING:
            case WAITING:
                // Also, display a special icon (alongside the "Incoming call"
                // label) if there's an incoming call and audio will be routed
                // to bluetooth when you answer it.
                /*
                if (PhoneApp.getInstance().showBluetoothIndication()) {
                    bluetoothIconId = R.drawable.ic_incoming_call_bluetooth;
                }*/
                break;

            case DISCONNECTING:
                // While in the DISCONNECTING state we display a "Hanging up"
                // message in order to make the UI feel more responsive.  (In
                // GSM it's normal to see a delay of a couple of seconds while
                // negotiating the disconnect with the network, so the "Hanging
                // up" state at least lets the user know that we're doing
                // something.  This state is currently not used with CDMA.)
                callStateLabel = mContext.getString(R.string.card_title_hanging_up);
                // MTK_OP01_PROTECT_START
                if ("OP01".equals(PhoneUtils.getOptrProperties())) {
                    if (VTInCallScreenFlags.getInstance().mVTConnectionStarttime.mStartDate > 0) {
                        String sTime = mContext.getString(R.string.vt_start_time_from) + " " +
                            DateFormat.getTimeFormat(mContext).format(new Date(VTInCallScreenFlags.getInstance().mVTConnectionStarttime.mStartDate));
                        if (!callStateLabel.isEmpty()) {
                            callStateLabel += ", ";
                        }
                        callStateLabel += sTime;
                    }
                }
                // MTK_OP01_PROTECT_END
                break;

            case DISCONNECTED:
                callStateLabel = getCallFailedString(call);
                // MTK_OP01_PROTECT_START
                if ("OP01".equals(PhoneUtils.getOptrProperties())) {
                    if (VTInCallScreenFlags.getInstance().mVTConnectionStarttime.mStartDate > 0) {
                        String sTime = mContext.getString(R.string.vt_start_time_from) + " " +
                            DateFormat.getTimeFormat(mContext).format(new Date(VTInCallScreenFlags.getInstance().mVTConnectionStarttime.mStartDate));
                        if (!callStateLabel.isEmpty()) {
                            callStateLabel += ", ";
                        }
                        callStateLabel += sTime;
                    }
                }
                // MTK_OP01_PROTECT_END
                break;

            default:
                Log.wtf(LOG_TAG, "updateCallStateWidgets: unexpected call state: " + state);
                break;
        }

        // Check a couple of other special cases (these are all CDMA-specific).

        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            if ((state == Call.State.ACTIVE)
                && mApplication.cdmaPhoneCallState.IsThreeWayCallOrigStateDialing()) {
                // Display "Dialing" while dialing a 3Way call, even
                // though the foreground call state is actually ACTIVE.
                callStateLabel = mContext.getString(R.string.card_title_dialing);
            } else if (PhoneApp.getInstance().notifier.getIsCdmaRedialCall()) {
                callStateLabel = mContext.getString(R.string.card_title_redialing);
            }
        }
        if (PhoneUtils.isPhoneInEcm(phone)) {
            // In emergency callback mode (ECM), use a special label
            // that shows your own phone number.
            callStateLabel = PhoneUtils.getECMCardTitle(mContext, phone);
        }

        if (DBG) log("==> callStateLabel: '" + callStateLabel + "'");
        mCallBanner.mCallStateLabel.setVisibility(View.VISIBLE);
        mCallBanner.mCallStateLabel.setText(callStateLabel);
    }
    
    /**
     * Updates the name / photo / number / label fields on the CallCard
     * based on the specified CallerInfo.
     *
     * If the current call is a conference call, use
     * updateDisplayForConference() instead.
     */
    private void updateDisplayForPerson(CallerInfo info,
                                        int presentation,
                                        boolean isTemporary,
                                        Call call,
                                        Connection conn) {
        if (DBG) log("updateDisplayForPerson(" + info + ")\npresentation:" +
                     presentation + " isTemporary:" + isTemporary);

        // inform the state machine that we are displaying a photo.
        mPhotoTracker.setPhotoRequest(info);
        mPhotoTracker.setPhotoState(ContactsAsyncHelper.ImageTracker.DISPLAY_IMAGE);
        
        // The actual strings we're going to display onscreen:
        String displayName;
        String displayNumber = null;
        String label = null;
        Uri personUri = null;
        String socialStatusText = null;
        Drawable socialStatusBadge = null;
        String numberGeoDescription = null;

        if (info != null) {
            // It appears that there is a small change in behaviour with the
            // PhoneUtils' startGetCallerInfo whereby if we query with an
            // empty number, we will get a valid CallerInfo object, but with
            // fields that are all null, and the isTemporary boolean input
            // parameter as true.

            // In the past, we would see a NULL callerinfo object, but this
            // ends up causing null pointer exceptions elsewhere down the
            // line in other cases, so we need to make this fix instead. It
            // appears that this was the ONLY call to PhoneUtils
            // .getCallerInfo() that relied on a NULL CallerInfo to indicate
            // an unknown contact.

            // Currently, info.phoneNumber may actually be a SIP address, and
            // if so, it might sometimes include the "sip:" prefix.  That
            // prefix isn't really useful to the user, though, so strip it off
            // if present.  (For any other URI scheme, though, leave the
            // prefix alone.)
            // TODO: It would be cleaner for CallerInfo to explicitly support
            // SIP addresses instead of overloading the "phoneNumber" field.
            // Then we could remove this hack, and instead ask the CallerInfo
            // for a "user visible" form of the SIP address.
            String number = info.phoneNumber;
            if ((number != null) && number.startsWith("sip:")) {
                number = number.substring(4);
            }
            number = HyphonManager.getInstance().formatNumber(number);
            
            if (TextUtils.isEmpty(info.name)) {
                // No valid "name" in the CallerInfo, so fall back to
                // something else.
                // (Typically, we promote the phone number up to the "name" slot
                // onscreen, and possibly display a descriptive string in the
                // "number" slot.)
                if (TextUtils.isEmpty(number)) {
                    // No name *or* number!  Display a generic "unknown" string
                    // (or potentially some other default based on the presentation.)
                    displayName =  getPresentationString(presentation);
                    if (DBG) log("  ==> no name *or* number! displayName = " + displayName);
                } else if (presentation != Connection.PRESENTATION_ALLOWED) {
                    // This case should never happen since the network should never send a phone #
                    // AND a restricted presentation. However we leave it here in case of weird
                    // network behavior
                    displayName = getPresentationString(presentation);
                    if (DBG) log("  ==> presentation not allowed! displayName = " + displayName);
                } else if (!TextUtils.isEmpty(info.cnapName)) {
                    // No name, but we do have a valid CNAP name, so use that.
                    displayName = info.cnapName;
                    info.name = info.cnapName;
                    displayNumber = number;
                    if (DBG) log("  ==> cnapName available: displayName '"
                                 + displayName + "', displayNumber '" + displayNumber + "'");
                } else {
                    // No name; all we have is a number.  This is the typical
                    // case when an incoming call doesn't match any contact,
                    // or if you manually dial an outgoing number using the
                    // dialpad.

                    // Promote the phone number up to the "name" slot:
                    displayName = number;

                    // ...and use the "number" slot for a geographical description
                    // string if available (but only for incoming calls.)
                    //if ((conn != null) && (conn.isIncoming())) {
                    if (conn != null) {
                        // TODO (CallerInfoAsyncQuery cleanup): Fix the CallerInfo
                        // query to only do the geoDescription lookup in the first
                        // place for incoming calls.
                        displayNumber = info.geoDescription;  // may be null
                    }

                    if (DBG) log("  ==>  no name; falling back to number: displayName '"
                                 + displayName + "', displayNumber '" + displayNumber + "'");
                }
            } else {
                // We do have a valid "name" in the CallerInfo.  Display that
                // in the "name" slot, and the phone number in the "number" slot.
                if (presentation != Connection.PRESENTATION_ALLOWED) {
                    // This case should never happen since the network should never send a name
                    // AND a restricted presentation. However we leave it here in case of weird
                    // network behavior
                    displayName = getPresentationString(presentation);
                    if (DBG) log("  ==> valid name, but presentation not allowed!"
                                 + " displayName = " + displayName);
                } else {
                    displayName = info.name;
                    displayNumber = number;
                    /*
                     * New Feature by Mediatek Begin.
                     * M:AAS
                     */
                    log("  ==> info.accountType: " + info.accountType);
                    if (info.accountType != null && PhoneUtils.isAasEnabled(info.accountType)) {
                        label = PhoneUtils.getAasLabel(info);
                    } else {
                        /*
                         * New Feature by Mediatek End.
                         */
                        label = info.phoneLabel;
                    }

                    if (DBG) log("  ==>  name is present in CallerInfo: displayName '"
                                 + displayName + "', displayNumber '" + displayNumber + "'");
                    if (FeatureOption.MTK_PHONE_NUMBER_GEODESCRIPTION) {
                        numberGeoDescription = info.geoDescription;
                        if (DBG) log("  ==>  name is present in CallerInfo: numberGeooDescription '" + numberGeoDescription + "'");
                    }
                }
            }
            personUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, info.person_id);
            if (DBG) log("- got personUri: '" + personUri
                         + "', based on info.person_id: " + info.person_id);
        } else {
            if (DBG) log("- info is null, just return ");
            return;
        }

        if (call.isGeneric()) {
            mCallBanner.mName.setText(R.string.card_title_in_call);
        } else {
            mCallBanner.mName.setText(displayName);
        }
        mCallBanner.mName.setVisibility(View.VISIBLE);

        if (displayNumber != null && !call.isGeneric()) {
            mCallBanner.mPhoneNumber.setText(displayNumber);
            mCallBanner.mPhoneNumber.setVisibility(View.VISIBLE);
        } else {
            mCallBanner.mPhoneNumber.setVisibility(View.GONE);
        }

        if (label != null && !call.isGeneric()) {
            mCallBanner.mLabel.setText(label);
            mCallBanner.mLabel.setVisibility(View.VISIBLE);
        } else {
            mCallBanner.mLabel.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(numberGeoDescription)) {
            mCallBanner.mPhoneNumberGeoDescription.setVisibility(View.INVISIBLE);
        } else {
            // MTK_OP01_PROTECT_START
            if ("OP01".equals(PhoneUtils.getOptrProperties())
                    && (Call.State.DISCONNECTING == call.getState() || Call.State.DISCONNECTED == call.getState())
                    && VTInCallScreenFlags.getInstance().mVTConnectionStarttime.mStartDate > 0) {
                mCallBanner.mPhoneNumberGeoDescription.setVisibility(View.INVISIBLE);
            } else {
            // MTK_OP01_PROTECT_END
                mCallBanner.mPhoneNumberGeoDescription.setText(numberGeoDescription);
                mCallBanner.mPhoneNumberGeoDescription.setVisibility(View.VISIBLE);
            // MTK_OP01_PROTECT_START
            }
            // MTK_OP01_PROTECT_END
        }

        // Other text fields:
        updateCallTypeLabel(call);
        updateSocialStatus(socialStatusText, socialStatusBadge, call);  // Currently unused
    }

    /**
     * Updates the "Call type" label, based on the current foreground call.
     * This is a special label and/or branding we display for certain
     * kinds of calls.
     *
     * (So far, this is used only for SIP calls, which get an
     * "Internet call" label.  TODO: But eventually, the telephony
     * layer might allow each pluggable "provider" to specify a string
     * and/or icon to be displayed here.)
     */
    private void updateCallTypeLabel(Call call) {
        int phoneType = (call != null) ? call.getPhone().getPhoneType() : Phone.PHONE_TYPE_NONE;
        if (phoneType == Phone.PHONE_TYPE_SIP) {
            mCallBanner.mCallTypeLabel.setVisibility(View.VISIBLE);
            mCallBanner.mCallTypeLabel.setText(R.string.incall_call_type_label_sip);
            mCallBanner.mCallTypeLabel.setTextColor(mTextColorCallTypeSip);
        } else {
            mCallBanner.mCallTypeLabel.setVisibility(View.GONE);
        }
    }
    
    /**
     * Updates the "social status" label with the specified text and
     * (optional) badge.
     */
    private void updateSocialStatus(String socialStatusText,
                                    Drawable socialStatusBadge,
                                    Call call) {
        // The socialStatus field is *only* visible while an incoming call
        // is ringing, never in any other call state.
        if ((socialStatusText != null)
                && (call != null)
                && call.isRinging()
                && !call.isGeneric()) {
            mCallBanner.mSocialStatus.setVisibility(View.VISIBLE);
            mCallBanner.mSocialStatus.setText(socialStatusText);
            mCallBanner.mSocialStatus.setCompoundDrawablesWithIntrinsicBounds(
                    socialStatusBadge, null, null, null);
            mCallBanner.mSocialStatus.setCompoundDrawablePadding((int) (mDensity * 6));
        } else {
            mCallBanner.mSocialStatus.setVisibility(View.GONE);
        }
    }
    
    private String getPresentationString(int presentation) {
        String name = mContext.getString(R.string.unknown);
        if (presentation == Connection.PRESENTATION_RESTRICTED) {
            name = mContext.getString(R.string.private_num);
        } else if (presentation == Connection.PRESENTATION_PAYPHONE) {
            name = mContext.getString(R.string.payphone);
        }
        return name;
    }
    
    private String getCallFailedString(Call call) {
        Connection c = call.getEarliestConnection();
        int resID;

        if (c == null) {
            if (DBG) log("getCallFailedString: connection is null, using default values.");
            // if this connection is null, just assume that the
            // default case occurs.
            resID = R.string.card_title_call_ended;
        } else {

            Connection.DisconnectCause cause = c.getDisconnectCause();

            // TODO: The card *title* should probably be "Call ended" in all
            // cases, but if the DisconnectCause was an error condition we should
            // probably also display the specific failure reason somewhere...

            switch (cause) {
                case BUSY:
                    resID = R.string.callFailed_userBusy;
                    break;

                case CONGESTION:
                    resID = R.string.callFailed_congestion;
                    break;

                case TIMED_OUT:
                    resID = R.string.callFailed_timedOut;
                    break;

                case SERVER_UNREACHABLE:
                    resID = R.string.callFailed_server_unreachable;
                    break;

                case NUMBER_UNREACHABLE:
                    resID = R.string.callFailed_number_unreachable;
                    break;

                case INVALID_CREDENTIALS:
                    resID = R.string.callFailed_invalid_credentials;
                    break;

                case SERVER_ERROR:
                    resID = R.string.callFailed_server_error;
                    break;

                case OUT_OF_NETWORK:
                    resID = R.string.callFailed_out_of_network;
                    break;

                case LOST_SIGNAL:
                case CDMA_DROP:
                    resID = R.string.callFailed_noSignal;
                    break;

                case LIMIT_EXCEEDED:
                    resID = R.string.callFailed_limitExceeded;
                    break;

                case POWER_OFF:
                    resID = R.string.callFailed_powerOff;
                    break;

                case ICC_ERROR:
                    resID = R.string.callFailed_simError;
                    break;

                case OUT_OF_SERVICE:
                    resID = R.string.callFailed_outOfService;
                    break;

                case INVALID_NUMBER:
                case UNOBTAINABLE_NUMBER:
                    resID = R.string.callFailed_unobtainable_number;
                    break;

                default:
                    resID = R.string.card_title_call_ended;
                    break;
            }
        }
        return mContext.getString(resID);
    }

    public void onQueryComplete(int token, Object cookie, CallerInfo ci) {
        if (DBG) log("onQueryComplete: token " + token + ", cookie " + cookie + ", ci " + ci);

        if (cookie instanceof Call) {
            // grab the call object and update the display for an individual call,
            // as well as the successive call to update image via call state.
            // If the object is a textview instead, we update it as we need to.
            if (DBG) log("callerinfo query complete, updating ui from displayMainCallStatus()");
            Call call = (Call) cookie;
            Connection conn = null;
            int phoneType = call.getPhone().getPhoneType();
            if (phoneType == Phone.PHONE_TYPE_CDMA) {
                conn = call.getLatestConnection();
            } else if ((phoneType == Phone.PHONE_TYPE_GSM)
                  || (phoneType == Phone.PHONE_TYPE_SIP)) {
                conn = call.getEarliestConnection();
            } else {
                throw new IllegalStateException("Unexpected phone type: " + phoneType);
            }
            PhoneUtils.CallerInfoToken cit =
                   PhoneUtils.startGetCallerInfo(mContext, conn, this, null);

            int presentation = Connection.PRESENTATION_ALLOWED;
            if (conn != null) presentation = conn.getNumberPresentation();
            if (DBG) log("- onQueryComplete: presentation=" + presentation
                    + ", contactExists=" + ci.contactExists);

            // Depending on whether there was a contact match or not, we want to pass in different
            // CallerInfo (for CNAP). Therefore if ci.contactExists then use the ci passed in.
            // Otherwise, regenerate the CIT from the Connection and use the CallerInfo from there.
            if (ci.contactExists) {
                updateDisplayForPerson(ci, Connection.PRESENTATION_ALLOWED, false, call, conn);
            } else {
                updateDisplayForPerson(cit.currentInfo, presentation, false, call, conn);
            }
            //updatePhotoForCallState(call);

        } else if (cookie instanceof TextView){
            if (DBG) log("callerinfo query complete, updating ui from ongoing or onhold");
            ((TextView) cookie).setText(PhoneUtils.getCompactNameFromCallerInfo(ci, mContext));
        }
    }
    
    public void clearCallBannerInfo() {
        mCallBanner.mName.setText("");
        mCallBanner.mPhoneNumber.setText("");
        mCallBanner.mLabel.setText("");
        mCallBanner.mCallTypeLabel.setText("");
        mCallBanner.mSocialStatus.setText("");
        mCallBanner.mOperatorName.setText("");
        mCallBanner.mSimIndicator.setText("");
        mCallBanner.mSimIndicator.setVisibility(View.INVISIBLE);
        mCallBanner.mCallStateLabel.setText("");
        mCallBanner.mPhoneNumberGeoDescription.setText("");
    }
    
    public void setNeedClearUserData(final boolean isNeedClearUserData) {
        mNeedClearUserData = isNeedClearUserData;
    }
    
    private static void log(final String msg) {
        Log.d(LOG_TAG, msg);
    }
}
