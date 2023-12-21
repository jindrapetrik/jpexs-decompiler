/*
 * Copyright (C) 2023 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.Tag;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class TestNoTraits {
    public static void main(String[] args) throws IOException, InterruptedException {
        SWF swf = new SWF(new FileInputStream("c:\\FlashRelated\\2162\\test_no_traits.swf"), false, false);
        ABCContainerTag cnt = swf.getAbcList().get(0);
        ABC abc = cnt.getABC();
        Trait t = abc.script_info.get(2).traits.traits.remove(0);
        ((TraitClass)t).slot_id = 2;
        //abc.script_info.get(1).traits.traits.add(0, t);
        TraitSlotConst tsc = new TraitSlotConst();
        tsc.kindType = TraitSlotConst.TRAIT_SLOT;
        tsc.slot_id = 2;
        tsc.name_index = t.name_index;        
        abc.script_info.get(1).traits.traits.add(0, tsc);
        //abc.script_info.get(1).traits.traits.clear();
        ((Tag) cnt).setModified(true);
        try(FileOutputStream fos = new FileOutputStream("c:\\FlashRelated\\2162\\test_no_traits_mod.swf")) {
            swf.saveTo(fos);
        }
       
    }
}
