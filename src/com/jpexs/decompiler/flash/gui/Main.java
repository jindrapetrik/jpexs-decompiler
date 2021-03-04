/*
 *  Copyright (C) 2010-2021 JPEXS
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
import com.jpexs.debugger.flash.messages.in.InCallFunction;
import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFBundle;
import com.jpexs.decompiler.flash.SWFSourceInfo;
import com.jpexs.decompiler.flash.SearchMode;
import com.jpexs.decompiler.flash.SwfOpenException;
import com.jpexs.decompiler.flash.UrlResolver;
import com.jpexs.decompiler.flash.Version;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.helpers.Reference;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.SwfSpecificConfiguration;
import com.jpexs.decompiler.flash.console.CommandLineArgumentParser;
import com.jpexs.decompiler.flash.console.ContextMenuTools;
import com.jpexs.decompiler.flash.exporters.modes.ExeExportMode;
import com.jpexs.decompiler.flash.gui.debugger.DebugListener;
import com.jpexs.decompiler.flash.gui.debugger.DebuggerTools;
import com.jpexs.decompiler.flash.gui.pipes.FirstInstance;
import com.jpexs.decompiler.flash.gui.proxy.ProxyFrame;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerPlugin;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImportTag;
import com.jpexs.decompiler.flash.treeitems.SWFList;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import com.jpexs.helpers.ProgressListener;
import com.jpexs.helpers.Stopwatch;
import com.jpexs.helpers.streams.SeekableInputStream;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinReg;
import java.awt.AWTException;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystems;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;

/**
 * Main executable class
 *
 * @author JPEXS
 */
public class Main {

    protected static ProxyFrame proxyFrame;

    private static List<SWFSourceInfo> sourceInfos = new ArrayList<>();

    public static LoadingDialog loadingDialog;

    private static boolean working = false;

    private static TrayIcon trayIcon;

    private static MenuItem stopMenuItem;

    private static volatile MainFrame mainFrame;

    public static final int UPDATE_SYSTEM_MAJOR = 1;

    public static final int UPDATE_SYSTEM_MINOR = 3;

    private static LoadFromMemoryFrame loadFromMemoryFrame;

    private static LoadFromCacheFrame loadFromCacheFrame;

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static DebugLogDialog debugDialog;

    public static boolean shouldCloseWhenClosingLoadingDialog;

    private static Debugger flashDebugger;

    private static DebuggerHandler debugHandler = null;

    //private static int ip = 0;
    //private static String ipClass = null;
    private static Process runProcess;

    private static boolean runProcessDebug;

    private static boolean runProcessDebugPCode;

    private static boolean inited = false;

    private static File runTempFile;

    private static List<File> runTempFiles = new ArrayList<>();

    private static WatchService watcher;

    private static SwingWorker watcherWorker;

    private static Map<WatchKey, File> watchedDirectories = new HashMap<>();

    private static FilesChangedDialog filesChangedDialog;

    private static List<File> savedFiles = Collections.synchronizedList(new ArrayList<>());

