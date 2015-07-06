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
import com.jpexs.decompiler.flash.SWFCompression;
import com.jpexs.decompiler.flash.SWFSourceInfo;
import com.jpexs.decompiler.flash.SearchMode;
import com.jpexs.decompiler.flash.SwfOpenException;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.RenameType;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.DeobfuscationLevel;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.ASM3Parser;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.MissingSymbolHandler;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.ActionScript3Parser;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.ConfigurationItem;
import com.jpexs.decompiler.flash.exporters.BinaryDataExporter;
import com.jpexs.decompiler.flash.exporters.FontExporter;
import com.jpexs.decompiler.flash.exporters.FrameExporter;
import com.jpexs.decompiler.flash.exporters.ImageExporter;
import com.jpexs.decompiler.flash.exporters.MorphShapeExporter;
import com.jpexs.decompiler.flash.exporters.MovieExporter;
import com.jpexs.decompiler.flash.exporters.ShapeExporter;
import com.jpexs.decompiler.flash.exporters.SoundExporter;
import com.jpexs.decompiler.flash.exporters.TextExporter;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.modes.BinaryDataExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ButtonExportMode;
import com.jpexs.decompiler.flash.exporters.modes.FontExportMode;
import com.jpexs.decompiler.flash.exporters.modes.FrameExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ImageExportMode;
import com.jpexs.decompiler.flash.exporters.modes.MorphShapeExportMode;
import com.jpexs.decompiler.flash.exporters.modes.MovieExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ShapeExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SoundExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SpriteExportMode;
import com.jpexs.decompiler.flash.exporters.modes.TextExportMode;
import com.jpexs.decompiler.flash.exporters.settings.BinaryDataExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.ButtonExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.FontExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.FrameExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.ImageExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.MorphShapeExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.MovieExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.ShapeExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.SoundExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.SpriteExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.TextExportSettings;
import com.jpexs.decompiler.flash.exporters.swf.SwfXmlExporter;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.helpers.CheckResources;
import com.jpexs.decompiler.flash.helpers.FileTextWriter;
import com.jpexs.decompiler.flash.importers.BinaryDataImporter;
import com.jpexs.decompiler.flash.importers.ImageImporter;
import com.jpexs.decompiler.flash.importers.ShapeImporter;
import com.jpexs.decompiler.flash.importers.SwfXmlImporter;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG3Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG4Tag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.JPEGTablesTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.treeitems.SWFList;
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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class CommandLineArgumentParser {

    private static final Logger logger = Logger.getLogger(CommandLineArgumentParser.class.getName());

    private static boolean commandLineMode = false;

    private static String stdOut = null;

    private static String stdErr = null;

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
        out.println(" " + (cnt++) + ") <infile> [<infile2> <infile3> ...]");
        out.println(" ...opens SWF file(s) with the decompiler GUI");
        out.println(" " + (cnt++) + ") -proxy [-P<port>]");
        out.println("  ...auto start proxy in the tray. Optional parameter -P specifies port for proxy. Defaults to 55555. ");
        out.println(" " + (cnt++) + ") -export <itemtypes> <outdirectory> <infile_or_directory> [-selectas3class <class1> <class2> ...]");
        out.println("  ...export <infile_or_directory> sources to <outdirectory>.");
        out.println("  Exports all files from <infile_or_directory> when it is a folder.");
        out.println("     Values for <itemtypes> parameter:");
        out.println("        script - Scripts (Default format: ActionScript source)");
        out.println("               - Optional DEPRECATED \"-selectas3class\" parameter can be passed in same way as -selectclass");
        out.println("        image - Images (Default format: PNG/JPEG)");
        out.println("        shape - Shapes (Default format: SVG)");
        out.println("   morphshape - MorphShapes (Default format: SVG)");
        out.println("        movie - Movies (Default format: FLV without sound)");
        out.println("        font - Fonts (Default format: TTF)");
        out.println("        frame - Frames (Default format: PNG)");
        out.println("        sprite - Sprites (Default format: PNG)");
        out.println("        button - Buttons (Default format: PNG)");
        out.println("        sound - Sounds (Default format: MP3/WAV/FLV only sound)");
        out.println("        binaryData - Binary data (Default format:  Raw data)");
        out.println("        text - Texts (Default format: Plain text)");
        out.println("        all - Every resource (but not FLA and XFL)");
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
        out.println("         shape:canvas - HTML5 Canvas format for Shapes");
        out.println("         shape:bmp - BMP format for Shapes");
        out.println("         morphshape:svg - SVG format for MorphShapes");
        out.println("         morphshape:canvas - HTML5 Canvas  format for MorphShapes");
        out.println("         frame:png - PNG format for Frames");
        out.println("         frame:gif - GIF format for Frames");
        out.println("         frame:avi - AVI format for Frames");
        out.println("         frame:svg - SVG format for Frames");
        out.println("         frame:canvas - HTML5 Canvas format for Frames");
        out.println("         frame:pdf - PDF format for Frames");
        out.println("         frame:bmp - BMP format for Frames");
        out.println("         sprite:png - PNG format for Sprites");
        out.println("         sprite:gif - GIF format for Sprites");
        out.println("         sprite:avi - AVI format for Sprites");
        out.println("         sprite:svg - SVG format for Sprites");
        out.println("         sprite:canvas - HTML5 Canvas format for Sprites");
        out.println("         sprite:pdf - PDF format for Sprites");
        out.println("         sprite:bmp - BMP format for Sprites");
        out.println("         button:png - PNG format for Buttons");
        out.println("         button:svg - SVG format for Buttons");
        out.println("         button:bmp - BMP format for Buttons");
        out.println("         image:png_gif_jpeg - PNG/GIF/JPEG format for Images");
        out.println("         image:png - PNG format for Images");
        out.println("         image:jpeg - JPEG format for Images");
        out.println("         image:bmp - BMP format for Images");
        out.println("         text:plain - Plain text format for Texts");
        out.println("         text:formatted - Formatted text format for Texts");
        out.println("         text:svg - SVG format for Texts");
        out.println("         sound:mp3_wav_flv - MP3/WAV/FLV format for Sounds");
        out.println("         sound:mp3_wav - MP3/WAV format for Sounds");
        out.println("         sound:wav - WAV format for Sounds");
        out.println("         sound:flv - FLV format for Sounds");
        out.println("         font:ttf - TTF format for Fonts");
        out.println("         font:woff - WOFF format for Fonts");
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
        out.println(" " + (cnt++) + ") -compress <infile> <outfile> [(zlib|lzma)]");
        out.println("  ...Compress SWF <infile> and save it to <outfile>. If <infile> is already compressed, it will be re-compressed. Default compression method is ZLIB");
        out.println(" " + (cnt++) + ") -decompress <infile> <outfile>");
        out.println("  ...Decompress <infile> and save it to <outfile>");
        out.println(" " + (cnt++) + ") -swf2xml <infile> <outfile>");
        out.println("  ...Converts the <infile> SWF to <outfile> XML file");
        out.println(" " + (cnt++) + ") -xml2swf <infile> <outfile>");
        out.println("  ...Converts the <infile> XML to <outfile> SWF file");
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
        out.println(" " + (cnt++) + ") -replaceAlpha <infile> <outfile> <imageId1> <importDataFile1> [<imageId2> <importDataFile2>]...");
        out.println(" ...replaces the alpha channel of the specified JPEG3 or JPEG4 tag");
        out.println(" " + (cnt++) + ") -deobfuscate <level> <infile> <outfile>");
        out.println("  ...Deobfuscates AS3 P-code in <infile> and saves result to <outfile>");
        out.println("  ...<level> can be one of: controlflow/3/max, traps/2, deadcode/1");
        out.println("  ...WARNING: The deobfuscation result is still probably far enough to be openable by other decompilers.");
        printCmdLineUsageExamples(out);
    }

    private static void printCmdLineUsageExamples(PrintStream out) {
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
        out.println("java -jar ffdec.jar -compress myfile.swf myfilecomp.swf");
        out.println("java -jar ffdec.jar -decompress myfile.swf myfiledec.swf");
        out.println("java -jar ffdec.jar -onerror ignore -export script \"C:\\decompiled\" myfile.swf");
        out.println("java -jar ffdec.jar -onerror retry 5 -export script \"C:\\decompiled\" myfile.swf");
        out.println("java -jar ffdec.jar -config autoDeobfuscate=1,parallelSpeedUp=0 -export script \"C:\\decompiled\" myfile.swf");
        out.println("java -jar ffdec.jar -deobfuscate max myas3file_secure.swf myas3file.swf");
        out.println("");
        out.println("Instead of \"java -jar ffdec.jar\" you can use ffdec.bat on Windows, ffdec.sh on Linux/MacOs");
    }

    /**
     * Parses the console arguments
     *
     * @param arguments Arguments
     * @return paths to the file which should be opened or null
     * @throws java.io.IOException On error
     */
    public static String[] parseArguments(String[] arguments) throws IOException {
        Level traceLevel = Level.WARNING;
        Stack<String> args = new Stack<>();
        for (int i = arguments.length - 1; i >= 0; i--) {
            String arg = arguments[i];
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
            nextParamOriginal = args.pop();
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
                case "-stdout":
                    parseStdOut(args);
                    break;
                case "-stderr":
                    parseStdErr(args);
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
                case "--debugtool":
                    parseDebugTool(args);
                    break;
                case "--compareresources":
                    parseCompareResources(args);
                    break;
                default:
                    break OUTER;
            }
            if (args.isEmpty()) {
                return null;
            }
        }

        String command = "";
        if (nextParam.startsWith("-")) {
            command = nextParam.substring(1);
        }

        if (command.equals("removefromcontextmenu")) {
            if (!args.isEmpty()) {
                badArguments(command);
            }
            ContextMenuTools.addToContextMenu(false, true);
            System.exit(0);
        } else if (command.equals("addtocontextmenu")) {
            if (!args.isEmpty()) {
                badArguments(command);
            }
            ContextMenuTools.addToContextMenu(true, true);
            System.exit(0);
        } else if (command.equals("proxy")) {
            parseProxy(args);
        } else if (command.equals("export")) {
            parseExport(selectionClasses, selection, selectionIds, args, handler, traceLevel, format, zoom);
        } else if (command.equals("compress")) {
            parseCompress(args);
        } else if (command.equals("decompress")) {
            parseDecompress(args);
        } else if (command.equals("swf2xml")) {
            parseSwf2Xml(args);
        } else if (command.equals("xml2swf")) {
            parseXml2Swf(args);
        } else if (command.equals("extract")) {
            parseExtract(args);
        } else if (command.equals("deobfuscate")) {
            parseDeobfuscate(args);
        } else if (command.equals("renameinvalididentifiers")) {
            parseRenameInvalidIdentifiers(args);
        } else if (command.equals("dumpswf")) {
            parseDumpSwf(args);
        } else if (command.equals("dumpas2")) {
            parseDumpAS2(args);
        } else if (command.equals("dumpas3")) {
            parseDumpAS3(args);
        } else if (command.equals("flashpaper2pdf")) {
            parseFlashPaperToPdf(selection, zoom, args);
        } else if (command.equals("replace")) {
            parseReplace(args);
        } else if (command.equals("replacealpha")) {
            parseReplaceAlpha(args);
        } else if (command.equals("as3compiler")) {
            ActionScript3Parser.compile(null /*?*/, args.pop(), args.pop(), 0);
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
        } else {
            args.push(nextParamOriginal); // file names should be the original one
            String[] fileNames = args.toArray(new String[args.size()]);
            boolean allParamIsAFile = true;
            for (String fileName : fileNames) {
                File file = new File(fileName);
                if (!file.exists() || !file.isFile()) {
                    allParamIsAFile = false;
                }
            }

            if (allParamIsAFile) {
                return fileNames;
            } else {
                badArguments();
            }
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
        badArguments(null);
    }

    public static void badArguments(String command) {
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

    private static void parseConfig(Stack<String> args) {
        if (args.isEmpty()) {
            System.err.println("Config values expected");
            badArguments();
        }
        setConfigurations(args.pop());
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

    private static Selection parseSelect(Stack<String> args) {
        List<Range> ret = new ArrayList<>();
        if (args.isEmpty()) {
            System.err.println("range parameter expected");
            badArguments();
        }
        String range = args.pop();
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

    private static double parseZoom(Stack<String> args) {
        if (args.isEmpty()) {
            System.err.println("zoom parameter expected");
            badArguments();
        }
        try {
            return Double.parseDouble(args.pop());
        } catch (NumberFormatException nfe) {
            System.err.println("invalid zoom");
            badArguments();
        }
        return 1;
    }

    private static AbortRetryIgnoreHandler parseOnError(Stack<String> args) {
        int errorMode = AbortRetryIgnoreHandler.UNDEFINED;
        int retryCount = 0;

        if (args.isEmpty()) {
            System.err.println("onerror parameter expected");
            badArguments();
        }
        String errorModeParameter = args.pop();
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
                    retryCount = Integer.parseInt(args.pop());
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

    private static void parseTimeout(Stack<String> args) {
        if (args.isEmpty()) {
            System.err.println("timeout parameter expected");
            badArguments();
        }
        try {
            int timeout = Integer.parseInt(args.pop());
            Configuration.decompilationTimeoutSingleMethod.set(timeout);
        } catch (NumberFormatException nex) {
            System.err.println("Bad timeout value");
        }
    }

    private static void parseExportTimeout(Stack<String> args) {
        if (args.isEmpty()) {
            System.err.println("timeout parameter expected");
            badArguments();
        }
        try {
            int timeout = Integer.parseInt(args.pop());
            Configuration.exportTimeout.set(timeout);
        } catch (NumberFormatException nex) {
            System.err.println("Bad timeout value");
        }
    }

    private static void parseExportFileTimeout(Stack<String> args) {
        if (args.isEmpty()) {
            System.err.println("timeout parameter expected");
            badArguments();
        }
        try {
            int timeout = Integer.parseInt(args.pop());
            Configuration.decompilationTimeoutFile.set(timeout);
        } catch (NumberFormatException nex) {
            System.err.println("Bad timeout value");
        }
    }

    private static void parseStdOut(Stack<String> args) {
        if (args.isEmpty()) {
            System.err.println("stdOut parameter expected");
            badArguments();
        }

        stdOut = args.pop();
    }

    private static void parseStdErr(Stack<String> args) {
        if (args.isEmpty()) {
            System.err.println("stdErr parameter expected");
            badArguments();
        }

        stdErr = args.pop();
    }

    private static void parseAffinity(Stack<String> args) {
        if (Platform.isWindows()) {
            if (args.isEmpty()) {
                System.err.println("affinity parameter expected");
                badArguments();
            }
            try {
                int affinityMask = Integer.parseInt(args.pop());
                Kernel32.INSTANCE.SetProcessAffinityMask(Kernel32.INSTANCE.GetCurrentProcess(), affinityMask);
            } catch (NumberFormatException nex) {
                System.err.println("Bad affinityMask value");
            }
        } else {
            System.err.println("Process affinity setting is only available on Windows platform.");
        }
    }

    private static void parsePriority(Stack<String> args) {
        if (Platform.isWindows()) {
            if (args.isEmpty()) {
                System.err.println("priority parameter expected");
                badArguments();
            }
            String priority = args.pop();
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

    private static void parseDebugTool(Stack<String> args) {
        String cmd = args.pop().toLowerCase();
        switch (cmd) {
            case "findtag": {
                String folder = args.pop();
                String tagIdOrName = args.pop();
                int tagId;
                try {
                    tagId = Integer.parseInt(tagIdOrName);
                } catch (NumberFormatException e) {
                    tagId = Tag.getKnownClassesByName().get(tagIdOrName).getId();
                }

                PrintStream oldOut = System.out;
                PrintStream oldErr = System.err;
                PrintStream nullStream = new PrintStream(new OutputStream() {
                    @Override
                    public void write(int b) {
                    }
                });
                System.setOut(nullStream);
                System.setErr(nullStream);
                Main.initLogging(Configuration.debugMode.get());

                File[] files = new File(folder).listFiles(getSwfFilter());
                for (File file : files) {
                    SWFSourceInfo sourceInfo = new SWFSourceInfo(null, file.getAbsolutePath(), file.getName());
                    try {
                        SWF swf = new SWF(new FileInputStream(file), sourceInfo.getFile(), sourceInfo.getFileTitle(), Configuration.parallelSpeedUp.get());
                        swf.swfList = new SWFList();
                        swf.swfList.sourceInfo = sourceInfo;
                        boolean found = false;
                        for (Tag tag : swf.tags) {
                            if (tag.getId() == tagId) {
                                found = true;
                                break;
                            }
                        }

                        if (found) {
                            oldOut.println("Tag found in file: " + file.getAbsolutePath());
                        }
                    } catch (IOException | InterruptedException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }

                System.setOut(oldOut);
                System.setErr(oldErr);
                Main.initLogging(Configuration.debugMode.get());
                break;
            }
            case "finderrorheader": {
                String folder = args.pop();

                PrintStream oldOut = System.out;
                PrintStream oldErr = System.err;
                PrintStream nullStream = new PrintStream(new OutputStream() {
                    @Override
                    public void write(int b) {
                    }
                });
                System.setOut(nullStream);
                System.setErr(nullStream);
                Main.initLogging(Configuration.debugMode.get());

                File[] files = new File(folder).listFiles(getSwfFilter());
                for (File file : files) {
                    SWFSourceInfo sourceInfo = new SWFSourceInfo(null, file.getAbsolutePath(), file.getName());
                    try {
                        SWF swf = new SWF(new FileInputStream(file), sourceInfo.getFile(), sourceInfo.getFileTitle(), Configuration.parallelSpeedUp.get());
                        swf.swfList = new SWFList();
                        swf.swfList.sourceInfo = sourceInfo;
                        boolean found = false;
                        for (Tag tag : swf.tags) {
                            if (tag instanceof JPEGTablesTag) {
                                JPEGTablesTag jtt = (JPEGTablesTag) tag;
                                if (ImageTag.hasErrorHeader(jtt.jpegData)) {
                                    found = true;
                                    break;
                                }
                            } else if (tag instanceof DefineBitsJPEG2Tag) {
                                DefineBitsJPEG2Tag jpeg = (DefineBitsJPEG2Tag) tag;
                                if (ImageTag.hasErrorHeader(jpeg.imageData)) {
                                    found = true;
                                    break;
                                }
                            } else if (tag instanceof DefineBitsJPEG3Tag) {
                                DefineBitsJPEG3Tag jpeg = (DefineBitsJPEG3Tag) tag;
                                if (ImageTag.hasErrorHeader(jpeg.imageData)) {
                                    found = true;
                                    break;
                                }
                            }
                        }

                        if (found) {
                            oldOut.println("Tag found in file: " + file.getAbsolutePath());
                        }
                    } catch (IOException | InterruptedException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }

                System.setOut(oldOut);
                System.setErr(oldErr);
                Main.initLogging(Configuration.debugMode.get());
                break;
            }
        }
    }

    private static void parseCompareResources(Stack<String> args) {
        String revision = args.pop().toLowerCase();
        String revision2 = null;
        if (!args.isEmpty()) {
            revision2 = args.pop();
        }

        CheckResources.compareResources(System.out, revision, revision2);
    }

    private static void parseProxy(Stack<String> args) {
        int port = 55555;
        String portStr = args.peek();
        if (portStr != null && portStr.startsWith("-P")) {
            args.pop();
            try {
                port = Integer.parseInt(portStr.substring(2));
            } catch (NumberFormatException nex) {
                System.err.println("Bad port number");
            }
        }
        Main.startProxy(port);
    }

    private static List<String> parseSelectClassOld(Stack<String> args) {
        List<String> ret = new ArrayList<>();
        if (!args.isEmpty() && args.peek().equals("-selectas3class")) {
            args.pop();
            while (!args.isEmpty()) {
                ret.add(args.pop());
            }
            System.err.println("WARNING: Using deprecated -selectas3class parameter. Please use -selectclass instead. See --help for usage.");
        }
        return ret;

    }

    private static List<String> parseSelectClass(Stack<String> args) {
        if (args.size() < 1) {
            badArguments();
        }
        List<String> ret = new ArrayList<>();
        String classesStr = args.pop();
        String classes[];
        if (classesStr.contains(",")) {
            classes = classesStr.split(",");
        } else {
            classes = new String[]{classesStr};
        }
        ret.addAll(Arrays.asList(classes));
        return ret;

    }

    private static void parseExport(List<String> selectionClasses, Selection selection, Selection selectionIds, Stack<String> args, AbortRetryIgnoreHandler handler, Level traceLevel, Map<String, String> formats, double zoom) {
        if (args.size() < 3) {
            badArguments();
        }
        String[] validExportItems = new String[]{
            "script",
            "image",
            "shape",
            "morphshape",
            "movie",
            "font",
            "frame",
            "sprite",
            "button",
            "sound",
            "binarydata",
            "text",
            "all",
            "fla",
            "xfl"
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
        String exportFormatString = args.pop().toLowerCase();
        List<String> exportFormats = Arrays.asList(exportFormatString.split(","));
        long startTime = System.currentTimeMillis();

        File outDirBase = new File(args.pop());
        File inFileOrFolder = new File(args.pop());
        if (!inFileOrFolder.exists()) {
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
            File[] inFiles;
            boolean singleFile = true;
            if (inFileOrFolder.isDirectory()) {
                singleFile = false;
                inFiles = inFileOrFolder.listFiles(getSwfFilter());
            } else {
                inFiles = new File[]{inFileOrFolder};
            }

            for (File inFile : inFiles) {
                String inFileName = Path.getFileNameWithoutExtension(inFile);
                if (stdOut != null) {
                    System.setOut(new PrintStream(new FileOutputStream(stdOut.replace("{swfFile}", inFileName), true)));
                }

                if (stdErr != null) {
                    System.setErr(new PrintStream(new FileOutputStream(stdErr.replace("{swfFile}", inFileName), true)));
                    Main.initLogging(Configuration.debugMode.get());
                }

                long startTimeSwf = 0;
                if (!singleFile) {
                    startTimeSwf = System.currentTimeMillis();
                    System.out.println("Start exporting " + inFile.getName());
                }

                SWFSourceInfo sourceInfo = new SWFSourceInfo(null, inFile.getAbsolutePath(), inFile.getName());
                SWF swf;
                try {
                    swf = new SWF(new FileInputStream(inFile), sourceInfo.getFile(), sourceInfo.getFileTitle(), Configuration.parallelSpeedUp.get());
                } catch (SwfOpenException ex) {
                    logger.log(Level.SEVERE, "Failed to open swf: " + inFile.getName(), ex);
                    continue;
                }

                swf.swfList = new SWFList();
                swf.swfList.sourceInfo = sourceInfo;
                String outDir = outDirBase.getAbsolutePath();
                if (!singleFile) {
                    outDir = Path.combine(outDir, inFile.getName());
                }

                List<Tag> extags = new ArrayList<>();
                for (Tag t : swf.tags) {
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
                swf.addEventListener(new EventListener() {
                    @Override
                    public void handleExportingEvent(String type, int index, int count, Object data) {
                        if (level.intValue() <= Level.FINE.intValue()) {
                            String text = "Exporting ";
                            if (type != null && type.length() > 0) {
                                text += type + " ";
                            }
                            System.out.println(text + index + "/" + count + " " + data);
                        }
                    }

                    @Override
                    public void handleExportedEvent(String type, int index, int count, Object data) {
                        String text = "Exported ";
                        if (type != null && type.length() > 0) {
                            text += type + " ";
                        }
                        System.out.println(text + index + "/" + count + " " + data);
                    }

                    @Override
                    public void handleEvent(String event, Object data) {
                    }
                });

                // First check all the specified export formats
                for (String exportFormat : exportFormats) {
                    if (Arrays.asList(removedExportFormats).contains(exportFormat)) {
                        System.err.println("Error: Export format : " + exportFormat + " was REMOVED. Run application with --help parameter to see available formats.");
                        System.exit(1);
                    } else if (!Arrays.asList(validExportItems).contains(exportFormat)) {
                        System.err.println("Invalid export item:" + exportFormat);
                        badArguments();
                    }
                }

                // Here the exportFormats array should contain only validitems
                commandLineMode = true;
                boolean exportAll = exportFormats.contains("all");
                boolean multipleExportTypes = exportAll || exportFormats.size() > 1;
                EventListener evl = swf.getExportEventListener();

                if (exportAll || exportFormats.contains("image")) {
                    System.out.println("Exporting images...");
                    new ImageExporter().exportImages(handler, outDir + (multipleExportTypes ? File.separator + ImageExportSettings.EXPORT_FOLDER_NAME : ""), extags, new ImageExportSettings(enumFromStr(formats.get("image"), ImageExportMode.class)), evl);
                }

                if (exportAll || exportFormats.contains("shape")) {
                    System.out.println("Exporting shapes...");
                    new ShapeExporter().exportShapes(handler, outDir + (multipleExportTypes ? File.separator + ShapeExportSettings.EXPORT_FOLDER_NAME : ""), extags, new ShapeExportSettings(enumFromStr(formats.get("shape"), ShapeExportMode.class), zoom), evl);
                }

                if (exportAll || exportFormats.contains("morphshape")) {
                    System.out.println("Exporting morphshapes...");
                    new MorphShapeExporter().exportMorphShapes(handler, outDir + (multipleExportTypes ? File.separator + MorphShapeExportSettings.EXPORT_FOLDER_NAME : ""), extags, new MorphShapeExportSettings(enumFromStr(formats.get("morphshape"), MorphShapeExportMode.class), zoom), evl);
                }

                if (exportAll || exportFormats.contains("movie")) {
                    System.out.println("Exporting movies...");
                    new MovieExporter().exportMovies(handler, outDir + (multipleExportTypes ? File.separator + MovieExportSettings.EXPORT_FOLDER_NAME : ""), extags, new MovieExportSettings(enumFromStr(formats.get("movie"), MovieExportMode.class)), evl);
                }

                if (exportAll || exportFormats.contains("font")) {
                    System.out.println("Exporting fonts...");
                    new FontExporter().exportFonts(handler, outDir + (multipleExportTypes ? File.separator + FontExportSettings.EXPORT_FOLDER_NAME : ""), extags, new FontExportSettings(enumFromStr(formats.get("font"), FontExportMode.class)), evl);
                }

                if (exportAll || exportFormats.contains("sound")) {
                    System.out.println("Exporting sounds...");
                    new SoundExporter().exportSounds(handler, outDir + (multipleExportTypes ? File.separator + SoundExportSettings.EXPORT_FOLDER_NAME : ""), extags, new SoundExportSettings(enumFromStr(formats.get("sound"), SoundExportMode.class)), evl);
                }

                if (exportAll || exportFormats.contains("binarydata")) {
                    System.out.println("Exporting binaryData...");
                    new BinaryDataExporter().exportBinaryData(handler, outDir + (multipleExportTypes ? File.separator + BinaryDataExportSettings.EXPORT_FOLDER_NAME : ""), extags, new BinaryDataExportSettings(enumFromStr(formats.get("binarydata"), BinaryDataExportMode.class)), evl);
                }

                if (exportAll || exportFormats.contains("text")) {
                    System.out.println("Exporting texts...");
                    Boolean singleTextFile = parseBooleanConfigValue(formats.get("singletext"));
                    if (singleTextFile == null) {
                        singleTextFile = Configuration.textExportSingleFile.get();
                    }
                    new TextExporter().exportTexts(handler, outDir + (multipleExportTypes ? File.separator + TextExportSettings.EXPORT_FOLDER_NAME : ""), extags, new TextExportSettings(enumFromStr(formats.get("text"), TextExportMode.class), singleTextFile, zoom), evl);
                }

                FrameExporter frameExporter = new FrameExporter();

                if (exportAll || exportFormats.contains("frame")) {
                    System.out.println("Exporting frames...");
                    List<Integer> frames = new ArrayList<>();
                    for (int i = 0; i < swf.frameCount; i++) {
                        if (selection.contains(i + 1)) {
                            frames.add(i);
                        }
                    }
                    FrameExportSettings fes = new FrameExportSettings(enumFromStr(formats.get("frame"), FrameExportMode.class), zoom);
                    frameExporter.exportFrames(handler, outDir + (multipleExportTypes ? File.separator + FrameExportSettings.EXPORT_FOLDER_NAME : ""), swf, 0, frames, fes, evl);
                }

                if (exportAll || exportFormats.contains("sprite")) {
                    System.out.println("Exporting sprite...");
                    SpriteExportSettings ses = new SpriteExportSettings(enumFromStr(formats.get("sprite"), SpriteExportMode.class), zoom);
                    for (CharacterTag c : swf.getCharacters().values()) {
                        if (c instanceof DefineSpriteTag) {
                            frameExporter.exportFrames(handler, outDir + (multipleExportTypes ? File.separator + SpriteExportSettings.EXPORT_FOLDER_NAME : ""), swf, c.getCharacterId(), null, ses, evl);
                        }
                    }
                }

                if (exportAll || exportFormats.contains("button")) {
                    System.out.println("Exporting buttons...");
                    ButtonExportSettings bes = new ButtonExportSettings(enumFromStr(formats.get("button"), ButtonExportMode.class), zoom);
                    for (CharacterTag c : swf.getCharacters().values()) {
                        if (c instanceof ButtonTag) {
                            List<Integer> frameNums = new ArrayList<>();
                            frameNums.add(0); // todo: export all frames
                            frameExporter.exportFrames(handler, outDir + (multipleExportTypes ? File.separator + ButtonExportSettings.EXPORT_FOLDER_NAME : ""), swf, c.getCharacterId(), frameNums, bes, evl);
                        }
                    }
                }

                boolean parallel = Configuration.parallelSpeedUp.get();
                Boolean singleScriptFile = parseBooleanConfigValue(formats.get("singlescript"));
                if (singleScriptFile == null) {
                    singleScriptFile = Configuration.scriptExportSingleFile.get();
                }

                if (parallel && singleScriptFile) {
                    System.out.println("Single file script export is not supported with enabled parallel speedup");
                    singleScriptFile = false;
                }

                ScriptExportSettings scriptExportSettings = new ScriptExportSettings(enumFromStr(formats.get("script"), ScriptExportMode.class), singleScriptFile);
                if (exportAll || exportFormats.contains("script")) {
                    System.out.println("Exporting scripts...");
                    if (as3classes.isEmpty()) {
                        as3classes = parseSelectClassOld(args);
                    }

                    String scriptsFolder = Path.combine(outDir, ScriptExportSettings.EXPORT_FOLDER_NAME);
                    Path.createDirectorySafe(new File(scriptsFolder));
                    String singleFileName = Path.combine(scriptsFolder, swf.getShortFileName() + scriptExportSettings.getFileExtension());
                    try (FileTextWriter writer = scriptExportSettings.singleFile ? new FileTextWriter(Configuration.getCodeFormatting(), new FileOutputStream(singleFileName)) : null) {
                        scriptExportSettings.singleFileWriter = writer;
                        if (!as3classes.isEmpty()) {
                            for (String as3class : as3classes) {
                                exportOK = exportOK && swf.exportAS3Class(as3class, scriptsFolder, scriptExportSettings, parallel, evl);
                            }
                        } else {
                            exportOK = exportOK && swf.exportActionScript(handler, scriptsFolder, scriptExportSettings, parallel, evl) != null;
                        }
                    }
                }

                if (exportFormats.contains("fla")) {
                    System.out.println("Exporting FLA...");
                    FLAVersion flaVersion = FLAVersion.fromString(formats.get("fla"));
                    if (flaVersion == null) {
                        flaVersion = FLAVersion.CS6; //Defaults to CS6
                    }
                    swf.exportFla(handler, outDir + (multipleExportTypes ? File.separator + "fla" : ""), inFile.getName(), ApplicationInfo.APPLICATION_NAME, ApplicationInfo.applicationVerName, ApplicationInfo.version, Configuration.parallelSpeedUp.get(), flaVersion);
                }

                if (exportFormats.contains("xfl")) {
                    System.out.println("Exporting XFL...");
                    FLAVersion xflVersion = FLAVersion.fromString(formats.get("xfl"));
                    if (xflVersion == null) {
                        xflVersion = FLAVersion.CS6; //Defaults to CS6
                    }
                    swf.exportXfl(handler, outDir + (multipleExportTypes ? File.separator + "xfl" : ""), inFile.getName(), ApplicationInfo.APPLICATION_NAME, ApplicationInfo.applicationVerName, ApplicationInfo.version, Configuration.parallelSpeedUp.get(), xflVersion);
                }

                if (!singleFile) {
                    long stopTimeSwf = System.currentTimeMillis();
                    long time = stopTimeSwf - startTimeSwf;
                    System.out.println("Export finished: " + inFile.getName() + " Export time: " + Helper.formatTimeSec(time));
                }

                swf.clearAllCache();
            }
        } catch (OutOfMemoryError | Exception ex) {
            System.err.print("FAIL: Exporting Failed on Exception - ");
            logger.log(Level.SEVERE, null, ex);
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

    private static void parseDeobfuscate(Stack<String> args) {
        if (args.size() < 3) {
            badArguments();
        }
        String mode = args.pop();
        DeobfuscationLevel lev = null;
        switch (mode) {
            case "controlflow":
            case "max":
            case "3":
                lev = DeobfuscationLevel.LEVEL_RESTORE_CONTROL_FLOW;
                break;
            case "traps":
            case "2":
                lev = DeobfuscationLevel.LEVEL_REMOVE_TRAPS;
                break;
            case "deadcode":
            case "1":
                lev = DeobfuscationLevel.LEVEL_REMOVE_DEAD_CODE;
                break;
            default:
                System.err.println("Invalid level, must be one of: controlflow,traps,deadcode or 1,2, 3/max");
                System.exit(1);
                break;
        }
        File inFile = new File(args.pop());
        File outFile = new File(args.pop());
        File tmpFile = null;
        if (inFile.equals(outFile)) {
            try {
                tmpFile = File.createTempFile("ffdec_deobf_", ".swf");
                outFile = tmpFile;
            } catch (IOException ex) {
                System.err.println("Unable to create temp file");
                System.exit(1);
            }
        }
        try (FileInputStream is = new FileInputStream(inFile);
                FileOutputStream fos = new FileOutputStream(outFile)) {
            SWF swf = new SWF(is, Configuration.parallelSpeedUp.get());
            if (!swf.isAS3()) {
                System.out.println("Warning: The file is not AS3. Only AS3 deobfuscation from commandline is available.");
                System.exit(0);
            }
            swf.deobfuscate(lev);
            swf.saveTo(fos);
            if (tmpFile != null) {
                inFile.delete();
                tmpFile.renameTo(inFile);
                tmpFile = null;
                System.out.println("" + inFile + " overwritten.");
            }
            System.out.println("OK");
        } catch (FileNotFoundException ex) {
            System.err.println("File not found.");
            System.exit(1);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
            System.exit(1);
        } catch (IOException ex) {
            Logger.getLogger(CommandLineArgumentParser.class.getName()).log(Level.SEVERE, "Error", ex);
            System.exit(1);
        } finally {
            if (tmpFile != null && tmpFile.exists()) {
                tmpFile.delete();
            }
        }
    }

    private static void parseCompress(Stack<String> args) {
        if (args.size() < 2) {
            badArguments();
        }

        try {
            try (InputStream fis = new BufferedInputStream(new FileInputStream(args.pop()));
                    OutputStream fos = new BufferedOutputStream(new FileOutputStream(args.pop()))) {
                SWFCompression compression = SWFCompression.ZLIB;
                String compressionString = !args.isEmpty() ? args.pop() : null;
                if (compressionString != null) {
                    switch (compressionString.toLowerCase()) {
                        case "zlib":
                            compression = SWFCompression.ZLIB;
                            break;
                        case "lzma":
                            compression = SWFCompression.LZMA;
                            break;
                        default:
                            System.out.println("Unsupported compression method: " + compressionString);
                            System.exit(0);
                            break;
                    }
                }

                if (SWF.compress(fis, fos, compression)) {
                    System.out.println("OK");
                } else {
                    System.err.println("FAIL");
                }
            } catch (FileNotFoundException ex) {
                System.err.println("File not found.");
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        System.exit(0);
    }

    private static void parseDecompress(Stack<String> args) {
        if (args.size() < 2) {
            badArguments();
        }

        try {
            try (InputStream fis = new BufferedInputStream(new FileInputStream(args.pop()));
                    OutputStream fos = new BufferedOutputStream(new FileOutputStream(args.pop()))) {
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
            logger.log(Level.SEVERE, null, ex);
        }

        System.exit(0);
    }

    private static void parseSwf2Xml(Stack<String> args) {
        if (args.size() < 2) {
            badArguments();
        }

        try {
            try (FileInputStream is = new FileInputStream(args.pop())) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get());
                new SwfXmlExporter().exportXml(swf, new File(args.pop()));
            } catch (FileNotFoundException ex) {
                System.err.println("File not found.");
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        System.exit(0);
    }

    private static void parseXml2Swf(Stack<String> args) {
        if (args.size() < 2) {
            badArguments();
        }

        try {
            String xml = Helper.readTextFile(args.pop());
            SWF swf = new SWF();
            new SwfXmlImporter().importSwf(swf, xml);
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(new File(args.pop())))) {
                swf.saveTo(new BufferedOutputStream(fos));
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        System.exit(0);
    }

    private static void parseExtract(Stack<String> args) {
        if (args.size() < 1) {
            badArguments();
        }

        String fileName = args.pop();
        SearchMode mode = SearchMode.ALL;

        boolean noCheck = false;
        String output = null;

        if (args.size() > 0 && args.peek().toLowerCase().equals("-o")) {
            args.pop();
            if (args.size() < 1) {
                badArguments();
            }
            output = args.pop();
        }

        if (args.size() > 0 && args.peek().toLowerCase().equals("nocheck")) {
            noCheck = true;
            args.pop();
        }

        if (args.size() > 0) {
            String modeStr = args.pop().toLowerCase();
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
            logger.log(Level.SEVERE, null, ex);
        }

        System.exit(0);
    }

    private static void parseRenameInvalidIdentifiers(Stack<String> args) {
        if (args.size() < 3) {
            badArguments();
        }

        String renameTypeStr = args.pop();
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
            try (InputStream fis = new BufferedInputStream(new FileInputStream(args.pop()));
                    OutputStream fos = new BufferedOutputStream(new FileOutputStream(args.pop()))) {
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
            logger.log(Level.SEVERE, null, ex);
        }

        System.exit(0);
    }

    private static Map<String, String> parseFormat(Stack<String> args) {
        if (args.size() < 1) {
            badArguments();
        }
        String fmtStr = args.pop();
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

    private static void parseFlashPaperToPdf(Selection selection, double zoom, Stack<String> args) {
        if (args.size() < 2) {
            badArguments();
        }
        File inFile = new File(args.pop());
        File outFile = new File(args.pop());
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
                        job = new PDFJob(new BufferedOutputStream(new FileOutputStream(outFile)));
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

    private static void parseReplace(Stack<String> args) {
        if (args.size() < 4) {
            badArguments();
        }

        File inFile = new File(args.pop());
        File outFile = new File(args.pop());
        try {
            try (FileInputStream is = new FileInputStream(inFile)) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get());
                while (true) {
                    String objectToReplace = args.pop();

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
                        String repFile = args.pop();
                        byte[] data = Helper.readFile(repFile);
                        if (characterTag instanceof DefineBinaryDataTag) {
                            DefineBinaryDataTag defineBinaryData = (DefineBinaryDataTag) characterTag;
                            new BinaryDataImporter().importData(defineBinaryData, data);
                        } else if (characterTag instanceof ImageTag) {
                            ImageTag imageTag = (ImageTag) characterTag;
                            new ImageImporter().importImage(imageTag, data);
                        } else if (characterTag instanceof ShapeTag) {
                            ShapeTag shapeTag = (ShapeTag) characterTag;
                            new ShapeImporter().importImage(shapeTag, data);
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
                            System.err.println("The specified tag type is not supported for import");
                            System.exit(1);
                        }
                    } else {
                        Map<String, ASMSource> asms = swf.getASMs(false);
                        boolean found = false;
                        if (asms.containsKey(objectToReplace)) {
                            found = true;
                            // replace AS1/2
                            String repFile = args.pop();
                            String repText = Helper.readTextFile(repFile);
                            ASMSource src = asms.get(objectToReplace);
                            if (Path.getExtension(repFile).equals(".as")) {
                                replaceAS2(repText, src);
                            } else {
                                replaceAS2PCode(repText, src);
                            }
                        } else {
                            List<ScriptPack> packs = swf.getAS3Packs();
                            for (ScriptPack entry : packs) {
                                if (entry.getClassPath().toString().equals(objectToReplace)) {
                                    found = true;
                                    // replace AS3
                                    String repFile = args.pop();
                                    String repText = Helper.readTextFile(repFile);
                                    ScriptPack pack = entry;
                                    if (Path.getExtension(repFile).equals(".as")) {
                                        replaceAS3(repText, pack);
                                    } else {
                                        // todo: get traits
                                        if (args.isEmpty()) {
                                            badArguments();
                                        }
                                        int bodyIndex = Integer.parseInt(args.pop());
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
                    try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(outFile))) {
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

    private static void parseReplaceAlpha(Stack<String> args) {
        if (args.size() < 4) {
            badArguments();
        }

        File inFile = new File(args.pop());
        File outFile = new File(args.pop());
        try {
            try (FileInputStream is = new FileInputStream(inFile)) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get());
                while (true) {
                    String objectToReplace = args.pop();

                    int imageId = 0;
                    try {
                        imageId = Integer.parseInt(objectToReplace);
                    } catch (NumberFormatException nfe) {
                        System.err.println("ImageId should be integer");
                        System.exit(1);
                    }
                    if (!swf.getCharacters().containsKey(imageId)) {
                        System.err.println("ImageId does not exist");
                        System.exit(1);
                    }

                    CharacterTag characterTag = swf.getCharacter(imageId);
                    String repFile = args.pop();
                    byte[] data = Helper.readFile(repFile);
                    if (characterTag instanceof DefineBitsJPEG3Tag || characterTag instanceof DefineBitsJPEG4Tag) {
                        ImageTag imageTag = (ImageTag) characterTag;
                        new ImageImporter().importImageAlpha(imageTag, data);
                    } else {
                        System.err.println("The specified tag type is not supported for alpha channel import");
                        System.exit(1);
                    }

                    if (args.isEmpty() || args.peek().startsWith("-")) {
                        break;
                    }
                }

                try {
                    try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(outFile))) {
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
        if (text.trim().startsWith(Helper.hexData)) {
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
        ActionScript2Parser par = new ActionScript2Parser(src.getSwf().version);
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
        if (text.trim().startsWith(Helper.hexData)) {
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
            pack.abc.replaceScriptPack(pack, as);
        } catch (AVM2ParseException ex) {
            System.err.println("%error% on line %line%".replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)));
            System.exit(1);
        } catch (CompilationException ex) {
            System.err.println("%error% on line %line%".replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)));
            System.exit(1);
        }
    }

    private static void parseDumpSwf(Stack<String> args) {
        if (args.isEmpty()) {
            badArguments();
        }
        try {
            Configuration.dumpTags.set(true);
            Configuration.parallelSpeedUp.set(false);
            SWFSourceInfo sourceInfo = new SWFSourceInfo(null, args.pop(), null);
            Main.parseSWF(sourceInfo);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        System.exit(0);
    }

    private static void parseDumpAS2(Stack<String> args) {
        if (args.isEmpty()) {
            badArguments();
        }
        File file = new File(args.pop());
        try {
            try (FileInputStream is = new FileInputStream(file)) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get());
                Map<String, ASMSource> asms = swf.getASMs(false);
                for (String as2 : asms.keySet()) {
                    System.out.println(as2);
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("I/O error during reading");
            System.exit(2);
        }
    }

    private static void parseDumpAS3(Stack<String> args) {
        if (args.isEmpty()) {
            badArguments();
        }
        File file = new File(args.pop());
        try {
            try (FileInputStream is = new FileInputStream(file)) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get());
                List<ScriptPack> packs = swf.getAS3Packs();
                for (ScriptPack entry : packs) {
                    System.out.println(entry.getClassPath().toString() + " " + entry.scriptIndex);
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("I/O error during reading");
            System.exit(2);
        }
    }

    private static FilenameFilter getSwfFilter() {
        return (File dir, String name) -> name.toLowerCase().endsWith(".swf");
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
