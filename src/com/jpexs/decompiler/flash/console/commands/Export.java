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
package com.jpexs.decompiler.flash.console.commands;
import com.jpexs.decompiler.flash.console.commands.types.ConfigConverter;
import com.jpexs.decompiler.flash.console.commands.types.ExportObject;
import com.jpexs.decompiler.flash.console.commands.types.ExportObjectFormat;
import com.jpexs.decompiler.flash.console.commands.types.ExportObjectFormatConverter;
import com.jpexs.decompiler.flash.console.commands.types.Selection;
import com.jpexs.decompiler.flash.console.commands.types.SelectionConverter;
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
        header = "Export sources to directory"
        )
public class Export implements Runnable {
       
    @ParentCommand
    private Main parent;
    
    @Option(names = "--frame", 
            paramLabel = "<frameRanges>",
            converter = SelectionConverter.class,            
            description = {
                "Selected frame(s) to export.",
                "Sample values: 1-5 or 2,4 or 2-5,7,9-"
            }
            )
    private Selection frames = new Selection();
    
    @Option(names = "--character-id",
            paramLabel = "<characterIdRanges>",
            converter = SelectionConverter.class,
            description = {
                "Selected character id(s) to export.",
                "Sample values: 27 or 2-5 or 12,24 or 2-5,10,9-"
            }
            )
    private Selection characterIds = new Selection();
    
    @Option(names = "--class", 
            paramLabel = "<class>",
            split = ",",
            description = {
                "Selected scripts to export by classname (ActionScript 3 ONLY).",
                "Sample values:",
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
    
    
    
    @Parameters(index = "0", 
            split = ",", 
            converter = ExportObjectFormatConverter.class,
            paramLabel = "<type[:format]>",
            description = {"What objects to export. Available formats:",   
        "script:as (default)",
        "script:pcode",
        "script:pcodehex",
        "script:hex",
        "shape:svg (default)",
        "shape:png",
        "shape:canvas",
        "shape:bmp",
        "morphshape:svg (default)",
        "morphshape:canvas",
        "frame:png (default)",
        "frame:gif",
        "frame:avi",
        "frame:svg",
        "frame:canvas",
        "frame:pdf",
        "frame:bmp",
        "sprite:png (default)",
        "sprite:gif",
        "sprite:avi",
        "sprite:svg",
        "sprite:canvas",
        "sprite:pdf",
        "sprite:bmp",
        "button:png (default)",
        "button:svg",
        "button:bmp",
        "image:png_gif_jpeg (default)",
        "image:png",
        "image:jpeg",
        "image:bmp",
        "image:png_gif_jpeg_alpha",
        "text:plain (default)",
        "text:formatted",
        "text:svg",
        "sound:mp3_wav_flv (default)",
        "sound:mp3_wav",
        "sound:wav",
        "sound:flv",
        "font:ttf (default)",
        "font:woff",
        "font4:cff (default)",
        "fla:cs5",
        "fla:cs5.5",
        "fla:cs6",
        "fla:cc",
        "xfl:cs5",
        "xfl:cs5.5",
        "xfl:cs6",
        "xfl:cc",                          
            })
    private List<ExportObjectFormat> objects;        
    
    @Parameters(index = "1", description = "Target directory")
    private String outDirectory;
    
    @Parameters(index = "2", description = "Input file or directory")
    private String inFileOrDirectory;    

    @Override
    public void run() {
        System.out.println("exporting...ok");
    }
}
