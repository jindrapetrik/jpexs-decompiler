package com.jpexs.asdec.types.filters;

import com.jpexs.asdec.types.RGBA;

/**
 * Two-dimensional discrete convolution filter.
 *
 * @author JPEXS
 */
public class CONVOLUTIONFILTER extends FILTER {
    /**
     * Horizontal matrix size
     */
    public int matrixX;
    /**
     * Vertical matrix size
     */
    public int matrixY;
    /**
     * Divisor applied to the matrix values
     */
    public float divisor;
    /**
     * Bias applied to the matrix values
     */
    public float bias;
    /**
     * Matrix values
     */
    public float matrix[][];
    /**
     * Default color for pixels outside the image
     */
    public RGBA defaultColor;
    /**
     * Clamp mode
     */
    public boolean clamp;
    /**
     * Preserve the alpha
     */
    public boolean preserveAlpha;

    /**
     * Constructor
     */
    public CONVOLUTIONFILTER() {
        super(5);
    }
}
