# How to contribute

## Bug reporting and feature requests
If you encounter a problem in the program, you should report it in our [issue tracker](https://www.free-decompiler.com/flash/issues/).
If you want us to implement a new feature in the app, use the issue tracker too.
The decompiler is opensource, you can implement the new features by yourself, if you know Java (See below)

## Pull requests
You can fork our decompiler on GitHub, modify FFDec code, and then create pull request.
Pull request target branch should be `dev`, as this is main development branch.

## GUI vs Library
Make sure library part (libsrc/ffdec_lib) does not depend on GUI and does not contain anything GUI related.

## Code formatting
Your code should pass our checkstyle validation.
To validate the code run:
```
ant checkstyle
```
It also will be executed via GitHub Actions when pushed.

## Tests
Make sure existing tests pass.

## Translations to other languages
For information about localizations to other languages and how to contribute, see [TRANSLATIONS.md](TRANSLATIONS.md) file
