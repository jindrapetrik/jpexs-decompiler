package com.jpexs.decompiler.flash.importers;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
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
import java.util.TreeSet;

public class FFDecAs3ScriptReplacer implements As3ScriptReplacerInterface {

    @Override
    public void replaceScript(ScriptPack pack, String text) throws As3ScriptReplaceException, IOException, InterruptedException {
        ABC abc = pack.abc;
        SWF swf = pack.abc.getSwf();
        String scriptName = pack.getPathScriptName() + ".as";
        int oldIndex = pack.scriptIndex;
        int newIndex = abc.script_info.size();
        try {
            String documentClass = swf.getDocumentClass();
            boolean isDocumentClass = documentClass != null && documentClass.equals(pack.getClassPath().toString());

            ScriptInfo si = abc.script_info.get(oldIndex);
            if (pack.isSimple) {
                si.delete(abc, true);
            } else {
                for (int t : pack.traitIndices) {
                    si.traits.traits.get(t).delete(abc, true);
                }
            }

            int newClassIndex = abc.instance_info.size();
            for (int t : pack.traitIndices) {
                if (si.traits.traits.get(t) instanceof TraitClass) {
                    TraitClass tc = (TraitClass) si.traits.traits.get(t);
                    newClassIndex = tc.class_info + 1;
                }

            }
            List<ABC> otherAbcs = new ArrayList<>(pack.allABCs);

            otherAbcs.remove(abc);
            abc.script_info.get(oldIndex).delete(abc, true);

            ActionScript3Parser.compile(text, abc, otherAbcs, isDocumentClass, scriptName, newClassIndex, oldIndex);
            if (pack.isSimple) {
                // Move newly added script to its position
                abc.script_info.set(oldIndex, abc.script_info.get(newIndex));
                abc.script_info.remove(newIndex);
            } else {
                //???
            }
            abc.script_info.get(oldIndex).setModified(true);
            abc.pack();//remove old deleted items            
            ((Tag) abc.parentTag).setModified(true);
        } catch (AVM2ParseException ex) {
            abc.script_info.get(oldIndex).delete(abc, false);
            throw new As3ScriptReplaceException(new As3ScriptReplaceExceptionItem(null, ex.text, (int) ex.line));
        } catch (CompilationException ex) {
            abc.script_info.get(oldIndex).delete(abc, false);
        }
    }

    @Override
    public boolean isAvailable() {
        File swc = Configuration.getPlayerSWC();
        return !(swc == null || !swc.exists());
    }

    @Override
    public void initReplacement(ScriptPack pack) {
        //empty
    }

    @Override
    public void deinitReplacement(ScriptPack pack) {
        //empty
    }

}
