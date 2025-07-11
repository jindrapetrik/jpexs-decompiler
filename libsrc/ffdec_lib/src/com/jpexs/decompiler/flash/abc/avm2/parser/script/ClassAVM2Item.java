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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class.
 *
 * @author JPEXS
 */
public class ClassAVM2Item extends AVM2Item implements Block {

    /**
     * Traits
     */
    public List<GraphTargetItem> traits;

    /**
     * Extends
     */
    public GraphTargetItem extendsOp;

    /**
     * Implements
     */
    public List<GraphTargetItem> implementsOp;

    /**
     * Class base name
     */
    public String classBaseName;

    /**
     * Instance initializer
     */
    public GraphTargetItem iinit;

    /**
     * Is dynamic
     */
    public boolean isDynamic;

    /**
     * Is final
     */
    public boolean isFinal;

    /**
     * Opened namespaces
     */
    public List<NamespaceItem> openedNamespaces;

    /**
     * Static initializer
     */
    public List<GraphTargetItem> staticInit;

    /**
     * Static initializer activation
     */
    public boolean cinitActivation;

    /**
     * Instance initializer activation
     */
    public boolean iinitActivation;

    /**
     * Static initializer variables
     */
    public List<AssignableAVM2Item> cinitVariables;

    /**
     * Imported classes
     */
    public List<DottedChain> importedClasses;

    /**
     * Package
     */
    public NamespaceItem pkg;

    /**
     * Instance initializer variables
     */
    public List<AssignableAVM2Item> iinitVariables;

    /**
     * Metadata
     */
    public List<Map.Entry<String, Map<String, String>>> metadata;
    
    /**
     * Is nullable
     */
    public boolean isNullable;

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        if (traits != null) {
            ret.add(traits);
        }
        return ret;
    }

    @Override
    public void visitNoBlock(GraphTargetVisitorInterface visitor) {

    }

    /**
     * Constructor.
     * @param metadata Metadata
     * @param importedClasses Imported classes
     * @param pkg Package
     * @param openedNamespaces Opened namespaces
     * @param isFinal Is final
     * @param isDynamic Is dynamic
     * @param className Class name
     * @param extendsOp Extends
     * @param implementsOp Implements
     * @param cinit Static initializer
     * @param staticInitActivation Static initializer activation
     * @param cinitVariables Static initializer variables
     * @param iinit Instance initializer
     * @param iinitVariables Instance initializer variables
     * @param traits Traits
     * @param iinitActivation Instance initializer activation
     * @param isNullable Nullable
     */
    public ClassAVM2Item(List<Map.Entry<String, Map<String, String>>> metadata, List<DottedChain> importedClasses, NamespaceItem pkg, List<NamespaceItem> openedNamespaces, boolean isFinal, boolean isDynamic, String className, GraphTargetItem extendsOp, List<GraphTargetItem> implementsOp, List<GraphTargetItem> cinit, boolean staticInitActivation, List<AssignableAVM2Item> cinitVariables, GraphTargetItem iinit, List<AssignableAVM2Item> iinitVariables, List<GraphTargetItem> traits, boolean iinitActivation, boolean isNullable) {
        super(null, null, NOPRECEDENCE);
        this.metadata = metadata;
        this.importedClasses = importedClasses;
        this.pkg = pkg;
        this.classBaseName = className;
        this.traits = traits;
        this.extendsOp = extendsOp;
        this.implementsOp = implementsOp;
        this.iinitActivation = iinitActivation;
        this.iinit = iinit;
        this.isDynamic = isDynamic;
        this.isFinal = isFinal;
        this.openedNamespaces = openedNamespaces;
        this.staticInit = cinit;
        this.cinitActivation = staticInitActivation;
        this.cinitVariables = cinitVariables;
        this.iinitVariables = iinitVariables;
        this.isNullable = isNullable;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer;
    }

    @Override
    public List<ContinueItem> getContinues() {
        List<ContinueItem> ret = new ArrayList<>();
        return ret;
    }
    
    @Override
    public List<BreakItem> getBreaks() {
        List<BreakItem> ret = new ArrayList<>();
        return ret;
    }
    

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED; //FIXME
    }
}
