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
public class IntegerPropertyField extends AbstractPropertyField<Integer> {            
    
    private PropertyValidationInterface<Integer> minValidation = null;
    private PropertyValidationInterface<Integer> maxValidation = null;
    
    public IntegerPropertyField(int value, int min, int max) {
        super("" + value);
        setMin(min);
        setMax(max);
        writeField.setColumns(("" + max).length());               
    }
    
    public IntegerPropertyField(int value) {
        super("" + value);        
    }
    
    public void setMax(int max) {
        if (maxValidation != null) {
            removeValidation(maxValidation);
        }
        maxValidation = new PropertyValidationInterface<Integer>() {
            @Override
            public boolean validate(Integer value) {
                return value <= max;
            }            
        };
        addValidation(maxValidation);
    }
    
    public void setMin(int min) {
        if (minValidation != null) {
            removeValidation(minValidation);
        }
        minValidation = new PropertyValidationInterface<Integer>() {
            @Override
            public boolean validate(Integer value) {
                return value >= min;
            }            
        };
        addValidation(minValidation);
    }

    @Override
    protected Integer textToValue(String text) {
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    @Override
    protected String valueToText(Integer value) {
        return "" + value;
    }
}
