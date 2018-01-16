/*
 *  Copyright (C) 2010-2018 JPEXS
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
package com.jpexs.decompiler.flash.gui.helpers;

import layout.TableLayout;

/**
 *
 * @author JPEXS
 */
public class TableLayoutHelper {

    public static void addTableSpaces(TableLayout tl, double size) {
        int cols = tl.getNumColumn();
        int rows = tl.getNumRow();
        for (int x = 0; x <= cols; x++) {
            tl.insertColumn(x * 2, size);
        }
        for (int y = 0; y <= rows; y++) {
            tl.insertRow(y * 2, size);
        }
    }
}
