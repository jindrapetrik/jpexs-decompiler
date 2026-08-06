# as3_samepackage_imports

Minimal SWF used by `ActionScript3SamePackageImportTest`.

A public class and a file-private helper live in the same script. The helper
references another public type from the same package, so the decompiler must
emit an `import` for that type outside the `package { }` block.

Rebuild (optional, requires AIR/Flex SDK):

```bash
amxmlc -swf-version=43 -debug=true -source-path+=src -output=bin/as3_samepackage_imports.swf src/Main.as
```
