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
/* Copyright (c) 2010 Daniel Doubrovkine, All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinBase.SECURITY_ATTRIBUTES;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.platform.win32.WinReg.HKEY;
import com.sun.jna.platform.win32.WinReg.HKEYByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * Advapi32.dll Interface.
 *
 * @author dblock[at]dblock.org
 */
public interface Advapi32 extends StdCallLibrary {

    /**
     * Advapi32 instance.
     */
    Advapi32 INSTANCE = (Advapi32) Native.loadLibrary("Advapi32",
            Advapi32.class, W32APIOptions.UNICODE_OPTIONS);

    /**
     * Retrieves the name of the user associated with the current thread.
     * http://msdn.microsoft.com/en-us/library/ms724432(VS.85).aspx
     *
     * @param buffer Buffer to receive the user's logon name.
     * @param len On input, the size of the buffer, on output the number of
     * characters copied into the buffer, including the terminating null
     * character.
     * @return True if succeeded.
     */
    public boolean GetUserNameW(char[] buffer, IntByReference len);

    /**
     * The LogonUser function attempts to log a user on to the local computer.
     * The local computer is the computer from which LogonUser was called. You
     * cannot use LogonUser to log on to a remote computer. You specify the user
     * with a user name and domain, and authenticate the user with a plaintext
     * password. If the function succeeds, you receive a handle to a token that
     * represents the logged-on user. You can then use this token handle to
     * impersonate the specified user or, in most cases, to create a process
     * that runs in the context of the specified user.
     *
     * @param lpszUsername A pointer to a null-terminated string that specifies
     * the name of the user. This is the name of the user account to log on to.
     * If you use the user principal name (UPN) format, user@DNS_domain_name,
     * the lpszDomain parameter must be NULL.
     * @param lpszDomain A pointer to a null-terminated string that specifies
     * the name of the domain or server whose account database contains the
     * lpszUsername account. If this parameter is NULL, the user name must be
     * specified in UPN format. If this parameter is ".", the function validates
     * the account using only the local account database.
     * @param lpszPassword A pointer to a null-terminated string that specifies
     * the plaintext password for the user account specified by lpszUsername.
     * @param logonType The type of logon operation to perform.
     * @param logonProvider Specifies the logon provider.
     * @param phToken A pointer to a handle variable that receives a handle to a
     * token that represents the specified user.
     * @return If the function succeeds, the function returns nonzero. If the
     * function fails, it returns zero. To get extended error information, call
     * GetLastError.
     */
    public boolean LogonUser(
            String lpszUsername,
            String lpszDomain,
            String lpszPassword,
            int logonType,
            int logonProvider,
            HANDLEByReference phToken);

    /**
     * The OpenThreadToken function opens the access token associated with a
     * thread.
     *
     * @param ThreadHandle Handle to the thread whose access token is opened.
     * @param DesiredAccess Specifies an access mask that specifies the
     * requested types of access to the access token. These requested access
     * types are reconciled against the token's discretionary access control
     * list (DACL) to determine which accesses are granted or denied.
     * @param OpenAsSelf Indicates whether the access check is to be made
     * against the security context of the thread calling the OpenThreadToken
     * function or against the security context of the process for the calling
     * thread.
     * @param TokenHandle Pointer to a variable that receives the handle to the
     * newly opened access token.
     * @return If the function succeeds, the return value is nonzero. If the
     * function fails, the return value is zero. To get extended error
     * information, call GetLastError.
     */
    public boolean OpenThreadToken(
            HANDLE ThreadHandle,
            int DesiredAccess,
            boolean OpenAsSelf,
            HANDLEByReference TokenHandle);

    /**
     * The OpenProcessToken function opens the access token associated with a
     * process.
     *
     * @param ProcessHandle Handle to the process whose access token is opened.
     * The process must have the PROCESS_QUERY_INFORMATION access permission.
     * @param DesiredAccess Specifies an access mask that specifies the
     * requested types of access to the access token. These requested access
     * types are compared with the discretionary access control list (DACL) of
     * the token to determine which accesses are granted or denied.
     * @param TokenHandle Pointer to a handle that identifies the newly opened
     * access token when the function returns.
     * @return If the function succeeds, the return value is nonzero. If the
     * function fails, the return value is zero. To get extended error
     * information, call GetLastError.
     */
    public boolean OpenProcessToken(
            HANDLE ProcessHandle,
            int DesiredAccess,
            HANDLEByReference TokenHandle);

    /**
     * The DuplicateToken function creates a new access token that duplicates
     * one already in existence.
     *
     * @param ExistingTokenHandle Handle to an access token opened with
     * TOKEN_DUPLICATE access.
     * @param ImpersonationLevel Specifies a SECURITY_IMPERSONATION_LEVEL
     * enumerated type that supplies the impersonation level of the new token.
     * @param DuplicateTokenHandle Pointer to a variable that receives a handle
     * to the duplicate token. This handle has TOKEN_IMPERSONATE and TOKEN_QUERY
     * access to the new token.
     * @return If the function succeeds, the return value is nonzero. If the
     * function fails, the return value is zero. To get extended error
     * information, call GetLastError.
     */
    public boolean DuplicateToken(
            HANDLE ExistingTokenHandle,
            int ImpersonationLevel,
            HANDLEByReference DuplicateTokenHandle);

