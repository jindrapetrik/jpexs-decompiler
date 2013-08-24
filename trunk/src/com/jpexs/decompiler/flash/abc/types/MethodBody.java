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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.Configuration;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.CodeStats;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.helpers.Highlighting;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.helpers.Helper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MethodBody implements Cloneable, Serializable {

    boolean debugMode = false;
    public int method_info;
    public int max_stack;
    public int max_regs;
    public int init_scope_depth;
    public int max_scope_depth;
    public byte codeBytes[];
    public AVM2Code code;
    public ABCException exceptions[] = new ABCException[0];
    public Traits traits = new Traits();

    public List<Integer> getExceptionEntries() {
        List<Integer> ret = new ArrayList<>();
        for (ABCException e : exceptions) {
            ret.add(code.adr2pos(e.start));
            ret.add(code.adr2pos(e.end));
            ret.add(code.adr2pos(e.target));
        }
        return ret;
    }

    @Override
    public String toString() {
        String s = "";
        s += "method_info=" + method_info + " max_stack=" + max_stack + " max_regs=" + max_regs + " scope_depth=" + init_scope_depth + " max_scope=" + max_scope_depth;
        s += "\r\nCode:\r\n" + code.toString();
        return s;
    }

    public int removeDeadCode(ConstantPool constants) {
        return code.removeDeadCode(constants, this);
    }

    public void restoreControlFlow(ConstantPool constants) {
        code.restoreControlFlow(constants, this);
    }

    public int removeTraps(ConstantPool constants, ABC abc, int scriptIndex, int classIndex, boolean isStatic, String path) {
        return code.removeTraps(constants, this, abc, scriptIndex, classIndex, isStatic, path);
    }

    public HashMap<Integer, String> getLocalRegNames(ABC abc) {
        HashMap<Integer, String> ret = new HashMap<>();
        for (int i = 1; i <= abc.method_info[this.method_info].param_types.length; i++) {
            String paramName = "param" + i;
            if (abc.method_info[this.method_info].flagHas_paramnames() && Configuration.PARAM_NAMES_ENABLE) {
                paramName = abc.constants.constant_string[abc.method_info[this.method_info].paramNames[i - 1]];
            }
            ret.put(i, paramName);
        }
        int pos = abc.method_info[this.method_info].param_types.length + 1;
        if (abc.method_info[this.method_info].flagNeed_arguments()) {
            ret.put(pos, "arguments");
            pos++;
        }
        if (abc.method_info[this.method_info].flagNeed_rest()) {
            ret.put(pos, "rest");
            pos++;
        }

        HashMap<Integer, String> debugRegNames = code.getLocalRegNamesFromDebug(abc);
        for (int k : debugRegNames.keySet()) {
            ret.put(k, debugRegNames.get(k));
        }
        return ret;
    }

    public String toString(final String path, boolean pcode, final boolean isStatic, final int scriptIndex, final int classIndex, final ABC abc, final ConstantPool constants, final MethodInfo method_info[], final Stack<GraphTargetItem> scopeStack, final boolean isStaticInitializer, final boolean hilight, final List<String> fullyQualifiedNames, final Traits initTraits) {
        if (debugMode) {
            System.err.println("Decompiling " + path);
        }
        String s = "";
        if (pcode) {
            s += code.toASMSource(constants, this, false);
        } else {
            if (!(Boolean) Configuration.getConfig("decompile", Boolean.TRUE)) {
                s = "//Decompilation skipped";
                if (hilight) {
                    s = Highlighting.hilighMethod(s, this.method_info);
                }
                return s;
            }
            try {
                s += Helper.timedCall(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return toSource(path, isStatic, scriptIndex, classIndex, abc, constants, method_info, scopeStack, isStaticInitializer, hilight, fullyQualifiedNames, initTraits);
                    }
                }, Configuration.DECOMPILATION_TIMEOUT_SINGLE_METHOD, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                Logger.getLogger(Action.class.getName()).log(Level.SEVERE, "Decompilation error", ex);
                s += "/*\r\n * Decompilation error\r\n * Timeout (" + Helper.formatTimeToText(Configuration.DECOMPILATION_TIMEOUT_SINGLE_METHOD) + ") was reached\r\n */";
            }
        }
        return s;
    }

    public String toSource(String path, boolean isStatic, int scriptIndex, int classIndex, ABC abc, ConstantPool constants, MethodInfo method_info[], Stack<GraphTargetItem> scopeStack, boolean isStaticInitializer, boolean hilight, List<String> fullyQualifiedNames, Traits initTraits) {
        AVM2Code deobfuscated = null;
        MethodBody b = (MethodBody) Helper.deepCopy(this);
        deobfuscated = b.code;
        deobfuscated.markMappedOffsets();
        if ((Boolean) Configuration.getConfig("autoDeobfuscate", true)) {
            try {
                deobfuscated.removeTraps(constants, b, abc, scriptIndex, classIndex, isStatic, path);
            } catch (Exception | StackOverflowError ex) {
                Logger.getLogger(MethodBody.class.getName()).log(Level.SEVERE, "Error during remove traps in " + path, ex);
            }
        }
        //deobfuscated.restoreControlFlow(constants, b);
        //try {
        String s = deobfuscated.toSource(path, isStatic, scriptIndex, classIndex, abc, constants, method_info, b, hilight, getLocalRegNames(abc), scopeStack, isStaticInitializer, fullyQualifiedNames, initTraits, Graph.SOP_USE_STATIC, new HashMap<Integer, Integer>(), deobfuscated.visitCode(b));
        s = s.trim();
        if (s.equals("")) {
            s = " ";
        }
        if (hilight) {
            s = Highlighting.hilighMethod(s, this.method_info);
        }

        return s;
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

    public boolean autoFillStats(ABC abc) {
        CodeStats stats = code.getStats(abc, this);
        if (stats == null) {
            return false;
        }
        max_stack = stats.maxstack;
        max_scope_depth = init_scope_depth + stats.maxscope;
        max_regs = stats.maxlocal;
        abc.method_info[method_info].setFlagSetsdxns(stats.has_set_dxns);
        abc.method_info[method_info].setFlagNeed_activation(stats.has_activation);
        return true;
    }
}
