# haxe_static_init_imports

Minimal SWF used by `ActionScript3HaxeStaticInitImportsTest`.

In a SWC, Haxe initializes a class's static vars in its script initializer,
whose values are printed on the class's vars. Their dependencies were not
collected for the class, so a type from another package, here
`other.Consts`, was not imported.

## Rebuild

`bin/haxe_static_init_imports.swf` is the `library.swf` of the SWC:

```bash
haxe build.hxml
unzip -p bin/haxe_static_init_imports.swc library.swf > bin/haxe_static_init_imports.swf
rm bin/haxe_static_init_imports.swc
```

Built with Haxe 4.3.7.
