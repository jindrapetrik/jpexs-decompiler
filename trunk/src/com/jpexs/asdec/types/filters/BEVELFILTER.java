package com.jpexs.asdec.types.filters;

import com.jpexs.asdec.types.RGBA;

/**
 * The Bevel filter creates a smooth bevel on display list objects.
 *
 * @author JPEXS
 */
public class BEVELFILTER extends FILTER {
    /**
     * Color of the shadow
     */
    public RGBA shadowColor;
    /**
     * Color of the highlight
     */
    public RGBA highlightColor;
    /**
     * Horizontal blur amount
     */
    public double blurX;
    /**
     * Vertical blur amount
     */
    public double blurY;
    /**
     * Radian angle of the drop shadow
     */
    public double angle;
    /**
     * Distance of the drop shadow
     */
    public double distance;
    /**
     * Strength of the drop shadow
     */
    public float strength;
    /**
     * Inner shadow mode
     */
    public boolean innerShadow;
    /**
     * Knockout mode
     */
    public boolean knockout;
    /**
     * Composite source
     */
    public boolean compositeSource;
    /**
     * OnTop mode
     */
    public boolean onTop;
    /**
     * Number of blur passes
     */
    public int passes;

    /**
     * Constructor
     */
    public BEVELFILTER() {
        super(3);
    }
}
