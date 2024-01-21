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
        name = "enabledebugging",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        header = "Enable debugging for SWF file",
        optionListHeading = "%n@|bold,underline Options|@:%n",           
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Examples|@:%n",
        footer = {
            "ffdec-cli enabledebugging input.swf output.swf",
            "ffdec-cli enabledebugging --inject-as3 input.swf output.swf",
            "ffdec-cli enabledebugging --inject-as3 --pcode input.swf output.swf",            
            "ffdec-cli enabledebugging --generate-swd input.swf output.swf",
        },
        sortSynopsis = false
)
public class EnableDebugging implements Runnable {      
    
        
    
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
    InjectAs3OrGenerateSwd injectAs3OrGenerateSwd;
    
    @Option(names = "--pcode")
    boolean pcode = false;
    
    static class InjectAs3OrGenerateSwd {
        @Option(
                names = "--inject-as3",
                description = "Inject debugfile and debugline instructions into the code to match decompiled/pcode source.",
                required = true
        )
        boolean injectAs3;
        
        @Option(
                names = "--generate-swd",
                description = "Create SWD file needed for AS1/2 debugging. for <outfile.swf>, <outfile.swd> is generated",
                required = true
        )
        boolean generateSwd;
    }
    
    @Override
    public void run() {
        
    }
}
