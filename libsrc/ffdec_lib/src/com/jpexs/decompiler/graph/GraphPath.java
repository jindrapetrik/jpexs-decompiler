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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class GraphPath implements Serializable {

    private final List<Integer> keys = new ArrayList<>();

    private final List<Integer> vals = new ArrayList<>();

    public final String rootName;

    public GraphPath(String rootName, List<Integer> keys, List<Integer> vals) {
        this.rootName = rootName;
        this.keys.addAll(keys);
        this.vals.addAll(vals);
    }

    public GraphPath(List<Integer> keys, List<Integer> vals) {
        rootName = "";
        this.keys.addAll(keys);
        this.vals.addAll(vals);
    }

    public GraphPath() {
        rootName = "";
    }

    public boolean startsWith(GraphPath p) {
        if (p.length() > length()) {
            return false;
        }

        List<Integer> otherKeys = new ArrayList<>(p.keys);
        List<Integer> otherVals = new ArrayList<>(p.vals);

        for (int i = 0; i < p.length(); i++) {
            if (!Objects.equals(keys.get(i), otherKeys.get(i))) {
                return false;
            }
            if (!Objects.equals(vals.get(i), otherVals.get(i))) {
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
        hash = 23 * hash + arrHashCode(keys);
        hash = 23 * hash + arrHashCode(vals);
        hash = 23 * hash + Objects.hashCode(rootName);
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
        if ((rootName == null) != (other.rootName == null)) {
            return false;
        }

        if (!Objects.equals(rootName, other.rootName)) {
            return false;
        }

        if (!arrMatch(keys, other.keys)) {
            return false;
        }

        if (!arrMatch(vals, other.vals)) {
            return false;
        }

        return true;
    }

    private static int arrHashCode(List<Integer> arr) {
        if (arr == null || arr.isEmpty()) {
            return 0;
        }

        int hash = 5;
        for (Integer i : arr) {
            hash = 23 * hash + Objects.hashCode(i);
        }

        return hash;
    }

    private static boolean arrMatch(List<Integer> arr, List<Integer> arr2) {
        if (arr.size() != arr2.size()) {
            return false;
        }
        for (int i = 0; i < arr.size(); i++) {
            if (!Objects.equals(arr.get(i), arr2.get(i))) {
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
