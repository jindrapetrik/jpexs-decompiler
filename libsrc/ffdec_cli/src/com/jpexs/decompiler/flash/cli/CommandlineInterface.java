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
package com.jpexs.decompiler.flash.cli;

import com.jpexs.decompiler.flash.cli.commands.Main;
import java.net.URLDecoder;
import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;

/**
 *
 * @author JPEXS
 */
public class CommandlineInterface {                
        
    /**
     * To bypass wrong encoded unicode characters coming from EXE, it Launch5j
     * encodes characters using URLEncoder.
     *
     */
    private static void decodeLaunch5jArgs(String[] args) {
        String encargs = System.getProperty("l5j.encargs");
        if ("true".equals(encargs) || "1".equals(encargs)) {
            for (int i = 0; i < args.length; ++i) {
                try {
                    args[i] = URLDecoder.decode(args[i], "UTF-8");
                } catch (Exception e) {
                    //ignored
                }
            }
        }
    }
    
    public static void main(String[] args) {
        decodeLaunch5jArgs(args);
        AnsiConsole.systemInstall();
        int exitCode = new CommandLine(new Main()).execute(args); 
        AnsiConsole.systemUninstall();
        System.exit(exitCode);
    }
}
