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
package com.jpexs.decompiler.flash.types.gfx;

import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import java.io.IOException;
import java.io.Serializable;

/**
 *
 * @author JPEXS
 */
public class EdgeType implements Serializable {

    private static final int[] sizes = new int[]{1, 2, 1, 2, 1, 2, 3, 4, 2, 3, 4, 5, 6, 7, 8, 9};

    private static final int Edge_H12 = 0;  //  2 bytes

    private static final int Edge_H20 = 1;  //  3 bytes

    private static final int Edge_V12 = 2;  //  2 bytes

    private static final int Edge_V20 = 3;  //  3 bytes

    private static final int Edge_L6 = 4;  //  2 bytes

    private static final int Edge_L10 = 5;  //  3 bytes

    private static final int Edge_L14 = 6;  //  4 bytes

    private static final int Edge_L18 = 7;  //  5 bytes

    private static final int Edge_C5 = 8;  //  3 bytes

    private static final int Edge_C7 = 9;  //  4 bytes

    private static final int Edge_C9 = 10; //  5 bytes

    private static final int Edge_C11 = 11; //  6 bytes

    private static final int Edge_C13 = 12; //  7 bytes

    private static final int Edge_C15 = 13; //  8 bytes

    private static final int Edge_C17 = 14; //  9 bytes

    private static final int Edge_C19 = 15;  // 10 bytes

    private static final int Edge_HLine = 0;

    private static final int Edge_VLine = 1;

    private static final int Edge_Line = 2;

    private static final int Edge_Quad = 3;

    public int[] data;

    public EdgeType(boolean vertical, int v) {
        data = new int[]{vertical ? Edge_VLine : Edge_HLine, v};
    }

    public EdgeType(int x, int y) {
        data = new int[]{Edge_Line, x, y};
    }

    public EdgeType(int cx, int cy, int ax, int ay) {
        data = new int[]{Edge_Quad, cx, cy, ax, ay};
    }

    @Override
    public String toString() {
        String ret = "[Edge data:";
        for (int i = 0; i < data.length; i++) {
            if (i > 0) {
                ret += ", ";
            }
            ret += "" + data[i];
        }
        ret += "]";
        return ret;
    }

    public EdgeType(GFxInputStream sis) throws IOException {
        data = readEdge(sis);
        /*if((raw[0]&0xf) == Edge_V20){
         data[1] = 0;
         }
         if((raw[0]&0xf) == Edge_C17){
         //System.out.println("========== 17 : "+toString())    ;
         data[1] = 0;
         data[2] = 0;
         data[3] = 0;
         data[4] = 0;
         }*/
    }

    private static int SInt8(int val) {
        /*boolean sign = (val & 0x80) == 0x80;
         val = val & 0x7F;
         if (sign) {
         val = -val;
         }
         return val;*/
        return (byte) val;
    }

    public SHAPERECORD toSHAPERECORD() {
        int multiplier = 1;
        StraightEdgeRecord ser;
        CurvedEdgeRecord cer;
        switch (data[0]) {
            case Edge_HLine:
                ser = new StraightEdgeRecord();
                ser.generalLineFlag = false;
                ser.deltaX = data[1] * multiplier;
                ser.calculateBits();
                return ser;
            case Edge_VLine:
                ser = new StraightEdgeRecord();
                ser.generalLineFlag = false;
                ser.vertLineFlag = true;
                ser.deltaY = data[1] * multiplier;
                ser.calculateBits();
                return ser;
            case Edge_Line:
                ser = new StraightEdgeRecord();
                ser.generalLineFlag = true;
                ser.deltaX = data[1] * multiplier;
                ser.deltaY = data[2] * multiplier;
                ser.calculateBits();
                return ser;
            case Edge_Quad:
                cer = new CurvedEdgeRecord();
                cer.controlDeltaX = data[1] * multiplier;
                cer.controlDeltaY = data[2] * multiplier;
                cer.anchorDeltaX = data[3] * multiplier;
                cer.anchorDeltaY = data[4] * multiplier;
                cer.calculateBits();
                return cer;
        }
        return null;
    }

