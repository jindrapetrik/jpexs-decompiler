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

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.importers.As3ScriptReplaceException;
import com.jpexs.decompiler.flash.importers.As3ScriptReplacerFactory;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.helpers.Helper;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript3DirectEditingPCodeTest {

    @BeforeClass
    public void init() {
        Configuration.autoDeobfuscate.set(false);
        Configuration.simplifyExpressions.set(false);
        Configuration._debugCopy.set(false);
        Configuration.useFlexAs3Compiler.set(false);
    }

    @Test
    public void testDirectEditingPCode() throws IOException, InterruptedException, AVM2ParseException, CompilationException {
        String filePath = "testdata/as3_new/bin/as3_new.flex.swf";
        File expectedDir = new File("testexpected/as3_new");
        File actualDir = new File("testactual/as3_new");
        
        if (!actualDir.exists()) {
            actualDir.mkdirs();
        }
        
        
        File playerSWC = Configuration.getPlayerSWC();
        if (playerSWC == null) {
            throw new IOException("Player SWC library not found, please place it to " + Configuration.getFlashLibPath());
        }
        try {
            SWF swf = new SWF(new BufferedInputStream(new FileInputStream(filePath)), false);
            if (swf.isAS3()) {
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
                            File actualFile = new File(actualDir.getAbsolutePath() + "/" + classDirPath + ".as");
                            File expectedFile = new File(expectedDir.getAbsolutePath() + "/" + classDirPath + ".as");                            
                            File outParent = actualFile.getParentFile();
                            if (!outParent.exists()) {
                                outParent.mkdirs();
                            }
                            FileOutputStream fos = new FileOutputStream(actualFile);
                            fos.write(modifiedPcode.getBytes("UTF-8"));
                            fos.close();
                        
                            if (!expectedFile.exists()) {
                                fail("Expected file " + expectedFile.getAbsolutePath() + " does not exists!");
                            }
                            
                            String expectedText = Helper.readTextFile(expectedFile.getAbsolutePath());
                            String actualText = modifiedPcode;
                            
                            expectedText = expectedText.replace("\r\n", "\n");
                            actualText = actualText.replace("\r\n", "\n");
                            
                            if (!Objects.equals(actualText, expectedText)) {
                                fail("Files are not same - " + actualDir.getPath() + "/" + classDirPath + ".as");
                            }
                        } catch (As3ScriptReplaceException ex) {
                            fail("Exception during decompilation - file: " + filePath + " class: " + classPathString + " msg:" + ex.getMessage(), ex);
                        } catch (Exception ex) {
                            fail("Exception during decompilation - file: " + filePath + " class: " + classPathString + " msg:" + ex.getMessage(), ex);
                            throw ex;
                        }
                    }
                }
            } 
        } catch (Exception ex) {
            fail("Exception during decompilation: " + filePath + ":" + ex.getMessage(), ex);
        }
    }
}
