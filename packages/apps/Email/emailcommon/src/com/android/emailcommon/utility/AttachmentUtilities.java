/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.emailcommon.utility;

import com.android.emailcommon.Logging;
import com.android.emailcommon.provider.EmailContent.Attachment;
import com.android.emailcommon.provider.EmailContent.Message;
import com.android.emailcommon.provider.EmailContent.MessageColumns;
import com.mediatek.featureoption.FeatureOption;
import com.google.android.mms.ContentType;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.drm.DrmStore;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;

public class AttachmentUtilities {
    public static final String AUTHORITY = "com.android.email.attachmentprovider";
    public static final Uri CONTENT_URI = Uri.parse( "content://" + AUTHORITY);
    private static final String TAG = "AttachmentUtilities";
    public static final String FORMAT_RAW = "RAW";
    public static final String FORMAT_THUMBNAIL = "THUMBNAIL";
    
    //Choose types of attachment
    private static final String AUDIO_OGG_MIME_TYPE = "application/x-ogg";
    /*
     * This is a parameter defined by contact app,to differ what kind of contact list be back.  
     */
    public static final int CHOICE_CONTACT_REQUEST_TYPE = 3;
    //Set max number user would choose in contact list.
    public static final int MAX_CHOICE_NUMBER = 20;
    public static final String CHOICE_CONTACT_ACTION     = "android.intent.action.contacts.list.PICKMULTICONTACTS";
    public static final String CHOICE_CALENDAR_ACTION    = "android.intent.action.CALENDARCHOICE";
    public static final String CHOICE_FILEMANAGER_ACTION = "com.mediatek.filemanager.ADD_FILE";
    private static final String ATTACH_CONTACT_EXTRA_REQUEST_TYPE = "request_type";
    private static final String ATTACH_CONTACT_EXTRA_PICK_COUNT = "pick_count";
    private static final String CONTACT_MIME_TYPE = "vnd.android.cursor.dir/contact";
    private static final String ATTACH_CALENDAR_EXTRA_REQUEST_TYPE = "request_type";
    private static final String CALENDAR_MIME_TYPE = "text/x-vcalendar";

    //Set MimeTypes usually used
    public static final String TEXT_PLAIN_MIME_TYPE = "text/plain";
    public static final String GENERAL_MIME_TYPE = "application/octet-stream";
    public static final String MPO_MIME_TYPE = "image/mpo";

    public static class Columns {
        public static final String _ID = "_id";
        public static final String DATA = "_data";
        public static final String DISPLAY_NAME = "_display_name";
        public static final String SIZE = "_size";
    }

    /**
     * The MIME type(s) of attachments we're willing to send via attachments.
     *
     * Any attachments may be added via Intents with Intent.ACTION_SEND or ACTION_SEND_MULTIPLE.
     */
    public static final String[] ACCEPTABLE_ATTACHMENT_SEND_INTENT_TYPES = new String[] {
        "*/*",
    };
    /**
     * The MIME type(s) of attachments we're willing to send from the internal UI.
     *
     * NOTE:  At the moment it is not possible to open a chooser with a list of filter types, so
     * the chooser is only opened with the first item in the list.
     */
    public static final String[] ACCEPTABLE_ATTACHMENT_SEND_UI_TYPES = new String[] {
        "image/*",
        "video/*",
    };
    /**
     * The MIME type(s) of attachments we're willing to view.
     */
    public static final String[] ACCEPTABLE_ATTACHMENT_VIEW_TYPES = new String[] {
        "*/*",
    };
    /**
     * The MIME type(s) of attachments we're not willing to view.
     */
    public static final String[] UNACCEPTABLE_ATTACHMENT_VIEW_TYPES = new String[] {
    };
    /**
     * The MIME type(s) of attachments we're willing to download to SD.
     */
    public static final String[] ACCEPTABLE_ATTACHMENT_DOWNLOAD_TYPES = new String[] {
        "*/*",
    };
    /**
     * The MIME type(s) of attachments we're not willing to download to SD.
     */
    public static final String[] UNACCEPTABLE_ATTACHMENT_DOWNLOAD_TYPES = new String[] {
    };
    /**
     * Filename extensions of attachments we're never willing to download (potential malware).
     * Entries in this list are compared to the end of the lower-cased filename, so they must
     * be lower case, and should not include a "."
     */
    public static final String[] UNACCEPTABLE_ATTACHMENT_EXTENSIONS = new String[] {
        // File types that contain malware
        "ade", "adp", "bat", "chm", "cmd", "com", "cpl", "dll", "exe",
        "hta", "ins", "isp", "jse", "lib", "mde", "msc", "msp",
        "mst", "pif", "scr", "sct", "shb", "sys", "vb", "vbe",
        "vbs", "vxd", "wsc", "wsf", "wsh",
        // File types of common compression/container formats (again, to avoid malware)
        "zip", "gz", "z", "tar", "tgz", "bz2",
    };
    /**
     * Filename extensions of attachments that can be installed.
     * Entries in this list are compared to the end of the lower-cased filename, so they must
     * be lower case, and should not include a "."
     */
    public static final String[] INSTALLABLE_ATTACHMENT_EXTENSIONS = new String[] {
        "apk",
    };

