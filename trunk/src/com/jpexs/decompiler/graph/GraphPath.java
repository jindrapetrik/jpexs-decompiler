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
package com.jpexs.decompiler.graph;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class GraphPath {

    private List<Integer> keys = new ArrayList<>();
    private List<Integer> vals = new ArrayList<>();
    public String rootName = "";

    public GraphPath(String rootName, List<Integer> keys, List<Integer> vals) {
        this.rootName = rootName;
        this.keys.addAll(keys);
        this.vals.addAll(vals);
    }

    public GraphPath(List<Integer> keys, List<Integer> vals) {
        this.keys.addAll(keys);
        this.vals.addAll(vals);
    }

    public boolean startsWith(GraphPath p) {
        if (p.length() > length()) {
            return false;
        }


        List<Integer> otherKeys = new ArrayList<>(p.keys);
        List<Integer> otherVals = new ArrayList<>(p.vals);

        for (int i = 0; i < p.length(); i++) {
            if (keys.get(i) != otherKeys.get(i)) {
                return false;
            }
            if (vals.get(i) != otherVals.get(i)) {
                return false;
            }
        }
        return true;
    }

    public GraphPath parent(int len) {
        GraphPath par = new GraphPath(rootName);
        for (int i = 0; i < len; i++) {
            par.keys.add(keys.get(i));
            par.vals.add(vals.get(i));
        }
        return par;
    }

    public GraphPath sub(int part, int codePos) {
        GraphPath next = new GraphPath(rootName, this.keys, this.vals);
        next.keys.add(codePos);
        next.vals.add(part);
        return next;
    }

    public GraphPath(String rootName) {
        this.rootName = rootName;
    }

    public GraphPath() {
    }

    public int length() {
        return vals.size();
    }

    public int get(int index) {
        return vals.get(index);
    }

    public int getKey(int index) {
        return keys.get(index);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (this.keys != null ? this.keys.hashCode() : 0);
        hash = 23 * hash + (this.vals != null ? this.vals.hashCode() : 0);
        hash = 23 * hash + (this.rootName != null ? this.rootName.hashCode() : 0);
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

        if (!arrMatch(keys, other.keys)) {
            return false;
        }
        if (!arrMatch(vals, other.vals)) {
            return false;
        }
        return true;
    }

    private static boolean arrMatch(List<Integer> arr, List<Integer> arr2) {
        if (arr.size() != arr2.size()) {
            return false;
        }
        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i) != arr2.get(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String ret = rootName;
        for (int i = 0; i < keys.size(); i++) {
            ret += "/" + keys.get(i) + ":" + vals.get(i);
        }
        return ret;
    }
}
