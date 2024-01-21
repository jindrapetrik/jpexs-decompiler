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
        name = "set",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        header = "Set instance metadata.",
        optionListHeading = "%n@|bold,underline Options|@:%n",           
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Examples|@:%n",
        footer = {
            "ffdec-cli instancemetadata set --instance=myitem --key=subkey1 --value=5 --out-file=output.swf input.swf",
            "ffdec-cli instancemetadata set --instance=other --data-file=input.bin --input-format=raw input.swf",
        },
        sortSynopsis = false
)
public class InstanceMetadataSet implements Runnable {      
    
    
    @Option(
            names = "--input-format",
            description = "Format of input data. @|bold Enum values|@: ${COMPLETION-CANDIDATES} Default is jslike."
    )
    InstanceMetadataFormat inputFormat = InstanceMetadataFormat.jslike;
    
    
    @Option(
            names = "--key",
            paramLabel = "<key>",
            description = {
                "Name of subkey to use. When present, the value is set as object property with the <key> name.",
                "Otherwise the value is set directly to the instance without any subkeys."
            }
    )
    String key;
    
    @ArgGroup(exclusive = true, multiplicity = "1")
    ValueOrDataFile valueOrDataFile;
    
    
    static class ValueOrDataFile {
        @Option(
            names = "--value",
            description = "Value to set."
        )
        String value = null;

        @Option(
                names = "--data-file",
                description = "Value to set from file."
        )
        String dataFile = null;
    
    }   
    
    @Option(
            names = "--instance",
            description = "Name of instance to replace metadata in.",
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
