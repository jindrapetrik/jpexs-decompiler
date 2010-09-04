package com.jpexs.asdec.types;

/**
 * Defines a transform that can be applied to the color space of a graphic object.
 *
 * @author JPEXS
 */
public class CXFORMWITHALPHA {
    /**
     * Has color addition values
     */
    public boolean hasAddTerms;
    /**
     * Has color multiply values
     */
    public boolean hasMultTerms;
    /**
     * Red multiply value
     */
    public int redMultTerm;
    /**
     * Green multiply value
     */
    public int greenMultTerm;
    /**
     * Blue multiply value
     */
    public int blueMultTerm;
    /**
     * Alpha multiply value
     */
    public int alphaMultTerm;
    /**
     * Red addition value
     */
    public int redAddTerm;
    /**
     * Green addition value
     */
    public int greenAddTerm;
    /**
     * Blue addition value
     */
    public int blueAddTerm;
    /**
     * Alpha addition value
     */
    public int alphaAddTerm;

    public int nbits;
}
