# haxe_returnvoid_typed

Minimal SWF used by `ActionScript3HaxeReturnVoidTypedDecompileTest`.

Haxe compiles an exhaustive `switch` on an enum to a `lookupswitch` whose
default case is a `returnvoid`, also in a function that returns a value. It
was printed as `return;`, which AS3 does not allow there ("Function does not
return a value"). A `returnvoid` in a function with a return type other than
`void` and `*` is now printed as `return undefined;`, which returns the same.

## Rebuild

```bash
haxe build.hxml
```

Built with Haxe 4.3.7.
