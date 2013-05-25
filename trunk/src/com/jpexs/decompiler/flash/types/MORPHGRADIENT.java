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
public class MORPHGRADIENT {

    public int numGradients;
    public MORPHGRADRECORD gradientRecords[];
    public int numGradientsExtra;

    public GRADIENT getStartGradient() {
        GRADIENT ret = new GRADIENT();
        ret.gradientRecords = new GRADRECORD[gradientRecords.length];
        for (int m = 0; m < gradientRecords.length; m++) {
            ret.gradientRecords[m] = gradientRecords[m].getStartRecord();
        }
        return ret;
    }

    public GRADIENT getEndGradient() {
        GRADIENT ret = new GRADIENT();
        ret.gradientRecords = new GRADRECORD[gradientRecords.length];
        for (int m = 0; m < gradientRecords.length; m++) {
            ret.gradientRecords[m] = gradientRecords[m].getEndRecord();
        }
        return ret;
    }
}