    /**
     * The DuplicateTokenEx function creates a new access token that duplicates
     * an existing token. This function can create either a primary token or an
     * impersonation token.
     *
     * @param hExistingToken A handle to an access token opened with
     * TOKEN_DUPLICATE access.
     * @param dwDesiredAccess Specifies the requested access rights for the new
     * token.
     * @param lpTokenAttributes A pointer to a SECURITY_ATTRIBUTES structure
     * that specifies a security descriptor for the new token and determines
     * whether child processes can inherit the token.
     * @param ImpersonationLevel Specifies a value from the
     * SECURITY_IMPERSONATION_LEVEL enumeration that indicates the impersonation
     * level of the new token.
     * @param TokenType Specifies one of the following values from the
     * TOKEN_TYPE enumeration.
     * @param phNewToken A pointer to a HANDLE variable that receives the new
     * token.
     * @return If the function succeeds, the function returns a nonzero value.
     * If the function fails, it returns zero. To get extended error
     * information, call GetLastError.
     */
    public boolean DuplicateTokenEx(
            HANDLE hExistingToken,
            int dwDesiredAccess,
            WinBase.SECURITY_ATTRIBUTES lpTokenAttributes,
            int ImpersonationLevel,
            int TokenType,
            HANDLEByReference phNewToken);

    /**
     * Retrieves a specified type of information about an access token. The
     * calling process must have appropriate access rights to obtain the
     * information.
     *
     * @param tokenHandle Handle to an access token from which information is
     * retrieved. If TokenInformationClass specifies TokenSource, the handle
     * must have TOKEN_QUERY_SOURCE access. For all other TokenInformationClass
     * values, the handle must have TOKEN_QUERY access.
     * @param tokenInformationClass Specifies a value from the
     * TOKEN_INFORMATION_CLASS enumerated type to identify the type of
     * information the function retrieves.
     * @param tokenInformation Pointer to a buffer the function fills with the
     * requested information. The structure put into this buffer depends upon
     * the type of information specified by the TokenInformationClass parameter.
     * @param tokenInformationLength Specifies the size, in bytes, of the buffer
     * pointed to by the TokenInformation parameter. If TokenInformation is
     * NULL, this parameter must be zero.
     * @param returnLength Pointer to a variable that receives the number of
     * bytes needed for the buffer pointed to by the TokenInformation parameter.
     * If this value is larger than the value specified in the
     * TokenInformationLength parameter, the function fails and stores no data
     * in the buffer.
     * @return If the function succeeds, the return value is nonzero. If the
     * function fails, the return value is zero. To get extended error
     * information, call GetLastError.
     */
    public boolean GetTokenInformation(
            HANDLE tokenHandle,
            int tokenInformationClass,
            Structure tokenInformation,
            int tokenInformationLength,
            IntByReference returnLength);

    /**
     * The ImpersonateLoggedOnUser function lets the calling thread impersonate
     * the security context of a logged-on user. The user is represented by a
     * token handle.
     *
     * @param hToken Handle to a primary or impersonation access token that
     * represents a logged-on user. This can be a token handle returned by a
     * call to LogonUser, CreateRestrictedToken, DuplicateToken,
     * DuplicateTokenEx, OpenProcessToken, or OpenThreadToken functions. If
     * hToken is a primary token, it must have TOKEN_QUERY and TOKEN_DUPLICATE
     * access. If hToken is an impersonation token, it must have TOKEN_QUERY and
     * TOKEN_IMPERSONATE access.
     * @return If the function succeeds, the return value is nonzero.
     */
    public boolean ImpersonateLoggedOnUser(HANDLE hToken);

    /**
     * The ImpersonateSelf function obtains an access token that impersonates
     * the security context of the calling process. The token is assigned to the
     * calling thread.
     *
     * @param ImpersonationLevel Specifies a SECURITY_IMPERSONATION_LEVEL
     * enumerated type that supplies the impersonation level of the new token.
     * @return If the function succeeds, the return value is nonzero.
     */
    public boolean ImpersonateSelf(int ImpersonationLevel);

    /**
     * The RevertToSelf function terminates the impersonation of a client
     * application.
     *
     * @return If the function succeeds, the return value is nonzero.
     */
    public boolean RevertToSelf();

    /**
     * The RegOpenKeyEx function opens the specified registry key. Note that key
     * names are not case-sensitive.
     *
     * @param hKey Handle to an open key.
     * @param lpSubKey Pointer to a null-terminated string containing the name
     * of the subkey to open.
     * @param ulOptions Reserved; must be zero.
     * @param samDesired Access mask that specifies the desired access rights to
     * the key. The function fails if the security descriptor of the key does
     * not permit the requested access for the calling process.
     * @param phkResult Pointer to a variable that receives a handle to the
     * opened key. If the key is not one of the predefined registry keys, call
     * the RegCloseKey function after you have finished using the handle.
     * @return If the function succeeds, the return value is ERROR_SUCCESS. If
     * the function fails, the return value is a nonzero error code defined in
     * Winerror.h.
     */
    public int RegOpenKeyEx(HKEY hKey, String lpSubKey, int ulOptions, int samDesired,
            HKEYByReference phkResult);

