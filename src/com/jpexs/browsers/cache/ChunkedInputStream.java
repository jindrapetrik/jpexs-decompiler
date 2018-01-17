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

/**
 *
 * @author JPEXS
 */
public class ChunkedInputStream extends InputStream {

    private final InputStream is;

    private int chunkPos = 0;

    private int chunkLen = 0;

    private boolean end = false;

    private boolean first = true;

    public ChunkedInputStream(InputStream is) {
        this.is = is;
    }

    @Override
    public int read() throws IOException {
        if (end) {
            return -1;
        }
        if (chunkPos >= chunkLen) {
            if (!first) {
                if (is.read() != '\r') {
                    throw new IOException("Invalid chunk");
                }
                if (is.read() != '\n') {
                    throw new IOException("Invalid chunk");
                }
            }
            String lenStr = readLine();
            try {
                chunkLen = Integer.parseInt(lenStr);
            } catch (NumberFormatException nfe) {
                throw new IOException("Invalid chunk");
            }
            if (chunkLen == 0) {
                is.read(); // \r
                is.read(); // \n
                end = true;
                return -1;
            }
            chunkPos = 0;
            first = false;
        }

        chunkPos++;
        return is.read();
    }

    private String readLine() throws IOException {
        int i;
        boolean inr = false;
        StringBuilder ret = new StringBuilder();
        while ((i = is.read()) > -1) {
            if (inr) {
                inr = false;
                if (i == '\n') {
                    break;
                } else {
                    ret.append("\r");
                }
            }
            if (i == '\r') {
                inr = true;
                continue;
            }
            ret.append((char) i);
        }
        if (inr) {
            ret.append("\r");
        }
        return ret.toString();
    }
}
