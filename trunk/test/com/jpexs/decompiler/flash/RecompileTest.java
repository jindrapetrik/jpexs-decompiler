/*
 * Copyright (C) 2010-2014 JPEXS
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
package com.jpexs.decompiler.flash;

import static com.jpexs.decompiler.flash.SWF.createASTagList;
import com.jpexs.decompiler.flash.abc.NotSameException;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.ParseException;
import com.jpexs.decompiler.flash.action.parser.script.ActionScriptParser;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.ContainerItem;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.treenodes.TagNode;
import com.jpexs.decompiler.flash.treenodes.TreeNode;
import com.jpexs.decompiler.graph.TranslateException;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class RecompileTest {

    public static final String TESTDATADIR = "testdata/recompile";

    private void testRecompileOne(String filename) {
        try {
            SWF swf = new SWF(new BufferedInputStream(new FileInputStream(TESTDATADIR + File.separator + filename)), false);
            Configuration.debugCopy.set(true);
            swf.saveTo(new ByteArrayOutputStream());
        } catch (IOException | InterruptedException ex) {
            fail();
        } catch (NotSameException ex) {
            fail("File is different after recompiling: " + filename);
        }
    }

    @Test
    public void testRecompile() {
        File dir = new File(TESTDATADIR);
        if (!dir.exists()) {
            return;
        }
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".swf");
            }
        });
        for (File f : files) {
            testRecompileOne(f.getName());
        }
    }

    private void testAS2DirectEditingOneRecursive(List<TreeNode> nodeList) {
        for (TreeNode node : nodeList) {
            if (node.subNodes.isEmpty()) {
                TreeItem item = node.getItem();
                if ((item instanceof ASMSource) && (node.export)) {
                    boolean retry;
                    do {
                        retry = false;
                        try {
                            ASMSource asm = ((ASMSource) item);
                            HilightedTextWriter writer = new HilightedTextWriter(new CodeFormatting(), false);
                            Action.actionsToSource(asm, asm.getActions(), asm.toString()/*FIXME?*/, writer);
                            String as = writer.toString();
                            ActionScriptParser par = new ActionScriptParser();
                            try {
                                asm.setActions(par.actionsFromString(as));
                            } catch (ParseException ex) {
                                fail("Unable to parse: " + item.getSwf().getShortFileName() + "/" + item.toString());
                            }
                            writer = new HilightedTextWriter(new CodeFormatting(), false);
                            Action.actionsToSource(asm, asm.getActions(), asm.toString()/*FIXME?*/, writer);
                            String as2 = writer.toString();
                            try {
                                asm.setActions(par.actionsFromString(as2));
                            } catch (ParseException ex) {
                                fail("Unable to parse: " + item.getSwf().getShortFileName() + "/" + item.toString());
                            }
                            writer = new HilightedTextWriter(new CodeFormatting(), false);
                            Action.actionsToSource(asm, asm.getActions(), asm.toString()/*FIXME?*/, writer);
                            String as3 = writer.toString();
                            if (!as3.equals(as2)) {
                                fail("ActionScript is diffrent: " + item.getSwf().getShortFileName() + "/" + item.toString());
                            }
                        } catch (InterruptedException | IOException | OutOfMemoryError | TranslateException | StackOverflowError ex) {
                        }
                    } while (retry);
                }
            } else {
                testAS2DirectEditingOneRecursive(node.subNodes);
            }
        }
    }
    
    private void testAS2DirectEditingOne(String filename) {
        try {
            SWF swf = new SWF(new BufferedInputStream(new FileInputStream(TESTDATADIR + File.separator + filename)), false);
            List<ContainerItem> list2 = new ArrayList<>();
            list2.addAll(swf.tags);
            List<TreeNode> list = createASTagList(list2, null);

            TagNode.setExport(list, true);
            testAS2DirectEditingOneRecursive(list);
        } catch (IOException | InterruptedException ex) {
            fail();
        }
    }

    @Test
    public void testAS2DirectEditing() {
        File dir = new File(TESTDATADIR);
        if (!dir.exists()) {
            return;
        }
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".swf");
            }
        });
        for (File f : files) {
            testAS2DirectEditingOne(f.getName());
        }
    }
}
