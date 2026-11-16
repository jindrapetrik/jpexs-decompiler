# haxe_shadowed_field

Minimal SWF used by `ActionScript3HaxeShadowedFieldDecompileTest`.

Haxe reads and writes a field through the scope chain, as in
`findproperty key; getlocal1; initproperty key` for `this.key = key`. With the
parameter names (`-debug`, and FFDec's `paramNamesEnable`), that printed as
`key = key`, which assigns the parameter in ActionScript. Such a property of the
class is now qualified with `this.` (or its class name for a static one) when a
local variable of the method has its name.

## Rebuild

```bash
haxe build.hxml
```

Built with Haxe 4.3.7.
