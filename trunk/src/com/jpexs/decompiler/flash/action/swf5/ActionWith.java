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
package com.jpexs.decompiler.flash.action.swf5;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.clauses.WithActionItem;
import com.jpexs.decompiler.flash.action.parser.ParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.action.parser.pcode.Label;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.helpers.ReReadableInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ActionWith extends Action implements GraphSourceItemContainer {

    public int codeSize;
    public int version;

    public ActionWith(int codeSize) {
        super(0x94, 2);
        this.codeSize = codeSize;
    }

    @Override
    public boolean parseDivision(long size, FlasmLexer lexer) {
        codeSize = (int) (size - getHeaderSize());
        return false;
    }

    public ActionWith(int actionLength, SWFInputStream sis, ReReadableInputStream rri, int version) throws IOException {
        super(0x94, actionLength);
        codeSize = sis.readUI16();
        this.version = version;
    }

    public ActionWith(FlasmLexer lexer) throws IOException, ParseException {
        super(0x94, 2);
        lexBlockOpen(lexer);
    }

    @Override
    public void setAddress(long address, int version, boolean recursive) {
        super.setAddress(address, version, recursive);
    }

    @Override
    public byte[] getBytes(int version) {
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            sos.writeUI16(codeSize);//codeBytes.length);
            sos.close();
            baos2.write(surroundWithAction(baos.toByteArray(), version));
        } catch (IOException e) {
        }
        return baos2.toByteArray();
    }

    @Override
    public String getASMSource(List<? extends GraphSourceItem> container, List<Long> knownAddreses, List<String> constantPool, int version, boolean hex) {
        return "With {";
    }

    @Override
    public List<Long> getAllRefs(int version) {
        return super.getAllRefs(version);
    }

    @Override
    public List<Action> getAllIfsOrJumps() {
        return super.getAllIfsOrJumps();
    }

    @Override
    public void translateContainer(List<List<GraphTargetItem>> content, Stack<GraphTargetItem> stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions) {
        output.add(new WithActionItem(this, stack.pop(), content.get(0)));
    }

    @Override
    public List<Long> getContainerSizes() {
        List<Long> ret = new ArrayList<>();
        ret.add((Long) (long) codeSize);
        return ret;
    }

    @Override
    public void setContainerSize(int index, long size) {
        if (index == 0) {
            codeSize = (int) size;
        }
        else {
            throw new IllegalArgumentException("Index must be 0.");
        }
    }

    @Override
    public String getASMSourceBetween(int pos) {
        return "";
    }

    @Override
    public long getHeaderSize() {
        return surroundWithAction(new byte[]{0, 0}, version).length;
    }

    @Override
    public HashMap<Integer, String> getRegNames() {
        return new HashMap<>();
    }

    @Override
    public String getName() {
        return null;
    }
}
