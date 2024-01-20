/*
 * Copyright (C) 2024 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.cli.commands;
import com.jpexs.decompiler.flash.cli.VersionProvider;
import com.jpexs.decompiler.flash.cli.commands.types.ConfigConverter;
import com.jpexs.decompiler.flash.cli.commands.types.ExportObject;
import com.jpexs.decompiler.flash.cli.commands.types.ExportObjectFormat;
import com.jpexs.decompiler.flash.cli.commands.types.ExportObjectFormatConverter;
import com.jpexs.decompiler.flash.cli.commands.types.OnErrorMode;
import com.jpexs.decompiler.flash.cli.commands.types.Selection;
import com.jpexs.decompiler.flash.cli.commands.types.SelectionConverter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

/**
 *
 * @author JPEXS
 */
@Command(name = "export",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        header = "Export sources to a directory",
        //descriptionHeading = "%n@|bold,underline Description|@:%n",        
        optionListHeading = "%n@|bold,underline Options|@:%n",           
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        synopsisHeading = "@|bold,underline Usage|@:",
        footerHeading = "%n@|bold,underline Examples|@:%n",
        footer = {
            "ffdec-cli export --character-id=51,43 shape:png,shape:bmp c:/out/ c:/files/input.swf",
            "ffdec-cli export --frame=2-10 frame:svg c:/out/ c:/files/input.swf",
            "ffdec-cli export --embed --class=mypkg.Main,other.+ script c:/out/ c:/files/input.swf",
            "ffdec-cli export all c:/out/ c:/files/input.swf",
        }
        )
public class Export implements Runnable {
       
    @ParentCommand
    private Main parent;
            
    @Option(names = "--frame", 
            paramLabel = "<frameRanges>",
            converter = SelectionConverter.class,            
            description = {
                "Selected frame(s) to export.",
                "@|bold Sample values|@: 1-5 or 2,4 or 2-5,7,9-"
            }
            )
    private Selection frames = new Selection();
    
    @Option(names = "--character-id",
            paramLabel = "<characterIdRanges>",
            converter = SelectionConverter.class,
            description = {
                "Selected character id(s) to export.",
                "@|bold Sample values|@: 27 or 2-5 or 12,24 or 2-5,10,9-"
            }
            )
    private Selection characterIds = new Selection();
    
    @Option(names = "--class", 
            paramLabel = "<class>",
            split = ",",
            description = {
                "Selected scripts to export by classname (ActionScript 3 ONLY).",
                "@|bold Sample values|@:",
                "com.example.MyClass",
                "com.example.+  (all classes in package \"com.example\")",
                "com.++,net.company.MyClass  (all classes in package \"com\" and all subpackages, class net.company.MyClass)",
            })
    private List<String> classes = new ArrayList<>();
    
    @Option(names = "--embed",
            description = {
                "Enables exporting embedded assets via [Embed tag].",
                "For script:as exports."
            }
            )
    private boolean useEmbed = false;
    
    @Option(
            names = "--on-error",
            description = {
                "Error handling mode.",
                "@|bold Possible values|@:",
                " @|bold abort|@ - stops exporting",
                " @|bold retry|@ - retries exporting (see --num-retries option)",
                " @|bold ignore|@ - ignores current file"
            }
    )
    private OnErrorMode onError = OnErrorMode.abort;
    
    @Option(
            names = "--num-retries",
            description = "Number of retries for option --on-error=retry. Default is 3."
    )
    private int numRetries = 3;
    
    @Option(
            names = "--timeout-method",
            description = "Decompilation timeout for a single method in AS3 or single action in AS1/2 in seconds"
    )
    Integer methodTimeout = null;
    
    @Option(
            names = "--timeout-total",
            description = "Total export timeout in seconds"
    )
    Integer totalTimeout = null;
    
    @Option(
            names = "--timeout-file",
            description = "Export timeout for a single AS3 class in seconds"
    )
    Integer fileTimeout = null;        
    
    @Parameters(index = "0", 
            split = ",", 
            arity = "1",
            converter = ExportObjectFormatConverter.class,
            paramLabel = "<type[:format]>",
            description = {"What objects to export.",
        "@|bold Available types and formats|@:",   
        "script:as|pcode|pcodehex|hex",
        "shape:svg|png|canvas|bmp|svg",
        "morphshape:svg|canvas",
        "frame:png|gif|avi|svg|canvas|pdf|bmp",
        "sprite:png|gif|avi|svg|canvas|pdf|bmp",
        "button:png|svg|bmp",
        "image:png_gif_jpeg|png|jpeg|bmp|png_gif_jpeg_alpha",
        "text:plain|formatted|svg",
        "sound:mp3_wav_flv|mp3_wav|wav|flv",
        "font:ttf|woff",
        "font4:cff",
        "fla:cs5|cs5.5|cs6|cc",
        "xfl:cs5|cs5.5|cs6|cc",
        "all (=everything except fla and xfl)"
            })
    private List<ExportObjectFormat> objects;        
    
    @Parameters(index = "1", description = "Target directory", paramLabel = "OUT_DIR")
    private String outDirectory;
    
    @Parameters(index = "2", description = "Input file or directory", paramLabel = "IN_DIR_OR_FILE")
    private String inFileOrDirectory;    

    @Override
    public void run() {
        System.out.println("exporting...ok");
        for(String c : classes) {
            System.out.println("class " + c);
        }
    }
}
