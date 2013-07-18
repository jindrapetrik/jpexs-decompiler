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

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.Configuration;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.PercentListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.Version;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import static com.jpexs.decompiler.flash.gui.AppStrings.translate;
import com.jpexs.decompiler.flash.gui.jna.platform.win32.Advapi32Util;
import com.jpexs.decompiler.flash.gui.jna.platform.win32.Kernel32;
import com.jpexs.decompiler.flash.gui.jna.platform.win32.SHELLEXECUTEINFO;
import com.jpexs.decompiler.flash.gui.jna.platform.win32.Shell32;
import com.jpexs.decompiler.flash.gui.jna.platform.win32.Win32Exception;
import com.jpexs.decompiler.flash.gui.jna.platform.win32.WinReg;
import com.jpexs.decompiler.flash.gui.jna.platform.win32.WinUser;
import com.jpexs.decompiler.flash.gui.player.FlashPlayerPanel;
import com.jpexs.decompiler.flash.gui.proxy.ProxyFrame;
import com.jpexs.decompiler.flash.helpers.Cache;
import com.jpexs.decompiler.flash.helpers.Helper;
import com.sun.jna.Platform;
import com.sun.jna.WString;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

/**
 * Main executable class
 *
 * @author JPEXS
 */
public class Main {

    public static ProxyFrame proxyFrame;
    public static String file;
    public static String maskURL;
    public static SWF swf;
    public static String version = "";
    public static final String applicationName = "JPEXS Free Flash Decompiler";
    public static String applicationVerName;
    public static final String shortApplicationName = "FFDec";
    public static String shortApplicationVerName;
    public static final String projectPage = "http://www.free-decompiler.com/flash";
    public static String updatePageStub = "http://www.free-decompiler.com/flash/update.html?currentVersion=";
    public static String updatePage;
    public static final String vendor = "JPEXS";
    public static LoadingDialog loadingDialog;
    public static ModeFrame modeFrame;
    private static boolean working = false;
    private static TrayIcon trayIcon;
    private static MenuItem stopMenuItem;
    private static boolean commandLineMode = false;
    public static MainFrame mainFrame;
    private static final int UPDATE_SYSTEM_MAJOR = 1;
    private static final int UPDATE_SYSTEM_MINOR = 0;

    private static void loadProperties() {
        Properties prop = new Properties();
        try {
            prop.load(Main.class.getResourceAsStream("/project.properties"));
            version = prop.getProperty("version");
            applicationVerName = applicationName + " v." + version;
            updatePage = updatePageStub + version;
            shortApplicationVerName = shortApplicationName + " v." + version;
        } catch (IOException ex) {
            //ignore
            version = "unknown";
        }
    }

    public static boolean isCommandLineMode() {
        return commandLineMode;
    }

    /**
     * Get title of the file
     *
     * @return file title
     */
    public static String getFileTitle() {
        if (maskURL != null) {
            return maskURL;
        }
        return file;
    }

    public static void setSubLimiter(boolean value) {
        if (value) {
            AVM2Code.toSourceLimit = Configuration.SUBLIMITER;
        } else {
            AVM2Code.toSourceLimit = -1;
        }
    }

    public static boolean isWorking() {
        return working;
    }

    public static void showProxy() {
        if (proxyFrame == null) {
            proxyFrame = new ProxyFrame();
        }
        proxyFrame.setVisible(true);
        proxyFrame.setState(Frame.NORMAL);
    }

    public static void startWork(String name) {
        startWork(name, -1);
    }

