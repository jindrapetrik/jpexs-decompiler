package com.jpexs.decompiler.flash.amf.amf3;

import java.io.IOException;
import java.io.OutputStream;

public class Amf3OutputStream_ extends OutputStream {

    private final OutputStream os;

    public Amf3OutputStream_(OutputStream os) {
        this.os = os;
    }

    public void writeUI8(int v) throws IOException {
        write(v);
    }

    public void writeUI16(int v) throws IOException {
        int b1 = (v >> 8) & 0xff;
        int b2 = v & 0xff;
        write(b1);
        write(b2);
    }

    public void writeUI32(long v) throws IOException {
        int b1 = (int) ((v >> 24) & 0xff);
        int b2 = (int) ((v >> 16) & 0xff);
        int b3 = (int) ((v >> 8) & 0xff);
        int b4 = (int) (v & 0xff);

        write(b1);
        write(b2);
        write(b3);
        write(b4);
    }

    public void writeDouble(double v) throws IOException {
        writeUI32(Double.doubleToLongBits(v));
    }

    public void writeUI29(long v) throws IOException {
        if (v <= 0x7F) {
            write((int) v);
        } else if (v <= 0x3FFF) {
            int b1 = (int) ((v >> 7) & 0x7F);
            int b2 = (int) (v & 0x7F);
            write(b1);
            write(b2);
        } else if (v <= 0x1FFFFF) {
            int b1 = (int) ((v >> 14) & 0x7F);
            int b2 = (int) ((v >> 7) & 0x7F);
            int b3 = (int) (v & 0x7F);
            write(b1);
            write(b2);
            write(b3);
        } else if (v <= 0x3FFFFFFF) {
            int b1 = (int) ((v >> 21) & 0x7F);
            int b2 = (int) ((v >> 14) & 0x7F);
            int b3 = (int) ((v >> 7) & 0x7F);
            int b4 = (int) (v & 0x7F);
            write(b1);
            write(b2);
            write(b3);
            write(b4);
        } else {
            throw new IllegalArgumentException("Value too long");
        }
    }

    @Override
    public void write(int v) throws IOException {
        os.write(v);
    }

    public void writeValue(Object object) throws IOException {
        //TODO:!!!
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
