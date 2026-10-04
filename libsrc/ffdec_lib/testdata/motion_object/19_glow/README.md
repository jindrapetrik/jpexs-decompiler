# 19_glow

Open `19_glow.xfl` in Flash CS6 and publish to `19_glow_compiled.swf` (already configured).

`reference.swf` is the native CS6 authoring capture. `native_motion.xml` preserves its native property encoding. The reference has 30 fps, a 640 x 360 stage and 61 frames. The decompiled XFL contains 1 motion-object spans. Filter channels use sampled property keys after independent curve fitting.

`expected_filters.csv` records the actual SWF filter values by frame, stack index, type and property. Do not infer animated properties from the original command's requested poses: some CS6 setters retained their initial values. See ../FILTERS.md for the current native verification results.

Native CS6 verification: `native-filter-roundtrip-1791107327675`, 61 frames at 30 fps. All filter fields and their order match exactly. Position residuals are at most one twip (0.05 px); other compared instance fields match. The matching native publication is retained as `19_glow_compiled.swf`.
