package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;

/**
 *
 * @author JPEXS
 */
public abstract class IggyTag implements StructureInterface {

    public abstract int getTagType();

    @Override
    public String toString() {
        return String.format("IggyTag (%04X)", getTagType());
    }
}
