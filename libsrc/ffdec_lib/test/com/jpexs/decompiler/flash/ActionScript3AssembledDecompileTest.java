package com.jpexs.decompiler.flash;

import java.io.IOException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript3AssembledDecompileTest extends ActionScript3DecompileTestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("assembled", "testdata/as3_assembled/bin/as3_assembled.swf");
    }

    @Test
    public void testDoubleDup() {
        decompileMethod("assembled", "testDoubleDup", "var _loc10_:Rectangle = myprop(_loc5_);\r\n"
                + "_loc10_.mymethod(-_loc10_.width,-_loc10_.height);\r\n",
                false);
    }

    @Test
    public void testDup() {
        decompileMethod("assembled", "testDup", "return 1 - (var _loc1_:Number = 1 - _loc1_ / _loc4_) * _loc1_;\r\n",
                false);
    }

    @Test
    public void testDupAssignment() {
        decompileMethod("assembled", "testDupAssignment", "var _loc1_:int = 0;\r\n"
                + "var _loc2_:int = 10;\r\n"
                + "if(_loc1_ = _loc2_)\r\n"
                + "{\r\n"
                + "trace(_loc2_);\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testForEach() {
        decompileMethod("assembled", "testForEach", "var _loc5_:* = undefined;\r\n"
                + "var _loc2_:* = 0;\r\n"
                + "var _loc3_:int = 0;\r\n"
                + "for each(var _loc4_ in _loc5_)\r\n"
                + "{\r\n"
                + "if(_loc4_ != null)\r\n"
                + "{\r\n"
                + "_loc2_ = _loc4_;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "_loc3_ = 0;\r\n",
                false);
    }

    @Test
    public void testForEachCoerced() {
        decompileMethod("assembled", "testForEachCoerced", "for each(var _loc6_ in someprop)\r\n"
                + "{\r\n"
                + "_loc6_.methodname(_loc1_,_loc2_,_loc5_);\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testIncrement() {
        decompileMethod("assembled", "testIncrement", "super();\r\n"
                + "b = a++;\r\n",
                false);
    }

    @Test
    public void testIncrement2() {
        decompileMethod("assembled", "testIncrement2", "if(++loadCount == 2)\r\n"
                + "{\r\n"
                + "somemethod();\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testIncrement3() {
        decompileMethod("assembled", "testIncrement3", "_loc1_.length--;\r\n",
                false);
    }

    @Test
    public void testSetSlotDup() {
        decompileMethod("assembled", "testSetSlotDup", "var _loc5_:int = 5;\r\n"
                + "myname.somemethod(\"okay\",myslot = _loc5_);\r\n"
                + "myname.start();\r\n",
                false);
    }

    @Test
    public void testSetSlotFindProperty() {
        decompileMethod("assembled", "testSetSlotFindProperty", "return var myprop:int = 50;\r\n",
                false);
    }

    @Test
    public void testSwitch() {
        decompileMethod("assembled", "testSwitch", "switch(int(somevar))\r\n"
                + "{\r\n"
                + "case 0:\r\n"
                + "var _loc2_:String = \"X\";\r\n"
                + "return;\r\n"
                + "break;\r\n"
                + "case 1:\r\n"
                + "_loc2_ = \"A\";\r\n"
                + "break;\r\n"
                + "case 3:\r\n"
                + "_loc2_ = \"B\";\r\n"
                + "break;\r\n"
                + "case 4:\r\n"
                + "_loc2_ = \"C\";\r\n"
                + "}\r\n"
                + "_loc2_ = \"after\";\r\n",
                false);
    }

    @Test
    public void testSwitchDefault() {
        decompileMethod("assembled", "testSwitchDefault", "switch(5)\r\n"
                + "{\r\n"
                + "case 6:\r\n"
                + "var _loc2_:int = 6;\r\n"
                + "case 0:\r\n"
                + "_loc2_ = 0;\r\n"
                + "break;\r\n"
                + "case 1:\r\n"
                + "_loc2_ = 1;\r\n"
                + "case 5:\r\n"
                + "_loc2_ = 5;\r\n"
                + "break;\r\n"
                + "case 3:\r\n"
                + "_loc2_ = 3;\r\n"
                + "break;\r\n"
                + "default:\r\n"
                + "_loc2_ = 100;\r\n"
                + "}\r\n",
                false);
    }
}
