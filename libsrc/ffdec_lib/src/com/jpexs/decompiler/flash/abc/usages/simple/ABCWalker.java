/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.usages.simple;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Walker for ABC. Used in Simple usage detector.
 *
 * @author JPEXS
 */
public abstract class ABCWalker {

    /**
     * Walk type
     */
    public static enum WalkType {
        /**
         * Orphan
         */
        Orphan,
        /**
         * Script
         */
        Script,
        /**
         * Class
         */
        Class,
        /**
         * Instance
         */
        Instance
    }

    /**
     * Constructor
     */
    public ABCWalker() {
    }

    /**
     * Walks ABC
     *
     * @param abc ABC
     * @param walkOrphanItems Walk orphan items
     */
    public final void walkABC(ABC abc, boolean walkOrphanItems) {
        Set<Integer> handledClasses = new HashSet<>();
        Set<Integer> handledMethodInfos = new HashSet<>();
        Set<Integer> handledMethodBodies = new HashSet<>();

        for (int i = 0; i < abc.script_info.size(); i++) {
            handleScript(abc, i);
            ScriptInfo si = abc.script_info.get(i);
            optionalHandleMethodInfo(handledMethodBodies, handledMethodInfos, abc, si.init_index, i, -1, -1, -1, WalkType.Script, true, new Stack<>());
            walkTraits(abc, si.traits, i, -1, -1, -1, -1, handledMethodBodies, handledMethodInfos, handledClasses, WalkType.Script, new Stack<>());
        }

        if (walkOrphanItems) {
            for (int i = 0; i < abc.method_info.size(); i++) {
                optionalHandleMethodInfo(handledMethodBodies, handledMethodInfos, abc, i, -1, -1, -1, -1, WalkType.Orphan, false, new Stack<>());
            }

            for (int i = 0; i < abc.bodies.size(); i++) {
                optionalHandleMethodBody(handledMethodBodies, handledMethodInfos, abc, i, -1, -1, -1, -1, WalkType.Orphan, false, new Stack<>());
            }

            for (int i = 0; i < abc.class_info.size(); i++) {
                optionalHandleClass(handledClasses, handledMethodBodies, handledMethodInfos, abc, i, -1, -1, -1, WalkType.Orphan);
            }
        }
    }

    /**
     * Optional handle class
     *
     * @param handledClasses Handled classes
     * @param handledMethodBodies Handled method bodies
     * @param handledMethodInfos Handled method infos
     * @param abc ABC
     * @param index Index
     * @param scriptIndex Script index
     * @param scriptTraitIndex Script trait index
     * @param traitIndex Trait index
     * @param walkType Walk type
     * @return True if handled
     */
    private boolean optionalHandleClass(Set<Integer> handledClasses, Set<Integer> handledMethodBodies, Set<Integer> handledMethodInfos, ABC abc, int index, int scriptIndex, int scriptTraitIndex, int traitIndex, WalkType walkType) {
        if (handledClasses.contains(index)) {
            return false;
        }
        handleClass(abc, index, scriptIndex, traitIndex, walkType);

        optionalHandleMethodInfo(handledMethodBodies, handledMethodInfos, abc, abc.instance_info.get(index).iinit_index, scriptIndex, scriptTraitIndex, index, -1, WalkType.Instance, true, new Stack<>());
        walkTraits(abc, abc.instance_info.get(index).instance_traits, scriptIndex, scriptTraitIndex, index, -1, -1, handledMethodBodies, handledMethodInfos, handledClasses, WalkType.Instance, new Stack<>());
        optionalHandleMethodInfo(handledMethodBodies, handledMethodInfos, abc, abc.class_info.get(index).cinit_index, scriptIndex, scriptTraitIndex, index, -1, WalkType.Class, true, new Stack<>());
        walkTraits(abc, abc.class_info.get(index).static_traits, scriptIndex, scriptTraitIndex, index, -1, -1, handledMethodBodies, handledMethodInfos, handledClasses, WalkType.Class, new Stack<>());

        return true;
    }

