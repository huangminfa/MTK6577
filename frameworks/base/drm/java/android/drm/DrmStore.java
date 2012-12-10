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

import java.lang.String;

/**
 * Defines constants that are used by the DRM framework.
 *
 */
public class DrmStore {
    /**
     * Interface definition for the columns that represent DRM constraints.
     */
    public interface ConstraintsColumns {
        /**
         * The maximum repeat count.
         * <p>
         * Type: INTEGER
         */
        public static final String MAX_REPEAT_COUNT = "max_repeat_count";

        /**
         * The remaining repeat count.
         * <p>
         * Type: INTEGER
         */
        public static final String REMAINING_REPEAT_COUNT = "remaining_repeat_count";

        /**
         * The time before which the rights-protected file cannot be played/viewed.
         * <p>
         * Type: TEXT
         */
        public static final String LICENSE_START_TIME = "license_start_time";

        /**
         * The time after which the rights-protected file cannot be played/viewed.
         * <p>
         * Type: TEXT
         */
        public static final String LICENSE_EXPIRY_TIME = "license_expiry_time";

        /**
         * The available time left before the license expires.
         * <p>
         * Type: TEXT
         */
        public static final String LICENSE_AVAILABLE_TIME = "license_available_time";

        /**
         * The data stream for extended metadata.
         * <p>
         * Type: TEXT
         */
        public static final String EXTENDED_METADATA = "extended_metadata";
    }

    /**
     * Defines DRM object types.
     */
    public static class DrmObjectType {
        /**
         * An unknown object type.
         */
        public static final int UNKNOWN = 0x00;
        /**
         * A rights-protected file object type.
         */
        public static final int CONTENT = 0x01;
        /**
         * A rights information object type.
         */
        public static final int RIGHTS_OBJECT = 0x02;
        /**
         * A trigger information object type.
         */
        public static final int TRIGGER_OBJECT = 0x03;
    }

    /**
     * Defines playback states for content.
     */
    public static class Playback {
        /**
         * Playback started.
         */
        public static final int START = 0x00;
        /**
         * Playback stopped.
         */
        public static final int STOP = 0x01;
        /**
         * Playback paused.
         */
        public static final int PAUSE = 0x02;
        /**
         * Playback resumed.
         */
        public static final int RESUME = 0x03;

        /* package */ static boolean isValid(int playbackStatus) {
            boolean isValid = false;

            switch (playbackStatus) {
                case START:
                case STOP:
                case PAUSE:
                case RESUME:
                    isValid = true;
            }
            return isValid;
        }
    }

    /**
     * Defines actions that can be performed on rights-protected content.
     */
    public static class Action {
        /**
         * The default action.
         */
        public static final int DEFAULT = 0x00;
        /**
         * The rights-protected content can be played.
         */
        public static final int PLAY = 0x01;
        /**
         * The rights-protected content can be set as a ringtone.
         */
        public static final int RINGTONE = 0x02;
        /**
         * The rights-protected content can be transferred.
         */
        public static final int TRANSFER = 0x03;
        /**
         * The rights-protected content can be set as output.
         */
        public static final int OUTPUT = 0x04;
        /**
         * The rights-protected content can be previewed.
         */
        public static final int PREVIEW = 0x05;
        /**
         * The rights-protected content can be executed.
         */
        public static final int EXECUTE = 0x06;
        /**
         * The rights-protected content can be displayed.
         */
        public static final int DISPLAY = 0x07;

        /**
         * @hide
         */
        public static final int PRINT     = 0x08;
        /**
         * @hide
         */
        public static final int WALLPAPER = 0x09; // for FL only

        /* package */ static boolean isValid(int action) {
            boolean isValid = false;

            switch (action) {
                case DEFAULT:
                case PLAY:
                case RINGTONE:
                case TRANSFER:
                case OUTPUT:
                case PREVIEW:
                case EXECUTE:
                case DISPLAY:
                case PRINT:
                case WALLPAPER:
                    isValid = true;
            }
            return isValid;
        }
    }

