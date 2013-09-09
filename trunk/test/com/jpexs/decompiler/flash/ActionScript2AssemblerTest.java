package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.ParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.Tag;
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
public class ActionScript2AssemblerTest {

    private SWF swf;

    @BeforeClass
    public void init() throws IOException {
        Configuration.setConfig("autoDeobfuscate", false);
        swf = new SWF(new FileInputStream("testdata/as2/as2.swf"), false);
    }

    private DoActionTag getFirstActionTag() {
        for (Tag t : swf.tags) {
            if (t instanceof DoActionTag) {
                return (DoActionTag) t;
            }
        }
        return null;
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
            String actualResult = Action.actionsToSource(doa.getActions(swf.version), swf.version, "", false);
            String decompiled = doa.getASMSource(swf.version, false, false, null);

            assertEquals(actualResult.trim(), "ok = false;");
            assertTrue(decompiled.contains("Push \"ok\" false"));
        } catch (IOException ex) {
            fail();
        } catch (ParseException ex) {
            fail();
        }
    }
}