    /**
     * Optional handle method info.
     *
     * @param handledMethodBodies Handled method bodies
     * @param handledMethodInfos Handled method infos
     * @param abc ABC
     * @param index Index
     * @param scriptIndex Script index
     * @param scriptTraitIndex Script trait index
     * @param classIndex Class index
     * @param traitIndex Trait index
     * @param walkType Walk type
     * @param initializer Initializer
     * @param callStack Call stack
     */
    private void optionalHandleMethodInfo(Set<Integer> handledMethodBodies, Set<Integer> handledMethodInfos, ABC abc, int index, int scriptIndex, int scriptTraitIndex, int classIndex, int traitIndex, WalkType walkType, boolean initializer, Stack<Integer> callStack) {
        if (handledMethodInfos.contains(index)) {
            return;
        }
        handledMethodInfos.add(index);
        if (callStack != null) {
            if (callStack.contains(index)) {
                return;
            }
            callStack.push(index);
        }
        handleMethodInfo(abc, index, scriptIndex, scriptTraitIndex, classIndex, traitIndex, walkType, initializer, callStack);
        int bodyIndex = abc.findBodyIndex(index);
        if (bodyIndex > -1) {
            optionalHandleMethodBody(handledMethodBodies, handledMethodInfos, abc, bodyIndex, scriptIndex, scriptTraitIndex, classIndex, traitIndex, walkType, initializer, callStack);
        }
        if (callStack != null) {
            callStack.pop();
        }
    }

    /**
     * Optional handle method body.
     *
     * @param handledMethodBodies Handled method bodies
     * @param handledMethodInfos Handled method infos
     * @param abc ABC
     * @param index Index
     * @param scriptIndex Script index
     * @param scriptTraitIndex Script trait index
     * @param classIndex Class index
     * @param traitIndex Trait index
     * @param walkType Walk type
     * @param initializer Initializer
     * @param callStack Call stack
     */
    private void optionalHandleMethodBody(Set<Integer> handledMethodBodies, Set<Integer> handledMethodInfos, ABC abc, int index, int scriptIndex, int scriptTraitIndex, int classIndex, int traitIndex, WalkType walkType, boolean initializer, Stack<Integer> callStack) {
        if (handledMethodBodies.contains(index)) {
            return;
        }
        handledMethodBodies.add(index);
        handleMethodBody(abc, index, scriptIndex, classIndex, traitIndex, walkType, initializer);
        List<AVM2Instruction> code = abc.bodies.get(index).getCode().code;
        for (AVM2Instruction ins : code) {
            for (int o = 0; o < ins.definition.operands.length; o++) {
                if (ins.definition.operands[o] == AVM2Code.DAT_METHOD_INDEX) {
                    optionalHandleMethodInfo(handledMethodBodies, handledMethodInfos, abc, ins.operands[o], scriptIndex, scriptTraitIndex, classIndex, traitIndex, walkType, initializer, callStack);
                }
            }
        }
        walkTraits(abc, abc.bodies.get(index).traits, scriptIndex, scriptTraitIndex, classIndex, traitIndex, index, handledMethodBodies, handledMethodInfos, handledMethodInfos, walkType, callStack);
    }

    /**
     * Walk traits.
     *
     * @param abc ABC
     * @param traits Traits
     * @param scriptIndex Script index
     * @param scriptTraitIndex Script trait index
     * @param classIndex Class index
     * @param traitIndex Trait index
     * @param bodyIndex Body index
     * @param handledMethodBodies Handled method bodies
     * @param handledMethodInfos Handled method infos
     * @param handledClasses Handled classes
     * @param walkType Walk type
     * @param callStack Call stack
     */
    private void walkTraits(ABC abc, Traits traits, int scriptIndex, int scriptTraitIndex, int classIndex, int traitIndex, int bodyIndex, Set<Integer> handledMethodBodies, Set<Integer> handledMethodInfos, Set<Integer> handledClasses, WalkType walkType, Stack<Integer> callStack) {

        int bodyTraitIndex = -1;

        for (int i = 0; i < traits.traits.size(); i++) {
            if (classIndex == -1) {
                scriptTraitIndex = i;
            }
            if (bodyIndex == -1 && classIndex != -1) {
                traitIndex = i;
            }
            if (bodyIndex != -1) {
                bodyTraitIndex = i;
            }
            Trait t = traits.traits.get(i);
            if ((t.kindFlags & Trait.ATTR_Metadata) > 0) {
                for (int m = 0; m < t.metadata.length; m++) {
                    handleMetadataInfo(abc, t.metadata[m], t, scriptIndex, scriptTraitIndex, classIndex, i, m, walkType);
                }
            }
            if (t instanceof TraitClass) {
                TraitClass tc = (TraitClass) t;
                handleTraitClass(abc, tc, scriptIndex, i);
                int subClassIndex = tc.class_info;
                optionalHandleClass(handledClasses, handledMethodBodies, handledMethodInfos, abc, subClassIndex, scriptIndex, scriptTraitIndex, i, walkType);
            }
            if (t instanceof TraitMethodGetterSetter) {
                TraitMethodGetterSetter tm = (TraitMethodGetterSetter) t;
                optionalHandleMethodInfo(handledMethodBodies, handledMethodInfos, abc, tm.method_info, scriptIndex, scriptTraitIndex, classIndex, i, walkType, false, new Stack<>());
                handleTraitMethodGetterSetter(abc, tm, scriptIndex, scriptTraitIndex, classIndex, i, walkType);
            }
            if (t instanceof TraitFunction) {
                TraitFunction tf = (TraitFunction) t;
                optionalHandleMethodInfo(handledMethodBodies, handledMethodInfos, abc, tf.method_info, scriptIndex, scriptTraitIndex, classIndex, i, walkType, false, new Stack<>());
                handleTraitFunction(abc, tf, scriptIndex, scriptTraitIndex, classIndex, i, walkType);
            }
            if (t instanceof TraitSlotConst) {
                TraitSlotConst tsc = (TraitSlotConst) t;
                handleTraitSlotConst(abc, tsc, scriptIndex, scriptTraitIndex, classIndex, traitIndex, bodyIndex, bodyTraitIndex, walkType, callStack);
            }
        }
    }

