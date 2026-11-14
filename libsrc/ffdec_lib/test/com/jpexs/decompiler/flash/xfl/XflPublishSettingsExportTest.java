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

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.EnableDebugger2Tag;
import com.jpexs.decompiler.flash.tags.EnableDebuggerTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.MetadataTag;
import com.jpexs.decompiler.flash.tags.ProtectTag;
import com.jpexs.decompiler.flash.tags.ScriptLimitsTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.flash.fla.reader.BinaryFlaDocument;
import com.jpexs.flash.fla.reader.BinaryFlaReader;
import com.jpexs.flash.fla.reader.FlaRecord;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import static org.testng.Assert.*;

public class XflPublishSettingsExportTest {

    private static final String XMP = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\"><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">"
            + "<rdf:Description rdf:about=\"\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">"
            + "<dc:description>Žluťoučký &amp; metadata</dc:description></rdf:Description></rdf:RDF></x:xmpmeta>";

    private SWF movie() {
        SWF swf = new SWF();
        swf.version = 10;
        swf.frameRate = 24;
        swf.frameCount = 1;
        return swf;
    }

    private SWF settingsMovie(int accelerationFlags) {
        SWF swf = movie();
        FileAttributesTag attributes = new FileAttributesTag(swf);
        attributes.useDirectBlit = (accelerationFlags & 1) != 0;
        attributes.useGPU = (accelerationFlags & 2) != 0;
        attributes.hasMetadata = true;
        swf.addTag(attributes);
        swf.addTag(new ProtectTag(swf));
        EnableDebugger2Tag debugger = new EnableDebugger2Tag(swf);
        debugger.setPassword("test-password");
        swf.addTag(debugger);
        ScriptLimitsTag limits = new ScriptLimitsTag(swf);
        limits.maxRecursionDepth = 256;
        limits.scriptTimeoutSeconds = 42;
        swf.addTag(limits);
        MetadataTag metadata = new MetadataTag(swf);
        metadata.xmlMetadata = XMP;
        swf.addTag(metadata);
        swf.addTag(new ShowFrameTag(swf));
        return swf;
    }

    private void export(SWF swf, Path file, FLAVersion version, boolean compressed) throws Exception {
        XFLExportSettings settings = new XFLExportSettings();
        settings.compressed = compressed;
        settings.exportScript = false;
        swf.exportXfl(new AbortRetryIgnoreHandler() {
            @Override
            public int handle(Throwable error) {
                throw new AssertionError(error);
            }

            @Override
            public AbortRetryIgnoreHandler getNewInstance() {
                return this;
            }
        }, file.toString(), "test.swf", "Test", "Test", "1", false, version, settings, null);
    }

    private Document document(Path file, String entry) throws Exception {
        try (ZipFile zip = new ZipFile(file.toFile())) {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(zip.getInputStream(zip.getEntry(entry)));
        }
    }