    //Filename extensions usually used
    public static final String MPO_EXTENSION = "mpo";

    /**
     * The maximum size of an attachment we're willing to download (either View or Save)
     * Attachments that are base64 encoded (most) will be about 1.375x their actual size
     * so we should probably factor that in. A 5MB attachment will generally be around
     * 6.8MB downloaded but only 5MB saved.
     */
    public static final int MAX_ATTACHMENT_DOWNLOAD_SIZE = (5 * 1024 * 1024);
    /**
     * The maximum size of an attachment we're willing to upload (measured as stored on disk).
     * Attachments that are base64 encoded (most) will be about 1.375x their actual size
     * so we should probably factor that in. A 5MB attachment will generally be around
     * 6.8MB uploaded.
     */
    public static final int MAX_ATTACHMENT_UPLOAD_SIZE = (5 * 1024 * 1024);
    /*
     * The attachment is unavailable and is not safe to use.
     */
    public static final int ATTACHMENT_UNAVAILABLE_SIZE = -1;

    public static Uri getAttachmentUri(long accountId, long id) {
        return CONTENT_URI.buildUpon()
        .appendPath(Long.toString(accountId))
        .appendPath(Long.toString(id))
        .appendPath(FORMAT_RAW)
        .build();
    }

    public static Uri getAttachmentThumbnailUri(long accountId, long id,
            int width, int height) {
        return CONTENT_URI.buildUpon()
        .appendPath(Long.toString(accountId))
        .appendPath(Long.toString(id))
        .appendPath(FORMAT_THUMBNAIL)
        .appendPath(Integer.toString(width))
        .appendPath(Integer.toString(height))
        .build();
    }

    /**
     * Return the filename for a given attachment.  This should be used by any code that is
     * going to *write* attachments.
     *
     * This does not create or write the file, or even the directories.  It simply builds
     * the filename that should be used.
     */
    public static File getAttachmentFilename(Context context, long accountId, long attachmentId) {
        return new File(getAttachmentDirectory(context, accountId), Long.toString(attachmentId));
    }

    /**
     * Return the directory for a given attachment.  This should be used by any code that is
     * going to *write* attachments.
     *
     * This does not create or write the directory.  It simply builds the pathname that should be
     * used.
     */
    public static File getAttachmentDirectory(Context context, long accountId) {
        return context.getDatabasePath(accountId + ".db_att");
    }

