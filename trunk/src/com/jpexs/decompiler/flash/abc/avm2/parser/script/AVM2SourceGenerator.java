/*
 *  Copyright (C) 2010-2014 JPEXS
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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.NotIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.StrictEqualsIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.ConstructSuperIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewClassIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfFalseIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfStrictNeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfTrueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.LookupSwitchIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocal0Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyStrictIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetLexIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetScopeObjectIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.HasNext2Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.InitPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.LabelIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.NextNameIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.NextValueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnValueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnVoidIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.DupIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PopScopeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushByteIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushScopeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushUndefinedIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.BooleanAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FloatValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.StringAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ForEachInAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ForInAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.TryAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.IfCondition;
import com.jpexs.decompiler.flash.abc.avm2.parser.ParseException;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.ClassInfo;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.action.swf5.ActionPushDuplicate;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.AndItem;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.CommaExpressionItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.DoWhileItem;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.decompiler.graph.model.ForItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.OrItem;
import com.jpexs.decompiler.graph.model.SwitchItem;
import com.jpexs.decompiler.graph.model.TernarOpItem;
import com.jpexs.decompiler.graph.model.UnboundedTypeItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class AVM2SourceGenerator implements SourceGenerator {

    public final ABC abc;
    public List<ABC> allABCs;

    private AVM2Instruction ins(InstructionDefinition def, int... operands) {
        return new AVM2Instruction(0, def, operands, new byte[0]);
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, AndItem item) {
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.addAll(generateToActionList(localData, item.leftSide));
        ret.add(new ActionPushDuplicate());
        ret.add(ins(new NotIns()));
        List<AVM2Instruction> andExpr = generateToActionList(localData, item.rightSide);
        andExpr.add(0, ins(new PopIns()));
        int andExprLen = insToBytes(andExpr).length;
        ret.add(ins(new IfTrueIns(), andExprLen));
        ret.addAll(andExpr);
        return ret;

    }

    private byte[] insToBytes(List<AVM2Instruction> code) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (AVM2Instruction instruction : code) {

                baos.write(instruction.getBytes());

            }
            return baos.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(AVM2SourceGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new byte[0];
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, OrItem item) {
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.addAll(generateToActionList(localData, item.leftSide));
        ret.add(new ActionPushDuplicate());
        List<AVM2Instruction> orExpr = generateToActionList(localData, item.rightSide);
        orExpr.add(0, ins(new PopIns()));
        int orExprLen = insToBytes(orExpr).length;
        ret.add(ins(new IfTrueIns(), orExprLen));
        ret.addAll(orExpr);
        return ret;
    }

    public List<AVM2Instruction> toInsList(List<GraphSourceItem> items) {
        List<AVM2Instruction> ret = new ArrayList<>();
        for (GraphSourceItem s : items) {
            if (s instanceof AVM2Instruction) {
                ret.add((AVM2Instruction) s);
            }
        }
        return ret;
    }

    private List<AVM2Instruction> nonempty(List<AVM2Instruction> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    private List<GraphSourceItem> condition(SourceGeneratorLocalData localData, GraphTargetItem t, int offset){
        if(t instanceof IfCondition){
            IfCondition ic=(IfCondition)t;
            return GraphTargetItem.toSourceMerge(localData, this, ic.getLeftSide(),ic.getRightSide(),ins(ic.getIfDefinition(),offset));
        }
        return GraphTargetItem.toSourceMerge(localData, this, t, ins(new IfTrueIns(),offset));
    }
    
    private List<GraphSourceItem> notCondition(SourceGeneratorLocalData localData, GraphTargetItem t, int offset){
        if(t instanceof IfCondition){
            IfCondition ic=(IfCondition)t;
            return GraphTargetItem.toSourceMerge(localData, this, ic.getLeftSide(),ic.getRightSide(),ins(ic.getIfNotDefinition(),offset));
        }
        return GraphTargetItem.toSourceMerge(localData, this, t, ins(new IfFalseIns(),offset));
    }
    
    private List<GraphSourceItem> generateIf(SourceGeneratorLocalData localData, GraphTargetItem expression, List<GraphTargetItem> onTrueCmds, List<GraphTargetItem> onFalseCmds, boolean ternar) {
        List<GraphSourceItem> ret = new ArrayList<>();
        //ret.addAll(notCondition(localData, expression));        
        List<AVM2Instruction> onTrue = null;
        List<AVM2Instruction> onFalse = null;
        if (ternar) {
            onTrue = toInsList(onTrueCmds.get(0).toSource(localData, this));
        } else {
            onTrue = generateToActionList(localData, onTrueCmds);
        }

        if (onFalseCmds != null && !onFalseCmds.isEmpty()) {
            if (ternar) {
                onFalse = toInsList(onFalseCmds.get(0).toSource(localData, this));
            } else {
                onFalse = generateToActionList(localData, onFalseCmds);
            }
        }
        AVM2Instruction ajmp = null;
        if (onFalse != null) {
            if (!((!nonempty(onTrue).isEmpty())
                    && ((onTrue.get(onTrue.size() - 1).definition instanceof ContinueJumpIns)
                    || ((onTrue.get(onTrue.size() - 1).definition instanceof BreakJumpIns))))) {
                ajmp = ins(new JumpIns(), 0);
                onTrue.add(ajmp);
            }
        }
        
        byte[] onTrueBytes = insToBytes(onTrue);
        int onTrueLen = onTrueBytes.length;

        ret.addAll(notCondition(localData, expression, onTrueLen));
        ret.addAll(onTrue);        
        
        if (onFalse != null) {
            byte[] onFalseBytes = insToBytes(onFalse);
            int onFalseLen = onFalseBytes.length;
            if (ajmp != null) {
                ajmp.operands[0] = onFalseLen;
            }
            ret.addAll(onFalse);
        }
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, IfItem item) {
        return generateIf(localData, item.expression, item.onTrue, item.onFalse, false);
    }

    private void fixSwitch(List<AVM2Instruction> code, int breakOffset, long loopId) {
        fixLoop(code, breakOffset, Integer.MAX_VALUE, loopId);
    }

    private void fixLoop(List<AVM2Instruction> code, int breakOffset, int continueOffset, long loopId) {
        int pos = 0;
        for (int a = 0; a < code.size(); a++) {
            AVM2Instruction ins = code.get(a);
            pos += ins.getBytes().length;
            if (ins.definition instanceof JumpIns) {
                if (ins.definition instanceof ContinueJumpIns) {
                    if (continueOffset != Integer.MAX_VALUE) {
                        ins.operands[0] = (-pos + continueOffset);
                    }
                    code.get(a).definition = new JumpIns();
                }
                if (ins.definition instanceof BreakJumpIns) {
                    ins.operands[0] = (-pos + breakOffset);
                    code.get(a).definition = new JumpIns();
                }
            }
        }
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, TernarOpItem item) {
        List<GraphTargetItem> onTrue = new ArrayList<>();
        onTrue.add(item.onTrue);
        List<GraphTargetItem> onFalse = new ArrayList<>();
        onFalse.add(item.onFalse);
        return generateIf(localData, item.expression, onTrue, onFalse, true);
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, WhileItem item) {
        List<GraphSourceItem> ret = new ArrayList<>();
        List<AVM2Instruction> whileExpr = new ArrayList<>();

        List<GraphTargetItem> ex = new ArrayList<>(item.expression);
        GraphTargetItem lastItem = null;
        if (!ex.isEmpty()) {            
            lastItem = ex.remove(ex.size() - 1);
            while(lastItem instanceof CommaExpressionItem){
                CommaExpressionItem cei=(CommaExpressionItem)lastItem;
                ex.addAll(cei.commands);
                lastItem = ex.remove(ex.size() - 1);
            }
            whileExpr.addAll(generateToActionList(localData, ex));
        }
        List<AVM2Instruction> whileBody = generateToActionList(localData, item.commands);        
        AVM2Instruction forwardJump = ins(new JumpIns(),0);
        ret.add(forwardJump);
        whileBody.add(0,ins(new LabelIns()));
        ret.addAll(whileBody);
        int whileBodyLen = insToBytes(whileBody).length;
        forwardJump.operands[0] = whileBodyLen;
        whileExpr.addAll(toInsList(condition(localData, lastItem, 0)));
        int whileExprLen = insToBytes(whileExpr).length;
        whileExpr.get(whileExpr.size()-1).operands[0] = -(whileExprLen+whileBodyLen); //Assuming last is if instruction
        ret.addAll(whileExpr);
        fixLoop(whileBody, whileBodyLen + whileExprLen, whileBodyLen, item.loop.id);
        return ret;
    }
    
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ForEachInAVM2Item item) {
        return generateForIn(localData,item.expression.collection,(AssignableAVM2Item)item.expression.object,item.commands,true);
    }
    
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ForInAVM2Item item) {
        return generateForIn(localData,item.expression.collection,(AssignableAVM2Item)item.expression.object,item.commands,false);
    }
    
    public List<GraphSourceItem> generateForIn(SourceGeneratorLocalData localData,GraphTargetItem collection, AssignableAVM2Item assignable,List<GraphTargetItem> commands, final boolean each) {
        List<GraphSourceItem> ret = new ArrayList<>();
        final Reference<Integer> counterReg = new Reference<>(0);
        final Reference<Integer> collectionReg = new Reference<>(0);
        
        ret.addAll(GraphTargetItem.toSourceMerge(localData, this, 
                ins(new PushByteIns(),0),
                AssignableAVM2Item.setTemp(localData, this, counterReg),
                collection,
                NameAVM2Item.generateCoerce(this, "*"),
                AssignableAVM2Item.setTemp(localData, this, collectionReg)                
                ));
                
        
        GraphTargetItem assigned=new GraphTargetItem() {

            @Override
            public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
                return null;
            }

            @Override
            public boolean hasReturnValue() {
                return true;
            }

            @Override
            public GraphTargetItem returnType() {
                return TypeItem.UNBOUNDED;
            }

            @Override
            public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) {
                return toSourceMerge(localData, generator, 
                AssignableAVM2Item.getTemp(localData, generator, collectionReg),
                AssignableAVM2Item.getTemp(localData, generator, counterReg),
                ins(each?new NextValueIns():new NextNameIns())
                );
            }                        
        };
        assignable.setAssignedValue(assigned);
        
        List<AVM2Instruction> forBody = toInsList(GraphTargetItem.toSourceMerge(localData, this, 
                ins(new LabelIns()),
                assignable.toSourceIgnoreReturnValue(localData, this)
                ));
               
        forBody.addAll(generateToActionList(localData, commands));
        int forBodyLen = insToBytes(forBody).length;
        
        AVM2Instruction forwardJump = ins(new JumpIns(),forBodyLen);
        ret.add(forwardJump);
        
        List<AVM2Instruction> expr = new ArrayList<>();
        expr.add(ins(new HasNext2Ins(),collectionReg.getVal(),counterReg.getVal()));
        AVM2Instruction backIf = ins(new IfTrueIns(),0);
        expr.add(backIf);
        
        int exprLen = insToBytes(expr).length;
        backIf.operands[0] = -(exprLen + forBodyLen);
        
        ret.addAll(forBody);
        ret.addAll(expr);
        ret.addAll(AssignableAVM2Item.killTemp(localData, this, Arrays.asList(collectionReg,counterReg)));
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, DoWhileItem item) {
        List<GraphSourceItem> ret = new ArrayList<>();
        List<AVM2Instruction> whileExpr = new ArrayList<>();

        List<GraphTargetItem> ex = new ArrayList<>(item.expression);
        GraphTargetItem lastItem = null;
        if (!ex.isEmpty()) {            
            lastItem = ex.remove(ex.size() - 1);
            while(lastItem instanceof CommaExpressionItem){
                CommaExpressionItem cei=(CommaExpressionItem)lastItem;
                ex.addAll(cei.commands);
                lastItem = ex.remove(ex.size() - 1);
            }
            whileExpr.addAll(generateToActionList(localData, ex));
        }
        List<AVM2Instruction> dowhileBody = generateToActionList(localData, item.commands);        
        List<AVM2Instruction> labelBody = new ArrayList<>();
        labelBody.add(ins(new LabelIns()));
        int labelBodyLen = insToBytes(labelBody).length;
        
        AVM2Instruction forwardJump = ins(new JumpIns(),labelBodyLen);
        ret.add(forwardJump);
        ret.addAll(labelBody);        
        ret.addAll(dowhileBody);        
        int dowhileBodyLen = insToBytes(dowhileBody).length;
        whileExpr.addAll(toInsList(condition(localData, lastItem, 0)));
        int dowhileExprLen = insToBytes(whileExpr).length;
        whileExpr.get(whileExpr.size()-1).operands[0] = -(dowhileExprLen+dowhileBodyLen+labelBodyLen); //Assuming last is if instruction
        ret.addAll(whileExpr);
        fixLoop(dowhileBody, dowhileBodyLen + dowhileExprLen, dowhileBodyLen, item.loop.id);
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ForItem item) {
        List<GraphSourceItem> ret = new ArrayList<>();
        List<AVM2Instruction> forExpr = new ArrayList<>();

        List<GraphTargetItem> ex = new ArrayList<>();       
        ex.add(item.expression);
        
        GraphTargetItem lastItem = null;
        if (!ex.isEmpty()) {            
            lastItem = ex.remove(ex.size() - 1);
            while(lastItem instanceof CommaExpressionItem){
                CommaExpressionItem cei=(CommaExpressionItem)lastItem;
                ex.addAll(cei.commands);
                lastItem = ex.remove(ex.size() - 1);
            }
            forExpr.addAll(generateToActionList(localData, ex));
        }
        List<AVM2Instruction> forBody = generateToActionList(localData, item.commands);       
        List<AVM2Instruction> forFinalCommands = generateToActionList(localData, item.finalCommands);       
        
        
        ret.addAll(generateToActionList(localData, item.firstCommands));   
        
        AVM2Instruction forwardJump = ins(new JumpIns(),0);
        ret.add(forwardJump);
        forBody.add(0,ins(new LabelIns()));
        ret.addAll(forBody);
        ret.addAll(forFinalCommands);
        int forBodyLen = insToBytes(forBody).length;
        int forFinalCLen = insToBytes(forFinalCommands).length;
        forwardJump.operands[0] = forBodyLen+forFinalCLen;
        forExpr.addAll(toInsList(condition(localData, lastItem, 0)));
        int forExprLen = insToBytes(forExpr).length;
        forExpr.get(forExpr.size()-1).operands[0] = -(forExprLen+forBodyLen+forFinalCLen); //Assuming last is if instruction
        ret.addAll(forExpr);
        fixLoop(forBody, forBodyLen + forFinalCLen + forExprLen, forBodyLen, item.loop.id);
        return ret;
    }
    private long uniqLast = 0;

    public String uniqId() {
        uniqLast++;
        return "" + uniqLast;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, SwitchItem item) {
        List<GraphSourceItem> ret = new ArrayList<>();
        Reference<Integer> switchedReg = new Reference<>(0);
        AVM2Instruction forwardJump = ins(new JumpIns(),0);
        ret.add(forwardJump);
       

        List<AVM2Instruction> cases=new ArrayList<>();
        cases.addAll(toInsList(new IntegerValueAVM2Item(null, (long)item.caseValues.size()).toSource(localData, this)));   
        int cLen = insToBytes(cases).length;
        List<AVM2Instruction> caseLast=new ArrayList<>();
        caseLast.add(0,ins(new JumpIns(),cLen));
        caseLast.addAll(0,toInsList(new IntegerValueAVM2Item(null, (long)item.caseValues.size()).toSource(localData, this)));   
        int cLastLen = insToBytes(caseLast).length;
        caseLast.add(0,ins(new JumpIns(),cLastLen));
        cases.addAll(0,caseLast);
        
        List<AVM2Instruction> preCases=new ArrayList<>();        
        preCases.addAll(toInsList(item.switchedObject.toSource(localData, this)));
        preCases.addAll(toInsList(AssignableAVM2Item.setTemp(localData, this, switchedReg)));
        
        for(int i=item.caseValues.size()-1;i>=0;i--){            
            List<AVM2Instruction> sub=new ArrayList<>();            
            sub.addAll(toInsList(new IntegerValueAVM2Item(null, (long)i).toSource(localData, this)));          
            sub.add(ins(new JumpIns(),insToBytes(cases).length));
            int subLen = insToBytes(sub).length;
            
            cases.addAll(0,sub);
            cases.add(0,ins(new IfStrictNeIns(), subLen));
            cases.addAll(0,toInsList(AssignableAVM2Item.getTemp(localData, this, switchedReg)));            
            cases.addAll(0,toInsList(item.caseValues.get(i).toSource(localData, this)));                        
        }
        cases.addAll(0,preCases);
                
        
        
        AVM2Instruction lookupOp = new AVM2Instruction(0, new LookupSwitchIns(), new int[item.caseValues.size()+1+1+1], new byte[0]);
        cases.addAll(toInsList(AssignableAVM2Item.killTemp(localData, this, Arrays.asList(switchedReg))));        
        List<AVM2Instruction> bodies = new ArrayList<>();
        List<Integer> bodiesOffsets=new ArrayList<>();
        int defOffset;
        int casesLen = insToBytes(cases).length;
        bodies.addAll(generateToActionList(localData, item.defaultCommands));
        bodies.add(0,ins(new LabelIns()));
        bodies.add(ins(new BreakJumpIns(item.loop.id),0));  //There could be two breaks when default clause ends with break, but official compiler does this too, so who cares...
        defOffset = -(insToBytes(bodies).length + casesLen);
        for(int i=item.caseCommands.size()-1;i>=0;i--){            
            bodies.addAll(0,generateToActionList(localData, item.caseCommands.get(i)));
            bodies.add(0,ins(new LabelIns()));
            bodiesOffsets.add(0,-(insToBytes(bodies).length + casesLen));
        }
        lookupOp.operands[0] = defOffset;        
        lookupOp.operands[1] = item.valuesMapping.size();
        lookupOp.operands[2 + item.caseValues.size()] = defOffset;
        for(int i=0;i<item.valuesMapping.size();i++){         
            lookupOp.operands[2 + i] = bodiesOffsets.get(item.valuesMapping.get(i));
        }
        
        forwardJump.operands[0] = insToBytes(bodies).length;
        ret.addAll(bodies);
        ret.addAll(cases);
        ret.add(lookupOp);
        fixSwitch(toInsList(ret), insToBytes(toInsList(ret)).length, uniqLast);
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, NotItem item) {
        /*if (item.getOriginal() instanceof Inverted) {
         GraphTargetItem norig = ((Inverted) item).invert();
         return norig.toSource(localData, this);
         }*/
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.addAll(item.getOriginal().toSource(localData, this));
        ret.add(ins(new NotIns()));
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, DuplicateItem item) {
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.add(new ActionPushDuplicate());
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, BreakItem item) {
        List<GraphSourceItem> ret = new ArrayList<>();
        AVM2Instruction abreak = ins(new BreakJumpIns(item.loopId), 0);
        ret.add(abreak);
        return ret;
    }

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, TryAVM2Item item) {
        List<GraphSourceItem> ret = new ArrayList<>();
        for (NameAVM2Item e : item.catchExceptions2) {
            ABCException aex = new ABCException();
            aex.name_index = str(e.getVariableName());
            aex.type_index = typeName(localData, e.type);
            localData.exceptions.add(aex);
            //TODO
        }
        //TODO: finally
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ContinueItem item) {
        List<GraphSourceItem> ret = new ArrayList<>();
        AVM2Instruction acontinue = ins(new ContinueJumpIns(item.loopId), 0);
        ret.add(acontinue);
        return ret;
    }

    private List<AVM2Instruction> generateToActionList(SourceGeneratorLocalData localData, List<GraphTargetItem> commands) {
        return toInsList(generate(localData, commands));
    }

    private List<AVM2Instruction> generateToActionList(SourceGeneratorLocalData localData, GraphTargetItem command) {
        return toInsList(command.toSource(localData, this));
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, List<GraphTargetItem> commands) {
        List<GraphSourceItem> ret = new ArrayList<>();
        for (GraphTargetItem item : commands) {
            ret.addAll(item.toSourceIgnoreReturnValue(localData, this));
        }
        return ret;
    }

    public HashMap<String, Integer> getRegisterVars(SourceGeneratorLocalData localData) {
        return localData.registerVars;
    }

    public void setRegisterVars(SourceGeneratorLocalData localData, HashMap<String, Integer> value) {
        localData.registerVars = value;
    }

    public void setInFunction(SourceGeneratorLocalData localData, int value) {
        localData.inFunction = value;
    }

    public int isInFunction(SourceGeneratorLocalData localData) {
        return localData.inFunction;
    }

    public boolean isInMethod(SourceGeneratorLocalData localData) {
        return localData.inMethod;
    }

    public void setInMethod(SourceGeneratorLocalData localData, boolean value) {
        localData.inMethod = value;
    }

    public int getForInLevel(SourceGeneratorLocalData localData) {
        return localData.forInLevel;
    }

    public void setForInLevel(SourceGeneratorLocalData localData, int value) {
        localData.forInLevel = value;
    }

    public int getTempRegister(SourceGeneratorLocalData localData) {
        HashMap<String, Integer> registerVars = getRegisterVars(localData);
        int tmpReg = 0;
        for (int i = 0; i < 256; i++) {
            if (!registerVars.containsValue(i)) {
                tmpReg = i;
                break;
            }
        }
        return tmpReg;
    }

    public AVM2SourceGenerator(ABC abc, List<ABC> allABCs) {
        this.abc = abc;
        this.allABCs = allABCs;
    }

    public ABC getABC() {
        return abc;
    }

    public void generateClass(int namespace, int initScope, PackageAVM2Item pkg, ClassInfo classInfo, InstanceInfo instanceInfo, SourceGeneratorLocalData localData, boolean isInterface, String name, String superName, GraphTargetItem extendsVal, List<GraphTargetItem> implementsStr, GraphTargetItem constructor, List<GraphTargetItem> traitItems) throws ParseException {
        localData.currentClass = pkg.packageName.isEmpty() ? name : pkg.packageName + "." + name;
        List<GraphSourceItem> ret = new ArrayList<>();
        if (extendsVal == null && !isInterface) {
            extendsVal = new TypeItem("Object");
        }
        ParsedSymbol s = null;

        Trait[] it = generateTraitsPhase1(pkg, name, superName, false, localData, traitItems, instanceInfo.instance_traits);
        Trait[] st = generateTraitsPhase1(pkg, name, superName, true, localData, traitItems, classInfo.static_traits);
        generateTraitsPhase2(initScope, pkg, name, superName, false, localData, traitItems, instanceInfo.instance_traits, it);
        generateTraitsPhase2(initScope, pkg, name, superName, true, localData, traitItems, classInfo.static_traits, st);
        if (constructor == null) {
            instanceInfo.iinit_index = method(initScope + 1, false, false, 0, name, extendsVal != null ? extendsVal.toString() : null, true, localData, new ArrayList<GraphTargetItem>(), new ArrayList<String>(), new ArrayList<GraphTargetItem>(), new ArrayList<GraphTargetItem>(), TypeItem.UNBOUNDED/*?? FIXME*/);
        } else {
            MethodAVM2Item m = (MethodAVM2Item) constructor;
            Reference<Boolean> hasArgs = new Reference<>(Boolean.FALSE);
            calcRegisters(localData, m, hasArgs);
            instanceInfo.iinit_index = method(initScope + 1, m.hasRest, hasArgs.getVal(), m.line, name, extendsVal != null ? extendsVal.toString() : null, true, localData, m.paramTypes, m.paramNames, m.paramValues, m.body, TypeItem.UNBOUNDED/*?? FIXME*/);
        }

        //Class initializer
        MethodInfo mi = new MethodInfo(new int[0], 0, 0, 0, new ValueKind[0], new int[0]);
        MethodBody mb = new MethodBody();
        mb.method_info = abc.addMethodInfo(mi);
        mb.code = new AVM2Code();
        mb.code.code = new ArrayList<>();
        mb.code.code.add(ins(new GetLocal0Ins()));
        mb.code.code.add(ins(new PushScopeIns()));
        for (GraphTargetItem ti : traitItems) {
            if (ti instanceof SlotAVM2Item) {
                SlotAVM2Item si = (SlotAVM2Item) ti;
                if (si.isStatic()) {
                    mb.code.code.add(ins(new FindPropertyStrictIns(), traitName(namespace, si.var)));
                    List<GraphTargetItem> tis = new ArrayList<>();
                    tis.add(si.value);
                    mb.code.code.addAll(toInsList(generate(localData, tis)));
                    mb.code.code.add(ins(new InitPropertyIns(), traitName(namespace, si.var)));
                }
            }
        }
        mb.code.code.add(ins(new ReturnVoidIns()));
        mb.autoFillStats(abc, initScope);

        abc.addMethodBody(mb);

        classInfo.cinit_index = mb.method_info;

        instanceInfo.interfaces = new int[implementsStr.size()];
        for (int i = 0; i < implementsStr.size(); i++) {
            instanceInfo.interfaces[i] = typeName(localData, implementsStr.get(i));
        }

    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, CommaExpressionItem item) {
        if (item.commands.isEmpty()) {
            return new ArrayList<>();
        }

        //We need to handle commands and last expression separately, otherwise last expression result will be popped
        List<GraphTargetItem> cmds = new ArrayList<>(item.commands);
        GraphTargetItem lastExpr = cmds.remove(cmds.size() - 1);
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.addAll(generate(localData, cmds));
        ret.addAll(lastExpr.toSource(localData, this));
        return ret;
    }

    public int generateClass(int namespace, ClassInfo ci, InstanceInfo ii, int initScope, PackageAVM2Item pkg, SourceGeneratorLocalData localData, AVM2Item cls) throws ParseException {
        /*ClassInfo ci = new ClassInfo();
         InstanceInfo ii = new InstanceInfo();
         abc.class_info.add(ci);
         abc.instance_info.add(ii);
         */
        if (cls instanceof ClassAVM2Item) {
            ClassAVM2Item cai = (ClassAVM2Item) cls;
            generateClass(namespace, initScope, pkg, ci, ii, localData, false, cai.className, cai.extendsOp.toString(), cai.extendsOp, cai.implementsOp, cai.constructor, cai.traits);
            if (!cai.isDynamic) {
                ii.flags |= InstanceInfo.CLASS_SEALED;
            }
            if (cai.isFinal) {
                ii.flags |= InstanceInfo.CLASS_FINAL;
            }
            ii.flags |= InstanceInfo.CLASS_PROTECTEDNS;
            ii.protectedNS = cai.protectedNs;
        }
        if (cls instanceof InterfaceAVM2Item) {
            InterfaceAVM2Item iai = (InterfaceAVM2Item) cls;
            generateClass(namespace, initScope, pkg, ci, ii, localData, true, iai.name, null, null, iai.superInterfaces, null, iai.methods);
            ii.flags |= InstanceInfo.CLASS_INTERFACE;
        }

        return abc.instance_info.size() - 1;
    }

    public int traitName(int namespace, String var) {
        return abc.constants.getMultinameId(new Multiname(Multiname.QNAME, str(var), namespace, 0, 0, new ArrayList<Integer>()), true);
    }

    public int typeName(SourceGeneratorLocalData localData, GraphTargetItem type) {
        if (type instanceof UnboundedTypeItem) {
            return 0;
        }

        String pkg = "";
        String name = type.toString();
        TypeItem nameItem = (TypeItem) type;
        if (name.contains(".")) {
            pkg = name.substring(0, name.lastIndexOf('.'));
            name = name.substring(name.lastIndexOf('.') + 1);
        }

        /*int nsKind = Namespace.KIND_PACKAGE;
         if (pkg.equals("")) {
         for (int i = 0; i < nameItem.openedNamespaces.size(); i++) {
         String ns = nameItem.openedNamespaces.get(i);
         String nspkg = ns;
         String nsclass = null;
         if (nspkg.contains(":")) {
         nsclass = nspkg.substring(nspkg.indexOf(":") + 1);
         nspkg = nspkg.substring(0, nspkg.indexOf(":"));
         }
         if (nsclass == null) {
         List<ABC> abcs = new ArrayList<>();
         abcs.add(abc);
         abcs.addAll(allABCs);
         loopabc:
         for (ABC a : abcs) {
         for (InstanceInfo ii : a.instance_info) {
         Multiname n = a.constants.constant_multiname.get(ii.name_index);
         if (n.getNamespace(a.constants).getName(a.constants).equals(nspkg) && n.getName(a.constants, new ArrayList<String>()).equals(name)) {
         pkg = nspkg;
         nsKind = n.getNamespace(a.constants).kind;
         break loopabc;
         }
         }
         }
         } else if (name.equals(nsclass)) {
         pkg = nspkg;
         nsKind = nameItem.openedNamespacesKind.get(i);
         break;
         }
         }
         }*/
        return abc.constants.getMultinameId(new Multiname(Multiname.QNAME, str(name), namespace(Namespace.KIND_PACKAGE/*?*/, pkg), 0, 0, new ArrayList<Integer>()), true);
    }

    public int ident(GraphTargetItem name) {
        if (name instanceof NameAVM2Item) {
            return str(((NameAVM2Item) name).getVariableName());
        }
        throw new RuntimeException("no ident"); //FIXME
    }

    public int namespace(int nsKind, String name) {
        return abc.constants.getNamespaceId(new Namespace(nsKind, str(name)), 0, true);
    }

    public int str(String name) {
        return abc.constants.getStringId(name, true);
    }

    public int propertyName(GraphTargetItem name) {
        if (name instanceof NameAVM2Item) {
            NameAVM2Item va = (NameAVM2Item) name;
            return abc.constants.getMultinameId(new Multiname(Multiname.QNAME, str(va.getVariableName()), namespace(Namespace.KIND_PACKAGE, ""), 0, 0, new ArrayList<Integer>()), true);
        }
        throw new RuntimeException("no prop"); //FIXME
    }

    public int getFreeRegister(SourceGeneratorLocalData localData) {
        for (int i = 0;; i++) {
            if (!localData.registerVars.containsValue(i)) {
                localData.registerVars.put("__TEMP__" + i, i);
                return i;
            }
        }
    }

    public boolean killRegister(SourceGeneratorLocalData localData, int i) {
        String key = null;
        for (String k : localData.registerVars.keySet()) {
            if (localData.registerVars.get(k) == i) {
                key = k;
                break;
            }
        }
        if (key != null) {
            localData.registerVars.remove(key);
            return true;
        }
        return false;
    }

    public int method(int initScope, boolean hasRest, boolean hasArguments, int line, String className, String superType, boolean constructor, SourceGeneratorLocalData localData, List<GraphTargetItem> paramTypes, List<String> paramNames, List<GraphTargetItem> paramValues, List<GraphTargetItem> body, GraphTargetItem retType) throws ParseException {
        int param_types[] = new int[paramTypes.size()];
        ValueKind optional[] = new ValueKind[paramValues.size()];
        int param_names[] = new int[paramNames.size()];
        for (int i = 0; i < paramTypes.size(); i++) {
            param_types[i] = typeName(localData, paramTypes.get(i));
            param_names[i] = str(paramNames.get(i));
        }

        for (int i = 0; i < paramValues.size(); i++) {
            optional[i] = getValueKind(Namespace.KIND_NAMESPACE/*FIXME*/, paramTypes.get(paramTypes.size() - paramValues.size() + i), paramTypes.get(i));
        }

        MethodInfo mi = new MethodInfo(param_types, constructor ? 0 : typeName(localData, retType), 0/*name_index*/, 0, optional, param_names);
        if (hasArguments) {
            mi.setFlagNeed_Arguments();
        }
        if (!paramNames.isEmpty()) {
            mi.setFlagHas_paramnames();
        }
        if (!paramValues.isEmpty()) {
            mi.setFlagHas_optional();
        }
        if (hasRest) {
            mi.setFlagNeed_rest();
        }

        MethodBody mbody = new MethodBody();
        mbody.method_info = abc.addMethodInfo(mi);
        List<GraphSourceItem> src = generate(localData, body);
        mbody.code = new AVM2Code();
        mbody.code.code = toInsList(src);

        if (constructor) {
            List<ABC> abcs = new ArrayList<>();
            abcs.add(abc);
            abcs.addAll(allABCs);

            int parentConsAC = 0;

            for (ABC a : abcs) {
                int ci = a.findClassByName(superType);
                if (ci > -1) {
                    MethodInfo pmi = a.method_info.get(a.instance_info.get(ci).iinit_index);
                    parentConsAC = pmi.param_types.length;

                }
            }
            int ac = -1;
            for (AVM2Instruction ins : mbody.code.code) {
                if (ins.definition instanceof ConstructSuperIns) {
                    ac = ins.operands[0];
                    if (parentConsAC != ac) {
                        throw new ParseException("Parent constructor call requires different number of arguments", line);
                    }

                }
            }
            if (ac == -1) {
                if (parentConsAC == 0) {
                    mbody.code.code.add(0, new AVM2Instruction(0, new GetLocal0Ins(), new int[]{}, new byte[0]));
                    mbody.code.code.add(1, new AVM2Instruction(0, new ConstructSuperIns(), new int[]{0}, new byte[0]));

                } else {
                    throw new ParseException("Parent constructor must be called", line);
                }
            }
        }
        mbody.code.code.add(0, new AVM2Instruction(0, new GetLocal0Ins(), new int[]{}, new byte[0]));
        mbody.code.code.add(1, new AVM2Instruction(0, new PushScopeIns(), new int[]{}, new byte[0]));

        InstructionDefinition lastDef = mbody.code.code.get(mbody.code.code.size() - 1).definition;
        if (!((lastDef instanceof ReturnVoidIns) || (lastDef instanceof ReturnValueIns))) {
            if (retType.toString().equals("*") || retType.toString().equals("void") || constructor) {
                mbody.code.code.add(new AVM2Instruction(0, new ReturnVoidIns(), new int[]{}, new byte[0]));
            } else {
                mbody.code.code.add(new AVM2Instruction(0, new PushUndefinedIns(), new int[]{}, new byte[0]));
                mbody.code.code.add(new AVM2Instruction(0, new ReturnValueIns(), new int[]{}, new byte[0]));
            }
        }
        mbody.autoFillStats(abc, initScope);

        abc.addMethodBody(mbody);
        //TODO: Exceptions
        return mbody.method_info;
    }

    public ValueKind getValueKind(int nsKind, GraphTargetItem type, GraphTargetItem val) {

        if (val instanceof BooleanAVM2Item) {
            BooleanAVM2Item bi = (BooleanAVM2Item) val;
            if (bi.value) {
                return new ValueKind(0, ValueKind.CONSTANT_True);
            } else {
                return new ValueKind(0, ValueKind.CONSTANT_False);
            }
        }

        boolean isNs = false;
        if (type instanceof NameAVM2Item) {
            if (((NameAVM2Item) type).getVariableName().equals("namespace")) {
                isNs = true;
            }
        }

        if ((type instanceof TypeItem) && (((TypeItem) type).fullTypeName.equals("Namespace"))) {
            isNs = true;
        }

        if (val instanceof StringAVM2Item) {
            StringAVM2Item sval = (StringAVM2Item) val;
            if (isNs) {
                return new ValueKind(namespace(nsKind, sval.value), Namespace.KIND_NAMESPACE);
            } else {
                return new ValueKind(str(sval.value), ValueKind.CONSTANT_Utf8);
            }
        }
        if (val instanceof IntegerValueAVM2Item) {
            return new ValueKind(abc.constants.getIntId(((IntegerValueAVM2Item) val).value, true), ValueKind.CONSTANT_Int);
        }
        if (val instanceof FloatValueAVM2Item) {
            return new ValueKind(abc.constants.getIntId(((IntegerValueAVM2Item) val).value, true), ValueKind.CONSTANT_Double);
        }
        if (val instanceof NullAVM2Item) {
            return new ValueKind(0, ValueKind.CONSTANT_Null);
        }
        if (val instanceof UndefinedAVM2Item) {
            return new ValueKind(0, ValueKind.CONSTANT_Undefined);
        }
        return null;
    }

    public void generateTraitsPhase2(int initScope, PackageAVM2Item pkg, String className, String superName, boolean generateStatic, SourceGeneratorLocalData localData, List<GraphTargetItem> items, Traits ts, Trait[] traits) throws ParseException {
        //Note: Names must be generated first before accesed in inner subs
        for (int k = 0; k < items.size(); k++) {
            GraphTargetItem item = items.get(k);
            if (traits[k] == null) {
                continue;
            }
            if (item instanceof InterfaceAVM2Item) {
                generateClass(((InterfaceAVM2Item) item).namespace, abc.class_info.get(((TraitClass) traits[k]).class_info), abc.instance_info.get(((TraitClass) traits[k]).class_info), initScope, pkg, localData, (InterfaceAVM2Item) item);
            }

            if (item instanceof ClassAVM2Item) {
                generateClass(((ClassAVM2Item) item).namespace, abc.class_info.get(((TraitClass) traits[k]).class_info), abc.instance_info.get(((TraitClass) traits[k]).class_info), initScope, pkg, localData, (ClassAVM2Item) item);
            }
            if ((item instanceof MethodAVM2Item) || (item instanceof GetterAVM2Item) || (item instanceof SetterAVM2Item)) {
                MethodAVM2Item mai = (MethodAVM2Item) item;
                if (mai.isStatic() != generateStatic) {
                    continue;
                }
                Reference<Boolean> hasArgs = new Reference<>(Boolean.FALSE);
                calcRegisters(localData, mai, hasArgs);
                ((TraitMethodGetterSetter) traits[k]).method_info = method(initScope + 1/*class scope*/, mai.hasRest, hasArgs.getVal(), mai.line, className, superName, false, localData, mai.paramTypes, mai.paramNames, mai.paramValues, mai.body, mai.retType);
            } else if (item instanceof FunctionAVM2Item) {
                FunctionAVM2Item fai = (FunctionAVM2Item) item;
                Reference<Boolean> hasArgs = new Reference<>(Boolean.FALSE);
                calcRegisters(localData, fai, hasArgs);
                ((TraitFunction) traits[k]).method_info = method(initScope, fai.hasRest, hasArgs.getVal(), fai.line, className, superName, false, localData, fai.paramTypes, fai.paramNames, fai.paramValues, fai.body, fai.retType);
            }
        }
    }

    public Trait[] generateTraitsPhase1(PackageAVM2Item pkg, String className, String superName, boolean generateStatic, SourceGeneratorLocalData localData, List<GraphTargetItem> items, Traits ts) throws ParseException {
        Trait[] traits = new Trait[items.size()];
        int slot_id = 1;
        for (int k = 0; k < items.size(); k++) {
            GraphTargetItem item = items.get(k);
            if (item instanceof InterfaceAVM2Item) {
                TraitClass tc = new TraitClass();
                ClassInfo ci = new ClassInfo();
                InstanceInfo ii = new InstanceInfo();
                abc.class_info.add(ci);
                abc.instance_info.add(ii);
                tc.class_info = abc.instance_info.size() - 1;
                tc.kindType = Trait.TRAIT_CLASS;
                tc.name_index = traitName(((InterfaceAVM2Item) item).namespace, ((InterfaceAVM2Item) item).name);
                tc.slot_id = slot_id++;
                ts.traits.add(tc);
                traits[k] = tc;
            }

            if (item instanceof ClassAVM2Item) {
                TraitClass tc = new TraitClass();
                ClassInfo ci = new ClassInfo();
                InstanceInfo instanceInfo = new InstanceInfo();
                abc.class_info.add(ci);
                abc.instance_info.add(instanceInfo);
                tc.class_info = abc.instance_info.size() - 1;

                instanceInfo.name_index = abc.constants.addMultiname(new Multiname(Multiname.QNAME, abc.constants.getStringId(((ClassAVM2Item) item).className, true),
                        abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE, abc.constants.getStringId(pkg.packageName, true)), 0, true), 0, 0, new ArrayList<Integer>()));

                if (((ClassAVM2Item) item).extendsOp != null) {
                    instanceInfo.super_index = typeName(localData, ((ClassAVM2Item) item).extendsOp);
                } else if (true) { //(!isInterface) {
                    instanceInfo.super_index = abc.constants.getMultinameId(new Multiname(Multiname.QNAME, str("Object"), namespace(Namespace.KIND_PACKAGE, ""), 0, 0, new ArrayList<Integer>()), true);
                }

                tc.kindType = Trait.TRAIT_CLASS;
                tc.name_index = traitName(((ClassAVM2Item) item).namespace, ((ClassAVM2Item) item).className);
                tc.slot_id = slot_id++;
                ts.traits.add(tc);
                traits[k] = tc;

            }
            if ((item instanceof SlotAVM2Item) || (item instanceof ConstAVM2Item)) {
                TraitSlotConst tsc = new TraitSlotConst();
                tsc.kindType = (item instanceof SlotAVM2Item) ? Trait.TRAIT_SLOT : Trait.TRAIT_CONST;
                String var = null;
                GraphTargetItem val = null;
                GraphTargetItem type = null;
                boolean isNamespace = false;
                int namespace = 0;
                boolean isStatic = false;
                if (item instanceof SlotAVM2Item) {
                    SlotAVM2Item sai = (SlotAVM2Item) item;
                    if (sai.isStatic() != generateStatic) {
                        continue;
                    }
                    var = sai.var;
                    val = sai.value;
                    type = sai.type;
                    isStatic = sai.isStatic();
                    namespace = sai.getNamespace();
                }
                if (item instanceof ConstAVM2Item) {
                    ConstAVM2Item cai = (ConstAVM2Item) item;
                    if (cai.isStatic() != generateStatic) {
                        continue;
                    }
                    var = cai.var;
                    val = cai.value;
                    type = cai.type;
                    namespace = cai.getNamespace();
                    isNamespace = type.toString().equals("Namespace");
                    isStatic = cai.isStatic();
                }
                tsc.name_index = traitName(namespace, var);
                tsc.type_index = isNamespace ? 0 : typeName(localData, type);

                ValueKind vk = getValueKind(abc.constants.constant_namespace.get(namespace).kind, type, val);
                if (vk == null) {
                    tsc.value_kind = ValueKind.CONSTANT_Undefined;
                } else {
                    tsc.value_kind = vk.value_kind;
                    tsc.value_index = vk.value_index;
                }
                tsc.slot_id = isStatic ? slot_id++ : 0;
                ts.traits.add(tsc);
                traits[k] = tsc;
            }
            if ((item instanceof MethodAVM2Item) || (item instanceof GetterAVM2Item) || (item instanceof SetterAVM2Item)) {
                MethodAVM2Item mai = (MethodAVM2Item) item;
                if (mai.isStatic() != generateStatic) {
                    continue;
                }
                TraitMethodGetterSetter tmgs = new TraitMethodGetterSetter();
                tmgs.kindType = (item instanceof MethodAVM2Item) ? Trait.TRAIT_METHOD : ((item instanceof GetterAVM2Item) ? Trait.TRAIT_GETTER : Trait.TRAIT_SETTER);
                tmgs.name_index = traitName(((MethodAVM2Item) item).namespace, ((MethodAVM2Item) item).functionName);
                tmgs.disp_id = 0; //0 = disable override optimization;  TODO: handle this
                if (mai.isFinal()) {
                    tmgs.kindFlags |= Trait.ATTR_Final;
                }
                if (mai.isOverride()) {
                    tmgs.kindFlags |= Trait.ATTR_Override;
                }
                ts.traits.add(tmgs);

                traits[k] = tmgs;
            } else if (item instanceof FunctionAVM2Item) {
                TraitFunction tf = new TraitFunction();
                tf.slot_id = slot_id++;
                tf.kindType = Trait.TRAIT_FUNCTION;
                tf.name_index = traitName(((FunctionAVM2Item) item).namespace, ((FunctionAVM2Item) item).functionName);
                ts.traits.add(tf);
                traits[k] = tf;
            }
        }

        return traits;
    }

    public ScriptInfo generateScriptInfo(PackageAVM2Item pkg, SourceGeneratorLocalData localData, List<GraphTargetItem> commands, boolean documentClass) throws ParseException {
        ScriptInfo si = new ScriptInfo();
        Trait[] traitArr = generateTraitsPhase1(pkg, null, null, false, localData, commands, si.traits);

        MethodInfo mi = new MethodInfo(new int[0], 0, 0, 0, new ValueKind[0], new int[0]);
        MethodBody mb = new MethodBody();
        mb.method_info = abc.addMethodInfo(mi);
        mb.code = new AVM2Code();
        mb.code.code.add(ins(new GetLocal0Ins()));
        mb.code.code.add(ins(new PushScopeIns()));

        int traitScope = 1;

        for (Trait t : si.traits.traits) {
            if (t instanceof TraitClass) {
                TraitClass tc = (TraitClass) t;
                List<Integer> parents = new ArrayList<>();
                parentNamesAddNames(abc, allABCs, abc.instance_info.get(tc.class_info).name_index, parents, new ArrayList<String>(), new ArrayList<String>());
                if (documentClass) {
                    mb.code.code.add(ins(new GetScopeObjectIns(), 0));
                    traitScope++;
                } else {
                    NamespaceSet nsset = new NamespaceSet(new int[]{abc.constants.constant_multiname.get(tc.name_index).namespace_index});
                    mb.code.code.add(ins(new FindPropertyStrictIns(), abc.constants.getMultinameId(new Multiname(Multiname.MULTINAME, abc.constants.constant_multiname.get(tc.name_index).name_index, 0, abc.constants.getNamespaceSetId(nsset, true), 0, new ArrayList<Integer>()), true)));
                }
                for (int i = parents.size() - 1; i >= 1; i--) {
                    mb.code.code.add(ins(new GetLexIns(), parents.get(i)));
                    mb.code.code.add(ins(new PushScopeIns()));
                    traitScope++;
                }
                mb.code.code.add(ins(new GetLexIns(), parents.get(1)));
                mb.code.code.add(ins(new NewClassIns(), tc.class_info));
                for (int i = parents.size() - 1; i >= 1; i--) {
                    mb.code.code.add(ins(new PopScopeIns()));
                }
                mb.code.code.add(ins(new InitPropertyIns(), tc.name_index));
            }
        }

        mb.code.code.add(ins(new ReturnVoidIns()));
        mb.autoFillStats(abc, documentClass ? 1 : 0);
        abc.addMethodBody(mb);
        si.init_index = mb.method_info;
        generateTraitsPhase2(traitScope, pkg, null, null, false, localData, commands, si.traits, traitArr);
        return si;
    }

    public static void parentNamesAddNames(ABC abc, List<ABC> allABCs, int name_index, List<Integer> indices, List<String> names, List<String> namespaces) {
        List<Integer> cindices = new ArrayList<>();

        List<ABC> outABCs = new ArrayList<>();
        parentNames(abc, allABCs, name_index, cindices, names, namespaces, outABCs);
        for (int i = 0; i < cindices.size(); i++) {
            ABC a = outABCs.get(i);
            int m = cindices.get(i);
            if (a == abc) {
                indices.add(m);
                continue;
            }
            Multiname superName = a.constants.constant_multiname.get(m);
            indices.add(
                    abc.constants.getMultinameId(
                            new Multiname(Multiname.QNAME,
                                    abc.constants.getStringId(superName.getName(a.constants, new ArrayList<String>()), true),
                                    abc.constants.getNamespaceId(new Namespace(superName.getNamespace(a.constants).kind, abc.constants.getStringId(superName.getNamespace(a.constants).getName(a.constants), true)), 0, true), 0, 0, new ArrayList<Integer>()), true)
            );
        }
    }

    public static GraphTargetItem getTraitReturnType(ABC abc, Trait t) {
        if (t instanceof TraitSlotConst) {
            TraitSlotConst tsc = (TraitSlotConst) t;
            if (tsc.type_index == 0) {
                return TypeItem.UNBOUNDED;
            }
            return new TypeItem(abc.constants.constant_multiname.get(tsc.type_index).getNameWithNamespace(abc.constants));
        }
        if (t instanceof TraitMethodGetterSetter) {
            TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) t;
            if (tmgs.kindType == Trait.TRAIT_GETTER) {
                return new TypeItem(abc.constants.constant_multiname.get(abc.method_info.get(tmgs.method_info).ret_type).getNameWithNamespace(abc.constants));
            }
            if (tmgs.kindType == Trait.TRAIT_SETTER) {
                return new TypeItem(abc.constants.constant_multiname.get(abc.method_info.get(tmgs.method_info).param_types[0]).getNameWithNamespace(abc.constants));
            }
        }
        if (t instanceof TraitFunction) {
            return new TypeItem("Function");
        }
        return TypeItem.UNBOUNDED;
    }

    public static boolean searchPrototypeChain(boolean instanceOnly, List<ABC> abcs, String pkg, String obj, String propertyName, Reference<String> outName, Reference<String> outNs, Reference<String> outPropNs, Reference<Integer> outPropNsKind, Reference<String> outPropType) {

        for (ABC abc : abcs) {
            if (!instanceOnly) {
                for (ScriptInfo ii : abc.script_info) {
                    for (Trait t : ii.traits.traits) {
                        if (pkg.equals(t.getName(abc).getNamespace(abc.constants).getName(abc.constants))) {
                            if (propertyName.equals(t.getName(abc).getName(abc.constants, new ArrayList<String>()))) {
                                outName.setVal(obj);
                                outNs.setVal(pkg);
                                outPropNs.setVal(t.getName(abc).getNamespace(abc.constants).getName(abc.constants));
                                outPropNsKind.setVal(t.getName(abc).getNamespace(abc.constants).kind);
                                outPropType.setVal(getTraitReturnType(abc, t).toString());
                                return true;
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < abc.instance_info.size(); i++) {
                InstanceInfo ii = abc.instance_info.get(i);
                Multiname clsName = ii.getName(abc.constants);
                if (obj.equals(clsName.getName(abc.constants, new ArrayList<String>()))) {
                    if (pkg.equals(clsName.getNamespace(abc.constants).getName(abc.constants))) {
                        //class found

                        for (Trait t : ii.instance_traits.traits) {
                            if (propertyName.equals(t.getName(abc).getName(abc.constants, new ArrayList<String>()))) {
                                outName.setVal(obj);
                                outNs.setVal(pkg);
                                outPropNs.setVal(t.getName(abc).getNamespace(abc.constants).getName(abc.constants));
                                outPropNsKind.setVal(t.getName(abc).getNamespace(abc.constants).kind);
                                outPropType.setVal(getTraitReturnType(abc, t).toString());
                                return true;
                            }
                        }

                        if (!instanceOnly) {
                            for (Trait t : abc.class_info.get(i).static_traits.traits) {
                                if (propertyName.equals(t.getName(abc).getName(abc.constants, new ArrayList<String>()))) {
                                    outName.setVal(obj);
                                    outNs.setVal(pkg);
                                    outPropNs.setVal(t.getName(abc).getNamespace(abc.constants).getName(abc.constants));
                                    outPropNsKind.setVal(t.getName(abc).getNamespace(abc.constants).kind);
                                    outPropType.setVal(getTraitReturnType(abc, t).toString());
                                    return true;
                                }
                            }
                        }

                        Multiname superName = abc.constants.constant_multiname.get(ii.super_index);
                        if (superName != null) {
                            return searchPrototypeChain(instanceOnly, abcs, superName.getNamespace(abc.constants).getName(abc.constants), superName.getName(abc.constants, new ArrayList<String>()), propertyName, outName, outNs, outPropNs, outPropNsKind, outPropType);
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void parentNames(ABC abc, List<ABC> allABCs, int name_index, List<Integer> indices, List<String> names, List<String> namespaces, List<ABC> outABCs) {
        indices.add(name_index);
        names.add(abc.constants.constant_multiname.get(name_index).getName(abc.constants, new ArrayList<String>()));
        namespaces.add(abc.constants.constant_multiname.get(name_index).getNamespace(abc.constants).getName(abc.constants));
        Multiname mname = abc.constants.constant_multiname.get(name_index);

        outABCs.add(abc);

        List<ABC> abcs = new ArrayList<>();
        abcs.add(abc);
        abcs.addAll(allABCs);

        for (ABC a : abcs) {
            for (int i = 0; i < a.instance_info.size(); i++) {
                Multiname m = a.constants.constant_multiname.get(a.instance_info.get(i).name_index);
                if (m.getName(a.constants, new ArrayList<String>()).equals(mname.getName(abc.constants, new ArrayList<String>()))) {
                    if (m.getNamespace(a.constants).getName(a.constants).equals(mname.getNamespace(abc.constants).getName(abc.constants))) {
                        //Multiname superName = a.constants.constant_multiname.get(a.instance_info.get(i).super_index);
                        abcs.remove(a);
                        if (a.instance_info.get(i).super_index != 0) {
                            parentNames(a, abcs, a.instance_info.get(i).super_index, indices, names, namespaces, outABCs);
                        }
                        /*parentNames(abc,allABCs,abc.constants.getMultinameId(
                         new Multiname(superName.kind, 
                         abc.constants.getStringId(superName.getName(a.constants, new ArrayList<String>()),true), 
                         abc.constants.getNamespaceId(new Namespace(superName.getNamespace(a.constants).kind, abc.constants.getStringId(superName.getNamespace(a.constants).getName(a.constants),true)),0,true), 0, 0, new ArrayList<Integer>()), true),indices,names,namespaces,outABCs);*/
                        return;
                    }
                }
            }
        }
    }

    public void calcRegisters(SourceGeneratorLocalData localData, FunctionAVM2Item fun, Reference<Boolean> hasArguments) throws ParseException {
        List<String> registerNames = new ArrayList<>();
        registerNames.add("this");
        registerNames.addAll(fun.paramNames);
        localData.registerVars.clear();
        for (NameAVM2Item n : fun.subvariables) {
            if (n.getVariableName().equals("arguments") & !n.isDefinition()) {
                registerNames.add("arguments");
                hasArguments.setVal(Boolean.TRUE);
                break;
            }
        }
        for (NameAVM2Item n : fun.subvariables) {
            if (n.isDefinition()) {
                registerNames.add(n.getVariableName());
            }
        }
        for (NameAVM2Item n : fun.subvariables) {
            if (n.unresolved) {
                GraphTargetItem resolved = null;
                List<ABC> abcs = new ArrayList<>();
                abcs.add(abc);
                abcs.addAll(allABCs);

                for (int i = 0; i < n.openedNamespaces.size(); i++) {
                    int nsIndex = n.openedNamespaces.get(i);
                    Namespace ns = abc.constants.constant_namespace.get(nsIndex);
                    int nsKind = ns.kind;
                    loopabc:
                    for (ABC a : abcs) {
                        for (int h = 0; h < a.instance_info.size(); h++) {
                            InstanceInfo ii = a.instance_info.get(h);
                            Multiname nm = a.constants.constant_multiname.get(ii.name_index);
                            if (nm.getNamespace(a.constants).getName(a.constants).equals(ns.getName(abc.constants)) && nm.getNamespace(a.constants).kind == nsKind) {

                                //found opened class
                                Reference<String> outName = new Reference<>("");
                                Reference<String> outNs = new Reference<>("");
                                Reference<String> outPropNs = new Reference<>("");
                                Reference<Integer> outPropNsKind = new Reference<>(1);
                                Reference<String> outPropType = new Reference<>("");
                                if (AVM2SourceGenerator.searchPrototypeChain(false, abcs, nm.getNamespace(a.constants).getName(a.constants), nm.getName(a.constants, new ArrayList<String>()), n.getVariableName(), outName, outNs, outPropNs, outPropNsKind, outPropType)) {
                                    resolved = new PropertyAVM2Item(null, n.getVariableName(), n.getIndex(), abc, allABCs, n.openedNamespaces);
                                }
                            }
                        }
                    }
                }
                n.redirect = resolved;
                if (resolved == null) {
                    throw new ParseException("Unknown variable or property:" + n.getVariableName(), n.line);
                }
            }
            if (n.getVariableName() != null) {
                n.setRegNumber(registerNames.indexOf(n.getVariableName()));
            }
        }
        for (int i = 0; i < registerNames.size(); i++) {
            localData.registerVars.put(registerNames.get(i), i);
        }
        List<NameAVM2Item> declarations=new ArrayList<>();
        for (NameAVM2Item n : fun.subvariables) {
            if(n.isDefinition() && n.getAssignedValue() == null){
                NameAVM2Item d=new NameAVM2Item(n.type, n.line, n.getVariableName(), NameAVM2Item.getDefaultValue(""+n.type), true, n.openedNamespaces);
                d.setRegNumber(n.getRegNumber());
                declarations.add(d);
            }
        }
        fun.body.addAll(0,declarations);
    }

    public int resolveType(String objType) {
        if (objType.equals("*")) {
            return 0;
        }
        List<ABC> abcs = new ArrayList<>();
        abcs.add(abc);
        abcs.addAll(allABCs);
        for (ABC a : abcs) {
            int ci = a.findClassByName(objType);
            if (ci != -1) {
                Multiname tname = a.instance_info.get(ci).getName(a.constants);
                return abc.constants.getMultinameId(new Multiname(tname.kind,
                        abc.constants.getStringId(tname.getName(a.constants, new ArrayList<String>()), true),
                        abc.constants.getNamespaceId(new Namespace(tname.getNamespace(a.constants).kind, abc.constants.getStringId(tname.getNamespace(a.constants).getName(a.constants), true)), 0, true), 0, 0, new ArrayList<Integer>()), true);
            }
        }
        return 0;
    }
}