    private int[] readEdge(GFxInputStream sis) throws IOException {
        byte firstByte = (byte) sis.readUI8("firstByte");
        byte raw1, raw2, raw3, raw4, raw5, raw6, raw7, raw8, raw9;
        raw1 = (byte) sis.readUI8("byte1");
        int[] data = new int[5];

        switch (firstByte & 0xF) {
            case Edge_H12:
                data[0] = Edge_HLine;
                data[1] = ((firstByte & 0xff) >> 4) | (SInt8(raw1 & 0xff) << 4);
                break;

            case Edge_H20:
                raw2 = (byte) sis.readUI8("byte2");
                data[0] = Edge_HLine;
                data[1] = ((firstByte & 0xff) >> 4) | ((raw1 & 0xff) << 4) | (SInt8(raw2 & 0xff) << 12);
                break;

            case Edge_V12:
                data[0] = Edge_VLine;
                data[1] = ((firstByte & 0xff) >> 4) | (SInt8(raw1 & 0xff) << 4);
                break;

            case Edge_V20:
                data[0] = Edge_VLine;
                raw2 = (byte) sis.readUI8("byte2");
                data[1] = ((firstByte & 0xff) >> 4) | ((raw1 & 0xff) << 4) | (SInt8(raw2 & 0xff) << 12);
                break;

            case Edge_L6:
                data[0] = Edge_Line;
                data[1] = ((firstByte & 0xff) >> 4) | (SInt8((raw1 & 0xff) << 6) >> 2);
                data[2] = SInt8(raw1 & 0xff) >> 2;
                break;

            case Edge_L10:
                raw2 = (byte) sis.readUI8("byte2");
                data[0] = Edge_Line;
                data[1] = ((firstByte & 0xff) >> 4) | (SInt8((raw1 & 0xff) << 2) << 2);
                data[2] = ((raw1 & 0xff) >> 6) | (SInt8((raw2 & 0xff)) << 2);
                break;

            case Edge_L14:
                raw2 = (byte) sis.readUI8("byte2");
                raw3 = (byte) sis.readUI8("byte3");
                data[0] = Edge_Line;
                data[1] = ((firstByte & 0xff) >> 4) | ((raw1 & 0xff) << 4) | (SInt8((raw2 & 0xff) << 6) << 6);
                data[2] = ((raw2 & 0xff) >> 2) | (SInt8((raw3 & 0xff)) << 6);
                break;

            case Edge_L18:
                raw2 = (byte) sis.readUI8("byte2");
                raw3 = (byte) sis.readUI8("byte3");
                raw4 = (byte) sis.readUI8("byte4");
                data[0] = Edge_Line;
                data[1] = ((firstByte & 0xff) >> 4) | ((raw1 & 0xff) << 4) | (SInt8((raw2 & 0xff) << 2) << 10);
                data[2] = ((raw2 & 0xff) >> 6) | ((raw3 & 0xff) << 2) | (SInt8((raw4 & 0xff)) << 10);
                break;

            case Edge_C5:
                raw2 = (byte) sis.readUI8("byte2");
                data[0] = Edge_Quad;
                data[1] = ((firstByte & 0xff) >> 4) | (SInt8((raw1 & 0xff) << 7) >> 3);
                data[2] = SInt8((raw1 & 0xff) << 2) >> 3;
                data[3] = ((raw1 & 0xff) >> 6) | (SInt8((raw2 & 0xff) << 5) >> 3);
                data[4] = SInt8((raw2 & 0xff)) >> 3;
                break;

            case Edge_C7:
                raw2 = (byte) sis.readUI8("byte2");
                raw3 = (byte) sis.readUI8("byte3");
                data[0] = Edge_Quad;
                data[1] = ((firstByte & 0xff) >> 4) | (SInt8((raw1 & 0xff) << 5) >> 1);
                data[2] = ((raw1 & 0xff) >> 3) | (SInt8((raw2 & 0xff) << 6) >> 1);
                data[3] = ((raw2 & 0xff) >> 2) | (SInt8((raw3 & 0xff) << 7) >> 1);
                data[4] = SInt8(raw3) >> 1;
                break;

            case Edge_C9:
                raw2 = (byte) sis.readUI8("byte2");
                raw3 = (byte) sis.readUI8("byte3");
                raw4 = (byte) sis.readUI8("byte4");
                data[0] = Edge_Quad;
                data[1] = ((firstByte & 0xff) >> 4) | (SInt8((raw1 & 0xff) << 3) << 1);
                data[2] = ((raw1 & 0xff) >> 5) | (SInt8((raw2 & 0xff) << 2) << 1);
                data[3] = ((raw2 & 0xff) >> 6) | (SInt8((raw3 & 0xff) << 1) << 1);
                data[4] = ((raw3 & 0xff) >> 7) | (SInt8(raw4 & 0xff) << 1);
                break;

            case Edge_C11:
                raw2 = (byte) sis.readUI8("byte2");
                raw3 = (byte) sis.readUI8("byte3");
                raw4 = (byte) sis.readUI8("byte4");
                raw5 = (byte) sis.readUI8("byte5");
                data[0] = Edge_Quad;
                data[1] = ((firstByte & 0xff) >> 4) | (SInt8((raw1 & 0xff) << 1) << 3);
                data[2] = (raw1 >> 7) | ((raw2 & 0xff) << 1) | (SInt8((raw3 & 0xff) << 6) << 3);
                data[3] = ((raw3 & 0xff) >> 2) | (SInt8((raw4 & 0xff) << 3) << 3);
                data[4] = ((raw4 & 0xff) >> 5) | (SInt8(raw5 & 0xff) << 3);
                break;

            case Edge_C13:
                raw2 = (byte) sis.readUI8("byte2");
                raw3 = (byte) sis.readUI8("byte3");
                raw4 = (byte) sis.readUI8("byte4");
                raw5 = (byte) sis.readUI8("byte5");
                raw6 = (byte) sis.readUI8("byte6");
                data[0] = Edge_Quad;
                data[1] = ((firstByte & 0xff) >> 4) | ((raw1 & 0xff) << 4) | (SInt8((raw2 & 0xff) << 7) << 5);
                data[2] = ((raw2 & 0xff) >> 1) | (SInt8((raw3 & 0xff) << 2) << 5);
                data[3] = ((raw3 & 0xff) >> 6) | ((raw4 & 0xff) << 2) | (SInt8((raw5 & 0xff) << 5) << 5);
                data[4] = ((raw5 & 0xff) >> 3) | (SInt8(raw6 & 0xff) << 5);
                break;

            case Edge_C15:
                raw2 = (byte) sis.readUI8("byte2");
                raw3 = (byte) sis.readUI8("byte3");
                raw4 = (byte) sis.readUI8("byte4");
                raw5 = (byte) sis.readUI8("byte5");
                raw6 = (byte) sis.readUI8("byte6");
                raw7 = (byte) sis.readUI8("byte7");
                data[0] = Edge_Quad;
                data[1] = ((firstByte & 0xff) >> 4) | ((raw1 & 0xff) << 4) | (SInt8((raw2 & 0xff) << 5) << 7);
                data[2] = ((raw2 & 0xff) >> 3) | ((raw3 & 0xff) << 5) | (SInt8((raw4 & 0xff) << 6) << 7);
                data[3] = ((raw4 & 0xff) >> 2) | ((raw5 & 0xff) << 6) | (SInt8((raw6 & 0xff) << 7) << 7);
                data[4] = ((raw6 & 0xff) >> 1) | (SInt8((raw7 & 0xff)) << 7);
                break;

            case Edge_C17:
                raw2 = (byte) sis.readUI8("byte2");
                raw3 = (byte) sis.readUI8("byte3");
                raw4 = (byte) sis.readUI8("byte4");
                raw5 = (byte) sis.readUI8("byte5");
                raw6 = (byte) sis.readUI8("byte6");
                raw7 = (byte) sis.readUI8("byte7");
                raw8 = (byte) sis.readUI8("byte8");
                data[0] = Edge_Quad;
                data[1] = ((firstByte & 0xff) >> 4) | ((raw1 & 0xff) << 4) | (SInt8((raw2 & 0xff) << 3) << 9);
                data[2] = ((raw2 & 0xff) >> 5) | ((raw3 & 0xff) << 3) | (SInt8((raw4 & 0xff) << 2) << 9);
                data[3] = ((raw4 & 0xff) >> 6) | ((raw5 & 0xff) << 2) | (SInt8((raw6 & 0xff) << 1) << 9);
                data[4] = ((raw6 & 0xff) >> 7) | ((raw7 & 0xff) << 1) | (SInt8(raw8 & 0xff) << 9);
                break;

            case Edge_C19:
                raw2 = (byte) sis.readUI8("byte2");
                raw3 = (byte) sis.readUI8("byte3");
                raw4 = (byte) sis.readUI8("byte4");
                raw5 = (byte) sis.readUI8("byte5");
                raw6 = (byte) sis.readUI8("byte6");
                raw7 = (byte) sis.readUI8("byte7");
                raw8 = (byte) sis.readUI8("byte8");
                raw9 = (byte) sis.readUI8("byte9");
                data[0] = Edge_Quad;
                data[1] = ((firstByte & 0xff) >> 4) | ((raw1 & 0xff) << 4) | (SInt8(raw2 << 1) << 11);
                data[2] = ((raw2 & 0xff) >> 7) | ((raw3 & 0xff) << 1) | ((raw4 & 0xff) << 9) | (SInt8(raw5 << 6) << 11);
                data[3] = ((raw5 & 0xff) >> 2) | ((raw6 & 0xff) << 6) | (SInt8(raw7 << 3) << 11);
                data[4] = ((raw7 & 0xff) >> 5) | ((raw8 & 0xff) << 3) | (SInt8(raw9) << 11);
                break;
        }
        return data;
    }

