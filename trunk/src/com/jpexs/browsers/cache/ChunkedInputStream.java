package com.jpexs.browsers.cache;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author JPEXS
 */
public class ChunkedInputStream extends InputStream {

    private InputStream is;
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
        String ret = "";
        while ((i = is.read()) > -1) {
            if (inr) {
                inr = false;
                if (i == '\n') {
                    break;
                } else {
                    ret += "\r";
                }
            }
            if (i == '\r') {
                inr = true;
                continue;
            }
            ret += (char) i;
        }
        if (inr) {
            ret += "\r";
        }
        return ret;
    }
}
