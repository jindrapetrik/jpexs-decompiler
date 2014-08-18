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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.SWF;
import java.util.ArrayList;

/**
 *
 * @author JPEXS
 */
public class ActionList extends ArrayList<Action> {
    
    public void removeAction(int index) {
        ActionListReader.removeAction(this, index, SWF.DEFAULT_VERSION, true);
    }
    
    public Action getByAddress(long address) {
        for (Action action : this) {
            if (action.getAddress() == address) {
                return action;
            }
        }
        return null;
    }
}
