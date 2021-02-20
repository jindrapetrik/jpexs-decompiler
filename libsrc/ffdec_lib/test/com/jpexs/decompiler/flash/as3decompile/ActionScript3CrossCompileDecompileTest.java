package com.jpexs.decompiler.flash.as3decompile;

import com.jpexs.decompiler.flash.ActionScript3DecompileTestBase;
import java.io.IOException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript3CrossCompileDecompileTest extends ActionScript3DecompileTestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("flex", "testdata/as3_cross_compile/bin/as3_cross_compile.flex.swf");
        addSwf("air", "testdata/as3_cross_compile/bin/as3_cross_compile.air.swf");
    }

    @DataProvider
    public Object[][] swfNamesProvider() {
        return new Object[][]{
            {"flex"},
            {"air"}
        };
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryCatch(String swfUsed) {
        decompileMethod(swfUsed, "testTryCatch", "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryCatchExceptionUsage(String swfUsed) {
        decompileMethod(swfUsed, "testTryCatchExceptionUsage", "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"catched exception: \" + e.message);\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryCatchIfInTry(String swfUsed) {
        decompileMethod(swfUsed, "testTryCatchIfInTry", "var a:Boolean = true;\r\n"
                + "trace(\"before\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "if(a)\r\n"
                + "{\r\n"
                + "trace(\"ret\");\r\n"
                + "return;\r\n"
                + "}\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryCatchInIf(String swfUsed) {
        decompileMethod(swfUsed, "testTryCatchInIf", "var a:int = Math.random();\r\n"
                + "if(a > 10)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "return 1;\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "}\r\n"
                + "}\r\n"
                + "return 2;\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryCatchInIf2(String swfUsed) {
        decompileMethod(swfUsed, "testTryCatchInIf2", "var a:int = Math.random();\r\n"
                + "if(a > 10)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"a\");\r\n"
                + "return;\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch 1\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"b\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch 2\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryCatchInWhile(String swfUsed) {
        decompileMethod(swfUsed, "testTryCatchInWhile", "trace(\"before loop\");\r\n"
                + "while(true)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "while(true)\r\n"
                + "{\r\n"
                + "trace(\"a\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "catch(e:EOFError)\r\n"
                + "{\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "}\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryCatchInWhile2(String swfUsed) {
        decompileMethod(swfUsed, "testTryCatchInWhile2", "var a:int = 0;\r\n"
                + "a = 0;\r\n"
                + "trace(\"before loop\");\r\n"
                + "while(a > 5)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "if(a == 6)\r\n"
                + "{\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "if(a == 7)\r\n"
                + "{\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "trace(\"after inner while\");\r\n"
                + "}\r\n"
                + "catch(e:EOFError)\r\n"
                + "{\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "if(a == 8)\r\n"
                + "{\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "a++;\r\n"
                + "}\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryCatchInWhile3(String swfUsed) {
        decompileMethod(swfUsed, "testTryCatchInWhile3", "var a:int = 0;\r\n"
                + "a = 0;\r\n"
                + "trace(\"before loop\");\r\n"
                + "while(a > 5)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "return \"intry return\";\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "a++;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "return \"OK\";\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryCatchInWhile4(String swfUsed) {
        decompileMethod(swfUsed, "testTryCatchInWhile4", "var a:int = 0;\r\n"
                + "a = 0;\r\n"
                + "while(true)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"try2\");\r\n"
                + "if(a == 10)\r\n"
                + "{\r\n"
                + "trace(\"br\");\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "return;\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch2\");\r\n"
                + "trace(\"a=\" + a);\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryCatchInWhile5(String swfUsed) {
        decompileMethod(swfUsed, "testTryCatchInWhile5", "var i:int = 0;\r\n"
                + "var j:int = 0;\r\n"
                + "i = 0;\r\n"
                + "j = 5;\r\n"
                + "while(i < 10)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "if(j > 4)\r\n"
                + "{\r\n"
                + "throw new Error(\"Problem: \" + e);\r\n"
                + "}\r\n"
                + "}\r\n"
                + "i++;\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryCatchLoop(String swfUsed) {
        decompileMethod(swfUsed, "testTryCatchLoop", "var j:int = 0;\r\n"
                + "var i:int = 0;\r\n"
                + "while(i < 100)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "j = 0;\r\n"
                + "while(j < 20)\r\n"
                + "{\r\n"
                + "trace(\"a\");\r\n"
                + "j++;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "catch(e:EOFError)\r\n"
                + "{\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "trace(\"after_try\");\r\n"
                + "i++;\r\n"
                + "}\r\n"
                + "trace(\"end\");\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryCatchLoopBreak(String swfUsed) {
        decompileMethod(swfUsed, "testTryCatchLoopBreak", "var a:int = 0;\r\n"
                + "a = 0;\r\n"
                + "trace(\"before loop\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try1\");\r\n"
                + "while(a < 10)\r\n"
                + "{\r\n"
                + "trace(\"a=\" + a);\r\n"
                + "a++;\r\n"
                + "}\r\n"
                + "trace(\"in try2\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryCatchLoopBreak2(String swfUsed) {
        decompileMethod(swfUsed, "testTryCatchLoopBreak2", "var a:int = 0;\r\n"
                + "a = 0;\r\n"
                + "trace(\"before loop\");\r\n"
                + "while(a < 20)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "return;\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "trace(\"a=\" + a);\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryCatchLoopBreak3(String swfUsed) {
        decompileMethod(swfUsed, "testTryCatchLoopBreak3", "var a:int = 0;\r\n"
                + "a = 0;\r\n"
                + "trace(\"before loop\");\r\n"
                + "while(true)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch1\");\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "catch(e:EOFError)\r\n"
                + "{\r\n"
                + "trace(\"in catch2\");\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryCatchLoopBreak4(String swfUsed) {
        decompileMethod(swfUsed, "testTryCatchLoopBreak4", "var a:int = 0;\r\n"
                + "a = 0;\r\n"
                + "trace(\"before loop\");\r\n"
                + "while(true)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch1\");\r\n"
                + "if(a > 5)\r\n"
                + "{\r\n"
                + "trace(\"a\");\r\n"
                + "if(a > 6)\r\n"
                + "{\r\n"
                + "trace(\"b\");\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "trace(\"c\");\r\n"
                + "}\r\n"
                + "trace(\"in catch1b\");\r\n"
                + "if(a > 10)\r\n"
                + "{\r\n"
                + "trace(\"d\");\r\n"
                + "if(a > 11)\r\n"
                + "{\r\n"
                + "trace(\"e\");\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "trace(\"f\");\r\n"
                + "}\r\n"
                + "trace(\"in catch1c\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryCatchLoopBreak5(String swfUsed) {
        decompileMethod(swfUsed, "testTryCatchLoopBreak5", "var a:int = 0;\r\n"
                + "a = 0;\r\n"
                + "trace(\"before loop\");\r\n"
                + "while(true)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch1\");\r\n"
                + "while(true)\r\n"
                + "{\r\n"
                + "trace(\"xx\");\r\n"
                + "if(a > 5)\r\n"
                + "{\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "trace(\"yy\");\r\n"
                + "}\r\n"
                + "trace(\"in catch1c\");\r\n"
                + "}\r\n"
                + "}\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryCatchLoopBreak6(String swfUsed) {
        decompileMethod(swfUsed, "testTryCatchLoopBreak6", "var a:int = 0;\r\n"
                + "a = 0;\r\n"
                + "trace(\"before loop\");\r\n"
                + "while(a < 10)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch1\");\r\n"
                + "if(a > 3)\r\n"
                + "{\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try2\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch2\");\r\n"
                + "if(a > 4)\r\n"
                + "{\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "}\r\n"
                + "a++;\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryCatchReturn(String swfUsed) {
        decompileMethod(swfUsed, "testTryCatchReturn", "var a:int = 0;\r\n"
                + "a = 5;\r\n"
                + "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "if(a == 5)\r\n"
                + "{\r\n"
                + "return a;\r\n"
                + "}\r\n"
                + "trace(\"in catch2\");\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n"
                + "return -1;\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryCatchTry(String swfUsed) {
        decompileMethod(swfUsed, "testTryCatchTry", "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in catch try\");\r\n"
                + "}\r\n"
                + "catch(e2:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch in catch\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryCatchWith(String swfUsed) {
        decompileMethod(swfUsed, "testTryCatchWith", "var a:MyTest = new MyTest();\r\n"
                + "trace(\"before with\");\r\n"
                + "with(a)\r\n"
                + "{\r\n"
                + "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "attrib = attrib + 1;\r\n"
                + "trace(\"in catch\");\r\n"
                + "}\r\n"
                + "trace(\"after try\");\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryFinally(String swfUsed) {
        decompileMethod(swfUsed, "testTryFinally", "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "trace(\"in finally\");\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryFinallyDirectReturnInFinally(String swfUsed) {
        decompileMethod(swfUsed, "testTryFinallyDirectReturnInFinally", "var str:String = \"xxx\";\r\n"
                + "try\r\n"
                + "{\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"error\");\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "trace(\"hi \");\r\n"
                + "if(str == \"check\")\r\n"
                + "{\r\n"
                + "return str;\r\n"
                + "}\r\n"
                + "return \"hu\" + str;\r\n"
                + "}\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryFinallyLoop(String swfUsed) {
        decompileMethod(swfUsed, "testTryFinallyLoop", "var i:int = 0;\r\n"
                + "while(i < 10)\r\n"
                + "{\r\n"
                + "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "if(i == 5)\r\n"
                + "{\r\n"
                + "i = i + 5;\r\n"
                + "trace(\"continue while\");\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "trace(\"in finally\");\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n"
                + "i++;\r\n"
                + "}\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryFinallyLoopInFinally(String swfUsed) {
        decompileMethod(swfUsed, "testTryFinallyLoopInFinally", "var i:int = 0;\r\n"
                + "while(i < 10)\r\n"
                + "{\r\n"
                + "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "if(i == 5)\r\n"
                + "{\r\n"
                + "i = i + 7;\r\n"
                + "trace(\"continue while\");\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "trace(\"in finally\");\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n"
                + "i++;\r\n"
                + "}\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryFinallyMultipleCatch(String swfUsed) {
        decompileMethod(swfUsed, "testTryFinallyMultipleCatch", "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch Error\");\r\n"
                + "}\r\n"
                + "catch(e:EOFError)\r\n"
                + "{\r\n"
                + "trace(\"in catch EOFError\");\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "trace(\"in finally\");\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryFinallyNoCatch(String swfUsed) {
        decompileMethod(swfUsed, "testTryFinallyNoCatch", "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "trace(\"in finally\");\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryFinallyReturn(String swfUsed) {
        decompileMethod(swfUsed, "testTryFinallyReturn", "var a:int = 0;\r\n"
                + "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "a = 5;\r\n"
                + "if(a > 4)\r\n"
                + "{\r\n"
                + "return \"RET\";\r\n"
                + "}\r\n"
                + "trace(\"between\");\r\n"
                + "if(a < 3)\r\n"
                + "{\r\n"
                + "return \"RE2\";\r\n"
                + "}\r\n"
                + "trace(\"in try2\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "trace(\"in finally\");\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n"
                + "return \"RETFINAL\";\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryFinallyReturnInFinally(String swfUsed) {
        decompileMethod(swfUsed, "testTryFinallyReturnInFinally", "var a:int = 0;\r\n"
                + "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "a = 5;\r\n"
                + "if(a > 4)\r\n"
                + "{\r\n"
                + "return \"RET\";\r\n"
                + "}\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "trace(\"in finally\");\r\n"
                + "if(a > 6)\r\n"
                + "{\r\n"
                + "return \"FINRET1\";\r\n"
                + "}\r\n"
                + "trace(\"xx\");\r\n"
                + "if(a > 5)\r\n"
                + "{\r\n"
                + "return \"FINRET2\";\r\n"
                + "}\r\n"
                + "trace(\"nofinret\");\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n"
                + "return \"RETEXIT\";\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryFinallyReturnNested(String swfUsed) {
        decompileMethod(swfUsed, "testTryFinallyReturnNested", "var a:int = Math.random() * 5;\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"before try2\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try2\");\r\n"
                + "if(a > 4)\r\n"
                + "{\r\n"
                + "return \"RET\";\r\n"
                + "}\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "trace(\"in finally2\");\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "trace(\"in finally1\");\r\n"
                + "}\r\n"
                + "return \"RETFINAL\";\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryFinallyReturnNested2(String swfUsed) {
        decompileMethod(swfUsed, "testTryFinallyReturnNested2", "var a:int = Math.random() * 5;\r\n"
                + "try\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try2\");\r\n"
                + "if(a > 4)\r\n"
                + "{\r\n"
                + "return \"RET\";\r\n"
                + "}\r\n"
                + "}\r\n"
                + "catch(e2:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch2:e\");\r\n"
                + "}\r\n"
                + "catch(e2:EOFError)\r\n"
                + "{\r\n"
                + "trace(\"in catch2:eof\");\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "trace(\"in finally2\");\r\n"
                + "}\r\n"
                + "trace(\"after2\");\r\n"
                + "}\r\n"
                + "catch(e1:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch1:e\");\r\n"
                + "}\r\n"
                + "catch(e1:EOFError)\r\n"
                + "{\r\n"
                + "trace(\"in catch1:eof\");\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "trace(\"in finally1\");\r\n"
                + "}\r\n"
                + "trace(\"after1\");\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "trace(\"in finally0\");\r\n"
                + "}\r\n"
                + "trace(\"after0\");\r\n"
                + "return \"RETFINAL\";\r\n",
                false);
    }

    @Test(dataProvider = "swfNamesProvider")
    public void testTryFinallyReturnVoid(String swfUsed) {
        decompileMethod(swfUsed, "testTryFinallyReturnVoid", "var a:int = Math.random() * 5;\r\n"
                + "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "if(a > 4)\r\n"
                + "{\r\n"
                + "return;\r\n"
                + "}\r\n"
                + "trace(\"in try2\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "trace(\"in finally\");\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }
}
