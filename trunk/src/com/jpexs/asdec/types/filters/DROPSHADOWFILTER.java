package com.jpexs.asdec.types.filters;

import com.jpexs.asdec.types.RGBA;

/**
 * Drop shadow filter based on the same median filter as the blur filter
 *
 * @author JPEXS
 */
public class DROPSHADOWFILTER extends FILTER {
    /**
     * Color of the shadow
     */
    public RGBA dropShadowColor;
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
     * Number of blur passes
     */
    public int passes;

    /**
     * Constructor
     */
    public DROPSHADOWFILTER() {
        super(0);
    }
}
