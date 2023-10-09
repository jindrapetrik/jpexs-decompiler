/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
 * License along with this library.
 */
package com.jpexs.helpers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author JPEXS
 */
public class JpegAnalyzer {

    public void analyze(InputStream is) throws IOException {
        int val;

        while (true) {
            val = is.read();
            if (val == -1) {
                break;
            }
            if (val == 0xFF) {
                val = is.read();
                if (val == -1) {
                    break;
                }
                if (val != 0) {
                    int len = 2;
                    if (JpegMarker.markerHasLength(val)) {
                        int len1 = is.read();
                        int len2 = is.read();
                        len = (len1 << 8) + len2;
                        is.skip(len - 2);
                    }
                    System.out.println("marker " + JpegMarker.markerToString(val) + " len: " + len);
                }
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        JpegAnalyzer analyzer = new JpegAnalyzer();
        analyzer.analyze(new FileInputStream("sample.jpg"));
    }
}
