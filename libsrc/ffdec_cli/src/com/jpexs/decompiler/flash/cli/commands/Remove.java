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
        name = "remove",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        header = "Remove a tag from the SWF.",
        optionListHeading = "%n@|bold,underline Options|@:%n",           
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Examples|@:%n",
        footer = {
            "ffdec-cli remove --tag-index=12 --tag-index=13 input.swf output.swf",
            "ffdec-cli remove --tag-index=5 input.swf output.swf"
        },
        sortSynopsis = false
)
public class Remove implements Runnable {      
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
    
    @Option(
            names = "--tag-index",
            description = "Index of tag on SWF timeline",
            paramLabel = "<tagIndex>",
            required = true,
            arity = "1..*"
    )
    List<Integer> tagIndices;
    
    @Override
    public void run() {
        
    }
}
