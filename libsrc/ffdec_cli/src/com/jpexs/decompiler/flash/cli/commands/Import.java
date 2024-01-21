package com.jpexs.decompiler.flash.cli.commands;

import com.jpexs.decompiler.flash.cli.VersionProvider;
import com.jpexs.decompiler.flash.cli.commands.types.CompressionKind;
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
        name = "import",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        header = "Bulk import items to the SWF",
        optionListHeading = "%n@|bold,underline Options|@:%n",           
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Examples|@:%n",
        footer = {
            "ffdec-cli import sound input.swf output.swf c:/sounds/",
            "ffdec-cli import --update-bounds shape input.swf output.swf c:/shapes/",
            "ffdec-cli import symbolclass input.swf output.swf c:/data/sc.csv",
        },
        sortSynopsis = false
)
public class Import implements Runnable {      
    
    @Option(
            names = "--air",
            description = "Use AIR (airglobal.swc) for AS3 compilation instead of playerglobal.swc"
    )
    boolean air = false;
    
    @Parameters(
            index = "0",
            paramLabel = "<itemKind>",
            description = {
                "Item kind to import.",
                "@|bold Enum values|@: ${COMPLETION-CANDIDATES}"
            }
    )
    ImportObject itemKind;
    
    @Parameters(
            index = "1",
            paramLabel = "IN_FILE",
            description = "Input file"
    )
    String inFile;
    
    @Parameters(
            index = "2",
            paramLabel = "OUT_FILE",
            description = "Output file"
    )
    String outFile;
    
    @Parameters(
            index = "3",
            paramLabel = "DATA_DIR",
            description = "Data directory containing imported items. For symbolclass item kind, it is a file instead of directory."
    )
    String dataDirectory;
    
    @Option(
            names = "--update-bounds",
            description = "For shape import: Update shape bounds (no fill)",
            order = 1
    )
    boolean updateShapeBounds;        
    
    @Override
    public void run() {
        
    }
}
