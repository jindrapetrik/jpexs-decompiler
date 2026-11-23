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
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Checks namespace dependencies in code linked against an external library.
 */
public class DependencyParserNamespaceTest {

    @Test
    public void testExternalConstructorAndInterfaceUseOnlyRealNamespaceAliases() throws InterruptedException {
        SWF swf = new SWF();
        ABC abc = new DoABC2Tag(swf).getABC();
        String pkg = "library.widgets";
        String interfaceNamespace = pkg + ":IWidget";
        String customNamespace = "https://example.org/widget";
        abc.constants.getQnameId("Widget", Namespace.KIND_PACKAGE, pkg, true);
        int constructor = abc.constants.getQnameId("Widget", Namespace.KIND_NAMESPACE, pkg, true);
        int interfaceMethod = abc.constants.getQnameId("refresh", Namespace.KIND_NAMESPACE, interfaceNamespace, true);
        int customProperty = abc.constants.getQnameId("data", Namespace.KIND_NAMESPACE, customNamespace, true);

        MethodBody body = new MethodBody();
        body.setCode(new AVM2Code(new ArrayList<>(Arrays.asList(
                new AVM2Instruction(0, AVM2Instructions.FindPropertyStrict, new int[]{constructor}),
                new AVM2Instruction(0, AVM2Instructions.ConstructProp, new int[]{constructor, 0}),
                new AVM2Instruction(0, AVM2Instructions.Dup, null),
                new AVM2Instruction(0, AVM2Instructions.CallPropVoid, new int[]{interfaceMethod, 0}),
                new AVM2Instruction(0, AVM2Instructions.GetProperty, new int[]{customProperty}),
                new AVM2Instruction(0, AVM2Instructions.Pop, null),
                new AVM2Instruction(0, AVM2Instructions.ReturnVoid, null)))));
        abc.addMethodInfo(new MethodInfo(new int[0], 0, 0, 0, new ValueKind[0], new int[0]));
        abc.addMethodBody(body);

        List<Dependency> dependencies = new ArrayList<>();
        NamespaceUses uses = new NamespaceUses(new ArrayList<>(), true);
        boolean previousDeobfuscation = Configuration.autoDeobfuscate.get();
        Configuration.autoDeobfuscate.set(false);
        try {
            DependencyParser.parseDependenciesFromMethodInfo(new LinkedHashSet<>(), swf.getAbcIndex(),
                    null, -1, -1, true, null, abc, 0, dependencies, null,
                    new ArrayList<>(), new ArrayList<>(), uses, new Reference<>(null));
        } finally {
            Configuration.autoDeobfuscate.set(previousDeobfuscation);
        }
        Assert.assertEquals(dependencies, Collections.singletonList(
                new Dependency(DottedChain.parseNoSuffix("library.widgets.Widget"), DependencyType.EXPRESSION)));
        Assert.assertEquals(uses.undeclared, Collections.singletonMap(customNamespace, false));
    }
}
