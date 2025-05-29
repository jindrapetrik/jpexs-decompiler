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
package com.jpexs.decompiler.flash.simpleparser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public interface SimpleParser {

    /**
     * Parses document.
     *
     * @param str The string to convert
     * @param definitionPosToReferences Definition position to references
     * @param referenceToDefinition Reference to definition
     * @param errors Errors
     * @throws SimpleParseException On parse error
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public void parse(
            String str,
            Map<Integer, List<Integer>> definitionPosToReferences,
            Map<Integer, Integer> referenceToDefinition,
            List<SimpleParseException> errors
    ) throws SimpleParseException, IOException, InterruptedException;

    public static void parseVariablesList(
            List<VariableOrScope> privateVariables,
            List<VariableOrScope> sharedVariables,
            Map<Integer, List<Integer>> definitionPosToReferences,
            Map<Integer, Integer> referenceToDefinition,
            Map<String, Integer> parentVarNameToDefinitionPosition
    ) {
        Map<String, Integer> privateVarNameToDefinitionPosition = new LinkedHashMap<>();
        privateVarNameToDefinitionPosition.putAll(parentVarNameToDefinitionPosition);

        for (VariableOrScope vt : privateVariables) {
            if (vt instanceof Variable) {
                Variable v = (Variable) vt;
                if (v.definition) {
                    privateVarNameToDefinitionPosition.put(v.name, v.position);
                    definitionPosToReferences.put(v.position, new ArrayList<>());
                } else {
                    if (!privateVarNameToDefinitionPosition.containsKey(v.name)) {
                        parentVarNameToDefinitionPosition.put(v.name, -v.position - 1);
                        privateVarNameToDefinitionPosition.put(v.name, -v.position - 1);
                        definitionPosToReferences.put(-v.position - 1, new ArrayList<>());
                        definitionPosToReferences.get(-v.position - 1).add(v.position);
                        referenceToDefinition.put(v.position, -v.position - 1);
                    } else {
                        int definitionPos = privateVarNameToDefinitionPosition.get(v.name);
                        definitionPosToReferences.get(definitionPos).add(v.position);
                        referenceToDefinition.put(v.position, definitionPos);
                    }
                }
            }
            if (vt instanceof Scope) {
                Scope vs = (Scope) vt;
                parseVariablesList(vs.getPrivateItems(), vs.getSharedItems(), definitionPosToReferences, referenceToDefinition, privateVarNameToDefinitionPosition);
            }
        }
        for (VariableOrScope vt : sharedVariables) {
            if (vt instanceof Variable) {
                Variable v = (Variable) vt;
                if (v.definition) {
                    parentVarNameToDefinitionPosition.put(v.name, v.position);
                    privateVarNameToDefinitionPosition.put(v.name, v.position);
                    definitionPosToReferences.put(v.position, new ArrayList<>());
                } else {
                    if (!privateVarNameToDefinitionPosition.containsKey(v.name)) {
                        parentVarNameToDefinitionPosition.put(v.name, -v.position - 1);
                        privateVarNameToDefinitionPosition.put(v.name, -v.position - 1);
                        definitionPosToReferences.put(-v.position - 1, new ArrayList<>());
                        definitionPosToReferences.get(-v.position - 1).add(v.position);
                        referenceToDefinition.put(v.position, -v.position - 1);
                    } else {
                        int definitionPos = privateVarNameToDefinitionPosition.get(v.name);
                        definitionPosToReferences.get(definitionPos).add(v.position);
                        referenceToDefinition.put(v.position, definitionPos);
                    }
                }
            }
            if (vt instanceof Scope) {
                Scope vs = (Scope) vt;
                parseVariablesList(vs.getPrivateItems(), vs.getSharedItems(), definitionPosToReferences, referenceToDefinition, privateVarNameToDefinitionPosition);
            }
        }
    }
}
