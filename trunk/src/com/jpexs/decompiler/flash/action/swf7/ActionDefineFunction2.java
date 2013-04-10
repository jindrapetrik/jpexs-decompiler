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
package com.jpexs.decompiler.flash.action.swf7;

import com.jpexs.decompiler.flash.ReReadableInputStream;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.ParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.action.parser.pcode.Label;
import com.jpexs.decompiler.flash.action.treemodel.FunctionTreeItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.helpers.Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ActionDefineFunction2 extends Action implements GraphSourceItemContainer {

    public String functionName;
    public String replacedFunctionName;
    public List<String> paramNames = new ArrayList<String>();
    public List<String> replacedParamNames;
    public List<Integer> paramRegisters = new ArrayList<Integer>();
    public boolean preloadParentFlag;
    public boolean preloadRootFlag;
    public boolean suppressSuperFlag;
    public boolean preloadSuperFlag;
    public boolean suppressArgumentsFlag;
    public boolean preloadArgumentsFlag;
    public boolean suppressThisFlag;
    public boolean preloadThisFlag;
    public boolean preloadGlobalFlag;
    public int registerCount;
    public int codeSize;
    //public List<Action> code;
    private int version;
    public List<String> constantPool;
    private long hdrSize;

    public ActionDefineFunction2(String functionName, boolean preloadParentFlag, boolean preloadRootFlag, boolean suppressSuperFlag, boolean preloadSuperFlag, boolean suppressArgumentsFlag, boolean preloadArgumentsFlag, boolean suppressThisFlag, boolean preloadThisFlag, boolean preloadGlobalFlag, int registerCount, int codeSize, int version, List<String> paramNames, List<Integer> paramRegisters) {
        super(0x8E, 0);
        this.functionName = functionName;
        this.preloadParentFlag = preloadParentFlag;
        this.preloadRootFlag = preloadRootFlag;
        this.suppressSuperFlag = suppressSuperFlag;
        this.preloadSuperFlag = preloadSuperFlag;
        this.suppressArgumentsFlag = suppressArgumentsFlag;
        this.preloadArgumentsFlag = preloadArgumentsFlag;
        this.suppressThisFlag = suppressThisFlag;
        this.preloadThisFlag = preloadThisFlag;
        this.preloadGlobalFlag = preloadGlobalFlag;
        this.registerCount = registerCount;
        this.codeSize = codeSize;
        this.version = version;
        this.paramNames = paramNames;
        this.paramRegisters = paramRegisters;
    }

    public ActionDefineFunction2(int actionLength, SWFInputStream sis, ReReadableInputStream rri, int version) throws IOException {
        super(0x8E, actionLength);
        long posBef = sis.getPos();
        this.version = version;
        functionName = sis.readString();
        int numParams = sis.readUI16();
        registerCount = sis.readUI8();
        preloadParentFlag = sis.readUB(1) == 1;
        preloadRootFlag = sis.readUB(1) == 1;
        suppressSuperFlag = sis.readUB(1) == 1;
        preloadSuperFlag = sis.readUB(1) == 1;
        suppressArgumentsFlag = sis.readUB(1) == 1;
        preloadArgumentsFlag = sis.readUB(1) == 1;
        suppressThisFlag = sis.readUB(1) == 1;
        preloadThisFlag = sis.readUB(1) == 1;
        sis.readUB(7);//reserved
        preloadGlobalFlag = sis.readUB(1) == 1;
        for (int i = 0; i < numParams; i++) {
            paramRegisters.add(sis.readUI8());
            paramNames.add(sis.readString());
        }
        codeSize = sis.readUI16();
        long posAfter = sis.getPos();
        hdrSize = posAfter - posBef;
        //code = new ArrayList<Action>();
        int posBef2 = rri.getPos();
        //code = sis.readActionList(rri.getPos(), getFileAddress() + hdrSize, rri, codeSize);
        //rri.setPos(posBef2 + codeSize);
    }

    public ActionDefineFunction2(long containerSWFPos, boolean ignoreNops, List<Label> labels, long address, FlasmLexer lexer, List<String> constantPool, int version) throws IOException, ParseException {
        super(0x8E, -1);
        functionName = lexString(lexer);
        int numParams = (int) lexLong(lexer);
        registerCount = (int) lexLong(lexer);
        preloadParentFlag = lexBoolean(lexer);
        preloadRootFlag = lexBoolean(lexer);
        suppressSuperFlag = lexBoolean(lexer);
        preloadSuperFlag = lexBoolean(lexer);
        suppressArgumentsFlag = lexBoolean(lexer);
        preloadArgumentsFlag = lexBoolean(lexer);
        suppressThisFlag = lexBoolean(lexer);
        preloadThisFlag = lexBoolean(lexer);
        preloadGlobalFlag = lexBoolean(lexer);
        for (int i = 0; i < numParams; i++) {
            paramRegisters.add((int) lexLong(lexer));
            paramNames.add(lexString(lexer));
        }
        lexBlockOpen(lexer);
        //code = ASMParser.parse(containerSWFPos + getHeaderLength(), ignoreNops, labels, address + getPreLen(version), lexer, constantPool, version);
    }

    @Override
    public long getHeaderSize() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        try {
            sos.writeString(functionName);
            sos.writeUI16(paramNames.size());
            sos.writeUI8(registerCount);
            sos.writeUB(1, preloadParentFlag ? 1 : 0);
            sos.writeUB(1, preloadRootFlag ? 1 : 0);
            sos.writeUB(1, suppressSuperFlag ? 1 : 0);
            sos.writeUB(1, preloadSuperFlag ? 1 : 0);
            sos.writeUB(1, suppressArgumentsFlag ? 1 : 0);
            sos.writeUB(1, preloadArgumentsFlag ? 1 : 0);
            sos.writeUB(1, suppressThisFlag ? 1 : 0);
            sos.writeUB(1, preloadThisFlag ? 1 : 0);
            sos.writeUB(7, 0);
            sos.writeUB(1, preloadGlobalFlag ? 1 : 0);
            for (int i = 0; i < paramNames.size(); i++) {
                sos.writeUI8(paramRegisters.get(i));

                sos.writeString(paramNames.get(i));
            }
            sos.writeUI16(0);
            sos.close();
            baos2.write(surroundWithAction(baos.toByteArray(), version));
        } catch (IOException e) {
        }
        return baos2.toByteArray().length;
    }

    @Override
    public byte[] getBytes(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        try {
            sos.writeString(functionName);
            sos.writeUI16(paramNames.size());
            sos.writeUI8(registerCount);
            sos.writeUB(1, preloadParentFlag ? 1 : 0);
            sos.writeUB(1, preloadRootFlag ? 1 : 0);
            sos.writeUB(1, suppressSuperFlag ? 1 : 0);
            sos.writeUB(1, preloadSuperFlag ? 1 : 0);
            sos.writeUB(1, suppressArgumentsFlag ? 1 : 0);
            sos.writeUB(1, preloadArgumentsFlag ? 1 : 0);
            sos.writeUB(1, suppressThisFlag ? 1 : 0);
            sos.writeUB(1, preloadThisFlag ? 1 : 0);
            sos.writeUB(7, 0);
            sos.writeUB(1, preloadGlobalFlag ? 1 : 0);
            for (int i = 0; i < paramNames.size(); i++) {
                sos.writeUI8(paramRegisters.get(i));
                sos.writeString(paramNames.get(i));
            }
            //byte codeBytes[] = Action.actionsToBytes(code, false, version);
            sos.writeUI16(codeSize);//codeBytes.length);
            sos.close();


            baos2.write(surroundWithAction(baos.toByteArray(), version));
            //baos2.write(codeBytes);
        } catch (IOException e) {
        }
        return baos2.toByteArray();
    }

    private long getPreLen(int version) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            sos.writeString(functionName);
            sos.writeUI16(paramNames.size());
            sos.writeUI8(registerCount);
            sos.writeUB(1, preloadParentFlag ? 1 : 0);
            sos.writeUB(1, preloadRootFlag ? 1 : 0);
            sos.writeUB(1, suppressSuperFlag ? 1 : 0);
            sos.writeUB(1, preloadSuperFlag ? 1 : 0);
            sos.writeUB(1, suppressArgumentsFlag ? 1 : 0);
            sos.writeUB(1, preloadArgumentsFlag ? 1 : 0);
            sos.writeUB(1, suppressThisFlag ? 1 : 0);
            sos.writeUB(1, preloadThisFlag ? 1 : 0);
            sos.writeUB(7, 0);
            sos.writeUB(1, preloadGlobalFlag ? 1 : 0);
            for (int i = 0; i < paramNames.size(); i++) {
                sos.writeUI8(paramRegisters.get(i));
                sos.writeString(paramNames.get(i));
            }
            sos.writeUI16(0);
            sos.close();
        } catch (IOException e) {
        }
        return surroundWithAction(baos.toByteArray(), version).length;
    }

    @Override
    public void setAddress(long address, int version, boolean recursive) {
        super.setAddress(address, version, recursive);
        if (recursive) {
            //Action.setActionsAddresses(code, address + getPreLen(version), version);
        }
    }

    @Override
    public String getASMSourceReplaced(List<GraphSourceItem> container, List<Long> knownAddreses, List<String> constantPool, int version, boolean hex) {
        List<String> oldParamNames = paramNames;
        if (replacedParamNames != null) {
            paramNames = replacedParamNames;
        }
        String oldFunctionName = functionName;
        if (replacedFunctionName != null) {
            functionName = replacedFunctionName;
        }
        String ret = getASMSource(container, knownAddreses, constantPool, version, hex);
        paramNames = oldParamNames;
        functionName = oldFunctionName;
        return ret;

    }

    @Override
    public String getASMSource(List<GraphSourceItem> container, List<Long> knownAddreses, List<String> constantPool, int version, boolean hex) {
        String paramStr = "";
        for (int i = 0; i < paramNames.size(); i++) {
            paramStr += paramRegisters.get(i) + " \"" + Helper.escapeString(paramNames.get(i)) + "\"";
            paramStr += " ";
        }

        return ("DefineFunction2 \"" + Helper.escapeString(functionName) + "\" " + paramRegisters.size() + " " + registerCount
                + " " + preloadParentFlag
                + " " + preloadRootFlag
                + " " + suppressSuperFlag
                + " " + preloadSuperFlag
                + " " + suppressArgumentsFlag
                + " " + preloadArgumentsFlag
                + " " + suppressThisFlag
                + " " + preloadThisFlag
                + " " + preloadGlobalFlag).trim() + " " + paramStr + " {";// + "\r\n" + Action.actionsToString(getAddress() + getHeaderLength(), getItems(container), knownAddreses, constantPool, version, hex, getFileAddress() + hdrSize) + "}";
    }

    @Override
    public String toString() {
        return "DefineFunction2";
    }

    public int getFirstRegister() {
        int pos = 1;
        if (preloadThisFlag) {
            pos++;
        }
        if (preloadArgumentsFlag) {
            pos++;
        }
        if (preloadSuperFlag) {
            pos++;
        }
        if (preloadRootFlag) {
            pos++;
        }
        if (preloadParentFlag) {
            pos++;
        }
        if (preloadGlobalFlag) {
            pos++;
        }
        return pos;
    }

    @Override
    public void translate(Stack<GraphTargetItem> stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions) {
    }

    @Override
    public HashMap<Integer, String> getRegNames() {
        HashMap<Integer, String> funcRegNames = new HashMap<Integer, String>();
        for (int f = 0; f < paramNames.size(); f++) {
            int reg = paramRegisters.get(f);
            if (reg != 0) {
                funcRegNames.put(reg, paramNames.get(f));
            }
        }
        int pos = 1;
        if (preloadThisFlag) {
            funcRegNames.put(pos, "this");
            pos++;
        }
        if (preloadArgumentsFlag) {
            funcRegNames.put(pos, "arguments");
            pos++;
        }
        if (preloadSuperFlag) {
            funcRegNames.put(pos, "super");
            pos++;
        }
        if (preloadRootFlag) {
            funcRegNames.put(pos, "_root");
            pos++;
        }
        if (preloadParentFlag) {
            funcRegNames.put(pos, "_parent");
            pos++;
        }
        if (preloadGlobalFlag) {
            funcRegNames.put(pos, "_global");
            pos++;
        }
        return funcRegNames;
    }

    @Override
    public void translateContainer(List<List<GraphTargetItem>> content, Stack<GraphTargetItem> stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions) {
        FunctionTreeItem fti = new FunctionTreeItem(this, functionName, paramNames, content.get(0), constantPool, getFirstRegister());
        functions.put(functionName, fti);
        stack.push(fti);
    }

    @Override
    public List<Long> getAllRefs(int version) {
        return super.getAllRefs(version);//return Action.getActionsAllRefs(code, version);
    }

    @Override
    public List<Action> getAllIfsOrJumps() {
        return super.getAllIfsOrJumps(); //return Action.getActionsAllIfsOrJumps(code);
    }

    @Override
    public List<Long> getContainerSizes() {
        List<Long> ret = new ArrayList<Long>();
        ret.add((Long) (long) codeSize);
        return ret;
    }

    @Override
    public boolean parseDivision(int pos, long addr, FlasmLexer lexer) {
        codeSize = (int) (addr - getAddress() - getHeaderSize());
        return false;
    }

    @Override
    public String getASMSourceBetween(int pos) {
        return "";
    }
}
