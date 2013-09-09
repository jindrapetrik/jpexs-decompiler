package com.jpexs.browsers.cache;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author JPEXS
 */
public class LimitedInputStream extends InputStream {

    private InputStream is;
    private int pos = 0;
    private int limit;

    public LimitedInputStream(InputStream is, int limit) {
        this.is = is;
        this.limit = limit;
    }

    @Override
    public int read() throws IOException {
        if (pos >= limit) {
            return -1;
        }
        pos++;
        return is.read();
    }

    @Override
    public int available() throws IOException {
        int avail = is.available();
        if (pos + avail > limit) {
            avail = limit - pos;
        }
        return avail;
    }
}
