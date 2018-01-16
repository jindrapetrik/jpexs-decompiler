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
package com.jpexs.decompiler.flash.importers.svg;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class SvgStyleProperty {

    private final String name;

    private final boolean inherited;

    private final Object initial;

    public SvgStyleProperty(String name, boolean inherited, Object initial) {
        this.name = name;
        this.inherited = inherited;
        this.initial = initial;
    }

    public String name() {
        return name;
    }

    public boolean isInherited() {
        return inherited;
    }

    public Object getInitialValue() {
        return initial;
    }

    private static final Map<String, SvgStyleProperty> properties;

    static {
        Map<String, SvgStyleProperty> p = new HashMap<>();
        p.put("color", new SvgStyleProperty("color", true, Color.BLACK /* depends on user agent */));
        p.put("fill", new SvgStyleProperty("fill", true, new SvgColor(Color.BLACK)));
        p.put("fill-opacity", new SvgStyleProperty("fill-opacity", true, 1.0));
        p.put("stroke", new SvgStyleProperty("stroke", true, null));
        p.put("stroke-width", new SvgStyleProperty("stroke-width", true, 1.0));
        p.put("stroke-opacity", new SvgStyleProperty("stroke-opacity", true, 1.0));
        p.put("stroke-linecap", new SvgStyleProperty("stroke-linecap", true, SvgLineCap.BUTT));
        p.put("stroke-linejoin", new SvgStyleProperty("stroke-linejoin", true, SvgLineJoin.MITER));
        p.put("stroke-miterlimit", new SvgStyleProperty("stroke-miterlimit", true, 4.0));
        p.put("opacity", new SvgStyleProperty("opacity", false, 1.0));
        p.put("stop-color", new SvgStyleProperty("stop-color", false, Color.BLACK));
        p.put("stop-opacity", new SvgStyleProperty("stop-opacity", false, 1.0));
        properties = p;
    }

    public static Collection<SvgStyleProperty> getProperties() {
        return properties.values();
    }

    public static SvgStyleProperty getByName(String name) {
        return properties.get(name);
    }
}
