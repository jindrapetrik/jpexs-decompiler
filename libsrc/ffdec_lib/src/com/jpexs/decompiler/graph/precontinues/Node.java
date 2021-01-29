/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.graph.precontinues;

import com.jpexs.decompiler.graph.GraphPart;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class Node {
    public List<Node> next = new ArrayList<>();
    public List<Node> prev = new ArrayList<Node>();
    public GraphPart graphPart;
    private static int CURRENT_ID = 0;
    private int id;

    public Node parentNode;

    public Node() {
        this.id = ++CURRENT_ID;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "node" + id + ":" + (graphPart == null ? "null" : graphPart.toString());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Node other = (Node) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }


    public void replacePrevs(Node newNode) {
        for (Node p : this.prev) {
            for (int i = 0; i < p.next.size(); i++) {
                if (p.next.get(i) == this) {
                    p.next.set(i, newNode);
                }
            }
        }
    }

    public void replaceNexts(Node newNode) {
        for (Node n : this.next) {
            for (int i = 0; i < n.prev.size(); i++) {
                if (n.prev.get(i) == this) {
                    n.prev.set(i, newNode);
                }
            }
        }
    }

    public void removeFromGraph() {
        for (Node p : this.prev) {
            for (int i = p.next.size() - 1; i >= 0; i--) {
                if (p.next.get(i) == this) {
                    p.next.remove(i);
                }
            }
        }
        for (Node n : this.next) {
            for (int i = n.prev.size() - 1; i >= 0; i--) {
                if (n.prev.get(i) == this) {
                    n.prev.remove(i);
                }
            }
        }
        next.clear();
        prev.clear();
    }
}
