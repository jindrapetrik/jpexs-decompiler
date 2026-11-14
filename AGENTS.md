# FFDec project instructions

## Localization strings

- Always append new localization entries to the end of the corresponding `.properties` files.
- Before adding entries, check the latest git tag matching `versionX.Y.Z` and use its actual version number.
- Add a `#after X.Y.Z` comment before new entries only if the same comment does not already exist anywhere in that file. Here, `X.Y.Z` is the version from the latest matching tag without the `version` prefix. This indicates that the following strings were added after that release. Keep only one such comment per version in each file; if it already exists, append the new entries without repeating it.
- Always provide all new strings in English, Czech, Slovak, and German. Append the same keys to the end of each of these language files, applying the version comment rule separately to each file.

## Configuration

- Always append new configuration entries in `Configuration` to the end of the list.
