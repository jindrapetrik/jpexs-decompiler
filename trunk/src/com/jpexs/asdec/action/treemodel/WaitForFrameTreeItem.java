/*
 *  Copyright (C) 2010-2011 JPEXS
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

package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class WaitForFrameTreeItem extends TreeItem {
    public int frame;
    public int skipCount;

    public WaitForFrameTreeItem(Action instruction, int frame, int skipCount) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.frame = frame;
        this.skipCount = skipCount;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "waitForFrame(" + frame + "," + skipCount + ");";
    }
}
