/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.configuration;

import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.exporters.modes.ExeExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.importers.TextImportResizeTextBoundsMode;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 *
 * @author JPEXS
 */
public class Configuration {

    private static final String CONFIG_NAME = "config.bin";

    private static final File unspecifiedFile = new File("unspecified");

    private static File directory = unspecifiedFile;

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

    @ConfigurationDefaultInt(10)
    @ConfigurationCategory("decompilation")
    public static final ConfigurationItem<Integer> parallelSpeedUpThreadCount = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static final ConfigurationItem<Boolean> autoDeobfuscate = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("")
    public static final ConfigurationItem<Boolean> deobfuscationOldMode = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("")
    public static final ConfigurationItem<Boolean> cacheOnDisk = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("")
    public static final ConfigurationItem<Boolean> cacheImages = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("display")
    public static final ConfigurationItem<Boolean> internalFlashViewer = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("display")
    public static final ConfigurationItem<Boolean> dumpView = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("display")
    public static final ConfigurationItem<Boolean> useHexColorFormat = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("display")
    public static final ConfigurationItem<Boolean> showOldTextDuringTextEditing = null;

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

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("export")
    public static final ConfigurationItem<Boolean> overwriteExistingFiles = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("export")
    public static final ConfigurationItem<Boolean> openFolderAfterFlaExport = null;

    @ConfigurationCategory("export")
    public static final ConfigurationItem<String> overrideTextExportFileName = null;

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

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("debug")
    public static final ConfigurationItem<Boolean> showDebugMenu = null;

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

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("debug")
    public static final ConfigurationItem<Boolean> setFFDecVersionInExportedFont = null;

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

    @ConfigurationDefaultInt(15)
    public static final ConfigurationItem<Integer> maxRecentFileCount = null;

    public static final ConfigurationItem<String> recentFiles = null;

    public static final ConfigurationItem<HashMap<String, String>> fontPairingMap = null;

    public static final ConfigurationItem<HashMap<String, SwfSpecificConfiguration>> swfSpecificConfigs = null;

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

    @ConfigurationDefaultDouble(0.5)
    @ConfigurationName("gui.avm2.splitPane.dividerLocationPercent")
    public static final ConfigurationItem<Double> guiAvm2SplitPaneDividerLocationPercent = null;

    @ConfigurationDefaultDouble(0.5)
    @ConfigurationName("gui.actionSplitPane.dividerLocationPercent")
    public static final ConfigurationItem<Double> guiActionSplitPaneDividerLocationPercent = null;

    @ConfigurationDefaultDouble(0.5)
    @ConfigurationName("gui.previewSplitPane.dividerLocationPercent")
    public static final ConfigurationItem<Double> guiPreviewSplitPaneDividerLocationPercent = null;

    @ConfigurationDefaultDouble(0.3333333333)
    @ConfigurationName("gui.splitPane1.dividerLocationPercent")
    public static final ConfigurationItem<Double> guiSplitPane1DividerLocationPercent = null;

    @ConfigurationDefaultDouble(0.6)
    @ConfigurationName("gui.splitPane2.dividerLocationPercent")
    public static final ConfigurationItem<Double> guiSplitPane2DividerLocationPercent = null;

    @ConfigurationDefaultDouble(0.5)
    @ConfigurationName("gui.timeLineSplitPane.dividerLocationPercent")
    public static final ConfigurationItem<Double> guiTimeLineSplitPaneDividerLocationPercent = null;

    @ConfigurationDefaultString("com.jpexs.decompiler.flash.gui.OceanicSkin")
    @ConfigurationName("gui.skin")
    @ConfigurationCategory("ui")
    public static final ConfigurationItem<String> guiSkin = null;

    @ConfigurationDefaultInt(3)
    @ConfigurationCategory("export")
    public static final ConfigurationItem<Integer> saveAsExeScaleMode = null;

    @ConfigurationCategory("export")
    public static final ConfigurationItem<ExeExportMode> exeExportMode = null;

    @ConfigurationDefaultInt(1024 * 1024/*1MiB*/)
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

    @ConfigurationCategory("update")
    public static final ConfigurationItem<String> updateProxyAddress = null;

    @ConfigurationDefaultString("")
    @ConfigurationName("export.formats")
    public static final ConfigurationItem<String> lastSelectedExportFormats = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("export")
    public static final ConfigurationItem<Boolean> textExportSingleFile = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("export")
    public static final ConfigurationItem<Boolean> scriptExportSingleFile = null;

    @ConfigurationDefaultString("--- SEPARATOR ---")
    @ConfigurationCategory("export")
    public static final ConfigurationItem<String> textExportSingleFileSeparator = null;

    @ConfigurationDefaultString("--- RECORDSEPARATOR ---")
    @ConfigurationCategory("export")
    public static final ConfigurationItem<String> textExportSingleFileRecordSeparator = null;

