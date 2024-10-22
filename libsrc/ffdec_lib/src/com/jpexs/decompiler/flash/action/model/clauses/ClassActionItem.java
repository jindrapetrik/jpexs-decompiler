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
package com.jpexs.decompiler.flash.action.model.clauses;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.ActionItem;
import com.jpexs.decompiler.flash.action.model.FunctionActionItem;
import com.jpexs.decompiler.flash.action.model.GetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.GetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.SetMemberActionItem;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.parser.script.VariableActionItem;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ActionScript2 class.
 *
 * @author JPEXS
 */
public class ClassActionItem extends ActionItem implements Block {

    /**
     * Extends
     */
    public GraphTargetItem extendsOp;

    /**
     * Implements
     */
    public List<GraphTargetItem> implementsOp;

    /**
     * Class name
     */
    public GraphTargetItem className;

    //public GraphTargetItem constructor;

    /**
     * Traits
     */
    public List<MyEntry<GraphTargetItem, GraphTargetItem>> traits;

    /**
     * Which traits are static
     */
    public List<Boolean> traitsStatic;

    /**
     * Uninitialized variables
     */
    public Set<String> uninitializedVars;

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        //? is this needed for traits ?
        return ret;
    }

    @Override
    public void visitNoBlock(GraphTargetVisitorInterface visitor) {

    }

    /**
     * Constructor.
     *
     * @param className Class name
     * @param extendsOp Extends
     * @param implementsOp Implements
     * @param traits Traits
     * @param traitsStatic Which traits are static
     */
    public ClassActionItem(GraphTargetItem className, GraphTargetItem extendsOp, List<GraphTargetItem> implementsOp, List<MyEntry<GraphTargetItem, GraphTargetItem>> traits, List<Boolean> traitsStatic) {
        super(null, null, NOPRECEDENCE);
        this.className = className;
        this.traits = traits;
        this.traitsStatic = traitsStatic;
        this.extendsOp = extendsOp;
        this.implementsOp = implementsOp;
        //this.constructor = constructor;

        this.uninitializedVars = new HashSet<>();
        List<GraphTargetItem> allUsages = new ArrayList<>();
        for (MyEntry<GraphTargetItem, GraphTargetItem> it : traits) {
            if (it.getValue() instanceof FunctionActionItem) {
                FunctionActionItem f = (FunctionActionItem) it.getValue();
                detectUninitializedVars(f.actions, allUsages);
            }
        }
        Set<String> allMembers = new HashSet<>();
        for (GraphTargetItem it : allUsages) {
            allMembers.add(it.toStringNoQuotes(LocalData.empty));
        }
        uninitializedVars.addAll(allMembers);
        for (MyEntry<GraphTargetItem, GraphTargetItem> v : traits) {
            String s = v.getKey().toStringNoQuotes(LocalData.empty);
            if (uninitializedVars.contains(s)) {
                uninitializedVars.remove(s);
            }
        }
    }

    private boolean isThis(GraphTargetItem item) {
        if (item instanceof VariableActionItem) {
            return "this".equals(((VariableActionItem) item).getVariableName());
        }
        return false;
    }

    private void detectUninitializedVars(GraphTargetItem item, List<GraphTargetItem> ret) {
        if (item == null) {
            return;
        }

        if (item instanceof GetMemberActionItem) {
            GetMemberActionItem gm = (GetMemberActionItem) item;
            if (isThis(gm.object)) {
                ret.add(gm.memberName);
            }
        }
        if (item instanceof SetMemberActionItem) {
            SetMemberActionItem sm = (SetMemberActionItem) item;
            if (isThis(sm.object)) {
                ret.add(sm.objectName);
            }
        }

        detectUninitializedVars(item.getAllSubItems(), ret);
    }

    private void detectUninitializedVars(List<GraphTargetItem> items, List<GraphTargetItem> ret) {
        for (GraphTargetItem it : items) {
            detectUninitializedVars(it, ret);
        }
    }

    private void makePrintObfuscated(GraphTargetItem item) {
        if (item == null) {
            return;
        }
        GraphTargetItem it = item;
        while (it instanceof GetMemberActionItem) {
            GetMemberActionItem m = (GetMemberActionItem) it;
            m.printObfuscatedMemberName = true;
            it = m.object;
        }
        if (it instanceof GetVariableActionItem) {
            GetVariableActionItem gv = (GetVariableActionItem) it;
            gv.printObfuscatedName = true;
        }
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        GraphTargetItem clsName = className;
        while (clsName instanceof GetMemberActionItem) {
            ((GetMemberActionItem) clsName).printObfuscatedMemberName = true;
            clsName = ((GetMemberActionItem) clsName).object;
        }
        if (clsName instanceof GetVariableActionItem) {
            ((GetVariableActionItem) clsName).printObfuscatedName = true;
        }

        makePrintObfuscated(extendsOp);
        for (GraphTargetItem im : implementsOp) {
            makePrintObfuscated(im);
        }

        writer.append("class ");
        className.toStringNoQuotes(writer, localData);
        if (extendsOp != null) {
            writer.append(" extends ");
            extendsOp.toStringNoQuotes(writer, localData);
        }
        if (!implementsOp.isEmpty()) {
            writer.append(" implements ");
            boolean first = true;
            for (GraphTargetItem t : implementsOp) {
                if (!first) {
                    writer.append(", ");
                }
                first = false;
                Action.getWithoutGlobal(t).toString(writer, localData);
            }
        }
        writer.startBlock();
        writer.startClass(className.toStringNoQuotes(localData));

        /*if (constructor != null) {
            constructor.toString(writer, localData).newLine();
        }*/
        for (int pass = 1; pass <= 2; pass++) {
            looptraits:
            for (int i = 0; i < traits.size(); i++) {
                MyEntry<GraphTargetItem, GraphTargetItem> item = traits.get(i);

                switch (pass) {
                    //pass 1: add variables
                    case 1:
                        if (item.getValue() instanceof FunctionActionItem) { //ignore methods
                            continue looptraits;
                        }
                        break;
                    //pass 2: add methods
                    case 2:
                        if (!(item.getValue() instanceof FunctionActionItem)) { //ignore nonmethods
                            continue looptraits;
                        }
                        break;

                }

                if (traitsStatic.get(i)) {
                    writer.append("static ");
                }
                if (item.getValue() instanceof FunctionActionItem) {
                    item.getValue().toString(writer, localData).newLine();
                } else {
                    writer.append("var ");
                    writer.append(IdentifiersDeobfuscation.printIdentifier(false, item.getKey().toStringNoQuotes(localData)));
                    if (item.getValue() != null) {
                        writer.append(" = ");
                        item.getValue().toString(writer, localData);
                    }
                    writer.append(";").newLine();
                }
            }
        }
        for (String v : uninitializedVars) {
            writer.append("var ");
            writer.append(v);
            writer.append(";").newLine();
        }
        writer.endClass();
        writer.endBlock();
        return writer;
    }

    @Override
    public List<ContinueItem> getContinues() {
        List<ContinueItem> ret = new ArrayList<>();
        return ret;
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        SourceGeneratorLocalData localData2 = Helper.deepCopy(localData);
        asGenerator.setInMethod(localData2, true);
        ret.addAll(asGenerator.generateTraits(localData2, false, className, extendsOp, implementsOp, traits, traitsStatic));
        return ret;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        for (MyEntry<GraphTargetItem, GraphTargetItem> en : traits) {
            GraphTargetItem value = en.getValue();
            if (value != null) {
                visitor.visit(value);
            }
        }
    }

}
