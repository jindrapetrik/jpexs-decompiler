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
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import java.io.IOException;
import java.util.Arrays;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript3ClassTest extends ActionScript3DecompileTestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("standard", "testdata/as3_new/bin/as3_new.flex.swf");
        addSwf("assembled", "testdata/as3_assembled/bin/as3_assembled.swf");
        addSwf("getouterscope", "testdata/getouterscope/getouterscope.swf");
        addSwf("haxe", "testdata/haxe/output.swf");
    }

    private void decompileScriptPack(String swfId, String path, String expectedResult) {

        DoABC2Tag tag = null;
        ABC abc = null;
        ScriptPack scriptPack = null;
        SWF swf = getSwf(swfId);
        for (Tag t : swf.getTags()) {
            if (t instanceof DoABC2Tag) {
                tag = (DoABC2Tag) t;
                abc = tag.getABC();
                scriptPack = abc.findScriptPackByPath(path, Arrays.asList(abc));
                if (scriptPack != null) {
                    break;
                }
            }
        }
        assertNotNull(abc);
        assertNotNull(scriptPack);
        HighlightedTextWriter writer = null;
        try {
            writer = new HighlightedTextWriter(new CodeFormatting(), false);
            scriptPack.toSource(swf.getAbcIndex(), writer, abc.script_info.get(scriptPack.scriptIndex).traits.traits, new ConvertData(), ScriptExportMode.AS, false, false, false);
        } catch (InterruptedException ex) {
            fail();
        }
        writer.finishHilights();
        String actualResult = cleanPCode(writer.toString());
        expectedResult = cleanPCode(expectedResult);
        assertEquals(actualResult, expectedResult);
    }

    @Test
    public void testMyPackage1TestClass() {
        decompileScriptPack("standard", "tests_classes.mypackage1.TestClass", "package tests_classes.mypackage1\n"
                + "{\n"
                + "   import tests_classes.mypackage2.TestClass;\n"
                + "   import tests_classes.mypackage2.TestInterface;\n"
                + "   \n"
                + "   public class TestClass implements tests_classes.mypackage1.TestInterface\n"
                + "   {\n"
                + "       \n"
                + "      \n"
                + "      public function TestClass()\n"
                + "      {\n"
                + "         super();\n"
                + "      }\n"
                + "      \n"
                + "      public function testCall() : String\n"
                + "      {\n"
                + "         trace(\"pkg1hello\");\n"
                + "         return \"pkg1hello\";\n"
                + "      }\n"
                + "      \n"
                + "      public function testMethod1() : void\n"
                + "      {\n"
                + "         var a:tests_classes.mypackage1.TestInterface = this;\n"
                + "         a.testMethod1();\n"
                + "         var b:tests_classes.mypackage2.TestInterface = this;\n"
                + "         b = new tests_classes.mypackage2.TestClass();\n"
                + "      }\n"
                + "      \n"
                + "      public function testMethod2() : void\n"
                + "      {\n"
                + "         var a:tests_classes.mypackage1.TestInterface = this;\n"
                + "         a.testMethod1();\n"
                + "         var b:tests_classes.mypackage2.TestInterface = this;\n"
                + "         b = new tests_classes.mypackage2.TestClass();\n"
                + "      }\n"
                + "      \n"
                + "      public function testParam(p1:tests_classes.mypackage1.TestInterface, p2:tests_classes.mypackage2.TestInterface) : void\n"
                + "      {\n"
                + "         var m:Function = function(m1:tests_classes.mypackage1.TestInterface, m2:tests_classes.mypackage2.TestInterface):void\n"
                + "         {\n"
                + "            var v1:tests_classes.mypackage1.TestInterface = null;\n"
                + "            var v2:tests_classes.mypackage2.TestInterface = null;\n"
                + "         };\n"
                + "      }\n"
                + "   }\n"
                + "}");
    }

    @Test
    public void testMyPackage1TestClass2() {
        decompileScriptPack("standard", "tests_classes.mypackage1.TestClass2", "package tests_classes.mypackage1\n"
                + "{\n"
                + "   import tests_classes.mypackage2.TestClass;\n"
                + "   import tests_classes.mypackage3.TestClass;\n"
                + "   \n"
                + "   use namespace myNamespace;"
                + "   \n"
                + "   public class TestClass2\n"
                + "   {\n"
                + "       \n"
                + "      public function TestClass2()\n"
                + "      {\n"
                + "         super();\n"
                + "      }\n"
                + "      \n"
                + "      public function testCall() : String\n"
                + "      {\n"
                + "         var a:tests_classes.mypackage1.TestClass = null;\n"
                + "         var b:tests_classes.mypackage2.TestClass = null;\n"
                + "         var c:tests_classes.mypackage3.TestClass = null;\n"
                + "         a = new tests_classes.mypackage1.TestClass();\n"
                + "         b = new tests_classes.mypackage2.TestClass();\n"
                + "         c = new tests_classes.mypackage3.TestClass();\n"
                + "         var res:String = a.testCall() + b.testCall() + c.testCall() + this.testCall2() + myNamespace::testCall3();\n"
                + "         trace(res);\n"
                + "         return res;\n"
                + "      }\n"
                + "      \n"
                + "      myNamespace function testCall2() : String\n"
                + "      {\n"
                + "         return \"1\";\n"
                + "      }\n"
                + "      \n"
                + "      myNamespace function testCall3() : String\n"
                + "      {\n"
                + "         return myNamespace::testCall2();\n"
                + "      }\n"
                + "      \n"
                + "      public function testCall2() : String\n"
                + "      {\n"
                + "         return \"2\";\n"
                + "      }\n"
                + "   }\n"
                + "}");
    }

    @Test
    public void testMyPackage1TestInterface() {
        decompileScriptPack("standard", "tests_classes.mypackage1.TestInterface", "package tests_classes.mypackage1\n"
                + "{\n"
                + "   import tests_classes.mypackage2.TestInterface;\n"
                + "   \n"
                + "   public interface TestInterface extends tests_classes.mypackage2.TestInterface\n"
                + "   {\n"
                + "       \n"
                + "      function testMethod1() : void;\n"
                + "   }\n"
                + "}");
    }

    @Test
    public void testMyPackage1MyNamespace() {
        decompileScriptPack("standard", "tests_classes.mypackage1.myNamespace", "package tests_classes.mypackage1\n"
                + "{\n"
                + "   public namespace myNamespace = \"https://www.free-decompiler.com/flash/test/namespace\";\n"
                + "}");
    }

    @Test
    public void testMyPackage2TestClass() {
        decompileScriptPack("standard", "tests_classes.mypackage2.TestClass", "package tests_classes.mypackage2\n"
                + "{\n"
                + "   public class TestClass implements TestInterface\n"
                + "   {\n"
                + "       \n"
                + "      public function TestClass()\n"
                + "      {\n"
                + "         super();\n"
                + "      }\n"
                + "      \n"
                + "      public function testCall() : String\n"
                + "      {\n"
                + "         trace(\"pkg2hello\");\n"
                + "         return \"pkg2hello\";\n"
                + "      }\n"
                + "      \n"
                + "      public function testMethod2() : void\n"
                + "      {\n"
                + "      }\n"
                + "   }\n"
                + "}");
    }

    @Test
    public void testMyPackage2TestInterface() {
        decompileScriptPack("standard", "tests_classes.mypackage2.TestInterface", "package tests_classes.mypackage2\n"
                + "{\n"
                + "   public interface TestInterface\n"
                + "   {\n"
                + "       \n"
                + "      function testMethod2() : void;\n"
                + "   }\n"
                + "}");
    }

    @Test
    public void testMyPackage3TestClass() {
        decompileScriptPack("standard", "tests_classes.mypackage3.TestClass", "package tests_classes.mypackage3\n"
                + "{\n"
                + "   public class TestClass\n"
                + "   {\n"
                + "       \n"
                + "      public function TestClass()\n"
                + "      {\n"
                + "         super();\n"
                + "      }\n"
                + "      \n"
                + "      public function testCall() : String\n"
                + "      {\n"
                + "         trace(\"pkg3hello\");\n"
                + "         return \"pkg3hello\";\n"
                + "      }\n"
                + "   }\n"
                + "}");
    }

    @Test
    public void testThisOutsideClass() {
        decompileScriptPack("standard", "tests_classes.TestThisOutsideClass", "package tests_classes\n"
                + "{\n"
                + "   public class TestThisOutsideClass\n"
                + "   {\n"
                + "       \n"
                + "      \n"
                + "      public var attrib:int = 0;\n"
                + "      \n"
                + "      public function TestThisOutsideClass()\n"
                + "      {\n"
                + "         super();\n"
                + "      }\n"
                + "      \n"
                + "      public function run() : void\n"
                + "      {\n"
                + "         helperFunc.call(this,\"hello\");\n"
                + "      }\n"
                + "   }\n"
                + "}\n"
                + "\n"
                + "function helperFunc(a:String):void\n"
                + "{\n"
                + "   trace(a);\n"
                + "   this.attrib++;\n"
                + "}");
    }

    @Test
    public void testSlots() {
        decompileScriptPack("assembled", "tests.TestSlots", "package tests\n"
                + "{\n"
                + "   public class TestSlots\n"
                + "   {\n"
                + "      \n"
                + "      public static var classVar1:String = \"cls1\";\n"
                + "      \n"
                + "      public static var classVar2:String = \"cls2\";\n"
                + "       \n"
                + "      \n"
                + "      public var instanceVar1:String = \"ins1\";\n"
                + "      \n"
                + "      public var instanceVar2:String = \"ins2\";\n"
                + "      \n"
                + "      public function TestSlots()\n"
                + "      {\n"
                + "         super();\n"
                + "      }\n"
                + "      \n"
                + "      public static function classMethod() : void\n"
                + "      {\n"
                + "         trace(classVar1);\n"
                + "         trace(classVar2);\n"
                + "         trace(globalVar1);\n"
                + "         trace(globalVar2);\n"
                + "      }\n"
                + "      \n"
                + "      public function instanceMethod() : void\n"
                + "      {\n"
                + "         trace(instanceVar1);\n"
                + "         trace(instanceVar2);\n"
                + "         trace(globalVar1);\n"
                + "         trace(globalVar2);\n"
                + "      }\n"
                + "   }\n"
                + "}\n"
                + "\n"
                + "function globalFunction():void\n"
                + "{\n"
                + "   trace(globalVar1);\n"
                + "   trace(globalVar2);\n"
                + "}\n"
                + "var globalVar1:String = \"glb1\";\n"
                + "\n"
                + "var globalVar2:String = \"glb2\";\n"
        );
    }

    @Test
    public void testImports() {
        decompileScriptPack("standard", "tests_classes.TestImports", "package tests_classes\n"
                + "{\n"
                + "   import tests_classes.myjson.JSON;\n"
                + "   import tests_classes.myjson2.JSON;\n"
                + "   \n"
                + "   public class TestImports\n"
                + "   {\n"
                + "       \n"
                + "      \n"
                + "      public function TestImports()\n"
                + "      {\n"
                + "         super();\n"
                + "      }\n"
                + "      \n"
                + "      public function run() : *\n"
                + "      {\n"
                + "         var j1:tests_classes.myjson.JSON = new tests_classes.myjson.JSON();\n"
                + "         var j2:tests_classes.myjson2.JSON = new tests_classes.myjson2.JSON();\n"
                + "         trace(j1);\n"
                + "         trace(j2);\n"
                + "      }\n"
                + "   }\n"
                + "}");
    }

    @Test
    public void testInitializer() {
        decompileScriptPack("standard", "tests_classes.TestInitializer", "package tests_classes\n"
                + "{\n"
                + "   public class TestInitializer\n"
                + "   {\n"
                + "      public static var s_alpha:RegExp = /[a-z]+/;\n"
                + "      \n"
                + "      public static var s_numbers:RegExp = /[0-9]+/;\n"
                + "      \n"
                + "      public static var s_regs:Array = [s_alpha,s_numbers];\n"
                + "      \n"
                + "      public var i_email:RegExp = /.*@.*\\..*/;\n"
                + "      \n"
                + "      public var i_link:RegExp = /<a href=\".*\">/;\n"
                + "      \n"
                + "      public var i_regs:Array = [this.i_email,this.i_link];\n"
                + "      \n"
                + "      public var i_a:int = 1;\n"
                + "      \n"
                + "      public var i_b:int = this.i_a + 1;\n"
                + "      \n"
                + "      public const i_c:int = this.i_a + this.i_b + 1;\n"
                + "      \n"
                + "      public var i_d:int;\n"
                + "      \n"
                + "      public function TestInitializer(p:int)\n"
                + "      {\n"
                + "         super();\n"
                + "         trace(s_regs[1]);\n"
                + "         this.i_a = 7;\n"
                + "         this.i_d = p;\n"
                + "      }\n"
                + "   }\n"
                + "}\n");
    }

    @Test
    public void testModifiers() {
        decompileScriptPack("standard", "tests_classes.TestModifiers", "package tests_classes\n"
                + "{\n"
                + "   import tests_other.myInternal;\n"
                + "   import tests_other.myInternal2;\n"
                + "   \n"
                + "   use namespace myInternal;\n"
                + "   use namespace myInternal2;\n"
                + "   \n"
                + "   public class TestModifiers\n"
                + "   {\n"
                + "      \n"
                + "      private static var attr_stat_private:int = 1;\n"
                + "      \n"
                + "      public static var attr_stat_public:int = 2;\n"
                + "      \n"
                + "      internal static var attr_stat_internal:int = 3;\n"
                + "      \n"
                + "      protected static var attr_stat_protected:int = 4;\n"
                + "      \n"
                + "      myInternal static var attr_stat_namespace_explicit:int = 5;\n"
                + "      \n"
                + "      myInternal2 static var attr_stat_namespace_implicit:int = 6;\n"
                + "       \n"
                + "      \n"
                + "      private var attr_inst_private:int = 7;\n"
                + "      \n"
                + "      public var attr_inst_public:int = 8;\n"
                + "      \n"
                + "      internal var attr_inst_internal:int = 9;\n"
                + "      \n"
                + "      protected var attr_inst_protected:int = 10;\n"
                + "      \n"
                + "      myInternal var attr_inst_namespace_explicit:int = 11;\n"
                + "      \n"
                + "      myInternal2 var attr_inst_namespace_implicit:int = 12;\n"
                + "      \n"
                + "      public function TestModifiers()\n"
                + "      {\n"
                + "         super();\n"
                + "      }\n"
                + "      \n"
                + "      private static function func_stat_private() : int\n"
                + "      {\n"
                + "         return 1;\n"
                + "      }\n"
                + "      \n"
                + "      public static function func_stat_public() : int\n"
                + "      {\n"
                + "         return 2;\n"
                + "      }\n"
                + "      \n"
                + "      internal static function func_stat_internal() : int\n"
                + "      {\n"
                + "         return 3;\n"
                + "      }\n"
                + "      \n"
                + "      protected static function func_stat_protected() : int\n"
                + "      {\n"
                + "         return 4;\n"
                + "      }\n"
                + "      \n"
                + "      myInternal static function func_stat_namespace_explicit() : int\n"
                + "      {\n"
                + "         return 5;\n"
                + "      }\n"
                + "      \n"
                + "      myInternal2 static function func_stat_namespace_implicit() : int\n"
                + "      {\n"
                + "         return 6;\n"
                + "      }\n"
                + "      \n"
                + "      private function func_inst_private() : int\n"
                + "      {\n"
                + "         return 7;\n"
                + "      }\n"
                + "      \n"
                + "      public function func_inst_public() : int\n"
                + "      {\n"
                + "         return 8;\n"
                + "      }\n"
                + "      \n"
                + "      internal function func_inst_internal() : int\n"
                + "      {\n"
                + "         return 9;\n"
                + "      }\n"
                + "      \n"
                + "      protected function func_inst_protected() : int\n"
                + "      {\n"
                + "         return 10;\n"
                + "      }\n"
                + "      \n"
                + "      myInternal function func_inst_namespace_explicit() : int\n"
                + "      {\n"
                + "         return 11;\n"
                + "      }\n"
                + "      \n"
                + "      myInternal2 function func_inst_namespace_implicit() : int\n"
                + "      {\n"
                + "         return 12;\n"
                + "      }\n"
                + "   }\n"
                + "}");
    }

    @Test
    public void testGetOuterScope() {
        decompileScriptPack("getouterscope", "mypkg.MainClass", "package mypkg\n"
                + "{\n"
                + "   import flash.display.DisplayObject;\n"
                + "   import flash.display.DisplayObjectContainer;\n"
                + "   import flash.display.InteractiveObject;\n"
                + "   import flash.display.MovieClip;\n"
                + "   import flash.display.Sprite;\n"
                + "   import flash.events.EventDispatcher;\n"
                + "   import flash.text.TextField;\n"
                + "   \n"
                + "   public class MainClass extends MovieClip\n"
                + "   {\n"
                + "       \n"
                + "      \n"
                + "      private var myTextBox:TextField;\n"
                + "      \n"
                + "      public function MainClass()\n"
                + "      {\n"
                + "         super();\n"
                + "         this.myTextBox = new TextField();\n"
                + "         this.myTextBox.text = \"\";\n"
                + "         this.myTextBox.width = 1024;\n"
                + "         this.myTextBox.height = 1024;\n"
                + "         this.myTextBox.multiline = true;\n"
                + "         addChild(this.myTextBox);\n"
                + "         this.test();\n"
                + "      }\n"
                + "      \n"
                + "      internal function test() : void\n"
                + "      {\n"
                + "         this.myTextBox.text = \"scopes:\\n\" + global + \"\\n\" + Object + \"\\n\" + EventDispatcher + \"\\n\" + DisplayObject + \"\\n\" + InteractiveObject + \"\\n\" + DisplayObjectContainer + \"\\n\" + Sprite + \"\\n\" + MovieClip + \"\\n\" + MainClass + \"\\n\";\n"
                + "      }\n"
                + "   }\n"
                + "}");
    }

    @Test
    public void testCollidingTraitNames() {
        decompileScriptPack("standard", "tests_classes.TestCollidingTraitNames", "package tests_classes\n"
                + "{\n"
                + "   public class TestCollidingTraitNames extends CollidingAttributeParent\n"
                + "   {\n"
                + "       \n"
                + "      \n"
                + "      public var CollidingAttribute:tests_classes.CollidingAttribute;\n"
                + "      \n"
                + "      public function TestCollidingTraitNames()\n"
                + "      {\n"
                + "         super();\n"
                + "      }\n"
                + "      \n"
                + "      public function test() : void\n"
                + "      {\n"
                + "         var t:tests_classes.CollidingAttribute2 = null;\n"
                + "      }\n"
                + "      \n"
                + "      public function CollidingMethod() : void\n"
                + "      {\n"
                + "         var t:tests_classes.CollidingMethod = null;\n"
                + "      }\n"
                + "   }\n"
                + "}");
    }

    @Test
    public void testInitializedVar() {
        decompileScriptPack("standard", "tests_classes.initializedvar", "package tests_classes\n"
                + "{\n"
                + "   public var initializedvar:Object = {\n"
                + "      \"a\":1,\n"
                + "      \"b\":2,\n"
                + "      \"c\":3\n"
                + "   };\n"
                + "}");
    }

    @Test
    public void testInitializedConst() {
        decompileScriptPack("standard", "tests_classes.initializedconst", "package tests_classes\n"
                + "{\n"
                + "   public const initializedconst:Object = {\n"
                + "      \"a\":1,\n"
                + "      \"b\":2,\n"
                + "      \"c\":3\n"
                + "   };\n"
                + "}");
    }

    @Test
    public void testSubClass() {
        decompileScriptPack("standard", "tests_classes.TestSubClass", "package tests_classes\n"
                + "{\n"
                + "   public class TestSubClass\n"
                + "   {\n"
                + "       \n"
                + "      \n"
                + "      public function TestSubClass()\n"
                + "      {\n"
                + "         super();\n"
                + "      }\n"
                + "      \n"
                + "      public function run() : *\n"
                + "      {\n"
                + "         var sc:SubClass = new SubClass();\n"
                + "         sc.a_internal = 1;\n"
                + "         sc.c_public = 3;\n"
                + "      }\n"
                + "   }\n"
                + "}\n"
                + "\n"
                + "class SubClass\n"
                + "{\n"
                + "    \n"
                + "   \n"
                + "   internal var a_internal:int;\n"
                + "   \n"
                + "   private var b_private:int;\n"
                + "   \n"
                + "   public var c_public:int;\n"
                + "   \n"
                + "   public function SubClass()\n"
                + "   {\n"
                + "      super();\n"
                + "   }\n"
                + "}");
    }

    @Test
    public void testScriptInitializer() {
        decompileScriptPack("standard", "tests_classes.TestScriptInitializer", "package tests_classes\n"
                + "{\n"
                + "   import tests.TestHello;\n"
                + "   \n"
                + "   public class TestScriptInitializer\n"
                + "   {\n"
                + "      private static var sv:int;\n"
                + "      \n"
                + "      private static var sa:int = 5;\n"
                + "      \n"
                + "      private static const sc:int = Math.floor(Math.random() * 50) + sa + x;\n"
                + "      \n"
                + "      private static var sb:int = sa + 20;\n"
                + "      \n"
                + "      if(Math.random() * 10 >= 5)\n"
                + "      {\n"
                + "         sa += 100;\n"
                + "      }\n"
                + "      else\n"
                + "      {\n"
                + "         sa += 200;\n"
                + "      }\n"
                + "      if(sb > 100)\n"
                + "      {\n"
                + "         sb += 10;\n"
                + "      }\n"
                + "      else\n"
                + "      {\n"
                + "         sb += 20;\n"
                + "      }\n"
                + "      for each(sv in [1,3,5])\n"
                + "      {\n"
                + "         trace(sv);\n"
                + "      }\n"
                + "      \n"
                + "      public function TestScriptInitializer()\n"
                + "      {\n"
                + "         super();\n"
                + "      }\n"
                + "      \n"
                + "      public function test() : void\n"
                + "      {\n"
                + "         var x:int = 5;\n"
                + "         var th:TestHello = new TestHello();\n"
                + "      }\n"
                + "   }\n"
                + "}\n"
                + "\n"
                + "import tests.TestHello;\n"
                + "\n"
                + "var v:int;\n"
                + "\n"
                + "var x:int = Math.random() * 100;\n"
                + "\n"
                + "var a:int = 5;\n"
                + "\n"
                + "if(Math.random() * 10 >= 5)\n"
                + "{\n"
                + "   a = a + 100;\n"
                + "}\n"
                + "else\n"
                + "{\n"
                + "   a = a + 200;\n"
                + "}\n"
                + "\n"
                + "const c:int = Math.floor(Math.random() * 50) + a;\n"
                + "\n"
                + "var b:int = a + 20;\n"
                + "\n"
                + "if(b > 100)\n"
                + "{\n"
                + "   b = b + 10;\n"
                + "}\n"
                + "else\n"
                + "{\n"
                + "   b = b + 20;\n"
                + "}\n"
                + "for each(v in [1,3,5])\n"
                + "{\n"
                + "   trace(v);\n"
                + "}\n"
                + "TestHello;\n"
        );
    }

    @Test
    public void testHaxeStaticVars() {
        /*
        Static vars in Haxe are initialized in script initializer (normal flash uses class initializer)
         */
        decompileScriptPack("haxe", "tests_classes.TestStaticVars", "package tests_classes\n"
                + "{\n"
                + "   public class TestStaticVars\n"
                + "   {\n"
                + "      public static var sa:int = 1001;\n"
                + "      \n"
                + "      public static var sb:int = 1002;\n"
                + "      \n"
                + "      public var b:int;\n"
                + "      \n"
                + "      public var a:int;\n"
                + "      \n"
                + "      public function TestStaticVars(param1:int, param2:int)\n"
                + "      {\n"
                + "         a = param1;\n"
                + "         b = param2;\n"
                + "      }\n"
                + "   }\n"
                + "}\n"
        );
    }

    @Test
    public void testConstructDynamically() {
        decompileScriptPack("assembled", "tests.TestConstructDynamically", "package tests\n"
                + "{\n"
                + "import flash.display.*;\n"
                + "import flash.utils.getDefinitionByName;\n"
                + "public class TestConstructDynamically\n"
                + "{\n"
                + "public function TestConstructDynamically()\n"
                + "{\n"
                + "super();\n"
                + "}\n"
                + "public function test() : void\n"
                + "{\n"
                + "var _loc1_:* = new (getDefinitionByName(\"flash.display\"+\".\"+\"Sprite\"))();\n"
                + "_loc1_ = new (getDefinitionByName(\"Object\"))();\n"
                + "}\n"
                + "}\n"
                + "}");
    }
}
