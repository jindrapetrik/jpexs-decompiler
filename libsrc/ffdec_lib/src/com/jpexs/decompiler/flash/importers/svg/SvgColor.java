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
package com.jpexs.decompiler.flash.importers.svg;

import java.awt.Color;

/**
 *
 * @author JPEXS
 */
class SvgColor extends SvgFill {

    public Color color;

    public SvgColor(int r, int g, int b, int opacity) {
        this(new Color(r, g, b, opacity));
    }

    public SvgColor(int r, int g, int b) {
        this(new Color(r, g, b));
    }

    public SvgColor(Color color) {
        this.color = color;
    }

    @Override
    public Color toColor() {
        return this.color;
    }

    public static SvgColor parse(String colorString) {
        if (colorString == null) {
            return null;
        }

        // named colors from: http://www.w3.org/TR/SVG/types.html#ColorKeywords
        switch (colorString) {
            case "aliceblue":
                return new SvgColor(240, 248, 255);
            case "antiquewhite":
                return new SvgColor(250, 235, 215);
            case "aqua":
                return new SvgColor(0, 255, 255);
            case "aquamarine":
                return new SvgColor(127, 255, 212);
            case "azure":
                return new SvgColor(240, 255, 255);
            case "beige":
                return new SvgColor(245, 245, 220);
            case "bisque":
                return new SvgColor(255, 228, 196);
            case "black":
                return new SvgColor(0, 0, 0);
            case "blanchedalmond":
                return new SvgColor(255, 235, 205);
            case "blue":
                return new SvgColor(0, 0, 255);
            case "blueviolet":
                return new SvgColor(138, 43, 226);
            case "brown":
                return new SvgColor(165, 42, 42);
            case "burlywood":
                return new SvgColor(222, 184, 135);
            case "cadetblue":
                return new SvgColor(95, 158, 160);
            case "chartreuse":
                return new SvgColor(127, 255, 0);
            case "chocolate":
                return new SvgColor(210, 105, 30);
            case "coral":
                return new SvgColor(255, 127, 80);
            case "cornflowerblue":
                return new SvgColor(100, 149, 237);
            case "cornsilk":
                return new SvgColor(255, 248, 220);
            case "crimson":
                return new SvgColor(220, 20, 60);
            case "cyan":
                return new SvgColor(0, 255, 255);
            case "darkblue":
                return new SvgColor(0, 0, 139);
            case "darkcyan":
                return new SvgColor(0, 139, 139);
            case "darkgoldenrod":
                return new SvgColor(184, 134, 11);
            case "darkgray":
                return new SvgColor(169, 169, 169);
            case "darkgreen":
                return new SvgColor(0, 100, 0);
            case "darkgrey":
                return new SvgColor(169, 169, 169);
            case "darkkhaki":
                return new SvgColor(189, 183, 107);
            case "darkmagenta":
                return new SvgColor(139, 0, 139);
            case "darkolivegreen":
                return new SvgColor(85, 107, 47);
            case "darkorange":
                return new SvgColor(255, 140, 0);
            case "darkorchid":
                return new SvgColor(153, 50, 204);
            case "darkred":
                return new SvgColor(139, 0, 0);
            case "darksalmon":
                return new SvgColor(233, 150, 122);
            case "darkseagreen":
                return new SvgColor(143, 188, 143);
            case "darkslateblue":
                return new SvgColor(72, 61, 139);
            case "darkslategray":
                return new SvgColor(47, 79, 79);
            case "darkslategrey":
                return new SvgColor(47, 79, 79);
            case "darkturquoise":
                return new SvgColor(0, 206, 209);
            case "darkviolet":
                return new SvgColor(148, 0, 211);
            case "deeppink":
                return new SvgColor(255, 20, 147);
            case "deepskyblue":
                return new SvgColor(0, 191, 255);
            case "dimgray":
                return new SvgColor(105, 105, 105);
            case "dimgrey":
                return new SvgColor(105, 105, 105);
            case "dodgerblue":
                return new SvgColor(30, 144, 255);
            case "firebrick":
                return new SvgColor(178, 34, 34);
            case "floralwhite":
                return new SvgColor(255, 250, 240);
            case "forestgreen":
                return new SvgColor(34, 139, 34);
            case "fuchsia":
                return new SvgColor(255, 0, 255);
            case "gainsboro":
                return new SvgColor(220, 220, 220);
            case "ghostwhite":
                return new SvgColor(248, 248, 255);
            case "gold":
                return new SvgColor(255, 215, 0);
            case "goldenrod":
                return new SvgColor(218, 165, 32);
            case "gray":
                return new SvgColor(128, 128, 128);
            case "grey":
                return new SvgColor(128, 128, 128);
            case "green":
                return new SvgColor(0, 128, 0);
            case "greenyellow":
                return new SvgColor(173, 255, 47);
            case "honeydew":
                return new SvgColor(240, 255, 240);
            case "hotpink":
                return new SvgColor(255, 105, 180);
            case "indianred":
                return new SvgColor(205, 92, 92);
            case "indigo":
                return new SvgColor(75, 0, 130);
            case "ivory":
                return new SvgColor(255, 255, 240);
            case "khaki":
                return new SvgColor(240, 230, 140);
            case "lavender":
                return new SvgColor(230, 230, 250);
            case "lavenderblush":
                return new SvgColor(255, 240, 245);
            case "lawngreen":
                return new SvgColor(124, 252, 0);
            case "lemonchiffon":
                return new SvgColor(255, 250, 205);
            case "lightblue":
                return new SvgColor(173, 216, 230);
            case "lightcoral":
                return new SvgColor(240, 128, 128);
            case "lightcyan":
                return new SvgColor(224, 255, 255);
            case "lightgoldenrodyellow":
                return new SvgColor(250, 250, 210);
            case "lightgray":
                return new SvgColor(211, 211, 211);
            case "lightgreen":
                return new SvgColor(144, 238, 144);
            case "lightgrey":
                return new SvgColor(211, 211, 211);
            case "lightpink":
                return new SvgColor(255, 182, 193);
            case "lightsalmon":
                return new SvgColor(255, 160, 122);
            case "lightseagreen":
                return new SvgColor(32, 178, 170);
            case "lightskyblue":
                return new SvgColor(135, 206, 250);
            case "lightslategray":
                return new SvgColor(119, 136, 153);
            case "lightslategrey":
                return new SvgColor(119, 136, 153);
            case "lightsteelblue":
                return new SvgColor(176, 196, 222);
            case "lightyellow":
                return new SvgColor(255, 255, 224);
            case "lime":
                return new SvgColor(0, 255, 0);
            case "limegreen":
                return new SvgColor(50, 205, 50);
            case "linen":
                return new SvgColor(250, 240, 230);
            case "magenta":
                return new SvgColor(255, 0, 255);
            case "maroon":
                return new SvgColor(128, 0, 0);
            case "mediumaquamarine":
                return new SvgColor(102, 205, 170);
            case "mediumblue":
                return new SvgColor(0, 0, 205);
            case "mediumorchid":
                return new SvgColor(186, 85, 211);
            case "mediumpurple":
                return new SvgColor(147, 112, 219);
            case "mediumseagreen":
                return new SvgColor(60, 179, 113);
            case "mediumslateblue":
                return new SvgColor(123, 104, 238);
            case "mediumspringgreen":
                return new SvgColor(0, 250, 154);
            case "mediumturquoise":
                return new SvgColor(72, 209, 204);
            case "mediumvioletred":
                return new SvgColor(199, 21, 133);
            case "midnightblue":
                return new SvgColor(25, 25, 112);
            case "mintcream":
                return new SvgColor(245, 255, 250);
            case "mistyrose":
                return new SvgColor(255, 228, 225);
            case "moccasin":
                return new SvgColor(255, 228, 181);
            case "navajowhite":
                return new SvgColor(255, 222, 173);
            case "navy":
                return new SvgColor(0, 0, 128);
            case "oldlace":
                return new SvgColor(253, 245, 230);
            case "olive":
                return new SvgColor(128, 128, 0);
            case "olivedrab":
                return new SvgColor(107, 142, 35);
            case "orange":
                return new SvgColor(255, 165, 0);
            case "orangered":
                return new SvgColor(255, 69, 0);
            case "orchid":
                return new SvgColor(218, 112, 214);
            case "palegoldenrod":
                return new SvgColor(238, 232, 170);
            case "palegreen":
                return new SvgColor(152, 251, 152);
            case "paleturquoise":
                return new SvgColor(175, 238, 238);
            case "palevioletred":
                return new SvgColor(219, 112, 147);
            case "papayawhip":
                return new SvgColor(255, 239, 213);
            case "peachpuff":
                return new SvgColor(255, 218, 185);
            case "peru":
                return new SvgColor(205, 133, 63);
            case "pink":
                return new SvgColor(255, 192, 203);
            case "plum":
                return new SvgColor(221, 160, 221);
            case "powderblue":
                return new SvgColor(176, 224, 230);
            case "purple":
                return new SvgColor(128, 0, 128);
            case "red":
                return new SvgColor(255, 0, 0);
            case "rosybrown":
                return new SvgColor(188, 143, 143);
            case "royalblue":
                return new SvgColor(65, 105, 225);
            case "saddlebrown":
                return new SvgColor(139, 69, 19);
            case "salmon":
                return new SvgColor(250, 128, 114);
            case "sandybrown":
                return new SvgColor(244, 164, 96);
            case "seagreen":
                return new SvgColor(46, 139, 87);
            case "seashell":
                return new SvgColor(255, 245, 238);
            case "sienna":
                return new SvgColor(160, 82, 45);
            case "silver":
                return new SvgColor(192, 192, 192);
            case "skyblue":
                return new SvgColor(135, 206, 235);
            case "slateblue":
                return new SvgColor(106, 90, 205);
            case "slategray":
                return new SvgColor(112, 128, 144);
            case "slategrey":
                return new SvgColor(112, 128, 144);
            case "snow":
                return new SvgColor(255, 250, 250);
            case "springgreen":
                return new SvgColor(0, 255, 127);
            case "steelblue":
                return new SvgColor(70, 130, 180);
            case "tan":
                return new SvgColor(210, 180, 140);
            case "teal":
                return new SvgColor(0, 128, 128);
            case "thistle":
                return new SvgColor(216, 191, 216);
            case "tomato":
                return new SvgColor(255, 99, 71);
            case "turquoise":
                return new SvgColor(64, 224, 208);
            case "violet":
                return new SvgColor(238, 130, 238);
            case "wheat":
                return new SvgColor(245, 222, 179);
            case "white":
                return new SvgColor(255, 255, 255);
            case "whitesmoke":
                return new SvgColor(245, 245, 245);
            case "yellow":
                return new SvgColor(255, 255, 0);
            case "yellowgreen":
                return new SvgColor(154, 205, 50);
        }

        if (colorString.startsWith("#")) {
            String s = colorString.substring(1);
            if (s.length() == 3) {
                s = "" + s.charAt(0) + s.charAt(0) + s.charAt(1) + s.charAt(1) + s.charAt(2) + s.charAt(2);
            }

            int i = Integer.parseInt(s, 16);
            return new SvgColor(new Color(i, false));
        } else if (colorString.startsWith("rgb")) {
            colorString = colorString.substring(3).trim();
            if (colorString.startsWith("(") && colorString.endsWith(")")) {
                colorString = colorString.substring(1, colorString.length() - 1);
                String[] args = colorString.split(",");
                if (args.length == 3) {
                    String a0 = args[0].trim();
                    String a1 = args[1].trim();
                    String a2 = args[2].trim();
                    if (a0.endsWith("%") && a1.endsWith("%") && a2.endsWith("%")) {
                        int r = (int) Math.round(Double.parseDouble(a0.substring(0, a0.length() - 1)) * 255.0 / 100);
                        int g = (int) Math.round(Double.parseDouble(a1.substring(0, a1.length() - 1)) * 255.0 / 100);
                        int b = (int) Math.round(Double.parseDouble(a2.substring(0, a2.length() - 1)) * 255.0 / 100);
                        return new SvgColor(r, g, b);
                    } else {
                        int r = Integer.parseInt(a0);
                        int g = Integer.parseInt(a1);
                        int b = Integer.parseInt(a2);
                        return new SvgColor(r, g, b);
                    }
                }
            }
        }

        return null;
    }
}
