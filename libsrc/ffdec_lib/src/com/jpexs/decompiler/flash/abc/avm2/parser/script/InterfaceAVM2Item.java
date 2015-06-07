/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.UnboundedTypeItem;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class InterfaceAVM2Item extends AVM2Item {

    public String name;

    public List<GraphTargetItem> superInterfaces;

    public List<GraphTargetItem> methods;

    public int namespace;

    public boolean isFinal;

    public List<Integer> openedNamespaces;

    public String pkg;

    public List<DottedChain> importedClasses;

    public InterfaceAVM2Item(List<DottedChain> importedClasses, String pkg, List<Integer> openedNamespaces, boolean isFinal, int namespace, String name, List<GraphTargetItem> superInterfaces, List<GraphTargetItem> traits) {
        super(null, NOPRECEDENCE);
        this.importedClasses = importedClasses;
        this.pkg = pkg;
        this.name = name;
        this.superInterfaces = superInterfaces;
        this.methods = traits;
        this.namespace = namespace;
        this.isFinal = isFinal;
        this.openedNamespaces = openedNamespaces;
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
        return new UnboundedTypeItem(); //FIXME
    }
}
