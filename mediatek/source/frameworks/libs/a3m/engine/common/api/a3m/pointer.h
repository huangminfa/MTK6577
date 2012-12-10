/**************************************************************************
 *
 * Copyright (c) 2010 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 ***************************************************************************/
/** \file
 * Smart pointer definitions
 *
 */
#pragma once
#ifndef A3M_POINTER_H
#define A3M_POINTER_H

/******************************************************************************
 * Include Files
 ******************************************************************************/
#include <a3m/assert.h>             /* included for A3M_ASSERT */
#include <a3m/base_types.h>         /* included for A3M_INT32 */
#include <a3m/noncopyable.h>        /* included for NonCopyable */
#include <algorithm>                /* for std::swap */


namespace a3m
{
  /**
   * \defgroup a3mPointers Smart Pointers
   * \ingroup  a3mRefTypes
   *
   * Smart Pointers offer a way to automatically manage object lifetimes. A
   * SharedPtr stores a pointer to a dynamically allocated object (typically
   * alocated using a new expression). The object pointed to is guaranteed to
   * be deleted when the last SharedPtr pointing to it is destroyed (or set to
   * point to something else).
   *
   * These pointers are designed for use within the A3M and by A3M API users.
   *
   * A class must inherit from the class Shared in order to declare a SharedPtr
   * for that class.
   *
   * Because the implementation uses reference counting, cycles of SharedPtr
   * instances will not be reclaimed. For example, if main() holds a SharedPtr
   * to A, which directly or indirectly holds a SharedPtr back to A, A's use
   * count will be 2. Destruction of the original SharedPtr will leave A
   * dangling with a use count of 1.
   *
   * SharedPtr<T> can be implicitly converted to SharedPtr<U> whenever T* can
   * be implicitly converted to U*. In particular, SharedPtr<T> is implicitly
   * convertible to SharedPtr<T const>, to SharedPtr<U> where U is an
   * accessible base of T, and to SharedPtr<void>.
   *
   *
   * Example usage:
   *
   *\code
   *
   * class MyBase : public Shared
   * {
   * public:
   *   virtual ~MyBase();   // It is important that MyBase has a virtual destructor
   *                        // otherwise when objects of derived classes are destroyed
   *                        // through base class pointers the wrong destructor will be
   *                        // called
   * };
   *
   * class MyClass
   * {
   * public:
   *   void foo();
   * };
   *
   * void bar( MyClass &myClass );
   *
   * SharedPtr<MyClass> p = new MyClass; // Initialise a shared pointer with
   *                                     // a raw pointer. Best to do this as
   *                                     // soon as you create an object.
   *
   * p->foo();    // Use it as you would an ordinary pointer,
   * bar( *p );
   *
   * SharedPtr<MyBase> pBase = p;       // This is ok because a pointer to MyClass
   *                                    // can be implicitly cast to a pointer
   *                                    // to MyBase.
   *
   *
   * p = SharedPtr<MyClass>();          // p has been reset to null, but the object still
   *                                    // exists because pBase is still pointing to it.
   * pBase = SharedPtr<MyBase>();       // The object will now be destroyed because there
   *                                    // are no more SharedPtrs pointing to it.
   *
   * \endcode
   * @{
   */

/** Class name macro
 * Use this macro in classes derived from Shared to identify the class at
 * compile time, using getClassNameStatic() or runtime using getClassName()
 *
 * This name is used to identify the class of pointer when an attempt is made
 * to dereference a null pointer. This provides more information when debugging.
 *
 * If you do not use this macro, the name will be reported as "Shared"
 *
 * Example:
 * \code
 * class MyClass : public Shared
 * {
 * public:
 *   A3M_NAME_SHARED_CLASS( MyClass )
 *   // Other members etc.
 * };
 *  \endcode
 */
#define A3M_NAME_SHARED_CLASS( name ) \
    static A3M_CHAR8 const *getClassNameStatic() {return #name;} \
    virtual A3M_CHAR8 const *getClassName() const {return getClassNameStatic();}

   /** Shared is a base class for any objects which need to be pointed to by
   * SharedPtrs.
   */
  class Shared
  {
  public:
    /** Default constructor */
    Shared() : m_SharedCount( 0 ) {}
    /** Copy constructor */
    Shared( Shared const &other /**< [in] other object */ ) : m_SharedCount( 0 ) {}

    /** virtual destructor */
    virtual ~Shared() {}

    /** Assignment operator
     * \return reference to the assignee
     */
    Shared &operator=( Shared const &other /**< [in] other object */)
    /* Don't copy the count from the other object */
    {
      return *this;
    }

    /** Get class name (static version)
     * \return name of this class (at compile time)
     */
    static char const *getClassNameStatic() {return "Shared";}

    /** Get class name (virtual version)
     * \return name of this class (at run time)
     */
    virtual char const *getClassName() const {return getClassNameStatic();}

    template< typename T >
    friend class SharedPtr;
  private:
    A3M_UINT32 sharedGetCount() {return m_SharedCount;}
    void sharedIncCount() {++m_SharedCount;}
    void sharedDecCount() {--m_SharedCount;}
    A3M_UINT32 m_SharedCount;
  };

  /** An intrusive (the object count is part of the object) shared pointer type.
   * Any type derived from Shared may be used as a pointee.
   */
  template< typename T >
  class SharedPtr
  {
  public:
    /** Type pointed to */
    typedef T ElementType;

    /** Constructor.
     * May fail to allocate count helper object, in which case object points to
     * NULL - can test with "if( !objectPtr.get() )"
     */
    explicit SharedPtr( T *p = 0 /**< [in] raw pointer */ );

    /** Destructor.
     * If this was the last SharedPtr pointing to the object then delete the
     * object
     */
    ~SharedPtr();

    /** Templated copy constructor.
     * Shares the object pointed to by the other pointer. This constructor works
     * with any pointer type that can be implicitly cast to a pointer to T
     * (i.e. a pointer to a base class of T).
     */
    template<class U>
    SharedPtr( SharedPtr<U> const &other /**< [in] another pointer */ );

    /** Copy constructor for exact matches.
     * Shares the object pointed to by the other pointer.
     */
    SharedPtr( SharedPtr const &other /**< [in] another pointer */ );

    /** Assignment operator.
     * Shares the object pointed to by the other pointer.
     * \return reference to this pointer
     */
    template<class U>
    SharedPtr &operator=( SharedPtr<U> const &other
                          /**< [in] RHS of assignment */);

    /** Assignment operator for exact matches.
     * Shares the object pointed to by the other pointer.
     * \return reference to this pointer
     */
    SharedPtr &operator=( SharedPtr const &other
                          /**< [in] RHS of assignment */);

    /** Set this pointer to NULL.
     * The object's (shared) count is reduced accordingly.*/
    void reset();

    /** Set this pointer to point to a different object.
     * The previous object's (shared) count is reduced accordingly.*/
    void reset( T *p /**< new raw pointer */ );

    /** Swap operation.
     * Swaps this pointer with another of the same type - fast and safe.
     */
    void swap( SharedPtr &other /**< [in] pointer to swap with */);

    /** Get the actual pointed to object.
     * \return raw pointer to object.
     */
    T *get() const;

    /** Pointer operator.
     * This pointer should be pointing to something when you use this operator.
     * In debug mode using this operator with a NULL pointer will ASSERT.
     * \return pointer to object.
     */
    T *operator->() const;

    /** Indirection operator.
     * This pointer should be pointing to something when you use this operator.
     * In debug mode using this operator with a NULL pointer will ASSERT.
     * \return reference to object.
     */
    T &operator*() const;

    /** Test if the object is pointed to by any other pointers.
     * \return true if this is the only pointer pointing to the object.
     */
    A3M_BOOL isUnique() const;

    /** Type used in safe bool idiom.
     */
    typedef void (SharedPtr<T>::*BoolType)() const;

    /** Safe boolean conversion function.
     * Allows the pointer to be tested as a boolean, without side effects.
     */
    operator BoolType() const;

    template<class Y> friend class SharedPtr;

  private:
    /** Dummy function used in safe bool idiom.
     */
    void type_does_not_support_comparisons() const;

    T *m_ptr;
  };

  /** Equality operator.
   * \return true if both pointers point to the same object
   */
  template<class T, class U>
  A3M_BOOL operator==( SharedPtr<T> const & a, /**< [in] first pointer */
                   SharedPtr<U> const & b  /**< [in] second pointer */);

  /** Inequality operator.
   * \return true if pointers point to different objects
   */
  template<class T, class U>
  A3M_BOOL operator!=( SharedPtr<T> const & a, /**< [in] first pointer */
                   SharedPtr<U> const & b  /**< [in] second pointer */);

  /** A simple smart pointer which deletes its object when the pointer is
   * deleted. It can be used with any type which has a public destructor.
   */
  template< typename T >
  class ScopedPtr : public NonCopyable
  {
  public:
    /** Type pointed to */
    typedef T ElementType;

    /** Constructor.
     * May fail to allocate count helper object, in which case object points to
     * NULL - can test with "if( !objectPtr.get() )"
     */
    explicit ScopedPtr( T *p /**< [in] raw pointer */ );

    /** Destructor.
     * If this was the last SharedPtr pointing to the object then delete the
     * object
     */
    ~ScopedPtr();
    /** Get the actual pointed to object.
     * \return raw pointer to object.
     */
    T *get() const;

    /** Pointer operator.
     * This pointer should be pointing to something when you use this operator.
     * In debug mode using this operator with a NULL pointer will ASSERT.
     * \return pointer to object.
     */
    T *operator->() const;

    /** Indirection operator.
     * This pointer should be pointing to something when you use this operator.
     * In debug mode using this operator with a NULL pointer will ASSERT.
     * \return reference to object.
     */
    T &operator*() const;

    /** Type used in safe bool idiom.
     * Allows the pointer to be tested as a boolean, without side effects.
     */
    typedef void (ScopedPtr<T>::*BoolType)() const;

    /** Safe boolean conversion function.
     */
    operator BoolType() const;

  private:
    /** Dummy function used in safe bool idiom.
     */
    void type_does_not_support_comparisons() const;

    T *m_ptr;
  };

  /** Safe bool illegal equality operator.
   * Ensures that two ScopedPtr objects cannot be compared.
   * \return Return value is irrelevent, as the function will never compile
   */
  template<class T, class U>
  A3M_BOOL operator==( ScopedPtr<T> const & a, /**< [in] first pointer */
                   ScopedPtr<U> const & b  /**< [in] second pointer */);

  /** Safe bool illegal inequality operator.
   * Ensures that two ScopedPtr objects cannot be compared.
   * \return Return value is irrelevent, as the function will never compile
   */
  template<class T, class U>
  A3M_BOOL operator!=( ScopedPtr<T> const & a, /**< [in] first pointer */
                   ScopedPtr<U> const & b  /**< [in] second pointer */);

/******************************************************************************
 * Implementation - SharedPtr
 ******************************************************************************/

  /*
   * Constructor from raw pointer.
   */
  template< class T >
  SharedPtr<T>::SharedPtr( T *p )
  : m_ptr( p )
  {
    if( m_ptr )
    {
      m_ptr->sharedIncCount();
    }
  }

  /*
   * Destructor.
   */
  template< class T >
  SharedPtr<T>::~SharedPtr()
  {
    if( m_ptr )
    {
      /* Shared count should not have reached 0 yet */
      A3M_ASSERT( m_ptr->sharedGetCount() != 0 );

      m_ptr->sharedDecCount();

      /* if shared count has reached 0 we can delete the object */
      if( m_ptr->sharedGetCount() == 0 ) { delete m_ptr; }
    }
  }


  /*
   * Templated copy constructor.
   */
  template< class T > template< class U >
  SharedPtr<T>::SharedPtr( SharedPtr<U> const &other )
  : m_ptr( other.get() )
  {
    if( m_ptr )
    {
      /* if m_ptr != 0 then m_count != 0 */
      A3M_ASSERT( m_ptr->sharedGetCount() != 0 );
      m_ptr->sharedIncCount();
    }
  }

  /*
   * Copy constructor.
   */
  template< class T >
  SharedPtr<T>::SharedPtr( SharedPtr<T> const &other )
  : m_ptr( other.m_ptr )
  {
    if( m_ptr )
    {
      /* if m_ptr != 0 then m_count != 0 */
      A3M_ASSERT( m_ptr->sharedGetCount() != 0 );
      m_ptr->sharedIncCount();
    }
  }

  /*
   * Templated assignment operator.
   */
  template< class T > template< class U >
  SharedPtr<T> &SharedPtr<T>::operator=( SharedPtr<U> const &other )
  {
    SharedPtr<T> temp( other );
    swap( temp );
    return *this;
  }

  /*
   * Assignment operator.
   */
  template< class T >
  SharedPtr<T> &SharedPtr<T>::operator=( SharedPtr const &other )
  {
    SharedPtr<T> temp( other );
    swap( temp );
    return *this;
  }

  /*
   * Swap with other pointer.
   */
  template< class T >
  void SharedPtr<T>::swap( SharedPtr<T> &other )
  {
    std::swap( other.m_ptr, m_ptr );
  }

  /* Set this pointer to NULL.
   * The object's count is reduced accordingly.
   */
  template< class T >
  void SharedPtr<T>::reset()
  {
    SharedPtr<T>().swap( *this );
  }

  /* Set this pointer to point to a different object.
   * The previous object's (shared) count is reduced accordingly.
   */
  template< class T >
  void SharedPtr<T>::reset( T *p )
  {
    SharedPtr<T>(p).swap( *this );
  }

  /*
   * Get the raw pointer.
   */
  template< class T >
  T *SharedPtr<T>::get() const
  {
    return m_ptr;
  }

  /*
   * Pointer operator
   */
  template< class T >
  T *SharedPtr<T>::operator->() const
  {
    if( !m_ptr )
    {
      A3M_LOG_ERROR( "Error: pointer operator on null. class: %s",
                     T::getClassNameStatic() );
    }
    A3M_ASSERT( m_ptr );
    return m_ptr;
  }

  /*
   * Indirection operator
   */
  template< class T >
  T &SharedPtr<T>::operator*() const
  {
    if( !m_ptr )
    {
      A3M_LOG_ERROR( "Error: null shared pointer dereference. class: %s",
                     T::getClassNameStatic() );
    }
    A3M_ASSERT( m_ptr );
    return *m_ptr;
  }


  /*
   * Test if the object is pointed to by any other pointers.
   */
  template< class T >
  A3M_BOOL SharedPtr<T>::isUnique() const
  {
    if( !m_ptr ) return true;
    return ( m_ptr->sharedGetCount() == 1 );
  }

  template< class T >
  void SharedPtr<T>::type_does_not_support_comparisons() const
  {
  }

  template< class T >
  SharedPtr<T>::operator BoolType() const
  {
    return m_ptr == 0 ? 0 : &SharedPtr<T>::type_does_not_support_comparisons;
  }

  /*
   * Equality operator
   */
  template<class T, class U>
  inline A3M_BOOL operator==( SharedPtr<T> const & a, SharedPtr<U> const & b )
  {
      return a.get() == b.get();
  }

  /*
   * Inequality operator
   */
  template<class T, class U>
  inline A3M_BOOL operator!=( SharedPtr<T> const & a, SharedPtr<U> const & b )
  {
      return a.get() != b.get();
  }

/******************************************************************************
 * Implementation - ScopedPtr
 ******************************************************************************/

  /*
   * Constructor from raw pointer.
   */
  template< typename T >
  ScopedPtr<T>::ScopedPtr( T *p /* raw pointer */ ) : m_ptr( p ) {}

  /*
   * Destructor.
   */
  template< typename T >
  ScopedPtr<T>::~ScopedPtr() { delete m_ptr; }

  /*
   * Get the actual pointed to object.
   */
  template< typename T >
  T *ScopedPtr<T>::get() const { return m_ptr; }

  /*
   * Pointer operator.
   */
  template< typename T >
  T *ScopedPtr<T>::operator->() const
  {
    A3M_ASSERT( m_ptr );
    return m_ptr;
  }

  /*
   * Indirection operator.
   */
  template< typename T >
  T &ScopedPtr<T>::operator*() const
  {
    A3M_ASSERT( m_ptr );
    return *m_ptr;
  }

  template< class T >
  void ScopedPtr<T>::type_does_not_support_comparisons() const
  {
  }

  template< class T >
  ScopedPtr<T>::operator BoolType() const
  {
    return m_ptr == 0 ? 0 : &ScopedPtr<T>::type_does_not_support_comparisons;
  }

  template<class T, class U>
  inline A3M_BOOL operator==( ScopedPtr<T> const & a, ScopedPtr<U> const & b )
  {
    // Call private function to cause compilation error.
    a.type_does_not_support_comparisons();
    return A3M_FALSE;
  }

  template<class T, class U>
  inline A3M_BOOL operator!=( ScopedPtr<T> const & a, ScopedPtr<U> const & b )
  {
    // Call private function to cause compilation error.
    a.type_does_not_support_comparisons();
    return A3M_FALSE;
  }

  /** @} */

} /* namespace a3m */

#endif /* A3M_POINTER_H */