    public static SearchResultsStorage searchResultsStorage = new SearchResultsStorage();

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
                runTempFile.delete();
                runTempFile = null;
            }
            for (File f : runTempFiles) {
                f.delete();
            }
            runTempFiles.clear();

            runProcess = null;
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

    /*
     * FIXME!          
     */
    public static synchronized void dumpBytes(Variable v) {
        InCallFunction icf;
        try {
            long objectId = 0l;
            if ((v.vType == VariableType.OBJECT || v.vType == VariableType.MOVIECLIP)) {
                objectId = (Long) v.value;
            }
            Object oldPos = getDebugHandler().getVariable(objectId, "position", true).parent.value;
            getDebugHandler().setVariable(objectId, "position", VariableType.NUMBER, 0);
            icf = getDebugHandler().callFunction(false, "readUTF", v, new ArrayList<>());
            System.out.println("Result=" + icf.variables.get(0).value);
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

        CancellableWorker runWorker = new CancellableWorker() {
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

        public SWF prepare(SWF swf);
    }

    private static class SwfRunPrepare implements SwfPreparation {

        @Override
        public SWF prepare(SWF swf) {
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
        public SWF prepare(SWF instrSWF) {
            instrSWF = super.prepare(instrSWF);
            try {
                File fTempFile = new File(instrSWF.getFile());
                instrSWF.enableDebugging(true, new File("."), true, doPCode);
                FileOutputStream fos = new FileOutputStream(fTempFile);
                instrSWF.saveTo(fos);
                fos.close();
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
                        if (doPCode) {
                            instrSWF.generatePCodeSwdFile(swdFile, getPackBreakPoints(true));
                        } else {
                            instrSWF.generateSwdFile(swdFile, getPackBreakPoints(true));
                        }
                    }
                }
            } catch (IOException ex) {
                //ignore, return instrSWF
            }
            return instrSWF;
        }
    }

    private static void prepareSwf(SwfPreparation prep, File toPrepareFile, File origFile, List<File> tempFiles) throws IOException {
        SWF instrSWF = null;
        try (FileInputStream fis = new FileInputStream(toPrepareFile)) {
            instrSWF = new SWF(fis, toPrepareFile.getAbsolutePath(), origFile == null ? "unknown.swf" : origFile.getName(), false);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        if (instrSWF != null) {
            if (origFile != null) {
                for (Tag t : instrSWF.getLocalTags()) {
                    if (t instanceof ImportTag) {
                        ImportTag it = (ImportTag) t;
                        String url = it.getUrl();
                        File importedFile = new File(origFile.getParentFile(), url);
                        if (importedFile.exists()) {
                            File newTempFile = File.createTempFile("ffdec_run_import_", ".swf");
                            it.setUrl("./" + newTempFile.getName());
                            byte[] impData = Helper.readFile(importedFile.getAbsolutePath());
                            Helper.writeFile(newTempFile.getAbsolutePath(), impData);
                            tempFiles.add(newTempFile);
                            prepareSwf(prep, newTempFile, importedFile, tempFiles);
                        }
                    }
                }
            }
            if (prep != null) {
                instrSWF = prep.prepare(instrSWF);
            }
            try (FileOutputStream fos = new FileOutputStream(toPrepareFile)) {
                instrSWF.saveTo(fos);
            }
        }
    }

    public static void run(SWF swf) {
        String flashVars = "";//key=val&key2=val2
        String playerLocation = Configuration.playerLocation.get();
        if (playerLocation.isEmpty() || (!new File(playerLocation).exists())) {
            View.showMessageDialog(null, AppStrings.translate("message.playerpath.notset"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            advancedSettings("paths");
            return;
        }
        if (swf == null) {
            return;
        }
        File tempFile;
        List<File> tempFiles = new ArrayList<>();
        try {
            tempFile = File.createTempFile("ffdec_run_", ".swf");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                swf.saveTo(fos);
            }

            prepareSwf(new SwfRunPrepare(), tempFile, swf.getFile() == null ? null : new File(swf.getFile()), tempFiles);

        } catch (IOException ex) {
            return;

        }
        if (tempFile != null) {
            synchronized (Main.class) {
                runTempFile = tempFile;
                runTempFiles = tempFiles;
                runProcessDebug = false;
            }
            runPlayer(AppStrings.translate("work.running"), playerLocation, tempFile.getAbsolutePath(), flashVars);
        }
    }

    public static void runDebug(SWF swf, final boolean doPCode) {
        String flashVars = "";//key=val&key2=val2
        String playerLocation = Configuration.playerDebugLocation.get();
        if (playerLocation.isEmpty() || (!new File(playerLocation).exists())) {
            View.showMessageDialog(null, AppStrings.translate("message.playerpath.debug.notset"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            Main.advancedSettings("paths");
            return;
        }
        if (swf == null) {
            return;
        }
        File tempFile = null;

        try {
            tempFile = File.createTempFile("ffdec_debug_", ".swf");
        } catch (Exception ex) {

        }

        if (tempFile != null) {
            final File fTempFile = tempFile;
            final List<File> tempFiles = new ArrayList<>();
            CancellableWorker instrumentWorker = new CancellableWorker() {
                @Override
                protected Object doInBackground() throws Exception {

                    try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(fTempFile))) {
                        swf.saveTo(fos);
                    }
                    prepareSwf(new SwfDebugPrepare(doPCode), fTempFile, swf.getFile() == null ? null : new File(swf.getFile()), tempFiles);
                    return null;
                }

                @Override
                public void workerCancelled() {
                    Main.stopWork();
                }

                @Override
                protected void done() {
                    synchronized (Main.class) {
                        runTempFile = fTempFile;
                        runProcessDebug = true;
                        runProcessDebugPCode = doPCode;
                        runTempFiles = tempFiles;
                    }
                    Main.stopWork();
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

    public synchronized static int getIp(Object pack) {
        return getDebugHandler().getBreakIp();
    }

    public synchronized static String getIpClass() {
        return getDebugHandler().getBreakScriptName();
    }

    public static synchronized boolean isBreakPointValid(String scriptName, int line) {
        return !getDebugHandler().isBreakpointInvalid(scriptName, line);
    }

    public synchronized static void addBreakPoint(String scriptName, int line) {
        getDebugHandler().addBreakPoint(scriptName, line);
    }

    public synchronized static void removeBreakPoint(String scriptName, int line) {
        getDebugHandler().removeBreakPoint(scriptName, line);
    }

    public synchronized static boolean toggleBreakPoint(String scriptName, int line) {
        if (getDebugHandler().isBreakpointToAdd(scriptName, line) || getDebugHandler().isBreakpointConfirmed(scriptName, line) || getDebugHandler().isBreakpointInvalid(scriptName, line)) {
            getDebugHandler().removeBreakPoint(scriptName, line);
            return false;
        } else {
            getDebugHandler().addBreakPoint(scriptName, line);
            return true;
        }
    }

    public synchronized static Map<String, Set<Integer>> getPackBreakPoints(boolean validOnly) {
        return getDebugHandler().getAllBreakPoints(validOnly);
    }

    public synchronized static Set<Integer> getScriptBreakPoints(String pack, boolean onlyValid) {
        return getDebugHandler().getBreakPoints(pack, onlyValid);
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

    public static void loadFromCache() {
        if (loadFromCacheFrame == null) {
            loadFromCacheFrame = new LoadFromCacheFrame();
        }
        loadFromCacheFrame.setVisible(true);
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

    public synchronized static boolean isInited() {
        return inited;
    }

    public synchronized static void setSessionLoaded(boolean v) {
        inited = v;
    }

    public static boolean isWorking() {
        return working;
    }

    public static void startProxy(int port) {
        if (proxyFrame == null) {
            proxyFrame = new ProxyFrame(mainFrame);
        }

        proxyFrame.setPort(port);
        addTrayIcon();
        switchProxy();
    }

    public static void showProxy() {
        if (proxyFrame == null) {
            proxyFrame = new ProxyFrame(mainFrame);
        }
        proxyFrame.setVisible(true);
        proxyFrame.setState(Frame.NORMAL);
    }

    public static void startWork(String name, CancellableWorker worker) {
        startWork(name, -1, worker);
    }

    public static void startWork(final String name, final int percent, final CancellableWorker worker) {
        working = true;
        View.execInEventDispatchLater(() -> {
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
        View.execInEventDispatchLater(() -> {
            if (mainFrame != null) {
                mainFrame.getPanel().setWorkStatus("", null);
            }
            if (loadingDialog != null) {
                loadingDialog.setDetail("");
            }
        });
    }

    public static SWFList parseSWF(SWFSourceInfo sourceInfo) throws Exception {
        SWFList result = new SWFList();

        InputStream inputStream = sourceInfo.getInputStream();
        SWFBundle bundle = null;
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
            result.bundle = bundle;
            result.name = new File(sourceInfo.getFileTitleOrName()).getName();
            for (Entry<String, SeekableInputStream> streamEntry : bundle.getAll().entrySet()) {
                InputStream stream = streamEntry.getValue();
                stream.reset();
                CancellableWorker<SWF> worker = new CancellableWorker<SWF>() {
                    @Override
                    public SWF doInBackground() throws Exception {
                        final CancellableWorker worker = this;
                        SWF swf = new SWF(stream, null, streamEntry.getKey(), new ProgressListener() {
                            @Override
                            public void progress(int p) {
                                startWork(AppStrings.translate("work.reading.swf"), p, worker);
                            }
                        }, Configuration.parallelSpeedUp.get());
                        return swf;
                    }
                };
                loadingDialog.setWroker(worker);
                worker.execute();

                try {
                    result.add(worker.get());
                } catch (CancellationException ex) {
                    logger.log(Level.WARNING, "Loading SWF {0} was cancelled.", streamEntry.getKey());
                }
            }
        } else {
            InputStream fInputStream = inputStream;

            final String[] yesno = new String[]{AppStrings.translate("button.yes"), AppStrings.translate("button.no"), AppStrings.translate("button.yes.all"), AppStrings.translate("button.no.all")};

            CancellableWorker<SWF> worker = new CancellableWorker<SWF>() {
                private boolean yestoall = false;

                private boolean notoall = false;

                private SWF open(InputStream is, String file, String fileTitle) throws IOException, InterruptedException {
                    final CancellableWorker worker = this;

                    SWF swf = new SWF(is, file, fileTitle, new ProgressListener() {
                        @Override
                        public void progress(int p) {
                            startWork(AppStrings.translate("work.reading.swf"), p, worker);
                        }
                    }, Configuration.parallelSpeedUp.get(), false, true, new UrlResolver() {
                        @Override
                        public SWF resolveUrl(final String url) {
                            int opt = -1;
                            if (!(yestoall || notoall)) {
                                opt = View.showOptionDialog(null, AppStrings.translate("message.imported.swf").replace("%url%", url), AppStrings.translate("message.warning"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, yesno, AppStrings.translate("button.yes"));
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

                            if (opt == 1) //no
                            {
                                return null;
                            }

                            if (url.startsWith("http://") || url.startsWith("https://")) {
                                try {
                                    URL u = new URL(url);
                                    return open(u.openStream(), null, url); //?
                                } catch (Exception ex) {
                                    //ignore
                                }
                            } else {
                                File f = new File(new File(file).getParentFile(), url);
                                if (f.exists()) {
                                    try {
                                        return open(new FileInputStream(f), f.getAbsolutePath(), f.getName());
                                    } catch (Exception ex) {
                                        //ignore
                                    }
                                }
                            }
                            Reference<SWF> ret = new Reference<>(null);
                            View.execInEventDispatch(new Runnable() {
                                @Override
                                public void run() {

                                    while (JOptionPane.YES_OPTION == View.showConfirmDialog(null, AppStrings.translate("message.imported.swf.manually").replace("%url%", url), AppStrings.translate("error"), JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE)) {

                                        JFileChooser fc = new JFileChooser();
                                        fc.setCurrentDirectory(new File(Configuration.lastOpenDir.get()));
                                        FileFilter allSupportedFilter = new FileFilter() {
                                            private final String[] supportedExtensions = new String[]{".swf", ".gfx"};

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
                                                return (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".swf")) || (f.isDirectory());
                                            }

                                            @Override
                                            public String getDescription() {
                                                return AppStrings.translate("filter.swf");
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
                                        JFrame f = new JFrame();
                                        View.setWindowIcon(f);
                                        int returnVal = fc.showOpenDialog(f);
                                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                                            Configuration.lastOpenDir.set(Helper.fixDialogFile(fc.getSelectedFile()).getParentFile().getAbsolutePath());
                                            File selFile = Helper.fixDialogFile(fc.getSelectedFile());
                                            try {
                                                ret.setVal(open(new FileInputStream(selFile), selFile.getAbsolutePath(), selFile.getName()));
                                                break;
                                            } catch (Exception ex) {
                                                //ignore;
                                            }
                                        } else {
                                            break;
                                        }
                                    }
                                }
                            });
                            return ret.getVal();
                        }
                    });
                    return swf;
                }

                @Override
                public SWF doInBackground() throws Exception {
                    return open(fInputStream, sourceInfo.getFile(), sourceInfo.getFileTitle());
                }
            };
            if (loadingDialog != null) {
                loadingDialog.setWroker(worker);
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
        for (SWF swf : result) {
            logger.log(Level.INFO, "");
            logger.log(Level.INFO, "== File information ==");
            logger.log(Level.INFO, "Size: {0}", Helper.formatFileSize(swf.fileSize));
            logger.log(Level.INFO, "Flash version: {0}", swf.version);
            int width = (int) ((swf.displayRect.Xmax - swf.displayRect.Xmin) / SWF.unitDivisor);
            int height = (int) ((swf.displayRect.Ymax - swf.displayRect.Ymin) / SWF.unitDivisor);
            logger.log(Level.INFO, "Width: {0}", width);
            logger.log(Level.INFO, "Height: {0}", height);

            swf.swfList = result;
            swf.addEventListener(new EventListener() {
                @Override
                public void handleExportingEvent(String type, int index, int count, Object data) {
                    String text = AppStrings.translate("work.exporting");
                    if (type != null && type.length() > 0) {
                        text += " " + type;
                    }

                    startWork(text + " " + index + "/" + count + " " + data, null);
                }

                @Override
                public void handleExportedEvent(String type, int index, int count, Object data) {
                    String text = AppStrings.translate("work.exported");
                    if (type != null && type.length() > 0) {
                        text += " " + type;
                    }

                    startWork(text + " " + index + "/" + count + " " + data, null);
                }

                @Override
                public void handleEvent(String event, Object data) {
                    if (event.equals("exporting") || event.equals("exported")) {
                        throw new Error("Event is not supported by this handler.");
                    }
                    if (event.equals("getVariables")) {
                        startWork(AppStrings.translate("work.gettingvariables") + "..." + (String) data, null);
                    }
                    if (event.equals("deobfuscate")) {
                        startWork(AppStrings.translate("work.deobfuscating") + "..." + (String) data, null);
                    }
                    if (event.equals("rename")) {
                        startWork(AppStrings.translate("work.renaming") + "..." + (String) data, null);
                    }
                }
            });
        }

        return result;
    }

    public static void saveFile(SWF swf, String outfile) throws IOException {
        saveFile(swf, outfile, SaveFileMode.SAVE, null);
    }

    public static void saveFile(SWF swf, String outfile, SaveFileMode mode, ExeExportMode exeExportMode) throws IOException {
        File savedFile = new File(outfile);
        startSaving(savedFile);
        if (mode == SaveFileMode.SAVEAS && swf.swfList != null /*SWF in binarydata has null*/ && !swf.swfList.isBundle()) {
            swf.setFile(outfile);
            swf.swfList.sourceInfo.setFile(outfile);
        }
        File outfileF = new File(outfile);
        File tmpFile = new File(outfile + ".tmp");
        try (FileOutputStream fos = new FileOutputStream(tmpFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            if (mode == SaveFileMode.EXE) {
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
            }

            long pos = fos.getChannel().position();
            swf.saveTo(bos);

            if (mode == SaveFileMode.EXE) {
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

        private final SWFSourceInfo[] sourceInfos;

        private final Runnable executeAfterOpen;

        private final int[] reloadIndices;

        public OpenFileWorker(SWFSourceInfo sourceInfo) {
            this(sourceInfo, -1);
        }

        public OpenFileWorker(SWFSourceInfo sourceInfo, int reloadIndex) {
            this(sourceInfo, null, reloadIndex);
        }

        public OpenFileWorker(SWFSourceInfo sourceInfo, Runnable executeAfterOpen) {
            this(sourceInfo, executeAfterOpen, -1);
        }

        public OpenFileWorker(SWFSourceInfo sourceInfo, Runnable executeAfterOpen, int reloadIndex) {
            this.sourceInfos = new SWFSourceInfo[]{sourceInfo};
            this.executeAfterOpen = executeAfterOpen;
            this.reloadIndices = new int[]{reloadIndex};
        }

        public OpenFileWorker(SWFSourceInfo[] sourceInfos) {
            this(sourceInfos, null, null);
        }

        public OpenFileWorker(SWFSourceInfo[] sourceInfos, Runnable executeAfterOpen) {
            this(sourceInfos, executeAfterOpen, null);
        }

        public OpenFileWorker(SWFSourceInfo[] sourceInfos, Runnable executeAfterOpen, int[] reloadIndices) {
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
            for (int index = 0; index < sourceInfos.length; index++) {
                SWFSourceInfo sourceInfo = sourceInfos[index];
                SWFList swfs = null;
                try {
                    Main.startWork(AppStrings.translate("work.reading.swf") + "...", null);
                    try {
                        swfs = parseSWF(sourceInfo);
                    } catch (ExecutionException ex) {
                        Throwable cause = ex.getCause();
                        if (cause instanceof SwfOpenException) {
                            throw (SwfOpenException) cause;
                        }

                        throw ex;
                    }
                } catch (OutOfMemoryError ex) {
                    logger.log(Level.SEVERE, null, ex);
                    View.showMessageDialog(null, "Cannot load SWF file. Out of memory.");
                    continue;
                } catch (SwfOpenException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    View.showMessageDialog(null, ex.getMessage());
                    continue;
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                    View.showMessageDialog(null, "Cannot load SWF file.");
                    continue;
                }

                final SWFList swfs1 = swfs;
                final boolean first1 = first;
                first = false;
                if (firstSWF == null && swfs1.size() > 0) {
                    firstSWF = swfs1.get(0);
                }

                final int findex = index;
                try {
                    View.execInEventDispatch(() -> {
                        Main.startWork(AppStrings.translate("work.creatingwindow") + "...", null);
                        ensureMainFrame();
                        if (reloadIndices[findex] > -1) {
                            mainFrame.getPanel().loadSwfAtPos(swfs1, reloadIndices[findex]);
                        } else {
                            mainFrame.getPanel().load(swfs1, first1);
                        }
                    });
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }

            loadingDialog.setVisible(false);
            shouldCloseWhenClosingLoadingDialog = false;

            final SWF fswf = firstSWF;
            View.execInEventDispatch(() -> {
                if (mainFrame != null) {
                    mainFrame.setVisible(true);
                }

                Main.stopWork();

                if (mainFrame != null && Configuration.gotoMainClassOnStartup.get()) {
                    mainFrame.getPanel().gotoDocumentClass(fswf);
                }

                if (mainFrame != null && fswf != null) {
                    SwfSpecificConfiguration swfConf = Configuration.getSwfSpecificConfiguration(fswf.getShortFileName());
                    if (swfConf != null) {
                        String pathStr = swfConf.lastSelectedPath;
                        if (isInited()) {
                            mainFrame.getPanel().tagTree.setSelectionPathString(pathStr);
                        } else {
                            mainFrame.getPanel().tagTree.setExpandPathString(pathStr);
                        }
                    }
                }

                if (executeAfterOpen != null) {
                    executeAfterOpen.run();
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
            SWFSourceInfo[] sourceInfosCopy = new SWFSourceInfo[sourceInfos.size()];
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
        if (proxyFrame != null) {
            proxyFrame.setVisible(false);
            proxyFrame.dispose();
            proxyFrame = null;
        }
        if (loadFromMemoryFrame != null) {
            loadFromMemoryFrame.setVisible(false);
            loadFromMemoryFrame.dispose();
            loadFromMemoryFrame = null;
        }
        if (loadFromCacheFrame != null) {
            loadFromCacheFrame.setVisible(false);
            loadFromCacheFrame.dispose();
            loadFromCacheFrame = null;
        }
        if (mainFrame != null) {
            mainFrame.setVisible(false);
            mainFrame.getPanel().closeAll(false);
            mainFrame.dispose();
            mainFrame = null;
        }
        FontTag.reload();
        Cache.clearAll();
        initGui();
        reloadSWFs();
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
                View.showMessageDialog(null, AppStrings.translate("open.error.fileNotFound"), AppStrings.translate("open.error"), JOptionPane.ERROR_MESSAGE);
                return OpenFileResult.NOT_FOUND;
            }
            swfFile = file.getCanonicalPath();
            SWFSourceInfo sourceInfo = new SWFSourceInfo(null, swfFile, fileTitle);
            OpenFileResult openResult = openFile(sourceInfo);
            return openResult;
        } catch (IOException ex) {
            View.showMessageDialog(null, AppStrings.translate("open.error.cannotOpen"), AppStrings.translate("open.error"), JOptionPane.ERROR_MESSAGE);
            return OpenFileResult.ERROR;
        }
    }

    public static OpenFileResult openFile(SWFSourceInfo sourceInfo) {
        View.checkAccess();

        return openFile(new SWFSourceInfo[]{sourceInfo});
    }

    public static OpenFileResult openFile(SWFSourceInfo sourceInfo, Runnable executeAfterOpen) {
        View.checkAccess();

        return openFile(new SWFSourceInfo[]{sourceInfo}, executeAfterOpen);
    }

    public static OpenFileResult openFile(SWFSourceInfo sourceInfo, Runnable executeAfterOpen, int reloadIndex) {
        View.checkAccess();

        return openFile(new SWFSourceInfo[]{sourceInfo}, executeAfterOpen, new int[]{reloadIndex});
    }

    public static OpenFileResult openFile(SWFSourceInfo[] newSourceInfos) {
        View.checkAccess();

        return openFile(newSourceInfos, null);
    }

    public static OpenFileResult openFile(SWFSourceInfo[] newSourceInfos, Runnable executeAfterOpen) {
        View.checkAccess();

        return openFile(newSourceInfos, executeAfterOpen, null);
    }

    public static OpenFileResult openFile(SWFSourceInfo[] newSourceInfos, Runnable executeAfterOpen, int[] reloadIndices) {
        View.checkAccess();

        if (mainFrame != null && !Configuration.openMultipleFiles.get()) {
            sourceInfos.clear();
            mainFrame.getPanel().closeAll(false);
            mainFrame.setVisible(false);
            Helper.freeMem();
            reloadIndices = null;
        }

        loadingDialog.setVisible(true);

        for (int i = 0; i < newSourceInfos.length; i++) {
            SWFSourceInfo si = newSourceInfos[i];
            String fileName = si.getFile();
            if (fileName != null) {
                Configuration.addRecentFile(fileName);
                if (watcher != null) {
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

    public static void closeFile(SWFList swf) {
        View.checkAccess();

        sourceInfos.remove(swf.sourceInfo);
        mainFrame.getPanel().close(swf);
    }

    public static void reloadFile(SWFList swf) {
        View.checkAccess();

        openFile(swf.sourceInfo, null, sourceInfos.indexOf(swf.sourceInfo));
    }

    public static void reloadFile(File file) {
        for (int i = 0; i < sourceInfos.size(); i++) {
            SWFSourceInfo info = sourceInfos.get(i);
            if (info.getFile() == null) {
                continue;
            }
            if (file.equals(new File(info.getFile()))) {
                openFile(info, null, i);
            }
        }
    }

    public static boolean closeAll() {
        View.checkAccess();

        boolean closeResult = mainFrame.getPanel().closeAll(true);
        if (closeResult) {
            sourceInfos.clear();
        }

        if (filesChangedDialog != null) {
            filesChangedDialog.setVisible(false);
        }

        return closeResult;
    }

    public static boolean saveFileDialog(SWF swf, final SaveFileMode mode) {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(Configuration.lastSaveDir.get()));
        String ext = ".swf";
        switch (mode) {
            case SAVE:
            case SAVEAS:
                if (swf.getFile() != null) {
                    ext = Path.getExtension(swf.getFile());
                }
                break;
            case EXE:
                ext = ".exe";
                break;
        }

        FileFilter swfFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".swf")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return AppStrings.translate("filter.swf");
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

        ExeExportMode exeExportMode = null;
        if (mode == SaveFileMode.EXE) {
            exeExportMode = Configuration.exeExportMode.get();
            if (exeExportMode == null) {
                exeExportMode = ExeExportMode.WRAPPER;
            }
            String filterDescription = null;
            switch (exeExportMode) {
                case WRAPPER:
                case PROJECTOR_WIN:
                    ext = ".exe";
                    filterDescription = "filter.exe";
                    break;
                case PROJECTOR_MAC:
                    ext = ".dmg";
                    filterDescription = "filter.dmg";
                    break;
                case PROJECTOR_LINUX:
                    // linux projector is compressed with tar.gz
                    // todo: decompress
                    ext = "";
                    filterDescription = "filter.linuxExe";
                    break;
            }

            String fext = ext;
            String ffilterDescription = filterDescription;
            FileFilter exeFilter = new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return (f.getName().toLowerCase(Locale.ENGLISH).endsWith(fext)) || (f.isDirectory());
                }

                @Override
                public String getDescription() {
                    return AppStrings.translate(ffilterDescription);
                }
            };
            fc.setFileFilter(exeFilter);
        } else if (swf.gfx) {
            fc.addChoosableFileFilter(swfFilter);
            fc.setFileFilter(gfxFilter);
        } else {
            fc.setFileFilter(swfFilter);
            fc.addChoosableFileFilter(gfxFilter);
        }
        final String extension = ext;
        fc.setAcceptAllFileFilterUsed(false);
        JFrame f = new JFrame();
        View.setWindowIcon(f);
        if (fc.showSaveDialog(f) == JFileChooser.APPROVE_OPTION) {
            File file = Helper.fixDialogFile(fc.getSelectedFile());
            FileFilter selFilter = fc.getFileFilter();
            try {
                String fileName = file.getAbsolutePath();
                if (selFilter == swfFilter) {
                    if (!fileName.toLowerCase(Locale.ENGLISH).endsWith(extension)) {
                        fileName += extension;
                    }
                    swf.gfx = false;
                }
                if (selFilter == gfxFilter) {
                    if (!fileName.toLowerCase(Locale.ENGLISH).endsWith(".gfx")) {
                        fileName += ".gfx";
                    }
                    swf.gfx = true;
                }
                Main.saveFile(swf, fileName, mode, exeExportMode);
                Configuration.lastSaveDir.set(file.getParentFile().getAbsolutePath());
                return true;
            } catch (IOException ex) {
                View.showMessageDialog(null, AppStrings.translate("error.file.write"));
            }
        }
        return false;
    }

    public static boolean openFileDialog() {
        View.checkAccess();

        JFileChooser fc = new JFileChooser();
        if (Configuration.openMultipleFiles.get()) {
            fc.setMultiSelectionEnabled(true);
        }
        fc.setCurrentDirectory(new File(Configuration.lastOpenDir.get()));
        FileFilter allSupportedFilter = new FileFilter() {
            private final String[] supportedExtensions = new String[]{".swf", ".gfx", ".swc", ".zip", ".iggy"};

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
                return (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".swf")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return AppStrings.translate("filter.swf");
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
        JFrame f = new JFrame();
        View.setWindowIcon(f);
        int returnVal = fc.showOpenDialog(f);
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

    private static String md5(byte data[]) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
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

        if (Configuration.hwAcceleratedGraphics.get()) {
            System.setProperty("sun.java2d.opengl", Configuration._debugMode.get() ? "True" : "true");
        } else {
            System.setProperty("sun.java2d.opengl", "false");
        }

        initUiLang();

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
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        View.execInEventDispatch(() -> {
            ErrorLogFrame.createNewInstance();

            autoCheckForUpdates();
            offerAssociation();
            loadingDialog = new LoadingDialog();

            if (Configuration.checkForModifications.get()) {
                try {
                    watcher = FileSystems.getDefault().newWatchService();
                } catch (IOException ex) {
                    //ignore
                }
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
                                    System.err.println("overflow");
                                    continue;
                                }

                                @SuppressWarnings("unchecked")
                                WatchEvent<java.nio.file.Path> ev = (WatchEvent<java.nio.file.Path>) event;

                                java.nio.file.Path filename = ev.context();

                                if (watchedDirectories.containsKey(key)) {
                                    File dir = watchedDirectories.get(key);
                                    java.nio.file.Path child = dir.toPath().resolve(filename);
                                    File fullPath = child.toFile();
                                    if (savedFiles.contains(fullPath)) {
                                        continue;
                                    }

                                    for (SWFSourceInfo info : sourceInfos) {
                                        final String infoFile = info.getFile();
                                        if (infoFile != null && new File(infoFile).equals(fullPath)) {
                                            View.execInEventDispatchLater(new Runnable() {
                                                @Override
                                                public void run() {
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
                                break;
                            }
                        }
                        return null;
                    }
                };
                watcherWorker.execute();
            }

            DebuggerTools.initDebugger().addMessageListener(new DebugListener() {
                @Override
                public void onMessage(String clientId, String msg) {
                }

                @Override
                public void onLoaderURL(String clientId, String url) {
                }

                @Override
                public void onLoaderBytes(String clientId, byte[] data) {
                    String hash = md5(data);
                    for (SWFList sl : Main.getMainFrame().getPanel().getSwfs()) {
                        for (int s = 0; s < sl.size(); s++) {
                            String t = sl.get(s).getFileTitle();
                            if (t == null) {
                                t = "";
                            }
                            if (t.endsWith(":" + hash)) { //this one is already opened
                                return;
                            }
                        }
                    }
                    SWF swf = Main.getMainFrame().getPanel().getCurrentSwf();

                    String title = swf == null ? "?" : swf.getFileTitle();
                    final String titleWithHash = title + ":" + hash;
                    try {
                        final String tfile = tempFile(titleWithHash);
                        Helper.writeFile(tfile, data);
                        View.execInEventDispatch(new Runnable() {
                            @Override
                            public void run() {
                                openFile(new SWFSourceInfo(null, tfile, titleWithHash));
                            }
                        });
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, "Cannot create tempfile");
                    }
                }

                @Override
                public void onFinish(String clientId) {
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
                                mainFrame.getPanel().gotoScriptLine(getMainFrame().getPanel().getCurrentSwf(), scriptName, line, classIndex, traitIndex, methodIndex);
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
                if ((!ContextMenuTools.isAddedToContextMenu()) && View.showConfirmDialog(null, "Do you want to add FFDec to context menu of SWF files?\n(Can be changed later from main menu)", "Context menu", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
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
     * @param args the command line arguments
     * @throws IOException On error
     */
    public static void main(String[] args) throws IOException {
        setSessionLoaded(false);

        clearTemp();

        try {
            SWFDecompilerPlugin.loadPlugins();
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "Failed to load plugins", ex);
        }

        AppStrings.setResourceClass(MainFrame.class);
        initLogging(Configuration._debugMode.get());

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
        } else {
            checkLibraryVersion();
            setSessionLoaded(true);
            String[] filesToOpen = CommandLineArgumentParser.parseArguments(args);
            if (filesToOpen != null && filesToOpen.length > 0) {
                View.execInEventDispatch(() -> {
                    initGui();
                    shouldCloseWhenClosingLoadingDialog = true;
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
                SWFSourceInfo[] sourceInfos = new SWFSourceInfo[exfiles.size()];
                for (int i = 0; i < exfiles.size(); i++) {
                    String extitle = extitles.get(i);
                    sourceInfos[i] = new SWFSourceInfo(null, exfiles.get(i), extitle == null || extitle.isEmpty() ? null : extitle);
                }
                if (sourceInfos.length > 0) {
                    openingFiles = true;
                    openFile(sourceInfos, () -> {
                        mainFrame.getPanel().tagTree.setSelectionPathString(Configuration.lastSessionSelection.get());
                        setSessionLoaded(true);
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

    public static void switchProxy() {
        proxyFrame.switchState();
        if (stopMenuItem != null) {
            if (proxyFrame.isRunning()) {
                stopMenuItem.setLabel(AppStrings.translate("proxy.stop"));
            } else {
                stopMenuItem.setLabel(AppStrings.translate("proxy.start"));
            }
        }
    }

    public static void addTrayIcon() {
        if (trayIcon != null) {
            return;
        }
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            trayIcon = new TrayIcon(View.loadImage("proxy16"), ApplicationInfo.VENDOR + " " + ApplicationInfo.SHORT_APPLICATION_NAME + " " + AppStrings.translate("proxy"));
            trayIcon.setImageAutoSize(true);
            PopupMenu trayPopup = new PopupMenu();

            ActionListener trayListener = new ActionListener() {
                /**
                 * Invoked when an action occurs.
                 */
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (e.getActionCommand().equals("EXIT")) {
                        Main.exit();
                    }
                    if (e.getActionCommand().equals("SHOW")) {
                        Main.showProxy();
                    }
                    if (e.getActionCommand().equals("SWITCH")) {
                        Main.switchProxy();
                    }
                }
            };

            MenuItem showMenuItem = new MenuItem(AppStrings.translate("proxy.show"));
            showMenuItem.setActionCommand("SHOW");
            showMenuItem.addActionListener(trayListener);
            trayPopup.add(showMenuItem);
            stopMenuItem = new MenuItem(AppStrings.translate("proxy.start"));
            stopMenuItem.setActionCommand("SWITCH");
            stopMenuItem.addActionListener(trayListener);
            trayPopup.add(stopMenuItem);
            trayPopup.addSeparator();
            MenuItem exitMenuItem = new MenuItem(AppStrings.translate("exit"));
            exitMenuItem.setActionCommand("EXIT");
            exitMenuItem.addActionListener(trayListener);
            trayPopup.add(exitMenuItem);

            trayIcon.setPopupMenu(trayPopup);
            trayIcon.addMouseListener(new MouseAdapter() {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        Main.showProxy();
                    }
                }
            });
            try {
                tray.add(trayIcon);
            } catch (AWTException ex) {
            }
        }
    }

    public static void exit() {
        try {
            searchResultsStorage.save();
        } catch (IOException ex) {
            //ignore
        }
        Configuration.saveConfig();
        if (mainFrame != null && mainFrame.getPanel() != null) {
            mainFrame.getPanel().unloadFlashPlayer();
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
        (new AboutDialog()).setVisible(true);
    }

    public static void advancedSettings() {
        advancedSettings(null);
    }

    public static void advancedSettings(String category) {
        (new AdvancedSettingsDialog(category)).setVisible(true);
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
            URL url = new URL(getUrl);

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
        if (currentVersion.equals("unknown")) {
            // sometimes during development the version information is not available
            return false;
        }

        boolean showStable = Configuration.checkForUpdatesStable.get();
        boolean showNightly = Configuration.checkForUpdatesNightly.get();

        if (!showStable && !showNightly) {
            return false;
        }

        String currentTagName;
        if (ApplicationInfo.nightly) {
            currentTagName = "nightly" + ApplicationInfo.version_build;
        } else {
            currentTagName = "version" + ApplicationInfo.version_major + "." + ApplicationInfo.version_minor + "." + ApplicationInfo.version_release;
        }

        if (!showNightly) {
            //prereleases are not shown as latest, when checking latest nightly, this is useless
            JsonValue latestVersionInfoJson = urlGetJson(ApplicationInfo.GITHUB_RELEASES_LATEST_URL);
            if (latestVersionInfoJson == null) {
                return false;
            }
            String latestTagName = latestVersionInfoJson.asObject().get("tag_name").asString();
            if (currentTagName.equals(latestTagName)) {
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
            if (currentVersion.equals(tagName)) {
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
                NewVersionDialog newVersionDialog = new NewVersionDialog(versions);
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
                if (e instanceof OutOfMemoryError && !Helper.is64BitJre() && Helper.is64BitOs()) {
                    View.showMessageDialog(null, AppStrings.translate("message.warning.outOfMemory32BitJre"), AppStrings.translate("message.warning"), JOptionPane.WARNING_MESSAGE);
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
}
