/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.MediatekDM.mdm;

public class MdmConfig {
    // TODO
    public class DmAccConfiguration {
        public DmVersion activeAccountDMVersion;
        public String dm12root;
        public boolean isExclusive;
        public boolean updateInactiveDMAccount;
    }

    public static enum DmVersion {
        INVALID,
        DM_1_2,
        DM_1_1_2,
    }

    public static enum HttpAuthLevel {
        BASIC,
        MD5,
        HMAC,
        NONE,
    }

    public static enum NotifVerificationMode {
        DISABLED,
        RESET_NONCE,
        REVERIFY,
        STRICT,
    }

    private DmAccConfiguration mDmAccConfiguration;
    private boolean mAllowBootstrapOverwriteAccount = false;
    private boolean mSwapCpPeers = false;
    private boolean mInstallNotifySuccessOnly = false;
    private boolean mIgnoreMissingETag = false;
    private boolean mIfRangeInsteadOfIfMatch = false;
    private NotifVerificationMode mNotificationVerificationMode = NotifVerificationMode.RESET_NONCE;
    private String mDlUserAgentName = null;
    private HttpAuthLevel mDlHttpAuthenticationLevel = HttpAuthLevel.NONE;
    private HttpAuthLevel mDlProxyAuthenticationLevel = HttpAuthLevel.NONE;
    private HttpAuthLevel mDmHttpAuthenticationLevel = HttpAuthLevel.NONE;
    private HttpAuthLevel mDmProxyAuthenticationLevel = HttpAuthLevel.NONE;
    private String mDlProxy;
    private String mDmUserAgentName;
    private String mDmProxy;
    private boolean mAbortIfClientCommandFailed = false;
    private boolean mB64EncodeBinDataOverWBXML = false;
    private boolean mAllowChallengeWithPkg1 = false;
    private boolean mSessionIDAsDec = false;
    private boolean mDDVersionCheck = false;
    private boolean mIsOMADLAbsoluteURL = false;
    private boolean mIsDMAccNameOptional = false;
    private boolean m202statusCodeNotSupportedByServer = false;
    private boolean mTndsEnabled = true;
    private MdmConfigAgent mAgent;

    public MdmConfig() {
        mAgent = MdmConfigAgent.getInstance();
    }

    public void setMaxMsgSize(int size) throws MdmException {
        mAgent.setMaxMsgSize(size);
    }

    public int getMaxMsgSize() {
        return mAgent.getMaxMsgSize();
    }

    public void setMaxObjSize(int size) throws MdmException {
        mAgent.setMaxObjSize(size);
    }

    public int getMaxObjSize() {
        return mAgent.getMaxObjSize();
    }

    public void setMaxNetRetries(int count) throws MdmException {
        mAgent.setMaxNetRetries(count);
    }

    public int getMaxNetRetries() {
        return mAgent.getMaxNetRetries();
    }

    public void setDmAccSingle(boolean isSingle) throws MdmException {
        mAgent.setDmAccSingle(isSingle);
    }

    public boolean getDmAccSingle() {
        return mAgent.getDmAccSingle();
    }

    public void setAllowBootstrapOverwriteAccount(boolean inIsAllowed) throws MdmException {
        mAllowBootstrapOverwriteAccount  = inIsAllowed;
    }

    public boolean getAllowBootstrapOverwriteAccount() {
        return mAllowBootstrapOverwriteAccount;
    }

    public void setSwapCpPeers(boolean inSwapCpPeers) throws MdmException {
        mSwapCpPeers  = inSwapCpPeers;
    }

    public boolean getSwapCpPeers() {
        return mSwapCpPeers;
    }

    /**
     * Setting this configuration to TRUE contradicts OMA DL standard behavior and is not recommended.
     * @param isSuccessOnly
     * @throws MdmException
     */
    public void setInstallNotifySuccessOnly(boolean isSuccessOnly) throws MdmException {
        mInstallNotifySuccessOnly  = isSuccessOnly;
    }

    public boolean getInstallNotifySuccessOnly() {
        return mInstallNotifySuccessOnly;
    }

