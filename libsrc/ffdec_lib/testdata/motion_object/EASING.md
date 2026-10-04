# Additional easing examples

Examples 37–46 cover Quad, Cubic, Quart, Quint, Sine, Circular, Exponential,
Back, Bounce and Elastic. Each example combines In on X, Out on Y and InOut
on X scale. There are 61 frames at 30 fps on a 640 × 360 stage.

The detector recognizes these standard curve families independently for each
numeric channel, including color transforms and animated filter parameters.
It also accepts a blend between a preset and linear progress. All observations,
including the interior of held SWF frames, must fit within the existing SWF
precision tolerances. A preset requires at least eleven distinct observations
and a changing endpoint. Existing cubic, DualQuadratic and piecewise detection
remains available. Configurable bounce counts, elastic periods/amplitudes and
back overshoot amounts are not recovered as new presets.

Recognized non-cubic presets use property keys at every SWF frame in one motion
object. This preserves sharp bounces, overshoots and quantized holds using the
already verified property-key XML schema. No guessed Adobe TimeMap type names
are emitted. The Motion Editor therefore displays the sampled curve, rather
than selecting a named easing preset. Curves that already fit existing cubic
or DualQuadratic representations can retain those more compact representations.

These are mathematical recognition candidates, not a claim that CS6's named
presets all use identical formulas. Standard easing references:
[CreateJS easing equations](https://github.com/CreateJS/TweenJS/blob/master/src/tweenjs/Ease.js)
and [Adobe motion easing documentation](https://helpx.adobe.com/animate/using/adding-custom-eases.html).
Back uses overshoot 1.70158 (1.525 times that amount for InOut); Elastic uses
period 0.3 (0.45 for InOut), unit amplitude; Bounce uses four parabolic arcs.

## Native Flash CS6 verification

All ten examples were published in native Flash CS6 and verified on 2026-10-04.
Each reference and published SWF has 61 frames, 30 fps and a 640 × 360 stage.
The Demo instance is present in all 610 compared states. RGBA transforms,
visibility, caching, blend mode, clipping, ratio and filters match exactly.
Bounce and Elastic retain their sampled reversals and overshoots.

| Example | Max X error (twips) | Max Y error (twips) | Max X-scale error (fixed16 units) |
| --- | ---: | ---: | ---: |
| 37_ease_quad | 1 | 1 | 1 |
| 38_ease_cubic | 1 | 1 | 0 |
| 39_ease_quart | 1 | 1 | 0 |
| 40_ease_quint | 1 | 1 | 0 |
| 41_ease_sine | 1 | 1 | 0 |
| 42_ease_circ | 1 | 1 | 0 |
| 43_ease_expo | 1 | 1 | 0 |
| 44_ease_back | 1 | 1 | 0 |
| 45_ease_bounce | 1 | 1 | 0 |
| 46_ease_elastic | 1 | 1 | 0 |

One twip is 0.05 px; one fixed16 unit is 1/65536. Only Quad has a scale
rounding residual (45 of 61 frames); all other matrix coefficients match
exactly. Every positional difference is at most one twip. The result verifies
the exported property-key representation; it does not establish the native
TimeMap names or formulas of Adobe's named easing presets.

The compiled SWFs are included in the example folders. To repeat the check,
open each XFL and Publish, or run `publish_easings.jsfl`. Outputs use the unique
`<example>_compiled.swf` filename inside each example's folder. Keep
`reference.swf` unchanged and compare the published frames against
`expected.csv`. Detailed comparison reports from this run are in
`build/motion-diagnostic/<example>-roundtrip.txt` and `.csv` (ignored build output).

Each folder includes a preview, its sampled reference SWF and an English README.
Generate selected fixtures with `MotionTweenFixtureGenerator` and a comma-separated
list of IDs as its second argument; this preserves the consolidated README.

A subsequent export improvement adds explicit native Quadratic TimeMaps for matching curves. See [NATIVE_TIMEMAPS.md](NATIVE_TIMEMAPS.md). The verified examples 37–46 and their compiled SWFs retain the representation measured above.
