package com.jpexs.asdec.types.filters;

/**
 * Applies a color transformation on the pixels of a display list object
 *
 * @author JPEXS
 */
public class COLORMATRIXFILTER extends FILTER {
    /**
     * Color matrix values
     */
    public float matrix[] = new float[20];

    /**
     * Constructor
     */
    public COLORMATRIXFILTER() {
        super(6);
    }
}
