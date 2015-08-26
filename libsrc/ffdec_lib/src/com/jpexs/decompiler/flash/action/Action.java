/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.AppResources;
import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.DisassemblyListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.model.ActionItem;
import com.jpexs.decompiler.flash.action.model.ConstantPool;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.ExtendsActionItem;
import com.jpexs.decompiler.flash.action.model.FunctionActionItem;
import com.jpexs.decompiler.flash.action.model.GetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.GetPropertyActionItem;
import com.jpexs.decompiler.flash.action.model.GetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.ImplementsOpActionItem;
import com.jpexs.decompiler.flash.action.model.NewObjectActionItem;
import com.jpexs.decompiler.flash.action.model.SetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.SetPropertyActionItem;
import com.jpexs.decompiler.flash.action.model.SetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.StoreRegisterActionItem;
import com.jpexs.decompiler.flash.action.model.TemporaryRegister;
import com.jpexs.decompiler.flash.action.model.UnsupportedActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.ClassActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.InterfaceActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParsedSymbol;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.action.parser.script.VariableActionItem;
import com.jpexs.decompiler.flash.action.special.ActionEnd;
import com.jpexs.decompiler.flash.action.special.ActionStore;
import com.jpexs.decompiler.flash.action.swf4.ActionEquals;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionNot;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionConstantPool;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionEquals2;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateException;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.CommentItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.PopItem;
import com.jpexs.decompiler.graph.model.ScriptEndItem;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents one ACTIONRECORD, also has some static method to work with Actions
 */
public abstract class Action implements GraphSourceItem {

    private static final int INFORM_LISTENER_RESOLUTION = 100;

