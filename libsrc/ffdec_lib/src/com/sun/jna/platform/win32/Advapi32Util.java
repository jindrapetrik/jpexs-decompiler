/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.sun.jna.platform.win32;

/**
 * @author JPEXS
 */
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT.EVENTLOGRECORD;
import com.sun.jna.platform.win32.WinReg.HKEY;
import com.sun.jna.platform.win32.WinReg.HKEYByReference;
import com.sun.jna.ptr.IntByReference;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Advapi32 utility API.
 *
 * @author dblock[at]dblock.org
 */
public abstract class Advapi32Util {

    /**
     * Constructor.
     */
    private Advapi32Util() {

    }

    /**
     * An account.
     */
    public static class Account {

        /**
         * Account name.
         */
        public String name;

        /**
         * Account domain.
         */
        public String domain;

        /**
         * Account SID.
         */
        public byte[] sid;

        /**
         * String representation of the account SID.
         */
        public String sidString;

        /**
         * Account type, one of SID_NAME_USE.
         */
        public int accountType;

        /**
         * Fully qualified account name.
         */
        public String fqn;
        
        /**
         * Constructor.
         */
        public Account() {
            
        }
    }

    /**
     * Checks whether a registry key exists.
     * @param root HKEY_LOCAL_MACHINE, etc.
     * @param key Path to the registry key.
     * @return True if the key exists.
     */
    public static boolean registryKeyExists(HKEY root, String key) {
        return registryKeyExists(root, key, false);
    }

