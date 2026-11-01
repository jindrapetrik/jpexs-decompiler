# FFDec project instructions

## Localization strings

- Always append new localization entries to the end of the corresponding `.properties` files.
- Before adding entries, check the latest git tag matching `versionX.Y.Z` and use its actual version number.
- Precede each newly added block of entries with a `#after X.Y.Z` comment, where `X.Y.Z` is the version from that tag without the `version` prefix. This indicates that the following strings were added after that release.
- Always provide all new strings in English, Czech, Slovak, and German. Append the same keys and version comment to the end of each of these language files.

## Configuration

- Always append new configuration entries in `Configuration` to the end of the list.
