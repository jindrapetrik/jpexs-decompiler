/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.action.ActionListReader;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Event handler
 *
 * @author JPEXS
 */
public class CLIPACTIONRECORD implements ASMSource, Serializable {

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
        actionBytes = sis.readByteRangeEx(actionRecordSize, "actionBytes");
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
    //public List<Action> actions;
    @Internal
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
        try {
            int prevLength = actionBytes.getPos();
            SWFInputStream rri = new SWFInputStream(swf, actionBytes.getArray());
            if (prevLength != 0) {
                rri.seek(prevLength);
            }

            ActionList list = ActionListReader.readActionListTimeout(listeners, rri, swf.version, prevLength, prevLength + actionBytes.getLength(), toString()/*FIXME?*/);
            return list;
        } catch (InterruptedException ex) {
            throw ex;
        } catch (Exception ex) {
            Logger.getLogger(CLIPACTIONRECORD.class.getName()).log(Level.SEVERE, null, ex);
            return new ActionList();
        }
    }

    @Override
    public void setActions(List<Action> actions) {
        byte[] bytes = Action.actionsToBytes(actions, true, swf.version);
        actionBytes = new ByteArrayRange(bytes);
    }

    @Override
    public byte[] getActionBytes() {
        return actionBytes.getRangeData();
    }

    @Override
    public void setActionBytes(byte[] actionBytes) {
        this.actionBytes = new ByteArrayRange(actionBytes);
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
