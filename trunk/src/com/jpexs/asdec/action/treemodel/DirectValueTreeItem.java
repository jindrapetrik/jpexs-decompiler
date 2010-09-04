package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.swf4.ConstantIndex;
import com.jpexs.asdec.helpers.Helper;

import java.util.List;

public class DirectValueTreeItem extends TreeItem {
    public Object value;
    public List<String> constants;

    public DirectValueTreeItem(Action instruction, Object value, ConstantPool constants) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.constants = constants.constants;
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        if (value instanceof Double) {
            if (Double.compare((double) (Double) value, 0) == 0) {
                return "0";
            }
        }
        if (value instanceof Float) {
            if (Float.compare((float) (Float) value, 0) == 0) {
                return "0";
            }
        }
        if (value instanceof String) {
            return "\"" + Helper.escapeString((String) value) + "\"";
        }
        if (value instanceof ConstantIndex) {
            return "\"" + Helper.escapeString(this.constants.get(((ConstantIndex) value).index)) + "\"";
        }
        return value.toString();
    }
}
