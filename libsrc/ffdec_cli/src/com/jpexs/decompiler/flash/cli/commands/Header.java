package com.jpexs.decompiler.flash.cli.commands;

import com.jpexs.decompiler.flash.cli.VersionProvider;
import com.jpexs.decompiler.flash.cli.commands.types.CompressionKind;
import java.awt.Point;
import java.util.List;
import java.util.Map;
import java.util.Stack;
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
        name = "header",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        header = "Display and manipulate SWF header values",
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Example|@:%n",
        footer = {
            "ffdec-cli header input.swf",
            "ffdec-cli header --set gfx=true --set width=1200 --out-file=output.swf input.swf",
            "ffdec-cli header --set displayrect=[0,0,800px,600px] input.swf",
        }
)
public class Header implements Runnable {
    
    @Option(
            names = "--set",
            paramLabel = "<key>=<value>",
            description = {"Set values of the SWF header.",
            "@|bold Available keys|@: version\n" +
            "                gfx (true/false)\n" +
            "                displayrect ([x1,y1,x2,y2])\n" +
            "                width\n" +
            "                height\n" +
            "                framecount\n" +
            "                framerate"},
            arity = "0..*"
    )
    Map<String, String> setValues;  
    
    @Option(
            names = "--out-file",
            description = "Output file to write modifications. If ommited, file is modified inplace."
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
