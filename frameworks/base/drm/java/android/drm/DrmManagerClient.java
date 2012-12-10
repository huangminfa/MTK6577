/*
 * Copyright (C) 2010 The Android Open Source Project
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

package android.drm;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.mediatek.xlog.Xlog;

/**
 * The main programming interface for the DRM framework. An application must instantiate this class
 * to access DRM agents through the DRM framework.
 *
 */
public class DrmManagerClient {
    /**
     * Indicates that a request was successful or that no error occurred.
     */
    public static final int ERROR_NONE = 0;
    /**
     * Indicates that an error occurred and the reason is not known.
     */
    public static final int ERROR_UNKNOWN = -2000;

    private static final String TAG = "DrmManagerClient";

    private static final boolean OMA_DRM_FL_ONLY; // M: Forward Lock Only

    static {
        // Load the respective library
        System.loadLibrary("drmframework_jni");

        // M: check the system property tho determine that if it's Forward Lock Only
        String drmFLOnly = System.getProperty("drm.forwardlock.only", "no");
        OMA_DRM_FL_ONLY =
            drmFLOnly.equals("true") || drmFLOnly.equals("yes") || drmFLOnly.equals("1");
    }

    /**
     * Interface definition for a callback that receives status messages and warnings
     * during registration and rights acquisition.
     */
    public interface OnInfoListener {
        /**
         * Called when the DRM framework sends status or warning information during registration
         * and rights acquisition.
         *
         * @param client The <code>DrmManagerClient</code> instance.
         * @param event The {@link DrmInfoEvent} instance that wraps the status information or 
         * warnings.
         */
        public void onInfo(DrmManagerClient client, DrmInfoEvent event);
    }

    /**
     * Interface definition for a callback that receives information
     * about DRM processing events.
     */
    public interface OnEventListener {
        /**
         * Called when the DRM framework sends information about a DRM processing request.
         *
         * @param client The <code>DrmManagerClient</code> instance.
         * @param event The {@link DrmEvent} instance that wraps the information being
         * conveyed, such as the information type and message.
         */
        public void onEvent(DrmManagerClient client, DrmEvent event);
    }

    /**
     * Interface definition for a callback that receives information about DRM framework errors.
     */
    public interface OnErrorListener {
        /**
         * Called when the DRM framework sends error information.
         *
         * @param client The <code>DrmManagerClient</code> instance.
         * @param event The {@link DrmErrorEvent} instance that wraps the error type and message.
         */
        public void onError(DrmManagerClient client, DrmErrorEvent event);
    }

    private static final int ACTION_REMOVE_ALL_RIGHTS = 1001;
    private static final int ACTION_PROCESS_DRM_INFO = 1002;

    private int mUniqueId;
    private int mNativeContext;
    private Context mContext;
    private InfoHandler mInfoHandler;
    private EventHandler mEventHandler;
    private OnInfoListener mOnInfoListener;
    private OnEventListener mOnEventListener;
    private OnErrorListener mOnErrorListener;

    // M: the dialog array list to deal with dialog UI operation
    private static ArrayList<CustomAlertDialog> sSecureTimerDialogQueue =
        new ArrayList<CustomAlertDialog>();
    private static ArrayList<CustomAlertDialog> sConsumeDialogQueue =
        new ArrayList<CustomAlertDialog>();
    private static ArrayList<CustomAlertDialog> sProtectionInfoDialogQueue =
        new ArrayList<CustomAlertDialog>();
    private static ArrayList<CustomAlertDialog> sLicenseDialogQueue =
        new ArrayList<CustomAlertDialog>();

    private class EventHandler extends Handler {

        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            DrmEvent event = null;
            DrmErrorEvent error = null;
            HashMap<String, Object> attributes = new HashMap<String, Object>();

