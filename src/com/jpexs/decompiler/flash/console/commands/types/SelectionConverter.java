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

import com.jpexs.decompiler.flash.console.CommandLineArgumentParser;
import static com.jpexs.decompiler.flash.console.CommandLineArgumentParser.badArguments;
import java.util.ArrayList;
import java.util.List;
import picocli.CommandLine;

/**
 *
 * @author JPEXS
 */
public class SelectionConverter implements CommandLine.ITypeConverter<Selection>{

    @Override
    public Selection convert(String value) throws Exception {
        List<Range> ret = new ArrayList<>();

        String[] ranges;
        if (value.contains(",")) {
            ranges = value.split(",");
        } else {
            ranges = new String[]{value};
        }
        for (String r : ranges) {
            Integer min = null;
            Integer max = null;
            if (r.contains("-")) {
                String[] ps = r.split("\\-");
                if (ps.length != 2) {
                    throw new CommandLine.TypeConversionException("Invalid range: " + r);
                }
                try {
                    if (!"".equals(ps[0])) {
                        min = Integer.parseInt(ps[0]);
                    }
                    if (!"".equals(ps[1])) {
                        max = Integer.parseInt(ps[1]);
                    }
                } catch (NumberFormatException nfe) {
                    throw new CommandLine.TypeConversionException("Invalid range: " + r);
                }
            } else {
                try {
                    min = Integer.parseInt(r);
                    max = min;
                } catch (NumberFormatException nfe) {
                    throw new CommandLine.TypeConversionException("Invalid range: " + r);
                }
            }
            ret.add(new Range(min, max));
        }
        return new Selection(ret);
    }    
}
