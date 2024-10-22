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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.AppResources;
import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.DisassemblyListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.as2.Trait;
import com.jpexs.decompiler.flash.action.deobfuscation.ActionDeobfuscator;
import com.jpexs.decompiler.flash.action.model.ActionItem;
import com.jpexs.decompiler.flash.action.model.ConstantPool;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.FunctionActionItem;
import com.jpexs.decompiler.flash.action.model.GetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.GetPropertyActionItem;
import com.jpexs.decompiler.flash.action.model.GetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.SetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.SetPropertyActionItem;
import com.jpexs.decompiler.flash.action.model.SetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.StoreRegisterActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParsedSymbol;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.action.parser.script.VariableActionItem;
import com.jpexs.decompiler.flash.action.special.ActionEnd;
import com.jpexs.decompiler.flash.action.special.ActionStore;
import com.jpexs.decompiler.flash.action.swf3.ActionSetTarget;
import com.jpexs.decompiler.flash.action.swf4.ActionEquals;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionNot;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionSetTarget2;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionConstantPool;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionEquals2;
import com.jpexs.decompiler.flash.action.swf5.ActionWith;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import com.jpexs.decompiler.flash.action.swf7.ActionTry;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerPlugin;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphPartChangeException;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SecondPassData;
import com.jpexs.decompiler.graph.SecondPassException;
import com.jpexs.decompiler.graph.TranslateException;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.CommentItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.ScriptEndItem;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Reference;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents one ACTIONRECORD, also has some static method to work with
 * Actions.
 *
 * @author JPEXS
 */
public abstract class Action implements GraphSourceItem {

    /**
     * When to inform listeners about progress
     */
    private static final int INFORM_LISTENER_RESOLUTION = 1000;

    /**
     * Whether this action is ignored
     */
    private boolean ignored = false;

    /**
     * File offset of this action
     */
    public long fileOffset = -1;

    /**
     * Action type identifier
     */
    private int actionCode;

    /**
     * Length of action data
     */
    protected int actionLength;

    /**
     * Address of this action
     */
    private long address;

    /**
     * Virtual address (address before deobfuscation)
     */
    private long virtualAddress = -1;

    /**
     * Charset - SWFs version 5 and lower do not use UTF-8
     */
    private String charset;

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

    /**
     * Property names list
     */
    public static final List<String> propertyNamesList = Arrays.asList(propertyNames);

    /**
     * Property names list in lower case
     */
    public static final List<String> propertyNamesListLowerCase = new ArrayList<>();

    static {
        for (String s : propertyNamesList) {
            propertyNamesListLowerCase.add(s.toLowerCase());
        }
    }

    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger(Action.class.getName());

    /**
     * Gets the line offset.
     *
     * @return Line offset
     */
    @Override
    public long getLineOffset() {
        return fileOffset;
    }

    /**
     * Gets charset
     *
     * @return Charset
     */
    public String getCharset() {
        return charset;
    }

    /**
     * Constructs new Action
     *
     * @param actionCode Action type identifier
     * @param actionLength Length of action data
     * @param charset Charset
     */
    public Action(int actionCode, int actionLength, String charset) {
        this.actionCode = actionCode;
        this.actionLength = actionLength;
        this.charset = charset;
    }

    /**
     * Constructs new Action
     */
    public Action() {
    }

    /**
     * Gets the address.
     *
     * @return Address
     */
    @Override
    public long getAddress() {
        return address;
    }

    /**
     * Gets code of this action.
     *
     * @return Code of this action
     */
    public int getActionCode() {
        return actionCode;
    }

    /**
     * Gets all addresses which are referenced from this action and/or
     * subactions.
     *
     * @param refs List of addresses
     */
    public void getRef(Set<Long> refs) {
    }

    /**
     * Gets all addresses which are referenced from the list of actions.
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

    /**
     * Gets total length of this action.
     *
     * @return Total length of this action
     */
    public int getTotalActionLength() {
        return actionLength + 1 + (actionCode >= 0x80 ? 2 : 0);
    }

    /**
     * Sets address of this instruction.
     *
     * @param address Address
     */
    public void setAddress(long address) {
        this.address = address;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "Action" + actionCode;
    }

