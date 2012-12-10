package com.android.nfc.extension.ndef;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import com.android.nfc.extension.ndef.NDEF.IParser;
import com.android.nfc.extension.ndef.NDEF.Record;

/**
 * @hide
 */
public class DefaultParser implements IParser 
{
	DefaultParser() 
	{
		super();
	}

	@Override
	public Record [] getRecords(byte[] raw) throws Exception 
	{
		
		try 
		{
			// raw to NDEFMessage
			NdefMessage message = new NdefMessage(raw);
			// get records
			NdefRecord [] records = message.getRecords();
			
			int aaa = 0;
			aaa = records.length;
			System.out.println("DefaultParser aaa = "+ aaa);
			// only get a record in this GCrecord-included NdefMessage
			
			if(records == null || records.length == 0)
			{
				return null;
			}
			
			Record [] result = new Record[records.length];
			Record rec = null;
			for(int i = 0;i < records.length; i++)
			{
				rec = new Record();
				rec.setID(records[i].getId());
				if(i == 0)
				{
					rec.setMB(true);
				}
				
				if(i == records.length - 1)
				{
					rec.setME(true);
				}
				
				rec.setTNF((byte)records[i].getTnf());
				rec.setType(records[i].getType());
				rec.setPayload(records[i].getPayload());
			
				result[i] = rec;
			}
			
			return result;
		} 
		catch (FormatException e) 
		{
			throw new Exception(e);
		}
	}
}
