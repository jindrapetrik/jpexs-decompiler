# as3_ctor_field_init

Minimal SWF used by `ActionScript3CtorFieldInitTest`.

Covers constructor assignments that read activation slots via `getlex`
(`NEED_ACTIVATION`, named params captured by a nested function). Those must
stay in the constructor body and must not become field initializers such as
`var mValue:Object = value;`.

Also keeps a local-register path (`TestCtorFieldInit`) and literal field
inits (`mLiteral`) that should still be promoted.

`TestCtorDependsOnInstanceSlot` covers ctor assigns that read another
instance slot set earlier in the same constructor (e.g. `mText = String(mSource)`
after `mSource = data`). Those must stay in the constructor: field initializers
run before the ctor body, so the source slot is still null.

## Rebuild

```bash
amxmlc -swf-version=43 -debug=true \
  -source-path+=src -output=bin/as3_ctor_field_init.swf src/Main.as
```

`TestCtorActivationFieldInit` must keep **named parameters** captured by an
**inline** nested function. Extra locals such as `var value:Object = param1`
change the bytecode so the activation is stored in a register, and the bug is
no longer exercised.
