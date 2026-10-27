/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.console;

import com.jpexs.decompiler.flash.SWF;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MxmlCommandLineTest {

    @DataProvider(name = "exportModes")
    public Object[][] exportModes() {
        return new Object[][]{{false}, {true}};
    }

    private void remove(Path directory) throws Exception {
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(path);
            }
        }
    }

    @Test(dataProvider = "exportModes")
    public void exportsMxmlToCommandLineOutputDirectory(boolean multipleExportTypes) throws Exception {
        Path directory = Files.createTempDirectory("mxml-cli-");
        try {
            Path output = directory.resolve("output");
            Path fixture = Paths.get("libsrc/ffdec_lib/testdata/mxml/Main.swf");
            try (InputStream input = new BufferedInputStream(Files.newInputStream(fixture))) {
                SWF swf = new SWF(input, false);
                Path exported = CommandLineArgumentParser.exportMxml(swf, output.toString(), multipleExportTypes).toPath();
                Assert.assertEquals(exported, output.resolve(multipleExportTypes ? "mxml/Main.mxml" : "Main.mxml"));
                Assert.assertTrue(Files.isRegularFile(exported));
            }
        } finally {
            remove(directory);
        }
    }

    @Test
    public void documentsMxmlExportInHelp() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8.name())) {
            CommandLineHelp.printCmdLineUsage(output, "export");
        }
        String help = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        Assert.assertTrue(Pattern.compile("(?m)^\\s*mxml\\s+-\\s+[^\\r\\n]*MXML").matcher(help).find());
    }
}