    /**
     * Reads String from FlasmLexer.
     *
     * @param lex FlasmLexer
     * @return String value
     * @throws IOException On I/O error
     * @throws ActionParseException When read object is not String
     */
    protected String lexString(FlasmLexer lex) throws IOException, ActionParseException {
        ASMParsedSymbol symb = lex.lex();
        if (symb.type != ASMParsedSymbol.TYPE_STRING) {
            throw new ActionParseException("String expected", lex.yyline());
        }
        return (String) symb.value;
    }

    /**
     * Reads Block startServer from FlasmLexer.
     *
     * @param lex FlasmLexer
     * @throws IOException On I/O error
     * @throws ActionParseException When read object is not Block startServer
     */
    protected void lexBlockOpen(FlasmLexer lex) throws IOException, ActionParseException {
        ASMParsedSymbol symb = lex.lex();
        if (symb.type != ASMParsedSymbol.TYPE_BLOCK_START) {
            throw new ActionParseException("Block startServer ", lex.yyline());
        }
    }

    /**
     * Reads Identifier from FlasmLexer.
     *
     * @param lex FlasmLexer
     * @return Identifier name
     * @throws IOException On I/O error
     * @throws ActionParseException When read object is not Identifier
     */
    protected String lexIdentifier(FlasmLexer lex) throws IOException, ActionParseException {
        ASMParsedSymbol symb = lex.lex();
        if (symb.type != ASMParsedSymbol.TYPE_IDENTIFIER) {
            throw new ActionParseException("Identifier expected", lex.yyline());
        }
        return (String) symb.value;
    }

    /**
     * Reads long value from FlasmLexer.
     *
     * @param lex FlasmLexer
     * @return long value
     * @throws IOException On I/O error
     * @throws ActionParseException When read object is not long value
     */
    protected long lexLong(FlasmLexer lex) throws IOException, ActionParseException {
        ASMParsedSymbol symb = lex.lex();
        if (symb.type != ASMParsedSymbol.TYPE_INTEGER) {
            throw new ActionParseException("Integer expected", lex.yyline());
        }
        return (Long) symb.value;
    }

    /**
     * Reads boolean value from FlasmLexer.
     *
     * @param lex FlasmLexer
     * @return boolean value
     * @throws IOException On I/O error
     * @throws ActionParseException When read object is not boolean value
     */
    protected boolean lexBoolean(FlasmLexer lex) throws IOException, ActionParseException {
        ASMParsedSymbol symb = lex.lex();
        if (symb.type != ASMParsedSymbol.TYPE_BOOLEAN) {
            throw new ActionParseException("Boolean expected", lex.yyline());
        }
        return (Boolean) symb.value;
    }

    /**
     * Reads optional comma from FlasmLexer.
     *
     * @param lex FlasmLexer
     * @throws IOException On I/O error
     * @throws ActionParseException On parse error
     */
    protected void lexOptionalComma(FlasmLexer lex) throws IOException, ActionParseException {
        ASMParsedSymbol symb = lex.lex();
        if (symb.type != ASMParsedSymbol.TYPE_COMMA) {
            lex.pushback(symb);
        }
    }

    /**
     * Gets action converted to bytes.
     *
     * @param version SWF version
     * @return Array of bytes
     */
    public final byte[] getBytes(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version, charset);
        try {
            getContentBytes(sos);
            sos.close();
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return surroundWithAction(baos.toByteArray(), version);
    }

    /**
     * Gets content bytes of action.
     *
     * @param sos SWFOutputStream
     * @throws IOException On I/O error
     */
    protected void getContentBytes(SWFOutputStream sos) throws IOException {
    }

    /**
     * Gets the length of action converted to bytes.
     *
     * @return Length
     */
    @Override
    public final int getBytesLength() {
        return getContentBytesLength() + (actionCode >= 0x80 ? 3 : 1);
    }

    /**
     * Gets the length of content bytes of action.
     *
     * @return Length
     */
    protected int getContentBytesLength() {
        return 0;
    }

    /**
     * Updates the action length to the length calculated from action bytes.
     */
    public void updateLength() {
        int length = getBytesLength();
        actionLength = length - 1 - (actionCode >= 0x80 ? 2 : 0);
    }

