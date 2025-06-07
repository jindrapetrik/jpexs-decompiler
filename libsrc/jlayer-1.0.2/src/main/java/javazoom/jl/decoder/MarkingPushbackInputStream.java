package javazoom.jl.decoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 *
 * @author JPEXS
 */
public class MarkingPushbackInputStream extends PushbackInputStream {
    
    private PushbackInputStream is;
    
    private long pos = 0;

    public long getPosition() {
        return pos;
    }
            
    public MarkingPushbackInputStream(InputStream in, int size) {
        super(in, size);
        is = new PushbackInputStream(in, size);
    }

    @Override
    public int available() throws IOException {
        return is.available();
    }

    @Override
    public synchronized void close() throws IOException {
        is.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        is.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return is.markSupported();
    }

    @Override
    public int read() throws IOException {
        int ret = is.read();
        if (ret > -1) {
            pos++;
        }
        return ret;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        
        int ret = is.read(b, off, len);
        if (ret > -1) {
            pos += ret;
        }
        return ret;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int ret = is.read(b);
        if (ret > -1) {
            pos += ret;
        }        
        return ret;
    }

    @Override
    public synchronized void reset() throws IOException {
        is.reset();
    }

    @Override
    public void unread(byte[] b) throws IOException {
        pos -= b.length;
        is.unread(b);
    }

    @Override
    public void unread(int b) throws IOException {
        pos--;
        is.unread(b);
    }

    @Override
    public void unread(byte[] b, int off, int len) throws IOException {
        pos -= len;
        is.unread(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        long ret = is.skip(n);
        pos += ret;
        return ret;
    }

    @Override
    public void skipNBytes(long n) throws IOException {
        pos += n;
        is.skipNBytes(n);
    }                                                    
}
