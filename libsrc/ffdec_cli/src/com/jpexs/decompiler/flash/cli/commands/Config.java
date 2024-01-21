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
        name = "config",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        header = "List available configuration options",
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Example|@:%n",
        footer = {
            "ffdec-cli config",
        }
)
public class Config implements Runnable {        

    @Override
    public void run() {
     
    }
}
