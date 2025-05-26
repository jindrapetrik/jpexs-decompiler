/*
 *  Copyright (C) 2024-2025 JPEXS
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
package com.jpexs.decompiler.flash.easygui.properties;

/**
 *
 * @author JPEXS
 */
public class FloatPropertyField extends AbstractPropertyField<Float> {

    private PropertyValidationInterface<Float> minValidation = null;
    private PropertyValidationInterface<Float> maxValidation = null;

    public FloatPropertyField(float value, float min, float max) {
        super("" + value);
        setMin(min);
        setMax(max);
    }

    public FloatPropertyField(float value) {
        super("" + value);
    }

    public void setMax(float max) {
        if (maxValidation != null) {
            removeValidation(maxValidation);
        }
        maxValidation = new PropertyValidationInterface<Float>() {
            @Override
            public boolean validate(Float value) {
                return value <= max;
            }
        };
        addValidation(maxValidation);
    }

    public void setMin(float min) {
        if (minValidation != null) {
            removeValidation(minValidation);
        }
        minValidation = new PropertyValidationInterface<Float>() {
            @Override
            public boolean validate(Float value) {
                return value >= min;
            }
        };
        addValidation(minValidation);
    }

    @Override
    protected Float textToValue(String text) {
        try {
            return Float.parseFloat(text);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    @Override
    protected String valueToText(Float value) {
        String ret = "" + value;
        if (ret.endsWith(".0")) {
            ret = ret.substring(0, ret.length() - 2);
        }
        return ret;
    }
}
