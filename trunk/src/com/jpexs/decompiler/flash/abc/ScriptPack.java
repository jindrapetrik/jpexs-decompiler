/*
 *  Copyright (C) 2012-2013 JPEXS
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
package com.jpexs.decompiler.flash.abc;

import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.helpers.Helper;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ScriptPack {

    public ABC abc;
    public int scriptIndex;
    public List<Integer> traitIndices;

    public ScriptPack(ABC abc, int scriptIndex, List<Integer> traitIndices) {
        this.abc = abc;
        this.scriptIndex = scriptIndex;
        this.traitIndices = traitIndices;
    }

    public String getPathPackage() {
        String packageName = "";
        for (int t : traitIndices) {
            Multiname name = abc.script_info[scriptIndex].traits.traits[t].getName(abc);
            Namespace ns = name.getNamespace(abc.constants);
            if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
                packageName = ns.getName(abc.constants);
            }
        }
        return packageName;
    }

    public String getPathScriptName() {
        String scriptName = "";
        for (int t : traitIndices) {
            Multiname name = abc.script_info[scriptIndex].traits.traits[t].getName(abc);
            Namespace ns = name.getNamespace(abc.constants);
            if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
                scriptName = name.getName(abc.constants, new ArrayList<String>());
            }
        }
        return scriptName;
    }

    public String getPath() {
        String packageName = "";
        String scriptName = "";
        for (int t : traitIndices) {
            Multiname name = abc.script_info[scriptIndex].traits.traits[t].getName(abc);
            Namespace ns = name.getNamespace(abc.constants);
            if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
                packageName = ns.getName(abc.constants);
                scriptName = name.getName(abc.constants, new ArrayList<String>());
            }
        }
        return packageName + "." + scriptName;
    }

    private static String makeDirPath(String packageName) {
        if (packageName.equals("")) {
            return "";
        }
        String pathParts[];
        if (packageName.contains(".")) {
            pathParts = packageName.split("\\.");
        } else {
            pathParts = new String[]{packageName};
        }
        for (int i = 0; i < pathParts.length; i++) {
            pathParts[i] = Helper.makeFileName(pathParts[i]);
        }
        return Helper.joinStrings(pathParts, File.separator);
    }

    public void export(String directory, List<ABCContainerTag> abcList, boolean pcode, boolean paralel) throws IOException {
        String scriptName = getPathScriptName();
        String packageName = getPathPackage();

        File outDir = new File(directory + File.separatorChar + makeDirPath(packageName));
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        String fileName = outDir.toString() + File.separator + Helper.makeFileName(scriptName) + ".as";
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            for (int t : traitIndices) {
                Multiname name = abc.script_info[scriptIndex].traits.traits[t].getName(abc);
                Namespace ns = name.getNamespace(abc.constants);
                if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
                    fos.write(abc.script_info[scriptIndex].traits.traits[t].convertPackaged("", abcList, abc, false, pcode, scriptIndex, -1, false, new ArrayList<String>(), paralel).getBytes());
                } else {
                    fos.write(abc.script_info[scriptIndex].traits.traits[t].convert("", abcList, abc, false, pcode, scriptIndex, -1, false, new ArrayList<String>(), paralel).getBytes());
                }
            }
        }
    }
}
