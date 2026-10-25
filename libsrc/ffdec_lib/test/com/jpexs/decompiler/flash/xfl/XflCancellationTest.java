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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ActionTreeOperation;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.ProgressListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class XflCancellationTest {

    @DataProvider(name = "formats")
    public Object[][] formats() {
        return new Object[][]{{true}, {false}};
    }

    private SWF movie() {
        SWF swf = new SWF();
        swf.frameCount = 1;
        swf.addTag(new ShowFrameTag(swf));
        return swf;
    }

    private void export(SWF swf, Path dir, boolean compressed, ProgressListener listener) throws Exception {
        XFLExportSettings settings = new XFLExportSettings();
        settings.compressed = compressed;
        swf.exportXfl(null, dir.resolve(compressed ? "movie.fla" : "movie.xfl").toString(),
                "movie.swf", "Test", "Test", "1", false, FLAVersion.CS6, settings, listener);
    }

    private void assertNoDocument(Path dir) throws Exception {
        assertFalse(Files.exists(dir.resolve("movie.fla")), "Cancelled export must not write the FLA");
        assertFalse(Files.exists(dir.resolve("movie/DOMDocument.xml")), "Cancelled export must not write the XFL document");
    }

    private void cleanup(Path dir) throws Exception {
        Thread.interrupted();
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(path);
            }
        }
    }

    @Test(dataProvider = "formats")
    public void alreadyInterruptedExportDoesNotStart(boolean compressed) throws Exception {
        Path dir = Files.createTempDirectory("ffdec-cancel-");
        try {
            SWF swf = movie();
            Thread.currentThread().interrupt();
            try {
                export(swf, dir, compressed, null);
                fail("Export ignored interruption");
            } catch (InterruptedException expected) {
                assertNoDocument(dir);
            }
        } finally {
            cleanup(dir);
        }
    }

    @Test(dataProvider = "formats")
    public void interruptDuringTimelineStopsProgressAndWriting(boolean compressed) throws Exception {
        Path dir = Files.createTempDirectory("ffdec-cancel-");
        AtomicInteger updatesAfterCancel = new AtomicInteger();
        AtomicInteger cancellations = new AtomicInteger();
        try {
            try {
                export(movie(), dir, compressed, new ProgressListener() {
                    @Override
                    public void progress(int percent) {
                    }

                    @Override
                    public void status(String status) {
                        if (cancellations.get() != 0) {
                            updatesAfterCancel.incrementAndGet();
                        } else if (status.equals("main timeline")) {
                            cancellations.incrementAndGet();
                            Thread.currentThread().interrupt();
                        }
                    }
                });
                fail("Export continued after cancellation");
            } catch (InterruptedException expected) {
                assertEquals(cancellations.get(), 1);
                assertEquals(updatesAfterCancel.get(), 0);
                assertNoDocument(dir);
            }
        } finally {
            cleanup(dir);
        }
    }

    @Test(dataProvider = "formats")
    public void scriptInterruptionEscapesTheConverter(boolean compressed) throws Exception {
        Path dir = Files.createTempDirectory("ffdec-cancel-");
        SWF swf = new SWF();
        swf.frameCount = 1;
        InterruptedException interruption = new InterruptedException("script cancelled");
        swf.addTag(new DoActionTag(swf) {
            @Override
            public GraphTextWriter getActionScriptSource(GraphTextWriter writer, ActionList actions,
                    List<ActionTreeOperation> treeOperations) throws InterruptedException {
                throw interruption;
            }
        });
        swf.addTag(new ShowFrameTag(swf));
        try {
            try {
                export(swf, dir, compressed, null);
                fail("Script interruption was swallowed");
            } catch (InterruptedException expected) {
                assertSame(expected, interruption);
                assertNoDocument(dir);
            }
        } finally {
            cleanup(dir);
        }
    }

    @Test(dataProvider = "formats")
    public void workerCancellationSurvivesClearedThreadInterrupt(boolean compressed) throws Exception {
        Path dir = Files.createTempDirectory("ffdec-cancel-");
        CountDownLatch reachedTimeline = new CountDownLatch(1);
        CountDownLatch resume = new CountDownLatch(1);
        CountDownLatch exited = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicInteger cancellations = new AtomicInteger();
        AtomicInteger updatesAfterCancel = new AtomicInteger();
        CancellableWorker<Void> worker = new CancellableWorker<Void>("test FLA cancellation") {
            @Override
            protected Void doInBackground() {
                try {
                    export(movie(), dir, compressed, new ProgressListener() {
                        @Override
                        public void progress(int percent) {
                        }

                        @Override
                        public void status(String status) {
                            if (cancellations.get() != 0) {
                                updatesAfterCancel.incrementAndGet();
                            } else if (status.equals("main timeline")) {
                                reachedTimeline.countDown();
                                try {
                                    resume.await();
                                } catch (InterruptedException expected) {
                                    // Simulate code that consumes the thread interrupt.
                                }
                                Thread.interrupted();
                            }
                        }
                    });
                    failure.set(new AssertionError("Cancelled worker continued exporting"));
                } catch (InterruptedException expected) {
                } catch (Throwable ex) {
                    failure.set(ex);
                } finally {
                    exited.countDown();
                }
                return null;
            }
        };
        try {
            worker.execute();
            assertTrue(reachedTimeline.await(10, TimeUnit.SECONDS), "Export did not reach the timeline");
            cancellations.incrementAndGet();
            worker.userCancel(true);
            resume.countDown();
            assertTrue(exited.await(10, TimeUnit.SECONDS), "Worker did not exit after cancellation");
            assertNull(failure.get());
            assertTrue(worker.isCancelled());
            assertEquals(cancellations.get(), 1);
            assertEquals(updatesAfterCancel.get(), 0);
            assertNoDocument(dir);
        } finally {
            worker.cancel(true);
            resume.countDown();
            if (exited.await(10, TimeUnit.SECONDS)) {
                cleanup(dir);
            }
        }
    }
}
