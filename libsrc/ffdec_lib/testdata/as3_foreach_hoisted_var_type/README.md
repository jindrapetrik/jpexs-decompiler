# as3_foreach_hoisted_var_type

Minimal SWF used by `ActionScript3ForEachHoistedVarTypeDecompileTest`.

A for-each variable declared before the loop (`var node:XML; for each (node in
list)`) is first set by ASC to `null` without a coerce, then `coerce XML` after
`nextvalue`. The declaration only saw the `null`, so it was printed as
`var node:* = null;`, which loses the type the loop body relies on (E4X access
on `node` is then untyped). The coerce after `nextvalue` must type it, as it
already does for a for-each that declares its variable inline.

## Rebuild

```bash
amxmlc -swf-version=43 -debug=true \
  -source-path+=src -output=bin/as3_foreach_hoisted_var_type.swf src/Main.as
```
