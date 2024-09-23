# JPEXS Free Flash Decompiler
![Build passing badge](https://github.com/jindrapetrik/jpexs-decompiler/actions/workflows/main.yml/badge.svg?branch=dev)

Open Source Flash SWF decompiler and editor. Extract resources, convert SWF to FLA, edit ActionScript, replace images, sounds, texts and fonts. Various output formats available. Works with Java on Windows, Linux and macOS.

## Application description and features
For information about using the software, list of features, etc., visit [FFDec Wiki](https://github.com/jindrapetrik/jpexs-decompiler/wiki).

## Free-Decompiler.com website
In the past (before 2018), we were using *free-decompiler.com* domain as homepage and GitHub for the source code, We've now moved all information (except the issue tracker) to GitHub.

## Download application
For downloading the app, see [latest release](https://github.com/jindrapetrik/jpexs-decompiler/releases/latest).
Older versions and nightly builds are availabe at the [releases section](https://github.com/jindrapetrik/jpexs-decompiler/releases)

### How to install
See [installation section of the wiki](https://github.com/jindrapetrik/jpexs-decompiler/wiki/Installation)

## Source code
### How to get the source
 You can make a local copy of the sources with the following command:
```
git clone https://github.com/jindrapetrik/jpexs-decompiler.git
```
This assumes you have git installed on your system.

### Branches 
Git source control manager supports multiple code branches. We use two main branches.

* `master` - for released "stable" versions
* `dev` - for newest changes from developers - "nightly" version is released from this branch

You can switch to `dev` branch with following git command:
```
git checkout dev
```

### GIT recommended
It is recommended to have [GIT] commandline executables installed. Building script uses GIT to include revision number in to the binary. (For Windows, you must enable Git in windows command line during installation.)

### Netbeans project

Source code contains Netbeans Project so you can open it in [Netbeans IDE]. Then you can use standard actions like Run, Build, Debug, Clean and Build in the IDE. Other specific tasks can be executed via menu on build.xml (see Ant part)

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
When a commit is pushed/merged into `dev` branch, a new prerelease version is created automatically by Github actions CI.
These prerelease versions are called nightly builds. On releasing a new nightly build, the previous nightly build is removed.

### Stable versions
A new stable version is created automatically by the Github Actions CI when marking a revision in `master` branch with a tag in format `versionx.y.z`.

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

Versions are in format `x.y.z`, for example `9.1.2`.
For the versions available, see the [tags on this repository](https://github.com/jindrapetrik/jpexs-decompiler/tags).

Nightly builds have additional suffix `_nightlyN` where `N` is number which increments with every (automatic) nightly release
and does not depend on the `x.y.z` numbers. (This means nightly number is *NOT* reseted to 0 when releasing stable)
Older nightly builds are *NOT* available through git tags.

## Authors
The decompiler was originally written by **Jindra Petřík** also known as **JPEXS**.
The application was made in Czech Republic.

### Developers
* **JPEXS** - leader, development of the decompiler, website main admin, github account admin, organization
* **honfika** - development of the decompiler
* **Paolo Cancedda** - former developer
* ...other pushers on GitHub or Google Code

### Translators
* **Jaume Badiella Aguilera** - catalan translation
* **Capasha** - swedish translation
* **王晨旭** (Chenxu Wang), **晓之车**, **安安** - chinese translation
* **focus** - russian translation
* **honfika** - hungarian translation
* **kalip** - italian translation
* **Krock** - german translation
* **Laurent LOUVET** - french translation
* **MaGiC** - portuguese translation
* **martinkoza** - polish translation
* **Osman ÖZ** - turkish translation
* **pepka** - ukrainian and dutch translation
* **poxyran** - spanish translation
* **realmaster42**, **alimsoftware** - portuguese-brasil translation
* **Rtsjx** - chinese translation
* **koiru** - japanese translation
* **J. Kramer** - dutch translation

## Contact
If you want to report a problem or request new feature, use our issue tracker at [https://www.free-decompiler.com/flash/issues](https://www.free-decompiler.com/flash/issues)

You should see [Frequently Asked Questions (FAQ) in wiki](https://github.com/jindrapetrik/jpexs-decompiler/wiki/FAQ) before.
Also see [Known problems list in wiki](https://github.com/jindrapetrik/jpexs-decompiler/wiki/Known-problems)

### Email contact
Emergency contact to JPEXS developer is `jindra.petrik@gmail.com`.
But we prefer Issue tracker contact.

## Licenses + Acknowledgments
### Application

FFDec Application is licensed under the GNU GPL v3 (GPL-3.0-or-later) licence, see the [license.txt](license.txt).
It uses modified code of these libraries:

* [JSyntaxPane] (Code editor) - Apache License 2.0

And links also these libraries:

* [Java Native Access - JNA] (Registry association, Process memory reading) - LGPL
* [Insubstantial] (Substance Look and Feel, Flamingo Ribbon component) - Revised BSD
* [javactivex] (Flash Player ActiveX embedding) - LGPLv3
* [flashdebugger library] (Debugging ActionScript) - LGPLv3
* [FLA Compound Document Tools] (Exporting to FLA CS4 and below) - LGPLv2.1
* FFDec Library (LGPLv3) - see below

Application uses also some icons of the [Silk icons pack], [Silk companion 1], [FatCow icons pack] and [Aha-Soft icons pack].

## Library
See [library README](libsrc/ffdec_lib/README.md) for more info about FFDec library.

[GIT]: http://git-scm.com/downloads
[Netbeans IDE]: http://www.netbeans.org/
[Apache Ant]: http://ant.apache.org/
[launch4j]: http://launch4j.sourceforge.net/
[NSIS]: http://nsis.sourceforge.net/
[JSyntaxPane]: https://code.google.com/p/jsyntaxpane/
[Java Native Access - JNA]: https://github.com/twall/jna
[Insubstantial]: http://shemnon.com/speling/2011/04/insubstantial-62-release.html
[javactivex]:https://github.com/jindrapetrik/javactivex
[flashdebugger library]: https://github.com/jindrapetrik/flashdebugger
[FLA Compound Document Tools]: https://github.com/jindrapetrik/flacomdoc
[Silk icons pack]: http://www.famfamfam.com/lab/icons/silk/
[Silk companion 1]: http://damieng.com/creative/icons/silk-companion-1-icons
[FatCow icons pack]: http://www.fatcow.com/free-icons
[Aha-Soft icons pack]: http://www.aha-soft.com
