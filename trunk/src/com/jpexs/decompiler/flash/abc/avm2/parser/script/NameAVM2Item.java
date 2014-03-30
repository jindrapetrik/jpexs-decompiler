/*
 * Copyright (C) 2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocal0Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocal1Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocal2Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocal3Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.KillIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocal0Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocal1Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocal2Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocal3Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyStrictIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.SetPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.DupIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceAIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceSIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.ConvertSIns;
import com.jpexs.decompiler.flash.abc.avm2.model.CoerceAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class NameAVM2Item extends AssignableAVM2Item {

    private String variableName;

    private boolean definition;
    private GraphTargetItem index;
    private int nsKind = -1;
    public List<String> openedNamespaces;
    public List<Integer> openedNamespacesKind;
    public int line;
    public GraphTargetItem type;
    private GraphTargetItem ns = null;
    private int regNumber = -1;
    public boolean unresolved = false;

    public GraphTargetItem redirect;

    public void setNs(GraphTargetItem ns) {
        this.ns = ns;
    }

    public void setRegNumber(int regNumber) {
        this.regNumber = regNumber;
    }

    public int getRegNumber() {
        return regNumber;
    }

    public GraphTargetItem getNs() {
        return ns;
    }

    public void appendName(String name) {
        this.variableName += "." + name;
    }

    public void setDefinition(boolean definition) {
        this.definition = definition;
    }

    public void setIndex(GraphTargetItem index) {
        this.index = index;
    }

    public GraphTargetItem getIndex() {
        return index;
    }

    public void setNsKind(int nsKind) {
        this.nsKind = nsKind;
    }

    public int getNsKind() {
        return nsKind;
    }

    @Override
    public void setAssignedValue(GraphTargetItem storeValue) {
        this.assignedValue = storeValue;
    }

    public String getVariableName() {
        return variableName;
    }

    public NameAVM2Item(GraphTargetItem type, int line, String variableName, GraphTargetItem storeValue, boolean definition, List<String> openedNamespaces, List<Integer> openedNamespacesKind) {
        super(storeValue);
        this.variableName = variableName;
        this.assignedValue = storeValue;
        this.definition = definition;
        this.line = line;
        this.type = type;
        this.openedNamespaces = openedNamespaces;
        this.openedNamespacesKind = openedNamespacesKind;
    }

    public boolean isDefinition() {
        return definition;
    }

    public GraphTargetItem getStoreValue() {
        return assignedValue;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer;
    }

    private int allNsSet(ABC abc) {
        int nssa[] = new int[openedNamespaces.size()];
        for (int i = 0; i < openedNamespaces.size(); i++) {
            nssa[i] = (abc.constants.getNamespaceId(new Namespace(openedNamespacesKind.get(i), abc.constants.getStringId(openedNamespaces.get(i), true)), 0, true));
        }
        return abc.constants.getNamespaceSetId(new NamespaceSet(nssa), true);
    }

    private AVM2Instruction generateSetLoc(int regNumber) {
        switch (regNumber) {
            case 0:
                return ins(new SetLocal0Ins());
            case 1:
                return ins(new SetLocal1Ins());
            case 2:
                return ins(new SetLocal2Ins());
            case 3:
                return ins(new SetLocal3Ins());
            default:
                return ins(new SetLocalIns(), regNumber);
        }
    }

    private AVM2Instruction generateGetLoc(int regNumber) {
        switch (regNumber) {
            case 0:
                return ins(new GetLocal0Ins());
            case 1:
                return ins(new GetLocal1Ins());
            case 2:
                return ins(new GetLocal2Ins());
            case 3:
                return ins(new GetLocal3Ins());
            default:
                return ins(new GetLocalIns(), regNumber);
        }
    }

    private AVM2Instruction generateCoerce(SourceGenerator generator, String type) {
        AVM2Instruction ins;
        switch (type) {
            case "*":
                ins = ins(new CoerceAIns());
                break;
            case "String":
                ins = ins(new CoerceSIns());
                break;
            default:
                int type_index = new TypeItem(type).resolveClass(((AVM2SourceGenerator) generator).abc);
                ins = ins(new CoerceIns(), type_index);
                break;
        }
        return ins;
    }

    private List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator, boolean needsReturn) {
        if (variableName != null && regNumber == -1 && ns == null) {
            throw new RuntimeException("No register set for " + variableName);
        }
        if (definition && assignedValue == null) {
            return new ArrayList<>();
        }
        AVM2SourceGenerator g = (AVM2SourceGenerator) generator;

        if (variableName == null && ns != null && index != null) {
            if (assignedValue != null) {
                List<GraphSourceItem> ret = toSourceMerge(localData, generator,
                        ns, generateCoerce(generator, "Namespace"), index, ins(new ConvertSIns()), ins(new FindPropertyStrictIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAMEL, 0, 0, 0, 0, new ArrayList<Integer>()), true)),
                        ns, generateCoerce(generator, "Namespace"), index, ins(new ConvertSIns()), assignedValue);
                int tempReg = -1;
                if (needsReturn) {
                    tempReg = getFreeRegister(localData, generator);
                    ret.add(ins(new DupIns()));
                    ret.add(generateSetLoc(tempReg));
                }
                ret.addAll(toSourceMerge(localData, generator,
                        ins(new SetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAMEL, 0, 0, 0, 0, new ArrayList<Integer>()), true))
                ));
                if (needsReturn) {
                    ret.add(generateGetLoc(tempReg));
                    ret.add(ins(new KillIns(), tempReg));
                    killRegister(localData, generator, tempReg);
                }
                return ret;
            } else {
                return toSourceMerge(localData, generator,
                        ns, generateCoerce(generator, "Namespace"), index, ins(new ConvertSIns()), ins(new FindPropertyStrictIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAMEL, 0, 0, 0, 0, new ArrayList<Integer>()), true)),
                        ns, generateCoerce(generator, "Namespace"), index, ins(new ConvertSIns()), ins(new GetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAMEL, 0, 0, 0, 0, new ArrayList<Integer>()), true)),
                        needsReturn ? null : ins(new PopIns())
                );
            }
        }
        if (variableName != null && ns != null && index == null) {
            if (assignedValue != null) {
                List<GraphSourceItem> ret = toSourceMerge(localData, generator,
                        ns, generateCoerce(generator, "Namespace"), ins(new FindPropertyStrictIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAME, g.abc.constants.getStringId(variableName, true), 0, 0, 0, new ArrayList<Integer>()), true)),
                        ns, generateCoerce(generator, "Namespace"), assignedValue);
                int tempReg = -1;
                if (needsReturn) {
                    tempReg = getFreeRegister(localData, generator);
                    ret.add(ins(new DupIns()));
                    ret.add(generateSetLoc(tempReg));
                }
                ret.addAll(toSourceMerge(localData, generator,
                        ins(new SetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAME, g.abc.constants.getStringId(variableName, true), 0, 0, 0, new ArrayList<Integer>()), true))
                ));
                if (needsReturn) {
                    ret.add(generateGetLoc(tempReg));
                    ret.add(ins(new KillIns(), tempReg));
                    killRegister(localData, generator, tempReg);
                }
                return ret;
            } else {
                return toSourceMerge(localData, generator,
                        ns, generateCoerce(generator, "Namespace"), ins(new FindPropertyStrictIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAME, g.abc.constants.getStringId(variableName, true), 0, 0, 0, new ArrayList<Integer>()), true)),
                        ns, generateCoerce(generator, "Namespace"), ins(new GetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAME, g.abc.constants.getStringId(variableName, true), 0, 0, 0, new ArrayList<Integer>()), true)),
                        needsReturn ? null : ins(new PopIns())
                );
            }
        }

        if (index != null) {
            if (assignedValue != null) {
                List<GraphSourceItem> ret = toSourceMerge(localData, generator,
                        generateGetLoc(regNumber), index, assignedValue);
                int tempReg = -1;
                if (needsReturn) {
                    tempReg = getFreeRegister(localData, generator);
                    ret.add(ins(new DupIns()));
                    ret.add(generateSetLoc(tempReg));
                }
                ret.addAll(toSourceMerge(localData, generator, ins(new SetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.MULTINAMEL, 0, 0, allNsSet(g.abc), 0, new ArrayList<Integer>()), true))
                ));
                if (needsReturn) {
                    ret.add(generateGetLoc(tempReg));
                    ret.add(ins(new KillIns(), tempReg));
                    killRegister(localData, generator, tempReg);
                }
                return ret;
            } else {
                return toSourceMerge(localData, generator,
                        generateGetLoc(regNumber), index,
                        ins(new GetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.MULTINAMEL, 0, 0, allNsSet(g.abc), 0, new ArrayList<Integer>()), true)),
                        needsReturn ? null : ins(new PopIns())
                );
            }
        }

        if (assignedValue != null) {
            return toSourceMerge(localData, generator, assignedValue, generateCoerce(generator, type.toString()), needsReturn
                    ? ins(new DupIns()) : null, generateSetLoc(regNumber));
        } else {
            return toSourceMerge(localData, generator, generateGetLoc(regNumber),
                    needsReturn ? null : ins(new PopIns()));
        }
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) {
        if (redirect != null) {
            return redirect.toSource(localData, generator);
        }
        return toSource(localData, generator, true);

    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) {
        if (redirect != null) {
            return redirect.toSourceIgnoreReturnValue(localData, generator);
        }
        return toSource(localData, generator, false);
    }

    @Override
    public boolean hasReturnValue() {
        if (definition) {
            return false;
        }
        return true;
    }

    @Override
    public boolean needsSemicolon() {
        if (definition) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return variableName;
    }

    @Override
    public GraphTargetItem returnType() {
        if (index != null) {
            return TypeItem.UNBOUNDED;
        }
        return type;
    }

    @Override
    public List<GraphSourceItem> toSourceChange(SourceGeneratorLocalData localData, SourceGenerator generator, List<GraphSourceItem> pre, List<GraphSourceItem> post, boolean needsReturn) {
        if (redirect != null) {
            return ((AssignableAVM2Item) redirect).toSourceChange(localData, generator, pre, post, needsReturn);
        }
        AVM2SourceGenerator g = (AVM2SourceGenerator) generator;

        if (variableName == null && ns != null && index != null) {
            List<GraphSourceItem> ret = toSourceMerge(localData, generator,
                    ns, generateCoerce(generator, "Namespace"), index, ins(new ConvertSIns()), ins(new FindPropertyStrictIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAMEL, 0, 0, 0, 0, new ArrayList<Integer>()), true)),
                    ns, generateCoerce(generator, "Namespace"), index, ins(new ConvertSIns()),
                    //Start get original
                    ns, generateCoerce(generator, "Namespace"), index, ins(new ConvertSIns()), ins(new FindPropertyStrictIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAMEL, 0, 0, 0, 0, new ArrayList<Integer>()), true)),
                    ns, generateCoerce(generator, "Namespace"), index, ins(new ConvertSIns()), ins(new GetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAMEL, 0, 0, 0, 0, new ArrayList<Integer>()), true)),
                    //End get original
                    pre);
            int tempReg = -1;
            if (needsReturn) {
                tempReg = getFreeRegister(localData, generator);
                ret.add(ins(new DupIns()));
                ret.add(generateSetLoc(tempReg));
            }
            ret.addAll(toSourceMerge(localData, generator,
                    post,
                    ins(new SetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAMEL, 0, 0, 0, 0, new ArrayList<Integer>()), true))
            ));
            if (needsReturn) {
                ret.add(generateGetLoc(tempReg));
                ret.add(ins(new KillIns(), tempReg));
                killRegister(localData, generator, tempReg);
            }
            return ret;
        }
        if (variableName != null && ns != null && index == null) {
            List<GraphSourceItem> ret = toSourceMerge(localData, generator,
                    ns, generateCoerce(generator, "Namespace"), ins(new FindPropertyStrictIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAME, g.abc.constants.getStringId(variableName, true), 0, 0, 0, new ArrayList<Integer>()), true)),
                    ns, generateCoerce(generator, "Namespace"),
                    //Start get original
                    ns, generateCoerce(generator, "Namespace"), ins(new FindPropertyStrictIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAME, g.abc.constants.getStringId(variableName, true), 0, 0, 0, new ArrayList<Integer>()), true)),
                    ns, generateCoerce(generator, "Namespace"), ins(new GetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAME, g.abc.constants.getStringId(variableName, true), 0, 0, 0, new ArrayList<Integer>()), true)),
                    //End get original
                    pre);
            int tempReg = -1;
            if (needsReturn) {
                tempReg = getFreeRegister(localData, generator);
                ret.add(ins(new DupIns()));
                ret.add(generateSetLoc(tempReg));
            }
            ret.addAll(toSourceMerge(localData, generator,
                    post,
                    ins(new SetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAME, g.abc.constants.getStringId(variableName, true), 0, 0, 0, new ArrayList<Integer>()), true))
            ));
            if (needsReturn) {
                ret.add(generateGetLoc(tempReg));
                ret.add(ins(new KillIns(), tempReg));
                killRegister(localData, generator, tempReg);
            }
            return ret;
        }

        if (index != null) {
            List<GraphSourceItem> ret = toSourceMerge(localData, generator,
                    generateGetLoc(regNumber), index,
                    //Start get original
                    generateGetLoc(regNumber), index,
                    ins(new GetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.MULTINAMEL, 0, 0, allNsSet(g.abc), 0, new ArrayList<Integer>()), true)),
                    //End get original
                    pre);
            int tempReg = -1;
            if (needsReturn) {
                tempReg = getFreeRegister(localData, generator);
                ret.add(ins(new DupIns()));
                ret.add(generateSetLoc(tempReg));
            }
            ret.addAll(toSourceMerge(localData, generator,
                    post,
                    ins(new SetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.MULTINAMEL, 0, 0, allNsSet(g.abc), 0, new ArrayList<Integer>()), true))
            ));
            if (needsReturn) {
                ret.add(generateGetLoc(tempReg));
                ret.add(ins(new KillIns(), tempReg));
                killRegister(localData, generator, tempReg);
            }
            return ret;
        }

        return toSourceMerge(localData, generator,
                //Start get original
                generateGetLoc(regNumber),
                //End get original
                pre, generateCoerce(generator, type.toString()), needsReturn
                ? ins(new DupIns()) : null, post, generateSetLoc(regNumber));
    }

}
