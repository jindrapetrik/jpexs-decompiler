/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.action.model.clauses;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.ConstantPool;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionPop;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionSetVariable;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionEquals2;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.action.swf6.ActionEnumerate2;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.ContinueItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ForInActionItem extends LoopActionItem implements Block {

    public GraphTargetItem variableName;
    public GraphTargetItem enumVariable;
    public List<GraphTargetItem> commands;

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        ret.add(commands);
        return ret;
    }

    public ForInActionItem(Action instruction, Loop loop, GraphTargetItem variableName, GraphTargetItem enumVariable, List<GraphTargetItem> commands) {
        super(instruction, loop);
        this.variableName = variableName;
        this.enumVariable = enumVariable;
        this.commands = commands;
    }

    @Override
    public String toString(boolean highlight, ConstantPool constants) {
        String ret = "";
        ret += hilight("loop" + loop.id + ":", highlight) + "\r\n";
        ret += hilight("for(", highlight) + ((variableName instanceof DirectValueActionItem) && (((DirectValueActionItem) variableName).value instanceof RegisterNumber) ? "var " : "") + stripQuotes(variableName, constants, highlight) + " in " + enumVariable.toString(highlight, constants) + ")\r\n{\r\n";
        for (GraphTargetItem ti : commands) {
            ret += ti.toStringSemicoloned(highlight, constants) + "\r\n";
        }
        ret += hilight("}", highlight) + "\r\n";
        ret += hilight(":loop" + loop.id, highlight);
        return ret;
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
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        List<GraphSourceItem> ret = new ArrayList<>();
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        HashMap<String, Integer> registerVars = asGenerator.getRegisterVars(localData);
        ret.addAll(enumVariable.toSource(localData, generator));
        ret.add(new ActionEnumerate2());

        List<Action> loopExpr = new ArrayList<>();
        int exprReg = 0;
        for (int i = 0; i < 256; i++) {
            if (!registerVars.containsValue(i)) {
                registerVars.put("__forin" + asGenerator.uniqId(), i);
                exprReg = i;
                break;
            }
        }
        int innerExprReg = asGenerator.getTempRegister(localData);
        loopExpr.add(new ActionStoreRegister(exprReg));
        loopExpr.add(new ActionPush(new Null()));
        loopExpr.add(new ActionEquals2());
        ActionIf forInEndIf = new ActionIf(0);
        loopExpr.add(forInEndIf);
        List<Action> loopBody = new ArrayList<>();
        loopBody.add(new ActionPush(new RegisterNumber(exprReg)));
        if (asGenerator.isInFunction(localData)) {
            loopBody.add(new ActionStoreRegister(innerExprReg));
            loopBody.add(new ActionPop());
        } else {
            loopBody.addAll(0, asGenerator.toActionList(variableName.toSource(localData, generator)));
            loopBody.add(new ActionSetVariable());
        }
        int oldForIn = asGenerator.getForInLevel(localData);
        asGenerator.setForInLevel(localData, oldForIn + 1);
        loopBody.addAll(asGenerator.toActionList(asGenerator.generate(localData, commands)));
        asGenerator.setForInLevel(localData, oldForIn);
        ActionJump forinJmpBack = new ActionJump(0);
        loopBody.add(forinJmpBack);
        int bodyLen = Action.actionsToBytes(loopBody, false, SWF.DEFAULT_VERSION).length;
        int exprLen = Action.actionsToBytes(loopExpr, false, SWF.DEFAULT_VERSION).length;
        forinJmpBack.setJumpOffset(-bodyLen - exprLen);
        forInEndIf.setJumpOffset(bodyLen);
        ret.addAll(loopExpr);
        ret.addAll(loopBody);
        return ret;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
