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
        name = "replacecharacterid",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        header = "Replace a character id with another character id from the same SWF",
        optionListHeading = "%n@|bold,underline Options|@:%n",           
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Examples|@:%n",
        footer = {
            "ffdec-cli replacecharacterid --old-id=12 --new-id=15 --old-id=56 --new-id=49  input.swf output.swf",
            "ffdec-cli replacecharacterid --old-id=7 --new-id=9 input.swf output.swf",
            "ffdec-cli replacecharacterid --pack input.swf output.swf",
            "ffdec-cli replacecharacterid --sort input.swf output.swf"
        },
        sortSynopsis = false
)
public class ReplaceCharacterId implements Runnable {      
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
    
    @ArgGroup(exclusive = true)
    PackOrSort packOrSort;
    

    static class PackOrSort {
        @Option(
                names = "--pack", 
                description = "Removes the spaces between the character ids (1,4,3 => 1,3,2)",                
                required = true
        )
        boolean pack;
        
        @Option(
                names = "--sort",
                description = "Assigns increasing IDs to the character tags + pack (1,4,3 => 1,2,3)",
                required = true
        )
        boolean sort;                
    }
    
    
    @ArgGroup(exclusive = false, multiplicity = "0..*")
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