    private String setting(Document document, String name) {
        return document.getElementsByTagName(name).item(0).getTextContent();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> nativeSettings(BinaryFlaDocument document) {
        FlaRecord settings = (FlaRecord) document.getContents().get("publishSettings");
        List<FlaRecord> profiles = (List<FlaRecord>) settings.get("profiles");
        FlaRecord profile = profiles == null ? (FlaRecord) settings.get("legacyProperties") : profiles.get(0);
        List<FlaRecord> entries = (List<FlaRecord>) profile.get("entries");
        Map<String, String> values = new HashMap<>();
        for (FlaRecord entry : entries) {
            values.put((String) entry.get("key"), (String) entry.get("value"));
        }
        return values;
    }

    @DataProvider(name = "accelerationFlags")
    public Object[][] accelerationFlags() {
        return new Object[][]{{0, "0"}, {1, "1"}, {2, "2"}, {3, "2"}};
    }

    @Test(dataProvider = "accelerationFlags")
    public void keepsPublishSettingsAndXmpInZipAndBinaryFla(int flags, String expectedAcceleration) throws Exception {
        SWF swf = settingsMovie(flags);
        Path file = Files.createTempFile("publish-settings-", ".fla");
        try {
            export(swf, file, FLAVersion.CS6, true);
            Document publish = document(file, "PublishSettings.xml");
            assertEquals(setting(publish, "Protect"), "1");
            assertEquals(setting(publish, "DebuggingPermitted"), "1");
            assertEquals(setting(publish, "DebuggingPassword"), "", "Do not use a one-way hash as a plaintext password");
            assertEquals(setting(publish, "ScriptStuckDelay"), "42");
            assertEquals(setting(publish, "HardwareAcceleration"), expectedAcceleration);
            document(file, "META-INF/metadata.xml");
            try (ZipFile zip = new ZipFile(file.toFile())) {
                ByteArrayOutputStream data = new ByteArrayOutputStream();
                try (InputStream input = zip.getInputStream(zip.getEntry("META-INF/metadata.xml"))) {
                    byte[] buffer = new byte[1024];
                    for (int count; (count = input.read(buffer)) != -1;) {
                        data.write(buffer, 0, count);
                    }
                }
                assertEquals(new String(data.toByteArray(), StandardCharsets.UTF_8), XMP);
            }
            export(swf, file, FLAVersion.CS4, true);
            BinaryFlaDocument binary = new BinaryFlaReader().read(file.toFile());
            assertTrue(binary.getIssues().isEmpty(), binary.getIssues().toString());
            assertEquals(binary.getContents().get("metadataXml"), XMP);
            Map<String, String> settings = nativeSettings(binary);
            assertEquals(settings.get("Vector::Protect"), "1");
            assertEquals(settings.get("Vector::Debugging Permitted"), "1");
            assertEquals(settings.get("Vector::ScriptStuckDelay"), "42");
            assertEquals(settings.get("Vector::HardwareAcceleration"), expectedAcceleration);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void keepsXmpInUncompressedXfl() throws Exception {
        Path directory = Files.createTempDirectory("xmp-xfl-");
        try {
            export(settingsMovie(2), directory.resolve("document.xfl"), FLAVersion.CS6, false);
            Path project = directory.resolve("document");
            assertEquals(new String(Files.readAllBytes(project.resolve("META-INF/metadata.xml")), StandardCharsets.UTF_8), XMP);
            Document publish = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(project.resolve("PublishSettings.xml").toFile());
            assertEquals(setting(publish, "ScriptStuckDelay"), "42");
            assertEquals(setting(publish, "HardwareAcceleration"), "2");
        } finally {
            try (Stream<Path> paths = Files.walk(directory)) {
                for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) {
                    Files.delete(path);
                }
            }
        }
    }

    @Test
    public void preservesLegacyDebuggerFlagAndDefaultsWhenTagsAreAbsent() throws Exception {
        Path file = Files.createTempFile("debugger-settings-", ".fla");
        try {
            SWF swf = movie();
            swf.addTag(new ShowFrameTag(swf));
            export(swf, file, FLAVersion.CS6, true);
            Document publish = document(file, "PublishSettings.xml");
            assertEquals(setting(publish, "Protect"), "0");
            assertEquals(setting(publish, "DebuggingPermitted"), "0");
            assertEquals(setting(publish, "ScriptStuckDelay"), "15");
            assertEquals(setting(publish, "HardwareAcceleration"), "0");
            try (ZipFile zip = new ZipFile(file.toFile())) {
                assertNull(zip.getEntry("META-INF/metadata.xml"));
            }
            swf.addTag(0, new EnableDebuggerTag(swf));
            export(swf, file, FLAVersion.F5, true);
            Map<String, String> settings = nativeSettings(new BinaryFlaReader().read(file.toFile()));
            assertEquals(settings.get("Vector::Debugging Permitted"), "1");
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
