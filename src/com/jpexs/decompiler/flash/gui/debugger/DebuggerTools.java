/*
 *  Copyright (C) 2010-2018 JPEXS
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
package com.jpexs.decompiler.flash.gui.debugger;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.DebugLogDialog;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author JPEXS
 */
public class DebuggerTools {

    private static final Logger logger = Logger.getLogger(DebuggerTools.class.getName());

    public static final String DEBUGGER_PACKAGE = "com.jpexs.decompiler.flash.debugger";

    private static volatile Debugger debugger;

    private static ScriptPack getDebuggerScriptPack(SWF swf) {
        List<ABC> allAbcList = new ArrayList<>();
        for (ABCContainerTag ac : swf.getAbcList()) {
            allAbcList.add(ac.getABC());
        }
        for (ABCContainerTag ac : swf.getAbcList()) {
            ABC a = ac.getABC();
            for (ScriptPack m : a.getScriptPacks(DEBUGGER_PACKAGE, allAbcList)) {
                if (isDebuggerClass(m.getClassPath().packageStr.toRawString(), null)) {
                    return m;
                }
            }
        }
        return null;
    }

    public static boolean hasDebugger(SWF swf) {
        return getDebuggerScriptPack(swf) != null;
    }

    private static boolean isDebuggerClass(String tested, String cls) {
        if (tested == null) {
            return false;
        }

        // fast check, because dynamic regex compile and match is expensive
        if (!tested.startsWith(DEBUGGER_PACKAGE)) {
            return false;
        }

        if (cls == null) {
            cls = "";
        } else {
            cls = "\\." + Pattern.quote(cls);
        }

        return tested.matches(Pattern.quote(DEBUGGER_PACKAGE) + "(\\.pkg[a-f0-9]+)?" + cls);
    }

    public static void injectDebugLoader(SWF swf) {
        if (hasDebugger(swf)) {
            ScriptPack dsp = getDebuggerScriptPack(swf);
            String debuggerPkg = dsp.getClassPath().packageStr.toRawString();
            for (ABCContainerTag ct : swf.getAbcList()) {
                ABC a = ct.getABC();
                if (dsp.abc == a) { //do not replace Loader in debugger itself
                    continue;
                }
                for (int i = 1; i < a.constants.getMultinameCount(); i++) {
                    Multiname m = a.constants.getMultiname(i);
                    if ("flash.display.Loader".equals(m.getNameWithNamespace(a.constants, true).toRawString())) {
                        m.namespace_index = a.constants.getNamespaceId(Namespace.KIND_PACKAGE, debuggerPkg, 0, true);
                        m.name_index = a.constants.getStringId("DebugLoader", true);
                        ((Tag) ct).setModified(true);
                    } else if ("flash.utils.getDefinitionByName".equals(m.getNameWithNamespace(a.constants, true).toRawString())) {
                        m.namespace_index = a.constants.getNamespaceId(Namespace.KIND_PACKAGE, debuggerPkg, 0, true);
                        m.name_index = a.constants.getStringId("debugGetDefinitionByName", true);
                        ((Tag) ct).setModified(true);
                    } else if ("flash.utils.getQualifiedClassName".equals(m.getNameWithNamespace(a.constants, true).toRawString())) {
                        m.namespace_index = a.constants.getNamespaceId(Namespace.KIND_PACKAGE, debuggerPkg, 0, true);
                        m.name_index = a.constants.getStringId("debugGetQualifiedClassName", true);
                        ((Tag) ct).setModified(true);
                    } else if ("flash.utils.getQualifiedSuperclassName".equals(m.getNameWithNamespace(a.constants, true).toRawString())) {
                        m.namespace_index = a.constants.getNamespaceId(Namespace.KIND_PACKAGE, debuggerPkg, 0, true);
                        m.name_index = a.constants.getStringId("debugGetQualifiedSuperclassName", true);
                        ((Tag) ct).setModified(true);
                    } else if ("flash.utils.describeType".equals(m.getNameWithNamespace(a.constants, true).toRawString())) {
                        m.namespace_index = a.constants.getNamespaceId(Namespace.KIND_PACKAGE, debuggerPkg, 0, true);
                        m.name_index = a.constants.getStringId("debugDescribeType", true);
                        ((Tag) ct).setModified(true);
                    }
                }
            }
        }
    }

