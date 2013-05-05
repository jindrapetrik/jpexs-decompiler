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
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScriptInfo {

    public int init_index; //MethodInfo
    public Traits traits;

    public int removeTraps(int scriptIndex, ABC abc) {
        return traits.removeTraps(scriptIndex, -1, true, abc);
    }

    @Override
    public String toString() {
        return "method_index=" + init_index + "\r\n" + traits.toString();
    }

    public String toString(ABC abc, List<String> fullyQualifiedNames) {
        return "method_index=" + init_index + "\r\n" + traits.toString(abc, fullyQualifiedNames);
    }

    public String convert(List<ABCContainerTag> abcTags, ABC abc, boolean pcode, boolean highlighting, int scriptIndex) {
        return traits.convert("", abcTags, abc, false, pcode, true, scriptIndex, -1, highlighting, new ArrayList<String>());
    }

    public void export(ABC abc, List<ABCContainerTag> abcList, String directory, boolean pcode, int scriptIndex) throws IOException {
        for (Trait t : traits.traits) {
            t.export(directory, abc, abcList, pcode, scriptIndex, -1, false);
        }


    }
}
