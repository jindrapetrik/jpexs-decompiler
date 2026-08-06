# as3_samepackage_imports

Minimal SWF used by `ActionScript3SamePackageImportTest`.

Covers traits written outside the `package { }` block:

- file-private **classes** that reference same-package public types
  (`Outer` / `Helper` / `SharedType`)
- file-private **script functions** (stored as `TraitSlotConst` + `newfunction`
  in script_init) that need other-package imports (`WithScriptFun`)
- public-only classes must not get a trailing self-import (`SharedType`)

Rebuild (optional, requires AIR/Flex SDK):

```bash
amxmlc -swf-version=43 -debug=true -source-path+=src -output=bin/as3_samepackage_imports.swf src/Main.as
```
