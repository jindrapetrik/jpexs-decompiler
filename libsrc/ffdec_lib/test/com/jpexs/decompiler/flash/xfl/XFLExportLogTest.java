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
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.helpers.ProgressListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipFile;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class XFLExportLogTest {

    @DataProvider(name = "formats")
    public Object[][] formats() {
        return new Object[][]{{true, FLAVersion.CS6}, {false, FLAVersion.CS6}, {true, FLAVersion.F4}};
    }

    @DataProvider(name = "failures")
    public Object[][] failures() {
        return new Object[][]{{true, FLAVersion.CS6, false}, {false, FLAVersion.CS6, false}, {true, FLAVersion.F4, false},
            {true, FLAVersion.CS6, true}, {false, FLAVersion.CS6, true}, {true, FLAVersion.F4, true}};
    }

    private Path logPath(Path dir, boolean compressed) {
        return dir.resolve(compressed ? "movie.fla.export.log" : "movie/movie.xfl.export.log");
    }

    private String read(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

    private void cleanup(Path dir) throws IOException {
        Thread.interrupted();
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(path);
            }
        }
    }

    private void export(Path dir, boolean compressed, FLAVersion version,
            AbortRetryIgnoreHandler handler, ProgressListener listener) throws IOException, InterruptedException {
        SWF swf = new SWF();
        swf.frameCount = 1;
        swf.addTag(new ShowFrameTag(swf));
        XFLExportSettings settings = new XFLExportSettings();
        settings.compressed = compressed;
        settings.exportScript = false;
        new XFLConverter(true).convertSWF(handler, swf, "příliš žluťoučký.swf",
                dir.resolve(compressed ? "movie.fla" : "movie.xfl").toString(), settings,
                "Test", "Test", "1", false, version, listener);
    }

    @Test(dataProvider = "formats")
    public void defaultConverterUsesConfiguredLogging(boolean compressed, FLAVersion version) throws Exception {
        Path dir = Files.createTempDirectory("ffdec-export-log-default-");
        try {
            SWF swf = new SWF();
            swf.frameCount = 1;
            swf.addTag(new ShowFrameTag(swf));
            XFLExportSettings settings = new XFLExportSettings();
            settings.compressed = compressed;
            settings.exportScript = false;
            swf.exportXfl(null, dir.resolve(compressed ? "movie.fla" : "movie.xfl").toString(),
                    "movie.swf", "Test", "Test", "1", false, version, settings, null);
            assertEquals(Files.exists(logPath(dir, compressed)), XFLConverter.EXPORT_LOG_ENABLED);
        } finally {
            cleanup(dir);
        }
    }

    @Test(dataProvider = "formats")
    public void successfulExportHasSidecarAndPreservesPreviousRuns(boolean compressed, FLAVersion version) throws Exception {
        Path dir = Files.createTempDirectory("ffdec-export-log-");
        try {
            export(dir, compressed, version, null, null);
            Path log = logPath(dir, compressed);
            String first = read(log);
            assertTrue(first.contains("příliš žluťoučký.swf"), first);
            assertTrue(first.contains("Normalizing fonts and texts"), first);
            assertTrue(first.contains("scene Scene 1 (frames 1-1)"), first);
            assertTrue(first.contains("Export finished successfully"), first);
            if (!compressed) {
                assertTrue(Files.exists(log.resolveSibling("movie.xfl")));
                assertTrue(first.contains("File written:"), first);
            } else if (version == FLAVersion.CS6) {
                try (ZipFile zip = new ZipFile(dir.resolve("movie.fla").toFile())) {
                    assertNotNull(zip.getEntry("DOMDocument.xml"));
                    assertNull(zip.getEntry("movie.fla.export.log"));
                }
                assertTrue(first.contains("FLA archive committed:"), first);
            } else {
                assertTrue(first.contains("Binary FLA written:"), first);
            }
            export(dir, compressed, version, null, null);
            assertTrue(read(log).startsWith(first), "Earlier export diagnostics must be preserved");
        } finally {
            cleanup(dir);
        }
    }

    @Test(dataProvider = "failures")
    public void failureIsFlushedAndRethrownWithContext(boolean compressed, FLAVersion version, boolean fatal) throws Exception {
        Path dir = Files.createTempDirectory("ffdec-export-log-failure-");
        Throwable failure = fatal ? new OutOfMemoryError("simulated conversion failure")
                : new IllegalStateException("simulated conversion failure");
        Logger logger = Logger.getLogger(XFLConverter.class.getName());
        int handlers = logger.getHandlers().length;
        try {
            try {
                export(dir, compressed, version, null, new ProgressListener() {
                    @Override
                    public void progress(int percent) {
                    }

                    @Override
                    public void status(String status) {
                        if (status.equals("main timeline")) {
                            try {
                                String live = read(logPath(dir, compressed));
                                assertTrue(live.contains("main timeline: START"), live);
                                assertFalse(live.contains("Export finished"), live);
                            } catch (IOException ex) {
                                throw new AssertionError(ex);
                            }
                            if (failure instanceof Error) {
                                throw (Error) failure;
                            }
                            throw (RuntimeException) failure;
                        }
                    }
                });
                fail("Expected conversion failure");
            } catch (RuntimeException | Error ex) {
                assertSame(ex, failure);
            }
            String log = read(logPath(dir, compressed));
            assertTrue(log.contains("Export FAILED"), log);
            assertTrue(log.contains("Last operation: main timeline: START"), log);
            assertTrue(log.contains(failure.getClass().getName() + ": simulated conversion failure"), log);
            assertTrue(log.contains("at " + getClass().getName()), log);
            assertFalse(log.contains("Export finished"), log);
            assertEquals(logger.getHandlers().length, handlers, "Per-export handlers must be removed on failure");
        } finally {
            cleanup(dir);
        }
    }

    @Test(dataProvider = "formats")
    public void alreadyInterruptedThreadStillWritesCancellation(boolean compressed, FLAVersion version) throws Exception {
        Path dir = Files.createTempDirectory("ffdec-export-log-cancel-");
        try {
            Thread.currentThread().interrupt();
            try {
                export(dir, compressed, version, null, null);
                fail("Expected interruption");
            } catch (InterruptedException expected) {
                assertTrue(Thread.currentThread().isInterrupted());
                Thread.interrupted();
            }
            String log = read(logPath(dir, compressed));
            assertTrue(log.contains("Export interrupted/cancelled"), log);
            assertTrue(log.contains("java.lang.InterruptedException"), log);
            assertFalse(log.contains("Export finished"), log);
        } finally {
            cleanup(dir);
        }
    }

    @Test
    public void retryAndIgnoreAreLoggedWithoutReportingSuccess() throws Exception {
        Path dir = Files.createTempDirectory("ffdec-export-log-retry-");
        AtomicInteger attempts = new AtomicInteger();
        try {
            // A directory cannot be replaced by the FLA file.
            Files.createDirectory(dir.resolve("movie.fla"));
            export(dir, true, FLAVersion.CS6, new AbortRetryIgnoreHandler() {
                @Override
                public int handle(Throwable thrown) {
                    return attempts.incrementAndGet() == 1 ? RETRY : IGNORE;
                }

                @Override
                public AbortRetryIgnoreHandler getNewInstance() {
                    return this;
                }
            }, null);
            String log = read(logPath(dir, true));
            assertEquals(attempts.get(), 2);
            assertTrue(log.contains("Error handler decision: 1"), log);
            assertTrue(log.contains("Error handler decision: 2"), log);
            assertTrue(log.contains("Last operation: Committing FLA archive:"), log);
            assertTrue(log.contains("Export finished with 2 warning/error record(s)"), log);
            assertFalse(log.contains("Export finished successfully"), log);
        } finally {
            cleanup(dir);
        }
    }

    @Test
    public void warningsAreScopedToTheExportThreadAndIncludeStackTraces() throws Exception {
        Path dir = Files.createTempDirectory("ffdec-export-log-warning-");
        Logger logger = Logger.getLogger(XFLConverter.class.getName());
        int handlers = logger.getHandlers().length;
        try {
            Path file = dir.resolve("warnings.log");
            try (XFLExportLog log = new XFLExportLog(file.toFile())) {
                log.progress("Converting character ID=42");
                Thread unrelated = new Thread(() -> logger.warning("unrelated export"));
                unrelated.start();
                unrelated.join();
                logger.log(Level.WARNING, "recoverable failure", new IOException("broken image"));
                log.finished();
            }
            String log = read(file);
            assertFalse(log.contains("unrelated export"), log);
            assertTrue(log.contains("WARNING: recoverable failure"), log);
            assertTrue(log.contains("Last operation: Converting character ID=42"), log);
            assertTrue(log.contains("java.io.IOException: broken image"), log);
            assertTrue(log.contains("Export finished with 1 warning/error record(s)"), log);
            assertEquals(logger.getHandlers().length, handlers);
            Files.delete(file); // Checks that the writer is closed on Windows.
        } finally {
            cleanup(dir);
        }
    }
}
