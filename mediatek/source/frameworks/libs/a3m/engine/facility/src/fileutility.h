/*****************************************************************************
 *
 * Copyright (c) 2010 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 *****************************************************************************/
/** \file
 * Utility class to load a file into memory
 *
 */
#pragma once
#ifndef A3M_FILETOSTRING_H
#define A3M_FILETOSTRING_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <cstring>            /* strlen */
#include <a3m/base_types.h>   /* A3M base type defines           */
#include <a3m/noncopyable.h>  /* for NonCopyable                 */
#include <a3m/stream.h>       /* file IO                         */

namespace a3m
{
  /*
   * CharRange objects represent strings using begin and end pointers.
   * The begin pointer points to the start of the string and the end pointer
   * points to one-past-the-end of the string.
   * A CharRange object does not own the memory pointed to, but may modify it.
   */
  struct CharRange
  {
    CharRange() : begin( 0 ), end( 0 ) {}
    CharRange( A3M_CHAR8 *begin, A3M_CHAR8 *end )
      : begin( begin ), end( end ) {}
    CharRange( A3M_CHAR8 *begin )
      : begin( begin ), end( begin + strlen( begin ) ) {}

    A3M_BOOL empty() const {return begin == end;}
    A3M_CHAR8 front() const {return *begin;}
    CharRange &operator++() {++begin; return *this;}

    void nullTerminate() { *end = 0; }

    A3M_CHAR8 *begin;
    A3M_CHAR8 *end;
  };

  inline A3M_BOOL operator==( CharRange const &a, CharRange const &b )
  {
    return ( ( a.end - a.begin ) == ( b.end - b.begin ) ) &&
      std::equal( a.begin, a.end, b.begin );
  }

  inline A3M_BOOL operator==( CharRange const &a, A3M_CHAR8 const *b )
  {
    // If the characters are equal for the length of the CharRange, we just
    // need to check that the c string also ends at the same place.
    return std::equal( a.begin, a.end, b ) &&
           ( b[ a.end - a.begin ] == 0 );
  }

  inline A3M_BOOL operator!=( CharRange const &a, CharRange const &b )
  {
    return !operator==( a, b );
  }

  inline A3M_BOOL operator<( CharRange const &a, CharRange const &b )
  {
    return std::lexicographical_compare( a.begin, a.end, b.begin, b.end );
  }

  /*
   * Advance the begin pointer until it no longer points to a white space
   * character (as defined by isspace() or the range is empty.
   */
  void eatWhite( CharRange &range );

  /*
   * Returns true if the front of the range matches the given character and
   * advances the range (consumes the character).
   */
  A3M_BOOL requireChar( CharRange &range, A3M_INT32 ch );

  /*
   * Consume all characters at the start of the range until the given
   * character is reached.
   */
  void readTo( CharRange &range, A3M_INT32 ch );

  /*
   * Create a new range from the start of the given range (after whitespace
   * at the start of the range is consumed) until a whitespace character is
   * reached. Advances the range to the character after the first whitespace
   * character after the token.
   * CAUTION - the first whitespace character after the token is set to null
   * to allow the token to be used by a function expecting a null terminated
   * string.
   */
  CharRange readToken( CharRange &range );

  /*
   * Read a float from the start of the range. The range is advanced to the
   * first character that was not part of the float. If a float was not
   * found at the start of the range, the default value is returned.
   */
  A3M_FLOAT readFloat( CharRange &range, A3M_FLOAT def = 0.0f );

  /*
   * Read an int from the start of the range. The range is advanced to the
   * first character that was not part of the int. If a int was not
   * found at the start of the range, the default value is returned.
   */
  A3M_INT32 readInt( CharRange &range, A3M_INT32 def = 0 );

  /*
   * Read an unsigned int from the start of the range. The range is advanced
   * to the first character that was not part of the int. If a int was not
   * found at the start of the range, the default value is returned.
   */
  A3M_UINT32 readUInt( CharRange &range, A3M_UINT32 def );

  /*
   * Read an bool from the start of the range. A token is consumed from the
   * range. If the token starts with one of '1', 't', 'T' the function returns
   * A3M_TRUE.
   */
  A3M_BOOL readBool( CharRange &range );

  /*
   * FileToString class reads the given file into memory. The memory is owned
   * by the object and is released upon destruction.
   * The buffer is null terminated and can the object can be cast either to a
   * char * string or a CharRange.
   */
  class FileToString : NonCopyable
  {
  public:
    FileToString( A3M_CHAR8 const *filename );
    FileToString( Stream &stream );
    ~FileToString();

    A3M_CHAR8 const *get() const { return m_string.begin; }
    operator A3M_CHAR8 const *() const { return m_string.begin; }
    operator A3M_UINT8 const *() const
      { return reinterpret_cast< A3M_UINT8 const *> ( m_string.begin ); }
    operator CharRange() { return m_string; }

    A3M_INT32 length() const
    {
      return m_string.end - m_string.begin;
    }

  private:
    void read( Stream &stream );
    CharRange m_string;
  };

} /* namespace a3m */

#endif /* A3M_FILETOSTRING_H */
