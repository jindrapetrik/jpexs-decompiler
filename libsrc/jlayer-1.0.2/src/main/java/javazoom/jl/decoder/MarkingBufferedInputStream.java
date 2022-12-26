package javazoom.jl.decoder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author JPEXS
 */
public class MarkingBufferedInputStream extends BufferedInputStream {
    
    private BufferedInputStream is;
    
    private long pos = 0;
    
    private long markedPos = -1;
    
    private boolean fixed = false;

    public long getPosition() {
        return pos;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }
            
    
    
    public MarkingBufferedInputStream(InputStream in) {
        super(in);
        this.is = new BufferedInputStream(in);
    }   

    @Override
    public synchronized int read() throws IOException {
        if (!fixed) pos++;
        return is.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        int ret = is.read(b);
        if (!fixed) pos += ret;
        return ret;
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        int ret = is.read(b, off, len);
        if (!fixed) pos += ret;
        return ret;
    }       

    @Override
    public synchronized void reset() throws IOException {
        if (markedPos > -1) {
            if (!fixed) pos = markedPos;
            markedPos = -1;
        }
        is.reset();
    }

    @Override
    public synchronized void mark(int readlimit) {
        markedPos = pos;
        is.mark(readlimit);
    }           

    @Override
    public synchronized int available() throws IOException {
        return is.available();
    }

    @Override
    public synchronized long skip(long n) throws IOException {        
        long ret = is.skip(n);
        if (!fixed) pos+=ret;
        return ret;
    }

    @Override
    public void skipNBytes(long n) throws IOException {
        if (!fixed) pos+=n;
        is.skipNBytes(n);
    }

    @Override
    public void close() throws IOException {
        is.close();
    }

    @Override
    public boolean markSupported() {
        return is.markSupported();
    }           
        
}
