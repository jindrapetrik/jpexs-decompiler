# as3_try_after_compound_continue

Minimal SWF used by `ActionScript3TryAfterCompoundContinueDecompileTest`.

A loop whose body starts with `if (a && b) continue;` and goes on with a
`try`/`catch` makes the graph walk its precontinue block first. That walk is
discarded, but it had already marked the exception as parsed, so the real walk
of the body did not see the `try` anymore: its body printed without it, and
its catch after the `return`, as `error = §§pop();` and `§§goto`.

## Rebuild

```bash
amxmlc -swf-version=43 -debug=true \
  -source-path+=src -output=bin/as3_try_after_compound_continue.swf src/Main.as
```
