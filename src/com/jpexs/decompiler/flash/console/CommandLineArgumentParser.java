/*
 *  Copyright (C) 2010-2024 JPEXS
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
import com.jpexs.decompiler.flash.Bundle;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.OpenableSourceInfo;
import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFCompression;
import com.jpexs.decompiler.flash.SearchMode;
import com.jpexs.decompiler.flash.SwfOpenException;
import com.jpexs.decompiler.flash.ValueTooLargeException;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ABCInputStream;
import com.jpexs.decompiler.flash.abc.RenameType;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.DeobfuscationLevel;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.ASM3Parser;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.MissingSymbolHandler;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.ActionScript3Parser;
import com.jpexs.decompiler.flash.abc.types.Float4;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.usages.simple.ABCCleaner;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.amf.amf3.Amf3InputStream;
import com.jpexs.decompiler.flash.amf.amf3.Amf3OutputStream;
import com.jpexs.decompiler.flash.amf.amf3.Amf3Value;
import com.jpexs.decompiler.flash.amf.amf3.NoSerializerExistsException;
import com.jpexs.decompiler.flash.amf.amf3.Traits;
import com.jpexs.decompiler.flash.amf.amf3.types.ObjectType;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.ConfigurationItem;
import com.jpexs.decompiler.flash.docs.As12PCodeDocs;
import com.jpexs.decompiler.flash.docs.As3PCodeDocs;
import com.jpexs.decompiler.flash.exporters.BinaryDataExporter;
import com.jpexs.decompiler.flash.exporters.DualPdfGraphics2D;
import com.jpexs.decompiler.flash.exporters.Font4Exporter;
import com.jpexs.decompiler.flash.exporters.FontExporter;
import com.jpexs.decompiler.flash.exporters.FrameExporter;
import com.jpexs.decompiler.flash.exporters.ImageExporter;
import com.jpexs.decompiler.flash.exporters.MorphShapeExporter;
import com.jpexs.decompiler.flash.exporters.MovieExporter;
import com.jpexs.decompiler.flash.exporters.ShapeExporter;
import com.jpexs.decompiler.flash.exporters.SoundExporter;
import com.jpexs.decompiler.flash.exporters.SymbolClassExporter;
import com.jpexs.decompiler.flash.exporters.TextExporter;
import com.jpexs.decompiler.flash.exporters.amf.amf3.Amf3Exporter;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.modes.BinaryDataExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ButtonExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ExeExportMode;
import com.jpexs.decompiler.flash.exporters.modes.Font4ExportMode;
import com.jpexs.decompiler.flash.exporters.modes.FontExportMode;
import com.jpexs.decompiler.flash.exporters.modes.FrameExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ImageExportMode;
import com.jpexs.decompiler.flash.exporters.modes.MorphShapeExportMode;
import com.jpexs.decompiler.flash.exporters.modes.MovieExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ShapeExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SoundExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SpriteExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SymbolClassExportMode;
import com.jpexs.decompiler.flash.exporters.modes.TextExportMode;
import com.jpexs.decompiler.flash.exporters.script.LinkReportExporter;
import com.jpexs.decompiler.flash.exporters.settings.BinaryDataExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.ButtonExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.Font4ExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.FontExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.FrameExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.ImageExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.MorphShapeExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.MovieExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.ShapeExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.SoundExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.SpriteExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.SymbolClassExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.TextExportSettings;
import com.jpexs.decompiler.flash.exporters.swf.SwfToSwcExporter;
import com.jpexs.decompiler.flash.exporters.swf.SwfXmlExporter;
import com.jpexs.decompiler.flash.flexsdk.MxmlcAs3ScriptReplacer;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.SearchInMemory;
import com.jpexs.decompiler.flash.gui.SearchInMemoryListener;
import com.jpexs.decompiler.flash.gui.SwfInMemory;
import com.jpexs.decompiler.flash.gui.helpers.CheckResources;
import com.jpexs.decompiler.flash.gui.translator.Translator;
import com.jpexs.decompiler.flash.helpers.FileTextWriter;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerPlugin;
import com.jpexs.decompiler.flash.importers.AS2ScriptImporter;
import com.jpexs.decompiler.flash.importers.AS3ScriptImporter;
import com.jpexs.decompiler.flash.importers.As3ScriptReplaceException;
import com.jpexs.decompiler.flash.importers.As3ScriptReplaceExceptionItem;
import com.jpexs.decompiler.flash.importers.As3ScriptReplacerFactory;
import com.jpexs.decompiler.flash.importers.As3ScriptReplacerInterface;
import com.jpexs.decompiler.flash.importers.BinaryDataImporter;
import com.jpexs.decompiler.flash.importers.FFDecAs3ScriptReplacer;
import com.jpexs.decompiler.flash.importers.FontImporter;
import com.jpexs.decompiler.flash.importers.ImageImporter;
import com.jpexs.decompiler.flash.importers.MorphShapeImporter;
import com.jpexs.decompiler.flash.importers.MovieImporter;
import com.jpexs.decompiler.flash.importers.ScriptImporterProgressListener;
import com.jpexs.decompiler.flash.importers.ShapeImporter;
import com.jpexs.decompiler.flash.importers.SoundImporter;
import com.jpexs.decompiler.flash.importers.SpriteImporter;
import com.jpexs.decompiler.flash.importers.SwfXmlImporter;
import com.jpexs.decompiler.flash.importers.SymbolClassImporter;
import com.jpexs.decompiler.flash.importers.TextImporter;
import com.jpexs.decompiler.flash.importers.amf.amf3.Amf3Importer;
import com.jpexs.decompiler.flash.importers.amf.amf3.Amf3ParseException;
import com.jpexs.decompiler.flash.importers.svg.SvgImporter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG3Tag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.JPEGTablesTag;
import com.jpexs.decompiler.flash.tags.PlaceObject4Tag;
import com.jpexs.decompiler.flash.tags.ScriptLimitsTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.HasSeparateAlphaChannel;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MissingCharacterHandler;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RenderContext;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.SoundImportException;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.tags.base.TextImportErrorHandler;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.base.UnsupportedSamplingRateException;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.OpenableList;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.sound.SoundFormat;
import com.jpexs.decompiler.flash.xfl.FLAVersion;
import com.jpexs.decompiler.flash.xfl.XFLExportSettings;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.MemoryInputStream;
import com.jpexs.helpers.Path;
import com.jpexs.helpers.ProgressListener;
import com.jpexs.helpers.SerializableImage;
import com.jpexs.helpers.stat.StatisticData;
import com.jpexs.helpers.stat.Statistics;
import com.jpexs.helpers.streams.SeekableInputStream;
import com.jpexs.helpers.utf8.Utf8Helper;
import com.jpexs.process.Process;
import com.jpexs.process.ProcessTools;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;
import gnu.jpdf.PDFGraphics;
import gnu.jpdf.PDFJob;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
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
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import macromedia.asc.util.Decimal128;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

/**
 * @author JPEXS
 */
public class CommandLineArgumentParser {

    private static final Logger logger = Logger.getLogger(CommandLineArgumentParser.class.getName());

    private static boolean commandLineMode = false;

    private static boolean showStat = false;

    private static String stdOut = null;

    private static String stdErr = null;

    private static final String METADATA_FORMAT_JSLIKE = "jslike";

    private static final String METADATA_FORMAT_RAW = "raw";

    public static boolean isCommandLineMode() {
        return commandLineMode;
    }

    public static void printConfigurationSettings() {
        Map<String, Field> fields = Configuration.getConfigurationFields();
        String[] keys = new String[fields.size()];
        keys = fields.keySet().toArray(keys);
        Arrays.sort(keys);

        System.out.println("Available keys[current setting]-type:");
        for (String name : keys) {
            Field field = fields.get(name);
            if (ConfigurationItem.isInternal(field)) {
                continue;
            }

            ConfigurationItem<?> item = ConfigurationItem.getItem(field);
            Object value = item.get();
            Class<?> type = ConfigurationItem.getConfigurationFieldType(field);
            String valueString = objectToString(value, type);
            String typeString = objectTypeToString(type);

            if (typeString != null) {
                System.out.println(name + "[" + valueString + "]-" + typeString);
            }
        }
    }

    private static String objectTypeToString(Class<?> type) {
        if (type == String.class) {
            return "string";
        } else if (type == Calendar.class) {
            return "date";
        } else if ((type == Integer.class) || (type == Long.class)) {
            return "integer";
        } else if ((type == Double.class) || (type == Float.class)) {
            return "float";
        } else if (type == Boolean.class) {
            return "bool";
        } else if (type.isEnum()) {
            return "enum";
        }

        return null;
    }

    private static String objectToString(Object obj, Class<?> type) {
        if (obj == null) {
            return "null";
        }

        if (type == String.class) {
            //return '"' + obj.toString() + '"';
            return obj.toString();
        } else if (type == Calendar.class) {
            return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(((Calendar) obj).getTime());
        } else if ((type == Integer.class) || (type == Long.class) || (type == Double.class) || (type == Float.class)) {
            return obj.toString();
        } else if (type == Boolean.class) {
            return ((boolean) (Boolean) obj) ? "true" : "false";
        } else if (type.isEnum()) {
            return obj.toString();
        }

        return null;
    }

    public static void printCmdLineUsage(String filter, boolean webHelp) {
        printCmdLineUsage(ansiSystemOut(), webHelp, filter);
    }

    public static void printCmdLineUsage(PrintStream out, boolean webHelp, String filter) {
        CommandLineHelp.printCmdLineUsage(out, filter);

        printCmdLineUsageExamples(out, filter);

        System.out.println();
        System.out.println("You can use special value \"/dev/stdin\" for input files to read data \r\nfrom standard input (even on Windows)");
    }

    private static void printCmdLineUsageExamples(PrintStream out, String filter) {
        if (filter == null) {
            return;
        }
        AnsiConsole.systemInstall();
        out.println();
        out.println("@|underline,bold Examples|@:");

        final String PREFIX = "ffdec ";

        boolean exampleFound = false;
        if (filter == null) {
            out.println(PREFIX + "myfile.swf");
            exampleFound = true;
        }      

        if (filter == null || filter.equals("export") || filter.equals("format") || filter.equals("selectclass") || filter.equals("onerror")) {
            out.println(PREFIX + "-export script \"C:\\decompiled\" myfile.swf");
            out.println(PREFIX + "-selectclass com.example.MyClass,com.example.SecondClass -export script \"C:\\decompiled\" myfile.swf");
            out.println(PREFIX + "-format script:pcode -export script \"C:\\decompiled\" myfile.swf");
            out.println(PREFIX + "-format script:pcode,text:plain -export script,text,image \"C:\\decompiled\" myfile.swf");
            out.println(PREFIX + "-format fla:cs5.5 -export fla \"C:\\sources\\myfile.fla\" myfile.swf");
            out.println(PREFIX + "-onerror ignore -export script \"C:\\decompiled\" myfile.swf");
            out.println(PREFIX + "-onerror retry 5 -export script \"C:\\decompiled\" myfile.swf");
            exampleFound = true;
        }

        if (filter == null || filter.equals("cli")) {
            out.println(PREFIX + "-cli myfile.swf");
            exampleFound = true;
        }

        if (filter == null || filter.equals("dumpswf")) {
            out.println(PREFIX + "-dumpSWF myfile.swf");
            exampleFound = true;
        }

        if (filter == null || filter.equals("compress")) {
            out.println(PREFIX + "-compress myfile.swf myfilecomp.swf");
            exampleFound = true;
        }

        if (filter == null || filter.equals("decompress")) {
            out.println(PREFIX + "-decompress myfile.swf myfiledec.swf");
            exampleFound = true;
        }

        if (filter == null || filter.equals("config")) {
            out.println(PREFIX + "-config autoDeobfuscate=1,parallelSpeedUp=0 export script \"C:\\decompiled\" myfile.swf");
            exampleFound = true;
        }

        if (filter == null || filter.equals("deobfuscate")) {
            out.println(PREFIX + "-deobfuscate max myas3file_secure.swf myas3file.swf");
            exampleFound = true;
        }

        if (filter == null || filter.equals("enabledebugging")) {
            out.println(PREFIX + "-enabledebugging -injectas3 myas3file.swf myas3file_debug.swf");
            out.println(PREFIX + "-enabledebugging -generateswd myas2file.swf myas2file_debug.swf");
            exampleFound = true;
        }

        if (filter == null || filter.equals("doc")) {
            out.println(PREFIX + "-doc -type as3.pcode.instructions -format html");
            out.println(PREFIX + "-doc -type as12.pcode.instructions -format html");
            out.println(PREFIX + "-doc -type as3.pcode.instructions -format html -locale en -out as3_docs_en.html");
            exampleFound = true;
        }

        if (filter == null || filter.equals("getinstancemetadata")) {
            out.println(PREFIX + "-getInstanceMetadata -instance myobj -key keyone myfile.swf");
            out.println(PREFIX + "-getInstanceMetadata -instance myobj2 -outputFormat raw -outfile out.amf myfile.swf");
            exampleFound = true;
        }
        if (filter == null || filter.equals("setinstancemetadata")) {
            out.println(PREFIX + "-setInstanceMetadata -instance myobj -key mykey -value 1234 myfile.swf");
            out.println(PREFIX + "-setInstanceMetadata -instance myobj -key my -inputFormat raw -datafile value.amf -outfile modified.swf myfile.swf");
            exampleFound = true;
        }

        if (filter == null || filter.equals("removeinstancemetadata")) {
            out.println(PREFIX + "-removeInstanceMetadata -instance myobj -key mykey -outfile result.swf myfile.swf");
            out.println(PREFIX + "-removeInstanceMetadata -instance myobj myfile.swf");
            exampleFound = true;
        }

        if (filter == null || filter.equals("swf2exe")) {
            out.println(PREFIX + "-swf2exe wrapper result.exe myfile.swf");
            exampleFound = true;
        }

        if (filter == null || filter.equals("header")) {
            out.println(PREFIX + "-header myfile.swf");
            out.println(PREFIX + "-header -set version 10 -set width 800px -set framerate 23.5 myfile.swf outfile.swf");
            out.println(PREFIX + "-header -set displayrect [0,0,800px,600px] myfile.swf outfile.swf");
            out.println(PREFIX + "-header -set gfx true myfile.swf outfile.swf");
            exampleFound = true;
        }

        if (!exampleFound) {
            out.println("Sorry, no example found for command " + filter + ",\r\nLet us know in issue tracker when you need it.");
        }

        AnsiConsole.systemUninstall();

        //out.println();
        //out.println("Instead of \"java -jar ffdec.jar\" you can use ffdec.bat on Windows, ffdec.sh on Linux/MacOs");
    }

    /**
     * Parses the console arguments
     *
     * @param arguments Arguments
     * @return paths to the file which should be opened or null
     * @throws java.io.IOException On error
     */
    public static String[] parseArguments(String[] arguments) throws IOException {
        return parseArgumentsInternal(arguments);
    }

    private static PrintStream ansiSystemOut() {
        return new PrintStream(System.out) {
            @Override
            public void print(String s) {
                super.print(Ansi.ansi().render(s));
            }
        };
    }

    private static void ansiPrint(String str) {
        System.out.print(Ansi.ansi().render(str));
    }

    private static void ansiPrintln(String str) {
        System.out.println(Ansi.ansi().render(str));
    }

