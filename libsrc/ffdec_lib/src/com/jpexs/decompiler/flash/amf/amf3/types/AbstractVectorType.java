package com.jpexs.decompiler.flash.amf.amf3.types;

import com.jpexs.decompiler.flash.amf.amf3.Amf3Tools_;
import java.util.ArrayList;
import java.util.List;
import com.jpexs.decompiler.flash.amf.amf3.WithSubValues;

public abstract class AbstractVectorType<T> implements WithSubValues {

    private boolean fixed;
    private List<T> values;

    public boolean isFixed() {
        return fixed;
    }

    public AbstractVectorType(boolean fixed, List<T> values) {
        this.values = values;
        this.fixed = fixed;
    }

    public List<T> getValues() {
        return values;
    }

    @Override
    public List<Object> getSubValues() {
        List<Object> ret = new ArrayList<>();
        ret.addAll(values);
        return ret;
    }

    public abstract String getTypeName();

    @Override
    public String toString() {
        return Amf3Tools_.amfToString(this);
    }

}
