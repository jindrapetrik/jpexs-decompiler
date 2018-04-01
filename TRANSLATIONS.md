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
3. Create new issue in [issue tracker](https://www.free-decompiler.com/flash/issues/) containing your new language name + code. (You should register first)
4. Download `Language pack for translators (zipped)` from latest (including nightly) version on [releases page](https://github.com/jindrapetrik/jpexs-decompiler/releases)
5. The archive contains all language files for newest version of FFDec. Each language in this pack has files with its own suffix which is standard language code.
6. Extract Language pack ZIP file
7. Copy each `.properties` file without `_xx` suffix (english), to new file which has your language code suffix (`_cs` is for czech, etc...)
8. Open `.properties` files with an editor. (`.properties` editor bundled with some Java IDE is better than standard text editor)
9. In order to `.properties` to work in FFDec, all nonascii characters should be replaced with unicode escapes (like `\u1234`). IDE editors like Netbeans do this automatically. If you have classic text editor, you can skip this phase, I will do it later myself.
10. Don't forget to place your name in `AboutDialog_xx.properties` file.
11. Attach translated files to that issue.
12. Goto [Issue #354] and look for new translated strings.
12. Subscribe to [Issue #354] to be notified about new strings in the future.
13. Wait for next release where your translation will be included.

[Issue #354]: https://www.free-decompiler.com/flash/issues/354-new-translations