    /**
     * Helper to convert unknown or unmapped attachments to something useful based on filename
     * extensions. The mime type is inferred based upon the table below. It's not perfect, but
     * it helps.
     *
     * <pre>
     *                   |---------------------------------------------------------|
     *                   |                  E X T E N S I O N                      |
     *                   |---------------------------------------------------------|
     *                   | .eml        | known(.png) | unknown(.abc) | none        |
     * | M |-----------------------------------------------------------------------|
     * | I | none        | msg/rfc822  | image/png   | app/abc       | app/oct-str |
     * | M |-------------| (always     |             |               |             |
     * | E | app/oct-str |  overrides  |             |               |             |
     * | T |-------------|             |             |-----------------------------|
     * | Y | text/plain  |             |             | text/plain                  |
     * | P |-------------|             |-------------------------------------------|
     * | E | any/type    |             | any/type                                  |
     * |---|-----------------------------------------------------------------------|
     * </pre>
     *
     * NOTE: Since mime types on Android are case-*sensitive*, return values are always in
     * lower case.
     *
     * @param fileName The given filename
     * @param mimeType The given mime type
     * @return A likely mime type for the attachment
     */
    public static String inferMimeType(final String fileName, final String mimeType) {
        String resultType = null;
        String fileExtension = getFilenameExtension(fileName);
        boolean isTextPlain = TEXT_PLAIN_MIME_TYPE.equalsIgnoreCase(mimeType);

        if ("eml".equals(fileExtension)) {
            resultType = "message/rfc822";
        } else {
            boolean isGenericType =
                    isTextPlain || GENERAL_MIME_TYPE.equalsIgnoreCase(mimeType);
            // If the given mime type is non-empty and non-generic, return it
            if (isGenericType || TextUtils.isEmpty(mimeType)) {
                if (!TextUtils.isEmpty(fileExtension)) {
                    // Try to get a mime type by file extension for Email
                    resultType = getAttachmentExtensionMimeType(fileExtension);
                    if (TextUtils.isEmpty(resultType)) {
                        // Otherwise, try to find a mime type based upon the file extension
                        resultType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
                        if (TextUtils.isEmpty(resultType)) {
                            // Finally, if original mimetype is text/plain, use it; otherwise synthesize
                            resultType = isTextPlain ? mimeType : "application/" + fileExtension;
                        }
                    }
                }
            } else {
                resultType = mimeType;
            }
        }

        // No good guess could be made; use an appropriate generic type
        if (TextUtils.isEmpty(resultType)) {
            resultType = isTextPlain ? TEXT_PLAIN_MIME_TYPE : GENERAL_MIME_TYPE;
        }
        return resultType.toLowerCase();
    }


    public static String getAttachmentExtensionMimeType(String fileNameExtension) {
        String mimeType = "";
        // Add extension mimeTypes here to avoid missing type
        if(MPO_EXTENSION.equalsIgnoreCase(fileNameExtension)) {
            mimeType = MPO_MIME_TYPE;
        }
        return mimeType;
    }

    /**
     * @return mime-type for a {@link Uri}.
     *    - Use {@link ContentResolver#getType} for a content: URI.
     *    - Use {@link #inferMimeType} for a file: URI.
     *    - Otherwise return null.
     */
    public static String inferMimeTypeForUri(Context context, Uri uri) {
        final String scheme = uri.getScheme();
        if ("content".equals(scheme)) {
            return context.getContentResolver().getType(uri);
        } else if ("file".equals(scheme)) {
            return inferMimeType(uri.getLastPathSegment(), "");
        } else {
            return null;
        }
    }

    /**
     * Extract and return filename's extension, converted to lower case, and not including the "."
     *
     * @return extension, or null if not found (or null/empty filename)
     */
    public static String getFilenameExtension(String fileName) {
        String extension = null;
        if (!TextUtils.isEmpty(fileName)) {
            int lastDot = fileName.lastIndexOf('.');
            if ((lastDot > 0) && (lastDot < fileName.length() - 1)) {
                extension = fileName.substring(lastDot + 1).toLowerCase();
            }
        }
        return extension;
    }

