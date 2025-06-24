# JPEXS Free Flash Decompiler Library

Open Source Flash SWF decompiler and editor library.

## Main application
For information about main application, see [Main project](https://github.com/jindrapetrik/jpexs-decompiler)

## Library JAR file
Main FFDec library JAR file is named `ffdec_lib.jar`.

## Dependencies
Some of the library features require additional library files.

These include:

* CMYKJPEG - `cmykjpeg.jar` - CMYK JPEG image support
* sfntly - `sfntly.jar` - WOFF font export
* JLayer - `jlayer-1.0.2.jar` - Decoding MP3
* UAB "DKD" NellyMoser ASAO codec - `nellymoser.jar` - Decoding Nelly Moser sound format
* Animated GIF Writer/Encoder -  `gif.jar`
* LZMA SDK - `LZMA.jar` - Reading SWF compressed with LZMA
* Monte Media Library - `avi.jar` - Frames to AVI export
* Fontastic, DoubleType - `ttf.jar` - Font TTF export
* jPacker - `jpacker.jar` - HTML Canvas export scripts compression
* gnujpdf - `gnujpdf.jar` - PDF export
* vlcj - `vlcj-4.7.3.jar`, `vlcj-natives-4.7.0.jar` - Display/Export of video tags
* Java Native Access - `jna-3.5.1.jar`, `jna-platform-3.5.1.jar` - Display/Export of video tags
* DDSReader - `ddsreader.jar` - DDS images reading (GFX files)
* Reality Interactive ImageIO TGA library - `tga.jar` - TGA images reading (GFX files)
* Flashdebugger library - `flashdebugger.jar` - Flash debugging
* Open Imaging GIF Decoder - `gifreader.jar` - Importing GIFs
* Miter clip - modified openjdk8 Stroker - `miterclip.jar` - Support for miter clip join style in shapes
* FlexSDK Decimal128 class - `decimal.jar` - Working with decimal type in AS3
* FLA Compound Document Tools - `flacomdoc.jar` - Exporting FLA to CS4 or lower
* TomlJ - `tomlj-1.1.1.jar` - Storing configuration
* ANTLR - `antlr-runtime-4.11.1.jar` - Storing configuration

## Basic library usage
```java
package com.jpexs.decompiler.flash.test;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SwfOpenException;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import java.io.FileInputStream;
import java.io.IOException;

public class Test {
    public static void main(String[] args) {
       try ( FileInputStream fis = new FileInputStream("data/as3.swf")) { //open up a file

            //Pass the InputStream to SWF constructor.
            //Note: There are many variants of the constructor - Do not use single parameter version - it does not process whole SWF.
            SWF swf = new SWF(fis, true); 

            //Get some SWF parameters
            System.out.println("SWF version = " + swf.version);
            System.out.println("FrameCount = " + swf.frameCount);

            //Process all tags
            for (Tag t : swf.getTags()) {                
                if (t instanceof CharacterIdTag) {  //Print character id with the tag if it has any
                    System.out.println("Tag " + t.getTagName() + " (" + ((CharacterIdTag) t).getCharacterId() + ")");
                } else {
                    System.out.println("Tag " + t.getTagName());
                }
            }

            System.out.println("OK");
        } catch (SwfOpenException ex) {
            System.out.println("ERROR: Invalid SWF file");
        } catch (IOException ex) {
            System.out.println("ERROR: Error during SWF opening");
        } catch (InterruptedException ex) {
            System.out.println("ERROR: Parsing interrupted");
        }
    }
}
```

## SWF modification
```java
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.types.MATRIX;
  ...
  SWF swf = new SWF(fis, true); 
  for (Tag t : swf.getTags()) {                
      if (t instanceof PlaceObjectTypeTag) { //Find all PlaceObject(1,2,3,4) tags
          PlaceObjectTypeTag po = (PlaceObjectTypeTag)t;
          MATRIX mat = po.getMatrix();
          if (mat != null) {
              mat.translateX += 2000; //move 100 pixels to right
          }
          po.setModified(true); //CRUCIAL to call on every modified tag, otherwise it won't save
      }
  }
  
  OutputStream os = new FileOutputStream("data/file_modified.swf");
  try {
      swf.saveTo(os);
  } catch (IOException e) {
      System.out.println("ERROR: Error during SWF saving");
  }
...
```

## HTML documentation
You can download HTML documentation for Java classes generated from Javadoc
as separate download from main project website.

## Authors and contact
The decompiler was originally written by **Jindra Petřík** also known as **JPEXS**.
The application was made in Czech Republic.
See more info on [main project website](https://github.com/jindrapetrik/jpexs-decompiler/)


## Licenses + Acknowledgments
FFDec Library is licensed under GNU LGPL v3 (LGPL-3.0-or-later), see [license.txt](license.txt) for details.
It uses modified code of these libraries:

* [sfntly] (WOFF font export) - Apache License 2.0
* [JLayer] (Decoding MP3) - LGPL
* UAB "DKD" NellyMoser ASAO codec (Decoding Nelly Moser sound format) - LGPL
* [Animated GIF Writer] (Frames to GIF export) - Creative Commons Attribution 3.0 Unported
* [Animated GIF Encoder] (Frames to GIF export)
* [gnujpdf] (PDF export) - LGPL License
* [openjdk8 Stroker] (Shapes - Miter clip drawing) - GPL License
* [Apache Flex SDK] (Decimal numbers support - Decimal128 class) - Apache Licence 2.0

And also links to these libraries:

* [LZMA SDK] (SWF de/compress) - public domain
* [Monte Media Library] (Frames to AVI export) - LGPL
* [Fontastic] (Font TTF export) - LGPL
* [DoubleType] (Font TTF export) - GPLv2
* [jPacker] (Canvas scripts compression) - MIT License
* [DDSReader] (DDS reading) - MIT License
* [vlcj] (DefineVideoStream playback) - GPLv3
* [Reality Interactive ImageIO TGA library] (TGA file display in GFX files) - LGPL v2.1
* [flashdebugger library] (Debugging ActionScript) - LGPLv3
* [Java Native Access - JNA] (Registry association, Process memory reading) - LGPL
* [Open Imaging GIF Decoder] (GIF file importing) - Apache License 2.0
* [FLA Compound Document Tools] (Exporting to FLA CS4 and below) - LGPLv2.1
* [TomlJ] (Storing configuration) - Apache License 2.0
* [ANTLR] (Storing configuration) - BSD 3-Clause

[sfntly]: https://code.google.com/p/sfntly/
[JLayer]: http://www.javazoom.net/javalayer/javalayer.html
[Animated GIF Writer]: http://elliot.kroo.net/software/java/GifSequenceWriter/
[Animated GIF Encoder]: http://www.fmsware.com/stuff/gif.html
[LZMA SDK]: http://www.7-zip.org/sdk.html
[Monte Media Library]: http://www.randelshofer.ch/monte/
[Fontastic]: http://code.andreaskoller.com/libraries/fontastic/
[DoubleType]: http://sourceforge.net/projects/doubletype/
[jPacker]: https://code.google.com/p/jpacker/
[gnujpdf]: http://gnujpdf.sourceforge.net/
[DDSReader]: https://github.com/npedotnet/DDSReader/
[vlcj]: https://github.com/caprica/vlcj
[Reality Interactive ImageIO TGA library]: https://github.com/tmyroadctfig/com.realityinteractive.imageio.tga
[flashdebugger library]: https://github.com/jindrapetrik/flashdebugger
[Java Native Access - JNA]: https://github.com/twall/jna
[Open Imaging GIF Decoder]: https://github.com/DhyanB/Open-Imaging
[openjdk8 Stroker]: https://github.com/JetBrains/jdk8u_jdk
[Apache Flex SDK]: https://github.com/apache/flex-sdk
[FLA Compound Document Tools]: https://github.com/jindrapetrik/flacomdoc
[TomlJ]: https://github.com/tomlj/tomlj
[ANTLR]: https://www.antlr.org/
