# Translations

## Available languages
 FFDec contains these translations:
 - English
 - Catalan
 - Czech
 - Chinese
 - Dutch
 - French
 - German
 - Hungarian
 - Italian
 - Japanese
 - Polish
 - Portugese - Portugal
 - Portugese - Brasilian
 - Russian
 - Spanish
 - Swedish
 - Turkish
 - Ukrainian

## Help translating
If you would like to translate FFDec to your language, please follow these steps:

1. Check whether your language already exists in the list.

   If yes, then:
     - Go to our [issue tracker]
     - Search there for existing issue with the translation.
     - If you find the translation issue, go to it and discuss there (after registration) that you want to continue translation.
     - DO NOT CONTINUE TRANSLATION UNTIL SOMEBODY CONFIRMS YOU CAN, otherwise there can be translation text conflicts
     - Subscribe to the issue to receive email notifications
       
   If it's new language, then:
     - Go to our [issue tracker]
     - Register there and create a new issue with the title Translation: XXX, where XXX is language name
       
3. Download (always) latest nightly version of FFDec to have the latest english strings.
4. In the FFDec installation directory, run FFDec Translator:
  `translator.exe`, `translator.bat`, `translator.sh` or `java -jar ffdec.jar -translator` will do
5. Use GUI editor to edit existing translations and/or add new Locale
6. If you create brand new locale, you will be asked for its code
 See [table](http://www.loc.gov/standards/iso639-2/php/code_list.php) (ISO 639-1 Code) for available codes.
7. When you are ready, use `Export JPT` button to export modified strings to an archive (.jpt extension)
8. Send that archive to the issue from the step 1
9. There's no need to translate everything. Some resource packs are HUGE (like `docs/pcode/AS3`) and generally not needed.
 For `AdvancedSettingDialog` you don't need to translate config descriptions (`config.description.*` keys).

## Translating NSIS installer files
Windows NSIS installer is translated separately (No translation tool for it),
See list of existing NSIS languages [here](https://github.com/jindrapetrik/jpexs-decompiler/tree/dev/nsis_locales)
and create new `.nsh` file for new installer language.

[issue tracker]: https://www.free-decompiler.com/flash/