            switch(msg.what) {
            case ACTION_PROCESS_DRM_INFO: {
                final DrmInfo drmInfo = (DrmInfo) msg.obj;
                DrmInfoStatus status = _processDrmInfo(mUniqueId, drmInfo);

                attributes.put(DrmEvent.DRM_INFO_STATUS_OBJECT, status);
                attributes.put(DrmEvent.DRM_INFO_OBJECT, drmInfo);

                if (null != status && DrmInfoStatus.STATUS_OK == status.statusCode) {
                    event = new DrmEvent(mUniqueId,
                            getEventType(status.infoType), null, attributes);
                } else {
                    int infoType = (null != status) ? status.infoType : drmInfo.getInfoType();
                    error = new DrmErrorEvent(mUniqueId,
                            getErrorType(infoType), null, attributes);
                }
                break;
            }
            case ACTION_REMOVE_ALL_RIGHTS: {
                if (ERROR_NONE == _removeAllRights(mUniqueId)) {
                    event = new DrmEvent(mUniqueId, DrmEvent.TYPE_ALL_RIGHTS_REMOVED, null);
                } else {
                    error = new DrmErrorEvent(mUniqueId,
                            DrmErrorEvent.TYPE_REMOVE_ALL_RIGHTS_FAILED, null);
                }
                break;
            }
            default:
                Log.e(TAG, "Unknown message type " + msg.what);
                return;
            }
            if (null != mOnEventListener && null != event) {
                mOnEventListener.onEvent(DrmManagerClient.this, event);
            }
            if (null != mOnErrorListener && null != error) {
                mOnErrorListener.onError(DrmManagerClient.this, error);
            }
        }
    }

    /**
     * {@hide}
     */
    public static void notify(
            Object thisReference, int uniqueId, int infoType, String message) {
        DrmManagerClient instance = (DrmManagerClient)((WeakReference)thisReference).get();

        if (null != instance && null != instance.mInfoHandler) {
            Message m = instance.mInfoHandler.obtainMessage(
                InfoHandler.INFO_EVENT_TYPE, uniqueId, infoType, message);
            instance.mInfoHandler.sendMessage(m);
        }
    }

    private class InfoHandler extends Handler {
        public static final int INFO_EVENT_TYPE = 1;

        public InfoHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            DrmInfoEvent info = null;
            DrmErrorEvent error = null;

            switch (msg.what) {
            case InfoHandler.INFO_EVENT_TYPE:
                int uniqueId = msg.arg1;
                int infoType = msg.arg2;
                String message = msg.obj.toString();

                switch (infoType) {
                case DrmInfoEvent.TYPE_REMOVE_RIGHTS: {
                    try {
                        DrmUtils.removeFile(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    info = new DrmInfoEvent(uniqueId, infoType, message);
                    break;
                }
                case DrmInfoEvent.TYPE_ALREADY_REGISTERED_BY_ANOTHER_ACCOUNT:
                case DrmInfoEvent.TYPE_RIGHTS_INSTALLED:
                case DrmInfoEvent.TYPE_WAIT_FOR_RIGHTS:
                case DrmInfoEvent.TYPE_ACCOUNT_ALREADY_REGISTERED:
                case DrmInfoEvent.TYPE_RIGHTS_REMOVED: {
                    info = new DrmInfoEvent(uniqueId, infoType, message);
                    break;
                }
                default:
                    error = new DrmErrorEvent(uniqueId, infoType, message);
                    break;
                }

                if (null != mOnInfoListener && null != info) {
                    mOnInfoListener.onInfo(DrmManagerClient.this, info);
                }
                if (null != mOnErrorListener && null != error) {
                    mOnErrorListener.onError(DrmManagerClient.this, error);
                }
                return;
            default:
                Log.e(TAG, "Unknown message type " + msg.what);
                return;
            }
        }
    }

    /**
     * Creates a <code>DrmManagerClient</code>.
     *
     * @param context Context of the caller.
     */
    public DrmManagerClient(Context context) {
        mContext = context;

        HandlerThread infoThread = new HandlerThread("DrmManagerClient.InfoHandler");
        infoThread.start();
        mInfoHandler = new InfoHandler(infoThread.getLooper());

        HandlerThread eventThread = new HandlerThread("DrmManagerClient.EventHandler");
        eventThread.start();
        mEventHandler = new EventHandler(eventThread.getLooper());

        // save the unique id
        mUniqueId = _initialize(new WeakReference<DrmManagerClient>(this));
        Xlog.d(TAG, "DrmManagerClient(), create instance, mUniqueId=" + mUniqueId
                  + ", pid=" + android.os.Process.myPid()
                  + ", active_thread_count="
                  + Thread.currentThread().getThreadGroup().activeCount());
    }

    protected void finalize() {
        Xlog.d(TAG, "finalize(), release DrmManagerClient instance. mUniqueId=" + mUniqueId);
        mInfoHandler.getLooper().quit();
        mEventHandler.getLooper().quit();
        _finalize(mUniqueId);
    }

    /**
     * Registers an {@link DrmManagerClient.OnInfoListener} callback, which is invoked when the 
     * DRM framework sends status or warning information during registration or rights acquisition.
     *
     * @param infoListener Interface definition for the callback.
     */
    public synchronized void setOnInfoListener(OnInfoListener infoListener) {
        if (null != infoListener) {
            mOnInfoListener = infoListener;
        }
    }

    /**
     * Registers an {@link DrmManagerClient.OnEventListener} callback, which is invoked when the 
     * DRM framework sends information about DRM processing.
     *
     * @param eventListener Interface definition for the callback.
     */
    public synchronized void setOnEventListener(OnEventListener eventListener) {
        if (null != eventListener) {
            mOnEventListener = eventListener;
        }
    }

    /**
     * Registers an {@link DrmManagerClient.OnErrorListener} callback, which is invoked when 
     * the DRM framework sends error information.
     *
     * @param errorListener Interface definition for the callback.
     */
    public synchronized void setOnErrorListener(OnErrorListener errorListener) {
        if (null != errorListener) {
            mOnErrorListener = errorListener;
        }
    }

    /**
     * Retrieves information about all the DRM plug-ins (agents) that are registered with
     * the DRM framework.
     *
     * @return A <code>String</code> array of DRM plug-in descriptions.
     */
    public String[] getAvailableDrmEngines() {
        DrmSupportInfo[] supportInfos = _getAllSupportInfo(mUniqueId);
        ArrayList<String> descriptions = new ArrayList<String>();

        for (int i = 0; i < supportInfos.length; i++) {
            descriptions.add(supportInfos[i].getDescriprition());
        }

        String[] drmEngines = new String[descriptions.size()];
        return descriptions.toArray(drmEngines);
    }

    /**
     * Retrieves constraint information for rights-protected content.
     *
     * @param path Path to the content from which you are retrieving DRM constraints.
     * @param action Action defined in {@link DrmStore.Action}.
     *
     * @return A {@link android.content.ContentValues} instance that contains
     * key-value pairs representing the constraints. Null in case of failure.
     */
    public ContentValues getConstraints(String path, int action) {
        if (null == path || path.equals("") || !DrmStore.Action.isValid(action)) {
            throw new IllegalArgumentException("Given usage or path is invalid/null");
        }
        return _getConstraints(mUniqueId, path, action);
    }

   /**
    * Retrieves metadata information for rights-protected content.
    *
    * @param path Path to the content from which you are retrieving metadata information.
    *
    * @return A {@link android.content.ContentValues} instance that contains
    * key-value pairs representing the metadata. Null in case of failure.
    */
    public ContentValues getMetadata(String path) {
        if (null == path || path.equals("")) {
            throw new IllegalArgumentException("Given path is invalid/null");
        }
        return _getMetadata(mUniqueId, path);
    }

    /**
     * Retrieves constraint information for rights-protected content.
     *
     * @param uri URI for the content from which you are retrieving DRM constraints.
     * @param action Action defined in {@link DrmStore.Action}.
     *
     * @return A {@link android.content.ContentValues} instance that contains
     * key-value pairs representing the constraints. Null in case of failure.
     */
    public ContentValues getConstraints(Uri uri, int action) {
        if (null == uri || Uri.EMPTY == uri) {
            throw new IllegalArgumentException("Uri should be non null");
        }
        return getConstraints(convertUriToPath(uri), action);
    }

   /**
    * Retrieves metadata information for rights-protected content.
    *
    * @param uri URI for the content from which you are retrieving metadata information.
    *
    * @return A {@link android.content.ContentValues} instance that contains
    * key-value pairs representing the constraints. Null in case of failure.
    */
    public ContentValues getMetadata(Uri uri) {
        if (null == uri || Uri.EMPTY == uri) {
            throw new IllegalArgumentException("Uri should be non null");
        }
        return getMetadata(convertUriToPath(uri));
    }

    /**
     * Saves rights to a specified path and associates that path with the content path.
     * 
     * <p class="note"><strong>Note:</strong> For OMA or WM-DRM, <code>rightsPath</code> and
     * <code>contentPath</code> can be null.</p>
     *
     * @param drmRights The {@link DrmRights} to be saved.
     * @param rightsPath File path where rights will be saved.
     * @param contentPath File path where content is saved.
     *
     * @return ERROR_NONE for success; ERROR_UNKNOWN for failure.
     *
     * @throws IOException If the call failed to save rights information at the given
     * <code>rightsPath</code>.
     */
    public int saveRights(
            DrmRights drmRights, String rightsPath, String contentPath) throws IOException {
        if (null == drmRights || !drmRights.isValid()) {
            throw new IllegalArgumentException("Given drmRights or contentPath is not valid");
        }
        if (null != rightsPath && !rightsPath.equals("")) {
            DrmUtils.writeToFile(rightsPath, drmRights.getData());
        }
        return _saveRights(mUniqueId, drmRights, rightsPath, contentPath);
    }

    /**
     * Installs a new DRM plug-in (agent) at runtime.
     *
     * @param engineFilePath File path to the plug-in file to be installed.
     *
     * {@hide}
     */
    public void installDrmEngine(String engineFilePath) {
        if (null == engineFilePath || engineFilePath.equals("")) {
            throw new IllegalArgumentException(
                "Given engineFilePath: "+ engineFilePath + "is not valid");
        }
        _installDrmEngine(mUniqueId, engineFilePath);
    }

    /**
     * Checks whether the given MIME type or path can be handled.
     *
     * @param path Path of the content to be handled.
     * @param mimeType MIME type of the object to be handled.
     *
     * @return True if the given MIME type or path can be handled; false if they cannot be handled.
     */
    public boolean canHandle(String path, String mimeType) {
        if ((null == path || path.equals("")) && (null == mimeType || mimeType.equals(""))) {
            throw new IllegalArgumentException("Path or the mimetype should be non null");
        }
        return _canHandle(mUniqueId, path, mimeType);
    }

    /**
     * Checks whether the given MIME type or URI can be handled.
     *
     * @param uri URI for the content to be handled.
     * @param mimeType MIME type of the object to be handled
     *
     * @return True if the given MIME type or URI can be handled; false if they cannot be handled.
     */
    public boolean canHandle(Uri uri, String mimeType) {
        if ((null == uri || Uri.EMPTY == uri) && (null == mimeType || mimeType.equals(""))) {
            throw new IllegalArgumentException("Uri or the mimetype should be non null");
        }
        return canHandle(convertUriToPath(uri), mimeType);
    }

    /**
     * Processes the given DRM information based on the information type.
     *
     * @param drmInfo The {@link DrmInfo} to be processed.
     * @return ERROR_NONE for success; ERROR_UNKNOWN for failure.
     */
    public int processDrmInfo(DrmInfo drmInfo) {
        if (null == drmInfo || !drmInfo.isValid()) {
            throw new IllegalArgumentException("Given drmInfo is invalid/null");
        }
        int result = ERROR_UNKNOWN;
        if (null != mEventHandler) {
            Message msg = mEventHandler.obtainMessage(ACTION_PROCESS_DRM_INFO, drmInfo);
            result = (mEventHandler.sendMessage(msg)) ? ERROR_NONE : result;
        }
        return result;
    }

    /**
     * Retrieves information for registering, unregistering, or acquiring rights.
     *
     * @param drmInfoRequest The {@link DrmInfoRequest} that specifies the type of DRM
     * information being retrieved.
     *
     * @return A {@link DrmInfo} instance.
     */
    public DrmInfo acquireDrmInfo(DrmInfoRequest drmInfoRequest) {
        if (null == drmInfoRequest || !drmInfoRequest.isValid()) {
            throw new IllegalArgumentException("Given drmInfoRequest is invalid/null");
        }
        return _acquireDrmInfo(mUniqueId, drmInfoRequest);
    }

    /**
     * Processes a given {@link DrmInfoRequest} and returns the rights information asynchronously.
     *<p>
     * This is a utility method that consists of an
     * {@link #acquireDrmInfo(DrmInfoRequest) acquireDrmInfo()} and a
     * {@link #processDrmInfo(DrmInfo) processDrmInfo()} method call. This utility method can be 
     * used only if the selected DRM plug-in (agent) supports this sequence of calls. Some DRM
     * agents, such as OMA, do not support this utility method, in which case an application must
     * invoke {@link #acquireDrmInfo(DrmInfoRequest) acquireDrmInfo()} and
     * {@link #processDrmInfo(DrmInfo) processDrmInfo()} separately.
     *
     * @param drmInfoRequest The {@link DrmInfoRequest} used to acquire the rights.
     * @return ERROR_NONE for success; ERROR_UNKNOWN for failure.
     */
    public int acquireRights(DrmInfoRequest drmInfoRequest) {
        DrmInfo drmInfo = acquireDrmInfo(drmInfoRequest);
        if (null == drmInfo) {
            return ERROR_UNKNOWN;
        }
        return processDrmInfo(drmInfo);
    }

    /**
     * Retrieves the type of rights-protected object (for example, content object, rights
     * object, and so on) using the specified path or MIME type. At least one parameter must
     * be specified to retrieve the DRM object type.
     *
     * @param path Path to the content or null.
     * @param mimeType MIME type of the content or null.
     * 
     * @return An <code>int</code> that corresponds to a {@link DrmStore.DrmObjectType}.
     */
    public int getDrmObjectType(String path, String mimeType) {
        if ((null == path || path.equals("")) && (null == mimeType || mimeType.equals(""))) {
            throw new IllegalArgumentException("Path or the mimetype should be non null");
        }
        return _getDrmObjectType(mUniqueId, path, mimeType);
    }

    /**
     * Retrieves the type of rights-protected object (for example, content object, rights
     * object, and so on) using the specified URI or MIME type. At least one parameter must
     * be specified to retrieve the DRM object type.
     *
     * @param uri URI for the content or null.
     * @param mimeType MIME type of the content or null.
     * 
     * @return An <code>int</code> that corresponds to a {@link DrmStore.DrmObjectType}.
     */
    public int getDrmObjectType(Uri uri, String mimeType) {
        if ((null == uri || Uri.EMPTY == uri) && (null == mimeType || mimeType.equals(""))) {
            throw new IllegalArgumentException("Uri or the mimetype should be non null");
        }
        String path = "";
        try {
            path = convertUriToPath(uri);
        } catch (Exception e) {
            // Even uri is invalid the mimetype shall be valid, so allow to proceed further.
            Log.w(TAG, "Given Uri could not be found in media store");
        }
        return getDrmObjectType(path, mimeType);
    }

    /**
     * Retrieves the MIME type embedded in the original content.
     *
     * @param path Path to the rights-protected content.
     *
     * @return The MIME type of the original content, such as <code>video/mpeg</code>.
     */
    public String getOriginalMimeType(String path) {
        if (null == path || path.equals("")) {
            throw new IllegalArgumentException("Given path should be non null");
        }
        return _getOriginalMimeType(mUniqueId, path);
    }

    /**
     * Retrieves the MIME type embedded in the original content.
     *
     * @param uri URI of the rights-protected content.
     *
     * @return MIME type of the original content, such as <code>video/mpeg</code>.
     */
    public String getOriginalMimeType(Uri uri) {
        if (null == uri || Uri.EMPTY == uri) {
            throw new IllegalArgumentException("Given uri is not valid");
        }
        return getOriginalMimeType(convertUriToPath(uri));
    }

    /**
     * Checks whether the given content has valid rights.
     *
     * @param path Path to the rights-protected content.
     *
     * @return An <code>int</code> representing the {@link DrmStore.RightsStatus} of the content.
     */
    // OMA DRM v1: without specifying action type, this is deprecated
    public int checkRightsStatus(String path) {
        return checkRightsStatus(path, DrmStore.Action.DEFAULT);
    }

    /**
     * Check whether the given content has valid rights.
     *
     * @param uri URI of the rights-protected content.
     *
     * @return An <code>int</code> representing the {@link DrmStore.RightsStatus} of the content.
     */
    // OMA DRM v1: without specifying action type, this is deprecated
    public int checkRightsStatus(Uri uri) {
        if (null == uri || Uri.EMPTY == uri) {
            throw new IllegalArgumentException("Given uri is not valid");
        }
        return checkRightsStatus(convertUriToPath(uri));
    }

    /**
     * Checks whether the given rights-protected content has valid rights for the specified
     * {@link DrmStore.Action}.
     *
     * @param path Path to the rights-protected content.
     * @param action The {@link DrmStore.Action} to perform.
     *
     * @return An <code>int</code> representing the {@link DrmStore.RightsStatus} of the content.
     */
    public int checkRightsStatus(String path, int action) {
        if (null == path || path.equals("") || !DrmStore.Action.isValid(action)) {
            throw new IllegalArgumentException("Given path or action is not valid");
        }
        int result = _checkRightsStatus(mUniqueId, path, action);
        if (result == DrmStore.RightsStatus.SECURE_TIMER_INVALID) {
            Xlog.d(TAG, "checkRightsStatus() : secure timer indicates invalid state");
            result = DrmStore.RightsStatus.RIGHTS_INVALID;
        }
        return result;
    }

    /**
     * Checks whether the given rights-protected content has valid rights for the specified
     * {@link DrmStore.Action}.
     *
     * @param uri URI for the rights-protected content.
     * @param action The {@link DrmStore.Action} to perform.
     *
     * @return An <code>int</code> representing the {@link DrmStore.RightsStatus} of the content.
     */
    public int checkRightsStatus(Uri uri, int action) {
        if (null == uri || Uri.EMPTY == uri) {
            throw new IllegalArgumentException("Given uri is not valid");
        }
        return checkRightsStatus(convertUriToPath(uri), action);
    }

    /**
     * Removes the rights associated with the given rights-protected content.
     *
     * @param path Path to the rights-protected content.
     *
     * @return ERROR_NONE for success; ERROR_UNKNOWN for failure.
     */
    public int removeRights(String path) {
        if (null == path || path.equals("")) {
            throw new IllegalArgumentException("Given path should be non null");
        }
        return _removeRights(mUniqueId, path);
    }

    /**
     * Removes the rights associated with the given rights-protected content.
     *
     * @param uri URI for the rights-protected content.
     *
     * @return ERROR_NONE for success; ERROR_UNKNOWN for failure.
     */
    public int removeRights(Uri uri) {
        if (null == uri || Uri.EMPTY == uri) {
            throw new IllegalArgumentException("Given uri is not valid");
        }
        return removeRights(convertUriToPath(uri));
    }

    /**
     * Removes all the rights information of every DRM plug-in (agent) associated with
     * the DRM framework. Will be used during a master reset.
     *
     * @return ERROR_NONE for success; ERROR_UNKNOWN for failure.
     */
    public int removeAllRights() {
        int result = ERROR_UNKNOWN;
        if (null != mEventHandler) {
            Message msg = mEventHandler.obtainMessage(ACTION_REMOVE_ALL_RIGHTS);
            result = (mEventHandler.sendMessage(msg)) ? ERROR_NONE : result;
        }
        return result;
    }

    /**
     * Initiates a new conversion session. An application must initiate a conversion session
     * with this method each time it downloads a rights-protected file that needs to be converted.
     *<p>
     * This method applies only to forward-locking (copy protection) DRM schemes.
     *
     * @param mimeType MIME type of the input data packet.
     *
     * @return A convert ID that is used used to maintain the conversion session.
     */
    public int openConvertSession(String mimeType) {
        if (null == mimeType || mimeType.equals("")) {
            throw new IllegalArgumentException("Path or the mimeType should be non null");
        }
        return _openConvertSession(mUniqueId, mimeType);
    }

    /**
     * Converts the input data (content) that is part of a rights-protected file. The converted
     * data and status is returned in a {@link DrmConvertedStatus} object. This method should be
     * called each time there is a new block of data received by the application.
     *
     * @param convertId Handle for the conversion session.
     * @param inputData Input data that needs to be converted.
     *
     * @return A {@link DrmConvertedStatus} object that contains the status of the data conversion,
     * the converted data, and offset for the header and body signature. An application can 
     * ignore the offset because it is only relevant to the
     * {@link #closeConvertSession closeConvertSession()} method.
     */
    public DrmConvertedStatus convertData(int convertId, byte[] inputData) {
        if (null == inputData || 0 >= inputData.length) {
            throw new IllegalArgumentException("Given inputData should be non null");
        }
        return _convertData(mUniqueId, convertId, inputData);
    }

    /**
     * Informs the DRM plug-in (agent) that there is no more data to convert or that an error 
     * has occurred. Upon successful conversion of the data, the DRM agent will provide an offset
     * value indicating where the header and body signature should be added. Appending the 
     * signature is necessary to protect the integrity of the converted file.
     *
     * @param convertId Handle for the conversion session.
     *
     * @return A {@link DrmConvertedStatus} object that contains the status of the data conversion,
     * the converted data, and the offset for the header and body signature.
     */
    public DrmConvertedStatus closeConvertSession(int convertId) {
        return _closeConvertSession(mUniqueId, convertId);
    }

    private int getEventType(int infoType) {
        int eventType = -1;

        switch (infoType) {
        case DrmInfoRequest.TYPE_REGISTRATION_INFO:
        case DrmInfoRequest.TYPE_UNREGISTRATION_INFO:
        case DrmInfoRequest.TYPE_RIGHTS_ACQUISITION_INFO:
            eventType = DrmEvent.TYPE_DRM_INFO_PROCESSED;
            break;
        }
        return eventType;
    }

    private int getErrorType(int infoType) {
        int error = -1;

        switch (infoType) {
        case DrmInfoRequest.TYPE_REGISTRATION_INFO:
        case DrmInfoRequest.TYPE_UNREGISTRATION_INFO:
        case DrmInfoRequest.TYPE_RIGHTS_ACQUISITION_INFO:
            error = DrmErrorEvent.TYPE_PROCESS_DRM_INFO_FAILED;
            break;
        }
        return error;
    }

    /**
     * This method expects uri in the following format
     *     content://media/<table_name>/<row_index> (or)
     *     file://sdcard/test.mp4
     *     http://test.com/test.mp4
     *
     * Here <table_name> shall be "video" or "audio" or "images"
     * <row_index> the index of the content in given table
     */
    private String convertUriToPath(Uri uri) {
        String path = null;
        if (null != uri) {
            String scheme = uri.getScheme();
            if (null == scheme || scheme.equals("") ||
                    scheme.equals(ContentResolver.SCHEME_FILE)) {
                path = uri.getPath();

            } else if (scheme.equals("http")) {
                path = uri.toString();

            } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                String[] projection = new String[] {MediaStore.MediaColumns.DATA};
                Cursor cursor = null;
                try {
                    cursor = mContext.getContentResolver().query(uri, projection, null,
                            null, null);
                    if (null == cursor || 0 == cursor.getCount() || !cursor.moveToFirst()) {
                        throw new IllegalArgumentException("Given Uri could not be found" +
                                " in media store");
                    }
                    int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    path = cursor.getString(pathIndex);
                } catch (SQLiteException e) {
                    throw new IllegalArgumentException("Given Uri is not formatted in a way " +
                            "so that it can be found in media store.");
                } finally {
                    if (null != cursor) {
                        cursor.close();
                    }
                }
            } else {
                throw new IllegalArgumentException("Given Uri scheme is not supported");
            }
        }
        return path;
    }

    // private native interfaces
    private native int _initialize(Object weak_this);

    private native void _finalize(int uniqueId);

    private native void _installDrmEngine(int uniqueId, String engineFilepath);

    private native ContentValues _getConstraints(int uniqueId, String path, int usage);

    private native ContentValues _getMetadata(int uniqueId, String path);

    private native boolean _canHandle(int uniqueId, String path, String mimeType);

    private native DrmInfoStatus _processDrmInfo(int uniqueId, DrmInfo drmInfo);

    private native DrmInfo _acquireDrmInfo(int uniqueId, DrmInfoRequest drmInfoRequest);

    private native int _saveRights(
            int uniqueId, DrmRights drmRights, String rightsPath, String contentPath);

    private native int _getDrmObjectType(int uniqueId, String path, String mimeType);

    private native String _getOriginalMimeType(int uniqueId, String path);

    private native int _checkRightsStatus(int uniqueId, String path, int action);

    private native int _removeRights(int uniqueId, String path);

    private native int _removeAllRights(int uniqueId);

    private native int _openConvertSession(int uniqueId, String mimeType);

    private native DrmConvertedStatus _convertData(
            int uniqueId, int convertId, byte[] inputData);

    private native DrmConvertedStatus _closeConvertSession(int uniqueId, int convertId);

    private native DrmSupportInfo[] _getAllSupportInfo(int uniqueId);

    private native String _saveRO(int uniqueId, DrmRights drmRights, String roFile);

    private native String _getContentIdFromRights(int uniqueId, DrmRights drmRights);

    private native int _installDrmMsg(int uniqueId, String path);

    private native int _consume(int uniqueId, String path, int action);

    private native static int _saveIMEI(String imei);

    private native static int _saveDeviceId(String id);

    private native static String _getDeviceId();

    private native static boolean _checkSecureTimerStatus();

    private native static int _updateClockBase();

    private native static int _updateClockOffset();

    private native static int _updateClock(int value);

    private native static int _saveClock();

    private native static int _loadClock();

    private native static int _getSecureTime();

    /**
     * Listener for Drm info dialog.
     * @hide
     */
    public interface DrmOperationListener {
        /**
         * User's operation will continue the process.
         * App should continue the normal logic when receiving this type.
         */
        public static final int CONTINUE = 1;
        /**
         * User's operation will stop the process.
         * App should stop the normal logic when receiving this type.
         */
        public static final int STOP = 2;
        /**
         * Will be called after user do some operation.
         * @param type possible values will be {@link #CONTINUE}, {@link #STOP}.
         */
        public void onOperated(int type);
    }

    /*
     * The custom alert dialog which DRM dialogs would ues.
     */
    private static class CustomAlertDialog extends AlertDialog {

        private DialogInterface.OnDismissListener mDismissListener = null;
        private DialogInterface.OnShowListener mShowListener = null;
        private ArrayList<CustomAlertDialog> mQueue = null;
        private Context mContext = null;

        public CustomAlertDialog(Context context, ArrayList<CustomAlertDialog> queue) {
            super(context);
            mQueue = queue;
            mContext = context;

            super.setOnDismissListener(
                new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        if (null != mDismissListener) {
                            Xlog.d(TAG, "CustomerAlertDialog: execute the original dismiss listener");
                            mDismissListener.onDismiss(dialog);
                        }
                        // remove this from array
                        if (null != mQueue) {
                            synchronized(mQueue) {
                                Xlog.d(TAG, "CustomerAlertDialog: remove this dialog from queue");
                                mQueue.remove(CustomAlertDialog.this);
                            }
                        }
                        mQueue = null;
                    }
                }
            );
            super.setOnShowListener(
                new DialogInterface.OnShowListener() {
                    public void onShow(DialogInterface dialog) {
                        if (null != mShowListener) {
                            Xlog.d(TAG, "CustomerAlertDialog: execute the original show listener");
                            mShowListener.onShow(dialog);
                        }
                        // remove this from array
                        if (null != mQueue) {
                            synchronized(mQueue) {
                                Xlog.d(TAG, "CustomerAlertDialog: add this dialog to queue");
                                mQueue.add(CustomAlertDialog.this);
                            }
                        }
                    }
                }
            );
        }

        @Override
        public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
            // just upadate the outer listener
            mDismissListener = listener;
        }

        @Override
        public void setOnShowListener(DialogInterface.OnShowListener listener) {
            mShowListener = listener;
        }

        public final Context getCreatorContext() {
            return mContext;
        }
    };

    private static Dialog validateCustomAlertDialog(ArrayList<CustomAlertDialog> list, Context context) {
        // go through all the stored dialogs and check
        Xlog.d(TAG, "validateCustomAlertDialog(): validate within context: " + context);
        Dialog result = null;
        synchronized(list) {
            Iterator<CustomAlertDialog> iter = list.iterator();
            while (iter.hasNext()) {
                CustomAlertDialog dialog = iter.next();
                Xlog.d(TAG, "validateCustomAlertDialog(): stored dialog with creator context: " + dialog.getCreatorContext());
                if (dialog.getCreatorContext().equals(context)) {
                    Xlog.d(TAG, "validateCustomAlertDialog(): context match");
                    result = dialog;
                    break;
                }
            }
        }
        return result;
    }

    private static void checkCustomAlertDialog(ArrayList<CustomAlertDialog> list, Context context) {
        Xlog.d(TAG, "checkCustomAlertDialog(): check within context: " + context);
        synchronized(list) {
            Iterator<CustomAlertDialog> iter = list.iterator();
            while (iter.hasNext()) {
                CustomAlertDialog dialog = iter.next();
                Xlog.d(TAG, "checkCustomAlertDialog(): stored dialog with creator context: " + dialog.getCreatorContext());
                if (dialog.getCreatorContext().equals(context)) {
                    Xlog.d(TAG, "checkCustomAlertDialog(): context match, dismiss it");
                    dialog.dismiss();
                    break;
                }
            }
        }
    }

    /**
     * get the content id (cid) of the media which the rights binds to
     *
     * @param drmRights the DrmRights object
     * @return ContentUri(cid) for succeed, NULL for fail.
     * @throws IOException if failed to get cid information from rights data
     * @hide
     */
    public String getContentIdFromRights(DrmRights drmRights) throws IOException {
        return _getContentIdFromRights(mUniqueId, drmRights);
    }

    /**
     * Installing the drm message file (.dm).
     *
     * @param uri Uri of the downloaded protected content (FL, CD, FLSD)
     * @return ERROR_NONE for success ERROR_UNKNOWN for failure
     * @hide
     */
    public int installDrmMsg(Uri uri) {
        if (null == uri || Uri.EMPTY == uri) {
            Xlog.e(TAG, "installDrmMsg() : Given uri is not valid");
            return ERROR_UNKNOWN;
        }

        String path = null;
        try {
            path = convertUriToPath(uri);
        } catch (IllegalArgumentException e) {
            Xlog.e(TAG, "installDrmMsg() : " + e.getMessage());
            return ERROR_UNKNOWN;
        }
        return installDrmMsg(path);
    }

    /**
     * Installing the drm message file (.dm).
     *
     * @param path Path of the downloaded protected content (FL, CD, FLSD)
     * @return ERROR_NONE for success ERROR_UNKNOWN for failure
     * @hide
     */
    public int installDrmMsg(String path) {
        if (null == path || path.equals("")) {
            Xlog.e(TAG, "installDrmMsg() : Given path should be non null");
            return ERROR_UNKNOWN;
        }
        return _installDrmMsg(mUniqueId, path);
    }

    /**
     * Consume the rights associated with the given protected content
     *
     * @param uri The content URI of the protected content
     * @return ERROR_NONE for success ERROR_UNKNOWN for failure
     * @hide
     */
    public int consume(Uri uri, int action) {
        if (null == uri || Uri.EMPTY == uri || !DrmStore.Action.isValid(action)) {
            Xlog.e(TAG, "consume() : Given uri or action is not valid");
            return ERROR_UNKNOWN;
        }

        String path = null;
        try {
            path = convertUriToPath(uri);
        } catch (IllegalArgumentException e) {
            Xlog.e(TAG, "consume() : " + e.getMessage());
            return ERROR_UNKNOWN;
        }
        return consume(path, action);
    }

   /**
     * Consume the rights associated with the given protected content
     *
     * @param path Path of the protected content
     * @return ERROR_NONE for success ERROR_UNKNOWN for failure
     * @hide
     */
    public int consume(String path, int action) {
        if (null == path || path.equals("") || !DrmStore.Action.isValid(action)) {
            Xlog.e(TAG, "consume() : Given path should be non null or action is not valid");
            return ERROR_UNKNOWN;
        }
        return _consume(mUniqueId, path, action);
    }

    /**
     * @hide
     */
    public static int saveIMEI(String imei) {
        if (null == imei || imei.equals("")) {
            Xlog.e(TAG, "saveIMEI() : Given IMEI number should be non null and valid");
            return -1;
        }
        Xlog.d(TAG, "saveIMEI() : IMEI number of this device: "+imei);
        return _saveIMEI(imei);
    }

    /**
     * @hide
     */
    public static int saveDeviceId(String id) {
        if (null == id || id.isEmpty()) {
            Xlog.e(TAG, "saveDeviceId() : Given device id invalid");
            return -1;
        }
        return _saveDeviceId(id);
    }

    /**
     * @hide
     */
    public static String getDeviceId() {
        return _getDeviceId();
    }

    /**
     * Check whether has valid secure timer or not.
     *
     * @return Status of the secure timer
     * @hide
     */
    public static boolean checkSecureTimerStatus() {
        return _checkSecureTimerStatus();
    }

    /**
     * @hide
     */
    public static int updateClockBase() {
        return _updateClockBase();
    }

    /**
     * @hide
     */
    public static int updateClockOffset() {
        return _updateClockOffset();
    }

    /**
     * @hide
     */
    public static int updateClock(int value) {
        return _updateClock(value);
    }

    /**
     * @hide
     */
    public static int saveClock() {
        return _saveClock();
    }

    /**
     * @hide
     */
    public static int loadClock() {
        return _loadClock();
    }

    /**
     * @hide
     * returns 0 (1970-1-1 00:00) if the secure timer is in invalid state.
     * if secure timer valid, returns the second count since 1970-1-1 00:00
     */
    public static int getSecureTime() {
        loadClock();
        return _getSecureTime();
    }

    /**
     * Informs the user that the operation cannot be performed because the secure timer is in
     * invalid state.
     *
     * @param context Context
     * @param clickListener OnClickListener, listener should listen for DialogInterface.BUTTON_POSITIVE
     * @param dismissListener OnDismissListener, listener should listen for dimiss
     * @return Dialog The dialog being shown.
     * @hide
     */
    public static Dialog showSecureTimerInvalidDialog(Context context,
            DialogInterface.OnClickListener clickListener,
            DialogInterface.OnDismissListener dismissListener) {
        Xlog.d(TAG, "showSecureTimerInvalidDialog() within context: " + context);

        Dialog result = validateCustomAlertDialog(sSecureTimerDialogQueue, context);
        if (null != result) {
            Xlog.d(TAG, "showSecureTimerInvalidDialog(): use the existing one");
            return result;
        }

        CustomAlertDialog dialog = new CustomAlertDialog(context, sSecureTimerDialogQueue);
        Resources res = context.getResources();
        dialog.setButton(Dialog.BUTTON_POSITIVE,
            res.getString(android.R.string.ok),
            clickListener);
        dialog.setIcon(android.R.drawable.ic_dialog_info);
        dialog.setTitle(com.mediatek.internal.R.string.drm_secure_timer_title);
        dialog.setMessage(res.getString(com.mediatek.internal.R.string.drm_secure_timer_message));
        dialog.setOnDismissListener(dismissListener);
        dialog.show();

        return dialog;
    }

    /**
     * M: special for VideoPlayer
     * @hide
     */
    public static Dialog showSecureTimerInvalid(
            Context context, final DrmOperationListener listener) {
        Xlog.d(TAG, "showSecureTimerInvalid() within context: " + context);

        Dialog result = validateCustomAlertDialog(sSecureTimerDialogQueue, context);
        if (null != result) {
            Xlog.d(TAG, "showSecureTimerInvalid(): use the existing one");
            return result;
        }

        CustomAlertDialog dialog = new CustomAlertDialog(context, sSecureTimerDialogQueue);
        Resources res = context.getResources();
        // OK to do nothing
        dialog.setButton(Dialog.BUTTON_POSITIVE,
            res.getString(android.R.string.ok),
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // nothing
                }
            }
        );
        dialog.setIcon(android.R.drawable.ic_dialog_info);
        dialog.setTitle(com.mediatek.internal.R.string.drm_secure_timer_title);
        dialog.setMessage(res.getString(com.mediatek.internal.R.string.drm_secure_timer_message));
        // when dismiss, cause the drm operation listener to STOP
        dialog.setOnDismissListener(
            new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    if (listener != null) {
                        Xlog.d(TAG, "showSecureTimerInvalid(): drm operation listener to operate STOP.");
                        listener.onOperated(DrmOperationListener.STOP);
                    }
                }
            }
        );
        dialog.show();

        return dialog;
    }

    /**
     * Informs the user that the FL / FLSD files cannot be forwarded.
     *
     * @param context Context
     * @hide
     */
    public static void showForwardForbiddenDialog(Context context) {
        // by default we use the android default icon.
        showForwardForbiddenDialog(context, android.R.drawable.ic_dialog_info);
    }

    /**
     * Informs the user that the FL / FLSD files cannot be forwarded.
     *
     * @param context Context
     * @param iconId the id of the icon resource to be used.
     * @hide
     */
    public static void showForwardForbiddenDialog(Context context, int iconId) {
        Xlog.v(TAG, "showForwardForbiddenDialog()");
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setPositiveButton(android.R.string.ok,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

        builder.setIcon(iconId)
               .setTitle(com.mediatek.internal.R.string.drm_forwardforbidden_title)
               .setMessage(com.mediatek.internal.R.string.drm_forwardforbidden_message);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Informs the user that the operation will consume drm rights and user can choose to accept or
     * cancel.
     *
     * @param context Context
     * @param listener OnClickListener, listener should listen for DialogInterface.BUTTON_POSITIVE, BUTTON_NEGATIVE
     * @param dismissListener OnDismissListener, listener should listen for dimiss
     * @return Dialog The dialog being shown.
     * @hide
     */
    public static Dialog showConsumeDialog(Context context,
            DialogInterface.OnClickListener listener,
            DialogInterface.OnDismissListener dismissListener) {
        Xlog.d(TAG, "showConsumeDialog() within context: " + context);

        checkCustomAlertDialog(sLicenseDialogQueue, context);
        Dialog result = validateCustomAlertDialog(sConsumeDialogQueue, context);
        if (null != result) {
            Xlog.d(TAG, "showConsumeDialog(): use the existing one");
            return result;
        }

        CustomAlertDialog dialog = new CustomAlertDialog(context, sConsumeDialogQueue);
        Resources res = context.getResources();
        dialog.setButton(Dialog.BUTTON_POSITIVE,
            res.getString(android.R.string.ok),
            listener);
        dialog.setButton(Dialog.BUTTON_NEGATIVE,
            res.getString(android.R.string.cancel),
            listener);
        dialog.setIcon(android.R.drawable.ic_dialog_info);
        dialog.setTitle(com.mediatek.internal.R.string.drm_consume_title);
        dialog.setMessage(res.getString(com.mediatek.internal.R.string.drm_consume_message));
        dialog.setOnDismissListener(dismissListener);
        dialog.show();

        return dialog;
    }

    /**
     * M: special for VideoPlayer
     * @hide
     */
    public static Dialog showConsume(
            Context context, final DrmOperationListener listener) {
        Xlog.d(TAG, "showConsume() within context: " + context);

        checkCustomAlertDialog(sLicenseDialogQueue, context);
        Dialog result = validateCustomAlertDialog(sConsumeDialogQueue, context);
        if (null != result) {
            Xlog.d(TAG, "showConsume(): use the existing one");
            return result;
        }

        CustomAlertDialog dialog = new CustomAlertDialog(context, sConsumeDialogQueue);
        Resources res = context.getResources();
        // OK to continue
        dialog.setButton(Dialog.BUTTON_POSITIVE,
            res.getString(android.R.string.ok),
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (listener != null) {
                        listener.onOperated(DrmOperationListener.CONTINUE);
                    }
                }
            }
        );
        // CANCEL to stop
        dialog.setButton(Dialog.BUTTON_NEGATIVE,
            res.getString(android.R.string.cancel),
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (listener != null) {
                        listener.onOperated(DrmOperationListener.STOP);
                    }
                }
            }
        );
        dialog.setIcon(android.R.drawable.ic_dialog_info);
        dialog.setTitle(com.mediatek.internal.R.string.drm_consume_title);
        dialog.setMessage(res.getString(com.mediatek.internal.R.string.drm_consume_message));
        // the "back" key shall also perform STOP
        dialog.setOnCancelListener(
            new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    if (listener != null) {
                        listener.onOperated(DrmOperationListener.STOP);
                    }
                }
            }
        );
        dialog.show();

        return dialog;
    }

    /**
     * Show protection info dialog.
     *
     * @param context Context
     * @param uri Uri of the protected content
     * @return Dialog The dialog being shown. null if it fails
     * @hide
     */
    public Dialog showProtectionInfoDialog(Context context, Uri uri) {
        if (null == uri || Uri.EMPTY == uri) {
            Xlog.e(TAG, "showProtectionInfoDialog() : Given uri is not valid");
            return null;
        }

        String path = null;
        try {
            path = convertUriToPath(uri);
        } catch (IllegalArgumentException e) {
            Xlog.e(TAG, "showProtectionInfoDialog() : " + e.getMessage());
            return null;
        }
        return showProtectionInfoDialog(context, path);
    }

    /**
     * Show protection info dialog.
     *
     * @param context Context
     * @param path Path of the protected content
     * @return Dialog The dialog being shown. null if it fails
     * @hide
     */
    public Dialog showProtectionInfoDialog(final Context context, String path) {
        Xlog.d(TAG, "showProtectionInfoDialog() withing context: " + context);

        if (null == path || path.equals("")) {
            Xlog.e(TAG, "showProtectionInfoDialog() : Given path should be non null");
            return null;
        }

        Dialog result = validateCustomAlertDialog(sProtectionInfoDialogQueue, context);
        if (null != result) {
            Xlog.d(TAG, "showProtectionInfoDialog(): use the existing one");
            return result;
        }

        final View scrollView =
            View.inflate(context, com.mediatek.internal.R.layout.drm_protectioninfoview, null);
        TextView fileNameView =
            (TextView)scrollView.findViewById(com.mediatek.internal.R.id.drm_file_name_value);
        if (fileNameView == null) {
            Xlog.e(TAG, "showProtectionInfoDialog() : the TextView: fileNameView is null");
            return null;
        }

        int start = path.lastIndexOf("/");
        int end = path.lastIndexOf(".");
        String fileNameStr = path.substring(start + 1, end);
        fileNameView.setText(fileNameStr);

        TextView protectionInfoStatusView =
                (TextView)scrollView.findViewById(com.mediatek.internal.R.id.drm_protection_status_value);
        if (protectionInfoStatusView == null) {
            Xlog.e(TAG, "showProtectionInfoDialog() : the TextView: protectionInfoStatusView is null");
            return null;
        }

        int rightsStatus = checkRightsStatus(path, DrmStore.Action.TRANSFER);
        if (rightsStatus == DrmStore.RightsStatus.RIGHTS_VALID) {
            protectionInfoStatusView.setText(com.mediatek.internal.R.string.drm_can_forward);
        } else if (rightsStatus == DrmStore.RightsStatus.RIGHTS_INVALID) {
            protectionInfoStatusView.setText(com.mediatek.internal.R.string.drm_can_not_forward);
        }

        TextView beginView =
                (TextView)scrollView.findViewById(com.mediatek.internal.R.id.drm_begin);
        if (beginView == null) {
            Xlog.e(TAG, "showProtectionInfoDialog() : the TextView: beginView is null");
            return null;
        }
        TextView endView =
                (TextView)scrollView.findViewById(com.mediatek.internal.R.id.drm_end);
        if (endView == null) {
            Xlog.e(TAG, "showProtectionInfoDialog() : the TextView: endView is null");
            return null;
        }
        TextView useLeftView =
                (TextView)scrollView.findViewById(com.mediatek.internal.R.id.drm_use_left);
        if (useLeftView == null) {
            Xlog.e(TAG, "showProtectionInfoDialog() : the TextView: useLeftView is null");
            return null;
        }
        TextView beginValueView =
                (TextView)scrollView.findViewById(com.mediatek.internal.R.id.drm_begin_value);
        if (beginValueView == null) {
            Xlog.e(TAG, "showProtectionInfoDialog() : the TextView: beginValueView is null");
            return null;
        }
        TextView endValueView =
                (TextView)scrollView.findViewById(com.mediatek.internal.R.id.drm_end_value);
        if (endValueView == null) {
            Xlog.e(TAG, "showProtectionInfoDialog() : the TextView: endValueView is null");
            return null;
        }
        TextView useLeftValueView =
                (TextView)scrollView.findViewById(com.mediatek.internal.R.id.drm_use_left_value);
        if (useLeftValueView == null) {
            Xlog.e(TAG, "showProtectionInfoDialog() : the TextView: useLeftValueView is null");
            return null;
        }

        String mime = getOriginalMimeType(path);
        if (null == mime) {
            Xlog.e(TAG, "showProtectionInfoDialog() : failed to get the original mime type");
            return null;
        }

        // the dialog prepared
        CustomAlertDialog dialog = new CustomAlertDialog(context, sProtectionInfoDialogQueue);
        Resources res = context.getResources();

        ContentValues values = getConstraints(path, DrmUtils.getAction(mime));
        if (values == null || values.size() == 0) { // no rights
            beginView.setText(com.mediatek.internal.R.string.drm_no_license);
            endView.setText("");
            useLeftView.setText("");

            ContentValues cv = getMetadata(path);
            String rightsIssuer = null;
            if (cv != null && cv.containsKey(DrmStore.MetadataKey.META_KEY_RIGHTS_ISSUER)) {
                rightsIssuer = cv.getAsString(DrmStore.MetadataKey.META_KEY_RIGHTS_ISSUER);
            }

            // the rights issuer is valid, we have a "renew" button in dialog
            final String rightsIssuerFinal = rightsIssuer;
            if (rightsIssuerFinal != null && !rightsIssuerFinal.isEmpty()) {
                dialog.setButton(Dialog.BUTTON_POSITIVE,
                    res.getString(com.mediatek.internal.R.string.drm_protectioninfo_renew),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //renew rights, start browser
                            Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(rightsIssuerFinal));
                            context.startActivity(it);
                            dialog.dismiss();
                        }
                    }
                );
            }
        } else { // with rights, FL will put -1
            if (values.containsKey(DrmStore.ConstraintsColumns.LICENSE_START_TIME)) {
                Long startL = values.getAsLong(DrmStore.ConstraintsColumns.LICENSE_START_TIME);
                if (startL == null) {
                    Xlog.e(TAG, "showProtectionInfoDialog() : startL is null");
                    return null;
                }
                if (startL == -1) {
                    beginValueView.setText(com.mediatek.internal.R.string.drm_no_limitation);
                } else {
                    beginValueView.setText(toDateTimeString(startL));
                }
            } else {
                beginValueView.setText(com.mediatek.internal.R.string.drm_no_limitation);
            }

            if (values.containsKey(DrmStore.ConstraintsColumns.LICENSE_EXPIRY_TIME)) {
                Long endL = values.getAsLong(DrmStore.ConstraintsColumns.LICENSE_EXPIRY_TIME);
                if (endL == null) {
                    Xlog.e(TAG, "showProtectionInfoDialog() : endL is null");
                    return null;
                }
                if (endL == -1) {
                    endValueView.setText(com.mediatek.internal.R.string.drm_no_limitation);
                } else {
                    endValueView.setText(toDateTimeString(endL));
                }
            } else {
                endValueView.setText(com.mediatek.internal.R.string.drm_no_limitation);
            }

            if (values.containsKey(DrmStore.ConstraintsColumns.REMAINING_REPEAT_COUNT)
                    && values.containsKey(DrmStore.ConstraintsColumns.MAX_REPEAT_COUNT)) {
                Long remainCount = values.getAsLong(DrmStore.ConstraintsColumns.REMAINING_REPEAT_COUNT);
                if (remainCount == null) {
                    Xlog.e(TAG, "showProtectionInfoDialog() : remainCount is null");
                    return null;
                }

                Long maxCount = values.getAsLong(DrmStore.ConstraintsColumns.MAX_REPEAT_COUNT);
                if (maxCount == null) {
                    Xlog.e(TAG, "showProtectionInfoDialog() : maxCount is null");
                    return null;
                }

                Xlog.v(TAG, "showProtectionInfoDialog() : remainCount=" + remainCount + ", maxCount=" + maxCount);
                if (remainCount == -1 || maxCount == -1) {
                    useLeftValueView.setText(com.mediatek.internal.R.string.drm_no_limitation);
                } else {
                    useLeftValueView.setText(remainCount.toString() + "/" + maxCount.toString());
                }
            } else {
                useLeftValueView.setText(com.mediatek.internal.R.string.drm_no_limitation);
            }
        }

        // we always have a "OK" button which does nothing.
        dialog.setButton(Dialog.BUTTON_NEUTRAL,
            res.getString(android.R.string.ok),
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }
        );
        dialog.setTitle(com.mediatek.internal.R.string.drm_protectioninfo_title);
        dialog.setView(scrollView);
        dialog.show();

        return dialog;
    }

    /**
     * Show license acquisition dialog.
     *
     * @param context Context
     * @param uri Uri of the protected content
     * @return Dialog The dialog being shown. null if it fails
     * @hide
     */
    public Dialog showLicenseAcquisitionDialog(Context context, Uri uri) {
        return showLicenseAcquisitionDialog(context, uri, null);
    }

    /**
     * Show license acquisition dialog.
     *
     * @param context Context
     * @param path Path of the protected content
     * @return Dialog The dialog being shown. null if it fails
     * @hide
     */
    public Dialog showLicenseAcquisitionDialog(Context context, String path) {
        return showLicenseAcquisitionDialog(context, path, null);
    }

    /**
     * Show license acquisition dialog.
     *
     * @param context Context
     * @param uri Uri of the protected content
     * @param dismissListener OnDismissListener
     * @return Dialog The dialog being shown. null if it fails
     * @hide
     */
    public Dialog showLicenseAcquisitionDialog(Context context, Uri uri,
            DialogInterface.OnDismissListener dismissListener) {
        if (OMA_DRM_FL_ONLY) {
            Xlog.d(TAG, "showLicenseAcquisitionDialog() : Forward-lock-only is set.");
            return null;
        }
        if (null == uri || Uri.EMPTY == uri) {
            Xlog.e(TAG, "showLicenseAcquisitionDialog() : Given uri is not valid");
            return null;
        }
        if (!(context instanceof Activity)) {
            Xlog.w(TAG, "showLicenseAcquisitionDialog() : not an Activity context, give up");
            return null;
        }

        String path = null;
        try {
            path = convertUriToPath(uri);
        } catch (IllegalArgumentException e) {
            Xlog.e(TAG, "showLicenseAcquisitionDialog() : " + e.getMessage());
            return null;
        }
        return showLicenseAcquisitionDialog(context, path, dismissListener);
    }

    /**
     * Show license acquisition dialog.
     *
     * @param context Context
     * @param path Path of the protected content
     * @param dismissListener OnDismissListener
     * @return Dialog The dialog being shown. null if it fails
     * @hide
     */
    public Dialog showLicenseAcquisitionDialog(final Context context, String path,
            DialogInterface.OnDismissListener dismissListener) {
        if (OMA_DRM_FL_ONLY) {
            Xlog.d(TAG, "showLicenseAcquisitionDialog() : Forward-lock-only is set.");
            return null;
        }
        Xlog.d(TAG, "showLicenseAcquisitionDialog() within context: " + context);

        if (null == path || path.equals("")) {
            Xlog.e(TAG, "showLicenseAcquisitionDialog() : Given path should be non null");
            return null;
        }
        if (!(context instanceof Activity)) {
            Xlog.w(TAG, "showLicenseAcquisitionDialog() : not an Activity context, give up");
            return null;
        }

        checkCustomAlertDialog(sConsumeDialogQueue, context);
        Dialog result = validateCustomAlertDialog(sLicenseDialogQueue, context);
        if (null != result) {
            Xlog.d(TAG, "showLicenseAcquisitionDialog(): use the existing one");
            return result;
        }

        // try to get rights-issuer
        ContentValues cv = getMetadata(path);
        String rightsIssuer = null;
        if (cv != null && cv.containsKey(DrmStore.MetadataKey.META_KEY_RIGHTS_ISSUER)) {
            rightsIssuer = cv.getAsString(DrmStore.MetadataKey.META_KEY_RIGHTS_ISSUER);
        }

        // for Combined Delivery, there's no rights-issuer
        if (rightsIssuer == null || !rightsIssuer.startsWith("http")) {
            Toast.makeText(context,
                com.mediatek.internal.R.string.drm_toast_license_expired,
                Toast.LENGTH_LONG).show();
            return null;
        }

        // valid rights-issuer for SD/FLSD
        Resources res = context.getResources();
        String message = String.format(
            res.getString(com.mediatek.internal.R.string.drm_licenseacquisition_message),
            path);
        final String rightsIssuerFinal = rightsIssuer;

        CustomAlertDialog dialog = new CustomAlertDialog(context, sLicenseDialogQueue);
        // OK to launch browser
        dialog.setButton(Dialog.BUTTON_POSITIVE,
            res.getString(com.mediatek.internal.R.string.drm_protectioninfo_renew),
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent it =
                        new Intent(Intent.ACTION_VIEW, Uri.parse(rightsIssuerFinal));
                    context.startActivity(it);
                }
            }
        );
        // CANCEL to do nothing
        dialog.setButton(Dialog.BUTTON_NEGATIVE,
            res.getString(android.R.string.cancel),
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // nothing
                }
            }
        );
        dialog.setIcon(android.R.drawable.ic_dialog_info);
        dialog.setTitle(com.mediatek.internal.R.string.drm_licenseacquisition_title);
        dialog.setMessage(message);
        dialog.setOnDismissListener(dismissListener);
        dialog.show();

        return dialog;
    }

    /**
     * M: special for VideoPlayer
     * @hide
     */
    public Dialog showLicenseAcquisition(
            Context context, Uri uri, DrmOperationListener listener) {
        if (OMA_DRM_FL_ONLY) {
            Xlog.d(TAG, "showLicenseAcquisition() : Forward-lock-only is set.");
            return null;
        }
        if (null == uri || Uri.EMPTY == uri) {
            Xlog.e(TAG, "showLicenseAcquisition() : Given uri is not valid");
            return null;
        }
        if (!(context instanceof Activity)) {
            Xlog.w(TAG, "showLicenseAcquisition() : not an Activity context, give up");
            return null;
        }

        String path = null;
        try {
            path = convertUriToPath(uri);
        } catch (IllegalArgumentException e) {
            Xlog.e(TAG, "showLicenseAcquisition() : " + e.getMessage());
            return null;
        }
        return showLicenseAcquisition(context, path, listener);
    }

    /**
     * M: special for VideoPlayer
     * @hide
     */
    public Dialog showLicenseAcquisition(
            final Context context, final String path, final DrmOperationListener listener) {
        if (OMA_DRM_FL_ONLY) {
            Xlog.d(TAG, "showLicenseAcquisition() : Forward-lock-only is set.");
            return null;
        }
        Xlog.d(TAG, "showLicenseAcquisition() within context: " + context);

        if (null == path || path.equals("")) {
            Xlog.e(TAG, "showLicenseAcquisition() : Given path should be non null");
            return null;
        }
        if (!(context instanceof Activity)) {
            Xlog.w(TAG, "showLicenseAcquisition() : not an Activity context, give up");
            return null;
        }

        checkCustomAlertDialog(sConsumeDialogQueue, context);
        Dialog result = validateCustomAlertDialog(sLicenseDialogQueue, context);
        if (null != result) {
            Xlog.d(TAG, "showLicenseAcquisition(): use the existing one");
            return result;
        }

        // try to get right-issuer
        ContentValues cv = getMetadata(path);
        String rightsIssuer = null;
        if (cv != null && cv.containsKey(DrmStore.MetadataKey.META_KEY_RIGHTS_ISSUER)) {
            rightsIssuer = cv.getAsString(DrmStore.MetadataKey.META_KEY_RIGHTS_ISSUER);
        }

        // for Combined Delivery, there's no rights-issuer
        if (rightsIssuer == null || !rightsIssuer.startsWith("http")) {
            Toast.makeText(context,
                com.mediatek.internal.R.string.drm_toast_license_expired,
                Toast.LENGTH_LONG).show();
            if (listener != null) {
                listener.onOperated(DrmOperationListener.STOP);
            }
            return null;
        }

        // valid rights-issuer for SD/FLSD
        Resources res = context.getResources();
        String message = String.format(
            res.getString(com.mediatek.internal.R.string.drm_licenseacquisition_message),
            path);
        final String rightsIssuerFinal = rightsIssuer;

        CustomAlertDialog dialog = new CustomAlertDialog(context, sLicenseDialogQueue);
        // OK to launch browser
        dialog.setButton(Dialog.BUTTON_POSITIVE,
            res.getString(com.mediatek.internal.R.string.drm_protectioninfo_renew),
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent it =
                        new Intent(Intent.ACTION_VIEW, Uri.parse(rightsIssuerFinal));
                    context.startActivity(it);
                }
            }
        );
        // CANCEL to do nothing
        dialog.setButton(Dialog.BUTTON_NEGATIVE,
            res.getString(android.R.string.cancel),
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // nothing
                }
            }
        );
        dialog.setIcon(android.R.drawable.ic_dialog_info);
        dialog.setTitle(com.mediatek.internal.R.string.drm_licenseacquisition_title);
        dialog.setMessage(message);
        // when dismiss, cause the drm operation listener to STOP
        dialog.setOnDismissListener(
            new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    if (listener != null) {
                        Xlog.d(TAG, "showLicenseAcquisition(): drm operation listener to operate STOP.");
                        listener.onOperated(DrmOperationListener.STOP);
                    }
                }
            }
        );
        dialog.show();

        return dialog;
    }

    /**
     * Overlay back ground bitmap with front pic right skew.
     *
     * @param bgdBmp Background bitmap to draw on.
     * @param front front drawable to draw
     * @return Bitmap New bitmap with overlay
     * @hide
     */
    public Bitmap overlayBitmapSkew(Bitmap bgdBmp, Drawable front) {
        if (null == bgdBmp || null == front) {
            Xlog.e(TAG, "overlayBitmapSkew() : Given background / front pic is null.");
            return null;
        }

        // get DPI info and offset
        int offset = 0;
        if (mContext instanceof Activity) {
            DisplayMetrics metric = new DisplayMetrics();
            ((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(metric);
            int densityDpi = metric.densityDpi;
            offset = 6 * DisplayMetrics.DENSITY_DEFAULT / densityDpi;
        } else {
            Xlog.e(TAG, "overlayBitmapSkew() : Given mContext is not an Activity type: "+mContext);
        }

        Bitmap bMutable = Bitmap.createBitmap(bgdBmp.getWidth() + offset,
                              bgdBmp.getHeight(),
                              bgdBmp.getConfig());
        Canvas overlayCanvas = new Canvas(bMutable);
        if (!bgdBmp.isRecycled()) { // make sure the bitmap is valid otherwise we use an empty one (may be black or white)
            overlayCanvas.drawBitmap(bgdBmp, 0, 0, null);
        }
        int overlayWidth = front.getIntrinsicWidth();
        int overlayHeight = front.getIntrinsicHeight();
        int left = bMutable.getWidth() - overlayWidth;
        int top = bMutable.getHeight() - overlayHeight;
        Rect newBounds = new Rect(left, top, left + overlayWidth, top + overlayHeight);
        front.setBounds(newBounds);
        front.draw(overlayCanvas);
        return bMutable;
    }

    /**
     * Overlay back ground bitmap with front pic right skew.
     *
     * @param res Resources.
     * @param bgdBmpId Back ground bitmap resource id.
     * @param frontId Front drawable resource id.
     * @return Bitmap New bitmap with overlay
     * @hide
     */
    public Bitmap overlayBitmapSkew(Resources res, int bgdBmpId, int frontId) {
        Bitmap bgdBmp = BitmapFactory.decodeResource(res, bgdBmpId);
        Drawable front = res.getDrawable(frontId);
        Bitmap bmp = overlayBitmapSkew(bgdBmp, front);
        if (bgdBmp != bmp && bgdBmp != null && !bgdBmp.isRecycled()) {
            bgdBmp.recycle();
            bgdBmp = null;
        }
        return bmp;
    }

    /**
     * Overlay back ground bitmap with front pic.
     *
     * @param bgdBmp Background bitmap to draw on.
     * @param front front drawable to draw
     * @return Bitmap New bitmap with overlay
     * @hide
     */
    public Bitmap overlayBitmap(Bitmap bgdBmp, Drawable front) {
        if (null == bgdBmp || null == front) {
            Xlog.e(TAG, "overlayBitmap() : Given background / front pic is null.");
            return null;
        }

        Bitmap bMutable = Bitmap.createBitmap(bgdBmp.getWidth(),
                              bgdBmp.getHeight(),
                              bgdBmp.getConfig());
        Canvas overlayCanvas = new Canvas(bMutable);
        if (!bgdBmp.isRecycled()) { // make sure the bitmap is valid otherwise we use an empty one (may be black or white)
            overlayCanvas.drawBitmap(bgdBmp, 0, 0, null);
        }
        int overlayWidth = front.getIntrinsicWidth();
        int overlayHeight = front.getIntrinsicHeight();
        int left = bgdBmp.getWidth() - overlayWidth;
        int top = bgdBmp.getHeight() - overlayHeight;
        Rect newBounds = new Rect(left, top, left + overlayWidth, top + overlayHeight);
        front.setBounds(newBounds);
        front.draw(overlayCanvas);
        return bMutable;
    }

    /**
     * Overlay back ground bitmap with front pic.
     *
     * @param res Resources.
     * @param bgdBmpId Back ground bitmap resource id.
     * @param frontId Front drawable resource id.
     * @return Bitmap New bitmap with overlay
     * @hide
     */
    public Bitmap overlayBitmap(Resources res, int bgdBmpId, int frontId) {
        Bitmap bgdBmp = BitmapFactory.decodeResource(res, bgdBmpId);
        Drawable front = res.getDrawable(frontId);
        Bitmap bmp = overlayBitmap(bgdBmp, front);
        if (bgdBmp != bmp && bgdBmp != null && !bgdBmp.isRecycled()) {
            bgdBmp.recycle();
            bgdBmp = null;
        }
        return bmp;
    }

    /**
     * Overlay back ground bitmap with drm icon right skew.
     *
     * @param res Resources.
     * @param path Path of drm content.
     * @param action Drm action.
     * @param bgdBmp Back ground bitmap.
     * @return Bitmap New bitmap with overlay
     * @hide
     */
    public Bitmap overlayDrmIconSkew(Resources res, String path, int action, Bitmap bgdBmp) {
        int method = getMethod(path);
        if (method == DrmStore.DrmMethod.METHOD_NONE) {
            Xlog.d(TAG, "overlayDrmIconSkew() : not set drm icon because of invalid method");
            return bgdBmp;
        }

        if (method == DrmStore.DrmMethod.METHOD_FL) {
            Xlog.d(TAG, "overlayDrmIconSkew() : not set drm icon because method is FL.");
            return bgdBmp;
        }

        int rightsStatus = checkRightsStatus(path, action);
        int lockId = -1;
        if (rightsStatus == DrmStore.RightsStatus.RIGHTS_VALID) {
            lockId = com.mediatek.internal.R.drawable.drm_green_lock;
        } else {
            lockId = com.mediatek.internal.R.drawable.drm_red_lock;
        }
        Drawable front = res.getDrawable(lockId);
        return overlayBitmapSkew(bgdBmp, front);
    }

    /**
     * Overlay back ground bitmap with drm icon right skew.
     *
     * @param res Resources.
     * @param path Path of drm content.
     * @param action Drm action.
     * @param bgdBmpId Back ground bitmap resource id.
     * @return Bitmap New bitmap with overlay
     * @hide
     */
    public Bitmap overlayDrmIconSkew(Resources res, String path, int action, int bgdBmpId) {
        Bitmap bgdBmp = BitmapFactory.decodeResource(res, bgdBmpId);
        Bitmap bmp = overlayDrmIconSkew(res, path, action, bgdBmp);
        if (bgdBmp != bmp && bgdBmp != null && !bgdBmp.isRecycled()) {
            bgdBmp.recycle();
            bgdBmp = null;
        }
        return bmp;
    }

    /**
     * Overlay back ground bitmap with drm icon.
     *
     * @param res Resources.
     * @param path Path of drm content.
     * @param action Drm action.
     * @param bgdBmp Back ground bitmap.
     * @return Bitmap New bitmap with overlay
     * @hide
     */
    public Bitmap overlayDrmIcon(Resources res, String path, int action, Bitmap bgdBmp) {
        int method = getMethod(path);
        if (method == DrmStore.DrmMethod.METHOD_NONE) {
            Xlog.d(TAG, "overlayDrmIcon() : not set drm icon because of invalid method");
            return bgdBmp;
        }

        if (method == DrmStore.DrmMethod.METHOD_FL) {
            Xlog.d(TAG, "overlayDrmIcon() : not set drm icon because method is FL");
            return bgdBmp;
        }

        int rightsStatus = checkRightsStatus(path, action);
        int lockId = -1;
        if (rightsStatus == DrmStore.RightsStatus.RIGHTS_VALID) {
            lockId = com.mediatek.internal.R.drawable.drm_green_lock;
        } else {
            lockId = com.mediatek.internal.R.drawable.drm_red_lock;
        }
        Drawable front = res.getDrawable(lockId);
        return overlayBitmap(bgdBmp, front);
    }

    /**
     * Overlay back ground bitmap with drm icon.
     *
     * @param res Resources.
     * @param path Path of drm content.
     * @param action Drm action.
     * @param bgdBmpId Back ground bitmap resource id.
     * @return Bitmap New bitmap with overlay
     * @hide
     */
    public Bitmap overlayDrmIcon(Resources res, String path, int action, int bgdBmpId) {
        Bitmap bgdBmp = BitmapFactory.decodeResource(res, bgdBmpId);
        Bitmap bmp = overlayDrmIcon(res, path, action, bgdBmp);
        if (bgdBmp != bmp && bgdBmp != null && !bgdBmp.isRecycled()) {
            bgdBmp.recycle();
            bgdBmp = null;
        }
        return bmp;
    }

    /**
     * Get drm method from drm content
     *
     * @param uri Content URI from where drm method would be retrieved.
     * @return int DrmStore.DrmMethod
     * @hide
     */
    public int getMethod(Uri uri) {
        if (null == uri || Uri.EMPTY == uri) {
            throw new IllegalArgumentException("Given uri is not valid");
        }
        return getMethod(convertUriToPath(uri));
    }

    /**
     * Get drm method from drm content
     *
     * @param path Path from where drm method would be retrieved.
     * @return int DrmStore.DrmMethod
     * @hide
     */
    public int getMethod(String path) {
        if (null == path || path.equals("")) {
            throw new IllegalArgumentException("Given path should be non null");
        }

        ContentValues cv = getMetadata(path);
        if (cv != null && cv.containsKey(DrmStore.MetadataKey.META_KEY_METHOD)) {
            Integer m = cv.getAsInteger(DrmStore.MetadataKey.META_KEY_METHOD);
            if (m != null) {
                return m.intValue();
            }
        }
        return DrmStore.DrmMethod.METHOD_NONE;
    }

    /**
     * Checks whether the given rights-protected content has valid rights for the specified
     * {@link DrmStore.Action}.
     *
     * ATTENTION: would return valid & invalid & secure_timer_invalid,
                  please use this API before showing LicenseAcquisition Dialog
     *
     * @param path Path to the rights-protected content.
     * @param action The {@link DrmStore.Action} to perform.
     * @return An <code>int</code> representing the {@link DrmStore.RightsStatus} of the content.
     * @see DrmStore.RightsStatus
     *
     * @hide
     */
      public int checkRightsStatusForTap(String path, int action) {
        if (null == path || path.equals("") || !DrmStore.Action.isValid(action)) {
            throw new IllegalArgumentException("Given path or action is not valid");
        }
        Xlog.v(TAG, "checkRightsStatusForTap(): java api. path=" + path + ", action=" + action + ", mContext=" + mContext);

        int result = _checkRightsStatus(mUniqueId, path, action);
        Xlog.v(TAG, "checkRightsStatusForTap(): java api. the result=" + result);

        return result;
     }

    /**
     * Checks whether the given rights-protected content has valid rights for the specified
     * {@link DrmStore.Action}.
     *
     * ATTENTION: would return valid & invalid & secure_timer_invalid,
                  please use this API before showing LicenseAcquisition Dialog
     *
     * @param uri URI for the rights-protected content.
     * @param action The {@link DrmStore.Action} to perform.
     * @return An <code>int</code> representing the {@link DrmStore.RightsStatus} of the content.
     * @see DrmStore.RightsStatus
     *
     * @hide
     */
     public int checkRightsStatusForTap(Uri uri, int action) {
        if (null == uri || Uri.EMPTY == uri) {
            throw new IllegalArgumentException("Given uri is not valid");
        }
        return checkRightsStatusForTap(convertUriToPath(uri), action);
     }

    /**
     * Convert seconds to date time string.
     *
     * @param sec Seconds
     * @return String Date time string
     */
    private String toDateTimeString(Long sec) {
        Date date = new Date(sec.longValue() * 1000L);
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        String str = dateFormat.format(date);
        return str;
    }
}