    /**
     * The RegQueryValueEx function retrieves the type and data for a specified
     * value name associated with an open registry key.
     *
     * @param hKey Handle to an open key. The key must have been opened with the
     * KEY_QUERY_VALUE access right.
     * @param lpValueName Pointer to a null-terminated string containing the
     * name of the value to query. If lpValueName is NULL or an empty string,
     * "", the function retrieves the type and data for the key's unnamed or
     * default value, if any.
     * @param lpReserved Reserved; must be NULL.
     * @param lpType Pointer to a variable that receives a code indicating the
     * type of data stored in the specified value.
     * @param lpData Pointer to a buffer that receives the value's data. This
     * parameter can be NULL if the data is not required. If the data is a
     * string, the function checks for a terminating null character. If one is
     * not found, the string is stored with a null terminator if the buffer is
     * large enough to accommodate the extra character. Otherwise, the string is
     * stored as is.
     * @param lpcbData Pointer to a variable that specifies the size of the
     * buffer pointed to by the lpData parameter, in bytes. When the function
     * returns, this variable contains the size of the data copied to lpData.
     * The lpcbData parameter can be NULL only if lpData is NULL. If the data
     * has the REG_SZ, REG_MULTI_SZ or REG_EXPAND_SZ type, this size includes
     * any terminating null character or characters. If the buffer specified by
     * lpData parameter is not large enough to hold the data, the function
     * returns ERROR_MORE_DATA and stores the required buffer size in the
     * variable pointed to by lpcbData. In this case, the contents of the lpData
     * buffer are undefined. If lpData is NULL, and lpcbData is non-NULL, the
     * function returns ERROR_SUCCESS and stores the size of the data, in bytes,
     * in the variable pointed to by lpcbData. This enables an application to
     * determine the best way to allocate a buffer for the value's data.
     * @return If the function succeeds, the return value is ERROR_SUCCESS. If
     * the function fails, the return value is a nonzero error code defined in
     * Winerror.h.
     */
    public int RegQueryValueEx(HKEY hKey, String lpValueName, int lpReserved,
            IntByReference lpType, char[] lpData, IntByReference lpcbData);

    /**
     * The RegQueryValueEx function retrieves the type and data for a specified
     * value name associated with an open registry key.
     *
     * @param hKey Handle to an open key. The key must have been opened with the
     * KEY_QUERY_VALUE access right.
     * @param lpValueName Pointer to a null-terminated string containing the
     * name of the value to query. If lpValueName is NULL or an empty string,
     * "", the function retrieves the type and data for the key's unnamed or
     * default value, if any.
     * @param lpReserved Reserved; must be NULL.
     * @param lpType Pointer to a variable that receives a code indicating the
     * type of data stored in the specified value.
     * @param lpData Pointer to a buffer that receives the value's data. This
     * parameter can be NULL if the data is not required. If the data is a
     * string, the function checks for a terminating null character. If one is
     * not found, the string is stored with a null terminator if the buffer is
     * large enough to accommodate the extra character. Otherwise, the string is
     * stored as is.
     * @param lpcbData Pointer to a variable that specifies the size of the
     * buffer pointed to by the lpData parameter, in bytes. When the function
     * returns, this variable contains the size of the data copied to lpData.
     * The lpcbData parameter can be NULL only if lpData is NULL. If the data
     * has the REG_SZ, REG_MULTI_SZ or REG_EXPAND_SZ type, this size includes
     * any terminating null character or characters. If the buffer specified by
     * lpData parameter is not large enough to hold the data, the function
     * returns ERROR_MORE_DATA and stores the required buffer size in the
     * variable pointed to by lpcbData. In this case, the contents of the lpData
     * buffer are undefined. If lpData is NULL, and lpcbData is non-NULL, the
     * function returns ERROR_SUCCESS and stores the size of the data, in bytes,
     * in the variable pointed to by lpcbData. This enables an application to
     * determine the best way to allocate a buffer for the value's data.
     * @return If the function succeeds, the return value is ERROR_SUCCESS. If
     * the function fails, the return value is a nonzero error code defined in
     * Winerror.h.
     */
    public int RegQueryValueEx(HKEY hKey, String lpValueName, int lpReserved,
            IntByReference lpType, byte[] lpData, IntByReference lpcbData);

    /**
     * The RegQueryValueEx function retrieves the type and data for a specified
     * value name associated with an open registry key.
     *
     * @param hKey Handle to an open key. The key must have been opened with the
     * KEY_QUERY_VALUE access right.
     * @param lpValueName Pointer to a null-terminated string containing the
     * name of the value to query. If lpValueName is NULL or an empty string,
     * "", the function retrieves the type and data for the key's unnamed or
     * default value, if any.
     * @param lpReserved Reserved; must be NULL.
     * @param lpType Pointer to a variable that receives a code indicating the
     * type of data stored in the specified value.
     * @param lpData Pointer to a buffer that receives the value's data. This
     * parameter can be NULL if the data is not required. If the data is a
     * string, the function checks for a terminating null character. If one is
     * not found, the string is stored with a null terminator if the buffer is
     * large enough to accommodate the extra character. Otherwise, the string is
     * stored as is.
     * @param lpcbData Pointer to a variable that specifies the size of the
     * buffer pointed to by the lpData parameter, in bytes. When the function
     * returns, this variable contains the size of the data copied to lpData.
     * The lpcbData parameter can be NULL only if lpData is NULL. If the data
     * has the REG_SZ, REG_MULTI_SZ or REG_EXPAND_SZ type, this size includes
     * any terminating null character or characters. If the buffer specified by
     * lpData parameter is not large enough to hold the data, the function
     * returns ERROR_MORE_DATA and stores the required buffer size in the
     * variable pointed to by lpcbData. In this case, the contents of the lpData
     * buffer are undefined. If lpData is NULL, and lpcbData is non-NULL, the
     * function returns ERROR_SUCCESS and stores the size of the data, in bytes,
     * in the variable pointed to by lpcbData. This enables an application to
     * determine the best way to allocate a buffer for the value's data.
     * @return If the function succeeds, the return value is ERROR_SUCCESS. If
     * the function fails, the return value is a nonzero error code defined in
     * Winerror.h.
     */
    public int RegQueryValueEx(HKEY hKey, String lpValueName, int lpReserved,
            IntByReference lpType, IntByReference lpData, IntByReference lpcbData);

