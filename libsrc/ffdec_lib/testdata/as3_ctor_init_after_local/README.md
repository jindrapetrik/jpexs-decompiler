# as3_ctor_init_after_local

Minimal SWF used by `ActionScript3CtorInitAfterLocalTest`.

ASC emits `setlocal` for an uninitialized ctor local before `initproperty` of a
`Trait_Const` (XML field initializer). Promotion must not stop at that
`setlocal`, or decompilation leaves illegal `const x; x = ...;` in the
constructor.

## Rebuild

```bash
amxmlc -swf-version=43 -debug=false -optimize=true \
  -source-path+=src -output=bin/as3_ctor_init_after_local.swf src/Main.as
```
