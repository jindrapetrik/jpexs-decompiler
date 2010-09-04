package com.jpexs.asdec.types.filters;

import com.jpexs.asdec.types.RGBA;

/**
 * Bevel filter with gradient instead of single color
 *
 * @author JPEXS
 */
public class GRADIENTBEVELFILTER extends FILTER {
    /**
     * Gradient colors
     */
    public RGBA gradientColors[];
    /**
     * Gradient ratios
     */
    public int gradientRatio[];
    /**
     * Horizontal blur amount
     */
    public double blurX;
    /**
     * Vertical blur amount
     */
    public double blurY;
    /**
     * Radian angle of the gradient bevel
     */
    public double angle;
    /**
     * Distance of the gradient bevel
     */
    public double distance;
    /**
     * Strength of the gradient bevel
     */
    public float strength;
    /**
     * Inner bevel mode
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

    public GRADIENTBEVELFILTER() {
        super(7);
    }
}