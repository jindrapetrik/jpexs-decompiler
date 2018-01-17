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
package com.jpexs.browsers.cache.firefox;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author JPEXS
 */
public class CacheInputStream extends InputStream {

    private final InputStream is;

    @Override
    public int available() throws IOException {
        return is.available();
    }

    public CacheInputStream(InputStream is) {
        this.is = is;
    }

    @Override
    public int read() throws IOException {
        return is.read();
    }

    public long readInt32() throws IOException {
        return (read() << 24) + (read() << 16) + (read() << 8) + read();
    }

    public int readInt16() throws IOException {
        return (read() << 8) + read();
    }
}
