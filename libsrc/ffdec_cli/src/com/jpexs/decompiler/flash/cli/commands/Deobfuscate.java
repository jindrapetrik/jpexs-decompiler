package com.jpexs.decompiler.flash.cli.commands;

import com.jpexs.decompiler.flash.cli.VersionProvider;
import com.jpexs.decompiler.flash.cli.commands.types.CompressionKind;
import com.jpexs.decompiler.flash.cli.commands.types.DeobfuscateLevel;
import com.jpexs.decompiler.flash.cli.commands.types.ImportObject;
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
        name = "deobfuscate",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        header = "Deobfuscate AS3 P-code.",
        optionListHeading = "%n@|bold,underline Options|@:%n",           
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Examples|@:%n",
        footer = {
            "ffdec-cli deobfuscate input.swf output.swf",
            "ffdec-cli deobfuscate --level=deadcode input.swf output.swf",            
        },
        sortSynopsis = false
)
public class Deobfuscate implements Runnable {      
    
        
    
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
            names = "--level",
            description = {
                "Deobfuscation level.",
                "@|bold Enum values|@: ${COMPLETION-CANDIDATES}. Default: traps"
            }
    )
    DeobfuscateLevel level = DeobfuscateLevel.traps;
    
    @Override
    public void run() {
        
    }
}
