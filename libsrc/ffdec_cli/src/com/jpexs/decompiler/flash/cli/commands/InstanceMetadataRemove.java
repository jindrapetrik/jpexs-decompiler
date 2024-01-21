package com.jpexs.decompiler.flash.cli.commands;

import com.jpexs.decompiler.flash.cli.VersionProvider;
import com.jpexs.decompiler.flash.cli.commands.types.CompressionKind;
import com.jpexs.decompiler.flash.cli.commands.types.DeobfuscateLevel;
import com.jpexs.decompiler.flash.cli.commands.types.ImportObject;
import com.jpexs.decompiler.flash.cli.commands.types.InstanceMetadataFormat;
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
        header = "Remove instance metadata",
        optionListHeading = "%n@|bold,underline Options|@:%n",           
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Examples|@:%n",
        footer = {
            "ffdec-cli instancemetadata remove --instance=myitem --key=subkey1 input.swf",
            "ffdec-cli instancemetadata remove --instance=other --out-file=output.swf input.swf",            
        },
        sortSynopsis = false
)
public class InstanceMetadataRemove implements Runnable {      
    
        
    @Option(
            names = "--key",
            paramLabel = "<key>",
            description = {
                "Name of subkey to remove. When present, only the value from subkey <key> of the AMF object is removed.",
                "Otherwise all metadata are removed from the instance."
            }
    )
    String key;
    
    @Option(
            names = "--instance",
            description = "Name of instance to remove data from.",
            required = true
    )
    String instance;
    
    @Option(
            names = "--out-file",
            description = "Where to save resulting file. If ommited, original SWF file is overwritten."
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
