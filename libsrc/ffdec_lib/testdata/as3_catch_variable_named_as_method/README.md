# as3_catch_variable_named_as_method

Minimal SWF used by `ActionScript3CatchVariableNamedAsMethodDecompileTest`.

ASC puts the name of a catch variable in the internal namespace of the package.
When the class also has a public member with that name, here a static method
`error` next to `catch (error:Error)`, the script has the name twice and it
becomes a fully qualified one. The catch variable was then printed with its
package, as `catch(tests.error:Error)`, which is not ActionScript.

## Rebuild

```bash
amxmlc -swf-version=43 -debug=true \
  -source-path+=src -output=bin/as3_catch_variable_named_as_method.swf src/Main.as
```