    @ConfigurationCategory("import")
    public static final ConfigurationItem<TextImportResizeTextBoundsMode> textImportResizeTextBoundsMode = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationName("warning.experimental.as12edit")
    @ConfigurationCategory("script")
    public static final ConfigurationItem<Boolean> warningExperimentalAS12Edit = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationName("warning.experimental.as3edit")
    @ConfigurationCategory("script")
    public static final ConfigurationItem<Boolean> warningExperimentalAS3Edit = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static final ConfigurationItem<Boolean> showCodeSavedMessage = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static final ConfigurationItem<Boolean> showTraitSavedMessage = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("export")
    public static final ConfigurationItem<Boolean> packJavaScripts = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("export")
    public static final ConfigurationItem<Boolean> textExportExportFontFace = null;

    @ConfigurationDefaultInt(128)
    public static final ConfigurationItem<Integer> lzmaFastBytes = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("debug")
    public static final ConfigurationItem<Boolean> showMethodBodyId = null;

    @ConfigurationDefaultDouble(1.0)
    @ConfigurationName("export.zoom")
    public static final ConfigurationItem<Double> lastSelectedExportZoom = null;

    public static final ConfigurationItem<String> pluginPath = null;

    @ConfigurationDefaultInt(55556)
    @ConfigurationCategory("script")
    public static final ConfigurationItem<Integer> debuggerPort = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static final ConfigurationItem<Boolean> randomDebuggerPackage = null;

    @ConfigurationDefaultBoolean(true)
    public static final ConfigurationItem<Boolean> displayDebuggerInfo = null;

    @ConfigurationDefaultString("debugConsole")
    public static final ConfigurationItem<String> lastDebuggerReplaceFunction = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static final ConfigurationItem<Boolean> getLocalNamesFromDebugInfo = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("ui")
    public static final ConfigurationItem<Boolean> tagTreeShowEmptyFolders = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("ui")
    public static final ConfigurationItem<Boolean> autoLoadEmbeddedSwfs = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static final ConfigurationItem<Boolean> showCloseConfirmation = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("ui")
    public static final ConfigurationItem<Boolean> editorMode = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("ui")
    public static final ConfigurationItem<Boolean> autoSaveTagModifications = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static final ConfigurationItem<Boolean> saveSessionOnExit = null;

    public static final ConfigurationItem<String> lastSessionFiles = null;

    public static final ConfigurationItem<String> lastSessionFileTitles = null;

    public static final ConfigurationItem<String> lastSessionSelection = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static final ConfigurationItem<Boolean> loopMedia = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static final ConfigurationItem<Boolean> allowOnlyOneInstance = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static final ConfigurationItem<Boolean> ignoreCLikePackages = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static final ConfigurationItem<Boolean> smartNumberFormatting = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static final ConfigurationItem<Boolean> enableScriptInitializerDisplay = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("ui")
    public static final ConfigurationItem<Boolean> autoOpenLoadedSWFs = null;

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

    public static Map<String, String> getFontToNameMap() {
        HashMap<String, String> map = fontPairingMap.get();
        if (map == null) {
            map = new HashMap<>();
            fontPairingMap.set(map);
        }

        return map;
    }

    public static void addFontPair(String fileName, int fontId, String fontName, String installedName) {
        Map<String, String> fontPairs = getFontToNameMap();
        fontPairs.put(fontName, installedName);

        SwfSpecificConfiguration swfConf = getOrCreateSwfSpecificConfiguration(fileName);
        swfConf.fontPairingMap.put(fontId + "_" + fontName, installedName);
    }

    public static SwfSpecificConfiguration getSwfSpecificConfiguration(String fileName) {
        HashMap<String, SwfSpecificConfiguration> map = swfSpecificConfigs.get();
        if (map == null) {
            map = new HashMap<>();
            swfSpecificConfigs.set(map);
        }

        return map.get(fileName);
    }

    public static SwfSpecificConfiguration getOrCreateSwfSpecificConfiguration(String fileName) {
        SwfSpecificConfiguration swfConf = getSwfSpecificConfiguration(fileName);
        if (swfConf == null) {
            swfConf = new SwfSpecificConfiguration();
            swfSpecificConfigs.get().put(fileName, swfConf);
        }

        return swfConf;
    }

    private static String getConfigFile() throws IOException {
        return getFFDecHome() + CONFIG_NAME;
    }

    private static HashMap<String, Object> loadFromFile(String file) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {

            @SuppressWarnings("unchecked")
            HashMap<String, Object> cfg = (HashMap<String, Object>) ois.readObject();
            return cfg;
        } catch (ClassNotFoundException | IOException ex) {
            // ignore
        }

