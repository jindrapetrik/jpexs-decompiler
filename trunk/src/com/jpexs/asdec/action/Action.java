/*
 *  Copyright (C) 2010-2011 JPEXS
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

package com.jpexs.asdec.action;

import com.jpexs.asdec.Main;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.action.parser.FlasmLexer;
import com.jpexs.asdec.action.parser.ParseException;
import com.jpexs.asdec.action.parser.ParsedSymbol;
import com.jpexs.asdec.action.swf4.*;
import com.jpexs.asdec.action.swf5.*;
import com.jpexs.asdec.action.swf6.ActionEnumerate2;
import com.jpexs.asdec.action.swf6.ActionStrictEquals;
import com.jpexs.asdec.action.swf7.ActionDefineFunction2;
import com.jpexs.asdec.action.swf7.ActionTry;
import com.jpexs.asdec.action.treemodel.*;
import com.jpexs.asdec.action.treemodel.clauses.*;
import com.jpexs.asdec.action.treemodel.operations.AndTreeItem;
import com.jpexs.asdec.action.treemodel.operations.NotTreeItem;
import com.jpexs.asdec.action.treemodel.operations.OrTreeItem;
import com.jpexs.asdec.helpers.Helper;
import com.jpexs.asdec.helpers.Highlighting;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Represents one ACTIONRECORD,
 * also has some static method to work with Actions
 */
public class Action {
    /**
     * Action type identifier
     */
    public int actionCode;
    /**
     * Length of action data
     */
    public int actionLength;

    private long address;

    /**
     * String used to indent line when converting to string
     */
    public static final String INDENTOPEN = "INDENTOPEN";
    /**
     * String used to unindent line when converting to string
     */
    public static final String INDENTCLOSE = "INDENTCLOSE";

    /**
     * Names of ActionScript properties
     */
    public static final String[] propertyNames = new String[]{
            "_x",
            "_y",
            "_xscale",
            "_yscale",
            "_currentframe",
            "_totalframes",
            "_alpha",
            "_visible",
            "_width",
            "_height",
            "_rotation",
            "_target",
            "_framesloaded",
            "_name",
            "_droptarget",
            "_url",
            "_highquality",
            "_focusrect",
            "_soundbuftime",
            "_quality",
            "_xmouse",
            "_ymouse"
    };

    /**
     * Constructor
     *
     * @param actionCode   Action type identifier
     * @param actionLength Length of action data
     */
    public Action(int actionCode, int actionLength) {
        this.actionCode = actionCode;
        this.actionLength = actionLength;
    }


    /**
     * Returns address of this action
     *
     * @return
     */
    public long getAddress() {
        return address;
    }

    /**
     * Gets all addresses which are referenced from this action and/or subactions
     *
     * @param version SWF version
     * @return List of addresses
     */
    public List<Long> getAllRefs(int version) {
        List<Long> ret = new ArrayList<Long>();
        return ret;
    }

    /**
     * Gets all ActionIf or ActionJump actions from subactions
     *
     * @return List of actions
     */
    public List<Action> getAllIfsOrJumps() {
        List<Action> ret = new ArrayList<Action>();
        return ret;
    }


    /**
     * Gets all ActionIf or ActionJump actions from list of actions
     *
     * @param list List of actions
     * @return List of actions
     */
    public static List<Action> getActionsAllIfsOrJumps(List<Action> list) {
        List<Action> ret = new ArrayList<Action>();
        for (Action a : list) {
            List<Action> part = a.getAllIfsOrJumps();
            ret.addAll(part);
        }
        return ret;
    }

    /**
     * Gets all addresses which are referenced from the list of actions
     *
     * @param list    List of actions
     * @param version SWF version
     * @return List of addresses
     */
    public static List<Long> getActionsAllRefs(List<Action> list, int version) {
        List<Long> ret = new ArrayList<Long>();
        for (Action a : list) {
            List<Long> part = a.getAllRefs(version);
            ret.addAll(part);
        }
        return ret;
    }

    /**
     * Sets address of this instruction
     *
     * @param address Address
     * @param version SWF version
     */
    public void setAddress(long address, int version) {
        this.address = address;
    }


