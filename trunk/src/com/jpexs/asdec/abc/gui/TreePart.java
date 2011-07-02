/*
 *  Copyright (C) 2010-2011 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
