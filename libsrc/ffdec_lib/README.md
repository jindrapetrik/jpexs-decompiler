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

## Usefull packages and classes
### `com.jpexs.decompiler.flash` - SWF reading and writing
* `SWF` - Basic class for SWF manipulation
* `SWFInputStream` - Reading of SWF data
* `SWFOuputStream` - Writing of SWF data

### `com.jpexs.decompiler.flash.abc` - AS3 bytecode (ABC format)
* `ABC` - AS3 bytecode structure
* `ABCInputStream` - Reading AS3 bytecode structure
* `ABCOutputStream` - Writing AS3 bytecode structure
* `ScriptPack` - **important** - for purposes of easily displaying of flashCC/alchemy long scripts, ABC scripts can be splitted into so called `script packs` which is script index and list of trait indices. FFDec always displays a scriptpack, not a whole script.

### `com.jpexs.decompiler.flash.abc.avm2` - AS3 AVM2 code
* `AVM2Code` - code handling
### `com.jpexs.decompiler.flash.abc.avm2.deobfuscation` - Some AS3 AVM2 code deobfuscation classes
### `com.jpexs.decompiler.flash.abc.avm2.graph` - Generates high-level code from instructions of AVM2 code
### `com.jpexs.decompiler.flash.abc.avm2.instructions.*` - Instructions for AS3 AVM2 code
### `com.jpexs.decompiler.flash.abc.avm2.model.*` - Model representing generated high-level code of AS3
### `com.jpexs.decompiler.flash.abc.avm2.parser.*` - Compilers of AS3 and its p-code
### `com.jpexs.decompiler.flash.abc.types` - ABC format related types
### `com.jpexs.decompiler.flash.abc.types.traits` - ABC traits
### `com.jpexs.decompiler.flash.abc.usages` - Get usages feature
### `com.jpexs.decompiler.flash.action` - AS1/2 related
### `com.jpexs.decompiler.flash.action.swf*` - AS1/2 Actions
### `com.jpexs.decompiler.flash.action.model` - Model representing generated high-level code of AS1/2
### `com.jpexs.decompiler.flash.action.parser.*` - Compilers of A1/2 and its p-code
### `com.jpexs.decompiler.flash.configuration` - Configuration of the decompilation
### `com.jpexs.decompiler.flash.exporters` - Exporters of resources/scripts to various formats
### `com.jpexs.decompiler.flash.iggy` - Iggy format related
### `com.jpexs.decompiler.flash.importers` - Importers of resources/scripts from various formats
### `com.jpexs.decompiler.flash.tags` - Definitions of SWF tags
### `com.jpexs.decompiler.flash.timeline` - Browsing SWF in a timeline
### `com.jpexs.decompiler.flash.types` - Structures used in SWF file
### `com.jpexs.decompiler.flash.xfl` - Export to FLA and XFL
### `com.jpexs.decompiler.flash.graph` - Generates high-level code from instructions of AS1/2/3 code
### `com.jpexs.decompiler.flash.graph.model.*` - Shared model representing generated high-level code


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