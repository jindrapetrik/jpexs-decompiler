# Native CS6 Sawtooth Wave / SawtoothWave strength 3

Captured on 2026-10-04; run: native-timemap-capture-1791121189098.

Motion_X uses TimeMapIndex 14, ignoreTimeMap 0, selecting SawtoothWave strength 3. Other unused maps do not compose with this assignment. The underlying linear keys remain 0 to 260 pixels, times 0 to 60000.

The script automatically generated reference_native.swf. All 61 frames retain 30 fps, the 640 x 360 stage and Demo instance. Y matches published example 47 exactly; X scale differs by at most one fixed16 unit. Other matrix coefficients, RGBA transforms, filters and compared instance flags match exactly.

Samples are preserved in samples.csv: progress = (xTwips - 3200) / 5200. Initial progress 0; final progress 1; range 0 to 1. Native waves and bounces need not reach the final underlying key value.

The formula candidate in formula-check.json matches all 61 rounded X positions within 1 twip(s). Maximum unrounded residual: 0.050000 px. This verifies only the captured parameter value.

Other strengths and decompiler export using this preset remain unverified.
