# Change Log
All notable changes to this project will be documented in this file.

## [11.3.0] - 2020-04-25
### Added
- Possibility to open SWF files using open on Mac
- Updated turkish translation

### Fixed
- #1500 Maximum SWF version constant increased, which is used by the memory search and also in the header editor
- #1457 AS3 switch without lookupswitch ins detection
- #1457 pushing on stack before exit item (return/throw)
- #1503 NullPointer Exception on commandline FLA export
- AS3 direct editation - invalid generation of lookupswitch
- AS3 direct editation - fix access to protected members with super
- #1529 commandline selection of tag ids now applies to sprites and buttons

### Changed
- #1378 Transparent background on PNG/GIF sprite export
- SWF background on shape BMP export

## [11.2.0] - 2018-09-08
### Added
- Warning panel on scripts containing §§ instructions (Tip for Settings/Automatic deobfuscation)
- Export AS1/2 P-code as GraphViz
- Display better Graph using GraphViz (Must be configured in Advanced settings / Path)
- Copy AS1/2 Graph source (GraphViz) to clipboard - rightclick menu on graph
- AS1 slash syntax support (decompilation, direct editation)
- Setting of limit of executed instructions during AS1/2 deobfuscation
- AS1/2 deobfuscation of empty stack pops

### Changed
- AS1/2 Better unresolved constant handling - §§constant(xx) func instead of §§constantxx
- AS1/2 Using eval, set functions on obfuscated names instead of §§ syntax
- Default limit of maximum executed instructions during AS1/2 deobfuscation raised to 10000

### Fixed
- Better continue in for handling
- AS1/2 For in break detection with inner switch statement
- AS1/2 Using temporary registers after for..in (causing incorrect chained assignments handling, etc.)
- AS1/2 getProperty, setProperty handling
- AS1/2 callmethod action arguments
- Fixed §§push after continue - should be before (usually on obfuscated code)
- AS1/2 Delete operator with nonIdentifier parameters (e.g. spaces or obfuscated)
- DefineBits (with JPEGTables) tag export
- No disabling autoOpenLoadedSWFs checkbox when ActiveX player not available (User can use Run button)
- Displaying contents of local variables (AS3) while debugging
- #1415 freezing on manually closing Flash player debug session
- #1484 AS import error printout on commandline (NullPointer)

