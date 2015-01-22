/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.exporters.script;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.FileTextWriter;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.graph.TranslateException;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class AS2ScriptExporter {

    public List<File> exportAS2ScriptsTimeout(final AbortRetryIgnoreHandler handler, final String outdir, final Map<String, ASMSource> asms, final ScriptExportMode exportMode, final EventListener ev) throws IOException {
        try {
            List<File> result = CancellableWorker.call(new Callable<List<File>>() {

                @Override
                public List<File> call() throws Exception {
                    return exportAS2Scripts(handler, outdir, asms, exportMode, ev);
                }
            }, Configuration.exportTimeout.get(), TimeUnit.SECONDS);
            return result;
        } catch (ExecutionException | InterruptedException | TimeoutException ex) {
        }
        return new ArrayList<>();
    }

    private List<File> exportAS2Scripts(AbortRetryIgnoreHandler handler, String outdir, Map<String, ASMSource> asms, ScriptExportMode exportMode, EventListener ev) throws IOException {
        List<File> ret = new ArrayList<>();
        if (!outdir.endsWith(File.separator)) {
            outdir += File.separator;
        }

        Map<String, List<String>> existingNamesMap = new HashMap<>();
        AtomicInteger cnt = new AtomicInteger(1);
        for (String key : asms.keySet()) {
            ASMSource asm = asms.get(key);
            String currentOutDir = outdir + key + File.separator;
            currentOutDir = new File(currentOutDir).getParentFile().toString() + File.separator;

            List<String> existingNames = existingNamesMap.get(currentOutDir);
            if (existingNames == null) {
                existingNames = new ArrayList<>();
                existingNamesMap.put(currentOutDir, existingNames);
            }

            String name = Helper.makeFileName(asm.getExportFileName());
            int i = 1;
            String baseName = name;
            while (existingNames.contains(name)) {
                i++;
                name = baseName + "_" + i;
            }
            existingNames.add(name);

            File f = exportAS2Script(handler, currentOutDir, asm, exportMode, ev, cnt, asms.size(), name);
            if (f != null) {
                ret.add(f);
            }
        }

        return ret;
    }

    private File exportAS2Script(AbortRetryIgnoreHandler handler, String outdir, ASMSource asm, ScriptExportMode exportMode, EventListener ev, AtomicInteger index, int count, String name) throws IOException {
        boolean retry;
        do {
            retry = false;
            try {
                int currentIndex = index.getAndIncrement();

                File dir = new File(outdir);
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

                return file;
            } catch (InterruptedException ex) {
            } catch (IOException | OutOfMemoryError | TranslateException | StackOverflowError ex) {
                Logger.getLogger(AS2ScriptExporter.class.getName()).log(Level.SEVERE, "Decompilation error in file: " + name + ".as", ex);
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

        return null;
    }
}
