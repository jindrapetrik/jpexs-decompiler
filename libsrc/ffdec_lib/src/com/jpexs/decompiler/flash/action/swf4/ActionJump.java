/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
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
 * Jump action - Jumps to a location.
 *
 * @author JPEXS
 */
@SWFVersion(from = 4)
public class ActionJump extends Action {

    /**
     * Offset to jump to
     */
    private int offset;

    /**
     * Identifier
     */
    public String identifier;

    /**
     * Is continue
     */
    public boolean isContinue = false;

    /**
     * Is break
     */
    public boolean isBreak = false;

    /**
     * Gets the jump offset
     * @return Offset
     */
    public int getJumpOffset() {
        return offset;
    }

    /**
     * Sets the jump offset
     * @param offset Offset
     */
    public final void setJumpOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Constructor
     * @param offset Offset
     * @param charset Charset
     */
    public ActionJump(int offset, String charset) {
        super(0x99, 2, charset);
        setJumpOffset(offset);
    }

    /**
     * Constructor
     * @param actionLength Action length
     * @param sis SWF input stream
     * @throws IOException On I/O error
     */
    public ActionJump(int actionLength, SWFInputStream sis) throws IOException {
        super(0x99, actionLength, sis.getCharset());
        setJumpOffset(sis.readSI16("offset"));
    }

    @Override
    public void getRef(Set<Long> refs) {
        refs.add(getTargetAddress());
    }

    /**
     * Gets the target address
     * @return Address
     */
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
    public String getASMSource(ActionList container, Set<Long> knownAddresses, ScriptExportMode exportMode) {
        long address = getTargetAddress();
        String ofsStr = Helper.formatAddress(address);
        return "Jump loc" + ofsStr;
    }

    /**
     * Constructor
     * @param lexer Lexer
     * @param charset Charset
     * @throws IOException On I/O error
     * @throws ActionParseException On action parse error
     */
    public ActionJump(FlasmLexer lexer, String charset) throws IOException, ActionParseException {
        super(0x99, 2, charset);
        identifier = lexIdentifier(lexer);
    }

    @Override
    public String toString() {
        return "Jump " + offset;
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        lda.jump = getTargetAddress();
        return true;
    }

    @Override
    public boolean isJump() {
        return true;
    }

    @Override
    public List<Integer> getBranches(GraphSource code) {
        List<Integer> ret = super.getBranches(code);
        long targetAddress = getTargetAddress();
        int ofs = code.adr2pos(targetAddress);
        if (ofs == -1) {
            int length = getBytesLength();
            ofs = code.adr2pos(getAddress() + length);
            Logger.getLogger(ActionJump.class.getName()).log(Level.SEVERE, "Invalid jump to ofs{0} from ofs{1}", new Object[]{Helper.formatAddress(targetAddress), Helper.formatAddress(getAddress())});
        }
        ret.add(ofs);
        return ret;
    }
}
