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

import com.jpexs.decompiler.flash.Configuration;
import com.jpexs.decompiler.flash.DisassemblyListener;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.Exportable;
import com.jpexs.helpers.ReReadableInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Event handler
 *
 * @author JPEXS
 */
public class CLIPACTIONRECORD implements ASMSource, Exportable {

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
    private long pos;
    private long hdrPos;

    public CLIPACTIONRECORD(InputStream is, int version, long pos) throws IOException {
        SWFInputStream sis = new SWFInputStream(is, version);
        eventFlags = sis.readCLIPEVENTFLAGS();
        if (eventFlags.isClear()) {
            return;
        }
        long actionRecordSize = sis.readUI32();
        if (eventFlags.clipEventKeyPress) {
            keyCode = sis.readUI8();
            actionRecordSize--;
        }
        hdrPos = sis.getPos();
        actionBytes = sis.readBytes(actionRecordSize);
        this.pos = pos;

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
    public int keyCode;
    /**
     * Actions to perform
     */
    //public List<Action> actions;
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
     * @param version SWF version
     * @return ASM source
     */
    @Override
    public String getASMSource(int version, boolean hex, boolean highlight) {
        return Action.actionsToString(listeners, 0, getActions(version), null, version, hex, highlight, getPos() + hdrPos, toString()/*FIXME?*/);
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
    public List<Action> getActions(int version) {
        try {
            boolean deobfuscate = Configuration.getConfig("autoDeobfuscate", true);
            List<Action> list = SWFInputStream.readActionList(listeners, 0, getPos() + hdrPos, new ReReadableInputStream(new ByteArrayInputStream(actionBytes)), version, 0, -1, toString()/*FIXME?*/);
            if (deobfuscate) {
                list = Action.removeNops(0, list, version, getPos() + hdrPos, toString()/*FIXME?*/);
            }
            return list;
        } catch (Exception ex) {
            Logger.getLogger(BUTTONCONDACTION.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<>();
        }
    }

    @Override
    public void setActions(List<Action> actions, int version) {
        actionBytes = Action.actionsToBytes(actions, true, version);
    }

    @Override
    public byte[] getActionBytes() {
        return actionBytes;
    }

    @Override
    public void setActionBytes(byte[] actionBytes) {
        this.actionBytes = actionBytes;
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
    public String getActionSourcePrefix() {
        return eventFlags.getHeader(keyCode, false) + "{\r\n";
    }

    @Override
    public String getActionSourceSuffix() {
        return "}\r\n";
    }

    @Override
    public String getExportFileName(List<Tag> tags) {
        return eventFlags.getHeader(keyCode, true);
    }

    @Override
    public int getActionSourceIndent() {
        return 1;
    }
}
