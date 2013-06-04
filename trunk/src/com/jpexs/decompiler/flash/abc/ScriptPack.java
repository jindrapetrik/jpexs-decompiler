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

    public void export(String directory, List<ABCContainerTag> abcList, boolean pcode) throws IOException {
        String path = getPath();
        String scriptName = path.substring(path.lastIndexOf(".") + 1);
        String packageName = path.substring(0, path.lastIndexOf("."));
        File outDir = new File(directory + File.separatorChar + packageName.replace('.', File.separatorChar));
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        String fileName = outDir.toString() + File.separator + scriptName + ".as";
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            for (int t : traitIndices) {
                Multiname name = abc.script_info[scriptIndex].traits.traits[t].getName(abc);
                Namespace ns = name.getNamespace(abc.constants);
                if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
                    fos.write(abc.script_info[scriptIndex].traits.traits[t].convertPackaged("", abcList, abc, false, pcode, scriptIndex, -1, false, new ArrayList<String>()).getBytes());
                } else {
                    fos.write(abc.script_info[scriptIndex].traits.traits[t].convert("", abcList, abc, false, pcode, scriptIndex, -1, false, new ArrayList<String>()).getBytes());
                }
            }
        }
    }
}
