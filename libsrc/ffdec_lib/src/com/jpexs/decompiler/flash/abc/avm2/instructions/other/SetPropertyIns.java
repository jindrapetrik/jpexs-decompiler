/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.avm2.instructions.other;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.SetTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ApplyTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConstructAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConvertAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.DecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetLexAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.InitVectorAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostDecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreDecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public class SetPropertyIns extends InstructionDefinition implements SetTypeIns {

    public SetPropertyIns() {
        super(0x61, "setproperty", new int[]{AVM2Code.DAT_MULTINAME_INDEX}, true);
    }

    private GraphTargetItem checkIncDec(boolean standalone, int multinameIndex, AVM2Instruction ins, AVM2LocalData localData, GraphTargetItem item,
            LocalRegAVM2Item valueLocalReg, LocalRegAVM2Item nameLocalReg, LocalRegAVM2Item objLocalReg) {
        if (item instanceof SetLocalAVM2Item) {
            SetLocalAVM2Item valueSetLocalReg = (SetLocalAVM2Item) item;
            if ((valueSetLocalReg.value instanceof IncrementAVM2Item) || (valueSetLocalReg.value instanceof DecrementAVM2Item)) {
                boolean isIncrement = (valueSetLocalReg.value instanceof IncrementAVM2Item);
                boolean hasConvert = valueSetLocalReg.value.value instanceof ConvertAVM2Item; //in air there is convert added when postincrement

                if (valueSetLocalReg.value.value.getNotCoercedNoDup() instanceof GetPropertyAVM2Item) {
                    GetPropertyAVM2Item getProperty = (GetPropertyAVM2Item) valueSetLocalReg.value.value.getNotCoercedNoDup();
                    FullMultinameAVM2Item propertyName = ((FullMultinameAVM2Item) getProperty.propertyName);
                    SetLocalAVM2Item nameSetLocalReg = null;
                    if (propertyName.name instanceof SetLocalAVM2Item) {
                        nameSetLocalReg = (SetLocalAVM2Item) propertyName.name;
                    }
                    if (getProperty.object instanceof SetLocalAVM2Item) {
                        SetLocalAVM2Item objSetLocalReg = (SetLocalAVM2Item) getProperty.object;

                        if ((valueLocalReg.regIndex == valueSetLocalReg.regIndex)
                                && (propertyName.multinameIndex == multinameIndex)
                                && ((nameLocalReg == null && nameSetLocalReg == null) || (nameLocalReg.regIndex == nameSetLocalReg.regIndex))
                                && (objLocalReg.regIndex == objSetLocalReg.regIndex)) {
                            if (nameSetLocalReg != null) {
                                propertyName.name = nameSetLocalReg.value;
                            }
                            getProperty.object = objSetLocalReg.value;

                            if (isIncrement) {
                                if (hasConvert && standalone) {
                                    return new PostIncrementAVM2Item(ins, localData.lineStartInstruction, getProperty);
                                }
                                return new PreIncrementAVM2Item(ins, localData.lineStartInstruction, getProperty);
                            } else {
                                if (hasConvert && standalone) {
                                    return new PostDecrementAVM2Item(ins, localData.lineStartInstruction, getProperty);
                                }
                                return new PreDecrementAVM2Item(ins, localData.lineStartInstruction, getProperty);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        int multinameIndex = ins.operands[0];
        GraphTargetItem value = stack.pop();
        FullMultinameAVM2Item multiname = resolveMultiname(localData, true, stack, localData.getConstants(), multinameIndex, ins);
        GraphTargetItem obj = stack.pop();

        //assembled/TestIncrement
        if ((value instanceof IncrementAVM2Item) || (value instanceof DecrementAVM2Item)) {
            boolean isIncrement = (value instanceof IncrementAVM2Item);
            if (value.value instanceof DuplicateItem) {
                GraphTargetItem duplicated = value.value.value;
                if (!stack.isEmpty()) {
                    if (stack.peek() == duplicated) {
                        GraphTargetItem notCoerced = duplicated.getNotCoerced();
                        if (notCoerced instanceof GetLexAVM2Item) {
                            GetLexAVM2Item getLex = (GetLexAVM2Item) notCoerced;
                            if (localData.abc.constants.getMultiname(multinameIndex).equals(getLex.propertyName)) {
                                stack.pop();
                                if (isIncrement) {
                                    stack.push(new PostIncrementAVM2Item(ins, localData.lineStartInstruction, getLex));
                                } else {
                                    stack.push(new PostDecrementAVM2Item(ins, localData.lineStartInstruction, getLex));
                                }
                                return;
                            }
                        }
                        if (notCoerced instanceof GetPropertyAVM2Item) {
                            GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) notCoerced;
                            if (((FullMultinameAVM2Item) getProp.propertyName).compareSame(multiname)) {

                                if (getProp.object instanceof DuplicateItem) { //assembled/TestIncrement3
                                    if (getProp.object.value == obj) {
                                        getProp.object = obj;
                                    }
                                }

                                stack.pop();
                                if (isIncrement) {
                                    stack.push(new PostIncrementAVM2Item(ins, localData.lineStartInstruction, getProp));
                                } else {
                                    stack.push(new PostDecrementAVM2Item(ins, localData.lineStartInstruction, getProp));
                                }
                                return;
                            }
                        }
                    }
                }
            }
        }

        if ((value instanceof IncrementAVM2Item) || (value instanceof DecrementAVM2Item)) {
            boolean isIncrement = (value instanceof IncrementAVM2Item);

            boolean hasConvert = value.value instanceof ConvertAVM2Item;
            if (value.value.getNotCoercedNoDup() instanceof GetLexAVM2Item) {
                GetLexAVM2Item getLex = (GetLexAVM2Item) value.value.getNotCoercedNoDup();
                if (localData.abc.constants.getMultiname(multinameIndex).equals(getLex.propertyName)) {
                    if (hasConvert) {
                        if (isIncrement) {
                            output.add(new PostIncrementAVM2Item(ins, localData.lineStartInstruction, getLex));
                        } else {
                            output.add(new PostDecrementAVM2Item(ins, localData.lineStartInstruction, getLex));
                        }
                    } else {
                        if (isIncrement) {
                            output.add(new PreIncrementAVM2Item(ins, localData.lineStartInstruction, getLex));
                        } else {
                            output.add(new PreDecrementAVM2Item(ins, localData.lineStartInstruction, getLex));
                        }
                    }
                    return;
                }
            }

            if (value.value.getNotCoercedNoDup() instanceof GetPropertyAVM2Item) {
                GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) value.value.getNotCoercedNoDup();
                if (((FullMultinameAVM2Item) getProp.propertyName).compareSame(multiname)) {

                    if (getProp.object instanceof DuplicateItem) {
                        if (getProp.object.value == obj) {
                            getProp.object = obj;
                        }
                    }
                    if (hasConvert) {
                        if (isIncrement) {
                            output.add(new PostIncrementAVM2Item(ins, localData.lineStartInstruction, getProp));
                        } else {
                            output.add(new PostDecrementAVM2Item(ins, localData.lineStartInstruction, getProp));
                        }
                    } else {
                        if (isIncrement) {
                            output.add(new PreIncrementAVM2Item(ins, localData.lineStartInstruction, getProp));
                        } else {
                            output.add(new PreDecrementAVM2Item(ins, localData.lineStartInstruction, getProp));
                        }
                    }
                    return;
                }
            }
        }

        //assembled/TestIncrement2
        if (value instanceof DuplicateItem) {
            GraphTargetItem duplicated = value.value;
            if ((duplicated instanceof IncrementAVM2Item) || (duplicated instanceof DecrementAVM2Item)) {
                boolean isIncrement = (duplicated instanceof IncrementAVM2Item);
                if (!stack.isEmpty()) {
                    if (stack.peek() == duplicated) {
                        GraphTargetItem incrementedProp = duplicated.value;
                        if (incrementedProp instanceof GetLexAVM2Item) {
                            GetLexAVM2Item getLex = (GetLexAVM2Item) incrementedProp;
                            if (localData.abc.constants.getMultiname(multinameIndex).equals(getLex.propertyName)) {
                                stack.pop();
                                if (isIncrement) {
                                    stack.push(new PreIncrementAVM2Item(ins, localData.lineStartInstruction, getLex));
                                } else {
                                    stack.push(new PreDecrementAVM2Item(ins, localData.lineStartInstruction, getLex));
                                }
                                return;
                            }
                        }
                        if (incrementedProp instanceof GetPropertyAVM2Item) {
                            GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) incrementedProp;
                            if (((FullMultinameAVM2Item) getProp.propertyName).compareSame(multiname)) {
                                stack.pop();
                                if (isIncrement) {
                                    stack.push(new PreIncrementAVM2Item(ins, localData.lineStartInstruction, getProp));
                                } else {
                                    stack.push(new PreDecrementAVM2Item(ins, localData.lineStartInstruction, getProp));
                                }
                                return;
                            }
                        }
                    }
                }
            }
        }
        if (value instanceof LocalRegAVM2Item) {
            LocalRegAVM2Item valueLocalReg = (LocalRegAVM2Item) value;
            LocalRegAVM2Item nameLocalReg = null;
            if (multiname.name instanceof LocalRegAVM2Item) {
                nameLocalReg = (LocalRegAVM2Item) multiname.name;;
            }
            if (obj instanceof LocalRegAVM2Item) {
                LocalRegAVM2Item objLocalReg = (LocalRegAVM2Item) obj;

                if (!output.isEmpty()) {
                    if (output.get(output.size() - 1) instanceof SetLocalAVM2Item) {
                        SetLocalAVM2Item valueSetLocalReg = (SetLocalAVM2Item) output.get(output.size() - 1);
                        if ((valueSetLocalReg.value instanceof IncrementAVM2Item)
                                || (valueSetLocalReg.value instanceof DecrementAVM2Item)) {
                            boolean isIncrement = (valueSetLocalReg.value instanceof IncrementAVM2Item);
                            if (valueSetLocalReg.value.value instanceof DuplicateItem) {
                                GraphTargetItem duplicated = valueSetLocalReg.value.value.value;
                                if (!stack.isEmpty() && stack.peek() == duplicated) {
                                    GraphTargetItem notCoerced = duplicated.getNotCoerced();
                                    if (notCoerced instanceof GetPropertyAVM2Item) {
                                        GetPropertyAVM2Item getProperty = (GetPropertyAVM2Item) notCoerced;
                                        FullMultinameAVM2Item propertyName = ((FullMultinameAVM2Item) getProperty.propertyName);
                                        SetLocalAVM2Item nameSetLocalReg = null;
                                        if (propertyName.name instanceof SetLocalAVM2Item) {
                                            nameSetLocalReg = (SetLocalAVM2Item) propertyName.name;
                                        }
                                        if (getProperty.object instanceof SetLocalAVM2Item) {
                                            SetLocalAVM2Item objSetLocalReg = (SetLocalAVM2Item) getProperty.object;

                                            if ((valueLocalReg.regIndex == valueSetLocalReg.regIndex)
                                                    && (propertyName.multinameIndex == multinameIndex)
                                                    && ((nameLocalReg == null && nameSetLocalReg == null) || (nameLocalReg.regIndex == nameSetLocalReg.regIndex))
                                                    && (objLocalReg.regIndex == objSetLocalReg.regIndex)) {
                                                if (nameSetLocalReg != null) {
                                                    propertyName.name = nameSetLocalReg.value;
                                                }
                                                getProperty.object = objSetLocalReg.value;
                                                output.remove(output.size() - 1);
                                                stack.pop();
                                                if (isIncrement) {
                                                    stack.push(new PostIncrementAVM2Item(ins, localData.lineStartInstruction, getProperty));
                                                } else {
                                                    stack.push(new PostDecrementAVM2Item(ins, localData.lineStartInstruction, getProperty));
                                                }
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (!stack.isEmpty()) {
                    GraphTargetItem checked = checkIncDec(false, multinameIndex, ins, localData, stack.peek(), valueLocalReg, nameLocalReg, objLocalReg);
                    if (checked != null) {
                        stack.pop();
                        stack.push(checked);
                        return;
                    }
                }
                if (!output.isEmpty()) {
                    GraphTargetItem checked = checkIncDec(true, multinameIndex, ins, localData, output.get(output.size() - 1), valueLocalReg, nameLocalReg, objLocalReg);
                    if (checked != null) {
                        output.remove(output.size() - 1);
                        output.add(checked);
                        return;
                    }
                }
            }
        }

        if (obj.getThroughDuplicate() instanceof ConstructAVM2Item) {
            ConstructAVM2Item c = (ConstructAVM2Item) obj.getThroughDuplicate();
            if (c.object instanceof ApplyTypeAVM2Item) {
                ApplyTypeAVM2Item at = (ApplyTypeAVM2Item) c.object;
                c.args.clear();
                List<GraphTargetItem> vals = new ArrayList<>();
                vals.add(value);
                c.object = new InitVectorAVM2Item(c.getInstruction(), c.getLineStartIns(), at.params.get(0), vals);
                return;
            } else if (c.object instanceof InitVectorAVM2Item) {
                InitVectorAVM2Item iv = (InitVectorAVM2Item) c.object;
                iv.arguments.add(value);
                return;
            }
        }

        GraphTargetItem result = new SetPropertyAVM2Item(ins, localData.lineStartInstruction, obj, multiname, value);
        SetTypeIns.handleResult(value, stack, output, localData, result, -1);
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        int multinameIndex = ins.operands[0];
        return 2 + getMultinameRequiredStackSize(abc.constants, multinameIndex);
    }
}
