# JPEXS Free Flash Decompiler

Opensource flash SWF decompiler and editor. Extract resources, convert SWF to FLA, edit ActionScript, replace images, sounds, texts or fonts. Various output formats available. Works with Java on Windows, Linux or MacOS.

## Application description and features
For information about using the software, list of features, etc., visit [FFDec Wiki](https://github.com/jindrapetrik/jpexs-decompiler/wiki/).

## Free-Decompiler.com website
In the past (before 2018), we were using *free-decompiler.com* domain as HomePage, GitHub for source code, now we moved all information to Github.

## Download application
For downloading the app, see [latest release](https://github.com/jindrapetrik/jpexs-decompiler/releases/latest).
Older versions and nightly builds are availabe on [releases section](https://github.com/jindrapetrik/jpexs-decompiler/releases)

### How to install
See [installation section of wiki](https://github.com/jindrapetrik/jpexs-decompiler/wiki/Installation)

## Source code
### How to get source
 You can make local copy of the sources with the following command:
```
git clone https://github.com/jindrapetrik/jpexs-decompiler.git
```

### Branches 
Git source control manager supports multiple code branches. We use two main branches.

* `master` - for released "stable" versions
* `dev` - for newest changes from developers - "nightly" version is released from this branch

You can switch to `dev` branch with following git command:
```
git checkout dev
```

### GIT recommended
It is recommended to have [GIT] commandline executables installed. Building script uses GIT to include revision number in to the binary. (For Windows, you must enable Git in windows commandline during installation.)

### Netbeans project

Source code contains Netbeans Project so you can open it in [Netbeans IDE]. Then you can use standard actions like Run, Build,Debug, Clean and Build in the IDE. Other specific tasks can be executed via menu on build.xml (see Ant part)

### Ant
If you do not have Netbeans, you can build source code also with Apache Ant.
After installing Ant it is good to put it into your PATH variable.
Open up commandline and navigate to sources directory.
To run application, execute task "run" by entering this command:
```
ant run
```
To only build, execute build task:
```
ant build
```
For creating EXE, Installer and ZIP version, there exist Ant tasks "exe","installer","release". These tasks require additional software installed:
* [launch4j] (3.5 or newer) - creates windows executable
* [NSIS] (Nullsoft Scriptable Install System) (3.0b3 or newer) - creates installer

You must configure installation path of these tools in tools.properties file, which could look like this for windows:
```
nsis.path = c:\\program files (x86)\\NSIS
launch4j.path = c:\\program files (x86)\\launch4j
```
### Building libraries

There are few libraries which need to be built too. These libraries are placed in "libsrc" directory.
* **FFDec_lib** - core of decompilation, SWF parsing, exporting
**This library is built automatically with main project, but can be build also separately with its own Ant script.**
* **jpacker** - used for compression of JavaScript Canvas scripts (Netbeans/Ant project)
* **jpproxy** - proxy part of FFDec (Netbeans/Ant project)
* **jsyntaxpane** - code editor (Netbeans/Apache Maven project)
* **LZMA** - used for SWF compression (Netbeans/Ant project)
* **nellymoser** - used for Nelly Moser sounds decoding (Netbeans/Ant project)
* **Swf2Exe** - Stub for "Save to EXE" feature (Delphi 7 Project)
* **ttf** - used for TTF font export (Netbeans/Ant project)
* **gnujpdf** - used for PDF export (Netbeans/Ant project)

## Change log
All notable changes are listed in the file [CHANGELOG.md](CHANGELOG.md)

## Deployment

### Nightly builds
When a commit is pushed/merged into `dev` branch, new prerelease version is created automatically by Travis CI,
these prerelease versions are called nightly builds. On releasing new nightly build, previous nightly build is removed.

### Stable versions
New stable version is created automatically by Travis CI when marking a revision in `master` branch with a tag in format `versionxxx`.


## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

Versions are in format `x.y.z`, for example `9.1.2`.
For the versions available, see the [tags on this repository](https://github.com/jindrapetrik/jpexs-decompiler/tags).

Nightly builds have additional suffix `_nightlyN` where `N` is number which increments with every (automatic) nightly releleas
and does not depend on the `x.y.z` numbers. (This means nightly number is *NOT* reseted to 0 when releasing stable)
Older nightly builds are *NOT* available through git tags.


## Authors
The decompiler was originally written by **Jindra Petřík** also known as **JPEXS**.
The application was made in Czech republic.

### Developers
* **JPEXS** - leader, development of the decompiler, website main admin, github account admin, organization
* **honfika** - development of the decompiler
* **Paolo Cancedda** - former developer
* ...other pushers on GitHub or Google Code

### Translators
* **Jaume Badiella Aguilera** - catalan translation
* **Capasha** - swedish translation
* **王晨旭** (Chenxu Wang) - chinese translation
* **focus** - russian translation
* **honfika** - hungarian translation
* **kalip** - italian translation
* **Krock** - german translation
* **Laurent LOUVET** - french translation
* **MaGiC** - portugese translation
* **martinkoza** - polish translation
* **Osman ÖZ** - turkish translation
* **pepka** - ukrainian and dutch translation
* **poxyran** - spanish translation
* **realmaster42** - portugese-brasil translation
* **Rtsjx** - chinese translation
* **koiru** - japanese translation

## Contact
You can use our Issue tracker to report bugs, but our support is VERY limited.
[https://www.free-decompiler.com/flash/issues](https://www.free-decompiler.com/flash/issues)

See [Frequently Asked Questions (FAQ) in wiki](https://github.com/jindrapetrik/jpexs-decompiler/wiki/FAQ) before you try to contact me.

### Email contact
Emergency contact to JPEXS developer is `jindra.petrik@gmail.com`.
But we prefer Issue tracker contact.

## Licenses + Acknowledgments
### Application

FFDec Application is licensed with GNU GPL v3, see the [license.txt](license.txt).
It uses modified code of these libraries:

* [JSyntaxPane] (Code editor) - Apache License 2.0
* [Muffin] (Proxy) - GPL

And links also these libraries:

* [Java Native Access - JNA] (Registry association, Process memory reading) - LGPL
* [Insubstantial] (Substance Look and Feel, Flamingo Ribbon component) - Revised BSD
* [javactivex] (Flash Player ActiveX embedding) - LGPLv3
* [flashdebugger library] (Debugging ActionScript) - LGPLv3
* FFDec Library (LGPLv3) - see below

Application uses also some icons of the [Silk icons pack], [Silk companion 1] and [FatCow icons pack].
### Library

FFDec Library is licensed with GNU LGPL v3, see [license.txt](libsrc/ffdec_lib/license.txt) for details.
It uses modified code of these libraries:

* [sfntly] (WOFF font export) - Apache License 2.0
* [JLayer] (Decoding MP3) - LGPL
* UAB "DKD" NellyMoser ASAO codec (Decoding Nelly Moser sound format) - LGPL
* [Animated GIF Writer] (Frames to GIF export) - Creative Commons Attribution 3.0 Unported
* [Animated GIF Encoder] (Frames to GIF export)

And links also these libraries:

* [LZMA SDK] (SWF de/compress) - public domain
* [Monte Media Library] (Frames to AVI export) - LGPL
* [Fontastic] (Font TTF export) - LGPL
* [DoubleType] (Font TTF export) - GPLv2
* [jPacker] (Canvas scripts compression) - MIT License
* [gnujpdf] (PDF export) - LGPL License

[GIT]: http://git-scm.com/downloads
[Netbeans IDE]: http://www.netbeans.org/
[Apache Ant]: http://ant.apache.org/
[launch4j]: http://launch4j.sourceforge.net/
[NSIS]: http://nsis.sourceforge.net/
[JSyntaxPane]: https://code.google.com/p/jsyntaxpane/
[Muffin]: http://muffin.doit.org/
[Java Native Access - JNA]: https://github.com/twall/jna
[Insubstantial]: http://shemnon.com/speling/2011/04/insubstantial-62-release.html
[javactivex]:https://github.com/jindrapetrik/javactivex
[flashdebugger library]: https://github.com/jindrapetrik/flashdebugger
[Silk icons pack]: http://www.famfamfam.com/lab/icons/silk/
[Silk companion 1]: http://damieng.com/creative/icons/silk-companion-1-icons
[FatCow icons pack]: http://www.fatcow.com/free-icons
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
