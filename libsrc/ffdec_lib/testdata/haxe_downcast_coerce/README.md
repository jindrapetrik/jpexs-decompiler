# haxe_downcast_coerce

Minimal SWF used by `ActionScript3HaxeDowncastCoerceDecompileTest`.

Haxe compiles an unchecked `cast` to a subtype, such as `var c:Circle = cast s;`
with `s:Shape`, as `coerce Circle`. A coerce to a class type was always printed
as an implicit coercion, but AS3 does not do such a downcast implicitly
("Implicit coercion of a value with static type Shape to a possibly unrelated
type Circle"). It is now printed as an explicit `Circle(s)` cast when the value
type is known and is neither the type nor one of its subtypes.

## Rebuild

```bash
haxe build.hxml
```

Built with Haxe 4.3.7.
