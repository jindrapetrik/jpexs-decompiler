/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class AS2ScriptExporter {

    private static final Logger logger = Logger.getLogger(AS2ScriptExporter.class.getName());

    public List<File> exportActionScript2(SWF swf, AbortRetryIgnoreHandler handler, String outdir, ScriptExportSettings exportSettings, boolean parallel, EventListener evl) throws IOException {
        return exportAS2Scripts(handler, outdir, swf.getASMs(true), exportSettings, parallel, evl);
    }

    public List<File> exportAS2Scripts(AbortRetryIgnoreHandler handler, String outdir, Map<String, ASMSource> asms, ScriptExportSettings exportSettings, boolean parallel, EventListener evl) throws IOException {
        List<File> ret = new ArrayList<>();
        if (!outdir.endsWith(File.separator)) {
            outdir += File.separator;
        }

        Map<String, List<String>> existingNamesMap = new HashMap<>();
        int cnt = 1;
        List<ExportScriptTask> tasks = new ArrayList<>();
        String[] keys = asms.keySet().toArray(new String[asms.size()]);

        for (String key : keys) {
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

            tasks.add(new ExportScriptTask(handler, cnt++, asms.size(), name, asm, currentOutDir, exportSettings, evl));
        }

        if (!parallel || tasks.size() < 2) {
            try {
                CancellableWorker.call(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        for (ExportScriptTask task : tasks) {
                            if (Thread.currentThread().isInterrupted()) {
                                throw new InterruptedException();
                            }

                            ret.add(task.call());
                        }
                        return null;
                    }
                }, Configuration.exportTimeout.get(), TimeUnit.SECONDS);
            } catch (TimeoutException ex) {
                logger.log(Level.SEVERE, Helper.formatTimeToText(Configuration.exportTimeout.get()) + " ActionScript export limit reached", ex);
            } catch (ExecutionException | InterruptedException ex) {
                logger.log(Level.SEVERE, "Error during AS2 export", ex);
            }
        } else {
            ExecutorService executor = Executors.newFixedThreadPool(Configuration.getParallelThreadCount());
            List<Future<File>> futureResults = new ArrayList<>();
            for (ExportScriptTask task : tasks) {
                Future<File> future = executor.submit(task);
                futureResults.add(future);
            }

            try {
                executor.shutdown();
                if (!executor.awaitTermination(Configuration.exportTimeout.get(), TimeUnit.SECONDS)) {
                    logger.log(Level.SEVERE, "{0} ActionScript export limit reached", Helper.formatTimeToText(Configuration.exportTimeout.get()));
                }
            } catch (InterruptedException ex) {
            } finally {
                executor.shutdownNow();
            }

            for (int f = 0; f < futureResults.size(); f++) {
                try {
                    if (futureResults.get(f).isDone()) {
                        ret.add(futureResults.get(f).get());
                    }
                } catch (InterruptedException ex) {
                } catch (ExecutionException ex) {
                    logger.log(Level.SEVERE, "Error during ABC export", ex);
                }
            }
        }

        return ret;
    }
}