    /**
     * Checks whether a registry key exists.
     *
     * @param root HKEY_LOCAL_MACHINE, etc.
     * @param key Path to the registry key.
     * @param use64BitKey True if the key is 64-bit.
     * @return True if the key exists.
     */
    public static boolean registryKeyExists(HKEY root, String key, boolean use64BitKey) {
        HKEYByReference phkKey = new HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, key, 0, WinNT.KEY_READ | (use64BitKey ? 0 : WinNT.KEY_WOW64_32KEY), phkKey);
        switch (rc) {
            case W32Errors.ERROR_SUCCESS:
                Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
                return true;
            case W32Errors.ERROR_FILE_NOT_FOUND:
                return false;
            default:
                throw new Win32Exception(rc);
        }
    }

    /**
     * Checks whether a registry key exists.
     *
     * @param root HKEY_LOCAL_MACHINE, etc.
     * @param key Path to the registry key.
     * @param value Value
     * @return True if the key exists.
     */
    public static boolean registryValueExists(HKEY root, String key, String value) {
        return registryValueExists(root, key, value, false);
    }

    /**
     * Checks whether a registry value exists.
     *
     * @param root HKEY_LOCAL_MACHINE, etc.
     * @param key Registry key path.
     * @param value Value name.
     * @param use64bitKey Use 64 bit key
     * @return True if the value exists.
     */
    public static boolean registryValueExists(HKEY root, String key, String value, boolean use64bitKey) {
        HKEYByReference phkKey = new HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, key, 0, WinNT.KEY_READ | (use64bitKey ? 0 : WinNT.KEY_WOW64_32KEY), phkKey);
        try {
            switch (rc) {
                case W32Errors.ERROR_SUCCESS:
                    break;
                case W32Errors.ERROR_FILE_NOT_FOUND:
                    return false;
                default:
                    throw new Win32Exception(rc);
            }
            IntByReference lpcbData = new IntByReference();
            IntByReference lpType = new IntByReference();
            rc = Advapi32.INSTANCE.RegQueryValueEx(
                    phkKey.getValue(), value, 0, lpType, (char[]) null, lpcbData);
            switch (rc) {
                case W32Errors.ERROR_SUCCESS:
                case W32Errors.ERROR_INSUFFICIENT_BUFFER:
                    return true;
                case W32Errors.ERROR_FILE_NOT_FOUND:
                    return false;
                default:
                    throw new Win32Exception(rc);
            }
        } finally {
            if (phkKey.getValue() != null && phkKey.getValue() != WinBase.INVALID_HANDLE_VALUE) {
                rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
                if (rc != W32Errors.ERROR_SUCCESS) {
                    throw new Win32Exception(rc);
                }
            }
        }
    }

    /**
     * Get a registry REG_SZ value.
     *
     * @param root Root key.
     * @param key Registry path.
     * @param value Name of the value to retrieve.
     * @return String value.
     */
    public static String registryGetStringValue(HKEY root, String key, String value) {
        return registryGetStringValue(root, key, value, false);
    }

    /**
     * Get a registry REG_SZ value.
     *
     * @param root Root key.
     * @param key Registry path.
     * @param value Name of the value to retrieve.
     * @param use64bitKey Use 64 bit key
     * @return String value.
     */
    public static String registryGetStringValue(HKEY root, String key, String value, boolean use64bitKey) {
        HKEYByReference phkKey = new HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, key, 0, WinNT.KEY_READ | (use64bitKey ? 0 : WinNT.KEY_WOW64_32KEY), phkKey);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        try {
            IntByReference lpcbData = new IntByReference();
            IntByReference lpType = new IntByReference();
            rc = Advapi32.INSTANCE.RegQueryValueEx(
                    phkKey.getValue(), value, 0, lpType, (char[]) null, lpcbData);
            if (rc != W32Errors.ERROR_SUCCESS && rc != W32Errors.ERROR_INSUFFICIENT_BUFFER) {
                throw new Win32Exception(rc);
            }
            if (lpType.getValue() != WinNT.REG_SZ) {
                throw new RuntimeException("Unexpected registry type " + lpType.getValue() + ", expected REG_SZ");
            }
            char[] data = new char[lpcbData.getValue()];
            rc = Advapi32.INSTANCE.RegQueryValueEx(
                    phkKey.getValue(), value, 0, lpType, data, lpcbData);
            if (rc != W32Errors.ERROR_SUCCESS && rc != W32Errors.ERROR_INSUFFICIENT_BUFFER) {
                throw new Win32Exception(rc);
            }
            return Native.toString(data);
        } finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }
        }
    }

    /**
     * Get a registry REG_EXPAND_SZ value.
     *
     * @param root Root key.
     * @param key Registry path.
     * @param value Name of the value to retrieve.
     * @return String value.
     */
    public static String registryGetExpandableStringValue(HKEY root, String key, String value) {
        HKEYByReference phkKey = new HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, key, 0, WinNT.KEY_READ | WinNT.KEY_WOW64_32KEY, phkKey);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        try {
            IntByReference lpcbData = new IntByReference();
            IntByReference lpType = new IntByReference();
            rc = Advapi32.INSTANCE.RegQueryValueEx(
                    phkKey.getValue(), value, 0, lpType, (char[]) null, lpcbData);
            if (rc != W32Errors.ERROR_SUCCESS && rc != W32Errors.ERROR_INSUFFICIENT_BUFFER) {
                throw new Win32Exception(rc);
            }
            if (lpType.getValue() != WinNT.REG_EXPAND_SZ) {
                throw new RuntimeException("Unexpected registry type " + lpType.getValue() + ", expected REG_SZ");
            }
            char[] data = new char[lpcbData.getValue()];
            rc = Advapi32.INSTANCE.RegQueryValueEx(
                    phkKey.getValue(), value, 0, lpType, data, lpcbData);
            if (rc != W32Errors.ERROR_SUCCESS && rc != W32Errors.ERROR_INSUFFICIENT_BUFFER) {
                throw new Win32Exception(rc);
            }
            return Native.toString(data);
        } finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }
        }
    }

    /**
     * Get a registry REG_MULTI_SZ value.
     *
     * @param root Root key.
     * @param key Registry path.
     * @param value Name of the value to retrieve.
     * @return String value.
     */
    public static String[] registryGetStringArray(HKEY root, String key, String value) {
        HKEYByReference phkKey = new HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, key, 0, WinNT.KEY_READ | WinNT.KEY_WOW64_32KEY, phkKey);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        try {
            IntByReference lpcbData = new IntByReference();
            IntByReference lpType = new IntByReference();
            rc = Advapi32.INSTANCE.RegQueryValueEx(
                    phkKey.getValue(), value, 0, lpType, (char[]) null, lpcbData);
            if (rc != W32Errors.ERROR_SUCCESS && rc != W32Errors.ERROR_INSUFFICIENT_BUFFER) {
                throw new Win32Exception(rc);
            }
            if (lpType.getValue() != WinNT.REG_MULTI_SZ) {
                throw new RuntimeException("Unexpected registry type " + lpType.getValue() + ", expected REG_SZ");
            }
            Memory data = new Memory(lpcbData.getValue());
            rc = Advapi32.INSTANCE.RegQueryValueEx(
                    phkKey.getValue(), value, 0, lpType, data, lpcbData);
            if (rc != W32Errors.ERROR_SUCCESS && rc != W32Errors.ERROR_INSUFFICIENT_BUFFER) {
                throw new Win32Exception(rc);
            }
            ArrayList<String> result = new ArrayList<>();
            int offset = 0;
            while (offset < data.size()) {
                String s = data.getString(offset); //FIXME, true);
                offset += s.length() * Native.WCHAR_SIZE;
                offset += Native.WCHAR_SIZE;
                result.add(s);
            }
            return result.toArray(new String[result.size()]);
        } finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }
        }
    }

    /**
     * Get a registry REG_BINARY value.
     *
     * @param root Root key.
     * @param key Registry path.
     * @param value Name of the value to retrieve.
     * @return String value.
     */
    public static byte[] registryGetBinaryValue(HKEY root, String key, String value) {
        HKEYByReference phkKey = new HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, key, 0, WinNT.KEY_READ | WinNT.KEY_WOW64_32KEY, phkKey);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        try {
            IntByReference lpcbData = new IntByReference();
            IntByReference lpType = new IntByReference();
            rc = Advapi32.INSTANCE.RegQueryValueEx(
                    phkKey.getValue(), value, 0, lpType, (char[]) null, lpcbData);
            if (rc != W32Errors.ERROR_SUCCESS && rc != W32Errors.ERROR_INSUFFICIENT_BUFFER) {
                throw new Win32Exception(rc);
            }
            if (lpType.getValue() != WinNT.REG_BINARY) {
                throw new RuntimeException("Unexpected registry type " + lpType.getValue() + ", expected REG_BINARY");
            }
            byte[] data = new byte[lpcbData.getValue()];
            rc = Advapi32.INSTANCE.RegQueryValueEx(
                    phkKey.getValue(), value, 0, lpType, data, lpcbData);
            if (rc != W32Errors.ERROR_SUCCESS && rc != W32Errors.ERROR_INSUFFICIENT_BUFFER) {
                throw new Win32Exception(rc);
            }
            return data;
        } finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }
        }
    }

    /**
     * Get a registry DWORD value.
     *
     * @param root Root key.
     * @param key Registry key path.
     * @param value Name of the value to retrieve.
     * @return Integer value.
     */
    public static int registryGetIntValue(HKEY root, String key, String value) {
        HKEYByReference phkKey = new HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, key, 0, WinNT.KEY_READ | WinNT.KEY_WOW64_32KEY, phkKey);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        try {
            IntByReference lpcbData = new IntByReference();
            IntByReference lpType = new IntByReference();
            rc = Advapi32.INSTANCE.RegQueryValueEx(
                    phkKey.getValue(), value, 0, lpType, (char[]) null, lpcbData);
            if (rc != W32Errors.ERROR_SUCCESS && rc != W32Errors.ERROR_INSUFFICIENT_BUFFER) {
                throw new Win32Exception(rc);
            }
            if (lpType.getValue() != WinNT.REG_DWORD) {
                throw new RuntimeException("Unexpected registry type " + lpType.getValue() + ", expected REG_SZ");
            }
            IntByReference data = new IntByReference();
            rc = Advapi32.INSTANCE.RegQueryValueEx(
                    phkKey.getValue(), value, 0, lpType, data, lpcbData);
            if (rc != W32Errors.ERROR_SUCCESS && rc != W32Errors.ERROR_INSUFFICIENT_BUFFER) {
                throw new Win32Exception(rc);
            }
            return data.getValue();
        } finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }
        }
    }

    /**
     * Create a registry key.
     *
     * @param hKey Parent key.
     * @param keyName Key name.
     * @return True if the key was created, false otherwise.
     */
    public static boolean registryCreateKey(HKEY hKey, String keyName) {
        HKEYByReference phkResult = new HKEYByReference();
        IntByReference lpdwDisposition = new IntByReference();
        int rc = Advapi32.INSTANCE.RegCreateKeyEx(hKey, keyName, 0, null, WinNT.REG_OPTION_NON_VOLATILE,
                WinNT.KEY_READ, null, phkResult, lpdwDisposition);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        rc = Advapi32.INSTANCE.RegCloseKey(phkResult.getValue());
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        return WinNT.REG_CREATED_NEW_KEY == lpdwDisposition.getValue();
    }

    /**
     * Create a registry key.
     *
     * @param root Root key.
     * @param parentPath Path to an existing registry key.
     * @param keyName Key name.
     * @return True if the key was created, false otherwise.
     */
    public static boolean registryCreateKey(HKEY root, String parentPath, String keyName) {
        HKEYByReference phkKey = new HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, parentPath, 0, WinNT.KEY_CREATE_SUB_KEY, phkKey);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        try {
            return registryCreateKey(phkKey.getValue(), keyName);
        } finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }
        }
    }

    /**
     * Set an integer value in registry.
     *
     * @param hKey Parent key.
     * @param name Value name.
     * @param value Value to write to registry.
     */
    public static void registrySetIntValue(HKEY hKey, String name, int value) {
        byte[] data = new byte[4];
        data[0] = (byte) (value & 0xff);
        data[1] = (byte) ((value >> 8) & 0xff);
        data[2] = (byte) ((value >> 16) & 0xff);
        data[3] = (byte) ((value >> 24) & 0xff);
        int rc = Advapi32.INSTANCE.RegSetValueEx(hKey, name, 0, WinNT.REG_DWORD, data, 4);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
    }

    /**
     * Set an integer value in registry.
     *
     * @param root Root key.
     * @param keyPath Path to an existing registry key.
     * @param name Value name.
     * @param value Value to write to registry.
     */
    public static void registrySetIntValue(HKEY root, String keyPath, String name, int value) {
        HKEYByReference phkKey = new HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, keyPath, 0, WinNT.KEY_READ | WinNT.KEY_WRITE | WinNT.KEY_WOW64_32KEY, phkKey);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        try {
            registrySetIntValue(phkKey.getValue(), name, value);
        } finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }
        }
    }

    /**
     * Set a string value in registry.
     *
     * @param hKey Parent key.
     * @param name Value name.
     * @param value Value to write to registry.
     */
    public static void registrySetStringValue(HKEY hKey, String name, String value) {
        char[] data = Native.toCharArray(value);
        int rc = Advapi32.INSTANCE.RegSetValueEx(hKey, name, 0, WinNT.REG_SZ,
                data, data.length * Native.WCHAR_SIZE);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
    }

    /**
     * Set a string value in registry.
     *
     * @param root Root key.
     * @param keyPath Path to an existing registry key.
     * @param name Value name.
     * @param value Value to write to registry.
     */
    public static void registrySetStringValue(HKEY root, String keyPath, String name, String value) {
        HKEYByReference phkKey = new HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, keyPath, 0, WinNT.KEY_READ | WinNT.KEY_WRITE | WinNT.KEY_WOW64_32KEY, phkKey);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        try {
            registrySetStringValue(phkKey.getValue(), name, value);
        } finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }
        }
    }

    /**
     * Set an expandable string value in registry.
     *
     * @param hKey Parent key.
     * @param name Value name.
     * @param value Value to write to registry.
     */
    public static void registrySetExpandableStringValue(HKEY hKey, String name, String value) {
        char[] data = Native.toCharArray(value);
        int rc = Advapi32.INSTANCE.RegSetValueEx(hKey, name, 0, WinNT.REG_EXPAND_SZ,
                data, data.length * Native.WCHAR_SIZE);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
    }

    /**
     * Set a string value in registry.
     *
     * @param root Root key.
     * @param keyPath Path to an existing registry key.
     * @param name Value name.
     * @param value Value to write to registry.
     */
    public static void registrySetExpandableStringValue(HKEY root, String keyPath, String name, String value) {
        HKEYByReference phkKey = new HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, keyPath, 0, WinNT.KEY_READ | WinNT.KEY_WRITE | WinNT.KEY_WOW64_32KEY, phkKey);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        try {
            registrySetExpandableStringValue(phkKey.getValue(), name, value);
        } finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }
        }
    }

    /**
     * Set a string array value in registry.
     *
     * @param hKey Parent key.
     * @param name Name.
     * @param arr Array of strings to write to registry.
     */
    public static void registrySetStringArray(HKEY hKey, String name, String[] arr) {
        int size = 0;
        for (String s : arr) {
            size += s.length() * Native.WCHAR_SIZE;
            size += Native.WCHAR_SIZE;
        }

        int offset = 0;
        Memory data = new Memory(size);
        for (String s : arr) {
            data.setString(offset, s);//FIXME, true);
            offset += s.length() * Native.WCHAR_SIZE;
            offset += Native.WCHAR_SIZE;
        }

        int rc = Advapi32.INSTANCE.RegSetValueEx(hKey, name, 0, WinNT.REG_MULTI_SZ,
                data.getByteArray(0, size), size);

        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
    }

    /**
     * Set a string array value in registry.
     *
     * @param root Root key.
     * @param keyPath Path to an existing registry key.
     * @param name Value name.
     * @param arr Array of strings to write to registry.
     */
    public static void registrySetStringArray(HKEY root, String keyPath, String name, String[] arr) {
        HKEYByReference phkKey = new HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, keyPath, 0, WinNT.KEY_READ | WinNT.KEY_WRITE | WinNT.KEY_WOW64_32KEY, phkKey);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        try {
            registrySetStringArray(phkKey.getValue(), name, arr);
        } finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }
        }
    }

    /**
     * Set a binary value in registry.
     *
     * @param hKey Parent key.
     * @param name Value name.
     * @param data Data to write to registry.
     */
    public static void registrySetBinaryValue(HKEY hKey, String name, byte[] data) {
        int rc = Advapi32.INSTANCE.RegSetValueEx(hKey, name, 0, WinNT.REG_BINARY, data, data.length);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
    }

    /**
     * Set a binary value in registry.
     *
     * @param root Root key.
     * @param keyPath Path to an existing registry key.
     * @param name Value name.
     * @param data Data to write to registry.
     */
    public static void registrySetBinaryValue(HKEY root, String keyPath, String name, byte[] data) {
        HKEYByReference phkKey = new HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, keyPath, 0, WinNT.KEY_READ | WinNT.KEY_WRITE | WinNT.KEY_WOW64_32KEY, phkKey);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        try {
            registrySetBinaryValue(phkKey.getValue(), name, data);
        } finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }
        }
    }

    /**
     * Delete a registry key.
     *
     * @param hKey Parent key.
     * @param keyName Name of the key to delete.
     */
    public static void registryDeleteKey(HKEY hKey, String keyName) {
        int rc = Advapi32.INSTANCE.RegDeleteKey(hKey, keyName);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
    }

    /**
     * Delete a registry key.
     *
     * @param root Root key.
     * @param keyPath Path to an existing registry key.
     * @param keyName Name of the key to delete.
     */
    public static void registryDeleteKey(HKEY root, String keyPath, String keyName) {
        HKEYByReference phkKey = new HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, keyPath, 0, WinNT.KEY_READ | WinNT.KEY_WRITE | WinNT.KEY_WOW64_32KEY, phkKey);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        try {
            registryDeleteKey(phkKey.getValue(), keyName);
        } finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }
        }
    }

    /**
     * Delete a registry value.
     *
     * @param hKey Parent key.
     * @param valueName Name of the value to delete.
     */
    public static void registryDeleteValue(HKEY hKey, String valueName) {
        int rc = Advapi32.INSTANCE.RegDeleteValue(hKey, valueName);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
    }

    /**
     * Delete a registry value.
     *
     * @param root Root key.
     * @param keyPath Path to an existing registry key.
     * @param valueName Name of the value to delete.
     */
    public static void registryDeleteValue(HKEY root, String keyPath, String valueName) {
        HKEYByReference phkKey = new HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, keyPath, 0, WinNT.KEY_READ | WinNT.KEY_WRITE | WinNT.KEY_WOW64_32KEY, phkKey);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        try {
            registryDeleteValue(phkKey.getValue(), valueName);
        } finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }
        }
    }

    /**
     * Get names of the registry key's sub-keys.
     *
     * @param hKey Registry key.
     * @return Array of registry key names.
     */
    public static String[] registryGetKeys(HKEY hKey) {
        IntByReference lpcSubKeys = new IntByReference();
        IntByReference lpcMaxSubKeyLen = new IntByReference();
        int rc = Advapi32.INSTANCE.RegQueryInfoKey(hKey, null, null, null,
                lpcSubKeys, lpcMaxSubKeyLen, null, null, null, null, null, null);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        ArrayList<String> keys = new ArrayList<>(lpcSubKeys.getValue());
        char[] name = new char[lpcMaxSubKeyLen.getValue() + 1];
        for (int i = 0; i < lpcSubKeys.getValue(); i++) {
            IntByReference lpcchValueName = new IntByReference(lpcMaxSubKeyLen.getValue() + 1);
            rc = Advapi32.INSTANCE.RegEnumKeyEx(hKey, i, name, lpcchValueName,
                    null, null, null, null);
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }
            keys.add(Native.toString(name));
        }
        return keys.toArray(new String[keys.size()]);
    }

    /**
     * Get names of the registry key's sub-keys.
     *
     * @param root Root key.
     * @param keyPath Path to a registry key.
     * @return Array of registry key names.
     */
    public static String[] registryGetKeys(HKEY root, String keyPath) {
        HKEYByReference phkKey = new HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, keyPath, 0, WinNT.KEY_READ | WinNT.KEY_WOW64_32KEY, phkKey);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        try {
            return registryGetKeys(phkKey.getValue());
        } finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }
        }
    }

    /**
     * Get a table of registry values.
     *
     * @param hKey Registry key.
     * @return Table of values.
     */
    public static TreeMap<String, Object> registryGetValues(HKEY hKey) {
        IntByReference lpcValues = new IntByReference();
        IntByReference lpcMaxValueNameLen = new IntByReference();
        IntByReference lpcMaxValueLen = new IntByReference();
        int rc = Advapi32.INSTANCE.RegQueryInfoKey(hKey, null, null, null, null,
                null, null, lpcValues, lpcMaxValueNameLen, lpcMaxValueLen, null, null);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        TreeMap<String, Object> keyValues = new TreeMap<>();
        char[] name = new char[lpcMaxValueNameLen.getValue() + 1];
        byte[] data = new byte[lpcMaxValueLen.getValue()];
        for (int i = 0; i < lpcValues.getValue(); i++) {
            IntByReference lpcchValueName = new IntByReference(lpcMaxValueNameLen.getValue() + 1);
            IntByReference lpcbData = new IntByReference(lpcMaxValueLen.getValue());
            IntByReference lpType = new IntByReference();
            rc = Advapi32.INSTANCE.RegEnumValue(hKey, i, name, lpcchValueName, null,
                    lpType, data, lpcbData);
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }

            String nameString = Native.toString(name);

            Memory byteData = new Memory(lpcbData.getValue());
            byteData.write(0, data, 0, lpcbData.getValue());

            switch (lpType.getValue()) {
                case WinNT.REG_DWORD: {
                    keyValues.put(nameString, byteData.getInt(0));
                    break;
                }
                case WinNT.REG_SZ:
                case WinNT.REG_EXPAND_SZ: {
                    keyValues.put(nameString, byteData.getString(0)); //FIXME, true);
                    break;
                }
                case WinNT.REG_BINARY: {
                    keyValues.put(nameString, byteData.getByteArray(0, lpcbData.getValue()));
                    break;
                }
                case WinNT.REG_MULTI_SZ: {
                    Memory stringData = new Memory(lpcbData.getValue());
                    stringData.write(0, data, 0, lpcbData.getValue());
                    ArrayList<String> result = new ArrayList<>();
                    int offset = 0;
                    while (offset < stringData.size()) {
                        String s = stringData.getString(offset); //FIXME, true);
                        offset += s.length() * Native.WCHAR_SIZE;
                        offset += Native.WCHAR_SIZE;
                        result.add(s);
                    }
                    keyValues.put(nameString, result.toArray(new String[result.size()]));
                    break;
                }
                default:
                    throw new RuntimeException("Unsupported type: " + lpType.getValue());
            }
        }
        return keyValues;
    }

    /**
     * Get a table of registry values.
     *
     * @param root Registry root.
     * @param keyPath Registry key path.
     * @return Table of values.
     */
    public static TreeMap<String, Object> registryGetValues(HKEY root, String keyPath) {
        HKEYByReference phkKey = new HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, keyPath, 0, WinNT.KEY_READ | WinNT.KEY_WOW64_32KEY, phkKey);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        try {
            return registryGetValues(phkKey.getValue());
        } finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }
        }
    }

    /**
     * Converts a map of environment variables to an environment block suitable
     * for {@link Advapi32#CreateProcessAsUser}. This environment block consists
     * of null-terminated blocks of null-terminated strings. Each string is in
     * the following form: name=value\0
     *
     * @param environment Environment variables
     * @return A environment block
     */
    public static String getEnvironmentBlock(Map<String, String> environment) {
        StringBuilder out = new StringBuilder();
        for (Entry<String, String> entry : environment.entrySet()) {
            if (entry.getValue() != null) {
                out.append(entry.getKey()).append("=").append(entry.getValue()).append("\0");
            }
        }
        return out.toString() + "\0";
    }

    /**
     * Event log types.
     */
    public static enum EventLogType {

        /**
         * Error event.
         */
        Error,
        /**
         * Warning event.
         */
        Warning,
        /**
         * Informational event.
         */
        Informational,
        /**
         * Audit success event.
         */
        AuditSuccess,
        /**
         * Audit failure event.
         */
        AuditFailure
    }

    /**
     * An event log record.
     */
    public static class EventLogRecord {

        private EVENTLOGRECORD _record = null;

        private final String _source;

        private byte[] _data;

        private String[] _strings;

        /**
         * Raw record data.
         *
         * @return EVENTLOGRECORD.
         */
        public EVENTLOGRECORD getRecord() {
            return _record;
        }

        /**
         * Event Id.
         *
         * @return Integer.
         */
        public int getEventId() {
            return _record.EventID.intValue();
        }

        /**
         * Event source.
         *
         * @return String.
         */
        public String getSource() {
            return _source;
        }

        /**
         * Status code for the facility, part of the Event ID.
         *
         * @return Status code.
         */
        public int getStatusCode() {
            return _record.EventID.intValue() & 0xFFFF;
        }

        /**
         * Record number of the record. This value can be used with the
         * EVENTLOG_SEEK_READ flag in the ReadEventLog function to begin reading
         * at a specified record.
         *
         * @return Integer.
         */
        public int getRecordNumber() {
            return _record.RecordNumber.intValue();
        }

        /**
         * Record length, with data.
         *
         * @return Number of bytes in the record including data.
         */
        public int getLength() {
            return _record.Length.intValue();
        }

        /**
         * Strings associated with this event.
         *
         * @return Array of strings or null.
         */
        public String[] getStrings() {
            return _strings;
        }

        /**
         * Event log type.
         *
         * @return Event log type.
         */
        public EventLogType getType() {
            switch (_record.EventType.intValue()) {
                case WinNT.EVENTLOG_SUCCESS:
                case WinNT.EVENTLOG_INFORMATION_TYPE:
                    return EventLogType.Informational;
                case WinNT.EVENTLOG_AUDIT_FAILURE:
                    return EventLogType.AuditFailure;
                case WinNT.EVENTLOG_AUDIT_SUCCESS:
                    return EventLogType.AuditSuccess;
                case WinNT.EVENTLOG_ERROR_TYPE:
                    return EventLogType.Error;
                case WinNT.EVENTLOG_WARNING_TYPE:
                    return EventLogType.Warning;
                default:
                    throw new RuntimeException("Invalid type: " + _record.EventType.intValue());
            }
        }

        /**
         * Raw data associated with the record.
         *
         * @return Array of bytes or null.
         */
        public byte[] getData() {
            return _data;
        }

        /**
         * Constructor.
         * @param pevlr Pointer to the EVENTLOGRECORD structure
         */
        public EventLogRecord(Pointer pevlr) {
            _record = new EVENTLOGRECORD(pevlr);
            _source = pevlr.getString(_record.size()); //, true); FIXME
            // data
            if (_record.DataLength.intValue() > 0) {
                _data = pevlr.getByteArray(_record.DataOffset.intValue(),
                        _record.DataLength.intValue());
            }
            // strings
            if (_record.NumStrings.intValue() > 0) {
                ArrayList<String> strings = new ArrayList<>();
                int count = _record.NumStrings.intValue();
                long offset = _record.StringOffset.intValue();
                while (count > 0) {
                    String s = pevlr.getString(offset); //FIXME, true);
                    strings.add(s);
                    offset += s.length() * Native.WCHAR_SIZE;
                    offset += Native.WCHAR_SIZE;
                    count--;
                }
                _strings = strings.toArray(new String[strings.size()]);
            }
        }
    }
}
