# as3_swf_metadata

Minimal SWF used by `ActionScript3SwfMetadataTest`.

`[SWF(width, height, backgroundColor, frameRate)]` is compile-time only and is
**not** stored in ABC. FFDec reconstructs it on the document class from the SWF
header so recompilation keeps the stage size / frame rate.

## Rebuild

```bash
amxmlc -swf-version=43 -debug=false \
  -output=bin/as3_swf_metadata.swf src/Main.as
```

(`[SWF(...)]` on `Main` sets the header; equivalent CLI flags are
`-default-size` / `-default-frame-rate` / `-default-background-color`.)
