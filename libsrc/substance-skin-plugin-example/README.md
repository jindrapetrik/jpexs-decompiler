# Example Skin Plugin for Substance 6.2

This project demonstrates a complete minimal plugin that Substance 6.2
automatically discovers on the classpath. It contains:

- the custom `SampleBlueSkin` skin,
- the `SampleSkinPlugin` plugin provider,
- the `META-INF/substance-plugin.xml` descriptor,
- a standalone color scheme, and
- a small Swing window for visual testing.

The project uses `substance-6.2.jar` and `trident-6.2.jar` from the main
`FFDec/lib` directory, so it does not duplicate any libraries.

## Building and Verifying

Run the following command from this directory:

```text
ant clean verify
```

The resulting file is `dist/substance-skin-plugin-example.jar`. The `verify`
target also checks that the skin is valid, the plugin descriptor is present,
and Substance discovers the skin automatically.

Run the visual demo with:

```text
ant run
```

## Using the Plugin in Another Application

Add the resulting JAR to the runtime classpath together with Substance 6.2 and
Trident 6.2. Substance reads `META-INF/substance-plugin.xml` and adds
`Sample Blue` to the map returned by `SubstanceLookAndFeel.getAllSkins()`.

The skin can be activated by its class name:

```java
SubstanceLookAndFeel.setSkin(
        "com.jpexs.examples.substance.SampleBlueSkin");
```

Alternatively, use the standard Swing API:

```java
UIManager.setLookAndFeel(
        new com.jpexs.examples.substance.SampleBlueLookAndFeel());
```

In Substance 6.2, `SubstanceSkinPlugin` belongs to an `internal` package, but
it is the interface required by the plugin mechanism of this specific legacy
version.
