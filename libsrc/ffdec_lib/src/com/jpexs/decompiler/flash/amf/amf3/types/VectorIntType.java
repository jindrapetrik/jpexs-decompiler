package com.jpexs.decompiler.flash.amf.amf3.types;

import java.util.List;

public class VectorIntType extends AbstractVectorType<Long> {

    public VectorIntType(boolean fixed, List<Long> values) {
        super(fixed, values);
    }

    @Override
    public String getTypeName() {
        return "int";
    }

}
