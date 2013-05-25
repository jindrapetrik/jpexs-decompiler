/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.types;

/**
 *
 * @author JPEXS
 */
public class MORPHLINESTYLEARRAY {

    public MORPHLINESTYLE lineStyles[];
    public MORPHLINESTYLE2 lineStyles2[];

    public LINESTYLEARRAY getStartLineStyles(int shapeNum) {
        LINESTYLEARRAY ret = new LINESTYLEARRAY();
        if (shapeNum == 1) {
            ret.lineStyles = new LINESTYLE[lineStyles.length];
            for (int m = 0; m < lineStyles.length; m++) {
                ret.lineStyles[m] = lineStyles[m].getStartLineStyle();
            }
        }
        if (shapeNum == 2) {
            ret.lineStyles2 = new LINESTYLE2[lineStyles2.length];
            for (int m = 0; m < lineStyles2.length; m++) {
                ret.lineStyles2[m] = lineStyles2[m].getStartLineStyle2();
            }
        }
        return ret;
    }

    public LINESTYLEARRAY getEndLineStyles(int shapeNum) {
        LINESTYLEARRAY ret = new LINESTYLEARRAY();
        if (shapeNum == 1) {
            ret.lineStyles = new LINESTYLE[lineStyles.length];
            for (int m = 0; m < lineStyles.length; m++) {
                ret.lineStyles[m] = lineStyles[m].getEndLineStyle();
            }
        }
        if (shapeNum == 2) {
            ret.lineStyles2 = new LINESTYLE2[lineStyles2.length];
            for (int m = 0; m < lineStyles2.length; m++) {
                ret.lineStyles2[m] = lineStyles2[m].getEndLineStyle2();
            }
        }
        return ret;
    }
}
