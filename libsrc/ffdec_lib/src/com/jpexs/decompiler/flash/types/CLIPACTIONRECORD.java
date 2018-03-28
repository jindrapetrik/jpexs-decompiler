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
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Event handler
 *
 * @author JPEXS
 */
public class CLIPACTIONRECORD implements ASMSource, Serializable {

    private String scriptName = "-";

    @Override
    public String getScriptName() {
        return scriptName;
    }

    public static String keyToString(int key) {
        if ((key < CLIPACTIONRECORD.KEYNAMES.length) && (key > 0) && (CLIPACTIONRECORD.KEYNAMES[key] != null)) {
            return CLIPACTIONRECORD.KEYNAMES[key];
        } else {
            return "" + (char) key;
        }
    }

    public static final String[] KEYNAMES = {
        null,
        "<Left>",
        "<Right>",
        "<Home>",
        "<End>",
        "<Insert>",
        "<Delete>",
        null,
        "<Backspace>",
        null,
        null,
        null,
        null,
        "<Enter>",
        "<Up>",
        "<Down>",
        "<PageUp>",
        "<PageDown>",
        "<Tab>",
        "<Escape>",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        "<Space>"
    };

    @Internal
    private SWF swf;

    @Internal
    private Tag tag;

    // Constructor for Generic tag editor. TODO:Handle this somehow better
    public CLIPACTIONRECORD() {
        swf = null;
        tag = null;
        eventFlags = new CLIPEVENTFLAGS();
        actionBytes = ByteArrayRange.EMPTY;
    }

    @Override
    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public CLIPACTIONRECORD(SWF swf, SWFInputStream sis, Tag tag) throws IOException {
        this.swf = swf;
        this.tag = tag;
        eventFlags = sis.readCLIPEVENTFLAGS("eventFlags");
        if (eventFlags.isClear()) {
            return;
        }
        long actionRecordSize = sis.readUI32("actionRecordSize");
        if (eventFlags.clipEventKeyPress) {
            keyCode = sis.readUI8("keyCode");
            actionRecordSize--;
        }
        actionBytes = sis.readByteRangeEx(actionRecordSize, "actionBytes", DumpInfoSpecialType.ACTION_BYTES, sis.getPos());
    }

    @Override
    public SWF getSwf() {
        return swf;
    }

    /**
     * Events to which this handler applies
     */
    public CLIPEVENTFLAGS eventFlags;

    /**
     * If EventFlags contain ClipEventKeyPress: Key code to trap
     */
    @Conditional("eventFlags.clipEventKeyPress")
    public int keyCode;

    /**
     * Actions to perform
     */
    @HideInRawEdit
    public ByteArrayRange actionBytes;

    /**
     * Returns a string representation of the object
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return eventFlags.getHeader(keyCode, false);
    }

    /**
     * Returns header with events converted to string
     *
     * @return String representation of events
     */
    public String getHeader() {
        String ret;
        ret = eventFlags.toString();
        if (eventFlags.clipEventKeyPress) {
            ret = ret.replace("keyPress", "keyPress<" + keyCode + ">");
        }
        return ret;
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

    @Override
    public GraphTextWriter getActionSourcePrefix(GraphTextWriter writer) {
        writer.appendNoHilight(eventFlags.getHeader(keyCode, false));
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
        return eventFlags.getHeader(keyCode, true);
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
