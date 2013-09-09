package com.jpexs.browsers.cache.firefox;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author JPEXS
 */
public class CacheInputStream extends InputStream {

    private InputStream is;

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
