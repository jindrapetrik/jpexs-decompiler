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
package com.jpexs.decompiler.flash.action.swf4;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.parser.ParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.helpers.Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActionIf extends Action {

    private int offset;
    public String identifier;
    public boolean jumpUsed = true;
    public boolean ignoreUsed = true;

    public int getJumpOffset() {
        return offset;
    }

    public final void setJumpOffset(int offset) {
        this.offset = offset;
    }

    public ActionIf(int offset) {
        super(0x9D, 2);
        setJumpOffset(offset);
    }

    public ActionIf(int actionLength, SWFInputStream sis) throws IOException {
        super(0x9D, actionLength);
        setJumpOffset(sis.readSI16("offset"));
    }

    @Override
    public void getRef(List<Long> refs) {
        refs.add(getAddress() + getTotalActionLength() + offset);
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
    public String getASMSource(ActionList container, List<Long> knownAddreses, ScriptExportMode exportMode) {
        long address = getAddress() + getTotalActionLength() + offset;
        String ofsStr = Helper.formatAddress(address);
        return "If loc" + ofsStr + (!jumpUsed ? " ;compileTimeIgnore" : (!ignoreUsed ? " ;compileTimeJump" : ""));
    }

    public ActionIf(FlasmLexer lexer) throws IOException, ParseException {
        super(0x9D, 2);
        identifier = lexIdentifier(lexer);
    }

    @Override
    public String toString() {
        return "ActionIf " + offset;
    }

    @Override
    public boolean isBranch() {
        return true;
    }

    @Override
    public List<Integer> getBranches(GraphSource code) {
        List<Integer> ret = super.getBranches(code);
        int length = getTotalActionLength();
        int jmp = code.adr2pos(getAddress() + length + offset);
        int after = code.adr2pos(getAddress() + length);
        if (jmp == -1) {
            Logger.getLogger(ActionIf.class.getName()).log(Level.SEVERE, "Invalid IF jump to ofs" + Helper.formatAddress(getAddress() + length + offset));
            ret.add(after);
        } else {
            ret.add(jmp);
        }
        ret.add(after);
        return ret;
    }

    @Override
    public boolean ignoredLoops() {
        return false; //compileTime;
    }
}
