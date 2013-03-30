package com.jpexs.decompiler.flash.flv;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author JPEXS
 */
public class FLVOutputStream extends OutputStream {

    private OutputStream os;
    private int bitPos = 0;
    private int tempByte = 0;
    private long pos = 0;

    public FLVOutputStream(OutputStream os) {
        this.os = os;
    }

    public long getPos() {
        return pos;
    }

    /**
     * Writes byte to the stream
     *
     * @param b byte to write
     * @throws IOException
     */
    @Override
    public void write(int b) throws IOException {
        alignByte();
        os.write(b);
        pos++;
    }

    private void alignByte() throws IOException {
        if (bitPos > 0) {
            bitPos = 0;
            write(tempByte);
            tempByte = 0;
        }
    }

    /**
     * Writes UI8 (Unsigned 8bit integer) value to the stream
     *
     * @param val UI8 value to write
     * @throws IOException
     */
    public void writeUI8(int val) throws IOException {
        write(val);
    }

    /**
     * Writes UI24 (Unsigned 24bit integer) value to the stream
     *
     * @param value UI32 value
     * @throws IOException
     */
    public void writeUI24(long value) throws IOException {
        write((int) ((value >> 16) & 0xff));
        write((int) ((value >> 8) & 0xff));
        write((int) (value & 0xff));

    }

    /**
     * Writes UI32 (Unsigned 32bit integer) value to the stream
     *
     * @param value UI32 value
     * @throws IOException
     */
    public void writeUI32(long value) throws IOException {
        write((int) ((value >> 24) & 0xff));
        write((int) ((value >> 16) & 0xff));
        write((int) ((value >> 8) & 0xff));
        write((int) (value & 0xff));
    }

    /**
     * Writes UI16 (Unsigned 16bit integer) value to the stream
     *
     * @param value UI16 value
     * @throws IOException
     */
    public void writeUI16(int value) throws IOException {
        write((int) ((value >> 8) & 0xff));
        write((int) (value & 0xff));
    }

    /**
     * Writes UB[nBits] (Unsigned-bit value) value to the stream
     *
     * @param nBits Number of bits which represent value
     * @param value Unsigned value to write
     * @throws IOException
     */
    public void writeUB(int nBits, long value) throws IOException {
        for (int bit = 0; bit < nBits; bit++) {
            int nb = (int) ((value >> (nBits - 1 - bit)) & 1);
            tempByte += nb * (1 << (7 - bitPos));
            bitPos++;
            if (bitPos == 8) {
                bitPos = 0;
                write(tempByte);
                tempByte = 0;
            }
        }
    }

    public void writeHeader(boolean audio, boolean video) throws IOException {
        write("FLV".getBytes());
        write(1); //version
        writeUB(5, 0); //must be 0
        writeUB(1, audio ? 1 : 0); //audio present
        writeUB(1, 0); //reserved
        writeUB(1, video ? 1 : 0); //video present
        writeUI32(9);  //header size            
        writeUI32(0);
    }

    public void writeTag(FLVTAG tag) throws IOException {
        long posBefore = getPos();
        writeUI8(tag.tagType);
        byte data[] = tag.data.getBytes();
        writeUI24(data.length);
        writeUI24(tag.timeStamp & 0xffffff);
        writeUI8((int) ((tag.timeStamp >> 24) & 0xff));
        writeUI24(0);
        write(data);
        long posAfter = getPos();
        long size = posAfter - posBefore;
        writeUI32(size);
    }
}
