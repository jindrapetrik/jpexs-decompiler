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
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple parser used for highlighting in editors.
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
     * @param externalTypes External types
     * @param referenceToExternalTypeIndex Reference to external type index
     * @param externalTypeIndexToReference External type index to reference
     * @param linkHandler Link handler
     * @param referenceToExternalTraitKey Reference to external trait key
     * @param externalTraitKeyToReference External trait key to reference
     * @throws SimpleParseException On parse error
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public void parse(
            String str,
            Map<Integer, List<Integer>> definitionPosToReferences,
            Map<Integer, Integer> referenceToDefinition,
            List<SimpleParseException> errors,
            List<Path> externalTypes,
            Map<Integer, Integer> referenceToExternalTypeIndex,
            Map<Integer, List<Integer>> externalTypeIndexToReference,
            LinkHandler linkHandler,
            Map<Integer, Path> referenceToExternalTraitKey,
            Map<Path, List<Integer>> externalTraitKeyToReference,
            Map<Integer, Path> separatorPosToType,
            Map<Integer, Boolean> separatorIsStatic,
            Map<Path, List<Variable>> localTypeTraitNames,
            Map<Integer, Path> definitionToType,
            Map<Integer, Path> definitionToCallType,
            Integer caretPosition,
            List<Variable> variableSuggestions
    ) throws SimpleParseException, IOException, InterruptedException;

    public static void fillSuggestionsOne(
            List<Variable> variableSuggestions,
            Map<Path, Integer> varNameToDefinition,
            Map<Integer, Boolean> positionToStatic,
            Map<Integer, Path> definitionToType,
            Map<Integer, Path> definitionToCallType,
            Map<Integer, Variable> definitionToSubType,
            Map<Integer, Variable> definitionToCallSubType,
            Set<String> used
    ) {
        for (Path p : varNameToDefinition.keySet()) {
            int definition = varNameToDefinition.get(p);
            if (definition >= 0) {
                if (used.contains(p.getLast().toString())) {
                    continue;
                }
                used.add(p.getLast().toString());
                Path type = definitionToType.get(definition);;
                Path callType = definitionToCallType.get(definition);
                Variable subType = definitionToSubType.get(definition);
                Variable callSubType = definitionToCallSubType.get(definition);
                Boolean isStatic = positionToStatic.get(definition);
                variableSuggestions.add(new Variable(true, p, definition, isStatic, type, callType, subType, callSubType));
            }
        }
    }

    public static void fillSuggestions(List<Variable> variableSuggestions,
            Map<Path, Integer> varNameToDefinition1,
            Map<Path, Integer> varNameToDefinition2,
            Map<Integer, Boolean> positionToStatic,
            Map<Integer, Path> definitionToType,
            Map<Integer, Path> definitionToCallType,
            Map<Integer, Variable> definitionToSubType,
            Map<Integer, Variable> definitionToCallSubType
    ) {
        Set<String> used = new HashSet<>();
        if (varNameToDefinition1 != null) {
            fillSuggestionsOne(variableSuggestions, varNameToDefinition1, positionToStatic, definitionToType, definitionToCallType, definitionToSubType, definitionToCallSubType, used);
        }
        if (varNameToDefinition2 != null) {
            fillSuggestionsOne(variableSuggestions, varNameToDefinition2, positionToStatic, definitionToType, definitionToCallType, definitionToSubType, definitionToCallSubType, used);
        }
        variableSuggestions.add(new Variable(true, new Path("--finish--"), 0));
    }

    public static void parseVariablesList(
            List<VariableOrScope> sharedVariables,
            Map<Integer, List<Integer>> definitionPosToReferences,
            Map<Integer, Integer> referenceToDefinition,
            List<SimpleParseException> errors,
            boolean innerFunctionCanUseTraits,
            List<Path> externalTypes,
            Map<Integer, Integer> referenceToExternalTypeIndex,
            Map<Integer, List<Integer>> externalTypeIndexToReference,
            LinkHandler linkHandler,
            Map<Integer, Path> referenceToExternalTraitKey,
            Map<Path, List<Integer>> externalTraitKeyToReference,
            Map<Integer, Path> separatorPosToType,
            Map<Integer, Boolean> separatorIsStatic,
            Map<Path, List<Variable>> localTypeTraits,
            Map<Integer, Path> definitionToType,
            Map<Integer, Path> definitionToCallType,
            Integer caretPosition,
            List<Variable> variableSuggestions
    ) {
        List<Path> externalSimpleTypes = new ArrayList<>();
        Map<Path, Path> simpleExternalClassNameToFullClassName = new LinkedHashMap<>();
        for (Path type : externalTypes) {
            externalSimpleTypes.add(type.getLast());
            simpleExternalClassNameToFullClassName.put(type.getLast(), type);
        }

        //Map<Integer, Path> definitionToType = new LinkedHashMap<>();
        //Map<Integer, Path> definitionToCallType = new LinkedHashMap<>();
        Map<Integer, Variable> definitionToSubType = new LinkedHashMap<>();
        Map<Integer, Variable> definitionToCallSubType = new LinkedHashMap<>();
        Map<Path, Integer> traitFullNameToDefinition = new LinkedHashMap<>();

        List<VariableOrScopeWithAccess> variables = new ArrayList<>();
        for (VariableOrScope vs : sharedVariables) {
            variables.add(new VariableOrScopeWithAccess(vs, true));
        }

        Map<Integer, Boolean> positionToStatic = new LinkedHashMap<>();

        Map<Path, Integer> parentVarFullNameToDefinitionPosition = new LinkedHashMap<>();
        Map<Path, Integer> parentVarNameToDefinitionPosition = new LinkedHashMap<>();

        findClassTraits(variables, traitFullNameToDefinition, definitionPosToReferences, positionToStatic, definitionToType, definitionToCallType, definitionToSubType, definitionToCallSubType, parentVarFullNameToDefinitionPosition, parentVarNameToDefinitionPosition);

        for (Path p : traitFullNameToDefinition.keySet()) {
            int definition = traitFullNameToDefinition.get(p);
            boolean isStatic = false;
            if (positionToStatic.containsKey(definition) && positionToStatic.get(definition)) {
                isStatic = true;
            }
            Path traitType = definitionToType.get(definition);
            Path traitCallType = definitionToCallType.get(definition);
            Variable traitSubType = definitionToSubType.get(definition);
            Variable traitCallSubType = definitionToCallSubType.get(definition);

            Path cls = p.getParent();
            String traitName = p.getLast().toString();
            if (!localTypeTraits.containsKey(cls)) {
                localTypeTraits.put(cls, new ArrayList<>());
            }
            localTypeTraits.get(cls).add(new Variable(true, p.getLast(), definition, isStatic, traitType, traitCallType, traitSubType, traitCallSubType));
        }

        parseVariablesList(variables, definitionPosToReferences, referenceToDefinition, parentVarFullNameToDefinitionPosition, parentVarNameToDefinitionPosition, positionToStatic, true, errors, null, innerFunctionCanUseTraits, externalSimpleTypes, externalTypes, referenceToExternalTypeIndex, definitionToType, definitionToCallType, definitionToSubType, definitionToCallSubType, traitFullNameToDefinition, linkHandler, simpleExternalClassNameToFullClassName, referenceToExternalTraitKey, externalTraitKeyToReference, separatorPosToType, separatorIsStatic, caretPosition, variableSuggestions, null, null);
        for (Map.Entry<Integer, Integer> entry : referenceToExternalTypeIndex.entrySet()) {
            if (!externalTypeIndexToReference.containsKey(entry.getValue())) {
                externalTypeIndexToReference.put(entry.getValue(), new ArrayList<>());
            }
            externalTypeIndexToReference.get(entry.getValue()).add(entry.getKey());
        }

        if (caretPosition != null && variableSuggestions.isEmpty()) {
            if ((variables.isEmpty() || variables.get(variables.size() - 1).var.getPosition() <= caretPosition)) {
                fillSuggestions(variableSuggestions, parentVarNameToDefinitionPosition, null, positionToStatic, definitionToType, definitionToCallType, definitionToSubType, definitionToCallSubType);
            }
        }

        if (!variableSuggestions.isEmpty()) {
            variableSuggestions.remove(variableSuggestions.size() - 1);
        }
    }

    public static void parseVariablesList(
            List<VariableOrScopeWithAccess> variables,
            Map<Integer, List<Integer>> definitionPosToReferences,
            Map<Integer, Integer> referenceToDefinition,
            Map<Path, Integer> parentVarFullNameToDefinitionPosition,
            Map<Path, Integer> parentVarNameToDefinitionPosition,
            Map<Integer, Boolean> positionToStatic,
            boolean isStatic,
            List<SimpleParseException> errors,
            Scope scope,
            boolean innerFunctionCanUseTraits,
            List<Path> externalSimpleTypes,
            List<Path> externalFullTypes,
            Map<Integer, Integer> referenceToExternalTypeIndex,
            Map<Integer, Path> definitionToType,
            Map<Integer, Path> definitionToCallType,
            Map<Integer, Variable> definitionToSubType,
            Map<Integer, Variable> definitionToCallSubType,
            Map<Path, Integer> traitFullNameToDefinition,
            LinkHandler linkHandler,
            Map<Path, Path> simpleExternalClassNameToFullClassName,
            Map<Integer, Path> referenceToExternalTraitKey,
            Map<Path, List<Integer>> externalTraitKeyToReference,
            Map<Integer, Path> separatorPosToType,
            Map<Integer, Boolean> separatorIsStatic,
            Integer caretPosition,
            List<Variable> variableSuggestions,
            Integer scopeStartPos,
            Integer scopeEndPos
    ) {
        Map<Path, Integer> privateVarNameToDefinitionPosition = new LinkedHashMap<>();
        privateVarNameToDefinitionPosition.putAll(parentVarNameToDefinitionPosition);

        Map<Path, Integer> privateVarFullNameToDefinitionPosition = new LinkedHashMap<>();
        privateVarFullNameToDefinitionPosition.putAll(parentVarFullNameToDefinitionPosition);

        for (int i = 0; i < variables.size(); i++) {
            VariableOrScopeWithAccess vsa = variables.get(i);
            VariableOrScope vt = variables.get(i).var;
            VariableOrScope vt2 = i + 1 < variables.size() ? variables.get(i + 1).var : null;

            if (i == 0
                    && scopeStartPos != null
                    && caretPosition != null
                    && caretPosition >= scopeStartPos
                    && vt.getPosition() > caretPosition
                    && variableSuggestions.isEmpty()) {
                fillSuggestions(variableSuggestions, parentVarNameToDefinitionPosition, privateVarNameToDefinitionPosition, positionToStatic, definitionToType, definitionToCallType, definitionToSubType, definitionToCallSubType);
            }

            if (vt instanceof Separator) {
                Separator s = (Separator) vt;
                searchTrait(s.parentName, s.position, true, separatorPosToType, separatorIsStatic, privateVarFullNameToDefinitionPosition, privateVarNameToDefinitionPosition, definitionToType, definitionToCallType, definitionToSubType, definitionToCallSubType, traitFullNameToDefinition, definitionPosToReferences, referenceToDefinition, linkHandler, simpleExternalClassNameToFullClassName, referenceToExternalTraitKey, externalTraitKeyToReference);
            }
            if (vt instanceof Variable) {
                Variable v = (Variable) vt;
                if (v.definition) {

                    if (vsa.shared) {
                        parentVarFullNameToDefinitionPosition.put(v.name, v.position);
                        parentVarNameToDefinitionPosition.put(v.getLastName(), v.position);
                    }
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

                        if (externalFullTypes.contains(v.name)) {
                            referenceToExternalTypeIndex.put(v.position, externalFullTypes.indexOf(v.name));
                        } else if (externalSimpleTypes.contains(v.name)) {
                            referenceToExternalTypeIndex.put(v.position, externalSimpleTypes.indexOf(v.name));
                        } else {
                            boolean traitFound = searchTrait(v.name, v.position, false, separatorPosToType, separatorIsStatic, privateVarFullNameToDefinitionPosition, privateVarNameToDefinitionPosition, definitionToType, definitionToCallType, definitionToSubType, definitionToCallSubType, traitFullNameToDefinition, definitionPosToReferences, referenceToDefinition, linkHandler, simpleExternalClassNameToFullClassName, referenceToExternalTraitKey, externalTraitKeyToReference);
                            if (!traitFound) {
                                if (vsa.shared) {
                                    parentVarFullNameToDefinitionPosition.put(v.name, -v.position - 1);
                                    parentVarNameToDefinitionPosition.put(v.getLastName(), -v.position - 1);
                                }
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
                Map<Path, Integer> subPrivateVarFullNameToDefinitionPosition = privateVarFullNameToDefinitionPosition;
                Map<Path, Integer> subPrivateVarNameToDefinitionPosition = privateVarNameToDefinitionPosition;
                if ((vs instanceof FunctionScope) && (!(vs instanceof MethodScope))) {
                    subPrivateVarFullNameToDefinitionPosition = new LinkedHashMap<>(subPrivateVarFullNameToDefinitionPosition);
                    subPrivateVarNameToDefinitionPosition = new LinkedHashMap<>(subPrivateVarNameToDefinitionPosition);
                    Set<Path> keys = new HashSet<>(subPrivateVarFullNameToDefinitionPosition.keySet());
                    for (Path vName : keys) {
                        if (vName.toString().equals("this") || vName.getFirst().toString().equals("this")) {
                            subPrivateVarFullNameToDefinitionPosition.remove(vName);
                            if (vName.toString().equals("this")) {
                                subPrivateVarNameToDefinitionPosition.remove(new Path("this"));
                            }
                            if (!innerFunctionCanUseTraits) {
                                subPrivateVarNameToDefinitionPosition.remove(vName.getLast());
                            }
                        }
                    }
                }

                parseVariablesList(vs.getScopeItems(), definitionPosToReferences, referenceToDefinition, privateVarFullNameToDefinitionPosition, privateVarNameToDefinitionPosition, positionToStatic, subStatic, errors, vs, innerFunctionCanUseTraits, externalSimpleTypes, externalFullTypes, referenceToExternalTypeIndex, definitionToType, definitionToCallType, definitionToSubType, definitionToCallSubType, traitFullNameToDefinition, linkHandler, simpleExternalClassNameToFullClassName, referenceToExternalTraitKey, externalTraitKeyToReference, separatorPosToType, separatorIsStatic, caretPosition, variableSuggestions, vs.getPosition(), vs.getEndPosition());
            }

            if (vt2 != null && caretPosition != null && variableSuggestions.isEmpty()) {
                if (vt.getPosition() <= caretPosition && vt2.getPosition() > caretPosition) {
                    if (vt instanceof Variable) {
                        fillSuggestions(variableSuggestions, parentVarNameToDefinitionPosition, privateVarNameToDefinitionPosition, positionToStatic, definitionToType, definitionToCallType, definitionToSubType, definitionToCallSubType);
                    }
                }
            }
        }

        if (caretPosition != null && variableSuggestions.isEmpty() && scopeEndPos != null) {
            if (scopeEndPos > caretPosition) {
                fillSuggestions(variableSuggestions, parentVarNameToDefinitionPosition, privateVarNameToDefinitionPosition, positionToStatic, definitionToType, definitionToCallType, definitionToSubType, definitionToCallSubType);
            }
        }
    }

    public static void findClassTraits(
            List<VariableOrScopeWithAccess> variables,
            Map<Path, Integer> traitFullNameToDefinition,
            Map<Integer, List<Integer>> definitionPosToReferences,
            Map<Integer, Boolean> positionToStatic,
            Map<Integer, Path> definitionToType,
            Map<Integer, Path> definitionToCallType,
            Map<Integer, Variable> definitionToSubType,
            Map<Integer, Variable> definitionToCallSubType,
            Map<Path, Integer> parentVarFullNameToDefinitionPosition,
            Map<Path, Integer> parentVarNameToDefinitionPosition
    ) {
        for (VariableOrScopeWithAccess vsa : variables) {
            VariableOrScope v = vsa.var;
            if (v instanceof Type) {
                Type t = (Type) v;
                if (t.definition) {
                    definitionToType.put(t.position, t.name);
                }
            }
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

                parentVarFullNameToDefinitionPosition.put(ct.name, ct.position);
                parentVarNameToDefinitionPosition.put(ct.getLastName(), ct.position);
            }
            if (v instanceof Scope) {
                Scope s = (Scope) v;
                findClassTraits(s.getScopeItems(), traitFullNameToDefinition, definitionPosToReferences, positionToStatic, definitionToType, definitionToCallType, definitionToSubType, definitionToCallSubType, parentVarFullNameToDefinitionPosition, parentVarNameToDefinitionPosition);
            }
        }
    }

    public static boolean searchTrait(
            Path vName,
            int vPosition,
            boolean separator,
            Map<Integer, Path> separatorPosToType,
            Map<Integer, Boolean> separatorIsStatic,
            Map<Path, Integer> privateVarFullNameToDefinitionPosition,
            Map<Path, Integer> privateVarNameToDefinitionPosition,
            Map<Integer, Path> definitionToType,
            Map<Integer, Path> definitionToCallType,
            Map<Integer, Variable> definitionToSubType,
            Map<Integer, Variable> definitionToCallSubType,
            Map<Path, Integer> traitFullNameToDefinition,
            Map<Integer, List<Integer>> definitionPosToReferences,
            Map<Integer, Integer> referenceToDefinition,
            LinkHandler linkHandler,
            Map<Path, Path> simpleExternalClassNameToFullClassName,
            Map<Integer, Path> referenceToExternalTraitKey,
            Map<Path, List<Integer>> externalTraitKeyToReference
    ) {
        boolean traitFound = false;
        if (vName.hasParent()) {
            //List<String> parts = v.getParts();
            Path parts = vName;

            Integer definitionPos = null;
            Path firstName = vName.getFirst();
            if (privateVarFullNameToDefinitionPosition.containsKey(firstName)) {
                definitionPos = privateVarFullNameToDefinitionPosition.get(firstName);
            } else if (privateVarNameToDefinitionPosition.containsKey(firstName)) {
                definitionPos = privateVarNameToDefinitionPosition.get(firstName);
            }
            Path type = null;
            boolean isStatic = false;
            if (definitionPos != null) {
                if (definitionToType.containsKey(definitionPos)) {
                    type = definitionToType.get(definitionPos);
                    if (type.getLast().equals(firstName)) {
                        isStatic = true;
                    }
                }
            } else if (simpleExternalClassNameToFullClassName.containsKey(firstName)) {
                type = simpleExternalClassNameToFullClassName.get(firstName);
                isStatic = true;
            }
            if (type != null) {
                traitFound = true;
                Variable lastSubType = null;
                Path traitKey = null;
                Path externalCallType = null;
                List<Path> externalSubTypes = new ArrayList<>();
                List<Path> externalCallSubTypes = new ArrayList<>();
                Path part = null;
                for (int p = 1; p < parts.size(); p++) {
                    part = parts.get(p);
                    if (part.equals(Path.PATH_PARENTHESIS)) {
                        if (parts.get(p - 1).equals(Path.PATH_BRACKETS)) {
                            traitFound = false;
                            break;
                        }
                        if (definitionPos == null) {
                            type = externalCallType;
                            externalSubTypes.clear();
                            externalSubTypes.addAll(externalCallSubTypes);
                        } else {
                            type = definitionToCallType.get(definitionPos);
                        }
                        lastSubType = null;
                    } else if (part.equals(Path.PATH_BRACKETS)) {
                        if (definitionPos != null) {
                            if (lastSubType != null) {
                                lastSubType = lastSubType.subType;
                            } else if (parts.get(p - 1).toString().equals("()")) {
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
                            if (externalSubTypes.isEmpty()) {
                                traitFound = false;
                                break;
                            }
                            type = externalSubTypes.remove(0);
                        }
                    } else {
                        if (type == null) {
                            break;
                        }
                        traitKey = type.add(part);
                        if (!traitFullNameToDefinition.containsKey(traitKey)) {
                            if (simpleExternalClassNameToFullClassName.containsKey(type)) {
                                type = simpleExternalClassNameToFullClassName.get(type);
                            }
                            traitKey = type.add(part);
                            Path newType = linkHandler.getTraitType(type, part.toString());
                            if (newType == null) {
                                traitFound = false;
                                break;
                            }
                            externalSubTypes.clear();
                            int i = 1;
                            while (true) {
                                Path st = linkHandler.getTraitSubType(type, part.toString(), i);
                                if (st == null) {
                                    break;
                                }
                                externalSubTypes.add(st);
                                i++;
                            }
                            externalCallType = linkHandler.getTraitCallType(type, part.toString());
                            externalCallSubTypes.clear();
                            i = 1;
                            while (true) {
                                Path st = linkHandler.getTraitCallSubType(type, part.toString(), i);
                                if (st == null) {
                                    break;
                                }
                                externalCallSubTypes.add(st);
                                i++;
                            }
                            type = newType;
                            definitionPos = null;
                            lastSubType = null;
                            continue;
                        }
                        externalCallSubTypes.clear();
                        externalSubTypes.clear();
                        externalCallType = null;
                        definitionPos = traitFullNameToDefinition.get(traitKey);
                        type = definitionToType.get(definitionPos);
                        lastSubType = null;
                    }
                }

                if (traitFound) {
                    if (definitionPos != null) {
                        definitionPosToReferences.get(definitionPos).add(vPosition);
                        referenceToDefinition.put(vPosition, definitionPos);
                    } else if (part != null) {
                        if (!externalTraitKeyToReference.containsKey(traitKey)) {
                            externalTraitKeyToReference.put(traitKey, new ArrayList<>());
                        }
                        externalTraitKeyToReference.get(traitKey).add(vPosition);
                        referenceToExternalTraitKey.put(vPosition, traitKey);
                    }
                    if (separator) {
                        separatorPosToType.put(vPosition, type);
                        separatorIsStatic.put(vPosition, isStatic);
                    }
                }
            }
        } else if (separator) {
            Integer definitionPos = null;
            Path firstName = vName.getFirst();
            if (privateVarFullNameToDefinitionPosition.containsKey(firstName)) {
                definitionPos = privateVarFullNameToDefinitionPosition.get(firstName);
            } else if (privateVarNameToDefinitionPosition.containsKey(firstName)) {
                definitionPos = privateVarNameToDefinitionPosition.get(firstName);
            }
            Path type = null;
            boolean isStatic = false;
            if (definitionPos != null) {
                if (definitionToType.containsKey(definitionPos)) {
                    type = definitionToType.get(definitionPos);
                    if (type.getLast().equals(firstName)) {
                        isStatic = true;
                    }
                }
            } else if (simpleExternalClassNameToFullClassName.containsKey(firstName)) {
                type = simpleExternalClassNameToFullClassName.get(firstName);
                isStatic = true;
            }
            if (type != null) {
                if (definitionPos != null) {
                    definitionPosToReferences.get(definitionPos).add(vPosition);
                    referenceToDefinition.put(vPosition, definitionPos);
                }
                /* else {
                    if (!externalTraitKeyToReference.containsKey(traitKey)) {
                        externalTraitKeyToReference.put(traitKey, new ArrayList<>());
                    }
                    externalTraitKeyToReference.get(traitKey).add(vPosition);
                    referenceToExternalTraitKey.put(vPosition, traitKey);
                }*/
                separatorPosToType.put(vPosition, type);
                separatorIsStatic.put(vPosition, isStatic);
            }
        }
        return traitFound;
    }
}
