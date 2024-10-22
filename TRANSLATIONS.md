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

1. Download latest nightly version of FFDec to have the latest english strings.
2. In the FFDec installation directory, run FFDec Translator:
  `translator.exe`, `translator.bat`, `translator.sh` or `java -jar ffdec.jar -translator` will do
3. Use GUI editor to edit existing translations and/or add new Locale
4. If you create brand new locale, you will be asked for its code
 See [table](http://www.loc.gov/standards/iso639-2/php/code_list.php) (ISO 639-1 Code) for available codes.
5. When you are ready, use `Export JPT` button to export modified strings to an archive (.jpt extension)
6. Send that archive to us, you can use either 
 - Issue tracker (preferred) - Search for an issue with title `Translation: xxxx`, create new if it does not exist
 - or e-mail it to `jindra.petrik@gmail.com`
7. There's no need to translate everything. Some resource packs are HUGE (like `docs/pcode/AS3`) and generally not needed.
 For `AdvancedSettingDialog` you don't need to translate config descriptions (`config.description.*` keys).

## Translating NSIS installer files
Windows NSIS installer is translated separately (No translation tool for it),
See list of existing NSIS languages [here](https://github.com/jindrapetrik/jpexs-decompiler/tree/dev/nsis_locales)
and create new `.nsh` file for new installer language.