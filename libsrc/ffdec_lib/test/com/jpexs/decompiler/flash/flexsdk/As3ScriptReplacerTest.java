package com.jpexs.decompiler.flash.flexsdk;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.helpers.Helper;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.Test;

public class As3ScriptReplacerTest {

    //Commented out yet... there's no Flex SDK on build server
    //@Test
    public void testReplace() throws IOException, InterruptedException, Exception {
        MxmlcAs3ScriptReplacer replacer = new MxmlcAs3ScriptReplacer(Configuration.flexSdkLocation.get());
        SWF swf = new SWF(new BufferedInputStream(new FileInputStream("testdata/as3/as3.swf")), false);
        String replacement = "package classes\n"
                + "{\n"
                + "\n"
                + "	public dynamic class TestClass1\n"
                + "	{\n"
                + "		public var attrib:int = 5;\n"
                + "		public var sons:Array;\n"
                + "\n"
                + "		public function testHello()\n"
                + "		{\n"
                + "			trace(\"helloA\");\n"
                + "		}\n"
                + "\n"
                + "		public function method(i:int):int\n"
                + "		{\n"
                + "			trace(\"methodB\");\n"
                + "			return 7;\n"
                + "		}\n"
                + "	}\n"
                + "}";
        List<String> classNames = new ArrayList<>();
        classNames.add("classes.TestClass1");
        List<ScriptPack> packs = swf.getScriptPacksByClassNames(classNames);
        for (ScriptPack sp : packs) {
            replacer.replaceScript(sp, replacement);
            return;
        }
    }
}
