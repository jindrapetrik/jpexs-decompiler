package com.jpexs.decompiler.flash.abc.types;

public class Float4 {

    public float[] values = new float[4];

    public Float4(float value1, float value2, float value3, float value4) {
        this.values = new float[]{value1, value2, value3, value4};
    }

    public Float4(float[] values) {
        if (values == null || values.length < 4) {
            throw new IllegalArgumentException("Invalid values size");
        }
        this.values[0] = values[0];
        this.values[1] = values[1];
        this.values[2] = values[2];
        this.values[3] = values[3];
    }

}
