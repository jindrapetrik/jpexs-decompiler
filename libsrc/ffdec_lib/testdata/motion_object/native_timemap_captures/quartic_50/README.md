# Native CS6 Simple (Fast) / Quartic strength 50 capture

Captured on 2026-10-04 from example 47. The user identified the native UI
preset as Simple (Fast). The command automatically exported reference_native.swf.

The selected Motion_X property uses TimeMapIndex="4", ignoreTimeMap="0",
referencing TimeMap type="Quartic" strength="50". Its two endpoint keys remain
linear, from 0 to 260 pixels at times 0 and 60000. The earlier Cubic 50 map
remains in the XML but is no longer assigned to Motion_X. Y remains Quadratic
65; X scale remains Quadratic -40.

All 61 frames have the expected Demo instance, 30 fps and 640 x 360 stage.
Observed X progress matches:

    p(t) = t + 0.5 * (1 - (1 - t)^4 - t), t = frameIndex / 60

Maximum X error against the rounded prediction is one twip. Against the
unrounded mathematical prediction it is about 0.050951 px. Compared with the
published example 47, Y matches exactly and X scale differs by at most one
fixed16 unit. Other matrix coefficients, color transforms and filters match.

This verifies Simple (Fast) as Quartic and its positive strength-50 behavior.
Negative and other strengths, and decompiled native Quartic export, remain
unverified. Per-frame comparison CSV and report remain in the ignored capture
build directory.
