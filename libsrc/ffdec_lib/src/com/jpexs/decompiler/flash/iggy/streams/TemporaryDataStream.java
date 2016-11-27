package com.jpexs.decompiler.flash.iggy.streams;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class TemporaryDataStream extends RandomAccessFileDataStream {

    public TemporaryDataStream() throws IOException {
        this(new byte[0]);
    }

    public TemporaryDataStream(byte[] data) throws IOException {
        super(File.createTempFile("tempdatastream", ".bin"));
        this.getFile().deleteOnExit();
        writeBytes(data);
        seek(0, SeekMode.SET);
    }

    @Override
    public void close() {
        try {
            this.getFile().delete();
        } catch (Exception ex) {
            //ignore
        }
    }
}