    public static void replaceTraceCalls(SWF swf, String fname) {
        if (hasDebugger(swf)) {
            String debuggerPkg = getDebuggerScriptPack(swf).getClassPath().packageStr.toRawString();
            //change trace to fname
            for (ABCContainerTag ct : swf.getAbcList()) {
                ABC a = ct.getABC();
                for (int i = 1; i < a.constants.getMultinameCount(); i++) {
                    Multiname m = a.constants.getMultiname(i);
                    if ("trace".equals(m.getNameWithNamespace(a.constants, true).toRawString())) {
                        m.namespace_index = a.constants.getNamespaceId(Namespace.KIND_PACKAGE, debuggerPkg, 0, true);
                        m.name_index = a.constants.getStringId(fname, true);
                        ((Tag) ct).setModified(true);
                    }
                }
            }
        }
    }

    public static void switchDebugger(SWF swf) {
        int port = Configuration.debuggerPort.get();
        ScriptPack found = getDebuggerScriptPack(swf);
        if (found != null) {
            ABCContainerTag tag = found.abc.parentTag;
            swf.removeTag((Tag) tag);
            swf.getAbcList().remove(tag);

            //Change all debugger calls to normal trace / Loader
            for (ABCContainerTag ct : swf.getAbcList()) {
                ABC a = ct.getABC();
                for (int i = 1; i < a.constants.getMultinameCount(); i++) {
                    Multiname m = a.constants.getMultiname(i);
                    String packageStr = m.getNameWithNamespace(a.constants, true).toString();
                    if (isDebuggerClass(packageStr, "debugTrace")
                            || isDebuggerClass(packageStr, "debugAlert")
                            || isDebuggerClass(packageStr, "debugSocket")
                            || isDebuggerClass(packageStr, "debugConsole")) {
                        m.name_index = a.constants.getStringId("trace", true);
                        m.namespace_index = a.constants.getNamespaceId(Namespace.KIND_PACKAGE, "", 0, true);
                        ((Tag) ct).setModified(true);
                    } else if (isDebuggerClass(packageStr, "DebugLoader")) {
                        m.name_index = a.constants.getStringId("Loader", true);
                        m.namespace_index = a.constants.getNamespaceId(Namespace.KIND_PACKAGE, "flash.display", 0, true);
                    }
                }
            }
        } else {
            Random rnd = new Random();
            byte[] rb = new byte[16];
            rnd.nextBytes(rb);
            String rhex = Helper.byteArrayToHex(rb);
            try {
                //load debug swf
                SWF debugSWF = new SWF(Main.class.getClassLoader().getResourceAsStream("com/jpexs/decompiler/flash/gui/debugger/debug.swf"), false);

                List<ABCContainerTag> al = swf.getAbcList();
                ABCContainerTag firstAbc = al.isEmpty() ? null : al.get(0);
                if (firstAbc == null) { //nothing to instrument?
                    return;
                }
                String newdebuggerpkg = DEBUGGER_PACKAGE;

                if (Configuration.randomDebuggerPackage.get()) {
                    newdebuggerpkg += ".pkg" + rhex;
                }

                //add debug ABC tags to main SWF
                for (ABCContainerTag ds : debugSWF.getAbcList()) {
                    ABC a = ds.getABC();
                    //Append random hex to Debugger package name
                    for (int i = 1; i < a.constants.getNamespaceCount(); i++) {
                        if (a.constants.getNamespace(i).hasName(DEBUGGER_PACKAGE, a.constants)) {
                            a.constants.getNamespace(i).name_index = a.constants.getStringId(newdebuggerpkg, true);
                        }
                    }
                    //Set debugger port to actually set port
                    for (int i = 0; i < a.constants.getIntCount(); i++) {
                        if (a.constants.getInt(i) == 123456L) {
                            a.constants.setInt(i, (long) port);
                        }
                    }
                    //Add to target SWF
                    ((Tag) ds).setSwf(swf);
                    swf.addTag((Tag) ds, (Tag) firstAbc);
                    swf.getAbcList().add(swf.getAbcList().indexOf(firstAbc), ds);
                    ((Tag) ds).setModified(true);

                    //To allow socket connection to FFDec. Is this safe?
                    FileAttributesTag ft = swf.getFileAttributes();
                    ft.useNetwork = true;
                    ft.setModified(true);
                }

            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error while attaching debugger", ex);
                //ignore
            }

        }
        initDebugger();
    }

    public static Debugger initDebugger() {
        if (debugger == null) {
            synchronized (Main.class) {
                if (debugger == null) {
                    Debugger dbg = new Debugger(Configuration.debuggerPort.get());
                    dbg.start();
                    debugger = dbg;
                }
            }
        }
        return debugger;
    }

    public static void debuggerShowLog() {
        initDebugger();
        if (Main.debugDialog == null) {
            Main.debugDialog = new DebugLogDialog(debugger);
        }
        Main.debugDialog.setVisible(true);
    }
}
