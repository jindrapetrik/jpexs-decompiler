package com.jpexs.decompiler.flash.abc.avm2;

public class NumberContext {

    public static final int ROUND_CEILING = 0;
    public static final int ROUND_UP = 1;
    public static final int ROUND_HALF_UP = 2;
    public static final int ROUND_HALF_EVEN = 3;
    public static final int ROUND_HALF_DOWN = 4;
    public static final int ROUND_DOWN = 5;
    public static final int ROUND_FLOOR = 6;

    public static final int USE_NUMBER = 0;
    public static final int USE_DECIMAL = 1;
    public static final int USE_DOUBLE = 2;
    public static final int USE_INT = 3;
    public static final int USE_UINT = 4;

    private int usage = USE_NUMBER;
    private int precision = 34;
    private int rounding = ROUND_HALF_EVEN;

    public NumberContext(int usage, int precision, int rounding) {
        this.usage = usage;
        this.precision = precision;
        this.rounding = rounding;
    }

    public NumberContext(int param) {
        this.usage = param & 7;
        this.rounding = (param >> 3) & 7;
        this.precision = param >> 6;
    }

    public void setUsage(int usage) {
        if (usage > 6 || usage < 0) {
            throw new IllegalArgumentException("Invalid usage value :" + usage);
        }
        this.usage = usage;
    }

    public int getUsage() {
        return usage;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        if (precision > 34) {
            throw new IllegalArgumentException("Maximum value of precision is 34");
        }
        this.precision = precision;
    }

    public int toParam() {
        int ret = usage;
        if (usage == USE_NUMBER || usage == USE_DECIMAL) {
            ret |= (rounding << 3);
            if (precision < 34) {
                ret |= (precision << 6);
            }
        }
        return ret;
    }
}
