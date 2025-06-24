/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.Helper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Specifies one or more sprite events to which an event handler applies.
 *
 * @author JPEXS
 */
public class CLIPEVENTFLAGS implements Serializable {

    /**
     * Key up event
     */
    public boolean clipEventKeyUp;

    /**
     * Key down event
     */
    public boolean clipEventKeyDown;

    /**
     * Mouse up event
     */
    public boolean clipEventMouseUp;

    /**
     * Mouse down event
     */
    public boolean clipEventMouseDown;

    /**
     * Mouse move event
     */
    public boolean clipEventMouseMove;

    /**
     * Clip unload event
     */
    public boolean clipEventUnload;

    /**
     * Frame event
     */
    public boolean clipEventEnterFrame;

    /**
     * Clip load event
     */
    public boolean clipEventLoad;

    /**
     * Mouse drag over event
     * @since SWF 6
     */
    @Conditional(minSwfVersion = 6)
    public boolean clipEventDragOver;

    /**
     * Mouse rollout event
     * @since SWF 6
     */
    @Conditional(minSwfVersion = 6)
    public boolean clipEventRollOut;

    /**
     * Mouse rollover event
     * @since SWF 6
     */
    @Conditional(minSwfVersion = 6)
    public boolean clipEventRollOver;

    /**
     * Mouse release outside event
     * @since SWF 6
     */
    @Conditional(minSwfVersion = 6)
    public boolean clipEventReleaseOutside;

    /**
     * Mouse release inside event
     * @since SWF 6
     */
    @Conditional(minSwfVersion = 6)
    public boolean clipEventRelease;

    /**
     * Mouse press event
     * @since SWF 6
     */
    @Conditional(minSwfVersion = 6)
    public boolean clipEventPress;

    /**
     * Initialize event
     * @since SWF 6
     */
    @Conditional(minSwfVersion = 6)
    public boolean clipEventInitialize;

    /**
     * Data received event
     */
    public boolean clipEventData;

    /**
     * Reserved
     */
    @Reserved
    @SWFType(value = BasicType.UB, count = 5)
    @Conditional(minSwfVersion = 6)
    public int reserved;

    /**
     * Construct event
     * @since SWF 7
     */
    @Conditional(minSwfVersion = 7) //in v 6 always 0
    public boolean clipEventConstruct = false;

    /**
     * Key press event
     * @since SWF 6
     */
    @Conditional(minSwfVersion = 6)
    public boolean clipEventKeyPress = false;

    /**
     * Mouse drag out event
     * @since SWF 6
     */
    @Conditional(minSwfVersion = 6)
    public boolean clipEventDragOut = false;

    /**
     * Reserved
     */
    @Reserved
    @SWFType(value = BasicType.UB, count = 8)
    @Conditional(minSwfVersion = 6)
    public int reserved2;


    /**
     * Constructor.
     */
    public CLIPEVENTFLAGS() {

    }

    /**
     * Gets header.
     * @param key Key code
     * @param asFileName If true, returns key as file name
     * @return Header
     */
    public String getHeader(int key, boolean asFileName) {
        String ret = "";
        List<String> onList = new ArrayList<>();
        if (clipEventKeyUp) {
            ret = "onClipEvent(keyUp)";
        }
        if (clipEventKeyDown) {
            ret = "onClipEvent(keyDown)";
        }
        if (clipEventMouseUp) {
            ret = "onClipEvent(mouseUp)";
        }
        if (clipEventMouseDown) {
            ret = "onClipEvent(mouseDown)";
        }
        if (clipEventMouseMove) {
            ret = "onClipEvent(mouseMove)";
        }
        if (clipEventUnload) {
            ret = "onClipEvent(unload)";
        }
        if (clipEventEnterFrame) {
            ret = "onClipEvent(enterFrame)";
        }
        if (clipEventLoad) {
            ret = "onClipEvent(load)";
        }
        if (clipEventData) {
            ret = "onClipEvent(data)";
        }
        if (clipEventDragOver) {
            onList.add("dragOver");
        }
        if (clipEventRollOut) {
            onList.add("rollOut");
        }
        if (clipEventRollOver) {
            onList.add("rollOver");
        }
        if (clipEventReleaseOutside) {
            onList.add("releaseOutside");
        }
        if (clipEventRelease) {
            onList.add("release");
        }
        if (clipEventPress) {
            onList.add("press");
        }
        if (clipEventInitialize) {
            onList.add("initialize");
        }

        if (clipEventConstruct) {
            onList.add("construct");
        }
        if (clipEventKeyPress) {
            if (asFileName) {
                onList.add("keyPress " + Helper.makeFileName(CLIPACTIONRECORD.keyToString(key).replace("<", "").replace(">", "")) + "");
            } else {
                onList.add("keyPress \"" + Helper.escapeActionScriptString(CLIPACTIONRECORD.keyToString(key)) + "\"");
            }
        }
        if (clipEventDragOut) {
            onList.add("dragOut");
        }

        if (!onList.isEmpty()) {
            ret = "on(" + Helper.joinStrings(onList, ",") + ")";
        }
        return ret.trim();
    }

    /**
     * Returns true if all events are false.
     *
     * @return True when all events are false
     */
    public boolean isClear() {
        if (clipEventKeyUp) {
            return false;
        }
        if (clipEventKeyDown) {
            return false;
        }
        if (clipEventMouseUp) {
            return false;
        }
        if (clipEventMouseDown) {
            return false;
        }
        if (clipEventMouseMove) {
            return false;
        }
        if (clipEventUnload) {
            return false;
        }
        if (clipEventEnterFrame) {
            return false;
        }
        if (clipEventLoad) {
            return false;
        }
        if (clipEventDragOver) {
            return false;
        }
        if (clipEventRollOut) {
            return false;
        }
        if (clipEventRollOver) {
            return false;
        }
        if (clipEventReleaseOutside) {
            return false;
        }
        if (clipEventRelease) {
            return false;
        }
        if (clipEventPress) {
            return false;
        }
        if (clipEventInitialize) {
            return false;
        }
        if (clipEventData) {
            return false;
        }
        if (clipEventConstruct) {
            return false;
        }
        if (clipEventKeyPress) {
            return false;
        }
        if (clipEventDragOut) {
            return false;
        }
        return true;
    }
}
