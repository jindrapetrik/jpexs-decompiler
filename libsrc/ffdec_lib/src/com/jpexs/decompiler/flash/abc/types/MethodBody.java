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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MethodBody implements Cloneable {

    private static final Logger logger = Logger.getLogger(MethodBody.class.getName());

    private static final String DEBUG_FIXED = null;

    @Internal
    public boolean deleted;

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

    public MethodBody(ABC abc) {
        this.traits = new Traits();
        this.codeBytes = SWFInputStream.BYTE_ARRAY_EMPTY;
        this.exceptions = new ABCException[0];
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

    public List<Integer> getExceptionEntries() {
        List<Integer> ret = new ArrayList<>();
        AVM2Code code = getCode();
        for (ABCException e : exceptions) {
            ret.add(code.adr2pos(e.start, true));
            ret.add(code.adr2pos(e.end, true));
            ret.add(code.adr2pos(e.target));
        }
        return ret;
    }

    public void markOffsets() {
        long offset = 0;
        AVM2Code code = getCode();
        for (int i = 0; i < code.code.size(); i++) {
            code.code.get(i).offset = offset;
            offset += code.code.get(i).getBytesLength();
        }
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

    public void restoreControlFlow(AVM2ConstantPool constants, Trait trait, MethodInfo info) throws InterruptedException {
        getCode().restoreControlFlow(constants, trait, info, this);
    }

    public int removeTraps(AVM2ConstantPool constants, ABC abc, Trait trait, int scriptIndex, int classIndex, boolean isStatic, String path) throws InterruptedException {

        return getCode().removeTraps(constants, trait, abc.method_info.get(method_info), this, abc, scriptIndex, classIndex, isStatic, path);
    }

    public void deobfuscate(DeobfuscationLevel level, Trait trait, int scriptIndex, int classIndex, boolean isStatic, String path) throws InterruptedException {
        if (level == DeobfuscationLevel.LEVEL_REMOVE_DEAD_CODE) {
            removeDeadCode(abc.constants, trait, abc.method_info.get(method_info));
        } else if (level == DeobfuscationLevel.LEVEL_REMOVE_TRAPS) {
            removeTraps(abc.constants, abc, trait, scriptIndex, classIndex, isStatic, path);
        } else if (level == DeobfuscationLevel.LEVEL_RESTORE_CONTROL_FLOW) {
            removeTraps(abc.constants, abc, trait, scriptIndex, classIndex, isStatic, path);
            restoreControlFlow(abc.constants, trait, abc.method_info.get(method_info));
        }
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
        int pos = abc.method_info.get(this.method_info).param_types.length + 1;
        if (abc.method_info.get(this.method_info).flagNeed_arguments()) {
            pos++;
        }
        if (abc.method_info.get(this.method_info).flagNeed_rest()) {
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

    public void convert(final String path, ScriptExportMode exportMode, final boolean isStatic, final int scriptIndex, final int classIndex, final ABC abc, final Trait trait, final AVM2ConstantPool constants, final List<MethodInfo> method_info, final ScopeStack scopeStack, final boolean isStaticInitializer, final GraphTextWriter writer, final List<DottedChain> fullyQualifiedNames, final Traits initTraits, boolean firstLevel) throws InterruptedException {
        if (debugMode) {
            System.err.println("Decompiling " + path);
        }
        if (exportMode != ScriptExportMode.AS) {
            getCode().toASMSource(constants, trait, method_info.get(this.method_info), this, exportMode, writer);
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
                        MethodBody converted = convertMethodBody(path, isStatic, scriptIndex, classIndex, abc, trait, constants, method_info, scopeStack, isStaticInitializer, fullyQualifiedNames, initTraits);
                        HashMap<Integer, String> localRegNames = getLocalRegNames(abc);
                        List<GraphTargetItem> convertedItems1 = converted.getCode().toGraphTargetItems(path, isStatic, scriptIndex, classIndex, abc, constants, method_info, converted, localRegNames, scopeStack, isStaticInitializer, fullyQualifiedNames, initTraits, Graph.SOP_USE_STATIC, new HashMap<>(), converted.getCode().visitCode(converted));
                        Graph.graphToString(convertedItems1, writer, LocalData.create(constants, localRegNames, fullyQualifiedNames));
                        convertedItems = convertedItems1;
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
            } catch (Exception | OutOfMemoryError | StackOverflowError ex) {
                if (ex instanceof TimeoutException) {
                    logger.log(Level.SEVERE, "Decompilation timeout in: " + path, ex);
                } else {
                    logger.log(Level.SEVERE, "Decompilation error in: " + path, ex);
                }
                convertException = ex;
                Throwable cause = ex.getCause();
                if (ex instanceof ExecutionException && cause instanceof Exception) {
                    convertException = (Exception) cause;
                }
            }
        }
    }

    public GraphTextWriter toString(final String path, ScriptExportMode exportMode, final ABC abc, final Trait trait, final AVM2ConstantPool constants, final List<MethodInfo> method_info, final GraphTextWriter writer, final List<DottedChain> fullyQualifiedNames) throws InterruptedException {
        if (exportMode != ScriptExportMode.AS) {
            getCode().toASMSource(constants, trait, method_info.get(this.method_info), this, exportMode, writer);
        } else {
            if ((DEBUG_FIXED != null && !path.endsWith(DEBUG_FIXED)) || (!Configuration.decompile.get())) {
                //writer.startMethod(this.method_info);
                writer.appendNoHilight(Helper.getDecompilationSkippedComment()).newLine();
                //writer.endMethod();
                return writer;
            }
            int timeout = Configuration.decompilationTimeoutSingleMethod.get();

            if (convertException == null) {
                HashMap<Integer, String> localRegNames = getLocalRegNames(abc);
                //writer.startMethod(this.method_info);
                if (Configuration.showMethodBodyId.get()) {
                    writer.appendNoHilight("// method body id: ");
                    writer.appendNoHilight(abc.findBodyIndex(this.method_info));
                    writer.newLine();
                }
                Graph.graphToString(convertedItems, writer, LocalData.create(constants, localRegNames, fullyQualifiedNames));
                //writer.endMethod();
            } else if (convertException instanceof TimeoutException) {
                // exception was logged in convert method
                Helper.appendTimeoutCommentAs3(writer, timeout, getCode().code.size());
            } else {
                // exception was logged in convert method
                Helper.appendErrorComment(writer, convertException);
            }
        }
        return writer;
    }

    public MethodBody convertMethodBody(String path, boolean isStatic, int scriptIndex, int classIndex, ABC abc, Trait trait, AVM2ConstantPool constants, List<MethodInfo> method_info, ScopeStack scopeStack, boolean isStaticInitializer, List<DottedChain> fullyQualifiedNames, Traits initTraits) throws InterruptedException {
        MethodBody body = clone();
        AVM2Code code = body.getCode();
        code.markMappedOffsets();
        code.fixJumps(path, body);

        if (Configuration.autoDeobfuscate.get()) {
            try {
                code.removeTraps(constants, trait, method_info.get(this.method_info), body, abc, scriptIndex, classIndex, isStatic, path);
            } catch (ThreadDeath | InterruptedException ex) {
                throw ex;
            } catch (Throwable ex) {
                //ignore
                body = clone();
                code = body.getCode();
                code.markMappedOffsets();
                code.fixJumps(path, body);
                return body;
            }
        }

        return body;
    }

    @Override
    public MethodBody clone() {
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

            // maybe deep clone traits
            /*if (traits != null) {
             ret.traits = traits.clone();
             }*/
            ret.convertedItems = null;
            ret.convertException = null;

            return ret;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException();
        }
    }

    public boolean autoFillStats(ABC abc, int initScope, boolean hasThis) {
        //System.out.println("--------------");
        CodeStats stats = getCode().getStats(abc, this, initScope);
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
}
