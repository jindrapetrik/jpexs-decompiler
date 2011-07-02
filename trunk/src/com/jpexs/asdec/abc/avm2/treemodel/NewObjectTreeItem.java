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

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;

import java.util.List;


public class NewObjectTreeItem extends TreeItem {
    public List<NameValuePair> pairs;

    public NewObjectTreeItem(AVM2Instruction instruction, List<NameValuePair> pairs) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.pairs = pairs;
    }

    @Override
    public String toString(ConstantPool constants) {
        String params = "";
        for (int n = 0; n < pairs.size(); n++) {
            if (n > 0) params += ",\r\n";
            params += pairs.get(n).toString(constants);
        }
        return hilight("{") + params + hilight("}");
    }


}
