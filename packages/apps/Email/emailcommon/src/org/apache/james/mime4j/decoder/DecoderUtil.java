/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.mime4j.decoder;

//BEGIN android-changed: Stubbing out logging
import org.apache.james.mime4j.Log;
import org.apache.james.mime4j.LogFactory;
//END android-changed
import org.apache.james.mime4j.util.CharsetUtil;

import com.android.emailcommon.Logging;
import com.android.emailcommon.utility.Utility;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static methods for decoding strings, byte arrays and encoded words.
 *
 * 
 * @version $Id: DecoderUtil.java,v 1.3 2005/02/07 15:33:59 ntherning Exp $
 */
public class DecoderUtil {
    private static Log log = LogFactory.getLog(DecoderUtil.class);
    private static final String DECODED_REGEX = "(=\\?)([A-Za-z0-9_-]*)\\?(?i)[b,q]\\?([^?])+(\\?=)";

    /**
     * Decodes a string containing quoted-printable encoded data. 
     * 
     * @param s the string to decode.
     * @return the decoded bytes.
     */
    public static byte[] decodeBaseQuotedPrintable(String s) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            byte[] bytes = s.getBytes("US-ASCII");
            
            QuotedPrintableInputStream is = new QuotedPrintableInputStream(
                                               new ByteArrayInputStream(bytes));
            