    /**
     * Defines status notifications for digital rights.
     */
    public static class RightsStatus {
        /**
         * The digital rights are valid.
         */
        public static final int RIGHTS_VALID = 0x00;
        /**
         * The digital rights are invalid.
         */
        public static final int RIGHTS_INVALID = 0x01;
        /**
         * The digital rights have expired.
         */
        public static final int RIGHTS_EXPIRED = 0x02;
        /**
         * The digital rights have not been acquired for the rights-protected content.
         */
        public static final int RIGHTS_NOT_ACQUIRED = 0x03;
        /**
         * Constant field signifies that the secure timer is invalid
         * @hide
         */
        public static final int SECURE_TIMER_INVALID = 0x04;
    }

    /**
     * Defines media mime type prefix
     * @hide
     */
    public static class MimePrefix {
        /**
         * Constant field signifies that image prefix
         */
        public static final String IMAGE="image/";
        /**
         * Constant field signifies that audio prefix
         */
        public static final String AUDIO="audio/";
        /**
         * Constant field signifies that video prefix
         */
        public static final String VIDEO="video/";
    }

    /**
     * defines the string constants for retrieving metadata from dcf file.
     * @hide
     */
    public static class MetadataKey {
        public static final String META_KEY_IS_DRM = "is_drm";
        public static final String META_KEY_CONTENT_URI = "drm_content_uri";
        public static final String META_KEY_OFFSET = "drm_offset";
        public static final String META_KEY_DATALEN = "drm_dataLen";
        public static final String META_KEY_RIGHTS_ISSUER = "drm_rights_issuer";
        public static final String META_KEY_CONTENT_NAME = "drm_content_name";
        public static final String META_KEY_CONTENT_DESCRIPTION =
                "drm_content_description";
        public static final String META_KEY_CONTENT_VENDOR =
                "drm_content_vendor";
        public static final String META_KEY_ICON_URI = "drm_icon_uri";
        public static final String META_KEY_METHOD = "drm_method";
        public static final String META_KEY_MIME = "drm_mime_type";
    }

    /**
     * defines the string for Drm object mime type.
     * @hide
     */
    public static class DrmObjectMime {
        public static final String MIME_RIGHTS_XML =
                "application/vnd.oma.drm.rights+xml";
        public static final String MIME_RIGHTS_WBXML =
                "application/vnd.oma.drm.rights+wbxml";
        public static final String MIME_DRM_CONTENT =
                "application/vnd.oma.drm.content";
        public static final String MIME_DRM_MESSAGE =
                "application/vnd.oma.drm.message";
    }

    /**
     * defines the string for Drm Object file's suffix.
     * @hide
     */
    public static class DrmFileExt {
        public static final String EXT_RIGHTS_XML = ".dr";
        public static final String EXT_RIGHTS_WBXML = ".drc";
        public static final String EXT_DRM_CONTENT = ".dcf";
        public static final String EXT_DRM_MESSAGE = ".dm";
    }

    /**
     * defines the string for drm method.
     * Don't change without DrmDef.h
     * @hide
     */
    public static class DrmMethod {
        public static final int METHOD_NONE = 0;
        public static final int METHOD_FL = 1;
        public static final int METHOD_CD = 2;
        public static final int METHOD_SD = 4;
        public static final int METHOD_FLDCF = 8;
    }

    /**
     * defines the drm extra key & value.
     * @hide
     */
    public static class DrmExtra {
        public static final String EXTRA_DRM_LEVEL =
                "android.intent.extra.drm_level";
        public static final int DRM_LEVEL_FL = 1;
        public static final int DRM_LEVEL_SD = 2;
        public static final int DRM_LEVEL_ALL = 4;
    }

    /**
     * defines result code for NTP time synchronization
     * @hide
     */
    public static class NTPResult {
        public static final int NTP_SYNC_SUCCESS = 0;
        public static final int NTP_SYNC_NETWORK_TIMEOUT = -1;
        public static final int NTP_SYNC_SERVER_TIMEOUT = -2;
        public static final int NTP_SYNC_NETWORK_ERROR = -3;
    }
}

