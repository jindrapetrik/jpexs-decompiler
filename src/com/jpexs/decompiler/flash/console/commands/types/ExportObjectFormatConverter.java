/*
 * Copyright (C) 2024 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.console.commands.types;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import picocli.CommandLine;


/**
 *
 * @author JPEXS
 */
public class ExportObjectFormatConverter implements CommandLine.ITypeConverter<ExportObjectFormat>{

    @Override
    public ExportObjectFormat convert(String value) throws Exception {
        Pattern pat = Pattern.compile("^(?<type>[a-z0-9]+)(:(?<format>[a-z0-9]+))?$");
        Matcher mat = pat.matcher(value);
        if (!mat.matches()) {
            throw new CommandLine.TypeConversionException("Invalid value: must be 'type:format' or 'type' but was '" + value + "'");
        }
        String typeStr = mat.group("type");
        String formatStr = mat.group("format");
        ExportObject type = null;
        try {
            type = ExportObject.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException ex) {
            List<String> ts = new ArrayList<>();
            for (ExportObject t : ExportObject.values()) {
                ts.add("'" + ts.toString().toLowerCase() + "'");
            }
            throw new CommandLine.TypeConversionException("Invalid type: must be one of " + String.join(", ", ts) + " but was '" + typeStr+  "'");
        }
        List<String> allowedFormats = type.getAllowedFormatsAsStr();
        if (!allowedFormats.isEmpty() && formatStr != null) {
            if (!allowedFormats.contains(formatStr)) {                
                throw new CommandLine.TypeConversionException("Invalid format: for type '" + type + "' must format be one of '" + String.join("', '", allowedFormats) + "' but was " + formatStr);
            }
        }
        
        return new ExportObjectFormat(type, formatStr);
    }   
}
