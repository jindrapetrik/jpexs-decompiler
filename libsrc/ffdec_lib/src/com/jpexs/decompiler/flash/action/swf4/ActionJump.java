/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.action.swf4;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionGraphSource;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.helpers.Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    public ActionJump(int actionLength, SWFInputStream sis) throws IOException {
        super(0x99, actionLength);
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
            throw new Error("This should never happen.", e);
        }
        return surroundWithAction(baos.toByteArray(), version);
    }

    @Override
    public String getASMSource(ActionList container, List<Long> knownAddreses, ScriptExportMode exportMode) {
        long address = getAddress() + getTotalActionLength() + offset;
        String ofsStr = Helper.formatAddress(address);
        return "Jump loc" + ofsStr;
    }

    public ActionJump(FlasmLexer lexer) throws IOException, ActionParseException {
        super(0x99, 2);
        identifier = lexIdentifier(lexer);
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
        int version = ((ActionGraphSource) code).version;
        int length = getBytesLength(version);
        int ofs = code.adr2pos(getAddress() + length + offset);
        if (ofs == -1) {
            ofs = code.adr2pos(getAddress() + length);
            Logger.getLogger(ActionJump.class.getName()).log(Level.SEVERE, "Invalid jump to ofs" + Helper.formatAddress(getAddress() + length + offset) + " from ofs" + Helper.formatAddress(getAddress()));
        }
        ret.add(ofs);
        return ret;
    }
}
