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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ABCInputStream;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.CodeStats;
import com.jpexs.decompiler.flash.abc.avm2.UnknownInstructionCode;
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.DeobfuscationLevel;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFField;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.MemoryInputStream;
import com.jpexs.helpers.stat.Statistics;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public final class MethodBody implements Cloneable {

    private static final Logger logger = Logger.getLogger(MethodBody.class.getName());

    private static final String DEBUG_FIXED = null;

    @Internal
    public boolean deleted;

    @Internal
    boolean debugMode = false;

    public int method_info;

    public int max_stack;

    public int max_regs;

    public int init_scope_depth;

    public int max_scope_depth;

    @SWFField
    private byte[] codeBytes;

    private AVM2Code code;

    public ABCException[] exceptions;

    public Traits traits;

    @Internal
    public transient List<GraphTargetItem> convertedItems;

    @Internal
    public transient Throwable convertException;

    @Internal
    private ABC abc;

    /**
     * DependencyParser uses this
     */
    @Internal
    private transient MethodBody lastConvertedBody = null;

    public MethodBody() {
        this.traits = new Traits();
        this.codeBytes = SWFInputStream.BYTE_ARRAY_EMPTY;
        this.exceptions = new ABCException[0];
        this.abc = null;
    }

    public void setAbc(ABC abc) {
        this.abc = abc;
    }

    public MethodBody(ABC abc, Traits traits, byte[] codeBytes, ABCException[] exceptions) {
        this.traits = traits;
        this.codeBytes = codeBytes;
        this.exceptions = exceptions;
        this.abc = abc;
    }

    public synchronized void setCodeBytes(byte codeBytes[]) {
        this.codeBytes = codeBytes;
        this.code = null;
    }

    public void setModified() {
        this.codeBytes = null;
    }

    public synchronized byte[] getCodeBytes() {
        if (codeBytes != null) {
            return codeBytes;
        } else {
            return code.getBytes();
        }
    }

    public synchronized AVM2Code getCode() {
        if (code == null) {
            AVM2Code avm2Code;
            try {
                ABCInputStream ais = new ABCInputStream(new MemoryInputStream(codeBytes));
                avm2Code = new AVM2Code(ais, this);
                avm2Code.removeWrongIndices(abc.constants);
            } catch (UnknownInstructionCode | IOException ex) {
                avm2Code = new AVM2Code();
                logger.log(Level.SEVERE, null, ex);
            }
            avm2Code.compact();
            code = avm2Code;
        }
        return code;
    }

    public void setCode(AVM2Code code) {
        this.code = code;
        this.codeBytes = null;
    }

    public void markOffsets() {
        getCode().markOffsets();
    }

    @Override
    public String toString() {
        String s = "";
        s += "method_info=" + method_info + " max_stack=" + max_stack + " max_regs=" + max_regs + " scope_depth=" + init_scope_depth + " max_scope=" + max_scope_depth;
        s += "\r\nCode:\r\n" + getCode().toString();
        return s;
    }

    public int removeDeadCode(AVM2ConstantPool constants, Trait trait, MethodInfo info) throws InterruptedException {
        return getCode().removeDeadCode(this);
    }

    public int removeTraps(ABC abc, Trait trait, int scriptIndex, int classIndex, boolean isStatic, String path) throws InterruptedException {

        return getCode().removeTraps(trait, method_info, this, abc, scriptIndex, classIndex, isStatic, path);
    }

    public void deobfuscate(DeobfuscationLevel level, Trait trait, int scriptIndex, int classIndex, boolean isStatic, String path) throws InterruptedException {
        if (level == DeobfuscationLevel.LEVEL_REMOVE_DEAD_CODE) {
            removeDeadCode(abc.constants, trait, abc.method_info.get(method_info));
        } else if (level == DeobfuscationLevel.LEVEL_REMOVE_TRAPS) {
            removeTraps(abc, trait, scriptIndex, classIndex, isStatic, path);
        } else if (level == DeobfuscationLevel.LEVEL_RESTORE_CONTROL_FLOW) {
            removeTraps(abc, trait, scriptIndex, classIndex, isStatic, path);
        }

        ((Tag) abc.parentTag).setModified(true);
    }

    public void removeInstruction(int pos) {
        getCode().removeInstruction(pos, this);
    }

    /**
     * Replaces instruction by another. Properly handles offsets. Note: If
     * newinstruction is jump, the offset operand must be handled properly by
     * caller.
     *
     * @param pos
     * @param instruction
     */
    public void replaceInstruction(int pos, AVM2Instruction instruction) {
        getCode().replaceInstruction(pos, instruction, this);
    }

    /**
     * Inserts instruction at specified point. Handles offsets properly. Note:
     * If newinstruction is jump, the offset operand must be handled properly by
     * caller. All old jump offsets to pos are targeted before new instruction.
     *
     * @param pos Position in the list
     * @param instruction Instruction False means before new instruction
     */
    public void insertInstruction(int pos, AVM2Instruction instruction) {
        getCode().insertInstruction(pos, instruction, this);
    }

    public void insertAll(int pos, List<AVM2Instruction> list) {
        for (AVM2Instruction ins : list) {
            insertInstruction(pos++, ins);
        }
    }

    /**
     * Inserts instruction at specified point. Handles offsets properly. Note:
     * If newinstruction is jump, the offset operand must be handled properly by
     * caller.
     *
     * @param pos Position in the list
     * @param instruction Instruction
     * @param mapOffsetsAfterIns Map all jumps to the pos after new instruction?
     * False means before new instruction
     */
    public void insertInstruction(int pos, AVM2Instruction instruction, boolean mapOffsetsAfterIns) {
        getCode().insertInstruction(pos, instruction, mapOffsetsAfterIns, this);
    }

    public int getLocalReservedCount() {
        MethodInfo methodInfo = abc.method_info.get(this.method_info);
        int pos = methodInfo.param_types.length + 1;
        if (methodInfo.flagNeed_arguments()) {
            pos++;
        }
        if (methodInfo.flagNeed_rest()) {
            pos++;
        }
        return pos;
    }

    public HashMap<Integer, String> getLocalRegNames(ABC abc) {
        HashMap<Integer, String> ret = new HashMap<>();
        for (int i = 1; i <= abc.method_info.get(this.method_info).param_types.length; i++) {
            String paramName = "param" + i;
            if (abc.method_info.get(this.method_info).flagHas_paramnames() && Configuration.paramNamesEnable.get()) {
                paramName = abc.constants.getString(abc.method_info.get(this.method_info).paramNames[i - 1]);
            }
            ret.put(i, paramName);
        }
        int pos = abc.method_info.get(this.method_info).param_types.length + 1;
        if (abc.method_info.get(this.method_info).flagNeed_arguments()) {
            ret.put(pos, "arguments");
            pos++;
        }
        if (abc.method_info.get(this.method_info).flagNeed_rest()) {
            ret.put(pos, "rest");
            pos++;
        }

        if (Configuration.getLocalNamesFromDebugInfo.get()) {
            Map<Integer, String> debugRegNames = getCode().getLocalRegNamesFromDebug(abc);
            for (int k : debugRegNames.keySet()) {
                ret.put(k, debugRegNames.get(k));
            }
        }
        return ret;
    }

    public void convert(final ConvertData convertData, final String path, ScriptExportMode exportMode, final boolean isStatic, final int methodIndex, final int scriptIndex, final int classIndex, final ABC abc, final Trait trait, final ScopeStack scopeStack, final int initializerType, final NulWriter writer, final List<DottedChain> fullyQualifiedNames, final List<Traits> initTraits, boolean firstLevel, Set<Integer> seenMethods) throws InterruptedException {
        seenMethods.add(this.method_info);
        if (debugMode) {
            System.err.println("Decompiling " + path);
        }
        if (exportMode != ScriptExportMode.AS) {
            getCode().toASMSource(abc, abc.constants, abc.method_info.get(this.method_info), this, exportMode, writer);
        } else {
            if ((DEBUG_FIXED != null && !path.endsWith(DEBUG_FIXED)) || (!Configuration.decompile.get())) {
                writer.appendNoHilight(Helper.getDecompilationSkippedComment()).newLine();
                return;
            }
            int timeout = Configuration.decompilationTimeoutSingleMethod.get();
            convertException = null;
            try {
                Callable<Void> callable = new Callable<Void>() {
                    @Override
                    public Void call() throws InterruptedException {
                        try (Statistics s1 = new Statistics("MethodBody.convert")) {
                            MethodBody converted = convertMethodBody(convertData.deobfuscationMode != 0, path, isStatic, scriptIndex, classIndex, abc, trait);
                            HashMap<Integer, String> localRegNames = getLocalRegNames(abc);
                            List<GraphTargetItem> convertedItems1;
                            try (Statistics s = new Statistics("AVM2Code.toGraphTargetItems")) {
                                convertedItems1 = converted.getCode().toGraphTargetItems(convertData.thisHasDefaultToPrimitive, convertData, path, methodIndex, isStatic, scriptIndex, classIndex, abc, converted, localRegNames, scopeStack, initializerType, fullyQualifiedNames, initTraits, Graph.SOP_USE_STATIC, new HashMap<>(), converted.getCode().visitCode(converted));
                            }
                            try (Statistics s = new Statistics("Graph.graphToString")) {
                                Graph.graphToString(convertedItems1, writer, LocalData.create(abc, localRegNames, fullyQualifiedNames, seenMethods));
                            }
                            convertedItems = convertedItems1;
                        }
                        return null;
                    }
                };
                if (firstLevel) {
                    CancellableWorker.call(callable, timeout, TimeUnit.SECONDS);
                } else {
                    callable.call();
                }
            } catch (InterruptedException ex) {
                throw ex;
            } catch (CancellationException ex) {
                throw new InterruptedException();
            } catch (Exception | OutOfMemoryError | StackOverflowError ex) {
                convertException = ex;
                Throwable cause = ex.getCause();
                if (ex instanceof ExecutionException && cause instanceof Exception) {
                    convertException = (Exception) cause;
                }
                if (convertException instanceof TimeoutException) {
                    logger.log(Level.SEVERE, "Decompilation timeout in: " + path, convertException);
                } else {
                    logger.log(Level.SEVERE, "Decompilation error in: " + path, convertException);
                }

            }
        }
    }

    public GraphTextWriter toString(final String path, ScriptExportMode exportMode, final ABC abc, final Trait trait, final GraphTextWriter writer, final List<DottedChain> fullyQualifiedNames, Set<Integer> seenMethods) throws InterruptedException {
        seenMethods.add(method_info);

        if (exportMode != ScriptExportMode.AS) {
            getCode().toASMSource(abc, abc.constants, abc.method_info.get(this.method_info), this, exportMode, writer);
        } else {
            if ((DEBUG_FIXED != null && !path.endsWith(DEBUG_FIXED)) || (!Configuration.decompile.get())) {
                //writer.startMethod(this.method_info);
                writer.appendNoHilight(Helper.getDecompilationSkippedComment()).newLine();
                //writer.endMethod();
                return writer;
            }
            int timeout = Configuration.decompilationTimeoutSingleMethod.get();

            try (Statistics s = new Statistics("MethodBody.toString")) {
                if (convertException == null) {
                    HashMap<Integer, String> localRegNames = getLocalRegNames(abc);
                    //writer.startMethod(this.method_info);
                    if (Configuration.showMethodBodyId.get()) {
                        writer.appendNoHilight("// method body index: ");
                        writer.appendNoHilight(abc.findBodyIndex(this.method_info));
                        writer.appendNoHilight(" method index: ");
                        writer.appendNoHilight(this.method_info);
                        writer.newLine();
                    }
                    Graph.graphToString(convertedItems, writer, LocalData.create(abc, localRegNames, fullyQualifiedNames, seenMethods));
                    //writer.endMethod();
                } else if (convertException instanceof TimeoutException) {
                    // exception was logged in convert method
                    Helper.appendTimeoutCommentAs3(writer, timeout, getCode().code.size());
                } else {
                    // exception was logged in convert method
                    Helper.appendErrorComment(writer, convertException);
                }
            }
        }
        return writer;
    }

    public MethodBody convertMethodBodyCanUseLast(boolean deobfuscate, String path, boolean isStatic, int scriptIndex, int classIndex, ABC abc, Trait trait) throws InterruptedException {
        if (lastConvertedBody != null) {
            return lastConvertedBody;
        }
        return convertMethodBody(deobfuscate, path, isStatic, scriptIndex, classIndex, abc, trait);
    }

    public void clearLastConverted() {
        this.lastConvertedBody = null;
    }

    public MethodBody convertMethodBody(boolean deobfuscate, String path, boolean isStatic, int scriptIndex, int classIndex, ABC abc, Trait trait) throws InterruptedException {
        MethodBody body = clone();
        AVM2Code code = body.getCode();
        code.markVirtualAddresses();
        code.fixJumps(path, body);

        if (deobfuscate) {
            try {
                code.removeTraps(trait, method_info, body, abc, scriptIndex, classIndex, isStatic, path);
            } catch (ThreadDeath | InterruptedException ex) {
                throw ex;
            } catch (Throwable ex) {
                //ignore
                logger.log(Level.SEVERE, "Deobfuscation failed in: " + path, ex);
                body = clone();
                code = body.getCode();
                code.fixJumps(path, body);
                return body;
            }
        }

        lastConvertedBody = body;
        return body;
    }

    public String toSource(int scriptIndex, Set<Integer> seenMethods) {
        ConvertData convertData = new ConvertData();
        convertData.deobfuscationMode = 0;
        try {
            convert(convertData, "", ScriptExportMode.AS, false, method_info, 0, 0, abc, null, new ScopeStack(), 0, new NulWriter(), new ArrayList<>(), new ArrayList<>(), true, seenMethods);
            HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
            writer.indent().indent().indent();
            toString("", ScriptExportMode.AS, abc, null, writer, new ArrayList<>(), seenMethods);
            writer.unindent().unindent().unindent();
            return writer.toString();
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public MethodBody clone() {
        return clone(false);
    }

    public MethodBody clone(boolean deepTraits) {
        try {
            MethodBody ret = (MethodBody) super.clone();
            if (code != null) {
                ret.code = code.clone();
            }

            if (exceptions != null) {
                ret.exceptions = new ABCException[exceptions.length];
                for (int i = 0; i < exceptions.length; i++) {
                    ret.exceptions[i] = exceptions[i].clone();
                }
            }

            if (deepTraits && traits != null) {
                ret.traits = traits.clone();
            }
            ret.convertedItems = null;
            ret.convertException = null;

            return ret;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException();
        }
    }

    public boolean autoFillStats(ABC abc, int initScope, boolean hasThis) {
        //System.out.println("--------------");
        CodeStats stats = getCode().getStats(abc, this, initScope, true);
        if (stats == null) {
            return false;
        }
        if (stats.has_activation) {
            initScope++;
        }
        max_stack = stats.maxstack;
        max_scope_depth = stats.maxscope + (stats.has_activation ? 1 : 0);
        max_regs = stats.maxlocal;
        init_scope_depth = initScope;
        abc.method_info.get(method_info).setFlagSetsdxns(stats.has_set_dxns);
        abc.method_info.get(method_info).setFlagNeed_activation(stats.has_activation);
        MethodInfo mi = abc.method_info.get(method_info);
        int min_regs = mi.param_types.length + 1 + (mi.flagNeed_rest() ? 1 : 0);
        if (max_regs < min_regs) {
            max_regs = min_regs;
        }
        return true;
    }

    public boolean autoFillMaxRegs(ABC abc) {
        CodeStats stats = getCode().getMaxLocal();
        if (stats == null) {
            return false;
        }
        max_regs = stats.maxlocal;
        MethodInfo mi = abc.method_info.get(method_info);
        int min_regs = mi.param_types.length + 1 + (mi.flagNeed_rest() ? 1 : 0);
        if (max_regs < min_regs) {
            max_regs = min_regs;
        }

        return true;
    }
}
