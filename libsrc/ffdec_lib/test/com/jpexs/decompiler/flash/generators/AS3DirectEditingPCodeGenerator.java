package com.jpexs.decompiler.flash.generators;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.importers.As3ScriptReplaceException;
import com.jpexs.decompiler.flash.importers.As3ScriptReplacerFactory;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class AS3DirectEditingPCodeGenerator {
    public static void main(String[] args) throws IOException, InterruptedException {
        String filePath = "testdata/as3_new/bin/as3_new.flex.swf";
        File outDir = new File("testexpected/as3_new");
        
        
        Configuration.autoDeobfuscate.set(false);
        Configuration.simplifyExpressions.set(false);
        Configuration._debugCopy.set(false);
        Configuration.useFlexAs3Compiler.set(false);
        
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        
        SWF swf = new SWF(new BufferedInputStream(new FileInputStream(filePath)), false);
            
        boolean dotest = false;
        List<ABC> allAbcs = new ArrayList<>();
        for (ABCContainerTag ct : swf.getAbcList()) {
            allAbcs.add(ct.getABC());
        }
        for (ABC abc : allAbcs) {
            for (int s = 0; s < abc.script_info.size(); s++) {
                HighlightedTextWriter htw = new HighlightedTextWriter(new CodeFormatting(), false);
                ScriptPack en = abc.script_info.get(s).getPacks(abc, s, null, allAbcs).get(0);
                String classPathString = en.getClassPath().toString();
                
                try {
                    en.toSource(swf.getAbcIndex(), htw, abc.script_info.get(s).traits.traits, new ConvertData(), ScriptExportMode.AS, false, false, false);
                    htw.finishHilights();
                    String original = htw.toString();
                    abc.replaceScriptPack(As3ScriptReplacerFactory.createFFDec() /*TODO: test the otherone*/, en, original, new ArrayList<>());
                    
                    htw = new HighlightedTextWriter(new CodeFormatting(), false);
                    en = abc.script_info.get(s).getPacks(abc, s, null, allAbcs).get(0);
                    en.toSource(swf.getAbcIndex(), htw, abc.script_info.get(s).traits.traits, new ConvertData(), ScriptExportMode.PCODE, false, false, false);
                    htw.finishHilights();
                    String modifiedPcode = htw.toString();
                    String classDirPath = classPathString.replace(".", "/");
                    File outFile = new File(outDir.getAbsolutePath() + "/" + classDirPath + ".as");
                    File outParent = outFile.getParentFile();
                    if (!outParent.exists()) {
                        outParent.mkdirs();
                    }
                    FileOutputStream fos = new FileOutputStream(outFile);
                    fos.write(modifiedPcode.getBytes("UTF-8"));
                    fos.close();
                    
                } catch (As3ScriptReplaceException ex) {
                    throw new RuntimeException("Exception during decompilation - file: " + filePath + " class: " + classPathString + " msg:" + ex.getMessage(), ex);
                } catch (Exception ex) {
                    throw new RuntimeException("Exception during decompilation - file: " + filePath + " class: " + classPathString + " msg:" + ex.getMessage(), ex);                    
                }
            }
        }
        System.exit(0);                
    }
}
