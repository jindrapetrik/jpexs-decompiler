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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.NotIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.StrictEqualsIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfTrueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.DupIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PopIns;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.types.ClassInfo;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionSetVariable;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionGetMember;
import com.jpexs.decompiler.flash.action.swf5.ActionPushDuplicate;
import com.jpexs.decompiler.flash.action.swf5.ActionSetMember;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.action.swf6.ActionStrictEquals;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.AndItem;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.CommaExpressionItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.DoWhileItem;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.decompiler.graph.model.ForItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.OrItem;
import com.jpexs.decompiler.graph.model.SwitchItem;
import com.jpexs.decompiler.graph.model.TernarOpItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import com.jpexs.helpers.Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class AVM2SourceGenerator implements SourceGenerator {

    private AVM2Instruction ins(InstructionDefinition def,int...operands)
    {
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
        ret.add(ins(new IfTrueIns(),andExprLen));
        ret.addAll(andExpr);
        return ret;

    }
    
    private byte[] insToBytes(List<AVM2Instruction> code){
        try {
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
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
        ret.add(ins(new IfTrueIns(),orExprLen));
        ret.addAll(orExpr);
        return ret;
    }

    public List<AVM2Instruction> toActionList(List<GraphSourceItem> items) {
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

    private List<GraphSourceItem> generateIf(SourceGeneratorLocalData localData, GraphTargetItem expression, List<GraphTargetItem> onTrueCmds, List<GraphTargetItem> onFalseCmds, boolean ternar) {
        List<GraphSourceItem> ret = new ArrayList<>();
        if (false){//expression instanceof Inverted) {
            //ret.addAll(((Inverted) expression).invert().toSource(localData, this));
        } else {
            ret.addAll(expression.toSource(localData, this));
            ret.add(ins(new NotIns()));
        }
        List<AVM2Instruction> onTrue = null;
        List<AVM2Instruction> onFalse = null;
        if (ternar) {
            onTrue = toActionList(onTrueCmds.get(0).toSource(localData, this));
        } else {
            onTrue = generateToActionList(localData, onTrueCmds);
        }

        if (onFalseCmds != null && !onFalseCmds.isEmpty()) {
            if (ternar) {
                onFalse = toActionList(onFalseCmds.get(0).toSource(localData, this));
            } else {
                onFalse = generateToActionList(localData, onFalseCmds);
            }
        }
        byte[] onTrueBytes = insToBytes(onTrue);
        int onTrueLen = onTrueBytes.length;

        AVM2Instruction ifaif = ins(new IfTrueIns(),0);
        ret.add(ifaif);
        ret.addAll(onTrue);
        ifaif.operands[0]=(onTrueLen);
        AVM2Instruction ajmp = null;
        if (onFalse != null) {
            if (!((!nonempty(onTrue).isEmpty())
                    && (onTrue.get(onTrue.size() - 1).definition instanceof JumpIns)
                    //&& ((((ActionJump) onTrue.get(onTrue.size() - 1)).isContinue)
                    //|| (((ActionJump) onTrue.get(onTrue.size() - 1)).isBreak))  //TODO
                    )
                    ) {
                ajmp = ins(new JumpIns(),0);
                ret.add(ajmp);
                onTrueLen += ajmp.getBytes().length;
            }
            ifaif.operands[0]=onTrueLen;
            byte[] onFalseBytes = insToBytes(onFalse);
            int onFalseLen = onFalseBytes.length;
            if (ajmp != null) {
                ajmp.operands[0]=onFalseLen;
            }
            ret.addAll(onFalse);
        }
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, IfItem item) {
        return generateIf(localData, item.expression, item.onTrue, item.onFalse, false);
    }

    private void fixLoop(List<AVM2Instruction> code, int breakOffset) {
        fixLoop(code, breakOffset, Integer.MAX_VALUE);
    }

    private void fixLoop(List<AVM2Instruction> code, int breakOffset, int continueOffset) {
        int pos = 0;
        //TODO: handle loops - continue, break
        for (AVM2Instruction a : code) {
            pos += a.getBytes().length;
            if (a.definition instanceof JumpIns) {
                /*ActionJump aj = (ActionJump) a;
                if (aj.isContinue && (continueOffset != Integer.MAX_VALUE)) {
                    aj.operands[0]=(-pos + continueOffset);
                    aj.isContinue = false;
                }
                if (aj.isBreak) {
                    aj.operands[0]=(-pos + breakOffset);
                    aj.isBreak = false;
                }*/
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
        if (!ex.isEmpty()) {
            GraphTargetItem lastItem = ex.remove(ex.size() - 1);
            whileExpr.addAll(generateToActionList(localData, ex));
            whileExpr.addAll(toActionList(lastItem.toSource(localData, this))); //Want result
        }

        List<AVM2Instruction> whileBody = generateToActionList(localData, item.commands);
        whileExpr.add(ins(new NotIns()));
        AVM2Instruction whileaif = ins(new IfTrueIns(),0);
        whileExpr.add(whileaif);
        AVM2Instruction whileajmp = ins(new JumpIns(),0);
        whileBody.add(whileajmp);
        int whileExprLen = insToBytes(whileExpr).length;
        int whileBodyLen = insToBytes(whileBody).length;
        whileajmp.operands[0]=(-(whileExprLen
                + whileBodyLen));
        whileaif.operands[0]=(whileBodyLen);
        ret.addAll(whileExpr);
        fixLoop(whileBody, whileBodyLen, -whileExprLen);
        ret.addAll(whileBody);
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, DoWhileItem item) {
        List<GraphSourceItem> ret = new ArrayList<>();
        List<AVM2Instruction> doExpr = generateToActionList(localData, item.expression);
        List<AVM2Instruction> doBody = generateToActionList(localData, item.commands);

        int doBodyLen = insToBytes(doBody).length;
        int doExprLen = insToBytes(doExpr).length;

        ret.addAll(doBody);
        ret.addAll(doExpr);
        AVM2Instruction doif = ins(new IfTrueIns(),0);
        ret.add(doif);
        doif.operands[0]=(-doBodyLen - doExprLen - doif.getBytes().length);
        fixLoop(doBody, doBodyLen + doExprLen + doif.getBytes().length, doBodyLen);
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ForItem item) {
        List<GraphSourceItem> ret = new ArrayList<>();
        List<AVM2Instruction> forExpr = generateToActionList(localData, item.expression);
        List<AVM2Instruction> forBody = generateToActionList(localData, item.commands);
        List<AVM2Instruction> forFinalCommands = generateToActionList(localData, item.finalCommands);

        forExpr.add(ins(new NotIns()));
        AVM2Instruction foraif = ins(new IfTrueIns(),0);
        forExpr.add(foraif);
        AVM2Instruction forajmp = ins(new JumpIns(),0);
        int forajmpLen = forajmp.getBytes().length;
        int forExprLen = insToBytes(forExpr).length;
        int forBodyLen = insToBytes(forBody).length;
        int forFinalLen = insToBytes(forFinalCommands).length;
        forajmp.operands[0]=(-(forExprLen
                + forBodyLen + forFinalLen + forajmpLen));
        foraif.operands[0]=(forBodyLen + forFinalLen + forajmpLen);
        ret.addAll(forExpr);
        ret.addAll(forBody);
        ret.addAll(forFinalCommands);
        ret.add(forajmp);
        fixLoop(forBody, forBodyLen + forFinalLen + forajmpLen, forBodyLen);
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
        HashMap<String, Integer> registerVars = getRegisterVars(localData);
        int exprReg = 0;
        for (int i = 0; i < 256; i++) {
            if (!registerVars.containsValue(i)) {
                registerVars.put("__switch" + uniqId(), i);
                exprReg = i;
                break;
            }
        }

        ret.addAll(toActionList(item.switchedObject.toSource(localData, this)));

        boolean firstCase = true;
        List<List<AVM2Instruction>> caseIfs = new ArrayList<>();
        List<List<AVM2Instruction>> caseCmds = new ArrayList<>();
        List<List<List<AVM2Instruction>>> caseExprsAll = new ArrayList<>();

        loopm:
        for (int m = 0; m < item.caseValues.size(); m++) {
            List<List<AVM2Instruction>> caseExprs = new ArrayList<>();
            List<AVM2Instruction> caseIfsOne = new ArrayList<>();
            int mapping = item.valuesMapping.get(m);
            for (; m < item.caseValues.size(); m++) {
                int newmapping = item.valuesMapping.get(m);
                if (newmapping != mapping) {
                    m--;
                    break;
                }
                List<AVM2Instruction> curCaseExpr = generateToActionList(localData, item.caseValues.get(m));
                caseExprs.add(curCaseExpr);
                if (firstCase) {
                    curCaseExpr.add(0, ins(new DupIns(),exprReg));
                    curCaseExpr.add(0, ins(new SetLocalIns(),exprReg));
                } else {
                    curCaseExpr.add(0, ins(new GetLocalIns(),exprReg));
                }
                curCaseExpr.add(ins(new StrictEqualsIns()));
                AVM2Instruction aif = ins(new IfTrueIns(),0);
                caseIfsOne.add(aif);
                curCaseExpr.add(aif);
                ret.addAll(curCaseExpr);
                firstCase = false;
            }
            caseExprsAll.add(caseExprs);
            caseIfs.add(caseIfsOne);
            List<AVM2Instruction> caseCmd = generateToActionList(localData, item.caseCommands.get(mapping));
            caseCmds.add(caseCmd);
        }
        AVM2Instruction defJump = ins(new JumpIns(),0);
        ret.add(defJump);
        List<AVM2Instruction> defCmd = new ArrayList<>();
        if (!item.defaultCommands.isEmpty()) {
            defCmd = generateToActionList(localData, item.defaultCommands);
        }
        for (List<AVM2Instruction> caseCmd : caseCmds) {
            ret.addAll(caseCmd);
        }
        ret.addAll(defCmd);

        List<List<Integer>> exprLengths = new ArrayList<>();
        for (List<List<AVM2Instruction>> caseExprs : caseExprsAll) {
            List<Integer> lengths = new ArrayList<>();
            for (List<AVM2Instruction> caseExpr : caseExprs) {
                lengths.add(insToBytes(caseExpr).length);
            }
            exprLengths.add(lengths);
        }
        List<Integer> caseLengths = new ArrayList<>();
        for (List<AVM2Instruction> caseCmd : caseCmds) {
            caseLengths.add(insToBytes(caseCmd).length);
        }
        int defLength = insToBytes(defCmd).length;

        for (int i = 0; i < caseIfs.size(); i++) {
            for (int c = 0; c < caseIfs.get(i).size(); c++) {
                int jmpPos = 0;
                for (int j = c + 1; j < caseIfs.get(i).size(); j++) {
                    jmpPos += exprLengths.get(i).get(j);
                }
                for (int k = i + 1; k < caseIfs.size(); k++) {
                    for (int m = 0; m < caseIfs.get(k).size(); m++) {
                        jmpPos += exprLengths.get(k).get(m);
                    }
                }
                jmpPos += defJump.getBytes().length;
                for (int n = 0; n < i; n++) {
                    jmpPos += caseLengths.get(n);
                }
                caseIfs.get(i).get(c).operands[0]=(jmpPos);
            }
        }
        int defJmpPos = 0;
        for (int i = 0; i < caseIfs.size(); i++) {
            defJmpPos += caseLengths.get(i);
        }

        defJump.operands[0]=(defJmpPos);
        List<AVM2Instruction> caseCmdsAll = new ArrayList<>();
        int breakOffset = 0;
        for (int i = 0; i < caseCmds.size(); i++) {
            caseCmdsAll.addAll(caseCmds.get(i));
            breakOffset += caseLengths.get(i);
        }
        breakOffset += defLength;
        fixLoop(caseCmdsAll, breakOffset);
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
        AVM2Instruction abreak = ins(new JumpIns(),0);
        //TODO: handle break
        //abreak.isBreak = true;
        ret.add(abreak);
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ContinueItem item) {
        List<GraphSourceItem> ret = new ArrayList<>();
        AVM2Instruction acontinue = ins(new JumpIns(),0);
        //TODO: handle continue
        //acontinue.isContinue = true;
        ret.add(acontinue);
        return ret;
    }

    private List<AVM2Instruction> generateToActionList(SourceGeneratorLocalData localData, List<GraphTargetItem> commands) {
        return toActionList(generate(localData, commands));
    }

    private List<AVM2Instruction> generateToActionList(SourceGeneratorLocalData localData, GraphTargetItem command) {
        return toActionList(command.toSource(localData, this));
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

    private String getName(GraphTargetItem item) {
        if (item instanceof VariableAVM2Item) {
            return ((VariableAVM2Item) item).getVariableName();
        }
        if (item instanceof GetPropertyAVM2Item) {
            GetPropertyAVM2Item mem = (GetPropertyAVM2Item) item;
            return getName(mem.propertyName);
        }
        return null;
    }

    private List<String> getVarParts(GraphTargetItem item) {
        List<String> ret = new ArrayList<>();
        do {
            if (item instanceof GetPropertyAVM2Item) {
                GetPropertyAVM2Item mem = (GetPropertyAVM2Item) item;
                ret.add(0, getName(mem));
                item = mem.object;
            }
        } while (item instanceof GetPropertyAVM2Item);
        String f = getName(item);
        if (f != null) {
            ret.add(0, f);
        }
        return ret;
    }


    private final ABC abc;

    public AVM2SourceGenerator(ABC abc) {
        this.abc = abc;
    }

    public ABC getABC() {
        return abc;
    }


    public void generateClass(ClassInfo classInfo,InstanceInfo instanceInfo,SourceGeneratorLocalData localData, boolean isInterface, GraphTargetItem name, GraphTargetItem extendsVal, List<GraphTargetItem> implementsStr, GraphTargetItem constructor, List<GraphTargetItem> functions, List<MyEntry<GraphTargetItem, GraphTargetItem>> vars, List<GraphTargetItem> staticFunctions, List<MyEntry<GraphTargetItem, GraphTargetItem>> staticVars) {
        List<String> extendsStr = getVarParts(extendsVal);
        List<GraphSourceItem> ret = new ArrayList<>();
        
        classInfo.cinit_index = 0; //class_initializer
               
        ParsedSymbol s = null;
        List<AVM2Instruction> constr = new ArrayList<>();

        if (constructor == null) {
            instanceInfo.iinit_index = 0; //TODO           
            //TODO: default constr
        } else {            
            instanceInfo.iinit_index = 0; //TODO: set constructor
        }
        List<Trait> staticTraits=new ArrayList<Trait>();
        List<Trait> instanceTraits=new ArrayList<Trait>();
        if (!isInterface) {
            for (GraphTargetItem f : staticFunctions) {
                 TraitMethodGetterSetter tmgs  = new TraitMethodGetterSetter();
                 tmgs.method_info = 0; //TODO
                 tmgs.name_index = 0; //TODO                 
                 staticTraits.add(tmgs);               
            }
            for (GraphTargetItem f : functions) {
                TraitMethodGetterSetter tmgs  = new TraitMethodGetterSetter();
                 tmgs.method_info = 0; //TODO
                 tmgs.name_index = 0; //TODO                 
                 instanceTraits.add(tmgs);      
            }
            for (MyEntry<GraphTargetItem, GraphTargetItem> en : staticVars) {
                TraitSlotConst tsc=new TraitSlotConst();
                tsc.type_index = 0; //TODO
                tsc.value_index = 0; //TODO
                tsc.value_kind = 0; //TODO
                staticTraits.add(tsc);
                
            }
            for (MyEntry<GraphTargetItem, GraphTargetItem> en : vars) {
                TraitSlotConst tsc=new TraitSlotConst();
                tsc.type_index = 0; //TODO
                tsc.value_index = 0; //TODO
                tsc.value_kind = 0; //TODO
                instanceTraits.add(tsc);
            }
        }
        
        if (!extendsStr.isEmpty()) {
            instanceInfo.super_index = 0;//TODO
        }
        if (!isInterface) {
            
        }

        if (!implementsStr.isEmpty()) {
            for (GraphTargetItem imp : implementsStr) {
                instanceInfo.interfaces[0] = 0; //TODO
            }
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
    
    
    public Traits generateScriptTraits(SourceGeneratorLocalData localData, List<GraphTargetItem> commands){
        Traits t=new Traits();
        for(GraphTargetItem item:commands)
        {
            if(item instanceof ClassAVM2Item){
                
            }
            if(item instanceof VariableAVM2Item)
            {
                
            }
        }        
        return t;
    }
    
    public ScriptInfo generateScriptInfo(SourceGeneratorLocalData localData, List<GraphTargetItem> commands){
        ScriptInfo si=new ScriptInfo();
        si.init_index = 0; //TODO
        si.traits = generateScriptTraits(localData, commands);
        return si;
    }
}
