/*
 *  Copyright (C) 2010-2014 JPEXS
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
package com.jpexs.decompiler.flash.gui.generictageditors;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ReflectionTools {

    public static Object getValue(Object obj, Field field, int index) throws IllegalArgumentException, IllegalAccessException {
        Object value = field.get(obj);
        if (List.class.isAssignableFrom(field.getType())) {
            return ((List) value).get(index);
        }
        
        if (field.getType().isArray()) {
            return Array.get(value, index);
        }
        
        return value;
    }
    
    @SuppressWarnings("unchecked")
    public static void setValue(Object obj, Field field, int index, Object newValue) throws IllegalArgumentException, IllegalAccessException {
        Object value = field.get(obj);
        if (List.class.isAssignableFrom(field.getType())) {
            ((List) value).set(index, newValue);
        }
        else if (field.getType().isArray()) {
            Array.set(value, index, newValue);
        }
        else {
            field.set(obj, newValue);
        }
    }
}
