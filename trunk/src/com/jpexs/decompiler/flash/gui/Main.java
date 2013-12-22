/*
 *  Copyright (C) 2010-2013 JPEXS
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

import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.Version;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.console.CommandLineArgumentParser;
import com.jpexs.decompiler.flash.console.ContextMenuTools;
import com.jpexs.decompiler.flash.gui.player.FlashPlayerPanel;
import com.jpexs.decompiler.flash.gui.proxy.ProxyFrame;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.ProgressListener;
import com.jpexs.helpers.streams.SeekableInputStream;
import com.sun.jna.Platform;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

/**
 * Main executable class
 *
 * @author JPEXS
 */
public class Main {

    public static ProxyFrame proxyFrame;
    private static List<SWFSourceInfo> sourceInfos = new ArrayList<>();
    public static LoadingDialog loadingDialog;
    public static ModeFrame modeFrame;
    private static boolean working = false;
    private static TrayIcon trayIcon;
    private static MenuItem stopMenuItem;
    private static MainFrame mainFrame;
    private static final int UPDATE_SYSTEM_MAJOR = 1;
    private static final int UPDATE_SYSTEM_MINOR = 0;
    public static LoadFromMemoryFrame loadFromMemoryFrame;
    public static LoadFromCacheFrame loadFromCacheFrame;
    private static ErrorLogFrame errorLogFrame;

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

    public static void setSubLimiter(boolean value) {
        if (value) {
            AVM2Code.toSourceLimit = Configuration.sublimiter.get();
        } else {
            AVM2Code.toSourceLimit = -1;
        }
    }

    public static boolean isWorking() {
        return working;
    }

