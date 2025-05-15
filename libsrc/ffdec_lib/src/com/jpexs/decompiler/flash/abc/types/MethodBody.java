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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ABCInputStream;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.CodeStats;
import com.jpexs.decompiler.flash.abc.avm2.UnknownInstructionCodeException;
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.DeobfuscationLevel;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
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
 * Method body of a method in ABC file.
 *
 * @author JPEXS
 */
public final class MethodBody implements Cloneable {

    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger(MethodBody.class.getName());

    /**
     * Used for debugging - decompile only fixed method
     */
    private static final String DEBUG_FIXED = null;

    /**
     * Method body is deleted
     */
    @Internal
    public boolean deleted;

    /**
     * Debug mode
     */
    @Internal
    boolean debugMode = false;

    /**
     * Method info index
     */
    public int method_info;

    /**
     * Maximum stack size
     */
    public int max_stack;

    /**
     * Maximum number of registers
     */
    public int max_regs;

    /**
     * Initial scope depth
     */
    public int init_scope_depth;

    /**
     * Maximum scope depth
     */
    public int max_scope_depth;

    /**
     * Code bytes
     */
    @SWFField
    private byte[] codeBytes;

    /**
     * AVM2 code
     */
    private AVM2Code code;

    /**
     * Exceptions
     */
    public ABCException[] exceptions;

    /**
     * Traits
     */
    public Traits traits;

    /**
     * Converted items
     */
    @Internal
    public transient List<GraphTargetItem> convertedItems;

    /**
     * Convert exception
     */
    @Internal
    public transient Throwable convertException;

    /**
     * ABC file
     */
    @Internal
    private ABC abc;

    /**
     * DependencyParser uses this
     */
    @Internal
    private transient MethodBody lastConvertedBody = null;

    /**
     * Constructs a new MethodBody with empty traits, code bytes and exceptions.
     */
    public MethodBody() {
        this.traits = new Traits();
        this.codeBytes = SWFInputStream.BYTE_ARRAY_EMPTY;
        this.exceptions = new ABCException[0];
        this.abc = null;
    }

    /**
     * Sets the ABC file.
     *
     * @param abc ABC file
     */
    public void setAbc(ABC abc) {
        this.abc = abc;
    }

    /**
     * Constructs a new MethodBody with given ABC file, traits, code bytes and
     * exceptions.
     *
     * @param abc ABC file
     * @param traits Traits
     * @param codeBytes Code bytes
     * @param exceptions Exceptions
     */
    public MethodBody(ABC abc, Traits traits, byte[] codeBytes, ABCException[] exceptions) {
        this.traits = traits;
        this.codeBytes = codeBytes;
        this.exceptions = exceptions;
        this.abc = abc;
    }

    /**
     * Sets the code bytes.
     *
     * @param codeBytes Code bytes
     */
    public synchronized void setCodeBytes(byte[] codeBytes) {
        this.codeBytes = codeBytes;
        this.code = null;
    }

    /**
     * Sets the modified flag.
     */
    public void setModified() {
        this.codeBytes = null;
    }

    /**
     * Gets the code bytes.
     *
     * @return Code bytes
     */
    public synchronized byte[] getCodeBytes() {
        if (codeBytes != null) {
            return codeBytes;
        } else {
            return code.getBytes();
        }
    }

    /**
     * Gets the AVM2 code.
     *
     * @return AVM2 code
     */
    public synchronized AVM2Code getCode() {
        if (code == null) {
            AVM2Code avm2Code;
            try {
                ABCInputStream ais = new ABCInputStream(new MemoryInputStream(codeBytes));
                avm2Code = new AVM2Code(ais, this);
                if (abc != null) {
                    avm2Code.removeWrongIndices(abc.constants);
                }
            } catch (UnknownInstructionCodeException | IOException ex) {
                avm2Code = new AVM2Code();
                logger.log(Level.SEVERE, null, ex);
            }
            avm2Code.compact();
            code = avm2Code;
        }
        return code;
    }

    /**
     * Sets the AVM2 code.
     *
     * @param code AVM2 code
     */
    public void setCode(AVM2Code code) {
        this.code = code;
        this.codeBytes = null;
    }

    /**
     * Marks offsets.
     */
    public void markOffsets() {
        getCode().markOffsets();
    }

    /**
     * Removes dead code.
     *
     * @param constants Constant pool
     * @param trait Trait
     * @param info Method info
     * @return Number of removed instructions
     * @throws InterruptedException On interrupt
     */
    public int removeDeadCode(AVM2ConstantPool constants, Trait trait, MethodInfo info) throws InterruptedException {
        return getCode().removeDeadCode(this);
    }

