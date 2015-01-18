/*
 *  Copyright (C) 2010-2015 JPEXS
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
import com.jpexs.decompiler.flash.SearchMode;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.RenameType;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.ASM3Parser;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.MissingSymbolHandler;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.ActionScriptParser;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.ConfigurationItem;
import com.jpexs.decompiler.flash.exporters.BinaryDataExporter;
import com.jpexs.decompiler.flash.exporters.FontExporter;
import com.jpexs.decompiler.flash.exporters.ImageExporter;
import com.jpexs.decompiler.flash.exporters.MorphShapeExporter;
import com.jpexs.decompiler.flash.exporters.MovieExporter;
import com.jpexs.decompiler.flash.exporters.ShapeExporter;
import com.jpexs.decompiler.flash.exporters.SoundExporter;
import com.jpexs.decompiler.flash.exporters.TextExporter;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.modes.BinaryDataExportMode;
import com.jpexs.decompiler.flash.exporters.modes.FontExportMode;
import com.jpexs.decompiler.flash.exporters.modes.FramesExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ImageExportMode;
import com.jpexs.decompiler.flash.exporters.modes.MorphShapeExportMode;
import com.jpexs.decompiler.flash.exporters.modes.MovieExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ShapeExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SoundExportMode;
import com.jpexs.decompiler.flash.exporters.modes.TextExportMode;
import com.jpexs.decompiler.flash.exporters.settings.BinaryDataExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.FontExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.FramesExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.ImageExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.MorphShapeExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.MovieExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.ShapeExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.SoundExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.TextExportSettings;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.flash.importers.BinaryDataImporter;
import com.jpexs.decompiler.flash.importers.ImageImporter;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.sound.SoundFormat;
import com.jpexs.decompiler.flash.xfl.FLAVersion;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import com.jpexs.helpers.streams.SeekableInputStream;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;
import gnu.jpdf.PDFJob;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
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
        //Configuration.autoDeobfuscate,
        Configuration.cacheOnDisk
    };

    public static boolean isCommandLineMode() {
        return commandLineMode;
    }

    public static void printCmdLineUsage() {
        printCmdLineUsage(System.out, true);
    }

    public static void printCmdLineUsage(PrintStream out, boolean printConfigs) {
        int cnt = 1;
        out.println("Commandline arguments:");
        out.println(" " + (cnt++) + ") -help | --help | /?");
        out.println(" ...shows commandline arguments (this help)");
        out.println(" " + (cnt++) + ") <infile>");
        out.println(" ...opens SWF file with the decompiler GUI");
        out.println(" " + (cnt++) + ") -proxy [-P<port>]");
        out.println("  ...auto start proxy in the tray. Optional parameter -P specifies port for proxy. Defaults to 55555. ");
        out.println(" " + (cnt++) + ") -export <itemtypes> <outdirectory> <infile> [-selectas3class <class1> <class2> ...]");
        out.println("  ...export <infile> sources to <outdirectory> ");
        out.println("     Values for <itemtypes> parameter:");
        out.println("        script - Scripts (Default format: ActionScript source)");
        out.println("               - Optional DEPRECATED \"-selectas3class\" parameter can be passed in same way as -selectclass");
        out.println("        image - Images (Default format: PNG/JPEG)");
        out.println("        shape - Shapes (Default format: SVG)");
        out.println("   morphshape - MorphShapes (Default format: SVG)");
        out.println("        movie - Movies (Default format: FLV without sound)");
        out.println("        frame - Frames (Default format: PNG)");
        out.println("        sound - Sounds (Default format: MP3/WAV/FLV only sound)");
        out.println("        binaryData - Binary data (Default format:  Raw data)");
        out.println("        text - Texts (Default format: Formatted text)");
        out.println("        all - Every resource");
        out.println("        fla - Everything to FLA compressed format");
        out.println("        xfl - Everything to uncompressed FLA format (XFL)");
        out.println("   You can export multiple types of items by using colon \",\"");
        out.println("      DO NOT PUT space between comma (,) and next value.");
        out.println();
        out.println("   Old DEPRECATED aliases include: (please use basic itemtypes and -format parameter instead)");
        out.println("        as, pcode, pcodehex, hex, all_as, all_pcode, all_pcodehex, textplain");
        out.println();
        out.println(" " + (cnt++) + ") -format <formats>");
        out.println("  ...sets output formats for export");
        out.println("    Values for <formats> parameter:");
        out.println("         script:as - ActionScript source");
        out.println("         script:pcode - ActionScript P-code");
        out.println("         script:pcodehex - ActionScript P-code with hex");
        out.println("         script:hex - ActionScript Hex only");
        out.println("         shape:svg - SVG format for Shapes");
        out.println("         shape:png - PNG format for Shapes");
        out.println("         shape:canvas - HTML5 Canvas  format for Shapes");
        out.println("         morphshape:svg - SVG format for MorphShapes");
        out.println("         morphshape:canvas - HTML5 Canvas  format for MorphShapes");
        out.println("         frame:png - PNG format for Frames");
        out.println("         frame:gif - GIF format for Frames");
        out.println("         frame:avi - AVI format for Frames");
        out.println("         frame:canvas - HTML5 Canvas format for Frames");
        out.println("         frame:pdf - PDF format for Frames");
        out.println("         image:png_jpeg - PNG/JPEG format for Images");
        out.println("         image:png - PNG format for Images");
        out.println("         image:jpeg - JPEG format for Images");
        out.println("         text:plain - Plain text format for Texts");
        out.println("         text:formatted - Formatted text format for Texts");
        out.println("         sound:mp3_wav_flv - MP3/WAV/FLV format for Sounds");
        out.println("         sound:mp3_wav - MP3/WAV format for Sounds");
        out.println("         sound:wav - WAV format for Sounds");
        out.println("         sound:flv - FLV format for Sounds");
        out.println("         fla:<flaversion> or xfl:<flaversion> - Specify FLA format version");
        out.println("            - values for <flaversion>: cs5,cs5.5,cs6,cc");

        out.println("      You can set multiple formats at once using comma (,)");
        out.println("      DO NOT PUT space between comma (,) and next value.");
        out.println("      The prefix with colon (:) is neccessary.");
        out.println(" " + (cnt++) + ") -select <ranges>");
        out.println("  ...selects frames/pages for export");
        out.println("    Example <ranges> formats:");
        out.println("                      1-5");
        out.println("                      2,3");
        out.println("                      2-5,7,9-");
        out.println("      DO NOT PUT space between comma (,) and next ramge.");
        out.println(" " + (cnt++) + ") -selectid <ranges>");
        out.println("  ...selects characters for export by character id");
        out.println("     <ranges> format is same as in -select");
        out.println(" " + (cnt++) + ") -selectclass <classnames>");
        out.println("  ...selects scripts to export by class name (ActionScript 3 ONLY)");
        out.println("     <classnames> format:");
        out.println("                    com.example.MyClass");
        out.println("                    com.example.+   (all classes in package \"com.example\")");
        out.println("                    com.++,net.company.MyClass   (all classes in package \"com\" and all subpackages, class net.company.MyClass)");
        out.println("      DO NOT PUT space between comma (,) and next class.");
        out.println(" " + (cnt++) + ") -dumpSWF <infile>");
        out.println("  ...dumps list of SWF tags to console");
        out.println(" " + (cnt++) + ") -dumpAS2 <infile>");
        out.println("  ...dumps list of AS1/2 sctipts to console");
        out.println(" " + (cnt++) + ") -dumpAS3 <infile>");
        out.println("  ...dumps list of AS3 sctipts to console");
        out.println(" " + (cnt++) + ") -compress <infile> <outfile>");
        out.println("  ...Compress SWF <infile> and save it to <outfile>");
        out.println(" " + (cnt++) + ") -decompress <infile> <outfile>");
        out.println("  ...Decompress <infile> and save it to <outfile>");
        out.println(" " + (cnt++) + ") -extract <infile> [-o <outpath>|<outfile>] [nocheck] [(all|biggest|smallest|first|last)]");
        out.println("  ...Extracts SWF files from ZIP or other binary files");
        out.println("  ...-o parameter should contain a file path when \"biggest\" or \"first\" parameter is specified");
        out.println("  ...-o parameter should contain a folder path when no exctaction mode or \"all\" parameter is specified");
        out.println(" " + (cnt++) + ") -renameInvalidIdentifiers (typeNumber|randomWord) <infile> <outfile>");
        out.println("  ...Renames the invalid identifiers in <infile> and save it to <outfile>");
        out.println(" " + (cnt++) + ") -config key=value[,key2=value2][,key3=value3...] [other parameters]");
        out.print("  ...Sets configuration values. ");
        if (printConfigs) {
            out.print("Available keys[current setting]:");
            for (ConfigurationItem item : commandlineConfigBoolean) {
                out.print(" " + item + "[" + item.get() + "]");
            }
        }
        out.println("");
        out.println("    Values are boolean, you can use 0/1, true/false, on/off or yes/no.");
        out.println("    If no other parameters passed, configuration is saved. Otherwise it is used only once.");
        out.println("    DO NOT PUT space between comma (,) and next value.");
        out.println(" " + (cnt++) + ") -onerror (abort|retryN|ignore)");
        out.println("  ...error handling mode. \"abort\" stops the exporting, \"retry\" tries the exporting N times, \"ignore\" ignores the current file");
        out.println(" " + (cnt++) + ") -timeout <N>");
        out.println("  ...decompilation timeout for a single method in AS3 or single action in AS1/2 in seconds");
        out.println(" " + (cnt++) + ") -exportTimeout <N>");
        out.println("  ...total export timeout in seconds");
        out.println(" " + (cnt++) + ") -exportFileTimeout <N>");
        out.println("  ...export timeout for a single AS3 class in seconds");
        out.println(" " + (cnt++) + ") -flashpaper2pdf <infile> <outfile>");
        out.println("  ...converts FlashPaper SWF file <infile> to PDF <outfile>. Use -zoom parameter to specify image quality.");
        out.println(" " + (cnt++) + ") -zoom <N>");
        out.println(" ...apply zoom during export (currently for FlashPaper conversion only)");
        out.println(" " + (cnt++) + ") -replace <infile> <outfile> (<characterId1>|<scriptName1>) <importDataFile1> [methodBodyIndex1] [(<characterId2>|<scriptName2>) <importDataFile2> [methodBodyIndex2]]...");
        out.println(" ...replaces the data of the specified BinaryData, Image, DefineSound tag or Script");
        out.println(" ...methodBodyIndexN parameter should be specified if and only if the imported entity is an AS3 P-Code");
        out.println();
        out.println("Examples:");
        out.println("java -jar ffdec.jar myfile.swf");
        out.println("java -jar ffdec.jar -proxy");
        out.println("java -jar ffdec.jar -proxy -P1234");
        out.println("java -jar ffdec.jar -export script \"C:\\decompiled\" myfile.swf");
        out.println("java -jar ffdec.jar -selectclass com.example.MyClass,com.example.SecondClass -export script \"C:\\decompiled\" myfile.swf");
        out.println("java -jar ffdec.jar -format script:pcode -export script \"C:\\decompiled\" myfile.swf");
        out.println("java -jar ffdec.jar -format script:pcode,text:plain -export script,text,image \"C:\\decompiled\" myfile.swf");
        out.println("java -jar ffdec.jar -format fla:cs5.5 -export fla \"C:\\sources\\myfile.fla\" myfile.swf");
        out.println("java -jar ffdec.jar -dumpSWF myfile.swf");
        out.println("java -jar ffdec.jar -compress myfile.swf myfiledec.swf");
        out.println("java -jar ffdec.jar -decompress myfiledec.swf myfile.swf");
        out.println("java -jar ffdec.jar -onerror ignore -export script \"C:\\decompiled\" myfile.swf");
        out.println("java -jar ffdec.jar -onerror retry 5 -export script \"C:\\decompiled\" myfile.swf");
        out.println("java -jar ffdec.jar -config autoDeobfuscate=1,parallelSpeedUp=0 -export script \"C:\\decompiled\" myfile.swf");
        out.println("");
        out.println("Instead of \"java -jar ffdec.jar\" you can use ffdec.bat on Windows, ffdec.sh on Linux/MacOs");
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
        Queue<String> args = new LinkedList<>();
        for (String arg : arguments) {
            if (arg.length() > 0) {
                args.add(arg);
            }
        }
        AbortRetryIgnoreHandler handler = null;
        Map<String, String> format = new HashMap<>();
        double zoom = 1;
        Selection selection = new Selection();
        Selection selectionIds = new Selection();
        List<String> selectionClasses = null;
        String nextParam = null, nextParamOriginal = null;
        OUTER:
        while (true) {
            nextParamOriginal = args.remove();
            if (nextParamOriginal != null) {
                nextParam = nextParamOriginal.toLowerCase();
            }
            switch (nextParam) {
                case "-selectid":
                    selectionIds = parseSelect(args);
                    break;
                case "-select":
                    selection = parseSelect(args);
                    break;
                case "-selectclass":
                    selectionClasses = parseSelectClass(args);
                    break;
                case "-zoom":
                    zoom = parseZoom(args);
                    break;
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
                    for (int i = 0; i < arguments.length; i++) {
                        System.out.println(i + ".:" + arguments[i]);
                    }
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
            parseExport(selectionClasses, selection, selectionIds, args, handler, traceLevel, format, zoom);
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
        } else if (nextParam.equals("-dumpas2")) {
            parseDumpAS2(args);
        } else if (nextParam.equals("-dumpas3")) {
            parseDumpAS3(args);
        } else if (nextParam.equals("-flashpaper2pdf")) {
            parseFlashPaperToPdf(selection, zoom, args);
        } else if (nextParam.equals("-replace")) {
            parseReplace(args);
        } else if (nextParam.equals("-as3compiler")) {
            ActionScriptParser.compile(null /*?*/, args.remove(), args.remove(), 0);
        } else if (nextParam.equals("-help") || nextParam.equals("--help") || nextParam.equals("/?") || nextParam.equals("\\_") /* /? translates as this on windows */) {
            printHeader();
            printCmdLineUsage();
            System.exit(0);
        } else if (nextParam.equals("--webhelp")) { //for generating commandline usage on webpages
            ByteArrayOutputStream whbaos = new ByteArrayOutputStream();
            printCmdLineUsage(new PrintStream(whbaos, true), false);
            String wh = new String(whbaos.toByteArray());
            wh = wh.replace("<", "&lt;").replace(">", "&gt;");
            System.out.println(wh);
        } else if (args.isEmpty()) {
            return nextParamOriginal; // file names should be the original one
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

    private static ScriptExportMode strToExportFormat(String exportFormatStr) {
        switch (exportFormatStr) {
            case "pcode":
                return ScriptExportMode.PCODE;
            case "pcodehex":
                return ScriptExportMode.PCODE_HEX;
            case "hex":
                return ScriptExportMode.HEX;
            default:
                return ScriptExportMode.AS;
        }
    }

    private static void parseConfig(Queue<String> args) {
        if (args.isEmpty()) {
            System.err.println("Config values expected");
            badArguments();
        }
        setConfigurations(args.remove());
    }

    private static class Range {

        public Integer min;

        public Integer max;

        public Range(Integer min, Integer max) {
            this.min = min;
            this.max = max;
        }

        public boolean contains(int index) {
            int minimum = min == null ? Integer.MIN_VALUE : min;
            int maximum = max == null ? Integer.MAX_VALUE : max;

            return index >= minimum && index <= maximum;
        }
    }

    private static class Selection {

        public List<Range> ranges;

        public Selection() {
            this.ranges = new ArrayList<>();
            this.ranges.add(new Range(null, null));
        }

        public Selection(List<Range> ranges) {
            this.ranges = ranges;
        }

        public boolean contains(int index) {
            for (Range r : ranges) {
                if (r.contains(index)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static Selection parseSelect(Queue<String> args) {
        List<Range> ret = new ArrayList<>();
        if (args.isEmpty()) {
            System.err.println("range parameter expected");
            badArguments();
        }
        String range = args.remove();
        String[] ranges;
        if (range.contains(",")) {
            ranges = range.split(",");
        } else {
            ranges = new String[]{range};
        }
        for (String r : ranges) {
            Integer min = null;
            Integer max = null;
            if (r.contains("-")) {
                String ps[] = r.split("\\-");
                if (ps.length != 2) {
                    System.err.println("invalid range");
                    badArguments();
                }
                try {
                    if (!"".equals(ps[0])) {
                        min = Integer.parseInt(ps[0]);
                    }
                    if (!"".equals(ps[1])) {
                        max = Integer.parseInt(ps[1]);
                    }
                } catch (NumberFormatException nfe) {
                    System.err.println("invalid range");
                    badArguments();
                }
            } else {
                try {
                    min = Integer.parseInt(r);
                    max = min;
                } catch (NumberFormatException nfe) {
                    System.err.println("invalid range");
                    badArguments();
                }
            }
            ret.add(new Range(min, max));
        }
        return new Selection(ret);
    }

    private static double parseZoom(Queue<String> args) {
        if (args.isEmpty()) {
            System.err.println("zoom parameter expected");
            badArguments();
        }
        try {
            return Double.parseDouble(args.remove());
        } catch (NumberFormatException nfe) {
            System.err.println("invalid zoom");
            badArguments();
        }
        return 1;
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

    private static List<String> parseSelectClassOld(Queue<String> args) {
        List<String> ret = new ArrayList<>();
        if (!args.isEmpty() && args.peek().equals("-selectas3class")) {
            args.remove();
            while (!args.isEmpty()) {
                ret.add(args.remove());
            }
            System.err.println("WARNING: Using deprecated -selectas3class parameter. Please use -selectclass instead. See --help for usage.");
        }
        return ret;

    }

    private static List<String> parseSelectClass(Queue<String> args) {
        if (args.size() < 1) {
            badArguments();
        }
        List<String> ret = new ArrayList<>();
        String classesStr = args.remove();
        String classes[];
        if (classesStr.contains(",")) {
            classes = classesStr.split(",");
        } else {
            classes = new String[]{classesStr};
        }
        ret.addAll(Arrays.asList(classes));
        return ret;

    }

    private static void parseExport(List<String> selectionClasses, Selection selection, Selection selectionIds, Queue<String> args, AbortRetryIgnoreHandler handler, Level traceLevel, Map<String, String> formats, double zoom) {
        if (args.size() < 3) {
            badArguments();
        }
        String[] validExportItems = new String[]{
            "script",
            "image",
            "shape",
            "morphshape",
            "movie",
            "sound",
            "binarydata",
            "text",
            "all",
            "frame",
            "fla",
            "xfl",
            "font"
        };

        String[] removedExportFormats = new String[]{
            "as",
            "pcode",
            "hex",
            "pcodehex",
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
        if (selectionClasses != null) {
            as3classes.addAll(selectionClasses);
        }

        try {
            SWF exfile = new SWF(new FileInputStream(inFile), Configuration.parallelSpeedUp.get());

            List<Tag> extags = new ArrayList<>();
            for (Tag t : exfile.tags) {
                if (t instanceof CharacterIdTag) {
                    CharacterIdTag c = (CharacterIdTag) t;
                    if (selectionIds.contains(c.getCharacterId())) {
                        extags.add(t);
                    }
                } else {
                    if (selectionIds.contains(0)) {
                        extags.add(t);
                    }
                }
            }

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
                if (!Arrays.asList(validExportItems).contains(exportFormat) && !Arrays.asList(removedExportFormats).contains(exportFormat)) {
                    System.err.println("Invalid export item:" + exportFormat);
                    badArguments();
                }
                if (Arrays.asList(removedExportFormats).contains(exportFormat)) {
                    System.err.println("Error: Export format : " + exportFormat + " was REMOVED. Run application with --help parameter to see available formats.");
                    System.exit(1);
                }

                commandLineMode = true;

                switch (exportFormat) {
                    case "all": {
                        ScriptExportMode allExportMode = ScriptExportMode.AS;
                        if (!exportFormat.equals("all")) {
                            allExportMode = strToExportFormat(exportFormat.substring("all_".length() - 1));
                        } else if (formats.containsKey("script")) {
                            allExportMode = strToExportFormat(formats.get("script"));
                        }
                        System.out.println("Exporting images...");
                        new ImageExporter().exportImages(handler, outDir.getAbsolutePath() + File.separator + "images", extags, new ImageExportSettings(ImageExportMode.PNG_JPEG));
                        System.out.println("Exporting shapes...");
                        new ShapeExporter().exportShapes(handler, outDir.getAbsolutePath() + File.separator + "shapes", extags, new ShapeExportSettings(ShapeExportMode.SVG, zoom));
                        System.out.println("Exporting morphshapes...");
                        new MorphShapeExporter().exportMorphShapes(handler, outDir.getAbsolutePath() + File.separator + "morphshapes", extags, new MorphShapeExportSettings(MorphShapeExportMode.SVG, zoom));
                        System.out.println("Exporting scripts...");
                        exfile.exportActionScript(handler, outDir.getAbsolutePath() + File.separator + "scripts", allExportMode, Configuration.parallelSpeedUp.get());
                        System.out.println("Exporting movies...");
                        new MovieExporter().exportMovies(handler, outDir.getAbsolutePath() + File.separator + "movies", extags, new MovieExportSettings(MovieExportMode.FLV));
                        System.out.println("Exporting sounds...");
                        new SoundExporter().exportSounds(handler, outDir.getAbsolutePath() + File.separator + "sounds", extags, new SoundExportSettings(SoundExportMode.MP3_WAV_FLV));
                        System.out.println("Exporting binaryData...");
                        new BinaryDataExporter().exportBinaryData(handler, outDir.getAbsolutePath() + File.separator + "binaryData", extags, new BinaryDataExportSettings(BinaryDataExportMode.RAW));
                        System.out.println("Exporting texts...");

                        String allTextFormat = formats.get("text");
                        if (allTextFormat == null) {
                            allTextFormat = "formatted";
                        }
                        Boolean singleTextFile = parseBooleanConfigValue(formats.get("singletext"));
                        if (singleTextFile == null) {
                            singleTextFile = Configuration.textExportSingleFile.get();
                        }
                        new TextExporter().exportTexts(handler, outDir.getAbsolutePath() + File.separator + "texts", extags, new TextExportSettings(allTextFormat.equals("formatted") ? TextExportMode.FORMATTED : TextExportMode.PLAIN, singleTextFile, zoom));
                    }
                    break;
                    case "image": {
                        System.out.println("Exporting images...");
                        new ImageExporter().exportImages(handler, outDir.getAbsolutePath() + (exportFormats.length > 1 ? File.separator + "images" : ""), extags, new ImageExportSettings(enumFromStr(formats.get("image"), ImageExportMode.class)));
                    }
                    break;
                    case "shape": {
                        System.out.println("Exporting shapes...");
                        new ShapeExporter().exportShapes(handler, outDir.getAbsolutePath() + (exportFormats.length > 1 ? File.separator + "shapes" : ""), extags, new ShapeExportSettings(enumFromStr(formats.get("shape"), ShapeExportMode.class), zoom));
                    }
                    break;
                    case "morphshape": {
                        System.out.println("Exporting morphshapes...");
                        new MorphShapeExporter().exportMorphShapes(handler, outDir.getAbsolutePath() + (exportFormats.length > 1 ? File.separator + "morphshapes" : ""), extags, new MorphShapeExportSettings(enumFromStr(formats.get("morphshape"), MorphShapeExportMode.class), zoom));
                    }
                    break;
                    case "script": {
                        System.out.println("Exporting scripts...");
                        boolean parallel = Configuration.parallelSpeedUp.get();
                        if (as3classes.isEmpty()) {
                            as3classes = parseSelectClassOld(args);
                        }
                        if (!as3classes.isEmpty()) {
                            for (String as3class : as3classes) {
                                exportOK = exportOK && exfile.exportAS3Class(as3class, outDir.getAbsolutePath(), enumFromStr(formats.get("script"), ScriptExportMode.class), parallel);
                            }
                        } else {
                            exportOK = exportOK && exfile.exportActionScript(handler, outDir.getAbsolutePath(), enumFromStr(formats.get("script"), ScriptExportMode.class), parallel) != null;
                        }
                    }
                    break;
                    case "movie": {
                        System.out.println("Exporting movies...");
                        new MovieExporter().exportMovies(handler, outDir.getAbsolutePath() + (exportFormats.length > 1 ? File.separator + "movies" : ""), extags,
                                new MovieExportSettings(enumFromStr(formats.get("movie"), MovieExportMode.class)));
                    }
                    break;
                    case "font": {
                        System.out.println("Exporting fonts...");
                        new FontExporter().exportFonts(handler, outDir.getAbsolutePath() + (exportFormats.length > 1 ? File.separator + "fonts" : ""), extags,
                                new FontExportSettings(enumFromStr(formats.get("font"), FontExportMode.class)));
                    }
                    break;
                    case "frame": {
                        System.out.println("Exporting frames...");
                        List<Integer> frames = new ArrayList<>();
                        for (int i = 0; i < exfile.frameCount; i++) {
                            if (selection.contains(i + 1)) {
                                frames.add(i);
                            }
                        }
                        exfile.exportFrames(handler, outDir.getAbsolutePath() + (exportFormats.length > 1 ? File.separator + "frames" : ""), 0, frames,
                                new FramesExportSettings(enumFromStr(formats.get("frame"), FramesExportMode.class), zoom));
                    }
                    break;
                    case "sound": {
                        System.out.println("Exporting sounds...");
                        new SoundExporter().exportSounds(handler, outDir.getAbsolutePath() + (exportFormats.length > 1 ? File.separator + "sounds" : ""), extags,
                                new SoundExportSettings(enumFromStr(formats.get("sound"), SoundExportMode.class)));
                    }
                    break;
                    case "binarydata": {
                        System.out.println("Exporting binaryData...");
                        new BinaryDataExporter().exportBinaryData(handler, outDir.getAbsolutePath() + (exportFormats.length > 1 ? File.separator + "binaryData" : ""), extags,
                                new BinaryDataExportSettings(enumFromStr(formats.get("binarydata"), BinaryDataExportMode.class)));
                    }
                    break;
                    case "text": {
                        System.out.println("Exporting texts...");
                        Boolean singleTextFile = parseBooleanConfigValue(formats.get("singletext"));
                        if (singleTextFile == null) {
                            singleTextFile = Configuration.textExportSingleFile.get();
                        }
                        new TextExporter().exportTexts(handler, outDir.getAbsolutePath() + (exportFormats.length > 1 ? File.separator + "texts" : ""), extags,
                                new TextExportSettings(enumFromStr(formats.get("text"), TextExportMode.class), singleTextFile, zoom));
                    }
                    break;
                    case "fla": {
                        System.out.println("Exporting FLA...");
                        FLAVersion flaVersion = FLAVersion.fromString(formats.get("fla"));
                        if (flaVersion == null) {
                            flaVersion = FLAVersion.CS6; //Defaults to CS6
                        }
                        exfile.exportFla(handler, outDir.getAbsolutePath() + (exportFormats.length > 1 ? File.separator + "fla" : ""), inFile.getName(), ApplicationInfo.APPLICATION_NAME, ApplicationInfo.applicationVerName, ApplicationInfo.version, Configuration.parallelSpeedUp.get(), flaVersion);
                    }
                    break;
                    case "xfl": {
                        System.out.println("Exporting XFL...");
                        FLAVersion xflVersion = FLAVersion.fromString(formats.get("xfl"));
                        if (xflVersion == null) {
                            xflVersion = FLAVersion.CS6; //Defaults to CS6
                        }
                        exfile.exportXfl(handler, outDir.getAbsolutePath() + (exportFormats.length > 1 ? File.separator + "xfl" : ""), inFile.getName(), ApplicationInfo.APPLICATION_NAME, ApplicationInfo.applicationVerName, ApplicationInfo.version, Configuration.parallelSpeedUp.get(), xflVersion);
                    }
                    break;
                    default:
                        exportOK = false;
                }

            }
        } catch (OutOfMemoryError | Exception ex) {
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
        SearchMode mode = SearchMode.ALL;

        boolean noCheck = false;
        String output = null;

        if (args.size() > 0 && args.peek().toLowerCase().equals("-o")) {
            args.remove();
            if (args.size() < 1) {
                badArguments();
            }
            output = args.remove();
        }

        if (args.size() > 0 && args.peek().toLowerCase().equals("nocheck")) {
            noCheck = true;
            args.remove();
        }

        if (args.size() > 0) {
            String modeStr = args.remove().toLowerCase();
            switch (modeStr) {
                case "biggest":
                    mode = SearchMode.BIGGEST;
                    break;
                case "smallest":
                    mode = SearchMode.SMALLEST;
                    break;
                case "first":
                    mode = SearchMode.FIRST;
                    break;
                case "last":
                    mode = SearchMode.LAST;
                    break;
            }
        }

        try {
            SWFSourceInfo sourceInfo = new SWFSourceInfo(null, fileName, null);
            if (!sourceInfo.isBundle()) {
                System.err.println("Error: <infile> should be a bundle. (ZIP or non SWF binary file)");
                System.exit(1);
            }
            SWFBundle bundle = sourceInfo.getBundle(noCheck, mode);
            List<Map.Entry<String, SeekableInputStream>> streamsToExtract = new ArrayList<>();
            for (Map.Entry<String, SeekableInputStream> streamEntry : bundle.getAll().entrySet()) {
                InputStream stream = streamEntry.getValue();
                stream.reset();
                streamsToExtract.add(streamEntry);
            }

            for (Map.Entry<String, SeekableInputStream> streamEntry : streamsToExtract) {
                InputStream stream = streamEntry.getValue();
                stream.reset();
                String fileNameOut;
                if (mode != SearchMode.ALL) {
                    if (output == null) {
                        fileNameOut = Path.getFileNameWithoutExtension(new File(fileName)) + ".swf";
                    } else {
                        fileNameOut = output;
                    }
                } else {
                    fileNameOut = streamEntry.getKey() + ".swf";
                    if (output != null) {
                        fileNameOut = Path.combine(output, fileNameOut);
                    }
                }

                try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(fileNameOut))) {
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
        for (String fmt : fmts) {
            String[] parts = fmt.split(":");
            ret.put(parts[0].toLowerCase(), parts[1].toLowerCase());
        }
        return ret;
    }

    private static void parseFlashPaperToPdf(Selection selection, double zoom, Queue<String> args) {
        if (args.size() < 2) {
            badArguments();
        }
        File inFile = new File(args.remove());
        File outFile = new File(args.remove());
        printHeader();

        try (FileInputStream is = new FileInputStream(inFile)) {

            PDFJob job = null;

            SWF swf = new SWF(is, Configuration.parallelSpeedUp.get());
            int totalPages = 0;

            for (Tag t : swf.tags) {
                if (t instanceof DefineSpriteTag) {
                    DefineSpriteTag ds = (DefineSpriteTag) t;
                    if ("page1".equals(ds.getExportName())) {
                        totalPages = 1;
                    } else {
                        if (totalPages > 0) {
                            totalPages++;
                        }
                    }
                }
            }

            int page = 0;

            for (Tag t : swf.tags) {
                if (t instanceof DefineSpriteTag) {
                    DefineSpriteTag ds = (DefineSpriteTag) t;
                    if ("page1".equals(ds.getExportName())) {
                        page = 1;
                        job = new PDFJob(new FileOutputStream(outFile));
                    } else {
                        if (page > 0) {
                            page++;
                        }
                    }
                    if (("page" + page).equals(ds.getExportName())) {
                        if (!selection.contains(page)) {
                            continue;
                        }
                        System.out.print("Page " + page + "/" + totalPages + "...");
                        RECT displayRect = new RECT(ds.getTimeline().displayRect);
                        //displayRect.Xmax *= zoom;
                        //displayRect.Ymax *= zoom;
                        Matrix m = new Matrix();
                        //m.scale(zoom);
                        BufferedImage img = SWF.frameToImageGet(ds.getTimeline(), 0, 0, null, 0, displayRect, m, new ColorTransform(), Color.white, false, zoom).getBufferedImage();
                        PageFormat pf = new PageFormat();
                        pf.setOrientation(PageFormat.PORTRAIT);
                        Paper p = new Paper();
                        p.setSize(img.getWidth(), img.getHeight());
                        pf.setPaper(p);
                        Graphics g = job.getGraphics(pf);
                        g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
                        g.dispose();
                        System.out.println("OK");

                    }
                }
            }

            if (job == null) {
                System.err.println("No pages found. Maybe it is not a FlashPaper file");
                System.exit(2);
            }
            job.end();

        } catch (FileNotFoundException ex) {
            System.err.println("File not found");
            System.exit(1);
        } catch (IOException | InterruptedException ex) {
            System.err.println("I/O error during reading");
            System.exit(2);
        }
        System.exit(0);
    }

    private static void parseReplace(Queue<String> args) {
        if (args.size() < 4) {
            badArguments();
        }

        File inFile = new File(args.remove());
        File outFile = new File(args.remove());
        try {
            try (FileInputStream is = new FileInputStream(inFile)) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get());
                while (true) {
                    String objectToReplace = args.remove();

                    if (objectToReplace.matches("\\d+")) {
                        // replace character tag
                        int characterId = 0;
                        try {
                            characterId = Integer.parseInt(objectToReplace);
                        } catch (NumberFormatException nfe) {
                            System.err.println("CharacterId should be integer");
                            System.exit(1);
                        }
                        if (!swf.getCharacters().containsKey(characterId)) {
                            System.err.println("CharacterId does not exist");
                            System.exit(1);
                        }

                        CharacterTag characterTag = swf.getCharacter(characterId);
                        String repFile = args.remove();
                        byte[] data = Helper.readFile(repFile);
                        if (characterTag instanceof DefineBinaryDataTag) {
                            DefineBinaryDataTag defineBinaryData = (DefineBinaryDataTag) characterTag;
                            new BinaryDataImporter().importData(defineBinaryData, data);
                        } else if (characterTag instanceof ImageTag) {
                            ImageTag imageTag = (ImageTag) characterTag;
                            new ImageImporter().importImage(imageTag, data);
                        } else if (characterTag instanceof SoundTag) {
                            SoundTag st = (SoundTag) characterTag;
                            int soundFormat = SoundFormat.FORMAT_UNCOMPRESSED_LITTLE_ENDIAN;
                            if (repFile.toLowerCase().endsWith(".mp3")) {
                                soundFormat = SoundFormat.FORMAT_MP3;
                            }
                            boolean ok = st.setSound(new ByteArrayInputStream(data), soundFormat);
                            if (!ok) {
                                System.err.println("Import FAILED. Maybe unsuppoted media type? Only MP3 and uncompressed WAV are available.");
                                System.exit(1);
                            }
                        } else {
                            System.err.println("The specified tag type it not supported for import");
                            System.exit(1);
                        }
                    } else {
                        Map<String, ASMSource> asms = swf.getASMs();
                        boolean found = false;
                        if (asms.containsKey(objectToReplace)) {
                            found = true;
                            // replace AS1/2
                            String repFile = args.remove();
                            String repText = Helper.readTextFile(repFile);
                            ASMSource src = asms.get(objectToReplace);
                            if (Path.getExtension(repFile).equals(".as")) {
                                replaceAS2(repText, src);
                            } else {
                                replaceAS2PCode(repText, src);
                            }
                        } else {
                            List<MyEntry<ClassPath, ScriptPack>> packs = swf.getAS3Packs();
                            for (MyEntry<ClassPath, ScriptPack> entry : packs) {
                                if (entry.getKey().toString().equals(objectToReplace)) {
                                    found = true;
                                    // replace AS3
                                    String repFile = args.remove();
                                    String repText = Helper.readTextFile(repFile);
                                    ScriptPack pack = entry.getValue();
                                    if (Path.getExtension(repFile).equals(".as")) {
                                        replaceAS3(repText, pack);
                                    } else {
                                        // todo: get traits
                                        if (args.isEmpty()) {
                                            badArguments();
                                        }
                                        int bodyIndex = Integer.parseInt(args.remove());
                                        //int classIndex = 0;
                                        //int traitId = 0;
                                        Trait trait = null; //abc.findTraitByTraitId(classIndex, traitId);
                                        replaceAS3PCode(repText, pack.abc, bodyIndex, trait);
                                    }
                                }
                            }
                        }

                        if (!found) {
                            System.err.println(objectToReplace + " is not reocginized as a CharacterId or a script name.");
                            System.exit(1);
                        }
                    }

                    if (args.isEmpty() || args.peek().startsWith("-")) {
                        break;
                    }
                }

                try {
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        swf.saveTo(fos);
                    }
                } catch (IOException e) {
                    System.err.println("I/O error during writing");
                    System.exit(2);
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("I/O error during reading");
            System.exit(2);
        }
    }

    private static void replaceAS2PCode(String text, ASMSource src) throws IOException, InterruptedException {
        System.out.println("Replace AS1/2 PCode");
        if (text.trim().startsWith("#hexdata")) {
            src.setActionBytes(Helper.getBytesFromHexaText(text));
        } else {
            try {
                src.setActions(ASMParser.parse(0, true, text, src.getSwf().version, false));
            } catch (ActionParseException ex) {
                System.err.println("%error% on line %line%".replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)));
                System.exit(1);
            }
        }
        src.setModified();
    }

    private static void replaceAS2(String as, ASMSource src) throws IOException, InterruptedException {
        System.out.println("Replace AS1/2");
        System.out.println("Warning: This feature is EXPERIMENTAL");
        com.jpexs.decompiler.flash.action.parser.script.ActionScriptParser par = new com.jpexs.decompiler.flash.action.parser.script.ActionScriptParser(src.getSwf().version);
        try {
            src.setActions(par.actionsFromString(as));
        } catch (ActionParseException ex) {
            System.err.println("%error% on line %line%".replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)));
            System.exit(1);
        } catch (CompilationException ex) {
            System.err.println("%error% on line %line%".replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)));
            System.exit(1);
        }
        src.setModified();
    }

    private static void replaceAS3PCode(String text, ABC abc, int bodyIndex, Trait trait) throws IOException, InterruptedException {
        System.out.println("Replace AS3 PCode");
        if (text.trim().startsWith("#hexdata")) {
            byte[] data = Helper.getBytesFromHexaText(text);
            MethodBody mb = abc.bodies.get(bodyIndex);
            mb.setCodeBytes(data);
        } else {
            try {
                AVM2Code acode = ASM3Parser.parse(new StringReader(text), abc.constants, trait, new MissingSymbolHandler() {
                    //no longer ask for adding new constants
                    @Override
                    public boolean missingString(String value) {
                        return true;
                    }

                    @Override
                    public boolean missingInt(long value) {
                        return true;
                    }

                    @Override
                    public boolean missingUInt(long value) {
                        return true;
                    }

                    @Override
                    public boolean missingDouble(double value) {
                        return true;
                    }
                }, abc.bodies.get(bodyIndex), abc.method_info.get(abc.bodies.get(bodyIndex).method_info));
                //acode.getBytes(abc.bodies.get(bodyIndex).getCodeBytes());
                abc.bodies.get(bodyIndex).setCode(acode);
            } catch (AVM2ParseException ex) {
                System.err.println("%error% on line %line%".replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)));
                System.exit(1);
            }
        }
        ((Tag) abc.parentTag).setModified(true);
    }

    private static void replaceAS3(String as, ScriptPack pack) throws IOException, InterruptedException {
        System.out.println("Replace AS3");
        System.out.println("Warning: This feature is EXPERIMENTAL");
        File swc = Configuration.getPlayerSWC();
        if (swc == null) {
            final String adobePage = "http://www.adobe.com/support/flashplayer/downloads.html";
            System.err.println("For ActionScript 3 direct editation, a library called \"PlayerGlobal.swc\" needs to be downloaded from Adobe homepage:");
            System.err.println(adobePage);
            System.err.println("Download the library called PlayerGlobal(.swc), and place it to directory");
            System.err.println(Configuration.getFlashLibPath().getAbsolutePath());
            System.exit(1);
        }

        try {
            pack.abc.replaceSciptPack(pack, as);
        } catch (AVM2ParseException ex) {
            System.err.println("%error% on line %line%".replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)));
            System.exit(1);
        } catch (CompilationException ex) {
            System.err.println("%error% on line %line%".replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)));
            System.exit(1);
        }
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

    private static void parseDumpAS2(Queue<String> args) {
        if (args.isEmpty()) {
            badArguments();
        }
        File file = new File(args.remove());
        try {
            try (FileInputStream is = new FileInputStream(file)) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get());
                Map<String, ASMSource> asms = swf.getASMs();
                for (String as2 : asms.keySet()) {
                    System.out.println(as2);
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("I/O error during reading");
            System.exit(2);
        }
    }

    private static void parseDumpAS3(Queue<String> args) {
        if (args.isEmpty()) {
            badArguments();
        }
        File file = new File(args.remove());
        try {
            try (FileInputStream is = new FileInputStream(file)) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get());
                List<MyEntry<ClassPath, ScriptPack>> packs = swf.getAS3Packs();
                for (MyEntry<ClassPath, ScriptPack> entry : packs) {
                    System.out.println(entry.getKey().toString() + " " + entry.getValue().scriptIndex);
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("I/O error during reading");
            System.exit(2);
        }
    }

    private static <E extends Enum> E enumFromStr(String str, Class<E> cls) {
        E[] vals = cls.getEnumConstants();
        if (str == null) {
            return vals[0];
        }
        for (E e : vals) {
            if (e.toString().toLowerCase().replace("_", "").equals(str.toLowerCase().replace("_", ""))) {
                return e;
            }
        }
        return vals[0];
    }
}
