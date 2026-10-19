# Native motion-object TimeMaps

The exporter now recognizes native `Quadratic` strength -100 through 100
independently for position, scale, rotation/skew and advanced color channels.
Positive strength eases out; negative strength eases in. Supported samples
must satisfy all held-frame observations within the existing SWF tolerances.
The property is written with two endpoint keys, `ignoreTimeMap="0"` and
`TimeMapIndex` referencing the recovered map.

Native `Spring`, `Bounce` and `BounceIn` are now recognized at independently measured positive strengths 1, 2, 3, 4, 5, 6 and 8. Recognition checks every sample and held frame, recovers the underlying target even when the final pose differs, and emits a linear base curve with its native map. Equal type/strength assignments share a map. Bounce additionally includes the span length in map identity because its segment widths round to whole frames. A coalesced final tail is included when all held frames fit the full native wave. Unmatched curves retain the existing fitting/fallback behavior; native Random export is still unsupported.

59 Java regressions pass. All 42 retained deterministic measurement publications are covered, including held-out strength 6; 21 actual reference SWFs have also been decompiled and their map assignments, strengths and full span lengths checked. The 20 non-equivalent native wave exports reproduce 11,300 source positions within one twip after rounding. BounceIn 1 is equivalent to native Quadratic -100. Dense frame tests can select classic acceleration 100; the actual SWF export selects the equivalent native Quadratic map because of its coalesced source frames.

The [native_wave_exports](native_wave_exports/README.md) are actual SWF -> XFL results, unlike the earlier candidate XFL measurement inputs rejected by CS6. Run [publish_native_wave_exports.jsfl](publish_native_wave_exports.jsfl) to open, save/reopen and publish all 21 exports; each `<id>_compiled.swf` is retained in its example folder. Native run native-wave-roundtrip-1791322188603 verified all 20 non-equivalent cases: 11,300 positions differ by at most one twip, while metadata and all other compared fields match exactly. Imported/persisted map assignments survive save/reopen. BounceIn 1 was stopped by an incorrect classic-only validator requirement; this has been corrected to accept native Quadratic -100 as well. Five simulated command scenarios pass, including the single-case retry. Run [publish_remaining_native_wave_exports.jsfl](publish_remaining_native_wave_exports.jsfl) to rerun only that case. Native retry native-wave-roundtrip-1791322525559 succeeded: its 601 frames differ by at most one twip, all other compared values match exactly, and Quadratic -100 remains assigned after native save/reopen. The complete roundtrip now passes all 21 cases and 11,901 positions.

Map identity includes both type and strength. Equal maps are shared between
properties; `Quadratic` and `DualQuadratic` with equal strengths remain separate.
The neutral `Quadratic` map remains at index zero. Maps ignored by sampled
position, angular or color properties are omitted. Paired spatial cubics keep
their sampled timing when needed. Numeric filter properties still use sampled
keys; their native TimeMap assignment is not implemented in this step.

## Native polynomial maps

The exporter now recognizes `Cubic`, `Quartic`, `Quintic`, `DualCubic`, `DualQuartic` and `DualQuintic` at the independently captured native strength **50**. Recognition runs before generic cubic/piecewise fitting, validates every observation including held frames, and writes two linear base keys with `ignoreTimeMap="0"` and the recovered `TimeMapIndex`. Matching maps are shared between properties, including opposite motion directions. Other strengths, especially negative values, have not been measured and are not enabled as these native maps; the existing curve representation remains available.

For power n=3,4,5, positive strength 50 follows `p=t+0.5*(q-t)`. For the simple types, `q=1-(1-t)^n`; for Dual types, `q=0.5*(1-(1-2*t)^n)` on the first half and `q=0.5*(1+(2*t-1)^n)` on the second half. Captured position residuals establish a maximum one-twip error **after rounding**, not a strict 0.05 px bound against an unrounded formula. DualQuintic's raw residual reaches about 0.0708 px. Quantized position validation therefore uses the captured one-twip criterion only for native wave/polynomial maps; other curve tolerances remain unchanged.

