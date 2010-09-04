package com.jpexs.asdec.types.filters;

/**
 * Blur filter based on a sub-pixel precise median filter
 *
 * @author JPEXS
 */
public class BLURFILTER extends FILTER {
    /**
     * Horizontal blur amount
     */
    public double blurX;
    /**
     * Vertical blur amount
     */
    public double blurY;
    /**
     * Number of blur passes
     */
    public int passes;

    public BLURFILTER() {
        super(1);
    }
}