    /**
     * Returns a string representation of the object
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "Action" + actionCode;
    }

    /**
     * Reads String from FlasmLexer
     *
     * @param lex FlasmLexer
     * @return String value
     * @throws IOException
     * @throws ParseException When read object is not String
     */
    protected String lexString(FlasmLexer lex) throws IOException, ParseException {
        ParsedSymbol symb = lex.yylex();
        if (symb.type != ParsedSymbol.TYPE_STRING) throw new ParseException("String expected", lex.yyline());
        return (String) symb.value;
    }

    /**
     * Reads Block startServer from FlasmLexer
     *
     * @param lex FlasmLexer
     * @throws IOException
     * @throws ParseException When read object is not Block startServer
     */
    protected void lexBlockOpen(FlasmLexer lex) throws IOException, ParseException {
        ParsedSymbol symb = lex.yylex();
        if (symb.type != ParsedSymbol.TYPE_BLOCK_START) throw new ParseException("Block startServer ", lex.yyline());
    }

    /**
     * Reads Identifier from FlasmLexer
     *
     * @param lex FlasmLexer
     * @return Identifier name
     * @throws IOException
     * @throws ParseException When read object is not Identifier
     */
    protected String lexIdentifier(FlasmLexer lex) throws IOException, ParseException {
        ParsedSymbol symb = lex.yylex();
        if (symb.type != ParsedSymbol.TYPE_IDENTIFIER) throw new ParseException("Identifier expected", lex.yyline());
        return (String) symb.value;
    }

    /**
     * Reads long value from FlasmLexer
     *
     * @param lex FlasmLexer
     * @return long value
     * @throws IOException
     * @throws ParseException When read object is not long value
     */
    protected long lexLong(FlasmLexer lex) throws IOException, ParseException {
        ParsedSymbol symb = lex.yylex();
        if (symb.type != ParsedSymbol.TYPE_INTEGER) throw new ParseException("Integer expected", lex.yyline());
        return (Long) symb.value;
    }

    /**
     * Reads boolean value from FlasmLexer
     *
     * @param lex FlasmLexer
     * @return boolean value
     * @throws IOException
     * @throws ParseException When read object is not boolean value
     */
    protected boolean lexBoolean(FlasmLexer lex) throws IOException, ParseException {
        ParsedSymbol symb = lex.yylex();
        if (symb.type != ParsedSymbol.TYPE_BOOLEAN) throw new ParseException("Boolean expected", lex.yyline());
        return (Boolean) symb.value;
    }

    /**
     * Gets action converted to bytes
     *
     * @param version SWF version
     * @return Array of bytes
     */
    public byte[] getBytes(int version) {
        byte ret[] = new byte[1];
        ret[0] = (byte) actionCode;
        return ret;
    }

    /**
     * Surrounds byte array with Action header
     *
     * @param data    Byte array
     * @param version SWF version
     * @return Byte array
     */
    protected byte[] surroundWithAction(byte[] data, int version) {
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        SWFOutputStream sos2 = new SWFOutputStream(baos2, version);
        try {
            sos2.writeUI8(actionCode);
            sos2.writeUI16(data.length);
            sos2.write(data);
            sos2.close();
        } catch (IOException e) {

        }
        return baos2.toByteArray();
    }

    /**
     * Converts list of Actions to bytes
     *
     * @param list    List of actions
     * @param addZero Whether or not to add 0 UI8 value to the end
     * @param version SWF version
     * @return Array of bytes
     */
    public static byte[] actionsToBytes(List<Action> list, boolean addZero, int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (Action a : list) {
            try {
                baos.write(a.getBytes(version));
            } catch (IOException e) {

            }
        }
        if (addZero) baos.write(0);
        return baos.toByteArray();
    }

    /**
     * Set addresses of actions in the list
     *
     * @param list        List of actions
     * @param baseAddress Address of first action in the list
     * @param version     SWF version
     */
    public static void setActionsAddresses(List<Action> list, long baseAddress, int version) {
        long offset = baseAddress;
        for (Action a : list) {
            a.setAddress(offset, version);
            offset += a.getBytes(version).length;
        }
    }


    /**
     * Converts list of actions to ASM source
     *
     * @param list             List of actions
     * @param importantOffsets List of important offsets to mark as labels
     * @param version          SWF version
     * @return ASM source as String
     */
    public static String actionsToString(List<Action> list, List<Long> importantOffsets, int version) {
        return actionsToString(list, importantOffsets, new ArrayList<String>(), version);
    }