    private boolean ignored = false;

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
     * Names of ActionScript properties
     */
    public static final String[] propertyNames = new String[]{
        "_X",
        "_Y",
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

    public static final List<String> propertyNamesList = Arrays.asList(propertyNames);

    private static final Logger logger = Logger.getLogger(Action.class.getName());

    /**
     * Constructor
     *
     * @param actionCode Action type identifier
     * @param actionLength Length of action data
     */
    public Action(int actionCode, int actionLength) {
        this.actionCode = actionCode;
        this.actionLength = actionLength;
    }

    public Action() {
    }

    /**
     * Returns address of this action
     *
     * @return address of this action
     */
    public long getAddress() {
        return address;
    }

    /**
     * Gets all addresses which are referenced from this action and/or
     * subactions
     *
     * @param refs list of addresses
     */
    public void getRef(Set<Long> refs) {
    }

    /**
     * Gets all addresses which are referenced from the list of actions
     *
     * @param list List of actions
     * @return List of addresses
     */
    public static Set<Long> getActionsAllRefs(List<Action> list) {
        Set<Long> ret = new HashSet<>();
        for (Action a : list) {
            a.getRef(ret);
        }
        return ret;
    }

    public int getTotalActionLength() {
        return actionLength + 1 + (actionCode >= 0x80 ? 2 : 0);
    }

    /**
     * Sets address of this instruction
     *
     * @param address Address
     */
    public void setAddress(long address) {
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
     * @throws ActionParseException When read object is not String
     */
    protected String lexString(FlasmLexer lex) throws IOException, ActionParseException {
        ASMParsedSymbol symb = lex.yylex();
        if (symb.type != ASMParsedSymbol.TYPE_STRING) {
            throw new ActionParseException("String expected", lex.yyline());
        }
        return (String) symb.value;
    }

    /**
     * Reads Block startServer from FlasmLexer
     *
     * @param lex FlasmLexer
     * @throws IOException
     * @throws ActionParseException When read object is not Block startServer
     */
    protected void lexBlockOpen(FlasmLexer lex) throws IOException, ActionParseException {
        ASMParsedSymbol symb = lex.yylex();
        if (symb.type != ASMParsedSymbol.TYPE_BLOCK_START) {
            throw new ActionParseException("Block startServer ", lex.yyline());
        }
    }

    /**
     * Reads Identifier from FlasmLexer
     *
     * @param lex FlasmLexer
     * @return Identifier name
     * @throws IOException
     * @throws ActionParseException When read object is not Identifier
     */
    protected String lexIdentifier(FlasmLexer lex) throws IOException, ActionParseException {
        ASMParsedSymbol symb = lex.yylex();
        if (symb.type != ASMParsedSymbol.TYPE_IDENTIFIER) {
            throw new ActionParseException("Identifier expected", lex.yyline());
        }
        return (String) symb.value;
    }

    /**
     * Reads long value from FlasmLexer
     *
     * @param lex FlasmLexer
     * @return long value
     * @throws IOException
     * @throws ActionParseException When read object is not long value
     */
    protected long lexLong(FlasmLexer lex) throws IOException, ActionParseException {
        ASMParsedSymbol symb = lex.yylex();
        if (symb.type != ASMParsedSymbol.TYPE_INTEGER) {
            throw new ActionParseException("Integer expected", lex.yyline());
        }
        return (Long) symb.value;
    }

    /**
     * Reads boolean value from FlasmLexer
     *
     * @param lex FlasmLexer
     * @return boolean value
     * @throws IOException
     * @throws ActionParseException When read object is not boolean value
     */
    protected boolean lexBoolean(FlasmLexer lex) throws IOException, ActionParseException {
        ASMParsedSymbol symb = lex.yylex();
        if (symb.type != ASMParsedSymbol.TYPE_BOOLEAN) {
            throw new ActionParseException("Boolean expected", lex.yyline());
        }
        return (Boolean) symb.value;
    }

    /**
     * Gets action converted to bytes
     *
     * @param version SWF version
     * @return Array of bytes
     */
    public final byte[] getBytes(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            getContentBytes(sos);
            sos.close();
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return surroundWithAction(baos.toByteArray(), version);
    }

    protected void getContentBytes(SWFOutputStream sos) throws IOException {
    }

    /**
     * Gets the length of action converted to bytes
     *
     * @param version SWF version
     * @return Length
     */
    public final int getBytesLength(int version) {
        return getContentBytesLength() + (actionCode >= 0x80 ? 3 : 1);
    }

    protected int getContentBytesLength() {
        return 0;
    }

    /**
     * Updates the action length to the length calculated from action bytes
     *
     * @param version SWF version
     */
    public void updateLength(int version) {
        int length = getBytesLength(version);
        actionLength = length - 1 - (actionCode >= 0x80 ? 2 : 0);
    }

    /**
     * Surrounds byte array with Action header
     *
     * @param data Byte array
     * @param version SWF version
     * @return Byte array
     */
    private byte[] surroundWithAction(byte[] data, int version) {
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        SWFOutputStream sos2 = new SWFOutputStream(baos2, version);
        try {
            sos2.writeUI8(actionCode);
            if (actionCode >= 0x80) {
                sos2.writeUI16(data.length);
            }
            sos2.write(data);
            sos2.close();
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return baos2.toByteArray();
    }

    /**
     * Converts list of Actions to bytes
     *
     * @param list List of actions
     * @param addZero Whether or not to add 0 UI8 value to the end
     * @param version SWF version
     * @return Array of bytes
     */
    public static byte[] actionsToBytes(List<Action> list, boolean addZero, int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Action lastAction = null;
        for (Action a : list) {
            try {
                lastAction = a;
                baos.write(a.getBytes(version));
            } catch (IOException e) {
            }
        }
        if (addZero && (lastAction == null || !(lastAction instanceof ActionEnd))) {
            baos.write(0);
        }
        return baos.toByteArray();
    }

    public static ByteArrayRange actionsToByteArrayRange(List<Action> list, boolean addZero, int version) {
        byte[] bytes = Action.actionsToBytes(list, addZero, version);
        return new ByteArrayRange(bytes);
    }

    public static void setConstantPools(ASMSource src, List<List<String>> constantPools, boolean tryInline) throws ConstantPoolTooBigException {
        try {
            ActionList actions = src.getActions();
            int poolIdx = 0;
            for (Action action : actions) {
                if (action instanceof ActionConstantPool) {
                    ActionConstantPool cPool = (ActionConstantPool) action;
                    List<String> constantPool = constantPools.get(poolIdx);

                    int size = ActionConstantPool.calculateSize(constantPool);
                    if (size > 0xffff && tryInline) {
                        for (int i = 0; i < constantPool.size(); i++) {
                            int refCount = actions.getConstantPoolIndexReferenceCount(i);
                            if (refCount == 1) {
                                actions.inlineConstantPoolString(i, constantPool.get(i));
                                constantPool.set(i, "");
                            }
                        }

                        size = ActionConstantPool.calculateSize(constantPool);
                    }

                    if (size > 0xffff) {
                        throw new ConstantPoolTooBigException(poolIdx, size);
                    }

                    cPool.constantPool = constantPool;

                    poolIdx++;
                    if (constantPools.size() <= poolIdx) {
                        break;
                    }
                }
            }

            actions.removeNonReferencedConstantPoolItems();
            src.setActions(actions);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Set addresses of actions in the list
     *
     * @param list List of actions
     * @param baseAddress Address of first action in the list
     */
    public static void setActionsAddresses(List<Action> list, long baseAddress) {
        long offset = baseAddress;
        for (Action a : list) {
            a.setAddress(offset);
            offset += a.getTotalActionLength();
        }
    }

    /**
     * Converts list of actions to ASM source
     *
     * @param listeners
     * @param address
     * @param list List of actions
     * @param version SWF version
     * @param exportMode PCode or hex?
     * @return source ASM
     *
     */
    public static String actionsToString(List<DisassemblyListener> listeners, long address, ActionList list, int version, ScriptExportMode exportMode) {
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        actionsToString(listeners, address, list, version, exportMode, writer);
        return writer.toString();
    }

    private static void informListeners(List<DisassemblyListener> listeners, int pos, int count) {
        if (pos % INFORM_LISTENER_RESOLUTION == 0) {
            DisassemblyListener[] listenersArray = listeners.toArray(new DisassemblyListener[listeners.size()]);
            for (DisassemblyListener listener : listenersArray) {
                listener.progressToString(pos + 1, count);
            }
        }
    }

    /**
     * Converts list of actions to ASM source
     *
     * @param listeners
     * @param address
     * @param list List of actions
     * @param version SWF version
     * @param exportMode PCode or hex?
     * @param writer
     * @return GraphTextWriter
     */
    public static GraphTextWriter actionsToString(List<DisassemblyListener> listeners, long address, ActionList list, int version, ScriptExportMode exportMode, GraphTextWriter writer) {
        if (exportMode == ScriptExportMode.CONSTANTS) {
            return constantPoolActionsToString(listeners, address, list, version, exportMode, writer);
        }

        long offset;
        Set<Long> importantOffsets = getActionsAllRefs(list);
        /*List<ConstantPool> cps = SWFInputStream.getConstantPool(new ArrayList<DisassemblyListener>(), new ActionGraphSource(list, version, new HashMap<Integer, String>(), new HashMap<String, GraphTargetItem>(), new HashMap<String, GraphTargetItem>()), 0, version, path);
         if (!cps.isEmpty()) {
         setConstantPool(list, cps.get(cps.size() - 1));
         }*/
        HashMap<Long, List<GraphSourceItemContainer>> containers = new HashMap<>();
        HashMap<GraphSourceItemContainer, Integer> containersPos = new HashMap<>();
        offset = address;
        int pos = 0;
        boolean lastPush = false;
        for (Action a : list) {
            informListeners(listeners, pos, list.size());

            if (exportMode == ScriptExportMode.PCODE_HEX) {
                if (lastPush) {
                    writer.newLine();
                    lastPush = false;
                }
                writer.appendNoHilight("; ");
                writer.appendNoHilight(Helper.bytesToHexString(a.getBytes(version)));
                writer.newLine();
            }

            offset = a.getAddress();

            if ((!(a.isIgnored())) && (a instanceof GraphSourceItemContainer)) {
                GraphSourceItemContainer cnt = (GraphSourceItemContainer) a;
                containersPos.put(cnt, 0);
                List<Long> sizes = cnt.getContainerSizes();
                long addr = ((Action) cnt).getAddress() + cnt.getHeaderSize();
                for (Long size : sizes) {
                    addr += size;
                    if (size == 0) {
                        continue;
                    }
                    if (!containers.containsKey(addr)) {
                        containers.put(addr, new ArrayList<>());
                    }
                    containers.get(addr).add(cnt);
                }
            }

            if (containers.containsKey(offset)) {
                for (int i = 0; i < containers.get(offset).size(); i++) {
                    if (lastPush) {
                        writer.newLine();
                        lastPush = false;
                    }
                    writer.appendNoHilight("}").newLine();
                    GraphSourceItemContainer cnt = containers.get(offset).get(i);
                    int cntPos = containersPos.get(cnt);
                    writer.appendNoHilight(cnt.getASMSourceBetween(cntPos));
                    cntPos++;
                    containersPos.put(cnt, cntPos);
                }
            }

            if (Configuration.showAllAddresses.get() || importantOffsets.contains(offset)) {
                if (lastPush) {
                    writer.newLine();
                    lastPush = false;
                }
                writer.appendNoHilight("loc");
                writer.appendNoHilight(Helper.formatAddress(offset));
                writer.appendNoHilight(":");
            }

            if (a.isIgnored()) {
                if (lastPush) {
                    writer.newLine();
                    lastPush = false;
                }
                if (!(a instanceof ActionEnd)) {
                    int len = a.getTotalActionLength();
                    for (int i = 0; i < len; i++) {
                        writer.appendNoHilight("Nop").newLine();
                    }
                }
            } else {
                //if (!(a instanceof ActionNop)) {
                String add = "";
                // honfika: commented out the following lines, because it makes no sense
                /*if (a instanceof ActionIf) {
                 add = " change: " + ((ActionIf) a).getJumpOffset();
                 }
                 if (a instanceof ActionJump) {
                 add = " change: " + ((ActionJump) a).getJumpOffset();
                 }
                 add = "; ofs" + Helper.formatAddress(offset) + add;
                 add = "";*/
                if ((a instanceof ActionPush) && lastPush) {
                    writer.appendNoHilight(" ");
                    ((ActionPush) a).paramsToStringReplaced(list, importantOffsets, exportMode, writer);
                } else {
                    if (lastPush) {
                        writer.newLine();
                        lastPush = false;
                    }

                    writer.append("", offset);

                    int fixBranch = -1;
                    if (a instanceof ActionIf) {
                        ActionIf aif = (ActionIf) a;
                        if (aif.jumpUsed && !aif.ignoreUsed) {
                            fixBranch = 0;
                        }
                        if (!aif.jumpUsed && aif.ignoreUsed) {
                            fixBranch = 1;
                        }
                    }

                    if (fixBranch > -1) {
                        writer.appendNoHilight("FFDec_DeobfuscatePop");
                        if (fixBranch == 0) { //jump
                            writer.newLine();
                            writer.appendNoHilight("Jump loc");
                            writer.appendNoHilight(Helper.formatAddress(a.getAddress() + a.getTotalActionLength() + ((ActionIf) a).getJumpOffset()));
                        } else {
                            //nojump, ignore
                        }
                    } else {
                        a.getASMSourceReplaced(list, importantOffsets, exportMode, writer);
                    }
                    writer.appendNoHilight(a.isIgnored() ? "; ignored" : "");
                    writer.appendNoHilight(add);
                    if (!(a instanceof ActionPush)) {
                        writer.newLine();
                    }
                }
                if (a instanceof ActionPush) {
                    lastPush = true;
                } else {
                    lastPush = false;
                }
                //}
            }

            offset += a.getTotalActionLength();
            pos++;
        }
        if (lastPush) {
            writer.newLine();
        }

        if (containers.containsKey(offset)) {
            for (int i = 0; i < containers.get(offset).size(); i++) {
                writer.appendNoHilight("}");
                writer.newLine();
                GraphSourceItemContainer cnt = containers.get(offset).get(i);
                int cntPos = containersPos.get(cnt);
                writer.appendNoHilight(cnt.getASMSourceBetween(cntPos));
                cntPos++;
                containersPos.put(cnt, cntPos);
            }
        }

        if (importantOffsets.contains(offset)) {
            writer.appendNoHilight("loc");
            writer.appendNoHilight(Helper.formatAddress(offset));
            writer.appendNoHilight(":");
            writer.newLine();
        }

        return writer;
    }

    public static GraphTextWriter constantPoolActionsToString(List<DisassemblyListener> listeners, long address, ActionList list, int version, ScriptExportMode exportMode, GraphTextWriter writer) {
        int poolIdx = 0;
        writer.appendNoHilight(Helper.constants).newLine();
        for (Action a : list) {
            if (a instanceof ActionConstantPool) {
                if (poolIdx > 0) {
                    writer.appendNoHilight("---").newLine();
                }

                ActionConstantPool cPool = (ActionConstantPool) a;
                int constIdx = 0;
                for (String c : cPool.constantPool) {
                    writer.appendNoHilight(constIdx);
                    writer.appendNoHilight("|");
                    writer.appendNoHilight(Helper.escapeString(c));
                    writer.newLine();
                    constIdx++;
                }

                poolIdx++;
            }
        }

        return writer;
    }

    /**
     * Convert action to ASM source
     *
     * @param container
     * @param knownAddreses List of important offsets to mark as labels
     * @param exportMode PCode or hex?
     * @return String of P-code source
     */
    public String getASMSource(ActionList container, Set<Long> knownAddreses, ScriptExportMode exportMode) {
        return toString();
    }

    /**
     * Translates this function to stack and output.
     *
     * @param stack Stack
     * @param output Output
     * @param regNames Register names
     * @param variables Variables
     * @param functions Functions
     * @param staticOperation the value of staticOperation
     * @param path the value of path
     * @throws java.lang.InterruptedException
     */
    public void translate(TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) throws InterruptedException {
    }

    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        return 0;
    }

    @Override
    public int getStackPushCount(BaseLocalData localData, TranslateStack stack) {
        return 0;
    }

    /**
     * Pops long value off the stack
     *
     * @param stack Stack
     * @return long value
     */
    protected long popLong(TranslateStack stack) {
        GraphTargetItem item = stack.pop();
        if (item instanceof DirectValueActionItem) {
            if (((DirectValueActionItem) item).value instanceof Long) {
                return (long) (Long) ((DirectValueActionItem) item).value;
            }
        }
        return 0;
    }

    /**
     * Converts action index to address in the specified list of actions
     *
     * @param actions List of actions
     * @param ip Action index
     * @return address
     */
    public static long ip2adr(List<Action> actions, int ip) {
        /*  List<Action> actions=new ArrayList<Action>();
         for(GraphSourceItem s:sources){
         if(s instanceof Action){
         actions.add((Action)s);
         }
         }*/
        if (ip >= actions.size()) {
            if (actions.isEmpty()) {
                return 0;
            }
            return actions.get(actions.size() - 1).getAddress() + actions.get(actions.size() - 1).getTotalActionLength();
        }
        if (ip == -1) {
            return 0;
        }
        return actions.get(ip).getAddress();
    }

    /**
     * Converts address to action index in the specified list of actions
     *
     * @param actions List of actions
     * @param addr Address
     * @return action index
     */
    public static int adr2ip(List<Action> actions, long addr) {
        for (int ip = 0; ip < actions.size(); ip++) {
            if (actions.get(ip).getAddress() == addr) {
                return ip;
            }
        }
        if (actions.size() > 0) {
            long outpos = actions.get(actions.size() - 1).getAddress() + actions.get(actions.size() - 1).getTotalActionLength();
            if (addr == outpos) {
                return actions.size();
            }
        }
        return -1;
    }

    public static List<GraphTargetItem> actionsToTree(List<Action> actions, int version, int staticOperation, String path) throws InterruptedException {
        return actionsToTree(new HashMap<>(), new HashMap<>(), new HashMap<>(), actions, version, staticOperation, path);
    }

    /**
     * Converts list of actions to ActionScript source code
     *
     * @param asm
     * @param actions List of actions
     * @param path
     * @return source
     * @throws java.lang.InterruptedException
     */
    public static String actionsToSource(final ASMSource asm, final List<Action> actions, final String path) throws InterruptedException {
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        actionsToSource(asm, actions, path, writer);
        return writer.toString();
    }

    /**
     * Converts list of actions to ActionScript source code
     *
     * @param asm
     * @param actions List of actions
     * @param path
     * @param writer
     * @throws java.lang.InterruptedException
     */
    public static void actionsToSource(final ASMSource asm, final List<Action> actions, final String path, GraphTextWriter writer) throws InterruptedException {
        writer.suspendMeasure();
        List<GraphTargetItem> tree = null;
        Throwable convertException = null;
        int timeout = Configuration.decompilationTimeoutSingleMethod.get();
        final int version = asm == null ? SWF.DEFAULT_VERSION : asm.getSwf().version;
        try {
            tree = CancellableWorker.call(new Callable<List<GraphTargetItem>>() {
                @Override
                public List<GraphTargetItem> call() throws Exception {
                    int staticOperation = Graph.SOP_USE_STATIC; //(Boolean) Configuration.getConfig("autoDeobfuscate", true) ? Graph.SOP_SKIP_STATIC : Graph.SOP_USE_STATIC;
                    List<GraphTargetItem> tree = actionsToTree(new HashMap<>(), new HashMap<>(), new HashMap<>(), actions, version, staticOperation, path);
                    Graph.graphToString(tree, new NulWriter(), new LocalData());
                    return tree;
                }
            }, timeout, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            throw ex;
        } catch (Exception | OutOfMemoryError | StackOverflowError ex) {
            if (ex instanceof TimeoutException) {
                logger.log(Level.SEVERE, "Decompilation timeout in: " + path, ex);
            } else {
                logger.log(Level.SEVERE, "Decompilation error in: " + path, ex);
            }
            convertException = ex;
            Throwable cause = ex.getCause();
            if (ex instanceof ExecutionException && cause instanceof Exception) {
                convertException = cause;
            }
        }
        writer.continueMeasure();

        if (asm != null) {
            asm.getActionSourcePrefix(writer);
        }
        if (convertException == null) {
            Graph.graphToString(tree, writer, new LocalData());
        } else if (convertException instanceof TimeoutException) {
            Helper.appendTimeoutCommentAs2(writer, timeout, actions.size());
        } else {
            Helper.appendErrorComment(writer, convertException);
        }
        if (asm != null) {
            asm.getActionSourceSuffix(writer);
        }
    }

    /**
     * Converts list of actions to List of treeItems
     *
     * @param regNames Register names
     * @param variables
     * @param functions
     * @param actions List of actions
     * @param version SWF version
     * @param staticOperation
     * @param path
     * @return List of treeItems
     * @throws java.lang.InterruptedException
     */
    public static List<GraphTargetItem> actionsToTree(HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, List<Action> actions, int version, int staticOperation, String path) throws InterruptedException {
        return ActionGraph.translateViaGraph(regNames, variables, functions, actions, version, staticOperation, path);
    }

    @Override
    public void translate(BaseLocalData localData, TranslateStack stack, List<GraphTargetItem> output, int staticOperation, String path) throws InterruptedException {
        ActionLocalData aLocalData = (ActionLocalData) localData;
        /*int expectedSize = stack.size() - getStackPopCount(localData, stack);
         if (expectedSize < 0) {
         expectedSize = 0;
         }
         expectedSize += getStackPushCount(localData, stack);*/

        translate(stack, output, aLocalData.regNames, aLocalData.variables, aLocalData.functions, staticOperation, path);
        /*if (stack.size() != expectedSize && !(this instanceof ActionPushDuplicate)) {
         throw new Error("HONFIKA stack size mismatch");
         }*/
    }

    @Override
    public boolean isJump() {
        return false;
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public boolean isExit() {
        return false;
    }

    @Override
    public long getOffset() {
        return getAddress();
    }

    @Override
    public List<Integer> getBranches(GraphSource code) {
        return new ArrayList<>();
    }

    @Override
    public boolean isIgnored() {
        return ignored;
    }

    @Override
    public void setIgnored(boolean ignored, int pos) {
        this.ignored = ignored;
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

        @Override
        public String toString() {
            return "[Loop continue:" + loopContinue + ", break:" + loopBreak + "]";
        }
    }

    private static void log(String s) {
        logger.fine(s);
    }

    public static List<GraphTargetItem> actionsPartToTree(HashMap<Integer, String> registerNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, TranslateStack stack, List<Action> actions, int start, int end, int version, int staticOperation, String path) throws InterruptedException {
        if (start < actions.size() && (end > 0) && (start > 0)) {
            log("Entering " + start + "-" + end + (actions.size() > 0 ? (" (" + actions.get(start).toString() + " - " + actions.get(end == actions.size() ? end - 1 : end) + ")") : ""));
        }
        ActionLocalData localData = new ActionLocalData(registerNames, variables, functions);
        List<GraphTargetItem> output = new ArrayList<>();
        int ip = start;
        boolean isWhile = false;
        boolean isForIn = false;
        GraphTargetItem inItem = null;
        int loopStart = 0;
        loopip:
        while (ip <= end) {

            long addr = ip2adr(actions, ip);
            if (ip > end) {
                break;
            }
            if (ip >= actions.size()) {
                output.add(new ScriptEndItem());
                break;
            }
            Action action = actions.get(ip);
            if (action.isIgnored()) {
                ip++;
                continue;
            }
            if (action instanceof GraphSourceItemContainer) {
                GraphSourceItemContainer cnt = (GraphSourceItemContainer) action;
                //List<GraphTargetItem> out=actionsPartToTree(new HashMap<Integer, String>(), new HashMap<String, GraphTargetItem>(),new HashMap<String, GraphTargetItem>(), new TranslateStack(), src, ip+1,endip-1 , version);
                long endAddr = action.getAddress() + cnt.getHeaderSize();
                String cntName = cnt.getName();
                List<List<GraphTargetItem>> outs = new ArrayList<>();
                HashMap<String, GraphTargetItem> variables2 = Helper.deepCopy(variables);
                if (cnt instanceof ActionDefineFunction || cnt instanceof ActionDefineFunction2) {
                    for (int r = 0; r < 256; r++) {
                        if (variables2.containsKey("__register" + r)) {
                            variables2.remove("__register" + r);
                        }
                    }
                }
                for (long size : cnt.getContainerSizes()) {
                    if (size == 0) {
                        outs.add(new ArrayList<>());
                        continue;
                    }
                    List<GraphTargetItem> out;
                    try {
                        out = ActionGraph.translateViaGraph(cnt.getRegNames(), variables2, functions, actions.subList(adr2ip(actions, endAddr), adr2ip(actions, endAddr + size)), version, staticOperation, path + (cntName == null ? "" : "/" + cntName));
                    } catch (OutOfMemoryError | TranslateException | StackOverflowError ex2) {
                        logger.log(Level.SEVERE, "Decompilation error in: " + path, ex2);
                        if (ex2 instanceof OutOfMemoryError) {
                            Helper.freeMem();
                        }
                        out = new ArrayList<>();
                        out.add(new CommentItem(new String[]{
                            "",
                            " * " + AppResources.translate("decompilationError"),
                            " * " + AppResources.translate("decompilationError.obfuscated"),
                            Helper.decompilationErrorAdd == null ? null : " * " + Helper.decompilationErrorAdd,
                            " * " + AppResources.translate("decompilationError.errorType") + ": "
                            + ex2.getClass().getSimpleName(),
                            ""}));
                    }
                    outs.add(out);
                    endAddr += size;
                }
                ((GraphSourceItemContainer) action).translateContainer(outs, stack, output, registerNames, variables, functions);
                ip = adr2ip(actions, endAddr);
                continue;
            }

            //return in for..in
            if ((action instanceof ActionPush) && (((ActionPush) action).values.size() == 1) && (((ActionPush) action).values.get(0) instanceof Null)) {
                if (ip + 3 <= end) {
                    if ((actions.get(ip + 1) instanceof ActionEquals) || (actions.get(ip + 1) instanceof ActionEquals2)) {
                        if (actions.get(ip + 2) instanceof ActionNot) {
                            if (actions.get(ip + 3) instanceof ActionIf) {
                                ActionIf aif = (ActionIf) actions.get(ip + 3);
                                if (adr2ip(actions, ip2adr(actions, ip + 4) + aif.getJumpOffset()) == ip) {
                                    ip += 4;
                                    continue;
                                }
                            }
                        }
                    }
                }
            }

            /*ActionJump && ActionIf removed*/
            /*if ((action instanceof ActionEnumerate2) || (action instanceof ActionEnumerate)) {
             loopStart = ip + 1;
             isForIn = true;
             ip += 4;
             action.translate(localData, stack, output);
             EnumerateActionItem en = (EnumerateActionItem) stack.peek();
             inItem = en.object;
             continue;
             } else*/ /*if (action instanceof ActionTry) {
             ActionTry atry = (ActionTry) action;
             List<GraphTargetItem> tryCommands = ActionGraph.translateViaGraph(registerNames, variables, functions, atry.tryBody, version);
             ActionItem catchName;
             if (atry.catchInRegisterFlag) {
             catchName = new DirectValueActionItem(atry, -1, new RegisterNumber(atry.catchRegister), new ArrayList<>());
             } else {
             catchName = new DirectValueActionItem(atry, -1, atry.catchName, new ArrayList<>());
             }
             List<GraphTargetItem> catchExceptions = new ArrayList<GraphTargetItem>();
             catchExceptions.add(catchName);
             List<List<GraphTargetItem>> catchCommands = new ArrayList<List<GraphTargetItem>>();
             catchCommands.add(ActionGraph.translateViaGraph(registerNames, variables, functions, atry.catchBody, version));
             List<GraphTargetItem> finallyCommands = ActionGraph.translateViaGraph(registerNames, variables, functions, atry.finallyBody, version);
             output.add(new TryActionItem(tryCommands, catchExceptions, catchCommands, finallyCommands));
             } else  if (action instanceof ActionWith) {
             ActionWith awith = (ActionWith) action;
             List<GraphTargetItem> withCommands = ActionGraph.translateViaGraph(registerNames, variables, functions,new ArrayList<Action>() , version); //TODO:parse with actions
             output.add(new WithActionItem(action, stack.pop(), withCommands));
             } else */ if (false) {
            } /*if (action instanceof ActionStoreRegister) {
             if ((ip + 1 <= end) && (actions.get(ip + 1) instanceof ActionPop)) {
             action.translate(localData, stack, output);
             stack.pop();
             ip++;
             } else {
             try {
             action.translate(localData, stack, output);
             } catch (Exception ex) {
             // ignore
             }
             }
             } */ /*else if (action instanceof ActionStrictEquals) {
             if ((ip + 1 < actions.size()) && (actions.get(ip + 1) instanceof ActionIf)) {
             List<ActionItem> caseValues = new ArrayList<ActionItem>();
             List<List<ActionItem>> caseCommands = new ArrayList<List<ActionItem>>();
             caseValues.add(stack.pop());
             ActionItem switchedObject = stack.pop();
             if (output.size() > 0) {
             if (output.get(output.size() - 1) instanceof StoreRegisterActionItem) {
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
             caseValues.add(actionsToStackTree(registerNames, jumpsOrIfs, actions, constants, caseStart, ip - 2, version).pop());
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
             caseCommands.add(actionsToTree(registerNames, unknownJumps, loopList, jumpsOrIfs, stack, constants, actions, caseBodyIps.get(i), caseEnd, version));
             }
             output.add(new SwitchActionItem(action, defaultAddr, switchedObject, caseValues, caseCommands, null));
             continue;
             } else {
             action.translate(stack, constants, output, registerNames);
             }
             } */ else {

                if (action instanceof ActionStore) {
                    ActionStore store = (ActionStore) action;
                    store.setStore(actions.subList(ip + 1, ip + 1 + store.getStoreSize()));
                    ip = ip + 1 + store.getStoreSize() - 1/*ip++ will be next*/;
                }

                try {
                    action.translate(localData, stack, output, staticOperation, path);
                } catch (EmptyStackException ese) {
                    logger.log(Level.SEVERE, "Decompilation error in: " + path, ese);
                    output.add(new UnsupportedActionItem(action, "Empty stack"));
                }

            }

            ip++;
        }
        //output = checkClass(output);
        log("Leaving " + start + "-" + end);
        return output;
    }

    public static GraphTargetItem getWithoutGlobal(GraphTargetItem ti) {
        GraphTargetItem t = ti;
        if (!(t instanceof GetMemberActionItem)) {
            return ti;
        }
        GetMemberActionItem lastMember = null;
        while (((GetMemberActionItem) t).object instanceof GetMemberActionItem) {
            lastMember = (GetMemberActionItem) t;
            t = ((GetMemberActionItem) t).object;
        }
        if (((GetMemberActionItem) t).object instanceof GetVariableActionItem) {
            GetVariableActionItem v = (GetVariableActionItem) ((GetMemberActionItem) t).object;
            if (v.name instanceof DirectValueActionItem) {
                if (((DirectValueActionItem) v.name).value instanceof String) {
                    if (((DirectValueActionItem) v.name).value.equals("_global")) {
                        GetVariableActionItem gvt = new GetVariableActionItem(null, ((GetMemberActionItem) t).memberName);
                        if (lastMember == null) {
                            return gvt;
                        } else {
                            lastMember.object = gvt;
                        }
                    }
                }
            }
        }
        return ti;
    }

    public static List<GraphTargetItem> checkClass(List<GraphTargetItem> output) {
        if (true) {
            //return output;
        }
        List<GraphTargetItem> ret = new ArrayList<>();
        List<GraphTargetItem> functions = new ArrayList<>();
        List<GraphTargetItem> staticFunctions = new ArrayList<>();
        List<MyEntry<GraphTargetItem, GraphTargetItem>> vars = new ArrayList<>();
        List<MyEntry<GraphTargetItem, GraphTargetItem>> staticVars = new ArrayList<>();
        GraphTargetItem className;
        GraphTargetItem extendsOp = null;
        List<GraphTargetItem> implementsOp = new ArrayList<>();
        boolean ok = true;
        int prevCount = 0;
        for (GraphTargetItem t : output) {
            if (t instanceof IfItem) {
                IfItem it = (IfItem) t;
                if (it.expression instanceof NotItem) {
                    NotItem nti = (NotItem) it.expression;
                    if ((nti.value instanceof GetMemberActionItem) || (nti.value instanceof GetVariableActionItem)) {
                        if (true) { //it.onFalse.isEmpty()){ //||(it.onFalse.get(0) instanceof UnsupportedActionItem)) {
                            if ((it.onTrue.size() == 1) && (it.onTrue.get(0) instanceof SetMemberActionItem) && (((SetMemberActionItem) it.onTrue.get(0)).value instanceof NewObjectActionItem)) {
                                // ignore
                            } else {
                                List<GraphTargetItem> parts = it.onTrue;
                                className = getWithoutGlobal(nti.value);
                                if (parts.size() >= 1) {
                                    int ipos = 0;

                                    while ((parts.get(ipos) instanceof PopItem) || ((parts.get(ipos) instanceof IfItem) && ((((IfItem) parts.get(ipos)).onTrue.size() == 1) && (((IfItem) parts.get(ipos)).onTrue.get(0) instanceof SetMemberActionItem) && (((SetMemberActionItem) ((IfItem) parts.get(ipos)).onTrue.get(0)).value instanceof NewObjectActionItem)))) {
                                        ipos++;
                                    }
                                    if (parts.get(ipos) instanceof ExtendsActionItem) {
                                        ExtendsActionItem et = (ExtendsActionItem) parts.get(ipos);
                                        extendsOp = getWithoutGlobal(et.superclass);
                                        ipos++;
                                    }
                                    if (parts.get(ipos) instanceof StoreRegisterActionItem) {
                                        StoreRegisterActionItem sr = (StoreRegisterActionItem) parts.get(ipos);
                                        int instanceReg = sr.register.number;
                                        if (sr.value instanceof GetMemberActionItem) {
                                            GetMemberActionItem gm = (GetMemberActionItem) sr.value;
                                            //gm.memberName should be "prototype"
                                            if (gm.object instanceof TemporaryRegister) {
                                                TemporaryRegister tm = (TemporaryRegister) gm.object;
                                                int classReg = tm.getRegId();
                                                if (tm.value instanceof SetMemberActionItem) {
                                                    SetMemberActionItem sm = (SetMemberActionItem) tm.value;
                                                    if (sm.value instanceof StoreRegisterActionItem) {
                                                        sr = (StoreRegisterActionItem) sm.value;
                                                        if (sr.value instanceof FunctionActionItem) {
                                                            ((FunctionActionItem) (sr.value)).calculatedFunctionName = (className instanceof GetMemberActionItem) ? ((GetMemberActionItem) className).memberName : className;
                                                            functions.add((FunctionActionItem) sr.value);

                                                            for (; ipos < parts.size(); ipos++) {
                                                                if (parts.get(ipos) instanceof ImplementsOpActionItem) {
                                                                    ImplementsOpActionItem io = (ImplementsOpActionItem) parts.get(ipos);
                                                                    implementsOp = io.superclasses;
                                                                    continue;
                                                                }
                                                                if (parts.get(ipos) instanceof SetMemberActionItem) {
                                                                    sm = (SetMemberActionItem) parts.get(ipos);
                                                                    int rnum = -1;
                                                                    if (sm.object instanceof DirectValueActionItem) {
                                                                        DirectValueActionItem dv = (DirectValueActionItem) sm.object;
                                                                        if (dv.value instanceof RegisterNumber) {
                                                                            RegisterNumber rn = (RegisterNumber) dv.value;
                                                                            rnum = rn.number;
                                                                        }
                                                                    }
                                                                    if (sm.object instanceof TemporaryRegister) {
                                                                        rnum = ((TemporaryRegister) sm.object).getRegId();
                                                                    }
                                                                    if (rnum == instanceReg) {
                                                                        if (sm.value instanceof FunctionActionItem) {
                                                                            ((FunctionActionItem) sm.value).calculatedFunctionName = sm.objectName;
                                                                            functions.add((FunctionActionItem) sm.value);
                                                                        } else {
                                                                            vars.add(new MyEntry<>(sm.objectName, sm.value));
                                                                        }
                                                                    } else if (rnum == classReg) {
                                                                        if (sm.value instanceof FunctionActionItem) {
                                                                            ((FunctionActionItem) sm.value).calculatedFunctionName = sm.objectName;
                                                                            staticFunctions.add((FunctionActionItem) sm.value);
                                                                        } else {
                                                                            staticVars.add(new MyEntry<>(sm.objectName, sm.value));
                                                                        }
                                                                    }

                                                                }
                                                            }

                                                        }

                                                    }
                                                }
                                                List<GraphTargetItem> output2 = new ArrayList<>();
                                                for (int i = 0; i < prevCount; i++) {
                                                    output2.add(output.get(i));
                                                }
                                                output2.add(new ClassActionItem(className, extendsOp, implementsOp, null/*FIXME*/, functions, vars, staticFunctions, staticVars));
                                                return output2;
                                            }
                                        }
                                    } else if (parts.get(ipos) instanceof SetMemberActionItem) {
                                        SetMemberActionItem sm = (SetMemberActionItem) parts.get(0);
                                        if (sm.value instanceof FunctionActionItem) {
                                            FunctionActionItem f = (FunctionActionItem) sm.value;
                                            if (f.actions.isEmpty()) {
                                                if (parts.size() == 2) {
                                                    if (parts.get(1) instanceof ImplementsOpActionItem) {
                                                        ImplementsOpActionItem iot = (ImplementsOpActionItem) parts.get(1);
                                                        implementsOp = iot.superclasses;
                                                    } else {
                                                        ok = false;
                                                        break;
                                                    }
                                                }
                                                List<GraphTargetItem> output2 = new ArrayList<>();
                                                for (int i = 0; i < prevCount; i++) {
                                                    output2.add(output.get(i));
                                                }
                                                output2.add(new InterfaceActionItem(sm.objectName, implementsOp));
                                                return output2;
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            ok = false;
                        }
                    } else {
                        ok = false;
                    }
                } else {
                    ok = false;
                }
            } else if (!(t instanceof PopItem)) {
                prevCount++;
                //ok = false;
            }
            if (!ok) {
                break;
            }
        }
        return output;
    }

    @Override
    public boolean ignoredLoops() {
        return false;
    }

    public static void setConstantPool(List<? extends GraphSourceItem> actions, ConstantPool cpool) {
        for (GraphSourceItem a : actions) {
            if (a instanceof ActionPush) {
                if (cpool != null) {
                    ((ActionPush) a).constantPool = cpool.constants;
                }
            }
            if (a instanceof ActionDefineFunction) {
                if (cpool != null) {
                    //((ActionDefineFunction) a).setConstantPool(cpool.constants,actions);
                }
            }
            if (a instanceof ActionDefineFunction2) {
                if (cpool != null) {
                    //((ActionDefineFunction2) a).setConstantPool(cpool.constants,actions);
                }
            }
        }
    }

    public GraphTextWriter getASMSourceReplaced(ActionList container, Set<Long> knownAddreses, ScriptExportMode exportMode, GraphTextWriter writer) {
        writer.appendNoHilight(getASMSource(container, knownAddreses, exportMode));
        return writer;
    }

    public static double toFloatPoint(Object o) {
        if (o instanceof Double) {
            return (Double) o;
        }
        if (o instanceof Integer) {
            return (Integer) o;
        }
        if (o instanceof Long) {
            return (Long) o;
        }
        if (o instanceof String) {
            try {
                return Double.parseDouble((String) o);
            } catch (NumberFormatException nfe) {
                return 0;
            }
        }
        return 0;
    }

    public static GraphTargetItem gettoset(GraphTargetItem get, GraphTargetItem value, List<VariableActionItem> variables) {
        GraphTargetItem ret = get;
        boolean boxed = false;
        if (get instanceof VariableActionItem) {
            boxed = true;
            ret = ((VariableActionItem) ret).getBoxedValue();
        }
        if (ret instanceof GetVariableActionItem) {
            GetVariableActionItem gv = (GetVariableActionItem) ret;
            ret = new SetVariableActionItem(null, gv.name, value);
        } else if (ret instanceof GetMemberActionItem) {
            GetMemberActionItem mem = (GetMemberActionItem) ret;
            ret = new SetMemberActionItem(null, mem.object, mem.memberName, value);
        } else if ((ret instanceof DirectValueActionItem) && ((DirectValueActionItem) ret).value instanceof RegisterNumber) {
            ret = new StoreRegisterActionItem(null, (RegisterNumber) ((DirectValueActionItem) ret).value, value, false);
        } else if (ret instanceof GetPropertyActionItem) {
            GetPropertyActionItem gp = (GetPropertyActionItem) ret;
            ret = new SetPropertyActionItem(null, gp.target, gp.propertyIndex, value);
        }
        if (boxed) {
            GraphTargetItem b = ret;
            ret = new VariableActionItem(((VariableActionItem) get).getVariableName(), value, ((VariableActionItem) get).isDefinition());
            ((VariableActionItem) ret).setBoxedValue((ActionItem) b);
            variables.remove((VariableActionItem) get);
            variables.add((VariableActionItem) ret);
        }
        return ret;
    }

    @Override
    public boolean isDeobfuscatePop() {
        return false;
    }

    @Override
    public int getLine() {
        return 0;
    }

    @Override
    public String getFile() {
        return null;
    }
}
