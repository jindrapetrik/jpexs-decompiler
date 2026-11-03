# as3_undefined_local_coerce_type

Minimal SWF used by `ActionScript3UndefinedLocalCoerceTypeDecompileTest`.

ASC declares a typed local without initializer (`var chain:Vector.<String>;`)
as `pushundefined; setlocal`, then coerces every assignment. The declaration
only saw `undefined`, so it was printed as `var chain:* = undefined;` and
lost its type (Starling's `EventDispatcher.bubbleEvent`, whose
`chain[i].invokeEvent(event)` then became a dynamic call). An untyped
`var untyped;` is the same `pushundefined`, but its assignments do not
coerce, so it must stay `*`.

`TestNumericDefaultLocalCoerceType` covers `var cv:uint;`, which ASC sets to
`pushbyte 0` (an int) before each assignment converts it with `convert_u`.
It was printed as `var cv:* = 0;`, so `cv > 2` compared a negative int
instead of a large uint (AGALMiniAssembler's write masks and swizzles).

## Rebuild

```bash
amxmlc -swf-version=43 -debug=true \
  -source-path+=src -output=bin/as3_undefined_local_coerce_type.swf src/Main.as
```
