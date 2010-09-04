/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.gui;


public class TreePart implements Comparable {

    public String path;
    public String name;
    public int classIndex;
    public boolean hasSubParts = false;

    public TreePart(String path, String name, int classIndex) {
        this.path = path;
        this.name = name;
        this.classIndex = classIndex;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TreePart other = (TreePart) obj;
        if ((this.path == null) ? (other.path != null) : !this.path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.path != null ? this.path.hashCode() : 0);
        return hash;
    }

    public int compareTo(Object o) {
        if (o instanceof TreePart) {
            if (((TreePart) o).hasSubParts && (!hasSubParts)) return 1;
            if ((!((TreePart) o).hasSubParts) && (hasSubParts)) return -1;
            return (path + "." + name).compareTo(((TreePart) o).path + "." + ((TreePart) o).name);
        }
        return -1;
    }
}
