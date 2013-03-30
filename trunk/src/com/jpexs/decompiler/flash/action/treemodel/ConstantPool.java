/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.decompiler.flash.action.treemodel;

import java.util.ArrayList;
import java.util.List;

public class ConstantPool {

    public List<List<String>> archive = new ArrayList<List<String>>();
    public List<Integer> archiveCounts = new ArrayList<Integer>();
    public List<String> constants = new ArrayList<String>();
    public int count;

    public ConstantPool() {
    }

    public ConstantPool(List<String> constants) {
        this.constants = constants;
    }

    public void setNew(List<String> constants) {
        archive.add(this.constants);
        this.constants = constants;
        archiveCounts.add(count);
        count = 0;
    }

    @Override
    public String toString() {
        return "" + count + "x " + constants.toString();
    }

    public boolean isEmpty() {
        return constants.isEmpty();
    }

    public void getLastUsed() {
        if (count > 0) {
            return;
        }
        for (int i = archive.size() - 1; i >= 0; i--) {
            if (archiveCounts.get(i) > 0) {
                count = archiveCounts.get(i);
                constants = archive.get(i);
                break;
            }
        }
    }
}
