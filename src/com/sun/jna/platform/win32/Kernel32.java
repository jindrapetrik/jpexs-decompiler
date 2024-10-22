/*
 *  Copyright (C) 2010-2024 JPEXS
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
package com.sun.jna.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.win32.W32APIOptions;

// TODO: Auto-generated Javadoc
/**
 * Interface definitions for <code>kernel32.dll</code>. Includes additional
 * alternate mappings from {@link WinNT} which make use of NIO buffers.
 */
public interface Kernel32 extends WinNT {

    /**
     * The instance.
     */
    Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32",
            Kernel32.class, W32APIOptions.UNICODE_OPTIONS);

    /**
     * The CloseHandle function closes an open object handle.
     *
     * @param hObject Handle to an open object. This parameter can be a pseudo
     * handle or INVALID_HANDLE_VALUE.
     * @return If the function succeeds, the return value is nonzero. If the
     * function fails, the return value is zero. To get extended error
     * information, call GetLastError.
     */
    boolean CloseHandle(HANDLE hObject);

    /**
     * Terminates the specified process and all of its threads.
     *
     * @param hProcess A handle to the process to be terminated.
     * @param uExitCode The exit code to be used by the process and threads
     * terminated as a result of this call.
     * @return If the function succeeds, the return value is nonzero.
     * <p>
     * If the function fails, the return value is zero. To get extended error
     * information, call GetLastError.
     */
    boolean TerminateProcess(HANDLE hProcess, int uExitCode);

    /**
     * Writes data to the specified file or input/output (I/O) device.
     *
     * @param hFile A handle to the file or I/O device (for example, a file,
     * file stream, physical disk, volume, console buffer, tape drive, socket,
     * communications resource, mailslot, or pipe).
     * @param lpBuffer A pointer to the buffer containing the data to be written
     * to the file or device.
     * @param nNumberOfBytesToWrite The number of bytes to be written to the
     * file or device.
     * @param lpNumberOfBytesWritten A pointer to the variable that receives the
     * number of bytes written when using a synchronous hFile parameter.
     * @param lpOverlapped A pointer to an OVERLAPPED structure is required if
     * the hFile parameter was opened with FILE_FLAG_OVERLAPPED, otherwise this
     * parameter can be NULL.
     * @return If the function succeeds, the return value is nonzero (TRUE). If
     * the function fails, or is completing asynchronously, the return value is
     * zero (FALSE). To get extended error information, call the GetLastError
     * function.
     */
    boolean WriteFile(HANDLE hFile, byte[] lpBuffer, int nNumberOfBytesToWrite,
            IntByReference lpNumberOfBytesWritten,
            WinBase.OVERLAPPED lpOverlapped);

    boolean ReadFile(HANDLE hFile, byte[] lpBuffer, int nNumberOfBytesToRead, IntByReference lpNumberOfBytesRead, WinBase.OVERLAPPED lpOverlapped);

    //
    // Define the NamedPipe definitions
    //
    //
    // Define the dwOpenMode values for CreateNamedPipe
    //
    public static final int PIPE_ACCESS_INBOUND = 0x00000001;

    public static final int PIPE_ACCESS_OUTBOUND = 0x00000002;

    public static final int PIPE_ACCESS_DUPLEX = 0x00000003;

    //
    // Define the Named Pipe End flags for GetNamedPipeInfo
    //
    public static final int PIPE_CLIENT_END = 0x00000000;

    public static final int PIPE_SERVER_END = 0x00000001;

    //
    // Define the dwPipeMode values for CreateNamedPipe
    //
    public static final int PIPE_WAIT = 0x00000000;

    public static final int PIPE_NOWAIT = 0x00000001;

    public static final int PIPE_READMODE_BYTE = 0x00000000;

    public static final int PIPE_READMODE_MESSAGE = 0x00000002;

    public static final int PIPE_TYPE_BYTE = 0x00000000;

    public static final int PIPE_TYPE_MESSAGE = 0x00000004;

    public static final int PIPE_ACCEPT_REMOTE_CLIENTS = 0x00000000;

    public static final int PIPE_REJECT_REMOTE_CLIENTS = 0x00000008;

    //
    // Define the well known values for CreateNamedPipe nMaxInstances
    //
    public static final int PIPE_UNLIMITED_INSTANCES = 255;

    //
    // Define the values for process priority
    //
    public static final int ABOVE_NORMAL_PRIORITY_CLASS = 0x00008000;

    public static final int BELOW_NORMAL_PRIORITY_CLASS = 0x00004000;

    public static final int HIGH_PRIORITY_CLASS = 0x00000080;

    public static final int IDLE_PRIORITY_CLASS = 0x00000040;

    public static final int NORMAL_PRIORITY_CLASS = 0x00000020;

    public static final int PROCESS_MODE_BACKGROUND_BEGIN = 0x00100000;

    public static final int PROCESS_MODE_BACKGROUND_END = 0x00200000;

    public static final int REALTIME_PRIORITY_CLASS = 0x00000100;

    //	__out
