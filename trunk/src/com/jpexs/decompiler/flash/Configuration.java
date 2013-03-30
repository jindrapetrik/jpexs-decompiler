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
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class Configuration {

    private static final String CONFIG_NAME = "config.bin";
    private static final String REPLACEMENTS_NAME = "replacements.cfg";
    private static HashMap<String, Object> config = new HashMap<String, Object>();
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

    public static String getASDecHome() {
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
            directory.mkdirs();
        }
        String ret = directory.getAbsolutePath();
        if (!ret.endsWith(File.separator)) {
            ret += File.separator;
        }
        return ret;
    }

    private static String getReplacementsFile() {
        return getASDecHome() + REPLACEMENTS_NAME;
    }

    private static String getConfigFile() {
        return getASDecHome() + CONFIG_NAME;
    }
    /**
     * List of replacements
     */
    public static java.util.List<Replacement> replacements = new ArrayList<Replacement>();

    /**
     * Saves replacements to file for future use
     */
    private static void saveReplacements() {
        try {
            if (replacements.isEmpty()) {
                File rf = new File(getReplacementsFile());
                if (rf.exists()) {
                    rf.delete();
                }
            } else {
                File f = new File(getASDecHome());
                if (!f.exists()) {
                    f.mkdir();
                }
                PrintWriter pw = new PrintWriter(new FileWriter(getReplacementsFile()));
                for (Replacement r : replacements) {
                    pw.println(r.urlPattern);
                    pw.println(r.targetFile);
                }
                pw.close();
            }
        } catch (IOException e) {
        }
    }

    /**
     * Load replacements from file
     */
    private static void loadReplacements() {
        replacements = new ArrayList<Replacement>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(getReplacementsFile()));
            String s;
            while ((s = br.readLine()) != null) {
                Replacement r = new Replacement(s, br.readLine());
                replacements.add(r);
            }
            br.close();
        } catch (IOException e) {
        }
    }

    public static Object getConfig(String cfg) {
        return getConfig(cfg, null);
    }

    public static Object getConfig(String cfg, Object defaultValue) {
        if (!config.containsKey(cfg)) {
            return defaultValue;
        }
        return config.get(cfg);
    }

    public static Object setConfig(String cfg, Object value) {
        return config.put(cfg, value);
    }

    public static void load() {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(getConfigFile()));
            config = (HashMap<String, Object>) ois.readObject();
        } catch (FileNotFoundException ex) {
        } catch (ClassNotFoundException cnf) {
        } catch (IOException ex) {
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException ex1) {
                    //ignore
                }
            }
        }
        loadReplacements();
    }

    public static void save() {
        File f = new File(getASDecHome());
        if (!f.exists()) {
            f.mkdir();
        }
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(getConfigFile()));
            oos.writeObject(config);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Cannot save configuration.", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(SWFInputStream.class.getName()).severe("Configuration directory is read only.");
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException ex1) {
                    //ignore
                }
            }
        }
        saveReplacements();
    }

    public static List<Replacement> getReplacements() {
        return replacements;
    }
}
