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

package com.mediatek.engineermode.memory;

import com.mediatek.engineermode.R;
import com.mediatek.engineermode.ShellExe;
import com.mediatek.xlog.Xlog;
import java.io.File;
import java.io.IOException;
import android.app.TabActivity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;
import android.os.Bundle;

public class NAND_Flash extends TabActivity {

    private final String TAG = "EM/Memory_NAND";
    private static final String FLASH_TYPE = "bEMMC";
    private boolean bEMMC = false;
    private String strEmmcId = null;

    private TextView mCommInfo = null;
    private TextView mFSInfo = null;
    private TextView mPartInfo = null;

    private void setLayout() {

        mCommInfo = (TextView) findViewById(R.id.comm_info);
        mFSInfo = (TextView) findViewById(R.id.file_sys_info);
        mPartInfo = (TextView) findViewById(R.id.partition_info);
        if (mCommInfo == null || mFSInfo == null || mPartInfo == null) {
            Xlog.d(TAG, "clocwork worked...");
            // not return and let exception happened.
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bEMMC = this.getIntent().getBooleanExtra(FLASH_TYPE, false);
        TabHost tabHost = getTabHost();
        LayoutInflater.from(this).inflate(R.layout.memory_nand_tabs,
                tabHost.getTabContentView(), true);
        // tab1
        tabHost.addTab(tabHost.newTabSpec(this.getString(R.string.comm_info))
                .setIndicator(this.getString(R.string.comm_info)).setContent(
                        R.id.LinerLayout_comm_info));

        // tab2
        tabHost.addTab(tabHost.newTabSpec(
                this.getString(R.string.file_sys_info)).setIndicator(
                this.getString(R.string.file_sys_info)).setContent(
                R.id.LinerLayout_file_sys_info));

        // tab3
        tabHost.addTab(tabHost.newTabSpec(
                this.getString(R.string.partition_info)).setIndicator(
                this.getString(R.string.partition_info)).setContent(
                R.id.LinerLayout_partition_info));

        setLayout();
        tabHost.setOnTabChangedListener(new OnTabChangeListener() {
            public void onTabChanged(String tabId) {
                String comm_info = NAND_Flash.this
                        .getString(R.string.comm_info);
                String file_sys_info = NAND_Flash.this
                        .getString(R.string.file_sys_info);
                String partition_info = NAND_Flash.this
                        .getString(R.string.partition_info);
                if (tabId.equals(comm_info)) {
                    if (bEMMC) {
                        mCommInfo.setText(strEmmcId);
                    } else {
                        mCommInfo.setText(getInfo("cat /proc/driver/nand"));
                    }
                } else if (tabId.equals(file_sys_info)) {
                    mFSInfo.setText(getInfo("cat /proc/mounts"));
                } else if (tabId.equals(partition_info)) {
                    if (bEMMC) {
                        mPartInfo.setText(getInfo("cat /proc/emmc"));
                    } else {
                        mPartInfo.setText(getInfo("cat /proc/mtd"));
                    }
                }
            }
        });
        // init
        if (bEMMC) {
            String emmcId = getInfo("cat /sys/block/mmcblk0/device/cid");
            Xlog.v(TAG, "emmcId: " + emmcId);
            StringBuilder sb = new StringBuilder();
            sb.append("emmc ID: ");
            sb.append(emmcId);
            sb.append("\n");
            MmcCid cid = new MmcCid();
            if(cid.parse(emmcId)) {
                sb.append(cid.toString());
                sb.append("\n");
            }
            sb.append("\n");
            sb.append(getInfo("cat /proc/dumchar_info"));
            strEmmcId = sb.toString();
            mCommInfo.setText(strEmmcId);
        } else {
            mCommInfo.setText(getInfo("cat /proc/driver/nand"));
        }
    }

    private String getInfo(String cmd) {
        String result = null;
        try {
            String[] cmdx = { "/system/bin/sh", "-c", cmd };
            int ret = ShellExe.execCommand(cmdx);
            if (0 == ret) {
                result = ShellExe.getOutput();
            } else {
                result = "Can not get flash info.";
            }
        } catch (IOException e) {
            Xlog.i(TAG, e.toString());
            result = e.toString();
        }
        return result;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    
    class MmcCid {
        int manfid;
        char[] prodName;
        String serial;
        int oemid;
        int year;
        int prv;
        int hwrev;
        int fwrev;
        int month;
        int cbox;

        public boolean parse(String cidStr) {
            if (null == cidStr || cidStr.length() != 32) {
                return false;
            }
            try {
                char[] chs = cidStr.toCharArray();
                manfid = Integer.parseInt(getSub(chs, 30, 2), 16);
                char[] name = new char[6];
                for (int i = 0; i < name.length; i++) {
                    name[i] = (char) Integer.parseInt(
                            getSub(chs, 24 - 2 * i, 2), 16);
                }
                prodName = name;
                serial = getSub(chs, 4, 8);
                oemid = Integer.parseInt(getSub(chs, 26, 4), 16);
                year = Integer.parseInt(getSub(chs, 2, 1), 16) + 1997;
                month = Integer.parseInt(getSub(chs, 3, 1), 16);
                prv = Integer.parseInt(getSub(chs, 12, 2), 16);
                return true;
            } catch (Exception e) {
                Xlog.d(TAG, "parse emmc ID exception: " + e.getMessage());
                return false;
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("manfid: ");
            String manname = null;
            switch (manfid & 0xFFFF) {
                case 0x2:
                    manname = "sandisk";
                    break;
                case 0x13:
                    manname = "micron";
                    break;
                case 0x15:
                    manname = "samsung";
                    break;
                case 0x90:
                    manname = "hynix";
                    break;
                default:
                    manname = "unknown";
                    break;
            }
            sb.append(manname);
            sb.append("\n");
            sb.append(String.format("OEM/Application ID: 0x%1$04x", oemid));
            sb.append("\n");
            sb.append(String.format("product name: %s", new String(prodName)));
            sb.append("\n");
            sb.append(String.format("product revision: %d.%d PRV = 0x%x",
                    prv >> 4, prv & 0xf, prv));
            sb.append("\n");
            sb.append(String.format("product serial number: 0x%s", serial));
            sb.append("\n");
            sb.append(String.format("manufacturing date: %s/%d MDT = 0x%04x",
                    month, year, month << 8 | (year - 1997)));
            return sb.toString();
        }

        private String getSub(char[] chs, int start, int leng) {
            int e = chs.length - start;
            int s = chs.length - start - leng;
            if (e >= chs.length) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = s; i < e; i++) {
                sb.append(chs[i]);
            }
            return sb.toString();
        }
    }

}