    /**
     * The RegQueryValueEx function retrieves the type and data for a specified
     * value name associated with an open registry key.
     *
     * @param hKey Handle to an open key. The key must have been opened with the
     * KEY_QUERY_VALUE access right.
     * @param lpValueName Pointer to a null-terminated string containing the
     * name of the value to query. If lpValueName is NULL or an empty string,
     * "", the function retrieves the type and data for the key's unnamed or
     * default value, if any.
     * @param lpReserved Reserved; must be NULL.
     * @param lpType Pointer to a variable that receives a code indicating the
     * type of data stored in the specified value.
     * @param lpData Pointer to a buffer that receives the value's data. This
     * parameter can be NULL if the data is not required. If the data is a
     * string, the function checks for a terminating null character. If one is
     * not found, the string is stored with a null terminator if the buffer is
     * large enough to accommodate the extra character. Otherwise, the string is
     * stored as is.
     * @param lpcbData Pointer to a variable that specifies the size of the
     * buffer pointed to by the lpData parameter, in bytes. When the function
     * returns, this variable contains the size of the data copied to lpData.
     * The lpcbData parameter can be NULL only if lpData is NULL. If the data
     * has the REG_SZ, REG_MULTI_SZ or REG_EXPAND_SZ type, this size includes
     * any terminating null character or characters. If the buffer specified by
     * lpData parameter is not large enough to hold the data, the function
     * returns ERROR_MORE_DATA and stores the required buffer size in the
     * variable pointed to by lpcbData. In this case, the contents of the lpData
     * buffer are undefined. If lpData is NULL, and lpcbData is non-NULL, the
     * function returns ERROR_SUCCESS and stores the size of the data, in bytes,
     * in the variable pointed to by lpcbData. This enables an application to
     * determine the best way to allocate a buffer for the value's data.
     * @return If the function succeeds, the return value is ERROR_SUCCESS. If
     * the function fails, the return value is a nonzero error code defined in
     * Winerror.h.
     */
    public int RegQueryValueEx(HKEY hKey, String lpValueName, int lpReserved,
            IntByReference lpType, Pointer lpData, IntByReference lpcbData);

    /**
     * The RegCloseKey function releases a handle to the specified registry key.
     *
     * @param hKey Handle to the open key to be closed. The handle must have
     * been opened by the RegCreateKeyEx, RegOpenKeyEx, or RegConnectRegistry
     * function.
     * @return If the function succeeds, the return value is ERROR_SUCCESS. If
     * the function fails, the return value is a nonzero error code defined in
     * Winerror.h.
     */
    public int RegCloseKey(HKEY hKey);

    /**
     * The RegDeleteValue function removes a named value from the specified
     * registry key. Note that value names are not case-sensitive.
     *
     * @param hKey Handle to an open key. The key must have been opened with the
     * KEY_SET_VALUE access right.
     * @param lpValueName Pointer to a null-terminated string that names the
     * value to remove. If this parameter is NULL or an empty string, the value
     * set by the RegSetValue function is removed.
     * @return If the function succeeds, the return value is ERROR_SUCCESS. If
     * the function fails, the return value is a nonzero error code defined in
     * Winerror.h.
     */
    public int RegDeleteValue(HKEY hKey, String lpValueName);

    /**
     * The RegSetValueEx function sets the data and type of a specified value
     * under a registry key.
     *
     * @param hKey Handle to an open key. The key must have been opened with the
     * KEY_SET_VALUE access right.
     * @param lpValueName Pointer to a string containing the name of the value
     * to set. If a value with this name is not already present in the key, the
     * function adds it to the key. If lpValueName is NULL or an empty string,
     * "", the function sets the type and data for the key's unnamed or default
     * value.
     * @param Reserved Reserved; must be zero.
     * @param dwType Type of data pointed to by the lpData parameter.
     * @param lpData Pointer to a buffer containing the data to be stored with
     * the specified value name.
     * @param cbData Size of the information pointed to by the lpData parameter,
     * in bytes. If the data is of type REG_SZ, REG_EXPAND_SZ, or REG_MULTI_SZ,
     * cbData must include the size of the terminating null character or
     * characters.
     * @return If the function succeeds, the return value is ERROR_SUCCESS. If
     * the function fails, the return value is a nonzero error code defined in
     * Winerror.h.
     */
    public int RegSetValueEx(HKEY hKey, String lpValueName, int Reserved, int dwType,
            char[] lpData, int cbData);