    /**
     * Removes traps - deobfuscation.
     *
     * @param abc ABC file
     * @param trait Trait
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param isStatic Is static
     * @param path Path
     * @return Number of removed instructions
     * @throws InterruptedException On interrupt
     */
    public int removeTraps(ABC abc, Trait trait, int scriptIndex, int classIndex, boolean isStatic, String path) throws InterruptedException {

        return getCode().removeTraps(trait, method_info, this, abc, scriptIndex, classIndex, isStatic, path);
    }

    /**
     * Deobfuscates the method body.
     *
     * @param level Deobfuscation level
     * @param trait Trait
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param isStatic Is static
     * @param path Path
     * @throws InterruptedException On interrupt
     */
    public void deobfuscate(DeobfuscationLevel level, Trait trait, int scriptIndex, int classIndex, boolean isStatic, String path) throws InterruptedException {
        if (level == DeobfuscationLevel.LEVEL_REMOVE_DEAD_CODE) {
            removeDeadCode(abc.constants, trait, abc.method_info.get(method_info));
        } else if (level == DeobfuscationLevel.LEVEL_REMOVE_TRAPS) {
            removeTraps(abc, trait, scriptIndex, classIndex, isStatic, path);
        }

        ((Tag) abc.parentTag).setModified(true);
    }

    /**
     * Removes instruction.
     *
     * @param pos Position
     */
    public void removeInstruction(int pos) {
        getCode().removeInstruction(pos, this);
    }

    /**
     * Replaces instruction by another. Properly handles offsets. Note: If
     * newinstruction is jump, the offset operand must be handled properly by
     * caller.
     *
     * @param pos Position
     * @param instruction Instruction
     */
    public void replaceInstruction(int pos, AVM2Instruction instruction) {
        getCode().replaceInstruction(pos, instruction, this);
    }

