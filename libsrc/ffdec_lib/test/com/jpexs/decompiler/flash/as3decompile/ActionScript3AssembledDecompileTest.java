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
    public void testActivationProps() {
        decompileMethod("assembled", "testActivationProps", "var myvar2:int = 10;\r\n",
                 false);
    }

    @Test
    public void testAlwaysBreak() {
        decompileMethod("assembled", "testAlwaysBreak", "if(true)\r\n"
                + "{\r\n"
                + "var v:* = 5;\r\n"
                + "trace(\"a\");\r\n"
                + "if(v > 4)\r\n"
                + "{\r\n"
                + "trace(\"b\");\r\n"
                + "if(v > 10)\r\n"
                + "{\r\n"
                + "trace(\"c\");\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "trace(\"d\");\r\n"
                + "addr003e:\r\n"
                + "trace(\"e\");\r\n"
                + "}\r\n"
                + "§§goto(addr004e);\r\n"
                + "}\r\n"
                + "§§goto(addr003e);\r\n"
                + "}\r\n"
                + "addr004e:\r\n"
                + "trace(\"f\");\r\n",
                 false);
    }

    @Test
    public void testAlwaysBreak2() {
        decompileMethod("assembled", "testAlwaysBreak2", "var v:* = 5;\r\n"
                + "trace(\"a\");\r\n"
                + "if(v > 4)\r\n"
                + "{\r\n"
                + "trace(\"b\");\r\n"
                + "if(v > 10)\r\n"
                + "{\r\n"
                + "trace(\"c\");\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "trace(\"d\");\r\n"
                + "addr003e:\r\n"
                + "trace(\"e\");\r\n"
                + "}\r\n"
                + "trace(\"f\");\r\n"
                + "return;\r\n"
                + "}\r\n"
                + "§§goto(addr003e);\r\n",
                 false);
    }

    @Test
    public void testDeclareReg() {
        decompileMethod("assembled", "testDeclareReg", "var other:XML;\r\n"
                + "with(other)\r\n"
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
                + "_loc1_ = _loc2_;\r\n"
                + "if(_loc1_)\r\n"
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
    public void testGoto() {
        decompileMethod("assembled", "testGoto", "var v:* = 5;\r\n"
                + "if(v > 1)\r\n"
                + "{\r\n"
                + "trace(\"a\");\r\n"
                + "if(v > 2)\r\n"
                + "{\r\n"
                + "trace(\"goto\");\r\n"
                + "addr0052:\r\n"
                + "trace(\"f\");\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "trace(\"b\");\r\n"
                + "addr003d:\r\n"
                + "trace(\"d\");\r\n"
                + "if(v > 3)\r\n"
                + "{\r\n"
                + "trace(\"e\");\r\n"
                + "§§goto(addr0052);\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "trace(\"g\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"end\");\r\n"
                + "return;\r\n"
                + "}\r\n"
                + "trace(\"c\");\r\n"
                + "§§goto(addr003d);\r\n",
                 false);
    }

    @Test
    public void testGoto2() {
        decompileMethod("assembled", "testGoto2", "var v:* = 5;\r\n"
                + "if(v > 1)\r\n"
                + "{\r\n"
                + "trace(\"a\");\r\n"
                + "if(v > 2)\r\n"
                + "{\r\n"
                + "trace(\"goto\");\r\n"
                + "addr0062:\r\n"
                + "trace(\"g\");\r\n"
                + "addr0069:\r\n"
                + "trace(\"h\");\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "trace(\"b\");\r\n"
                + "addr003d:\r\n"
                + "trace(\"d\");\r\n"
                + "if(v > 3)\r\n"
                + "{\r\n"
                + "trace(\"e\");\r\n"
                + "if(b > 5)\r\n"
                + "{\r\n"
                + "trace(\"f\");\r\n"
                + "§§goto(addr0062);\r\n"
                + "}\r\n"
                + "§§goto(addr0069);\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "trace(\"i\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"end\");\r\n"
                + "return;\r\n"
                + "}\r\n"
                + "trace(\"c\");\r\n"
                + "§§goto(addr003d);\r\n",
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
    public void testLocalRegIf() {
        decompileMethod("assembled", "testLocalRegIf", "var _loc1_:int = 8;\r\n"
                + "if(_loc1_ > 5 && _loc1_ < 10)\r\n"
                + "{\r\n"
                + "trace(\"I\");\r\n"
                + "}\r\n",
                 false);
    }

    @Test
    public void testMutatingSwitch() {
        decompileMethod("assembled", "testMutatingSwitch", "switch(this.k)\r\n"
                + "{\r\n"
                + "case \"a\":\r\n"
                + "trace(\"A\");\r\n"
                + "return;\r\n"
                + "case \"b\":\r\n"
                + "trace(\"B\");\r\n"
                + "return;\r\n"
                + "case \"c\":\r\n"
                + "trace(\"C\");\r\n"
                + "return;\r\n"
                + "default:\r\n"
                + "return;\r\n"
                + "}\r\n",
                 false);
    }

    @Test
    public void testPushPlacement() {
        decompileMethod("assembled", "testPushPlacement", "var a:int = 1;\r\n"
                + "var b:* = 2;\r\n"
                + "§§push(a);\r\n"
                + "a += 1;\r\n"
                + "if(b >= 2)\r\n"
                + "{\r\n"
                + "b = §§pop() + 7;\r\n"
                + "trace(b);\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "§§pop();\r\n"
                + "}\r\n",
                 false);
    }

    @Test
    public void testPushWhile() {
        decompileMethod("assembled", "testPushWhile", "var _loc3_:int = 5;\r\n"
                + "§§push(obfuscated[\"xxx\"] = new (getDefinitionByName(\"flash.utils\"+\".\"+\"ByteArray\"))());\r\n"
                + "§§push(50);\r\n"
                + "while(§§dup(§§pop()))\r\n"
                + "{\r\n"
                + "§§dup(§§pop())[§§dup(§§dup(§§pop())).length] = 0x29 ^ 0x6F;\r\n"
                + "§§dup(§§pop())[§§dup(§§dup(§§pop())).length] = 9 ^ 0x54;\r\n"
                + "§§push(§§pop() - 1);\r\n"
                + "}\r\n"
                + "§§pop();\r\n"
                + "§§pop();\r\n",
                 false);
    }

    @Test
    public void testSetSlotDup() {
        decompileMethod("assembled", "testSetSlotDup", "var myslot:int;\r\n"
                + "var _loc5_:int = 5;\r\n"
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
    public void testSwapAssignment() {
        decompileMethod("assembled", "testSwapAssignment", "var _loc6_:Bitmap = MyFactory.createBitmap();\r\n"
                + "_loc6_.x = _loc6_.x + 5;\r\n"
                + "_loc6_.y = -10;\r\n",
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
    public void testSwitchGoto() {
        decompileMethod("assembled", "testSwitchGoto", "var i:int = 5;\r\n"
                + "var a:Boolean = true;\r\n"
                + "var b:Boolean = false;\r\n"
                + "switch(i)\r\n"
                + "{\r\n"
                + "case 0:\r\n"
                + "trace(\"case0\");\r\n"
                + "break;\r\n"
                + "case 1:\r\n"
                + "if(a)\r\n"
                + "{\r\n"
                + "if(!b)\r\n"
                + "{\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "trace(\"a\");\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "trace(\"b\");\r\n"
                + "}\r\n"
                + "trace(\"c\");\r\n"
                + "break;\r\n"
                + "case 2:\r\n"
                + "trace(\"case2\");\r\n"
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
                + "addr0121:\r\n"
                + "trace(\"F\");\r\n"
                + "break;\r\n"
                + "case 5:\r\n"
                + "trace(\"5\");\r\n"
                + "addr011a:\r\n"
                + "trace(\"E\");\r\n"
                + "§§goto(addr0121);\r\n"
                + "case 7:\r\n"
                + "trace(\"7\");\r\n"
                + "addr0113:\r\n"
                + "trace(\"D\");\r\n"
                + "§§goto(addr011a);\r\n"
                + "case 2:\r\n"
                + "trace(\"2\");\r\n"
                + "addr010c:\r\n"
                + "trace(\"C\");\r\n"
                + "§§goto(addr0113);\r\n"
                + "case 8:\r\n"
                + "trace(\"8\");\r\n"
                + "addr0105:\r\n"
                + "trace(\"B\");\r\n"
                + "§§goto(addr010c);\r\n"
                + "default:\r\n"
                + "trace(\"def\");\r\n"
                + "trace(\"A\");\r\n"
                + "§§goto(addr0105);\r\n"
                + "}\r\n"
                + "trace(\"G\");\r\n"
                + "return null;\r\n",
                 false);
    }

    @Test
    public void testSwitchMostCommon() {
        decompileMethod("assembled", "testSwitchMostCommon", "var _loc2_:int = 0;\r\n"
                + "var _loc4_:* = undefined;\r\n"
                + "if(something == null)\r\n"
                + "{\r\n"
                + "switch(param1.keyCode)\r\n"
                + "{\r\n"
                + "case 89:\r\n"
                + "_loc2_ = 7;\r\n"
                + "break;\r\n"
                + "case 112:\r\n"
                + "return;\r\n"
                + "}\r\n"
                + "switch(param1.charCode)\r\n"
                + "{\r\n"
                + "case 49:\r\n"
                + "return;\r\n"
                + "case 69:\r\n"
                + "return;\r\n"
                + "case 113:\r\n"
                + "_loc2_ = 1;\r\n"
                + "}\r\n"
                + "}\r\n",
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
        decompileMethod("assembled", "testTryDoWhile2", "var _loc5_:*;\r\n"
                + "trace(\"hello\");\r\n"
                + "_loc5_ = Math.random();\r\n"
                + "do\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"second\");\r\n"
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
    public void testTryWhile() {
        decompileMethod("assembled", "testTryWhile", "var a:String;\r\n"
                + "var b:String;\r\n"
                + "var c:String;\r\n"
                + "try\r\n"
                + "{\r\n"
                + "c = \"aa\";\r\n"
                + "while(c)\r\n"
                + "{\r\n"
                + "if(b)\r\n"
                + "{\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "c = c.Object;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "browserMode = false;\r\n"
                + "return;\r\n"
                + "}\r\n"
                + "trace(\"finish\");\r\n",
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

    @Test
    public void testXmlStar() {
        decompileMethod("assembled", "testXmlStar", "var _loc1_:XML = <a>\r\n"
                + "<b>xxx</b>\r\n"
                + "<b>yyy</b>\r\n"
                + "</a>;\r\n"
                + "var _loc2_:* = _loc1_.b.*;\r\n",
                 false);
    }
}
