/*
 *  Copyright (C) 2024-2025 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.soleditor;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.io.File;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class Cookie implements TreeItem {

    private final SWF swf;
    private final File solFile;

    public Cookie(SWF swf, File solFile) {
        this.swf = swf;        
        this.solFile = solFile;
    }

    public File getSolFile() {
        return solFile;
    }        
    
    @Override
    public Openable getOpenable() {
        return swf;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.solFile);
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
        final Cookie other = (Cookie) obj;
        return Objects.equals(this.solFile, other.solFile);
    }        

    @Override
    public String toString() {
        return solFile.getName();
    }        
}
