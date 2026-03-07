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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript2DirectEditingPCodeTest {

    @BeforeClass
    public void init() {
        Configuration.autoDeobfuscate.set(false);
        Configuration.simplifyExpressions.set(false);
        Configuration._debugCopy.set(false);
        Configuration.useFlexAs3Compiler.set(false);
    }

    @Test
    public void testDirectEditingPCode() throws IOException, InterruptedException, AVM2ParseException, CompilationException {
        String filePath = "testdata/as2/as2.swf";
        File expectedDir = new File("testexpected/as2");
        File actualDir = new File("testactual/as2");

        if (!actualDir.exists()) {
            actualDir.mkdirs();
        }

        List<String> paths = new ArrayList<>();

        try {

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
                asm.getActionScriptSource(writer, null);
                writer.finishHilights();
                String as2 = writer.toString();
                //as2 = asm.removePrefixAndSuffix(as2);
                try {
                    asm.setActions(par.actionsFromString(as2, Utf8Helper.charsetName));
                } catch (ActionParseException | CompilationException ex) {
                    fail("Unable to parse: " + asm.getSwf().getTitleOrShortFileName() + "/" + asm.toString(), ex);
                }
                writer = new HighlightedTextWriter(new CodeFormatting(), false);
                asm.getASMSource(ScriptExportMode.PCODE, writer, null);
                //asm.getActionScriptSource(writer, null);
                writer.finishHilights();
                String modifiedPcode = writer.toString();

                String classDirPath = key.replace("\\", "/");
                File actualFile = new File(actualDir.getAbsolutePath() + classDirPath + ".as");
                File outParent = actualFile.getParentFile();
                if (!outParent.exists()) {
                    outParent.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(actualFile);
                fos.write(modifiedPcode.getBytes("UTF-8"));
                fos.close();
                paths.add(classDirPath);
            }

        } catch (Exception ex) {
            fail("Exception during decompilation: " + filePath + ":" + ex.getMessage(), ex);
        }

        StringBuilder notSameBuilder = new StringBuilder();

        for (String path : paths) {
            File expectedFile = new File(expectedDir.getAbsolutePath() + "/" + path + ".as");
            File actualFile = new File(actualDir.getAbsolutePath() + "/" + path + ".as");
            String expectedText = Helper.readTextFile(expectedFile.getAbsolutePath());
            String actualText = Helper.readTextFile(actualFile.getAbsolutePath());

            expectedText = expectedText.replace("\r\n", "\n");
            actualText = actualText.replace("\r\n", "\n");

            if (!Objects.equals(actualText, expectedText)) {
                notSameBuilder.append(actualDir.getPath()).append(path.replace("\\", "/")).append(".as\r\n");
                //
            }
        }
        if (notSameBuilder.length() > 0) {
            fail("File(s) are not same: " + notSameBuilder.toString());
        }
    }
}