        return new HashMap<>();
    }

    private static void saveToFile(String file) {
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
    }

    public static void saveConfig() {
        try {
            saveToFile(getConfigFile());
        } catch (IOException ex) {
            // ignore
        }
    }

    static {
        setConfigurationFields();
        if (useDetailedLogging.get()) {
            logLevel = Level.FINEST;
        } else if (debugMode.get()) {
            logLevel = Level.INFO;
        } else {
            logLevel = Level.WARNING;
        }
        //limit paralel threads?
        //int processorCount = Runtime.getRuntime().availableProcessors();

        if (lastUpdatesCheckDate.get() == null) {
            GregorianCalendar mingc = new GregorianCalendar();
            mingc.setTime(new Date(Long.MIN_VALUE));
            lastUpdatesCheckDate.set(mingc);
        }
    }

    @SuppressWarnings("unchecked")
    public static void setConfigurationFields() {
        try {
            HashMap<String, Object> config = loadFromFile(getConfigFile());
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

                    Class<?> type;
                    Type type2 = ((ParameterizedType) (field.getGenericType())).getActualTypeArguments()[0];
                    if (type2 instanceof Class<?>) {
                        type = (Class<?>) type2;
                    } else {
                        type = (Class<?>) ((ParameterizedType) type2).getRawType();
                    }
                    if (value != null && !type.isAssignableFrom(value.getClass())) {
                        System.out.println("Configuration item has a wrong type: " + name + " expected: " + type.getSimpleName() + " actual: " + value.getClass().getSimpleName());
                        value = null;
                    }
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
        ConfigurationDefaultDouble aDouble = field.getAnnotation(ConfigurationDefaultDouble.class);
        if (aDouble != null) {
            defaultValue = aDouble.value();
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

    public static int getParallelThreadCount() {
        int count = parallelSpeedUpThreadCount.get();
        if (count < 2) {
            count = 2;
        }

        return count;
    }

    public static File getPath(String folder) {
        String home = getFFDecHome();
        File dir = new File(home + folder);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getFlashLibPath() {
        return getPath("flashlib");
    }

    public static File getProjectorPath() {
        return getPath("projector");
    }

    private static byte[] downloadsUrl(String urlString) throws IOException {
        String proxyAddress = Configuration.updateProxyAddress.get();
        URL url = new URL(urlString);

        URLConnection uc = null;
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

        return Helper.readStream(uc.getInputStream());
    }

    private static String getDownloadsHtml() throws IOException {
        byte[] data = downloadsUrl("https://www.adobe.com/support/flashplayer/downloads.html");
        String html = new String(data, Utf8Helper.charset);
        return html;
    }

    private static String getUrlFromDownloadsHtml(String urlPatternString) {
        try {
            String html = getDownloadsHtml();
            Pattern urlPattern = Pattern.compile(urlPatternString, Pattern.DOTALL);
            Matcher matcher = urlPattern.matcher(html);
            if (matcher.matches()) {
                String url = matcher.group(1);
                int a = url.length();
                return url;
            }

            return null;
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private static String getLatestPlayerGlobalUrl() {
        return getUrlFromDownloadsHtml(".*<a href=\"([^\"]*playerglobal[^\"]*\\.swc)\".*");
    }

    private static String getLatestProjectorUrlWin() {
        return getUrlFromDownloadsHtml(".*<a href=\"([^\"]*flashplayer[^\"]*_sa\\.exe)\".*");
    }

    private static String getLatestProjectorUrlMac() {
        return getUrlFromDownloadsHtml(".*<a href=\"([^\"]*flashplayer[^\"]*_sa\\.dmg)\".*");
    }

    private static String getLatestProjectorUrlLinux() {
        // This is a compressed file
        return getUrlFromDownloadsHtml(".*<a href=\"([^\"]*flashplayer[^\"]*_sa\\.i386\\.tar\\.gz)\".*");
    }

    public static File getPlayerSWC() {
        File libsDir = getFlashLibPath();
        if (libsDir != null && libsDir.exists()) {
            File[] libs = libsDir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().startsWith("playerglobal");
                }
            });
            List<String> libNames = new ArrayList<>();
            for (File f : libs) {
                libNames.add(f.getName());
            }
            Collections.sort(libNames);
            if (!libNames.isEmpty()) {
                return new File(libsDir.getAbsolutePath() + File.separator + libNames.get(libNames.size() - 1));
            } else {
                return null;
            }
        }

        return null;
    }

    public static File getProjectorFile(ExeExportMode exportMode) {
        File projectoDir = getProjectorPath();
        if (projectoDir != null && projectoDir.exists()) {
            File[] projectors = projectoDir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    switch (exportMode) {
                        case PROJECTOR_WIN:
                            return name.toLowerCase().endsWith(".exe");
                        case PROJECTOR_MAC:
                            return name.toLowerCase().endsWith(".dmg");
                        case PROJECTOR_LINUX:
                            return name.toLowerCase().endsWith(".gz");
                    }

                    return false;
                }
            });
            List<String> projectorNames = new ArrayList<>();
            for (File f : projectors) {
                projectorNames.add(f.getName());
            }
            Collections.sort(projectorNames);
            if (!projectorNames.isEmpty()) {
                return new File(projectoDir.getAbsolutePath() + File.separator + projectorNames.get(projectorNames.size() - 1));
            } else {
                return null;
            }
        }

        return null;
    }
}
