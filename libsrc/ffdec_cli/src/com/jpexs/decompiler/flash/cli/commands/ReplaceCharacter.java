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
        name = "replacecharacter",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        header = "Replace a character tag with other character.",
        optionListHeading = "%n@|bold,underline Options|@:%n",           
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Examples|@:%n",
        footer = {
            "ffdec-cli replacecharacter --old-id=12 --new-id=15 --old-id=56 --new-id=49  input.swf output.swf",
            "ffdec-cli replacecharacter --old-id=7 --new-id=9 input.swf output.swf"
        },
        sortSynopsis = false
)
public class ReplaceCharacter implements Runnable {      
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
    
    @ArgGroup(exclusive = false, multiplicity = "1..*")
    List<Item> items;
    
    static class Item {      
        
       @Option(
                names = "--old-id",
                description = "Old character id",
                required = true,
                order = 0
        )
        int oldCharacterId; 
                
        @Option(
                names = {"--new-id"},
                description = "New character id",
                required = true,
                order = 1
        )
        int newCharacterId;              
    }        
    
    @Override
    public void run() {
        
    }
}
