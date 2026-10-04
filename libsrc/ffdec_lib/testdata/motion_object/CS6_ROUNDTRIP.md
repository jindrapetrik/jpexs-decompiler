# Current motion-object roundtrip results

The fractional-angle detection fix has passed native CS6 verification. 05_animated_skew and 16_rotation_skew_dual_quadratic now retain 61 ordinary matrix keyframes, with at most one fixed16 unit of matrix error and exact positions and colors. Their current XFLs and matching compiled SWFs have been replaced by the verified exports. The other 15 examples retain motion objects.

The additional quarter-degree motion-object control passed with at most 3 matrix units and 2 twips of residual error. Pure rotation retains the same small residual error. See [SKEW_FIX.md](SKEW_FIX.md) for the detector rule and measurements.

## Current native results

| Example | Differing frames | Max matrix units | Max position twips | Max multiplier units | Max offset units | Assessment |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| 01_position | 32 | 0 | 1 | 0 | 0 | One-unit quantization differences |
| 02_scale | 47 | 1 | 0 | 0 | 0 | One-unit quantization differences |
| 03_rotation | 60 | 3 | 2 | 0 | 0 | Large angular error fixed; small residual differences |
| 04_static_skew | 60 | 1 | 1 | 0 | 0 | One-unit quantization differences |
| 05_animated_skew | 21 | 1 | 0 | 0 | 0 | Verified ordinary-keyframe fallback |
| 06_reflection | 44 | 1 | 0 | 0 | 0 | One-unit quantization differences |
| 07_rgba_multipliers | 53 | 0 | 0 | 1 | 0 | One-unit quantization differences |
| 08_rgba_offsets | 56 | 0 | 0 | 0 | 1 | One-unit quantization differences |
| 09_alpha_dual_quadratic | 0 | 0 | 0 | 0 | 0 | Exact |
| 10_motion_dual_quadratic | 56 | 1 | 1 | 0 | 0 | One-unit quantization differences |
| 11_color_dual_quadratic | 54 | 0 | 0 | 1 | 1 | One-unit quantization differences |
| 12_multiple_segments | 38 | 0 | 1 | 1 | 0 | One-unit quantization differences |
| 13_quantized_holds | 35 | 0 | 0 | 1 | 0 | One-unit quantization differences |
| 14_final_hold | 32 | 0 | 1 | 0 | 0 | One-unit quantization differences |
| 15_instance_restart | 37 | 0 | 1 | 0 | 0 | One-unit quantization differences |
| 16_rotation_skew_dual_quadratic | 25 | 1 | 0 | 0 | 0 | Verified ordinary-keyframe fallback |
| 17_three_segments | 35 | 0 | 1 | 0 | 0 | One-unit quantization differences |

One fixed16 matrix unit is 1/65536, one position twip is 1/20 pixel, and one fixed8 multiplier unit is 1/256. All examples have matching frame counts, 30 fps, a 640 x 360 stage, root instance presence and filters. Comparison covers root instance Demo; character IDs and binary file layout are not compared.

## What is confirmed

- Sampled property keys fixed the large paired-path timing errors, including the final hold and instance restart.
- Native percentage keys preserve multipliers above 100%. Choosing the nearest representable whole percentage reduced multiplier errors to at most one fixed8 unit.
- Independent axis angles fixed the constant-skew example; its matrix and position differences are at most one unit.
- The alpha DualQuadratic example still matches exactly.
- Validated integer-degree snaps are now written exactly, without converting them back to a nearby non-integer through radians. Native CS6 distinguishes 29.999999999999996 from 30. The corrected 03_rotation motion properties match the native-tested angles_six_decimals variant exactly. Its published SWF is included; the previous publication is preserved in the local diagnostic output folder.

## Fractional-angle limitation and historical experiments

The previous sampled-angle motion encodings had errors up to 201 matrix units for 05_animated_skew and 644 for 16_rotation_skew_dual_quadratic. Sparse recovered angular curves did not fix them (190 and 855 units respectively). Nine alternative encodings also failed; see [NATIVE_ENCODING_RESULTS.md](NATIVE_ENCODING_RESULTS.md). Those representations are not faithful motion objects.

Observed CS6 fractional-angle behavior closely follows a discontinuous quarter-degree sine lookup. The detector now emits only exact quarter-degree angles that reconstruct each sampled matrix within its existing tolerance; unsupported angular spans keep their original matrix keyframes. The native quarter-degree control confirms small residual quantization error rather than the earlier large angular error. A general faithful motion-object encoding for arbitrary fractional-angle matrices remains unsupported.

The original failing XML is retained as unsafe_motion.xml in each affected fixture. Historical authoring and encoding probes use it when present. Previous complete fixtures and publications are backed up in build/motion-diagnostic/native-skew-fix-1791074636784/before-promotion. Native comparison CSVs and reports are in that run's five case folders.

## Validation

All 31 detector tests and all 17 fixture exports pass. The other 15 fixtures retain their motion-object spans. No further native publishing is pending. All native comparisons cover root instance Demo, frame counts, frame rate, stage dimensions, instance presence, transforms, colors and filters; they do not establish binary equality or exact pixel equality.
