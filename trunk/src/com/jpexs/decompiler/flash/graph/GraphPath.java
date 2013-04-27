/*
 * Copyright (C) 2013 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.graph;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class GraphPath {

    private List<Integer> parts = new ArrayList<Integer>();
    public String rootName = "";

    public GraphPath(String rootName, List<Integer> parts) {
        this.rootName = rootName;
        this.parts.addAll(parts);
    }

    public GraphPath(List<Integer> parts) {
        this.parts.addAll(parts);
    }

    public boolean startsWith(GraphPath p) {
        if (p.length() > length()) {
            return false;
        }
        for (int i = 0; i < p.length(); i++) {
            if (parts.get(i) != p.get(i)) {
                return false;
            }
        }
        return true;
    }

    public GraphPath parent(int len) {
        GraphPath par = new GraphPath(rootName);
        for (int i = 0; i < len; i++) {
            par.parts.add(parts.get(i));
        }
        return par;
    }

    public GraphPath sub(int part) {
        GraphPath next = new GraphPath(rootName, this.parts);
        next.parts.add(part);
        return next;
    }

    public GraphPath(String rootName, Integer... parts) {
        this.rootName = rootName;
        for (int p : parts) {
            this.parts.add(p);
        }
    }

    public GraphPath(Integer... parts) {
        for (int p : parts) {
            this.parts.add(p);
        }
    }

    public int length() {
        return parts.size();
    }

    public int get(int index) {
        return parts.get(index);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.parts != null ? this.parts.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GraphPath other = (GraphPath) obj;
        if (this.rootName == null && other.rootName != null) {
            return false;
        }
        if (this.rootName != null && other.rootName == null) {
            return false;
        }

        if (this.rootName != null && other.rootName != null) {
            if (!this.rootName.equals(other.rootName)) {
                return false;
            }
        }
        if (this.parts.size() != other.parts.size()) {
            return false;
        }
        for (int i = 0; i < this.parts.size(); i++) {
            if (this.parts.get(i) != other.parts.get(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String ret = rootName;
        for (int i : parts) {
            ret += "/" + i;
        }
        return ret;
    }
}
