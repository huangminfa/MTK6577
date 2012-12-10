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

package com.android.mms.model;


import com.android.mms.ContentRestrictionException;
import com.android.mms.ExceedMessageSizeException;
import com.android.mms.LogTag;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.UnsupportContentTypeException;
import com.android.mms.dom.smil.parser.SmilXmlSerializer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import android.drm.mobile1.DrmException;
import com.android.mms.drm.DrmWrapper;
import java.util.Comparator;
import com.android.mms.layout.LayoutManager;
import com.google.android.mms.ContentType;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.MultimediaMessagePdu;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.CharacterSets;

import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.smil.SMILDocument;
import org.w3c.dom.smil.SMILElement;
import org.w3c.dom.smil.SMILLayoutElement;
import org.w3c.dom.smil.SMILMediaElement;
import org.w3c.dom.smil.SMILParElement;
import org.w3c.dom.smil.SMILRegionElement;
import org.w3c.dom.smil.SMILRootLayoutElement;

import android.content.ContentUris;
import android.content.ContentResolver;
import android.content.Context;
import android.drm.mobile1.DrmException;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import android.view.Display;
import android.view.WindowManager;
import android.content.res.Configuration;
import android.drm.DrmManagerClient;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.xlog.Xlog;

public class SlideshowModel extends Model
        implements List<SlideModel>, IModelChangedObserver {
    private static final String TAG = "Mms/slideshow";

    private final LayoutModel mLayout;
    private final ArrayList<SlideModel> mSlides;
    // add for vcard
    private final ArrayList<FileAttachmentModel> mAttachFiles;
    private SMILDocument mDocumentCache;
    private PduBody mPduBodyCache;
    private Context mContext;
    private static boolean mHasDrmContent;
    private static boolean mHasDrmRight;
    private static final float DEFAULT_DUR_SEC = 5.0F;
    // amount of space to leave in a slideshow for text and overhead.
    public static final int SLIDESHOW_SLOP = 1024;
    private static final int MMS_HEADER_SIZE                  = 128;
    private static final int MMS_CONTENT_TYPE_HEAER_LENGTH    = 128; 
    private static final int SMILE_HEADER_SIZE                = 128;
    private int mCurrentSlideshowSize = MMS_HEADER_SIZE + MMS_CONTENT_TYPE_HEAER_LENGTH 
                                        + SMILE_HEADER_SIZE + SLIDESHOW_SLOP;
    public static final int MMS_SLIDESHOW_INIT_SIZE = MMS_HEADER_SIZE + MMS_CONTENT_TYPE_HEAER_LENGTH
        + SMILE_HEADER_SIZE + SLIDESHOW_SLOP + MediaModel.SMILE_TAG_SIZE_ATTACH + MediaModel.SMILE_TAG_SIZE_PAGE
        + MediaModel.SMILE_TAG_SIZE_IMAGE;

    public static final int mReserveSize = MMS_HEADER_SIZE + MMS_CONTENT_TYPE_HEAER_LENGTH
                                            + SMILE_HEADER_SIZE + SLIDESHOW_SLOP;

    public static final String VCARD = "BEGIN:VCARD";
    public static final String VCALENDAR = "BEGIN:VCALENDAR";
    public static final String VCARD_DESCRIPTION = "vcf.vcf";
    public static final String VCALENDAR_DESCRIPTION = "vcs.vcs";

    public boolean checkDrmContent() {
    	return mHasDrmContent;
    }
    
    public boolean checkDrmRight() {
    	return mHasDrmRight;
    }
    
    public void setDrmContentFlag(boolean hasDrmContent) {
    	mHasDrmContent = hasDrmContent;
    }
    
    public void setDrmRightFlag(boolean hasDrmRight) {
    	mHasDrmRight = hasDrmRight;
    }
    private SlideshowModel(Context context) {
        mLayout = new LayoutModel();
        mSlides = new ArrayList<SlideModel>();
        // add for vcard
        mAttachFiles = new ArrayList<FileAttachmentModel>();
        mContext = context;
    }

    private SlideshowModel (
            LayoutModel layouts, ArrayList<SlideModel> slides,
            ArrayList<FileAttachmentModel> attachFiles,
            SMILDocument documentCache, PduBody pbCache,
            Context context) {
        Xlog.d(TAG, "SlideshowModel.init");
        mLayout = layouts;
        mSlides = slides;
        mAttachFiles = attachFiles;
        mContext = context;
        mDocumentCache = documentCache;
        mPduBodyCache = pbCache;
        for (SlideModel slide : mSlides) {
            increaseSlideshowSize(slide.getSlideSize());
            slide.setParent(this);
        }
    }

    public static SlideshowModel createNew(Context context) {
        return new SlideshowModel(context);
    }

    public static SlideshowModel createFromMessageUri(
            Context context, Uri uri) throws MmsException {
        return createFromPduBody(context, getPduBody(context, uri));
    }

    public static SlideshowModel createFromPduBody(Context context, PduBody pb) throws MmsException {
    	int partNum = pb.getPartsNum();
        for(int i = 0; i < partNum; i++) {
            PduPart part = pb.getPart(i);
            byte[] cl = part.getContentLocation();
            String path = null ;
            if (cl != null) {
            	path = new String(cl);
            }
            Xlog.i(TAG, "SlideshowModel path:" + path);
            mHasDrmContent = false;
            if (FeatureOption.MTK_DRM_APP) {
                if (path != null) {
                    String extName = path.substring(path.lastIndexOf('.') + 1);
                    if (extName.equals("dcf")) {
                	    Xlog.i(TAG, "SlideshowModel Has DrmContent") ;
                	    mHasDrmContent = true;
                    }
                }
            }
        }
    	
        SMILDocument document = SmilHelper.getDocument(pb);

        // Create root-layout model.
        SMILLayoutElement sle = document.getLayout();
        SMILRootLayoutElement srle = sle.getRootLayout();
       // int w = srle.getWidth();
       // int h = srle.getHeight();
        WindowManager windowM = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Configuration config = context.getResources().getConfiguration();
        Display defDisplay = windowM.getDefaultDisplay();
        int w = 0;
        int h = 0;
        if( config.orientation == Configuration.ORIENTATION_PORTRAIT){
            w = defDisplay.getWidth();
            h = defDisplay.getHeight();
        }else{
            w = defDisplay.getHeight();
            h = defDisplay.getWidth();
        }
        if ((w == 0) || (h == 0)) {
            w = LayoutManager.getInstance().getLayoutParameters().getWidth();
            h = LayoutManager.getInstance().getLayoutParameters().getHeight();
            srle.setWidth(w);
            srle.setHeight(h);
        }
        RegionModel rootLayout = new RegionModel(
                null, 0, 0, w, h);

        // Create region models.
        ArrayList<RegionModel> regions = new ArrayList<RegionModel>();
        NodeList nlRegions = sle.getRegions();
        int regionsNum = nlRegions.getLength();
        SMILRegionElement[] smils = new SMILRegionElement[regionsNum];
        for (int i = 0;i<regionsNum;i++) {
        	smils[i] = (SMILRegionElement) nlRegions.item(i);
        }
        Arrays.sort(smils,new Comparator<SMILRegionElement>(){

			public int compare(SMILRegionElement object1,
					SMILRegionElement object2) {
				if (object1.getTop() < object2.getTop()) {
					return -1;
				} else if(object1.getTop() > object2.getTop()) {
					return 1;
				}
				return 0;
			}});
        int itemHeight = 0;
        if (regionsNum != 0) {
            itemHeight = h / regionsNum;
        }
        Xlog.d(TAG, "SlideshowModel.createFromPduBody(): RootLayout.Width=" + srle.getWidth()
                + ", RootLayout.Height=" + srle.getHeight() + ", Windows.w=" + w + ", Windows.h=" + h);

        for (int i = 0; i < regionsNum; i++) {
            SMILRegionElement sre = smils[i];
            RegionModel r;
            if (regionsNum == 1) {
    	    	r = new RegionModel(sre.getId(), sre.getFit(), 0, sre.getTop(), w, itemHeight, sre.getBackgroundColor());
    	    } else {
    	        int left = 0;
    	        int width = 0;
    	        int top = 0;
    	        int height = 0;
    	        if (srle.getWidth() != 0) {
                    left = (defDisplay.getWidth() * sre.getLeft()) / srle.getWidth();
                    width = (defDisplay.getWidth() * sre.getWidth()) / srle.getWidth();
    	        } else {
    	            left = 0;
    	            width = w;
    	        }
    	        if (srle.getHeight() != 0) {
                    top = (defDisplay.getHeight() * sre.getTop()) / srle.getHeight();
                    height = (defDisplay.getHeight() * sre.getHeight()) / srle.getHeight();
    	        } else {
    	            top = sre.getTop();
    	            height = itemHeight;
    	        }
                r = new RegionModel(sre.getId(), sre.getFit(), left, top, width, height, sre.getBackgroundColor());
                Xlog.d(TAG, "SlideshowModel.createFromPduBody(): " + r.toString());
            }
            
            regions.add(r);
        }
        LayoutModel layouts = new LayoutModel(rootLayout, regions);

        // Create slide models.
        SMILElement docBody = document.getBody();
        NodeList slideNodes = docBody.getChildNodes();
        int slidesNum = slideNodes.getLength();
        ArrayList<SlideModel> slides = new ArrayList<SlideModel>(slidesNum);
        Xlog.i(TAG, "SlideshowModel slidesNum:" + slidesNum);
        for (int i = 0; i < slidesNum; i++) {
            // FIXME: This is NOT compatible with the SMILDocument which is
            // generated by some other mobile phones.
            SMILParElement par = null;
            try { 
                par = (SMILParElement) slideNodes.item(i);
            } catch (ClassCastException cce) {
                Xlog.e(TAG, cce.getMessage());
                continue;
            }

            // Create media models for each slide.
            NodeList mediaNodes = par.getChildNodes();
            int mediaNum = mediaNodes.getLength();
            ArrayList<MediaModel> mediaSet = new ArrayList<MediaModel>(mediaNum);

            for (int j = 0; j < mediaNum; j++) {
                SMILMediaElement sme = null;
                try { 
                    sme = (SMILMediaElement) mediaNodes.item(j);
                } catch (ClassCastException cce) {
                    Xlog.e(TAG, cce.getMessage());
                    continue;
                }
                try {
                    MediaModel media = MediaModelFactory.getMediaModel(
                            context, sme, layouts, pb);

                    /*
                    * This is for slide duration value set.
                    * If mms server does not support slide duration.
                    */
                    Xlog.i(TAG, "MmsConfig.getSlideDurationEnabled(): " + MmsConfig.getSlideDurationEnabled());
                    if (MmsConfig.getSlideDurationEnabled()) {
                        int mediadur = media.getDuration();
                        Xlog.i(TAG, "media.getDuration(): " + media.getDuration());
                        float dur = par.getDur();
                        Xlog.i(TAG, "dur = par.getDur():" + dur);
                        if (dur == 0) {
                            mediadur = MmsConfig.getMinimumSlideElementDuration() * 1000;
                            media.setDuration(mediadur);
                        }

                        if ((int)mediadur / 1000 != dur) {
                        	Xlog.i(TAG, "mediadur / 1000 != dur");
                            String tag = sme.getTagName();

                            if (ContentType.isVideoType(media.mContentType)
                              || tag.equals(SmilHelper.ELEMENT_TAG_VIDEO)
                              || ContentType.isAudioType(media.mContentType)
                              || tag.equals(SmilHelper.ELEMENT_TAG_AUDIO)) {
                            	// if need according media file duration , use like this :
                                //par.setDur((float)mediadur / 1000);
                            	
                            	// if need according slide setting duration , use like this:
                                media.setDuration((int)dur * 1000);
                            	Xlog.i(TAG, "par.setDur:" + dur);
                            } else {
                                /*
                                * If a slide has an image and an audio/video element
                                * and the audio/video element has longer duration than the image,
                                * The Image disappear before the slide play done. so have to match
                                * an image duration to the slide duration.
                                */
                                if ((int)mediadur / 1000 < dur) {
                                    media.setDuration((int)dur * 1000);
                                    Xlog.i(TAG, "media.setDuration:" + (int)dur * 1000);
                                } else {
                                    if ((int)dur != 0) {
                                        media.setDuration((int)dur * 1000);
                                        Xlog.i(TAG, "media.setDuration:" + (int)dur * 1000);
                                    } else {
                                        par.setDur((float)mediadur / 1000);
                                        Xlog.i(TAG, "media.setDuration:" + (float)mediadur / 1000);
                                    }
                                }
                            }
                        }
                    }
                    SmilHelper.addMediaElementEventListeners(
                            (EventTarget) sme, media);
                    mediaSet.add(media);
                } catch (DrmException e) {
                    Log.e(TAG, e.getMessage(), e);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, e.getMessage(), e);
                } catch (UnsupportContentTypeException e) {
                    Xlog.e(TAG, e.getMessage(), e);
                    continue;
                }
                }
            try {
                float durSec = par.getDur();
                if (durSec <= 0) {
                    par.setDur(DEFAULT_DUR_SEC);
            }

            SlideModel slide = new SlideModel((int) (par.getDur() * 1000), mediaSet);
            slide.setFill(par.getFill());
            SmilHelper.addParElementEventListeners((EventTarget) par, slide);
            slides.add(slide);
            } catch (ClassCastException cce) {
                Xlog.e(TAG, cce.getMessage());
            }
        }
        
        // get file attachments(vcard) from PduBody for vCard
        ArrayList<FileAttachmentModel> attachFiles = new ArrayList<FileAttachmentModel>();
        for (int i = 0; i < partNum; i++) {
            PduPart part = pb.getPart(i);
            byte[] cl = part.getContentLocation();
            byte[] name = part.getName();
            byte[] ci = part.getContentId();
            byte[] fn = part.getFilename();
            byte[] data = part.getData();
            String filename = null;
            if (cl != null) {
            	filename = new String(cl);
            } else if (name != null){
            	filename = new String(name);
            } else if (ci != null){
            	filename = new String(ci);
            } else if (fn != null){
            	filename = new String(fn);
            } else {
            	continue;
            }
             
            final String type = new String(part.getContentType());
            if (FileAttachmentModel.isVCard(part)) {
                FileAttachmentModel fam = new VCardModel(context, ContentType.TEXT_VCARD,
                        filename, part.getDataUri());
                attachFiles.add(fam);
            } else if (FileAttachmentModel.isVCalendar(part)) {
                FileAttachmentModel fam = new VCalendarModel(context, ContentType.TEXT_VCALENDAR,
                        filename, part.getDataUri());
                attachFiles.add(fam);
            } else if (data != null) {
                filename = new String(data);
                if (filename.startsWith(VCARD)) {
                    FileAttachmentModel fam = new VCalendarModel(context,
                            ContentType.TEXT_VCARD, VCARD_DESCRIPTION,
                            part.getDataUri());
                    attachFiles.add(fam);
                } else if (filename.startsWith(VCALENDAR)) {
                    FileAttachmentModel fam = new VCalendarModel(context,
                            ContentType.TEXT_VCALENDAR, VCALENDAR_DESCRIPTION,
                            part.getDataUri());
                    attachFiles.add(fam);
                }
            }
        }

        SlideshowModel slideshow = new SlideshowModel(layouts, slides, attachFiles, document, pb, context);
        slideshow.registerModelChangedObserver(slideshow);
        return slideshow;
    }

    public PduBody toPduBody() {
        if (mPduBodyCache == null) {
            mDocumentCache = SmilHelper.getDocument(this);
            mPduBodyCache = makePduBody(mDocumentCache);
        }
        return mPduBodyCache;
    }

    private PduBody makePduBody(SMILDocument document) {
        return makePduBody(null, document, false);
    }

    private PduBody makePduBody(Context context, SMILDocument document, boolean isMakingCopy) {
        PduBody pb = new PduBody();

        boolean hasForwardLock = false;
        for (int i = 0; i < mSlides.size(); i++) {
            for (MediaModel media : mSlides.get(i)) {
                if (isMakingCopy) {
                    if (media.isDrmProtected() && !media.isAllowedToForward()) {
                        hasForwardLock = true;
                        continue;
                    }
                }

                PduPart part = new PduPart();

                if (media.isText()) {
                    TextModel text = (TextModel) media;
                    // Don't create empty text part.
                    if (TextUtils.isEmpty(text.getText())) {
                        continue;
                    }
                    // Set Charset if it's a text media.
                    part.setCharset(text.getCharset());
                }

                // Set Content-Type.
                part.setContentType(media.getContentType().getBytes());

                String src = media.getSrc();
                String location;
                boolean startWithContentId = src.startsWith("cid:");
                if (startWithContentId) {
                    location = src.substring("cid:".length());
                } else {
                    location = src;
                }

                // Set Content-Location.
                part.setContentLocation(location.getBytes());

                // Set Content-Id.
                if (startWithContentId) {
                    //Keep the original Content-Id.
                    part.setContentId(location.getBytes());
                }
                else {
                    int index = location.lastIndexOf(".");
                    String contentId = (index == -1) ? location
                            : location.substring(0, index);
                    part.setContentId(contentId.getBytes());
                }

                if (media.isDrmProtected()) {
                    DrmWrapper wrapper = media.getDrmObject();
                    part.setDataUri(wrapper.getOriginalUri());
                    part.setData(wrapper.getOriginalData());
                } else if (media.isText()) {
                    try {
                        String charsetName = CharacterSets.getMimeName(part.getCharset());
                        part.setData(((TextModel) media).getText().getBytes(charsetName));
                    } catch (UnsupportedEncodingException e) {
                        Xlog.e(TAG, "Unsupported encoding: ", e);
                        part.setData(((TextModel) media).getText().getBytes());
                    }
                } else if (media.isImage() || media.isVideo() || media.isAudio()) {
                    part.setDataUri(media.getUri());
                } else {
                    Log.w(TAG, "Unsupport media: " + media);
                }

                pb.addPart(part);
            }
        }

        if (hasForwardLock && isMakingCopy && context != null) {
        	/*
            Toast.makeText(context,
                    context.getString(R.string.cannot_forward_drm_obj),
                    Toast.LENGTH_LONG).show();
                    */
            document = SmilHelper.getDocument(pb);
        }

        // Create and insert SMIL part(as the first part) into the PduBody.
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SmilXmlSerializer.serialize(document, out);
        PduPart smilPart = new PduPart();
        smilPart.setContentId("smil".getBytes());
        smilPart.setContentLocation("smil.xml".getBytes());
        smilPart.setContentType(ContentType.APP_SMIL.getBytes());
        smilPart.setData(out.toByteArray());
        pb.addPart(0, smilPart);

        // Add file attachment(vcard) into PduBody
        for (FileAttachmentModel fileAttachment : mAttachFiles) {
            PduPart part = new PduPart();

            part.setContentType(fileAttachment.getContentType().toLowerCase().getBytes());

            String src = fileAttachment.getSrc();
            String location;
            boolean startWithContentId = src.startsWith("cid:");
            if (startWithContentId) {
                location = src.substring("cid:".length());
            } else {
                location = src;
            }

            part.setContentLocation(location.getBytes());

            if (startWithContentId) {
                part.setContentId(location.getBytes());
            } else {
                int index = location.lastIndexOf(".");
                String contentId = (index == -1) ? location
                        : location.substring(0, index);
                part.setContentId(contentId.getBytes());
            }
            part.setName(fileAttachment.getSrc().getBytes());
            part.setFilename(fileAttachment.getSrc().getBytes());

            if (fileAttachment.isSupportedFile()) {
                part.setDataUri(fileAttachment.getUri());
            } else {
                Xlog.w(TAG, "Unsupport file attachment: " + fileAttachment);
            }

            pb.addPart(part);
        }
        return pb;
    }

    public PduBody makeCopy(Context context) {
        return makePduBody(context, SmilHelper.getDocument(this), true);
    }

    public SMILDocument toSmilDocument() {
        if (mDocumentCache == null) {
            mDocumentCache = SmilHelper.getDocument(this);
        }
        return mDocumentCache;
    }

    public static PduBody getPduBody(Context context, Uri msg) throws MmsException {
        PduPersister p = PduPersister.getPduPersister(context);
        GenericPdu pdu = p.load(msg);

        int msgType = pdu.getMessageType();
        if ((msgType == PduHeaders.MESSAGE_TYPE_SEND_REQ)
                || (msgType == PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF)) {
            return ((MultimediaMessagePdu) pdu).getBody();
        } else {
            throw new MmsException();
        }
    }

    public void setCurrentSlideshowSize(int size) {
    	mCurrentSlideshowSize = size;
    }

    // getCurrentMessageSize returns the size of the message, not including resizable attachments
    // such as photos. mCurrentMessageSize is used when adding/deleting/replacing non-resizable
    // attachments (movies, sounds, etc) in order to compute how much size is left in the message.
    // The difference between mCurrentMessageSize and the maxSize allowed for a message is then
    // divided up between the remaining resizable attachments. While this function is public,
    // it is only used internally between various MMS classes. If the UI wants to know the
    // size of a MMS message, it should call getTotalMessageSize() instead.
    public int getCurrentSlideshowSize() {
        return mCurrentSlideshowSize;
    }

    public void increaseSlideshowSize(int increaseSize) {
        if (increaseSize > 0) {
        	mCurrentSlideshowSize += increaseSize;
        }
    }

    public void decreaseSlideshowSize(int decreaseSize) {
        if (decreaseSize > 0) {
        	mCurrentSlideshowSize -= decreaseSize;
        }
    }
    public void resetSlideshowSize() {
    	mCurrentSlideshowSize = MMS_HEADER_SIZE + MMS_CONTENT_TYPE_HEAER_LENGTH 
    	                        + SMILE_HEADER_SIZE + SLIDESHOW_SLOP;
    }
    public LayoutModel getLayout() {
        return mLayout;
    }

    //
    // Implement List<E> interface.
    //
    public boolean add(SlideModel object) {
        int increaseSize = object.getSlideSize();
        checkMessageSize(increaseSize);

        if ((object != null) && mSlides.add(object)) {
        	increaseSlideshowSize(increaseSize);
            object.registerModelChangedObserver(this);
            for (IModelChangedObserver observer : mModelChangedObservers) {
                object.registerModelChangedObserver(observer);
            }
            notifyModelChanged(true);
            return true;
        }
        return false;
    }

    public boolean addAll(Collection<? extends SlideModel> collection) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    public void clear() {
        if (mSlides.size() > 0) {
            for (SlideModel slide : mSlides) {
                slide.unregisterModelChangedObserver(this);
                for (IModelChangedObserver observer : mModelChangedObservers) {
                    slide.unregisterModelChangedObserver(observer);
                }
            }
            resetSlideshowSize();
            mSlides.clear();
            notifyModelChanged(true);
        }
    }

    public boolean contains(Object object) {
        return mSlides.contains(object);
    }

    public boolean containsAll(Collection<?> collection) {
        return mSlides.containsAll(collection);
    }

    public boolean isEmpty() {
        return mSlides.isEmpty();
    }

    public Iterator<SlideModel> iterator() {
        return mSlides.iterator();
    }

    public boolean remove(Object object) {
        if ((object != null) && mSlides.remove(object)) {
            SlideModel slide = (SlideModel) object;
            decreaseSlideshowSize(slide.getSlideSize());
            slide.unregisterAllModelChangedObservers();
            notifyModelChanged(true);
            return true;
        }
        return false;
    }

    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    public int size() {
        return mSlides == null ? 0 : mSlides.size();
    }

    public Object[] toArray() {
        return mSlides.toArray();
    }

    public <T> T[] toArray(T[] array) {
        return mSlides.toArray(array);
    }

    public void add(int location, SlideModel object) {
        if (object != null) {
            int increaseSize = object.getSlideSize();
            checkMessageSize(increaseSize);

            mSlides.add(location, object);
            increaseSlideshowSize(increaseSize);
            object.registerModelChangedObserver(this);
            for (IModelChangedObserver observer : mModelChangedObservers) {
                object.registerModelChangedObserver(observer);
            }
            notifyModelChanged(true);
        }
    }
    // only for SlideshowEditActivity to swap slides. 
    // In this case, We don't check the message size
    public void addNoCheckSize(int location, SlideModel object) {
        if (object != null) {
            mSlides.add(location, object);
            increaseSlideshowSize(object.getSlideSize());
            object.registerModelChangedObserver(this);
            for (IModelChangedObserver observer : mModelChangedObservers) {
                object.registerModelChangedObserver(observer);
            }
            notifyModelChanged(true);
        }
    }
    public boolean addAll(int location,
            Collection<? extends SlideModel> collection) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    public SlideModel get(int location) {
        return (location >= 0 && location < mSlides.size()) ? mSlides.get(location) : null;
    }

    public int indexOf(Object object) {
        return mSlides.indexOf(object);
    }

    public int lastIndexOf(Object object) {
        return mSlides.lastIndexOf(object);
    }

    public ListIterator<SlideModel> listIterator() {
        return mSlides.listIterator();
    }

    public ListIterator<SlideModel> listIterator(int location) {
        return mSlides.listIterator(location);
    }

    public SlideModel remove(int location) {
        SlideModel slide = mSlides.remove(location);
        if (slide != null) {
        	decreaseSlideshowSize(slide.getSlideSize());
            slide.unregisterAllModelChangedObservers();
            notifyModelChanged(true);
        }
        return slide;
    }

    public SlideModel set(int location, SlideModel object) {
        SlideModel slide = mSlides.get(location);
        if (null != object) {
            int removeSize = 0;
            int addSize = object.getSlideSize();
            if (null != slide) {
                removeSize = slide.getSlideSize();
            }
            if (addSize > removeSize) {
                checkMessageSize(addSize - removeSize);
                increaseSlideshowSize(addSize - removeSize);
            } else {
            	decreaseSlideshowSize(removeSize - addSize);
            }
        }

        slide =  mSlides.set(location, object);
        if (slide != null) {
            slide.unregisterAllModelChangedObservers();
        }

        if (object != null) {
            object.registerModelChangedObserver(this);
            for (IModelChangedObserver observer : mModelChangedObservers) {
                object.registerModelChangedObserver(observer);
            }
        }

        notifyModelChanged(true);
        return slide;
    }

    public List<SlideModel> subList(int start, int end) {
        return mSlides.subList(start, end);
    }

    @Override
    protected void registerModelChangedObserverInDescendants(
            IModelChangedObserver observer) {
        mLayout.registerModelChangedObserver(observer);

        for (SlideModel slide : mSlides) {
            slide.registerModelChangedObserver(observer);
        }
    }

    @Override
    protected void unregisterModelChangedObserverInDescendants(
            IModelChangedObserver observer) {
        mLayout.unregisterModelChangedObserver(observer);

        for (SlideModel slide : mSlides) {
            slide.unregisterModelChangedObserver(observer);
        }
    }

    @Override
    protected void unregisterAllModelChangedObserversInDescendants() {
        mLayout.unregisterAllModelChangedObservers();

        for (SlideModel slide : mSlides) {
            slide.unregisterAllModelChangedObservers();
        }
    }

    public void onModelChanged(Model model, boolean dataChanged) {
        if (dataChanged) {
            mDocumentCache = null;
            mPduBodyCache = null;
        }
    }

    public void sync(PduBody pb) {
        for (SlideModel slide : mSlides) {
            for (MediaModel media : slide) {
                PduPart part = pb.getPartByContentLocation(media.getSrc());
                if (part != null) {
                    media.setUri(part.getDataUri());
                }
            }
        }
        // add for vCard
        for (FileAttachmentModel fileAttach : mAttachFiles) {
            PduPart part = pb.getPartByContentLocation(fileAttach.getSrc());
            if (part != null) {
                fileAttach.setUri(part.getDataUri());
                fileAttach.setData(part.getData());
            }
        }
    }

    public void checkMessageSize(int increaseSize) throws ContentRestrictionException {
        ContentRestriction cr = ContentRestrictionFactory.getContentRestriction();
        cr.checkMessageSize(mCurrentSlideshowSize, increaseSize, mContext.getContentResolver());
    }

    public void checkAttachmentSize(int newSize, boolean append) throws ContentRestrictionException {
        int currentSize = 0;
        if (mAttachFiles != null) {
            for (FileAttachmentModel file : mAttachFiles) {
                currentSize += file.mSize;
            }
        }

        int added = append ? newSize : newSize + SlideshowModel.mReserveSize - currentSize;
        if (added < 0) {
            return;
        }

        ContentRestriction cr = ContentRestrictionFactory.getContentRestriction();
        cr.checkMessageSize(currentSize, added, mContext.getContentResolver());
    }

    /**
     * Determines whether this is a "simple" slideshow.
     * Criteria:
     * - Exactly one slide
     * - Exactly one multimedia attachment
     * - It can optionally have a caption
    */
    public boolean isSimple() {
        // There must be one (and only one) slide.
        if (size() != 1) {
        	Xlog.i(TAG, "size() != 1, isSimple return false");
            return false;
        }

        SlideModel slide = get(0);
        // The slide must have either an audio image or video, but not both.
        if (!(slide.hasAudio() ^ slide.hasImage() ^ slide.hasVideo())) {
        	Xlog.i(TAG, "isSimple return false");
            return false;
        }
        Xlog.i(TAG, "isSimple return true");
        //mtk81083:will return true when there is only one audio/video/image.
        return true;
    }

    /**
     * Make sure the text in slide 0 is no longer holding onto a reference to the text
     * in the message text box.
     */
    public void prepareForSend() {
        if (size() == 1) {
            TextModel text = get(0).getText();
            if (text != null) {
                text.cloneText();
            }
        }
    }

    // Add for vcard begin
    public FileAttachmentModel removeAttachFile(int location) {
        FileAttachmentModel attach = mAttachFiles.remove(location);
        if (attach != null) {
         // vCard and slide won't co-exist, so it should not occupy quota
//            decreaseSlideshowSize(attach.getAttachSize());
            attach.unregisterAllModelChangedObservers();
            notifyModelChanged(true);
        }
        return attach;
    }

    // Because there are extra things to do when remove, so we call #removeAttachFile()
    // rather than use List#clear();
    public void removeAllAttachFiles() {
        final int size = mAttachFiles.size();
        for (int i = (size - 1); i >= 0; i--) {
            removeAttachFile(i);
        }
    }

    public boolean addFileAttachment(FileAttachmentModel object) {
        if ((object != null) && mAttachFiles.add(object)) {
         // vCard and slide won't co-exist, so it should not occupy quota
//            increaseSlideshowSize(increaseSize);
            object.registerModelChangedObserver(this);
            for (IModelChangedObserver observer : mModelChangedObservers) {
                object.registerModelChangedObserver(observer);
            }
            notifyModelChanged(true);
            return true;
        }
        return false;
    }

    public ArrayList<FileAttachmentModel> getAttachFiles() {
        return mAttachFiles;
    }

    public boolean removeAttachFile(Object object){
        if ((object != null) && mAttachFiles.remove(object)) {
            FileAttachmentModel attach = (FileAttachmentModel) object;
         // vCard and slide won't co-exist, so it should not occupy quota
//            decreaseSlideshowSize(attach.getAttachSize());
            attach.unregisterAllModelChangedObservers();
            notifyModelChanged(true);
            return true;
        }
        return false;
    }

    public int sizeOfFilesAttach() {
        return mAttachFiles == null ? 0 : mAttachFiles.size();
    }
    // Add for vCard end
    
    public enum MediaType{IMAGE,AUDIO,VIDEO};
    
    public String[] getAllMediaNames(MediaType mediaType) {
        if (mSlides == null || mSlides.size() < 1) {
            return null;
        }
        String[] names = new String[mSlides.size()];
        int mIndex = 0;
        switch (mediaType) {
            case IMAGE:
                for (SlideModel sm : mSlides) {
                    if (sm.hasImage()) {
                        names[mIndex] = sm.getImage().getSrc();
                        mIndex++;
                    }
                }
                return names;
            case AUDIO:
                for (SlideModel sm : mSlides) {
                    if (sm.hasAudio()) {
                        names[mIndex] = sm.getAudio().getSrc();
                        mIndex++;
                    }
                }
                return names;
            case VIDEO:
                for (SlideModel sm : mSlides) {
                    if (sm.hasVideo()) {
                        names[mIndex] = sm.getVideo().getSrc();
                        mIndex++;
                    }
                }
                return names;
            default:
                return null;
        }
    }
    
}
