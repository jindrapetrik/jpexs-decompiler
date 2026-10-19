/*
 * Copyright (C) 2010-2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.ConstantPool;
import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.helpers.utf8.Utf8Helper;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.Collections;
import java.util.LinkedHashSet;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ActionScript2StaticMembersTest extends ActionScript2TestBase {

    @BeforeClass
    public void init() {
        Configuration.autoDeobfuscate.set(false);
        Configuration.simplifyExpressions.set(false);
        Configuration.decompile.set(true);
        Configuration.registerNameFormat.set("_loc%d_");
    }

    private DoActionTag compile(String source) throws Exception {
        SWF swf = new SWF();
        DoActionTag tag = new DoActionTag(swf);
        tag.setActions(new ActionScript2Parser(swf, tag).actionsFromString(source, Utf8Helper.charsetName));
        return tag;
    }

    private String decompile(String source) throws Exception {
        SWF swf = new SWF();
        DoInitActionTag tag = new DoInitActionTag(swf);
        tag.setActions(new ActionScript2Parser(swf, tag).actionsFromString(source, Utf8Helper.charsetName));
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        Graph.graphToString(Action.actionsToTree(new LinkedHashSet<>(), false, Collections.emptyMap(), true, false,
                tag.getActions(), swf.version, 0, "", Utf8Helper.charsetName), writer,
                LocalData.create(new ConstantPool(Collections.emptyList()), swf, new LinkedHashSet<>()));
        writer.finishHilights();
        return cleanPCode(writer.toString());
    }

    private void assertSameCompilation(String implicit, String explicit) throws Exception {
        Assert.assertEquals(compile(implicit).getActionBytes().getRangeData(), compile(explicit).getActionBytes().getRangeData());
    }

    @Test
    public void testUsers() throws Exception {
        String source = "class Users { private static var numInstances:Number = 0; "
                + "function Users() { numInstances++; } "
                + "static function get instances():Number { return numInstances; } }";
        assertSameCompilation(source, source.replace("numInstances++;", "Users.numInstances++;")
                .replace("return numInstances;", "return Users.numInstances;"));
        Assert.assertEquals(decompile(source), cleanPCode("class Users\n{\nstatic var numInstances = 0;\n"
                + "function Users()\n{\nnumInstances++;\n}\n"
                + "static function get instances()\n{\nreturn numInstances;\n}\n}"));
    }

    @Test
    public void testReturnTypesAreIgnored() throws Exception {
        assertSameCompilation("function f(x:Number):some.package.Result { return x; } "
                + "var g = function():Void {};", "function f(x) { return x; } var g = function() {};");
    }

    @Test
    public void testPackagedClassAndWrites() throws Exception {
        String source = "class example.Users { static var count = 0; "
                + "function Users() { count = 2; ++count; count--; count += 3; } "
                + "static function read() { return count; } }";
        assertSameCompilation(source, source.replace("count = 2", "example.Users.count = 2")
                .replace("++count", "++example.Users.count").replace("count--", "example.Users.count--")
                .replace("count += 3", "example.Users.count += 3").replace("return count", "return example.Users.count"));
        String result = decompile(source);
        Assert.assertTrue(result.contains("count = 2;"), result);
        Assert.assertTrue(result.contains("return count;"), result);
        Assert.assertFalse(result.contains("example.Users.count"), result);
    }

    @Test
    public void testShadowingAndNestedFunctions() throws Exception {
        String source = "class Users { static var count = 0; "
                + "function Users() {} "
                + "function parameter(count) { return count + Users.count; } "
                + "function local() { var count = 3; return count + Users.count; } "
                + "function closure(count) { return function() { return count + Users.count; }; } "
                + "function nested() { return function() { return count; }; } }";
        assertSameCompilation(source, source.replace("return count;", "return Users.count;"));
        String result = decompile(source);
        Assert.assertTrue(result.contains("count + Users.count"), result);
        Assert.assertTrue(result.contains("var count = 3;\nreturn count + Users.count;"), result);
        Assert.assertTrue(result.contains("return count;"), result);
    }

    @Test
    public void testStaticInitializerAndLaterDeclaration() throws Exception {
        String source = "class Users { function Users() { count++; } "
                + "static var count = 0; static var next = count + 1; }";
        assertSameCompilation(source, source.replace("count++;", "Users.count++;")
                .replace("next = count", "next = Users.count"));
        Assert.assertTrue(decompile(source).contains("static var next = count + 1;"));
    }

    @Test
    public void testStaticCallsAndConstructors() throws Exception {
        String source = "class Users { static var factory = Object; "
                + "function Users() { factory(); new factory(); delete factory; } }";
        assertSameCompilation(source, source.replace("factory();", "Users.factory();")
                .replace("delete factory", "delete Users.factory"));
        String result = decompile(source);
        Assert.assertTrue(result.contains("factory();"), result);
        Assert.assertTrue(result.contains("new factory();"), result);
        Assert.assertTrue(result.contains("delete factory;"), result);
    }

    @Test
    public void testGetterAndSetterReferences() throws Exception {
        String source = "class Users { static var count = 0; function Users() { instances = instances + 1; } "
                + "static function get instances():Number { return count; } "
                + "static function set instances(value:Number):Void { count = value; } }";
        assertSameCompilation(source, source.replace("instances = instances", "Users.instances = Users.instances")
                .replace("return count", "return Users.count").replace("count = value", "Users.count = value"));
        String result = decompile(source);
        Assert.assertFalse(result.contains("Users.instances"), result);
        Assert.assertFalse(result.contains("Users.count"), result);
        Assert.assertTrue(result.contains("instances += 1;"), result);
    }

    @Test
    public void testUninitializedStaticAndHoistedLocal() throws Exception {
        String source = "class Users { static var count:Number; function Users() { count = 1; } "
                + "function local() { trace(count); var count = 3; return count; } }";
        assertSameCompilation(source, source.replace("count = 1", "Users.count = 1"));
    }

    @Test
    public void testCatchShadowing() throws Exception {
        String source = "class Users { static var count = 0; function Users() { "
                + "try { count++; } catch(count) { trace(count); trace(Users.count); } return count; } }";
        assertSameCompilation(source, source.replace("count++;", "Users.count++;")
                .replace("return count;", "return Users.count;"));
        String result = decompile(source);
        Assert.assertTrue(result.contains("trace(Users.count);"), result);
        Assert.assertTrue(result.contains("return count;"), result);
    }

    @Test
    public void testOtherObjectsAndWithScopeKeepQualifiers() throws Exception {
        String result = decompile("class Users { static var count = 0; function Users() { "
                + "trace(Other.count); trace(this.count); with(other) { trace(Users.count); } trace(Users.count); } "
                + "function shadow(Users) { return Users.count; } }");
        Assert.assertTrue(result.contains("trace(Other.count);"), result);
        Assert.assertTrue(result.contains("trace(this.count);"), result);
        Assert.assertTrue(result.contains("trace(Users.count);"), result);
        Assert.assertTrue(result.contains("trace(count);"), result);
        Assert.assertTrue(result.contains("return Users.count;"), result);
    }
}
