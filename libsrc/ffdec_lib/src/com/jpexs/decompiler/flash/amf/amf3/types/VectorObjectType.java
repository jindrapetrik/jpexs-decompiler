package com.jpexs.decompiler.flash.amf.amf3.types;

import java.util.List;

public class VectorObjectType extends AbstractVectorType<Object> {

    private String typeName;

    public VectorObjectType(boolean fixed, String typeName, List<Object> values) {
        super(fixed, values);
        this.typeName = typeName;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

}
