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
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.DecrementIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.DecrementIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.IncrementIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.IncrementIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.IncLocalIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.IncLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyStrictIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetScopeObjectIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.SetPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.SetSlotIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.DupIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceAIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceSIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.ConvertDIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.ConvertIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.ConvertSIns;
import static com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item.ins;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NanAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.UnboundedTypeItem;
import java.util.ArrayList;
import java.util.Arrays;
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
    public List<Integer> openedNamespaces;
    public int line;
    public GraphTargetItem type;
    private GraphTargetItem ns = null;
    private int regNumber = -1;
    public boolean unresolved = false;
    private int slotNumber = -1;
    private int slotScope = 0;

    public GraphTargetItem redirect;

    @Override
    public AssignableAVM2Item copy() {
        NameAVM2Item c = new NameAVM2Item(type, line, variableName, assignedValue, definition, openedNamespaces);
        c.setNs(ns);
        c.regNumber = regNumber;
        c.unresolved = unresolved;
        c.nsKind = nsKind;
        c.setIndex(index);
        return c;
    }

    public void setSlotScope(int slotScope) {
        this.slotScope = slotScope;
    }

    public int getSlotScope() {
        return slotScope;
    }

    public void setNs(GraphTargetItem ns) {
        this.ns = ns;
    }

    public void setRegNumber(int regNumber) {
        this.regNumber = regNumber;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
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

    public NameAVM2Item(GraphTargetItem type, int line, String variableName, GraphTargetItem storeValue, boolean definition, List<Integer> openedNamespaces) {
        super(storeValue);
        this.variableName = variableName;
        this.assignedValue = storeValue;
        this.definition = definition;
        this.line = line;
        this.type = type;
        this.openedNamespaces = openedNamespaces;
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
            nssa[i] = openedNamespaces.get(i);
        }
        return abc.constants.getNamespaceSetId(new NamespaceSet(nssa), true);
    }

    public static GraphTargetItem getDefaultValue(String type) {
        switch (type) {
            case "*":
                return new UndefinedAVM2Item(null);
            case "int":
                return new IntegerValueAVM2Item(null, 0L);
            case "Number":
                return new NanAVM2Item(null);
            default:
                return new NullAVM2Item(null);
        }
    }

    public static AVM2Instruction generateCoerce(SourceGenerator generator, GraphTargetItem ttype) {
        if (ttype instanceof UnresolvedAVM2Item) {
            ttype = ((UnresolvedAVM2Item) ttype).resolved;
        }
        AVM2Instruction ins;
        if (ttype instanceof UnboundedTypeItem) {
            ins = ins(new CoerceAIns());
        } else {
            TypeItem type = (TypeItem) ttype;

            if (type.subtypes.isEmpty()) {
                switch (type.fullTypeName) {
                    case "int":
                        ins = ins(new ConvertIIns());
                        break;
                    case "*":
                        ins = ins(new CoerceAIns());
                        break;
                    case "String":
                        ins = ins(new CoerceSIns());
                        break;
                    default:
                        int type_index = AVM2SourceGenerator.resolveType(type,((AVM2SourceGenerator) generator).abc);
                        ins = ins(new CoerceIns(), type_index);
                        break;
                }
            } else {
                int type_index = AVM2SourceGenerator.resolveType(type,((AVM2SourceGenerator) generator).abc);
                ins = ins(new CoerceIns(), type_index);
            }
        }
        return ins;
    }

    private List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator, boolean needsReturn) throws CompilationException {
        if (variableName != null && regNumber == -1 && slotNumber == -1 && ns == null) {
            throw new RuntimeException("No register or slot set for " + variableName);
        }
        if (definition && assignedValue == null) {
            return new ArrayList<>();
        }
        AVM2SourceGenerator g = (AVM2SourceGenerator) generator;
        Reference<Integer> ns_temp = new Reference<>(-1);
        Reference<Integer> index_temp = new Reference<>(-1);
        Reference<Integer> ret_temp = new Reference<>(-1);

        if (variableName == null && ns != null && index != null) {
            if (assignedValue != null) {
                return toSourceMerge(localData, generator,
                        ns, generateCoerce(generator, new TypeItem("Namespace")), index, ins(new ConvertSIns()), ins(new FindPropertyStrictIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAMEL, 0, 0, 0, 0, new ArrayList<Integer>()), true)),
                        ns, generateCoerce(generator, new TypeItem("Namespace")), index, ins(new ConvertSIns()), assignedValue,
                        needsReturn ? dupSetTemp(localData, generator, ret_temp) : null,
                        ins(new SetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAMEL, 0, 0, 0, 0, new ArrayList<Integer>()), true)),
                        needsReturn ? getTemp(localData, generator, ret_temp) : null,
                        killTemp(localData, generator, Arrays.asList(ns_temp, index_temp, ret_temp))
                );
            } else {
                return toSourceMerge(localData, generator,
                        ns, generateCoerce(generator, new TypeItem("Namespace")), index, ins(new ConvertSIns()), ins(new FindPropertyStrictIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAMEL, 0, 0, 0, 0, new ArrayList<Integer>()), true)),
                        ns, generateCoerce(generator, new TypeItem("Namespace")), index, ins(new ConvertSIns()), ins(new GetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAMEL, 0, 0, 0, 0, new ArrayList<Integer>()), true)),
                        needsReturn ? null : ins(new PopIns()),
                        killTemp(localData, generator, Arrays.asList(ns_temp, index_temp, ret_temp))
                );
            }
        }
        if (variableName != null && ns != null && index == null) {
            if (assignedValue != null) {
                return toSourceMerge(localData, generator,
                        ns, generateCoerce(generator, new TypeItem("Namespace")), ins(new FindPropertyStrictIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAME, g.abc.constants.getStringId(variableName, true), 0, 0, 0, new ArrayList<Integer>()), true)),
                        ns, generateCoerce(generator, new TypeItem("Namespace")), assignedValue,
                        needsReturn ? dupSetTemp(localData, generator, ret_temp) : null,
                        ins(new SetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAME, g.abc.constants.getStringId(variableName, true), 0, 0, 0, new ArrayList<Integer>()), true)),
                        needsReturn ? getTemp(localData, generator, ret_temp) : null,
                        killTemp(localData, generator, Arrays.asList(ns_temp, index_temp, ret_temp))
                );
            } else {
                return toSourceMerge(localData, generator,
                        ns, generateCoerce(generator, new TypeItem("Namespace")), ins(new FindPropertyStrictIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAME, g.abc.constants.getStringId(variableName, true), 0, 0, 0, new ArrayList<Integer>()), true)),
                        ns, generateCoerce(generator, new TypeItem("Namespace")), ins(new GetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAME, g.abc.constants.getStringId(variableName, true), 0, 0, 0, new ArrayList<Integer>()), true)),
                        needsReturn ? null : ins(new PopIns()),
                        killTemp(localData, generator, Arrays.asList(ns_temp, index_temp, ret_temp))
                );
            }
        }

        if (index != null) {
            if (assignedValue != null) {
                return toSourceMerge(localData, generator,
                        generateGetLoc(regNumber), generateGetSlot(slotScope, slotNumber), index, assignedValue,
                        needsReturn ? dupSetTemp(localData, generator, ret_temp) : null,
                        ins(new SetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.MULTINAMEL, 0, 0, allNsSet(g.abc), 0, new ArrayList<Integer>()), true)),
                        needsReturn ? getTemp(localData, generator, ret_temp) : null,
                        killTemp(localData, generator, Arrays.asList(ns_temp, index_temp, ret_temp))
                );
            } else {
                return toSourceMerge(localData, generator,
                        generateGetLoc(regNumber), index,
                        ins(new GetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.MULTINAMEL, 0, 0, allNsSet(g.abc), 0, new ArrayList<Integer>()), true)),
                        needsReturn ? null : ins(new PopIns())
                );
            }
        }

        if (assignedValue != null) {
            List<String> basicTypes = Arrays.asList("int", "Number");
            if (slotNumber > -1) {
                return toSourceMerge(localData, generator,
                        ins(new GetScopeObjectIns(), slotScope),
                        assignedValue, !(("" + assignedValue.returnType()).equals("" + type) && (basicTypes.contains("" + type))) ? generateCoerce(generator, type) : null, needsReturn
                        ? dupSetTemp(localData, generator, ret_temp) : null, generateSetLoc(regNumber), slotNumber > -1
                        ? ins(new SetSlotIns(), slotNumber)
                        : null,
                        needsReturn ? getTemp(localData, generator, ret_temp) : null,
                        killTemp(localData, generator, Arrays.asList(ret_temp)));
            } else {

                return toSourceMerge(localData, generator, assignedValue, !(("" + assignedValue.returnType()).equals("" + type) && (basicTypes.contains("" + type))) ? generateCoerce(generator, type) : null, needsReturn
                        ? ins(new DupIns()) : null, generateSetLoc(regNumber));
            }
        } else {
            return toSourceMerge(localData, generator, generateGetLoc(regNumber), generateGetSlot(slotScope, slotNumber),
                    needsReturn ? null : ins(new PopIns()));
        }
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        if (redirect != null) {
            return redirect.toSource(localData, generator);
        }
        return toSource(localData, generator, true);

    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
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
        if (type == null) {
            return TypeItem.UNBOUNDED;
        }
        return type;
    }

    @Override
    public List<GraphSourceItem> toSourceChange(SourceGeneratorLocalData localData, SourceGenerator generator, boolean post, boolean decrement, boolean needsReturn) throws CompilationException {
        if (redirect != null) {
            return ((AssignableAVM2Item) redirect).toSourceChange(localData, generator, post, decrement, needsReturn);
        }
        AVM2SourceGenerator g = (AVM2SourceGenerator) generator;
        Reference<Integer> ns_temp = new Reference<>(-1);
        Reference<Integer> name_temp = new Reference<>(-1);
        Reference<Integer> index_temp = new Reference<>(-1);
        Reference<Integer> ret_temp = new Reference<>(-1);
        boolean isInteger = returnType().toString().equals("int");
        /*
        
                
         */

        if (variableName == null && ns != null && index != null) {
            return toSourceMerge(localData, generator,
                    ns, generateCoerce(generator, new TypeItem("Namespace")), index, ins(new ConvertSIns()),
                    ins(new FindPropertyStrictIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAMEL, 0, 0, 0, 0, new ArrayList<Integer>()), true)),
                    dupSetTemp(localData, generator, name_temp),
                    ns, generateCoerce(generator, new TypeItem("Namespace")),
                    dupSetTemp(localData, generator, ns_temp),
                    //getTemp(localData, generator, ns_temp), generateCoerce(generator, "Namespace"), getTemp(localData, generator, index_temp), ins(new ConvertSIns()),
                    //Start get original
                    //getTemp(localData, generator, ns_temp), generateCoerce(generator, "Namespace"), getTemp(localData, generator, index_temp), ins(new ConvertSIns()), ins(new FindPropertyStrictIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAMEL, 0, 0, 0, 0, new ArrayList<Integer>()), true)),
                    //getTemp(localData, generator, ns_temp), generateCoerce(generator, "Namespace"), getTemp(localData, generator, index_temp), ins(new ConvertSIns()),
                    ins(new GetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.MULTINAMEL, 0, 0, allNsSet(g.abc), 0, new ArrayList<Integer>()), true)),
                    !isInteger ? ins(new ConvertDIns()) : null,
                    //End get original
                    (!post) ? (decrement ? ins(isInteger ? new DecrementIIns() : new DecrementIns()) : ins(isInteger ? new IncrementIIns() : new IncrementIns())) : null,
                    needsReturn ? ins(new DupIns()) : null,
                    (post) ? (decrement ? ins(isInteger ? new DecrementIIns() : new DecrementIns()) : ins(isInteger ? new IncrementIIns() : new IncrementIns())) : null,
                    setTemp(localData, generator, ret_temp),
                    getTemp(localData, generator, name_temp),
                    getTemp(localData, generator, ns_temp),
                    getTemp(localData, generator, ret_temp),
                    ins(new SetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.MULTINAMEL, 0, 0, allNsSet(g.abc), 0, new ArrayList<Integer>()), true)),
                    killTemp(localData, generator, Arrays.asList(ret_temp, name_temp, ns_temp)));
        }
        if (variableName != null && ns != null && index == null) {
            return toSourceMerge(localData, generator,
                    ns, generateCoerce(generator, new TypeItem("Namespace")),
                    ins(new FindPropertyStrictIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAME, g.abc.constants.getStringId(variableName, true), 0, 0, 0, new ArrayList<Integer>()), true)),
                    dupSetTemp(localData, generator, name_temp),
                    ns, generateCoerce(generator, new TypeItem("Namespace")),
                    dupSetTemp(localData, generator, ns_temp),
                    //Start get original
                    //getTemp(localData, generator, ns_temp), generateCoerce(generator, "Namespace"), ins(new FindPropertyStrictIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAME, g.abc.constants.getStringId(variableName, true), 0, 0, 0, new ArrayList<Integer>()), true)),
                    //getTemp(localData, generator, ns_temp), generateCoerce(generator, "Namespace"),
                    ins(new GetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.MULTINAMEL, 0, 0, allNsSet(g.abc), 0, new ArrayList<Integer>()), true)),
                    !isInteger ? ins(new ConvertDIns()) : null,
                    //End get original
                    (!post) ? (decrement ? ins(isInteger ? new DecrementIIns() : new DecrementIns()) : ins(isInteger ? new IncrementIIns() : new IncrementIns())) : null,
                    needsReturn ? ins(new DupIns()) : null,
                    (post) ? (decrement ? ins(isInteger ? new DecrementIIns() : new DecrementIns()) : ins(isInteger ? new IncrementIIns() : new IncrementIns())) : null,
                    setTemp(localData, generator, ret_temp),
                    getTemp(localData, generator, name_temp),
                    getTemp(localData, generator, ns_temp),
                    getTemp(localData, generator, ret_temp),
                    ins(new SetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.MULTINAMEL, 0, 0, allNsSet(g.abc), 0, new ArrayList<Integer>()), true)),
                    killTemp(localData, generator, Arrays.asList(ret_temp, name_temp, ns_temp))
            );
        }

        if (index != null) {
            return toSourceMerge(localData, generator,
                    generateGetLoc(regNumber), generateGetSlot(slotScope, slotNumber), dupSetTemp(localData, generator, name_temp), index, dupSetTemp(localData, generator, index_temp),
                    //Start get original
                    //generateGetLoc(regNumber), getTemp(localData, generator, index_temp),
                    ins(new GetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.MULTINAMEL, 0, 0, allNsSet(g.abc), 0, new ArrayList<Integer>()), true)),
                    !isInteger ? ins(new ConvertDIns()) : null,
                    //End get original
                    (!post) ? (decrement ? ins(isInteger ? new DecrementIIns() : new DecrementIns()) : ins(isInteger ? new IncrementIIns() : new IncrementIns())) : null,
                    needsReturn ? ins(new DupIns()) : null,
                    (post) ? (decrement ? ins(isInteger ? new DecrementIIns() : new DecrementIns()) : ins(isInteger ? new IncrementIIns() : new IncrementIns())) : null,
                    setTemp(localData, generator, ret_temp),
                    getTemp(localData, generator, name_temp),
                    getTemp(localData, generator, index_temp),
                    getTemp(localData, generator, ret_temp),
                    ins(new SetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.MULTINAMEL, 0, 0, allNsSet(g.abc), 0, new ArrayList<Integer>()), true)),
                    killTemp(localData, generator, Arrays.asList(ret_temp, name_temp, index_temp))
            );
        }

        if (!needsReturn) {
            if (slotNumber > -1) {
                return toSourceMerge(localData, generator,
                        ins(new GetScopeObjectIns(), slotScope),
                        generateGetSlot(slotScope, slotNumber),
                        (decrement ? ins(isInteger ? new DecrementIIns() : new DecrementIns()) : ins(isInteger ? new IncrementIIns() : new IncrementIns())),
                        ins(new SetSlotIns(), slotNumber)
                );
            } else {
                return toSourceMerge(localData, generator,
                        ins(isInteger ? new IncLocalIIns() : new IncLocalIns(), regNumber));
            }
        }
        return toSourceMerge(localData, generator,
                slotNumber > -1 ? ins(new GetScopeObjectIns(), slotScope) : null,
                //Start get original
                generateGetLoc(regNumber), generateGetSlot(slotScope, slotNumber),
                //End get original
                !isInteger ? ins(new ConvertDIns()) : null,
                //End get original
                (!post) ? (decrement ? ins(isInteger ? new DecrementIIns() : new DecrementIns()) : ins(isInteger ? new IncrementIIns() : new IncrementIns())) : null,
                needsReturn ? ins(new DupIns()) : null,
                (post) ? (decrement ? ins(isInteger ? new DecrementIIns() : new DecrementIns()) : ins(isInteger ? new IncrementIIns() : new IncrementIns())) : null,
                generateSetLoc(regNumber),
                slotNumber > -1 ? ins(new SetSlotIns(), slotNumber) : null
        );
    }

}
