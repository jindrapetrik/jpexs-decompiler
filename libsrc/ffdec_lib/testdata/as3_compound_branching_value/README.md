# as3_compound_branching_value

Minimal SWF used by `ActionScript3CompoundBranchingValueDecompileTest`.

ASC compiles a compound assignment to an unqualified property as
`findpropstrict p; dup; getproperty p; <value>; <op>; setproperty p`. When the
value branches (`_s += (i == 0 ? "" : ", ") + "x"`), the `dup` temporary is
already output before the branch, and folding the assignment back into `+=`
left it printed as a stray `var _temp_1:* = §§findproperty(_s);`, which does
not compile.

## Rebuild

```bash
amxmlc -swf-version=43 -debug=true \
  -source-path+=src -output=bin/as3_compound_branching_value.swf src/Main.as
```
