# haxe_compound_initproperty

Minimal SWF used by `ActionScript3CompoundInitPropertyDecompileTest`.

Haxe compiles a compound assignment to a property of an expression, such as
`p.x += 1.5`, as `<p>; dup; getproperty x; <value>; add; initproperty x`
(ASC uses `setproperty`). The `dup` temporary folded back into `+=` was only
replaced by its source for `setproperty`, so the `initproperty` one printed as
an undeclared `_temp_1.x += 1.5;`, which does not compile.

## Rebuild

```bash
haxe build.hxml
```

Built with Haxe 4.3.7.
