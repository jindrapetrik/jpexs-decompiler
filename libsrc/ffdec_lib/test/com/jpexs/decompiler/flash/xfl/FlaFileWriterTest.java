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
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.flash.fla.converter.FlaFormatVersion;
import com.jpexs.flash.fla.reader.BinaryFlaReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class FlaFileWriterTest {

    @DataProvider(name = "failures")
    public Object[][] failures() {
        return new Object[][]{{true, false}, {false, false}, {true, true}, {false, true}};
    }

    @DataProvider(name = "destinations")
    public Object[][] destinations() {
        return new Object[][]{{true}, {false}};
    }

    @DataProvider(name = "formats")
    public Object[][] formats() {
        return new Object[][]{{FLAVersion.F4}, {FLAVersion.CS6}};
    }

    private byte[] previousFile(Path output, boolean exists) throws Exception {
        byte[] previous = new byte[16385];
        Arrays.fill(previous, (byte) 0x5a);
        if (exists) {
            Files.write(output, previous);
        }
        return previous;
    }

    private void assertDestination(Path output, boolean existed, byte[] previous) throws Exception {
        if (existed) {
            assertEquals(Files.readAllBytes(output), previous, "Previous file must survive failure");
        } else {
            assertFalse(Files.exists(output), "Failure must not publish an incomplete FLA");
        }
        assertFileCount(output.getParent(), existed ? 1 : 0);
    }

    private void assertFileCount(Path directory, long count) throws Exception {
        try (Stream<Path> files = Files.list(directory)) {
            assertEquals(files.filter(path -> !path.getFileName().toString().equals("document.fla.export.log")).count(),
                    count, "Temporary files must be cleaned up (the diagnostic log is retained)");
        }
    }

    @Test(dataProvider = "failures")
    public void failedWriteOrClosePreservesDestination(boolean existed, boolean failOnClose) throws Exception {
        Path directory = Files.createTempDirectory("ffdec-fla-failure-");
        Path output = directory.resolve("document.fla");
        byte[] previous = previousFile(output, existed);
        IOException failure = new IOException("simulated disk error");
        try {
            try (FlaFileWriter file = new FlaFileWriter(output.toFile())) {
                try (OutputStream out = new FilterOutputStream(new FileOutputStream(file.getFile())) {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        if (failOnClose) {
                            throw failure;
                        }
                    }
                }) {
                    out.write(new byte[]{1, 2, 3});
                    if (!failOnClose) {
                        throw failure;
                    }
                }
                file.commit();
                fail("Write failure was swallowed");
            } catch (IOException expected) {
                assertSame(expected, failure);
            }
            assertDestination(output, existed, previous);
        } finally {
            Files.deleteIfExists(output);
            Files.delete(directory);
        }
    }

    @Test(dataProvider = "destinations")
    public void cancellationBeforeReplacementPreservesDestination(boolean existed) throws Exception {
        Path directory = Files.createTempDirectory("ffdec-fla-cancel-write-");
        Path output = directory.resolve("document.fla");
        byte[] previous = previousFile(output, existed);
        try {
            try (FlaFileWriter file = new FlaFileWriter(output.toFile())) {
                Files.write(file.getFile().toPath(), new byte[]{1, 2, 3});
                Thread.currentThread().interrupt();
                file.commit();
                fail("Cancellation was ignored");
            } catch (InterruptedException expected) {
                // The existing destination must remain untouched.
            } finally {
                Thread.interrupted();
            }
            assertDestination(output, existed, previous);
        } finally {
            Files.deleteIfExists(output);
            Files.delete(directory);
        }
    }

    @Test(dataProvider = "destinations")
    public void invalidBinaryConversionReportsFailureAndPreservesDestination(boolean existed) throws Exception {
        Path directory = Files.createTempDirectory("ffdec-fla-invalid-input-");
        Path input = directory.resolve("input.fla");
        Path output = directory.resolve("document.fla");
        byte[] previous = previousFile(output, existed);
        try {
            try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(input))) {
                zip.putNextEntry(new ZipEntry("missing-document.txt"));
                zip.write(1);
            }
            try {
                XFLConverter.convertToBinaryFla(input.toFile(), output.toFile(), FlaFormatVersion.F4, "windows-1252");
                fail("Invalid XFL must fail conversion");
            } catch (IOException expected) {
                assertTrue(expected.getMessage().contains("DOMDocument.xml"), expected.toString());
            }
            // Deleting the input also checks that the ZIP handle was closed on Windows.
            Files.delete(input);
            assertDestination(output, existed, previous);
        } finally {
            Files.deleteIfExists(input);
            Files.deleteIfExists(output);
            Files.delete(directory);
        }
    }

    private void export(Path output, FLAVersion version) throws Exception {
        SWF swf = new SWF();
        swf.frameCount = 1;
        swf.addTag(new ShowFrameTag(swf));
        XFLExportSettings settings = new XFLExportSettings();
        settings.compressed = true;
        settings.exportScript = false;
        settings.tweenDetectionQuality = 0;
        swf.exportXfl(null, output.toString(), "movie.swf", "Test", "Test", "1", false, version, settings, null);
    }

    @Test(dataProvider = "formats")
    public void successfulExportReplacesTheWholeFile(FLAVersion version) throws Exception {
        Path directory = Files.createTempDirectory("ffdec-fla-replace-");
        Path output = directory.resolve("document.fla");
        previousFile(output, true);
        try {
            export(output, version);
            if (version == FLAVersion.CS6) {
                try (ZipFile zip = new ZipFile(output.toFile())) {
                    assertNotNull(zip.getEntry("DOMDocument.xml"));
                    assertNotNull(zip.getEntry("PublishSettings.xml"));
                    for (ZipEntry entry : java.util.Collections.list(zip.entries())) {
                        try (java.io.InputStream in = zip.getInputStream(entry)) {
                            byte[] buffer = new byte[4096];
                            while (in.read(buffer) != -1) {
                                // Read to EOF to verify the compressed entries and CRCs.
                            }
                        }
                    }
                }
            } else {
                assertEquals(new BinaryFlaReader().read(output.toFile()).getContentsRevision(), FlaFormatVersion.F4.contentsRevision);
            }
            assertFileCount(directory, 1);
        } finally {
            Files.deleteIfExists(output);
            Files.deleteIfExists(directory.resolve("document.fla.export.log"));
            Files.delete(directory);
        }
    }

    @Test(dataProvider = "formats")
    public void lockedDestinationReportsFailureAndRemainsIntact(FLAVersion version) throws Exception {
        if (!System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows")) {
            throw new SkipException("Windows file sharing test");
        }
        Path directory = Files.createTempDirectory("ffdec-fla-locked-");
        Path output = directory.resolve("document.fla");
        byte[] previous = previousFile(output, true);
        try {
            try (FileInputStream locked = new FileInputStream(output.toFile())) {
                try {
                    export(output, version);
                    fail("Export must report that the destination cannot be replaced");
                } catch (IOException expected) {
                    assertEquals(locked.read(), previous[0] & 255);
                }
                assertDestination(output, true, previous);
            }
        } finally {
            Files.deleteIfExists(output);
            Files.deleteIfExists(directory.resolve("document.fla.export.log"));
            Files.delete(directory);
        }
    }
}
