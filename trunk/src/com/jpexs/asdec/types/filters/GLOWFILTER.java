package com.jpexs.asdec.types.filters;

import com.jpexs.asdec.types.RGBA;

/**
 * Glow filter
 *
 * @author JPEXS
 */
public class GLOWFILTER extends FILTER {
    /**
     * Color of the shadow
     */
    public RGBA glowColor;
    /**
     * Horizontal blur amount
     */
    public double blurX;
    /**
     * Vertical blur amount
     */
    public double blurY;
    /**
     * Strength of the glow
     */
    public float strength;
    /**
     * Inner glow mode
     */
    public boolean innerGlow;
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
    public GLOWFILTER() {
        super(2);
    }
}