    public static void startProxy(int port) {
        View.execInEventDispatch(new Runnable() {
            @Override
            public void run() {
                if (proxyFrame == null) {
                    proxyFrame = new ProxyFrame(mainFrame);
                }
            }
        });
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

    public static void startWork(String name) {
        startWork(name, -1, null);
    }

    public static void startWork(String name, int percent) {
        startWork(name, percent, null);
    }

    public static void startWork(String name, CancellableWorker worker) {
        startWork(name, -1, worker);
    }

    public static void startWork(final String name, final int percent, final CancellableWorker worker) {
        working = true;
        View.execInEventDispatchLater(new Runnable() {
            @Override
            public void run() {
                if (mainFrame != null) {
                    mainFrame.setWorkStatus(name, worker);
                    if (percent == -1) {
                        mainFrame.hidePercent();
                    } else {
                        mainFrame.setPercent(percent);
                    }
                }
                if (loadingDialog != null) {
                    loadingDialog.setDetail(name);
                    if (percent == -1) {
                        loadingDialog.hidePercent();
                    } else {
                        loadingDialog.setPercent(percent);
                    }
                }
                if (CommandLineArgumentParser.isCommandLineMode()) {
                    System.out.println(name);
                }
            }
        });

    }

    public static void stopWork() {
        working = false;
        View.execInEventDispatchLater(new Runnable() {
            @Override
            public void run() {
                if (mainFrame != null) {
                    mainFrame.setWorkStatus("", null);
                }
                if (loadingDialog != null) {
                    loadingDialog.setDetail("");
                }
            }
        });
    }

    public static SWF parseSWF(String file) throws Exception {
        SWFSourceInfo sourceInfo = new SWFSourceInfo(new FileInputStream(file), file, null);
        return parseSWF(sourceInfo);
    }

    public static SWF parseSWF(SWFSourceInfo sourceInfo) throws Exception {
        SWF locswf;
    
        InputStream inputStream = sourceInfo.getInputStream();
        if (inputStream == null) {
            inputStream = new FileInputStream(sourceInfo.getFile());
        } else if (inputStream instanceof SeekableInputStream) {
            try {
                ((SeekableInputStream) inputStream).seek(0);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (inputStream instanceof BufferedInputStream) {
            try {
                ((BufferedInputStream) inputStream).reset();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        locswf = new SWF(inputStream, new ProgressListener() {
            @Override
            public void progress(int p) {
                startWork(AppStrings.translate("work.reading.swf"), p);
            }
        }, Configuration.parallelSpeedUp.get());
        
        if (inputStream instanceof FileInputStream) {
            inputStream.close();
        }
        
        locswf.sourceInfo = sourceInfo;
        locswf.file = sourceInfo.getFile();
        locswf.fileTitle = sourceInfo.getFileTitle();
        locswf.addEventListener(new EventListener() {
            @Override
            public void handleEvent(String event, Object data) {
                if (event.equals("exporting")) {
                    startWork((String) data);
                }
                if (event.equals("getVariables")) {
                    startWork(AppStrings.translate("work.gettingvariables") + "..." + (String) data);
                }
                if (event.equals("deobfuscate")) {
                    startWork(AppStrings.translate("work.deobfuscating") + "..." + (String) data);
                }
                if (event.equals("rename")) {
                    startWork(AppStrings.translate("work.renaming") + "..." + (String) data);
                }
            }
        });
        //}
        return locswf;
    }

    public static void saveFile(SWF swf, String outfile) throws IOException {
        swf.file = outfile;
        File outfileF = new File(outfile);
        File tmpFile = new File(outfile + ".tmp");
        swf.saveTo(new FileOutputStream(tmpFile));
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
        }
    }

    private static class OpenFileWorker extends SwingWorker {

        private SWFSourceInfo[] sourceInfos;
        
        public OpenFileWorker(SWFSourceInfo sourceInfo) {
            this.sourceInfos = new SWFSourceInfo[] { sourceInfo };
        }

        public OpenFileWorker(SWFSourceInfo[] sourceInfos) {
            this.sourceInfos = sourceInfos;
        }

        @Override
        protected Object doInBackground() throws Exception {
            for (SWFSourceInfo sourceInfo : sourceInfos) {
                SWF swf = null;
                try {
                    Main.startWork(AppStrings.translate("work.reading.swf") + "...");
                    swf = parseSWF(sourceInfo);
                } catch (OutOfMemoryError ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    View.showMessageDialog(null, "Cannot load SWF file. Out of memory.");
                } catch (Exception ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    View.showMessageDialog(null, "Cannot load SWF file.");
                }

                final SWF swf1 = swf;
                try {
                    Main.startWork(AppStrings.translate("work.creatingwindow") + "...");
                    View.execInEventDispatch(new Runnable() {
                        @Override
                        public void run() {
                            if (mainFrame == null) {
                                mainFrame = new MainFrame();
                            }
                            mainFrame.load(swf1);
                            if (errorState) {
                                mainFrame.setErrorState();
                            }
                        }
                    });

                } catch (Exception ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            loadingDialog.setVisible(false);
            View.execInEventDispatch(new Runnable() {
                @Override
                public void run() {
                    if (mainFrame != null) {
                        mainFrame.setVisible(true);
                    }
                    Main.stopWork();
                }
            });

            return true;
        }
    }

    public static boolean reloadSWFs() {
        CancellableWorker.cancelBackgroundThreads();
        mainFrame.closeAll();
        if (Main.sourceInfos.isEmpty()) {
            Cache.clearAll();
            System.gc();
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
        if (loadingDialog != null) {
            loadingDialog.setVisible(false);
            loadingDialog = null;
        }
        if (proxyFrame != null) {
            proxyFrame.setVisible(false);
            proxyFrame = null;
        }
        if (loadFromMemoryFrame != null) {
            loadFromMemoryFrame.setVisible(false);
            loadFromMemoryFrame = null;
        }
        if (loadFromCacheFrame != null) {
            loadFromCacheFrame.setVisible(false);
            loadFromCacheFrame = null;
        }
        if (mainFrame != null) {
            mainFrame.setVisible(false);
            mainFrame = null;
        }
        reloadSWFs();
    }

    public static OpenFileResult openFile(String swfFile, String fileTitle) {
        try {
            File file = new File(swfFile);
            if (!file.exists()) {
                View.showMessageDialog(null, "File not found", "Error", JOptionPane.ERROR_MESSAGE);
                return OpenFileResult.NOT_FOUND;
            }
            swfFile = file.getCanonicalPath();
            Configuration.addRecentFile(swfFile);
            SWFSourceInfo sourceInfo = new SWFSourceInfo(null, swfFile, fileTitle);
            OpenFileResult openResult = openFile(sourceInfo);
            return openResult;
        } catch (IOException ex) {
            View.showMessageDialog(null, "Cannot open file", "Error", JOptionPane.ERROR_MESSAGE);
            return OpenFileResult.ERROR;
        }
    }

    public static OpenFileResult openFile(SWFSourceInfo sourceInfo) {
        return openFile(new SWFSourceInfo[] { sourceInfo });
    }
    
    public static OpenFileResult openFile(SWFSourceInfo[] newSourceInfos) {
        if (mainFrame != null && !Configuration.openMultiple.get()) {
            sourceInfos.clear();
            mainFrame.closeAll();
            mainFrame.setVisible(false);
            Cache.clearAll();
            System.gc();
        }

        View.execInEventDispatch(new Runnable() {
            @Override
            public void run() {
                if (Main.loadingDialog == null) {
                    Main.loadingDialog = new LoadingDialog();
                }
            }
        });

        Main.loadingDialog.setVisible(true);
        OpenFileWorker wrk = new OpenFileWorker(newSourceInfos);
        wrk.execute();
        sourceInfos.addAll(Arrays.asList(newSourceInfos));
        return OpenFileResult.OK;
    }
    
    public static OpenFileResult openFile(InputStream is, String fileTitle) {
        SWFSourceInfo sourceInfo = new SWFSourceInfo(is, null, fileTitle);
        return openFile(sourceInfo);
    }

    public static void closeFile(SWF swf) {
       sourceInfos.remove(swf.sourceInfo);
       mainFrame.close(swf);
    }
    
    public static boolean saveFileDialog(SWF swf) {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(Configuration.lastSaveDir.get()));
        FileFilter swfFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().toLowerCase().endsWith(".swf")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return AppStrings.translate("filter.swf");
            }
        };
        if (!swf.gfx) {
            fc.setFileFilter(swfFilter);
        } else {
            fc.addChoosableFileFilter(swfFilter);
        }
        FileFilter gfxFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().toLowerCase().endsWith(".gfx")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return AppStrings.translate("filter.gfx");
            }
        };
        if (swf.gfx) {
            fc.setFileFilter(gfxFilter);
        } else {
            fc.addChoosableFileFilter(gfxFilter);
        }
        fc.setAcceptAllFileFilterUsed(false);
        JFrame f = new JFrame();
        View.setWindowIcon(f);
        int returnVal = fc.showSaveDialog(f);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = Helper.fixDialogFile(fc.getSelectedFile());
            FileFilter selFilter = fc.getFileFilter();
            try {
                String fileName = file.getAbsolutePath();
                if (selFilter == swfFilter) {
                    if (!fileName.toLowerCase().endsWith(".swf")) {
                        fileName += ".swf";
                    }
                    swf.gfx = false;
                }
                if (selFilter == gfxFilter) {
                    if (!fileName.toLowerCase().endsWith(".gfx")) {
                        fileName += ".gfx";
                    }
                    swf.gfx = true;
                }
                Main.saveFile(swf, fileName);
                Configuration.lastSaveDir.set(file.getParentFile().getAbsolutePath());
                return true;
            } catch (IOException ex) {
                View.showMessageDialog(null, AppStrings.translate("error.file.write"));
            }
        }
        return false;
    }

    public static boolean openFileDialog() {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(Configuration.lastOpenDir.get()));
        FileFilter allSupportedFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().toLowerCase().toLowerCase().endsWith(".swf"))
                        || (f.getName().toLowerCase().toLowerCase().endsWith(".gfx"))
                        || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return AppStrings.translate("filter.supported");
            }
        };
        fc.setFileFilter(allSupportedFilter);
        FileFilter swfFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().toLowerCase().endsWith(".swf")) || (f.isDirectory());
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
                return (f.getName().toLowerCase().endsWith(".gfx")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return AppStrings.translate("filter.gfx");
            }
        };
        fc.addChoosableFileFilter(gfxFilter);
        fc.setAcceptAllFileFilterUsed(true);
        JFrame f = new JFrame();
        View.setWindowIcon(f);
        int returnVal = fc.showOpenDialog(f);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            Configuration.lastOpenDir.set(Helper.fixDialogFile(fc.getSelectedFile()).getParentFile().getAbsolutePath());
            File selfile = Helper.fixDialogFile(fc.getSelectedFile());
            Main.openFile(selfile.getAbsolutePath(), null);
            return true;
        } else {
            return false;
        }
    }

    public static void displayErrorFrame() {
        if (errorLogFrame != null) {
            errorLogFrame.setVisible(true);
        }
    }
    private static boolean errorState = false;

    private static void initGui() {
        View.setLookAndFeel();
        View.execInEventDispatch(new Runnable() {
            @Override
            public void run() {
                if (errorLogFrame == null) {
                    errorLogFrame = new ErrorLogFrame();
                }
                Logger logger = Logger.getLogger("");
                logger.addHandler(errorLogFrame.getHandler());
                logger.addHandler(new Handler() {
                    @Override
                    public void publish(final LogRecord record) {
                        View.execInEventDispatch(new Runnable() {
                            @Override
                            public void run() {
                                if (record.getLevel() == Level.SEVERE) {
                                    errorState = true;
                                    if (mainFrame != null) {
                                        mainFrame.setErrorState();
                                    }
                                }
                            }
                        });

                    }

                    @Override
                    public void flush() {
                    }

                    @Override
                    public void close() throws SecurityException {
                    }
                });
            }
        });
        autoCheckForUpdates();
        offerAssociation();
    }

    public static void showModeFrame() {
        View.execInEventDispatch(new Runnable() {
            @Override
            public void run() {
                if (mainFrame == null) {
                    mainFrame = new MainFrame();
                    if (errorState) {
                        mainFrame.setErrorState();
                    }
                }
                mainFrame.setVisible(true);
            }
        });

    }

    private static void offerAssociation() {
        boolean offered = Configuration.offeredAssociation.get();
        if (!offered) {
            if (Platform.isWindows()) {
                if ((!ContextMenuTools.isAddedToContextMenu()) && View.showConfirmDialog(null, "Do you want to add FFDec to context menu of SWF files?\n(Can be changed later from main menu)", "Context menu", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    ContextMenuTools.addToContextMenu(true);
                }
            }
        }
        Configuration.offeredAssociation.set(true);
    }

    public static void initLang() {
        Locale.setDefault(Locale.forLanguageTag(Configuration.locale.get()));
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

    /**
     * This may help to free some memory when not needed (maybe?)
     */
    private static void startFreeMemThread() {
        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(5000);
                        System.gc();
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();
    }

    /**
     * @param args the command line arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        startFreeMemThread();
        initLogging(Configuration.debugMode.get());

        initLang();

        if (Configuration.cacheOnDisk.get()) {
            Cache.setStorageType(Cache.STORAGE_FILES);
        } else {
            Cache.setStorageType(Cache.STORAGE_MEMORY);
        }

        if (args.length == 0) {
            initGui();
            showModeFrame();
        } else {
            String fileToOpen = CommandLineArgumentParser.parseArguments(args);
            if (fileToOpen != null) {
                initGui();
                openFile(fileToOpen, null);
            }
        }
    }

    public static String tempFile(String url) throws IOException {
        File f = new File(Configuration.getFFDecHome() + "saved" + File.separator);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                if (!f.exists()) {
                    throw new IOException("cannot create directory " + f);
                }
            }
        }
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
        Configuration.saveConfig();
        FlashPlayerPanel.unload();
        System.exit(0);
    }

    public static void about() {
        (new AboutDialog()).setVisible(true);
    }

    public static void advancedSettings() {
        (new AdvancedSettingsDialog()).setVisible(true);
    }

    public static void autoCheckForUpdates() {
        Calendar lastUpdatesCheckDate = Configuration.lastUpdatesCheckDate.get();
        if ((lastUpdatesCheckDate == null) || (lastUpdatesCheckDate.getTime().getTime() < Calendar.getInstance().getTime().getTime() - 1000 * 60 * 60 * 24)) {
            checkForUpdates();
        }
    }

    public static boolean checkForUpdates() {
        try {
            Socket sock = new Socket("www.free-decompiler.com", 80);
            OutputStream os = sock.getOutputStream();
            os.write(("GET /flash/update.html?action=check&currentVersion=" + ApplicationInfo.version + " HTTP/1.1\r\nHost: www.free-decompiler.com\r\nUser-Agent: " + ApplicationInfo.shortApplicationVerName + "\r\nConnection: close\r\n\r\n").getBytes());
            BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String s;
            boolean start = false;
            java.util.List<Version> versions = new ArrayList<>();
            String header = "";
            Pattern headerPat = Pattern.compile("\\[([a-zA-Z0-9]+)\\]");
            int updateMajor = 0;
            int updateMinor = 0;
            Version ver = null;
            while ((s = br.readLine()) != null) {
                if (start) {
                    Matcher m = headerPat.matcher(s);
                    if (m.matches()) {
                        header = m.group(1);
                        if (header.equals("version")) {
                            ver = new Version();
                            versions.add(ver);
                        }
                        if (header.equals("noversion")) {
                            break;
                        }
                    } else {
                        if (s.contains("=")) {
                            String key = s.substring(0, s.indexOf('='));
                            String val = s.substring(s.indexOf('=') + 1);
                            if ("updateSystem".equals(header)) {
                                if (key.equals("majorVersion")) {
                                    updateMajor = Integer.parseInt(val);
                                    if (updateMajor > UPDATE_SYSTEM_MAJOR) {
                                        break;
                                    }
                                }
                                if (key.equals("minorVersion")) {
                                    updateMinor = Integer.parseInt(val);
                                }
                            }
                            if ("version".equals(header) && (ver != null)) {
                                if (key.equals("versionId")) {
                                    ver.versionId = Integer.parseInt(val);
                                }
                                if (key.equals("versionName")) {
                                    ver.versionName = val;
                                }
                                if (key.equals("longVersionName")) {
                                    ver.longVersionName = val;
                                }
                                if (key.equals("releaseDate")) {
                                    ver.releaseDate = val;
                                }
                                if (key.equals("appName")) {
                                    ver.appName = val;
                                }
                                if (key.equals("appFullName")) {
                                    ver.appFullName = val;
                                }
                                if (key.equals("updateLink")) {
                                    ver.updateLink = val;
                                }
                                if (key.equals("change[]")) {
                                    String changeType = val.substring(0, val.indexOf('|'));
                                    String change = val.substring(val.indexOf('|') + 1);
                                    if (!ver.changes.containsKey(changeType)) {
                                        ver.changes.put(changeType, new ArrayList<String>());
                                    }
                                    java.util.List<String> chlist = ver.changes.get(changeType);
                                    chlist.add(change);
                                }
                            }
                        }
                    }
                }
                if (s.isEmpty()) {
                    start = true;
                }
            }

            if (!versions.isEmpty()) {
                NewVersionDialog newVersionDialog = new NewVersionDialog(versions);
                newVersionDialog.setVisible(true);
                Configuration.lastUpdatesCheckDate.set(Calendar.getInstance());
                return true;
            }
        } catch (IOException | NumberFormatException ex) {
            return false;
        }
        Configuration.lastUpdatesCheckDate.set(Calendar.getInstance());
        return false;
    }
    private static FileHandler fileTxt;

    public static void clearLogFile() {
        Logger logger = Logger.getLogger("");
        if (fileTxt != null) {
            fileTxt.flush();
            fileTxt.close();
            logger.removeHandler(fileTxt);
        }
        try {
            File f = new File(Configuration.getFFDecHome() + File.separator + "log.txt");
            FileOutputStream fos = new FileOutputStream(f);
            fos.close();
        } catch (IOException ex) {
        }
        try {
            fileTxt = new FileHandler(Configuration.getFFDecHome() + File.separator + "log.txt");
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        SimpleFormatter formatterTxt = new SimpleFormatter();
        fileTxt.setFormatter(formatterTxt);
        logger.addHandler(fileTxt);

        errorState = false;
        if (mainFrame != null) {
            mainFrame.clearErrorState();
        }
    }

    public static void initLogging(boolean debug) {
        try {
            Logger logger = Logger.getLogger("");
            logger.setLevel(debug ? Level.CONFIG : Level.WARNING);
            if (debug) {
                ConsoleHandler conHan = new ConsoleHandler();
                SimpleFormatter formatterTxt = new SimpleFormatter();
                conHan.setFormatter(formatterTxt);
                logger.addHandler(conHan);
            }
            clearLogFile();

        } catch (Exception ex) {
            throw new RuntimeException("Problems with creating the log files");
        }
    }
}
