/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.importers.As3ScriptReplaceException;
import com.jpexs.decompiler.flash.importers.As3ScriptReplacerFactory;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.TranslateException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class DirectEditingTest extends FileTestBase {

    @BeforeClass
    public void init() {
        Configuration.autoDeobfuscate.set(false);
        Configuration.simplifyExpressions.set(false);
        Configuration._debugCopy.set(false);
        Configuration.useFlexAs3Compiler.set(false);
    }

    public static final String TESTDATADIR = "testdata/directediting";

    @Test(dataProvider = "provideFiles")
    public void testDirectEditing(String filePath) throws IOException, InterruptedException, AVM2ParseException, CompilationException {
        File playerSWC = Configuration.getPlayerSWC();
        if (playerSWC == null) {
            throw new IOException("Player SWC library not found, please place it to " + Configuration.getFlashLibPath());
        }
        try {
            SWF swf = new SWF(new BufferedInputStream(new FileInputStream(filePath)), false);
            if (swf.isAS3()) {
                boolean dotest = false;
                List<ABC> allAbcs = new ArrayList<>();
                for (ABCContainerTag ct : swf.getAbcList()) {
                    allAbcs.add(ct.getABC());
                }
                for (ABC abc : allAbcs) {
                    for (int s = 0; s < abc.script_info.size(); s++) {
                        String startAfter = null;
                        HighlightedTextWriter htw = new HighlightedTextWriter(new CodeFormatting(), false);
                        ScriptPack en = abc.script_info.get(s).getPacks(abc, s, null, allAbcs).get(0);
                        String classPathString = en.getClassPath().toString();
                        if (startAfter == null || classPathString.equals(startAfter)) {
                            dotest = true;
                        }
                        if (!dotest) {
                            System.out.println("Skipped:" + classPathString);
                            continue;
                        }

                        System.out.println("Recompiling:" + classPathString + "...");
                        try {
                            en.toSource(htw, abc.script_info.get(s).traits.traits, new ConvertData(), ScriptExportMode.AS, false);
                            String original = htw.toString();
                            abc.replaceScriptPack(As3ScriptReplacerFactory.createFFDec() /*TODO: test the otherone*/, en, original);
                        } catch (As3ScriptReplaceException ex) {
                            fail("Exception during decompilation - file: " + filePath + " class: " + classPathString + " msg:" + ex.getMessage(), ex);
                        } catch (Exception ex) {
                            fail("Exception during decompilation - file: " + filePath + " class: " + classPathString + " msg:" + ex.getMessage(), ex);
                            throw ex;
                        }
                    }
                }
            } else {
                Map<String, ASMSource> asms = swf.getASMs(false);

                for (ASMSource asm : asms.values()) {
                    try {
                        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
                        asm.getActionScriptSource(writer, null);
                        String as = writer.toString();
                        as = asm.removePrefixAndSuffix(as);
                        ActionScript2Parser par = new ActionScript2Parser(swf.version);
                        try {
                            asm.setActions(par.actionsFromString(as));
                        } catch (ActionParseException | CompilationException ex) {
                            fail("Unable to parse: " + as + "/" + asm.toString(), ex);
                        }
                        writer = new HighlightedTextWriter(new CodeFormatting(), false);
                        asm.getActionScriptSource(writer, null);
                        String as2 = writer.toString();
                        as2 = asm.removePrefixAndSuffix(as2);
                        try {
                            asm.setActions(par.actionsFromString(as2));
                        } catch (ActionParseException | CompilationException ex) {
                            fail("Unable to parse: " + asm.getSwf().getShortFileName() + "/" + asm.toString(), ex);
                        }
                        writer = new HighlightedTextWriter(new CodeFormatting(), false);
                        asm.getActionScriptSource(writer, null);
                        String as3 = writer.toString();
                        as3 = asm.removePrefixAndSuffix(as3);
                        if (!as3.equals(as2)) {
                            fail("ActionScript is different: " + asm.getSwf().getShortFileName() + "/" + asm.toString());
                        }
                        asm.setModified();
                    } catch (InterruptedException | IOException | OutOfMemoryError | TranslateException | StackOverflowError ex) {
                    }
                }
            }
            String nFilePath = filePath.substring(0, filePath.length() - 4); //remove .swf
            nFilePath += ".recompiled.swf";

            try (FileOutputStream fos = new FileOutputStream(nFilePath)) {
                swf.saveTo(fos);
            }
            //TODO: try tu run it in debug flashplayer (?)
        } catch (Exception ex) {
            fail("Exception during decompilation: " + filePath + ":" + ex.getMessage(), ex);
        }
    }

    @Override
    public String[] getTestDataDirs() {
        return new String[]{TESTDATADIR, FREE_ACTIONSCRIPT_AS2, FREE_ACTIONSCRIPT_AS3};
    }
}
