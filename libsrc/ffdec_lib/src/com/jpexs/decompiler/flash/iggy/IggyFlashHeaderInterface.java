package com.jpexs.decompiler.flash.iggy;

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
