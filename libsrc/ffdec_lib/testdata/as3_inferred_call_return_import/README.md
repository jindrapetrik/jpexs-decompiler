# as3_inferred_call_return_import

Minimal SWF used by `ActionScript3InferredCallReturnImportTest`.

When a local is assigned from `obj.method()` without a typed declaration,
ASC may omit a `coerce` to the method's return type. The decompiler still
types the local from AbcIndexing; that return type must be imported.

Only applies when the call result is stored with `setlocal` (not when it is
immediately cast, e.g. `iterator() as IMapIterator`).

## Rebuild

```bash
amxmlc -swf-version=43 -debug=false \
  -source-path+=src -output=bin/as3_inferred_call_return_import.swf src/Main.as
```
