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
package com.jpexs.decompiler.flash.action.model.clauses;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.GetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.SetTypeActionItem;
import com.jpexs.decompiler.flash.action.model.StoreRegisterActionItem;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.parser.script.VariableActionItem;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionEquals2;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.action.swf6.ActionEnumerate2;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.LoopWithType;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * For in loop.
 *
 * @author JPEXS
 */
public class ForInActionItem extends LoopActionItem implements Block {

    /**
     * Variable name
     */
    public GraphTargetItem variableName;

    /**
     * Enumerated variable
     */
    public GraphTargetItem enumVariable;

    /**
     * Commands
     */
    public List<GraphTargetItem> commands;

    /**
     * Label used
     */
    private boolean labelUsed;

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        if (commands != null) {
            ret.add(commands);
        }
        return ret;
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(variableName);
        visitor.visit(enumVariable);
        visitor.visitAll(commands);
    }

    @Override
    public void visitNoBlock(GraphTargetVisitorInterface visitor) {
        visitor.visit(variableName);
        visitor.visit(enumVariable);
    }

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param loop Loop
     * @param variableName Variable name
     * @param enumVariable Enumerated variable
     * @param commands Commands
     */
    public ForInActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, Loop loop, GraphTargetItem variableName, GraphTargetItem enumVariable, List<GraphTargetItem> commands) {
        super(instruction, lineStartIns, loop);
        this.variableName = variableName;
        this.enumVariable = enumVariable;
        this.commands = commands;
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (writer instanceof NulWriter) {
            ((NulWriter) writer).startLoop(loop.id, LoopWithType.LOOP_TYPE_LOOP);
        }
        if (labelUsed) {
            writer.append("loop").append(loop.id).append(":").newLine();
        }
        writer.append("for");
        if (writer.getFormatting().spaceBeforeParenthesesForParentheses) {
            writer.append(" ");
        }
        writer.append("(");
        if (variableName instanceof SetTypeActionItem) {
            GraphTargetItem vn = ((SetTypeActionItem) variableName).getObject();

            if ((variableName instanceof StoreRegisterActionItem) && ((StoreRegisterActionItem) variableName).define) {
                writer.append("var ");
            }

            if (vn instanceof GetVariableActionItem) {
                ((GetVariableActionItem) vn).printObfuscatedName = true; //cannot use eval
            }

            stripQuotes(vn, localData, writer);
        }

        writer.append(" in ");
        if (enumVariable.getPrecedence() > PRECEDENCE_PRIMARY) {
            writer.append("(");
        }
        enumVariable.toString(writer, localData);
        if (enumVariable.getPrecedence() > PRECEDENCE_PRIMARY) {
            writer.append(")");
        }
        writer.append(")").startBlock();
        for (GraphTargetItem ti : commands) {
            ti.toStringSemicoloned(writer, localData).newLine();
        }
        writer.endBlock();
        if (writer instanceof NulWriter) {
            LoopWithType loopOjb = ((NulWriter) writer).endLoop(loop.id);
            labelUsed = loopOjb.used;
        }
        return writer;
    }

    @Override
    public List<ContinueItem> getContinues() {
        List<ContinueItem> ret = new ArrayList<>();
        for (GraphTargetItem ti : commands) {
            if (ti instanceof ContinueItem) {
                ret.add((ContinueItem) ti);
            }
            if (ti instanceof Block) {
                ret.addAll(((Block) ti).getContinues());
            }
        }
        return ret;
    }
    
    @Override
    public List<BreakItem> getBreaks() {
        List<BreakItem> ret = new ArrayList<>();
        for (GraphTargetItem ti : commands) {
            if (ti instanceof BreakItem) {
                ret.add((BreakItem) ti);
            }
            if (ti instanceof Block) {
                ret.addAll(((Block) ti).getBreaks());
            }
        }
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        String charset = asGenerator.getCharset();
        HashMap<String, Integer> registerVars = asGenerator.getRegisterVars(localData);
        ret.addAll(enumVariable.toSource(localData, generator));
        ret.add(new ActionEnumerate2());

        List<Action> loopExpr = new ArrayList<>();
        int exprReg = asGenerator.getTempRegister(localData);

        loopExpr.add(new ActionStoreRegister(exprReg, charset));
        loopExpr.add(new ActionPush(Null.INSTANCE, charset));
        loopExpr.add(new ActionEquals2());
        ActionIf forInEndIf = new ActionIf(0, charset);
        loopExpr.add(forInEndIf);
        List<Action> loopBody = new ArrayList<>();

        //assuming (variableName instanceof VariableActionItem)       
        VariableActionItem vaact = (VariableActionItem) variableName;
        GraphTargetItem setVar = vaact.getBoxedValue();
        setVar.value = new DirectValueActionItem(new RegisterNumber(exprReg));

        loopBody.addAll(asGenerator.toActionList(setVar.toSourceIgnoreReturnValue(localData, generator)));
        //loopBody.add(new ActionPush(new RegisterNumber(exprReg)));
        int oldForIn = asGenerator.getForInLevel(localData);
        asGenerator.setForInLevel(localData, oldForIn + 1);
        loopBody.addAll(asGenerator.toActionList(asGenerator.generate(localData, commands)));
        asGenerator.setForInLevel(localData, oldForIn);
        ActionJump forinJmpBack = new ActionJump(0, charset);
        loopBody.add(forinJmpBack);
        int bodyLen = Action.actionsToBytes(loopBody, false, SWF.DEFAULT_VERSION).length;
        int exprLen = Action.actionsToBytes(loopExpr, false, SWF.DEFAULT_VERSION).length;
        forinJmpBack.setJumpOffset(-bodyLen - exprLen);
        forInEndIf.setJumpOffset(bodyLen);
        ret.addAll(loopExpr);
        ret.addAll(loopBody);
        asGenerator.releaseTempRegister(localData, exprReg);
        return ret;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
