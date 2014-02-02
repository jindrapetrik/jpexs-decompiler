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
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.ContainerItem;
import com.jpexs.decompiler.flash.tags.base.Exportable;
import com.jpexs.decompiler.graph.ExportMode;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.MemoryInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Actions to execute at particular button events
 *
 * @author JPEXS
 */
public class BUTTONCONDACTION implements ASMSource, Exportable, ContainerItem {

    private final SWF swf;
    private final long pos;

    @Override
    public SWF getSwf() {
        return swf;
    }

    @Override
    public long getPos() {
        return pos;
    }

    public BUTTONCONDACTION(SWF swf, InputStream is, int version, long containerOffset) throws IOException {
        this.swf = swf;
        SWFInputStream sis = new SWFInputStream(is, version);
        pos = containerOffset;
        int condActionSize = sis.readUI16();
        isLast = condActionSize <= 0;
        condIdleToOverDown = sis.readUB(1) == 1;
        condOutDownToIdle = sis.readUB(1) == 1;
        condOutDownToOverDown = sis.readUB(1) == 1;
        condOverDownToOutDown = sis.readUB(1) == 1;
        condOverDownToOverUp = sis.readUB(1) == 1;
        condOverUpToOverDown = sis.readUB(1) == 1;
        condOverUpToIddle = sis.readUB(1) == 1;
        condIdleToOverUp = sis.readUB(1) == 1;
        condKeyPress = (int) sis.readUB(7);
        condOverDownToIddle = sis.readUB(1) == 1;
        if (condActionSize <= 0) {
            actionBytes = sis.readBytesEx(sis.available());
        } else {
            actionBytes = sis.readBytesEx(condActionSize - 4);
        }
    }
    /**
     * Is this BUTTONCONDACTION last in the list?
     */
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
    public int condKeyPress;
    /**
     * OverDown to Idle
     */
    public boolean condOverDownToIddle;
    /**
     * Actions to perform
     */
    //public List<Action> actions;
    /**
     * Actions to perform in byte array
     */
    public byte[] actionBytes;

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
     * @param version SWF version
     * @return ASM source
     */
    @Override
    public GraphTextWriter getASMSource(int version, ExportMode exportMode, GraphTextWriter writer, List<Action> actions) throws InterruptedException {
        if (actions == null) {
            actions = getActions(version);
        }
        return Action.actionsToString(listeners, 0, actions, null, version, exportMode, writer, getPos() + 4, toString()/*FIXME?*/);
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
     * @param version Version
     * @return List of actions
     */
    @Override
    public List<Action> getActions(int version) throws InterruptedException {
        try {
            List<Action> list = ActionListReader.readActionListTimeout(listeners, getPos() + 4, new MemoryInputStream(actionBytes), version, 0, -1, toString()/*FIXME?*/);
            return list;

        } catch (InterruptedException ex) {
            throw ex;
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
}
