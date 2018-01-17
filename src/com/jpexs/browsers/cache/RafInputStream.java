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
package com.jpexs.browsers.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class RafInputStream extends InputStream {

    private final RandomAccessFile raf;

    private long pos = 0;

    public RafInputStream(RandomAccessFile raf) {
        this.raf = raf;
        try {
            pos = raf.getFilePointer();
        } catch (IOException ex) {
            Logger.getLogger(RafInputStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public int read() throws IOException {
        raf.seek(pos++);
        return raf.read();
    }
}