    /**
     * The RegSetValueEx function sets the data and type of a specified value
     * under a registry key.
     *
     * @param hKey Handle to an open key. The key must have been opened with the
     * KEY_SET_VALUE access right.
     * @param lpValueName Pointer to a string containing the name of the value
     * to set. If a value with this name is not already present in the key, the
     * function adds it to the key. If lpValueName is NULL or an empty string,
     * "", the function sets the type and data for the key's unnamed or default
     * value.
     * @param Reserved Reserved; must be zero.
     * @param dwType Type of data pointed to by the lpData parameter.
     * @param lpData Pointer to a buffer containing the data to be stored with
     * the specified value name.
     * @param cbData Size of the information pointed to by the lpData parameter,
     * in bytes. If the data is of type REG_SZ, REG_EXPAND_SZ, or REG_MULTI_SZ,
     * cbData must include the size of the terminating null character or
     * characters.
     * @return If the function succeeds, the return value is ERROR_SUCCESS. If
     * the function fails, the return value is a nonzero error code defined in
     * Winerror.h.
     */
    public int RegSetValueEx(HKEY hKey, String lpValueName, int Reserved, int dwType,
            byte[] lpData, int cbData);

    /**
     * Creates the specified registry key.
     * @param hKey hKey
     * @param lpSubKey lpSubKey
     * @param Reserved Reserved
     * @param lpClass lpClass
     * @param dwOptions dwOptions
     * @param samDesired samDesired
     * @param lpSecurityAttributes lpSecurityAttributes
     * @param phkResult phkResult
     * @param lpdwDisposition lpdwDisposition
     * @return If the function succeeds, the return value is ERROR_SUCCESS. If
     * the function fails, the return value is a nonzero error code defined in
     * Winerror.h.
     */
    public int RegCreateKeyEx(HKEY hKey, String lpSubKey, int Reserved, String lpClass,
            int dwOptions, int samDesired, SECURITY_ATTRIBUTES lpSecurityAttributes,
            HKEYByReference phkResult, IntByReference lpdwDisposition);

    /**
     * Removes the specified registry key.
     * @param hKey hKey
     * @param name name
     * @return If the function succeeds, the return value is ERROR_SUCCESS. If
     * the function fails, the return value is a nonzero error code defined in
     * Winerror.h.
     */
    public int RegDeleteKey(HKEY hKey, String name);

    /**
     * The RegEnumKeyEx function enumerates subkeys of the specified open
     * registry key. The function retrieves information about one subkey each
     * time it is called.
     *
     * @param hKey Handle to an open key. The key must have been opened with the
     * KEY_ENUMERATE_SUB_KEYS access right.
     * @param dwIndex Index of the subkey to retrieve. This parameter should be
     * zero for the first call to the RegEnumKeyEx function and then incremented
     * for subsequent calls. Because subkeys are not ordered, any new subkey
     * will have an arbitrary index. This means that the function may return
     * subkeys in any order.
     * @param lpName Pointer to a buffer that receives the name of the subkey,
     * including the terminating null character. The function copies only the
     * name of the subkey, not the full key hierarchy, to the buffer.
     * @param lpcName Pointer to a variable that specifies the size of the
     * buffer specified by the lpName parameter, in TCHARs. This size should
     * include the terminating null character. When the function returns, the
     * variable pointed to by lpcName contains the number of characters stored
     * in the buffer. The count returned does not include the terminating null
     * character.
     * @param reserved Reserved; must be NULL.
     * @param lpClass Pointer to a buffer that receives the null-terminated
     * class string of the enumerated subkey. This parameter can be NULL.
     * @param lpcClass Pointer to a variable that specifies the size of the
     * buffer specified by the lpClass parameter, in TCHARs. The size should
     * include the terminating null character. When the function returns,
     * lpcClass contains the number of characters stored in the buffer. The
     * count returned does not include the terminating null character. This
     * parameter can be NULL only if lpClass is NULL.
     * @param lpftLastWriteTime Pointer to a variable that receives the time at
     * which the enumerated subkey was last written.
     * @return If the function succeeds, the return value is ERROR_SUCCESS. If
     * the function fails, the return value is a nonzero error code defined in
     * Winerror.h.
     */
    public int RegEnumKeyEx(HKEY hKey, int dwIndex, char[] lpName, IntByReference lpcName,
            IntByReference reserved, char[] lpClass, IntByReference lpcClass,
            WinBase.FILETIME lpftLastWriteTime);

    /**
     * The RegEnumValue function enumerates the values for the specified open
     * registry key. The function copies one indexed value name and data block
     * for the key each time it is called.
     *
     * @param hKey Handle to an open key. The key must have been opened with the
     * KEY_QUERY_VALUE access right.
     * @param dwIndex Index of the value to be retrieved. This parameter should
     * be zero for the first call to the RegEnumValue function and then be
     * incremented for subsequent calls. Because values are not ordered, any new
     * value will have an arbitrary index. This means that the function may
     * return values in any order.
     * @param lpValueName Pointer to a buffer that receives the name of the
     * value, including the terminating null character.
     * @param lpcchValueName Pointer to a variable that specifies the size of
     * the buffer pointed to by the lpValueName parameter, in TCHARs. This size
     * should include the terminating null character. When the function returns,
     * the variable pointed to by lpcValueName contains the number of characters
     * stored in the buffer. The count returned does not include the terminating
     * null character.
     * @param reserved Reserved; must be NULL.
     * @param lpType Pointer to a variable that receives a code indicating the
     * type of data stored in the specified value.
     * @param lpData Pointer to a buffer that receives the data for the value
     * entry. This parameter can be NULL if the data is not required.
     * @param lpcbData Pointer to a variable that specifies the size of the
     * buffer pointed to by the lpData parameter, in bytes.
     * @return If the function succeeds, the return value is ERROR_SUCCESS. If
     * the function fails, the return value is a nonzero error code defined in
     * Winerror.h.
     */
    public int RegEnumValue(HKEY hKey, int dwIndex, char[] lpValueName,
            IntByReference lpcchValueName, IntByReference reserved,
            IntByReference lpType, byte[] lpData, IntByReference lpcbData);

