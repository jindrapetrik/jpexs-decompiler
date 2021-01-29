/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.action.swf4;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.helpers.Helper;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 4)
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
    public void getRef(Set<Long> refs) {
        refs.add(getTargetAddress());
    }

    public long getTargetAddress() {
        return getAddress() + 5 /*getTotalActionLength()*/ + offset;
    }

    @Override
    protected void getContentBytes(SWFOutputStream sos) throws IOException {
        sos.writeSI16(offset);
    }

    /**
     * Gets the length of action converted to bytes
     *
     * @return Length
     */
    @Override
    protected int getContentBytesLength() {
        return 2;
    }

    @Override
    public String getASMSource(ActionList container, Set<Long> knownAddreses, ScriptExportMode exportMode) {
        long address = getTargetAddress();
        String ofsStr = Helper.formatAddress(address);
        return "If loc" + ofsStr + (!jumpUsed ? " ;compileTimeIgnore" : (!ignoreUsed ? " ;compileTimeJump" : ""));
    }

    public ActionIf(FlasmLexer lexer) throws IOException, ActionParseException {
        super(0x9D, 2);
        identifier = lexIdentifier(lexer);
    }

    @Override
    public String toString() {
        return "ActionIf " + offset;
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        if (lda.stackIsEmpty()) {
            return false;
        }

        if (EcmaScript.toBoolean(lda.pop())) {
            lda.jump = getTargetAddress();
        }

        return true;
    }

    @Override
    public boolean isBranch() {
        return true;
    }

    @Override
    public List<Integer> getBranches(GraphSource code) {
        List<Integer> ret = super.getBranches(code);
        int length = getTotalActionLength();
        long targetAddress = getTargetAddress();
        int jmp = code.adr2pos(targetAddress);
        int after = code.adr2pos(getAddress() + length);
        if (jmp == -1) {
            Logger.getLogger(ActionIf.class.getName()).log(Level.SEVERE, "Invalid IF jump to ofs{0}", Helper.formatAddress(targetAddress));
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
