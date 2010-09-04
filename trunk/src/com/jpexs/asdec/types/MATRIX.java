package com.jpexs.asdec.types;

/**
 * Represents a standard 2x3 transformation matrix of the sort commonly used in 2D graphics
 *
 * @author JPEXS
 */
public class MATRIX {
    /**
     * Has scale values
     */
    public boolean hasScale;
    /**
     * X scale value
     */
    public double scaleX;
    /**
     * Y scale value
     */
    public double scaleY;
    /**
     * Has rotate and skew values
     */
    public boolean hasRotate;
    /**
     * First rotate and skew value
     */
    public double rotateSkew0;
    /**
     * Second rotate and skew value
     */
    public double rotateSkew1;
    /**
     * X translate value in twips
     */
    public long translateX;
    /**
     * Y translate value in twips
     */
    public long translateY;

    /**
     * Nbits used for store translate values
     */
    public int translateNBits;
    /**
     * Nbits used for store scale values
     */
    public int scaleNBits;
    /**
     * Nbits used for store rotate values
     */
    public int rotateNBits;
}
