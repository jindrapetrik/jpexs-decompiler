package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;

/**
 *
 * @author JPEXS
 */
public interface IggyFlashHeaderInterface extends StructureInterface {

    public long getXMin();

    public long getYMin();

    public long getXMax();

    public long getYMax();

    public float getFrameRate();
}
