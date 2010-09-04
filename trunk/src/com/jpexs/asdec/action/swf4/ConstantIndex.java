package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.helpers.Helper;

import java.util.ArrayList;
import java.util.List;

public class ConstantIndex {
    public int index;
    public List<String> constantPool;


    public ConstantIndex(int index) {
        this.index = index;
        this.constantPool = new ArrayList<String>();
    }

    public ConstantIndex(int index, List<String> constantPool) {
        this.index = index;
        this.constantPool = constantPool;
    }

    @Override
    public String toString() {
        if (constantPool != null) {
            if (index < constantPool.size()) {
                return "\"" + Helper.escapeString(constantPool.get(index)) + "\"";
            }
        }
        return "constant" + index;
    }
}
