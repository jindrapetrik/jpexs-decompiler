package com.jpexs.decompiler.flash.cli.commands;

import com.jpexs.decompiler.flash.cli.VersionProvider;
import com.jpexs.decompiler.flash.cli.commands.types.CompressionKind;
import com.jpexs.decompiler.flash.cli.commands.types.ExeExportMode;
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
        name = "swf2exe",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        header = "Export SWF to executable file.",
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Example|@:%n",
        footer = {
            "ffdec-cli swf2exe --mode=wrapper input.swf output.exe",
        }
)
public class Swf2Exe implements Runnable {
    
    
    @Option(
            names = "--mode",
            description = "Export mode. @|bold Enum values|@: ${COMPLETION-CANDIDATES}",
            required = true
    )
    ExeExportMode mode;
    
    @Parameters(
            index = "0",
            paramLabel = "IN_FILE",
            description = "Input file"
    )
    String inFile;
    
    @Parameters(
            index = "1",
            paramLabel = "OUT_FILE",
            description = "Output file"
    )
    String outFile;

    @Override
    public void run() {
     
    }
}
