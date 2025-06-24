/*
 *  Copyright (C) 2022-2025 JPEXS
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * FileInputStream to which can be passed /dev/stdin as special file for stdin
 * on Windows. On linux, standard /dev/stdin is used.
 *
 * @author JPEXS
 */
public class StdInAwareFileInputStream extends InputStream implements AutoCloseable {

    public static final String STDIN_PATH = "/dev/stdin";

    private InputStream is;

    public StdInAwareFileInputStream(File file) throws FileNotFoundException {
        String absPath = file.getPath().replace("\\", "/");
        if (absPath.equals(STDIN_PATH) && !file.exists()) {
            is = System.in;
        } else {
            is = new FileInputStream(file);
        }
    }

    public StdInAwareFileInputStream(String file) throws FileNotFoundException {
        this(new File(file));
    }

    @Override
    public int available() throws IOException {
        return is.available();
    }

    @Override
    public long skip(long n) throws IOException {
        return is.skip(n);
    }

    @Override
    public synchronized void reset() throws IOException {
        is.reset();
    }

    @Override
    public void close() throws IOException {
        is.close();
    }

    @Override
    public int read() throws IOException {
        return is.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return is.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return is.read(b, off, len);
    }
}
