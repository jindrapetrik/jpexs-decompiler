/*
 *  Copyright (C) 2010-2016 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class CachedScript implements Serializable {

    public String text;

    public List<Highlighting> hilights;
    public List<Highlighting> methodHilights;
    public List<Highlighting> classHilights;
    public List<Highlighting> specialHilights;

    public CachedScript(String text, List<Highlighting> hilights, List<Highlighting> methodHilights, List<Highlighting> classHilights, List<Highlighting> specialHilights) {
        this.text = text;
        this.hilights = hilights;
        this.methodHilights = methodHilights;
        this.classHilights = classHilights;
        this.specialHilights = specialHilights;
    }
}
