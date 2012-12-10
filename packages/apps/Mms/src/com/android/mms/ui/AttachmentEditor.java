/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.mms.ui;

import com.android.mms.R;
import com.android.mms.data.WorkingMessage;
import com.android.mms.model.FileAttachmentModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
// a0
import com.android.mms.ExceedMessageSizeException;
import com.android.mms.model.MediaModel;
import com.android.mms.MmsConfig;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.mediatek.featureoption.FeatureOption;
import android.drm.DrmManagerClient;
import com.mediatek.xlog.Xlog;

import java.util.List;
// a1
/**
 * This is an embedded editor/view to add photos and sound/video clips
 * into a multimedia message.
 */
public class AttachmentEditor extends LinearLayout {
    private static final String TAG = "AttachmentEditor";

    static final int MSG_EDIT_SLIDESHOW   = 1;
    static final int MSG_SEND_SLIDESHOW   = 2;
    static final int MSG_PLAY_SLIDESHOW   = 3;
    static final int MSG_REPLACE_IMAGE    = 4;
    static final int MSG_REPLACE_VIDEO    = 5;
    static final int MSG_REPLACE_AUDIO    = 6;
    static final int MSG_PLAY_VIDEO       = 7;
    static final int MSG_PLAY_AUDIO       = 8;
    static final int MSG_VIEW_IMAGE       = 9;
    static final int MSG_REMOVE_ATTACHMENT = 10;

    private final Context mContext;
    private Handler mHandler;

    private SlideViewInterface mView;
    // add for vCard
    private View mFileAttachmentView;
    private SlideshowModel mSlideshow;
    private Presenter mPresenter;
    private boolean mCanSend;
    private Button mSendButton;

    public AttachmentEditor(Context context, AttributeSet attr) {
        super(context, attr);
        mContext = context;
    }

