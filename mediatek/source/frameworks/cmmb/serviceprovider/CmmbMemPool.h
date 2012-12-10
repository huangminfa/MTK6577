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

#ifndef CMMB_MEM_POOL_H
#define CMMB_MEM_POOL_H


#include <stdlib.h>
#include "CmmbSPCommon.h"

template<class T>
class CMemPool
{
public:
	CMemPool(UINT32 Block_num,UINT32 Block_size ) 
		:_BLOCK_SIZE(Block_size)        
		,_BLOCK_NUM(Block_num)
		,_BLOCK_EMPTYBUF_COUNT(Block_num)
		,_BlockIndex(0)
	{
	       CAutoLock lock(MempoolMutex);
		_alloc_blocks();		
	}

	~CMemPool()
	{
	       CAutoLock lock(MempoolMutex);
		for( UINT32 i=0 ; i<_BLOCK_NUM; i++ )
		{
			if( _Blocks[i] )
			{
				free(_Blocks[i]);            
			}
		}
		free(_Blocks);
		free(_Blocks_record);
	}
	
	void* _alloc(UINT32 size);  
	void  _free(void* pobj , UINT32 size );  
	UINT32 _get_emptycnt();

	void reset() 
	{	
	       CAutoLock lock(MempoolMutex);
		   
       	for( UINT32 i=0; i<_BLOCK_NUM; i++ )
		{			   
		     *(_Blocks+i) = *(_Blocks_record+i);
		}	
	       _BlockIndex = 0;
		_BLOCK_EMPTYBUF_COUNT = _BLOCK_NUM;
		 	
	}
	UINT32 _BlockIndex;
private:
	void _alloc_blocks(); 
	void _realloc_blocks(UINT32 num);  
private:
	T**  _Blocks;
	T**  _Blocks_record;//structure to record mempool buffers' pointer, used for reset mempool
	UINT32 _BLOCK_SIZE;
	UINT32 _BLOCK_NUM;		
	UINT32 _BLOCK_EMPTYBUF_COUNT;
	CMutex MempoolMutex;
	
};

template<class T>
void CMemPool<T>:: _alloc_blocks()
{
	_Blocks = (T* *)malloc( sizeof(T*)*_BLOCK_NUM );
	_Blocks_record = (T* *)malloc( sizeof(T*)*_BLOCK_NUM );
	for( UINT32 i=0; i<_BLOCK_NUM; i++ )
	{
		T* p = static_cast<T*>(malloc(_BLOCK_SIZE));
		*(_Blocks_record+i)=*(_Blocks+i)=p;
	}
	_BlockIndex = 0;
}

template<class T>
UINT32 CMemPool<T>:: _get_emptycnt()
{
    CAutoLock lock(MempoolMutex);
    return _BLOCK_EMPTYBUF_COUNT;
}

template<class T>
void CMemPool<T>::_realloc_blocks(UINT32 num)
{
	T** _Blocks_new1;
	T** _Blocks_new2;
	_Blocks_new1 = (T* *)malloc(sizeof(T*)*(_BLOCK_NUM+num));
	_Blocks_new2 = (T* *)malloc(sizeof(T*)*(_BLOCK_NUM+num));
	memcpy(_Blocks_new1, _Blocks, _BlockIndex);
	memcpy(_Blocks_new1, _Blocks_record, _BlockIndex);
	free(_Blocks);
	free(_Blocks_record);
	_Blocks = _Blocks_new1;
	_Blocks_record =_Blocks_new2;
	for( UINT32 i= 0; i<num; i++ )
	{
		T* p = static_cast<T*>(malloc(_BLOCK_SIZE));
		*(_Blocks_record + _BLOCK_NUM+i) = *(_Blocks+_BLOCK_NUM+i)=p;
	}
	_BLOCK_NUM += num;      
}

