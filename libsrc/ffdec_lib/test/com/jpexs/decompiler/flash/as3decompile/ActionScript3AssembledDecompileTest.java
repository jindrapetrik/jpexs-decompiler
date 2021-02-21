package com.jpexs.decompiler.flash.as3decompile;

import com.jpexs.decompiler.flash.ActionScript3DecompileTestBase;
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
    public void testDeclareReg() {
        decompileMethod("assembled", "testDeclareReg", "with(other)\r\n"
                + "{\r\n"
                + "trace(\"hey\");\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testDecrementPrecedence() {
        decompileMethod("assembled", "testDecrementPrecedence", "var _loc2_:int = 10;\r\n"
                + "var _loc1_:int = 5;\r\n"
                + "var _loc3_:* = _loc2_ & (1 << _loc1_) - 1;\r\n",
                false);
    }

    @Test
    public void testDeobfuscatorJumpsExceptionStart() {
        decompileMethod("assembled", "testDeobfuscatorJumpsExceptionStart", "try\r\n"
                + "{\r\n"
                + "while(this.rnd())\r\n"
                + "{\r\n"
                + "trace(\"loop1\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "}\r\n"
                + "try\r\n"
                + "{\r\n"
                + "while(this.rnd())\r\n"
                + "{\r\n"
                + "trace(\"loop2\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "}\r\n"
                + "return 5;\r\n",
                false);
    }

    @Test
    public void testDoubleDup() {
        decompileMethod("assembled", "testDoubleDup", "var _loc10_:Rectangle = myprop(_loc5_);\r\n"
                + "_loc10_.mymethod(-_loc10_.width,-_loc10_.height);\r\n",
                false);
    }

    @Test
    public void testDup() {
        decompileMethod("assembled", "testDup", "var _loc1_:Number;\r\n"
                + "return 1 - (_loc1_ = 1 - _loc1_ / _loc4_) * _loc1_;\r\n",
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
                + "var myslot:int;\r\n"
                + "myname.somemethod(\"okay\",myslot = _loc5_);\r\n"
                + "myname.start();\r\n",
                false);
    }

    @Test
    public void testSetSlotFindProperty() {
        decompileMethod("assembled", "testSetSlotFindProperty", "var myprop:int;\r\n"
                + "return myprop = 50;\r\n",
                false);
    }

    @Test
    public void testSwitch() {
        decompileMethod("assembled", "testSwitch", "switch(int(somevar))\r\n"
                + "{\r\n"
                + "case 0:\r\n"
                + "var _loc2_:String = \"X\";\r\n"
                + "return;\r\n"
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

    @Test
    public void testSwitchJoin() {
        decompileMethod("assembled", "testSwitchJoin", "trace(\"before\");\r\n"
                + "var _loc2_:int = 57;\r\n"
                + "switch(_loc2_)\r\n"
                + "{\r\n"
                + "case 0:\r\n"
                + "case 3:\r\n"
                + "trace(\"0-3\");\r\n"
                + "break;\r\n"
                + "case 1:\r\n"
                + "case 6:\r\n"
                + "trace(\"1-6\");\r\n"
                + "addr106:\r\n"
                + "trace(\"F\");\r\n"
                + "break;\r\n"
                + "case 5:\r\n"
                + "trace(\"5\");\r\n"
                + "addr103:\r\n"
                + "trace(\"E\");\r\n"
                + "§§goto(addr106);\r\n"
                + "case 7:\r\n"
                + "trace(\"7\");\r\n"
                + "addr100:\r\n"
                + "trace(\"D\");\r\n"
                + "§§goto(addr103);\r\n"
                + "case 2:\r\n"
                + "trace(\"2\");\r\n"
                + "addr97:\r\n"
                + "trace(\"C\");\r\n"
                + "§§goto(addr100);\r\n"
                + "case 8:\r\n"
                + "trace(\"8\");\r\n"
                + "addr94:\r\n"
                + "trace(\"B\");\r\n"
                + "§§goto(addr97);\r\n"
                + "default:\r\n"
                + "trace(\"def\");\r\n"
                + "trace(\"A\");\r\n"
                + "§§goto(addr94);\r\n"
                + "}\r\n"
                + "trace(\"G\");\r\n"
                + "return null;\r\n",
                false);
    }

    @Test
    public void testTryCatchLoopBreakLevel2() {
        decompileMethod("assembled", "testTryCatchLoopBreakLevel2", "var a:int = 0;\r\n"
                + "a = 0;\r\n"
                + "trace(\"before loop\");\r\n"
                + "loop0:\r\n"
                + "while(true)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch1\");\r\n"
                + "while(a <= 5)\r\n"
                + "{\r\n"
                + "if(a > 5)\r\n"
                + "{\r\n"
                + "break loop0;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"in catch1c\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test
    public void testTryDoWhile() {
        decompileMethod("assembled", "testTryDoWhile", "trace(\"first\");\r\n"
                + "var _loc5_:* = rnd();\r\n"
                + "try\r\n"
                + "{\r\n"
                + "do\r\n"
                + "{\r\n"
                + "trace(\"second\");\r\n"
                + "}\r\n"
                + "while(_loc5_ <= 100);\r\n"
                + "\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test
    public void testTryDoWhile2() {
        decompileMethod("assembled", "testTryDoWhile2", "trace(\"hello\");\r\n"
                + "var _loc5_:* = Math.random();\r\n"
                + "do\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "while(_loc5_ <= 100);\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test
    public void testUnnamedException() {
        decompileMethod("assembled", "testUnnamedException", "var _loc5_:int = 5;\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"hello\");\r\n"
                + "}\r\n"
                + "catch(_loc_e_:*)\r\n"
                + "{\r\n"
                + "return _loc5_;\r\n"
                + "}\r\n",
                false);
    }
}
