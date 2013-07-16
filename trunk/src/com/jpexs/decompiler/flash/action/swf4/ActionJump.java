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
package com.jpexs.decompiler.flash.action.swf4;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionGraphSource;
import com.jpexs.decompiler.flash.action.parser.ParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.helpers.Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActionJump extends Action {

    private int offset;
    public String identifier;
    public boolean isContinue = false;
    public boolean isBreak = false;

    public int getJumpOffset() {
        return offset;
    }

    public final void setJumpOffset(int offset) {
        this.offset = offset;
    }

    public ActionJump(int offset) {
        super(0x99, 2);
        setJumpOffset(offset);
    }

    public ActionJump(SWFInputStream sis) throws IOException {
        super(0x99, 2);
        setJumpOffset(sis.readSI16());
    }

    @Override
    public List<Long> getAllRefs(int version) {
        List<Long> ret = new ArrayList<>();
        ret.add(getRef(version));
        return ret;
    }

    public long getRef(int version) {
        return getAddress() + getBytes(version).length + offset;
    }

    @Override
    public byte[] getBytes(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            sos.writeSI16(offset);
            sos.close();
        } catch (IOException e) {
        }
        return surroundWithAction(baos.toByteArray(), version);
    }

    @Override
    public String getASMSource(List<? extends GraphSourceItem> container, List<Long> knownAddreses, List<String> constantPool, int version, boolean hex) {
        String ofsStr = Helper.formatAddress(getAddress() + getBytes(version).length + offset);
        return "Jump loc" + ofsStr;
    }

    public ActionJump(FlasmLexer lexer) throws IOException, ParseException {
        super(0x99, 0);
        identifier = lexIdentifier(lexer);
    }

    @Override
    public List<Action> getAllIfsOrJumps() {
        List<Action> ret = new ArrayList<>();
        ret.add(this);
        return ret;
    }

    @Override
    public String toString() {
        return "Jump " + offset;
    }

    @Override
    public boolean isJump() {
        return true;
    }

    @Override
    public List<Integer> getBranches(GraphSource code) {
        List<Integer> ret = super.getBranches(code);
        int ofs = code.adr2pos(getAddress() + getBytes(((ActionGraphSource) code).version).length + offset);
        if (ofs == -1) {
            ofs = code.adr2pos(getAddress() + getBytes(((ActionGraphSource) code).version).length);
            new Exception().printStackTrace();
            Logger.getLogger(ActionJump.class.getName()).log(Level.SEVERE, "Invalid jump to ofs" + Helper.formatAddress(getAddress() + getBytes(((ActionGraphSource) code).version).length + offset) + " from ofs" + Helper.formatAddress(getAddress()));
        }
        ret.add(ofs);
        return ret;
    }
}
