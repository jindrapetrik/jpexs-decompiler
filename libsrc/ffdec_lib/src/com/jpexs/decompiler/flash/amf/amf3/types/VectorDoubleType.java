package com.jpexs.decompiler.flash.amf.amf3.types;

import java.util.List;

public class VectorDoubleType extends AbstractVectorType<Double> {

    public VectorDoubleType(boolean fixed, List<Double> values) {
        super(fixed, values);
    }

    @Override
    public String getTypeName() {
        return "Number";
    }

}
