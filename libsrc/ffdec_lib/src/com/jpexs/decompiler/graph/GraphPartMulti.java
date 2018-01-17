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
package com.jpexs.decompiler.graph;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class GraphPartMulti extends GraphPart {

    public List<GraphPart> parts;

    public GraphPartMulti(List<GraphPart> parts) {
        super(parts.get(0).start, parts.get(parts.size() - 1).end);
        this.parts = parts;
        this.path = parts.get(0).path;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("[multi ");
        boolean first = true;
        for (GraphPart g : parts) {
            if (first) {
                first = false;
            } else {
                ret.append(", ");
            }
            ret.append(g.toString());
        }
        ret.append("]");
        return ret.toString();
    }

    @Override
    public int getHeight() {
        int ret = 0;
        for (GraphPart p : parts) {
            ret += p.getHeight();
        }
        return ret;
    }

    @Override
    public int getPosAt(int offset) {
        int ofs = 0;
        int pos = 0;
        for (GraphPart p : parts) {
            for (int i = 0; i < p.getHeight(); i++) {
                pos = p.start + i;
                ofs += 1;
                if (ofs == offset) {
                    return pos;
                }
            }
        }
        return -1;
    }

    @Override
    public List<GraphPart> getSubParts() {
        return Collections.unmodifiableList(parts);
    }
}
