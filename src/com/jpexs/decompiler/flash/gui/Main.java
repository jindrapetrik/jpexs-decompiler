/*
 *  Copyright (C) 2010-2025 JPEXS
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
package com.jpexs.decompiler.flash.gui;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.jpexs.debugger.flash.Debugger;
import com.jpexs.debugger.flash.DebuggerCommands;
import com.jpexs.debugger.flash.Variable;
import com.jpexs.debugger.flash.VariableType;
import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.Bundle;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.OpenableSourceInfo;
import com.jpexs.decompiler.flash.OpenableSourceKind;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SearchMode;
import com.jpexs.decompiler.flash.SwfOpenException;
import com.jpexs.decompiler.flash.UrlResolver;
import com.jpexs.decompiler.flash.Version;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ABCInputStream;
import com.jpexs.decompiler.flash.abc.ABCOpenException;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.CustomConfigurationKeys;
import com.jpexs.decompiler.flash.configuration.SwfSpecificConfiguration;
import com.jpexs.decompiler.flash.configuration.SwfSpecificCustomConfiguration;
import com.jpexs.decompiler.flash.console.CommandLineArgumentParser;
import com.jpexs.decompiler.flash.console.ContextMenuTools;
import com.jpexs.decompiler.flash.exporters.modes.ExeExportMode;
import com.jpexs.decompiler.flash.gfx.GfxConvertor;
import com.jpexs.decompiler.flash.gui.debugger.DebugAdapter;
import com.jpexs.decompiler.flash.gui.debugger.DebugLoaderDataModified;
import com.jpexs.decompiler.flash.gui.debugger.DebuggerTools;
import com.jpexs.decompiler.flash.gui.jna.platform.win32.Advapi32Util;
import com.jpexs.decompiler.flash.gui.jna.platform.win32.Kernel32;
import com.jpexs.decompiler.flash.gui.jna.platform.win32.WinReg;
import com.jpexs.decompiler.flash.gui.pipes.FirstInstance;
import com.jpexs.decompiler.flash.gui.soleditor.CookiesChangedListener;
import com.jpexs.decompiler.flash.gui.soleditor.SharedObjectsStorage;
import com.jpexs.decompiler.flash.gui.soleditor.SolEditorFrame;
import com.jpexs.decompiler.flash.gui.taglistview.TagListTreeModel;
import com.jpexs.decompiler.flash.gui.tagtree.TagTreeModel;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerPlugin;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImportTag;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.treeitems.OpenableList;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.MemoryInputStream;
import com.jpexs.helpers.Path;
import com.jpexs.helpers.ProgressListener;
import com.jpexs.helpers.ReReadableInputStream;
import com.jpexs.helpers.Reference;
import com.jpexs.helpers.Stopwatch;
import com.jpexs.helpers.streams.SeekableInputStream;
import com.jpexs.helpers.utf8.Utf8Helper;
import com.sun.jna.Platform;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.MenuItem;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.geom.AffineTransform;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreePath;
import jsyntaxpane.DefaultSyntaxKit;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;

/**
 * Main executable class
 *
 * @author JPEXS
 */
public class Main {

    public static final String IMPORT_ASSETS_SEPARATOR = "{*sep*}";

    private static List<OpenableSourceInfo> sourceInfos = new ArrayList<>();

    public static LoadingDialog loadingDialog;

    private static boolean working = false;

    private static TrayIcon trayIcon;

    private static MenuItem stopMenuItem;

    private static volatile MainFrame mainFrame;

    public static final int UPDATE_SYSTEM_MAJOR = 1;

    public static final int UPDATE_SYSTEM_MINOR = 3;

    private static LoadFromMemoryFrame loadFromMemoryFrame;

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static DebugLogDialog debugDialog;

    private static Debugger flashDebugger;

    private static DebuggerHandler debugHandler = null;

    //private static int ip = 0;
    //private static String ipClass = null;
    private static Process runProcess;

    private static boolean runProcessDebug;

    private static boolean runProcessDebugPCode;

    private static boolean inited = false;

    private static File runTempFile;

    private static CookiesChangedListener runCookieListener;

    private static List<File> runTempFiles = new ArrayList<>();

    private static WatchService watcher;

    private static SwingWorker watcherWorker;

    private static Map<WatchKey, File> watchedDirectories = new HashMap<>();

    private static FilesChangedDialog filesChangedDialog;

    private static List<File> savedFiles = Collections.synchronizedList(new ArrayList<>());

    public static SearchResultsStorage searchResultsStorage = new SearchResultsStorage();

    public static CancellableWorker importWorker = null;
    public static CancellableWorker deobfuscatePCodeWorker = null;
    public static CancellableWorker swfPrepareWorker = null;

    public static String currentDebuggerPackage = null;

    private static SWF runningSWF = null;

    private static SwfPreparation runningPreparation = null;

    public static SWF getRunningSWF() {
        return runningSWF;
    }

    public static WatchService getWatcher() {
        return watcher;
    }

    public static boolean isSwfAir(Openable openable) {
        SwfSpecificCustomConfiguration conf = Configuration.getSwfSpecificCustomConfiguration(openable.getShortPathTitle());
        if (conf != null) {
            String libraryAsStr = conf.getCustomData(CustomConfigurationKeys.KEY_LIBRARY, "" + SWF.LIBRARY_FLASH);
            int libraryAsInt = Integer.parseInt(libraryAsStr);
            return libraryAsInt == SWF.LIBRARY_AIR;
        }
        return false;
    }

    //This method makes file watcher to shut up during our own file saving
    public static void startSaving(File savedFile) {
        savedFiles.add(savedFile);
    }

