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
package com.jpexs.decompiler.flash.abc.avm2.instructions.other;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.SetTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.ClassTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.DecrementTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.GetSlotTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.IncrementTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.NewActivationTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.PostDecrementTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.PostIncrementTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.SetSlotTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.ThisTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.TreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.clauses.ExceptionTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.operations.PreDecrementTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.operations.PreIncrementTreeItem;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class SetSlotIns extends InstructionDefinition implements SetTypeIns {

    public SetSlotIns() {
        super(0x6d, "setslot", new int[]{AVM2Code.DAT_SLOT_INDEX});
    }

    @Override
    public void translate(boolean isStatic, int scriptIndex, int classIndex, java.util.HashMap<Integer, GraphTargetItem> localRegs, Stack<GraphTargetItem> stack, java.util.Stack<GraphTargetItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<GraphTargetItem> output, com.jpexs.decompiler.flash.abc.types.MethodBody body, com.jpexs.decompiler.flash.abc.ABC abc, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        int slotIndex = ins.operands[0];
        GraphTargetItem value = (GraphTargetItem) stack.pop();
        GraphTargetItem obj = (GraphTargetItem) stack.pop(); //scopeId
        obj = obj.getThroughRegister();
        Multiname slotname = null;
        if (obj instanceof NewActivationTreeItem) {
            ((NewActivationTreeItem) obj).slots.put(slotIndex, value);
        }
        if (obj instanceof ExceptionTreeItem) {
            slotname = constants.constant_multiname[((ExceptionTreeItem) obj).exception.name_index];
        } else if (obj instanceof ClassTreeItem) {
            slotname = ((ClassTreeItem) obj).className;
        } else if (obj instanceof ThisTreeItem) {
            slotname = ((ThisTreeItem) obj).className;
        } else {
            //if(value.startsWith("catched ")) return;
            for (int t = 0; t < body.traits.traits.length; t++) {
                if (body.traits.traits[t] instanceof TraitSlotConst) {
                    if (((TraitSlotConst) body.traits.traits[t]).slot_id == slotIndex) {
                        slotname = body.traits.traits[t].getName(abc);
                    }
                }

            }
        }

        if (slotname != null) {
            if (localRegNames.containsValue(slotname.getName(constants, fullyQualifiedNames))) {
                return;
            };
        }

        if (value.getNotCoerced() instanceof IncrementTreeItem) {
            GraphTargetItem inside = ((IncrementTreeItem) value.getNotCoerced()).object.getThroughRegister().getNotCoerced();
            if (inside instanceof GetSlotTreeItem) {
                GetSlotTreeItem slotItem = (GetSlotTreeItem) inside;
                if ((slotItem.scope.getThroughRegister() == obj.getThroughRegister())
                        && (slotItem.slotName == slotname)) {
                    if (stack.size() > 0) {
                        GraphTargetItem top = stack.peek().getNotCoerced();
                        if (top == inside) {
                            stack.pop();
                            stack.push(new PostIncrementTreeItem(ins, inside));
                        } else if ((top instanceof IncrementTreeItem) && (((IncrementTreeItem) top).object == inside)) {
                            stack.pop();
                            stack.push(new PreIncrementTreeItem(ins, inside));
                        } else {
                            output.add(new PostIncrementTreeItem(ins, inside));
                        }
                    } else {
                        output.add(new PostIncrementTreeItem(ins, inside));
                    }
                    return;
                }
            }
        }

        if (value.getNotCoerced() instanceof DecrementTreeItem) {
            GraphTargetItem inside = ((DecrementTreeItem) value.getNotCoerced()).object.getThroughRegister().getNotCoerced();
            if (inside instanceof GetSlotTreeItem) {
                GetSlotTreeItem slotItem = (GetSlotTreeItem) inside;
                if ((slotItem.scope.getThroughRegister() == obj.getThroughRegister())
                        && (slotItem.slotName == slotname)) {
                    if (stack.size() > 0) {
                        GraphTargetItem top = stack.peek().getNotCoerced();
                        if (top == inside) {
                            stack.pop();
                            stack.push(new PostDecrementTreeItem(ins, inside));
                        } else if ((top instanceof DecrementTreeItem) && (((DecrementTreeItem) top).object == inside)) {
                            stack.pop();
                            stack.push(new PreDecrementTreeItem(ins, inside));
                        } else {
                            output.add(new PostDecrementTreeItem(ins, inside));
                        }
                    } else {
                        output.add(new PostDecrementTreeItem(ins, inside));
                    }
                    return;
                }
            }
        }

        output.add(new SetSlotTreeItem(ins, obj, slotname, value));
    }

    @Override
    public String getObject(Stack<TreeItem> stack, ABC abc, AVM2Instruction ins, List<TreeItem> output, com.jpexs.decompiler.flash.abc.types.MethodBody body, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        int slotIndex = ins.operands[0];
        ////String obj = stack.get(1);
        String slotname = "";
        for (int t = 0; t < body.traits.traits.length; t++) {
            if (body.traits.traits[t] instanceof TraitSlotConst) {
                if (((TraitSlotConst) body.traits.traits[t]).slot_id == slotIndex) {
                    slotname = body.traits.traits[t].getName(abc).getName(abc.constants, fullyQualifiedNames);
                }
            }

        }
        return slotname;
    }

    @Override
    public int getStackDelta(AVM2Instruction ins, ABC abc) {
        return -2;
    }
}
