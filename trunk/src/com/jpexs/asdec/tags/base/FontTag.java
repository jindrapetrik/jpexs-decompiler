package com.jpexs.asdec.tags.base;

import com.jpexs.asdec.types.SHAPE;

/**
 *
 * @author JPEXS
 */
public interface FontTag extends AloneTag {

   public int getFontId();

   public SHAPE[] getGlyphShapeTable();
}
