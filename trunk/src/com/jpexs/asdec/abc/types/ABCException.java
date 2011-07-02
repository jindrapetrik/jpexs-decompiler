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

package com.jpexs.asdec.abc.types;

import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.ConvertException;
import com.jpexs.asdec.helpers.Helper;


public class ABCException {
    public int start;
    public int end;
    public int target;
    public int type_index;
    public int name_index;

    @Override
    public String toString() {
        return "Exception: startServer=" + Helper.formatAddress(start) + " end=" + Helper.formatAddress(end) + " target=" + target + " type_index=" + type_index + " name_index=" + name_index;
    }

    public String toString(ConstantPool constants) {
        return "Exception: startServer=" + Helper.formatAddress(start) + " end=" + Helper.formatAddress(end) + " target=" + target + " type=\"" + getTypeName(constants) + "\" name=\"" + getVarName(constants) + "\"";
    }

    public String toString(ConstantPool constants, AVM2Code code) {
        try {
            return "Exception: startServer=" + code.adr2pos(start) + ":" + code.code.get(code.adr2pos(start)).toStringNoAddress(constants) + " end=" + code.adr2pos(end) + ":" + code.code.get(code.adr2pos(end)).toStringNoAddress(constants) + " target=" + code.adr2pos(target) + ":" + code.code.get(code.adr2pos(target)).toStringNoAddress(constants) + " type=\"" + getTypeName(constants) + "\" name=\"" + getVarName(constants) + "\"";
        } catch (ConvertException ex) {
            return "";
        }
    }

    public boolean isFinally() {
        return (name_index == 0) && (type_index == 0);
    }

    public String getVarName(ConstantPool constants) {
        if (name_index == 0) return "";
        return constants.constant_multiname[name_index].getName(constants);
    }

    public String getTypeName(ConstantPool constants) {
        if (type_index == 0) return "*";
        return constants.constant_multiname[type_index].getName(constants);
    }

}
