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
package com.jpexs.decompiler.flash.exporters.script;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.RetryTask;
import com.jpexs.decompiler.flash.RunnableIOExResult;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.helpers.Helper;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 *
 * @author JPEXS
 */
public class ExportPackTask implements Callable<File> {

    ScriptPack pack;

    File file;

    ScriptExportSettings exportSettings;

    ClassPath path;

    int index;

    int count;

    boolean parallel;

    AbortRetryIgnoreHandler handler;

    long startTime;

    long stopTime;

    EventListener eventListener;

    public ExportPackTask(AbortRetryIgnoreHandler handler, int index, int count, ClassPath path, ScriptPack pack, File file, ScriptExportSettings exportSettings, boolean parallel, EventListener evl) {
        this.pack = pack;
        this.file = file;
        this.exportSettings = exportSettings;
        this.path = path;
        this.index = index;
        this.count = count;
        this.parallel = parallel;
        this.handler = handler;
        this.eventListener = evl;
    }

    @Override
    public File call() throws IOException, InterruptedException {
        RunnableIOExResult<File> rio = new RunnableIOExResult<File>() {
            @Override
            public void run() throws IOException, InterruptedException {
                startTime = System.currentTimeMillis();
                this.result = pack.export(file, exportSettings, parallel);
                stopTime = System.currentTimeMillis();
            }
        };

        if (eventListener != null) {
            eventListener.handleExportingEvent("script", index, count, path);
        }

        new RetryTask(rio, handler).run();

        if (eventListener != null) {
            long time = stopTime - startTime;
            eventListener.handleExportedEvent("script", index, count, path + ", " + Helper.formatTimeSec(time));
        }

        return rio.result;
    }
}
