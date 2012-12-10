
package com.mediatek.nfc.tag.write;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.TagTypePreference;
import com.mediatek.nfc.tag.record.ParsedNdefRecord;
import com.mediatek.nfc.tag.utils.Utils;

public class TagTypeList extends PreferenceActivity {
    public static final String TAG = "NfcTag/TagWriteNew";

    private boolean mShowAdvancedWritingOption = false;

    private PreferenceScreen mRoot = null;

    public static final int[] BASIC_SUPPORTED_TYPES = {
            Utils.TAG_TYPE_PHONE_NUM, Utils.TAG_TYPE_SMS, Utils.TAG_TYPE_URL,
            Utils.TAG_TYPE_VEVENT, Utils.TAG_TYPE_TEXT, Utils.TAG_TYPE_VCARD
    };

    /**
     * Just create another array for advanced writing options. Though there are
     * some waste to do it like this, but simple is beauty
     */
    public static final int[] ALL_SUPPORTED_TYPES = {
            Utils.TAG_TYPE_PHONE_NUM, Utils.TAG_TYPE_SMS, Utils.TAG_TYPE_URL,
            Utils.TAG_TYPE_VEVENT, Utils.TAG_TYPE_TEXT, Utils.TAG_TYPE_VCARD,
            // Advanced options
            Utils.TAG_TYPE_EMAIL, Utils.TAG_TYPE_APP, Utils.TAG_TYPE_PARAM
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.layout.tag_type_list);
        mRoot = getPreferenceScreen();
    }

    @Override
    protected void onResume() {
        mShowAdvancedWritingOption = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(Utils.KEY_SHOW_ADVANCED_WRITING,
                        Utils.DEFAULT_VALUE_SHOW_ADVANCED_WRITIN);
        new TagTypeLoadTask().execute((Void[]) null);
        super.onResume();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference instanceof TagTypePreference) {
            TagTypePreference pref = (TagTypePreference) preference;
            log("Tag type[" + pref.getTagType() + "] is clicked.");
            startTagEditor(pref.getTagType());
        } else {
            loge("Unknown tag type preference");
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void startTagEditor(int tagType) {
        log("-->startTagEditor(), tag type = " + tagType);
        Intent intent = new Intent(this, TagInfoEditorFrame.class);
        intent.putExtra("type", tagType);

        startActivity(intent);
        log("<--startTagEditor()");
    }

    /**
     * Class for generating Tag type preference for each tag
     */
    class TagTypeLoadTask extends AsyncTask<Void, Void, Preference[]> {
        @Override
        protected Preference[] doInBackground(Void... params) {
            int[] supportedTypes = BASIC_SUPPORTED_TYPES;
            if (mShowAdvancedWritingOption) {
                supportedTypes = ALL_SUPPORTED_TYPES;
            }
            int supportedTypeSize = supportedTypes.length;
            TagTypePreference[] prefs = new TagTypePreference[supportedTypeSize];
            ParsedNdefRecord record = null;
            for (int i = 0; i < supportedTypeSize; i++) {
                log("Add a new tag, type = " + supportedTypes[i]);
                record = ParsedNdefRecord.getRecordInstance(TagTypeList.this, supportedTypes[i]);

                if (record != null) {
                    prefs[i] = record.getTagTypePreference();
                } else {
                    loge("Fail to get tag type preference for type [" + supportedTypes[i] + "]");
                }
            }

            return prefs;
        }

        @Override
        protected void onPostExecute(Preference[] result) {
            mRoot.removeAll();
            for (Preference pref : result) {
                mRoot.addPreference(pref);
            }
        }
    }

    public void log(String msg) {
        Utils.logd(TAG, msg);
    }

    public void loge(String msg) {
        Utils.loge(TAG, msg);
    }
}
