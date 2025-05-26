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
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Map;

/**
 * Interface.
 *
 * @author JPEXS
 */
public class InterfaceAVM2Item extends AVM2Item {

    /**
     * Base name
     */
    public String baseName;

    /**
     * Super interfaces
     */
    public List<GraphTargetItem> superInterfaces;

    /**
     * Methods
     */
    public List<GraphTargetItem> methods;

    /**
     * Is final
     */
    public boolean isFinal;

    /**
     * Opened namespaces
     */
    public List<NamespaceItem> openedNamespaces;

    /**
     * Package
     */
    public NamespaceItem pkg;

    /**
     * Imported classes
     */
    public List<DottedChain> importedClasses;

    /**
     * Metadata
     */
    public List<Map.Entry<String, Map<String, String>>> metadata;

    /**
     * Is nullable
     */
    public boolean isNullable;
    
    /**
     * Constructor.
     * @param metadata Metadata
     * @param importedClasses Imported classes
     * @param pkg Package
     * @param openedNamespaces Opened namespaces
     * @param isFinal Is final
     * @param name Name
     * @param superInterfaces Super interfaces
     * @param traits Traits
     * @param isNullable Nullable
     */
    public InterfaceAVM2Item(List<Map.Entry<String, Map<String, String>>> metadata, List<DottedChain> importedClasses, NamespaceItem pkg, List<NamespaceItem> openedNamespaces, boolean isFinal, String name, List<GraphTargetItem> superInterfaces, List<GraphTargetItem> traits, boolean isNullable) {
        super(null, null, NOPRECEDENCE);
        this.metadata = metadata;
        this.importedClasses = importedClasses;
        this.pkg = pkg;
        this.baseName = name;
        this.superInterfaces = superInterfaces;
        this.methods = traits;
        this.isFinal = isFinal;
        this.openedNamespaces = openedNamespaces;
        this.isNullable = isNullable;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer;
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
