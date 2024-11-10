/*
 * Copyright (C) 2024 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.soleditor;

import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for accessing storage of Shared objects (.SOL files)
 *
 * @author JPEXS
 */
public class SharedObjectsStorage {

    public static boolean watchingPaused = false;
    
    public static Map<WatchKey, File> watchedCookieDirectories = new HashMap<>();
    
    
    private static Map<File, List<CookiesChangedListener>> swfFileToListeners = new LinkedHashMap<>();
    
    public static void addChangedListener(File file, CookiesChangedListener listener) {
        if (!swfFileToListeners.containsKey(file)) {
            swfFileToListeners.put(file, new ArrayList<>());
        }
        swfFileToListeners.get(file).add(listener);
        
        File solDir = getSolDirectoryForLocalFile(file);
        if (solDir == null) {
            return;
        }
        while (!solDir.exists()) {
            solDir = solDir.getParentFile();
        }
        if (!watchedCookieDirectories.containsValue(solDir)) {
            watchDir(solDir);
        }
    }

    public static void removeChangedListener(File file, CookiesChangedListener listener) {
        if (!swfFileToListeners.containsKey(file)) {
            return;
        }
        swfFileToListeners.get(file).remove(listener);
    }

    
    private static void watchDir(File dir) {
        try {
            WatchKey key = dir.toPath().register(Main.getWatcher(), StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            watchedCookieDirectories.put(key, dir);
        } catch (IOException ex) {
            //ignored
            //ex.printStackTrace();
        }        
    }
    
    private enum OSId {
        WINDOWS, OSX, UNIX
    }

    private static OSId getOSId() {
        String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if ((OS.contains("mac")) || (OS.contains("darwin"))) {
            return OSId.OSX;
        } else if (OS.contains("win")) {
            return OSId.WINDOWS;
        } else {
            return OSId.UNIX;
        }
    }

    public static List<File> getSolFilesForLocalFile(File file) {
        File solDirectory = getSolDirectoryForLocalFile(file);
        if (solDirectory == null) {
            return new ArrayList<>();
        }
        if (!solDirectory.exists()) {
            return new ArrayList<>();
        }
        File[] retArr = solDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().toLowerCase(Locale.ENGLISH).endsWith(".sol");
            }
        });
        return Arrays.asList(retArr);
    }

    public static List<File> getSolFilesForUrl(String url, FlashPlayerApi api) {
        File solDirectory = getSolDirectoryForUrl(url, api);
        if (solDirectory == null) {
            return new ArrayList<>();
        }
        File[] retArr = solDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().toLowerCase(Locale.ENGLISH).endsWith(".sol");
            }
        });
        return Arrays.asList(retArr);
    }

    public static File getSolDirectoryForLocalFile(File file) {
        File parentDir = getDirectory();
        if (parentDir == null) {
            return null;
        }
        String absPath = file.getAbsolutePath();
        absPath = absPath.replace("\\", "/");
        if (absPath.startsWith("//?/") || absPath.startsWith("//./")) {
            absPath = absPath.substring(4);
        } else if (absPath.matches("^[a-zA-Z]:/.*")) {
            absPath = absPath.substring(3);
        } else if (absPath.startsWith("/")) {
            absPath = absPath.substring(1);
        }

        absPath = absPath.replace("/", File.separator);

        return new File(parentDir, "localhost" + File.separator + encodeLocalPath(absPath));
    }

    private static String encodeUrl(String path) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c > 127) {
                byte[] bytes = Utf8Helper.getBytes("" + c);
                for (int b = 0; b < bytes.length; b++) {
                    int d = bytes[b] & 0xFF;
                    String hex = String.format("%02X", d);
                    sb.append("%").append(hex);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String encodeLocalPath(String path) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c > 127) {
                byte[] bytes = Utf8Helper.getBytes("" + c);
                for (int b = 0; b < bytes.length; b++) {
                    int d = bytes[b] & 0xFF;
                    String hex = String.format("%02X", d);
                    hex = hex.charAt(0) + "#" + hex.charAt(1);
                    sb.append(hex);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static File getSolDirectoryForUrl(String url, FlashPlayerApi api) {
        File parentDir = getDirectory(api);
        if (parentDir == null) {
            return null;
        }

        Pattern urlPattern = Pattern.compile("^https?://(?<host>[^/]+)/(?<path>[^?#]+)(\\?.*)?(#.*)?$");
        Matcher m = urlPattern.matcher(url);
        if (!m.matches()) {
            throw new IllegalArgumentException("Not a valid URL: " + url);
        }
        String host = m.group("host");
        String path = m.group("path");
        path = path.replace("/", File.separator);

        if (host.equals("localhost")) {
            host = "#" + host;
        }

        return new File(parentDir, host + File.separator + encodeUrl(path));
    }

    public static File getDirectory() {
        return getDirectory(FlashPlayerApi.NPAPI);
    }

    public static File getDirectory(FlashPlayerApi api) {
        switch (api) {
            case NPAPI:
                return getNpApiDirectory();
            case PPAPI:
                return getPpApiDirectory();
        }
        return null;
    }

    public static File getPpApiDirectory() {
        String userHome = null;
        try {
            userHome = System.getProperty("user.home");
        } catch (SecurityException ignore) {
            //ignored
        }

        File sharedObjectsDir = null;

        switch (getOSId()) {
            case WINDOWS:
                File winLocalAppDataDir = null;
                try {
                    String appDataEV = System.getenv("LOCALAPPDATA");
                    if ((appDataEV != null) && (appDataEV.length() > 0)) {
                        winLocalAppDataDir = new File(appDataEV);
                    }
                } catch (SecurityException ignore) {
                    //ignored
                }
                if (winLocalAppDataDir == null) {
                    return null;
                }
                sharedObjectsDir = new File(winLocalAppDataDir, "Google\\Chrome\\User Data\\Default\\Pepper Data\\Shockwave Flash\\WritableRoot\\#SharedObjects");
                break;
            case OSX:
                if (userHome == null) {
                    return null;
                }
                sharedObjectsDir = new File(userHome, "Library/Application Support/Google/Chrome/Default/Pepper Data/Shockwave Flash/WritableRoot/#SharedObjects");
                break;
            case UNIX:
                if (userHome == null) {
                    return null;
                }
                sharedObjectsDir = new File(userHome, ".config/google-chrome/Default/Pepper Data/Shockwave Flash/WritableRoot/#SharedObjects");
                break;
        }

        if (sharedObjectsDir == null) {
            return null;
        }
        if (!sharedObjectsDir.exists()) {
            return null;
        }
        File[] subDirs = sharedObjectsDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        if (subDirs.length == 0) {
            return null;
        }

        return subDirs[0];
    }

    public static File getNpApiDirectory() {
        String userHome = null;
        try {
            userHome = System.getProperty("user.home");
        } catch (SecurityException ignore) {
            //ignored
        }

        File sharedObjectsDir = null;

        switch (getOSId()) {
            case WINDOWS:
                File winAppDataDir = null;
                try {
                    String appDataEV = System.getenv("APPDATA");
                    if ((appDataEV != null) && (appDataEV.length() > 0)) {
                        winAppDataDir = new File(appDataEV);
                    }
                } catch (SecurityException ignore) {
                    //ignored
                }
                if ((winAppDataDir != null) && winAppDataDir.isDirectory()) {
                    sharedObjectsDir = new File(winAppDataDir, "Macromedia\\Flash Player\\#SharedObjects");
                } else {
                    if (userHome == null) {
                        return null;
                    }
                    sharedObjectsDir = new File(userHome, "Application Data\\Macromedia\\Flash Player\\#SharedObjects");
                }
                break;
            case OSX:
                if (userHome == null) {
                    return null;
                }
                sharedObjectsDir = new File(userHome, "Library/Preferences/Macromedia/Flash Player/#SharedObjects");
                break;
            case UNIX:
                if (userHome == null) {
                    return null;
                }
                sharedObjectsDir = new File(userHome, ".macromedia/Flash_Player/#SharedObjects");
                break;
        }

        if (sharedObjectsDir == null) {
            return null;
        }
        if (!sharedObjectsDir.exists()) {
            return null;
        }
        File[] subDirs = sharedObjectsDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        if (subDirs.length == 0) {
            return null;
        }

        return subDirs[0];
    }
    
    public static void watchedDirectoryChanged(File file) {        
        if (watchingPaused) {
            return;
        }
        
        List<File> swfFiles = new ArrayList<>(swfFileToListeners.keySet());
        for (File swfFile : swfFiles) {
            File solDir = getSolDirectoryForLocalFile(swfFile);
            if (file.equals(solDir) || file.getParentFile().equals(solDir)) {
                fireChanged(swfFile, getSolFilesForLocalFile(swfFile));
            } else {                
                if (solDir.exists()) {
                    continue;
                }
                while (!solDir.exists()) {
                    solDir = solDir.getParentFile();
                }
                if (solDir.equals(file)) {
                    if (!watchedCookieDirectories.containsValue(file)) {
                        watchDir(file);
                    }
                }
            }
        }
    }
    
    private static void fireChanged(File swfFile, List<File> files) {
        //System.err.println("- firing changed " + swfFile.getAbsolutePath());
        List<CookiesChangedListener> listeners = swfFileToListeners.get(swfFile);
        if (listeners != null) {
            listeners = new ArrayList<>(listeners);
            for (CookiesChangedListener l:listeners) {
                l.cookiesChanged(swfFile, files);
            }
        }
    }
}