    /**
     * Converts list of actions to ASM source
     *
     * @param list             List of actions
     * @param importantOffsets List of important offsets to mark as labels
     * @param constantPool     Constant pool
     * @param version          SWF version
     * @return ASM source as String
     */
    public static String actionsToString(List<Action> list, List<Long> importantOffsets, List<String> constantPool, int version) {
        String ret = "";
        long offset = 0;
        if (importantOffsets == null) {
            setActionsAddresses(list, 0, version);
            importantOffsets = getActionsAllRefs(list, version);
        }

        offset = 0;
        if(Main.LATEST_CONSTANTPOOL_HACK){
           for (Action a : list) {
            if (a instanceof ActionConstantPool) {                
                constantPool.clear();
                constantPool.addAll(((ActionConstantPool) a).constantPool);
            }
           }
        }
        for (Action a : list) {
            if(!Main.LATEST_CONSTANTPOOL_HACK)
            {
            if (a instanceof ActionConstantPool) {                
                constantPool.clear();
                constantPool.addAll(((ActionConstantPool) a).constantPool);
            }
            }
            if (a instanceof ActionPush) {
                ((ActionPush) a).constantPool = constantPool;
            }
            offset = a.getAddress();
            if (importantOffsets.contains(offset)) {
                ret += "loc" + Helper.formatAddress(offset) + ":";
            }
            offset += a.getBytes(version).length;
            ret += a.getASMSource(importantOffsets, constantPool, version) + "\r\n";
        }
        if (importantOffsets.contains(offset)) {
            ret += "loc" + Helper.formatAddress(offset) + ":\r\n";
        }
        return ret;
    }

    /**
     * Convert action to ASM source
     *
     * @param knownAddreses List of important offsets to mark as labels
     * @param constantPool  Constant pool
     * @param version       SWF version
     * @return
     */
    public String getASMSource(List<Long> knownAddreses, List<String> constantPool, int version) {
        return toString();
    }

    /**
     * Translates this function to stack and output.
     *
     * @param stack     Stack
     * @param constants Constant pool
     * @param output    Output
     * @param regNames  Register names
     */
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {

    }

    /**
     * Pops long value off the stack
     *
     * @param stack Stack
     * @return long value
     */
    protected long popLong(Stack<TreeItem> stack) {
        TreeItem item = stack.pop();
        if (item instanceof DirectValueTreeItem) {
            if (((DirectValueTreeItem) item).value instanceof Long) {
                return (long) (Long) ((DirectValueTreeItem) item).value;
            }
        }
        return 0;
    }

    /**
     * Converts action index to address in the specified list of actions
     *
     * @param actions List of actions
     * @param ip      Action index
     * @param version SWF version
     * @return address
     */
    public static long ip2adr(List<Action> actions, int ip, int version) {
        if (ip >= actions.size()) {
            if (actions.size() == 0) return 0;
            return actions.get(actions.size() - 1).getAddress() + actions.get(actions.size() - 1).getBytes(version).length;
        }
        if(ip==-1)
        {
           return 0;
        }
        return actions.get(ip).getAddress();
    }

    /**
     * Converts address to action index in the specified list of actions
     *
     * @param actions List of actions
     * @param addr    Address
     * @param version SWF version
     * @return action index
     */
    public static int adr2ip(List<Action> actions, long addr, int version) {
        for (int ip = 0; ip < actions.size(); ip++) {
            if (actions.get(ip).getAddress() == addr) return ip;
        }
        if (actions.size() > 0) {
            long outpos = actions.get(actions.size() - 1).getAddress() + actions.get(actions.size() - 1).getBytes(version).length;
            if (addr == outpos) {
                return actions.size();
            }
        }
        return -1;
    }


