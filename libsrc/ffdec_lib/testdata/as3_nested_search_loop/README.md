# as3_nested_search_loop

Minimal SWF used by `ActionScript3NestedSearchLoopTest`.

ASC merges an inner `while` search `break` with the outer `for each`
continue target. Without restructuring, decompilation emits:

```
loop0:
for each (...) {
  while (true) {
    if (i >= length) {
      continue loop0;
    }
    if (match) {
      break;
    }
    i++;
  }
  // side effect that belonged inside the match branch
}
```

The expected form matches the original source: `while (i < length)` with
the side effect before `break`, and no loop labels.

## Rebuild

```bash
amxmlc -swf-version=43 -debug=false -optimize=true \
  -source-path+=src -output=bin/as3_nested_search_loop.swf src/Main.as
```
