/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JPEG marker.
 * @author JPEXS
 */
public class JpegMarker {

    public static final int SOF0 = 0xC0; //Start of Frame 0
    public static final int SOF1 = 0xC1; //Start of Frame 1
    public static final int SOF2 = 0xC2; //Start of Frame 2
    public static final int SOF3 = 0xC3; //Start of Frame 3

    public static final int DHT = 0xC4; //Define Huffman Table

    public static final int SOF5 = 0xC5; //Start of Frame 5
    public static final int SOF6 = 0xC6; //Start of Frame 6
    public static final int SOF7 = 0xC7; //Start of Frame 7

    public static final int JPG = 0xC8; //JPEG Extensions

    public static final int SOF9 = 0xC9; //Start of Frame 9
    public static final int SOF10 = 0xCA; //Start of Frame 10
    public static final int SOF11 = 0xCB; //Start of Frame 11

    public static final int DAC = 0xCC; //Define Arithmetic Coding

    public static final int SOF13 = 0xCD; //Start of Frame 13
    public static final int SOF14 = 0xCE; //Start of Frame 14
    public static final int SOF15 = 0xCF; //Start of Frame 15

    public static final int RST0 = 0xD0; //Restart Marker 0
    public static final int RST1 = 0xD1; //Restart Marker 1
    public static final int RST2 = 0xD2; //Restart Marker 2
    public static final int RST3 = 0xD3; //Restart Marker 3
    public static final int RST4 = 0xD4; //Restart Marker 4
    public static final int RST5 = 0xD5; //Restart Marker 5
    public static final int RST6 = 0xD6; //Restart Marker 6
    public static final int RST7 = 0xD7; //Restart Marker 7

    public static final int SOI = 0xD8; //Start of Image
    public static final int EOI = 0xD9; //End of Image

    public static final int SOS = 0xDA; //Start of Scan
    public static final int DQT = 0xDB; //Define Quantization Table
    public static final int DNL = 0xDC; //Define Number of Lines
    public static final int DRI = 0xDD; //Define Restart Interval
    public static final int DHP = 0xDE; //Define Hierarchical Progression
    public static final int EXP = 0xDF; //Expand Reference Component

    public static final int APP0 = 0xE0; //Application Segment 0
    public static final int APP1 = 0xE1; //Application Segment 1
    public static final int APP2 = 0xE2; //Application Segment 2
    public static final int APP3 = 0xE3; //Application Segment 3
    public static final int APP4 = 0xE4; //Application Segment 4
    public static final int APP5 = 0xE5; //Application Segment 5
    public static final int APP6 = 0xE6; //Application Segment 6
    public static final int APP7 = 0xE7; //Application Segment 7
    public static final int APP8 = 0xE8; //Application Segment 8
    public static final int APP9 = 0xE9; //Application Segment 9
    public static final int APP10 = 0xEA; //Application Segment 10
    public static final int APP11 = 0xEB; //Application Segment 11
    public static final int APP12 = 0xEC; //Application Segment 12
    public static final int APP13 = 0xED; //Application Segment 13
    public static final int APP14 = 0xEE; //Application Segment 14
    public static final int APP15 = 0xEF; //Application Segment 15

    public static final int JPG0 = 0xF0; //JPEG Extension 0
    public static final int JPG1 = 0xF1; //JPEG Extension 1
    public static final int JPG2 = 0xF2; //JPEG Extension 2
    public static final int JPG3 = 0xF3; //JPEG Extension 3
    public static final int JPG4 = 0xF4; //JPEG Extension 4
    public static final int JPG5 = 0xF5; //JPEG Extension 5
    public static final int JPG6 = 0xF6; //JPEG Extension 6
    public static final int JPG7 = 0xF7; //JPEG Extension 7
    public static final int SOF48 = 0xF7; //JPEG-LS
    public static final int JPG8 = 0xF8; //JPEG Extension 8
    public static final int LSE = 0xF8; //JPEG Extension 8
    public static final int JPG9 = 0xF9; //JPEG Extension 9
    public static final int JPG10 = 0xFA; //JPEG Extension 10
    public static final int JPG11 = 0xFB; //JPEG Extension 11
    public static final int JPG12 = 0xFC; //JPEG Extension 12
    public static final int JPG13 = 0xFD; //JPEG Extension 13   
    public static final int COM = 0xFE; //Comment       

    public static boolean markerHasLength(int marker) {
        return marker != 0
                && marker != SOI
                && marker != EOI
                && marker != RST0
                && marker != RST1
                && marker != RST2
                && marker != RST3
                && marker != RST4
                && marker != RST5
                && marker != RST6
                && marker != RST7;
    }

    public static String markerToString(int marker) {
        for (Field field : JpegMarker.class.getDeclaredFields()) {
            try {
                if (field.getInt(null) == marker) {
                    return field.getName();
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(JpegMarker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return "0x" + Integer.toHexString(marker);
    }
}
