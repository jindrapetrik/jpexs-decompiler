/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * One export's diagnostic log. Every record is flushed before conversion continues,
 * so an abruptly terminated process leaves the last operation in the log.
 */
final class XFLExportLog implements AutoCloseable {

    static final XFLExportLog DISABLED = new XFLExportLog();

    private final BufferedWriter writer;
    private final Logger logger = Logger.getLogger(XFLConverter.class.getName());
    private final long start = System.nanoTime();
    private final Thread owner = Thread.currentThread();
    private final Handler handler;
    private String lastOperation = "Opening export log";
    private int problems;
    private boolean writeFailed;

    private XFLExportLog() {
        writer = null;
        handler = null;
    }

    XFLExportLog(File file) throws IOException {
        // FileOutputStream also works on an interrupted thread. A channel-backed
        // writer could close itself before the cancellation record is written.
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8));
        handler = new Handler() {
            private final SimpleFormatter formatter = new SimpleFormatter();

            @Override
            public void publish(LogRecord record) {
                // Do not mix diagnostics from simultaneous exports.
                if (Thread.currentThread() == owner && record.getLevel().intValue() >= Level.WARNING.intValue()) {
                    problem(record.getLevel().getName() + ": " + formatter.formatMessage(record), record.getThrown());
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        logger.addHandler(handler);
        progress("=== Export started ===");
    }

    synchronized void progress(String message) {
        if (writer == null) {
            return;
        }
        lastOperation = message;
        write(message, null);
    }

    synchronized void problem(String message, Throwable thrown) {
        if (writer == null) {
            return;
        }
        problems++;
        write(message + "\nLast operation: " + lastOperation, thrown);
    }

    synchronized void finished() {
        if (writer == null) {
            return;
        }
        progress("=== Export finished" + (problems == 0 ? " successfully" : " with " + problems + " warning/error record(s)") + " ===");
    }

    private void write(String message, Throwable thrown) {
        if (writeFailed) {
            return;
        }
        try {
            writer.write(OffsetDateTime.now() + " [" + (System.nanoTime() - start) / 1000000
                    + " ms] [" + Thread.currentThread().getName() + "] " + message);
            writer.newLine();
            if (thrown != null) {
                StringWriter trace = new StringWriter();
                thrown.printStackTrace(new PrintWriter(trace));
                writer.write(trace.toString());
            }
            writer.flush();
        } catch (IOException ex) {
            writeFailed = true;
            // Logging failures must not hide the original error or break conversion.
            Logger.getLogger(XFLExportLog.class.getName()).log(Level.WARNING, "Cannot write FLA/XFL export log", ex);
        }
    }

    AbortRetryIgnoreHandler wrap(AbortRetryIgnoreHandler delegate) {
        if (writer == null || delegate == null) {
            return delegate;
        }
        return new AbortRetryIgnoreHandler() {
            @Override
            public int handle(Throwable thrown) {
                problem("Export operation failed; waiting for abort/retry/ignore decision", thrown);
                int decision = delegate.handle(thrown);
                synchronized (XFLExportLog.this) {
                    write("Error handler decision: " + decision + " (0=abort, 1=retry, 2=ignore, 3=ignore all)", null);
                }
                return decision;
            }

            @Override
            public AbortRetryIgnoreHandler getNewInstance() {
                return wrap(delegate.getNewInstance());
            }
        };
    }

    @Override
    public synchronized void close() {
        if (writer == null) {
            return;
        }
        logger.removeHandler(handler);
        try {
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(XFLExportLog.class.getName()).log(Level.WARNING, "Cannot close FLA/XFL export log", ex);
        }
    }
}
