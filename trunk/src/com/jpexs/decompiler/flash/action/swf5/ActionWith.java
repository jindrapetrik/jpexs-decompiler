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

import com.jpexs.decompiler.flash.ReReadableInputStream;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.ParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.action.parser.pcode.Label;
import com.jpexs.decompiler.flash.action.treemodel.clauses.WithTreeItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ActionWith extends Action implements GraphSourceItemContainer {

    //public List<Action> actions;
    public int codeSize;
    public int version;

    @Override
    public boolean parseDivision(int pos, long addr, FlasmLexer lexer) {
        codeSize = (int) (addr - getAddress() - getHeaderSize());
        return false;
    }

    public ActionWith(SWFInputStream sis, ReReadableInputStream rri, int version) throws IOException {
        super(0x94, 2);
        codeSize = sis.readUI16();
        this.version = version;
        //actions = new ArrayList<Action>();
        //actions = (new SWFInputStream(new ByteArrayInputStream(sis.readBytes(codeSize)), version)).readActionList(rri.getPos(), containerSWFOffset + getAddress() + 2, rri, codeSize);

    }

    public ActionWith(long containerSWFPos, boolean ignoreNops, List<Label> labels, long address, FlasmLexer lexer, List<String> constantPool, int version) throws IOException, ParseException {
        super(0x94, 2);
        lexBlockOpen(lexer);
        //actions = ASMParser.parse(containerSWFPos + 2, ignoreNops, labels, address + 5, lexer, constantPool, version);
    }

    @Override
    public void setAddress(long address, int version, boolean recursive) {
        super.setAddress(address, version, recursive);
        if (recursive) {
            //Action.setActionsAddresses(actions, address + 5, version);
        }
    }

    @Override
    public byte[] getBytes(int version) {
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            //byte codeBytes[] = Action.actionsToBytes(actions, false, version);
            sos.writeUI16(codeSize);//codeBytes.length);
            sos.close();
            baos2.write(surroundWithAction(baos.toByteArray(), version));
            //baos2.write(codeBytes);
        } catch (IOException e) {
        }
        return baos2.toByteArray();
    }

    @Override
    public String getASMSource(List<? extends GraphSourceItem> container, List<Long> knownAddreses, List<String> constantPool, int version, boolean hex) {
        return "With {"; // + "\r\n" + Action.actionsToString(getAddress() + 2, getItems(container), knownAddreses, constantPool, version, hex, containerSWFOffset + getAddress() + 2) + "}";
    }

    @Override
    public List<Long> getAllRefs(int version) {
        return super.getAllRefs(version);//Action.getActionsAllRefs(actions, version);
    }

    @Override
    public List<Action> getAllIfsOrJumps() {
        return super.getAllIfsOrJumps();// Action.getActionsAllIfsOrJumps(actions);
    }

    @Override
    public void translateContainer(List<List<GraphTargetItem>> content, Stack<GraphTargetItem> stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions) {
        output.add(new WithTreeItem(this, stack.pop(), content.get(0)));
    }

    @Override
    public List<Long> getContainerSizes() {
        List<Long> ret = new ArrayList<Long>();
        ret.add((Long) (long) codeSize);
        return ret;
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
        return new HashMap<Integer, String>();
    }
}
