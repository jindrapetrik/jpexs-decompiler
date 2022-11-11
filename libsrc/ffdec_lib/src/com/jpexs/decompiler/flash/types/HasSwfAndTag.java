package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.Tag;

/**
 *
 * @author JPEXS
 */
public interface HasSwfAndTag {
    public void setSwfAndTag(SWF swf, Tag tag);
    
    public SWF getSwf();
    
    public Tag getTag();
}