    /**
     * Set whether a missing eTag should be ignored while resuming a download. When set to TRUE,
     * the object URL will be used as the object identifier for resuming the download.
     * <b>NOTE:</b> Using this setting is against the standard and not recommended. It should be
     *  used only if the server is committed not to change the content of the object file (but
     *  replace it and its URL, if needed). errors or only download success. Reports are sent
     *  only if InstallNotifyURI attribute exists in the download descriptor. The initial value
     *  is FALSE, indicating all results are reported.
     * @throws MdmException
     */
    public void setIgnoreMissingETag() throws MdmException {
        mIgnoreMissingETag  = true;
    }

    public void setEncodeWBXMLMsg(boolean isWBXML) throws MdmException {
        mAgent.setWbxmlMsgEncoding(isWBXML);
    }

    public boolean getEncodeWBXMLMsg() {
        return mAgent.getWbxmlMsgEncoding();
    }

    /**
     * Set whether to use 'If-Range' HTTP header - instead of 'If-Match' - in OMA DL retries.
     * The initial value is False, indicating that 'If-Match' is used.
     * @param useIfRange
     * @throws MdmException
     */
    public void setIfRangeInsteadOfIfMatch(boolean useIfRange) throws MdmException {
        mIfRangeInsteadOfIfMatch = useIfRange;
    }

    public boolean getIfRangeInsteadOfIfMatch() {
        return mIfRangeInsteadOfIfMatch;
    }

    public void setNotificationVerificationMode(NotifVerificationMode mode) throws MdmException {
        mNotificationVerificationMode = mode;
    }

    public NotifVerificationMode getNotificationVerificationMode() {
        return mNotificationVerificationMode;
    }

    public void setDmAccConfiguration(DmAccConfiguration config) throws MdmException {
        /** @todo */
        mDmAccConfiguration = config;
    }

    public DmAccConfiguration getDmAccConfiguration() {
        /** @todo */
        return mDmAccConfiguration;
    }

    public void setDlUserAgentName(String userAgent) throws MdmException {
        mDlUserAgentName = userAgent;
    }

    public String getDlUserAgentName() {
        return mDlUserAgentName;
    }

    public void setDlHttpAuthentication(HttpAuthLevel level, String username, String password) throws MdmException {
        mDlHttpAuthenticationLevel  = level;
    }

    public void setDlProxy(String proxy) throws MdmException {
        mDlProxy = proxy;
    }

    public String getDlProxy() {
        return mDlProxy;
    }

    public void setDlProxyAuthentication(HttpAuthLevel level, String username, String password) throws MdmException {
        mDlProxyAuthenticationLevel = level;
    }

    public void setDmUserAgentName(String userAgent) throws MdmException {
        mDmUserAgentName = userAgent;
    }

    public String getDmUserAgentName() {
        return mDmUserAgentName;
    }

    public void setDmHttpAuthentication(HttpAuthLevel level, String username, String password) throws MdmException {
        mDmHttpAuthenticationLevel = level;
    }

    public void setDmProxy(String proxy) throws MdmException {
        mAgent.setDmProxy(proxy);
    }

    public String getDmProxy() {
        return mAgent.getDmProxy();
    }

    public void setDmProxyAuthentication(HttpAuthLevel level, String username, String password) throws MdmException {
        mDmProxyAuthenticationLevel = level;
    }

    /**
     * <b>NOTE:</b> Setting this configuration to TRUE contradicts OMA DM
     *  standard behavior and can break the client-server authentication
     *  negotiation when the authentication type is other than "none".
     * @param abort
     * @throws MdmException
     */
    public void setAbortIfClientCommandFailed(boolean abort) throws MdmException {
        mAbortIfClientCommandFailed  = abort;
    }

    public boolean getAbortIfClientCommandFailed() {
        return mAbortIfClientCommandFailed;
    }

    public void setIsClientNoncePerMessage(boolean isPerMessage) throws MdmException {
        mAgent.setClientNoncePerMessage(isPerMessage);
    }

    public boolean getIsClientNoncePerMessage() {
        return mAgent.getClientNoncePerMessage();
    }

    public void setIsServerNoncePerMessage(boolean isPerMessage) throws MdmException {
        mAgent.setServerNoncePerMessage(isPerMessage);
    }

