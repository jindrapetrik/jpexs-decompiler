package com.jpexs.decompiler.flash.cli.commands;

import com.jpexs.decompiler.flash.cli.VersionProvider;
import com.jpexs.decompiler.flash.cli.commands.types.CompressionKind;
import com.jpexs.decompiler.flash.cli.commands.types.DeobfuscateLevel;
import com.jpexs.decompiler.flash.cli.commands.types.DocFormat;
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
        name = "generatedoc",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        header = "Generate documentation",
        optionListHeading = "%n@|bold,underline Options|@:%n",           
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Examples|@:%n",
        footer = {
            "ffdec-cli generatedoc --type=as3.pcode.instructions --format=html --locale=en --out=docs.en.html",
            "ffdec-cli generatedoc --type=as3.pcode.instructions --format=html --locale=cs --out=docs.cs.html",
        },
        sortSynopsis = false
)
public class GenerateDoc implements Runnable {      
    
    @Option(
            names = "--type",
            description = "can be currently only: as3.pcode.instructions for list of ActionScript3 AVM2 instructions",
            required = true
    )
    String type;
    
    @Option(
            names = "--format",
            description = "Selects output format. Currently only html is supported."
    )
    DocFormat format = DocFormat.html;
    
    @Option(
            names = "--locale",
            description = "Override default locale. Sample value: en"
    )
    String locale = null;
    
    @Option(
            names = "--out",
            description = "File to write docs into. If ommited, it is written to stdout."
    )
    String outputFile;
    
    @Override
    public void run() {
        
    }
}