    /**
     * Surrounds byte array with Action header.
     *
     * @param data Byte array
     * @param version SWF version
     * @return Byte array
     */
    private byte[] surroundWithAction(byte[] data, int version) {
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        SWFOutputStream sos2 = new SWFOutputStream(baos2, version, charset);
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
     * Gets file offset.
     *
     * @return File offset
     */
    @Override
    public long getFileOffset() {
        return fileOffset;
    }

    /**
     * Converts list of Actions to bytes.
     *
     * @param list List of actions
     * @param addZero Whether to add 0 UI8 value to the end
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
                //ignore
            }
        }
        if (addZero && (lastAction == null || !(lastAction instanceof ActionEnd))) {
            baos.write(0);
        }
        return baos.toByteArray();
    }

    /**
     * Converts list of Actions to bytes.
     *
     * @param list List of actions
     * @param addZero Whether to add 0 UI8 value to the end
     * @param version SWF version
     * @return ByteArrayRange
     */
    public static ByteArrayRange actionsToByteArrayRange(List<Action> list, boolean addZero, int version) {
        byte[] bytes = Action.actionsToBytes(list, addZero, version);
        return new ByteArrayRange(bytes);
    }

    /**
     * Sets constant pool of actions.
     *
     * @param src ASMSource
     * @param constantPools List of constant pools
     * @param tryInline Whether to try to inline constant pools
     * @throws ConstantPoolTooBigException When constant pool is too big
     */
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
     * Sets addresses of actions in the list.
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

    private static void informListeners(List<DisassemblyListener> listeners, int pos, int count) {
        DisassemblyListener[] listenersArray = listeners.toArray(new DisassemblyListener[listeners.size()]);
        for (DisassemblyListener listener : listenersArray) {
            listener.progressToString(pos + 1, count);
        }
    }

    /**
     * Converts list of actions to ASM source.
     *
     * @param listeners List of listeners
     * @param address Address
     * @param list List of actions
     * @param version SWF version
     * @param exportMode PCode or hex?
     * @param writer Writer
     * @return Writer
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
        boolean lastPush = false;
        byte[] fileData = list.fileData;
        for (int pos = 0; pos < list.size(); pos++) {
            Action a = list.get(pos);

            if ((pos + 1) % INFORM_LISTENER_RESOLUTION == 0) {
                informListeners(listeners, pos, list.size());
            }

            if (exportMode == ScriptExportMode.PCODE_HEX) {
                if (lastPush) {
                    writer.newLine();
                    lastPush = false;
                }
                writer.appendNoHilight("; ");
                long fileOffset = a.getFileOffset();
                if (Configuration.showFileOffsetInPcodeHex.get()) {
                    writer.appendNoHilight("@");
                    writer.appendNoHilight(Helper.formatHex(fileOffset, 8));
                    writer.appendNoHilight(" ");
                }

                byte[] bytes = a.getBytes(version);
                writer.appendNoHilight(Helper.bytesToHexString(bytes));

                if (Configuration.showOriginalBytesInPcodeHex.get()) {
                    if (fileData != null && fileOffset != -1 && fileData.length > fileOffset + bytes.length - 1) {
                        boolean same = true;
                        for (int i = 0; i < bytes.length; i++) {
                            byte b = fileData[(int) (fileOffset + i)];
                            if (b != bytes[i]) {
                                same = false;
                                break;
                            }
                        }

                        if (!same) {
                            writer.appendNoHilight(" (");
                            for (int i = 0; i < bytes.length; i++) {
                                if (i != 0) {
                                    writer.appendNoHilight(" ");
                                }

                                writer.appendNoHilight(Helper.byteToHex(fileData[(int) (fileOffset + i)]));
                            }

                            writer.appendNoHilight(")");
                        }

                    }
                }

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
                    writer.appendNoHilight(", ");
                    ((ActionPush) a).paramsToStringReplaced(list, importantOffsets, exportMode, writer);
                } else {
                    if (lastPush) {
                        writer.newLine();
                        //lastPush = false;
                    }

                    writer.append("", offset, a.getFileOffset());

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
                            writer.appendNoHilight(Helper.formatAddress(((ActionIf) a).getTargetAddress()));
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
                lastPush = a instanceof ActionPush;
                //}
            }

            offset += a.getTotalActionLength();
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

    /**
     * Converts constant pool actions to String.
     *
     * @param listeners List of listeners
     * @param address Address
     * @param list List of actions
     * @param version SWF version
     * @param exportMode Export mode
     * @param writer GraphTextWriter
     * @return GraphTextWriter
     */
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
     * Converts action to ASM source.
     *
     * @param container Container
     * @param knownAddresses List of important offsets to mark as labels
     * @param exportMode PCode or hex?
     * @return String of P-code source
     */
    public String getASMSource(ActionList container, Set<Long> knownAddresses, ScriptExportMode exportMode) {
        return toString();
    }

    /**
     * Executes the action.
     *
     * @param lda Local data area
     * @return Whether the action was executed successfully
     */
    public abstract boolean execute(LocalDataArea lda);

    /**
     * Gets the number of stack items that are popped by this item.
     *
     * @param localData Local data
     * @param stack Stack
     * @return Number of stack items that are popped by this item
     */
    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        return 0;
    }

