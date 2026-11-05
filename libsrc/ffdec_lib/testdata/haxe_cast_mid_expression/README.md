# haxe_cast_mid_expression

Minimal SWF used by `ActionScript3HaxeCastMidExpressionDecompileTest`.

Haxe checks a `cast(obj, String)` inside an expression with
`if (!(obj is String)) throw "Class cast error";`, while the values of the
expression evaluated before it (`"\""` and the class of `esc`) are already on
the stack. It printed as `§§push("\"")`, `§§push(...)` and
`return §§pop() + §§pop().esc(§§pop()) + "\"";`. The pushed values have no side
effect and only the check runs between them and the `return`, so the `return`
now reads them in place of its pops.

## Rebuild

```bash
haxe build.hxml
```

Built with Haxe 4.3.7.
