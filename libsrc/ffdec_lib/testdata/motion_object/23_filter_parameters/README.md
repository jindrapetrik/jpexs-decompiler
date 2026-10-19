# 23_filter_parameters

Open `23_filter_parameters.xfl` in Flash CS6 and publish to `23_filter_parameters_compiled.swf` (already configured).

`reference.swf` is a synthetic four-filter reference covering numeric parameters, RGB, alpha and strength at SWF precision. The reference has 30 fps, a 640 x 360 stage and 61 frames. The decompiled XFL contains 2 motion-object spans. CS6 motion-object strength uses integer percent. Nonrepresentable fixed8 samples retain ordinary filter keyframes; representable subspans may use motion objects.

`expected_filters.csv` records the actual SWF filter values by frame, stack index, type and property. Do not infer animated properties from the original command's requested poses: some CS6 setters retained their initial values. See ../FILTERS.md for the current native verification results.

Native CS6 verification: `native-filter-roundtrip-1791107327675`, 61 frames at 30 fps. All filter fields except strength match exactly. Strength differs in 45 frames by at most 3/256 because CS6 truncates it to whole percent even in ordinary keyframes. This is a documented compiler precision limitation. The matching native publication is retained as `23_filter_parameters_compiled.swf`.
