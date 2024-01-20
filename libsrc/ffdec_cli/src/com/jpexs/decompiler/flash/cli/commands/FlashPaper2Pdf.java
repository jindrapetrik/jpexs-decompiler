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
        name = "flashpaper2pdf",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        header = "Convert FlashPaper SWF file to PDF",
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Examples|@:%n",
        footer = {
            "ffdec-cli flashpaper2pdf input.swf output.pdf",
            "ffdec-cli flashpaper2pdf --zoom=3 input.swf output.pdf",
        }
)
public class FlashPaper2Pdf implements Runnable {
    @Option (
            names = "--zoom",
            paramLabel = "<N>",
            description = "Specify image quality"
    )
    Double zoom;

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
