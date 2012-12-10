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

package com.mediatek.bluetooth.opp.adp;

import java.util.LinkedList;

import com.mediatek.bluetooth.Options;
import com.mediatek.bluetooth.util.BtLog;

/**
 * @param <EventType>
 * 
 * used for two thread communication: one thread add message and another one remove(process) it.
 */
public class EventQueue<EventType> {

	private boolean isCanceled = false;

	private int returnCount;
	private int returnThreshold; 

	private int timeout;

	private LinkedList<EventType> queue = new LinkedList<EventType>();

	/**
	 * Constructor
	 * 
	 * @param timeout operation timeout
	 */
	public EventQueue( int timeout, int returnThreshold ){

		this.timeout = timeout;
		this.returnThreshold = returnThreshold;
	}

	public EventType waitNewEvent() throws InterruptedException {
		
		return this.waitNewEvent( this.timeout );
	}

	/**
	 * block thread until event available
	 * 
	 * @return available event or null if no event available and return-threshold is reach
	 * @throws InterruptedException
	 */
	public EventType waitNewEvent( int waitTimeout ) throws InterruptedException {

		this.returnCount = 0;

		while( true ){

			synchronized( this.queue ){

				if( Options.LL_DEBUG ){

					BtLog.d( "EventQueue.waitNewEvent() before remove queue size:" + this.queue.size() );
				}

				// event available => return first event
				if( !this.queue.isEmpty() ){

					return this.queue.removeFirst();
				}
				// return threshold is enabled when it's > 0
				else if( this.returnThreshold > 0 ){

					// event not available => check return threshold
					// this is for the abnormal case and avoid infinite loop situation (no end event)
					this.returnCount++;
					if( this.returnCount >= this.returnThreshold ){

						return null;
					}
				}

				// wait until event available or timeout
				try {
					if( this.isCanceled ){

						this.isCanceled = false;
						return null;
					}
					this.queue.wait( waitTimeout );
					if( this.isCanceled ){

						this.isCanceled = false;
						return null;
					}
				}
				catch( InterruptedException ie ){

					BtLog.i( "EventQueue.waitNewEvent() thread[" + Thread.currentThread().getName() + "] interrupted" );
					throw ie;
				}
				catch( Exception ex ){

					BtLog.e( "EventQueue.waitNewEvent() error: " + ex.getMessage() );
					throw new IllegalStateException( "EventQueue.waitNewEvent() error.",  ex );
				}
			}
		}
	}

	/**
	 * return null from waitNewEvent()
	 */
	public void cancelWaitNewEvent(){

		synchronized( this.queue ){

			this.isCanceled = true;

			if( Options.LL_DEBUG ){

				BtLog.d( "EventQueue.cancelWaitNewEvent():" + this.queue.size() );
			}
			this.queue.notify();
		}
	}
	
	/**
	 * add event into this queue and notify waiting thread to process
	 * 
	 * @param newEvent
	 */
	public void notifyNewEvent( EventType newEvent ){

		synchronized( this.queue ){

			this.queue.addLast(newEvent);
			
			if( Options.LL_DEBUG ){

				BtLog.d( "EventQueue.notifyNewEvent() after insert queue size:" + this.queue.size() );
			}
			this.queue.notify();
		}
	}

	/**
	 * clear all events in queue
	 */
	public void clear(){

		synchronized( this.queue ){

			this.queue.clear();
		}
	}

	/**
	 * get the size of queue
	 * 
	 * @return
	 */
	public int size(){

		return this.queue.size();
	}

	/**
	 * check whether the given event is in queue or not
	 * 
	 * @param event
	 * @return
	 */
	public boolean contains( EventType event ){

		return this.queue.contains(event);
	}

	/**
	 * used to print the queue content
	 * 
	 * @return
	 */
	public String getPrintableString(){

		StringBuilder res = new StringBuilder();
		for( EventType e : this.queue ){

			res.append( e.toString() );
		}
		return res.toString();
	}
}
