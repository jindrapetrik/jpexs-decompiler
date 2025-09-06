/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
import com.jpexs.helpers.ProgressListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * ActionScript 1/2 script exporter.
 *
 * @author JPEXS
 */
public class AS2ScriptExporter {

    private static final Logger logger = Logger.getLogger(AS2ScriptExporter.class.getName());

    /**
     * Constructor.
     */
    public AS2ScriptExporter() {
        
    }

    /**
     * Export ActionScript 2 scripts.
     * @param swf SWF
     * @param handler AbortRetryIgnoreHandler
     * @param outdir Output directory
     * @param exportSettings Export settings
     * @param parallel Parallel
     * @param evl EventListener
     * @return List of exported files
     * @throws IOException On I/O error
     */
    public List<File> exportActionScript2(SWF swf, AbortRetryIgnoreHandler handler, String outdir, ScriptExportSettings exportSettings, boolean parallel, EventListener evl) throws IOException {
        return exportAS2Scripts(handler, outdir, swf.getASMs(true), exportSettings, parallel, evl);
    }

    /**
     * Export ActionScript 2 scripts.
     * @param handler AbortRetryIgnoreHandler
     * @param outdir Output directory
     * @param asms ASM sources
     * @param exportSettings Export settings
     * @param parallel Parallel
     * @param evl EventListener
     * @return List of exported files
     * @throws IOException On I/O error
     */
    public List<File> exportAS2Scripts(AbortRetryIgnoreHandler handler, String outdir, Map<String, ASMSource> asms, ScriptExportSettings exportSettings, boolean parallel, EventListener evl) throws IOException {
        List<File> ret = new ArrayList<>();
        if (!outdir.endsWith(File.separator)) {
            outdir += File.separator;
        }

        Map<String, List<String>> existingNamesMap = new HashMap<>();
        int cnt = 1;
        List<ExportScriptTask> tasks = new ArrayList<>();
        String[] keys = asms.keySet().toArray(new String[asms.size()]);

        Set<SWF> swfsThatNeedUninitializedClassTraitsDetection = new HashSet<>();
        
        for (String key : keys) {
            ASMSource asm = asms.get(key);
            if (asm.getSwf().needsCalculatingAS2UninitializeClassTraits(asm)) {
                swfsThatNeedUninitializedClassTraitsDetection.add(asm.getSwf());
            }
        }
        
        ProgressListener progressListener = new ProgressListener() {
            @Override
            public void progress(int p) {
            }

            @Override
            public void status(String status) {
                if (evl != null) {
                    evl.handleEvent("uninitializedClassFields", status);
                }
            }
        };
        
        for (SWF swf : swfsThatNeedUninitializedClassTraitsDetection) {
            swf.getUninitializedClassFieldsDetector().addProgressListener(progressListener);
        }
        
        try {
            for (SWF swf : swfsThatNeedUninitializedClassTraitsDetection) {
                swf.calculateAs2UninitializedClassTraits();
            }
        } catch (InterruptedException ie) {
            return ret;
        }
        
        try {
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
                    CancellableWorker.call("as2scriptexport", new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            for (ExportScriptTask task : tasks) {
                                if (CancellableWorker.isInterrupted()) {
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

                        for (ExportScriptTask task : tasks) {
                            CancellableWorker.cancelThread(task.thread);
                        }
                    }
                } catch (InterruptedException ex) {
                    //ignored
                } finally {
                    executor.shutdownNow();                
                }

                for (int f = 0; f < futureResults.size(); f++) {
                    try {
                        if (futureResults.get(f).isDone()) {
                            ret.add(futureResults.get(f).get());
                        }
                    } catch (InterruptedException ex) {
                        //ignored
                    } catch (ExecutionException ex) {
                        if (!(ex.getCause() instanceof InterruptedException)) {
                            logger.log(Level.SEVERE, "Error during ActionScript export", ex);
                        }
                    }
                }            
            }

            return ret;
        } finally {
            for (SWF swf : swfsThatNeedUninitializedClassTraitsDetection) {
                swf.getUninitializedClassFieldsDetector().removeProgressListener(progressListener);
            }        
        }
    }
}
