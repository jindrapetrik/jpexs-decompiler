package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.CXFORM;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public interface PlaceObjectTypeTag {

    public int getCharacterId();

    public int getDepth();

    public MATRIX getMatrix();

    public String getInstanceName();

    public void setInstanceName(String name);

    public void setClassName(String className);

    public CXFORM getColorTransform();

    public CXFORMWITHALPHA getColorTransformWithAlpha();

    public int getBlendMode();

    public List<FILTER> getFilters();

    public int getClipDepth();

    public String getClassName();

    public boolean cacheAsBitmap();

    public boolean isVisible();

    public RGBA getBackgroundColor();

    public boolean flagMove();

    public int getRatio();

    public CLIPACTIONS getClipActions();
}
