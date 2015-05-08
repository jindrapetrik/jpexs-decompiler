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
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.helpers.FileTextWriter;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.graph.TranslateException;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
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

    private static final Logger logger = Logger.getLogger(AS2ScriptExporter.class.getName());

    public List<File> exportActionScript2(SWF swf, AbortRetryIgnoreHandler handler, String outdir, ScriptExportSettings exportSettings, boolean parallel, EventListener evl) throws IOException {
        return exportAS2ScriptsTimeout(handler, outdir, swf.getASMs(true), exportSettings, evl);
    }

    public List<File> exportAS2ScriptsTimeout(final AbortRetryIgnoreHandler handler, final String outdir, final Map<String, ASMSource> asms, final ScriptExportSettings exportSettings, final EventListener evl) throws IOException {
        try {
            List<File> result = CancellableWorker.call(new Callable<List<File>>() {

                @Override
                public List<File> call() throws Exception {
                    return exportAS2Scripts(handler, outdir, asms, exportSettings, evl);
                }
            }, Configuration.exportTimeout.get(), TimeUnit.SECONDS);
            return result;
        } catch (TimeoutException ex) {
            logger.log(Level.SEVERE, Helper.formatTimeToText(Configuration.exportTimeout.get()) + " ActionScript export limit reached", ex);
        } catch (ExecutionException | InterruptedException ex) {
            logger.log(Level.SEVERE, "Error during AS2 export", ex);
        }
        return new ArrayList<>();
    }

    private List<File> exportAS2Scripts(AbortRetryIgnoreHandler handler, String outdir, Map<String, ASMSource> asms, ScriptExportSettings exportSettings, EventListener evl) throws IOException {
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

            File f = exportAS2Script(handler, currentOutDir, asm, exportSettings, evl, cnt, asms.size(), name);
            if (f != null) {
                ret.add(f);
            }
        }

        return ret;
    }

    private File exportAS2Script(AbortRetryIgnoreHandler handler, String outdir, ASMSource asm, ScriptExportSettings exportSettings, EventListener evl, AtomicInteger index, int count, String name) throws IOException {
        boolean retry;
        do {
            retry = false;
            try {
                int currentIndex = index.getAndIncrement();

                if (!exportSettings.singleFile) {
                    Path.createDirectorySafe(new File(outdir));
                }

                String f = Path.combine(outdir, name) + exportSettings.getFileExtension();
                if (evl != null) {
                    evl.handleExportingEvent("script", currentIndex, count, f);
                }

                long startTime = System.currentTimeMillis();

                File file = new File(f);
                try (FileTextWriter writer = exportSettings.singleFile ? null : new FileTextWriter(Configuration.getCodeFormatting(), new FileOutputStream(f))) {
                    FileTextWriter writer2 = exportSettings.singleFile ? exportSettings.singleFileWriter : writer;
                    ScriptExportMode exportMode = exportSettings.mode;
                    if (exportMode == ScriptExportMode.HEX) {
                        asm.getActionSourcePrefix(writer2);
                        asm.getActionBytesAsHex(writer2);
                        asm.getActionSourceSuffix(writer2);
                    } else if (exportMode != ScriptExportMode.AS) {
                        asm.getActionSourcePrefix(writer2);
                        asm.getASMSource(exportMode, writer2, null);
                        asm.getActionSourceSuffix(writer2);
                    } else {
                        List<Action> as = asm.getActions();
                        Action.setActionsAddresses(as, 0);
                        Action.actionsToSource(asm, as, ""/*FIXME*/, writer2);
                    }
                }

                long stopTime = System.currentTimeMillis();

                if (evl != null) {
                    long time = stopTime - startTime;
                    evl.handleExportedEvent("script", currentIndex, count, f + ", " + Helper.formatTimeSec(time));
                }

                return file;
            } catch (InterruptedException ex) {
            } catch (IOException | OutOfMemoryError | TranslateException | StackOverflowError ex) {
                Logger.getLogger(AS2ScriptExporter.class.getName()).log(Level.SEVERE, "Decompilation error in script: " + name, ex);
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
