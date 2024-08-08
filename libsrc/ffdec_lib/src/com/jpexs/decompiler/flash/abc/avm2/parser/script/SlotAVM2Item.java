/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Map;

/**
 * Slot.
 *
 * @author JPEXS
 */
public class SlotAVM2Item extends AVM2Item {

    /**
     * Is static
     */
    private final boolean isStatic;

    /**
     * Variable
     */
    public String var;

    /**
     * Type
     */
    public GraphTargetItem type;

    /**
     * Custom namespace
     */
    public String customNamespace;

    /**
     * Line
     */
    public int line;

    /**
     * Metadata
     */
    public List<Map.Entry<String, Map<String, String>>> metadata;

    /**
     * Package
     */
    public NamespaceItem pkg;

    /**
     * Is static.
     * @return Is static
     */
    public boolean isStatic() {
        return isStatic;
    }

    /**
     * Constructor.
     * @param metadata Metadata
     * @param pkg Package
     * @param customNamespace Custom namespace
     * @param isStatic Is static
     * @param var Variable
     * @param type Type
     * @param value Value
     * @param line Line
     */
    public SlotAVM2Item(List<Map.Entry<String, Map<String, String>>> metadata, NamespaceItem pkg, String customNamespace, boolean isStatic, String var, GraphTargetItem type, GraphTargetItem value, int line) {
        super(null, null, NOPRECEDENCE, value);
        this.metadata = metadata;
        this.pkg = pkg;
        this.line = line;
        this.isStatic = isStatic;
        this.var = var;
        this.type = type;
        this.customNamespace = customNamespace;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer; //TODO
    }

    @Override
    public GraphTargetItem returnType() {
        return type;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
