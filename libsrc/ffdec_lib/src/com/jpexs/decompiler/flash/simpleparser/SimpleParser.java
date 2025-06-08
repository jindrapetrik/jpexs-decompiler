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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            List<SimpleParseException> errors,
            List<String> externalTypes,
            Map<Integer, Integer> referenceToExternalTypeIndex,
            Map<Integer, List<Integer>> externalTypeIndexToReference
    ) throws SimpleParseException, IOException, InterruptedException;

    public static void parseVariablesList(
            List<VariableOrScope> privateVariables,
            List<VariableOrScope> sharedVariables,
            Map<Integer, List<Integer>> definitionPosToReferences,
            Map<Integer, Integer> referenceToDefinition,
            List<SimpleParseException> errors,
            boolean innerFunctionCanUseTraits,
            List<String> externalTypes,
            Map<Integer, Integer> referenceToExternalTypeIndex,
            Map<Integer, List<Integer>> externalTypeIndexToReference
    ) {
        List<String> externalSimpleTypes = new ArrayList<>();
        for (String type : externalTypes) {
            externalSimpleTypes.add(type.contains(".") ? type.substring(type.lastIndexOf(".") + 1) : type);
        }

        Map<Integer, String> definitionToType = new LinkedHashMap<>();
        Map<Integer, String> definitionToCallType = new LinkedHashMap<>();
        Map<Integer, Variable> definitionToSubType = new LinkedHashMap<>();
        Map<Integer, Variable> definitionToCallSubType = new LinkedHashMap<>();
        Map<String, Integer> traitFullNameToDefinition = new LinkedHashMap<>();

        Map<Integer, Boolean> positionToStatic = new LinkedHashMap<>();
        findClassTraits(privateVariables, traitFullNameToDefinition, definitionPosToReferences, positionToStatic, definitionToType, definitionToCallType, definitionToSubType, definitionToCallSubType);
        findClassTraits(sharedVariables, traitFullNameToDefinition, definitionPosToReferences, positionToStatic, definitionToType, definitionToCallType, definitionToSubType, definitionToCallSubType);

        parseVariablesList(privateVariables, sharedVariables, definitionPosToReferences, referenceToDefinition, new LinkedHashMap<>(), new LinkedHashMap<>(), positionToStatic, true, errors, null, innerFunctionCanUseTraits, externalSimpleTypes, referenceToExternalTypeIndex, definitionToType, definitionToCallType, definitionToSubType, definitionToCallSubType, traitFullNameToDefinition);
        for (Map.Entry<Integer, Integer> entry : referenceToExternalTypeIndex.entrySet()) {
            if (!externalTypeIndexToReference.containsKey(entry.getValue())) {
                externalTypeIndexToReference.put(entry.getValue(), new ArrayList<>());
            }
            externalTypeIndexToReference.get(entry.getValue()).add(entry.getKey());
        }
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
            List<SimpleParseException> errors,
            Scope scope,
            boolean innerFunctionCanUseTraits,
            List<String> externalSimpleTypes,
            Map<Integer, Integer> referenceToExternalTypeIndex,
            Map<Integer, String> definitionToType,
            Map<Integer, String> definitionToCallType,
            Map<Integer, Variable> definitionToSubType,
            Map<Integer, Variable> definitionToCallSubType,
            Map<String, Integer> traitFullNameToDefinition
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
                    if (!definitionPosToReferences.containsKey(v.position)) {
                        definitionPosToReferences.put(v.position, new ArrayList<>());
                    }
                    positionToStatic.put(v.position, v.isStatic != null ? v.isStatic : isStatic);
                    if (v.type != null) {
                        definitionToType.put(v.position, v.type);
                    }
                    if (v.subType != null) {
                        definitionToSubType.put(v.position, v.subType);
                    }
                } else {
                    if (!privateVarFullNameToDefinitionPosition.containsKey(v.name)
                            && !privateVarNameToDefinitionPosition.containsKey(v.name)) {

                        if (externalSimpleTypes.contains(v.name)) {
                            referenceToExternalTypeIndex.put(v.position, externalSimpleTypes.indexOf(v.name));
                        } else {
                            boolean traitFound = searchTrait(v, privateVarFullNameToDefinitionPosition, privateVarNameToDefinitionPosition, definitionToType, definitionToCallType, definitionToSubType, definitionToCallSubType, traitFullNameToDefinition, definitionPosToReferences, referenceToDefinition);
                            if (!traitFound) {
                                parentVarFullNameToDefinitionPosition.put(v.name, -v.position - 1);
                                parentVarNameToDefinitionPosition.put(v.getLastName(), -v.position - 1);
                                privateVarFullNameToDefinitionPosition.put(v.name, -v.position - 1);
                                privateVarNameToDefinitionPosition.put(v.getLastName(), -v.position - 1);
                                definitionPosToReferences.put(-v.position - 1, new ArrayList<>());
                                definitionPosToReferences.get(-v.position - 1).add(v.position);
                                referenceToDefinition.put(v.position, -v.position - 1);
                            }
                        }
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
                                //errors.add(new SimpleParseException("Cannot reference instance variable from static context", -1, v.position));
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

                //if its inner function (not a method), remove all this variables
                Map<String, Integer> subPrivateVarFullNameToDefinitionPosition = privateVarFullNameToDefinitionPosition;
                Map<String, Integer> subPrivateVarNameToDefinitionPosition = privateVarNameToDefinitionPosition;
                if ((vs instanceof FunctionScope) && (!(vs instanceof MethodScope))) {
                    subPrivateVarFullNameToDefinitionPosition = new LinkedHashMap<>(subPrivateVarFullNameToDefinitionPosition);
                    subPrivateVarNameToDefinitionPosition = new LinkedHashMap<>(subPrivateVarNameToDefinitionPosition);
                    Set<String> keys = new HashSet<>(subPrivateVarFullNameToDefinitionPosition.keySet());
                    for (String vName : keys) {
                        if (vName.equals("this") || vName.startsWith("this.")) {
                            subPrivateVarFullNameToDefinitionPosition.remove(vName);
                            if (vName.equals("this")) {
                                subPrivateVarNameToDefinitionPosition.remove("this");
                            }
                            if (!innerFunctionCanUseTraits) {
                                String lastName = vName.contains(".") ? vName.substring(vName.lastIndexOf(".") + 1) : vName;
                                subPrivateVarNameToDefinitionPosition.remove(lastName);
                            }
                        }
                    }
                }

                parseVariablesList(vs.getPrivateItems(), vs.getSharedItems(), definitionPosToReferences, referenceToDefinition, subPrivateVarFullNameToDefinitionPosition, subPrivateVarNameToDefinitionPosition, positionToStatic, subStatic, errors, vs, innerFunctionCanUseTraits, externalSimpleTypes, referenceToExternalTypeIndex, definitionToType, definitionToCallType, definitionToSubType, definitionToCallSubType, traitFullNameToDefinition);
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
                    if (!definitionPosToReferences.containsKey(v.position)) {
                        definitionPosToReferences.put(v.position, new ArrayList<>());
                    }
                    positionToStatic.put(v.position, v.isStatic != null ? v.isStatic : isStatic);
                    if (v.type != null) {
                        definitionToType.put(v.position, v.type);
                    }
                    if (v.subType != null) {
                        definitionToSubType.put(v.position, v.subType);
                    }
                } else {
                    if (!privateVarFullNameToDefinitionPosition.containsKey(v.name)
                            && !privateVarNameToDefinitionPosition.containsKey(v.name)) {

                        if (externalSimpleTypes.contains(v.name)) {
                            referenceToExternalTypeIndex.put(v.position, externalSimpleTypes.indexOf(v.name));
                        } else {
                            boolean traitFound = searchTrait(v, privateVarFullNameToDefinitionPosition, privateVarNameToDefinitionPosition, definitionToType, definitionToCallType, definitionToSubType, definitionToCallSubType, traitFullNameToDefinition, definitionPosToReferences, referenceToDefinition);
                            if (!traitFound) {
                                parentVarFullNameToDefinitionPosition.put(v.name, -v.position - 1);
                                parentVarNameToDefinitionPosition.put(v.getFirstName(), -v.position - 1);
                                privateVarFullNameToDefinitionPosition.put(v.name, -v.position - 1);
                                privateVarNameToDefinitionPosition.put(v.getFirstName(), -v.position - 1);
                                definitionPosToReferences.put(-v.position - 1, new ArrayList<>());
                                definitionPosToReferences.get(-v.position - 1).add(v.position);
                                referenceToDefinition.put(v.position, -v.position - 1);
                            }
                        }
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
                                //errors.add(new SimpleParseException("Cannot reference instance variable from static context", -1, v.position));
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

                //if its inner function (not a method), remove all this variables                
                Map<String, Integer> subPrivateVarFullNameToDefinitionPosition = privateVarFullNameToDefinitionPosition;
                Map<String, Integer> subPrivateVarNameToDefinitionPosition = privateVarNameToDefinitionPosition;
                if ((vs instanceof FunctionScope) && (!(vs instanceof MethodScope))) {
                    subPrivateVarFullNameToDefinitionPosition = new LinkedHashMap<>(subPrivateVarFullNameToDefinitionPosition);
                    subPrivateVarNameToDefinitionPosition = new LinkedHashMap<>(subPrivateVarNameToDefinitionPosition);
                    Set<String> keys = new HashSet<>(subPrivateVarFullNameToDefinitionPosition.keySet());
                    for (String vName : keys) {
                        if (vName.equals("this") || vName.startsWith("this.")) {
                            subPrivateVarFullNameToDefinitionPosition.remove(vName);
                            if (vName.equals("this")) {
                                subPrivateVarNameToDefinitionPosition.remove("this");
                            }
                            if (!innerFunctionCanUseTraits) {
                                String lastName = vName.contains(".") ? vName.substring(vName.lastIndexOf(".") + 1) : vName;
                                subPrivateVarNameToDefinitionPosition.remove(lastName);
                            }
                        }
                    }
                }

                parseVariablesList(vs.getPrivateItems(), vs.getSharedItems(), definitionPosToReferences, referenceToDefinition, privateVarFullNameToDefinitionPosition, privateVarNameToDefinitionPosition, positionToStatic, subStatic, errors, vs, innerFunctionCanUseTraits, externalSimpleTypes, referenceToExternalTypeIndex, definitionToType, definitionToCallType, definitionToSubType, definitionToCallSubType, traitFullNameToDefinition);
            }
        }
    }

    public static void findClassTraits(
            List<VariableOrScope> variables,
            Map<String, Integer> traitFullNameToDefinition,
            Map<Integer, List<Integer>> definitionPosToReferences,
            Map<Integer, Boolean> positionToStatic,
            Map<Integer, String> definitionToType,
            Map<Integer, String> definitionToCallType,
            Map<Integer, Variable> definitionToSubType,
            Map<Integer, Variable> definitionToCallSubType
    ) {
        for (VariableOrScope v : variables) {
            if (v instanceof ClassTrait) {
                ClassTrait ct = (ClassTrait) v;
                definitionPosToReferences.put(ct.position, new ArrayList<>());
                positionToStatic.put(ct.position, ct.isStatic);
                if (ct.type != null) {
                    definitionToType.put(ct.position, ct.type);
                }
                if (ct.callType != null) {
                    definitionToCallType.put(ct.position, ct.callType);
                }
                if (ct.subType != null) {
                    definitionToSubType.put(ct.position, ct.subType);
                }
                if (ct.callSubType != null) {
                    definitionToCallSubType.put(ct.position, ct.callSubType);
                }
                traitFullNameToDefinition.put(ct.getFullIdentifier(), ((ClassTrait) v).position);
            }
            if (v instanceof Scope) {
                Scope s = (Scope) v;
                findClassTraits(s.getPrivateItems(), traitFullNameToDefinition, definitionPosToReferences, positionToStatic, definitionToType, definitionToCallType, definitionToSubType, definitionToCallSubType);
                findClassTraits(s.getSharedItems(), traitFullNameToDefinition, definitionPosToReferences, positionToStatic, definitionToType, definitionToCallType, definitionToSubType, definitionToCallSubType);
            }
        }
    }

    public static boolean searchTrait(
            Variable v,
            Map<String, Integer> privateVarFullNameToDefinitionPosition,
            Map<String, Integer> privateVarNameToDefinitionPosition,
            Map<Integer, String> definitionToType,
            Map<Integer, String> definitionToCallType,
            Map<Integer, Variable> definitionToSubType,
            Map<Integer, Variable> definitionToCallSubType,
            Map<String, Integer> traitFullNameToDefinition,
            Map<Integer, List<Integer>> definitionPosToReferences,
            Map<Integer, Integer> referenceToDefinition
    ) {
        boolean traitFound = false;
        if (v.hasParent()) {
            List<String> parts = v.getParts();

            Integer definitionPos = null;
            String firstName = parts.get(0);
            if (privateVarFullNameToDefinitionPosition.containsKey(firstName)) {
                definitionPos = privateVarFullNameToDefinitionPosition.get(firstName);
            } else if (privateVarNameToDefinitionPosition.containsKey(firstName)) {
                definitionPos = privateVarNameToDefinitionPosition.get(firstName);
            }
            if (definitionPos != null) {
                if (definitionToType.containsKey(definitionPos)) {
                    String type = definitionToType.get(definitionPos);
                    traitFound = true;
                    Variable lastSubType = null;
                    for (int p = 1; p < parts.size(); p++) {
                        String part = parts.get(p);
                        if (part.equals("()")) {
                            if (parts.get(p - 1).equals("[]")) {
                                traitFound = false;
                                break;
                            }
                            type = definitionToCallType.get(definitionPos);
                            lastSubType = null;
                        } else if (part.equals("[]")) {
                            if (lastSubType != null) {
                                lastSubType = lastSubType.subType;
                            } else if (parts.get(p - 1).equals("()")) {
                                lastSubType = definitionToCallSubType.get(definitionPos);
                            } else {
                                lastSubType = definitionToSubType.get(definitionPos);
                            }
                            if (lastSubType == null) {
                                traitFound = false;
                                break;
                            }
                            type = lastSubType.name;
                        } else {
                            String traitKey = type + "/" + part;
                            if (!traitFullNameToDefinition.containsKey(traitKey)) {
                                traitFound = false;
                                break;
                            }
                            definitionPos = traitFullNameToDefinition.get(traitKey);
                            type = definitionToType.get(definitionPos);
                            lastSubType = null;
                        }
                    }

                    if (traitFound) {
                        definitionPosToReferences.get(definitionPos).add(v.position);
                        referenceToDefinition.put(v.position, definitionPos);
                    }
                }
            }
        }
        return traitFound;
    }
}
