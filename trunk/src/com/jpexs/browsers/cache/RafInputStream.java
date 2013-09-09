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

    private RandomAccessFile raf;
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
