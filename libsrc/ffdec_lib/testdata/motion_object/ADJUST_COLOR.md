# Adjust Color motion-object support

Adjust Color detection/export now uses the brightness, contrast, saturation and hue attributes produced by the existing XFLConverter.convertAdjustColorFilter and ColorMatrixConvertor. Each channel must fit a supported independent curve before becoming a motion object. Signed integer values are preserved with per-frame property keys, including held samples. Missing attributes default to zero. Unknown attributes, invalid ranges, duplicate filter types and irregular curves retain ordinary filter frames.

Native pose XML confirms the AdjustColor_Filter container and numeric properties AdjColor_Brightness, AdjColor_Contrast, AdjColor_Saturation and AdjColor_Hue. The exporter uses those measured property names. The corrected decompiled exports have now completed native CS6 comparison, with the float residuals documented below. The command captures the native schema and publishes the decompiled exports in the same run so both checks can be reproduced.

## Native CS6 results

Run `native-adjust-color-1791112001348` completed all six cases without the earlier import crash. All four recovered Adjust Color parameters match in all 61 frames of each example. Every filter count, type and stack position matches; the constant Blur and Glow filters in example 36 are exact. All movies retain 61 frames, 30 fps and a 640 x 360 stage. Other compared instance fields match, except for position residuals of at most one twip (0.05 px).

All 20 color-matrix elements were compared as actual SWF floats. Brightness is bit-identical. The other references show small numerical residuals consistent with different float evaluation in the Java converter and CS6; they are not exact matrix roundtrips.

| Example | Frames with matrix differences | Maximum coefficient difference | Maximum offset difference |
| --- | ---: | ---: | ---: |
| 31 brightness | 0 | 0 | 0 |
| 32 contrast | 49 | 1.19e-7 | 6.20e-6 |
| 33 saturation | 9 | 2.38e-7 | 0 |
| 34 hue | 4 | 1.19e-7 | 0 |
| 35 combined | 61 | 4.77e-7 | 7.63e-6 |
| 36 stack | 61 | 4.77e-7 | 7.63e-6 |

For RGBA input channels in 0–255, the largest row-wise linear output-error bound is 0.000247 channel units, calculated as 255 times the sum of absolute coefficient differences plus the absolute offset difference. This bounds the unrounded matrix result; it does not assert bit-identical rendered pixels at integer rounding boundaries.

Native publications are retained as `<example>_compiled.swf` in each example folder, alongside the recorded pose schema native_motion.xml. Full comparison logs, per-frame differences and all matrix-element deltas remain in the diagnostic run directory as comparison.txt/csv and matrix-comparison.txt/csv.

## Interrupted CS6 runs and corrected channel names

Both native-adjust-color-1791111558381 and native-adjust-color-1791111645343 captured the three brightness poses and successfully published the native authored reference. They stopped when opening the first decompiled XFL, before imported_motion.xml was saved. That XFL used AdjustColor_* property names; the captured native schema uses AdjColor_*. The incorrect property identifiers are a probable cause of the native import crash.

All six decompiled examples have been regenerated with AdjColor_* names. A regression test compares their identifiers with the captured native schema. The command validates those names before opening a decompiled XFL, rejects the old identifiers, and writes LAST_STEP.txt and a running STATUS.txt before major native calls. If CS6 terminates again, the diagnostic directory preserves the last attempted stage. The corrected native run completed all six imports and publications successfully; that observed result verifies these examples, rather than guaranteeing that CS6 can never crash on other input.

## Matrix conversion fix

While generating the references, pure saturation changes sometimes recovered hue as +/-180 instead of zero. The existing zero-hue recognition compared matrix coefficients after multiplying them by large integer constants, amplifying SWF float rounding errors. The comparisons now use values at the original matrix scale. Regression tests cover 200 nondegenerate saturation settings with three contrast settings, as well as the 61-frame combined animation. The existing converter and conversion order are retained.

This feature inherits the existing converter's representability limits. Arbitrary SWF color matrices can contain transformations outside the four Adjust Color controls, and singular matrices can have ambiguous parameter decompositions. These examples use matrices constructed from the existing four-parameter converter. Validation must compare all 20 matrix values as well as the recovered parameters; parameter agreement alone does not establish native compiler fidelity.

## Examples

Each reference has 61 frames, 30 fps and a 640 x 360 stage, with a unique publication filename. All six currently export one motion-object span.

- 31_adjust_brightness: brightness from -40 to 40 with a cubic curve.
- 32_adjust_contrast: contrast from -30 to 30 with a quadratic curve.
- 33_adjust_saturation: saturation from -40 to 40 with DualQuadratic easing.
- 34_adjust_hue: hue from -90 to 90.
- 35_adjust_combined: independently changing brightness, contrast, saturation and hue.
- 36_adjust_stack: ordered Blur, Adjust Color and Glow stack.

reference.swf contains the source color matrices. expected_filters.csv records every matrix element and other filter fields; expected_parameters.csv records the existing converter's recovered parameters. capture_pose_0.xml through capture_pose_2.xml preserve the static filter poses for frames 0, 30 and 60 separately from the exported motion XML.

## Native capture and export verification

In CS6 run **Commands > Run Command > native_adjust_color_filters.jsfl** once. No separate Publish is required. The command preserves source examples and existing publications and uses fresh copies under **ffdec_lib/build/motion-diagnostic/native-adjust-color-TIMESTAMP**.

For each example it imports three static filtered poses, then calls native createMotionObject(0, 61). It records each pose's native XML, filter properties and native FLA. It constructs native filter keys from those actual pose encodings, saves and reopens the animation, and publishes reference_native.swf. That native authored movie is diagnostic schema data; its three-pose interpolation is not assumed to equal the synthetic reference curve.

The command then opens a fresh copy of the actual decompiled XFL, saves native_export.fla and imported_motion.xml, and publishes published.swf. Compare **published.swf against the example's reference.swf** to assess the decompiler export. STATUS.txt records per-case errors and successes; native pose data is preserved before filter validation so that failure diagnostics remain useful.

## Checks

51 Java tests pass: the existing 44 tests, two converter regressions and five Adjust Color detector tests. They cover independent signed channels, defaults, final holds, stack order, malformed/duplicate filters and irregular curves. Seven simulated command scenarios check source preservation, closing only owned documents, and capture/publication failures, checkpoint recording and refusal to open an export with the old incorrect prefix. The separate native CS6 results above establish the measured compiler fidelity of the six examples.