    /**
     * Converts list of TreeItems to string
     *
     * @param tree List of TreeItem
     * @return String
     */
    public static String treeToString(List<TreeItem> tree) {
        String ret = "";
        for (TreeItem ti : tree) {
            ret += ti.toString() + "\r\n";
        }
        String parts[] = ret.split("\r\n");
        ret = "";


        try {
            Stack<String> loopStack = new Stack<String>();
            for (int p = 0; p < parts.length; p++) {
                String stripped = Highlighting.stripHilights(parts[p]);
                if (stripped.endsWith(":") && (!stripped.startsWith("case ")) && (!stripped.equals("default:"))) {
                    loopStack.add(stripped.substring(0, stripped.length() - 1));
                }
                if (stripped.startsWith("break ")) {
                    if (stripped.equals("break " + loopStack.peek() + ";")) {
                        parts[p] = parts[p].replace(" " + loopStack.peek(), "");
                    }
                }
                if (stripped.startsWith("continue ")) {
                    if (loopStack.size() > 0) {
                        if (stripped.equals("continue " + loopStack.peek() + ";")) {
                            parts[p] = parts[p].replace(" " + loopStack.peek(), "");
                        }
                    }
                }
                if (stripped.startsWith(":")) {
                    loopStack.pop();
                }
            }
        } catch (Exception ex) {
        }

        int level = 0;
        for (int p = 0; p < parts.length; p++) {
            String strippedP = Highlighting.stripHilights(parts[p]);
            if (strippedP.endsWith(":") && (!strippedP.startsWith("case ")) && (!strippedP.equals("default:"))) {
                String loopname = strippedP.substring(0, strippedP.length() - 1);
                boolean dorefer = false;
                for (int q = p + 1; q < parts.length; q++) {
                    String strippedQ = Highlighting.stripHilights(parts[q]);
                    if (strippedQ.equals("break " + loopname + ";")) {
                        dorefer = true;
                        break;
                    }
                    if (strippedQ.equals("continue " + loopname + ";")) {
                        dorefer = true;
                        break;
                    }
                    if (strippedQ.equals(":" + loopname)) {
                        break;
                    }
                }
                if (!dorefer) {
                    continue;
                }
            }
            if (strippedP.startsWith(":")) {
                continue;
            }
            if (Highlighting.stripHilights(parts[p]).equals(INDENTOPEN)) {
                level++;
                continue;
            }
            if (Highlighting.stripHilights(parts[p]).equals(INDENTCLOSE)) {
                level--;
                continue;
            }
            if (Highlighting.stripHilights(parts[p]).equals("}")) level--;
            if (Highlighting.stripHilights(parts[p]).equals("};")) level--;
            ret += tabString(level) + parts[p] + "\r\n";
            if (Highlighting.stripHilights(parts[p]).equals("{")) level++;
        }
        return ret;
    }

    private static final String INDENT_STRING = "   ";


    private static String tabString(int len) {
        String ret = "";
        for (int i = 0; i < len; i++) {
            ret += INDENT_STRING;
        }
        return ret;
    }

    /**
     * Converts list of actions to ActionScript source code
     *
     * @param actions List of actions
     * @param version SWF version
     * @return String with Source code
     */
    public static String actionsToSource(List<Action> actions, int version) {
        List<TreeItem> tree = actionsToTree(new HashMap<Integer,String>(),actions, version);
        return treeToString(tree);
    }

    /**
     * Converts list of actions to List of treeItems
     *
     * @param regNames Register names
     * @param actions List of actions
     * @param version SWF version
     * @return List of treeItems
     */
    public static List<TreeItem> actionsToTree(HashMap<Integer,String> regNames,List<Action> actions, int version) {
        return actionsToTree(regNames,new ArrayList<Long>(), new ArrayList<Loop>(), getActionsAllIfsOrJumps(actions), new Stack<TreeItem>(), new ConstantPool(), actions, 0, actions.size() - 1, version);
    }

    private static Stack<TreeItem> actionsToStackTree(HashMap<Integer,String> regNames,List<Action> jumpsOrIfs, List<Action> actions, ConstantPool constants, int start, int end, int version) {
        Stack<TreeItem> ret = new Stack<TreeItem>();
        actionsToTree(regNames,new ArrayList<Long>(), new ArrayList<Loop>(), jumpsOrIfs, ret, constants, actions, start, end, version);
        return ret;
    }


    private static class Loop {

        public long loopContinue;
        public long loopBreak;
        public int continueCount = 0;
        public int breakCount = 0;

        public Loop(long loopContinue, long loopBreak) {
            this.loopContinue = loopContinue;
            this.loopBreak = loopBreak;
        }
    }

