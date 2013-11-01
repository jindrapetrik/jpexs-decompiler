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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.DisassemblyListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionListReader;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.graph.ExportMode;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.ReReadableInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Instructs Flash Player to perform a list of actions when the current frame is
 * complete.
 */
public class DoActionTag extends Tag implements ASMSource {

    /**
     * List of actions to perform
     */
    //public List<Action> actions = new ArrayList<Action>();
    public byte[] actionBytes;
    public static final int ID = 12;

    /**
     * Constructor
     *
     * @param swf
     * @param data Data bytes
     * @param version SWF version
     * @param pos
     */
    public DoActionTag(SWF swf, byte[] data, int version, long pos) {
        super(swf, ID, "DoAction", data, pos);
        actionBytes = data;
    }

    /**
     * Gets data bytes
     *
     * @param version SWF version
     * @return Bytes of data
     */
    @Override
    public byte[] getData(int version) {
        return actionBytes;//Action.actionsToBytes(actions, true, version);
    }

    /**
     * Converts actions to ASM source
     *
     * @param version SWF version
     * @return ASM source
     */
    @Override
    public GraphTextWriter getASMSource(int version, ExportMode exportMode, GraphTextWriter writer, List<Action> actions) {
        if (actions == null) {
            actions = getActions(version);
        }
        return Action.actionsToString(listeners, 0, actions, null, version, exportMode, writer, getPos(), toString()/*FIXME?*/);
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
     * Returns string representation of the object
     *
     * @return String representation of the object
     */
    @Override
    public String toString() {
        return "DoAction";
    }

    @Override
    public List<Action> getActions(int version) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int prevLength = 0;
            if (previousTag != null) {
                byte[] prevData = previousTag.getData(version);
                baos.write(prevData);
                prevLength = prevData.length;
                byte[] header = SWFOutputStream.getTagHeader(this, data, version);
                baos.write(header);
                prevLength += header.length;
            }
            baos.write(actionBytes);
            ReReadableInputStream rri = new ReReadableInputStream(new ByteArrayInputStream(baos.toByteArray()));
            rri.setPos(prevLength);
            List<Action> list = ActionListReader.readActionList(listeners, getPos() - prevLength, rri, version, prevLength, -1, toString()/*FIXME?*/);
            return list;
        } catch (Exception ex) {
            Logger.getLogger(DoActionTag.class.getName()).log(Level.SEVERE, null, ex);
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
        return Helper.byteArrayToHex(writer, actionBytes);
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
        return "";
    }

    @Override
    public String getActionSourceSuffix() {
        return "";
    }

    @Override
    public int getActionSourceIndent() {
        return 0;
    }
}