    /**
     * The RegQueryInfoKey function retrieves information about the specified
     * registry key.
     *
     * @param hKey A handle to an open key. The key must have been opened with
     * the KEY_QUERY_VALUE access right.
     * @param lpClass A pointer to a buffer that receives the null-terminated
     * class string of the key. This parameter can be ignored. This parameter
     * can be NULL.
     * @param lpcClass A pointer to a variable that specifies the size of the
     * buffer pointed to by the lpClass parameter, in characters.
     * @param lpReserved Reserved; must be NULL.
     * @param lpcSubKeys A pointer to a variable that receives the number of
     * subkeys that are contained by the specified key. This parameter can be
     * NULL.
     * @param lpcMaxSubKeyLen A pointer to a variable that receives the size of
     * the key's subkey with the longest name, in characters, not including the
     * terminating null character. This parameter can be NULL.
     * @param lpcMaxClassLen A pointer to a variable that receives the size of
     * the longest string that specifies a subkey class, in characters. The
     * count returned does not include the terminating null character. This
     * parameter can be NULL.
     * @param lpcValues A pointer to a variable that receives the number of
     * values that are associated with the key. This parameter can be NULL.
     * @param lpcMaxValueNameLen A pointer to a variable that receives the size
     * of the key's longest value name, in characters. The size does not include
     * the terminating null character. This parameter can be NULL.
     * @param lpcMaxValueLen A pointer to a variable that receives the size of
     * the longest data component among the key's values, in bytes. This
     * parameter can be NULL.
     * @param lpcbSecurityDescriptor A pointer to a variable that receives the
     * size of the key's security descriptor, in bytes. This parameter can be
     * NULL.
     * @param lpftLastWriteTime A pointer to a FILETIME structure that receives
     * the last write time. This parameter can be NULL.
     * @return If the function succeeds, the return value is ERROR_SUCCESS. If
     * the function fails, the return value is a nonzero error code defined in
     * Winerror.h.
     */
    public int RegQueryInfoKey(HKEY hKey, char[] lpClass,
            IntByReference lpcClass, IntByReference lpReserved,
            IntByReference lpcSubKeys, IntByReference lpcMaxSubKeyLen,
            IntByReference lpcMaxClassLen, IntByReference lpcValues,
            IntByReference lpcMaxValueNameLen, IntByReference lpcMaxValueLen,
            IntByReference lpcbSecurityDescriptor,
            WinBase.FILETIME lpftLastWriteTime);

    /**
     * Retrieves a registered handle to the specified event log.
     *
     * @param lpUNCServerName The Universal Naming Convention (UNC) name of the
     * remote server on which this operation is to be performed. If this
     * parameter is NULL, the local computer is used.
     * @param lpSourceName The name of the event source whose handle is to be
     * retrieved. The source name must be a subkey of a log under the Eventlog
     * registry key. However, the Security log is for system use only.
     * @return If the function succeeds, the return value is a handle to the
     * event log. If the function fails, the return value is NULL. To get
     * extended error information, call GetLastError. The function returns
     * ERROR_ACCESS_DENIED if lpSourceName specifies the Security event log.
     */
    public HANDLE RegisterEventSource(String lpUNCServerName, String lpSourceName);

    /**
     * Closes the specified event log.
     *
     * @param hEventLog A handle to the event log. The RegisterEventSource
     * function returns this handle.
     * @return If the function succeeds, the return value is nonzero. If the
     * function fails, the return value is zero. To get extended error
     * information, call GetLastError.
     */
    public boolean DeregisterEventSource(HANDLE hEventLog);

    /**
     * Opens a handle to the specified event log.
     *
     * @param lpUNCServerName The Universal Naming Convention (UNC) name of the
     * remote server on which the event log is to be opened. If this parameter
     * is NULL, the local computer is used.
     * @param lpSourceName The name of the log. If you specify a custom log and
     * it cannot be found, the event logging service opens the Application log;
     * however, there will be no associated message or category string file.
     * @return If the function succeeds, the return value is the handle to an
     * event log. If the function fails, the return value is NULL. To get
     * extended error information, call GetLastError.
     */
    public HANDLE OpenEventLog(String lpUNCServerName, String lpSourceName);

    /**
     * Closes the specified event log.
     *
     * @param hEventLog A handle to the event log to be closed. The OpenEventLog
     * or OpenBackupEventLog function returns this handle.
     * @return If the function succeeds, the return value is nonzero. If the
     * function fails, the return value is zero. To get extended error
     * information, call GetLastError.
     */
    public boolean CloseEventLog(HANDLE hEventLog);

