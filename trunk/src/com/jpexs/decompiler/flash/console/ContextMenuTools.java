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
package com.jpexs.decompiler.flash.console;

import com.jpexs.helpers.utf8.Utf8Helper;
import com.sun.jna.Platform;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.SHELLEXECUTEINFO;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.WinUser;
import java.io.File;

/**
 *
 * @author JPEXS
 */
public class ContextMenuTools {
    
    public static String getAppDir() {
        String path = Utf8Helper.urlDecode(ContextMenuTools.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String appDir = new File(path).getParentFile().getAbsolutePath();
        if (!appDir.endsWith("\\")) {
            appDir += "\\";
        }
        return appDir;
    }

    public static boolean isAddedToContextMenu() {
        if (!Platform.isWindows()) {
            return false;
        }
        final WinReg.HKEY REG_CLASSES_HKEY = WinReg.HKEY_LOCAL_MACHINE;
        final String REG_CLASSES_PATH = "Software\\Classes\\";
        try {
            if (!Advapi32Util.registryKeyExists(REG_CLASSES_HKEY, REG_CLASSES_PATH + ".swf")) {
                return false;
            }
            String clsName = Advapi32Util.registryGetStringValue(REG_CLASSES_HKEY, REG_CLASSES_PATH + ".swf", "");
            if (clsName == null) {
                return false;
            }
            return Advapi32Util.registryKeyExists(REG_CLASSES_HKEY, REG_CLASSES_PATH + clsName + "\\shell\\ffdec");
        } catch (Win32Exception ex) {
            return false;
        }
    }

    public static boolean addToContextMenu(boolean add) {
        if (add == isAddedToContextMenu()) {
            return true;
        }

        String exeName32 = "ffdec.exe";
        String exeName64 = "ffdec64.exe";
        
        if (add) {
            String exeName = Platform.is64Bit() ? exeName64 : exeName32;
            return addToContextMenu(add, exeName);
        } else {
            // remove both 32 and 64 bit references
            addToContextMenu(add, exeName32);
            return addToContextMenu(add, exeName64);
        }
    }
    
    private static boolean addToContextMenu(boolean add, String exeName) {
        final String extensions[] = new String[]{"swf", "gfx"};

        final WinReg.HKEY REG_CLASSES_HKEY = WinReg.HKEY_LOCAL_MACHINE;
        final String REG_CLASSES_PATH = "Software\\Classes\\";

        String appDir = getAppDir();
        String verb = "ffdec";
        String verbName = "Open with FFDec";
        boolean exists;
        try {

            exists = Advapi32Util.registryKeyExists(REG_CLASSES_HKEY, REG_CLASSES_PATH + "Applications\\" + exeName);
            if ((!exists) && add) { //add
                Advapi32Util.registryCreateKey(REG_CLASSES_HKEY, REG_CLASSES_PATH + "Applications\\" + exeName);
                Advapi32Util.registryCreateKey(REG_CLASSES_HKEY, REG_CLASSES_PATH + "Applications\\" + exeName + "\\shell");
                Advapi32Util.registryCreateKey(REG_CLASSES_HKEY, REG_CLASSES_PATH + "Applications\\" + exeName + "\\shell\\open");
                Advapi32Util.registrySetStringValue(REG_CLASSES_HKEY, REG_CLASSES_PATH + "Applications\\" + exeName + "\\shell\\open", "", verbName);
                Advapi32Util.registryCreateKey(REG_CLASSES_HKEY, REG_CLASSES_PATH + "Applications\\" + exeName + "\\shell\\open\\command");
                Advapi32Util.registrySetStringValue(REG_CLASSES_HKEY, REG_CLASSES_PATH + "Applications\\" + exeName + "\\shell\\open\\command", "", "\"" + appDir + exeName + "\" \"%1\"");

            }

            for (String ext : extensions) {

                // 1) Add to context menu of SWF
                if (!Advapi32Util.registryKeyExists(REG_CLASSES_HKEY, REG_CLASSES_PATH + "." + ext)) {
                    Advapi32Util.registryCreateKey(REG_CLASSES_HKEY, REG_CLASSES_PATH + "." + ext);
                    Advapi32Util.registrySetStringValue(REG_CLASSES_HKEY, REG_CLASSES_PATH + "." + ext, "", "ShockwaveFlash.ShockwaveFlash");
                }

                String clsName = Advapi32Util.registryGetStringValue(REG_CLASSES_HKEY, REG_CLASSES_PATH + "." + ext, "");
                if (!Advapi32Util.registryKeyExists(REG_CLASSES_HKEY, REG_CLASSES_PATH + clsName)) {
                    Advapi32Util.registryCreateKey(REG_CLASSES_HKEY, REG_CLASSES_PATH + clsName);
                    Advapi32Util.registrySetStringValue(REG_CLASSES_HKEY, REG_CLASSES_PATH + clsName, "", "Flash Movie");
                }

                if (!Advapi32Util.registryKeyExists(REG_CLASSES_HKEY, REG_CLASSES_PATH + clsName + "\\shell")) {
                    Advapi32Util.registryCreateKey(REG_CLASSES_HKEY, REG_CLASSES_PATH + clsName + "\\shell");
                }

                exists = Advapi32Util.registryKeyExists(REG_CLASSES_HKEY, REG_CLASSES_PATH + clsName + "\\shell\\" + verb);

                if ((!exists) && add) { //add
                    Advapi32Util.registryCreateKey(REG_CLASSES_HKEY, REG_CLASSES_PATH + clsName + "\\shell\\" + verb);
                    Advapi32Util.registrySetStringValue(REG_CLASSES_HKEY, REG_CLASSES_PATH + clsName + "\\shell\\" + verb, "", verbName);
                    Advapi32Util.registryCreateKey(REG_CLASSES_HKEY, REG_CLASSES_PATH + clsName + "\\shell\\" + verb + "\\command");
                    Advapi32Util.registrySetStringValue(REG_CLASSES_HKEY, REG_CLASSES_PATH + clsName + "\\shell\\" + verb + "\\command", "", "\"" + appDir + exeName + "\" \"%1\"");
                }
                if (exists && (!add)) { //remove
                    Advapi32Util.registryDeleteKey(REG_CLASSES_HKEY, REG_CLASSES_PATH + clsName + "\\shell\\" + verb + "\\command");
                    Advapi32Util.registryDeleteKey(REG_CLASSES_HKEY, REG_CLASSES_PATH + clsName + "\\shell\\" + verb);
                }

                if (exists && (!add)) { //remove
                    Advapi32Util.registryDeleteKey(REG_CLASSES_HKEY, REG_CLASSES_PATH + "Applications\\" + exeName + "\\shell\\open\\command");
                    Advapi32Util.registryDeleteKey(REG_CLASSES_HKEY, REG_CLASSES_PATH + "Applications\\" + exeName + "\\shell\\open");
                    Advapi32Util.registryDeleteKey(REG_CLASSES_HKEY, REG_CLASSES_PATH + "Applications\\" + exeName + "\\shell");
                    Advapi32Util.registryDeleteKey(REG_CLASSES_HKEY, REG_CLASSES_PATH + "Applications\\" + exeName);
                }
                //2) Add to OpenWith list
                String mruList = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\." + ext + "\\OpenWithList", "MRUList");
                if (mruList != null) {
                    exists = false;
                    char appChar = 0;
                    for (int i = 0; i < mruList.length(); i++) {
                        String app = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\." + ext + "\\OpenWithList", "" + mruList.charAt(i));
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
                                Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\." + ext + "\\OpenWithList", "" + (char) c, exeName);
                                Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\." + ext + "\\OpenWithList", "MRUList", mruList);
                                break;
                            }
                        }
                    }
                    if (exists && (!add)) { //remove
                        mruList = mruList.replace("" + appChar, "");
                        Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\." + ext + "\\OpenWithList", "MRUList", mruList);
                        Advapi32Util.registryDeleteValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\." + ext + "\\OpenWithList", "" + appChar);
                    }
                }

                //On some systems, file must be associated in SystemFileAssociations too
                if (Advapi32Util.registryKeyExists(REG_CLASSES_HKEY, REG_CLASSES_PATH + "SystemFileAssociations")) {
                    exists = Advapi32Util.registryKeyExists(REG_CLASSES_HKEY, REG_CLASSES_PATH + "SystemFileAssociations\\." + ext + "\\Shell\\" + verb);
                    if ((!exists) && add) { //add
                        if (!Advapi32Util.registryKeyExists(REG_CLASSES_HKEY, REG_CLASSES_PATH + "SystemFileAssociations\\." + ext + "")) {
                            Advapi32Util.registryCreateKey(REG_CLASSES_HKEY, REG_CLASSES_PATH + "SystemFileAssociations\\." + ext + "");
                        }
                        if (!Advapi32Util.registryKeyExists(REG_CLASSES_HKEY, REG_CLASSES_PATH + "SystemFileAssociations\\." + ext + "\\Shell")) {
                            Advapi32Util.registryCreateKey(REG_CLASSES_HKEY, REG_CLASSES_PATH + "SystemFileAssociations\\." + ext + "\\Shell");
                        }
                        Advapi32Util.registryCreateKey(REG_CLASSES_HKEY, REG_CLASSES_PATH + "SystemFileAssociations\\." + ext + "\\Shell\\" + verb);
                        Advapi32Util.registrySetStringValue(REG_CLASSES_HKEY, REG_CLASSES_PATH + "SystemFileAssociations\\." + ext + "\\Shell\\" + verb, "", verbName);
                        Advapi32Util.registryCreateKey(REG_CLASSES_HKEY, REG_CLASSES_PATH + "SystemFileAssociations\\." + ext + "\\Shell\\" + verb + "\\Command");
                        Advapi32Util.registrySetStringValue(REG_CLASSES_HKEY, REG_CLASSES_PATH + "SystemFileAssociations\\." + ext + "\\Shell\\" + verb + "\\Command", "", "\"" + appDir + exeName + "\" \"%1\"");
                    }
                    if (exists && (!add)) { //remove        
                        Advapi32Util.registryDeleteKey(REG_CLASSES_HKEY, REG_CLASSES_PATH + "SystemFileAssociations\\." + ext + "\\Shell\\" + verb + "\\Command");
                        Advapi32Util.registryDeleteKey(REG_CLASSES_HKEY, REG_CLASSES_PATH + "SystemFileAssociations\\." + ext + "\\Shell\\" + verb);
                    }
                }
            }
            return true;
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
