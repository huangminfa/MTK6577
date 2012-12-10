// The Message file format:
// [0]  Magic number: MTK
// [3]  Compress: the sign,1 if this package has other brother
// [4]  Length: the size about appending data (not including header).
// [8]  Type: the type about handshake, bye etc...
// [9]  Item: the item about message type
// [10] Header Size: header size
// [10 + X] X, the appending data, size is in Length

package com.mediatek.ngin3d.debugtools.messagepackage;

import java.util.Arrays;

public class NDBMessage {

    private static final byte[] ID_MTK = {
        'M', 'T', 'K'
    };
    private static final int IH_MAGIC = 0;
    private static final int IH_COMPRESS = 3;
    private static final int IH_LENGTH = 4;
    private static final int IH_TYPE = 8;
    private static final int IH_ITEM = 9;
    private static final int INDEX_HEADER_SIZE = 10;
    private static final int BUFFER_SIZE = 4096;
    private static final int COMPRESSED = 1;

    private byte[] mByteMsg = new byte[BUFFER_SIZE];
    private byte[] mReciMsg;

    public NDBMessage() {
        System.arraycopy(ID_MTK, IH_MAGIC, mByteMsg, IH_MAGIC, IH_COMPRESS);
    }

    public boolean msgDecode(byte[] data) {
        mReciMsg = data;
        return checkHeader(mReciMsg, IH_MAGIC, ID_MTK);
    }

    public byte getMessageType() {
        return mReciMsg[IH_TYPE];
    }

    public byte getTypeItem() {
        return mReciMsg[IH_ITEM];
    }

    public int getDataLength() {
        return Utils.readInt(mReciMsg, IH_LENGTH);
    }

    public String getMsgData() {
        return new String(Arrays.copyOfRange(mReciMsg, INDEX_HEADER_SIZE,
                INDEX_HEADER_SIZE + Utils.readInt(mReciMsg, IH_LENGTH)));
    }

    public byte[] msgEncode(byte type, byte item, byte[] data) {
        return msgEncode(type, item, data, false);
    }

    public byte[] msgEncode(byte type, byte item, byte[] data, boolean isCompress) {

        // to decide the message size if the data is not null, add the data size
        // IH_LENGTH is the data size
        if (data == null) {
            Utils.writeInt(mByteMsg, IH_LENGTH, 0);
        } else {
            System.arraycopy(data, 0, mByteMsg, INDEX_HEADER_SIZE,
                    data.length);
            Utils.writeInt(mByteMsg, IH_LENGTH, data.length);
        }

        mByteMsg[IH_TYPE] = type;
        mByteMsg[IH_ITEM] = item;
        if (isCompress) {
            mByteMsg[IH_COMPRESS] = COMPRESSED;
        }

        return mByteMsg;
    }

    /**
     * Checks the result array starts with the provided code
     * 
     * @param result The result array to check
     * @param code The 3 byte code.
     * @return true if the code matches.
     */
    private static boolean checkHeader(byte[] result, int begin, byte[] code) {
        if (result[begin] != code[0]
            || result[begin + 1] != code[1]
            || result[begin + 2] != code[2]) {
            return false;
        }
        return true;
    }

}
