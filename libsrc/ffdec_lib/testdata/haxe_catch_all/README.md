# haxe_catch_all

Minimal SWF used by `ActionScript3HaxeCatchAllDecompileTest`.

Haxe compiles `catch (e:Dynamic)` (and every typed catch, which it dispatches
inside) as an exception entry without type and name, like an ASC `finally`,
whose target is the catch body. As no finally kind matched, it was printed as
an unnamed catch whose body was the part after the target: the real catch body
was lost, and the code after the `try` was printed in the catch instead.

## Rebuild

```bash
haxe build.hxml
```

Built with Haxe 4.3.7.
