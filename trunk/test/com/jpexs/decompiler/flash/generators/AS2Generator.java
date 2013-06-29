package com.jpexs.decompiler.flash.generators;

import com.jpexs.decompiler.flash.Configuration;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.helpers.Highlighting;
import com.jpexs.decompiler.flash.tags.DoABCDefineTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 *
 * Generates stub for ActionScript3Test
 *
 * @author JPEXS
 */
public class AS2Generator {

    public static void main(String[] args) throws Exception {
        Configuration.setConfig("autoDeobfuscate", false);
        SWF swf = new SWF(new FileInputStream("testdata/as2/as2.swf"), false);
        DoABCDefineTag tag = null;
        DoActionTag doa = null;
        int frame = 0;
        StringBuilder s = new StringBuilder();
        for (Tag t : swf.tags) {
            if (t instanceof DoActionTag) {
                doa = (DoActionTag) t;
            }
            if (t instanceof ShowFrameTag) {
                frame++;
                if (doa == null) {
                    continue;
                }
                String src = Highlighting.stripHilights(Action.actionsToSource(doa.getActions(swf.version), swf.version));
                if (src.trim().equals("")) {
                    doa = null;
                    continue;
                }
               

                String srcs[] = src.split("[\r\n]+");
                String testName="testFrame"+frame;
                if(srcs.length>0){
                    String pref="trace(\"";
                    if(srcs[0].startsWith(pref)){
                        testName="frame"+frame+"_"+srcs[0].substring(pref.length(),srcs[0].length()-3/* "); */);
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
                fos.write(s.toString().getBytes("UTF-8"));
            }
        }
    }
}
