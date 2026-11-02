# as3_undefined_local_coerce_type

Minimal SWF used by `ActionScript3UndefinedLocalCoerceTypeDecompileTest`.

ASC declares a typed local without initializer (`var chain:Vector.<String>;`)
as `pushundefined; setlocal`, then coerces every assignment. The declaration
only saw `undefined`, so it was printed as `var chain:* = undefined;` and
lost its type (Starling's `EventDispatcher.bubbleEvent`, whose
`chain[i].invokeEvent(event)` then became a dynamic call). An untyped
`var untyped;` is the same `pushundefined`, but its assignments do not
coerce, so it must stay `*`.

## Rebuild

```bash
amxmlc -swf-version=43 -debug=true \
  -source-path+=src -output=bin/as3_undefined_local_coerce_type.swf src/Main.as
```
