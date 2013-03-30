/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.types;

/**
 * Specifies one or more sprite events to which an event handler applies.
 *
 * @author JPEXS
 */
public class CLIPEVENTFLAGS {

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
     * @since SWF 6 Mouse drag over event
     */
    public boolean clipEventDragOver;
    /**
     * @since SWF 6 Mouse rollout event
     */
    public boolean clipEventRollOut;
    /**
     * @since SWF 6 Mouse rollover event
     */
    public boolean clipEventRollOver;
    /**
     * @since SWF 6 Mouse release outside event
     */
    public boolean clipEventReleaseOutside;
    /**
     * @since SWF 6 Mouse release inside event
     */
    public boolean clipEventRelease;
    /**
     * @since SWF 6 Mouse press event
     */
    public boolean clipEventPress;
    /**
     * @since SWF 6 Initialize event
     */
    public boolean clipEventInitialize;
    /**
     * Data received event
     */
    public boolean clipEventData;
    /**
     * @since SWF 7 Construct event
     */
    public boolean clipEventConstruct = false;
    /**
     * @since SWF 6 Key press event
     */
    public boolean clipEventKeyPress = false;
    /**
     * @since SWF 6 Mouse drag out event
     */
    public boolean clipEventDragOut = false;

    /**
     * Returns a string representation of the object
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        String ret = "";
        if (clipEventKeyUp) {
            ret += "onClipEvent(keyUp)";
        }
        if (clipEventKeyDown) {
            ret += "onClipEvent(keyDown)";
        }
        if (clipEventMouseUp) {
            ret += "onClipEvent(mouseUp)";
        }
        if (clipEventMouseDown) {
            ret += "onClipEvent(mouseDown)";
        }
        if (clipEventMouseMove) {
            ret += "onClipEvent(mouseMove)";
        }
        if (clipEventUnload) {
            ret += "onClipEvent(unload)";
        }
        if (clipEventEnterFrame) {
            ret += "onClipEvent(enterFrame)";
        }
        if (clipEventLoad) {
            ret += "onClipEvent(load)";
        }
        if (clipEventDragOver) {
            ret += "on(dragOver)";
        }
        if (clipEventRollOut) {
            ret += "on(rollOut)";
        }
        if (clipEventRollOver) {
            ret += "on(rollOver)";
        }
        if (clipEventReleaseOutside) {
            ret += "on(releaseOutside)";
        }
        if (clipEventRelease) {
            ret += "on(release)";
        }
        if (clipEventPress) {
            ret += "on(press)";
        }
        if (clipEventInitialize) {
            ret += "on(initialize)";
        }
        if (clipEventData) {
            ret += "onClipEvent(data)";
        }
        if (clipEventConstruct) {
            ret += "on(construct)";
        }
        if (clipEventKeyPress) {
            ret += "on(keyPress)";
        }
        if (clipEventDragOut) {
            ret += "on(dragOut)";
        }

        return ret.trim();
    }

    /**
     * Returns true if all events are false
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
