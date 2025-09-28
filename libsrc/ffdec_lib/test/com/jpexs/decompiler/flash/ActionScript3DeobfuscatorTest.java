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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.AVM2DeobfuscatorGroupParts;
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.AVM2DeobfuscatorJumps;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.ASM3Parser;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.MissingSymbolHandler;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.ActionScript3Parser;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.ClassInfo;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.Float4;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerAdapter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.graph.CompilationException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.concurrent.TimeoutException;
import macromedia.asc.util.Decimal128;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
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
        Configuration.padAs3PCodeInstructionName.set(false);
        Configuration.useOldStyleGetSetLocalsAs3PCode.set(false);
        Configuration.labelOnSeparateLineAs3PCode.set(true);
        Configuration.decompilationTimeoutSingleMethod.set(5);
        swf = new SWF(new BufferedInputStream(new FileInputStream("testdata/as3/as3.swf")), false);
    }

    private void decompilePCode(String pCode, String expected) throws Exception {
        pCode = "code\r\n"
                + "getlocal0\r\n"
                + "pushscope\r\n"
                + pCode
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

            @Override
            public void setABC(ABC abc) {
            }
        });
        MethodBody b = new MethodBody(abc, new Traits(), new byte[0], new ABCException[0]);
        AVM2Code code = ASM3Parser.parse(abc, new StringReader(pCode), null, new MissingSymbolHandler() {
            //no longer ask for adding new constants
            @Override
            public boolean missingString(String value) {
                return true;
            }

            @Override
            public boolean missingInt(long value) {
                return true;
            }

            @Override
            public boolean missingUInt(long value) {
                return true;
            }

            @Override
            public boolean missingDouble(double value) {
                return true;
            }

            @Override
            public boolean missingDecimal(Decimal128 value) {
                return true;
            }

            @Override
            public boolean missingFloat(float value) {
                return true;
            }

            @Override
            public boolean missingFloat4(Float4 value) {
                return true;
            }
        }, b, new MethodInfo());
        b.setCode(code);
        abc.addMethodBody(b);
        abc.addMethodInfo(new MethodInfo(new int[]{0}, 0, 0, 0, new ValueKind[0], new int[0]));

        ClassInfo ci = new ClassInfo();
        InstanceInfo ii = new InstanceInfo();
        ii.name_index = abc.constants.addMultiname(
                Multiname.createQName(
                        false,
                        abc.constants.getStringId("Test", true),
                        abc.constants.getNamespaceId(Namespace.KIND_PACKAGE, "", 0, true)
                ));
        abc.addClass(ci, ii, 0);
        ScriptInfo si = new ScriptInfo(new Traits());
        abc.script_info.add(si);

        code.removeTraps(null, 0, b, abc, 0, -1, true, pCode);
        code.removeLabelsAndDebugLine(b);
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        String actual = b.toSource(new LinkedHashSet<>(), 10, new ArrayList<>(), swf.getAbcIndex(), 0, new HashSet<>());
        actual = actual.replace("\r\n", "\n");
        assertEquals(actual, expected);
    }

    private String recompilePCode(String str, SWFDecompilerAdapter deobfuscator) throws IOException, AVM2ParseException, InterruptedException {
        str = "code\r\n"
                + "getlocal0\r\n"
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

            @Override
            public void setABC(ABC abc) {
            }
        });
        MethodBody b = new MethodBody(abc, new Traits(), new byte[0], new ABCException[0]);
        AVM2Code code = ASM3Parser.parse(abc, new StringReader(str), null, b, new MethodInfo());
        b.setCode(code);
        deobfuscator.avm2CodeRemoveTraps("test", 0, true, 0, abc, null, 0, b);
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        code.toASMSource(abc, abc.constants, new MethodInfo(), new MethodBody(abc, new Traits(), new byte[0], new ABCException[0]), ScriptExportMode.PCODE, writer);
        writer.finishHilights();
        String ret = writer.toString();
        ret = ret.replaceAll("\r\n +", "\r\n");
        String prefix = "\r\ncode\r\n";
        String suffix = "end ; code";
        return ret.substring(ret.lastIndexOf(prefix) + prefix.length(), ret.lastIndexOf(suffix));
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

            @Override
            public void setABC(ABC abc) {
            }
        });
        AbcIndexing index = swf.getAbcIndex();
        index.addAbc(abc);
        index.rebuildPkgToObjectsNameMap();
        ActionScript3Parser par = new ActionScript3Parser(index);
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        par.addScript(str, "Test.as", 0, 0, swf.getDocumentClass(), abc);

        abc.script_info.get(0).getPacks(abc, 0, "", new ArrayList<>()).get(0).toSource(swf.getAbcIndex(), writer, abc.script_info.get(0).traits.traits, new ConvertData(), ScriptExportMode.AS, false, false, false);
        writer.finishHilights();
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
            fail("variables for obfuscation not removed");
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
                + "b:pushbyte 3\r\n", new AVM2DeobfuscatorJumps());
        Assert.assertEquals(res, "getlocal0\r\n"
                + "pushscope\r\n"
                + "pushbyte 3\r\n"
                + "pushbyte 4\r\n"
                + "ifeq ofs000e\r\n"
                + "jump ofs0010\r\n"
                + "ofs000e:\r\n"
                + "pushbyte 4\r\n"
                + "ofs0010:\r\n"
                + "pushbyte 3\r\n"
                + "returnvoid\r\n");
    }

    @Test
    public void testGroupParts() throws Exception {
        String res = recompilePCode(
                "pushbyte 1\r\npop\r\n"
                + "jump A\r\n"
                + "B:pushbyte 3\r\npop\r\n"
                + "jump C\r\n"
                + "A:pushbyte 2\r\npop\r\n"
                + "jump B\r\n"
                + "C:pushbyte 4\r\npop\r\n", new AVM2DeobfuscatorGroupParts());
        Assert.assertEquals(res, "getlocal0\r\n"
                + "pushscope\r\n"
                + "pushbyte 1\r\n"
                + "pop\r\n"
                + "pushbyte 2\r\n"
                + "pop\r\n"
                + "pushbyte 3\r\n"
                + "pop\r\n"
                + "pushbyte 4\r\n"
                + "pop\r\n"
                + "returnvoid\r\n");
    }

    @Test
    public void testGroupParts2() throws Exception {
        String res = recompilePCode(
                "pushbyte 1\r\npop\r\n"
                + "jump A\r\n"
                + "B:pushbyte 3\r\npop\r\n"
                + "jump C\r\n"
                + "A:pushbyte 2\r\npop\r\n"
                + "jump B\r\n"
                + "C:pushbyte 4\r\npop\r\n", new AVM2DeobfuscatorGroupParts());
        Assert.assertEquals(res, "getlocal0\r\n"
                + "pushscope\r\n"
                + "pushbyte 1\r\n"
                + "pop\r\n"
                + "pushbyte 2\r\n"
                + "pop\r\n"
                + "pushbyte 3\r\n"
                + "pop\r\n"
                + "pushbyte 4\r\n"
                + "pop\r\n"
                + "returnvoid\r\n");
    }

    @Test
    public void testIgnoreLabels() throws Exception {
        decompilePCode("getlocal1\n"
                + "            setlocal2\n"
                + "            pushfalse\n"
                + "            setlocal3\n"
                + "            pushbyte 0\n"
                + "            convert_u\n"
                + "            setlocal 4\n"
                + "            jump ofs00a4\n"
                + "   ofs000f:\n"
                + "            label\n"
                + "            jump ofs001d\n"
                + "   ofs0014:\n"
                + "            label\n"
                + "            getlocal2\n"
                + "            pushstring \"C\"\n"
                + "            ifne ofs001d\n"
                + "   ofs001c:\n"
                + "            label\n" //this label critical to be removed
                + "   ofs001d:\n"
                + "            returnvoid\n"
                + "            pushtrue\n"
                + "            iftrue ofs0023\n"
                + "   ofs0023:\n"
                + "            label\n"
                + "            getlocal0\n"
                + "            getlocal1\n"
                + "            callpropvoid QName(PackageNamespace(\"\"),\"trace\"), 1\n"
                + "            jump ofs002d\n"
                + "   ofs002d:\n"
                + "            label\n"
                + "            jump ofs000f\n"
                + "            getlocal0\n"
                + "            getproperty QName(PackageNamespace(\"\"),\"trace\")\n"
                + "            getlocal1\n"
                + "            callpropvoid QName(Namespace(\"http://adobe.com/AS3/2006/builtin\"),\"push\"), 1\n"
                + "            jump ofs003d\n"
                + "   ofs003d:\n"
                + "            label\n"
                + "            getlocal 4\n"
                + "            increment\n"
                + "            convert_u\n"
                + "            setlocal 4\n"
                + "            jump ofs0048\n"
                + "   ofs0048:\n"
                + "            label\n"
                + "            jump ofs002d\n"
                + "            label\n"
                + "            getlocal1\n"
                + "            getlocal0\n"
                + "            getproperty QName(PackageNamespace(\"\"),\"xxx\")\n"
                + "            getlocal 4\n"
                + "            getproperty QName(PackageNamespace(\"\"),\"xxx\")\n"
                + "            ifne ofs005c\n"
                + "   ofs005c:\n"
                + "            pushtrue\n"
                + "            setlocal3\n"
                + "            jump ofs0062\n"
                + "   ofs0062:\n"
                + "            label\n"
                + "            jump ofs001c\n"
                + "   ofs0067:\n"
                + "            label\n"
                + "            getlocal2\n"
                + "            pushstring \"B\"\n"
                + "            ifne ofs0014\n"
                + "            pushbyte 0\n"
                + "            convert_u\n"
                + "            setlocal 4\n"
                + "            jump ofs0048\n"
                + "   ofs0078:\n"
                + "            label\n"
                + "            findproperty QName(PackageNamespace(\"\"),\"trace\")\n"
                + "            getlocal1\n"
                + "            callpropvoid QName(PackageNamespace(\"\"),\"trace\"), 1\n"
                + "            jump ofs0062\n"
                + "   ofs0083:\n"
                + "            label\n"
                + "            getlocal3\n"
                + "            pushfalse\n"
                + "            ifne ofs0062\n"
                + "            jump ofs0078\n"
                + "            label\n"
                + "            getlocal 4\n"
                + "            increment\n"
                + "            convert_u\n"
                + "            setlocal 4\n"
                + "            jump ofs0099\n"
                + "   ofs0099:\n"
                + "            label\n"
                + "            jump ofs0083\n"
                + "            pushtrue\n"
                + "            setlocal3\n"
                + "            jump ofs00a4\n"
                + "   ofs00a4:\n"
                + "            getlocal2\n"
                + "            pushstring \"A\"\n"
                + "            ifne ofs0067\n"
                + "            pushbyte 0\n"
                + "            convert_u\n"
                + "            setlocal 4\n"
                + "            jump ofs0099\n", "         var _loc2_:* = param1;\n"
                + "         var _loc4_:uint = 0;\n"
                + "         if(_loc2_ == \"A\")\n"
                + "         {\n"
                + "            _loc4_ = 0;\n"
                + "            trace(param1);\n"
                + "         }\n"
                + "         else if(_loc2_ == \"B\")\n"
                + "         {\n"
                + "            _loc4_ = 0;\n"
                + "         }\n"
                + "         else if(_loc2_ == \"C\")\n"
                + "         {\n"
                + "         }\n");
    }

    @Test
    public void testWhileTrue() throws Exception {
        decompilePCode(
                "            getlex QName(PackageNamespace(\"\"),\"Math\")\n"
                + "            getlex QName(PackageNamespace(\"\"),\"Math\")\n"
                + "            debugline 8\n"
                + "            callproperty QName(PackageNamespace(\"\"),\"random\"), 0\n"
                + "            pushbyte 6\n"
                + "            multiply\n"
                + "            callproperty QName(PackageNamespace(\"\"),\"floor\"), 1\n"
                + "            convert_i\n"
                + "            setlocal1\n"
                + "            getlocal1\n"
                + "            debugline 10\n"
                + "            pushbyte 4\n"
                + "            ifngt ofs0034\n"
                + "            jump ofs0030\n"
                + "   ofs002d:\n"
                + "            label\n"
                + "            debugline 11\n"
                + "   ofs0030:\n"
                + "            jump ofs002d\n"
                + "   ofs0034:\n"
                + "            ", "         param1 = Math.floor(Math.random() * 6);\n"
                + "         if(param1 <= 4)\n"
                + "         {\n"
                + "            return;\n"
                + "         }\n"
                + "         while(true)\n"
                + "         {\n"
                + "         }\n");

    }

    @Test
    public void testObfuscatedSwitch() throws Exception {
        decompilePCode("getlocal0\n"
                + "            pushbyte 0\n"
                + "            initproperty QName(PackageNamespace(\"\"),\"testA\")\n"
                + "            jump ofs0012\n"
                + "            call 7\n"
                + "            throw\n"
                + "            callmethod 1413, 7\n"
                + "   ofs0012:\n"
                + "            jump ofs001a\n"
                + "            ifge ofs001a\n"
                + "   ofs001a:\n"
                + "            jump ofs0024\n"
                + "            equals\n"
                + "            pushwith\n"
                + "            newfunction 30\n"
                + "            pop\n"
                + "            setlocal2\n"
                + "   ofs0024:\n"
                + "            jump ofs0132\n"
                + "            pushwith\n"
                + "            setlocal3\n"
                + "   ofs002a:\n"
                + "            label\n"
                + "            pushtrue\n"
                + "            iftrue ofs0036\n"
                + "            returnvoid\n"
                + "            popscope\n"
                + "            callsuper QName(PackageNamespace(\"aaa\"),\"xxx\"), 10\n"
                + "            pushwith\n"
                + "   ofs0036:\n"
                + "            getlocal0\n"
                + "            callpropvoid QName(PackageNamespace(\"\"),\"test\"), 0\n"
                + "            jump ofs01ff\n"
                + "            pushwith\n"
                + "            setlocal0\n"
                + "   ofs0040:\n"
                + "            label\n"
                + "            getlocal0\n"
                + "            callpropvoid QName(PackageNamespace(\"\"),\"test\"), 0\n"
                + "            getlocal0\n"
                + "            pushtrue\n"
                + "            iftrue ofs0052\n"
                + "            returnvoid\n"
                + "            popscope\n"
                + "            pushwith\n"
                + "            newfunction 48\n"
                + "            pop\n"
                + "            divide\n"
                + "   ofs0052:\n"
                + "            callpropvoid QName(PackageNamespace(\"\"),\"test\"), 0\n"
                + "            getlocal0\n"
                + "            callpropvoid QName(PackageNamespace(\"\"),\"test\"), 0\n"
                + "            jump ofs01ff\n"
                + "            ifgt ofs0061\n"
                + "   ofs0061:\n"
                + "            label\n"
                + "            jump ofs006a\n"
                + "            ifstricteq ofs006a\n"
                + "   ofs006a:\n"
                + "            getlocal0\n"
                + "            callpropvoid QName(PackageNamespace(\"\"),\"test\"), 0\n"
                + "            jump ofs0078\n"
                + "            popscope\n"
                + "            pushwith\n"
                + "            newfunction 4\n"
                + "            throw\n"
                + "            checkfilter\n"
                + "   ofs0078:\n"
                + "            getlocal0\n"
                + "            callpropvoid QName(PackageNamespace(\"\"),\"test\"), 0\n"
                + "            jump ofs01ff\n"
                + "            pushwith\n"
                + "            setlocal0\n"
                + "            getlocal3\n"
                + "   ofs0083:\n"
                + "            label\n"
                + "            getlocal0\n"
                + "            callpropvoid QName(PackageNamespace(\"\"),\"test\"), 0\n"
                + "            pushfalse\n"
                + "            iffalse ofs0094\n"
                + "            returnvoid\n"
                + "            popscope\n"
                + "            equals\n"
                + "            setlocal1\n"
                + "            newactivation\n"
                + "            increment\n"
                + "            decrement\n"
                + "   ofs0094:\n"
                + "            jump ofs01ff\n"
                + "            pushwith\n"
                + "            setlocal2\n"
                + "   ofs009a:\n"
                + "            label\n"
                + "            getlocal0\n"
                + "            callpropvoid QName(PackageNamespace(\"\"),\"test\"), 0\n"
                + "            getlocal0\n"
                + "            jump ofs00a9\n"
                + "            modulo\n"
                + "            callsupervoid QName(PackageInternalNs(\"xxxx\"),\"tmp\"), 5\n"
                + "            pushwith\n"
                + "   ofs00a9:\n"
                + "            callpropvoid QName(PackageNamespace(\"\"),\"test\"), 0\n"
                + "            jump ofs00b4\n"
                + "            ifge ofs00b4\n"
                + "   ofs00b4:\n"
                + "            getlocal0\n"
                + "            callpropvoid QName(PackageNamespace(\"\"),\"test\"), 0\n"
                + "            jump ofs00c1\n"
                + "            popscope\n"
                + "            callproplex QName(PackageNamespace(\"\"),\"xxx\"), 18\n"
                + "            setlocal2\n"
                + "   ofs00c1:\n"
                + "            jump ofs01ff\n"
                + "            ifstrictne ofs00c9\n"
                + "   ofs00c9:\n"
                + "            label\n"
                + "            getlocal0\n"
                + "            callpropvoid QName(PackageNamespace(\"\"),\"test\"), 0\n"
                + "            jump ofs00d8\n"
                + "            nextname\n"
                + "            dxns \"position\"\n"
                + "            pushwith\n"
                + "            setlocal2\n"
                + "            returnvalue\n"
                + "   ofs00d8:\n"
                + "            jump ofs01ff\n"
                + "            pushwith\n"
                + "            setlocal3\n"
                + "            getlocal1\n"
                + "   ofs00df:\n"
                + "            label\n"
                + "            getlocal0\n"
                + "            callpropvoid QName(PackageNamespace(\"\"),\"test\"), 0\n"
                + "            getlocal0\n"
                + "            jump ofs00ef\n"
                + "            popscope\n"
                + "            pushwith\n"
                + "            newfunction 24\n"
                + "            pop\n"
                + "            setlocal3\n"
                + "   ofs00ef:\n"
                + "            callpropvoid QName(PackageNamespace(\"\"),\"test\"), 0\n"
                + "            jump ofs00fa\n"
                + "            ifne ofs00fa\n"
                + "   ofs00fa:\n"
                + "            getlocal0\n"
                + "            callpropvoid QName(PackageNamespace(\"\"),\"test\"), 0\n"
                + "            jump ofs0107\n"
                + "            popscope\n"
                + "            callproplex TypeName(QName(PackageNamespace(\"__AS3__.vec\"),\"Vector\")<QName(PackageNamespace(\"xxx\"),\"aaa\")>), 15\n"
                + "            throw\n"
                + "   ofs0107:\n"
                + "            getlocal0\n"
                + "            callpropvoid QName(PackageNamespace(\"\"),\"test\"), 0\n"
                + "            pushtrue\n"
                + "            iftrue ofs0117\n"
                + "            returnvoid\n"
                + "            checkfilter\n"
                + "            pushwith\n"
                + "            newfunction 51\n"
                + "            throw\n"
                + "            instanceof\n"
                + "   ofs0117:\n"
                + "            getlocal0\n"
                + "            callpropvoid QName(PackageNamespace(\"\"),\"test\"), 0\n"
                + "            pushfalse\n"
                + "            iffalse ofs0127\n"
                + "            returnvoid\n"
                + "            popscope\n"
                + "            setlocal1\n"
                + "            istypelate\n"
                + "            setlocal1\n"
                + "            setlocal2\n"
                + "            divide\n"
                + "   ofs0127:\n"
                + "            jump ofs01ff\n"
                + "            pushwith\n"
                + "            setlocal2\n"
                + "            getlocal1\n"
                + "            jump ofs0132\n"
                + "   ofs0132:\n"
                + "            getlex QName(PackageNamespace(\"\"),\"Abc\")\n"
                + "            getproperty QName(PackageNamespace(\"\"),\"value\")\n"
                + "            setlocal1\n"
                + "            jump ofs013f\n"
                + "            ifle ofs013f\n"
                + "   ofs013f:\n"
                + "            pushbyte 1\n"
                + "            getlocal1\n"
                + "            ifstricteq ofs014a\n"
                + "            jump ofs015e\n"
                + "   ofs014a:\n"
                + "            pushbyte 0\n"
                + "            jump ofs0156\n"
                + "            pushscope\n"
                + "            setlocal0\n"
                + "            istypelate\n"
                + "            nextvalue\n"
                + "            istypelate\n"
                + "            decrement\n"
                + "   ofs0156:\n"
                + "            jump ofs01e3\n"
                + "            ifgt ofs015e\n"
                + "   ofs015e:\n"
                + "            pushbyte 2\n"
                + "            getlocal1\n"
                + "            ifstricteq ofs0169\n"
                + "            jump ofs0178\n"
                + "   ofs0169:\n"
                + "            pushbyte 1\n"
                + "            jump ofs01e3\n"
                + "            jump ofs0173\n"
                + "   ofs0173:\n"
                + "            dup\n"
                + "            callsuper QName(PackageNamespace(\"xxx\"),\"aaa\"), 27\n"
                + "            pop\n"
                + "   ofs0178:\n"
                + "            pushbyte 3\n"
                + "            getlocal1\n"
                + "            ifstricteq ofs0183\n"
                + "            jump ofs018b\n"
                + "   ofs0183:\n"
                + "            pushbyte 2\n"
                + "            jump ofs01e3\n"
                + "            pushwith\n"
                + "            setlocal0\n"
                + "   ofs018b:\n"
                + "            pushbyte 4\n"
                + "            getlocal1\n"
                + "            ifstrictne ofs01a2\n"
                + "            pushbyte 3\n"
                + "            jump ofs01e3\n"
                + "            jump ofs019c\n"
                + "   ofs019c:\n"
                + "            convert_s\n"
                + "            construct 110\n"
                + "            pushwith\n"
                + "            nextvalue\n"
                + "            setlocal2\n"
                + "   ofs01a2:\n"
                + "            pushbyte 5\n"
                + "            getlocal1\n"
                + "            ifstricteq ofs01ad\n"
                + "            jump ofs01b5\n"
                + "   ofs01ad:\n"
                + "            pushbyte 4\n"
                + "            jump ofs01e3\n"
                + "            pushwith\n"
                + "            setlocal2\n"
                + "   ofs01b5:\n"
                + "            pushbyte 6\n"
                + "            jump ofs01bf\n"
                + "            ifgt ofs01bf\n"
                + "   ofs01bf:\n"
                + "            getlocal1\n"
                + "            ifstrictne ofs01d4\n"
                + "            pushbyte 5\n"
                + "            jump ofs01e3\n"
                + "            jump ofs01ce\n"
                + "   ofs01ce:\n"
                + "            popscope\n"
                + "            pushwith\n"
                + "            newfunction 49\n"
                + "            pop\n"
                + "            checkfilter\n"
                + "   ofs01d4:\n"
                + "            jump ofs01e1\n"
                + "            throw\n"
                + "            setlocal3\n"
                + "            getlocal2\n"
                + "            pushbyte 6\n"
                + "            jump ofs01e1\n"
                + "   ofs01e1:\n"
                + "            pushbyte 6\n"
                + "   ofs01e3:\n"
                + "            kill 1\n"
                + "            lookupswitch ofs00df, [ofs002a, ofs0040, ofs0061, ofs0083, ofs009a, ofs00c9, ofs00df]\n"
                + "   ofs01ff:\n"
                + "            getlocal0\n"
                + "            pushbyte 0\n"
                + "            initproperty QName(PackageNamespace(\"\"),\"testX\")\n"
                + "            jump ofs020e\n"
                + "            popscope\n"
                + "            pushwith\n"
                + "            setlocal1\n"
                + "            bitxor\n"
                + "            convert_d\n"
                + "            setlocal2\n"
                + "   ofs020e:\n"
                + "            pushfalse\n"
                + "            iffalse ofs021a\n"
                + "            returnvoid\n"
                + "            typeof\n"
                + "            checkfilter\n"
                + "            bitand\n"
                + "            in\n"
                + "            convert_s\n"
                + "            nextvalue\n"
                + "   ofs021a:\n"
                + "            jump ofs0222\n"
                + "            ifgt ofs0222\n"
                + "   ofs0222:\n"
                + "            pushtrue\n"
                + "            iftrue ofs022d\n"
                + "            returnvoid\n"
                + "            setlocal2\n"
                + "            callsuper QName(PackageNamespace(\"xxx\"),\"aaa\"), 15\n"
                + "            throw\n"
                + "   ofs022d:", "         this.testA = 0;\n"
                + "         switch(Abc.value)\n"
                + "         {\n"
                + "            case 1:\n"
                + "               this.test();\n"
                + "               break;\n"
                + "            case 2:\n"
                + "               this.test();\n"
                + "               this.test();\n"
                + "               this.test();\n"
                + "               break;\n"
                + "            case 3:\n"
                + "               this.test();\n"
                + "               this.test();\n"
                + "               break;\n"
                + "            case 4:\n"
                + "               this.test();\n"
                + "               break;\n"
                + "            case 5:\n"
                + "               this.test();\n"
                + "               this.test();\n"
                + "               this.test();\n"
                + "               break;\n"
                + "            case 6:\n"
                + "               this.test();\n"
                + "               break;\n"
                + "            default:\n"
                + "               this.test();\n"
                + "               this.test();\n"
                + "               this.test();\n"
                + "               this.test();\n"
                + "               this.test();\n"
                + "         }\n"
                + "         this.testX = 0;\n");
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
    public void testEvalExpressionAfterWhile() throws Exception {
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
            fail("reachable if onTrue removed");
        }
    }
}
