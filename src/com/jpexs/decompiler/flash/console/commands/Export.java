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
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 *
 * @author JPEXS
 */
@Command(name = "export",
        header = "Export sources to directory"
        )
public class Export {
       
    @Parameters(index = "0", 
            split = ",", 
            paramLabel = "<type>",
            description = {"Types to export:",
            "script - Scripts (Default format: ActionScript source)",
        "image - Images (Default format: PNG/JPEG)",
        "shape - Shapes (Default format: SVG)",
        "morphshape - MorphShapes (Default format: SVG)",
        "movie - Movies (Default format: FLV without sound)",
        "font - Fonts (Default format: TTF)",
        "font4 - DefineFont4 (Default format: CFF)",
        "frame - Frames (Default format: PNG)",
        "sprite - Sprites (Default format: PNG)",
        "button - Buttons (Default format: PNG)",
        "sound - Sounds (Default format: MP3/WAV/FLV only sound)",
        "binaryData - Binary data (Default format:  Raw data)",
        "symbolClass - Symbol-Class mapping (Default format: CSV)",
        "text - Texts (Default format: Plain text)",
        "all - Every resource (but not FLA and XFL)",
        "fla - Everything to FLA compressed format",
        "xfl - Everything to uncompressed FLA format (XFL)"}
            )
    private String[] types;
    
    @Option(names = "--format", split = ",", paramLabel = "<type:format>", description = {
        "Sets output format for export:",
        "script:as - ActionScript source",
        "script:pcode - ActionScript P-code",
        "script:pcodehex - ActionScript P-code with hex",
        "script:hex - ActionScript Hex only",
        "shape:svg - SVG format for Shapes",
        "shape:png - PNG format for Shapes",
        "shape:canvas - HTML5 Canvas format for Shapes",
        "shape:bmp - BMP format for Shapes",
        "morphshape:svg - SVG format for MorphShapes",
        "morphshape:canvas - HTML5 Canvas  format for MorphShapes",
        "frame:png - PNG format for Frames",
        "frame:gif - GIF format for Frames",
        "frame:avi - AVI format for Frames",
        "frame:svg - SVG format for Frames",
        "frame:canvas - HTML5 Canvas format for Frames",
        "frame:pdf - PDF format for Frames",
        "frame:bmp - BMP format for Frames",
        "sprite:png - PNG format for Sprites",
        "sprite:gif - GIF format for Sprites",
        "sprite:avi - AVI format for Sprites",
        "sprite:svg - SVG format for Sprites",
        "sprite:canvas - HTML5 Canvas format for Sprites",
        "sprite:pdf - PDF format for Sprites",
        "sprite:bmp - BMP format for Sprites",
        "button:png - PNG format for Buttons",
        "button:svg - SVG format for Buttons",
        "button:bmp - BMP format for Buttons",
        "image:png_gif_jpeg - PNG/GIF/JPEG format for Images",
        "image:png - PNG format for Images",
        "image:jpeg - JPEG format for Images",
        "image:bmp - BMP format for Images",
        "image:png_gif_jpeg_alpha - PNG/GIF/JPEG+ALPHA format for Images",
        "text:plain - Plain text format for Texts",
        "text:formatted - Formatted text format for Texts",
        "text:svg - SVG format for Texts",
        "sound:mp3_wav_flv - MP3/WAV/FLV format for Sounds",
        "sound:mp3_wav - MP3/WAV format for Sounds",
        "sound:wav - WAV format for Sounds",
        "sound:flv - FLV format for Sounds",
        "font:ttf - TTF format for Fonts",
        "font:woff - WOFF format for Fonts",
        "font4:cff - CFF format for DefineFont4",
        "fla:<flaversion> or xfl:<flaversion> - Specify FLA format version",
        "   - values for <flaversion>: cs5,cs5.5,cs6,cc",
    })
    private String[] formats;
    
    @Parameters(index = "1", description = "Target directory")
    private String outDirectory;
    
    @Parameters(index = "2", description = "Input file or directory")
    private String inFileOrDirectory;    
}
