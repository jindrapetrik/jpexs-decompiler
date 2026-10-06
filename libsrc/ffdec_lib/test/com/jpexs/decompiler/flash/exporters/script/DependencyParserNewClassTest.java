/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.exporters.script;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Regression coverage for filtering class-parent dependencies. */
public class DependencyParserNewClassTest {

    private boolean previousDeobfuscation;

    @BeforeMethod
    public void configure() {
        previousDeobfuscation = Configuration.autoDeobfuscate.get();
        Configuration.autoDeobfuscate.set(false);
    }

    @AfterMethod(alwaysRun = true)
    public void restoreConfiguration() {
        Configuration.autoDeobfuscate.set(previousDeobfuscation);
    }

    private static class Fixture {
        final SWF swf = new SWF();
        final ABC abc = new DoABC2Tag(swf).getABC();

        void addMethod(String... instructions) {
            MethodBody body = new MethodBody();
            body.method_info = abc.method_info.size();
            AVM2Code code = new AVM2Code();
            for (String instruction : instructions) {
                String[] tokens = instruction.split(" ");
                if (tokens[0].equals("newclass")) {
                    code.code.add(new AVM2Instruction(0, 0x58, new int[]{0}));
                } else if (tokens[0].equals("newfunction")) {
                    code.code.add(new AVM2Instruction(0, 0x40, new int[]{Integer.parseInt(tokens[1])}));
                } else if (tokens[0].equals("nop")) {
                    code.code.add(new AVM2Instruction(0, 0x02, null));
                } else {
                    int name = abc.constants.addMultiname(Multiname.createQName(false,
                            abc.constants.getStringId(tokens[1], true),
                            abc.constants.getNamespaceId(Namespace.KIND_PACKAGE, "sample", 0, true)));
                    code.code.add(new AVM2Instruction(0, tokens[0].equals("getlex") ? 0x60 : 0x66, new int[]{name}));
                }
            }
            body.setCode(code);
            abc.addMethodInfo(new MethodInfo(new int[0], 0, 0, 0, new ValueKind[0], new int[0]));
            abc.addMethodBody(body);
        }

        void assertImports(int classIndex, String... names) throws InterruptedException {
            List<Dependency> dependencies = new ArrayList<>();
            DependencyParser.parseDependenciesFromMethodInfo(new LinkedHashSet<>(), swf.getAbcIndex(),
                    null, 0, classIndex, true, null, abc, 0, dependencies, null,
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new Reference<>(0));
            List<String> actual = new ArrayList<>();
            for (Dependency dependency : dependencies) {
                actual.add(dependency.getId().toRawString());
                Assert.assertEquals(dependency.getType(), DependencyType.EXPRESSION);
            }
            Assert.assertEquals(actual, Arrays.asList(names));
        }
    }

    @Test
    public void testNoNewClass() throws Exception {
        Fixture fixture = new Fixture();
        fixture.addMethod("getlex A", "nop", "getlex B", "getlex A");
        fixture.assertImports(0, "sample.A", "sample.B");
        fixture.assertImports(-1, "sample.A", "sample.B");
    }

    @Test
    public void testMultipleClassesAndInterveningInstructions() throws Exception {
        Fixture fixture = new Fixture();
        fixture.addMethod("getlex Parent1", "nop", "newclass", "getlex Parent2",
                "getproperty Used", "nop", "newclass", "getlex After");
        fixture.assertImports(0, "sample.Used", "sample.After");
        fixture.assertImports(-1, "sample.Used", "sample.After");
    }

    @Test
    public void testScriptInitializerPrefix() throws Exception {
        Fixture fixture = new Fixture();
        fixture.addMethod("getproperty Prefix", "newclass", "getlex After");
        fixture.assertImports(0, "sample.Prefix", "sample.After");
        fixture.assertImports(-1, "sample.After");
    }

    @Test
    public void testNestedFunctionHasItsOwnClassBoundary() throws Exception {
        Fixture fixture = new Fixture();
        fixture.addMethod("newfunction 1", "newclass", "getlex After");
        fixture.addMethod("getlex Nested");
        fixture.assertImports(0, "sample.Nested", "sample.After");
    }

    @Test
    public void testEmptyAndTrailingClass() throws Exception {
        Fixture empty = new Fixture();
        empty.addMethod();
        empty.assertImports(-1);
        Fixture trailing = new Fixture();
        trailing.addMethod("getlex Parent", "newclass");
        trailing.assertImports(0);
        trailing.assertImports(-1);
    }
}
