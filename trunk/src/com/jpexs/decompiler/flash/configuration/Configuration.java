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
package com.jpexs.decompiler.flash.configuration;

import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.proxy.Replacement;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Calendar;
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

    public static final boolean DISPLAY_FILENAME = true;
    public static boolean DEBUG_COPY = false;
    public static boolean dump_tags = false;
    /**
     * Debug mode = throwing an error when comparing original file and
     * recompiled
     */
    public static boolean debugMode = false;
    /**
     * Turn off reading unsafe tags (tags which can cause problems with
     * recompiling)
     */
    public static boolean DISABLE_DANGEROUS = false;
    /**
     * Turn off resolving constants in ActionScript 2
     */
    public static final boolean RESOLVE_CONSTANTS = true;
    /**
     * Find latest constant pool in the code
     */
    public static final boolean LATEST_CONSTANTPOOL_HACK = false;
    /**
     * Limit of code subs (for obfuscated code)
     */
    public static final int SUBLIMITER = 500;
    /**
     * Decompilation timeout in seconds
     */
    public static final int DECOMPILATION_TIMEOUT = 30 * 60;
    /**
     * Decompilation timeout in seconds for a single file
     */
    public static final int DECOMPILATION_TIMEOUT_FILE = 5 * 60;
    //using parameter names in decompiling may cause problems because official programs like Flash CS 5.5 inserts wrong parameter names indices
    public static final boolean PARAM_NAMES_ENABLE = false;
    /**
     * List of replacements
     */
    private static java.util.List<Replacement> replacements = new ArrayList<>();
    private static HashMap<String, Object> configDefaults = new HashMap<String, Object>() {
        {
            put("decompile", true);
            put("parallelSpeedUp", true);
            put("autoDeobfuscate", true);
            put("cacheOnDisk", true);
            put("internalFlashViewer", false);
            put("gotoMainClassOnStartup", false);
            put("deobfuscateUsePrevTagOnly", true);
            put("decompilationTimeoutSingleMethod", 60);
            put("lastSaveDir", ".");
            put("lastOpenDir", ".");
            put("offeredAssociation", false);
            put("locale", "en");
            put("lastUpdatesCheckDate", null);
            put("gui.window.width", 1000);
            put("gui.window.height", 700);
            put("gui.window.maximized.horizontal", false);
            put("gui.window.maximized.vertical", false);
            put("lastRenameType", 1);
            put("removeNops", true);
        }
    };

    public static final ConfigurationItem<Boolean> decompile = null;
    public static final ConfigurationItem<Boolean> parallelSpeedUp = null;
    public static final ConfigurationItem<Boolean> autoDeobfuscate = null;
    public static final ConfigurationItem<Boolean> cacheOnDisk = null;
    public static final ConfigurationItem<Boolean> internalFlashViewer = null;
    public static final ConfigurationItem<Boolean> gotoMainClassOnStartup = null;
    public static final ConfigurationItem<Boolean> deobfuscateUsePrevTagOnly = null;
    public static final ConfigurationItem<Boolean> offeredAssociation = null;
    public static final ConfigurationItem<Boolean> removeNops = null;

    public static final ConfigurationItem<Integer> decompilationTimeoutSingleMethod = null;
    public static final ConfigurationItem<Integer> xxx = null;
    public static final ConfigurationItem<Integer> lastRenameType = null;

    public static final ConfigurationItem<String> lastSaveDir = null;
    public static final ConfigurationItem<String> lastOpenDir = null;
    public static final ConfigurationItem<String> lastExportDir = null;
    public static final ConfigurationItem<String> locale = null;
    
    public static final ConfigurationItem<Calendar> lastUpdatesCheckDate = null;

    @ConfigurationName(name = "gui.window.width")
    public static final ConfigurationItem<Integer> guiWindowWidth = null;
    @ConfigurationName(name = "gui.window.height")
    public static final ConfigurationItem<Integer> guiWindowHeight = null;
    @ConfigurationName(name = "gui.window.maximized.horizontal")
    public static final ConfigurationItem<Boolean> guiWindowMaximizedHorizontal = null;
    @ConfigurationName(name = "gui.window.maximized.vertical")
    public static final ConfigurationItem<Boolean> guiWindowMaximizedVertical = null;
    @ConfigurationName(name = "gui.avm2.splitPane.dividerLocation")
    public static final ConfigurationItem<Integer> guiAvm2SplitPaneDividerLocation = null;
    @ConfigurationName(name = "guiActionSplitPaneDividerLocation")
    public static final ConfigurationItem<Integer> guiActionSplitPaneDividerLocation = null;
    @ConfigurationName(name = "gui.splitPane1.dividerLocation")
    public static final ConfigurationItem<Integer> guiSplitPane1DividerLocation = null;
    @ConfigurationName(name = "gui.splitPane2.dividerLocation")
    public static final ConfigurationItem<Integer> guiSplitPane2DividerLocation = null;
    
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
                String applicationId = ApplicationInfo.shortApplicationName;
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
                    String vendorId = ApplicationInfo.vendor;
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
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(replacementsFile), "utf-8"))) {
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
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(replacementsFile), "utf-8"))) {
            String s;
            while ((s = br.readLine()) != null) {
                Replacement r = new Replacement(s, br.readLine());
                replacements.add(r);
            }
        } catch (IOException e) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, "Error during load replacements", e);
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
        } catch (FileNotFoundException ex) {
        } catch (ClassNotFoundException cnf) {
        } catch (IOException ex) {
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
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(config);
        } catch (IOException ex) {
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
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static {
        setConfigurationFields();
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

                Object defaultValue = configDefaults.get(name);

                if (config.containsKey(name)) {
                    field.set(null, new ConfigurationItem(field.getName(), defaultValue, config.get(name)));
                } else {
                    field.set(null, new ConfigurationItem(field.getName(), defaultValue));
                }
            }
        } catch (IOException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static Map<String, Field> getConfigurationFields() {
        Field[] fields = Configuration.class.getFields();
        Map<String, Field> result = new HashMap<>();
        for (Field field : fields) {
            if (ConfigurationItem.class.isAssignableFrom(field.getType())) {
                ConfigurationName annotation = (ConfigurationName) field.getAnnotation(ConfigurationName.class);
                String name = annotation == null ? field.getName() : annotation.name();
                result.put(name, field);
            }
        }
        return result;
    }
}
