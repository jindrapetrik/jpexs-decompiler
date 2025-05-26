/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.xfl.shapefixer;

import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;

/**
 * Advanced style change record with double precision.
 *
 * @author JPEXS
 */
public class StyleChangeRecordAdvanced extends ShapeRecordAdvanced {

    public boolean stateNewStyles;

    public boolean stateLineStyle;

    public boolean stateFillStyle1;

    public boolean stateFillStyle0;

    public boolean stateMoveTo;

    public double moveDeltaX;

    public double moveDeltaY;

    public int fillStyle0;

    public int fillStyle1;

    public int lineStyle;

    public FILLSTYLEARRAY fillStyles;

    public LINESTYLEARRAY lineStyles;

    public StyleChangeRecordAdvanced() {

    }

    public StyleChangeRecordAdvanced(StyleChangeRecord scr) {
        this.stateNewStyles = scr.stateNewStyles;
        this.stateLineStyle = scr.stateLineStyle;
        this.stateFillStyle0 = scr.stateFillStyle0;
        this.stateFillStyle1 = scr.stateFillStyle1;
        this.stateMoveTo = scr.stateMoveTo;
        this.moveDeltaX = scr.moveDeltaX;
        this.moveDeltaY = scr.moveDeltaY;
        this.fillStyle0 = scr.fillStyle0;
        this.fillStyle1 = scr.fillStyle1;
        this.lineStyle = scr.lineStyle;
        this.fillStyles = scr.fillStyles;
        this.lineStyles = scr.lineStyles;
    }

    @Override
    public StyleChangeRecord toBasicRecord() {
        StyleChangeRecord ret = new StyleChangeRecord();
        ret.stateNewStyles = this.stateNewStyles;
        ret.stateLineStyle = this.stateLineStyle;
        ret.stateFillStyle0 = this.stateFillStyle0;
        ret.stateFillStyle1 = this.stateFillStyle1;
        ret.stateMoveTo = this.stateMoveTo;
        ret.moveDeltaX = (int) Math.round(this.moveDeltaX);
        ret.moveDeltaY = (int) Math.round(this.moveDeltaY);
        ret.fillStyle0 = this.fillStyle0;
        ret.fillStyle1 = this.fillStyle1;
        ret.lineStyle = this.lineStyle;
        ret.fillStyles = this.fillStyles;
        ret.lineStyles = this.lineStyles;
        return ret;
    }

    @Override
    public double changeX(double x) {
        if (stateMoveTo) {
            return moveDeltaX;
        }
        return x;
    }

    @Override
    public double changeY(double y) {
        if (stateMoveTo) {
            return moveDeltaY;
        }
        return y;
    }

    @Override
    public void round() {
        if (stateMoveTo) {
            moveDeltaX = Math.round(moveDeltaX);
            moveDeltaY = Math.round(moveDeltaY);
        }
    }

}