    /**
     * Retrieves the number of records in the specified event log.
     *
     * @param hEventLog A handle to the open event log. The OpenEventLog or
     * OpenBackupEventLog function returns this handle.
     * @param NumberOfRecords A pointer to a variable that receives the number
     * of records in the specified event log.
     * @return If the function succeeds, the return value is nonzero. If the
     * function fails, the return value is zero. To get extended error
     * information, call GetLastError.
     */
    public boolean GetNumberOfEventLogRecords(HANDLE hEventLog, IntByReference NumberOfRecords);

    /**
     * Clears the specified event log, and optionally saves the current copy of
     * the log to a backup file.
     *
     * @param hEventLog A handle to the event log to be cleared. The
     * OpenEventLog function returns this handle.
     * @param lpBackupFileName The absolute or relative path of the backup file.
     * If this file already exists, the function fails. If the lpBackupFileName
     * parameter is NULL, the event log is not backed up.
     * @return If the function succeeds, the return value is nonzero. If the
     * function fails, the return value is zero. To get extended error
     * information, call GetLastError. The ClearEventLog function can fail if
     * the event log is empty or the backup file already exists.
     */
    public boolean ClearEventLog(HANDLE hEventLog, String lpBackupFileName);

    /**
     * Saves the specified event log to a backup file. The function does not
     * clear the event log.
     *
     * @param hEventLog A handle to the open event log. The OpenEventLog
     * function returns this handle.
     * @param lpBackupFileName The absolute or relative path of the backup file.
     * @return If the function succeeds, the return value is nonzero. If the
     * function fails, the return value is zero. To get extended error
     * information, call GetLastError.
     */
    public boolean BackupEventLog(HANDLE hEventLog, String lpBackupFileName);

    /**
     * Opens a handle to a backup event log created by the BackupEventLog
     * function.
     *
     * @param lpUNCServerName The Universal Naming Convention (UNC) name of the
     * remote server on which this operation is to be performed. If this
     * parameter is NULL, the local computer is used.
     * @param lpFileName The full path of the backup file.
     * @return If the function succeeds, the return value is a handle to the
     * backup event log. If the function fails, the return value is NULL. To get
     * extended error information, call GetLastError.
     */
    public HANDLE OpenBackupEventLog(String lpUNCServerName, String lpFileName);

    /**
     * Reads the specified number of entries from the specified event log. The
     * function can be used to read log entries in chronological or reverse
     * chronological order.
     *
     * @param hEventLog A handle to the event log to be read. The OpenEventLog
     * function returns this handle.
     * @param dwReadFlags Use the following flag values to indicate how to read
     * the log file.
     * @param dwRecordOffset The record number of the log-entry at which the
     * read operation should start. This parameter is ignored unless dwReadFlags
     * includes the EVENTLOG_SEEK_READ flag.
     * @param lpBuffer An application-allocated buffer that will receive one or
     * more EVENTLOGRECORD structures. This parameter cannot be NULL, even if
     * the nNumberOfBytesToRead parameter is zero. The maximum size of this
     * buffer is 0x7ffff bytes.
     * @param nNumberOfBytesToRead The size of the lpBuffer buffer, in bytes.
     * This function will read as many log entries as will fit in the buffer;
     * the function will not return partial entries.
     * @param pnBytesRead A pointer to a variable that receives the number of
     * bytes read by the function.
     * @param pnMinNumberOfBytesNeeded A pointer to a variable that receives the
     * required size of the lpBuffer buffer. This value is valid only this
     * function returns zero and GetLastError returns ERROR_INSUFFICIENT_BUFFER.
     * @return If the function succeeds, the return value is nonzero. If the
     * function fails, the return value is zero. To get extended error
     * information, call GetLastError.
     */
    public boolean ReadEventLog(HANDLE hEventLog, int dwReadFlags, int dwRecordOffset,
            Pointer lpBuffer, int nNumberOfBytesToRead, IntByReference pnBytesRead,
            IntByReference pnMinNumberOfBytesNeeded);

    /**
     * The GetOldestEventLogRecord function retrieves the absolute record number
     * of the oldest record in the specified event log.
     *
     * @param hEventLog Handle to the open event log. This handle is returned by
     * the OpenEventLog or OpenBackupEventLog function.
     * @param OldestRecord Pointer to a variable that receives the absolute
     * record number of the oldest record in the specified event log.
     * @return If the function succeeds, the return value is nonzero. If the
     * function fails, the return value is zero. To get extended error
     * information, call GetLastError.
     */
    public boolean GetOldestEventLogRecord(HANDLE hEventLog, IntByReference OldestRecord);

