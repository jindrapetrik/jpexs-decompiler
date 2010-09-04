package com.jpexs.asdec.types.filters;

import com.jpexs.asdec.types.RGBA;

/**
 * Glow filter with gradient instead of single color
 *
 * @author JPEXS
 */
public class GRADIENTGLOWFILTER extends FILTER {
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
     * Radian angle of the gradient glow
     */
    public double angle;
    /**
     * Distance of the gradient glow
     */
    public double distance;
    /**
     * Strength of the gradient glow
     */
    public float strength;
    /**
     * Inner glow mode
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
    public GRADIENTGLOWFILTER() {
        super(4);
    }
}