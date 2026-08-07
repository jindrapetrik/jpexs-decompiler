# as3_embed_dollar_hyphen_names

Minimal SWF used by `ActionScript3EmbedDollarHyphenNamesTest`.

## What this reproduces

`amxmlc` `[Embed]` of a `.swf` invents class names such as:

```text
loading_screen_swf$<md5>-<int>
loading_screen_swf$<md5>-<int>ByteArray
```

`$` is legal in AS3; the **hyphen** is not. FFDec always renames these to the
stem (`loading_screen_swf` / `loading_screen_swf_ByteArray`) instead of
`§…§` or `_SafeCls_N`, including in `[Embed(source=…)]` asset paths.
This is independent of "Deobfuscate identifiers" (compiler-generated, not obfuscated).

## Rebuild

```bash
amxmlc -swf-version=43 -debug=false \
  -output=assets/loading_screen.swf asset_src/Dummy.as

amxmlc -swf-version=43 -debug=false \
  -output=bin/as3_embed_dollar_hyphen_names.swf src/Main.as
```
