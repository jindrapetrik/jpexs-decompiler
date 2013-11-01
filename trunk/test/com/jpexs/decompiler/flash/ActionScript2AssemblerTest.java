package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.ParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.graph.ExportMode;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript2AssemblerTest extends ActionStript2TestBase {

    @BeforeClass
    public void init() throws IOException {
        Configuration.setConfig("autoDeobfuscate", false);
        swf = new SWF(new FileInputStream("testdata/as2/as2.swf"), false);
    }

    @Test
    public void testModifiedConstantPools() {
        String actionsString = "ConstantPool \"ok\"\n" +
            "Jump loc001f\n" +
            "loc000d:Push \"ok\" false\n" +
            "SetVariable\n" +
            "Jump loc002f\n" +
            "loc001f:ConstantPool \"wrong\"\n" +
            "Jump loc000d\n" +
            "loc002f:";
        try {
            List<Action> actions = ASMParser.parse(0, 0, true, actionsString, swf.version, false);

            DoActionTag doa = getFirstActionTag();
            doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HilightedTextWriter writer = new HilightedTextWriter(false);
            Action.actionsToSource(doa.getActions(swf.version), swf.version, "", writer);
            String actualResult = writer.toString();
            writer = new HilightedTextWriter(false);
            doa.getASMSource(swf.version, ExportMode.PCODE, writer, null);
            String decompiled = writer.toString();

            assertEquals(actualResult.trim(), "ok = false;");
            assertTrue(decompiled.contains("Push \"ok\" false"));
        } catch (IOException | ParseException ex) {
            fail();
        }
    }
}
