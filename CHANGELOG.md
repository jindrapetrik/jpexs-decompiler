# Change Log
All notable changes to this project will be documented in this file.

## [18.5.0] - 2023-06-25
### Added
- [#1998] Setting for maximum number of items in the cache - allows less memory consumption (Defaults to 500 per cache)
- [#2038], [#2028], [#2034], [#2036] Support for Harman AIR encrypted SWFs (Read-only)
- Decrypt Harman AIR SWFs via commandline

### Fixed 
- [#2004] Freezing when a shape has nonimage character set as fill
- [#2004] Nonrepeating fill border
- [#2008] AS3 P-code editing optional Double value when it has no fractional part
- AS3 P-code editation - zero line number on error
- [#2007] AS3 renaming invalid identifiers - not refreshing AbcIndex afterwards
- AS1/2 - loadMovie / loadVariables / loadMovieNum / loadVariablesNum editation incorrectly setting GET as method

## [18.4.1] - 2023-04-05
### Fixed
- [#1993] Incorrect scroll position causing shapes to be hidden
- [#1994] Replace command in commandline with three argument causing replacements file load
- [#1477] Open file (Context menu) with unicode characters, unicode in paths, on Windows
- Starting app with parameters causing wrong GUI init
- [#1991] ConcurrentModificationException on clearing cache thread
- [#1999] AS3 decompilation - XML constructor call with other than string argument

### Changed
- [#1996] Items are now exported in order of appearance in the tag tree (usually SWF order), previously was it in order of selection

## [18.4.0] - 2023-03-19
### Added
- AS3 support for logical AND/OR compound operator
- AS3 Display missing namespaces along traits as §§namespace("url")
- [#1888], [#1892] AS3 option to select SWF dependencies to properly resolve namespaces, types, etc. (currently in GUI only)
- FileAttributes tag - SWF relative Urls flag
- AS3 P-code editing class trait
- [#355] Updated Chinese translation
- FLA Export - AS2 - Sprite linkage to class
- [#1682] AS1/2 Context menu add script on frames/buttons/placeObjects
- Allow adding second DoAction to a frame

### Fixed
- [#1981] AS3 fully qualified (colliding) types in submethods
- AS3 direct editation - Allow member or call for doubles
- AS3 direct editation - Allow comma operator in XML filter operation
- AS3 direct editation - Allow comma operator in switch expressions
- AS3 XML embedded variables display and direct edit
- AS3 Metadata values order
- AS3 Metadata in P-code formatting
- AS3 Metadata single value (null item key)
- [#1981] AS3 star import collisions
- [#1982] Slow calculation of large shape outlines - now use only rectangles for large shapes
- [#1986] AS2 Class detection - NullPointerException on certain classes
- AS3 P-code ValueKind namespaces handling
- AS3 direct editation - namespace definition without explicit value
- AS3 direct editation - var/const outside package
- AS3 interfaces - internal modifier on methods
- AS3 direct editation - interface method namespace
- AS3 p-code docs - deldescendants, negate_p operands
- AS3 p-code - IGNORE_REST method flag incorrectly shown as EXPLICIT
- [#1989] AS3 - Slow deobfuscation (AVM2DeobfuscatorSimpleOld)
- AS3 - getouterscope instruction support
- [#1990] Cloning DefineSprite causing incorrect tags written
- Do not display fonts added to stage (for example in testdata/as2.swf, the vertical text - sprite 10)
- AS2 Class detection - TemporaryRegisterMark handling
- FLA export scripts location
- FLA export shape tweens (morphshapes)
- AS1/2 adding CLIPACTIONRECORD to PlaceObject which already has a record

### Changed
- AS1/2/3 P-code - format Number values with EcmaScript toString function
- AS3 p-code - EXPLICIT method flag renamed to NATIVE

## [18.3.6] - 2023-02-25
### Fixed
- [#1970] FLA export - do not strip empty frames at the end of timeline
- [#1970] AS2 Renaming invalid identifiers for direct strings (no constant indices)
- [#1970] AS2 Renaming invalid identifiers IndexOutOfBounds on invalid constant index (obfuscated code, etc.)
- [#1972] AS3 Renaming invalid identifiers - '#' character
- [#1972] AS3 Renaming invalid identifiers - various fixes
- [#1972] AS3 imports taken only from packages, not package internal
- Unresponsive status bar and its icon
- [#1973] FLA export - improper calculation of shape instance count
- FLA export - XML formatting with blank lines on Java9+
- [#1974] DefineBits image reading problem
- [#1963] AS2 properly decompile/direct edit long classes
- [#1977] AS3 Find usages - class and function usages, various fixes
- IllegalArgumentException: JSplitPane weight must be between 0 and 1
- [#1979] SVG import - autoclosing fill paths (without closing stroke paths)

## [18.3.5] - 2023-02-12
### Added
- [#1959] Display frame labels along frames and FrameLabel tags

### Fixed
- [#1960] Hide tag tree root handles as it was in previous versions
- [#1964] Freezing on releasing mouse while shape transforming (deadlock)
- [#1961] Characters can use characterId 0, PlaceObject can use depth 0
- [#1963] Reading CLIPEVENTFLAGS ClipActionEndFlag on SWF versions >= 6
- [#1968], [#1971], [#1957] Cannot start FFDec due to large stack size on some configurations

### Changed
- [#1960] Quick search does not search in SWF name or folder names
- [#1961] SoundStreamHead on main timeline is exported/imported with identifier "-1"
- [#1957] Larger stack size (when needed) must be configured manually in ffdec.bat or ffdec.sh

## [18.3.4] - 2023-01-30
### Added
- [#1029] Better separation of library and main app, dependencies inside library zip, library readme
- Remembering script+folder scroll/caret position when switching between items, saving for pinned items

### Fixed
- [#1948] Timeout while deobfuscation did not skip method
- [#1948] NullPointerException on Simplify expressions on incrementent/decrement
- [#1941] Export when no node is selected after SWF opening
- Exception handling in cache clearing thread
- DottedChain.PathPart NoSerializable exception
- [#1951] Clearing Namespace/Multiname cache after renaming identifiers
- [#1951] Renaming invalid identifiers with existing string collisions
- [#1888] String casts - ConvertS on XML, XMLList
- [#1953] Save as EXE - add file extension when missing
- [#1954] Incorrect calculation of empty button bounds causing OutOfMemory
- [#1944] Scroll position not retained on Ctrl+click in the tag tree
- [#1940] AS3 decompilation - wrong assignment
- AS3 - incorrect switching P-code causing empty text
- AS3 - Select the trait after adding new
- [#1955] AS3 - Exception during removing trait
- [#688] AS3 Direct editation - construction (new keyword) converted to call when result not used

### Changed
- [#1957] Increased maximum stack size to avoid StackOverflowErrors on unusual scripts

## [18.3.3] - 2023-01-22
### Added
- [#1913] Option to retain shape exact position(bounds) in SVG export
- [#1913] Option to disable bitmap smoothing for display

### Fixed
- [#1888] AS3 - missing casts in declarations
- [#1894] Switch inside loop
- [#1801] AS3 - AIR/Flash switching
- [#1892] AS3 - internal modifier after implicit namespace
- [#1888] AS3 - Coerce to string
- AS3 - local registers type declarations vs for..in clause
- [#1888] AS3 - Coerce to int when Number
- AS3 - super properties resolving
- AS3 - line numbering on pushback string on regexp
- AS3 Direct editation - removing method bodies after unsuccessful edit
- [#1936] AS3 - Parentheses around function definition call
- [#1936] AS3 - Scope stack in second pass
- [#1936] AS3 Direct editation - handling undefined variables
- [#1936] AS3 Direct editation - colliding try..catch variable
- [#1936] AS3 Direct editation - missing pop after call
- [#1936] AS3 Direct editation - slots increment, decrement
- [#1936] AS3 Direct editation - scope of nested functions
- AS3 - empty P-code shown on clicking script
- [#1888] AS3 - Coerces, module operator
- [#1937] AS3 - declarations vs null
- [#1458] Quick find bar overlaying horizontal scrollbar
- [#1842] AS1/2 Better handling obfuscated code, for..in
- [#1842] AS1/2 use parenthesis when initObject has nonstring keys
- [#1842] AS - Do not display §§dup when the value has no sideeffect
- Deobfuscation icon on script toolbar did not match the deobfuscation status
- [#1938] AS3 Direct editation - implied this instead of findprop
- [#1938] AS3 Direct editation - local registers coerce/convert
- [#1938] AS3 Direct editation - setting default values for slots
- AS3 Direct editation - using local classes as types
- [#1938] AS3 - coercion call type
- [#1938] AS3 - shortening + 1 to increment
- [#1938] AS3 - implicit coercion of operations
- [#1938] AS3 - initproperty compound operators, increment/decrement
- [#1938] "Open loaded during play" Loader injection for Multiname types
- AS3 - not using visitCode when not needed => faster decompilation
- Cache thread as daemon
- [#1949] Incorrect reading FIXED and FIXED8 SWF values causing wrong Filters size and OutOfMemory
 
## [18.3.2] - 2023-01-10
### Removed
- [#1935], [#1913] Retaining shape exact position(bounds) in SVG export/import

## [18.3.1] - 2023-01-09
### Added
- GFX - support for TGA external images
- GFX - DefineExternalGradient tag has gradientId in its name
- GFX - DefineExternalSound and DefineExternalStreamSound playback

### Fixed
- GFX - DefineExternalImage2 display and correct handling if standalone
- [#1931], [#1934] DefineSprite rectange calculation (incorrect export dimensions)
- [#1929], [#1932] Wrong subsprite frames display
- [#1933] AS3 - Detection of variable names from debug info on multiple debug ins with same regindex
- GFX - ExporterInfo prefix is NetString
- Scrollbars on sound playback
- Clear preview on raw edit to stop sound playback
- CXFORM and GRADRECORD causing NotSerializableException
- Scrollbars
- Incorrect frame counting
- Save as does not change file title upon reload

## [18.3.0] - 2023-01-01
### Added
- [#1913] Shape transforming, point editation
- Hilighting currently selected shape edge in the raw edit
- [#1905] Key strokes on folder preview panel
- Scrollbars
- Morphshape transforming, point editation
- Raw edit - (MORPH)GRADIENT spreadMode,interpolationMode as enums
- Unit selection (pixels/twips) in header editation

### Fixed
- [#1915] SVG import - gradient when it has two final stops
- Native sound export format for ADPCM compression is FLV
- [#1923] Wrong cyclic tag detection causing hidden sprites
- Ctrl + G shortcut for tag list view
- Uncompressed FLA (XFL) export creates a directory
- [#1827] Video replacing VP6 reading
- [#1926] Constructors namespace taken from class - should be always public
- [#1772] AS1/2 decompilation - StackOverflow during getVariables function
- [#1890] AS3 - Removing first assignment in for in loop

### Changed
- [#1913] SVG export/import of shapes - shape exact position (bounds) is retained

## [18.2.1] - 2022-12-28
### Fixed
- Copy/Move/Cut with dependencies did not handle original tag when not charactertag
- [#1922] FLA/XFL/Canvas/SVG export - exporting DefineBitsJPEG3/4 with alpha as JPEG with PNG extension
- [#1921] AS3 direct editation - exception on code save - wrong selected ABC

## [18.2.0] - 2022-12-27
### Added
- [#1917] Better error message for sound import on unsupported sampling rate
- [#1827] Replacing and bulk import of DefineVideoStream
- Movie FLV export - writing simple onMetadata tag
- [#1424], [#1473], [#1835], [#1852] Replacing sound streams (SoundStreamHead, SoundStreamBlock)
- Bulk import sounds and sound streams

### Fixed
- [#1914] DropShadow filter
- [#1916] Translation tool did not load up
- PlaceObject preview not cleared causing sound to repeat
- [#1920] AS3 - Slower decompilation (returnType method optimization)

## [18.1.0] - 2022-12-23
### Added
- Deobfuscation and its options as icons on script panel toolbar
- Warning before switching auto rename identifiers on
- [#1231] Button transforming
- [#1690] Deobfuscation tool dialog for script level (not just current method / all classes)
- [#1460] Commandline import of text, images, shapes, symbol-class
- [#1909] Export/import DefineBitsJPEG3/4s alpha channel to/from separate file
 ("PNG/GIF/JPEG+alpha" option in GUI, "-format image:png_gif_jpeg_alpha" for commandline)
- [#1910] Copy/paste transform matrix to/from the clipboard
- [#1912] Persist selected item in the tree upon quick search (Ctrl+F)
- [#1901] Editor mode and autosave feature for header, raw editor, transform
- [#583] FlashPaper SWF to PDF with selectable text (commandline)
- [#1858] PDF export - JPEG with alpha channel exported as is

### Fixed
- [#1904] NullPointerException when renaming invalid identifiers in AS1/2 files caused by missing charset
- [#1904] NullPointerException when fast switching items
- [#1904] NullPointerException on ErrorLog frame
- [#1904] NullPointerException on decompiler pool
- [#1904] AS1/2 Simplify expressions breaks registers, functions
- [#1904] AS1/2 Throw is an ExitItem to properly handle continues vs ifs
- [#595] AS3 direct editation - protected property resolving
- AS3 direct editation and decompiler share same AbcIndex
- BUTTONRECORD display does not use its Matrix
- Editation status not cleared after Sprite transforming
- Image flickering
- Show Hex dump for AS1/2 script tags
- Speaker image when sound selected not in the center
- [#1908] Slow commandline opening SWF
- [#1908] Shape/image import must accept also filenames in the form "CHARID_xxx.ext" instead of just "CHARID.ext"
- Exporting DefineJPEG3/4 with alpha channel to PNG produced JPEG instead
- AS3 package level const with function value - separate P-code for trait and method
- Slot/const trait proper p-code indentation
- [#1858] PDF export - Adding same ExtGState multiple times,
- [#1858] PDF export - Applying same alpha/blendmode multiple times
- [#1858] PDF export - Applying same color multiple times
- [#1907] Crashing on memory search
- [#1906] Memory search - byte align opens wrong SWFs

### Changed
- Warning before switching deobfuscation is now optional
- [#1690] Redesigned Deobfuscation tool dialog.
- Shape/image/script/text import does not require specific folder name inside (but still preffers it when exists)

### Removed
- "Restore control flow" deobfuscation level as it was the same as "Remove traps"

## [18.0.0] - 2022-12-18
### Added
- [#1898] Keyboard shortcut to remove tags (DEL, SHIFT+DEL)
- [#1511], [#1765] Quick search tree (Ctrl+F) for everything, not just AS3 classes
- Quick search (Ctrl+F) for tag list view
- [#1884] Memory search - show size and adress in hex, show only aligned to N bytes
- AS3 - "internal" keyword support
- ProductInfo tag information display
- DebugId tag proper display and editation
- [#1564], [#1676], [#1697], [#1893] Display of DefineVideoStream tags with VLC player
- List of treenode subitems on otherwise empty panel (with 32x32 icons)
- DefineVideoStream codecId and videoFlagsDeblocking handled as enums in raw editation
- Option to mute frame sounds
- Experimental option to fix conflation artifacts in antialising (slow)
- Option to disable autoplay of sounds (DefineSound)
- [#1181] Remembering choice of loading assets via importassets tag
- [#1900] Free transform whole sprites
- Show axis as dashed line in Free transform of sprites
- [#1900] Transformation panel with flip/move/scale/rotate/skew/matrix options
- [#1900] Move object around with arrow keys (in transform mode)
- Alt + click selects PlaceObjectTag under cursor
- [#1901] Double click tree node to start edit (can be enabled in settings)
- Info about editation in status bar
- AS3 P-code keyword "Unknown(N)", where N is index. For constants out of bounds. (mostly in dead code)
- AS3 P-code - Editing methods without body (interfaces / native methods)

### Fixed
- [#1897] Close menu button without selecting specific item
- Reading UI32 values
- Parsing obfuscated namespaces with hash character "#"
- Tag dependency checking
- [#1884] Memory search - Logged exception when cannot get page range
- [#1884] Memory search - Exception on sorting by pid
- [#1006] AS3 - Warning - Function value used where type Boolean was expected
- AS3 - Resolving types on static protected namespaced properties
- Hiding selection after raw editation save
- Proper disabling switching items or other actions on editation
- Raw editor item count and edit display
- Warnings about invalid reflective access in color dialog on Java 9+
- Folder preview tag names have indices when multiple with same name
- ShapeImporter fillstyles shapenum
- Reload button disabled after saving new file
- PlaceObject tag - do not display export name twice
- Loading nested characters when Importassets tag used
- Hide various actions for imported tags
- Clone tag
- Hide freetransform button in readonly mode
- Maintain export name/class on imported tags
- Classnames in PlaceObject
- [#1828] AS1/2 deobfuscation removing variable declarations
- Loaded SWFs using "Open loaded during play" feature have short filenames
- [#1796] Exception on closing multiple SWFs
- AS3 Deobfuscation causing invalid jump offsets for files with constant indices out of bounds
- AS3 - "native" modifier only for methods with EXPLICIT flag
- AS3 - AS3 builtin namespace visibility

### Changed
- Quick search needs minimum of 3 characters
- AS1/2 deobfuscation - removing obfuscated declarations is now optional (default: off)
- AS3 - order of modifiers: final, override, access, static, native

## [17.0.4] - 2022-12-02
### Fixed
- [#1888] Casts for missing types, cast handling for script local classes
- [#1895] Handling of unstructured switch
- [#1896] NullPointer during deobfuscation

## [17.0.3] - 2022-11-30
### Added
- Translator tool for easier localization
- AS3 improved goto declaration for properties and methods
- playerglobal.swc and airglobal.swf now part of FFDec bundle

### Fixed
- [#1769] AS3 - Missing some body trait variable declaration
- [#1769], [#1888] AS3 - Missing casts like int()
- [#1890] AS3 - Chained assignments in some special cases
- [#1810] AS3 Direct editation - XML attribute handling
- [#1810] AS3 Direct editation - Calls inside submethods using this
- [#1891] AS3 - duplicate variable declaration in some cases
- All SWF classes inside DoABC tags in the taglist view
- Exception on package selection inside DoABC tag on taglist view
- [#1892] AS3 - Package internal custom namespaces
- Unpin all context menu not clearing pins properly
- AS3 - RegExp escaping
- AS3 - Avoid Error Implicit coercion of a value of type XXX to an unrelated type YYY
- AS3 - XML - get descendants operator parenthesis
- Switch decompilation in some corner cases
- [#1894] Switches vs loops decompilation (now with two passes)
- [#1894] AS3 - XML filters in some corner cases
- [#1887] AS3 - strict equals operator decompilation

## [17.0.2] - 2022-11-22
### Fixed
- [#1882] Close button on the menu toolbar

## [17.0.1] - 2022-11-21
### Added
- PR119 Option to set scale factor in advanced settings (Set it to 2.0 on Mac retina displays)

### Fixed
- [#1880] JPEG Fixer
- Close action from menu not available on bundles (zip, etc...)
- [#1881] Wrong locale reference for invalid tag order
- New file action
- Moving tags to frames

## [17.0.0] - 2022-11-20
### Added
- [#1870] AS3 Adding new class - Target DoABC tag or position can be selected to prevent Error 1014
- [#1871] Toogle buttons for disabling subsprite animation, display preview of sprites/frames
- [#1875] Remove no longer accessed items from cache after certain amount of time
- [#1280] AS3 Direct editation of traits with the same name
- [#1743] GFX - Adding DefineExternalImage2 and DefineSubImage tags
- [#1822], [#1803] AS3 direct editation - optional using AIR (airglobal.swc) to compile
- [#1501] Bulk import shapes
- [#1680] Pinning items
- Indices in brackets for items with same name (like two subsequent DoAction tags)
- Flattened ActionScript packages (one row per package instead package tree), can be turned off in settings
- [#1820] Opening standalone ABC files (*.abc)
- Classes tree inside DoABC tags in taglist view
- Export ABC data from DoABC tags

### Fixed
- [#1869] Replace references now replaces all references, not just PlaceObject
- Handle StartSound tag as CharacterIdTag
- Clearing shape export cache on changes
- Preview of PlaceObject and frames on hex dump view
- AS3 Direct editation - Top level classes do not use ":" in their namespace names
- AS3 Direct editation - Using "/" separator for method names
- Folder preview resizing (scrollbar too long)
- [#1872] Removing PlaceObject/RemoveObject with no characterid with Remove character action
- [#1692] Resolving use namespace
- [#1692] Properly distinguish obfuscated names vs namespace suffixes and attributes
- [#1757] Binary search - SWF files need to be sorted by file position
- [#1803] AS3 Direct editation - Colliding catch name with other variable names / arguments
- AS3 Direct editation - slow property resolving (Now up to 10 times faster compilation)
- [#1875] Garbage collect SWF and its caches after closing it
- [#1807] Proper parenthesis around call inside another call
- [#1840] AS3 - Allow to compile object literal keys with nonstring/numbers in obfuscated code
- [#1840] AS3 Direct editation - Type mismatched for a trait
- [#1840] Proper if..continue..break handling
- [#1877] Recalculate dependent characters and frames on removing / editing item
- DefineShape4 SVG import NullPointerException
- List of objects under cursor and coordinates not showing
- ConcurrentModificationException in getCharacters on exit
- Header of display panel not visible on certain color schemes
- Move tag to action did not remove original tag
- Show in tag list from tag scripts
- Move/Copy tag to action on tag scripts
- [#1879] False tag order error with SoundStreamHead
- Error messages during SWF/ABC reading have correct error icon and title, are translatable

### Changed
- GFX - DefineExternalImage2 no longer handled as character
- Raw editor does not show tag name in the tree (it's now in the new pinnable head)
- DoInitAction is not shown in resources/sprites section, only in scripts
- ActionScript packages are by default flattened (can be turned off in settings)

## [16.3.1] - 2022-11-14
### Fixed
- [#1867] AS3 - §§hasnext, §§nextvalue, §§nextname in some nonstandard compiled SWFs
- [#1868] Raw editation NullPointerException

## [16.3.0] - 2022-11-14
### Added
- Allowed copy/cut tags to clipboard across multiple SWFs
- Keyboard shortcuts for tag clipboard operations
- Hilight clipboard panel on copy/cut action for a few seconds
- Drag and drop to move/copy tags in the tag list view (Can be disabled in settings)
- Setting for enabling placing Define tags into DefineSprite
- Icons for tags in replace character dialog
- Move tag with dependencies
- Copy/Move tag operation has select position dialog
- Select position dialog has target file in its title
- [#1649] Moving SWF files (and bundles) up and down (context menuitem + ALT up/down shortcut)
- Moving tags up and down in the taglist view (context menuitem + ALT up/down shortcut)
- [#1701] Setting charset for SWF files with version 5 or lower (GUI, commandline)
- [#1864] Commandline: Allow to set special value "/dev/stdin" for input files to read from stdin (even on Windows)
- Show button records in the tree, preview them
- Show in Hex dump for BUTTONCONDACTION, BUTTONRECORD, CLIPACTIONRECORD
- Alpha and Erase blend modes support
- Raw editor - Edit blend modes as enum
- Search in the advanced settings

### Fixed
- Exception when bundle selected
- File path in window title for SWFs inside DefineBinaryData
- [#1863] Export to PDF - cannot read fonts with long CMAP
- Go to document class when switched to tag list view
- Copy/Move with dependencies order of tags
- [#1865] ConcurrentModificationException on SWF close
- NullPointerException on expanding needed/dependent characters on basic tag info
- Copy/Move with dependencies should copy mapped tags too
- Recalculating dependencies in the loop (now only on change)
- Dependencies handling
- Raw editing of DefineFontInfo/DefineFont2-3, KERNINGRECORD - proper switching wide codes
- Storing SWF configuration for files inside bundles and/or binarydata
- [#1846] blend modes with alpha
- Raw editor does not select item in enum list
- Sound not played on frames
- [#1678] Miter clip join - can be enabled in Settings
- Html label links visibility

### Changed
- Full path inside bundle is displayed as SWF name instead simple name

## [16.2.0] - 2022-11-08
### Added
- [#1414] Cancelling in-progress exportation
- [#1755] Copy tags to tag clipboard and paste them elsewhere
- [#1460] Bulk importing images
- Bulk importing scripts/text/images added to SWF context menu
- [#1465] Configuration option to disable SWF preview autoplay
- Setting for disabling expanding first level of tree nodes on SWF load

### Fixed
- FLA export printing xxx string on exporting character with id 320
- Copy to with dependencies does not refresh timeline
- Copy to with dependencies does not set the timelined, that can result to missing dependencies (red tags in the tree)
- Double warning/error when copy to / move to and same character id already exists
- [#1862], [#1735] Exporting selection to subfolders by SWFname when multiple SWFs selected
- Java code export indentation
- Java code does not export tags
- On new SWF loading, do not expand all other SWFs nodes, only this one

## [16.1.0] - 2022-11-06
### Added
- [#1459], [#1832], [#1849] AS1/2 direct editation - Error dialog when saved value (UI16, SI16, ...) exceeds its limit and this code cannot be saved.
- Attach tag menu (Like DefineScaling grid to DefineSprite, etc.)
- Better tag error handling - these tags now got error icon
- Show in Hex dump command from other views for tags
- Show in Taglist command from dump view for tags
- Create new empty SWF file
- Checking missing needed character tags and their proper position (Marking them as red - with tooltip)
- [#1432] Save as EXE from commandline
- [#1232] Needed/dependent characters list in basic tag info can be expanded to show tag names

### Fixed
- Flash viewer - subtract blend mode
- [#1712], [#1857], [#1455] JPEG images errors fixer
- Ignore missing font on DefineEditText
- GFX: Drawing missing DefineExternalImage/2, DefineSubImage as red instead of throwing exception
- GFX: DefineExternalImage2 properly saving characterId
- Hex view refreshing after selecting Unknown tag
- [#1818], [#1727], [#1666] GFX: Importing XML
- GFX: Correct refreshing image when raw editing DefineExternalImage/2, DefineSubImage
- GFX: DefineExternalImage/2, DefineSubImage disallow not working replace button in favor of raw editing
- [#1795] AS3 P-code - optional (default parameter values) saving
- [#1785] AS1/2 try..catch block in for..in
- [#1770] Links in basictag info (like needed/dependent characters) were barely visible on most themes
- Show in Resource command from Hex dump not working for tags inside DefineSprite
- File did not appear modified when only header was modified
- Copy / Move to tag tree refreshing
- Preview of PlaceObject and ShowFrame in the Dump view
- FileAttributes tag exception in the Dump view
- Adding new frames did not set correct timelined to ShowFrame
- Computing dependent characters inside DefineSprite

### Changed
- [#1455] All tag types are now allowed inside DefineSprite

### Removed
- Auto fixing character tags order based on dependencies during saving

## [16.0.4] - 2022-11-03
### Fixed
- [#1860] FLA export - EmptyStackException during exporting MorphShape
- [#1782] FLA export - exporting from SWF files inside bundles (like binarysearch)
- Expand correct tree on SWF load
- [#1679] FLA export - MorphShapes (shape tween)
- [#1860], [#1732], [#1837] FLA export - AS3 - missing framescripts on the timeline
- Flash viewer - dropshadow filter hideobject(compositeSource) parameter

## [16.0.3] - 2022-11-02
### Fixed
- [#1817] PDF export - now storing JPEG images without recompression to PNG
- [#1816] PDF export - leaking temporary files when frame has embedded texts
- PDF export - reusing images when used as pattern vs standalone
- [#1859] AS3 P-code editing not working due to integer/long casting

## [16.0.2] - 2022-11-01
### Added
- Copy/move tag to for SWFs inside bundles and/or DefineBinaryData
- Replace button under shape and DefineSound display (previously, only context menu allowed that)

### Fixed
- SWF Add tag before/after menuitem
- Context menu on bundles (ZIP, SWC, binarysearch, etc...)
- Reloading SWF inside DefineBinaryData
- Working with byte ranges - caused problems when cloning tags
- All "mapped" tags have character id in parenthesis in the tag tree
- Raw editor now checks whether field value can be placed inside this kind of tag
- Refreshing parent tags and/or timelines on raw editor save
- Items could not be edited on taglist view (for example raw edit)

### Changed
- Do not show export name (class) in DoInitAction in Tag list view instead of tag name

## [16.0.1] - 2022-10-31
### Added
- Allow add tag after header context menu
- DefineScalingGrid has icon
- Adding tag "inside" allows setting character id to original when possible

### Fixed
- Do not show option to Show in taglist on resource view folders
- Disallow add tag before header context menu
- Context menu on tags mapped to other characters like DefineScalingGrid
- Add tag before/after for frame selection position
- Add tag (before/after/inside) refactored to more meaningful menus

### Changed
- Add tag renamed to Add tag inside
- Clone tag menuitem renamed to just Clone as it clones both tags and frames

## [16.0.0] - 2022-10-30
### Added
- Replace characters references
- Replace commandline action allows to load replacements list from a textfile
- SymbolClass export from commandline
- data-characterId and data-characterName tags to SVG export
- [#1731] Image viewer zoom support
- Cloning of tags and frames
- Changing tag position
- Tag list view
- Inserting new tags before and after selection
- [#1825], [#1737] Adding new frames
- Context menu icons
- Icon of tag in raw editor
- [#1845] Show warning on opening file in Read only mode (binary search, unknown extensions, etc.)
- [#1845] Show error message on saving in Read only mode, "Save As" must be used

### Fixed
- [#1834] PlaceObject4 tags appear as Unresolved inside of DefineSprite
- [#1839] Sprite frames exported incorrectly and repeating
- [#1838] AS3 - Properly handling of long unsigned values, hex values, default uint values etc.
- [#1847] Shape viewer and PDF exporter - correct drawing of pure vertical/horizontal shapes (zero width/height)
- Slow zooming/redrawing on action when SWF has low framerate
- Correct debug info label position/content on the top of flash viewer to avoid unwanted initial scroll
- [#1829] Adding extra pixel to the width and height when rendering items (for example to AVI)
- [#1828] Zero scale layer matrices support
- [#1828] Incorrect stroke scaling (normal/none/vertical/horizontal)
- [#1771] DefineShape4 line filled using single color
- Minimum stroke width should be 1 px
- [#1828] Closing path in shape strokes from last moveTo only
- Shape not clipped when clip area ouside of view
- Sound tag player now uses less memory / threads - does not use Clip sound class
- Freetransform tool dragging not always started on mousedown
- [#1695] Freetransform tool vs zooming
- [#1752] Freetransform tool on sprites with offset
- [#1711] DefineFont2-3 advance values need to be handled as unsigned (UI16)
- Leading of the font can be set to negative value
- Reset configuration button in advanced settings not working

### Changed
- AS3 integer values are internally (e.g. in the lib) handled as java int type instead of long.

## [15.1.1] - 2022-07-03
### Added
- Support for loading external images in DefineExternalImage2, DefineSubImage

### Changed
- Updated pt_BR translation
- XML import/export uses less memory

### Removed
- Auto downloading playerglobal.swf in the installer

### Fixed
- No longer working link to adobe dev downloads changed to its web-archived version

## [15.1.0] - 2022-02-20
### Added
- Display object depth in flash panel
- Show imported files on script import, able to cancel import
- [#270] AS3 show progress on deofuscating p-code
- [#1718] Show progress on injecting debug info / SWD generation (before Debugging)

### Changed
- [#1801] - Flex SDK links to Apache Flex

### Fixed
- [#1761] AS3 - try..finally inside another structure like if
- [#1762] AS call on integer numbers parenthesis
- [#1762] AS3 - Auto adding returnvoid/return undefined
- [#1762] AS - switch detection (mostcommon pathpart)
- [#1763] AS3 - initialization of activation object in some cases
- AS3 - direct editation - arguments object on method with activation
- AS3 - direct editation - bit not
- AS3 - direct editation - call on local register
- AS3 - direct editation - resolve properties and local regs before types
- AS3 - direct editation - call on index
- Incorrect position in Flash Player preview and SWF export
- AS1/2 actioncontainers (like try) inside ifs
- AS1/2 switch detection
- [#1766] AS3 - direct editation - namespaces on global level without leading colon
- [#1763] AS3 - function with activation - param assignment is not a declaration
- AS3 - insert debug instruction to mark register names even with activation
- AS3 - debugging in inner functions
- AS1/2 - debugger - rewinding playback to apply breakpoints
- [#1773] - Auto set flagWideCodes on FontInfo wide character adding
- [#1769] - Do not mark getter+setter as colliding (#xxx suffix)
- [#1801] - Flex SDK not required on commandline when Flex compilation is disabled
- Multiname - performance issues

## [15.0.0] - 2021-11-29
### Added
- Frame dependencies

### Changed
- AS1/2 direct editation no longer marked as experimental

### Fixed
- AS1/2 - switch with getvariable decompilation
- AS1/2 - call action parameters as string
- AS1/2 - direct editation - use actionadd instead of add2 on swfver < 5
- AS1/2 - tellTarget when single
- AS1/2 - use slash syntax in get/setvariable only in eval/set
- AS1/2 - get/setProperty when propertyindex is string
- DefineEditText - ampersand in link href
- AS1/2 - cannot use globalfunc/const variable names
- AS2 - class detection when no constructor found
- AS1/2 - subtract precedence
- AS2 - getters and setters decompilation and editing
- AS1/2 - definefunction2 suppresssuper parameter
- New version dialog error when no main window available
- AS1/2 direct editation - commands as expressions
- AS1/2 direct editation - delete operator on anything
- AS2 - class detection of top level classes
- AS2 - class detection - warning only if propertyname does not match getter/setter
- AS2 - some minor cases in class detection
- AS2 - class detection - ignore standalone directvalues
- AS1/2 - obfuscated name in forin cannot use eval
- AS1/2 - Ternar visit (can cause invalid reg declarations)
- AS1/2 - typeof precedence / parenthesis
- AS1/2 - switch detection
- AS1/2 - nested tellTarget
- AS1/2 - switch with nontrivial expressions like and/or,ternar (second pass)
- AS1/2 - ifFrameLoaded with nontrivial items inside
- AS1/2 - direct editation - (mb)length is expressioncommand, not a command
- AS1/2 - get/set top level properties
- AS1/2 - properties postincrement
- AS1/2 - direct editation - allow call on numbers, boolean, etc.
- AS1/2 - direct editation - try..finally without catch clause
- AS1/2 - GotoFrame2 - scene bias is first
- AS1/2 - direct editation - gotoAndPlay/Stop with scenebias
- AS1/2 - parenthesis around callfunction
- AS1/2 - deobfuscate function parameter names in registers
- AS1/2 - direct editation - do..while
- AS1/2 - newmethod proper brackets
- AS1/2 - class detection with ternars
- AS1/2 - empty tellTarget
- AS1/2 - deobfuscate object literal names
- AS1/2 - spacing in with statement
- Playercontrols frame display incorrect frame
- AS1/2 - direct editation - empty parenthesis nullpointer
- AS1/2 - delete on nonmember
- AS1/2 - direct editation - Infinity, NaN can be used as identifiers, are normal variables
- AS2 - obfuscated class attribute names
- AS1/2 - newobject deobfuscated name
- AS2 - obfuscated extends, implements
- AS1/2 - chained assignments with obfuscated/slash variables
- AS - direct editation - long integer values
- AS1/2 - on keypress key escaping
- AS1/2 - stop/play/etc. can be used in expressions, pushing undefined
- AS1/2 - startDrag constaint
- AS1/2 - gotoAndStop/play with simple label compiled as gotolabel

## [14.6.0] - 2021-11-22
### Added
- Information message before importing scripts, text, XML, Symbol-Class

### Fixed
- Japanese in english locales for Gotoaddress, addclass dialog
- AS1/2 DefineFunction cleaner
- AS1/2 direct editation - postincrement/decrement
- Reload menu disabled when no SWF selected
- AS2 - Do not detect classes inside functions
- AS1/2 - Slash syntax colon vs ternar operator collision
- AS1/2 - Allow nonstandard identifiers in object literal
- AS1/2 - Allow globalfunc names as variable identifiers
- AS1/2 - Registers in for..in clause, proper define
- AS1/2 - loops and switch break/continue vs definefunction
- AS1/2 - callmethod on register instead of callfunction on var
- AS1/2 - delete operator correct localreg names
- AS1/2 - temporary registers handling

## [14.5.2] - 2021-11-20
### Fixed
- AS1/2 handle declaration of registers in certain cases
- AS1/2 setProperty, getProperty handling
- [#1750] Application won't start when cannot access font file
- AS2 direct editation of classes - missing _global prefix

## [14.5.1] - 2021-11-20
### Fixed
- AS 1/2 - do not use eval function on obfuscated increment/decrement
- AS 1/2 direct editation - newline as "\n", not "\r"
- AS 1/2 allow various nonstandard names for definelocal
- AS 1/2 use DefineLocal in function instead of registers when eval, set is used
- AS 1/2 direct editation - delete operator parenthesis
- AS 1/2 direct editation - call function on eval
- AS 1/2 export selection of scripts in buttons, classes and similar

## [14.5.0] - 2021-11-19
### Added
- SoundStreamHead has associated sprite id in its name in the tagtree
- [#1485] Improved skins support, night mode
- [#1681] AS3 - context menu for adding classes on packages
- GFX: Support for loading external images
- Updated Japanese translation
- Try loading .gfx files if .swf failed to load for imports and similar
- [#1744] SVG shape import from commandline
- [#1496] repeat escape sequence `\{xx}C` to avoid long same char strings/names

### Fixed
- [#1687] Slow speed of cyclic tags detection
- CopyStream bug for copies smaller than the buffer size
- [#1748] Wrong matching of DefineEditText fonts for rendering
- [#1748] Line height - Descent/ascent in multiline DefineEditText
- Editation of font descent colliding with leading
- [#1741] AS1/2 direct editation - new String constructor call
- [#1726] Decompiling AS - missing break when on false branch vs continue on true
- AS3 jumps deobfuscator
- [#1699] AS1/2 detection of unitialized vars stuck
- [#1686] AS1/2 decompilation and editation of nested tellTarget
- [#1685] Generic tag editor - removing multiple items at once vs single item
- [#1684] Internal viewer - animated subsprites

## [14.4.0] - 2021-04-05
### Added
- [#1015], [#1466], [#1513] Better error messages during saving, display message on out of memory
- [#1657] Option to disable adding second quote/bracket/parenthesis
- Option to automatically show error dialog on every error
- [#1676] View video tags in external flash projector

### Fixed
- PDF export - NullPointer when font of text is missing
- PDF export - Text position on font change
- Writing DefineFont2/3 ascent/descent as SI16 - it's UI16
- [#1660] Empty thumbnail view on remove item
- [#1669] FILLSTYLE color handling in DefineShape3/4
- [#1668] Not removing SymbolClass/ExportAssets entry on character remove
- [#1670] Parent component/window of dialogs not properly set
- AS decompilation - Gotos handling vs and/or
- AS decompilation - certain combinations of ifs and switch
- AS3 jump deobfuscator - fix for try..catch clauses
- [#1669] DefineBitsJPEG3/4 alpha premultiplied
- [#1671] JPEG images display when not CMYK
- Generic tag editor - remove more items at once
- [#1669] Flash viewer - Smoothed vs non-smoothed bitmaps
- PDF export - Smoothed bitmaps
- Flash viewer - slow on larger zooms (now only diplayed rect is rendered)
- Flash viewer - scaling grid - ignore nonshapes when scaling
- [#1672] Raw editor - digits grouping causing incorrect cursor movement
- Rename invalid identifiers renames identifiers with a dollar sign
- [#1676] Messages on movie tags when Flash Player ActiveX not available
- [#1677] DefineFont2/3 - missing codeTableOffset if numGlyphs is zero and font has layout
- AS decompilation - §§push before loop
- [#1678] Removing AS3 class does not correctly clear cache

### Removed
- [#1678] Flash viewer - miter with clip support removed as it was not working correctly

## [14.3.1] - 2021-03-25
### Fixed
- "protected", "const", "namespace", "package" are not reserved keywords in AS1/2
- Not counting newlines in comments
- [#1665] Export selection not working for AS1/2 scripts other than frame scripts

## [14.3.0] - 2021-03-24
### Added
- AS3 - Remove trait which is outside class
- PDF vector export

### Fixed
- Flash viewer - bitmap stroke style, strokes scaling, cropped strokes
- Flash viewer - filters zooming
- Flash viewer - miter strokes
- SVG export - miter strokes as miter-clip style
- [#1660] Thumbnail view context menu Remove
- SVG export - bitmap stroke style
- Flash viewer - is visible flag
- Flash viewer - linear colorspace radial gradient
- Folder preview of frames with time increasing
- Flash viewer - Do not play StartSoundTag all over again on single frame
- Flash viewer - StartSoundTag loops
- Flash viewer - Sound envelope handling
- AS3 decompilation - inc/decrements handling - hiding some items
- Flash viewer - stop sounds when switching panels

### Changed
- [#1661] Slow rendering warning is optional with default to not display

## [14.2.1] - 2021-03-13
### Added
- Placeobject display and edit - raw editor on right side

### Fixed
- AS3 hilight and edit XML based on CData or comment only
- [#1435] Adding DefineScalingGrid to DefineSprite
- [#1488] SVG Export - EmptyStackException when clipping used
- [#1584] SVG Import - paths with horizontal/vertical lines and rotation
- [#1572] SVG Export - clipping must not use groups
- [#270] AS decompilation - switch in loop
- [#270] AS decompilation - loop followed by try
- [#270] AS decompilation - comma in ternar

## [14.2.0] - 2021-03-12
### Added
- [#1645] Scrollbar to recent searches dropdown
- [#1639] Clearing search results for current file
- [#1371] Go to character id (Ctrl+G in tag tree)
- [#1156] FLA, SVG, Canvas Export -  9 slice scaling (DefineScalingGrid)
- [#843] Compound assignments (like +=) decompilation and direct editation
- [#1221] Separate icons for different actionscript objects (class/interface/frame/)
- AS3 P-code - hilight and edit traits outside classes
- [#1585] SVG import - support for style tag (CSS)
- [#1585] SVG import - support for switch tag
- [#1122] SVG import - relative coordinates
(tests coords-units-01-b, coords-units-02-b, pservers-grad-10-b, pservers-grad-12-b)
- Preview in image file selection dialogs
- [#1541] XML Import/Export of Unknown tags
- Unknown tags display and binary contents replace

### Changed
- [#1471] Import script menuitem renamed to Import scripts.

### Fixed
- Exception when switching from nonribbon interface to ribbon
- [#1396], [#1254] FLA Export - AS3 frame scripts
- FLA Export - mutliple FrameLabel layers
- [#1636] Nullpointer exception on empty editorpane
- [#1156] Rendering - 9 slice scaling (DefineScalingGrid) clipping
- [#1647] Copying to clipboard - Transparency support
- Incorrect placeobject display (tag selection)
- Generic tag saving problem - timelined exception
- [#1332] Flash viewer - Show directly added images when placeFlagHasImage is true on AS3 swfs
- XML Import - not set SWF and Timelined internal values caused an exception on item display
- [#1636] Goto usage exception and incorrect trait position
- [#1648] Search - loaded search results mixed
- [#1650] Empty search results from history after reloading SWF file
- [#1651] FLA Export - mask layers
- [#1532] Rendering - clipping using transparent color

## [14.1.0] - 2021-03-05
### Added
- [#1561] Font editing - import ascent, descent, leading, kerning
- Font editing - font name, ascent, descent, leading
- PlaceObject tags matrix editation - FreeTransform tool (move, resize, rotate, shear)

### Fixed
- [#1623] Right side marker (gray line) in P-code
- [#1622] Slow scrolling (search results, advanced settings and others)
- [#1626] AS3 decompilation - unpopped obfuscated function
- [#1624] Saving last searches saves only first results
- [#1627] Previously decompiled scripts not cached
- SWF is not garbage collected on close in some situations
- AS1/2 script search does not show all results
- [#1633] AS3 decompilation - return in for..in clause
- AS3 p-code/AS hilighting when outside trait
- AS3 p-code/AS hilighting after p-code save
- Decompilation - Goto handling
- Not selecting proper script after restoring session
- [#1603] empty script after search selection
- Generic tag tree exception on save
- Copying to clipboard does not support transparency
- [#1634] AS3 slot/const editor loses focus on edit button press
- [#1636] Exception after search - traitslist with not properly set abc, other ui exception
- Flash viewer- cyclic DefineSprite usage
- [#1570] Incorrect shape rendering when edge is reversed
- [#1643] Separate AS1/2 and AS3 editor hilighting
- AS3 direct editation - slot/const default values
- [#1328] AS1/2/3 direct editation - empty commands (just semicolon)
- [#1310] AS1/2/3 direct editation - modulo operator precedence
- AS3 - escaping star import
- [#1298] AS1 colon syntax handling
- [#1298] AS1 direct editation of add,eq,ne and such operations
- [#1260] AS1/2 direct editation - is/as/:: are not reserved operators
- Goto declaration - exception when exists QName with 0 namespace index
- [#1179] FLA Export - button sounds
- FLA export - sound effects - fade in / out / left to right / right to left

### Removed
- [#1631] ActiveX Flash component download in windows installer

### Changed
- Spaces around ternar operators, parenthesis on ternar inside ternat

## [14.0.1] - 2021-02-26
### Added
- AS3 goto definition on imports

### Fixed
- [#1336] AS3 direct editation - Regexp / character escaping
- [#1615] Turning off Checking for modifications disables SWF loading
- [#1100], [#1123], [#1516] AS1/2/3 direct editation - comma operator
- [#1618] Export to PDF selectable text escaping and text size
- [#1101] AS3 direct editation - handling imported vars
- [#1169] AS1/2 direct editation - getmember after new operator
- [#1338], [#1480] AS3 direct editation - Vector in combination with activation
- AS3 decompilation - do not show setslot on activation when has same name as method parameter
- [#1450] AS3 direct editation - handling types from same package
- AS3 goto definition for types in another ABC tag
- AS3 goto definition for obfuscated names
- AS3 direct editation - compilation of top level classes
- [#1494] AS1/2 Direct editation - GetURL not properly saved caused by case
- AS1/2 Direct editation - functions case sensitivity

### Changed
- [#1616] Close SWF menuitem is last in the context menu
- [#1620] Search results - Using list component instead of tree when searching in single SWF

## [14.0.0] - 2021-02-24
### Added
- [#1202] Check for modifications outside FFDec and ask user to reload
- [#1155], [#1602] AS3 remove trait button
- [#1260], [#1438] AS1/2 direct editing on(xxx), onClipEvent(xxx) handlers
- [#1366], [#1409], [#1429], [#1573], [#1598] AS1/2/3 Add script/class (context menu on scripts folder)
- Removing BUTTONCONDACTION, CLIPACTIONRECORD
- Removing whole AS1/2 script folders (frame, DefineSprite, packages)
- Removing AS3 scripts and whole packages
- Japanese translation
- [#428], [#583], [#1373] Exporting PDFs with selectable text
- Goto address dialog in Hex view (Ctrl+G or via context menu)
- AS3 P-code editation checking all referenced labels exist
- [#1595] History of script search results per SWF
- Ignore case and RegExp options displayed on search results dialog
- [#1611] Warning about initializers has do not show again checkbox
- [#644] Scoped script text search
- Search across multiple SWFs
- [#1601] Option to hide AS3 docs panel and traitslist/constants panel

### Fixed
- [#1298] AS1/2 properly decompiled setProperty/getProperty
- AS1/2 Direct editation mark line on error
- Collapsing tag tree on SWF reload
- [#1339] AS1/2 direct editation - targetPath as an expression
- [#1467] AS1/2 direct editation - allow new Number call
- [#1489] AS1/2 direct editation - reversed negations
- [#1489] AS1/2 direct editation - for in loop
- [#1490], [#1493] AS1/2 direct editation - cast op
- AS1/2 cast op decompilation
- Only last DoInitAction tag displayed
- [#1606] Run/Debug SWF that is embedded (has no file associated)
- [#1270], [#1336] AS3 direct editation - unnecessary coerce in setproperty
- AS3 direct editation - unary minus (negate) compiled as 0 - value
- AS3 direct editation - using finally clause for continue and break
- AS3 direct editation - popscope in catch on continue and break
- [#1159], [#1608] Regexp syntax hilight when not a regexp (only division) again
- Graphviz Graph not showing AS3 exception end
- [#1609] First frame missing in frame to PDF export
- AS3 with statement decompilation
- [#1610] AS3 unnecessary adding namespaces
- [#1610] AS3 P-code editation - true/false/undefined/null has value_index same as value_kind
- Ribbon stealing focus when pressing Alt (for example in editors)
- Focused byte barely visible in hex view
- AS3 P-code editation - only first try offset was saved when multiple try with same label
- AS3 decompilation: try..catch..finally suborder when debugline info not present
- AS3 decompilation: increment/decrement on properties
- AS1/2 Goto search result not properly selecting line (delay)
- ActiveX exceptions when FlashPlayer disabled in classic GUI
- [#1569] AS3 direct editation - incorrect slot names handling (IndexOutOfBounds)
- [#1153], [#1347], [#1400], [#1552], [#1553] Images export for some nonstandard JPEGs

### Changed
- [#1565], [#1407], [#1350] On BinaryData SWF save, parent SWF is saved
- Mouseover / focused bytes in hexview displayed with border
  instead of background color change to improve readability
- [#692] Search results dialog Goto button does not close dialog

## [13.0.3] - 2021-02-12
### Added
- [#1594] Option to disable AS3 P-code indentation, label on separate line
- [#1594] Option to use old style of getlocalx, setlocalx with underscore in AS3 P-code
- [#1597] Option to use old style lookupswitch in AS3 P-code

### Fixed
- [#1114] Script search results dialogs closing on swf close
- [#1159] Regexp syntax hilight when not a regexp (only division)
- [#1227] AS3 avoid recursion (stackoverflow) caused by newfunction instruction
- [#1360] Precedence of increment/decrement operations
- [#1407] NullPointer on Save as in BinaryData SWF subtree
- [#1596] Infinite loop when sorting traits according to dependencies
- Cannot properly cancel script searching

## [13.0.2] - 2021-02-10
### Changed
- AS3 pcode - Use Undefined as default keyword for value kind

### Fixed
- AS1/2 script export to single file maintains script order
- [#1088] ECMA Number to string conversion
- AS3 getslot/setslot in certain situations
- [#1185] AS3 Incorrect imports in obfuscated files
- [#1186] Missing import when item is fully qualified
- [#1188] AS3 reorder traits if one slot/const references another

## [13.0.1] - 2021-02-09
### Fixed
- AS3 break loop in catch clause
- AS3 inner functions scope (setslot/getslot)
- AS3 p-code traits of bodys of inner methods
- AS3 getslot/setslot, getglobalscope instruction

## [13.0.0] - 2021-02-08
### Added
- Graphviz graphs colorized
- AS3: Show try graph heads in Graphviz distinguished
- [#341], [#1379] AS3: Support for scripts not using kill instruction
- AS3 method trait p-code indentation, (optional) instruction name padding
- AS3 editation of method body traits (slot/const only)

### Fixed
- Using new FFDec icon on Mac
- AS3: get/set slot for global scope
- AS3: Incorrect handling of strict equals operator in if vs switch resulting in §§pop
- Better goto detection/for continue
- Support for comma operator in switch case statements
- Losing script tree focus on script selection (disallowed walking tree with keyboard up/down)
- Proper window screen centering
- Graph dialog proper window size
- Graph dialog scroll speed increased
- AS3: return in finally
- AS3 docs not correctly displayed under p-code when metadata present
- Improper initialization of ActiveX component when Flash not available causing FFDec not start
- [#1206] Switch with multiple default clauses
- ASC2 §§push of function calls before returning from a method
- Support for ASC2 and swftools try..catch..finally block
- Dot parent operator not detected in some cases
- Namespaces handling
- Incorrectly colliding class names detection on script private classes
- AS3 deobfuscator of registers parsing of exception targets
- AS3 code with exception start/end not matching instruction boundary
- AS3 deobfuscator in some cases
- [#349] AS3 - better handling of declarations
- [#735] AS3 - index out of bounds in deobfuscator 
- AS3 deobfuscator on &&, || operators
- Merged continues in try..catch
- AS3 method display in GUI when method name is null
- [#1195] this keyword in functions outside class
- AS3 p-code parser adding ;trait comment to last instruction

### Changed
- AS3 test methods separated to classes
- AS3 p-code more RAbcDasm like (WARNING: Older versions cannot read new code!): 
- parenthesis after True/False/Undefined/Null trait kinds
- end after try
- commas in parameters list 
- lookupswitch caseoffsets in brackets
- get/setlocal_x renamed to get/setlocalx
- QName casing changed from Qname
- Void keyword instead of Undefined for optional parameters and slot/const values
- Not displaying slot/const value when Void

### Removed
- Code structure detection in Graphviz graphs as it was usually wrong

## [12.0.1] - 2021-01-14
### Fixed
- Critical fix - application GUI did not start on OSes without flashplayer (Linux, MacOS, even Windows)

## [12.0.0] - 2021-01-12
### Added
- Java 9+ support
- Chinese translation updated
- Enable bulk replace for single typed tags
- Option to use logging configuration file (ffdec home / logging.properties)

### Removed
- Due to Java9+ limited support of reflection, following features were removed:
- Automatic detection of installed fonts changes (on font editation) - FFDec needs to be restarted to font changes to take effect
- Using kerning pairs of installed fonts
- Support for installing java in FFDec windows installer

### Changed
- Making internal flash viewer a default viewer + move FP option switch to Advanced settings / others
- Increased scrolling speed in folder preview
- Changed /bin/bash to /usr/bin/env bash
- Building does not require Naashorn - uses Beanshell instead
- Use object.§§slot[index] syntax instead of /* UnknownSlot */ comment when slot cannot be determined (AS3)
- Show §§findproperty when neccessary (AS3)

### Fixed
- Scaling - Distorted images/canvas on Hi-dpi monitors for Java 9 and later
- Radial gradients focal point reading/writing
- Correct AS/P-code matching in editor for AS3 after using deobfuscation
- Correct line matching in debugger of AS3 after using deobfuscation
- Concurrent access while in debugger
- Correct body index for script initializer in P-code debugging
- [#1550] TTF export - correctly handle duplicate unicode codes
- [#1548] correctly handle empty generated file names
- [#1379] AS3 - better handling local registers postincrement/decrement
- Better unresolved if handling
- Escaping in P-code Graphviz exporter (Problems with graphs containing backslash strings)

## [11.3.0] - 2020-04-25
### Added
- Possibility to open SWF files using open on Mac
- Updated turkish translation

### Fixed
- [#1500] Maximum SWF version constant increased, which is used by the memory search and also in the header editor
- [#1457] AS3 switch without lookupswitch ins detection
- [#1457] pushing on stack before exit item (return/throw)
- [#1503] NullPointer Exception on commandline FLA export
- AS3 direct editation - invalid generation of lookupswitch
- AS3 direct editation - fix access to protected members with super
- [#1529] commandline selection of tag ids now applies to sprites and buttons

### Changed
- [#1378] Transparent background on PNG/GIF sprite export
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
- [#1415] freezing on manually closing Flash player debug session
- [#1484] AS import error printout on commandline (NullPointer)

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
### Fixed
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

[18.5.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version18.4.1...version18.5.0
[18.4.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version18.4.0...version18.4.1
[18.4.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version18.3.6...version18.4.0
[18.3.6]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version18.3.5...version18.3.6
[18.3.5]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version18.3.4...version18.3.5
[18.3.4]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version18.3.3...version18.3.4
[18.3.3]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version18.3.2...version18.3.3
[18.3.2]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version18.3.1...version18.3.2
[18.3.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version18.3.0...version18.3.1
[18.3.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version18.2.1...version18.3.0
[18.2.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version18.2.0...version18.2.1
[18.2.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version18.1.0...version18.2.0
[18.1.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version18.0.0...version18.1.0
[18.0.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version17.0.4...version18.0.0
[17.0.4]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version17.0.3...version17.0.4
[17.0.3]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version17.0.2...version17.0.3
[17.0.2]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version17.0.1...version17.0.2
[17.0.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version17.0.0...version17.0.1
[17.0.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version16.3.1...version17.0.0
[16.3.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version16.3.0...version16.3.1
[16.3.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version16.2.0...version16.3.0
[16.2.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version16.1.0...version16.2.0
[16.1.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version16.0.4...version16.1.0
[16.0.4]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version16.0.3...version16.0.4
[16.0.3]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version16.0.2...version16.0.3
[16.0.2]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version16.0.1...version16.0.2
[16.0.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version16.0.0...version16.0.1
[16.0.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version15.1.1...version16.0.0
[15.1.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version15.1.0...version15.1.1
[15.1.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version15.0.0...version15.1.0
[15.0.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version14.6.0...version15.0.0
[14.6.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version14.5.2...version14.6.0
[14.5.2]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version14.5.1...version14.5.2
[14.5.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version14.5.0...version14.5.1
[14.5.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version14.4.0...version14.5.0
[14.4.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version14.3.1...version14.4.0
[14.3.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version14.3.0...version14.3.1
[14.3.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version14.2.1...version14.3.0
[14.2.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version14.2.0...version14.2.1
[14.2.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version14.1.0...version14.2.0
[14.1.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version14.0.1...version14.1.0
[14.0.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version14.0.0...version14.0.1
[14.0.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version13.0.3...version14.0.0
[13.0.3]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version13.0.2...version13.0.3
[13.0.2]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version13.0.1...version13.0.2
[13.0.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version13.0.0...version13.0.1
[13.0.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version12.0.1...version13.0.0
[12.0.1]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version12.0.0...version12.0.1
[12.0.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version11.3.0...version12.0.0
[11.3.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version11.2.0...version11.3.0
[11.2.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version11.1.0...version11.2.0
[11.1.0]: https://github.com/jindrapetrik/jpexs-decompiler/compare/version11.0.0...version11.1.0
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
[#1998]: https://www.free-decompiler.com/flash/issues/1998
[#2038]: https://www.free-decompiler.com/flash/issues/2038
[#2028]: https://www.free-decompiler.com/flash/issues/2028
[#2034]: https://www.free-decompiler.com/flash/issues/2034
[#2036]: https://www.free-decompiler.com/flash/issues/2036
[#2004]: https://www.free-decompiler.com/flash/issues/2004
[#2008]: https://www.free-decompiler.com/flash/issues/2008
[#2007]: https://www.free-decompiler.com/flash/issues/2007
[#1993]: https://www.free-decompiler.com/flash/issues/1993
[#1994]: https://www.free-decompiler.com/flash/issues/1994
[#1477]: https://www.free-decompiler.com/flash/issues/1477
[#1991]: https://www.free-decompiler.com/flash/issues/1991
[#1999]: https://www.free-decompiler.com/flash/issues/1999
[#1996]: https://www.free-decompiler.com/flash/issues/1996
[#1888]: https://www.free-decompiler.com/flash/issues/1888
[#1892]: https://www.free-decompiler.com/flash/issues/1892
[#355]: https://www.free-decompiler.com/flash/issues/355
[#1682]: https://www.free-decompiler.com/flash/issues/1682
[#1981]: https://www.free-decompiler.com/flash/issues/1981
[#1982]: https://www.free-decompiler.com/flash/issues/1982
[#1986]: https://www.free-decompiler.com/flash/issues/1986
[#1989]: https://www.free-decompiler.com/flash/issues/1989
[#1990]: https://www.free-decompiler.com/flash/issues/1990
[#1970]: https://www.free-decompiler.com/flash/issues/1970
[#1972]: https://www.free-decompiler.com/flash/issues/1972
[#1973]: https://www.free-decompiler.com/flash/issues/1973
[#1974]: https://www.free-decompiler.com/flash/issues/1974
[#1963]: https://www.free-decompiler.com/flash/issues/1963
[#1977]: https://www.free-decompiler.com/flash/issues/1977
[#1979]: https://www.free-decompiler.com/flash/issues/1979
[#1959]: https://www.free-decompiler.com/flash/issues/1959
[#1960]: https://www.free-decompiler.com/flash/issues/1960
[#1964]: https://www.free-decompiler.com/flash/issues/1964
[#1961]: https://www.free-decompiler.com/flash/issues/1961
[#1968]: https://www.free-decompiler.com/flash/issues/1968
[#1971]: https://www.free-decompiler.com/flash/issues/1971
[#1957]: https://www.free-decompiler.com/flash/issues/1957
[#1029]: https://www.free-decompiler.com/flash/issues/1029
[#1948]: https://www.free-decompiler.com/flash/issues/1948
[#1941]: https://www.free-decompiler.com/flash/issues/1941
[#1951]: https://www.free-decompiler.com/flash/issues/1951
[#1953]: https://www.free-decompiler.com/flash/issues/1953
[#1954]: https://www.free-decompiler.com/flash/issues/1954
[#1944]: https://www.free-decompiler.com/flash/issues/1944
[#1940]: https://www.free-decompiler.com/flash/issues/1940
[#1955]: https://www.free-decompiler.com/flash/issues/1955
[#688]: https://www.free-decompiler.com/flash/issues/688
[#1913]: https://www.free-decompiler.com/flash/issues/1913
[#1894]: https://www.free-decompiler.com/flash/issues/1894
[#1801]: https://www.free-decompiler.com/flash/issues/1801
[#1936]: https://www.free-decompiler.com/flash/issues/1936
[#1937]: https://www.free-decompiler.com/flash/issues/1937
[#1458]: https://www.free-decompiler.com/flash/issues/1458
[#1842]: https://www.free-decompiler.com/flash/issues/1842
[#1938]: https://www.free-decompiler.com/flash/issues/1938
[#1949]: https://www.free-decompiler.com/flash/issues/1949
[#1935]: https://www.free-decompiler.com/flash/issues/1935
[#1931]: https://www.free-decompiler.com/flash/issues/1931
[#1934]: https://www.free-decompiler.com/flash/issues/1934
[#1929]: https://www.free-decompiler.com/flash/issues/1929
[#1932]: https://www.free-decompiler.com/flash/issues/1932
[#1933]: https://www.free-decompiler.com/flash/issues/1933
[#1905]: https://www.free-decompiler.com/flash/issues/1905
[#1915]: https://www.free-decompiler.com/flash/issues/1915
[#1923]: https://www.free-decompiler.com/flash/issues/1923
[#1827]: https://www.free-decompiler.com/flash/issues/1827
[#1926]: https://www.free-decompiler.com/flash/issues/1926
[#1772]: https://www.free-decompiler.com/flash/issues/1772
[#1890]: https://www.free-decompiler.com/flash/issues/1890
[#1922]: https://www.free-decompiler.com/flash/issues/1922
[#1921]: https://www.free-decompiler.com/flash/issues/1921
[#1917]: https://www.free-decompiler.com/flash/issues/1917
[#1424]: https://www.free-decompiler.com/flash/issues/1424
[#1473]: https://www.free-decompiler.com/flash/issues/1473
[#1835]: https://www.free-decompiler.com/flash/issues/1835
[#1852]: https://www.free-decompiler.com/flash/issues/1852
[#1914]: https://www.free-decompiler.com/flash/issues/1914
[#1916]: https://www.free-decompiler.com/flash/issues/1916
[#1920]: https://www.free-decompiler.com/flash/issues/1920
[#1231]: https://www.free-decompiler.com/flash/issues/1231
[#1690]: https://www.free-decompiler.com/flash/issues/1690
[#1460]: https://www.free-decompiler.com/flash/issues/1460
[#1909]: https://www.free-decompiler.com/flash/issues/1909
[#1910]: https://www.free-decompiler.com/flash/issues/1910
[#1912]: https://www.free-decompiler.com/flash/issues/1912
[#1901]: https://www.free-decompiler.com/flash/issues/1901
[#583]: https://www.free-decompiler.com/flash/issues/583
[#1858]: https://www.free-decompiler.com/flash/issues/1858
[#1904]: https://www.free-decompiler.com/flash/issues/1904
[#595]: https://www.free-decompiler.com/flash/issues/595
[#1908]: https://www.free-decompiler.com/flash/issues/1908
[#1907]: https://www.free-decompiler.com/flash/issues/1907
[#1906]: https://www.free-decompiler.com/flash/issues/1906
[#1898]: https://www.free-decompiler.com/flash/issues/1898
[#1511]: https://www.free-decompiler.com/flash/issues/1511
[#1765]: https://www.free-decompiler.com/flash/issues/1765
[#1884]: https://www.free-decompiler.com/flash/issues/1884
[#1564]: https://www.free-decompiler.com/flash/issues/1564
[#1676]: https://www.free-decompiler.com/flash/issues/1676
[#1697]: https://www.free-decompiler.com/flash/issues/1697
[#1893]: https://www.free-decompiler.com/flash/issues/1893
[#1181]: https://www.free-decompiler.com/flash/issues/1181
[#1900]: https://www.free-decompiler.com/flash/issues/1900
[#1897]: https://www.free-decompiler.com/flash/issues/1897
[#1006]: https://www.free-decompiler.com/flash/issues/1006
[#1828]: https://www.free-decompiler.com/flash/issues/1828
[#1796]: https://www.free-decompiler.com/flash/issues/1796
[#1895]: https://www.free-decompiler.com/flash/issues/1895
[#1896]: https://www.free-decompiler.com/flash/issues/1896
[#1769]: https://www.free-decompiler.com/flash/issues/1769
[#1810]: https://www.free-decompiler.com/flash/issues/1810
[#1891]: https://www.free-decompiler.com/flash/issues/1891
[#1887]: https://www.free-decompiler.com/flash/issues/1887
[#1882]: https://www.free-decompiler.com/flash/issues/1882
[#1880]: https://www.free-decompiler.com/flash/issues/1880
[#1881]: https://www.free-decompiler.com/flash/issues/1881
[#1870]: https://www.free-decompiler.com/flash/issues/1870
[#1871]: https://www.free-decompiler.com/flash/issues/1871
[#1875]: https://www.free-decompiler.com/flash/issues/1875
[#1280]: https://www.free-decompiler.com/flash/issues/1280
[#1743]: https://www.free-decompiler.com/flash/issues/1743
[#1822]: https://www.free-decompiler.com/flash/issues/1822
[#1803]: https://www.free-decompiler.com/flash/issues/1803
[#1501]: https://www.free-decompiler.com/flash/issues/1501
[#1680]: https://www.free-decompiler.com/flash/issues/1680
[#1820]: https://www.free-decompiler.com/flash/issues/1820
[#1869]: https://www.free-decompiler.com/flash/issues/1869
[#1872]: https://www.free-decompiler.com/flash/issues/1872
[#1692]: https://www.free-decompiler.com/flash/issues/1692
[#1757]: https://www.free-decompiler.com/flash/issues/1757
[#1807]: https://www.free-decompiler.com/flash/issues/1807
[#1840]: https://www.free-decompiler.com/flash/issues/1840
[#1877]: https://www.free-decompiler.com/flash/issues/1877
[#1879]: https://www.free-decompiler.com/flash/issues/1879
[#1867]: https://www.free-decompiler.com/flash/issues/1867
[#1868]: https://www.free-decompiler.com/flash/issues/1868
[#1649]: https://www.free-decompiler.com/flash/issues/1649
[#1701]: https://www.free-decompiler.com/flash/issues/1701
[#1864]: https://www.free-decompiler.com/flash/issues/1864
[#1863]: https://www.free-decompiler.com/flash/issues/1863
[#1865]: https://www.free-decompiler.com/flash/issues/1865
[#1846]: https://www.free-decompiler.com/flash/issues/1846
[#1678]: https://www.free-decompiler.com/flash/issues/1678
[#1414]: https://www.free-decompiler.com/flash/issues/1414
[#1755]: https://www.free-decompiler.com/flash/issues/1755
[#1465]: https://www.free-decompiler.com/flash/issues/1465
[#1862]: https://www.free-decompiler.com/flash/issues/1862
[#1735]: https://www.free-decompiler.com/flash/issues/1735
[#1459]: https://www.free-decompiler.com/flash/issues/1459
[#1832]: https://www.free-decompiler.com/flash/issues/1832
[#1849]: https://www.free-decompiler.com/flash/issues/1849
[#1432]: https://www.free-decompiler.com/flash/issues/1432
[#1232]: https://www.free-decompiler.com/flash/issues/1232
[#1712]: https://www.free-decompiler.com/flash/issues/1712
[#1857]: https://www.free-decompiler.com/flash/issues/1857
[#1455]: https://www.free-decompiler.com/flash/issues/1455
[#1818]: https://www.free-decompiler.com/flash/issues/1818
[#1727]: https://www.free-decompiler.com/flash/issues/1727
[#1666]: https://www.free-decompiler.com/flash/issues/1666
[#1795]: https://www.free-decompiler.com/flash/issues/1795
[#1785]: https://www.free-decompiler.com/flash/issues/1785
[#1770]: https://www.free-decompiler.com/flash/issues/1770
[#1860]: https://www.free-decompiler.com/flash/issues/1860
[#1782]: https://www.free-decompiler.com/flash/issues/1782
[#1679]: https://www.free-decompiler.com/flash/issues/1679
[#1732]: https://www.free-decompiler.com/flash/issues/1732
[#1837]: https://www.free-decompiler.com/flash/issues/1837
[#1817]: https://www.free-decompiler.com/flash/issues/1817
[#1816]: https://www.free-decompiler.com/flash/issues/1816
[#1859]: https://www.free-decompiler.com/flash/issues/1859
[#1731]: https://www.free-decompiler.com/flash/issues/1731
[#1825]: https://www.free-decompiler.com/flash/issues/1825
[#1737]: https://www.free-decompiler.com/flash/issues/1737
[#1845]: https://www.free-decompiler.com/flash/issues/1845
[#1834]: https://www.free-decompiler.com/flash/issues/1834
[#1839]: https://www.free-decompiler.com/flash/issues/1839
[#1838]: https://www.free-decompiler.com/flash/issues/1838
[#1847]: https://www.free-decompiler.com/flash/issues/1847
[#1829]: https://www.free-decompiler.com/flash/issues/1829
[#1771]: https://www.free-decompiler.com/flash/issues/1771
[#1695]: https://www.free-decompiler.com/flash/issues/1695
[#1752]: https://www.free-decompiler.com/flash/issues/1752
[#1711]: https://www.free-decompiler.com/flash/issues/1711
[#270]: https://www.free-decompiler.com/flash/issues/270
[#1718]: https://www.free-decompiler.com/flash/issues/1718
[#1761]: https://www.free-decompiler.com/flash/issues/1761
[#1762]: https://www.free-decompiler.com/flash/issues/1762
[#1763]: https://www.free-decompiler.com/flash/issues/1763
[#1766]: https://www.free-decompiler.com/flash/issues/1766
[#1773]: https://www.free-decompiler.com/flash/issues/1773
[#1750]: https://www.free-decompiler.com/flash/issues/1750
[#1485]: https://www.free-decompiler.com/flash/issues/1485
[#1681]: https://www.free-decompiler.com/flash/issues/1681
[#1744]: https://www.free-decompiler.com/flash/issues/1744
[#1496]: https://www.free-decompiler.com/flash/issues/1496
[#1687]: https://www.free-decompiler.com/flash/issues/1687
[#1748]: https://www.free-decompiler.com/flash/issues/1748
[#1741]: https://www.free-decompiler.com/flash/issues/1741
[#1726]: https://www.free-decompiler.com/flash/issues/1726
[#1699]: https://www.free-decompiler.com/flash/issues/1699
[#1686]: https://www.free-decompiler.com/flash/issues/1686
[#1685]: https://www.free-decompiler.com/flash/issues/1685
[#1684]: https://www.free-decompiler.com/flash/issues/1684
[#1015]: https://www.free-decompiler.com/flash/issues/1015
[#1466]: https://www.free-decompiler.com/flash/issues/1466
[#1513]: https://www.free-decompiler.com/flash/issues/1513
[#1657]: https://www.free-decompiler.com/flash/issues/1657
[#1660]: https://www.free-decompiler.com/flash/issues/1660
[#1669]: https://www.free-decompiler.com/flash/issues/1669
[#1668]: https://www.free-decompiler.com/flash/issues/1668
[#1670]: https://www.free-decompiler.com/flash/issues/1670
[#1671]: https://www.free-decompiler.com/flash/issues/1671
[#1672]: https://www.free-decompiler.com/flash/issues/1672
[#1677]: https://www.free-decompiler.com/flash/issues/1677
[#1665]: https://www.free-decompiler.com/flash/issues/1665
[#1661]: https://www.free-decompiler.com/flash/issues/1661
[#1435]: https://www.free-decompiler.com/flash/issues/1435
[#1488]: https://www.free-decompiler.com/flash/issues/1488
[#1584]: https://www.free-decompiler.com/flash/issues/1584
[#1572]: https://www.free-decompiler.com/flash/issues/1572
[#1645]: https://www.free-decompiler.com/flash/issues/1645
[#1639]: https://www.free-decompiler.com/flash/issues/1639
[#1371]: https://www.free-decompiler.com/flash/issues/1371
[#1156]: https://www.free-decompiler.com/flash/issues/1156
[#843]: https://www.free-decompiler.com/flash/issues/843
[#1221]: https://www.free-decompiler.com/flash/issues/1221
[#1585]: https://www.free-decompiler.com/flash/issues/1585
[#1122]: https://www.free-decompiler.com/flash/issues/1122
[#1541]: https://www.free-decompiler.com/flash/issues/1541
[#1471]: https://www.free-decompiler.com/flash/issues/1471
[#1396]: https://www.free-decompiler.com/flash/issues/1396
[#1254]: https://www.free-decompiler.com/flash/issues/1254
[#1636]: https://www.free-decompiler.com/flash/issues/1636
[#1647]: https://www.free-decompiler.com/flash/issues/1647
[#1332]: https://www.free-decompiler.com/flash/issues/1332
[#1648]: https://www.free-decompiler.com/flash/issues/1648
[#1650]: https://www.free-decompiler.com/flash/issues/1650
[#1651]: https://www.free-decompiler.com/flash/issues/1651
[#1532]: https://www.free-decompiler.com/flash/issues/1532
[#1561]: https://www.free-decompiler.com/flash/issues/1561
[#1623]: https://www.free-decompiler.com/flash/issues/1623
[#1622]: https://www.free-decompiler.com/flash/issues/1622
[#1626]: https://www.free-decompiler.com/flash/issues/1626
[#1624]: https://www.free-decompiler.com/flash/issues/1624
[#1627]: https://www.free-decompiler.com/flash/issues/1627
[#1633]: https://www.free-decompiler.com/flash/issues/1633
[#1603]: https://www.free-decompiler.com/flash/issues/1603
[#1634]: https://www.free-decompiler.com/flash/issues/1634
[#1570]: https://www.free-decompiler.com/flash/issues/1570
[#1643]: https://www.free-decompiler.com/flash/issues/1643
[#1328]: https://www.free-decompiler.com/flash/issues/1328
[#1310]: https://www.free-decompiler.com/flash/issues/1310
[#1298]: https://www.free-decompiler.com/flash/issues/1298
[#1260]: https://www.free-decompiler.com/flash/issues/1260
[#1179]: https://www.free-decompiler.com/flash/issues/1179
[#1631]: https://www.free-decompiler.com/flash/issues/1631
[#1336]: https://www.free-decompiler.com/flash/issues/1336
[#1615]: https://www.free-decompiler.com/flash/issues/1615
[#1100]: https://www.free-decompiler.com/flash/issues/1100
[#1123]: https://www.free-decompiler.com/flash/issues/1123
[#1516]: https://www.free-decompiler.com/flash/issues/1516
[#1618]: https://www.free-decompiler.com/flash/issues/1618
[#1101]: https://www.free-decompiler.com/flash/issues/1101
[#1169]: https://www.free-decompiler.com/flash/issues/1169
[#1338]: https://www.free-decompiler.com/flash/issues/1338
[#1480]: https://www.free-decompiler.com/flash/issues/1480
[#1450]: https://www.free-decompiler.com/flash/issues/1450
[#1494]: https://www.free-decompiler.com/flash/issues/1494
[#1616]: https://www.free-decompiler.com/flash/issues/1616
[#1620]: https://www.free-decompiler.com/flash/issues/1620
[#1202]: https://www.free-decompiler.com/flash/issues/1202
[#1155]: https://www.free-decompiler.com/flash/issues/1155
[#1602]: https://www.free-decompiler.com/flash/issues/1602
[#1438]: https://www.free-decompiler.com/flash/issues/1438
[#1366]: https://www.free-decompiler.com/flash/issues/1366
[#1409]: https://www.free-decompiler.com/flash/issues/1409
[#1429]: https://www.free-decompiler.com/flash/issues/1429
[#1573]: https://www.free-decompiler.com/flash/issues/1573
[#1598]: https://www.free-decompiler.com/flash/issues/1598
[#428]: https://www.free-decompiler.com/flash/issues/428
[#1373]: https://www.free-decompiler.com/flash/issues/1373
[#1595]: https://www.free-decompiler.com/flash/issues/1595
[#1611]: https://www.free-decompiler.com/flash/issues/1611
[#644]: https://www.free-decompiler.com/flash/issues/644
[#1601]: https://www.free-decompiler.com/flash/issues/1601
[#1339]: https://www.free-decompiler.com/flash/issues/1339
[#1467]: https://www.free-decompiler.com/flash/issues/1467
[#1489]: https://www.free-decompiler.com/flash/issues/1489
[#1490]: https://www.free-decompiler.com/flash/issues/1490
[#1493]: https://www.free-decompiler.com/flash/issues/1493
[#1606]: https://www.free-decompiler.com/flash/issues/1606
[#1270]: https://www.free-decompiler.com/flash/issues/1270
[#1159]: https://www.free-decompiler.com/flash/issues/1159
[#1608]: https://www.free-decompiler.com/flash/issues/1608
[#1609]: https://www.free-decompiler.com/flash/issues/1609
[#1610]: https://www.free-decompiler.com/flash/issues/1610
[#1569]: https://www.free-decompiler.com/flash/issues/1569
[#1153]: https://www.free-decompiler.com/flash/issues/1153
[#1347]: https://www.free-decompiler.com/flash/issues/1347
[#1400]: https://www.free-decompiler.com/flash/issues/1400
[#1552]: https://www.free-decompiler.com/flash/issues/1552
[#1553]: https://www.free-decompiler.com/flash/issues/1553
[#1565]: https://www.free-decompiler.com/flash/issues/1565
[#1407]: https://www.free-decompiler.com/flash/issues/1407
[#1350]: https://www.free-decompiler.com/flash/issues/1350
[#692]: https://www.free-decompiler.com/flash/issues/692
[#1594]: https://www.free-decompiler.com/flash/issues/1594
[#1597]: https://www.free-decompiler.com/flash/issues/1597
[#1114]: https://www.free-decompiler.com/flash/issues/1114
[#1227]: https://www.free-decompiler.com/flash/issues/1227
[#1360]: https://www.free-decompiler.com/flash/issues/1360
[#1596]: https://www.free-decompiler.com/flash/issues/1596
[#1088]: https://www.free-decompiler.com/flash/issues/1088
[#1185]: https://www.free-decompiler.com/flash/issues/1185
[#1186]: https://www.free-decompiler.com/flash/issues/1186
[#1188]: https://www.free-decompiler.com/flash/issues/1188
[#341]: https://www.free-decompiler.com/flash/issues/341
[#1379]: https://www.free-decompiler.com/flash/issues/1379
[#1206]: https://www.free-decompiler.com/flash/issues/1206
[#349]: https://www.free-decompiler.com/flash/issues/349
[#735]: https://www.free-decompiler.com/flash/issues/735
[#1195]: https://www.free-decompiler.com/flash/issues/1195
[#1550]: https://www.free-decompiler.com/flash/issues/1550
[#1548]: https://www.free-decompiler.com/flash/issues/1548
[#1500]: https://www.free-decompiler.com/flash/issues/1500
[#1457]: https://www.free-decompiler.com/flash/issues/1457
[#1503]: https://www.free-decompiler.com/flash/issues/1503
[#1529]: https://www.free-decompiler.com/flash/issues/1529
[#1378]: https://www.free-decompiler.com/flash/issues/1378
[#1415]: https://www.free-decompiler.com/flash/issues/1415
[#1484]: https://www.free-decompiler.com/flash/issues/1484
[#1449]: https://www.free-decompiler.com/flash/issues/1449
[#1456]: https://www.free-decompiler.com/flash/issues/1456
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
[#907]: https://www.free-decompiler.com/flash/issues/907
[#1311]: https://www.free-decompiler.com/flash/issues/1311
[#1313]: https://www.free-decompiler.com/flash/issues/1313
[#1189]: https://www.free-decompiler.com/flash/issues/1189
[#1274]: https://www.free-decompiler.com/flash/issues/1274
[#1275]: https://www.free-decompiler.com/flash/issues/1275
[#1278]: https://www.free-decompiler.com/flash/issues/1278
[#1281]: https://www.free-decompiler.com/flash/issues/1281
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
[#337]: https://www.free-decompiler.com/flash/issues/337
[#584]: https://www.free-decompiler.com/flash/issues/584
[#576]: https://www.free-decompiler.com/flash/issues/576
[#250]: https://www.free-decompiler.com/flash/issues/250
[#580]: https://www.free-decompiler.com/flash/issues/580
[#510]: https://www.free-decompiler.com/flash/issues/510
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
[#27]: https://www.free-decompiler.com/flash/issues/27