    /**
     * Creates a new process and its primary thread. The new process runs in the
     * security context of the user represented by the specified token.
     * <p>
     * Typically, the process that calls the CreateProcessAsUser function must
     * have the SE_INCREASE_QUOTA_NAME privilege and may require the
     * SE_ASSIGNPRIMARYTOKEN_NAME privilege if the token is not assignable. If
     * this function fails with ERROR_PRIVILEGE_NOT_HELD (1314), use the
     * CreateProcessWithLogonW function instead. CreateProcessWithLogonW
     * requires no special privileges, but the specified user account must be
     * allowed to log on interactively. Generally, it is best to use
     * CreateProcessWithLogonW to create a process with alternate credentials.
     *
     * @param hToken A handle to the primary token that represents a user.
     * @param lpApplicationName The name of the module to be executed.
     * @param lpCommandLine The command line to be executed.
     * @param lpProcessAttributes A pointer to a SECURITY_ATTRIBUTES structure
     * that specifies a security descriptor for the new process object and
     * determines whether child processes can inherit the returned handle to the
     * process.
     * @param lpThreadAttributes A pointer to a SECURITY_ATTRIBUTES structure
     * that specifies a security descriptor for the new thread object and
     * determines whether child processes can inherit the returned handle to the
     * thread.
     * @param bInheritHandles If this parameter is TRUE, each inheritable handle
     * in the calling process is inherited by the new process. If the parameter
     * is FALSE, the handles are not inherited. Note that inherited handles have
     * the same value and access rights as the original handles.
     * @param dwCreationFlags The flags that control the priority class and the
     * creation of the process. For a list of values, see Process Creation
     * Flags.
     * @param lpEnvironment A pointer to an environment block for the new
     * process. If this parameter is NULL, the new process uses the environment
     * of the calling process.
     * <p>
     * An environment block consists of a null-terminated block of
     * null-terminated strings. Each string is in the following form:
     * name=value\0
     * @param lpCurrentDirectory The full path to the current directory for the
     * process. The string can also specify a UNC path.
     * @param lpStartupInfo A pointer to a STARTUPINFO or STARTUPINFOEX
     * structure.
     * @param lpProcessInformation A pointer to a PROCESS_INFORMATION structure
     * that receives identification information about the new process.
     * @return If the function succeeds, the return value is nonzero. If the
     * function fails, the return value is zero. To get extended error
     * information, call GetLastError.
     */
    public boolean CreateProcessAsUser(
            HANDLE hToken,
            String lpApplicationName,
            String lpCommandLine,
            SECURITY_ATTRIBUTES lpProcessAttributes,
            SECURITY_ATTRIBUTES lpThreadAttributes,
            boolean bInheritHandles,
            int dwCreationFlags,
            String lpEnvironment,
            String lpCurrentDirectory,
            WinBase.STARTUPINFO lpStartupInfo,
            WinBase.PROCESS_INFORMATION lpProcessInformation);

    /**
     * The AdjustTokenPrivileges function enables or disables privileges in the
     * specified access token. Enabling or disabling privileges in an access
     * token requires TOKEN_ADJUST_PRIVILEGES access.
     *
     * @param TokenHandle A handle to the access token that contains the
     * privileges to be modified.
     * @param DisableAllPrivileges Specifies whether the function disables all
     * of the token's privileges.
     * @param NewState A pointer to a TOKEN_PRIVILEGES structure that specifies
     * an array of privileges and their attributes.
     * @param BufferLength Specifies the size, in bytes, of the buffer pointed
     * to by the PreviousState parameter. This parameter can be zero if the
     * PreviousState parameter is NULL.
     * @param PreviousState A pointer to a buffer that the function fills with a
     * TOKEN_PRIVILEGES structure that contains the previous state of any
     * privileges that the function modifies.
     * @param ReturnLength A pointer to a variable that receives the required
     * size, in bytes, of the buffer pointed to by the PreviousState parameter.
     * @return If the function succeeds, the return value is nonzero. If the
     * function fails, the return value is zero. To get extended error
     * information, call GetLastError.
     */
    public boolean AdjustTokenPrivileges(
            HANDLE TokenHandle,
            boolean DisableAllPrivileges,
            WinNT.TOKEN_PRIVILEGES NewState,
            int BufferLength,
            WinNT.TOKEN_PRIVILEGES PreviousState,
            IntByReference ReturnLength);

    /**
     * The LookupPrivilegeName function retrieves the name that corresponds to
     * the privilege represented on a specific system by a specified locally
     * unique identifier (LUID).
     *
     * @param lpSystemName A pointer to a null-terminated string that specifies
     * the name of the system on which the privilege name is retrieved. If a
     * null string is specified, the function attempts to find the privilege
     * name on the local system.
     * @param lpLuid A pointer to the LUID by which the privilege is known on
     * the target system.
     * @param lpName A pointer to a buffer that receives a null-terminated
     * string that represents the privilege name. For example, this string could
     * be "SeSecurityPrivilege".
     * @param cchName A pointer to a variable that specifies the size, in a
     * TCHAR value, of the lpName buffer.
     * @return If the function succeeds, the return value is nonzero. If the
     * function fails, the return value is zero. To get extended error
     * information, call GetLastError.
     */
    public boolean LookupPrivilegeName(
            String lpSystemName,
            WinNT.LUID lpLuid,
            char[] lpName,
            IntByReference cchName);

    /**
     * The LookupPrivilegeValue function retrieves the locally unique identifier
     * (LUID) used on a specified system to locally represent the specified
     * privilege name.
     *
     * @param lpSystemName A pointer to a null-terminated string that specifies
     * the name of the system on which the privilege name is retrieved. If a
     * null string is specified, the function attempts to find the privilege
     * name on the local system.
     * @param lpName A pointer to a null-terminated string that specifies the
     * name of the privilege, as defined in the Winnt.h header file. For
     * example, this parameter could specify the constant, SE_SECURITY_NAME, or
     * its corresponding string, "SeSecurityPrivilege".
     * @param lpLuid A pointer to a variable that receives the LUID by which the
     * privilege is known on the system specified by the lpSystemName parameter.
     * @return If the function succeeds, the return value is nonzero. If the
     * function fails, the return value is zero. To get extended error
     * information, call GetLastError.
     */
    public boolean LookupPrivilegeValue(
            String lpSystemName,
            String lpName,
            WinNT.LUID lpLuid);
}
