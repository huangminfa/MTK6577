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

package com.mediatek.backuprestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

//public class ResultDialog extends AlertDialog {
public class ResultDialog {

    public final static String ITEM_NAME = "name";
    public final static String ITEM_RESULT = "result";
           
    public static AlertDialog createResultDlg(Context context, int title_id, Bundle args, 
                    DialogInterface.OnClickListener listener){
        ResultDlgAdapter adapter = createResultAdapter(context, args);
        AlertDialog dialog =  new AlertDialog.Builder(context).setCancelable(false)
                .setTitle(title_id)
                .setPositiveButton(R.string.btn_ok, listener)
                .setAdapter(adapter, null)
                .create();

        ListView listview = dialog.getListView();
        if (listview != null){
            listview.setAdapter(adapter);
        }

        return dialog;
    }
    
    public static ResultDlgAdapter createResultAdapter(Context context,Bundle args){
        List<Map<String, Object>>dataList = new ArrayList<Map<String, Object>>();
        List<ResultEntity> list = args.getParcelableArrayList("result");
        for (ResultEntity item : list){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(ITEM_NAME, BackupRestoreUtils.getModuleStringFromType(context, item.mType));
            int resId = item.mResult ? R.string.result_success : R.string.result_fail;
            map.put(ITEM_RESULT, context.getString(resId));
            dataList.add(map);
        }
        String[] from = new String[] {ITEM_NAME, ITEM_RESULT};
        int[] to = new int[] {R.id.module_name, R.id.result};
        return  new ResultDlgAdapter(context, dataList, R.layout.result_list_item, from, to);
    }
    
    
    public static class ResultEntity implements Parcelable{
        int mType;
        boolean mResult;
        
        public int describeContents() {
            return 0;
        }
        
        public ResultEntity(int type, boolean result){
            mType = type;
            mResult = result;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mType);
            dest.writeInt(mResult ? 1 : 0);
        }
        
        private ResultEntity(Parcel in){
            mType = in.readInt();
            mResult = in.readInt() > 0;
        }
        public static final Parcelable.Creator<ResultEntity> CREATOR = new Parcelable.Creator<ResultEntity>() {
            
            public ResultEntity createFromParcel(Parcel in) {
                return new ResultEntity(in);
            }

            public ResultEntity[] newArray(int size) {
                return new ResultEntity[size];
            }
        };
        
    }
    
   
    private static class ResultDlgAdapter extends SimpleAdapter { 
        List<? extends Map<String, ?>> mData;
        private Context mContext;
        public ResultDlgAdapter(Context context, List<? extends Map<String, ?>> data,
            int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            mData = data;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            
            View view = super.getView(position, convertView, parent);  
            TextView result = (TextView)view.findViewById(R.id.result);
            if(mData.get(position).get(ITEM_RESULT).equals(mContext.getResources().getString(R.string.result_success))){
                result.setTextColor(mContext.getResources().getColor( R.color.result_success));
            }
            else{
                result.setTextColor(mContext.getResources().getColor( R.color.result_fail));
            }
            return view;
        }
    }
}
