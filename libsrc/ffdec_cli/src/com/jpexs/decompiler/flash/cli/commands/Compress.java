package com.jpexs.decompiler.flash.cli.commands;

import com.jpexs.decompiler.flash.cli.VersionProvider;
import com.jpexs.decompiler.flash.cli.commands.types.CompressionKind;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

/**
 *
 * @author JPEXS
 */
@Command(
        name = "compress",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        header = "Compress SWF file.",
        optionListHeading = "%n@|bold,underline Options|@:%n",           
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Examples|@:%n",
        footer = {
            "ffdec-cli compress input.swf out_compressed.swf",
            "ffdec-cli compress --kind=lzma input.swf out_compressed.swf",
        }
)
public class Compress implements Runnable {
    @Option(
            names = "--kind",
            description = "Compression kind. @|bold Enum values|@: ${COMPLETION-CANDIDATES}   default: zlib"
    )
    CompressionKind kind = CompressionKind.zlib;
    
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

    @Override
    public void run() {
     
    }
}