## [11.1.0] - 2018-05-24
### Added
- [#1449] Turkish translation by Osman ÖZ
- AS2 classes: maintain order of variables, and methods (place variables before methods)
- AS1/2: displaying script path in the error log when jump to invalid address
- AS1/2: Try..catch with Error types - decompilation and direct editation
- AS1/2: Properly handling of Flash7 scripts exported to Flash5/6 - ActionDefineFunction local registers

### Changed
- AS1/2 direct editation - generated constantpool is sorted according to ActionPush position
 
### Fixed
- Accessing font list on demand - prevents loading X11 on systems without UI
- Better AS2 class detection
- AS1/2 break statement decompilation in for..in loops
- AS2 direct editation - not generating Pop in class header ifs
- AS1/2 deobfuscation - ActionRandom fixed for nonpositive numbers
- AS1/2 switch statement detection - fixes of default section position
- AS1/2: break on the end of for..in loop
- AS1/2: Export selection dialog did not allow to select script export formats and/or export script at all
- AS3 P-code - HAS_OPTIONAL flag in AVM2 code displayed twice
- Turkish locale toLowerCase I problem fix - causing not loading main window at all
- [#1456] jsyntaxpane (code display/editor) fixed for Java 9/10

## [11.0.0] - 2018-01-17
### Added
- [#1240] AS search using multiple threads when parallel speedup is enabled
- [#1308] Search by all P-code files in AS3
- [#1333] Exporting sprites as swf files
- [#1365] Allow to configure all types of configuration settings from command line
- [#1369] Allow zoom in sprite (and button) export
- Debug tooltips on hover
- AS3 deobfuscation - removing push instructions immediately followed by pop

### Changed
- Homepage is now on GitHub jindrapetrik/jpexs-decompiler instead of free-decompiler.com
- Update checking now checks changes on GitHub releases

### Removed
- Removed help us section
- Removed changeslog from new version dialog

### Fixed
- [#1327] P-code editing: error message and syntax highlighting fixed when instruction name contains upper cased letter
- [#1343] AS 1/2 direct editation critical bug
- [#1348] Cannot properly export frame with cyrillic symbols to SVG
- [#1354] Various FLA export problem fixes
- [#1367] Raw edit conditional type fix.
- [#1401] SVG export: duplicate pattern IDs fixed
- [#1402] SVG export: certain font characters not exported properly fixed
- [#1430] AS 1/2 deobfuscation fixes
- Opening loaded SWF files during run fixed
- Not opening first script when clicked on app startup

## [10.0.0] - 2016-12-24
### Added
- Iggy Files support - reading and (limited) editation of fonts, texts and AS3 - 64 bit variant only
- optional AS3 direct editation with Flex SDK
- AS3 p-code editing - metadata read/write support
- AS3 p-code editing - end of the block command like in RABCDasm
- AS3 p-code editing - popup docs for more than instructions
- Debugger - New columns for variable details - scope, flags, trait
- Debugger - Add watch feature
- AS3 decompilation - colliding trait/class names handling - show hash suffix with namespace index on such cases
- Deobfuscation Tool - Fix colliding trait/classes via toolbar command
- Auto rename identifiers option now fixes colliding trait/classes aswell
- [#1254] FLA export - detecting scripts on AS3 timeline
- [#907] FFDec Library JAR file has version inside it.
- [#1311], [#1313] actionScript source font size
- Display warning when library version and GUI version mismatch
- Changelog file
- [#1308] Search by all P-code files (AS 1/2 only)

### Changed
- [#1189] AS3 - sort imports to have same order always
- GUI: AS3 P-code header show actual trait type and method type
- GUI: Script editing buttons now named "Edit ActionScript" and "Edit P-code"
- Set advance values button has confirm dialog with information
- [#1274] Linux package no longer requires Oracle Java only
- Library now packaged inside ZIP file

### Fixed
- P-code docs formatting fix
- Export dialog - handling sprite and SWF frames correctly
- [#1275] debugger - show local variables fixes
- AS3 p-code editing - popup docs correctly displayed when label on line start
- [#1278] replacing DefineBits error
- [#1281] DefineFont 2/3 getting character advance value when replacing fix
- Set advance values button - Do not set advance if the char cannot be displayed in source font
- AS3 Goto declaration for single character names
- Identifier renaming for top level classes
- AS3 direct editation not correctly saving local register names
- [#1254] FLA export - placing AS3 classes to FLA directory instead of scripts dir
- Mac OS X installer fix (.pkg)
- [#1289] AS1/2 direct editation - variables used in inner functions must not be stored in local registers
- [#1283] AS3 Unbounded Vector - Vector<*> decompilation and direct editation fix
- [#1294] Font editation (DefineFont2/3) - correct switching of wide character codes
- [#1302] Callpropvoid instruction docblock not correct
- [#1309] recent files not getting updates
- [#1312] faster colliding usages finder
- [#1303] garbled text when exporting frame with text
- [#1314] user interface: button order
- Internal viewer shows red image when bitmap fill is not available (see issue [#1320])
- [#1323] Audio playback fails

## [9.0.0] - 2016-08-12
### Added
- Instance metadata (AMF3) editing in PlaceObject4
- [#1156] Flash Viewer - DefineScalingGrid support (9-slice scaling)
- [#1171] Export stroke scale to FLA
- FLA export - check invalid unicode characters
- [#1170] Extract from memory in commandline
- Reload one vs Reload all buttons
- ABC: Float and Float4 support
- AS3 p-code instruction documentation in GUI
- [#1241] Settings to show original bytes in hex view
- Search in dump view
- Jump to resources view from hex view
- Show warning on 32bit JRE

### Changed
- [#1162] improved opening loaded SWF files
- Flash Viewer - skip frames when not on time
- [#1199] Automatically import alpha channel to JPEG3/4 from PNG

### Fixed
- [#1151] Filters on texts fixed
- [#1128] Adding characters to font fixed (FontAlignZones not removed)
- [#1163] Clicking open->file makes program buggy
- Refresh tree after raw edit
- [#1172] Text double escape fix
- [#1174] Change language fix
- some AS2 deobfuscation fixes
- [#1183] Index out of bounds fix
- Implicit coersion on binary/unary opfix
- debugger: corect display variable values through getters
- Multiple XLF export fix
- [#1193] FLA export - text tag advance fix, one layer shape fix
- [#1193] FLA export - smoothed image detection, export raw JPEG data
- [#1193] Export space character to TTF correctly
- [#1200] Previous search text selected when quick find
- Flash viewer: aspect ratio on startup fix
- [#1198] Saving trait slot const value
- Zoom parameter commandline fixed
- [#1205] clipping fixed
- [#1194] Wrong sound effects in FLA
- [#1210] Frame Export fix
- Improved/fixed go to declaration in AS3
- [#1217] PCode window not in same position as AS
- Hide memor search on non windows platform
- [#1244] Incorrect showing of NOP instructions
- [#1244] Remove unknown actions when deobfuscation is enabled, compole unknown instructions back
- [#1241] File content is different from hex view
- [#1247] Incorrectly handling remainingbytes for DefineCompactedFont
- [#1236] won't open fixed
- [#1251] SWF not same after export XML and import back
- [#1265] Error during export
- [#1268] Font export - Using second glyph when two glyphs for one character found
- [#1268] GFX compacted font - fixed advance values on export

## [8.0.1] - 2016-02-20
### Changed
- FFDec debug tab in advanced settings moved to other tabs

### Fixed
- [#1161] AS1/2 deobfuscation broken
- AS1/2 Simplify expressions fix

## [8.0.0] - 2016-02-18
### Added
- Debugger - AS1/2 Show registers
- Debugger - display variables in the tree structure
- Debugger - set value of variable
- Debugger - AS1/2 View constantpool
- Debugger - P-code level debugging for both AS1/2 and AS3
- Basic SVG import for shapes
- Simplify expression setting
- [#1118] Loading characters through ImportAssets - show as readonly
- [#409], [#1132], [SkinPart] metadata support - decompilation and direct editation in AS3
- [#1134] compiling §§ instructions back while direct editation (§§goto is still missing)
- [#1121] Ability to save binary data by its name
- [#1052] Add object to existing frame
- Allow adding tag to main timeline
- AS1/2: Ctrl+click to declaration of variables, registers
- Allow trait specification in pcode import
- Icons for tag types in Dump view
- Show error message when a text tag is invalid (glyph index problem)
- AS3 direct editation - store local register names in debug info = allow to rename them

### Changed
- New application icon and splash screen
- [#1145] AS3 better declaration type detection, better convert_x instruction handling
- Binary export - use .swf extension for swf files
- Better tree labels in generic tag editor (Raw edit)
- [#758] Allow zooming more than preview area in internal viewer

### Fixed
- [#1096] FLA export - pretty print
- [#1104] AS1/2/3 Script Importat not working
- [#1107] Text Offset Incorrect fixed
- [#1106] New Shapes replace function fix
- [#1113] It takes too long to switch between rendered sprite
- [#1075] Lenght of DefineText in some cases
- [#1127] autoRenameIdentifiers is not supported in CLI mode
- [#1128] Letterspacing bug (after font embed): ignore letterspacing when character changed
- [#1103] Foreach variables fixes
- AS3 Switch fixes
- Default clause position in switch
- [#1133] Incorrect frame order for nested sprite
- [#1135] Handle try "to" in p-code correctly
- Font wideOffsets,wideCodes fixed in DefineFont2/3
- AS3: super method call
- [#1138] All exported videos are the same file which may be broken
- [#1139],[#930] Windows Installer: Correct ActiveX download link, Download latest java from webpages
- [#1137] running flashplayer(debugger) executable in Linux/MacOs
- [#1144] Command line argument renameInvalidIdentifiers
- [#1145] double not (!!) not removed
- [#1147] Sprite is exported incorrectly
- [#1148] handing end of stream exception in abc reader, loc exception
- [#1152] Font info tag modified tag was not set => saved swf was corrupt
- [#1154] Some 32bit JRE problems - program won't start
- [#1145] Correct precedence handling on binary operators
- [#116] not resolving unusual tags in DefineSprite

## [7.1.2] - 2015-12-03
### Fixed
- AS3 debugger start halt fix
- AS1/2 debugger fix on nondebug enabled SWFs
- AS1/2 debugger fix for functions
- Debug menu item enabled fix
- AS3 local reg index fix
- Advanced settings calendar
- AVM2 instructions in hex view
- [#1070] Incorrect switch decompilation
- [#1098] Import XML fix

## [7.1.1] - 2015-11-23
### Fixed
- Critical debugger fix - widelines

## [7.1.0] - 2015-11-23
### Added
- AS1/2 debugger
- Breakpoint/IP marker on line beginning

### Changed
- Starting debugger on demand
- Installer message about playerglobal is only warning now

### Fixed
- [#1033], [#1083] AS3 deobfuscation issues
- [#1091] AS 1/2 direct editation saving

## [7.0.1] - 2015-11-18
### Fixed
- Debugger: Adding breakpoint if script initializer not displayed

## [7.0.0] - 2015-11-18
### Added
- AS3 Debugger - breakpoints, stepping, show variables
- Faster AS3 direct editation

### Changed
- Better Configuration of flashplayer paths

### Removed
- Removed old "debugger" buttons
- Removed search from browsers cache - inactual code

### Fixed
- Many AS3 direct editation related bugs
- [#1076] export fix

## [6.1.1] - 2015-10-30
### Fixed
- Deobfuscate AS3 metadata
- [#1068] MorphShape with focal gradient fix, FLA XML export formatting fix
- [#1063] AS3 direct edit - script initializer fix, generating method names
- XML export/import fixes
- [#1019] Namespace imports fix
- AVM2 code execution fix
- [#1016] AS3 direct editation fixes
- [#1010] AS2 direct editation - internal and override is not a reserved word
- [#1008] pushshort instruction diassembly
- [#1004] this/super can be AS1/2 variable
- [#933] AS3 allow numbers as object literal keys

## [6.1.0] - 2015-10-26
### Added
- Open other loaded SWFs during playback (useful for loaders)
- Export uncompressed data from dump tree
- Print performance statistics from commandline
- [#1062] Editing/displaying script initializers
- Enable debugging on SWF file (commandline)

### Changed
- Faster syntax highlighting
- Better AS1/2 deobfuscator
- [#418] AS3 deobfuscator improved

### Fixed
- AS call method fix (first parameter is "this")
- [#1047] open all scripts folders
- [#812] decompile fail
- [#1056] deltaY missing when adding a new StraightEdgeRecord
- [#1057] Editing as in editor results in package name moving
- [#991] GUI export
- [#689] Ignore Case not correctly toggled
- [#1060] reversed and/or detection in some cases
- [#1037] isXML call

## [6.0.2] - 2015-09-12
### Added
- AS3: Display and direct edit trait Metadata
- Allow to specify tag type on image or shape import
- Convert image tags from commandline
- [#489] Hex decode very large integers
- Add new tags without show empty folders
- Dependent characters in basic tag info
- [#1007] replace bytearray in raw editing
- Italian translation

### Changed
- AS2 parser - add string to constant pool if there is not enough space
- [#1044] AS2 - order scripts by physical location, name by offset

### Removed
- Deprecated commandline parameters removed

### Fixed
- JNA problems on some JDKs
- [#947] Marklevels errors ignored 17a94b7
- [#953] Mac application permission fix (maybe)
- [#954] IndexOutOfBounds fix
- [#950] AddTrait setting modified fix
- [#945] AS1/2 directeditation fix - member named as global function
- [#957] AS1/2 IndexOutOfBounds fix
- [#956] Invalid jump offsets warning
- [#968] Sprites export with wrong coloring
- [#978] case sensitivity of filenames
- [#955] AS2 decompilation problem
- Image alpha fixes
- [#966] Go to document class
- [#991] scripts exporting
- [#999] save as fla
- [#1000] image export for malformed JPEG3 tags
- [#1017] store alchemy opcodes with wrong order
- [#1030] stack overflow fix

## [6.0.1] - 2015-07-06
### Added
- Special §§ instructions marked as red
- [#949] Replace alpha channel from commandline
- AS3 deobfuscation from commandline
- Option to ignore FlashCC/Alchemy packages

### Changed
- [#944],[#991],[#939],[#942] AS3 deobfuscation improvements

### Fixed
- AS1/2 deobfuscation fixed
- [#952] Not loading SWF without extension

## [6.0.0] - 2015-07-04
### Added
- New AS3 deobfuscation method
- Internal "preprocessor" §§ actions introduced - §§pop,§§push,...
- Allow reload FFDec when no SWF is opened
- [#858] Allow to set compression type in header
- [#905] Show codec details for sound items
- Better alchemy/DomainMemory instruction handling
- Better obfuscated names handling
- [#920] Export instance name to SVG
- [#921] Export html DefineEditText to SVG
- Open multiple files with drag and drop
- Better "multi packs" handling (Alchemy)
- SWF version 29 to flash player 18 mapping
- ImportAssets2 sha1 field
- [#924] Sprites to image from commandline
- AS1/2 direct editation big numbers fix
- Allow to add FILTERs and SHAPERECORDs in generic tag editor
- Enable close all menu when no swf is selected
- Restore modified state even when something goes wrong
- Some old tags added

### Changed
- AS decompilation highly improved
- Better &&, || handling
- DoABCDefine renamed to DoABC2
- Separated Sprite export settings

### Deprecated
- Old AS1/2/3 deobfuscation method marked as deprecated (can be enabled back deep in the configuration)

### Fixed
- Many decompilation problems - EmptyStack exception, Maximum recursion level reached, etc.
- Few menu issues
- [#895] Correct handling CMYK JPEG
- [#884] AS direct edit assignment
- [#899] Show script after AS3 direct editation
- Some AS1/2 parser problems
- [#903] FLA export - fix for missing fontname, lastframe
- [#855] AS3 direct edit - for..in variable declaration fix
- [#850] Constant initialization for same multinames
- [#832] AS3 direct edit - other ABCs resolving fix
- [#904] Cannot export images
- [#910] Missing instructions
- Opening not existent files on restoring last session
- [#922] Edit text leading
- Put image before shape on shape replace
- [#916] Replacing Shape corrupts SWF
- JRE setup parameters fixed
- [#938] Parallel speedup limit fix

## [5.3.0] - 2015-05-25
### Added
- Generic tag editor: improved table editing (import/export assets tags, etc.)

### Changed
- Classic (nonribbon) UI improved - has same items as Ribbon UI
- Icons improvements
- Disabling menu items when work in progress

### Fixed
- [#897] Classic UI fix

## [5.2.0] - 2015-05-22
### Added
- UI8 editbox for swf version in header panel
- Basic tag information panel

### Changed
- AS1/2: Shown only the constant pool(s) in pcode editor
- Do not allow to chage tag tree selection, when current tag is under editing
- Faster bitmap export
- Using less memory when playing sounds
- Error message changed when the opened file is not swf

### Fixed
- [#470] panels size after resizing from/to full screen
- [#877] A small glitch after search in AS
- [#878] small glitch after saving P-code or swf file
- [#470] glitch
- [#845] If frame consist 2 DoAction then it imports only first one
- pdf export (when no frame exists)
- text rendering (alpha channel was ignored),
- bmp export (paddings when width%2==1)
- [#883] -dumpSWF option does not work anymore
- [#882] Canvas export border size
- [#760] Internal viewer line linear gradient fill is not working
- [#887] error on export a special swf's P-CODE
- Extensions of exported images fixed

## [5.1.0] - 2015-05-04
### Added
- Allow to copy/move multiple tags, and dependencies
- [#842] For reconstruction if debug line info present
- [#841] Loop control for sound preview
- [#845] Import exported AS1/2 (DefineButton2&DefineSprite) button
- Scrollbar added to fontpanel
- SWF header editor
- Configure what object types to export in exportdialog

### Changed
- Better gif exporter
- [#772] closing loading dialog now cancels the loading of the swf
- [#762] export pcode with different extension

### Fixed
- CRITICAL: Update System Bug causing updates not working
- [#862] AS3 asm: do not read beyond return/throw instructions
- [#865], [#613] ribbon prefered width fix
- [#868] export path fix, allow to export buttons
- [#865] TagTree font size problem on high resolution screens
- [#713],[#807] Installer for 4.0+ fails to access Adobe Website
- [#728] Large fonts, [#857] add scroll on DefineFont3

## [5.0.2] - 2015-04-18
### Added
- Reopen last session

### Fixed
- ffdec.sh file line endings fixed

## [5.0.1] - 2015-04-18
### Fixed
- [#860] Opening bundle (zip, swc, any binary file) files fixed

## [5.0.0] - 2015-04-18
### Added
- Color skins
- [#824] Mac OS X package
- [#809] Move left,right buttons for DefineTexts using translatex parameter
- [#805] Editor mode for DefineTexts
- [#825] Hotkeys for next/previous DefineText
- Export/Import symbol classes/export asset tags
- Frame export progress
- [#737] Single file script export
- Displaying changed AS3 scripts in GUI as bold
- Additional character info tags placed under character node
- New icons for other tags (metadata,fileattributes,setbackground,place/remove)
- Metadata tag editor

### Changed
- Default color skin altered
- [#350] Allow only one running instance (Windows only, can be turned off)
- SWFs in zip based bundles (SWC for example) can be modified & saved
- Performace improvements
- More compact SWF-XML format
- Marking changed parentnodes as bold too

### Fixed
- [#814] Exporting with scale problem
- [#816] P-code not shown after class initializer trait selection
- [#835] Static initializer improvements
- AS3 direct editing - local register decrement fix
- AS3 direct editing - maintain register order/names
- [#836] AS1/2/3 Correct expression precedence handling
- AS3 preincrement
- [#848] Correct toggling text switches
- [#817] AS1/2 for..in variable declaration
- [#849] Attribute member
- [#852] Ignore case for russian characters
- [#837] AS3 try..finally without catch

## [4.1.1] - 2015-02-21
### Added
- Export/Import XML added to ribbon menu
- Few GUI enhancements
- Undo tag changes context menu

### Changed
- Java 8 now required

### Removed
- Removed support for Java below 8

### Fixed
- [#811] export ActionScript

## [4.1.0] - 2015-02-18
### Added
- XML export/import
- confirmation dialogs added
- Add support for non-standard ABC-compressed SWF file
- [#745] Copy tag to another SWF
- [#803] Align text in DefineText

### Changed
- performance improvements
- [#758] Zoom to fit is dynamic

### Fixed
- [#738] Frame export
- [#742] Can't edit frames
- [#747] Move tag to adds extra frame
- [#749] Internal viewer Sprite fill color
- [#752] Sound is not stopped
- [#753] Reload swf
- [#759] Decompilation § symbol
- [#766] Can't extract all resources
- [#768] Super calls not being correctly recognized
- [#773] Scripts associated with ClipActions are not shown
- [#776] Stop working after setting "number of threads" to 0
- [#783] No OK box when edited script or text was saved
- [#785] Text search. Remember last choise, Unicode case insensitive search
- [#787] Search in AS bug (when navigating to searched results)
- [#788] Add DefineCompactedFont Tag to gfx file
- [#790] Impossible to change letters under a font
- [#794] Font extraction fails sometimes
- [#798] Close file streams after export, exporting progress
- [#800] Unexpected deleted carrier return in DefineEditText
- Build fix on Linux
- Fis Startup Script for OpenJDK
- Other minor fixes

## [4.0.5] - 2014-12-01
### Added
- Escape control characters in strings, identifier names
- [#676] import text error messages / logging enhancement
- [#734] \xAB escapes, \uABCD escapes
- [#687] AS3 - allowing p-code comments on separate line
- [#709] Text Export to Single File with custom filename

### Fixed
- [#732] Random freezing - JavactiveX library updated.
- [#730] Not working without ActiveX fix on Windows
- [#735] Automatic deobfuscation not correctly switched (required restart)

## [4.0.4] - 2014-11-23
### Changed
- better file cache, removing unneccessary temp files

### Fixed
- obfuscated identifiers

## [4.0.3] - 2014-11-23
### Added
- [#722] Go to next/previous frame
- BMP file format export (images,frames,shapes) and import(images)

### Fixed
- [#725] various AS direct editation bug fixes - namespace compilation, AS 1/2 strict equals, submethod scope, unbounded type
- [#715] namespace resolving fix
- [#635],[#726] placing cursor inside Unicode characters

## [4.0.2] - 2014-11-22
### Added
- show frame number during play
- flashplayer - show controls for DefineSprite
- goto frame
- [#716],[#717],[#718] Proxy - save SWF, replace, copy URL, filesizes, table design

### Changed
- [#720] edited shape tag is not marked as modified after replacing
- reorganized about dialog

### Fixed
- [#719] null swf name in Proxy after cancelling rename dialog
- flashplayer - font display
- [#723] saving swf with invalid referenced characters
- DefineCompactedFont paging
- [#288] Less memory usage during FLA export
- Corrected syntax hilighting for AS3 P-code

## [4.0.1] - 2014-11-12
### Fixed
- [#713] Installer can continue when no file can be downloaded
- Fixed shapes
- Checking for updates moved to separate thread

## [4.0.0] - 2014-11-11
### Added
- [#677] Zoom level in export settings
- internal viewer: linear/srgb gradients
- zooming buttons for flashplayer/internal viewer
- stroke scaling modes for canvas export
- create snapshot button
- [#389] Selecting font face on import
- [#701] Importing font from TTF file
- Reorganized font panel
- [#707] Debugger for logging messages
- [#302] AS3: Better Ctrl+Click handling with underline, more declaration targets
- [#685] Getting local register names from debug info can be disabled
- Adding new tags
- [#698] Allowing unicode letters in identifiers
- [#710] Information about deobfuscation in error comments
- One EXE for 32/64 bit, uses percentage memory.
- EXE SplashScreen
- New Improved Windows Installer (NSIS) - can install Java and FlashPlayer, download playerglobal.swc
- Config setting to load inner SWFs automatically
- Replace shape with image

### Changed
- better FlashPlayer integration using JavactiveX library
- Faster building tag tree
- Faster timeline construction
- [#711] Improved folder view - faster and with correct context menu

### Fixed
- AS2 deobfuscation fixes
- AS2 loops fix
- [#681] Linux script fixes
- AS2 constructor name fix
- [#688] AS3 direct edit fixes
- [#691] AS3 p-code reading/saving fix
- AS3 direct edit -submethod name resolve fix
- frames to html canvas fix
- [#524] Mask layer not applied when nonempty script layer
- [#663] AS3 imports fixes
- Font export of dot character
- Font panel Yes button fixed
- [#702] GFX font reading fix
- Better obfuscated names handling
- [#539] for(each) in declaration fixes

## [3.0.0] - 2014-09-20
### Added
- Separated GUI (GPL) and library (now LGPL)
- Editing obfuscated identifiers via new paragraph(§) syntax
- Timeline View with preview and object hilighting
- Show GFX data in dump view
- [#650] New parameter to replace binarydata, images, sounds, scripts from commandline
- Dump view - selecting node
- [#680] Loading subSWFs from binaryTags now optional (button/context menu) to avoid unnecessary memory consumption

### Removed
- Removed deprecated commandline export formats (see --help)

### Fixed
- FileAttributes tag reading fix
- [#649] GFX reading fixed
- [#656] Search in memory - 64 bit processes fix
- [#661] scripts not showing
- [#664] expanding fillStyles in raw edit
- [#668] add missing character fix, text tags fix
- [#674] texts hilighting initialization fix
- [#675] AS1/2 and/or operator compilation
- [#632] Locking file after opening (cannot save, etc.)
- [#651] Unnecessary removing expression killed in unreachable part
- [#678] Windows batch file paths fixed
- [#672] Disabling transparency slider on RGB only selection
- [#684] Sound streams inside DefineSprites, soundstream handling

## [2.1.4] - 2014-08-23
### Added
- AS1/2: New method for deobfuscation (can be switched off in settings)
- AS1/2: Using eval/set on invalid identifiers, quotes in function names/parameters

### Fixed
- [#647] Skipping FileAttributesTag with Parallel speedup on
- [#648] Export from embedded SWF

## [2.1.3] - 2014-08-18
### Added
- Show "save" and "saveas" in application menu
- Saving data range in dump view
- Show actions, abcdata in dumpview (context menu on the tree node)
- [#612] show color in hex format

### Changed
- Faster dump info collecting (less memory)
- Allow selecting multiple files in open file dialog

### Fixed
- [#623] ffdec.sh UNIX file endings, executable
- [#624] search in embedded swf files
- [#632] AS1/2 Unnecessary GetVariable before NewObject
- [#627] filter swf not working
- LZMA saving
- Export pcode&hex from commandline
- [#640] text import fixed, ignore BOM

## [2.1.2] - 2014-07-20
### Added
- Dump view
- Context menu: Jump to character, raw edit all tags
- Catalan translation
- SWF header display

### Fixed
- [#595] AS3 direct edit - Getter/Setter generation - caused FlashPlayer crashes
- [#592] AS3 Multiname resolving in P-code causing different bytecode
- [#585] AS3 moving popped values to output
- [#578] Always on top fixed on search results
- [#501] GotoFrame2 fix
- [#616] Frames to PNG export
- Export context menu
- [#559] Bitmap export opacity
- [#401] Placeobject 3/4 fix
- [#593] Return object newline
- [#594] Setting for curly brace

## [2.1.1] - 2014-06-05
### Added
- [#302] Find declaration (Ctrl+click, Ctrl+B), Find usages (Ctrl+U) - Works only for exactly same multinames, not local registers
- AS1/2 direct edit - global functions improvements
- AS1/2 negate operator, unary minus operator
- Opening SWFs in BinaryData tags
- AS1: Old string operators support, and/or, <> operator (editation)
- Statusbar loading animation improved
- [#579] AS3 direct editation - removing old class/methods from ABC
- remove character without the dependencies (remove only the place/remove tags)
- Running on system with no home directory
- [#428] PDF export (as images only)
- Commandline FlashPaper to PDF export
- Select frames / Characters commandline options

### Changed
- [#337] quickfind visibility improved
- [#584] commandline script export - select whole packages (use .+ at the end of -selectas3class)

### Fixed
- [#576] AS1/2 direct editation: DefineFunction2 fix
- AS1/2 property fix
- AS1/2 typeof operator fix
- [#250] line spacing fix
- PlaceObject 3-4 className
- [#579] AS3 direct editation bugfixes - property resolving, integer values
- Morphshape canvas export fix
- Canvas export fix - closing path
- [#580] Rename invalid identifiers commandline fix
- [#510] JSyntaxPane find and replace dialog wrap around fix
- No more frame caching during export => memory saving (like [#583])
- [#586] DropShadow filter fix
- Canvas export colortransform fix

## [2.1.0 update 2] - 2014-05-08
### Added
- AS3 decompilation/editation: Vector initializers
- AS3 direct editation: more classes in one file

### Fixed
- [#574] DefineSprite editing fix
- Various AS3 direct editation fixes

## [2.1.0 update 1] - 2014-05-05
### Added
- Portugese-brasilian translation

### Changed
- HTML Canvas export improvements

### Fixed
- Various AS3 direct editation bugs, like [#570]

## [2.1.0] - 2014-05-01
### Added
- AS3 direct editation (Experimental!)
- Frames SVG Export
- Shape/MorphShape/Frames HTML 5 Canvas Export
- [#559] morphshapes as animated SVG
- [#563] Single file text export/import
- Font WOFF export
- Advanced settings dialog with tabs, config names, descriptions

### Fixed
- [#561], [#509], [#433] AS3 EmptyStackException fix - correct hasnext2 arguments
- Internal viewer: Filters fix

## [2.0.1 update 2] - 2014-04-05
### Fixed
- [#557] AS3 null namespace fix - p-code not working

## [2.0.1 update 1] - 2014-04-04
### Fixed
- [#556] Goto main class on startup fix
- [#557] Nullpointer fix (private namespaces)

## [2.0.1] - 2014-04-03
### Added
- Thumbnail view
- Font TTF export
- Exporting frames: PNGs, AVI, GIF (via Internal flash viewer)
- Expand all context menu
- Internal viewer: Button mouse move and click handling
- Playing sounds without flash player
- Internal viewer: Sounds on stage
- All sounds to WAV export
- Internal viewer: Showing texts, dynamic text border/fill
- [#504] Unicode characters in JSyntaxPane
- Internal viewer: showing object under cursor
- Folder icons
- Sound/Image format on command line.
- Removing placeobject tags
- Removing frames
- AS: "elseif" statements
- Code formatting: space before parenthesis

### Changed
- Single frames animated.

### Fixed
- [#529] limit the number of displayed binaryData bytes
- [#538] Interface are sometimes dynamic
- [#537] super is sometime preceded by a dot
- [#540] Saving SWF changes very large static uint values
- [#387] Frames preview bugged
- AS:loop mismatch fix on parallel speedup
- [#552] Some timeout exceptions
- [#494] Fixed nightly builds updates

## [2.0.0] - 2014-03-02
### Added
- Generic tag tree editor
- Timeline view (stub only)
- FLA export to CS5, CS5.5, CC format (previously only CS6 was supported)
- [#513] command line option to extract swf from binary file
- Configurable code formatting (Indentation + brace position)
- [#262] Export FLA: Font character ranges export
- Configurable checking for updates

### Changed
- Improved Internal Flash viewer - better shapes, morphshapes, DefineEditText tag, clipping, blend modes
- Improved commandline usage
- Automatic deobfuscation default value set to False (See News on webpages)
- Check for updates can be configured to inform about Nightly builds aswell

### Deprecated
- Some commandline options are now deprecated, see --help

### Fixed
- [#499] Cannot save via Proxy fixed
- [#504] font name reading fixed
- [#508] Support for OS without GUI
- [#305] Export FLA: empty sound layers
- [#312] Export FLA: Improved Shape/MorphShape fix
- [#503] Export FLA: Smoothing invalid shapes
- [#401] Invalid GFX tags in non GFX files
- [#304],[#306],[#507],[#424],[#425],[#478],[#485],[#517],[#518] Many direct AS1/2 editing issues
- [#361] FFDec icon is not visible on application start
- [#392] Video stream data fix
- [#516] AS3 P-code editor - Null name namespace handling

## [1.8.1 update 1] - 2014-02-02
### Fixed
- [#495] font embedding fix
- [#496] date format in new version dialog
- cosmetic changes

## [1.8.1] - 2014-01-30
### Added
- [#299] replace DefineBits images
- [#303] open folder with exported FLA
- [#324],[#346] SWC/zip/other binary file support
- [#371] detailed logging
- [#426] command line switch to rename identifiers
- [#457] clear recent opened files list
- [#458] save selected system font for swf fonts
- [#460] text editor: do not scroll to the end automatically
- [#462] font embedding dialog: show more sample characters
- [#463] global search in texts
- [#465] make font properties editable
- [#466] font preview

### Changed
- [#369] new SVG and preview image rendering
- [#390] refresh font list without reloading the application
- [#453] update texts aftert adding new character to a font tag
- [#459] remember text panel splitter position
- [#461] font panel gui redesigned

### Fixed
- [#451] dialog windows are not on the center of the screen
- [#454] Text syntax highlighting
- [#455],[#465] classic interface issues
- [#474] changeing language only available one time
- [#477] log window localization
- [#481] SVG export fix
- [#484] Oversized advance value after editing DefineText with DefineFont2 font
- [#493] missing search results

## [1.8.0 update 1] - 2013-12-27
### Added
- [#453] refresh (edit+save action) all texts button

### Fixed
- Flash panel and font panel fixed

## [1.8.0] - 2013-12-27
### Added
- [#350] Allow to open multiple SWFs
- [#365] Filter fake SWFs during memory search
- [#366] Allow to sort the result list in memory search window
- [#429] Auto rename invalid identifiers setting
- [#447] Non-ribbon interface

### Fixed
- [#354] Infinite decompilation fixed
- [#438] Case sensitive Command line arguments fixed
- [#436] Saving actionscript fixed
- [#446] Precedence issue fixed
- [#451] Dialogue window positions on a multi-monitor configuration fixed

## [1.7.4 update 1] - 2013-12-05
### Added
- [#426] Command line parameter for renaming invalid identifiers

### Fixed
- [#427] Exception on linux fixed
- [#405], [#420], [#421] Some decompilation issues fixed
- [#430] Configuration default value problem fixed
- [#397], [#431] Deobfuscation stucked sometimes problem fixed

## [1.7.4] - 2013-11-10
### Added
- [#169] hexedit method body bytes
- [#335] last opened files
- [#404] Exporting P-code and Hex + console parameters
- [#407] register name is configurable
- Advanced settings
- Cancellable decompiling, exporting and searching

### Fixed
- [#399], [#400] performance optimizations

## [1.7.3 update 2] - 2013-09-29
### Fixed
- [#398] AS3 p-code values with index 0 (null)

## [1.7.3 update 1] - 2013-09-28
### Added
- [#382] AS3: Adding new method

## [1.7.3] - 2013-09-27
### Added
- AS3: Multiname and namespace editing.
- [#382],[#396] AS3: Adding new trait (method/slot/const)
- AS3: Highlighting pair parenthesis/bracket
- AS3: Editing various new P-code parameters
- AS3: Highligting of trait names/types/parameters
- AS3: Global rename identifier for traits
- [#357] Playback controls for DefineSound
- [#391] AS3: Native methods mark
- [#395] Support for GFx ScaleForm SWFs (with fonts editing)
- Displaying fonts in internal viewer
- [#334], [#395] New Embed font dialog - selecting character ranges with preview
- Replacing characters in font (Yes/No to all dialog)

### Changed
- AS3: New p-code syntax inspired by RABCDasm
- AS3: Editing whole trait in one textarea
- AS3: Removed messages about adding new constants
- AS3: Modified colors in editor
- [#301] Clearing error log causes icon to reset

## [1.7.2 update 2] - 2013-09-13
### Changed
- Updated translations

### Fixed
- [#383] Firefox browser cache handling
- [#386] SWF resizing

## [1.7.2 update 1] - 2013-09-11
### Changed
- updated translations

### Fixed
- [#383] Fixed cache loading when Firefox not used

## [1.7.2] - 2013-09-11
### Added
- [#357] Sounds Preview (Windows only)
- Movies preview (Windows only)
- Whole SWF display
- Preview controls (Play,Pause,Stop)
- Search SWFs in browsers cache (Firefox, Chrome)
- [#367] Memory search: Save selected files to disk
- Portugese translation

### Changed
- [#380] Faster displaying DefineBitsLossless(2) images

### Fixed
- [#292] Background color for Fonts preview fixed
- [#375] Replacing DefineBitsLossless image tag
- [#378] Refreshing language of JSyntaxPane
- MORPHGRADIENT reading fix

## [1.7.1] - 2013-08-25
### Added
- Loading SWFs from other processes memory (Windows only, sorry)
- [#325] Spanish translation
- [#210] Ukrainian and Dutch translation
- [#355] Chinese translation
- [#292] Change background color in SWF preview
- [#301] Clear errors log button
- [#313] Command line parameter for ignore all errors
- [#330] Protection agains adding same characters
- [#332] AS1/2 Showing elapsed times during commandline export
- [#344] Reload opened SWF
- Decompilation timeouts

### Fixed
- [#295] Export FLA: wrong font
- [#297] Too bright titlebar button colors
- [#307] Export FLA: fixed empty textfields
- [#309] Export FLA: static text letter spacing detection
- [#310] Export FLA: Strokes
- [#311] Export FLA: BitmapFill
- [#327] AS1/2 Disassembly error stop application
- [#328] Fixed replacing images in DefineBitsJPEGX
- [#333] AS1/2 action reading
- [#336] Graph window is too small
- [#337] Quick search panel barely visible in same cases
- [#338] Expand/collapse icon in errorlog

## [1.7.0 update 1] - 2013-08-11
### Added
- [#315] German translation (partial)

### Fixed
- [#123] Better context menu integration
- [#243],[#326] Better deobfuscation
- [#287] Typo in parallelSpeedUp parameter
- [#290],[#291] improved select language dialog
- [#294] minor GUI fixes
- [#298] Progressbar positition issues
- [#296] better export directory remembering
- [#314] Better deobfuscating filenames
- [#316] Readonly editor panes accepted Ctrl+Z/Y
- [#318] Export FLA: Shapes export fix
- [#319] AS3: Improved try..catch..finally decompilation
- [#323] AS3: Fixed default switch part

## [1.7.0] - 2013-08-03
### Added
- Listing contributors on about page
- [#223] AS2: Detecting uninitialized class fields
- [#250] Export FLA: Detecting static fields margin and spacing
- [#261] Export FLA: AS1/2 Frame scripts on first layer
- [#269] Commandline parameters for switching configuration
- [#274] AS3 Displaying elapsed time during commandline export
- [#275] AS3 Removing returnvoid as last statement

### Changed
- New GUI based on Substance look and feel
- Menu changed to ribbon panel
- New round icon
- [#258] AS1/2: Improved chained assignments
- [#267] Starting program without choosing a file
- [#286] Saving to temp file first

### Fixed
- [#123] Improved context menu integration on Windows
- [#233] Globally rename identifier deselects item in the tree
- [#235] Export FLA: Dynamic text fields coordinates
- [#243],[#263],[#264],[#265],[#266],[#281] Improved deobfuscation
- [#251] Export FLA: Fixed filter strength rounding
- [#257] Export FLA: Text field color and size issues
- [#259] Fixed images alpha
- [#260] Export FLA: Labels position
- [#268] AS1/2 Function parameter shown as register instead loc
- [#272] AS3 Class initializer editation fix
- [#276] Fixed anonymous/inline functions handling
- [#220] Improved editing fonts / texts
- [#284],... other small fixes

## [1.6.7] - 2013-07-20
### Added
- [#220] Selection of font to import characters from
- [#232] Automatically add .swf extension in saveas dialog
- [#253] Abort/Retry/Ignore dialog on errors with file saving

### Changed
- Improved translations

### Fixed
- [#137],[#242], [#243], [#244] AS1/2/3 fixed deobfuscation
- [#203] AS1/2 improved direct editing
- [#220] Adding characters to font fix
- [#225] AS1/2 object literal without name quotes
- [#236] AS1/2 Rename invalid identifiers issues
- [#245] AS3 Double space around "as" keyword
- [#247] AS3 Scrolling to main class at startup
- [#248] Memory issues (slowdown)
- [#254] Expressions as commands
- [#255] Windows 7 loading issues
- [#256] AS3 Object literal in return clause
- SWF text parsing (new lines)
- Labels size by locales

## [1.6.6 update 2] - 2013-07-16
### Fixed
- [#241] Program could not be started

## [1.6.6 update 1] - 2013-07-16
### Changed
- Better localization support

### Fixed
- [#238],[#239],[#240] Fixed deobfuscation related problems
- [#237] Parentheses in AS1/2 add,subtract

## [1.6.6] - 2013-07-16
### Added
- [#217] Russian translation (focus)
- [#219] Hungarian translation (honfika)
- [#224] Swedish translation (Capasha)
- [#220] Adding characters to Fonts, displaying font info
- [#121] Search progress indication
- Error log

### Changed
- [#203] Improved direct editing of AS1/2
- [#207] Update SWF preview after switching external/internal flash player

### Fixed
- [#151] Memory caching
- [#171] Skipping invalid AS3 code - newobject, newarray
- [#206] AS3 switch problem
- [#208] Renaming anonymous functions
- [#209],[#229] FLA export texts positions
- [#213],[#221] other decompilation issues
- [#225] AS object literal broken with ternar operator
- [#226] onClip indentation in FLA export
- [#227] gotoAndStop wrong frame index
- [#230] FLA export missing strokes
- Shapes viewer - missing strokes

## [1.6.5 update 1] - 2013-07-09
### Fixed
- [#151] Fixed caching in memory
- [#172] AS1/2 constant detection fix
- [#174] Renaming SymbolClass fix
- [#175],[#212] Fixed create directory issues on export
- [#185],[#186] on-clip actions indentation
- [#197] AS1/2 Missing storeregister before switch
- [#216] AS2 Fixed field order
- [#213] AS2 Fixed var fields quotes, switch nullpointer

## [1.6.5] - 2013-07-08
### Added
- Multilanguage support (currently English and Czech)
- [#151] Option for caching in memory instead of files
- [#168] Export selection in tree context menu
- [#176] option to show main class on startup
- [#177] saving window maximized state
- [#202] Removing tags other than DefineSprite

### Changed
- [#173],[#190] Better renaming
- [#129], [#153] Better deobfuscation
- [#180] better error handling
- [#185],[#186] better displaying and exporting onclip actions

### Fixed
- [#123] Better context menu integration
- [#136] FLA export - text sizes
- [#137],[#179] foreach issues (hasNext)
- [#144] Plain text export - empty line fix
- [#144] Not displaying texts
- [#164] DefineMorphShape issues
- [#167] Sprite tag appearing twice in export filename
- [#170] AS3 Try in loop
- [#172] loop detection fix
- [#175] use empty namespace
- [#178] AS subtract with negate
- [#181] AS3 missing quotes in object field
- [#182] missing namespace imports
- [#183] wrong stage size
- [#184] wrong video link
- [#189] Fixed three dots in tree
- [#191] Focalgradient fill fix
- [#195] AS2 issues
- [#196],[#197] switch issues
- [#198] DefineFont2 empty check
- [#200] DefineBitsLossLess fix
- [#201] Nonworking main window in Linux/MacOS (due toAssociation)

## [1.6.4 update 1] - 2013-06-30
### Fixed
- [#166] For loops detection
- [#165] AS3:direct lookupswitch support

## [1.6.4] - 2013-06-30
### Added
- [#63] Globally rename identifier
- [#67] Deobfuscation - rename identifiers according to type
- [#117] Drag & Drop SWF file to main window opens it
- [#123] Context menu integration on Windows
- [#127] Drag & Drop items from tree outside of application
- [#134] AS3: Find document class
- [#144] New lines in plain text export
- [#155] Remembering window size + splitbar positions between runs

### Changed
- [#142] Using exportassets tag for tag names
- [#146] Display AS2 classes as tree of packages
- Better loop detection

### Fixed
- [#129] AS1/2: not refreshing decompiled after rename
- [#130] Renaming SymbolClass identifiers too
- [#132] Renaming identifiers renamed strings
- [#136] Invalid text positions in FLA export
- [#145] Unicode support
- [#147] Escape filenames during obfuscated AS3 export
- [#148] Better package vs classname handling
- [#152] Empty if branches not inverted
- [#156] Better search handling (not freezing)
- [#157] AS3: Try statements in loops
- [#158] Graph repaint problem
- [#159] AS3: Improper rest parameter handling
- [#160] Commandline binaryData export
- [#162] DefineBitsJPEG2 image replacing
- [#163] Closing SWF file after loading
- other minor fixes

## [1.6.3 update 2] - 2013-06-21
### Changed
- [#149] Ifs with empty onTrue branches now inverted

### Fixed
- [#150] Long line restriction removed

## [1.6.3 update 1] - 2013-06-21
### Fixed
- Memory limit decreased - FFDec was not working on 32 bit JVM.

## [1.6.3] - 2013-06-20
### Changed
- Parallel SpeedUp can be disabled in menu
- Better loop detection

### Fixed
- [#119] Replacements file not found issue
- [#101] AS1/2 postincrement fix
- [#114],[#116],[#135],[#141] Fixed loop detection
- [#102] Fixed loop highlighting in export
- [#124] Flash player file path detection
- [#128] Improved imports
- [#135] CommentItem fix
- [#129],[#131] Better deobfuscation
- [#104] AS3 inc/dec local deobfuscation fix
- [#113],[#133],[#140] Memory limit increased

## [1.6.2] - 2013-06-09
### Added
- New loop detection algorithm

### Changed
- [#108] - Faster loading and decompiling (Parallelism)
- Improved Internal flash viewer - shapes and morphshapes

### Fixed
- Ternar operator fix
- [#102] Fixed Shapes to FLA export
- AS1/2 class detection fix
- [#105],[#104],[#101] fixed via new loop detection

## [1.6.1] - 2013-06-03
### Added
- Internal Flash Viewer - preview of flash parts (shapes,sprites,frames) without need of Flash Player. (Used on nonWindows platforms by default)
- [#109], [#106], [#107] some code improvements

### Changed
- Application needs Java 1.7 to run

### Removed
- Support for Java before 1.7

### Fixed
- [#102], [#110] AS3: Class highlight fix
- [#103] AS3:Fixed setslot handling
- [#104] AS3:Inc/Declocal nullpointer fix
- [#104] Multiple conditions in loop fix
- [#111] AS3:Object literal truncates line
- [#105] Better do while..break handling
- loop fixes

## [1.6.0 update 1] - 2013-05-25
### Added
- better FLA export

### Fixed
- Many FLA export related bugs (like [#96])
- [#98] Empty initializers do not cause empty lines now
- [#99] small logging issues
- [#100] large obfuscated code support

## [1.6.0] - 2013-05-20
### Added
- Export to FLA (Experimental BETA!)
- [#85] Search text in all ActionScripts
- SWF 11 support

### Fixed
- [#79] ActionStartDrag constraint fix
- [#92] Inversed GreaterThan/LessThan
- [#93] AS1/2 fixed switch detection
- [#94] AS1/2 ActionTry - register cast fix
- [#95] Better script end handling

## [1.5.2] - 2013-05-05
### Added
- Improved automatic update system (changes log).
- Handling script traits as separate objects.
- [#86] open/save file dialog now accepts absolute paths in quotes

### Fixed
- [#87] Not displaying image changes in DefineBitsLossLess1 & 2
- [#88] Fixed graph building
- [#89] AS3: bracket in property name lead to missing dot
- [#82] printgraph issue

## [1.5.1 update 1] - 2013-05-04
### Added
- Exporting texts via commandline
- Exporting all via commandline

### Fixed
- DefineText2 color parameter
- AS3 GetSlot,SetSlot
- [#78],[#81],[#82],[#84]   Fixed deobfuscation, exceptions during printgraph,...
- [#83] Fixed images transparency (zlib fix)
- Fixed graphparts with only jump in it (obfuscators)
- MORPHGRADIENT FIX
- Trasparency in DefineBitsJPEG3 and 4
- Displaying shapes,morphshapes and sprites with bitmaps

## [1.5.1] - 2013-05-01
### Added
- Support for larger switches (10+cases)
- Editing text tags
- [#65] Exporting text tags
- Removing sprites
- Replacing images

### Fixed
- DefineMorphShape2 fix
- [#79] - AS1/2 class detection fix, wrong printgraph fix
- [#78] - script trait slots fix

## [1.5.0 update 1] - 2013-04-21
### Fixed
- Automatic deobfuscation config defaulted to Off for AS1/2.

## [1.5.0] - 2013-04-20
### Added
- Direct editing of ActionScript 1/2 code (Beta)
- AS1/2: ifFrameLoaded support
- Automatic deobfuscation can be disabled in the menu
- [#48] - Decompile only specified class (commandline option)
- [#53] - AS3: Displaying multiname indices in trait detail, displaying method indices
- [#66] - Decompressing LZMA via commandline
- [#68] - Exporting DefineBinaryData tags, assigning class names to characters (SymbolClass tag)
- [#69] - DoABC vs DoABCDefine tags decompilation
- [#75] - Comma separator in while/do..while conditions, better if..return handling
- AS1/2: parsing NaN,Infinity value (Fix for [#73])

### Changed
- New icons (edit/save/cancel and main menu)

### Fixed
- [#62] - Errors on not defined character tags (import tag)
- [#72] - First ternary operator expression was always true
- Fixed many deobfuscation related bugs

## [1.4.3 update 2] - 2013-04-10
### Fixed
- [#64] - AS1/2 Resolving registers in ActionDefineFunction2 (super,this,...and parameters shown as registerxx)
- Try to fix lib/FlashPlayer.exe issue

## [1.4.3 update 1] - 2013-04-06
### Fixed
- [#38] - Indentation in const/var initializers, missing semicolon
- [#56] - Test output left in last release
- [#57] - Unknown instructions now do not log an exception (obfuscators do this)
- [#58] - Index out of bounds exception fix on methodinfo indices in imports detection.
- AS3 loops fix
- While true fix

## [1.4.3] - 2013-04-04
### Added
- AS1/2 Better deobfuscation

### Fixed
- [#45] - Unicode characters fix
- [#50] - AS1/2 Function body deobfuscation fix
- [#51] - Displaying java class names instead of expressions
- [#52] - AS1/2 Better constantpool detection (deobfuscation)
- [#38] - AS3 indentation in initialized const/var value for newobject
- Fixed ImportAssets2 tag id

## [1.4.2 update 1] - 2013-03-25
### Fixed
- [#47] - wrong AS3 deobfuscation
- AS3 deobfuscation issues
- AS3 switch

## [1.4.2] - 2013-03-24
### Added
- [#42] - Displaying code as hex
- AS1/2: Renaming identifiers (deobfuscation)
- AS1/2,AS3: Better deobfuscation
- Storing configuration to user home
- Installer for Windows systems

### Changed
- Graph button changed to icon.

### Fixed
- [#39] - AS1/2 NewMethod..Pop fix
- [#40] - No logging + For..in..return decompilation
- [#44] - DefineFont2 fix
- [#36] - Multiname with invalid index
- [#43] - Ternary operator and more
- [#46] - Ifs with empty branches got ignored
- [#3] - Ignoring unknown opcodes
- Logging exceptions during export

## [1.4.1] - 2013-03-10
### Added
- Exporting sounds
- Better AS1/2 deobfuscation (disassembly & decompilation)

### Fixed
- Exporting only first 1000 frames of the movie
- Decompiled code was not refreshed on AS1/2 changes
- Application no longer creates empty directories on export

## [1.4.0 update 1] - 2013-03-04
### Fixed
- [#37] - AS3: Reversed loop conditions

## [1.4.0] - 2013-03-03
### Added
- AS3: ignoring return void at the end of methods
- New icons - Silk icons
- AS3: Traits list sort button
- Better Graph display
- Frames view
- Exporting of movies (No audio)
- Some AS3 related Tests
- Homepage & Donate link in the menu

### Changed
- Tree view instead of tabs
- AS1/2 and AS3 now share same decompiling method.

### Fixed
- [#34] - Reversed loop conditions
- [#35] - Fixed unicode strings (Japanese)

## [1.3.1] - 2013-02-23
### Changed
- Flash player no longer uses SWT library

### Fixed
- [#32] - AS2: Action255 bug
- [#31] - Erorrneous tags are now ignored
- DefineBitsLossLess 1&2 on 8bit colormapped images

## [1.3.0] - 2013-02-17
### Added
- Decompilation is more resistant to obfuscation
- Shapes SVG export
- AS2: Decompiling classes & interfaces
- Click&go feature - clicking actionscript source displays appropriate P-code instruction and vice-versa (both AS1/2 and AS3)
- AS3: Deobfuscation menu
- Graph button for displaying code flow Graph

### Changed
- Complete new decompiling method in both AS1/2 and AS3
- Application renamed from "JP ActionScript Decompiler" to "JPEXS Free Flash Decompiler".
- To edit source, Edit button must be pressed first (Due to click&go feature)

### Fixed
- AS3: Method info editor fixed
- Edittext & Button displaying

## [1.2.0 update 1] - 2013-01-19
## Fixed
- [#27] Problems on loading DefineSceneAndFrameLabel
- CSMTextSettings tag writing fix

## [1.2.0] - 2013-01-19
### Added
- Displaying various SWF objects (shapes, sprites,...) with flash player library (Windows only, sorry).
- Images display and export
- AS2: Exporting selection
- Progressbar during loading

### Changed
- One merged window for AS1/2 and 3.
- Updated icons

### Fixed
- AS3: xml attrib, switch in anonymous function (in AS2 too)

## [1.1.0] - 2013-01-02
### Added
- Checking for updates
- AS2: Exporting
- AS3: Decompiling whole scripts instead of just classes
- AS3: Exporting selected scripts
- AS3: Script search bar
- AS3: List of DoABCTags now has default "- all -" item
- AS3: Better imports, use namespaces
- AS3: XML related instructions
- AS3: Anonymous functions with names
- AS3: Better initialization of const values
- Logging exceptions to log.txt file

### Fixed
- AS3: set_local..get_local, dup, chained assignments, highlighting, callsupervoid, typenames, with statement, loops

## [1.0.1] - 2012-12-26
### Added
- AS3: Runtime namespace resolving
- AS3: Arguments variable
- AS3: Better recognizing Pre/Post Increments/Decrements
- AS3: Better declarations

### Fixed
- AS3: Fixed static variables

## [1.0.0] - 2012-12-24
### Added
- Support for LZMA compressed files
- AS3: Detecting local register types for declaration.
- AS3: Displaying inline functions
- AS3: Last save/open dir is remembered
- AS3: Better usage detection for multinames
- AS3: Commandline arguments for exporting
- AS3: Better chained assignments
- AS2: FSCommand2 instruction support
- Proxy: Mimetype application/octet-stream added
- Added executable for Windows users.

### Changed
- AS3: GUI - Constants tab moved to the top
- AS3: Deobfuscation is now optional, can be accessed via menu

### Fixed
- AS3: rest parameter, for..in, fail on large classes (due to sub limiter)
- Other minor fixes

## [beta 1] - 2011-07-30
### Added
- AS3: Automatic computing method body parameters (EXPERIMENTAL)
- AS3: Editing return type of methods
- AS3: Editing type and default value for variables/constants (Slot/Const traits)
- AS1/2: Few enhancements
- About dialog

### Changed
- Gui: Updated Icons

### Fixed
- AS 1/2: Fixed large bug causing Ifs to not decompile properly
- Proxy: Some minor fixes

## [alpha 10] - 2011-07-13
### Added
- AS3:Highlighting actual line
- AS3:Completing instruction names via Ctrl+Space
- AS3:Editing method parameters, method body parameters via tab panel
- AS3:ByteCode minor_version 17 supported - decimal datatypes
- AS3:Local variables and method parameters take name from debug information if present
- AS3:Automatic renaming of classes/methods when obfuscated names
- AS3:Better error messages (When cannot decompile obfuscated code)

### Fixed
- AS3:Fixed Vector datatypes (TypeName multiname, applytype instruction)
- AS3:Hilighting fixes
- AS3:Fixed decrement/increment statements decompilation
- AS3:Decompiler now adds variable declarations on the beginning of decompiled method
- AS3:Try/catch statements fixed when debug information present
- AS3:Fixed for each statements
- AS3:Other minor fixes

## [alpha 9] - 2011-07-02
### Added
- AS3: Added disassembling of some new types of instructions
- AS3: Exporting source as PCode

### Fixed
- AS3: Many other bugfixes...

## [alpha 8] - 2010-09-19
### Added
- AS3: Editing exceptions
- AS3: Finding usage of multinames from constant table

### Changed
- AS1/2: Better GUI
- AS1/2: Better decompiling of Ifs, For..in

## [alpha 7] - 2010-09-04
### Added
- Initial public release

[Unreleased]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version11.0.0...dev
[11.0.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version10.0.0...version11.0.0
[10.0.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version9.0.0...version10.0.0
[9.0.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version8.0.1...version9.0.0
[8.0.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version8.0.0...version8.0.1
[8.0.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version7.1.2...version8.0.0
[7.1.2]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version7.1.1...version7.1.2
[7.1.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version7.1.0...version7.1.1
[7.1.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version7.0.1...version7.1.0
[7.0.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version7.0.0...version7.0.1
[7.0.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version6.1.1...version7.0.0
[6.1.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version6.1.0...version6.1.1
[6.1.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version6.0.2...version6.1.0
[6.0.2]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version6.0.1...version6.0.2
[6.0.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version6.0.0...version6.0.1
[6.0.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version5.3.0...version6.0.0
[5.3.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version5.2.0...version5.3.0
[5.2.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version5.1.0...version5.2.0
[5.1.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version5.0.2...version5.1.0
[5.0.2]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version5.0.1...version5.0.2
[5.0.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version5.0.0...version5.0.1
[5.0.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version4.1.1...version5.0.0
[4.1.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version4.1.0...version4.1.1
[4.1.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version4.0.5...version4.1.0
[4.0.5]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version4.0.4...version4.0.5
[4.0.4]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version4.0.3...version4.0.4
[4.0.3]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version4.0.2...version4.0.3
[4.0.2]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version4.0.1...version4.0.2
[4.0.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version4.0.0...version4.0.1
[4.0.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version3.0.0...version4.0.0
[3.0.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version2.1.4...version3.0.0
[2.1.4]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version2.1.3...version2.1.4
[2.1.3]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version2.1.2...version2.1.3
[2.1.2]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version2.1.1...version2.1.2
[2.1.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version2.1.0u2...version2.1.1
[2.1.0 update 2]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version2.1.0u1...version2.1.0u2
[2.1.0 update 1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version2.1.0...version2.1.0u1
[2.1.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version2.0.1u2...version2.1.0
[2.0.1 update 2]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version2.0.1u1...version2.0.1u2
[2.0.1 update 1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version2.0.1...version2.0.1u1
[2.0.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version2.0.0...version2.0.1
[2.0.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.8.1u1...version2.0.0
[1.8.1 update 1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.8.1...version1.8.1u1
[1.8.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.8.0u1...version1.8.1
[1.8.0 update 1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.8.0...version1.8.0u1
[1.8.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.7.4u1...version1.8.0
[1.7.4 update 1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.7.4...version1.7.4u1
[1.7.4]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.7.3u2...version1.7.4
[1.7.3 update 2]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.7.3u1...version1.7.3u2
[1.7.3 update 1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.7.3...version1.7.3u1
[1.7.3]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.7.2u2...version1.7.3
[1.7.2 update 2]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.7.2u1...version1.7.2u2
[1.7.2 update 1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.7.2...version1.7.2u1
[1.7.2]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.7.1...version1.7.2
[1.7.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.7.0u1...version1.7.1
[1.7.0 update 1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.7.0...version1.7.0u1
[1.7.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.6.7...version1.7.0
[1.6.7]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.6.6u2...version1.6.7
[1.6.6 update 2]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.6.6u1...version1.6.6u2
[1.6.6 update 1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.6.6...version1.6.6u1
[1.6.6]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.6.5u1...version1.6.6
[1.6.5 update 1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.6.5...version1.6.5u1
[1.6.5]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.6.4u1...version1.6.5
[1.6.4 update 1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.6.4...version1.6.4u1
[1.6.4]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.6.3u2...version1.6.4
[1.6.3 update 2]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.6.3u1...version1.6.3u2
[1.6.3 update 1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.6.3...version1.6.3u1
[1.6.3]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.6.2...version1.6.3
[1.6.2]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.6.1...version1.6.2
[1.6.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.6.0u1...version1.6.1
[1.6.0 update 1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.6.0...version1.6.0u1
[1.6.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.5.2...version1.6.0
[1.5.2]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.5.1u1...version1.5.2
[1.5.1 update 1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.5.1...version1.5.1u1
[1.5.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.5.0u1...version1.5.1
[1.5.0 update 1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.5.0...version1.5.0u1
[1.5.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.4.3u2...version1.5.0
[1.4.3 update 2]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.4.3u1...version1.4.3u2
[1.4.3 update 1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.4.3...version1.4.3u1
[1.4.3]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.4.2u1...version1.4.3
[1.4.2 update 1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.4.2...version1.4.2u1
[1.4.2]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.4.1...version1.4.2
[1.4.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.4.0u1...version1.4.1
[1.4.0 update 1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.4.0...version1.4.0u1
[1.4.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.3.1...version1.4.0
[1.3.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.3.0...version1.3.1
[1.3.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.2.0u1...version1.3.0
[1.2.0 update 1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.2.0...version1.2.0u1
[1.2.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.1.0...version1.2.0
[1.1.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.0.1...version1.1.0
[1.0.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version1.0.0...version1.0.1
[1.0.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/beta1...version1.0.0
[beta 1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/alpha10...beta1
[alpha 10]: https://github.com/jindrapetrik/jpexs-decompiler/compare/alpha9...alpha10
[alpha 9]: https://github.com/jindrapetrik/jpexs-decompiler/compare/alpha8...alpha9
[alpha 8]: https://github.com/jindrapetrik/jpexs-decompiler/compare/alpha7...alpha8
[alpha 7]: https://github.com/jindrapetrik/jpexs-decompiler/releases/tag/alpha7
[#1156]: https://www.free-decompiler.com/flash/issues/1156
[#1171]: https://www.free-decompiler.com/flash/issues/1171
[#1170]: https://www.free-decompiler.com/flash/issues/1170
[#1241]: https://www.free-decompiler.com/flash/issues/1241
[#1162]: https://www.free-decompiler.com/flash/issues/1162
[#1199]: https://www.free-decompiler.com/flash/issues/1199
[#1151]: https://www.free-decompiler.com/flash/issues/1151
[#1128]: https://www.free-decompiler.com/flash/issues/1128
[#1163]: https://www.free-decompiler.com/flash/issues/1163
[#1172]: https://www.free-decompiler.com/flash/issues/1172
[#1174]: https://www.free-decompiler.com/flash/issues/1174
[#1183]: https://www.free-decompiler.com/flash/issues/1183
[#1193]: https://www.free-decompiler.com/flash/issues/1193
[#1200]: https://www.free-decompiler.com/flash/issues/1200
[#1198]: https://www.free-decompiler.com/flash/issues/1198
[#1205]: https://www.free-decompiler.com/flash/issues/1205
[#1194]: https://www.free-decompiler.com/flash/issues/1194
[#1210]: https://www.free-decompiler.com/flash/issues/1210
[#1217]: https://www.free-decompiler.com/flash/issues/1217
[#1244]: https://www.free-decompiler.com/flash/issues/1244
[#1247]: https://www.free-decompiler.com/flash/issues/1247
[#1236]: https://www.free-decompiler.com/flash/issues/1236
[#1251]: https://www.free-decompiler.com/flash/issues/1251
[#1265]: https://www.free-decompiler.com/flash/issues/1265
[#1268]: https://www.free-decompiler.com/flash/issues/1268
[#1161]: https://www.free-decompiler.com/flash/issues/1161
[#1118]: https://www.free-decompiler.com/flash/issues/1118
[#409]: https://www.free-decompiler.com/flash/issues/409
[#1132]: https://www.free-decompiler.com/flash/issues/1132
[#1134]: https://www.free-decompiler.com/flash/issues/1134
[#1121]: https://www.free-decompiler.com/flash/issues/1121
[#1052]: https://www.free-decompiler.com/flash/issues/1052
[#1145]: https://www.free-decompiler.com/flash/issues/1145
[#758]: https://www.free-decompiler.com/flash/issues/758
[#1096]: https://www.free-decompiler.com/flash/issues/1096
[#1104]: https://www.free-decompiler.com/flash/issues/1104
[#1107]: https://www.free-decompiler.com/flash/issues/1107
[#1106]: https://www.free-decompiler.com/flash/issues/1106
[#1113]: https://www.free-decompiler.com/flash/issues/1113
[#1075]: https://www.free-decompiler.com/flash/issues/1075
[#1127]: https://www.free-decompiler.com/flash/issues/1127
[#1103]: https://www.free-decompiler.com/flash/issues/1103
[#1133]: https://www.free-decompiler.com/flash/issues/1133
[#1135]: https://www.free-decompiler.com/flash/issues/1135
[#1138]: https://www.free-decompiler.com/flash/issues/1138
[#1139]: https://www.free-decompiler.com/flash/issues/1139
[#930]: https://www.free-decompiler.com/flash/issues/930
[#1137]: https://www.free-decompiler.com/flash/issues/1137
[#1144]: https://www.free-decompiler.com/flash/issues/1144
[#1147]: https://www.free-decompiler.com/flash/issues/1147
[#1148]: https://www.free-decompiler.com/flash/issues/1148
[#1152]: https://www.free-decompiler.com/flash/issues/1152
[#1154]: https://www.free-decompiler.com/flash/issues/1154
[#116]: https://www.free-decompiler.com/flash/issues/116
[#1070]: https://www.free-decompiler.com/flash/issues/1070
[#1098]: https://www.free-decompiler.com/flash/issues/1098
[#1033]: https://www.free-decompiler.com/flash/issues/1033
[#1083]: https://www.free-decompiler.com/flash/issues/1083
[#1091]: https://www.free-decompiler.com/flash/issues/1091
[#1076]: https://www.free-decompiler.com/flash/issues/1076
[#1068]: https://www.free-decompiler.com/flash/issues/1068
[#1063]: https://www.free-decompiler.com/flash/issues/1063
[#1019]: https://www.free-decompiler.com/flash/issues/1019
[#1016]: https://www.free-decompiler.com/flash/issues/1016
[#1010]: https://www.free-decompiler.com/flash/issues/1010
[#1008]: https://www.free-decompiler.com/flash/issues/1008
[#1004]: https://www.free-decompiler.com/flash/issues/1004
[#933]: https://www.free-decompiler.com/flash/issues/933
[#1062]: https://www.free-decompiler.com/flash/issues/1062
[#418]: https://www.free-decompiler.com/flash/issues/418
[#1047]: https://www.free-decompiler.com/flash/issues/1047
[#812]: https://www.free-decompiler.com/flash/issues/812
[#1056]: https://www.free-decompiler.com/flash/issues/1056
[#1057]: https://www.free-decompiler.com/flash/issues/1057
[#991]: https://www.free-decompiler.com/flash/issues/991
[#689]: https://www.free-decompiler.com/flash/issues/689
[#1060]: https://www.free-decompiler.com/flash/issues/1060
[#1037]: https://www.free-decompiler.com/flash/issues/1037
[#489]: https://www.free-decompiler.com/flash/issues/489
[#1007]: https://www.free-decompiler.com/flash/issues/1007
[#1044]: https://www.free-decompiler.com/flash/issues/1044
[#947]: https://www.free-decompiler.com/flash/issues/947
[#953]: https://www.free-decompiler.com/flash/issues/953
[#954]: https://www.free-decompiler.com/flash/issues/954
[#950]: https://www.free-decompiler.com/flash/issues/950
[#945]: https://www.free-decompiler.com/flash/issues/945
[#957]: https://www.free-decompiler.com/flash/issues/957
[#956]: https://www.free-decompiler.com/flash/issues/956
[#968]: https://www.free-decompiler.com/flash/issues/968
[#978]: https://www.free-decompiler.com/flash/issues/978
[#955]: https://www.free-decompiler.com/flash/issues/955
[#966]: https://www.free-decompiler.com/flash/issues/966
[#999]: https://www.free-decompiler.com/flash/issues/999
[#1000]: https://www.free-decompiler.com/flash/issues/1000
[#1017]: https://www.free-decompiler.com/flash/issues/1017
[#1030]: https://www.free-decompiler.com/flash/issues/1030
[#949]: https://www.free-decompiler.com/flash/issues/949
[#944]: https://www.free-decompiler.com/flash/issues/944
[#939]: https://www.free-decompiler.com/flash/issues/939
[#942]: https://www.free-decompiler.com/flash/issues/942
[#952]: https://www.free-decompiler.com/flash/issues/952
[#858]: https://www.free-decompiler.com/flash/issues/858
[#905]: https://www.free-decompiler.com/flash/issues/905
[#920]: https://www.free-decompiler.com/flash/issues/920
[#921]: https://www.free-decompiler.com/flash/issues/921
[#924]: https://www.free-decompiler.com/flash/issues/924
[#895]: https://www.free-decompiler.com/flash/issues/895
[#884]: https://www.free-decompiler.com/flash/issues/884
[#899]: https://www.free-decompiler.com/flash/issues/899
[#903]: https://www.free-decompiler.com/flash/issues/903
[#855]: https://www.free-decompiler.com/flash/issues/855
[#850]: https://www.free-decompiler.com/flash/issues/850
[#832]: https://www.free-decompiler.com/flash/issues/832
[#904]: https://www.free-decompiler.com/flash/issues/904
[#910]: https://www.free-decompiler.com/flash/issues/910
[#922]: https://www.free-decompiler.com/flash/issues/922
[#916]: https://www.free-decompiler.com/flash/issues/916
[#938]: https://www.free-decompiler.com/flash/issues/938
[#897]: https://www.free-decompiler.com/flash/issues/897
[#470]: https://www.free-decompiler.com/flash/issues/470
[#877]: https://www.free-decompiler.com/flash/issues/877
[#878]: https://www.free-decompiler.com/flash/issues/878
[#845]: https://www.free-decompiler.com/flash/issues/845
[#883]: https://www.free-decompiler.com/flash/issues/883
[#882]: https://www.free-decompiler.com/flash/issues/882
[#760]: https://www.free-decompiler.com/flash/issues/760
[#887]: https://www.free-decompiler.com/flash/issues/887
[#842]: https://www.free-decompiler.com/flash/issues/842
[#841]: https://www.free-decompiler.com/flash/issues/841
[#772]: https://www.free-decompiler.com/flash/issues/772
[#762]: https://www.free-decompiler.com/flash/issues/762
[#862]: https://www.free-decompiler.com/flash/issues/862
[#865]: https://www.free-decompiler.com/flash/issues/865
[#613]: https://www.free-decompiler.com/flash/issues/613
[#868]: https://www.free-decompiler.com/flash/issues/868
[#713]: https://www.free-decompiler.com/flash/issues/713
[#807]: https://www.free-decompiler.com/flash/issues/807
[#728]: https://www.free-decompiler.com/flash/issues/728
[#857]: https://www.free-decompiler.com/flash/issues/857
[#860]: https://www.free-decompiler.com/flash/issues/860
[#824]: https://www.free-decompiler.com/flash/issues/824
[#809]: https://www.free-decompiler.com/flash/issues/809
[#805]: https://www.free-decompiler.com/flash/issues/805
[#825]: https://www.free-decompiler.com/flash/issues/825
[#737]: https://www.free-decompiler.com/flash/issues/737
[#350]: https://www.free-decompiler.com/flash/issues/350
[#814]: https://www.free-decompiler.com/flash/issues/814
[#816]: https://www.free-decompiler.com/flash/issues/816
[#835]: https://www.free-decompiler.com/flash/issues/835
[#836]: https://www.free-decompiler.com/flash/issues/836
[#848]: https://www.free-decompiler.com/flash/issues/848
[#817]: https://www.free-decompiler.com/flash/issues/817
[#849]: https://www.free-decompiler.com/flash/issues/849
[#852]: https://www.free-decompiler.com/flash/issues/852
[#837]: https://www.free-decompiler.com/flash/issues/837
[#811]: https://www.free-decompiler.com/flash/issues/811
[#745]: https://www.free-decompiler.com/flash/issues/745
[#803]: https://www.free-decompiler.com/flash/issues/803
[#738]: https://www.free-decompiler.com/flash/issues/738
[#742]: https://www.free-decompiler.com/flash/issues/742
[#747]: https://www.free-decompiler.com/flash/issues/747
[#749]: https://www.free-decompiler.com/flash/issues/749
[#752]: https://www.free-decompiler.com/flash/issues/752
[#753]: https://www.free-decompiler.com/flash/issues/753
[#759]: https://www.free-decompiler.com/flash/issues/759
[#766]: https://www.free-decompiler.com/flash/issues/766
[#768]: https://www.free-decompiler.com/flash/issues/768
[#773]: https://www.free-decompiler.com/flash/issues/773
[#776]: https://www.free-decompiler.com/flash/issues/776
[#783]: https://www.free-decompiler.com/flash/issues/783
[#785]: https://www.free-decompiler.com/flash/issues/785
[#787]: https://www.free-decompiler.com/flash/issues/787
[#788]: https://www.free-decompiler.com/flash/issues/788
[#790]: https://www.free-decompiler.com/flash/issues/790
[#794]: https://www.free-decompiler.com/flash/issues/794
[#798]: https://www.free-decompiler.com/flash/issues/798
[#800]: https://www.free-decompiler.com/flash/issues/800
[#676]: https://www.free-decompiler.com/flash/issues/676
[#734]: https://www.free-decompiler.com/flash/issues/734
[#687]: https://www.free-decompiler.com/flash/issues/687
[#709]: https://www.free-decompiler.com/flash/issues/709
[#732]: https://www.free-decompiler.com/flash/issues/732
[#730]: https://www.free-decompiler.com/flash/issues/730
[#735]: https://www.free-decompiler.com/flash/issues/735
[#722]: https://www.free-decompiler.com/flash/issues/722
[#725]: https://www.free-decompiler.com/flash/issues/725
[#715]: https://www.free-decompiler.com/flash/issues/715
[#635]: https://www.free-decompiler.com/flash/issues/635
[#726]: https://www.free-decompiler.com/flash/issues/726
[#716]: https://www.free-decompiler.com/flash/issues/716
[#717]: https://www.free-decompiler.com/flash/issues/717
[#718]: https://www.free-decompiler.com/flash/issues/718
[#720]: https://www.free-decompiler.com/flash/issues/720
[#719]: https://www.free-decompiler.com/flash/issues/719
[#723]: https://www.free-decompiler.com/flash/issues/723
[#288]: https://www.free-decompiler.com/flash/issues/288
[#677]: https://www.free-decompiler.com/flash/issues/677
[#389]: https://www.free-decompiler.com/flash/issues/389
[#701]: https://www.free-decompiler.com/flash/issues/701
[#707]: https://www.free-decompiler.com/flash/issues/707
[#302]: https://www.free-decompiler.com/flash/issues/302
[#685]: https://www.free-decompiler.com/flash/issues/685
[#698]: https://www.free-decompiler.com/flash/issues/698
[#710]: https://www.free-decompiler.com/flash/issues/710
[#711]: https://www.free-decompiler.com/flash/issues/711
[#681]: https://www.free-decompiler.com/flash/issues/681
[#688]: https://www.free-decompiler.com/flash/issues/688
[#691]: https://www.free-decompiler.com/flash/issues/691
[#524]: https://www.free-decompiler.com/flash/issues/524
[#663]: https://www.free-decompiler.com/flash/issues/663
[#702]: https://www.free-decompiler.com/flash/issues/702
[#539]: https://www.free-decompiler.com/flash/issues/539
[#650]: https://www.free-decompiler.com/flash/issues/650
[#680]: https://www.free-decompiler.com/flash/issues/680
[#649]: https://www.free-decompiler.com/flash/issues/649
[#656]: https://www.free-decompiler.com/flash/issues/656
[#661]: https://www.free-decompiler.com/flash/issues/661
[#664]: https://www.free-decompiler.com/flash/issues/664
[#668]: https://www.free-decompiler.com/flash/issues/668
[#674]: https://www.free-decompiler.com/flash/issues/674
[#675]: https://www.free-decompiler.com/flash/issues/675
[#632]: https://www.free-decompiler.com/flash/issues/632
[#651]: https://www.free-decompiler.com/flash/issues/651
[#678]: https://www.free-decompiler.com/flash/issues/678
[#672]: https://www.free-decompiler.com/flash/issues/672
[#684]: https://www.free-decompiler.com/flash/issues/684
[#647]: https://www.free-decompiler.com/flash/issues/647
[#648]: https://www.free-decompiler.com/flash/issues/648
[#612]: https://www.free-decompiler.com/flash/issues/612
[#623]: https://www.free-decompiler.com/flash/issues/623
[#624]: https://www.free-decompiler.com/flash/issues/624
[#627]: https://www.free-decompiler.com/flash/issues/627
[#640]: https://www.free-decompiler.com/flash/issues/640
[#595]: https://www.free-decompiler.com/flash/issues/595
[#592]: https://www.free-decompiler.com/flash/issues/592
[#585]: https://www.free-decompiler.com/flash/issues/585
[#578]: https://www.free-decompiler.com/flash/issues/578
[#501]: https://www.free-decompiler.com/flash/issues/501
[#616]: https://www.free-decompiler.com/flash/issues/616
[#559]: https://www.free-decompiler.com/flash/issues/559
[#401]: https://www.free-decompiler.com/flash/issues/401
[#593]: https://www.free-decompiler.com/flash/issues/593
[#594]: https://www.free-decompiler.com/flash/issues/594
[#579]: https://www.free-decompiler.com/flash/issues/579
[#428]: https://www.free-decompiler.com/flash/issues/428
[#337]: https://www.free-decompiler.com/flash/issues/337
[#584]: https://www.free-decompiler.com/flash/issues/584
[#576]: https://www.free-decompiler.com/flash/issues/576
[#250]: https://www.free-decompiler.com/flash/issues/250
[#580]: https://www.free-decompiler.com/flash/issues/580
[#510]: https://www.free-decompiler.com/flash/issues/510
[#583]: https://www.free-decompiler.com/flash/issues/583
[#586]: https://www.free-decompiler.com/flash/issues/586
[#574]: https://www.free-decompiler.com/flash/issues/574
[#570]: https://www.free-decompiler.com/flash/issues/570
[#563]: https://www.free-decompiler.com/flash/issues/563
[#561]: https://www.free-decompiler.com/flash/issues/561
[#509]: https://www.free-decompiler.com/flash/issues/509
[#433]: https://www.free-decompiler.com/flash/issues/433
[#557]: https://www.free-decompiler.com/flash/issues/557
[#556]: https://www.free-decompiler.com/flash/issues/556
[#504]: https://www.free-decompiler.com/flash/issues/504
[#529]: https://www.free-decompiler.com/flash/issues/529
[#538]: https://www.free-decompiler.com/flash/issues/538
[#537]: https://www.free-decompiler.com/flash/issues/537
[#540]: https://www.free-decompiler.com/flash/issues/540
[#387]: https://www.free-decompiler.com/flash/issues/387
[#552]: https://www.free-decompiler.com/flash/issues/552
[#494]: https://www.free-decompiler.com/flash/issues/494
[#513]: https://www.free-decompiler.com/flash/issues/513
[#262]: https://www.free-decompiler.com/flash/issues/262
[#499]: https://www.free-decompiler.com/flash/issues/499
[#508]: https://www.free-decompiler.com/flash/issues/508
[#305]: https://www.free-decompiler.com/flash/issues/305
[#312]: https://www.free-decompiler.com/flash/issues/312
[#503]: https://www.free-decompiler.com/flash/issues/503
[#304]: https://www.free-decompiler.com/flash/issues/304
[#306]: https://www.free-decompiler.com/flash/issues/306
[#507]: https://www.free-decompiler.com/flash/issues/507
[#424]: https://www.free-decompiler.com/flash/issues/424
[#425]: https://www.free-decompiler.com/flash/issues/425
[#478]: https://www.free-decompiler.com/flash/issues/478
[#485]: https://www.free-decompiler.com/flash/issues/485
[#517]: https://www.free-decompiler.com/flash/issues/517
[#518]: https://www.free-decompiler.com/flash/issues/518
[#361]: https://www.free-decompiler.com/flash/issues/361
[#392]: https://www.free-decompiler.com/flash/issues/392
[#516]: https://www.free-decompiler.com/flash/issues/516
[#495]: https://www.free-decompiler.com/flash/issues/495
[#496]: https://www.free-decompiler.com/flash/issues/496
[#299]: https://www.free-decompiler.com/flash/issues/299
[#303]: https://www.free-decompiler.com/flash/issues/303
[#324]: https://www.free-decompiler.com/flash/issues/324
[#346]: https://www.free-decompiler.com/flash/issues/346
[#371]: https://www.free-decompiler.com/flash/issues/371
[#426]: https://www.free-decompiler.com/flash/issues/426
[#457]: https://www.free-decompiler.com/flash/issues/457
[#458]: https://www.free-decompiler.com/flash/issues/458
[#460]: https://www.free-decompiler.com/flash/issues/460
[#462]: https://www.free-decompiler.com/flash/issues/462
[#463]: https://www.free-decompiler.com/flash/issues/463
[#465]: https://www.free-decompiler.com/flash/issues/465
[#466]: https://www.free-decompiler.com/flash/issues/466
[#369]: https://www.free-decompiler.com/flash/issues/369
[#390]: https://www.free-decompiler.com/flash/issues/390
[#453]: https://www.free-decompiler.com/flash/issues/453
[#459]: https://www.free-decompiler.com/flash/issues/459
[#461]: https://www.free-decompiler.com/flash/issues/461
[#451]: https://www.free-decompiler.com/flash/issues/451
[#454]: https://www.free-decompiler.com/flash/issues/454
[#455]: https://www.free-decompiler.com/flash/issues/455
[#474]: https://www.free-decompiler.com/flash/issues/474
[#477]: https://www.free-decompiler.com/flash/issues/477
[#481]: https://www.free-decompiler.com/flash/issues/481
[#484]: https://www.free-decompiler.com/flash/issues/484
[#493]: https://www.free-decompiler.com/flash/issues/493
[#365]: https://www.free-decompiler.com/flash/issues/365
[#366]: https://www.free-decompiler.com/flash/issues/366
[#429]: https://www.free-decompiler.com/flash/issues/429
[#447]: https://www.free-decompiler.com/flash/issues/447
[#354]: https://www.free-decompiler.com/flash/issues/354
[#438]: https://www.free-decompiler.com/flash/issues/438
[#436]: https://www.free-decompiler.com/flash/issues/436
[#446]: https://www.free-decompiler.com/flash/issues/446
[#427]: https://www.free-decompiler.com/flash/issues/427
[#405]: https://www.free-decompiler.com/flash/issues/405
[#420]: https://www.free-decompiler.com/flash/issues/420
[#421]: https://www.free-decompiler.com/flash/issues/421
[#430]: https://www.free-decompiler.com/flash/issues/430
[#397]: https://www.free-decompiler.com/flash/issues/397
[#431]: https://www.free-decompiler.com/flash/issues/431
[#169]: https://www.free-decompiler.com/flash/issues/169
[#335]: https://www.free-decompiler.com/flash/issues/335
[#404]: https://www.free-decompiler.com/flash/issues/404
[#407]: https://www.free-decompiler.com/flash/issues/407
[#399]: https://www.free-decompiler.com/flash/issues/399
[#400]: https://www.free-decompiler.com/flash/issues/400
[#398]: https://www.free-decompiler.com/flash/issues/398
[#382]: https://www.free-decompiler.com/flash/issues/382
[#396]: https://www.free-decompiler.com/flash/issues/396
[#357]: https://www.free-decompiler.com/flash/issues/357
[#391]: https://www.free-decompiler.com/flash/issues/391
[#395]: https://www.free-decompiler.com/flash/issues/395
[#334]: https://www.free-decompiler.com/flash/issues/334
[#301]: https://www.free-decompiler.com/flash/issues/301
[#383]: https://www.free-decompiler.com/flash/issues/383
[#386]: https://www.free-decompiler.com/flash/issues/386
[#367]: https://www.free-decompiler.com/flash/issues/367
[#380]: https://www.free-decompiler.com/flash/issues/380
[#292]: https://www.free-decompiler.com/flash/issues/292
[#375]: https://www.free-decompiler.com/flash/issues/375
[#378]: https://www.free-decompiler.com/flash/issues/378
[#325]: https://www.free-decompiler.com/flash/issues/325
[#210]: https://www.free-decompiler.com/flash/issues/210
[#355]: https://www.free-decompiler.com/flash/issues/355
[#313]: https://www.free-decompiler.com/flash/issues/313
[#330]: https://www.free-decompiler.com/flash/issues/330
[#332]: https://www.free-decompiler.com/flash/issues/332
[#344]: https://www.free-decompiler.com/flash/issues/344
[#295]: https://www.free-decompiler.com/flash/issues/295
[#297]: https://www.free-decompiler.com/flash/issues/297
[#307]: https://www.free-decompiler.com/flash/issues/307
[#309]: https://www.free-decompiler.com/flash/issues/309
[#310]: https://www.free-decompiler.com/flash/issues/310
[#311]: https://www.free-decompiler.com/flash/issues/311
[#327]: https://www.free-decompiler.com/flash/issues/327
[#328]: https://www.free-decompiler.com/flash/issues/328
[#333]: https://www.free-decompiler.com/flash/issues/333
[#336]: https://www.free-decompiler.com/flash/issues/336
[#338]: https://www.free-decompiler.com/flash/issues/338
[#315]: https://www.free-decompiler.com/flash/issues/315
[#123]: https://www.free-decompiler.com/flash/issues/123
[#243]: https://www.free-decompiler.com/flash/issues/243
[#326]: https://www.free-decompiler.com/flash/issues/326
[#287]: https://www.free-decompiler.com/flash/issues/287
[#290]: https://www.free-decompiler.com/flash/issues/290
[#291]: https://www.free-decompiler.com/flash/issues/291
[#294]: https://www.free-decompiler.com/flash/issues/294
[#298]: https://www.free-decompiler.com/flash/issues/298
[#296]: https://www.free-decompiler.com/flash/issues/296
[#314]: https://www.free-decompiler.com/flash/issues/314
[#316]: https://www.free-decompiler.com/flash/issues/316
[#318]: https://www.free-decompiler.com/flash/issues/318
[#319]: https://www.free-decompiler.com/flash/issues/319
[#323]: https://www.free-decompiler.com/flash/issues/323
[#223]: https://www.free-decompiler.com/flash/issues/223
[#261]: https://www.free-decompiler.com/flash/issues/261
[#269]: https://www.free-decompiler.com/flash/issues/269
[#274]: https://www.free-decompiler.com/flash/issues/274
[#275]: https://www.free-decompiler.com/flash/issues/275
[#258]: https://www.free-decompiler.com/flash/issues/258
[#267]: https://www.free-decompiler.com/flash/issues/267
[#286]: https://www.free-decompiler.com/flash/issues/286
[#233]: https://www.free-decompiler.com/flash/issues/233
[#235]: https://www.free-decompiler.com/flash/issues/235
[#263]: https://www.free-decompiler.com/flash/issues/263
[#264]: https://www.free-decompiler.com/flash/issues/264
[#265]: https://www.free-decompiler.com/flash/issues/265
[#266]: https://www.free-decompiler.com/flash/issues/266
[#281]: https://www.free-decompiler.com/flash/issues/281
[#251]: https://www.free-decompiler.com/flash/issues/251
[#257]: https://www.free-decompiler.com/flash/issues/257
[#259]: https://www.free-decompiler.com/flash/issues/259
[#260]: https://www.free-decompiler.com/flash/issues/260
[#268]: https://www.free-decompiler.com/flash/issues/268
[#272]: https://www.free-decompiler.com/flash/issues/272
[#276]: https://www.free-decompiler.com/flash/issues/276
[#220]: https://www.free-decompiler.com/flash/issues/220
[#284]: https://www.free-decompiler.com/flash/issues/284
[#232]: https://www.free-decompiler.com/flash/issues/232
[#253]: https://www.free-decompiler.com/flash/issues/253
[#137]: https://www.free-decompiler.com/flash/issues/137
[#242]: https://www.free-decompiler.com/flash/issues/242
[#244]: https://www.free-decompiler.com/flash/issues/244
[#203]: https://www.free-decompiler.com/flash/issues/203
[#225]: https://www.free-decompiler.com/flash/issues/225
[#236]: https://www.free-decompiler.com/flash/issues/236
[#245]: https://www.free-decompiler.com/flash/issues/245
[#247]: https://www.free-decompiler.com/flash/issues/247
[#248]: https://www.free-decompiler.com/flash/issues/248
[#254]: https://www.free-decompiler.com/flash/issues/254
[#255]: https://www.free-decompiler.com/flash/issues/255
[#256]: https://www.free-decompiler.com/flash/issues/256
[#241]: https://www.free-decompiler.com/flash/issues/241
[#238]: https://www.free-decompiler.com/flash/issues/238
[#239]: https://www.free-decompiler.com/flash/issues/239
[#240]: https://www.free-decompiler.com/flash/issues/240
[#237]: https://www.free-decompiler.com/flash/issues/237
[#217]: https://www.free-decompiler.com/flash/issues/217
[#219]: https://www.free-decompiler.com/flash/issues/219
[#224]: https://www.free-decompiler.com/flash/issues/224
[#121]: https://www.free-decompiler.com/flash/issues/121
[#207]: https://www.free-decompiler.com/flash/issues/207
[#151]: https://www.free-decompiler.com/flash/issues/151
[#171]: https://www.free-decompiler.com/flash/issues/171
[#206]: https://www.free-decompiler.com/flash/issues/206
[#208]: https://www.free-decompiler.com/flash/issues/208
[#209]: https://www.free-decompiler.com/flash/issues/209
[#229]: https://www.free-decompiler.com/flash/issues/229
[#213]: https://www.free-decompiler.com/flash/issues/213
[#221]: https://www.free-decompiler.com/flash/issues/221
[#226]: https://www.free-decompiler.com/flash/issues/226
[#227]: https://www.free-decompiler.com/flash/issues/227
[#230]: https://www.free-decompiler.com/flash/issues/230
[#172]: https://www.free-decompiler.com/flash/issues/172
[#174]: https://www.free-decompiler.com/flash/issues/174
[#175]: https://www.free-decompiler.com/flash/issues/175
[#212]: https://www.free-decompiler.com/flash/issues/212
[#185]: https://www.free-decompiler.com/flash/issues/185
[#186]: https://www.free-decompiler.com/flash/issues/186
[#197]: https://www.free-decompiler.com/flash/issues/197
[#216]: https://www.free-decompiler.com/flash/issues/216
[#168]: https://www.free-decompiler.com/flash/issues/168
[#176]: https://www.free-decompiler.com/flash/issues/176
[#177]: https://www.free-decompiler.com/flash/issues/177
[#202]: https://www.free-decompiler.com/flash/issues/202
[#173]: https://www.free-decompiler.com/flash/issues/173
[#190]: https://www.free-decompiler.com/flash/issues/190
[#129]: https://www.free-decompiler.com/flash/issues/129
[#153]: https://www.free-decompiler.com/flash/issues/153
[#180]: https://www.free-decompiler.com/flash/issues/180
[#136]: https://www.free-decompiler.com/flash/issues/136
[#179]: https://www.free-decompiler.com/flash/issues/179
[#144]: https://www.free-decompiler.com/flash/issues/144
[#164]: https://www.free-decompiler.com/flash/issues/164
[#167]: https://www.free-decompiler.com/flash/issues/167
[#170]: https://www.free-decompiler.com/flash/issues/170
[#178]: https://www.free-decompiler.com/flash/issues/178
[#181]: https://www.free-decompiler.com/flash/issues/181
[#182]: https://www.free-decompiler.com/flash/issues/182
[#183]: https://www.free-decompiler.com/flash/issues/183
[#184]: https://www.free-decompiler.com/flash/issues/184
[#189]: https://www.free-decompiler.com/flash/issues/189
[#191]: https://www.free-decompiler.com/flash/issues/191
[#195]: https://www.free-decompiler.com/flash/issues/195
[#196]: https://www.free-decompiler.com/flash/issues/196
[#198]: https://www.free-decompiler.com/flash/issues/198
[#200]: https://www.free-decompiler.com/flash/issues/200
[#201]: https://www.free-decompiler.com/flash/issues/201
[#166]: https://www.free-decompiler.com/flash/issues/166
[#165]: https://www.free-decompiler.com/flash/issues/165
[#63]: https://www.free-decompiler.com/flash/issues/63
[#67]: https://www.free-decompiler.com/flash/issues/67
[#117]: https://www.free-decompiler.com/flash/issues/117
[#127]: https://www.free-decompiler.com/flash/issues/127
[#134]: https://www.free-decompiler.com/flash/issues/134
[#155]: https://www.free-decompiler.com/flash/issues/155
[#142]: https://www.free-decompiler.com/flash/issues/142
[#146]: https://www.free-decompiler.com/flash/issues/146
[#130]: https://www.free-decompiler.com/flash/issues/130
[#132]: https://www.free-decompiler.com/flash/issues/132
[#145]: https://www.free-decompiler.com/flash/issues/145
[#147]: https://www.free-decompiler.com/flash/issues/147
[#148]: https://www.free-decompiler.com/flash/issues/148
[#152]: https://www.free-decompiler.com/flash/issues/152
[#156]: https://www.free-decompiler.com/flash/issues/156
[#157]: https://www.free-decompiler.com/flash/issues/157
[#158]: https://www.free-decompiler.com/flash/issues/158
[#159]: https://www.free-decompiler.com/flash/issues/159
[#160]: https://www.free-decompiler.com/flash/issues/160
[#162]: https://www.free-decompiler.com/flash/issues/162
[#163]: https://www.free-decompiler.com/flash/issues/163
[#149]: https://www.free-decompiler.com/flash/issues/149
[#150]: https://www.free-decompiler.com/flash/issues/150
[#119]: https://www.free-decompiler.com/flash/issues/119
[#101]: https://www.free-decompiler.com/flash/issues/101
[#114]: https://www.free-decompiler.com/flash/issues/114
[#135]: https://www.free-decompiler.com/flash/issues/135
[#141]: https://www.free-decompiler.com/flash/issues/141
[#102]: https://www.free-decompiler.com/flash/issues/102
[#124]: https://www.free-decompiler.com/flash/issues/124
[#128]: https://www.free-decompiler.com/flash/issues/128
[#131]: https://www.free-decompiler.com/flash/issues/131
[#104]: https://www.free-decompiler.com/flash/issues/104
[#113]: https://www.free-decompiler.com/flash/issues/113
[#133]: https://www.free-decompiler.com/flash/issues/133
[#140]: https://www.free-decompiler.com/flash/issues/140
[#108]: https://www.free-decompiler.com/flash/issues/108
[#105]: https://www.free-decompiler.com/flash/issues/105
[#109]: https://www.free-decompiler.com/flash/issues/109
[#106]: https://www.free-decompiler.com/flash/issues/106
[#107]: https://www.free-decompiler.com/flash/issues/107
[#110]: https://www.free-decompiler.com/flash/issues/110
[#103]: https://www.free-decompiler.com/flash/issues/103
[#111]: https://www.free-decompiler.com/flash/issues/111
[#96]: https://www.free-decompiler.com/flash/issues/96
[#98]: https://www.free-decompiler.com/flash/issues/98
[#99]: https://www.free-decompiler.com/flash/issues/99
[#100]: https://www.free-decompiler.com/flash/issues/100
[#85]: https://www.free-decompiler.com/flash/issues/85
[#79]: https://www.free-decompiler.com/flash/issues/79
[#92]: https://www.free-decompiler.com/flash/issues/92
[#93]: https://www.free-decompiler.com/flash/issues/93
[#94]: https://www.free-decompiler.com/flash/issues/94
[#95]: https://www.free-decompiler.com/flash/issues/95
[#86]: https://www.free-decompiler.com/flash/issues/86
[#87]: https://www.free-decompiler.com/flash/issues/87
[#88]: https://www.free-decompiler.com/flash/issues/88
[#89]: https://www.free-decompiler.com/flash/issues/89
[#82]: https://www.free-decompiler.com/flash/issues/82
[#78]: https://www.free-decompiler.com/flash/issues/78
[#81]: https://www.free-decompiler.com/flash/issues/81
[#84]: https://www.free-decompiler.com/flash/issues/84
[#83]: https://www.free-decompiler.com/flash/issues/83
[#65]: https://www.free-decompiler.com/flash/issues/65
[#48]: https://www.free-decompiler.com/flash/issues/48
[#53]: https://www.free-decompiler.com/flash/issues/53
[#66]: https://www.free-decompiler.com/flash/issues/66
[#68]: https://www.free-decompiler.com/flash/issues/68
[#69]: https://www.free-decompiler.com/flash/issues/69
[#75]: https://www.free-decompiler.com/flash/issues/75
[#73]: https://www.free-decompiler.com/flash/issues/73
[#62]: https://www.free-decompiler.com/flash/issues/62
[#72]: https://www.free-decompiler.com/flash/issues/72
[#64]: https://www.free-decompiler.com/flash/issues/64
[#38]: https://www.free-decompiler.com/flash/issues/38
[#56]: https://www.free-decompiler.com/flash/issues/56
[#57]: https://www.free-decompiler.com/flash/issues/57
[#58]: https://www.free-decompiler.com/flash/issues/58
[#45]: https://www.free-decompiler.com/flash/issues/45
[#50]: https://www.free-decompiler.com/flash/issues/50
[#51]: https://www.free-decompiler.com/flash/issues/51
[#52]: https://www.free-decompiler.com/flash/issues/52
[#47]: https://www.free-decompiler.com/flash/issues/47
[#42]: https://www.free-decompiler.com/flash/issues/42
[#39]: https://www.free-decompiler.com/flash/issues/39
[#40]: https://www.free-decompiler.com/flash/issues/40
[#44]: https://www.free-decompiler.com/flash/issues/44
[#36]: https://www.free-decompiler.com/flash/issues/36
[#43]: https://www.free-decompiler.com/flash/issues/43
[#46]: https://www.free-decompiler.com/flash/issues/46
[#3]: https://www.free-decompiler.com/flash/issues/3
[#37]: https://www.free-decompiler.com/flash/issues/37
[#34]: https://www.free-decompiler.com/flash/issues/34
[#35]: https://www.free-decompiler.com/flash/issues/35
[#32]: https://www.free-decompiler.com/flash/issues/32
[#31]: https://www.free-decompiler.com/flash/issues/31
[#1240]: https://www.free-decompiler.com/flash/issues/1240
[#1308]: https://www.free-decompiler.com/flash/issues/1308
[#1333]: https://www.free-decompiler.com/flash/issues/1333
[#1365]: https://www.free-decompiler.com/flash/issues/1365
[#1369]: https://www.free-decompiler.com/flash/issues/1369
[#1327]: https://www.free-decompiler.com/flash/issues/1327
[#1343]: https://www.free-decompiler.com/flash/issues/1343
[#1348]: https://www.free-decompiler.com/flash/issues/1348
[#1354]: https://www.free-decompiler.com/flash/issues/1354
[#1367]: https://www.free-decompiler.com/flash/issues/1367
[#1401]: https://www.free-decompiler.com/flash/issues/1401
[#1402]: https://www.free-decompiler.com/flash/issues/1402
[#1430]: https://www.free-decompiler.com/flash/issues/1430
[#1254]: https://www.free-decompiler.com/flash/issues/1254
[#907]: https://www.free-decompiler.com/flash/issues/907
[#1311]: https://www.free-decompiler.com/flash/issues/1311
[#1313]: https://www.free-decompiler.com/flash/issues/1313
[#1308]: https://www.free-decompiler.com/flash/issues/1308
[#1189]: https://www.free-decompiler.com/flash/issues/1189
[#1274]: https://www.free-decompiler.com/flash/issues/1274
[#1275]: https://www.free-decompiler.com/flash/issues/1275
[#1278]: https://www.free-decompiler.com/flash/issues/1278
[#1281]: https://www.free-decompiler.com/flash/issues/1281
[#1254]: https://www.free-decompiler.com/flash/issues/1254
[#1289]: https://www.free-decompiler.com/flash/issues/1289
[#1283]: https://www.free-decompiler.com/flash/issues/1283
[#1294]: https://www.free-decompiler.com/flash/issues/1294
[#1302]: https://www.free-decompiler.com/flash/issues/1302
[#1309]: https://www.free-decompiler.com/flash/issues/1309
[#1312]: https://www.free-decompiler.com/flash/issues/1312
[#1303]: https://www.free-decompiler.com/flash/issues/1303
[#1314]: https://www.free-decompiler.com/flash/issues/1314
[#1320]: https://www.free-decompiler.com/flash/issues/1320
[#1323]: https://www.free-decompiler.com/flash/issues/1323
[#27]: https://www.free-decompiler.com/flash/issues/27
[#1449]: https://www.free-decompiler.com/flash/issues/1449
