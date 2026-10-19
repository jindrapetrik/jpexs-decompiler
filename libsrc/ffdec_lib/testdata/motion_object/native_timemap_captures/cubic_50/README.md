# Native CS6 Simple (Medium) / Cubic strength 50 capture

Captured from example 47 on 2026-10-04. The user identified the native UI preset as Simple (Medium). The capture command automatically
published reference_native.swf; manual publication was not required.

The native XML contains TimeMap type="Cubic" strength="50" at index 3,
assigned to Motion_X through TimeMapIndex="3", ignoreTimeMap="0". Motion_X
retains its two linear endpoint keys (0 to 260 pixels, time 0 to 60000).
Y remains Quadratic 65 and X scale remains Quadratic -40.

All 61 SWF frames retain 30 fps, the 640 x 360 stage and the Demo instance.
Observed X progress matches:

    p(t) = t + 0.5 * (1 - (1 - t)^3 - t), t = frameIndex / 60

The maximum difference from the rounded predicted X position is one twip.
The maximum difference from the unrounded mathematical position is about
0.051852 px, including native interpolation and quantization. Relative to
the unchanged published example 47, Y matches exactly and X scale differs by
at most one fixed16 unit. Relative to the original sampled example 47,
Y differs by at most one twip and X scale by at most one fixed16 unit.
Other matrix coefficients, color transforms and filters match the control.

This verifies the Cubic identifier, its assignment and the observed positive
strength-50 behavior. It does not yet verify negative strength, other strengths
or native Cubic export from the decompiler. Further captures are needed before
claiming the entire native preset family is verified.

Full per-frame reports are in the ignored diagnostic capture directory.
