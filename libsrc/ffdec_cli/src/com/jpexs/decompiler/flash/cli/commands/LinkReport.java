package com.jpexs.decompiler.flash.cli.commands;

import com.jpexs.decompiler.flash.cli.VersionProvider;
import com.jpexs.decompiler.flash.cli.commands.types.CompressionKind;
import com.jpexs.decompiler.flash.cli.commands.types.ReplaceFormat;
import java.util.List;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.ArgGroup;

/**
 *
 * @author JPEXS
 */
@Command(
        name = "linkreport",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        header = "Generate linker report for the swffile",
        optionListHeading = "%n@|bold,underline Options|@:%n",           
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Examples|@:%n",
        footer = {
            "ffdec-cli linkreport input.swf",
            "ffdec-cli linkreport --out-file=out.xml input.swf"
        },
        sortSynopsis = false
)
public class LinkReport implements Runnable {      
    
    @Option(
            names = "--out-file",
            paramLabel = "<outFile>",
            description = "Saves XML report to <outFile>. When ommited, the report is printed to stdout."
    )
    String outFile = null;
    
    @Parameters(
            index = "0",
            paramLabel = "IN_FILE",
            description = "Input file"
    )
    String inFile;
    
    @Override
    public void run() {
        
    }
}
