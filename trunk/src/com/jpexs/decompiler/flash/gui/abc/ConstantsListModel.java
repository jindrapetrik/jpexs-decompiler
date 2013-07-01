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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.helpers.Helper;
import java.util.ArrayList;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

public class ConstantsListModel implements ListModel {

    private ConstantPool constants;
    public static final int TYPE_UINT = 0;
    public static final int TYPE_INT = 1;
    public static final int TYPE_DOUBLE = 2;
    public static final int TYPE_DECIMAL = 3;
    public static final int TYPE_STRING = 4;
    public static final int TYPE_NAMESPACE = 5;
    public static final int TYPE_NAMESPACESET = 6;
    public static final int TYPE_MULTINAME = 7;
    private int type = TYPE_INT;

    public ConstantsListModel(ConstantPool constants, int type) {
        this.type = type;
        this.constants = constants;
    }

    private int makeUp(int i) {
        if (i < 0) {
            return 0;
        }
        return i;
    }

    @Override
    public int getSize() {
        switch (type) {
            case TYPE_UINT:
                return makeUp(constants.constant_uint.length - 1);
            case TYPE_INT:
                return makeUp(constants.constant_int.length - 1);
            case TYPE_DOUBLE:
                return makeUp(constants.constant_double.length - 1);
            case TYPE_DECIMAL:
                return makeUp(constants.constant_decimal.length - 1);
            case TYPE_STRING:
                return makeUp(constants.constant_string.length - 1);
            case TYPE_NAMESPACE:
                return makeUp(constants.constant_namespace.length - 1);
            case TYPE_NAMESPACESET:
                return makeUp(constants.constant_namespace_set.length - 1);
            case TYPE_MULTINAME:
                return makeUp(constants.constant_multiname.length - 1);
        }
        return 0;
    }

    @Override
    public Object getElementAt(int index) {
        switch (type) {
            case TYPE_UINT:
                return "" + (index + 1) + ":" + constants.constant_uint[index + 1];
            case TYPE_INT:
                return "" + (index + 1) + ":" + constants.constant_int[index + 1];
            case TYPE_DOUBLE:
                return "" + (index + 1) + ":" + constants.constant_double[index + 1];
            case TYPE_DECIMAL:
                return "" + (index + 1) + ":" + constants.constant_decimal[index + 1];
            case TYPE_STRING:
                return "" + (index + 1) + ":" + Helper.escapeString(constants.constant_string[index + 1]);
            case TYPE_NAMESPACE:
                return "" + (index + 1) + ":" + constants.constant_namespace[index + 1].getNameWithKind(constants);
            case TYPE_NAMESPACESET:
                return "" + (index + 1) + ":" + constants.constant_namespace_set[index + 1].toString(constants);
            case TYPE_MULTINAME:
                return "" + (index + 1) + ":" + constants.constant_multiname[index + 1].toString(constants, new ArrayList<String>());
        }
        return null;
    }

    @Override
    public void addListDataListener(ListDataListener l) {
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
    }
}