    public static void stopSaving(File savedFile) {
        View.execInEventDispatchLater(new Runnable() {
            @Override
            public void run() {
                //TODO: handle this better
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    //ignore
                }
                savedFiles.remove(savedFile);
            }
        });
    }

    public static void freeRun() {
        synchronized (Main.class) {
            if (runTempFile != null) {
                deleteCookiesAfterRun(runTempFile);
                runTempFile.delete();
                runTempFile = null;
            }
            for (File f : runTempFiles) {
                f.delete();
            }
            runTempFiles.clear();

            runProcess = null;
            runningSWF = null;
            runningPreparation = null;
        }
        if (mainFrame != null && mainFrame.getPanel() != null) {
            mainFrame.getPanel().clearDebuggerColors();
        }
        if (runProcessDebug) {
            Main.getDebugHandler().disconnect();
        }
    }

    public static synchronized boolean isDebugPaused() {
        return runProcess != null && runProcessDebug && getDebugHandler().isPaused();
    }

    public static synchronized boolean isDebugRunning() {
        return runProcess != null && runProcessDebug;
    }

    public static synchronized boolean isDebugPCode() {
        return runProcessDebugPCode;
    }

    public static synchronized boolean isDebugConnected() {
        return getDebugHandler().isConnected();
    }

    public static synchronized boolean isRunning() {
        return runProcess != null && !runProcessDebug;
    }

    public static synchronized void debugExportByteArray(Variable v, OutputStream os) throws IOException {
        try {
            long objectId = 0L;
            if ((v.vType == VariableType.OBJECT || v.vType == VariableType.MOVIECLIP)) {
                objectId = (Long) v.value;
            }
            Object oldPos = getDebugHandler().getVariable(objectId, "position", false, true).parent.value;
            getDebugHandler().setVariable(objectId, "position", VariableType.NUMBER, 0);
            int length = (int) (double) (Double) getDebugHandler().getVariable(objectId, "length", false, true).parent.value;

            for (int i = 0; i < length; i++) {
                int b = (int) (double) (Double) getDebugHandler().callFunction(false, "readByte", v, new ArrayList<>()).variables.get(0).value;
                os.write(b);
            }
            getDebugHandler().setVariable(objectId, "position", VariableType.NUMBER, oldPos);
        } catch (DebuggerHandler.ActionScriptException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static synchronized void debugImportByteArray(Variable v, InputStream is) throws IOException {
        try {
            long objectId = 0L;
            if ((v.vType == VariableType.OBJECT || v.vType == VariableType.MOVIECLIP)) {
                objectId = (Long) v.value;
            }
            Double oldPos = (Double) getDebugHandler().getVariable(objectId, "position", false, true).parent.value;
            getDebugHandler().setVariable(objectId, "length", VariableType.NUMBER, 0);
            getDebugHandler().setVariable(objectId, "position", VariableType.NUMBER, 0);

            int length = 0;
            int b;
            while ((b = is.read()) > -1) {
                getDebugHandler().callFunction(false, "writeByte", v, Arrays.asList((Double) (double) b));
                length++;
            }
            if (oldPos > length) {
                oldPos = (Double) (double) length;
            }
            getDebugHandler().setVariable(objectId, "position", VariableType.NUMBER, oldPos);
        } catch (DebuggerHandler.ActionScriptException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static synchronized boolean addWatch(Variable v, long v_id, boolean watchRead, boolean watchWrite) {
        DebuggerCommands.Watch w = getDebugHandler().addWatch(v, v_id, watchRead, watchWrite);
        return w != null;
    }

    public static void runPlayer(String title, final String exePath, String file, String flashVars) {
        if (!new File(file).exists()) {
            return;
        }
        if (flashVars != null && !flashVars.isEmpty()) {
            file += "?" + flashVars;
        }
        final String ffile = file;

        CancellableWorker runWorker = new CancellableWorker("runWorker") {
            @Override
            protected Object doInBackground() throws Exception {
                Process proc;
                try {
                    proc = Runtime.getRuntime().exec(new String[]{exePath, ffile});
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    return null;
                }
                boolean isDebug;

                synchronized (Main.class) {
                    runProcess = proc;
                    isDebug = runProcessDebug;
                }
                if (isDebug) {
                    mainFrame.getMenu().hilightPath("/debugging");
                }
                mainFrame.getMenu().updateComponents();
                try {
                    if (proc != null) {
                        proc.waitFor();
                    }
                } catch (InterruptedException ex) {
                    if (proc != null) {
                        try {
                            proc.destroy();
                        } catch (Exception ex2) {
                            //ignore
                        }
                    }
                }
                freeRun();
                stopDebugger();
                mainFrame.getMenu().updateComponents();
                return null;
            }

            @Override
            protected void done() {
                Main.stopWork();
            }

            @Override
            public void workerCancelled() {
                Main.stopWork();
                synchronized (Main.class) {
                    if (runProcess != null) {
                        try {
                            runProcess.destroy();
                        } catch (Exception ex) {
                            //ignored
                        }
                    }
                }
                freeRun();
                mainFrame.getMenu().updateComponents();
            }
        };

        mainFrame.getMenu().updateComponents();
        Main.startWork(title + "...", runWorker);
        runWorker.execute();
    }

    public static void stopRun() {

        synchronized (Main.class) {
            if (runProcess != null) {
                runProcess.destroy();
            }
        }
        freeRun();
        stopDebugger();
        mainFrame.getMenu().updateComponents();
    }

    private static interface SwfPreparation {

        public SWF prepare(SWF swf, String swfHash, List<File> tempFiles) throws InterruptedException;
    }

    private static class SwfRunPrepare implements SwfPreparation {

        @Override
        public SWF prepare(SWF swf, String swfHash, List<File> tempFiles) throws InterruptedException {
            if (Configuration.autoOpenLoadedSWFs.get()) {
                if (!DebuggerTools.hasDebugger(swf)) {
                    DebuggerTools.switchDebugger(swf);
                }
                DebuggerTools.injectDebugLoader(swf);
            }
            return swf;
        }
    }

    private static class SwfDebugPrepare extends SwfRunPrepare {

        private boolean doPCode;

        public SwfDebugPrepare(boolean doPCode) {
            this.doPCode = doPCode;
        }

        @Override
        public SWF prepare(SWF instrSWF, String swfHash, List<File> tempFiles) throws InterruptedException {
            EventListener prepEventListener = new EventListener() {
                @Override
                public void handleExportingEvent(String type, int index, int count, Object data) {
                }

                @Override
                public void handleExportedEvent(String type, int index, int count, Object data) {
                }

                @Override
                public void handleEvent(String event, Object data) {
                    if (event.equals("inject_debuginfo")) {
                        startWork(AppStrings.translate("work.injecting_debuginfo") + "..." + (String) data, swfPrepareWorker);
                    }
                }
            };
            instrSWF.addEventListener(prepEventListener);
            try {
                File fTempFile = new File(instrSWF.getFile());
                startWork(AppStrings.translate("work.injecting_debuginfo"), swfPrepareWorker, true);
                instrSWF.enableDebugging(true, new File("."), true, doPCode, swfHash);
                try (FileOutputStream fos = new FileOutputStream(fTempFile)) {
                    instrSWF.saveTo(fos);
                }
                if (!instrSWF.isAS3()) {
                    //Read again, because line file offsets changed with adding debug tags
                    //TODO: handle somehow without rereading?
                    instrSWF = null;
                    try (FileInputStream fis = new FileInputStream(fTempFile)) {
                        instrSWF = new SWF(fis, false, false);
                    } catch (InterruptedException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                    if (instrSWF != null) {
                        String swfFileName = fTempFile.getAbsolutePath();
                        if (swfFileName.toLowerCase(Locale.ENGLISH).endsWith(".swf")) {
                            swfFileName = swfFileName.substring(0, swfFileName.length() - 4) + ".swd";
                        } else {
                            swfFileName = swfFileName + ".swd";
                        }
                        File swdFile = new File(swfFileName);
                        instrSWF.addEventListener(new EventListener() {
                            @Override
                            public void handleExportingEvent(String type, int index, int count, Object data) {
                            }

                            @Override
                            public void handleExportedEvent(String type, int index, int count, Object data) {
                            }

                            @Override
                            public void handleEvent(String event, Object data) {
                                if (event.equals("generate_swd")) {
                                    startWork(AppStrings.translate("work.generating_swd") + "..." + (String) data, swfPrepareWorker);
                                }
                            }
                        });
                        startWork(AppStrings.translate("work.generating_swd"), swfPrepareWorker, true);
                        if (doPCode) {
                            instrSWF.generatePCodeSwdFile(swdFile, getPackBreakPoints(true, swfHash), swfHash);
                        } else {
                            instrSWF.generateSwdFile(swdFile, getPackBreakPoints(true, swfHash), swfHash);
                        }
                        tempFiles.add(swdFile);
                    }
                }
            } catch (IOException ex) {
                //ignore, return instrSWF
            }
            instrSWF.removeEventListener(prepEventListener);

            //instrSWF = super.prepare(instrSWF);
            if (!DebuggerTools.hasDebugger(instrSWF)) {
                DebuggerTools.switchDebugger(instrSWF);
            }
            DebuggerTools.injectDebugLoader(instrSWF);
            currentDebuggerPackage = instrSWF.debuggerPackage;

            return instrSWF;
        }
    }

    private static void prepareSwf(String swfHash, SwfPreparation prep, File toPrepareFile, File origFile, File tempFilesDir, List<File> tempFiles) throws IOException, InterruptedException {
        SWF instrSWF = null;
        try (FileInputStream fis = new FileInputStream(toPrepareFile)) {
            instrSWF = new SWF(fis, toPrepareFile.getAbsolutePath(), origFile == null ? "unknown.swf" : origFile.getName(), false);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        if (instrSWF != null) {
            if (origFile != null) {
                for (Tag t : instrSWF.getTags()) {
                    if (t instanceof ImportTag) {
                        ImportTag it = (ImportTag) t;
                        String url = it.getUrl();
                        File importedFile = new File(origFile.getParentFile(), url);
                        if (importedFile.exists()) {
                            File newTempFile = createTempFileInDir(tempFilesDir, "~ffdec_run_import_", ".swf");
                            it.setUrl("./" + newTempFile.getName());
                            byte[] impData = Helper.readFile(importedFile.getAbsolutePath());
                            Helper.writeFile(newTempFile.getAbsolutePath(), impData);
                            tempFiles.add(newTempFile);
                            prepareSwf("imported_" + md5(impData), prep, newTempFile, importedFile, tempFilesDir, tempFiles);
                        }
                    }
                }
            }
            if (prep != null) {
                List<File> prepTempFiles = new ArrayList<>();
                instrSWF = prep.prepare(instrSWF, swfHash, prepTempFiles);
                tempFiles.addAll(prepTempFiles);
            }
            try (FileOutputStream fos = new FileOutputStream(toPrepareFile)) {
                instrSWF.saveTo(fos);
            }
        }
    }

    private static File createTempFileInDir(File tempFilesDir, String prefix, String suffix) throws IOException {
        if (tempFilesDir == null || !tempFilesDir.isDirectory()) {
            return File.createTempFile(prefix, suffix);
        }
        try {
            return File.createTempFile(prefix, suffix, tempFilesDir);
        } catch (IOException ex) {
            return File.createTempFile(prefix, suffix);
        }
    }

    public static void runAsync(File swfFile) {
        String playerLocation = Configuration.playerLocation.get();
        if (playerLocation.isEmpty() || (!new File(playerLocation).exists())) {
            ViewMessages.showMessageDialog(getDefaultMessagesComponent(), AppStrings.translate("message.playerpath.notset"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            advancedSettings("paths");
            return;
        }
        try {
            final Process process = Runtime.getRuntime().exec(new String[]{playerLocation, swfFile.getAbsolutePath()});
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    if (process.isAlive()) {
                        process.destroyForcibly();
                    }
                }
            });
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public static void run(SWF swf) {
        String flashVars = ""; //key=val&key2=val2
        String playerLocation = Configuration.playerLocation.get();
        if (playerLocation.isEmpty() || (!new File(playerLocation).exists())) {
            ViewMessages.showMessageDialog(getDefaultMessagesComponent(), AppStrings.translate("message.playerpath.notset"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            advancedSettings("paths");
            return;
        }
        if (swf == null) {
            return;
        }
        File tempRunDir = swf.getFile() == null ? null : new File(swf.getFile()).getParentFile();

        File tempFile;
        List<File> tempFiles = new ArrayList<>();
        try {
            tempFile = createTempFileInDir(tempRunDir, "~ffdec_run_", ".swf");

            SWF swfToSave = swf;
            if (swf.gfx) {
                swfToSave = new GfxConvertor().convertSwf(swf);
            }
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                swfToSave.saveTo(fos, false, swf.gfx);
            }

            prepareSwf("main", new SwfRunPrepare(), tempFile, swf.getFile() == null ? null : new File(swf.getFile()), tempRunDir, tempFiles);

        } catch (IOException ex) {
            return;
        } catch (InterruptedException ex) {
            return;
        }
        if (tempFile != null) {
            prepareCookiesForRun(tempFile, swf);
            synchronized (Main.class) {
                runTempFile = tempFile;
                runTempFiles = tempFiles;
                runProcessDebug = false;
                runningSWF = swf;
                runningPreparation = new SwfRunPrepare();
            }
            runPlayer(AppStrings.translate("work.running"), playerLocation, tempFile.getAbsolutePath(), flashVars);
        }
    }

    private static void deleteCookiesAfterRun(File tempFile) {
        SharedObjectsStorage.removeChangedListener(tempFile, runCookieListener);
        File solDir = SharedObjectsStorage.getSolDirectoryForLocalFile(tempFile);
        File origSolDir = runningSWF.getFile() == null ? null : SharedObjectsStorage.getSolDirectoryForLocalFile(new File(runningSWF.getFile()));
        if (solDir != null) {
            WatchKey foundKey = null;
            for (WatchKey key : SharedObjectsStorage.watchedCookieDirectories.keySet()) {
                if (SharedObjectsStorage.watchedCookieDirectories.get(key).equals(solDir)) {
                    foundKey = key;
                    break;
                }
            }
            if (foundKey != null) {
                SharedObjectsStorage.watchedCookieDirectories.remove(foundKey);
            }

            View.execInEventDispatchLater(new Runnable() {
                public void run() {
                    if (solDir.exists()) {

                        if (origSolDir != null && origSolDir.exists()) {
                            for (File f : origSolDir.listFiles()) {
                                f.delete();
                            }

                            for (File f : solDir.listFiles()) {
                                try {
                                    Files.copy(f.toPath(), origSolDir.toPath().resolve(f.getName()), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                                } catch (IOException ex) {
                                    //ignored
                                }
                            }
                        }

                        for (File f : solDir.listFiles()) {
                            f.delete();
                        }
                        solDir.delete();
                    }
                }
            });
        }
        synchronized (Main.class) {
            runCookieListener = null;
        }
    }

    private static void prepareCookiesForRun(File tempFile, SWF swf) {
        if (swf.getFile() == null) {
            return;
        }
        File origSolDir = SharedObjectsStorage.getSolDirectoryForLocalFile(new File(swf.getFile()));
        if (origSolDir == null) {
            return;
        }
        File tempSolDir = SharedObjectsStorage.getSolDirectoryForLocalFile(tempFile);
        if (tempSolDir == null) {
            return;
        }
        tempSolDir.mkdirs();
        if (origSolDir.exists()) {
            for (File f : origSolDir.listFiles()) {
                try {
                    Files.copy(f.toPath(), tempSolDir.toPath().resolve(f.getName()), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                } catch (IOException ex) {
                    //ignored
                }
            }
        }

        runCookieListener = new CookiesChangedListener() {
            @Override
            public void cookiesChanged(File swfFile, List<File> cookies) {
                File origSolDir = SharedObjectsStorage.getSolDirectoryForLocalFile(new File(swf.getFile()));
                if (cookies.isEmpty() && !origSolDir.exists()) {
                    return;
                }
                origSolDir.mkdirs();
                for (File f : origSolDir.listFiles()) {
                    f.delete();
                }
                for (File f : cookies) {
                    if (!f.exists()) {
                        continue;
                    }
                    try {
                        Files.copy(f.toPath(), origSolDir.toPath().resolve(f.getName()), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                    } catch (IOException ex) {
                        //ignored
                    }
                }
            }
        };
        SharedObjectsStorage.addChangedListener(tempFile, runCookieListener);
    }

    public static void runDebug(SWF swf, final boolean doPCode) {
        String flashVars = ""; //key=val&key2=val2
        String playerLocation = Configuration.playerDebugLocation.get();
        if (playerLocation.isEmpty() || (!new File(playerLocation).exists())) {
            ViewMessages.showMessageDialog(getDefaultMessagesComponent(), AppStrings.translate("message.playerpath.debug.notset"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            Main.advancedSettings("paths");
            return;
        }
        if (swf == null) {
            return;
        }
        debugHandler.setMainDebuggedSwf(swf);
        File tempRunDir = swf.getFile() == null ? null : new File(swf.getFile()).getParentFile();
        File tempFile = null;

        try {
            tempFile = createTempFileInDir(tempRunDir, "~ffdec_debug_", ".swf");
        } catch (Exception ex) {
            //ignored
        }

        if (tempFile != null) {
            final File fTempFile = tempFile;
            final List<File> tempFiles = new ArrayList<>();
            CancellableWorker instrumentWorker = new CancellableWorker("instrumentWorker") {
                @Override
                protected Object doInBackground() throws Exception {

                    SWF swfToSave = swf;
                    if (swf.gfx) {
                        swfToSave = new GfxConvertor().convertSwf(swf);
                    }
                    try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(fTempFile))) {
                        swfToSave.saveTo(fos, false, swf.gfx);
                    }
                    prepareSwf("main", new SwfDebugPrepare(doPCode), fTempFile, swf.getFile() == null ? null : new File(swf.getFile()), tempRunDir, tempFiles);
                    return null;
                }

                @Override
                public void workerCancelled() {
                    Main.stopWork();
                }

                @Override
                protected void onStart() {
                    swfPrepareWorker = this;
                }

                @Override
                protected void done() {
                    prepareCookiesForRun(fTempFile, swf);
                    synchronized (Main.class) {
                        runTempFile = fTempFile;
                        runProcessDebug = true;
                        runProcessDebugPCode = doPCode;
                        runTempFiles = tempFiles;
                        runningSWF = swf;
                        runningPreparation = new SwfDebugPrepare(doPCode);
                    }
                    Main.stopWork();
                    Main.startWork(AppStrings.translate("work.debugging.start") + "...", null, true);
                    Main.startDebugger();
                    runPlayer(AppStrings.translate("work.debugging.wait"), playerLocation, fTempFile.getAbsolutePath(), flashVars);
                }
            };

            Main.startWork(AppStrings.translate("work.debugging.instrumenting"), instrumentWorker);
            instrumentWorker.execute();
        }
    }

    /*    public static void debuggerNotSuspended() {

     }*/
    public static boolean isDebugging() {
        return isDebugRunning();
    }

    public static synchronized int getIp(Object pack) {
        return getDebugHandler().getBreakIp();
    }

    public static synchronized String getIpClass() {
        return getDebugHandler().getBreakScriptName();
    }

    public static synchronized List<Integer> getStackLines() {
        return getDebugHandler().getStackLines();
    }

    public static synchronized List<String> getStackClasses() {
        return getDebugHandler().getStackScripts();
    }

    public static synchronized boolean isBreakPointValid(String scriptName, int line) {
        SWF swf = getMainFrame().getPanel().getCurrentSwf();
        return !getDebugHandler().isBreakpointInvalid(swf, scriptName, line);
    }

    public static synchronized void addBreakPoint(String scriptName, int line) {
        SWF swf = getMainFrame().getPanel().getCurrentSwf();
        getDebugHandler().addBreakPoint(swf, scriptName, line);
    }

    public static synchronized void removeBreakPoint(String scriptName, int line) {
        SWF swf = getMainFrame().getPanel().getCurrentSwf();
        getDebugHandler().removeBreakPoint(swf, scriptName, line);
    }

    public static synchronized boolean toggleBreakPoint(String scriptName, int line) {
        SWF swf = getMainFrame().getPanel().getCurrentSwf();
        if (getDebugHandler().isBreakpointToAdd(swf, scriptName, line) || getDebugHandler().isBreakpointConfirmed(swf, scriptName, line) || getDebugHandler().isBreakpointInvalid(swf, scriptName, line)) {
            getDebugHandler().removeBreakPoint(swf, scriptName, line);
            return false;
        } else {
            getDebugHandler().addBreakPoint(swf, scriptName, line);
            return true;
        }
    }

    public static synchronized Map<String, Set<Integer>> getPackBreakPoints(boolean validOnly, String swfHash) {
        SWF swf = Main.getSwfByHash(swfHash);
        return getDebugHandler().getAllBreakPoints(swf, validOnly);
    }

    public static synchronized Set<Integer> getScriptBreakPoints(String pack, boolean onlyValid) {
        SWF swf = getMainFrame().getPanel().getCurrentSwf();
        return getDebugHandler().getBreakPoints(swf, pack, onlyValid);
    }

    public static DebuggerHandler getDebugHandler() {
        return debugHandler;
    }

    public static void ensureMainFrame() {
        if (mainFrame == null) {
            synchronized (Main.class) {
                if (mainFrame == null) {
                    MainFrame frame;
                    if (Configuration.useRibbonInterface.get()) {
                        frame = new MainFrameRibbon();
                    } else {
                        frame = new MainFrameClassic();
                    }
                    frame.getPanel().setErrorState(ErrorLogFrame.getInstance().getErrorState());
                    mainFrame = frame;
                }
            }
        }
    }

    public static MainFrame getMainFrame() {
        return mainFrame;
    }

    public static Component getDefaultMessagesComponent() {
        if (mainFrame != null) {
            return mainFrame.getPanel();
        }
        return null;
    }

    public static Window getDefaultDialogsOwner() {
        if (mainFrame != null) {
            return mainFrame.getWindow();
        }
        return null;
    }

    public static void loadFromMemory() {
        if (loadFromMemoryFrame == null) {
            loadFromMemoryFrame = new LoadFromMemoryFrame(mainFrame);
        }
        loadFromMemoryFrame.setVisible(true);
    }

    public static void setVariable(long parentId, String varName, int valueType, Object value) {
        getDebugHandler().setVariable(parentId, varName, valueType, value);
    }

    public static void setSubLimiter(boolean value) {
        if (value) {
            AVM2Code.toSourceLimit = Configuration.sublimiter.get();
        } else {
            AVM2Code.toSourceLimit = -1;
        }
    }

    public static synchronized boolean isInited() {
        return inited;
    }

    public static synchronized void setSessionLoaded(boolean v) {
        inited = v;
    }

    public static boolean isWorking() {
        return working;
    }

    public static void continueWork(String name) {
        continueWork(name, -1);
    }

    public static void continueWork(String name, final int percent) {
        startWork(name, percent, mainFrame.getPanel().getCurrentWorker());
    }

    private static long lastTimeStartWork = 0L;

    private static final Timer statusTimer = new Timer("status", true);

    static {
        statusTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mainFrame == null) {
                    return;
                }
                MainPanel mainPanel = mainFrame.getPanel();
                if (mainPanel == null) {
                    return;
                }
                MainFrameStatusPanel sp = mainPanel.getStatusPanel();
                if (sp != null && sp.isStatusHidden()) {
                    long nowTime = System.currentTimeMillis();
                    if (nowTime > lastTimeStartWork + 5000) {
                        mainFrame.getPanel().showOldStatus();
                    }
                }
            }
        }, 5000, 5000);
    }

    public static void startWork(String name, CancellableWorker worker) {
        startWork(name, -1, worker, false);
    }

    public static void startWork(String name, CancellableWorker worker, boolean force) {
        startWork(name, -1, worker, force);
    }

    public static void startWork(final String name, final int percent, final CancellableWorker worker) {
        startWork(name, percent, worker, false);
    }

    public static void startWork(final String name, final int percent, final CancellableWorker worker, boolean force) {
        working = true;
        long nowTime = System.currentTimeMillis();
        if (mainFrame != null && nowTime < lastTimeStartWork + 1000) {
            mainFrame.getPanel().setWorkStatusHidden(name, worker);
            return;
        }
        lastTimeStartWork = nowTime;
        View.execInEventDispatch(() -> {
            if (mainFrame != null) {
                mainFrame.getPanel().setWorkStatus(name, worker);
                if (percent == -1) {
                    mainFrame.getPanel().hidePercent();
                } else {
                    mainFrame.getPanel().setPercent(percent);
                }
            }
            if (loadingDialog != null) {
                loadingDialog.setDetail(name);
                loadingDialog.setPercent(percent);
            }
            if (CommandLineArgumentParser.isCommandLineMode()) {
                System.out.println(name);
            }
        });
    }

    public static void stopWork() {
        working = false;
        lastTimeStartWork = 0;
        View.execInEventDispatchLater(() -> {
            if (mainFrame != null) {
                mainFrame.getPanel().setWorkStatus("", null);
            }
            if (loadingDialog != null) {
                loadingDialog.setDetail("");
            }
        });
    }

    public static void populateSwfs(SWF swfParent, List<SWF> output) {
        for (Tag t : swfParent.getTags()) {
            if (t instanceof DefineBinaryDataTag) {
                DefineBinaryDataTag b = (DefineBinaryDataTag) t;
                if (b.innerSwf != null) {
                    output.add(b.innerSwf);
                    populateSwfs(b.innerSwf, output);
                }
            }
        }
    }

    public static OpenableList parseOpenable(OpenableSourceInfo sourceInfo) throws Exception {
        OpenableList result = new OpenableList();

        InputStream inputStream = sourceInfo.getInputStream();
        Bundle bundle = null;
        FileInputStream fis = null;
        if (inputStream == null) {
            inputStream = new BufferedInputStream(fis = new FileInputStream(sourceInfo.getFile()));
            bundle = sourceInfo.getBundle(false, SearchMode.ALL);
            logger.log(Level.INFO, "Load file: {0}", sourceInfo.getFile());
        } else if (inputStream instanceof SeekableInputStream
                || inputStream instanceof BufferedInputStream) {
            try {
                inputStream.reset();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            logger.log(Level.INFO, "Load stream: {0}", sourceInfo.getFileTitle());
        }

        Stopwatch sw = Stopwatch.startNew();
        if (bundle != null) {
            if (bundle.isReadOnly()) {
                View.execInEventDispatchLater(new Runnable() {
                    @Override
                    public void run() {
                        ViewMessages.showMessageDialog(getMainFrame().getWindow(), AppStrings.translate("warning.readonly").replace("%file%", sourceInfo.getFileTitleOrName()), AppStrings.translate("message.warning"), JOptionPane.WARNING_MESSAGE, Configuration.warningOpeningReadOnly);
                    }
                });
            }
            result.bundle = bundle;
            result.name = new File(sourceInfo.getFileTitleOrName()).getName();
            final String fname = result.name;
            for (Entry<String, SeekableInputStream> streamEntry : bundle.getAll().entrySet()) {
                InputStream stream = streamEntry.getValue();
                stream.reset();
                CancellableWorker<? extends Openable> worker = null;
                if (streamEntry.getKey().toLowerCase().endsWith(".abc")) {
                    CancellableWorker<ABC> abcWorker = new CancellableWorker<ABC>("parseOpenable") {
                        private ABC open(InputStream is, String file, String fileTitle) throws IOException, InterruptedException {
                            SWF dummySwf = new SWF();
                            dummySwf.setFileTitle(fileTitle != null ? fileTitle : file);
                            FileAttributesTag fileAttributes = new FileAttributesTag(dummySwf);
                            fileAttributes.actionScript3 = true;
                            dummySwf.addTag(fileAttributes);
                            fileAttributes.setTimelined(dummySwf);
                            DoABC2Tag doABC2Tag = new DoABC2Tag(dummySwf);
                            dummySwf.addTag(doABC2Tag);
                            doABC2Tag.setTimelined(dummySwf);
                            dummySwf.clearModified();
                            startWork(AppStrings.translate("work.reading.abc"), this);
                            ABC abc = new ABC(new ABCInputStream(new MemoryInputStream(Helper.readStream(is))), dummySwf, doABC2Tag, file, fileTitle);
                            doABC2Tag.setABC(abc);
                            return abc;
                        }

                        @Override
                        protected ABC doInBackground() throws Exception {
                            return open(stream, null, streamEntry.getKey());
                        }

                        @Override
                        protected void done() {
                            stopWork();
                        }

                    };
                    worker = abcWorker;
                } else {
                    CancellableWorker<SWF> swfWorker = new CancellableWorker<SWF>("bundleSwfWorker") {
                        @Override
                        public SWF doInBackground() throws Exception {
                            final CancellableWorker worker = this;
                            String fileKey = fname + "/" + streamEntry.getKey();
                            SwfSpecificCustomConfiguration conf = Configuration.getSwfSpecificCustomConfiguration(fileKey);

                            String charset = conf == null ? Charset.defaultCharset().name() : conf.getCustomData(CustomConfigurationKeys.KEY_CHARSET, Charset.defaultCharset().name());
                            SWF swf = new SWF(stream, null, streamEntry.getKey(), new ProgressListener() {
                                @Override
                                public void progress(int p) {
                                    startWork(AppStrings.translate("work.reading.swf"), p, worker);
                                }

                                @Override
                                public void status(String status) {
                                    if ("renaming.identifiers".equals(status)) {
                                        startWork(AppStrings.translate("work.renaming.identifiers"), worker);
                                    }
                                }
                            }, Configuration.parallelSpeedUp.get(), charset);
                            return swf;
                        }
                    };
                    worker = swfWorker;
                }
                loadingDialog.setWorker(worker);
                worker.execute();

                try {
                    result.add(worker.get());
                } catch (CancellationException ex) {
                    logger.log(Level.WARNING, "Loading SWF {0} was cancelled.", streamEntry.getKey());
                }
            }
        } else {
            InputStream fInputStream = inputStream;

            CancellableWorker<? extends Openable> worker = null;

            if (sourceInfo.getKind() == OpenableSourceKind.ABC) {
                CancellableWorker<ABC> abcWorker = new CancellableWorker<ABC>("abcWorker") {
                    private ABC open(InputStream is, String file, String fileTitle) throws IOException, InterruptedException {
                        SWF dummySwf = new SWF();
                        dummySwf.setFileTitle(fileTitle != null ? fileTitle : file);
                        FileAttributesTag fileAttributes = new FileAttributesTag(dummySwf);
                        fileAttributes.actionScript3 = true;
                        dummySwf.addTag(fileAttributes);
                        fileAttributes.setTimelined(dummySwf);
                        DoABC2Tag doABC2Tag = new DoABC2Tag(dummySwf);
                        dummySwf.addTag(doABC2Tag);
                        doABC2Tag.setTimelined(dummySwf);
                        dummySwf.clearModified();
                        startWork(AppStrings.translate("work.reading.abc"), this);
                        ABC abc = new ABC(new ABCInputStream(new MemoryInputStream(Helper.readStream(is))), dummySwf, doABC2Tag, file, fileTitle);
                        doABC2Tag.setABC(abc);
                        return abc;
                    }

                    @Override
                    protected ABC doInBackground() throws Exception {
                        return open(fInputStream, sourceInfo.getFile(), sourceInfo.getFileTitle());
                    }

                    @Override
                    protected void done() {
                        stopWork();
                    }

                };
                worker = abcWorker;
            } else if (sourceInfo.getKind() == OpenableSourceKind.SWF) {
                final String[] yesno = new String[]{AppStrings.translate("button.yes"), AppStrings.translate("button.no"), AppStrings.translate("button.yes.all"), AppStrings.translate("button.no.all")};

                CancellableWorker<SWF> swfWorker = new CancellableWorker<SWF>("swfWorker") {
                    private boolean yestoall = false;

                    private boolean notoall = false;

                    private SWF open(InputStream is, String file, String fileTitle) throws IOException, InterruptedException {
                        final CancellableWorker worker = this;
                        String shortName = fileTitle != null ? fileTitle : file;
                        String fileKey = shortName == null ? "" : new File(shortName).getName();
                        SwfSpecificCustomConfiguration conf = Configuration.getSwfSpecificCustomConfiguration(fileKey);
                        String charset = conf == null ? Charset.defaultCharset().name() : conf.getCustomData(CustomConfigurationKeys.KEY_CHARSET, "WINDOWS-1252");
                        List<String> loadedUrls = new ArrayList<>();
                        List<String> loadedStatus = new ArrayList<>();

                        Map<String, String> configuredImportAssets = new HashMap<>();
                        if (conf != null) {
                            String impAssetsStr = conf.getCustomData(CustomConfigurationKeys.KEY_LOADED_IMPORT_ASSETS, "");
                            if (impAssetsStr != null && !impAssetsStr.isEmpty()) {
                                String[] parts = (impAssetsStr + IMPORT_ASSETS_SEPARATOR).split(Pattern.quote(IMPORT_ASSETS_SEPARATOR));
                                for (String s : parts) {
                                    if (!s.isEmpty()) {
                                        String[] urlPlusStatus = (s + "|").split(Pattern.quote("|"));
                                        String url = urlPlusStatus[0];
                                        String status = urlPlusStatus[1];
                                        configuredImportAssets.put(url, status);
                                    }
                                }
                            }
                        }

                        SWF swf = new SWF(is, file, fileTitle, new ProgressListener() {
                            @Override
                            public void progress(int p) {
                                startWork(AppStrings.translate("work.reading.swf"), p, worker);
                            }

                            @Override
                            public void status(String status) {
                                if ("renaming.identifiers".equals(status)) {
                                    startWork(AppStrings.translate("work.renaming.identifiers"), worker);
                                }
                            }
                        }, Configuration.parallelSpeedUp.get(), false, true, new UrlResolver() {
                            @Override
                            public SWF resolveUrl(String file, final String url) {
                                loadedUrls.add(url);
                                File selFile = null;
                                int opt = -1;

                                if (configuredImportAssets.containsKey(url)) {
                                    String statusStr = configuredImportAssets.get(url);
                                    if (statusStr.equals("NO")) {
                                        loadedStatus.add("NO");
                                        return null;
                                    }
                                    if (statusStr.startsWith("CUSTOM:")) {
                                        selFile = new File(statusStr.substring("CUSTOM:".length()));
                                    }
                                } else {
                                    if (!(yestoall || notoall)) {
                                        opt = ViewMessages.showOptionDialog(getDefaultMessagesComponent(), AppStrings.translate("message.imported.swf").replace("%url%", url), AppStrings.translate("message.warning"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, yesno, AppStrings.translate("button.yes"));
                                        if (opt == 2) {
                                            yestoall = true;
                                        }
                                        if (opt == 3) {
                                            notoall = true;
                                        }
                                    }

                                    if (yestoall) {
                                        opt = 0; // yes
                                    } else if (notoall) {
                                        opt = 1; // no
                                    }

                                    if (opt == 1) { //no
                                        loadedStatus.add("NO");
                                        return null;
                                    }
                                }

                                if (selFile == null && (url.startsWith("http://") || url.startsWith("https://"))) {
                                    try {
                                        URL u = URI.create(url).toURL();
                                        SWF ret = open(u.openStream(), null, url); //?
                                        loadedStatus.add("YES");
                                        return ret;
                                    } catch (Exception ex) {
                                        //ignore
                                    }
                                } else {
                                    File swf = selFile != null ? selFile : new File(new File(file).getParentFile(), url);
                                    if (swf.exists()) {
                                        try {
                                            SWF ret = open(new FileInputStream(swf), swf.getAbsolutePath(), swf.getName());
                                            if (selFile != null) {
                                                loadedStatus.add("CUSTOM:" + selFile.getAbsolutePath());
                                            } else {
                                                loadedStatus.add("YES");
                                            }
                                            return ret;
                                        } catch (Exception ex) {
                                            //ignore
                                        }
                                    }
                                    // try .gfx if .swf failed
                                    if (selFile == null && url.endsWith(".swf")) {
                                        File gfx = new File(new File(file).getParentFile(), url.substring(0, url.length() - 4) + ".gfx");
                                        if (gfx.exists()) {
                                            try {
                                                SWF ret = open(new FileInputStream(gfx), gfx.getAbsolutePath(), gfx.getName());
                                                loadedStatus.add("YES");
                                                return ret;
                                            } catch (Exception ex) {
                                                //ignore
                                            }
                                        }
                                    }
                                }
                                Reference<SWF> ret = new Reference<>(null);
                                View.execInEventDispatch(new Runnable() {
                                    @Override
                                    public void run() {

                                        while (JOptionPane.YES_OPTION == ViewMessages.showConfirmDialog(getDefaultMessagesComponent(), AppStrings.translate("message.imported.swf.manually").replace("%url%", url), AppStrings.translate("error"), JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE)) {

                                            JFileChooser fc = new JFileChooser();
                                            fc.setCurrentDirectory(new File(Configuration.lastOpenDir.get()));
                                            FileFilter allSupportedFilter = new FileFilter() {
                                                private final String[] supportedExtensions = new String[]{".swf", ".spl", ".gfx"};

                                                @Override
                                                public boolean accept(File f) {
                                                    String name = f.getName().toLowerCase(Locale.ENGLISH);
                                                    for (String ext : supportedExtensions) {
                                                        if (name.endsWith(ext)) {
                                                            return true;
                                                        }
                                                    }
                                                    return f.isDirectory();
                                                }

                                                @Override
                                                public String getDescription() {
                                                    String exts = Helper.joinStrings(supportedExtensions, "*%s", "; ");
                                                    return AppStrings.translate("filter.supported") + " (" + exts + ")";
                                                }
                                            };
                                            fc.setFileFilter(allSupportedFilter);
                                            FileFilter swfFilter = new FileFilter() {
                                                @Override
                                                public boolean accept(File f) {
                                                    return (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".swf"))
                                                            || (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".spl"))
                                                            || (f.isDirectory());
                                                }

                                                @Override
                                                public String getDescription() {
                                                    return AppStrings.translate("filter.swf_spl");
                                                }
                                            };
                                            fc.addChoosableFileFilter(swfFilter);

                                            FileFilter gfxFilter = new FileFilter() {
                                                @Override
                                                public boolean accept(File f) {
                                                    return (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".gfx")) || (f.isDirectory());
                                                }

                                                @Override
                                                public String getDescription() {
                                                    return AppStrings.translate("filter.gfx");
                                                }
                                            };
                                            fc.addChoosableFileFilter(gfxFilter);
                                            fc.setAcceptAllFileFilterUsed(false);
                                            int returnVal = fc.showOpenDialog(getDefaultMessagesComponent());
                                            if (returnVal == JFileChooser.APPROVE_OPTION) {
                                                Configuration.lastOpenDir.set(Helper.fixDialogFile(fc.getSelectedFile()).getParentFile().getAbsolutePath());
                                                File selFile = Helper.fixDialogFile(fc.getSelectedFile());
                                                try {
                                                    ret.setVal(open(new FileInputStream(selFile), selFile.getAbsolutePath(), selFile.getName()));
                                                    loadedStatus.add("CUSTOM:" + selFile.getAbsolutePath());
                                                    return;
                                                } catch (Exception ex) {
                                                    //ignore;
                                                }
                                            } else {
                                                break;
                                            }
                                        }
                                        loadedStatus.add("NO");
                                    }
                                });
                                return ret.getVal();
                            }
                        }, charset);

                        if (!loadedUrls.isEmpty()) {
                            SwfSpecificCustomConfiguration cc2 = Configuration.getOrCreateSwfSpecificCustomConfiguration(swf.getShortPathTitle());
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < loadedUrls.size(); i++) {
                                if (i > 0) {
                                    sb.append(IMPORT_ASSETS_SEPARATOR);
                                }
                                sb.append(loadedUrls.get(i));
                                sb.append("|");
                                sb.append(loadedStatus.get(i));
                            }
                            cc2.setCustomData(CustomConfigurationKeys.KEY_LOADED_IMPORT_ASSETS, sb.toString());
                        }

                        return swf;
                    }

                    @Override
                    public SWF doInBackground() throws Exception {
                        return open(fInputStream, sourceInfo.getFile(), sourceInfo.getFileTitle());
                    }
                };
                worker = swfWorker;
            }
            if (loadingDialog != null) {
                loadingDialog.setWorker(worker);
            }
            worker.execute();

            try {
                result.add(worker.get());
                worker.free();
                worker = null;
            } catch (CancellationException ex) {
                logger.log(Level.WARNING, "Loading SWF {0} was cancelled.", sourceInfo.getFileTitleOrName());
            }
        }

        if (fis != null) {
            logger.log(Level.INFO, "File loaded in {0} seconds.", (sw.getElapsedMilliseconds() / 1000));
            fis.close();
        } else {
            logger.log(Level.INFO, "Stream loaded in {0} seconds.", (sw.getElapsedMilliseconds() / 1000));
        }

        result.sourceInfo = sourceInfo;

        boolean hasVideoStreams = false;
        //boolean hasEncrypted = false;
        for (Openable openable : result) {

            openable.setOpenableList(result);

            if (openable instanceof SWF) {
                SWF swf = (SWF) openable;
                logger.log(Level.INFO, "");
                logger.log(Level.INFO, "== File information ==");
                logger.log(Level.INFO, "Size: {0}", Helper.formatFileSize(swf.fileSize));
                logger.log(Level.INFO, "Flash version: {0}", swf.version);
                int width = (int) ((swf.displayRect.Xmax - swf.displayRect.Xmin) / SWF.unitDivisor);
                int height = (int) ((swf.displayRect.Ymax - swf.displayRect.Ymin) / SWF.unitDivisor);
                logger.log(Level.INFO, "Width: {0}", width);
                logger.log(Level.INFO, "Height: {0}", height);

                for (Tag t : swf.getTags()) {
                    if (t instanceof DefineVideoStreamTag) {
                        hasVideoStreams = true;
                    }
                }

                /*if (swf.encrypted) {
                    hasEncrypted = true;
                }*/
                swf.addEventListener(new EventListener() {
                    @Override
                    public void handleExportingEvent(String type, int index, int count, Object data) {
                        String text = AppStrings.translate("work.exporting");
                        if (type != null && type.length() > 0) {
                            text += " " + type;
                        }

                        continueWork(text + " " + index + "/" + count + " " + data);
                    }

                    @Override
                    public void handleExportedEvent(String type, int index, int count, Object data) {
                        String text = AppStrings.translate("work.exported");
                        if (type != null && type.length() > 0) {
                            text += " " + type;
                        }

                        continueWork(text + " " + index + "/" + count + " " + data);
                    }

                    @Override
                    public void handleEvent(String event, Object data) {
                        if (event.equals("exporting") || event.equals("exported")) {
                            throw new Error("Event is not supported by this handler.");
                        }
                        if (event.equals("getVariables")) {
                            continueWork(AppStrings.translate("work.gettingvariables") + "..." + (String) data);
                        }
                        if (event.equals("deobfuscate")) {
                            continueWork(AppStrings.translate("work.deobfuscating") + "..." + (String) data);
                        }
                        if (event.equals("deobfuscate_pcode")) {
                            startWork(AppStrings.translate("work.deobfuscating_pcode") + "..." + (String) data, deobfuscatePCodeWorker);
                        }
                        if (event.equals("rename")) {
                            continueWork(AppStrings.translate("work.renaming") + "..." + (String) data);
                        }
                        if (event.equals("importing_as")) {
                            startWork(AppStrings.translate("work.importing_as") + "..." + (String) data, importWorker);
                        }
                    }
                });
            }
        }

        if (hasVideoStreams && !DefineVideoStreamTag.displayAvailable()) {
            View.execInEventDispatchLater(new Runnable() {
                @Override
                public void run() {
                    ViewMessages.showMessageDialog(getDefaultMessagesComponent(), AppStrings.translate("message.video.installvlc").replace("%file%", sourceInfo.getFileTitleOrName()), AppStrings.translate("message.warning"), JOptionPane.WARNING_MESSAGE, Configuration.warningVideoVlc);
                }

            });
        }

        /*if (hasEncrypted) {
            View.execInEventDispatchLater(new Runnable() {
                @Override
                public void run() {
                    ViewMessages.showMessageDialog(getDefaultMessagesComponent(), AppStrings.translate("warning.cannotencrypt").replace("%file%", sourceInfo.getFileTitleOrName()), AppStrings.translate("message.warning"), JOptionPane.WARNING_MESSAGE, Configuration.warningCannotEncrypt);
                }

            });
        }*/
        return result;
    }

    public static void saveFileToExe(SWF swf, ExeExportMode exeExportMode, File tmpFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(tmpFile); BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            switch (exeExportMode) {
                case WRAPPER:
                    InputStream exeStream = View.class.getClassLoader().getResourceAsStream("com/jpexs/helpers/resource/Swf2Exe.bin");
                    Helper.copyStream(exeStream, bos);
                    int width = swf.displayRect.Xmax - swf.displayRect.Xmin;
                    int height = swf.displayRect.Ymax - swf.displayRect.Ymin;
                    bos.write(width & 0xff);
                    bos.write((width >> 8) & 0xff);
                    bos.write((width >> 16) & 0xff);
                    bos.write((width >> 24) & 0xff);
                    bos.write(height & 0xff);
                    bos.write((height >> 8) & 0xff);
                    bos.write((height >> 16) & 0xff);
                    bos.write((height >> 24) & 0xff);
                    bos.write(Configuration.saveAsExeScaleMode.get());
                    break;
                case PROJECTOR_WIN:
                case PROJECTOR_MAC:
                case PROJECTOR_LINUX:
                    File projectorFile = Configuration.getProjectorFile(exeExportMode);
                    if (projectorFile == null) {
                        String message = "Projector not found, please place it to " + Configuration.getProjectorPath();
                        logger.log(Level.SEVERE, message);
                        throw new IOException(message);
                    }
                    Helper.copyStream(new FileInputStream(projectorFile), bos);
                    bos.flush();
                    break;
            }

            long pos = fos.getChannel().position();
            swf.saveTo(bos);

            switch (exeExportMode) {
                case PROJECTOR_WIN:
                case PROJECTOR_MAC:
                case PROJECTOR_LINUX:
                    bos.flush();
                    int swfSize = (int) (fos.getChannel().position() - pos);

                    // write magic number
                    bos.write(0x56);
                    bos.write(0x34);
                    bos.write(0x12);
                    bos.write(0xfa);

                    bos.write(swfSize & 0xff);
                    bos.write((swfSize >> 8) & 0xff);
                    bos.write((swfSize >> 16) & 0xff);
                    bos.write((swfSize >> 24) & 0xff);
            }
        }
    }

    public static void saveFile(Openable openable, String outfile) throws IOException {
        saveFile(openable, outfile, SaveFileMode.SAVE, null);
    }

    public static void saveFile(Openable openable, String outfile, SaveFileMode mode, ExeExportMode exeExportMode) throws IOException {
        File savedFile = new File(outfile);
        startSaving(savedFile);
        if (mode == SaveFileMode.SAVEAS && openable.getOpenableList() != null /*SWF in binarydata has null*/ && !openable.getOpenableList().isBundle()) {
            openable.setFile(outfile);
            OpenableSourceInfo sourceInfo = openable.getOpenableList().sourceInfo;
            sourceInfo.setFile(outfile);
            sourceInfo.setFileTitle(null);
            if (mainFrame != null && mainFrame.getPanel() != null) {
                mainFrame.getPanel().refreshPins();
            }
        }
        File outfileF = new File(outfile);
        File tmpFile = new File(outfile + ".tmp");

        try {
            if (mode == SaveFileMode.EXE) {
                saveFileToExe((SWF) openable, exeExportMode, tmpFile);
            } else {
                if (openable instanceof SWF) {
                    SWF swf = (SWF) openable;
                    swf.saveNestedDefineBinaryData();
                }
                try (FileOutputStream fos = new FileOutputStream(tmpFile); BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                    openable.saveTo(bos);
                }
            }
        } catch (Throwable t) {
            stopSaving(savedFile);
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            throw t;
        }
        if (tmpFile.exists()) {
            if (tmpFile.length() > 0) {
                outfileF.delete();
                if (!tmpFile.renameTo(outfileF)) {
                    tmpFile.delete();
                    throw new IOException("Cannot access " + outfile);
                }
            } else {
                throw new IOException("Output is empty");
            }
        } else {
            throw new IOException("Output not found");
        }
        stopSaving(savedFile);
    }

    private static class OpenFileWorker extends SwingWorker {

        private final OpenableSourceInfo[] sourceInfos;

        private final OpenableOpened executeAfterOpen;

        private final int[] reloadIndices;

        public OpenFileWorker(OpenableSourceInfo sourceInfo) {
            this(sourceInfo, -1);
        }

        public OpenFileWorker(OpenableSourceInfo sourceInfo, int reloadIndex) {
            this(sourceInfo, null, reloadIndex);
        }

        public OpenFileWorker(OpenableSourceInfo sourceInfo, OpenableOpened executeAfterOpen) {
            this(sourceInfo, executeAfterOpen, -1);
        }

        public OpenFileWorker(OpenableSourceInfo sourceInfo, OpenableOpened executeAfterOpen, int reloadIndex) {
            this.sourceInfos = new OpenableSourceInfo[]{sourceInfo};
            this.executeAfterOpen = executeAfterOpen;
            this.reloadIndices = new int[]{reloadIndex};
        }

        public OpenFileWorker(OpenableSourceInfo[] sourceInfos) {
            this(sourceInfos, null, null);
        }

        public OpenFileWorker(OpenableSourceInfo[] sourceInfos, OpenableOpened executeAfterOpen) {
            this(sourceInfos, executeAfterOpen, null);
        }

        public OpenFileWorker(OpenableSourceInfo[] sourceInfos, OpenableOpened executeAfterOpen, int[] reloadIndices) {
            this.sourceInfos = sourceInfos;
            this.executeAfterOpen = executeAfterOpen;
            int[] indices = new int[sourceInfos.length];
            for (int i = 0; i < indices.length; i++) {
                indices[i] = -1;
            }
            this.reloadIndices = reloadIndices == null ? indices : reloadIndices;
        }

        @Override
        protected Object doInBackground() throws Exception {
            boolean first = true;
            SWF firstSWF = null;
            Openable firstOpenable = null;
            List<OpenableList> openableLists = new ArrayList<>();
            for (int index = 0; index < sourceInfos.length; index++) {
                OpenableSourceInfo sourceInfo = sourceInfos[index];
                OpenableList openables = null;
                try {
                    Main.startWork(AppStrings.translate("work.reading.swf") + "...", null);
                    try {
                        openables = parseOpenable(sourceInfo);
                    } catch (ExecutionException ex) {
                        Throwable cause = ex.getCause();
                        if (cause instanceof SwfOpenException) {
                            throw (SwfOpenException) cause;
                        }
                        if (cause instanceof ABCOpenException) {
                            throw (ABCOpenException) cause;
                        }

                        throw ex;
                    }
                } catch (OutOfMemoryError ex) {
                    logger.log(Level.SEVERE, null, ex);
                    handleOutOfMemory("Cannot load SWF file.");
                    continue;
                } catch (ABCOpenException | SwfOpenException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    ViewMessages.showMessageDialog(getDefaultMessagesComponent(), ex.getMessage(), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                    continue;
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                    ViewMessages.showMessageDialog(getDefaultMessagesComponent(), "Cannot load SWF file: " + ex.getLocalizedMessage(), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                openableLists.add(openables);
                final OpenableList openables1 = openables;
                final boolean first1 = first;
                first = false;
                if (firstOpenable == null && openables1.size() > 0) {
                    firstOpenable = openables1.get(0);
                }
                if (firstSWF == null && openables1.size() > 0 && (openables1.get(0) instanceof SWF)) {
                    firstSWF = (SWF) openables1.get(0);
                }

                final int findex = index;
                try {
                    View.execInEventDispatch(() -> {
                        Main.startWork(AppStrings.translate("work.creatingwindow") + "...", null);
                        ensureMainFrame();
                        if (openables1.isEmpty()) {
                            return;
                        }
                        if (reloadIndices[findex] > -1) {
                            mainFrame.getPanel().loadSwfAtPos(openables1, reloadIndices[findex]);
                        } else {
                            mainFrame.getPanel().load(openables1, first1);
                        }
                    });
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }

            if (mainFrame != null) {
                for (OpenableList openableList : openableLists) {
                    for (Openable openable : openableList) {
                        if (openable instanceof SWF) {
                            SWF swf = (SWF) openable;
                            SwfSpecificCustomConfiguration conf = Configuration.getSwfSpecificCustomConfiguration(swf.getShortPathTitle());
                            if (conf != null) {
                                List<String> preselectedNames = conf.getCustomDataAsList(CustomConfigurationKeys.KEY_ABC_DEPENDENCIES);
                                if (!preselectedNames.isEmpty()) {
                                    swf.setAbcIndexDependencies(namesToSwfs(preselectedNames));
                                    if (mainFrame != null && mainFrame.getPanel() != null && mainFrame.getPanel().getABCPanel() != null) {
                                        mainFrame.getPanel().getABCPanel().updateLinksLabel();
                                    }
                                }
                            }
                        }
                    }
                }
            }

            final SWF fswf = firstSWF;
            final Openable fopenable = firstOpenable;
            View.execInEventDispatch(() -> {
                if (mainFrame == null) {
                    Main.startWork(AppStrings.translate("work.creatingwindow") + "...", null);
                    ensureMainFrame();
                }
                loadingDialog.setVisible(false);

                if (mainFrame != null) {
                    mainFrame.setVisible(true);
                }

                Main.stopWork();

                if (mainFrame != null && Configuration.gotoMainClassOnStartup.get() && fswf != null) {
                    mainFrame.getPanel().gotoDocumentClass(fswf);
                }

                if (mainFrame != null && fopenable != null) {
                    SwfSpecificConfiguration swfConf = Configuration.getSwfSpecificConfiguration(fopenable.getShortPathTitle());
                    String resourcesPathStr = null;
                    String tagListPathStr = null;
                    if (swfConf != null) {
                        resourcesPathStr = swfConf.lastSelectedPath;
                    }
                    SwfSpecificCustomConfiguration swfCustomConf = Configuration.getSwfSpecificCustomConfiguration(fopenable.getShortPathTitle());
                    if (swfCustomConf != null) {
                        resourcesPathStr = swfCustomConf.getCustomData(CustomConfigurationKeys.KEY_LAST_SELECTED_PATH_RESOURCES, resourcesPathStr);
                        tagListPathStr = swfCustomConf.getCustomData(CustomConfigurationKeys.KEY_LAST_SELECTED_PATH_TAGLIST, null);
                        List<String> breakpointsList = swfCustomConf.getCustomDataAsList(CustomConfigurationKeys.KEY_BREAKPOINTS);
                        for (String breakpoint : breakpointsList) {
                            if (breakpoint.matches("^.*:[0-9]+$")) {
                                int line = Integer.parseInt(breakpoint.substring(breakpoint.lastIndexOf(":") + 1));
                                String scriptName = breakpoint.substring(0, breakpoint.lastIndexOf(":"));
                                getDebugHandler().addBreakPoint(fswf, scriptName, line);
                            }
                        }
                    }

                    if (isInited()) {
                        if (resourcesPathStr == null) {
                            TagTreeModel model = mainFrame.getPanel().tagTree.getFullModel();
                            TreePath tp = model == null ? null : model.getTreePath(fopenable);
                            if (tp != null) {
                                mainFrame.getPanel().tagTree.setSelectionPath(tp);
                            }
                        } else {
                            mainFrame.getPanel().tagTree.setSelectionPathString(resourcesPathStr);
                        }
                        if (tagListPathStr == null) {
                            TagListTreeModel model = mainFrame.getPanel().tagListTree.getFullModel();
                            TreePath tp = model == null ? null : model.getTreePath(fopenable);
                            if (tp != null) {
                                mainFrame.getPanel().tagListTree.setSelectionPath(tp);
                            }
                        } else {
                            mainFrame.getPanel().tagListTree.setSelectionPathString(tagListPathStr);                        
                        }
                    } else {
                        mainFrame.getPanel().tagTree.setExpandPathString(resourcesPathStr);
                        mainFrame.getPanel().tagListTree.setExpandPathString(tagListPathStr);
                    }
                    mainFrame.getPanel().updateMissingNeededCharacters();
                    if (fswf != null) {
                        mainFrame.getPanel().easyPanel.setTimelined(fswf);
                    }
                }

                if (executeAfterOpen != null && fopenable != null) {
                    executeAfterOpen.opened(fopenable);
                }
            });

            return true;
        }
    }

    public static boolean reloadSWFs() {
        CancellableWorker.cancelBackgroundThreads();
        if (Main.sourceInfos.isEmpty()) {
            Helper.freeMem();
            showModeFrame();
            return true;
        } else {
            for (int i = sourceInfos.size() - 1; i >= 0; i--) {
                if (sourceInfos.get(i).isEmpty()) {
                    sourceInfos.remove(i);
                }
            }
            OpenableSourceInfo[] sourceInfosCopy = new OpenableSourceInfo[sourceInfos.size()];
            sourceInfos.toArray(sourceInfosCopy);
            sourceInfos.clear();
            openFile(sourceInfosCopy);
            return true;
        }
    }

    public static void reloadApp() {
        if (debugDialog != null) {
            debugDialog.setVisible(false);
            debugDialog.dispose();
            debugDialog = null;
        }
        if (loadingDialog != null) {
            synchronized (Main.class) {
                if (loadingDialog != null) {
                    loadingDialog.setVisible(false);
                    loadingDialog.dispose();
                    loadingDialog = null;
                }
            }
        }
        if (loadFromMemoryFrame != null) {
            loadFromMemoryFrame.setVisible(false);
            loadFromMemoryFrame.dispose();
            loadFromMemoryFrame = null;
        }
        if (mainFrame != null) {
            mainFrame.setVisible(false);
            mainFrame.getPanel().closeAll(false, false);
            mainFrame.dispose();
            mainFrame = null;
        }
        FontTag.reload();
        Cache.clearAll();
        initGui();
        reloadSWFs();
    }

    public static void newFile() {
        View.checkAccess();

        NewFileDialog newFileDialog = new NewFileDialog(getDefaultDialogsOwner());
        if (newFileDialog.showDialog() == AppDialog.OK_OPTION) {
            if (mainFrame != null && !Configuration.openMultipleFiles.get()) {
                sourceInfos.clear();
                mainFrame.getPanel().closeAll(false, false);
                mainFrame.setVisible(false);
                Helper.freeMem();
            }
            String ext = newFileDialog.isGfx() ? "gfx" : "swf";
            String fileTitle = AppStrings.translate("new.filename") + "." + ext;
            OpenableSourceInfo sourceInfo = new OpenableSourceInfo(fileTitle);
            sourceInfos.add(sourceInfo);
            OpenableList list = new OpenableList();
            list.sourceInfo = sourceInfo;
            SWF swf = new SWF();
            swf.setFile(null);
            swf.setFileTitle(fileTitle);

            swf.displayRect = new RECT(
                    newFileDialog.getXMin(),
                    newFileDialog.getXMax(),
                    newFileDialog.getYMin(),
                    newFileDialog.getYmax()
            );
            swf.compression = newFileDialog.getCompression();
            swf.version = newFileDialog.getVersionNumber();
            swf.frameRate = newFileDialog.getFrameRate();
            swf.gfx = newFileDialog.isGfx();
            swf.setHeaderModified(true);
            FileAttributesTag f = new FileAttributesTag(swf);
            if (newFileDialog.isAs3()) {
                f.actionScript3 = true;
            }
            Tag t = f;
            t.setTimelined(swf);
            swf.addTag(t);
            t = new SetBackgroundColorTag(swf, new RGB(newFileDialog.getBackgroundColor()));
            t.setTimelined(swf);
            swf.addTag(t);
            t = new ShowFrameTag(swf);
            t.setTimelined(swf);
            swf.addTag(t);
            swf.frameCount = 1;
            swf.hasEndTag = true;
            list.add(swf);
            swf.openableList = list;
            mainFrame.getPanel().load(list, true);

            //select first frame
            mainFrame.getPanel().setTagTreeSelectedNode(mainFrame.getPanel().getCurrentTree(), swf.getTimeline().getFrame(0));
        }
    }

    public static OpenFileResult openFile(String swfFile, String fileTitle) {
        View.checkAccess();

        return openFile(swfFile, fileTitle, null);
    }

    public static OpenFileResult openFile(String swfFile, String fileTitle, Runnable executeAfterOpen) {
        View.checkAccess();

        try {
            File file = new File(swfFile);
            if (!file.exists()) {
                ViewMessages.showMessageDialog(getDefaultMessagesComponent(), AppStrings.translate("open.error.fileNotFound"), AppStrings.translate("open.error"), JOptionPane.ERROR_MESSAGE);
                return OpenFileResult.NOT_FOUND;
            }
            swfFile = file.getCanonicalPath();
            OpenableSourceInfo sourceInfo = new OpenableSourceInfo(null, swfFile, fileTitle);
            OpenFileResult openResult = openFile(sourceInfo);
            return openResult;
        } catch (IOException ex) {
            ViewMessages.showMessageDialog(getDefaultMessagesComponent(), AppStrings.translate("open.error.cannotOpen"), AppStrings.translate("open.error"), JOptionPane.ERROR_MESSAGE);
            return OpenFileResult.ERROR;
        }
    }

    public static OpenFileResult openFile(OpenableSourceInfo sourceInfo) {
        View.checkAccess();

        return openFile(new OpenableSourceInfo[]{sourceInfo});
    }

    public static OpenFileResult openFile(OpenableSourceInfo sourceInfo, OpenableOpened executeAfterOpen) {
        View.checkAccess();

        return openFile(new OpenableSourceInfo[]{sourceInfo}, executeAfterOpen);
    }

    public static OpenFileResult openFile(OpenableSourceInfo sourceInfo, OpenableOpened executeAfterOpen, int reloadIndex) {
        View.checkAccess();

        return openFile(new OpenableSourceInfo[]{sourceInfo}, executeAfterOpen, new int[]{reloadIndex});
    }

    public static OpenFileResult openFile(OpenableSourceInfo[] newSourceInfos) {
        View.checkAccess();

        return openFile(newSourceInfos, null);
    }

    public static OpenFileResult openFile(OpenableSourceInfo[] newSourceInfos, OpenableOpened executeAfterOpen) {
        View.checkAccess();

        return openFile(newSourceInfos, executeAfterOpen, null);
    }

    public static OpenFileResult openFile(OpenableSourceInfo[] newSourceInfos, OpenableOpened executeAfterOpen, int[] reloadIndices) {
        View.checkAccess();

        if (mainFrame != null && !Configuration.openMultipleFiles.get()) {
            sourceInfos.clear();
            mainFrame.getPanel().closeAll(false, false);
            mainFrame.setVisible(false);
            Helper.freeMem();
            reloadIndices = null;
        }

        loadingDialog.setVisible(true);

        for (int i = 0; i < newSourceInfos.length; i++) {
            OpenableSourceInfo si = newSourceInfos[i];
            String fileName = si.getFile();
            if (fileName != null) {
                Configuration.addRecentFile(fileName);
                SharedObjectsStorage.addChangedListener(new File(fileName), new CookiesChangedListener() {

                    Timer timer;

                    @Override
                    public void cookiesChanged(File swfFile, List<File> cookies) {
                        if (timer != null) {
                            return;
                        }

                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                View.execInEventDispatchLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        getMainFrame().getPanel().refreshTree();
                                        timer = null;
                                    }
                                });

                            }
                        }, 500);

                    }
                });
                if (watcher != null && Configuration.checkForModifications.get()) {
                    try {
                        File dir = new File(fileName).getParentFile();
                        if (dir == null) {
                            continue;
                        }
                        if (!watchedDirectories.containsValue(dir)) {
                            WatchKey key = dir.toPath().register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
                            watchedDirectories.put(key, dir);
                        }
                    } catch (IOException ex) {
                        //ignore
                    }
                }
            }
        }

        OpenFileWorker wrk = new OpenFileWorker(newSourceInfos, executeAfterOpen, reloadIndices);
        wrk.execute();
        if (reloadIndices == null) {
            sourceInfos.addAll(Arrays.asList(newSourceInfos));
        } else {
            for (int i = 0; i < reloadIndices.length; i++) {
                sourceInfos.set(reloadIndices[i], newSourceInfos[i]);
            }
        }
        return OpenFileResult.OK;
    }

    public static void closeFile(OpenableList openableList) {
        View.checkAccess();

        sourceInfos.remove(openableList.sourceInfo);
        mainFrame.getPanel().close(openableList);
    }

    public static void reloadFile(OpenableList swf) {
        View.checkAccess();

        for (Openable o : swf.items) {
            SwfSpecificCustomConfiguration cc = Configuration.getSwfSpecificCustomConfiguration(o.getShortPathTitle());
            if (cc != null) {
                cc.setCustomData(CustomConfigurationKeys.KEY_LOADED_IMPORT_ASSETS, "");
            }
        }

        if (mainFrame != null) {
            for (Openable o : swf.items) {
                mainFrame.getPanel().closeAbcExplorer(o);
            }
        }

        openFile(swf.sourceInfo, null, sourceInfos.indexOf(swf.sourceInfo));
    }

    public static void reloadFile(File file) {
        for (int i = 0; i < sourceInfos.size(); i++) {
            OpenableSourceInfo info = sourceInfos.get(i);
            if (info.getFile() == null) {
                continue;
            }
            if (file.equals(new File(info.getFile()))) {
                openFile(info, null, i);
            }
        }
    }

    public static boolean closeAll(boolean onExit) {
        View.checkAccess();

        boolean closeResult = mainFrame.getPanel().closeAll(true, onExit);
        if (closeResult) {
            sourceInfos.clear();
            System.gc();
        }

        if (filesChangedDialog != null) {
            filesChangedDialog.setVisible(false);
        }

        return closeResult;
    }

    public static boolean saveFileDialog(Openable openable, final SaveFileMode mode) {

        String ext = ".swf";
        switch (mode) {
            case SAVE:
            case SAVEAS:
                if (openable.getFile() != null) {
                    ext = Path.getExtension(openable.getFile());
                }
                break;
            case EXE:
                ext = ".exe";
                break;
        }

        String icon = "save";
        if (mode == SaveFileMode.SAVEAS) {
            icon = "saveas";
        }
        if (mode == SaveFileMode.EXE) {
            icon = "saveasexe";
        }
        JFileChooser fc = View.getFileChooserWithIcon(icon);
        fc.setCurrentDirectory(new File(Configuration.lastSaveDir.get()));

        FileFilter swfFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".swf"))
                        || (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".spl"))
                        || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return AppStrings.translate("filter.swf_spl");
            }
        };

        FileFilter gfxFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".gfx")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return AppStrings.translate("filter.gfx");
            }
        };

        FileFilter abcFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".abc")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return AppStrings.translate("filter.abc");
            }
        };

        ExeExportMode exeExportMode = null;
        FileFilter wrapperFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".exe")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return AppStrings.translate("filter.exe.wrapper");
            }
        };

        FileFilter projectorWinFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".exe")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return AppStrings.translate("filter.exe.projectorWin");
            }
        };

        FileFilter projectorMacFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".dmg")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return AppStrings.translate("filter.exe.projectorMac");
            }
        };

        FileFilter projectorLinuxFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return true;
            }

            @Override
            public String getDescription() {
                return AppStrings.translate("filter.exe.projectorLinux");
            }
        };
        if (mode == SaveFileMode.EXE) {
            exeExportMode = Configuration.exeExportMode.get();
            if (exeExportMode == null) {
                exeExportMode = ExeExportMode.WRAPPER;
            }
            fc.addChoosableFileFilter(wrapperFilter);
            fc.addChoosableFileFilter(projectorWinFilter);
            fc.addChoosableFileFilter(projectorMacFilter);
            fc.addChoosableFileFilter(projectorLinuxFilter);

            switch (exeExportMode) {
                case WRAPPER:
                    fc.setFileFilter(wrapperFilter);
                    break;
                case PROJECTOR_WIN:
                    fc.setFileFilter(projectorWinFilter);
                    break;
                case PROJECTOR_MAC:
                    fc.setFileFilter(projectorMacFilter);
                    break;
                case PROJECTOR_LINUX:
                    // linux projector is compressed with tar.gz
                    // todo: decompress
                    fc.setFileFilter(projectorLinuxFilter);
                    break;
            }
        } else if ((openable instanceof SWF) && ((SWF) openable).gfx) {
            fc.addChoosableFileFilter(swfFilter);
            fc.setFileFilter(gfxFilter);
        } else if (openable instanceof SWF) {
            fc.setFileFilter(swfFilter);
            fc.addChoosableFileFilter(gfxFilter);
        } else if (openable instanceof ABC) {
            fc.setFileFilter(abcFilter);
        }
        final String extension = ext;
        fc.setAcceptAllFileFilterUsed(false);
        if (fc.showSaveDialog(getDefaultMessagesComponent()) == JFileChooser.APPROVE_OPTION) {
            File file = Helper.fixDialogFile(fc.getSelectedFile());
            FileFilter selFilter = fc.getFileFilter();
            try {
                String fileName = file.getAbsolutePath();
                if (selFilter == swfFilter) {
                    if (!fileName.toLowerCase(Locale.ENGLISH).endsWith(extension)
                            && !fileName.toLowerCase(Locale.ENGLISH).endsWith(".spl")) {
                        fileName += extension;
                    }
                    ((SWF) openable).gfx = false;
                }
                if (selFilter == gfxFilter) {
                    if (!fileName.toLowerCase(Locale.ENGLISH).endsWith(".gfx")) {
                        fileName += ".gfx";
                    }
                    ((SWF) openable).gfx = true;
                }

                if (selFilter == abcFilter) {
                    if (!fileName.toLowerCase(Locale.ENGLISH).endsWith(".abc")) {
                        fileName += ".abc";
                    }
                }
                if (selFilter == wrapperFilter) {
                    if (!fileName.toLowerCase(Locale.ENGLISH).endsWith(".exe")) {
                        fileName += ".exe";
                    }
                    exeExportMode = ExeExportMode.WRAPPER;
                }
                if (selFilter == projectorWinFilter) {
                    if (!fileName.toLowerCase(Locale.ENGLISH).endsWith(".exe")) {
                        fileName += ".exe";
                    }
                    exeExportMode = ExeExportMode.PROJECTOR_WIN;
                }
                if (selFilter == projectorMacFilter) {
                    if (!fileName.toLowerCase(Locale.ENGLISH).endsWith(".dmg")) {
                        fileName += ".dmg";
                    }
                    exeExportMode = ExeExportMode.PROJECTOR_MAC;
                }
                if (selFilter == projectorLinuxFilter) {
                    exeExportMode = ExeExportMode.PROJECTOR_LINUX;
                }
                Main.saveFile(openable, fileName, mode, exeExportMode);
                if (mode == SaveFileMode.EXE && exeExportMode != null) {
                    Configuration.exeExportMode.set(exeExportMode);
                }
                Configuration.lastSaveDir.set(file.getParentFile().getAbsolutePath());
                return true;
            } catch (Exception | OutOfMemoryError | StackOverflowError ex) {
                handleSaveError(ex);
            }
        }
        return false;
    }

    public static void handleOutOfMemory(String actionMessage) {
        String errorMessage = actionMessage;
        if (!errorMessage.isEmpty()) {
            errorMessage += " ";
        }
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        long heapMaxSizeMb = heapMaxSize / 1024 / 1024;
        String currentMaxHeap = "" + heapMaxSizeMb + "m";
        errorMessage += AppStrings.translate("error.outOfMemory").replace("%maxheap%", currentMaxHeap);
        errorMessage += "\n";
        if (Platform.isWindows()) {
            errorMessage += AppStrings.translate("error.outOfMemory.windows");
        } else {
            errorMessage += AppStrings.translate("error.outOfMemory.unixmac");
        }
        errorMessage += "\n";
        if (Helper.is64BitOs() && !Helper.is64BitJre()) {
            errorMessage += AppStrings.translate("error.outOfMemory.32BitJreOn64bitOs");
            errorMessage += "\n";
        }

        errorMessage += AppStrings.translate("error.outOfMemory.64bit");
        ViewMessages.showMessageDialog(getDefaultMessagesComponent(), errorMessage, AppStrings.translate("error.outOfMemory.title"), JOptionPane.ERROR_MESSAGE);
    }

    public static void handleSaveError(Throwable ex) {
        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Error saving file", ex);
        if (ex instanceof OutOfMemoryError) {
            String errorMessage = AppStrings.translate("error.file.save") + ".";
            handleOutOfMemory(errorMessage);
        } else {
            ViewMessages.showMessageDialog(getDefaultMessagesComponent(), AppStrings.translate("error.file.save") + ": " + ex.getLocalizedMessage(), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public static boolean openFileDialog() {
        View.checkAccess();

        JFileChooser fc = View.getFileChooserWithIcon("open");
        if (Configuration.openMultipleFiles.get()) {
            fc.setMultiSelectionEnabled(true);
        }
        fc.setCurrentDirectory(new File(Configuration.lastOpenDir.get()));
        FileFilter allSupportedFilter = new FileFilter() {
            private final String[] supportedExtensions = new String[]{".swf", ".spl", ".gfx", ".swc", ".zip", ".iggy", ".abc"};

            @Override
            public boolean accept(File f) {
                String name = f.getName().toLowerCase(Locale.ENGLISH);
                for (String ext : supportedExtensions) {
                    if (name.endsWith(ext)) {
                        return true;
                    }
                }
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                String exts = Helper.joinStrings(supportedExtensions, "*%s", "; ");
                return AppStrings.translate("filter.supported") + " (" + exts + ")";
            }
        };
        fc.setFileFilter(allSupportedFilter);
        FileFilter swfFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".swf"))
                        || (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".spl"))
                        || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return AppStrings.translate("filter.swf_spl");
            }
        };
        fc.addChoosableFileFilter(swfFilter);

        FileFilter swcFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".swc")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return AppStrings.translate("filter.swc");
            }
        };
        fc.addChoosableFileFilter(swcFilter);

        FileFilter gfxFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".gfx")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return AppStrings.translate("filter.gfx");
            }
        };
        fc.addChoosableFileFilter(gfxFilter);

        FileFilter iggyFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".iggy")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return AppStrings.translate("filter.iggy");
            }
        };
        fc.addChoosableFileFilter(iggyFilter);

        FileFilter abcFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".abc")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return AppStrings.translate("filter.abc");
            }
        };
        fc.addChoosableFileFilter(abcFilter);

        FileFilter zipFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".zip")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return AppStrings.translate("filter.zip");
            }
        };
        fc.addChoosableFileFilter(zipFilter);

        FileFilter binaryFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return true;
            }

            @Override
            public String getDescription() {
                return AppStrings.translate("filter.binary");
            }
        };
        fc.addChoosableFileFilter(binaryFilter);

        fc.setAcceptAllFileFilterUsed(false);
        int returnVal = fc.showOpenDialog(getDefaultMessagesComponent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            Configuration.lastOpenDir.set(Helper.fixDialogFile(fc.getSelectedFile()).getParentFile().getAbsolutePath());
            File[] selFiles = fc.getSelectedFiles();
            for (File file : selFiles) {
                File selfile = Helper.fixDialogFile(file);
                Main.openFile(selfile.getAbsolutePath(), null);
            }
            return true;
        } else {
            return false;
        }
    }

    public static void displayErrorFrame() {
        ErrorLogFrame.getInstance().setVisible(true);
    }

    private static String md5(byte[] data) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            //ignore
        }
        return null;
    }

    private static void initGui() {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Error: Your system does not support Graphic User Interface");
            exit();
        }

        System.setProperty("sun.java2d.d3d", "false");
        System.setProperty("sun.java2d.noddraw", "true");

        if (System.getProperty("sun.java2d.uiScale") == null) { //it was not set by commandline, etc.       
            if (!Configuration.uiScale.hasValue()) {
                GraphicsConfiguration configuration = View.getMainDefaultScreenDevice().getDefaultConfiguration();

                AffineTransform transform = configuration.getDefaultTransform();
                if (transform != null) {
                    Configuration.uiScale.set(transform.getScaleX());
                }
            }
            System.setProperty("sun.java2d.uiScale", "" + Configuration.uiScale.get());
        }

        if (Configuration.hwAcceleratedGraphics.get()) {
            System.setProperty("sun.java2d.opengl", Configuration._debugMode.get() ? "True" : "true");
        } else {
            System.setProperty("sun.java2d.opengl", "false");
        }

        initUiLang();

        initLookAndFeel();
                
        View.execInEventDispatch(() -> {
            ErrorLogFrame.createNewInstance();

            autoCheckForUpdates();
            offerAssociation();
            loadingDialog = new LoadingDialog(getDefaultDialogsOwner());

            try {
                watcher = FileSystems.getDefault().newWatchService();
            } catch (IOException ex) {
                //ignore
            }

            if (watcher != null) {
                watcherWorker = new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        while (true) {
                            WatchKey key;
                            try {
                                key = watcher.take();
                            } catch (InterruptedException ex) {
                                return null;
                            }

                            for (WatchEvent<?> event : key.pollEvents()) {
                                WatchEvent.Kind<?> kind = event.kind();
                                if (kind == StandardWatchEventKinds.OVERFLOW) {
                                    continue;
                                }

                                @SuppressWarnings("unchecked")
                                WatchEvent<java.nio.file.Path> ev = (WatchEvent<java.nio.file.Path>) event;

                                java.nio.file.Path filename = ev.context();

                                if (SharedObjectsStorage.watchedCookieDirectories.containsKey(key)) {
                                    File dir = SharedObjectsStorage.watchedCookieDirectories.get(key);
                                    java.nio.file.Path child = dir.toPath().resolve(filename);
                                    File fullPath = child.toFile();

                                    View.execInEventDispatchLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            SharedObjectsStorage.watchedDirectoryChanged(fullPath);
                                        }
                                    });
                                }

                                if (Configuration.checkForModifications.get() && watchedDirectories.containsKey(key)) {
                                    File dir = watchedDirectories.get(key);
                                    java.nio.file.Path child = dir.toPath().resolve(filename);
                                    File fullPath = child.toFile();
                                    if (savedFiles.contains(fullPath)) {
                                        continue;
                                    }

                                    for (OpenableSourceInfo info : sourceInfos) {
                                        final String infoFile = info.getFile();
                                        if (infoFile != null && new File(infoFile).equals(fullPath)) {
                                            for (OpenableList list : Main.getMainFrame().getPanel().getSwfs()) {
                                                if (info == list.sourceInfo) {
                                                    list.setModified();
                                                }
                                            }

                                            View.execInEventDispatchLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mainFrame.getPanel().refreshTree();
                                                    if (filesChangedDialog == null) {
                                                        filesChangedDialog = new FilesChangedDialog(Main.mainFrame.getWindow());
                                                    }
                                                    filesChangedDialog.addItem(infoFile);
                                                    if (!filesChangedDialog.isVisible()) {
                                                        filesChangedDialog.setVisible(true);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }

                            }
                            boolean valid = key.reset();
                            if (!valid) {
                                SharedObjectsStorage.watchedCookieDirectories.remove(key);
                                watchedDirectories.remove(key);
                            }
                        }
                    }
                };
                watcherWorker.execute();
            }

            DebuggerTools.initDebugger().addMessageListener(new DebugAdapter() {

                @Override
                public boolean isModifyBytesSupported() {
                    return true;
                }

                @Override
                public void onLoaderModifyBytes(String clientId, byte[] inputData, String url, DebugLoaderDataModified modifiedListener) {
                    if (inputData.length < 3) {
                        modifiedListener.dataModified(inputData);
                        return;
                    }
                    String signature = new String(inputData, 0, 3, Utf8Helper.charset);
                    if (!SWF.swfSignatures.contains(signature)) {
                        modifiedListener.dataModified(inputData);
                        return;
                    }
                    final String hash = md5(inputData);
                    OpenableOpened afterLoad = new OpenableOpened() {
                        @Override
                        public void opened(Openable openable) {
                            try {
                                SWF mainSWF = getRunningSWF();
                                File tempRunDir = mainSWF.getFile() == null ? null : new File(mainSWF.getFile()).getParentFile();
                                File tempFile = createTempFileInDir(tempRunDir, "~ffdec_loader_", ".swf");
                                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                                    fos.write(inputData);
                                }
                                Logger.getLogger(Main.class.getName()).log(Level.FINE, "preparing for load: {0}", hash);
                                prepareSwf("loaded_" + hash, runningPreparation, tempFile, mainSWF.getFile() == null ? null : new File(mainSWF.getFile()), tempRunDir, runTempFiles);
                                byte[] outputData = Helper.readFileEx(tempFile.getAbsolutePath());
                                tempFile.delete();
                                Logger.getLogger(Main.class.getName()).log(Level.FINE, "calling datamodified");
                                modifiedListener.dataModified(outputData);
                            } catch (IOException ex) {
                                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            Logger.getLogger(Main.class.getName()).log(Level.FINE, "finished opened method");
                        }
                    };

                    for (OpenableList sl : Main.getMainFrame().getPanel().getSwfs()) {
                        for (int s = 0; s < sl.size(); s++) {
                            Openable op = sl.get(s);
                            String t = op.getTitleOrShortFileName();
                            if (t == null) {
                                t = "";
                            }
                            if (t.endsWith(":" + hash)) { //this one is already opened
                                afterLoad.opened(op);
                                return;
                            }
                        }
                    }
                    SWF swf = Main.getRunningSWF();

                    String title = swf == null ? "?" : swf.getTitleOrShortFileName();
                    final String titleWithHash = title + ":" + hash;
                    View.execInEventDispatch(new Runnable() {
                        @Override
                        public void run() {
                            OpenableSourceInfo osi = new OpenableSourceInfo(new ReReadableInputStream(new ByteArrayInputStream(inputData)), null, titleWithHash);
                            openFile(osi, afterLoad);
                        }
                    });
                }
            });

            try {
                flashDebugger = new Debugger();
                debugHandler = new DebuggerHandler();
                debugHandler.addBreakListener(new DebuggerHandler.BreakListener() {
                    @Override
                    public void doContinue() {
                        mainFrame.getPanel().clearDebuggerColors();
                    }

                    @Override
                    public void breakAt(String scriptName, int line, final int classIndex, final int traitIndex, final int methodIndex) {
                        View.execInEventDispatch(new Runnable() {
                            @Override
                            public void run() {
                                String hash = "main";
                                String scriptNameNoHash = scriptName;
                                if (scriptName.contains(":")) {
                                    hash = scriptName.substring(0, scriptName.indexOf(":"));
                                    scriptNameNoHash = scriptName.substring(scriptName.indexOf(":") + 1);
                                }
                                SWF swf = Main.getSwfByHash(hash);
                                mainFrame.getPanel().gotoScriptLine(swf, scriptNameNoHash, line, classIndex, traitIndex, methodIndex, Main.isDebugPCode());
                            }
                        });
                    }
                });
                debugHandler.addConnectionListener(new DebuggerHandler.ConnectionListener() {
                    @Override
                    public void connected() {
                        Main.mainFrame.getMenu().updateComponents();
                    }

                    @Override
                    public void disconnected() {
                        if (Main.mainFrame != null && Main.mainFrame.getPanel() != null) {
                            Main.mainFrame.getPanel().refreshBreakPoints();
                        }
                    }
                });
                flashDebugger.addConnectionListener(debugHandler);

                searchResultsStorage.load();
            } catch (IOException ex) {
                //ignore
            }

        });
    }

    public static void startDebugger() {
        flashDebugger.startDebugger();
    }

    public static void stopDebugger() {
        flashDebugger.stopDebugger();
    }

    public static void showModeFrame() {
        ensureMainFrame();
        mainFrame.setVisible(true);
    }

    private static void offerAssociation() {
        boolean offered = Configuration.offeredAssociation.get();
        if (!offered) {
            if (Platform.isWindows()) {
                if ((!ContextMenuTools.isAddedToContextMenu()) && ViewMessages.showConfirmDialog(getDefaultMessagesComponent(), "Do you want to add FFDec to context menu of SWF files?\n(Can be changed later from main menu)", "Context menu", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    ContextMenuTools.addToContextMenu(true, false);
                }
            }

            Configuration.offeredAssociation.set(true);
        }
    }

    public static void initUiLang() {
        if (GraphicsEnvironment.isHeadless()) { //No GUI in OS
            return;
        }
        try {
            Class<?> cl = Class.forName("org.pushingpixels.substance.api.SubstanceLookAndFeel");
            Field field = cl.getDeclaredField("LABEL_BUNDLE");
            field.setAccessible(true);
            field.set(null, null);
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        UIManager.put("OptionPane.okButtonText", AppStrings.translate("button.ok"));
        UIManager.put("OptionPane.yesButtonText", AppStrings.translate("button.yes"));
        UIManager.put("OptionPane.noButtonText", AppStrings.translate("button.no"));
        UIManager.put("OptionPane.cancelButtonText", AppStrings.translate("button.cancel"));
        UIManager.put("OptionPane.messageDialogTitle", AppStrings.translate("dialog.message.title"));
        UIManager.put("OptionPane.titleText", AppStrings.translate("dialog.select.title"));

        UIManager.put("FileChooser.acceptAllFileFilterText", AppStrings.translate("FileChooser.acceptAllFileFilterText"));
        UIManager.put("FileChooser.lookInLabelText", AppStrings.translate("FileChooser.lookInLabelText"));
        UIManager.put("FileChooser.cancelButtonText", AppStrings.translate("button.cancel"));
        UIManager.put("FileChooser.cancelButtonToolTipText", AppStrings.translate("button.cancel"));
        UIManager.put("FileChooser.openButtonText", AppStrings.translate("FileChooser.openButtonText"));
        UIManager.put("FileChooser.openButtonToolTipText", AppStrings.translate("FileChooser.openButtonToolTipText"));
        UIManager.put("FileChooser.filesOfTypeLabelText", AppStrings.translate("FileChooser.filesOfTypeLabelText"));
        UIManager.put("FileChooser.fileNameLabelText", AppStrings.translate("FileChooser.fileNameLabelText"));
        UIManager.put("FileChooser.listViewButtonToolTipText", AppStrings.translate("FileChooser.listViewButtonToolTipText"));
        UIManager.put("FileChooser.listViewButtonAccessibleName", AppStrings.translate("FileChooser.listViewButtonAccessibleName"));
        UIManager.put("FileChooser.detailsViewButtonToolTipText", AppStrings.translate("FileChooser.detailsViewButtonToolTipText"));
        UIManager.put("FileChooser.detailsViewButtonAccessibleName", AppStrings.translate("FileChooser.detailsViewButtonAccessibleName"));
        UIManager.put("FileChooser.upFolderToolTipText", AppStrings.translate("FileChooser.upFolderToolTipText"));
        UIManager.put("FileChooser.upFolderAccessibleName", AppStrings.translate("FileChooser.upFolderAccessibleName"));
        UIManager.put("FileChooser.homeFolderToolTipText", AppStrings.translate("FileChooser.homeFolderToolTipText"));
        UIManager.put("FileChooser.homeFolderAccessibleName", AppStrings.translate("FileChooser.homeFolderAccessibleName"));
        UIManager.put("FileChooser.fileNameHeaderText", AppStrings.translate("FileChooser.fileNameHeaderText"));
        UIManager.put("FileChooser.fileSizeHeaderText", AppStrings.translate("FileChooser.fileSizeHeaderText"));
        UIManager.put("FileChooser.fileTypeHeaderText", AppStrings.translate("FileChooser.fileTypeHeaderText"));
        UIManager.put("FileChooser.fileDateHeaderText", AppStrings.translate("FileChooser.fileDateHeaderText"));
        UIManager.put("FileChooser.fileAttrHeaderText", AppStrings.translate("FileChooser.fileAttrHeaderText"));
        UIManager.put("FileChooser.openDialogTitleText", AppStrings.translate("FileChooser.openDialogTitleText"));
        UIManager.put("FileChooser.directoryDescriptionText", AppStrings.translate("FileChooser.directoryDescriptionText"));
        UIManager.put("FileChooser.directoryOpenButtonText", AppStrings.translate("FileChooser.directoryOpenButtonText"));
        UIManager.put("FileChooser.directoryOpenButtonToolTipText", AppStrings.translate("FileChooser.directoryOpenButtonToolTipText"));
        UIManager.put("FileChooser.fileDescriptionText", AppStrings.translate("FileChooser.fileDescriptionText"));
        UIManager.put("FileChooser.fileNameLabelText", AppStrings.translate("FileChooser.fileNameLabelText"));
        UIManager.put("FileChooser.helpButtonText", AppStrings.translate("FileChooser.helpButtonText"));
        UIManager.put("FileChooser.helpButtonToolTipText", AppStrings.translate("FileChooser.helpButtonToolTipText"));
        UIManager.put("FileChooser.newFolderAccessibleName", AppStrings.translate("FileChooser.newFolderAccessibleName"));
        UIManager.put("FileChooser.newFolderErrorText", AppStrings.translate("FileChooser.newFolderErrorText"));
        UIManager.put("FileChooser.newFolderToolTipText", AppStrings.translate("FileChooser.newFolderToolTipText"));
        UIManager.put("FileChooser.other.newFolder", AppStrings.translate("FileChooser.other.newFolder"));
        UIManager.put("FileChooser.other.newFolder.subsequent", AppStrings.translate("FileChooser.other.newFolder.subsequent"));
        UIManager.put("FileChooser.win32.newFolder", AppStrings.translate("FileChooser.win32.newFolder"));
        UIManager.put("FileChooser.win32.newFolder.subsequent", AppStrings.translate("FileChooser.win32.newFolder.subsequent"));
        UIManager.put("FileChooser.saveButtonText", AppStrings.translate("FileChooser.saveButtonText"));
        UIManager.put("FileChooser.saveButtonToolTipText", AppStrings.translate("FileChooser.saveButtonToolTipText"));
        UIManager.put("FileChooser.saveDialogTitleText", AppStrings.translate("FileChooser.saveDialogTitleText"));
        UIManager.put("FileChooser.saveInLabelText", AppStrings.translate("FileChooser.saveInLabelText"));
        UIManager.put("FileChooser.updateButtonText", AppStrings.translate("FileChooser.updateButtonText"));
        UIManager.put("FileChooser.updateButtonToolTipText", AppStrings.translate("FileChooser.updateButtonToolTipText"));

        UIManager.put("FileChooser.detailsViewActionLabel.textAndMnemonic", AppStrings.translate("FileChooser.detailsViewActionLabel.textAndMnemonic"));
        UIManager.put("FileChooser.detailsViewButtonToolTip.textAndMnemonic", AppStrings.translate("FileChooser.detailsViewButtonToolTip.textAndMnemonic"));
        UIManager.put("FileChooser.fileAttrHeader.textAndMnemonic", AppStrings.translate("FileChooser.fileAttrHeader.textAndMnemonic"));
        UIManager.put("FileChooser.fileDateHeader.textAndMnemonic", AppStrings.translate("FileChooser.fileDateHeader.textAndMnemonic"));
        UIManager.put("FileChooser.fileNameHeader.textAndMnemonic", AppStrings.translate("FileChooser.fileNameHeader.textAndMnemonic"));
        UIManager.put("FileChooser.fileNameLabel.textAndMnemonic", AppStrings.translate("FileChooser.fileNameLabel.textAndMnemonic"));
        UIManager.put("FileChooser.fileSizeHeader.textAndMnemonic", AppStrings.translate("FileChooser.fileSizeHeader.textAndMnemonic"));
        UIManager.put("FileChooser.fileTypeHeader.textAndMnemonic", AppStrings.translate("FileChooser.fileTypeHeader.textAndMnemonic"));
        UIManager.put("FileChooser.filesOfTypeLabel.textAndMnemonic", AppStrings.translate("FileChooser.filesOfTypeLabel.textAndMnemonic"));
        UIManager.put("FileChooser.folderNameLabel.textAndMnemonic", AppStrings.translate("FileChooser.folderNameLabel.textAndMnemonic"));
        UIManager.put("FileChooser.homeFolderToolTip.textAndMnemonic", AppStrings.translate("FileChooser.homeFolderToolTip.textAndMnemonic"));
        UIManager.put("FileChooser.listViewActionLabel.textAndMnemonic", AppStrings.translate("FileChooser.listViewActionLabel.textAndMnemonic"));
        UIManager.put("FileChooser.listViewButtonToolTip.textAndMnemonic", AppStrings.translate("FileChooser.listViewButtonToolTip.textAndMnemonic"));
        UIManager.put("FileChooser.lookInLabel.textAndMnemonic", AppStrings.translate("FileChooser.lookInLabel.textAndMnemonic"));
        UIManager.put("FileChooser.newFolderActionLabel.textAndMnemonic", AppStrings.translate("FileChooser.newFolderActionLabel.textAndMnemonic"));
        UIManager.put("FileChooser.newFolderToolTip.textAndMnemonic", AppStrings.translate("FileChooser.newFolderToolTip.textAndMnemonic"));
        UIManager.put("FileChooser.refreshActionLabel.textAndMnemonic", AppStrings.translate("FileChooser.refreshActionLabel.textAndMnemonic"));
        UIManager.put("FileChooser.saveInLabel.textAndMnemonic", AppStrings.translate("FileChooser.saveInLabel.textAndMnemonic"));
        UIManager.put("FileChooser.upFolderToolTip.textAndMnemonic", AppStrings.translate("FileChooser.upFolderToolTip.textAndMnemonic"));
        UIManager.put("FileChooser.viewMenuButtonAccessibleName", AppStrings.translate("FileChooser.viewMenuButtonAccessibleName"));
        UIManager.put("FileChooser.viewMenuButtonToolTipText", AppStrings.translate("FileChooser.viewMenuButtonToolTipText"));
        UIManager.put("FileChooser.viewMenuLabel.textAndMnemonic", AppStrings.translate("FileChooser.viewMenuLabel.textAndMnemonic"));
        UIManager.put("FileChooser.newFolderActionLabelText", AppStrings.translate("FileChooser.newFolderActionLabelText"));
        UIManager.put("FileChooser.listViewActionLabelText", AppStrings.translate("FileChooser.listViewActionLabelText"));
        UIManager.put("FileChooser.detailsViewActionLabelText", AppStrings.translate("FileChooser.detailsViewActionLabelText"));
        UIManager.put("FileChooser.refreshActionLabelText", AppStrings.translate("FileChooser.refreshActionLabelText"));
        UIManager.put("FileChooser.sortMenuLabelText", AppStrings.translate("FileChooser.sortMenuLabelText"));
        UIManager.put("FileChooser.viewMenuLabelText", AppStrings.translate("FileChooser.viewMenuLabelText"));
        UIManager.put("FileChooser.fileSizeKiloBytes", AppStrings.translate("FileChooser.fileSizeKiloBytes"));
        UIManager.put("FileChooser.fileSizeMegaBytes", AppStrings.translate("FileChooser.fileSizeMegaBytes"));
        UIManager.put("FileChooser.fileSizeGigaBytes", AppStrings.translate("FileChooser.fileSizeGigaBytes"));
        UIManager.put("FileChooser.folderNameLabelText", AppStrings.translate("FileChooser.folderNameLabelText"));

        UIManager.put("ColorChooser.okText", AppStrings.translate("ColorChooser.okText"));
        UIManager.put("ColorChooser.cancelText", AppStrings.translate("ColorChooser.cancelText"));
        UIManager.put("ColorChooser.resetText", AppStrings.translate("ColorChooser.resetText"));
        UIManager.put("ColorChooser.previewText", AppStrings.translate("ColorChooser.previewText"));
        UIManager.put("ColorChooser.swatchesNameText", AppStrings.translate("ColorChooser.swatchesNameText"));
        UIManager.put("ColorChooser.swatchesRecentText", AppStrings.translate("ColorChooser.swatchesRecentText"));
        UIManager.put("ColorChooser.sampleText", AppStrings.translate("ColorChooser.sampleText"));

    }

    public static String getDefaultCharacterEncoding() {
        // Creating an array of byte type chars and
        // passing random  alphabet as an argument.abstract
        // Say alphabet be 'w'
        byte[] byte_array = {'w'};

        // Creating an object of InputStream
        InputStream instream
                = new ByteArrayInputStream(byte_array);

        // Now, opening new file input stream reader
        InputStreamReader streamreader
                = new InputStreamReader(instream);
        String defaultCharset = streamreader.getEncoding();

        // Returning default character encoding
        return defaultCharset;
    }

    private static void initLookAndFeel() {
        if (Configuration.useRibbonInterface.get()) {
            View.setLookAndFeel();
        } else {
            try {
                UIManager.put(SubstanceLookAndFeel.COLORIZATION_FACTOR, null);
                UIManager.put("Tree.expandedIcon", null);
                UIManager.put("Tree.collapsedIcon", null);
                UIManager.put("ColorChooserUI", null);
                UIManager.put("ColorChooser.swatchesRecentSwatchSize", null);
                UIManager.put("ColorChooser.swatchesSwatchSize", null);
                UIManager.put("RibbonApplicationMenuPopupPanelUI", null);
                UIManager.put("RibbonApplicationMenuButtonUI", null);
                UIManager.put("ProgressBarUI", null);
                UIManager.put("TextField.background", null);
                UIManager.put("FormattedTextField.background", null);
                UIManager.put("CommandButtonUI", null);
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | UnsupportedLookAndFeelException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void initJna() {
        if (Platform.isWindows()) {
            String jnaTempDir = Configuration.jnaTempDirectory.get();
            if (!jnaTempDir.isEmpty()) {
                System.setProperty("jna.tmpdir", jnaTempDir);
            } else {
                jnaTempDir = System.getProperty("java.io.tmpdir");
            }

            try {
                com.sun.jna.Native.toByteArray(""); //Trigger JNA.Native class static initializer, which will load the DLL
            } catch (UnsatisfiedLinkError error) {
                initLookAndFeel();
                ViewMessages.showMessageDialog(null,
                        "Cannot read JNA DLL file from current Temporary directory:\r\n"
                        + jnaTempDir + "\r\n"
                        + "The reason is probably Unicode characters in the path or in your username.\r\n"
                        + "In the following dialog, please specify new temporary directory path,\r\n"
                        + "which DOES NOT contain any special Unicode characters. (Only basic latin supported)\r\n"
                        + "Then application restart is required.",
                        "FFDec JNA Error", JOptionPane.ERROR_MESSAGE);
                View.execInEventDispatch(new Runnable() {
                    @Override
                    public void run() {
                        JFileChooser fc = new JFileChooser();
                        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        fc.setDialogTitle("Select new temporary directory without Unicode characters in its path");
                        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                            File dir = Helper.fixDialogFile(fc.getSelectedFile());
                            Configuration.jnaTempDirectory.set(dir.getAbsolutePath());
                            Configuration.saveConfig();
                            System.exit(0);
                        } else {
                            System.exit(1);
                        }
                    }
                });
            }
        }
    }

    public static void initLang() {
        if (!Configuration.locale.hasValue()) {
            if (Platform.isWindows()) {
                //Load from Installer
                String uninstKey = "{E618D276-6596-41F4-8A98-447D442A77DB}_is1";
                uninstKey = "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\" + uninstKey;
                try {
                    if (Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE, uninstKey)) {
                        if (Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, uninstKey, "NSIS: Language")) {
                            String installedLoc = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, uninstKey, "NSIS: Language");
                            int lcid = Integer.parseInt(installedLoc);
                            char[] buf = new char[9];
                            int cnt = Kernel32.INSTANCE.GetLocaleInfo(lcid, Kernel32.LOCALE_SISO639LANGNAME, buf, 9);
                            String langCode = new String(buf, 0, cnt).trim().toLowerCase();

                            cnt = Kernel32.INSTANCE.GetLocaleInfo(lcid, Kernel32.LOCALE_SISO3166CTRYNAME, buf, 9);
                            String countryCode = new String(buf, 0, cnt).trim().toLowerCase();

                            List<String> langs = Arrays.asList(SelectLanguageDialog.getAvailableLanguages());
                            for (int i = 0; i < langs.size(); i++) {
                                langs.set(i, langs.get(i).toLowerCase());
                            }

                            String selectedLang = null;

                            if (langs.contains(langCode + "-" + countryCode)) {
                                selectedLang = SelectLanguageDialog.getAvailableLanguages()[langs.indexOf(langCode + "-" + countryCode)];
                            } else if (langs.contains(langCode)) {
                                selectedLang = SelectLanguageDialog.getAvailableLanguages()[langs.indexOf(langCode)];
                            }
                            if (selectedLang != null) {
                                Configuration.locale.set(selectedLang);
                            }
                        }
                    }
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
        Locale.setDefault(Locale.forLanguageTag(Configuration.locale.get()));
        AppStrings.updateLanguage();

        Helper.decompilationErrorAdd = AppStrings.translate(Configuration.autoDeobfuscate.get() ? "deobfuscation.comment.failed" : "deobfuscation.comment.tryenable");
        
        ResourceBundle advancedSettingsBundle = ResourceBundle.getBundle(AppStrings.getResourcePath(AdvancedSettingsDialog.class));
        Set<String> confirationNames = Configuration.getConfigurationFields(false, true).keySet();
        Map<String, String> titles = new LinkedHashMap<>();
        Map<String, String> descriptions = new LinkedHashMap<>();
        for (String name : confirationNames) {
            if (advancedSettingsBundle.containsKey("config.name." + name)) {
                titles.put(name, advancedSettingsBundle.getString("config.name." + name));
            }
            if (advancedSettingsBundle.containsKey("config.description." + name)) {
                descriptions.put(name, advancedSettingsBundle.getString("config.description." + name));            
            }
        }
        Configuration.setConfigurationDescriptions(descriptions);
        Configuration.setConfigurationTitles(titles);
    }

    /**
     * Clear old FFDec/JavactiveX temp files
     */
    private static void clearTemp() {
        String tempDirPath = System.getProperty("java.io.tmpdir");
        if (tempDirPath == null) {
            return;
        }
        File tempDir = new File(tempDirPath);
        if (!tempDir.exists()) {
            return;
        }
        File[] delFiles = tempDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches("ffdec_cache.*\\.tmp") || name.matches("javactivex_.*\\.exe") || name.matches("temp[0-9]+\\.swf") || name.matches("ffdec_view_.*\\.swf");
            }
        });

        if (delFiles != null) {
            for (File f : delFiles) {
                try {
                    f.delete();
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
    }

    /**
     * To bypass wrong encoded unicode characters coming from EXE, it Launch5j
     * encodes characters using URLEncoder.
     */
    private static void decodeLaunch5jArgs(String[] args) {
        String encargs = System.getProperty("l5j.encargs");
        if ("true".equals(encargs) || "1".equals(encargs)) {
            for (int i = 0; i < args.length; ++i) {
                try {
                    args[i] = URLDecoder.decode(args[i], "UTF-8");
                } catch (Exception e) {
                    //ignored
                }
            }
        }
    }

    /**
     * @param args the command line arguments
     * @throws IOException On error
     */
    public static void main(String[] args) throws IOException {
        decodeLaunch5jArgs(args);
        setSessionLoaded(false);

        System.setProperty("sun.io.serialization.extendedDebugInfo", "true");

        clearTemp();

        try {
            SWFDecompilerPlugin.loadPlugins();
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "Failed to load plugins", ex);
        }

        AppStrings.setResourceClass(MainFrame.class);
        initLogging(Configuration._debugMode.get());

        initJna();
        initLang();

        if (Configuration.cacheOnDisk.get()) {
            Cache.setStorageType(Cache.STORAGE_FILES);
        } else {
            Cache.setStorageType(Cache.STORAGE_MEMORY);
        }

        if (args.length == 0) {
            initGui();
            checkLibraryVersion();
            View.execInEventDispatch(() -> {
                if (Configuration.allowOnlyOneInstance.get() && FirstInstance.focus()) { //Try to focus first instance
                    Main.exit();
                } else {
                    showModeFrame();
                    reloadLastSession();
                }
            });
        } else if (args.length == 1 && "-soleditor".equals(args[0])) {
            initGui();
            checkLibraryVersion();
            View.execInEventDispatch(() -> {
                DefaultSyntaxKit.initKit();
                SolEditorFrame solEditor = new SolEditorFrame(true);
                solEditor.setVisible(true);
            });
        } else {
            checkLibraryVersion();
            setSessionLoaded(true);
            String[] filesToOpen = CommandLineArgumentParser.parseArguments(args);
            if (filesToOpen != null && filesToOpen.length > 0) {
                initGui();
                View.execInEventDispatch(() -> {
                    if (Configuration.allowOnlyOneInstance.get() && FirstInstance.openFiles(Arrays.asList(filesToOpen))) { //Try to open in first instance
                        Main.exit();
                    } else {
                        for (String fileToOpen : filesToOpen) {
                            openFile(fileToOpen, null);
                        }
                    }
                });
            }
        }
    }

    private static void checkLibraryVersion() {
        if (!ApplicationInfo.version.equals("unknown") && !ApplicationInfo.libraryVersion.equals("unknown")
                && !Objects.equals(ApplicationInfo.version, ApplicationInfo.libraryVersion)) {
            logger.log(Level.WARNING, "Application version is different from library version. FFDec may not work properly.");
        }
    }

    private static void reloadLastSession() {
        boolean openingFiles = false;
        if (Configuration.saveSessionOnExit.get()) {
            String lastSession = Configuration.lastSessionFiles.get();
            if (lastSession != null && lastSession.length() > 0) {
                String[] filesToOpen = lastSession.split(File.pathSeparator, -1);
                List<String> exfiles = new ArrayList<>();
                List<String> extitles = new ArrayList<>();
                String lastSessionTitles = Configuration.lastSessionFileTitles.get();
                String[] fileTitles = new String[0];
                if (lastSessionTitles != null && !lastSessionTitles.isEmpty()) {
                    fileTitles = lastSessionTitles.split(File.pathSeparator, -1);
                }
                for (int i = 0; i < filesToOpen.length; i++) {
                    if (new File(filesToOpen[i]).exists()) {
                        exfiles.add(filesToOpen[i]);
                        if (fileTitles.length > i) {
                            extitles.add(fileTitles[i]);
                        } else {
                            extitles.add(null);
                        }
                    }
                }
                OpenableSourceInfo[] sourceInfos = new OpenableSourceInfo[exfiles.size()];
                for (int i = 0; i < exfiles.size(); i++) {
                    String extitle = extitles.get(i);
                    sourceInfos[i] = new OpenableSourceInfo(null, exfiles.get(i), extitle == null || extitle.isEmpty() ? null : extitle);
                }
                if (sourceInfos.length > 0) {
                    openingFiles = true;
                    openFile(sourceInfos, (Openable openable) -> {
                        mainFrame.getPanel().tagTree.setSelectionPathString(Configuration.lastSessionSelection.get());
                        mainFrame.getPanel().tagListTree.setSelectionPathString(Configuration.lastSessionTagListSelection.get());

                        Set<SWF> allSwfs = mainFrame.getPanel().getAllSwfs();

                        for (SWF s : allSwfs) {
                            String name = s.getFile() + "|" + s.getFileTitle();
                            if (name.equals(Configuration.lastSessionEasySwf.get())) {
                                mainFrame.getPanel().getEasyPanel().setSwf(s);
                            }
                        }

                        setSessionLoaded(true);
                        mainFrame.getPanel().reload(true);
                        mainFrame.getPanel().updateUiWithCurrentOpenable();
                    });
                }
            }
        }

        if (!openingFiles) {
            setSessionLoaded(true);
        }
    }

    public static String tempFile(String url) throws IOException {
        File f = new File(Configuration.getFFDecHome() + "saved" + File.separator);
        Path.createDirectorySafe(f);
        return Configuration.getFFDecHome() + "saved" + File.separator + "asdec_" + Integer.toHexString(url.hashCode()) + ".tmp";
    }

    public static void removeTrayIcon() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            if (trayIcon != null) {
                tray.remove(trayIcon);
                trayIcon = null;
            }
        }
    }

    public static List<SWF> namesToSwfs(List<String> names) {
        List<SWF> ret = new ArrayList<>();
        Map<String, SWF> swfs = new LinkedHashMap<>();
        populateAllSWFs(swfs);
        for (String name : names) {
            if (swfs.containsKey(name)) {
                ret.add(swfs.get(name));
            }
        }
        return ret;
    }

    public static void populateAllSWFs(Map<String, SWF> swfs) {
        if (mainFrame == null) {
            return;
        }
        List<OpenableList> ols = mainFrame.getPanel().getSwfs();
        for (OpenableList ol : ols) {
            for (Openable op : ol) {
                if (op instanceof SWF) {
                    SWF swf = (SWF) op;
                    populateSwf(swfs, swf, swf.getShortPathTitle()); //swf.getShortFileName());
                }
            }
        }
    }

    private static void populateSwf(Map<String, SWF> ret, SWF swf, String name) {
        int pos = 1;
        String baseName = name;
        while (ret.containsKey(name)) {
            pos++;
            name = baseName + "[" + pos + "]";
        }
        ret.put(name, swf);
        for (Tag t : swf.getTags()) {
            if (t instanceof DefineBinaryDataTag) {
                DefineBinaryDataTag binaryData = (DefineBinaryDataTag) t;
                if (binaryData.innerSwf != null) {
                    populateSwf(ret, binaryData.innerSwf, binaryData.innerSwf.getShortPathTitle());
                }
            }
        }
    }

    public static void exit() {
        View.saveScreen();
        if (mainFrame != null && mainFrame.getPanel() != null) {
            mainFrame.getPanel().scrollPosStorage.saveScrollPos(mainFrame.getPanel().getCurrentTree().getCurrentTreeItem());
            mainFrame.getPanel().savePins();
        }
        try {
            searchResultsStorage.save();
        } catch (IOException ex) {
            //ignore
        }
        Configuration.saveConfig();
        if (mainFrame != null && mainFrame.getPanel() != null) {
            mainFrame.dispose();
        }
        if (fileTxt != null) {
            try {
                fileTxt.flush();
                fileTxt.close();
            } catch (Exception ex) {
                //ignore
            }
        }
        System.exit(0);
    }

    public static void about() {
        (new AboutDialog(mainFrame.getWindow())).setVisible(true);
    }

    public static void advancedSettings() {
        advancedSettings(null);
    }

    public static void advancedSettings(String category) {
        (new AdvancedSettingsDialog(mainFrame.getWindow(), category)).setVisible(true);
    }

    public static void autoCheckForUpdates() {
        if (Configuration.checkForUpdatesAuto.get()) {
            Calendar lastUpdatesCheckDate = Configuration.lastUpdatesCheckDate.get();
            if ((lastUpdatesCheckDate == null) || (lastUpdatesCheckDate.getTime().getTime() < Calendar.getInstance().getTime().getTime() - Configuration.checkForUpdatesDelay.get())) {
                new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        checkForUpdates();
                        return null;
                    }
                }.execute();
            }
        }
    }

    private static JsonValue urlGetJson(String getUrl) {
        try {
            String proxyAddress = Configuration.updateProxyAddress.get();
            URL url = URI.create(getUrl).toURL();

            URLConnection uc;
            if (proxyAddress != null && !proxyAddress.isEmpty()) {
                int port = 8080;
                if (proxyAddress.contains(":")) {
                    String[] parts = proxyAddress.split(":");
                    port = Integer.parseInt(parts[1]);
                    proxyAddress = parts[0];
                }

                uc = url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAddress, port)));
            } else {
                uc = url.openConnection();
            }
            uc.setRequestProperty("User-Agent", ApplicationInfo.shortApplicationVerName);
            uc.connect();
            JsonValue value = Json.parse(new InputStreamReader(uc.getInputStream()));
            return value;
        } catch (IOException | NumberFormatException ex) {
            return null;
        }
    }

    public static boolean checkForUpdates() {
        String currentVersion = ApplicationInfo.version;
        if (currentVersion.equals("unknown") || currentVersion.equals("0.0.0")) {
            // during development the version information is not available
            return false;
        }

        boolean showStable = Configuration.checkForUpdatesStable.get();
        boolean showNightly = Configuration.checkForUpdatesNightly.get();

        if (!showStable && !showNightly) {
            return false;
        }
                
        String stableTagName = "version" + ApplicationInfo.version_major + "." + ApplicationInfo.version_minor + "." + ApplicationInfo.version_release;
        String ignoreVersion = "-";
        
        String currentTagName;
        if (ApplicationInfo.nightly) {
            currentTagName = "nightly" + ApplicationInfo.version_build;
        } else {
            currentTagName = stableTagName;
        }
        
        if (!showNightly) {
            //prereleases are not shown as latest, when checking latest nightly, this is useless
            JsonValue latestVersionInfoJson = urlGetJson(ApplicationInfo.GITHUB_RELEASES_LATEST_URL);
            if (latestVersionInfoJson == null) {
                return false;
            }
            String latestTagName = latestVersionInfoJson.asObject().get("tag_name").asString();
            if (currentTagName.equals(latestTagName) || stableTagName.equals(latestTagName)) {
                Configuration.lastUpdatesCheckDate.set(Calendar.getInstance());
                //no new version
                return false;
            }
        }

        JsonValue allChangesInfoJson = urlGetJson(ApplicationInfo.GITHUB_RELEASES_URL);
        if (allChangesInfoJson == null) {
            return false;
        }
        JsonArray arr = allChangesInfoJson.asArray();
        final java.util.List<Version> versions = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JsonObject versionObj = arr.get(i).asObject();
            String tagName = versionObj.get("tag_name").asString();
            if (currentVersion.equals(tagName) || stableTagName.equals(tagName)) {
                //Stop at current version, do not display more
                break;
            }
            Version v = new Version();
            v.versionName = versionObj.get("name").asString();
            //v.description = versionObj.get("body").asString();
            //Note: "body" is Markdown formatted and contains other things than changeslog, 
            //we cannot show it in FFDec correctly.
            v.description = "";
            v.releaseDate = versionObj.get("published_at").asString();
            boolean isNightly = versionObj.get("prerelease").asBoolean();
            if (isNightly && !showNightly) {
                continue;
            }

            if (!isNightly && !showStable) {
                continue;
            }

            versions.add(v);
        }

        if (!versions.isEmpty()) {
            View.execInEventDispatch(() -> {
                NewVersionDialog newVersionDialog = new NewVersionDialog(mainFrame == null ? null : mainFrame.getWindow(), versions);
                newVersionDialog.setVisible(true);
                Configuration.lastUpdatesCheckDate.set(Calendar.getInstance());
            });

            return true;
        }

        Configuration.lastUpdatesCheckDate.set(Calendar.getInstance());
        return false;
    }

    private static FileHandler fileTxt;

    public static void clearLogFile() {
        Logger logger = Logger.getLogger("");

        FileHandler oldFileTxt = fileTxt;
        fileTxt = null;
        if (oldFileTxt != null) {
            logger.removeHandler(fileTxt);
            oldFileTxt.flush();
            oldFileTxt.close();
        }

        String fileName = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");

        try {
            fileName = Configuration.getFFDecHome() + "logs" + File.separator;
            if (Configuration.useDetailedLogging.get()) {
                fileName += "log-" + sdf.format(new Date()) + ".txt";
            } else {
                fileName += "log.txt";
            }
            File f = new File(fileName).getParentFile();
            if (!f.exists()) {
                f.mkdir();
            }
            fileTxt = new FileHandler(fileName);
        } catch (IOException | SecurityException ex) {
            //cannot get lock error
            if (ex.getMessage().contains("lock for")) {
                //remove all old log files and their .lck
                for (int i = 0; i <= 100; i++) {
                    File flog = new File(fileName + (i == 0 ? "" : "." + i));
                    File flog_lock = new File(fileName + (i == 0 ? "" : "." + i) + ".lck");
                    flog.delete();
                    flog_lock.delete();
                }
                try {
                    fileTxt = new FileHandler(fileName);
                } catch (IOException | SecurityException ex1) {
                    logger.log(Level.SEVERE, "Cannot initialize logging", ex);
                }
            } else {
                logger.log(Level.SEVERE, "Cannot initialize logging", ex);
            }
        }

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                logger.log(Level.SEVERE, "Uncaught exception in thread: " + t.getName(), e);
                if (e instanceof OutOfMemoryError) {
                    handleOutOfMemory("");
                }
            }
        });

        Formatter formatterTxt = new LogFormatter();
        if (fileTxt != null) {
            fileTxt.setFormatter(formatterTxt);
            logger.addHandler(fileTxt);
        }

        if (!GraphicsEnvironment.isHeadless() && ErrorLogFrame.hasInstance()) {
            ErrorLogFrame.getInstance().clearErrorState();
        }

        sdf = new SimpleDateFormat("yyyy-MM-dd");
        logger.log(Level.INFO, "Date: {0}", sdf.format(new Date()));
        logger.log(Level.INFO, ApplicationInfo.applicationVerName);
        logger.log(Level.INFO, "{0} {1} {2}", new Object[]{
            System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch")});
        logger.log(Level.INFO, "{0} {1} {2}", new Object[]{
            System.getProperty("java.version"), System.getProperty("java.vendor"), System.getProperty("os.arch")});
    }

    public static void initLogging(boolean debug) {
        File loggingFile = new File(Configuration.getFFDecHome() + "/logging.properties");
        if (loggingFile.exists()) { //use manual configuration file
            final LogManager logManager = LogManager.getLogManager();
            try {
                logManager.readConfiguration(new FileInputStream(loggingFile));
                return;
            } catch (IOException ex) {
                //ignore
            }
        }
        try {
            Logger logger = Logger.getLogger("");
            logger.setLevel(Configuration.logLevel);

            Handler[] handlers = logger.getHandlers();
            for (int i = handlers.length - 1; i >= 0; i--) {
                logger.removeHandler(handlers[i]);
            }

            ConsoleHandler conHan = new ConsoleHandler();
            conHan.setLevel(debug ? Level.CONFIG : Level.WARNING);
            SimpleFormatter formatterTxt = new SimpleFormatter();
            conHan.setFormatter(formatterTxt);
            logger.addHandler(conHan);
            clearLogFile();

        } catch (Exception ex) {
            throw new RuntimeException("Problems with creating the log files");
        }
    }

    public static List<SWF> getDependencies(SWF swf) {
        SwfSpecificCustomConfiguration conf = Configuration.getSwfSpecificCustomConfiguration(swf.getShortPathTitle());
        List<SWF> dependencies = new ArrayList<>();
        if (conf != null) {
            List<String> preselectedNames = conf.getCustomDataAsList(CustomConfigurationKeys.KEY_ABC_DEPENDENCIES);
            if (!preselectedNames.isEmpty()) {
                dependencies = Main.namesToSwfs(preselectedNames);
            }
        }
        return dependencies;
    }

    public static void showBreakpointsList() {
        SWF swf = getMainFrame().getPanel().getCurrentSwf();
        getMainFrame().getPanel().showBreakpointlistDialog(swf);
    }

    public static String getSwfHash(SWF swf) {
        if (swf == getRunningSWF()) {
            return "main";
        }
        String tit = swf.getTitleOrShortFileName();
        if (tit != null && tit.contains(":")) {
            return "loaded_" + tit.substring(tit.lastIndexOf(":") + 1);
        }
        return "";
    }

    public static SWF getSwfByHash(String hash) {
        if ("main".equals(hash)) {
            SWF runningSwf = getRunningSWF();
            if (runningSwf == null) {
                return mainFrame.getPanel().getCurrentSwf();
            }
            return runningSwf;
        }
        if (!hash.startsWith("loaded_")) {
            return null;
        }
        hash = hash.substring("loaded_".length());
        for (OpenableList sl : Main.getMainFrame().getPanel().getSwfs()) {
            for (int s = 0; s < sl.size(); s++) {
                Openable op = sl.get(s);
                if (!(op instanceof SWF)) {
                    continue;
                }
                String t = op.getTitleOrShortFileName();
                if (t == null) {
                    t = "";
                }
                if (t.endsWith(":" + hash)) { //this one is already opened
                    return (SWF) op;
                }
            }
        }
        return null;
    }

    public static void openSolEditor() {
        SolEditorFrame solEdit = new SolEditorFrame(false);
        solEdit.setVisible(true);
    }
}
