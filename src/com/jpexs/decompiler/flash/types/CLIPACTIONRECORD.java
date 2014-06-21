/*
 *  Copyright (C) 2010-2014 JPEXS
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

import com.jpexs.decompiler.flash.DisassemblyListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionListReader;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.ContainerItem;
import com.jpexs.decompiler.flash.tags.base.Exportable;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.Internal;
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
public class CLIPACTIONRECORD implements ASMSource, Exportable, ContainerItem, Serializable {

    public static String keyToString(int key) {
        if ((key < CLIPACTIONRECORD.KEYNAMES.length) && (key > 0) && (CLIPACTIONRECORD.KEYNAMES[key] != null)) {
            return CLIPACTIONRECORD.KEYNAMES[key];
        } else {
            return "" + (char) key;
        }
    }
    public static final String KEYNAMES[] = {
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
    private final SWF swf;
    @Internal
    private final Tag tag;
    @Internal
    private long pos;
    @Internal
    private long hdrPos;

    //Constructor for Generic tag editor. TODO:Handle this somehow better
    public CLIPACTIONRECORD() {
        swf = null;
        tag = null;
        eventFlags = new CLIPEVENTFLAGS();
        actionBytes = new byte[0];
        hdrPos = 0;
    }

    public CLIPACTIONRECORD(SWF swf, SWFInputStream sis, long pos, Tag tag) throws IOException {
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
        hdrPos = sis.getPos();
        actionBytes = sis.readBytesEx(actionRecordSize, "actionBytes");
        this.pos = pos;
    }

    @Override
    public SWF getSwf() {
        return swf;
    }

    @Override
    public long getPos() {
        return pos;
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
    public byte[] actionBytes;

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
     * @param actions
     * @param writer
     * @return ASM source
     * @throws java.lang.InterruptedException
     */
    @Override
    public GraphTextWriter getASMSource(ScriptExportMode exportMode, GraphTextWriter writer, List<Action> actions) throws InterruptedException {
        if (actions == null) {
            actions = getActions();
        }
        return Action.actionsToString(listeners, 0, actions, null, swf.version, exportMode, writer, getPos() + hdrPos, toString()/*FIXME?*/);
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
    public List<Action> getActions() throws InterruptedException {
        try {
            List<Action> list = ActionListReader.readActionListTimeout(listeners, getPos() + hdrPos, new SWFInputStream(swf, actionBytes), swf.version, 0, -1, toString()/*FIXME?*/);
            return list;
        } catch (InterruptedException ex) {
            throw ex;
        } catch (Exception ex) {
            Logger.getLogger(BUTTONCONDACTION.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<>();
        }
    }

    @Override
    public void setActions(List<Action> actions) {
        actionBytes = Action.actionsToBytes(actions, true, swf.version);
    }

    @Override
    public byte[] getActionBytes() {
        return actionBytes;
    }

    @Override
    public void setActionBytes(byte[] actionBytes) {
        this.actionBytes = actionBytes;
    }

    @Override
    public void setModified() {
        if (tag != null) {
            tag.setModified(true);
        }
    }

    @Override
    public GraphTextWriter getActionBytesAsHex(GraphTextWriter writer) {
        return Helper.byteArrayToHexWithHeader(writer, actionBytes);
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

}