    /**
     * Returns true if the attachment editor has an attachment to show.
     */
    public boolean update(WorkingMessage msg) {
        hideView();
        mView = null;
        // add for vcard
        mFileAttachmentView = null;
// a0
        mWorkingMessage = msg;
// a1
        // If there's no attachment, we have nothing to do.
        if (!msg.hasAttachment()) {
            return false;
        }

        // Get the slideshow from the message.
        mSlideshow = msg.getSlideshow();
// e0
//        mView = createView();
        try {
            // for vcard: file attachment view and other views are exclusive to each other
            if (mSlideshow.sizeOfFilesAttach() > 0) {
                mFileAttachmentView = createFileAttachmentView(msg);
                if (mFileAttachmentView != null) {
                    mFileAttachmentView.setVisibility(View.VISIBLE);
                }
            }
        	mView = createView(msg);
        } catch(IllegalArgumentException e) {
        	return false;
        }
// e1

        if ((mPresenter == null) || !mSlideshow.equals(mPresenter.getModel())) {
            mPresenter = PresenterFactory.getPresenter(
                    "MmsThumbnailPresenter", mContext, mView, mSlideshow);
        } else {
            mPresenter.setView(mView);
        }

        if (mSlideshow.size() > 1) {
            mPresenter.present();
        } else if (mSlideshow.size() == 1) {
            SlideModel sm = mSlideshow.get(0);
            if (sm.hasAudio() || sm.hasImage() || sm.hasVideo()) {
                mPresenter.present();
            }
        }
        return true;
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public void setCanSend(boolean enable) {
        if (mCanSend != enable) {
            mCanSend = enable;
            updateSendButton();
        }
    }

    private void updateSendButton() {
        if (null != mSendButton) {
            mSendButton.setEnabled(mCanSend);
            mSendButton.setFocusable(mCanSend);
        }
    }

    public void hideView() {
        if (mView != null) {
            ((View)mView).setVisibility(View.GONE);
        }
        // add for vcard
        if (mFileAttachmentView != null) {
            mFileAttachmentView.setVisibility(View.GONE);
        }
    }

    private View getStubView(int stubId, int viewId) {
        View view = findViewById(viewId);
        if (view == null) {
            ViewStub stub = (ViewStub) findViewById(stubId);
            view = stub.inflate();
        }
        return view;
    }

    private class MessageOnClick implements OnClickListener {
        private int mWhat;

        public MessageOnClick(int what) {
            mWhat = what;
        }

        public void onClick(View v) {
            Message msg = Message.obtain(mHandler, mWhat);
            msg.sendToTarget();
            //if (mWhat == MSG_EDIT_SLIDESHOW) {
            //    v.setEnabled(false);
            //}
        }
    }
// mo
//    private SlideViewInterface createView() {
    private SlideViewInterface createView(WorkingMessage msg) {
// m1
        boolean inPortrait = inPortraitMode();
        if (mSlideshow.size() > 1) {
// m0
//            return createSlideshowView(inPortrait);
            return createSlideshowView(inPortrait, msg);
// m1
        }

        SlideModel slide = mSlideshow.get(0);
        /// M: before using SlideModel's function,we should make sure it is
        // null or not
        if(null == slide) {
        	throw new IllegalArgumentException();
        }
        if (slide.hasImage()) {
            return createMediaView(
                    R.id.image_attachment_view_stub,
                    R.id.image_attachment_view,
                    R.id.view_image_button, R.id.replace_image_button, R.id.remove_image_button,
// m0
//                    MSG_VIEW_IMAGE, MSG_REPLACE_IMAGE, MSG_REMOVE_ATTACHMENT);
                    R.id.media_size_info, msg.getCurrentMessageSize(),
                        MSG_VIEW_IMAGE, MSG_REPLACE_IMAGE, MSG_REMOVE_ATTACHMENT, msg);           
// m1
        } else if (slide.hasVideo()) {
            return createMediaView(
                    R.id.video_attachment_view_stub,
                    R.id.video_attachment_view,
                    R.id.view_video_button, R.id.replace_video_button, R.id.remove_video_button,
// m0
//                    MSG_PLAY_VIDEO, MSG_REPLACE_VIDEO, MSG_REMOVE_ATTACHMENT);
                    R.id.media_size_info, msg.getCurrentMessageSize(),
                        MSG_PLAY_VIDEO, MSG_REPLACE_VIDEO, MSG_REMOVE_ATTACHMENT, msg);	            
// m1
        } else if (slide.hasAudio()) {
            return createMediaView(
                    R.id.audio_attachment_view_stub,
                    R.id.audio_attachment_view,
                    R.id.play_audio_button, R.id.replace_audio_button, R.id.remove_audio_button,
// mo
//                    MSG_PLAY_AUDIO, MSG_REPLACE_AUDIO, MSG_REMOVE_ATTACHMENT);
                    R.id.media_size_info, msg.getCurrentMessageSize(),
                        MSG_PLAY_AUDIO, MSG_REPLACE_AUDIO, MSG_REMOVE_ATTACHMENT, msg);	
// m1
        } else {
            throw new IllegalArgumentException();
        }
    }

    // add for vcard
    private View createFileAttachmentView(WorkingMessage msg) {
        List<FileAttachmentModel> attachFiles = mSlideshow.getAttachFiles();
        if (attachFiles == null || attachFiles.size() != 1) {
            Log.e(TAG, "createFileAttachmentView, oops no attach files found.");
            return null;
        }
        FileAttachmentModel attach = attachFiles.get(0);
        Log.i(TAG, "createFileAttachmentView, attach " + attach.toString());
        final View view = getStubView(R.id.file_attachment_view_stub, R.id.file_attachment_view);
        view.setVisibility(View.VISIBLE);
        final ImageView thumb = (ImageView) view.findViewById(R.id.file_attachment_thumbnail);
        final TextView name = (TextView) view.findViewById(R.id.file_attachment_name_info);
        String nameText = null;
        int thumbResId = -1;
        if (attach.isVCard()) {
            nameText = mContext.getString(R.string.file_attachment_vcard_name, attach.getSrc());
            thumbResId = R.drawable.ic_vcard_attach;
        } else if (attach.isVCalendar()) {
            nameText = mContext.getString(R.string.file_attachment_vcalendar_name, attach.getSrc());
            thumbResId = R.drawable.ic_vcalendar_attach;
        }
        name.setText(nameText);
        thumb.setImageResource(thumbResId);
        final TextView size = (TextView) view.findViewById(R.id.file_attachment_size_info);
        size.setText(MessageUtils.getHumanReadableSize(attach.getAttachSize())
                +"/"+MmsConfig.getUserSetMmsSizeLimit(false) + "K");
        final ImageView remove = (ImageView) view.findViewById(R.id.file_attachment_button_remove);
        final ImageView divider = (ImageView) view.findViewById(R.id.file_attachment_divider);
        divider.setVisibility(View.VISIBLE);
        remove.setVisibility(View.VISIBLE);
        remove.setOnClickListener(new MessageOnClick(MSG_REMOVE_ATTACHMENT));
        return view;
    }

    /**
     * What is the current orientation?
     */
    private boolean inPortraitMode() {
        final Configuration configuration = mContext.getResources().getConfiguration();
        return configuration.orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    private SlideViewInterface createMediaView(
            int stub_view_id, int real_view_id,
            int view_button_id, int replace_button_id, int remove_button_id,
// m0
//            int view_message, int replace_message, int remove_message) {
            int size_view_id, int msgSize,
            int view_message, int replace_message, int remove_message, WorkingMessage msg) {
// m1
        LinearLayout view = (LinearLayout)getStubView(stub_view_id, real_view_id);
        view.setVisibility(View.VISIBLE);

        Button viewButton = (Button) view.findViewById(view_button_id);
        Button replaceButton = (Button) view.findViewById(replace_button_id);
        Button removeButton = (Button) view.findViewById(remove_button_id);

// a0
        // show Mms Size  
        mMediaSize = (TextView) view.findViewById(size_view_id); 
        int sizeShow = (msgSize - 1)/1024 + 1;
        String info = sizeShow + "K/" + MmsConfig.getUserSetMmsSizeLimit(false) + "K";
        mMediaSize.setText(info); 
// a1

        viewButton.setOnClickListener(new MessageOnClick(view_message));
        replaceButton.setOnClickListener(new MessageOnClick(replace_message));
        removeButton.setOnClickListener(new MessageOnClick(remove_message));

// a0
        if (mFlagMini) {
        	replaceButton.setVisibility(View.GONE);
        }
// a1
        return (SlideViewInterface) view;
    }

// m0
//    private SlideViewInterface createSlideshowView(boolean inPortrait) {
    private SlideViewInterface createSlideshowView(boolean inPortrait, WorkingMessage msg) {
// m1
        LinearLayout view =(LinearLayout) getStubView(
                R.id.slideshow_attachment_view_stub,
                R.id.slideshow_attachment_view);
        view.setVisibility(View.VISIBLE);

        Button editBtn = (Button) view.findViewById(R.id.edit_slideshow_button);
        mSendButton = (Button) view.findViewById(R.id.send_slideshow_button);
// a0
        mSendButton.setOnClickListener(new MessageOnClick(MSG_SEND_SLIDESHOW));
// a1

        updateSendButton();
        final ImageButton playBtn = (ImageButton) view.findViewById(
                R.id.play_slideshow_button);
// a0
    	if (FeatureOption.MTK_DRM_APP) {
    		if (msg.mHasDrmPart) {
    			Xlog.i(TAG, "mHasDrmPart");
        	    Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.mms_play_btn);        
        	    Drawable front = mContext.getResources().getDrawable(com.mediatek.internal.R.drawable.drm_red_lock);
        	    DrmManagerClient drmManager= new DrmManagerClient(mContext);
        	    Bitmap drmBitmap = drmManager.overlayBitmap(bitmap, front);
        	    playBtn.setImageBitmap(drmBitmap);
        	    if (bitmap != null && !bitmap.isRecycled()) {
        	    	bitmap.recycle();
        	    	bitmap = null;
        	    }
    		}
    	}

        // show Mms Size  
        mMediaSize = (TextView) view.findViewById(R.id.media_size_info); 
		int sizeShow = (msg.getCurrentMessageSize() - 1)/1024 + 1;
        String info = sizeShow + "K/" + MmsConfig.getUserSetMmsSizeLimit(false) + "K";
        
        mMediaSize.setText(info);
// a1

        editBtn.setEnabled(true);
        editBtn.setOnClickListener(new MessageOnClick(MSG_EDIT_SLIDESHOW));
        mSendButton.setOnClickListener(new MessageOnClick(MSG_SEND_SLIDESHOW));
        playBtn.setOnClickListener(new MessageOnClick(MSG_PLAY_SLIDESHOW));

        Button removeButton = (Button) view.findViewById(R.id.remove_slideshow_button);
        removeButton.setOnClickListener(new MessageOnClick(MSG_REMOVE_ATTACHMENT));

        return (SlideViewInterface) view;
    }

// a0
    private WorkingMessage mWorkingMessage;
    private boolean mTextIncludedInMms;
    private TextView mMediaSize;
    private ImageView mDrmLock;
    private boolean mFlagMini = false;

    public void update(WorkingMessage msg, boolean isMini) {
    	mFlagMini = isMini;
    	update(msg);
    }

    public void onTextChangeForOneSlide(CharSequence s) throws ExceedMessageSizeException {

    	if (null == mMediaSize || (mWorkingMessage.hasSlideshow() && mWorkingMessage.getSlideshow().size() >1)) {
    	    return;
    	}

    	// borrow this method to get the encoding type
	    int[] params = SmsMessage.calculateLength(s, false);
	    int type = params[3];
	    int totalSize = 0;
	    if (mWorkingMessage.hasAttachment()) {
	        totalSize = mWorkingMessage.getCurrentMessageSize();
	    }
    	// show
		int sizeShow = (totalSize - 1)/1024 + 1;
		String info = sizeShow + "K/" + MmsConfig.getUserSetMmsSizeLimit(false) + "K";
        mMediaSize.setText(info);
    }
// a1
}
