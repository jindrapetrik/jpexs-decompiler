/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.DisassemblyListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ConstantPoolTooBigException;
import com.jpexs.decompiler.flash.dumpview.DumpInfoSpecialType;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.HideInRawEdit;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Actions to execute at particular button events
 *
 * @author JPEXS
 */
public class BUTTONCONDACTION implements ASMSource, Serializable {

    private SWF swf;

    private Tag tag;

    private String scriptName = "-";

    @Override
    public String getScriptName() {
        return scriptName;
    }

    // Constructor for Generic tag editor.
    public BUTTONCONDACTION() {
        swf = null;
        tag = null;
        actionBytes = new ByteArrayRange(SWFInputStream.BYTE_ARRAY_EMPTY);
    }

    @Override
    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public BUTTONCONDACTION(SWF swf, SWFInputStream sis, Tag tag) throws IOException {
        this.swf = swf;
        this.tag = tag;
        int condActionSize = sis.readUI16("condActionSize");
        isLast = condActionSize <= 0;
        condIdleToOverDown = sis.readUB(1, "condIdleToOverDown") == 1;
        condOutDownToIdle = sis.readUB(1, "condOutDownToIdle") == 1;
        condOutDownToOverDown = sis.readUB(1, "condOutDownToOverDown") == 1;
        condOverDownToOutDown = sis.readUB(1, "condOverDownToOutDown") == 1;
        condOverDownToOverUp = sis.readUB(1, "condOverDownToOverUp") == 1;
        condOverUpToOverDown = sis.readUB(1, "condOverUpToOverDown") == 1;
        condOverUpToIddle = sis.readUB(1, "condOverUpToIddle") == 1;
        condIdleToOverUp = sis.readUB(1, "condIdleToOverUp") == 1;
        condKeyPress = (int) sis.readUB(7, "condKeyPress");
        condOverDownToIdle = sis.readUB(1, "condOverDownToIdle") == 1;
        actionBytes = sis.readByteRangeEx(condActionSize <= 0 ? sis.available() : condActionSize - 4, "actionBytes", DumpInfoSpecialType.ACTION_BYTES, sis.getPos());
    }

    @Override
    public SWF getSwf() {
        return swf;
    }

    /**
     * Is this BUTTONCONDACTION last in the list?
     */
    @Internal
    public boolean isLast;

    /**
     * Idle to OverDown
     */
    public boolean condIdleToOverDown;

    /**
     * OutDown to Idle
     */
    public boolean condOutDownToIdle;

    /**
     * OutDown to OverDown
     */
    public boolean condOutDownToOverDown;

    /**
     * OverDown to OutDown
     */
    public boolean condOverDownToOutDown;

    /**
     * OverDown to OverUp
     */
    public boolean condOverDownToOverUp;

    /**
     * OverUp to OverDown
     */
    public boolean condOverUpToOverDown;

    /**
     * OverUp to Idle
     */
    public boolean condOverUpToIddle;

    /**
     * Idle to OverUp
     */
    public boolean condIdleToOverUp;

    /**
     * @since SWF 4 key code
     */
    @SWFType(value = BasicType.UB, count = 7)
    @Conditional(minSwfVersion = 4)
    public int condKeyPress;

    /**
     * OverDown to Idle
     */
    public boolean condOverDownToIdle;

    /**
     * Actions to perform in byte array
     */
    @HideInRawEdit
    public ByteArrayRange actionBytes;

    /**
     * Sets actions associated with this object
     *
     * @param actions Action list
     */
    /*public void setActions(List<Action> actions) {
     this.actions = actions;
     }*/
    /**
     * Returns a string representation of the object
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "BUTTONCONDACTION";
    }

    /**
     * Converts actions to ASM source
     *
     * @param exportMode PCode or hex?
     * @param writer
     * @param actions
     * @return ASM source
     * @throws java.lang.InterruptedException
     */
    @Override
    public GraphTextWriter getASMSource(ScriptExportMode exportMode, GraphTextWriter writer, ActionList actions) throws InterruptedException {
        if (actions == null) {
            actions = getActions();
        }
        return Action.actionsToString(listeners, 0, actions, swf.version, exportMode, writer);
    }