            int b = 0;
            while ((b = is.read()) != -1) {
                baos.write(b);
            }
        } catch (IOException e) {
            /*
             * This should never happen!
             */
            log.error(e);
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Decodes a string containing base64 encoded data. 
     * 
     * @param s the string to decode.
     * @return the decoded bytes.
     */
    public static byte[] decodeBase64(String s) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            byte[] bytes = s.getBytes("US-ASCII");
            
            Base64InputStream is = new Base64InputStream(
                                        new ByteArrayInputStream(bytes));
            
            int b = 0;
            while ((b = is.read()) != -1) {
                baos.write(b);
            }
        } catch (IOException e) {
            /*
             * This should never happen!
             */
            log.error(e);
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Decodes an encoded word encoded with the 'B' encoding (described in 
     * RFC 2047) found in a header field body.
     * 
     * @param encodedWord the encoded word to decode.
     * @param charset the Java charset to use.
     * @return the decoded string.
     * @throws UnsupportedEncodingException if the given Java charset isn't 
     *         supported.
     */
    public static String decodeB(String encodedWord, String charset) 
            throws UnsupportedEncodingException {
        
        return new String(decodeBase64(encodedWord), charset);
    }
    
    /**
     * Decodes an encoded word encoded with the 'Q' encoding (described in 
     * RFC 2047) found in a header field body.
     * 
     * @param encodedWord the encoded word to decode.
     * @param charset the Java charset to use.
     * @return the decoded string.
     * @throws UnsupportedEncodingException if the given Java charset isn't 
     *         supported.
     */
    public static String decodeQ(String encodedWord, String charset)
            throws UnsupportedEncodingException {
           
        /*
         * Replace _ with =20
         */
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < encodedWord.length(); i++) {
            char c = encodedWord.charAt(i);
            if (c == '_') {
                sb.append("=20");
            } else {
                sb.append(c);
            }
        }
        
        return new String(decodeBaseQuotedPrintable(sb.toString()), charset);
    }

    public static String decodeEncodedWords(String body) {
        StringBuffer sb = new StringBuffer();
        int start = 0;
        int quotedPos = 0;
        int next = 0;
        while (true) {
            quotedPos = body.indexOf('"', start);
            if (quotedPos == -1) {
                sb.append(decodeEncodedWordsProcess(body.substring(start)));
                break;
            }
            quotedPos++;
            sb.append(body.substring(start, quotedPos));
            next = body.indexOf('"', quotedPos);
            String quoted = body.substring(quotedPos, next);
            sb.append(decodeEncodedWordsProcess(quoted) + "\"");
            next++;
            start = next;
        }
        return sb.toString();
    }
    public static boolean isAllAscii(InputStream in) {
        int result = 0;
        try {
            while ((result = in.read()) != -1) {
                if ((0x0080 & result) != 0) {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static String emailCharsetDetect(InputStream in) {
        String properCharset = null;
        // use ICU lib to detect charset.
        CharsetDetector cd = new CharsetDetector();
        try {
            cd.setText(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
         CharsetMatch[] cm = cd.detectAll();
         if (cm != null && cm.length > 0) {
             // print chraset information.
             for(CharsetMatch match:cm){
                 Logging.d("Charset Detect Result: " + match.getName());
             }
             // If the most possible charset was "UTF-8", we do nothing.
             if(cm[0].getName().equals("UTF-8")) {
                 return cm[0].getName();
             }
             for (CharsetMatch match : cm) {
                 if(match.getName().startsWith("GB")){
                     properCharset = match.getName();
                     break;
                 }
             }
             if (properCharset != null) {
                 return properCharset;
             } else {
                 return cm[0].getName();
             }
         }else {
             return null;
         }
    }

    /**
     * Decodes a string containing encoded words as defined by RFC 2047.
     * Encoded words in have the form 
     * =?charset?enc?Encoded word?= where enc is either 'Q' or 'q' for 
     * quoted-printable and 'B' or 'b' for Base64.
     * 
     * Mediatek: Implement new version
     * 1. Match all substrings by regular-expression
     * 2. Decode each substrings or combine them first
     * 3. This version could resolved most of the situations but still few kinds of missing which I don not know
     * @param body the string to decode.
     * @return the decoded string.
     */
    public static String decodeEncodedWordsProcess(String body) {

        // ANDROID:  Most strings will not include "=?" so a quick test can prevent unneeded
        // object creation.  This could also be handled via lazy creation of the StringBuilder.
        if (body.indexOf("=?") == -1) {
            String resultString = body;
            String properCharset = null;
            byte[] bytesOfUnkown = Utility.bytesFromUnknownString(body);
            boolean isAscii = isAllAscii(new ByteArrayInputStream(bytesOfUnkown));
            if (isAscii) {
                return resultString;
            }
            // detect charset.
            properCharset = emailCharsetDetect(new ByteArrayInputStream(bytesOfUnkown));
            if (properCharset != null) {
                try {
                    resultString = new String(bytesOfUnkown, properCharset);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            return resultString;
        }
        body = body.replace("\t", " ");

        String decodeReg = DECODED_REGEX;

        Pattern p = Pattern.compile(decodeReg);
        Matcher m = p.matcher(body);
        StringBuilder encodedText = new StringBuilder();
        String[] bodys = body.split(decodeReg);

        while (m.find()) {
            encodedText.append(m.group().replaceAll("\\s*", ""));
            encodedText.append(" ");
        }
        return (bodys.length > 0 ? bodys[0] : "") 
        + decodeEncodedWord(encodedText.toString(), 0, encodedText.length());
    }

    private static String decodeEncodedWord(String body, int begin, int end) {
        // split the content with space then join the sub-parts together into one string.
        // then decode it.
        String [] ms = body.split(" ");
        StringBuilder sb = new StringBuilder();
        String charset = null;
        String encoding = null;
        int textPiece = 0; 
        for (String s : ms) {
            textPiece++;
            int b = 0;
            int e = s.length();
            int qm1 = s.indexOf('?', b + 2);
            // "add qm1==-1, and return body" if string is not include, it will not parse, and return original string.
            if (qm1 == e - 2 || qm1 == -1 )
                return body;

            int qm2 = s.indexOf('?', qm1 + 1);
            if (qm2 == e - 2)
                return body;

            String mimeCharset = s.substring(b + 2, qm1);
            encoding = s.substring(qm1 + 1, qm2);
            String encodedText = s.substring(qm2 + 1, e - 2);
            //sb.append(encodedText);

            charset = CharsetUtil.toJavaCharset(mimeCharset);
            if (charset == null) {
                if (log.isWarnEnabled()) {
                    log.warn("MIME charset '" + mimeCharset + "' in encoded word '"
                            + s.substring(b, e) + "' doesn't have a "
                            + "corresponding Java charset");
                }
                return null;
            } else if (charset.equalsIgnoreCase("GB18030")) {
                log.warn("Current JDK doesn't support decoding of charset '"+ charset + "',use GBK!");
                charset = "GBK";

            } else if (!CharsetUtil.isDecodingSupported(charset)) {
                if (log.isWarnEnabled()) {
                    log.warn("Current JDK doesn't support decoding of charset '"
                            + charset + "' (MIME charset '" + mimeCharset
                            + "' in encoded word '" + body.substring(b, e)
                            + "')");
                }
                return null;
            }
        //}
            //String encodedText = sb.toString();

            if (encodedText.length() == 0) {
                if (log.isWarnEnabled()) {
                    log.warn("Warning: Unknown encoding in encoded word '"
                            + body.substring(begin, end) + "'");
                }
                return null;
            }
    
            try {
                if ("Q".equalsIgnoreCase(encoding)) {
                    sb.append(encodedText);
                    if(textPiece >= ms.length) {
                        return DecoderUtil.decodeQ(sb.toString(), charset);
                    }
                    //return DecoderUtil.decodeQ(encodedText, charset);
                } else if ("B".equalsIgnoreCase(encoding)) {
                    //return DecoderUtil.decodeB(encodedText, charset);
                    sb.append(DecoderUtil.decodeB(encodedText, charset));
                } else {
                    if (log.isWarnEnabled()) {
                        log.warn("Warning: Unknown encoding in encoded word '"
                                + body.substring(begin, end) + "'");
                    }
                    return null;
                }
            } catch (UnsupportedEncodingException ex) {
                // should not happen because of isDecodingSupported check above
                if (log.isWarnEnabled()) {
                    log.warn("Unsupported encoding in encoded word '"
                            + body.substring(begin, end) + "'", ex);
                }
                return null;
            } catch (RuntimeException ex) {
                if (log.isWarnEnabled()) {
                    log.warn("Could not decode encoded word '"
                            + body.substring(begin, end) + "'", ex);
                }
                return null;
            }
        }
        return sb.toString();
    }
}
