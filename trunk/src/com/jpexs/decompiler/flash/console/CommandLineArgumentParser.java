/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.console;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFBundle;
import com.jpexs.decompiler.flash.SWFSourceInfo;
import com.jpexs.decompiler.flash.abc.RenameType;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.ConfigurationItem;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.xfl.FLAVersion;
import com.jpexs.decompiler.graph.ExportMode;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.streams.SeekableInputStream;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class CommandLineArgumentParser {

    private static boolean commandLineMode = false;

    @SuppressWarnings("unchecked")
    private static final ConfigurationItem<Boolean>[] commandlineConfigBoolean = new ConfigurationItem[]{
        Configuration.decompile,
        Configuration.parallelSpeedUp,
        Configuration.internalFlashViewer,
        Configuration.autoDeobfuscate,
        Configuration.cacheOnDisk
    };

    public static boolean isCommandLineMode() {
        return commandLineMode;
    }

    //(as|pcode|pcodehex|hex|image|shape|movie|sound|binaryData|text|textplain|all|all_as|all_pcode|all_pcodehex|all_hex)
    public static void printCmdLineUsage() {
        System.out.println("Commandline arguments:");
        System.out.println(" 1) -help | --help | /?");
        System.out.println(" ...shows commandline arguments (this help)");
        System.out.println(" 2) <infile>");
        System.out.println(" ...opens SWF file with the decompiler GUI");
        System.out.println(" 3) -proxy [-P<port>]");
        System.out.println("  ...auto start proxy in the tray. Optional parameter -P specifies port for proxy. Defaults to 55555. ");
        System.out.println(" 4) -export <itemtypes> <outdirectory> <infile> [-selectas3class <class1> <class2> ...]");
        System.out.println("  ...export <infile> sources to <outdirectory> ");
        System.out.println("     Values for <itemtypes> parameter:");
        System.out.println("        script - Scripts (Default format: ActionScript source)");
        System.out.println("               - For this type, optional \"-selectas3class\" parameter can be passed to export only selected classes (ActionScript 3 only)");
        System.out.println("        image - Images (Default format: PNG/JPEG)");
        System.out.println("        shape - Shapes (Default format: SVG)");
        System.out.println("        movie - Movies (Default format: FLV without sound)");
        System.out.println("        sound - Sounds (Default format: MP3/WAV/FLV only sound)");
        System.out.println("        binaryData - Binary data (Default format:  Raw data)");
        System.out.println("        text - Texts (Default format: Formatted text)");
        System.out.println("        all - Every resource");
        System.out.println("        fla - Everything to FLA compressed format");
        System.out.println("        xfl - Everything to uncompressed FLA format (XFL)");
        System.out.println("   You can export multiple types of items by using colon \",\"");
        System.out.println("      DO NOT PUT space between comma (,) and next value.");
        System.out.println();
        System.out.println("   Old DEPRECATED aliases include: (please use basic itemtypes and -format parameter instead)");
        System.out.println("        as, pcode, pcodehex, hex, all_as, all_pcode, all_pcodehex, textplain");
        System.out.println();
        System.out.println(" 5) -format <formats>");
        System.out.println("  ...sets output formats for export");
        System.out.println("    Values for <formats> parameter:");
        System.out.println("         script:as - ActionScript source");
        System.out.println("         script:pcode - ActionScript P-code");
        System.out.println("         script:pcodehex - ActionScript P-code with hex");
        System.out.println("         script:hex - ActionScript Hex only");
        System.out.println("         text:plain - Plain text format for Texts");
        System.out.println("         text:formatted - Formatted text format for Texts");
        System.out.println("         fla:<flaversion> or xfl:<flaversion> - Specify FLA format version");
        System.out.println("            - values for <flaversion>: cs5,cs5.5,cs6,cc");

        System.out.println("      You can set multiple formats at once using comma (,)");
        System.out.println("      DO NOT PUT space between comma (,) and next value.");
        System.out.println("      The prefix with colon (:) is neccessary.");
        System.out.println(" 6) -dumpSWF <infile>");
        System.out.println("  ...dumps list of SWF tags to console");
        System.out.println(" 7) -compress <infile> <outfile>");
        System.out.println("  ...Compress SWF <infile> and save it to <outfile>");
        System.out.println(" 8) -decompress <infile> <outfile>");
        System.out.println("  ...Decompress <infile> and save it to <outfile>");
        System.out.println(" 9) -extract <infile> [nocheck] [(all|biggest)]");
        System.out.println("  ...Extracts SWF files from ZIP or other binary files");
        System.out.println(" 10) -renameInvalidIdentifiers (typeNumber|randomWord) <infile> <outfil>e");
        System.out.println("  ...Renames the invalid identifiers in <infile> and save it to <outfile>");
        System.out.println(" 11) -config key=value[,key2=value2][,key3=value3...] [other parameters]");
        System.out.print("  ...Sets configuration values. Available keys[current setting]:");
        for (ConfigurationItem item : commandlineConfigBoolean) {
            System.out.print(" " + item + "[" + item.get() + "]");
        }
        System.out.println("");
        System.out.println("    Values are boolean, you can use 0/1, true/false, on/off or yes/no.");
        System.out.println("    If no other parameters passed, configuration is saved. Otherwise it is used only once.");
        System.out.println("    DO NOT PUT space between comma (,) and next value.");
        System.out.println(" 12) -onerror (abort|retryN|ignore)");
        System.out.println("  ...error handling mode. \"abort\" stops the exporting, \"retry\" tries the exporting N times, \"ignore\" ignores the current file");
        System.out.println(" 13) -timeout <N>");
        System.out.println("  ...decompilation timeout for a single method in AS3 or single action in AS1/2 in seconds");
        System.out.println(" 14) -exportTimeout <N>");
        System.out.println("  ...total export timeout in seconds");
        System.out.println(" 15) -exportFileTimeout <N>");
        System.out.println("  ...export timeout for a single AS3 class in seconds");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("java -jar ffdec.jar myfile.swf");
        System.out.println("java -jar ffdec.jar -proxy");
        System.out.println("java -jar ffdec.jar -proxy -P1234");
        System.out.println("java -jar ffdec.jar -export script \"C:\\decompiled\" myfile.swf");
        System.out.println("java -jar ffdec.jar -export script \"C:\\decompiled\" myfile.swf -selectas3class com.example.MyClass com.example.SecondClass");
        System.out.println("java -jar ffdec.jar -format script:pcode -export script \"C:\\decompiled\" myfile.swf");
        System.out.println("java -jar ffdec.jar -format script:pcode,text:plain -export script,text,image \"C:\\decompiled\" myfile.swf");
        System.out.println("java -jar ffdec.jar -format fla:cs5.5 -export fla \"C:\\sources\\myfile.fla\" myfile.swf");
        System.out.println("java -jar ffdec.jar -dumpSWF myfile.swf");
        System.out.println("java -jar ffdec.jar -compress myfile.swf myfiledec.swf");
        System.out.println("java -jar ffdec.jar -decompress myfiledec.swf myfile.swf");
        System.out.println("java -jar ffdec.jar -onerror ignore -export script \"C:\\decompiled\" myfile.swf");
        System.out.println("java -jar ffdec.jar -onerror retry 5 -export script \"C:\\decompiled\" myfile.swf");
        System.out.println("java -jar ffdec.jar -config autoDeobfuscate=1,parallelSpeedUp=0 -export script \"C:\\decompiled\" myfile.swf");
        System.out.println("");
        System.out.println("Instead of \"java -jar ffdec.jar\" you can use ffdec.bat on Windows, ffdec.sh on Linux/MacOs");
    }

    /**
     * Parses the console arguments
     *
     * @param arguments
     * @return path to the file which should be opened or null
     * @throws java.io.IOException
     */
    public static String parseArguments(String[] arguments) throws IOException {
        Level traceLevel = Level.WARNING;
        Queue<String> args = new LinkedList<>(Arrays.asList(arguments));
        AbortRetryIgnoreHandler handler = null;
        Map<String, String> format = new HashMap<>();

        String nextParam;
        OUTER:
        while (true) {
            nextParam = args.remove();
            if (nextParam != null) {
                nextParam = nextParam.toLowerCase();
            }
            switch (nextParam) {
                case "-format":
                    format = parseFormat(args);
                    break;
                case "-config":
                    parseConfig(args);
                    if (args.isEmpty()) {
                        Configuration.saveConfig();
                        System.out.println("Configuration saved");
                        return null;
                    }
                    break;
                case "-onerror":
                    handler = parseOnError(args);
                    break;
                case "-timeout":
                    parseTimeout(args);
                    break;
                case "-exporttimeout":
                    parseExportTimeout(args);
                    break;
                case "-exportfiletimeout":
                    parseExportFileTimeout(args);
                    break;
                case "-affinity":
                    parseAffinity(args);
                    break;
                case "-priority":
                    parsePriority(args);
                    break;
                case "-verbose":
                    traceLevel = Level.FINE;
                    break;
                case "-debug":
                    Configuration.debugMode.set(true);
                    break;
                default:
                    break OUTER;
            }
            if (args.isEmpty()) {
                return null;
            }
        }
        if (nextParam.equals("-removefromcontextmenu")) {
            ContextMenuTools.addToContextMenu(false, true);
            System.exit(0);
        } else if (nextParam.equals("-addtocontextmenu")) {
            ContextMenuTools.addToContextMenu(true, true);
            System.exit(0);
        } else if (nextParam.equals("-proxy")) {
            parseProxy(args);
        } else if (nextParam.equals("-export")) {
            parseExport(args, handler, traceLevel, format);
        } else if (nextParam.equals("-compress")) {
            parseCompress(args);
        } else if (nextParam.equals("-decompress")) {
            parseDecompress(args);
        } else if (nextParam.equals("-extract")) {
            parseExtract(args);
        } else if (nextParam.equals("-renameinvalididentifiers")) {
            parseRenameInvalidIdentifiers(args);
        } else if (nextParam.equals("-dumpswf")) {
            parseDumpSwf(args);
        } else if (nextParam.equals("-help") || nextParam.equals("--help") || nextParam.equals("/?")) {
            printHeader();
            printCmdLineUsage();
            System.exit(0);
        } else if (args.isEmpty()) {
            return nextParam;
        } else {
            badArguments();
        }

        return null;
    }

    public static void printHeader() {
        System.out.println(ApplicationInfo.applicationVerName);
        for (int i = 0; i < ApplicationInfo.applicationVerName.length(); i++) {
            System.out.print("-");
        }
        System.out.println();
    }

    public static void badArguments() {
        System.err.println("Error: Bad Commandline Arguments!");
        printCmdLineUsage();
        System.exit(1);
    }

    private static void setConfigurations(String cfgStr) {
        String[] cfgs;
        if (cfgStr.contains(",")) {
            cfgs = cfgStr.split(",");
        } else {
            cfgs = new String[]{cfgStr};
        }

        for (String c : cfgs) {
            String[] cp;
            if (c.contains("=")) {
                cp = c.split("=");
            } else {
                cp = new String[]{c, "1"};
            }
            String key = cp[0];
            String value = cp[1];
            if (key.toLowerCase().equals("paralelSpeedUp".toLowerCase())) {
                key = "parallelSpeedUp";
            }
            for (ConfigurationItem<Boolean> item : commandlineConfigBoolean) {
                if (key.toLowerCase().equals(item.getName().toLowerCase())) {
                    Boolean bValue = parseBooleanConfigValue(value);
                    if (bValue != null) {
                        System.out.println("Config " + item.getName() + " set to " + bValue);
                        item.set(bValue);
                    }
                }
            }
        }
    }

    private static Boolean parseBooleanConfigValue(String value) {
        if (value == null) {
            return null;
        }

        Boolean bValue = null;
        value = value.toLowerCase();
        if (value.equals("0") || value.equals("false") || value.equals("no") || value.equals("off")) {
            bValue = false;
        }
        if (value.equals("1") || value.equals("true") || value.equals("yes") || value.equals("on")) {
            bValue = true;
        }
        return bValue;
    }

    private static ExportMode strToExportFormat(String exportFormatStr) {
        switch (exportFormatStr) {
            case "pcode":
                return ExportMode.PCODE;
            case "pcodehex":
                return ExportMode.PCODEWITHHEX;
            case "hex":
                return ExportMode.HEX;
            default:
                return ExportMode.SOURCE;
        }
    }

    private static void parseConfig(Queue<String> args) {
        if (args.isEmpty()) {
            System.err.println("Config values expected");
            badArguments();
        }
        setConfigurations(args.remove());
    }

    private static AbortRetryIgnoreHandler parseOnError(Queue<String> args) {
        int errorMode = AbortRetryIgnoreHandler.UNDEFINED;
        int retryCount = 0;

        if (args.isEmpty()) {
            System.err.println("onerror parameter expected");
            badArguments();
        }
        String errorModeParameter = args.remove();
        switch (errorModeParameter) {
            case "abort":
                errorMode = AbortRetryIgnoreHandler.ABORT;
                break;
            case "retry":
                errorMode = AbortRetryIgnoreHandler.RETRY;
                if (args.isEmpty()) {
                    System.err.println("onerror retry count parameter expected");
                    badArguments();
                }

                try {
                    retryCount = Integer.parseInt(args.remove());
                } catch (NumberFormatException nex) {
                    System.err.println("Bad retry count number");
                }
                break;
            case "ignore":
                errorMode = AbortRetryIgnoreHandler.IGNORE;
                break;
        }

        return new ConsoleAbortRetryIgnoreHandler(errorMode, retryCount);
    }

    private static void parseTimeout(Queue<String> args) {
        if (args.isEmpty()) {
            System.err.println("timeout parameter expected");
            badArguments();
        }
        try {
            int timeout = Integer.parseInt(args.remove());
            Configuration.decompilationTimeoutSingleMethod.set(timeout);
        } catch (NumberFormatException nex) {
            System.err.println("Bad timeout value");
        }
    }

    private static void parseExportTimeout(Queue<String> args) {
        if (args.isEmpty()) {
            System.err.println("timeout parameter expected");
            badArguments();
        }
        try {
            int timeout = Integer.parseInt(args.remove());
            Configuration.exportTimeout.set(timeout);
        } catch (NumberFormatException nex) {
            System.err.println("Bad timeout value");
        }
    }

    private static void parseExportFileTimeout(Queue<String> args) {
        if (args.isEmpty()) {
            System.err.println("timeout parameter expected");
            badArguments();
        }
        try {
            int timeout = Integer.parseInt(args.remove());
            Configuration.decompilationTimeoutFile.set(timeout);
        } catch (NumberFormatException nex) {
            System.err.println("Bad timeout value");
        }
    }

    private static void parseAffinity(Queue<String> args) {
        if (Platform.isWindows()) {
            if (args.isEmpty()) {
                System.err.println("affinity parameter expected");
                badArguments();
            }
            try {
                int affinityMask = Integer.parseInt(args.remove());
                Kernel32.INSTANCE.SetProcessAffinityMask(Kernel32.INSTANCE.GetCurrentProcess(), affinityMask);
            } catch (NumberFormatException nex) {
                System.err.println("Bad affinityMask value");
            }
        } else {
            System.err.println("Process affinity setting is only available on Windows platform.");
        }
    }

    private static void parsePriority(Queue<String> args) {
        if (Platform.isWindows()) {
            if (args.isEmpty()) {
                System.err.println("priority parameter expected");
                badArguments();
            }
            String priority = args.remove();
            int priorityClass = 0;
            switch (priority) {
                case "low":
                    priorityClass = Kernel32.IDLE_PRIORITY_CLASS;
                    break;
                case "belownormal":
                    priorityClass = Kernel32.BELOW_NORMAL_PRIORITY_CLASS;
                    break;
                case "normal":
                    priorityClass = Kernel32.NORMAL_PRIORITY_CLASS;
                    break;
                case "abovenormal":
                    priorityClass = Kernel32.ABOVE_NORMAL_PRIORITY_CLASS;
                    break;
                case "high":
                    priorityClass = Kernel32.HIGH_PRIORITY_CLASS;
                    break;
                case "realtime":
                    priorityClass = Kernel32.REALTIME_PRIORITY_CLASS;
                    break;
                default:
                    System.err.println("Bad affinityMask value");
            }
            if (priorityClass != 0) {
                Kernel32.INSTANCE.SetPriorityClass(Kernel32.INSTANCE.GetCurrentProcess(), priorityClass);
            }
        } else {
            System.err.println("Process priority setting is only available on Windows platform.");
        }
    }

    private static void parseProxy(Queue<String> args) {
        int port = 55555;
        String portStr = args.peek();
        if (portStr != null && portStr.startsWith("-P")) {
            args.remove();
            try {
                port = Integer.parseInt(portStr.substring(2));
            } catch (NumberFormatException nex) {
                System.err.println("Bad port number");
            }
        }
        Main.startProxy(port);
    }

    private static List<String> parseSelectClasses(Queue<String> args) {
        List<String> ret = new ArrayList<>();
        if (!args.isEmpty() && args.peek().equals("-selectas3class")) {
            args.remove();
            while (!args.isEmpty()) {
                ret.add(args.remove());

            }
        }
        return ret;

    }

    private static void parseExport(Queue<String> args, AbortRetryIgnoreHandler handler, Level traceLevel, Map<String, String> formats) {
        if (args.size() < 3) {
            badArguments();
        }
        String[] validExportItems = new String[]{
            "script",
            "image",
            "shape",
            "movie",
            "sound",
            "binarydata",
            "text",
            "all",
            "fla",
            "xfl"
        };

        String[] deprecatedExportFormats = new String[]{
            "as",
            "pcode",
            "all_as",
            "all_pcode",
            "all_pcodehex",
            "all_hex",
            "textplain"
        };

        if (handler == null) {
            handler = new ConsoleAbortRetryIgnoreHandler(AbortRetryIgnoreHandler.UNDEFINED, 0);
        }
        String exportFormatString = args.remove().toLowerCase();
        String exportFormats[];
        if (exportFormatString.contains(",")) {
            exportFormats = exportFormatString.split(",");
        } else {
            exportFormats = new String[]{exportFormatString};
        }
        long startTime = System.currentTimeMillis();

        File outDir = new File(args.remove());
        File inFile = new File(args.remove());
        if (!inFile.exists()) {
            System.err.println("Input SWF file does not exist!");
            badArguments();
        }
        printHeader();
        boolean exportOK = true;

        List<String> as3classes = new ArrayList<>();

        try {
            SWF exfile = new SWF(new FileInputStream(inFile), Configuration.parallelSpeedUp.get());
            final Level level = traceLevel;
            exfile.addEventListener(new EventListener() {
                @Override
                public void handleEvent(String event, Object data) {
                    if (level.intValue() <= Level.FINE.intValue() && event.equals("exporting")) {
                        System.out.println((String) data);
                    }
                    if (event.equals("exported")) {
                        System.out.println((String) data);
                    }
                }
            });

            for (String exportFormat : exportFormats) {
                if (!Arrays.asList(validExportItems).contains(exportFormat) && !Arrays.asList(deprecatedExportFormats).contains(exportFormat)) {
                    System.err.println("Invalid export item:" + exportFormat);
                    badArguments();
                }
                if (Arrays.asList(deprecatedExportFormats).contains(exportFormat)) {
                    System.err.println("Warning: Using DEPRECATED export item: " + exportFormat + ". Run application with --help parameter to see available formats.");
                }

                commandLineMode = true;

                switch (exportFormat) {
                    case "all":
                    case "all_as":
                    case "all_pcode":
                    case "all_pcodehex":
                    case "all_hex":
                        ExportMode allExportMode = ExportMode.SOURCE;
                        if (!exportFormat.equals("all")) {
                            allExportMode = strToExportFormat(exportFormat.substring("all_".length() - 1));
                        } else if (formats.containsKey("script")) {
                            allExportMode = strToExportFormat(formats.get("script"));
                        }
                        System.out.println("Exporting images...");
                        exfile.exportImages(handler, outDir.getAbsolutePath() + File.separator + "images");
                        System.out.println("Exporting shapes...");
                        exfile.exportShapes(handler, outDir.getAbsolutePath() + File.separator + "shapes");
                        System.out.println("Exporting scripts...");
                        exfile.exportActionScript(handler, outDir.getAbsolutePath() + File.separator + "scripts", allExportMode, Configuration.parallelSpeedUp.get());
                        System.out.println("Exporting movies...");
                        exfile.exportMovies(handler, outDir.getAbsolutePath() + File.separator + "movies");
                        System.out.println("Exporting sounds...");
                        exfile.exportSounds(handler, outDir.getAbsolutePath() + File.separator + "sounds", true, true);
                        System.out.println("Exporting binaryData...");
                        exfile.exportBinaryData(handler, outDir.getAbsolutePath() + File.separator + "binaryData");
                        System.out.println("Exporting texts...");

                        String allTextFormat = formats.get("text");
                        if (allTextFormat == null) {
                            allTextFormat = "formatted";
                        }
                        exfile.exportTexts(handler, outDir.getAbsolutePath() + File.separator + "texts", allTextFormat.equals("formatted"));
                        break;
                    case "image":
                        System.out.println("Exporting images...");
                        exfile.exportImages(handler, outDir.getAbsolutePath() + (exportFormats.length > 1 ? File.separator + "images" : ""));
                        break;
                    case "shape":
                        System.out.println("Exporting shapes...");
                        exfile.exportShapes(handler, outDir.getAbsolutePath() + (exportFormats.length > 1 ? File.separator + "shapes" : ""));
                        break;
                    case "script":
                    case "as":
                    case "pcode":
                    case "pcodehex":
                    case "hex":
                        System.out.println("Exporting scripts...");
                        ExportMode exportMode = ExportMode.SOURCE;
                        if (!exportFormat.equals("script")) {
                            exportMode = strToExportFormat(exportFormat);
                        } else if (formats.containsKey("script")) {
                            exportMode = strToExportFormat(formats.get("script"));
                        }
                        boolean parallel = Configuration.parallelSpeedUp.get();
                        if (as3classes.isEmpty()) {
                            as3classes = parseSelectClasses(args);
                        }
                        if (!as3classes.isEmpty()) {
                            for (String as3class : as3classes) {
                                exportOK = exportOK && exfile.exportAS3Class(as3class, outDir.getAbsolutePath(), exportMode, parallel);
                            }
                        } else {
                            exportOK = exportOK && exfile.exportActionScript(handler, outDir.getAbsolutePath(), exportMode, parallel) != null;
                        }
                        break;
                    case "movie":
                        System.out.println("Exporting movies...");
                        exfile.exportMovies(handler, outDir.getAbsolutePath() + (exportFormats.length > 1 ? File.separator + "movies" : ""));
                        break;
                    case "sound":
                        System.out.println("Exporting sounds...");
                        exfile.exportSounds(handler, outDir.getAbsolutePath() + (exportFormats.length > 1 ? File.separator + "sounds" : ""), true, true);
                        break;
                    case "binarydata":
                        System.out.println("Exporting binaryData...");
                        exfile.exportBinaryData(handler, outDir.getAbsolutePath() + (exportFormats.length > 1 ? File.separator + "binaryData" : ""));
                        break;
                    case "text":
                        System.out.println("Exporting texts...");
                        String textFormat = formats.get("text");
                        if (textFormat == null) {
                            textFormat = "formatted";
                        }
                        exfile.exportTexts(handler, outDir.getAbsolutePath() + (exportFormats.length > 1 ? File.separator + "texts" : ""), textFormat.equals("formatted"));
                        break;
                    case "textplain":
                        System.out.println("Exporting texts...");
                        exfile.exportTexts(handler, outDir.getAbsolutePath() + (exportFormats.length > 1 ? File.separator + "texts" : ""), false);
                        break;
                    case "fla":
                        System.out.println("Exporting FLA...");
                        FLAVersion flaVersion = FLAVersion.fromString(formats.get("fla"));
                        if (flaVersion == null) {
                            flaVersion = FLAVersion.CS6; //Defaults to CS6
                        }
                        exfile.exportFla(handler, outDir.getAbsolutePath() + (exportFormats.length > 1 ? File.separator + "fla" : ""), inFile.getName(), ApplicationInfo.APPLICATION_NAME, ApplicationInfo.applicationVerName, ApplicationInfo.version, Configuration.parallelSpeedUp.get(), flaVersion);
                        break;
                    case "xfl":
                        System.out.println("Exporting XFL...");
                        FLAVersion xflVersion = FLAVersion.fromString(formats.get("xfl"));
                        if (xflVersion == null) {
                            xflVersion = FLAVersion.CS6; //Defaults to CS6                            
                        }
                        exfile.exportXfl(handler, outDir.getAbsolutePath() + (exportFormats.length > 1 ? File.separator + "xfl" : ""), inFile.getName(), ApplicationInfo.APPLICATION_NAME, ApplicationInfo.applicationVerName, ApplicationInfo.version, Configuration.parallelSpeedUp.get(), xflVersion);
                        break;
                    default:
                        exportOK = false;
                }

            }
        } catch (OutOfMemoryError | Exception ex) {
            exportOK = false;
            System.err.print("FAIL: Exporting Failed on Exception - ");
            Logger.getLogger(CommandLineArgumentParser.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        long stopTime = System.currentTimeMillis();
        long time = stopTime - startTime;
        System.out.println("Export finished. Total export time: " + Helper.formatTimeSec(time));
        if (exportOK) {
            System.out.println("OK");
            System.exit(0);
        } else {
            System.err.println("FAIL");
            System.exit(1);
        }
    }

    private static void parseCompress(Queue<String> args) {
        if (args.size() < 2) {
            badArguments();
        }

        try {
            try (InputStream fis = new BufferedInputStream(new FileInputStream(args.remove()));
                    OutputStream fos = new BufferedOutputStream(new FileOutputStream(args.remove()))) {
                if (SWF.fws2cws(fis, fos)) {
                    System.out.println("OK");
                } else {
                    System.err.println("FAIL");
                }
            } catch (FileNotFoundException ex) {
                System.err.println("File not found.");
            }
        } catch (IOException ex) {
            Logger.getLogger(CommandLineArgumentParser.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.exit(0);
    }

    private static void parseDecompress(Queue<String> args) {
        if (args.size() < 2) {
            badArguments();
        }

        try {
            try (InputStream fis = new BufferedInputStream(new FileInputStream(args.remove()));
                    OutputStream fos = new BufferedOutputStream(new FileOutputStream(args.remove()))) {
                if (SWF.decompress(fis, fos)) {
                    System.out.println("OK");
                    System.exit(0);
                } else {
                    System.err.println("FAIL");
                    System.exit(1);
                }
            } catch (FileNotFoundException ex) {
                System.err.println("File not found.");
            }
        } catch (IOException ex) {
            Logger.getLogger(CommandLineArgumentParser.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.exit(0);
    }

    private static void parseExtract(Queue<String> args) {
        if (args.size() < 1) {
            badArguments();
        }

        String fileName = args.remove();
        ExtractMode mode = ExtractMode.ALL;

        boolean noCheck = false;
        if (args.size() > 0) {
            if (args.peek().toLowerCase().equals("nocheck")) {
                noCheck = true;
                args.remove();
            }
        }

        if (args.size() > 0) {
            String modeStr = args.remove().toLowerCase();
            switch (modeStr) {
                case "biggest":
                    mode = ExtractMode.BIGGEST;
                    break;
            }
        }

        try {
            SWFSourceInfo sourceInfo = new SWFSourceInfo(null, fileName, null);
            if (!sourceInfo.isBundle()) {
                System.err.println("Error: <infile> should be a bundle. (ZIP or non SWF binary file)");
                System.exit(1);
            }
            SWFBundle bundle = sourceInfo.getBundle(noCheck);
            List<Map.Entry<String, SeekableInputStream>> streamsToExtract = new ArrayList<>();
            Map.Entry<String, SeekableInputStream> biggest = null;
            int biggestSize = 0;
            for (Map.Entry<String, SeekableInputStream> streamEntry : bundle.getAll().entrySet()) {
                InputStream stream = streamEntry.getValue();
                stream.reset();
                switch (mode) {
                    case ALL:
                        streamsToExtract.add(streamEntry);
                        break;
                    case BIGGEST:
                        byte[] swfData = new byte[stream.available()];
                        int available = stream.read(swfData); // stream.available() reports wrong value
                        if (available > biggestSize) {
                            biggest = streamEntry;
                            biggestSize = available;
                        }
                        break;
                }
            }

            if (mode == ExtractMode.BIGGEST && biggest != null) {
                streamsToExtract.add(biggest);
            }

            for (Map.Entry<String, SeekableInputStream> streamEntry : streamsToExtract) {
                InputStream stream = streamEntry.getValue();
                stream.reset();
                try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(streamEntry.getKey() + ".swf"))) {
                    byte[] swfData = new byte[stream.available()];
                    int cnt = stream.read(swfData);
                    fos.write(swfData, 0, cnt);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CommandLineArgumentParser.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.exit(0);
    }

    private static void parseRenameInvalidIdentifiers(Queue<String> args) {
        if (args.size() < 3) {
            badArguments();
        }

        String renameTypeStr = args.remove();
        RenameType renameType;
        switch (renameTypeStr.toLowerCase()) {
            case "typenumber":
                renameType = RenameType.TYPENUMBER;
                break;
            case "randomword":
                renameType = RenameType.RANDOMWORD;
                break;
            default:
                System.err.println("Invalid rename type:" + renameTypeStr);
                badArguments();
                return;
        }

        try {
            try (InputStream fis = new BufferedInputStream(new FileInputStream(args.remove()));
                    OutputStream fos = new BufferedOutputStream(new FileOutputStream(args.remove()))) {
                if (SWF.renameInvalidIdentifiers(renameType, fis, fos)) {
                    System.out.println("OK");
                    System.exit(0);
                } else {
                    System.err.println("FAIL");
                    System.exit(1);
                }
            } catch (FileNotFoundException ex) {
                System.err.println("File not found.");
            }
        } catch (IOException ex) {
            Logger.getLogger(CommandLineArgumentParser.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.exit(0);
    }

    private static Map<String, String> parseFormat(Queue<String> args) {
        if (args.size() < 1) {
            badArguments();
        }
        String fmtStr = args.remove();
        String[] fmts;
        if (fmtStr.contains(",")) {
            fmts = fmtStr.split(",");
        } else {
            fmts = new String[]{fmtStr};
        }
        Map<String, String> ret = new HashMap<>();
        for (int i = 0; i < fmts.length; i++) {
            String parts[] = fmts[i].split(":");
            ret.put(parts[0].toLowerCase(), parts[1].toLowerCase());
        }
        return ret;
    }

    private static void parseDumpSwf(Queue<String> args) {
        if (args.isEmpty()) {
            badArguments();
        }
        try {
            Configuration.dumpTags.set(true);
            Configuration.parallelSpeedUp.set(false);
            SWFSourceInfo sourceInfo = new SWFSourceInfo(null, args.remove(), null);
            Main.parseSWF(sourceInfo);
        } catch (Exception ex) {
            Logger.getLogger(CommandLineArgumentParser.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        System.exit(0);
    }
}
