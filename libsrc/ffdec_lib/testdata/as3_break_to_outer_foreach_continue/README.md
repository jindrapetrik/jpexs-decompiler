# as3_break_to_outer_foreach_continue

Minimal SWF used by `ActionScript3BreakToOuterForEachContinueDecompileTest`.

When an inner loop ends the body of a `for each` (or `while`) loop, its natural
exit is the continue part of the outer loop. `LoopDetector.findBreaks` took that
part for an abrupt continue of the outer loop, so it picked another exit as the
break of the inner loop, here the body of the `if`, and printed:

```as3
loop0:
for each(var n:String in names)
{
   do
   {
      for each(var k:String in _loc5_)
      {
      }
      continue loop0;
   }
   while(k != n);
   out += k;
}
```

which always continues the outer loop. The natural exit to the direct parent's
continue is now the break, as it already was for the parent's precontinue (the
increment of a `for`).

## Rebuild

```bash
amxmlc -swf-version=43 -debug=true \
  -source-path+=src -output=bin/as3_break_to_outer_foreach_continue.swf src/Main.as
```
