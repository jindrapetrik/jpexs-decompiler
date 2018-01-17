/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.UnboundedTypeItem;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class PackageAVM2Item extends AVM2Item {

    public List<GraphTargetItem> items;

    public DottedChain packageName;

    public List<DottedChain> importedClasses = new ArrayList<>();

    public PackageAVM2Item(List<DottedChain> importedClasses, DottedChain packageName, List<GraphTargetItem> items) {
        super(null, null, NOPRECEDENCE);
        this.importedClasses = importedClasses;
        this.items = items;
        this.packageName = packageName;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer;
    }

    @Override
    public GraphTargetItem returnType() {
        return new UnboundedTypeItem(); //FIXME
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
