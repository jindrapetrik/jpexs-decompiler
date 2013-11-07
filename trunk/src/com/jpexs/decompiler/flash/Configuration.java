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
package com.jpexs.decompiler.flash;

import com.jpexs.proxy.Replacement;
import java.io.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private static HashMap<String, Object> config = new HashMap<>();
    /**
     * List of replacements
     */
    public static java.util.List<Replacement> replacements = new ArrayList<>();
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

    public static boolean containsConfig(String cfg) {
        return config.containsKey(cfg);
    }

    public static <T> T getConfig(String cfg) {
        T defaultValue = null;
        if (configDefaults.containsKey(cfg)) {
            @SuppressWarnings("unchecked")
            T def = (T) configDefaults.get(cfg);
            defaultValue = def;
        }
        return getConfig(cfg, defaultValue);
    }

    public static <T> T getConfig(String cfg, T defaultValue) {
        if (!config.containsKey(cfg)) {
            return defaultValue;
        }
        @SuppressWarnings("unchecked")
        T result = (T) config.get(cfg);
        return result;
    }

    public static <T> T setConfig(String cfg, T value) {
        if (cfg.equals("paralelSpeedUp")) {
            cfg = "parallelSpeedUp";
        }
        @SuppressWarnings("unchecked")
        T result = (T) config.put(cfg, value);
        return result;
    }

    private static String getReplacementsFile() throws IOException {
        return getFFDecHome() + REPLACEMENTS_NAME;
    }

    private static String getConfigFile() throws IOException {
        return getFFDecHome() + CONFIG_NAME;
    }

    @SuppressWarnings("unchecked")
    public static void loadFromFile(String file, String replacementsFile) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {

            config = (HashMap<String, Object>) ois.readObject();
        } catch (FileNotFoundException ex) {
        } catch (ClassNotFoundException cnf) {
        } catch (IOException ex) {
        }
        if (replacementsFile != null) {
            loadReplacements(replacementsFile);
        }
        if (containsConfig("paralelSpeedUp")) {
            setConfig("parallelSpeedUp", getConfig("paralelSpeedUp"));
            config.remove("paralelSpeedUp");
        }
    }

    private static void saveToFile(String file, String replacementsFile) {
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

    public static void loadConfig() throws IOException {
        loadFromFile(getConfigFile(), getReplacementsFile());
    }

    public static void saveConfig() {
        try {
            saveToFile(getConfigFile(), getReplacementsFile());
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
