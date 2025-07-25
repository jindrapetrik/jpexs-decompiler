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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;

/**
 * Package.
 * @author JPEXS
 */
public class PackageAVM2Item extends AVM2Item {

    private final List<GraphTargetItem> items;
    private final String packageName;
   
    public PackageAVM2Item(List<GraphTargetItem> items, String packageName) {
        super(null, null, PRECEDENCE_PRIMARY);
        this.items = items;
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }
    
    public void addItem(GraphTargetItem item) {
        items.add(item);
    }
        
    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.appendNoHilight("package");
        if (!packageName.isEmpty()) {
            writer.appendNoHilight(" " + DottedChain.parseWithSuffix(packageName).toPrintableString(localData.usedDeobfuscations, localData.swf, true));
        }
        writer.startBlock();
        Graph.graphToString(items, writer, localData);
        writer.endBlock();
        writer.newLine();
        return writer;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public GraphTargetItem returnType() {
        return null;
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }        

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visitAll(items);
    }

    @Override
    public void visitNoBlock(GraphTargetVisitorInterface visitor) {
    }
}
