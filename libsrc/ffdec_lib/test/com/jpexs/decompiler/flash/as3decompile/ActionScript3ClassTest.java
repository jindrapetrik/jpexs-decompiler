package com.jpexs.decompiler.flash.as3decompile;

import com.jpexs.decompiler.flash.ActionScript3DecompileTestBase;
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
    }

    private void decompileScriptPack(String path, String expectedResult) {

        DoABC2Tag tag = null;
        ABC abc = null;
        ScriptPack scriptPack = null;
        for (Tag t : getSwf("standard").getTags()) {
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
            scriptPack.toSource(writer, abc.script_info.get(scriptPack.scriptIndex).traits.traits, new ConvertData(), ScriptExportMode.AS, false);
        } catch (InterruptedException ex) {
            fail();
        }
        String actualResult = cleanPCode(writer.toString());
        expectedResult = cleanPCode(expectedResult);
        assertEquals(actualResult, expectedResult);
    }

    @Test
    public void testMyPackage1TestClass() {
        decompileScriptPack("tests_classes.mypackage1.TestClass", "package tests_classes.mypackage1\n"
                + "{\n"
                + "   public class TestClass implements tests_classes.mypackage1.TestInterface\n"
                + "   {\n"
                + "       \n"
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
                + "   }\n"
                + "}");
    }

    @Test
    public void testMyPackage1TestClass2() {
        decompileScriptPack("tests_classes.mypackage1.TestClass2", "package tests_classes.mypackage1\n"
                + "{\n"
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
        decompileScriptPack("tests_classes.mypackage1.TestInterface", "package tests_classes.mypackage1\n"
                + "{\n"
                + "   public interface TestInterface extends tests_classes.mypackage2.TestInterface\n"
                + "   {\n"
                + "       \n"
                + "      function testMethod1() : void;\n"
                + "   }\n"
                + "}");
    }

    @Test
    public void testMyPackage1MyNamespace() {
        decompileScriptPack("tests_classes.mypackage1.myNamespace", "package tests_classes.mypackage1\n"
                + "{\n"
                + "   public namespace myNamespace = \"https://www.free-decompiler.com/flash/test/namespace\";\n"
                + "}");
    }

    @Test
    public void testMyPackage2TestClass() {
        decompileScriptPack("tests_classes.mypackage2.TestClass", "package tests_classes.mypackage2\n"
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
        decompileScriptPack("tests_classes.mypackage2.TestInterface", "package tests_classes.mypackage2\n"
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
        decompileScriptPack("tests_classes.mypackage3.TestClass", "package tests_classes.mypackage3\n"
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
        decompileScriptPack("tests_classes.TestThisOutsideClass", "package tests_classes\n"
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
}
