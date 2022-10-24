package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.tags.base.NeedsCharacters;
import com.jpexs.helpers.ConcreteClasses;
import java.io.Serializable;

/**
 *
 * @author JPEXS
 */
@ConcreteClasses({LINESTYLE.class, LINESTYLE2.class})
public interface ILINESTYLE extends NeedsCharacters, Serializable {

    public int getNum();

    public RGB getColor();

    public int getWidth();

    public void setColor(RGB color);

    public void setWidth(int width);
}
