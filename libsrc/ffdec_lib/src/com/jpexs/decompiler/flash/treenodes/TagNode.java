/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.treenodes;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.FileTextWriter;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.Exportable;
import com.jpexs.decompiler.graph.TranslateException;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TagNode extends ContainerNode {

    public TagNode(Tag tag) {
        super(tag);
    }

    @Override
    public Tag getItem() {
        return (Tag) item;
    }

    public static void setExport(List<TreeNode> nodeList, boolean export) {
        for (TreeNode node : nodeList) {
            node.export = export;
            setExport(node.subNodes, export);
        }
    }

    public static int getTagCountRecursive(List<TreeNode> nodeList) {
        int count = 0;

        for (TreeNode node : nodeList) {
            if (node.subNodes.isEmpty()) {
                if ((node.item instanceof ASMSource) && (node.export)) {
                    count += 1;
                }
            } else {
                count += getTagCountRecursive(node.subNodes);
            }

        }

        return count;
    }

    public static List<File> exportNodeAS(final AbortRetryIgnoreHandler handler, final List<TreeNode> nodeList, final String outdir, final ScriptExportMode exportMode, final EventListener ev) throws IOException {
        try {
            List<File> result = CancellableWorker.call(new Callable<List<File>>() {

                @Override
                public List<File> call() throws Exception {
                    AtomicInteger cnt = new AtomicInteger(1);
                    int totalCount = TagNode.getTagCountRecursive(nodeList);
                    return exportNodeAS(handler, nodeList, outdir, exportMode, cnt, totalCount, ev);
                }
            }, Configuration.exportTimeout.get(), TimeUnit.SECONDS);
            return result;
        } catch (ExecutionException | InterruptedException | TimeoutException ex) {
        }
        return new ArrayList<>();
    }

    private static List<File> exportNodeAS(AbortRetryIgnoreHandler handler, List<TreeNode> nodeList, String outdir, ScriptExportMode exportMode, AtomicInteger index, int count, EventListener ev) throws IOException {
        File dir = new File(outdir);
        List<File> ret = new ArrayList<>();
        if (!outdir.endsWith(File.separator)) {
            outdir += File.separator;
        }
        List<String> existingNames = new ArrayList<>();
        for (TreeNode node : nodeList) {
            String name = "";
            if (node.item instanceof Exportable) {
                name = Helper.makeFileName(((Exportable) node.item).getExportFileName());
            } else {
                name = Helper.makeFileName(node.item.toString());
            }
            int i = 1;
            String baseName = name;
            while (existingNames.contains(name)) {
                i++;
                name = baseName + "_" + i;
            }
            existingNames.add(name);
            if (node.subNodes.isEmpty()) {
                if ((node.item instanceof ASMSource) && (node.export)) {
                    boolean retry;
                    do {
                        retry = false;
                        try {
                            int currentIndex = index.getAndIncrement();

                            if (!dir.exists()) {
                                if (!dir.mkdirs()) {
                                    if (!dir.exists()) {
                                        throw new IOException("Cannot create directory " + outdir);
                                    }
                                }
                            }

                            String f = outdir + name + ".as";
                            if (ev != null) {
                                ev.handleEvent("exporting", "Exporting " + currentIndex + "/" + count + " " + f);
                            }

                            long startTime = System.currentTimeMillis();

                            File file = new File(f);
                            ASMSource asm = ((ASMSource) node.item);
                            try (FileTextWriter writer = new FileTextWriter(Configuration.getCodeFormatting(), new FileOutputStream(f))) {
                                if (exportMode == ScriptExportMode.HEX) {
                                    asm.getActionSourcePrefix(writer);
                                    asm.getActionBytesAsHex(writer);
                                    asm.getActionSourceSuffix(writer);
                                } else if (exportMode != ScriptExportMode.AS) {
                                    asm.getActionSourcePrefix(writer);
                                    asm.getASMSource(exportMode, writer, null);
                                    asm.getActionSourceSuffix(writer);
                                } else {
                                    List<Action> as = asm.getActions();
                                    Action.setActionsAddresses(as, 0);
                                    Action.actionsToSource(asm, as, ""/*FIXME*/, writer);
                                }
                            }

                            long stopTime = System.currentTimeMillis();

                            if (ev != null) {
                                long time = stopTime - startTime;
                                ev.handleEvent("exported", "Exported " + currentIndex + "/" + count + " " + f + ", " + Helper.formatTimeSec(time));
                            }

                            ret.add(file);
                        } catch (InterruptedException ex) {
                        } catch (IOException | OutOfMemoryError | TranslateException | StackOverflowError ex) {
                            Logger.getLogger(TagNode.class.getName()).log(Level.SEVERE, "Decompilation error in file: " + name + ".as", ex);
                            if (handler != null) {
                                int action = handler.getNewInstance().handle(ex);
                                switch (action) {
                                    case AbortRetryIgnoreHandler.ABORT:
                                        throw ex;
                                    case AbortRetryIgnoreHandler.RETRY:
                                        retry = true;
                                        break;
                                    case AbortRetryIgnoreHandler.IGNORE:
                                        retry = false;
                                        break;
                                }
                            }
                        }
                    } while (retry);
                }
            } else {
                ret.addAll(exportNodeAS(handler, node.subNodes, outdir + name, exportMode, index, count, ev));
            }

        }
        return ret;
    }
}
