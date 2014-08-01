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
package com.jpexs.decompiler.flash.configuration;

import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8InputStreamReader;
import com.jpexs.helpers.utf8.Utf8OutputStreamWriter;
import com.jpexs.proxy.Replacement;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class Configuration {

    private static final String CONFIG_NAME = "config.bin";
    private static final String REPLACEMENTS_NAME = "replacements.cfg";
    private static final File unspecifiedFile = new File("unspecified");
    private static File directory = unspecifiedFile;

    /**
     * List of replacements
     */
    private static List<Replacement> replacements = new ArrayList<>();

    public static final Level logLevel;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static final ConfigurationItem<Boolean> openMultipleFiles = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static final ConfigurationItem<Boolean> decompile = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("decompilation")
    public static final ConfigurationItem<Boolean> parallelSpeedUp = null;

    @ConfigurationDefaultInt(20)
    @ConfigurationCategory("decompilation")
    public static final ConfigurationItem<Integer> parallelThreadCount = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static final ConfigurationItem<Boolean> autoDeobfuscate = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("decompilation")
    public static final ConfigurationItem<Boolean> cacheOnDisk = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("display")
    public static final ConfigurationItem<Boolean> internalFlashViewer = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("display")
    public static final ConfigurationItem<Boolean> dumpView = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("display")
    public static final ConfigurationItem<Boolean> dumpInfoCollecting = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("ui")
    public static final ConfigurationItem<Boolean> gotoMainClassOnStartup = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static final ConfigurationItem<Boolean> autoRenameIdentifiers = null;

    @ConfigurationDefaultBoolean(false)
    public static final ConfigurationItem<Boolean> offeredAssociation = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static final ConfigurationItem<Boolean> decimalAddress = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static final ConfigurationItem<Boolean> showAllAddresses = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("display")
    public static final ConfigurationItem<Boolean> useFrameCache = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static final ConfigurationItem<Boolean> useRibbonInterface = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("export")
    public static final ConfigurationItem<Boolean> openFolderAfterFlaExport = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("debug")
    public static final ConfigurationItem<Boolean> useDetailedLogging = null;

    /**
     * Debug mode = throwing an error when comparing original file and
     * recompiled
     */
    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("debug")
    public static final ConfigurationItem<Boolean> debugMode = null;
    /**
     * Turn off resolving constants in ActionScript 2
     */
    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static final ConfigurationItem<Boolean> resolveConstants = null;
    /**
     * Limit of code subs (for obfuscated code)
     */
    @ConfigurationDefaultInt(500)
    @ConfigurationCategory("limit")
    public static final ConfigurationItem<Integer> sublimiter = null;
    /**
     * Total export timeout in seconds
     */
    @ConfigurationDefaultInt(30 * 60)
    @ConfigurationCategory("limit")
    public static final ConfigurationItem<Integer> exportTimeout = null;
    /**
     * Decompilation timeout in seconds for a single file
     */
    @ConfigurationDefaultInt(5 * 60)
    @ConfigurationCategory("limit")
    public static final ConfigurationItem<Integer> decompilationTimeoutFile = null;
    /**
     * Using parameter names in decompiling may cause problems because official
     * programs like Flash CS 5.5 inserts wrong parameter names indices
     */
    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static final ConfigurationItem<Boolean> paramNamesEnable = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static final ConfigurationItem<Boolean> displayFileName = null;
    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("debug")
    public static final ConfigurationItem<Boolean> debugCopy = null;
    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("debug")
    public static final ConfigurationItem<Boolean> dumpTags = null;

    @ConfigurationDefaultInt(60)
    @ConfigurationCategory("limit")
    public static final ConfigurationItem<Integer> decompilationTimeoutSingleMethod = null;
    @ConfigurationDefaultInt(1)
    public static final ConfigurationItem<Integer> lastRenameType = null;

    @ConfigurationDefaultString(".")
    public static final ConfigurationItem<String> lastSaveDir = null;

    @ConfigurationDefaultString(".")
    public static final ConfigurationItem<String> lastOpenDir = null;

    @ConfigurationDefaultString(".")
    public static final ConfigurationItem<String> lastExportDir = null;

    @ConfigurationDefaultString("en")
    @ConfigurationCategory("ui")
    public static final ConfigurationItem<String> locale = null;

    @ConfigurationDefaultString("_loc%d_")
    @ConfigurationCategory("script")
    public static final ConfigurationItem<String> registerNameFormat = null;

    @ConfigurationDefaultInt(10)
    public static final ConfigurationItem<Integer> maxRecentFileCount = null;

    public static final ConfigurationItem<String> recentFiles = null;

    public static final ConfigurationItem<String> fontPairing = null;

    public static final ConfigurationItem<Calendar> lastUpdatesCheckDate = null;

    @ConfigurationDefaultInt(1000)
    @ConfigurationName("gui.window.width")
    public static final ConfigurationItem<Integer> guiWindowWidth = null;
    @ConfigurationDefaultInt(700)
    @ConfigurationName("gui.window.height")
    public static final ConfigurationItem<Integer> guiWindowHeight = null;
    @ConfigurationDefaultBoolean(false)
    @ConfigurationName("gui.window.maximized.horizontal")
    public static final ConfigurationItem<Boolean> guiWindowMaximizedHorizontal = null;
    @ConfigurationDefaultBoolean(false)
    @ConfigurationName("gui.window.maximized.vertical")
    public static final ConfigurationItem<Boolean> guiWindowMaximizedVertical = null;
    @ConfigurationName("gui.avm2.splitPane.dividerLocation")
    public static final ConfigurationItem<Integer> guiAvm2SplitPaneDividerLocation = null;
    @ConfigurationName("guiActionSplitPaneDividerLocation")
    public static final ConfigurationItem<Integer> guiActionSplitPaneDividerLocation = null;
    @ConfigurationName("guiPreviewSplitPaneDividerLocation")
    public static final ConfigurationItem<Integer> guiPreviewSplitPaneDividerLocation = null;
    @ConfigurationName("gui.splitPane1.dividerLocation")
    public static final ConfigurationItem<Integer> guiSplitPane1DividerLocation = null;
    @ConfigurationName("gui.splitPane2.dividerLocation")
    public static final ConfigurationItem<Integer> guiSplitPane2DividerLocation = null;
    @ConfigurationDefaultInt(3)
    @ConfigurationCategory("export")
    public static final ConfigurationItem<Integer> saveAsExeScaleMode = null;

    @ConfigurationDefaultInt(1024 * 100/*100KB*/)
    @ConfigurationCategory("limit")
    public static final ConfigurationItem<Integer> syntaxHighlightLimit = null;
    public static final ConfigurationItem<Integer> guiFontPreviewSampleText = null;
    @ConfigurationName("gui.fontPreviewWindow.width")
    public static final ConfigurationItem<Integer> guiFontPreviewWidth = null;
    @ConfigurationName("gui.fontPreviewWindow.height")
    public static final ConfigurationItem<Integer> guiFontPreviewHeight = null;
    @ConfigurationName("gui.fontPreviewWindow.posX")
    public static final ConfigurationItem<Integer> guiFontPreviewPosX = null;
    @ConfigurationName("gui.fontPreviewWindow.posY")
    public static final ConfigurationItem<Integer> guiFontPreviewPosY = null;

    @ConfigurationDefaultInt(3)
    @ConfigurationName("formatting.indent.size")
    @ConfigurationCategory("format")
    public static final ConfigurationItem<Integer> indentSize = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationName("formatting.indent.useTabs")
    @ConfigurationCategory("format")
    public static final ConfigurationItem<Boolean> indentUseTabs = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("format")
    public static final ConfigurationItem<Boolean> beginBlockOnNewLine = null;

    @ConfigurationDefaultInt(1000 * 60 * 60 * 24)
    @ConfigurationCategory("update")
    @ConfigurationName("check.updates.delay")
    public static final ConfigurationItem<Integer> checkForUpdatesDelay = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("update")
    @ConfigurationName("check.updates.stable")
    public static final ConfigurationItem<Boolean> checkForUpdatesStable = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("update")
    @ConfigurationName("check.updates.nightly")
    public static final ConfigurationItem<Boolean> checkForUpdatesNightly = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("update")
    @ConfigurationName("check.updates.enabled")
    public static final ConfigurationItem<Boolean> checkForUpdatesAuto = null;

    @ConfigurationDefaultString("")
    @ConfigurationName("export.formats")
    public static final ConfigurationItem<String> lastSelectedExportFormats = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("export")
    public static final ConfigurationItem<Boolean> textExportSingleFile = null;

    @ConfigurationDefaultString("--- SEPARATOR ---")
    @ConfigurationCategory("export")
    public static final ConfigurationItem<String> textExportSingleFileSeparator = null;

    @ConfigurationDefaultString("--- RECORDSEPARATOR ---")
    @ConfigurationCategory("export")
    public static final ConfigurationItem<String> textExportSingleFileRecordSeparator = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationName("warning.experimental.as12edit")
    @ConfigurationCategory("script")
    public static final ConfigurationItem<Boolean> warningExperimentalAS12Edit = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationName("warning.experimental.as3edit")
    @ConfigurationCategory("script")
    public static final ConfigurationItem<Boolean> warningExperimentalAS3Edit = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("export")
    public static final ConfigurationItem<Boolean> packJavaScripts = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("export")
    public static final ConfigurationItem<Boolean> textExportExportFontFace = null;

    @ConfigurationDefaultInt(128)
    public static final ConfigurationItem<Integer> lzmaFastBytes = null;

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

    public static String getFFDecHome() {
        if (directory == unspecifiedFile) {
            directory = null;
            String userHome = null;
            try {
                userHome = System.getProperty("user.home");
            } catch (SecurityException ignore) {
            }
            if (userHome != null) {
                String applicationId = ApplicationInfo.SHORT_APPLICATION_NAME;
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
                    String vendorId = ApplicationInfo.VENDOR;
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
            } else {
                //no home, then use application directory
                directory = new File(".");
            }
        }
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                if (!directory.exists()) {
                    directory = new File("."); //fallback to current directory
                }
            }
        }
        String ret = directory.getAbsolutePath();
        if (!ret.endsWith(File.separator)) {
            ret += File.separator;
        }
        return ret;
    }

    public static List<String> getRecentFiles() {
        String files = recentFiles.get();
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(files.split("::"));
    }

    public static void addRecentFile(String path) {
        List<String> recentFilesArray = new ArrayList<>(getRecentFiles());
        int idx = recentFilesArray.indexOf(path);
        if (idx != -1) {
            recentFilesArray.remove(idx);
        }
        recentFilesArray.add(path);
        while (recentFilesArray.size() > maxRecentFileCount.get()) {
            recentFilesArray.remove(0);
        }
        recentFiles.set(Helper.joinStrings(recentFilesArray, "::"));
    }

    public static void removeRecentFile(String path) {
        List<String> recentFilesArray = new ArrayList<>(getRecentFiles());
        int idx = recentFilesArray.indexOf(path);
        if (idx != -1) {
            recentFilesArray.remove(idx);
        }
        recentFiles.set(Helper.joinStrings(recentFilesArray, "::"));
    }

    public static Map<String, String> getFontPairs() {
        String fonts = fontPairing.get();
        if (fonts == null) {
            return new HashMap<>();
        }

        Map<String, String> result = new HashMap<>();
        for (String pair : fonts.split("::")) {
            String[] splittedPair = pair.split("=");
            result.put(splittedPair[0], splittedPair[1]);
        }
        return result;
    }

    public static void addFontPair(String fileName, int fontId, String fontName, String systemFontName) {
        String key = fileName + "_" + fontId + "_" + fontName;
        Map<String, String> fontPairs = getFontPairs();
        fontPairs.put(key, systemFontName);
        fontPairs.put(fontName, systemFontName);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Entry<String, String> pair : fontPairs.entrySet()) {
            if (i != 0) {
                sb.append("::");
            }
            sb.append(pair.getKey()).append("=").append(pair.getValue());
            i++;
        }
        fontPairing.set(sb.toString());
    }

    /**
     * Saves replacements to file for future use
     */
    private static void saveReplacements(String replacementsFile) {
        if (replacements.isEmpty()) {
            File rf = new File(replacementsFile);
            if (rf.exists()) {
                if (!rf.delete()) {
                    Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, "Cannot delete replacements file");
                }
            }
        } else {
            try (PrintWriter pw = new PrintWriter(new Utf8OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(replacementsFile))))) {
                for (Replacement r : replacements) {
                    pw.println(r.urlPattern);
                    pw.println(r.targetFile);
                }
            } catch (IOException ex) {
                Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, "Exception during saving replacements", ex);
            }
        }
    }

    /**
     * Load replacements from file
     */
    private static void loadReplacements(String replacementsFile) {
        if (!(new File(replacementsFile)).exists()) {
            return;
        }
        replacements = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new Utf8InputStreamReader(new FileInputStream(replacementsFile)))) {
            String s;
            while ((s = br.readLine()) != null) {
                Replacement r = new Replacement(s, br.readLine());
                replacements.add(r);
            }
        } catch (IOException e) {
            //ignore
        }
    }

    private static String getReplacementsFile() throws IOException {
        return getFFDecHome() + REPLACEMENTS_NAME;
    }

    private static String getConfigFile() throws IOException {
        return getFFDecHome() + CONFIG_NAME;
    }

    private static HashMap<String, Object> loadFromFile(String file, String replacementsFile) {
        HashMap<String, Object> config = new HashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {

            @SuppressWarnings("unchecked")
            HashMap<String, Object> cfg = (HashMap<String, Object>) ois.readObject();
            config = cfg;
        } catch (ClassNotFoundException | IOException ex) {
            //ignore
        }
        if (replacementsFile != null) {
            loadReplacements(replacementsFile);
        }
        if (config.containsKey("paralelSpeedUp")) {
            config.put("parallelSpeedUp", config.get("paralelSpeedUp"));
            config.remove("paralelSpeedUp");
        }
        return config;
    }

    private static void saveToFile(String file, String replacementsFile) {
        HashMap<String, Object> config = new HashMap<>();
        for (Entry<String, Field> entry : getConfigurationFields().entrySet()) {
            try {
                String name = entry.getKey();
                Field field = entry.getValue();
                ConfigurationItem item = (ConfigurationItem) field.get(null);
                if (item.hasValue) {
                    config.put(name, item.get());
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            oos.writeObject(config);
        } catch (IOException ex) {
            //TODO: move this to GUI
            JOptionPane.showMessageDialog(null, "Cannot save configuration.", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Configuration.class.getName()).severe("Configuration directory is read only.");
        }
        if (replacementsFile != null) {
            saveReplacements(replacementsFile);
        }
    }

    public static List<Replacement> getReplacements() {
        return replacements;
    }

    public static void saveConfig() {
        try {
            saveToFile(getConfigFile(), getReplacementsFile());
        } catch (IOException ex) {
            //ignore
        }
    }

    static {
        setConfigurationFields();
        if (useDetailedLogging.get() || debugMode.get()) {
            logLevel = Level.CONFIG;
        } else {
            logLevel = Level.WARNING;
        }
        int processorCount = Runtime.getRuntime().availableProcessors();
        if (parallelThreadCount.get() > processorCount) {
            parallelThreadCount.set(processorCount);
        }

        if (lastUpdatesCheckDate.get() == null) {
            GregorianCalendar mingc = new GregorianCalendar();
            mingc.setTime(new Date(Long.MIN_VALUE));
            lastUpdatesCheckDate.set(mingc);
        }
    }

    @SuppressWarnings("unchecked")
    public static void setConfigurationFields() {
        try {
            HashMap<String, Object> config = loadFromFile(getConfigFile(), getReplacementsFile());
            for (Entry<String, Field> entry : getConfigurationFields().entrySet()) {
                String name = entry.getKey();
                Field field = entry.getValue();
                // remove final modifier from field
                Field modifiersField = field.getClass().getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

                Object defaultValue = getDefaultValue(field);
                Object value = null;
                if (config.containsKey(name)) {
                    value = config.get(name);
                }

                if (value != null) {
                    field.set(null, new ConfigurationItem(name, defaultValue, value));
                } else {
                    field.set(null, new ConfigurationItem(name, defaultValue));
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
            // Reflection exceptions. This should never happen
            throw new Error(ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Object getDefaultValue(Field field) {
        Object defaultValue = null;
        ConfigurationDefaultBoolean aBool = field.getAnnotation(ConfigurationDefaultBoolean.class);
        if (aBool != null) {
            defaultValue = aBool.value();
        }
        ConfigurationDefaultInt aInt = field.getAnnotation(ConfigurationDefaultInt.class);
        if (aInt != null) {
            defaultValue = aInt.value();
        }
        ConfigurationDefaultString aString = field.getAnnotation(ConfigurationDefaultString.class);
        if (aString != null) {
            defaultValue = aString.value();
        }
        return defaultValue;
    }

    public static Map<String, Field> getConfigurationFields() {
        Field[] fields = Configuration.class.getFields();
        Map<String, Field> result = new HashMap<>();
        for (Field field : fields) {
            if (ConfigurationItem.class.isAssignableFrom(field.getType())) {
                ConfigurationName annotation = field.getAnnotation(ConfigurationName.class);
                String name = annotation == null ? field.getName() : annotation.value();
                result.put(name, field);
            }
        }
        return result;
    }

    public static CodeFormatting getCodeFormatting() {
        CodeFormatting ret = new CodeFormatting();
        String indentString = "";
        for (int i = 0; i < indentSize.get(); i++) {
            indentString += indentUseTabs.get() ? "\t" : " ";
        }
        ret.indentString = indentString;
        ret.beginBlockOnNewLine = beginBlockOnNewLine.get();
        return ret;
    }

    public static File getFlashLibPath() {
        String home = getFFDecHome();
        File libsdir = new File(home + "flashlib");
        if (!libsdir.exists()) {
            libsdir.mkdirs();
        }
        return libsdir;
    }

    public static File getPlayerSWC() {
        File libsdir = getFlashLibPath();
        if (libsdir != null && libsdir.exists()) {
            File libs[] = libsdir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().startsWith("playerglobal");
                }
            });
            List<String> libnames = new ArrayList<>();
            for (File f : libs) {
                libnames.add(f.getName());
            }
            Collections.sort(libnames);
            if (!libnames.isEmpty()) {
                return new File(libsdir.getAbsolutePath() + File.separator + libnames.get(libnames.size() - 1));
            } else {
                return null;
            }
        }

        return null;
    }
}
