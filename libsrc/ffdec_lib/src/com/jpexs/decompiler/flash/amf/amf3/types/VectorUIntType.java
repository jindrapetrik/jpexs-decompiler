package com.jpexs.decompiler.flash.amf.amf3.types;

import java.util.List;

public class VectorUIntType extends AbstractVectorType<Long> {

    public VectorUIntType(boolean fixed, List<Long> values) {
        super(fixed, values);
    }

    @Override
    public String getTypeName() {
        return "uint";
    }

}
