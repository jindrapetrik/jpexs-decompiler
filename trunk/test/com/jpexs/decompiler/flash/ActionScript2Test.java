/*
 * Copyright (C) 2010-2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript2Test extends ActionStript2TestBase {

    private final int frameIdxDiff = 23;
    
    @BeforeClass
    public void init() throws IOException, InterruptedException {
        Configuration.autoDeobfuscate.set(false);
        Configuration.decompile.set(true);
        Configuration.registerNameFormat.set("_loc%d_");
        swf = new SWF(new BufferedInputStream(new FileInputStream("testdata/as2/as2.swf")), false);
    }

    private void compareSrc(int frame, String expectedResult) {
        DoActionTag doa = getFrameSource(frame);
        assertNotNull(doa);
        HilightedTextWriter writer = new HilightedTextWriter(new CodeFormatting(), false);
        try {
            Action.actionsToSource(doa, doa.getActions(), "", writer);
        } catch (InterruptedException ex) {
            fail();
        }
        String actualResult = writer.toString().replaceAll("[ \r\n]", "");
        expectedResult = expectedResult.replaceAll("[ \r\n]", "");
        assertEquals(actualResult, expectedResult);

    }

    private DoActionTag getFrameSource(int frame) {
        int f = 0;
        DoActionTag lastDoa = null;

        for (Tag t : swf.tags) {
            if (t instanceof DoActionTag) {
                lastDoa = (DoActionTag) t;
            }
            if (t instanceof ShowFrameTag) {
                f++;
                if (f == frame) {
                    return lastDoa;
                }
                lastDoa = null;
            }
        }
        return null;
    }

    @Test
    public void frame24_unicodeTest() {
        compareSrc(frameIdxDiff + 1, "trace(\"unicodeTest\");\r\n"
                + "var k = \"היפופוטמי, או א\";\r\n"
                + "trace(k);\r\n"
        );
    }

    @Test
    public void frame2_ifWithElseTest() {
        compareSrc(frameIdxDiff + 2, "trace(\"ifWithElseTest\");\r\n"
                + "var i = 5;\r\n"
                + "if(i == 258)\r\n"
                + "{\r\n"
                + "trace(\"onTrue\");\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "trace(\"onFalse\" + i);\r\n"
                + "}\r\n"
        );
    }

    @Test
    public void frame3_forTest() {
        compareSrc(frameIdxDiff + 3, "trace(\"forTest\");\r\n"
                + "var i = 0;\r\n"
                + "while(i < 10)\r\n"
                + "{\r\n"
                + "trace(\"hello:\" + i);\r\n"
                + "i++;\r\n"
                + "}\r\n"
        );
    }

    @Test
    public void frame4_whileTest() {
        compareSrc(frameIdxDiff + 4, "trace(\"whileTest\");\r\n"
                + "var i = 0;\r\n"
                + "while(i < 10)\r\n"
                + "{\r\n"
                + "trace(\"hello:\" + i);\r\n"
                + "i++;\r\n"
                + "}\r\n"
        );
    }

    @Test
    public void frame5_forWithContinueTest() {
        compareSrc(frameIdxDiff + 5, "trace(\"forWithContinueTest\");\r\n"
                + "var i = 0;\r\n"
                + "for(;i < 10;i++)\r\n"
                + "{\r\n"
                + "trace(\"hello:\" + i);\r\n"
                + "if(i == 5)\r\n"
                + "{\r\n"
                + "trace(\"i==5\");\r\n"
                + "if(i == 7)\r\n"
                + "{\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "trace(\"hawk\");\r\n"
                + "}\r\n"
                + "trace(\"end of the loop\");\r\n"
                + "}\r\n"
        );
    }

    @Test
    public void frame6_doWhileTest() {
        compareSrc(frameIdxDiff + 6, "trace(\"doWhileTest\");\r\n"
                + "var i = 0;\r\n"
                + "do\r\n"
                + "{\r\n"
                + "trace(\"i=\" + i);\r\n"
                + "i++;\r\n"
                + "}\r\n"
                + "while(i < 10);\r\n"
                + "trace(\"end\");\r\n"
        );
    }

    @Test
    public void frame7_switchTest() {
        compareSrc(frameIdxDiff + 7, "trace(\"switchTest\");\r\n"
                + "var i = 5;\r\n"
                + "switch(i)\r\n"
                + "{\r\n"
                + "case 0:\r\n"
                + "case 1:\r\n"
                + "trace(\"one\");\r\n"
                + "break;\r\n"
                + "case 2:\r\n"
                + "trace(\"two\");\r\n"
                + "case 3:\r\n"
                + "trace(\"three\");\r\n"
                + "break;\r\n"
                + "case 4:\r\n"
                + "trace(\"four\");\r\n"
                + "break;\r\n"
                + "default:\r\n"
                + "trace(\"default clause\");\r\n"
                + "}\r\n"
                + "trace(\"scriptend\");\r\n"
        );
    }

    @Test
    public void frame8_strictEqualsTest() {
        compareSrc(frameIdxDiff + 8, "trace(\"strictEqualsTest\");\r\n"
                + "var i = 5;\r\n"
                + "if(i === 5)\r\n"
                + "{\r\n"
                + "trace(\"equals strict\");\r\n"
                + "}\r\n"
                + "if(!(i === 5))\r\n"
                + "{\r\n"
                + "trace(\"not equals strict\");\r\n"
                + "}\r\n"
        );
    }

    @Test
    public void frame9_switchForTest() {
        compareSrc(frameIdxDiff + 9, "trace(\"switchForTest\");\r\n"
                + "var i = 0;\r\n"
                + "for(;i < 10;i++)\r\n"
                + "{\r\n"
                + "switch(i)\r\n"
                + "{\r\n"
                + "case 0:\r\n"
                + "trace(\"zero\");\r\n"
                + "continue;\r\n"
                + "case 5:\r\n"
                + "trace(\"five\");\r\n"
                + "break;\r\n"
                + "case 10:\r\n"
                + "trace(\"ten\");\r\n"
                + "break;\r\n"
                + "case 1:\r\n"
                + "if(i == 7)\r\n"
                + "{\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "trace(\"one\");\r\n"
                + "default:\r\n"
                + "trace(\"def\");\r\n"
                + "}\r\n"
                + "trace(\"before loop end\");\r\n"
                + "}\r\n"
        );
    }

    @Test
    public void frame10_functionTest() {
        compareSrc(frameIdxDiff + 10, "function hello(what, second)\r\n"
                + "{\r\n"
                + "trace(\"hello \" + what + \"! \" + second);\r\n"
                + "}\r\n"
                + "trace(\"functionTest\");\r\n"
                + "hello(\"friend\",7);\r\n"
        );
    }

    @Test
    public void frame11_multipleConditionsTest() {
        compareSrc(frameIdxDiff + 11, "trace(\"multipleConditionsTest\");\r\n"
                + "var k = 5;\r\n"
                + "if(k == 7 && k == 8)\r\n"
                + "{\r\n"
                + "trace(\"first\");\r\n"
                + "}\r\n"
                + "if(k == 9)\r\n"
                + "{\r\n"
                + "trace(\"second\");\r\n"
                + "}\r\n"
                + "trace(\"finish\");\r\n"
        );
    }

    @Test
    public void frame12_multipleConditions2Test() {
        compareSrc(frameIdxDiff + 12, "trace(\"multipleConditions2Test\");\r\n"
                + "var k = 5;\r\n"
                + "if(k == 7 && k == 8)\r\n"
                + "{\r\n"
                + "trace(\"first\");\r\n"
                + "}\r\n"
                + "if(k == 9 || k == 6)\r\n"
                + "{\r\n"
                + "trace(\"second\");\r\n"
                + "}\r\n"
                + "trace(\"finish\");\r\n"
        );
    }

    @Test
    public void frame13_chainedAssignmentsTest() {
        compareSrc(frameIdxDiff + 13, "trace(\"chainedAssignmentsTest\");\r\n"
                + "var a = 7;\r\n"
                + "var b = 8;\r\n"
                + "var c = 9;\r\n"
                + "var d = c = b = a = 10;\r\n"
                + "trace(d);\r\n"
        );
    }

    @Test
    public void frame14_objectsTest() {
        compareSrc(frameIdxDiff + 14, "trace(\"objectsTest\");\r\n"
                + "var flashBox = new Box(box1);\r\n"
                + "_root.onEnterFrame = function()\r\n"
                + "{\r\n"
                + "flashBox.moveUp();\r\n"
                + "};\r\n"
                + "var ship = new Ship(200);\r\n"
                + "var enemy = new Enemy(56);\r\n"
                + "ship.moveDown(0.5);\r\n"
                + "ship.moveUp(0.2);\r\n"
                + "enemy.moveRight(230);\r\n"
                + "enemy.moveLeft(100);\r\n"
                + "var mt = new com.jpexs.MyTest();\r\n"
                + "mt.test();\r\n"
                + "var c = new Cox(box1);\r\n"
        );
    }

    @Test
    public void frame15_doWhile2Test() {
        compareSrc(frameIdxDiff + 15, "trace(\"doWhile2Test\");\r\n"
                + "var k = 5;\r\n"
                + "do\r\n"
                + "{\r\n"
                + "k++;\r\n"
                + "if(k == 7)\r\n"
                + "{\r\n"
                + "k = 5 * k;\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "k = 5 + k;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "while(k < 9);\r\n"
        );
    }

    @Test
    public void frame16_whileAndTest() {
        compareSrc(frameIdxDiff + 16, "trace(\"whileAndTest\");\r\n"
                + "var a = 5;\r\n"
                + "var b = 10;\r\n"
                + "while(a < 10 && b > 1)\r\n"
                + "{\r\n"
                + "a++;\r\n"
                + "b--;\r\n"
                + "}\r\n"
                + "a = 7;\r\n"
                + "b = 9;\r\n"
        );
    }

    @Test
    public void frame17_forInTest() {
        compareSrc(frameIdxDiff + 17, "function testForIn()\r\n"
                + "{\r\n"
                + "var _loc1_ = [];\r\n"
                + "for(var _loc2_ in _loc1_)\r\n"
                + "{\r\n"
                + "if(_loc2_ > 3)\r\n"
                + "{\r\n"
                + "if(_loc2_ == 5)\r\n"
                + "{\r\n"
                + "return 7;\r\n"
                + "}\r\n"
                + "return 8;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"forInTest\");\r\n"
                + "trace(testForIn());\r\n"
                + "var arr = [];\r\n"
                + "for(var a in arr)\r\n"
                + "{\r\n"
                + "trace(a);\r\n"
                + "}\r\n"
        );
    }

    @Test
    public void frame18_tryTest() {
        compareSrc(frameIdxDiff + 18, "trace(\"tryTest\");\r\n"
                + "var k = 5;\r\n"
                + "try\r\n"
                + "{\r\n"
                + "k = Infinity;\r\n"
                + "}\r\n"
                + "catch(e)\r\n"
                + "{\r\n"
                + "trace(\"bug \" + e);\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "trace(\"huu\");\r\n"
                + "}\r\n"
                + "trace(\"next\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "k = 6;\r\n"
                + "}\r\n"
                + "catch(e)\r\n"
                + "{\r\n"
                + "trace(\"bug2 \" + e);\r\n"
                + "}\r\n"
                + "trace(\"next2\");\r\n"
                + "var k = 5;\r\n"
                + "try\r\n"
                + "{\r\n"
                + "k = Infinity;\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "trace(\"final\");\r\n"
                + "}\r\n"
                + "trace(\"end\");\r\n"
        );
    }

    @Test
    public void frame19_indicesTest() {
        compareSrc(frameIdxDiff + 19, "trace(\"indicesTest\");\r\n"
                + "var k = [1,2,3];\r\n"
                + "var b = k[1];\r\n"
                + "trace(b);\r\n"
        );
    }

    @Test
    public void frame20_incDecTest() {
        compareSrc(frameIdxDiff + 20, "function tst()\r\n"
                + "{\r\n"
                + "return 1;\r\n"
                + "}\r\n"
                + "trace(\"incDecTest\");\r\n"
                + "var i = 5;\r\n"
                + "var b = i++;\r\n"
                + "var c = --i + 5;\r\n"
                + "trace(\"a:\" + a + \" b:\" + b + \" c:\" + c);\r\n"
                + "var arr = [1,2,3];\r\n"
                + "arr[tst()]++;\r\n"
                + "var d = arr[tst()];\r\n"
                + "trace(d);\r\n"
        );
    }

    @Test
    public void frame21_chainedAssignments2Test() {
        compareSrc(frameIdxDiff + 21, "trace(\"chainedAssignments2Test\");\r\n"
                + "var a = 5;\r\n"
                + "var b = 6;\r\n"
                + "var c = 7;\r\n"
                + "var d = c = b = a = 4;\r\n"
                + "if((d = c = b = a = 7) > 2)\r\n"
                + "{\r\n"
                + "trace(d);\r\n"
                + "}\r\n"
                + "trace(d + 1);\r\n"
                + "var i = 0;\r\n"
                + "while(i < 5)\r\n"
                + "{\r\n"
                + "if(i == 7)\r\n"
                + "{\r\n"
                + "}\r\n"
                + "i++;\r\n"
                + "}\r\n"
        );
    }

    @Test
    public void frame22_function2Test() {
        compareSrc(frameIdxDiff + 22, "function a()\r\n"
                + "{\r\n"
                + "trace(\"hi\");\r\n"
                + "var _loc1_ = 5;\r\n"
                + "if(_loc1_ == 7)\r\n"
                + "{\r\n"
                + "return undefined;\r\n"
                + "}\r\n"
                + "_loc1_ = _loc1_ * 9;\r\n"
                + "trace(_loc1_);\r\n"
                + "}\r\n"
                + "trace(\"function2Test\");\r\n"
        );
    }

    @Test
    public void frame23_tryFunctionTest() {
        compareSrc(frameIdxDiff + 23, "function testtry()\r\n"
                + "{\r\n"
                + "var _loc1_ = 5;\r\n"
                + "try\r\n"
                + "{\r\n"
                + "if(_loc1_ == 3)\r\n"
                + "{\r\n"
                + "return undefined;\r\n"
                + "}\r\n"
                + "if(_loc1_ == 4)\r\n"
                + "{\r\n"
                + "throw new Error();\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "_loc1_ = 7;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "catch(e)\r\n"
                + "{\r\n"
                + "trace(\"error\");\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "trace(\"finally\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"tryFunctionTest\");\r\n"
        );
    }

    @Test
    public void frame24_ternarTest() {
        compareSrc(frameIdxDiff + 24, "trace(\"ternarTest\");\r\n"
                + "var a = 5;\r\n"
                + "var b = a != 4?3:2;\r\n"
                + "trace(b);\r\n"
        );
    }

    @Test
    public void frame25_forInInTest() {
        compareSrc(frameIdxDiff + 25, "function tst()\r\n"
                + "{\r\n"
                + "var _loc2_ = [];\r\n"
                + "_loc2_[0] = [];\r\n"
                + "for(var _loc3_ in _loc2_)\r\n"
                + "{\r\n"
                + "for(var _loc1_ in _loc3_)\r\n"
                + "{\r\n"
                + "if(_loc1_ == 5)\r\n"
                + "{\r\n"
                + "return 5;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "if(_loc3_ == 8)\r\n"
                + "{\r\n"
                + "return 3;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "return 8;\r\n"
                + "}\r\n"
                + "trace(\"forInInTest\");\r\n"
                + "tst();\r\n"
        );
    }

    @Test
    public void frame26_registersFuncTest() {
        compareSrc(frameIdxDiff + 26, "function tst(px)\r\n"
                + "{\r\n"
                + "var _loc1_ = 57;\r\n"
                + "_loc1_ = _loc1_ * 27;\r\n"
                + "}\r\n"
                + "trace(\"registersFuncTest\");\r\n"
                + "tst(5);\r\n"
                + "var s = String(5);\r\n"
        );
    }

    @Test
    public void frame27_ifFrameLoadedTest() {
        compareSrc(frameIdxDiff + 27, "trace(\"ifFrameLoadedTest\");\r\n"
                + "ifFrameLoaded(9)\r\n"
                + "{\r\n"
                + "trace(\"loaded\");\r\n"
                + "}\r\n"
        );
    }

    @Test
    public void frame28_function3Test() {
        compareSrc(frameIdxDiff + 28, "function tst()\r\n"
                + "{\r\n"
                + "var _loc1_ = 5;\r\n"
                + "c = _loc1_ = 8;\r\n"
                + "trace(\"hi\");\r\n"
                + "trace(_loc1_);\r\n"
                + "if((e = d = f = c = 9) > 5)\r\n"
                + "{\r\n"
                + "trace(\"dd\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"function3Test\");\r\n"
                + "var c = 7;\r\n"
                + "var d = 7;\r\n"
                + "var e = 8;\r\n"
                + "tst();\r\n"
        );
    }

    @Test
    public void frame29_commaOperatorTest() {
        compareSrc(frameIdxDiff + 29, "trace(\"commaOperatorTest\");\r\n"
                + "var a = 0;\r\n"
                + "var b = 0;\r\n"
                + "var c = 0;\r\n"
                + "while(true)\r\n"
                + "{\r\n"
                + "a++;\r\n"
                + "b = b + 2;\r\n"
                + "if(c < 10)\r\n"
                + "{\r\n"
                + "trace(c);\r\n"
                + "c++;\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "trace(\"konec\");\r\n"
        );
    }

    @Test
    public void frame30_commaOperator2Test() {
        compareSrc(frameIdxDiff + 30, "trace(\"commaOperator2Test\");\r\n"
                + "var k = 8;\r\n"
                + "do\r\n"
                + "{\r\n"
                + "if(k == 9)\r\n"
                + "{\r\n"
                + "trace(\"h\");\r\n"
                + "if(k == 9)\r\n"
                + "{\r\n"
                + "trace(\"f\");\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "trace(\"b\");\r\n"
                + "}\r\n"
                + "trace(\"gg\");\r\n"
                + "}\r\n"
                + "while(k++, k < 10);\r\n"
                + "trace(\"ss\");\r\n"
        );
    }

    @Test
    public void frame31_function4Test() {
        compareSrc(frameIdxDiff + 31, "function tst()\r\n"
                + "{\r\n"
                + "var _loc1_ = 5;\r\n"
                + "while(_loc1_ < 10)\r\n"
                + "{\r\n"
                + "if(_loc1_ == 5)\r\n"
                + "{\r\n"
                + "if(_loc1_ == 6)\r\n"
                + "{\r\n"
                + "return true;\r\n"
                + "}\r\n"
                + "_loc1_ = _loc1_ + 1;\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "return false;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"function4Test\");\r\n"
                + "tst();\r\n"
        );
    }

    @Test
    public void frame32_pushTest() {
        compareSrc(frameIdxDiff + 32, "trace(\"pushTest\");\r\n"
        );
    }

    @Test
    public void frame33_commaOperator3Test() {
        compareSrc(frameIdxDiff + 33, "trace(\"commaOperator3Test\");\r\n"
                + "var k = 1;\r\n"
                + "while(true)\r\n"
                + "{\r\n"
                + "k++;\r\n"
                + "if(k < 10)\r\n"
                + "{\r\n"
                + "k = k * 5;\r\n"
                + "trace(k);\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "trace(\"end\");\r\n"
        );
    }

    @Test
    public void frame34_commaOperator4Test() {
        compareSrc(frameIdxDiff + 34, "trace(\"commaOperator4Test\");\r\n"
                + "var k = 0;\r\n"
                + "do\r\n"
                + "{\r\n"
                + "trace(k);\r\n"
                + "if(k == 8)\r\n"
                + "{\r\n"
                + "trace(\"a\");\r\n"
                + "if(k == 9)\r\n"
                + "{\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "trace(\"d\");\r\n"
                + "trace(\"b\");\r\n"
                + "}\r\n"
                + "k++;\r\n"
                + "}\r\n"
                + "while(k = k + 5, k < 20);\r\n"
                + "trace(\"end\");\r\n"
        );
    }

    @Test
    public void frame35_globalFunctionsTest() {
        compareSrc(frameIdxDiff + 35, "function tst(p1)\r\n"
                + "{\r\n"
                + "trace(\"test\");\r\n"
                + "}\r\n"
                + "trace(\"globalFunctionsTest\");\r\n"
                + "var k = Array(1,2,3);\r\n"
                + "var a = 1;\r\n"
                + "var b = Boolean(a);\r\n"
                + "call(5);\r\n"
                + "var c = \"A\";\r\n"
                + "clearInterval(5);\r\n"
                + "clearTimeout(4);\r\n"
                + "var mc;\r\n"
                + "duplicateMovieClip(mc,\"copy\",16389);\r\n"
                + "a = escape(\"how\");\r\n"
                + "var f = a;\r\n"
                + "fscommand(\"alert(\\\"hi\\\");\");\r\n"
                + "a = mc._alpha;\r\n"
                + "a = getTimer();\r\n"
                + "getURL(\"http://localhost/\",\"wnd\",\"POST\");\r\n"
                + "a = getVersion();\r\n"
                + "gotoAndStop(5);\r\n"
                + "play();\r\n"
                + "gotoAndStop(8);\r\n"
                + "ifFrameLoaded(4)\r\n"
                + "{\r\n"
                + "trace(\"loaded\");\r\n"
                + "}\r\n"
                + "a = int(f);\r\n"
                + "a = isFinite(f);\r\n"
                + "a = isNaN(f);\r\n"
                + "a = length(f);\r\n"
                + "loadMovie(\"http://localhost/test.swf\",a,\"GET\");\r\n"
                + "loadMovieNum(\"http://localhost/test.swf\",5,\"GET\");\r\n"
                + "loadVariables(\"http://localhost/vars.txt\",a,\"GET\");\r\n"
                + "loadVariablesNum(\"http://localhost/vars.txt\",4,\"GET\");\r\n"
                + "a = mbchr(f);\r\n"
                + "a = mblength(f);\r\n"
                + "a = mbord(f);\r\n"
                + "a = mbsubstring(\"aaaa\",5,4);\r\n"
                + "MMExecute(\"destroyPC\");\r\n"
                + "nextFrame();\r\n"
                + "gotoAndStop(1);\r\n"
                + "a = Number(f);\r\n"
                + "a = Object(f);\r\n"
                + "a = ord(f);\r\n"
                + "a = parseFloat(f);\r\n"
                + "a = parseInt(f,16);\r\n"
                + "play();\r\n"
                + "prevFrame();\r\n"
                + "gotoAndStop(1);\r\n"
                + "print(mc,\"bframe\");\r\n"
                + "printAsBitmap(mc,\"bframe\");\r\n"
                + "printAsBitmapNum(5,\"bframe\");\r\n"
                + "printNum(4,\"bframe\");\r\n"
                + "a = random(10);\r\n"
                + "removeMovieClip(mc);\r\n"
                + "setInterval(tst,5,f);\r\n"
                + "mc._X = 25;\r\n"
                + "setTimeout(tst,5,f);\r\n"
                + "showRedrawRegions(false,0);\r\n"
                + "startDrag(mc,1,5,5,6,6);\r\n"
                + "stop();\r\n"
                + "stopAllSounds();\r\n"
                + "stopDrag();\r\n"
                + "a = String(f);\r\n"
                + "a = \"aa\";\r\n"
                + "targetPath(f);\r\n"
                + "tellTarget(mc)\r\n"
                + "{\r\n"
                + "trace(\"told\")\r\n"
                + "};\r\n"
                + "toggleHighQuality();\r\n"
                + "a = unescape(f);\r\n"
                + "unloadMovie(mc);\r\n"
                + "unloadMovieNum(4);\r\n"
                + "updateAfterEvent();\r\n"
        );
    }

}
