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

import com.jpexs.decompiler.flash.configuration.Configuration;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import picocli.CommandLine;

/**
 *
 * @author JPEXS
 */
public class ConfigConverter implements CommandLine.ITypeConverter<Map<String,String>>{

    @Override
    public Map<String, String> convert(String value) throws Exception {
        String[] cfgs;
        if (value.contains(",")) {
            cfgs = value.split(",");
        } else {
            cfgs = new String[]{value};
        }

        Map<String, String> ret = new HashMap<>();
        
        for (String c : cfgs) {
            String[] cp = c.split("=");
            if (cp.length == 1) {
                cp = new String[]{cp[0], "1"};
            }
            ret.put(cp[0], cp[1]);
        }
        return ret;
    }    
}