template<class T>
void* CMemPool<T>::_alloc(UINT32 size)
{
       CAutoLock lock(MempoolMutex);
       UINT32 scanCount = _BLOCK_NUM;
	if( size > _BLOCK_SIZE )
 	{
                LOGE("cmmbSP - mempool alloc >_BLOCK_SIZE size = %d", size);
		T* p = static_cast<T*>(malloc(size));
                LOGE("cmmbSP - mempool alloc >_BLOCK_SIZE pobj= %x", p);
		return  p; 
	}                
	if( _BlockIndex > _BLOCK_NUM ) 
	{
	       LOGE("cmmbSP - mempool alloc fail for _BlockIndex big,  = %d", _BlockIndex);
		return null;
	}
	if(0 == _BLOCK_EMPTYBUF_COUNT)
	{
	       LOGE("cmmbSP - mempool alloc fail for _BLOCK_EMPTYBUF_COUNT is zero");
		return null;
	}
	if ( _BlockIndex == _BLOCK_NUM )
	{
	       LOGE("cmmbSP - mempool alloc maybe error _BlockIndex is _BLOCK_NUM");
		_BlockIndex = _BLOCK_NUM - 1;
	}
	while(scanCount)
	{
	     if(null != _Blocks[_BlockIndex])
	     {
              	T* p =  _Blocks[_BlockIndex];
              	_Blocks[_BlockIndex] = null;     
              	_BlockIndex ++ ;
			if(_BLOCK_EMPTYBUF_COUNT > 0)
				_BLOCK_EMPTYBUF_COUNT --;
              	return p;
	     }
	     _BlockIndex ++ ;
	     if( _BlockIndex >= _BLOCK_NUM ) 
		  _BlockIndex = 0;
	     scanCount --;
	}
	LOGE("cmmbSP - mempool alloc fail for scan fail");
	return null;

}

template<class T>
void CMemPool<T>::_free(void* pobj , UINT32 size )
{
       CAutoLock lock(MempoolMutex);
       UINT32 scanCount = _BLOCK_NUM;
	if( pobj == null )
		return ;
	if( size > _BLOCK_SIZE )       
	{
                LOGE("cmmbSP - mempool free size >BLOCK_SIZE ptr = %x, size = %d", pobj, size);
		  if(0x10002 == (UINT32)(pobj))
		  {
		   	LOGE("cmmbSP - mempool free 0x10002 error!!!!!!!");
		  	return;
		  }
		free(pobj);       
                return;         
	}
			
	if (_BLOCK_NUM == _BLOCK_EMPTYBUF_COUNT)
	{
	      LOGE("cmmbSP - mempool free error becuase pool full size = %d, ptr = %x", size, pobj);
             return;
	}
	else if(0 == _BlockIndex)
	{
	      LOGE("cmmbSP - mempool free maybe error _BlockIndex is 0");
             _BlockIndex = 1;
	}
	else if(_BlockIndex > _BLOCK_NUM)
	{
             LOGE("cmmbSP - mempool error _BlockIndex >_BLOCK_NUM = %d", _BlockIndex);
	      _BlockIndex = _BLOCK_NUM;
	}
	else
	{
	      /*check buffer */
	      bool hit = false;
	      for( INT32 i = 0; i < _BLOCK_NUM; i ++)
	      {
	           if (pobj == (void *)(*(_Blocks_record+i)))
	           {
	                hit = true;
			  break;
	           }
	      }
	      if (false == hit)
	      {
	           LOGE("cmmbSP - mempool error free buffer addr = %x", pobj);
                  return;
	      }
	      
	      while(scanCount)
	      {
       	      if (null == _Blocks[_BlockIndex-1])
       	      {
       	           
       	           _BLOCK_EMPTYBUF_COUNT ++;
       		    _Blocks[_BlockIndex-1] = static_cast<T*>(pobj); 
       		     if(_BlockIndex > 0)
       		        _BlockIndex --;
       		     else
       			 _BlockIndex = 0;
       		    return;
       	      }
		      if(_BlockIndex > 1)
		        _BlockIndex --;
		      else
			  _BlockIndex = _BLOCK_NUM;
		      scanCount --;
	      }
	      LOGE("cmmbSP - mempool error can't free buffer, size = %d", size);
		  
		//_Blocks[_BlockIndex-1] = static_cast<T*>(pobj); 
		//_BlockIndex --;
	}
}


#endif // CMMB_MEM_POOL_H