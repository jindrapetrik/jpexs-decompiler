# 16_rotation_skew_dual_quadratic

Rotation and skew with opposing DualQuadratic easing strengths.

Open `16_rotation_skew_dual_quadratic.xfl` in Flash CS6 and publish to `16_rotation_skew_dual_quadratic_compiled.swf` (already configured).

Reference: `reference.swf`, 30 fps, 640 x 360 pixels, 61 frames. Instance to compare: `Demo`. The export contains 0 motion object spans. The detector retains ordinary keyframes because CS6 cannot faithfully publish these fractional-angle motion properties.

`expected.csv` contains reference matrices and RGBA transforms for each frame. Indices start at zero; matrices use fixed16, multipliers use fixed8, and translations use twips. `preview.png` shows frame 31 of the reference SWF.

The XFL document and reference SWF require no external images, fonts or scripts.

Native CS6 verification passed: maximum matrix difference 1 fixed16 unit, exact positions and colors, 61 frames, no missing instances or filter differences. The matching publication is included in the parent folder. The previous inaccurate motion-object XML is retained as `unsafe_motion.xml` for historical diagnostics. See [roundtrip results](../CS6_ROUNDTRIP.md).
