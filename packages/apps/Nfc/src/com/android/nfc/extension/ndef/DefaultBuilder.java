package com.android.nfc.extension.ndef;

import java.util.ArrayList;
import java.util.List;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import com.android.nfc.extension.ndef.NDEF.IBuilder;
import com.android.nfc.extension.ndef.NDEF.Record;

/**
 * @hide
 */
public class DefaultBuilder implements IBuilder 
{
	/**
	 * Item table
	 */
	private List<Record> mItems = new ArrayList<Record>();
	
	/**
	 * Append Record to message
	 * @param NDEF Record
	 */
	public void appendRecord(Record record) 
	{
		mItems.add(record);
	}

	/**
	 * Convert to Message byte array
	 */
	public byte [] toBytesArray() 
	{
		if(mItems.size() == 0)
		{
			return null;
		}		
		
		NdefRecord [] records = new NdefRecord[mItems.size()];
		Record record = null;		
		for(int index = 0;index < mItems.size();index ++)
		{
			record = mItems.get(index);
			records[index] = new NdefRecord(record.getTNF(), 
											record.getType(),
											record.getID(),
											record.getPayload());
		}
		
		NdefMessage message = new NdefMessage(records);
		return message.toByteArray();
	}

	public byte[] toBytesArray(Record record) {
		NdefRecord ndefRecord = new NdefRecord(record.getTNF(), record.getType(), record.getID(), record.getPayload());
		return ndefRecord.toByteArray();
	}
}
