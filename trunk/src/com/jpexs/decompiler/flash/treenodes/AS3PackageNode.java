/*
 *  Copyright (C) 2013 JPEXS
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
package com.jpexs.decompiler.flash.treenodes;

import com.jpexs.decompiler.flash.AS3PackageNodeItem;
import com.jpexs.decompiler.flash.TreeElementItem;
import com.jpexs.decompiler.flash.gui.abc.TreeElement;

/**
 *
 * @author JPEXS
 */
public class AS3PackageNode extends TreeElement {
    
    public AS3PackageNode(String name, String path, AS3PackageNodeItem item, TreeElement parent) {
        super(name, path, item, parent);
    }

    @Override
    public AS3PackageNodeItem getItem() {
        return (AS3PackageNodeItem) item;
    }

}
