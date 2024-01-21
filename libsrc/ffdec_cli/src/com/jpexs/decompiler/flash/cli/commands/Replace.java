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
        name = "replace",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        header = "Replace the data of the specified items",
        optionListHeading = "%n@|bold,underline Options|@:%n",           
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Examples|@:%n",
        footer = {
            "ffdec-cli replace --character-id=27 --update-bounds --data-file=char27.svg --character-id=43 --data-file=char43.jpg --format=lossless2 input.swf output.swf",
            "ffdec-cli replace --character-id=12 --data-file=data.bin input.swf output.swf",
        },
        sortSynopsis = false
)
public class Replace implements Runnable {     
    
    @Option(
            names = "--air",
            description = "Use AIR (airglobal.swc) for AS3 compilation instead of playerglobal.swc"
    )
    boolean air = false;
    
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
        
        @ArgGroup(exclusive = true, multiplicity = "1", order = 0)
        CharacterIdOrScriptName characterIdOrScriptName;
        
        @Option(
                names = "--update-bounds",
                description = "Update shape bounds (no fill)",
                order = 1
        )
        boolean updateShapeBounds;
        
        @Option(
                names = "--format",
                description = {
                    "Input format for images or shapes.",
                    "@|bold Enum values|@: ${COMPLETION-CANDIDATES}"
                },
                order = 2
        )
        ReplaceFormat format = null;
        
        @Option(
                names = "--body-id",
                description = "Method body index if the imported entity is an AS3 P-Code",
                order = 3
        )
        Integer bodyIndex = null;
        
        @Option(
                names = {"--data-file"},
                description = "Imported data file",
                required = true,
                order = 4
        )
        String dataFile;        
    }
    
    static class CharacterIdOrScriptName {
        @Option(
                names = "--character-id",
                description = "Character id. -1 for main timeline sound stream.",
                required = true
        )
        int characterId;
        
        @Option(
                names = "--script-name",
                description = "Name of the script",
                required = true
        )
        String scriptName;                 
    }
    
    @Override
    public void run() {
        
    }
}
