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
import com.jpexs.decompiler.flash.RetryTask;
import com.jpexs.decompiler.flash.RunnableIOExResult;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.helpers.Helper;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author JPEXS
 */
public class ExportPackTask implements Callable<File> {

    ScriptPack pack;

    String directory;

    ScriptExportSettings exportSettings;

    ClassPath path;

    AtomicInteger index;

    int count;

    boolean parallel;

    AbortRetryIgnoreHandler handler;

    long startTime;

    long stopTime;

    EventListener eventListener;

    public ExportPackTask(AbortRetryIgnoreHandler handler, AtomicInteger index, int count, ClassPath path, ScriptPack pack, String directory, ScriptExportSettings exportSettings, boolean parallel, EventListener evl) {
        this.pack = pack;
        this.directory = directory;
        this.exportSettings = exportSettings;
        this.path = path;
        this.index = index;
        this.count = count;
        this.parallel = parallel;
        this.handler = handler;
        this.eventListener = evl;
    }

    @Override
    public File call() throws Exception {
        RunnableIOExResult<File> rio = new RunnableIOExResult<File>() {
            @Override
            public void run() throws IOException {
                startTime = System.currentTimeMillis();
                this.result = pack.export(directory, exportSettings, parallel);
                stopTime = System.currentTimeMillis();
            }
        };
        int currentIndex = index.getAndIncrement();
        if (eventListener != null) {
            synchronized (ABC.class) {
                eventListener.handleExportingEvent("script", currentIndex, count, path);
            }
        }
        new RetryTask(rio, handler).run();
        if (eventListener != null) {
            synchronized (ABC.class) {
                long time = stopTime - startTime;
                eventListener.handleExportedEvent("script", currentIndex, count, path + ", " + Helper.formatTimeSec(time));
            }
        }
        return rio.result;
    }
}
