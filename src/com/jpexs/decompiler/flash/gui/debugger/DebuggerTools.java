/*
 *  Copyright (C) 2010-2015 JPEXS
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
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.helpers.Helper;
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

    private static Debugger debugger;

    private static ScriptPack getDebuggerScriptPack(SWF swf) {
        for (ABCContainerTag ac : swf.getAbcList()) {
            ABC a = ac.getABC();
            for (ScriptPack m : a.getScriptPacks(DEBUGGER_PACKAGE)) {
                if (isDebuggerClass(m.getClassPath().packageStr, null)) {
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

    public static void replaceTraceCalls(SWF swf, String fname) {
        if (hasDebugger(swf)) {
            String debuggerPkg = getDebuggerScriptPack(swf).getClassPath().packageStr;
            //change trace to fname
            for (ABCContainerTag ct : swf.getAbcList()) {
                ABC a = ct.getABC();
                for (int i = 1; i < a.constants.constant_multiname.size(); i++) {
                    Multiname m = a.constants.constant_multiname.get(i);
                    if ("trace".equals(m.getNameWithNamespace(a.constants, true))) {
                        m.namespace_index = a.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE, a.constants.getStringId(debuggerPkg, true)), 0, true);
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
            swf.tags.remove((Tag) tag);
            swf.getAbcList().remove(tag);

            //Change all debugger calls to normal trace
            for (ABCContainerTag ct : swf.getAbcList()) {
                ABC a = ct.getABC();
                for (int i = 1; i < a.constants.constant_multiname.size(); i++) {
                    Multiname m = a.constants.constant_multiname.get(i);
                    String packageStr = m.getNameWithNamespace(a.constants, true);
                    if (isDebuggerClass(packageStr, "debugTrace")
                            || isDebuggerClass(packageStr, "debugAlert")
                            || isDebuggerClass(packageStr, "debugSocket")
                            || isDebuggerClass(packageStr, "debugConsole")) {
                        m.name_index = a.constants.getStringId("trace", true);
                        m.namespace_index = a.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE, a.constants.getStringId("", true)), 0, true);
                        ((Tag) ct).setModified(true);
                    }
                }
            }
        } else {
            Random rnd = new Random();
            byte rb[] = new byte[16];
            rnd.nextBytes(rb);
            String rhex = Helper.byteArrayToHex(rb);
            try {
                //load debug swf
                SWF debugSWF = new SWF(Main.class.getClassLoader().getResourceAsStream("com/jpexs/decompiler/flash/gui/debugger/debug.swf"), false);

                ABCContainerTag firstAbc = swf.getAbcList().get(0);
                String newdebuggerpkg = DEBUGGER_PACKAGE;

                if (Configuration.randomDebuggerPackage.get()) {
                    newdebuggerpkg += ".pkg" + rhex;
                }

                //add debug ABC tags to main SWF
                for (ABCContainerTag ds : debugSWF.getAbcList()) {
                    ABC a = ds.getABC();
                    //Append random hex to Debugger package name
                    for (int i = 1; i < a.constants.constant_namespace.size(); i++) {
                        if (a.constants.constant_namespace.get(i).hasName(DEBUGGER_PACKAGE, a.constants)) {
                            a.constants.constant_namespace.get(i).name_index = a.constants.getStringId(newdebuggerpkg, true);
                        }
                    }
                    //Set debugger port to actually set port
                    for (int i = 0; i < a.constants.constant_int.size(); i++) {
                        if (a.constants.constant_int.get(i) == 123456L) {
                            a.constants.constant_int.set(i, (long) port);
                        }
                    }
                    //Add to target SWF
                    ((Tag) ds).setSwf(swf);
                    swf.tags.add(swf.tags.indexOf(firstAbc), (Tag) ds);
                    swf.getAbcList().add(swf.getAbcList().indexOf(firstAbc), ds);
                    ((Tag) ds).setModified(true);
                }

            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error while attaching debugger", ex);
                //ignore
            }

        }
        initDebugger();
    }

    private static void initDebugger() {
        if (debugger == null) {
            synchronized (Main.class) {
                if (debugger == null) {
                    Debugger dbg = new Debugger(Configuration.debuggerPort.get());
                    dbg.start();
                    debugger = dbg;
                }
            }
        }
    }

    public static void debuggerShowLog() {
        initDebugger();
        if (Main.debugDialog == null) {
            Main.debugDialog = new DebugLogDialog(debugger);
        }
        Main.debugDialog.setVisible(true);
    }
}
