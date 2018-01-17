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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;

/**
 *
 * @author JPEXS
 */
public class RootABCContainerTag implements ABCContainerTag {

    @Override
    public ABC getABC() {
        return null;
    }

    @Override
    public SWF getSwf() {
        return null;
    }

    @Override
    public int compareTo(ABCContainerTag t) {
        if (t instanceof RootABCContainerTag) {
            return 0;
        }
        return -1;
    }

    @Override
    public String toString() {
        return " - all - ";
    }
}
