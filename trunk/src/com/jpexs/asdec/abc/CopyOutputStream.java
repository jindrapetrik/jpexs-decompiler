/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CopyOutputStream extends OutputStream {
    private OutputStream os;
    private InputStream is;
    private long pos = 0;
    private int TEMPSIZE = 5;
    private int temp[] = new int[TEMPSIZE];
    private int tempPos = 0;

    public CopyOutputStream(OutputStream os, InputStream is) {
        this.os = os;
        this.is = is;
    }

    @Override
    public void write(int b) throws IOException {
        temp[tempPos] = b;
        tempPos = (tempPos + 1) % TEMPSIZE;

        pos++;
        int r = is.read();
        if ((b & 0xff) != r) {
            os.flush();

            boolean output = false;

            if (output) {
                System.out.print("Last written:");
                for (int i = 0; i < TEMPSIZE; i++) {
                    System.out.print("" + Integer.toHexString(temp[(tempPos + i) % TEMPSIZE]) + " ");
                }
                System.out.println("");
                System.out.println("More expected:");
                for (int i = 0; i < TEMPSIZE; i++) {
                    System.out.println("" + Integer.toHexString(is.read()));
                }

                System.out.println("");
                System.out.println(Integer.toHexString(r) + " expected but " + Integer.toHexString(b) + " found");
            }
            throw new NotSameException(pos);
        }
        os.write(b);
    }
}
