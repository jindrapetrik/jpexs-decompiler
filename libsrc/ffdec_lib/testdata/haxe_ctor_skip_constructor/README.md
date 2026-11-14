# haxe_ctor_skip_constructor

Minimal SWF used by `ActionScript3ClassTest.testHaxeCtorSkipConstructor`.

When a program uses `Type.createEmptyInstance`, Haxe starts every constructor
with `if (flash.Boot.skip_constructor) return;`, before the `super()` call.
AS3 does not allow `super()` after a `return`, so the constructor printed as is
did not compile. It now prints as `if (!Boot.skip_constructor) { super(); ... }`.

## Rebuild

```bash
haxe build.hxml
```

Built with Haxe 4.3.7.
