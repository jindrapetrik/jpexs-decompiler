package com.jpexs.decompiler.flash.cli.commands;

import com.jpexs.decompiler.flash.cli.VersionProvider;
import com.jpexs.decompiler.flash.cli.legacy.CommandLineArgumentParser;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;


/**
 *
 * @author JPEXS
 */
@CommandLine.Command(
        name = "old",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        header = "Old legacy commandline interface.",
        optionListHeading = "%n@|bold,underline Options|@:%n",           
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Examples|@:%n",
        footer = {
            "ffdec-cli old",
            "ffdec-cli old -export script -format script:pcode outdir/ file.swf"
        },
        sortSynopsis = false
)
public class Old implements Runnable { 

    @Parameters (
            index = "0", 
            arity = "0..*"
    )
    String args[] = new String[]{};
    
    @Override
    public void run() {        
        if (args.length == 0) {
            args = new String[] {"--help"};
        }
        try {
            CommandLineArgumentParser.parseArguments(args);
        } catch (IOException ex) {
            Logger.getLogger(Old.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
}