    /**
     * Inserts all instructions at specified point. Handles offsets properly.
     *
     * @param pos Position in the list
     * @param list List of instructions
     */
    public void insertAll(int pos, List<AVM2Instruction> list) {
        for (AVM2Instruction ins : list) {
            insertInstruction(pos++, ins);
        }
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

    /**
     * Get number of local registers reserved for this method.
     *
     * @return Number of local registers reserved for this method
     */
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

    /**
     * Get local register names.
     *
     * @param abc ABC file
     * @return Local register names
     */
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
            Map<Integer, String> debugRegNames = getCode().getLocalRegNamesFromDebug(abc, max_regs);
            for (int k : debugRegNames.keySet()) {
                ret.put(k, debugRegNames.get(k));
            }
        }
        return ret;
    }

    /**
     * Converts the method body.
     *
     * @param swfVersion SWF version
     * @param callStack Call stack
     * @param abcIndex ABC indexing
     * @param convertData Convert data
     * @param path Path
     * @param exportMode Export mode
     * @param isStatic Is static
     * @param methodIndex Method index
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param abc ABC file
     * @param trait Trait
     * @param scopeStack Scope stack
     * @param initializerType Initializer type
     * @param writer Writer
     * @param fullyQualifiedNames Fully qualified names
     * @param initTraits Initial traits
     * @param firstLevel First level
     * @param seenMethods Seen methods
     * @param initTraitClasses Class ids which traits to init
     * @throws InterruptedException On interrupt
     */
    public void convert(int swfVersion, List<MethodBody> callStack, AbcIndexing abcIndex, final ConvertData convertData, final String path, ScriptExportMode exportMode, final boolean isStatic, final int methodIndex, final int scriptIndex, final int classIndex, final ABC abc, final Trait trait, final ScopeStack scopeStack, final int initializerType, final NulWriter writer, final List<DottedChain> fullyQualifiedNames, Traits initTraits, boolean firstLevel, Set<Integer> seenMethods, List<Integer> initTraitClasses) throws InterruptedException {
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
                                convertedItems1 = converted.getCode().toGraphTargetItems(swfVersion, callStack, abcIndex, convertData.thisHasDefaultToPrimitive, convertData, path, methodIndex, isStatic, scriptIndex, classIndex, abc, converted, localRegNames, scopeStack, initializerType, fullyQualifiedNames, initTraits, 0, new HashMap<>(), initTraitClasses); //converted.getCode().visitCode(converted)
                            }
                            try (Statistics s = new Statistics("Graph.graphToString")) {
                                Graph.graphToString(convertedItems1, writer, LocalData.create(callStack, abcIndex, abc, localRegNames, fullyQualifiedNames, seenMethods, exportMode, swfVersion));
                            }
                            convertedItems = convertedItems1;
                        }
                        return null;
                    }
                };
                if (firstLevel) {
                    CancellableWorker.call("script.methodbody.convert", callable, timeout, TimeUnit.SECONDS);
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

    /**
     * Returns a string representation of this MethodBody.
     *
     * @return String representation of this MethodBody
     */
    @Override
    public String toString() {
        String s = "";
        s += "method_info=" + method_info + " max_stack=" + max_stack + " max_regs=" + max_regs + " scope_depth=" + init_scope_depth + " max_scope=" + max_scope_depth;
        s += "\r\nCode:\r\n" + getCode().toString();
        return s;
    }

    /**
     * Returns a string representation of this MethodBody.
     *
     * @param swfVersion SWF version
     * @param callStack Call stack
     * @param abcIndex ABC indexing
     * @param path Path
     * @param exportMode Export mode
     * @param abc ABC file
     * @param trait Trait
     * @param writer Writer
     * @param fullyQualifiedNames Fully qualified names
     * @param seenMethods Seen methods
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter toString(int swfVersion, List<MethodBody> callStack, AbcIndexing abcIndex, final String path, ScriptExportMode exportMode, final ABC abc, final Trait trait, final GraphTextWriter writer, final List<DottedChain> fullyQualifiedNames, Set<Integer> seenMethods) throws InterruptedException {
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
                    List<DottedChain> fullyQualifiedNames2 = new ArrayList<>(fullyQualifiedNames);
                    for (Trait t : traits.traits) {
                        DottedChain tname = DottedChain.parseWithSuffix(t.getName(abc).getName(abc.constants, new ArrayList<>(), false, true));
                        fullyQualifiedNames2.remove(tname);
                    }

                    Graph.graphToString(convertedItems, writer, LocalData.create(callStack, abcIndex, abc, localRegNames, fullyQualifiedNames2, seenMethods, exportMode, swfVersion));
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

    /**
     * Converts the method body. Can use previously converted method body.
     *
     * @param deobfuscate Deobfuscate
     * @param path Path
     * @param isStatic Is static
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param abc ABC file
     * @param trait Trait
     * @return Method body
     * @throws InterruptedException On interrupt
     */
    public MethodBody convertMethodBodyCanUseLast(boolean deobfuscate, String path, boolean isStatic, int scriptIndex, int classIndex, ABC abc, Trait trait) throws InterruptedException {
        if (lastConvertedBody != null) {
            return lastConvertedBody;
        }
        return convertMethodBody(deobfuscate, path, isStatic, scriptIndex, classIndex, abc, trait);
    }

    /**
     * Clears the last converted method body.
     */
    public void clearLastConverted() {
        this.lastConvertedBody = null;
    }

    /**
     * Converts the method body.
     *
     * @param deobfuscate Deobfuscate
     * @param path Path
     * @param isStatic Is static
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param abc ABC file
     * @param trait Trait
     * @return Method body
     * @throws InterruptedException On interrupt
     */
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

    /**
     * Converts the method body to high-level source code.
     *
     * @param swfVersion SWF version
     * @param callStack Call stack
     * @param abcIndex ABC indexing
     * @param scriptIndex Script index
     * @param seenMethods Seen methods
     * @return High-level source code
     */
    public String toSource(int swfVersion, List<MethodBody> callStack, AbcIndexing abcIndex, int scriptIndex, Set<Integer> seenMethods) {
        ConvertData convertData = new ConvertData();
        convertData.deobfuscationMode = 0;
        try {
            convert(swfVersion, callStack, abcIndex, convertData, "", ScriptExportMode.AS, false, method_info, 0, 0, abc, null, new ScopeStack(), 0, new NulWriter(), new ArrayList<>(), new Traits(), true, seenMethods, new ArrayList<>());
            HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
            writer.indent().indent().indent();
            toString(swfVersion, callStack, abcIndex, "", ScriptExportMode.AS, abc, null, writer, new ArrayList<>(), seenMethods);
            writer.unindent().unindent().unindent();
            writer.finishHilights();
            return writer.toString();
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * Clones this MethodBody.
     *
     * @return Cloned MethodBody
     */
    @Override
    public MethodBody clone() {
        return clone(false);
    }

    /**
     * Clones this MethodBody.
     *
     * @param deepTraits Deep traits
     * @return Cloned MethodBody
     */
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

    /**
     * Auto fills the statistics (max stack, max scope depth, ...).
     *
     * @param abc ABC file
     * @param initScope Initial scope
     * @param hasThis Has this
     * @return True if successful
     */
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

    /**
     * Auto fills the maximum number of registers.
     *
     * @param abc ABC file
     * @return True if successful
     */
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
