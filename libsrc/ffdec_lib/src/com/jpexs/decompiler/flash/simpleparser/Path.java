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
package com.jpexs.decompiler.flash.simpleparser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class Path {
    private List<String> parts;
    
    private static final String PART_PARENTHESIS = "--FFDEC-()--";
    private static final String PART_BRACKETS = "--FFDEC-[]--";
    
    public static Path PATH_PARENTHESIS = new Path(PART_PARENTHESIS);
    public static Path PATH_BRACKETS = new Path(PART_BRACKETS);
    
    private Integer cachedHashCode = null;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                sb.append(".");
            }
            String part = parts.get(i);
            switch (part) {
                case PART_PARENTHESIS:
                    sb.append("()");
                    break;
                case PART_BRACKETS:
                    sb.append("[]");
                    break;
                default:
                    sb.append(part);
                    break;
            }
        }
        return sb.toString();
    }

    
    
    
    
    public Path(String ...name) {
        this.parts = new ArrayList<>();
        for (String n : name) {
            this.parts.add(n);
        }
    }
    
    public Path(List<String> parent, String name) {
        this(parent);
        this.parts.add(name);
    }
    
    public Path(List<String> parts) {
        this.parts = new ArrayList<>(parts);
    }
    
    public Path(Path path) {
        this.parts = new ArrayList<>(path.parts);
    }
    
    public Path add(Path path) {
        Path ret = new Path(this);
        ret.parts.addAll(path.parts);
        return ret;
    }
    
    public Path add(String ...name) {
        Path ret = new Path(this);
        for (String n : name) {
            ret.parts.add(n);
        }
        return ret;
    }
    
    public Path add(List<String> names) {
        Path ret = new Path(this);
        ret.parts.addAll(names);
        return ret;
    }
    
    public Path getLast() {
        if (parts.isEmpty()) {
            return null;
        }
        return new Path(parts.get(parts.size() - 1));
    }
    
    public Path getFirst() {
        if (parts.isEmpty()) {
            return null;
        }
        return new Path(parts.get(0));
    }
    
    public boolean isEmpty() {
        return parts.isEmpty();
    }
    
    public boolean hasParent() {
        return parts.size() >= 2;
    }
    
    public Path getParent() {        
        List<String> copy = new ArrayList<>(this.parts);
        if (!copy.isEmpty()) {
            copy.remove(copy.size() - 1);
        }
        return new Path(copy);
    }
    
    
    @Override
    public int hashCode() {
        if (cachedHashCode != null) {
            return cachedHashCode;
        }
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.parts);
        return cachedHashCode = hash;
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
        final Path other = (Path) obj;
        return Objects.equals(this.parts, other.parts);
    }        
    
    public List<String> getParts() {
        return new ArrayList<>(parts);
    }
    
    public int size() {
        return parts.size();
    }
    
    public Path get(int index) {
        return new Path(parts.get(index));
    }
}
