/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.AppResources;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ABCInputStream;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.CodeStats;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.UnknownInstructionCode;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.MemoryInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MethodBody implements Cloneable, Serializable {

    public boolean deleted;
    boolean debugMode = false;
    public int method_info;
    public int max_stack;
    public int max_regs;
    public int init_scope_depth;
    public int max_scope_depth;
    public byte[] codeBytes;
    private AVM2Code code;
    public ABCException[] exceptions = new ABCException[0];
    public Traits traits = new Traits();
    public transient List<GraphTargetItem> convertedItems;
    public transient Throwable convertException;

    public synchronized AVM2Code getCode() {
        if (code == null) {
            AVM2Code avm2Code;
            try {
                ABCInputStream ais = new ABCInputStream(new MemoryInputStream(codeBytes));
                avm2Code = new AVM2Code(ais);
            } catch (UnknownInstructionCode | IOException ex) {
                avm2Code = new AVM2Code();
                Logger.getLogger(MethodBody.class.getName()).log(Level.SEVERE, null, ex);
            }
            avm2Code.compact();
            code = avm2Code;
        }
        return code;
    }
    
    public void setCode(AVM2Code code) {
        this.code = code;
    }
    
    public List<Integer> getExceptionEntries() {
        List<Integer> ret = new ArrayList<>();
        for (ABCException e : exceptions) {
            ret.add(getCode().adr2pos(e.start));
            ret.add(getCode().adr2pos(e.end));
            ret.add(getCode().adr2pos(e.target));
        }
        return ret;
    }

    public void markOffsets() {
        long offset = 0;
        for (int i = 0; i < getCode().code.size(); i++) {
            getCode().code.get(i).offset = offset;
            offset += getCode().code.get(i).getBytes().length;
        }
    }

    @Override
    public String toString() {
        String s = "";
        s += "method_info=" + method_info + " max_stack=" + max_stack + " max_regs=" + max_regs + " scope_depth=" + init_scope_depth + " max_scope=" + max_scope_depth;
        s += "\r\nCode:\r\n" + getCode().toString();
        return s;
    }

    public int removeDeadCode(ConstantPool constants, Trait trait, MethodInfo info) throws InterruptedException {
        return getCode().removeDeadCode(constants, trait, info, this);
    }

    public void restoreControlFlow(ConstantPool constants, Trait trait, MethodInfo info) throws InterruptedException {
        getCode().restoreControlFlow(constants, trait, info, this);
    }

    public int removeTraps(ConstantPool constants, ABC abc, Trait trait, int scriptIndex, int classIndex, boolean isStatic, String path) throws InterruptedException {
        return getCode().removeTraps(constants, trait, abc.method_info.get(method_info), this, abc, scriptIndex, classIndex, isStatic, path);
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

        HashMap<Integer, String> debugRegNames = getCode().getLocalRegNamesFromDebug(abc);
        for (int k : debugRegNames.keySet()) {
            ret.put(k, debugRegNames.get(k));
        }
        return ret;
    }

    public void convert(final String path, ScriptExportMode exportMode, final boolean isStatic, final int scriptIndex, final int classIndex, final ABC abc, final Trait trait, final ConstantPool constants, final List<MethodInfo> method_info, final ScopeStack scopeStack, final boolean isStaticInitializer, final GraphTextWriter writer, final List<String> fullyQualifiedNames, final Traits initTraits, boolean firstLevel) throws InterruptedException {
        if (debugMode) {
            System.err.println("Decompiling " + path);
        }
        if (exportMode != ScriptExportMode.AS) {
            getCode().toASMSource(constants, trait, method_info.get(this.method_info), this, exportMode, writer);
        } else {
            if (!Configuration.decompile.get()) {
                writer.appendNoHilight("//" + AppResources.translate("decompilation.skipped")).newLine();
                return;
            }
            int timeout = Configuration.decompilationTimeoutSingleMethod.get();
            try {
                Callable<Void> callable = new Callable<Void>() {
                    @Override
                    public Void call() throws InterruptedException {
                        MethodBody converted = convertMethodBody(path, isStatic, scriptIndex, classIndex, abc, trait, constants, method_info, scopeStack, isStaticInitializer, fullyQualifiedNames, initTraits);
                        HashMap<Integer, String> localRegNames = getLocalRegNames(abc);
                        convertedItems = converted.getCode().toGraphTargetItems(path, isStatic, scriptIndex, classIndex, abc, constants, method_info, converted, localRegNames, scopeStack, isStaticInitializer, fullyQualifiedNames, initTraits, Graph.SOP_USE_STATIC, new HashMap<Integer, Integer>(), converted.getCode().visitCode(converted));
                        Graph.graphToString(convertedItems, writer, LocalData.create(constants, localRegNames, fullyQualifiedNames));
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
                Logger.getLogger(MethodBody.class.getName()).log(Level.SEVERE, "Decompilation error", ex);
                convertException = ex;
                Throwable cause = ex.getCause();
                if (ex instanceof ExecutionException && cause instanceof Exception) {
                    convertException = (Exception) cause;
                }
            }
        }
    }

    public GraphTextWriter toString(final String path, ScriptExportMode exportMode, final ABC abc, final Trait trait, final ConstantPool constants, final List<MethodInfo> method_info, final GraphTextWriter writer, final List<String> fullyQualifiedNames) throws InterruptedException {
        if (exportMode != ScriptExportMode.AS) {
            getCode().toASMSource(constants, trait, method_info.get(this.method_info), this, exportMode, writer);
        } else {
            if (!Configuration.decompile.get()) {
                writer.startMethod(this.method_info);
                writer.appendNoHilight("//" + AppResources.translate("decompilation.skipped")).newLine();
                writer.endMethod();
                return writer;
            }
            int timeout = Configuration.decompilationTimeoutSingleMethod.get();

            if (convertException == null) {
                HashMap<Integer, String> localRegNames = getLocalRegNames(abc);
                writer.startMethod(this.method_info);
                Graph.graphToString(convertedItems, writer, LocalData.create(constants, localRegNames, fullyQualifiedNames));
                writer.endMethod();
            } else if (convertException instanceof TimeoutException) {
                Logger.getLogger(MethodBody.class.getName()).log(Level.SEVERE, "Decompilation error", convertException);
                Helper.appendTimeoutComment(writer, timeout);
            } else {
                Logger.getLogger(MethodBody.class.getName()).log(Level.SEVERE, "Decompilation error", convertException);
                Helper.appendErrorComment(writer, convertException);
            }
        }
        return writer;
    }

    public MethodBody convertMethodBody(String path, boolean isStatic, int scriptIndex, int classIndex, ABC abc, Trait trait, ConstantPool constants, List<MethodInfo> method_info, ScopeStack scopeStack, boolean isStaticInitializer, List<String> fullyQualifiedNames, Traits initTraits) throws InterruptedException {
        MethodBody b = Helper.deepCopy(this);
        AVM2Code deobfuscated = b.getCode();
        deobfuscated.markMappedOffsets();
        if (Configuration.autoDeobfuscate.get()) {
            try {
                deobfuscated.removeTraps(constants, trait, method_info.get(this.method_info), b, abc, scriptIndex, classIndex, isStatic, path);
            } catch (StackOverflowError ex) {
                Logger.getLogger(MethodBody.class.getName()).log(Level.SEVERE, "Error during remove traps in " + path, ex);
            }
        }
        //deobfuscated.restoreControlFlow(constants, b);

        return b;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        MethodBody ret = new MethodBody();
        ret.code = code;
        ret.codeBytes = codeBytes;
        ret.exceptions = exceptions;
        ret.max_regs = max_regs;
        ret.max_scope_depth = max_scope_depth;
        ret.max_stack = max_stack;
        ret.method_info = method_info;
        ret.init_scope_depth = init_scope_depth;
        ret.traits = traits; //maybe deep clone
        return ret;
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
        int min_regs = mi.param_types.length + (hasThis ? 1 : 0) + (mi.flagNeed_rest() ? 1 : 0);
        if (max_regs < min_regs) {
            max_regs = min_regs;
        }
        return true;
    }
}
