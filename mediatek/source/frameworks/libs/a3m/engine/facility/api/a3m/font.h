/*****************************************************************************
 *
 * Copyright (c) 2011 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 *****************************************************************************/
/** \file
 * Font class
 *
 */
#pragma once
#ifndef A3M_FONT_H
#define A3M_FONT_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/texture2d.h>   /* Texture2D */

namespace a3m
{
  /** \defgroup a3mFont Font support
   * \ingroup  a3mRefText
   * \ingroup  a3mRefAssets
   *
   * Font encapsulates a typeface at a given size.
   *
   * Fonts are assets and are therefore created and managed by a FontCache.
   *
   * \note
   * Generally, the client will want to use AssetCachePool to create and manage
   * asset caches, rather than creating them manually.
   *
   * Example:
   * \code
   * // Fonts use Texture2D objects, so we create a Texture2DCache first
   * Texture2DCache::Ptr textureCache( new Texture2DCache() );
   *
   * // Create a font cache and add a path from which to load fonts.
   * FontCache::Ptr cache( new FontCache( textureCache ) );
   * registerSource( *cache, ".\\assets\\fonts");
   *
   * // Register a loader for the cache to use
   * cache->registerLoader( myLoader );
   *
   * // Load Font of point size 12 from ".\assets\shaders"
   * Font::Ptr font = get( cache, "Droid-Sans.ttf", 12 );
   * \endcode
   *
   *  @{
   */

  // Forward declarations
  class FontCache;

  /** Font class.
   * A Font object contains a Texture2d object and a list of bounding boxes.
   */
  class Font : public Shared, NonCopyable
  {
  public:
    A3M_NAME_SHARED_CLASS( Font )

    /** Smart pointer type */
    typedef a3m::SharedPtr< Font > Ptr;

    /** Cache type for this class */
    typedef FontCache CacheType;

    /** CharacterData struct
     * The CharacterData structure describes the position of a characters
     * bitmap within the texture refered to by a font object. It also
     * specifies where a character should be drawn relative to the
     * preceding character in a string.
     */
    struct CharacterData
    {
      /** Default constructor.
       * This will be used by array constructors.
       */
      CharacterData()
      : left( 0.0f ), top( 0.0f ), right( 0.0f ), bottom( 0.0f ),
        xoff( 0.0f ), yoff( 0.0f ), xadvance( 0.0f ) {}

/*      ** Constructor
       * Convenience constructor initializes members
      CharacterData( A3M_FLOAT left, A3M_FLOAT top,
                     A3M_FLOAT right, A3M_FLOAT bottom,
                     A3M_FLOAT xoff, A3M_FLOAT yoff, A3M_FLOAT xadvance )
      : left( left ), top( top ), right( right ), bottom( bottom ),
        xoff( xoff ), yoff( yoff ), xadvance( xadvance ) {}
*/
      A3M_FLOAT left;     /**< left of bounding box in pixels */
      A3M_FLOAT top;      /**< top of bounding box in pixels */
      A3M_FLOAT right;    /**< right of bounding box in pixels */
      A3M_FLOAT bottom;   /**< bottom of bounding box in pixels */
      A3M_FLOAT xoff;     /**< x offset before drawing this character */
      A3M_FLOAT yoff;     /**< y offset before drawing this character */
      A3M_FLOAT xadvance; /**< x offset after drawing this character */
    };

    /** Destructor
     */
    ~Font();

    /** Get texture.
     * Returns a shared pointer to the texture used by this Font.
     * \return this font's texture
     */
    Texture2D::Ptr getTexture() const { return m_texture; }

    /** Get bounding box.
     * Returns the bounding box for the given character. If the given character
     * is outside the range of characters in the font, a zero-sized bounding box
     * is returned.
     * \return bounding box for the given character.
     */
    CharacterData const &getCharacterData( A3M_INT32 character
      /**< [in] character for which to return a bounding box */ ) const;

    /** Get font size.
     * \return size (height) of largest character in font.
     */
    A3M_INT32 getSize() const { return m_size; }

    /** Get ascent distance.
     * \return distance from baseline to top of tallest characters.
     */
    A3M_FLOAT getAscent() const { return m_ascent; }

    /** Get descent distance.
     * \return distance from baseline to bottom of character 'tails'.
     */
    A3M_FLOAT getDescent() const { return m_descent; }

