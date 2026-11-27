/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.exporters.script;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Namespace directives and undeclared namespace URIs used in a source scope.
 */
public final class NamespaceUses {

    public final List<String> names;

    /**
     * The value is true when a namespace set needs a use namespace directive.
     */
    public final Map<String, Boolean> undeclared = new LinkedHashMap<>();

    public final boolean collectUndeclared;

    public NamespaceUses(List<String> names, boolean collectUndeclared) {
        this.names = names;
        this.collectUndeclared = collectUndeclared;
    }

    public void add(String name) {
        if (!names.contains(name)) {
            names.add(name);
        }
    }
}