    @Override
    public GraphTextWriter getActionScriptSource(GraphTextWriter writer, ActionList actions) throws InterruptedException {
        if (actions == null) {
            actions = getActions();
        }

        return Action.actionsToSource(this, actions, getScriptName(), writer);
    }

    /**
     * Whether or not this object contains ASM source
     *
     * @return True when contains
     */
    @Override
    public boolean containsSource() {
        return true;
    }

    /**
     * Returns actions associated with this object
     *
     * @return List of actions
     * @throws java.lang.InterruptedException
     */
    @Override
    public ActionList getActions() throws InterruptedException {
        return SWF.getCachedActionList(this, listeners);
    }

    @Override
    public void setActions(List<Action> actions) {
        actionBytes = Action.actionsToByteArrayRange(actions, true, swf.version);
    }

    @Override
    public ByteArrayRange getActionBytes() {
        return actionBytes;
    }

    @Override
    public void setActionBytes(byte[] actionBytes) {
        this.actionBytes = new ByteArrayRange(actionBytes);
        SWF.uncache(this);
    }

    @Override
    public void setConstantPools(List<List<String>> constantPools) throws ConstantPoolTooBigException {
        Action.setConstantPools(this, constantPools, false);
    }

    @Override
    public void setModified() {
        if (tag != null) {
            tag.setModified(true);
        }
    }

    @Override
    public boolean isModified() {
        if (tag != null) {
            return tag.isModified();
        }
        return false;
    }

    @Override
    public GraphTextWriter getActionBytesAsHex(GraphTextWriter writer) {
        return Helper.byteArrayToHexWithHeader(writer, actionBytes.getRangeData());
    }

    List<DisassemblyListener> listeners = new ArrayList<>();

    @Override
    public void addDisassemblyListener(DisassemblyListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeDisassemblyListener(DisassemblyListener listener) {
        listeners.remove(listener);
    }

    private String getHeader(boolean asFilename) {
        List<String> events = new ArrayList<>();
        if (condOverUpToOverDown) {
            events.add("press");
        }
        if (condOverDownToOverUp) {
            events.add("release");
        }
        if (condOutDownToIdle) {
            events.add("releaseOutside");
        }
        if (condIdleToOverUp) {
            events.add("rollOver");
        }
        if (condOverUpToIddle) {
            events.add("rollOut");
        }
        if (condOverDownToOutDown) {
            events.add("dragOut");
        }
        if (condOutDownToOverDown) {
            events.add("dragOver");
        }
        if (condKeyPress > 0) {
            if (asFilename) {
                events.add("keyPress " + Helper.makeFileName(CLIPACTIONRECORD.keyToString(condKeyPress).replace("<", "").replace(">", "")) + "");
            } else {
                events.add("keyPress \"" + CLIPACTIONRECORD.keyToString(condKeyPress) + "\"");
            }
        }
        String onStr = "";
        for (int i = 0; i < events.size(); i++) {
            if (i > 0) {
                onStr += ", ";
            }
            onStr += events.get(i);
        }
        return "on(" + onStr + ")";
    }

    @Override
    public GraphTextWriter getActionSourcePrefix(GraphTextWriter writer) {
        writer.appendNoHilight(getHeader(false));
        writer.appendNoHilight("{").newLine();
        return writer.indent();
    }

    @Override
    public GraphTextWriter getActionSourceSuffix(GraphTextWriter writer) {
        writer.unindent();
        return writer.appendNoHilight("}").newLine();
    }

    @Override
    public int getPrefixLineCount() {
        return 1;
    }

    @Override
    public String removePrefixAndSuffix(String source) {
        return Helper.unindentRows(1, 1, source);
    }

    @Override
    public String getExportFileName() {
        return getHeader(true);
    }

    @Override
    public Tag getSourceTag() {
        return tag;
    }

    @Override
    public void setSourceTag(Tag t) {
        this.tag = t;
        this.swf = t.getSwf();
    }
}