62 Java regression tests pass, including all six captured polynomial SWFs, shared maps with reversed motion, and fallback for an unmeasured strength. Six real SWF -> XFL exports in [native_polynomial_exports](native_polynomial_exports/README.md) have the expected native types, strength, map assignments and two base keys; all 366 serialized X positions match their references within one twip after rounding. They also retain the existing independent Y Quadratic 65 and X-scale Quadratic -40 channels. Native CS6 recompilation passed all six cases in native-polynomial-roundtrip-1791323831469. All 366 X positions differ by at most one twip; the existing Quadratic -40 X-scale channel differs by one fixed16 unit in two frames per case. Metadata, Y, other matrix fields, RGBA, filters and instance flags match exactly. Native types, strengths, assignments and two base keys survive FLA save/reopen. See [ROUNDTRIP.md](native_polynomial_exports/ROUNDTRIP.md) for exact residuals.

Run [publish_native_polynomial_exports.jsfl](publish_native_polynomial_exports.jsfl) in CS6. It opens fresh copies of actual decompiler exports, checks X/Y/scale map assignments and timing, saves/reopens native FLA files, and retains `<id>_compiled.swf` in each example folder. Immutable STEP files and one final STATUS.txt identify the output directory. Per-case failures do not stop the remaining cases; essential XML capture failures are errors, diagnostic-write failures are warnings. Existing compiled outputs are backed up in the fresh diagnostic directory before replacement. Six simulated scenarios pass, including repeat publication, save failure, map loss and capture failure.

## New verification cases

- `47_native_quadratic`: X and X scale share Quadratic -40; Y uses Quadratic 65.
- `48_native_mixed_maps`: X and X scale share Quadratic 50; Y uses DualQuadratic
  50. This checks that identical strengths do not merge different map types.

Both examples have 61 frames, 30 fps, a 640 × 360 stage and a Demo instance.
Open their XFL files and Publish, or run `publish_native_timemaps.jsfl` in CS6.
The command uses fresh diagnostic copies, saves and reopens a native FLA,
captures imported/persisted motion XML and writes each `<example>_compiled.swf`
inside its example folder. It preserves reference SWFs and source XFL files.
Example 47 was published and verified on 2026-10-04: all 61 frames, frame rate,
stage and instance presence match. Position differences are at most one twip
(0.05 px), X-scale differences at most one fixed16 unit (1/65536); all other
matrix coefficients, RGBA transforms and compared instance fields match exactly.
Its imported and persisted XML retained the native map assignments.
The corrected run `native-timemap-publish-1791119810976` completed both examples.
Example 48 also matches all 61 frames, frame rate, stage and instance presence.
Its maximum position error is one twip and maximum X-scale error one fixed16
unit; other matrix coefficients, RGBA transforms and compared instance fields
match exactly. Both examples preserve each property's map type, strength and
assignment after native FLA save/reopen. X and X scale share their map; example
48 keeps Quadratic 50 and DualQuadratic 50 separate. Persisted native XML is
retained as `native_motion.xml` in each example folder, alongside the compiled SWF.

| Example | X differences (frames) | Y differences (frames) | X-scale differences (frames) | Maximum error |
| --- | ---: | ---: | ---: | --- |
| 47_native_quadratic | 35 | 30 | 46 | 1 twip / 1 fixed16 unit |
| 48_native_mixed_maps | 37 | 27 | 48 | 1 twip / 1 fixed16 unit |
The already verified examples 37–46 have not been regenerated in this step.

The first publishing run stopped after example 47 when CS6 refused to overwrite
the shared LAST_STEP.txt diagnostic. The precise cause of that write failure is
unknown. The corrected command completed both cases in native CS6. It creates a separate STEP-NNN.txt for each checkpoint,
a per-case RESULT.txt and one final STATUS.txt. Failure to write a diagnostic
is reported as a warning and does not stop publication; failure to capture
essential motion XML still stops the check. Eleven simulated API scenarios
cover successful publication, denied overwrites, checkpoint failures/exceptions,
native save/reopen/map/capture/export failures and capture publication.