    /**
     * Handle method info.
     *
     * @param abc ABC
     * @param index Index
     * @param scriptIndex Script index
     * @param scriptTraitIndex Script trait index
     * @param classIndex Class index
     * @param traitIndex Trait index
     * @param walkType Walk type
     * @param initializer Initializer
     * @param callStack Call stack
     */
    protected void handleMethodInfo(ABC abc, int index, int scriptIndex, int scriptTraitIndex, int classIndex, int traitIndex, WalkType walkType, boolean initializer, Stack<Integer> callStack) {

    }

    /**
     * Handle method body.
     *
     * @param abc ABC
     * @param index Index
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param traitIndex Trait index
     * @param walkType Walk type
     * @param initializer Initializer
     */
    protected void handleMethodBody(ABC abc, int index, int scriptIndex, int classIndex, int traitIndex, WalkType walkType, boolean initializer) {

    }

    /**
     * Handle class.
     *
     * @param abc ABC
     * @param index Index
     * @param scriptIndex Script index
     * @param traitIndex Trait index
     * @param walkType Walk type
     */
    protected void handleClass(ABC abc, int index, int scriptIndex, int traitIndex, WalkType walkType) {

    }

    /**
     * Handle script.
     *
     * @param abc ABC
     * @param index Index
     */
    protected void handleScript(ABC abc, int index) {

    }

    /**
     * Handle trait slot const.
     *
     * @param abc ABC
     * @param trait Trait
     * @param scriptIndex Script index
     * @param scriptTraitIndex Script trait index
     * @param classIndex Class index
     * @param traitIndex Trait index
     * @param bodyIndex Body index
     * @param bodyTraitIndex Body trait index
     * @param walkType Walk type
     * @param callStack Call stack
     */
    protected void handleTraitSlotConst(ABC abc, TraitSlotConst trait, int scriptIndex, int scriptTraitIndex, int classIndex, int traitIndex, int bodyIndex, int bodyTraitIndex, WalkType walkType, Stack<Integer> callStack) {

    }

    /**
     * Handle trait method getter setter.
     *
     * @param abc ABC
     * @param trait Trait
     * @param scriptIndex Script index
     * @param scriptTraitIndex Script trait index
     * @param classIndex Class index
     * @param traitIndex Trait index
     * @param walkType Walk type
     */
    protected void handleTraitMethodGetterSetter(ABC abc, TraitMethodGetterSetter trait, int scriptIndex, int scriptTraitIndex, int classIndex, int traitIndex, WalkType walkType) {

    }

    /**
     * Handle trait function.
     *
     * @param abc ABC
     * @param trait Trait
     * @param scriptIndex Script index
     * @param scriptTraitIndex Script trait index
     * @param classIndex Class index
     * @param traitIndex Trait index
     * @param walkType Walk type
     */
    protected void handleTraitFunction(ABC abc, TraitFunction trait, int scriptIndex, int scriptTraitIndex, int classIndex, int traitIndex, WalkType walkType) {

    }

    /**
     * Handle trait class.
     *
     * @param abc ABC
     * @param trait Trait
     * @param scriptIndex Script index
     * @param scriptTraitIndex Script trait index
     */
    protected void handleTraitClass(ABC abc, TraitClass trait, int scriptIndex, int scriptTraitIndex) {

    }

    /**
     * Handle metadata info.
     *
     * @param abc ABC
     * @param index Index
     * @param trait Trait
     * @param scriptIndex Script index
     * @param scriptTraitIndex Script trait index
     * @param classIndex Class index
     * @param traitIndex Trait index
     * @param traitMetadataIndex Trait metadata index
     * @param walkType Walk type
     */
    protected void handleMetadataInfo(ABC abc, int index, Trait trait, int scriptIndex, int scriptTraitIndex, int classIndex, int traitIndex, int traitMetadataIndex, WalkType walkType) {

    }
}
