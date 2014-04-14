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
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.ConstructIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.ConstructPropIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyStrictIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetLexIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.ApplyTypeIns;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ConstructSomethingAVM2Item extends CallAVM2Item {

    public List<Integer> openedNamespaces;
    
    public ConstructSomethingAVM2Item(List<Integer> openedNamespaces,GraphTargetItem name, List<GraphTargetItem> arguments) {
        super(name, arguments);
        this.openedNamespaces = openedNamespaces;
    }

    @Override
    public GraphTargetItem returnType() {
        return name.returnType();
    }

    private int allNsSetWithVec(ABC abc) {
        int nssa[] = new int[openedNamespaces.size()+1];
        for (int i = 0; i < openedNamespaces.size(); i++) {
            nssa[i] = openedNamespaces.get(i);
        }
        nssa[nssa.length-1] = abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE, abc.constants.getStringId("__AS3__.vec", true)), 0, true);                            
        return abc.constants.getNamespaceSetId(new NamespaceSet(nssa), true);
        
    }
    
    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {

        GraphTargetItem resname = name;
        if (resname instanceof UnresolvedAVM2Item) {
            resname = ((UnresolvedAVM2Item) resname).resolved;
        }
        if (resname instanceof TypeItem) {
            TypeItem prop = (TypeItem) resname;
            if(!prop.subtypes.isEmpty()){ //It's Vector - TypeName
                //int qname = ((AVM2SourceGenerator) generator).resolveType(prop.fullTypeName);
                String name = prop.fullTypeName.substring(prop.fullTypeName.lastIndexOf(".")+1);
                ABC abc=((AVM2SourceGenerator)generator).abc;
                int qname = abc.constants.getMultinameId(new Multiname(Multiname.MULTINAME,abc.constants.getStringId(name,true) , 0,allNsSetWithVec(abc), 0, new ArrayList<Integer>()), true);
                List<Integer> params=new ArrayList<>();
                for(String p:prop.subtypes){
                    params.add(((AVM2SourceGenerator) generator).resolveType(p));
                }
                List<GraphSourceItem> ret=new ArrayList<>();
                ret.add(ins(new GetLexIns(),qname));
                for(int p:params){
                    ret.add(ins(new GetLexIns(),p));
                }
                ret.add(ins(new ApplyTypeIns(),params.size()));
                ret.addAll(toSourceMerge(localData, generator, arguments,
                        ins(new ConstructIns(),arguments.size())
                        ));
                return ret;
                        
            }else{
            int type_index = ((AVM2SourceGenerator) generator).resolveType(resname.toString());
            return toSourceMerge(localData, generator,
                    new AVM2Instruction(0, new FindPropertyStrictIns(), new int[]{type_index, arguments.size()}, new byte[0]), arguments,
                    new AVM2Instruction(0, new ConstructPropIns(), new int[]{type_index, arguments.size()}, new byte[0])
            );
            }
        }
        if (resname instanceof NameAVM2Item) {
            //TODO
        }
        return new ArrayList<>();
    }

}
