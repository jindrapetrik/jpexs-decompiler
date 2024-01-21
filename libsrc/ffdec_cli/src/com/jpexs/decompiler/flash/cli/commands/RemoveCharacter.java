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
        name = "removecharacter",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        header = "Remove a character tag from the SWF.",
        optionListHeading = "%n@|bold,underline Options|@:%n",           
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Examples|@:%n",
        footer = {
            "ffdec-cli removecharacter --character-id=12 --character-id=29 input.swf output.swf",
            "ffdec-cli removecharacter --character-id=13,57 input.swf output.swf",
            "ffdec-cli removecharacter --with-dependencies --character-id=130 input.swf output.swf",
            
        },
        sortSynopsis = false
)
public class RemoveCharacter implements Runnable {      
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
            names = "--with-dependencies",
            description = "Remove with dependencies"
    )
    boolean withDependencies;
    
    @Option(
            names = "--character-id",
            split = ",",
            description = "Character id",
            paramLabel = "<characterId>",
            required = true,
            arity = "1..*"
    )
    List<Integer> characterIds;
    
    @Override
    public void run() {
        
    }
}
