package com.jpexs.decompiler.flash.as3decompile;

import com.jpexs.decompiler.flash.ActionScript3DecompileTestBase;
import java.io.IOException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript3CrossCompileSwfToolsDecompileTest extends ActionScript3DecompileTestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("swftools", "testdata/as3_cross_compile/bin/as3_cross_compile.swftools.swf");
    }

    @Test
    public void testTryCatch() {
        decompileMethod("swftools", "testTryCatch", "trace(\"before try\");\r\n"
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

    @Test
    public void testTryCatchExceptionUsage() {
        decompileMethod("swftools", "testTryCatchExceptionUsage", "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "var _loc1_:* = e;\r\n"
                + "trace(\"catched exception: \" + _loc1_.message);\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test
    public void testTryCatchIfInTry() {
        decompileMethod("swftools", "testTryCatchIfInTry", "var _loc1_:Boolean = true;\r\n"
                + "trace(\"before\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "if(_loc1_)\r\n"
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

    @Test
    public void testTryCatchInIf() {
        decompileMethod("swftools", "testTryCatchInIf", "var _loc1_:int = Math.random();\r\n"
                + "if(_loc1_ > 10)\r\n"
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

    @Test
    public void testTryCatchInIf2() {
        decompileMethod("swftools", "testTryCatchInIf2", "var _loc1_:int = Math.random();\r\n"
                + "if(_loc1_ > 10)\r\n"
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

    @Test
    public void testTryCatchInWhile() {
        decompileMethod("swftools", "testTryCatchInWhile", "trace(\"before loop\");\r\n"
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

    @Test
    public void testTryCatchInWhile2() {
        decompileMethod("swftools", "testTryCatchInWhile2", "var _loc1_:int = 0;\r\n"
                + "_loc1_ = 0;\r\n"
                + "trace(\"before loop\");\r\n"
                + "while(_loc1_ > 5)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "if(_loc1_ == 6)\r\n"
                + "{\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "if(_loc1_ == 7)\r\n"
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
                + "if(_loc1_ == 8)\r\n"
                + "{\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "_loc1_++;\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testTryCatchInWhile3() {
        decompileMethod("swftools", "testTryCatchInWhile3", "var _loc1_:int = 0;\r\n"
                + "_loc1_ = 0;\r\n"
                + "trace(\"before loop\");\r\n"
                + "while(_loc1_ > 5)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "return \"intry return\";\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "_loc1_++;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "return \"OK\";\r\n",
                false);
    }

    @Test
    public void testTryCatchInWhile4() {
        decompileMethod("swftools", "testTryCatchInWhile4", "var _loc1_:int = 0;\r\n"
                + "_loc1_ = 0;\r\n"
                + "while(true)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"try2\");\r\n"
                + "if(_loc1_ == 10)\r\n"
                + "{\r\n"
                + "trace(\"br\");\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "return;\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch2\");\r\n"
                + "trace(\"a=\" + _loc1_);\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test
    public void testTryCatchInWhile5() {
        decompileMethod("swftools", "testTryCatchInWhile5", "var _loc1_:int = 0;\r\n"
                + "_loc1_ = 0;\r\n"
                + "var _loc2_:int = 0;\r\n"
                + "_loc2_ = 5;\r\n"
                + "while(_loc1_ < 10)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "var _loc3_:* = e;\r\n"
                + "if(_loc2_ > 4)\r\n"
                + "{\r\n"
                + "throw new Error(\"Problem: \" + _loc3_);\r\n"
                + "}\r\n"
                + "}\r\n"
                + "_loc1_++;\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test
    public void testTryCatchLoop() {
        decompileMethod("swftools", "testTryCatchLoop", "var _loc1_:int = 0;\r\n"
                + "while(_loc1_ < 100)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "var _loc2_:int = 0;\r\n"
                + "while(_loc2_ < 20)\r\n"
                + "{\r\n"
                + "trace(\"a\");\r\n"
                + "_loc2_++;\r\n"
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
                + "_loc1_++;\r\n"
                + "}\r\n"
                + "trace(\"end\");\r\n",
                false);
    }

    @Test
    public void testTryCatchLoopBreak() {
        decompileMethod("swftools", "testTryCatchLoopBreak", "var _loc1_:int = 0;\r\n"
                + "_loc1_ = 0;\r\n"
                + "trace(\"before loop\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try1\");\r\n"
                + "while(_loc1_ < 10)\r\n"
                + "{\r\n"
                + "trace(\"a=\" + _loc1_);\r\n"
                + "_loc1_++;\r\n"
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

    @Test
    public void testTryCatchLoopBreak2() {
        decompileMethod("swftools", "testTryCatchLoopBreak2", "var _loc1_:int = 0;\r\n"
                + "_loc1_ = 0;\r\n"
                + "trace(\"before loop\");\r\n"
                + "while(_loc1_ < 20)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "return;\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "trace(\"a=\" + _loc1_);\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test
    public void testTryCatchLoopBreak3() {
        decompileMethod("swftools", "testTryCatchLoopBreak3", "var _loc1_:int = 0;\r\n"
                + "_loc1_ = 0;\r\n"
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

    @Test
    public void testTryCatchLoopBreak4() {
        decompileMethod("swftools", "testTryCatchLoopBreak4", "var _loc1_:int = 0;\r\n"
                + "_loc1_ = 0;\r\n"
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
                + "if(_loc1_ > 5)\r\n"
                + "{\r\n"
                + "trace(\"a\");\r\n"
                + "if(_loc1_ > 6)\r\n"
                + "{\r\n"
                + "trace(\"b\");\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "trace(\"c\");\r\n"
                + "}\r\n"
                + "trace(\"in catch1b\");\r\n"
                + "if(_loc1_ > 10)\r\n"
                + "{\r\n"
                + "trace(\"d\");\r\n"
                + "if(_loc1_ > 11)\r\n"
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

    @Test
    public void testTryCatchLoopBreak5() {
        decompileMethod("swftools", "testTryCatchLoopBreak5", "var _loc1_:int = 0;\r\n"
                + "_loc1_ = 0;\r\n"
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
                + "if(_loc1_ > 5)\r\n"
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

    @Test
    public void testTryCatchLoopBreak6() {
        decompileMethod("swftools", "testTryCatchLoopBreak6", "var _loc1_:int = 0;\r\n"
                + "_loc1_ = 0;\r\n"
                + "trace(\"before loop\");\r\n"
                + "while(_loc1_ < 10)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch1\");\r\n"
                + "if(_loc1_ > 3)\r\n"
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
                + "if(_loc1_ > 4)\r\n"
                + "{\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "}\r\n"
                + "_loc1_++;\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test
    public void testTryCatchReturn() {
        decompileMethod("swftools", "testTryCatchReturn", "var _loc1_:int = 0;\r\n"
                + "_loc1_ = 5;\r\n"
                + "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "if(_loc1_ == 5)\r\n"
                + "{\r\n"
                + "return _loc1_;\r\n"
                + "}\r\n"
                + "trace(\"in catch2\");\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n"
                + "return -1;\r\n",
                false);
    }

    @Test
    public void testTryCatchTry() {
        decompileMethod("swftools", "testTryCatchTry", "trace(\"before try\");\r\n"
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

    @Test
    public void testTryCatchWith() {
        decompileMethod("swftools", "testTryCatchWith", "var _loc1_:* = new MyTest();\r\n"
                + "trace(\"before with\");\r\n"
                + "var _loc2_:*;\r\n"
                + "with(_loc2_ = _loc1_)\r\n"
                + "{\r\n"
                + "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "with(_loc2_)\r\n"
                + "{\r\n"
                + "\r\n"
                + "attrib = attrib + 1;\r\n"
                + "trace(\"in catch\");\r\n"
                + "}\r\n"
                + "trace(\"after try\");\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test
    public void testTryFinally() {
        decompileMethod("swftools", "testTryFinally", "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "catch(_loc_e_:*)\r\n"
                + "{\r\n"
                + "trace(\"in finally\");\r\n"
                + "throw _loc_e_;\r\n"
                + "}\r\n"
                + "trace(\"in finally\");\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test
    public void testTryFinallyDirectReturnInFinally() {
        decompileMethod("swftools", "testTryFinallyDirectReturnInFinally", "var _loc1_:String = \"xxx\";\r\n"
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
                + "if(_loc1_ == \"check\")\r\n"
                + "{\r\n"
                + "return _loc1_;\r\n"
                + "}\r\n"
                + "return \"hu\" + _loc1_;\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testTryFinallyLoop() {
        decompileMethod("swftools", "testTryFinallyLoop", "var _loc1_:* = 0;\r\n"
                + "while(_loc1_ < 10)\r\n"
                + "{\r\n"
                + "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "if(_loc1_ == 5)\r\n"
                + "{\r\n"
                + "_loc1_ = _loc1_ + 5;\r\n"
                + "trace(\"continue while\");\r\n"
                + "trace(\"in finally\");\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "catch(_loc_e_:*)\r\n"
                + "{\r\n"
                + "trace(\"in finally\");\r\n"
                + "throw _loc_e_;\r\n"
                + "}\r\n"
                + "trace(\"in finally\");\r\n"
                + "trace(\"after\");\r\n"
                + "_loc1_++;\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testTryFinallyLoopInFinally() {
        decompileMethod("swftools", "testTryFinallyLoopInFinally", "var _loc1_:* = 0;\r\n"
                + "while(_loc1_ < 10)\r\n"
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
                + "if(_loc1_ == 5)\r\n"
                + "{\r\n"
                + "_loc1_ = _loc1_ + 7;\r\n"
                + "trace(\"continue while\");\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "trace(\"in finally\");\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n"
                + "_loc1_++;\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testTryFinallyMultipleCatch() {
        decompileMethod("swftools", "testTryFinallyMultipleCatch", "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
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
                + "}\r\n"
                + "catch(_loc_e_:*)\r\n"
                + "{\r\n"
                + "trace(\"in finally\");\r\n"
                + "throw _loc_e_;\r\n"
                + "}\r\n"
                + "trace(\"in finally\");\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test
    public void testTryFinallyNoCatch() {
        decompileMethod("swftools", "testTryFinallyNoCatch", "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "}\r\n"
                + "catch(_loc_e_:*)\r\n"
                + "{\r\n"
                + "trace(\"in finally\");\r\n"
                + "throw _loc_e_;\r\n"
                + "}\r\n"
                + "trace(\"in finally\");\r\n"
                + "trace(\"after\");\r\n",
                false);
    }

    @Test
    public void testTryFinallyReturn() {
        decompileMethod("swftools", "testTryFinallyReturn", "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "var _loc1_:int = 5;\r\n"
                + "if(_loc1_ > 4)\r\n"
                + "{\r\n"
                + "trace(\"in finally\");\r\n"
                + "return \"RET\";\r\n"
                + "}\r\n"
                + "trace(\"between\");\r\n"
                + "if(_loc1_ < 3)\r\n"
                + "{\r\n"
                + "trace(\"in finally\");\r\n"
                + "return \"RE2\";\r\n"
                + "}\r\n"
                + "trace(\"in try2\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "catch(_loc_e_:*)\r\n"
                + "{\r\n"
                + "trace(\"in finally\");\r\n"
                + "throw _loc_e_;\r\n"
                + "}\r\n"
                + "trace(\"in finally\");\r\n"
                + "trace(\"after\");\r\n"
                + "return \"RETFINAL\";\r\n",
                false);
    }

    @Test
    public void testTryFinallyReturnInFinally() {
        decompileMethod("swftools", "testTryFinallyReturnInFinally", "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "var _loc1_:int = 5;\r\n"
                + "if(_loc1_ > 4)\r\n"
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
                + "if(_loc1_ > 6)\r\n"
                + "{\r\n"
                + "return \"FINRET1\";\r\n"
                + "}\r\n"
                + "trace(\"xx\");\r\n"
                + "if(_loc1_ > 5)\r\n"
                + "{\r\n"
                + "return \"FINRET2\";\r\n"
                + "}\r\n"
                + "trace(\"nofinret\");\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n"
                + "return \"RETEXIT\";\r\n",
                false);
    }

    @Test
    public void testTryFinallyReturnNested() {
        decompileMethod("swftools", "testTryFinallyReturnNested", "var _loc1_:int = Math.random() * 5;\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"before try2\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try2\");\r\n"
                + "if(_loc1_ > 4)\r\n"
                + "{\r\n"
                + "trace(\"in finally2\");\r\n"
                + "trace(\"in finally1\");\r\n"
                + "return \"RET\";\r\n"
                + "}\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "catch(_loc_e_:*)\r\n"
                + "{\r\n"
                + "trace(\"in finally2\");\r\n"
                + "throw _loc_e_;\r\n"
                + "}\r\n"
                + "trace(\"in finally2\");\r\n"
                + "trace(\"after\");\r\n"
                + "}\r\n"
                + "catch(_loc_e_:*)\r\n"
                + "{\r\n"
                + "trace(\"in finally1\");\r\n"
                + "throw _loc_e_;\r\n"
                + "}\r\n"
                + "trace(\"in finally1\");\r\n"
                + "return \"RETFINAL\";\r\n",
                false);
    }

    @Test
    public void testTryFinallyReturnNested2() {
        decompileMethod("swftools", "testTryFinallyReturnNested2", "var _loc1_:int = Math.random() * 5;\r\n"
                + "try\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try2\");\r\n"
                + "if(_loc1_ > 4)\r\n"
                + "{\r\n"
                + "trace(\"in finally2\");\r\n"
                + "trace(\"in finally1\");\r\n"
                + "trace(\"in finally0\");\r\n"
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
                + "}\r\n"
                + "catch(_loc_e_:*)\r\n"
                + "{\r\n"
                + "trace(\"in finally2\");\r\n"
                + "throw _loc_e_;\r\n"
                + "}\r\n"
                + "trace(\"in finally2\");\r\n"
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
                + "}\r\n"
                + "catch(_loc_e_:*)\r\n"
                + "{\r\n"
                + "trace(\"in finally1\");\r\n"
                + "throw _loc_e_;\r\n"
                + "}\r\n"
                + "trace(\"in finally1\");\r\n"
                + "trace(\"after1\");\r\n"
                + "}\r\n"
                + "catch(_loc_e_:*)\r\n"
                + "{\r\n"
                + "trace(\"in finally0\");\r\n"
                + "throw _loc_e_;\r\n"
                + "}\r\n"
                + "trace(\"in finally0\");\r\n"
                + "trace(\"after0\");\r\n"
                + "return \"RETFINAL\";\r\n",
                false);
    }

    @Test
    public void testTryFinallyReturnVoid() {
        decompileMethod("swftools", "testTryFinallyReturnVoid", "var _loc1_:int = Math.random() * 5;\r\n"
                + "trace(\"before try\");\r\n"
                + "try\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"in try\");\r\n"
                + "if(_loc1_ > 4)\r\n"
                + "{\r\n"
                + "trace(\"in finally\");\r\n"
                + "return;\r\n"
                + "}\r\n"
                + "trace(\"in try2\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"in catch\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "catch(_loc_e_:*)\r\n"
                + "{\r\n"
                + "trace(\"in finally\");\r\n"
                + "throw _loc_e_;\r\n"
                + "}\r\n"
                + "trace(\"in finally\");\r\n"
                + "trace(\"after\");\r\n",
                false);
    }
}
