/*
 *  Copyright (C) 2010-2014 JPEXS
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
package com.jpexs.decompiler.flash.abc.avm2;

import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.List;

public class UnknownJumpException extends RuntimeException {

    public TranslateStack stack;
    public int ip;
    public List<GraphTargetItem> output;

    public UnknownJumpException(TranslateStack stack, int ip, List<GraphTargetItem> output) {
        this.stack = stack;
        this.ip = ip;
        this.output = output;
    }

    @Override
    public String toString() {
        return "Unknown jump to " + ip;
    }
}
