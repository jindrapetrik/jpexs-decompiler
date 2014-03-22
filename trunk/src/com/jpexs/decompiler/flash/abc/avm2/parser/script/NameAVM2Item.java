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
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.ParseException;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class NameAVM2Item extends AVM2Item {

    private AVM2Item it;

    private String variableName;
    private GraphTargetItem storeValue;
    private boolean definition;
    private GraphTargetItem index;
    private int nsKind = -1;
    public List<String> openedNamespaces;
    public List<Integer> openedNamespacesKind;
    public int line;

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

    public void setStoreValue(GraphTargetItem storeValue) {
        this.storeValue = storeValue;
    }

    public String getVariableName() {
        return variableName;
    }

    public NameAVM2Item(int line,String variableName, GraphTargetItem storeValue, boolean definition, List<String> openedNamespaces, List<Integer> openedNamespacesKind) {
        super(null, PRECEDENCE_PRIMARY);
        this.variableName = variableName;
        this.storeValue = storeValue;
        this.definition = definition;
        this.line = line;
    }

    public boolean isDefinition() {
        return definition;
    }

    public void setBoxedValue(AVM2Item it) {
        this.it = it;
        if (it != null) {
            this.precedence = it.getPrecedence();
        }
    }

    public AVM2Item getBoxedValue() {
        return it;
    }

    public GraphTargetItem getStoreValue() {
        return storeValue;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (it == null) {
            return writer;
        }
        return it.appendTo(writer, localData);
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) {
        if (it == null) {
            return new ArrayList<>();
        }
        return it.toSource(localData, generator);
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) {
        if (it == null) {
            return new ArrayList<>();
        }
        return it.toSourceIgnoreReturnValue(localData, generator);
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

    public GraphTargetItem resolve(boolean typeOnly,List<NameAVM2Item> variables,AVM2SourceGenerator generator) throws ParseException {
        String name = toString();
        String parts[] = new String[]{name};
        if (name.contains(".")) {
            parts = name.split(".");
        }

        String pkg = "";
        int k;
        loopk:for (k = parts.length - 1; k >= 0; k--) {
            pkg = "";
            for (int j = 0; j <= k - 1; j++) {
                if (!"".equals(pkg)) {
                    pkg += ".";
                }
                pkg += parts[j];
            }
            name = parts[k];

            int nsKind = Namespace.KIND_PACKAGE;
            //if (pkg.equals("")) {
            for (int i = 0; i < openedNamespaces.size(); i++) {
                String ns = openedNamespaces.get(i);
                String nspkg = ns;
                String nsclass = null;
                if (nspkg.contains(":")) {
                    nsclass = nspkg.substring(nspkg.indexOf(":") + 1);
                    nspkg = nspkg.substring(0, nspkg.indexOf(":"));
                }
                if (nspkg.equals(pkg)) {
                    if (nsclass == null) {
                        List<ABC> abcs = new ArrayList<>();
                        abcs.add(generator.abc);
                        abcs.addAll(generator.allABCs);
                        loopabc:
                        for (ABC a : abcs) {
                            for (InstanceInfo ii : a.instance_info) {
                                Multiname n = a.constants.constant_multiname.get(ii.name_index);
                                if (n.getNamespace(a.constants).getName(a.constants).equals(nspkg) && n.getName(a.constants, new ArrayList<String>()).equals(name)) {
                                    
                                    nsKind = n.getNamespace(a.constants).kind;
                                    break loopk;
                                }
                            }
                        }
                    } else if (name.equals(nsclass)) {
                        nsKind = openedNamespacesKind.get(i);
                        break loopk;
                    }
                }
            }
            //}
        }
        
        GraphTargetItem ret;
        if(k<0){ //Class not found, its variable                        
            if(typeOnly){
                throw new ParseException("Undefined type", line);
            }
            this.variableName = null;
            for(NameAVM2Item n:variables){
                if(n.definition && n.getVariableName().equals(parts[0])){
                    this.variableName = parts[0];
                }
            }
            
            if(this.variableName == null){
                throw new ParseException("Undefined variable", line);
            }
            ret=this;
            for(int i=1;i<parts.length;i++){
                ret = new GetPropertyAVM2Item(null, ret, new NameAVM2Item(line,parts[i], null, false, openedNamespaces, openedNamespacesKind));
            }
            return ret;
        }else{
            this.variableName = null; //resolved
            ret= new FullMultinameAVM2Item(null, generator.abc.constants.getMultinameId(new Multiname(Multiname.QNAME, generator.str(name), generator.namespace(nsKind, pkg), 0, 0, new ArrayList<Integer>()), true));
            /*for(int i=k+1;i<parts.length;i++){
                ret = new GetPropertyAVM2Item(null, ret, new FullMultinameAVM2Item(null,new Multiname(Multiname.QNAME, nsKind, precedence, precedence, nsKind, openedNamespacesKind)line,parts[i], null, false, openedNamespaces, openedNamespacesKind));
            }*/
            //TODO
        }        
        setBoxedValue((AVM2Item)ret);
        return ret;                        
    }
}