//	HANDLE
//	WINAPI
//	CreateNamedPipe(
//	    __in     LPCWSTR lpName,
//	    __in     DWORD dwOpenMode,
//	    __in     DWORD dwPipeMode,
//	    __in     DWORD nMaxInstances,
//	    __in     DWORD nOutBufferSize,
//	    __in     DWORD nInBufferSize,
//	    __in     DWORD nDefaultTimeOut,
//	    __in_opt LPSECURITY_ATTRIBUTES lpSecurityAttributes
//	    );
    HANDLE CreateNamedPipe(String lpName, int dwOpenMode, int dwPipeMode, int nMaxInstances, int nOutBufferSize, int nInBufferSize, int nDefaultTimeOut,
            WinBase.SECURITY_ATTRIBUTES lpSecurityAttributes);

    //    WINBASEAPI
//    BOOL
//    WINAPI
//    ConnectNamedPipe(
//        __in        HANDLE hNamedPipe,
//        __inout_opt LPOVERLAPPED lpOverlapped
//        );
    boolean ConnectNamedPipe(HANDLE hNamedPipe, WinBase.OVERLAPPED lpOverlapped);

    //    WINBASEAPI
//    BOOL
//    WINAPI
//    DisconnectNamedPipe(
//        __in HANDLE hNamedPipe
//        );
    boolean DisconnectNamedPipe(HANDLE hNamedPipe);

    /**
     * Waits until the specified object is in the signaled state or the time-out
     * interval elapses. To enter an alertable wait state, use the
     * WaitForSingleObjectEx function. To wait for multiple objects, use the
     * WaitForMultipleObjects.
     *
     * @param hHandle A handle to the object. For a list of the object types
     * whose handles can be specified, see the following Remarks section. If
     * this handle is closed while the wait is still pending, the function's
     * behavior is undefined. The handle must have the SYNCHRONIZE access right.
     * For more information, see Standard Access Rights.
     * @param dwMilliseconds The time-out interval, in milliseconds. If a
     * nonzero value is specified, the function waits until the object is
     * signaled or the interval elapses. If dwMilliseconds is zero, the function
     * does not enter a wait state if the object is not signaled; it always
     * returns immediately. If dwMilliseconds is INFINITE, the function will
     * return only when the object is signaled.
     * @return If the function succeeds, the return value indicates the event
     * that caused the function to return.
     */
    int WaitForSingleObject(HANDLE hHandle, int dwMilliseconds);

    /**
     * This function returns a pseudohandle for the current process.
     *
     * @return The return value is a pseudohandle to the current process.
     */
    HANDLE GetCurrentProcess();

    int SetProcessAffinityMask(HANDLE hProcess, int mask);

    int SetPriorityClass(HANDLE hProcess, int dwPriorityClass);

    /**
     * This function returns a handle to an existing process object.
     *
     * @param fdwAccess Not supported; set to zero.
     * @param fInherit Not supported; set to FALSE.
     * @param IDProcess Specifies the process identifier of the process to open.
     * @return An open handle to the specified process indicates success. NULL
     * indicates failure. To get extended error information, call GetLastError.
     */
    HANDLE OpenProcess(int fdwAccess, boolean fInherit, DWORD IDProcess);

    /**
     * The GetSystemInfo function returns information about the current system.
     *
     * @param lpSystemInfo Pointer to a SYSTEM_INFO structure that receives the
     * information.
     */
    void GetSystemInfo(SYSTEM_INFO lpSystemInfo);

    public static final int PROCESS_VM_READ = 0x0010;

    public static final int PROCESS_VM_WRITE = 0x0020;

    public static final int PROCESS_QUERY_INFORMATION = 0x0400;

    public static final int PROCESS_VM_OPERATION = 0x0008;

    SIZE_T VirtualQueryEx(HANDLE hProcess, Pointer lpAddress, MEMORY_BASIC_INFORMATION lpBuffer, SIZE_T dwLength);

    /**
     * The GetLastError function retrieves the calling thread's last-error code
     * value. The last-error code is maintained on a per-thread basis. Multiple
     * threads do not overwrite each other's last-error code.
     *
     * @return The return value is the calling thread's last-error code value.
     */
    int GetLastError();

    public static int MEM_COMMIT = 0x1000;

    public static int MEM_FREE = 0x10000;

    public static int MEM_RESERVE = 0x2000;

    public static int MEM_IMAGE = 0x1000000;

    public static int MEM_MAPPED = 0x40000;

    public static int MEM_PRIVATE = 0x20000;

    boolean ReadProcessMemory(HANDLE hProcess, Pointer inBaseAddress, Pointer outputBuffer, NativeLong nSize, NativeLongByReference outNumberOfBytesRead);

    /**
     * Takes a snapshot of the specified processes, as well as the heaps,
     * modules, and threads used by these processes.
     *
     * @param dwFlags The portions of the system to be included in the snapshot.
     * @param th32ProcessID The process identifier of the process to be included
     * in the snapshot. This parameter can be zero to indicate the current
     * process. This parameter is used when the TH32CS_SNAPHEAPLIST,
     * TH32CS_SNAPMODULE, TH32CS_SNAPMODULE32, or TH32CS_SNAPALL value is
     * specified. Otherwise, it is ignored and all processes are included in the
     * snapshot.
     * <p>
     * If the specified process is the Idle process or one of the CSRSS
     * processes, this function fails and the last error code is
     * ERROR_ACCESS_DENIED because their access restrictions prevent user-level
     * code from opening them.
     * <p>
     * If the specified process is a 64-bit process and the caller is a 32-bit
     * process, this function fails and the last error code is
     * ERROR_PARTIAL_COPY (299).
     * @return If the function succeeds, it returns an open handle to the
     * specified snapshot.
     * <p>
     * If the function fails, it returns INVALID_HANDLE_VALUE. To get extended
     * error information, call GetLastError. Possible error codes include
     * ERROR_BAD_LENGTH.
     */
    HANDLE CreateToolhelp32Snapshot(DWORD dwFlags, DWORD th32ProcessID);

    /**
     * Retrieves information about the first process encountered in a system
     * snapshot.
     *
     * @param hSnapshot A handle to the snapshot returned from a previous call
     * to the CreateToolhelp32Snapshot function.
     * @param lppe A pointer to a PROCESSENTRY32 structure. It contains process
     * information such as the name of the executable file, the process
     * identifier, and the process identifier of the parent process.
     * @return Returns TRUE if the first entry of the process list has been
     * copied to the buffer or FALSE otherwise. The ERROR_NO_MORE_FILES error
     * value is returned by the GetLastError function if no processes exist or
     * the snapshot does not contain process information.
     */
    boolean Process32First(HANDLE hSnapshot, PROCESSENTRY32 lppe);

    /**
     * Retrieves information about the next process recorded in a system
     * snapshot.
     *
     * @param hSnapshot A handle to the snapshot returned from a previous call
     * to the CreateToolhelp32Snapshot function.
     * @param lppe A pointer to a PROCESSENTRY32 structure.
     * @return Returns TRUE if the next entry of the process list has been
     * copied to the buffer or FALSE otherwise. The ERROR_NO_MORE_FILES error
     * value is returned by the GetLastError function if no processes exist or
     * the snapshot does not contain process information.
     */
    boolean Process32Next(HANDLE hSnapshot, PROCESSENTRY32 lppe);

    public static int TH32CS_SNAPPROCESS = 0x00000002;

    //Needed for some Windows 7 Versions
    //boolean EnumProcesses(int[] ProcessIDsOut, int size, int[] BytesReturned);
    int GetProcessImageFileNameW(HANDLE Process, char[] outputname, int length);

    DWORD QueryDosDevice(String lpDeviceName, char[] lpTargetPath, int length);

    boolean VirtualProtectEx(HANDLE hProcess, LPVOID lpAddress, SIZE_T dwSize, int flNewProtect, IntByReference lpflOldProtect);

    public static final int LOCALE_SISO3166CTRYNAME = 90;

    public static final int LOCALE_SISO639LANGNAME = 89;

    int GetLocaleInfo(int Locale, int LCType, char[] lpLCData, int cchData);

    public HANDLE CreateMutex(WinBase.SECURITY_ATTRIBUTES sa, boolean initialOwner, String name);

    HANDLE CreateFile(String lpFileName, int dwDesiredAccess, int dwShareMode,
            WinBase.SECURITY_ATTRIBUTES lpSecurityAttributes, int dwCreationDisposition,
            int dwFlagsAndAttributes, HANDLE hTemplateFile);
}
