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
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class AS3ScriptExporter {

    private static final Logger logger = Logger.getLogger(AS3ScriptExporter.class.getName());

    public List<File> exportActionScript3(SWF swf, final AbortRetryIgnoreHandler handler, final String outdir, final ScriptExportSettings exportSettings, final boolean parallel, final EventListener evl) {
        final AtomicInteger cnt = new AtomicInteger(1);

        final List<File> ret = new ArrayList<>();
        final List<ScriptPack> packs = swf.getAS3Packs();

        if (!parallel || packs.size() < 2) {
            try {
                CancellableWorker.call(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        for (ScriptPack item : packs) {
                            if (Thread.currentThread().isInterrupted()) {
                                throw new InterruptedException();
                            }

                            if (!item.isSimple && Configuration.ignoreCLikePackages.get()) {
                                continue;
                            }

                            ExportPackTask task = new ExportPackTask(handler, cnt, packs.size(), item.getClassPath(), item, outdir, exportSettings, parallel, evl);
                            ret.add(task.call());
                        }
                        return null;
                    }
                }, Configuration.exportTimeout.get(), TimeUnit.SECONDS);
            } catch (TimeoutException ex) {
                logger.log(Level.SEVERE, Helper.formatTimeToText(Configuration.exportTimeout.get()) + " ActionScript export limit reached", ex);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error during ABC export", ex);
            }
        } else {
            ExecutorService executor = Executors.newFixedThreadPool(Configuration.getParallelThreadCount());
            List<Future<File>> futureResults = new ArrayList<>();
            for (ScriptPack item : packs) {
                if (!item.isSimple && Configuration.ignoreCLikePackages.get()) {
                    continue;
                }
                Future<File> future = executor.submit(new ExportPackTask(handler, cnt, packs.size(), item.getClassPath(), item, outdir, exportSettings, parallel, evl));
                futureResults.add(future);
            }

            try {
                executor.shutdown();
                if (!executor.awaitTermination(Configuration.exportTimeout.get(), TimeUnit.SECONDS)) {
                    logger.log(Level.SEVERE, Helper.formatTimeToText(Configuration.exportTimeout.get()) + " ActionScript export limit reached");
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
