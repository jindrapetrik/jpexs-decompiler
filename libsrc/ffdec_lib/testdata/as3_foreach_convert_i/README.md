# as3_foreach_convert_i

Minimal SWF used by `ActionScript3ForEachConvertIDecompileTest`.

ASC emits `convert_i` after `nextvalue` for a typed for-each variable
(`for each (var n:int in values)`). That type must appear on the decompiled
for-each variable; otherwise recompilation drops the truncation and the
loop body sees raw `Number` values.

`TestForEachConvertIUse` covers the related pattern where the source uses an
explicit `int(...)` at the use site (`convert_i` after `getlocal`). That form
must keep `int(n)` in the body rather than inventing a typed for-each var.

## Rebuild

```bash
amxmlc -swf-version=43 -debug=true \
  -source-path+=src -output=bin/as3_foreach_convert_i.swf src/Main.as
```
