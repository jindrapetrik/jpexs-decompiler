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

## New translation
If you would like to translate FFDec to your language, please follow these steps:

1. Check whether your language is not already present in the development branch:
[dev/TRANSLATIONS.md](https://github.com/jindrapetrik/jpexs-decompiler/blob/dev/TRANSLATIONS.md)
2. Find out your language code (See [table](http://www.loc.gov/standards/iso639-2/php/code_list.php) )
4. Download `Language pack for translators (zipped)` from latest (including nightly) version on [releases page](https://github.com/jindrapetrik/jpexs-decompiler/releases)
5. The archive contains all language files for newest version of FFDec. Each language in this pack has files with its own suffix which is standard language code.
6. Extract Language pack ZIP file
7. Copy each `.properties` file without `_xx` suffix (english), to new file which has your language code suffix (`_cs` is for czech, etc...)
8. Open `.properties` files with an editor. (`.properties` editor bundled with some Java IDE is better than standard text editor)
9. In order to `.properties` to work in FFDec, all nonascii characters should be replaced with unicode escapes (like `\u1234`). IDE editors like Netbeans do this automatically. If you have classic text editor, you can skip this phase, I will do it later myself.
10. Don't forget to place your name in `AboutDialog_xx.properties` file.
11. Create branch from `dev` and place .properties files to correct locations. TODO: specify what's correct location
12. Create pull request
