/*
 *  Copyright (C) 2010-2018 JPEXS
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
package com.jpexs.decompiler.flash.gui.pipes;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author JPEXS
 */
public class PipeOutputStream extends OutputStream {

    protected HANDLE pipe;

    private boolean closed = false;

    public PipeOutputStream(String pipeName, boolean newPipe) throws IOException {
        if (!Platform.isWindows()) {
            throw new IOException("Cannot create Pipe on nonWindows OS");
        }
        String fullPipePath = "\\\\.\\pipe\\" + pipeName;
        if (newPipe) {
            pipe = Kernel32.INSTANCE.CreateNamedPipe(fullPipePath, Kernel32.PIPE_ACCESS_OUTBOUND, Kernel32.PIPE_TYPE_BYTE, 1, 4096, 4096, 0, null);
            if (pipe == null || !Kernel32.INSTANCE.ConnectNamedPipe(pipe, null)) {
                throw new IOException("Cannot connect to the pipe. Error " + Kernel32.INSTANCE.GetLastError());
            }
        } else {
            pipe = Kernel32.INSTANCE.CreateFile(fullPipePath, Kernel32.GENERIC_WRITE, Kernel32.FILE_SHARE_WRITE, null, Kernel32.OPEN_EXISTING, Kernel32.FILE_ATTRIBUTE_NORMAL, null);
            if (pipe == null) {
                throw new IOException("Cannot connect to the pipe. Error " + Kernel32.INSTANCE.GetLastError());
            }
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    close();
                } catch (IOException ex) {
                    //ignore
                }
            }

        });
    }

    @Override
    public synchronized void close() throws IOException {
        if (!closed) {
            Kernel32.INSTANCE.CloseHandle(pipe);
            closed = true;
        }
    }

    @Override
    public synchronized void write(int b) throws IOException {
        byte[] data = new byte[]{(byte) b};
        IntByReference ibr = new IntByReference();
        boolean result = Kernel32.INSTANCE.WriteFile(pipe, data, data.length, ibr, null);
        if (!result) {
            throw new IOException("Cannot write to the pipe. Error " + Kernel32.INSTANCE.GetLastError());
        }
        if (ibr.getValue() != data.length) {
            throw new IOException("Cannot write to the pipe. Error " + Kernel32.INSTANCE.GetLastError());
        }
    }
}
