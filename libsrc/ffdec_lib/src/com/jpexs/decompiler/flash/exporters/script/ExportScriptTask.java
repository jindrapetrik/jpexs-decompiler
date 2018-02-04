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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.exporters.script;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.RetryTask;
import com.jpexs.decompiler.flash.RunnableIOExResult;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.helpers.FileTextWriter;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import com.jpexs.helpers.stat.Statistics;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class ExportScriptTask implements Callable<File> {

    private static final Logger logger = Logger.getLogger(ExportScriptTask.class.getName());

    ASMSource asm;

    String directory;

    ScriptExportSettings exportSettings;

    String name;

    int index;

    int count;

    AbortRetryIgnoreHandler handler;

    long startTime;

    long stopTime;

    EventListener eventListener;

    public ExportScriptTask(AbortRetryIgnoreHandler handler, int index, int count, String name, ASMSource asm, String directory, ScriptExportSettings exportSettings, EventListener evl) {
        this.asm = asm;
        this.directory = directory;
        this.exportSettings = exportSettings;
        this.name = name;
        this.index = index;
        this.count = count;
        this.handler = handler;
        this.eventListener = evl;
    }

    @Override
    public File call() throws IOException, InterruptedException {
        String f = Path.combine(directory, name) + exportSettings.getFileExtension();
        RunnableIOExResult<File> rio = new RunnableIOExResult<File>() {
            @Override
            public void run() throws IOException, InterruptedException {
                startTime = System.currentTimeMillis();

                File file = new File(f);
                if (!exportSettings.singleFile) {
                    Path.createDirectorySafe(new File(directory));
                    if (file.exists() && !Configuration.overwriteExistingFiles.get()) {
                        this.result = file;
                        return;
                    }
                }

                try (FileTextWriter writer = exportSettings.singleFile ? null : new FileTextWriter(Configuration.getCodeFormatting(), new FileOutputStream(f))) {
                    FileTextWriter writer2 = exportSettings.singleFile ? exportSettings.singleFileWriter : writer;
                    ScriptExportMode exportMode = exportSettings.mode;
                    if (exportMode == ScriptExportMode.HEX) {
                        asm.getActionSourcePrefix(writer2);
                        asm.getActionBytesAsHex(writer2);
                        asm.getActionSourceSuffix(writer2);
                    } else if (exportMode == ScriptExportMode.PCODE_GRAPHVIZ) {
                        new PcodeGraphVizExporter().exportAs12(asm, writer2);
                    } else if (exportMode != ScriptExportMode.AS) {
                        asm.getActionSourcePrefix(writer2);
                        asm.getASMSource(exportMode, writer2, null);
                        asm.getActionSourceSuffix(writer2);
                    } else {
                        ActionList as;
                        try (Statistics s = new Statistics("ASMSource.getActions")) {
                            as = asm.getActions();
                        }

                        Action.setActionsAddresses(as, 0);

                        try (Statistics s = new Statistics("Action.actionsToSource")) {
                            asm.getActionScriptSource(writer2, as);
                        }
                    }
                }

                this.result = file;

                stopTime = System.currentTimeMillis();
            }
        };

        if (eventListener != null) {
            eventListener.handleExportingEvent("script", index, count, f);
        }

        new RetryTask(rio, handler).run();

        if (eventListener != null) {
            long time = stopTime - startTime;
            eventListener.handleExportedEvent("script", index, count, f + ", " + Helper.formatTimeSec(time));
        }

        return rio.result;
    }
}
