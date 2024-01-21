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
        name = "replacealpha",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        header = "Replace the alpha channel of the specified JPEG3/4 tag.",
        optionListHeading = "%n@|bold,underline Options|@:%n",           
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Examples|@:%n",
        footer = {
            "ffdec-cli replacealpha --character-id=13 --data-file=alpha13.bin --character-id=32 --data-file=alpha32.bin  input.swf output.swf",
            "ffdec-cli replacealpha --character-id=14 --data-file=data.bin input.swf output.swf",
        },
        sortSynopsis = false
)
public class ReplaceAlpha implements Runnable {      
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
                names = "--character-id",
                description = "Character id",
                required = true,
                order = 0
        )
        int characterId; 
                
        @Option(
                names = {"--data-file"},
                description = "Imported data file",
                required = true,
                order = 4
        )
        String dataFile;        
    }        
    
    @Override
    public void run() {
        
    }
}
