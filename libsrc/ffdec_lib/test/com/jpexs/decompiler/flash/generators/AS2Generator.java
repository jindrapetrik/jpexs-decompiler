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
package com.jpexs.decompiler.flash.generators;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 *
 * Generates stub for ActionScript2Test
 *
 * @author JPEXS
 */
public class AS2Generator {

    public static void main(String[] args) throws Exception {
        Configuration.autoDeobfuscate.set(false);
        SWF swf = new SWF(new BufferedInputStream(new FileInputStream("testdata/as2/as2.swf")), false);
        DoABC2Tag tag = null;
        DoActionTag doa = null;
        int frame = 0;
        StringBuilder s = new StringBuilder();
        for (Tag t : swf.getTags()) {
            if (t instanceof DoActionTag) {
                doa = (DoActionTag) t;
            }
            if (t instanceof ShowFrameTag) {
                frame++;
                if (doa == null) {
                    continue;
                }
                HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
                Action.actionsToSource(doa, doa.getActions(), "", writer);
                String src = writer.toString();
                if (src.trim().isEmpty()) {
                    doa = null;
                    continue;
                }

                String[] srcs = src.split("[\r\n]+");
                String testName = "frame" + frame + "_Test";
                String pref = "trace(\"";
                for (String p : srcs) {
                    if (p.trim().matches("trace\\(\"(.*)Test\"\\);")) {
                        testName = "frame" + frame + "_" + p.substring(pref.length(), p.length() - 3/* "); */);
                    }
                }

                s.append("@Test\r\npublic void ");
                s.append(testName);
                s.append("(){\r\ncompareSrc(");
                s.append(frame);
                s.append(",");

                for (int i = 0; i < srcs.length; i++) {
                    String ss = srcs[i];
                    s.append("\"");
                    s.append(ss.trim().replace("\\", "\\\\").replace("\"", "\\\""));
                    s.append("\\r\\n\"");
                    if (i < srcs.length - 1) {
                        s.append("+");
                    }
                    s.append("\r\n");
                }
                s.append(");");
                s.append("}");
                doa = null;
            }
            /*try (PrintWriter pw = new PrintWriter("as2_teststub.java")) {
             pw.println(s.toString());
             }*/
            try (FileOutputStream fos = new FileOutputStream("as2_teststub.java")) {
                fos.write(Utf8Helper.getBytes(s.toString()));
            }
        }
    }
}