    public boolean getIsServerNoncePerMessage() {
        return mAgent.getServerNoncePerMessage();
    }

    public void setB64EncodeBinDataOverWBXML(boolean isB64) throws MdmException {
        mB64EncodeBinDataOverWBXML = isB64;
    }

    public boolean getB64EncodeBinDataOverWBXML() {
        return mB64EncodeBinDataOverWBXML;
    }

    public void setAllowChallengeWithPkg1(boolean isAllowed) throws MdmException {
        mAllowChallengeWithPkg1  = isAllowed;
    }

    public boolean getAllowChallengeWithPkg1() {
        return mAllowChallengeWithPkg1;
    }

    public void setSessionIDAsDec(boolean decimal) throws MdmException {
        mSessionIDAsDec = decimal;
    }

    public boolean getSessionIDAsDec() {
        return mSessionIDAsDec;
    }

    public void setDDVersionCheck(boolean check) throws MdmException {
        mDDVersionCheck  = check;
    }

    public boolean getDDVersionCheck() {
        return mDDVersionCheck;
    }

    public void setIsOMADLAbsoluteURL(boolean absolute) throws MdmException {
        mIsOMADLAbsoluteURL  = absolute;
    }

    public boolean getIsOMADLAbsoluteURL() {
        return mIsOMADLAbsoluteURL;
    }

    public void setIsDMAccNameOptional(boolean optional) throws MdmException {
        mIsDMAccNameOptional = optional;
    }

    /**
     * <b>NOTE:</b> Setting this configuration to TRUE contradicts OMA DM standard behavior and is not recommended.
     * @param notSupported
     * @throws MdmException
     */
    public void set202statusCodeNotSupportedByServer(boolean notSupported) throws MdmException {
        m202statusCodeNotSupportedByServer  = notSupported;
    }

    public boolean get202statusCodeNotSupportedByServer() {
        return m202statusCodeNotSupportedByServer;
    }

    public void setTndsEnabled(boolean isEnabled) throws MdmException {
        mTndsEnabled  = isEnabled;
    }

    public boolean getTndsEnabled() {
        return mTndsEnabled;
    }
    
    public void setDmTreeRootElement(String rootElement) {
    	mAgent.setDmTreeRootElement(rootElement);
    }
    
    public String getDmTreeRootElement() {
    	return mAgent.getDmTreeRootElement();
    }


    private static class MdmConfigAgent {
        private static MdmConfigAgent mInstance;

        static {
            System.loadLibrary("jni_mdm");
        }

        private MdmConfigAgent() {}
        public static MdmConfigAgent getInstance() {
            synchronized (MdmConfigAgent.class) {
                if (mInstance == null) {
                    mInstance = new MdmConfigAgent();
                }
                return mInstance;
            }
        }

        public native void setWbxmlMsgEncoding(boolean isWBXML);
        public native boolean getWbxmlMsgEncoding();
        public native void setDefaultClientAuthType(HttpAuthLevel type);
        public native HttpAuthLevel getDefaultClientAuthType();
        public native void setMinServerAuthType(HttpAuthLevel type);
        public native HttpAuthLevel getMinServerAuthType();
        public native void setMaxServerAuthType(HttpAuthLevel type);
        public native HttpAuthLevel getMaxServerAuthType();
        public native void setClientNoncePerMessage(boolean isPerMessage);
        public native boolean getClientNoncePerMessage();
        public native void setServerNoncePerMessage(boolean isPerMessage);
        public native boolean getServerNoncePerMessage();
        public native void setMaxMsgSize(int size);
        public native int getMaxMsgSize();
        public native void setMaxObjSize(int size);
        public native int getMaxObjSize();
        public native void setEnsurePackage1Sent(boolean ensurePkg1Sent);
        public native boolean getEnsurePackage1Sent();
        public native void setDmAccSingle(boolean isSingle) throws MdmException;
        public native boolean getDmAccSingle();
        public native void setDmProxy(String proxy);
        public native String getDmProxy();
        public native void setDmTreeRootElement(String rootElement);
        public native String getDmTreeRootElement();
        public native void setMaxNetRetries(int count) throws MdmException;
        public native int getMaxNetRetries();
    }
}

