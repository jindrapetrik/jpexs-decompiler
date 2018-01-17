/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. */
package com.jpexs.decompiler.flash.abc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author JPEXS
 */
public class CopyOutputStream extends OutputStream {

    private final OutputStream os;

    private final InputStream is;

    private long pos = 0;

    private final int TEMPSIZE = 5;

    private final int[] temp = new int[TEMPSIZE];

    private int tempPos = 0;

    public int ignoreFirst = 0;

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
            if (ignoreFirst <= 0) {
                os.flush();

                boolean output = true;

                if (output) {
                    System.out.println("Position: " + pos);
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
            } else {
                ignoreFirst--;
            }
        }
        os.write(b);
    }
}