    public static void startWork(String name, int percent) {
        working = true;
        if (mainFrame != null) {
            mainFrame.setWorkStatus(name);
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
        if (Main.isCommandLineMode()) {
            System.out.println(name);
        }
    }

    public static void stopWork() {
        working = false;
        if (mainFrame != null) {
            mainFrame.setWorkStatus("");
        }
        if (loadingDialog != null) {
            loadingDialog.setDetail("");
        }
    }

    public static SWF parseSWF(String file) throws Exception {
        SWF locswf;
        try (FileInputStream fis = new FileInputStream(file)) {
            InputStream bis = new BufferedInputStream(fis);
            locswf = new SWF(bis, new PercentListener() {
                @Override
                public void percent(int p) {
                    startWork(translate("work.reading.swf"), p);
                }
            }, (Boolean) Configuration.getConfig("paralelSpeedUp", Boolean.TRUE));
            locswf.addEventListener(new EventListener() {
                @Override
                public void handleEvent(String event, Object data) {
                    if (event.equals("export")) {
                        startWork((String) data);
                    }
                    if (event.equals("getVariables")) {
                        startWork(translate("work.gettingvariables") + "..." + (String) data);
                    }
                    if (event.equals("deobfuscate")) {
                        startWork(translate("work.deobfuscating") + "..." + (String) data);
                    }
                    if (event.equals("rename")) {
                        startWork(translate("work.renaming") + "..." + (String) data);
                    }
                }
            });
        }
        return locswf;
    }

    public static void saveFile(String outfile) throws IOException {
        file = outfile;
        swf.saveTo(new FileOutputStream(outfile));
    }

    private static class OpenFileWorker extends SwingWorker {

        @Override
        protected Object doInBackground() throws Exception {
            try {
                Main.startWork(translate("work.reading.swf") + "...");
                swf = parseSWF(Main.file);
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, "Cannot load SWF file.");
                loadingDialog.setVisible(false);
                exit();
                return false;
            }

            try {
                Main.startWork(translate("work.creatingwindow") + "...");
                mainFrame = new MainFrame(swf);
                loadingDialog.setVisible(false);
                mainFrame.setVisible(true);
                Main.stopWork();
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;
        }
    }

    public static boolean openFile(String swfFile) {
        if (mainFrame != null) {
            mainFrame.setVisible(false);
        }
        Main.file = swfFile;
        if (Main.loadingDialog == null) {
            Main.loadingDialog = new LoadingDialog();
        }
        Main.loadingDialog.setVisible(true);
        (new OpenFileWorker()).execute();
        return true;
    }

    public static boolean saveFileDialog() {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File((String) Configuration.getConfig("lastSaveDir", ".")));
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().endsWith(".swf")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return translate("filter.swf");
            }
        });
        fc.setAcceptAllFileFilterUsed(false);
        JFrame f = new JFrame();
        View.setWindowIcon(f);
        int returnVal = fc.showSaveDialog(f);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = Helper.fixDialogFile(fc.getSelectedFile());
            try {
                Main.saveFile(file.getAbsolutePath());
                Configuration.setConfig("lastSaveDir", file.getParentFile().getAbsolutePath());
                maskURL = null;
                return true;
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, translate("error.file.write"));
            }
        }
        return false;
    }

    public static boolean openFileDialog() {
        maskURL = null;
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File((String) Configuration.getConfig("lastOpenDir", ".")));
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().endsWith(".swf")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return translate("filter.swf");
            }
        });
        JFrame f = new JFrame();
        View.setWindowIcon(f);
        int returnVal = fc.showOpenDialog(f);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            Configuration.setConfig("lastOpenDir", Helper.fixDialogFile(fc.getSelectedFile()).getParentFile().getAbsolutePath());
            File selfile = Helper.fixDialogFile(fc.getSelectedFile());
            Main.openFile(selfile.getAbsolutePath());
            return true;
        } else {
            return false;
        }
    }

    public static void showModeFrame() {
        if (modeFrame == null) {
            modeFrame = new ModeFrame();
        }
        modeFrame.setVisible(true);
    }

    public static void updateLicense() {
        updateLicenseInDir(new File(".\\src\\"));
    }

    /**
     * Script for updating license header in java files :-)
     *
     * @param dir Star directory (e.g. "src/")
     */
    public static void updateLicenseInDir(File dir) {
        int defaultStartYear = 2010;
        int defaultFinalYear = 2013;
        String defaultAuthor = "JPEXS";
        String defaultYearStr = "" + defaultStartYear;
        if (defaultFinalYear != defaultStartYear) {
            defaultYearStr += "-" + defaultFinalYear;
        }
        String license = "/*\r\n *  Copyright (C) {year} {author}\r\n * \r\n *  This program is free software: you can redistribute it and/or modify\r\n *  it under the terms of the GNU General Public License as published by\r\n *  the Free Software Foundation, either version 3 of the License, or\r\n *  (at your option) any later version.\r\n * \r\n *  This program is distributed in the hope that it will be useful,\r\n *  but WITHOUT ANY WARRANTY; without even the implied warranty of\r\n *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\r\n *  GNU General Public License for more details.\r\n * \r\n *  You should have received a copy of the GNU General Public License\r\n *  along with this program.  If not, see <http://www.gnu.org/licenses/>.\r\n */";

        File files[] = dir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                updateLicenseInDir(f);
            } else {
                if (f.getName().endsWith(".java")) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PrintWriter pw = null;
                    try {
                        pw = new PrintWriter(new OutputStreamWriter(baos, "utf8"));
                    } catch (UnsupportedEncodingException ex) {
                    }
                    try {
                        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                            String s;
                            boolean packageFound = false;
                            String author = defaultAuthor;
                            String yearStr = defaultYearStr;
                            while ((s = br.readLine()) != null) {
                                if (!packageFound) {
                                    if (s.trim().startsWith("package")) {
                                        packageFound = true;
                                        pw.println(license.replace("{year}", yearStr).replace("{author}", author));
                                    } else {
                                        Matcher mAuthor = Pattern.compile("^.*Copyright \\(C\\) ([0-9]+)(-[0-9]+)? (.*)$").matcher(s);
                                        if (mAuthor.matches()) {
                                            author = mAuthor.group(3).trim();
                                            int startYear = Integer.parseInt(mAuthor.group(1).trim());
                                            if (startYear == defaultFinalYear) {
                                                yearStr = "" + startYear;
                                            } else {
                                                yearStr = "" + startYear + "-" + defaultFinalYear;
                                            }
                                            if (!author.equals(defaultAuthor)) {
                                                System.out.println("Detected nodefault author:" + author + " in " + f.getAbsolutePath());
                                            }
                                        }
                                    }
                                }
                                if (packageFound) {
                                    pw.println(s);
                                }
                            }
                        }
                        pw.close();
                    } catch (IOException ex) {
                    }

                    FileOutputStream fos;
                    try {
                        fos = new FileOutputStream(f);
                        fos.write(baos.toByteArray());
                        fos.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }

    }

    public static void badArguments() {
        System.err.println("Error: Bad Commandline Arguments!");
        printCmdLineUsage();
        System.exit(1);
    }

    public static void printHeader() {
        System.out.println(applicationVerName);
        for (int i = 0; i < applicationVerName.length(); i++) {
            System.out.print("-");
        }
        System.out.println();
    }

    public static void printCmdLineUsage() {
        System.out.println("Commandline arguments:");
        System.out.println(" 1) -help | --help | /?");
        System.out.println(" ...shows commandline arguments (this help)");
        System.out.println(" 2) infile");
        System.out.println(" ...opens SWF file with the decompiler GUI");
        System.out.println(" 3) -proxy (-PXXX)");
        System.out.println("  ...auto start proxy in the tray. Optional parameter -P specifies port for proxy. Defaults to 55555. ");
        System.out.println(" 4) -export (as|pcode|image|shape|movie|sound|binaryData|text|textplain|all) outdirectory infile [-selectas3class class1 class2 ...]");
        System.out.println("  ...export infile sources to outdirectory as AsctionScript code (\"as\" argument) or as PCode (\"pcode\" argument), images, shapes, movies, binaryData, text with formatting, plain text or all.");
        System.out.println("     When \"as\" or \"pcode\" type specified, optional \"-selectas3class\" parameter can be passed to export only selected classes (ActionScript 3 only)");
        System.out.println(" 5) -dumpSWF infile");
        System.out.println("  ...dumps list of SWF tags to console");
        System.out.println(" 6) -compress infile outfile");
        System.out.println("  ...Compress SWF infile and save it to outfile");
        System.out.println(" 7) -decompress infile outfile");
        System.out.println("  ...Decompress infile and save it to outfile");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("java -jar ffdec.jar myfile.swf");
        System.out.println("java -jar ffdec.jar -proxy");
        System.out.println("java -jar ffdec.jar -proxy -P1234");
        System.out.println("java -jar ffdec.jar -export as \"C:\\decompiled\\\" myfile.swf");
        System.out.println("java -jar ffdec.jar -export as \"C:\\decompiled\\\" myfile.swf -selectas3class com.example.MyClass com.example.SecondClass");
        System.out.println("java -jar ffdec.jar -export pcode \"C:\\decompiled\\\" myfile.swf");
        System.out.println("java -jar ffdec.jar -dumpSWF myfile.swf");
        System.out.println("java -jar ffdec.jar -compress myfile.swf myfiledec.swf");
        System.out.println("java -jar ffdec.jar -decompress myfiledec.swf myfile.swf");
    }

    private static void offerAssociation() {
        boolean offered = (Boolean) Configuration.getConfig("offeredAssociation", Boolean.FALSE);
        if (!offered) {
            if (Platform.isWindows()) {
                if ((!isAddedToContextMenu()) && JOptionPane.showConfirmDialog(null, "Do you want to add FFDec to context menu of SWF files?\n(Can be changed later from main menu)", "Context menu", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    addToContextMenu(true);
                }
            }
        }
        Configuration.setConfig("offeredAssociation", Boolean.TRUE);
    }

    public static void initLang() {
        if (Configuration.containsConfig("locale")) {
            Locale.setDefault(Locale.forLanguageTag((String) Configuration.getConfig("locale", "en")));
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
    }

    /**
     * @param args the command line arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        loadProperties();
        Configuration.loadFromFile(getConfigFile(), getReplacementsFile());
        int pos = 0;
        if (args.length > 0) {
            if (args[0].equals("-debug")) {
                Configuration.debugMode = true;
                pos++;
            }
        }
        initLogging(Configuration.debugMode);

        initLang();
        View.setLookAndFeel();
        if ((Boolean) Configuration.getConfig("cacheOnDisk", Boolean.TRUE)) {
            Cache.setStorageType(Cache.STORAGE_FILES);
        } else {
            Cache.setStorageType(Cache.STORAGE_MEMORY);
        }

        if (args.length < pos + 1) {
            autoCheckForUpdates();
            offerAssociation();
            showModeFrame();
        } else {
            if (args[pos].equals("-removefromcontextmenu")) {
                addToContextMenu(false);
            } else if (args[pos].equals("-addtocontextmenu")) {
                addToContextMenu(true);
            } else if (args[pos].equals("-proxy")) {
                int port = 55555;
                for (int i = pos; i < args.length; i++) {
                    if (args[i].startsWith("-P")) {
                        try {
                            port = Integer.parseInt(args[pos].substring(2));
                        } catch (NumberFormatException nex) {
                            System.err.println("Bad port number");
                        }
                    }
                }
                if (proxyFrame == null) {
                    proxyFrame = new ProxyFrame();
                }
                proxyFrame.setPort(port);
                addTrayIcon();
                switchProxy();
            } else if (args[pos].equals("-export")) {
                if (args.length < pos + 4) {
                    badArguments();
                }
                String validExportFormats[] = new String[]{
                    "as",
                    "pcode",
                    "image",
                    "shape",
                    "movie",
                    "sound",
                    "binarydata",
                    "text",
                    "textplain",
                    "all",
                    "fla",
                    "xfl"
                };

                AbortRetryIgnoreHandler handler = new AbortRetryIgnoreHandler() {
                    @Override
                    public int handle(Throwable thrown) {
                        Scanner sc = new Scanner(System.in);
                        System.out.println("Error occured: " + thrown.getLocalizedMessage());
                        String n = null;
                        do {
                            System.out.print("Select action: (A)bort, (R)Retry, (I)Ignore:");
                            n = sc.nextLine();
                            switch (n.toLowerCase()) {
                                case "a":
                                    return AbortRetryIgnoreHandler.ABORT;
                                case "r":
                                    return AbortRetryIgnoreHandler.RETRY;
                                case "i":
                                    return AbortRetryIgnoreHandler.IGNORE;
                            }
                        } while (true);
                    }
                };
                String exportFormat = args[pos + 1].toLowerCase();
                if (!Arrays.asList(validExportFormats).contains(exportFormat)) {
                    System.err.println("Invalid export format:" + exportFormat);
                    badArguments();
                }
                File outDir = new File(args[pos + 2]);
                File inFile = new File(args[pos + 3]);
                if (!inFile.exists()) {
                    System.err.println("Input SWF file does not exist!");
                    badArguments();
                }
                commandLineMode = true;
                boolean exportOK;
                try {
                    printHeader();
                    SWF exfile = new SWF(new FileInputStream(inFile), (Boolean) Configuration.getConfig("paralelSpeedUp", Boolean.TRUE));
                    exfile.addEventListener(new EventListener() {
                        @Override
                        public void handleEvent(String event, Object data) {
                            if (event.equals("export")) {
                                System.out.println((String) data);
                            }
                        }
                    });

                    switch (exportFormat) {
                        case "all":
                            System.out.println("Exporting images...");
                            exfile.exportImages(handler, outDir.getAbsolutePath() + File.separator + "images");
                            System.out.println("Exporting shapes...");
                            exfile.exportShapes(handler, outDir.getAbsolutePath() + File.separator + "shapes");
                            System.out.println("Exporting scripts...");
                            exfile.exportActionScript(handler, outDir.getAbsolutePath() + File.separator + "scripts", false, (Boolean) Configuration.getConfig("paralelSpeedUp", Boolean.TRUE));
                            System.out.println("Exporting movies...");
                            exfile.exportMovies(handler, outDir.getAbsolutePath() + File.separator + "movies");
                            System.out.println("Exporting sounds...");
                            exfile.exportSounds(handler, outDir.getAbsolutePath() + File.separator + "sounds", true, true);
                            System.out.println("Exporting binaryData...");
                            exfile.exportBinaryData(handler, outDir.getAbsolutePath() + File.separator + "binaryData");
                            System.out.println("Exporting texts...");
                            exfile.exportTexts(handler, outDir.getAbsolutePath() + File.separator + "texts", true);
                            exportOK = true;
                            break;
                        case "image":
                            exfile.exportImages(handler, outDir.getAbsolutePath());
                            exportOK = true;
                            break;
                        case "shape":
                            exfile.exportShapes(handler, outDir.getAbsolutePath());
                            exportOK = true;
                            break;
                        case "as":
                        case "pcode":
                            if ((pos + 5 < args.length) && (args[pos + 4].equals("-selectas3class"))) {
                                exportOK = true;
                                for (int i = pos + 5; i < args.length; i++) {
                                    exportOK = exportOK && exfile.exportAS3Class(args[i], outDir.getAbsolutePath(), exportFormat.equals("pcode"), (Boolean) Configuration.getConfig("paralelSpeedUp", Boolean.TRUE));
                                }
                            } else {
                                exportOK = !exfile.exportActionScript(handler, outDir.getAbsolutePath(), exportFormat.equals("pcode"), (Boolean) Configuration.getConfig("paralelSpeedUp", Boolean.TRUE)).isEmpty();
                            }
                            break;
                        case "movie":
                            exfile.exportMovies(handler, outDir.getAbsolutePath());
                            exportOK = true;
                            break;
                        case "sound":
                            exfile.exportSounds(handler, outDir.getAbsolutePath(), true, true);
                            exportOK = true;
                            break;
                        case "binarydata":
                            exfile.exportBinaryData(handler, outDir.getAbsolutePath());
                            exportOK = true;
                            break;
                        case "text":
                            exfile.exportTexts(handler, outDir.getAbsolutePath(), true);
                            exportOK = true;
                            break;
                        case "textplain":
                            exfile.exportTexts(handler, outDir.getAbsolutePath(), false);
                            exportOK = true;
                            break;
                        case "fla":
                            exfile.exportFla(handler, outDir.getAbsolutePath(), inFile.getName(), applicationName, applicationVerName, version, (Boolean) Configuration.getConfig("paralelSpeedUp", Boolean.TRUE));
                            exportOK = true;
                            break;
                        case "xfl":
                            exfile.exportXfl(handler, outDir.getAbsolutePath(), inFile.getName(), applicationName, applicationVerName, version, (Boolean) Configuration.getConfig("paralelSpeedUp", Boolean.TRUE));
                            exportOK = true;
                            break;
                        default:
                            exportOK = false;
                    }
                } catch (Exception ex) {
                    exportOK = false;
                    System.err.print("FAIL: Exporting Failed on Exception - ");
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(1);
                }
                if (exportOK) {
                    System.out.println("OK");
                    System.exit(0);
                } else {
                    System.err.println("FAIL");
                    System.exit(1);
                }
            } else if (args[pos].equals("-compress")) {
                if (args.length < pos + 3) {
                    badArguments();
                }

                if (SWF.fws2cws(new FileInputStream(args[pos + 1]), new FileOutputStream(args[pos + 2]))) {
                    System.out.println("OK");
                } else {
                    System.err.println("FAIL");
                }
            } else if (args[pos].equals("-decompress")) {
                if (args.length < pos + 3) {
                    badArguments();
                }

                if (SWF.decompress(new FileInputStream(args[pos + 1]), new FileOutputStream(args[pos + 2]))) {
                    System.out.println("OK");
                    System.exit(0);
                } else {
                    System.err.println("FAIL");
                    System.exit(1);
                }
            } else if (args[pos].equals("-dumpSWF")) {
                if (args.length < pos + 2) {
                    badArguments();
                }
                try {
                    Configuration.dump_tags = true;
                    SWF swf = parseSWF(args[pos + 1]);
                } catch (Exception ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(1);
                }
                System.exit(0);
            } else if (args[pos].equals("-help") || args[pos].equals("--help") || args[pos].equals("/?")) {
                printHeader();
                printCmdLineUsage();
                System.exit(0);
            } else if (args.length == pos + 1) {
                autoCheckForUpdates();
                offerAssociation();
                openFile(args[pos]);
            } else {
                badArguments();
            }
        }
    }

    public static String tempFile(String url) throws IOException {
        File f = new File(getFFDecHome() + "saved" + File.separator);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                if (!f.exists()) {
                    throw new IOException("cannot create directory " + f);
                }
            }
        }
        return getFFDecHome() + "saved" + File.separator + "asdec_" + Integer.toHexString(url.hashCode()) + ".tmp";

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
                stopMenuItem.setLabel(translate("proxy.stop"));
            } else {
                stopMenuItem.setLabel(translate("proxy.start"));
            }
        }
    }

    public static void addTrayIcon() {
        if (trayIcon != null) {
            return;
        }
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            trayIcon = new TrayIcon(View.loadImage("proxy16"), vendor + " " + shortApplicationName + " " + translate("proxy"));
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


            MenuItem showMenuItem = new MenuItem(translate("proxy.show"));
            showMenuItem.setActionCommand("SHOW");
            showMenuItem.addActionListener(trayListener);
            trayPopup.add(showMenuItem);
            stopMenuItem = new MenuItem(translate("proxy.start"));
            stopMenuItem.setActionCommand("SWITCH");
            stopMenuItem.addActionListener(trayListener);
            trayPopup.add(stopMenuItem);
            trayPopup.addSeparator();
            MenuItem exitMenuItem = new MenuItem(translate("exit"));
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
            Configuration.saveToFile(getConfigFile(), getReplacementsFile());
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        FlashPlayerPanel.unload();
        System.exit(0);
    }

    public static void about() {
        (new AboutDialog()).setVisible(true);
    }

    public static void autoCheckForUpdates() {
        Calendar lastUpdatesCheckDate = (Calendar) Configuration.getConfig("lastUpdatesCheckDate", null);
        if ((lastUpdatesCheckDate == null) || (lastUpdatesCheckDate.getTime().getTime() < Calendar.getInstance().getTime().getTime() - 1000 * 60 * 60 * 24)) {
            checkForUpdates();
        }
    }

    public static boolean checkForUpdates() {
        try {
            Socket sock = new Socket("www.free-decompiler.com", 80);
            OutputStream os = sock.getOutputStream();
            os.write(("GET /flash/update.html?action=check&currentVersion=" + version + " HTTP/1.1\r\nHost: www.free-decompiler.com\r\nUser-Agent: " + shortApplicationVerName + "\r\nConnection: close\r\n\r\n").getBytes());
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
                            String key = s.substring(0, s.indexOf("="));
                            String val = s.substring(s.indexOf("=") + 1);
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
                                    String changeType = val.substring(0, val.indexOf("|"));
                                    String change = val.substring(val.indexOf("|") + 1);
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
                if (s.equals("")) {
                    start = true;
                }
            }

            if (!versions.isEmpty()) {
                NewVersionDialog newVersionDialog = new NewVersionDialog(versions);
                newVersionDialog.setVisible(true);
                Configuration.setConfig("lastUpdatesCheckDate", Calendar.getInstance());
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
        Configuration.setConfig("lastUpdatesCheckDate", Calendar.getInstance());
        return false;
    }

    public static void initLogging(boolean debug) {
        try {
            Logger logger = Logger.getLogger("");
            logger.setLevel(debug ? Level.CONFIG : Level.WARNING);
            FileHandler fileTxt = new FileHandler(getFFDecHome() + File.separator + "log.txt");

            SimpleFormatter formatterTxt = new SimpleFormatter();
            fileTxt.setFormatter(formatterTxt);
            logger.addHandler(fileTxt);

            if (debug) {
                ConsoleHandler conHan = new ConsoleHandler();
                conHan.setFormatter(formatterTxt);
                logger.addHandler(conHan);
            }

        } catch (Exception ex) {
            throw new RuntimeException("Problems with creating the log files");
        }
    }
    private static final String CONFIG_NAME = "config.bin";
    private static final String REPLACEMENTS_NAME = "replacements.cfg";
    private static final File unspecifiedFile = new File("unspecified");
    private static File directory = unspecifiedFile;

    private enum OSId {

        WINDOWS, OSX, UNIX
    }

    private static OSId getOSId() {
        PrivilegedAction<String> doGetOSName = new PrivilegedAction<String>() {
            @Override
            public String run() {
                return System.getProperty("os.name");
            }
        };
        OSId id = OSId.UNIX;
        String osName = AccessController.doPrivileged(doGetOSName);
        if (osName != null) {
            if (osName.toLowerCase().startsWith("mac os x")) {
                id = OSId.OSX;
            } else if (osName.contains("Windows")) {
                id = OSId.WINDOWS;
            }
        }
        return id;
    }

    public static String getFFDecHome() throws IOException {
        if (directory == unspecifiedFile) {
            directory = null;
            String userHome = null;
            try {
                userHome = System.getProperty("user.home");
            } catch (SecurityException ignore) {
            }
            if (userHome != null) {
                String applicationId = Main.shortApplicationName;
                OSId osId = getOSId();
                if (osId == OSId.WINDOWS) {
                    File appDataDir = null;
                    try {
                        String appDataEV = System.getenv("APPDATA");
                        if ((appDataEV != null) && (appDataEV.length() > 0)) {
                            appDataDir = new File(appDataEV);
                        }
                    } catch (SecurityException ignore) {
                    }
                    String vendorId = Main.vendor;
                    if ((appDataDir != null) && appDataDir.isDirectory()) {
                        // ${APPDATA}\{vendorId}\${applicationId}
                        String path = vendorId + "\\" + applicationId + "\\";
                        directory = new File(appDataDir, path);
                    } else {
                        // ${userHome}\Application Data\${vendorId}\${applicationId}
                        String path = "Application Data\\" + vendorId + "\\" + applicationId + "\\";
                        directory = new File(userHome, path);
                    }
                } else if (osId == OSId.OSX) {
                    // ${userHome}/Library/Application Support/${applicationId}
                    String path = "Library/Application Support/" + applicationId + "/";
                    directory = new File(userHome, path);
                } else {
                    // ${userHome}/.${applicationId}/
                    String path = "." + applicationId + "/";
                    directory = new File(userHome, path);
                }
            }
        }
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                if (!directory.exists()) {
                    throw new IOException("cannot create directory " + directory);
                }
            }
        }
        String ret = directory.getAbsolutePath();
        if (!ret.endsWith(File.separator)) {
            ret += File.separator;
        }
        return ret;
    }

    private static String getReplacementsFile() throws IOException {
        return getFFDecHome() + REPLACEMENTS_NAME;
    }

    private static String getConfigFile() throws IOException {
        return getFFDecHome() + CONFIG_NAME;
    }

    public static boolean isAddedToContextMenu() {
        if (!Platform.isWindows()) {
            return false;
        }
        try {
            final String classesPath = "Software\\Classes\\";
            if (!Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE, classesPath + ".swf")) {
                return false;
            }
            String clsName = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, classesPath + ".swf", "");
            if (clsName == null) {
                return false;
            }
            return Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE, classesPath + clsName + "\\shell\\ffdec");
        } catch (Win32Exception ex) {
            return false;
        }
    }

    public static String getAppDir() {
        String appDir = "";
        try {
            appDir = new File(URLDecoder.decode(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8")).getParentFile().getAbsolutePath();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(FlashPlayerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!appDir.endsWith("\\")) {
            appDir += "\\";
        }
        return appDir;
    }

    public static boolean addToContextMenu(boolean add) {
        if (add == isAddedToContextMenu()) {
            return true;
        }

        String appDir = getAppDir();
        String exeName = "ffdec.exe";
        final String classesPath = "Software\\Classes\\";

        try {
            // 1) Add to context menu of SWF
            if (!Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE, classesPath + ".swf")) {
                Advapi32Util.registryCreateKey(WinReg.HKEY_LOCAL_MACHINE, classesPath + ".swf");
                Advapi32Util.registrySetStringValue(WinReg.HKEY_LOCAL_MACHINE, classesPath + ".swf", "", "ShockwaveFlash.ShockwaveFlash");
            }

            String clsName = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, classesPath + ".swf", "");
            if (!Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE, classesPath + clsName)) {
                Advapi32Util.registryCreateKey(WinReg.HKEY_LOCAL_MACHINE, classesPath + clsName);
                Advapi32Util.registrySetStringValue(WinReg.HKEY_LOCAL_MACHINE, classesPath + clsName, "", "Flash Movie");
            }

            if (!Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE, classesPath + clsName + "\\shell")) {
                Advapi32Util.registryCreateKey(WinReg.HKEY_LOCAL_MACHINE, classesPath + clsName + "\\shell");
            }

            boolean exists = Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE, classesPath + clsName + "\\shell\\ffdec");

            if ((!exists) && add) { //add
                Advapi32Util.registryCreateKey(WinReg.HKEY_LOCAL_MACHINE, classesPath + clsName + "\\shell\\ffdec");
                Advapi32Util.registrySetStringValue(WinReg.HKEY_LOCAL_MACHINE, classesPath + clsName + "\\shell\\ffdec", "", "Open with FFDec");
                Advapi32Util.registryCreateKey(WinReg.HKEY_LOCAL_MACHINE, classesPath + clsName + "\\shell\\ffdec\\command");
                Advapi32Util.registrySetStringValue(WinReg.HKEY_LOCAL_MACHINE, classesPath + clsName + "\\shell\\ffdec\\command", "", "\"" + appDir + exeName + "\" \"%1\"");
            }
            if (exists && (!add)) { //remove
                Advapi32Util.registryDeleteKey(WinReg.HKEY_LOCAL_MACHINE, classesPath + clsName + "\\shell\\ffdec\\command");
                Advapi32Util.registryDeleteKey(WinReg.HKEY_LOCAL_MACHINE, classesPath + clsName + "\\shell\\ffdec");
            }

            exists = Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE, classesPath + "Applications\\" + exeName);
            if ((!exists) && add) { //add
                Advapi32Util.registryCreateKey(WinReg.HKEY_LOCAL_MACHINE, classesPath + "Applications\\" + exeName);
                Advapi32Util.registryCreateKey(WinReg.HKEY_LOCAL_MACHINE, classesPath + "Applications\\" + exeName + "\\shell");
                Advapi32Util.registryCreateKey(WinReg.HKEY_LOCAL_MACHINE, classesPath + "Applications\\" + exeName + "\\shell\\open");
                Advapi32Util.registrySetStringValue(WinReg.HKEY_LOCAL_MACHINE, classesPath + "Applications\\" + exeName + "\\shell\\open", "", "Open with FFDec");
                Advapi32Util.registryCreateKey(WinReg.HKEY_LOCAL_MACHINE, classesPath + "Applications\\" + exeName + "\\shell\\open\\command");
                Advapi32Util.registrySetStringValue(WinReg.HKEY_LOCAL_MACHINE, classesPath + "Applications\\" + exeName + "\\shell\\open\\command", "", "\"" + appDir + "ffdec.exe\" \"%1\"");

            }
            if (exists && (!add)) { //remove
                Advapi32Util.registryDeleteKey(WinReg.HKEY_LOCAL_MACHINE, classesPath + "Applications\\" + exeName + "\\shell\\open\\command");
                Advapi32Util.registryDeleteKey(WinReg.HKEY_LOCAL_MACHINE, classesPath + "Applications\\" + exeName + "\\shell\\open");
                Advapi32Util.registryDeleteKey(WinReg.HKEY_LOCAL_MACHINE, classesPath + "Applications\\" + exeName + "\\shell");
                Advapi32Util.registryDeleteKey(WinReg.HKEY_LOCAL_MACHINE, classesPath + "Applications\\" + exeName);
            }
            //2) Add to OpenWith list
            String mruList = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\.swf\\OpenWithList", "MRUList");
            if (mruList != null) {
                exists = false;
                char appChar = 0;
                for (int i = 0; i < mruList.length(); i++) {
                    String app = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\.swf\\OpenWithList", "" + mruList.charAt(i));
                    if (app.equals(exeName)) {
                        appChar = mruList.charAt(i);
                        exists = true;
                        break;
                    }
                }
                if ((!exists) && add) { //add
                    for (int c = 'a'; c <= 'z'; c++) {
                        if (mruList.indexOf(c) == -1) {
                            mruList += (char) c;
                            Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\.swf\\OpenWithList", "" + (char) c, exeName);
                            Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\.swf\\OpenWithList", "MRUList", mruList);
                            break;
                        }
                    }
                }
                if (exists && (!add)) { //remove
                    mruList = mruList.replace("" + appChar, "");
                    Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\.swf\\OpenWithList", "MRUList", mruList);
                    Advapi32Util.registryDeleteValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\.swf\\OpenWithList", "" + appChar);
                }
                return true;
            }

        } catch (Exception ex) {
            //Updating registry failed, try elevating rights
            SHELLEXECUTEINFO sei = new SHELLEXECUTEINFO();
            sei.fMask = 0x00000040;
            sei.lpVerb = new WString("runas");
            sei.lpFile = new WString(appDir + exeName);
            sei.lpParameters = new WString(add ? "-addtocontextmenu" : "-removefromcontextmenu");
            sei.nShow = WinUser.SW_NORMAL;
            Shell32.INSTANCE.ShellExecuteEx(sei);
            //Wait till exit
            Kernel32.INSTANCE.WaitForSingleObject(sei.hProcess, 1000 * 60 * 60 * 24 /*1 day max*/);
        }
        return false;
    }
}
