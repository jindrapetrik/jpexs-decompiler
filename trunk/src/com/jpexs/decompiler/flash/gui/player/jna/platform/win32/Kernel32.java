/* Copyright (c) 2007 Timothy Wall, All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package com.jpexs.decompiler.flash.gui.player.jna.platform.win32;

import java.nio.Buffer;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.W32APIOptions;

// TODO: Auto-generated Javadoc
/**
 * Interface definitions for <code>kernel32.dll</code>. Includes additional
 * alternate mappings from {@link WinNT} which make use of NIO buffers.
 */
public interface Kernel32 extends WinNT {
	
	/** The instance. */
	Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32",
			Kernel32.class, W32APIOptions.UNICODE_OPTIONS);

	/**
	 * The CloseHandle function closes an open object handle.
	 * 
	 * @param hObject
	 *            Handle to an open object. This parameter can be a pseudo
	 *            handle or INVALID_HANDLE_VALUE.
	 * @return If the function succeeds, the return value is nonzero. If the
	 *         function fails, the return value is zero. To get extended error
	 *         information, call GetLastError.
	 */
	boolean CloseHandle(HANDLE hObject);

	/**
	 * Terminates the specified process and all of its threads.
	 * 
	 * @param hProcess
	 *            A handle to the process to be terminated.
	 * @param uExitCode
	 *            The exit code to be used by the process and threads terminated
	 *            as a result of this call.
	 * @return If the function succeeds, the return value is nonzero.
	 * 
	 *         If the function fails, the return value is zero. To get extended
	 *         error information, call GetLastError.
	 */
	boolean TerminateProcess(HANDLE hProcess, int uExitCode);

	/**
	 * Writes data to the specified file or input/output (I/O) device.
	 * 
	 * @param hFile
	 *            A handle to the file or I/O device (for example, a file, file
	 *            stream, physical disk, volume, console buffer, tape drive,
	 *            socket, communications resource, mailslot, or pipe).
	 * @param lpBuffer
	 *            A pointer to the buffer containing the data to be written to
	 *            the file or device.
	 * @param nNumberOfBytesToWrite
	 *            The number of bytes to be written to the file or device.
	 * @param lpNumberOfBytesWritten
	 *            A pointer to the variable that receives the number of bytes
	 *            written when using a synchronous hFile parameter.
	 * @param lpOverlapped
	 *            A pointer to an OVERLAPPED structure is required if the hFile
	 *            parameter was opened with FILE_FLAG_OVERLAPPED, otherwise this
	 *            parameter can be NULL.
	 * @return If the function succeeds, the return value is nonzero (TRUE). If
	 *         the function fails, or is completing asynchronously, the return
	 *         value is zero (FALSE). To get extended error information, call
	 *         the GetLastError function.
	 */
	boolean WriteFile(HANDLE hFile, byte[] lpBuffer, int nNumberOfBytesToWrite,
			IntByReference lpNumberOfBytesWritten,
			WinBase.OVERLAPPED lpOverlapped);


   
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
}
