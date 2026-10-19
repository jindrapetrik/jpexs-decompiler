# Fractional-angle detection fix (native verified)

The detector now accepts sampled angular motion only when each axis can be snapped to an exact quarter degree while reconstructing the original matrix within two fixed16 units. Snapped degrees are emitted directly, avoiding a radians roundtrip across a native lookup boundary. Scale snapping is validated separately for each axis. Smoothness, easing, segmentation, color and instance-boundary checks still apply.

The previous native motion-object publications of 05_animated_skew and 16_rotation_skew_dual_quadratic have maximum matrix errors of 201 and 644 fixed16 units. Their fractional-angle results closely follow a quarter-degree sine lookup whose interpolation weight is the remaining number of degrees rather than four times that remainder. This explains the large errors; residual native rounding is not fully modeled. The detector therefore uses validated table boundaries instead of attempting an unverified inverse compensation.

Both examples now export as 61 ordinary matrix keyframes. Their reference CSVs are byte-identical to the originals. The verified XFLs and matching compiled SWFs are now the current testdata examples. Earlier versions are preserved under the native run directory and their motion XML is retained as unsafe_motion.xml for the historical probes. Supported motion objects, including animated quarter-degree skew and independent DualQuadratic angles, remain supported.

## Native results

The run in build/motion-diagnostic/native-skew-fix-1791074636784 completed and was compared frame by frame:

| Example | Motion objects | Max matrix units | Max position twips |
| --- | ---: | ---: | ---: |
| 05_animated_skew | 0 | 1 (previously 201) | 0 |
| 16_rotation_skew_dual_quadratic | 0 | 1 (previously 644) | 0 |
| quarter_degree | 1 | 3 | 2 |
| 03_rotation | 1 | 3 | 2 |
| 04_static_skew | 1 | 1 | 1 |

All five preserve 61 frames, 30 fps, the stage dimensions, instances, colors and filters. One matrix unit is 1/65536; one twip is 1/20 pixel. These are quantization-level residuals, not exact roundtrips. No further publishing is pending.

## Repeat the native check

Run **Commands > Run Command > native_skew_fix.jsfl** in this folder. It publishes five fresh copies automatically:

- The two corrected exports from `../../build/motion-diagnostic/skew-fix/`.
- A new quarter-degree motion object from that folder, with negative and positive angles and changing scale.
- The existing 03_rotation and 04_static_skew controls.

The command writes to a fresh `../../build/motion-diagnostic/native-skew-fix-TIMESTAMP/` folder, preserves source documents, and closes only its own copies. Each case includes `reference.swf` and `published.swf`; STATUS.txt records export completion, not fidelity. Compare them with the local RoundtripCompare helper after publishing. The completed run includes compare.csv and compare.txt alongside every publication.

Validation: all 17 fixtures export successfully; the other 15 retain their motion-object spans. 31 detector tests pass. The new publishing command passes simulated success and export-failure scenarios, preserving every source file and closing only its own documents. New tests cover the actual two fixture CSVs, preserving the final hold on fallback, and exact quarter-degree property keys. Existing angular tests retain supported animated-skew, reflected-matrix and independent-easing coverage using table-aligned angles. Purely mathematical reconstruction is not sufficient evidence of native publishing fidelity.
