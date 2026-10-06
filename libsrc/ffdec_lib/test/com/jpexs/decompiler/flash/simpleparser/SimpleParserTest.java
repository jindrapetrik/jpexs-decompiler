/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3.0 of the License, or (at your option) any later version.
 */
package com.jpexs.decompiler.flash.simpleparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Tests for shared simple-parser symbol resolution.
 */
public class SimpleParserTest {

    @Test
    public void importedQualifiedFunctionDoesNotCollideWithClassMethod() {
        int importPosition = 25;
        int methodPosition = 100;
        int localReferencePosition = 150;
        int importedReferencePosition = 200;

        Path importedFunction = new Path("flash", "utils", "getTimer");
        Path className = new Path("tests_classes", "TestImports3");

        List<VariableOrScope> classItems = new ArrayList<>();
        classItems.add(new ClassTrait(className, new Path("getTimer"), null, methodPosition,
                true, new Path("Function"), new Path("Number"), null, null));
        classItems.add(new Variable(false, new Path("getTimer"), localReferencePosition));
        classItems.add(new Variable(false, importedFunction, importedReferencePosition));

        List<VariableOrScope> variables = new ArrayList<>();
        variables.add(new Import(importedFunction, new Path("getTimer"), importPosition));
        variables.add(new ClassScope(50, 250, classItems));

        Map<Integer, List<Integer>> definitionPosToReferences = new LinkedHashMap<>();
        Map<Integer, Integer> referenceToDefinition = new LinkedHashMap<>();
        List<Path> externalTypes = new ArrayList<>();
        externalTypes.add(importedFunction);
        Map<Integer, Integer> referenceToExternalTypeIndex = new LinkedHashMap<>();
        Map<Integer, List<Integer>> externalTypeIndexToReference = new LinkedHashMap<>();

        SimpleParser.parseVariablesList(
                variables,
                definitionPosToReferences,
                referenceToDefinition,
                new ArrayList<>(),
                true,
                externalTypes,
                referenceToExternalTypeIndex,
                externalTypeIndexToReference,
                null,
                new LinkedHashMap<>(),
                new LinkedHashMap<>(),
                new LinkedHashMap<>(),
                new LinkedHashMap<>(),
                new LinkedHashMap<>(),
                new LinkedHashMap<>(),
                new LinkedHashMap<>(),
                null,
                new ArrayList<>()
        );

        assertFalse(referenceToDefinition.containsKey(importPosition));
        assertEquals(referenceToDefinition.get(localReferencePosition), Integer.valueOf(methodPosition));
        assertFalse(definitionPosToReferences.get(methodPosition).contains(importPosition));

        assertEquals(referenceToExternalTypeIndex.get(importPosition), Integer.valueOf(0));
        assertEquals(referenceToExternalTypeIndex.get(importedReferencePosition), Integer.valueOf(0));
        assertTrue(externalTypeIndexToReference.get(0).containsAll(
                Arrays.asList(importPosition, importedReferencePosition)));
    }
}
