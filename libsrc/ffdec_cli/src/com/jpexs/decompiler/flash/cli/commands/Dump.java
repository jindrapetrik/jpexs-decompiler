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
package com.jpexs.decompiler.flash.cli.commands;

import com.jpexs.decompiler.flash.cli.VersionProvider;
import com.jpexs.decompiler.flash.cli.commands.types.DumpKind;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
/**
 *
 * @author JPEXS
 */
@Command(
        name="dump",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        header = "Dumps specific data of SWF file to console",
        optionListHeading = "%n@|bold,underline Options|@:%n",           
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Examples|@:%n",
        footer = {
            "ffdec-cli dump swf c:/files/input.swf",
            "ffdec-cli dump as3 c:/files/input.swf",
        }        
)
public class Dump implements Runnable {
    @Parameters(
            index = "0",
            description = "Kind of data to dump. @|bold Enum values|@: ${COMPLETION-CANDIDATES}"
            )
    DumpKind kind;
    
    @Parameters(
            index = "1",
            paramLabel = "FILE",
            description = "Input file"
            )
    String inputFile;

    @Override
    public void run() {
        
    }
}
