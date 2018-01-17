/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.AVM2DeobfuscatorJumps;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.ASM3Parser;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.ActionScript3Parser;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.graph.CompilationException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import org.testng.Assert;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript3DeobfuscatorTest extends ActionScriptTestBase {

    protected SWF swf;

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        //Main.initLogging(false);
        Configuration.autoDeobfuscate.set(true);
        Configuration.decimalAddress.set(false);
        Configuration.decompilationTimeoutSingleMethod.set(Integer.MAX_VALUE);
        swf = new SWF(new BufferedInputStream(new FileInputStream("testdata/as3/as3.swf")), false);
    }

    private String recompilePCode(String str) throws IOException, AVM2ParseException, InterruptedException {
        str = "code\r\n"
                + "getlocal_0\r\n"
                + "pushscope\r\n"
                + str
                + "returnvoid\r\n";
        final ABC abc = new ABC(new ABCContainerTag() {
            @Override
            public ABC getABC() {
                return null;
            }

            @Override
            public SWF getSwf() {
                return swf;
            }

            @Override
            public int compareTo(ABCContainerTag o) {
                return 0;
            }
        });
        MethodBody b = new MethodBody(abc, new Traits(), new byte[0], new ABCException[0]);
        AVM2Code code = ASM3Parser.parse(abc, new StringReader(str), null, b, new MethodInfo());
        b.setCode(code);
        new AVM2DeobfuscatorJumps().avm2CodeRemoveTraps("test", 0, true, 0, abc, null, 0, b);
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        code.toASMSource(abc.constants, new MethodInfo(), new MethodBody(abc, new Traits(), new byte[0], new ABCException[0]), ScriptExportMode.PCODE, writer);
        String ret = writer.toString();
        return ret.substring(ret.lastIndexOf("\r\ncode\r\n") + 8, ret.lastIndexOf("end ; code"));
    }

    private String recompile(String str) throws AVM2ParseException, IOException, CompilationException, InterruptedException {
        str = "package { public class Test {  public static function trace(s){ } public static function test(){ " + str + " }   }  }";
        final ABC abc = new ABC(new ABCContainerTag() {
            @Override
            public ABC getABC() {
                return null;
            }

            @Override
            public SWF getSwf() {
                return swf;
            }

            @Override
            public int compareTo(ABCContainerTag o) {
                return 0;
            }
        });
        ActionScript3Parser par = new ActionScript3Parser(abc, new ArrayList<>());
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        par.addScript(str, true, "Test.as", 0, 0);

        abc.script_info.get(0).getPacks(abc, 0, "", new ArrayList<>()).get(0).toSource(writer, abc.script_info.get(0).traits.traits, new ConvertData(), ScriptExportMode.AS, false);
        return writer.toString();
    }

    @DataProvider(name = "provideBasicTrueExpressions")
    public Object[][] provideBasicTrueExpressions() {
        return new Object[][]{
            {"1!=5"}, {"5==5"}, {"1<4"}, {"5>4"}, {"5*6==30"}
        };
    }

    @DataProvider(name = "provideBasicFalseExpressions")
    public Object[][] provideBasicFalseExpressions() {
        return new Object[][]{
            {"1==5"}, {"5!=5"}, {"1>4"}, {"5<4"}, {"5*7==12"}
        };
    }

    @Test(dataProvider = "provideBasicTrueExpressions")
    public void testRemoveBasicTrueExpressions(String expression) throws IOException, CompilationException, InterruptedException, TimeoutException, AVM2ParseException {
        String res = recompile("if(" + expression + "){"
                + "trace(\"OK\");"
                + "} else {"
                + "trace(\"FAIL\");"
                + "}");
        if (res.contains("\"FAIL\"")) {
            fail("OnFalse clause was not removed: " + res);
        }
        if (!res.contains("\"OK\"")) {
            fail("OnTrue clause was removed: " + res);
        }
    }

    @Test(dataProvider = "provideBasicFalseExpressions")
    public void testRemoveBasicFalseExpressions(String expression) throws Exception {
        String res = recompile("if(" + expression + "){"
                + "trace(\"FAIL\");"
                + "} else {"
                + "trace(\"OK\");"
                + "}");
        if (res.contains("\"FAIL\"")) {
            fail("OnTrue clause was not removed:" + res);
        }
        if (!res.contains("\"OK\"")) {
            fail("OnFalse clause was removed:" + res);
        }
    }

    @Test
    public void testRemoveKnownVariables() throws Exception {
        String res = recompile("var a = true; var b = false;"
                + "if(a){"
                + "trace(\"OK1\");"
                + "}else{"
                + "trace(\"FAIL1\");"
                + "}"
                + "if(b){"
                + "trace(\"FAIL2\");"
                + "}else{"
                + "trace(\"OK2\");"
                + "}");
        if (!res.contains("\"OK1\"")) {
            fail("if true OnTrue removed");
        }
        if (!res.contains("\"OK2\"")) {
            fail("if false OnFalse removed");
        }
        if (res.contains("\"FAIL1\"")) {
            fail("if true OnFalse not removed:");
        }
        if (res.contains("\"FAIL2\"")) {
            fail("if false OnTrue not removed");
        }
        if (res.contains("var ")) {
            fail("variables for obsucation not removed");
        }
        if (res.contains("if")) {
            fail("if clauses not removed");
        }
    }

    @Test
    public void testRemoveKnownVariables2() throws Exception {
        String res = recompile("var a = true; var b = false;"
                + "if(a){"
                + "trace(\"OK1\");"
                + "}else{"
                + "trace(\"FAIL1\");"
                + "}"
                //TODO:
                //+ "a = 59;"
                + "if(b){"
                + "trace(\"FAIL2\");"
                + "}else{"
                + "trace(\"OK2\");"
                + "}");
        if (!res.contains("\"OK1\"")) {
            fail("!OK1:" + res);
        }
        if (res.contains("\"FAIL1\"")) {
            fail("FAIL1");
        }
        if (!res.contains("\"OK2\"")) {
            fail("!OK2");
        }
        if (res.contains("\"FAIL2\"")) {
            fail("FAIL2");
        }
    }

    @Test
    public void testJumps() throws Exception {
        String res = recompilePCode("pushbyte 3\r\n"
                + "pushbyte 4\r\n"
                + "ifeq a\r\n" //should change to ifeq c
                + "jump b\r\n" //should not change
                + "a:jump c\r\n"
                + "c:pushbyte 4\r\n"
                + "b:pushbyte 3\r\n");
        Assert.assertEquals(res, "getlocal_0\r\n"
                + "pushscope\r\n"
                + "pushbyte 3\r\n"
                + "pushbyte 4\r\n"
                + "ifeq ofs000e\r\n"
                + "jump ofs0010\r\n"
                + "ofs000e:pushbyte 4\r\n"
                + "ofs0010:pushbyte 3\r\n"
                + "returnvoid\r\n");
    }

    // TODO: JPEXS @Test
    public void testNotRemoveParams() throws Exception {
        String res = recompile("function tst(p1,p2){"
                + "var a = 2;"
                + "var b = 3 * a;"
                + "if(b>1){"
                + "trace(\"OK1\");"
                + "}else{"
                + "trace(\"FAIL1\");"
                + "}"
                + "var c = p1*5;"
                + "if(c){"
                + "trace(\"OK2\");"
                + "}else{"
                + "trace(\"OK3\");"
                + "}"
                + "}");
        if (!res.contains("\"OK1\"")) {
            fail("basic if true onTrue removed");
        }
        if (res.contains("\"FAIL1\"")) {
            fail("basic if true onFalse not removed");
        }
        if (!res.contains("\"OK2\"")) {
            fail("if parameter onTrue removed");
        }
        if (!res.contains("\"OK3\"")) {
            fail("if parameter onFalse removed");
        }
    }

    //TODO: JPEXS @Test
    public void testEvailExpressionAfterWhile() throws Exception {
        String res = recompile("var a = 5;"
                + "while(true){"
                + "if(a==73){"
                + "a = 15;"
                + "}"
                + "if(a==1){"
                + "trace(\"FAIL1\");"
                + "}"
                + "if(a==5){"
                + "a=50;"
                + "}"
                + "if(a == 201){"
                + "break;"
                + "}"
                + "a++;"
                + "if(a == 53){"
                + "a = a + 20;"
                + "}"
                + "if(a>500){"
                + "trace(\"FAIL2\");"
                + "}"
                + "if(a==16){"
                + "a = 200;"
                + "}"
                + "}"
                + ""
                + "if(a == 201){"
                + "trace(\"OK\");"
                + "}else{"
                + "trace(\"FAIL3\");"
                + "}");
        if (res.contains("\"FAIL1\"")) {
            fail("unreachable if onTrue not removed");
        }
        if (res.contains("\"FAIL2\"")) {
            fail("unreachable if onTrue 2 not removed");
        }
        if (res.contains("\"FAIL3\"")) {
            fail("unreachable if onTrue 3 not removed");
        }
        if (!res.contains("\"OK\"")) {
            fail("reachable of onTrue removed");
        }
    }
}