    /** Get line gap distance.
     * \return gap between lines.
     */
    A3M_FLOAT getLineGap() const { return m_lineGap; }

    /** Maximum length of a Font filename */
    static const A3M_UINT32 MAX_NAME_LENGTH = 64;

  private:
    friend class FontCache; /* Is Font's factory class */

    /** Private constructor.
     * This constructor is called by FontCache.
     */
    Font(
        Texture2D::Ptr const &texture, /**< Texture containing character
                                         shapes */
        A3M_INT32 size, /**< Size of font in pixels */
        A3M_FLOAT ascent, /**< Height of font from baseline to top of
                            character */
        A3M_FLOAT descent, /**< Distance from baseline to bottom of
                             descenders */
        A3M_FLOAT lineGap, /**< Gap between lines */
        A3M_INT32 firstCharacter, /**< ASCII code of first character in the
                                    font */
        A3M_INT32 nCharacters, /**< Number of characters in the font */
        CharacterData const *characterData /**< Array of bounding boxes for
                                             each character in the font */);

    Texture2D::Ptr m_texture;
    A3M_INT32 m_size;
    A3M_FLOAT m_ascent;
    A3M_FLOAT m_descent;
    A3M_FLOAT m_lineGap;
    A3M_INT32 m_firstCharacter;
    A3M_INT32 m_nCharacters;
    CharacterData *m_characterData;
  };

  /**
   * AssetCache specialised for storing and creating Font assets.
   * Font asset names should be of the format <b>"fontname:size"</b> (e.g.
   * <b>"verdana.ttf:12"</b>), where fontname is the name of the font to load
   * (<b>verdana.ttf</b>), and the size is the point size of the font
   * (<b>12</b>).  Utility functions a3m::get() and a3m::exists() are provided
   * for the usual case where the client has a separate font name string, and
   * numeric font size.
   */
  class FontCache : public AssetCache<Font>
  {
  public:
    /** Smart pointer type for this class */
    typedef SharedPtr< FontCache > Ptr;

    /** Constructor.
     * FontCache requires a TextureCache as Font assets contain Texture assets.
     */
    explicit FontCache(Texture2DCache::Ptr const& texture2DCache
                       /**< Texture cache */);

    /**
     * Constructs an Font from a Texture.
     * \return The font
     */
    Font::Ptr create(
        Texture2D::Ptr const &texture, /**< Texture containing character
                                         shapes */
        A3M_INT32 size, /**< Size of font in pixels */
        A3M_FLOAT ascent, /**< Height of font from baseline to top of
                            character */
        A3M_FLOAT descent, /**< Distance from baseline to bottom of
                             descenders */
        A3M_FLOAT lineGap, /**< Gap between lines */
        A3M_INT32 firstCharacter, /**< ASCII code of first character in the
                                    font */
        A3M_INT32 nCharacters, /**< Number of characters in the font */
        Font::CharacterData const *characterData, /**< Array of bounding boxes
                                                    for each character in the
                                                    font */
        A3M_CHAR8 const* name = 0 /**< Optional name to give the asset. If
                                    omitted, the asset will not be reachable
                                    via the AssetCache::get() function. */);

    /**
     * Returns the Texture2DCache owned by this cache.
     * FontCache creates Font assets, which have to create Texture2D assets,
     * and so must own a Texture2DCache.
     * \return The Texture2DCache
     */
    Texture2DCache::Ptr texture2DCache() const { return m_texture2DCache; }

  private:
    Texture2DCache::Ptr m_texture2DCache; /**< Texture cache */
  };

  /**
   * Loads or retrieves a Font from the cache, specifying a font name and point
   * size.
   * \copydetails AssetCache::get()
   */
  Font::Ptr get(
      FontCache& cache, /**< Cache to use to retrieve the font */
      A3M_CHAR8 const* name, /**< Name of the font */
      A3M_UINT32 size /**< Point size of the font */);

  /**
   * Reports whether a particular asset exists (is loaded) in the cache,
   * specifying a font name and point size.
   * \copydetails AssetCache::exists()
   */
  A3M_BOOL exists(
      FontCache& cache, /**< Cache in which to check */
      A3M_CHAR8 const* name, /**< Name of the font */
      A3M_UINT32 size /**< Point size of the font */);

  /** @} */

} /* end of namespace */

#endif /* A3M_FONT_H */
