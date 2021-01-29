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
 * License along with this library. */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.cache.ScriptDecompiledListener;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.HighlightedText;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.helpers.ImmediateFuture;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class DecompilerPool {

    private final ThreadPoolExecutor executor;

    public DecompilerPool() {
        int threadCount = Configuration.getParallelThreadCount();
        executor = new ThreadPoolExecutor(threadCount, threadCount,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
    }

    public Future<HighlightedText> submitTask(ASMSource src, ActionList actions, ScriptDecompiledListener<HighlightedText> listener) {
        Callable<HighlightedText> callable = new Callable<HighlightedText>() {
            @Override
            public HighlightedText call() throws Exception {
                if (listener != null) {
                    listener.onStart();
                }

                HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), true);
                writer.startFunction("!script");
                src.getActionScriptSource(writer, actions);
                writer.endFunction();

                HighlightedText result = new HighlightedText(writer);
                SWF swf = src.getSwf();
                if (swf != null) {
                    swf.as2Cache.put(src, result);
                }

                if (listener != null) {
                    listener.onComplete(result);
                }

                return result;
            }
        };

        return submit(callable);
    }

    public Future<HighlightedText> submitTask(ScriptPack pack, ScriptDecompiledListener<HighlightedText> listener) {
        Callable<HighlightedText> callable = new Callable<HighlightedText>() {
            @Override
            public HighlightedText call() throws Exception {
                if (listener != null) {
                    listener.onStart();
                }

                int scriptIndex = pack.scriptIndex;
                ScriptInfo script = null;
                if (scriptIndex > -1) {
                    script = pack.abc.script_info.get(scriptIndex);
                }
                boolean parallel = Configuration.parallelSpeedUp.get();
                HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), true);
                pack.toSource(writer, script == null ? null : script.traits.traits, new ConvertData(), ScriptExportMode.AS, parallel);

                HighlightedText result = new HighlightedText(writer);
                SWF swf = pack.getSwf();
                if (swf != null) {
                    swf.as3Cache.put(pack, result);
                }

                if (listener != null) {
                    listener.onComplete(result);
                }

                return result;
            }
        };

        return submit(callable);
    }

    private Future<HighlightedText> submit(Callable<HighlightedText> callable) {
        boolean parallel = Configuration.parallelSpeedUp.get();
        if (parallel) {
            Future<HighlightedText> f = executor.submit(callable);
            return f;
        } else {
            boolean cancelled = false;
            Throwable throwable = null;
            HighlightedText result = null;
            try {
                result = callable.call();
            } catch (InterruptedException ex) {
                cancelled = true;
            } catch (Exception ex) {
                throwable = ex;
            }

            return new ImmediateFuture<>(result, throwable, cancelled);
        }
    }

    public String getStat() {
        return "core: " + executor.getCorePoolSize()
                + " size: " + executor.getPoolSize()
                + " largest: " + executor.getLargestPoolSize()
                + " max: " + executor.getMaximumPoolSize()
                + " active: " + executor.getActiveCount()
                + " count: " + executor.getTaskCount()
                + " completed: " + executor.getCompletedTaskCount();
    }

    public HighlightedText decompile(ASMSource src, ActionList actions) throws InterruptedException {
        Future<HighlightedText> future = submitTask(src, actions, null);
        try {
            return future.get();
        } catch (InterruptedException ex) {
            future.cancel(true);
            throw ex;
        } catch (ExecutionException ex) {
            Logger.getLogger(DecompilerPool.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public HighlightedText decompile(ScriptPack pack) throws InterruptedException {
        Future<HighlightedText> future = submitTask(pack, null);
        try {
            return future.get();
        } catch (InterruptedException ex) {
            future.cancel(true);
            throw ex;
        } catch (ExecutionException ex) {
            Logger.getLogger(DecompilerPool.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public void shutdown() throws InterruptedException {
        executor.shutdown();
        if (!executor.awaitTermination(100, TimeUnit.SECONDS)) {
        }
    }
}