    public void write(GFxOutputStream sos) throws IOException {
        int x;
        int y;
        int m1 = 1;
        int m2 = 3;
        int m3 = 7;
        int m4 = 0xF;
        int m5 = 0x1F;
        int m6 = 0x3F;
        int m7 = 0x7F;
        switch (data[0]) {
            case Edge_HLine:
                x = data[1];
                if (x >= GFxOutputStream.MinSInt12 && x <= GFxOutputStream.MaxSInt12) {
                    sos.writeUI8((x << 4) | Edge_H12);
                    sos.writeUI8(x >> 4);
                    return;
                }
                sos.writeUI8((x << 4) | Edge_H20);
                sos.writeUI8(x >> 4);
                sos.writeUI8(x >> 12);
                break;
            case Edge_VLine:
                y = data[1];
                if (y >= GFxOutputStream.MinSInt12 && y <= GFxOutputStream.MaxSInt12) {
                    sos.writeUI8((y << 4) | Edge_V12);
                    sos.writeUI8(y >> 4);
                    return;
                }
                sos.writeUI8((y << 4) | Edge_V20);
                sos.writeUI8(y >> 4);
                sos.writeUI8(y >> 12);
                return;
            case Edge_Line:
                x = data[1];
                y = data[2];
                if (x >= GFxOutputStream.MinSInt6 && x <= GFxOutputStream.MaxSInt6 && y >= GFxOutputStream.MinSInt6 && y <= GFxOutputStream.MaxSInt6) {
                    sos.writeUI8((x << 4) | Edge_L6);
                    sos.writeUI8(((x >> 4) & m2) | (y << 2));
                    return;
                }
                if (x >= GFxOutputStream.MinSInt10 && x <= GFxOutputStream.MaxSInt10 && y >= GFxOutputStream.MinSInt10 && y <= GFxOutputStream.MaxSInt10) {
                    sos.writeUI8((x << 4) | Edge_L10);
                    sos.writeUI8(((x >> 4) & m6) | (y << 6));
                    sos.writeUI8(y >> 2);
                    return;
                }
                if (x >= GFxOutputStream.MinSInt14 && x <= GFxOutputStream.MaxSInt14 && y >= GFxOutputStream.MinSInt14 && y <= GFxOutputStream.MaxSInt14) {
                    sos.writeUI8((x << 4) | Edge_L14);
                    sos.writeUI8(x >> 4);
                    sos.writeUI8(((x >> 12) & m2) | (y << 2));
                    sos.writeUI8(y >> 6);
                    return;
                }
                sos.writeUI8((x << 4) | Edge_L18);
                sos.writeUI8(x >> 4);
                sos.writeUI8(((x >> 12) & m6) | (y << 6));
                sos.writeUI8(y >> 2);
                sos.writeUI8(y >> 10);
                return;
            case Edge_Quad:
                int cx = data[1];
                int cy = data[2];
                int ax = data[3];
                int ay = data[4];
                int minV = cx;
                int maxV = cx;
                if (cy < minV) {
                    minV = cy;
                }
                if (cy > maxV) {
                    maxV = cy;
                }
                if (ax < minV) {
                    minV = ax;
                }
                if (ax > maxV) {
                    maxV = ax;
                }
                if (ay < minV) {
                    minV = ay;
                }
                if (ay > maxV) {
                    maxV = ay;
                }

                if (minV >= GFxOutputStream.MinSInt5 && maxV <= GFxOutputStream.MaxSInt5) {
                    sos.writeUI8(((cx << 4) | Edge_C5));
                    sos.writeUI8((((cx >> 4) & m1) | ((cy << 1) & m6) | (ax << 6)));
                    sos.writeUI8((((ax >> 2) & m3) | (ay << 3)));
                    return;
                }
                if (minV >= GFxOutputStream.MinSInt7 && maxV <= GFxOutputStream.MaxSInt7) {
                    sos.writeUI8(((cx << 4) | Edge_C7));
                    sos.writeUI8((((cx >> 4) & m3) | (cy << 3)));
                    sos.writeUI8((((cy >> 5) & m2) | (ax << 2)));
                    sos.writeUI8((((ax >> 6) & m1) | (ay << 1)));
                    return;
                }
                if (minV >= GFxOutputStream.MinSInt9 && maxV <= GFxOutputStream.MaxSInt9) {
                    sos.writeUI8(((cx << 4) | Edge_C9));
                    sos.writeUI8((((cx >> 4) & m5) | (cy << 5)));
                    sos.writeUI8((((cy >> 3) & m6) | (ax << 6)));
                    sos.writeUI8((((ax >> 2) & m7) | (ay << 7)));
                    sos.writeUI8(((ay >> 1)));
                    return;
                }
                if (minV >= GFxOutputStream.MinSInt11 && maxV <= GFxOutputStream.MaxSInt11) {
                    sos.writeUI8(((cx << 4) | Edge_C11));
                    sos.writeUI8((((cx >> 4) & m7) | (cy << 7)));
                    sos.writeUI8(((cy >> 1)));
                    sos.writeUI8((((cy >> 9) & m2) | (ax << 2)));
                    sos.writeUI8((((ax >> 6) & m5) | (ay << 5)));
                    sos.writeUI8(((ay >> 3)));
                    return;
                }
                if (minV >= GFxOutputStream.MinSInt13 && maxV <= GFxOutputStream.MaxSInt13) {
                    sos.writeUI8(((cx << 4) | Edge_C13));
                    sos.writeUI8(((cx >> 4)));
                    sos.writeUI8((((cx >> 12) & m1) | (cy << 1)));
                    sos.writeUI8((((cy >> 7) & m6) | (ax << 6)));
                    sos.writeUI8(((ax >> 2)));
                    sos.writeUI8((((ax >> 10) & m3) | (ay << 3)));
                    sos.writeUI8(((ay >> 5)));
                    return;
                }
                if (minV >= GFxOutputStream.MinSInt15 && maxV <= GFxOutputStream.MaxSInt15) {
                    sos.writeUI8(((cx << 4) | Edge_C15));
                    sos.writeUI8(((cx >> 4)));
                    sos.writeUI8((((cx >> 12) & m3) | (cy << 3)));
                    sos.writeUI8(((cy >> 5)));
                    sos.writeUI8((((cy >> 13) & m2) | (ax << 2)));
                    sos.writeUI8(((ax >> 6)));
                    sos.writeUI8((((ax >> 14) & m1) | (ay << 1)));
                    sos.writeUI8(((ay >> 7)));
                    return;
                }
                if (minV >= GFxOutputStream.MinSInt17 && maxV <= GFxOutputStream.MaxSInt17) {
                    sos.writeUI8(((cx << 4) | Edge_C17));
                    sos.writeUI8(((cx >> 4)));
                    sos.writeUI8((((cx >> 12) & m5) | (cy << 5)));
                    sos.writeUI8(((cy >> 3)));
                    sos.writeUI8((((cy >> 11) & m6) | (ax << 6)));
                    sos.writeUI8(((ax >> 2)));
                    sos.writeUI8((((ax >> 10) & m7) | (ay << 7)));
                    sos.writeUI8(((ay >> 1)));
                    sos.writeUI8(((ay >> 9)));
                    return;
                }
                sos.writeUI8(((cx << 4) | Edge_C19));
                sos.writeUI8(((cx >> 4)));
                sos.writeUI8((((cx >> 12) & m7) | (cy << 7)));
                sos.writeUI8(((cy >> 1)));
                sos.writeUI8(((cy >> 9)));
                sos.writeUI8((((cy >> 17) & m2) | (ax << 2)));
                sos.writeUI8(((ax >> 6)));
                sos.writeUI8((((ax >> 14) & m5) | (ay << 5)));
                sos.writeUI8(((ay >> 3)));
                sos.writeUI8(((ay >> 11)));
        }
    }
}