## Capturing further native presets

CS6's Motion Editor has its own preset families (Simple, Stop and Start, Bounce,
Spring and wave curves). The mathematical families in `EASING.md` do not imply
an identically named or identically shaped CS6 TimeMap. Additional native type
names and strength semantics must be measured before enabling their export.

To capture a preset:

1. Open example 47 in CS6 and select its motion span on the root timeline.
2. Open the Motion Editor. Add a native ease and assign it to Motion X.
   Capture one preset and strength at a time; leave the underlying X curve
   linear, with its two endpoint keys. Record the visible preset name/strength.
3. Run `capture_native_timemap.jsfl`. It captures the selected span's XML and
   publishes the entire document into a fresh `build/motion-diagnostic/`
   directory. It does not save or close the active document.
4. Repeat with other presets/strengths, then close the fixture without saving
   your temporary Motion Editor changes.

The captured XML supplies the exact type spelling, parameters and property
assignment; the SWF supplies its actual sampled progress. These captures are
needed for Cubic/Quartic/Quintic, Spring/Bounce and wave-preset support. Existing
sampled easing output remains available until that mapping is established.

References: [Adobe CS6 Motion Editor documentation](https://help.adobe.com/archive/en/flash/cs6/flash_reference.pdf)
and [Adobe setMotionObjectXML API](https://github.com/AdobeDocs/developers-animatesdk-docs/blob/master/Frame_object/frame26.md).

## First additional native preset capture

Capture `native-timemap-capture-1791120443967` completed successfully and is retained in [native_timemap_captures/cubic_50](native_timemap_captures/cubic_50/README.md). It identifies native `Cubic` strength 50 assigned to Motion_X and verifies its sampled positive ease-out blend. The command generated the reference SWF automatically. Negative and other strengths, and decompiled native Cubic export, remain unverified.

## Additional preset name mapping

| CS6 UI name | XML TimeMap type | Verified strength | Capture |
| --- | --- | ---: | --- |
| Simple (Medium) | Cubic | 50 | [cubic_50](native_timemap_captures/cubic_50/README.md) |
| Simple (Fast) | Quartic | 50 | [quartic_50](native_timemap_captures/quartic_50/README.md) |

The Simple (Fast) capture `native-timemap-capture-1791120796579` completed and its Quartic 50 sampled progress was verified over all 61 frames. Captured native XML and SWF are retained. Other strengths and native export of these two newly captured types remain unverified.

## Further native captures

Thirteen additional captures were checked, including all nine user-listed presets in chronological order. The mapping and retained references are in [native_timemap_captures/README.md](native_timemap_captures/README.md). Every run completed and all 61 frames have expected metadata and instance presence. Y matches published example 47 exactly; scale residuals are at most one fixed16 unit; all other compared fields match exactly.

At strength 3, SineWave and SawtoothWave traverse three half-cycles and SquareWave switches at one-third and two-thirds. DampedWave matches three full cosine cycles with a quadratic amplitude envelope about progress 0.5. Polynomial and these wave candidates match rounded native X within one twip; unrounded residuals are recorded per capture. Bounce, BounceIn, Spring and Random have verified samples but no complete independent formula validation yet. Random repeatability remains unknown. A Custom map with CustomTimeMapPoint children is present but not assigned in these captures.

Bounce 4 ends at progress 0, Spring 5 near 0.702 and DampedWave 3 near 0.5. These endpoints differ from the final underlying key value and must be preserved during recognition. New captures do not yet imply native export support or full parameter-range verification.

## Controlled formula measurement campaign

The [measurement inputs](native_timemap_measurements/README.md) cover Bounce, BounceIn, Spring and RandomSquareWave at strengths 1, 2, 3, 4, 5 and 8 (601 frames, 30 fps, 1000 px displacement). Strength 6 uses 361 frames and 24 fps as a held-out validation set. No formula may be declared validated by fitting these validation observations.

Run [measure_native_timemaps.jsfl](measure_native_timemaps.jsfl) using Commands > Run Command in CS6. No manual editing of eases is needed. The command checks imported and reopened motion XML, including type, strength, assignments, timing and linear base handles. It exports deterministic curves before and after native save/reopen. Random is exported twice before and twice after reopening, allowing same-session and cross-session reproducibility comparisons. Source inputs remain untouched. A failed case is reported in RESULT.txt and STATUS.txt; remaining cases continue. STEP files are immutable and diagnostic write failures only produce warnings.

The run directory appears in the final dialog and Output panel under `build/motion-diagnostic/native-timemap-measurement-*`. Run `native-timemap-measurement-1791126570956` completed all 28 cases and 70 exports; metadata and static-channel checks passed. References, samples and [formula verification](native_timemap_measurements/NATIVE_RESULTS.md) are retained in testdata. The analyzer generates `samples_<publication>.csv` and `ANALYSIS.md`, checks frame rate, stage, frame count, instance identity and constant channels, and compares positions across publications. It does not assume that the last progress value is 1 and does not fit a formula automatically.

Compile and run the tools from the `ffdec_lib` directory (PowerShell; use your installed JDK):

```powershell
javac -cp 'build/classes;lib/*' -d build/native-timemap-fixtures test/com/jpexs/decompiler/flash/xfl/NativeTimeMapMeasurementGenerator.java test/com/jpexs/decompiler/flash/xfl/NativeTimeMapMeasurementAnalyzer.java
java -cp 'build/native-timemap-fixtures;build/classes;lib/*' com.jpexs.decompiler.flash.xfl.NativeTimeMapMeasurementGenerator
java -cp 'build/native-timemap-fixtures;build/classes;lib/*' com.jpexs.decompiler.flash.xfl.NativeTimeMapMeasurementAnalyzer 'build/motion-diagnostic/native-timemap-measurement-RUN_ID'
```

`build/classes` must contain the compiled library. Once native observations are available, locate Bounce segment boundaries and fit each parabola, then compare segment durations/amplitudes across strengths; for Spring fit frequency and amplitude decay. Check candidate formulas against strength 6 and the different sampling interval. For Random, compare identical input publications before choosing a deterministic model; if the sequence changes without a recoverable seed, preserve sampled property keys instead.

The first campaign attempt (`native-timemap-measurement-1791125175716`) stopped while opening the first XFL with an "unexpected file format" dialog; it produced no SWF or completed status. Removing the XML declaration did not resolve the failure: the second attempt (`native-timemap-measurement-1791125869888`) stopped at the same import. The precise importer rejection remains unexplained. Neither incomplete run may be used as measurement data.

The command now bypasses those candidate XFL imports. For each case it opens a fresh byte-identical copy of verified example 47, selects the existing motion object, sets its stage, frame rate, initial matrix and native span duration, then applies the separately generated requested_motion.xml. It uses [frame.setMotionObjectDuration](https://github.com/AdobeDocs/developers-animatesdk-docs/blob/master/Frame_object/frame25.md) and [frame.setMotionObjectXML](https://github.com/AdobeDocs/developers-animatesdk-docs/blob/master/Frame_object/frame26.md), both available since CS5. XML application has already been used in the verified native filter workflow. All metadata and map checks still apply before publication and after save/reopen. Nine simulated command scenarios pass, including duration and XML authoring failures, and this revised campaign is now confirmed in native CS6. Generated candidate XFL files remain unverified and are not the command's input documents.

Spring, Bounce and BounceIn formulas pass 23,802 measured frame positions across 42 publications, including six held-out publications, with a maximum rounded error of one twip. Bounce segment widths depend on frame count and are rounded individually. These three native types are now connected to the exporter as described above. Random repeats identically within this run and after FLA save/reopen, but its full PRNG/strength model is unresolved.
