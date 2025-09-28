/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
import java.io.File;
import java.util.Locale;

/**
 * App directory provider
 * @author JPEXS
 */
public class AppDirectoryProvider {

    private static final File UNSPECIFIED_FILE = new File("unspecified");

    private static File directory = UNSPECIFIED_FILE;

    private enum OSId {
        WINDOWS, OSX, UNIX
    }

    private static OSId getOSId() {
        String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
            return OSId.OSX;
        } else if (OS.indexOf("win") >= 0) {
            return OSId.WINDOWS;
        } else {
            return OSId.UNIX;
        }
    }

    /**
     * Get FFDec home directory
     *
     * @return FFDec home directory
     */
    public static String getFFDecHome() {
        if (directory == UNSPECIFIED_FILE) {
            directory = null;
            String userHome = null;
            try {
                userHome = System.getProperty("user.home");
            } catch (SecurityException ignore) {
                //ignored
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
                        //ignored
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
                    File xdgConfigHome = null;
                    File oldConfigDir = new File(userHome, "." + applicationId + "/");
                    try {
                        String xdgConfigHomeEV = System.getenv("XDG_CONFIG_HOME");
                        if ((xdgConfigHomeEV != null) && (xdgConfigHomeEV.length() > 0)) {
                            xdgConfigHome = new File(xdgConfigHomeEV);
                        }
                    } catch (SecurityException ignore) {
                        //ignored
                    }
                    if ((xdgConfigHome != null) && xdgConfigHome.isDirectory()) {
                        // ${xdgConfigHome}/${applicationId}
                        String path = applicationId + "/";
                        directory = new File(xdgConfigHome, path);
                    } else if (oldConfigDir.isDirectory()) {
                        // ${userHome}/.${applicationId}
                        directory = oldConfigDir;
                    } else {
                        // ${userHome}/.config/${applicationId}
                        String path = ".config/" + applicationId + "/";
                        directory = new File(userHome, path);
                    }
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
}
