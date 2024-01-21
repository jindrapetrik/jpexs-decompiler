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
        name = "get",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        header = "Get instance metadata",
        optionListHeading = "%n@|bold,underline Options|@:%n",           
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Examples|@:%n",
        footer = {
            "ffdec-cli instancemetadata get --instance=myitem --key=subkey1 input.swf",
            "ffdec-cli instancemetadata get --instance=other --data-file=output.json input.swf",
            "ffdec-cli instancemetadata get --instance=third --data-file=output.bin --output-format=raw input.swf",
        },
        sortSynopsis = false
)
public class InstanceMetadataGet implements Runnable {      
    
    
    @Option(
            names = "--output-format",
            description = "Format of output. @|bold Enum values|@: ${COMPLETION-CANDIDATES} Default is jslike."
    )
    InstanceMetadataFormat outputFormat = InstanceMetadataFormat.jslike;
    
    
    @Option(
            names = "--key",
            paramLabel = "<key>",
            description = "Name of subkey to display. When present, only value from subkey <key> is shown, whole object value otherwise."
    )
    String key;
    
    @Option(
            names = "--instance",
            description = "Name of instance to fetch metadata from.",
            required = true
    )
    String instance;
    
    @Option(
            names = "--data-file",
            description = "File to write the data to. If ommited, stdout is used."
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
