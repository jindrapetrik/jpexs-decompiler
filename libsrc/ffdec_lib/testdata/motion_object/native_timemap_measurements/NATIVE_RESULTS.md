# Native CS6 measurement results

Run `native-timemap-measurement-1791126570956` completed all 28 cases and 70 exports. The revised command opened unchanged copies of example 47 and authored each measurement through native CS6 APIs. This confirms the measurement command; direct import of the generated candidate XFL remains unsuccessful and unexplained.

Each case's `native_capture/` folder retains imported/persisted motion XML, all measured SWFs, per-frame CSVs and its RESULT.txt. Native FLA files and seed copies remain in the original diagnostic directory. [STATUS.txt](STATUS.txt) records the 28 successful cases; [ANALYSIS.md](ANALYSIS.md) reports metadata/static-channel checks and publication comparisons.

Every SWF has the expected frame count, frame rate and stage, one Demo instance, constant Y, identity scale/skew, neutral color transform and unchanged instance flags. Deterministic before/after-save publications differ by at most one twip. All four Random publications of each input have exactly equal sampled X positions, including after FLA save/reopen. This establishes repeatability for these inputs in this run, not a universal seed rule or reproducibility across CS6 process restarts.

## Candidate formulas

The following formulas were derived from discovery strengths 1, 2, 3, 4, 5 and 8. Strength 6 (361 frames, 24 fps) was used only for verification. [FORMULA_CHECK.md](FORMULA_CHECK.md) compares both imported and persisted publications: 42 SWFs, 23,802 frame positions, maximum error of one twip after rounding the mathematical position to integer twips. Raw residuals are listed separately. Only the measured positive integer strengths and lengths are confirmed. Spring, Bounce and BounceIn are now implemented in the exporter; see [actual decompiler exports](../native_wave_exports/README.md) and [implementation status](../NATIVE_TIMEMAPS.md). All 21 exports are now verified by recompilation in CS6: 11,901 positions differ by at most one twip, while all other compared values match exactly; the historical formula-check report predates this integration.

### Spring

For normalized time `t = frameIndex / (frameCount - 1)` and strength `s`, define `a = 0.4 / s`. Then:

```text
t <= a: p(t) = sin(pi * t / (2*a))
t >  a: u = 2*s*(t-a)/(1-a)
        p(t) = 0.7 + 0.3*exp(-u/2)*cos(pi*u)
```

The first segment is a sine rise. Subsequent motion oscillates about 0.7 with exponential decay. For a positive integer strength, the final value is `0.7 + 0.3*exp(-s)`, rather than 1.

### Bounce and BounceIn

Let `N = frameCount - 1`, `H_s = sum(1/j, j=1..s)`. Segment `j` has native width `round(N/(j*H_s))` **in frames**. Accumulate these widths to find its start frame. Truncate a segment at frame N if needed. If the rounded widths undershoot N, retain a flat tail at the endpoint.

For local segment progress `u` from 0 to 1:

```text
Bounce, every segment j: p = exp(1-j) * 4*u*(1-u)
BounceIn, segment 1:     p = u*u
BounceIn, segment j>1:   p = 1 - exp(1-j) * 4*u*(1-u)
```

Bounce ends at 0; BounceIn ends at 1. Continuous harmonic segment widths without native frame rounding are insufficient: they produce errors exceeding 160 twips in the held-out case, compared with at most one twip for the frame-rounded model. Consequently recognition/export must consider the tween's frame count and preserve the native endpoint/tail behavior.

### RandomSquareWave

Repeated publications preserve identical samples. The sampled nonzero levels form a shared prefix across strengths: approximately 0.3166, 0.4175, 0.53075 and 0.0362 in this 1000 px experiment, alternating with zero. These are quantized observations, not recovered exact PRNG constants. Strength controls the number of intervals; transition boundaries need careful floating-point/native timing treatment (strength 5 changes at frame 361 rather than the naive 360). The endpoint is zero even for odd strengths.

Neither the PRNG/seed rule nor a complete arbitrary-strength model has been recovered. Exact native Random export remains unsupported; repeated equality alone does not justify inventing the rest of its sequence.

## Reproducing formula verification

Run NativeTimeMapMeasurementAnalyzer on the diagnostic directory first. Then compile/run `test/com/jpexs/decompiler/flash/xfl/NativeTimeMapFormulaCheck.java` using the same classpath as the analyzer. The formula checker reads the extracted samples, does not fit validation data and rejects incomplete campaigns or rounded residuals above one twip. Retained case CSVs are copies of those checked samples.