    /**
     * Resolve attachment id to content URI.  Returns the resolved content URI (from the attachment
     * DB) or, if not found, simply returns the incoming value.
     *
     * @param attachmentUri
     * @return resolved content URI
     *
     * TODO:  Throws an SQLite exception on a missing DB file (e.g. unknown URI) instead of just
     * returning the incoming uri, as it should.
     */
    public static Uri resolveAttachmentIdToContentUri(ContentResolver resolver, Uri attachmentUri) {
        Cursor c = resolver.query(attachmentUri,
                new String[] { Columns.DATA },
                null, null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    final String strUri = c.getString(0);
                    if (strUri != null) {
                        return Uri.parse(strUri);
                    }
                }
            } finally {
                c.close();
            }
        }
        return attachmentUri;
    }

    /**
     * In support of deleting a message, find all attachments and delete associated attachment
     * files.
     * @param context
     * @param accountId the account for the message
     * @param messageId the message
     */
    public static void deleteAllAttachmentFiles(Context context, long accountId, long messageId) {
        Uri uri = ContentUris.withAppendedId(Attachment.MESSAGE_ID_URI, messageId);
        Cursor c = context.getContentResolver().query(uri, Attachment.ID_PROJECTION,
                null, null, null);
        try {
            while (c.moveToNext()) {
                long attachmentId = c.getLong(Attachment.ID_PROJECTION_COLUMN);
                File attachmentFile = getAttachmentFilename(context, accountId, attachmentId);
                // Note, delete() throws no exceptions for basic FS errors (e.g. file not found)
                // it just returns false, which we ignore, and proceed to the next file.
                // This entire loop is best-effort only.
                attachmentFile.delete();
            }
        } finally {
            c.close();
        }
    }

    /**
     * In support of deleting a mailbox, find all messages and delete their attachments.
     *
     * @param context
     * @param accountId the account for the mailbox
     * @param mailboxId the mailbox for the messages
     */
    public static void deleteAllMailboxAttachmentFiles(Context context, long accountId,
            long mailboxId) {
        Cursor c = context.getContentResolver().query(Message.CONTENT_URI,
                Message.ID_COLUMN_PROJECTION, MessageColumns.MAILBOX_KEY + "=?",
                new String[] { Long.toString(mailboxId) }, null);
        try {
            while (c.moveToNext()) {
                long messageId = c.getLong(Message.ID_PROJECTION_COLUMN);
                deleteAllAttachmentFiles(context, accountId, messageId);
            }
        } finally {
            c.close();
        }
    }

    /**
     * In support of deleting or wiping an account, delete all related attachments.
     *
     * @param context
     * @param accountId the account to scrub
     */
    public static void deleteAllAccountAttachmentFiles(Context context, long accountId) {
        File[] files = getAttachmentDirectory(context, accountId).listFiles();
        if (files == null) return;
        for (File file : files) {
            boolean result = file.delete();
            if (!result) {
                Log.e(Logging.LOG_TAG, "Failed to delete attachment file " + file.getName());
            }
        }
    }

    public static String refactorHtmlTextRaw(String htmlBody, Attachment attachment) {
        String htmlText = htmlBody;
        // for html body, replace CID for inline images
        // Regexp which matches ' src="cid:contentId"'.
        //String contentIdRe = "\\s+(?i)(\"src\"|\"backkground\")=\"cid:(?-i)\\Q" + attachment.mContentId + "\\E\"";
        String contentIdRe = "\\s+(?i)src=\"cid:(?-i)\\Q" + attachment.mContentId + "\\E\"";
        String srcContentUri = " src=\"" + attachment.mContentUri + "\"";
        String backgroundRe = "\\s+(?i)background=\"cid:(?-i)\\Q" + attachment.mContentId + "\\E\"";
        String backgroundUri = " background=\"" + attachment.mContentUri + "\"";
        if (htmlText != null) {
            // Some Emails with background images will have htmls like:
            // background=cid:image0001:XXXXXXXXXXXXXXX
            // so we need to replace this contentId to contentUri
            htmlText = htmlText.replaceAll(backgroundRe, backgroundUri);
            htmlText = htmlText.replaceAll(contentIdRe, srcContentUri);
        }
        return htmlText;
    }

    public static void selectAudio(Context context, int requestCode)
            throws ActivityNotFoundException {
        if (context instanceof Activity) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(ContentType.AUDIO_UNSPECIFIED);
            intent.setType(ContentType.AUDIO_OGG);
            intent.setType(AUDIO_OGG_MIME_TYPE);
            if (FeatureOption.MTK_DRM_APP) {
                intent.putExtra(DrmStore.DrmExtra.EXTRA_DRM_LEVEL, DrmStore.DrmExtra.DRM_LEVEL_SD);
            }
            ((Activity) context).startActivityForResult(intent, requestCode);
            Logging.d(TAG, "Add attachment Music, send intent " + intent.toString());
        }
    }

    public static void selectVideo(Context context, int requestCode)
            throws ActivityNotFoundException {
        selectMediaByType(context, requestCode, ContentType.VIDEO_UNSPECIFIED);
    }

    public static void selectImage(Context context, int requestCode)
            throws ActivityNotFoundException {
        selectMediaByType(context, requestCode, ContentType.IMAGE_UNSPECIFIED);
    }

    private static void selectMediaByType(Context context, int requestCode,
            String contentType) throws ActivityNotFoundException {
        if (context instanceof Activity) {
            Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            innerIntent.setType(contentType);
            if (FeatureOption.MTK_DRM_APP) {
                innerIntent.putExtra(DrmStore.DrmExtra.EXTRA_DRM_LEVEL, DrmStore.DrmExtra.DRM_LEVEL_SD);
            }
            Intent wrapperIntent = Intent.createChooser(innerIntent, null);
            ((Activity) context).startActivityForResult(wrapperIntent,
                    requestCode);
            Logging.d(TAG, "Add attachment Image/Video, send intent " + innerIntent.toString());
        }
    }

    public static void selectContact(Context context, int requestCode) 
        throws ActivityNotFoundException{
        if (context instanceof Activity) {
            Intent intent = new Intent(CHOICE_CONTACT_ACTION);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra(ATTACH_CONTACT_EXTRA_REQUEST_TYPE,
                    CHOICE_CONTACT_REQUEST_TYPE);
            intent.putExtra(ATTACH_CONTACT_EXTRA_PICK_COUNT, MAX_CHOICE_NUMBER);
            intent.setType(CONTACT_MIME_TYPE);
            ((Activity) context).startActivityForResult(intent, requestCode);
            Logging.d(TAG, "Add attachment Contact, send intent " + intent.toString());
        }
    }
    
    public static void selectCalendar(Context context, int requestCode)
            throws ActivityNotFoundException {
        if (context instanceof Activity) {
            Intent intent = new Intent(CHOICE_CALENDAR_ACTION);
            intent.setType(CALENDAR_MIME_TYPE);
            intent.putExtra(ATTACH_CALENDAR_EXTRA_REQUEST_TYPE, 0);
            ((Activity) context).startActivityForResult(intent, requestCode);
            Logging.d(TAG, "Add attachment Calendar, send intent " + intent.toString());
        }
    }
    
    public static void selectFile(Context context, int requestCode) {
        if (context instanceof Activity) {
            Intent intent = new Intent(CHOICE_FILEMANAGER_ACTION);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            if (FeatureOption.MTK_DRM_APP) {
            intent.putExtra(DrmStore.DrmExtra.EXTRA_DRM_LEVEL,
            DrmStore.DrmExtra.DRM_LEVEL_SD);
            }
            ((Activity) context).startActivityForResult(intent, requestCode);
            Logging.d(TAG, "Add attachment FileManager, send intent " + intent.toString());
        }
    }
}
