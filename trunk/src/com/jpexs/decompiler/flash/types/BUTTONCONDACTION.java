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
import com.jpexs.decompiler.flash.ReReadableInputStream;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import java.io.ByteArrayInputStream;
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
public class BUTTONCONDACTION implements ASMSource {

    private long pos;

    @Override
    public long getPos() {
        return pos;
    }

    public BUTTONCONDACTION(InputStream is, int version, long containerOffset) throws IOException {
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
            actionBytes = sis.readBytes(sis.available());
        } else {
            actionBytes = sis.readBytes(condActionSize - 4);
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
    public String getASMSource(int version, boolean hex) {
        return Action.actionsToString(listeners, 0, getActions(version), null, version, hex, getPos() + 4);
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
    public List<Action> getActions(int version) {
        try {
            boolean deobfuscate = (Boolean) Configuration.getConfig("autoDeobfuscate", true);
            List<Action> list = SWFInputStream.readActionList(listeners, 0, getPos() + 4, new ReReadableInputStream(new ByteArrayInputStream(actionBytes)), version, 0, -1);
            if (deobfuscate) {
                list = Action.removeNops(0, list, version, getPos() + 4);
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
}
