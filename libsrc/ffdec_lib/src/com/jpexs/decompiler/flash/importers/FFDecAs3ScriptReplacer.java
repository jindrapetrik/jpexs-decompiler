/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.importers;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.ActionScript3Parser;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.graph.CompilationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * FFDec ActionScript 3 script replacer.
 */
public class FFDecAs3ScriptReplacer implements As3ScriptReplacerInterface {

    private boolean air;

    /**
     * Constructor.
     * @param air True if AIR is used, false if Flash Player is used
     */
    public FFDecAs3ScriptReplacer(boolean air) {
        this.air = air;
    }

    @Override
    public void replaceScript(ScriptPack pack, String text, List<SWF> dependencies) throws As3ScriptReplaceException, IOException, InterruptedException {
        ABC abc = pack.abc;
        SWF swf = pack.abc.getSwf();
        String scriptName = pack.getPathScriptName() + ".as";
        int oldIndex = pack.scriptIndex;
        int newIndex = abc.script_info.size();
        AbcIndexing abcIndex = swf.getAbcIndex();
        try {
            ScriptInfo si = abc.script_info.get(oldIndex);
            pack.delete(abc, true);

            int newClassIndex = abc.instance_info.size();
            for (int t : pack.traitIndices) {
                if (si.traits.traits.get(t) instanceof TraitClass) {
                    TraitClass tc = (TraitClass) si.traits.traits.get(t);
                    newClassIndex = tc.class_info + 1;
                }

            }
            List<ABC> otherAbcs = new ArrayList<>(pack.allABCs);

            otherAbcs.remove(abc);            
            abcIndex.selectAbc(abc);
            abcIndex.refreshAbc(abc);

            ActionScript3Parser.compile(text, abc, abcIndex, scriptName, newClassIndex, oldIndex, air, swf.getDocumentClass());
            if (pack.isSimple) {
                // Move newly added script to its position
                abc.script_info.set(oldIndex, abc.script_info.get(newIndex));
                abc.script_info.remove(newIndex);
            } else {
                //???
            }
            abc.script_info.get(oldIndex).setModified(true);
            abc.pack(); //remove old deleted items
            ((Tag) abc.parentTag).setModified(true);
        } catch (AVM2ParseException ex) {
            pack.delete(abc, false);
            abcIndex.refreshAbc(abc);
            //ex.printStackTrace();
            throw new As3ScriptReplaceException(new As3ScriptReplaceExceptionItem(null, ex.text, (int) ex.line));
        } catch (CompilationException ex) {
            pack.delete(abc, false);
            abcIndex.refreshAbc(abc);
            //ex.printStackTrace();
            throw new As3ScriptReplaceException(new As3ScriptReplaceExceptionItem(null, ex.text, (int) ex.line));
        }
    }

    @Override
    public boolean isAvailable() {
        File swc = air ? Configuration.getAirSWC() : Configuration.getPlayerSWC();
        return !(swc == null || !swc.exists());
    }

    /**
     * Check if AIR is used.
     * @return True if AIR is used, false if Flash Player is used
     */
    public boolean isAir() {
        return air;
    }

    @Override
    public void initReplacement(ScriptPack pack, List<SWF> dependencies) {
        //empty
    }

    @Override
    public void deinitReplacement(ScriptPack pack) {
        //empty
    }

}
