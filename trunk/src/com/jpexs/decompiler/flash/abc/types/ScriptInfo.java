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

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import java.util.ArrayList;
import java.util.List;

public class ScriptInfo {

    public int init_index; //MethodInfo
    public Traits traits;

    public List<MyEntry<ClassPath, ScriptPack>> getPacks(ABC abc, int scriptIndex) {
        List<MyEntry<ClassPath, ScriptPack>> ret = new ArrayList<>();

        List<Integer> otherTraits = new ArrayList<>();
        for (int j = 0; j < traits.traits.length; j++) {
            Trait t = traits.traits[j];
            Multiname name = t.getName(abc);
            Namespace ns = name.getNamespace(abc.constants);
            if (!((ns.kind == Namespace.KIND_PACKAGE_INTERNAL)
                    || (ns.kind == Namespace.KIND_PACKAGE))) {
                otherTraits.add(j);
            }
        }
        for (int j = 0; j < traits.traits.length; j++) {
            Trait t = traits.traits[j];
            Multiname name = t.getName(abc);
            Namespace ns = name.getNamespace(abc.constants);
            if ((ns.kind == Namespace.KIND_PACKAGE_INTERNAL)
                    || (ns.kind == Namespace.KIND_PACKAGE)) {
                String packageName = ns.getName(abc.constants);
                String objectName = name.getName(abc.constants, new ArrayList<String>());
                List<Integer> traitIndices = new ArrayList<>();

                traitIndices.add(j);
                if (!otherTraits.isEmpty()) {
                    traitIndices.addAll(otherTraits);
                }
                otherTraits = new ArrayList<>();
                ClassPath cp = new ClassPath(packageName, objectName);
                ret.add(new MyEntry<>(cp, new ScriptPack(cp, abc, scriptIndex, traitIndices)));
            }
        }
        return ret;
    }

    public int removeTraps(int scriptIndex, ABC abc, String path) {
        return traits.removeTraps(scriptIndex, -1, true, abc, path);
    }

    @Override
    public String toString() {
        return "method_index=" + init_index + "\r\n" + traits.toString();
    }

    public String toString(ABC abc, List<String> fullyQualifiedNames) {
        return "method_index=" + init_index + "\r\n" + traits.toString(abc, fullyQualifiedNames);
    }

    public String convert(List<ABCContainerTag> abcTags, ABC abc, boolean pcode, boolean highlighting, int scriptIndex, boolean paralel) {
        return traits.convert("", abcTags, abc, false, pcode, true, scriptIndex, -1, highlighting, new ArrayList<String>(), paralel);
    }
}
