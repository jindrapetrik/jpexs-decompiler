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
package com.jpexs.decompiler.flash.console.commands;
import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.console.commands.types.ConfigConverter;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.ScopeType;

/**
 *
 * @author JPEXS
 */
@Command(name="<ffdec> --cli",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        subcommands = {
            HelpCommand.class,
            Export.class
        },
        descriptionHeading = "%n     <ffdec>      ffdec.sh on Linux/MacOs, ffdec.bat on Windows%n",
        customSynopsis = "<ffdec> [FILE...]%n    or <ffdec> [-hV]%n    or <ffdec> --cli [COMMAND]"
        )
public class Main implements Runnable {
    
    @Option(names = "--config",
            paramLabel = "<key>=<value>[,<key>=<value>...]",
            converter = ConfigConverter.class,
            description = {
                "Sets configuration values for this session.",
                "Use command 'config' to list available configuration settings. "
            },
            scope = ScopeType.INHERIT
            )
    private Map<String, String> configs = new HashMap<>();
    
    @Parameters(paramLabel = "FILE", description = "one or more files to open in GUI")
    private File[] files;
    
    @Option(names = "--cli", required = true, description = "Use new commandline mode")
    private boolean cli = false;        
    
    @Override
    public void run() {
        System.out.println("Main command");
    }
}

class VersionProvider implements IVersionProvider {
    @Override
    public String[] getVersion() throws Exception {
        return new String[]{ApplicationInfo.applicationVerName};
    }        
}
