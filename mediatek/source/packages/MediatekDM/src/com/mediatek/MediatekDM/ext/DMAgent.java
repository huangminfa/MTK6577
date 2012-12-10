/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.MediatekDM.ext;

import java.lang.String;

import android.os.RemoteException;
import android.os.IBinder;


public interface DMAgent extends android.os.IInterface {
    public static abstract class Stub extends android.os.Binder implements
                DMAgent {
        private static final java.lang.String DESCRIPTOR = "DMAgent";

        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        public static DMAgent asInterface(android.os.IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = (android.os.IInterface) obj
                                        .queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof DMAgent))) {
                return ((DMAgent) iin);
            }
            return new DMAgent.Stub.Proxy(obj);
        }

        public android.os.IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, android.os.Parcel data,
                                  android.os.Parcel reply, int flags)
        throws android.os.RemoteException {
            return true;
        }

        private static class Proxy implements DMAgent {
            private android.os.IBinder mRemote;

            Proxy(android.os.IBinder remote) {
                mRemote = remote;
            }

            public android.os.IBinder asBinder() {
                return mRemote;
            }

            public java.lang.String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }

            public byte[] readDMTree() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                byte[] _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_readDMTree, _data,
                                     _reply, 0);
                    _result = _reply.createByteArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            public boolean writeDMTree(byte[] tree)
            throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeByteArray(tree);
                    mRemote.transact(Stub.TRANSACTION_writeDMTree, _data,
                                     _reply, 0);
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            public boolean isLockFlagSet() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_isLockFlagSet, _data,
                                     _reply, 0);
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            public boolean setLockFlag(String lockType)
            throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeByteArray(lockType.getBytes());
                    mRemote.transact(Stub.TRANSACTION_setLockFlag, _data,
                                     _reply, 0);
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            public boolean clearLockFlag() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_clearLockFlag, _data,
                                     _reply, 0);
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            public boolean isWipeSet() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_isWipeSet, _data, _reply,
                                     0);
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            public boolean setWipeFlag(String lockType)
            throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeByteArray(lockType.getBytes());
                    mRemote.transact(Stub.TRANSACTION_setWipeFlag, _data,
                                     _reply, 0);
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            public boolean clearWipeFlag() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_clearWipeFlag, _data,
                                     _reply, 0);
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            public String readIMSI() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                byte[] _result;
                String ret = null;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_readIMSI, _data, _reply,
                                     0);
                    _result = _reply.createByteArray();
                    if (_result != null) {
                        ret = new String(_result);
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return ret;
            }

            public boolean writeIMSI(String imsi)
            throws android.os.RemoteException {
                if (imsi == null) {
                    return false;
                }
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeByteArray(imsi.getBytes());
                    mRemote.transact(Stub.TRANSACTION_writeIMSI, _data, _reply,
                                     0);
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;

            }

            public String readOperatorName() throws RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                byte[] _result;
                String ret = null;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_readOperatorName, _data,
                                     _reply, 0);
                    _result = _reply.createByteArray();
                    if (_result != null) {
                        ret = new String(_result);
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return ret;
            }

            public boolean setRebootFlag() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_setRebootFlag, _data,
                                     _reply, 0);
                    _result = (1 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            public boolean getLockType() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getLockType, _data,
                                     _reply, 0);
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            public int getOperatorID() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getOperatorID, _data,
                                     _reply, 0);
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            public String getOperatorName() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                byte[] _result;
                String ret = null;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getOperatorName, _data,
                                     _reply, 0);
                    _result = _reply.createByteArray();
                    if (_result != null) {
                        ret = new String(_result);
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return ret;
            }

            public boolean isHangMoCallLocking()
            throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_isHangMoCallLocking,
                                     _data, _reply, 0);
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            public boolean isHangMtCallLocking()
            throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_isHangMtCallLocking,
                                     _data, _reply, 0);
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            public boolean isBootRecoveryFlag()
            throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_isBootRecoveryFlag,
                                     _data, _reply, 0);
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            public boolean clearRebootFlag() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_clearRebootFlag, _data,
                                     _reply, 0);
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            public int getUpgradeStatus() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getUpgradeStatus, _data,
                                     _reply, 0);
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            public int restartAndroid() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_restartAndroid, _data,
                                     _reply, 1);
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            public int readOtaResult() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_readOtaResult, _data,
                                     _reply, 0);
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }
        }

        static final int TRANSACTION_readDMTree = (IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_writeDMTree = (IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_isLockFlagSet = (IBinder.FIRST_CALL_TRANSACTION + 2);
        static final int TRANSACTION_setLockFlag = (IBinder.FIRST_CALL_TRANSACTION + 3);
        static final int TRANSACTION_clearLockFlag = (IBinder.FIRST_CALL_TRANSACTION + 4);
        static final int TRANSACTION_readIMSI = (IBinder.FIRST_CALL_TRANSACTION + 5);
        static final int TRANSACTION_writeIMSI = (IBinder.FIRST_CALL_TRANSACTION + 6);
        static final int TRANSACTION_readOperatorName = (IBinder.FIRST_CALL_TRANSACTION + 7);

        static final int TRANSACTION_setRebootFlag = (IBinder.FIRST_CALL_TRANSACTION + 100);

        static final int TRANSACTION_getLockType = (IBinder.FIRST_CALL_TRANSACTION + 101);
        static final int TRANSACTION_getOperatorID = (IBinder.FIRST_CALL_TRANSACTION + 102);
        static final int TRANSACTION_getOperatorName = (IBinder.FIRST_CALL_TRANSACTION + 103);
        static final int TRANSACTION_isHangMoCallLocking = (IBinder.FIRST_CALL_TRANSACTION + 104);
        static final int TRANSACTION_isHangMtCallLocking = (IBinder.FIRST_CALL_TRANSACTION + 105);
        static final int TRANSACTION_clearRebootFlag = (IBinder.FIRST_CALL_TRANSACTION + 106);
        static final int TRANSACTION_isBootRecoveryFlag = (IBinder.FIRST_CALL_TRANSACTION + 107);
        static final int TRANSACTION_getUpgradeStatus = (IBinder.FIRST_CALL_TRANSACTION + 108);
        static final int TRANSACTION_restartAndroid = (IBinder.FIRST_CALL_TRANSACTION + 109);
        static final int TRANSACTION_isWipeSet = (IBinder.FIRST_CALL_TRANSACTION + 201);
        static final int TRANSACTION_setWipeFlag = (IBinder.FIRST_CALL_TRANSACTION + 202);
        static final int TRANSACTION_clearWipeFlag = (IBinder.FIRST_CALL_TRANSACTION + 203);

        static final int TRANSACTION_readOtaResult = (IBinder.FIRST_CALL_TRANSACTION + 204);
    }

    public byte[] readDMTree() throws android.os.RemoteException;

    public boolean writeDMTree(byte[] tree) throws android.os.RemoteException;

    public boolean isLockFlagSet() throws android.os.RemoteException;

    public boolean setLockFlag(String lockType)
    throws android.os.RemoteException;

    public boolean clearLockFlag() throws android.os.RemoteException;

    public String readIMSI() throws android.os.RemoteException;;

    public boolean writeIMSI(String imsi) throws android.os.RemoteException;;

    public String readOperatorName() throws android.os.RemoteException;;

    public boolean setRebootFlag() throws android.os.RemoteException;

    public boolean getLockType() throws android.os.RemoteException;

    public int getOperatorID() throws android.os.RemoteException;

    public String getOperatorName() throws android.os.RemoteException;

    public boolean isHangMoCallLocking() throws android.os.RemoteException;

    public boolean isHangMtCallLocking() throws android.os.RemoteException;

    public boolean isBootRecoveryFlag() throws android.os.RemoteException;

    public boolean clearRebootFlag() throws android.os.RemoteException;

    public int getUpgradeStatus() throws android.os.RemoteException;

    public int restartAndroid() throws android.os.RemoteException;

    public boolean isWipeSet() throws android.os.RemoteException;

    public boolean setWipeFlag(String lockType)
    throws android.os.RemoteException;

    public boolean clearWipeFlag() throws android.os.RemoteException;

    public int readOtaResult() throws android.os.RemoteException;
}