    /**
     * Gets the number of stack items that are pushed by this item.
     *
     * @param localData Local data
     * @param stack Stack
     * @return Number of stack items that are pushed by this item
     */
    @Override
    public int getStackPushCount(BaseLocalData localData, TranslateStack stack) {
        return 0;
    }

    /**
     * Pops long value off the stack.
     *
     * @param stack Stack
     * @return long value
     */
    protected long popLong(TranslateStack stack) {
        GraphTargetItem item = stack.pop();
        if (item instanceof DirectValueActionItem) {
            return (long) (double) EcmaScript.toNumberAs2(((DirectValueActionItem) item).value);
        }

        return 0;
    }

    /**
     * Converts action index to address in the specified list of actions.
     *
     * @param actions List of actions
     * @param ip Action index
     * @return address
     */
    public static long ip2adr(List<Action> actions, int ip) {
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
     * Converts address to action index in the specified list of actions.
     *
     * @param actions List of actions
     * @param addr Address
     * @return Action index
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

    /**
     * Converts actions to ActionScript source code - decompiles.
     *
     * @param uninitializedClassTraits Uninitialized class traits
     * @param asm ASM source
     * @param actions List of actions
     * @param path Path
     * @param writer Writer
     * @param charset Charset
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public static GraphTextWriter actionsToSource(Map<String, Map<String, Trait>> uninitializedClassTraits, final ASMSource asm, final List<Action> actions, final String path, GraphTextWriter writer, String charset) throws InterruptedException {
        return Action.actionsToSource(uninitializedClassTraits, asm, actions, path, writer, charset, new ArrayList<>());
    }

    /**
     * Converts list of actions to ActionScript source code.
     *
     * @param uninitializedClassTraits Uninitialized class traits
     * @param asm ASM source
     * @param actions List of actions
     * @param path Path
     * @param writer Writer
     * @param charset Charset
     * @param treeOperations List of tree operations
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public static GraphTextWriter actionsToSource(Map<String, Map<String, Trait>> uninitializedClassTraits, final ASMSource asm, final List<Action> actions, final String path, GraphTextWriter writer, String charset, List<ActionTreeOperation> treeOperations) throws InterruptedException {
        writer.suspendMeasure();
        List<GraphTargetItem> tree = null;
        Throwable convertException = null;
        int timeout = Configuration.decompilationTimeoutSingleMethod.get();
        final SWF swf = asm == null ? null : asm.getSwf();
        final int version = swf == null ? SWF.DEFAULT_VERSION : swf.version;
        try {
            tree = CancellableWorker.call("script.actionsToSource", new Callable<List<GraphTargetItem>>() {
                @Override
                public List<GraphTargetItem> call() throws Exception {
                    int staticOperation = 0;
                    boolean insideDoInitAction = (asm instanceof DoInitActionTag);
                    List<GraphTargetItem>
                            tree = actionsToTree(uninitializedClassTraits, insideDoInitAction, false, new HashMap<>(), new HashMap<>(), new HashMap<>(), actions, version, staticOperation, path, charset);
                    SWFDecompilerPlugin.fireActionTreeCreated(tree, swf);
                    for (ActionTreeOperation treeOperation : treeOperations) {
                        treeOperation.run(tree);
                    }
                    if (Configuration.autoDeobfuscate.get()) {
                        new ActionDeobfuscator().actionTreeCreated(tree, swf);
                    }

                    Graph.graphToString(tree, new NulWriter(), new LocalData());
                    return tree;
                }
            }, timeout, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            throw ex;
        } catch (Exception | OutOfMemoryError | StackOverflowError ex) {

            ex.printStackTrace();
            convertException = ex;
            Throwable cause = ex.getCause();
            if (ex instanceof ExecutionException && cause instanceof Exception) {
                convertException = cause;
            }
            if (convertException instanceof TimeoutException) {
                logger.log(Level.SEVERE, "Decompilation timeout in: " + path, convertException);
            } else {
                logger.log(Level.SEVERE, "Decompilation error in: " + path, convertException);
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

        return writer;
    }

    /**
     * Converts list of actions to List of treeItems
     *
     * @param uninitializedClassTraits Uninitialized class traits
     * @param insideDoInitAction Inside DoInitAction?
     * @param insideFunction Inside function?
     * @param actions List of actions
     * @param version SWF version
     * @param staticOperation Unused
     * @param path Path
     * @param charset Charset
     * @return List of treeItems
     * @throws InterruptedException On interrupt
     */
    public static List<GraphTargetItem> actionsToTree(Map<String, Map<String, Trait>> uninitializedClassTraits, boolean insideDoInitAction, boolean insideFunction, List<Action> actions, int version, int staticOperation, String path, String charset) throws InterruptedException {
        return actionsToTree(uninitializedClassTraits, insideDoInitAction, insideFunction, new HashMap<>(), new HashMap<>(), new HashMap<>(), actions, version, staticOperation, path, charset);
    }

    /**
     * Converts list of actions to List of treeItems.
     * @param uninitializedClassTraits Uninitialized class traits
     * @param insideDoInitAction Inside DoInitAction?
     * @param insideFunction Inside function?
     * @param regNames Register names
     * @param variables Variables
     * @param functions Functions
     * @param actions List of actions
     * @param version SWF version
     * @param staticOperation Unused
     * @param path Path
     * @param charset Charset
     * @return List of treeItems
     * @throws InterruptedException On interrupt
     */
    public static List<GraphTargetItem> actionsToTree(Map<String, Map<String, Trait>> uninitializedClassTraits, boolean insideDoInitAction, boolean insideFunction, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, List<Action> actions, int version, int staticOperation, String path, String charset) throws InterruptedException {
        HashMap<String, GraphTargetItem> variablesBackup = new LinkedHashMap<>(variables);
        HashMap<String, GraphTargetItem> functionsBackup = new LinkedHashMap<>(functions);
        try {
            return ActionGraph.translateViaGraph(uninitializedClassTraits, null, insideDoInitAction, insideFunction, regNames, variables, functions, actions, version, staticOperation, path, charset);
        } catch (SecondPassException spe) {
            variables.clear();
            variables.putAll(variablesBackup);
            functions.clear();
            functions.putAll(functionsBackup);
            return ActionGraph.translateViaGraph(uninitializedClassTraits, spe.getData(), insideDoInitAction, insideFunction, regNames, variables, functions, actions, version, staticOperation, path, charset);
        }
    }

    /**
     * Translates this function to stack and output.
     *
     * @param uninitializedClassTraits Uninitialized class traits
     * @param secondPassData Second pass data
     * @param insideDoInitAction Inside DoInitAction?
     * @param lineStartIns Line start instruction
     * @param stack Stack
     * @param output Output
     * @param regNames Register names
     * @param variables Variables
     * @param functions Functions
     * @param staticOperation the value of staticOperation
     * @param path the value of path
     * @throws InterruptedException On interrupt
     */
    public void translate(Map<String, Map<String, Trait>> uninitializedClassTraits, SecondPassData secondPassData, boolean insideDoInitAction, GraphSourceItem lineStartIns, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) throws InterruptedException {
    }

    /**
     * Translate the item to target items.
     *
     * @param localData Local data
     * @param stack Stack
     * @param output Output list
     * @param staticOperation Unused
     * @param path Path
     * @throws InterruptedException On interrupt
     */
    @Override
    public void translate(BaseLocalData localData, TranslateStack stack, List<GraphTargetItem> output, int staticOperation, String path) throws InterruptedException {
        ActionLocalData aLocalData = (ActionLocalData) localData;
        translate(aLocalData.uninitializedClassTraits, aLocalData.secondPassData, aLocalData.insideDoInitAction, aLocalData.lineStartAction, stack, output, aLocalData.regNames, aLocalData.variables, aLocalData.functions, staticOperation, path);
    }

    /**
     * Checks whether this item is a jump.
     *
     * @return True if this item is a jump, false otherwise
     */
    @Override
    public boolean isJump() {
        return false;
    }

    /**
     * Checks whether this item is a branch.
     *
     * @return True if this item is a branch, false otherwise
     */
    @Override
    public boolean isBranch() {
        return false;
    }

    /**
     * Checks whether this item is an exit (throw, return, etc.).
     *
     * @return True if this item is an exit, false otherwise
     */
    @Override
    public boolean isExit() {
        return false;
    }

    /**
     * Gets branches
     *
     * @param code Code
     * @return List of IPs to branch to
     */
    @Override
    public List<Integer> getBranches(GraphSource code) {
        return new ArrayList<>();
    }

    /**
     * Checks whether this item is ignored.
     *
     * @return True if this item is ignored, false otherwise
     */
    @Override
    public boolean isIgnored() {
        return ignored;
    }

    /**
     * Sets whether this item is ignored.
     *
     * @param ignored True if this item is ignored, false otherwise
     * @param pos Sub position
     */
    @Override
    public void setIgnored(boolean ignored, int pos) {
        this.ignored = ignored;
    }

    /**
     * Prepares variables for the given container.
     *
     * @param cnt Container
     * @param variables Variables - map of variable names to variable items
     * @return Variables
     */
    private static HashMap<String, GraphTargetItem> prepareVariables(GraphSourceItemContainer cnt, HashMap<String, GraphTargetItem> variables) {
        HashMap<String, GraphTargetItem> variables2 = new LinkedHashMap<>(variables);
        if (cnt instanceof ActionDefineFunction || cnt instanceof ActionDefineFunction2) {
            for (int r = 0; r < 256; r++) {
                if (variables2.containsKey("__register" + r)) {
                    variables2.remove("__register" + r);
                }
            }
        }
        return variables2;
    }

    /**
     * Converts list of actions to tree.
     *
     * @param graph ActionGraph
     * @param switchParts Switch parts
     * @param secondPassData Second pass data
     * @param insideDoInitAction Inside DoInitAction?
     * @param lineStartActionRef Line start action reference
     * @param registerNames Register names
     * @param variables Variables
     * @param functions Functions
     * @param stack Stack
     * @param actions List of actions
     * @param start Start
     * @param end End
     * @param version SWF version
     * @param staticOperation Static operation
     * @param path Path
     * @param charset Charset
     * @return List of tree items
     * @throws InterruptedException On interrupt
     * @throws GraphPartChangeException On graph part change
     */
    public static List<GraphTargetItem> actionsPartToTree(ActionGraph graph, Set<GraphPart> switchParts, SecondPassData secondPassData, boolean insideDoInitAction, Reference<GraphSourceItem> lineStartActionRef, HashMap<Integer, String> registerNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, TranslateStack stack, List<Action> actions, int start, int end, int version, int staticOperation, String path, String charset) throws InterruptedException, GraphPartChangeException {
        if (start < actions.size() && (end > 0) && (start > 0)) {
            logger.log(Level.FINE, "Entering {0}-{1}{2}", new Object[]{start, end, actions.size() > 0 ? (" (" + actions.get(start).toString() + " - " + actions.get(end == actions.size() ? end - 1 : end) + ")") : ""});
        }
        ActionLocalData localData = new ActionLocalData(switchParts, secondPassData, insideDoInitAction, registerNames, variables, functions, graph.getUninitializedClassTraits());
        localData.lineStartAction = lineStartActionRef.getVal();
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
            if (Configuration.simplifyExpressions.get()) {
                stack.simplify();
            }
            Action action = actions.get(ip);            
            if (action.isIgnored()) {
                ip++;
                continue;
            }

            //FunctionActionItem after DefineFunction(/2) are left on the stack. For linestart offsets we consider this kind of stack empty.
            boolean isStackEmpty = true;
            for (int i = 0; i < stack.size(); i++) {
                if ((!(stack.get(i) instanceof FunctionActionItem))) {
                    isStackEmpty = false;
                    break;
                }
            }

            if (isStackEmpty) {
                localData.lineStartAction = action;
                lineStartActionRef.setVal(action);
            }
            if (action instanceof GraphSourceItemContainer) {
                GraphSourceItemContainer cnt = (GraphSourceItemContainer) action;
                //List<GraphTargetItem> out=actionsPartToTree(new HashMap<Integer, String>(), new HashMap<String, GraphTargetItem>(),new HashMap<String, GraphTargetItem>(), new TranslateStack(), src, ip+1,endip-1 , version);
                long endAddr = action.getAddress() + cnt.getHeaderSize();
                String cntName = cnt.getName();
                List<List<GraphTargetItem>> outs = new ArrayList<>();
                HashMap<String, GraphTargetItem> variables2 = prepareVariables(cnt, variables);
                for (long size : cnt.getContainerSizes()) {
                    if (size == 0) {
                        outs.add(new ArrayList<>());
                        continue;
                    }
                    List<GraphTargetItem> out;
                    try {
                        HashMap<Integer, String> regNames = cnt.getRegNames();
                        if (action instanceof ActionWith || action instanceof ActionTry) {
                            for (Map.Entry<Integer, String> e : registerNames.entrySet()) {
                                if (!regNames.containsKey(e.getKey())) {
                                    regNames.put(e.getKey(), e.getValue());
                                }
                            }
                        }
                        try {
                            out = ActionGraph.translateViaGraph(graph.getUninitializedClassTraits(), null, insideDoInitAction, true, regNames, variables2, functions, actions.subList(adr2ip(actions, endAddr), adr2ip(actions, endAddr + size)), version, staticOperation, path + (cntName == null ? "" : "/" + cntName), charset);
                        } catch (SecondPassException spe) {
                            variables2 = prepareVariables(cnt, variables);
                            out = ActionGraph.translateViaGraph(graph.getUninitializedClassTraits(), spe.getData(), insideDoInitAction, true, regNames, variables2, functions, actions.subList(adr2ip(actions, endAddr), adr2ip(actions, endAddr + size)), version, staticOperation, path + (cntName == null ? "" : "/" + cntName), charset);
                        }
                    } catch (OutOfMemoryError | TranslateException | StackOverflowError ex) {
                        logger.log(Level.SEVERE, "Decompilation error in: " + path, ex);
                        if (ex instanceof OutOfMemoryError) {
                            Helper.freeMem();
                        }

                        out = new ArrayList<>();
                        String[] lines = new String[]{
                            "",
                            " * " + AppResources.translate("decompilationError"),
                            " * " + AppResources.translate("decompilationError.obfuscated"),
                            Helper.decompilationErrorAdd == null ? null : " * " + Helper.decompilationErrorAdd,
                            " * " + AppResources.translate("decompilationError.errorType") + ": "
                            + ex.getClass().getSimpleName(),
                            ""};
                        out.add(new CommentItem(lines));
                    }
                    outs.add(out);
                    endAddr += size;
                }

                ((GraphSourceItemContainer) action).translateContainer(outs, action, stack, output, registerNames, variables, functions);
                ip = adr2ip(actions, endAddr);
                continue;
            }

            //return in for..in
            if ((action instanceof ActionPush) && (((ActionPush) action).values.size() == 1) && (((ActionPush) action).values.get(0) == Null.INSTANCE)) {
                if (ip + 3 <= end) {
                    if ((actions.get(ip + 1) instanceof ActionEquals) || (actions.get(ip + 1) instanceof ActionEquals2)) {
                        if (actions.get(ip + 2) instanceof ActionNot) {
                            if (actions.get(ip + 3) instanceof ActionIf) {
                                ActionIf aif = (ActionIf) actions.get(ip + 3);
                                if (adr2ip(actions, ip2adr(actions, ip + 3) + 5 + aif.getJumpOffset()) == ip) {
                                    ip += 4;
                                    continue;
                                }
                            }
                        }
                    }
                }
            }

            if (action instanceof ActionStore) {
                ActionStore store = (ActionStore) action;
                store.setStore(actions.subList(ip + 1, ip + 1 + store.getStoreSize()));
                ip = ip + 1 + store.getStoreSize() - 1/*ip++ will be next*/;
            }

            action.translate(localData, stack, output, staticOperation, path);

            if (((action instanceof ActionSetTarget) || (action instanceof ActionSetTarget2)) && (!stack.isEmpty())) {
                GraphTargetItem lastItem = output.remove(output.size() - 1);
                graph.makeAllCommands(output, stack);
                output.add(lastItem);
            }

            ip++;
        }
        if (ip > end + 1) {
            throw new GraphPartChangeException(output, ip);
        }
        logger.log(Level.FINE, "Leaving {0}-{1}", new Object[]{start, end});
        return output;
    }

