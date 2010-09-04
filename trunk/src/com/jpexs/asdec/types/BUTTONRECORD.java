package com.jpexs.asdec.types;

import com.jpexs.asdec.types.filters.FILTER;

import java.util.List;

/**
 * Defines a character to be displayed in one or more button states.
 *
 * @author JPEXS
 */
public class BUTTONRECORD {
    /**
     * @since SWF 8
     *        Has blend mode?
     */
    public boolean buttonHasBlendMode;
    /**
     * @since SWF 8
     *        Has filter list?
     */
    public boolean buttonHasFilterList;
    /**
     * Present in hit test state
     */
    public boolean buttonStateHitTest;
    /**
     * Present in down state
     */
    public boolean buttonStateDown;
    /**
     * Present in over state
     */
    public boolean buttonStateOver;
    /**
     * Present in up state
     */
    public boolean buttonStateUp;
    /**
     * ID of character to place
     */
    public int characterId;
    /**
     * Depth at which to place character
     */
    public int placeDepth;
    /**
     * Transformation matrix for character placement
     */
    public MATRIX placeMatrix;
    /**
     * If within DefineButton2Tag: Character color transform
     */
    public CXFORMWITHALPHA colorTransform;
    /**
     * If within DefineButton2Tag and buttonHasFilterList: List of filters on this button
     */
    public List<FILTER> filterList;
    /**
     * If within DefineButton2Tag and buttonHasBlendMode: Blend mode
     */
    public int blendMode;
}
