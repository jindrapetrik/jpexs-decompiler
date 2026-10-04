# 26_gradient_bevel

Inner Gradient Bevel: blur X/Y, strength, angle and distance.

Open `26_gradient_bevel.xfl` in Flash CS6. Publish uses the unique `26_gradient_bevel_compiled.swf` filename.

The synthetic reference has 61 frames, 30 fps and a 640 x 360 stage. `expected_filters.csv` records every gradient stop and filter field.

The decompiled XFL contains 1 motion-object spans using native gradient channels. Run ../native_gradient_roundtrip.jsfl to publish diagnostic copies automatically for native verification. See ../GRADIENT_FILTERS.md.

Native CS6 verification: `native-gradient-roundtrip-1791108933199`. All filter fields match the reference exactly across all 61 frames, including stop RGBA/positions, strength, type and order. Position residuals are at most one twip (0.05 px); other compared instance fields and movie metadata match. The native publication is retained as `26_gradient_bevel_compiled.swf`.