    /**
     * Gets item without global prefix.
     *
     * @param ti Target item
     * @return Target item without global prefix
     */
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
                        GetVariableActionItem gvt = new GetVariableActionItem(null, null, ((GetMemberActionItem) t).memberName);
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

    /**
     * Checks whether the loops are ignored.
     *
     * @return True if the loops are ignored, false otherwise
     */
    @Override
    public boolean ignoredLoops() {
        return false;
    }

    /**
     * Sets constant pool to actions.
     *
     * @param actions List of actions
     * @param cpool Constant pool
     */
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

    /**
     * Get ASM source with replaced Actions.
     *
     * @param container Container
     * @param knownAddresses Known addresses
     * @param exportMode Export mode
     * @param writer Writer
     * @return Writer
     */
    public GraphTextWriter getASMSourceReplaced(ActionList container, Set<Long> knownAddresses, ScriptExportMode exportMode, GraphTextWriter writer) {
        writer.appendNoHilight(getASMSource(container, knownAddresses, exportMode));
        return writer;
    }

    /**
     * Converts ECMA object to float point.
     *
     * @param o Object
     * @return Float point
     */
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
        if (o == Null.INSTANCE) {
            return Double.NaN;
        }
        if (o == Undefined.INSTANCE) {
            return Double.NaN;
        }
        if (o instanceof Boolean) {
            return (Boolean) o ? 1.0 : 0.0;
        }
        if (o instanceof String) {
            try {
                return Double.parseDouble((String) o);
            } catch (NumberFormatException nfe) {
                return Double.NaN;
            }
        }
        return 0;
    }

    /**
     * Converts Get to Set. (variables, members, properties)
     *
     * @param get Get item
     * @param value Value
     * @param variables Variables
     * @return Set item
     */
    public static GraphTargetItem gettoset(GraphTargetItem get, GraphTargetItem value, List<VariableActionItem> variables) {
        GraphTargetItem ret = get;
        boolean boxed = false;
        if (get instanceof VariableActionItem) {
            boxed = true;
            ret = ((VariableActionItem) ret).getBoxedValue();
        }
        if (ret instanceof GetVariableActionItem) {
            GetVariableActionItem gv = (GetVariableActionItem) ret;
            ret = new SetVariableActionItem(null, null, gv.name, value);
        } else if (ret instanceof GetMemberActionItem) {
            GetMemberActionItem mem = (GetMemberActionItem) ret;
            ret = new SetMemberActionItem(null, null, mem.object, mem.memberName, value);
        } else if ((ret instanceof DirectValueActionItem) && ((DirectValueActionItem) ret).value instanceof RegisterNumber) {
            ret = new StoreRegisterActionItem(null, null, (RegisterNumber) ((DirectValueActionItem) ret).value, value, false);
        } else if (ret instanceof GetPropertyActionItem) {
            GetPropertyActionItem gp = (GetPropertyActionItem) ret;
            ret = new SetPropertyActionItem(null, null, gp.target, gp.propertyIndex, value);
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

    /**
     * Checks whether this item is a DeobfuscatePop instruction. It is a special
     * instruction for deobfuscation.
     *
     * @return True if this item is a DeobfuscatePop instruction, false
     * otherwise
     */
    @Override
    public boolean isDeobfuscatePop() {
        return false;
    }

    /**
     * Gets the line in the high level source code.
     *
     * @return Line
     */
    @Override
    public int getLine() {
        return 0;
    }

    /**
     * Gets the high level source code file name.
     *
     * @return File name
     */
    @Override
    public String getFile() {
        return null;
    }

    /**
     * Gets virtual address. A virtual address can be used for storing original
     * address before applying deobfuscation.
     *
     * @return Virtual address
     */
    @Override
    public long getVirtualAddress() {
        return virtualAddress;
    }

    /**
     * Sets virtual address. A virtual address can be used for storing original
     * address before applying deobfuscation.
     *
     * @param virtualAddress Virtual address
     */
    @Override
    public void setVirtualAddress(long virtualAddress) {
        this.virtualAddress = virtualAddress;
    }
}