    private static String[] parseArgumentsInternal(String[] arguments) throws IOException {
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
        String charset = Charset.defaultCharset().name();
        boolean cliMode = false;
        boolean air = false;
        boolean exportEmbed = false;
        boolean resampleWav = false;
        boolean transparentBackground = false;
        Selection selection = new Selection();
        Selection selectionIds = new Selection();
        List<String> selectionClasses = null;
        String nextParam = null;
        String nextParamOriginal = null;
        OUTER:
        while (true) {
            nextParamOriginal = args.pop();
            if (nextParamOriginal != null) {
                nextParam = nextParamOriginal.toLowerCase(Locale.ENGLISH);
            }
            if (nextParam == null) {
                nextParam = "";
            }
            switch (nextParam) {
                case "-air":
                    air = true;
                    break;
                case "-cli":
                    cliMode = true;
                    break;
                case "-selectid":
                    selectionIds = parseSelect(args);
                    break;
                case "-select":
                    selection = parseSelect(args);
                    break;
                case "-selectclass":
                    selectionClasses = parseSelectClass(args);
                    break;
                case "-exportembed":
                    exportEmbed = true;
                    break;
                case "-resamplewav":
                    resampleWav = true;
                    break;
                case "-ignorebackground":
                    transparentBackground = true;
                    break;
                case "-zoom":
                    zoom = parseZoom(args);
                    break;
                case "-format":
                    format = parseFormat(args);
                    break;
                case "-charset":
                    charset = parseCharset(args);
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
                case "-stat":
                    parseStat(args);
                    break;
                case "-info":
                    parseInfo(args, charset);
                    System.exit(0);
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
                    Configuration._debugMode.set(true);
                    break;
                default:
                    break OUTER;
            }
            if (args.isEmpty()) {
                return null;
            }
        }

        String command = "";
        if (nextParam == null) {
            nextParam = "";
        }
        if (nextParam.startsWith("-")) {
            command = nextParam.substring(1);
        }

        if (command.equals("header")) {
            parseHeader(args);
            System.exit(0);
        } else if (command.equals("translator")) {
            Translator.main(new String[]{});
        } else if (command.equals("swf2exe")) {
            parseSwf2Exe(args, charset);
            System.exit(0);
        } else if (command.equals("abcclean")) {
            parseAbcClean(args, charset);
            System.exit(0);
        } else if (command.equals("abcmerge")) {
            parseAbcMerge(args, charset);
            System.exit(0);
        } else if (command.equals("swf2swc")) {
            parseSwf2Swc(args, charset);
            System.exit(0);
        } else if (command.equals("linkreport")) {
            parseLinkReport(selectionClasses, args, charset);
            System.exit(0);
        } else if (command.equals("getinstancemetadata")) {
            parseGetInstanceMetadata(args, charset);
            System.exit(0);
        } else if (command.equals("setinstancemetadata")) {
            parseSetInstanceMetadata(args, charset);
            System.exit(0);
        } else if (command.equals("removeinstancemetadata")) {
            parseRemoveInstanceMetadata(args, charset);
            System.exit(0);
        } else if (command.equals("removefromcontextmenu")) {
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
            parseExport(selectionClasses, selection, selectionIds, args, handler, traceLevel, format, zoom, charset, exportEmbed, resampleWav, transparentBackground);
            System.exit(0);
        } else if (command.equals("compress")) {
            parseCompress(args);
            System.exit(0);
        } else if (command.equals("decompress")) {
            parseDecompress(args);
            System.exit(0);
        } else if (command.equals("encrypt")) {
            parseDecrypt(args);
            System.exit(0);
        } else if (command.equals("decrypt")) {
            parseDecrypt(args);
            System.exit(0);
        } else if (command.equals("swf2xml")) {
            parseSwf2Xml(args, charset);
            System.exit(0);
        } else if (command.equals("xml2swf")) {
            parseXml2Swf(args, charset);
            System.exit(0);
        } else if (command.equals("extract")) {
            parseExtract(args);
            System.exit(0);
        } else if (command.equals("memorysearch")) {
            parseMemorySearch(args);
            System.exit(0);
        } else if (command.equals("deobfuscate")) {
            parseDeobfuscate(args, charset);
            System.exit(0);
        } else if (command.equals("renameinvalididentifiers")) {
            parseRenameInvalidIdentifiers(args);
            System.exit(0);
        } else if (command.equals("dumpswf")) {
            parseDumpSwf(args);
            System.exit(0);
        } else if (command.equals("dumpas2")) {
            parseDumpAS2(args, charset);
            System.exit(0);
        } else if (command.equals("dumpas3")) {
            parseDumpAS3(args, charset);
            System.exit(0);
        } else if (command.equals("enabledebugging")) {
            parseEnableDebugging(args, charset);
            System.exit(0);
        } else if (command.equals("flashpaper2pdf")) {
            parseFlashPaperToPdf(selection, zoom, args, charset);
            System.exit(0);
        } else if (command.equals("replace")) {
            parseReplace(args, charset, air);
            System.exit(0);
        } else if (command.equals("replacealpha")) {
            parseReplaceAlpha(args, charset);
            System.exit(0);
        } else if (command.equals("replacecharacter")) {
            parseReplaceCharacter(args, charset);
            System.exit(0);
        } else if (command.equals("replacecharacterid")) {
            parseReplaceCharacterId(args, charset);
            System.exit(0);
        } else if (command.equals("convert")) {
            parseConvert(args, charset);
            System.exit(0);
        } else if (command.equals("remove")) {
            parseRemove(args, charset);
            System.exit(0);
        } else if (command.equals("removecharacter")) {
            parseRemoveCharacter(args, false, charset);
            System.exit(0);
        } else if (command.equals("removecharacterwithdependencies")) {
            parseRemoveCharacter(args, true, charset);
            System.exit(0);
        } else if (command.equals("doc")) {
            parseDoc(args);
        } else if (command.equals("importsymbolclass")) {
            parseImportSymbolClass(args, charset);
            System.exit(0);
        } else if (command.equals("importmovies")) {
            parseImportMovies(args, charset);
            System.exit(0);
        } else if (command.equals("importsounds")) {
            parseImportSounds(args, charset);
            System.exit(0);
        } else if (command.equals("importshapes")) {
            parseImportShapes(args, charset);
            System.exit(0);
        } else if (command.equals("importimages")) {
            parseImportImages(args, charset);
            System.exit(0);
        } else if (command.equals("importsprites")) {
            parseImportSprites(args, charset);
            System.exit(0);
        } else if (command.equals("importtext")) {
            parseImportText(args, charset);
            System.exit(0);
        } else if (command.equals("importscript")) {
            parseImportScript(args, charset, air, handler);
            System.exit(0);
        } else if (command.equals("as3compiler")) {
            ActionScript3Parser.compile(null /*?*/, args.pop(), args.pop(), 0, 0);
        } else if (nextParam.equals("--debugtool")) {
            parseDebugTool(args, charset);
        } else if (nextParam.equals("--compareresources")) {
            parseCompareResources(args);
            System.exit(0);
        } else if (nextParam.equals("--resourcedates")) {
            parseResourceDates(args);
            System.exit(0);
        } else if (nextParam.equals("-listconfigs")) {
            printHeader();
            printConfigurationSettings();
            System.exit(0);
        } else if (nextParam.equals("-help") || nextParam.equals("--help") || nextParam.equals("/?") || nextParam.equals("\\_") /* /? translates as this on windows */) {
            parseHelp(args);
            System.exit(0);
        } else if (nextParam.equals("--webhelp")) { //for generating commandline usage on webpages
            ByteArrayOutputStream whbaos = new ByteArrayOutputStream();
            printCmdLineUsage(new PrintStream(whbaos, true), true, null);
            String wh = new String(whbaos.toByteArray());
            wh = wh.replace("<", "&lt;").replace(">", "&gt;");
            System.out.println(wh);
            System.exit(0);
        } else {
            args.push(nextParamOriginal); // file names should be the original one
            List<String> fileNames = new ArrayList<>();
            boolean allParamIsAFile = true;
            while (!args.isEmpty()) {
                String arg = args.pop();
                if (arg.equals("-custom")) {
                    parseCustom(args);
                    break;
                }

                fileNames.add(arg);
                File file = new File(arg);
                if (!file.exists() || !file.isFile()) {
                    allParamIsAFile = false;
                }
            }

            if (allParamIsAFile) {
                String[] fileNamesArray = fileNames.toArray(new String[fileNames.size()]);
                if (cliMode) {
                    loadFiles(fileNamesArray);
                    return null;
                } else {
                    return fileNamesArray;
                }
            } else {
                badArguments();
            }
        }

        return null;
    }

    public static void printHeader() {
        AnsiConsole.systemInstall();
        ansiPrintln(ApplicationInfo.cliApplicationVerName);
        AnsiConsole.systemUninstall();
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
        printCmdLineUsage(command, false);
        System.exit(1);
    }

