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

#ifndef _smart_ptr_for_object_c_
#define _smart_ptr_for_object_c_

template<class T> inline void checked_delete(T * x)
{
    // intentionally complex - simplification causes regressions
    typedef char type_must_be_complete[ sizeof(T)? 1: -1 ];
    (void) sizeof(type_must_be_complete);
    delete x;
}

template<class T> class scoped_ptr // noncopyable
{

private:

	T * ptr;

	explicit scoped_ptr(scoped_ptr const &);
	scoped_ptr & operator=(scoped_ptr const &);

	typedef scoped_ptr<T> this_type;

	void operator==( scoped_ptr const& ) const;
	void operator!=( scoped_ptr const& ) const;

public:

	typedef T element_type;

	explicit scoped_ptr(T * p = 0): ptr(p) // never throws
	{
	}


	~scoped_ptr() // never throws
	{
		// intentionally complex - simplification causes regressions
		checked_delete(ptr);
	}

	void reset(T * p = 0) // never throws
	{
		ASSERT(p == 0 || p != ptr); // catch self-reset errors
		this_type(p).swap(*this);
	}

	T & operator*() const // never throws
	{
		ASSERT( ptr != 0 );
		return *ptr;
	}

	T * operator->() const // never throws
	{
		ASSERT( ptr != 0 );
		return ptr;
	}


	T * get() const // never throws
	{
		return ptr;
	}

	void swap(scoped_ptr & b) // never throws
	{
		T * tmp = b.ptr;
		b.ptr = ptr;
		ptr = tmp;
	}
};

template<class T> inline void swap(scoped_ptr<T> & a, scoped_ptr<T> & b) // never throws
{
    a.swap(b);
}

// get_pointer(p) is a generic way to say p.get()

template<class T> inline T * get_pointer(scoped_ptr<T> const & p)
{
    return p.get();
}





namespace noncopyable_  // protection from unintended ADL
{
  class noncopyable
  {
   protected:
      noncopyable() {}
      ~noncopyable() {}
   private:  // emphasize the following members are private
      noncopyable( const noncopyable& );
      const noncopyable& operator=( const noncopyable& );
  };
};

typedef noncopyable_::noncopyable noncopyable;

template <typename T>
struct singleton
{
private:
	singleton();
    struct creator
    {
        inline void noaction() const { }
		creator() {
			singleton<T>::instance();
		}
    };
    static creator create_obj;
	
public:
    typedef T type;
    static inline type &instance()
    {
		static type obj;
		create_obj.noaction();
		return obj;
    }
};

template <typename T>
typename singleton<T>::creator singleton<T>::create_obj;

template<class T, class U> 
class Conversion 
{ 
	typedef char Small; 
	class Big { char dummy[2]; }; 
	static Small Test(U); 
	static Big Test(...); 
	static T MakeT(); 
public: 
	enum { exists = 
		sizeof(Test(MakeT())) == sizeof(Small) }; 
};

//if (Conversion<double, int>::exists) do_whatever;

#endif