    private static List<TreeItem> actionsToTree(HashMap<Integer,String> registerNames,List<Long> unknownJumps, List<Loop> loopList, List<Action> jumpsOrIfs, Stack<TreeItem> stack, ConstantPool constants, List<Action> actions, int start, int end, int version) {
        List<TreeItem> output = new ArrayList<TreeItem>();
        int ip = start;
        boolean isWhile = false;
        boolean isForIn = false;
        TreeItem inItem=null;
        int loopStart = 0;
        loopip:
        while (ip <= end + 1) {

            long addr = ip2adr(actions, ip, version);
            if (unknownJumps.contains(addr)) {
                unknownJumps.remove(new Long(addr));
                boolean switchFound = false;
                for (int i = output.size() - 1; i >= 0; i--) {
                    if (output.get(i) instanceof SwitchTreeItem) {
                        if (((SwitchTreeItem) output.get(i)).defaultCommands == null) {
                            List<ContinueTreeItem> continues = ((SwitchTreeItem) output.get(i)).getContinues();
                            boolean breakFound = false;
                            for (ContinueTreeItem cti : continues) {
                                if (cti.loopPos == addr) {
                                    cti.isKnown = true;
                                    cti.isBreak = true;
                                    ((SwitchTreeItem) output.get(i)).loopBreak = addr;
                                    breakFound = true;
                                }
                            }
                            if (breakFound) {
                                switchFound = true;
                                ((SwitchTreeItem) output.get(i)).defaultCommands = new ArrayList<TreeItem>();
                                for (int k = i + 1; k < output.size(); k++) {
                                    ((SwitchTreeItem) output.get(i)).defaultCommands.add(output.remove(i + 1));
                                }
                            }
                        }
                        break;
                    }
                }
                if (!switchFound) {
                    throw new UnknownJumpException(stack, addr, output);
                }
            }
            if (ip > end) break;
            Action action = actions.get(ip);
            for (int j = 0; j < jumpsOrIfs.size(); j++) {
                Action jif = jumpsOrIfs.get(j);
                if (jif instanceof ActionIf) {
                    if (((ActionIf) jif).getRef(version) == addr) {
                        if (jif.getAddress() > addr) {
                            jumpsOrIfs.remove(j);
                            List<TreeItem> doBody = actionsToTree(registerNames,unknownJumps, loopList, jumpsOrIfs, stack, constants, actions, ip, adr2ip(actions, jif.getAddress(), version) - 1, version);
                            Loop currentLoop = new Loop(ip, adr2ip(actions, jif.getAddress(), version) + 1);
                            loopList.add(currentLoop);
                            output.add(new DoWhileTreeItem(action, adr2ip(actions, jif.getAddress(), version) + 1, ip, doBody, stack.pop()));
                            ip = adr2ip(actions, jif.getAddress(), version) + 1;
                            continue loopip;
                        }
                    }
                }

            }
            for (int j = 0; j < jumpsOrIfs.size(); j++) {
                Action jif = jumpsOrIfs.get(j);
                if (jif instanceof ActionJump) {
                    if (((ActionJump) jif).getRef(version) == addr) {
                        if (jif.getAddress() > addr) {
                            isWhile = true;
                            loopStart = ip;
                            break;
                        }
                    }
                }
            }
            if (action instanceof ActionJump) {
                int jumpIp = adr2ip(actions, ((ActionJump) action).getRef(version), version);
                //if (jumpIp > ip) {
                for (Loop l : loopList) {
                    if (l.loopBreak == ((ActionJump) action).getRef(version)) {
                        output.add(new BreakTreeItem(action, l.loopBreak));
                        ip = ip + 1;
                        continue loopip;
                    }
                    if (l.loopContinue == ((ActionJump) action).getRef(version)) {
                        l.continueCount++;
                        output.add(new ContinueTreeItem(action, l.loopBreak));
                        ip = ip + 1;
                        continue loopip;
                    }
                }

                output.add(new ContinueTreeItem(action, ((ActionJump) action).getRef(version), false));

                if (!unknownJumps.contains(((ActionJump) action).getRef(version))) {
                    unknownJumps.add(((ActionJump) action).getRef(version));
                }
                ip = ip + 1;
                break;

            } else if (action instanceof ActionIf) {
                int jumpIp = adr2ip(actions, ((ActionIf) action).getRef(version), version);
                if (jumpIp < ip) {
                    output.add(new UnsupportedTreeItem(action, "ActionIf to jump back"));
                    break;
                }
                TreeItem expression = null;
                if(!isForIn){
                    expression=stack.pop();
                    if (expression instanceof NotTreeItem) {
                        expression = ((NotTreeItem) expression).value;
                    } else {
                        expression = new NotTreeItem(action, expression);
                    }
                }
                List<TreeItem> onTrue = new ArrayList<TreeItem>();
                List<TreeItem> onFalse = new ArrayList<TreeItem>();
                boolean hasElse = false;
                int jumpElseIp = 0;
                Stack<TreeItem> falseStack = new Stack<TreeItem>();
                Stack<TreeItem> trueStack = new Stack<TreeItem>();
                //if (!isWhile) {
                if (actions.get(jumpIp - 1) instanceof ActionJump) {
                    long ref = ((ActionJump) actions.get(jumpIp - 1)).getRef(version);
                    int refIp = adr2ip(actions, ref, version);
                    if ((refIp > jumpIp) && (refIp <= end + 1)) {
                        hasElse = true;
                        jumpElseIp = adr2ip(actions, ((ActionJump) actions.get(jumpIp - 1)).getRef(version), version);
                        onFalse = actionsToTree(registerNames,unknownJumps, loopList, jumpsOrIfs, falseStack, constants, actions, jumpIp, jumpElseIp - 1, version);
                    }
                }
                //}
                Loop currentLoop = null;
                if (isWhile||isForIn) {
                    currentLoop = new Loop(loopStart, jumpIp);
                    loopList.add(currentLoop);
                }
                boolean isFor = false;
                boolean isTernar = false;
                List<TreeItem> finalExpression = null;

                TreeItem variableName=null;
                if(isForIn){
                    for(int t=ip+1;t<=end;t++){
                        Action actionSV=actions.get(t);
                        actionSV.translate(stack, constants, output, registerNames);
                        if(actionSV instanceof ActionSetVariable){
                            SetVariableTreeItem svt=(SetVariableTreeItem)output.remove(output.size()-1);
                            variableName=svt.name;
                            ip=t;
                            break;
                        }
                    }
                }

                try {

                    onTrue = actionsToTree(registerNames,unknownJumps, loopList, jumpsOrIfs, trueStack, constants, actions, ip + 1, jumpIp - 1 - (hasElse || isWhile || isForIn ? 1 : 0), version);
                    if (onTrue.size() == 0 && trueStack.size() > 0) {
                        isTernar = true;
                    }
                } catch (UnknownJumpException uje) {
                    if ((adr2ip(actions, uje.addr, version) >= start) && (adr2ip(actions, uje.addr, version) <= end)) {
                        currentLoop.loopContinue = uje.addr;
                        onTrue = uje.output;
                        List<ContinueTreeItem> contList = new ArrayList<ContinueTreeItem>();
                        for (TreeItem ti : onTrue) {
                            if (ti instanceof ContinueTreeItem) {
                                contList.add((ContinueTreeItem) ti);
                            }
                            if (ti instanceof Block) {
                                List<ContinueTreeItem> subcont = ((Block) ti).getContinues();
                                for (int k = 0; k < subcont.size(); k++)
                                    contList.add(subcont.get(k));
                            }
                        }
                        for (int u = 0; u < contList.size(); u++) {
                            if (contList.get(u) instanceof ContinueTreeItem) {
                                if (((ContinueTreeItem) contList.get(u)).loopPos == uje.addr) {
                                    if (!((ContinueTreeItem) contList.get(u)).isKnown) {
                                        ((ContinueTreeItem) contList.get(u)).isKnown = true;
                                        ((ContinueTreeItem) contList.get(u)).loopPos = currentLoop.loopBreak;
                                    }
                                }
                            }
                        }
                        finalExpression = actionsToTree(registerNames,unknownJumps, loopList, jumpsOrIfs, stack, constants, actions, adr2ip(actions, uje.addr, version), jumpIp - 2, version);
                        isFor = true;
                    } else {
                        //throw new ConvertException("Unknown pattern: jump to nowhere", ip);
                    }
                }
                if(isForIn){
                    output.add(new ForInTreeItem(action,jumpIp, loopStart,variableName,inItem,onTrue));
                }else if (isFor) {
                    output.add(new ForTreeItem(action, currentLoop.loopBreak, currentLoop.loopContinue, new ArrayList<TreeItem>(), expression, finalExpression, onTrue));
                } else if (isTernar) {
                    stack.push(new TernarOpTreeItem(action, expression, trueStack.pop(), falseStack.pop()));
                } else if (isWhile) {
                    output.add(new WhileTreeItem(action, jumpIp, loopStart, expression, onTrue));
                } else {
                    output.add(new IfTreeItem(action, expression, onTrue, onFalse));
                }
                ip = (hasElse ? jumpElseIp : jumpIp);
                isWhile = false;
                isFor = false;
                isForIn = false;
                continue;
            } else if((action instanceof ActionEnumerate2)||(action instanceof ActionEnumerate)){
               loopStart=ip+1;
               isForIn=true;
               inItem=stack.pop();
               ip+=4;
               continue;
            }else if (action instanceof ActionTry) {
                ActionTry atry = (ActionTry) action;
                List<TreeItem> tryCommands = actionsToTree(registerNames,unknownJumps, loopList, jumpsOrIfs, new Stack<TreeItem>(), constants, atry.tryBody, 0, atry.tryBody.size() - 1, version);
                TreeItem catchName;
                if (atry.catchInRegisterFlag) {
                    catchName = new DirectValueTreeItem(atry, new RegisterNumber(atry.catchRegister), constants);
                } else {
                    catchName = new DirectValueTreeItem(atry, atry.catchName, constants);
                }
                List<TreeItem> catchExceptions = new ArrayList<TreeItem>();
                catchExceptions.add(catchName);
                List<List<TreeItem>> catchCommands = new ArrayList<List<TreeItem>>();
                catchCommands.add(actionsToTree(registerNames,unknownJumps, loopList, jumpsOrIfs, new Stack<TreeItem>(), constants, atry.catchBody, 0, atry.catchBody.size() - 1, version));
                List<TreeItem> finallyCommands = actionsToTree(registerNames,unknownJumps, loopList, jumpsOrIfs, new Stack<TreeItem>(), constants, atry.finallyBody, 0, atry.finallyBody.size() - 1, version);
                output.add(new TryTreeItem(tryCommands, catchExceptions, catchCommands, finallyCommands));
            } else if (action instanceof ActionWith) {
                ActionWith awith = (ActionWith) action;
                List<TreeItem> withCommands = actionsToTree(registerNames,unknownJumps, loopList, jumpsOrIfs, new Stack<TreeItem>(), constants, awith.actions, 0, awith.actions.size() - 1, version);
                output.add(new WithTreeItem(action, stack.pop(), withCommands));
            } else if (action instanceof ActionDefineFunction) {
                FunctionTreeItem fti = new FunctionTreeItem(action,((ActionDefineFunction)action).functionName,((ActionDefineFunction)action).paramNames, actionsToTree(registerNames,unknownJumps, loopList, jumpsOrIfs, new Stack<TreeItem>(), constants, ((ActionDefineFunction) action).code, 0, ((ActionDefineFunction) action).code.size() - 1, version), constants);
                stack.push(fti);
            } else if (action instanceof ActionDefineFunction2) {
                HashMap<Integer,String> funcRegNames=(HashMap<Integer,String>)registerNames.clone();
                for(int f=0;f<((ActionDefineFunction2)action).paramNames.size();f++){
                    int reg=((ActionDefineFunction2)action).paramRegisters.get(f);
                    if(reg!=0)
                    funcRegNames.put(reg, ((ActionDefineFunction2)action).paramNames.get(f));
                }
                int pos=1;
                if(((ActionDefineFunction2)action).preloadThisFlag) {funcRegNames.put(pos, "this");pos++;}
                if(((ActionDefineFunction2)action).preloadArgumentsFlag) {funcRegNames.put(pos, "arguments");pos++;}
                if(((ActionDefineFunction2)action).preloadSuperFlag) {funcRegNames.put(pos, "super");pos++;}
                if(((ActionDefineFunction2)action).preloadRootFlag) {funcRegNames.put(pos, "_root");pos++;}
                if(((ActionDefineFunction2)action).preloadParentFlag) {funcRegNames.put(pos, "_parent");pos++;}
                if(((ActionDefineFunction2)action).preloadGlobalFlag) {funcRegNames.put(pos, "_global");pos++;}

                FunctionTreeItem fti = new FunctionTreeItem(action,((ActionDefineFunction2)action).functionName,((ActionDefineFunction2)action).paramNames, actionsToTree(funcRegNames,unknownJumps, loopList, jumpsOrIfs, new Stack<TreeItem>(), constants, ((ActionDefineFunction2) action).code, 0, ((ActionDefineFunction2) action).code.size() - 1, version), constants);
                stack.push(fti);
            } else if (action instanceof ActionPushDuplicate) {
                do {
                    if (actions.get(ip + 1) instanceof ActionNot) {
                        if (actions.get(ip + 2) instanceof ActionIf) {
                            int nextPos = adr2ip(actions, ((ActionIf) actions.get(ip + 2)).getRef(version), version);
                            stack.push(new AndTreeItem(action, stack.pop(), actionsToStackTree(registerNames,jumpsOrIfs, actions, constants, ip + 4 /*je tam pop*/, nextPos - 1, version).pop()));
                            ip = nextPos;
                        } else {
                            output.add(new UnsupportedTreeItem(action, "ActionPushDuplicate with Not"));
                            break;
                        }
                    } else if (actions.get(ip + 1) instanceof ActionIf) {
                        int nextPos = adr2ip(actions, ((ActionIf) actions.get(ip + 1)).getRef(version), version);
                        stack.push(new OrTreeItem(action, stack.pop(), actionsToStackTree(registerNames,jumpsOrIfs, actions, constants, ip + 3, nextPos - 1, version).pop()));
                        ip = nextPos;
                    } else {
                        output.add(new UnsupportedTreeItem(action, "ActionPushDuplicate with no If"));
                        break loopip;
                    }
                    action = actions.get(ip);
                }
                while (action instanceof ActionPushDuplicate);
                continue;
            } else if (action instanceof ActionStoreRegister) {
                if ((ip + 1 <= end) && (actions.get(ip + 1) instanceof ActionPop)) {
                    action.translate(stack, constants, output, registerNames);
                    stack.pop();
                    ip++;
                } else {
                    action.translate(stack, constants, output, registerNames);
                }
            } else if (action instanceof ActionStrictEquals) {
                if ((ip + 1 < actions.size()) && (actions.get(ip + 1) instanceof ActionIf)) {
                    List<TreeItem> caseValues = new ArrayList<TreeItem>();
                    List<List<TreeItem>> caseCommands = new ArrayList<List<TreeItem>>();
                    caseValues.add(stack.pop());
                    TreeItem switchedObject = stack.pop();
                    if (output.size() > 0) {
                        if (output.get(output.size() - 1) instanceof StoreRegisterTreeItem) {
                            output.remove(output.size() - 1);
                        }
                    }
                    int caseStart = ip + 2;
                    List<Integer> caseBodyIps = new ArrayList<Integer>();
                    long defaultAddr = 0;
                    caseBodyIps.add(adr2ip(actions, ((ActionIf) actions.get(ip + 1)).getRef(version), version));
                    ip++;
                    do {
                        ip++;
                        if ((actions.get(ip - 1) instanceof ActionStrictEquals) && (actions.get(ip) instanceof ActionIf)) {
                            caseValues.add(actionsToStackTree(registerNames,jumpsOrIfs, actions, constants, caseStart, ip - 2, version).pop());
                            caseStart = ip + 1;
                            caseBodyIps.add(adr2ip(actions, ((ActionIf) actions.get(ip)).getRef(version), version));
                            if (actions.get(ip + 1) instanceof ActionJump) {
                                defaultAddr = ((ActionJump) actions.get(ip + 1)).getRef(version);
                                ip = adr2ip(actions, defaultAddr, version);
                                break;
                            }
                        }
                    } while (ip < end);
                    for (int i = 0; i < caseBodyIps.size(); i++) {
                        int caseEnd = ip - 1;
                        if (i < caseBodyIps.size() - 1) {
                            caseEnd = caseBodyIps.get(i + 1) - 1;
                        }
                        caseCommands.add(actionsToTree(registerNames,unknownJumps, loopList, jumpsOrIfs, stack, constants, actions, caseBodyIps.get(i), caseEnd, version));
                    }
                    output.add(new SwitchTreeItem(action, defaultAddr, switchedObject, caseValues, caseCommands, null));
                    continue;
                } else {
                    action.translate(stack, constants, output, registerNames);
                }
            } else {
                try {
                    action.translate(stack, constants, output, registerNames);
                } catch (EmptyStackException ese) {
                    output.add(new UnsupportedTreeItem(action, "Empty stack"));
                }
            }

            ip++;
        }
        if (stack.size() > 0) {
            for (int i = stack.size() - 1; i >= 0; i--) {
                if (stack.get(i) instanceof FunctionTreeItem) {
                    output.add(0, stack.get(i));
                    stack.remove(i);
                }
            }
        }
        return output;
    }
}