    @SuppressWarnings("unchecked")
    private static void setConfigurations(String cfgStr) {
        String[] cfgs;
        if (cfgStr.contains(",")) {
            cfgs = cfgStr.split(",");
        } else {
            cfgs = new String[]{cfgStr};
        }

        Map<String, Field> fields = Configuration.getConfigurationFields(true);
        for (String c : cfgs) {
            String[] cp = c.split("=");
            if (cp.length == 1) {
                cp = new String[]{cp[0], "1"};
            }

            Field field = fields.get(cp[0].toLowerCase(Locale.ENGLISH));
            ConfigurationItem<?> item = ConfigurationItem.getItem(field);
            String stringValue = cp[1];
            Class<?> type = ConfigurationItem.getConfigurationFieldType(field);

            if (type == String.class) {
                System.out.println("Config " + item.getName() + " set to " + stringValue);
                ((ConfigurationItem<String>) item).set(stringValue);
            } else if (type == Calendar.class) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date dateValue;
                try {
                    dateValue = dateFormat.parse(stringValue);
                    GregorianCalendar calendarValue = new GregorianCalendar();
                    calendarValue.setTime(dateValue);
                    ((ConfigurationItem<Calendar>) item).set(calendarValue);
                } catch (ParseException ex) {
                    Logger.getLogger(CommandLineArgumentParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (type == Integer.class) {
                int intValue = Integer.parseInt(stringValue);
                ((ConfigurationItem<Integer>) item).set(intValue);
            } else if ((type == Integer.class) || (type == Long.class)) {
                long longValue = Long.parseLong(stringValue);
                ((ConfigurationItem<Long>) item).set(longValue);
            } else if (type == Double.class) {
                double doubleValue = Double.parseDouble(stringValue);
                ((ConfigurationItem<Double>) item).set(doubleValue);
            } else if (type == Float.class) {
                float floatValue = Float.parseFloat(stringValue);
                ((ConfigurationItem<Float>) item).set(floatValue);
            } else if (type == Boolean.class) {
                Boolean boolValue = parseBooleanConfigValue(stringValue);
                if (boolValue != null) {
                    System.out.println("Config " + item.getName() + " set to " + boolValue);
                    ((ConfigurationItem<Boolean>) item).set(boolValue);
                } else {
                    System.out.println("Invalid config value for " + item.getName() + ": " + stringValue);
                }
            } else if (type.isEnum()) {
                Enum enumValue = Enum.valueOf((Class) type, stringValue);
                ConfigurationItem uncheckedItem = (ConfigurationItem) item;
                uncheckedItem.set(enumValue);
            }
        }
    }

    private static Boolean parseBooleanConfigValue(String value) {
        if (value == null) {
            return null;
        }

        Boolean bValue = null;
        value = value.toLowerCase(Locale.ENGLISH);
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
            badArguments("config");
        }

        setConfigurations(args.pop());
    }

    private static void parseSwf2Exe(Stack<String> args, String charset) {
        if (args.size() != 3) {
            badArguments("swf2exe");
        }
        final String type = args.pop();
        final File outFile = new File(args.pop());
        final File swfFile = new File(args.pop());
        ExeExportMode exportMode = enumFromStr(type, ExeExportMode.class);
        processReadSWF(swfFile, null, (SWF swf, OutputStream stdout) -> {
            Main.saveFileToExe(swf, exportMode, outFile);
        }, charset);
    }

    private static void parseAbcMerge(Stack<String> args, String charset) {
        if (args.size() < 2) {
            badArguments("abcmerge");
        }
        final File outFile = new File(args.pop());
        final File swfFile = new File(args.pop());
        processModifySWF(swfFile, outFile, null, (SWF swf, OutputStream stdout) -> {
            List<ABCContainerTag> abcList = swf.getAbcList();
            if (!abcList.isEmpty() && abcList.size() > 1) {
                ABC firstAbc = abcList.get(0).getABC();
                for (int i = 1; i < abcList.size(); i++) {
                    firstAbc.mergeABC(abcList.get(i).getABC());
                }
                for (int i = 1; i < abcList.size(); i++) {
                    swf.removeTag((Tag) abcList.get(i));
                }
            }
        }, charset);

    }

    private static void parseAbcClean(Stack<String> args, String charset) {
        if (args.size() < 2) {
            badArguments("abcclean");
        }
        final File inFile = new File(args.pop());
        final File outFile = new File(args.pop());

        processModifyAbc(inFile, outFile, null, new AbcAction() {
            @Override
            public void abcAction(ABC abc, OutputStream stdout) throws IOException {
                ABCCleaner cleaner = new ABCCleaner();
                cleaner.clean(abc);
            }
        }, charset);
    }

    private static void parseSwf2Swc(Stack<String> args, String charset) {
        if (args.size() < 2) {
            badArguments("swf2swc");
        }
        final File outFile = new File(args.pop());
        final File swfFile = new File(args.pop());
        processReadSWF(swfFile, null, (SWF swf, OutputStream stdout) -> {
            SwfToSwcExporter exporter = new SwfToSwcExporter();
            try {
                exporter.exportSwf(swf, outFile, false);
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(CommandLineArgumentParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }, charset);
    }

    private static void parseLinkReport(List<String> selectionClasses, Stack<String> args, String charset) {
        if (args.isEmpty()) {
            badArguments("linkreport");
        }
        File stdOutFile = null;
        File swfFile = null;
        while (!args.isEmpty()) {
            String paramName = args.pop().toLowerCase(Locale.ENGLISH);
            switch (paramName) {
                case "-outfile":
                    if (args.empty()) {
                        System.err.println("Missing output file");
                        badArguments("linkreport");
                    }
                    stdOutFile = new File(args.pop());
                    break;
                default:
                    if (!args.isEmpty()) {
                        badArguments("linkreport");
                    }
                    swfFile = new File(paramName);
            }
        }
        if (swfFile == null) {
            System.err.println("No SWF file specified");
            badArguments("getinstancemetadata");
        }
        processReadSWF(swfFile, stdOutFile, (SWF swf, OutputStream stdout) -> {

            LinkReportExporter lre = new LinkReportExporter();

            List<ScriptPack> reportPacks;
            try {
                reportPacks = selectionClasses != null ? swf.getScriptPacksByClassNames(selectionClasses) : swf.getAS3Packs();
            } catch (Exception ex) {
                System.err.println("Error while getting packs");
                System.exit(1);
                return;
            }
            try {
                String reportStr = lre.generateReport(swf, reportPacks, null);
                stdout.write(reportStr.getBytes("UTF-8"));
            } catch (InterruptedException ex) {
                System.err.println("Report generation interrupted");
            }
        }, charset);
    }

    private static void parseGetInstanceMetadata(Stack<String> args, String charset) {
        if (args.size() < 3) {
            badArguments("getinstancemetadata");
        }
        Set<String> processedParams = new HashSet<>();
        String format = METADATA_FORMAT_JSLIKE;
        String key = null;
        String instance = null;
        File stdOutFile = null;
        File swfFile = null;

        while (!args.empty()) {
            String paramName = args.pop().toLowerCase(Locale.ENGLISH);
            if (processedParams.contains(paramName)) {
                System.err.println("Parameter " + paramName + " can appear only once.");
            }
            switch (paramName) {
                case "-instance":
                    if (args.isEmpty()) {
                        System.err.println("Missing instance name");
                        badArguments("getinstancemetadata");
                    }
                    instance = args.pop();
                    break;
                case "-outputformat":
                    if (args.empty()) {
                        System.err.println("Missing format value");
                        badArguments("getinstancemetadata");
                    }
                    format = args.pop();
                    if (!Arrays.asList(METADATA_FORMAT_RAW, METADATA_FORMAT_JSLIKE).contains(format)) {
                        System.err.println("Invalid output format");
                        badArguments("getinstancemetadata");
                    }
                    break;
                case "-key":
                    if (args.empty()) {
                        System.err.println("Missing key value");
                        badArguments("getinstancemetadata");
                    }

                    key = args.pop();
                    break;
                case "-datafile":
                    if (args.empty()) {
                        System.err.println("Missing datafile file");
                        badArguments("getinstancemetadata");
                    }
                    stdOutFile = new File(args.pop());
                    break;
                default:
                    if (!args.isEmpty()) {
                        badArguments("getinstancemetadata");
                    }
                    swfFile = new File(paramName);
                    paramName = null;
            }
            if (paramName != null) {
                processedParams.add(paramName);
            }
        }
        if (instance == null) {
            System.err.println("No instance specified");
            badArguments("getinstancemetadata");
        }
        if (swfFile == null) {
            System.err.println("No SWF file specified");
            badArguments("getinstancemetadata");
        }

        final String fInstance = instance;
        final String fKey = key;
        final String fFormat = format;

        processReadSWF(swfFile, stdOutFile, new SwfAction() {
            @Override
            public void swfAction(SWF swf, OutputStream stdout) throws IOException {
                if (!processTimelined(swf, stdout)) {
                    System.err.println("No instance with name " + fInstance + " found");
                    System.exit(0);
                }
            }

            private boolean processTimelined(Timelined tim, OutputStream stdout) throws IOException {
                ReadOnlyTagList rtl = tim.getTags();
                for (int i = 0; i < rtl.size(); i++) {
                    Tag t = rtl.get(i);
                    if (t instanceof Timelined) {
                        if (processTimelined((Timelined) t, stdout)) {
                            return true;
                        }
                    }
                    if (t instanceof PlaceObjectTypeTag) {
                        PlaceObjectTypeTag pt = (PlaceObjectTypeTag) t;
                        String instanceName = pt.getInstanceName();
                        if (fInstance.equals(instanceName)) {
                            Amf3Value oldValue = pt.getAmfData();
                            if (oldValue == null) {
                                System.err.println("No metadata for instance " + instanceName + " found");
                                System.exit(1); //TODO? Different exit code
                            }
                            Object actualValue = oldValue.getValue();

                            Object displayVal = actualValue;
                            if (fKey != null) {
                                if (actualValue instanceof ObjectType) {
                                    ObjectType ot = (ObjectType) actualValue;
                                    if (ot.containsDynamicMember(fKey)) {
                                        displayVal = ot.getDynamicMember(fKey);
                                    } else {
                                        System.err.println("No value with key " + fKey + " exists");
                                        System.err.println("Available keys: " + String.join(",", ot.dynamicMembersKeySet()));
                                        System.exit(1);
                                    }
                                } else {
                                    System.err.println("Metadata present, but not as Object type, cannot get key " + fKey);
                                    System.exit(1);
                                }
                            }

                            switch (fFormat) {
                                case METADATA_FORMAT_JSLIKE:
                                    stdout.write(Utf8Helper.getBytes(Amf3Exporter.amfToString(displayVal, "  ", System.lineSeparator()) + System.lineSeparator()));
                                    break;
                                case METADATA_FORMAT_RAW:
                                    Amf3OutputStream aos = new Amf3OutputStream(stdout);
                                    try {
                                        aos.writeValue(displayVal);
                                    } catch (NoSerializerExistsException ex) {
                                        //should not happen
                                    }
                                    break;
                            }
                            return true;
                        }
                    }
                }
                return false;
            }
        }, charset);
    }

    private static void parseSetInstanceMetadata(Stack<String> args, String charset) {
        if (args.size() < 3) {
            badArguments("setinstancemetadata");
        }
        Set<String> processedParams = new HashSet<>();
        String format = METADATA_FORMAT_JSLIKE;
        String key = null;
        String instance = null;
        File outFile = null;
        File swfFile = null;
        String value = null;
        File valueFile = null;

        while (!args.empty()) {
            String paramName = args.pop().toLowerCase(Locale.ENGLISH);
            if (processedParams.contains(paramName)) {
                System.err.println("Parameter " + paramName + " can appear only once.");
            }
            switch (paramName) {
                case "-instance":
                    if (args.isEmpty()) {
                        System.err.println("Missing instance name");
                        badArguments("setinstancemetadata");
                    }
                    instance = args.pop();
                    break;
                case "-inputformat":
                    if (args.empty()) {
                        System.err.println("Missing format value");
                        badArguments("setinstancemetadata");
                    }
                    format = args.pop();
                    if (!Arrays.asList(METADATA_FORMAT_RAW, METADATA_FORMAT_JSLIKE).contains(format)) {
                        System.err.println("Invalid output format");
                        badArguments("setinstancemetadata");
                    }
                    break;
                case "-key":
                    if (args.empty()) {
                        System.err.println("Missing key value");
                        badArguments("setinstancemetadata");
                    }

                    key = args.pop();
                    break;
                case "-value":
                    if (args.empty()) {
                        System.err.println("Missing value");
                        badArguments("setinstancemetadata");
                    }

                    value = args.pop();
                    break;
                case "-outfile":
                    if (args.empty()) {
                        System.err.println("Missing outFile");
                        badArguments("setinstancemetadata");
                    }
                    outFile = new File(args.pop());
                    break;

                case "-datafile":
                    if (args.empty()) {
                        System.err.println("Missing datafile file");
                        badArguments("setinstancemetadata");
                    }
                    valueFile = new File(args.pop());
                    break;
                default:
                    if (!args.isEmpty()) {
                        badArguments("setinstancemetadata");
                    }
                    swfFile = new File(paramName);
                    paramName = null;
            }
            if (paramName != null) {
                processedParams.add(paramName);
            }
        }
        if (instance == null) {
            System.err.println("No instance specified");
            badArguments("getinstancemetadata");
        }
        if (swfFile == null) {
            System.err.println("No SWF file specified");
            badArguments("getinstancemetadata");
        }
        if (outFile == null) {
            outFile = swfFile;
        }

        byte[] valueBytes = new byte[]{};
        if (valueFile != null) {
            try {
                valueBytes = Helper.readFileEx(valueFile.getAbsolutePath());
            } catch (IOException ex) {
                System.err.println("Cannot read value: " + ex.getMessage());
                System.exit(1);
                return;
            }
        } else if (value != null) {
            valueBytes = Utf8Helper.getBytes(value);
        }

        if (valueBytes.length == 0) {
            valueBytes = Helper.readStream(System.in);
        }

        if (valueBytes.length < 1) {
            System.err.println("No value to set specified");
            System.exit(1);
        }

        Object amfValue = null;
        try {
            switch (format) {
                case METADATA_FORMAT_JSLIKE:
                    Amf3Importer importer = new Amf3Importer();
                    amfValue = importer.stringToAmf(value);
                    break;
                case METADATA_FORMAT_RAW:
                    Amf3InputStream ais = new Amf3InputStream(new MemoryInputStream(valueBytes));
                    amfValue = ais.readValue("val");
                    break;
            }
        } catch (IOException | Amf3ParseException | NoSerializerExistsException ex) {
            System.err.println("Error parsing input value: " + ex.getMessage());
            System.exit(1);
            return;
        }

        final String fInstance = instance;
        final String fKey = key;
        final Object fAmfValue = amfValue;

        processModifySWF(swfFile, outFile, null, new SwfAction() {
            @Override
            public void swfAction(SWF swf, OutputStream stdout) throws IOException {
                if (!processTimelined(swf, stdout)) {
                    System.err.println("No instance with name " + fInstance + " found");
                    System.exit(0);
                }
            }

            private boolean processTimelined(Timelined tim, OutputStream stdout) throws IOException {
                ReadOnlyTagList rtl = tim.getTags();
                for (int i = 0; i < rtl.size(); i++) {
                    Tag t = rtl.get(i);
                    if (t instanceof Timelined) {
                        if (processTimelined((Timelined) t, stdout)) {
                            return true;
                        }
                    }
                    if (t instanceof PlaceObjectTypeTag) {
                        PlaceObjectTypeTag pt = (PlaceObjectTypeTag) t;
                        String instanceName = pt.getInstanceName();
                        if (fInstance.equals(instanceName)) {

                            Amf3Value oldValue = pt.getAmfData();
                            if (oldValue != null && oldValue.getValue() == null) {
                                oldValue = null;
                            }
                            if (oldValue != null && fKey != null) { //it has AMFData and we are going to set key
                                Object actualValue = oldValue.getValue();
                                if (actualValue instanceof ObjectType) {    //add it to ObjectType
                                    ObjectType ot = (ObjectType) actualValue;
                                    ot.putDynamicMember(fKey, fAmfValue);
                                    t.setModified(true);
                                    oldValue.setValue(ot);
                                    System.out.println("Key " + fKey + " added");
                                    System.out.println("New instance data for " + instanceName + ":");
                                    System.out.println(Amf3Exporter.amfToString(ot, "  ", System.lineSeparator()));
                                    return true;
                                }
                            }

                            PlaceObject4Tag pt4;
                            if (pt instanceof PlaceObject4Tag) {
                                pt4 = (PlaceObject4Tag) pt;
                            } else {
                                pt4 = new PlaceObject4Tag(
                                        pt.getSwf(), pt.flagMove(), pt.getDepth(), pt.getClassName(), pt.getCharacterId(), pt.getMatrix(), pt.getColorTransform() == null ? null : new CXFORMWITHALPHA(pt.getColorTransform()), pt.getRatio(),
                                        pt.getInstanceName(), pt.getClipDepth(), pt.getFilters(), pt.getBlendMode(), pt.getBitmapCache(), pt.getVisible(), pt.getBackgroundColor(), pt.getClipActions(), pt.getAmfData(), pt.hasImage());
                                tim.replaceTag(i, pt4);
                            }

                            Object newValue;
                            if (fKey != null) {
                                ObjectType ot = new ObjectType(new Traits("", true, new ArrayList<>()));
                                ot.put(fKey, fAmfValue);
                                newValue = ot;
                            } else {
                                newValue = fAmfValue;
                            }
                            pt4.amfData = new Amf3Value(newValue);
                            pt4.setModified(true);

                            System.out.println("New instance data for " + instanceName + ":");
                            System.out.println(Amf3Exporter.amfToString(newValue, "  ", System.lineSeparator()));

                            return true;
                        }
                    }
                }
                return false;
            }
        }, charset);
    }

    private static void parseRemoveInstanceMetadata(Stack<String> args, String charset) {
        if (args.size() < 2) {
            badArguments("removeinstancemetadata");
        }

        Set<String> processedParams = new HashSet<>();
        String key = null;
        String instance = null;
        File swfFile = null;
        File outFile = null;
        while (!args.empty()) {
            String paramName = args.pop().toLowerCase(Locale.ENGLISH);
            if (processedParams.contains(paramName)) {
                System.err.println("Parameter " + paramName + " can appear only once.");
            }
            switch (paramName) {
                case "-instance":
                    if (args.isEmpty()) {
                        System.err.println("Missing instance name");
                        badArguments("removeinstancemetadata");
                    }
                    instance = args.pop();
                    break;
                case "-key":
                    if (args.empty()) {
                        System.err.println("Missing key value");
                        badArguments("removeinstancemetadata");
                    }

                    key = args.pop();
                    break;
                case "-outfile":
                    if (args.empty()) {
                        System.err.println("Missing outFile");
                        badArguments("removeinstancemetadata");
                    }
                    outFile = new File(args.pop());
                    break;
                default:
                    if (!args.isEmpty()) {
                        badArguments("removeinstancemetadata");
                    }
                    swfFile = new File(paramName);
                    paramName = null;
            }
            if (paramName != null) {
                processedParams.add(paramName);
            }
        }
        if (instance == null) {
            System.err.println("No instance specified");
            badArguments("removeinstancemetadata");
        }
        if (swfFile == null) {
            System.err.println("No SWF file specified");
            badArguments("removeinstancemetadata");
        }
        if (outFile == null) {
            outFile = swfFile;
        }

        final String fInstance = instance;
        final String fKey = key;

        processModifySWF(swfFile, outFile, null, new SwfAction() {
            @Override
            public void swfAction(SWF swf, OutputStream stdout) throws IOException {
                if (!processTimelined(swf, stdout)) {
                    System.err.println("No instance with name " + fInstance + " found");
                    System.exit(0);
                }
            }

            private boolean processTimelined(Timelined tim, OutputStream stdout) throws IOException {
                ReadOnlyTagList rtl = tim.getTags();
                for (int i = 0; i < rtl.size(); i++) {
                    Tag t = rtl.get(i);
                    if (t instanceof Timelined) {
                        if (processTimelined((Timelined) t, stdout)) {
                            return true;
                        }
                    }
                    if (t instanceof PlaceObject4Tag) {
                        PlaceObject4Tag pt4 = (PlaceObject4Tag) t;
                        String instanceName = pt4.getInstanceName();
                        if (fInstance.equals(instanceName)) {
                            Amf3Value oldValue = pt4.getAmfData();
                            if (oldValue == null) {
                                System.err.println("No metadata for instance " + instanceName + " found");
                                System.exit(1); //TODO? Different exit code
                            }
                            Object actualValue = oldValue.getValue();

                            if (fKey != null) {
                                if (actualValue instanceof ObjectType) {
                                    ObjectType ot = (ObjectType) actualValue;
                                    if (ot.containsDynamicMember(fKey)) {
                                        ot.remove(fKey);
                                        oldValue.setValue(ot);
                                        System.out.println("Key " + fKey + " removed");
                                        System.out.println("New instance data for " + instanceName + ":");
                                        System.out.println(Amf3Exporter.amfToString(ot, "  ", System.lineSeparator()));
                                        pt4.setModified(true);
                                        return true;
                                    } else {
                                        System.err.println("No value with key " + fKey + " exists");
                                        System.err.println("Available keys: " + String.join(",", ot.dynamicMembersKeySet()));
                                        System.exit(1);
                                    }
                                } else {
                                    System.err.println("Metadata present, but not as Object type, cannot remove key " + fKey);
                                    System.exit(1);
                                }
                            } else {
                                pt4.amfData = null;
                                pt4.setModified(true);
                                System.out.println("Whole metadata removed for instance " + instanceName);
                            }

                            return true;
                        }
                    }
                }
                return false;
            }
        }, charset);
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
            badArguments("select");
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
                String[] ps = r.split("\\-");
                if (ps.length != 2) {
                    System.err.println("invalid range");
                    badArguments("select");
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
                    badArguments("select");
                }
            } else {
                try {
                    min = Integer.parseInt(r);
                    max = min;
                } catch (NumberFormatException nfe) {
                    System.err.println("invalid range");
                    badArguments("select");
                }
            }
            ret.add(new Range(min, max));
        }
        return new Selection(ret);
    }

    private static double parseZoom(Stack<String> args) {
        if (args.isEmpty()) {
            System.err.println("zoom parameter expected");
            badArguments("zoom");
        }
        try {
            return Double.parseDouble(args.pop());
        } catch (NumberFormatException nfe) {
            System.err.println("invalid zoom");
            badArguments("zoom");
        }
        return 1;
    }

    private static AbortRetryIgnoreHandler parseOnError(Stack<String> args) {
        int errorMode = AbortRetryIgnoreHandler.UNDEFINED;
        int retryCount = 0;

        if (args.isEmpty()) {
            System.err.println("onerror parameter expected");
            badArguments("onerror");
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
                    badArguments("onerror");
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
            badArguments("timeout");
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
            badArguments("exporttimeout");
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
            badArguments("exportfiletimeout");
        }
        try {
            int timeout = Integer.parseInt(args.pop());
            Configuration.decompilationTimeoutFile.set(timeout);
        } catch (NumberFormatException nex) {
            System.err.println("Bad timeout value");
        }
    }

    private static void parseStat(Stack<String> args) {
        showStat = true;
        Configuration.showStat = showStat;
    }

    private static void parseStdOut(Stack<String> args) {
        if (args.isEmpty()) {
            System.err.println("stdOut parameter expected");
            badArguments("stdout");
        }

        stdOut = args.pop();
    }

    private static void parseStdErr(Stack<String> args) {
        if (args.isEmpty()) {
            System.err.println("stdErr parameter expected");
            badArguments("stderr");
        }

        stdErr = args.pop();
    }

    private static void parseAffinity(Stack<String> args) {
        if (Platform.isWindows()) {
            if (args.isEmpty()) {
                System.err.println("affinity parameter expected");
                badArguments("affinity");
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
                badArguments("priority");
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

    private static void parseDebugTool(Stack<String> args, String charset) {
        String cmd = args.pop().toLowerCase(Locale.ENGLISH);
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
                Main.initLogging(Configuration._debugMode.get());

                File[] files = new File(folder).listFiles(getSwfFilter());
                for (File file : files) {
                    OpenableSourceInfo sourceInfo = new OpenableSourceInfo(null, file.getAbsolutePath(), file.getName());
                    try {
                        SWF swf = new SWF(new FileInputStream(file), sourceInfo.getFile(), sourceInfo.getFileTitle(), Configuration.parallelSpeedUp.get(), charset);
                        swf.openableList = new OpenableList();
                        swf.openableList.sourceInfo = sourceInfo;
                        boolean found = false;
                        for (Tag tag : swf.getTags()) {
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
                Main.initLogging(Configuration._debugMode.get());
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
                Main.initLogging(Configuration._debugMode.get());

                File[] files = new File(folder).listFiles(getSwfFilter());
                for (File file : files) {
                    OpenableSourceInfo sourceInfo = new OpenableSourceInfo(null, file.getAbsolutePath(), file.getName());
                    try {
                        SWF swf = new SWF(new FileInputStream(file), sourceInfo.getFile(), sourceInfo.getFileTitle(), Configuration.parallelSpeedUp.get(), charset);
                        swf.openableList = new OpenableList();
                        swf.openableList.sourceInfo = sourceInfo;
                        boolean found = false;
                        for (Tag tag : swf.getTags()) {
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
                Main.initLogging(Configuration._debugMode.get());
                break;
            }
        }
    }

    private static void parseCompareResources(Stack<String> args) {
        String revision = args.pop().toLowerCase(Locale.ENGLISH);
        String revision2 = null;
        if (!args.isEmpty()) {
            revision2 = args.pop();
        }

        CheckResources.compareResources(System.out, revision, revision2);
    }

    private static void parseResourceDates(Stack<String> args) {
        CheckResources.checkTranslationDate(System.out);
    }

    private static void parseHelp(Stack<String> args) {
        String filter = null;
        if (!args.isEmpty()) {
            filter = args.pop();
            if (filter.startsWith("-")) {
                filter = filter.substring(1);
            }
        }
        printHeader();
        printCmdLineUsage(filter, false);
    }

    private static void parseProxy(Stack<String> args) {
        System.err.println("Proxy functionality was REMOVED");
        System.exit(1);
    }

    private static List<String> parseSelectClass(Stack<String> args) {
        if (args.size() < 1) {
            badArguments("selectclass");
        }
        List<String> ret = new ArrayList<>();
        String classesStr = args.pop();
        String[] classes;
        if (classesStr.contains(",")) {
            classes = classesStr.split(",");
        } else {
            classes = new String[]{classesStr};
        }
        ret.addAll(Arrays.asList(classes));
        return ret;

    }

    private static void parseExport(List<String> selectionClasses, Selection selection, Selection selectionIds, Stack<String> args, AbortRetryIgnoreHandler handler, Level traceLevel, Map<String, String> formats, double zoom, String charset, boolean exportEmbed, boolean resampleWav, boolean transparentBackground) {
        if (args.size() < 3) {
            badArguments("export");
        }
        String[] validExportItems = new String[]{
            "script",
            "script_as2",
            "script_as3",
            "image",
            "shape",
            "morphshape",
            "movie",
            "font",
            "font4",
            "frame",
            "sprite",
            "button",
            "sound",
            "binarydata",
            "symbolclass",
            "text",
            "all",
            "fla",
            "xfl"
        };

        String[] removedExportFormats = new String[]{
            "as", "pcode", "hex", "pcodehex", "all_as", "all_pcode", "all_pcodehex", "all_hex", "textplain"
        };

        if (handler == null) {
            handler = new ConsoleAbortRetryIgnoreHandler(AbortRetryIgnoreHandler.UNDEFINED, 0);
        }

        String exportFormatString = args.pop().toLowerCase(Locale.ENGLISH);
        List<String> exportFormats = Arrays.asList(exportFormatString.split(","));
        long startTime = System.currentTimeMillis();

        File outDirBase = new File(args.pop());
        String inFileOrFolderStr = args.pop();
        File inFileOrFolder = new File(inFileOrFolderStr);
        if (!StdInAwareFileInputStream.STDIN_PATH.equals(inFileOrFolderStr) && !inFileOrFolder.exists()) {
            System.err.println("Input SWF file does not exist!");
            badArguments("export");
        }

        if (!args.isEmpty() && args.peek().equals("-selectas3class")) {
            System.err.println("Error: -selectas3class parameter was REMOVED. Please use -selectclass instead. See --help for usage.");
            System.exit(1);
        }

        printHeader();
        boolean exportOK = true;

        List<String> as3classes = new ArrayList<>();
        if (selectionClasses != null) {
            as3classes.addAll(selectionClasses);
        }

        Map<String, StatisticData> stat = new HashMap<>();

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
                    String outFilePath = stdOut.replace("{swfFile}", inFileName);
                    Path.createDirectorySafe(new File(outFilePath).getParentFile());
                    System.setOut(new PrintStream(new FileOutputStream(outFilePath, true)));
                }

                if (stdErr != null) {
                    String errFilePath = stdErr.replace("{swfFile}", inFileName);
                    Path.createDirectorySafe(new File(errFilePath).getParentFile());
                    System.setErr(new PrintStream(new FileOutputStream(errFilePath, true)));
                    Main.initLogging(Configuration._debugMode.get());
                }

                long startTimeSwf = 0;
                if (!singleFile) {
                    startTimeSwf = System.currentTimeMillis();
                    System.out.println("Start exporting " + inFile.getName());
                }

                OpenableSourceInfo sourceInfo = new OpenableSourceInfo(null, inFile.getAbsolutePath(), inFile.getName());
                SWF swf;
                try {
                    swf = new SWF(new BufferedInputStream(new StdInAwareFileInputStream(inFile)), sourceInfo.getFile(), sourceInfo.getFileTitle(), null, Configuration.parallelSpeedUp.get(), false, true, charset);
                } catch (FileNotFoundException | SwfOpenException ex) {
                    // FileNotFoundException when anti virus software blocks to open the file
                    logger.log(Level.SEVERE, "Failed to open swf: " + inFile.getName(), ex);
                    continue;
                }

                swf.openableList = new OpenableList();
                swf.openableList.sourceInfo = sourceInfo;
                String outDir = outDirBase.getAbsolutePath();
                if (!singleFile) {
                    outDir = Path.combine(outDir, inFile.getName());
                }

                List<Tag> extags = new ArrayList<>();
                for (Tag t : swf.getTags()) {
                    if (t instanceof CharacterIdTag) {
                        CharacterIdTag c = (CharacterIdTag) t;
                        if (selectionIds.contains(c.getCharacterId())) {
                            extags.add(t);
                        }
                    } else if (selectionIds.contains(0)) {
                        extags.add(t);
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
                        badArguments("export");
                    }
                }

                // Here the exportFormats array should contain only validitems
                commandLineMode = true;
                boolean exportAll = exportFormats.contains("all");
                boolean multipleExportTypes = exportAll || exportFormats.size() > 1;
                EventListener evl = swf.getExportEventListener();

                if (exportAll || exportFormats.contains("image")) {
                    System.out.println("Exporting images...");
                    new ImageExporter().exportImages(handler, outDir + (multipleExportTypes ? File.separator + ImageExportSettings.EXPORT_FOLDER_NAME : ""), new ReadOnlyTagList(extags), new ImageExportSettings(enumFromStr(formats.get("image"), ImageExportMode.class)), evl);
                }

                if (exportAll || exportFormats.contains("shape")) {
                    System.out.println("Exporting shapes...");
                    new ShapeExporter().exportShapes(handler, outDir + (multipleExportTypes ? File.separator + ShapeExportSettings.EXPORT_FOLDER_NAME : ""), swf, new ReadOnlyTagList(extags), new ShapeExportSettings(enumFromStr(formats.get("shape"), ShapeExportMode.class), zoom), evl, zoom);
                }

                if (exportAll || exportFormats.contains("morphshape")) {
                    System.out.println("Exporting morphshapes...");
                    new MorphShapeExporter().exportMorphShapes(handler, outDir + (multipleExportTypes ? File.separator + MorphShapeExportSettings.EXPORT_FOLDER_NAME : ""), new ReadOnlyTagList(extags), new MorphShapeExportSettings(enumFromStr(formats.get("morphshape"), MorphShapeExportMode.class), zoom), evl);
                }

                if (exportAll || exportFormats.contains("movie")) {
                    System.out.println("Exporting movies...");
                    new MovieExporter().exportMovies(handler, outDir + (multipleExportTypes ? File.separator + MovieExportSettings.EXPORT_FOLDER_NAME : ""), new ReadOnlyTagList(extags), new MovieExportSettings(enumFromStr(formats.get("movie"), MovieExportMode.class)), evl);
                }

                if (exportAll || exportFormats.contains("font")) {
                    System.out.println("Exporting fonts...");
                    new FontExporter().exportFonts(handler, outDir + (multipleExportTypes ? File.separator + FontExportSettings.EXPORT_FOLDER_NAME : ""), new ReadOnlyTagList(extags), new FontExportSettings(enumFromStr(formats.get("font"), FontExportMode.class)), evl);
                }

                if (exportAll || exportFormats.contains("font4")) {
                    System.out.println("Exporting DefineFont4s...");
                    new Font4Exporter().exportFonts(handler, outDir + (multipleExportTypes ? File.separator + FontExportSettings.EXPORT_FOLDER_NAME : ""), new ReadOnlyTagList(extags), new Font4ExportSettings(enumFromStr(formats.get("font4"), Font4ExportMode.class)), evl);
                }

                if (exportAll || exportFormats.contains("sound")) {
                    System.out.println("Exporting sounds...");
                    new SoundExporter().exportSounds(handler, outDir + (multipleExportTypes ? File.separator + SoundExportSettings.EXPORT_FOLDER_NAME : ""), new ReadOnlyTagList(extags), new SoundExportSettings(enumFromStr(formats.get("sound"), SoundExportMode.class), resampleWav), evl);
                }

                if (exportAll || exportFormats.contains("binarydata")) {
                    System.out.println("Exporting binaryData...");
                    new BinaryDataExporter().exportBinaryData(handler, outDir + (multipleExportTypes ? File.separator + BinaryDataExportSettings.EXPORT_FOLDER_NAME : ""), new ReadOnlyTagList(extags), new BinaryDataExportSettings(enumFromStr(formats.get("binarydata"), BinaryDataExportMode.class)), evl);
                }

                if (exportAll || exportFormats.contains("symbolclass")) {
                    System.out.println("Exporting symbolClass...");
                    new SymbolClassExporter().exportNames(handler, outDir + (multipleExportTypes ? File.separator + SymbolClassExportSettings.EXPORT_FOLDER_NAME : ""), new ReadOnlyTagList(extags), new SymbolClassExportSettings(enumFromStr(formats.get("symbolclass"), SymbolClassExportMode.class)), evl);
                }

                if (exportAll || exportFormats.contains("text")) {
                    System.out.println("Exporting texts...");
                    Boolean singleTextFile = parseBooleanConfigValue(formats.get("singletext"));
                    if (singleTextFile == null) {
                        singleTextFile = Configuration.textExportSingleFile.get();
                    }
                    new TextExporter().exportTexts(handler, outDir + (multipleExportTypes ? File.separator + TextExportSettings.EXPORT_FOLDER_NAME : ""), new ReadOnlyTagList(extags), new TextExportSettings(enumFromStr(formats.get("text"), TextExportMode.class), singleTextFile, zoom), evl);
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
                    FrameExportSettings fes = new FrameExportSettings(enumFromStr(formats.get("frame"), FrameExportMode.class), zoom, transparentBackground);
                    frameExporter.exportFrames(handler, outDir + (multipleExportTypes ? File.separator + FrameExportSettings.EXPORT_FOLDER_NAME : ""), swf, 0, frames, fes, evl);
                }

                if (exportAll || exportFormats.contains("sprite")) {
                    System.out.println("Exporting sprite...");
                    SpriteExportSettings ses = new SpriteExportSettings(enumFromStr(formats.get("sprite"), SpriteExportMode.class), zoom);
                    for (Tag t : extags) {
                        if (t instanceof DefineSpriteTag) {
                            frameExporter.exportSpriteFrames(handler, outDir + (multipleExportTypes ? File.separator + SpriteExportSettings.EXPORT_FOLDER_NAME : ""), swf, ((DefineSpriteTag) t).getCharacterId(), null, ses, evl);
                        }
                    }
                }

                if (exportAll || exportFormats.contains("button")) {
                    System.out.println("Exporting buttons...");
                    ButtonExportSettings bes = new ButtonExportSettings(enumFromStr(formats.get("button"), ButtonExportMode.class), zoom);
                    for (Tag t : extags) {
                        if (t instanceof ButtonTag) {
                            frameExporter.exportButtonFrames(handler, outDir + (multipleExportTypes ? File.separator + ButtonExportSettings.EXPORT_FOLDER_NAME : ""), swf, ((ButtonTag) t).getCharacterId(), null, bes, evl);
                        }
                    }
                }

                boolean parallel = Configuration.parallelSpeedUp.get();
                Boolean singleScriptFile = parseBooleanConfigValue(formats.get("singlescript"));
                if (singleScriptFile == null) {
                    singleScriptFile = Configuration.scriptExportSingleFile.get();
                }

                if (parallel && singleScriptFile) {
                    logger.log(Level.WARNING, AppStrings.translate("export.script.singleFileParallelModeWarning"));
                    singleScriptFile = false;
                }

                ScriptExportSettings scriptExportSettings = new ScriptExportSettings(enumFromStr(formats.get("script"), ScriptExportMode.class), singleScriptFile, false, exportEmbed, false, resampleWav);
                boolean exportAllScript = exportAll || exportFormats.contains("script");
                boolean exportAs2Script = exportAllScript || exportFormats.contains("script_as2");
                boolean exportAs3Script = exportAllScript || exportFormats.contains("script_as3");
                if (exportAs2Script || exportAs3Script) {
                    System.out.println("Exporting scripts...");
                    
                    String scriptsFolder = Path.combine(outDir, ScriptExportSettings.EXPORT_FOLDER_NAME);
                    Path.createDirectorySafe(new File(scriptsFolder));
                    String singleFileName = Path.combine(scriptsFolder, swf.getShortFileName() + scriptExportSettings.getFileExtension());
                    try (FileTextWriter writer = scriptExportSettings.singleFile ? new FileTextWriter(Configuration.getCodeFormatting(), new FileOutputStream(singleFileName)) : null) {
                        scriptExportSettings.singleFileWriter = writer;
                        List<ScriptPack> as3packs = as3classes.isEmpty() ? null : swf.getScriptPacksByClassNames(as3classes);
                        exportOK = swf.exportActionScript(handler, scriptsFolder, as3classes.isEmpty() ? null : as3packs, scriptExportSettings, parallel, evl, exportAs2Script, exportAs3Script) != null && exportOK;
                    }

                    if (showStat) {
                        Statistics.print();
                        Statistics.addToMap(stat);
                        Statistics.clear();
                    }
                }

                if (exportFormats.contains("fla")) {
                    System.out.println("Exporting FLA...");
                    exportFla(true, outDir, inFile, swf, multipleExportTypes, formats, handler);
                }

                if (exportFormats.contains("xfl")) {
                    System.out.println("Exporting XFL...");
                    exportFla(false, outDir, inFile, swf, multipleExportTypes, formats, handler);
                }

                if (!singleFile) {
                    long stopTimeSwf = System.currentTimeMillis();
                    long time = stopTimeSwf - startTimeSwf;
                    System.out.println("Export finished: " + inFile.getName() + " Export time: " + Helper.formatTimeSec(time));
                }

                swf.clearAllCache();
                CancellableWorker.cancelBackgroundThreads();
            }
        } catch (OutOfMemoryError | Exception ex) {
            System.err.print("FAIL: Exporting Failed on Exception - ");
            logger.log(Level.SEVERE, null, ex);
            System.exit(1);
        }

        if (showStat) {
            Statistics.print(stat);
        }

        long stopTime = System.currentTimeMillis();
        long time = stopTime - startTime;
        System.out.println("Export finished. Total export time: " + Helper.formatTimeSec(time));
        System.out.println(exportOK ? "OK" : "FAIL");
        System.exit(exportOK ? 0 : 1);
    }

    private static void exportFla(boolean compressed, String outDir, File inFile, SWF swf, boolean multipleExportTypes, Map<String, String> formats, AbortRetryIgnoreHandler handler) throws IOException, InterruptedException {
        String exportFormat = compressed ? "fla" : "xfl";
        String format = formats.get(exportFormat);
        boolean exportScript = true;
        if (format != null && format.endsWith("_noscript")) {
            format = format.substring(0, format.length() - 9);
            exportScript = false;
        }

        FLAVersion flaVersion = FLAVersion.fromString(format);
        if (flaVersion == null) {
            flaVersion = FLAVersion.CS6; //Defaults to CS6
        }

        String outFile = outDir;
        if (multipleExportTypes) {
            outFile = Path.combine(outFile, exportFormat);
        }

        String outFileName = inFile.getName().toLowerCase(Locale.ENGLISH).endsWith(".swf") ? inFile.getName().substring(0, inFile.getName().length() - 3) + exportFormat : inFile.getName();
        outFile = Path.combine(outFile, outFileName);
        XFLExportSettings settings = new XFLExportSettings();
        settings.compressed = compressed;
        settings.exportScript = exportScript;

        try {
            if (Configuration.setFFDecVersionInExportedFont.get()) {
                swf.exportXfl(handler, outFile, inFile.getName(), ApplicationInfo.APPLICATION_NAME, ApplicationInfo.applicationVerName, ApplicationInfo.version, Configuration.parallelSpeedUp.get(), flaVersion, settings, null);
            } else {
                swf.exportXfl(handler, outFile, inFile.getName(), ApplicationInfo.APPLICATION_NAME, ApplicationInfo.APPLICATION_NAME, "1.0.0", Configuration.parallelSpeedUp.get(), flaVersion, settings, null);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error during XFL/FLA export", ex);
        }
    }

    private static void parseDeobfuscate(Stack<String> args, String charset) {
        if (args.size() < 3) {
            badArguments("deobfuscate");
        }
        String mode = args.pop();
        DeobfuscationLevel lev;
        switch (mode) {
            case "controlflow":
            case "3":
                System.err.println("WARNING: Control flow level(3) is not implemented - it is the same as remove traps (2) level.");
                lev = DeobfuscationLevel.LEVEL_REMOVE_TRAPS;
                break;
            case "traps":
            case "max":
            case "2":
                lev = DeobfuscationLevel.LEVEL_REMOVE_TRAPS;
                break;
            case "deadcode":
            case "1":
                lev = DeobfuscationLevel.LEVEL_REMOVE_DEAD_CODE;
                break;
            default:
                System.err.println("Invalid level, must be one of: controlflow,traps,deadcode or 1,2,3/max");
                System.exit(1);
                return;
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
        try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(inFile); FileOutputStream fos = new FileOutputStream(outFile)) {
            SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);
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
                System.out.println(inFile + " overwritten.");
            }
            System.out.println("OK");
        } catch (FileNotFoundException ex) {
            System.err.println("File not found.");
            System.exit(1);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
            System.exit(1);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error", ex);
            System.exit(1);
        } finally {
            if (tmpFile != null && tmpFile.exists()) {
                tmpFile.delete();
            }
        }
    }

    private static void parseCompress(Stack<String> args) {
        if (args.size() < 2) {
            badArguments("compress");
        }

        boolean result = false;
        try {
            SWFCompression compression = SWFCompression.ZLIB;
            String compressionString = !args.isEmpty() ? args.pop() : null;
            if (compressionString != null) {
                switch (compressionString.toLowerCase(Locale.ENGLISH)) {
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

            try (InputStream fis = new BufferedInputStream(new StdInAwareFileInputStream(args.pop())); OutputStream fos = new BufferedOutputStream(new FileOutputStream(args.pop()))) {
                result = SWF.compress(fis, fos, compression);
                System.out.println(result ? "OK" : "FAIL");
            } catch (FileNotFoundException ex) {
                System.err.println("File not found.");
                System.exit(1);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        System.exit(result ? 0 : 1);
    }

    private static void parseEncrypt(Stack<String> args) {
        if (args.size() < 2) {
            badArguments("encrypt");
        }

        boolean result = false;
        try {
            try (InputStream fis = new BufferedInputStream(new StdInAwareFileInputStream(args.pop())); OutputStream fos = new BufferedOutputStream(new FileOutputStream(args.pop()))) {
                result = SWF.encrypt(fis, fos);
                System.out.println(result ? "OK" : "FAIL");
            } catch (FileNotFoundException ex) {
                System.err.println("File not found.");
                System.exit(1);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        System.exit(result ? 0 : 1);
    }

    private static void parseDecrypt(Stack<String> args) {
        if (args.size() < 2) {
            badArguments("decrypt");
        }

        boolean result = false;
        try {
            try (InputStream fis = new BufferedInputStream(new StdInAwareFileInputStream(args.pop())); OutputStream fos = new BufferedOutputStream(new FileOutputStream(args.pop()))) {
                result = SWF.decrypt(fis, fos);
                System.out.println(result ? "OK" : "FAIL");
            } catch (FileNotFoundException ex) {
                System.err.println("File not found.");
                System.exit(1);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        System.exit(result ? 0 : 1);
    }

    private static void parseDecompress(Stack<String> args) {
        if (args.size() < 2) {
            badArguments("decompress");
        }

        boolean result = false;
        try {
            try (InputStream fis = new BufferedInputStream(new StdInAwareFileInputStream(args.pop())); OutputStream fos = new BufferedOutputStream(new FileOutputStream(args.pop()))) {
                result = SWF.decompress(fis, fos);
                System.out.println(result ? "OK" : "FAIL");
            } catch (FileNotFoundException ex) {
                System.err.println("File not found.");
                System.exit(1);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        System.exit(result ? 0 : 1);
    }

    private static void parseSwf2Xml(Stack<String> args, String charset) {
        if (args.size() < 2) {
            badArguments("swf2xml");
        }

        try {
            try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(args.pop())) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);
                new SwfXmlExporter().exportXml(swf, new File(args.pop()));
            } catch (FileNotFoundException ex) {
                System.err.println("File not found.");
                System.exit(1);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private static void parseXml2Swf(Stack<String> args, String charset) {
        if (args.size() < 2) {
            badArguments("xml2swf");
        }

        try {
            SWF swf = new SWF(charset);
            try (StdInAwareFileInputStream in = new StdInAwareFileInputStream(args.pop())) {
                new SwfXmlImporter().importSwf(swf, in);
            }
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(new File(args.pop())))) {
                swf.saveTo(fos);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

    }

    private static void parseExtract(Stack<String> args) {
        if (args.size() < 1) {
            badArguments("extract");
        }

        String fileName = args.pop();
        SearchMode mode = SearchMode.ALL;

        boolean noCheck = false;
        String output = null;

        if (args.size() > 0 && args.peek().toLowerCase(Locale.ENGLISH).equals("-o")) {
            args.pop();
            if (args.size() < 1) {
                badArguments("extract");
            }
            output = args.pop();
        }

        if (args.size() > 0 && args.peek().toLowerCase(Locale.ENGLISH).equals("nocheck")) {
            noCheck = true;
            args.pop();
        }

        if (args.size() > 0) {
            String modeStr = args.pop().toLowerCase(Locale.ENGLISH);
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
            OpenableSourceInfo sourceInfo = new OpenableSourceInfo(null, fileName, null);
            if (!sourceInfo.isBundle()) {
                System.err.println("Error: <infile> should be a bundle. (ZIP or non SWF binary file)");
                System.exit(1);
            }
            Bundle bundle = sourceInfo.getBundle(noCheck, mode);
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

    }

    private static void parseMemorySearch(Stack<String> args) {
        if (args.size() < 1) {
            badArguments("memorysearch");
        }

        if (Platform.isWindows()) {
            AtomicInteger cnt = new AtomicInteger();
            List<com.jpexs.process.Process> procs = new ArrayList<>();
            List<Process> processList = ProcessTools.listProcesses();
            while (args.size() > 0) {
                String arg = args.pop();
                if (arg.matches("\\d+")) {
                    int processId = 0;
                    try {
                        processId = Integer.parseInt(arg);
                    } catch (NumberFormatException nfe) {
                        System.err.println("ProcessId should be integer");
                        badArguments("memorysearch");
                    }

                    boolean found = false;
                    if (processList != null) {
                        for (Process process : processList) {
                            if (process.getPid() == processId) {
                                if (!procs.contains(process)) {
                                    procs.add(process);
                                }

                                found = true;
                                break; // only 1 process can have this process id
                            }
                        }
                    }

                    if (!found) {
                        System.out.println("Process id=" + processId + " was not found.");
                    }
                } else {
                    boolean found = false;
                    if (processList != null) {
                        for (Process process : processList) {
                            if (process.getFileName().equals(arg)) {
                                if (!procs.contains(process)) {
                                    procs.add(process);
                                }

                                found = true;
                            }
                        }
                    }

                    if (!found) {
                        System.out.println("Process name=" + arg + " was not found.");
                    }
                }
            }

            try {
                new SearchInMemory(new SearchInMemoryListener() {
                    @Override
                    public void publish(Object... chunks) {
                        for (Object s : chunks) {
                            if (s instanceof SwfInMemory) {
                                SwfInMemory swf = (SwfInMemory) s;
                                String fileName = cnt.getAndIncrement() + ".swf";
                                System.out.println("SWF found (" + fileName + "). Version: " + swf.version + ", file size: " + swf.fileSize + ", address: " + swf.address);
                                Helper.writeFile(fileName, swf.is);
                            }
                        }
                    }

                    @Override
                    public void setProgress(int progress) {
                        // ignore
                    }
                }).search(procs);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        } else {
            System.err.println("Memory search is only available on Windows platform.");
        }

    }

    private static void parseRenameInvalidIdentifiers(Stack<String> args) {
        if (args.size() < 3) {
            badArguments("renameinvalididentifiers");
        }

        String renameTypeStr = args.pop();
        RenameType renameType;
        switch (renameTypeStr.toLowerCase(Locale.ENGLISH)) {
            case "typenumber":
                renameType = RenameType.TYPENUMBER;
                break;
            case "randomword":
                renameType = RenameType.RANDOMWORD;
                break;
            default:
                System.err.println("Invalid rename type:" + renameTypeStr);
                badArguments("renameinvalididentifiers");
                return;
        }

        boolean result = false;
        try {
            try (InputStream fis = new BufferedInputStream(new StdInAwareFileInputStream(args.pop())); OutputStream fos = new BufferedOutputStream(new FileOutputStream(args.pop()))) {
                result = SWF.renameInvalidIdentifiers(renameType, fis, fos);
                System.out.println(result ? "OK" : "FAIL");
            } catch (FileNotFoundException ex) {
                System.err.println("File not found.");
                System.exit(1);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        System.exit(result ? 0 : 1);
    }

    private static String parseCharset(Stack<String> args) {
        if (args.size() < 1) {
            badArguments("charset");
        }
        String charsetName = args.pop();
        boolean charsetValid = false;
        try {
            if (Charset.isSupported(charsetName)) {
                charsetValid = true;
            }
        } catch (Exception ex) {
            charsetValid = false;
        }

        if (!charsetValid) {
            System.err.println("Specified charset is not valid");
            badArguments("charset");
        }
        return charsetName;
    }

    private static Map<String, String> parseFormat(Stack<String> args) {
        if (args.size() < 1) {
            badArguments("format");
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
            if (!fmt.contains(":")) {
                badArguments("format");
            }
            String[] parts = fmt.split(":");
            ret.put(parts[0].toLowerCase(Locale.ENGLISH), parts[1].toLowerCase(Locale.ENGLISH));
        }
        return ret;
    }

    private static void parseFlashPaperToPdf(Selection selection, double zoom, Stack<String> args, String charset) {
        if (args.size() < 2) {
            badArguments("flashpaper2pdf");
        }
        File inFile = new File(args.pop());
        File outFile = new File(args.pop());
        printHeader();

        try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(inFile)) {

            PDFJob job = null;

            SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);
            int totalPages = 0;

            for (Tag t : swf.getTags()) {
                if (t instanceof DefineSpriteTag) {
                    DefineSpriteTag ds = (DefineSpriteTag) t;
                    String exportName = ds.getExportName();
                    if (exportName != null && exportName.matches("^page[0-9]+$")) {
                        int pageNum = Integer.parseInt(exportName.substring(4));
                        if (pageNum > totalPages) {
                            totalPages = pageNum;
                        }
                    }
                }
            }

            PageFormat pf = new PageFormat();
            pf.setOrientation(PageFormat.PORTRAIT);
            Paper p = new Paper();
            int page = 0;

            for (Tag t : swf.getTags()) {
                if (t instanceof DefineSpriteTag) {
                    DefineSpriteTag ds = (DefineSpriteTag) t;
                    if ("page1".equals(ds.getExportName())) {
                        page = 1;
                        job = new PDFJob(new BufferedOutputStream(new FileOutputStream(outFile)));
                    } else if (page > 0) {
                        page++;
                    }
                    if (("page" + page).equals(ds.getExportName())) {
                        if (!selection.contains(page)) {
                            continue;
                        }
                        System.out.print("Page " + page + "/" + totalPages + "...");
                        Timeline tim = ds.getTimeline();
                        RECT rect = tim.displayRect;

                        double w = (rect.getWidth() * zoom / SWF.unitDivisor);
                        double h = (rect.getHeight() * zoom / SWF.unitDivisor);
                        p.setSize(w, h);
                        pf.setPaper(p);
                        Matrix m = new Matrix();
                        m.translate(-rect.Xmin * zoom, -rect.Ymin * zoom);
                        m.scale(zoom);
                        Matrix transformation = m;
                        Map<Integer, Font> existingFonts = new HashMap<>();

                        int fframe = 0;
                        final Graphics2D g = (Graphics2D) job.getGraphics(pf);

                        SerializableImage image = new SerializableImage((int) w + 1, (int) h + 1, SerializableImage.TYPE_INT_ARGB_PRE) {

                            private Graphics2D compositeGraphics;

                            @Override
                            public Graphics getGraphics() {
                                if (compositeGraphics != null) {
                                    return compositeGraphics;
                                }
                                final Graphics2D parentGraphics = (Graphics2D) super.getGraphics();
                                compositeGraphics = new DualPdfGraphics2D(parentGraphics, (PDFGraphics) g, existingFonts);
                                return compositeGraphics;
                            }

                            @Override
                            public void fillTransparent() {

                            }

                        };
                        int imgWidth = (int) (rect.getWidth() * zoom / SWF.unitDivisor) + 1;
                        int imgHeight = (int) (rect.getHeight() * zoom / SWF.unitDivisor) + 1;

                        RenderContext renderContext = new RenderContext();
                        renderContext.cursorPosition = new Point(-1, -1);
                        renderContext.mouseButton = 0;
                        renderContext.stateUnderCursor = new ArrayList<>();

                        try {
                            tim.toImage(fframe, fframe, renderContext, image, image, false, m, new Matrix(), m, null, zoom, true, new ExportRectangle(rect), m, true, Timeline.DRAW_MODE_ALL, 0, true);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        g.dispose();

                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }

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
    }

    private static void parseReplace(Stack<String> args, String charset, boolean air) {
        if (args.size() < 3) {
            badArguments("replace");
        }

        File inFile = new File(args.pop());
        File outFile = new File(args.pop());

        if (args.size() == 1) {
            System.out.println("Replacing - only single argument passed, taking it as file to load replacements from");
            try {
                List<String> lines = Files.readAllLines(Paths.get(args.pop()), StandardCharsets.UTF_8);
                Collections.reverse(lines);

                args.clear();
                args.addAll(lines);

            } catch (IOException e) {
                System.err.println("I/O Error during reading replacements file");
                System.exit(1);
            }

            if (args.isEmpty()) {
                System.err.println("Replacements file is empty.");
                System.exit(1);
            }
        }

        try {
            try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(inFile)) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);
                while (true) {
                    String objectToReplace = args.pop();

                    if (objectToReplace.matches("\\d+")) {
                        // replace character tag
                        int characterId = 0;
                        try {
                            characterId = Integer.parseInt(objectToReplace);
                        } catch (NumberFormatException nfe) {
                            System.err.println("CharacterId should be integer");
                            badArguments("replace");
                        }

                        SoundStreamHeadTypeTag soundStreamHead = null;
                        CharacterTag characterTag = null;
                        if (characterId == -1) {
                            //replacing soundstreamhead on main timeline
                        } else if (swf.getCharacters(false).containsKey(characterId)) {
                            characterTag = swf.getCharacter(characterId);
                        } else {
                            System.err.println("CharacterId does not exist");
                            System.exit(1);
                        }

                        String repFile = args.pop();

                        byte[] data = Helper.readFile(repFile);
                        String ext = Path.getExtension(repFile);

                        int soundFormat = SoundFormat.FORMAT_UNCOMPRESSED_LITTLE_ENDIAN;
                        if (".mp3".equals(ext)) {
                            soundFormat = SoundFormat.FORMAT_MP3;
                        }

                        if ((characterTag == null || (characterTag instanceof DefineSpriteTag)) && (".wav".equals(ext) || ".mp3".equals(ext))) {
                            ReadOnlyTagList tags = (characterTag == null) ? swf.getTags() : ((DefineSpriteTag) characterTag).getTags();
                            for (Tag t : tags) {
                                if (t instanceof SoundStreamHeadTypeTag) {
                                    soundStreamHead = (SoundStreamHeadTypeTag) t;
                                    break;
                                }
                            }
                            if (soundStreamHead == null) {
                                System.err.println((characterTag == null ? "Main timeline " : "DefineSprite(" + characterId + ")") + " does not contain any SoundStreamHead");
                                System.exit(1);
                            }
                            boolean ok = false;
                            try {
                                ok = new SoundImporter().importSoundStream(soundStreamHead, new ByteArrayInputStream(data), soundFormat);
                            } catch (UnsupportedSamplingRateException usre) {
                                List<String> supportedRatesStr = new ArrayList<>();
                                for (int i : usre.getSupportedRates()) {
                                    supportedRatesStr.add("" + i);
                                }
                                System.err.println("Import FAILED. Input file has unsupported sampling rate (" + usre.getSoundRate() + "). Supported rates for this sound format: " + String.join(", ", supportedRatesStr) + ".");
                                System.exit(2);
                            }
                            if (!ok) {
                                System.err.println("Import FAILED. Maybe unsupported media type? Only MP3 and uncompressed WAV are available.");
                                System.exit(1);
                            }
                        } else if (characterTag instanceof DefineBinaryDataTag) {
                            DefineBinaryDataTag defineBinaryData = (DefineBinaryDataTag) characterTag;
                            new BinaryDataImporter().importData(defineBinaryData, data);
                        } else if (characterTag instanceof ImageTag) {
                            int format = parseImageFormat(args);
                            ImageTag imageTag = (ImageTag) characterTag;
                            new ImageImporter().importImage(imageTag, data, format);
                        } else if (characterTag instanceof ShapeTag) {
                            boolean fill = true;
                            if (!args.isEmpty() && args.peek().equals("nofill")) {
                                args.pop();
                                fill = false;
                            }
                            ShapeTag shapeTag = (ShapeTag) characterTag;

                            if (".svg".equals(ext)) {
                                String svgText = Helper.readTextFile(repFile);
                                new SvgImporter().importSvg(shapeTag, svgText);
                            } else {
                                int format = parseImageFormat(args);
                                new ShapeImporter().importImage(shapeTag, data, format, fill);
                            }
                        } else if (characterTag instanceof TextTag) {
                            TextTag textTag = (TextTag) characterTag;
                            new TextImporter(new MissingCharacterHandler(), new TextImportErrorHandler() {
                                @Override
                                public boolean handle(TextTag textTag) {
                                    String msg = "Error during text import.";
                                    logger.log(Level.SEVERE, msg);
                                    return false;
                                }

                                @Override
                                public boolean handle(TextTag textTag, String message, long line) {
                                    String msg = "Error during text import: %text% on line %line%".replace("%text%", message).replace("%line%", Long.toString(line));
                                    logger.log(Level.SEVERE, msg);
                                    return false;
                                }
                            }).importText(textTag, new String(data, Utf8Helper.charset));
                        } else if (characterTag instanceof SoundTag) {
                            SoundTag st = (SoundTag) characterTag;
                            boolean ok = false;
                            SoundImporter soundImporter = new SoundImporter();
                            try {
                                ok = soundImporter.importSound(st, new ByteArrayInputStream(data), soundFormat);
                            } catch (UnsupportedSamplingRateException usre) {
                                List<String> supportedRatesStr = new ArrayList<>();
                                for (int i : usre.getSupportedRates()) {
                                    supportedRatesStr.add("" + i);
                                }
                                System.err.println("Import FAILED. Input file has unsupported sampling rate (" + usre.getSoundRate() + "). Supported rates for this sound format: " + String.join(", ", supportedRatesStr) + ".");
                                System.exit(2);
                            } catch (SoundImportException sie) {
                                ok = false;
                            }
                            if (!ok) {
                                System.err.println("Import FAILED. Maybe unsupported media type? Only MP3 and uncompressed WAV are available.");
                                System.exit(1);
                            }
                        } else if (characterTag instanceof DefineVideoStreamTag) {
                            DefineVideoStreamTag movie = (DefineVideoStreamTag) characterTag;
                            try {
                                new MovieImporter().importMovie(movie, data);
                            } catch (IOException iex) {
                                System.err.println("Import FAILED: " + iex.getMessage());
                                System.exit(1);
                            }
                        } else if (characterTag instanceof DefineSpriteTag) {
                            DefineSpriteTag sprite = (DefineSpriteTag) characterTag;
                            try {
                                new SpriteImporter().importSprite(sprite, new ByteArrayInputStream(data));
                            } catch (IOException iex) {
                                System.err.println("Import FAILED: " + iex.getMessage());
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
                                        replaceAS3(repFile, repText, pack, air);
                                    } else {
                                        // todo: get traits
                                        if (args.isEmpty()) {
                                            badArguments("replace");
                                        }

                                        int bodyIndex = Integer.parseInt(args.pop());
                                        ABC abc = pack.abc;
                                        List<Trait> resultTraits = abc.getMethodIndexing().findMethodTraits(pack, bodyIndex);

                                        //int classIndex = 0;
                                        //int traitId = 0;
                                        Trait trait = null; //abc.findTraitByTraitId(classIndex, traitId);
                                        if (resultTraits.size() == 1) {
                                            trait = resultTraits.get(0);
                                        }

                                        replaceAS3PCode(repText, abc, bodyIndex, trait);
                                    }
                                }
                            }
                        }

                        if (!found) {
                            System.err.println(objectToReplace + " is not recognized as a CharacterId or a script name.");
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

    private static int parseImageFormat(Stack<String> args) {
        if (args.isEmpty()) {
            return 0;
        }

        int res = ImageImporter.getImageTagType(args.peek().toLowerCase(Locale.ENGLISH));
        if (res != 0) {
            args.pop();
        }

        return res;
    }

    private static void parseReplaceAlpha(Stack<String> args, String charset) {
        if (args.size() < 4) {
            badArguments("replacealpha");
        }

        File inFile = new File(args.pop());
        File outFile = new File(args.pop());
        try {
            try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(inFile)) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);
                while (true) {
                    String objectToReplace = args.pop();

                    int imageId = 0;
                    try {
                        imageId = Integer.parseInt(objectToReplace);
                    } catch (NumberFormatException nfe) {
                        System.err.println("ImageId should be integer");
                        badArguments("replacealpha");
                    }
                    if (!swf.getCharacters(false).containsKey(imageId)) {
                        System.err.println("ImageId does not exist");
                        System.exit(1);
                    }

                    CharacterTag characterTag = swf.getCharacter(imageId);
                    String repFile = args.pop();
                    byte[] data = Helper.readFile(repFile);
                    if (characterTag instanceof HasSeparateAlphaChannel) {
                        ImageTag imageTag = (ImageTag) characterTag;
                        new ImageImporter().importImageAlpha(imageTag, data);
                    } else {
                        System.err.println("The specified tag type is not supported for alpha channel import");
                        badArguments("replacealpha");
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

    private static void parseReplaceCharacter(Stack<String> args, String charset) {
        if (args.size() < 4) {
            badArguments("replacecharacter");
        }

        File inFile = new File(args.pop());
        File outFile = new File(args.pop());
        try {
            try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(inFile)) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);
                while (true) {
                    String objectToReplace = args.pop();

                    int characterId = 0;
                    try {
                        characterId = Integer.parseInt(objectToReplace);
                    } catch (NumberFormatException nfe) {
                        System.err.println("CharacterId should be integer");
                        badArguments("replacecharacter");
                    }
                    if (!swf.getCharacters(false).containsKey(characterId)) {
                        System.err.println("CharacterId does not exist");
                        System.exit(1);
                    }

                    CharacterTag characterTag = swf.getCharacter(characterId);
                    String newCharacterIdStr = args.pop();

                    int newCharacterId = 0;
                    try {
                        newCharacterId = Integer.parseInt(newCharacterIdStr);
                    } catch (NumberFormatException nfe) {
                        System.err.println("NewCharacterId should be integer");
                        badArguments("replacecharacter");
                    }
                    if (!swf.getCharacters(false).containsKey(newCharacterId)) {
                        System.err.println("NewCharacterId does not exist");
                        System.exit(1);
                    }

                    swf.replaceCharacterTags(characterTag, newCharacterId);

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

    private static void parseReplaceCharacterId(Stack<String> args, String charset) {
        if (args.size() < 3) {
            badArguments("replacecharacterid");
        }

        File inFile = new File(args.pop());
        File outFile = new File(args.pop());
        try {
            try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(inFile)) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);
                String arg = args.pop().toLowerCase(Locale.ENGLISH);
                if (arg.equals("pack")) {
                    swf.packCharacterIds();
                } else if (arg.equals("sort")) {
                    swf.sortCharacterIds();
                } else {
                    String[] characterIdsStr = arg.split(",");
                    if (characterIdsStr.length % 2 != 0) {
                        System.err.println("CharacterId count should be an even number");
                        badArguments("replacecharacterid");
                    }

                    List<Integer> characterIds = new ArrayList<>();
                    for (int i = 0; i < characterIdsStr.length; i++) {
                        int characterId;
                        try {
                            characterId = Integer.parseInt(characterIdsStr[i]);
                            characterIds.add(characterId);
                        } catch (NumberFormatException nfe) {
                            System.err.println("CharacterId should be integer");
                            badArguments("replacecharacterid");
                        }
                    }

                    for (int i = 0; i < characterIds.size(); i += 2) {
                        int oldCharacterId = characterIds.get(i);
                        int newCharacterId = characterIds.get(i + 1);
                        swf.replaceCharacter(oldCharacterId, newCharacterId);
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

    private static void parseConvert(Stack<String> args, String charset) {
        if (args.size() < 4) {
            badArguments("convert");
        }

        File inFile = new File(args.pop());
        File outFile = new File(args.pop());
        try {
            try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(inFile)) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);

                String objectToConvert = args.pop();

                int characterId = 0;
                try {
                    characterId = Integer.parseInt(objectToConvert);
                } catch (NumberFormatException nfe) {
                    System.err.println("CharacterId should be integer");
                    badArguments("convert");
                }
                if (!swf.getCharacters(false).containsKey(characterId)) {
                    System.err.println("CharacterId does not exist");
                    System.exit(1);
                }

                CharacterTag characterTag = swf.getCharacter(characterId);
                String targetType = args.pop().toLowerCase(Locale.ENGLISH);
                if (characterTag instanceof ImageTag) {
                    int format = ImageImporter.getImageTagType(targetType);
                    ImageTag imageTag = (ImageTag) characterTag;
                    new ImageImporter().convertImage(imageTag, format);
                } else if (characterTag instanceof ShapeTag) {
                    int format = ShapeImporter.getShapeTagType(targetType);
                    ShapeTag shapeTag = (ShapeTag) characterTag;
                    System.err.println("Converting shape tag is currently not supported");
                } else if (characterTag instanceof MorphShapeTag) {
                    int format = MorphShapeImporter.getMorphShapeTagType(targetType);
                    MorphShapeTag morphShapeTag = (MorphShapeTag) characterTag;
                    System.err.println("Converting morph shape tag is currently not supported");
                } else if (characterTag instanceof FontTag) {
                    int format = FontImporter.getFontTagType(targetType);
                    FontTag fontTag = (FontTag) characterTag;
                    System.err.println("Converting font tag is currently not supported");
                } else if (characterTag instanceof TextTag) {
                    int format = TextImporter.getTextTagType(targetType);
                    TextTag textTag = (TextTag) characterTag;
                    System.err.println("Converting text tag is currently not supported");
                } else {
                    System.err.println("The specified tag type is not supported for import");
                    System.exit(1);
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

    private static void parseRemove(Stack<String> args, String charset) {
        if (args.size() < 3) {
            badArguments("remove");
        }

        File inFile = new File(args.pop());
        File outFile = new File(args.pop());
        try {
            try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(inFile)) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);
                List<Integer> tagNumbersToRemove = new ArrayList<>();
                while (true) {
                    String tagNoToRemoveStr = args.pop();

                    int tagNo;
                    try {
                        tagNo = Integer.parseInt(tagNoToRemoveStr);
                    } catch (NumberFormatException nfe) {
                        System.err.println("Tag number should be integer");
                        System.exit(1);
                        return;
                    }
                    if (tagNo < 0 || tagNo >= swf.getTags().size()) {
                        System.err.println("Tag number does not exist. Tag number should be between 0 and " + (swf.getTags().size() - 1));
                        System.exit(1);
                    }

                    if (!tagNumbersToRemove.contains(tagNo)) {
                        tagNumbersToRemove.add(tagNo);
                    }

                    if (args.isEmpty() || args.peek().startsWith("-")) {
                        break;
                    }
                }

                Collections.sort(tagNumbersToRemove);
                for (int i = tagNumbersToRemove.size() - 1; i >= 0; i--) {
                    swf.removeTag((int) tagNumbersToRemove.get(i));
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

    private static void parseDoc(Stack<String> args) {
        String type = null;
        String format = null;
        String out = null;
        String locale = null;
        boolean nightMode = false;
        while (!args.isEmpty()) {
            String arg = args.pop();
            switch (arg) {
                case "-night":
                    nightMode = true;
                    break;
                case "-out":
                    if (args.isEmpty() || out != null) {
                        badArguments("doc");
                    }
                    out = args.pop();
                    break;
                case "-type":
                    if (args.isEmpty() || type != null) {
                        badArguments("doc");
                    }
                    type = args.pop();
                    break;
                case "-format":
                    if (args.isEmpty() || format != null) {
                        badArguments("doc");
                    }
                    format = args.pop();
                    break;
                case "-locale":
                    if (args.isEmpty() || locale != null) {
                        badArguments("doc");
                    }
                    locale = args.pop();

                    break;
            }
        }

        if (format == null) {
            format = "html";
        }
        
        if (!format.equals("html")) {
            badArguments("doc");
        }
        if (locale != null) {
            Locale.setDefault(Locale.forLanguageTag(locale));
        }

        String doc = "";
        if (type == null) {
            badArguments("doc");
        } else if (type.equals("as3.pcode.instructions")) {
            doc = As3PCodeDocs.getAllInstructionDocs(nightMode);
        } else if (type.equals("as12.pcode.instructions")) {
            doc = As12PCodeDocs.getAllInstructionDocs(nightMode);
        } else {
            badArguments("doc");
        }
        PrintStream outStream;

        if (out == null) {
            outStream = System.out;
        } else {
            try {
                outStream = new PrintStream(out, "UTF-8");
            } catch (UnsupportedEncodingException | FileNotFoundException ex) {
                Logger.getLogger(CommandLineArgumentParser.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
                System.exit(1);
                return;
            }
        }

        outStream.print(doc);
    }

    private static void parseRemoveCharacter(Stack<String> args, boolean removeDependencies, String charset) {
        if (args.size() < 3) {
            badArguments("removecharacter");
        }

        File inFile = new File(args.pop());
        File outFile = new File(args.pop());
        try {
            try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(inFile)) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);
                while (true) {
                    String objectToRemove = args.pop();

                    int characterId = 0;
                    try {
                        characterId = Integer.parseInt(objectToRemove);
                    } catch (NumberFormatException nfe) {
                        System.err.println("CharacterId should be integer");
                        badArguments("removecharacter");
                    }
                    if (!swf.getCharacters(false).containsKey(characterId)) {
                        System.err.println("CharacterId does not exist");
                        System.exit(1);
                    }

                    CharacterTag characterTag = swf.getCharacter(characterId);
                    swf.removeTag(characterTag, removeDependencies, null);

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

    private static void parseImportSymbolClass(Stack<String> args, String charset) {
        if (args.size() < 3) {
            badArguments("importsymbolclass");
        }

        File inFile = new File(args.pop());
        File outFile = new File(args.pop());

        try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(inFile)) {
            SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);

            String selFile = args.pop();
            File importFile = new File(Path.combine(selFile, SymbolClassExporter.SYMBOL_CLASS_EXPORT_FILENAME));
            SymbolClassImporter importer = new SymbolClassImporter();

            if (!importFile.exists()) {
                System.err.println("Symbol class file " + importFile + " does not exist.");
                System.exit(1);
            }
            importer.importSymbolClasses(importFile, swf);
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(outFile))) {
                swf.saveTo(fos);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("I/O error during writing");
            System.exit(2);
        }

    }

    private static void parseImportShapes(Stack<String> args, String charset) {
        if (args.size() < 3) {
            badArguments("importshapes");
        }

        File inFile = new File(args.pop());
        File outFile = new File(args.pop());
        boolean noFill = false;
        if (args.size() > 1) {
            if (args.pop().equals("nofill")) {
                noFill = true;
            } else {
                badArguments("importshapes");
            }
        }
        try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(inFile)) {
            SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);
            System.out.println("Source file opened");
            String selFile = args.pop();

            File shapesDir = new File(Path.combine(selFile, ShapeExportSettings.EXPORT_FOLDER_NAME));
            if (shapesDir.exists()) {
                System.out.println("Using the directory: " + shapesDir.getAbsolutePath());
            } else {
                shapesDir = new File(selFile);
            }
            if (!shapesDir.exists()) {
                System.err.println("Shapes directory does not exist: " + shapesDir.getAbsolutePath());
                System.exit(1);
            }
            ShapeImporter shapeImporter = new ShapeImporter();
            int shapeCount = shapeImporter.bulkImport(shapesDir, swf, noFill, true);

            System.out.println("Writing outfile");
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(outFile))) {
                swf.saveTo(fos);
            }
            System.out.println("" + shapeCount + " shapes successfully imported");
        } catch (IOException | InterruptedException e) {
            System.err.println("I/O error during writing");
            System.exit(2);
        }
    }

    private static void parseImportMovies(Stack<String> args, String charset) {
        if (args.size() < 3) {
            badArguments("importmovies");
        }

        File inFile = new File(args.pop());
        File outFile = new File(args.pop());

        try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(inFile)) {
            SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);
            System.out.println("Source file opened");
            String selFile = args.pop();

            File moviesDir = new File(Path.combine(selFile, MovieExportSettings.EXPORT_FOLDER_NAME));
            if (moviesDir.exists()) {
                System.out.println("Using the directory: " + moviesDir.getAbsolutePath());
            } else {
                moviesDir = new File(selFile);
            }
            if (!moviesDir.exists()) {
                System.err.println("Movies directory does not exist: " + moviesDir.getAbsolutePath());
                System.exit(1);
            }
            MovieImporter movieImporter = new MovieImporter();
            int movieCount = movieImporter.bulkImport(moviesDir, swf, true);
            System.out.println("Writing outfile");
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(outFile))) {
                swf.saveTo(fos);
            }
            System.out.println("" + movieCount + " movies successfully imported");
        } catch (IOException | InterruptedException e) {
            System.err.println("I/O error during writing");
            System.exit(2);
        }
    }

    private static void parseImportSounds(Stack<String> args, String charset) {
        if (args.size() < 3) {
            badArguments("importsounds");
        }

        File inFile = new File(args.pop());
        File outFile = new File(args.pop());

        try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(inFile)) {
            SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);
            System.out.println("Source file opened");
            String selFile = args.pop();

            File soundsDir = new File(Path.combine(selFile, SoundExportSettings.EXPORT_FOLDER_NAME));
            if (soundsDir.exists()) {
                System.out.println("Using the directory: " + soundsDir.getAbsolutePath());
            } else {
                soundsDir = new File(selFile);
            }
            if (!soundsDir.exists()) {
                System.err.println("Sounds directory does not exist: " + soundsDir.getAbsolutePath());
                System.exit(1);
            }
            SoundImporter soundImporter = new SoundImporter();
            int soundCount = soundImporter.bulkImport(soundsDir, swf, true);
            System.out.println("Writing outfile");
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(outFile))) {
                swf.saveTo(fos);
            }
            System.out.println("" + soundCount + " sounds successfully imported");
        } catch (IOException | InterruptedException e) {
            System.err.println("I/O error during writing");
            System.exit(2);
        }
    }

    private static void parseImportImages(Stack<String> args, String charset) {
        if (args.size() < 3) {
            badArguments("importimages");
        }

        File inFile = new File(args.pop());
        File outFile = new File(args.pop());

        try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(inFile)) {
            SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);
            System.out.println("Source file opened");
            String selFile = args.pop();

            File imagesDir = new File(Path.combine(selFile, ImageExportSettings.EXPORT_FOLDER_NAME));
            if (imagesDir.exists()) {
                System.out.println("Using the directory: " + imagesDir.getAbsolutePath());
            } else {
                imagesDir = new File(selFile);
            }
            if (!imagesDir.exists()) {
                System.err.println("Images directory does not exist: " + imagesDir.getAbsolutePath());
                System.exit(1);
            }
            ImageImporter imageImporter = new ImageImporter();
            int imageCount = imageImporter.bulkImport(imagesDir, swf, true);
            System.out.println("Writing outfile");
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(outFile))) {
                swf.saveTo(fos);
            }
            System.out.println("" + imageCount + " images successfully imported");
        } catch (IOException | InterruptedException e) {
            System.err.println("I/O error during writing");
            System.exit(2);
        }
    }

    private static void parseImportSprites(Stack<String> args, String charset) {
        if (args.size() < 3) {
            badArguments("importsprites");
        }

        File inFile = new File(args.pop());
        File outFile = new File(args.pop());

        try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(inFile)) {
            SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);
            System.out.println("Source file opened");
            String selFile = args.pop();

            File spritesDir = new File(Path.combine(selFile, SpriteExportSettings.EXPORT_FOLDER_NAME));
            if (spritesDir.exists()) {
                System.out.println("Using the directory: " + spritesDir.getAbsolutePath());
            } else {
                spritesDir = new File(selFile);
            }
            if (!spritesDir.exists()) {
                System.err.println("Sprites directory does not exist: " + spritesDir.getAbsolutePath());
                System.exit(1);
            }
            SpriteImporter spriteImporter = new SpriteImporter();
            int spriteCount = spriteImporter.bulkImport(spritesDir, swf, true);
            System.out.println("Writing outfile");
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(outFile))) {
                swf.saveTo(fos);
            }
            System.out.println("" + spriteCount + " sprites successfully imported");
        } catch (IOException | InterruptedException e) {
            System.err.println("I/O error during writing");
            System.exit(2);
        }
    }

    private static void parseImportText(Stack<String> args, String charset) {
        if (args.size() < 3) {
            badArguments("importtext");
        }

        File inFile = new File(args.pop());
        File outFile = new File(args.pop());

        try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(inFile)) {
            SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);

            String selFile = args.pop();
            boolean textsFolderExists = new File(Path.combine(selFile, TextExportSettings.EXPORT_FOLDER_NAME)).exists();
            File textsFile = new File(Path.combine(selFile, TextExportSettings.EXPORT_FOLDER_NAME, TextExporter.TEXT_EXPORT_FILENAME_FORMATTED));
            if (textsFolderExists) {
                System.out.println("Using the directory: " + new File(Path.combine(selFile, TextExportSettings.EXPORT_FOLDER_NAME)).getAbsolutePath());
            } else {
                textsFile = new File(Path.combine(selFile, TextExporter.TEXT_EXPORT_FILENAME_FORMATTED));
            }
            TextImporter textImporter = new TextImporter(new MissingCharacterHandler() {
                @Override
                public boolean getIgnoreMissingCharacters() {
                    return true;
                }

                @Override
                public boolean handle(TextTag textTag, final FontTag font, final char character) {
                    String fontName = font.getSwf().sourceFontNamesMap.get(font.getCharacterId());
                    if (fontName == null) {
                        fontName = font.getFontName();
                    }
                    final Font f = FontTag.getInstalledFontsByName().get(fontName);
                    if (f == null || !f.canDisplay(character)) {
                        String msg = AppStrings.translate("error.font.nocharacter").replace("%char%", "" + character);
                        logger.log(Level.SEVERE, "{0} FontId: {1} TextId: {2}", new Object[]{msg, font.getCharacterId(), textTag.getCharacterId()});
                        return false;
                    }

                    return font.addCharacter(character, f);
                }
            }, new TextImportErrorHandler() {
                private String getTextTagInfo(TextTag textTag) {
                    StringBuilder ret = new StringBuilder();
                    if (textTag != null) {
                        ret.append(" TextId: ").append(textTag.getCharacterId()).append(" (").append(String.join(", ", textTag.getTexts())).append(")");
                    }

                    return ret.toString();
                }

                @Override
                public boolean handle(TextTag textTag) {
                    String msg = AppStrings.translate("error.text.import");
                    logger.log(Level.SEVERE, "{0}{1}", new Object[]{msg, getTextTagInfo(textTag)});
                    return true;
                }

                @Override
                public boolean handle(TextTag textTag, String message, long line) {
                    String msg = AppStrings.translate("error.text.invalid.continue").replace("%text%", message).replace("%line%", Long.toString(line));
                    logger.log(Level.SEVERE, "{0}{1}", new Object[]{msg, getTextTagInfo(textTag)});
                    return true;
                }
            });

            // try to import formatted texts
            if (textsFile.exists()) {
                textImporter.importTextsSingleFileFormatted(textsFile, swf);
            } else {
                textsFile = new File(Path.combine(selFile, TextExportSettings.EXPORT_FOLDER_NAME, TextExporter.TEXT_EXPORT_FILENAME_PLAIN));
                if (!textsFolderExists) {
                    textsFile = new File(Path.combine(selFile, TextExporter.TEXT_EXPORT_FILENAME_PLAIN));
                }
                // try to import plain texts
                if (textsFile.exists()) {
                    textImporter.importTextsSingleFile(textsFile, swf);
                } else {
                    textImporter.importTextsMultipleFiles(selFile, swf);
                }
            }
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(outFile))) {
                swf.saveTo(fos);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("I/O error during writing");
            System.exit(2);
        }
    }

    private static void parseImportScript(Stack<String> args, String charset, boolean air, AbortRetryIgnoreHandler errorHandler) {

        String flexLocation = Configuration.flexSdkLocation.get();
        if (Configuration.useFlexAs3Compiler.get() && (flexLocation.isEmpty() || (!new File(flexLocation).exists()))) {
            System.err.println("Flex AS3 compiler enabled but Flex SDK path not set");
            System.exit(1);
        }

        if (args.size() < 3) {
            badArguments("importscript");
        }

        File inFile = new File(args.pop());
        File outFile = new File(args.pop());
        try {
            try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(inFile)) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);
                String baseFolder = args.pop();
                String scriptsFolder = Path.combine(baseFolder, ScriptExportSettings.EXPORT_FOLDER_NAME);
                if (new File(scriptsFolder).exists()) {
                    System.out.println("Using the directory: " + new File(scriptsFolder).getAbsolutePath());
                } else {
                    scriptsFolder = baseFolder;
                }
                ScriptImporterProgressListener listener = new ScriptImporterProgressListener() {
                    @Override
                    public void scriptImported() {
                    }

                    @Override
                    public void scriptImportError() {
                        if (errorHandler == null || ((ConsoleAbortRetryIgnoreHandler) errorHandler).errorMode == AbortRetryIgnoreHandler.ABORT) {
                            System.exit(1);
                        }
                    }
                };
                new AS2ScriptImporter().importScripts(scriptsFolder, swf.getASMs(true), listener);
                new AS3ScriptImporter().importScripts(As3ScriptReplacerFactory.createByConfig(air), scriptsFolder, swf.getAS3Packs(), listener, Main.getDependencies(swf));

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

    private static void parseCustom(Stack<String> args) {
        String[] customParameters = new String[args.size()];
        for (int i = 0; i < customParameters.length; i++) {
            customParameters[i] = args.pop();
        }

        SWFDecompilerPlugin.customParameters = customParameters;
    }

    private static void loadFiles(String[] fileNames) {
        boolean result = true;
        for (String fileName : fileNames) {
            try {
                OpenableSourceInfo sourceInfo = new OpenableSourceInfo(null, fileName, null);
                Main.parseOpenable(sourceInfo);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
                result = false;
            }
        }

        System.exit(result ? 0 : 1);
    }

    private static void replaceAS2PCode(String text, ASMSource src) throws IOException, InterruptedException {
        System.out.println("Replace AS1/2 PCode");
        if (text.trim().startsWith(Helper.hexData)) {
            src.setActionBytes(Helper.getBytesFromHexaText(text));
        } else {
            try {
                src.setActions(ASMParser.parse(0, true, text, src.getSwf().version, false, src.getSwf().getCharset()));
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
        ActionScript2Parser par = new ActionScript2Parser(src.getSwf(), src);
        try {
            src.setActions(par.actionsFromString(as, src.getSwf().getCharset()));
        } catch (ValueTooLargeException ex) {
            System.err.println("Script or some of its functions are too large");
            System.exit(1);
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
                AVM2Code acode = ASM3Parser.parse(abc, new StringReader(text), trait, new MissingSymbolHandler() {
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

                    @Override
                    public boolean missingDecimal(Decimal128 value) {
                        return true;
                    }

                    @Override
                    public boolean missingFloat(float value) {
                        return true;
                    }

                    @Override
                    public boolean missingFloat4(Float4 value) {
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

    private static void replaceAS3(String asp, String as, ScriptPack pack, boolean air) throws IOException, InterruptedException {
        System.out.println("Replacing: " + asp);
        System.out.println("Warning: This feature is EXPERIMENTAL");
        As3ScriptReplacerInterface scriptReplacer = As3ScriptReplacerFactory.createByConfig(air);
        if (!scriptReplacer.isAvailable()) {
            System.err.println("Current script replacer is not available.");
            if (scriptReplacer instanceof FFDecAs3ScriptReplacer) {
                System.err.println("Current replacer: FFDec");
                final String adobePage = "http://www.adobe.com/support/flashplayer/downloads.html";
                System.err.println("For ActionScript 3 direct editation, a library called \"PlayerGlobal.swc\" needs to be downloaded from Adobe homepage:");
                System.err.println(adobePage);
                System.err.println("Download the library called PlayerGlobal(.swc), and place it to directory");
                System.err.println(Configuration.getFlashLibPath().getAbsolutePath());
            } else if (scriptReplacer instanceof MxmlcAs3ScriptReplacer) {
                System.err.println("Current replacer: Flex SDK");
                final String flexPage = "http://www.adobe.com/devnet/flex/flex-sdk-download.html";
                System.err.println("For ActionScript 3 direct editation, Flex SDK needs to be download");
                System.err.println(flexPage);
                System.err.println("Download FLEX Sdk, unzip it to some directory and set its directory path in the configuration");

            }
            System.exit(1);
        }

        try {
            pack.abc.replaceScriptPack(scriptReplacer, pack, as, Main.getDependencies(pack.abc.getSwf()));
        } catch (As3ScriptReplaceException asre) {
            for (As3ScriptReplaceExceptionItem item : asre.getExceptionItems()) {
                String r = "%error% on line %line%, column %col%, file: %file%".replace("%error%", "" + item.getMessage());
                r = r.replace("%line%", Long.toString(item.getLine()));
                r = r.replace("%file%", "" + item.getFile());
                r = r.replace("%col%", "" + item.getCol());
                logger.log(Level.SEVERE, r);
            }
            System.exit(1);
        }
    }

    private static void parseInfo(Stack<String> args, String charset) throws FileNotFoundException {
        File out;
        PrintWriter pw = new PrintWriter(System.out);
        boolean found = false;
        boolean detectBundle = true;
        while (!args.isEmpty()) {
            String a = args.pop();
            switch (a) {
                case "-out":
                    if (args.isEmpty()) {
                        badArguments("info");
                    }
                    out = new File(args.pop());
                    if (out.isDirectory()) {
                        logger.log(Level.SEVERE, "File is a directory");
                        System.exit(1);
                    } else {
                        pw = new PrintWriter(out);
                    }
                    break;
                case "-nobundle":
                    detectBundle = false;
                    break;
                default:
                    Bundle bundle;
                    String sfile = a;
                    File file = new File(sfile);
                    try {

                        OpenableSourceInfo sourceInfo = new OpenableSourceInfo(null, sfile, sfile, detectBundle);
                        bundle = sourceInfo.getBundle(false, SearchMode.ALL);
                        logger.log(Level.INFO, "Load file: {0}", sourceInfo.getFile());

                        if (bundle != null) {
                            int subcnt = 0;
                            for (Entry<String, SeekableInputStream> streamEntry : bundle.getAll().entrySet()) {
                                InputStream stream = streamEntry.getValue();
                                stream.reset();
                                CancellableWorker<SWF> worker = new CancellableWorker<SWF>("parseInfoBundle") {
                                    @Override
                                    public SWF doInBackground() throws Exception {
                                        final CancellableWorker worker = this;
                                        SWF swf = new SWF(stream, null, streamEntry.getKey(), new ProgressListener() {
                                            @Override
                                            public void progress(int p) {
                                                //...
                                            }

                                            @Override
                                            public void status(String status) {
                                            }
                                        }, Configuration.parallelSpeedUp.get(), charset);
                                        return swf;
                                    }
                                };
                                //loadingDialog.setWorker(worker);
                                worker.execute();

                                subcnt++;
                                try {
                                    processInfoSWF(file, worker.get(), pw);
                                } catch (CancellationException | ExecutionException | InterruptedException ex) {
                                    logger.log(Level.WARNING, "Loading SWF {0} was cancelled.", streamEntry.getKey());
                                }
                            }
                            if (subcnt > 0) {
                                found = true;
                            } else {
                                System.err.println("No SWFs found in \"" + file + "\"");
                            }
                        } else {
                            StdInAwareFileInputStream fis = new StdInAwareFileInputStream(file);
                            BufferedInputStream inputStream = new BufferedInputStream(fis);

                            InputStream fInputStream = inputStream;
                            CancellableWorker<SWF> worker = new CancellableWorker<SWF>("parseInfo") {
                                @Override
                                public SWF doInBackground() throws Exception {
                                    final CancellableWorker worker = this;
                                    SWF swf = new SWF(fInputStream, sourceInfo.getFile(), sourceInfo.getFileTitle(), new ProgressListener() {
                                        @Override
                                        public void progress(int p) {
                                            //startWork(AppStrings.translate("work.reading.swf"), p, worker);
                                        }

                                        @Override
                                        public void status(String status) {
                                        }
                                    }, Configuration.parallelSpeedUp.get(), charset);
                                    return swf;
                                }
                            };

                            worker.execute();

                            try {
                                processInfoSWF(null, worker.get(), pw);
                            } catch (CancellationException | ExecutionException | InterruptedException ex) {
                                logger.log(Level.WARNING, "Loading SWF was cancelled.");
                            }
                            found = true;
                        }
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, "Cannot read.");
                        System.exit(1);
                    }
            }
        }
        if (!found) {
            System.exit(1);
        }
    }

    private static String doubleToString(double d) {
        String ds = "" + d;
        if (ds.endsWith(".0")) {
            ds = ds.substring(0, ds.length() - 2);
        }
        return ds;
    }

    private static void processInfoSWF(File bundle, SWF swf, PrintWriter pw) throws IOException {
        pw.println("[swf]");
        pw.println("file=" + (bundle == null ? swf.getFile() : bundle + ":" + swf.getFileTitle()));
        pw.println("fileSize=" + swf.fileSize);
        pw.println("version=" + swf.version);
        pw.println("compression=" + swf.compression);
        pw.println("encrypted=" + swf.encrypted);
        pw.println("gfx=" + swf.gfx);
        pw.println("width=" + doubleToString(swf.displayRect.getWidth() / SWF.unitDivisor));
        pw.println("height=" + doubleToString(swf.displayRect.getHeight() / SWF.unitDivisor));
        pw.println("frameCount=" + swf.frameCount);
        pw.println("frameRate=" + doubleToString(swf.frameRate));
        for (Tag t : swf.getTags()) {
            if (t instanceof SetBackgroundColorTag) {
                pw.println("backgroundColor=" + ((SetBackgroundColorTag) t).backgroundColor.toHexRGB());
            }
            if (t instanceof ScriptLimitsTag) {
                pw.println("maxRecursionDepth=" + ((ScriptLimitsTag) t).maxRecursionDepth);
                pw.println("scriptTimeoutSeconds=" + ((ScriptLimitsTag) t).scriptTimeoutSeconds);
            }
        }
        pw.println();

        pw.println("[tags]");
        pw.println("tagCount=" + swf.getTags().size());
        pw.println("hasEndTag=" + swf.hasEndTag);
        pw.println("characterCount=" + (swf.getCharacters(true).size()));
        pw.println("maxCharacterId=" + (swf.getNextCharacterId() - 1));
        pw.println();

        FileAttributesTag fa = swf.getFileAttributes();
        if (fa != null) {
            pw.println("[attributes]");
            pw.println("actionScript3=" + fa.actionScript3);
            pw.println("hasMetadata=" + fa.hasMetadata);
            pw.println("noCrossDomainCache=" + fa.noCrossDomainCache);
            pw.println("swfRelativeUrls=" + fa.swfRelativeUrls);
            pw.println("useDirectBlit=" + fa.useDirectBlit);
            pw.println("useGPU=" + fa.useGPU);
            pw.println("useNetwork=" + fa.useNetwork);

            pw.println();
        }

        pw.println("[as2]");
        pw.println("scriptsCount=" + swf.getASMs(true).size());
        pw.println();

        pw.println("[as3]");
        pw.println("ABCtagCount=" + swf.getAbcList().size());
        pw.println("packsCount=" + swf.getAS3Packs().size());
        String dcs = swf.getDocumentClass();
        if (dcs != null) {
            if (dcs.contains(".")) {
                DottedChain dc = DottedChain.parseWithSuffix(dcs);
                pw.println("documentClass=" + dc.toPrintableString(true));
            } else {
                pw.println("documentClass=" + IdentifiersDeobfuscation.printIdentifier(true, dcs));
            }
        } else {
            pw.println("documentClass=");
        }

        pw.println();
        pw.flush();
    }

    private static void parseDumpSwf(Stack<String> args) {
        if (args.isEmpty()) {
            badArguments("dumpswf");
        }
        try {
            Configuration.dumpTags.set(true);
            Configuration.parallelSpeedUp.set(false);
            OpenableSourceInfo sourceInfo = new OpenableSourceInfo(null, args.pop(), null);
            Main.parseOpenable(sourceInfo);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

    private static void parseDumpAS2(Stack<String> args, String charset) {
        if (args.isEmpty()) {
            badArguments("dumpas2");
        }
        File file = new File(args.pop());
        try {
            try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(file)) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);
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

    private static void parseEnableDebugging(Stack<String> args, String charset) {
        if (args.size() < 2) {
            badArguments("enabledebugging");
        }

        boolean injectas3 = false;
        boolean doPCode = false;
        boolean generateSwd = false;
        String file = args.pop();
        if (file.equals("-generateswd")) {
            if (args.size() < 2) {
                badArguments("enabledebugging");
            }
            file = args.pop();
            generateSwd = true;
        }
        if (file.equals("-injectas3")) {
            if (args.size() < 2) {
                badArguments("enabledebugging");
            }
            file = args.pop();
            injectas3 = true;
        }
        if (file.equals("-pcode")) {
            if (args.size() < 2) {
                badArguments("enabledebugging");
            }
            doPCode = true;
            file = args.pop();
        }
        String outfile = args.pop();
        try {
            System.out.print("Working...");
            StdInAwareFileInputStream fis = new StdInAwareFileInputStream(file);
            SWF swf = new SWF(fis, Configuration.parallelSpeedUp.get(), charset);
            fis.close();
            if (swf.isAS3()) {
                swf.enableDebugging(injectas3, new File(outfile).getParentFile(), doPCode);
            } else {
                swf.enableDebugging();
            }
            FileOutputStream fos = new FileOutputStream(outfile);
            swf.saveTo(fos);
            fos.close();
            if (!swf.isAS3()) {
                if (generateSwd) {
                    fis = new StdInAwareFileInputStream(outfile);
                    swf = new SWF(fis, Configuration.parallelSpeedUp.get(), charset);
                    fis.close();
                    String outSwd = outfile;
                    if (outSwd.toLowerCase(Locale.ENGLISH).endsWith(".swf")) {
                        outSwd = outSwd.substring(0, outSwd.length() - 4) + ".swd";
                    } else {
                        outSwd = outSwd + ".swd";
                    }
                    if (doPCode) {
                        if (!swf.generatePCodeSwdFile(new File(outSwd), new HashMap<>())) {
                            System.err.println("Generating SWD failed");
                        }
                    } else if (!swf.generateSwdFile(new File(outSwd), new HashMap<>())) {
                        System.err.println("Generating SWD failed");
                    }
                }
            } else if (generateSwd) {
                System.err.println("WARNING: Cannot generate SWD for AS3 file");
            }
            System.out.println("OK");
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, "Cannot read {0}", file);
            System.exit(1);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Reading error {0}", file);
            System.exit(2);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, "Cancelled {0}", file);
            System.exit(3);
        }

        System.out.println("Finished");
    }

    private static void parseDumpAS3(Stack<String> args, String charset) {
        if (args.isEmpty()) {
            badArguments("dumpas3");
        }
        File file = new File(args.pop());
        try {
            try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(file)) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);
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

    private static void parseHeader(Stack<String> args) {
        if (args.isEmpty()) {
            badArguments("header");
        }
        Float frameRate = null;
        Integer frameCount = null;
        Integer width = null;
        Integer height = null;
        Integer x = null;
        Integer y = null;
        Boolean gfx = null;
        Integer version = null;

        Pattern displayRectPattern = Pattern.compile("\\[(?<x1>-?[0-9.]+)(?<x1px>px)?,(?<y1>-?[0-9.]+)(?<y1px>px)?,(?<x2>-?[0-9.]+)(?<x2px>px)?,(?<y2>-?[0-9.]+)(?<y2px>px)?\\]");

        boolean printOnly = args.size() == 1;

        while (!args.isEmpty()) {
            String arg = args.pop();
            if (arg.equals("-set")) {
                if (args.size() < 3) {
                    badArguments("header");
                }
                String key = args.pop();
                String value = args.pop();
                switch (key.toLowerCase()) {
                    case "version":
                        try {
                            version = Integer.valueOf(value);
                        } catch (NumberFormatException nfe) {
                            badArguments("header");
                        }
                        break;
                    case "gfx":
                        gfx = parseBooleanConfigValue(value);
                        if (gfx == null) {
                            badArguments("header");
                        }
                        break;
                    case "displayrect":
                        Matcher displayRectMatcher = displayRectPattern.matcher(value);
                        if (!displayRectMatcher.matches()) {
                            badArguments("header");
                        }
                        if (displayRectMatcher.group("x1px") != null) {
                            x = (int) Math.round(Float.parseFloat(displayRectMatcher.group("x1")) * SWF.unitDivisor);
                        } else {
                            try {
                                x = Integer.valueOf(displayRectMatcher.group("x1"));
                            } catch (NumberFormatException nfe) {
                                badArguments("header");
                            }
                        }
                        if (displayRectMatcher.group("y1px") != null) {
                            y = (int) Math.round(Float.parseFloat(displayRectMatcher.group("y1")) * SWF.unitDivisor);
                        } else {
                            try {
                                y = Integer.valueOf(displayRectMatcher.group("y1"));
                            } catch (NumberFormatException nfe) {
                                badArguments("header");
                            }
                        }

                        int x2 = 0;
                        int y2 = 0;

                        if (displayRectMatcher.group("x2px") != null) {
                            x2 = (int) Math.round(Float.parseFloat(displayRectMatcher.group("x2")) * SWF.unitDivisor);
                        } else {
                            try {
                                x2 = Integer.parseInt(displayRectMatcher.group("x2"));
                            } catch (NumberFormatException nfe) {
                                badArguments("header");
                            }
                        }
                        if (displayRectMatcher.group("y2px") != null) {
                            y2 = (int) Math.round(Float.parseFloat(displayRectMatcher.group("y2")) * SWF.unitDivisor);
                        } else {
                            try {
                                y2 = Integer.parseInt(displayRectMatcher.group("y2"));
                            } catch (NumberFormatException nfe) {
                                badArguments("header");
                            }
                        }

                        width = x2 - x;
                        height = y2 - y;
                        break;
                    case "width":
                        if (value.endsWith("px")) {
                            value = value.substring(0, value.length() - 2).trim();
                            try {
                                width = (int) Math.round(Float.parseFloat(value) * SWF.unitDivisor);
                            } catch (NumberFormatException nfe) {
                                badArguments("header");
                            }
                        } else {
                            try {
                                width = Integer.valueOf(value);
                            } catch (NumberFormatException nfe) {
                                badArguments("header");
                            }
                        }
                        break;
                    case "height":
                        if (value.endsWith("px")) {
                            value = value.substring(0, value.length() - 2).trim();
                            try {
                                height = (int) Math.round(Float.parseFloat(value) * SWF.unitDivisor);
                            } catch (NumberFormatException nfe) {
                                badArguments("header");
                            }
                        } else {
                            try {
                                height = Integer.valueOf(value);
                            } catch (NumberFormatException nfe) {
                                badArguments("header");
                            }
                        }
                        break;
                    case "framecount":
                        try {
                            frameCount = Integer.valueOf(value);
                        } catch (NumberFormatException nfe) {
                            badArguments("header");
                        }
                        break;
                    case "framerate":
                        try {
                            frameRate = Float.valueOf(value);
                        } catch (NumberFormatException nfe) {
                            badArguments("header");
                        }
                        break;
                    default:
                        badArguments("header");
                }
            } else {
                args.push(arg);
                break;
            }
        }

        if (!printOnly && args.size() != 2) {
            badArguments("header");
        }

        File file = new File(args.pop());
        File outfile = printOnly ? null : new File(args.pop());
        PrintWriter pw = new PrintWriter(System.out);
        try {
            try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(file)) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), true);

                if (printOnly) {
                    pw.println("[header]");
                    pw.println("fileSize=" + swf.fileSize);
                    pw.println("version=" + swf.version);
                    pw.println("compression=" + swf.compression);
                    pw.println("encrypted=" + swf.encrypted);
                    pw.println("gfx=" + swf.gfx);
                    pw.println("displayRect=[" + swf.displayRect.Xmin + ", " + swf.displayRect.Ymin + ", " + swf.displayRect.Xmax + ", " + swf.displayRect.Ymax + "]");
                    pw.println("width=" + swf.displayRect.getWidth());
                    pw.println("widthPx=" + doubleToString(swf.displayRect.getWidth() / SWF.unitDivisor));
                    pw.println("height=" + swf.displayRect.getHeight());
                    pw.println("heightPx=" + doubleToString(swf.displayRect.getHeight() / SWF.unitDivisor));
                    pw.println("frameCount=" + swf.frameCount);
                    pw.println("frameRate=" + doubleToString(swf.frameRate));
                    pw.flush();
                    return;
                }
                if (version != null) {
                    swf.version = version;
                    pw.println("version set to " + version);
                }
                if (gfx != null) {
                    swf.gfx = gfx;
                    pw.println("gfx set to " + gfx);
                }
                if (x != null) {
                    swf.displayRect.Xmin = x;
                    pw.println("displayrect.x1 set to " + x);
                }
                if (y != null) {
                    swf.displayRect.Ymin = y;
                    pw.println("displayrect.y1 set to " + y);
                }
                if (width != null) {
                    swf.displayRect.Xmax = swf.displayRect.Xmin + width;
                    pw.println("displayrect.x2 set to " + swf.displayRect.Xmax);
                }
                if (height != null) {
                    swf.displayRect.Ymax = swf.displayRect.Ymin + height;
                    pw.println("displayrect.y2 set to " + swf.displayRect.Ymax);
                }
                if (frameCount != null) {
                    swf.frameCount = frameCount;
                    pw.println("framecount set to " + frameCount);
                }
                if (frameRate != null) {
                    swf.frameRate = frameRate;
                    pw.println("framerate set to " + frameRate);
                }

                try (FileOutputStream fos = new FileOutputStream(outfile)) {
                    swf.saveTo(fos);
                }
                pw.println("Successfully saved");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("I/O error during reading");
            System.exit(2);
        }
        pw.flush();
    }

    private static FilenameFilter getSwfFilter() {
        return (File dir, String name) -> name.toLowerCase(Locale.ENGLISH).endsWith(".swf");
    }

    private static <E extends Enum> E enumFromStr(String str, Class<E> cls) {
        E[] vals = cls.getEnumConstants();
        if (str == null) {
            return vals[0];
        }
        for (E e : vals) {
            if (e.toString().toLowerCase(Locale.ENGLISH).replace("_", "").equals(str.toLowerCase(Locale.ENGLISH).replace("_", ""))) {
                return e;
            }
        }
        return vals[0];
    }

    private static interface SwfAction {

        public void swfAction(SWF swf, OutputStream stdout) throws IOException;
    }

    private static interface AbcAction {

        public void abcAction(ABC abc, OutputStream stdout) throws IOException;
    }

    private static void processReadSWF(File inFile, File stdOutFile, SwfAction action, String charset) {
        OutputStream stdout = null;

        try {
            if (stdOutFile != null) {
                try {
                    stdout = new FileOutputStream(stdOutFile);
                } catch (FileNotFoundException ex) {
                    System.err.println("File not found: " + ex.getMessage());
                    System.exit(1);
                }
            } else {
                stdout = System.out;
            }

            try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(inFile)) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);
                action.swfAction(swf, stdout);
            } catch (FileNotFoundException ex) {
                System.err.println("File not found: " + ex.getMessage());
                System.exit(1);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
                System.exit(1);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Error", ex);
                System.exit(1);
            }
        } finally {
            if (stdOutFile != null) {
                if (stdout != null) {
                    try {
                        stdout.close();
                    } catch (IOException ex) {
                        //ignore
                    }
                }
            }
        }
    }

    private static void processModifySWF(File inFile, File outFile, File stdOutFile, SwfAction action, String charset) {

        OutputStream stdout = null;

        try {
            if (stdOutFile != null) {
                try {
                    stdout = new FileOutputStream(stdOutFile);
                } catch (FileNotFoundException ex) {
                    System.err.println("File not found: " + ex.getMessage());
                    System.exit(1);
                }
            } else {
                stdout = System.out;
            }

            File tmpFile = null;
            if (inFile.equals(outFile)) {
                try {
                    tmpFile = File.createTempFile("ffdec_modify_", ".swf");
                    outFile = tmpFile;
                } catch (IOException ex) {
                    System.err.println("Unable to create temp file");
                    System.exit(1);
                }
            }
            try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(inFile); FileOutputStream fos = new FileOutputStream(outFile)) {
                SWF swf = new SWF(is, Configuration.parallelSpeedUp.get(), charset);
                action.swfAction(swf, stdout);
                swf.saveTo(fos);
            } catch (FileNotFoundException ex) {
                System.err.println("File not found: " + ex.getMessage());
                System.exit(1);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
                System.exit(1);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Error", ex);
                System.exit(1);
            }

            if (tmpFile != null) {
                try {
                    if (!inFile.delete()) {
                        System.err.println("Cannot overwrite original file");
                        System.exit(1);
                    }
                    if (!tmpFile.renameTo(inFile)) {
                        System.err.println("Cannot rename tempfile to original file");
                        System.exit(1);
                    }
                    tmpFile = null;
                    System.out.println(inFile + " overwritten.");
                } finally {
                    if (tmpFile != null && tmpFile.exists()) {
                        tmpFile.delete();
                    }
                }
            }
        } finally {
            if (stdOutFile != null) {
                if (stdout != null) {
                    try {
                        stdout.close();
                    } catch (IOException ex) {
                        //ignore
                    }
                }
            }
        }
    }

    private static void processModifyAbc(File inFile, File outFile, File stdOutFile, AbcAction action, String charset) {

        //It does not have .abc extension, assuming its SWF - process all its ABC tags
        if (!inFile.getAbsolutePath().toLowerCase().endsWith(".abc")) {
            SwfAction swfAction = new SwfAction() {
                @Override
                public void swfAction(SWF swf, OutputStream stdout) throws IOException {
                    List<ABCContainerTag> abcList = swf.getAbcList();
                    for (ABCContainerTag cnt : abcList) {
                        action.abcAction(cnt.getABC(), stdout);
                    }
                }
            };
            processModifySWF(inFile, outFile, stdOutFile, swfAction, charset);
            return;
        }

        OutputStream stdout = null;

        try {
            if (stdOutFile != null) {
                try {
                    stdout = new FileOutputStream(stdOutFile);
                } catch (FileNotFoundException ex) {
                    System.err.println("File not found: " + ex.getMessage());
                    System.exit(1);
                }
            } else {
                stdout = System.out;
            }

            File tmpFile = null;
            if (inFile.equals(outFile)) {
                try {
                    tmpFile = File.createTempFile("ffdec_modify_", ".abc");
                    outFile = tmpFile;
                } catch (IOException ex) {
                    System.err.println("Unable to create temp file");
                    System.exit(1);
                }
            }
            try (StdInAwareFileInputStream is = new StdInAwareFileInputStream(inFile); FileOutputStream fos = new FileOutputStream(outFile)) {
                SWF dummySwf = new SWF();
                dummySwf.setFileTitle(inFile.getName());
                FileAttributesTag fileAttributes = new FileAttributesTag(dummySwf);
                fileAttributes.actionScript3 = true;
                dummySwf.addTag(fileAttributes);
                fileAttributes.setTimelined(dummySwf);
                DoABC2Tag doABC2Tag = new DoABC2Tag(dummySwf);
                dummySwf.addTag(doABC2Tag);
                doABC2Tag.setTimelined(dummySwf);
                dummySwf.clearModified();
                ABC abc = new ABC(new ABCInputStream(new MemoryInputStream(Helper.readStream(is))), dummySwf, doABC2Tag, inFile.getName(), null);
                doABC2Tag.setABC(abc);
                action.abcAction(abc, stdout);
                abc.saveTo(fos);
            } catch (FileNotFoundException ex) {
                System.err.println("File not found: " + ex.getMessage());
                System.exit(1);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Error", ex);
                System.exit(1);
            }

            if (tmpFile != null) {
                try {
                    if (!inFile.delete()) {
                        System.err.println("Cannot overwrite original file");
                        System.exit(1);
                    }
                    if (!tmpFile.renameTo(inFile)) {
                        System.err.println("Cannot rename tempfile to original file");
                        System.exit(1);
                    }
                    tmpFile = null;
                    System.out.println(inFile + " overwritten.");
                } finally {
                    if (tmpFile != null && tmpFile.exists()) {
                        tmpFile.delete();
                    }
                }
            }
        } finally {
            if (stdOutFile != null) {
                if (stdout != null) {
                    try {
                        stdout.close();
                    } catch (IOException ex) {
                        //ignore
                    }
                }
            }
        }
    }
}
