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
