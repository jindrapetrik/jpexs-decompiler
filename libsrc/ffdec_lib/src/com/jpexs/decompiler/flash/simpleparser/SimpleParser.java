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
            List<SimpleParseException> errors
    ) {
        parseVariablesList(privateVariables, sharedVariables, definitionPosToReferences, referenceToDefinition, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), true, errors);
    }

    public static void parseVariablesList(
            List<VariableOrScope> privateVariables,
            List<VariableOrScope> sharedVariables,
            Map<Integer, List<Integer>> definitionPosToReferences,
            Map<Integer, Integer> referenceToDefinition,
            Map<String, Integer> parentVarFullNameToDefinitionPosition,
            Map<String, Integer> parentVarNameToDefinitionPosition,
            Map<Integer, Boolean> positionToStatic,
            boolean isStatic,
            List<SimpleParseException> errors
    ) {
        Map<String, Integer> privateVarNameToDefinitionPosition = new LinkedHashMap<>();
        privateVarNameToDefinitionPosition.putAll(parentVarNameToDefinitionPosition);

        Map<String, Integer> privateVarFullNameToDefinitionPosition = new LinkedHashMap<>();
        privateVarFullNameToDefinitionPosition.putAll(parentVarFullNameToDefinitionPosition);

        for (VariableOrScope vt : privateVariables) {
            if (vt instanceof Variable) {
                Variable v = (Variable) vt;
                if (v.definition) {
                    privateVarFullNameToDefinitionPosition.put(v.name, v.position);
                    privateVarNameToDefinitionPosition.put(v.getLastName(), v.position);
                    definitionPosToReferences.put(v.position, new ArrayList<>());
                    positionToStatic.put(v.position, v.isStatic != null ? v.isStatic : isStatic);
                } else {
                    if (!privateVarFullNameToDefinitionPosition.containsKey(v.name)
                            && !privateVarNameToDefinitionPosition.containsKey(v.name)) {
                        parentVarFullNameToDefinitionPosition.put(v.name, -v.position - 1);
                        parentVarNameToDefinitionPosition.put(v.getLastName(), -v.position - 1);
                        privateVarFullNameToDefinitionPosition.put(v.name, -v.position - 1);
                        privateVarNameToDefinitionPosition.put(v.getLastName(), -v.position - 1);
                        definitionPosToReferences.put(-v.position - 1, new ArrayList<>());
                        definitionPosToReferences.get(-v.position - 1).add(v.position);
                        referenceToDefinition.put(v.position, -v.position - 1);
                    } else {

                        if ("this".equals(v.name) && isStatic) {
                            errors.add(new SimpleParseException("Cannot use this in static context", -1, v.position));
                        } else {
                            int definitionPos;
                            if (privateVarFullNameToDefinitionPosition.containsKey(v.name)) {
                                definitionPos = privateVarFullNameToDefinitionPosition.get(v.name);
                            } else {
                                definitionPos = privateVarNameToDefinitionPosition.get(v.name);
                            }
                            boolean staticDefinition = definitionPos >= 0 ? positionToStatic.get(definitionPos) : true;
                            if (!(!staticDefinition && isStatic)) {
                                definitionPosToReferences.get(definitionPos).add(v.position);
                                referenceToDefinition.put(v.position, definitionPos);
                            } else {
                                errors.add(new SimpleParseException("Cannot reference instance variable from static context", -1, v.position));
                            }
                        }
                    }
                }
            }
            if (vt instanceof Scope) {
                Scope vs = (Scope) vt;
                boolean subStatic = isStatic;
                if (vs instanceof FunctionScope) {
                    subStatic = ((FunctionScope) vs).isStatic();
                }
                if (vs instanceof TraitVarConstValueScope) {
                    subStatic = ((TraitVarConstValueScope) vs).isStatic();
                }
                parseVariablesList(vs.getPrivateItems(), vs.getSharedItems(), definitionPosToReferences, referenceToDefinition, privateVarFullNameToDefinitionPosition, privateVarNameToDefinitionPosition, positionToStatic, subStatic, errors);
            }
        }
        for (VariableOrScope vt : sharedVariables) {
            if (vt instanceof Variable) {
                Variable v = (Variable) vt;
                if (v.definition) {
                    parentVarFullNameToDefinitionPosition.put(v.name, v.position);
                    parentVarNameToDefinitionPosition.put(v.getLastName(), v.position);
                    privateVarFullNameToDefinitionPosition.put(v.name, v.position);
                    privateVarNameToDefinitionPosition.put(v.getLastName(), v.position);
                    definitionPosToReferences.put(v.position, new ArrayList<>());
                    positionToStatic.put(v.position, v.isStatic != null ? v.isStatic : isStatic);
                } else {
                    if (!privateVarFullNameToDefinitionPosition.containsKey(v.name)
                            && !privateVarNameToDefinitionPosition.containsKey(v.name)) {
                        parentVarFullNameToDefinitionPosition.put(v.name, -v.position - 1);
                        parentVarNameToDefinitionPosition.put(v.getFirstName(), -v.position - 1);
                        privateVarFullNameToDefinitionPosition.put(v.name, -v.position - 1);
                        privateVarNameToDefinitionPosition.put(v.getFirstName(), -v.position - 1);
                        definitionPosToReferences.put(-v.position - 1, new ArrayList<>());
                        definitionPosToReferences.get(-v.position - 1).add(v.position);
                        referenceToDefinition.put(v.position, -v.position - 1);
                    } else {

                        if ("this".equals(v.name) && isStatic) {
                            errors.add(new SimpleParseException("Cannot use this in static context", -1, v.position));
                        } else {
                            int definitionPos;
                            if (privateVarFullNameToDefinitionPosition.containsKey(v.name)) {
                                definitionPos = privateVarFullNameToDefinitionPosition.get(v.name);
                            } else {
                                definitionPos = privateVarNameToDefinitionPosition.get(v.name);
                            }
                            boolean staticDefinition = definitionPos >= 0 ? positionToStatic.get(definitionPos) : true;
                            if (!(!staticDefinition && isStatic)) {
                                definitionPosToReferences.get(definitionPos).add(v.position);
                                referenceToDefinition.put(v.position, definitionPos);
                            } else {
                                errors.add(new SimpleParseException("Cannot reference instance variable from static context", -1, v.position));
                            }
                        }
                    }
                }
            }
            if (vt instanceof Scope) {
                Scope vs = (Scope) vt;
                boolean subStatic = isStatic;
                if (vs instanceof FunctionScope) {
                    subStatic = ((FunctionScope) vs).isStatic();
                }
                if (vs instanceof TraitVarConstValueScope) {
                    subStatic = ((TraitVarConstValueScope) vs).isStatic();
                }
                parseVariablesList(vs.getPrivateItems(), vs.getSharedItems(), definitionPosToReferences, referenceToDefinition, privateVarFullNameToDefinitionPosition, privateVarNameToDefinitionPosition, positionToStatic, subStatic, errors);
            }
        }
    }
}
