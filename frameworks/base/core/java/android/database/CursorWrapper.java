/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * Wrapper class for Cursor that delegates all calls to the actual cursor object.  The primary
 * use for this class is to extend a cursor while overriding only a subset of its methods.
 */
public class CursorWrapper implements Cursor {
	/** @hide */
	protected final Cursor mCursor;

	/**
	 * Creates a cursor wrapper.
	 * @param cursor The underlying cursor to wrap.
	 */
	public CursorWrapper(Cursor cursor) {
		mCursor = cursor;
	}

	/**
	 * Gets the underlying cursor that is wrapped by this instance.
	 *
	 * @return The wrapped cursor.
	 */
	public Cursor getWrappedCursor() {
		return mCursor;
	}

	public void close() {
		mCursor.close();
	}

	public boolean isClosed() {
		return mCursor.isClosed();
	}

	public int getCount() {
		try {
			return mCursor.getCount();
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null",e);
			} else {
				throw e;
			}
		}
		return -1;
	}

	public void deactivate() {
		try {
			mCursor.deactivate();
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null",e);
			} else {
				throw e;
			}
		}
	}

	public boolean moveToFirst() {
		try {
			return mCursor.moveToFirst();
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null",e);
			} else {
				throw e;
			}
		}
		return false;
	}

	public int getColumnCount() {
		try {
			return mCursor.getColumnCount();
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null",e);
			} else {
				throw e;
			}
		}
		return -1;
	}

	public int getColumnIndex(String columnName) {
		try {
			return mCursor.getColumnIndex(columnName);
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null"+",ColumnName="+columnName,e);
			} else {
				throw e;
			}
		}
		return -1;
	}

	public int getColumnIndexOrThrow(String columnName)
	throws IllegalArgumentException {
		try {
			return mCursor.getColumnIndexOrThrow(columnName);
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null"+",ColumnName="+columnName,e);
			} else {
				throw e;
			}
		}
		return -1;
	}

	public String getColumnName(int columnIndex) {
		try {
			return mCursor.getColumnName(columnIndex);
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null"+",ColumnIndex="+columnIndex,e);
			} else {
				throw e;
			}
		}
		return null;
	}

	public String[] getColumnNames() {
		try {
			return mCursor.getColumnNames();
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null",e);
			} else {
				throw e;
			}
		}
		return null;
	}

	public double getDouble(int columnIndex) {
		try {
			return mCursor.getDouble(columnIndex);
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null"+",ColumnIndex="+columnIndex,e);
			} else {
				throw e;
			}
		}
		return -1;
	}

	public Bundle getExtras() {
		try {
			return mCursor.getExtras();
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null",e);
			} else {
				throw e;
			}
		}
		return null;
	}

	public float getFloat(int columnIndex) {
		try {
			return mCursor.getFloat(columnIndex);
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null,ColumnIndex="+columnIndex,e);
			} else {
				throw e;
			}
		}
		return -1;
	}

	public int getInt(int columnIndex) {
		try {
			return mCursor.getInt(columnIndex);
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null,ColumnIndex="+columnIndex,e);
			} else {
				throw e;
			}
		}
		return -1;
	}

	public long getLong(int columnIndex) {
		try {
			return mCursor.getLong(columnIndex);
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null,ColumnIndex="+columnIndex,e);
			} else {
				throw e;
			}
		}
		return -1;
	}

	public short getShort(int columnIndex) {
		try {
			return mCursor.getShort(columnIndex);
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null,ColumnIndex="+columnIndex,e);
			} else {
				throw e;
			}
		}
		return -1;
	}

	public String getString(int columnIndex) {
		try {
			return mCursor.getString(columnIndex);
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null,ColumnIndex="+columnIndex,e);
			} else {
				throw e;
			}
		}
		return null;
	}

	public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
		try {
			mCursor.copyStringToBuffer(columnIndex, buffer);
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null,ColumnIndex="+columnIndex,e);
			} else {
				throw e;
			}
		}
	}

	public byte[] getBlob(int columnIndex) {
		try {
			return mCursor.getBlob(columnIndex);
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null,ColumnIndex="+columnIndex,e);
			} else {
				throw e;
			}
		}
		return null;
	}

	public boolean getWantsAllOnMoveCalls() {
		try {
			return mCursor.getWantsAllOnMoveCalls();
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null",e);
			} else {
				throw e;
			}
		}
		return false;
	}

	public boolean isAfterLast() {
		try {
			return mCursor.isAfterLast();
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null",e);
			} else {
				throw e;
			}
		}
		return false;
	}

	public boolean isBeforeFirst() {
		try {
			return mCursor.isBeforeFirst();
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null",e);
			} else {
				throw e;
			}
		}
		return false;
	}

	public boolean isFirst() {
		try {
			return mCursor.isFirst();
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Curso=null",e);
			} else {
				throw e;
			}
		}
		return false;
	}

	public boolean isLast() {
		try {
			return mCursor.isLast();
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor="+mCursor,e);
			} else {
				throw e;
			}
		}
		return false;
	}

	public int getType(int columnIndex) {
		try {
			return mCursor.getType(columnIndex);
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null,ColumnIndex="+columnIndex,e);
			} else {
				throw e;
			}
		}
		return -1;
	}

	public boolean isNull(int columnIndex) {
		try {
			return mCursor.isNull(columnIndex);
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null,ColumnIndex="+columnIndex,e);
			} else {
				throw e;
			}
		}
		return false;
	}

	public boolean moveToLast() {
		try {
			return mCursor.moveToLast();
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null",e);
			} else {
				throw e;
			}
		}
		return false;
	}

	public boolean move(int offset) {
		try {
			return mCursor.move(offset);
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null,offset="+offset,e);
			} else {
				throw e;
			}
		}
		return false;
	}

	public boolean moveToPosition(int position) {
		try {
			return mCursor.moveToPosition(position);
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null,position="+position,e);
			} else {
				throw e;
			}
		}
		return false;
	}

	public boolean moveToNext() {
		try {
			return mCursor.moveToNext();
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null",e);
			} else {
				throw e;
			}
		}
		return false;
	}

	public int getPosition() {
		try {
			return mCursor.getPosition();
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null",e);
			} else {
				throw e;
			}
		}
		return -1;
	}

	public boolean moveToPrevious() {
		try {
			return mCursor.moveToPrevious();
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null",e);
			} else {
				throw e;
			}
		}
		return false;
	}

	public void registerContentObserver(ContentObserver observer) {
		try {
			mCursor.registerContentObserver(observer);
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null",e);
			} else {
				throw e;
			}
		}
	}

	public void registerDataSetObserver(DataSetObserver observer) {
		try {
			mCursor.registerDataSetObserver(observer);
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null",e);
			} else {
				throw e;
			}
		}
	}

	public boolean requery() {
		try {
			return mCursor.requery();
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null",e);
			} else {
				throw e;
			}
		}
		return false;
	}

	public Bundle respond(Bundle extras) {
		try {
			return mCursor.respond(extras);
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null",e);
			} else {
				throw e;
			}
		}
		return null;
	}

	public void setNotificationUri(ContentResolver cr, Uri uri) {
		try {
			mCursor.setNotificationUri(cr, uri);
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null,uri="+uri,e);
			} else {
				throw e;
			}
		}
	}

	public void unregisterContentObserver(ContentObserver observer) {
		try {
			mCursor.unregisterContentObserver(observer);
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null",e);
			} else {
				throw e;
			}
		}
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
		try {
			mCursor.unregisterDataSetObserver(observer);
		} catch(NullPointerException e) {
			if (mCursor == null) {
				Log.e("CursorWapper", "NullPointerException Cursor=null",e);
			} else {
				throw e;
			}
		}
	}
}

