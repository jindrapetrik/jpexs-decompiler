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
import java.io.InputStream;

/**
 *
 * @author JPEXS
 */
public class PipeInputStream extends InputStream {

    protected HANDLE pipe;

    private boolean closed = false;

    public PipeInputStream(String pipeName, boolean newpipe) throws IOException {
        if (!Platform.isWindows()) {
            throw new IOException("Cannot create Pipe on nonWindows OS");
        }
        String fullPipePath = "\\\\.\\pipe\\" + pipeName;
        if (newpipe) {
            pipe = Kernel32.INSTANCE.CreateNamedPipe(fullPipePath, Kernel32.PIPE_ACCESS_INBOUND, Kernel32.PIPE_TYPE_BYTE, 1, 4096, 4096, 0, null);
            if (pipe == null || !Kernel32.INSTANCE.ConnectNamedPipe(pipe, null)) {
                throw new IOException("Cannot connect to the pipe");
            }
        } else {
            pipe = Kernel32.INSTANCE.CreateFile(fullPipePath, Kernel32.GENERIC_READ, Kernel32.FILE_SHARE_READ, null, Kernel32.OPEN_EXISTING, Kernel32.FILE_ATTRIBUTE_NORMAL, null);
        }
        if (pipe == null) {
            throw new IOException("Cannot connect to the pipe");
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
    public synchronized int read() throws IOException {
        byte[] d = new byte[1];
        if (readPipe(d) == 0) {
            return -1;
        }
        return d[0];
    }

    private int readPipe(byte res[]) throws IOException {
        final IntByReference ibr = new IntByReference();
        int read = 0;
        while (read < res.length) {
            byte[] data = new byte[res.length - read];
            boolean result = Kernel32.INSTANCE.ReadFile(pipe, data, data.length, ibr, null);
            if (!result) {
                throw new IOException("Cannot read pipe. Error " + Kernel32.INSTANCE.GetLastError());
            }
            int readNow = ibr.getValue();
            System.arraycopy(data, 0, res, read, readNow);
            read += readNow;
        }
        return read;
    }
}
