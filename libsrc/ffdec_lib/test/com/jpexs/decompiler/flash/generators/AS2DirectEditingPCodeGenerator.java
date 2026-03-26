/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.generators;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import static org.testng.Assert.fail;

/**
 *
 * @author JPEXS
 */
public class AS2DirectEditingPCodeGenerator {
    public static void main(String[] args) throws IOException, InterruptedException {
        String filePath = "testdata/as2/as2.swf";
        File outDir = new File("testexpected/as2");
        
        
        Configuration.autoDeobfuscate.set(false);
        Configuration.simplifyExpressions.set(false);
        Configuration._debugCopy.set(false);
        Configuration.useFlexAs3Compiler.set(false);
        Configuration.skipDetectionOfUninitializedClassFields.set(false);
        
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        
        SWF swf = new SWF(new BufferedInputStream(new FileInputStream(filePath)), false);
        
        Map<String, ASMSource> asms = swf.getASMs(true);

        for (String key : asms.keySet()) { 
            ASMSource asm = asms.get(key);
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
            asm.getActionScriptSource(writer, null);
            writer.finishHilights();
            String as = writer.toString();
            //as = asm.removePrefixAndSuffix(as);

            ActionScript2Parser par = new ActionScript2Parser(swf, asm);
            try {
                asm.setActions(par.actionsFromString(as, Utf8Helper.charsetName));
            } catch (ActionParseException | CompilationException ex) {
                fail("Unable to parse: " + as + "/" + asm.toString(), ex);
            }           
            writer = new HighlightedTextWriter(new CodeFormatting(), false);
            asm.getASMSource(ScriptExportMode.PCODE, writer, null);
            //asm.getActionScriptSource(writer, null);
            writer.finishHilights();
            String modifiedPcode = writer.toString();
            
            String classDirPath = key;
            File outFile = new File(outDir.getAbsolutePath() + "/" + classDirPath + ".as");
            File outParent = outFile.getParentFile();
            if (!outParent.exists()) {
                outParent.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(outFile);
            fos.write(modifiedPcode.getBytes("UTF-8"));
            fos.close();            
        }
        System.exit(0);
    }
}
