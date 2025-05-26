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
package com.jpexs.decompiler.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a path in a graph.
 *
 * @author JPEXS
 */
public class GraphPath implements Serializable {

    /**
     * List of IPs where the Path branched
     */
    private final List<Integer> branchIps = new ArrayList<>();

    /**
     * List of branch indices of which way the path went
     */
    private final List<Integer> branchIndices = new ArrayList<>();

    /**
     * Name of the root
     */
    public final String rootName;

    /**
     * Constructs a GraphPath with the given root name, branch IPs and branch
     * indices.
     *
     * @param rootName Root name
     * @param branchIps Branch IPs
     * @param branchIndices Branch indices
     */
    public GraphPath(String rootName, List<Integer> branchIps, List<Integer> branchIndices) {
        this.rootName = rootName;
        this.branchIps.addAll(branchIps);
        this.branchIndices.addAll(branchIndices);
    }

    /**
     * Constructs a GraphPath with the given branch IPs and branch indices.
     *
     * @param branchIps Branch IPs
     * @param branchIndices Branch indices
     */
    public GraphPath(List<Integer> branchIps, List<Integer> branchIndices) {
        rootName = "";
        this.branchIps.addAll(branchIps);
        this.branchIndices.addAll(branchIndices);
    }

    /**
     * Constructs a GraphPath
     */
    public GraphPath() {
        rootName = "";
    }

    /**
     * Checks whether the path starts with the given path.
     *
     * @param p Path
     * @return True if the path starts with the given path, false otherwise
     */
    public boolean startsWith(GraphPath p) {
        if (p.length() > length()) {
            return false;
        }

        List<Integer> otherKeys = new ArrayList<>(p.branchIps);
        List<Integer> otherVals = new ArrayList<>(p.branchIndices);

        for (int i = 0; i < p.length(); i++) {
            if (!Objects.equals(branchIps.get(i), otherKeys.get(i))) {
                return false;
            }
            if (!Objects.equals(branchIndices.get(i), otherVals.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a new parent GraphPath with the given length.
     *
     * @param len Length
     * @return New parent GraphPath
     */
    public GraphPath parent(int len) {
        GraphPath par = new GraphPath(rootName);
        for (int i = 0; i < len; i++) {
            par.branchIps.add(branchIps.get(i));
            par.branchIndices.add(branchIndices.get(i));
        }
        return par;
    }

    /**
     * Returns a new sub GraphPath with the given branch index and code
     * position.
     *
     * @param branchIndex Branch index
     * @param codePos Code position
     * @return New sub GraphPath
     */
    public GraphPath sub(int branchIndex, int codePos) {
        GraphPath next = new GraphPath(rootName, this.branchIps, this.branchIndices);
        next.branchIps.add(codePos);
        next.branchIndices.add(branchIndex);
        return next;
    }

    /**
     * Constructs a GraphPath with the given root name.
     *
     * @param rootName Root name
     */
    public GraphPath(String rootName) {
        this.rootName = rootName;
    }

    /**
     * Gets length of the path.
     *
     * @return Length
     */
    public int length() {
        return branchIndices.size();
    }

    /**
     * Gets the branch index at the given index.
     *
     * @param index Index
     * @return Branch index
     */
    public int get(int index) {
        return branchIndices.get(index);
    }

    /**
     * Gets the IP at the given index.
     *
     * @param index Index
     * @return IP
     */
    public int getKey(int index) {
        return branchIps.get(index);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + arrHashCode(branchIps);
        hash = 23 * hash + arrHashCode(branchIndices);
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

        if (!arrMatch(branchIps, other.branchIps)) {
            return false;
        }

        if (!arrMatch(branchIndices, other.branchIndices)) {
            return false;
        }

        return true;
    }

    /**
     * Hash code for a list of integers.
     *
     * @param arr List of integers
     * @return Hash code
     */
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

    /**
     * Checks whether two lists of integers match.
     *
     * @param arr List of integers
     * @param arr2 List of integers
     * @return True if the lists match, false otherwise
     */
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

    /**
     * Returns a string representation of the GraphPath.
     *
     * @return String representation
     */
    @Override
    public String toString() {
        String ret = rootName;
        for (int i = 0; i < branchIps.size(); i++) {
            ret += "/" + branchIps.get(i) + ":" + branchIndices.get(i);
        }
        return ret;
    }
}
