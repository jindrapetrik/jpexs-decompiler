package com.jpexs.helpers;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author JPEXS
 */
public class LimitedInputStream extends InputStream {

    private InputStream is;
    private long pos = 0;
    private long limit;

    public LimitedInputStream(InputStream is, long limit) {
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
            avail = (int) (limit - pos);
        }
        return avail;
    }
}
