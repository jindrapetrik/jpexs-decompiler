/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. */
package com.jpexs.decompiler.flash.ecma;

/**
 *
 * @author JPEXS
 */
public class EcmaScript {

    public static Double toNumber(Object o) {
        if (o == null) {
            return 0.0;
        }
        if (o instanceof Undefined) {
            return Double.NaN;
        }
        if (o instanceof Null) {
            return 0.0;
        }
        if (o instanceof Boolean) {
            return (Boolean) o ? 1.0 : 0.0;
        }
        if (o instanceof Double) {
            return (Double) o;
        }
        if (o instanceof Long) {
            return (double) (long) (Long) o;
        }
        if (o instanceof Integer) {
            return (double) (int) (Integer) o;
        }
        if (o instanceof String) {
            try {
                return Double.parseDouble((String) o);
            } catch (NumberFormatException nfe) {
                return Double.NaN;
            }
        }
        //TODO:ToPrimitive
        return 0.0;
    }

    public static EcmaType type(Object o) {
        if (o == null) {
            return EcmaType.NULL;
        }
        if (o.getClass() == String.class) {
            return EcmaType.STRING;
        }
        if (o.getClass() == Integer.class) {
            return EcmaType.NUMBER;
        }
        if (o.getClass() == Double.class) {
            return EcmaType.NUMBER;
        }
        if (o.getClass() == Long.class) {
            return EcmaType.NUMBER;
        }

        if (o.getClass() == Null.class) {
            return EcmaType.NULL;
        }
        if (o.getClass() == Undefined.class) {
            return EcmaType.UNDEFINED;
        }
        return EcmaType.OBJECT;
    }

    public static Object compare(Object x, Object y) {
        Object px = x;
        Object py = y;
        /*if (leftFirst) {
         px = x;  //toPrimitive
         py = y;  //toPrimitive
         } else {
         py = y;  //toPrimitive
         px = x;  //toPrimitive
         }*/
        if (type(px) != EcmaType.STRING || type(py) != EcmaType.STRING) {
            Double nx = toNumber(px);
            Double ny = toNumber(py);
            if (nx.isNaN()) {
                return new Undefined();
            }
            if (ny.isNaN()) {
                return new Undefined();
            }
            if ((nx).compareTo(ny) == 0) {
                return false;
            }
            if ((Double.compare(nx, -0.0) == 0) && (Double.compare(ny, 0.0) == 0)) {
                return false;
            }
            if ((Double.compare(nx, 0.0) == 0) && (Double.compare(ny, -0.0) == 0)) {
                return false;
            }
            if (nx.isInfinite() && nx > 0) {
                return false;
            }
            if (ny.isInfinite() && ny > 0) {
                return true;
            }
            if (nx.isInfinite() && nx < 0) {
                return false;
            }
            if (ny.isInfinite() && ny < 0) {
                return true;
            }
            if (nx.compareTo(ny) < 0) {
                return true;
            }
            return false;
        } else {//Both are STRING
            String sx = (String) px;
            String sy = (String) py;

            if (sx.startsWith(sy)) {
                return false;
            }
            if (sy.startsWith(sx)) {
                return true;
            }
            int len = sx.length() > sy.length() ? sx.length() : sy.length();
            for (int k = 0; k < len; k++) {
                int m = 0;
                int n = 0;
                if (sx.length() > k) {
                    m = sx.charAt(k);
                }
                if (sy.length() > k) {
                    n = sy.charAt(k);
                }
                if (m != n) {
                    if (m < n) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            return false;
        }
    }

    public static boolean equals(Object x, Object y) {
        EcmaType typeX = type(x);
        EcmaType typeY = type(y);
        if (typeX == typeY) {
            if (typeX == null) {
                return true;
            }
            if (typeX == EcmaType.NULL) {
                return true;
            }
            if (typeX == EcmaType.UNDEFINED) {
                return true;
            }
            if (typeX == EcmaType.NUMBER) {
                if (x instanceof Integer) {
                    x = Double.valueOf((Integer) x);
                }
                if (x instanceof Long) {
                    x = Double.valueOf((Long) x);
                }
                if (y instanceof Integer) {
                    y = Double.valueOf((Integer) y);
                }
                if (y instanceof Long) {
                    y = Double.valueOf((Long) y);
                }
                if (((Double) x).isNaN()) {
                    return false;
                }
                if (((Double) y).isNaN()) {
                    return false;
                }
                if (((Double) x).compareTo((Double) y) == 0) {
                    return true;
                }
                if ((Double.compare((Double) x, -0.0) == 0) && (Double.compare((Double) y, 0.0) == 0)) {
                    return true;
                }
                if ((Double.compare((Double) x, 0.0) == 0) && (Double.compare((Double) y, -0.0) == 0)) {
                    return true;
                }
                return false;
            }
            if (typeX == EcmaType.STRING) {
                return ((String) x).equals((String) y);
            }
            if (typeX == EcmaType.BOOLEAN) {
                return x == y;
            }
            return x == y;
        }
        if ((typeX == EcmaType.NULL) && (typeY == EcmaType.UNDEFINED)) {
            return true;
        }
        if ((typeX == EcmaType.UNDEFINED) && (typeY == EcmaType.NULL)) {
            return true;
        }

        if ((typeX == EcmaType.NUMBER) && (typeY == EcmaType.STRING)) {
            return equals(x, toNumber(y));
        }
        if ((typeX == EcmaType.STRING) && (typeY == EcmaType.NUMBER)) {
            return equals(toNumber(x), y);
        }
        if (typeX == EcmaType.BOOLEAN) {
            return equals(toNumber(x), y);
        }
        if (typeY == EcmaType.BOOLEAN) {
            return equals(x, toNumber(y));
        }
        if (typeX == EcmaType.STRING || typeX == EcmaType.NUMBER) {
            //y is object
            //return ecmaEquals(ecmaToPrimitive(x), y);
        }
        if (typeY == EcmaType.STRING || typeY == EcmaType.NUMBER) {
            //x is object
            //return ecmaEquals(x, ecmaToPrimitive(y));
        }
        return false;
    }

    public static boolean toBoolean(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Undefined) {
            return false;
        }
        if (o instanceof Null) {
            return false;
        }
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        if (o instanceof Long) {
            return ((Long) o) != 0;
        }
        if (o instanceof Integer) {
            return ((Integer) o) != 0;
        }
        if (o instanceof Double) {
            Double d = (Double) o;
            if (d.isNaN()) {
                return false;
            }
            if (Double.compare(d, 0) == 0) {
                return false;
            }
            return true;
        }
        if (o instanceof String) {
            String s = (String) o;
            return !s.isEmpty();
        }
        return true; //other Object
    }

    public static Long toUint32(Object o) {
        Double n = toNumber(o);
        if (n.isNaN()) {
            return 0L;
        }
        if (Double.compare(n, 0.0) == 0) {
            return 0L;
        }
        if (Double.compare(n, -0.0) == 0) {
            return 0L;
        }
        if (Double.isInfinite(n)) {
            return 0L;
        }
        Long posInt = (long) (double) (Math.signum(n) * Math.floor(Math.abs(n)));
        posInt %= (1 << 32);
        return posInt;
    }
}
